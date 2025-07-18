export interface PduParseResult {
  pduType: string;
  sender: string;
  message: string;
  timestamp: string;
  encoding: string;
  messageLength: number;
  smscLength: number;
  smscType: string;
  senderLength: number;
  status: string;
  errorMessage?: string;
  technicalDetails: TechnicalDetail[];
  rawPduBreakdown: PduBreakdown;
}

export interface TechnicalDetail {
  field: string;
  value: string;
  hex: string;
  description: string;
}

export interface PduBreakdown {
  smscInfo: string;
  pduType: string;
  sender: string;
  timestamp: string;
  message: string;
}

export class PduParser {
  static parse(pduString: string): PduParseResult {
    try {
      // Remove any whitespace and ensure uppercase
      const pdu = pduString.replace(/\s/g, '').toUpperCase();
      
      // Basic validation
      if (pdu.length < 10) {
        throw new Error('PDU too short');
      }

      let offset = 0;
      
      // Parse SMSC length
      const smscLength = parseInt(pdu.substr(offset, 2), 16);
      offset += 2;
      
      // Parse SMSC information
      const smscInfo = pdu.substr(offset, smscLength * 2);
      offset += smscLength * 2;
      
      // Parse PDU type
      const pduTypeByte = parseInt(pdu.substr(offset, 2), 16);
      offset += 2;
      
      const pduType = this.getPduType(pduTypeByte);
      
      // Parse sender address length
      const senderLength = parseInt(pdu.substr(offset, 2), 16);
      offset += 2;
      
      // Parse sender address type
      const senderType = pdu.substr(offset, 2);
      offset += 2;
      
      // Parse sender address
      const senderDigits = Math.ceil(senderLength / 2);
      const senderHex = pdu.substr(offset, senderDigits * 2);
      const sender = this.parsePhoneNumber(senderHex, senderLength);
      offset += senderDigits * 2;
      
      // Parse PID (Protocol Identifier)
      const pid = pdu.substr(offset, 2);
      offset += 2;
      
      // Parse DCS (Data Coding Scheme)
      const dcs = pdu.substr(offset, 2);
      offset += 2;
      const encoding = this.getEncoding(parseInt(dcs, 16));
      
      // Parse timestamp (7 bytes for SMS-DELIVER)
      let timestamp = '';
      if (pduType === 'SMS-DELIVER') {
        const timestampHex = pdu.substr(offset, 14);
        timestamp = this.parseTimestamp(timestampHex);
        offset += 14;
      }
      
      // Parse UDL (User Data Length)
      const udl = parseInt(pdu.substr(offset, 2), 16);
      offset += 2;
      
      // Parse User Data
      const userData = pdu.substr(offset);
      const message = this.decodeMessage(userData, encoding, udl);
      
      const technicalDetails: TechnicalDetail[] = [
        {
          field: 'SMSC Length',
          value: smscLength.toString(),
          hex: pdu.substr(0, 2),
          description: 'Length of SMSC information'
        },
        {
          field: 'SMSC Type',
          value: this.getSmscType(smscInfo),
          hex: smscInfo.substr(0, 2),
          description: 'Type of SMSC address'
        },
        {
          field: 'PDU Type',
          value: pduType,
          hex: pdu.substr((smscLength + 1) * 2, 2),
          description: 'Type of PDU message'
        },
        {
          field: 'Sender Length',
          value: senderLength.toString(),
          hex: pdu.substr((smscLength + 2) * 2, 2),
          description: 'Length of sender address'
        },
        {
          field: 'PID',
          value: parseInt(pid, 16).toString(),
          hex: pid,
          description: 'Protocol identifier'
        },
        {
          field: 'DCS',
          value: encoding,
          hex: dcs,
          description: 'Data coding scheme'
        },
        {
          field: 'UDL',
          value: udl.toString(),
          hex: pdu.substr(offset - 2, 2),
          description: 'User data length'
        }
      ];

      const rawPduBreakdown: PduBreakdown = {
        smscInfo: pdu.substr(0, (smscLength + 1) * 2),
        pduType: pdu.substr((smscLength + 1) * 2, 2),
        sender: pdu.substr((smscLength + 2) * 2, (Math.ceil(senderLength / 2) + 1) * 2),
        timestamp: timestamp ? pdu.substr(offset - 16, 14) : '',
        message: userData
      };

      return {
        pduType,
        sender,
        message,
        timestamp: timestamp || new Date().toISOString(),
        encoding,
        messageLength: message.length,
        smscLength,
        smscType: this.getSmscType(smscInfo),
        senderLength,
        status: 'Valid',
        technicalDetails,
        rawPduBreakdown
      };
      
    } catch (error) {
      return {
        pduType: 'Unknown',
        sender: '',
        message: '',
        timestamp: '',
        encoding: 'Unknown',
        messageLength: 0,
        smscLength: 0,
        smscType: 'Unknown',
        senderLength: 0,
        status: 'Invalid',
        errorMessage: error instanceof Error ? error.message : 'Unknown error',
        technicalDetails: [],
        rawPduBreakdown: {
          smscInfo: '',
          pduType: '',
          sender: '',
          timestamp: '',
          message: ''
        }
      };
    }
  }

  private static getPduType(pduTypeByte: number): string {
    const mti = pduTypeByte & 0x03;
    switch (mti) {
      case 0: return 'SMS-DELIVER';
      case 1: return 'SMS-SUBMIT';
      case 2: return 'SMS-STATUS-REPORT';
      case 3: return 'Reserved';
      default: return 'Unknown';
    }
  }

  private static getSmscType(smscInfo: string): string {
    if (smscInfo.length < 2) return 'Unknown';
    const type = parseInt(smscInfo.substr(0, 2), 16);
    if (type === 0x91) return 'International';
    if (type === 0x81) return 'National';
    return 'Unknown';
  }

  private static parsePhoneNumber(hex: string, length: number): string {
    let number = '';
    for (let i = 0; i < hex.length; i += 2) {
      const byte = hex.substr(i, 2);
      if (byte === 'F0' || byte === '0F') break;
      number += byte[1] + byte[0];
    }
    return number.replace(/F/g, '').substr(0, length);
  }

  private static parseTimestamp(timestampHex: string): string {
    try {
      const year = parseInt(timestampHex.substr(0, 2)[1] + timestampHex.substr(0, 2)[0], 10) + 2000;
      const month = parseInt(timestampHex.substr(2, 2)[1] + timestampHex.substr(2, 2)[0], 10);
      const day = parseInt(timestampHex.substr(4, 2)[1] + timestampHex.substr(4, 2)[0], 10);
      const hour = parseInt(timestampHex.substr(6, 2)[1] + timestampHex.substr(6, 2)[0], 10);
      const minute = parseInt(timestampHex.substr(8, 2)[1] + timestampHex.substr(8, 2)[0], 10);
      const second = parseInt(timestampHex.substr(10, 2)[1] + timestampHex.substr(10, 2)[0], 10);
      
      return `${year}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')} ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:${second.toString().padStart(2, '0')}`;
    } catch {
      return new Date().toISOString();
    }
  }

  private static getEncoding(dcs: number): string {
    const codingGroup = (dcs >> 4) & 0x0F;
    if (codingGroup === 0) return 'GSM 7-bit';
    if (codingGroup === 1) return 'GSM 8-bit';
    if (codingGroup === 2) return 'UCS2';
    return 'Unknown';
  }

  private static decodeMessage(userData: string, encoding: string, udl: number): string {
    try {
      if (encoding === 'GSM 7-bit') {
        return this.decode7BitMessage(userData, udl);
      } else if (encoding === 'UCS2') {
        return this.decodeUCS2Message(userData);
      }
      return this.decodeHexMessage(userData);
    } catch {
      return 'Unable to decode message';
    }
  }

  private static decode7BitMessage(userData: string, udl: number): string {
    // Simplified GSM 7-bit decoding
    const bytes = [];
    for (let i = 0; i < userData.length; i += 2) {
      bytes.push(parseInt(userData.substr(i, 2), 16));
    }
    
    let result = '';
    let carry = 0;
    let carryBits = 0;
    
    for (let i = 0; i < bytes.length && result.length < udl; i++) {
      const byte = bytes[i];
      const shift = i % 7;
      
      if (shift === 0) {
        result += String.fromCharCode(byte & 0x7F);
        carry = byte >> 7;
        carryBits = 1;
      } else {
        const char = ((byte << (7 - shift)) | carry) & 0x7F;
        result += String.fromCharCode(char);
        carry = byte >> (8 - shift);
        carryBits = shift;
        
        if (carryBits === 7) {
          result += String.fromCharCode(carry);
          carry = 0;
          carryBits = 0;
        }
      }
    }
    
    return result.substr(0, udl);
  }

  private static decodeUCS2Message(userData: string): string {
    let result = '';
    for (let i = 0; i < userData.length; i += 4) {
      const charCode = parseInt(userData.substr(i, 4), 16);
      result += String.fromCharCode(charCode);
    }
    return result;
  }

  private static decodeHexMessage(userData: string): string {
    let result = '';
    for (let i = 0; i < userData.length; i += 2) {
      const byte = parseInt(userData.substr(i, 2), 16);
      result += String.fromCharCode(byte);
    }
    return result;
  }
}

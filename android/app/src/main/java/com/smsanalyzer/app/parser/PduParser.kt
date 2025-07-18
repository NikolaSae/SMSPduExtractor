package com.smsanalyzer.app.parser

import android.util.Log
import com.smsanalyzer.app.model.PduBreakdown
import com.smsanalyzer.app.model.TechnicalDetail
import java.text.SimpleDateFormat
import java.util.*

class PduParser {

    companion object {
        private const val TAG = "PduParser"
    }

    data class PduParseResult(
        val pduType: String,
        val sender: String?,
        val message: String?,
        val timestamp: String,
        val encoding: String,
        val messageLength: Int,
        val smscLength: Int,
        val smscType: String,
        val senderLength: Int,
        val status: String,
        val errorMessage: String?,
        val technicalDetails: List<TechnicalDetail>,
        val rawPduBreakdown: PduBreakdown
    )

    fun parse(pduString: String): PduParseResult {
        return try {
            val pdu = pduString.replace("\\s".toRegex(), "").uppercase()

            if (pdu.length < 10) {
                throw IllegalArgumentException("PDU too short")
            }

            var offset = 0

            // Parse SMSC length
            val smscLength = pdu.substring(offset, offset + 2).toInt(16)
            offset += 2

            // Parse SMSC information
            val smscInfo = pdu.substring(offset, offset + smscLength * 2)
            offset += smscLength * 2

            // Parse PDU type
            val pduTypeByte = pdu.substring(offset, offset + 2).toInt(16)
            offset += 2

            val pduType = getPduType(pduTypeByte)

            // Parse sender address length
            val senderLength = pdu.substring(offset, offset + 2).toInt(16)
            offset += 2

            // Parse sender address type
            val senderType = pdu.substring(offset, offset + 2)
            offset += 2

            // Parse sender address
            val senderDigits = kotlin.math.ceil(senderLength / 2.0).toInt()
            val senderHex = pdu.substring(offset, offset + senderDigits * 2)
            val sender = parsePhoneNumber(senderHex, senderLength)
            offset += senderDigits * 2

            // Parse PID (Protocol Identifier)
            val pid = pdu.substring(offset, offset + 2)
            offset += 2

            // Parse DCS (Data Coding Scheme)
            val dcs = pdu.substring(offset, offset + 2)
            offset += 2
            val encoding = getEncoding(dcs.toInt(16))

            // Parse timestamp (7 bytes for SMS-DELIVER)
            var timestamp = ""
            if (pduType == "SMS-DELIVER") {
                val timestampHex = pdu.substring(offset, offset + 14)
                timestamp = parseTimestamp(timestampHex)
                offset += 14
            }

            // Parse UDL (User Data Length)
            val udl = pdu.substring(offset, offset + 2).toInt(16)
            offset += 2

            // Parse User Data
            val userData = pdu.substring(offset)
            val message = decodeMessage(userData, encoding, udl)

            val technicalDetails = listOf(
                TechnicalDetail("SMSC Length", smscLength.toString(), pdu.substring(0, 2), "Length of SMSC information"),
                TechnicalDetail("SMSC Type", getSmscType(smscInfo), if (smscInfo.length >= 2) smscInfo.substring(0, 2) else "", "Type of SMSC address"),
                TechnicalDetail("PDU Type", pduType, pdu.substring((smscLength + 1) * 2, (smscLength + 1) * 2 + 2), "Type of PDU message"),
                TechnicalDetail("Sender Length", senderLength.toString(), pdu.substring((smscLength + 2) * 2, (smscLength + 2) * 2 + 2), "Length of sender address"),
                TechnicalDetail("PID", pid.toInt(16).toString(), pid, "Protocol identifier"),
                TechnicalDetail("DCS", encoding, dcs, "Data coding scheme"),
                TechnicalDetail("UDL", udl.toString(), pdu.substring(offset - 2, offset), "User data length")
            )

            val rawPduBreakdown = PduBreakdown(
                smscInfo = pdu.substring(0, (smscLength + 1) * 2),
                pduType = pdu.substring((smscLength + 1) * 2, (smscLength + 1) * 2 + 2),
                sender = pdu.substring((smscLength + 2) * 2, (smscLength + 2) * 2 + (senderDigits + 1) * 2),
                timestamp = if (timestamp.isNotEmpty()) pdu.substring(offset - 16, offset - 2) else "",
                message = userData
            )

            PduParseResult(
                pduType = pduType,
                sender = sender,
                message = message,
                timestamp = timestamp.ifEmpty { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()) },
                encoding = encoding,
                messageLength = message?.length ?: 0,
                smscLength = smscLength,
                smscType = getSmscType(smscInfo),
                senderLength = senderLength,
                status = "Valid",
                errorMessage = null,
                technicalDetails = technicalDetails,
                rawPduBreakdown = rawPduBreakdown
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error parsing PDU", e)
            PduParseResult(
                pduType = "Unknown",
                sender = null,
                message = null,
                timestamp = "",
                encoding = "Unknown",
                messageLength = 0,
                smscLength = 0,
                smscType = "Unknown",
                senderLength = 0,
                status = "Invalid",
                errorMessage = e.message,
                technicalDetails = emptyList(),
                rawPduBreakdown = PduBreakdown("", "", "", "", "")
            )
        }
    }

    private fun getPduType(pduTypeByte: Int): String {
        val mti = pduTypeByte and 0x03
        return when (mti) {
            0 -> "SMS-DELIVER"
            1 -> "SMS-SUBMIT"
            2 -> "SMS-STATUS-REPORT"
            3 -> "Reserved"
            else -> "Unknown"
        }
    }

    private fun getSmscType(smscInfo: String): String {
        if (smscInfo.length < 2) return "Unknown"
        val type = smscInfo.substring(0, 2).toInt(16)
        return when (type) {
            0x91 -> "International"
            0x81 -> "National"
            else -> "Unknown"
        }
    }

    private fun parsePhoneNumber(hex: String, length: Int): String {
        var number = ""
        var i = 0
        while (i < hex.length) {
            val byte = hex.substring(i, i + 2)
            if (byte == "F0" || byte == "0F") break
            number += byte[1].toString() + byte[0].toString()
            i += 2
        }
        return number.replace("F", "").take(length)
    }

    private fun parseTimestamp(timestampHex: String): String {
        return try {
            val year = timestampHex.substring(0, 2).let { "${it[1]}${it[0]}" }.toInt() + 2000
            val month = timestampHex.substring(2, 4).let { "${it[1]}${it[0]}" }.toInt()
            val day = timestampHex.substring(4, 6).let { "${it[1]}${it[0]}" }.toInt()
            val hour = timestampHex.substring(6, 8).let { "${it[1]}${it[0]}" }.toInt()
            val minute = timestampHex.substring(8, 10).let { "${it[1]}${it[0]}" }.toInt()
            val second = timestampHex.substring(10, 12).let { "${it[1]}${it[0]}" }.toInt()

            String.format("%04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second)
        } catch (e: Exception) {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        }
    }

    private fun getEncoding(dcs: Int): String {
        val codingGroup = (dcs shr 4) and 0x0F
        return when (codingGroup) {
            0 -> "GSM 7-bit"
            1 -> "GSM 8-bit"
            2 -> "UCS2"
            else -> "Unknown"
        }
    }

    private fun decodeMessage(userData: String, encoding: String, udl: Int): String? {
        return try {
            Log.d("PduParser", "Decoding message: encoding=$encoding, udl=$udl, userData=$userData")
            val result = when (encoding) {
                "GSM 7-bit" -> decode7BitMessage(userData, udl)
                "UCS2" -> decodeUCS2Message(userData)
                else -> decodeHexMessage(userData)
            }
            Log.d("PduParser", "Decoded result: $result")
            result
        } catch (e: Exception) {
            Log.e("PduParser", "Error decoding message", e)
            "Unable to decode message"
        }
    }

    private fun decode7BitMessage(userData: String, udl: Int): String {
        // Convert hex string to bytes
        val bytes = mutableListOf<Int>()
        var i = 0
        while (i < userData.length) {
            bytes.add(userData.substring(i, i + 2).toInt(16))
            i += 2
        }

        val result = StringBuilder()
        var carry = 0
        var carryBits = 0

        for (byteIndex in bytes.indices) {
            if (result.length >= udl) break

            val currentByte = bytes[byteIndex]
            val shift = byteIndex % 7

            when (shift) {
                0 -> {
                    // First byte, extract 7 bits
                    val char = currentByte and 0x7F
                    if (char != 0) result.append(getGsm7BitChar(char))
                    carry = currentByte shr 7
                    carryBits = 1
                }
                else -> {
                    // Extract character from current byte + carry
                    val char = ((currentByte shl (7 - shift)) or carry) and 0x7F
                    if (char != 0 && result.length < udl) {
                        result.append(getGsm7BitChar(char))
                    }

                    // Update carry for next iteration
                    carry = currentByte shr (8 - shift)
                    carryBits = shift

                    // If we have 7 carry bits, that's a complete character
                    if (carryBits == 7 && result.length < udl) {
                        if (carry != 0) result.append(getGsm7BitChar(carry))
                        carry = 0
                        carryBits = 0
                    }
                }
            }
        }

        // Add any remaining carry bits as final character
        if (carryBits > 0 && carry != 0 && result.length < udl) {
            result.append(getGsm7BitChar(carry))
        }

        return result.toString().take(udl)
    }

    private fun decodeUCS2Message(userData: String): String {
        var result = ""
        var i = 0
        while (i < userData.length) {
            val charCode = userData.substring(i, i + 4).toInt(16)
            result += charCode.toChar()
            i += 4
        }
        return result
    }

    private fun decodeHexMessage(userData: String): String {
        var result = ""
        var i = 0
        while (i < userData.length) {
            val byte = userData.substring(i, i + 2).toInt(16)
            result += byte.toChar()
            i += 2
        }
        return result
    }

    private fun getGsm7BitChar(code: Int): Char {
        return when (code) {
            0 -> '@'
            1 -> '£'
            2 -> '$'
            3 -> '¥'
            4 -> 'è'
            5 -> 'é'
            6 -> 'ù'
            7 -> 'ì'
            8 -> 'ò'
            9 -> 'Ç'
            10 -> '\n'
            11 -> 'Ø'
            12 -> 'ø'
            13 -> '\r'
            14 -> 'Å'
            15 -> 'å'
            16 -> 'Δ'
            17 -> '_'
            18 -> 'Φ'
            19 -> 'Γ'
            20 -> 'Λ'
            21 -> 'Ω'
            22 -> 'Π'
            23 -> 'Ψ'
            24 -> 'Σ'
            25 -> 'Θ'
            26 -> 'Ξ'
            27 -> '\u001B' // Escape character
            28 -> 'Æ'
            29 -> 'æ'
            30 -> 'ß'
            31 -> 'É'
            32 -> ' '
            33 -> '!'
            34 -> '"'
            35 -> '#'
            36 -> '¤'
            37 -> '%'
            38 -> '&'
            39 -> '\''
            40 -> '('
            41 -> ')'
            42 -> '*'
            43 -> '+'
            44 -> ','
            45 -> '-'
            46 -> '.'
            47 -> '/'
            48 -> '0'
            49 -> '1'
            50 -> '2'
            51 -> '3'
            52 -> '4'
            53 -> '5'
            54 -> '6'
            55 -> '7'
            56 -> '8'
            57 -> '9'
            58 -> ':'
            59 -> ';'
            60 -> '<'
            61 -> '='
            62 -> '>'
            63 -> '?'
            64 -> '¡'
            65 -> 'A'
            66 -> 'B'
            67 -> 'C'
            68 -> 'D'
            69 -> 'E'
            70 -> 'F'
            71 -> 'G'
            72 -> 'H'
            73 -> 'I'
            74 -> 'J'
            75 -> 'K'
            76 -> 'L'
            77 -> 'M'
            78 -> 'N'
            79 -> 'O'
            80 -> 'P'
            81 -> 'Q'
            82 -> 'R'
            83 -> 'S'
            84 -> 'T'
            85 -> 'U'
            86 -> 'V'
            87 -> 'W'
            88 -> 'X'
            89 -> 'Y'
            90 -> 'Z'
            91 -> 'Ä'
            92 -> 'Ö'
            93 -> 'Ñ'
            94 -> 'Ü'
            95 -> '§'
            96 -> '¿'
            97 -> 'a'
            98 -> 'b'
            99 -> 'c'
            100 -> 'd'
            101 -> 'e'
            102 -> 'f'
            103 -> 'g'
            104 -> 'h'
            105 -> 'i'
            106 -> 'j'
            107 -> 'k'
            108 -> 'l'
            109 -> 'm'
            110 -> 'n'
            111 -> 'o'
            112 -> 'p'
            113 -> 'q'
            114 -> 'r'
            115 -> 's'
            116 -> 't'
            117 -> 'u'
            118 -> 'v'
            119 -> 'w'
            120 -> 'x'
            121 -> 'y'
            122 -> 'z'
            123 -> 'ä'
            124 -> 'ö'
            125 -> 'ñ'
            126 -> 'ü'
            127 -> 'à'
            else -> ' '
        }
    }
}

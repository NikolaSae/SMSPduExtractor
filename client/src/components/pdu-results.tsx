import { CheckCircle, Info, MessageSquare, Settings, Code } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";

interface PduResultsProps {
  result: {
    id: number;
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
    technicalDetails: Array<{
      field: string;
      value: string;
      hex: string;
      description: string;
    }>;
    rawPduBreakdown: {
      smscInfo: string;
      pduType: string;
      sender: string;
      timestamp: string;
      message: string;
    };
  };
}

export default function PduResults({ result }: PduResultsProps) {
  const getStatusColor = (status: string) => {
    return status === "Valid" ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800";
  };

  const getEncodingColor = (encoding: string) => {
    if (encoding === "GSM 7-bit") return "bg-blue-100 text-blue-800";
    if (encoding === "UCS2") return "bg-green-100 text-green-800";
    return "bg-gray-100 text-gray-800";
  };

  const highlightRawPdu = (breakdown: any) => {
    const colors = [
      { bg: "bg-red-100", text: "text-red-800" },
      { bg: "bg-blue-100", text: "text-blue-800" },
      { bg: "bg-green-100", text: "text-green-800" },
      { bg: "bg-yellow-100", text: "text-yellow-800" },
      { bg: "bg-purple-100", text: "text-purple-800" },
      { bg: "bg-gray-100", text: "text-gray-800" }
    ];

    const segments = [
      { data: breakdown.smscInfo, label: "SMSC Info", color: colors[0] },
      { data: breakdown.pduType, label: "PDU Type", color: colors[1] },
      { data: breakdown.sender, label: "Sender", color: colors[2] },
      { data: breakdown.timestamp, label: "Timestamp", color: colors[3] },
      { data: breakdown.message, label: "Message", color: colors[4] }
    ];

    return segments.filter(segment => segment.data);
  };

  const rawPduSegments = highlightRawPdu(result.rawPduBreakdown);

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <CheckCircle className="h-5 w-5 text-green-600" />
          Analysis Results
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-6">
        {/* PDU Overview */}
        <div className="bg-gray-50 rounded-lg p-4">
          <h3 className="font-medium text-gray-900 mb-3 flex items-center gap-2">
            <Info className="h-4 w-4" />
            PDU Overview
          </h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
            <div className="flex justify-between">
              <span className="text-gray-600">Type:</span>
              <span className="font-medium">{result.pduType}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Length:</span>
              <span className="font-medium">{result.smscLength} bytes</span>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Encoding:</span>
              <Badge className={getEncodingColor(result.encoding)}>
                {result.encoding}
              </Badge>
            </div>
            <div className="flex justify-between">
              <span className="text-gray-600">Status:</span>
              <Badge className={getStatusColor(result.status)}>
                {result.status}
              </Badge>
            </div>
          </div>
        </div>

        {/* Message Information */}
        <div className="bg-blue-50 rounded-lg p-4">
          <h3 className="font-medium text-gray-900 mb-3 flex items-center gap-2">
            <MessageSquare className="h-4 w-4" />
            Message Information
          </h3>
          <div className="space-y-3">
            <div>
              <label className="text-gray-600 text-sm font-medium">Sender Number:</label>
              <div className="mt-1 p-2 bg-white rounded border font-mono text-sm">
                {result.sender || "N/A"}
              </div>
            </div>
            <div>
              <label className="text-gray-600 text-sm font-medium">Message Content:</label>
              <div className="mt-1 p-3 bg-white rounded border">
                {result.message || "Unable to decode message"}
              </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="text-gray-600 text-sm font-medium">Timestamp:</label>
                <div className="mt-1 p-2 bg-white rounded border font-mono text-sm">
                  {result.timestamp || "N/A"}
                </div>
              </div>
              <div>
                <label className="text-gray-600 text-sm font-medium">Message Length:</label>
                <div className="mt-1 p-2 bg-white rounded border font-mono text-sm">
                  {result.messageLength} characters
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Technical Details */}
        <div className="bg-gray-50 rounded-lg p-4">
          <h3 className="font-medium text-gray-900 mb-3 flex items-center gap-2">
            <Settings className="h-4 w-4" />
            Technical Details
          </h3>
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Field</TableHead>
                  <TableHead>Value</TableHead>
                  <TableHead>Hex</TableHead>
                  <TableHead>Description</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {result.technicalDetails.map((detail, index) => (
                  <TableRow key={index}>
                    <TableCell className="font-mono">{detail.field}</TableCell>
                    <TableCell>{detail.value}</TableCell>
                    <TableCell className="font-mono text-primary">{detail.hex}</TableCell>
                    <TableCell className="text-gray-600">{detail.description}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        </div>

        {/* Raw Data Breakdown */}
        <div className="bg-yellow-50 rounded-lg p-4">
          <h3 className="font-medium text-gray-900 mb-3 flex items-center gap-2">
            <Code className="h-4 w-4" />
            Raw Data Breakdown
          </h3>
          <div className="space-y-3">
            <div className="font-mono text-sm bg-white p-3 rounded border break-all">
              {rawPduSegments.map((segment, index) => (
                <span
                  key={index}
                  className={`${segment.color.bg} ${segment.color.text} px-1 rounded`}
                >
                  {segment.data}
                </span>
              ))}
            </div>
            <div className="flex flex-wrap gap-4 text-xs text-gray-600">
              {rawPduSegments.map((segment, index) => (
                <span key={index} className="flex items-center gap-1">
                  <span className={`w-3 h-3 ${segment.color.bg} border border-gray-300 rounded`}></span>
                  {segment.label}
                </span>
              ))}
            </div>
          </div>
        </div>

        {/* Error Message */}
        {result.errorMessage && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4">
            <h3 className="font-medium text-red-900 mb-2">Error Details</h3>
            <p className="text-red-700 text-sm">{result.errorMessage}</p>
          </div>
        )}
      </CardContent>
    </Card>
  );
}

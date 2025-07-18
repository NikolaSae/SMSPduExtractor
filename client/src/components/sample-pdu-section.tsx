import { useQuery } from "@tanstack/react-query";
import { Variable, Files } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { useToast } from "@/hooks/use-toast";

interface SamplePdu {
  id: string;
  name: string;
  pdu: string;
  description: string;
  encoding: string;
  type: string;
}

interface SamplePduSectionProps {
  onLoadSample?: (pdu: string) => void;
}

export default function SamplePduSection({ onLoadSample }: SamplePduSectionProps) {
  const { toast } = useToast();

  const { data: samples, isLoading } = useQuery({
    queryKey: ["/api/pdu/samples"],
    queryFn: async () => {
      const response = await fetch("/api/pdu/samples");
      return response.json();
    },
  });

  const handleLoadSample = (pdu: string) => {
    // Copy to clipboard
    navigator.clipboard.writeText(pdu).then(() => {
      toast({
        title: "Sample Loaded",
        description: "PDU copied to clipboard and ready for analysis",
      });
    }).catch(() => {
      toast({
        title: "Sample Ready",
        description: "PDU is ready for analysis",
      });
    });

    // Trigger input update if callback provided
    if (onLoadSample) {
      onLoadSample(pdu);
    }

    // Scroll to top to show the input field
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const getEncodingColor = (encoding: string) => {
    if (encoding === "GSM 7-bit") return "bg-blue-100 text-blue-800";
    if (encoding === "UCS2") return "bg-green-100 text-green-800";
    return "bg-gray-100 text-gray-800";
  };

  const getTypeColor = (type: string) => {
    if (type === "SMS-DELIVER") return "bg-blue-100 text-blue-800";
    if (type === "SMS-SUBMIT") return "bg-green-100 text-green-800";
    return "bg-gray-100 text-gray-800";
  };

  if (isLoading) {
    return (
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Variable className="h-5 w-5 text-orange-600" />
            Sample PDU Data
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            {[1, 2, 3].map((i) => (
              <div key={i} className="border border-gray-200 rounded-lg p-4 animate-pulse">
                <div className="flex items-start justify-between mb-2">
                  <div className="flex items-center gap-2">
                    <div className="h-4 w-32 bg-gray-200 rounded"></div>
                    <div className="h-6 w-16 bg-gray-200 rounded"></div>
                  </div>
                  <div className="h-8 w-20 bg-gray-200 rounded"></div>
                </div>
                <div className="h-4 bg-gray-200 rounded mb-2"></div>
                <div className="h-3 w-48 bg-gray-200 rounded"></div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Variable className="h-5 w-5 text-orange-600" />
          Sample PDU Data
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-4">
        <p className="text-gray-600 text-sm">
          Try these sample PDU strings to see how the analyzer works:
        </p>
        
        <div className="space-y-4">
          {samples && Array.isArray(samples) ? samples.map((sample: SamplePdu) => (
            <div
              key={sample.id}
              className="border border-gray-200 rounded-lg p-4 hover:bg-gray-50 transition-colors"
            >
              <div className="flex items-start justify-between mb-2">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium text-gray-900">{sample.name}</span>
                  <Badge className={getEncodingColor(sample.encoding)}>
                    {sample.encoding}
                  </Badge>
                  <Badge className={getTypeColor(sample.type)}>
                    {sample.type}
                  </Badge>
                </div>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => handleLoadSample(sample.pdu)}
                  className="text-primary hover:text-primary/80 flex items-center gap-1"
                >
                  <Files className="h-4 w-4" />
                  Use Sample
                </Button>
              </div>
              <div className="font-mono text-xs bg-gray-100 p-2 rounded break-all text-gray-700 mb-2">
                {sample.pdu}
              </div>
              <p className="text-xs text-gray-500">Message: "{sample.description}"</p>
            </div>
          )) : (
            <div className="text-center py-8 text-gray-500">
              <p>No sample PDU data available</p>
            </div>
          )}
        </div>
      </CardContent>
    </Card>
  );
}

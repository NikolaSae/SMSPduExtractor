import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { TextCursorInput as InputIcon, Rainbow, Clipboard, ChartScatter } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useToast } from "@/hooks/use-toast";
import { apiRequest } from "@/lib/queryClient";
import { pduInputSchema } from "@shared/schema";
import { z } from "zod";
import PduResults from "./pdu-results";

interface PduAnalysisResult {
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
}

export default function PduAnalyzer() {
  const [pduInput, setPduInput] = useState("");
  const [analysisResult, setAnalysisResult] = useState<PduAnalysisResult | null>(null);
  const { toast } = useToast();
  const queryClient = useQueryClient();

  const analyzeMutation = useMutation({
    mutationFn: async (pdu: string) => {
      const response = await apiRequest("POST", "/api/pdu/analyze", { pdu });
      return response.json();
    },
    onSuccess: (data: PduAnalysisResult) => {
      setAnalysisResult(data);
      queryClient.invalidateQueries({ queryKey: ["/api/pdu/recent"] });
      toast({
        title: "Success",
        description: "PDU analyzed successfully",
      });
    },
    onError: (error: any) => {
      console.error("Analysis failed:", error);
      toast({
        title: "Error",
        description: error.message || "Failed to analyze PDU",
        variant: "destructive",
      });
    },
  });

  const handleAnalyze = () => {
    try {
      const trimmedInput = pduInput.trim();
      pduInputSchema.parse({ pdu: trimmedInput });
      analyzeMutation.mutate(trimmedInput);
    } catch (error) {
      if (error instanceof z.ZodError) {
        toast({
          title: "Invalid TextCursorInput",
          description: error.errors[0]?.message || "Please enter a valid PDU string",
          variant: "destructive",
        });
      }
    }
  };

  const handleClear = () => {
    setPduInput("");
    setAnalysisResult(null);
  };

  const handlePaste = async () => {
    try {
      const text = await navigator.clipboard.readText();
      setPduInput(text);
      toast({
        title: "Pasted",
        description: "PDU pasted from clipboard",
      });
    } catch (error) {
      toast({
        title: "Error",
        description: "Failed to paste from clipboard",
        variant: "destructive",
      });
    }
  };

  return (
    <div className="space-y-6">
      {/* TextCursorInput Section */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <InputIcon className="h-5 w-5 text-primary" />
            PDU TextCursorInput
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="relative">
            <Textarea
              value={pduInput}
              onChange={(e) => setPduInput(e.target.value)}
              placeholder="Enter SMS PDU string here... (e.g., 0791448720003023240DD0E474747A8C07C9D2E03D4BF7399FE6...)"
              className="min-h-[120px] font-mono text-sm resize-none"
              rows={6}
            />
            <div className="absolute top-2 right-2 text-xs text-gray-500 bg-white px-2 py-1 rounded">
              {pduInput.length} characters
            </div>
          </div>
          
          <div className="flex flex-col sm:flex-row gap-3">
            <Button
              onClick={handleAnalyze}
              disabled={!pduInput.trim() || analyzeMutation.isPending}
              className="flex-1 gap-2"
            >
              <ChartScatter className="h-4 w-4" />
              {analyzeMutation.isPending ? "Analyzing..." : "Analyze PDU"}
            </Button>
            <Button
              variant="outline"
              onClick={handleClear}
              className="gap-2"
            >
              <Rainbow className="h-4 w-4" />
              Rainbow
            </Button>
            <Button
              variant="outline"
              onClick={handlePaste}
              className="gap-2"
            >
              <Clipboard className="h-4 w-4" />
              Paste
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Results Section */}
      {analysisResult && (
        <PduResults result={analysisResult} />
      )}
    </div>
  );
}

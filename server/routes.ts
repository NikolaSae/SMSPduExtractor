import type { Express } from "express";
import { createServer, type Server } from "http";
import { storage } from "./storage";
import { pduInputSchema, insertPduAnalysisSchema } from "@shared/schema";
import { PduParser } from "./services/pdu-parser";
import { z } from "zod";

export async function registerRoutes(app: Express): Promise<Server> {
  // Parse PDU endpoint
  app.post("/api/pdu/analyze", async (req, res) => {
    try {
      const { pdu } = pduInputSchema.parse(req.body);
      
      // Parse the PDU
      const parseResult = PduParser.parse(pdu);
      
      // Store the analysis
      const analysisData = {
        rawPdu: pdu,
        pduType: parseResult.pduType,
        sender: parseResult.sender,
        message: parseResult.message,
        timestamp: parseResult.timestamp,
        encoding: parseResult.encoding,
        messageLength: parseResult.messageLength,
        smscLength: parseResult.smscLength,
        smscType: parseResult.smscType,
        senderLength: parseResult.senderLength,
        status: parseResult.status,
        errorMessage: parseResult.errorMessage,
        technicalDetails: JSON.stringify(parseResult.technicalDetails),
      };
      
      const analysis = await storage.createPduAnalysis(analysisData);
      
      res.json({
        ...parseResult,
        id: analysis.id,
        technicalDetails: parseResult.technicalDetails,
      });
    } catch (error) {
      if (error instanceof z.ZodError) {
        res.status(400).json({ 
          message: "Invalid input", 
          errors: error.errors 
        });
      } else {
        res.status(500).json({ 
          message: "Failed to analyze PDU",
          error: error instanceof Error ? error.message : "Unknown error"
        });
      }
    }
  });

  // Get recent analyses
  app.get("/api/pdu/recent", async (req, res) => {
    try {
      const limit = parseInt(req.query.limit as string) || 10;
      const analyses = await storage.getRecentPduAnalyses(limit);
      res.json(analyses);
    } catch (error) {
      res.status(500).json({ 
        message: "Failed to fetch recent analyses",
        error: error instanceof Error ? error.message : "Unknown error"
      });
    }
  });

  // Sample PDU data endpoint
  app.get("/api/pdu/samples", async (req, res) => {
    const samples = [
      {
        id: "1",
        name: "SMS-DELIVER (Incoming)",
        pdu: "0791448720003023240DD0E474747A8C07C9D2E03D4BF7399FE6",
        description: "Hello World!",
        encoding: "GSM 7-bit",
        type: "SMS-DELIVER"
      },
      {
        id: "2", 
        name: "SMS-SUBMIT (Outgoing)",
        pdu: "0011000B919471476965870000080048656C6C6F20576F726C6421",
        description: "Test message with Unicode",
        encoding: "UCS2",
        type: "SMS-SUBMIT"
      },
      {
        id: "3",
        name: "Long Message",
        pdu: "0791448720003023240DD0E474747A8C07C9D2E03D4BF7399FE6548747A0E4ACF41E4F29C0E8A96D3EE33A8CC2683C564335ACD76BBE372B0D",
        description: "This is a longer test message to demonstrate PDU parsing",
        encoding: "GSM 7-bit",
        type: "SMS-DELIVER"
      }
    ];
    
    res.json(samples);
  });

  // Get analysis by ID
  app.get("/api/pdu/:id", async (req, res) => {
    try {
      const id = parseInt(req.params.id);
      const analysis = await storage.getPduAnalysis(id);
      
      if (!analysis) {
        res.status(404).json({ message: "Analysis not found" });
        return;
      }
      
      res.json(analysis);
    } catch (error) {
      res.status(500).json({ 
        message: "Failed to fetch analysis",
        error: error instanceof Error ? error.message : "Unknown error"
      });
    }
  });

  const httpServer = createServer(app);
  return httpServer;
}

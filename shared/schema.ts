import { pgTable, text, serial, integer, boolean, timestamp } from "drizzle-orm/pg-core";
import { createInsertSchema } from "drizzle-zod";
import { z } from "zod";

export const pduAnalysis = pgTable("pdu_analysis", {
  id: serial("id").primaryKey(),
  rawPdu: text("raw_pdu").notNull(),
  pduType: text("pdu_type"),
  sender: text("sender"),
  message: text("message"),
  timestamp: text("timestamp"),
  encoding: text("encoding"),
  messageLength: integer("message_length"),
  smscLength: integer("smsc_length"),
  smscType: text("smsc_type"),
  senderLength: integer("sender_length"),
  status: text("status"),
  errorMessage: text("error_message"),
  technicalDetails: text("technical_details"), // JSON string
  createdAt: timestamp("created_at").defaultNow(),
});

export const insertPduAnalysisSchema = createInsertSchema(pduAnalysis).omit({
  id: true,
  createdAt: true,
});

export const pduInputSchema = z.object({
  pdu: z.string().min(1, "PDU string is required").regex(/^[0-9A-Fa-f]+$/, "PDU must contain only hexadecimal characters"),
});

export type InsertPduAnalysis = z.infer<typeof insertPduAnalysisSchema>;
export type PduAnalysis = typeof pduAnalysis.$inferSelect;
export type PduInput = z.infer<typeof pduInputSchema>;

// Sample PDU data structure
export const samplePduSchema = z.object({
  id: z.string(),
  name: z.string(),
  pdu: z.string(),
  description: z.string(),
  encoding: z.string(),
  type: z.string(),
});

export type SamplePdu = z.infer<typeof samplePduSchema>;

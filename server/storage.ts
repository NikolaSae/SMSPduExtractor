import { pduAnalysis, type PduAnalysis, type InsertPduAnalysis } from "@shared/schema";

export interface IStorage {
  createPduAnalysis(analysis: InsertPduAnalysis): Promise<PduAnalysis>;
  getPduAnalysis(id: number): Promise<PduAnalysis | undefined>;
  getRecentPduAnalyses(limit?: number): Promise<PduAnalysis[]>;
}

export class MemStorage implements IStorage {
  private analyses: Map<number, PduAnalysis>;
  private currentId: number;

  constructor() {
    this.analyses = new Map();
    this.currentId = 1;
  }

  async createPduAnalysis(insertAnalysis: InsertPduAnalysis): Promise<PduAnalysis> {
    const id = this.currentId++;
    const analysis: PduAnalysis = {
      id,
      rawPdu: insertAnalysis.rawPdu,
      pduType: insertAnalysis.pduType ?? null,
      sender: insertAnalysis.sender ?? null,
      message: insertAnalysis.message ?? null,
      timestamp: insertAnalysis.timestamp ?? null,
      encoding: insertAnalysis.encoding ?? null,
      messageLength: insertAnalysis.messageLength ?? null,
      smscLength: insertAnalysis.smscLength ?? null,
      smscType: insertAnalysis.smscType ?? null,
      senderLength: insertAnalysis.senderLength ?? null,
      status: insertAnalysis.status ?? null,
      errorMessage: insertAnalysis.errorMessage ?? null,
      technicalDetails: insertAnalysis.technicalDetails ?? null,
      createdAt: new Date(),
    };
    this.analyses.set(id, analysis);
    return analysis;
  }

  async getPduAnalysis(id: number): Promise<PduAnalysis | undefined> {
    return this.analyses.get(id);
  }

  async getRecentPduAnalyses(limit: number = 10): Promise<PduAnalysis[]> {
    const analyses = Array.from(this.analyses.values())
      .sort((a, b) => (b.createdAt?.getTime() || 0) - (a.createdAt?.getTime() || 0))
      .slice(0, limit);
    return analyses;
  }
}

export const storage = new MemStorage();

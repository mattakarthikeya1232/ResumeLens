export type SkillMatch = { skill: string; category: string; evidence: string[]; relevance: number; sourceSection: string }
export type EvidenceItem = { classification: 'TECHNICAL' | 'NON_TECHNICAL'; skill: string; category: string; sourceSection: string; text: string; relevance: number }
export type ResumeSection = { name: string; preview: string; characterCount: number }
export type Diagnostics = {
  embeddingModel: string; embeddingStatus: string; llmModel: string; llmStatus: string
  inferenceTimeMs: number; documentProcessingTimeMs: number; analysisDurationMs: number
  usedHeapMb: number; maxHeapMb: number; peakMemoryMb: number; collectedAt: string
}
export type LocalLlmSettings = { adapterAvailable: boolean; enabled: boolean }
export type Explanation = { summary: string; technicalProfile: string; nonTechnicalProfile: string; strengths: string[]; improvementAreas: string[]; candidateProfile: string; generatedBy: string }
export type Analysis = {
  analysisId: string; resumeName: string; analyzedAt: string; status: string; technicalScore: number; nonTechnicalScore: number
  technicalSkills: SkillMatch[]; nonTechnicalSkills: SkillMatch[]; sections: ResumeSection[]; evidence: EvidenceItem[]; explanation: Explanation; diagnostics: Diagnostics; processingMode: string
}
export type AnalysisSummary = Pick<Analysis, 'analysisId' | 'resumeName' | 'analyzedAt' | 'technicalScore' | 'nonTechnicalScore' | 'status'>
export type ApiError = { message?: string }

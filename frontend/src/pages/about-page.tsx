import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { ArrowRight, FileText, GitBranch, ShieldCheck, Zap } from 'lucide-react'
import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'

const stages = ['PDF / DOCX', 'Java parser', 'Text cleanup', 'Section extraction', 'Evidence rules', 'Classifier', 'Grounded report', 'Dashboard']

export function AboutPage() {
  return (
    <div className="space-y-8">
      <section className="max-w-3xl">
        <Badge variant="secondary">Architecture</Badge>
        <h1 className="mt-4 text-3xl font-semibold tracking-tight sm:text-4xl">Built for explainability, not an opaque black box.</h1>
        <p className="mt-4 text-lg leading-8 text-muted-foreground">ResumeLens keeps the core workflow in Java and works without expensive cloud APIs. It starts with deterministic document parsing and evidence extraction, then adds optional local models through clean interfaces.</p>
      </section>
      <Card>
        <CardHeader><CardTitle>Analysis pipeline</CardTitle><CardDescription>Every stage has a graceful fallback so a missing model never blocks core resume analysis.</CardDescription></CardHeader>
        <CardContent><div className="flex flex-wrap items-center gap-2">{stages.map((stage, index) => <div key={stage} className="flex items-center gap-2"><div className="rounded-lg border bg-muted/25 px-3 py-2 text-sm font-medium">{stage}</div>{index < stages.length - 1 && <ArrowRight className="size-4 text-muted-foreground" />}</div>)}</div></CardContent>
      </Card>
      <div className="grid gap-6 md:grid-cols-3">
        <Principle icon={<ShieldCheck />} title="Evidence-first" text="A skill appears only when a matching resume excerpt is retained with its source section and relevance." />
        <Principle icon={<Zap />} title="Resource-conscious" text="The default path uses Java parsing and contextual rules. Optional ONNX embeddings and a quantized local LLM can be enabled independently." />
        <Principle icon={<GitBranch />} title="Modular by design" text="Parser, classifier, explanation, and diagnostics services are separate so models can evolve without rewriting the product." />
      </div>
      <Card>
        <CardHeader><CardTitle>Why this architecture?</CardTitle></CardHeader>
        <CardContent className="grid gap-5 text-sm leading-6 text-muted-foreground md:grid-cols-2">
          <p><span className="font-medium text-foreground">Local-first:</span> core analysis does not transmit a resume to an external LLM. The UI clearly identifies when a local model is unavailable and a deterministic fallback is active.</p>
          <p><span className="font-medium text-foreground">Defensible scores:</span> classification scores represent each category’s share of observed evidence relevance. They are not a hiring recommendation or scientifically precise probability.</p>
          <p><span className="font-medium text-foreground">Measured diagnostics:</span> document time, classification time, analysis duration, and JVM memory are captured from the running process.</p>
          <p><span className="font-medium text-foreground">Safe document handling:</span> accepted files are size- and extension-validated, parsed in memory, never executed, and not persisted by the default API.</p>
        </CardContent>
      </Card>
      <div className="rounded-2xl border bg-muted/30 p-6 sm:flex sm:items-center sm:justify-between">
        <div><p className="font-medium">Ready to inspect a resume?</p><p className="mt-1 text-sm text-muted-foreground">Upload a PDF or DOCX to generate a traceable report.</p></div>
        <Button nativeButton={false} render={<Link to="/" />} className="mt-4 sm:mt-0"><FileText className="size-4" />Analyze resume</Button>
      </div>
    </div>
  )
}

function Principle({ icon, title, text }: { icon: ReactNode; title: string; text: string }) {
  return <Card><CardHeader><span className="text-muted-foreground">{icon}</span><CardTitle className="text-base">{title}</CardTitle><CardDescription className="leading-6">{text}</CardDescription></CardHeader></Card>
}

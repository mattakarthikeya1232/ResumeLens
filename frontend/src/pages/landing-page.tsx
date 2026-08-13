import { Brand } from '@/components/brand'
import { Alert, AlertDescription, AlertTitle } from '@/components/ui/alert'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Progress } from '@/components/ui/progress'
import { Separator } from '@/components/ui/separator'
import { api } from '@/services/api'
import { AlertCircle, BrainCircuit, CheckCircle2, FileText, ShieldCheck, Sparkles, UploadCloud, X } from 'lucide-react'
import { type ReactNode, useRef, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

const MAX_BYTES = 8 * 1024 * 1024
type UploadState = 'idle' | 'uploading' | 'analyzing'

function validFile(file: File) { return ['application/pdf', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', ''].includes(file.type) && /\.(pdf|docx)$/i.test(file.name) }

export function LandingPage() {
  const [file, setFile] = useState<File | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [state, setState] = useState<UploadState>('idle')
  const [progress, setProgress] = useState(0)
  const inputRef = useRef<HTMLInputElement>(null)
  const controllerRef = useRef<AbortController | null>(null)
  const navigate = useNavigate()
  const busy = state !== 'idle'

  const choose = (candidate?: File) => {
    setError(null)
    if (!candidate) return
    if (!validFile(candidate)) { setFile(null); setError('Choose a PDF or DOCX resume.'); return }
    if (candidate.size > MAX_BYTES) { setFile(null); setError('This resume exceeds the 8 MB upload limit.'); return }
    setFile(candidate)
  }
  const analyze = async () => {
    if (!file) { setError('Choose a resume before starting the analysis.'); return }
    controllerRef.current = new AbortController()
    setState('uploading'); setProgress(0); setError(null)
    try {
      const result = await api.analyze(file, setProgress, controllerRef.current.signal)
      setState('analyzing')
      localStorage.setItem('resumelens-active-analysis', result.analysisId)
      navigate('/dashboard')
    } catch (reason) {
      if ((reason as DOMException).name !== 'AbortError') setError(reason instanceof Error ? reason.message : 'The resume could not be analyzed.')
      setState('idle'); setProgress(0)
    }
  }
  const cancel = () => { controllerRef.current?.abort(); setState('idle'); setProgress(0) }

  return <div className="min-h-screen bg-[radial-gradient(circle_at_50%_-20%,color-mix(in_oklab,var(--primary)_10%,transparent),transparent_45%)]">
    <header className="mx-auto flex max-w-6xl items-center justify-between px-5 py-5 sm:px-8"><Brand /><div className="flex gap-2"><Button variant="ghost" nativeButton={false} render={<Link to="/about" />}>Architecture</Button><Button nativeButton={false} render={<a href="#upload" />}>Analyze resume</Button></div></header>
    <main className="mx-auto max-w-6xl px-5 pb-12 pt-10 sm:px-8 sm:pt-18">
      <section className="mx-auto max-w-3xl text-center"><Badge variant="secondary" className="mb-5 gap-1.5"><Sparkles className="size-3" />Evidence-first classification</Badge><h1 className="text-balance text-4xl font-semibold tracking-tight sm:text-6xl">Understand the signals in every resume.</h1><p className="mx-auto mt-5 max-w-2xl text-pretty text-base leading-7 text-muted-foreground sm:text-lg">ResumeLens separates technical and professional experience with transparent evidence, lightweight local processing, and clear next steps.</p></section>
      <section id="upload" className="mx-auto mt-10 max-w-3xl scroll-mt-8"><Card className="shadow-xl shadow-foreground/5"><CardContent className="p-5 sm:p-8">
        {!file ? <button type="button" className="group flex min-h-72 w-full flex-col items-center justify-center rounded-xl border border-dashed border-muted-foreground/35 bg-muted/25 px-6 text-center transition-colors hover:border-foreground/50 hover:bg-muted/40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring" onClick={() => inputRef.current?.click()} onDragOver={event => event.preventDefault()} onDrop={event => { event.preventDefault(); choose(event.dataTransfer.files?.[0]) }} aria-label="Drop a PDF or DOCX resume here, or open a file picker">
          <span className="grid size-14 place-items-center rounded-2xl bg-background shadow-sm ring-1 ring-border"><UploadCloud className="size-6" /></span><span className="mt-5 text-base font-medium">Drop your resume here</span><span className="mt-1 text-sm text-muted-foreground">or select a file from your computer</span><span className="mt-5 inline-flex h-8 items-center rounded-2xl bg-secondary px-3 text-sm font-medium text-secondary-foreground">Choose resume</span><span className="mt-5 text-xs text-muted-foreground">PDF or DOCX · Maximum 8 MB</span>
        </button> : <div className="rounded-xl border bg-muted/20 p-5"><div className="flex items-start gap-4"><span className="grid size-11 shrink-0 place-items-center rounded-lg bg-background ring-1 ring-border"><FileText className="size-5" /></span><div className="min-w-0 flex-1"><p className="truncate font-medium">{file.name}</p><p className="mt-1 text-sm text-muted-foreground">{(file.size / 1024 / 1024).toFixed(2)} MB · Ready for local analysis</p></div>{!busy && <Button type="button" size="icon" variant="ghost" onClick={() => { setFile(null); setError(null) }} aria-label="Remove selected resume"><X className="size-4" /></Button>}</div>{busy && <div className="mt-5"><div className="mb-2 flex justify-between text-sm"><span>{state === 'uploading' ? 'Uploading securely…' : 'Extracting content and evaluating evidence…'}</span><span>{state === 'uploading' ? `${progress}%` : 'Working'}</span></div><Progress value={state === 'uploading' ? progress : 100} /></div>}</div>}
        <input ref={inputRef} className="sr-only" type="file" accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document" onChange={event => choose(event.target.files?.[0])} />
        {error && <Alert variant="destructive" className="mt-4"><AlertCircle className="size-4" /><AlertTitle>Unable to continue</AlertTitle><AlertDescription>{error}</AlertDescription></Alert>}
        <div className="mt-5 flex flex-col gap-3 sm:flex-row"><Button className="flex-1" size="lg" onClick={analyze} disabled={!file || busy}>{busy ? 'Analyzing resume…' : 'Analyze resume'}</Button>{busy && <Button size="lg" variant="outline" onClick={cancel}>Cancel</Button>}</div>
      </CardContent></Card></section>
      <section className="mt-12 grid gap-4 md:grid-cols-3"><Feature icon={<ShieldCheck />} title="Local by design" text="Your document is processed in the running application; no cloud LLM is required for core analysis." /><Feature icon={<BrainCircuit />} title="Explainable output" text="Each detected signal is linked to a source section and exact resume evidence." /><Feature icon={<CheckCircle2 />} title="Graceful fallbacks" text="Rule-based classification and deterministic explanations keep analysis available without models." /></section>
      <section className="mt-14 rounded-2xl border bg-card p-6 sm:p-8"><p className="text-xs font-medium uppercase tracking-[0.16em] text-muted-foreground">How it works</p><div className="mt-5 grid gap-3 text-sm sm:grid-cols-5"><Step number="01" text="Parse PDF / DOCX" /><Step number="02" text="Detect sections" /><Step number="03" text="Extract evidence" /><Step number="04" text="Classify signals" /><Step number="05" text="Generate report" /></div></section>
    </main>
  </div>
}

function Feature({ icon, title, text }: { icon: ReactNode; title: string; text: string }) { return <Card><CardHeader><span className="text-foreground">{icon}</span><CardTitle className="text-base">{title}</CardTitle><CardDescription className="leading-6">{text}</CardDescription></CardHeader></Card> }
function Step({ number, text }: { number: string; text: string }) { return <div className="flex items-center gap-3 rounded-lg bg-muted/45 p-3"><span className="text-xs font-semibold text-muted-foreground">{number}</span><Separator orientation="vertical" className="h-4" /><span>{text}</span></div> }

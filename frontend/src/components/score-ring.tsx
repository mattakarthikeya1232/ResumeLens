import { cn } from '@/lib/utils'

export function ScoreRing({ value, label, className }: { value: number; label: string; className?: string }) {
  return <div className={cn('relative grid size-36 place-items-center rounded-full', className)} style={{ background: `conic-gradient(currentColor ${value * 3.6}deg, color-mix(in oklab, currentColor 12%, transparent) 0deg)` }}>
    <div className="grid size-[calc(100%-12px)] place-items-center rounded-full bg-card text-center text-card-foreground">
      <span className="text-3xl font-semibold tracking-tight">{value}%</span><span className="max-w-20 text-[10px] font-medium uppercase tracking-[0.12em] text-muted-foreground">{label}</span>
    </div>
  </div>
}

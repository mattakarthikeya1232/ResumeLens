import { ScanSearch } from 'lucide-react'
import { Link } from 'react-router-dom'

export function Brand({ compact = false }: { compact?: boolean }) {
  return <Link to="/" className="flex items-center gap-2 font-semibold tracking-tight" aria-label="ResumeLens home">
    <span className="grid size-8 place-items-center rounded-lg bg-primary text-primary-foreground"><ScanSearch className="size-4" /></span>
    {!compact && <span>ResumeLens</span>}
  </Link>
}

import { Brand } from '@/components/brand'
import { Button } from '@/components/ui/button'
import { Separator } from '@/components/ui/separator'
import { Sheet, SheetContent, SheetTrigger } from '@/components/ui/sheet'
import { useTheme } from '@/hooks/use-theme'
import { BookOpen, FileUp, History, LayoutDashboard, Menu, Moon, ScanSearch, Settings, Sun, Workflow } from 'lucide-react'
import { type ReactNode } from 'react'
import { Link, NavLink, Outlet } from 'react-router-dom'

const navigation = [
  { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/', label: 'Analyze resume', icon: FileUp },
  { to: '/history', label: 'History', icon: History },
  { to: '/models', label: 'Models', icon: ScanSearch },
  { to: '/settings', label: 'Settings', icon: Settings },
  { to: '/about', label: 'About', icon: BookOpen },
]

function Navigation({ onNavigate }: { onNavigate?: () => void }) {
  return <nav className="grid gap-1" aria-label="Application navigation">
    {navigation.map(({ to, label, icon: Icon }) => <NavLink key={to} to={to} onClick={onNavigate} className={({ isActive }) => `flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors ${isActive ? 'bg-accent text-accent-foreground' : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'}`}>
      <Icon className="size-4" />{label}
    </NavLink>)}
  </nav>
}

export function AppLayout({ children }: { children?: ReactNode }) {
  const { theme, setTheme } = useTheme()
  const nextTheme = theme === 'light' ? 'dark' : 'light'
  return <div className="min-h-screen bg-background">
    <aside className="fixed inset-y-0 left-0 hidden w-64 border-r bg-card p-5 lg:block">
      <Brand /><Separator className="my-6" /><Navigation />
      <div className="absolute bottom-5 left-5 right-5 rounded-lg border bg-muted/40 p-3 text-xs leading-relaxed text-muted-foreground"><Workflow className="mb-2 size-4 text-foreground" />Local-first analysis with transparent fallback paths.</div>
    </aside>
    <main className="lg:pl-64">
      <header className="sticky top-0 z-20 flex h-16 items-center justify-between border-b bg-background/90 px-4 backdrop-blur sm:px-6 lg:px-8">
        <div className="flex items-center gap-3 lg:hidden"><Sheet><SheetTrigger render={<Button variant="ghost" size="icon" aria-label="Open navigation" />}><Menu /></SheetTrigger><SheetContent side="left" className="w-72"><Brand /><Separator className="my-6" /><Navigation /></SheetContent></Sheet><Brand compact /></div>
        <div className="hidden lg:block"><p className="text-sm font-medium">Lightweight AI-powered Resume Intelligence</p></div>
        <Button variant="ghost" size="icon" onClick={() => setTheme(nextTheme)} aria-label={`Switch to ${nextTheme} mode`}>
          {theme === 'dark' ? <Sun className="size-4" /> : <Moon className="size-4" />}
        </Button>
      </header>
      <div className="mx-auto w-full max-w-7xl p-4 sm:p-6 lg:p-8">{children ?? <Outlet />}</div>
    </main>
  </div>
}

export function MarketingHeader() {
  const { theme, setTheme } = useTheme()
  return <header className="mx-auto flex w-full max-w-6xl items-center justify-between px-5 py-5 sm:px-8">
    <Brand /><div className="flex items-center gap-2"><Link to="/about" className="hidden text-sm text-muted-foreground hover:text-foreground sm:block">Architecture</Link><Button variant="ghost" size="icon" onClick={() => setTheme(theme === 'dark' ? 'light' : 'dark')} aria-label="Toggle color theme">{theme === 'dark' ? <Sun className="size-4" /> : <Moon className="size-4" />}</Button><Button nativeButton={false} render={<a href="#upload" />} size="sm">Analyze resume</Button></div>
  </header>
}

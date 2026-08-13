import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Label } from '@/components/ui/label'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Separator } from '@/components/ui/separator'
import { Switch } from '@/components/ui/switch'
import { useTheme, type ThemePreference } from '@/hooks/use-theme'
import { api } from '@/services/api'
import type { LocalLlmSettings } from '@/types/analysis'
import { HardDrive, ShieldCheck, SlidersHorizontal } from 'lucide-react'
import { useEffect, useState, type ReactNode } from 'react'

export function SettingsPage() {
  const { theme, setTheme } = useTheme()

  return (
    <div className="space-y-6">
      <div>
        <p className="text-sm font-medium text-muted-foreground">Application preferences</p>
        <h1 className="mt-1 text-3xl font-semibold tracking-tight">Settings</h1>
        <p className="mt-2 text-muted-foreground">The analysis defaults are intentionally constrained to keep results transparent and resource-conscious.</p>
      </div>
      <div className="grid gap-6 lg:grid-cols-2">
        <Card>
          <CardHeader><CardTitle>Appearance</CardTitle><CardDescription>Choose how ResumeLens follows your system theme.</CardDescription></CardHeader>
          <CardContent>
            <Label htmlFor="theme">Color theme</Label>
            <Select value={theme} onValueChange={value => setTheme(value as ThemePreference)}>
              <SelectTrigger id="theme" className="mt-2"><SelectValue /></SelectTrigger>
              <SelectContent><SelectItem value="system">System</SelectItem><SelectItem value="light">Light</SelectItem><SelectItem value="dark">Dark</SelectItem></SelectContent>
            </Select>
          </CardContent>
        </Card>
        <Card>
          <CardHeader><CardTitle>Analysis defaults</CardTitle><CardDescription>Server-side rules are configured in environment variables rather than exposed as unsafe end-user controls.</CardDescription></CardHeader>
          <CardContent className="space-y-4">
            <Row icon={<SlidersHorizontal />} title="Classification threshold" detail="0.62 minimum contextual relevance" />
            <Separator />
            <Row icon={<HardDrive />} title="Maximum upload size" detail="8 MB PDF or DOCX" />
            <Separator />
            <Row icon={<ShieldCheck />} title="Local processing" detail="No external service required for core analysis" />
          </CardContent>
        </Card>
        <LocalLlmPolicy />
      </div>
    </div>
  )
}

function LocalLlmPolicy() {
  const [settings, setSettings] = useState<LocalLlmSettings | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    api.localLlmSettings()
      .then(setSettings)
      .catch(() => setError('Unable to check the trusted local adapter.'))
      .finally(() => setLoading(false))
  }, [])

  const update = async (enabled: boolean) => {
    setSaving(true)
    setError(null)
    try {
      setSettings(await api.setLocalLlmEnabled(enabled))
    } catch {
      setError('Unable to update the local model policy. The existing policy is unchanged.')
    } finally {
      setSaving(false)
    }
  }

  const adapterUnavailable = !settings?.adapterAvailable
  const detail = loading
    ? 'Checking whether the server has a trusted local adapter configured.'
    : adapterUnavailable
      ? 'No trusted local adapter is available. ResumeLens will use deterministic, evidence-only explanations.'
      : settings.enabled
        ? 'The trusted local adapter will be used for new analyses.'
        : 'The trusted local adapter is available but turned off. New analyses will use deterministic, evidence-only explanations.'

  return (
    <Card className="lg:col-span-2">
      <CardHeader>
        <CardTitle>Local model policy</CardTitle>
        <CardDescription>Local LLM output is optional. If unavailable, ResumeLens uses a deterministic, evidence-only explanation.</CardDescription>
      </CardHeader>
      <CardContent className="flex items-center justify-between gap-6 rounded-b-xl bg-muted/20">
        <div>
          <Label htmlFor="local-llm" className="text-sm font-medium">Use configured local LLM</Label>
          <p id="local-llm-description" className="mt-1 text-sm text-muted-foreground">{detail}</p>
          {error && <p role="status" className="mt-2 text-sm text-destructive">{error}</p>}
        </div>
        <Switch
          id="local-llm"
          checked={settings?.enabled ?? false}
          disabled={loading || saving || adapterUnavailable}
          onCheckedChange={update}
          aria-describedby="local-llm-description"
        />
      </CardContent>
    </Card>
  )
}

function Row({ icon, title, detail }: { icon: ReactNode; title: string; detail: string }) {
  return <div className="flex gap-3"><span className="text-muted-foreground">{icon}</span><div><p className="text-sm font-medium">{title}</p><p className="mt-0.5 text-sm text-muted-foreground">{detail}</p></div></div>
}

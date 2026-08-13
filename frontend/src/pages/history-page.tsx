import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { api } from '@/services/api'
import type { AnalysisSummary } from '@/types/analysis'
import { MoreHorizontal, Trash2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

export function HistoryPage() {
  const [items, setItems] = useState<AnalysisSummary[]>([]); const [loading, setLoading] = useState(true); const [error, setError] = useState<string | null>(null)
  const navigate = useNavigate()
  useEffect(() => { api.history().then(setItems).catch(reason => setError(reason instanceof Error ? reason.message : 'Unable to load history.')).finally(() => setLoading(false)) }, [])
  const open = (id: string) => { localStorage.setItem('resumelens-active-analysis', id); navigate('/dashboard') }
  const remove = async (id: string) => { try { await api.delete(id); setItems(current => current.filter(item => item.analysisId !== id)); if (localStorage.getItem('resumelens-active-analysis') === id) localStorage.removeItem('resumelens-active-analysis') } catch (reason) { setError(reason instanceof Error ? reason.message : 'Unable to delete analysis.') } }
  return <div className="space-y-6"><div><p className="text-sm font-medium text-muted-foreground">Resume workspace</p><h1 className="mt-1 text-3xl font-semibold tracking-tight">Analysis history</h1><p className="mt-2 text-muted-foreground">Analyses remain available while the local API is running. Uploaded files themselves are not retained.</p></div><Card><CardHeader className="flex-row items-center justify-between space-y-0"><div><CardTitle>Recent analyses</CardTitle><CardDescription>View, compare, or remove the reports created in this session.</CardDescription></div><Button nativeButton={false} render={<Link to="/" />}>Analyze resume</Button></CardHeader><CardContent>{error && <p className="mb-4 rounded-lg border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">{error}</p>}{loading ? <Skeleton className="h-48 w-full" /> : items.length ? <div className="overflow-x-auto"><Table><TableHeader><TableRow><TableHead>Resume</TableHead><TableHead>Date</TableHead><TableHead>Technical</TableHead><TableHead>Professional</TableHead><TableHead>Status</TableHead><TableHead className="text-right">Actions</TableHead></TableRow></TableHeader><TableBody>{items.map(item => <TableRow key={item.analysisId}><TableCell className="font-medium">{item.resumeName}</TableCell><TableCell>{new Date(item.analyzedAt).toLocaleString()}</TableCell><TableCell>{item.technicalScore}%</TableCell><TableCell>{item.nonTechnicalScore}%</TableCell><TableCell><Badge variant="secondary">{item.status.toLowerCase()}</Badge></TableCell><TableCell className="text-right"><Button variant="ghost" size="sm" onClick={() => open(item.analysisId)}>View</Button><Button variant="ghost" size="icon" onClick={() => remove(item.analysisId)} aria-label={`Delete ${item.resumeName}`}><Trash2 className="size-4" /></Button></TableCell></TableRow>)}</TableBody></Table></div> : <div className="rounded-lg border border-dashed p-10 text-center text-sm text-muted-foreground"><MoreHorizontal className="mx-auto mb-3 size-5" />There are no saved analyses in this session.</div>}</CardContent></Card></div>
}

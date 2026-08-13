import type { Analysis, AnalysisSummary, Diagnostics, LocalLlmSettings } from '@/types/analysis'

const API_BASE = import.meta.env.VITE_API_BASE_URL ?? ''

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, init)
  if (!response.ok) {
    const data = await response.json().catch(() => ({})) as { message?: string }
    throw new Error(data.message ?? `Request failed (${response.status})`)
  }
  if (response.status === 204) return undefined as T
  return response.json() as Promise<T>
}

export const api = {
  analyze: (file: File, onProgress?: (percent: number) => void, signal?: AbortSignal) => {
    const form = new FormData()
    form.append('file', file)
    return new Promise<Analysis>((resolve, reject) => {
      const upload = new XMLHttpRequest()
      upload.open('POST', `${API_BASE}/api/resumes/analyze`)
      upload.responseType = 'json'
      upload.upload.onprogress = event => { if (event.lengthComputable) onProgress?.(Math.round(event.loaded / event.total * 100)) }
      upload.onerror = () => reject(new Error('The backend is unavailable. Start the ResumeLens API and try again.'))
      upload.onload = () => {
        const data = upload.response as Analysis & { message?: string }
        if (upload.status >= 200 && upload.status < 300) resolve(data)
        else reject(new Error(data?.message ?? `Request failed (${upload.status})`))
      }
      signal?.addEventListener('abort', () => { upload.abort(); reject(new DOMException('Upload cancelled', 'AbortError')) }, { once: true })
      upload.send(form)
    })
  },
  analysis: (id: string) => request<Analysis>(`/api/analyses/${id}`),
  history: () => request<AnalysisSummary[]>('/api/analyses'),
  delete: (id: string) => request<void>(`/api/analyses/${id}`, { method: 'DELETE' }),
  diagnostics: () => request<Diagnostics>('/api/system/diagnostics'),
  localLlmSettings: () => request<LocalLlmSettings>('/api/settings/local-llm'),
  setLocalLlmEnabled: (enabled: boolean) => request<LocalLlmSettings>('/api/settings/local-llm', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ enabled }),
  }),
}

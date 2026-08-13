import { useEffect, useState } from 'react'

export type ThemePreference = 'light' | 'dark' | 'system'
const key = 'resumelens-theme'

function resolve(preference: ThemePreference) {
  return preference === 'system' ? window.matchMedia('(prefers-color-scheme: dark)').matches : preference === 'dark'
}

export function useTheme() {
  const [theme, setThemeState] = useState<ThemePreference>(() => (localStorage.getItem(key) as ThemePreference | null) ?? 'system')
  useEffect(() => {
    document.documentElement.classList.toggle('dark', resolve(theme))
    localStorage.setItem(key, theme)
  }, [theme])
  return { theme, setTheme: setThemeState }
}

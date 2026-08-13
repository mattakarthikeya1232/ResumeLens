import { TooltipProvider } from '@/components/ui/tooltip'
import { AppLayout } from '@/layouts/app-layout'
import { lazy, Suspense } from 'react'
import { BrowserRouter, Route, Routes } from 'react-router-dom'

const LandingPage = lazy(() => import('@/pages/landing-page').then(module => ({ default: module.LandingPage })))
const DashboardPage = lazy(() => import('@/pages/dashboard-page').then(module => ({ default: module.DashboardPage })))
const HistoryPage = lazy(() => import('@/pages/history-page').then(module => ({ default: module.HistoryPage })))
const ModelsPage = lazy(() => import('@/pages/models-page').then(module => ({ default: module.ModelsPage })))
const SettingsPage = lazy(() => import('@/pages/settings-page').then(module => ({ default: module.SettingsPage })))
const AboutPage = lazy(() => import('@/pages/about-page').then(module => ({ default: module.AboutPage })))

function ApplicationRoutes() {
  return <Routes><Route path="/" element={<LandingPage />} /><Route element={<AppLayout />}><Route path="/dashboard" element={<DashboardPage />} /><Route path="/history" element={<HistoryPage />} /><Route path="/models" element={<ModelsPage />} /><Route path="/settings" element={<SettingsPage />} /><Route path="/about" element={<AboutPage />} /></Route></Routes>
}

export default function App() {
  return <BrowserRouter><TooltipProvider><Suspense fallback={<div className="grid min-h-screen place-items-center text-sm text-muted-foreground">Loading ResumeLens…</div>}><ApplicationRoutes /></Suspense></TooltipProvider></BrowserRouter>
}

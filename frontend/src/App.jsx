import { useRef, useState } from 'react'
import { T } from './i18n'
import { analyze } from './services/api'
import Header from './components/Header'
import Hero from './components/Hero'
import PlanForm from './components/PlanForm'
import HowItWorks from './components/HowItWorks'
import { ErrorBanner, CachedBanner } from './components/Banners'
import { ScoreCard, EnvCard, Factors } from './components/Score'
import RouteMap from './components/RouteMap'
import RouteCards from './components/RouteCards'

export default function App() {
  const [lang, setLang] = useState('en')
  const t = T[lang]
  const [form, setForm] = useState({ origin: null, destination: null, workerType: 'delivery', mobility: 'motorcycle' })
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(false)
  const [selected, setSelected] = useState(null)
  const planRef = useRef(null)

  async function run() {
    setLoading(true); setError(false)
    try {
      const r = await analyze(form)
      setResult(r); setSelected((r.routes.find((x) => x.recommended) || r.routes[0]).id)
      setTimeout(() => document.getElementById('results')?.scrollIntoView({ behavior: 'smooth' }), 100)
    } catch { setResult(null); setError(true) }
    finally { setLoading(false) }
  }

  return (
    <div className="page" id="top">
      <Header t={t} lang={lang} setLang={setLang} dataSource={result?.dataSource} />
      <main>
        <div className="top">
          <Hero t={t} onPlan={() => planRef.current?.scrollIntoView({ behavior: 'smooth' })} />
          <div ref={planRef}><PlanForm t={t} form={form} setForm={setForm} onSubmit={run} loading={loading} /></div>
          <HowItWorks t={t} />
        </div>
        {error && <ErrorBanner t={t} />}
        {result?.dataSource === 'cached' && <CachedBanner t={t} />}
        {result && (
          <section id="results" className="dash">
            <h2 className="dash-title"><span className="logo sm">K</span>{t.results}</h2>
            <div className="grid">
              <div className="col left"><ScoreCard t={t} r={result} /><EnvCard t={t} r={result} /><Factors t={t} r={result} /></div>
              <div className="col mid"><RouteMap t={t} routes={result.routes} selectedId={selected} /></div>
              <div className="col right"><RouteCards t={t} routes={result.routes} selectedId={selected} onSelect={setSelected} /></div>
            </div>
          </section>
        )}
      </main>
    </div>
  )
}

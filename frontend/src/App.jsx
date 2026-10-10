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
  const [view, setView] = useState('home') // home | results | about | how | workers
  const [form, setForm] = useState({ origin: null, destination: null, workerType: 'delivery', mobility: 'motorcycle' })
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(false)
  const [selected, setSelected] = useState(null)
  const planRef = useRef(null)
  const backLabel = lang === 'hi' ? '← दूसरा रास्ता चुनें' : '← Plan another route'

  async function run() {
    setLoading(true); setError(false)
    try {
      const r = await analyze(form)
      setResult(r)
      setSelected((r.routes.find((x) => x.recommended) || r.routes[0]).id)
      setView('results')
      window.scrollTo({ top: 0 })
    } catch { setResult(null); setError(true) }
    finally { setLoading(false) }
  }

  const goPlan = () => { setView('home'); window.scrollTo({ top: 0 }) }

  return (
    <div className="page" id="top">
      <Header t={t} lang={lang} setLang={setLang} dataSource={result?.dataSource} view={view} setView={setView} />
      <main>
        {view === 'home' && (
          <>
            <div className="top">
              <Hero t={t} onPlan={() => planRef.current?.scrollIntoView({ behavior: 'smooth' })} />
              <div ref={planRef}><PlanForm t={t} form={form} setForm={setForm} onSubmit={run} loading={loading} /></div>
              <HowItWorks t={t} />
            </div>
            {error && <ErrorBanner t={t} />}
          </>
        )}

        {view === 'results' && result && (
          <section className="dash">
            <button className="btn ghost back" onClick={goPlan}>{backLabel}</button>
            {result.dataSource === 'cached' && <CachedBanner t={t} />}
            <h2 className="dash-title"><span className="logo sm">K</span>{t.results}</h2>
            <div className="grid">
              <div className="col left"><ScoreCard t={t} r={result} /><EnvCard t={t} r={result} /><Factors t={t} r={result} /></div>
              <div className="col mid"><RouteMap t={t} routes={result.routes} selectedId={selected} /></div>
              <div className="col right"><RouteCards t={t} routes={result.routes} selectedId={selected} onSelect={setSelected} /></div>
            </div>
          </section>
        )}

        {view === 'about' && (
          <section className="card info">
            <h2>{t.about}</h2>
            <h3>{t.heroTitle}</h3>
            <p className="muted">{t.heroSub}</p>
            <ul className="feats">{[t.f1, t.f2, t.f3, t.f4].map((x) => <li key={x}>✓ {x}</li>)}</ul>
            <button className="btn" onClick={goPlan}>{t.planBtn} →</button>
          </section>
        )}

        {view === 'how' && (
          <section className="card info">
            <h2>{t.howTitle}</h2>
            <div className="steps">
              {[t.s1, t.s2, t.s3, t.s4].map((s, i) => (
                <div key={i} className="stp"><b>{i + 1}</b><span>{s}</span></div>
              ))}
            </div>
            <button className="btn" onClick={goPlan} style={{ marginTop: 16 }}>{t.planBtn} →</button>
          </section>
        )}

        {view === 'workers' && (
          <section className="card info">
            <h2>{t.workers}</h2>
            <div className="workers">
              {[['🛵', 'delivery'], ['🏗️', 'construction'], ['🛒', 'street'], ['🧹', 'sanitation']].map(([e, k]) => (
                <div key={k} className="wk"><span>{e}</span><b>{t[k]}</b></div>
              ))}
            </div>
            <button className="btn" onClick={goPlan}>{t.planBtn} →</button>
          </section>
        )}
      </main>
    </div>
  )
}

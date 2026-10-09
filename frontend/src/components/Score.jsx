const tone = (s) => (s >= 75 ? 'good' : s >= 50 ? 'mid' : 'bad')
export function ScoreCard({ t, r }) {
  const C = 2 * Math.PI * 44
  return (
    <div className="card score">
      <h3>{t.score}</h3>
      <div className="score-body">
        <div className="ring">
          <svg viewBox="0 0 100 100"><circle cx="50" cy="50" r="44" className="track" />
            <circle cx="50" cy="50" r="44" className={'arc ' + tone(r.score)} strokeDasharray={`${(r.score / 100) * C} ${C}`} transform="rotate(-90 50 50)" /></svg>
          <div className="num"><strong>{r.score}</strong><small>/100</small></div>
          <span className={'badge ' + tone(r.score)}>{t[r.label] || r.label}</span>
        </div>
        <div className="window"><small>{t.window}</small><strong>{r.window}</strong><small>{t.windowNote}</small></div>
      </div>
    </div>
  )
}
export function EnvCard({ t, r }) {
  return (
    <div className="card env">
      <h3>{t.env}</h3>
      <div className="envgrid">
        <div><span className="ic">🌡️</span><strong>{r.temperature}°C</strong><small>{t.temp}</small></div>
        <div><span className="ic">💨</span><strong>{r.aqi}</strong><small>{t.aqi} · {t[r.aqiLabel] || r.aqiLabel}</small></div>
        <div><span className="ic">🌧️</span><strong>{r.rainProb}%</strong><small>{t.rain}</small></div>
      </div>
    </div>
  )
}
export function Factors({ t, r }) {
  const rows = [[t.heat, r.factors.heat], [t.air, r.factors.air], [t.rainF, r.factors.rain]]
  return (
    <div className="card factors">
      <h3>{t.factors}</h3>
      {rows.map(([l, v]) => (
        <div className="bar" key={l}><span>{l}</span>
          <div className="trk"><i className={tone(100 - v)} style={{ width: v + '%' }} /></div><b>{v}</b></div>
      ))}
    </div>
  )
}

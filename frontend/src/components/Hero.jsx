export default function Hero({ t, onPlan }) {
  const feats = [['🌡️', t.f1], ['💨', t.f2], ['🌿', t.f3], ['🛡️', t.f4]]
  return (
    <section className="hero card" id="about">
      <div className="hero-copy">
        <h1>{t.heroTitle}</h1>
        <p>{t.heroSub}</p>
        <ul className="feats">{feats.map(([i, l]) => <li key={l}><span>{i}</span>{l}</li>)}</ul>
        <button className="btn" onClick={onPlan}>{t.planBtn} →</button>
      </div>
      <div className="hero-art" aria-hidden="true">
        <svg viewBox="0 0 300 220" preserveAspectRatio="xMidYMax slice">
          <circle cx="220" cy="60" r="34" fill="#ffd27a" />
          <path d="M0 150 L50 110 L90 140 L140 90 L190 135 L240 105 L300 145 V220 H0Z" fill="#cfe6d8" />
          <path d="M0 175 Q80 140 150 170 T300 165 V220 H0Z" fill="#1f6b4a" />
          <rect x="125" y="120" width="50" height="42" rx="8" fill="#e04b2f" />
          <circle cx="132" cy="170" r="12" fill="#222" /><circle cx="170" cy="170" r="12" fill="#222" />
        </svg>
      </div>
    </section>
  )
}

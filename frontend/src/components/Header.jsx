export default function Header({ t, lang, setLang, dataSource, view, setView }) {
  const links = [['home', t.home], ['about', t.about], ['how', t.how], ['workers', t.workers]]
  const current = view === 'results' ? 'home' : view
  return (
    <header className="nav">
      <div className="brand" onClick={() => setView('home')} style={{ cursor: 'pointer' }}>
        <span className="logo">K</span>KaamSafe
      </div>
      <nav className="navlinks">
        {links.map(([id, label]) => (
          <button key={id} className={'navbtn ' + (current === id ? 'active' : '')} onClick={() => { setView(id); window.scrollTo({ top: 0 }) }}>{label}</button>
        ))}
      </nav>
      <div className="navright">
        <span className="pill"><i className={'dot ' + (dataSource === 'cached' ? 'amber' : '')} />{dataSource === 'cached' ? t.cached : t.live}</span>
        <div className="langs">
          <button className={lang === 'en' ? 'on' : ''} onClick={() => setLang('en')}>EN</button>
          <button className={lang === 'hi' ? 'on' : ''} onClick={() => setLang('hi')}>हिं</button>
        </div>
      </div>
    </header>
  )
}


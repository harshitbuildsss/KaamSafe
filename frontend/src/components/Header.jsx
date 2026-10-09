export default function Header({ t, lang, setLang, dataSource }) {
  return (
    <header className="nav">
      <div className="brand"><span className="logo">K</span>KaamSafe</div>
      <nav className="navlinks">
        <a className="active" href="#top">{t.home}</a><a href="#about">{t.about}</a>
        <a href="#how">{t.how}</a><a href="#results">{t.workers}</a>
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

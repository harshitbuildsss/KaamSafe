export default function RouteCards({ t, routes, selectedId, onSelect }) {
  return (
    <div className="card routes">
      <h3>{t.routeOptions}</h3>
      {routes.map((r) => {
        const sel = r.id === selectedId
        return (
          <article key={r.id} className={'route ' + (sel ? 'sel' : '')} onClick={() => onSelect(r.id)}>
            <header><span className={'tag ' + (r.recommended ? 'rec' : '')}>{r.recommended ? t.recommended : t.alternate}</span><small>{r.id}</small></header>
            <div className="stats">
              <div><small>{t.distance}</small><b>{r.distanceKm} {t.km}</b></div>
              <div><small>{t.duration}</small><b>{r.durationMin} {t.min}</b></div>
              <div className="rs"><b>{r.score}</b><small>/100</small></div>
            </div>
            <ul>{r.tags.map((x) => <li key={x}>{x}</li>)}</ul>
            <button className={'btn wide ' + (sel ? '' : 'ghost')}>{sel ? t.selected : t.viewMap}</button>
          </article>
        )
      })}
    </div>
  )
}

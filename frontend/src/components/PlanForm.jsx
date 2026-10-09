import PlaceInput from './PlaceInput'

export default function PlanForm({ t, form, setForm, onSubmit, loading }) {
  const set = (k) => (v) => setForm((f) => ({ ...f, [k]: v }))
  const ok = form.origin && form.destination
  return (
    <section className="card plan" id="plan">
      <h2>{t.planTitle}</h2>
      <p className="muted">{t.planSub}</p>
      <PlaceInput label={t.origin} value={form.origin} onPick={set('origin')} hint={t.typeHint} />
      <PlaceInput label={t.dest} value={form.destination} onPick={set('destination')} hint={t.typeHint} />
      <div className="row">
        <label className="field"><span>{t.worker}</span>
          <select value={form.workerType} onChange={(e) => set('workerType')(e.target.value)}>
            {['delivery', 'construction', 'street', 'sanitation'].map((k) => <option key={k} value={k}>{t[k]}</option>)}
          </select></label>
        <label className="field"><span>{t.mobility}</span>
          <select value={form.mobility} onChange={(e) => set('mobility')(e.target.value)}>
            {['motorcycle', 'cycle', 'walking'].map((k) => <option key={k} value={k}>{t[k]}</option>)}
          </select></label>
      </div>
      <button className="btn wide" disabled={!ok || loading} onClick={onSubmit}>{loading ? t.analyzing : t.analyze + ' →'}</button>
    </section>
  )
}

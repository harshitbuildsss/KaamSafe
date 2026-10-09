export default function HowItWorks({ t }) {
  return (
    <aside className="card how" id="how">
      <h2>{t.howTitle}</h2>
      <ol>{[t.s1, t.s2, t.s3, t.s4].map((s, i) => <li key={i}><b>{i + 1}</b><span>{s}</span></li>)}</ol>
    </aside>
  )
}

import { useEffect, useState } from 'react'
import { searchPlaces } from '../services/api'

export default function PlaceInput({ label, value, onPick, hint }) {
  const [text, setText] = useState(value?.name || '')
  const [list, setList] = useState([])
  const [open, setOpen] = useState(false)

  useEffect(() => {
    if (text.length < 3 || text === value?.name) { setList([]); return }
    const id = setTimeout(async () => { try { setList(await searchPlaces(text)) } catch { setList([]) } }, 300)
    return () => clearTimeout(id)
  }, [text])

  return (
    <label className="field">
      <span>{label}</span>
      <div className="inputwrap">
        <input value={text} placeholder={hint} onChange={(e) => { setText(e.target.value); onPick(null); setOpen(true) }} onBlur={() => setTimeout(() => setOpen(false), 150)} />
        {open && list.length > 0 && (
          <ul className="suggest">{list.map((p) => (
            <li key={p.name} onMouseDown={() => { setText(p.name); onPick(p); setOpen(false) }}>{p.name}</li>
          ))}</ul>
        )}
      </div>
    </label>
  )
}

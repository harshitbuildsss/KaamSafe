import { useEffect, useState } from 'react';
import { geocode } from '../services/api.js';

// Search box. Only a place picked from the list counts as a location.
export default function LocationInput({ label, placeholder, value, onChange }) {
  const [text, setText] = useState('');
  const [results, setResults] = useState([]);
  const [open, setOpen] = useState(false);
  const [searched, setSearched] = useState(false);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (value || text.trim().length < 3) { setResults([]); setSearched(false); return; }
    const timer = setTimeout(async () => {
      try {
        setResults(await geocode(text.trim()));
        setFailed(false);
      } catch {
        setResults([]);
        setFailed(true);
      }
      setSearched(true);
      setOpen(true);
    }, 400); // wait until the user stops typing
    return () => clearTimeout(timer);
  }, [text, value]);

  function pick(place) {
    setText(place.label);
    onChange(place);
    setOpen(false);
  }

  return (
    <label className="location">{label}
      <input
        value={text}
        placeholder={placeholder}
        onChange={e => { setText(e.target.value); onChange(null); }}
        onFocus={() => results.length && setOpen(true)}
        onBlur={() => setTimeout(() => setOpen(false), 150)}
      />
      {open && searched && (
        <ul className="suggestions">
          {failed && <li className="hint">Search is unavailable right now.</li>}
          {!failed && results.length === 0 && <li className="hint">No places found. Try a different name.</li>}
          {results.map(p => (
            <li key={`${p.latitude},${p.longitude}`}>
              <button type="button" onMouseDown={() => pick(p)}>{p.label}</button>
            </li>
          ))}
        </ul>
      )}
      {value && <span className="picked">Location selected</span>}
    </label>
  );
}

import { useState } from 'react';
import { ShieldCheck, MapPinned, ArrowRight } from 'lucide-react';
import { analyzeWorkability } from './services/api.js';

const workers = [
  ['DELIVERY_RIDER', 'Delivery Rider'],
  ['CONSTRUCTION_WORKER', 'Construction Worker'],
  ['STREET_VENDOR', 'Street Vendor'],
  ['WASTE_PICKER', 'Waste Picker'],
  ['SANITATION_WORKER', 'Sanitation Worker']
];

const mobility = [
  ['MOTORCYCLE', 'Motorcycle'],
  ['BICYCLE', 'Bicycle'],
  ['HANDCART', 'Handcart'],
  ['WALKING', 'Walking'],
  ['E_RICKSHAW', 'E-Rickshaw']
];

export default function App() {
  const [workerType, setWorkerType] = useState('DELIVERY_RIDER');
  const [move, setMove] = useState('MOTORCYCLE');
  const [origin, setOrigin] = useState('');
  const [destination, setDestination] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  async function submit(event) {
    event.preventDefault();
    setError('');
    // TODO: Day 1 — geocode origin/destination, then call /api/workability/analyze.
    try {
      const response = await analyzeWorkability({
        workerType,
        mobility: move,
        origin,
        destination
      });
      setResult(response);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <main className="shell">
      <header className="brand">
        <span className="brand-mark"><ShieldCheck size={20} /></span>
        <div>
          <strong>KaamSafe</strong>
          <span>Safer routes. Better work days.</span>
        </div>
      </header>

      <section className="hero">
        <div>
          <p className="eyebrow">Environmental Safety Assistant</p>
          <h1>Know when to work.<br />Know which route to take.</h1>
          <p className="subcopy">
            KaamSafe combines environmental conditions and route characteristics
            to recommend a safer option for outdoor workers.
          </p>
        </div>

        <form className="card form" onSubmit={submit}>
          <h2>Plan your work</h2>

          <label>Worker type
            <select value={workerType} onChange={e => setWorkerType(e.target.value)}>
              {workers.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </label>

          <label>Mobility
            <select value={move} onChange={e => setMove(e.target.value)}>
              {mobility.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
            </select>
          </label>

          <label>From
            <input value={origin} onChange={e => setOrigin(e.target.value)} placeholder="Connaught Place" />
          </label>

          <label>To
            <input value={destination} onChange={e => setDestination(e.target.value)} placeholder="India Gate" />
          </label>

          <button type="submit">Check conditions <ArrowRight size={17} /></button>
          {error && <p className="error">{error}</p>}
        </form>
      </section>

      {result && (
        <section className="card result-placeholder">
          <MapPinned size={20} />
          <div>
            <strong>Backend response connected.</strong>
            <p>Results UI and map rendering will be built against the frozen API contract.</p>
          </div>
        </section>
      )}
    </main>
  );
}

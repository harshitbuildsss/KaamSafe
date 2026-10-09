import { MOCK_RESULT } from '../mock/mockData'

const USE_MOCK = import.meta.env.VITE_USE_MOCK !== 'false'
const BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
const wait = (ms) => new Promise((r) => setTimeout(r, ms))

// Real place search (OpenStreetMap), works for any place in India
export async function searchPlaces(q) {
  const r = await fetch(
    `https://nominatim.openstreetmap.org/search?format=json&limit=6&countrycodes=in&q=${encodeURIComponent(q)}`
  )
  const j = await r.json()
  return j.map((x) => ({ name: x.display_name, lat: +x.lat, lng: +x.lon }))
}

const km = (a, b) => {
  const R = 6371, rad = (d) => (d * Math.PI) / 180
  const dLat = rad(b.lat - a.lat), dLng = rad(b.lng - a.lng)
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(rad(a.lat)) * Math.cos(rad(b.lat)) * Math.sin(dLng / 2) ** 2
  return 2 * R * Math.asin(Math.sqrt(h))
}

// Demo routes: a curved line between the two chosen places (not real roads)
function line(a, b, bend) {
  const pts = []
  for (let i = 0; i <= 8; i++) {
    const t = i / 8, off = Math.sin(Math.PI * t) * bend
    pts.push([a.lat + (b.lat - a.lat) * t + (b.lng - a.lng) * off, a.lng + (b.lng - a.lng) * t - (b.lat - a.lat) * off])
  }
  return pts
}

function demoResult({ origin, destination, mobility }) {
  const speed = { motorcycle: 20, cycle: 12, walking: 5 }[mobility] || 20
  const d = km(origin, destination) * 1.3
  const rec = line(origin, destination, 0.12), alt = line(origin, destination, -0.08)
  const pick = (c, i) => ({ lat: c[i][0], lng: c[i][1] })
  return {
    ...MOCK_RESULT,
    routes: [
      { ...MOCK_RESULT.routes[0], distanceKm: +(d * 1.1).toFixed(1), durationMin: Math.round((d * 1.1 / speed) * 60),
        coords: rec, points: [{ type: 'water', ...pick(rec, 3) }, { type: 'shade', ...pick(rec, 5) }] },
      { ...MOCK_RESULT.routes[1], distanceKm: +d.toFixed(1), durationMin: Math.round((d / speed) * 60),
        coords: alt, points: [{ type: 'water', ...pick(alt, 4) }] },
    ],
  }
}

function adapt(d) { return d } // change this when the real backend response differs

export async function analyze(form) {
  const { origin, destination, workerType, mobility } = form
  if (USE_MOCK) {
    await wait(700)
    if (km(origin, destination) < 0.2) throw new Error('NO_ROUTE')
    return demoResult(form)
  }
  const res = await fetch(`${BASE}/api/workability`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ origin, destination, workerType, mobility }),
  })
  if (res.status === 404 || res.status === 422) throw new Error('NO_ROUTE')
  if (!res.ok) throw new Error('SERVER')
  return adapt(await res.json())
}
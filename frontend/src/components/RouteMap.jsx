import { useEffect, useRef } from 'react'
import L from 'leaflet'

const icon = (c, ch = '') => L.divIcon({ className: '', iconSize: [22, 22], iconAnchor: [11, 11],
  html: `<div style="width:22px;height:22px;border-radius:50%;background:${c};border:2px solid #fff;box-shadow:0 1px 4px #0006;color:#fff;font:700 11px/18px sans-serif;text-align:center">${ch}</div>` })

export default function RouteMap({ t, routes, selectedId }) {
  const el = useRef(null), map = useRef(null), layer = useRef(null)
  useEffect(() => { map.current = L.map(el.current, { zoomControl: true }).setView([28.62, 77.22], 13)
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { attribution: '© OpenStreetMap' }).addTo(map.current)
    layer.current = L.layerGroup().addTo(map.current); return () => map.current.remove() }, [])
  useEffect(() => {
    layer.current.clearLayers()
    const sorted = [...routes].sort((a, b) => (a.id === selectedId) - (b.id === selectedId))
    sorted.forEach((r) => {
      const sel = r.id === selectedId
      L.polyline(r.coords, { color: sel ? '#1f6bd6' : '#8a93a0', weight: sel ? 6 : 4, dashArray: sel ? null : '8 8', opacity: sel ? 1 : .8 }).addTo(layer.current)
      if (sel) r.points?.forEach((p) => L.marker([p.lat, p.lng], { icon: icon(p.type === 'water' ? '#1c9ad6' : '#2f9e5b', p.type === 'water' ? '💧' : '🌳') }).addTo(layer.current))
    })
    const sel = routes.find((r) => r.id === selectedId) || routes[0]
    if (sel) {
      L.marker(sel.coords[0], { icon: icon('#2f9e5b', 'A') }).addTo(layer.current)
      L.marker(sel.coords.at(-1), { icon: icon('#e04b2f', 'B') }).addTo(layer.current)
      map.current.fitBounds(L.polyline(sel.coords).getBounds(), { padding: [40, 40] })
    }
  }, [routes, selectedId])
  return (
    <div className="mapbox">
      <div ref={el} className="map" />
      <div className="legend"><b>{t.legend}</b>
        <span><i style={{ background: '#1f6bd6' }} />{t.legRec}</span><span><i className="dash" />{t.legAlt}</span>
        <span><i style={{ background: '#2f9e5b' }} />{t.legStart}</span><span><i style={{ background: '#e04b2f' }} />{t.legEnd}</span>
        <span>💧 {t.legWater}</span><span>🌳 {t.legShade}</span></div>
    </div>
  )
}

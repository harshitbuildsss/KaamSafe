import { Droplets, Mountain, Trees } from 'lucide-react';

const GREEN = { LOW: 'Little shade', MEDIUM: 'Some shade', HIGH: 'Lots of shade' };

export default function RouteCard({ route, index, selected, onSelect }) {
  return (
    <button
      type="button"
      className={`route-card ${selected ? 'selected' : ''}`}
      onClick={() => onSelect(route.routeId)}
      aria-pressed={selected}
    >
      <div className="route-top">
        <strong>Route {index + 1}</strong>
        {route.recommended && <span className="badge recommended">Recommended</span>}
        <span className="route-score">{route.score}<small>/100</small></span>
      </div>
      <p className="route-meta">
        {(route.distanceMeters / 1000).toFixed(1)} km · {Math.round(route.durationSeconds / 60)} min
      </p>
      <p className="route-icons">
        <span><Trees size={14} /> {GREEN[route.greenCoverageLevel] ?? route.greenCoverageLevel}</span>
        <span><Mountain size={14} /> {route.totalAscentMeters} m climb</span>
        {route.waterPointNearby && <span><Droplets size={14} /> Water nearby</span>}
      </p>
      <ul className="reasons">
        {route.reasons.map(r => <li key={r}>{r}</li>)}
      </ul>
    </button>
  );
}

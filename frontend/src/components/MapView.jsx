import { useEffect } from 'react';
import { MapContainer, TileLayer, GeoJSON, CircleMarker, useMap } from 'react-leaflet';
import L from 'leaflet';

// Zoom the map so every route is visible.
function FitRoutes({ routes }) {
  const map = useMap();
  useEffect(() => {
    const bounds = L.latLngBounds([]);
    routes.forEach(r => r.geometry.coordinates.forEach(([lng, lat]) => bounds.extend([lat, lng])));
    if (bounds.isValid()) map.fitBounds(bounds, { padding: [30, 30] });
  }, [routes, map]);
  return null;
}

export default function MapView({ routes, selectedId, origin, destination }) {
  // Draw the selected route last so it sits on top.
  const ordered = [...routes].sort((a, b) => (a.routeId === selectedId) - (b.routeId === selectedId));
  return (
    <MapContainer center={[28.6139, 77.209]} zoom={12} className="map" scrollWheelZoom={false}>
      <TileLayer
        attribution="&copy; OpenStreetMap contributors"
        url="https://tile.openstreetmap.org/{z}/{x}/{y}.png"
      />
      <FitRoutes routes={routes} />
      {ordered.map(r => {
        const active = r.routeId === selectedId;
        return (
          <GeoJSON
            key={`${r.routeId}-${active}`}
            data={r.geometry}
            style={{ color: active ? '#087f73' : '#8aa39e', weight: active ? 6 : 4, opacity: active ? 1 : 0.7 }}
          />
        );
      })}
      {/* GeoJSON uses [longitude, latitude]; markers use [latitude, longitude]. */}
      {origin && <CircleMarker center={[origin.latitude, origin.longitude]} radius={8} pathOptions={{ color: '#fff', fillColor: '#087f73', fillOpacity: 1, weight: 3 }} />}
      {destination && <CircleMarker center={[destination.latitude, destination.longitude]} radius={8} pathOptions={{ color: '#fff', fillColor: '#c2410c', fillOpacity: 1, weight: 3 }} />}
    </MapContainer>
  );
}

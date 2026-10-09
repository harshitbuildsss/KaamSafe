// Demo data. Shape is adapted in services/api.js — change only that file when the backend differs.
export const PLACES = [
  { name: 'Connaught Place, New Delhi', lat: 28.6315, lng: 77.2167 },
  { name: 'India Gate, New Delhi', lat: 28.6129, lng: 77.2295 },
  { name: 'Karol Bagh, New Delhi', lat: 28.6519, lng: 77.1909 },
  { name: 'Lodi Garden, New Delhi', lat: 28.5931, lng: 77.2197 },
  { name: 'Humayun’s Tomb, New Delhi', lat: 28.5933, lng: 77.2507 },
  { name: 'Rajpath, New Delhi', lat: 28.6143, lng: 77.2195 },
  { name: 'Nehru Place, New Delhi', lat: 28.5494, lng: 77.2516 },
  { name: 'Faridabad Sector 15', lat: 28.4089, lng: 77.3178 },
]

export const MOCK_RESULT = {
  dataSource: 'live',
  score: 72, label: 'Moderate',
  window: '17:00 – 20:00',
  temperature: 38.5, aqi: 142, aqiLabel: 'Moderate', rainProb: 12,
  factors: { heat: 68, air: 55, rain: 12 },
  routes: [
    { id: 'route-2', recommended: true, distanceKm: 5.1, durationMin: 45, score: 72,
      tags: ['Medium green coverage', '14 m total ascent', '2 water points nearby', 'Better environmental conditions'],
      coords: [[28.6315,77.2167],[28.6280,77.2185],[28.6245,77.2160],[28.6200,77.2200],[28.6160,77.2215],[28.6129,77.2295]],
      points: [{ type: 'water', lat: 28.6245, lng: 77.2160 }, { type: 'shade', lat: 28.6200, lng: 77.2200 }, { type: 'water', lat: 28.6160, lng: 77.2215 }] },
    { id: 'route-1', recommended: false, distanceKm: 4.2, durationMin: 50, score: 64,
      tags: ['Low green coverage', '22 m total ascent', 'Shorter route'],
      coords: [[28.6315,77.2167],[28.6290,77.2230],[28.6250,77.2270],[28.6190,77.2290],[28.6129,77.2295]],
      points: [{ type: 'water', lat: 28.6250, lng: 77.2270 }] },
  ],
}

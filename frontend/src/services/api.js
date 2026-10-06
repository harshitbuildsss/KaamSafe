const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

export async function geocode(query) {
  const response = await fetch(`${API_BASE_URL}/api/geocode?query=${encodeURIComponent(query)}`);
  if (!response.ok) throw new Error('Could not find that location.');
  return response.json();
}

export async function analyzeWorkability({ workerType, mobility, origin, destination }) {
  // TODO: geocode both text locations first, then send the frozen request DTO.
  throw new Error('Workability API is not wired yet.');
}

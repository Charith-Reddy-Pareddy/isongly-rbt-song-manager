const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080';

async function handle(response) {
  if (!response.ok) {
    const message = await response.text().catch(() => response.statusText);
    throw new Error(message || `Request failed with status ${response.status}`);
  }
  return response.json();
}

export function getRange(min, max) {
  const params = new URLSearchParams();
  if (min !== '' && min != null) params.set('min', min);
  if (max !== '' && max != null) params.set('max', max);
  return fetch(`${BASE_URL}/api/songs/range?${params}`).then(handle);
}

export function getFilteredByYear(year) {
  const params = new URLSearchParams();
  if (year !== '' && year != null) params.set('year', year);
  return fetch(`${BASE_URL}/api/songs/filter?${params}`).then(handle);
}

export function getTopFive() {
  return fetch(`${BASE_URL}/api/songs/top-five`).then(handle);
}

export function reset() {
  return fetch(`${BASE_URL}/api/songs/reset`, { method: 'POST' }).then(handle);
}

export function uploadCsv(file) {
  const formData = new FormData();
  formData.append('file', file);
  return fetch(`${BASE_URL}/api/songs/upload`, { method: 'POST', body: formData }).then(handle);
}

export function search({ q = '', genre = '', sortBy = 'title', sortDir = 'asc' } = {}, signal) {
  const params = new URLSearchParams({ sortBy, sortDir });
  if (q) params.set('q', q);
  if (genre) params.set('genre', genre);
  return fetch(`${BASE_URL}/api/songs/search?${params}`, { signal }).then(handle);
}

export function getGenres() {
  return fetch(`${BASE_URL}/api/songs/genres`).then(handle);
}

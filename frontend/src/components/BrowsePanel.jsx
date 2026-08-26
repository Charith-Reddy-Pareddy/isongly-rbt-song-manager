import { useEffect, useState } from 'react';
import { search, getGenres } from '../api';
import SongTable from './SongTable';
import StatBar from './StatBar';

const VALID_SORT_COLUMNS = new Set(['title', 'artist', 'genre', 'year', 'bpm', 'energy']);

function readInitialState() {
  const params = new URLSearchParams(window.location.search);
  const sortBy = params.get('sortBy');
  return {
    query: params.get('q') ?? '',
    genre: params.get('genre') ?? '',
    sortBy: VALID_SORT_COLUMNS.has(sortBy) ? sortBy : 'title',
    sortDir: params.get('sortDir') === 'desc' ? 'desc' : 'asc',
  };
}

export default function BrowsePanel() {
  const [initial] = useState(readInitialState);
  const [query, setQuery] = useState(initial.query);
  const [genre, setGenre] = useState(initial.genre);
  const [genres, setGenres] = useState([]);
  const [sortBy, setSortBy] = useState(initial.sortBy);
  const [sortDir, setSortDir] = useState(initial.sortDir);
  const [retryToken, setRetryToken] = useState(0);

  const [songs, setSongs] = useState([]);
  const [status, setStatus] = useState('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    getGenres().then(setGenres).catch(() => {});
  }, []);

  // Keep the URL in sync so the current search/filter/sort is bookmarkable,
  // shareable, and survives a page refresh.
  useEffect(() => {
    const params = new URLSearchParams();
    if (query) params.set('q', query);
    if (genre) params.set('genre', genre);
    if (sortBy !== 'title') params.set('sortBy', sortBy);
    if (sortDir !== 'asc') params.set('sortDir', sortDir);
    const queryString = params.toString();
    const newUrl = window.location.pathname + (queryString ? `?${queryString}` : '');
    window.history.replaceState(null, '', newUrl);
  }, [query, genre, sortBy, sortDir]);

  useEffect(() => {
    const controller = new AbortController();
    const timer = setTimeout(() => {
      setStatus('loading');
      setErrorMessage('');
      search({ q: query, genre, sortBy, sortDir }, controller.signal)
        .then((result) => {
          setSongs(result);
          setStatus('idle');
        })
        .catch((err) => {
          if (err.name === 'AbortError') return; // superseded by a newer request
          setStatus('error');
          setErrorMessage(err.message || 'Something went wrong talking to the backend.');
        });
    }, 250);
    return () => {
      clearTimeout(timer);
      controller.abort();
    };
  }, [query, genre, sortBy, sortDir, retryToken]);

  function handleSort(column) {
    if (column === sortBy) {
      setSortDir((dir) => (dir === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(column);
      setSortDir('asc');
    }
  }

  return (
    <>
      <div className="browse-controls">
        <input
          type="search"
          className="search-input"
          placeholder="Search by title or artist…"
          aria-label="Search by title or artist"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <select aria-label="Filter by genre" value={genre} onChange={(e) => setGenre(e.target.value)}>
          <option value="">All genres</option>
          {genres.map((g) => (
            <option key={g} value={g}>
              {g}
            </option>
          ))}
        </select>
      </div>

      <section className="results">
        <div className="results-header">
          <span className={`status-dot status-${status}`} />
          <span>
            {status === 'error'
              ? errorMessage
              : query || genre
                ? `Matches for "${query || 'any title/artist'}"${genre ? ` in ${genre}` : ''} — click a column to sort`
                : 'All songs — click a column to sort'}
          </span>
        </div>
        {status === 'loading' && songs.length === 0 ? (
          <p className="empty-state">Loading…</p>
        ) : status === 'error' ? (
          <div className="empty-state error">
            <p>
              Could not reach the backend. Is it running at{' '}
              <code>{import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'}</code>?
            </p>
            <button type="button" className="secondary" onClick={() => setRetryToken((n) => n + 1)}>
              Retry
            </button>
          </div>
        ) : (
          <>
            <StatBar songs={songs} />
            <SongTable songs={songs} sortBy={sortBy} sortDir={sortDir} onSort={handleSort} />
          </>
        )}
      </section>
    </>
  );
}

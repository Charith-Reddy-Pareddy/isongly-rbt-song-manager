import { useEffect, useState } from 'react';
import { search, getGenres } from '../api';
import SongTable from './SongTable';
import StatBar from './StatBar';

export default function BrowsePanel() {
  const [query, setQuery] = useState('');
  const [genre, setGenre] = useState('');
  const [genres, setGenres] = useState([]);
  const [sortBy, setSortBy] = useState('title');
  const [sortDir, setSortDir] = useState('asc');

  const [songs, setSongs] = useState([]);
  const [status, setStatus] = useState('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    getGenres().then(setGenres).catch(() => {});
  }, []);

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
  }, [query, genre, sortBy, sortDir]);

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
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
        <select value={genre} onChange={(e) => setGenre(e.target.value)}>
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
          <p className="empty-state error">
            Could not reach the backend. Is it running at{' '}
            <code>{import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'}</code>?
          </p>
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

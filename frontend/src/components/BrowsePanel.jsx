import { useEffect, useState } from 'react';
import { search, getGenres } from '../api';
import SongTable from './SongTable';
import StatBar from './StatBar';

const VALID_SORT_COLUMNS = new Set(['title', 'artist', 'genre', 'year', 'bpm', 'energy']);
const RANGE_FIELDS = ['minYear', 'maxYear', 'minBpm', 'maxBpm', 'minEnergy', 'maxEnergy'];

// The library now spans ~27k songs; rendering every match as its own table
// row would bog down the DOM, so only the first page renders and a note
// tells you to narrow the search for the rest.
const MAX_DISPLAY_ROWS = 300;

function readInitialState() {
  const params = new URLSearchParams(window.location.search);
  const sortBy = params.get('sortBy');
  const state = {
    query: params.get('q') ?? '',
    genre: params.get('genre') ?? '',
    sortBy: VALID_SORT_COLUMNS.has(sortBy) ? sortBy : 'title',
    sortDir: params.get('sortDir') === 'desc' ? 'desc' : 'asc',
  };
  for (const field of RANGE_FIELDS) {
    state[field] = params.get(field) ?? '';
  }
  return state;
}

export default function BrowsePanel() {
  const [initial] = useState(readInitialState);
  const [query, setQuery] = useState(initial.query);
  const [genre, setGenre] = useState(initial.genre);
  const [genres, setGenres] = useState([]);
  const [minYear, setMinYear] = useState(initial.minYear);
  const [maxYear, setMaxYear] = useState(initial.maxYear);
  const [minBpm, setMinBpm] = useState(initial.minBpm);
  const [maxBpm, setMaxBpm] = useState(initial.maxBpm);
  const [minEnergy, setMinEnergy] = useState(initial.minEnergy);
  const [maxEnergy, setMaxEnergy] = useState(initial.maxEnergy);
  const [sortBy, setSortBy] = useState(initial.sortBy);
  const [sortDir, setSortDir] = useState(initial.sortDir);
  const [retryToken, setRetryToken] = useState(0);

  const [songs, setSongs] = useState([]);
  const [status, setStatus] = useState('loading');
  const [errorMessage, setErrorMessage] = useState('');

  const rangeValues = { minYear, maxYear, minBpm, maxBpm, minEnergy, maxEnergy };
  const hasRangeFilter = RANGE_FIELDS.some((field) => rangeValues[field] !== '');

  useEffect(() => {
    getGenres().then(setGenres).catch(() => {});
  }, []);

  // Keep the URL in sync so the current search/filter/sort is bookmarkable,
  // shareable, and survives a page refresh.
  useEffect(() => {
    const params = new URLSearchParams();
    if (query) params.set('q', query);
    if (genre) params.set('genre', genre);
    for (const field of RANGE_FIELDS) {
      if (rangeValues[field] !== '') params.set(field, rangeValues[field]);
    }
    if (sortBy !== 'title') params.set('sortBy', sortBy);
    if (sortDir !== 'asc') params.set('sortDir', sortDir);
    const queryString = params.toString();
    const newUrl = window.location.pathname + (queryString ? `?${queryString}` : '');
    window.history.replaceState(null, '', newUrl);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, genre, minYear, maxYear, minBpm, maxBpm, minEnergy, maxEnergy, sortBy, sortDir]);

  useEffect(() => {
    const controller = new AbortController();
    const timer = setTimeout(() => {
      setStatus('loading');
      setErrorMessage('');
      search({ q: query, genre, ...rangeValues, sortBy, sortDir }, controller.signal)
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
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, genre, minYear, maxYear, minBpm, maxBpm, minEnergy, maxEnergy, sortBy, sortDir, retryToken]);

  function handleSort(column) {
    if (column === sortBy) {
      setSortDir((dir) => (dir === 'asc' ? 'desc' : 'asc'));
    } else {
      setSortBy(column);
      setSortDir('asc');
    }
  }

  function clearRangeFilters() {
    setMinYear('');
    setMaxYear('');
    setMinBpm('');
    setMaxBpm('');
    setMinEnergy('');
    setMaxEnergy('');
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

      <div className="range-filters">
        <label className="range-filter">
          <span>Year</span>
          <div className="range-filter-inputs">
            <input type="number" aria-label="Minimum year" placeholder="from" value={minYear} onChange={(e) => setMinYear(e.target.value)} />
            <input type="number" aria-label="Maximum year" placeholder="to" value={maxYear} onChange={(e) => setMaxYear(e.target.value)} />
          </div>
        </label>
        <label className="range-filter">
          <span>BPM</span>
          <div className="range-filter-inputs">
            <input type="number" aria-label="Minimum BPM" placeholder="from" value={minBpm} onChange={(e) => setMinBpm(e.target.value)} />
            <input type="number" aria-label="Maximum BPM" placeholder="to" value={maxBpm} onChange={(e) => setMaxBpm(e.target.value)} />
          </div>
        </label>
        <label className="range-filter">
          <span>Energy</span>
          <div className="range-filter-inputs">
            <input type="number" aria-label="Minimum energy" placeholder="from" value={minEnergy} onChange={(e) => setMinEnergy(e.target.value)} />
            <input type="number" aria-label="Maximum energy" placeholder="to" value={maxEnergy} onChange={(e) => setMaxEnergy(e.target.value)} />
          </div>
        </label>
        {hasRangeFilter && (
          <button type="button" className="secondary range-filter-clear" onClick={clearRangeFilters}>
            Clear ranges
          </button>
        )}
      </div>

      <section className="results">
        <div className="results-header">
          <span className={`status-dot status-${status}`} />
          <span>
            {status === 'error'
              ? errorMessage
              : query || genre || hasRangeFilter
                ? `Matches for "${query || 'any title/artist'}"${genre ? ` in ${genre}` : ''}${hasRangeFilter ? ', filtered by year/BPM/energy' : ''} — click a column to sort`
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
            <SongTable songs={songs.slice(0, MAX_DISPLAY_ROWS)} sortBy={sortBy} sortDir={sortDir} onSort={handleSort} />
            {songs.length > MAX_DISPLAY_ROWS && (
              <p className="truncation-note">
                Showing the first {MAX_DISPLAY_ROWS} of {songs.length} matches — narrow your search or
                pick a genre to see more.
              </p>
            )}
          </>
        )}
      </section>
    </>
  );
}

import { useEffect, useMemo, useState } from 'react';
import { getRecentHits } from '../api';

const SORTERS = {
  lastCharted: (a, b) => b.lastCharted - a.lastCharted,
  peakPosition: (a, b) => a.peakPosition - b.peakPosition,
  weeksOnChart: (a, b) => b.weeksOnChart - a.weeksOnChart,
};

export default function RecentHitsPanel() {
  const [data, setData] = useState(null);
  const [status, setStatus] = useState('loading');
  const [errorMessage, setErrorMessage] = useState('');
  const [query, setQuery] = useState('');
  const [sortBy, setSortBy] = useState('lastCharted');

  useEffect(() => {
    getRecentHits()
      .then((result) => {
        setData(result);
        setStatus('idle');
      })
      .catch((err) => {
        setStatus('error');
        setErrorMessage(err.message || 'Something went wrong talking to the backend.');
      });
  }, []);

  const filtered = useMemo(() => {
    if (!data) return [];
    const q = query.trim().toLowerCase();
    const matches = q
      ? data.entries.filter(
          (e) => e.title.toLowerCase().includes(q) || e.performer.toLowerCase().includes(q)
        )
      : data.entries;
    return [...matches].sort(SORTERS[sortBy]);
  }, [data, query, sortBy]);

  return (
    <section className="results recent-hits">
      <h2 className="recent-hits-title">Notable hits, 2021–2026</h2>
      <p className="recent-hits-intro">
        The library above tops out around 2020 — real audio-feature data (BPM, energy, etc.) for
        newer songs isn't freely available anymore. This is a real substitute: songs that reached
        the Billboard Hot 100 top 20 between 2021 and 2026. "Last charted" is when that happened,
        not necessarily the song's original release year — a few older catalog songs (holiday
        hits, viral reissues) re-chart and appear here too.
      </p>

      <div className="results-header">
        <span className={`status-dot status-${status}`} />
        <span>
          {status === 'error'
            ? errorMessage
            : data
              ? `${filtered.length} of ${data.entries.length} shown`
              : 'Loading recent hits…'}
        </span>
      </div>

      {status === 'loading' ? (
        <p className="empty-state">Loading…</p>
      ) : status === 'error' ? (
        <p className="empty-state error">
          Could not reach the backend. Is it running at{' '}
          <code>{import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'}</code>?
        </p>
      ) : (
        <>
          <div className="browse-controls">
            <input
              type="search"
              className="search-input"
              placeholder="Search by title or artist…"
              aria-label="Search recent hits by title or artist"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
            />
            <select aria-label="Sort recent hits by" value={sortBy} onChange={(e) => setSortBy(e.target.value)}>
              <option value="lastCharted">Most recently charted</option>
              <option value="peakPosition">Highest peak position</option>
              <option value="weeksOnChart">Most weeks on chart</option>
            </select>
          </div>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th scope="col">Title</th>
                  <th scope="col">Performer</th>
                  <th scope="col">Last charted</th>
                  <th scope="col">Peak</th>
                  <th scope="col">Weeks on chart</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((entry, i) => (
                  <tr key={`${entry.title}-${entry.performer}-${i}`}>
                    <td>{entry.title}</td>
                    <td>{entry.performer}</td>
                    <td>{entry.lastCharted}</td>
                    <td>{entry.peakPosition}</td>
                    <td>{entry.weeksOnChart}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <p className="data-attribution">Source: {data.source}.</p>
        </>
      )}
    </section>
  );
}

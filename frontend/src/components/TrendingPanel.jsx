import { useEffect, useState } from 'react';
import { getTrending } from '../api';

function Movement({ entry }) {
  if (entry.lastWeek == null) {
    return <span className="movement movement-new">NEW</span>;
  }
  const change = entry.lastWeek - entry.rank;
  if (change > 0) {
    return <span className="movement movement-up">▲ {change}</span>;
  }
  if (change < 0) {
    return <span className="movement movement-down">▼ {Math.abs(change)}</span>;
  }
  return <span className="movement movement-same">—</span>;
}

export default function TrendingPanel() {
  const [data, setData] = useState(null);
  const [status, setStatus] = useState('loading');
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    setStatus('loading');
    getTrending()
      .then((result) => {
        setData(result);
        setStatus('idle');
      })
      .catch((err) => {
        setStatus('error');
        setErrorMessage(err.message || 'Something went wrong talking to the backend.');
      });
  }, []);

  return (
    <section className="results">
      <div className="results-header">
        <span className={`status-dot status-${status}`} />
        <span>
          {status === 'error'
            ? errorMessage
            : data
              ? `Billboard Hot 100 — chart week of ${data.chartWeek}`
              : 'Loading trending chart…'}
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
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th scope="col">Rank</th>
                  <th scope="col">Title</th>
                  <th scope="col">Performer</th>
                  <th scope="col">Peak</th>
                  <th scope="col">Weeks on chart</th>
                  <th scope="col">Movement</th>
                </tr>
              </thead>
              <tbody>
                {data.entries.map((entry) => (
                  <tr key={entry.rank}>
                    <td>{entry.rank}</td>
                    <td>{entry.title}</td>
                    <td>{entry.performer}</td>
                    <td>{entry.peakPosition}</td>
                    <td>{entry.weeksOnChart}</td>
                    <td>
                      <Movement entry={entry} />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <p className="data-attribution">
            Source: {data.source}. This is a real, dated chart snapshot — not our own audio-feature
            library above, and not updated live.
          </p>
        </>
      )}
    </section>
  );
}

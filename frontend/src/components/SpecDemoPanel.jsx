import { useEffect, useState } from 'react';
import { getRange, getFilteredByYear, getTopFive, reset, uploadCsv, reloadSampleData } from '../api';
import SongTable from './SongTable';

/**
 * Reproduces the original CS400 assignment's stateful query model 1:1:
 * L/G/F/D/Q from the CLI, now as REST calls. getRange sets a BPM range that
 * persists for later filter/top-five calls, exactly like the spec required.
 */
export default function SpecDemoPanel() {
  const [songs, setSongs] = useState([]);
  const [description, setDescription] = useState('Loading songs…');
  const [status, setStatus] = useState('loading');
  const [errorMessage, setErrorMessage] = useState('');

  const [minBpm, setMinBpm] = useState('');
  const [maxBpm, setMaxBpm] = useState('');
  const [year, setYear] = useState('');

  async function run(promiseFactory, describeAs) {
    setStatus('loading');
    setErrorMessage('');
    try {
      const result = await promiseFactory();
      setSongs(result);
      setDescription(describeAs(result.length));
      setStatus('idle');
    } catch (err) {
      setStatus('error');
      setErrorMessage(err.message || 'Something went wrong talking to the backend.');
    }
  }

  useEffect(() => {
    run(() => getRange('', ''), (count) => `Showing all ${count} songs, sorted by BPM`);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function handleGetRange(e) {
    e.preventDefault();
    run(
      () => getRange(minBpm, maxBpm),
      (count) => {
        const lo = minBpm || 'any';
        const hi = maxBpm || 'any';
        return `${count} song(s) with BPM between ${lo} and ${hi}`;
      }
    );
  }

  function handleSetFilter(e) {
    e.preventDefault();
    run(
      () => getFilteredByYear(year),
      (count) => `${count} song(s) released after ${year || 'any year'} (within the last BPM range)`
    );
  }

  function handleClearFilter() {
    run(() => getFilteredByYear(''), (count) => `Year filter cleared — ${count} song(s) match the current BPM range`);
  }

  function handleShowTopFive() {
    run(() => getTopFive(), (count) => `Top ${count} most energetic song(s) in the current range/filter`);
  }

  function handleReset() {
    setMinBpm('');
    setMaxBpm('');
    setYear('');
    run(() => reset(), (count) => `Range and filter cleared — showing all ${count} songs`);
  }

  function handleUpload(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setMinBpm('');
    setMaxBpm('');
    setYear('');
    run(() => uploadCsv(file), (count) => `Loaded "${file.name}" — ${count} song(s)`);
    e.target.value = '';
  }

  function handleReloadSample() {
    setMinBpm('');
    setMaxBpm('');
    setYear('');
    run(() => reloadSampleData(), (count) => `Restored the bundled sample dataset — ${count} song(s)`);
  }

  return (
    <>
      <section className="controls">
        <form className="control-card" onSubmit={handleGetRange}>
          <h2>Get songs by BPM</h2>
          <div className="field-row">
            <label>
              Min BPM
              <input type="number" value={minBpm} onChange={(e) => setMinBpm(e.target.value)} placeholder="any" />
            </label>
            <label>
              Max BPM
              <input type="number" value={maxBpm} onChange={(e) => setMaxBpm(e.target.value)} placeholder="any" />
            </label>
          </div>
          <button type="submit">Get Songs</button>
        </form>

        <form className="control-card" onSubmit={handleSetFilter}>
          <h2>Filter by year</h2>
          <label>
            Released after
            <input type="number" value={year} onChange={(e) => setYear(e.target.value)} placeholder="e.g. 2015" />
          </label>
          <div className="field-row">
            <button type="submit">Set Filter</button>
            <button type="button" className="secondary" onClick={handleClearFilter}>
              Clear Filter
            </button>
          </div>
        </form>

        <div className="control-card">
          <h2>Top 5 &amp; reset</h2>
          <div className="field-row">
            <button type="button" onClick={handleShowTopFive}>
              Show Top 5 Most Energetic
            </button>
          </div>
          <div className="field-row">
            <button type="button" className="secondary" onClick={handleReset}>
              Reset
            </button>
          </div>
          <label className="upload-label">
            Load a different CSV
            <input type="file" accept=".csv" onChange={handleUpload} />
          </label>
          <button type="button" className="secondary" onClick={handleReloadSample}>
            Restore Sample Dataset
          </button>
        </div>
      </section>

      <section className="results">
        <div className="results-header">
          <span className={`status-dot status-${status}`} />
          <span>{status === 'error' ? errorMessage : description}</span>
        </div>
        {status === 'loading' ? (
          <p className="empty-state">Loading…</p>
        ) : status === 'error' ? (
          <p className="empty-state error">
            Could not reach the backend. Is it running at{' '}
            <code>{import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'}</code>?
          </p>
        ) : (
          <SongTable songs={songs} />
        )}
      </section>
    </>
  );
}

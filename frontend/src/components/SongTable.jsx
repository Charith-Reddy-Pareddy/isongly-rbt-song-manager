import { Fragment, useState } from 'react';

const COLUMNS = [
  { key: 'title', label: 'Title' },
  { key: 'artist', label: 'Artist' },
  { key: 'genre', label: 'Genre' },
  { key: 'year', label: 'Year' },
  { key: 'bpm', label: 'BPM' },
  { key: 'energy', label: 'Energy' },
];

const DETAIL_FIELDS = [
  { key: 'durationSeconds', label: 'Duration', format: formatDuration },
  { key: 'popularity', label: 'Popularity', format: outOf100 },
  { key: 'danceability', label: 'Danceability', format: outOf100 },
  { key: 'valence', label: 'Positivity (valence)', format: outOf100 },
  { key: 'acousticness', label: 'Acousticness', format: outOf100 },
  { key: 'speechiness', label: 'Speechiness', format: outOf100 },
  { key: 'loudness', label: 'Loudness', format: (v) => `${v} dB` },
  { key: 'liveness', label: 'Liveness', format: outOf100 },
];

function formatDuration(totalSeconds) {
  if (totalSeconds == null) return '—';
  const minutes = Math.floor(totalSeconds / 60);
  const seconds = String(totalSeconds % 60).padStart(2, '0');
  return `${minutes}:${seconds}`;
}

function outOf100(value) {
  return value == null ? '—' : `${value}/100`;
}

/** Renders a BPM/energy/etc. value, or "—" for songs with no audio-feature data at all (see Song.UNKNOWN). */
function audioFeature(song, value) {
  return song.hasAudioFeatures === false ? '—' : value;
}

/**
 * Renders a table of songs. When sortBy/sortDir/onSort are all provided,
 * column headers become clickable and show a sort-direction arrow;
 * otherwise they're plain labels. Each row can be expanded to show
 * additional audio-feature details (duration, popularity, valence, etc.)
 * beyond the compact summary columns.
 */
export default function SongTable({ songs, sortBy, sortDir, onSort }) {
  const [expandedKey, setExpandedKey] = useState(null);

  if (songs.length === 0) {
    return <p className="empty-state">No songs match.</p>;
  }

  const sortable = Boolean(onSort);

  function ariaSortFor(columnKey) {
    if (!sortable || sortBy !== columnKey) return undefined;
    return sortDir === 'asc' ? 'ascending' : 'descending';
  }

  function toggle(key) {
    setExpandedKey((current) => (current === key ? null : key));
  }

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th scope="col" className="details-toggle-header">
              <span className="visually-hidden">Details</span>
            </th>
            {COLUMNS.map((col) => (
              <th key={col.key} scope="col" aria-sort={ariaSortFor(col.key)}>
                {sortable ? (
                  <button type="button" className="sort-header" onClick={() => onSort(col.key)}>
                    {col.label}
                    {sortBy === col.key && <span className="sort-arrow">{sortDir === 'asc' ? ' ▲' : ' ▼'}</span>}
                  </button>
                ) : (
                  col.label
                )}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {songs.map((song, i) => {
            const rowKey = `${song.title}-${i}`;
            const expanded = expandedKey === rowKey;
            return (
              <Fragment key={rowKey}>
                <tr className={expanded ? 'row-expanded' : undefined}>
                  <td>
                    <button
                      type="button"
                      className="details-toggle"
                      aria-expanded={expanded}
                      aria-label={`${expanded ? 'Hide' : 'Show'} details for ${song.title}`}
                      onClick={() => toggle(rowKey)}
                    >
                      {expanded ? '▾' : '▸'}
                    </button>
                  </td>
                  <td>{song.title}</td>
                  <td>{song.artist}</td>
                  <td>{song.genre}</td>
                  <td>{song.year}</td>
                  <td>{audioFeature(song, song.bpm)}</td>
                  <td>{audioFeature(song, song.energy)}</td>
                </tr>
                {expanded && (
                  <tr className="details-row">
                    <td colSpan={COLUMNS.length + 1}>
                      {song.hasAudioFeatures === false && (
                        <p className="details-unavailable">
                          No audio-feature data is available for this song (see the Trending tab for why).
                        </p>
                      )}
                      <dl className="details-grid">
                        {DETAIL_FIELDS.map((field) => (
                          <div key={field.key} className="details-item">
                            <dt>{field.label}</dt>
                            <dd>{song.hasAudioFeatures === false ? '—' : field.format(song[field.key])}</dd>
                          </div>
                        ))}
                      </dl>
                    </td>
                  </tr>
                )}
              </Fragment>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

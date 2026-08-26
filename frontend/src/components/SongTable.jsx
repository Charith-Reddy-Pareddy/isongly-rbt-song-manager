const COLUMNS = [
  { key: 'title', label: 'Title' },
  { key: 'artist', label: 'Artist' },
  { key: 'genre', label: 'Genre' },
  { key: 'year', label: 'Year' },
  { key: 'bpm', label: 'BPM' },
  { key: 'energy', label: 'Energy' },
];

/**
 * Renders a table of songs. When sortBy/sortDir/onSort are all provided,
 * column headers become clickable and show a sort-direction arrow;
 * otherwise they're plain labels.
 */
export default function SongTable({ songs, sortBy, sortDir, onSort }) {
  if (songs.length === 0) {
    return <p className="empty-state">No songs match.</p>;
  }

  const sortable = Boolean(onSort);

  function ariaSortFor(columnKey) {
    if (!sortable || sortBy !== columnKey) return undefined;
    return sortDir === 'asc' ? 'ascending' : 'descending';
  }

  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
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
          {songs.map((song, i) => (
            <tr key={`${song.title}-${i}`}>
              <td>{song.title}</td>
              <td>{song.artist}</td>
              <td>{song.genre}</td>
              <td>{song.year}</td>
              <td>{song.bpm}</td>
              <td>{song.energy}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

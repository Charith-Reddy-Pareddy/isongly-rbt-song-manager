// Selector may return null/undefined to exclude a song from the average
// (used for audio-feature stats, since some songs have no such data at all).
function average(songs, selector) {
  const values = songs.map(selector).filter((v) => v != null);
  if (values.length === 0) return '—';
  const total = values.reduce((sum, v) => sum + v, 0);
  return Math.round(total / values.length);
}

function audioFeatureOrNull(song, value) {
  return song.hasAudioFeatures === false ? null : value;
}

export default function StatBar({ songs }) {
  return (
    <div className="stat-bar">
      <div className="stat">
        <span className="stat-value">{songs.length}</span>
        <span className="stat-label">songs</span>
      </div>
      <div className="stat">
        <span className="stat-value">{average(songs, (s) => audioFeatureOrNull(s, s.bpm))}</span>
        <span className="stat-label">avg BPM</span>
      </div>
      <div className="stat">
        <span className="stat-value">{average(songs, (s) => audioFeatureOrNull(s, s.energy))}</span>
        <span className="stat-label">avg energy</span>
      </div>
      <div className="stat">
        <span className="stat-value">{average(songs, (s) => s.year)}</span>
        <span className="stat-label">avg year</span>
      </div>
    </div>
  );
}

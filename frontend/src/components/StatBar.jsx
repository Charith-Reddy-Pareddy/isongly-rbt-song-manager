function average(songs, selector) {
  if (songs.length === 0) return '—';
  const total = songs.reduce((sum, song) => sum + selector(song), 0);
  return Math.round(total / songs.length);
}

export default function StatBar({ songs }) {
  return (
    <div className="stat-bar">
      <div className="stat">
        <span className="stat-value">{songs.length}</span>
        <span className="stat-label">songs</span>
      </div>
      <div className="stat">
        <span className="stat-value">{average(songs, (s) => s.bpm)}</span>
        <span className="stat-label">avg BPM</span>
      </div>
      <div className="stat">
        <span className="stat-value">{average(songs, (s) => s.energy)}</span>
        <span className="stat-label">avg energy</span>
      </div>
      <div className="stat">
        <span className="stat-value">{average(songs, (s) => s.year)}</span>
        <span className="stat-label">avg year</span>
      </div>
    </div>
  );
}

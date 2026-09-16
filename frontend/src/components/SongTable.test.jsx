import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import SongTable from './SongTable';

const songs = [
  {
    title: 'Alpha', artist: 'A', genre: 'pop', year: 2010, bpm: 100, energy: 50,
    durationSeconds: 185, popularity: 72, danceability: 60, valence: 55,
    acousticness: 10, speechiness: 5, loudness: -4, liveness: 8,
  },
  { title: 'Beta', artist: 'B', genre: 'rock', year: 2015, bpm: 120, energy: 70 },
];

describe('SongTable', () => {
  it('shows an empty-state message when there are no songs', () => {
    render(<SongTable songs={[]} />);
    expect(screen.getByText(/no songs match/i)).toBeInTheDocument();
  });

  it('renders one row per song with every column', () => {
    render(<SongTable songs={songs} />);
    expect(screen.getByText('Alpha')).toBeInTheDocument();
    expect(screen.getByText('Beta')).toBeInTheDocument();
    expect(screen.getAllByRole('row')).toHaveLength(3); // header + 2 data rows
  });

  it('renders plain (non-clickable) headers when no sort handler is given', () => {
    render(<SongTable songs={songs} />);
    expect(screen.queryByRole('button', { name: /title/i })).not.toBeInTheDocument();
    expect(screen.getByText('Title')).toBeInTheDocument();
  });

  it('renders clickable sort headers and reports the clicked column', async () => {
    const onSort = vi.fn();
    const user = userEvent.setup();
    render(<SongTable songs={songs} sortBy="title" sortDir="asc" onSort={onSort} />);

    await user.click(screen.getByRole('button', { name: /bpm/i }));
    expect(onSort).toHaveBeenCalledWith('bpm');
  });

  it('shows an ascending or descending arrow only on the active sort column', () => {
    const { rerender } = render(
      <SongTable songs={songs} sortBy="bpm" sortDir="asc" onSort={() => {}} />
    );
    expect(screen.getByRole('button', { name: /bpm/i }).textContent).toContain('▲');
    expect(screen.getByRole('button', { name: /title/i }).textContent).not.toContain('▲');

    rerender(<SongTable songs={songs} sortBy="bpm" sortDir="desc" onSort={() => {}} />);
    expect(screen.getByRole('button', { name: /bpm/i }).textContent).toContain('▼');
  });

  it('exposes the active sort column and direction via aria-sort for screen readers', () => {
    const { rerender } = render(
      <SongTable songs={songs} sortBy="bpm" sortDir="asc" onSort={() => {}} />
    );
    expect(screen.getByRole('columnheader', { name: /bpm/i })).toHaveAttribute('aria-sort', 'ascending');
    expect(screen.getByRole('columnheader', { name: /title/i })).not.toHaveAttribute('aria-sort');

    rerender(<SongTable songs={songs} sortBy="bpm" sortDir="desc" onSort={() => {}} />);
    expect(screen.getByRole('columnheader', { name: /bpm/i })).toHaveAttribute('aria-sort', 'descending');
  });

  it('does not set aria-sort on any header when sorting is unavailable', () => {
    render(<SongTable songs={songs} />);
    for (const header of screen.getAllByRole('columnheader')) {
      expect(header).not.toHaveAttribute('aria-sort');
    }
  });

  it('expands a row to show extra details, and collapses it again on a second click', async () => {
    const user = userEvent.setup();
    render(<SongTable songs={songs} />);

    expect(screen.queryByText('3:05')).not.toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /show details for alpha/i }));
    expect(screen.getByText('3:05')).toBeInTheDocument(); // 185s formatted
    expect(screen.getByText('72/100')).toBeInTheDocument(); // popularity

    await user.click(screen.getByRole('button', { name: /hide details for alpha/i }));
    expect(screen.queryByText('3:05')).not.toBeInTheDocument();
  });

  it('only shows one row expanded at a time', async () => {
    const user = userEvent.setup();
    render(<SongTable songs={songs} />);

    await user.click(screen.getByRole('button', { name: /show details for alpha/i }));
    expect(screen.getByText('3:05')).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /show details for beta/i }));
    expect(screen.queryByText('3:05')).not.toBeInTheDocument();
  });

  it('falls back to a placeholder for missing detail fields instead of showing blank/undefined', async () => {
    const user = userEvent.setup();
    render(<SongTable songs={songs} />);

    await user.click(screen.getByRole('button', { name: /show details for beta/i }));
    expect(screen.getAllByText('—').length).toBeGreaterThan(0);
    expect(screen.queryByText('undefined')).not.toBeInTheDocument();
  });

  it('shows "—" instead of the raw -1 sentinel for a song with no audio-feature data', async () => {
    const user = userEvent.setup();
    const noFeatureSongs = [
      { title: 'New Release', artist: 'New Artist', genre: 'unknown', year: 2024, bpm: -1, energy: -1, hasAudioFeatures: false },
    ];
    render(<SongTable songs={noFeatureSongs} />);

    // Main columns show "—", not "-1"
    expect(screen.queryByText('-1')).not.toBeInTheDocument();
    expect(screen.getAllByText('—').length).toBeGreaterThan(0);

    await user.click(screen.getByRole('button', { name: /show details for new release/i }));
    expect(screen.getByText(/no audio-feature data is available/i)).toBeInTheDocument();
  });
});

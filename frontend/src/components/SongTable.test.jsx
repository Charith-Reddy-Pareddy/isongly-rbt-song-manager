import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import SongTable from './SongTable';

const songs = [
  { title: 'Alpha', artist: 'A', genre: 'pop', year: 2010, bpm: 100, energy: 50 },
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
});

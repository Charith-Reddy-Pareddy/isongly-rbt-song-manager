import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import RecentHitsPanel from './RecentHitsPanel';
import * as api from '../api';

vi.mock('../api');

const sampleResponse = {
  source: 'Billboard Hot 100, via utdata/rwd-billboard-data (MIT licensed)',
  entries: [
    { title: 'New Song', performer: 'New Artist', lastCharted: 2024, peakPosition: 3, weeksOnChart: 10 },
    { title: 'Old Classic', performer: 'Legacy Act', lastCharted: 2021, peakPosition: 1, weeksOnChart: 40 },
    { title: 'Bieber Track', performer: 'Justin Bieber', lastCharted: 2022, peakPosition: 5, weeksOnChart: 5 },
  ],
};

describe('RecentHitsPanel', () => {
  beforeEach(() => {
    api.getRecentHits.mockResolvedValue(sampleResponse);
  });

  it('loads and shows every entry with the source attribution', async () => {
    render(<RecentHitsPanel />);

    expect(await screen.findByText('New Song')).toBeInTheDocument();
    expect(screen.getByText('Old Classic')).toBeInTheDocument();
    expect(screen.getByText(/rwd-billboard-data/i)).toBeInTheDocument();
    expect(screen.getByText(/3 of 3 shown/i)).toBeInTheDocument();
  });

  it('filters by title or artist as you type', async () => {
    const user = userEvent.setup();
    render(<RecentHitsPanel />);
    await screen.findByText('New Song');

    await user.type(screen.getByLabelText(/search recent hits/i), 'bieber');

    expect(screen.getByText('Bieber Track')).toBeInTheDocument();
    expect(screen.queryByText('New Song')).not.toBeInTheDocument();
    expect(screen.getByText(/1 of 3 shown/i)).toBeInTheDocument();
  });

  it('sorts by most recently charted by default', async () => {
    render(<RecentHitsPanel />);
    await screen.findByText('New Song');

    const rows = screen.getAllByRole('row').slice(1); // drop header row
    expect(rows[0]).toHaveTextContent('New Song'); // 2024
    expect(rows[1]).toHaveTextContent('Bieber Track'); // 2022
    expect(rows[2]).toHaveTextContent('Old Classic'); // 2021
  });

  it('re-sorts by highest peak position when selected', async () => {
    const user = userEvent.setup();
    render(<RecentHitsPanel />);
    await screen.findByText('New Song');

    await user.selectOptions(screen.getByLabelText(/sort recent hits by/i), 'peakPosition');

    const rows = screen.getAllByRole('row').slice(1);
    expect(rows[0]).toHaveTextContent('Old Classic'); // peak 1
  });

  it('shows a friendly error message when the backend is unreachable', async () => {
    api.getRecentHits.mockRejectedValue(new Error('boom'));
    render(<RecentHitsPanel />);
    expect(await screen.findByText(/could not reach the backend/i)).toBeInTheDocument();
  });
});

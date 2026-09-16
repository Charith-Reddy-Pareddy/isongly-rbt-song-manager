import { render, screen } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import TrendingPanel from './TrendingPanel';
import * as api from '../api';

vi.mock('../api');

const sampleResponse = {
  chartWeek: '2026-09-19',
  source: 'Billboard Hot 100, via utdata/rwd-billboard-data (MIT licensed)',
  entries: [
    { rank: 1, title: "Choosin' Texas", performer: 'Ella Langley', lastWeek: 1, peakPosition: 1, weeksOnChart: 47 },
    { rank: 2, title: 'Boston', performer: 'Stella Lefty', lastWeek: 3, peakPosition: 2, weeksOnChart: 23 },
    { rank: 3, title: 'A Brand New Song', performer: 'Someone New', lastWeek: null, peakPosition: 3, weeksOnChart: 1 },
    { rank: 4, title: 'Falling Back', performer: 'Another Act', lastWeek: 2, peakPosition: 1, weeksOnChart: 10 },
  ],
};

describe('TrendingPanel', () => {
  beforeEach(() => {
    api.getTrending.mockResolvedValue(sampleResponse);
  });

  it('loads and shows the chart week and every entry', async () => {
    render(<TrendingPanel />);

    expect(await screen.findByText(/chart week of 2026-09-19/i)).toBeInTheDocument();
    expect(screen.getByText("Choosin' Texas")).toBeInTheDocument();
    expect(screen.getByText('Ella Langley')).toBeInTheDocument();
    expect(screen.getAllByRole('row')).toHaveLength(5); // header + 4 entries
  });

  it('shows the source attribution', async () => {
    render(<TrendingPanel />);
    await screen.findByText("Choosin' Texas");
    expect(screen.getByText(/rwd-billboard-data/i)).toBeInTheDocument();
  });

  it('marks a rank improvement, a drop, and a new entry correctly', async () => {
    render(<TrendingPanel />);
    await screen.findByText("Choosin' Texas");

    // Boston: rank 2, was 3 -> improved by 1
    expect(screen.getByText('▲ 1')).toBeInTheDocument();
    // Falling Back: rank 4, was 2 -> dropped by 2
    expect(screen.getByText('▼ 2')).toBeInTheDocument();
    // A Brand New Song: no last week -> NEW
    expect(screen.getByText('NEW')).toBeInTheDocument();
    // Choosin' Texas: rank 1, was 1 -> unchanged
    expect(screen.getByText('—')).toBeInTheDocument();
  });

  it('shows a friendly error message when the backend is unreachable', async () => {
    api.getTrending.mockRejectedValue(new Error('boom'));
    render(<TrendingPanel />);
    expect(await screen.findByText(/could not reach the backend/i)).toBeInTheDocument();
  });
});

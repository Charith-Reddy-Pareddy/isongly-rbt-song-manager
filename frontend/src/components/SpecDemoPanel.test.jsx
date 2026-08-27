import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import SpecDemoPanel from './SpecDemoPanel';
import * as api from '../api';

vi.mock('../api');

const oneSong = [{ title: 'Song A', artist: 'Artist A', genre: 'pop', year: 2020, bpm: 120, energy: 80 }];

describe('SpecDemoPanel', () => {
  beforeEach(() => {
    api.getRange.mockResolvedValue(oneSong);
    api.getFilteredByYear.mockResolvedValue(oneSong);
    api.getTopFive.mockResolvedValue(oneSong);
    api.reset.mockResolvedValue(oneSong);
    api.reloadSampleData.mockResolvedValue(oneSong);
  });

  it('loads the unfiltered song list on mount via an unbounded range query', async () => {
    render(<SpecDemoPanel />);
    expect(await screen.findByText('Song A')).toBeInTheDocument();
    expect(api.getRange).toHaveBeenCalledWith('', '');
  });

  it('submits the BPM range form and shows the result', async () => {
    const user = userEvent.setup();
    render(<SpecDemoPanel />);
    await screen.findByText('Song A');

    await user.type(screen.getByLabelText(/min bpm/i), '100');
    await user.type(screen.getByLabelText(/max bpm/i), '150');
    await user.click(screen.getByRole('button', { name: /get songs/i }));

    expect(api.getRange).toHaveBeenLastCalledWith('100', '150');
  });

  it('submits the year filter and can clear it', async () => {
    const user = userEvent.setup();
    render(<SpecDemoPanel />);
    await screen.findByText('Song A');

    await user.type(screen.getByLabelText(/released after/i), '2015');
    await user.click(screen.getByRole('button', { name: /set filter/i }));
    expect(api.getFilteredByYear).toHaveBeenLastCalledWith('2015');

    await user.click(screen.getByRole('button', { name: /clear filter/i }));
    expect(api.getFilteredByYear).toHaveBeenLastCalledWith('');
  });

  it('shows the top five most energetic songs on demand', async () => {
    const user = userEvent.setup();
    render(<SpecDemoPanel />);
    await screen.findByText('Song A');

    await user.click(screen.getByRole('button', { name: /show top 5 most energetic/i }));
    expect(api.getTopFive).toHaveBeenCalled();
  });

  it('resets range/filter state and clears the form fields', async () => {
    const user = userEvent.setup();
    render(<SpecDemoPanel />);
    await screen.findByText('Song A');

    await user.type(screen.getByLabelText(/min bpm/i), '100');
    await user.click(screen.getByRole('button', { name: /^reset$/i }));

    expect(api.reset).toHaveBeenCalled();
    expect(screen.getByLabelText(/min bpm/i)).toHaveValue(null);
  });

  it('restores the bundled sample dataset when asked', async () => {
    const user = userEvent.setup();
    render(<SpecDemoPanel />);
    await screen.findByText('Song A');

    await user.click(screen.getByRole('button', { name: /restore sample dataset/i }));

    expect(api.reloadSampleData).toHaveBeenCalled();
    expect(await screen.findByText(/restored the bundled sample dataset/i)).toBeInTheDocument();
  });

  it('shows a friendly error message when the backend is unreachable', async () => {
    api.getRange.mockRejectedValue(new Error('NetworkError when attempting to fetch resource.'));
    render(<SpecDemoPanel />);

    expect(await screen.findByText(/could not reach the backend/i)).toBeInTheDocument();
  });
});

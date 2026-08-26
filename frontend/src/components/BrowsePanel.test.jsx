import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import BrowsePanel from './BrowsePanel';
import * as api from '../api';

vi.mock('../api');

const oneSong = [{ title: 'Song A', artist: 'Artist A', genre: 'pop', year: 2020, bpm: 120, energy: 80 }];

describe('BrowsePanel', () => {
  beforeEach(() => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    api.getGenres.mockResolvedValue(['pop', 'rock']);
    api.search.mockResolvedValue(oneSong);
    window.history.pushState({}, '', '/');
  });

  it('loads genres and the unfiltered song list on mount', async () => {
    render(<BrowsePanel />);
    await vi.advanceTimersByTimeAsync(300);

    expect(api.search).toHaveBeenCalledWith(
      { q: '', genre: '', sortBy: 'title', sortDir: 'asc' },
      expect.any(AbortSignal)
    );
    expect(await screen.findByText('Song A')).toBeInTheDocument();
  });

  it('debounces rapid typing into a single search call with the final value', async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime, delay: null });
    render(<BrowsePanel />);
    await vi.advanceTimersByTimeAsync(300); // let the initial mount search settle
    api.search.mockClear();

    const input = screen.getByPlaceholderText(/search by title or artist/i);
    await user.type(input, 'bieber');
    await vi.advanceTimersByTimeAsync(300);

    const searchCallsWithQuery = api.search.mock.calls.filter(([args]) => args.q === 'bieber');
    expect(searchCallsWithQuery).toHaveLength(1);
  });

  it('toggles sort direction when the same column header is clicked twice', async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime, delay: null });
    render(<BrowsePanel />);
    await vi.advanceTimersByTimeAsync(300);
    await screen.findByText('Song A');

    await user.click(screen.getByRole('button', { name: /bpm/i }));
    await vi.advanceTimersByTimeAsync(300);
    await waitFor(() =>
      expect(api.search).toHaveBeenLastCalledWith(
        expect.objectContaining({ sortBy: 'bpm', sortDir: 'asc' }),
        expect.any(AbortSignal)
      )
    );

    await user.click(screen.getByRole('button', { name: /bpm/i }));
    await vi.advanceTimersByTimeAsync(300);
    await waitFor(() =>
      expect(api.search).toHaveBeenLastCalledWith(
        expect.objectContaining({ sortBy: 'bpm', sortDir: 'desc' }),
        expect.any(AbortSignal)
      )
    );
  });

  it('shows a friendly error message when the backend is unreachable', async () => {
    api.search.mockRejectedValue(new Error('NetworkError when attempting to fetch resource.'));
    render(<BrowsePanel />);
    await vi.advanceTimersByTimeAsync(300);

    expect(await screen.findByText(/could not reach the backend/i)).toBeInTheDocument();
  });

  it('retrying after an error re-issues the search and can recover', async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime, delay: null });
    api.search.mockRejectedValueOnce(new Error('boom'));
    render(<BrowsePanel />);
    await vi.advanceTimersByTimeAsync(300);
    await screen.findByText(/could not reach the backend/i);

    api.search.mockResolvedValueOnce(oneSong);
    await user.click(screen.getByRole('button', { name: /retry/i }));
    await vi.advanceTimersByTimeAsync(300);

    expect(await screen.findByText('Song A')).toBeInTheDocument();
  });

  it('initializes query/genre/sort from the URL on mount', async () => {
    window.history.pushState({}, '', '/?q=bieber&genre=pop&sortBy=bpm&sortDir=desc');
    render(<BrowsePanel />);
    await vi.advanceTimersByTimeAsync(300);

    expect(api.search).toHaveBeenCalledWith(
      { q: 'bieber', genre: 'pop', sortBy: 'bpm', sortDir: 'desc' },
      expect.any(AbortSignal)
    );
    expect(screen.getByPlaceholderText(/search by title or artist/i)).toHaveValue('bieber');
  });

  it('reflects the current search/genre/sort back into the URL', async () => {
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime, delay: null });
    render(<BrowsePanel />);
    await vi.advanceTimersByTimeAsync(300);

    await user.click(screen.getByRole('button', { name: /bpm/i }));
    await vi.advanceTimersByTimeAsync(300);

    const params = new URLSearchParams(window.location.search);
    expect(params.get('sortBy')).toBe('bpm');
    expect(params.has('sortDir')).toBe(false); // 'asc' is the default, omitted rather than written out
    expect(params.has('q')).toBe(false); // default/empty values are omitted, not written as ""
  });
});

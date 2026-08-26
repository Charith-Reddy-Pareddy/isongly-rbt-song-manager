import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { search, getRange, uploadCsv } from './api';

function jsonResponse(body, ok = true, status = 200) {
  return {
    ok,
    status,
    statusText: 'error',
    json: () => Promise.resolve(body),
    text: () => Promise.resolve(typeof body === 'string' ? body : JSON.stringify(body)),
  };
}

describe('api', () => {
  beforeEach(() => {
    global.fetch = vi.fn();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('search() omits empty q/genre but always sends sortBy/sortDir', async () => {
    fetch.mockResolvedValueOnce(jsonResponse([]));
    await search({ q: '', genre: '', sortBy: 'bpm', sortDir: 'desc' });

    const [url] = fetch.mock.calls[0];
    expect(url).toContain('/api/songs/search?');
    expect(url).toContain('sortBy=bpm');
    expect(url).toContain('sortDir=desc');
    expect(url).not.toContain('q=');
    expect(url).not.toContain('genre=');
  });

  it('search() includes q and genre when provided', async () => {
    fetch.mockResolvedValueOnce(jsonResponse([]));
    await search({ q: 'bieber', genre: 'pop', sortBy: 'title', sortDir: 'asc' });

    const [url] = fetch.mock.calls[0];
    expect(url).toContain('q=bieber');
    expect(url).toContain('genre=pop');
  });

  it('search() forwards an abort signal to fetch', async () => {
    fetch.mockResolvedValueOnce(jsonResponse([]));
    const controller = new AbortController();
    await search({}, controller.signal);

    const [, options] = fetch.mock.calls[0];
    expect(options.signal).toBe(controller.signal);
  });

  it('getRange() treats blank/null bounds as unbounded (omitted from the query)', async () => {
    fetch.mockResolvedValueOnce(jsonResponse([]));
    await getRange('', null);

    const [url] = fetch.mock.calls[0];
    expect(url).toBe('http://localhost:8080/api/songs/range?');
  });

  it('rejects with the response body text when the backend returns a non-OK status', async () => {
    fetch.mockResolvedValueOnce(jsonResponse('boom', false, 500));
    await expect(getRange(1, 2)).rejects.toThrow('boom');
  });

  it('uploadCsv() posts the file as multipart form data', async () => {
    fetch.mockResolvedValueOnce(jsonResponse([]));
    const file = new File(['a,b\n1,2'], 'songs.csv', { type: 'text/csv' });
    await uploadCsv(file);

    const [url, options] = fetch.mock.calls[0];
    expect(url).toContain('/api/songs/upload');
    expect(options.method).toBe('POST');
    expect(options.body).toBeInstanceOf(FormData);
  });
});

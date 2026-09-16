import { render, screen } from '@testing-library/react';
import { describe, expect, it } from 'vitest';
import StatBar from './StatBar';

describe('StatBar', () => {
  it('shows em dashes for every stat when there are no songs', () => {
    render(<StatBar songs={[]} />);
    const dashes = screen.getAllByText('—');
    expect(dashes).toHaveLength(3); // avg BPM, avg energy, avg year (count itself is 0, not a dash)
    expect(screen.getByText('0')).toBeInTheDocument();
  });

  it('computes rounded averages across the given songs', () => {
    const songs = [
      { bpm: 100, energy: 50, year: 2010 },
      { bpm: 101, energy: 51, year: 2011 },
      { bpm: 102, energy: 52, year: 2012 },
    ];
    render(<StatBar songs={songs} />);

    expect(screen.getByText('3')).toBeInTheDocument(); // count
    expect(screen.getByText('101')).toBeInTheDocument(); // avg bpm
    expect(screen.getByText('51')).toBeInTheDocument(); // avg energy
    expect(screen.getByText('2011')).toBeInTheDocument(); // avg year
  });

  it('excludes songs with no audio-feature data from avg BPM/energy, but still counts them and their year', () => {
    const songs = [
      { bpm: 100, energy: 50, year: 2010, hasAudioFeatures: true },
      { bpm: -1, energy: -1, year: 2024, hasAudioFeatures: false },
    ];
    render(<StatBar songs={songs} />);

    expect(screen.getByText('2')).toBeInTheDocument(); // count includes both
    expect(screen.getByText('100')).toBeInTheDocument(); // avg bpm ignores the unknown one
    expect(screen.getByText('50')).toBeInTheDocument(); // avg energy ignores the unknown one
    expect(screen.getByText('2017')).toBeInTheDocument(); // avg year uses both: (2010+2024)/2
  });
});

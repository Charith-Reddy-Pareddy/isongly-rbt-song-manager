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
});

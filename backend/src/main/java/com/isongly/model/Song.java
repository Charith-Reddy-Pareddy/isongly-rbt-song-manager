package com.isongly.model;

import java.util.Comparator;

/**
 * Data for a single song. A comparator can be provided as the basis for
 * compareTo; otherwise songs are ordered by title.
 */
public class Song implements Comparable<Song> {

  private final String title;
  private final String artist;
  private final String genre;
  private final int year;
  private final int bpm;
  private final int energy;
  private final int danceability;
  private final int loudness;
  private final int liveness;
  private final Comparator<Song> comparator;

  public Song(String title,
              String artist,
              String genre,
              int year,
              int bpm,
              int energy,
              int danceability,
              int loudness,
              int liveness,
              Comparator<Song> comparator) {
    this.title = title;
    this.artist = artist;
    this.genre = genre;
    this.year = year;
    this.bpm = bpm;
    this.energy = energy;
    this.danceability = danceability;
    this.loudness = loudness;
    this.liveness = liveness;
    this.comparator = comparator;
  }

  public Song(String title,
              String artist,
              String genre,
              int year,
              int bpm,
              int energy,
              int danceability,
              int loudness,
              int liveness) {
    this(title, artist, genre, year, bpm, energy, danceability, loudness, liveness, null);
  }

  public String getTitle() { return title; }
  public String getArtist() { return artist; }
  public String getGenre() { return genre; }
  public int getYear() { return year; }
  public int getBPM() { return bpm; }
  public int getEnergy() { return energy; }
  public int getDanceability() { return danceability; }
  public int getLoudness() { return loudness; }
  public int getLiveness() { return liveness; }

  @Override
  public int compareTo(Song other) {
    if (this.comparator != null) {
      return this.comparator.compare(this, other);
    }
    return this.title.compareTo(other.title);
  }
}

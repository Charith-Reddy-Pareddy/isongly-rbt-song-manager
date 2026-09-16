package com.isongly.model;

import java.util.Comparator;

/**
 * Data for a single song. A comparator can be provided as the basis for
 * compareTo; otherwise songs are ordered by title.
 */
public class Song implements Comparable<Song> {

  /**
   * Sentinel for an audio-feature field (bpm, energy, danceability, etc.)
   * whose real value isn't known -- e.g. songs added from chart data where
   * no free source of audio features exists. All such fields are always
   * unknown together (never a mix), since they come from the same source.
   */
  public static final int UNKNOWN = -1;

  private final String title;
  private final String artist;
  private final String genre;
  private final int year;
  private final int bpm;
  private final int energy;
  private final int danceability;
  private final int loudness;
  private final int liveness;
  private final int valence;
  private final int durationSeconds;
  private final int acousticness;
  private final int speechiness;
  private final int popularity;
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
              int valence,
              int durationSeconds,
              int acousticness,
              int speechiness,
              int popularity,
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
    this.valence = valence;
    this.durationSeconds = durationSeconds;
    this.acousticness = acousticness;
    this.speechiness = speechiness;
    this.popularity = popularity;
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
              int liveness,
              int valence,
              int durationSeconds,
              int acousticness,
              int speechiness,
              int popularity) {
    this(title, artist, genre, year, bpm, energy, danceability, loudness, liveness,
        valence, durationSeconds, acousticness, speechiness, popularity, null);
  }

  /** Convenience constructor for callers (tests, fixtures) that don't care about the extra audio-feature fields. */
  public Song(String title,
              String artist,
              String genre,
              int year,
              int bpm,
              int energy,
              int danceability,
              int loudness,
              int liveness) {
    this(title, artist, genre, year, bpm, energy, danceability, loudness, liveness, 0, 0, 0, 0, 0, null);
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
  /** Musical positiveness (0-100): higher sounds happier/more cheerful, lower sounds sadder/more negative. */
  public int getValence() { return valence; }
  public int getDurationSeconds() { return durationSeconds; }
  /** Confidence the track is acoustic (0-100). */
  public int getAcousticness() { return acousticness; }
  /** Presence of spoken words in the track (0-100). */
  public int getSpeechiness() { return speechiness; }
  /** Relative popularity score (0-100) as recorded in the source dataset. */
  public int getPopularity() { return popularity; }

  /** False for songs added from a source with no audio-feature data (see {@link #UNKNOWN}). */
  public boolean hasAudioFeatures() { return bpm != UNKNOWN; }

  @Override
  public int compareTo(Song other) {
    if (this.comparator != null) {
      return this.comparator.compare(this, other);
    }
    return this.title.compareTo(other.title);
  }
}

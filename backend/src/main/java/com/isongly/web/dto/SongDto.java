package com.isongly.web.dto;

import com.isongly.model.Song;

/**
 * Read-only view of a Song returned by the REST API. When
 * {@code hasAudioFeatures} is false, bpm/energy/danceability/etc. carry no
 * real data (see {@link Song#UNKNOWN}) -- the frontend should show them as
 * "unknown" rather than as the raw sentinel value.
 */
public record SongDto(
    String title,
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
    boolean hasAudioFeatures
) {
  public static SongDto from(Song song) {
    return new SongDto(
        song.getTitle(),
        song.getArtist(),
        song.getGenre(),
        song.getYear(),
        song.getBPM(),
        song.getEnergy(),
        song.getDanceability(),
        song.getLoudness(),
        song.getLiveness(),
        song.getValence(),
        song.getDurationSeconds(),
        song.getAcousticness(),
        song.getSpeechiness(),
        song.getPopularity(),
        song.hasAudioFeatures());
  }
}

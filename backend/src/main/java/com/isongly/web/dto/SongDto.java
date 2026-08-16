package com.isongly.web.dto;

import com.isongly.model.Song;

/** Read-only view of a Song returned by the REST API. */
public record SongDto(
    String title,
    String artist,
    String genre,
    int year,
    int bpm,
    int energy,
    int danceability,
    int loudness,
    int liveness
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
        song.getLiveness());
  }
}

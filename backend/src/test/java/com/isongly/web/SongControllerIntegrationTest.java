package com.isongly.web;

import com.isongly.web.dto.SongDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SongControllerIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  private String url(String path) {
    return "http://localhost:" + port + path;
  }

  @Test
  void topFiveReturnsAtMostFiveSongsOrderedByEnergyDescending() {
    SongDto[] songs = restTemplate.getForObject(url("/api/songs/top-five"), SongDto[].class);

    assertThat(songs).isNotEmpty();
    assertThat(songs.length).isLessThanOrEqualTo(5);
    for (int i = 1; i < songs.length; i++) {
      assertThat(songs[i - 1].energy()).isGreaterThanOrEqualTo(songs[i].energy());
    }
  }

  @Test
  void rangeReturnsOnlySongsWithinTheRequestedBpmBounds() {
    SongDto[] songs = restTemplate.getForObject(url("/api/songs/range?min=100&max=110"), SongDto[].class);

    assertThat(songs).isNotEmpty();
    for (SongDto song : songs) {
      assertThat(song.bpm()).isBetween(100, 110);
    }
  }

  @Test
  void filterReturnsOnlySongsReleasedAfterTheGivenYear() {
    SongDto[] songs = restTemplate.getForObject(url("/api/songs/filter?year=2018"), SongDto[].class);

    assertThat(songs).isNotEmpty();
    for (SongDto song : songs) {
      assertThat(song.year()).isGreaterThan(2018);
    }
  }

  @Test
  void resetClearsRangeAndFilterAndReturnsEveryLoadedSong() {
    restTemplate.getForObject(url("/api/songs/range?min=100&max=110"), SongDto[].class);
    SongDto[] songs = restTemplate.postForObject(url("/api/songs/reset"), null, SongDto[].class);
    assertThat(songs.length).isEqualTo(600);
  }

  @Test
  void searchMatchesOnTitleOrArtistAndIgnoresPriorRangeOrFilterState() {
    restTemplate.getForObject(url("/api/songs/range?min=100&max=110"), SongDto[].class);

    SongDto[] songs = restTemplate.getForObject(url("/api/songs/search?q=bieber"), SongDto[].class);

    assertThat(songs).isNotEmpty();
    for (SongDto song : songs) {
      assertThat((song.title() + " " + song.artist()).toLowerCase()).contains("bieber");
    }
  }

  @Test
  void searchSortsByRequestedFieldDescending() {
    SongDto[] songs = restTemplate.getForObject(url("/api/songs/search?sortBy=bpm&sortDir=desc"), SongDto[].class);

    assertThat(songs.length).isEqualTo(600);
    for (int i = 1; i < songs.length; i++) {
      assertThat(songs[i - 1].bpm()).isGreaterThanOrEqualTo(songs[i].bpm());
    }
  }

  @Test
  void genresReturnsNonEmptySortedDistinctList() {
    String[] genres = restTemplate.getForObject(url("/api/songs/genres"), String[].class);

    assertThat(genres).isNotEmpty();
    assertThat(genres).doesNotHaveDuplicates();
    String[] sorted = genres.clone();
    java.util.Arrays.sort(sorted, String.CASE_INSENSITIVE_ORDER);
    assertThat(genres).isEqualTo(sorted);
  }
}

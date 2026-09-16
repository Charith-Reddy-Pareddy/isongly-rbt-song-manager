package com.isongly.web;

import com.isongly.web.dto.SongDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SongControllerIntegrationTest {

  // 600 from the original CS400 songs.csv + 26,160 deduplicated from the merged songs-extra.csv
  private static final int TOTAL_SAMPLE_SONGS = 26760;

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
    assertThat(songs.length).isEqualTo(TOTAL_SAMPLE_SONGS);
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

    assertThat(songs.length).isEqualTo(TOTAL_SAMPLE_SONGS);
    for (int i = 1; i < songs.length; i++) {
      assertThat(songs[i - 1].bpm()).isGreaterThanOrEqualTo(songs[i].bpm());
    }
  }

  @Test
  void searchFiltersByYearBpmAndEnergyRangesTogether() {
    SongDto[] songs = restTemplate.getForObject(
        url("/api/songs/search?minYear=2015&maxYear=2019&minBpm=100&maxBpm=130&minEnergy=60"),
        SongDto[].class);

    assertThat(songs).isNotEmpty();
    for (SongDto song : songs) {
      assertThat(song.year()).isBetween(2015, 2019);
      assertThat(song.bpm()).isBetween(100, 130);
      assertThat(song.energy()).isGreaterThanOrEqualTo(60);
    }
  }

  @Test
  void searchWithOnlyAMaxEnergyExcludesHigherEnergySongs() {
    SongDto[] songs = restTemplate.getForObject(url("/api/songs/search?maxEnergy=10"), SongDto[].class);

    assertThat(songs).isNotEmpty();
    for (SongDto song : songs) {
      assertThat(song.energy()).isLessThanOrEqualTo(10);
    }
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
  void uploadingMalformedCsvReturns400AndPreservesTheExistingLibrary() {
    // Regression test: a failed upload used to clear the library before
    // parsing the replacement CSV, wiping out every bundled song.
    // @DirtiesContext resets the shared bean afterward so this destructive
    // test can't affect the other tests in this class.
    String malformedCsv = "title,artist,top genre,year,bpm,nrgy,dnce,dB,live,val,dur,acous,spch,pop\nOnly Title,Only Artist\n";

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ByteArrayResource(malformedCsv.getBytes(StandardCharsets.UTF_8)) {
      @Override
      public String getFilename() {
        return "malformed.csv";
      }
    });
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    var response = restTemplate.postForEntity(url("/api/songs/upload"), new HttpEntity<>(body, headers), String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    SongDto[] songs = restTemplate.getForObject(url("/api/songs/search"), SongDto[].class);
    assertThat(songs.length).isEqualTo(TOTAL_SAMPLE_SONGS);
  }

  @Test
  @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
  void reloadSampleRestoresTheOriginalDatasetAfterAnUpload() {
    // Regression test: uploading a replacement CSV used to be a one-way
    // door -- there was no way back to the bundled dataset short of
    // restarting the whole server.
    String replacementCsv = "title,artist,top genre,year,bpm,nrgy,dnce,dB,live,val,dur,acous,spch,pop\n"
        + "Only Song,Only Artist,pop,2020,120,80,60,-5,10,70,200,5,4,60\n";

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file", new ByteArrayResource(replacementCsv.getBytes(StandardCharsets.UTF_8)) {
      @Override
      public String getFilename() {
        return "replacement.csv";
      }
    });
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);
    restTemplate.postForEntity(url("/api/songs/upload"), new HttpEntity<>(body, headers), String.class);

    SongDto[] afterUpload = restTemplate.getForObject(url("/api/songs/search"), SongDto[].class);
    assertThat(afterUpload.length).isEqualTo(1);

    SongDto[] afterReload = restTemplate.postForObject(url("/api/songs/reload-sample"), null, SongDto[].class);
    assertThat(afterReload.length).isEqualTo(TOTAL_SAMPLE_SONGS);
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

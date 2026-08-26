package com.isongly.service;

import com.isongly.model.Song;
import com.isongly.testutil.SongTreePlaceholder;
import com.isongly.tree.IterableRedBlackTree;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SongLibraryServiceTest {

  // SongTreePlaceholder always contains, in this order:
  //   A L I E N S          year 2017  bpm 148  energy 88
  //   BO$$                 year 2015  bpm 103  energy 87
  //   Cake By The Ocean    year 2016  bpm 119  energy 75

  @Test
  void getRangeReturnsTitlesOrderedByBpmWithinBounds() {
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    List<String> actual = backend.getRange(105, 150);
    assertEquals(Arrays.asList("Cake By The Ocean", "A L I E N S"), actual);
  }

  @Test
  void getRangeWithNullBoundsIsUnbounded() {
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    List<String> actual = backend.getRange(null, null);
    assertEquals(Arrays.asList("BO$$", "Cake By The Ocean", "A L I E N S"), actual);
  }

  @Test
  void getRangeBeyondKnownBpmReturnsEmpty() {
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    assertTrue(backend.getRange(200, 300).isEmpty());
  }

  @Test
  void setFilterReturnsTitlesReleasedAfterThreshold() {
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    List<String> actual = backend.setFilter(2015);
    assertEquals(Arrays.asList("Cake By The Ocean", "A L I E N S"), actual);
  }

  @Test
  void setFilterWithNoMatchesReturnsEmpty() {
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    assertTrue(backend.setFilter(2020).isEmpty());
  }

  @Test
  void fiveMostOrdersByEnergyDescending() {
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    List<String> actual = backend.fiveMost();
    assertEquals(Arrays.asList("A L I E N S", "BO$$", "Cake By The Ocean"), actual);
  }

  @Test
  void bpmRangeSetByGetRangePersistsForLaterFiveMostCalls() {
    // Regression test: getRange used to leak its bounds nowhere, so a later
    // fiveMost() would consider every song regardless of the range the user
    // had just asked for.
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    backend.getRange(140, 150); // only "A L I E N S" (148 bpm) qualifies
    assertEquals(List.of("A L I E N S"), backend.fiveMost());
  }

  @Test
  void clearingYearFilterDoesNotAlsoClearBpmRange() {
    // Regression test: the original operator-precedence bug meant that
    // passing null to setFilter (to clear the year filter) accidentally
    // ignored the BPM range too, returning every song.
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    backend.getRange(140, 150); // narrows to "A L I E N S" only
    backend.setFilter(2020);    // no songs pass this year filter
    List<String> result = backend.setFilter(null); // clear year filter, BPM range should remain
    assertEquals(List.of("A L I E N S"), result);
  }

  @Test
  void readDataParsesColumnsIncludingGenreRegardlessOfOrder() throws IOException {
    // Regression test: the original Song constructor accidentally assigned
    // its genre field to itself, so every song's genre was silently null.
    String csv = "artist,title,bpm,top genre,year,nrgy,dnce,dB,live\n"
        + "Test Artist,Test Song,120,synthpop,2020,80,60,-5,10\n";
    SongLibraryService backend = new SongLibraryService(new IterableRedBlackTree<>());
    backend.readData(new StringReader(csv));

    Song song = backend.getSongCollection().iterator().next();
    assertEquals("Test Song", song.getTitle());
    assertEquals("Test Artist", song.getArtist());
    assertEquals("synthpop", song.getGenre());
    assertEquals(2020, song.getYear());
    assertEquals(120, song.getBPM());
  }

  @Test
  void readDataLoadsFullBundledDataset() throws IOException {
    SongLibraryService backend = new SongLibraryService(new IterableRedBlackTree<>());
    try (InputStreamReader reader =
             new InputStreamReader(getClass().getResourceAsStream("/songs.csv"))) {
      backend.readData(reader);
    }
    assertEquals(600, backend.getSongCollection().size());
  }

  @Test
  void readDataThrowsIOExceptionForEmptyContent() {
    SongLibraryService backend = new SongLibraryService(new IterableRedBlackTree<>());
    assertThrows(IOException.class, () -> backend.readData(new StringReader("")));
  }

  @Test
  void readDataThrowsCleanIOExceptionForRowWithTooFewColumns() {
    // Regression test: a data row with fewer columns than the header used to
    // throw an unhandled IndexOutOfBoundsException instead of a clean IOException.
    String csv = "title,artist,top genre,year,bpm,nrgy,dnce,dB,live\nOnly Title,Only Artist\n";
    SongLibraryService backend = new SongLibraryService(new IterableRedBlackTree<>());

    IOException ex = assertThrows(IOException.class, () -> backend.readData(new StringReader(csv)));
    assertTrue(ex.getMessage().contains("Line 2"));
  }

  @Test
  void replaceDataPreservesExistingLibraryWhenNewCsvIsMalformed() throws IOException {
    // Regression test: the upload endpoint used to clear() the library before
    // parsing the new CSV, so a malformed upload wiped out everything that
    // was previously loaded instead of leaving it in place.
    String goodCsv = "title,artist,top genre,year,bpm,nrgy,dnce,dB,live\n"
        + "Song One,Artist One,pop,2020,120,80,60,-5,10\n"
        + "Song Two,Artist Two,pop,2021,110,70,50,-6,20\n";
    String malformedCsv = "title,artist,top genre,year,bpm,nrgy,dnce,dB,live\nOnly Title,Only Artist\n";

    SongLibraryService backend = new SongLibraryService(new IterableRedBlackTree<>());
    backend.readData(new StringReader(goodCsv));
    assertEquals(2, backend.getSongCollection().size());

    assertThrows(IOException.class, () -> backend.replaceData(new StringReader(malformedCsv)));

    assertEquals(2, backend.getSongCollection().size());
  }

  @Test
  void replaceDataClearsOldLibraryWhenNewCsvIsValid() throws IOException {
    String goodCsv = "title,artist,top genre,year,bpm,nrgy,dnce,dB,live\n"
        + "Song One,Artist One,pop,2020,120,80,60,-5,10\n";
    String replacementCsv = "title,artist,top genre,year,bpm,nrgy,dnce,dB,live\n"
        + "New Song,New Artist,rock,2022,130,90,70,-4,30\n";

    SongLibraryService backend = new SongLibraryService(new IterableRedBlackTree<>());
    backend.readData(new StringReader(goodCsv));

    backend.replaceData(new StringReader(replacementCsv));

    assertEquals(1, backend.getSongCollection().size());
    assertEquals("New Song", backend.getSongCollection().iterator().next().getTitle());
  }

  @Test
  void searchMatchesTitleOrArtistCaseInsensitively() {
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    assertEquals(List.of("Cake By The Ocean"),
        titlesOf(backend.search("cake", null, "title", "asc")));
    assertEquals(List.of("Cake By The Ocean"),
        titlesOf(backend.search("dnce", null, "title", "asc"))); // matches artist "DNCE"
  }

  @Test
  void searchWithBlankQueryMatchesEverySong() {
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    assertEquals(3, backend.search("", null, "title", "asc").size());
  }

  @Test
  void searchFiltersByExactGenreCaseInsensitively() {
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    assertEquals(Arrays.asList("BO$$", "Cake By The Ocean"),
        titlesOf(backend.search("", "Dance Pop", "title", "asc")));
  }

  @Test
  void searchSortsByRequestedFieldAndDirection() {
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    assertEquals(Arrays.asList("A L I E N S", "Cake By The Ocean", "BO$$"),
        titlesOf(backend.search("", null, "bpm", "desc")));
  }

  @Test
  void searchIsIndependentOfBpmRangeAndYearFilterState() {
    // Unlike getRange/setFilter, search() browses everything regardless of
    // any range/filter previously set via the CLI-style query methods.
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    backend.getRange(140, 150);
    backend.setFilter(2020);
    assertEquals(3, backend.search("", null, "title", "asc").size());
  }

  @Test
  void getGenresReturnsDistinctGenresAlphabetically() {
    SongLibraryService backend = new SongLibraryService(new SongTreePlaceholder());
    assertEquals(Arrays.asList("dance pop", "permanent wave"), backend.getGenres());
  }

  private List<String> titlesOf(List<Song> songs) {
    return songs.stream().map(Song::getTitle).toList();
  }
}

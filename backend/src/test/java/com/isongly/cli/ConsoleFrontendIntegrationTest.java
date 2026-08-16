package com.isongly.cli;

import com.isongly.model.Song;
import com.isongly.service.BackendInterface;
import com.isongly.service.SongLibraryService;
import com.isongly.testutil.TextUiTester;
import com.isongly.tree.IterableRedBlackTree;
import com.isongly.tree.IterableSortedCollection;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * End-to-end tests driving the console UI exactly as a user would, against
 * the bundled sample dataset (src/main/resources/songs.csv).
 */
class ConsoleFrontendIntegrationTest {

  private static final String SAMPLE_CSV = "src/main/resources/songs.csv";

  private String runCli(String scriptedInput) {
    TextUiTester tester = new TextUiTester(scriptedInput);
    IterableSortedCollection<Song> tree = new IterableRedBlackTree<>();
    BackendInterface backend = new SongLibraryService(tree);
    FrontendInterface frontend = new ConsoleFrontend(new Scanner(System.in), backend);
    frontend.runCommandLoop();
    return tester.checkOutput();
  }

  @Test
  void filteringByYearShowsOnlyLaterSongs() {
    String output = runCli("L\n" + SAMPLE_CSV + "\nF\n2018\nq\n");

    List<String> expectedTitles = List.of(
        "Memories", "Beautiful People (feat. Khalid)", "No Guidance (feat. Drake)",
        "Con Calma - Remix", "Only Human", "Happier", "Takeaway",
        "I Don't Care (with Justin Bieber)", "Sucker", "Truth Hurts");

    for (String title : expectedTitles) {
      assertTrue(output.contains(title), "expected output to contain: " + title);
    }
  }

  @Test
  void gettingSongsBySpeedRangeShowsOnlyMatchingSongs() {
    String output = runCli("L\n" + SAMPLE_CSV + "\ng\n42\n80\nq\n");

    List<String> expectedTitles = List.of(
        "You Lost Me", "1+1", "Baby", "Praying", "Jar of Hearts",
        "Let Her Go", "Ferrari", "We Can't Stop");

    for (String title : expectedTitles) {
      assertTrue(output.contains(title), "expected output to contain: " + title);
    }
  }

  @Test
  void loadingAMissingFileReportsAnErrorAndKeepsRunning() {
    String output = runCli("L\nno-such-file.csv\nq\n");
    assertTrue(output.contains("Please enter a new filename"));
  }
}

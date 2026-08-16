package com.isongly.service;

import com.isongly.model.Song;
import com.isongly.tree.IterableSortedCollection;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Backend for the song library: loads songs from CSV into a sorted tree and
 * answers range/filter/top-five queries against them.
 *
 * <p>Songs are stored in the tree ordered by title (their natural ordering);
 * queries below re-sort the eligible subset by BPM or energy as needed, so
 * the choice of underlying SortedCollection only affects insert/iterate
 * performance, not query results.
 */
public class SongLibraryService implements BackendInterface {

  private static final Comparator<Song> BY_BPM = Comparator.comparingInt(Song::getBPM);
  private static final Comparator<Song> BY_ENERGY_DESC =
      Comparator.comparingInt(Song::getEnergy).reversed();

  private final IterableSortedCollection<Song> songCollection;

  private Integer currentYearThreshold = null;
  private int lowBPM = Integer.MIN_VALUE;
  private int highBPM = Integer.MAX_VALUE;

  public SongLibraryService(IterableSortedCollection<Song> tree) {
    this.songCollection = tree;
  }

  public IterableSortedCollection<Song> getSongCollection() {
    return songCollection;
  }

  /** Removes all loaded songs and resets the range/filter state to unbounded. */
  public void clear() {
    songCollection.clear();
    currentYearThreshold = null;
    lowBPM = Integer.MIN_VALUE;
    highBPM = Integer.MAX_VALUE;
  }

  @Override
  public void readData(String filename) throws IOException {
    try {
      readData(new FileReader(filename));
    } catch (FileNotFoundException e) {
      throw new IOException("CSV file is not found.", e);
    }
  }

  /** Loads songs from CSV content read from an arbitrary Reader (e.g. a classpath resource or upload). */
  public void readData(Reader source) throws IOException {
    Scanner scanner = new Scanner(source);
    try {
      if (!scanner.hasNextLine()) {
        throw new IOException("CSV file is empty or not found.");
      }
      int[] indices = findColumnIndices(scanner.nextLine().split(","));
      while (scanner.hasNextLine()) {
        List<String> fields = splitCsvLine(scanner.nextLine());
        songCollection.insert(new Song(
            fields.get(indices[0]),               // title
            fields.get(indices[1]),                // artist
            fields.get(indices[2]),                // genre
            parseIntSafe(fields.get(indices[3])),  // year
            parseIntSafe(fields.get(indices[4])),  // bpm
            parseIntSafe(fields.get(indices[5])),  // energy
            parseIntSafe(fields.get(indices[6])),  // danceability
            parseIntSafe(fields.get(indices[7])),  // loudness
            parseIntSafe(fields.get(indices[8]))   // liveness
        ));
      }
    } finally {
      scanner.close();
    }
  }

  private int parseIntSafe(String input) {
    try {
      return Integer.parseInt(input.trim());
    } catch (NumberFormatException e) {
      return 0;
    }
  }

  /** Parses one CSV line into fields, honoring quoted fields that may contain commas or escaped quotes. */
  private List<String> splitCsvLine(String line) {
    List<String> fields = new ArrayList<>();
    boolean inQuotes = false;
    StringBuilder currentField = new StringBuilder();
    int i = 0;
    while (i < line.length()) {
      char c = line.charAt(i);
      if (c == '"') {
        if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
          currentField.append('"');
          i++;
        } else {
          inQuotes = !inQuotes;
        }
      } else if (c == ',' && !inQuotes) {
        fields.add(currentField.toString());
        currentField.setLength(0);
      } else {
        currentField.append(c);
      }
      i++;
    }
    fields.add(currentField.toString());
    return fields;
  }

  /** Maps each required column name to its position in the CSV header row. */
  private int[] findColumnIndices(String[] headers) {
    int[] indices = new int[9];
    for (int i = 0; i < headers.length; i++) {
      switch (headers[i].trim().toLowerCase()) {
        case "title" -> indices[0] = i;
        case "artist" -> indices[1] = i;
        case "top genre" -> indices[2] = i;
        case "year" -> indices[3] = i;
        case "bpm" -> indices[4] = i;
        case "nrgy" -> indices[5] = i;
        case "dnce" -> indices[6] = i;
        case "db" -> indices[7] = i;
        case "live" -> indices[8] = i;
        default -> { /* ignore columns we don't need */ }
      }
    }
    return indices;
  }

  @Override
  public List<String> getRange(Integer low, Integer high) {
    this.lowBPM = (low != null) ? low : Integer.MIN_VALUE;
    this.highBPM = (high != null) ? high : Integer.MAX_VALUE;
    return titlesOf(eligibleSongsSortedByBPM());
  }

  @Override
  public List<String> setFilter(Integer threshold) {
    this.currentYearThreshold = threshold;
    return titlesOf(eligibleSongsSortedByBPM());
  }

  @Override
  public List<String> fiveMost() {
    return titlesOf(topFiveSongs());
  }

  /** Same query as {@link #getRange}, returning full Song records instead of just titles. */
  public List<Song> getRangeAsSongs(Integer low, Integer high) {
    getRange(low, high);
    return eligibleSongsSortedByBPM();
  }

  /** Same query as {@link #setFilter}, returning full Song records instead of just titles. */
  public List<Song> getFilteredSongs(Integer threshold) {
    setFilter(threshold);
    return eligibleSongsSortedByBPM();
  }

  /** Same query as {@link #fiveMost}, returning full Song records instead of just titles. */
  public List<Song> topFiveSongs() {
    List<Song> eligible = eligibleSongs();
    eligible.sort(BY_ENERGY_DESC);
    return eligible.subList(0, Math.min(5, eligible.size()));
  }

  /** Songs matching the current BPM range and year filter, sorted by BPM ascending. */
  private List<Song> eligibleSongsSortedByBPM() {
    List<Song> eligible = eligibleSongs();
    eligible.sort(BY_BPM);
    return eligible;
  }

  /** Songs matching the current BPM range and year filter, in tree (title) order. */
  private List<Song> eligibleSongs() {
    List<Song> result = new ArrayList<>();
    if (songCollection == null) {
      return result;
    }
    for (Song song : songCollection) {
      boolean withinBpmRange = song.getBPM() >= lowBPM && song.getBPM() <= highBPM;
      boolean passesYearFilter = currentYearThreshold == null || song.getYear() > currentYearThreshold;
      if (withinBpmRange && passesYearFilter) {
        result.add(song);
      }
    }
    return result;
  }

  private List<String> titlesOf(List<Song> songs) {
    List<String> titles = new ArrayList<>();
    for (Song song : songs) {
      titles.add(song.getTitle());
    }
    return titles;
  }
}

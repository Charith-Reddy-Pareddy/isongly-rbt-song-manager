package com.isongly.service;

import com.isongly.model.RecentHitEntry;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Serves an archive of songs that reached the Billboard Hot 100 top 20 at
 * some point between 2021 and 2026 -- covering the "current era" that the
 * main song library (which tops out around 2020) doesn't reach, since a
 * real, freely available dataset with full audio features for 2021+ songs
 * doesn't currently exist (see README). {@code lastCharted} is when a song
 * most recently hit that peak, not necessarily its original release year.
 *
 * <p>Data source: Billboard Hot 100, via the MIT-licensed
 * <a href="https://github.com/utdata/rwd-billboard-data">utdata/rwd-billboard-data</a>
 * archive -- the same source as {@link TrendingChartService}.
 */
public class RecentHitsService {

  private final List<RecentHitEntry> entries;

  public RecentHitsService(Reader source) throws IOException {
    this.entries = parse(source);
  }

  public List<RecentHitEntry> getEntries() {
    return entries;
  }

  private List<RecentHitEntry> parse(Reader source) throws IOException {
    List<RecentHitEntry> result = new ArrayList<>();
    Scanner scanner = new Scanner(source);
    try {
      if (!scanner.hasNextLine()) {
        throw new IOException("Recent-hits CSV is empty.");
      }
      scanner.nextLine(); // header: title,performer,last_charted,peak_pos,weeks_on_chart
      while (scanner.hasNextLine()) {
        List<String> fields = splitCsvLine(scanner.nextLine());
        result.add(new RecentHitEntry(
            fields.get(0),
            fields.get(1),
            Integer.parseInt(fields.get(2).trim()),
            Integer.parseInt(fields.get(3).trim()),
            Integer.parseInt(fields.get(4).trim())
        ));
      }
    } finally {
      scanner.close();
    }
    return result;
  }

  /** Parses one CSV line into fields, honoring quoted fields that may contain commas. */
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
}

package com.isongly.service;

import com.isongly.model.TrendingEntry;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Serves a single Billboard Hot 100 chart-week snapshot, loaded once from a
 * bundled CSV. Unlike {@link SongLibraryService}, this has no upload/replace
 * capability -- it's a fixed, dated snapshot, not a live-editable library.
 *
 * <p>Data source: Billboard Hot 100, via the MIT-licensed
 * <a href="https://github.com/utdata/rwd-billboard-data">utdata/rwd-billboard-data</a>
 * archive.
 */
public class TrendingChartService {

  private final String chartWeek;
  private final List<TrendingEntry> entries;

  public TrendingChartService(Reader source, String chartWeek) throws IOException {
    this.chartWeek = chartWeek;
    this.entries = parse(source);
  }

  public String getChartWeek() {
    return chartWeek;
  }

  public List<TrendingEntry> getEntries() {
    return entries;
  }

  private List<TrendingEntry> parse(Reader source) throws IOException {
    List<TrendingEntry> result = new ArrayList<>();
    Scanner scanner = new Scanner(source);
    try {
      if (!scanner.hasNextLine()) {
        throw new IOException("Trending chart CSV is empty.");
      }
      scanner.nextLine(); // header: rank,title,performer,last_week,peak_pos,weeks_on_chart
      while (scanner.hasNextLine()) {
        List<String> fields = splitCsvLine(scanner.nextLine());
        Integer lastWeek = fields.get(3).isBlank() ? null : Integer.parseInt(fields.get(3).trim());
        result.add(new TrendingEntry(
            Integer.parseInt(fields.get(0).trim()),
            fields.get(1),
            fields.get(2),
            lastWeek,
            Integer.parseInt(fields.get(4).trim()),
            Integer.parseInt(fields.get(5).trim())
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

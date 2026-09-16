package com.isongly.service;

import com.isongly.model.TrendingEntry;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrendingChartServiceTest {

  @Test
  void parsesEntriesInFileOrder() throws IOException {
    String csv = "rank,title,performer,last_week,peak_pos,weeks_on_chart\n"
        + "1,Choosin' Texas,Ella Langley,1,1,47\n"
        + "2,\"I Knew It, I Knew You\",Taylor Swift,3,1,14\n";

    TrendingChartService service = new TrendingChartService(new StringReader(csv), "2026-09-19");

    assertEquals("2026-09-19", service.getChartWeek());
    List<TrendingEntry> entries = service.getEntries();
    assertEquals(2, entries.size());
    assertEquals(new TrendingEntry(1, "Choosin' Texas", "Ella Langley", 1, 1, 47), entries.get(0));
    assertEquals(new TrendingEntry(2, "I Knew It, I Knew You", "Taylor Swift", 3, 1, 14), entries.get(1));
  }

  @Test
  void treatsBlankLastWeekAsNullForANewEntry() throws IOException {
    String csv = "rank,title,performer,last_week,peak_pos,weeks_on_chart\n"
        + "100,Ghost Town,Benson Boone,,100,1\n";

    TrendingChartService service = new TrendingChartService(new StringReader(csv), "2026-09-19");

    assertNull(service.getEntries().get(0).lastWeek());
  }

  @Test
  void throwsIOExceptionForEmptyContent() {
    assertThrows(IOException.class, () -> new TrendingChartService(new StringReader(""), "2026-09-19"));
  }

  @Test
  void loadsFullBundledChartSnapshot() throws IOException {
    TrendingChartService service = new TrendingChartService(
        new java.io.InputStreamReader(getClass().getResourceAsStream("/trending.csv")), "2026-09-19");

    List<TrendingEntry> entries = service.getEntries();
    assertEquals(100, entries.size());
    assertEquals(1, entries.get(0).rank());
    assertEquals(100, entries.get(99).rank());
  }
}

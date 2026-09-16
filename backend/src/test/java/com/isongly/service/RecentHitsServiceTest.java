package com.isongly.service;

import com.isongly.model.RecentHitEntry;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RecentHitsServiceTest {

  @Test
  void parsesEntries() throws IOException {
    String csv = "title,performer,last_charted,peak_pos,weeks_on_chart\n"
        + "Some Song,Some Artist,2023,4,15\n"
        + "\"A Song, With A Comma\",Another Artist,2021,1,30\n";

    RecentHitsService service = new RecentHitsService(new StringReader(csv));

    List<RecentHitEntry> entries = service.getEntries();
    assertEquals(2, entries.size());
    assertEquals(new RecentHitEntry("Some Song", "Some Artist", 2023, 4, 15), entries.get(0));
    assertEquals(new RecentHitEntry("A Song, With A Comma", "Another Artist", 2021, 1, 30), entries.get(1));
  }

  @Test
  void throwsIOExceptionForEmptyContent() {
    assertThrows(IOException.class, () -> new RecentHitsService(new StringReader("")));
  }

  @Test
  void loadsFullBundledArchiveCoveringEveryYear2021Through2026() throws IOException {
    RecentHitsService service = new RecentHitsService(
        new InputStreamReader(getClass().getResourceAsStream("/recent-hits.csv")));

    List<RecentHitEntry> entries = service.getEntries();
    assertFalse(entries.isEmpty());
    for (int year = 2021; year <= 2026; year++) {
      int y = year;
      assertTrue(entries.stream().anyMatch(e -> e.lastCharted() == y),
          "expected at least one entry last charted in " + y);
    }
  }
}

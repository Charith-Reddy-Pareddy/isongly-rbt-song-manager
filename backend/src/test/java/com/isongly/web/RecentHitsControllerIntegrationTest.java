package com.isongly.web;

import com.isongly.web.dto.RecentHitsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RecentHitsControllerIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void recentHitsReturnsRealChartedSongsCovering2021Through2026WithSourceAttribution() {
    RecentHitsResponse response =
        restTemplate.getForObject("http://localhost:" + port + "/api/recent-hits", RecentHitsResponse.class);

    assertThat(response.entries()).isNotEmpty();
    assertThat(response.source()).contains("Billboard");
    for (int year = 2021; year <= 2026; year++) {
      int expectedYear = year;
      assertThat(response.entries()).anyMatch(e -> e.lastCharted() == expectedYear);
    }
  }
}

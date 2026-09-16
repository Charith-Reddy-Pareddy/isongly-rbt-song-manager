package com.isongly.web;

import com.isongly.web.dto.TrendingResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TrendingControllerIntegrationTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void trendingReturnsAFullHot100SnapshotWithSourceAttribution() {
    TrendingResponse response =
        restTemplate.getForObject("http://localhost:" + port + "/api/trending", TrendingResponse.class);

    assertThat(response.entries()).hasSize(100);
    assertThat(response.chartWeek()).isNotBlank();
    assertThat(response.source()).contains("Billboard");
    assertThat(response.entries().get(0).rank()).isEqualTo(1);
  }
}

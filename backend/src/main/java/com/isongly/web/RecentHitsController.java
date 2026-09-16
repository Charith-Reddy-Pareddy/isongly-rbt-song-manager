package com.isongly.web;

import com.isongly.service.RecentHitsService;
import com.isongly.web.dto.RecentHitsResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Serves the 2021-2026 Billboard recent-hits archive (see RecentHitsService for the data source). */
@RestController
@RequestMapping("/api/recent-hits")
public class RecentHitsController {

  private static final String SOURCE =
      "Billboard Hot 100, via utdata/rwd-billboard-data (MIT licensed)";

  private final RecentHitsService recentHitsService;

  public RecentHitsController(RecentHitsService recentHitsService) {
    this.recentHitsService = recentHitsService;
  }

  @GetMapping
  public RecentHitsResponse recentHits() {
    return new RecentHitsResponse(SOURCE, recentHitsService.getEntries());
  }
}

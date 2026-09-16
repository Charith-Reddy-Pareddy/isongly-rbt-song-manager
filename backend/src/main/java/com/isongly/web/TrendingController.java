package com.isongly.web;

import com.isongly.service.TrendingChartService;
import com.isongly.web.dto.TrendingResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Serves a fixed Billboard Hot 100 chart-week snapshot (see TrendingChartService for the data source). */
@RestController
@RequestMapping("/api/trending")
public class TrendingController {

  private static final String SOURCE =
      "Billboard Hot 100, via utdata/rwd-billboard-data (MIT licensed)";

  private final TrendingChartService trendingChartService;

  public TrendingController(TrendingChartService trendingChartService) {
    this.trendingChartService = trendingChartService;
  }

  @GetMapping
  public TrendingResponse trending() {
    return new TrendingResponse(
        trendingChartService.getChartWeek(),
        SOURCE,
        trendingChartService.getEntries());
  }
}

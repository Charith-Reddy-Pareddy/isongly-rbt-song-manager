package com.isongly.web.dto;

import com.isongly.model.TrendingEntry;

import java.util.List;

/** Response for GET /api/trending: a dated Billboard Hot 100 snapshot plus its source. */
public record TrendingResponse(
    String chartWeek,
    String source,
    List<TrendingEntry> entries
) {
}

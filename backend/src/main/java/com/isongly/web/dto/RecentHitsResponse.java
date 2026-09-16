package com.isongly.web.dto;

import com.isongly.model.RecentHitEntry;

import java.util.List;

/** Response for GET /api/recent-hits: real 2021-2026 Billboard chart entries, plus their source. */
public record RecentHitsResponse(
    String source,
    List<RecentHitEntry> entries
) {
}

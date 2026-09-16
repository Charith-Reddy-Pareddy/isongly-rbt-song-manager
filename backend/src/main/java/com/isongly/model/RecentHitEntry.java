package com.isongly.model;

/**
 * One entry in the recent-hits archive: a song that reached the Billboard
 * Hot 100 top 20 at some point between 2021 and 2026. {@code lastCharted} is
 * the most recent year it appeared at that peak -- not necessarily the
 * song's original release year, since older catalog songs sometimes
 * re-chart (holiday songs, viral trends, anniversary reissues).
 */
public record RecentHitEntry(
    String title,
    String performer,
    int lastCharted,
    int peakPosition,
    int weeksOnChart
) {
}

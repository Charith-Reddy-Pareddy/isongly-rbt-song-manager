package com.isongly.model;

/**
 * One row of a Billboard Hot 100 chart snapshot: current rank, the song and
 * performer, last week's rank (absent for a new entry), peak position ever
 * reached, and total weeks on the chart.
 */
public record TrendingEntry(
    int rank,
    String title,
    String performer,
    Integer lastWeek,
    int peakPosition,
    int weeksOnChart
) {
}

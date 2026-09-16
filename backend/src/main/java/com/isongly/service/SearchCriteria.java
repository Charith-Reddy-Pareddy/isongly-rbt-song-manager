package com.isongly.service;

/**
 * Filters for {@link SongLibraryService#search}. Every bound is optional
 * (null means unbounded on that side); range checks are inclusive.
 */
public record SearchCriteria(
    String query,
    String genre,
    Integer minYear,
    Integer maxYear,
    Integer minBpm,
    Integer maxBpm,
    Integer minEnergy,
    Integer maxEnergy,
    String sortBy,
    String sortDir
) {
  public static SearchCriteria unbounded(String query, String genre, String sortBy, String sortDir) {
    return new SearchCriteria(query, genre, null, null, null, null, null, null, sortBy, sortDir);
  }
}

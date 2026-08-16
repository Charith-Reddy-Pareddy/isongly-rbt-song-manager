package com.isongly.service;

import java.io.IOException;
import java.util.List;

/**
 * Defines the query operations exposed by the song library: loading a CSV,
 * retrieving songs within a BPM range, filtering by release year, and
 * finding the most energetic songs matching the current range/filter.
 */
public interface BackendInterface {

  /**
   * Loads songs from the CSV file referenced by filename and inserts them
   * into the underlying collection. Column order is not assumed, but the
   * expected headers (title, artist, top genre, year, bpm, nrgy, dnce, dB,
   * live) must be present.
   *
   * @param filename the name of the csv file to load data from
   * @throws IOException when there is trouble finding/reading the file
   */
  void readData(String filename) throws IOException;

  /**
   * Retrieves titles of songs whose BPM falls within [low, high], ordered by
   * BPM ascending. This range is remembered for later calls to setFilter and
   * fiveMost. A null bound is treated as unbounded.
   *
   * @param low  minimum BPM of songs in the returned list, or null for unbounded
   * @param high maximum BPM of songs in the returned list, or null for unbounded
   * @return titles of matching songs ordered by BPM, or an empty list if none match
   */
  List<String> getRange(Integer low, Integer high);

  /**
   * Retrieves titles of songs released after threshold, restricted to the
   * BPM range set by the most recent call to getRange (or unbounded if
   * getRange has not been called). This threshold is remembered for later
   * calls to getRange and fiveMost. A null threshold clears the year filter.
   *
   * @param threshold only include songs with a year greater than this, or null to clear the filter
   * @return titles of matching songs ordered by BPM, or an empty list if none match
   */
  List<String> setFilter(Integer threshold);

  /**
   * Retrieves up to five titles of the most energetic songs that satisfy the
   * BPM range set by getRange and the year filter set by setFilter.
   *
   * @return up to five titles ordered by energy descending, or an empty list if none match
   */
  List<String> fiveMost();
}

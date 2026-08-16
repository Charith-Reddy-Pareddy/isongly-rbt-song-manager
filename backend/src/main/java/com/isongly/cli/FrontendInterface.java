package com.isongly.cli;

/**
 * Defines the text-based user interface for iSongly: a command loop offering
 * load, get-songs-by-range, set-filter, and show-top-five operations.
 */
public interface FrontendInterface {

  /** Repeatedly prompts for and runs commands until the user quits. */
  void runCommandLoop();

  /** Prints the menu of available commands: L, G, F, D, Q (case-insensitive). */
  void displayMainMenu();

  /** [L]oad a CSV file of songs, retrying on read errors. */
  void loadFile();

  /** [G]et song titles within a user-specified BPM range. */
  void getSongs();

  /** [F]ilter songs to those released after a user-specified year. */
  void setFilter();

  /** [D]isplay up to five of the most energetic songs matching the current range/filter. */
  void displayTopFive();
}

package com.isongly.web;

import com.isongly.service.SongLibraryService;
import com.isongly.web.dto.SongDto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * REST API over the iSongly song library: BPM-range and year queries, and
 * the top-five-most-energetic query, mirroring the original CLI's commands.
 */
@RestController
@RequestMapping("/api/songs")
public class SongController {

  private final SongLibraryService songLibraryService;
  private final Resource sampleData;

  public SongController(
      SongLibraryService songLibraryService,
      @Value("classpath:songs.csv") Resource sampleData) {
    this.songLibraryService = songLibraryService;
    this.sampleData = sampleData;
  }

  /** [G]et songs by Speed: titles ordered by BPM within [min, max] (either bound optional). */
  @GetMapping("/range")
  public List<SongDto> getRange(
      @RequestParam(required = false) Integer min,
      @RequestParam(required = false) Integer max) {
    return songLibraryService.getRangeAsSongs(min, max).stream().map(SongDto::from).toList();
  }

  /** [F]ilter Songs by Year: titles released after year, within the last-set BPM range. */
  @GetMapping("/filter")
  public List<SongDto> filterByYear(@RequestParam(required = false) Integer year) {
    return songLibraryService.getFilteredSongs(year).stream().map(SongDto::from).toList();
  }

  /** [D]isplay five most Energetic: up to five songs matching the current range/filter. */
  @GetMapping("/top-five")
  public List<SongDto> topFive() {
    return songLibraryService.topFiveSongs().stream().map(SongDto::from).toList();
  }

  /** Free-text search over title/artist, optionally narrowed by genre and sorted — independent of range/filter state. */
  @GetMapping("/search")
  public List<SongDto> search(
      @RequestParam(required = false, defaultValue = "") String q,
      @RequestParam(required = false) String genre,
      @RequestParam(required = false, defaultValue = "title") String sortBy,
      @RequestParam(required = false, defaultValue = "asc") String sortDir) {
    return songLibraryService.search(q, genre, sortBy, sortDir).stream().map(SongDto::from).toList();
  }

  /** Every distinct genre among the currently loaded songs, for populating a filter dropdown. */
  @GetMapping("/genres")
  public List<String> genres() {
    return songLibraryService.getGenres();
  }

  /** Clears the BPM range and year filter, returning every loaded song. */
  @PostMapping("/reset")
  public List<SongDto> reset() {
    songLibraryService.setFilter(null);
    return songLibraryService.getRangeAsSongs(null, null).stream().map(SongDto::from).toList();
  }

  /** [L]oad Song File: replaces the current library with songs parsed from an uploaded CSV. */
  @PostMapping("/upload")
  public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) {
    if (file.isEmpty()) {
      return ResponseEntity.badRequest().body("Uploaded file is empty.");
    }
    try (var reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
      songLibraryService.replaceData(reader);
      return ResponseEntity.ok(
          songLibraryService.getRangeAsSongs(null, null).stream().map(SongDto::from).toList());
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body("Could not read CSV file: " + e.getMessage());
    }
  }

  /**
   * Restores the bundled sample dataset, discarding whatever was loaded by a
   * previous upload. Without this, there was no way back to the original
   * 600-song dataset short of restarting the server.
   */
  @PostMapping("/reload-sample")
  public ResponseEntity<?> reloadSample() {
    try (var reader = new InputStreamReader(sampleData.getInputStream(), StandardCharsets.UTF_8)) {
      songLibraryService.replaceData(reader);
      return ResponseEntity.ok(
          songLibraryService.getRangeAsSongs(null, null).stream().map(SongDto::from).toList());
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Could not reload the sample dataset: " + e.getMessage());
    }
  }
}

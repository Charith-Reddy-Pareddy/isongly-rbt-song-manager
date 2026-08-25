package com.isongly.web;

import com.isongly.model.Song;
import com.isongly.service.SongLibraryService;
import com.isongly.tree.IterableRedBlackTree;
import com.isongly.tree.IterableSortedCollection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@SpringBootApplication
public class IsonglyApplication {

  public static void main(String[] args) {
    SpringApplication.run(IsonglyApplication.class, args);
  }

  @Bean
  public SongLibraryService songLibraryService() {
    IterableSortedCollection<Song> tree = new IterableRedBlackTree<>();
    return new SongLibraryService(tree);
  }

  /** Loads the bundled sample dataset into the tree once on startup. */
  @Bean
  public CommandLineRunner loadSampleData(
      SongLibraryService songLibraryService,
      @Value("classpath:songs.csv") Resource sampleData) {
    return args -> {
      try (var reader = new InputStreamReader(sampleData.getInputStream(), StandardCharsets.UTF_8)) {
        songLibraryService.readData(reader);
      } catch (IOException e) {
        System.err.println("Could not load bundled sample dataset: " + e.getMessage());
      }
    };
  }

  /**
   * Origins may include glob patterns (e.g. "http://localhost:*") so local
   * dev keeps working no matter which port Vite ends up on; the deployed
   * frontend origin is set to an exact value via APP_CORS_ALLOWED_ORIGINS.
   */
  @Bean
  public WebMvcConfigurer corsConfigurer(
      @Value("${app.cors.allowed-origins}") String[] allowedOriginPatterns) {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns(allowedOriginPatterns)
            .allowedMethods("GET", "POST");
      }
    };
  }
}

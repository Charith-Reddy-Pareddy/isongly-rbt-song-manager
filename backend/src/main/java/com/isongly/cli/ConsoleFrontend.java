package com.isongly.cli;

import com.isongly.service.BackendInterface;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

/** A text-based console UI for iSongly, driven by a Scanner and a BackendInterface. */
public class ConsoleFrontend implements FrontendInterface {

  private final Scanner userInput;
  private final BackendInterface backend;

  public ConsoleFrontend(Scanner in, BackendInterface backend) {
    this.userInput = in;
    this.backend = backend;
  }

  @Override
  public void runCommandLoop() {
    boolean quitLoop = false;

    while (!quitLoop) {
      displayMainMenu();

      if (this.userInput.hasNext()) {
        String entry = userInput.nextLine().trim().toLowerCase();
        switch (entry) {
          case "l" -> loadFile();
          case "g" -> getSongs();
          case "f" -> setFilter();
          case "d" -> displayTopFive();
          case "q" -> {
            System.out.println("Exiting loop....");
            quitLoop = true;
          }
          default -> System.out.println("Enter a valid input from the menu:");
        }
      } else {
        System.out.println("Please provide an input or exit: ");
      }
    }
  }

  @Override
  public void displayMainMenu() {
    System.out.println("Display Menu Options: ");
    System.out.println("Enter 'L' to load a file");
    System.out.println("Enter 'G' to get songs");
    System.out.println("Enter 'F' to set a filter");
    System.out.println("Enter 'D' to display the top five songs");
    System.out.println("Enter 'Q' to quit menu");
    System.out.println("Enter your choice:");
  }

  @Override
  public void loadFile() {
    System.out.println("Enter the fileName: ");
    String fileName = this.userInput.nextLine();
    try {
      backend.readData(fileName);
    } catch (IOException e) {
      System.out.println("Error reading this file caused" + e.getMessage() + ". Please enter a new filename");
    } catch (NoSuchElementException e) {
      System.out.println("No such file exists. Please enter a new filename.");
    }
  }

  @Override
  public void getSongs() {
    System.out.println("Get songs by speed...");
    Integer minimumSpeed = readOptionalInt("Please enter the minimum Speed (or press Enter to skip): ", Integer.MIN_VALUE);
    Integer maximumSpeed = readOptionalInt("Please enter the maximum Speed (or press Enter to skip): ", Integer.MAX_VALUE);

    List<String> songList = backend.getRange(minimumSpeed, maximumSpeed);
    System.out.println("Songs in the speed range are:");
    if (songList.isEmpty()) {
      System.out.println("No songs found in the specified speed range.");
    } else {
      songList.forEach(System.out::println);
    }
  }

  private Integer readOptionalInt(String prompt, int defaultWhenBlank) {
    while (true) {
      System.out.println(prompt);
      String input = userInput.nextLine().trim();
      if (input.isBlank()) {
        return defaultWhenBlank;
      }
      try {
        return Integer.parseInt(input);
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Please enter a valid number.");
      }
    }
  }

  @Override
  public void setFilter() {
    while (true) {
      System.out.println("[F]ilter Songs by Year");
      System.out.println("Enter a filter threshold (year) or enter 'null' to remove the filter:");
      String input = userInput.nextLine().trim();

      if (input.equalsIgnoreCase("null")) {
        backend.setFilter(null);
        System.out.println("Filter cleared. All songs will now be shown.");
        return;
      }
      try {
        int userThreshold = Integer.parseInt(input);
        List<String> filteredSongs = backend.setFilter(userThreshold);
        System.out.println("Filter is set. Songs released after " + userThreshold + " year:");
        if (filteredSongs.isEmpty()) {
          System.out.println("No songs found after " + userThreshold + ".");
        } else {
          filteredSongs.forEach(System.out::println);
        }
        return;
      } catch (NumberFormatException e) {
        System.out.println("Invalid input. Please enter a valid number for the year.");
      }
    }
  }

  @Override
  public void displayTopFive() {
    List<String> topFive = backend.fiveMost();
    if (topFive.isEmpty()) {
      System.out.println("There are no songs in the set speed range and more recent than the "
          + "specified year. Please adjust your current range or filter settings.");
    } else {
      topFive.forEach(System.out::println);
      System.out.println();
    }
  }
}

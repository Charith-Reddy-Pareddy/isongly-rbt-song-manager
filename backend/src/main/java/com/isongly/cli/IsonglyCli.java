package com.isongly.cli;

import com.isongly.model.Song;
import com.isongly.service.BackendInterface;
import com.isongly.service.SongLibraryService;
import com.isongly.tree.IterableRedBlackTree;
import com.isongly.tree.IterableSortedCollection;

import java.util.Scanner;

/** Entry point for the original text-based iSongly CLI. */
public class IsonglyCli {

  public static void main(String[] args) {
    IterableSortedCollection<Song> tree = new IterableRedBlackTree<>();
    BackendInterface backend = new SongLibraryService(tree);
    Scanner in = new Scanner(System.in);
    FrontendInterface frontend = new ConsoleFrontend(in, backend);

    System.out.println("Welcome to iSongly");
    System.out.println("==================");

    frontend.runCommandLoop();

    System.out.println();
    System.out.println("====================");
    System.out.println("Thanks, and Goodbye.");
  }
}

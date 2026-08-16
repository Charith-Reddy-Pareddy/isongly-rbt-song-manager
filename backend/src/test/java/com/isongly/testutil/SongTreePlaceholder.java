package com.isongly.testutil;

import com.isongly.model.Song;
import com.isongly.tree.IterableSortedCollection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * A fixed, non-tree stand-in for IterableSortedCollection&lt;Song&gt; used to
 * test SongLibraryService with deterministic data, independent of the real
 * RedBlackTree implementation.
 */
public class SongTreePlaceholder implements IterableSortedCollection<Song> {

  private Song lastAddedSong = null;

  private final List<Song> songs = Arrays.asList(
      new Song("A L I E N S", "Coldplay", "permanent wave", 2017, 148, 88, 43, -5, 21),
      new Song("BO$$", "Fifth Harmony", "dance pop", 2015, 103, 87, 81, -5, 5),
      new Song("Cake By The Ocean", "DNCE", "dance pop", 2016, 119, 75, 77, -5, 4));

  private Comparable<Song> min = null;
  private Comparable<Song> max = null;

  @Override
  public void insert(Song data) throws NullPointerException {
    if (data == null) {
      throw new NullPointerException("Data cannot be null");
    }
    this.lastAddedSong = data;
  }

  @Override
  public boolean contains(Comparable<Song> find) {
    return lastAddedSong != null && find.compareTo(lastAddedSong) == 0;
  }

  @Override
  public int size() {
    return lastAddedSong == null ? 3 : 4;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("cannot call on placeholder");
  }

  @Override
  public void setIteratorMin(Comparable<Song> min) { this.min = min; }

  @Override
  public void setIteratorMax(Comparable<Song> max) { this.max = max; }

  @Override
  public Iterator<Song> iterator() {
    List<Song> tmp = new ArrayList<>(songs);
    if (lastAddedSong != null) {
      tmp.add(lastAddedSong);
    }
    tmp.removeIf(song ->
        (min != null && min.compareTo(song) > 0) || (max != null && max.compareTo(song) < 0));
    return tmp.iterator();
  }
}

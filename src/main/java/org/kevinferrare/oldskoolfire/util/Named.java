package org.kevinferrare.oldskoolfire.util;

/**
 * Interface for objects that have a name.
 * Records with a name() component automatically implement this.
 */
public interface Named {
  String name();

  /**
   * Find the index of an element by name (case-insensitive).
   *
   * @return index if found, 0 otherwise
   */
  static <T extends Named> int findIndex(T[] items, String name) {
    for (int i = 0; i < items.length; i++) {
      if (items[i].name().equalsIgnoreCase(name)) {
        return i;
      }
    }
    return 0;
  }
}

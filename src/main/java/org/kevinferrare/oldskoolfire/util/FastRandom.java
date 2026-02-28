package org.kevinferrare.oldskoolfire.util;

/**
 * Fast, non-thread-safe pseudo-random number generator using the XorShift algorithm.
 *
 * <p>Much faster than {@link java.util.Random} since it avoids synchronization and uses
 * only three XOR/shift operations per generated value. Not cryptographically secure.
 *
 */
public class FastRandom {

  private long seed;

  public FastRandom() {
    this(System.nanoTime() ^ Thread.currentThread().threadId());
  }

  public FastRandom(long seed) {
    // seed must never be 0
    this.seed = seed != 0 ? seed : 1;
  }

  public long nextLong() {
    seed ^= (seed << 21);
    seed ^= (seed >>> 35);
    seed ^= (seed << 4);
    return seed;
  }

  public boolean nextBoolean() {
    return (nextLong() & 1) == 0;
  }
}

package org.kevinferrare.oldskoolfire.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FastRandomTest {

  @Test
  void sameSeedProducesSameSequence() {
    FastRandom a = new FastRandom(42);
    FastRandom b = new FastRandom(42);
    for (int i = 0; i < 100; i++) {
      assertEquals(a.nextLong(), b.nextLong());
    }
  }

  @Test
  void differentSeedsProduceDifferentSequences() {
    FastRandom a = new FastRandom(42);
    FastRandom b = new FastRandom(99);
    boolean anyDifference = false;
    for (int i = 0; i < 100; i++) {
      if (a.nextLong() != b.nextLong()) {
        anyDifference = true;
        break;
      }
    }
    assertTrue(anyDifference);
  }

  @Test
  void zeroSeedDoesNotGetStuck() {
    FastRandom rng = new FastRandom(0);
    long first = rng.nextLong();
    boolean varied = false;
    for (int i = 0; i < 100; i++) {
      if (rng.nextLong() != first) {
        varied = true;
        break;
      }
    }
    assertTrue(varied, "RNG with seed 0 should not produce only identical values");
  }

  @Test
  void nextBooleanProducesBothValues() {
    FastRandom rng = new FastRandom(42);
    boolean seenTrue = false, seenFalse = false;
    for (int i = 0; i < 100; i++) {
      if (rng.nextBoolean()) {
        seenTrue = true;
      } else {
        seenFalse = true;
      }
      if (seenTrue && seenFalse) {
        break;
      }
    }
    assertTrue(seenTrue && seenFalse, "nextBoolean should produce both true and false");
  }

  @Test
  void nextLongProducesVariedValues() {
    FastRandom rng = new FastRandom(42);
    long first = rng.nextLong();
    boolean varied = false;
    for (int i = 0; i < 1000; i++) {
      if (rng.nextLong() != first) {
        varied = true;
        break;
      }
    }
    assertTrue(varied, "nextLong should produce varied values");
  }
}

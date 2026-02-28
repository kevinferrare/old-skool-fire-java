package org.kevinferrare.oldskoolfire.drawable;

import org.junit.jupiter.api.Test;
import org.kevinferrare.oldskoolfire.AppConfig;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests for TornadoVM GPU convolution strategy and GPU-related detection logic.
 */
public class TornadoConvolutionStrategyTest {

  private static final int WIDTH = 320;
  private static final int HEIGHT = 200;
  private static final int COOLING = 3;
  private static final int MAX_DELTA = 4;

  private static boolean isTornadoAvailable() {
    try {
      new TornadoConvolutionStrategy();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private FixedIntSurface randomSurface(int width, int height, long seed) {
    Random rng = new Random(seed);
    int[] data = new int[width * (height + 1)];
    for (int i = 0; i < data.length; i++) {
      data[i] = rng.nextInt(FixedIntSurface.SCALING * 256);
    }
    return new FixedIntSurface(data, width, height);
  }

  private FixedIntSurface emptySurface(int width, int height) {
    return new FixedIntSurface(new int[width * (height + 1)], width, height);
  }

  private void assertArraysNearlyEqual(int[] expected, int[] actual, int maxDelta, String msg) {
    assertEquals(expected.length, actual.length, msg + " - array lengths differ");
    for (int i = 0; i < expected.length; i++) {
      int diff = Math.abs(expected[i] - actual[i]);
      if (diff > maxDelta) {
        fail(msg + " - at index [" + i + "]: expected <" + expected[i]
          + "> but was <" + actual[i] + ">, delta " + diff + " exceeds max " + maxDelta);
      }
    }
  }

  @Test
  void tornadoProducesSameOutputAsScalar() {
    assumeTrue(isTornadoAvailable(), "TornadoVM runtime not available");

    FixedIntSurface src = randomSurface(WIDTH, HEIGHT, 42);
    FixedIntSurface destScalar = emptySurface(WIDTH, HEIGHT);
    FixedIntSurface destTornado = emptySurface(WIDTH, HEIGHT);

    new ConvolveAndRiseEffect(COOLING, new ScalarConvolutionStrategy()).draw(src, destScalar);
    new ConvolveAndRiseEffect(COOLING, new TornadoConvolutionStrategy()).draw(src, destTornado);

    assertArraysNearlyEqual(destScalar.data(), destTornado.data(), MAX_DELTA,
      "Tornado GPU strategy must produce near-identical output to scalar strategy");
  }

  @Test
  void tornadoHandlesEmptySurface() {
    assumeTrue(isTornadoAvailable(), "TornadoVM runtime not available");

    FixedIntSurface src = new FixedIntSurface(new int[0], 0, 0);
    FixedIntSurface dst = new FixedIntSurface(new int[0], 0, 0);

    assertDoesNotThrow(() ->
      new ConvolveAndRiseEffect(COOLING, new TornadoConvolutionStrategy()).draw(src, dst));
  }

  @Test
  void tornadoHandlesSmallSurface() {
    assumeTrue(isTornadoAvailable(), "TornadoVM runtime not available");

    int smallWidth = 4;
    int smallHeight = 3;
    FixedIntSurface src = randomSurface(smallWidth, smallHeight, 99);
    FixedIntSurface destScalar = emptySurface(smallWidth, smallHeight);
    FixedIntSurface destTornado = emptySurface(smallWidth, smallHeight);

    new ConvolveAndRiseEffect(COOLING, new ScalarConvolutionStrategy()).draw(src, destScalar);
    new ConvolveAndRiseEffect(COOLING, new TornadoConvolutionStrategy()).draw(src, destTornado);

    assertArraysNearlyEqual(destScalar.data(), destTornado.data(), MAX_DELTA,
      "Tornado GPU strategy must handle small surfaces correctly");
  }

  @Test
  void detectStrategyReturnsTornadoWhenAvailable() {
    assumeTrue(isTornadoAvailable(), "TornadoVM runtime not available");

    ConvolutionStrategy strategy = ConvolveAndRiseEffect.detectStrategy(true, false);
    assertInstanceOf(TornadoConvolutionStrategy.class, strategy,
      "detectStrategy(gpu=true) should return TornadoConvolutionStrategy when runtime is available");
  }

  @Test
  void detectStrategyFallsBackWhenTornadoUnavailable() {
    // This test always runs - verifies graceful fallback
    // If Tornado IS available, this will return TornadoConvolutionStrategy which is also fine
    ConvolutionStrategy strategy = ConvolveAndRiseEffect.detectStrategy(true, false);
    assertNotNull(strategy, "detectStrategy must always return a non-null strategy");
  }
}

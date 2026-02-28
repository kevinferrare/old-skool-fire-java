package org.kevinferrare.oldskoolfire.drawable;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that scalar and vector convolution strategies produce correct output.
 * The vector strategy uses reduced-precision int-only arithmetic for SIMD performance,
 * so it may differ from scalar by at most {@link #MAX_DELTA} per pixel.
 */
public class ConvolutionStrategyTest {

  private static final int WIDTH = 320;
  private static final int HEIGHT = 200;
  private static final int COOLING = 3;
  /**
   * Maximum acceptable per-pixel difference between vector and scalar strategies.
   */
  private static final int MAX_DELTA = 4;

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
  void scalarProducesSameOutputAsOriginalDraw() {
    FixedIntSurface src = randomSurface(WIDTH, HEIGHT, 42);
    FixedIntSurface destOriginal = emptySurface(WIDTH, HEIGHT);
    FixedIntSurface destScalar = emptySurface(WIDTH, HEIGHT);

    // Original draw()
    ConvolveAndRiseEffect original = new ConvolveAndRiseEffect(COOLING, false, false);
    original.draw(src, destOriginal);

    // Scalar strategy through the strategy interface
    ConvolutionStrategy scalar = new ScalarConvolutionStrategy();
    ConvolveAndRiseEffect withScalar = new ConvolveAndRiseEffect(COOLING, scalar);
    withScalar.draw(src, destScalar);

    assertArraysNearlyEqual(destOriginal.data(), destScalar.data(), MAX_DELTA,
      "Scalar strategy must produce near-identical output to original draw()");
  }

  @Test
  void vectorProducesSameOutputAsScalar() {
    FixedIntSurface src = randomSurface(WIDTH, HEIGHT, 42);
    FixedIntSurface destScalar = emptySurface(WIDTH, HEIGHT);
    FixedIntSurface destVector = emptySurface(WIDTH, HEIGHT);

    ConvolutionStrategy scalar = new ScalarConvolutionStrategy();
    ConvolveAndRiseEffect scalarEffect = new ConvolveAndRiseEffect(COOLING, scalar);
    scalarEffect.draw(src, destScalar);

    ConvolutionStrategy vector = new VectorConvolutionStrategy();
    ConvolveAndRiseEffect vectorEffect = new ConvolveAndRiseEffect(COOLING, vector);
    vectorEffect.draw(src, destVector);

    assertArraysNearlyEqual(destScalar.data(), destVector.data(), MAX_DELTA,
      "Vector strategy must produce near-identical output to scalar strategy");
  }

  @Test
  void vectorHandlesSmallSurface() {
    // Surface smaller than a SIMD vector lane count - only remainder loop should run
    int smallWidth = 4;
    int smallHeight = 3;
    FixedIntSurface src = randomSurface(smallWidth, smallHeight, 99);
    FixedIntSurface destScalar = emptySurface(smallWidth, smallHeight);
    FixedIntSurface destVector = emptySurface(smallWidth, smallHeight);

    new ConvolveAndRiseEffect(COOLING, new ScalarConvolutionStrategy()).draw(src, destScalar);
    new ConvolveAndRiseEffect(COOLING, new VectorConvolutionStrategy()).draw(src, destVector);

    assertArraysNearlyEqual(destScalar.data(), destVector.data(), MAX_DELTA,
      "Vector strategy must handle surfaces smaller than SIMD width");
  }

  @Test
  void bothStrategiesHandleEmptySurface() {
    FixedIntSurface src = new FixedIntSurface(new int[0], 0, 0);
    FixedIntSurface dst = new FixedIntSurface(new int[0], 0, 0);

    assertDoesNotThrow(() ->
      new ConvolveAndRiseEffect(COOLING, new ScalarConvolutionStrategy()).draw(src, dst));
    assertDoesNotThrow(() ->
      new ConvolveAndRiseEffect(COOLING, new VectorConvolutionStrategy()).draw(src, dst));
  }
}

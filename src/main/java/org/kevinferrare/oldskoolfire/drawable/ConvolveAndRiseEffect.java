package org.kevinferrare.oldskoolfire.drawable;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Applies a convolution filter that makes the fire rise and cool/grow.
 * The cooling parameter controls fire behavior:
 * - Positive values: fire cools down (dissipates)
 * - Negative values: fire grows (intensifies)
 * - Zero: fire maintains constant intensity
 * <p>
 * Uses reciprocal multiplication instead of division for performance.
 * Delegates the inner loop to a {@link ConvolutionStrategy} (scalar or SIMD).
 */
@Slf4j
public class ConvolveAndRiseEffect {

  static final int RECIPROCAL_SHIFT = 14;

  @Getter
  private int cooling = 3;  // default cooling value
  private int reciprocal;  // pre-computed reciprocal for fast division
  private final ConvolutionStrategy strategy;

  public String getStrategyName() {
    return strategy.name();
  }

  public ConvolveAndRiseEffect(int cooling, boolean gpu, boolean noVectorApi) {
    this(cooling, detectStrategy(gpu, noVectorApi));
  }

  public ConvolveAndRiseEffect(int cooling, ConvolutionStrategy strategy) {
    this.strategy = strategy;
    log.info("Convolution strategy: {}", strategy.getClass().getSimpleName());
    setCooling(cooling);
  }

  static ConvolutionStrategy detectStrategy(boolean gpu, boolean noVectorApi) {
    if (gpu) {
      try {
        return new TornadoConvolutionStrategy();
      } catch (Exception e) {
        log.info("TornadoVM GPU not available ({}: {}), falling back to CPU",
          e.getClass().getSimpleName(), e.getMessage(), e);
      }
    }
    if (!noVectorApi) {
      try {
        Class.forName("jdk.incubator.vector.IntVector");
        log.info("Vector API detected, using SIMD convolution");
        return new VectorConvolutionStrategy();
      } catch (ClassNotFoundException e) {
        log.info("Vector API not available, using scalar convolution");
      }
    }
    return new ScalarConvolutionStrategy();
  }

  public void setCooling(int cooling) {
    if (cooling < 0) {
      return;
    }
    this.cooling = cooling;
    // Pre-compute optimized division:
    // - divisor is fixed, we can compute it now
    // - use reciprocal method to do division via multiplication (a/b = a*1/b), we need to scale the fraction so that too much details are not lost
    int divisor = 5 * FixedIntSurface.SCALING + cooling;
    this.reciprocal = (1 << (RECIPROCAL_SHIFT + FixedIntSurface.SCALING_SHIFT)) / divisor;
  }

  /**
   * Applies the convolution filter reading from source and writing to dest.
   *
   * @param source the surface to read from (previous frame)
   * @param dest   the surface to write to (current frame)
   */
  public void draw(FixedIntSurface source, FixedIntSurface dest) {
    int width = source.width();
    int widthPlus1 = width + 1;
    int widthMinus1 = width - 1;
    int widthTimes2 = 2 * width;
    int[] srcData = source.data();
    int[] dstData = dest.data();
    int recip = this.reciprocal;

    // Compute safe iteration count - JIT can eliminate bounds checks when
    // it can prove all array accesses are within bounds for the entire loop
    int srcLimit = srcData.length - widthTimes2;
    int dstLimit = dstData.length;
    int end = Math.min(srcLimit, dstLimit);

    if (end <= 0) {
      return;
    }

    strategy.convolve(srcData, dstData, end, width, widthMinus1, widthPlus1, widthTimes2, recip);
  }
}

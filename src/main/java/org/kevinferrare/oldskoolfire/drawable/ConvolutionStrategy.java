package org.kevinferrare.oldskoolfire.drawable;

/**
 * Strategy for the inner convolution loop.
 * Implementations process pixels from srcData into dstData using the
 * pre-computed reciprocal for fast division.
 */
@FunctionalInterface
public interface ConvolutionStrategy {
  /**
   * Convolves pixels from source to destination.
   *
   * @param srcData     source pixel array (previous frame)
   * @param dstData     destination pixel array (current frame)
   * @param end         number of pixels to process
   * @param width       surface width
   * @param widthMinus1 width - 1
   * @param widthPlus1  width + 1
   * @param widthTimes2 width * 2
   * @param reciprocal  pre-computed reciprocal for fast division
   */
  void convolve(int[] srcData, int[] dstData, int end,
                int width, int widthMinus1, int widthPlus1,
                int widthTimes2, int reciprocal);

  /**
   * Human-readable name of the algorithm used.
   */
  default String name() {
    return getClass().getSimpleName();
  }
}

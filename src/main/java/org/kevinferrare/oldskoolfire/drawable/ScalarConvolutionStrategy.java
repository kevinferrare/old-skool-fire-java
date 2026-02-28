package org.kevinferrare.oldskoolfire.drawable;

/**
 * Scalar convolution using a 4x-unrolled loop for instruction-level parallelism.
 */
public class ScalarConvolutionStrategy implements ConvolutionStrategy {

  private static final int RECIPROCAL_SHIFT = ConvolveAndRiseEffect.RECIPROCAL_SHIFT;

  @Override
  public String name() {
    return "Scalar";
  }

  @Override
  public void convolve(int[] srcData, int[] dstData, int end,
                       int width, int widthMinus1, int widthPlus1,
                       int widthTimes2, int reciprocal) {
    int i = 0;
    int unrolledEnd = end - 3;
    while (i < unrolledEnd) {
      dstData[i] = convolvePixel(srcData, i, width, widthMinus1, widthPlus1, widthTimes2, reciprocal);
      dstData[i + 1] = convolvePixel(srcData, i + 1, width, widthMinus1, widthPlus1, widthTimes2, reciprocal);
      dstData[i + 2] = convolvePixel(srcData, i + 2, width, widthMinus1, widthPlus1, widthTimes2, reciprocal);
      dstData[i + 3] = convolvePixel(srcData, i + 3, width, widthMinus1, widthPlus1, widthTimes2, reciprocal);
      i += 4;
    }
    while (i < end) {
      dstData[i] = convolvePixel(srcData, i, width, widthMinus1, widthPlus1, widthTimes2, reciprocal);
      i++;
    }
  }

  static int convolvePixel(int[] src, int i, int width, int widthMinus1,
                           int widthPlus1, int widthTimes2, int recip) {
    int sum = src[i]
      + src[i + width]
      + src[i + widthMinus1]
      + src[i + widthPlus1]
      + src[i + widthTimes2];
    return (sum * recip) >> RECIPROCAL_SHIFT;
  }
}

package org.kevinferrare.oldskoolfire.drawable;

import jdk.incubator.vector.IntVector;
import jdk.incubator.vector.VectorOperators;
import jdk.incubator.vector.VectorSpecies;

/**
 * SIMD convolution using the Java Vector API.
 */
public class VectorConvolutionStrategy implements ConvolutionStrategy {

  @Override
  public String name() {
    return "SIMD";
  }

  private static final int INT_SHIFT = ConvolveAndRiseEffect.RECIPROCAL_SHIFT;
  private static final VectorSpecies<Integer> SPECIES = IntVector.SPECIES_PREFERRED;
  private static final int LANE_COUNT = SPECIES.length();

  @Override
  public void convolve(int[] srcData, int[] dstData, int end,
                       int width, int widthMinus1, int widthPlus1,
                       int widthTimes2, int reciprocal) {
    IntVector recipVec = IntVector.broadcast(SPECIES, reciprocal);
    int vectorEnd = end - (end % LANE_COUNT);

    for (int i = 0; i < vectorEnd; i += LANE_COUNT) {
      IntVector v0 = IntVector.fromArray(SPECIES, srcData, i);
      IntVector v1 = IntVector.fromArray(SPECIES, srcData, i + width);
      IntVector v2 = IntVector.fromArray(SPECIES, srcData, i + widthMinus1);
      IntVector v3 = IntVector.fromArray(SPECIES, srcData, i + widthPlus1);
      IntVector v4 = IntVector.fromArray(SPECIES, srcData, i + widthTimes2);

      v0.add(v1).add(v2).add(v3).add(v4)
        .mul(recipVec)
        .lanewise(VectorOperators.ASHR, INT_SHIFT)
        .intoArray(dstData, i);
    }

    // Scalar remainder
    for (int i = vectorEnd; i < end; i++) {
      dstData[i] = ScalarConvolutionStrategy.convolvePixel(
        srcData, i, width, widthMinus1, widthPlus1, widthTimes2, reciprocal);
    }
  }
}

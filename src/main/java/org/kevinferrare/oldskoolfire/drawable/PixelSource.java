package org.kevinferrare.oldskoolfire.drawable;

/**
 * A functional interface for providing pixel values.
 * Allows decoupling the color/randomization logic from the geometric drawing logic.
 */
@FunctionalInterface
public interface PixelSource {
  int getPixel();
}

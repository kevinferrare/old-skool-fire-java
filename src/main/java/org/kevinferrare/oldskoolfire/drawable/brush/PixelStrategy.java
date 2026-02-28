package org.kevinferrare.oldskoolfire.drawable.brush;

/**
 * Strategy interface for pixel value generation.
 * Implementations define different pixel rendering behaviors (fixed, flickering, pulsing, etc.).
 */
@FunctionalInterface
public interface PixelStrategy {
  /**
   * Gets the next pixel value according to this strategy.
   *
   * @return the pixel intensity value
   */
  int getPixel();
}

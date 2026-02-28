package org.kevinferrare.oldskoolfire.drawable.brush;

/**
 * A stateless pixel strategy that always returns the same constant value.
 */
public class FixedPixelStrategy implements PixelStrategy {
  private final int pixelValue;

  /**
   * Creates a fixed pixel strategy with the specified value.
   *
   * @param pixelValue the constant pixel value to return
   */
  public FixedPixelStrategy(int pixelValue) {
    this.pixelValue = pixelValue;
  }

  @Override
  public int getPixel() {
    return pixelValue;
  }
}

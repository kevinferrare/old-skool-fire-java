package org.kevinferrare.oldskoolfire.drawable;

/**
 * A graphics surface for the fire effect, encapsulating the pixel buffer and dimensions.
 * Uses fixed-point arithmetic for performance.
 */
public record FixedIntSurface(int[] data, int width, int height) {
  public static final int SCALING_SHIFT = 8;
  public static final int SCALING = 1 << SCALING_SHIFT;

  public void setPixel(int index, int value) {
    data[index] = value;
  }

  public int getDataLength() {
    return data.length;
  }

  public void drawPixel(int imageDataIndex, PixelSource pixelSource) {
    if (imageDataIndex < 0 || imageDataIndex >= getDataLength()) {
      return;
    }
    setPixel(imageDataIndex, pixelSource.getPixel());
  }
}

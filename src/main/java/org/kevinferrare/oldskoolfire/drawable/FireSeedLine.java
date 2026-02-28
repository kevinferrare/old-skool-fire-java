package org.kevinferrare.oldskoolfire.drawable;

import org.kevinferrare.oldskoolfire.drawable.brush.Material;

/**
 * Draws random fire seed pixels at the bottom of the surface.
 * Uses Material for pixel value generation with flicker effect.
 */
public class FireSeedLine {

  private final Material material;

  public FireSeedLine() {
    // Create material with fixed value 127 and flicker value 255
    this.material = new Material(127, 255);
    this.material.setRandomPixel(true);
  }

  /**
   * Draws random pixels along the bottom line of the surface.
   *
   * @param g the surface to draw on
   */
  public void draw(FixedIntSurface g) {
    int width = g.width();
    int height = g.height();
    PixelSource pixelSource = material.getPixelSource();

    for (int x = 0; x < width; x++) {
      // Randomize bottom line
      g.drawPixel(x + width * height, pixelSource);
    }
  }

  /**
   * Returns the material used for pixel generation.
   * Allows external configuration of pixel values and flicker mode.
   *
   * @return the material instance
   */
  public Material getMaterial() {
    return material;
  }
}

package org.kevinferrare.oldskoolfire.palette;

import org.kevinferrare.oldskoolfire.drawable.FixedIntSurface;
import org.kevinferrare.oldskoolfire.util.Named;

import java.awt.*;

public record Palette(String name, int[] indexToRGB) implements Named {

  /**
   * Converts fixed-point fire intensities to RGB pixels using the palette.
   */
  public void apply(FixedIntSurface source, int[] destination) {
    int[] data = source.data();
    // Local copy avoids repeated field loads inside the loop
    int[] lookupTable = this.indexToRGB;
    // Power-of-2 mask: clamps shifted value to valid palette index range
    int mask = lookupTable.length - 1;
    for (int i = 0; i < destination.length; i++) {
      destination[i] = lookupTable[(data[i] >> FixedIntSurface.SCALING_SHIFT) & mask];
    }
  }

  static int rgbToInteger(int r, int g, int b) {
    return (r << 16) | (g << 8) | b;
  }

  static int hsbToInteger(float hue, float saturation, float brightness) {
    return Color.HSBtoRGB(hue, saturation, brightness);
  }
}

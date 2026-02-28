package org.kevinferrare.oldskoolfire.palette;

import org.junit.jupiter.api.Test;
import org.kevinferrare.oldskoolfire.drawable.FixedIntSurface;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PaletteTest {

  private final Palette[] palettes = {
    PaletteFactory.createFire(),
    PaletteFactory.createEvil(),
    PaletteFactory.createRockbox(),
    PaletteFactory.createBlueFire(),
    PaletteFactory.createMatrix(),
    PaletteFactory.createPurple(),
    PaletteFactory.createGrayscale()
  };

  @Test
  void everyPaletteHas256Entries() {
    for (Palette p : palettes) {
      assertEquals(256, p.indexToRGB().length, p.name() + " should have 256 entries");
    }
  }

  @Test
  void rgbToIntegerBlack() {
    assertEquals(0x000000, Palette.rgbToInteger(0, 0, 0));
  }

  @Test
  void rgbToIntegerWhite() {
    assertEquals(0xFFFFFF, Palette.rgbToInteger(255, 255, 255));
  }

  @Test
  void hsbToIntegerRed() {
    int result = Palette.hsbToInteger(0f, 1f, 1f);
    // Java Color.HSBtoRGB returns full ARGB with alpha=0xFF
    assertEquals(0xFFFF0000, result);
  }

  @Test
  void applyMapsZeroSurfaceToBlack() {
    int width = 10;
    int height = 5;
    FixedIntSurface surface = new FixedIntSurface(new int[width * (height + 1)], width, height);
    int[] destination = new int[width * height];

    PaletteFactory.createFire().apply(surface, destination);

    int blackRgb = PaletteFactory.createFire().indexToRGB()[0];
    for (int pixel : destination) {
      assertEquals(blackRgb, pixel);
    }
  }

  @Test
  void applyMapsKnownValueToCorrectPaletteIndex() {
    Palette palette = PaletteFactory.createGrayscale();
    int width = 1;
    int height = 1;
    // Set pixel to index 128 (in fixed-point: 128 << SCALING_SHIFT)
    int[] data = new int[width * (height + 1)];
    data[0] = 128 << FixedIntSurface.SCALING_SHIFT;
    FixedIntSurface surface = new FixedIntSurface(data, width, height);
    int[] destination = new int[1];

    palette.apply(surface, destination);

    assertEquals(palette.indexToRGB()[128], destination[0]);
  }
}

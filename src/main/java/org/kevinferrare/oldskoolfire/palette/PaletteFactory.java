package org.kevinferrare.oldskoolfire.palette;

/**
 * Factory for creating color palettes used by the fire effect.
 */
public final class PaletteFactory {

  private PaletteFactory() {
  }

  /**
   * Fire palette: black -> red -> yellow -> white with HSB interpolation.
   */
  public static Palette createFire() {
    int[] rgb = new int[256];
    for (int i = 0; i < 256; i++) {
      // Normalize i, 0<n<1
      float n = i / (float) 256;
      // From Red to green (0~160/360). Green will never appear because saturation is
      // at max after 1/5 of the items
      float hue = n * (160.0F / 360.0F);
      // After 1/2 of the colors, saturation is 0
      float saturation = 1.0F - Math.min(1.0F, 2 * n);
      // After 1/5 of the items, brightness is at max
      float brightness = Math.min(1.0F, 5 * n);
      rgb[i] = Palette.hsbToInteger(hue, saturation, brightness);
    }
    return new Palette("fire", rgb);
  }

  /**
   * Evil palette: black -> red -> yellow -> white with sharp RGB transitions.
   */
  public static Palette createEvil() {
    int[] rgb = new int[256];
    for (int i = 0; i < 64; i++) {
      // Black to red
      rgb[i] = Palette.rgbToInteger(i * 4, 0, 0);
      // Red to yellow
      rgb[i + 64] = Palette.rgbToInteger(255, i * 4, 0);
      // Yellow to white
      rgb[i + 128] = Palette.rgbToInteger(255, 255, i * 4);
      // White
      rgb[i + 192] = Palette.rgbToInteger(255, 255, 255);
    }
    return new Palette("evil", rgb);
  }

  /**
   * Rockbox palette: black -> blue -> red -> yellow -> white.
   */
  public static Palette createRockbox() {
    int[] rgb = new int[256];
    int n = 32;
    for (int i = 0; i < n; i++) {
      // black to blue
      rgb[i] = Palette.rgbToInteger(0, 0, 2 * n + 2 * i);
      // blue to red
      rgb[i + n] = Palette.rgbToInteger(8 * i, 0, 4 * n + 2 * i);
      // red to yellow
      rgb[i + 2 * n] = Palette.rgbToInteger(255, 8 * i, 0);
      // yellow to white
      rgb[i + 3 * n] = Palette.rgbToInteger(255, 255, 4 * i);
      rgb[i + 4 * n] = Palette.rgbToInteger(255, 255, 2 * n + 4 * i);
      rgb[i + 5 * n] = Palette.rgbToInteger(255, 255, 4 * n + 4 * i);
      rgb[i + 6 * n] = Palette.rgbToInteger(255, 255, 5 * n + i);
      rgb[i + 7 * n] = Palette.rgbToInteger(255, 255, 6 * n + i);
    }
    return new Palette("rockbox", rgb);
  }

  /**
   * Blue fire palette: black -> dark blue -> blue -> cyan -> white.
   */
  public static Palette createBlueFire() {
    int[] rgb = new int[256];
    for (int i = 0; i < 256; i++) {
      float n = i / 256.0F;
      // Hue in blue range (0.5 to 0.7)
      float hue = 0.55F + n * 0.15F;
      // Saturation decreases toward white
      float saturation = 1.0F - Math.min(1.0F, 2.0F * n);
      // Brightness increases from black
      float brightness = Math.min(1.0F, 4.0F * n);
      rgb[i] = Palette.hsbToInteger(hue, saturation, brightness);
    }
    return new Palette("blue fire", rgb);
  }

  /**
   * Matrix palette: black -> dark green -> green -> light green.
   */
  public static Palette createMatrix() {
    int[] rgb = new int[256];
    for (int i = 0; i < 256; i++) {
      float n = i / 256.0F;
      // Green hue (1/3)
      float hue = 1.0F / 3.0F;
      // Full saturation, fading to less saturated at high values
      float saturation = 1.0F - 0.5F * n;
      // Brightness increases
      float brightness = n;
      rgb[i] = Palette.hsbToInteger(hue, saturation, brightness);
    }
    return new Palette("matrix", rgb);
  }

  /**
   * Purple palette: black -> purple -> magenta -> pink -> white.
   */
  public static Palette createPurple() {
    int[] rgb = new int[256];
    for (int i = 0; i < 256; i++) {
      float n = i / 256.0F;
      // Hue in purple/magenta range (0.75 to 0.9)
      float hue = 0.8F - n * 0.1F;
      // Saturation decreases toward white
      float saturation = 1.0F - Math.min(1.0F, 1.5F * n);
      // Brightness increases from black
      float brightness = Math.min(1.0F, 3.0F * n);
      rgb[i] = Palette.hsbToInteger(hue, saturation, brightness);
    }
    return new Palette("purple", rgb);
  }

  /**
   * Grayscale palette: black -> white.
   */
  public static Palette createGrayscale() {
    int[] rgb = new int[256];
    for (int i = 0; i < 256; i++) {
      rgb[i] = Palette.rgbToInteger(i, i, i);
    }
    return new Palette("grayscale", rgb);
  }
}

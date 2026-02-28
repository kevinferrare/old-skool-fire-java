package org.kevinferrare.oldskoolfire.drawable.brush;

import org.kevinferrare.oldskoolfire.drawable.FixedIntSurface;
import org.kevinferrare.oldskoolfire.drawable.PixelSource;
import org.kevinferrare.oldskoolfire.drawable.threed.Mesh;
import org.kevinferrare.oldskoolfire.drawable.threed.Vec3;

/**
 * Encapsulates the visual appearance (pixel intensity and behavior) of an object.
 * Uses the Strategy Pattern to delegate pixel generation to pluggable PixelStrategy implementations.
 */
public class Material {
  // Reference total line length for intensity normalization (cube ~= 12 * 2 = 24)
  private static final double REFERENCE_TOTAL_LENGTH = 24.0;
  // Base intensity values calibrated for reference
  private static final int BASE_FIXED_VALUE = 128;
  private static final int BASE_FLICKER_VALUE = 192;

  private final PixelStrategy fixedStrategy;
  private final PixelStrategy flickerStrategy;
  private PixelStrategy currentStrategy;

  /**
   * Creates a material with both fixed and flicker pixel modes.
   *
   * @param pixelValue   the constant pixel value for fixed mode
   * @param flickerValue the maximum pixel value for flicker mode
   */
  public Material(int pixelValue, int flickerValue) {
    this.fixedStrategy = new FixedPixelStrategy(pixelValue << FixedIntSurface.SCALING_SHIFT);
    this.flickerStrategy = new FlickerPixelStrategy(flickerValue << FixedIntSurface.SCALING_SHIFT);
    this.currentStrategy = fixedStrategy;  // default to fixed mode
  }

  /**
   * Creates a material with intensity automatically computed from mesh geometry.
   * Shapes with more total line length get lower intensity to maintain consistent visual brightness.
   *
   * @param mesh the mesh to compute intensity for
   * @return material with appropriate intensity values
   */
  public static Material forMesh(Mesh mesh) {
    double totalLength = computeTotalLineLength(mesh);
    // Scale intensity inversely with total line length
    double ratio = Math.sqrt(REFERENCE_TOTAL_LENGTH / Math.max(1.0, totalLength));
    int fixedValue = clamp((int) (BASE_FIXED_VALUE * ratio), 32, 255);
    int flickerValue = clamp((int) (BASE_FLICKER_VALUE * ratio), 48, 255);
    return new Material(fixedValue, flickerValue);
  }

  private static double computeTotalLineLength(Mesh mesh) {
    Vec3[] vertices = mesh.vertices();
    int[] lines = mesh.lines();
    double total = 0.0;
    for (int i = 0; i < lines.length; i += 2) {
      Vec3 v0 = vertices[lines[i]];
      Vec3 v1 = vertices[lines[i + 1]];
      double dx = v1.x() - v0.x();
      double dy = v1.y() - v0.y();
      double dz = v1.z() - v0.z();
      total += Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
    return total;
  }

  private static int clamp(int value, int min, int max) {
    return Math.max(min, Math.min(max, value));
  }

  /**
   * Toggles between fixed and random flicker pixel modes.
   *
   * @param randomPixel true for flicker mode, false for fixed mode
   */
  public void setRandomPixel(boolean randomPixel) {
    this.currentStrategy = randomPixel ? flickerStrategy : fixedStrategy;
  }

  /**
   * Returns whether the material is currently in flicker mode.
   *
   * @return true if in flicker mode, false if in fixed mode
   */
  public boolean isRandomPixel() {
    return currentStrategy == flickerStrategy;
  }

  /**
   * Returns the pixel source for rendering.
   *
   * @return a PixelSource that delegates to the current strategy
   */
  public PixelSource getPixelSource() {
    return currentStrategy::getPixel;
  }
}

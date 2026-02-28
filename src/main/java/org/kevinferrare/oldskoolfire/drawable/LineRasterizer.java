package org.kevinferrare.oldskoolfire.drawable;

/**
 * Rasterizes lines onto a pixel surface using optimized integer algorithms. Combines Cohen-Sutherland line clipping
 * with Bresenham's line algorithm: 1. Cohen-Sutherland clips the line to the viewport bounds 2. Bresenham rasterizes
 * the clipped segment using integer-only arithmetic This approach is faster than floating-point DDA and correctly
 * handles lines that extend beyond the viewport.
 */
public class LineRasterizer implements LineDrawer {

  // Region code bit flags for Cohen-Sutherland clipping
  private static final int INSIDE = 0;
  private static final int LEFT = 1;  // bit 0
  private static final int RIGHT = 1 << 1;  // bit 1
  private static final int BELOW = 1 << 2;  // bit 2
  private static final int ABOVE = 1 << 3;  // bit 3

  private final FixedIntSurface surface;
  private final int width;
  private final int yMin;
  private final int yMax;

  /**
   * Creates a line rasterizer for the given surface.
   *
   * @param surface the target surface to draw on
   * @param yMin    minimum Y coordinate (inclusive) - allows for guard lines
   * @param yMax    maximum Y coordinate (inclusive) - allows for guard lines
   */
  public LineRasterizer(FixedIntSurface surface, int yMin, int yMax) {
    this.surface = surface;
    this.width = surface.width();
    this.yMin = yMin;
    this.yMax = yMax;
  }

  /**
   * Creates a line rasterizer using default guard lines. Row 0 and height-1 are reserved, drawing occurs in [1,
   * height-2].
   */
  public LineRasterizer(FixedIntSurface surface) {
    this(surface, 1, surface.height() - 2);
  }

  /**
   * Draws a line from (startX, startY) to (endX, endY) using the given pixel source. The line is clipped to the
   * viewport bounds before rasterization.
   */
  public void drawLine(int startX, int startY, int endX, int endY, PixelSource source) {
    if (yMax < yMin) {
      return; // Surface too small
    }

    // ========== PHASE 1: Cohen-Sutherland Line Clipping ==========
    // Clips the line segment to the rectangular viewport [0, width-1] x [yMin, yMax].
    // Uses 4-bit region codes to quickly accept/reject lines and find intersection points.

    int regionCodeStart = computeRegionCode(startX, startY);
    int regionCodeEnd = computeRegionCode(endX, endY);

    // While coordinates are outside viewport
    while (true) {
      // TRIVIAL ACCEPT: Both endpoints inside viewport (both codes are 0)
      if ((regionCodeStart | regionCodeEnd) == INSIDE) {
        break;
      }

      // TRIVIAL REJECT: Both endpoints share an outside region (common bit set)
      // This means the line is entirely outside that edge
      if ((regionCodeStart & regionCodeEnd) != INSIDE) {
        return;
      }

      // Line crosses viewport boundary - clip to the edge
      // Pick an endpoint that's outside (non-zero code)
      int codeToClip = (regionCodeStart != INSIDE) ? regionCodeStart : regionCodeEnd;

      int clippedX, clippedY;
      int deltaX = endX - startX;
      int deltaY = endY - startY;

      // Find intersection with the edge indicated by the region code.
      // Uses line equation: (y - y0) / (x - x0) = deltaY / deltaX
      if ((codeToClip & ABOVE) != 0) {
        // Line crosses top edge: solve for x when y = yMin
        clippedX = startX + deltaX * (yMin - startY) / deltaY;
        clippedY = yMin;
      } else if ((codeToClip & BELOW) != 0) {
        // Line crosses bottom edge: solve for x when y = yMax
        clippedX = startX + deltaX * (yMax - startY) / deltaY;
        clippedY = yMax;
      } else if ((codeToClip & RIGHT) != 0) {
        // Line crosses right edge: solve for y when x = width-1
        clippedY = startY + deltaY * (width - 1 - startX) / deltaX;
        clippedX = width - 1;
      } else {
        // Line crosses left edge: solve for y when x = 0
        clippedY = startY + deltaY * -startX / deltaX;
        clippedX = 0;
      }

      // Replace the outside endpoint with the clipped point and recompute its code
      if (codeToClip == regionCodeStart) {
        startX = clippedX;
        startY = clippedY;
        regionCodeStart = computeRegionCode(startX, startY);
      } else {
        endX = clippedX;
        endY = clippedY;
        regionCodeEnd = computeRegionCode(endX, endY);
      }
    }

    // ========== PHASE 2: Bresenham's Line Algorithm ==========
    // Rasterizes the clipped line using only integer arithmetic.
    // Tracks accumulated error to decide when to step in the minor axis.

    int absDeltaX = Math.abs(endX - startX);  // horizontal distance
    int absDeltaY = Math.abs(endY - startY);  // vertical distance
    int stepX = startX < endX ? 1 : -1;       // direction of X movement
    int stepY = startY < endY ? 1 : -1;       // direction of Y movement

    // Error term: positive means we should step in X, negative means step in Y
    // Initialized to favor the major axis (larger delta)
    int error = absDeltaX - absDeltaY;

    int[] pixelData = surface.data();
    int currentX = startX;
    int currentY = startY;

    while (true) {
      // Draw pixel at current position
      pixelData[currentY * width + currentX] = source.getPixel();

      // Check if we've reached the end
      if (currentX == endX && currentY == endY) {
        break;
      }

      // Double the error for comparison (avoids fractions)
      int doubledError = 2 * error;

      // Step in X direction if error indicates we should
      if (doubledError > -absDeltaY) {
        error -= absDeltaY;
        currentX += stepX;
      }

      // Step in Y direction if error indicates we should
      if (doubledError < absDeltaX) {
        error += absDeltaX;
        currentY += stepY;
      }
    }
  }

  /**
   * Computes the 4-bit Cohen-Sutherland region code for a point. The viewport is divided into 9 regions. The center
   * region (code 0) is inside the viewport. Each bit indicates the point is beyond that edge: x<0    0≤x<w   x≥w
   * ┌──────┬──────┬──────┐ y<yMin│ 1001 │ 1000 │ 1010 │  ABOVE (bit 3) │  9   │  8   │  10  │ ├──────┼──────┼──────┤
   * inside│ 0001 │ 0000 │ 0010 │  INSIDE │  1   │  0   │  2   │ ├──────┼──────┼──────┤ y>yMax│ 0101 │ 0100 │ 0110 │
   * BELOW (bit 2) │  5   │  4   │  6   │ └──────┴──────┴──────┘ LEFT   INSIDE RIGHT (bit 0) (bit 1)
   */
  private int computeRegionCode(int x, int y) {
    int code = INSIDE;
    if (x < 0) {
      code |= LEFT;
    } else if (x >= width) {
      code |= RIGHT;
    }
    if (y < yMin) {
      code |= ABOVE;
    } else if (y > yMax) {
      code |= BELOW;
    }
    return code;
  }
}

package org.kevinferrare.oldskoolfire.drawable;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LineRasterizerTest {

  private static final int PIXEL_VALUE = 999;
  private static final PixelSource SOURCE = () -> PIXEL_VALUE;

  private FixedIntSurface surface(int width, int height) {
    return new FixedIntSurface(new int[width * (height + 1)], width, height);
  }

  private int countDrawnPixels(FixedIntSurface surface) {
    int count = 0;
    for (int v : surface.data()) {
      if (v == PIXEL_VALUE) count++;
    }
    return count;
  }

  @Test
  void horizontalLineDrawsExpectedPixels() {
    FixedIntSurface s = surface(20, 10);
    LineRasterizer rasterizer = new LineRasterizer(s, 0, 9);
    rasterizer.drawLine(2, 5, 10, 5, SOURCE);
    // Bresenham inclusive: 10 - 2 + 1 = 9 pixels
    assertEquals(9, countDrawnPixels(s));
  }

  @Test
  void verticalLineDrawsExpectedPixels() {
    FixedIntSurface s = surface(20, 10);
    LineRasterizer rasterizer = new LineRasterizer(s, 0, 9);
    rasterizer.drawLine(5, 1, 5, 8, SOURCE);
    assertEquals(8, countDrawnPixels(s));
  }

  @Test
  void diagonalLineDrawsCorrectPixels() {
    FixedIntSurface s = surface(20, 20);
    LineRasterizer rasterizer = new LineRasterizer(s, 0, 19);
    rasterizer.drawLine(0, 0, 9, 9, SOURCE);
    // 45-degree diagonal: 10 pixels
    assertEquals(10, countDrawnPixels(s));
  }

  @Test
  void lineFullyOutsideClipRegionDrawsNothing() {
    FixedIntSurface s = surface(20, 10);
    LineRasterizer rasterizer = new LineRasterizer(s, 0, 9);
    // Line entirely above viewport
    rasterizer.drawLine(0, -10, 10, -5, SOURCE);
    assertEquals(0, countDrawnPixels(s));
  }

  @Test
  void linePartiallyClippedDrawsOnlyVisiblePortion() {
    FixedIntSurface s = surface(20, 10);
    LineRasterizer rasterizer = new LineRasterizer(s, 0, 9);
    // Horizontal line starting off-screen left
    rasterizer.drawLine(-5, 5, 5, 5, SOURCE);
    // Only pixels 0..5 should be drawn = 6 pixels
    assertEquals(6, countDrawnPixels(s));
  }

  @Test
  void zeroLengthLineDrawsSinglePixel() {
    FixedIntSurface s = surface(20, 10);
    LineRasterizer rasterizer = new LineRasterizer(s, 0, 9);
    rasterizer.drawLine(5, 5, 5, 5, SOURCE);
    assertEquals(1, countDrawnPixels(s));
  }

  @Test
  void yMinYMaxGuardBandRespected() {
    FixedIntSurface s = surface(20, 10);
    // Restrict drawing to rows 3..6
    LineRasterizer rasterizer = new LineRasterizer(s, 3, 6);
    // Draw vertical line spanning full height
    rasterizer.drawLine(5, 0, 5, 9, SOURCE);
    // Should only draw rows 3,4,5,6 = 4 pixels
    assertEquals(4, countDrawnPixels(s));
  }

  @Test
  void negativeCoordinatesClippedCorrectly() {
    FixedIntSurface s = surface(20, 10);
    LineRasterizer rasterizer = new LineRasterizer(s, 0, 9);
    // Line from deeply negative to inside
    rasterizer.drawLine(-100, 5, 3, 5, SOURCE);
    // Should draw 4 pixels: x=0,1,2,3
    assertEquals(4, countDrawnPixels(s));
  }

  @Test
  void steepLineRasterizesCorrectly() {
    FixedIntSurface s = surface(20, 20);
    LineRasterizer rasterizer = new LineRasterizer(s, 0, 19);
    // dy > dx: steep line
    rasterizer.drawLine(5, 0, 7, 10, SOURCE);
    assertTrue(countDrawnPixels(s) > 0);
    // At minimum, should draw 11 pixels (one per row from y=0 to y=10)
    assertTrue(countDrawnPixels(s) >= 11);
  }

  @Test
  void lineAtExactSurfaceBoundaryIsIncluded() {
    FixedIntSurface s = surface(20, 10);
    LineRasterizer rasterizer = new LineRasterizer(s, 0, 9);
    // Draw along right edge
    rasterizer.drawLine(19, 0, 19, 9, SOURCE);
    assertEquals(10, countDrawnPixels(s));
  }
}

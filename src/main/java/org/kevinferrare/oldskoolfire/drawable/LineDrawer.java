package org.kevinferrare.oldskoolfire.drawable;

/**
 * Draws a line between two screen-space points using the given pixel source.
 * Decouples line rasterization from 3D projection so callers can inject
 * recording implementations for testing.
 */
@FunctionalInterface
public interface LineDrawer {
  void drawLine(int startX, int startY, int endX, int endY, PixelSource source);
}

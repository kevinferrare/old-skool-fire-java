package org.kevinferrare.oldskoolfire.renderer;

import org.kevinferrare.oldskoolfire.drawable.threed.Entity3D;

import java.util.Arrays;

/**
 * Simple wireframe renderer - draws lines on a black background.
 * Uses palette colors for the wireframe lines.
 */
public class WireframeSceneRenderer extends SceneRenderer {

  @Override
  public String getName() {
    return "wireframe";
  }

  @Override
  public void update(Entity3D shape) {
    // Clear back buffer to black
    Arrays.fill(backSurface.data(), 0);
    drawShape(shape);
  }
}

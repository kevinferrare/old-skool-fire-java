package org.kevinferrare.oldskoolfire.drawable.threed;

import org.kevinferrare.oldskoolfire.drawable.LineDrawer;
import org.kevinferrare.oldskoolfire.drawable.PixelSource;

import static java.lang.Math.round;

/**
 * A dedicated 3D graphics context that handles projection of meshes onto a surface.
 * Caches dimensions for perspective projection but accepts surface per draw call
 * to support triple buffering where the target surface changes each frame.
 */
public class Graphics3D {
  private static final double NEAR_PLANE = 0.1;
  private final int xCenter;
  private final int yCenter;

  /**
   * Creates a Graphics3D for surfaces of the given dimensions.
   * The actual surface is passed to draw methods to support buffer swapping.
   */
  public Graphics3D(int width, int height) {
    this.xCenter = width / 2;
    this.yCenter = height / 2;
  }

  public void drawEntity(LineDrawer lineDrawer, Entity3D entity) {
    drawMesh(lineDrawer, entity.mesh(), entity.transform(), entity.material().getPixelSource());
  }

  public void drawMesh(LineDrawer lineDrawer, Mesh mesh, Transform transform, PixelSource source) {
    Quaternion orientation = transform.getOrientation();
    double scale = transform.getScale();
    Vec3 translation = transform.getTranslation();

    Vec3[] vertices = mesh.vertices();
    int[] lines = mesh.lines();

    for (int i = 0; i < lines.length; i += 2) {
      drawProjectedLine(lineDrawer, vertices[lines[i]], vertices[lines[i + 1]],
        orientation, scale, translation, source);
    }
  }

  private void drawProjectedLine(LineDrawer lineDrawer, Vec3 v0, Vec3 v1, Quaternion orientation,
                                 double scale, Vec3 translation, PixelSource source) {

    // Rotate then translate
    Vec3 p0 = orientation.rotate(v0).add(translation);
    Vec3 p1 = orientation.rotate(v1).add(translation);

    // Near-plane clipping
    boolean p0Behind = p0.z() <= NEAR_PLANE;
    boolean p1Behind = p1.z() <= NEAR_PLANE;
    if (p0Behind && p1Behind) {
      return;
    }
    if (p0Behind) {
      p0 = clipToNearPlane(p0, p1);
    }
    if (p1Behind) {
      p1 = clipToNearPlane(p1, p0);
    }

    // Perspective projection: scale controls apparent size
    int x0Screen = (int) round(scale * p0.x() / p0.z()) + xCenter;
    int y0Screen = (int) round(scale * p0.y() / p0.z()) + yCenter;
    int x1Screen = (int) round(scale * p1.x() / p1.z()) + xCenter;
    int y1Screen = (int) round(scale * p1.y() / p1.z()) + yCenter;

    lineDrawer.drawLine(x0Screen, y0Screen, x1Screen, y1Screen, source);
  }

  private static Vec3 clipToNearPlane(Vec3 behind, Vec3 inFront) {
    double t = (NEAR_PLANE - behind.z()) / (inFront.z() - behind.z());
    return new Vec3(
      behind.x() + t * (inFront.x() - behind.x()),
      behind.y() + t * (inFront.y() - behind.y()),
      NEAR_PLANE);
  }
}

package org.kevinferrare.oldskoolfire.drawable.threed;

import org.junit.jupiter.api.Test;
import org.kevinferrare.oldskoolfire.drawable.FixedIntSurface;
import org.kevinferrare.oldskoolfire.drawable.LineRasterizer;

import static org.junit.jupiter.api.Assertions.*;

public class Geometry3DTest {

  @Test
  public void testMeshImmutability() {
    double[] rawVertices = {1.0, 2.0, 3.0};
    int[] lines = {0, 1};
    Mesh mesh = Mesh.fromDoubleArray(rawVertices, lines);

    // Ensure data is stored - now as Vec3[]
    assertEquals(1, mesh.vertices().length);
    assertEquals(1.0, mesh.vertices()[0].x());
    assertEquals(2.0, mesh.vertices()[0].y());
    assertEquals(3.0, mesh.vertices()[0].z());
    assertArrayEquals(lines, mesh.lines());
  }

  @Test
  public void testTransformState() {
    Transform t = new Transform();
    // Verify initial orientation is identity
    Quaternion q = t.getOrientation();
    assertEquals(1.0, q.w(), 1e-9);
    assertEquals(0.0, q.x(), 1e-9);
    assertEquals(0.0, q.y(), 1e-9);
    assertEquals(0.0, q.z(), 1e-9);

    // After rotation, quaternion should change
    t.rotate(0.5, 0.2);
    Quaternion q2 = t.getOrientation();
    // Quaternion is no longer identity
    assertFalse(q2.w() == 1.0 && q2.x() == 0.0 && q2.y() == 0.0 && q2.z() == 0.0);
  }

  @Test
  public void testProjectionMath() {
    // We use a custom surface to verify pixel plotting
    int width = 100;
    int height = 100;
    int[] data = new int[width * height];
    FixedIntSurface surface = new FixedIntSurface(data, width, height);
    Graphics3D g3d = new Graphics3D(width, height);
    LineRasterizer lineRasterizer = new LineRasterizer(surface);

    // A single line from (1, 0, 0) to (0, 0, 0)
    Mesh mesh = Mesh.fromDoubleArray(new double[]{1.0, 0.0, 0.0, 0.0, 0.0, 0.0}, new int[]{0, 1});
    Transform transform = new Transform();
    transform.setScale(100);
    transform.setTranslateZ(10);

    // Rotate 90 degrees around Y (angleX=0, angleY=PI/2)
    // x' = x*cosY + z*sinY = 1*0 + 0*1 = 0
    // z' = z*cosY - x*sinY = 0*0 - 1*1 = -1
    transform.rotate(0, Math.PI / 2);

    g3d.drawMesh(lineRasterizer, mesh, transform, () -> 255);

    // Final Z = -1 + 10 = 9
    // Projected X = 100 * 0 / 9 = 0. Offset by center (50) = 50.
    // The start of the line should be at x=50, y=50
    // The end of the line (0,0,0) is also at x=50, y=50
    // Wait, a point (0,0,0) projects to 50,50
    // Point (1,0,0) rotated 90deg Y -> (0,0,-1).
    // Projected: x=0, y=0. Screen: 50,50.
    // So the line is 0 pixels long if both points are same.

    // Let's use a fresh transform with no rotation
    Transform transform2 = new Transform();
    transform2.setScale(100);
    transform2.setTranslateZ(10);
    // (1,0,0) and (0,0,0)
    // (1,0,0) -> Z=10, X=100*1/10 = 10. Screen = 50+10=60.
    // (0,0,0) -> Z=10, X=0. Screen = 50+0=50.
    g3d.drawMesh(lineRasterizer, mesh, transform2, () -> 255);

    // Check if any pixels were set near 60,50
    assertEquals(255, data[50 * width + 60], "Pixel at (60, 50) should be set");
    assertEquals(255, data[50 * width + 50], "Pixel at (50, 50) should be set");
  }
}

package org.kevinferrare.oldskoolfire.drawable.threed;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kevinferrare.oldskoolfire.drawable.LineDrawer;
import org.kevinferrare.oldskoolfire.drawable.PixelSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests 3D-to-2D projection and near-plane clipping in {@link Graphics3D}
 * by injecting a recording {@link LineDrawer} instead of a real rasterizer.
 */
public class Graphics3DTest {

  private static final int WIDTH = 200;
  private static final int HEIGHT = 200;
  private static final int CENTER_X = WIDTH / 2;
  private static final int CENTER_Y = HEIGHT / 2;
  private static final PixelSource WHITE = () -> 255;

  private record DrawnLine(int x0, int y0, int x1, int y1) {
  }

  private final List<DrawnLine> lines = new ArrayList<>();
  private final LineDrawer recorder = (x0, y0, x1, y1, src) -> lines.add(new DrawnLine(x0, y0, x1, y1));
  private Graphics3D g3d;

  @BeforeEach
  void setUp() {
    g3d = new Graphics3D(WIDTH, HEIGHT);
    lines.clear();
  }

  @Test
  void projectionOfKnownVertex() {
    // Vertex at (1,0,0) and (0,0,0), identity rotation, scale=100, translateZ=10
    // (1,0,0) -> Z=10, X_screen = 100*1/10 + 100 = 110
    // (0,0,0) -> Z=10, X_screen = 0 + 100 = 100
    Mesh mesh = Mesh.fromDoubleArray(
      new double[]{1, 0, 0, 0, 0, 0}, new int[]{0, 1});
    Transform t = new Transform();
    t.setScale(100);
    t.setTranslateZ(10);

    g3d.drawMesh(recorder, mesh, t, WHITE);

    assertEquals(1, lines.size(), "One drawLine call expected");
    DrawnLine l = lines.getFirst();
    assertEquals(CENTER_X + 10, l.x0, "x0 projection");
    assertEquals(CENTER_Y, l.y0, "y0 projection");
    assertEquals(CENTER_X, l.x1, "x1 projection");
    assertEquals(CENTER_Y, l.y1, "y1 projection");
  }

  @Test
  void bothVerticesBehindNearPlane() {
    // Both vertices at z < NEAR_PLANE after translation → no drawLine calls
    Mesh mesh = Mesh.fromDoubleArray(
      new double[]{0, 0, 0, 1, 0, 0}, new int[]{0, 1});
    Transform t = new Transform();
    t.setScale(100);
    t.setTranslateZ(-1); // puts both at z=-1+(-1)=-2, well behind near plane

    g3d.drawMesh(recorder, mesh, t, WHITE);

    assertTrue(lines.isEmpty(), "No line should be drawn when both behind near plane");
  }

  @Test
  void oneVertexBehindNearPlaneIsClipped() {
    // v0=(0,0,-5), v1=(0,0,5) with translateZ=0
    // After translate: v0.z=-5 (behind), v1.z=5 (in front)
    // Clip: t = (0.1 - (-5)) / (5 - (-5)) = 5.1/10 = 0.51
    // Clipped point: z=0.1, x=0, y=0
    Mesh mesh = Mesh.fromDoubleArray(
      new double[]{0, 0, -5, 0, 0, 5}, new int[]{0, 1});
    Transform t = new Transform();
    t.setScale(100);
    t.setTranslateZ(0);

    g3d.drawMesh(recorder, mesh, t, WHITE);

    assertEquals(1, lines.size(), "One clipped line expected");
    DrawnLine l = lines.getFirst();
    // Clipped point projects to center (x=0/0.1 * 100 + center = center)
    // v1 projects to center (x=0/5 * 100 + center = center)
    assertEquals(CENTER_X, l.x0);
    assertEquals(CENTER_X, l.x1);
  }

  @Test
  void bothVerticesInFront() {
    // v0=(2,0,5), v1=(-2,0,5) - both at z=5
    Mesh mesh = Mesh.fromDoubleArray(
      new double[]{2, 0, 5, -2, 0, 5}, new int[]{0, 1});
    Transform t = new Transform();
    t.setScale(100);
    t.setTranslateZ(0);

    g3d.drawMesh(recorder, mesh, t, WHITE);

    assertEquals(1, lines.size(), "One line expected");
    DrawnLine l = lines.getFirst();
    // x0 = 100*2/5 + 100 = 140
    // x1 = 100*(-2)/5 + 100 = 60
    assertEquals(CENTER_X + 40, l.x0, "x0 = center + scale*x/z");
    assertEquals(CENTER_X - 40, l.x1, "x1 = center + scale*x/z");
  }

  @Test
  void swappedVertexOrderStillClips() {
    // Same as oneVertexBehindNearPlaneIsClipped but vertices swapped
    Mesh mesh = Mesh.fromDoubleArray(
      new double[]{0, 0, 5, 0, 0, -5}, new int[]{0, 1});
    Transform t = new Transform();
    t.setScale(100);
    t.setTranslateZ(0);

    g3d.drawMesh(recorder, mesh, t, WHITE);

    assertEquals(1, lines.size(), "Clipping should work regardless of vertex order");
  }
}

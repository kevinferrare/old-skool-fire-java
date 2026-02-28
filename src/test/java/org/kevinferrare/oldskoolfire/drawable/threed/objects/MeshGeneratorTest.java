package org.kevinferrare.oldskoolfire.drawable.threed.objects;

import org.junit.jupiter.api.Test;
import org.kevinferrare.oldskoolfire.drawable.threed.Mesh;
import org.kevinferrare.oldskoolfire.drawable.threed.Vec3;

import static org.junit.jupiter.api.Assertions.*;

class MeshGeneratorTest {

  @Test
  void createCubeHas8VerticesAnd12Edges() {
    Mesh cube = MeshGenerator.createCube();
    assertEquals(8, cube.vertices().length);
    assertEquals(24, cube.lines().length); // 12 edges * 2 indices
  }

  @Test
  void createCubeVerticesAreUnitSized() {
    Mesh cube = MeshGenerator.createCube();
    for (Vec3 v : cube.vertices()) {
      assertTrue(Math.abs(v.x()) <= 1.0);
      assertTrue(Math.abs(v.y()) <= 1.0);
      assertTrue(Math.abs(v.z()) <= 1.0);
    }
  }

  @Test
  void createPyramidHas5Vertices() {
    Mesh pyramid = MeshGenerator.createPyramid(1.5);
    assertEquals(5, pyramid.vertices().length);
    assertEquals(16, pyramid.lines().length); // 4 base + 4 side edges = 8 * 2
  }

  @Test
  void createOctahedronHas6Vertices() {
    Mesh oct = MeshGenerator.createOctahedron();
    assertEquals(6, oct.vertices().length);
    assertTrue(oct.lines().length > 0);
  }

  @Test
  void createIcosahedronHas12VerticesAnd30Edges() {
    Mesh ico = MeshGenerator.createIcosahedron();
    assertEquals(12, ico.vertices().length);
    assertEquals(60, ico.lines().length); // 30 edges * 2 indices
  }

  @Test
  void createTorusHasCorrectVertexCount() {
    int major = 12, minor = 6;
    Mesh torus = MeshGenerator.createTorus(0.4, major, minor);
    assertEquals(major * minor, torus.vertices().length);
  }

  @Test
  void createSphereHasPolesAndRings() {
    int lat = 8, lon = 10;
    Mesh sphere = MeshGenerator.createSphere(lat, lon);
    // 2 poles + (lat-1) rings * lon vertices per ring
    assertEquals(2 + (lat - 1) * lon, sphere.vertices().length);
  }

  @Test
  void createTrefoilKnotIsClosedLoop() {
    int segments = 60;
    Mesh knot = MeshGenerator.createTrefoilKnot(segments);
    assertEquals(segments, knot.vertices().length);
    assertEquals(segments * 2, knot.lines().length);
  }

  @Test
  void createHelixHasCorrectVertexCount() {
    int segments = 40;
    Mesh helix = MeshGenerator.createHelix(3, 4.0, segments);
    assertEquals(segments + 1, helix.vertices().length);
  }

  @Test
  void createLissajousIsClosedLoop() {
    int segments = 80;
    Mesh lissajous = MeshGenerator.createLissajous(3, 2, 5, Math.PI / 2, 0, segments);
    assertEquals(segments, lissajous.vertices().length);
    assertEquals(segments * 2, lissajous.lines().length);
  }

  @Test
  void createStarHasFrontAndBackFaces() {
    int points = 5;
    Mesh star = MeshGenerator.createStar(points, 0.4, 0.3);
    // Front: 2*points, back: 2*points
    assertEquals(points * 4, star.vertices().length);
  }

  @Test
  void createNoneIsEmpty() {
    Mesh none = MeshGenerator.createNone();
    assertEquals(0, none.vertices().length);
    assertEquals(0, none.lines().length);
  }

  @Test
  void createMobiusStripHasCorrectVertexCount() {
    int lengthSeg = 16, widthSeg = 4;
    Mesh mobius = MeshGenerator.createMobiusStrip(0.5, lengthSeg, widthSeg);
    assertEquals(lengthSeg * (widthSeg + 1), mobius.vertices().length);
  }

  @Test
  void allLineIndicesAreWithinBounds() {
    Mesh[] meshes = {
      MeshGenerator.createCube(),
      MeshGenerator.createPyramid(1.0),
      MeshGenerator.createOctahedron(),
      MeshGenerator.createIcosahedron(),
      MeshGenerator.createTorus(0.4, 12, 6),
      MeshGenerator.createSphere(6, 8),
      MeshGenerator.createTrefoilKnot(30),
      MeshGenerator.createHelix(2, 3.0, 20),
      MeshGenerator.createLissajous(3, 2, 5, 0, 0, 40),
      MeshGenerator.createStar(5, 0.4, 0.3),
      MeshGenerator.createMobiusStrip(0.5, 16, 4),
    };
    for (Mesh mesh : meshes) {
      assertNotNull(mesh.vertices());
      assertNotNull(mesh.lines());
      for (int idx : mesh.lines()) {
        assertTrue(idx >= 0 && idx < mesh.vertices().length,
          "Line index " + idx + " out of bounds for " + mesh.vertices().length + " vertices");
      }
    }
  }
}

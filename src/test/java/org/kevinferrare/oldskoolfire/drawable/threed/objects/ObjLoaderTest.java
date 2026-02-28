package org.kevinferrare.oldskoolfire.drawable.threed.objects;

import org.junit.jupiter.api.Test;
import org.kevinferrare.oldskoolfire.drawable.threed.Mesh;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ObjLoaderTest {

  private final ObjLoader loader = new ObjLoader();

  @Test
  void loadTriangleFromStream() throws IOException {
    String obj = """
      v 0 0 0
      v 1 0 0
      v 0 1 0
      f 1 2 3
      """;
    Mesh mesh = loader.load(new ByteArrayInputStream(obj.getBytes(StandardCharsets.UTF_8)));
    assertEquals(3, mesh.vertices().length);
    // Triangle has 3 edges, each stored as 2 indices
    assertEquals(6, mesh.lines().length);
  }

  @Test
  void edgesAreDeduplicatedAcrossSharedFaces() throws IOException {
    // Two triangles sharing edge 2-3
    String obj = """
      v 0 0 0
      v 1 0 0
      v 1 1 0
      v 0 1 0
      f 1 2 3
      f 1 3 4
      """;
    Mesh mesh = loader.load(new ByteArrayInputStream(obj.getBytes(StandardCharsets.UTF_8)));
    assertEquals(4, mesh.vertices().length);
    // 5 unique edges: (0,1), (1,2), (0,2), (2,3), (0,3)
    assertEquals(10, mesh.lines().length);
  }

  @Test
  void yAxisIsNegated() throws IOException {
    String obj = """
      v 1 2 3
      f 1
      """;
    Mesh mesh = loader.load(new ByteArrayInputStream(obj.getBytes(StandardCharsets.UTF_8)));
    assertEquals(1.0, mesh.vertices()[0].x(), 1e-6);
    assertEquals(-2.0, mesh.vertices()[0].y(), 1e-6);
    assertEquals(3.0, mesh.vertices()[0].z(), 1e-6);
  }

  @Test
  void loadResourceLoadsBuiltInMesh() {
    Mesh mesh = loader.loadResource("/meshes/plane.obj");
    assertNotNull(mesh);
    assertTrue(mesh.vertices().length > 0);
    assertTrue(mesh.lines().length > 0);
  }

  @Test
  void loadResourceThrowsOnMissingResource() {
    assertThrows(IllegalArgumentException.class, () -> loader.loadResource("/nonexistent.obj"));
  }

  @Test
  void allLineIndicesAreWithinBounds() {
    Mesh mesh = loader.loadResource("/meshes/plane.obj");
    for (int idx : mesh.lines()) {
      assertTrue(idx >= 0 && idx < mesh.vertices().length,
        "Line index " + idx + " out of bounds for " + mesh.vertices().length + " vertices");
    }
  }
}

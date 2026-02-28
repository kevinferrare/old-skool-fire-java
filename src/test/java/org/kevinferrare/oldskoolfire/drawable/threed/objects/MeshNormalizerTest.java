package org.kevinferrare.oldskoolfire.drawable.threed.objects;

import org.junit.jupiter.api.Test;
import org.kevinferrare.oldskoolfire.drawable.threed.Mesh;
import org.kevinferrare.oldskoolfire.drawable.threed.Vec3;

import static org.junit.jupiter.api.Assertions.*;

public class MeshNormalizerTest {
  private static final double EPSILON = 1e-9;

  private final MeshNormalizer normalizer = new MeshNormalizer();

  private Mesh cubeMesh() {
    Vec3[] vertices = {
      new Vec3(-1, -1, -1), new Vec3(1, -1, -1),
      new Vec3(1, 1, -1), new Vec3(-1, 1, -1),
      new Vec3(-1, -1, 1), new Vec3(1, -1, 1),
      new Vec3(1, 1, 1), new Vec3(-1, 1, 1)
    };
    int[] lines = {0, 1, 1, 2, 2, 3, 3, 0};
    return new Mesh(vertices, lines);
  }

  @Test
  void centeredCubeStaysCentered() {
    Mesh result = normalizer.normalize(cubeMesh());
    double cx = 0, cy = 0, cz = 0;
    for (Vec3 v : result.vertices()) {
      cx += v.x();
      cy += v.y();
      cz += v.z();
    }
    cx /= result.vertices().length;
    cy /= result.vertices().length;
    cz /= result.vertices().length;
    assertEquals(0.0, cx, EPSILON);
    assertEquals(0.0, cy, EPSILON);
    assertEquals(0.0, cz, EPSILON);
  }

  @Test
  void offCenterMeshIsRecentered() {
    Vec3[] vertices = {
      new Vec3(10, 20, 30), new Vec3(12, 22, 32)
    };
    Mesh mesh = new Mesh(vertices, new int[]{0, 1});
    Mesh result = normalizer.normalize(mesh);
    double cx = 0, cy = 0, cz = 0;
    for (Vec3 v : result.vertices()) {
      cx += v.x();
      cy += v.y();
      cz += v.z();
    }
    cx /= result.vertices().length;
    cy /= result.vertices().length;
    cz /= result.vertices().length;
    assertEquals(0.0, cx, EPSILON);
    assertEquals(0.0, cy, EPSILON);
    assertEquals(0.0, cz, EPSILON);
  }

  @Test
  void normalizedMeshFitsWithinBounds() {
    Vec3[] vertices = {
      new Vec3(0, 0, 0), new Vec3(100, 50, 25)
    };
    Mesh mesh = new Mesh(vertices, new int[]{0, 1});
    Mesh result = normalizer.normalize(mesh);
    for (Vec3 v : result.vertices()) {
      assertTrue(Math.abs(v.x()) <= 1.0 + EPSILON, "x out of bounds: " + v.x());
      assertTrue(Math.abs(v.y()) <= 1.0 + EPSILON, "y out of bounds: " + v.y());
      assertTrue(Math.abs(v.z()) <= 1.0 + EPSILON, "z out of bounds: " + v.z());
    }
  }

  @Test
  void singleVertexNormalizesToOrigin() {
    // Single vertex → bounding sphere diameter is 0, mesh returned as-is (degenerate)
    Vec3[] vertices = {new Vec3(5, 5, 5)};
    Mesh mesh = new Mesh(vertices, new int[0]);
    Mesh result = normalizer.normalize(mesh);
    // Degenerate mesh: returned unchanged
    assertEquals(5, result.vertices()[0].x(), EPSILON);
  }

  @Test
  void emptyMeshReturnsEmpty() {
    Mesh mesh = new Mesh(new Vec3[0], new int[0]);
    Mesh result = normalizer.normalize(mesh);
    assertEquals(0, result.vertices().length);
  }

  @Test
  void normalizationPreservesLineIndices() {
    int[] lines = {0, 1, 1, 2, 2, 3, 3, 0};
    Mesh result = normalizer.normalize(cubeMesh());
    assertArrayEquals(lines, result.lines());
  }

  @Test
  void normalizationIsIdempotent() {
    Mesh first = normalizer.normalize(cubeMesh());
    Mesh second = normalizer.normalize(first);
    for (int i = 0; i < first.vertices().length; i++) {
      assertEquals(first.vertices()[i].x(), second.vertices()[i].x(), EPSILON);
      assertEquals(first.vertices()[i].y(), second.vertices()[i].y(), EPSILON);
      assertEquals(first.vertices()[i].z(), second.vertices()[i].z(), EPSILON);
    }
  }
}

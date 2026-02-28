package org.kevinferrare.oldskoolfire.drawable.threed.objects;

import org.kevinferrare.oldskoolfire.drawable.threed.Mesh;
import org.kevinferrare.oldskoolfire.drawable.threed.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Procedural mesh generator for various 3D shapes.
 * All generators output unit-sized meshes (roughly -1 to 1 range).
 * Use Transform.setScale() to control apparent size.
 */
public final class MeshGenerator {

  private MeshGenerator() {
  }

  /**
   * Generate a unit cube centered at origin (-1 to 1 on each axis).
   */
  public static Mesh createCube() {
    return Mesh.fromDoubleArray(
      new double[]{
        -1, -1, -1, 1, -1, -1, 1, 1, -1, -1, 1, -1,
        -1, -1, 1, 1, -1, 1, 1, 1, 1, -1, 1, 1
      },
      new int[]{
        0, 1, 1, 2, 2, 3, 3, 0, // front
        4, 5, 5, 6, 6, 7, 7, 4, // back
        0, 4, 1, 5, 2, 6, 3, 7  // sides
      }
    );
  }

  /**
   * Generate a unit pyramid with square base and apex.
   *
   * @param heightRatio height relative to base (1.0 = same as base width)
   */
  public static Mesh createPyramid(double heightRatio) {
    return Mesh.fromDoubleArray(
      new double[]{
        -1, -1, 0, 1, -1, 0, 1, 1, 0, -1, 1, 0, 0, 0, heightRatio
      },
      new int[]{
        0, 1, 1, 2, 2, 3, 3, 0, // base
        0, 4, 1, 4, 2, 4, 3, 4  // sides to apex
      }
    );
  }

  /**
   * Generate a torus (donut shape).
   *
   * @param tubeRatio     tube radius relative to major radius (0.0-1.0)
   * @param majorSegments segments around the main ring
   * @param minorSegments segments around the tube
   */
  public static Mesh createTorus(double tubeRatio, int majorSegments, int minorSegments) {
    double majorRadius = 1.0 / (1.0 + tubeRatio);
    double minorRadius = majorRadius * tubeRatio;
    List<Vec3> vertices = new ArrayList<>();
    List<int[]> lines = new ArrayList<>();

    for (int i = 0; i < majorSegments; i++) {
      double theta = 2 * Math.PI * i / majorSegments;
      double cosTheta = Math.cos(theta);
      double sinTheta = Math.sin(theta);

      for (int j = 0; j < minorSegments; j++) {
        double phi = 2 * Math.PI * j / minorSegments;
        double cosPhi = Math.cos(phi);
        double sinPhi = Math.sin(phi);

        double x = (majorRadius + minorRadius * cosPhi) * cosTheta;
        double y = minorRadius * sinPhi;
        double z = (majorRadius + minorRadius * cosPhi) * sinTheta;
        vertices.add(new Vec3(x, y, z));

        int current = i * minorSegments + j;
        int nextJ = i * minorSegments + (j + 1) % minorSegments;
        int nextI = ((i + 1) % majorSegments) * minorSegments + j;

        lines.add(new int[]{current, nextJ});
        lines.add(new int[]{current, nextI});
      }
    }

    return buildMesh(vertices, lines);
  }

  /**
   * Generate a wireframe sphere using latitude/longitude grid.
   */
  public static Mesh createSphere(int latSegments, int lonSegments) {
    double radius = 1.0;
    List<Vec3> vertices = new ArrayList<>();
    List<int[]> lines = new ArrayList<>();

    // Add poles
    vertices.add(new Vec3(0, radius, 0));  // North pole (index 0)
    vertices.add(new Vec3(0, -radius, 0)); // South pole (index 1)

    // Add latitude rings
    for (int lat = 1; lat < latSegments; lat++) {
      double theta = Math.PI * lat / latSegments;
      double sinTheta = Math.sin(theta);
      double cosTheta = Math.cos(theta);
      double y = radius * cosTheta;
      double ringRadius = radius * sinTheta;

      for (int lon = 0; lon < lonSegments; lon++) {
        double phi = 2 * Math.PI * lon / lonSegments;
        double x = ringRadius * Math.cos(phi);
        double z = ringRadius * Math.sin(phi);
        vertices.add(new Vec3(x, y, z));

        int current = 2 + (lat - 1) * lonSegments + lon;
        int nextLon = 2 + (lat - 1) * lonSegments + (lon + 1) % lonSegments;

        // Longitude line
        lines.add(new int[]{current, nextLon});

        // Latitude line to next ring
        if (lat < latSegments - 1) {
          int nextLat = 2 + lat * lonSegments + lon;
          lines.add(new int[]{current, nextLat});
        }

        // Connect to poles
        if (lat == 1) {
          lines.add(new int[]{0, current}); // North pole
        }
        if (lat == latSegments - 1) {
          lines.add(new int[]{1, current}); // South pole
        }
      }
    }

    return buildMesh(vertices, lines);
  }

  /**
   * Generate an octahedron (8 triangular faces, 6 vertices).
   */
  public static Mesh createOctahedron() {
    double size = 1.0;
    Vec3[] vertices = {
      new Vec3(size, 0, 0),
      new Vec3(-size, 0, 0),
      new Vec3(0, size, 0),
      new Vec3(0, -size, 0),
      new Vec3(0, 0, size),
      new Vec3(0, 0, -size)
    };

    int[] lines = {
      // Top pyramid
      0, 2, 2, 4, 4, 0,
      2, 1, 1, 4,
      1, 5, 5, 2,
      0, 5,
      // Bottom pyramid
      0, 3, 3, 4,
      3, 1,
      1, 3, 3, 5, 5, 0
    };

    return new Mesh(vertices, lines);
  }

  /**
   * Generate an icosahedron (20 triangular faces, 12 vertices).
   */
  public static Mesh createIcosahedron() {
    double size = 1.0;
    double phi = (1 + Math.sqrt(5)) / 2; // Golden ratio
    double a = size;
    double b = size / phi;

    Vec3[] vertices = {
      new Vec3(0, b, -a), new Vec3(b, a, 0), new Vec3(-b, a, 0),
      new Vec3(0, b, a), new Vec3(0, -b, a), new Vec3(-a, 0, b),
      new Vec3(0, -b, -a), new Vec3(a, 0, -b), new Vec3(a, 0, b),
      new Vec3(-a, 0, -b), new Vec3(b, -a, 0), new Vec3(-b, -a, 0)
    };

    // All 30 edges of icosahedron
    int[] lines = {
      0, 1, 0, 2, 0, 6, 0, 7, 0, 9,
      1, 2, 1, 3, 1, 7, 1, 8,
      2, 3, 2, 5, 2, 9,
      3, 4, 3, 5, 3, 8,
      4, 5, 4, 8, 4, 10, 4, 11,
      5, 9, 5, 11,
      6, 7, 6, 9, 6, 10, 6, 11,
      7, 8, 7, 10,
      8, 10,
      9, 11,
      10, 11
    };

    return new Mesh(vertices, lines);
  }

  /**
   * Generate a trefoil knot (parametric curve).
   */
  public static Mesh createTrefoilKnot(int segments) {
    double size = 0.3;
    List<Vec3> vertices = new ArrayList<>();
    List<int[]> lines = new ArrayList<>();

    for (int i = 0; i < segments; i++) {
      double t = 2 * Math.PI * i / segments;
      double x = size * (Math.sin(t) + 2 * Math.sin(2 * t));
      double y = size * (Math.cos(t) - 2 * Math.cos(2 * t));
      double z = size * (-Math.sin(3 * t));
      vertices.add(new Vec3(x, y, z));

      lines.add(new int[]{i, (i + 1) % segments});
    }

    return buildMesh(vertices, lines);
  }

  /**
   * Generate a Möbius strip.
   *
   * @param widthRatio     width of strip relative to radius
   * @param lengthSegments segments along the strip
   * @param widthSegments  segments across the width
   */
  public static Mesh createMobiusStrip(double widthRatio, int lengthSegments, int widthSegments) {
    double radius = 1.0;
    double width = widthRatio;
    List<Vec3> vertices = new ArrayList<>();
    List<int[]> lines = new ArrayList<>();

    for (int i = 0; i < lengthSegments; i++) {
      double u = 2 * Math.PI * i / lengthSegments;
      double cosU = Math.cos(u);
      double sinU = Math.sin(u);

      for (int j = 0; j <= widthSegments; j++) {
        double v = width * (j - widthSegments / 2.0) / widthSegments;
        double halfTwist = u / 2;

        double x = (radius + v * Math.cos(halfTwist)) * cosU;
        double y = (radius + v * Math.cos(halfTwist)) * sinU;
        double z = v * Math.sin(halfTwist);
        vertices.add(new Vec3(x, y, z));

        int current = i * (widthSegments + 1) + j;

        // Width lines
        if (j < widthSegments) {
          lines.add(new int[]{current, current + 1});
        }

        // Length lines - connect to next ring, but last ring connects reversed due to half-twist
        int nextI;
        if (i < lengthSegments - 1) {
          nextI = (i + 1) * (widthSegments + 1) + j;
        } else {
          // Last segment wraps to first, but flipped (j -> widthSegments - j)
          nextI = widthSegments - j;
        }
        lines.add(new int[]{current, nextI});
      }
    }

    return buildMesh(vertices, lines);
  }

  /**
   * Generate a helix (spring shape).
   *
   * @param turns       number of turns
   * @param heightRatio height relative to radius
   * @param segments    number of segments
   */
  public static Mesh createHelix(double turns, double heightRatio, int segments) {
    double radius = 1.0 / (1.0 + heightRatio);
    double height = radius * heightRatio;
    List<Vec3> vertices = new ArrayList<>();
    List<int[]> lines = new ArrayList<>();

    for (int i = 0; i <= segments; i++) {
      double t = turns * 2 * Math.PI * i / segments;
      double x = radius * Math.cos(t);
      double z = radius * Math.sin(t);
      double y = height * i / segments - height / 2;
      vertices.add(new Vec3(x, y, z));

      if (i < segments) {
        lines.add(new int[]{i, i + 1});
      }
    }

    return buildMesh(vertices, lines);
  }

  /**
   * Generate a 3D Lissajous curve.
   */
  public static Mesh createLissajous(int a, int b, int c,
                                     double deltaX, double deltaY, int segments) {
    double size = 1.0;
    List<Vec3> vertices = new ArrayList<>();
    List<int[]> lines = new ArrayList<>();

    for (int i = 0; i < segments; i++) {
      double t = 2 * Math.PI * i / segments;
      double x = size * Math.sin(a * t + deltaX);
      double y = size * Math.sin(b * t + deltaY);
      double z = size * Math.sin(c * t);
      vertices.add(new Vec3(x, y, z));

      lines.add(new int[]{i, (i + 1) % segments});
    }

    return buildMesh(vertices, lines);
  }

  /**
   * Generate a 3D star shape.
   *
   * @param points     number of points
   * @param innerRatio inner radius relative to outer (0.0-1.0)
   * @param depthRatio depth relative to radius
   */
  public static Mesh createStar(int points, double innerRatio, double depthRatio) {
    double outerRadius = 1.0;
    double innerRadius = innerRatio;
    double depth = depthRatio;
    List<Vec3> vertices = new ArrayList<>();
    List<int[]> lines = new ArrayList<>();

    // Front face vertices
    for (int i = 0; i < points * 2; i++) {
      double angle = Math.PI * i / points - Math.PI / 2;
      double r = (i % 2 == 0) ? outerRadius : innerRadius;
      vertices.add(new Vec3(r * Math.cos(angle), r * Math.sin(angle), depth / 2));
    }

    // Back face vertices
    for (int i = 0; i < points * 2; i++) {
      double angle = Math.PI * i / points - Math.PI / 2;
      double r = (i % 2 == 0) ? outerRadius : innerRadius;
      vertices.add(new Vec3(r * Math.cos(angle), r * Math.sin(angle), -depth / 2));
    }

    int n = points * 2;

    // Front face lines
    for (int i = 0; i < n; i++) {
      lines.add(new int[]{i, (i + 1) % n});
    }

    // Back face lines
    for (int i = 0; i < n; i++) {
      lines.add(new int[]{n + i, n + (i + 1) % n});
    }

    // Connecting lines (depth)
    for (int i = 0; i < n; i++) {
      lines.add(new int[]{i, n + i});
    }

    return buildMesh(vertices, lines);
  }

  /**
   * Returns an empty mesh with no geometry. Used as the "no shape" option.
   */
  public static Mesh createNone() {
    return new Mesh(new Vec3[0], new int[0]);
  }

  private static Mesh buildMesh(List<Vec3> vertices, List<int[]> lineList) {
    Vec3[] vertArray = vertices.toArray(new Vec3[0]);
    int[] lines = new int[lineList.size() * 2];
    for (int i = 0; i < lineList.size(); i++) {
      lines[i * 2] = lineList.get(i)[0];
      lines[i * 2 + 1] = lineList.get(i)[1];
    }
    return new Mesh(vertArray, lines);
  }

}

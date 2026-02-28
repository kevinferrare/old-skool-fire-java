package org.kevinferrare.oldskoolfire.drawable.threed;

/**
 * Represents the static geometry of a 3D object.
 *
 * @param vertices array of 3D vertex positions
 * @param lines    indexes of the lines to draw
 *                 format is lineStart1, lineEnd1, lineStart2, lineEnd2, ..
 *                 index refers to the vertices array
 */
public record Mesh(Vec3[] vertices, int[] lines) {

  /**
   * Creates a Mesh from a flat double array of coordinates.
   *
   * @param rawVertices 3d coordinates in format x0, y0, z0, x1, y1, z1, ...
   * @param lines       line indices
   */
  public static Mesh fromDoubleArray(double[] rawVertices, int[] lines) {
    int vertexCount = rawVertices.length / 3;
    Vec3[] vertices = new Vec3[vertexCount];
    for (int i = 0; i < vertexCount; i++) {
      int idx = i * 3;
      vertices[i] = new Vec3(rawVertices[idx], rawVertices[idx + 1], rawVertices[idx + 2]);
    }
    return new Mesh(vertices, lines);
  }

}

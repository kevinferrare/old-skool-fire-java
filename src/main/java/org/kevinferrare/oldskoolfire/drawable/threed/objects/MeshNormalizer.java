package org.kevinferrare.oldskoolfire.drawable.threed.objects;

import lombok.extern.slf4j.Slf4j;
import org.kevinferrare.oldskoolfire.drawable.threed.Mesh;
import org.kevinferrare.oldskoolfire.drawable.threed.Vec3;

/**
 * Utility for normalizing meshes to a standard size and position.
 */
@Slf4j
public class MeshNormalizer {

  /**
   * Normalizes a mesh to fit within -1..1 on all axes, centered at origin.
   * Uses uniform scaling (preserves aspect ratio).
   */
  public Mesh normalize(Mesh mesh) {
    Vec3[] vertices = mesh.vertices();
    if (vertices.length == 0) {
      return mesh;
    }

    // Find bounding box
    double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
    double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
    double minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;

    for (Vec3 v : vertices) {
      minX = Math.min(minX, v.x());
      maxX = Math.max(maxX, v.x());
      minY = Math.min(minY, v.y());
      maxY = Math.max(maxY, v.y());
      minZ = Math.min(minZ, v.z());
      maxZ = Math.max(maxZ, v.z());
    }

    // Calculate center and scale
    double centerX = (minX + maxX) / 2;
    double centerY = (minY + maxY) / 2;
    double centerZ = (minZ + maxZ) / 2;

    double rangeX = maxX - minX;
    double rangeY = maxY - minY;
    double rangeZ = maxZ - minZ;

    // Use bounding sphere diameter for uniform visual size regardless of rotation
    // This ensures all meshes have the same maximum extent in any viewing direction
    double boundingSphereDiameter = Math.sqrt(rangeX * rangeX + rangeY * rangeY + rangeZ * rangeZ);

    log.debug("Bounding box: X[{}, {}] Y[{}, {}] Z[{}, {}]", minX, maxX, minY, maxY, minZ, maxZ);
    log.debug("Center: ({}, {}, {}), boundingSphereDiameter: {}", centerX, centerY, centerZ, boundingSphereDiameter);

    if (boundingSphereDiameter == 0) {
      return mesh; // Degenerate mesh
    }

    double scale = 2.0 / boundingSphereDiameter; // Scale so bounding sphere fits in -1..1
    log.debug("Scale factor: {}", scale);

    // Transform vertices
    Vec3[] normalized = new Vec3[vertices.length];
    for (int i = 0; i < vertices.length; i++) {
      Vec3 v = vertices[i];
      normalized[i] = new Vec3(
        (v.x() - centerX) * scale,
        (v.y() - centerY) * scale,
        (v.z() - centerZ) * scale
      );
    }

    return new Mesh(normalized, mesh.lines());
  }
}

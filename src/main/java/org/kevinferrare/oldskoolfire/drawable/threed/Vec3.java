package org.kevinferrare.oldskoolfire.drawable.threed;

/**
 * Immutable 3D vector/point.
 */
public record Vec3(double x, double y, double z) {

  public static final Vec3 ZERO = new Vec3(0, 0, 0);

  public Vec3 add(Vec3 other) {
    return new Vec3(x + other.x, y + other.y, z + other.z);
  }

  public double length() {
    return Math.sqrt(x * x + y * y + z * z);
  }
}

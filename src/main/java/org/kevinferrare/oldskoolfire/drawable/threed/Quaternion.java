package org.kevinferrare.oldskoolfire.drawable.threed;

/**
 * A quaternion for representing 3D rotations.
 * Quaternions avoid gimbal lock and compose naturally.
 */
public class Quaternion {

  private double w, x, y, z;

  public Quaternion() {
    this.w = 1.0;
    this.x = 0.0;
    this.y = 0.0;
    this.z = 0.0;
  }

  public Quaternion(double w, double x, double y, double z) {
    this.w = w;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public double w() {
    return w;
  }

  public double x() {
    return x;
  }

  public double y() {
    return y;
  }

  public double z() {
    return z;
  }

  /**
   * Create quaternion from axis-angle representation.
   */
  public static Quaternion fromAxisAngle(double axisX, double axisY, double axisZ, double angle) {
    double halfAngle = angle * 0.5;
    double s = Math.sin(halfAngle);
    return new Quaternion(
      Math.cos(halfAngle),
      axisX * s,
      axisY * s,
      axisZ * s
    );
  }

  /**
   * Create quaternion from Euler angles (X then Y rotation order).
   */
  public static Quaternion fromEulerXY(double angleX, double angleY) {
    Quaternion qx = fromAxisAngle(1, 0, 0, angleX);
    Quaternion qy = fromAxisAngle(0, 1, 0, angleY);
    return qy.multiply(qx);
  }

  /**
   * Hamilton product: this * other
   */
  public Quaternion multiply(Quaternion q) {
    return new Quaternion(
      w * q.w - x * q.x - y * q.y - z * q.z,
      w * q.x + x * q.w + y * q.z - z * q.y,
      w * q.y - x * q.z + y * q.w + z * q.x,
      w * q.z + x * q.y - y * q.x + z * q.w
    );
  }

  /**
   * Normalize to unit quaternion.
   */
  public Quaternion normalize() {
    double mag = Math.sqrt(w * w + x * x + y * y + z * z);
    if (mag < 1e-10) {
      return new Quaternion(); // identity
    }
    return new Quaternion(w / mag, x / mag, y / mag, z / mag);
  }

  /**
   * Normalize in place and return this.
   */
  public void normalizeInPlace() {
    double mag = Math.sqrt(w * w + x * x + y * y + z * z);
    if (mag < 1e-10) {
      w = 1.0;
      x = 0.0;
      y = 0.0;
      z = 0.0;
    } else {
      w /= mag;
      x /= mag;
      y /= mag;
      z /= mag;
    }
  }

  /**
   * Pre-multiply in place: this = q * this
   */
  public void preMultiplyInPlace(Quaternion q) {
    double nw = q.w * w - q.x * x - q.y * y - q.z * z;
    double nx = q.w * x + q.x * w + q.y * z - q.z * y;
    double ny = q.w * y - q.x * z + q.y * w + q.z * x;
    double nz = q.w * z + q.x * y - q.y * x + q.z * w;
    this.w = nw;
    this.x = nx;
    this.y = ny;
    this.z = nz;
  }

  /**
   * Rotate a 3D point by this quaternion.
   */
  public Vec3 rotate(Vec3 p) {
    // q * p * q^(-1), optimized for unit quaternion (q^(-1) = conjugate)
    // Using the formula: v' = v + 2w(q_xyz × v) + 2(q_xyz × (q_xyz × v))

    // t = 2 * (q_xyz × v)
    double tx = 2.0 * (y * p.z() - z * p.y());
    double ty = 2.0 * (z * p.x() - x * p.z());
    double tz = 2.0 * (x * p.y() - y * p.x());

    // v' = v + w*t + (q_xyz × t)
    return new Vec3(
      p.x() + w * tx + (y * tz - z * ty),
      p.y() + w * ty + (z * tx - x * tz),
      p.z() + w * tz + (x * ty - y * tx)
    );
  }

}

package org.kevinferrare.oldskoolfire.drawable.threed;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the dynamic state and positioning of a 3D object.
 * Uses quaternion for rotation to avoid gimbal lock and support composition.
 * Default values are tuned for normalized meshes (-1..1 range).
 */
public class Transform {

  public static final double DEFAULT_SCALE = 200.0;
  public static final double DEFAULT_TRANSLATE_Z = 3.0;

  @Getter
  @Setter
  private double scale = DEFAULT_SCALE;
  private final Quaternion orientation = new Quaternion();
  private Vec3 translation = new Vec3(0, 0, DEFAULT_TRANSLATE_Z);

  public void incScale(double value) {
    setScale(scale + value);
  }

  public Quaternion getOrientation() {
    return orientation;
  }

  /**
   * Rotate by delta angles around world X and Y axes.
   * This matches the original Euler behavior for automatic animation.
   */
  public void rotate(double dx, double dy) {
    Quaternion delta = Quaternion.fromEulerXY(dx, dy);
    // Apply delta in world space (pre-multiply)
    orientation.preMultiplyInPlace(delta);
    // Normalize periodically to prevent drift
    orientation.normalizeInPlace();
  }

  public Vec3 getTranslation() {
    return translation;
  }

  public void setTranslateZ(double z) {
    this.translation = new Vec3(translation.x(), translation.y(), z);
  }

  public void incTranslateZ(double value) {
    setTranslateZ(translation.z() + value);
  }
}

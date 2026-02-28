package org.kevinferrare.oldskoolfire.drawable.threed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuaternionTest {

  private static final double EPSILON = 1e-9;

  @Test
  void identityQuaternionDoesNotRotateVector() {
    Quaternion identity = new Quaternion();
    Vec3 v = new Vec3(1, 2, 3);
    Vec3 rotated = identity.rotate(v);
    assertEquals(v.x(), rotated.x(), EPSILON);
    assertEquals(v.y(), rotated.y(), EPSILON);
    assertEquals(v.z(), rotated.z(), EPSILON);
  }

  @Test
  void fromAxisAngleY90RotatesXtoZ() {
    Quaternion q = Quaternion.fromAxisAngle(0, 1, 0, Math.PI / 2);
    Vec3 rotated = q.rotate(new Vec3(1, 0, 0));
    assertEquals(0, rotated.x(), EPSILON);
    assertEquals(0, rotated.y(), EPSILON);
    assertEquals(-1, rotated.z(), EPSILON);
  }

  @Test
  void fromAxisAngleX180NegatesYandZ() {
    Quaternion q = Quaternion.fromAxisAngle(1, 0, 0, Math.PI);
    Vec3 rotated = q.rotate(new Vec3(0, 1, 1));
    assertEquals(0, rotated.x(), EPSILON);
    assertEquals(-1, rotated.y(), EPSILON);
    assertEquals(-1, rotated.z(), EPSILON);
  }

  @Test
  void fromEulerXY_0_0_isIdentity() {
    Quaternion q = Quaternion.fromEulerXY(0, 0);
    assertEquals(1, q.w(), EPSILON);
    assertEquals(0, q.x(), EPSILON);
    assertEquals(0, q.y(), EPSILON);
    assertEquals(0, q.z(), EPSILON);
  }

  @Test
  void multiplyIdentityTimesQequalsQ() {
    Quaternion identity = new Quaternion();
    Quaternion q = Quaternion.fromAxisAngle(0, 1, 0, 1.0);
    Quaternion result = identity.multiply(q);
    assertEquals(q.w(), result.w(), EPSILON);
    assertEquals(q.x(), result.x(), EPSILON);
    assertEquals(q.y(), result.y(), EPSILON);
    assertEquals(q.z(), result.z(), EPSILON);
  }

  @Test
  void twoNinetyDegreeRotationsEquals180() {
    Quaternion q90 = Quaternion.fromAxisAngle(0, 1, 0, Math.PI / 2);
    Quaternion q180 = q90.multiply(q90);
    Quaternion expected = Quaternion.fromAxisAngle(0, 1, 0, Math.PI);
    // Rotation should produce same result on a test vector
    Vec3 v = new Vec3(1, 0, 0);
    Vec3 rComposed = q180.rotate(v);
    Vec3 rDirect = expected.rotate(v);
    assertEquals(rDirect.x(), rComposed.x(), EPSILON);
    assertEquals(rDirect.y(), rComposed.y(), EPSILON);
    assertEquals(rDirect.z(), rComposed.z(), EPSILON);
  }

  @Test
  void normalizeInPlaceIsIdempotentOnUnitQuaternion() {
    Quaternion q = Quaternion.fromAxisAngle(0, 1, 0, 1.0);
    double wBefore = q.w(), xBefore = q.x(), yBefore = q.y(), zBefore = q.z();
    q.normalizeInPlace();
    assertEquals(wBefore, q.w(), EPSILON);
    assertEquals(xBefore, q.x(), EPSILON);
    assertEquals(yBefore, q.y(), EPSILON);
    assertEquals(zBefore, q.z(), EPSILON);
  }

  @Test
  void normalizeInPlaceOnScaledQuaternionProducesUnitLength() {
    Quaternion q = new Quaternion(2, 0, 0, 0);
    q.normalizeInPlace();
    double mag = Math.sqrt(q.w() * q.w() + q.x() * q.x() + q.y() * q.y() + q.z() * q.z());
    assertEquals(1.0, mag, EPSILON);
  }

  @Test
  void rotatePreservesVectorLength() {
    Quaternion q = Quaternion.fromAxisAngle(1, 1, 1, 0.7).normalize();
    Vec3 v = new Vec3(3, 4, 5);
    Vec3 rotated = q.rotate(v);
    assertEquals(v.length(), rotated.length(), EPSILON);
  }
}

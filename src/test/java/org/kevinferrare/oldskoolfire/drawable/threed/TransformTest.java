package org.kevinferrare.oldskoolfire.drawable.threed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TransformTest {

  private static final double EPSILON = 1e-9;

  @Test
  void rotateZeroProducesNoChange() {
    Transform t = new Transform();
    Quaternion before = new Quaternion();

    t.rotate(0.0, 0.0);

    Quaternion after = t.getOrientation();
    assertEquals(before.w(), after.w(), EPSILON);
    assertEquals(before.x(), after.x(), EPSILON);
    assertEquals(before.y(), after.y(), EPSILON);
    assertEquals(before.z(), after.z(), EPSILON);
  }

  @Test
  void rotateNonZeroChangesOrientation() {
    Transform t = new Transform();

    t.rotate(0.1, 0.2);

    Quaternion q = t.getOrientation();
    assertNotEquals(1.0, q.w(), EPSILON, "Orientation should have changed from identity");
  }

  @Test
  void twoSmallRotationsComposeCorrectly() {
    Transform a = new Transform();
    Transform b = new Transform();

    // Single rotation of 0.01
    a.rotate(0.01, 0.0);

    // Two rotations of 0.005
    b.rotate(0.005, 0.0);
    b.rotate(0.005, 0.0);

    Quaternion qa = a.getOrientation();
    Quaternion qb = b.getOrientation();
    assertEquals(qa.w(), qb.w(), EPSILON);
    assertEquals(qa.x(), qb.x(), EPSILON);
    assertEquals(qa.y(), qb.y(), EPSILON);
    assertEquals(qa.z(), qb.z(), EPSILON);
  }
}

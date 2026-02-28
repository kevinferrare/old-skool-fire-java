package org.kevinferrare.oldskoolfire.drawable.threed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Vec3Test {

  private static final double EPSILON = 1e-9;

  @Test
  void addReturnsSumOfComponents() {
    Vec3 result = new Vec3(1, 2, 3).add(new Vec3(4, 5, 6));
    assertEquals(5, result.x(), EPSILON);
    assertEquals(7, result.y(), EPSILON);
    assertEquals(9, result.z(), EPSILON);
  }

  @Test
  void lengthOfUnitAxisVectorIsOne() {
    assertEquals(1.0, new Vec3(1, 0, 0).length(), EPSILON);
    assertEquals(1.0, new Vec3(0, 1, 0).length(), EPSILON);
    assertEquals(1.0, new Vec3(0, 0, 1).length(), EPSILON);
  }

  @Test
  void lengthOf3_4_0Is5() {
    assertEquals(5.0, new Vec3(3, 4, 0).length(), EPSILON);
  }

  @Test
  void zeroConstantHasAllComponentsZero() {
    assertEquals(0, Vec3.ZERO.x(), EPSILON);
    assertEquals(0, Vec3.ZERO.y(), EPSILON);
    assertEquals(0, Vec3.ZERO.z(), EPSILON);
  }
}

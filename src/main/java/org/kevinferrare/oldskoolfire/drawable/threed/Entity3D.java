package org.kevinferrare.oldskoolfire.drawable.threed;

import org.kevinferrare.oldskoolfire.drawable.brush.Material;
import org.kevinferrare.oldskoolfire.util.Named;

/**
 * A composite 3D object that brings together geometry, spatial state, and appearance.
 */
public record Entity3D(String name, Mesh mesh, Transform transform, Material material) implements Named {
}

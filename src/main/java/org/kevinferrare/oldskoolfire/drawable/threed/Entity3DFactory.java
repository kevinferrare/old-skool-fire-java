package org.kevinferrare.oldskoolfire.drawable.threed;

import lombok.extern.slf4j.Slf4j;
import org.kevinferrare.oldskoolfire.drawable.brush.Material;
import org.kevinferrare.oldskoolfire.drawable.threed.objects.MeshNormalizer;

/**
 * Factory for creating Entity3D instances with normalization and logging.
 */
@Slf4j
public class Entity3DFactory {

  private final MeshNormalizer normalizer = new MeshNormalizer();

  /**
   * Creates an Entity3D from a mesh, normalizing it to fit -1..1 on all axes.
   */
  public Entity3D create(String name, Mesh mesh) {
    log.debug("Normalizing '{}'...", name);
    Mesh normalized = normalizer.normalize(mesh);
    log.info("Loaded entity '{}': {} vertices, {} lines",
      name, normalized.vertices().length, normalized.lines().length / 2);
    return new Entity3D(name, normalized, new Transform(), Material.forMesh(normalized));
  }

  /**
   * Creates an Entity3D with an initial scale already applied.
   */
  public Entity3D createScaled(String name, Mesh mesh, double initialScale) {
    Entity3D entity = create(name, mesh);
    entity.transform().setScale(initialScale);
    return entity;
  }
}

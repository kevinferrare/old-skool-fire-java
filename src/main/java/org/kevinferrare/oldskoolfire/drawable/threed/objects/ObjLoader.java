package org.kevinferrare.oldskoolfire.drawable.threed.objects;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjReader;
import org.kevinferrare.oldskoolfire.drawable.threed.Mesh;
import org.kevinferrare.oldskoolfire.drawable.threed.Vec3;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads wireframe meshes from OBJ files using the javaobj library.
 * Converts faces to wireframe edges with deduplication.
 */
public class ObjLoader {

  /**
   * Loads a mesh from a resource path (relative to classpath).
   */
  public Mesh loadResource(String resourcePath) {
    try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
      if (is == null) {
        throw new IllegalArgumentException("Resource not found: " + resourcePath);
      }
      return load(is);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load OBJ resource: " + resourcePath, e);
    }
  }

  /**
   * Loads a mesh from an external file path.
   */
  public Mesh loadFile(Path filePath) {
    try (InputStream is = Files.newInputStream(filePath)) {
      return load(is);
    } catch (IOException e) {
      throw new RuntimeException("Failed to load OBJ file: " + filePath, e);
    }
  }

  /**
   * Loads a mesh from an input stream.
   */
  public Mesh load(InputStream is) throws IOException {
    Obj obj = ObjReader.read(is);
    return convertToWireframe(obj);
  }

  private Mesh convertToWireframe(Obj obj) {
    // Extract vertices (negate Y to convert from OBJ's Y-up to screen Y-down)
    Vec3[] vertices = new Vec3[obj.getNumVertices()];
    for (int i = 0; i < obj.getNumVertices(); i++) {
      FloatTuple v = obj.getVertex(i);
      vertices[i] = new Vec3(v.getX(), -v.getY(), v.getZ());
    }

    // Extract edges from faces with deduplication
    Set<Long> edgeSet = new HashSet<>();
    List<int[]> lineSegments = new ArrayList<>();

    for (int f = 0; f < obj.getNumFaces(); f++) {
      ObjFace face = obj.getFace(f);
      int numVertices = face.getNumVertices();
      for (int i = 0; i < numVertices; i++) {
        int v1 = face.getVertexIndex(i);
        int v2 = face.getVertexIndex((i + 1) % numVertices);
        addEdge(edgeSet, lineSegments, v1, v2);
      }
    }

    int[] lines = new int[lineSegments.size() * 2];
    for (int i = 0; i < lineSegments.size(); i++) {
      lines[i * 2] = lineSegments.get(i)[0];
      lines[i * 2 + 1] = lineSegments.get(i)[1];
    }

    return new Mesh(vertices, lines);
  }

  private void addEdge(Set<Long> edgeSet, List<int[]> lineSegments, int v1, int v2) {
    int min = Math.min(v1, v2);
    int max = Math.max(v1, v2);
    long key = ((long) min << 32) | (max & 0xFFFFFFFFL);
    if (edgeSet.add(key)) {
      lineSegments.add(new int[]{v1, v2});
    }
  }
}

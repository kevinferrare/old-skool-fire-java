package org.kevinferrare.oldskoolfire.renderer;

import lombok.Setter;
import org.kevinferrare.oldskoolfire.drawable.FixedIntSurface;
import org.kevinferrare.oldskoolfire.drawable.LineDrawer;
import org.kevinferrare.oldskoolfire.drawable.LineRasterizer;
import org.kevinferrare.oldskoolfire.drawable.threed.Entity3D;
import org.kevinferrare.oldskoolfire.drawable.threed.Graphics3D;
import org.kevinferrare.oldskoolfire.palette.Palette;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base class for scene renderers providing shared state management.
 * Subclasses implement different visualization modes (fire effect, wireframe, etc.)
 * Uses triple buffering for lock-free synchronization:
 * - backSurface: simulation writes here exclusively
 * - readySurface: most recently completed frame (atomic reference)
 * - renderSurface: render's private buffer, exchanged atomically with ready
 */
public abstract class SceneRenderer {

  private final AtomicReference<FixedIntSurface> readySurface = new AtomicReference<>();
  private FixedIntSurface renderSurface;            // render thread only
  protected FixedIntSurface backSurface;            // simulation thread only
  protected Graphics3D graphics3D;                  // cached, recreated on surface change
  protected LineDrawer lineDrawer;                   // cached, recreated on surface change
  @Setter
  protected Palette palette;
  protected int[] imageBuffer;

  public void setBufferedImage(BufferedImage bufferedImage) {
    initImageBuffer(bufferedImage);
    int width = bufferedImage.getWidth();
    int height = bufferedImage.getHeight();
    // Triple buffer: 3 separate surfaces, never shared
    this.backSurface = createSurface(width, height);
    this.readySurface.set(createSurface(width, height));
    this.renderSurface = createSurface(width, height);
    this.graphics3D = new Graphics3D(width, height);
    this.lineDrawer = new LineRasterizer(backSurface);
  }

  /**
   * Creates a rendering surface. Override to customize dimensions.
   */
  protected FixedIntSurface createSurface(int width, int height) {
    return new FixedIntSurface(new int[width * height], width, height);
  }

  /**
   * Publishes backSurface, gets a recycled buffer back. Lock-free.
   * Simulation calls this after completing a frame.
   */
  public void swapBuffers() {
    // Atomically exchange: publish our completed back, get the old ready to reuse
    FixedIntSurface oldReady = readySurface.getAndSet(backSurface);
    backSurface = oldReady;  // recycle for next frame
    lineDrawer = new LineRasterizer(backSurface);
  }

  /**
   * Returns the ready surface for reading (used by FireSceneRenderer for convolution source).
   */
  protected FixedIntSurface frontSurface() {
    return readySurface.get();
  }

  /**
   * Creates the standard image buffer from a BufferedImage.
   */
  private void initImageBuffer(BufferedImage bufferedImage) {
    this.imageBuffer = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();
  }

  /**
   * Draws the given shape onto the back surface.
   */
  protected void drawShape(Entity3D shape) {
    graphics3D.drawEntity(lineDrawer, shape);
  }

  /**
   * Updates the scene state with the given shape. Writes to backSurface.
   */
  public abstract void update(Entity3D shape);

  /**
   * Renders the scene to the output buffer. Lock-free.
   * Exchanges renderSurface with readySurface to grab the latest completed frame.
   */
  public void render() {
    // Atomically exchange: give back our old frame, get latest
    FixedIntSurface latest = readySurface.getAndSet(renderSurface);
    renderSurface = latest;
    palette.apply(renderSurface, this.imageBuffer);
  }

  /**
   * Returns the name of this renderer for display purposes.
   */
  public abstract String getName();
}

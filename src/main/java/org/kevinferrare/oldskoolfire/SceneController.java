package org.kevinferrare.oldskoolfire;

import com.google.common.base.Suppliers;
import lombok.extern.slf4j.Slf4j;
import org.kevinferrare.oldskoolfire.drawable.threed.Entity3D;
import org.kevinferrare.oldskoolfire.drawable.threed.Entity3DFactory;
import org.kevinferrare.oldskoolfire.drawable.threed.objects.MeshGenerator;
import org.kevinferrare.oldskoolfire.drawable.threed.objects.ObjLoader;
import org.kevinferrare.oldskoolfire.palette.Palette;
import org.kevinferrare.oldskoolfire.palette.PaletteFactory;
import org.kevinferrare.oldskoolfire.renderer.FireSceneRenderer;
import org.kevinferrare.oldskoolfire.renderer.SceneRenderer;
import org.kevinferrare.oldskoolfire.renderer.WireframeSceneRenderer;
import org.kevinferrare.oldskoolfire.util.Named;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The core engine of the application.
 * Manages:
 * - Scene state (palettes, shapes, parameters)
 * - Threading (simulation and render loops)
 * - Synchronization
 * - Performance monitoring
 */
@Slf4j
public class SceneController {

  /**
   * Pairs a display name with a lazily-created Entity3D.
   * The supplier is memoized so the mesh is loaded/normalized only on first access.
   */
  private record LazyShape(String name, Supplier<Entity3D> entitySupplier) implements Named {
    Entity3D get() {
      return entitySupplier.get();
    }
  }

  private static LazyShape lazyShape(String name, Supplier<Entity3D> factory) {
    return new LazyShape(name, Suppliers.memoize(factory::get));
  }

  /**
   * Radians per second for auto-rotation around X axis (calibrated to ~0.001 rad/tick at 1000 UPS).
   */
  private static final double AUTO_ROTATE_X_SPEED = 1.0;
  /**
   * Radians per second for auto-rotation around Y axis (calibrated to ~0.002 rad/tick at 1000 UPS).
   */
  private static final double AUTO_ROTATE_Y_SPEED = 2.0;
  private static final double NANOS_TO_SECONDS = 1.0 / 1_000_000_000.0;

  private int timeBetweenScenes = 3000;

  private Palette[] palettes;
  private LazyShape[] shapes;

  private static List<Palette> createBuiltInPalettes() {
    return new ArrayList<>(List.of(
      PaletteFactory.createFire(),
      PaletteFactory.createEvil(),
      PaletteFactory.createRockbox(),
      PaletteFactory.createBlueFire(),
      PaletteFactory.createMatrix(),
      PaletteFactory.createPurple(),
      PaletteFactory.createGrayscale()
    ));
  }

  private static List<LazyShape> createBuiltInShapes(double initialScale) {
    Entity3DFactory factory = new Entity3DFactory();
    ObjLoader loader = new ObjLoader();
    List<LazyShape> list = new ArrayList<>();
    list.add(lazyShape("cube", () -> factory.createScaled("cube", MeshGenerator.createCube(), initialScale)));
    list.add(lazyShape("dragon", () -> factory.createScaled("dragon", loader.loadResource("/meshes/dragon.obj"), initialScale)));
    list.add(lazyShape("plane", () -> factory.createScaled("plane", loader.loadResource("/meshes/plane.obj"), initialScale)));
    list.add(lazyShape("teapot", () -> factory.createScaled("teapot", loader.loadResource("/meshes/teapot.obj"), initialScale)));
    list.add(lazyShape("teaspoon", () -> factory.createScaled("teaspoon", loader.loadResource("/meshes/teaspoon.obj"), initialScale)));
    list.add(lazyShape("teacup", () -> factory.createScaled("teacup", loader.loadResource("/meshes/teacup.obj"), initialScale)));
    list.add(lazyShape("pyramid", () -> factory.createScaled("pyramid", MeshGenerator.createPyramid(1.5), initialScale)));
    list.add(lazyShape("torus", () -> factory.createScaled("torus", MeshGenerator.createTorus(0.4, 24, 12), initialScale)));
    list.add(lazyShape("sphere", () -> factory.createScaled("sphere", MeshGenerator.createSphere(12, 16), initialScale)));
    list.add(lazyShape("octahedron", () -> factory.createScaled("octahedron", MeshGenerator.createOctahedron(), initialScale)));
    list.add(lazyShape("icosahedron", () -> factory.createScaled("icosahedron", MeshGenerator.createIcosahedron(), initialScale)));
    list.add(lazyShape("trefoil-knot", () -> factory.createScaled("trefoil-knot", MeshGenerator.createTrefoilKnot(120), initialScale)));
    list.add(lazyShape("mobius-strip", () -> factory.createScaled("mobius-strip", MeshGenerator.createMobiusStrip(0.5, 32, 4), initialScale)));
    list.add(lazyShape("helix", () -> factory.createScaled("helix", MeshGenerator.createHelix(3, 4.0, 80), initialScale)));
    list.add(lazyShape("lissajous", () -> factory.createScaled("lissajous", MeshGenerator.createLissajous(3, 2, 5, Math.PI / 2, 0, 150), initialScale)));
    list.add(lazyShape("star", () -> factory.createScaled("star", MeshGenerator.createStar(5, 0.4, 0.3), initialScale)));
    list.add(lazyShape("none", () -> factory.createScaled("none", MeshGenerator.createNone(), initialScale)));
    return list;
  }

  private static List<LazyShape> loadExternalMeshes(List<Path> meshFiles, double initialScale) {
    Entity3DFactory factory = new Entity3DFactory();
    ObjLoader loader = new ObjLoader();
    List<LazyShape> list = new ArrayList<>();
    for (Path path : meshFiles) {
      String name = path.getFileName().toString().replaceFirst("\\.[^.]+$", "");
      list.add(lazyShape(name, () -> factory.createScaled(name, loader.loadFile(path), initialScale)));
    }
    return list;
  }

  // State
  private int paletteIndex = 0;
  private int shape3dIndex = 0;
  private int rendererIndex = 0;
  private boolean pause = false;
  private boolean autoRotate = true;
  private Long lastSwitchTime;

  // Dependencies / Components
  private SceneRenderer[] renderers;
  private FireSceneRenderer fireRenderer;
  private final PerformanceMonitor performanceMonitor;
  private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

  // Threading
  private volatile boolean running = false;
  private Thread simulationThread;
  private Thread renderThread;

  // Rendering
  private BufferedImage bufferedImage;
  private Consumer<String> onTitleUpdate;
  private String lastStats = "";

  public SceneController() {
    this.performanceMonitor = new PerformanceMonitor();
  }

  public void init(int width, int height, AppConfig config) {
    // Create renderers
    this.fireRenderer = new FireSceneRenderer(config.gpu(), config.noVectorApi());
    this.renderers = new SceneRenderer[]{
      fireRenderer,
      new WireframeSceneRenderer()
    };

    // Set initial renderer based on config
    this.rendererIndex = config.wireframe() ? 1 : 0;

    // Build palettes array
    this.palettes = createBuiltInPalettes().toArray(new Palette[0]);

    // Set initial scale based on window size
    // Using 1.2x fills ~80% of screen height, leaving room for rotation
    double initialScale = Math.min(width, height) * 1.2;

    // Build shapes array: built-in + external meshes (lazy - loaded on first access)
    List<LazyShape> shapeList = createBuiltInShapes(initialScale);
    shapeList.addAll(shapeList.size() - 1, loadExternalMeshes(config.meshFiles(), initialScale)); // Insert before "none"
    this.shapes = shapeList.toArray(new LazyShape[0]);

    // Apply initial configuration
    this.paletteIndex = findPaletteIndex(config.palette());
    this.shape3dIndex = findShapeIndex(config.shape());
    this.autoRotate = config.autoRotate();
    this.pause = config.paused();

    // Apply fire effect settings
    this.fireRenderer.setCooling(config.cooling());
    this.fireRenderer.setStillFireBottom(config.stillFire());

    // Apply auto-switch settings
    if (config.autoSwitch()) {
      this.lastSwitchTime = System.currentTimeMillis() - config.switchInterval();
    }
    this.timeBetweenScenes = config.switchInterval();

    this.updatePalette();
    this.resize(width, height);
  }

  private int findPaletteIndex(String name) {
    int index = Named.findIndex(palettes, name);
    if (index == 0 && !palettes[0].name().equalsIgnoreCase(name)) {
      log.warn("Unknown palette '{}', using '{}'", name, palettes[0].name());
    }
    return index;
  }

  private int findShapeIndex(String name) {
    int index = Named.findIndex(shapes, name);
    if (index == 0 && !shapes[0].name().equalsIgnoreCase(name)) {
      log.warn("Unknown shape '{}', using first available", name);
    }
    return index;
  }

  public void resize(int width, int height) {
    withWriteLock(() -> {
      this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      for (SceneRenderer renderer : renderers) {
        renderer.setBufferedImage(bufferedImage);
      }
      performanceMonitor.reset();
    });
  }

  public void start(int refreshRate, Runnable onRender, Consumer<String> onTitleUpdate) {
    if (running) {
      return;
    }
    running = true;
    this.onTitleUpdate = onTitleUpdate;
    updateTitle();

    // Simulation Thread
    simulationThread = new Thread(() -> {
      long lastNanoTime = System.nanoTime();
      while (running) {
        long now = System.nanoTime();
        double deltaSec = (now - lastNanoTime) * NANOS_TO_SECONDS;
        lastNanoTime = now;
        if (calculate(deltaSec)) {
          performanceMonitor.recordUpdate();
        } else {
          try {
            Thread.sleep(100);
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            break;
          }
        }
      }
    }, "SimulationLoop");
    simulationThread.start();

    // Rendering Thread
    renderThread = new Thread(() -> {
      int sleepDuration = refreshRate > 0 ? 1000 / refreshRate : 16;

      while (running) {
        render();
        performanceMonitor.recordFrame();

        onRender.run();

        String stats = performanceMonitor.getStatsAndReset();
        if (stats != null) {
          lastStats = stats;
          updateTitle();
        }

        try {
          Thread.sleep(sleepDuration);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    }, "RenderLoop");
    renderThread.start();
  }

  public void stop() {
    running = false;
    try {
      if (simulationThread != null) simulationThread.join(1000);
      if (renderThread != null) renderThread.join(1000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  // --- Core Loop Methods ---

  private SceneRenderer currentRenderer() {
    return renderers[rendererIndex];
  }

  private boolean calculate(double deltaSec) {
    lock.writeLock().lock();
    try {
      if (pause) {
        return false;
      }
      switchSceneIfNeeded();
      if (autoRotate && deltaSec > 0.0) {
        double dx = AUTO_ROTATE_X_SPEED * deltaSec;
        double dy = AUTO_ROTATE_Y_SPEED * deltaSec;
        withShape(shape -> shape.transform().rotate(dx, dy));
      }
      // Downgrade to read lock for the heavy compute - lets render() run in parallel
      lock.readLock().lock();
    } finally {
      lock.writeLock().unlock();
    }
    try {
      currentRenderer().update(getShape3d());
      currentRenderer().swapBuffers();
      return true;
    } finally {
      lock.readLock().unlock();
    }
  }

  private void render() {
    withReadLock(() -> currentRenderer().render());
  }

  // --- Public Command Interface (For InputHandler) ---

  public void nextPalette() {
    withWriteLock(() -> incPaletteIndex(1));
  }

  public void previousPalette() {
    withWriteLock(() -> incPaletteIndex(-1));
  }

  public void nextShape() {
    withWriteLock(() -> incShape3dIndex(1));
  }

  public void previousShape() {
    withWriteLock(() -> incShape3dIndex(-1));
  }

  public void increaseScale(double amount) {
    withWriteLock(() -> incScale(amount));
  }

  public void increaseZ(double amount) {
    withWriteLock(() -> inczOffset(amount));
  }

  public void toggleStillShape() {
    withWriteLock(this::toggleStillShapeInternal);
  }

  public void toggleStillFireBottom() {
    withWriteLock(() -> fireRenderer.toggleStillFireBottom());
  }

  public void rotateShape(double dx, double dy) {
    withWriteLock(() -> withShape(shape -> shape.transform().rotate(dy * 0.01, dx * 0.01)));
  }

  public void toggleAutoRotate() {
    withWriteLock(() -> autoRotate = !autoRotate);
  }

  public void toggleAutomaticSceneSwitch() {
    withWriteLock(this::toggleAutomaticSceneSwitchInternal);
  }

  public void increaseCooling() {
    withWriteLock(() -> fireRenderer.incCooling(1));
  }

  public void decreaseCooling() {
    withWriteLock(() -> fireRenderer.incCooling(-1));
  }

  public void toggleRenderer() {
    withWriteLock(() -> {
      rendererIndex = wrapIndex(rendererIndex, 1, renderers.length);
      updateTitle();
    });
  }

  public void togglePause() {
    withWriteLock(() -> pause = !pause);
  }

  // --- Internal Logic ---

  private void withWriteLock(Runnable action) {
    lock.writeLock().lock();
    try {
      action.run();
    } finally {
      lock.writeLock().unlock();
    }
  }

  private void withReadLock(Runnable action) {
    lock.readLock().lock();
    try {
      action.run();
    } finally {
      lock.readLock().unlock();
    }
  }

  private void updatePalette() {
    for (SceneRenderer renderer : renderers) {
      renderer.setPalette(palettes[paletteIndex]);
    }
  }

  private Entity3D getShape3d() {
    return shapes[shape3dIndex].get();
  }

  public String getCurrentPaletteName() {
    return palettes[paletteIndex].name();
  }

  public String getCurrentShapeName() {
    return shapes[shape3dIndex].name();
  }

  private void withShape(Consumer<Entity3D> action) {
    action.accept(getShape3d());
  }

  private int wrapIndex(int current, int delta, int length) {
    return ((current + delta) % length + length) % length;
  }

  private void updateTitle() {
    if (onTitleUpdate != null) {
      String title = String.format("%s | %s | %s | %s | %s",
        currentRenderer().getName(), fireRenderer.getStrategyName(), getCurrentShapeName(), getCurrentPaletteName(), lastStats);
      onTitleUpdate.accept(title);
    }
  }

  private void incScale(double value) {
    withShape(shape -> shape.transform().incScale(value));
  }

  private void toggleStillShapeInternal() {
    withShape(shape -> shape.material().setRandomPixel(!shape.material().isRandomPixel()));
  }

  private void inczOffset(double value) {
    withShape(shape -> shape.transform().incTranslateZ(value));
  }

  private void incPaletteIndex(int value) {
    paletteIndex = wrapIndex(paletteIndex, value, palettes.length);
    this.updatePalette();
    updateTitle();
  }

  private void incShape3dIndex(int value) {
    shape3dIndex = wrapIndex(shape3dIndex, value, shapes.length);
    updateTitle();
  }

  private void toggleAutomaticSceneSwitchInternal() {
    if (lastSwitchTime == null) {
      lastSwitchTime = System.currentTimeMillis() - timeBetweenScenes;
    } else {
      lastSwitchTime = null;
    }
  }

  private void switchSceneIfNeeded() {
    if (lastSwitchTime == null) {
      return;
    }
    long currentTime = System.currentTimeMillis();
    if (currentTime - lastSwitchTime < timeBetweenScenes) {
      return;
    }
    lastSwitchTime = currentTime;
    incPaletteIndex(1);
    if (paletteIndex == 0) {
      incShape3dIndex(1);
    }
  }

  public BufferedImage getBufferedImage() {
    return bufferedImage;
  }
}

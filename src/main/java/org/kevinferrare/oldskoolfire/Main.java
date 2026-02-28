package org.kevinferrare.oldskoolfire;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Application entry point.
 * Responsible for parsing command line arguments and launching the UI.
 */
@Command(
  name = "oldskoolfire",
  mixinStandardHelpOptions = true,
  version = "1.0",
  description = "Retro fire effect demo with 3D wireframe objects"
)
public class Main implements Runnable {

  // Display settings
  @Option(names = {"-W", "--width"}, description = "Initial window width (default: ${DEFAULT-VALUE})")
  private int width = AppConfig.DEFAULT_WIDTH;

  @Option(names = {"-H", "--height"}, description = "Initial window height (default: ${DEFAULT-VALUE})")
  private int height = AppConfig.DEFAULT_HEIGHT;

  @Option(names = {"-f", "--fullscreen"}, description = "Start in fullscreen mode")
  private boolean fullscreen = false;

  // Visual settings
  @Option(names = {"-p", "--palette"}, description = "Initial palette: fire, evil, rockbox (default: ${DEFAULT-VALUE})")
  private String palette = AppConfig.DEFAULT_PALETTE;

  @Option(names = {"-s", "--shape"}, description = "Initial 3D shape name (default: ${DEFAULT-VALUE})")
  private String shape = AppConfig.DEFAULT_SHAPE;

  @Option(names = {"-c", "--cooling"}, description = "Fire cooling factor (default: ${DEFAULT-VALUE})")
  private int cooling = AppConfig.DEFAULT_COOLING;

  // External mesh loading
  @Option(names = {"-m", "--mesh"}, description = "OBJ file(s) to load", arity = "1..*")
  private List<Path> meshFiles = new ArrayList<>();

  // Animation settings
  @Option(names = {"--auto-rotate"}, description = "Start with auto-rotation enabled")
  private boolean autoRotate = true;

  @Option(names = {"--auto-switch"}, description = "Enable automatic scene switching")
  private boolean autoSwitch = false;

  @Option(names = {"--switch-interval"}, description = "Time between scene switches in ms (default: ${DEFAULT-VALUE})")
  private int switchInterval = AppConfig.DEFAULT_SWITCH_INTERVAL;

  @Option(names = {"--paused"}, description = "Start in paused state")
  private boolean paused = false;

  // Fire effect settings
  @Option(names = {"--still-fire"}, description = "Use fixed intensity fire (no flicker)")
  private boolean stillFire = false;

  // Performance settings
  @Option(names = {"--gpu"}, description = "Enable GPU-accelerated convolution via TornadoVM (requires TornadoVM runtime)")
  private boolean gpu = false;

  @Option(names = {"--no-vector-api"}, description = "Disable SIMD Vector API for convolution (use scalar fallback)")
  private boolean noVectorApi = false;

  // Renderer settings
  @Option(names = {"-w", "--wireframe"}, description = "Start in wireframe mode (no fire effect)")
  private boolean wireframe = false;

  public static void main(String[] args) {
    int exitCode = new CommandLine(new Main()).execute(args);
    if (exitCode != 0) {
      System.exit(exitCode);
    }
  }

  @Override
  public void run() {
    AppConfig config = new AppConfig(
      width,
      height,
      fullscreen,
      palette,
      shape,
      cooling,
      meshFiles,
      autoRotate,
      autoSwitch,
      switchInterval,
      paused,
      stillFire,
      gpu,
      noVectorApi,
      wireframe
    );
    new AppFrame(config).setVisible(true);
  }
}

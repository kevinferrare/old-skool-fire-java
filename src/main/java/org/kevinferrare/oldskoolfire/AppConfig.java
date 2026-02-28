package org.kevinferrare.oldskoolfire;

import java.nio.file.Path;
import java.util.List;

/**
 * Application configuration populated from command line arguments.
 */
public record AppConfig(
  // Display settings
  int width,
  int height,
  boolean fullscreen,

  // Visual settings
  String palette,
  String shape,
  int cooling,

  // External mesh loading
  List<Path> meshFiles,

  // Animation settings
  boolean autoRotate,
  boolean autoSwitch,
  int switchInterval,
  boolean paused,

  // Fire effect settings
  boolean stillFire,

  // Performance settings
  boolean gpu,
  boolean noVectorApi,

  // Renderer settings
  boolean wireframe
) {

  // Default values as constants for use by CLI parser
  public static final int DEFAULT_WIDTH = 1000;
  public static final int DEFAULT_HEIGHT = 600;
  public static final String DEFAULT_PALETTE = "fire";
  public static final String DEFAULT_SHAPE = "cube";
  public static final int DEFAULT_COOLING = 12;
  public static final int DEFAULT_SWITCH_INTERVAL = 3000;
}

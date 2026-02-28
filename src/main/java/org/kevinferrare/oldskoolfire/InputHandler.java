package org.kevinferrare.oldskoolfire;

import lombok.extern.slf4j.Slf4j;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Translates raw UI events into semantic commands for the {@link SceneController}.
 * Decouples the UI layer from the application logic.
 */
@Slf4j
public class InputHandler {

  private final SceneController controller;
  private final Runnable onExit;

  public InputHandler(SceneController controller, Runnable onExit) {
    this.controller = controller;
    this.onExit = onExit;
    logShortcuts();
  }

  private void logShortcuts() {
    log.info("=== Keyboard & Mouse Shortcuts ===");
    log.info("  Left/Right Arrow : Previous/Next shape");
    log.info("  Up/Down Arrow    : Next/Previous palette");
    log.info("  Mouse Wheel      : Next/Previous palette");
    log.info("  Left/Right Click : Next/Previous shape");
    log.info("  Mouse Drag       : Rotate shape");
    log.info("  Numpad +/-       : Increase/Decrease scale");
    log.info("  Numpad *//       : Increase/Decrease Z depth");
    log.info("  PageUp/PageDown  : Increase/Decrease cooling");
    log.info("  C                : Toggle still shape");
    log.info("  B                : Toggle still fire bottom");
    log.info("  R                : Toggle auto-rotate");
    log.info("  W                : Toggle renderer (fire/wireframe)");
    log.info("  Ctrl             : Toggle automatic scene switch");
    log.info("  Space            : Pause/Resume");
    log.info("  Escape / Q       : Quit");
    log.info("==================================");
  }

  public void onMouseWheel(int rotation) {
    if (rotation < 0) {
      controller.nextPalette();
    } else {
      controller.previousPalette();
    }
  }

  public void onMouseClicked(int button) {
    switch (button) {
      case MouseEvent.BUTTON1:
        controller.nextShape();
        break;
      case MouseEvent.BUTTON3:
      default:
        controller.previousShape();
        break;
    }
  }

  public void onMouseDragged(int dx, int dy) {
    controller.rotateShape(dx, dy);
  }

  public void onKeyPressed(int keyCode) {
    switch (keyCode) {
      case KeyEvent.VK_ADD:
        controller.increaseScale(50);
        break;
      case KeyEvent.VK_SUBTRACT:
        controller.increaseScale(-50);
        break;
      case KeyEvent.VK_MULTIPLY:
        controller.increaseZ(1);
        break;
      case KeyEvent.VK_DIVIDE:
        controller.increaseZ(-1);
        break;
      case KeyEvent.VK_C:
        controller.toggleStillShape();
        break;
      case KeyEvent.VK_B:
        controller.toggleStillFireBottom();
        break;
      case KeyEvent.VK_R:
        controller.toggleAutoRotate();
        break;
      case KeyEvent.VK_CONTROL:
        controller.toggleAutomaticSceneSwitch();
        break;
      case KeyEvent.VK_LEFT:
        controller.previousShape();
        break;
      case KeyEvent.VK_RIGHT:
        controller.nextShape();
        break;
      case KeyEvent.VK_UP:
        controller.nextPalette();
        break;
      case KeyEvent.VK_DOWN:
        controller.previousPalette();
        break;
      case KeyEvent.VK_PAGE_UP:
        controller.increaseCooling();
        break;
      case KeyEvent.VK_PAGE_DOWN:
        controller.decreaseCooling();
        break;
      case KeyEvent.VK_W:
        controller.toggleRenderer();
        break;
      case KeyEvent.VK_SPACE:
        controller.togglePause();
        break;
      case KeyEvent.VK_ESCAPE:
      case KeyEvent.VK_Q:
        onExit.run();
        break;
    }
  }
}

package org.kevinferrare.oldskoolfire;

import java.awt.*;
import java.awt.event.*;

/**
 * Main UI frame for the application.
 * Focuses on presentation concerns: window management, event handling, and rendering.
 * Delegates application logic to {@link SceneController}.
 */
@SuppressWarnings("serial")
public class AppFrame extends Frame {

  private final SceneController controller;
  private final InputHandler inputHandler;

  public AppFrame(AppConfig config) {
    this.controller = new SceneController();
    this.inputHandler = new InputHandler(controller, this::exit);

    setLocationRelativeTo(null);
    setSize(config.width(), config.height());

    if (config.fullscreen()) {
      setExtendedState(Frame.MAXIMIZED_BOTH);
      setUndecorated(true);
    }

    initWindowCloseHandling();
    initWindowResizeHandling();
    initMouseWheelHandling();
    initMouseClickHandling();
    initMouseDragHandling();
    initKeyboardHandling();

    controller.init(this.getWidth(), this.getHeight(), config);

    // Start application with callbacks for rendering and stats updates
    int refreshRate = getGraphicsConfiguration().getDevice().getDisplayMode().getRefreshRate();
    if (refreshRate <= 0) {
      refreshRate = 60;
    }

    controller.start(
      refreshRate,
      this::repaint,
      this::setTitle
    );
  }

  private void initWindowCloseHandling() {
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent we) {
        exit();
      }
    });
  }

  private void exit() {
    controller.stop();
    dispose();
  }

  private void initWindowResizeHandling() {
    this.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent componentEvent) {
        Component component = componentEvent.getComponent();
        controller.resize(component.getWidth(), component.getHeight());
      }
    });
  }

  private void initMouseWheelHandling() {
    this.addMouseWheelListener(e ->
      inputHandler.onMouseWheel(e.getWheelRotation()));
  }

  private void initMouseClickHandling() {
    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        inputHandler.onMouseClicked(e.getButton());
      }
    });
  }

  private void initMouseDragHandling() {
    final int[] lastPos = new int[2];
    this.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        lastPos[0] = e.getX();
        lastPos[1] = e.getY();
      }
    });
    this.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseDragged(MouseEvent e) {
        int dx = e.getX() - lastPos[0];
        int dy = e.getY() - lastPos[1];
        lastPos[0] = e.getX();
        lastPos[1] = e.getY();
        inputHandler.onMouseDragged(dx, dy);
      }
    });
  }

  private void initKeyboardHandling() {
    this.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        inputHandler.onKeyPressed(e.getKeyCode());
      }
    });
  }

  @Override
  public void update(Graphics g) {
    g.drawImage(controller.getBufferedImage(), 0, 0, this.getWidth(), this.getHeight(), null);
  }
}

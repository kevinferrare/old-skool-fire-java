package org.kevinferrare.oldskoolfire.renderer;

import org.kevinferrare.oldskoolfire.drawable.ConvolveAndRiseEffect;
import org.kevinferrare.oldskoolfire.drawable.FireSeedLine;
import org.kevinferrare.oldskoolfire.drawable.FixedIntSurface;
import org.kevinferrare.oldskoolfire.drawable.brush.Material;
import org.kevinferrare.oldskoolfire.drawable.threed.Entity3D;

public class FireSceneRenderer extends SceneRenderer {

  private final FireSeedLine fireSeed = new FireSeedLine();

  private final ConvolveAndRiseEffect convolveAndRiseEffect;

  private boolean stillFireBottom = false;

  public FireSceneRenderer(boolean gpu, boolean noVectorApi) {
    this.convolveAndRiseEffect = new ConvolveAndRiseEffect(3, gpu, noVectorApi);
  }

  @Override
  public String getName() {
    return "fire";
  }

  @Override
  protected FixedIntSurface createSurface(int width, int height) {
    // Add one extra row for the fire seed line
    return new FixedIntSurface(new int[width * (height + 1)], width, height);
  }

  public void toggleStillFireBottom() {
    setStillFireBottom(!stillFireBottom);
  }

  public void incCooling(int delta) {
    convolveAndRiseEffect.setCooling(convolveAndRiseEffect.getCooling() + delta);
  }

  public void setCooling(int value) {
    convolveAndRiseEffect.setCooling(value);
  }

  public String getStrategyName() {
    return convolveAndRiseEffect.getStrategyName();
  }

  public void setStillFireBottom(boolean still) {
    stillFireBottom = still;
    Material material = fireSeed.getMaterial();
    // still = fixed mode with full intensity; !still = flicker mode
    material.setRandomPixel(!still);
  }

  @Override
  public void update(Entity3D shape) {
    // Order matters: convolve first (read front, write back), then add new heat sources
    convolveAndRiseEffect.draw(frontSurface(), backSurface);
    drawShape(shape);
    fireSeed.draw(backSurface);
  }
}

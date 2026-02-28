package org.kevinferrare.oldskoolfire.drawable.brush;

import org.kevinferrare.oldskoolfire.util.FastRandom;

/**
 * A stateful pixel strategy that randomly flickers between zero and a maximum value.
 * The flicker effect is controlled by configurable update frequency and threshold parameters.
 *
 * <p><b>Thread safety:</b> This class is <b>not</b> thread-safe. It holds mutable state
 * ({@code counter}, {@code currentValue}) that is updated on every {@link #getPixel()} call
 * without synchronization. Each thread must use its own instance.</p>
 */
public class FlickerPixelStrategy implements PixelStrategy {
  private final int flickerValue;
  private final int updateFrequency;
  private final FastRandom random = new FastRandom();

  private int counter = 0;
  private int currentValue = 0;

  /**
   * Creates a flicker strategy with custom parameters.
   *
   * @param flickerValue    the maximum pixel value when "on"
   * @param updateFrequency how often to recalculate (every N calls)
   */
  public FlickerPixelStrategy(int flickerValue, int updateFrequency) {
    this.flickerValue = flickerValue;
    this.updateFrequency = updateFrequency;
  }

  /**
   * Creates a flicker strategy with default parameters (update every 3 calls, 50% threshold).
   *
   * @param flickerValue the maximum pixel value when "on"
   */
  public FlickerPixelStrategy(int flickerValue) {
    this(flickerValue, 3);
  }

  @Override
  public int getPixel() {
    if (counter % updateFrequency == 0) {
      currentValue = random.nextBoolean() ? flickerValue : 0;
      counter = 0;
    }
    counter++;
    return currentValue;
  }
}

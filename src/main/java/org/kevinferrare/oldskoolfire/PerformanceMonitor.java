package org.kevinferrare.oldskoolfire;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitors and calculates performance metrics (FPS and UPS) for the fire effect application.
 * Thread-safe implementation using atomic operations.
 */
public class PerformanceMonitor {
  private final AtomicInteger framesCount = new AtomicInteger(0);
  private final AtomicInteger updatesCount = new AtomicInteger(0);
  private final AtomicLong firstFrame = new AtomicLong(System.currentTimeMillis());

  /**
   * Records a frame render.
   */
  public void recordFrame() {
    framesCount.incrementAndGet();
  }

  /**
   * Records a simulation update.
   */
  public void recordUpdate() {
    updatesCount.incrementAndGet();
  }

  /**
   * Gets the current performance statistics as a formatted string.
   * Resets counters if more than 1 second has elapsed.
   *
   * @return formatted string like "Fire: 60.0 FPS / 120.0 UPS", or null if not enough time has passed
   */
  public String getStatsAndReset() {
    long current = System.currentTimeMillis();
    long first = firstFrame.get();
    long diff = current - first;

    if (diff > 1000) {
      int frames = framesCount.get();
      int updates = updatesCount.get();

      double fps = 1000.0 * frames / diff;
      double ups = 1000.0 * updates / diff;

      // Reset counters for next period
      firstFrame.set(current);
      framesCount.set(0);
      updatesCount.set(0);

      return String.format("Fire: %.1f FPS / %.1f UPS", fps, ups);
    }

    return null;
  }

  /**
   * Resets all counters and timing.
   */
  public void reset() {
    firstFrame.set(System.currentTimeMillis());
    framesCount.set(0);
    updatesCount.set(0);
  }
}

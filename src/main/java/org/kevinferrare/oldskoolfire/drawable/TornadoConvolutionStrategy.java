package org.kevinferrare.oldskoolfire.drawable;

import lombok.extern.slf4j.Slf4j;
import uk.ac.manchester.tornado.api.ImmutableTaskGraph;
import uk.ac.manchester.tornado.api.TaskGraph;
import uk.ac.manchester.tornado.api.TornadoBackend;
import uk.ac.manchester.tornado.api.TornadoExecutionPlan;
import uk.ac.manchester.tornado.api.annotations.Parallel;
import uk.ac.manchester.tornado.api.common.TornadoDevice;
import uk.ac.manchester.tornado.api.enums.DataTransferMode;

import java.util.List;

/**
 * GPU-accelerated convolution via TornadoVM.
 * Internal buffers decouple the caller's swapping double-buffered surfaces
 * from the fixed array references registered in the TornadoVM TaskGraph.
 * Constructor probes GPU availability - throws if TornadoVM runtime is absent.
 */

@Slf4j
public class TornadoConvolutionStrategy implements ConvolutionStrategy {

  @Override
  public String name() {
    return "GPU (Tornado)";
  }

  private static final int INT_SHIFT = ConvolveAndRiseEffect.RECIPROCAL_SHIFT;

  private int[] gpuSrc;
  private int[] gpuDst;
  private TornadoExecutionPlan executionPlan;
  private int cachedSrcLen;
  private int cachedEnd;
  private int cachedWidth;
  private int cachedReciprocal;

  /**
   * Probes GPU availability by building and executing a small test task graph.
   * Throws if TornadoVM runtime is absent or GPU initialization fails.
   */
  public TornadoConvolutionStrategy() {
    displayRuntimeInfo();
    int probeWidth = 4;
    int probeSize = probeWidth * 5;
    int[] probeSrc = new int[probeSize];
    int[] probeDst = new int[probeSize];

    TaskGraph probeGraph = new TaskGraph("probe")
      .transferToDevice(DataTransferMode.EVERY_EXECUTION, probeSrc)
      .task("t0", TornadoConvolutionStrategy::convolveKernel,
        probeSrc, probeDst, probeWidth, probeWidth, probeWidth - 1,
        probeWidth + 1, probeWidth * 2, 1000, INT_SHIFT)
      .transferToHost(DataTransferMode.EVERY_EXECUTION, probeDst);

    ImmutableTaskGraph snapshot = probeGraph.snapshot();
    try (TornadoExecutionPlan plan = new TornadoExecutionPlan(snapshot)) {
      plan.execute();
    } catch (Exception e) {
      throw new RuntimeException("TornadoVM GPU probe failed", e);
    }
  }

  public static void displayRuntimeInfo() {
    try {
      List<TornadoBackend> backends = TornadoExecutionPlan.getTornadoDeviceMap().getAllBackends();
      for (TornadoBackend backend : backends) {
        System.out.println("Tornado Backend: " + backend.getName());
        List<TornadoDevice> devices = backend.getAllDevices();
        for (TornadoDevice device : devices) {
          System.out.println("  Device: " + device.getDeviceName());
          System.out.println("    Type: " + device.getDeviceType());
          System.out.println("    Description: " + device.getDescription());
          System.out.println("    Memory: " + device.getMaxGlobalMemory());
        }
      }
    } catch (Throwable e) {
      log.error("Could not display device info", e);
    }
  }

  /**
   * GPU kernel: convolves source pixels into destination using the same 5-tap stencil
   * as the scalar/vector strategies. Uses int-only reciprocal multiplication
   * (same reduced precision as VectorConvolutionStrategy).
   */
  static void convolveKernel(int[] src, int[] dst, int end,
                             int width, int widthMinus1, int widthPlus1,
                             int widthTimes2, int intReciprocal, int shift) {
    for (@Parallel int i = 0; i < end; i++) {
      int sum = src[i] + src[i + width] + src[i + widthMinus1]
        + src[i + widthPlus1] + src[i + widthTimes2];
      dst[i] = (sum * intReciprocal) >> shift;
    }
  }

  @Override
  public void convolve(int[] srcData, int[] dstData, int end,
                       int width, int widthMinus1, int widthPlus1,
                       int widthTimes2, int reciprocal) {
    if (end <= 0) {
      return;
    }

    if (needsRebuild(srcData.length, end, width, reciprocal)) {
      rebuildPlan(srcData.length, dstData.length, end,
        width, widthMinus1, widthPlus1, widthTimes2, reciprocal);
    }

    System.arraycopy(srcData, 0, gpuSrc, 0, srcData.length);

    try {
      executionPlan.execute();
    } catch (Exception e) {
      throw new RuntimeException("GPU execution failed", e);
    }

    System.arraycopy(gpuDst, 0, dstData, 0, dstData.length);
  }

  private boolean needsRebuild(int srcLen, int end, int width, int intReciprocal) {
    return executionPlan == null
      || srcLen != cachedSrcLen
      || end != cachedEnd
      || width != cachedWidth
      || intReciprocal != cachedReciprocal;
  }

  private void rebuildPlan(int srcLen, int dstLen, int end, int width,
                           int widthMinus1, int widthPlus1, int widthTimes2,
                           int intReciprocal) {
    if (executionPlan != null) {
      try {
        executionPlan.close();
      } catch (Exception ignored) {
      }
    }

    gpuSrc = new int[srcLen];
    gpuDst = new int[dstLen];

    TaskGraph taskGraph = new TaskGraph("fire")
      .transferToDevice(DataTransferMode.EVERY_EXECUTION, gpuSrc)
      .task("convolve", TornadoConvolutionStrategy::convolveKernel,
        gpuSrc, gpuDst, end, width, widthMinus1, widthPlus1, widthTimes2,
        intReciprocal, INT_SHIFT)
      .transferToHost(DataTransferMode.EVERY_EXECUTION, gpuDst);

    ImmutableTaskGraph snapshot = taskGraph.snapshot();
    try {
      executionPlan = new TornadoExecutionPlan(snapshot);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create GPU execution plan", e);
    }

    cachedSrcLen = srcLen;
    cachedEnd = end;
    cachedWidth = width;
    cachedReciprocal = intReciprocal;
  }
}

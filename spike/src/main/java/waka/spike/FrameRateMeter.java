package waka.spike;

import java.util.Arrays;
import java.util.function.Consumer;

import javafx.animation.AnimationTimer;

/**
 * Probe B and Probe C, measurement half: run a scripted sweep and report min, average and p95
 * frame time.
 *
 * <p>The sweep is scripted and not driven by hand, because 60fps is a number that goes into the
 * stage's evidence and a hand on a mouse wheel is an impression. The caller says what to move on
 * each frame; this class only counts.
 */
public final class FrameRateMeter {

    /** 60 frames per second means a frame has to finish inside this many milliseconds. */
    public static final double SIXTY_FPS_BUDGET_MILLIS = 16.7;

    /**
     * One and a half refreshes at 60Hz. What this class can observe is the gap between pulses,
     * and vsync pins that gap to the refresh period: a flawless run measures about 16.67ms, so a
     * p95 at or under the budget above is close to unreachable by construction. A gap longer
     * than this one means the window manager showed the previous frame twice, which is a dropped
     * frame and the thing a person actually sees.
     */
    public static final double DROPPED_FRAME_MILLIS = 25.0;

    /** The "degraded but still usable" tier recorded next to the ceiling. */
    public static final double THIRTY_FPS_BUDGET_MILLIS = 33.3;

    /**
     * Frames thrown away before measuring starts, so just-in-time warm-up and the first texture
     * uploads do not land in the numbers.
     */
    private static final int WARM_UP_FRAMES = 30;

    /** What the caller moves on each measured frame. */
    public interface FrameAction {
        /**
         * @param progress runs from 0 to 1 and back to 0: top to bottom to top
         * @param progressDelta the change since the previous frame, so an action that scrolls by
         *                      a relative amount knows which way it is going
         */
        void onFrame(double progress, double progressDelta);
    }

    public record Measurement(String label, int frameCount,
                              double minMillis, double averageMillis, double p95Millis,
                              double droppedFrameShare) {

        /** The budget read literally, which vsync makes hard to reach even when nothing is wrong. */
        public boolean holdsStrictSixtyFps() {
            return p95Millis <= SIXTY_FPS_BUDGET_MILLIS;
        }

        /** The budget read as "the viewer sees 60 frames a second", allowing 5% dropped. */
        public boolean holdsSixtyFpsAsSeen() {
            return p95Millis <= DROPPED_FRAME_MILLIS;
        }

        public boolean holdsThirtyFps() {
            return p95Millis <= THIRTY_FPS_BUDGET_MILLIS;
        }

        @Override
        public String toString() {
            return String.format(
                    "%s | min=%5.2fms avg=%6.2fms p95=%7.2fms dropped=%5.1f%% "
                    + "| p95<=16.7ms: %-3s | dropped under 5%%: %-3s",
                    label, minMillis, averageMillis, p95Millis, droppedFrameShare * 100.0,
                    holdsStrictSixtyFps() ? "yes" : "no",
                    holdsSixtyFpsAsSeen() ? "yes" : "no");
        }
    }

    private FrameRateMeter() {
    }

    /**
     * Sweep once and hand the result to {@code whenFinished}. Returns immediately: the sweep runs
     * on the JavaFX application thread, one step per pulse, which is the only place a frame time
     * means anything.
     */
    public static void sweep(String label, int measuredFrames, FrameAction action,
                             Consumer<Measurement> whenFinished) {
        double[] frameMillis = new double[measuredFrames];

        AnimationTimer timer = new AnimationTimer() {
            private int pulseNumber = 0;
            private long previousNanos = 0;
            private double previousProgress = 0.0;

            @Override
            public void handle(long nowNanos) {
                int measuredIndex = pulseNumber - WARM_UP_FRAMES;
                pulseNumber++;

                if (measuredIndex < 0) {
                    previousNanos = nowNanos;
                    return;
                }
                if (measuredIndex >= measuredFrames) {
                    stop();
                    whenFinished.accept(summarise(label, frameMillis));
                    return;
                }

                // The gap since the previous pulse is the cost of the frame that pulse asked for.
                frameMillis[measuredIndex] = (nowNanos - previousNanos) / 1_000_000.0;
                previousNanos = nowNanos;

                double progress = sweepProgress(measuredIndex, measuredFrames);
                action.onFrame(progress, progress - previousProgress);
                previousProgress = progress;
            }
        };
        timer.start();
    }

    /** A triangle: first half scrolls to the bottom, second half scrolls back to the top. */
    private static double sweepProgress(int frameIndex, int frameCount) {
        double half = frameCount / 2.0;
        if (frameIndex < half) {
            return frameIndex / half;
        }
        return (frameCount - frameIndex) / half;
    }

    private static Measurement summarise(String label, double[] frameMillis) {
        double[] sorted = frameMillis.clone();
        Arrays.sort(sorted);

        double total = 0.0;
        int droppedFrames = 0;
        for (double millis : sorted) {
            total += millis;
            if (millis > DROPPED_FRAME_MILLIS) {
                droppedFrames++;
            }
        }
        int p95Index = (int) Math.ceil(sorted.length * 0.95) - 1;

        return new Measurement(label, sorted.length,
                sorted[0], total / sorted.length, sorted[p95Index],
                (double) droppedFrames / sorted.length);
    }
}

package com.android.systemui.classifier.brightline;

import android.view.MotionEvent;
import android.view.VelocityTracker;
import com.android.systemui.util.DeviceConfigProxy;
import java.util.List;

/* access modifiers changed from: package-private */
public class DistanceClassifier extends FalsingClassifier {
    private DistanceVectors mCachedDistance;
    private boolean mDistanceDirty = true;
    private final float mHorizontalFlingThresholdPx;
    private final float mHorizontalSwipeThresholdPx;
    private final float mVelocityToDistanceMultiplier;
    private final float mVerticalFlingThresholdPx;
    private final float mVerticalSwipeThresholdPx;

    DistanceClassifier(FalsingDataProvider falsingDataProvider, DeviceConfigProxy deviceConfigProxy) {
        super(falsingDataProvider);
        this.mVelocityToDistanceMultiplier = deviceConfigProxy.getFloat("systemui", "brightline_falsing_distance_velcoity_to_distance", 30.0f);
        float f = deviceConfigProxy.getFloat("systemui", "brightline_falsing_distance_horizontal_fling_threshold_in", 1.0f);
        float f2 = deviceConfigProxy.getFloat("systemui", "brightline_falsing_distance_vertical_fling_threshold_in", 1.5f);
        float f3 = deviceConfigProxy.getFloat("systemui", "brightline_falsing_distance_horizontal_swipe_threshold_in", 3.0f);
        float f4 = deviceConfigProxy.getFloat("systemui", "brightline_falsing_distance_horizontal_swipe_threshold_in", 3.0f);
        float f5 = deviceConfigProxy.getFloat("systemui", "brightline_falsing_distance_screen_fraction_max_distance", 0.8f);
        this.mHorizontalFlingThresholdPx = Math.min(((float) getWidthPixels()) * f5, f * getXdpi());
        this.mVerticalFlingThresholdPx = Math.min(((float) getHeightPixels()) * f5, f2 * getYdpi());
        this.mHorizontalSwipeThresholdPx = Math.min(((float) getWidthPixels()) * f5, f3 * getXdpi());
        this.mVerticalSwipeThresholdPx = Math.min(((float) getHeightPixels()) * f5, f4 * getYdpi());
    }

    private DistanceVectors getDistances() {
        if (this.mDistanceDirty) {
            this.mCachedDistance = calculateDistances();
            this.mDistanceDirty = false;
        }
        return this.mCachedDistance;
    }

    private DistanceVectors calculateDistances() {
        VelocityTracker obtain = VelocityTracker.obtain();
        List<MotionEvent> recentMotionEvents = getRecentMotionEvents();
        if (recentMotionEvents.size() < 3) {
            FalsingClassifier.logDebug("Only " + recentMotionEvents.size() + " motion events recorded.");
            return new DistanceVectors(this, 0.0f, 0.0f, 0.0f, 0.0f);
        }
        for (MotionEvent motionEvent : recentMotionEvents) {
            obtain.addMovement(motionEvent);
        }
        obtain.computeCurrentVelocity(1);
        float xVelocity = obtain.getXVelocity();
        float yVelocity = obtain.getYVelocity();
        obtain.recycle();
        float x = getLastMotionEvent().getX() - getFirstMotionEvent().getX();
        float y = getLastMotionEvent().getY() - getFirstMotionEvent().getY();
        FalsingClassifier.logInfo("dX: " + x + " dY: " + y + " xV: " + xVelocity + " yV: " + yVelocity);
        return new DistanceVectors(this, x, y, xVelocity, yVelocity);
    }

    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public void onTouchEvent(MotionEvent motionEvent) {
        this.mDistanceDirty = true;
    }

    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public boolean isFalseTouch() {
        return !getPassedFlingThreshold();
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public String getReason() {
        return String.format(null, "{distanceVectors=%s, isHorizontal=%s, velocityToDistanceMultiplier=%f, horizontalFlingThreshold=%f, verticalFlingThreshold=%f, horizontalSwipeThreshold=%f, verticalSwipeThreshold=%s}", getDistances(), Boolean.valueOf(isHorizontal()), Float.valueOf(this.mVelocityToDistanceMultiplier), Float.valueOf(this.mHorizontalFlingThresholdPx), Float.valueOf(this.mVerticalFlingThresholdPx), Float.valueOf(this.mHorizontalSwipeThresholdPx), Float.valueOf(this.mVerticalSwipeThresholdPx));
    }

    /* access modifiers changed from: package-private */
    public boolean isLongSwipe() {
        boolean passedDistanceThreshold = getPassedDistanceThreshold();
        FalsingClassifier.logDebug("Is longSwipe? " + passedDistanceThreshold);
        return passedDistanceThreshold;
    }

    private boolean getPassedDistanceThreshold() {
        DistanceVectors distances = getDistances();
        if (isHorizontal()) {
            FalsingClassifier.logDebug("Horizontal swipe distance: " + Math.abs(distances.mDx));
            FalsingClassifier.logDebug("Threshold: " + this.mHorizontalSwipeThresholdPx);
            return Math.abs(distances.mDx) >= this.mHorizontalSwipeThresholdPx;
        }
        FalsingClassifier.logDebug("Vertical swipe distance: " + Math.abs(distances.mDy));
        FalsingClassifier.logDebug("Threshold: " + this.mVerticalSwipeThresholdPx);
        return Math.abs(distances.mDy) >= this.mVerticalSwipeThresholdPx;
    }

    private boolean getPassedFlingThreshold() {
        DistanceVectors distances = getDistances();
        float f = distances.mDx + (distances.mVx * this.mVelocityToDistanceMultiplier);
        float f2 = distances.mDy + (distances.mVy * this.mVelocityToDistanceMultiplier);
        if (isHorizontal()) {
            FalsingClassifier.logDebug("Horizontal swipe and fling distance: " + distances.mDx + ", " + (distances.mVx * this.mVelocityToDistanceMultiplier));
            StringBuilder sb = new StringBuilder();
            sb.append("Threshold: ");
            sb.append(this.mHorizontalFlingThresholdPx);
            FalsingClassifier.logDebug(sb.toString());
            return Math.abs(f) >= this.mHorizontalFlingThresholdPx;
        }
        FalsingClassifier.logDebug("Vertical swipe and fling distance: " + distances.mDy + ", " + (distances.mVy * this.mVelocityToDistanceMultiplier));
        StringBuilder sb2 = new StringBuilder();
        sb2.append("Threshold: ");
        sb2.append(this.mVerticalFlingThresholdPx);
        FalsingClassifier.logDebug(sb2.toString());
        return Math.abs(f2) >= this.mVerticalFlingThresholdPx;
    }

    /* access modifiers changed from: private */
    public class DistanceVectors {
        final float mDx;
        final float mDy;
        private final float mVx;
        private final float mVy;

        DistanceVectors(DistanceClassifier distanceClassifier, float f, float f2, float f3, float f4) {
            this.mDx = f;
            this.mDy = f2;
            this.mVx = f3;
            this.mVy = f4;
        }

        public String toString() {
            return String.format(null, "{dx=%f, vx=%f, dy=%f, vy=%f}", Float.valueOf(this.mDx), Float.valueOf(this.mVx), Float.valueOf(this.mDy), Float.valueOf(this.mVy));
        }
    }
}

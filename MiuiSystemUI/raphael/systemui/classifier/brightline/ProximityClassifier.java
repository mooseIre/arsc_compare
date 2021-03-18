package com.android.systemui.classifier.brightline;

import android.view.MotionEvent;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.sensors.ProximitySensor;

/* access modifiers changed from: package-private */
public class ProximityClassifier extends FalsingClassifier {
    private final DistanceClassifier mDistanceClassifier;
    private long mGestureStartTimeNs;
    private boolean mNear;
    private long mNearDurationNs;
    private final float mPercentCoveredThreshold;
    private float mPercentNear;
    private long mPrevNearTimeNs;

    ProximityClassifier(DistanceClassifier distanceClassifier, FalsingDataProvider falsingDataProvider, DeviceConfigProxy deviceConfigProxy) {
        super(falsingDataProvider);
        this.mDistanceClassifier = distanceClassifier;
        this.mPercentCoveredThreshold = deviceConfigProxy.getFloat("systemui", "brightline_falsing_proximity_percent_covered_threshold", 0.1f);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public void onSessionStarted() {
        this.mPrevNearTimeNs = 0;
        this.mPercentNear = 0.0f;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public void onSessionEnded() {
        this.mPrevNearTimeNs = 0;
        this.mPercentNear = 0.0f;
    }

    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public void onTouchEvent(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mGestureStartTimeNs = motionEvent.getEventTimeNano();
            if (this.mPrevNearTimeNs > 0) {
                this.mPrevNearTimeNs = motionEvent.getEventTimeNano();
            }
            FalsingClassifier.logDebug("Gesture start time: " + this.mGestureStartTimeNs);
            this.mNearDurationNs = 0;
        }
        if (actionMasked == 1 || actionMasked == 3) {
            update(this.mNear, motionEvent.getEventTimeNano());
            long eventTimeNano = motionEvent.getEventTimeNano() - this.mGestureStartTimeNs;
            FalsingClassifier.logDebug("Gesture duration, Proximity duration: " + eventTimeNano + ", " + this.mNearDurationNs);
            if (eventTimeNano == 0) {
                this.mPercentNear = this.mNear ? 1.0f : 0.0f;
            } else {
                this.mPercentNear = ((float) this.mNearDurationNs) / ((float) eventTimeNano);
            }
        }
    }

    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public void onProximityEvent(ProximitySensor.ProximityEvent proximityEvent) {
        boolean near = proximityEvent.getNear();
        long timestampNs = proximityEvent.getTimestampNs();
        FalsingClassifier.logDebug("Sensor is: " + near + " at time " + timestampNs);
        update(near, timestampNs);
    }

    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public boolean isFalseTouch() {
        if (getInteractionType() == 0) {
            return false;
        }
        FalsingClassifier.logInfo("Percent of gesture in proximity: " + this.mPercentNear);
        if (this.mPercentNear > this.mPercentCoveredThreshold) {
            return !this.mDistanceClassifier.isLongSwipe();
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.systemui.classifier.brightline.FalsingClassifier
    public String getReason() {
        return String.format(null, "{percentInProximity=%f, threshold=%f, distanceClassifier=%s}", Float.valueOf(this.mPercentNear), Float.valueOf(this.mPercentCoveredThreshold), this.mDistanceClassifier.getReason());
    }

    private void update(boolean z, long j) {
        long j2 = this.mPrevNearTimeNs;
        if (j2 != 0 && j > j2 && this.mNear) {
            this.mNearDurationNs += j - j2;
            FalsingClassifier.logDebug("Updating duration: " + this.mNearDurationNs);
        }
        if (z) {
            FalsingClassifier.logDebug("Set prevNearTimeNs: " + j);
            this.mPrevNearTimeNs = j;
        }
        this.mNear = z;
    }
}

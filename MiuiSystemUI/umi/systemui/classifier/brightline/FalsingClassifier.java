package com.android.systemui.classifier.brightline;

import android.view.MotionEvent;
import com.android.systemui.util.sensors.ProximitySensor;
import java.util.List;

/* access modifiers changed from: package-private */
public abstract class FalsingClassifier {
    private final FalsingDataProvider mDataProvider;

    /* access modifiers changed from: package-private */
    public abstract String getReason();

    /* access modifiers changed from: package-private */
    public abstract boolean isFalseTouch();

    /* access modifiers changed from: package-private */
    public void onProximityEvent(ProximitySensor.ProximityEvent proximityEvent) {
    }

    /* access modifiers changed from: package-private */
    public void onSessionEnded() {
    }

    /* access modifiers changed from: package-private */
    public void onSessionStarted() {
    }

    /* access modifiers changed from: package-private */
    public void onTouchEvent(MotionEvent motionEvent) {
    }

    FalsingClassifier(FalsingDataProvider falsingDataProvider) {
        this.mDataProvider = falsingDataProvider;
    }

    /* access modifiers changed from: package-private */
    public List<MotionEvent> getRecentMotionEvents() {
        return this.mDataProvider.getRecentMotionEvents();
    }

    /* access modifiers changed from: package-private */
    public MotionEvent getFirstMotionEvent() {
        return this.mDataProvider.getFirstRecentMotionEvent();
    }

    /* access modifiers changed from: package-private */
    public MotionEvent getLastMotionEvent() {
        return this.mDataProvider.getLastMotionEvent();
    }

    /* access modifiers changed from: package-private */
    public boolean isHorizontal() {
        return this.mDataProvider.isHorizontal();
    }

    /* access modifiers changed from: package-private */
    public boolean isRight() {
        return this.mDataProvider.isRight();
    }

    /* access modifiers changed from: package-private */
    public boolean isVertical() {
        return this.mDataProvider.isVertical();
    }

    /* access modifiers changed from: package-private */
    public boolean isUp() {
        return this.mDataProvider.isUp();
    }

    /* access modifiers changed from: package-private */
    public float getAngle() {
        return this.mDataProvider.getAngle();
    }

    /* access modifiers changed from: package-private */
    public int getWidthPixels() {
        return this.mDataProvider.getWidthPixels();
    }

    /* access modifiers changed from: package-private */
    public int getHeightPixels() {
        return this.mDataProvider.getHeightPixels();
    }

    /* access modifiers changed from: package-private */
    public float getXdpi() {
        return this.mDataProvider.getXdpi();
    }

    /* access modifiers changed from: package-private */
    public float getYdpi() {
        return this.mDataProvider.getYdpi();
    }

    /* access modifiers changed from: package-private */
    public final int getInteractionType() {
        return this.mDataProvider.getInteractionType();
    }

    static void logDebug(String str) {
        BrightLineFalsingManager.logDebug(str);
    }

    static void logInfo(String str) {
        BrightLineFalsingManager.logInfo(str);
    }
}

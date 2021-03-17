package com.android.systemui.classifier.brightline;

import android.util.DisplayMetrics;
import android.view.MotionEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FalsingDataProvider {
    private float mAngle = 0.0f;
    private boolean mDirty = true;
    private MotionEvent mFirstRecentMotionEvent;
    private final int mHeightPixels;
    private int mInteractionType;
    private MotionEvent mLastMotionEvent;
    private final TimeLimitedMotionEventBuffer mRecentMotionEvents = new TimeLimitedMotionEventBuffer(1000);
    private final int mWidthPixels;
    private final float mXdpi;
    private final float mYdpi;

    public FalsingDataProvider(DisplayMetrics displayMetrics) {
        this.mXdpi = displayMetrics.xdpi;
        this.mYdpi = displayMetrics.ydpi;
        this.mWidthPixels = displayMetrics.widthPixels;
        this.mHeightPixels = displayMetrics.heightPixels;
        FalsingClassifier.logInfo("xdpi, ydpi: " + getXdpi() + ", " + getYdpi());
        FalsingClassifier.logInfo("width, height: " + getWidthPixels() + ", " + getHeightPixels());
    }

    /* access modifiers changed from: package-private */
    public void onMotionEvent(MotionEvent motionEvent) {
        motionEvent.getActionMasked();
        List<MotionEvent> unpackMotionEvent = unpackMotionEvent(motionEvent);
        FalsingClassifier.logDebug("Unpacked into: " + unpackMotionEvent.size());
        if (BrightLineFalsingManager.DEBUG) {
            for (MotionEvent motionEvent2 : unpackMotionEvent) {
                FalsingClassifier.logDebug("x,y,t: " + motionEvent2.getX() + "," + motionEvent2.getY() + "," + motionEvent2.getEventTime());
            }
        }
        if (motionEvent.getActionMasked() == 0) {
            this.mRecentMotionEvents.clear();
        }
        this.mRecentMotionEvents.addAll(unpackMotionEvent);
        FalsingClassifier.logDebug("Size: " + this.mRecentMotionEvents.size());
        this.mDirty = true;
    }

    /* access modifiers changed from: package-private */
    public int getWidthPixels() {
        return this.mWidthPixels;
    }

    /* access modifiers changed from: package-private */
    public int getHeightPixels() {
        return this.mHeightPixels;
    }

    /* access modifiers changed from: package-private */
    public float getXdpi() {
        return this.mXdpi;
    }

    /* access modifiers changed from: package-private */
    public float getYdpi() {
        return this.mYdpi;
    }

    /* access modifiers changed from: package-private */
    public List<MotionEvent> getRecentMotionEvents() {
        return this.mRecentMotionEvents;
    }

    /* access modifiers changed from: package-private */
    public final void setInteractionType(int i) {
        this.mInteractionType = i;
    }

    public boolean isDirty() {
        return this.mDirty;
    }

    /* access modifiers changed from: package-private */
    public final int getInteractionType() {
        return this.mInteractionType;
    }

    /* access modifiers changed from: package-private */
    public MotionEvent getFirstRecentMotionEvent() {
        recalculateData();
        return this.mFirstRecentMotionEvent;
    }

    /* access modifiers changed from: package-private */
    public MotionEvent getLastMotionEvent() {
        recalculateData();
        return this.mLastMotionEvent;
    }

    /* access modifiers changed from: package-private */
    public float getAngle() {
        recalculateData();
        return this.mAngle;
    }

    /* access modifiers changed from: package-private */
    public boolean isHorizontal() {
        recalculateData();
        if (!this.mRecentMotionEvents.isEmpty() && Math.abs(this.mFirstRecentMotionEvent.getX() - this.mLastMotionEvent.getX()) > Math.abs(this.mFirstRecentMotionEvent.getY() - this.mLastMotionEvent.getY())) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isRight() {
        recalculateData();
        if (!this.mRecentMotionEvents.isEmpty() && this.mLastMotionEvent.getX() > this.mFirstRecentMotionEvent.getX()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean isVertical() {
        return !isHorizontal();
    }

    /* access modifiers changed from: package-private */
    public boolean isUp() {
        recalculateData();
        if (!this.mRecentMotionEvents.isEmpty() && this.mLastMotionEvent.getY() < this.mFirstRecentMotionEvent.getY()) {
            return true;
        }
        return false;
    }

    private void recalculateData() {
        if (this.mDirty) {
            if (this.mRecentMotionEvents.isEmpty()) {
                this.mFirstRecentMotionEvent = null;
                this.mLastMotionEvent = null;
            } else {
                this.mFirstRecentMotionEvent = this.mRecentMotionEvents.get(0);
                TimeLimitedMotionEventBuffer timeLimitedMotionEventBuffer = this.mRecentMotionEvents;
                this.mLastMotionEvent = timeLimitedMotionEventBuffer.get(timeLimitedMotionEventBuffer.size() - 1);
            }
            calculateAngleInternal();
            this.mDirty = false;
        }
    }

    private void calculateAngleInternal() {
        if (this.mRecentMotionEvents.size() < 2) {
            this.mAngle = Float.MAX_VALUE;
            return;
        }
        this.mAngle = (float) Math.atan2((double) (this.mLastMotionEvent.getY() - this.mFirstRecentMotionEvent.getY()), (double) (this.mLastMotionEvent.getX() - this.mFirstRecentMotionEvent.getX()));
        while (true) {
            float f = this.mAngle;
            if (f >= 0.0f) {
                break;
            }
            this.mAngle = f + 6.2831855f;
        }
        while (true) {
            float f2 = this.mAngle;
            if (f2 > 6.2831855f) {
                this.mAngle = f2 - 6.2831855f;
            } else {
                return;
            }
        }
    }

    private List<MotionEvent> unpackMotionEvent(MotionEvent motionEvent) {
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        int pointerCount = motionEvent.getPointerCount();
        int i = 0;
        for (int i2 = 0; i2 < pointerCount; i2++) {
            MotionEvent.PointerProperties pointerProperties = new MotionEvent.PointerProperties();
            motionEvent.getPointerProperties(i2, pointerProperties);
            arrayList2.add(pointerProperties);
        }
        MotionEvent.PointerProperties[] pointerPropertiesArr = new MotionEvent.PointerProperties[arrayList2.size()];
        arrayList2.toArray(pointerPropertiesArr);
        int historySize = motionEvent.getHistorySize();
        int i3 = 0;
        while (i3 < historySize) {
            ArrayList arrayList3 = new ArrayList();
            for (int i4 = i; i4 < pointerCount; i4++) {
                MotionEvent.PointerCoords pointerCoords = new MotionEvent.PointerCoords();
                motionEvent.getHistoricalPointerCoords(i4, i3, pointerCoords);
                arrayList3.add(pointerCoords);
            }
            arrayList.add(MotionEvent.obtain(motionEvent.getDownTime(), motionEvent.getHistoricalEventTime(i3), motionEvent.getAction(), pointerCount, pointerPropertiesArr, (MotionEvent.PointerCoords[]) arrayList3.toArray(new MotionEvent.PointerCoords[i]), motionEvent.getMetaState(), motionEvent.getButtonState(), motionEvent.getXPrecision(), motionEvent.getYPrecision(), motionEvent.getDeviceId(), motionEvent.getEdgeFlags(), motionEvent.getSource(), motionEvent.getFlags()));
            i3++;
            pointerPropertiesArr = pointerPropertiesArr;
            i = i;
            pointerCount = pointerCount;
        }
        arrayList.add(MotionEvent.obtainNoHistory(motionEvent));
        return arrayList;
    }

    /* access modifiers changed from: package-private */
    public void onSessionEnd() {
        Iterator<MotionEvent> it = this.mRecentMotionEvents.iterator();
        while (it.hasNext()) {
            it.next().recycle();
        }
        this.mRecentMotionEvents.clear();
        this.mDirty = true;
    }
}

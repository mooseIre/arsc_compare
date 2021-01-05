package com.android.systemui.analytics;

import android.hardware.SensorEvent;
import android.os.Build;
import android.view.MotionEvent;
import com.android.systemui.statusbar.phone.nano.TouchAnalyticsProto$Session;
import java.util.ArrayList;

public class SensorLoggerSession {
    private long mEndTimestampMillis;
    private ArrayList<TouchAnalyticsProto$Session.TouchEvent> mMotionEvents = new ArrayList<>();
    private ArrayList<TouchAnalyticsProto$Session.PhoneEvent> mPhoneEvents = new ArrayList<>();
    private int mResult = 2;
    private ArrayList<TouchAnalyticsProto$Session.SensorEvent> mSensorEvents = new ArrayList<>();
    private final long mStartSystemTimeNanos;
    private final long mStartTimestampMillis;
    private int mTouchAreaHeight;
    private int mTouchAreaWidth;
    private int mType;

    public SensorLoggerSession(long j, long j2) {
        this.mStartTimestampMillis = j;
        this.mStartSystemTimeNanos = j2;
        this.mType = 3;
    }

    public void setType(int i) {
        this.mType = i;
    }

    public void end(long j, int i) {
        this.mResult = i;
        this.mEndTimestampMillis = j;
    }

    public void addMotionEvent(MotionEvent motionEvent) {
        this.mMotionEvents.add(motionEventToProto(motionEvent));
    }

    public void addSensorEvent(SensorEvent sensorEvent, long j) {
        this.mSensorEvents.add(sensorEventToProto(sensorEvent, j));
    }

    public void addPhoneEvent(int i, long j) {
        this.mPhoneEvents.add(phoneEventToProto(i, j));
    }

    public String toString() {
        return "Session{" + "mStartTimestampMillis=" + this.mStartTimestampMillis + ", mStartSystemTimeNanos=" + this.mStartSystemTimeNanos + ", mEndTimestampMillis=" + this.mEndTimestampMillis + ", mResult=" + this.mResult + ", mTouchAreaHeight=" + this.mTouchAreaHeight + ", mTouchAreaWidth=" + this.mTouchAreaWidth + ", mMotionEvents=[size=" + this.mMotionEvents.size() + "]" + ", mSensorEvents=[size=" + this.mSensorEvents.size() + "]" + ", mPhoneEvents=[size=" + this.mPhoneEvents.size() + "]" + '}';
    }

    public TouchAnalyticsProto$Session toProto() {
        TouchAnalyticsProto$Session touchAnalyticsProto$Session = new TouchAnalyticsProto$Session();
        long j = this.mStartTimestampMillis;
        touchAnalyticsProto$Session.startTimestampMillis = j;
        touchAnalyticsProto$Session.durationMillis = this.mEndTimestampMillis - j;
        touchAnalyticsProto$Session.build = Build.FINGERPRINT;
        touchAnalyticsProto$Session.deviceId = Build.DEVICE;
        touchAnalyticsProto$Session.result = this.mResult;
        touchAnalyticsProto$Session.type = this.mType;
        touchAnalyticsProto$Session.sensorEvents = (TouchAnalyticsProto$Session.SensorEvent[]) this.mSensorEvents.toArray(touchAnalyticsProto$Session.sensorEvents);
        touchAnalyticsProto$Session.touchEvents = (TouchAnalyticsProto$Session.TouchEvent[]) this.mMotionEvents.toArray(touchAnalyticsProto$Session.touchEvents);
        touchAnalyticsProto$Session.phoneEvents = (TouchAnalyticsProto$Session.PhoneEvent[]) this.mPhoneEvents.toArray(touchAnalyticsProto$Session.phoneEvents);
        touchAnalyticsProto$Session.touchAreaWidth = this.mTouchAreaWidth;
        touchAnalyticsProto$Session.touchAreaHeight = this.mTouchAreaHeight;
        return touchAnalyticsProto$Session;
    }

    private TouchAnalyticsProto$Session.PhoneEvent phoneEventToProto(int i, long j) {
        TouchAnalyticsProto$Session.PhoneEvent phoneEvent = new TouchAnalyticsProto$Session.PhoneEvent();
        phoneEvent.type = i;
        phoneEvent.timeOffsetNanos = j - this.mStartSystemTimeNanos;
        return phoneEvent;
    }

    private TouchAnalyticsProto$Session.SensorEvent sensorEventToProto(SensorEvent sensorEvent, long j) {
        TouchAnalyticsProto$Session.SensorEvent sensorEvent2 = new TouchAnalyticsProto$Session.SensorEvent();
        sensorEvent2.type = sensorEvent.sensor.getType();
        sensorEvent2.timeOffsetNanos = j - this.mStartSystemTimeNanos;
        sensorEvent2.timestamp = sensorEvent.timestamp;
        sensorEvent2.values = (float[]) sensorEvent.values.clone();
        return sensorEvent2;
    }

    private TouchAnalyticsProto$Session.TouchEvent motionEventToProto(MotionEvent motionEvent) {
        int pointerCount = motionEvent.getPointerCount();
        TouchAnalyticsProto$Session.TouchEvent touchEvent = new TouchAnalyticsProto$Session.TouchEvent();
        touchEvent.timeOffsetNanos = motionEvent.getEventTimeNano() - this.mStartSystemTimeNanos;
        touchEvent.action = motionEvent.getActionMasked();
        touchEvent.actionIndex = motionEvent.getActionIndex();
        touchEvent.pointers = new TouchAnalyticsProto$Session.TouchEvent.Pointer[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            TouchAnalyticsProto$Session.TouchEvent.Pointer pointer = new TouchAnalyticsProto$Session.TouchEvent.Pointer();
            pointer.x = motionEvent.getX(i);
            pointer.y = motionEvent.getY(i);
            pointer.size = motionEvent.getSize(i);
            pointer.pressure = motionEvent.getPressure(i);
            pointer.id = motionEvent.getPointerId(i);
            touchEvent.pointers[i] = pointer;
        }
        return touchEvent;
    }

    public void setTouchArea(int i, int i2) {
        this.mTouchAreaWidth = i;
        this.mTouchAreaHeight = i2;
    }

    public int getResult() {
        return this.mResult;
    }
}

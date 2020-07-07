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
        touchAnalyticsProto$Session.setStartTimestampMillis(this.mStartTimestampMillis);
        touchAnalyticsProto$Session.setDurationMillis(this.mEndTimestampMillis - this.mStartTimestampMillis);
        touchAnalyticsProto$Session.setBuild(Build.FINGERPRINT);
        touchAnalyticsProto$Session.setResult(this.mResult);
        touchAnalyticsProto$Session.setType(this.mType);
        touchAnalyticsProto$Session.sensorEvents = (TouchAnalyticsProto$Session.SensorEvent[]) this.mSensorEvents.toArray(touchAnalyticsProto$Session.sensorEvents);
        touchAnalyticsProto$Session.touchEvents = (TouchAnalyticsProto$Session.TouchEvent[]) this.mMotionEvents.toArray(touchAnalyticsProto$Session.touchEvents);
        touchAnalyticsProto$Session.phoneEvents = (TouchAnalyticsProto$Session.PhoneEvent[]) this.mPhoneEvents.toArray(touchAnalyticsProto$Session.phoneEvents);
        touchAnalyticsProto$Session.setTouchAreaWidth(this.mTouchAreaWidth);
        touchAnalyticsProto$Session.setTouchAreaHeight(this.mTouchAreaHeight);
        return touchAnalyticsProto$Session;
    }

    private TouchAnalyticsProto$Session.PhoneEvent phoneEventToProto(int i, long j) {
        TouchAnalyticsProto$Session.PhoneEvent phoneEvent = new TouchAnalyticsProto$Session.PhoneEvent();
        phoneEvent.setType(i);
        phoneEvent.setTimeOffsetNanos(j - this.mStartSystemTimeNanos);
        return phoneEvent;
    }

    private TouchAnalyticsProto$Session.SensorEvent sensorEventToProto(SensorEvent sensorEvent, long j) {
        TouchAnalyticsProto$Session.SensorEvent sensorEvent2 = new TouchAnalyticsProto$Session.SensorEvent();
        sensorEvent2.setType(sensorEvent.sensor.getType());
        sensorEvent2.setTimeOffsetNanos(j - this.mStartSystemTimeNanos);
        sensorEvent2.setTimestamp(sensorEvent.timestamp);
        sensorEvent2.values = (float[]) sensorEvent.values.clone();
        return sensorEvent2;
    }

    private TouchAnalyticsProto$Session.TouchEvent motionEventToProto(MotionEvent motionEvent) {
        int pointerCount = motionEvent.getPointerCount();
        TouchAnalyticsProto$Session.TouchEvent touchEvent = new TouchAnalyticsProto$Session.TouchEvent();
        touchEvent.setTimeOffsetNanos(motionEvent.getEventTimeNano() - this.mStartSystemTimeNanos);
        touchEvent.setAction(motionEvent.getActionMasked());
        touchEvent.setActionIndex(motionEvent.getActionIndex());
        touchEvent.pointers = new TouchAnalyticsProto$Session.TouchEvent.Pointer[pointerCount];
        for (int i = 0; i < pointerCount; i++) {
            TouchAnalyticsProto$Session.TouchEvent.Pointer pointer = new TouchAnalyticsProto$Session.TouchEvent.Pointer();
            pointer.setX(motionEvent.getX(i));
            pointer.setY(motionEvent.getY(i));
            pointer.setSize(motionEvent.getSize(i));
            pointer.setPressure(motionEvent.getPressure(i));
            pointer.setId(motionEvent.getPointerId(i));
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

    public long getStartTimestampMillis() {
        return this.mStartTimestampMillis;
    }
}

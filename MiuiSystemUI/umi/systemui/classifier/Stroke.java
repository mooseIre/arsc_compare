package com.android.systemui.classifier;

import java.util.ArrayList;

public class Stroke {
    private final float mDpi;
    private long mEndTimeNano;
    private float mLength;
    private ArrayList<Point> mPoints = new ArrayList<>();
    private long mStartTimeNano;

    public Stroke(long j, float f) {
        this.mDpi = f;
        this.mEndTimeNano = j;
        this.mStartTimeNano = j;
    }

    public void addPoint(float f, float f2, long j) {
        this.mEndTimeNano = j;
        float f3 = this.mDpi;
        Point point = new Point(f / f3, f2 / f3, j - this.mStartTimeNano);
        if (!this.mPoints.isEmpty()) {
            float f4 = this.mLength;
            ArrayList<Point> arrayList = this.mPoints;
            this.mLength = f4 + arrayList.get(arrayList.size() - 1).dist(point);
        }
        this.mPoints.add(point);
    }

    public int getCount() {
        return this.mPoints.size();
    }

    public float getTotalLength() {
        return this.mLength;
    }

    public float getEndPointLength() {
        ArrayList<Point> arrayList = this.mPoints;
        return this.mPoints.get(0).dist(arrayList.get(arrayList.size() - 1));
    }

    public long getDurationNanos() {
        return this.mEndTimeNano - this.mStartTimeNano;
    }

    public float getDurationSeconds() {
        return ((float) getDurationNanos()) / 1.0E9f;
    }

    public ArrayList<Point> getPoints() {
        return this.mPoints;
    }

    public long getLastEventTimeNano() {
        if (this.mPoints.isEmpty()) {
            return this.mStartTimeNano;
        }
        ArrayList<Point> arrayList = this.mPoints;
        return arrayList.get(arrayList.size() - 1).timeOffsetNano;
    }
}

package com.android.systemui.classifier;

public class Point {
    public long timeOffsetNano;
    public float x;
    public float y;

    public Point(float f, float f2) {
        this.x = f;
        this.y = f2;
        this.timeOffsetNano = 0;
    }

    public Point(float f, float f2, long j) {
        this.x = f;
        this.y = f2;
        this.timeOffsetNano = j;
    }

    public boolean equals(Point point) {
        return this.x == point.x && this.y == point.y;
    }

    public float dist(Point point) {
        return (float) Math.hypot((double) (point.x - this.x), (double) (point.y - this.y));
    }

    public float crossProduct(Point point, Point point2) {
        float f = point.x;
        float f2 = this.x;
        float f3 = point2.y;
        float f4 = this.y;
        return ((f - f2) * (f3 - f4)) - ((point.y - f4) * (point2.x - f2));
    }

    public float dotProduct(Point point, Point point2) {
        float f = point.x;
        float f2 = this.x;
        float f3 = point.y;
        float f4 = this.y;
        return ((f - f2) * (point2.x - f2)) + ((f3 - f4) * (point2.y - f4));
    }

    public float getAngle(Point point, Point point2) {
        float dist = dist(point);
        float dist2 = dist(point2);
        if (dist == 0.0f || dist2 == 0.0f) {
            return 0.0f;
        }
        float crossProduct = crossProduct(point, point2);
        float acos = (float) Math.acos((double) Math.min(1.0f, Math.max(-1.0f, (dotProduct(point, point2) / dist) / dist2)));
        return ((double) crossProduct) < 0.0d ? 6.2831855f - acos : acos;
    }
}

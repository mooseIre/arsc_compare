package com.android.systemui.assist.ui;

import android.graphics.Path;
import android.graphics.PointF;
import java.util.ArrayList;
import java.util.List;

public abstract class CornerPathRenderer {

    public enum Corner {
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        TOP_RIGHT,
        TOP_LEFT
    }

    public abstract Path getCornerPath(Corner corner);

    public Path getInsetPath(Corner corner, float f) {
        return approximateInnerPath(getCornerPath(corner), -f);
    }

    private Path approximateInnerPath(Path path, float f) {
        return toPath(shiftBy(getApproximatePoints(path), f));
    }

    private ArrayList<PointF> getApproximatePoints(Path path) {
        float[] approximate = path.approximate(0.1f);
        ArrayList<PointF> arrayList = new ArrayList<>();
        for (int i = 0; i < approximate.length; i += 3) {
            arrayList.add(new PointF(approximate[i + 1], approximate[i + 2]));
        }
        return arrayList;
    }

    private ArrayList<PointF> shiftBy(ArrayList<PointF> arrayList, float f) {
        ArrayList<PointF> arrayList2 = new ArrayList<>();
        for (int i = 0; i < arrayList.size(); i++) {
            PointF pointF = arrayList.get(i);
            PointF normalAt = normalAt(arrayList, i);
            arrayList2.add(new PointF(pointF.x + (normalAt.x * f), pointF.y + (normalAt.y * f)));
        }
        return arrayList2;
    }

    private Path toPath(List<PointF> list) {
        Path path = new Path();
        if (list.size() > 0) {
            path.moveTo(list.get(0).x, list.get(0).y);
            for (PointF pointF : list.subList(1, list.size())) {
                path.lineTo(pointF.x, pointF.y);
            }
        }
        return path;
    }

    private PointF normalAt(List<PointF> list, int i) {
        PointF pointF;
        PointF pointF2;
        if (i == 0) {
            pointF = new PointF(0.0f, 0.0f);
        } else {
            PointF pointF3 = list.get(i);
            PointF pointF4 = list.get(i - 1);
            pointF = new PointF(pointF3.x - pointF4.x, pointF3.y - pointF4.y);
        }
        if (i == list.size() - 1) {
            pointF2 = new PointF(0.0f, 0.0f);
        } else {
            PointF pointF5 = list.get(i);
            PointF pointF6 = list.get(i + 1);
            pointF2 = new PointF(pointF6.x - pointF5.x, pointF6.y - pointF5.y);
        }
        return rotate90Ccw(normalize(new PointF(pointF.x + pointF2.x, pointF.y + pointF2.y)));
    }

    private PointF rotate90Ccw(PointF pointF) {
        return new PointF(-pointF.y, pointF.x);
    }

    private float magnitude(PointF pointF) {
        float f = pointF.x;
        float f2 = pointF.y;
        return (float) Math.sqrt((double) ((f * f) + (f2 * f2)));
    }

    private PointF normalize(PointF pointF) {
        float magnitude = magnitude(pointF);
        if (magnitude == 0.0f) {
            return pointF;
        }
        float f = 1.0f / magnitude;
        return new PointF(pointF.x * f, pointF.y * f);
    }
}

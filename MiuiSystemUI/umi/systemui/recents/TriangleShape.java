package com.android.systemui.recents;

import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.drawable.shapes.PathShape;

public class TriangleShape extends PathShape {
    private Path mTriangularPath;

    public TriangleShape(Path path, float f, float f2) {
        super(path, f, f2);
        this.mTriangularPath = path;
    }

    public static TriangleShape create(float f, float f2, boolean z) {
        Path path = new Path();
        if (z) {
            path.moveTo(0.0f, f2);
            path.lineTo(f, f2);
            path.lineTo(f / 2.0f, 0.0f);
            path.close();
        } else {
            path.moveTo(0.0f, 0.0f);
            path.lineTo(f / 2.0f, f2);
            path.lineTo(f, 0.0f);
            path.close();
        }
        return new TriangleShape(path, f, f2);
    }

    public static TriangleShape createHorizontal(float f, float f2, boolean z) {
        Path path = new Path();
        if (z) {
            path.moveTo(0.0f, f2 / 2.0f);
            path.lineTo(f, f2);
            path.lineTo(f, 0.0f);
            path.close();
        } else {
            path.moveTo(0.0f, f2);
            path.lineTo(f, f2 / 2.0f);
            path.lineTo(0.0f, 0.0f);
            path.close();
        }
        return new TriangleShape(path, f, f2);
    }

    public void getOutline(Outline outline) {
        outline.setPath(this.mTriangularPath);
    }
}

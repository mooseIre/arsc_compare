package com.android.systemui.pip;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import java.io.PrintWriter;

public class PipSnapAlgorithm {
    private final Context mContext;
    private final float mMaxAspectRatioForMinSize;
    private int mOrientation = 0;

    public PipSnapAlgorithm(Context context) {
        Resources resources = context.getResources();
        this.mContext = context;
        resources.getFloat(17105073);
        this.mMaxAspectRatioForMinSize = resources.getFloat(17105071);
        onConfigurationChanged();
    }

    public void onConfigurationChanged() {
        this.mOrientation = this.mContext.getResources().getConfiguration().orientation;
    }

    public float getSnapFraction(Rect rect, Rect rect2) {
        Rect rect3 = new Rect();
        snapRectToClosestEdge(rect, rect2, rect3);
        float width = ((float) (rect3.left - rect2.left)) / ((float) rect2.width());
        float height = ((float) (rect3.top - rect2.top)) / ((float) rect2.height());
        int i = rect3.top;
        if (i == rect2.top) {
            return width;
        }
        if (rect3.left == rect2.right) {
            return height + 1.0f;
        }
        return i == rect2.bottom ? (1.0f - width) + 2.0f : (1.0f - height) + 3.0f;
    }

    public void applySnapFraction(Rect rect, Rect rect2, float f) {
        if (f < 1.0f) {
            rect.offsetTo(rect2.left + ((int) (f * ((float) rect2.width()))), rect2.top);
        } else if (f < 2.0f) {
            rect.offsetTo(rect2.right, rect2.top + ((int) ((f - 1.0f) * ((float) rect2.height()))));
        } else if (f < 3.0f) {
            rect.offsetTo(rect2.left + ((int) ((1.0f - (f - 2.0f)) * ((float) rect2.width()))), rect2.bottom);
        } else {
            rect.offsetTo(rect2.left, rect2.top + ((int) ((1.0f - (f - 3.0f)) * ((float) rect2.height()))));
        }
    }

    public void snapRectToClosestEdge(Rect rect, Rect rect2, Rect rect3) {
        int max = Math.max(rect2.left, Math.min(rect2.right, rect.left));
        int max2 = Math.max(rect2.top, Math.min(rect2.bottom, rect.top));
        rect3.set(rect);
        int abs = Math.abs(rect.left - rect2.left);
        int abs2 = Math.abs(rect.top - rect2.top);
        int abs3 = Math.abs(rect2.right - rect.left);
        int min = Math.min(Math.min(abs, abs3), Math.min(abs2, Math.abs(rect2.bottom - rect.top)));
        if (min == abs) {
            rect3.offsetTo(rect2.left, max2);
        } else if (min == abs2) {
            rect3.offsetTo(max, rect2.top);
        } else if (min == abs3) {
            rect3.offsetTo(rect2.right, max2);
        } else {
            rect3.offsetTo(max, rect2.bottom);
        }
    }

    public void dump(PrintWriter printWriter, String str) {
        printWriter.println(str + PipSnapAlgorithm.class.getSimpleName());
        printWriter.println((str + "  ") + "mOrientation=" + this.mOrientation);
    }
}

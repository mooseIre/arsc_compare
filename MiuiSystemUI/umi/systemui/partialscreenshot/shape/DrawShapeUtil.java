package com.android.systemui.partialscreenshot.shape;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;

public class DrawShapeUtil {
    private static Paint mPaintLine;
    private static Paint mPaintStroke;

    public static TouchAreaEnum handleArea(int i, int i2, Rect rect) {
        int i3;
        if (rect.right - rect.left > 600 && rect.bottom - rect.top > 600) {
            i3 = 150;
        } else if (rect.right - rect.left > 500 && rect.bottom - rect.top > 500) {
            i3 = 125;
        } else if (rect.right - rect.left > 400 && rect.bottom - rect.top > 400) {
            i3 = 100;
        } else if (rect.right - rect.left <= 300 || rect.bottom - rect.top <= 300) {
            i3 = (rect.right - rect.left <= 200 || rect.bottom - rect.top <= 200) ? 40 : 50;
        } else {
            i3 = 75;
        }
        if (isInLeftTopCorner(i, i2, rect, i3)) {
            return TouchAreaEnum.LEFT_TOP;
        }
        if (isInRightTopCorner(i, i2, rect, i3)) {
            return TouchAreaEnum.RIGHT_TOP;
        }
        if (isInLeftBottomCorner(i, i2, rect, i3)) {
            return TouchAreaEnum.LEFT_BOTTOM;
        }
        if (isInRightBottomCorner(i, i2, rect, i3)) {
            return TouchAreaEnum.RIGHT_BOTTOM;
        }
        if (isInCenterLeftCorner(i, i2, rect, i3)) {
            return TouchAreaEnum.CENTER_LEFT;
        }
        if (isInCenterTopCorner(i, i2, rect, i3)) {
            return TouchAreaEnum.CENTER_TOP;
        }
        if (isInCenterRightCorner(i, i2, rect, i3)) {
            return TouchAreaEnum.CENTER_RIGHT;
        }
        if (isInCenterBottomCorner(i, i2, rect, i3)) {
            return TouchAreaEnum.CENTER_BOTTOM;
        }
        if (isInFrameCenter(i, i2, rect)) {
            return TouchAreaEnum.CENTER;
        }
        return TouchAreaEnum.OUT_OF_BOUNDS;
    }

    private static boolean isInsideBound(int i, int i2, int i3) {
        return Math.pow((double) i3, 2.0d) >= ((double) ((int) (Math.pow((double) i, 2.0d) + Math.pow((double) i2, 2.0d))));
    }

    private static boolean isInFrameCenter(int i, int i2, Rect rect) {
        return i >= rect.left && i <= rect.right && i2 >= rect.top && i2 <= rect.bottom;
    }

    private static boolean isInLeftTopCorner(int i, int i2, Rect rect, int i3) {
        return isInsideBound(i - rect.left, i2 - rect.top, i3);
    }

    private static boolean isInLeftBottomCorner(int i, int i2, Rect rect, int i3) {
        return isInsideBound(i - rect.left, i2 - rect.bottom, i3);
    }

    private static boolean isInRightTopCorner(int i, int i2, Rect rect, int i3) {
        return isInsideBound(i - rect.right, i2 - rect.top, i3);
    }

    private static boolean isInRightBottomCorner(int i, int i2, Rect rect, int i3) {
        return isInsideBound(i - rect.right, i2 - rect.bottom, i3);
    }

    private static boolean isInCenterLeftCorner(int i, int i2, Rect rect, int i3) {
        return Math.pow((double) i3, 2.0d) >= ((double) ((int) (Math.pow((double) (i - rect.left), 2.0d) + Math.pow((double) (i2 - ((rect.bottom + rect.top) / 2)), 2.0d))));
    }

    private static boolean isInCenterRightCorner(int i, int i2, Rect rect, int i3) {
        return Math.pow((double) i3, 2.0d) >= ((double) ((int) (Math.pow((double) (i - rect.right), 2.0d) + Math.pow((double) (i2 - ((rect.bottom + rect.top) / 2)), 2.0d))));
    }

    private static boolean isInCenterTopCorner(int i, int i2, Rect rect, int i3) {
        return Math.pow((double) i3, 2.0d) >= ((double) ((int) (Math.pow((double) (i - ((rect.right + rect.left) / 2)), 2.0d) + Math.pow((double) (i2 - rect.top), 2.0d))));
    }

    private static boolean isInCenterBottomCorner(int i, int i2, Rect rect, int i3) {
        return Math.pow((double) i3, 2.0d) >= ((double) ((int) (Math.pow((double) (i - ((rect.right + rect.left) / 2)), 2.0d) + Math.pow((double) (i2 - rect.bottom), 2.0d))));
    }

    public static boolean isUseful(int i, int i2, int i3, int i4) {
        return Math.abs(i - i3) <= 350 && Math.abs(i2 - i4) <= 350;
    }

    private static void initPaint() {
        if (mPaintLine == null) {
            Paint paint = new Paint();
            mPaintLine = paint;
            paint.setColor(-1);
            mPaintLine.setStyle(Paint.Style.STROKE);
            mPaintLine.setAntiAlias(true);
            mPaintLine.setStrokeWidth(6.0f);
            mPaintLine.setStrokeCap(Paint.Cap.ROUND);
        }
        if (mPaintStroke == null) {
            Paint paint2 = new Paint();
            mPaintStroke = paint2;
            paint2.setColor(-1);
            mPaintStroke.setFlags(1);
            mPaintStroke.setAlpha(160);
            mPaintStroke.setAntiAlias(true);
            mPaintStroke.setStyle(Paint.Style.STROKE);
            mPaintStroke.setStrokeWidth(2.91f);
        }
    }

    public static void drawTrimmingFrame(Canvas canvas, Rect rect) {
        initPaint();
        int i = rect.right;
        int i2 = rect.left;
        int i3 = (int) (((double) (i - i2)) * 0.08d);
        int i4 = rect.bottom;
        int i5 = rect.top;
        int i6 = (int) (((double) (i4 - i5)) * 0.08d);
        canvas.drawRect((float) (i2 - 2), (float) (i5 - 2), (float) (i + 2), (float) (i4 + 2), mPaintStroke);
        int i7 = rect.left;
        int i8 = rect.top;
        Canvas canvas2 = canvas;
        canvas2.drawLine((float) (i7 - 2), (float) (i8 - 2), (float) (i7 + i3), (float) (i8 - 2), mPaintLine);
        int i9 = rect.left;
        int i10 = rect.top;
        canvas2.drawLine((float) (i9 - 2), (float) (i10 - 2), (float) (i9 - 2), (float) (i10 + i6), mPaintLine);
        int i11 = rect.left;
        int i12 = rect.bottom;
        canvas2.drawLine((float) (i11 - 2), (float) (i12 + 2), (float) (i11 + i3), (float) (i12 + 2), mPaintLine);
        int i13 = rect.left;
        int i14 = rect.bottom;
        canvas2.drawLine((float) (i13 - 2), (float) (i14 + 2), (float) (i13 - 2), (float) (i14 - i6), mPaintLine);
        int i15 = rect.right;
        int i16 = rect.top;
        canvas2.drawLine((float) (i15 + 2), (float) (i16 - 2), (float) (i15 - i3), (float) (i16 - 2), mPaintLine);
        int i17 = rect.right;
        int i18 = rect.top;
        canvas2.drawLine((float) (i17 + 2), (float) (i18 - 2), (float) (i17 + 2), (float) (i18 + i6), mPaintLine);
        int i19 = rect.right;
        int i20 = rect.bottom;
        canvas2.drawLine((float) (i19 + 2), (float) (i20 + 2), (float) (i19 - i3), (float) (i20 + 2), mPaintLine);
        int i21 = rect.right;
        int i22 = rect.bottom;
        canvas2.drawLine((float) (i21 + 2), (float) (i22 + 2), (float) (i21 + 2), (float) (i22 - i6), mPaintLine);
        int i23 = rect.left;
        int i24 = rect.right;
        int i25 = i3 / 2;
        int i26 = ((i23 + i24) / 2) - i25;
        int i27 = ((i23 + i24) / 2) + i25;
        int i28 = rect.top;
        int i29 = rect.bottom;
        int i30 = i6 / 2;
        int i31 = ((i28 + i29) / 2) - i30;
        int i32 = ((i29 + i28) / 2) + i30;
        Canvas canvas3 = canvas;
        float f = (float) i26;
        float f2 = (float) i27;
        canvas3.drawLine(f, (float) (i28 - 2), f2, (float) (i28 - 2), mPaintLine);
        int i33 = rect.bottom;
        canvas3.drawLine(f, (float) (i33 + 2), f2, (float) (i33 + 2), mPaintLine);
        int i34 = rect.left;
        float f3 = (float) i32;
        float f4 = (float) i31;
        canvas3.drawLine((float) (i34 - 2), f3, (float) (i34 - 2), f4, mPaintLine);
        int i35 = rect.right;
        canvas.drawLine((float) (i35 + 2), f3, (float) (i35 + 2), f4, mPaintLine);
    }

    public static Bitmap getResultBitmap(int i, int i2, Bitmap bitmap, Bitmap bitmap2, Rect rect) {
        Bitmap createBitmap = Bitmap.createBitmap(bitmap2, rect.left, rect.top, i, i2);
        Bitmap createBitmap2 = Bitmap.createBitmap(i, i2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap2);
        Paint paint = new Paint(7);
        PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
        canvas.drawBitmap(createBitmap, 0.0f, 0.0f, paint);
        paint.setXfermode(porterDuffXfermode);
        canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
        paint.setXfermode((Xfermode) null);
        return createBitmap2;
    }
}

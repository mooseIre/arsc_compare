package com.android.systemui.controlcenter.policy;

import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;

public class SmoothPathProvider {
    private SmoothData mAllData = null;

    private static boolean isHeightCollapsed(int i, float f, float f2, double d) {
        return ((double) i) <= ((double) (f + f2)) * (d + 1.0d);
    }

    private static boolean isWidthCollapsed(int i, float f, float f2, double d) {
        return ((double) i) <= ((double) (f + f2)) * (d + 1.0d);
    }

    /* access modifiers changed from: private */
    public static double radToAngle(double d) {
        return (d * 180.0d) / 3.141592653589793d;
    }

    /* access modifiers changed from: private */
    public static double thetaForHeight(double d) {
        return (d * 3.141592653589793d) / 4.0d;
    }

    /* access modifiers changed from: private */
    public static double thetaForWidth(double d) {
        return (d * 3.141592653589793d) / 4.0d;
    }

    /* access modifiers changed from: private */
    public static double yForHeight(double d, double d2) {
        return d * d2;
    }

    /* access modifiers changed from: private */
    public static double yForWidth(double d, double d2) {
        return d * d2;
    }

    public void buildSmoothData(int i, int i2, float f, double d) {
        buildSmoothData(i, i2, new float[]{f, f, f, f, f, f, f, f}, d);
    }

    public void buildSmoothData(int i, int i2, float[] fArr, double d) {
        this.mAllData = new SmoothData(i, i2, d);
        if (fArr != null) {
            float[] fArr2 = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
            for (int i3 = 0; i3 < Math.min(8, fArr.length); i3++) {
                fArr2[i3] = fArr[i3];
            }
            float f = fArr2[0];
            float f2 = fArr2[1];
            float f3 = fArr2[2];
            float f4 = fArr2[3];
            float f5 = fArr2[4];
            float f6 = fArr2[5];
            float f7 = fArr2[6];
            float f8 = fArr2[7];
            float f9 = (float) i;
            if (f + f3 > f9) {
                f = (fArr2[0] * f9) / (fArr2[0] + fArr2[2]);
                f3 = (fArr2[2] * f9) / (fArr2[0] + fArr2[2]);
            }
            float f10 = (float) i2;
            if (f4 + f6 > f10) {
                f4 = (fArr2[3] * f10) / (fArr2[3] + fArr2[5]);
                f6 = (fArr2[5] * f10) / (fArr2[3] + fArr2[5]);
            }
            if (f5 + f7 > f9) {
                f5 = (fArr2[4] * f9) / (fArr2[4] + fArr2[6]);
                f7 = (f9 * fArr2[6]) / (fArr2[4] + fArr2[6]);
            }
            if (f8 + f2 > f10) {
                f8 = (fArr2[7] * f10) / (fArr2[7] + fArr2[1]);
                f2 = (f10 * fArr2[1]) / (fArr2[7] + fArr2[1]);
            }
            ensureFourCornerData();
            this.mAllData.topLeft.build(Math.min(f, f2), i, i2, d, 0);
            this.mAllData.topRight.build(Math.min(f3, f4), i, i2, d, 1);
            this.mAllData.bottomRight.build(Math.min(f5, f6), i, i2, d, 2);
            this.mAllData.bottomLeft.build(Math.min(f7, f8), i, i2, d, 3);
        }
    }

    private void ensureFourCornerData() {
        SmoothData smoothData = this.mAllData;
        if (smoothData.topLeft == null) {
            smoothData.topLeft = new CornerData();
        }
        SmoothData smoothData2 = this.mAllData;
        if (smoothData2.topRight == null) {
            smoothData2.topRight = new CornerData();
        }
        SmoothData smoothData3 = this.mAllData;
        if (smoothData3.bottomRight == null) {
            smoothData3.bottomRight = new CornerData();
        }
        SmoothData smoothData4 = this.mAllData;
        if (smoothData4.bottomLeft == null) {
            smoothData4.bottomLeft = new CornerData();
        }
    }

    public Path getSmoothPath() {
        if (isFourCornerDataValid()) {
            Path path = new Path();
            SmoothData smoothData = this.mAllData;
            path.addRect(new RectF(0.0f, 0.0f, (float) smoothData.width, (float) smoothData.height), Path.Direction.CCW);
            return path;
        }
        Path path2 = new Path();
        path2.reset();
        CornerData cornerData = this.mAllData.topLeft;
        path2.arcTo(cornerData.rect, (float) radToAngle(cornerData.thetaForVertical + 3.141592653589793d), this.mAllData.topLeft.swapAngle);
        CornerData cornerData2 = this.mAllData.topLeft;
        if (cornerData2.smoothForHorizontal != 0.0d) {
            PointF[] pointFArr = cornerData2.bezierAnchorHorizontal;
            path2.cubicTo(pointFArr[1].x, pointFArr[1].y, pointFArr[2].x, pointFArr[2].y, pointFArr[3].x, pointFArr[3].y);
        }
        SmoothData smoothData2 = this.mAllData;
        if (!isWidthCollapsed(smoothData2.width, smoothData2.topLeft.radius, smoothData2.topRight.radius, smoothData2.smooth)) {
            PointF[] pointFArr2 = this.mAllData.topRight.bezierAnchorHorizontal;
            path2.lineTo(pointFArr2[0].x, pointFArr2[0].y);
        }
        CornerData cornerData3 = this.mAllData.topRight;
        if (cornerData3.smoothForHorizontal != 0.0d) {
            PointF[] pointFArr3 = cornerData3.bezierAnchorHorizontal;
            path2.cubicTo(pointFArr3[1].x, pointFArr3[1].y, pointFArr3[2].x, pointFArr3[2].y, pointFArr3[3].x, pointFArr3[3].y);
        }
        CornerData cornerData4 = this.mAllData.topRight;
        path2.arcTo(cornerData4.rect, (float) radToAngle(cornerData4.thetaForHorizontal + 4.71238898038469d), this.mAllData.topRight.swapAngle);
        CornerData cornerData5 = this.mAllData.topRight;
        if (cornerData5.smoothForVertical != 0.0d) {
            PointF[] pointFArr4 = cornerData5.bezierAnchorVertical;
            path2.cubicTo(pointFArr4[1].x, pointFArr4[1].y, pointFArr4[2].x, pointFArr4[2].y, pointFArr4[3].x, pointFArr4[3].y);
        }
        SmoothData smoothData3 = this.mAllData;
        if (!isHeightCollapsed(smoothData3.height, smoothData3.topRight.radius, smoothData3.bottomRight.radius, smoothData3.smooth)) {
            PointF[] pointFArr5 = this.mAllData.bottomRight.bezierAnchorVertical;
            path2.lineTo(pointFArr5[0].x, pointFArr5[0].y);
        }
        CornerData cornerData6 = this.mAllData.bottomRight;
        if (cornerData6.smoothForVertical != 0.0d) {
            PointF[] pointFArr6 = cornerData6.bezierAnchorVertical;
            path2.cubicTo(pointFArr6[1].x, pointFArr6[1].y, pointFArr6[2].x, pointFArr6[2].y, pointFArr6[3].x, pointFArr6[3].y);
        }
        CornerData cornerData7 = this.mAllData.bottomRight;
        path2.arcTo(cornerData7.rect, (float) radToAngle(cornerData7.thetaForVertical), this.mAllData.bottomRight.swapAngle);
        CornerData cornerData8 = this.mAllData.bottomRight;
        if (cornerData8.smoothForHorizontal != 0.0d) {
            PointF[] pointFArr7 = cornerData8.bezierAnchorHorizontal;
            path2.cubicTo(pointFArr7[1].x, pointFArr7[1].y, pointFArr7[2].x, pointFArr7[2].y, pointFArr7[3].x, pointFArr7[3].y);
        }
        SmoothData smoothData4 = this.mAllData;
        if (!isWidthCollapsed(smoothData4.width, smoothData4.bottomRight.radius, smoothData4.bottomLeft.radius, smoothData4.smooth)) {
            PointF[] pointFArr8 = this.mAllData.bottomLeft.bezierAnchorHorizontal;
            path2.lineTo(pointFArr8[0].x, pointFArr8[0].y);
        }
        CornerData cornerData9 = this.mAllData.bottomLeft;
        if (cornerData9.smoothForHorizontal != 0.0d) {
            PointF[] pointFArr9 = cornerData9.bezierAnchorHorizontal;
            path2.cubicTo(pointFArr9[1].x, pointFArr9[1].y, pointFArr9[2].x, pointFArr9[2].y, pointFArr9[3].x, pointFArr9[3].y);
        }
        CornerData cornerData10 = this.mAllData.bottomLeft;
        path2.arcTo(cornerData10.rect, (float) radToAngle(cornerData10.thetaForHorizontal + 1.5707963267948966d), this.mAllData.bottomLeft.swapAngle);
        CornerData cornerData11 = this.mAllData.bottomLeft;
        if (cornerData11.smoothForVertical != 0.0d) {
            PointF[] pointFArr10 = cornerData11.bezierAnchorVertical;
            path2.cubicTo(pointFArr10[1].x, pointFArr10[1].y, pointFArr10[2].x, pointFArr10[2].y, pointFArr10[3].x, pointFArr10[3].y);
        }
        SmoothData smoothData5 = this.mAllData;
        if (!isHeightCollapsed(smoothData5.height, smoothData5.bottomLeft.radius, smoothData5.topLeft.radius, smoothData5.smooth)) {
            PointF[] pointFArr11 = this.mAllData.topLeft.bezierAnchorVertical;
            path2.lineTo(pointFArr11[0].x, pointFArr11[0].y);
        }
        CornerData cornerData12 = this.mAllData.topLeft;
        if (cornerData12.smoothForVertical != 0.0d) {
            PointF[] pointFArr12 = cornerData12.bezierAnchorVertical;
            path2.cubicTo(pointFArr12[1].x, pointFArr12[1].y, pointFArr12[2].x, pointFArr12[2].y, pointFArr12[3].x, pointFArr12[3].y);
        }
        path2.close();
        return path2;
    }

    private boolean isFourCornerDataValid() {
        SmoothData smoothData = this.mAllData;
        return smoothData.topLeft == null || smoothData.topRight == null || smoothData.bottomRight == null || smoothData.bottomLeft == null;
    }

    /* access modifiers changed from: private */
    public static double smoothForWidth(int i, float f, double d) {
        return isWidthCollapsed(i, f, f, d) ? (double) Math.max(Math.min((((float) i) / (f * 2.0f)) - 1.0f, 1.0f), 0.0f) : d;
    }

    /* access modifiers changed from: private */
    public static double smoothForHeight(int i, float f, double d) {
        return isHeightCollapsed(i, f, f, d) ? (double) Math.max(Math.min((((float) i) / (f * 2.0f)) - 1.0f, 1.0f), 0.0f) : d;
    }

    /* access modifiers changed from: private */
    public static double mForHeight(float f, double d) {
        return ((double) f) * (1.0d - Math.cos(d));
    }

    /* access modifiers changed from: private */
    public static double nForHeight(float f, double d) {
        return ((double) f) * (1.0d - Math.sin(d));
    }

    /* access modifiers changed from: private */
    public static double mForWidth(float f, double d) {
        return ((double) f) * (1.0d - Math.sin(d));
    }

    /* access modifiers changed from: private */
    public static double nForWidth(float f, double d) {
        return ((double) f) * (1.0d - Math.cos(d));
    }

    /* access modifiers changed from: private */
    public static double pForWidth(float f, double d) {
        return ((double) f) * (1.0d - Math.tan(d / 2.0d));
    }

    /* access modifiers changed from: private */
    public static double pForHeight(float f, double d) {
        return ((double) f) * (1.0d - Math.tan(d / 2.0d));
    }

    /* access modifiers changed from: private */
    public static double xForWidth(float f, double d) {
        return ((((double) f) * 1.5d) * Math.tan(d / 2.0d)) / (Math.cos(d) + 1.0d);
    }

    /* access modifiers changed from: private */
    public static double xForHeight(float f, double d) {
        return ((((double) f) * 1.5d) * Math.tan(d / 2.0d)) / (Math.cos(d) + 1.0d);
    }

    /* access modifiers changed from: private */
    public static double kForWidth(double d, double d2) {
        if (d2 == 0.0d) {
            return 0.0d;
        }
        double d3 = d2 / 2.0d;
        return ((((d + Math.tan(d3)) * 2.0d) * (Math.cos(d2) + 1.0d)) / (Math.tan(d3) * 3.0d)) - 1.0d;
    }

    /* access modifiers changed from: private */
    public static double kForHeight(double d, double d2) {
        if (d2 == 0.0d) {
            return 0.0d;
        }
        double d3 = d2 / 2.0d;
        return ((((d + Math.tan(d3)) * 2.0d) * (Math.cos(d2) + 1.0d)) / (Math.tan(d3) * 3.0d)) - 1.0d;
    }

    public static class CornerData {
        public PointF[] bezierAnchorHorizontal = new PointF[4];
        public PointF[] bezierAnchorVertical = new PointF[4];
        public float radius;
        public RectF rect;
        public double smoothForHorizontal;
        public double smoothForVertical;
        public float swapAngle;
        public double thetaForHorizontal;
        public double thetaForVertical;

        public void build(float f, int i, int i2, double d, int i3) {
            this.radius = f;
            this.smoothForHorizontal = SmoothPathProvider.smoothForWidth(i, f, d);
            this.smoothForVertical = SmoothPathProvider.smoothForHeight(i2, this.radius, d);
            this.thetaForHorizontal = SmoothPathProvider.thetaForWidth(this.smoothForHorizontal);
            double thetaForHeight = SmoothPathProvider.thetaForHeight(this.smoothForVertical);
            this.thetaForVertical = thetaForHeight;
            this.swapAngle = (float) SmoothPathProvider.radToAngle((1.5707963267948966d - thetaForHeight) - this.thetaForHorizontal);
            double kForWidth = SmoothPathProvider.kForWidth(this.smoothForHorizontal, this.thetaForHorizontal);
            double mForWidth = SmoothPathProvider.mForWidth(this.radius, this.thetaForHorizontal);
            double nForWidth = SmoothPathProvider.nForWidth(this.radius, this.thetaForHorizontal);
            double pForWidth = SmoothPathProvider.pForWidth(this.radius, this.thetaForHorizontal);
            double xForWidth = SmoothPathProvider.xForWidth(this.radius, this.thetaForHorizontal);
            double yForWidth = SmoothPathProvider.yForWidth(kForWidth, xForWidth);
            double kForHeight = SmoothPathProvider.kForHeight(this.smoothForVertical, this.thetaForVertical);
            double mForHeight = SmoothPathProvider.mForHeight(this.radius, this.thetaForVertical);
            double nForHeight = SmoothPathProvider.nForHeight(this.radius, this.thetaForVertical);
            double pForHeight = SmoothPathProvider.pForHeight(this.radius, this.thetaForVertical);
            double xForHeight = SmoothPathProvider.xForHeight(this.radius, this.thetaForVertical);
            double yForHeight = SmoothPathProvider.yForHeight(kForHeight, xForHeight);
            if (i3 == 0) {
                float f2 = this.radius;
                this.rect = new RectF(0.0f, 0.0f, f2 * 2.0f, f2 * 2.0f);
                this.bezierAnchorHorizontal[0] = new PointF((float) mForWidth, (float) nForWidth);
                this.bezierAnchorHorizontal[1] = new PointF((float) pForWidth, 0.0f);
                double d2 = pForWidth + xForWidth;
                this.bezierAnchorHorizontal[2] = new PointF((float) d2, 0.0f);
                this.bezierAnchorHorizontal[3] = new PointF((float) (d2 + yForWidth), 0.0f);
                double d3 = pForHeight + xForHeight;
                this.bezierAnchorVertical[0] = new PointF(0.0f, (float) (d3 + yForHeight));
                this.bezierAnchorVertical[1] = new PointF(0.0f, (float) d3);
                this.bezierAnchorVertical[2] = new PointF(0.0f, (float) pForHeight);
                this.bezierAnchorVertical[3] = new PointF((float) mForHeight, (float) nForHeight);
            } else if (i3 == 1) {
                float f3 = (float) i;
                float f4 = this.radius;
                this.rect = new RectF(f3 - (f4 * 2.0f), 0.0f, f3, f4 * 2.0f);
                double d4 = (double) i;
                double d5 = d4 - pForWidth;
                double d6 = d5 - xForWidth;
                this.bezierAnchorHorizontal[0] = new PointF((float) (d6 - yForWidth), 0.0f);
                this.bezierAnchorHorizontal[1] = new PointF((float) d6, 0.0f);
                this.bezierAnchorHorizontal[2] = new PointF((float) d5, 0.0f);
                this.bezierAnchorHorizontal[3] = new PointF((float) (d4 - mForWidth), (float) nForWidth);
                this.bezierAnchorVertical[0] = new PointF((float) (d4 - mForHeight), (float) nForHeight);
                this.bezierAnchorVertical[1] = new PointF(f3, (float) pForHeight);
                double d7 = pForHeight + xForHeight;
                this.bezierAnchorVertical[2] = new PointF(f3, (float) d7);
                this.bezierAnchorVertical[3] = new PointF(f3, (float) (d7 + yForHeight));
            } else if (i3 == 2) {
                float f5 = (float) i;
                float f6 = this.radius;
                float f7 = (float) i2;
                this.rect = new RectF(f5 - (f6 * 2.0f), f7 - (f6 * 2.0f), f5, f7);
                double d8 = (double) i;
                double d9 = (double) i2;
                this.bezierAnchorHorizontal[0] = new PointF((float) (d8 - mForWidth), (float) (d9 - nForWidth));
                double d10 = d8 - pForWidth;
                this.bezierAnchorHorizontal[1] = new PointF((float) d10, f7);
                double d11 = d10 - xForWidth;
                this.bezierAnchorHorizontal[2] = new PointF((float) d11, f7);
                this.bezierAnchorHorizontal[3] = new PointF((float) (d11 - yForWidth), f7);
                double d12 = d9 - pForHeight;
                double d13 = d12 - xForHeight;
                this.bezierAnchorVertical[0] = new PointF(f5, (float) (d13 - yForHeight));
                this.bezierAnchorVertical[1] = new PointF(f5, (float) d13);
                this.bezierAnchorVertical[2] = new PointF(f5, (float) d12);
                this.bezierAnchorVertical[3] = new PointF((float) (d8 - mForHeight), (float) (d9 - nForHeight));
            } else if (i3 == 3) {
                float f8 = (float) i2;
                float f9 = this.radius;
                this.rect = new RectF(0.0f, f8 - (f9 * 2.0f), f9 * 2.0f, f8);
                double d14 = pForWidth + xForWidth;
                this.bezierAnchorHorizontal[0] = new PointF((float) (d14 + yForWidth), f8);
                this.bezierAnchorHorizontal[1] = new PointF((float) d14, f8);
                this.bezierAnchorHorizontal[2] = new PointF((float) pForWidth, f8);
                double d15 = (double) i2;
                this.bezierAnchorHorizontal[3] = new PointF((float) mForWidth, (float) (d15 - nForWidth));
                this.bezierAnchorVertical[0] = new PointF((float) mForHeight, (float) (d15 - nForHeight));
                double d16 = d15 - pForHeight;
                this.bezierAnchorVertical[1] = new PointF(0.0f, (float) d16);
                double d17 = d16 - xForHeight;
                this.bezierAnchorVertical[2] = new PointF(0.0f, (float) d17);
                this.bezierAnchorVertical[3] = new PointF(0.0f, (float) (d17 - yForHeight));
            }
        }
    }

    public static class SmoothData {
        public CornerData bottomLeft = null;
        public CornerData bottomRight = null;
        public int height;
        public double smooth;
        public CornerData topLeft = null;
        public CornerData topRight = null;
        public int width;

        public SmoothData(int i, int i2, double d) {
            this.width = i;
            this.height = i2;
            this.smooth = d;
        }
    }
}

package com.android.systemui;

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
        int i3 = i;
        int i4 = i2;
        float[] fArr2 = fArr;
        this.mAllData = new SmoothData(i3, i4, d);
        if (fArr2 != null) {
            float[] fArr3 = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};
            for (int i5 = 0; i5 < Math.min(fArr3.length, fArr2.length); i5++) {
                fArr3[i5] = fArr2[i5];
            }
            float f = fArr3[0];
            float f2 = fArr3[1];
            float f3 = fArr3[2];
            float f4 = fArr3[3];
            float f5 = fArr3[4];
            float f6 = fArr3[5];
            float f7 = fArr3[6];
            float f8 = fArr3[7];
            float f9 = (float) i3;
            if (f + f3 > f9) {
                f = (fArr3[0] * f9) / (fArr3[0] + fArr3[2]);
                f3 = (fArr3[2] * f9) / (fArr3[0] + fArr3[2]);
            }
            float f10 = (float) i4;
            if (f4 + f6 > f10) {
                f4 = (fArr3[3] * f10) / (fArr3[3] + fArr3[5]);
                f6 = (fArr3[5] * f10) / (fArr3[3] + fArr3[5]);
            }
            float f11 = f6;
            if (f5 + f7 > f9) {
                f5 = (fArr3[4] * f9) / (fArr3[4] + fArr3[6]);
                f7 = (f9 * fArr3[6]) / (fArr3[4] + fArr3[6]);
            }
            float f12 = f7;
            if (f8 + f2 > f10) {
                f8 = (fArr3[7] * f10) / (fArr3[7] + fArr3[1]);
                f2 = (f10 * fArr3[1]) / (fArr3[7] + fArr3[1]);
            }
            ensureFourCornerData();
            CornerData cornerData = this.mAllData.topLeft;
            float min = Math.min(f, f2);
            CornerData cornerData2 = cornerData;
            float f13 = min;
            int i6 = i;
            int i7 = i2;
            double d2 = d;
            cornerData2.build(f13, i6, i7, d2, 0);
            this.mAllData.topRight.build(Math.min(f3, f4), i6, i7, d2, 1);
            this.mAllData.bottomRight.build(Math.min(f5, f11), i6, i7, d2, 2);
            this.mAllData.bottomLeft.build(Math.min(f12, f8), i, i2, d, 3);
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
            double d2 = d;
            int i4 = i3;
            this.radius = f;
            this.smoothForHorizontal = SmoothPathProvider.smoothForWidth(i, this.radius, d2);
            this.smoothForVertical = SmoothPathProvider.smoothForHeight(i2, this.radius, d2);
            this.thetaForHorizontal = SmoothPathProvider.thetaForWidth(this.smoothForHorizontal);
            this.thetaForVertical = SmoothPathProvider.thetaForHeight(this.smoothForVertical);
            this.swapAngle = (float) SmoothPathProvider.radToAngle((1.5707963267948966d - this.thetaForVertical) - this.thetaForHorizontal);
            double access$500 = SmoothPathProvider.kForWidth(this.smoothForHorizontal, this.thetaForHorizontal);
            double access$600 = SmoothPathProvider.mForWidth(this.radius, this.thetaForHorizontal);
            double access$700 = SmoothPathProvider.nForWidth(this.radius, this.thetaForHorizontal);
            double access$800 = SmoothPathProvider.pForWidth(this.radius, this.thetaForHorizontal);
            double access$900 = SmoothPathProvider.xForWidth(this.radius, this.thetaForHorizontal);
            double access$1000 = SmoothPathProvider.yForWidth(access$500, access$900);
            double access$1100 = SmoothPathProvider.kForHeight(this.smoothForVertical, this.thetaForVertical);
            double access$1200 = SmoothPathProvider.mForHeight(this.radius, this.thetaForVertical);
            double access$1300 = SmoothPathProvider.nForHeight(this.radius, this.thetaForVertical);
            double access$1400 = SmoothPathProvider.pForHeight(this.radius, this.thetaForVertical);
            double access$1500 = SmoothPathProvider.xForHeight(this.radius, this.thetaForVertical);
            double access$1600 = SmoothPathProvider.yForHeight(access$1100, access$1500);
            if (i4 == 0) {
                float f2 = this.radius;
                this.rect = new RectF(0.0f, 0.0f, f2 * 2.0f, f2 * 2.0f);
                this.bezierAnchorHorizontal[0] = new PointF((float) access$600, (float) access$700);
                this.bezierAnchorHorizontal[1] = new PointF((float) access$800, 0.0f);
                double d3 = access$800 + access$900;
                this.bezierAnchorHorizontal[2] = new PointF((float) d3, 0.0f);
                this.bezierAnchorHorizontal[3] = new PointF((float) (d3 + access$1000), 0.0f);
                double d4 = access$1400 + access$1500;
                this.bezierAnchorVertical[0] = new PointF(0.0f, (float) (d4 + access$1600));
                this.bezierAnchorVertical[1] = new PointF(0.0f, (float) d4);
                this.bezierAnchorVertical[2] = new PointF(0.0f, (float) access$1400);
                this.bezierAnchorVertical[3] = new PointF((float) access$1200, (float) access$1300);
                return;
            }
            double d5 = access$1600;
            double d6 = access$1200;
            double d7 = access$1400;
            double d8 = access$1500;
            double d9 = access$1300;
            if (i4 == 1) {
                int i5 = i;
                double d10 = d8;
                float f3 = (float) i5;
                double d11 = d9;
                float f4 = this.radius;
                this.rect = new RectF(f3 - (f4 * 2.0f), 0.0f, f3, f4 * 2.0f);
                double d12 = (double) i5;
                double d13 = d12 - access$800;
                double d14 = d13 - access$900;
                this.bezierAnchorHorizontal[0] = new PointF((float) (d14 - access$1000), 0.0f);
                this.bezierAnchorHorizontal[1] = new PointF((float) d14, 0.0f);
                this.bezierAnchorHorizontal[2] = new PointF((float) d13, 0.0f);
                this.bezierAnchorHorizontal[3] = new PointF((float) (d12 - access$600), (float) access$700);
                this.bezierAnchorVertical[0] = new PointF((float) (d12 - d6), (float) d11);
                double d15 = d7;
                this.bezierAnchorVertical[1] = new PointF(f3, (float) d15);
                double d16 = d15 + d10;
                this.bezierAnchorVertical[2] = new PointF(f3, (float) d16);
                this.bezierAnchorVertical[3] = new PointF(f3, (float) (d16 + d5));
                return;
            }
            double d17 = d6;
            double d18 = d9;
            double d19 = d8;
            double d20 = d7;
            int i6 = i;
            if (i4 == 2) {
                float f5 = (float) i6;
                double d21 = d18;
                float f6 = this.radius;
                double d22 = d20;
                int i7 = i2;
                float f7 = (float) i7;
                this.rect = new RectF(f5 - (f6 * 2.0f), f7 - (f6 * 2.0f), f5, f7);
                double d23 = access$900;
                double d24 = (double) i6;
                double d25 = (double) i7;
                this.bezierAnchorHorizontal[0] = new PointF((float) (d24 - access$600), (float) (d25 - access$700));
                double d26 = d24 - access$800;
                this.bezierAnchorHorizontal[1] = new PointF((float) d26, f7);
                double d27 = d26 - d23;
                this.bezierAnchorHorizontal[2] = new PointF((float) d27, f7);
                this.bezierAnchorHorizontal[3] = new PointF((float) (d27 - access$1000), f7);
                double d28 = d25 - d22;
                double d29 = d28 - d19;
                this.bezierAnchorVertical[0] = new PointF(f5, (float) (d29 - d5));
                this.bezierAnchorVertical[1] = new PointF(f5, (float) d29);
                this.bezierAnchorVertical[2] = new PointF(f5, (float) d28);
                this.bezierAnchorVertical[3] = new PointF((float) (d24 - d17), (float) (d25 - d21));
                return;
            }
            double d30 = d20;
            double d31 = d18;
            double d32 = access$900;
            int i8 = i2;
            if (i4 == 3) {
                float f8 = (float) i8;
                float f9 = this.radius;
                this.rect = new RectF(0.0f, f8 - (f9 * 2.0f), f9 * 2.0f, f8);
                double d33 = access$800 + d32;
                this.bezierAnchorHorizontal[0] = new PointF((float) (d33 + access$1000), f8);
                this.bezierAnchorHorizontal[1] = new PointF((float) d33, f8);
                this.bezierAnchorHorizontal[2] = new PointF((float) access$800, f8);
                double d34 = (double) i8;
                this.bezierAnchorHorizontal[3] = new PointF((float) access$600, (float) (d34 - access$700));
                this.bezierAnchorVertical[0] = new PointF((float) d17, (float) (d34 - d31));
                double d35 = d34 - d30;
                this.bezierAnchorVertical[1] = new PointF(0.0f, (float) d35);
                double d36 = d35 - d19;
                this.bezierAnchorVertical[2] = new PointF(0.0f, (float) d36);
                this.bezierAnchorVertical[3] = new PointF(0.0f, (float) (d36 - d5));
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

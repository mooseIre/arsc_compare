package com.android.systemui.assist.ui;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.Log;
import android.util.Pair;
import com.android.systemui.assist.ui.CornerPathRenderer;

public class PerimeterPathGuide {
    private final int mBottomCornerRadiusPx;
    private final CornerPathRenderer mCornerPathRenderer;
    private final int mDeviceHeightPx;
    private final int mDeviceWidthPx;
    private final int mEdgeInset;
    private RegionAttributes[] mRegions;
    private int mRotation;
    private final Path mScratchPath = new Path();
    private final PathMeasure mScratchPathMeasure;
    private final int mTopCornerRadiusPx;

    public enum Region {
        BOTTOM,
        BOTTOM_RIGHT,
        RIGHT,
        TOP_RIGHT,
        TOP,
        TOP_LEFT,
        LEFT,
        BOTTOM_LEFT
    }

    /* access modifiers changed from: private */
    public class RegionAttributes {
        public float absoluteLength;
        public float endCoordinate;
        public float normalizedLength;
        public Path path;

        private RegionAttributes(PerimeterPathGuide perimeterPathGuide) {
        }
    }

    public PerimeterPathGuide(Context context, CornerPathRenderer cornerPathRenderer, int i, int i2, int i3) {
        int i4 = 0;
        this.mScratchPathMeasure = new PathMeasure(this.mScratchPath, false);
        this.mRotation = 0;
        this.mCornerPathRenderer = cornerPathRenderer;
        this.mDeviceWidthPx = i2;
        this.mDeviceHeightPx = i3;
        this.mTopCornerRadiusPx = DisplayUtils.getCornerRadiusTop(context);
        this.mBottomCornerRadiusPx = DisplayUtils.getCornerRadiusBottom(context);
        this.mEdgeInset = i;
        this.mRegions = new RegionAttributes[8];
        while (true) {
            RegionAttributes[] regionAttributesArr = this.mRegions;
            if (i4 < regionAttributesArr.length) {
                regionAttributesArr[i4] = new RegionAttributes();
                i4++;
            } else {
                computeRegions();
                return;
            }
        }
    }

    public void setRotation(int i) {
        if (i == this.mRotation) {
            return;
        }
        if (i == 0 || i == 1 || i == 2 || i == 3) {
            this.mRotation = i;
            computeRegions();
            return;
        }
        Log.e("PerimeterPathGuide", "Invalid rotation provided: " + i);
    }

    public void strokeSegment(Path path, float f, float f2) {
        path.reset();
        float f3 = ((f % 1.0f) + 1.0f) % 1.0f;
        float f4 = ((f2 % 1.0f) + 1.0f) % 1.0f;
        if (f3 > f4) {
            strokeSegmentInternal(path, f3, 1.0f);
            f3 = 0.0f;
        }
        strokeSegmentInternal(path, f3, f4);
    }

    public float getRegionWidth(Region region) {
        return this.mRegions[region.ordinal()].normalizedLength;
    }

    private int getPhysicalCornerRadius(CornerPathRenderer.Corner corner) {
        if (corner == CornerPathRenderer.Corner.BOTTOM_LEFT || corner == CornerPathRenderer.Corner.BOTTOM_RIGHT) {
            return this.mBottomCornerRadiusPx;
        }
        return this.mTopCornerRadiusPx;
    }

    private void computeRegions() {
        int i = this.mDeviceWidthPx;
        int i2 = this.mDeviceHeightPx;
        int i3 = this.mRotation;
        int i4 = 0;
        int i5 = i3 != 1 ? i3 != 2 ? i3 != 3 ? 0 : -270 : -180 : -90;
        Matrix matrix = new Matrix();
        matrix.postRotate((float) i5, (float) (this.mDeviceWidthPx / 2), (float) (this.mDeviceHeightPx / 2));
        int i6 = this.mRotation;
        if (i6 == 1 || i6 == 3) {
            i2 = this.mDeviceWidthPx;
            i = this.mDeviceHeightPx;
            matrix.postTranslate((float) ((i - i2) / 2), (float) ((i2 - i) / 2));
        }
        CornerPathRenderer.Corner rotatedCorner = getRotatedCorner(CornerPathRenderer.Corner.BOTTOM_LEFT);
        CornerPathRenderer.Corner rotatedCorner2 = getRotatedCorner(CornerPathRenderer.Corner.BOTTOM_RIGHT);
        CornerPathRenderer.Corner rotatedCorner3 = getRotatedCorner(CornerPathRenderer.Corner.TOP_LEFT);
        CornerPathRenderer.Corner rotatedCorner4 = getRotatedCorner(CornerPathRenderer.Corner.TOP_RIGHT);
        this.mRegions[Region.BOTTOM_LEFT.ordinal()].path = this.mCornerPathRenderer.getInsetPath(rotatedCorner, (float) this.mEdgeInset);
        this.mRegions[Region.BOTTOM_RIGHT.ordinal()].path = this.mCornerPathRenderer.getInsetPath(rotatedCorner2, (float) this.mEdgeInset);
        this.mRegions[Region.TOP_RIGHT.ordinal()].path = this.mCornerPathRenderer.getInsetPath(rotatedCorner4, (float) this.mEdgeInset);
        this.mRegions[Region.TOP_LEFT.ordinal()].path = this.mCornerPathRenderer.getInsetPath(rotatedCorner3, (float) this.mEdgeInset);
        this.mRegions[Region.BOTTOM_LEFT.ordinal()].path.transform(matrix);
        this.mRegions[Region.BOTTOM_RIGHT.ordinal()].path.transform(matrix);
        this.mRegions[Region.TOP_RIGHT.ordinal()].path.transform(matrix);
        this.mRegions[Region.TOP_LEFT.ordinal()].path.transform(matrix);
        Path path = new Path();
        path.moveTo((float) getPhysicalCornerRadius(rotatedCorner), (float) (i2 - this.mEdgeInset));
        path.lineTo((float) (i - getPhysicalCornerRadius(rotatedCorner2)), (float) (i2 - this.mEdgeInset));
        this.mRegions[Region.BOTTOM.ordinal()].path = path;
        Path path2 = new Path();
        path2.moveTo((float) (i - getPhysicalCornerRadius(rotatedCorner4)), (float) this.mEdgeInset);
        path2.lineTo((float) getPhysicalCornerRadius(rotatedCorner3), (float) this.mEdgeInset);
        this.mRegions[Region.TOP.ordinal()].path = path2;
        Path path3 = new Path();
        path3.moveTo((float) (i - this.mEdgeInset), (float) (i2 - getPhysicalCornerRadius(rotatedCorner2)));
        path3.lineTo((float) (i - this.mEdgeInset), (float) getPhysicalCornerRadius(rotatedCorner4));
        this.mRegions[Region.RIGHT.ordinal()].path = path3;
        Path path4 = new Path();
        path4.moveTo((float) this.mEdgeInset, (float) getPhysicalCornerRadius(rotatedCorner3));
        path4.lineTo((float) this.mEdgeInset, (float) (i2 - getPhysicalCornerRadius(rotatedCorner)));
        this.mRegions[Region.LEFT.ordinal()].path = path4;
        PathMeasure pathMeasure = new PathMeasure();
        float f = 0.0f;
        float f2 = 0.0f;
        int i7 = 0;
        while (true) {
            RegionAttributes[] regionAttributesArr = this.mRegions;
            if (i7 >= regionAttributesArr.length) {
                break;
            }
            pathMeasure.setPath(regionAttributesArr[i7].path, false);
            this.mRegions[i7].absoluteLength = pathMeasure.getLength();
            f2 += this.mRegions[i7].absoluteLength;
            i7++;
        }
        while (true) {
            RegionAttributes[] regionAttributesArr2 = this.mRegions;
            if (i4 < regionAttributesArr2.length) {
                regionAttributesArr2[i4].normalizedLength = regionAttributesArr2[i4].absoluteLength / f2;
                f += regionAttributesArr2[i4].absoluteLength;
                regionAttributesArr2[i4].endCoordinate = f / f2;
                i4++;
            } else {
                regionAttributesArr2[regionAttributesArr2.length - 1].endCoordinate = 1.0f;
                return;
            }
        }
    }

    private CornerPathRenderer.Corner getRotatedCorner(CornerPathRenderer.Corner corner) {
        int ordinal = corner.ordinal();
        int i = this.mRotation;
        if (i == 1) {
            ordinal += 3;
        } else if (i == 2) {
            ordinal += 2;
        } else if (i == 3) {
            ordinal++;
        }
        return CornerPathRenderer.Corner.values()[ordinal % 4];
    }

    private void strokeSegmentInternal(Path path, float f, float f2) {
        Pair<Region, Float> placePoint = placePoint(f);
        Pair<Region, Float> placePoint2 = placePoint(f2);
        if (((Region) placePoint.first).equals(placePoint2.first)) {
            strokeRegion(path, (Region) placePoint.first, ((Float) placePoint.second).floatValue(), ((Float) placePoint2.second).floatValue());
            return;
        }
        strokeRegion(path, (Region) placePoint.first, ((Float) placePoint.second).floatValue(), 1.0f);
        Region[] values = Region.values();
        boolean z = false;
        for (Region region : values) {
            if (region.equals(placePoint.first)) {
                z = true;
            } else if (!z) {
                continue;
            } else if (!region.equals(placePoint2.first)) {
                strokeRegion(path, region, 0.0f, 1.0f);
            } else {
                strokeRegion(path, region, 0.0f, ((Float) placePoint2.second).floatValue());
                return;
            }
        }
    }

    private void strokeRegion(Path path, Region region, float f, float f2) {
        if (f != f2) {
            this.mScratchPathMeasure.setPath(this.mRegions[region.ordinal()].path, false);
            PathMeasure pathMeasure = this.mScratchPathMeasure;
            pathMeasure.getSegment(f * pathMeasure.getLength(), f2 * this.mScratchPathMeasure.getLength(), path, true);
        }
    }

    private Pair<Region, Float> placePoint(float f) {
        if (0.0f > f || f > 1.0f) {
            f = ((f % 1.0f) + 1.0f) % 1.0f;
        }
        Region regionForPoint = getRegionForPoint(f);
        if (regionForPoint.equals(Region.BOTTOM)) {
            return Pair.create(regionForPoint, Float.valueOf(f / this.mRegions[regionForPoint.ordinal()].normalizedLength));
        }
        return Pair.create(regionForPoint, Float.valueOf((f - this.mRegions[regionForPoint.ordinal() - 1].endCoordinate) / this.mRegions[regionForPoint.ordinal()].normalizedLength));
    }

    private Region getRegionForPoint(float f) {
        if (f < 0.0f || f > 1.0f) {
            f = ((f % 1.0f) + 1.0f) % 1.0f;
        }
        Region[] values = Region.values();
        for (Region region : values) {
            if (f <= this.mRegions[region.ordinal()].endCoordinate) {
                return region;
            }
        }
        Log.e("PerimeterPathGuide", "Fell out of getRegionForPoint");
        return Region.BOTTOM;
    }
}

package com.android.systemui.wm;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.SystemProperties;
import android.provider.Settings;
import android.util.RotationUtils;
import android.util.Size;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.DisplayInfo;

public class DisplayLayout {
    private DisplayCutout mCutout;
    private int mDensityDpi;
    private boolean mHasNavigationBar = false;
    private boolean mHasStatusBar = false;
    private int mHeight;
    private int mNavBarFrameHeight = 0;
    private final Rect mNonDecorInsets = new Rect();
    private int mRotation;
    private final Rect mStableInsets = new Rect();
    private int mUiMode;
    private int mWidth;

    private static int getBoundIndexFromRotation(int i, int i2) {
        int i3 = i - i2;
        return i3 < 0 ? i3 + 4 : i3;
    }

    static boolean hasStatusBar(int i) {
        return i == 0;
    }

    public DisplayLayout() {
    }

    public DisplayLayout(Context context, Display display) {
        int displayId = display.getDisplayId();
        DisplayInfo displayInfo = new DisplayInfo();
        display.getDisplayInfo(displayInfo);
        init(displayInfo, context.getResources(), hasNavigationBar(displayInfo, context, displayId), hasStatusBar(displayId));
    }

    public DisplayLayout(DisplayLayout displayLayout) {
        set(displayLayout);
    }

    public void set(DisplayLayout displayLayout) {
        this.mUiMode = displayLayout.mUiMode;
        this.mWidth = displayLayout.mWidth;
        this.mHeight = displayLayout.mHeight;
        this.mCutout = displayLayout.mCutout;
        this.mRotation = displayLayout.mRotation;
        this.mDensityDpi = displayLayout.mDensityDpi;
        this.mHasNavigationBar = displayLayout.mHasNavigationBar;
        this.mHasStatusBar = displayLayout.mHasStatusBar;
        this.mNonDecorInsets.set(displayLayout.mNonDecorInsets);
        this.mStableInsets.set(displayLayout.mStableInsets);
    }

    private void init(DisplayInfo displayInfo, Resources resources, boolean z, boolean z2) {
        this.mUiMode = resources.getConfiguration().uiMode;
        this.mWidth = displayInfo.logicalWidth;
        this.mHeight = displayInfo.logicalHeight;
        this.mRotation = displayInfo.rotation;
        this.mCutout = displayInfo.displayCutout;
        this.mDensityDpi = displayInfo.logicalDensityDpi;
        this.mHasNavigationBar = z;
        this.mHasStatusBar = z2;
        recalcInsets(resources);
    }

    private void recalcInsets(Resources resources) {
        computeNonDecorInsets(resources, this.mRotation, this.mWidth, this.mHeight, this.mCutout, this.mUiMode, this.mNonDecorInsets, this.mHasNavigationBar);
        this.mStableInsets.set(this.mNonDecorInsets);
        boolean z = this.mHasStatusBar;
        if (z) {
            convertNonDecorInsetsToStableInsets(resources, this.mStableInsets, this.mWidth, this.mHeight, z);
        }
        this.mNavBarFrameHeight = getNavigationBarFrameHeight(resources, this.mWidth > this.mHeight);
    }

    public void rotateTo(Resources resources, int i) {
        int i2 = ((i - this.mRotation) + 4) % 4;
        boolean z = i2 % 2 != 0;
        int i3 = this.mWidth;
        int i4 = this.mHeight;
        this.mRotation = i;
        if (z) {
            this.mWidth = i4;
            this.mHeight = i3;
        }
        DisplayCutout displayCutout = this.mCutout;
        if (displayCutout != null && !displayCutout.isEmpty()) {
            this.mCutout = calculateDisplayCutoutForRotation(this.mCutout, i2, i3, i4);
        }
        recalcInsets(resources);
    }

    public Rect nonDecorInsets() {
        return this.mNonDecorInsets;
    }

    public Rect stableInsets() {
        return this.mStableInsets;
    }

    public int width() {
        return this.mWidth;
    }

    public int height() {
        return this.mHeight;
    }

    public int rotation() {
        return this.mRotation;
    }

    public float density() {
        return ((float) this.mDensityDpi) * 0.00625f;
    }

    public boolean isLandscape() {
        return this.mWidth > this.mHeight;
    }

    public int navBarFrameHeight() {
        return this.mNavBarFrameHeight;
    }

    public int getOrientation() {
        return this.mWidth > this.mHeight ? 2 : 1;
    }

    public void getStableBounds(Rect rect) {
        rect.set(0, 0, this.mWidth, this.mHeight);
        rect.inset(this.mStableInsets);
    }

    public int getNavigationBarPosition(Resources resources) {
        return navigationBarPosition(resources, this.mWidth, this.mHeight, this.mRotation);
    }

    public static void rotateBounds(Rect rect, Rect rect2, int i) {
        int i2 = ((i % 4) + 4) % 4;
        int i3 = rect.left;
        if (i2 == 1) {
            rect.left = rect.top;
            rect.top = rect2.right - rect.right;
            rect.right = rect.bottom;
            rect.bottom = rect2.right - i3;
        } else if (i2 == 2) {
            int i4 = rect2.right;
            rect.left = i4 - rect.right;
            rect.right = i4 - i3;
        } else if (i2 == 3) {
            rect.left = rect2.bottom - rect.bottom;
            rect.bottom = rect.right;
            rect.right = rect2.bottom - rect.top;
            rect.top = i3;
        }
    }

    private static void convertNonDecorInsetsToStableInsets(Resources resources, Rect rect, int i, int i2, boolean z) {
        if (z) {
            rect.top = Math.max(rect.top, getStatusBarHeight(i > i2, resources));
        }
    }

    static void computeNonDecorInsets(Resources resources, int i, int i2, int i3, DisplayCutout displayCutout, int i4, Rect rect, boolean z) {
        rect.setEmpty();
        if (z) {
            int navigationBarPosition = navigationBarPosition(resources, i2, i3, i);
            int navigationBarSize = getNavigationBarSize(resources, navigationBarPosition, i2 > i3, i4);
            if (navigationBarPosition == 4) {
                rect.bottom = navigationBarSize;
            } else if (navigationBarPosition == 2) {
                rect.right = navigationBarSize;
            } else if (navigationBarPosition == 1) {
                rect.left = navigationBarSize;
            }
        }
        if (displayCutout != null) {
            rect.left += displayCutout.getSafeInsetLeft();
            rect.top += displayCutout.getSafeInsetTop();
            rect.right += displayCutout.getSafeInsetRight();
            rect.bottom += displayCutout.getSafeInsetBottom();
        }
    }

    static int getStatusBarHeight(boolean z, Resources resources) {
        if (z) {
            return resources.getDimensionPixelSize(17105490);
        }
        return resources.getDimensionPixelSize(17105491);
    }

    public static DisplayCutout calculateDisplayCutoutForRotation(DisplayCutout displayCutout, int i, int i2, int i3) {
        if (displayCutout == null || displayCutout == DisplayCutout.NO_CUTOUT) {
            return null;
        }
        Insets rotateInsets = RotationUtils.rotateInsets(displayCutout.getWaterfallInsets(), i);
        if (i == 0) {
            return computeSafeInsets(displayCutout, i2, i3);
        }
        boolean z = true;
        if (!(i == 1 || i == 3)) {
            z = false;
        }
        Rect[] boundingRectsAll = displayCutout.getBoundingRectsAll();
        Rect[] rectArr = new Rect[boundingRectsAll.length];
        Rect rect = new Rect(0, 0, i2, i3);
        for (int i4 = 0; i4 < boundingRectsAll.length; i4++) {
            Rect rect2 = new Rect(boundingRectsAll[i4]);
            if (!rect2.isEmpty()) {
                rotateBounds(rect2, rect, i);
            }
            rectArr[getBoundIndexFromRotation(i4, i)] = rect2;
        }
        DisplayCutout fromBoundsAndWaterfall = DisplayCutout.fromBoundsAndWaterfall(rectArr, rotateInsets);
        int i5 = z ? i3 : i2;
        if (!z) {
            i2 = i3;
        }
        return computeSafeInsets(fromBoundsAndWaterfall, i5, i2);
    }

    public static DisplayCutout computeSafeInsets(DisplayCutout displayCutout, int i, int i2) {
        if (displayCutout == DisplayCutout.NO_CUTOUT) {
            return null;
        }
        return displayCutout.replaceSafeInsets(computeSafeInsets(new Size(i, i2), displayCutout));
    }

    private static Rect computeSafeInsets(Size size, DisplayCutout displayCutout) {
        if (size.getWidth() != size.getHeight()) {
            return new Rect(Math.max(displayCutout.getWaterfallInsets().left, findCutoutInsetForSide(size, displayCutout.getBoundingRectLeft(), 3)), Math.max(displayCutout.getWaterfallInsets().top, findCutoutInsetForSide(size, displayCutout.getBoundingRectTop(), 48)), Math.max(displayCutout.getWaterfallInsets().right, findCutoutInsetForSide(size, displayCutout.getBoundingRectRight(), 5)), Math.max(displayCutout.getWaterfallInsets().bottom, findCutoutInsetForSide(size, displayCutout.getBoundingRectBottom(), 80)));
        }
        throw new UnsupportedOperationException("not implemented: display=" + size + " cutout=" + displayCutout);
    }

    private static int findCutoutInsetForSide(Size size, Rect rect, int i) {
        if (rect.isEmpty()) {
            return 0;
        }
        if (i == 3) {
            return Math.max(0, rect.right);
        }
        if (i == 5) {
            return Math.max(0, size.getWidth() - rect.left);
        }
        if (i == 48) {
            return Math.max(0, rect.bottom);
        }
        if (i == 80) {
            return Math.max(0, size.getHeight() - rect.top);
        }
        throw new IllegalArgumentException("unknown gravity: " + i);
    }

    static boolean hasNavigationBar(DisplayInfo displayInfo, Context context, int i) {
        if (i == 0) {
            String str = SystemProperties.get("qemu.hw.mainkeys");
            if ("1".equals(str)) {
                return false;
            }
            if ("0".equals(str)) {
                return true;
            }
            return context.getResources().getBoolean(17891526);
        }
        boolean z = displayInfo.type == 5 && displayInfo.ownerUid != 1000;
        boolean z2 = Settings.Global.getInt(context.getContentResolver(), "force_desktop_mode_on_external_displays", 0) != 0;
        if ((displayInfo.flags & 64) == 0) {
            return z2 && !z;
        }
        return true;
    }

    public static int navigationBarPosition(Resources resources, int i, int i2, int i3) {
        if (!(i != i2 && resources.getBoolean(17891491)) || i <= i2) {
            return 4;
        }
        return i3 == 1 ? 2 : 1;
    }

    public static int getNavigationBarSize(Resources resources, int i, boolean z, int i2) {
        if ((i2 & 15) == 3) {
            if (i != 4) {
                return resources.getDimensionPixelSize(17105342);
            }
            return resources.getDimensionPixelSize(z ? 17105339 : 17105337);
        } else if (i != 4) {
            return resources.getDimensionPixelSize(17105341);
        } else {
            return resources.getDimensionPixelSize(z ? 17105338 : 17105336);
        }
    }

    public static int getNavigationBarFrameHeight(Resources resources, boolean z) {
        return resources.getDimensionPixelSize(z ? 17105334 : 17105333);
    }
}

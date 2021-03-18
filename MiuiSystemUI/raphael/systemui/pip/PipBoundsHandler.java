package com.android.systemui.pip;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.util.TypedValue;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.window.WindowContainerTransaction;
import com.android.systemui.wm.DisplayController;
import com.android.systemui.wm.DisplayLayout;
import java.io.PrintWriter;

public class PipBoundsHandler {
    private static final String TAG = "PipBoundsHandler";
    private float mAspectRatio;
    private final Context mContext;
    private int mCurrentMinSize;
    private float mDefaultAspectRatio;
    private int mDefaultMinSize;
    private int mDefaultStackGravity;
    private final DisplayController mDisplayController;
    private final DisplayInfo mDisplayInfo = new DisplayInfo();
    private final DisplayLayout mDisplayLayout;
    private final DisplayController.OnDisplaysChangedListener mDisplaysChangedListener = new DisplayController.OnDisplaysChangedListener() {
        /* class com.android.systemui.pip.PipBoundsHandler.AnonymousClass1 */

        @Override // com.android.systemui.wm.DisplayController.OnDisplaysChangedListener
        public void onDisplayAdded(int i) {
            if (i == PipBoundsHandler.this.mContext.getDisplayId()) {
                PipBoundsHandler.this.mDisplayLayout.set(PipBoundsHandler.this.mDisplayController.getDisplayLayout(i));
            }
        }
    };
    private int mImeHeight;
    private boolean mIsImeShowing;
    private boolean mIsShelfShowing;
    private ComponentName mLastPipComponentName;
    private float mMaxAspectRatio;
    private float mMinAspectRatio;
    private Size mOverrideMinimalSize;
    private Size mReentrySize;
    private float mReentrySnapFraction = -1.0f;
    private Point mScreenEdgeInsets;
    private int mShelfHeight;
    private final PipSnapAlgorithm mSnapAlgorithm;

    public PipBoundsHandler(Context context, PipSnapAlgorithm pipSnapAlgorithm, DisplayController displayController) {
        this.mContext = context;
        this.mSnapAlgorithm = pipSnapAlgorithm;
        this.mDisplayLayout = new DisplayLayout();
        this.mDisplayController = displayController;
        displayController.addDisplayWindowListener(this.mDisplaysChangedListener);
        reloadResources();
        this.mAspectRatio = this.mDefaultAspectRatio;
    }

    private void reloadResources() {
        Point point;
        Resources resources = this.mContext.getResources();
        this.mDefaultAspectRatio = resources.getFloat(17105072);
        this.mDefaultStackGravity = resources.getInteger(17694782);
        int dimensionPixelSize = resources.getDimensionPixelSize(17105157);
        this.mDefaultMinSize = dimensionPixelSize;
        this.mCurrentMinSize = dimensionPixelSize;
        String string = resources.getString(17039894);
        Size parseSize = !string.isEmpty() ? Size.parseSize(string) : null;
        if (parseSize == null) {
            point = new Point();
        } else {
            point = new Point(dpToPx((float) parseSize.getWidth(), resources.getDisplayMetrics()), dpToPx((float) parseSize.getHeight(), resources.getDisplayMetrics()));
        }
        this.mScreenEdgeInsets = point;
        this.mMinAspectRatio = resources.getFloat(17105075);
        this.mMaxAspectRatio = resources.getFloat(17105074);
    }

    public void setMinEdgeSize(int i) {
        this.mCurrentMinSize = i;
    }

    public boolean setShelfHeight(boolean z, int i) {
        if ((z && i > 0) == this.mIsShelfShowing && i == this.mShelfHeight) {
            return false;
        }
        this.mIsShelfShowing = z;
        this.mShelfHeight = i;
        return true;
    }

    public void onImeVisibilityChanged(boolean z, int i) {
        this.mIsImeShowing = z;
        this.mImeHeight = i;
    }

    public void onMovementBoundsChanged(Rect rect, Rect rect2, Rect rect3, DisplayInfo displayInfo) {
        getInsetBounds(rect);
        Rect defaultBounds = getDefaultBounds(-1.0f, null);
        rect2.set(defaultBounds);
        if (rect3.isEmpty()) {
            rect3.set(defaultBounds);
        }
        if (isValidPictureInPictureAspectRatio(this.mAspectRatio)) {
            transformBoundsToAspectRatio(rect2, this.mAspectRatio, false);
        }
        displayInfo.copyFrom(this.mDisplayInfo);
    }

    public void onSaveReentryBounds(ComponentName componentName, Rect rect) {
        this.mReentrySnapFraction = getSnapFraction(rect);
        this.mReentrySize = new Size(rect.width(), rect.height());
        this.mLastPipComponentName = componentName;
    }

    public void onResetReentryBounds(ComponentName componentName) {
        if (componentName.equals(this.mLastPipComponentName)) {
            onResetReentryBoundsUnchecked();
        }
    }

    private void onResetReentryBoundsUnchecked() {
        this.mReentrySnapFraction = -1.0f;
        this.mReentrySize = null;
        this.mLastPipComponentName = null;
    }

    public boolean hasSaveReentryBounds() {
        return this.mReentrySnapFraction != -1.0f;
    }

    public Rect getDisplayBounds() {
        DisplayInfo displayInfo = this.mDisplayInfo;
        return new Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
    }

    public int getDisplayRotation() {
        return this.mDisplayInfo.rotation;
    }

    public void onDisplayInfoChanged(DisplayInfo displayInfo) {
        this.mDisplayInfo.copyFrom(displayInfo);
    }

    public void onConfigurationChanged() {
        reloadResources();
    }

    public void onAspectRatioChanged(float f) {
        this.mAspectRatio = f;
    }

    /* access modifiers changed from: package-private */
    public Rect getDestinationBounds(ComponentName componentName, float f, Rect rect, Size size) {
        return getDestinationBounds(componentName, f, rect, size, false);
    }

    /* access modifiers changed from: package-private */
    public Rect getDestinationBounds(ComponentName componentName, float f, Rect rect, Size size, boolean z) {
        Rect rect2;
        if (!componentName.equals(this.mLastPipComponentName)) {
            onResetReentryBoundsUnchecked();
            this.mLastPipComponentName = componentName;
        }
        if (rect == null) {
            rect2 = new Rect(getDefaultBounds(this.mReentrySnapFraction, this.mReentrySize));
            if (this.mReentrySnapFraction == -1.0f && this.mReentrySize == null) {
                this.mOverrideMinimalSize = size;
            }
        } else {
            rect2 = new Rect(rect);
        }
        if (isValidPictureInPictureAspectRatio(f)) {
            transformBoundsToAspectRatio(rect2, f, z);
        }
        this.mAspectRatio = f;
        return rect2;
    }

    /* access modifiers changed from: package-private */
    public float getDefaultAspectRatio() {
        return this.mDefaultAspectRatio;
    }

    public void onDisplayRotationChangedNotInPip(int i) {
        this.mDisplayLayout.rotateTo(this.mContext.getResources(), i);
        this.mDisplayInfo.rotation = i;
        updateDisplayInfoIfNeeded();
    }

    public boolean onDisplayRotationChanged(Rect rect, Rect rect2, Rect rect3, int i, int i2, int i3, WindowContainerTransaction windowContainerTransaction) {
        if (i == this.mDisplayInfo.displayId && i2 != i3) {
            try {
                ActivityManager.StackInfo stackInfo = ActivityTaskManager.getService().getStackInfo(2, 0);
                if (stackInfo == null) {
                    return false;
                }
                Rect rect4 = new Rect(rect2);
                float snapFraction = getSnapFraction(rect4);
                this.mDisplayLayout.rotateTo(this.mContext.getResources(), i3);
                this.mDisplayInfo.rotation = i3;
                updateDisplayInfoIfNeeded();
                this.mSnapAlgorithm.applySnapFraction(rect4, getMovementBounds(rect4, false), snapFraction);
                getInsetBounds(rect3);
                rect.set(rect4);
                windowContainerTransaction.setBounds(stackInfo.stackToken, rect);
                return true;
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to get StackInfo for pinned stack", e);
            }
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0010, code lost:
        if (r0.logicalWidth < r0.logicalHeight) goto L_0x001d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001b, code lost:
        if (r0.logicalWidth > r0.logicalHeight) goto L_0x001d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateDisplayInfoIfNeeded() {
        /*
            r5 = this;
            android.view.DisplayInfo r0 = r5.mDisplayInfo
            int r1 = r0.rotation
            r2 = 1
            r3 = 0
            if (r1 == 0) goto L_0x0015
            r4 = 2
            if (r1 != r4) goto L_0x000c
            goto L_0x0015
        L_0x000c:
            int r1 = r0.logicalWidth
            int r0 = r0.logicalHeight
            if (r1 >= r0) goto L_0x0013
            goto L_0x001d
        L_0x0013:
            r2 = r3
            goto L_0x001d
        L_0x0015:
            android.view.DisplayInfo r0 = r5.mDisplayInfo
            int r1 = r0.logicalWidth
            int r0 = r0.logicalHeight
            if (r1 <= r0) goto L_0x0013
        L_0x001d:
            if (r2 == 0) goto L_0x0029
            android.view.DisplayInfo r5 = r5.mDisplayInfo
            int r0 = r5.logicalWidth
            int r1 = r5.logicalHeight
            r5.logicalWidth = r1
            r5.logicalHeight = r0
        L_0x0029:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.pip.PipBoundsHandler.updateDisplayInfoIfNeeded():void");
    }

    private boolean isValidPictureInPictureAspectRatio(float f) {
        return Float.compare(this.mMinAspectRatio, f) <= 0 && Float.compare(f, this.mMaxAspectRatio) <= 0;
    }

    public void transformBoundsToAspectRatio(Rect rect) {
        transformBoundsToAspectRatio(rect, this.mAspectRatio, true);
    }

    private void transformBoundsToAspectRatio(Rect rect, float f, boolean z) {
        Size size;
        float snapFraction = this.mSnapAlgorithm.getSnapFraction(rect, getMovementBounds(rect));
        if (z) {
            size = this.mSnapAlgorithm.getSizeForAspectRatio(new Size(rect.width(), rect.height()), f, (float) this.mCurrentMinSize);
        } else {
            int i = this.mDefaultMinSize;
            DisplayInfo displayInfo = this.mDisplayInfo;
            size = this.mSnapAlgorithm.getSizeForAspectRatio(f, (float) i, displayInfo.logicalWidth, displayInfo.logicalHeight);
        }
        int centerX = (int) (((float) rect.centerX()) - (((float) size.getWidth()) / 2.0f));
        int centerY = (int) (((float) rect.centerY()) - (((float) size.getHeight()) / 2.0f));
        rect.set(centerX, centerY, size.getWidth() + centerX, size.getHeight() + centerY);
        Size size2 = this.mOverrideMinimalSize;
        if (size2 != null) {
            transformBoundsToMinimalSize(rect, f, size2);
        }
        this.mSnapAlgorithm.applySnapFraction(rect, getMovementBounds(rect), snapFraction);
    }

    private void transformBoundsToMinimalSize(Rect rect, float f, Size size) {
        Size size2;
        if (size != null) {
            if (((float) size.getWidth()) / ((float) size.getHeight()) > f) {
                size2 = new Size(size.getWidth(), (int) (((float) size.getWidth()) / f));
            } else {
                size2 = new Size((int) (((float) size.getHeight()) * f), size.getHeight());
            }
            Gravity.apply(this.mDefaultStackGravity, size2.getWidth(), size2.getHeight(), new Rect(rect), rect);
        }
    }

    private Rect getDefaultBounds(float f, Size size) {
        Rect rect = new Rect();
        int i = 0;
        if (f == -1.0f || size == null) {
            Rect rect2 = new Rect();
            getInsetBounds(rect2);
            DisplayInfo displayInfo = this.mDisplayInfo;
            Size sizeForAspectRatio = this.mSnapAlgorithm.getSizeForAspectRatio(this.mDefaultAspectRatio, (float) this.mDefaultMinSize, displayInfo.logicalWidth, displayInfo.logicalHeight);
            int i2 = this.mDefaultStackGravity;
            int width = sizeForAspectRatio.getWidth();
            int height = sizeForAspectRatio.getHeight();
            int i3 = this.mIsImeShowing ? this.mImeHeight : 0;
            if (this.mIsShelfShowing) {
                i = this.mShelfHeight;
            }
            Gravity.apply(i2, width, height, rect2, 0, Math.max(i3, i), rect);
        } else {
            rect.set(0, 0, size.getWidth(), size.getHeight());
            this.mSnapAlgorithm.applySnapFraction(rect, getMovementBounds(rect), f);
        }
        return rect;
    }

    /* access modifiers changed from: protected */
    public void getInsetBounds(Rect rect) {
        Rect stableInsets = this.mDisplayLayout.stableInsets();
        int i = stableInsets.left;
        Point point = this.mScreenEdgeInsets;
        int i2 = point.x;
        int i3 = stableInsets.top;
        int i4 = point.y;
        DisplayInfo displayInfo = this.mDisplayInfo;
        rect.set(i + i2, i3 + i4, (displayInfo.logicalWidth - stableInsets.right) - i2, (displayInfo.logicalHeight - stableInsets.bottom) - i4);
    }

    private Rect getMovementBounds(Rect rect) {
        return getMovementBounds(rect, true);
    }

    private Rect getMovementBounds(Rect rect, boolean z) {
        Rect rect2 = new Rect();
        getInsetBounds(rect2);
        this.mSnapAlgorithm.getMovementBounds(rect, rect2, rect2, (!z || !this.mIsImeShowing) ? 0 : this.mImeHeight);
        return rect2;
    }

    public float getSnapFraction(Rect rect) {
        return this.mSnapAlgorithm.getSnapFraction(rect, getMovementBounds(rect));
    }

    public void applySnapFraction(Rect rect, float f) {
        this.mSnapAlgorithm.applySnapFraction(rect, getMovementBounds(rect), f);
    }

    private int dpToPx(float f, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(1, f, displayMetrics);
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + TAG);
        printWriter.println(str2 + "mLastPipComponentName=" + this.mLastPipComponentName);
        printWriter.println(str2 + "mReentrySnapFraction=" + this.mReentrySnapFraction);
        printWriter.println(str2 + "mReentrySize=" + this.mReentrySize);
        printWriter.println(str2 + "mDisplayInfo=" + this.mDisplayInfo);
        printWriter.println(str2 + "mDefaultAspectRatio=" + this.mDefaultAspectRatio);
        printWriter.println(str2 + "mMinAspectRatio=" + this.mMinAspectRatio);
        printWriter.println(str2 + "mMaxAspectRatio=" + this.mMaxAspectRatio);
        printWriter.println(str2 + "mAspectRatio=" + this.mAspectRatio);
        printWriter.println(str2 + "mDefaultStackGravity=" + this.mDefaultStackGravity);
        printWriter.println(str2 + "mIsImeShowing=" + this.mIsImeShowing);
        printWriter.println(str2 + "mImeHeight=" + this.mImeHeight);
        printWriter.println(str2 + "mIsShelfShowing=" + this.mIsShelfShowing);
        printWriter.println(str2 + "mShelfHeight=" + this.mShelfHeight);
    }
}

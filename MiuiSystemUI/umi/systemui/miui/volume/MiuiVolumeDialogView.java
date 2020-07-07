package com.android.systemui.miui.volume;

import android.content.Context;
import android.graphics.Region;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Display;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.systemui.miui.volume.MiuiVolumeDialogMotion;
import com.android.systemui.miui.volume.widget.ExpandCollapseLinearLayout;

public class MiuiVolumeDialogView extends ExpandCollapseLinearLayout implements DisplayManager.DisplayListener, ViewTreeObserver.OnComputeInternalInsetsListener {
    private boolean mAttached;
    private ViewGroup mDialogContentView;
    private Display mDisplay;
    private int[] mDisplayLocation;
    private ImageView mExpandButton;
    private int mLastRotation;
    private MiuiVolumeDialogMotion mMotion;
    private boolean mObservingInternalInsets;
    private MiuiRingerModeLayout mRingerModeLayout;
    private FrameLayout mTempColumnContainer;

    public void onDisplayAdded(int i) {
    }

    public void onDisplayRemoved(int i) {
    }

    public MiuiVolumeDialogView(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiVolumeDialogView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiVolumeDialogView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mDisplayLocation = new int[2];
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mExpandButton = (ImageView) findViewById(R$id.volume_expand_button);
        this.mDialogContentView = (ViewGroup) findViewById(R$id.volume_dialog_content);
        this.mRingerModeLayout = (MiuiRingerModeLayout) findViewById(R$id.miui_volume_ringer_layout);
        this.mTempColumnContainer = (FrameLayout) findViewById(R$id.volume_dialog_column_temp);
        this.mMotion = new MiuiVolumeDialogMotion(this, this.mDialogContentView, this.mTempColumnContainer, this.mExpandButton, this.mRingerModeLayout);
    }

    public void onExpandStateUpdated(boolean z) {
        super.onExpandStateUpdated(z);
        this.mMotion.startExpandH(z);
        this.mRingerModeLayout.updateExpandedH(z);
        updateExpandButtonH(z);
        setInternalInsetsListener();
    }

    private void setInternalInsetsListener() {
        boolean z = this.mAttached && !isExpanded();
        if (z != this.mObservingInternalInsets) {
            this.mObservingInternalInsets = z;
            if (z) {
                getViewTreeObserver().addOnComputeInternalInsetsListener(this);
                requestLayout();
                return;
            }
            getViewTreeObserver().removeOnComputeInternalInsetsListener(this);
        }
    }

    public void setMotionCallback(MiuiVolumeDialogMotion.Callback callback) {
        this.mMotion.setCallback(callback);
    }

    public void showH() {
        this.mMotion.startShow();
        this.mRingerModeLayout.init();
    }

    public void dismissH(Runnable runnable) {
        this.mMotion.startDismiss(runnable);
        this.mRingerModeLayout.cleanUp();
    }

    public boolean isAnimating() {
        return this.mMotion.isAnimating();
    }

    public boolean isOffMode() {
        return this.mRingerModeLayout.getRingerMode() == 0;
    }

    public void updateFooterVisibility(boolean z) {
        Util.setVisOrGone(this.mRingerModeLayout, z);
    }

    private void updateExpandButtonH(boolean z) {
        this.mExpandButton.setContentDescription(getContext().getString(z ? R$string.accessibility_volume_collapse : R$string.accessibility_volume_expand));
    }

    public void setSilenceMode(int i, boolean z) {
        this.mRingerModeLayout.setSilenceMode(i, z);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Display display = getDisplay();
        this.mDisplay = display;
        this.mMotion.setDisplay(display);
        this.mLastRotation = this.mDisplay.getRotation();
        ((DisplayManager) getContext().getSystemService("display")).registerDisplayListener(this, (Handler) null);
        this.mAttached = true;
        setInternalInsetsListener();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mDisplay = null;
        this.mMotion.setDisplay((Display) null);
        ((DisplayManager) getContext().getSystemService("display")).unregisterDisplayListener(this);
        this.mAttached = false;
        setInternalInsetsListener();
    }

    public void onDisplayChanged(int i) {
        Display display = this.mDisplay;
        if (display != null) {
            int rotation = display.getRotation();
            if (this.mLastRotation != rotation) {
                this.mMotion.updateStates();
            }
            this.mLastRotation = rotation;
        }
    }

    public void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        if (!isExpanded()) {
            internalInsetsInfo.setTouchableInsets(3);
            if (this.mDisplay.getRotation() == 3) {
                getLocationOnScreen(this.mDisplayLocation);
            } else {
                this.mDisplayLocation[0] = getLeft();
                this.mDisplayLocation[1] = getTop();
            }
            Region region = internalInsetsInfo.touchableRegion;
            int[] iArr = this.mDisplayLocation;
            region.set(iArr[0], iArr[1], iArr[0] + getWidth(), this.mDisplayLocation[1] + getHeight());
        }
    }
}

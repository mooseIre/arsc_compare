package com.android.systemui.statusbar.phone;

import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Handler;
import android.view.CompositionSamplingListener;
import android.view.View;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.Dependency;
import com.miui.systemui.statusbar.phone.SmartDarkObserver;

public class PhoneStatusBarTintController implements View.OnAttachStateChangeListener, View.OnLayoutChangeListener, SmartDarkObserver.Callback {
    private float mCurrentMedianLuma;
    private final Handler mHandler = new Handler();
    private final MiuiLightBarController mLightBarController;
    private final float mLuminanceChangeThreshold;
    private final float mLuminanceThreshold;
    private final PhoneStatusBarView mPhoneStatusBarView;
    private final Rect mSamplingBounds = new Rect();
    private final CompositionSamplingListener mSamplingListener;
    private boolean mSamplingListenerRegistered = false;
    private boolean mSmartDarkEnable;
    private final Rect mTempBounds = new Rect();
    private boolean mUpdateOnNextDraw;
    private final Runnable mUpdateSamplingListener = new Runnable() {
        /* class com.android.systemui.statusbar.phone.$$Lambda$PhoneStatusBarTintController$hVV5RULyY1yrjuLx9olLfRKQ9bM */

        public final void run() {
            PhoneStatusBarTintController.this.updateSamplingListener();
        }
    };
    private boolean mWindowVisible = true;

    public PhoneStatusBarTintController(PhoneStatusBarView phoneStatusBarView, MiuiLightBarController miuiLightBarController) {
        this.mSamplingListener = new CompositionSamplingListener(phoneStatusBarView.getContext().getMainExecutor()) {
            /* class com.android.systemui.statusbar.phone.PhoneStatusBarTintController.AnonymousClass1 */

            public void onSampleCollected(float f) {
                PhoneStatusBarTintController.this.updateTint(f);
            }
        };
        this.mPhoneStatusBarView = phoneStatusBarView;
        phoneStatusBarView.addOnAttachStateChangeListener(this);
        this.mPhoneStatusBarView.addOnLayoutChangeListener(this);
        this.mPhoneStatusBarView.getBoundsOnScreen(this.mSamplingBounds);
        this.mLightBarController = miuiLightBarController;
        Resources resources = this.mPhoneStatusBarView.getResources();
        this.mLuminanceThreshold = resources.getFloat(C0012R$dimen.phone_status_bar_luminance_threshold);
        this.mLuminanceChangeThreshold = resources.getFloat(C0012R$dimen.phone_status_bar_luminance_change_threshold);
        SmartDarkObserver smartDarkObserver = (SmartDarkObserver) Dependency.get(SmartDarkObserver.class);
        smartDarkObserver.addCallback(this);
        boolean isSmartDarkEnable = smartDarkObserver.isSmartDarkEnable();
        this.mSmartDarkEnable = isSmartDarkEnable;
        miuiLightBarController.setSmartDarkEnable(isSmartDarkEnable);
        requestUpdateSamplingListener();
    }

    /* access modifiers changed from: package-private */
    public void onDraw() {
        if (this.mUpdateOnNextDraw) {
            this.mUpdateOnNextDraw = false;
            requestUpdateSamplingListener();
        }
    }

    public void onViewAttachedToWindow(View view) {
        requestUpdateSamplingListener();
    }

    public void onViewDetachedFromWindow(View view) {
        requestUpdateSamplingListener();
    }

    public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
        this.mPhoneStatusBarView.getBoundsOnScreen(this.mTempBounds);
        if (!this.mTempBounds.equals(this.mSamplingBounds)) {
            this.mSamplingBounds.set(this.mTempBounds);
            requestUpdateSamplingListener();
        }
    }

    @Override // com.miui.systemui.statusbar.phone.SmartDarkObserver.Callback
    public void onSmartDarkEnableChanged(boolean z) {
        this.mSmartDarkEnable = z;
        this.mLightBarController.setSmartDarkEnable(z);
        requestUpdateSamplingListener();
    }

    private void requestUpdateSamplingListener() {
        this.mHandler.removeCallbacks(this.mUpdateSamplingListener);
        this.mHandler.post(this.mUpdateSamplingListener);
    }

    /* access modifiers changed from: private */
    public void updateSamplingListener() {
        if (this.mSamplingListenerRegistered) {
            this.mSamplingListenerRegistered = false;
            CompositionSamplingListener.unregister(this.mSamplingListener);
        }
        if (this.mSmartDarkEnable && this.mWindowVisible && !this.mSamplingBounds.isEmpty() && this.mPhoneStatusBarView.isAttachedToWindow()) {
            if (!this.mPhoneStatusBarView.getViewRootImpl().getSurfaceControl().isValid()) {
                this.mUpdateOnNextDraw = true;
                return;
            }
            this.mSamplingListenerRegistered = true;
            this.mCurrentMedianLuma = -1.0f;
            CompositionSamplingListener.register(this.mSamplingListener, 0, this.mPhoneStatusBarView.getViewRootImpl().getSurfaceControl(), this.mSamplingBounds);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateTint(float f) {
        if (Math.abs(this.mCurrentMedianLuma - f) > this.mLuminanceChangeThreshold) {
            this.mCurrentMedianLuma = f;
            this.mLightBarController.setSmartDarkLight(f > this.mLuminanceThreshold);
        }
    }
}

package com.android.keyguard;

import android.content.Context;
import com.android.keyguard.KeyguardCameraView;
import com.android.keyguard.KeyguardHorizontalMoveView;

public class KeyguardHorizontalMoveRightViewContainer extends KeyguardHorizontalMoveView {
    /* access modifiers changed from: private */
    public boolean mCameraViewShowing;
    private boolean mIsOnIconTouchDown;
    private KeyguardCameraView mKeyguardCameraView;
    private KeyguardCameraView.CallBack mKeyguardCameraViewCallBack = new KeyguardCameraView.CallBack() {
        public void onAnimUpdate(float f) {
            KeyguardHorizontalMoveRightViewContainer.this.mCallBack.onAnimUpdate(f);
        }

        public void onCompletedAnimationEnd() {
            KeyguardHorizontalMoveRightViewContainer.this.mCallBack.onCompletedAnimationEnd(true);
            KeyguardHorizontalMoveRightViewContainer.this.mCallBack.updateCanShowGxzw(false);
        }

        public void onCancelAnimationEnd(boolean z) {
            KeyguardHorizontalMoveRightViewContainer.this.mCallBack.onCancelAnimationEnd(true, z);
            KeyguardHorizontalMoveRightViewContainer.this.mCallBack.updateCanShowGxzw(true);
        }

        public void onBackAnimationEnd() {
            KeyguardHorizontalMoveRightViewContainer.this.mCallBack.onBackAnimationEnd(true);
            KeyguardHorizontalMoveRightViewContainer.this.mCallBack.updateCanShowGxzw(true);
        }

        public void onVisibilityChanged(boolean z) {
            boolean unused = KeyguardHorizontalMoveRightViewContainer.this.mCameraViewShowing = z;
            KeyguardHorizontalMoveRightViewContainer.this.mCallBack.getMoveIconLayout(true).setVisibility(KeyguardHorizontalMoveRightViewContainer.this.mCameraViewShowing ? 8 : 0);
        }
    };
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onStartedGoingToSleep(int i) {
            if (KeyguardHorizontalMoveRightViewContainer.this.mCameraViewShowing) {
                KeyguardHorizontalMoveRightViewContainer.this.reset();
            }
        }

        public void onKeyguardVisibilityChanged(boolean z) {
            if (z) {
                KeyguardHorizontalMoveRightViewContainer.this.mCallBack.updateCanShowGxzw(true);
            }
        }

        public void onKeyguardBouncerChanged(boolean z) {
            if (z) {
                KeyguardHorizontalMoveRightViewContainer.this.reset();
            }
        }

        public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
            if (z) {
                KeyguardHorizontalMoveRightViewContainer.this.reset();
            }
        }
    };
    private boolean mTouchDownInitial;

    public KeyguardHorizontalMoveRightViewContainer(Context context, KeyguardHorizontalMoveView.CallBack callBack) {
        super(context, callBack);
        this.mKeyguardCameraView = new KeyguardCameraView(context, this.mKeyguardCameraViewCallBack);
        KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(context);
        this.mKeyguardUpdateMonitor = instance;
        instance.registerCallback(this.mKeyguardUpdateMonitorCallback);
    }

    public void onTouchDown(float f, float f2, boolean z) {
        if (!this.mCallBack.isMoveInCenterScreen() || this.mCallBack.isRightMove()) {
            this.mKeyguardCameraView.reset();
            return;
        }
        this.mIsOnIconTouchDown = z;
        this.mKeyguardCameraView.onTouchDown(f, f2, z);
        if (this.mIsOnIconTouchDown) {
            this.mCallBack.getMoveIconLayout(true).setVisibility(8);
        }
        if (this.mIsOnIconTouchDown) {
            this.mCallBack.updateCanShowGxzw(false);
        }
        this.mTouchDownInitial = true;
    }

    public boolean onTouchMove(float f, float f2) {
        if (!this.mTouchDownInitial) {
            return false;
        }
        this.mKeyguardCameraView.onTouchMove(f, f2);
        if (!this.mIsOnIconTouchDown) {
            return true;
        }
        this.mCallBack.updateCanShowGxzw(false);
        return true;
    }

    public void onTouchUp(float f, float f2) {
        if (this.mTouchDownInitial) {
            this.mTouchDownInitial = false;
            this.mKeyguardCameraView.onTouchUp(f, f2);
            this.mCallBack.updateSwipingInProgress(false);
        }
    }

    public void reset() {
        this.mKeyguardCameraView.reset();
        this.mCallBack.updateCanShowGxzw(true);
        this.mCallBack.getMoveIconLayout(true).setVisibility(0);
    }

    public void setDarkMode(boolean z) {
        this.mKeyguardCameraView.setDarkMode(z);
    }
}

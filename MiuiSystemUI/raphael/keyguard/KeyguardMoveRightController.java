package com.android.keyguard;

import android.content.Context;
import android.util.Log;
import com.android.keyguard.BaseKeyguardMoveController;
import com.android.keyguard.MiuiKeyguardCameraView;
import com.android.systemui.Dependency;
import com.miui.systemui.DebugConfig;

public class KeyguardMoveRightController extends BaseKeyguardMoveController {
    /* access modifiers changed from: private */
    public boolean mCameraViewShowing;
    private boolean mIsOnIconTouchDown;
    private MiuiKeyguardCameraView mKeyguardCameraView;
    private MiuiKeyguardCameraView.CallBack mKeyguardCameraViewCallBack = new MiuiKeyguardCameraView.CallBack() {
        public void onAnimUpdate(float f) {
            KeyguardMoveRightController.this.mCallBack.onAnimUpdate(f);
        }

        public void onCompletedAnimationEnd() {
            KeyguardMoveRightController.this.mCallBack.onCompletedAnimationEnd(true);
            KeyguardMoveRightController.this.mCallBack.updateCanShowGxzw(false);
        }

        public void onCancelAnimationEnd(boolean z) {
            KeyguardMoveRightController.this.mCallBack.onCancelAnimationEnd(true, z);
            KeyguardMoveRightController.this.mCallBack.updateCanShowGxzw(true);
        }

        public void onBackAnimationEnd() {
            KeyguardMoveRightController.this.mCallBack.onBackAnimationEnd(true);
            KeyguardMoveRightController.this.mCallBack.updateCanShowGxzw(true);
        }

        public void onVisibilityChanged(boolean z) {
            boolean unused = KeyguardMoveRightController.this.mCameraViewShowing = z;
            KeyguardMoveRightController.this.mCallBack.getMoveIconLayout(true).setVisibility(KeyguardMoveRightController.this.mCameraViewShowing ? 8 : 0);
        }
    };
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        public void onStartedGoingToSleep(int i) {
            if (KeyguardMoveRightController.this.mCameraViewShowing) {
                KeyguardMoveRightController.this.reset();
            }
        }

        public void onKeyguardVisibilityChanged(boolean z) {
            if (z) {
                KeyguardMoveRightController.this.mCallBack.updateCanShowGxzw(true);
            }
        }

        public void onKeyguardBouncerChanged(boolean z) {
            if (z) {
                KeyguardMoveRightController.this.reset();
            }
        }

        public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
            if (z) {
                KeyguardMoveRightController.this.reset();
            }
        }
    };
    private boolean mTouchDownInitial;

    public KeyguardMoveRightController(Context context, BaseKeyguardMoveController.CallBack callBack) {
        super(callBack);
        this.mKeyguardCameraView = new MiuiKeyguardCameraView(context, this.mKeyguardCameraViewCallBack);
        KeyguardUpdateMonitor keyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        keyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
    }

    public void onTouchDown(float f, float f2, boolean z) {
        if (!this.mCallBack.isMoveInCenterScreen() || this.mCallBack.isRightMove()) {
            this.mKeyguardCameraView.reset();
            return;
        }
        if (DebugConfig.DEBUG_KEYGUARD) {
            Log.d("KeyguardMoveRightController", "onTouchDown mTouchDownInitial = true");
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
            if (this.mIsOnIconTouchDown) {
                this.mCallBack.updateCanShowGxzw(true);
            }
        }
    }

    public void reset() {
        this.mKeyguardCameraView.reset();
        this.mCallBack.updateCanShowGxzw(true);
        this.mCallBack.getMoveIconLayout(true).setVisibility(0);
    }
}

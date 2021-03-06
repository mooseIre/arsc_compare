package com.android.keyguard;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import com.android.keyguard.BaseKeyguardMoveController;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.negative.KeyguardClientCallback;
import com.android.keyguard.negative.LockScreenMagazineClient;
import com.android.keyguard.negative.MiuiQuickConnectController;
import com.android.systemui.Dependency;
import miui.util.Log;

public class KeyguardMoveLeftController extends BaseKeyguardMoveController {
    private MiuiFaceUnlockManager mFaceUnlockManager = ((MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class));
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class com.android.keyguard.KeyguardMoveLeftController.AnonymousClass1 */

        public void handleMessage(Message message) {
            if (message.what == 0) {
                KeyguardMoveLeftController.this.mLockScreenMagazineClient.unBind();
            }
        }
    };
    private float mInitialTouchX;
    private KeyguardClientCallback mKeyguardClientCallback = new KeyguardClientCallback() {
        /* class com.android.keyguard.KeyguardMoveLeftController.AnonymousClass2 */

        @Override // com.android.keyguard.negative.KeyguardClientCallback
        public void onOverlayScrollChanged(float f) {
            KeyguardMoveLeftController.this.mCallBack.updateCanShowGxzw(f == 0.0f);
            KeyguardMoveLeftController.this.mFaceUnlockManager.updateHorizontalMoveLeftProgress(f);
            if (KeyguardMoveLeftController.this.mScrollProgress != f) {
                KeyguardMoveLeftController.this.mScrollProgress = f;
                if (KeyguardMoveLeftController.this.mScrollProgress == 0.0f || KeyguardMoveLeftController.this.mScrollProgress == 1.0f) {
                    Slog.i("KeyguardHorizontalMoveLeftViewContainer", "onOverlayScrollChanged mScrollProgress = " + KeyguardMoveLeftController.this.mScrollProgress);
                }
                KeyguardMoveLeftController keyguardMoveLeftController = KeyguardMoveLeftController.this;
                keyguardMoveLeftController.mCallBack.onAnimUpdate(keyguardMoveLeftController.mScrollProgress * ((float) KeyguardMoveLeftController.this.mScreenPoint.x));
                KeyguardMoveLeftController.this.mKeyguardUpdateMonitor.updateFingerprintListeningState();
                KeyguardMoveLeftController.this.mKeyguardUpdateMonitor.requestFaceAuth();
            }
        }

        @Override // com.android.keyguard.negative.KeyguardClientCallback
        public void onServiceStateChanged(boolean z) {
            ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).setLockScreenLeftOverlayAvailable(z);
        }
    };
    private KeyguardUpdateMonitor mKeyguardUpdateMonitor = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class));
    private MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        /* class com.android.keyguard.KeyguardMoveLeftController.AnonymousClass3 */

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onKeyguardOccludedChanged(boolean z) {
            if (z) {
                KeyguardMoveLeftController.this.mLockScreenMagazineClient.hideOverlay(true);
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onKeyguardBouncerChanged(boolean z) {
            if (z) {
                KeyguardMoveLeftController.this.mLockScreenMagazineClient.hideOverlay(true);
            }
        }

        @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
        public void onKeyguardShowingChanged(boolean z) {
            if (!z) {
                KeyguardMoveLeftController.this.mLockScreenMagazineClient.hideOverlay(true);
                KeyguardMoveLeftController.this.mHandler.removeMessages(0);
                KeyguardMoveLeftController.this.mHandler.sendEmptyMessage(0);
            }
        }
    };
    private LockScreenMagazineClient mLockScreenMagazineClient;
    private volatile float mScrollProgress;
    private boolean mTouchDownInitial;

    public KeyguardMoveLeftController(Context context, BaseKeyguardMoveController.CallBack callBack) {
        super(callBack, context);
        this.mLockScreenMagazineClient = new LockScreenMagazineClient(context, this.mKeyguardClientCallback);
        this.mKeyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
    }

    public void onTouchDown(float f, float f2, boolean z) {
        if (this.mCallBack.isMoveInCenterScreen() && this.mCallBack.isRightMove() && !isLeftViewLaunchActivity()) {
            this.mInitialTouchX = f;
            this.mLockScreenMagazineClient.startMove();
            if (this.mScrollProgress != 0.0f) {
                this.mCallBack.updateCanShowGxzw(false);
            }
            this.mTouchDownInitial = true;
        }
    }

    @Override // com.android.keyguard.BaseKeyguardMoveController
    public boolean onTouchMove(float f, float f2) {
        if (!this.mTouchDownInitial || isLeftViewLaunchActivity()) {
            return false;
        }
        float f3 = (f - this.mInitialTouchX) / ((float) this.mScreenPoint.x);
        LockScreenMagazineClient lockScreenMagazineClient = this.mLockScreenMagazineClient;
        if (!this.mCallBack.isRightMove()) {
            f3 = -f3;
        }
        lockScreenMagazineClient.updateMove(f3);
        if (this.mScrollProgress == 0.0f) {
            return true;
        }
        this.mCallBack.updateCanShowGxzw(false);
        return true;
    }

    @Override // com.android.keyguard.BaseKeyguardMoveController
    public void onTouchUp(float f, float f2) {
        if (this.mTouchDownInitial && !isLeftViewLaunchActivity()) {
            this.mLockScreenMagazineClient.endMove();
            this.mCallBack.updateSwipingInProgress(false);
        }
        super.onTouchUp(f, f2);
    }

    public void reset() {
        this.mLockScreenMagazineClient.unBind();
        this.mCallBack.updateCanShowGxzw(true);
    }

    public void onStartedWakingUp() {
        this.mHandler.removeMessages(0);
        Log.d("KeyguardHorizontalMoveLeftViewContainer", "onStartedWakingUp " + ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeftOverlay() + " " + MiuiGxzwManager.getInstance().isUnlockByGxzw() + " " + ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFingerprintUnlock() + " " + ((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFaceUnlock() + " " + this.mFaceUnlockManager.isStayScreenWhenFaceUnlockSuccess() + " " + this.mCallBack.getMoveIconState(false).isVisible);
        if (((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeftOverlay() && !MiuiGxzwManager.getInstance().isUnlockByGxzw() && !((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFingerprintUnlock()) {
            if ((!((KeyguardUpdateMonitorInjector) Dependency.get(KeyguardUpdateMonitorInjector.class)).isFaceUnlock() || !this.mFaceUnlockManager.isStayScreenWhenFaceUnlockSuccess()) && this.mCallBack.getMoveIconState(false).isVisible) {
                this.mLockScreenMagazineClient.bind();
            }
        }
    }

    public void onFinishedGoingToSleep() {
        this.mLockScreenMagazineClient.hideOverlay(false);
        this.mHandler.removeMessages(0);
        this.mHandler.sendEmptyMessageDelayed(0, 5000);
    }

    public boolean isLeftViewLaunchActivity() {
        return ((MiuiQuickConnectController) Dependency.get(MiuiQuickConnectController.class)).isUseXMYZLLeft() || !((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isLockScreenLeftOverlayAvailable();
    }
}

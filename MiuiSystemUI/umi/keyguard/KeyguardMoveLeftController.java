package com.android.keyguard;

import android.content.Context;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import android.view.Display;
import com.android.keyguard.BaseKeyguardMoveController;
import com.android.keyguard.faceunlock.MiuiFaceUnlockManager;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.injector.KeyguardUpdateMonitorInjector;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.negative.KeyguardClientCallback;
import com.android.keyguard.negative.LockScreenMagazineClient;
import com.android.systemui.Dependency;
import miui.util.Log;

public class KeyguardMoveLeftController extends BaseKeyguardMoveController {
    private Context mContext;
    /* access modifiers changed from: private */
    public MiuiFaceUnlockManager mFaceUnlockManager;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            if (message.what == 0) {
                KeyguardMoveLeftController.this.mLockScreenMagazineClient.unBind();
            }
        }
    };
    private float mInitialTouchX;
    private KeyguardClientCallback mKeyguardClientCallback = new KeyguardClientCallback() {
        public void onOverlayScrollChanged(float f) {
            KeyguardMoveLeftController.this.mCallBack.updateCanShowGxzw(f == 0.0f);
            KeyguardMoveLeftController.this.mFaceUnlockManager.updateHorizontalMoveLeftProgress(f);
            if (KeyguardMoveLeftController.this.mScrollProgress != f) {
                float unused = KeyguardMoveLeftController.this.mScrollProgress = f;
                if (KeyguardMoveLeftController.this.mScrollProgress == 0.0f || KeyguardMoveLeftController.this.mScrollProgress == 1.0f) {
                    Slog.i("KeyguardHorizontalMoveLeftViewContainer", "onOverlayScrollChanged mScrollProgress = " + KeyguardMoveLeftController.this.mScrollProgress);
                }
                KeyguardMoveLeftController keyguardMoveLeftController = KeyguardMoveLeftController.this;
                keyguardMoveLeftController.mCallBack.onAnimUpdate(keyguardMoveLeftController.mScrollProgress * KeyguardMoveLeftController.this.mScreenWidth);
                if (KeyguardMoveLeftController.this.mScrollProgress == 0.0f && !KeyguardMoveLeftController.this.mKeyguardUpdateMonitor.isFaceDetectionRunning()) {
                    KeyguardMoveLeftController.this.mKeyguardUpdateMonitor.requestFaceAuth();
                } else if (KeyguardMoveLeftController.this.mKeyguardUpdateMonitor.isFaceDetectionRunning()) {
                    KeyguardMoveLeftController.this.mKeyguardUpdateMonitor.cancelFaceAuth();
                }
            }
        }

        public void onServiceStateChanged(boolean z) {
            ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).setLockScreenLeftOverlayAvailable(z);
        }
    };
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new MiuiKeyguardUpdateMonitorCallback() {
        public void onKeyguardOccludedChanged(boolean z) {
            if (z) {
                KeyguardMoveLeftController.this.mLockScreenMagazineClient.hideOverlay(true);
            }
        }

        public void onKeyguardBouncerChanged(boolean z) {
            if (z) {
                KeyguardMoveLeftController.this.mLockScreenMagazineClient.hideOverlay(true);
            }
        }

        public void onKeyguardShowingChanged(boolean z) {
            if (!z) {
                KeyguardMoveLeftController.this.mLockScreenMagazineClient.hideOverlay(true);
                KeyguardMoveLeftController.this.mHandler.removeMessages(0);
                KeyguardMoveLeftController.this.mHandler.sendEmptyMessage(0);
            }
        }
    };
    /* access modifiers changed from: private */
    public LockScreenMagazineClient mLockScreenMagazineClient;
    /* access modifiers changed from: private */
    public float mScreenWidth;
    /* access modifiers changed from: private */
    public volatile float mScrollProgress;
    private boolean mTouchDownInitial;

    public KeyguardMoveLeftController(Context context, BaseKeyguardMoveController.CallBack callBack) {
        super(callBack);
        this.mContext = context;
        this.mKeyguardUpdateMonitor = (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class);
        this.mFaceUnlockManager = (MiuiFaceUnlockManager) Dependency.get(MiuiFaceUnlockManager.class);
        this.mLockScreenMagazineClient = new LockScreenMagazineClient(context, this.mKeyguardClientCallback);
        this.mKeyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        initScreenSize(this.mContext);
    }

    public void onTouchDown(float f, float f2, boolean z) {
        if (this.mCallBack.isMoveInCenterScreen() && this.mCallBack.isRightMove() && ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isLockScreenLeftOverlayAvailable()) {
            this.mInitialTouchX = f;
            this.mLockScreenMagazineClient.startMove();
            if (this.mScrollProgress != 0.0f) {
                this.mCallBack.updateCanShowGxzw(false);
            }
            this.mTouchDownInitial = true;
        }
    }

    public boolean onTouchMove(float f, float f2) {
        if (!this.mTouchDownInitial) {
            return false;
        }
        float f3 = (f - this.mInitialTouchX) / this.mScreenWidth;
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

    public void onTouchUp(float f, float f2) {
        if (this.mTouchDownInitial && ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isLockScreenLeftOverlayAvailable()) {
            this.mTouchDownInitial = false;
            this.mLockScreenMagazineClient.endMove();
            this.mCallBack.updateSwipingInProgress(false);
        }
    }

    public void reset() {
        this.mLockScreenMagazineClient.unBind();
        this.mCallBack.updateCanShowGxzw(true);
    }

    private void initScreenSize(Context context) {
        Display display = ((DisplayManager) context.getSystemService("display")).getDisplay(0);
        Point point = new Point();
        display.getRealSize(point);
        this.mScreenWidth = (float) point.x;
    }

    public void onStartedWakingUp() {
        Class cls = LockScreenMagazineController.class;
        Class cls2 = KeyguardUpdateMonitorInjector.class;
        this.mHandler.removeMessages(0);
        Log.d("KeyguardHorizontalMoveLeftViewContainer", "onStartedWakingUp " + ((LockScreenMagazineController) Dependency.get(cls)).isSupportLockScreenMagazineLeftOverlay() + " " + MiuiGxzwManager.getInstance().isUnlockByGxzw() + " " + ((KeyguardUpdateMonitorInjector) Dependency.get(cls2)).isFingerprintUnlock() + " " + ((KeyguardUpdateMonitorInjector) Dependency.get(cls2)).isFaceUnlock() + " " + this.mFaceUnlockManager.isStayScreenWhenFaceUnlockSuccess() + " " + this.mCallBack.getMoveIconState(false).isVisible);
        if (((LockScreenMagazineController) Dependency.get(cls)).isSupportLockScreenMagazineLeftOverlay() && !MiuiGxzwManager.getInstance().isUnlockByGxzw() && !((KeyguardUpdateMonitorInjector) Dependency.get(cls2)).isFingerprintUnlock()) {
            if ((!((KeyguardUpdateMonitorInjector) Dependency.get(cls2)).isFaceUnlock() || !this.mFaceUnlockManager.isStayScreenWhenFaceUnlockSuccess()) && this.mCallBack.getMoveIconState(false).isVisible) {
                this.mLockScreenMagazineClient.bind();
            }
        }
    }

    public void onFinishedGoingToSleep() {
        this.mLockScreenMagazineClient.hideOverlay(false);
        this.mHandler.removeMessages(0);
        this.mHandler.sendEmptyMessageDelayed(0, 5000);
    }
}

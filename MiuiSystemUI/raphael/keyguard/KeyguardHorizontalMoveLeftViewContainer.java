package com.android.keyguard;

import android.content.Context;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import android.view.Display;
import com.android.keyguard.KeyguardHorizontalMoveView;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.negative.KeyguardClientCallback;
import com.android.keyguard.negative.LockScreenMagazineClient;
import miui.util.Log;

public class KeyguardHorizontalMoveLeftViewContainer extends KeyguardHorizontalMoveView {
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public FaceUnlockManager mFaceUnlockManager;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            if (message.what == 0) {
                KeyguardHorizontalMoveLeftViewContainer.this.mLockScreenMagazineClient.unBind();
            }
        }
    };
    private float mInitialTouchX;
    private KeyguardClientCallback mKeyguardClientCallback = new KeyguardClientCallback() {
        public void onOverlayScrollChanged(float f) {
            KeyguardHorizontalMoveLeftViewContainer.this.mCallBack.updateCanShowGxzw(f == 0.0f);
            KeyguardHorizontalMoveLeftViewContainer.this.mFaceUnlockManager.updateHorizontalMoveLeftProgress(f);
            if (KeyguardHorizontalMoveLeftViewContainer.this.mScrollProgress != f) {
                float unused = KeyguardHorizontalMoveLeftViewContainer.this.mScrollProgress = f;
                if (KeyguardHorizontalMoveLeftViewContainer.this.mScrollProgress == 0.0f || KeyguardHorizontalMoveLeftViewContainer.this.mScrollProgress == 1.0f) {
                    Slog.i("KeyguardHorizontalMoveLeftViewContainer", "onOverlayScrollChanged mScrollProgress = " + KeyguardHorizontalMoveLeftViewContainer.this.mScrollProgress);
                }
                KeyguardHorizontalMoveLeftViewContainer keyguardHorizontalMoveLeftViewContainer = KeyguardHorizontalMoveLeftViewContainer.this;
                keyguardHorizontalMoveLeftViewContainer.mCallBack.onAnimUpdate(keyguardHorizontalMoveLeftViewContainer.mScrollProgress * KeyguardHorizontalMoveLeftViewContainer.this.mScreenWidth);
                if (KeyguardHorizontalMoveLeftViewContainer.this.mScrollProgress == 0.0f && !KeyguardHorizontalMoveLeftViewContainer.this.mFaceUnlockManager.isFaceUnlockStarted()) {
                    KeyguardHorizontalMoveLeftViewContainer.this.mFaceUnlockManager.startFaceUnlock();
                } else if (KeyguardHorizontalMoveLeftViewContainer.this.mFaceUnlockManager.isFaceUnlockStarted()) {
                    KeyguardHorizontalMoveLeftViewContainer.this.mFaceUnlockManager.stopFaceUnlock();
                }
            }
        }

        public void onServiceStateChanged(boolean z) {
            KeyguardUpdateMonitor.getInstance(KeyguardHorizontalMoveLeftViewContainer.this.mContext).setLockScreenLeftOverlayAvailable(z);
        }
    };
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onStartedWakingUp() {
            KeyguardHorizontalMoveLeftViewContainer.this.mHandler.removeMessages(0);
            Log.d("KeyguardHorizontalMoveLeftViewContainer", "onStartedWakingUp " + KeyguardHorizontalMoveLeftViewContainer.this.mKeyguardUpdateMonitor.isSupportLockScreenMagazineLeftOverlay() + " " + MiuiGxzwManager.getInstance().isUnlockByGxzw() + " " + KeyguardHorizontalMoveLeftViewContainer.this.mKeyguardUpdateMonitor.isFingerprintUnlock() + " " + KeyguardHorizontalMoveLeftViewContainer.this.mKeyguardUpdateMonitor.isFaceUnlock() + " " + KeyguardHorizontalMoveLeftViewContainer.this.mFaceUnlockManager.isStayScreenWhenFaceUnlockSuccess() + " " + KeyguardHorizontalMoveLeftViewContainer.this.mCallBack.getMoveIconState(false).isVisible);
            if (KeyguardHorizontalMoveLeftViewContainer.this.mKeyguardUpdateMonitor.isSupportLockScreenMagazineLeftOverlay() && !MiuiGxzwManager.getInstance().isUnlockByGxzw() && !KeyguardHorizontalMoveLeftViewContainer.this.mKeyguardUpdateMonitor.isFingerprintUnlock()) {
                if ((!KeyguardHorizontalMoveLeftViewContainer.this.mKeyguardUpdateMonitor.isFaceUnlock() || !KeyguardHorizontalMoveLeftViewContainer.this.mFaceUnlockManager.isStayScreenWhenFaceUnlockSuccess()) && KeyguardHorizontalMoveLeftViewContainer.this.mCallBack.getMoveIconState(false).isVisible) {
                    KeyguardHorizontalMoveLeftViewContainer.this.mLockScreenMagazineClient.bind();
                }
            }
        }

        public void onFinishedGoingToSleep(int i) {
            KeyguardHorizontalMoveLeftViewContainer.this.mLockScreenMagazineClient.hideOverlay(false);
            KeyguardHorizontalMoveLeftViewContainer.this.mHandler.removeMessages(0);
            KeyguardHorizontalMoveLeftViewContainer.this.mHandler.sendEmptyMessageDelayed(0, 5000);
        }

        public void onKeyguardBouncerChanged(boolean z) {
            if (z) {
                KeyguardHorizontalMoveLeftViewContainer.this.mLockScreenMagazineClient.hideOverlay(true);
            }
        }

        public void onKeyguardOccludedChanged(boolean z) {
            if (z) {
                KeyguardHorizontalMoveLeftViewContainer.this.mLockScreenMagazineClient.hideOverlay(true);
            }
        }

        public void onKeyguardShowingChanged(boolean z) {
            if (!z) {
                KeyguardHorizontalMoveLeftViewContainer.this.mLockScreenMagazineClient.hideOverlay(true);
                KeyguardHorizontalMoveLeftViewContainer.this.mHandler.removeMessages(0);
                KeyguardHorizontalMoveLeftViewContainer.this.mHandler.sendEmptyMessageDelayed(0, 5000);
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

    public KeyguardHorizontalMoveLeftViewContainer(Context context, KeyguardHorizontalMoveView.CallBack callBack) {
        super(context, callBack);
        this.mContext = context;
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mFaceUnlockManager = FaceUnlockManager.getInstance();
        this.mLockScreenMagazineClient = new LockScreenMagazineClient(context, this.mKeyguardClientCallback);
        this.mKeyguardUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        initScreenSize(this.mContext);
    }

    public void onTouchDown(float f, float f2, boolean z) {
        if (this.mCallBack.isMoveInCenterScreen() && this.mCallBack.isRightMove() && this.mKeyguardUpdateMonitor.isLockScreenLeftOverlayAvailable()) {
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
        if (this.mTouchDownInitial && this.mKeyguardUpdateMonitor.isLockScreenLeftOverlayAvailable()) {
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
}

package com.android.keyguard.faceunlock;

import android.content.Context;
import android.hardware.miuiface.IMiuiFaceManager;
import android.hardware.miuiface.MiuiFaceFactory;
import android.hardware.miuiface.Miuiface;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.util.Slog;
import com.android.keyguard.BoostFrameworkHelper;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.xiaomi.stat.c.b;

public class FaceUnlockManagerSupport extends BaseFaceUnlockManager {
    /* access modifiers changed from: private */
    public CancellationSignal mAuhtenCancelSignal;
    IMiuiFaceManager.AuthenticationCallback mAuthenCallback = new IMiuiFaceManager.AuthenticationCallback() {
        public void onAuthenticationSucceeded(Miuiface miuiface) {
            FaceUnlockManagerSupport.super.onAuthenticationSucceeded(miuiface);
            Slog.i("miui_face", " authenCallback, onAuthenticationSucceeded time=" + (System.currentTimeMillis() - FaceUnlockManagerSupport.this.mStageInFaceUnlockTime));
            FaceUnlockManagerSupport.this.mStageInFaceUnlockTime = System.currentTimeMillis();
            FaceUnlockManagerSupport faceUnlockManagerSupport = FaceUnlockManagerSupport.this;
            faceUnlockManagerSupport.mFaceAuthSucceed = true;
            faceUnlockManagerSupport.mMainHandler.sendEmptyMessage(b.c);
            FaceUnlockManagerSupport.this.stopFaceUnlock();
        }

        public void onAuthenticationHelp(int i, CharSequence charSequence) {
            FaceUnlockManagerSupport.super.onAuthenticationHelp(i, charSequence);
            Slog.i("miui_face", " authenCallback, onAuthenticationHelp helpCode:" + i + " helpString:" + charSequence);
            FaceUnlockManagerSupport faceUnlockManagerSupport = FaceUnlockManagerSupport.this;
            faceUnlockManagerSupport.mHelpCode = i;
            faceUnlockManagerSupport.mMainHandler.sendEmptyMessage(b.b);
            if (!MiuiFaceUnlockUtils.isStLightFaceUnlockType(FaceUnlockManagerSupport.this.mContext)) {
                if (i != 5) {
                    FaceUnlockManagerSupport.this.mHasFace = true;
                }
                if (i == 14) {
                    FaceUnlockManagerSupport faceUnlockManagerSupport2 = FaceUnlockManagerSupport.this;
                    int i2 = faceUnlockManagerSupport2.mLiveAttackValue + 1;
                    faceUnlockManagerSupport2.mLiveAttackValue = i2;
                    if (i2 >= 3) {
                        faceUnlockManagerSupport2.mLiveAttack = true;
                    }
                } else {
                    FaceUnlockManagerSupport.this.mLiveAttackValue = 0;
                }
                if (MiuiFaceUnlockUtils.isScreenTurnOnDelayed()) {
                    FaceUnlockManagerSupport faceUnlockManagerSupport3 = FaceUnlockManagerSupport.this;
                    int i3 = faceUnlockManagerSupport3.mNoFaceDetectedValue + 1;
                    faceUnlockManagerSupport3.mNoFaceDetectedValue = i3;
                    if (i3 >= 3) {
                        faceUnlockManagerSupport3.mFaceUnlockCallback.unblockScreenOn();
                        MiuiFaceUnlockUtils.setScreenTurnOnDelayed(false);
                        return;
                    }
                    return;
                }
                return;
            }
            if (i == 10) {
                FaceUnlockManagerSupport.this.mHasFace = true;
            }
            if (i == 63) {
                FaceUnlockManagerSupport.this.mLiveAttack = true;
            }
        }

        public void onAuthenticationFailed() {
            Slog.i("miui_face", "authenCallback, onAuthenticationFailed");
            FaceUnlockManagerSupport faceUnlockManagerSupport = FaceUnlockManagerSupport.this;
            faceUnlockManagerSupport.mFaceAuthTimeOut = true;
            faceUnlockManagerSupport.stopFaceUnlock();
        }

        public void onAuthenticationError(int i, CharSequence charSequence) {
            Slog.i("miui_face", "authenCallback, onAuthenticationError code:" + i + " msg:" + charSequence);
            CancellationSignal unused = FaceUnlockManagerSupport.this.mAuhtenCancelSignal = null;
            if (!FaceUnlockManagerSupport.this.mStartFaceUnlockSuccess || i == 2002) {
                FaceUnlockManagerSupport.this.mMainHandler.sendEmptyMessage(b.g);
            }
        }
    };
    protected Context mContext;
    protected boolean mFaceAuthSucceed;
    protected boolean mFaceAuthTimeOut;
    private IMiuiFaceManager mFaceManager;
    /* access modifiers changed from: private */
    public FaceRemoveCallback mFaceRemoveCallback;
    /* access modifiers changed from: private */
    public FaceUnlockCallback mFaceUnlockCallback;
    protected int mFaceUnlockType;
    protected int mFailedCount;
    protected int mFailedLiveCount;
    protected HandlerThread mHandlerThread = new HandlerThread("face_unlock");
    protected boolean mHasFace;
    protected int mHelpCode;
    protected boolean mLiveAttack;
    protected int mLiveAttackValue;
    protected Handler mMainHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            switch (message.what) {
                case b.a /*1001*/:
                    FaceUnlockManagerSupport.this.mFaceUnlockCallback.onFaceAuthStart();
                    return;
                case b.b /*1002*/:
                    FaceUnlockManagerSupport.this.mFaceUnlockCallback.onFaceAuthHelp(FaceUnlockManagerSupport.this.mHelpCode);
                    return;
                case b.c /*1003*/:
                    FaceUnlockManagerSupport.this.mFaceUnlockCallback.onFaceAuthenticated();
                    return;
                case b.d /*1004*/:
                    FaceUnlockManagerSupport.this.mFaceUnlockCallback.onFaceAuthFailed();
                    return;
                case b.e /*1005*/:
                    FaceUnlockManagerSupport.this.mFaceUnlockCallback.onFaceAuthTimeOut(FaceUnlockManagerSupport.this.mHasFace);
                    return;
                case b.f /*1006*/:
                    FaceUnlockManagerSupport.this.mFaceUnlockCallback.onFaceAuthLocked();
                    return;
                case b.g /*1007*/:
                    FaceUnlockManagerSupport.this.mFaceUnlockCallback.restartFaceUnlock();
                    return;
                default:
                    return;
            }
        }
    };
    protected int mNoFaceDetectedValue = 0;
    IMiuiFaceManager.RemovalCallback mRemovalCallback = new IMiuiFaceManager.RemovalCallback() {
        public void onRemovalError(Miuiface miuiface, int i, CharSequence charSequence) {
            Slog.i("miui_face", "mRemovalCallback, onRemovalError code:" + i + " msg:" + charSequence + ";id=" + miuiface.getMiuifaceId());
            if (FaceUnlockManagerSupport.this.mFaceRemoveCallback != null) {
                FaceUnlockManagerSupport.this.mFaceRemoveCallback.onFailed();
            }
        }

        public void onRemovalSucceeded(Miuiface miuiface, int i) {
            Slog.i("miui_face", "mRemovalCallback, onRemovalSucceeded id=" + miuiface.getMiuifaceId() + ";remaining=" + i);
            if (FaceUnlockManagerSupport.this.mFaceRemoveCallback != null) {
                FaceUnlockManagerSupport.this.mFaceRemoveCallback.onRemoved();
            }
            MiuiFaceUnlockUtils.resetFaceUnlockSettingValues(FaceUnlockManagerSupport.this.mContext);
        }
    };
    protected long mStageInFaceUnlockTime;
    protected boolean mStartFaceUnlockSuccess;
    protected Handler mWorkerHandler;

    public FaceUnlockManagerSupport(Context context, int i) {
        this.mContext = context;
        this.mFaceManager = MiuiFaceFactory.getFaceManager(context, i);
        this.mHandlerThread.start();
        this.mWorkerHandler = new Handler(this.mHandlerThread.getLooper());
        BoostFrameworkHelper.initBoostFramework();
    }

    public void runOnFaceUnlockWorkerThread(Runnable runnable) {
        HandlerThread handlerThread = this.mHandlerThread;
        if (handlerThread != null && this.mWorkerHandler != null) {
            if (handlerThread.getThreadId() == Process.myTid()) {
                runnable.run();
            } else {
                this.mWorkerHandler.post(runnable);
            }
        }
    }

    public void initAll() {
        IMiuiFaceManager faceManager = MiuiFaceFactory.getFaceManager(this.mContext, this.mFaceUnlockType);
        this.mFaceManager = faceManager;
        faceManager.preInitAuthen();
    }

    public boolean isFaceUnlockInited() {
        IMiuiFaceManager faceManager = MiuiFaceFactory.getFaceManager(this.mContext, this.mFaceUnlockType);
        this.mFaceManager = faceManager;
        return faceManager.isFaceUnlockInited();
    }

    public void startFaceUnlock(FaceUnlockCallback faceUnlockCallback) {
        if (this.mAuhtenCancelSignal != null) {
            Slog.d("miui_face", "start face unlock is runing");
            this.mStartFaceUnlockSuccess = false;
            return;
        }
        this.mFaceUnlockCallback = faceUnlockCallback;
        this.mLiveAttackValue = 0;
        this.mNoFaceDetectedValue = 0;
        this.mHasFace = false;
        this.mLiveAttack = false;
        this.mFaceAuthSucceed = false;
        this.mFaceAuthTimeOut = false;
        this.mStartFaceUnlockSuccess = true;
        this.mFaceManager = MiuiFaceFactory.getFaceManager(this.mContext, this.mFaceUnlockType);
        Slog.i("miui_face", "start verify time=" + (System.currentTimeMillis() - KeyguardUpdateMonitor.sScreenTurnedOnTime));
        CancellationSignal cancellationSignal = new CancellationSignal();
        this.mAuhtenCancelSignal = cancellationSignal;
        this.mFaceManager.authenticate(cancellationSignal, 0, this.mAuthenCallback, this.mWorkerHandler, 5000);
        this.mStageInFaceUnlockTime = System.currentTimeMillis();
        BoostFrameworkHelper.setBoost(3);
        this.mMainHandler.sendEmptyMessage(b.a);
    }

    public void stopFaceUnlock() {
        if (this.mAuhtenCancelSignal != null) {
            Log.i("miui_face", "stopFaceUnlock mHasFace=" + this.mHasFace + ";mLiveAttack=" + this.mLiveAttack);
            if (!this.mAuhtenCancelSignal.isCanceled()) {
                this.mAuhtenCancelSignal.cancel();
            }
            this.mAuhtenCancelSignal = null;
            if (!this.mFaceAuthSucceed && this.mHasFace) {
                this.mFailedCount++;
            }
            if (this.mLiveAttack) {
                this.mFailedLiveCount++;
            }
            if (this.mFaceAuthTimeOut) {
                this.mMainHandler.sendEmptyMessage(b.e);
            }
            if (isFaceUnlockLocked()) {
                this.mMainHandler.sendEmptyMessage(b.f);
            }
            if (!this.mFaceAuthSucceed && !this.mFaceAuthTimeOut && !isFaceUnlockLocked()) {
                this.mMainHandler.sendEmptyMessage(b.d);
            }
        }
    }

    public void deleteFeature(String str, FaceRemoveCallback faceRemoveCallback) {
        FaceRemoveCallback faceRemoveCallback2;
        Slog.i("miui_face", "deleteFeature faceId=" + str);
        this.mFaceRemoveCallback = faceRemoveCallback;
        this.mFaceManager = MiuiFaceFactory.getFaceManager(this.mContext, this.mFaceUnlockType);
        this.mFaceManager.remove(new Miuiface((CharSequence) null, 0, Integer.parseInt(str), 0), this.mRemovalCallback);
        if (MiuiFaceUnlockUtils.isStLightFaceUnlockType(this.mContext) && (faceRemoveCallback2 = this.mFaceRemoveCallback) != null) {
            faceRemoveCallback2.onRemoved();
        }
    }

    public void resetFailCount() {
        this.mFailedCount = 0;
        this.mFailedLiveCount = 0;
    }

    /* access modifiers changed from: protected */
    public boolean isFaceUnlockLocked() {
        return this.mFailedCount >= 5 || this.mFailedLiveCount >= 3;
    }

    public void updateFaceUnlockType(int i) {
        this.mFaceUnlockType = i;
    }
}

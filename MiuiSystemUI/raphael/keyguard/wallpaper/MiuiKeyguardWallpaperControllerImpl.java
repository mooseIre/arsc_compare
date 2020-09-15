package com.android.keyguard.wallpaper;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.Property;
import android.view.View;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.utils.DeviceLevelUtils;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.policy.BatteryController;
import com.miui.systemui.annotation.Inject;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MiuiKeyguardWallpaperControllerImpl implements MiuiKeyguardWallpaperController, KeyguardUpdateMonitor.WallpaperChangeCallback, BatteryController.BatteryStateChangeCallback, Dumpable {
    private static final boolean DEBUG = Constants.DEBUG;
    private ValueAnimator mAlphaAnimator;
    private final List<MiuiKeyguardWallpaperController.KeyguardWallpaperCallback> mCallbacks = new ArrayList();
    private final Context mContext;
    /* access modifiers changed from: private */
    public boolean mFingerprintAuthenticated;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    /* access modifiers changed from: private */
    public boolean mIsInteractive;
    private boolean mIsPowerSave;
    private KeyguardUpdateMonitorCallback mKeyguardCallback = new KeyguardUpdateMonitorCallback() {
        public void onUserSwitchComplete(int i) {
            super.onUserSwitchComplete(i);
            Log.i("MiuiKeyguardWallpaper", "sys wallpaper changed due to user switched to " + i);
            MiuiKeyguardWallpaperControllerImpl.this.updateWallpaper(true);
        }

        public void onFingerprintAuthenticated(int i) {
            boolean unused = MiuiKeyguardWallpaperControllerImpl.this.mFingerprintAuthenticated = true;
        }

        public void onStartedWakingUp() {
            super.onStartedWakingUp();
            boolean z = true;
            if (!MiuiKeyguardWallpaperControllerImpl.this.mIsInteractive) {
                boolean unused = MiuiKeyguardWallpaperControllerImpl.this.mIsInteractive = true;
            }
            if (!MiuiKeyguardWallpaperControllerImpl.this.mScreenOnNotified) {
                MiuiKeyguardWallpaperControllerImpl.this.dispatchScreenTurnedOn();
            }
            if (MiuiKeyguardWallpaperControllerImpl.this.mKeyguardOccluded || MiuiKeyguardWallpaperControllerImpl.this.mFingerprintAuthenticated || DeviceLevelUtils.isLowEndDevice()) {
                z = false;
            }
            MiuiKeyguardWallpaperControllerImpl.this.animateWallpaperScrim(z);
        }

        public void onStartedGoingToSleep(int i) {
            boolean unused = MiuiKeyguardWallpaperControllerImpl.this.mIsInteractive = false;
            boolean unused2 = MiuiKeyguardWallpaperControllerImpl.this.mScreenOnNotified = false;
            super.onStartedGoingToSleep(i);
            String unused3 = MiuiKeyguardWallpaperControllerImpl.this.mWakingUpReason = null;
            boolean unused4 = MiuiKeyguardWallpaperControllerImpl.this.mFingerprintAuthenticated = false;
            MiuiKeyguardWallpaperControllerImpl miuiKeyguardWallpaperControllerImpl = MiuiKeyguardWallpaperControllerImpl.this;
            miuiKeyguardWallpaperControllerImpl.animateScrim(miuiKeyguardWallpaperControllerImpl.mKeyguardVisible, !MiuiKeyguardWallpaperControllerImpl.this.mSupportsAmbientMode);
        }

        public void onFinishedGoingToSleep(int i) {
            super.onFinishedGoingToSleep(i);
            if (!MiuiKeyguardWallpaperControllerImpl.this.mIsInteractive) {
                MiuiKeyguardWallpaperControllerImpl.this.onKeyguardDisappear();
            }
        }

        public void onKeyguardVisibilityChangedRaw(boolean z) {
            super.onKeyguardVisibilityChangedRaw(z);
            if (MiuiKeyguardWallpaperControllerImpl.this.mKeyguardVisible != z) {
                boolean unused = MiuiKeyguardWallpaperControllerImpl.this.mKeyguardVisible = z;
                if (z) {
                    MiuiKeyguardWallpaperControllerImpl.this.onKeyguardShowing();
                    return;
                }
                MiuiKeyguardWallpaperControllerImpl.this.cancelScrimAnimation();
                MiuiKeyguardWallpaperControllerImpl.this.onKeyguardDisappear();
            }
        }

        public void onKeyguardShowingChanged(boolean z) {
            if (MiuiKeyguardWallpaperControllerImpl.this.mKeyguardShowing != z) {
                boolean unused = MiuiKeyguardWallpaperControllerImpl.this.mKeyguardShowing = z;
            }
        }

        public void onKeyguardOccludedChanged(boolean z) {
            boolean unused = MiuiKeyguardWallpaperControllerImpl.this.mKeyguardOccluded = z;
        }
    };
    /* access modifiers changed from: private */
    public boolean mKeyguardOccluded;
    private float mKeyguardRatio;
    private ValueAnimator mKeyguardRatioAnimator;
    /* access modifiers changed from: private */
    public boolean mKeyguardShowing;
    /* access modifiers changed from: private */
    public boolean mKeyguardVisible;
    private final MiuiKeyguardWallpaperWindow mKeyguardWallpaper;
    private MiuiKeyguardWallpaperController.KeyguardWallpaperType mKeyguardWallpaperType = MiuiKeyguardWallpaperController.KeyguardWallpaperType.LIVE_SYSTEM;
    private final ConcurrentHashMap<String, Float> mRequestedBlurs = new ConcurrentHashMap<>();
    /* access modifiers changed from: private */
    public boolean mScreenOnNotified;
    /* access modifiers changed from: private */
    public boolean mSupportsAmbientMode;
    /* access modifiers changed from: private */
    public String mWakingUpReason;
    private float mWallpaperBlurRatio;
    /* access modifiers changed from: private */
    public View mWallpaperScrim;

    public MiuiKeyguardWallpaperControllerImpl(@Inject Context context) {
        this.mContext = context;
        this.mKeyguardWallpaper = new MiuiKeyguardWallpaperWindow(context);
        KeyguardUpdateMonitor.getInstance(context).registerWallpaperChangeCallback(this);
        KeyguardUpdateMonitor.getInstance(context).registerCallback(this.mKeyguardCallback);
        ((BatteryController) Dependency.get(BatteryController.class)).addCallback(this);
    }

    public void preWakeUpWithReason(String str) {
        this.mWakingUpReason = str;
        if (!this.mIsInteractive) {
            this.mIsInteractive = true;
            this.mHandler.post(new Runnable() {
                public final void run() {
                    MiuiKeyguardWallpaperControllerImpl.this.dispatchScreenTurnedOn();
                }
            });
        }
        synchronized (this.mCallbacks) {
            for (MiuiKeyguardWallpaperController.KeyguardWallpaperCallback onPreWakeUpWithReason : this.mCallbacks) {
                onPreWakeUpWithReason.onPreWakeUpWithReason(str);
            }
        }
    }

    public MiuiKeyguardWallpaperController.KeyguardWallpaperType getKeyguardWallpaperType() {
        return this.mKeyguardWallpaperType;
    }

    public void onWallpaperChange(boolean z) {
        if (z) {
            updateWallpaper(true);
        }
    }

    public void setWallpaperScrim(View view) {
        this.mWallpaperScrim = view;
    }

    /* access modifiers changed from: private */
    public void animateWallpaperScrim(boolean z) {
        dispatchWallpaperAnimationUpdated(z);
        animateScrim(z, false);
    }

    /* access modifiers changed from: private */
    public void animateScrim(boolean z, boolean z2) {
        if (this.mWallpaperScrim != null) {
            ValueAnimator valueAnimator = this.mAlphaAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            View view = this.mWallpaperScrim;
            Property property = View.ALPHA;
            float[] fArr = new float[2];
            fArr[0] = view.getAlpha();
            fArr[1] = z2 ? 1.0f : 0.0f;
            this.mAlphaAnimator = ObjectAnimator.ofFloat(view, property, fArr);
            this.mAlphaAnimator.addListener(new AnimatorListenerAdapter() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                    MiuiKeyguardWallpaperControllerImpl.this.mWallpaperScrim.setVisibility(0);
                }

                public void onAnimationEnd(Animator animator) {
                    MiuiKeyguardWallpaperControllerImpl.this.mWallpaperScrim.setVisibility(8);
                }
            });
            this.mAlphaAnimator.setInterpolator(Interpolators.CUBIC_EASE_OUT);
            this.mAlphaAnimator.setDuration(z ? 300 : 0);
            this.mAlphaAnimator.start();
        }
    }

    /* access modifiers changed from: private */
    public void cancelScrimAnimation() {
        dispatchWallpaperAnimationUpdated(false);
        ValueAnimator valueAnimator = this.mAlphaAnimator;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mAlphaAnimator.cancel();
        }
        View view = this.mWallpaperScrim;
        if (view != null) {
            view.setAlpha(0.0f);
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v2, resolved type: java.io.File} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0041  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateWallpaper(boolean r7) {
        /*
            r6 = this;
            com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController$KeyguardWallpaperType r0 = com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController.KeyguardWallpaperType.PICTORIAL
            boolean r1 = com.android.keyguard.MiuiKeyguardUtils.isDefaultLockScreenTheme()
            r2 = 0
            if (r1 != 0) goto L_0x000d
            com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController$KeyguardWallpaperType r0 = com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController.KeyguardWallpaperType.AWESOME_LOCK
        L_0x000b:
            r1 = r2
            goto L_0x003d
        L_0x000d:
            android.content.Context r1 = r6.mContext
            boolean r1 = com.android.keyguard.wallpaper.WallpaperAuthorityUtils.isThemeLockLiveWallpaper(r1)
            if (r1 == 0) goto L_0x0018
            com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController$KeyguardWallpaperType r0 = com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController.KeyguardWallpaperType.LIVE_SYSTEM
            goto L_0x000b
        L_0x0018:
            android.content.Context r1 = r6.mContext
            android.util.Pair r1 = com.android.keyguard.wallpaper.KeyguardWallpaperUtils.getLockWallpaper(r1)
            if (r1 == 0) goto L_0x000b
            java.lang.Object r0 = r1.first
            r2 = r0
            java.io.File r2 = (java.io.File) r2
            java.lang.Object r0 = r1.second
            android.graphics.drawable.Drawable r0 = (android.graphics.drawable.Drawable) r0
            java.lang.String r1 = r2.getPath()
            java.lang.String r3 = ".mp4"
            boolean r1 = r1.endsWith(r3)
            if (r1 == 0) goto L_0x0038
            com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController$KeyguardWallpaperType r1 = com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController.KeyguardWallpaperType.LIVE_LOCK
            goto L_0x003a
        L_0x0038:
            com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController$KeyguardWallpaperType r1 = com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController.KeyguardWallpaperType.PICTORIAL
        L_0x003a:
            r5 = r1
            r1 = r0
            r0 = r5
        L_0x003d:
            com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController$KeyguardWallpaperType r3 = r6.mKeyguardWallpaperType
            if (r3 == r0) goto L_0x0058
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "wallpaper changed, type="
            r3.append(r4)
            r3.append(r0)
            java.lang.String r3 = r3.toString()
            java.lang.String r4 = "MiuiKeyguardWallpaper"
            android.util.Log.i(r4, r3)
        L_0x0058:
            com.android.keyguard.wallpaper.MiuiKeyguardWallpaperWindow r3 = r6.mKeyguardWallpaper
            r3.updateWallpaperType(r0)
            r6.mKeyguardWallpaperType = r0
            r6.dispatchWallpaperUpdated(r0, r7, r2, r1)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.wallpaper.MiuiKeyguardWallpaperControllerImpl.updateWallpaper(boolean):void");
    }

    public void updateKeyguardRatio(float f, long j) {
        if (shouldDispatchEffects() && !DeviceLevelUtils.isLowEndDevice()) {
            ValueAnimator valueAnimator = this.mKeyguardRatioAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
                this.mKeyguardRatioAnimator = null;
            }
            if (!(j > 0)) {
                dispatchKeyguardSwipeUpdated(f);
                return;
            }
            this.mKeyguardRatioAnimator = ObjectAnimator.ofFloat(new float[]{this.mKeyguardRatio, f});
            this.mKeyguardRatioAnimator.setInterpolator(Interpolators.DECELERATE);
            this.mKeyguardRatioAnimator.setDuration(j);
            this.mKeyguardRatioAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    MiuiKeyguardWallpaperControllerImpl.this.lambda$updateKeyguardRatio$0$MiuiKeyguardWallpaperControllerImpl(valueAnimator);
                }
            });
            this.mKeyguardRatioAnimator.start();
        }
    }

    public /* synthetic */ void lambda$updateKeyguardRatio$0$MiuiKeyguardWallpaperControllerImpl(ValueAnimator valueAnimator) {
        dispatchKeyguardSwipeUpdated(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    public void requestWallpaperBlur(String str, float f) {
        if (!TextUtils.isEmpty(str)) {
            this.mRequestedBlurs.put(str, Float.valueOf(f));
            float floatValue = this.mRequestedBlurs.values().stream().max($$Lambda$GdMqx0Au4lWyLdp0MOWEIaYrmio.INSTANCE).orElse(Float.valueOf(0.0f)).floatValue();
            if (this.mWallpaperBlurRatio != floatValue) {
                dispatchWallpaperBlurUpdated(floatValue);
                this.mWallpaperBlurRatio = floatValue;
                return;
            }
            return;
        }
        throw new IllegalArgumentException("Must have a valid source name");
    }

    public boolean isWallpaperEffectsEnabled() {
        return ActivityManager.isHighEndGfx() && KeyguardWallpaperUtils.hasKeyguardWallpaperEffects(this.mContext);
    }

    public void onPowerSaveChanged(boolean z) {
        if (this.mIsPowerSave != z) {
            this.mIsPowerSave = z;
        }
    }

    public void setWallpaperSupportsAmbientMode(boolean z) {
        this.mSupportsAmbientMode = z;
    }

    public boolean isWallpaperSupportsAmbientMode() {
        return this.mSupportsAmbientMode;
    }

    private boolean shouldDispatchEffects() {
        return hasKeyguardWallpaperLayer() && isWallpaperEffectsEnabled() && !this.mIsPowerSave;
    }

    /* access modifiers changed from: private */
    public void dispatchScreenTurnedOn() {
        if ("fastUnlock".equals(this.mWakingUpReason) || "android.policy:FINGERPRINT".equals(this.mWakingUpReason)) {
            onKeyguardDisappear();
        } else if (this.mKeyguardVisible) {
            onKeyguardShowing();
            this.mScreenOnNotified = true;
        }
    }

    /* access modifiers changed from: private */
    public void onKeyguardShowing() {
        if (this.mIsInteractive) {
            dispatchKeyguardSwipeUpdated(-1.0f);
        }
    }

    /* access modifiers changed from: private */
    public void onKeyguardDisappear() {
        if (!shouldDispatchEffects()) {
            dispatchKeyguardSwipeUpdated(1.0f);
        }
    }

    private void dispatchWallpaperUpdated(MiuiKeyguardWallpaperController.KeyguardWallpaperType keyguardWallpaperType, boolean z, File file, Drawable drawable) {
        synchronized (this.mCallbacks) {
            for (MiuiKeyguardWallpaperController.KeyguardWallpaperCallback onKeyguardWallpaperUpdated : this.mCallbacks) {
                onKeyguardWallpaperUpdated.onKeyguardWallpaperUpdated(keyguardWallpaperType, z, file, drawable);
            }
        }
    }

    private void dispatchWallpaperAnimationUpdated(boolean z) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.forEach(new Consumer(z) {
                private final /* synthetic */ boolean f$0;

                {
                    this.f$0 = r1;
                }

                public final void accept(Object obj) {
                    ((MiuiKeyguardWallpaperController.KeyguardWallpaperCallback) obj).onWallpaperAnimationUpdated(this.f$0);
                }
            });
        }
    }

    private void dispatchKeyguardSwipeUpdated(float f) {
        dispatchKeyguardSwipeUpdated(f, (MiuiKeyguardWallpaperController.KeyguardWallpaperCallback) null);
    }

    private void dispatchKeyguardSwipeUpdated(float f, MiuiKeyguardWallpaperController.KeyguardWallpaperCallback keyguardWallpaperCallback) {
        this.mKeyguardRatio = f;
        if (keyguardWallpaperCallback != null) {
            keyguardWallpaperCallback.onKeyguardAnimationUpdated(this.mKeyguardRatio);
            return;
        }
        synchronized (this.mCallbacks) {
            for (MiuiKeyguardWallpaperController.KeyguardWallpaperCallback onKeyguardAnimationUpdated : this.mCallbacks) {
                onKeyguardAnimationUpdated.onKeyguardAnimationUpdated(this.mKeyguardRatio);
            }
        }
    }

    private void dispatchWallpaperBlurUpdated(float f) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.forEach(new Consumer(f) {
                private final /* synthetic */ float f$0;

                {
                    this.f$0 = r1;
                }

                public final void accept(Object obj) {
                    ((MiuiKeyguardWallpaperController.KeyguardWallpaperCallback) obj).onWallpaperBlurUpdated(this.f$0);
                }
            });
        }
    }

    public void addCallback(MiuiKeyguardWallpaperController.KeyguardWallpaperCallback keyguardWallpaperCallback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.add(keyguardWallpaperCallback);
        }
        if (!this.mKeyguardShowing) {
            dispatchKeyguardSwipeUpdated(1.0f, keyguardWallpaperCallback);
        } else {
            dispatchKeyguardSwipeUpdated(-1.0f, keyguardWallpaperCallback);
        }
        updateWallpaper(false);
    }

    public void removeCallback(MiuiKeyguardWallpaperController.KeyguardWallpaperCallback keyguardWallpaperCallback) {
        synchronized (this.mCallbacks) {
            this.mCallbacks.remove(keyguardWallpaperCallback);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("State of miui keyguard wallpaper");
        printWriter.println("  shouldDispatchEffects=" + shouldDispatchEffects());
        printWriter.println("  wallpaperType=" + this.mKeyguardWallpaperType.name());
        printWriter.println("  blurRatio=" + this.mWallpaperBlurRatio);
        this.mRequestedBlurs.forEach(new BiConsumer(printWriter) {
            private final /* synthetic */ PrintWriter f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj, Object obj2) {
                MiuiKeyguardWallpaperControllerImpl.lambda$dump$3(this.f$0, (String) obj, (Float) obj2);
            }
        });
    }

    static /* synthetic */ void lambda$dump$3(PrintWriter printWriter, String str, Float f) {
        printWriter.print("    ");
        printWriter.print(str);
        printWriter.print(" -> ");
        printWriter.println(f);
    }
}

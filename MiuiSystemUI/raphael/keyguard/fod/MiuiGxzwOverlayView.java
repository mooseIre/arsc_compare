package com.android.keyguard.fod;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.fod.MiuiGxzwIconView;
import com.android.systemui.plugins.R;
import com.android.systemui.util.FixedFileObserver;
import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import miui.hardware.display.DisplayFeatureManager;

class MiuiGxzwOverlayView extends GxzwWindowFrameLayout implements MiuiGxzwIconView.CollectGxzwListener, DisplayManager.DisplayListener {
    private static final double[] CEPHEUS_LOW_BRIGHTNESS_ALPHA = {0.9271d, 0.9235d, 0.9201d, 0.92d, 0.92005d, 0.9169d};
    private BrightnessFileObserver mBrightnessFileObserver;
    private String mBrightnessFilePath = getBrightnessFile();
    private boolean mCollecting = false;
    private final Runnable mDisableReadingModeAction = new Runnable() {
        public final void run() {
            MiuiGxzwOverlayView.this.lambda$new$0$MiuiGxzwOverlayView();
        }
    };
    private DisplayManager mDisplayManager;
    private int mDisplayState = 2;
    /* access modifiers changed from: private */
    public volatile boolean mDozing = false;
    private boolean mEnrolling;
    /* access modifiers changed from: private */
    public final Executor mExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean mGoingToSleep;
    private Handler mHandler = new Handler();
    private View mHbmOverlay;
    private volatile boolean mInvertColors = false;
    /* access modifiers changed from: private */
    public boolean mKeyguardAuthen;
    private WindowManager.LayoutParams mLayoutParams;
    private int mMaxBrightness = -1;
    private MiuiGxzwOverlayTypeManager mMiuiGxzwOverlayTypeManager;
    /* access modifiers changed from: private */
    public float mOverlayAlpha = 0.5f;
    /* access modifiers changed from: private */
    public final boolean mOverlayAlwaysOn;
    private float mPreAlpha = 0.5f;
    private volatile boolean mScreenEffectNone = false;
    private volatile boolean mShowed = false;
    private volatile boolean mWaitDisableReadingMode = false;

    public void onDisplayAdded(int i) {
    }

    public void onDisplayRemoved(int i) {
    }

    public /* synthetic */ void lambda$new$0$MiuiGxzwOverlayView() {
        this.mExecutor.execute(new Runnable() {
            public final void run() {
                MiuiGxzwOverlayView.this.disableReadingMode();
            }
        });
    }

    public MiuiGxzwOverlayView(Context context, MiuiGxzwOverlayTypeManager miuiGxzwOverlayTypeManager) {
        super(context);
        this.mMiuiGxzwOverlayTypeManager = miuiGxzwOverlayTypeManager;
        this.mOverlayAlwaysOn = miuiGxzwOverlayTypeManager.isOverlayTypeAlwaysOn();
        initView();
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.miui_keyguard_gxzw_overlay, this);
        this.mHbmOverlay = findViewById(R.id.hbm_overlay);
        setSystemUiVisibility(4864);
        this.mLayoutParams = new WindowManager.LayoutParams(-1, -1, 2021, 83957016, -2);
        WindowManager.LayoutParams layoutParams = this.mLayoutParams;
        layoutParams.extraFlags |= 8388608;
        layoutParams.privateFlags |= MiuiGxzwUtils.PRIVATE_FLAG_IS_HBM_OVERLAY;
        WindowManager.LayoutParams layoutParams2 = this.mLayoutParams;
        layoutParams2.alpha = 0.0f;
        layoutParams2.setTitle("hbm_overlay");
        this.mBrightnessFileObserver = new BrightnessFileObserver(this.mBrightnessFilePath);
        setVisibility(8);
        this.mDisplayManager = (DisplayManager) getContext().getSystemService("display");
        if (!this.mMiuiGxzwOverlayTypeManager.isOverlayTypeUrsa()) {
            this.mDisplayManager.registerDisplayListener(this, this.mHandler);
        }
    }

    public void show() {
        if (!this.mShowed) {
            Log.d("MiuiGxzwOverlayView", "show");
            this.mInvertColors = MiuiKeyguardUtils.isInvertColorsEnable(getContext());
            this.mHbmOverlay.setBackgroundColor(this.mInvertColors ? -1 : -16777216);
            this.mShowed = true;
            if (this.mKeyguardAuthen) {
                this.mLayoutParams.screenOrientation = 5;
            } else {
                this.mLayoutParams.screenOrientation = -1;
            }
            addViewAndUpdateAlpha();
            if (!this.mScreenEffectNone) {
                this.mExecutor.execute(new Runnable() {
                    public final void run() {
                        MiuiGxzwOverlayView.this.lambda$show$1$MiuiGxzwOverlayView();
                    }
                });
                this.mScreenEffectNone = true;
            }
            this.mWaitDisableReadingMode = true;
            if (!this.mGoingToSleep) {
                this.mDisableReadingModeAction.run();
            }
        }
    }

    public /* synthetic */ void lambda$show$1$MiuiGxzwOverlayView() {
        notifySurfaceFlinger(1);
        DisplayFeatureManager.getInstance().setScreenEffect(17, 1, 1);
    }

    public void dismiss() {
        if (this.mShowed) {
            Log.d("MiuiGxzwOverlayView", "dismiss");
            this.mShowed = false;
            restoreScreenEffect();
            removeOverlayView();
            updateBrightnessFileWatchState();
        }
    }

    public void startDozing() {
        Log.d("MiuiGxzwOverlayView", "startDozing");
        this.mDozing = true;
        this.mOverlayAlpha = 0.657f;
        updateAlpha(this.mOverlayAlpha);
        addOverlayView();
    }

    public void stopDozing() {
        Log.d("MiuiGxzwOverlayView", "stopDozing");
        this.mDozing = false;
        if (!this.mCollecting && !this.mOverlayAlwaysOn) {
            removeOverlayView();
        }
        if (this.mOverlayAlwaysOn && !MiuiGxzwManager.getInstance().isUnlockByGxzw()) {
            updateAlpha(this.mOverlayAlpha);
        }
        updateBrightnessFileWatchState();
    }

    public void onScreenTurnedOn() {
        Log.d("MiuiGxzwOverlayView", "onScreenTurnedOn");
        updateBrightnessFileWatchState();
    }

    public void onStartedGoingToSleep() {
        this.mGoingToSleep = true;
        Log.d("MiuiGxzwOverlayView", "onStartedGoingToSleep");
        if (!this.mCollecting && !this.mOverlayAlwaysOn) {
            removeOverlayView();
        }
    }

    public void disableReadingMode() {
        if (this.mWaitDisableReadingMode) {
            this.mWaitDisableReadingMode = false;
            notifySurfaceFlinger(1);
            DisplayFeatureManager.getInstance().setScreenEffect(17, 1, 1);
        }
    }

    public void onFinishedGoingToSleep() {
        Log.d("MiuiGxzwOverlayView", "onFinishedGoingToSleep");
        this.mDisableReadingModeAction.run();
        this.mGoingToSleep = false;
    }

    public void onKeyguardAuthen(boolean z) {
        this.mKeyguardAuthen = z;
    }

    public void setEnrolling(boolean z) {
        this.mEnrolling = z;
    }

    public void restoreScreenEffect() {
        if (this.mScreenEffectNone) {
            this.mExecutor.execute(new Runnable(isAttachedToWindow()) {
                private final /* synthetic */ boolean f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MiuiGxzwOverlayView.this.lambda$restoreScreenEffect$2$MiuiGxzwOverlayView(this.f$1);
                }
            });
            this.mScreenEffectNone = false;
        }
        this.mWaitDisableReadingMode = false;
        this.mHandler.removeCallbacks(this.mDisableReadingModeAction);
    }

    public /* synthetic */ void lambda$restoreScreenEffect$2$MiuiGxzwOverlayView(boolean z) {
        int i = z ? 0 : 2;
        notifySurfaceFlinger(i);
        DisplayFeatureManager.getInstance().setScreenEffect(17, i, 1);
    }

    public void onHandUpChange(boolean z) {
        if (z) {
            removeOverlayView();
        } else {
            addOverlayView();
        }
    }

    public void onCollectStateChange(boolean z) {
        this.mCollecting = z;
        if (z) {
            addOverlayView();
            if (this.mDozing) {
                updateAlpha(this.mOverlayAlpha);
            }
        } else if (this.mDozing && !MiuiGxzwManager.getInstance().isHbmAlwaysOnWhenDoze()) {
            updateAlpha(0.0f);
        }
    }

    public void onIconStateChange(boolean z) {
        if (this.mDozing && MiuiGxzwManager.getInstance().isHbmAlwaysOnWhenDoze()) {
            if (z) {
                updateAlpha(0.0f);
                return;
            }
            addOverlayView();
            updateAlpha(this.mOverlayAlpha);
        }
    }

    /* access modifiers changed from: private */
    public void addOverlayView() {
        if (this.mShowed && getVisibility() != 0 && !isAttachedToWindow() && getParent() == null) {
            this.mLayoutParams.alpha = this.mOverlayAlpha;
            if (this.mDozing) {
                MiuiGxzwManager.getInstance().requestDrawWackLock(300);
            }
            if (isDisplayDozing() && !this.mCollecting && !MiuiGxzwManager.getInstance().isHbmAlwaysOnWhenDoze()) {
                this.mLayoutParams.alpha = 0.0f;
            }
            if (!this.mMiuiGxzwOverlayTypeManager.isOverlayTypeUrsa()) {
                this.mLayoutParams.setTitle(this.mEnrolling ? "enroll_overlay" : "hbm_overlay");
            }
            Slog.i("MiuiGxzwOverlayView", "add overlay view: mLayoutParams.alpha = " + this.mLayoutParams.alpha);
            addViewToWindow();
            setVisibility(0);
        }
    }

    private void removeOverlayView() {
        if (getVisibility() == 0) {
            Slog.i("MiuiGxzwOverlayView", "remove overlay view");
            if (this.mDozing) {
                MiuiGxzwManager.getInstance().requestDrawWackLock(300);
            }
            removeViewFromWindow();
            setVisibility(8);
        }
    }

    private void addViewAndUpdateAlpha() {
        new AsyncTask<Void, Void, Float>() {
            /* access modifiers changed from: protected */
            public Float doInBackground(Void... voidArr) {
                return Float.valueOf(MiuiGxzwOverlayView.this.caculateOverlayAlpha());
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Float f) {
                float unused = MiuiGxzwOverlayView.this.mOverlayAlpha = f.floatValue();
                if (!MiuiGxzwOverlayView.this.mKeyguardAuthen || MiuiGxzwOverlayView.this.mDozing || MiuiGxzwOverlayView.this.mOverlayAlwaysOn) {
                    MiuiGxzwOverlayView.this.addOverlayView();
                }
                MiuiGxzwOverlayView.this.updateBrightnessFileWatchState();
            }
        }.executeOnExecutor(this.mExecutor, new Void[0]);
    }

    private void notifySurfaceFlinger(int i) {
        MiuiGxzwUtils.notifySurfaceFlinger(1102, i);
    }

    /* access modifiers changed from: private */
    public void updateAlpha(float f) {
        if (this.mShowed && isAttachedToWindow()) {
            if (isDisplayDozing() && !this.mCollecting && !MiuiGxzwManager.getInstance().isHbmAlwaysOnWhenDoze()) {
                f = 0.0f;
            }
            if (Float.compare(this.mLayoutParams.alpha, f) != 0) {
                this.mLayoutParams.alpha = f;
                Slog.i("MiuiGxzwOverlayView", "upldate overlay view alpha: " + f);
                this.mWindowManager.updateViewLayout(this, this.mLayoutParams);
                if (this.mDozing) {
                    MiuiGxzwManager.getInstance().requestDrawWackLock(300);
                }
            }
        }
    }

    private boolean isDisplayDozing() {
        if (this.mMiuiGxzwOverlayTypeManager.isOverlayTypeUrsa()) {
            return this.mDozing;
        }
        int i = this.mDisplayState;
        return (i == 3 || i == 4) && this.mDozing;
    }

    private String getBrightnessFile() {
        String[] stringArray = getResources().getStringArray(285343774);
        for (int i = 0; i < stringArray.length; i++) {
            if (new File(stringArray[i]).exists()) {
                return stringArray[i];
            }
        }
        return "/sys/class/leds/lcd-backlight/brightness";
    }

    /* access modifiers changed from: private */
    public float caculateOverlayAlpha() {
        if (this.mMaxBrightness <= 0) {
            this.mMaxBrightness = readMaxBrightnessFromFile();
        }
        if (this.mInvertColors) {
            return brighnessToAlpha(converBrighnessFrom1024(8));
        }
        int readBrightnessFromFile = readBrightnessFromFile();
        float f = 0.657f;
        if (readBrightnessFromFile > 0 && this.mMaxBrightness > 0) {
            Log.i("MiuiGxzwOverlayView", "read brightness from file: " + readBrightnessFromFile + ", mMaxBrightness = " + this.mMaxBrightness);
            int min = Math.min(readBrightnessFromFile, this.mMaxBrightness);
            if (!this.mDozing) {
                f = brighnessToAlpha(min);
            } else if (min <= converBrighnessFrom1024(5)) {
                if (min > 0) {
                    f = 0.89f;
                } else {
                    f = brighnessToAlpha(min);
                }
            }
            this.mPreAlpha = f;
        } else if (this.mDozing) {
            this.mPreAlpha = 0.657f;
        } else {
            f = this.mPreAlpha;
        }
        Log.i("MiuiGxzwOverlayView", "caculate overlay alpha: " + f);
        return f;
    }

    private float brighnessToAlpha(int i) {
        double d;
        double d2;
        if (i == 0) {
            d = 0.9619584887d;
        } else {
            if (i >= 2 && i <= 8 && ("equuleus".equals(Build.DEVICE) || "ursa".equals(Build.DEVICE))) {
                d2 = (((double) i) * 0.0032d) + 0.0739d;
            } else if (i >= 5 && i <= 10 && "cepheus".equals(Build.DEVICE)) {
                d = CEPHEUS_LOW_BRIGHTNESS_ALPHA[i - 5];
            } else if (this.mMiuiGxzwOverlayTypeManager.isOverlayTypeUrsa()) {
                d2 = Math.pow((((((double) i) * 1.0d) / ((double) this.mMaxBrightness)) * 430.0d) / 600.0d, 0.45d);
            } else if (i > 500) {
                d2 = Math.pow((((((double) i) * 1.0d) / 2047.0d) * 430.0d) / 600.0d, 0.455d);
            } else {
                d2 = Math.pow((((double) i) * 1.0d) / 1680.0d, 0.455d);
            }
            d = 1.0d - d2;
        }
        return (float) d;
    }

    private int converBrighnessFrom1024(int i) {
        return (int) (((((float) this.mMaxBrightness) + 1.0f) / 1024.0f) * ((float) i));
    }

    private int readBrightnessFromFile() {
        return readIntFromFile(this.mBrightnessFilePath);
    }

    private int readMaxBrightnessFromFile() {
        File file = new File(this.mBrightnessFilePath);
        return readIntFromFile(file.getParentFile().getAbsolutePath() + "/max_brightness");
    }

    /* JADX WARNING: Removed duplicated region for block: B:23:0x0032 A[SYNTHETIC, Splitter:B:23:0x0032] */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x003e A[SYNTHETIC, Splitter:B:28:0x003e] */
    /* JADX WARNING: Removed duplicated region for block: B:34:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int readIntFromFile(java.lang.String r3) {
        /*
            r2 = this;
            r2 = 0
            java.io.BufferedReader r0 = new java.io.BufferedReader     // Catch:{ Exception -> 0x002c }
            java.io.FileReader r1 = new java.io.FileReader     // Catch:{ Exception -> 0x002c }
            r1.<init>(r3)     // Catch:{ Exception -> 0x002c }
            r0.<init>(r1)     // Catch:{ Exception -> 0x002c }
            java.lang.String r2 = r0.readLine()     // Catch:{ Exception -> 0x0026, all -> 0x0022 }
            if (r2 == 0) goto L_0x001e
            int r2 = java.lang.Integer.parseInt(r2)     // Catch:{ Exception -> 0x0026, all -> 0x0022 }
            r0.close()     // Catch:{ IOException -> 0x0019 }
            goto L_0x001d
        L_0x0019:
            r3 = move-exception
            r3.printStackTrace()
        L_0x001d:
            return r2
        L_0x001e:
            r0.close()     // Catch:{ IOException -> 0x0036 }
            goto L_0x003a
        L_0x0022:
            r2 = move-exception
            r3 = r2
            r2 = r0
            goto L_0x003c
        L_0x0026:
            r2 = move-exception
            r3 = r2
            r2 = r0
            goto L_0x002d
        L_0x002a:
            r3 = move-exception
            goto L_0x003c
        L_0x002c:
            r3 = move-exception
        L_0x002d:
            r3.printStackTrace()     // Catch:{ all -> 0x002a }
            if (r2 == 0) goto L_0x003a
            r2.close()     // Catch:{ IOException -> 0x0036 }
            goto L_0x003a
        L_0x0036:
            r2 = move-exception
            r2.printStackTrace()
        L_0x003a:
            r2 = -1
            return r2
        L_0x003c:
            if (r2 == 0) goto L_0x0046
            r2.close()     // Catch:{ IOException -> 0x0042 }
            goto L_0x0046
        L_0x0042:
            r2 = move-exception
            r2.printStackTrace()
        L_0x0046:
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.fod.MiuiGxzwOverlayView.readIntFromFile(java.lang.String):int");
    }

    /* access modifiers changed from: private */
    public void updateBrightnessFileWatchState() {
        if (this.mMiuiGxzwOverlayTypeManager.isOverlayTypeUrsa()) {
            if (this.mShowed) {
                this.mBrightnessFileObserver.stopWatching();
                this.mBrightnessFileObserver.startWatching();
                this.mBrightnessFileObserver.onEvent(2, this.mBrightnessFilePath);
                return;
            }
            this.mBrightnessFileObserver.stopWatching();
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mShowed && getVisibility() == 0 && Float.compare(this.mOverlayAlpha, this.mLayoutParams.alpha) != 0) {
            updateAlpha(this.mOverlayAlpha);
        }
    }

    /* access modifiers changed from: protected */
    public WindowManager.LayoutParams generateLayoutParams() {
        return this.mLayoutParams;
    }

    public void onDisplayChanged(int i) {
        if (i == 0) {
            int state = this.mDisplayManager.getDisplay(i).getState();
            int i2 = this.mDisplayState;
            this.mDisplayState = state;
            if ((i2 == 3 || i2 == 4 || (state != 3 && state != 4)) ? false : true) {
                updateAlpha(this.mOverlayAlpha);
            }
        }
    }

    private class BrightnessFileObserver extends FixedFileObserver {
        public BrightnessFileObserver(String str) {
            super(str, 2);
        }

        public void onEvent(int i, String str) {
            Log.i("MiuiGxzwOverlayView", "onEvent: event = " + i);
            if (i == 2) {
                new AsyncTask<Void, Void, Float>() {
                    /* access modifiers changed from: protected */
                    public Float doInBackground(Void... voidArr) {
                        return Float.valueOf(MiuiGxzwOverlayView.this.caculateOverlayAlpha());
                    }

                    /* access modifiers changed from: protected */
                    public void onPostExecute(Float f) {
                        float unused = MiuiGxzwOverlayView.this.mOverlayAlpha = f.floatValue();
                        MiuiGxzwOverlayView.this.updateAlpha(f.floatValue());
                    }
                }.executeOnExecutor(MiuiGxzwOverlayView.this.mExecutor, new Void[0]);
            }
        }
    }
}

package com.android.systemui.settings;

import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.display.BrightnessUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.settings.ToggleSlider;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import miui.mqsas.sdk.MQSEventManagerDelegate;

public class BrightnessController implements ToggleSlider.Listener, Dumpable {
    /* access modifiers changed from: private */
    public volatile boolean mAutomatic;
    /* access modifiers changed from: private */
    public final boolean mAutomaticAvailable;
    /* access modifiers changed from: private */
    public final Handler mBackgroundHandler;
    /* access modifiers changed from: private */
    public final BrightnessObserver mBrightnessObserver;
    /* access modifiers changed from: private */
    public ArrayList<BrightnessStateChangeCallback> mChangeCallbacks = new ArrayList<>();
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public final ToggleSlider mControl;
    private boolean mControlValueInitialized;
    /* access modifiers changed from: private */
    public final int mDefaultBacklight;
    /* access modifiers changed from: private */
    public final int mDefaultBacklightForVr;
    private final DisplayManager mDisplayManager;
    /* access modifiers changed from: private */
    public boolean mExternalChange;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            boolean z = true;
            boolean unused = BrightnessController.this.mExternalChange = true;
            try {
                int i = message.what;
                if (i == 0) {
                    BrightnessController brightnessController = BrightnessController.this;
                    int i2 = message.arg1;
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    brightnessController.updateSlider(i2, z);
                } else if (i == 1) {
                    BrightnessController.this.mControl.setOnChangedListener(BrightnessController.this);
                } else if (i == 2) {
                    BrightnessController.this.mControl.setOnChangedListener((ToggleSlider.Listener) null);
                } else if (i != 3) {
                    super.handleMessage(message);
                } else {
                    BrightnessController brightnessController2 = BrightnessController.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    brightnessController2.updateVrMode(z);
                }
            } finally {
                boolean unused2 = BrightnessController.this.mExternalChange = false;
            }
        }
    };
    /* access modifiers changed from: private */
    public volatile boolean mIsVrModeEnabled;
    private boolean mListening;
    private final int mMaximumBacklight;
    private final int mMaximumBacklightForVr;
    private final int mMinimumBacklight;
    private final int mMinimumBacklightForVr;
    /* access modifiers changed from: private */
    public int mSliderAnimationDuration = 3000;
    private ValueAnimator mSliderAnimator;
    private final Runnable mStartListeningRunnable = new Runnable() {
        public void run() {
            BrightnessController.this.mBrightnessObserver.startObserving();
            BrightnessController.this.mUserTracker.startTracking();
            BrightnessController.this.mUpdateModeRunnable.run();
            BrightnessController.this.mUpdateSliderRunnable.run();
            BrightnessController.this.mHandler.sendEmptyMessage(1);
        }
    };
    private int mStartValue;
    private final Runnable mStopListeningRunnable = new Runnable() {
        public void run() {
            BrightnessController.this.mBrightnessObserver.stopObserving();
            BrightnessController.this.mUserTracker.stopTracking();
            BrightnessController.this.mHandler.sendEmptyMessage(2);
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mUpdateModeRunnable = new Runnable() {
        public void run() {
            if (BrightnessController.this.mAutomaticAvailable) {
                boolean z = false;
                int intForUser = Settings.System.getIntForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2);
                BrightnessController brightnessController = BrightnessController.this;
                if (intForUser != 0) {
                    z = true;
                }
                boolean unused = brightnessController.mAutomatic = z;
            }
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mUpdateSliderRunnable = new Runnable() {
        public void run() {
            int i;
            boolean access$1000 = BrightnessController.this.mIsVrModeEnabled;
            if (access$1000) {
                i = Settings.System.getIntForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness_for_vr", BrightnessController.this.mDefaultBacklightForVr, KeyguardUpdateMonitor.getCurrentUser());
            } else {
                i = Settings.System.getIntForUser(BrightnessController.this.mContext.getContentResolver(), "screen_brightness", BrightnessController.this.mDefaultBacklight, KeyguardUpdateMonitor.getCurrentUser());
                BrightnessController brightnessController = BrightnessController.this;
                int unused = brightnessController.mSliderAnimationDuration = Settings.System.getIntForUser(brightnessController.mContext.getContentResolver(), "slider_animation_duration", 3000, KeyguardUpdateMonitor.getCurrentUser());
            }
            Log.d("BrightnessController", "UpdateSliderRunnable: value: " + i);
            BrightnessController.this.mHandler.obtainMessage(0, i, access$1000 ? 1 : 0).sendToTarget();
        }
    };
    /* access modifiers changed from: private */
    public final CurrentUserTracker mUserTracker;
    private final IVrManager mVrManager;
    private final IVrStateCallbacks mVrStateCallbacks = new IVrStateCallbacks.Stub() {
        public void onVrStateChanged(boolean z) {
            BrightnessController.this.mHandler.obtainMessage(3, z ? 1 : 0, 0).sendToTarget();
        }
    };

    public interface BrightnessStateChangeCallback {
        void onBrightnessLevelChanged();
    }

    public void onInit(ToggleSlider toggleSlider) {
    }

    private class BrightnessObserver extends ContentObserver {
        private final Uri BRIGHTNESS_FOR_VR_URI = Settings.System.getUriFor("screen_brightness_for_vr");
        private final Uri BRIGHTNESS_MODE_URI = Settings.System.getUriFor("screen_brightness_mode");
        private final Uri BRIGHTNESS_URI = Settings.System.getUriFor("screen_brightness");
        private final Uri DURATION_SLIDE_BAR_ANIMATION = Settings.System.getUriFor("slider_animation_duration");

        public BrightnessObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean z) {
            onChange(z, (Uri) null);
        }

        public void onChange(boolean z, Uri uri) {
            if (!z) {
                if (this.BRIGHTNESS_MODE_URI.equals(uri)) {
                    Log.d("BrightnessController", "BrightnessObserver: brightness mode change.");
                    BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateModeRunnable);
                    BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
                } else if (this.BRIGHTNESS_URI.equals(uri)) {
                    Log.d("BrightnessController", "BrightnessObserver: brightness change.");
                    BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
                } else if (this.BRIGHTNESS_FOR_VR_URI.equals(uri)) {
                    Log.d("BrightnessController", "BrightnessObserver: vr brightness change.");
                    BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
                } else if (this.DURATION_SLIDE_BAR_ANIMATION.equals(uri)) {
                    Log.d("BrightnessController", "BrightnessObserver: slide animation duration change.");
                    BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
                    return;
                } else {
                    BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateModeRunnable);
                    BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
                }
                Iterator it = BrightnessController.this.mChangeCallbacks.iterator();
                while (it.hasNext()) {
                    ((BrightnessStateChangeCallback) it.next()).onBrightnessLevelChanged();
                }
            }
        }

        public void startObserving() {
            Log.d("BrightnessController", "BrightnessObserver: startObserving.");
            ContentResolver contentResolver = BrightnessController.this.mContext.getContentResolver();
            contentResolver.unregisterContentObserver(this);
            contentResolver.registerContentObserver(this.BRIGHTNESS_MODE_URI, false, this, -1);
            contentResolver.registerContentObserver(this.BRIGHTNESS_URI, false, this, -1);
            contentResolver.registerContentObserver(this.BRIGHTNESS_FOR_VR_URI, false, this, -1);
            contentResolver.registerContentObserver(this.DURATION_SLIDE_BAR_ANIMATION, false, this, -1);
        }

        public void stopObserving() {
            Log.d("BrightnessController", "BrightnessObserver: stopObserving.");
            BrightnessController.this.mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    public BrightnessController(Context context, ToggleSlider toggleSlider) {
        this.mContext = context;
        this.mControl = toggleSlider;
        toggleSlider.setMax(BrightnessUtils.GAMMA_SPACE_MAX);
        this.mBackgroundHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
        this.mUserTracker = new CurrentUserTracker(this.mContext) {
            public void onUserSwitched(int i) {
                BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateModeRunnable);
                BrightnessController.this.mBackgroundHandler.post(BrightnessController.this.mUpdateSliderRunnable);
            }
        };
        this.mBrightnessObserver = new BrightnessObserver(this.mHandler);
        PowerManager powerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mMinimumBacklight = powerManager.getMinimumScreenBrightnessSetting();
        this.mMaximumBacklight = powerManager.getMaximumScreenBrightnessSetting();
        this.mDefaultBacklight = powerManager.getDefaultScreenBrightnessSetting();
        this.mMinimumBacklightForVr = powerManager.getMinimumScreenBrightnessForVrSetting();
        this.mMaximumBacklightForVr = powerManager.getMaximumScreenBrightnessForVrSetting();
        this.mDefaultBacklightForVr = powerManager.getDefaultScreenBrightnessForVrSetting();
        this.mAutomaticAvailable = context.getResources().getBoolean(17891369);
        this.mDisplayManager = (DisplayManager) context.getSystemService(DisplayManager.class);
        this.mVrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
    }

    public void registerCallbacks() {
        if (!this.mListening) {
            this.mControlValueInitialized = false;
            IVrManager iVrManager = this.mVrManager;
            if (iVrManager != null) {
                try {
                    iVrManager.registerListener(this.mVrStateCallbacks);
                    this.mIsVrModeEnabled = this.mVrManager.getVrModeState();
                } catch (RemoteException e) {
                    Log.e("BrightnessController", "Failed to register VR mode state listener: ", e);
                }
            }
            this.mBackgroundHandler.post(this.mStartListeningRunnable);
            this.mListening = true;
        }
    }

    public void unregisterCallbacks() {
        if (this.mListening) {
            IVrManager iVrManager = this.mVrManager;
            if (iVrManager != null) {
                try {
                    iVrManager.unregisterListener(this.mVrStateCallbacks);
                } catch (RemoteException e) {
                    Log.e("BrightnessController", "Failed to unregister VR mode state listener: ", e);
                }
            }
            this.mBackgroundHandler.post(this.mStopListeningRunnable);
            this.mListening = false;
        }
    }

    public void onStart(int i) {
        Log.d("BrightnessController", "ToggleSlider: onStart: value: " + i);
        this.mStartValue = i;
        MQSEventManagerDelegate.getInstance().reportBrightnessEvent(0, i, this.mAutomatic ? 1 : 0, "");
    }

    public void onStop(int i) {
        Log.d("BrightnessController", "ToggleSlider: onStop: value: " + i);
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleSlideBrightnessBarEvent(this.mStartValue, i, this.mAutomatic);
        MQSEventManagerDelegate.getInstance().reportBrightnessEvent(1, i, this.mAutomatic ? 1 : 0, "");
    }

    public void onChanged(ToggleSlider toggleSlider, final boolean z, int i, boolean z2) {
        int i2;
        final String str;
        int i3;
        int i4;
        if (!this.mExternalChange) {
            ValueAnimator valueAnimator = this.mSliderAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            if (this.mIsVrModeEnabled) {
                i2 = 498;
                i4 = this.mMinimumBacklightForVr;
                i3 = this.mMaximumBacklightForVr;
                str = "screen_brightness_for_vr";
            } else {
                i2 = this.mAutomatic ? 219 : 218;
                i4 = this.mMinimumBacklight;
                i3 = this.mMaximumBacklight;
                str = "screen_brightness";
            }
            final int convertGammaToLinear = BrightnessUtils.convertGammaToLinear(i, i4, i3);
            if (z2) {
                MetricsLogger.action(this.mContext, i2, convertGammaToLinear);
            }
            AsyncTask.execute(new Runnable() {
                public void run() {
                    if (z) {
                        BrightnessController.this.setBrightness(convertGammaToLinear);
                    } else if (Settings.System.getIntForUser(BrightnessController.this.mContext.getContentResolver(), str, -1, -2) == convertGammaToLinear) {
                        BrightnessController.this.setBrightness(-1);
                    } else {
                        Settings.System.putIntForUser(BrightnessController.this.mContext.getContentResolver(), str, convertGammaToLinear, -2);
                    }
                }
            });
            Iterator<BrightnessStateChangeCallback> it = this.mChangeCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onBrightnessLevelChanged();
            }
        }
    }

    /* access modifiers changed from: private */
    public void setBrightness(int i) {
        this.mDisplayManager.setTemporaryBrightness((float) i);
    }

    /* access modifiers changed from: private */
    public void updateVrMode(boolean z) {
        if (this.mIsVrModeEnabled != z) {
            this.mIsVrModeEnabled = z;
            this.mBackgroundHandler.post(this.mUpdateSliderRunnable);
        }
    }

    /* access modifiers changed from: private */
    public void updateSlider(int i, boolean z) {
        int i2;
        int i3;
        if (z) {
            i2 = this.mMinimumBacklightForVr;
            i3 = this.mMaximumBacklightForVr;
        } else {
            i2 = this.mMinimumBacklight;
            i3 = this.mMaximumBacklight;
        }
        if (i == BrightnessUtils.convertGammaToLinear(this.mControl.getValue(), i2, i3)) {
            this.mControlValueInitialized = true;
        } else {
            animateSliderTo(BrightnessUtils.convertLinearToGamma(i, i2, i3));
        }
    }

    private void animateSliderTo(int i) {
        if (!this.mControlValueInitialized) {
            this.mControl.setValue(i);
            this.mControlValueInitialized = true;
        }
        ValueAnimator valueAnimator = this.mSliderAnimator;
        if (valueAnimator != null && valueAnimator.isStarted()) {
            this.mSliderAnimator.cancel();
        }
        ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{this.mControl.getValue(), i});
        this.mSliderAnimator = ofInt;
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                BrightnessController.this.lambda$animateSliderTo$0$BrightnessController(valueAnimator);
            }
        });
        ValueAnimator valueAnimator2 = this.mSliderAnimator;
        int i2 = this.mSliderAnimationDuration;
        valueAnimator2.setDuration(i2 > 0 ? (long) i2 : 3000);
        this.mSliderAnimator.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateSliderTo$0 */
    public /* synthetic */ void lambda$animateSliderTo$0$BrightnessController(ValueAnimator valueAnimator) {
        this.mExternalChange = true;
        this.mControl.setValue(((Integer) valueAnimator.getAnimatedValue()).intValue());
        this.mExternalChange = false;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("BrightnessController state:");
        printWriter.print("  mAutomaticAvailable=");
        printWriter.println(this.mAutomaticAvailable);
        printWriter.print("  mAutomatic=");
        printWriter.println(this.mAutomatic);
        printWriter.print("  mIsVrModeEnabled=");
        printWriter.println(this.mIsVrModeEnabled);
        printWriter.print("  mSliderAnimationDuration=");
        printWriter.println(this.mSliderAnimationDuration);
    }
}

package com.android.systemui.statusbar.policy;

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
import android.util.MathUtils;
import com.android.internal.BrightnessSynchronizer;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.settingslib.RestrictedLockUtilsInternal;
import com.android.settingslib.display.BrightnessUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.BrightnessController$BrightnessStateChangeCallback;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.settings.ToggleSlider;
import com.android.systemui.settings.ToggleSliderView;
import com.miui.systemui.analytics.SystemUIStat;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import miui.mqsas.sdk.MQSEventManagerDelegate;

public class MiuiBrightnessController implements ToggleSlider.Listener, Dumpable {
    private static final Uri BRIGHTNESS_FLOAT_URI = Settings.System.getUriFor("screen_brightness_float");
    private static final Uri BRIGHTNESS_FOR_VR_FLOAT_URI = Settings.System.getUriFor("screen_brightness_for_vr_float");
    private static final Uri BRIGHTNESS_MODE_URI = Settings.System.getUriFor("screen_brightness_mode");
    private static final Uri BRIGHTNESS_URI = Settings.System.getUriFor("screen_brightness");
    private static final Uri DURATION_SLIDE_BAR_ANIMATION = Settings.System.getUriFor("slider_animation_duration");
    private volatile boolean mAutomatic;
    private final boolean mAutomaticAvailable;
    private final Handler mBackgroundHandler;
    private final BrightnessObserver mBrightnessObserver;
    private ArrayList<BrightnessController$BrightnessStateChangeCallback> mChangeCallbacks = new ArrayList<>();
    private final Context mContext;
    private final ToggleSlider mControl;
    private boolean mControlValueInitialized;
    private final float mDefaultBacklight;
    private final float mDefaultBacklightForVr;
    private final DisplayManager mDisplayManager;
    private boolean mExternalChange;
    private final Handler mHandler = new Handler() {
        /* class com.android.systemui.statusbar.policy.MiuiBrightnessController.AnonymousClass6 */

        public void handleMessage(Message message) {
            boolean z = true;
            MiuiBrightnessController.this.mExternalChange = true;
            try {
                int i = message.what;
                if (i == 0) {
                    MiuiBrightnessController miuiBrightnessController = MiuiBrightnessController.this;
                    float intBitsToFloat = Float.intBitsToFloat(message.arg1);
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    miuiBrightnessController.updateSlider(intBitsToFloat, z);
                } else if (i == 1) {
                    MiuiBrightnessController.this.mControl.setOnChangedListener(MiuiBrightnessController.this);
                } else if (i == 2) {
                    MiuiBrightnessController.this.mControl.setOnChangedListener(null);
                } else if (i != 3) {
                    super.handleMessage(message);
                } else {
                    MiuiBrightnessController miuiBrightnessController2 = MiuiBrightnessController.this;
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    miuiBrightnessController2.updateVrMode(z);
                }
            } finally {
                MiuiBrightnessController.this.mExternalChange = false;
            }
        }
    };
    private volatile boolean mIsVrModeEnabled;
    private boolean mListening;
    private final float mMaximumBacklight;
    private final float mMaximumBacklightForVr;
    private final int mMaximumBacklightForVrInt;
    private final int mMaximumBacklightInt;
    private final float mMinimumBacklight;
    private final float mMinimumBacklightForVr;
    private final int mMinimumBacklightForVrInt;
    private final int mMinimumBacklightInt;
    private int mSliderAnimationDuration = 3000;
    private ValueAnimator mSliderAnimator;
    private final Runnable mStartListeningRunnable = new Runnable() {
        /* class com.android.systemui.statusbar.policy.MiuiBrightnessController.AnonymousClass1 */

        public void run() {
            if (!MiuiBrightnessController.this.mListening) {
                MiuiBrightnessController.this.mListening = true;
                MiuiBrightnessController.this.mControlValueInitialized = false;
                if (MiuiBrightnessController.this.mVrManager != null) {
                    try {
                        MiuiBrightnessController.this.mVrManager.registerListener(MiuiBrightnessController.this.mVrStateCallbacks);
                        MiuiBrightnessController.this.mIsVrModeEnabled = MiuiBrightnessController.this.mVrManager.getVrModeState();
                    } catch (RemoteException e) {
                        Log.e("BrightnessController", "Failed to register VR mode state listener: ", e);
                    }
                }
                MiuiBrightnessController.this.mBrightnessObserver.startObserving();
                MiuiBrightnessController.this.mUserTracker.startTracking();
                MiuiBrightnessController.this.mUpdateModeRunnable.run();
                MiuiBrightnessController.this.mUpdateSliderRunnable.run();
                MiuiBrightnessController.this.mHandler.sendEmptyMessage(1);
            }
        }
    };
    private int mStartValue;
    private final Runnable mStopListeningRunnable = new Runnable() {
        /* class com.android.systemui.statusbar.policy.MiuiBrightnessController.AnonymousClass2 */

        public void run() {
            if (MiuiBrightnessController.this.mListening) {
                MiuiBrightnessController.this.mListening = false;
                if (MiuiBrightnessController.this.mVrManager != null) {
                    try {
                        MiuiBrightnessController.this.mVrManager.unregisterListener(MiuiBrightnessController.this.mVrStateCallbacks);
                    } catch (RemoteException e) {
                        Log.e("BrightnessController", "Failed to unregister VR mode state listener: ", e);
                    }
                }
                MiuiBrightnessController.this.mBrightnessObserver.stopObserving();
                MiuiBrightnessController.this.mUserTracker.stopTracking();
                MiuiBrightnessController.this.mHandler.sendEmptyMessage(2);
            }
        }
    };
    private final Runnable mUpdateModeRunnable = new Runnable() {
        /* class com.android.systemui.statusbar.policy.MiuiBrightnessController.AnonymousClass3 */

        public void run() {
            if (MiuiBrightnessController.this.mAutomaticAvailable) {
                boolean z = false;
                int intForUser = Settings.System.getIntForUser(MiuiBrightnessController.this.mContext.getContentResolver(), "screen_brightness_mode", 0, -2);
                MiuiBrightnessController miuiBrightnessController = MiuiBrightnessController.this;
                if (intForUser != 0) {
                    z = true;
                }
                miuiBrightnessController.mAutomatic = z;
            }
        }
    };
    private final Runnable mUpdateSliderRunnable = new Runnable() {
        /* class com.android.systemui.statusbar.policy.MiuiBrightnessController.AnonymousClass4 */

        public void run() {
            float f;
            boolean z = MiuiBrightnessController.this.mIsVrModeEnabled;
            if (z) {
                f = Settings.System.getFloatForUser(MiuiBrightnessController.this.mContext.getContentResolver(), "screen_brightness_for_vr_float", MiuiBrightnessController.this.mDefaultBacklightForVr, KeyguardUpdateMonitor.getCurrentUser());
            } else {
                f = Settings.System.getFloatForUser(MiuiBrightnessController.this.mContext.getContentResolver(), "screen_brightness_float", MiuiBrightnessController.this.mDefaultBacklight, KeyguardUpdateMonitor.getCurrentUser());
                MiuiBrightnessController miuiBrightnessController = MiuiBrightnessController.this;
                miuiBrightnessController.mSliderAnimationDuration = Settings.System.getIntForUser(miuiBrightnessController.mContext.getContentResolver(), "slider_animation_duration", 3000, KeyguardUpdateMonitor.getCurrentUser());
            }
            Log.d("BrightnessController", "UpdateSliderRunnable: value: " + f);
            MiuiBrightnessController.this.mHandler.obtainMessage(0, Float.floatToIntBits(f), z ? 1 : 0).sendToTarget();
        }
    };
    private final CurrentUserTracker mUserTracker;
    private final IVrManager mVrManager;
    private final IVrStateCallbacks mVrStateCallbacks = new IVrStateCallbacks.Stub() {
        /* class com.android.systemui.statusbar.policy.MiuiBrightnessController.AnonymousClass5 */

        public void onVrStateChanged(boolean z) {
            MiuiBrightnessController.this.mHandler.obtainMessage(3, z ? 1 : 0, 0).sendToTarget();
        }
    };

    @Override // com.android.systemui.settings.ToggleSlider.Listener
    public void onInit(ToggleSlider toggleSlider) {
    }

    /* access modifiers changed from: private */
    public class BrightnessObserver extends ContentObserver {
        public BrightnessObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean z) {
            onChange(z, null);
        }

        public void onChange(boolean z, Uri uri) {
            if (!z) {
                if (MiuiBrightnessController.BRIGHTNESS_MODE_URI.equals(uri)) {
                    Log.d("BrightnessController", "BrightnessObserver: brightness mode change.");
                    MiuiBrightnessController.this.mBackgroundHandler.post(MiuiBrightnessController.this.mUpdateModeRunnable);
                    MiuiBrightnessController.this.mBackgroundHandler.post(MiuiBrightnessController.this.mUpdateSliderRunnable);
                } else if (MiuiBrightnessController.BRIGHTNESS_FLOAT_URI.equals(uri)) {
                    Log.d("BrightnessController", "BrightnessObserver: brightness change.");
                    MiuiBrightnessController.this.mBackgroundHandler.post(MiuiBrightnessController.this.mUpdateSliderRunnable);
                } else if (MiuiBrightnessController.BRIGHTNESS_FOR_VR_FLOAT_URI.equals(uri)) {
                    Log.d("BrightnessController", "BrightnessObserver: vr brightness change.");
                    MiuiBrightnessController.this.mBackgroundHandler.post(MiuiBrightnessController.this.mUpdateSliderRunnable);
                } else if (MiuiBrightnessController.DURATION_SLIDE_BAR_ANIMATION.equals(uri)) {
                    Log.d("BrightnessController", "BrightnessObserver: slide animation duration change.");
                    MiuiBrightnessController.this.mBackgroundHandler.post(MiuiBrightnessController.this.mUpdateSliderRunnable);
                    return;
                } else {
                    MiuiBrightnessController.this.mBackgroundHandler.post(MiuiBrightnessController.this.mUpdateModeRunnable);
                    MiuiBrightnessController.this.mBackgroundHandler.post(MiuiBrightnessController.this.mUpdateSliderRunnable);
                }
                Iterator it = MiuiBrightnessController.this.mChangeCallbacks.iterator();
                while (it.hasNext()) {
                    ((BrightnessController$BrightnessStateChangeCallback) it.next()).onBrightnessLevelChanged();
                }
            }
        }

        public void startObserving() {
            Log.d("BrightnessController", "BrightnessObserver: startObserving.");
            ContentResolver contentResolver = MiuiBrightnessController.this.mContext.getContentResolver();
            contentResolver.unregisterContentObserver(this);
            contentResolver.registerContentObserver(MiuiBrightnessController.BRIGHTNESS_MODE_URI, false, this, -1);
            contentResolver.registerContentObserver(MiuiBrightnessController.BRIGHTNESS_URI, false, this, -1);
            contentResolver.registerContentObserver(MiuiBrightnessController.BRIGHTNESS_FLOAT_URI, false, this, -1);
            contentResolver.registerContentObserver(MiuiBrightnessController.BRIGHTNESS_FOR_VR_FLOAT_URI, false, this, -1);
            contentResolver.registerContentObserver(MiuiBrightnessController.DURATION_SLIDE_BAR_ANIMATION, false, this, -1);
        }

        public void stopObserving() {
            Log.d("BrightnessController", "BrightnessObserver: stopObserving.");
            MiuiBrightnessController.this.mContext.getContentResolver().unregisterContentObserver(this);
        }
    }

    public MiuiBrightnessController(Context context, ToggleSlider toggleSlider, BroadcastDispatcher broadcastDispatcher) {
        this.mContext = context;
        this.mControl = toggleSlider;
        toggleSlider.setMax(BrightnessUtils.GAMMA_SPACE_MAX);
        this.mBackgroundHandler = new Handler((Looper) Dependency.get(Dependency.BG_LOOPER));
        this.mUserTracker = new CurrentUserTracker(broadcastDispatcher) {
            /* class com.android.systemui.statusbar.policy.MiuiBrightnessController.AnonymousClass7 */

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                MiuiBrightnessController.this.mBackgroundHandler.post(MiuiBrightnessController.this.mUpdateModeRunnable);
                MiuiBrightnessController.this.mBackgroundHandler.post(MiuiBrightnessController.this.mUpdateSliderRunnable);
            }
        };
        this.mBrightnessObserver = new BrightnessObserver(this.mHandler);
        PowerManager powerManager = (PowerManager) context.getSystemService(PowerManager.class);
        this.mMinimumBacklightInt = powerManager.getMinimumScreenBrightnessSetting();
        this.mMaximumBacklightInt = powerManager.getMaximumScreenBrightnessSetting();
        this.mMinimumBacklightForVrInt = powerManager.getMinimumScreenBrightnessForVrSetting();
        this.mMaximumBacklightForVrInt = powerManager.getMaximumScreenBrightnessForVrSetting();
        this.mMinimumBacklight = powerManager.getBrightnessConstraint(0);
        this.mMaximumBacklight = powerManager.getBrightnessConstraint(1);
        this.mDefaultBacklight = powerManager.getBrightnessConstraint(2);
        this.mMinimumBacklightForVr = powerManager.getBrightnessConstraint(5);
        this.mMaximumBacklightForVr = powerManager.getBrightnessConstraint(6);
        this.mDefaultBacklightForVr = powerManager.getBrightnessConstraint(7);
        this.mAutomaticAvailable = context.getResources().getBoolean(17891369);
        this.mDisplayManager = (DisplayManager) context.getSystemService(DisplayManager.class);
        this.mVrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
    }

    @Override // com.android.systemui.settings.ToggleSlider.Listener
    public void onChanged(ToggleSlider toggleSlider, boolean z, boolean z2, int i, boolean z3) {
        onChanged(toggleSlider, z, i, z3);
    }

    public void registerCallbacks() {
        this.mBackgroundHandler.post(this.mStartListeningRunnable);
    }

    public void unregisterCallbacks() {
        this.mBackgroundHandler.post(this.mStopListeningRunnable);
    }

    @Override // com.android.systemui.settings.ToggleSlider.Listener
    public void onStart(int i) {
        Log.d("BrightnessController", "ToggleSlider: onStart: value: " + i);
        this.mStartValue = i;
        MQSEventManagerDelegate.getInstance().reportBrightnessEvent(0, i, this.mAutomatic ? 1 : 0, "");
    }

    @Override // com.android.systemui.settings.ToggleSlider.Listener
    public void onStop(int i) {
        Log.d("BrightnessController", "ToggleSlider: onStop: value: " + i);
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).onSlideBrightnessBar(this.mStartValue, i, this.mAutomatic);
        MQSEventManagerDelegate.getInstance().reportBrightnessEvent(1, i, this.mAutomatic ? 1 : 0, "");
    }

    public void onChanged(ToggleSlider toggleSlider, final boolean z, int i, boolean z2) {
        int i2;
        int i3;
        int i4;
        float f;
        float f2;
        final String str;
        if (!this.mExternalChange) {
            ValueAnimator valueAnimator = this.mSliderAnimator;
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            if (this.mIsVrModeEnabled) {
                i2 = 498;
                i3 = this.mMinimumBacklightForVrInt;
                i4 = this.mMaximumBacklightForVrInt;
                f = this.mMinimumBacklightForVr;
                f2 = this.mMaximumBacklightForVr;
                str = "screen_brightness_for_vr_float";
            } else {
                i2 = this.mAutomatic ? 219 : 218;
                i3 = this.mMinimumBacklightInt;
                i4 = this.mMaximumBacklightInt;
                f = this.mMinimumBacklight;
                f2 = this.mMaximumBacklight;
                str = "screen_brightness_float";
            }
            final int convertGammaToLinear = BrightnessUtils.convertGammaToLinear(i, i3, i4);
            final float min = MathUtils.min(BrightnessUtils.convertGammaToLinearFloat(i, f, f2), 1.0f);
            if (z2) {
                Context context = this.mContext;
                MetricsLogger.action(context, i2, BrightnessSynchronizer.brightnessFloatToInt(context, min));
            }
            AsyncTask.execute(new Runnable() {
                /* class com.android.systemui.statusbar.policy.MiuiBrightnessController.AnonymousClass8 */

                public void run() {
                    if (z) {
                        MiuiBrightnessController.this.setBrightness(convertGammaToLinear);
                    } else if (BrightnessSynchronizer.floatEquals(min, Settings.System.getFloatForUser(MiuiBrightnessController.this.mContext.getContentResolver(), str, Float.NaN, -2))) {
                        MiuiBrightnessController.this.setBrightness(-1);
                    } else {
                        Settings.System.putFloatForUser(MiuiBrightnessController.this.mContext.getContentResolver(), str, min, -2);
                    }
                }
            });
            Iterator<BrightnessController$BrightnessStateChangeCallback> it = this.mChangeCallbacks.iterator();
            while (it.hasNext()) {
                it.next().onBrightnessLevelChanged();
            }
        }
    }

    public void checkRestrictionAndSetEnabled() {
        this.mBackgroundHandler.post(new Runnable() {
            /* class com.android.systemui.statusbar.policy.MiuiBrightnessController.AnonymousClass9 */

            public void run() {
                ((ToggleSliderView) MiuiBrightnessController.this.mControl).setEnforcedAdmin(RestrictedLockUtilsInternal.checkIfRestrictionEnforced(MiuiBrightnessController.this.mContext, "no_config_brightness", MiuiBrightnessController.this.mUserTracker.getCurrentUserId()));
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setBrightness(int i) {
        this.mDisplayManager.setTemporaryBrightness((float) i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVrMode(boolean z) {
        if (this.mIsVrModeEnabled != z) {
            this.mIsVrModeEnabled = z;
            this.mBackgroundHandler.post(this.mUpdateSliderRunnable);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSlider(float f, boolean z) {
        float f2;
        float f3;
        if (z) {
            f2 = this.mMinimumBacklightForVr;
            f3 = this.mMaximumBacklightForVr;
        } else {
            f2 = this.mMinimumBacklight;
            f3 = this.mMaximumBacklight;
        }
        if (BrightnessSynchronizer.floatEquals(f, BrightnessUtils.convertGammaToLinearFloat(this.mControl.getValue(), f2, f3))) {
            this.mControlValueInitialized = true;
        } else {
            animateSliderTo(BrightnessUtils.convertLinearToGammaFloat(f, f2, f3));
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
        ValueAnimator ofInt = ValueAnimator.ofInt(this.mControl.getValue(), i);
        this.mSliderAnimator = ofInt;
        ofInt.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.statusbar.policy.$$Lambda$MiuiBrightnessController$piACoSZooumUkA9yjO8oCJ26Pu0 */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiBrightnessController.this.lambda$animateSliderTo$0$MiuiBrightnessController(valueAnimator);
            }
        });
        ValueAnimator valueAnimator2 = this.mSliderAnimator;
        int i2 = this.mSliderAnimationDuration;
        valueAnimator2.setDuration(i2 > 0 ? (long) i2 : 3000);
        this.mSliderAnimator.start();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$animateSliderTo$0 */
    public /* synthetic */ void lambda$animateSliderTo$0$MiuiBrightnessController(ValueAnimator valueAnimator) {
        this.mExternalChange = true;
        this.mControl.setValue(((Integer) valueAnimator.getAnimatedValue()).intValue());
        this.mExternalChange = false;
    }

    @Override // com.android.systemui.Dumpable
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

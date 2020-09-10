package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.Log;
import android.view.CompositionSamplingListenerCompat;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.DarkIconDispatcher;
import com.google.android.collect.Lists;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class LightBarController implements BatteryController.BatteryStateChangeCallback, Dumpable {
    /* access modifiers changed from: private */
    public static boolean DEBUG = Constants.DEBUG;
    private final BatteryController mBatteryController;
    /* access modifiers changed from: private */
    public final ArrayList<WeakReference<DarkModeCallback>> mCallbacks = Lists.newArrayList();
    /* access modifiers changed from: private */
    public Context mContext;
    private boolean mDockedLight;
    private Rect mDockedStackBounds = new Rect();
    private int mDockedStackVisibility;
    private boolean mDriveMode;
    private FingerprintUnlockController mFingerprintUnlockController;
    private boolean mForceBlack;
    private Rect mFullScreenStackBounds = new Rect();
    private boolean mFullscreenLight;
    private int mFullscreenStackVisibility;
    private boolean mHasLightNavigationBar;
    private boolean mIconsDarkInExpanded;
    private boolean mIconsDarkInSmart;
    private final Rect mLastDockedBounds = new Rect();
    private final Rect mLastFullscreenBounds = new Rect();
    private int mLastNavigationBarMode;
    private int mLastStatusBarMode;
    private boolean mNavigationLight;
    private float mScrimAlpha;
    private boolean mScrimAlphaBelowThreshold;
    /* access modifiers changed from: private */
    public boolean mSmartDark;
    private ContentObserver mSmartDarkObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            boolean z2 = true;
            if (!MiuiSettings.System.getBoolean(LightBarController.this.mContext.getContentResolver(), "smart_dark_enable", Build.VERSION.SDK_INT > 28) || Settings.Secure.getInt(LightBarController.this.mContext.getContentResolver(), "ui_night_mode", 0) != 2) {
                z2 = false;
            }
            if (z2 != LightBarController.this.mSmartDark) {
                boolean unused = LightBarController.this.mSmartDark = z2;
                for (int i = 0; i < LightBarController.this.mCallbacks.size(); i++) {
                    DarkModeCallback darkModeCallback = (DarkModeCallback) ((WeakReference) LightBarController.this.mCallbacks.get(i)).get();
                    if (darkModeCallback != null) {
                        darkModeCallback.onDarkModeChanged(LightBarController.this.mSmartDark);
                    }
                }
            }
            if (LightBarController.DEBUG) {
                Log.d("LightBarController", "smartDark=" + LightBarController.this.mSmartDark);
            }
        }
    };
    private CompositionSamplingListenerCompat mStatuBarSamplingListener;
    private boolean mStatusBarExpanded;
    private final DarkIconDispatcher mStatusBarIconController;
    private int mSystemUiVisibility;

    private boolean isLight(int i, int i2, int i3) {
        return (i2 == 4 || i2 == 6) && ((i & i3) != 0);
    }

    public void onBatteryLevelChanged(int i, boolean z, boolean z2) {
    }

    public void onBatteryStyleChanged(int i) {
    }

    public void onExtremePowerSaveChanged(boolean z) {
    }

    public void onPowerSaveChanged(boolean z) {
    }

    public LightBarController(Context context) {
        this.mContext = context;
        this.mStatusBarIconController = (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class);
        this.mBatteryController = (BatteryController) Dependency.get(BatteryController.class);
        this.mBatteryController.addCallback(this);
        registerDarkModeObserver();
        this.mStatuBarSamplingListener = new CompositionSamplingListenerCompat(this.mContext.getApplicationContext().getMainExecutor()) {
            public void onSampleCollected(float f) {
                LightBarController.this.onStatusBarSampleCollected(f);
            }
        };
    }

    public void registerCallback(DarkModeCallback darkModeCallback) {
        if (!this.mCallbacks.contains(darkModeCallback)) {
            this.mCallbacks.add(new WeakReference(darkModeCallback));
            removeCallback((DarkModeCallback) null);
            darkModeCallback.onDarkModeChanged(this.mSmartDark);
        }
    }

    public void removeCallback(DarkModeCallback darkModeCallback) {
        this.mCallbacks.remove(darkModeCallback);
    }

    public CompositionSamplingListenerCompat getCompositionSamplingListener() {
        return this.mStatuBarSamplingListener;
    }

    public void setFingerprintUnlockController(FingerprintUnlockController fingerprintUnlockController) {
        this.mFingerprintUnlockController = fingerprintUnlockController;
    }

    public void onSystemUiVisibilityChanged(int i, int i2, int i3, Rect rect, Rect rect2, boolean z, int i4) {
        int i5 = this.mFullscreenStackVisibility;
        int i6 = ~i3;
        int i7 = (i & i3) | (i5 & i6);
        int i8 = this.mDockedStackVisibility;
        int i9 = (i2 & i3) | (i6 & i8);
        int i10 = i9 ^ i8;
        if (((i5 ^ i7) & 8192) != 0 || (i10 & 8192) != 0 || z || !this.mLastFullscreenBounds.equals(rect) || !this.mLastDockedBounds.equals(rect2)) {
            this.mFullscreenLight = isLight(i7, i4, 8192);
            this.mDockedLight = isLight(i9, i4, 8192);
            updateStatus(rect, rect2);
        }
        this.mFullscreenStackVisibility = i7;
        this.mDockedStackVisibility = i9;
        this.mLastStatusBarMode = i4;
        this.mLastFullscreenBounds.set(rect);
        this.mLastDockedBounds.set(rect2);
    }

    public void onNavigationVisibilityChanged(int i, int i2, boolean z, int i3) {
        int i4 = this.mSystemUiVisibility;
        int i5 = (i2 & i) | ((~i2) & i4);
        if (((i4 ^ i5) & 16) != 0 || z) {
            this.mHasLightNavigationBar = isLight(i, i3, 16);
            this.mNavigationLight = this.mHasLightNavigationBar && this.mScrimAlphaBelowThreshold;
        }
        this.mSystemUiVisibility = i5;
        this.mLastNavigationBarMode = i3;
    }

    private void reevaluate() {
        onSystemUiVisibilityChanged(this.mFullscreenStackVisibility, this.mDockedStackVisibility, 0, this.mLastFullscreenBounds, this.mLastDockedBounds, true, this.mLastStatusBarMode);
        onNavigationVisibilityChanged(this.mSystemUiVisibility, 0, true, this.mLastNavigationBarMode);
    }

    public void setScrimAlpha(float f) {
        this.mScrimAlpha = f;
        boolean z = this.mScrimAlphaBelowThreshold;
        this.mScrimAlphaBelowThreshold = this.mScrimAlpha < 0.1f;
        if (this.mHasLightNavigationBar && z != this.mScrimAlphaBelowThreshold) {
            reevaluate();
        }
    }

    private boolean animateChange() {
        int mode;
        FingerprintUnlockController fingerprintUnlockController = this.mFingerprintUnlockController;
        if (fingerprintUnlockController == null || (mode = fingerprintUnlockController.getMode()) == 2 || mode == 1) {
            return false;
        }
        return true;
    }

    private void updateStatus(Rect rect, Rect rect2) {
        if (this.mStatusBarExpanded) {
            this.mStatusBarIconController.getTransitionsController().setIconsDark(this.mIconsDarkInExpanded, animateChange());
            return;
        }
        this.mFullScreenStackBounds = rect;
        this.mDockedStackBounds = rect2;
        boolean z = !rect2.isEmpty();
        if (this.mDriveMode || this.mForceBlack) {
            this.mStatusBarIconController.getTransitionsController().setIconsDark(false, animateChange());
        } else if (this.mSmartDark) {
            this.mStatusBarIconController.getTransitionsController().setIconsDark(this.mIconsDarkInSmart, animateChange());
        } else if ((this.mFullscreenLight && this.mDockedLight) || (this.mFullscreenLight && !z)) {
            this.mStatusBarIconController.setIconsDarkArea((Rect) null);
            this.mStatusBarIconController.getTransitionsController().setIconsDark(true, animateChange());
        } else if ((this.mFullscreenLight || this.mDockedLight) && (this.mFullscreenLight || z)) {
            if (this.mFullscreenLight && rect.contains(rect2)) {
                int i = rect.bottom;
                int i2 = rect2.bottom;
                if (i > i2) {
                    rect.top = i2;
                } else {
                    int i3 = rect.left;
                    int i4 = rect2.left;
                    if (i3 < i4) {
                        rect.right = i4;
                    } else {
                        int i5 = rect.right;
                        int i6 = rect2.right;
                        if (i5 > i6) {
                            rect.left = i6;
                        }
                    }
                }
            }
            if (!this.mFullscreenLight) {
                rect = rect2;
            }
            if (rect.isEmpty()) {
                this.mStatusBarIconController.setIconsDarkArea((Rect) null);
            } else {
                this.mStatusBarIconController.setIconsDarkArea(rect);
            }
            this.mStatusBarIconController.getTransitionsController().setIconsDark(true, animateChange());
        } else {
            this.mStatusBarIconController.getTransitionsController().setIconsDark(false, animateChange());
        }
    }

    public void setDriveMode(boolean z) {
        this.mDriveMode = z;
        updateStatus(this.mFullScreenStackBounds, this.mDockedStackBounds);
    }

    public void setForceBlack(boolean z) {
        this.mForceBlack = z;
        updateStatus(this.mFullScreenStackBounds, this.mDockedStackBounds);
    }

    public void statusBarExpandChanged(boolean z, boolean z2) {
        if (z != this.mStatusBarExpanded || z2 != this.mIconsDarkInExpanded) {
            this.mStatusBarExpanded = z;
            this.mIconsDarkInExpanded = z2;
            updateStatus(this.mFullScreenStackBounds, this.mDockedStackBounds);
        }
    }

    public void registerDarkModeObserver() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("smart_dark_enable"), false, this.mSmartDarkObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("ui_night_mode"), false, this.mSmartDarkObserver, -1);
        this.mSmartDarkObserver.onChange(true);
    }

    public void onStatusBarSampleCollected(float f) {
        if (DEBUG) {
            Log.d("LightBarController", "onSampleCollected " + f);
        }
        if (f > 0.25f) {
            this.mIconsDarkInSmart = true;
        } else {
            this.mIconsDarkInSmart = false;
        }
        updateStatus(this.mFullScreenStackBounds, this.mDockedStackBounds);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("LightBarController: ");
        printWriter.print(" mSystemUiVisibility=0x");
        printWriter.print(Integer.toHexString(this.mSystemUiVisibility));
        printWriter.print(" mFullscreenStackVisibility=0x");
        printWriter.print(Integer.toHexString(this.mFullscreenStackVisibility));
        printWriter.print(" mDockedStackVisibility=0x");
        printWriter.println(Integer.toHexString(this.mDockedStackVisibility));
        printWriter.print(" mFullscreenLight=");
        printWriter.print(this.mFullscreenLight);
        printWriter.print(" mDockedLight=");
        printWriter.println(this.mDockedLight);
        printWriter.print(" mLastFullscreenBounds=");
        printWriter.print(this.mLastFullscreenBounds);
        printWriter.print(" mLastDockedBounds=");
        printWriter.println(this.mLastDockedBounds);
        printWriter.print(" mNavigationLight=");
        printWriter.print(this.mNavigationLight);
        printWriter.print(" mHasLightNavigationBar=");
        printWriter.println(this.mHasLightNavigationBar);
        printWriter.print(" mLastStatusBarMode=");
        printWriter.print(this.mLastStatusBarMode);
        printWriter.print(" mLastNavigationBarMode=");
        printWriter.println(this.mLastNavigationBarMode);
        printWriter.print(" mScrimAlpha=");
        printWriter.print(this.mScrimAlpha);
        printWriter.print(" mScrimAlphaBelowThreshold=");
        printWriter.println(this.mScrimAlphaBelowThreshold);
        printWriter.println();
        printWriter.println(" StatusBarTransitionsController:");
        this.mStatusBarIconController.getTransitionsController().dump(fileDescriptor, printWriter, strArr);
        printWriter.println();
    }
}

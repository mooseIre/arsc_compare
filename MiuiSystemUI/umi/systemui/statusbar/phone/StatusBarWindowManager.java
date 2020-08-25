package com.android.systemui.statusbar.phone;

import android.app.ActivityManagerCompat;
import android.content.Context;
import android.content.res.Resources;
import android.os.Binder;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.view.SurfaceControlCompat;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerCompat;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.RemoteInputController;
import com.xiaomi.stat.d.i;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class StatusBarWindowManager implements RemoteInputController.Callback, Dumpable {
    private int mBarHeight;
    private List<BlurRatioChangedListener> mBlurRatioListeners = new ArrayList();
    private final Context mContext;
    private final State mCurrentState = new State();
    private boolean mHasTopUi;
    private boolean mHasTopUiChanged;
    private final boolean mKeyguardScreenRotation;
    private OtherwisedCollapsedListener mListener;
    private WindowManager.LayoutParams mLp;
    private WindowManager.LayoutParams mLpChanged;
    private float mRestoredBlurRatio;
    private final float mScreenBrightnessDoze;
    private ViewGroup mStatusBarView;
    private int mUserActivityTime = i.a;
    private final WindowManager mWindowManager;

    public interface BlurRatioChangedListener {
        void onBlurRatioChanged(float f);
    }

    public interface OtherwisedCollapsedListener {
        void setWouldOtherwiseCollapse(boolean z);
    }

    public void onRemoteInputSent(NotificationData.Entry entry) {
    }

    public StatusBarWindowManager(Context context) {
        this.mContext = context;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mKeyguardScreenRotation = shouldEnableKeyguardScreenRotation();
        this.mScreenBrightnessDoze = ((float) this.mContext.getResources().getInteger(17694890)) / 255.0f;
    }

    private boolean shouldEnableKeyguardScreenRotation() {
        Resources resources = this.mContext.getResources();
        if (SystemProperties.getBoolean("lockscreen.rot_override", false) || resources.getBoolean(R.bool.config_enableLockScreenRotation)) {
            return true;
        }
        return false;
    }

    public void add(ViewGroup viewGroup, int i) {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, i, 2000, -2138832824, -3);
        this.mLp = layoutParams;
        layoutParams.token = new Binder();
        WindowManager.LayoutParams layoutParams2 = this.mLp;
        layoutParams2.flags |= 16777216;
        layoutParams2.gravity = 48;
        layoutParams2.softInputMode = 16;
        layoutParams2.setTitle("StatusBar");
        this.mLp.packageName = this.mContext.getPackageName();
        WindowManagerCompat.setLayoutInDisplayCutoutMode(this.mLp, 1);
        this.mStatusBarView = viewGroup;
        this.mBarHeight = i;
        this.mWindowManager.addView(viewGroup, this.mLp);
        WindowManager.LayoutParams layoutParams3 = new WindowManager.LayoutParams();
        this.mLpChanged = layoutParams3;
        layoutParams3.copyFrom(this.mLp);
    }

    public ViewGroup getStatusBarView() {
        return this.mStatusBarView;
    }

    private void applyKeyguardFlags(State state) {
        Class cls = MiuiKeyguardWallpaperController.class;
        if (state.keyguardShowing) {
            this.mLpChanged.privateFlags |= 1024;
        } else {
            this.mLpChanged.privateFlags &= -1025;
        }
        boolean isLegacyKeyguardWallpaper = ((MiuiKeyguardWallpaperController) Dependency.get(cls)).isLegacyKeyguardWallpaper();
        boolean isWallpaperSupportsAmbientMode = ((MiuiKeyguardWallpaperController) Dependency.get(cls)).isWallpaperSupportsAmbientMode();
        if (!state.keyguardShowing || ((state.dozing || isLegacyKeyguardWallpaper) && !isWallpaperSupportsAmbientMode)) {
            this.mLpChanged.flags &= -1048577;
        } else {
            this.mLpChanged.flags |= 1048576;
        }
        if ((state.isKeyguardShowingAndNotOccluded() || state.keyguardFadingAway) && state.keygaurdTransparent) {
            WindowManager.LayoutParams layoutParams = this.mLpChanged;
            int i = layoutParams.flags & -1048577;
            layoutParams.flags = i;
            layoutParams.alpha = 0.0f;
            layoutParams.flags = i | 16;
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                MiuiGxzwManager.getInstance().nofifySurfaceFlinger(false);
            }
        } else {
            WindowManager.LayoutParams layoutParams2 = this.mLpChanged;
            layoutParams2.alpha = 1.0f;
            layoutParams2.flags &= -17;
            this.mCurrentState.keygaurdTransparent = false;
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                MiuiGxzwManager.getInstance().nofifySurfaceFlinger(true);
            }
        }
        if (this.mCurrentState.bouncerShowing) {
            this.mLpChanged.flags |= 8192;
            return;
        }
        this.mLpChanged.flags &= -8193;
    }

    private void adjustScreenOrientation(State state) {
        if (!state.isKeyguardShowingAndNotOccluded() && !state.bouncerShowing) {
            this.mLpChanged.screenOrientation = -1;
        } else if (this.mKeyguardScreenRotation) {
            this.mLpChanged.screenOrientation = 2;
        } else {
            this.mLpChanged.screenOrientation = 5;
        }
    }

    private void applyFocusableFlag(State state) {
        boolean z = state.statusBarFocusable && state.panelExpanded;
        if ((state.bouncerShowing && (state.keyguardOccluded || state.keyguardNeedsInput)) || ((StatusBar.ENABLE_REMOTE_INPUT && state.remoteInputActive) || state.bubbleExpanded)) {
            WindowManager.LayoutParams layoutParams = this.mLpChanged;
            int i = layoutParams.flags & -9;
            layoutParams.flags = i;
            layoutParams.flags = i & -131073;
        } else if (state.isKeyguardShowingAndNotOccluded() || z) {
            WindowManager.LayoutParams layoutParams2 = this.mLpChanged;
            int i2 = layoutParams2.flags & -9;
            layoutParams2.flags = i2;
            layoutParams2.flags = i2 | 131072;
        } else {
            WindowManager.LayoutParams layoutParams3 = this.mLpChanged;
            int i3 = layoutParams3.flags | 8;
            layoutParams3.flags = i3;
            layoutParams3.flags = i3 & -131073;
        }
        this.mLpChanged.softInputMode = 16;
    }

    private void applyExpandedFlag(State state) {
        WindowManagerCompat.applyExpandedFlag(state.panelExpanded || state.isKeyguardShowingAndNotOccluded() || state.bouncerShowing || (StatusBar.ENABLE_REMOTE_INPUT && state.remoteInputActive), this.mLpChanged);
    }

    private void applyHeight(State state) {
        boolean isExpanded = isExpanded(state);
        if (state.forcePluginOpen) {
            this.mListener.setWouldOtherwiseCollapse(isExpanded);
            isExpanded = true;
        }
        if (isExpanded) {
            this.mLpChanged.height = -1;
            return;
        }
        this.mLpChanged.height = this.mBarHeight;
    }

    private boolean isExpanded(State state) {
        return !state.forceCollapsed && (state.isKeyguardShowingAndNotOccluded() || state.panelVisible || state.keyguardFadingAway || state.bouncerShowing || state.headsUpShowing || state.bubblesShowing);
    }

    private void applyFitsSystemWindows(State state) {
        boolean z = !state.isKeyguardShowingAndNotOccluded();
        if (this.mStatusBarView.getFitsSystemWindows() != z) {
            this.mStatusBarView.setFitsSystemWindows(z);
            this.mStatusBarView.requestApplyInsets();
        }
    }

    private void applyUserActivityTimeout(State state) {
        if (!state.isKeyguardShowingAndNotOccluded() || state.statusBarState != 1 || state.qsExpanded) {
            this.mLpChanged.userActivityTimeout = -1;
            return;
        }
        this.mLpChanged.userActivityTimeout = (long) this.mUserActivityTime;
    }

    private void applyInputFeatures(State state) {
        if (!state.isKeyguardShowingAndNotOccluded() || state.statusBarState != 1 || state.qsExpanded || state.forceUserActivity) {
            this.mLpChanged.inputFeatures &= -5;
            return;
        }
        this.mLpChanged.inputFeatures |= 4;
    }

    private void apply(State state) {
        applyKeyguardFlags(state);
        applyForceStatusBarVisibleFlag(state);
        applyFocusableFlag(state);
        applyExpandedFlag(state);
        adjustScreenOrientation(state);
        applyHeight(state);
        applyUserActivityTimeout(state);
        applyInputFeatures(state);
        applyFitsSystemWindows(state);
        applyModalFlag(state);
        applyBrightness(state);
        applyHasTopUi(state);
        applyNotTouchable(state);
        applyBlurRatio(state);
        applySleepToken(state);
        if (this.mLp.copyFrom(this.mLpChanged) != 0) {
            this.mWindowManager.updateViewLayout(this.mStatusBarView, this.mLp);
        }
        boolean z = this.mHasTopUi;
        boolean z2 = this.mHasTopUiChanged;
        if (z != z2) {
            try {
                ActivityManagerCompat.setHasTopUi(z2);
            } catch (RemoteException e) {
                Log.e("StatusBarWindowManager", "Failed to call setHasTopUi", e);
            }
            this.mHasTopUi = this.mHasTopUiChanged;
        }
    }

    private void applyForceStatusBarVisibleFlag(State state) {
        boolean z = Build.VERSION.SDK_INT > 29 && state.panelExpanded;
        if (state.forceStatusBarVisible || z) {
            this.mLpChanged.privateFlags |= 4096;
            return;
        }
        this.mLpChanged.privateFlags &= -4097;
    }

    private void applyModalFlag(State state) {
        if (state.headsUpShowing) {
            this.mLpChanged.flags |= 32;
            return;
        }
        this.mLpChanged.flags &= -33;
    }

    private void applyBrightness(State state) {
        if (state.forceDozeBrightness) {
            this.mLpChanged.screenBrightness = this.mScreenBrightnessDoze;
            return;
        }
        this.mLpChanged.screenBrightness = -1.0f;
    }

    private void applyHasTopUi(State state) {
        this.mHasTopUiChanged = isExpanded(state);
    }

    private void applyNotTouchable(State state) {
        if (state.notTouchable) {
            this.mLpChanged.flags |= 16;
            return;
        }
        this.mLpChanged.flags &= -17;
    }

    private void applyBlurRatio(State state) {
        SurfaceControlCompat.setBlur(this.mLpChanged, this.mStatusBarView.getViewRootImpl(), state.blurRatio, 0);
        for (BlurRatioChangedListener onBlurRatioChanged : this.mBlurRatioListeners) {
            onBlurRatioChanged.onBlurRatioChanged(state.blurRatio);
        }
    }

    private void applySleepToken(State state) {
        WindowManagerCompat.applySleepToken(state.dozing, this.mLpChanged);
    }

    public void toggleBlurBackgroundByBrightnessMirror(boolean z) {
        if (!z) {
            float f = this.mCurrentState.blurRatio;
            if (f > 0.0f) {
                this.mRestoredBlurRatio = f;
                setBlurRatio(0.0f);
            }
        } else if (this.mCurrentState.blurRatio == 0.0f) {
            setBlurRatio(this.mRestoredBlurRatio);
        }
    }

    public void setUserActivityTime(int i) {
        if (this.mUserActivityTime != i) {
            this.mUserActivityTime = i;
            apply(this.mCurrentState);
        }
    }

    public void setKeyguardShowing(boolean z) {
        State state = this.mCurrentState;
        state.keyguardShowing = z;
        apply(state);
    }

    public void setKeyguardOccluded(boolean z) {
        State state = this.mCurrentState;
        state.keyguardOccluded = z;
        apply(state);
    }

    public void setKeygaurdTransparent(boolean z) {
        State state = this.mCurrentState;
        state.keygaurdTransparent = z;
        apply(state);
    }

    public void setKeyguardNeedsInput(boolean z) {
        State state = this.mCurrentState;
        state.keyguardNeedsInput = z;
        apply(state);
    }

    public void setPanelVisible(boolean z) {
        State state = this.mCurrentState;
        state.panelVisible = z;
        state.statusBarFocusable = z;
        apply(state);
    }

    public void setStatusBarFocusable(boolean z) {
        State state = this.mCurrentState;
        state.statusBarFocusable = z;
        apply(state);
    }

    public void setBouncerShowing(boolean z) {
        State state = this.mCurrentState;
        state.bouncerShowing = z;
        apply(state);
    }

    public void setBackdropShowing(boolean z) {
        State state = this.mCurrentState;
        state.backdropShowing = z;
        apply(state);
    }

    public void setKeyguardFadingAway(boolean z) {
        State state = this.mCurrentState;
        state.keyguardFadingAway = z;
        apply(state);
    }

    public void setQsExpanded(boolean z) {
        State state = this.mCurrentState;
        state.qsExpanded = z;
        apply(state);
    }

    public void setHeadsUpShowing(boolean z) {
        State state = this.mCurrentState;
        state.headsUpShowing = z;
        apply(state);
    }

    public void setStatusBarState(int i) {
        State state = this.mCurrentState;
        state.statusBarState = i;
        apply(state);
    }

    public void setBlurRatio(float f) {
        State state = this.mCurrentState;
        state.blurRatio = f;
        apply(state);
    }

    public void setForceStatusBarVisible(boolean z) {
        State state = this.mCurrentState;
        state.forceStatusBarVisible = z;
        apply(state);
    }

    public void setForceWindowCollapsed(boolean z) {
        State state = this.mCurrentState;
        state.forceCollapsed = z;
        apply(state);
    }

    public void setPanelExpanded(boolean z) {
        State state = this.mCurrentState;
        state.panelExpanded = z;
        apply(state);
    }

    public void onRemoteInputActive(boolean z) {
        State state = this.mCurrentState;
        state.remoteInputActive = z;
        apply(state);
    }

    public void setForceDozeBrightness(boolean z) {
        State state = this.mCurrentState;
        state.forceDozeBrightness = z;
        apply(state);
    }

    public void setDozing(boolean z) {
        State state = this.mCurrentState;
        state.dozing = z;
        apply(state);
    }

    public void setBarHeight(int i) {
        this.mBarHeight = i;
        apply(this.mCurrentState);
    }

    public void setForcePluginOpen(boolean z) {
        State state = this.mCurrentState;
        state.forcePluginOpen = z;
        apply(state);
    }

    public void setNotTouchable(boolean z) {
        State state = this.mCurrentState;
        state.notTouchable = z;
        apply(state);
    }

    public void setBubblesShowing(boolean z) {
        State state = this.mCurrentState;
        state.bubblesShowing = z;
        apply(state);
    }

    public boolean getBubblesShowing() {
        return this.mCurrentState.bubblesShowing;
    }

    public void setBubbleExpanded(boolean z) {
        State state = this.mCurrentState;
        state.bubbleExpanded = z;
        apply(state);
    }

    public boolean getPanelExpanded() {
        return this.mCurrentState.panelExpanded;
    }

    public void setStateListener(OtherwisedCollapsedListener otherwisedCollapsedListener) {
        this.mListener = otherwisedCollapsedListener;
    }

    public void addBlurRatioListener(BlurRatioChangedListener blurRatioChangedListener) {
        this.mBlurRatioListeners.add(blurRatioChangedListener);
    }

    public void removeBlurRatioListener(BlurRatioChangedListener blurRatioChangedListener) {
        this.mBlurRatioListeners.remove(blurRatioChangedListener);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("StatusBarWindowManager state:");
        printWriter.println(this.mCurrentState);
    }

    private static class State {
        boolean backdropShowing;
        float blurRatio;
        boolean bouncerShowing;
        boolean bubbleExpanded;
        boolean bubblesShowing;
        boolean dozing;
        boolean forceCollapsed;
        boolean forceDozeBrightness;
        boolean forcePluginOpen;
        boolean forceStatusBarVisible;
        boolean forceUserActivity;
        boolean headsUpShowing;
        boolean keygaurdTransparent;
        boolean keyguardFadingAway;
        boolean keyguardNeedsInput;
        boolean keyguardOccluded;
        boolean keyguardShowing;
        boolean notTouchable;
        boolean panelExpanded;
        boolean panelVisible;
        boolean qsExpanded;
        boolean remoteInputActive;
        boolean statusBarFocusable;
        int statusBarState;

        private State() {
        }

        /* access modifiers changed from: private */
        public boolean isKeyguardShowingAndNotOccluded() {
            return this.keyguardShowing && !this.keyguardOccluded;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Window State {");
            sb.append("\n");
            for (Field field : State.class.getDeclaredFields()) {
                sb.append("  ");
                try {
                    sb.append(field.getName());
                    sb.append(": ");
                    sb.append(field.get(this));
                } catch (IllegalAccessException unused) {
                }
                sb.append("\n");
            }
            sb.append("}");
            return sb.toString();
        }
    }
}

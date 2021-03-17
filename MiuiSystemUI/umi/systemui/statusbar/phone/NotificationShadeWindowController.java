package com.android.systemui.statusbar.phone;

import android.app.IActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Binder;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.WallpaperAuthorityUtils;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0016R$integer;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dumpable;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.google.android.collect.Lists;
import com.miui.systemui.util.BlurUtil;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class NotificationShadeWindowController implements RemoteInputController.Callback, Dumpable, ConfigurationController.ConfigurationListener {
    private final IActivityManager mActivityManager;
    private final ArrayList<WeakReference<StatusBarWindowCallback>> mCallbacks = Lists.newArrayList();
    private final SysuiColorExtractor mColorExtractor;
    private Configuration mConfiguration;
    private final Context mContext;
    private final State mCurrentState = new State();
    private final DozeParameters mDozeParameters;
    private ForcePluginOpenListener mForcePluginOpenListener;
    private boolean mHasTopUi;
    private boolean mHasTopUiChanged;
    private final KeyguardBypassController mKeyguardBypassController;
    private final Display.Mode mKeyguardDisplayMode;
    private boolean mKeyguardScreenRotation;
    private final KeyguardViewMediator mKeyguardViewMediator;
    private OtherwisedCollapsedListener mListener;
    private WindowManager.LayoutParams mLp;
    private final WindowManager.LayoutParams mLpChanged;
    private ViewGroup mNotificationShadeView;
    private boolean mOnPcMode;
    private float mScreenBrightnessDoze;
    private Consumer<Integer> mScrimsVisibilityListener;
    private final StatusBarStateController.StateListener mStateListener;
    private int mUserActivityTime;
    private final WindowManager mWindowManager;

    public interface ForcePluginOpenListener {
        void onChange(boolean z);
    }

    public interface OtherwisedCollapsedListener {
        void setWouldOtherwiseCollapse(boolean z);
    }

    public NotificationShadeWindowController(Context context, WindowManager windowManager, IActivityManager iActivityManager, DozeParameters dozeParameters, StatusBarStateController statusBarStateController, ConfigurationController configurationController, KeyguardViewMediator keyguardViewMediator, KeyguardBypassController keyguardBypassController, SysuiColorExtractor sysuiColorExtractor, DumpManager dumpManager) {
        boolean z = false;
        this.mOnPcMode = false;
        this.mConfiguration = new Configuration();
        this.mUserActivityTime = 10000;
        this.mStateListener = new StatusBarStateController.StateListener() {
            /* class com.android.systemui.statusbar.phone.NotificationShadeWindowController.AnonymousClass1 */

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                NotificationShadeWindowController.this.setStatusBarState(i);
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onDozingChanged(boolean z) {
                NotificationShadeWindowController.this.setDozing(z);
            }
        };
        this.mContext = context;
        this.mWindowManager = windowManager;
        this.mActivityManager = iActivityManager;
        this.mKeyguardScreenRotation = shouldEnableKeyguardScreenRotation();
        this.mDozeParameters = dozeParameters;
        this.mScreenBrightnessDoze = dozeParameters.getScreenBrightnessDoze();
        this.mLpChanged = new WindowManager.LayoutParams();
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mKeyguardBypassController = keyguardBypassController;
        this.mColorExtractor = sysuiColorExtractor;
        dumpManager.registerDumpable(NotificationShadeWindowController.class.getName(), this);
        context.getResources().getInteger(C0016R$integer.config_lockScreenDisplayTimeout);
        ((SysuiStatusBarStateController) statusBarStateController).addCallback(this.mStateListener, 1);
        configurationController.addCallback(this);
        this.mKeyguardDisplayMode = (Display.Mode) Arrays.stream(context.getDisplay().getSupportedModes()).filter(new Predicate(context.getResources().getInteger(C0016R$integer.config_keyguardRefreshRate), context.getDisplay().getMode()) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationShadeWindowController$eZhKF4qxAkYFnq9gGQ6_QkkGic4 */
            public final /* synthetic */ int f$0;
            public final /* synthetic */ Display.Mode f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Predicate
            public final boolean test(Object obj) {
                return NotificationShadeWindowController.lambda$new$0(this.f$0, this.f$1, (Display.Mode) obj);
            }
        }).findFirst().orElse(null);
        this.mOnPcMode = (context.getResources().getConfiguration().uiMode & 8192) != 0 ? true : z;
    }

    static /* synthetic */ boolean lambda$new$0(int i, Display.Mode mode, Display.Mode mode2) {
        return ((int) mode2.getRefreshRate()) == i && mode2.getPhysicalWidth() == mode.getPhysicalWidth() && mode2.getPhysicalHeight() == mode.getPhysicalHeight();
    }

    public void registerCallback(StatusBarWindowCallback statusBarWindowCallback) {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            if (this.mCallbacks.get(i).get() == statusBarWindowCallback) {
                return;
            }
        }
        this.mCallbacks.add(new WeakReference<>(statusBarWindowCallback));
    }

    public void setScrimsVisibilityListener(Consumer<Integer> consumer) {
        if (consumer != null && this.mScrimsVisibilityListener != consumer) {
            this.mScrimsVisibilityListener = consumer;
        }
    }

    private boolean shouldEnableKeyguardScreenRotation() {
        Resources resources = this.mContext.getResources();
        if (SystemProperties.getBoolean("lockscreen.rot_override", false) || resources.getBoolean(C0010R$bool.config_enableLockScreenRotation)) {
            return true;
        }
        return false;
    }

    public void attach() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, -1, 2040, -2138832824, -3);
        this.mLp = layoutParams;
        layoutParams.token = new Binder();
        WindowManager.LayoutParams layoutParams2 = this.mLp;
        layoutParams2.gravity = 48;
        layoutParams2.setFitInsetsTypes(0);
        WindowManager.LayoutParams layoutParams3 = this.mLp;
        layoutParams3.softInputMode = 16;
        layoutParams3.setTitle("NotificationShade");
        this.mLp.packageName = this.mContext.getPackageName();
        WindowManager.LayoutParams layoutParams4 = this.mLp;
        layoutParams4.layoutInDisplayCutoutMode = 3;
        layoutParams4.privateFlags |= 134217728;
        layoutParams4.insetsFlags.behavior = 2;
        this.mWindowManager.addView(this.mNotificationShadeView, layoutParams4);
        this.mLpChanged.copyFrom(this.mLp);
        onThemeChanged();
        if (this.mKeyguardViewMediator.isShowingAndNotOccluded()) {
            setKeyguardShowing(true);
        }
    }

    public void setNotificationShadeView(ViewGroup viewGroup) {
        this.mNotificationShadeView = viewGroup;
    }

    public void setDozeScreenBrightness(int i) {
        this.mScreenBrightnessDoze = ((float) i) / 255.0f;
    }

    private void setKeyguardDark(boolean z) {
        int systemUiVisibility = this.mNotificationShadeView.getSystemUiVisibility();
        this.mNotificationShadeView.setSystemUiVisibility(z ? systemUiVisibility | 16 | 8192 : systemUiVisibility & -17 & -8193);
    }

    private void applyKeyguardFlags(State state) {
        boolean isDefaultLockScreenTheme = MiuiKeyguardUtils.isDefaultLockScreenTheme();
        boolean isThemeLockLiveWallpaper = WallpaperAuthorityUtils.isThemeLockLiveWallpaper();
        if (!state.mKeyguardShowing || (!isDefaultLockScreenTheme && !state.mWallpaperSupportsAmbientMode)) {
            this.mLpChanged.flags &= -1048577;
        } else {
            this.mLpChanged.flags |= 1048576;
        }
        if (state.mDozing) {
            this.mLpChanged.privateFlags |= 524288;
        } else {
            this.mLpChanged.privateFlags &= -524289;
        }
        if (this.mKeyguardDisplayMode != null) {
            boolean z = this.mKeyguardBypassController.getBypassEnabled() && state.mStatusBarState == 1 && !state.mKeyguardFadingAway && !state.mKeyguardGoingAway;
            if (state.mDozing || z) {
                this.mLpChanged.preferredDisplayModeId = this.mKeyguardDisplayMode.getModeId();
            } else {
                this.mLpChanged.preferredDisplayModeId = 0;
            }
            Trace.setCounter("display_mode_id", (long) this.mLpChanged.preferredDisplayModeId);
        }
        if ((state.isKeyguardShowingAndNotOccluded() || state.mKeyguardFadingAway) && state.keygaurdTransparent) {
            if (!isThemeLockLiveWallpaper) {
                this.mLpChanged.flags &= -1048577;
            }
            WindowManager.LayoutParams layoutParams = this.mLpChanged;
            layoutParams.alpha = 0.0f;
            layoutParams.flags |= 16;
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                MiuiGxzwManager.getInstance().nofifySurfaceFlinger(false);
            }
        } else {
            WindowManager.LayoutParams layoutParams2 = this.mLpChanged;
            layoutParams2.alpha = 1.0f;
            this.mCurrentState.keygaurdTransparent = false;
            layoutParams2.flags &= -17;
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                MiuiGxzwManager.getInstance().nofifySurfaceFlinger(true);
            }
        }
        if (this.mCurrentState.mBouncerShowing) {
            this.mLpChanged.flags |= 8192;
            return;
        }
        this.mLpChanged.flags &= -8193;
    }

    private void adjustScreenOrientation(State state) {
        if (!state.isKeyguardShowingAndNotOccluded() && !state.mDozing && !state.mBouncerShowing) {
            this.mLpChanged.screenOrientation = -1;
        } else if (this.mOnPcMode) {
            this.mLpChanged.screenOrientation = 0;
        } else if (this.mKeyguardScreenRotation) {
            this.mLpChanged.screenOrientation = 2;
        } else {
            this.mLpChanged.screenOrientation = 5;
        }
    }

    private void applyFocusableFlag(State state) {
        boolean z = state.mNotificationShadeFocusable && state.mPanelExpanded;
        if ((state.mBouncerShowing && (state.mKeyguardOccluded || state.mKeyguardNeedsInput)) || (NotificationRemoteInputManager.ENABLE_REMOTE_INPUT && state.mRemoteInputActive)) {
            WindowManager.LayoutParams layoutParams = this.mLpChanged;
            int i = layoutParams.flags & -9;
            layoutParams.flags = i;
            layoutParams.flags = i & -131073;
        } else if (state.isKeyguardShowingAndNotOccluded() || z) {
            this.mLpChanged.flags &= -9;
            if (!state.mKeyguardNeedsInput || !state.isKeyguardShowingAndNotOccluded()) {
                this.mLpChanged.flags |= 131072;
            } else {
                this.mLpChanged.flags &= -131073;
            }
        } else {
            WindowManager.LayoutParams layoutParams2 = this.mLpChanged;
            int i2 = layoutParams2.flags | 8;
            layoutParams2.flags = i2;
            layoutParams2.flags = i2 & -131073;
        }
        this.mLpChanged.softInputMode = 16;
    }

    private void applyForceShowNavigationFlag(State state) {
        if (state.mPanelExpanded || state.mBouncerShowing || (NotificationRemoteInputManager.ENABLE_REMOTE_INPUT && state.mRemoteInputActive)) {
            this.mLpChanged.privateFlags |= 8388608;
            return;
        }
        this.mLpChanged.privateFlags &= -8388609;
    }

    private void applyVisibility(State state) {
        boolean isExpanded = isExpanded(state);
        if (state.mForcePluginOpen) {
            OtherwisedCollapsedListener otherwisedCollapsedListener = this.mListener;
            if (otherwisedCollapsedListener != null) {
                otherwisedCollapsedListener.setWouldOtherwiseCollapse(isExpanded);
            }
            isExpanded = true;
        }
        if (isExpanded) {
            this.mNotificationShadeView.setVisibility(0);
        } else {
            this.mNotificationShadeView.setVisibility(4);
        }
    }

    private boolean isExpanded(State state) {
        return (!state.mForceCollapsed && (state.isKeyguardShowingAndNotOccluded() || state.mPanelVisible || state.mKeyguardFadingAway || state.mBouncerShowing || state.mHeadsUpShowing || state.mScrimsVisibility != 0)) || state.mBackgroundBlurRadius > 0 || state.mLaunchingActivity;
    }

    private void applyFitsSystemWindows(State state) {
        boolean z = !state.isKeyguardShowingAndNotOccluded();
        ViewGroup viewGroup = this.mNotificationShadeView;
        if (viewGroup != null && viewGroup.getFitsSystemWindows() != z) {
            this.mNotificationShadeView.setFitsSystemWindows(z);
            this.mNotificationShadeView.requestApplyInsets();
        }
    }

    private void applyUserActivityTimeout(State state) {
        if (!state.isKeyguardShowingAndNotOccluded() || state.mStatusBarState != 1 || state.mQsExpanded) {
            this.mLpChanged.userActivityTimeout = -1;
            return;
        }
        this.mLpChanged.userActivityTimeout = (long) this.mUserActivityTime;
    }

    private void applyInputFeatures(State state) {
        if (!state.isKeyguardShowingAndNotOccluded() || state.mStatusBarState != 1 || state.mQsExpanded || state.mForceUserActivity) {
            this.mLpChanged.inputFeatures &= -5;
            return;
        }
        this.mLpChanged.inputFeatures |= 4;
    }

    private void applyStatusBarColorSpaceAgnosticFlag(State state) {
        if (!isExpanded(state)) {
            this.mLpChanged.privateFlags |= 16777216;
            return;
        }
        this.mLpChanged.privateFlags &= -16777217;
    }

    private void apply(State state) {
        applyKeyguardFlags(state);
        applyFocusableFlag(state);
        applyForceShowNavigationFlag(state);
        adjustScreenOrientation(state);
        applyVisibility(state);
        applyUserActivityTimeout(state);
        applyInputFeatures(state);
        applyFitsSystemWindows(state);
        applyModalFlag(state);
        applyBrightness(state);
        applyHasTopUi(state);
        applyNotTouchable(state);
        applyStatusBarColorSpaceAgnosticFlag(state);
        applyBlurRatio(state);
        WindowManager.LayoutParams layoutParams = this.mLp;
        if (!(layoutParams == null || layoutParams.copyFrom(this.mLpChanged) == 0)) {
            this.mWindowManager.updateViewLayout(this.mNotificationShadeView, this.mLp);
        }
        if (this.mHasTopUi != this.mHasTopUiChanged) {
            DejankUtils.whitelistIpcs(new Runnable() {
                /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationShadeWindowController$cWnla7q4SPNKNSlx9hB8mcjvaHk */

                public final void run() {
                    NotificationShadeWindowController.this.lambda$apply$1$NotificationShadeWindowController();
                }
            });
        }
        notifyStateChangedCallbacks();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$apply$1 */
    public /* synthetic */ void lambda$apply$1$NotificationShadeWindowController() {
        try {
            this.mActivityManager.setHasTopUi(this.mHasTopUiChanged);
        } catch (RemoteException e) {
            Log.e("NotificationShadeWindowController", "Failed to call setHasTopUi", e);
        }
        this.mHasTopUi = this.mHasTopUiChanged;
    }

    public void notifyStateChangedCallbacks() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            StatusBarWindowCallback statusBarWindowCallback = this.mCallbacks.get(i).get();
            if (statusBarWindowCallback != null) {
                State state = this.mCurrentState;
                statusBarWindowCallback.onStateChanged(state.mKeyguardShowing, state.mKeyguardOccluded, state.mBouncerShowing);
            }
        }
    }

    private void applyModalFlag(State state) {
        if (state.mHeadsUpShowing) {
            this.mLpChanged.flags |= 32;
            return;
        }
        this.mLpChanged.flags &= -33;
    }

    private void applyBrightness(State state) {
        if (state.mForceDozeBrightness) {
            this.mLpChanged.screenBrightness = this.mScreenBrightnessDoze;
            return;
        }
        this.mLpChanged.screenBrightness = -1.0f;
    }

    private void applyHasTopUi(State state) {
        this.mHasTopUiChanged = state.mForceHasTopUi || isExpanded(state);
    }

    private void applyNotTouchable(State state) {
        if (state.mNotTouchable) {
            this.mLpChanged.flags |= 16;
            return;
        }
        this.mLpChanged.flags &= -17;
    }

    public void setKeyguardShowing(boolean z) {
        State state = this.mCurrentState;
        state.mKeyguardShowing = z;
        apply(state);
    }

    public void setKeyguardOccluded(boolean z) {
        State state = this.mCurrentState;
        state.mKeyguardOccluded = z;
        apply(state);
    }

    public void setKeyguardNeedsInput(boolean z) {
        State state = this.mCurrentState;
        state.mKeyguardNeedsInput = z;
        apply(state);
    }

    public void setKeygaurdTransparent(boolean z) {
        State state = this.mCurrentState;
        state.keygaurdTransparent = z;
        apply(state);
    }

    public void setBlurRatio(float f) {
        State state = this.mCurrentState;
        state.mBlurRatio = f;
        apply(state);
    }

    private void applyBlurRatio(State state) {
        BlurUtil.setBlurWithWindowManager(this.mNotificationShadeView.getViewRootImpl(), state.mBlurRatio, 0, this.mLpChanged);
    }

    public void setUserActivityTime(int i) {
        if (this.mUserActivityTime != i) {
            this.mUserActivityTime = i;
            apply(this.mCurrentState);
        }
    }

    public void setPanelVisible(boolean z) {
        State state = this.mCurrentState;
        state.mPanelVisible = z;
        state.mNotificationShadeFocusable = z;
        apply(state);
    }

    public void setNotificationShadeFocusable(boolean z) {
        State state = this.mCurrentState;
        state.mNotificationShadeFocusable = z;
        apply(state);
    }

    public void setBouncerShowing(boolean z) {
        State state = this.mCurrentState;
        state.mBouncerShowing = z;
        apply(state);
    }

    public void setBackdropShowing(boolean z) {
        State state = this.mCurrentState;
        state.mBackdropShowing = z;
        apply(state);
    }

    public void setKeyguardFadingAway(boolean z) {
        State state = this.mCurrentState;
        state.mKeyguardFadingAway = z;
        apply(state);
    }

    public void setQsExpanded(boolean z) {
        State state = this.mCurrentState;
        state.mQsExpanded = z;
        apply(state);
    }

    /* access modifiers changed from: package-private */
    public void setLaunchingActivity(boolean z) {
        State state = this.mCurrentState;
        state.mLaunchingActivity = z;
        apply(state);
    }

    public void setScrimsVisibility(int i) {
        State state = this.mCurrentState;
        state.mScrimsVisibility = i;
        apply(state);
        this.mScrimsVisibilityListener.accept(Integer.valueOf(i));
    }

    public void setBackgroundBlurRadius(int i) {
        State state = this.mCurrentState;
        if (state.mBackgroundBlurRadius != i) {
            state.mBackgroundBlurRadius = i;
            apply(state);
        }
    }

    public void setHeadsUpShowing(boolean z) {
        State state = this.mCurrentState;
        state.mHeadsUpShowing = z;
        apply(state);
    }

    public void setWallpaperSupportsAmbientMode(boolean z) {
        State state = this.mCurrentState;
        state.mWallpaperSupportsAmbientMode = z;
        apply(state);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setStatusBarState(int i) {
        State state = this.mCurrentState;
        state.mStatusBarState = i;
        apply(state);
    }

    public void setForceWindowCollapsed(boolean z) {
        State state = this.mCurrentState;
        state.mForceCollapsed = z;
        apply(state);
    }

    public void setPanelExpanded(boolean z) {
        State state = this.mCurrentState;
        state.mPanelExpanded = z;
        apply(state);
    }

    @Override // com.android.systemui.statusbar.RemoteInputController.Callback
    public void onRemoteInputActive(boolean z) {
        State state = this.mCurrentState;
        state.mRemoteInputActive = z;
        apply(state);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onConfigChanged(Configuration configuration) {
        boolean shouldEnableKeyguardScreenRotation;
        boolean z = (configuration.uiMode & 8192) != 0;
        if (z != this.mOnPcMode) {
            this.mOnPcMode = z;
            apply(this.mCurrentState);
        }
        if ((this.mConfiguration.updateFrom(configuration) & 2048) != 0 && (shouldEnableKeyguardScreenRotation = shouldEnableKeyguardScreenRotation()) != this.mKeyguardScreenRotation) {
            this.mKeyguardScreenRotation = shouldEnableKeyguardScreenRotation;
            apply(this.mCurrentState);
        }
    }

    public void setForceDozeBrightness(boolean z) {
        State state = this.mCurrentState;
        state.mForceDozeBrightness = z;
        apply(state);
    }

    public void setDozing(boolean z) {
        State state = this.mCurrentState;
        state.mDozing = z;
        apply(state);
    }

    public void setForcePluginOpen(boolean z) {
        State state = this.mCurrentState;
        state.mForcePluginOpen = z;
        apply(state);
        ForcePluginOpenListener forcePluginOpenListener = this.mForcePluginOpenListener;
        if (forcePluginOpenListener != null) {
            forcePluginOpenListener.onChange(z);
        }
    }

    public boolean getForcePluginOpen() {
        return this.mCurrentState.mForcePluginOpen;
    }

    public void setNotTouchable(boolean z) {
        State state = this.mCurrentState;
        state.mNotTouchable = z;
        apply(state);
    }

    public boolean getPanelExpanded() {
        return this.mCurrentState.mPanelExpanded;
    }

    public void setStateListener(OtherwisedCollapsedListener otherwisedCollapsedListener) {
        this.mListener = otherwisedCollapsedListener;
    }

    public void setForcePluginOpenListener(ForcePluginOpenListener forcePluginOpenListener) {
        this.mForcePluginOpenListener = forcePluginOpenListener;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NotificationShadeWindowController:");
        printWriter.println("  mKeyguardDisplayMode=" + this.mKeyguardDisplayMode);
        printWriter.println("  mOnPcMode= " + this.mOnPcMode);
        printWriter.println("  mKeyguardScreenRotation=" + this.mKeyguardScreenRotation);
        printWriter.println(this.mCurrentState);
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onThemeChanged() {
        if (this.mNotificationShadeView != null) {
            setKeyguardDark(this.mColorExtractor.getNeutralColors().supportsDarkText());
        }
    }

    public void setKeyguardGoingAway(boolean z) {
        State state = this.mCurrentState;
        state.mKeyguardGoingAway = z;
        apply(state);
    }

    public boolean getForceHasTopUi() {
        return this.mCurrentState.mForceHasTopUi;
    }

    public void setForceHasTopUi(boolean z) {
        State state = this.mCurrentState;
        state.mForceHasTopUi = z;
        apply(state);
    }

    /* access modifiers changed from: private */
    public static class State {
        boolean keygaurdTransparent;
        boolean mBackdropShowing;
        int mBackgroundBlurRadius;
        float mBlurRatio;
        boolean mBouncerShowing;
        boolean mDozing;
        boolean mForceCollapsed;
        boolean mForceDozeBrightness;
        boolean mForceHasTopUi;
        boolean mForcePluginOpen;
        boolean mForceUserActivity;
        boolean mHeadsUpShowing;
        boolean mKeyguardFadingAway;
        boolean mKeyguardGoingAway;
        boolean mKeyguardNeedsInput;
        boolean mKeyguardOccluded;
        boolean mKeyguardShowing;
        boolean mLaunchingActivity;
        boolean mNotTouchable;
        boolean mNotificationShadeFocusable;
        boolean mPanelExpanded;
        boolean mPanelVisible;
        boolean mQsExpanded;
        boolean mRemoteInputActive;
        int mScrimsVisibility;
        int mStatusBarState;
        boolean mWallpaperSupportsAmbientMode;

        private State() {
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isKeyguardShowingAndNotOccluded() {
            return this.mKeyguardShowing && !this.mKeyguardOccluded;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Window State {");
            sb.append("\n");
            Field[] declaredFields = State.class.getDeclaredFields();
            for (Field field : declaredFields) {
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

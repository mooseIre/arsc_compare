package com.android.systemui.statusbar.phone;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.assist.AssistHandleViewController;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.model.SysUiState;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsOnboarding;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.QuickStepContract;
import com.android.systemui.shared.system.SysUiStatsLog;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.RegionSamplingHelper;
import com.android.systemui.statusbar.policy.DeadZone;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;
import com.android.systemui.util.Utils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.function.Consumer;

public class NavigationBarView extends FrameLayout implements NavigationModeController.ModeChangedListener {
    private final Region mActiveRegion = new Region();
    private Rect mBackButtonBounds = new Rect();
    private KeyButtonDrawable mBackIcon;
    private final NavigationBarTransitions mBarTransitions;
    private final SparseArray<ButtonDispatcher> mButtonDispatchers = new SparseArray<>();
    private Configuration mConfiguration;
    private final ContextualButtonGroup mContextualButtonGroup;
    private ControlPanelWindowManager mControlPanelWindowManager;
    private int mCurrentRotation = -1;
    View mCurrentView = null;
    private final DeadZone mDeadZone;
    private boolean mDeadZoneConsuming = false;
    int mDisabledFlags = 0;
    private KeyButtonDrawable mDockedIcon;
    private final Consumer<Boolean> mDockedListener = new Consumer() {
        /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarView$3_rm_LYAhHXvCBhrsX10ry5w8OA */

        @Override // java.util.function.Consumer
        public final void accept(Object obj) {
            NavigationBarView.this.lambda$new$2$NavigationBarView((Boolean) obj);
        }
    };
    private boolean mDockedStackExists;
    private EdgeBackGestureHandler mEdgeBackGestureHandler;
    private FloatingRotationButton mFloatingRotationButton;
    private Rect mHomeButtonBounds = new Rect();
    private KeyButtonDrawable mHomeDefaultIcon;
    private View mHorizontal;
    private final View.OnClickListener mImeSwitcherClickListener = new View.OnClickListener() {
        /* class com.android.systemui.statusbar.phone.NavigationBarView.AnonymousClass1 */

        public void onClick(View view) {
            ((InputMethodManager) ((FrameLayout) NavigationBarView.this).mContext.getSystemService(InputMethodManager.class)).showInputMethodPickerFromSystem(true, NavigationBarView.this.getContext().getDisplayId());
        }
    };
    private boolean mImeVisible;
    private boolean mInCarMode = false;
    private boolean mIsVertical = false;
    private boolean mLayoutTransitionsEnabled = true;
    private int mNavBarMode;
    private final int mNavColorSampleMargin;
    int mNavigationIconHints = 0;
    private NavigationBarInflaterView mNavigationInflaterView;
    private final ViewTreeObserver.OnComputeInternalInsetsListener mOnComputeInternalInsetsListener = new ViewTreeObserver.OnComputeInternalInsetsListener() {
        /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarView$khIxhJwBd7pJnFFXnq8zupcHrv8 */

        public final void onComputeInternalInsets(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
            NavigationBarView.this.lambda$new$0$NavigationBarView(internalInsetsInfo);
        }
    };
    private OnVerticalChangedListener mOnVerticalChangedListener;
    private Rect mOrientedHandleSamplingRegion;
    private final OverviewProxyService mOverviewProxyService;
    private NotificationPanelViewController mPanelView;
    private final PluginManager mPluginManager;
    private final View.AccessibilityDelegate mQuickStepAccessibilityDelegate = new View.AccessibilityDelegate() {
        /* class com.android.systemui.statusbar.phone.NavigationBarView.AnonymousClass2 */
        private AccessibilityNodeInfo.AccessibilityAction mToggleOverviewAction;

        public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
            if (this.mToggleOverviewAction == null) {
                this.mToggleOverviewAction = new AccessibilityNodeInfo.AccessibilityAction(C0015R$id.action_toggle_overview, NavigationBarView.this.getContext().getString(C0021R$string.quick_step_accessibility_toggle_overview));
            }
            accessibilityNodeInfo.addAction(this.mToggleOverviewAction);
        }

        public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
            if (i != C0015R$id.action_toggle_overview) {
                return super.performAccessibilityAction(view, i, bundle);
            }
            ((Recents) Dependency.get(Recents.class)).toggleRecentApps();
            return true;
        }
    };
    private KeyButtonDrawable mRecentIcon;
    private Rect mRecentsButtonBounds = new Rect();
    private RecentsOnboarding mRecentsOnboarding;
    private final RegionSamplingHelper mRegionSamplingHelper;
    private Rect mRotationButtonBounds = new Rect();
    private RotationButtonController mRotationButtonController;
    private Rect mSamplingBounds = new Rect();
    private boolean mScreenOn = true;
    private ScreenPinningNotify mScreenPinningNotify;
    private final SysUiState mSysUiFlagContainer;
    private NavigationBarViewTaskSwitchHelper mTaskSwitchHelper;
    private Configuration mTmpLastConfiguration;
    private int[] mTmpPosition = new int[2];
    private final NavTransitionListener mTransitionListener = new NavTransitionListener();
    private boolean mUseCarModeUi = false;
    private View mVertical;
    private boolean mWakeAndUnlocking;

    public interface OnVerticalChangedListener {
        void onVerticalChanged(boolean z);
    }

    private static String visibilityToString(int i) {
        return i != 4 ? i != 8 ? "VISIBLE" : "GONE" : "INVISIBLE";
    }

    /* access modifiers changed from: private */
    public class NavTransitionListener implements LayoutTransition.TransitionListener {
        private boolean mBackTransitioning;
        private long mDuration;
        private boolean mHomeAppearing;
        private TimeInterpolator mInterpolator;
        private long mStartDelay;

        private NavTransitionListener() {
        }

        public void startTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            if (view.getId() == C0015R$id.back) {
                this.mBackTransitioning = true;
            } else if (view.getId() == C0015R$id.home && i == 2) {
                this.mHomeAppearing = true;
                this.mStartDelay = layoutTransition.getStartDelay(i);
                this.mDuration = layoutTransition.getDuration(i);
                this.mInterpolator = layoutTransition.getInterpolator(i);
            }
        }

        public void endTransition(LayoutTransition layoutTransition, ViewGroup viewGroup, View view, int i) {
            if (view.getId() == C0015R$id.back) {
                this.mBackTransitioning = false;
            } else if (view.getId() == C0015R$id.home && i == 2) {
                this.mHomeAppearing = false;
            }
        }

        public void onBackAltCleared() {
            ButtonDispatcher backButton = NavigationBarView.this.getBackButton();
            if (!this.mBackTransitioning && backButton.getVisibility() == 0 && this.mHomeAppearing && NavigationBarView.this.getHomeButton().getAlpha() == 0.0f) {
                NavigationBarView.this.getBackButton().setAlpha(0.0f);
                ObjectAnimator ofFloat = ObjectAnimator.ofFloat(backButton, "alpha", 0.0f, 1.0f);
                ofFloat.setStartDelay(this.mStartDelay);
                ofFloat.setDuration(this.mDuration);
                ofFloat.setInterpolator(this.mInterpolator);
                ofFloat.start();
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$NavigationBarView(ViewTreeObserver.InternalInsetsInfo internalInsetsInfo) {
        if (!this.mEdgeBackGestureHandler.isHandlingGestures() || this.mImeVisible) {
            internalInsetsInfo.setTouchableInsets(0);
            return;
        }
        internalInsetsInfo.setTouchableInsets(3);
        ButtonDispatcher imeSwitchButton = getImeSwitchButton();
        if (imeSwitchButton.getVisibility() == 0) {
            int[] iArr = new int[2];
            View currentView = imeSwitchButton.getCurrentView();
            currentView.getLocationInWindow(iArr);
            internalInsetsInfo.touchableRegion.set(iArr[0], iArr[1], iArr[0] + currentView.getWidth(), iArr[1] + currentView.getHeight());
            return;
        }
        internalInsetsInfo.touchableRegion.setEmpty();
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v7, resolved type: com.android.systemui.statusbar.phone.FloatingRotationButton */
    /* JADX WARN: Multi-variable type inference failed */
    public NavigationBarView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        int addListener = ((NavigationModeController) Dependency.get(NavigationModeController.class)).addListener(this);
        this.mNavBarMode = addListener;
        boolean isGesturalMode = QuickStepContract.isGesturalMode(addListener);
        this.mSysUiFlagContainer = (SysUiState) Dependency.get(SysUiState.class);
        this.mPluginManager = (PluginManager) Dependency.get(PluginManager.class);
        this.mContextualButtonGroup = new ContextualButtonGroup(C0015R$id.menu_container);
        ContextualButton contextualButton = new ContextualButton(C0015R$id.ime_switcher, C0013R$drawable.ic_ime_switcher_default);
        RotationContextButton rotationContextButton = new RotationContextButton(C0015R$id.rotate_suggestion, C0013R$drawable.ic_sysbar_rotate_button);
        ContextualButton contextualButton2 = new ContextualButton(C0015R$id.accessibility_button, C0013R$drawable.ic_sysbar_accessibility_button);
        this.mContextualButtonGroup.addButton(contextualButton);
        if (!isGesturalMode) {
            this.mContextualButtonGroup.addButton(rotationContextButton);
        }
        this.mContextualButtonGroup.addButton(contextualButton2);
        OverviewProxyService overviewProxyService = (OverviewProxyService) Dependency.get(OverviewProxyService.class);
        this.mOverviewProxyService = overviewProxyService;
        this.mRecentsOnboarding = new RecentsOnboarding(context, overviewProxyService);
        FloatingRotationButton floatingRotationButton = new FloatingRotationButton(context);
        this.mFloatingRotationButton = floatingRotationButton;
        this.mRotationButtonController = new RotationButtonController(context, C0022R$style.RotateButtonCCWStart90, !isGesturalMode ? rotationContextButton : floatingRotationButton);
        this.mConfiguration = new Configuration();
        this.mTmpLastConfiguration = new Configuration();
        this.mConfiguration.updateFrom(context.getResources().getConfiguration());
        this.mScreenPinningNotify = new ScreenPinningNotify(((FrameLayout) this).mContext);
        this.mBarTransitions = new NavigationBarTransitions(this, (CommandQueue) Dependency.get(CommandQueue.class));
        SparseArray<ButtonDispatcher> sparseArray = this.mButtonDispatchers;
        int i = C0015R$id.back;
        sparseArray.put(i, new ButtonDispatcher(i));
        SparseArray<ButtonDispatcher> sparseArray2 = this.mButtonDispatchers;
        int i2 = C0015R$id.home;
        sparseArray2.put(i2, new ButtonDispatcher(i2));
        SparseArray<ButtonDispatcher> sparseArray3 = this.mButtonDispatchers;
        int i3 = C0015R$id.home_handle;
        sparseArray3.put(i3, new ButtonDispatcher(i3));
        SparseArray<ButtonDispatcher> sparseArray4 = this.mButtonDispatchers;
        int i4 = C0015R$id.recent_apps;
        sparseArray4.put(i4, new ButtonDispatcher(i4));
        this.mButtonDispatchers.put(C0015R$id.ime_switcher, contextualButton);
        this.mButtonDispatchers.put(C0015R$id.accessibility_button, contextualButton2);
        this.mButtonDispatchers.put(C0015R$id.rotate_suggestion, rotationContextButton);
        this.mButtonDispatchers.put(C0015R$id.menu_container, this.mContextualButtonGroup);
        this.mDeadZone = new DeadZone(this);
        this.mTaskSwitchHelper = new NavigationBarViewTaskSwitchHelper(context, this);
        this.mNavColorSampleMargin = getResources().getDimensionPixelSize(C0012R$dimen.navigation_handle_sample_horizontal_margin);
        this.mEdgeBackGestureHandler = new EdgeBackGestureHandler(context, this.mOverviewProxyService, this.mSysUiFlagContainer, this.mPluginManager, new Runnable() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$WrUd8iBVzCnkNGlDjVh6Yvbf6CM */

            public final void run() {
                NavigationBarView.this.updateStates();
            }
        });
        this.mRegionSamplingHelper = new RegionSamplingHelper(this, new RegionSamplingHelper.SamplingCallback() {
            /* class com.android.systemui.statusbar.phone.NavigationBarView.AnonymousClass3 */

            @Override // com.android.systemui.statusbar.phone.RegionSamplingHelper.SamplingCallback
            public void onRegionDarknessChanged(boolean z) {
                NavigationBarView.this.getLightTransitionsController().setIconsDark(!z, true);
            }

            @Override // com.android.systemui.statusbar.phone.RegionSamplingHelper.SamplingCallback
            public Rect getSampledRegion(View view) {
                if (NavigationBarView.this.mOrientedHandleSamplingRegion != null) {
                    return NavigationBarView.this.mOrientedHandleSamplingRegion;
                }
                NavigationBarView.this.updateSamplingRect();
                return NavigationBarView.this.mSamplingBounds;
            }

            @Override // com.android.systemui.statusbar.phone.RegionSamplingHelper.SamplingCallback
            public boolean isSamplingEnabled() {
                return Utils.isGesturalModeOnDefaultDisplay(NavigationBarView.this.getContext(), NavigationBarView.this.mNavBarMode);
            }
        });
    }

    public NavigationBarTransitions getBarTransitions() {
        return this.mBarTransitions;
    }

    public LightBarTransitionsController getLightTransitionsController() {
        return this.mBarTransitions.getLightTransitionsController();
    }

    public void setComponents(NotificationPanelViewController notificationPanelViewController) {
        this.mPanelView = notificationPanelViewController;
        updatePanelSystemUiStateFlags();
    }

    public void setOnVerticalChangedListener(OnVerticalChangedListener onVerticalChangedListener) {
        this.mOnVerticalChangedListener = onVerticalChangedListener;
        notifyVerticalChangedListener(this.mIsVertical);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (QuickStepContract.isGesturalMode(this.mNavBarMode) && this.mImeVisible && motionEvent.getAction() == 0) {
            SysUiStatsLog.write(304, (int) motionEvent.getX(), (int) motionEvent.getY());
        }
        if ((QuickStepContract.isGesturalMode(this.mNavBarMode) || !this.mTaskSwitchHelper.onInterceptTouchEvent(motionEvent)) && !shouldDeadZoneConsumeTouchEvents(motionEvent) && !super.onInterceptTouchEvent(motionEvent)) {
            return false;
        }
        return true;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!QuickStepContract.isGesturalMode(this.mNavBarMode) && this.mTaskSwitchHelper.onTouchEvent(motionEvent)) {
            return true;
        }
        shouldDeadZoneConsumeTouchEvents(motionEvent);
        return super.onTouchEvent(motionEvent);
    }

    /* access modifiers changed from: package-private */
    public void onTransientStateChanged(boolean z) {
        this.mEdgeBackGestureHandler.onNavBarTransientStateChanged(z);
    }

    /* access modifiers changed from: package-private */
    public void onBarTransition(int i) {
        if (i == 4) {
            this.mRegionSamplingHelper.stop();
            getLightTransitionsController().setIconsDark(false, true);
            return;
        }
        this.mRegionSamplingHelper.start(this.mSamplingBounds);
    }

    private boolean shouldDeadZoneConsumeTouchEvents(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            this.mDeadZoneConsuming = false;
        }
        if (!this.mDeadZone.onTouchEvent(motionEvent) && !this.mDeadZoneConsuming) {
            return false;
        }
        if (actionMasked == 0) {
            setSlippery(true);
            this.mDeadZoneConsuming = true;
        } else if (actionMasked == 1 || actionMasked == 3) {
            updateSlippery();
            this.mDeadZoneConsuming = false;
        }
        return true;
    }

    public void abortCurrentGesture() {
        getHomeButton().abortCurrentGesture();
    }

    public View getCurrentView() {
        return this.mCurrentView;
    }

    public RotationButtonController getRotationButtonController() {
        return this.mRotationButtonController;
    }

    public ButtonDispatcher getRecentsButton() {
        return this.mButtonDispatchers.get(C0015R$id.recent_apps);
    }

    public ButtonDispatcher getBackButton() {
        return this.mButtonDispatchers.get(C0015R$id.back);
    }

    public ButtonDispatcher getHomeButton() {
        return this.mButtonDispatchers.get(C0015R$id.home);
    }

    public ButtonDispatcher getImeSwitchButton() {
        return this.mButtonDispatchers.get(C0015R$id.ime_switcher);
    }

    public ButtonDispatcher getAccessibilityButton() {
        return this.mButtonDispatchers.get(C0015R$id.accessibility_button);
    }

    public RotationContextButton getRotateSuggestionButton() {
        return (RotationContextButton) this.mButtonDispatchers.get(C0015R$id.rotate_suggestion);
    }

    public ButtonDispatcher getHomeHandle() {
        return this.mButtonDispatchers.get(C0015R$id.home_handle);
    }

    public SparseArray<ButtonDispatcher> getButtonDispatchers() {
        return this.mButtonDispatchers;
    }

    public boolean isRecentsButtonVisible() {
        return getRecentsButton().getVisibility() == 0;
    }

    public boolean isOverviewEnabled() {
        return (this.mDisabledFlags & 16777216) == 0;
    }

    public boolean isQuickStepSwipeUpEnabled() {
        return this.mOverviewProxyService.shouldShowSwipeUpUI() && isOverviewEnabled();
    }

    private void reloadNavIcons() {
        updateIcons(Configuration.EMPTY);
    }

    private void updateIcons(Configuration configuration) {
        boolean z = true;
        boolean z2 = configuration.orientation != this.mConfiguration.orientation;
        boolean z3 = configuration.densityDpi != this.mConfiguration.densityDpi;
        if (configuration.getLayoutDirection() == this.mConfiguration.getLayoutDirection()) {
            z = false;
        }
        if (z2 || z3) {
            this.mDockedIcon = getDrawable(C0013R$drawable.ic_sysbar_docked);
            this.mHomeDefaultIcon = getHomeDrawable();
        }
        if (z3 || z) {
            this.mRecentIcon = getDrawable(C0013R$drawable.ic_sysbar_recent);
            this.mContextualButtonGroup.updateIcons();
        }
        if (z2 || z3 || z) {
            this.mBackIcon = getBackDrawable();
        }
    }

    public KeyButtonDrawable getBackDrawable() {
        KeyButtonDrawable drawable = getDrawable(getBackDrawableRes());
        orientBackButton(drawable);
        return drawable;
    }

    public int getBackDrawableRes() {
        return chooseNavigationIconDrawableRes(C0013R$drawable.ic_sysbar_back, C0013R$drawable.ic_sysbar_back_quick_step);
    }

    public KeyButtonDrawable getHomeDrawable() {
        KeyButtonDrawable keyButtonDrawable;
        if (this.mOverviewProxyService.shouldShowSwipeUpUI()) {
            keyButtonDrawable = getDrawable(C0013R$drawable.ic_sysbar_home_quick_step);
        } else {
            keyButtonDrawable = getDrawable(C0013R$drawable.ic_sysbar_home);
        }
        orientHomeButton(keyButtonDrawable);
        return keyButtonDrawable;
    }

    private void orientBackButton(KeyButtonDrawable keyButtonDrawable) {
        float f;
        boolean z = (this.mNavigationIconHints & 1) != 0;
        boolean z2 = this.mConfiguration.getLayoutDirection() == 1;
        float f2 = 0.0f;
        if (z) {
            f = (float) (z2 ? 90 : -90);
        } else {
            f = 0.0f;
        }
        if (keyButtonDrawable.getRotation() != f) {
            if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
                keyButtonDrawable.setRotation(f);
                return;
            }
            if (!this.mOverviewProxyService.shouldShowSwipeUpUI() && !this.mIsVertical && z) {
                f2 = -getResources().getDimension(C0012R$dimen.navbar_back_button_ime_offset);
            }
            ObjectAnimator ofPropertyValuesHolder = ObjectAnimator.ofPropertyValuesHolder(keyButtonDrawable, PropertyValuesHolder.ofFloat(KeyButtonDrawable.KEY_DRAWABLE_ROTATE, f), PropertyValuesHolder.ofFloat(KeyButtonDrawable.KEY_DRAWABLE_TRANSLATE_Y, f2));
            ofPropertyValuesHolder.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofPropertyValuesHolder.setDuration(200L);
            ofPropertyValuesHolder.start();
        }
    }

    private void orientHomeButton(KeyButtonDrawable keyButtonDrawable) {
        keyButtonDrawable.setRotation(this.mIsVertical ? 90.0f : 0.0f);
    }

    private int chooseNavigationIconDrawableRes(int i, int i2) {
        return this.mOverviewProxyService.shouldShowSwipeUpUI() ? i2 : i;
    }

    private KeyButtonDrawable getDrawable(int i) {
        return KeyButtonDrawable.create(((FrameLayout) this).mContext, i, true);
    }

    public void onScreenStateChanged(boolean z) {
        this.mScreenOn = z;
        if (!z) {
            this.mRegionSamplingHelper.stop();
        } else if (Utils.isGesturalModeOnDefaultDisplay(getContext(), this.mNavBarMode)) {
            this.mRegionSamplingHelper.start(this.mSamplingBounds);
        }
    }

    public void setWindowVisible(boolean z) {
        this.mRegionSamplingHelper.setWindowVisible(z);
        this.mRotationButtonController.onNavigationBarWindowVisibilityChange(z);
    }

    public void setLayoutDirection(int i) {
        reloadNavIcons();
        super.setLayoutDirection(i);
    }

    public void setNavigationIconHints(int i) {
        if (i != this.mNavigationIconHints) {
            boolean z = false;
            boolean z2 = (i & 1) != 0;
            if ((this.mNavigationIconHints & 1) != 0) {
                z = true;
            }
            if (z2 != z) {
                onImeVisibilityChanged(z2);
            }
            this.mNavigationIconHints = i;
            updateNavButtonIcons();
        }
    }

    private void onImeVisibilityChanged(boolean z) {
        if (!z) {
            this.mTransitionListener.onBackAltCleared();
        }
        this.mImeVisible = z;
        this.mRotationButtonController.getRotationButton().setCanShowRotationButton(!this.mImeVisible);
    }

    public void setDisabledFlags(int i) {
        if (this.mDisabledFlags != i) {
            boolean isOverviewEnabled = isOverviewEnabled();
            this.mDisabledFlags = i;
            if (!isOverviewEnabled && isOverviewEnabled()) {
                reloadNavIcons();
            }
            updateNavButtonIcons();
            updateSlippery();
            setUpSwipeUpOnboarding(isQuickStepSwipeUpEnabled());
            updateDisabledSystemUiStateFlags();
        }
    }

    public void updateNavButtonIcons() {
        LayoutTransition layoutTransition;
        int i = 0;
        boolean z = (this.mNavigationIconHints & 1) != 0;
        KeyButtonDrawable keyButtonDrawable = this.mBackIcon;
        orientBackButton(keyButtonDrawable);
        KeyButtonDrawable keyButtonDrawable2 = this.mHomeDefaultIcon;
        if (!this.mUseCarModeUi) {
            orientHomeButton(keyButtonDrawable2);
        }
        getHomeButton().setImageDrawable(keyButtonDrawable2);
        getBackButton().setImageDrawable(keyButtonDrawable);
        updateRecentsIcon();
        this.mContextualButtonGroup.setButtonVisibility(C0015R$id.ime_switcher, (this.mNavigationIconHints & 2) != 0);
        this.mBarTransitions.reapplyDarkIntensity();
        boolean z2 = QuickStepContract.isGesturalMode(this.mNavBarMode) || (this.mDisabledFlags & 2097152) != 0;
        boolean isRecentsButtonDisabled = isRecentsButtonDisabled();
        boolean z3 = isRecentsButtonDisabled && (2097152 & this.mDisabledFlags) != 0;
        boolean z4 = !z && (QuickStepContract.isGesturalMode(this.mNavBarMode) || (this.mDisabledFlags & 4194304) != 0);
        boolean isScreenPinningActive = ActivityManagerWrapper.getInstance().isScreenPinningActive();
        if (this.mOverviewProxyService.isEnabled()) {
            isRecentsButtonDisabled |= true ^ QuickStepContract.isLegacyMode(this.mNavBarMode);
            if (isScreenPinningActive && !QuickStepContract.isGesturalMode(this.mNavBarMode)) {
                z4 = false;
                z2 = false;
            }
        } else if (isScreenPinningActive) {
            z4 = false;
            isRecentsButtonDisabled = false;
        }
        ViewGroup viewGroup = (ViewGroup) getCurrentView().findViewById(C0015R$id.nav_buttons);
        if (!(viewGroup == null || (layoutTransition = viewGroup.getLayoutTransition()) == null || layoutTransition.getTransitionListeners().contains(this.mTransitionListener))) {
            layoutTransition.addTransitionListener(this.mTransitionListener);
        }
        getBackButton().setVisibility(z4 ? 4 : 0);
        getHomeButton().setVisibility(z2 ? 4 : 0);
        getRecentsButton().setVisibility(isRecentsButtonDisabled ? 4 : 0);
        ButtonDispatcher homeHandle = getHomeHandle();
        if (z3) {
            i = 4;
        }
        homeHandle.setVisibility(i);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isRecentsButtonDisabled() {
        return this.mUseCarModeUi || !isOverviewEnabled() || getContext().getDisplayId() != 0;
    }

    private Display getContextDisplay() {
        return getContext().getDisplay();
    }

    public void setLayoutTransitionsEnabled(boolean z) {
        this.mLayoutTransitionsEnabled = z;
        updateLayoutTransitionsEnabled();
    }

    public void setWakeAndUnlocking(boolean z) {
        setUseFadingAnimations(z);
        this.mWakeAndUnlocking = z;
        updateLayoutTransitionsEnabled();
    }

    private void updateLayoutTransitionsEnabled() {
        boolean z = !this.mWakeAndUnlocking && this.mLayoutTransitionsEnabled;
        LayoutTransition layoutTransition = ((ViewGroup) getCurrentView().findViewById(C0015R$id.nav_buttons)).getLayoutTransition();
        if (layoutTransition == null) {
            return;
        }
        if (z) {
            layoutTransition.enableTransitionType(2);
            layoutTransition.enableTransitionType(3);
            layoutTransition.enableTransitionType(0);
            layoutTransition.enableTransitionType(1);
            return;
        }
        layoutTransition.disableTransitionType(2);
        layoutTransition.disableTransitionType(3);
        layoutTransition.disableTransitionType(0);
        layoutTransition.disableTransitionType(1);
    }

    private void setUseFadingAnimations(boolean z) {
        WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams) ((ViewGroup) getParent()).getLayoutParams();
        if (layoutParams != null) {
            boolean z2 = layoutParams.windowAnimations != 0;
            if (!z2 && z) {
                layoutParams.windowAnimations = C0022R$style.Animation_NavigationBarFadeIn;
            } else if (z2 && !z) {
                layoutParams.windowAnimations = 0;
            } else {
                return;
            }
            ((WindowManager) getContext().getSystemService("window")).updateViewLayout((View) getParent(), layoutParams);
        }
    }

    public void onStatusBarPanelStateChanged() {
        updateSlippery();
        updatePanelSystemUiStateFlags();
    }

    public void updateDisabledSystemUiStateFlags() {
        int displayId = ((FrameLayout) this).mContext.getDisplayId();
        SysUiState sysUiState = this.mSysUiFlagContainer;
        boolean z = true;
        sysUiState.setFlag(1, ActivityManagerWrapper.getInstance().isScreenPinningActive());
        sysUiState.setFlag(128, (this.mDisabledFlags & 16777216) != 0);
        sysUiState.setFlag(256, (this.mDisabledFlags & 2097152) != 0);
        if ((this.mDisabledFlags & 33554432) == 0) {
            z = false;
        }
        sysUiState.setFlag(1024, z);
        sysUiState.commitUpdate(displayId);
    }

    public void updatePanelSystemUiStateFlags() {
        int displayId = ((FrameLayout) this).mContext.getDisplayId();
        NotificationPanelViewController notificationPanelViewController = this.mPanelView;
        if (notificationPanelViewController != null) {
            SysUiState sysUiState = this.mSysUiFlagContainer;
            sysUiState.setFlag(4, notificationPanelViewController.isFullyExpanded() && !this.mPanelView.isInSettings());
            sysUiState.setFlag(2048, this.mPanelView.isInSettings());
            sysUiState.commitUpdate(displayId);
        }
    }

    public void updateStates() {
        boolean shouldShowSwipeUpUI = this.mOverviewProxyService.shouldShowSwipeUpUI();
        NavigationBarInflaterView navigationBarInflaterView = this.mNavigationInflaterView;
        if (navigationBarInflaterView != null) {
            navigationBarInflaterView.onLikelyDefaultLayoutChange();
        }
        updateSlippery();
        reloadNavIcons();
        updateNavButtonIcons();
        setUpSwipeUpOnboarding(isQuickStepSwipeUpEnabled());
        WindowManagerWrapper.getInstance().setNavBarVirtualKeyHapticFeedbackEnabled(!shouldShowSwipeUpUI);
        getHomeButton().setAccessibilityDelegate(shouldShowSwipeUpUI ? this.mQuickStepAccessibilityDelegate : null);
    }

    public void updateSlippery() {
        NotificationPanelViewController notificationPanelViewController;
        ControlPanelWindowManager controlPanelWindowManager;
        setSlippery(!isQuickStepSwipeUpEnabled() || ((notificationPanelViewController = this.mPanelView) != null && notificationPanelViewController.isFullyExpanded() && !this.mPanelView.isCollapsing()) || ((controlPanelWindowManager = this.mControlPanelWindowManager) != null && controlPanelWindowManager.isPanelExpanded()));
    }

    private void setSlippery(boolean z) {
        setWindowFlag(536870912, z);
    }

    private void setWindowFlag(int i, boolean z) {
        WindowManager.LayoutParams layoutParams;
        ViewGroup viewGroup = (ViewGroup) getParent();
        if (viewGroup != null && (layoutParams = (WindowManager.LayoutParams) viewGroup.getLayoutParams()) != null) {
            if (z != ((layoutParams.flags & i) != 0)) {
                if (z) {
                    layoutParams.flags = i | layoutParams.flags;
                } else {
                    layoutParams.flags = (~i) & layoutParams.flags;
                }
                ((WindowManager) getContext().getSystemService("window")).updateViewLayout(viewGroup, layoutParams);
            }
        }
    }

    @Override // com.android.systemui.statusbar.phone.NavigationModeController.ModeChangedListener
    public void onNavigationModeChanged(int i) {
        this.mNavBarMode = i;
        this.mBarTransitions.onNavigationModeChanged(i);
        this.mEdgeBackGestureHandler.onNavigationModeChanged(this.mNavBarMode);
        this.mRecentsOnboarding.onNavigationModeChanged(this.mNavBarMode);
        getRotateSuggestionButton().onNavigationModeChanged(this.mNavBarMode);
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            this.mRegionSamplingHelper.start(this.mSamplingBounds);
        } else {
            this.mRegionSamplingHelper.stop();
        }
    }

    public void setAccessibilityButtonState(boolean z, boolean z2) {
        getAccessibilityButton().setLongClickable(z2);
        this.mContextualButtonGroup.setButtonVisibility(C0015R$id.accessibility_button, z);
    }

    /* access modifiers changed from: package-private */
    public void hideRecentsOnboarding() {
        this.mRecentsOnboarding.hide(true);
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        NavigationBarInflaterView navigationBarInflaterView = (NavigationBarInflaterView) findViewById(C0015R$id.navigation_inflater);
        this.mNavigationInflaterView = navigationBarInflaterView;
        navigationBarInflaterView.setButtonDispatchers(this.mButtonDispatchers);
        getImeSwitchButton().setOnClickListener(this.mImeSwitcherClickListener);
        ((Divider) Dependency.get(Divider.class)).registerInSplitScreenListener(this.mDockedListener);
        updateOrientationViews();
        reloadNavIcons();
        this.mControlPanelWindowManager = (ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        this.mDeadZone.onDraw(canvas);
        super.onDraw(canvas);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSamplingRect() {
        this.mSamplingBounds.setEmpty();
        View currentView = getHomeHandle().getCurrentView();
        if (currentView != null) {
            int[] iArr = new int[2];
            currentView.getLocationOnScreen(iArr);
            Point point = new Point();
            currentView.getContext().getDisplay().getRealSize(point);
            this.mSamplingBounds.set(new Rect(iArr[0] - this.mNavColorSampleMargin, point.y - getNavBarHeight(), iArr[0] + currentView.getWidth() + this.mNavColorSampleMargin, point.y));
        }
    }

    /* access modifiers changed from: package-private */
    public void setOrientedHandleSamplingRegion(Rect rect) {
        this.mOrientedHandleSamplingRegion = rect;
        this.mRegionSamplingHelper.updateSamplingRect();
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        this.mActiveRegion.setEmpty();
        updateButtonLocation(getBackButton(), this.mBackButtonBounds, true);
        updateButtonLocation(getHomeButton(), this.mHomeButtonBounds, false);
        updateButtonLocation(getRecentsButton(), this.mRecentsButtonBounds, false);
        updateButtonLocation(getRotateSuggestionButton(), this.mRotationButtonBounds, true);
        this.mOverviewProxyService.onActiveNavBarRegionChanges(this.mActiveRegion);
        this.mRecentsOnboarding.setNavBarHeight(getMeasuredHeight());
    }

    private void updateButtonLocation(ButtonDispatcher buttonDispatcher, Rect rect, boolean z) {
        View currentView = buttonDispatcher.getCurrentView();
        if (currentView == null) {
            rect.setEmpty();
            return;
        }
        float translationX = currentView.getTranslationX();
        float translationY = currentView.getTranslationY();
        currentView.setTranslationX(0.0f);
        currentView.setTranslationY(0.0f);
        if (z) {
            currentView.getLocationOnScreen(this.mTmpPosition);
            int[] iArr = this.mTmpPosition;
            rect.set(iArr[0], iArr[1], iArr[0] + currentView.getMeasuredWidth(), this.mTmpPosition[1] + currentView.getMeasuredHeight());
            this.mActiveRegion.op(rect, Region.Op.UNION);
        }
        currentView.getLocationInWindow(this.mTmpPosition);
        int[] iArr2 = this.mTmpPosition;
        rect.set(iArr2[0], iArr2[1], iArr2[0] + currentView.getMeasuredWidth(), this.mTmpPosition[1] + currentView.getMeasuredHeight());
        currentView.setTranslationX(translationX);
        currentView.setTranslationY(translationY);
    }

    private void updateOrientationViews() {
        this.mHorizontal = findViewById(C0015R$id.horizontal);
        this.mVertical = findViewById(C0015R$id.vertical);
        updateCurrentView();
    }

    /* access modifiers changed from: package-private */
    public boolean needsReorient(int i) {
        return this.mCurrentRotation != i;
    }

    private void updateCurrentView() {
        resetViews();
        View view = this.mIsVertical ? this.mVertical : this.mHorizontal;
        this.mCurrentView = view;
        boolean z = false;
        view.setVisibility(0);
        this.mNavigationInflaterView.setVertical(this.mIsVertical);
        int rotation = getContextDisplay().getRotation();
        this.mCurrentRotation = rotation;
        NavigationBarInflaterView navigationBarInflaterView = this.mNavigationInflaterView;
        if (rotation == 1) {
            z = true;
        }
        navigationBarInflaterView.setAlternativeOrder(z);
        this.mNavigationInflaterView.updateButtonDispatchersCurrentView();
        updateLayoutTransitionsEnabled();
    }

    private void resetViews() {
        this.mHorizontal.setVisibility(8);
        this.mVertical.setVisibility(8);
    }

    private void updateRecentsIcon() {
        this.mDockedStackExists = false;
        this.mDockedIcon.setRotation((0 == 0 || !this.mIsVertical) ? 0.0f : 90.0f);
        getRecentsButton().setImageDrawable(this.mDockedStackExists ? this.mDockedIcon : this.mRecentIcon);
        this.mBarTransitions.reapplyDarkIntensity();
    }

    public void showPinningEnterExitToast(boolean z) {
        if (z) {
            this.mScreenPinningNotify.showPinningStartToast();
        } else {
            this.mScreenPinningNotify.showPinningExitToast();
        }
    }

    public void showPinningEscapeToast() {
        this.mScreenPinningNotify.showEscapeToast(this.mNavBarMode == 2, isRecentsButtonVisible());
    }

    public void reorient() {
        updateCurrentView();
        ((NavigationBarFrame) getRootView()).setDeadZone(this.mDeadZone);
        this.mDeadZone.onConfigurationChanged(this.mCurrentRotation);
        this.mBarTransitions.init();
        if (!isLayoutDirectionResolved()) {
            resolveLayoutDirection();
        }
        updateNavButtonIcons();
        updateTaskSwitchHelper();
        getHomeButton().setVertical(this.mIsVertical);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int i3;
        int size = View.MeasureSpec.getSize(i);
        int size2 = View.MeasureSpec.getSize(i2);
        boolean z = size > 0 && size2 > size && !QuickStepContract.isGesturalMode(this.mNavBarMode);
        if (z != this.mIsVertical) {
            this.mIsVertical = z;
            reorient();
            notifyVerticalChangedListener(z);
        }
        if (QuickStepContract.isGesturalMode(this.mNavBarMode)) {
            if (this.mIsVertical) {
                i3 = getResources().getDimensionPixelSize(17105338);
            } else {
                i3 = getResources().getDimensionPixelSize(17105336);
            }
            this.mBarTransitions.setBackgroundFrame(new Rect(0, getResources().getDimensionPixelSize(17105333) - i3, size, size2));
        }
        super.onMeasure(i, i2);
    }

    private int getNavBarHeight() {
        if (this.mIsVertical) {
            return getResources().getDimensionPixelSize(17105338);
        }
        return getResources().getDimensionPixelSize(17105336);
    }

    private void notifyVerticalChangedListener(boolean z) {
        OnVerticalChangedListener onVerticalChangedListener = this.mOnVerticalChangedListener;
        if (onVerticalChangedListener != null) {
            onVerticalChangedListener.onVerticalChanged(z);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:5:0x0038, code lost:
        if (r3.getLayoutDirection() == r2.mConfiguration.getLayoutDirection()) goto L_0x003d;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onConfigurationChanged(android.content.res.Configuration r3) {
        /*
            r2 = this;
            super.onConfigurationChanged(r3)
            android.content.res.Configuration r0 = r2.mTmpLastConfiguration
            android.content.res.Configuration r1 = r2.mConfiguration
            r0.updateFrom(r1)
            android.content.res.Configuration r0 = r2.mConfiguration
            r0.updateFrom(r3)
            boolean r3 = r2.updateCarMode()
            android.content.res.Configuration r0 = r2.mTmpLastConfiguration
            r2.updateIcons(r0)
            r2.updateRecentsIcon()
            com.android.systemui.recents.RecentsOnboarding r0 = r2.mRecentsOnboarding
            android.content.res.Configuration r1 = r2.mConfiguration
            r0.onConfigurationChanged(r1)
            if (r3 != 0) goto L_0x003a
            android.content.res.Configuration r3 = r2.mTmpLastConfiguration
            int r0 = r3.densityDpi
            android.content.res.Configuration r1 = r2.mConfiguration
            int r1 = r1.densityDpi
            if (r0 != r1) goto L_0x003a
            int r3 = r3.getLayoutDirection()
            android.content.res.Configuration r0 = r2.mConfiguration
            int r0 = r0.getLayoutDirection()
            if (r3 == r0) goto L_0x003d
        L_0x003a:
            r2.updateNavButtonIcons()
        L_0x003d:
            r2.updateTaskSwitchHelper()
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NavigationBarView.onConfigurationChanged(android.content.res.Configuration):void");
    }

    private boolean updateCarMode() {
        Configuration configuration = this.mConfiguration;
        if (configuration != null) {
            boolean z = (configuration.uiMode & 15) == 3;
            if (z != this.mInCarMode) {
                this.mInCarMode = z;
                this.mUseCarModeUi = false;
            }
        }
        return false;
    }

    private String getResourceName(int i) {
        if (i == 0) {
            return "(null)";
        }
        try {
            return getContext().getResources().getResourceName(i);
        } catch (Resources.NotFoundException unused) {
            return "(unknown)";
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        requestApplyInsets();
        reorient();
        onNavigationModeChanged(this.mNavBarMode);
        setUpSwipeUpOnboarding(isQuickStepSwipeUpEnabled());
        RotationButtonController rotationButtonController = this.mRotationButtonController;
        if (rotationButtonController != null) {
            rotationButtonController.registerListeners();
        }
        this.mEdgeBackGestureHandler.onNavBarAttached();
        getViewTreeObserver().addOnComputeInternalInsetsListener(this.mOnComputeInternalInsetsListener);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((NavigationModeController) Dependency.get(NavigationModeController.class)).removeListener(this);
        setUpSwipeUpOnboarding(false);
        for (int i = 0; i < this.mButtonDispatchers.size(); i++) {
            this.mButtonDispatchers.valueAt(i).onDestroy();
        }
        RotationButtonController rotationButtonController = this.mRotationButtonController;
        if (rotationButtonController != null) {
            rotationButtonController.unregisterListeners();
        }
        this.mEdgeBackGestureHandler.onNavBarDetached();
        getViewTreeObserver().removeOnComputeInternalInsetsListener(this.mOnComputeInternalInsetsListener);
    }

    private void setUpSwipeUpOnboarding(boolean z) {
        if (z) {
            this.mRecentsOnboarding.onConnectedToLauncher();
        } else {
            this.mRecentsOnboarding.onDisconnectedFromLauncher();
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("NavigationBarView {");
        Rect rect = new Rect();
        Point point = new Point();
        getContextDisplay().getRealSize(point);
        printWriter.println(String.format("      this: " + StatusBar.viewInfo(this) + " " + visibilityToString(getVisibility()), new Object[0]));
        getWindowVisibleDisplayFrame(rect);
        boolean z = rect.right > point.x || rect.bottom > point.y;
        StringBuilder sb = new StringBuilder();
        sb.append("      window: ");
        sb.append(rect.toShortString());
        sb.append(" ");
        sb.append(visibilityToString(getWindowVisibility()));
        sb.append(z ? " OFFSCREEN!" : "");
        printWriter.println(sb.toString());
        printWriter.println(String.format("      mCurrentView: id=%s (%dx%d) %s %f", getResourceName(getCurrentView().getId()), Integer.valueOf(getCurrentView().getWidth()), Integer.valueOf(getCurrentView().getHeight()), visibilityToString(getCurrentView().getVisibility()), Float.valueOf(getCurrentView().getAlpha())));
        Object[] objArr = new Object[3];
        objArr[0] = Integer.valueOf(this.mDisabledFlags);
        objArr[1] = this.mIsVertical ? "true" : "false";
        objArr[2] = Float.valueOf(getLightTransitionsController().getCurrentDarkIntensity());
        printWriter.println(String.format("      disabled=0x%08x vertical=%s darkIntensity=%.2f", objArr));
        printWriter.println("      mOrientedHandleSamplingRegion: " + this.mOrientedHandleSamplingRegion);
        dumpButton(printWriter, "back", getBackButton());
        dumpButton(printWriter, "home", getHomeButton());
        dumpButton(printWriter, "rcnt", getRecentsButton());
        dumpButton(printWriter, "rota", getRotateSuggestionButton());
        dumpButton(printWriter, "a11y", getAccessibilityButton());
        printWriter.println("    }");
        printWriter.println("    mScreenOn: " + this.mScreenOn);
        NavigationBarInflaterView navigationBarInflaterView = this.mNavigationInflaterView;
        if (navigationBarInflaterView != null) {
            navigationBarInflaterView.dump(printWriter);
        }
        this.mContextualButtonGroup.dump(printWriter);
        this.mRecentsOnboarding.dump(printWriter);
        this.mRegionSamplingHelper.dump(printWriter);
        this.mEdgeBackGestureHandler.dump(printWriter);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        AssistHandleViewController assistHandleViewController;
        int systemWindowInsetLeft = windowInsets.getSystemWindowInsetLeft();
        int systemWindowInsetRight = windowInsets.getSystemWindowInsetRight();
        setPadding(systemWindowInsetLeft, windowInsets.getSystemWindowInsetTop(), systemWindowInsetRight, windowInsets.getSystemWindowInsetBottom());
        this.mEdgeBackGestureHandler.setInsets(systemWindowInsetLeft, systemWindowInsetRight);
        boolean z = !QuickStepContract.isGesturalMode(this.mNavBarMode) || windowInsets.getSystemWindowInsetBottom() == 0;
        setClipChildren(z);
        setClipToPadding(z);
        NavigationBarController navigationBarController = (NavigationBarController) Dependency.get(NavigationBarController.class);
        if (navigationBarController == null) {
            assistHandleViewController = null;
        } else {
            assistHandleViewController = navigationBarController.getAssistHandlerViewController();
        }
        if (assistHandleViewController != null) {
            assistHandleViewController.setBottomOffset(windowInsets.getSystemWindowInsetBottom());
        }
        return super.onApplyWindowInsets(windowInsets);
    }

    private static void dumpButton(PrintWriter printWriter, String str, ButtonDispatcher buttonDispatcher) {
        printWriter.print("      " + str + ": ");
        if (buttonDispatcher == null) {
            printWriter.print("null");
        } else {
            printWriter.print(visibilityToString(buttonDispatcher.getVisibility()) + " alpha=" + buttonDispatcher.getAlpha());
        }
        printWriter.println();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$NavigationBarView(Boolean bool) {
        post(new Runnable(bool) {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NavigationBarView$seINE1MF9Wb6jBs3U7jhkEzAV4 */
            public final /* synthetic */ Boolean f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                NavigationBarView.this.lambda$new$1$NavigationBarView(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$NavigationBarView(Boolean bool) {
        this.mDockedStackExists = bool.booleanValue();
        updateRecentsIcon();
    }

    public void applyDarkIntensity(float f) {
        NavigationBarInflaterView navigationBarInflaterView = this.mNavigationInflaterView;
        if (navigationBarInflaterView != null) {
            navigationBarInflaterView.updateBackground(f > 0.0f);
        }
    }

    private void updateTaskSwitchHelper() {
        boolean z = true;
        if (getLayoutDirection() != 1) {
            z = false;
        }
        this.mTaskSwitchHelper.setBarState(this.mIsVertical, z);
    }

    public void reverseOrder() {
        ViewGroup viewGroup = (ViewGroup) this.mHorizontal.findViewById(C0015R$id.ends_group);
        if (viewGroup != null) {
            NavigationBarViewOrderHelper.INSTANCE.reverseOrder(viewGroup);
        }
    }

    public void onViewDestroyed() {
        this.mEdgeBackGestureHandler.onDestroy();
    }
}

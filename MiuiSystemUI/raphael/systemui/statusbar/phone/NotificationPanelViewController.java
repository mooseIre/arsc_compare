package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.biometrics.BiometricSourceType;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;
import android.util.MathUtils;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.util.LatencyTracker;
import com.android.keyguard.KeyguardClockSwitch;
import com.android.keyguard.KeyguardStatusView;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiKeyguardAffordanceHelperNoOp;
import com.android.keyguard.faceunlock.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.injector.KeyguardBottomAreaInjector;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.wallpaper.MiuiWallpaperClient;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.doze.DozeLog;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.media.MediaHierarchyManager;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.qs.QSFragment;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.PulseExpansionHandler;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.VibratorHelper;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.AnimatableProperty;
import com.android.systemui.statusbar.notification.ConversationNotificationManager;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.notification.PropertyAnimator;
import com.android.systemui.statusbar.notification.ViewGroupFadeHelper;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.AnimationProperties;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.phone.KeyguardAffordanceHelper;
import com.android.systemui.statusbar.phone.KeyguardClockPositionAlgorithm;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.PanelViewController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.util.InjectionInflationController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class NotificationPanelViewController extends PanelViewController {
    private static final AnimationProperties CLOCK_ANIMATION_PROPERTIES;
    private static final Rect EMPTY_RECT = new Rect();
    private static final AnimationProperties KEYGUARD_HUN_PROPERTIES;
    private static final Rect M_DUMMY_DIRTY_RECT = new Rect(0, 0, 1, 1);
    private final AnimatableProperty KEYGUARD_HEADS_UP_SHOWING_AMOUNT = AnimatableProperty.from("KEYGUARD_HEADS_UP_SHOWING_AMOUNT", new BiConsumer() {
        /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationPanelViewController$OZSOwGanFM_WhbUvj9snkcbnX8 */

        @Override // java.util.function.BiConsumer
        public final void accept(Object obj, Object obj2) {
            NotificationPanelViewController.this.lambda$new$0$NotificationPanelViewController((NotificationPanelView) obj, (Float) obj2);
        }
    }, new Function() {
        /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationPanelViewController$zLH5b8RJBtRSCf103Z1grf2LCf0 */

        @Override // java.util.function.Function
        public final Object apply(Object obj) {
            return NotificationPanelViewController.this.lambda$new$1$NotificationPanelViewController((NotificationPanelView) obj);
        }
    }, C0015R$id.keyguard_hun_animator_tag, C0015R$id.keyguard_hun_animator_end_tag, C0015R$id.keyguard_hun_animator_start_tag);
    private View.AccessibilityDelegate mAccessibilityDelegate;
    private final AccessibilityManager mAccessibilityManager;
    private final ActivityManager mActivityManager;
    private boolean mAffordanceHasPreview;
    private KeyguardAffordanceHelper mAffordanceHelper;
    private Consumer<Boolean> mAffordanceLaunchListener;
    private boolean mAllowExpandForSmallExpansion;
    private int mAmbientIndicationBottomPadding;
    protected final Runnable mAnimateKeyguardBottomAreaInvisibleEndRunnable;
    private final Runnable mAnimateKeyguardStatusBarInvisibleEndRunnable;
    private final Runnable mAnimateKeyguardStatusViewGoneEndRunnable;
    private final Runnable mAnimateKeyguardStatusViewInvisibleEndRunnable;
    protected final Runnable mAnimateKeyguardStatusViewVisibleEndRunnable;
    private boolean mAnimateNextPositionUpdate;
    private boolean mAnimatingQS;
    private int mBarState;
    private ViewGroup mBigClockContainer;
    private final BiometricUnlockController mBiometricUnlockController;
    private boolean mBlockTouches;
    private boolean mBlockingExpansionForCurrentTouch;
    private float mBottomAreaShadeAlpha;
    private final ValueAnimator mBottomAreaShadeAlphaAnimator;
    protected final MiuiKeyguardClockPositionAlgorithm mClockPositionAlgorithm = new MiuiKeyguardClockPositionAlgorithm();
    protected final KeyguardClockPositionAlgorithm.Result mClockPositionResult = new KeyguardClockPositionAlgorithm.Result();
    private boolean mClosingWithAlphaFadeOut;
    protected boolean mCollapsedOnDown;
    private final CommandQueue mCommandQueue;
    private final ConfigurationController mConfigurationController;
    private final ConfigurationListener mConfigurationListener = new ConfigurationListener();
    private boolean mConflictingQsExpansionGesture;
    private final ConversationNotificationManager mConversationNotificationManager;
    private int mDarkIconSize;
    private boolean mDelayShowingKeyguardStatusBar;
    private int mDisplayId;
    private float mDownX;
    private float mDownY;
    private final DozeParameters mDozeParameters;
    protected boolean mDozing;
    private boolean mDozingOnDown;
    private float mEmptyDragAmount;
    private final NotificationEntryManager mEntryManager;
    private Runnable mExpandAfterLayoutRunnable;
    private float mExpandOffset;
    private boolean mExpandingFromHeadsUp;
    private final ExpansionCallback mExpansionCallback = new ExpansionCallback();
    private boolean mExpectingSynthesizedDown;
    private FalsingManager mFalsingManager;
    private boolean mFirstBypassAttempt;
    protected FlingAnimationUtils mFlingAnimationUtils;
    private final FlingAnimationUtils.Builder mFlingAnimationUtilsBuilder;
    private final FragmentHostManager.FragmentListener mFragmentListener;
    private NotificationGroupManager mGroupManager;
    private boolean mHeadsUpAnimatingAway;
    private HeadsUpAppearanceController mHeadsUpAppearanceController;
    private Runnable mHeadsUpExistenceChangedRunnable = new Runnable() {
        /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationPanelViewController$rvgAK3TYgwqivfcZ2YHWM7JuvzA */

        public final void run() {
            NotificationPanelViewController.this.lambda$new$2$NotificationPanelViewController();
        }
    };
    private int mHeadsUpInset;
    private boolean mHeadsUpPinnedMode;
    protected HeadsUpTouchHelper mHeadsUpTouchHelper;
    private final HeightListener mHeightListener = new HeightListener();
    private boolean mHideIconsDuringNotificationLaunch = true;
    private int mIndicationBottomPadding;
    private float mInitialHeightOnTouch;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private final InjectionInflationController mInjectionInflationController;
    private float mInterpolatedDarkAmount;
    protected boolean mIsExpanding;
    private boolean mIsFullWidth;
    private boolean mIsLaunchTransitionFinished;
    private boolean mIsLaunchTransitionRunning;
    private boolean mIsPanelCollapseOnQQS;
    private final KeyguardAffordanceHelperCallback mKeyguardAffordanceHelperCallback = new KeyguardAffordanceHelperCallback();
    protected final KeyguardBypassController mKeyguardBypassController;
    private float mKeyguardHeadsUpShowingAmount;
    private KeyguardIndicationController mKeyguardIndicationController;
    protected boolean mKeyguardShowing;
    protected KeyguardStatusBarView mKeyguardStatusBar;
    private float mKeyguardStatusBarAnimateAlpha = 1.0f;
    private KeyguardStatusView mKeyguardStatusView;
    protected boolean mKeyguardStatusViewAnimating;
    @VisibleForTesting
    final KeyguardUpdateMonitorCallback mKeyguardUpdateCallback = new KeyguardUpdateMonitorCallback() {
        /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass1 */

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricAuthenticated(int i, BiometricSourceType biometricSourceType, boolean z) {
            if (NotificationPanelViewController.this.mFirstBypassAttempt && NotificationPanelViewController.this.mUpdateMonitor.isUnlockingWithBiometricAllowed(z)) {
                NotificationPanelViewController.this.mDelayShowingKeyguardStatusBar = true;
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onBiometricRunningStateChanged(boolean z, BiometricSourceType biometricSourceType) {
            boolean z2 = true;
            if (!(NotificationPanelViewController.this.mBarState == 1 || NotificationPanelViewController.this.mBarState == 2)) {
                z2 = false;
            }
            if (!z && NotificationPanelViewController.this.mFirstBypassAttempt && z2) {
                NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
                if (!notificationPanelViewController.mDozing && !notificationPanelViewController.mDelayShowingKeyguardStatusBar && !NotificationPanelViewController.this.mBiometricUnlockController.isBiometricUnlock()) {
                    NotificationPanelViewController.this.mFirstBypassAttempt = false;
                    NotificationPanelViewController.this.animateKeyguardStatusBarIn(300);
                }
            }
        }

        @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
        public void onFinishedGoingToSleep(int i) {
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.mFirstBypassAttempt = notificationPanelViewController.mKeyguardBypassController.getBypassEnabled();
            NotificationPanelViewController.this.mDelayShowingKeyguardStatusBar = false;
        }
    };
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private String mLastCameraLaunchSource = "lockscreen_affordance";
    private boolean mLastEventSynthesizedDown;
    private int mLastOrientation = -1;
    private float mLastOverscroll;
    private Runnable mLaunchAnimationEndRunnable;
    protected boolean mLaunchingAffordance;
    private float mLinearDarkAmount;
    private boolean mListenForHeadsUp;
    private LockscreenGestureLogger mLockscreenGestureLogger = new LockscreenGestureLogger();
    private final NotificationLockscreenUserManager mLockscreenUserManager;
    private final MediaHierarchyManager mMediaHierarchyManager;
    private final MetricsLogger mMetricsLogger;
    protected MiuiKeyguardFaceUnlockView mMiuiKeyguardFaceUnlockView;
    private int mNavigationBarBottomHeight;
    protected NotificationsQuickSettingsContainer mNotificationContainerParent;
    private NotificationStackScrollLayout mNotificationStackScroller;
    private int mNotificationsHeaderCollideDistance;
    private int mOldLayoutDirection;
    private final OnClickListener mOnClickListener = new OnClickListener();
    private final OnEmptySpaceClickListener mOnEmptySpaceClickListener = new OnEmptySpaceClickListener();
    private final MyOnHeadsUpChangedListener mOnHeadsUpChangedListener = new MyOnHeadsUpChangedListener();
    private final OnHeightChangedListener mOnHeightChangedListener = new OnHeightChangedListener();
    private final OnOverscrollTopChangedListener mOnOverscrollTopChangedListener = new OnOverscrollTopChangedListener();
    private Runnable mOnReinflationListener;
    private boolean mOnlyAffordanceInThisMotion;
    private int mPanelAlpha;
    private final AnimatableProperty mPanelAlphaAnimator = AnimatableProperty.from("panelAlpha", $$Lambda$aKsp0zdf_wKFZXD1TonJ2cFEsN4.INSTANCE, $$Lambda$SmdYpsZqQm1fpR9OgK3SiEL3pJQ.INSTANCE, C0015R$id.panel_alpha_animator_tag, C0015R$id.panel_alpha_animator_start_tag, C0015R$id.panel_alpha_animator_end_tag);
    private Runnable mPanelAlphaEndAction;
    private final AnimationProperties mPanelAlphaInPropertiesAnimator;
    private final AnimationProperties mPanelAlphaOutPropertiesAnimator;
    protected boolean mPanelExpanded;
    private int mPositionMinSideMargin;
    private final PowerManager mPowerManager;
    private final PulseExpansionHandler mPulseExpansionHandler;
    private boolean mPulsing;
    protected QS mQs;
    private boolean mQsAnimatorExpand;
    private boolean mQsExpandImmediate;
    protected boolean mQsExpanded;
    private boolean mQsExpandedWhenExpandingStarted;
    private ValueAnimator mQsExpansionAnimator;
    protected boolean mQsExpansionEnabled = true;
    private boolean mQsExpansionFromOverscroll;
    private float mQsExpansionHeight;
    private int mQsFalsingThreshold;
    public FrameLayout mQsFrame;
    private boolean mQsFullyExpanded;
    protected int mQsMaxExpansionHeight;
    private int mQsMinExpansionHeight;
    private View mQsNavbarScrim;
    private int mQsNotificationTopPadding;
    private int mQsPeekHeight;
    private boolean mQsScrimEnabled = true;
    private ValueAnimator mQsSizeChangeAnimator;
    private boolean mQsTouchAboveFalsingThreshold;
    protected boolean mQsTracking;
    private VelocityTracker mQsVelocityTracker;
    protected final ShadeController mShadeController;
    private int mShelfHeight;
    private boolean mShowEmptyShadeView;
    private boolean mShowIconsWhenExpanded;
    private boolean mShowingKeyguardHeadsUp;
    private int mStackScrollerMeasuringPass;
    private boolean mStackScrollerOverscrolling;
    private final ValueAnimator.AnimatorUpdateListener mStatusBarAnimateAlphaListener;
    private final StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    private int mStatusBarMinHeight;
    private final StatusBarStateListener mStatusBarStateListener = new StatusBarStateListener();
    private int mThemeResId;
    private ArrayList<Consumer<ExpandableNotificationRow>> mTrackingHeadsUpListeners = new ArrayList<>();
    private int mTrackingPointer;
    private boolean mTwoFingerQsExpandPossible;
    protected final KeyguardUpdateMonitor mUpdateMonitor;
    private ControlPanelController.UseControlPanelChangeListener mUseControlPanelChangeListener;
    private boolean mUserSetupComplete;
    private ArrayList<Runnable> mVerticalTranslationListener = new ArrayList<>();
    protected final NotificationPanelView mView;
    private final NotificationWakeUpCoordinator mWakeUpCoordinator;
    private final ZenModeController mZenModeController;
    private final ZenModeControllerCallback mZenModeControllerCallback = new ZenModeControllerCallback();

    /* access modifiers changed from: protected */
    public View[] getQsDetailAnimatedViews() {
        return null;
    }

    public void onBouncerPreHideAnimation() {
    }

    /* access modifiers changed from: protected */
    public void updateAwePauseResumeStatus() {
    }

    static {
        AnimationProperties animationProperties = new AnimationProperties();
        animationProperties.setDuration(300);
        CLOCK_ANIMATION_PROPERTIES = animationProperties;
        AnimationProperties animationProperties2 = new AnimationProperties();
        animationProperties2.setDuration(300);
        KEYGUARD_HUN_PROPERTIES = animationProperties2;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$NotificationPanelViewController(NotificationPanelView notificationPanelView, Float f) {
        setKeyguardHeadsUpShowingAmount(f.floatValue());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ Float lambda$new$1$NotificationPanelViewController(NotificationPanelView notificationPanelView) {
        return Float.valueOf(getKeyguardHeadsUpShowingAmount());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$NotificationPanelViewController() {
        setHeadsUpAnimatingAway(false);
        notifyBarPanelExpansionChanged();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$3 */
    public /* synthetic */ void lambda$new$3$NotificationPanelViewController(Property property) {
        Runnable runnable = this.mPanelAlphaEndAction;
        if (runnable != null) {
            runnable.run();
        }
    }

    public NotificationPanelViewController(NotificationPanelView notificationPanelView, InjectionInflationController injectionInflationController, NotificationWakeUpCoordinator notificationWakeUpCoordinator, PulseExpansionHandler pulseExpansionHandler, DynamicPrivacyController dynamicPrivacyController, KeyguardBypassController keyguardBypassController, FalsingManager falsingManager, ShadeController shadeController, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationEntryManager notificationEntryManager, KeyguardStateController keyguardStateController, StatusBarStateController statusBarStateController, DozeLog dozeLog, DozeParameters dozeParameters, CommandQueue commandQueue, VibratorHelper vibratorHelper, LatencyTracker latencyTracker, PowerManager powerManager, AccessibilityManager accessibilityManager, int i, KeyguardUpdateMonitor keyguardUpdateMonitor, MetricsLogger metricsLogger, ActivityManager activityManager, ZenModeController zenModeController, ConfigurationController configurationController, FlingAnimationUtils.Builder builder, StatusBarTouchableRegionManager statusBarTouchableRegionManager, ConversationNotificationManager conversationNotificationManager, MediaHierarchyManager mediaHierarchyManager, BiometricUnlockController biometricUnlockController, StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        super(notificationPanelView, falsingManager, dozeLog, keyguardStateController, (SysuiStatusBarStateController) statusBarStateController, vibratorHelper, latencyTracker, builder, statusBarTouchableRegionManager);
        AnimationProperties animationProperties = new AnimationProperties();
        animationProperties.setDuration(150);
        animationProperties.setCustomInterpolator(this.mPanelAlphaAnimator.getProperty(), Interpolators.ALPHA_OUT);
        this.mPanelAlphaOutPropertiesAnimator = animationProperties;
        AnimationProperties animationProperties2 = new AnimationProperties();
        animationProperties2.setDuration(200);
        animationProperties2.setAnimationEndAction(new Consumer() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationPanelViewController$HnNg1uN3kkP2Byw0u02uaOtmnk */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                NotificationPanelViewController.this.lambda$new$3$NotificationPanelViewController((Property) obj);
            }
        });
        animationProperties2.setCustomInterpolator(this.mPanelAlphaAnimator.getProperty(), Interpolators.ALPHA_IN);
        this.mPanelAlphaInPropertiesAnimator = animationProperties2;
        this.mKeyguardHeadsUpShowingAmount = 0.0f;
        this.mAccessibilityDelegate = new View.AccessibilityDelegate() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass2 */

            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP);
            }

            public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
                if (i != AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD.getId() && i != AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP.getId()) {
                    return super.performAccessibilityAction(view, i, bundle);
                }
                NotificationPanelViewController.this.mStatusBarKeyguardViewManager.showBouncer(true);
                return true;
            }
        };
        this.mUseControlPanelChangeListener = new ControlPanelController.UseControlPanelChangeListener() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass3 */

            @Override // com.android.systemui.controlcenter.phone.ControlPanelController.UseControlPanelChangeListener
            public void onUseControlPanelChange(boolean z) {
                NotificationPanelViewController.this.mNotificationStackScroller.updateChildrenBg();
            }
        };
        this.mAnimateKeyguardStatusViewInvisibleEndRunnable = new Runnable() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass9 */

            public void run() {
                NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
                notificationPanelViewController.mKeyguardStatusViewAnimating = false;
                notificationPanelViewController.mKeyguardStatusView.setVisibility(4);
            }
        };
        this.mAnimateKeyguardStatusViewGoneEndRunnable = new Runnable() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass10 */

            public void run() {
                NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
                notificationPanelViewController.mKeyguardStatusViewAnimating = false;
                notificationPanelViewController.mKeyguardStatusView.setVisibility(8);
            }
        };
        this.mAnimateKeyguardStatusViewVisibleEndRunnable = new Runnable() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass11 */

            public void run() {
                NotificationPanelViewController.this.mKeyguardStatusViewAnimating = false;
            }
        };
        this.mAnimateKeyguardStatusBarInvisibleEndRunnable = new Runnable() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass12 */

            public void run() {
                NotificationPanelViewController.this.mKeyguardStatusBar.setVisibility(4);
                NotificationPanelViewController.this.mKeyguardStatusBar.setAlpha(1.0f);
                NotificationPanelViewController.this.mKeyguardStatusBarAnimateAlpha = 1.0f;
            }
        };
        this.mStatusBarAnimateAlphaListener = new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass14 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                NotificationPanelViewController.this.mKeyguardStatusBarAnimateAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                NotificationPanelViewController.this.updateHeaderKeyguardAlpha();
            }
        };
        this.mAnimateKeyguardBottomAreaInvisibleEndRunnable = new Runnable() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass15 */

            public void run() {
                NotificationPanelViewController.this.mKeyguardBottomArea.setVisibility(8);
            }
        };
        this.mFragmentListener = new FragmentHostManager.FragmentListener() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass19 */

            @Override // com.android.systemui.fragments.FragmentHostManager.FragmentListener
            public void onFragmentViewCreated(String str, Fragment fragment) {
                NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
                QS qs = (QS) fragment;
                notificationPanelViewController.mQs = qs;
                qs.setPanelView(notificationPanelViewController.mHeightListener);
                NotificationPanelViewController notificationPanelViewController2 = NotificationPanelViewController.this;
                notificationPanelViewController2.mQs.setExpandClickListener(notificationPanelViewController2.mOnClickListener);
                NotificationPanelViewController notificationPanelViewController3 = NotificationPanelViewController.this;
                notificationPanelViewController3.mQs.setHeaderClickable(notificationPanelViewController3.mQsExpansionEnabled);
                NotificationPanelViewController.this.updateQSPulseExpansion();
                NotificationPanelViewController notificationPanelViewController4 = NotificationPanelViewController.this;
                notificationPanelViewController4.mQs.setOverscrolling(notificationPanelViewController4.mStackScrollerOverscrolling);
                NotificationPanelViewController notificationPanelViewController5 = NotificationPanelViewController.this;
                notificationPanelViewController5.mQs.setDetailAnimatedViews(notificationPanelViewController5.getQsDetailAnimatedViews());
                NotificationPanelViewController.this.mNotificationStackScroller.setQsContainer((ViewGroup) NotificationPanelViewController.this.mQs.getView());
                NotificationPanelViewController notificationPanelViewController6 = NotificationPanelViewController.this;
                QS qs2 = notificationPanelViewController6.mQs;
                if (qs2 instanceof QSFragment) {
                    notificationPanelViewController6.mKeyguardStatusBar.setQSPanel(((QSFragment) qs2).getQsPanel());
                }
                NotificationPanelViewController.this.updateQsExpansion();
            }

            @Override // com.android.systemui.fragments.FragmentHostManager.FragmentListener
            public void onFragmentViewDestroyed(String str, Fragment fragment) {
                NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
                if (fragment == notificationPanelViewController.mQs) {
                    notificationPanelViewController.mQs = null;
                }
            }
        };
        this.mView = notificationPanelView;
        this.mMetricsLogger = metricsLogger;
        this.mActivityManager = activityManager;
        this.mZenModeController = zenModeController;
        this.mConfigurationController = configurationController;
        this.mFlingAnimationUtilsBuilder = builder;
        this.mMediaHierarchyManager = mediaHierarchyManager;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
        notificationPanelView.setWillNotDraw(true);
        this.mInjectionInflationController = injectionInflationController;
        this.mFalsingManager = falsingManager;
        this.mPowerManager = powerManager;
        this.mWakeUpCoordinator = notificationWakeUpCoordinator;
        this.mAccessibilityManager = accessibilityManager;
        this.mView.setAccessibilityPaneTitle(determineAccessibilityPaneTitle());
        setPanelAlpha(255, false);
        this.mCommandQueue = commandQueue;
        this.mDisplayId = i;
        this.mPulseExpansionHandler = pulseExpansionHandler;
        this.mDozeParameters = dozeParameters;
        this.mBiometricUnlockController = biometricUnlockController;
        pulseExpansionHandler.setPulseExpandAbortListener(new Runnable() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationPanelViewController$iN7P4plRLlpAkFmRApdB8IRWjNM */

            public final void run() {
                NotificationPanelViewController.this.lambda$new$4$NotificationPanelViewController();
            }
        });
        this.mThemeResId = this.mView.getContext().getThemeResId();
        this.mKeyguardBypassController = keyguardBypassController;
        this.mUpdateMonitor = keyguardUpdateMonitor;
        this.mFirstBypassAttempt = keyguardBypassController.getBypassEnabled();
        this.mKeyguardStateController.addCallback(new KeyguardStateController.Callback() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass4 */

            @Override // com.android.systemui.statusbar.policy.KeyguardStateController.Callback
            public void onKeyguardFadingAwayChanged() {
                if (!NotificationPanelViewController.this.mKeyguardStateController.isKeyguardFadingAway()) {
                    NotificationPanelViewController.this.mFirstBypassAttempt = false;
                    NotificationPanelViewController.this.mDelayShowingKeyguardStatusBar = false;
                }
            }
        });
        dynamicPrivacyController.addListener(new DynamicPrivacyControlListener());
        ValueAnimator ofFloat = ValueAnimator.ofFloat(1.0f, 0.0f);
        this.mBottomAreaShadeAlphaAnimator = ofFloat;
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationPanelViewController$LinMYB2Oj2N48himJfZkXl5Ac08 */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                NotificationPanelViewController.this.lambda$new$5$NotificationPanelViewController(valueAnimator);
            }
        });
        this.mBottomAreaShadeAlphaAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass5 */

            public void onAnimationEnd(Animator animator) {
                NotificationPanelViewController.this.mKeyguardBottomArea.setVisibility(8);
            }
        });
        this.mBottomAreaShadeAlphaAnimator.setDuration(160L);
        this.mBottomAreaShadeAlphaAnimator.setInterpolator(Interpolators.ALPHA_OUT);
        this.mShadeController = shadeController;
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        this.mEntryManager = notificationEntryManager;
        this.mConversationNotificationManager = conversationNotificationManager;
        this.mView.setBackgroundColor(0);
        OnAttachStateChangeListener onAttachStateChangeListener = new OnAttachStateChangeListener();
        this.mView.addOnAttachStateChangeListener(onAttachStateChangeListener);
        if (this.mView.isAttachedToWindow()) {
            onAttachStateChangeListener.onViewAttachedToWindow(this.mView);
        }
        this.mView.setOnApplyWindowInsetsListener(new OnApplyWindowInsetsListener());
        onFinishInflate();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$4 */
    public /* synthetic */ void lambda$new$4$NotificationPanelViewController() {
        QS qs = this.mQs;
        if (qs != null) {
            qs.animateHeaderSlidingOut();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$5 */
    public /* synthetic */ void lambda$new$5$NotificationPanelViewController(ValueAnimator valueAnimator) {
        this.mBottomAreaShadeAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
        updateKeyguardBottomAreaAlpha();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        loadDimens();
        this.mKeyguardStatusBar = (KeyguardStatusBarView) this.mView.findViewById(C0015R$id.keyguard_header);
        this.mKeyguardStatusView = (KeyguardStatusView) this.mView.findViewById(C0015R$id.keyguard_status_view);
        ViewGroup viewGroup = (ViewGroup) this.mView.findViewById(C0015R$id.big_clock_container);
        this.mBigClockContainer = viewGroup;
        ((KeyguardClockSwitch) this.mView.findViewById(C0015R$id.keyguard_clock_container)).setBigClockContainer(viewGroup);
        this.mNotificationContainerParent = (NotificationsQuickSettingsContainer) this.mView.findViewById(C0015R$id.notification_container_parent);
        NotificationStackScrollLayout notificationStackScrollLayout = (NotificationStackScrollLayout) this.mView.findViewById(C0015R$id.notification_stack_scroller);
        this.mNotificationStackScroller = notificationStackScrollLayout;
        notificationStackScrollLayout.setOnHeightChangedListener(this.mOnHeightChangedListener);
        this.mNotificationStackScroller.setOverscrollTopChangedListener(this.mOnOverscrollTopChangedListener);
        this.mNotificationStackScroller.setOnEmptySpaceClickListener(this.mOnEmptySpaceClickListener);
        NotificationStackScrollLayout notificationStackScrollLayout2 = this.mNotificationStackScroller;
        Objects.requireNonNull(notificationStackScrollLayout2);
        addTrackingHeadsUpListener(new Consumer() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$hB_2bxao9PtuBwZm92el8Nt3UKY */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                NotificationStackScrollLayout.this.setTrackingHeadsUp((ExpandableNotificationRow) obj);
            }
        });
        this.mKeyguardBottomArea = (KeyguardBottomAreaView) this.mView.findViewById(C0015R$id.keyguard_bottom_area);
        this.mQsNavbarScrim = this.mView.findViewById(C0015R$id.qs_navbar_scrim);
        this.mLastOrientation = this.mResources.getConfiguration().orientation;
        initBottomArea();
        MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView = (MiuiKeyguardFaceUnlockView) this.mView.findViewById(C0015R$id.miui_keyguard_face_unlock_view);
        this.mMiuiKeyguardFaceUnlockView = miuiKeyguardFaceUnlockView;
        miuiKeyguardFaceUnlockView.setKeyguardFaceUnlockView(true);
        this.mWakeUpCoordinator.setStackScroller(this.mNotificationStackScroller);
        this.mQsFrame = (FrameLayout) this.mView.findViewById(C0015R$id.qs_frame);
        this.mPulseExpansionHandler.setUp(this.mNotificationStackScroller, this.mExpansionCallback, this.mShadeController);
        this.mWakeUpCoordinator.addListener(new NotificationWakeUpCoordinator.WakeUpListener() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass6 */

            @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
            public void onFullyHiddenChanged(boolean z) {
                NotificationPanelViewController.this.updateKeyguardStatusBarForHeadsUp();
            }

            @Override // com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator.WakeUpListener
            public void onPulseExpansionChanged(boolean z) {
                if (NotificationPanelViewController.this.mKeyguardBypassController.getBypassEnabled()) {
                    NotificationPanelViewController.this.requestScrollerTopPaddingUpdate(false);
                    NotificationPanelViewController.this.updateQSPulseExpansion();
                }
            }
        });
        this.mView.setRtlChangeListener(new NotificationPanelView.RtlChangeListener() {
            /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationPanelViewController$UjVwGXo83aLB3W0dUJndREhKfQk */

            @Override // com.android.systemui.statusbar.phone.NotificationPanelView.RtlChangeListener
            public final void onRtlPropertielsChanged(int i) {
                NotificationPanelViewController.this.lambda$onFinishInflate$6$NotificationPanelViewController(i);
            }
        });
        this.mView.setAccessibilityDelegate(this.mAccessibilityDelegate);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onFinishInflate$6 */
    public /* synthetic */ void lambda$onFinishInflate$6$NotificationPanelViewController(int i) {
        if (i != this.mOldLayoutDirection) {
            this.mAffordanceHelper.onRtlPropertiesChanged();
            this.mOldLayoutDirection = i;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void loadDimens() {
        super.loadDimens();
        FlingAnimationUtils.Builder builder = this.mFlingAnimationUtilsBuilder;
        builder.reset();
        builder.setMaxLengthSeconds(0.4f);
        this.mFlingAnimationUtils = builder.build();
        this.mStatusBarMinHeight = this.mResources.getDimensionPixelSize(17105489);
        this.mQsPeekHeight = this.mResources.getDimensionPixelSize(C0012R$dimen.qs_peek_height);
        this.mNotificationsHeaderCollideDistance = this.mResources.getDimensionPixelSize(C0012R$dimen.header_notifications_collide_distance);
        this.mClockPositionAlgorithm.loadDimens(this.mResources);
        this.mQsFalsingThreshold = this.mResources.getDimensionPixelSize(C0012R$dimen.qs_falsing_threshold);
        this.mPositionMinSideMargin = this.mResources.getDimensionPixelSize(C0012R$dimen.notification_panel_min_side_margin);
        this.mIndicationBottomPadding = this.mResources.getDimensionPixelSize(C0012R$dimen.keyguard_indication_bottom_padding);
        this.mQsNotificationTopPadding = this.mResources.getDimensionPixelSize(C0012R$dimen.qs_notification_padding);
        this.mShelfHeight = this.mResources.getDimensionPixelSize(C0012R$dimen.notification_shelf_height);
        this.mDarkIconSize = this.mResources.getDimensionPixelSize(C0012R$dimen.status_bar_icon_drawing_size_dark);
        this.mHeadsUpInset = this.mResources.getDimensionPixelSize(17105489) + this.mResources.getDimensionPixelSize(C0012R$dimen.heads_up_status_bar_padding);
    }

    public boolean hasCustomClock() {
        return this.mKeyguardStatusView.hasCustomClock();
    }

    /* access modifiers changed from: protected */
    public void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
        this.mKeyguardBottomArea.setStatusBar(statusBar);
    }

    public void setLaunchAffordanceListener(Consumer<Boolean> consumer) {
        this.mAffordanceLaunchListener = consumer;
    }

    public void updateResources(boolean z) {
        int dimensionPixelSize = this.mResources.getDimensionPixelSize(C0012R$dimen.qs_panel_width);
        int integer = this.mResources.getInteger(C0016R$integer.notification_panel_layout_gravity);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mQsFrame.getLayoutParams();
        if (!(layoutParams.width == dimensionPixelSize && layoutParams.gravity == integer)) {
            layoutParams.width = dimensionPixelSize;
            layoutParams.gravity = integer;
            this.mQsFrame.setLayoutParams(layoutParams);
        }
        int dimensionPixelSize2 = this.mResources.getDimensionPixelSize(C0012R$dimen.notification_panel_width);
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mNotificationStackScroller.getLayoutParams();
        if (layoutParams2.width != dimensionPixelSize2 || layoutParams2.gravity != integer) {
            layoutParams2.width = dimensionPixelSize2;
            layoutParams2.gravity = integer;
            this.mNotificationStackScroller.setLayoutParams(layoutParams2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void reInflateViews() {
        updateShowEmptyShadeView();
        int indexOfChild = this.mView.indexOfChild(this.mKeyguardStatusView);
        this.mView.removeView(this.mKeyguardStatusView);
        KeyguardStatusView keyguardStatusView = (KeyguardStatusView) this.mInjectionInflationController.injectable(LayoutInflater.from(this.mView.getContext())).inflate(C0017R$layout.keyguard_status_view, (ViewGroup) this.mView, false);
        this.mKeyguardStatusView = keyguardStatusView;
        this.mView.addView(keyguardStatusView, indexOfChild);
        this.mBigClockContainer.removeAllViews();
        ((KeyguardClockSwitch) this.mView.findViewById(C0015R$id.keyguard_clock_container)).setBigClockContainer(this.mBigClockContainer);
        int indexOfChild2 = this.mView.indexOfChild(this.mKeyguardBottomArea);
        this.mView.removeView(this.mKeyguardBottomArea);
        KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomArea;
        KeyguardBottomAreaView keyguardBottomAreaView2 = (KeyguardBottomAreaView) this.mInjectionInflationController.injectable(LayoutInflater.from(this.mView.getContext())).inflate(C0017R$layout.keyguard_bottom_area, (ViewGroup) this.mView, false);
        this.mKeyguardBottomArea = keyguardBottomAreaView2;
        keyguardBottomAreaView2.initFrom(keyguardBottomAreaView);
        this.mView.addView(this.mKeyguardBottomArea, indexOfChild2);
        initBottomArea();
        this.mKeyguardIndicationController.setIndicationArea(this.mKeyguardBottomArea);
        this.mStatusBarStateListener.onDozeAmountChanged(this.mStatusBarStateController.getDozeAmount(), this.mStatusBarStateController.getInterpolatedDozeAmount());
        KeyguardStatusBarView keyguardStatusBarView = this.mKeyguardStatusBar;
        if (keyguardStatusBarView != null) {
            keyguardStatusBarView.onThemeChanged();
        }
        setKeyguardStatusViewVisibility(this.mBarState, false, false);
        setKeyguardBottomAreaVisibility(this.mBarState, false);
        Runnable runnable = this.mOnReinflationListener;
        if (runnable != null) {
            runnable.run();
        }
    }

    private void initBottomArea() {
        MiuiKeyguardAffordanceHelperNoOp miuiKeyguardAffordanceHelperNoOp = new MiuiKeyguardAffordanceHelperNoOp(this.mKeyguardAffordanceHelperCallback, this.mView.getContext(), this.mFalsingManager);
        this.mAffordanceHelper = miuiKeyguardAffordanceHelperNoOp;
        this.mKeyguardBottomArea.setAffordanceHelper(miuiKeyguardAffordanceHelperNoOp);
        this.mKeyguardBottomArea.setStatusBar(this.mStatusBar);
        this.mKeyguardBottomArea.setUserSetupComplete(this.mUserSetupComplete);
    }

    public void setKeyguardIndicationController(KeyguardIndicationController keyguardIndicationController) {
        this.mKeyguardIndicationController = keyguardIndicationController;
        keyguardIndicationController.setIndicationArea(this.mKeyguardBottomArea);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateGestureExclusionRect() {
        List list;
        Rect calculateGestureExclusionRect = calculateGestureExclusionRect();
        NotificationPanelView notificationPanelView = this.mView;
        if (calculateGestureExclusionRect.isEmpty()) {
            list = Collections.EMPTY_LIST;
        } else {
            list = Collections.singletonList(calculateGestureExclusionRect);
        }
        notificationPanelView.setSystemGestureExclusionRects(list);
    }

    private Rect calculateGestureExclusionRect() {
        Region calculateTouchableRegion = this.mStatusBarTouchableRegionManager.calculateTouchableRegion();
        Rect bounds = (!isFullyCollapsed() || calculateTouchableRegion == null) ? null : calculateTouchableRegion.getBounds();
        return bounds != null ? bounds : EMPTY_RECT;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setIsFullWidth(boolean z) {
        this.mIsFullWidth = z;
        this.mNotificationStackScroller.setIsFullWidth(z);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startQsSizeChangeAnimation(int i, int i2) {
        ValueAnimator valueAnimator = this.mQsSizeChangeAnimator;
        if (valueAnimator != null) {
            i = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            this.mQsSizeChangeAnimator.cancel();
        }
        ValueAnimator ofInt = ValueAnimator.ofInt(i, i2);
        this.mQsSizeChangeAnimator = ofInt;
        ofInt.setDuration(400L);
        this.mQsSizeChangeAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mQsSizeChangeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass7 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                NotificationPanelViewController.this.requestScrollerTopPaddingUpdate(false);
                NotificationPanelViewController.this.requestPanelHeightUpdate();
                NotificationPanelViewController.this.mQs.setHeightOverride(((Integer) NotificationPanelViewController.this.mQsSizeChangeAnimator.getAnimatedValue()).intValue());
            }
        });
        this.mQsSizeChangeAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass8 */

            public void onAnimationEnd(Animator animator) {
                NotificationPanelViewController.this.mQsSizeChangeAnimator = null;
            }
        });
        this.mQsSizeChangeAnimator.start();
    }

    /* access modifiers changed from: protected */
    public void positionClockAndNotifications() {
        int i;
        boolean isAddOrRemoveAnimationPending = this.mNotificationStackScroller.isAddOrRemoveAnimationPending();
        boolean z = isAddOrRemoveAnimationPending || this.mAnimateNextPositionUpdate;
        if (this.mBarState != 1) {
            i = getUnlockedStackScrollerPadding();
        } else {
            int height = this.mView.getHeight();
            int max = Math.max(this.mIndicationBottomPadding, this.mAmbientIndicationBottomPadding);
            int clockPreferredY = this.mKeyguardStatusView.getClockPreferredY(height);
            boolean bypassEnabled = this.mKeyguardBypassController.getBypassEnabled();
            boolean z2 = !bypassEnabled && this.mNotificationStackScroller.getVisibleNotificationCount() != 0;
            this.mKeyguardStatusView.setHasVisibleNotifications(z2);
            this.mClockPositionAlgorithm.setup(this.mStatusBarMinHeight, height - max, this.mNotificationStackScroller.getIntrinsicContentHeight(), getExpandedFraction(), height, (int) ((((float) this.mKeyguardStatusView.getHeight()) - (((float) this.mShelfHeight) / 2.0f)) - (((float) this.mDarkIconSize) / 2.0f)), clockPreferredY, hasCustomClock(), z2, this.mInterpolatedDarkAmount, this.mEmptyDragAmount, bypassEnabled, getUnlockedStackScrollerPadding());
            this.mClockPositionAlgorithm.run(this.mClockPositionResult);
            PropertyAnimator.setProperty(this.mKeyguardStatusView, AnimatableProperty.X, (float) this.mClockPositionResult.clockX, CLOCK_ANIMATION_PROPERTIES, z);
            PropertyAnimator.setProperty(this.mKeyguardStatusView, AnimatableProperty.Y, (float) this.mClockPositionResult.clockY, CLOCK_ANIMATION_PROPERTIES, z);
            updateNotificationTranslucency();
            updateClock();
            i = this.mClockPositionResult.stackScrollerPaddingExpanded;
        }
        this.mNotificationStackScroller.setIntrinsicPadding(i);
        this.mKeyguardBottomArea.setAntiBurnInOffsetX(this.mClockPositionResult.clockX);
        this.mStackScrollerMeasuringPass++;
        requestScrollerTopPaddingUpdate(isAddOrRemoveAnimationPending);
        this.mStackScrollerMeasuringPass = 0;
        this.mAnimateNextPositionUpdate = false;
    }

    private int getUnlockedStackScrollerPadding() {
        QS qs = this.mQs;
        return (qs != null ? qs.getHeader().getHeight() : 0) + this.mQsPeekHeight + this.mQsNotificationTopPadding;
    }

    public int computeMaxKeyguardNotifications(int i) {
        float minStackScrollerPadding = this.mClockPositionAlgorithm.getMinStackScrollerPadding();
        int max = Math.max(1, this.mResources.getDimensionPixelSize(C0012R$dimen.notification_divider_height));
        NotificationShelf notificationShelf = this.mNotificationStackScroller.getNotificationShelf();
        float intrinsicHeight = notificationShelf.getVisibility() == 8 ? 0.0f : (float) (notificationShelf.getIntrinsicHeight() + max);
        float height = (((((float) this.mNotificationStackScroller.getHeight()) - minStackScrollerPadding) - intrinsicHeight) - ((float) Math.max(this.mIndicationBottomPadding, this.mAmbientIndicationBottomPadding))) - ((float) this.mKeyguardStatusView.getLogoutButtonHeight());
        ExpandableView expandableView = null;
        int i2 = 0;
        for (int i3 = 0; i3 < this.mNotificationStackScroller.getChildCount(); i3++) {
            ExpandableView expandableView2 = (ExpandableView) this.mNotificationStackScroller.getChildAt(i3);
            if (canShowViewOnLockscreen(expandableView2)) {
                height = ((height - ((float) expandableView2.getMinHeight(true))) - (i2 == 0 ? 0.0f : (float) max)) - this.mNotificationStackScroller.calculateGapHeight(expandableView, expandableView2, i2);
                if (height >= 0.0f && i2 < i) {
                    i2++;
                    expandableView = expandableView2;
                } else if (height <= (-intrinsicHeight)) {
                    return i2;
                } else {
                    for (int i4 = i3 + 1; i4 < this.mNotificationStackScroller.getChildCount(); i4++) {
                        ExpandableView expandableView3 = (ExpandableView) this.mNotificationStackScroller.getChildAt(i4);
                        if ((expandableView3 instanceof ExpandableNotificationRow) && canShowViewOnLockscreen(expandableView3)) {
                            return i2;
                        }
                    }
                    return i2 + 1;
                }
            }
        }
        return i2;
    }

    private boolean canShowViewOnLockscreen(ExpandableView expandableView) {
        if (expandableView.hasNoContentHeight()) {
            return false;
        }
        if ((!(expandableView instanceof ExpandableNotificationRow) || canShowRowOnLockscreen((ExpandableNotificationRow) expandableView)) && expandableView.getVisibility() != 8) {
            return true;
        }
        return false;
    }

    private boolean canShowRowOnLockscreen(ExpandableNotificationRow expandableNotificationRow) {
        NotificationGroupManager notificationGroupManager = this.mGroupManager;
        return !(notificationGroupManager != null && notificationGroupManager.isSummaryOfSuppressedGroup(expandableNotificationRow.getEntry().getSbn())) && this.mLockscreenUserManager.shouldShowOnKeyguard(expandableNotificationRow.getEntry()) && !expandableNotificationRow.isRemoved();
    }

    private void updateClock() {
        if (!this.mKeyguardStatusViewAnimating) {
            this.mKeyguardStatusView.setAlpha(this.mClockPositionResult.clockAlpha);
        }
    }

    public void animateToFullShade(long j) {
        this.mNotificationStackScroller.goToFullShade(j);
        this.mView.requestLayout();
        this.mAnimateNextPositionUpdate = true;
    }

    public void setQsExpansionEnabled(boolean z) {
        this.mQsExpansionEnabled = z;
        QS qs = this.mQs;
        if (qs != null) {
            qs.setHeaderClickable(z);
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void resetViews(boolean z) {
        this.mIsLaunchTransitionFinished = false;
        this.mBlockTouches = false;
        if (!this.mLaunchingAffordance) {
            this.mAffordanceHelper.reset(false);
            this.mLastCameraLaunchSource = "lockscreen_affordance";
        }
        this.mStatusBar.getGutsManager().closeAndSaveGuts(true, true, true, -1, -1, true);
        if (z) {
            animateCloseQs(true);
        } else {
            closeQs();
        }
        this.mNotificationStackScroller.setOverScrollAmount(0.0f, true, z, !z);
        this.mNotificationStackScroller.resetScrollPosition();
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void collapse(boolean z, float f) {
        if (canPanelBeCollapsed()) {
            if (this.mQsExpanded) {
                this.mQsExpandImmediate = true;
                this.mNotificationStackScroller.setShouldShowShelfOnly(true);
            }
            super.collapse(z, f);
        }
    }

    public void closeQs() {
        cancelQsAnimation();
        setQsExpansion((float) this.mQsMinExpansionHeight);
    }

    public void cancelAnimation() {
        this.mView.animate().cancel();
    }

    public void animateCloseQs(boolean z) {
        ValueAnimator valueAnimator = this.mQsExpansionAnimator;
        if (valueAnimator != null) {
            if (this.mQsAnimatorExpand) {
                float f = this.mQsExpansionHeight;
                valueAnimator.cancel();
                setQsExpansion(f);
            } else {
                return;
            }
        }
        flingSettings(0.0f, z ? 2 : 1);
    }

    public void expandWithQs() {
        if (this.mQsExpansionEnabled) {
            this.mQsExpandImmediate = true;
            this.mNotificationStackScroller.setShouldShowShelfOnly(true);
        }
        if (isFullyCollapsed()) {
            expand(true);
        } else {
            flingSettings(0.0f, 0);
        }
    }

    public void expandWithoutQs() {
        if (isQsExpanded()) {
            flingSettings(0.0f, 1);
        } else {
            expand(true);
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void fling(float f, boolean z) {
        GestureRecorder gestureRecorder = ((PhoneStatusBarView) this.mBar).mBar.getGestureRecorder();
        if (gestureRecorder != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("fling ");
            sb.append(f > 0.0f ? "open" : "closed");
            String sb2 = sb.toString();
            gestureRecorder.tag(sb2, "notifications,v=" + f);
        }
        super.fling(f, z);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void flingToHeight(float f, boolean z, float f2, float f3, boolean z2) {
        this.mHeadsUpTouchHelper.notifyFling(!z);
        setClosingWithAlphaFadeout(!z && !isOnKeyguard() && getFadeoutAlpha() == 1.0f);
        super.flingToHeight(f, z, f2, f3, z2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean onQsIntercept(MotionEvent motionEvent) {
        int pointerId;
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        if (findPointerIndex < 0) {
            this.mTrackingPointer = motionEvent.getPointerId(0);
            findPointerIndex = 0;
        }
        float x = motionEvent.getX(findPointerIndex);
        float y = motionEvent.getY(findPointerIndex);
        int actionMasked = motionEvent.getActionMasked();
        boolean z = true;
        int i = 1;
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    float f = y - this.mInitialTouchY;
                    trackMovement(motionEvent);
                    if (this.mQsTracking) {
                        setQsExpansion(f + this.mInitialHeightOnTouch);
                        trackMovement(motionEvent);
                        return true;
                    } else if ((f > getTouchSlop(motionEvent) || (f < (-getTouchSlop(motionEvent)) && this.mQsExpanded)) && Math.abs(f) > Math.abs(x - this.mInitialTouchX) && shouldQuickSettingsIntercept(this.mInitialTouchX, this.mInitialTouchY, f)) {
                        this.mView.getParent().requestDisallowInterceptTouchEvent(true);
                        this.mQsTracking = true;
                        onQsExpansionStarted();
                        notifyExpandingFinished();
                        this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                        this.mInitialTouchY = y;
                        this.mInitialTouchX = x;
                        this.mNotificationStackScroller.cancelLongPress();
                        return true;
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 6 && this.mTrackingPointer == (pointerId = motionEvent.getPointerId(motionEvent.getActionIndex()))) {
                        if (motionEvent.getPointerId(0) != pointerId) {
                            i = 0;
                        }
                        this.mTrackingPointer = motionEvent.getPointerId(i);
                        this.mInitialTouchX = motionEvent.getX(i);
                        this.mInitialTouchY = motionEvent.getY(i);
                    }
                }
            }
            trackMovement(motionEvent);
            if (this.mQsTracking) {
                if (motionEvent.getActionMasked() != 3) {
                    z = false;
                }
                flingQsWithCurrentVelocity(y, z);
                this.mQsTracking = false;
            }
        } else {
            this.mInitialTouchY = y;
            this.mInitialTouchX = x;
            initVelocityTracker();
            trackMovement(motionEvent);
            if (this.mKeyguardShowing && shouldQuickSettingsIntercept(this.mInitialTouchX, this.mInitialTouchY, 0.0f)) {
                this.mView.getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (this.mQsExpansionAnimator != null) {
                onQsExpansionStarted();
                this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                this.mQsTracking = true;
                this.mNotificationStackScroller.cancelLongPress();
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean isInContentBounds(float f, float f2) {
        float x = this.mNotificationStackScroller.getX();
        return !this.mNotificationStackScroller.isBelowLastNotification(f - x, f2) && x < f && f < x + ((float) this.mNotificationStackScroller.getWidth());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void initDownStates(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            this.mOnlyAffordanceInThisMotion = false;
            this.mQsTouchAboveFalsingThreshold = this.mQsFullyExpanded;
            this.mDozingOnDown = isDozing();
            this.mDownX = motionEvent.getX();
            this.mDownY = motionEvent.getY();
            this.mCollapsedOnDown = isFullyCollapsed();
            this.mIsPanelCollapseOnQQS = canPanelCollapseOnQQS(this.mDownX, this.mDownY);
            this.mListenForHeadsUp = this.mCollapsedOnDown && this.mHeadsUpManager.hasPinnedHeadsUp();
            boolean z = this.mExpectingSynthesizedDown;
            this.mAllowExpandForSmallExpansion = z;
            this.mTouchSlopExceededBeforeDown = z;
            if (z) {
                this.mLastEventSynthesizedDown = true;
            } else {
                this.mLastEventSynthesizedDown = false;
            }
        } else {
            this.mLastEventSynthesizedDown = false;
        }
    }

    /* access modifiers changed from: protected */
    public boolean canPanelCollapseOnQQS(float f, float f2) {
        if (this.mCollapsedOnDown || this.mKeyguardShowing || this.mQsExpanded) {
            return false;
        }
        QS qs = this.mQs;
        View header = qs == null ? this.mKeyguardStatusBar : qs.getHeader();
        if (f < this.mQsFrame.getX() || f > this.mQsFrame.getX() + ((float) this.mQsFrame.getWidth()) || f2 > ((float) header.getBottom())) {
            return false;
        }
        return true;
    }

    private void flingQsWithCurrentVelocity(float f, boolean z) {
        float currentQSVelocity = getCurrentQSVelocity();
        boolean flingExpandsQs = flingExpandsQs(currentQSVelocity);
        if (flingExpandsQs) {
            logQsSwipeDown(f);
        }
        flingSettings(currentQSVelocity, (!flingExpandsQs || z) ? 1 : 0);
    }

    private void logQsSwipeDown(float f) {
        this.mLockscreenGestureLogger.write(this.mBarState == 1 ? 193 : 194, (int) ((f - this.mInitialTouchY) / this.mStatusBar.getDisplayDensity()), (int) (getCurrentQSVelocity() / this.mStatusBar.getDisplayDensity()));
    }

    private boolean flingExpandsQs(float f) {
        if (this.mFalsingManager.isUnlockingDisabled() || isFalseTouch()) {
            return false;
        }
        if (Math.abs(f) < this.mFlingAnimationUtils.getMinVelocityPxPerSecond()) {
            if (getQsExpansionFraction() > 0.5f) {
                return true;
            }
            return false;
        } else if (f > 0.0f) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isFalseTouch() {
        if (!this.mKeyguardAffordanceHelperCallback.needsAntiFalsing()) {
            return false;
        }
        if (this.mFalsingManager.isClassifierEnabled()) {
            return this.mFalsingManager.isFalseTouch();
        }
        return !this.mQsTouchAboveFalsingThreshold;
    }

    /* access modifiers changed from: protected */
    public float getQsExpansionFraction() {
        float f = this.mQsExpansionHeight;
        int i = this.mQsMinExpansionHeight;
        return Math.min(1.0f, (f - ((float) i)) / ((float) (this.mQsMaxExpansionHeight - i)));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean shouldExpandWhenNotFlinging() {
        if (super.shouldExpandWhenNotFlinging()) {
            return true;
        }
        if (!this.mAllowExpandForSmallExpansion) {
            return false;
        }
        if (SystemClock.uptimeMillis() - this.mDownTime <= 300) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public float getOpeningHeight() {
        return this.mNotificationStackScroller.getOpeningHeight();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean handleQsTouch(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0 && getExpandedFraction() == 1.0f && this.mBarState != 1 && this.mQsExpansionEnabled) {
            this.mQsTracking = true;
            this.mConflictingQsExpansionGesture = true;
            onQsExpansionStarted();
            this.mInitialHeightOnTouch = this.mQsExpansionHeight;
            this.mInitialTouchY = motionEvent.getX();
            this.mInitialTouchX = motionEvent.getY();
        }
        if (this.mQsExpanded) {
            this.mQsExpandImmediate = false;
        }
        if (!isFullyCollapsed()) {
            handleQsDown(motionEvent);
        }
        if (!this.mQsExpandImmediate && this.mQsTracking) {
            onQsTouch(motionEvent);
            if (!this.mConflictingQsExpansionGesture) {
                return true;
            }
        }
        if (actionMasked == 3 || actionMasked == 1) {
            this.mConflictingQsExpansionGesture = false;
        }
        if (actionMasked == 0 && isFullyCollapsed() && this.mQsExpansionEnabled) {
            this.mTwoFingerQsExpandPossible = true;
        }
        if (this.mTwoFingerQsExpandPossible && isOpenQsEvent(motionEvent) && motionEvent.getY(motionEvent.getActionIndex()) < ((float) this.mStatusBarMinHeight)) {
            this.mMetricsLogger.count("panel_open_qs", 1);
            this.mQsExpandImmediate = true;
            this.mNotificationStackScroller.setShouldShowShelfOnly(true);
            requestPanelHeightUpdate();
            setListening(true);
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isInQsArea(float f, float f2) {
        return f >= this.mQsFrame.getX() && f <= this.mQsFrame.getX() + ((float) this.mQsFrame.getWidth()) && (f2 <= this.mNotificationStackScroller.getBottomMostNotificationBottom() || f2 <= this.mQs.getView().getY() + ((float) this.mQs.getView().getHeight()));
    }

    private boolean isOpenQsEvent(MotionEvent motionEvent) {
        int pointerCount = motionEvent.getPointerCount();
        int actionMasked = motionEvent.getActionMasked();
        return (actionMasked == 5 && pointerCount == 2) || (actionMasked == 0 && (motionEvent.isButtonPressed(32) || motionEvent.isButtonPressed(64))) || (actionMasked == 0 && (motionEvent.isButtonPressed(2) || motionEvent.isButtonPressed(4)));
    }

    private void handleQsDown(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0 && shouldQuickSettingsIntercept(motionEvent.getX(), motionEvent.getY(), -1.0f)) {
            this.mFalsingManager.onQsDown();
            this.mQsTracking = true;
            onQsExpansionStarted();
            this.mInitialHeightOnTouch = this.mQsExpansionHeight;
            this.mInitialTouchY = motionEvent.getX();
            this.mInitialTouchX = motionEvent.getY();
            notifyExpandingFinished();
        }
    }

    public void startWaitingForOpenPanelGesture() {
        if (isFullyCollapsed()) {
            this.mExpectingSynthesizedDown = true;
            onTrackingStarted();
            updatePanelExpanded();
        }
    }

    public void stopWaitingForOpenPanelGesture(boolean z, float f) {
        if (this.mExpectingSynthesizedDown) {
            this.mExpectingSynthesizedDown = false;
            if (z) {
                collapse(false, 1.0f);
            } else {
                maybeVibrateOnOpening();
                fling(f > 1.0f ? f * 1000.0f : 0.0f, true);
            }
            onTrackingStopped(false);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean flingExpands(float f, float f2, float f3, float f4) {
        boolean flingExpands = super.flingExpands(f, f2, f3, f4);
        if (this.mQsExpansionAnimator != null) {
            return true;
        }
        return flingExpands;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean shouldGestureWaitForTouchSlop() {
        if (this.mExpectingSynthesizedDown) {
            this.mExpectingSynthesizedDown = false;
            return false;
        } else if (isFullyCollapsed() || this.mBarState != 0) {
            return true;
        } else {
            return false;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean shouldGestureIgnoreXTouchSlop(float f, float f2) {
        return !this.mAffordanceHelper.isOnAffordanceIcon(f, f2);
    }

    /* access modifiers changed from: protected */
    public void onQsTouch(MotionEvent motionEvent) {
        int pointerId;
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        boolean z = false;
        int i = 0;
        if (findPointerIndex < 0) {
            this.mTrackingPointer = motionEvent.getPointerId(0);
            findPointerIndex = 0;
        }
        float y = motionEvent.getY(findPointerIndex);
        float x = motionEvent.getX(findPointerIndex);
        float f = y - this.mInitialTouchY;
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    setQsExpansion(this.mInitialHeightOnTouch + f);
                    if (f >= ((float) getFalsingThreshold())) {
                        this.mQsTouchAboveFalsingThreshold = true;
                    }
                    trackMovement(motionEvent);
                    return;
                } else if (actionMasked != 3) {
                    if (actionMasked == 6 && this.mTrackingPointer == (pointerId = motionEvent.getPointerId(motionEvent.getActionIndex()))) {
                        if (motionEvent.getPointerId(0) == pointerId) {
                            i = 1;
                        }
                        float y2 = motionEvent.getY(i);
                        float x2 = motionEvent.getX(i);
                        this.mTrackingPointer = motionEvent.getPointerId(i);
                        this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                        this.mInitialTouchY = y2;
                        this.mInitialTouchX = x2;
                        return;
                    }
                    return;
                }
            }
            this.mQsTracking = false;
            this.mTrackingPointer = -1;
            trackMovement(motionEvent);
            if (getQsExpansionFraction() != 0.0f || y >= this.mInitialTouchY) {
                if (motionEvent.getActionMasked() == 3) {
                    z = true;
                }
                flingQsWithCurrentVelocity(y, z);
            }
            VelocityTracker velocityTracker = this.mQsVelocityTracker;
            if (velocityTracker != null) {
                velocityTracker.recycle();
                this.mQsVelocityTracker = null;
                return;
            }
            return;
        }
        this.mQsTracking = true;
        this.mInitialTouchY = y;
        this.mInitialTouchX = x;
        onQsExpansionStarted();
        this.mInitialHeightOnTouch = this.mQsExpansionHeight;
        initVelocityTracker();
        trackMovement(motionEvent);
    }

    private int getFalsingThreshold() {
        return (int) (((float) this.mQsFalsingThreshold) * (this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f));
    }

    /* access modifiers changed from: protected */
    public void setOverScrolling(boolean z) {
        this.mStackScrollerOverscrolling = z;
        QS qs = this.mQs;
        if (qs != null) {
            qs.setOverscrolling(z);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onQsExpansionStarted() {
        onQsExpansionStarted(0);
    }

    /* access modifiers changed from: protected */
    public void onQsExpansionStarted(int i) {
        cancelQsAnimation();
        cancelHeightAnimator();
        float f = this.mQsExpansionHeight - ((float) i);
        setQsExpansion(f);
        requestPanelHeightUpdate();
        this.mNotificationStackScroller.checkSnoozeLeavebehind();
        if (f == 0.0f) {
            this.mStatusBar.requestFaceAuth();
        }
    }

    /* access modifiers changed from: protected */
    public void setQsExpanded(boolean z) {
        if (this.mQsExpanded != z) {
            this.mQsExpanded = z;
            updateQsState();
            requestPanelHeightUpdate();
            this.mFalsingManager.setQsExpanded(z);
            this.mStatusBar.setQsExpanded(z);
            this.mNotificationContainerParent.setQsExpanded(z);
            this.mPulseExpansionHandler.setQsExpanded(z);
            this.mKeyguardBypassController.setQSExpanded(z);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeAnimateBottomAreaAlpha() {
        this.mBottomAreaShadeAlphaAnimator.cancel();
        if (this.mBarState == 2) {
            this.mBottomAreaShadeAlphaAnimator.start();
        } else {
            this.mBottomAreaShadeAlpha = 1.0f;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void animateKeyguardStatusBarOut() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(this.mKeyguardStatusBar.getAlpha(), 0.0f);
        ofFloat.addUpdateListener(this.mStatusBarAnimateAlphaListener);
        ofFloat.setStartDelay(this.mKeyguardStateController.isKeyguardFadingAway() ? this.mKeyguardStateController.getKeyguardFadingAwayDelay() : 0);
        ofFloat.setDuration(this.mKeyguardStateController.isKeyguardFadingAway() ? this.mKeyguardStateController.getShortenedFadingAwayDuration() : 300);
        ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        ofFloat.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass13 */

            public void onAnimationEnd(Animator animator) {
                NotificationPanelViewController.this.mAnimateKeyguardStatusBarInvisibleEndRunnable.run();
            }
        });
        ofFloat.start();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void animateKeyguardStatusBarIn(long j) {
        this.mKeyguardStatusBar.setVisibility(0);
        this.mKeyguardStatusBar.setAlpha(0.0f);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        ofFloat.addUpdateListener(this.mStatusBarAnimateAlphaListener);
        ofFloat.setDuration(j);
        ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        ofFloat.start();
    }

    /* access modifiers changed from: protected */
    public void setKeyguardBottomAreaVisibility(int i, boolean z) {
        this.mKeyguardBottomArea.animate().cancel();
        if (z) {
            this.mKeyguardBottomArea.animate().alpha(0.0f).setStartDelay(this.mKeyguardStateController.getKeyguardFadingAwayDelay()).setDuration(this.mKeyguardStateController.getShortenedFadingAwayDuration()).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(this.mAnimateKeyguardBottomAreaInvisibleEndRunnable).start();
        } else if (i == 1 || i == 2) {
            this.mKeyguardBottomArea.setVisibility(0);
            ((KeyguardBottomAreaInjector) Dependency.get(KeyguardBottomAreaInjector.class)).setAlpha(1.0f);
        } else {
            this.mKeyguardBottomArea.setVisibility(8);
        }
    }

    /* access modifiers changed from: protected */
    public void setKeyguardStatusViewVisibility(int i, boolean z, boolean z2) {
        this.mKeyguardStatusView.animate().cancel();
        this.mKeyguardStatusViewAnimating = false;
        if ((!z && this.mBarState == 1 && i != 1) || z2) {
            this.mKeyguardStatusViewAnimating = true;
            this.mKeyguardStatusView.animate().alpha(0.0f).setStartDelay(0).setDuration(160).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(this.mAnimateKeyguardStatusViewGoneEndRunnable);
            if (z) {
                this.mKeyguardStatusView.animate().setStartDelay(this.mKeyguardStateController.getKeyguardFadingAwayDelay()).setDuration(this.mKeyguardStateController.getShortenedFadingAwayDuration()).start();
            }
        } else if (this.mBarState == 2 && i == 1) {
            this.mKeyguardStatusView.setVisibility(0);
            this.mKeyguardStatusViewAnimating = true;
            this.mKeyguardStatusView.setAlpha(0.0f);
            this.mKeyguardStatusView.animate().alpha(1.0f).setStartDelay(0).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).withEndAction(this.mAnimateKeyguardStatusViewVisibleEndRunnable);
        } else if (i != 1) {
            this.mKeyguardStatusView.setVisibility(8);
            this.mKeyguardStatusView.setAlpha(1.0f);
        } else if (z) {
            this.mKeyguardStatusViewAnimating = true;
            this.mKeyguardStatusView.animate().alpha(0.0f).translationYBy(((float) (-getHeight())) * 0.05f).setInterpolator(Interpolators.FAST_OUT_LINEAR_IN).setDuration(125).setStartDelay(0).withEndAction(this.mAnimateKeyguardStatusViewInvisibleEndRunnable).start();
        } else {
            this.mKeyguardStatusView.setVisibility(0);
            this.mKeyguardStatusView.setAlpha(1.0f);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateQsState() {
        this.mNotificationStackScroller.setQsExpanded(this.mQsExpanded);
        int i = 0;
        this.mNotificationStackScroller.setScrollingEnabled(this.mBarState != 1 && (!this.mQsExpanded || this.mQsExpansionFromOverscroll));
        updateEmptyShadeView();
        View view = this.mQsNavbarScrim;
        if (this.mBarState != 0 || !this.mQsExpanded || this.mStackScrollerOverscrolling || !this.mQsScrimEnabled) {
            i = 4;
        }
        view.setVisibility(i);
        KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
        if (keyguardUserSwitcher != null && this.mQsExpanded && !this.mStackScrollerOverscrolling) {
            keyguardUserSwitcher.hideIfNotSimple(true);
        }
        QS qs = this.mQs;
        if (qs != null) {
            qs.setExpanded(this.mQsExpanded);
        }
    }

    /* access modifiers changed from: protected */
    public void setQsExpansion(float f) {
        float min = Math.min(Math.max(f, (float) this.mQsMinExpansionHeight), (float) this.mQsMaxExpansionHeight);
        int i = this.mQsMaxExpansionHeight;
        this.mQsFullyExpanded = min == ((float) i) && i != 0;
        if (min > ((float) this.mQsMinExpansionHeight) && !this.mQsExpanded && !this.mStackScrollerOverscrolling && !this.mDozing) {
            setQsExpanded(true);
        } else if (min <= ((float) this.mQsMinExpansionHeight) && this.mQsExpanded) {
            setQsExpanded(false);
        }
        this.mQsExpansionHeight = min;
        updateQsExpansion();
        requestScrollerTopPaddingUpdate(false);
        updateHeaderKeyguardAlpha();
        int i2 = this.mBarState;
        if (i2 == 2 || i2 == 1) {
            updateBigClockAlpha();
        }
        if (this.mBarState == 0 && this.mQsExpanded && !this.mStackScrollerOverscrolling && this.mQsScrimEnabled) {
            this.mQsNavbarScrim.setAlpha(getQsExpansionFraction());
        }
        if (this.mAccessibilityManager.isEnabled()) {
            this.mView.setAccessibilityPaneTitle(determineAccessibilityPaneTitle());
        }
        if (!this.mFalsingManager.isUnlockingDisabled() && this.mQsFullyExpanded && this.mFalsingManager.shouldEnforceBouncer()) {
            this.mStatusBar.executeRunnableDismissingKeyguard(null, null, false, true, false);
        }
        for (int i3 = 0; i3 < this.mExpansionListeners.size(); i3++) {
            PanelExpansionListener panelExpansionListener = this.mExpansionListeners.get(i3);
            int i4 = this.mQsMaxExpansionHeight;
            panelExpansionListener.onQsExpansionChanged(i4 != 0 ? this.mQsExpansionHeight / ((float) i4) : 0.0f);
        }
    }

    /* access modifiers changed from: protected */
    public void updateQsExpansion() {
        if (this.mQs != null) {
            float qsExpansionFraction = getQsExpansionFraction();
            this.mQs.setQsExpansion(qsExpansionFraction, getHeaderTranslation());
            this.mMediaHierarchyManager.setQsExpansion(qsExpansionFraction);
            this.mNotificationStackScroller.setQsExpansionFraction(qsExpansionFraction);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String determineAccessibilityPaneTitle() {
        QS qs = this.mQs;
        if (qs != null && qs.isCustomizing()) {
            return this.mResources.getString(C0021R$string.accessibility_desc_quick_settings_edit);
        }
        if (this.mQsExpansionHeight != 0.0f && this.mQsFullyExpanded) {
            return this.mResources.getString(C0021R$string.accessibility_desc_quick_settings);
        }
        if (this.mBarState == 1) {
            return this.mResources.getString(C0021R$string.accessibility_desc_lock_screen);
        }
        return this.mResources.getString(C0021R$string.accessibility_desc_notification_shade);
    }

    /* access modifiers changed from: protected */
    public float calculateQsTopPadding() {
        if (!this.mKeyguardShowing || (!this.mQsExpandImmediate && (!this.mIsExpanding || !this.mQsExpandedWhenExpandingStarted))) {
            ValueAnimator valueAnimator = this.mQsSizeChangeAnimator;
            if (valueAnimator != null) {
                return (float) Math.max(((Integer) valueAnimator.getAnimatedValue()).intValue() + this.mQsNotificationTopPadding, getKeyguardNotificationStaticPadding());
            }
            if (this.mKeyguardShowing) {
                return MathUtils.lerp((float) getKeyguardNotificationStaticPadding(), (float) (this.mQsMaxExpansionHeight + this.mQsNotificationTopPadding), getQsExpansionFraction());
            }
            return this.mQsExpansionHeight + ((float) this.mQsNotificationTopPadding);
        }
        int keyguardNotificationStaticPadding = getKeyguardNotificationStaticPadding();
        int i = this.mQsMaxExpansionHeight + this.mQsNotificationTopPadding;
        if (this.mBarState == 1) {
            i = Math.max(keyguardNotificationStaticPadding, i);
        }
        return (float) ((int) MathUtils.lerp((float) this.mQsMinExpansionHeight, (float) i, getExpandedFraction()));
    }

    /* access modifiers changed from: protected */
    public int getKeyguardNotificationStaticPadding() {
        if (!this.mKeyguardShowing) {
            return 0;
        }
        if (!this.mKeyguardBypassController.getBypassEnabled()) {
            return this.mClockPositionResult.stackScrollerPadding;
        }
        int i = this.mHeadsUpInset;
        if (!this.mNotificationStackScroller.isPulseExpanding()) {
            return i;
        }
        return (int) MathUtils.lerp((float) i, (float) this.mClockPositionResult.stackScrollerPadding, this.mNotificationStackScroller.calculateAppearFractionBypass());
    }

    /* access modifiers changed from: protected */
    public void requestScrollerTopPaddingUpdate(boolean z) {
        this.mNotificationStackScroller.updateTopPadding(calculateQsTopPadding(), z);
        if (this.mKeyguardShowing && this.mKeyguardBypassController.getBypassEnabled()) {
            updateQsExpansion();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateQSPulseExpansion() {
        QS qs = this.mQs;
        if (qs != null) {
            qs.setShowCollapsedOnKeyguard(this.mKeyguardShowing && this.mKeyguardBypassController.getBypassEnabled() && this.mNotificationStackScroller.isPulseExpanding());
        }
    }

    private void trackMovement(MotionEvent motionEvent) {
        VelocityTracker velocityTracker = this.mQsVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.addMovement(motionEvent);
        }
    }

    private void initVelocityTracker() {
        VelocityTracker velocityTracker = this.mQsVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
        }
        this.mQsVelocityTracker = VelocityTracker.obtain();
    }

    private float getCurrentQSVelocity() {
        VelocityTracker velocityTracker = this.mQsVelocityTracker;
        if (velocityTracker == null) {
            return 0.0f;
        }
        velocityTracker.computeCurrentVelocity(1000);
        return this.mQsVelocityTracker.getYVelocity();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelQsAnimation() {
        ValueAnimator valueAnimator = this.mQsExpansionAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    public void flingSettings(float f, int i) {
        flingSettings(f, i, null, false);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x001a  */
    /* JADX WARNING: Removed duplicated region for block: B:9:0x0014  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void flingSettings(float r7, int r8, final java.lang.Runnable r9, boolean r10) {
        /*
        // Method dump skipped, instructions count: 114
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationPanelViewController.flingSettings(float, int, java.lang.Runnable, boolean):void");
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$flingSettings$7 */
    public /* synthetic */ void lambda$flingSettings$7$NotificationPanelViewController(ValueAnimator valueAnimator) {
        setQsExpansion(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    public int getActiveNotificationsCount() {
        return this.mEntryManager.getActiveNotificationsCount();
    }

    /* access modifiers changed from: protected */
    public boolean shouldQuickSettingsIntercept(float f, float f2, float f3) {
        QS qs;
        if (!this.mQsExpansionEnabled || this.mCollapsedOnDown || (this.mKeyguardShowing && this.mKeyguardBypassController.getBypassEnabled())) {
            return false;
        }
        View header = (this.mKeyguardShowing || (qs = this.mQs) == null) ? this.mKeyguardStatusBar : qs.getHeader();
        boolean z = f >= this.mQsFrame.getX() && f <= this.mQsFrame.getX() + ((float) this.mQsFrame.getWidth()) && f2 >= ((float) header.getTop()) && f2 <= ((float) header.getBottom());
        if (!this.mQsExpanded) {
            return z;
        }
        if (z || (f3 < 0.0f && isInQsArea(f, f2))) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean canCollapsePanelOnTouch() {
        if (isInSettings() || this.mBarState == 1 || this.mNotificationStackScroller.isScrolledToBottom() || this.mIsPanelCollapseOnQQS) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public int getMaxPanelHeight() {
        if (!this.mKeyguardBypassController.getBypassEnabled() || this.mBarState != 1) {
            return getMaxPanelHeightNonBypass();
        }
        return getMaxPanelHeightBypass();
    }

    private int getMaxPanelHeightNonBypass() {
        int i;
        int i2 = this.mStatusBarMinHeight;
        if (this.mBarState != 1 && this.mNotificationStackScroller.getNotGoneChildCount() == 0) {
            i2 = Math.max(i2, (int) (((float) this.mQsMinExpansionHeight) + getOverExpansionAmount()));
        }
        if (this.mQsExpandImmediate || this.mQsExpanded || ((this.mIsExpanding && this.mQsExpandedWhenExpandingStarted) || this.mPulsing)) {
            i = calculatePanelHeightQsExpanded();
        } else {
            i = calculatePanelHeightShade();
        }
        int max = Math.max(i2, i);
        if (max == 0) {
            String str = PanelViewController.TAG;
            Log.wtf(str, "maxPanelHeight is 0. getOverExpansionAmount(): " + getOverExpansionAmount() + ", calculatePanelHeightQsExpanded: " + calculatePanelHeightQsExpanded() + ", calculatePanelHeightShade: " + calculatePanelHeightShade() + ", mStatusBarMinHeight = " + this.mStatusBarMinHeight + ", mQsMinExpansionHeight = " + this.mQsMinExpansionHeight);
        }
        return max;
    }

    private int getMaxPanelHeightBypass() {
        int expandedClockPosition = this.mClockPositionAlgorithm.getExpandedClockPosition() + this.mKeyguardStatusView.getHeight();
        return this.mNotificationStackScroller.getVisibleNotificationCount() != 0 ? (int) (((float) expandedClockPosition) + (((float) this.mShelfHeight) / 2.0f) + (((float) this.mDarkIconSize) / 2.0f)) : expandedClockPosition;
    }

    public boolean isInSettings() {
        return this.mQsExpanded;
    }

    public boolean isExpanding() {
        return this.mIsExpanding;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void onHeightUpdated(float f) {
        float f2;
        if ((!this.mQsExpanded || this.mQsExpandImmediate || (this.mIsExpanding && this.mQsExpandedWhenExpandingStarted)) && this.mStackScrollerMeasuringPass <= 2) {
            positionClockAndNotifications();
        }
        if (this.mQsExpandImmediate || (this.mQsExpanded && !this.mQsTracking && this.mQsExpansionAnimator == null && !this.mQsExpansionFromOverscroll)) {
            if (this.mKeyguardShowing) {
                f2 = f / ((float) getMaxPanelHeight());
            } else {
                float intrinsicPadding = (float) (this.mNotificationStackScroller.getIntrinsicPadding() + this.mNotificationStackScroller.getLayoutMinHeight());
                f2 = (f - intrinsicPadding) / (((float) calculatePanelHeightQsExpanded()) - intrinsicPadding);
            }
            int i = this.mQsMinExpansionHeight;
            setQsExpansion(((float) i) + (f2 * ((float) (this.mQsMaxExpansionHeight - i))));
        }
        updateExpandedHeight(f);
        updateHeader();
        updateNotificationTranslucency();
        updatePanelExpanded();
        updateGestureExclusionRect();
    }

    /* access modifiers changed from: protected */
    public void updatePanelExpanded() {
        boolean z = !isFullyCollapsed() || this.mExpectingSynthesizedDown;
        if (this.mPanelExpanded != z) {
            this.mHeadsUpManager.setIsPanelExpanded(z);
            this.mStatusBarTouchableRegionManager.setPanelExpanded(z);
            this.mStatusBar.setPanelExpanded(z);
            this.mPanelExpanded = z;
        }
    }

    private int calculatePanelHeightShade() {
        int height = (int) (((float) (this.mNotificationStackScroller.getHeight() - this.mNotificationStackScroller.getEmptyBottomMargin())) + this.mNotificationStackScroller.getTopPaddingOverflow());
        return this.mBarState == 1 ? Math.max(height, this.mClockPositionAlgorithm.getExpandedClockPosition() + this.mKeyguardStatusView.getHeight() + this.mNotificationStackScroller.getIntrinsicContentHeight()) : height;
    }

    private int calculatePanelHeightQsExpanded() {
        float height = (float) ((this.mNotificationStackScroller.getHeight() - this.mNotificationStackScroller.getEmptyBottomMargin()) - this.mNotificationStackScroller.getTopPadding());
        if (this.mNotificationStackScroller.getNotGoneChildCount() == 0 && this.mShowEmptyShadeView) {
            height = (float) this.mNotificationStackScroller.getEmptyShadeViewHeight();
        }
        int i = this.mQsMaxExpansionHeight;
        if (this.mKeyguardShowing) {
            i += this.mQsNotificationTopPadding;
        }
        ValueAnimator valueAnimator = this.mQsSizeChangeAnimator;
        if (valueAnimator != null) {
            i = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        }
        float max = ((float) Math.max(i, this.mBarState == 1 ? this.mClockPositionResult.stackScrollerPadding : 0)) + height + this.mNotificationStackScroller.getTopPaddingOverflow();
        if (max > ((float) this.mNotificationStackScroller.getHeight())) {
            max = Math.max((float) (i + this.mNotificationStackScroller.getLayoutMinHeight()), (float) this.mNotificationStackScroller.getHeight());
        }
        return (int) max;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNotificationTranslucency() {
        float fadeoutAlpha = (!this.mClosingWithAlphaFadeOut || this.mExpandingFromHeadsUp || this.mHeadsUpManager.hasPinnedHeadsUp()) ? 1.0f : getFadeoutAlpha();
        if (this.mBarState == 1 && !this.mHintAnimationRunning && !this.mKeyguardBypassController.getBypassEnabled()) {
            fadeoutAlpha *= this.mClockPositionResult.clockAlpha;
        }
        if (!((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isPreViewVisible() || fadeoutAlpha != 1.0f) {
            this.mNotificationStackScroller.setAlpha(fadeoutAlpha);
        }
    }

    private float getFadeoutAlpha() {
        if (this.mQsMinExpansionHeight == 0) {
            return 1.0f;
        }
        return (float) Math.pow((double) Math.max(0.0f, Math.min(getExpandedHeight() / ((float) this.mQsMinExpansionHeight), 1.0f)), 0.75d);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public float getOverExpansionAmount() {
        float currentOverScrollAmount = this.mNotificationStackScroller.getCurrentOverScrollAmount(true);
        if (Float.isNaN(currentOverScrollAmount)) {
            Log.wtf(PanelViewController.TAG, "OverExpansionAmount is NaN!");
        }
        return currentOverScrollAmount;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public float getOverExpansionPixels() {
        return this.mNotificationStackScroller.getCurrentOverScrolledPixels(true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHeader() {
        if (this.mBarState == 1) {
            updateHeaderKeyguardAlpha();
        }
        updateQsExpansion();
    }

    /* access modifiers changed from: protected */
    public float getHeaderTranslation() {
        if (this.mBarState == 1 && !this.mKeyguardBypassController.getBypassEnabled()) {
            return (float) (-this.mQs.getQsMinExpansionHeight());
        }
        float calculateAppearFraction = this.mNotificationStackScroller.calculateAppearFraction(this.mExpandedHeight);
        float f = -this.mQsExpansionHeight;
        if (this.mKeyguardBypassController.getBypassEnabled() && isOnKeyguard() && this.mNotificationStackScroller.isPulseExpanding()) {
            if (this.mPulseExpansionHandler.isExpanding() || this.mPulseExpansionHandler.getLeavingLockscreen()) {
                calculateAppearFraction = this.mNotificationStackScroller.calculateAppearFractionBypass();
            } else {
                calculateAppearFraction = 0.0f;
            }
            f = (float) (-this.mQs.getQsMinExpansionHeight());
        }
        return Math.min(0.0f, MathUtils.lerp(f, 0.0f, Math.min(1.0f, calculateAppearFraction)) + this.mExpandOffset);
    }

    /* access modifiers changed from: protected */
    public float getKeyguardContentsAlpha() {
        float f;
        float f2;
        if (this.mBarState == 1) {
            f2 = getExpandedHeight();
            f = (float) (this.mKeyguardStatusBar.getHeight() + this.mNotificationsHeaderCollideDistance);
        } else {
            f2 = getExpandedHeight();
            f = (float) this.mKeyguardStatusBar.getHeight();
        }
        return (float) Math.pow((double) MathUtils.saturate(f2 / f), 0.75d);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHeaderKeyguardAlpha() {
        if (this.mKeyguardShowing) {
            float min = Math.min(getKeyguardContentsAlpha(), 1.0f - Math.min(1.0f, getQsExpansionFraction() * 2.0f)) * this.mKeyguardStatusBarAnimateAlpha * (1.0f - this.mKeyguardHeadsUpShowingAmount);
            this.mKeyguardStatusBar.setAlpha(min);
            int i = 0;
            boolean z = (this.mFirstBypassAttempt && this.mUpdateMonitor.shouldListenForFace()) || this.mDelayShowingKeyguardStatusBar;
            KeyguardStatusBarView keyguardStatusBarView = this.mKeyguardStatusBar;
            if (min == 0.0f || this.mDozing || z) {
                i = 4;
            }
            keyguardStatusBarView.setVisibility(i);
        }
    }

    private void updateKeyguardBottomAreaAlpha() {
        float min = Math.min(MathUtils.map(isUnlockHintRunning() ? 0.0f : 0.95f, 1.0f, 0.0f, 1.0f, getExpandedFraction()), 1.0f - getQsExpansionFraction()) * this.mBottomAreaShadeAlpha;
        ((KeyguardBottomAreaInjector) Dependency.get(KeyguardBottomAreaInjector.class)).setAlpha(1.0f);
        this.mKeyguardBottomArea.setImportantForAccessibility(min == 0.0f ? 4 : 0);
        View ambientIndicationContainer = this.mStatusBar.getAmbientIndicationContainer();
        if (ambientIndicationContainer != null) {
            ambientIndicationContainer.setAlpha(min);
        }
    }

    private void updateBigClockAlpha() {
        this.mBigClockContainer.setAlpha(Math.min(MathUtils.map(isUnlockHintRunning() ? 0.0f : 0.95f, 1.0f, 0.0f, 1.0f, getExpandedFraction()), 1.0f - getQsExpansionFraction()));
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void onExpandingStarted() {
        super.onExpandingStarted();
        this.mNotificationStackScroller.onExpansionStarted();
        this.mIsExpanding = true;
        boolean z = this.mQsFullyExpanded;
        this.mQsExpandedWhenExpandingStarted = z;
        this.mMediaHierarchyManager.setCollapsingShadeFromQS(z && !this.mAnimatingQS);
        if (this.mQsExpanded) {
            onQsExpansionStarted();
        }
        QS qs = this.mQs;
        if (qs != null) {
            qs.setHeaderListening(true);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void onExpandingFinished() {
        super.onExpandingFinished();
        this.mNotificationStackScroller.onExpansionStopped();
        this.mHeadsUpManager.onExpandingFinished();
        this.mConversationNotificationManager.onNotificationPanelExpandStateChanged(isFullyCollapsed());
        this.mIsExpanding = false;
        this.mMediaHierarchyManager.setCollapsingShadeFromQS(false);
        if (isFullyCollapsed()) {
            DejankUtils.postAfterTraversal(new Runnable() {
                /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass17 */

                public void run() {
                    NotificationPanelViewController.this.setListening(false);
                    NotificationPanelViewController.this.updateAwePauseResumeStatus();
                }
            });
            this.mView.postOnAnimation(new Runnable() {
                /* class com.android.systemui.statusbar.phone.NotificationPanelViewController.AnonymousClass18 */

                public void run() {
                    NotificationPanelViewController.this.mView.getParent().invalidateChild(NotificationPanelViewController.this.mView, NotificationPanelViewController.M_DUMMY_DIRTY_RECT);
                }
            });
        } else {
            setListening(true);
        }
        this.mQsExpandImmediate = false;
        this.mNotificationStackScroller.setShouldShowShelfOnly(false);
        this.mTwoFingerQsExpandPossible = false;
        notifyListenersTrackingHeadsUp(null);
        this.mExpandingFromHeadsUp = false;
        setPanelScrimMinFraction(0.0f);
    }

    private void notifyListenersTrackingHeadsUp(ExpandableNotificationRow expandableNotificationRow) {
        for (int i = 0; i < this.mTrackingHeadsUpListeners.size(); i++) {
            this.mTrackingHeadsUpListeners.get(i).accept(expandableNotificationRow);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setListening(boolean z) {
        this.mKeyguardStatusBar.setListening(z);
        QS qs = this.mQs;
        if (qs != null) {
            qs.setListening(z);
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void expand(boolean z) {
        super.expand(z);
        setListening(true);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void setOverExpansion(float f, boolean z) {
        if (!this.mConflictingQsExpansionGesture && !this.mQsExpandImmediate && this.mBarState != 1) {
            this.mNotificationStackScroller.setOnHeightChangedListener(null);
            if (z) {
                this.mNotificationStackScroller.setOverScrolledPixels(f, true, false);
            } else {
                this.mNotificationStackScroller.setOverScrollAmount(f, true, false);
            }
            this.mNotificationStackScroller.setOnHeightChangedListener(this.mOnHeightChangedListener);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void onTrackingStarted() {
        this.mFalsingManager.onTrackingStarted(!this.mKeyguardStateController.canDismissLockScreen());
        super.onTrackingStarted();
        if (this.mQsFullyExpanded) {
            this.mQsExpandImmediate = true;
            this.mNotificationStackScroller.setShouldShowShelfOnly(true);
        }
        int i = this.mBarState;
        if (i == 1 || i == 2) {
            this.mAffordanceHelper.animateHideLeftRightIcon();
        }
        this.mNotificationStackScroller.onPanelTrackingStarted();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void onTrackingStopped(boolean z) {
        this.mFalsingManager.onTrackingStopped();
        super.onTrackingStopped(z);
        if (z) {
            this.mNotificationStackScroller.setOverScrolledPixels(0.0f, true, true);
        }
        this.mNotificationStackScroller.onPanelTrackingStopped();
        if (z) {
            int i = this.mBarState;
            if ((i == 1 || i == 2) && !this.mHintAnimationRunning) {
                this.mAffordanceHelper.reset(true);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMaxHeadsUpTranslation() {
        this.mNotificationStackScroller.setHeadsUpBoundaries(getHeight(), this.mNavigationBarBottomHeight);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void startUnlockHintAnimation() {
        if (this.mPowerManager.isPowerSaveMode()) {
            onUnlockHintStarted();
            onUnlockHintFinished();
            return;
        }
        super.startUnlockHintAnimation();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void onUnlockHintFinished() {
        super.onUnlockHintFinished();
        this.mNotificationStackScroller.setUnlockHintRunning(false);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void onUnlockHintStarted() {
        super.onUnlockHintStarted();
        this.mNotificationStackScroller.setUnlockHintRunning(true);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public float getPeekHeight() {
        int i;
        if (this.mNotificationStackScroller.getNotGoneChildCount() > 0) {
            i = this.mNotificationStackScroller.getPeekHeight();
        } else {
            i = this.mQsMinExpansionHeight;
        }
        return (float) i;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean shouldExpandToTopOfClearAll(float f) {
        if (super.shouldExpandToTopOfClearAll(f) && this.mNotificationStackScroller.calculateAppearFraction(f) >= 1.0f) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean shouldUseDismissingAnimation() {
        return this.mBarState != 0 && (this.mKeyguardStateController.canDismissLockScreen() || !isTracking());
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean fullyExpandedClearAllVisible() {
        return this.mNotificationStackScroller.isFooterViewNotGone() && this.mNotificationStackScroller.isScrolledToBottom() && !this.mQsExpandImmediate;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean isClearAllVisible() {
        return this.mNotificationStackScroller.isFooterViewContentVisible();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public int getClearAllHeightWithPadding() {
        return this.mNotificationStackScroller.getFooterViewHeightWithPadding();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean isTrackingBlocked() {
        return (this.mConflictingQsExpansionGesture && this.mQsExpanded) || this.mBlockingExpansionForCurrentTouch;
    }

    public boolean isQsExpanded() {
        return this.mQsExpanded;
    }

    public boolean isQsDetailShowing() {
        return this.mQs.isShowingDetail();
    }

    public void closeQsDetail() {
        this.mQs.closeDetail();
    }

    public boolean isLaunchTransitionFinished() {
        return this.mIsLaunchTransitionFinished;
    }

    public boolean isLaunchTransitionRunning() {
        return this.mIsLaunchTransitionRunning;
    }

    public void setLaunchTransitionRunning(boolean z) {
        this.mIsLaunchTransitionRunning = z;
    }

    public void setLaunchTransitionEndRunnable(Runnable runnable) {
        this.mLaunchAnimationEndRunnable = runnable;
    }

    public Runnable getLaunchAnimationEndRunnable() {
        return this.mLaunchAnimationEndRunnable;
    }

    public FalsingManager getFalsingManager() {
        return this.mFalsingManager;
    }

    public String getLastCameraLaunchSource() {
        return this.mLastCameraLaunchSource;
    }

    public LockscreenGestureLogger getLockscreenGestureLogger() {
        return this.mLockscreenGestureLogger;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDozingVisibilities(boolean z) {
        this.mKeyguardBottomArea.setDozing(this.mDozing, z);
        if (!this.mDozing && z) {
            animateKeyguardStatusBarIn(300);
        }
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean isDozing() {
        return this.mDozing;
    }

    public void showEmptyShadeView(boolean z) {
        this.mShowEmptyShadeView = z;
        updateEmptyShadeView();
    }

    private void updateEmptyShadeView() {
        this.mNotificationStackScroller.updateEmptyShadeView(this.mShowEmptyShadeView && !this.mQsExpanded);
    }

    public void setQsScrimEnabled(boolean z) {
        boolean z2 = this.mQsScrimEnabled != z;
        this.mQsScrimEnabled = z;
        if (z2) {
            updateQsState();
        }
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        this.mKeyguardUserSwitcher = keyguardUserSwitcher;
    }

    public void onScreenTurningOn() {
        this.mKeyguardStatusView.dozeTimeTick();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean onMiddleClicked() {
        int i = this.mBarState;
        if (i == 0) {
            this.mView.post(this.mPostCollapseRunnable);
            return false;
        } else if (i != 1) {
            if (i == 2 && !this.mQsExpanded) {
                this.mStatusBarStateController.setState(1);
            }
            return true;
        } else {
            if (!this.mDozingOnDown && !this.mKeyguardBypassController.getBypassEnabled()) {
                this.mLockscreenGestureLogger.write(188, 0, 0);
                this.mLockscreenGestureLogger.log(LockscreenGestureLogger.LockscreenUiEvent.LOCKSCREEN_LOCK_SHOW_HINT);
                startUnlockHintAnimation();
            }
            return true;
        }
    }

    public void setPanelAlpha(int i, boolean z) {
        if (this.mPanelAlpha != i) {
            this.mPanelAlpha = i;
            PropertyAnimator.setProperty(this.mView, this.mPanelAlphaAnimator, (float) i, i == 255 ? this.mPanelAlphaInPropertiesAnimator : this.mPanelAlphaOutPropertiesAnimator, z);
        }
    }

    public void setPanelAlphaEndAction(Runnable runnable) {
        this.mPanelAlphaEndAction = runnable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateKeyguardStatusBarForHeadsUp() {
        boolean z = this.mKeyguardShowing && this.mHeadsUpAppearanceController.shouldBeVisible();
        if (this.mShowingKeyguardHeadsUp != z) {
            this.mShowingKeyguardHeadsUp = z;
            float f = 0.0f;
            if (this.mKeyguardShowing) {
                NotificationPanelView notificationPanelView = this.mView;
                AnimatableProperty animatableProperty = this.KEYGUARD_HEADS_UP_SHOWING_AMOUNT;
                if (z) {
                    f = 1.0f;
                }
                PropertyAnimator.setProperty(notificationPanelView, animatableProperty, f, KEYGUARD_HUN_PROPERTIES, true);
                return;
            }
            PropertyAnimator.applyImmediately(this.mView, this.KEYGUARD_HEADS_UP_SHOWING_AMOUNT, 0.0f);
        }
    }

    private void setKeyguardHeadsUpShowingAmount(float f) {
        this.mKeyguardHeadsUpShowingAmount = f;
        updateHeaderKeyguardAlpha();
    }

    private float getKeyguardHeadsUpShowingAmount() {
        return this.mKeyguardHeadsUpShowingAmount;
    }

    public void setHeadsUpAnimatingAway(boolean z) {
        this.mHeadsUpAnimatingAway = z;
        this.mNotificationStackScroller.setHeadsUpAnimatingAway(z);
        updateHeadsUpVisibility();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateHeadsUpVisibility() {
        ((PhoneStatusBarView) this.mBar).setHeadsUpVisible(this.mHeadsUpAnimatingAway || this.mHeadsUpPinnedMode);
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void setHeadsUpManager(HeadsUpManagerPhone headsUpManagerPhone) {
        super.setHeadsUpManager(headsUpManagerPhone);
        this.mHeadsUpTouchHelper = new HeadsUpTouchHelper(headsUpManagerPhone, this.mNotificationStackScroller.getHeadsUpCallback(), this);
    }

    public void setTrackedHeadsUp(ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow != null) {
            notifyListenersTrackingHeadsUp(expandableNotificationRow);
            this.mExpandingFromHeadsUp = true;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void onClosingFinished() {
        super.onClosingFinished();
        resetHorizontalPanelPosition();
        setClosingWithAlphaFadeout(false);
    }

    private void setClosingWithAlphaFadeout(boolean z) {
        this.mClosingWithAlphaFadeOut = z;
        this.mNotificationStackScroller.forceNoOverlappingRendering(z);
    }

    /* access modifiers changed from: protected */
    public void updateVerticalPanelPosition(float f) {
        if (((float) this.mNotificationStackScroller.getWidth()) * 1.75f > ((float) this.mView.getWidth())) {
            resetHorizontalPanelPosition();
            return;
        }
        float width = (float) (this.mPositionMinSideMargin + (this.mNotificationStackScroller.getWidth() / 2));
        float width2 = (float) ((this.mView.getWidth() - this.mPositionMinSideMargin) - (this.mNotificationStackScroller.getWidth() / 2));
        if (Math.abs(f - ((float) (this.mView.getWidth() / 2))) < ((float) (this.mNotificationStackScroller.getWidth() / 4))) {
            f = (float) (this.mView.getWidth() / 2);
        }
        setHorizontalPanelTranslation(Math.min(width2, Math.max(width, f)) - ((float) (this.mNotificationStackScroller.getLeft() + (this.mNotificationStackScroller.getWidth() / 2))));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetHorizontalPanelPosition() {
        setHorizontalPanelTranslation(0.0f);
    }

    /* access modifiers changed from: protected */
    public void setHorizontalPanelTranslation(float f) {
        this.mNotificationStackScroller.setTranslationX(f);
        this.mQsFrame.setTranslationX(f);
        int size = this.mVerticalTranslationListener.size();
        for (int i = 0; i < size; i++) {
            this.mVerticalTranslationListener.get(i).run();
        }
    }

    /* access modifiers changed from: protected */
    public void updateExpandedHeight(float f) {
        if (this.mTracking) {
            this.mNotificationStackScroller.setExpandingVelocity(getCurrentExpandVelocity());
        }
        if (this.mKeyguardBypassController.getBypassEnabled() && isOnKeyguard()) {
            f = (float) getMaxPanelHeightNonBypass();
        }
        this.mNotificationStackScroller.setExpandedHeight(f);
        updateBigClockAlpha();
        updateStatusBarIcons();
    }

    public boolean isFullWidth() {
        return this.mIsFullWidth;
    }

    private void updateStatusBarIcons() {
        boolean z = (isPanelVisibleBecauseOfHeadsUp() || isFullWidth()) && getExpandedHeight() < getOpeningHeight();
        if (z && isOnKeyguard()) {
            z = false;
        }
        if (z != this.mShowIconsWhenExpanded) {
            this.mShowIconsWhenExpanded = z;
            this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, false);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isOnKeyguard() {
        return this.mBarState == 1;
    }

    public void setPanelScrimMinFraction(float f) {
        this.mBar.panelScrimMinFractionChanged(f);
    }

    public void clearNotificationEffects() {
        this.mStatusBar.clearNotificationEffects();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public boolean isPanelVisibleBecauseOfHeadsUp() {
        return (this.mHeadsUpManager.hasPinnedHeadsUp() || this.mHeadsUpAnimatingAway) && this.mBarState == 0;
    }

    public void launchCamera(boolean z, int i) {
        boolean z2 = true;
        if (i == 1) {
            this.mLastCameraLaunchSource = "power_double_tap";
        } else if (i == 0) {
            this.mLastCameraLaunchSource = "wiggle_gesture";
        } else if (i == 2) {
            this.mLastCameraLaunchSource = "lift_to_launch_ml";
        } else {
            this.mLastCameraLaunchSource = "lockscreen_affordance";
        }
        if (!isFullyCollapsed()) {
            setLaunchingAffordance(true);
        } else {
            z = false;
        }
        this.mAffordanceHasPreview = this.mKeyguardBottomArea.getRightPreview() != null;
        KeyguardAffordanceHelper keyguardAffordanceHelper = this.mAffordanceHelper;
        if (this.mView.getLayoutDirection() != 1) {
            z2 = false;
        }
        keyguardAffordanceHelper.launchAffordance(z, z2);
    }

    public void onAffordanceLaunchEnded() {
        setLaunchingAffordance(false);
    }

    private void setLaunchingAffordance(boolean z) {
        this.mLaunchingAffordance = z;
        this.mKeyguardAffordanceHelperCallback.getLeftIcon().setLaunchingAffordance(z);
        this.mKeyguardAffordanceHelperCallback.getRightIcon().setLaunchingAffordance(z);
        this.mKeyguardBypassController.setLaunchingAffordance(z);
        Consumer<Boolean> consumer = this.mAffordanceLaunchListener;
        if (consumer != null) {
            consumer.accept(Boolean.valueOf(z));
        }
    }

    public boolean isLaunchingAffordanceWithPreview() {
        return this.mLaunchingAffordance && this.mAffordanceHasPreview;
    }

    public boolean canCameraGestureBeLaunched() {
        ActivityInfo activityInfo;
        if (!this.mStatusBar.isCameraAllowedByAdmin()) {
            return false;
        }
        ResolveInfo resolveCameraIntent = this.mKeyguardBottomArea.resolveCameraIntent();
        String str = (resolveCameraIntent == null || (activityInfo = resolveCameraIntent.activityInfo) == null) ? null : activityInfo.packageName;
        if (str == null) {
            return false;
        }
        if ((this.mBarState != 0 || !isForegroundApp(str)) && !this.mAffordanceHelper.isSwipingInProgress()) {
            return true;
        }
        return false;
    }

    private boolean isForegroundApp(String str) {
        List<ActivityManager.RunningTaskInfo> runningTasks = this.mActivityManager.getRunningTasks(1);
        if (runningTasks.isEmpty() || !str.equals(runningTasks.get(0).topActivity.getPackageName())) {
            return false;
        }
        return true;
    }

    private void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
    }

    public boolean hideStatusBarIconsWhenExpanded() {
        if (this.mLaunchingNotification) {
            return this.mHideIconsDuringNotificationLaunch;
        }
        HeadsUpAppearanceController headsUpAppearanceController = this.mHeadsUpAppearanceController;
        if (headsUpAppearanceController != null && headsUpAppearanceController.shouldBeVisible()) {
            return false;
        }
        if (!isFullWidth() || !this.mShowIconsWhenExpanded) {
            return true;
        }
        return false;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void setTouchAndAnimationDisabled(boolean z) {
        super.setTouchAndAnimationDisabled(z);
        if (z && this.mAffordanceHelper.isSwipingInProgress() && !this.mIsLaunchTransitionRunning) {
            this.mAffordanceHelper.reset(false);
        }
        this.mNotificationStackScroller.setAnimationsEnabled(!z);
    }

    public void setDozing(boolean z, boolean z2, PointF pointF) {
        if (z != this.mDozing) {
            this.mView.setDozing(z);
            this.mDozing = z;
            this.mNotificationStackScroller.setDozing(z, z2, pointF);
            this.mKeyguardBottomArea.setDozing(this.mDozing, z2);
            if (z) {
                this.mBottomAreaShadeAlphaAnimator.cancel();
            }
            int i = this.mBarState;
            if (i == 1 || i == 2) {
                updateDozingVisibilities(z2);
            }
            this.mStatusBarStateController.setDozeAmount(z ? 1.0f : 0.0f, z2);
        }
    }

    public void setPulsing(boolean z) {
        this.mPulsing = z;
        boolean z2 = !this.mDozeParameters.getDisplayNeedsBlanking() && this.mDozeParameters.getAlwaysOn();
        if (z2) {
            this.mAnimateNextPositionUpdate = true;
        }
        if (!this.mPulsing && !this.mDozing) {
            this.mAnimateNextPositionUpdate = false;
        }
        this.mNotificationStackScroller.setPulsing(z, z2);
        this.mKeyguardStatusView.setPulsing(z);
    }

    public void dozeTimeTick() {
        this.mKeyguardBottomArea.dozeTimeTick();
        this.mKeyguardStatusView.dozeTimeTick();
        if (this.mInterpolatedDarkAmount > 0.0f) {
            positionClockAndNotifications();
        }
    }

    public void setStatusAccessibilityImportance(int i) {
        this.mKeyguardStatusView.setImportantForAccessibility(i);
    }

    public void setUserSetupComplete(boolean z) {
        this.mUserSetupComplete = z;
        this.mKeyguardBottomArea.setUserSetupComplete(z);
    }

    public void applyExpandAnimationParams(ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters) {
        this.mExpandOffset = expandAnimationParameters != null ? (float) expandAnimationParameters.getTopChange() : 0.0f;
        updateQsExpansion();
        if (expandAnimationParameters != null) {
            boolean z = expandAnimationParameters.getProgress(14, 100) == 0.0f;
            if (z != this.mHideIconsDuringNotificationLaunch) {
                this.mHideIconsDuringNotificationLaunch = z;
                if (!z) {
                    this.mCommandQueue.recomputeDisableFlags(this.mDisplayId, true);
                }
            }
        }
    }

    public void addTrackingHeadsUpListener(Consumer<ExpandableNotificationRow> consumer) {
        this.mTrackingHeadsUpListeners.add(consumer);
    }

    public void removeTrackingHeadsUpListener(Consumer<ExpandableNotificationRow> consumer) {
        this.mTrackingHeadsUpListeners.remove(consumer);
    }

    public void addVerticalTranslationListener(Runnable runnable) {
        this.mVerticalTranslationListener.add(runnable);
    }

    public void removeVerticalTranslationListener(Runnable runnable) {
        this.mVerticalTranslationListener.remove(runnable);
    }

    public void setHeadsUpAppearanceController(HeadsUpAppearanceController headsUpAppearanceController) {
        this.mHeadsUpAppearanceController = headsUpAppearanceController;
    }

    public void blockExpansionForCurrentTouch() {
        this.mBlockingExpansionForCurrentTouch = this.mTracking;
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(fileDescriptor, printWriter, strArr);
        printWriter.println("    gestureExclusionRect: " + calculateGestureExclusionRect());
        KeyguardStatusBarView keyguardStatusBarView = this.mKeyguardStatusBar;
        if (keyguardStatusBarView != null) {
            keyguardStatusBarView.dump(fileDescriptor, printWriter, strArr);
        }
        KeyguardStatusView keyguardStatusView = this.mKeyguardStatusView;
        if (keyguardStatusView != null) {
            keyguardStatusView.dump(fileDescriptor, printWriter, strArr);
        }
    }

    public boolean hasActiveClearableNotifications() {
        return this.mNotificationStackScroller.hasActiveClearableNotifications(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateShowEmptyShadeView() {
        boolean z = true;
        if (this.mBarState == 1 || this.mEntryManager.hasActiveNotifications()) {
            z = false;
        }
        showEmptyShadeView(z);
    }

    public RemoteInputController.Delegate createRemoteInputDelegate() {
        return this.mNotificationStackScroller.createDelegate();
    }

    /* access modifiers changed from: package-private */
    public void updateNotificationViews(String str) {
        this.mNotificationStackScroller.updateSectionBoundaries(str);
        this.mNotificationStackScroller.updateSpeedBumpIndex();
        this.mNotificationStackScroller.updateFooter();
        updateShowEmptyShadeView();
        this.mNotificationStackScroller.updateIconAreaViews();
    }

    public void onUpdateRowStates() {
        this.mNotificationStackScroller.onUpdateRowStates();
    }

    public boolean hasPulsingNotifications() {
        return this.mNotificationStackScroller.hasPulsingNotifications();
    }

    public ActivatableNotificationView getActivatedChild() {
        return this.mNotificationStackScroller.getActivatedChild();
    }

    public void setActivatedChild(ActivatableNotificationView activatableNotificationView) {
        this.mNotificationStackScroller.setActivatedChild(activatableNotificationView);
    }

    public void runAfterAnimationFinished(Runnable runnable) {
        this.mNotificationStackScroller.runAfterAnimationFinished(runnable);
    }

    public void initDependencies(StatusBar statusBar, NotificationGroupManager notificationGroupManager, NotificationShelf notificationShelf, NotificationIconAreaController notificationIconAreaController, ScrimController scrimController) {
        setStatusBar(statusBar);
        setGroupManager(this.mGroupManager);
        this.mNotificationStackScroller.setNotificationPanelController(this);
        this.mNotificationStackScroller.setIconAreaController(notificationIconAreaController);
        this.mNotificationStackScroller.setStatusBar(statusBar);
        this.mNotificationStackScroller.setGroupManager(notificationGroupManager);
        this.mNotificationStackScroller.setShelf(notificationShelf);
        this.mNotificationStackScroller.setScrimController(scrimController);
        updateShowEmptyShadeView();
    }

    public void showTransientIndication(int i) {
        this.mKeyguardIndicationController.showTransientIndication(i);
    }

    public void setOnReinflationListener(Runnable runnable) {
        this.mOnReinflationListener = runnable;
    }

    public void setAlpha(float f) {
        this.mView.setAlpha(f);
    }

    public ViewPropertyAnimator fadeOut(long j, long j2, Runnable runnable) {
        return this.mView.animate().alpha(0.0f).setStartDelay(j).setDuration(j2).setInterpolator(Interpolators.ALPHA_OUT).withLayer().withEndAction(runnable);
    }

    public void resetViewGroupFade() {
        ViewGroupFadeHelper.reset(this.mView);
    }

    public void addOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        this.mView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public void removeOnGlobalLayoutListener(ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener) {
        this.mView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public MyOnHeadsUpChangedListener getOnHeadsUpChangedListener() {
        return this.mOnHeadsUpChangedListener;
    }

    public int getHeight() {
        return this.mView.getHeight();
    }

    public void onThemeChanged() {
        this.mConfigurationListener.onThemeChanged();
    }

    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public OnLayoutChangeListener createLayoutChangeListener() {
        return new OnLayoutChangeListener();
    }

    public void setEmptyDragAmount(float f) {
        this.mExpansionCallback.setEmptyDragAmount(f);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public PanelViewController.TouchHandler createTouchHandler() {
        return new NotificationPanelTouchHandler();
    }

    public void setBlockTouch(boolean z) {
        this.mBlockTouches = z;
    }

    public void setLaunchTransitionFinished(boolean z) {
        this.mIsLaunchTransitionFinished = z;
    }

    public final void setLaunchAnimationEndRunnable(Runnable runnable) {
        this.mLaunchAnimationEndRunnable = runnable;
    }

    public class NotificationPanelTouchHandler extends PanelViewController.TouchHandler {
        /* access modifiers changed from: protected */
        public boolean handleMiuiTouch(MotionEvent motionEvent) {
            return false;
        }

        /* access modifiers changed from: protected */
        public boolean onMiuiIntercept(MotionEvent motionEvent) {
            return false;
        }

        public NotificationPanelTouchHandler() {
            super();
        }

        @Override // com.android.systemui.statusbar.phone.PanelViewController.TouchHandler
        public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            if (notificationPanelViewController.mClosing || notificationPanelViewController.mBlockTouches || (NotificationPanelViewController.this.mQsFullyExpanded && NotificationPanelViewController.this.mQs.disallowPanelTouches())) {
                Log.d(PanelViewController.TAG, "NotificationPanelView not intercept");
                return false;
            }
            NotificationPanelViewController.this.initDownStates(motionEvent);
            if (NotificationPanelViewController.this.mStatusBar.isBouncerShowing()) {
                return true;
            }
            if (!NotificationPanelViewController.this.mBar.panelEnabled() || !NotificationPanelViewController.this.mHeadsUpTouchHelper.onInterceptTouchEvent(motionEvent)) {
                NotificationPanelViewController notificationPanelViewController2 = NotificationPanelViewController.this;
                if ((!notificationPanelViewController2.shouldQuickSettingsIntercept(notificationPanelViewController2.mDownX, NotificationPanelViewController.this.mDownY, 0.0f) && NotificationPanelViewController.this.mPulseExpansionHandler.onInterceptTouchEvent(motionEvent)) || onMiuiIntercept(motionEvent)) {
                    return true;
                }
                if (NotificationPanelViewController.this.isFullyCollapsed() || !NotificationPanelViewController.this.onQsIntercept(motionEvent)) {
                    return super.onInterceptTouchEvent(motionEvent);
                }
                return true;
            }
            NotificationPanelViewController.this.mMetricsLogger.count("panel_open", 1);
            NotificationPanelViewController.this.mMetricsLogger.count("panel_open_peek", 1);
            return true;
        }

        /* JADX WARNING: Removed duplicated region for block: B:49:0x00cf A[RETURN] */
        /* JADX WARNING: Removed duplicated region for block: B:50:0x00d0  */
        @Override // com.android.systemui.statusbar.phone.PanelViewController.TouchHandler
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean onTouch(android.view.View r7, android.view.MotionEvent r8) {
            /*
            // Method dump skipped, instructions count: 299
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationPanelViewController.NotificationPanelTouchHandler.onTouch(android.view.View, android.view.MotionEvent):boolean");
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.PanelViewController
    public PanelViewController.OnConfigurationChangedListener createOnConfigurationChangedListener() {
        return new OnConfigurationChangedListener();
    }

    /* access modifiers changed from: private */
    public class OnHeightChangedListener implements ExpandableView.OnHeightChangedListener {
        @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
        public void onReset(ExpandableView expandableView) {
        }

        private OnHeightChangedListener() {
        }

        @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
        public void onHeightChanged(ExpandableView expandableView, boolean z) {
            if (expandableView != null || !NotificationPanelViewController.this.mQsExpanded) {
                if (z && NotificationPanelViewController.this.mInterpolatedDarkAmount == 0.0f) {
                    NotificationPanelViewController.this.mAnimateNextPositionUpdate = true;
                }
                ExpandableView firstChildNotGone = NotificationPanelViewController.this.mNotificationStackScroller.getFirstChildNotGone();
                ExpandableNotificationRow expandableNotificationRow = firstChildNotGone instanceof ExpandableNotificationRow ? (ExpandableNotificationRow) firstChildNotGone : null;
                if (expandableNotificationRow != null && (expandableView == expandableNotificationRow || expandableNotificationRow.getNotificationParent() == expandableNotificationRow)) {
                    NotificationPanelViewController.this.requestScrollerTopPaddingUpdate(false);
                }
                NotificationPanelViewController.this.requestPanelHeightUpdate();
            }
        }
    }

    /* access modifiers changed from: private */
    public class OnClickListener implements View.OnClickListener {
        private OnClickListener() {
        }

        public void onClick(View view) {
            NotificationPanelViewController.this.onQsExpansionStarted();
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            if (notificationPanelViewController.mQsExpanded) {
                notificationPanelViewController.flingSettings(0.0f, 1, null, true);
            } else if (notificationPanelViewController.mQsExpansionEnabled) {
                notificationPanelViewController.mLockscreenGestureLogger.write(195, 0, 0);
                NotificationPanelViewController.this.flingSettings(0.0f, 0, null, true);
            }
        }
    }

    /* access modifiers changed from: private */
    public class OnOverscrollTopChangedListener implements NotificationStackScrollLayout.OnOverscrollTopChangedListener {
        private OnOverscrollTopChangedListener() {
        }

        @Override // com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.OnOverscrollTopChangedListener
        public void onOverscrollTopChanged(float f, boolean z) {
            NotificationPanelViewController.this.cancelQsAnimation();
            if (!NotificationPanelViewController.this.mQsExpansionEnabled) {
                f = 0.0f;
            }
            if (f < 1.0f) {
                f = 0.0f;
            }
            int i = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
            boolean z2 = true;
            NotificationPanelViewController.this.setOverScrolling(i != 0 && z);
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            if (i == 0) {
                z2 = false;
            }
            notificationPanelViewController.mQsExpansionFromOverscroll = z2;
            NotificationPanelViewController.this.mLastOverscroll = f;
            NotificationPanelViewController.this.updateQsState();
            NotificationPanelViewController notificationPanelViewController2 = NotificationPanelViewController.this;
            notificationPanelViewController2.setQsExpansion(((float) notificationPanelViewController2.mQsMinExpansionHeight) + f);
        }

        @Override // com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.OnOverscrollTopChangedListener
        public void flingTopOverscroll(float f, boolean z) {
            NotificationPanelViewController.this.mLastOverscroll = 0.0f;
            NotificationPanelViewController.this.mQsExpansionFromOverscroll = false;
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.setQsExpansion(notificationPanelViewController.mQsExpansionHeight);
            NotificationPanelViewController notificationPanelViewController2 = NotificationPanelViewController.this;
            if (!notificationPanelViewController2.mQsExpansionEnabled && z) {
                f = 0.0f;
            }
            notificationPanelViewController2.flingSettings(f, (!z || !NotificationPanelViewController.this.mQsExpansionEnabled) ? 1 : 0, new Runnable() {
                /* class com.android.systemui.statusbar.phone.$$Lambda$NotificationPanelViewController$OnOverscrollTopChangedListener$6FaWnl4RjuYk8pBm1fvFXqN0qu8 */

                public final void run() {
                    NotificationPanelViewController.OnOverscrollTopChangedListener.this.lambda$flingTopOverscroll$0$NotificationPanelViewController$OnOverscrollTopChangedListener();
                }
            }, false);
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$flingTopOverscroll$0 */
        public /* synthetic */ void lambda$flingTopOverscroll$0$NotificationPanelViewController$OnOverscrollTopChangedListener() {
            NotificationPanelViewController.this.mStackScrollerOverscrolling = false;
            NotificationPanelViewController.this.setOverScrolling(false);
            NotificationPanelViewController.this.updateQsState();
        }
    }

    private class DynamicPrivacyControlListener implements DynamicPrivacyController.Listener {
        private DynamicPrivacyControlListener() {
        }

        @Override // com.android.systemui.statusbar.notification.DynamicPrivacyController.Listener
        public void onDynamicPrivacyChanged() {
            if (NotificationPanelViewController.this.mLinearDarkAmount == 0.0f) {
                NotificationPanelViewController.this.mAnimateNextPositionUpdate = true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class KeyguardAffordanceHelperCallback implements KeyguardAffordanceHelper.Callback {
        private KeyguardAffordanceHelperCallback() {
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public KeyguardAffordanceView getLeftIcon() {
            return NotificationPanelViewController.this.mView.getLayoutDirection() == 1 ? NotificationPanelViewController.this.mKeyguardBottomArea.getRightView() : NotificationPanelViewController.this.mKeyguardBottomArea.getLeftView();
        }

        @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper.Callback
        public KeyguardAffordanceView getRightIcon() {
            return NotificationPanelViewController.this.mView.getLayoutDirection() == 1 ? NotificationPanelViewController.this.mKeyguardBottomArea.getLeftView() : NotificationPanelViewController.this.mKeyguardBottomArea.getRightView();
        }

        public boolean needsAntiFalsing() {
            return NotificationPanelViewController.this.mBarState == 1;
        }
    }

    /* access modifiers changed from: private */
    public class OnEmptySpaceClickListener implements NotificationStackScrollLayout.OnEmptySpaceClickListener {
        private OnEmptySpaceClickListener() {
        }

        @Override // com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.OnEmptySpaceClickListener
        public void onEmptySpaceClicked(float f, float f2) {
            NotificationPanelViewController.this.onEmptySpaceClick(f);
        }
    }

    /* access modifiers changed from: private */
    public class MyOnHeadsUpChangedListener implements OnHeadsUpChangedListener {
        private MyOnHeadsUpChangedListener() {
        }

        @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
        public void onHeadsUpPinnedModeChanged(boolean z) {
            NotificationPanelViewController.this.mNotificationStackScroller.setInHeadsUpPinnedMode(z);
            if (z) {
                NotificationPanelViewController.this.mHeadsUpExistenceChangedRunnable.run();
                NotificationPanelViewController.this.updateNotificationTranslucency();
            } else {
                NotificationPanelViewController.this.setHeadsUpAnimatingAway(true);
                NotificationPanelViewController.this.mNotificationStackScroller.runAfterAnimationFinished(NotificationPanelViewController.this.mHeadsUpExistenceChangedRunnable);
            }
            NotificationPanelViewController.this.updateGestureExclusionRect();
            NotificationPanelViewController.this.mHeadsUpPinnedMode = z;
            NotificationPanelViewController.this.updateHeadsUpVisibility();
            NotificationPanelViewController.this.updateKeyguardStatusBarForHeadsUp();
        }

        @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
        public void onHeadsUpPinned(NotificationEntry notificationEntry) {
            if (!NotificationPanelViewController.this.isOnKeyguard()) {
                NotificationPanelViewController.this.mNotificationStackScroller.generateHeadsUpAnimation(notificationEntry.getHeadsUpAnimationView(), true);
            }
        }

        @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
        public void onHeadsUpUnPinned(NotificationEntry notificationEntry) {
            if (NotificationPanelViewController.this.isFullyCollapsed() && notificationEntry.isRowHeadsUp() && !NotificationPanelViewController.this.isOnKeyguard()) {
                NotificationPanelViewController.this.mNotificationStackScroller.generateHeadsUpAnimation(notificationEntry.getHeadsUpAnimationView(), false);
                notificationEntry.setHeadsUpIsVisible();
            }
        }

        @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
        public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
            NotificationPanelViewController.this.mNotificationStackScroller.generateHeadsUpAnimation(notificationEntry, z);
        }
    }

    /* access modifiers changed from: private */
    public class HeightListener implements QS.HeightListener {
        private HeightListener() {
        }

        @Override // com.android.systemui.plugins.qs.QS.HeightListener
        public void onQsHeightChanged() {
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            QS qs = notificationPanelViewController.mQs;
            notificationPanelViewController.mQsMaxExpansionHeight = qs != null ? qs.getDesiredHeight() : 0;
            NotificationPanelViewController notificationPanelViewController2 = NotificationPanelViewController.this;
            if (notificationPanelViewController2.mQsExpanded && notificationPanelViewController2.mQsFullyExpanded) {
                NotificationPanelViewController notificationPanelViewController3 = NotificationPanelViewController.this;
                notificationPanelViewController3.mQsExpansionHeight = (float) notificationPanelViewController3.mQsMaxExpansionHeight;
                NotificationPanelViewController.this.requestScrollerTopPaddingUpdate(false);
                NotificationPanelViewController.this.requestPanelHeightUpdate();
            }
            if (NotificationPanelViewController.this.mAccessibilityManager.isEnabled()) {
                NotificationPanelViewController notificationPanelViewController4 = NotificationPanelViewController.this;
                notificationPanelViewController4.mView.setAccessibilityPaneTitle(notificationPanelViewController4.determineAccessibilityPaneTitle());
            }
            NotificationStackScrollLayout notificationStackScrollLayout = NotificationPanelViewController.this.mNotificationStackScroller;
            NotificationPanelViewController notificationPanelViewController5 = NotificationPanelViewController.this;
            notificationStackScrollLayout.setMaxTopPadding(notificationPanelViewController5.mQsMaxExpansionHeight + notificationPanelViewController5.mQsNotificationTopPadding);
        }
    }

    /* access modifiers changed from: private */
    public class ZenModeControllerCallback implements ZenModeController.Callback {
        private ZenModeControllerCallback(NotificationPanelViewController notificationPanelViewController) {
        }
    }

    /* access modifiers changed from: private */
    public class ConfigurationListener implements ConfigurationController.ConfigurationListener {
        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onUiModeChanged() {
        }

        private ConfigurationListener() {
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onDensityOrFontScaleChanged() {
            NotificationPanelViewController.this.updateShowEmptyShadeView();
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onThemeChanged() {
            int themeResId = NotificationPanelViewController.this.mView.getContext().getThemeResId();
            if (NotificationPanelViewController.this.mThemeResId != themeResId) {
                NotificationPanelViewController.this.mThemeResId = themeResId;
                NotificationPanelViewController.this.reInflateViews();
            }
        }

        @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
        public void onOverlayChanged() {
            NotificationPanelViewController.this.reInflateViews();
        }
    }

    public class StatusBarStateListener implements StatusBarStateController.StateListener {
        public StatusBarStateListener() {
        }

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onStateChanged(int i) {
            long j;
            boolean goingToFullShade = NotificationPanelViewController.this.mStatusBarStateController.goingToFullShade();
            boolean isKeyguardFadingAway = NotificationPanelViewController.this.mKeyguardStateController.isKeyguardFadingAway();
            int i2 = NotificationPanelViewController.this.mBarState;
            boolean z = i == 1;
            NotificationPanelViewController.this.setKeyguardStatusViewVisibility(i, isKeyguardFadingAway, goingToFullShade);
            NotificationPanelViewController.this.setKeyguardBottomAreaVisibility(i, goingToFullShade);
            if (z && !NotificationPanelViewController.this.mKeyguardShowing) {
                ((MiuiWallpaperClient) Dependency.get(MiuiWallpaperClient.class)).updateWallpaper(false);
            }
            NotificationPanelViewController.this.mBarState = i;
            NotificationPanelViewController.this.mKeyguardShowing = z;
            if (i2 == 1 && (goingToFullShade || i == 2)) {
                NotificationPanelViewController.this.animateKeyguardStatusBarOut();
                if (NotificationPanelViewController.this.mBarState == 2) {
                    j = 0;
                } else {
                    j = NotificationPanelViewController.this.mKeyguardStateController.calculateGoingToFullShadeDelay();
                }
                NotificationPanelViewController.this.mQs.animateHeaderSlidingIn(j);
            } else if (i2 == 2 && i == 1) {
                NotificationPanelViewController.this.animateKeyguardStatusBarIn(300);
                NotificationPanelViewController.this.mNotificationStackScroller.resetScrollPosition();
                NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
                if (!notificationPanelViewController.mQsExpanded) {
                    notificationPanelViewController.mQs.animateHeaderSlidingOut();
                }
            } else {
                NotificationPanelViewController.this.mKeyguardStatusBar.setAlpha(1.0f);
                NotificationPanelViewController.this.mKeyguardStatusBar.setVisibility(z ? 0 : 4);
                if (z && i2 != NotificationPanelViewController.this.mBarState) {
                    QS qs = NotificationPanelViewController.this.mQs;
                    if (qs != null) {
                        qs.hideImmediately();
                    }
                    NotificationPanelViewController.this.mKeyguardBottomArea.onKeyguardShowingChanged();
                }
            }
            NotificationPanelViewController.this.updateKeyguardStatusBarForHeadsUp();
            if (z) {
                NotificationPanelViewController.this.updateDozingVisibilities(false);
            }
            NotificationPanelViewController.this.updateQSPulseExpansion();
            NotificationPanelViewController.this.maybeAnimateBottomAreaAlpha();
            NotificationPanelViewController.this.resetHorizontalPanelPosition();
            NotificationPanelViewController.this.updateQsState();
        }

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onDozeAmountChanged(float f, float f2) {
            NotificationPanelViewController.this.mInterpolatedDarkAmount = f2;
            NotificationPanelViewController.this.mLinearDarkAmount = f;
            NotificationPanelViewController.this.mKeyguardStatusView.setDarkAmount(NotificationPanelViewController.this.mInterpolatedDarkAmount);
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.mKeyguardBottomArea.setDarkAmount(notificationPanelViewController.mInterpolatedDarkAmount);
            NotificationPanelViewController.this.positionClockAndNotifications();
        }
    }

    /* access modifiers changed from: private */
    public class ExpansionCallback implements PulseExpansionHandler.ExpansionCallback {
        private ExpansionCallback() {
        }

        @Override // com.android.systemui.statusbar.PulseExpansionHandler.ExpansionCallback
        public void setEmptyDragAmount(float f) {
            NotificationPanelViewController.this.mEmptyDragAmount = f * 0.2f;
            NotificationPanelViewController.this.positionClockAndNotifications();
        }
    }

    private class OnAttachStateChangeListener implements View.OnAttachStateChangeListener {
        private OnAttachStateChangeListener() {
        }

        public void onViewAttachedToWindow(View view) {
            FragmentHostManager.get(NotificationPanelViewController.this.mView).addTagListener(QS.TAG, NotificationPanelViewController.this.mFragmentListener);
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.mStatusBarStateController.addCallback(notificationPanelViewController.mStatusBarStateListener);
            NotificationPanelViewController.this.mZenModeController.addCallback(NotificationPanelViewController.this.mZenModeControllerCallback);
            NotificationPanelViewController.this.mConfigurationController.addCallback(NotificationPanelViewController.this.mConfigurationListener);
            NotificationPanelViewController notificationPanelViewController2 = NotificationPanelViewController.this;
            notificationPanelViewController2.mUpdateMonitor.registerCallback(notificationPanelViewController2.mKeyguardUpdateCallback);
            NotificationPanelViewController.this.mConfigurationListener.onThemeChanged();
            ((ControlPanelController) Dependency.get(ControlPanelController.class)).addCallback(NotificationPanelViewController.this.mUseControlPanelChangeListener);
        }

        public void onViewDetachedFromWindow(View view) {
            FragmentHostManager.get(NotificationPanelViewController.this.mView).removeTagListener(QS.TAG, NotificationPanelViewController.this.mFragmentListener);
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.mStatusBarStateController.removeCallback(notificationPanelViewController.mStatusBarStateListener);
            NotificationPanelViewController.this.mZenModeController.removeCallback(NotificationPanelViewController.this.mZenModeControllerCallback);
            NotificationPanelViewController.this.mConfigurationController.removeCallback(NotificationPanelViewController.this.mConfigurationListener);
            NotificationPanelViewController notificationPanelViewController2 = NotificationPanelViewController.this;
            notificationPanelViewController2.mUpdateMonitor.removeCallback(notificationPanelViewController2.mKeyguardUpdateCallback);
            ((ControlPanelController) Dependency.get(ControlPanelController.class)).removeCallback(NotificationPanelViewController.this.mUseControlPanelChangeListener);
        }
    }

    /* access modifiers changed from: private */
    public class OnLayoutChangeListener extends PanelViewController.OnLayoutChangeListener {
        private OnLayoutChangeListener() {
            super();
        }

        @Override // com.android.systemui.statusbar.phone.PanelViewController.OnLayoutChangeListener
        public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
            QS qs;
            DejankUtils.startDetectingBlockingIpcs("NVP#onLayout");
            super.onLayoutChange(view, i, i2, i3, i4, i5, i6, i7, i8);
            NotificationPanelViewController notificationPanelViewController = NotificationPanelViewController.this;
            notificationPanelViewController.setIsFullWidth(notificationPanelViewController.mNotificationStackScroller.getWidth() == NotificationPanelViewController.this.mView.getWidth());
            NotificationPanelViewController.this.mKeyguardStatusView.setPivotX((float) (NotificationPanelViewController.this.mView.getWidth() / 2));
            NotificationPanelViewController.this.mKeyguardStatusView.setPivotY(NotificationPanelViewController.this.mKeyguardStatusView.getClockTextSize() * 0.34521484f);
            NotificationPanelViewController notificationPanelViewController2 = NotificationPanelViewController.this;
            int i9 = notificationPanelViewController2.mQsMaxExpansionHeight;
            if (notificationPanelViewController2.mQs != null) {
                float f = (float) notificationPanelViewController2.mQsMinExpansionHeight;
                NotificationPanelViewController notificationPanelViewController3 = NotificationPanelViewController.this;
                notificationPanelViewController3.mQsMinExpansionHeight = notificationPanelViewController3.mKeyguardShowing ? 0 : notificationPanelViewController3.mQs.getQsMinExpansionHeight();
                if (NotificationPanelViewController.this.mQsExpansionHeight == f) {
                    NotificationPanelViewController notificationPanelViewController4 = NotificationPanelViewController.this;
                    notificationPanelViewController4.mQsExpansionHeight = (float) notificationPanelViewController4.mQsMinExpansionHeight;
                }
                NotificationPanelViewController notificationPanelViewController5 = NotificationPanelViewController.this;
                notificationPanelViewController5.mQsMaxExpansionHeight = notificationPanelViewController5.mQs.getDesiredHeight();
                NotificationStackScrollLayout notificationStackScrollLayout = NotificationPanelViewController.this.mNotificationStackScroller;
                NotificationPanelViewController notificationPanelViewController6 = NotificationPanelViewController.this;
                notificationStackScrollLayout.setMaxTopPadding(notificationPanelViewController6.mQsMaxExpansionHeight + notificationPanelViewController6.mQsNotificationTopPadding);
            }
            NotificationPanelViewController.this.positionClockAndNotifications();
            NotificationPanelViewController notificationPanelViewController7 = NotificationPanelViewController.this;
            if (!notificationPanelViewController7.mQsExpanded || !notificationPanelViewController7.mQsFullyExpanded) {
                NotificationPanelViewController notificationPanelViewController8 = NotificationPanelViewController.this;
                if (!notificationPanelViewController8.mQsExpanded) {
                    notificationPanelViewController8.setQsExpansion(((float) notificationPanelViewController8.mQsMinExpansionHeight) + NotificationPanelViewController.this.mLastOverscroll);
                }
            } else {
                NotificationPanelViewController notificationPanelViewController9 = NotificationPanelViewController.this;
                notificationPanelViewController9.mQsExpansionHeight = (float) notificationPanelViewController9.mQsMaxExpansionHeight;
                NotificationPanelViewController.this.requestScrollerTopPaddingUpdate(false);
                NotificationPanelViewController.this.requestPanelHeightUpdate();
                NotificationPanelViewController notificationPanelViewController10 = NotificationPanelViewController.this;
                int i10 = notificationPanelViewController10.mQsMaxExpansionHeight;
                if (i10 != i9) {
                    notificationPanelViewController10.startQsSizeChangeAnimation(i9, i10);
                }
            }
            NotificationPanelViewController notificationPanelViewController11 = NotificationPanelViewController.this;
            notificationPanelViewController11.updateExpandedHeight(notificationPanelViewController11.getExpandedHeight());
            NotificationPanelViewController.this.updateHeader();
            if (NotificationPanelViewController.this.mQsSizeChangeAnimator == null && (qs = NotificationPanelViewController.this.mQs) != null) {
                qs.setHeightOverride(qs.getDesiredHeight());
            }
            NotificationPanelViewController.this.updateMaxHeadsUpTranslation();
            NotificationPanelViewController.this.updateGestureExclusionRect();
            if (NotificationPanelViewController.this.mExpandAfterLayoutRunnable != null) {
                NotificationPanelViewController.this.mExpandAfterLayoutRunnable.run();
                NotificationPanelViewController.this.mExpandAfterLayoutRunnable = null;
            }
            DejankUtils.stopDetectingBlockingIpcs("NVP#onLayout");
        }
    }

    protected class OnConfigurationChangedListener extends PanelViewController.OnConfigurationChangedListener {
        protected OnConfigurationChangedListener() {
            super();
        }

        @Override // com.android.systemui.statusbar.phone.PanelViewController.OnConfigurationChangedListener, com.android.systemui.statusbar.phone.PanelView.OnConfigurationChangedListener
        public void onConfigurationChanged(Configuration configuration) {
            super.onConfigurationChanged(configuration);
            NotificationPanelViewController.this.mAffordanceHelper.onConfigurationChanged();
            if (configuration.orientation != NotificationPanelViewController.this.mLastOrientation) {
                NotificationPanelViewController.this.resetHorizontalPanelPosition();
            }
            NotificationPanelViewController.this.mLastOrientation = configuration.orientation;
        }
    }

    private class OnApplyWindowInsetsListener implements View.OnApplyWindowInsetsListener {
        private OnApplyWindowInsetsListener() {
        }

        public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
            NotificationPanelViewController.this.mNavigationBarBottomHeight = windowInsets.getStableInsetBottom();
            NotificationPanelViewController.this.updateMaxHeadsUpTranslation();
            return windowInsets;
        }
    }
}

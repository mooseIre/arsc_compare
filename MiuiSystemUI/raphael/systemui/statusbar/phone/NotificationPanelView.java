package com.android.systemui.statusbar.phone;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManagerNative;
import android.app.Fragment;
import android.app.MiuiStatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.Log;
import android.util.MathUtils;
import android.util.Slog;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.keyguard.AwesomeLockScreen;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.MiuiFastUnlockController;
import com.android.keyguard.MiuiKeyguardFaceUnlockView;
import com.android.keyguard.MiuiKeyguardUtils;
import com.android.keyguard.analytics.AnalyticsHelper;
import com.android.keyguard.charge.ChargeUtils;
import com.android.keyguard.charge.MiuiChargeController;
import com.android.keyguard.clock.KeyguardClockContainer;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.keyguard.magazine.LockScreenMagazinePreView;
import com.android.keyguard.magazine.LockScreenMagazineUtils;
import com.android.keyguard.negative.MiuiKeyguardMoveLeftViewContainer;
import com.android.keyguard.utils.ViewAnimationUtils;
import com.android.keyguard.wallpaper.KeyguardWallpaperUtils;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController;
import com.android.systemui.Constants;
import com.android.systemui.DejankUtils;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.Util;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.miui.statusbar.analytics.NotificationStat;
import com.android.systemui.miui.statusbar.phone.MiuiStatusBarPromptController;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.miui.statusbar.policy.SuperSaveModeController;
import com.android.systemui.plugins.IntentButtonProvider;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.statusbar.DismissView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.phone.DoubleTapHelper;
import com.android.systemui.statusbar.phone.KeyguardClockPositionAlgorithm;
import com.android.systemui.statusbar.phone.KeyguardMoveHelper;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.KeyguardUserSwitcher;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.util.AutoCleanFloatTransitionListener;
import com.android.systemui.util.QcomBoostFramework;
import com.miui.systemui.renderlayer.MiRenderInfo;
import com.miui.systemui.renderlayer.RenderLayerManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import miui.util.CustomizeUtil;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;

public class NotificationPanelView extends PanelView implements ExpandableView.OnHeightChangedListener, StatusBarWindowManager.BlurRatioChangedListener, View.OnClickListener, NotificationStackScrollLayout.OnOverscrollTopChangedListener, NotificationStackScrollLayout.OnEmptySpaceClickListener, KeyguardMoveHelper.Callback, OnHeadsUpChangedListener, QS.HeightListener, NotificationStackScrollLayout.OnTopPaddingUpdateListener, MiuiKeyguardWallpaperController.KeyguardWallpaperCallback, SuperSaveModeController.SuperSaveModeChangeListener, ControlPanelController.UseControlPanelChangeListener {
    private static final boolean DEBUG = Constants.DEBUG;
    private static final FloatProperty<NotificationPanelView> SET_DARK_AMOUNT_PROPERTY = new FloatProperty<NotificationPanelView>("mDarkAmount") {
        public void setValue(NotificationPanelView notificationPanelView, float f) {
            notificationPanelView.setDarkAmount(f);
        }

        public Float get(NotificationPanelView notificationPanelView) {
            return Float.valueOf(notificationPanelView.mDarkAmount);
        }
    };
    public static final String TAG = "NotificationPanelView";
    /* access modifiers changed from: private */
    public static final Rect mDummyDirtyRect = new Rect(0, 0, 1, 1);
    public static boolean sQsExpanded;
    ContentObserver contentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            NotificationPanelView notificationPanelView = NotificationPanelView.this;
            boolean unused = notificationPanelView.mExpandableUnderKeyguard = MiuiStatusBarManager.isExpandableUnderKeyguardForUser(notificationPanelView.mContext, -2);
        }
    };
    private final Runnable mAnimateKeyguardBottomAreaInvisibleEndRunnable = new Runnable() {
        public void run() {
            NotificationPanelView.this.mKeyguardBottomArea.setVisibility(8);
        }
    };
    /* access modifiers changed from: private */
    public final Runnable mAnimateKeyguardStatusBarInvisibleEndRunnable = new Runnable() {
        public void run() {
            NotificationPanelView.this.mKeyguardStatusBar.setVisibility(4);
            NotificationPanelView.this.mKeyguardStatusBar.setAlpha(1.0f);
            float unused = NotificationPanelView.this.mKeyguardStatusBarAnimateAlpha = 1.0f;
        }
    };
    private final Runnable mAnimateKeyguardStatusViewInvisibleEndRunnable = new Runnable() {
        public void run() {
            boolean unused = NotificationPanelView.this.mKeyguardStatusViewAnimating = false;
            NotificationPanelView.this.mKeyguardClockView.setVisibility(8);
            NotificationPanelView.this.mAwesomeLockScreenContainer.setVisibility(8);
        }
    };
    private final Runnable mAnimateKeyguardStatusViewVisibleEndRunnable = new Runnable() {
        public void run() {
            boolean unused = NotificationPanelView.this.mKeyguardStatusViewAnimating = false;
        }
    };
    /* access modifiers changed from: private */
    public boolean mAnimateNextTopPaddingChange;
    private AwesomeLockScreen mAwesomeLockScreen;
    /* access modifiers changed from: private */
    public FrameLayout mAwesomeLockScreenContainer;
    private boolean mBlockTouches;
    /* access modifiers changed from: private */
    public float mBlurRatio;
    private AutoCleanFloatTransitionListener mBlurRatioListener = new AutoCleanFloatTransitionListener("PanelViewBlur") {
        public void onUpdate(Map<String, Float> map) {
            float unused = NotificationPanelView.this.mBlurRatio = map.get("blurRatio").floatValue();
            ((StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class)).setBlurRatio(NotificationPanelView.this.mBlurRatio);
        }
    };
    private ValueAnimator mBouncerFractionAnimator;
    private final MiRenderInfo mClearBlurInfo = new MiRenderInfo() {
    };
    private boolean mClearBlurRegistered;
    private int mClockAnimationTarget = -1;
    private KeyguardClockPositionAlgorithm mClockPositionAlgorithm = new KeyguardClockPositionAlgorithm();
    private KeyguardClockPositionAlgorithm.Result mClockPositionResult = new KeyguardClockPositionAlgorithm.Result();
    private float mCloseHandleUnderlapSize;
    private boolean mClosingWithAlphaFadeOut;
    private boolean mCollapsedOnDown;
    private boolean mConflictingQsExpansionGesture;
    /* access modifiers changed from: private */
    public float mDarkAmount;
    private ValueAnimator mDarkAnimator;
    protected DismissView mDismissView;
    private int mDismissViewBottomMargin;
    private boolean mDismissViewShowUp;
    private Animation mDismissViewShowUpAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.recents_to_launcher_enter);
    private int mDismissViewSize;
    private DoubleTapHelper mDoubleTapHelper;
    private int mDoubleTapUninvalidBottomAreaHeight;
    private int mDoubleTapUninvalidStartEndAreaWidth;
    private int mDoubleTapUninvalidTopAreaHeight;
    private boolean mDozing;
    private boolean mDozingOnDown;
    private float mEmptyDragAmount;
    private TextView mEmptyShadeView;
    /* access modifiers changed from: private */
    public boolean mExpandableUnderKeyguard;
    private boolean mExpandingFromHeadsUp;
    private MiuiKeyguardFaceUnlockView mFaceUnlockView;
    private FalsingManager mFalsingManager;
    private boolean mFlingAfterTracking;
    private FlingAnimationUtils mFlingAnimationUtils;
    /* access modifiers changed from: private */
    public boolean mForceBlack;
    private final FragmentHostManager.FragmentListener mFragmentListener = new FragmentHostManager.FragmentListener() {
        public void onFragmentViewCreated(String str, Fragment fragment) {
            QS unused = NotificationPanelView.this.mQs = (QS) fragment;
            NotificationPanelView.this.mQs.setPanelView(NotificationPanelView.this);
            NotificationPanelView.this.mQs.setExpandClickListener(NotificationPanelView.this);
            NotificationPanelView.this.mQs.setHeaderClickable(NotificationPanelView.this.mQsExpansionEnabled);
            NotificationPanelView.this.mQs.setKeyguardShowing(NotificationPanelView.this.mKeyguardShowing);
            NotificationPanelView.this.mQs.setOverscrolling(NotificationPanelView.this.mStackScrollerOverscrolling);
            NotificationPanelView notificationPanelView = NotificationPanelView.this;
            notificationPanelView.mNotificationStackScroller.setQs(notificationPanelView.mQs);
            NotificationPanelView.this.updateQsExpansion();
            NotificationPanelView.this.updateDismissViewState();
        }

        public void onFragmentViewDestroyed(String str, Fragment fragment) {
            if (fragment == NotificationPanelView.this.mQs) {
                if (NotificationPanelView.this.isQsDetailShowing()) {
                    NotificationPanelView.this.mQs.closeDetail();
                }
                QS unused = NotificationPanelView.this.mQs = null;
            }
        }
    };
    private ContentObserver mGestureWakeupModeContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            NotificationPanelView notificationPanelView = NotificationPanelView.this;
            boolean unused = notificationPanelView.mOpenDoubleTapGoToSleep = MiuiSettings.System.getBoolean(notificationPanelView.mContext.getContentResolver(), "gesture_wakeup", false);
        }
    };
    private NotificationGroupManager mGroupManager;
    private boolean mHeadsUpAnimatingAway;
    private Runnable mHeadsUpExistenceChangedRunnable = new Runnable() {
        public void run() {
            NotificationPanelView.this.setHeadsUpAnimatingAway(false);
            NotificationPanelView.this.notifyBarPanelExpansionChanged();
        }
    };
    private HeadsUpTouchHelper mHeadsUpTouchHelper;
    private float mHorizontalMoveDistance;
    private float mHorizontalMovePer;
    private int mIndicationBottomPadding;
    private float mInitialHeightOnTouch;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private boolean mIntercepting;
    private boolean mIsExpanding;
    private boolean mIsExpansionFromHeadsUp;
    private boolean mIsFullWidth;
    private boolean mIsInteractive;
    private boolean mIsKeyguardCoverd;
    private boolean mIsLaunchTransitionFinished;
    private boolean mIsLaunchTransitionRunning;
    private boolean mIsStatusBarShowDismissView;
    private float mKeyguardBouncerFraction;
    private boolean mKeyguardBouncerShowing;
    /* access modifiers changed from: private */
    public KeyguardClockContainer mKeyguardClockView;
    private boolean mKeyguardHorizontalMoving;
    private KeyguardIndicationController mKeyguardIndicationController;
    /* access modifiers changed from: private */
    public MiuiKeyguardMoveLeftViewContainer mKeyguardLeftView;
    private KeyguardMoveHelper mKeyguardMoveHelper;
    private boolean mKeyguardOccluded;
    /* access modifiers changed from: private */
    public boolean mKeyguardShowing;
    /* access modifiers changed from: private */
    public KeyguardStatusBarView mKeyguardStatusBar;
    /* access modifiers changed from: private */
    public float mKeyguardStatusBarAnimateAlpha = 1.0f;
    /* access modifiers changed from: private */
    public boolean mKeyguardStatusViewAnimating;
    private float mKeyguardTouchDownX;
    private float mKeyguardTouchDownY;
    private KeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
        public void onUserSwitchComplete(int i) {
            super.onUserSwitchComplete(i);
            NotificationPanelView.this.mKeyguardClockView.onUserChanged();
        }

        public void onLockScreenMagazinePreViewVisibilityChanged(boolean z) {
            if (NotificationPanelView.this.mLockScreenMagazinePreViewVisible != z) {
                boolean unused = NotificationPanelView.this.mLockScreenMagazinePreViewVisible = z;
                NotificationPanelView.this.updateGxzwState();
            }
        }

        public void onUserUnlocked() {
            NotificationPanelView.this.mKeyguardLeftView.setCustomBackground();
        }

        public void onKeyguardBouncerChanged(boolean z) {
            NotificationPanelView.this.onBouncerShowingChanged(z);
        }
    };
    private KeyguardUserSwitcher mKeyguardUserSwitcher;
    private MiuiKeyguardWallpaperController mKeyguardWallpaperController;
    private boolean mLastAnnouncementWasQuickSettings;
    private String mLastCameraLaunchSource = "lockscreen_affordance";
    private int mLastDensityDpi = -1;
    private int mLastOrientation = -1;
    private float mLastOverscroll;
    private Runnable mLaunchAnimationEndRunnable;
    private boolean mLaunchingAffordance;
    private ImageView mLeftViewBg;
    private boolean mListenForHeadsUp;
    private LockScreenMagazineController mLockScreenMagazineController;
    private LockScreenMagazinePreView mLockScreenMagazinePreView;
    /* access modifiers changed from: private */
    public boolean mLockScreenMagazinePreViewVisible;
    private LockscreenGestureLogger mLockscreenGestureLogger = new LockscreenGestureLogger();
    private Paint mMaskPaint = new Paint();
    private MiuiChargeController mMiuiChargeController;
    private MiuiStatusBarPromptController mMiuiStatusBarPromptController;
    private List<View> mMoveListViews = new ArrayList();
    private int mNavigationBarBottomHeight;
    private boolean mNoVisibleNotifications = true;
    private View mNotchCorner;
    protected NotificationsQuickSettingsContainer mNotificationContainerParent;
    protected NotificationStackScrollLayout mNotificationStackScroller;
    private int mNotificationsHeaderCollideDistance;
    private int mOldLayoutDirection;
    private boolean mOnlyAffordanceInThisMotion;
    /* access modifiers changed from: private */
    public boolean mOpenDoubleTapGoToSleep;
    private int mOrientation = 1;
    private boolean mPanelExpanded;
    private int mPanelGravity;
    private int mPanelWidth;
    /* access modifiers changed from: private */
    public QcomBoostFramework mPerf = null;
    private int mPositionMinSideMargin;
    /* access modifiers changed from: private */
    public PowerManager mPowerManager;
    /* access modifiers changed from: private */
    public QS mQs;
    private boolean mQsAnimatorExpand;
    private boolean mQsExpandImmediate;
    private boolean mQsExpanded;
    private boolean mQsExpandedWhenExpandingStarted;
    /* access modifiers changed from: private */
    public ValueAnimator mQsExpansionAnimator;
    protected boolean mQsExpansionEnabled = true;
    private boolean mQsExpansionFromOverscroll;
    protected float mQsExpansionHeight;
    private int mQsFalsingThreshold;
    private FrameLayout mQsFrame;
    private boolean mQsFullyExpanded;
    protected int mQsMaxExpansionHeight;
    protected int mQsMinExpansionHeight;
    private boolean mQsOverscrollExpansionEnabled;
    private int mQsPeekHeight;
    private boolean mQsScrimEnabled = true;
    /* access modifiers changed from: private */
    public ValueAnimator mQsSizeChangeAnimator;
    /* access modifiers changed from: private */
    public ValueAnimator mQsTopPaddingAnimator;
    private boolean mQsTouchAboveFalsingThreshold;
    private boolean mQsTracking;
    private VelocityTracker mQsVelocityTracker;
    private int mScreenHeight;
    private int mScreenWidth;
    private boolean mShowEmptyShadeView;
    private boolean mShowIconsWhenExpanded;
    /* access modifiers changed from: private */
    public boolean mStackScrollerOverscrolling;
    private final ValueAnimator.AnimatorUpdateListener mStatusBarAnimateAlphaListener = new ValueAnimator.AnimatorUpdateListener() {
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            float unused = NotificationPanelView.this.mKeyguardStatusBarAnimateAlpha = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            NotificationPanelView.this.updateHeaderKeyguardAlpha();
        }
    };
    private int mStatusBarMinHeight;
    protected int mStatusBarState;
    private boolean mStatusbarExpandIconsDark;
    private boolean mSuperSaveModeOn;
    private boolean mSupportGestureWakeup;
    /* access modifiers changed from: private */
    public View mSwitchSystemUser;
    private View mThemeBackgroundView;
    private int mTopPaddingAdjustment;
    private int mTopPaddingWhenQsBeingCovered;
    private boolean mTouchAtKeyguardBottomArea = false;
    private int mTouchSlop;
    private int mTrackingPointer;
    private boolean mTwoFingerQsExpandPossible;
    private int mUnlockMoveDistance;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;

    private static float interpolate(float f, float f2, float f3) {
        return ((1.0f - f) * f2) + (f * f3);
    }

    private boolean isPort(int i) {
        return i == 1;
    }

    public void onReset(ExpandableView expandableView) {
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public NotificationPanelView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setWillNotDraw(true ^ DEBUG);
        this.mFalsingManager = FalsingManager.getInstance(context);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mQsOverscrollExpansionEnabled = getResources().getBoolean(R.bool.config_enableQuickSettingsOverscrollExpansion);
        this.mPerf = new QcomBoostFramework();
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mUpdateMonitor.registerCallback(this.mKeyguardUpdateMonitorCallback);
        this.mLockScreenMagazineController = LockScreenMagazineController.getInstance(context);
        this.mKeyguardWallpaperController = (MiuiKeyguardWallpaperController) Dependency.get(MiuiKeyguardWallpaperController.class);
        this.mMiuiStatusBarPromptController = (MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class);
        if (ChargeUtils.supportWirelessCharge() || ChargeUtils.supportNewChargeAnimation()) {
            this.mMiuiChargeController = new MiuiChargeController(this.mContext);
        }
        this.mPowerManager = (PowerManager) context.getSystemService("power");
        this.mDoubleTapHelper = new DoubleTapHelper(this, 200, new DoubleTapHelper.ActivationListener() {
            public void onActiveChanged(boolean z) {
            }
        }, new DoubleTapHelper.DoubleTapListener() {
            public boolean onDoubleTap() {
                Slog.i(NotificationPanelView.TAG, "keyguard_screen_off_reason:double tap");
                NotificationPanelView.this.mPowerManager.goToSleep(SystemClock.uptimeMillis());
                AnalyticsHelper.getInstance(NotificationPanelView.this.mContext).record("action_double_click_sleep");
                return true;
            }
        }, (DoubleTapHelper.SlideBackListener) null, (DoubleTapHelper.DoubleTapLogListener) null);
        this.mExpandableUnderKeyguard = MiuiStatusBarManager.isExpandableUnderKeyguard(context);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                NotificationPanelView notificationPanelView = NotificationPanelView.this;
                boolean unused = notificationPanelView.mExpandableUnderKeyguard = MiuiStatusBarManager.isExpandableUnderKeyguardForUser(notificationPanelView.mContext, -2);
            }
        }, intentFilter);
        initScreenSize();
        loadDimens(getResources());
    }

    public void onKeyguardWallpaperUpdated(MiuiKeyguardWallpaperController.KeyguardWallpaperType keyguardWallpaperType, boolean z, File file, Drawable drawable) {
        if (!this.mUpdateMonitor.isSupportLockScreenMagazineLeft() || !this.mKeyguardLeftView.hasBackgroundImageDrawable()) {
            this.mLeftViewBg.setBackgroundColor(this.mUpdateMonitor.getWallpaperBlurColor());
        }
        boolean isWallpaperColorLight = KeyguardUpdateMonitor.isWallpaperColorLight(this.mContext);
        this.mKeyguardBottomArea.setDarkMode(isWallpaperColorLight);
        this.mKeyguardMoveHelper.getRightMoveView().setDarkMode(isWallpaperColorLight);
        this.mKeyguardClockView.setDarkMode(isWallpaperColorLight);
        this.mKeyguardStatusBar.setDarkMode(!this.mForceBlack && isWallpaperColorLight);
        this.mLockScreenMagazinePreView.setDarkMode(isWallpaperColorLight);
    }

    public void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
        this.mKeyguardLeftView.setStatusBar(this.mStatusBar);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setClipChildren(false);
        this.mStatusbarExpandIconsDark = getResources().getBoolean(R.bool.expanded_status_bar_darkmode);
        this.mLockScreenMagazineController.setNotificationPanelView(this);
        this.mKeyguardStatusBar = (KeyguardStatusBarView) findViewById(R.id.keyguard_header);
        this.mLockScreenMagazinePreView = (LockScreenMagazinePreView) findViewById(R.id.wallpaper_des);
        this.mKeyguardClockView = (KeyguardClockContainer) findViewById(R.id.keyguard_clock_view);
        this.mThemeBackgroundView = findViewById(R.id.theme_background);
        this.mNotchCorner = findViewById(R.id.notch_corner);
        this.mLeftViewBg = (ImageView) findViewById(R.id.left_view_bg);
        this.mAwesomeLockScreenContainer = (FrameLayout) findViewById(R.id.awesome_lock_screen_container);
        this.mNotificationContainerParent = (NotificationsQuickSettingsContainer) findViewById(R.id.notification_container_parent);
        this.mNotificationStackScroller = (NotificationStackScrollLayout) findViewById(R.id.notification_stack_scroller);
        this.mNotificationStackScroller.setOnHeightChangedListener(this);
        this.mNotificationStackScroller.setOverscrollTopChangedListener(this);
        this.mNotificationStackScroller.setOnEmptySpaceClickListener(this);
        this.mNotificationStackScroller.setOnTopPaddingUpdateListener(this);
        this.mNotificationStackScroller.setFlingAnimationUtils(this.mFlingAnimationUtils);
        this.mEmptyShadeView = (TextView) findViewById(R.id.no_notifications);
        this.mKeyguardBottomArea = (KeyguardBottomAreaView) findViewById(R.id.keyguard_bottom_area);
        this.mFaceUnlockView = (MiuiKeyguardFaceUnlockView) findViewById(R.id.miui_keyguard_face_unlock_view);
        this.mFaceUnlockView.setKeyguardFaceUnlockView(true);
        this.mKeyguardMoveHelper = new KeyguardMoveHelper(this, getContext());
        this.mKeyguardVerticalMoveHelper = new KeyguardVerticalMoveHelper(this.mContext, this, this.mKeyguardClockView, this.mNotificationStackScroller, this.mFaceUnlockView, this.mLockScreenMagazinePreView);
        this.mKeyguardBottomArea.setNotificationPanelView(this);
        this.mLastOrientation = getResources().getConfiguration().orientation;
        this.mSwitchSystemUser = findViewById(R.id.switch_to_system_user);
        this.mSwitchSystemUser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!KeyguardUpdateMonitor.isOwnerUser()) {
                    try {
                        ActivityManagerNative.getDefault().switchUser(0);
                        NotificationPanelView.this.mSwitchSystemUser.setVisibility(8);
                    } catch (RemoteException e) {
                        Log.e(NotificationPanelView.TAG, "switchUser failed", e);
                    }
                }
            }
        });
        this.mMoveListViews.add(this.mKeyguardClockView);
        this.mMoveListViews.add(this.mNotificationContainerParent);
        this.mMoveListViews.add(this.mKeyguardBottomArea);
        this.mMoveListViews.add(this.mSwitchSystemUser);
        this.mMoveListViews.add(this.mLockScreenMagazinePreView);
        this.mQsFrame = (FrameLayout) findViewById(R.id.qs_frame);
        this.mKeyguardLeftView = (MiuiKeyguardMoveLeftViewContainer) findViewById(R.id.keyguard_left_view);
        this.mKeyguardLeftView.setPanel(this);
        this.mIsDefaultTheme = MiuiKeyguardUtils.isDefaultLockScreenTheme();
        this.mSupportGestureWakeup = MiuiKeyguardUtils.isSupportGestureWakeup();
        this.mKeyguardWallpaperController.updateWallpaper(true);
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().setNotificationPanelView(this);
        }
        updateResources(false);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = configuration.orientation;
        if (i != this.mOrientation) {
            this.mOrientation = i;
            initScreenSize();
        }
    }

    private boolean shouldShowSwitchSystemUser() {
        if (KeyguardUpdateMonitor.isOwnerUser()) {
            return false;
        }
        int secondUser = KeyguardUpdateMonitor.getSecondUser();
        int currentUser = KeyguardUpdateMonitor.getCurrentUser();
        int kidSpaceUser = KeyguardUpdateMonitor.getKidSpaceUser();
        if (currentUser == secondUser || currentUser == kidSpaceUser) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        FragmentHostManager.get(this).addTagListener(QS.TAG, this.mFragmentListener);
        post(new Runnable() {
            public void run() {
                boolean z;
                KeyguardStatusBarView access$1300 = NotificationPanelView.this.mKeyguardStatusBar;
                if (!NotificationPanelView.this.mForceBlack) {
                    KeyguardUpdateMonitor unused = NotificationPanelView.this.mUpdateMonitor;
                    if (KeyguardUpdateMonitor.isWallpaperColorLight(NotificationPanelView.this.mContext)) {
                        z = true;
                        access$1300.setDarkMode(z);
                    }
                }
                z = false;
                access$1300.setDarkMode(z);
            }
        });
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_expandable_under_keyguard"), false, this.contentObserver, -1);
        ((StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class)).addBlurRatioListener(this);
        this.mUpdateMonitor.registerPhoneSignalChangeCallback();
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("gesture_wakeup"), false, this.mGestureWakeupModeContentObserver, -1);
        this.mGestureWakeupModeContentObserver.onChange(false);
        this.mKeyguardWallpaperController.addCallback(this);
        ((SuperSaveModeController) Dependency.get(SuperSaveModeController.class)).addCallback(this);
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).removeCallback(this);
        FragmentHostManager.get(this).removeTagListener(QS.TAG, this.mFragmentListener);
        this.mContext.getContentResolver().unregisterContentObserver(this.contentObserver);
        ((StatusBarWindowManager) Dependency.get(StatusBarWindowManager.class)).removeBlurRatioListener(this);
        this.mUpdateMonitor.unRegisterPhoneSignalChangeCallback();
        this.mContext.getContentResolver().unregisterContentObserver(this.mGestureWakeupModeContentObserver);
        this.mKeyguardWallpaperController.removeCallback(this);
        this.mKeyguardMoveHelper.getLeftMoveView().reset();
        ((SuperSaveModeController) Dependency.get(SuperSaveModeController.class)).removeCallback(this);
    }

    public void updateResources(boolean z) {
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        loadDimens(resources);
        updateLayout(isPort(configuration.orientation));
        int i = configuration.orientation;
        if (i != this.mLastOrientation) {
            this.mLastOrientation = i;
            resetVerticalPanelPosition();
            this.mKeyguardMoveHelper.reset(true);
        }
        if (z) {
            this.mStatusbarExpandIconsDark = resources.getBoolean(R.bool.expanded_status_bar_darkmode);
            reInflateThemeBackgroundView();
            boolean isDefaultLockScreenTheme = MiuiKeyguardUtils.isDefaultLockScreenTheme();
            if (isDefaultLockScreenTheme != this.mIsDefaultTheme) {
                String str = TAG;
                Slog.i(str, "default theme change: mIsDefaultTheme = " + this.mIsDefaultTheme + ", isDefaultTheme = " + isDefaultLockScreenTheme);
            }
            this.mIsDefaultTheme = isDefaultLockScreenTheme;
            ChargeUtils.disableChargeAnimation(false);
            PanelBar.LOG(NotificationPanelView.class, "isDefaultTheme = " + this.mIsDefaultTheme);
            if (isKeyguardShowing()) {
                if (this.mIsDefaultTheme) {
                    removeAwesomeLockScreen();
                } else {
                    addAwesomeLockScreenIfNeed(true);
                    AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
                    if (awesomeLockScreen != null) {
                        awesomeLockScreen.updatePauseResumeStatus();
                    }
                }
                this.mKeyguardWallpaperController.updateWallpaper(true);
                setBarState(this.mStatusBarState, false, false);
            }
            new AsyncTask<Void, Void, Void>() {
                /* access modifiers changed from: protected */
                public Void doInBackground(Void... voidArr) {
                    NotificationPanelView.this.mUpdateMonitor.updateWallpaperBlurColor();
                    KeyguardWallpaperUtils.clearWallpaperSrc(NotificationPanelView.this.mContext);
                    return null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
        if (configuration.densityDpi != this.mLastDensityDpi) {
            inflateLeftView();
            this.mKeyguardMoveHelper.reset(false);
        }
        this.mLastDensityDpi = configuration.densityDpi;
        this.mKeyguardMoveHelper.onConfigurationChanged();
        this.mLockScreenMagazineController.updateResources(z);
    }

    /* access modifiers changed from: protected */
    public void loadDimens(Resources resources) {
        super.loadDimens(resources);
        this.mFlingAnimationUtils = new FlingAnimationUtils(getContext(), 0.4f);
        this.mStatusBarMinHeight = resources.getDimensionPixelSize(17105467);
        this.mQsPeekHeight = getResources().getDimensionPixelSize(R.dimen.qs_peek_height);
        this.mNotificationsHeaderCollideDistance = resources.getDimensionPixelSize(R.dimen.header_notifications_collide_distance);
        this.mUnlockMoveDistance = resources.getDimensionPixelOffset(R.dimen.unlock_move_distance);
        this.mClockPositionAlgorithm.loadDimens(resources);
        this.mQsFalsingThreshold = resources.getDimensionPixelSize(R.dimen.qs_falsing_threshold);
        this.mPositionMinSideMargin = resources.getDimensionPixelSize(R.dimen.notification_panel_min_side_margin);
        this.mIndicationBottomPadding = resources.getDimensionPixelSize(R.dimen.keyguard_indication_bottom_padding);
        this.mCloseHandleUnderlapSize = resources.getDimension(R.dimen.close_handle_underlap);
        this.mDismissViewSize = resources.getDimensionPixelSize(R.dimen.notification_clear_all_size);
        this.mDismissViewBottomMargin = resources.getDimensionPixelSize(R.dimen.notification_clear_all_bottom_margin);
        this.mPanelWidth = resources.getDimensionPixelSize(R.dimen.notification_panel_width);
        this.mPanelGravity = resources.getInteger(R.integer.notification_panel_layout_gravity);
        this.mDoubleTapUninvalidTopAreaHeight = resources.getDimensionPixelSize(R.dimen.double_tap_sleep_uninvalid_top_area_height);
        this.mDoubleTapUninvalidBottomAreaHeight = resources.getDimensionPixelSize(R.dimen.double_tap_sleep_uninvalid_bottom_area_height);
        this.mDoubleTapUninvalidStartEndAreaWidth = resources.getDimensionPixelSize(R.dimen.double_tap_sleep_uninvalid_start_end_area_width);
    }

    /* access modifiers changed from: protected */
    public void updateLayout(boolean z) {
        int i = (Constants.IS_TABLET || !z) ? this.mPanelWidth : -1;
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mQsFrame.getLayoutParams();
        if (!(layoutParams.width == i && layoutParams.gravity == this.mPanelGravity)) {
            layoutParams.width = i;
            layoutParams.gravity = this.mPanelGravity;
            if (z) {
                this.mQsFrame.setLayoutParams(layoutParams);
            }
        }
        FrameLayout.LayoutParams layoutParams2 = (FrameLayout.LayoutParams) this.mNotificationStackScroller.getLayoutParams();
        if (layoutParams2.width != i || layoutParams2.gravity != this.mPanelGravity) {
            layoutParams2.width = i;
            layoutParams2.gravity = this.mPanelGravity;
            if (z) {
                this.mNotificationStackScroller.setLayoutParams(layoutParams2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        QS qs;
        super.onLayout(z, i, i2, i3, i4);
        int i5 = 0;
        setIsFullWidth(this.mNotificationStackScroller.getWidth() == getWidth());
        QS qs2 = this.mQs;
        if (qs2 != null) {
            if (!this.mKeyguardShowing) {
                i5 = qs2.getQsMinExpansionHeight();
            }
            this.mQsMinExpansionHeight = i5;
        }
        positionClockAndNotifications();
        onQsHeightChanged();
        if (!this.mQsExpanded) {
            setQsExpansion(((float) this.mQsMinExpansionHeight) + this.mLastOverscroll);
        }
        updateExpandedHeight(getExpandedHeight());
        updateHeader();
        if (this.mQsSizeChangeAnimator == null && (qs = this.mQs) != null) {
            qs.setHeightOverride(qs.getDesiredHeight());
        }
        updateMaxHeadsUpTranslation();
    }

    private void setIsFullWidth(boolean z) {
        this.mIsFullWidth = z;
        this.mNotificationStackScroller.setIsFullWidth(z);
    }

    private void startQsSizeChangeAnimation(int i, int i2) {
        ValueAnimator valueAnimator = this.mQsSizeChangeAnimator;
        if (valueAnimator != null) {
            i = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            this.mQsSizeChangeAnimator.cancel();
        }
        this.mQsSizeChangeAnimator = ValueAnimator.ofInt(new int[]{i, i2});
        Interpolator interpolator = Interpolators.CUBIC_EASE_IN_OUT;
        this.mQsSizeChangeAnimator.setDuration((long) 400);
        this.mQsSizeChangeAnimator.setInterpolator(interpolator);
        this.mQsSizeChangeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                NotificationPanelView.this.requestScrollerTopPaddingUpdate(false);
                NotificationPanelView.this.requestPanelHeightUpdate();
                NotificationPanelView.this.mQs.setHeightOverride(((Integer) NotificationPanelView.this.mQsSizeChangeAnimator.getAnimatedValue()).intValue());
            }
        });
        this.mQsSizeChangeAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                ValueAnimator unused = NotificationPanelView.this.mQsSizeChangeAnimator = null;
            }
        });
        this.mQsSizeChangeAnimator.start();
    }

    private void positionClockAndNotifications() {
        int i;
        boolean isAddOrRemoveAnimationPending = this.mNotificationStackScroller.isAddOrRemoveAnimationPending();
        if (this.mStatusBarState != 1) {
            ValueAnimator valueAnimator = this.mQsTopPaddingAnimator;
            if (valueAnimator != null) {
                i = ((Integer) valueAnimator.getAnimatedValue()).intValue();
            } else if (this.mNotificationStackScroller.isQsCovered() || this.mNotificationStackScroller.isQsBeingCovered()) {
                QS qs = this.mQs;
                i = qs == null ? 0 : qs.getQsHeaderHeight();
            } else {
                QS qs2 = this.mQs;
                i = (qs2 != null ? qs2.getQsMinExpansionHeight() : 0) + this.mQsPeekHeight;
            }
            this.mTopPaddingAdjustment = 0;
        } else {
            this.mClockPositionAlgorithm.setup(this.mStatusBar.getMaxKeyguardNotifications(), getMaxPanelHeight(), getExpandedHeight(), this.mNotificationStackScroller.getNotGoneChildCount(), getHeight(), this.mKeyguardClockView.getClockHeight(), this.mEmptyDragAmount, 0, this.mDarkAmount, this.mKeyguardClockView.getClockVisibleHeight(), 0.0f);
            this.mClockPositionAlgorithm.run(this.mClockPositionResult);
            KeyguardClockPositionAlgorithm.Result result = this.mClockPositionResult;
            int i2 = result.stackScrollerPadding;
            this.mTopPaddingAdjustment = result.stackScrollerPaddingAdjustment;
            i = i2;
        }
        this.mNotificationStackScroller.setIntrinsicPadding(i);
        requestScrollerTopPaddingUpdate(isAddOrRemoveAnimationPending);
    }

    public int computeMaxKeyguardNotifications(int i) {
        return getKeyguardNotificationsViewList(i).size();
    }

    public List<View> getKeyguardNotificationsViewList(int i) {
        float f;
        int i2;
        ArrayList arrayList = new ArrayList();
        float intrinsicPadding = (float) this.mNotificationStackScroller.getIntrinsicPadding();
        int max = Math.max(1, getResources().getDimensionPixelSize(R.dimen.notification_divider_height));
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            Rect fodPosition = MiuiGxzwManager.getFodPosition(this.mContext);
            f = ((float) (fodPosition.top - fodPosition.height())) - intrinsicPadding;
        } else {
            View mainLayout = this.mLockScreenMagazinePreView.getMainLayout();
            if (mainLayout == null || !mainLayout.isShown()) {
                i2 = 0;
            } else {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mainLayout.getLayoutParams();
                i2 = mainLayout.getHeight() + layoutParams.topMargin + layoutParams.bottomMargin;
            }
            f = ((((float) this.mNotificationStackScroller.getHeight()) - intrinsicPadding) - ((float) this.mIndicationBottomPadding)) - ((float) i2);
        }
        for (int i3 = 0; i3 < this.mNotificationStackScroller.getChildCount(); i3++) {
            ExpandableView expandableView = (ExpandableView) this.mNotificationStackScroller.getChildAt(i3);
            if (expandableView instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                if (!this.mGroupManager.isSummaryOfSuppressedGroup(expandableNotificationRow.getStatusBarNotification()) && this.mStatusBar.shouldShowOnKeyguard(expandableNotificationRow.getEntry()) && !expandableNotificationRow.isRemoved()) {
                    f -= (float) (expandableView.getMinHeight() + max);
                    if (f < 0.0f || arrayList.size() >= i) {
                        break;
                    }
                    arrayList.add(expandableNotificationRow);
                }
            }
        }
        return arrayList;
    }

    public void animateToFullShade(long j) {
        onPanelDisplayChanged(true);
        ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelExpanded(false, true, this.mNotificationStackScroller.getNotGoneNotifications());
        ValueAnimator valueAnimator = this.mQsTopPaddingAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mQsTopPaddingAnimator = ValueAnimator.ofInt(new int[]{this.mNotificationStackScroller.getTopPadding(), this.mQs.getQsMinExpansionHeight()});
        this.mQsTopPaddingAnimator.setStartDelay(j);
        this.mQsTopPaddingAnimator.setDuration(448);
        this.mQsTopPaddingAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
        this.mQsTopPaddingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                NotificationPanelView.this.lambda$animateToFullShade$0$NotificationPanelView(valueAnimator);
            }
        });
        this.mQsTopPaddingAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animator) {
                NotificationPanelView.this.mAwesomeLockScreenContainer.setVisibility(8);
            }

            public void onAnimationEnd(Animator animator) {
                ValueAnimator unused = NotificationPanelView.this.mQsTopPaddingAnimator = null;
                boolean unused2 = NotificationPanelView.this.mAnimateNextTopPaddingChange = true;
                NotificationPanelView.this.mNotificationStackScroller.goToFullShade(0);
                NotificationPanelView.this.requestLayout();
                ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelAnimationEnd();
            }
        });
        this.mQsTopPaddingAnimator.start();
    }

    public /* synthetic */ void lambda$animateToFullShade$0$NotificationPanelView(ValueAnimator valueAnimator) {
        requestScrollerTopPaddingUpdate(false);
        updateStatusBarWindowBlur();
    }

    public void setQsExpansionEnabled(boolean z) {
        this.mQsExpansionEnabled = z;
        QS qs = this.mQs;
        if (qs != null) {
            qs.setHeaderClickable(z);
        }
    }

    public void resetViews() {
        this.mIsLaunchTransitionFinished = false;
        this.mBlockTouches = false;
        if (!this.mLaunchingAffordance) {
            this.mKeyguardMoveHelper.resetImmediately();
            this.mLastCameraLaunchSource = "lockscreen_affordance";
        }
        closeQs();
        this.mStatusBar.closeAndSaveGuts(true, true, true, -1, -1, true);
        this.mNotificationStackScroller.resetViews();
        if (this.mStatusBarState == 1) {
            this.mLockScreenMagazineController.reset();
        }
        this.mKeyguardVerticalMoveHelper.reset();
    }

    public void closeQs() {
        cancelQsAnimation();
        setQsExpansion((float) this.mQsMinExpansionHeight);
    }

    public void animateCloseQs() {
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
        flingSettings(0.0f, false);
    }

    public void expandWithQs() {
        if (this.mQsExpansionEnabled) {
            this.mQsExpandImmediate = true;
        }
        expand(true);
    }

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
    public void flingToHeight(float f, boolean z, float f2, float f3, boolean z2) {
        this.mFlingAfterTracking = isTracking();
        this.mHeadsUpTouchHelper.notifyFling(!z);
        setClosingWithAlphaFadeout(!z && getFadeoutAlpha() == 1.0f);
        super.flingToHeight(f, z, f2, f3, z2);
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent accessibilityEvent) {
        if (accessibilityEvent.getEventType() != 32) {
            return super.dispatchPopulateAccessibilityEventInternal(accessibilityEvent);
        }
        accessibilityEvent.getText().add(getKeyguardOrLockScreenString());
        this.mLastAnnouncementWasQuickSettings = false;
        return true;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (this.mClosing || this.mBlockTouches || isQsDetailShowing()) {
            Log.d(TAG, "NotificationPanelView not intercept");
            return false;
        }
        initDownStates(motionEvent);
        if (this.mAppMiniWindowManager.onInterceptTouchEvent(motionEvent)) {
            return true;
        }
        if (this.mHeadsUpTouchHelper.onInterceptTouchEvent(motionEvent)) {
            this.mIsExpansionFromHeadsUp = true;
            MetricsLogger.count(this.mContext, "panel_open", 1);
            MetricsLogger.count(this.mContext, "panel_open_peek", 1);
            return true;
        } else if (!Constants.IS_INTERNATIONAL && !isOnKeyguard() && isFullyExpanded() && !this.mQsOverscrollExpansionEnabled && onSpringIntercept(motionEvent)) {
            return true;
        } else {
            if (this.mQsOverscrollExpansionEnabled && !isFullyCollapsed() && !this.mNotificationStackScroller.isQsCovered() && onQsIntercept(motionEvent)) {
                return true;
            }
            boolean onInterceptTouchEvent = super.onInterceptTouchEvent(motionEvent);
            if (this.mKeyguardShowing) {
                if (motionEvent.getActionMasked() == 0) {
                    this.mKeyguardMoveHelper.onTouchEvent(motionEvent);
                    if (motionEvent.getY() >= ((float) this.mKeyguardBottomArea.getTop()) && this.mKeyguardBottomArea.getVisibility() == 0 && this.mKeyguardBottomArea.getAlpha() == 1.0f) {
                        z = true;
                    }
                    this.mTouchAtKeyguardBottomArea = z;
                    this.mKeyguardTouchDownX = motionEvent.getX();
                    this.mKeyguardTouchDownY = motionEvent.getY();
                }
                if (!this.mTouchAtKeyguardBottomArea || (Math.abs(motionEvent.getX() - this.mKeyguardTouchDownX) < ((float) this.mTouchSlop) && Math.abs(motionEvent.getY() - this.mKeyguardTouchDownY) < ((float) this.mTouchSlop))) {
                    return onInterceptTouchEvent;
                }
                return true;
            }
            return onInterceptTouchEvent;
        }
    }

    private boolean onSpringIntercept(MotionEvent motionEvent) {
        int pointerId;
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        if (findPointerIndex < 0) {
            this.mTrackingPointer = motionEvent.getPointerId(0);
            findPointerIndex = 0;
        }
        float x = motionEvent.getX(findPointerIndex);
        float y = motionEvent.getY(findPointerIndex);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            int i = 1;
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    float f = x - this.mInitialTouchX;
                    float f2 = y - this.mInitialTouchY;
                    if (this.mNotificationStackScroller.isScrolledToTop() && f2 > ((float) this.mTouchSlop) && f2 > Math.abs(f)) {
                        if (!this.mStretching) {
                            cancelFlingSpring();
                        }
                        this.mStretching = true;
                        return true;
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 6 && this.mTrackingPointer == (pointerId = motionEvent.getPointerId(motionEvent.getActionIndex()))) {
                        if (motionEvent.getPointerId(0) != pointerId) {
                            i = 0;
                        }
                        this.mTrackingPointer = motionEvent.getPointerId(i);
                        this.mInitialTouchY = motionEvent.getY(i);
                    }
                }
            }
            this.mStretching = false;
        } else {
            this.mInitialTouchX = x;
            this.mInitialTouchY = y;
            this.mStretching = false;
        }
        return false;
    }

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
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    float f = y - this.mInitialTouchY;
                    trackMovement(motionEvent);
                    if (this.mQsTracking) {
                        setQsExpansion(f + this.mInitialHeightOnTouch);
                        trackMovement(motionEvent);
                        this.mIntercepting = false;
                        return true;
                    } else if (Math.abs(f) > ((float) this.mTouchSlop) && Math.abs(f) > Math.abs(x - this.mInitialTouchX) && shouldQuickSettingsIntercept(this.mInitialTouchX, this.mInitialTouchY, f)) {
                        this.mQsTracking = true;
                        onQsExpansionStarted();
                        notifyExpandingFinished();
                        this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                        this.mInitialTouchY = y;
                        this.mInitialTouchX = x;
                        this.mIntercepting = false;
                        this.mNotificationStackScroller.removeLongPressCallback();
                        return true;
                    }
                } else if (actionMasked != 3) {
                    if (actionMasked == 6 && this.mTrackingPointer == (pointerId = motionEvent.getPointerId(motionEvent.getActionIndex()))) {
                        if (motionEvent.getPointerId(0) != pointerId) {
                            z = false;
                        }
                        this.mTrackingPointer = motionEvent.getPointerId(z ? 1 : 0);
                        this.mInitialTouchX = motionEvent.getX(z);
                        this.mInitialTouchY = motionEvent.getY(z);
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
            this.mIntercepting = false;
        } else {
            this.mIntercepting = true;
            this.mInitialTouchY = y;
            this.mInitialTouchX = x;
            initVelocityTracker();
            trackMovement(motionEvent);
            if (shouldQuickSettingsIntercept(this.mInitialTouchX, this.mInitialTouchY, 0.0f)) {
                getParent().requestDisallowInterceptTouchEvent(true);
            }
            if (this.mQsExpansionAnimator != null) {
                onQsExpansionStarted();
                this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                this.mQsTracking = true;
                this.mIntercepting = false;
                this.mNotificationStackScroller.removeLongPressCallback();
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isInContentBounds(float f, float f2) {
        float x = this.mNotificationStackScroller.getX();
        return !this.mNotificationStackScroller.isBelowLastNotification(f - x, f2) && x < f && f < x + ((float) this.mNotificationStackScroller.getWidth());
    }

    /* access modifiers changed from: protected */
    public boolean isInUnderlapBounds(float f, float f2) {
        return ((float) getHeight()) - f2 < this.mCloseHandleUnderlapSize;
    }

    private void initDownStates(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0) {
            boolean z = false;
            this.mOnlyAffordanceInThisMotion = false;
            this.mQsTouchAboveFalsingThreshold = this.mQsFullyExpanded;
            this.mDozingOnDown = isDozing();
            this.mCollapsedOnDown = isFullyCollapsed();
            if (this.mCollapsedOnDown && this.mHeadsUpManager.hasPinnedHeadsUp()) {
                z = true;
            }
            this.mListenForHeadsUp = z;
        }
    }

    private void flingQsWithCurrentVelocity(float f, boolean z) {
        float currentQSVelocity = getCurrentQSVelocity();
        boolean flingExpandsQs = flingExpandsQs(currentQSVelocity);
        if (flingExpandsQs) {
            logQsSwipeDown(f);
        }
        flingSettings(currentQSVelocity, flingExpandsQs && !z);
    }

    private void logQsSwipeDown(float f) {
        this.mLockscreenGestureLogger.write(getContext(), this.mStatusBarState == 1 ? 193 : 194, (int) ((f - this.mInitialTouchY) / this.mStatusBar.getDisplayDensity()), (int) (getCurrentQSVelocity() / this.mStatusBar.getDisplayDensity()));
    }

    private boolean flingExpandsQs(float f) {
        if (isFalseTouch()) {
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
        if (!needsAntiFalsing()) {
            return false;
        }
        if (this.mFalsingManager.isClassiferEnabled()) {
            return this.mFalsingManager.isFalseTouch();
        }
        return !this.mQsTouchAboveFalsingThreshold;
    }

    /* access modifiers changed from: protected */
    public float getQsExpansionFraction() {
        float f;
        int tempQsMaxExpansion = getTempQsMaxExpansion();
        int i = this.mQsMinExpansionHeight;
        int i2 = tempQsMaxExpansion - i;
        if (i2 == 0) {
            f = 1.0f;
        } else {
            f = (this.mQsExpansionHeight - ((float) i)) / ((float) i2);
        }
        return Math.min(1.0f, f);
    }

    /* access modifiers changed from: protected */
    public float getOpeningHeight() {
        return this.mNotificationStackScroller.getOpeningHeight();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z = false;
        if (this.mClosing || this.mBlockTouches || isQsDetailShowing()) {
            return false;
        }
        initDownStates(motionEvent);
        if (this.mListenForHeadsUp && !this.mHeadsUpTouchHelper.isTrackingHeadsUp() && this.mHeadsUpTouchHelper.onInterceptTouchEvent(motionEvent)) {
            this.mIsExpansionFromHeadsUp = true;
            MetricsLogger.count(this.mContext, "panel_open_peek", 1);
        }
        if ((!this.mIsExpanding || this.mHintAnimationRunning) && !this.mQsExpanded && this.mStatusBar.getBarState() == 1 && !this.mDozing) {
            z = false | this.mKeyguardMoveHelper.onTouchEvent(motionEvent);
        }
        if (this.mOnlyAffordanceInThisMotion || this.mKeyguardMoveHelper.isInLeftView() || this.mAppMiniWindowManager.onTouchEvent(motionEvent) || this.mHeadsUpTouchHelper.onTouchEvent(motionEvent)) {
            return true;
        }
        if (!isOnKeyguard() && isFullyExpanded() && !this.mQsOverscrollExpansionEnabled && this.mStretching && handleSpringTouch(motionEvent)) {
            return true;
        }
        if (this.mQsOverscrollExpansionEnabled && !this.mHeadsUpTouchHelper.isTrackingHeadsUp() && handleQsTouch(motionEvent)) {
            return true;
        }
        if (motionEvent.getActionMasked() == 0 && isFullyCollapsed()) {
            MetricsLogger.count(this.mContext, "panel_open", 1);
            resetVerticalPanelPosition();
            z = true;
        }
        if (this.mSupportGestureWakeup && this.mOpenDoubleTapGoToSleep && this.mStatusBarState == 1 && !isDoubleTapBoundaryTouchEvent(motionEvent)) {
            this.mDoubleTapHelper.onTouchEvent(motionEvent);
        }
        this.mLockScreenMagazineController.onTouchEvent(motionEvent, this.mStatusBarState);
        if (this.mLockScreenMagazinePreViewVisible) {
            return true;
        }
        this.mKeyguardIndicationController.onTouchEvent(motionEvent, this.mStatusBarState, this.mInitialTouchX, this.mInitialTouchY);
        boolean onTouchEvent = super.onTouchEvent(motionEvent) | z;
        if (this.mDozing) {
            return onTouchEvent;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean isExpandForbiddenInKeyguard() {
        return this.mStatusBar.isKeyguardShowing() && !this.mExpandableUnderKeyguard;
    }

    private boolean handleSpringTouch(MotionEvent motionEvent) {
        int pointerId;
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        if (findPointerIndex < 0) {
            this.mTrackingPointer = motionEvent.getPointerId(0);
            findPointerIndex = 0;
        }
        float x = motionEvent.getX(findPointerIndex);
        float y = motionEvent.getY(findPointerIndex);
        float f = x - this.mInitialTouchX;
        float f2 = y - this.mInitialTouchY;
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            int i = 1;
            if (actionMasked != 1) {
                if (actionMasked != 2) {
                    if (actionMasked != 3) {
                        if (actionMasked == 6 && this.mTrackingPointer == (pointerId = motionEvent.getPointerId(motionEvent.getActionIndex()))) {
                            if (motionEvent.getPointerId(0) != pointerId) {
                                i = 0;
                            }
                            this.mTrackingPointer = motionEvent.getPointerId(i);
                            this.mInitialTouchX = motionEvent.getX(i);
                            this.mInitialTouchY = motionEvent.getY(i);
                        }
                    }
                } else if (this.mNotificationStackScroller.isScrolledToTop() && Math.abs(f2) > Math.abs(f)) {
                    setStretchLength(f2 - ((float) this.mTouchSlop));
                    return true;
                }
            }
            this.mStretching = false;
        } else {
            this.mInitialTouchX = x;
            this.mInitialTouchY = y;
        }
        return false;
    }

    private boolean handleQsTouch(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0 && getExpandedFraction() == 1.0f && this.mStatusBar.getBarState() != 1 && !this.mQsExpanded && this.mQsExpansionEnabled && !this.mNotificationStackScroller.isQsCovered()) {
            this.mQsTracking = true;
            this.mConflictingQsExpansionGesture = true;
            onQsExpansionStarted();
            this.mInitialHeightOnTouch = this.mQsExpansionHeight;
            this.mInitialTouchY = motionEvent.getX();
            this.mInitialTouchX = motionEvent.getY();
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
            MetricsLogger.count(this.mContext, "panel_open_qs", 1);
            this.mQsExpandImmediate = true;
            requestPanelHeightUpdate();
            setListening(true);
        }
        return false;
    }

    private boolean isOpenQsEvent(MotionEvent motionEvent) {
        int pointerCount = motionEvent.getPointerCount();
        int actionMasked = motionEvent.getActionMasked();
        boolean z = actionMasked == 5 && pointerCount == 2;
        boolean z2 = actionMasked == 0 && (motionEvent.isButtonPressed(32) || motionEvent.isButtonPressed(64));
        boolean z3 = actionMasked == 0 && (motionEvent.isButtonPressed(2) || motionEvent.isButtonPressed(4));
        if (z || z2 || z3) {
            return true;
        }
        return false;
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

    /* access modifiers changed from: protected */
    public boolean flingExpands(float f, float f2, float f3, float f4) {
        boolean flingExpands = super.flingExpands(f, f2, f3, f4);
        if (this.mQsExpansionAnimator != null) {
            return true;
        }
        return flingExpands;
    }

    /* access modifiers changed from: protected */
    public boolean hasConflictingGestures() {
        return this.mStatusBar.getBarState() != 0;
    }

    public boolean isKeyguardShowing() {
        return this.mKeyguardShowing;
    }

    /* access modifiers changed from: protected */
    public boolean shouldGestureIgnoreXTouchSlop(float f, float f2) {
        return !this.mKeyguardMoveHelper.isOnAffordanceIcon(f, f2);
    }

    /* access modifiers changed from: protected */
    public boolean isOnBottomIcon(float f, float f2) {
        return this.mKeyguardMoveHelper.isOnAffordanceIcon(f, f2);
    }

    private void onQsTouch(MotionEvent motionEvent) {
        int pointerId;
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        boolean z = false;
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
                if (actionMasked != 2) {
                    if (actionMasked != 3) {
                        if (actionMasked == 6 && this.mTrackingPointer == (pointerId = motionEvent.getPointerId(motionEvent.getActionIndex()))) {
                            if (motionEvent.getPointerId(0) == pointerId) {
                                z = true;
                            }
                            float y2 = motionEvent.getY(z ? 1 : 0);
                            float x2 = motionEvent.getX(z);
                            this.mTrackingPointer = motionEvent.getPointerId(z);
                            this.mInitialHeightOnTouch = this.mQsExpansionHeight;
                            this.mInitialTouchY = y2;
                            this.mInitialTouchX = x2;
                            return;
                        }
                        return;
                    }
                } else if (!isExpandForbiddenInKeyguard()) {
                    setQsExpansion(this.mInitialHeightOnTouch + f);
                    if (f >= ((float) getFalsingThreshold())) {
                        this.mQsTouchAboveFalsingThreshold = true;
                    }
                    trackMovement(motionEvent);
                    return;
                } else {
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
            } else {
                refreshNotificationStackScrollerVisible();
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

    public void onOverscrollTopChanged(float f, boolean z) {
        if (this.mQsOverscrollExpansionEnabled && !this.mNotificationStackScroller.isQsCovered()) {
            cancelQsAnimation();
            if (!this.mQsExpansionEnabled) {
                f = 0.0f;
            }
            if (f < 1.0f) {
                f = 0.0f;
            }
            int i = (f > 0.0f ? 1 : (f == 0.0f ? 0 : -1));
            boolean z2 = true;
            setOverScrolling(i != 0 && z);
            if (i == 0) {
                z2 = false;
            }
            this.mQsExpansionFromOverscroll = z2;
            this.mLastOverscroll = f;
            updateQsState();
            setQsExpansion(((float) this.mQsMinExpansionHeight) + f);
        }
    }

    public void flingTopOverscroll(float f, boolean z) {
        if (this.mQsOverscrollExpansionEnabled && !this.mNotificationStackScroller.isQsCovered()) {
            this.mLastOverscroll = 0.0f;
            this.mQsExpansionFromOverscroll = false;
            setQsExpansion(this.mQsExpansionHeight);
            if (!this.mQsExpansionEnabled && z) {
                f = 0.0f;
            }
            flingSettings(f, z && this.mQsExpansionEnabled, new Runnable() {
                public void run() {
                    boolean unused = NotificationPanelView.this.mStackScrollerOverscrolling = false;
                    NotificationPanelView.this.setOverScrolling(false);
                    NotificationPanelView.this.updateQsState();
                }
            }, false);
        }
    }

    public void onScrollerTopPaddingUpdate(int i) {
        this.mTopPaddingWhenQsBeingCovered = i;
        positionClockAndNotifications();
        float qsMinExpansionHeight = this.mQs.getQsMinExpansionHeight() > this.mQs.getQsHeaderHeight() ? (((float) (this.mQs.getQsMinExpansionHeight() - i)) * 1.0f) / ((float) (this.mQs.getQsMinExpansionHeight() - this.mQs.getQsHeaderHeight())) : 0.0f;
        QS qs = this.mQs;
        if (qs != null && qs.getQsContent() != null && this.mQs.getQsContent().isShown()) {
            float f = 1.0f - (0.100000024f * qsMinExpansionHeight);
            this.mQs.getQsContent().setScaleX(f);
            this.mQs.getQsContent().setScaleY(f);
            this.mQs.getQsContent().setAlpha(1.0f - (qsMinExpansionHeight * 1.0f));
        }
    }

    /* access modifiers changed from: private */
    public void setOverScrolling(boolean z) {
        this.mStackScrollerOverscrolling = z;
        QS qs = this.mQs;
        if (qs != null) {
            qs.setOverscrolling(z);
        }
    }

    private void onQsExpansionStarted() {
        onQsExpansionStarted(0);
    }

    /* access modifiers changed from: protected */
    public void onQsExpansionStarted(int i) {
        cancelQsAnimation();
        cancelHeightAnimator();
        setQsExpansion(this.mQsExpansionHeight - ((float) i));
        requestPanelHeightUpdate();
        this.mNotificationStackScroller.checkSnoozeLeavebehind();
    }

    private void setQsExpanded(boolean z) {
        boolean z2 = this.mQsExpanded != z;
        if (!z && this.mIsKeyguardCoverd) {
            this.mIsKeyguardCoverd = false;
            LockScreenMagazineUtils.sendLockScreenMagazineEventBroadcast(this.mContext, "Wallpaper_Uncovered");
        }
        if (z2) {
            this.mQsExpanded = z;
            sQsExpanded = this.mQsExpanded;
            updateQsState();
            requestPanelHeightUpdate();
            this.mFalsingManager.setQsExpanded(z);
            this.mStatusBar.setQsExpanded(z);
            this.mNotificationContainerParent.setQsExpanded(z);
            if (MiuiKeyguardUtils.isGxzwSensor()) {
                updateGxzwState();
            }
            if (this.mQsExpanded && (this.mLockScreenMagazineController.isSwitchAnimating() || this.mLockScreenMagazinePreViewVisible)) {
                this.mLockScreenMagazineController.reset();
            }
            if (z) {
                ((NotificationStat) Dependency.get(NotificationStat.class)).onOpenQSPanel();
            }
            refreshNotificationStackScrollerVisible();
        }
    }

    public void setBarState(int i, boolean z, boolean z2) {
        long j;
        int i2 = this.mStatusBarState;
        boolean z3 = i == 1;
        setKeyguardStatusViewVisibility(i, z, z2);
        setKeyguardBottomAreaVisibility(i, z2);
        this.mStatusBarState = i;
        setKeyguardOtherViewVisibility();
        if (z3 && !this.mKeyguardShowing) {
            this.mKeyguardWallpaperController.updateWallpaper(false);
            this.mNotificationStackScroller.resetIsQsCovered(false);
        }
        this.mKeyguardShowing = z3;
        QS qs = this.mQs;
        if (qs != null) {
            qs.setKeyguardShowing(this.mKeyguardShowing);
        }
        if (i2 == 1 && (z2 || i == 2)) {
            updateGxzwState();
            if (!((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter() || this.mExpandedHeight <= 30.0f) {
                animateKeyguardStatusBarOut();
                if (this.mStatusBarState == 2) {
                    j = 0;
                } else {
                    j = this.mStatusBar.calculateGoingToFullShadeDelay();
                }
                this.mQs.animateHeaderSlidingIn(j);
            }
        } else if (i2 == 2 && i == 1) {
            updateGxzwState();
            animateKeyguardStatusBarIn(360);
            this.mQs.animateHeaderSlidingOut();
        } else {
            this.mKeyguardStatusBar.setAlpha(1.0f);
            this.mKeyguardStatusBar.setVisibility(z3 ? 0 : 4);
            if (z3 && i2 != this.mStatusBarState) {
                this.mKeyguardBottomArea.onKeyguardShowingChanged();
                QS qs2 = this.mQs;
                if (qs2 != null) {
                    qs2.hideImmediately();
                }
            }
        }
        if (z3) {
            updateDozingVisibilities(false);
        }
        updateNotchCornerVisibility();
        resetVerticalPanelPosition();
        updateQsState();
        updateStatusBarWindowBlur();
        AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
        if (awesomeLockScreen != null) {
            awesomeLockScreen.updatePauseResumeStatus();
        }
    }

    private void animateKeyguardStatusBarOut() {
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mKeyguardStatusBar.getAlpha(), 0.0f});
        ofFloat.addUpdateListener(this.mStatusBarAnimateAlphaListener);
        ofFloat.setStartDelay(this.mStatusBar.isKeyguardFadingAway() ? this.mStatusBar.getKeyguardFadingAwayDelay() : 0);
        ofFloat.setDuration(this.mStatusBar.isKeyguardFadingAway() ? this.mStatusBar.getKeyguardFadingAwayDuration() / 2 : 360);
        ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        ofFloat.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                NotificationPanelView.this.mAnimateKeyguardStatusBarInvisibleEndRunnable.run();
            }
        });
        ofFloat.start();
    }

    private void animateKeyguardStatusBarIn(long j) {
        this.mKeyguardStatusBar.setVisibility(0);
        this.mKeyguardStatusBar.setAlpha(0.0f);
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        ofFloat.addUpdateListener(this.mStatusBarAnimateAlphaListener);
        ofFloat.setDuration(j);
        ofFloat.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
        ofFloat.start();
    }

    private void setKeyguardBottomAreaVisibility(int i, boolean z) {
        this.mKeyguardBottomArea.animate().cancel();
        int i2 = 0;
        if (z || i == 2) {
            this.mKeyguardBottomArea.animate().alpha(0.0f).setStartDelay(0).setDuration(160).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(this.mAnimateKeyguardBottomAreaInvisibleEndRunnable).start();
            MiuiKeyguardFaceUnlockView miuiKeyguardFaceUnlockView = this.mFaceUnlockView;
            if (!this.mUpdateMonitor.isFaceUnlock()) {
                i2 = 4;
            }
            miuiKeyguardFaceUnlockView.setVisibility(i2);
        } else if (i == 1) {
            KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomArea;
            if (!this.mIsDefaultTheme) {
                i2 = 4;
            }
            keyguardBottomAreaView.setVisibility(i2);
            this.mKeyguardBottomArea.setAlpha(1.0f);
            this.mFaceUnlockView.updateFaceUnlockView();
            this.mFaceUnlockView.setAlpha(1.0f);
        } else {
            this.mKeyguardBottomArea.setVisibility(8);
            this.mKeyguardBottomArea.setAlpha(1.0f);
            this.mFaceUnlockView.setVisibility(8);
            this.mFaceUnlockView.setAlpha(1.0f);
        }
    }

    private void setKeyguardOtherViewVisibility() {
        updateThemeBackgroundVisibility();
        boolean z = true;
        int i = 0;
        if (this.mStatusBarState != 1) {
            z = false;
        }
        this.mKeyguardLeftView.setVisibility(z ? 0 : 4);
        View view = this.mSwitchSystemUser;
        if (!z || !shouldShowSwitchSystemUser()) {
            i = 8;
        }
        view.setVisibility(i);
        refreshNotificationStackScrollerVisible();
    }

    public boolean isThemeBgVisible() {
        return !this.mIsDefaultTheme && this.mStatusBarState == 0;
    }

    private void updateThemeBackgroundVisibility() {
        this.mThemeBackgroundView.setVisibility(isThemeBgVisible() ? 0 : 8);
    }

    private void updateNotchCornerVisibility() {
        if (CustomizeUtil.HAS_NOTCH) {
            this.mNotchCorner.setVisibility((!this.mForceBlack || !this.mKeyguardShowing) ? 8 : 0);
        }
    }

    /* access modifiers changed from: protected */
    public void notifyBarPanelExpansionChanged() {
        super.notifyBarPanelExpansionChanged();
        refreshNotificationStackScrollerVisible();
    }

    public void refreshNotificationStackScrollerVisible() {
        this.mNotificationStackScroller.setVisibility((this.mStatusBarState != 1 || this.mIsDefaultTheme || this.mQsTracking || this.mQsExpanded) ? 0 : 4);
    }

    private void setKeyguardStatusViewVisibility(int i, boolean z, boolean z2) {
        if ((z || this.mStatusBarState != 1 || i == 1) && !z2) {
            int i2 = 4;
            if (this.mStatusBarState == 2 && i == 1) {
                this.mKeyguardClockView.animate().cancel();
                KeyguardClockContainer keyguardClockContainer = this.mKeyguardClockView;
                if (this.mIsDefaultTheme) {
                    i2 = 0;
                }
                keyguardClockContainer.setVisibility(i2);
                addAwesomeLockScreenIfNeed();
                this.mKeyguardStatusViewAnimating = true;
                this.mKeyguardClockView.setAlpha(0.0f);
                this.mKeyguardClockView.animate().alpha(1.0f).setStartDelay(0).setDuration(320).setInterpolator(Interpolators.ALPHA_IN).withEndAction(this.mAnimateKeyguardStatusViewVisibleEndRunnable);
            } else if (i == 1) {
                this.mKeyguardClockView.animate().cancel();
                this.mKeyguardStatusViewAnimating = false;
                KeyguardClockContainer keyguardClockContainer2 = this.mKeyguardClockView;
                if (this.mIsDefaultTheme) {
                    i2 = 0;
                }
                keyguardClockContainer2.setVisibility(i2);
                addAwesomeLockScreenIfNeed();
                this.mKeyguardClockView.setAlpha(1.0f);
            } else {
                this.mKeyguardClockView.animate().cancel();
                this.mKeyguardStatusViewAnimating = false;
                this.mKeyguardClockView.setVisibility(8);
                this.mKeyguardClockView.setAlpha(1.0f);
                removeAwesomeLockScreen();
            }
        } else {
            this.mKeyguardClockView.animate().cancel();
            this.mKeyguardStatusViewAnimating = true;
            this.mKeyguardClockView.animate().alpha(0.0f).setStartDelay(0).setDuration(160).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(this.mAnimateKeyguardStatusViewInvisibleEndRunnable);
            if (z) {
                this.mKeyguardClockView.animate().setStartDelay(this.mStatusBar.getKeyguardFadingAwayDelay()).setDuration(this.mStatusBar.getKeyguardFadingAwayDuration() / 2).start();
            }
        }
    }

    private void addAwesomeLockScreenIfNeed() {
        addAwesomeLockScreenIfNeed(false);
    }

    private void addAwesomeLockScreenIfNeed(boolean z) {
        if ((this.mAwesomeLockScreen == null && !this.mIsDefaultTheme) || z) {
            this.mAwesomeLockScreen = new AwesomeLockScreen(this.mContext, this.mStatusBar, this, this.mBar);
            this.mAwesomeLockScreenContainer.removeAllViews();
            this.mAwesomeLockScreenContainer.addView(this.mAwesomeLockScreen);
            this.mAwesomeLockScreen.setIsInteractive(this.mIsInteractive);
        }
        if (this.mAwesomeLockScreen != null) {
            this.mAwesomeLockScreenContainer.setVisibility(0);
        }
    }

    private void removeAwesomeLockScreen() {
        AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
        if (awesomeLockScreen != null) {
            awesomeLockScreen.setIsInteractive(false);
            this.mAwesomeLockScreenContainer.removeAllViews();
            this.mAwesomeLockScreen = null;
            this.mAwesomeLockScreenContainer.setVisibility(8);
        }
    }

    /* access modifiers changed from: private */
    public void updateQsState() {
        this.mNotificationStackScroller.setQsExpanded(this.mQsExpanded);
        this.mNotificationStackScroller.setScrollingEnabled(this.mStatusBarState != 1 && (!this.mQsExpanded || this.mQsExpansionFromOverscroll));
        updateEmptyShadeView();
        KeyguardUserSwitcher keyguardUserSwitcher = this.mKeyguardUserSwitcher;
        if (keyguardUserSwitcher != null && this.mQsExpanded && !this.mStackScrollerOverscrolling) {
            keyguardUserSwitcher.hideIfNotSimple(true);
        }
        QS qs = this.mQs;
        if (qs != null) {
            qs.setExpanded(this.mQsExpanded);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x005e  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0062  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x0102  */
    /* JADX WARNING: Removed duplicated region for block: B:65:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setQsExpansion(float r7) {
        /*
            r6 = this;
            boolean r0 = DEBUG
            if (r0 == 0) goto L_0x001a
            java.lang.String r0 = TAG
            java.lang.StringBuilder r1 = new java.lang.StringBuilder
            r1.<init>()
            java.lang.String r2 = "setQsExpansion height="
            r1.append(r2)
            r1.append(r7)
            java.lang.String r1 = r1.toString()
            android.util.Log.d(r0, r1)
        L_0x001a:
            int r0 = r6.mQsMinExpansionHeight
            float r0 = (float) r0
            float r7 = java.lang.Math.max(r7, r0)
            int r0 = r6.mQsMaxExpansionHeight
            float r0 = (float) r0
            float r7 = java.lang.Math.min(r7, r0)
            boolean r0 = r6.isFullyCollapsed()
            r1 = 0
            r2 = 1
            if (r0 != 0) goto L_0x003b
            int r0 = r6.mQsMaxExpansionHeight
            float r3 = (float) r0
            int r3 = (r7 > r3 ? 1 : (r7 == r3 ? 0 : -1))
            if (r3 != 0) goto L_0x003b
            if (r0 == 0) goto L_0x003b
            r0 = r2
            goto L_0x003c
        L_0x003b:
            r0 = r1
        L_0x003c:
            r6.mQsFullyExpanded = r0
            boolean r0 = r6.mQsFullyExpanded
            if (r0 == 0) goto L_0x004f
            boolean r3 = r6.mIsKeyguardCoverd
            if (r3 == r0) goto L_0x004f
            r6.mIsKeyguardCoverd = r2
            android.content.Context r0 = r6.mContext
            java.lang.String r3 = "Wallpaper_Covered"
            com.android.keyguard.magazine.LockScreenMagazineUtils.sendLockScreenMagazineEventBroadcast(r0, r3)
        L_0x004f:
            int r0 = r6.mQsMinExpansionHeight
            float r0 = (float) r0
            int r0 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r0 <= 0) goto L_0x0062
            boolean r0 = r6.mQsExpanded
            if (r0 != 0) goto L_0x0062
            boolean r0 = r6.mStackScrollerOverscrolling
            if (r0 != 0) goto L_0x0062
            r6.setQsExpanded(r2)
            goto L_0x0087
        L_0x0062:
            int r0 = r6.mQsMinExpansionHeight
            float r0 = (float) r0
            int r0 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r0 > 0) goto L_0x0087
            boolean r0 = r6.mQsExpanded
            if (r0 == 0) goto L_0x0087
            r6.setQsExpanded(r1)
            boolean r0 = r6.mLastAnnouncementWasQuickSettings
            if (r0 == 0) goto L_0x0087
            boolean r0 = r6.mTracking
            if (r0 != 0) goto L_0x0087
            boolean r0 = r6.isCollapsing()
            if (r0 != 0) goto L_0x0087
            java.lang.String r0 = r6.getKeyguardOrLockScreenString()
            r6.announceForAccessibility(r0)
            r6.mLastAnnouncementWasQuickSettings = r1
        L_0x0087:
            r6.mQsExpansionHeight = r7
            r6.updateQsExpansion()
            r6.updateDismissViewState()
            r6.requestScrollerTopPaddingUpdate(r1)
            boolean r0 = r6.mKeyguardShowing
            if (r0 == 0) goto L_0x0099
            r6.updateHeaderKeyguardAlpha()
        L_0x0099:
            int r0 = r6.mStatusBarMinHeight
            float r0 = (float) r0
            int r0 = (r7 > r0 ? 1 : (r7 == r0 ? 0 : -1))
            if (r0 <= 0) goto L_0x00a1
            r1 = r2
        L_0x00a1:
            r6.setListening(r1)
            int r0 = r6.mStatusBarState
            r1 = 2
            if (r0 == r1) goto L_0x00ab
            if (r0 != r2) goto L_0x00bc
        L_0x00ab:
            r6.updateKeyguardClockBottomAreaAlpha()
            boolean r0 = r6.mIsDefaultTheme
            if (r0 != 0) goto L_0x00b9
            com.android.keyguard.AwesomeLockScreen r0 = r6.mAwesomeLockScreen
            if (r0 == 0) goto L_0x00b9
            r0.updateQsExpandHeight(r7)
        L_0x00b9:
            r6.updateStatusBarWindowBlur()
        L_0x00bc:
            java.lang.Class<com.android.systemui.miui.statusbar.policy.ControlPanelController> r0 = com.android.systemui.miui.statusbar.policy.ControlPanelController.class
            java.lang.Object r0 = com.android.systemui.Dependency.get(r0)
            com.android.systemui.miui.statusbar.policy.ControlPanelController r0 = (com.android.systemui.miui.statusbar.policy.ControlPanelController) r0
            boolean r0 = r0.isUseControlCenter()
            r0 = r0 ^ r2
            r1 = 0
            int r7 = (r7 > r1 ? 1 : (r7 == r1 ? 0 : -1))
            if (r7 == 0) goto L_0x00e8
            boolean r7 = r6.mQsFullyExpanded
            if (r7 == 0) goto L_0x00e8
            boolean r7 = r6.mLastAnnouncementWasQuickSettings
            if (r7 != 0) goto L_0x00e8
            if (r0 == 0) goto L_0x00e8
            android.content.Context r7 = r6.getContext()
            r0 = 2131820644(0x7f110064, float:1.9274009E38)
            java.lang.String r7 = r7.getString(r0)
            r6.announceForAccessibility(r7)
            r6.mLastAnnouncementWasQuickSettings = r2
        L_0x00e8:
            boolean r7 = r6.mQsFullyExpanded
            if (r7 == 0) goto L_0x00fe
            com.android.systemui.classifier.FalsingManager r7 = r6.mFalsingManager
            boolean r7 = r7.shouldEnforceBouncer()
            if (r7 == 0) goto L_0x00fe
            com.android.systemui.statusbar.phone.StatusBar r0 = r6.mStatusBar
            r1 = 0
            r2 = 0
            r3 = 0
            r4 = 1
            r5 = 0
            r0.executeRunnableDismissingKeyguard(r1, r2, r3, r4, r5)
        L_0x00fe:
            boolean r7 = DEBUG
            if (r7 == 0) goto L_0x0105
            r6.invalidate()
        L_0x0105:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationPanelView.setQsExpansion(float):void");
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x0028 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0029  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateDismissViewState() {
        /*
            r3 = this;
            boolean r0 = r3.mDismissViewShowUp
            boolean r1 = r3.mOpening
            r2 = 0
            if (r1 != 0) goto L_0x0023
            boolean r1 = r3.mPanelAppeared
            if (r1 == 0) goto L_0x0023
            boolean r1 = r3.isQsDetailShowing()
            if (r1 == 0) goto L_0x0012
            goto L_0x0023
        L_0x0012:
            boolean r1 = r3.mTracking
            if (r1 != 0) goto L_0x0024
            float r0 = r3.getAppearFraction()
            r1 = 1061997773(0x3f4ccccd, float:0.8)
            int r0 = (r0 > r1 ? 1 : (r0 == r1 ? 0 : -1))
            if (r0 <= 0) goto L_0x0023
            r0 = 1
            goto L_0x0024
        L_0x0023:
            r0 = r2
        L_0x0024:
            boolean r1 = r3.mDismissViewShowUp
            if (r1 != r0) goto L_0x0029
            return
        L_0x0029:
            r3.mDismissViewShowUp = r0
            r3.updateDismissView()
            com.android.systemui.statusbar.DismissView r0 = r3.mDismissView
            r0.stopAnimator()
            com.android.systemui.statusbar.DismissView r0 = r3.mDismissView
            int r0 = r0.getVisibility()
            if (r0 != 0) goto L_0x0048
            com.android.systemui.statusbar.DismissView r0 = r3.mDismissView
            r0.clearAccessibilityFocus()
            com.android.systemui.statusbar.DismissView r0 = r3.mDismissView
            android.view.animation.Animation r3 = r3.mDismissViewShowUpAnimation
            r0.startAnimation(r3)
            goto L_0x004d
        L_0x0048:
            com.android.systemui.statusbar.DismissView r3 = r3.mDismissView
            r3.clearAnimation()
        L_0x004d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationPanelView.updateDismissViewState():void");
    }

    /* access modifiers changed from: protected */
    public void updateQsExpansion() {
        QS qs = this.mQs;
        if (qs != null) {
            qs.setQsExpansion(getQsExpansionFraction(), getHeaderTranslation(), getAppearFraction());
        }
    }

    public boolean isQSFullyCollapsed() {
        if (!this.mKeyguardShowing) {
            return isFullyCollapsed();
        }
        QS qs = this.mQs;
        return qs == null || qs.isQSFullyCollapsed();
    }

    private String getKeyguardOrLockScreenString() {
        QS qs = this.mQs;
        if (qs != null && qs.isCustomizing()) {
            return getContext().getString(R.string.accessibility_desc_quick_settings_edit);
        }
        if (this.mStatusBarState == 1) {
            return getContext().getString(R.string.accessibility_desc_lock_screen);
        }
        return getContext().getString(R.string.accessibility_desc_notification_shade);
    }

    private float calculateQsTopPadding() {
        if (!this.mKeyguardShowing || (!this.mQsExpandImmediate && (!this.mIsExpanding || !this.mQsExpandedWhenExpandingStarted))) {
            ValueAnimator valueAnimator = this.mQsSizeChangeAnimator;
            if (valueAnimator != null) {
                return (float) ((Integer) valueAnimator.getAnimatedValue()).intValue();
            }
            ValueAnimator valueAnimator2 = this.mQsTopPaddingAnimator;
            if (valueAnimator2 != null) {
                return (float) ((Integer) valueAnimator2.getAnimatedValue()).intValue();
            }
            if (this.mKeyguardShowing) {
                return interpolate(getQsExpansionFraction(), (float) this.mNotificationStackScroller.getIntrinsicPadding(), (float) this.mQsMaxExpansionHeight);
            }
            if (this.mNotificationStackScroller.isQsCovered()) {
                return (float) this.mQs.getQsHeaderHeight();
            }
            if (this.mNotificationStackScroller.isQsBeingCovered()) {
                return (float) this.mTopPaddingWhenQsBeingCovered;
            }
            return this.mQsExpansionHeight;
        }
        KeyguardClockPositionAlgorithm.Result result = this.mClockPositionResult;
        int i = result.stackScrollerPadding - result.stackScrollerPaddingAdjustment;
        int tempQsMaxExpansion = getTempQsMaxExpansion();
        if (this.mStatusBarState == 1) {
            tempQsMaxExpansion = Math.max(i, tempQsMaxExpansion);
        }
        return (float) ((int) interpolate(getExpandedFraction(), (float) this.mQsMinExpansionHeight, (float) tempQsMaxExpansion));
    }

    /* access modifiers changed from: protected */
    public void requestScrollerTopPaddingUpdate(boolean z) {
        NotificationStackScrollLayout notificationStackScrollLayout = this.mNotificationStackScroller;
        float calculateQsTopPadding = calculateQsTopPadding();
        boolean z2 = true;
        boolean z3 = this.mAnimateNextTopPaddingChange || z;
        if (!this.mKeyguardShowing || (!this.mQsExpandImmediate && (!this.mIsExpanding || !this.mQsExpandedWhenExpandingStarted))) {
            z2 = false;
        }
        notificationStackScrollLayout.updateTopPadding(calculateQsTopPadding, z3, z2);
        this.mAnimateNextTopPaddingChange = false;
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

    private void cancelQsAnimation() {
        ValueAnimator valueAnimator = this.mQsExpansionAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
    }

    public void flingSettings(float f, boolean z) {
        if (isKeyguardShowing()) {
            if (z) {
                ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelExpanded(true, true, this.mNotificationStackScroller.getNotGoneNotifications());
            } else {
                ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelCollapsed(true, true, this.mNotificationStackScroller.getNotGoneNotifications());
            }
        }
        flingSettings(f, z, (Runnable) null, false);
    }

    /* access modifiers changed from: protected */
    public void flingSettings(float f, boolean z, final Runnable runnable, boolean z2) {
        boolean z3;
        float f2 = (float) (z ? this.mQsMaxExpansionHeight : this.mQsMinExpansionHeight);
        if (f2 != this.mQsExpansionHeight) {
            if (this.mPerf != null) {
                this.mPerf.perfHint(4224, this.mContext.getPackageName(), -1, 1);
            }
            if ((f <= 0.0f || z) && (f >= 0.0f || !z)) {
                z3 = false;
            } else {
                f = 0.0f;
                z3 = true;
            }
            ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{this.mQsExpansionHeight, f2});
            if (z2) {
                ofFloat.setInterpolator(Interpolators.TOUCH_RESPONSE);
                ofFloat.setDuration(368);
            } else {
                this.mFlingAnimationUtils.apply((Animator) ofFloat, this.mQsExpansionHeight, f2, f);
            }
            if (z3) {
                ofFloat.setDuration(350);
            }
            ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    NotificationPanelView.this.setQsExpansion(((Float) valueAnimator.getAnimatedValue()).floatValue());
                }
            });
            ofFloat.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    if (NotificationPanelView.this.mPerf != null) {
                        NotificationPanelView.this.mPerf.perfLockRelease();
                    }
                    NotificationPanelView.this.mNotificationStackScroller.resetCheckSnoozeLeavebehind();
                    ValueAnimator unused = NotificationPanelView.this.mQsExpansionAnimator = null;
                    Runnable runnable = runnable;
                    if (runnable != null) {
                        runnable.run();
                    }
                    ((NotificationStat) Dependency.get(NotificationStat.class)).onPanelAnimationEnd();
                }
            });
            ofFloat.start();
            this.mQsExpansionAnimator = ofFloat;
            this.mQsAnimatorExpand = z;
        } else if (runnable != null) {
            runnable.run();
        }
    }

    private boolean shouldQuickSettingsIntercept(float f, float f2, float f3) {
        if (!this.mQsExpansionEnabled || this.mCollapsedOnDown || this.mNotificationStackScroller.isQsCovered()) {
            return false;
        }
        if (this.mQsExpanded && this.mKeyguardShowing && isXWithinQsFrame(f)) {
            return true;
        }
        boolean isInQuickQsArea = isInQuickQsArea(f, f2);
        if (!this.mQsExpanded) {
            return isInQuickQsArea;
        }
        if (isInQuickQsArea || (f3 < 0.0f && isInQsArea(f, f2))) {
            return true;
        }
        return false;
    }

    private boolean isInQuickQsArea(float f, float f2) {
        if (this.mKeyguardShowing) {
            if (!isXWithinQsFrame(f) || f2 < ((float) this.mKeyguardStatusBar.getTop()) || f2 > ((float) this.mKeyguardStatusBar.getBottom())) {
                return false;
            }
            return true;
        } else if (!isXWithinQsFrame(f) || f2 < ((float) this.mQs.getHeader().getTop()) || f2 > ((float) (this.mQs.getHeader().getTop() + this.mQs.getQsMinExpansionHeight()))) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isInQsArea(float f, float f2) {
        return isXWithinQsFrame(f) && (f2 <= this.mNotificationStackScroller.getBottomMostNotificationBottom() || f2 <= this.mQs.getView().getY() + ((float) this.mQs.getView().getHeight()));
    }

    private boolean isXWithinQsFrame(float f) {
        return f >= this.mQsFrame.getX() && f <= this.mQsFrame.getX() + ((float) this.mQsFrame.getWidth());
    }

    /* access modifiers changed from: protected */
    public boolean isScrolledToBottom() {
        if (isInSettings() || this.mStatusBar.getBarState() == 1 || this.mNotificationStackScroller.isScrolledToBottom()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public int getMaxPanelHeight() {
        int i;
        int i2 = this.mStatusBarMinHeight;
        if (this.mStatusBar.getBarState() != 1 && this.mNotificationStackScroller.getNotGoneChildCount() == 0) {
            i2 = Math.max(i2, (int) (((float) this.mQsMinExpansionHeight) + getOverExpansionAmount()));
        }
        if (this.mQsExpandImmediate || this.mQsExpanded || (this.mIsExpanding && this.mQsExpandedWhenExpandingStarted)) {
            i = calculatePanelHeightQsExpanded();
        } else {
            i = calculatePanelHeightShade();
        }
        return Math.max(i, i2);
    }

    public boolean isInSettings() {
        return this.mQsExpanded;
    }

    public boolean isExpanding() {
        return this.mIsExpanding;
    }

    /* access modifiers changed from: protected */
    public void setStretchLength(float f) {
        boolean isScrolledToTop = this.mNotificationStackScroller.isScrolledToTop();
        boolean isScrolledToBottom = this.mNotificationStackScroller.isScrolledToBottom();
        if (isScrolledToTop && !isScrolledToBottom) {
            f = Math.max(0.0f, f);
        } else if (!isScrolledToTop && isScrolledToBottom) {
            f = Math.min(0.0f, f);
        }
        super.setStretchLength(f);
        updateStatusBarWindowBlur();
        updateDismissViewState();
    }

    /* access modifiers changed from: protected */
    public void onSpringLengthUpdated(float f) {
        if (this.mSpringLength != f) {
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "pv onSpringLengthUpdated " + f);
            }
            this.mSpringLength = f;
            QS qs = this.mQs;
            if (qs != null) {
                qs.updateTopPadding(f);
            }
            this.mNotificationStackScroller.onSpringLengthUpdated(f);
        }
    }

    /* access modifiers changed from: protected */
    public void flingSpring(float f, boolean z) {
        String str = TAG;
        Log.d(str, "flingSpring mSpringLength=" + this.mSpringLength + ", expand=" + z + ", vel=" + f);
        if (!z || this.mSpringLength <= 0.0f) {
            flingToPanelHeight(z);
            onSpringLengthUpdated(0.0f);
        } else {
            Folme.getValueTarget("PanelViewSpring").setMinVisibleChange(1.0f, "length");
            IStateStyle useValue = Folme.useValue("PanelViewSpring");
            useValue.setTo("length", Float.valueOf(this.mSpringLength));
            useValue.addListener(new AutoCleanFloatTransitionListener("PanelViewSpring") {
                public void onStart() {
                    NotificationPanelView.this.mNotificationStackScroller.onSpringAnimationStart();
                }

                public final void onUpdate(Map<String, Float> map) {
                    NotificationPanelView.this.onSpringLengthUpdated(map.get("length").floatValue());
                }

                public void onEnd() {
                    NotificationPanelView.this.mNotificationStackScroller.onSpringAnimationEnd();
                }
            });
            AnimConfig animConfig = new AnimConfig();
            animConfig.setEase(-2, 0.7f, 0.5f);
            useValue.to("length", 0, animConfig);
        }
        resetStretchLength(z);
        onTrackingStopped(z);
        updateDismissViewState();
    }

    private void cancelFlingSpring() {
        Log.d(TAG, "cancelFlingSpring");
        this.mNotificationStackScroller.onSpringAnimationCanceled();
        Folme.useValue("PanelViewSpring").cancel();
    }

    /* access modifiers changed from: protected */
    public void onPanelDisplayChanged(boolean z) {
        boolean z2 = this.mPanelAppeared != z;
        this.mPanelAppeared = z;
        if (z2) {
            if (DEBUG) {
                String str = TAG;
                Log.d(str, "pv onPanelDisplayChanged " + z);
            }
            QS qs = this.mQs;
            if (qs != null) {
                qs.onPanelDisplayChanged(z, false);
            }
            this.mNotificationStackScroller.onPanelDisplayChanged(z);
            updateEmptyShadeView();
        }
        if (z2 && !z) {
            cancelFlingSpring();
        }
    }

    /* access modifiers changed from: protected */
    public void onHeightUpdated(float f) {
        int i;
        float f2;
        if (DEBUG) {
            Log.d(TAG, "onHeightUpdated expandedHeight=" + f);
        }
        if (!this.mQsExpanded || this.mQsExpandImmediate || (this.mIsExpanding && this.mQsExpandedWhenExpandingStarted)) {
            positionClockAndNotifications();
        }
        if (this.mQsExpandImmediate || (this.mQsExpanded && !this.mQsTracking && this.mQsExpansionAnimator == null && !this.mQsExpansionFromOverscroll)) {
            if (this.mKeyguardShowing) {
                f2 = f / ((float) getMaxPanelHeight());
            } else {
                float intrinsicPadding = (float) (this.mNotificationStackScroller.getIntrinsicPadding() + this.mNotificationStackScroller.getLayoutMinHeight());
                f2 = (f - intrinsicPadding) / (((float) calculatePanelHeightQsExpanded()) - intrinsicPadding);
            }
            setQsExpansion(((float) this.mQsMinExpansionHeight) + (f2 * ((float) (getTempQsMaxExpansion() - this.mQsMinExpansionHeight))));
        }
        updateExpandedHeight(f);
        updateHeader();
        updateNotificationTranslucency();
        updatePanelExpanded();
        updateStatusBarWindowBlur();
        View statusBarView = this.mStatusBar.getStatusBarView();
        boolean isUseControlCenter = ((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter();
        boolean z = false;
        if (!isUseControlCenter && statusBarView != null && !isKeyguardShowing()) {
            statusBarView.setVisibility(this.mExpandedHeight > 30.0f ? 4 : 0);
        }
        boolean z2 = isUseControlCenter && this.mExpandedHeight > 30.0f && (i = this.mStatusBarState) != 1 && i != 2;
        boolean z3 = isUseControlCenter && this.mExpandedHeight > 30.0f && this.mStatusBarState == 2;
        ((LightBarController) Dependency.get(LightBarController.class)).statusBarExpandChanged(z2, this.mStatusbarExpandIconsDark);
        this.mKeyguardStatusBar.statusBarExpandChanged(z3, this.mStatusbarExpandIconsDark);
        NotificationStackScrollLayout notificationStackScrollLayout = this.mNotificationStackScroller;
        if (!isFullyCollapsed()) {
            z = true;
        }
        notificationStackScrollLayout.setShadeExpanded(z);
        if (DEBUG) {
            invalidate();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x00f3  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x011f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateStatusBarWindowBlur() {
        /*
            r10 = this;
            boolean r0 = r10.mIsInteractive
            r1 = 1065353216(0x3f800000, float:1.0)
            r2 = 0
            r3 = 0
            if (r0 != 0) goto L_0x000c
        L_0x0008:
            r1 = r2
        L_0x0009:
            r0 = r3
            goto L_0x00eb
        L_0x000c:
            boolean r0 = r10.isPanelVisibleBecauseOfHeadsUp()
            if (r0 == 0) goto L_0x001c
            boolean r0 = r10.mClearBlurRegistered
            if (r0 == 0) goto L_0x001c
            r0 = -1
            r9 = r3
            r3 = r0
            r0 = r9
            goto L_0x00eb
        L_0x001c:
            int r0 = r10.mStatusBarState
            r4 = 2
            r5 = 1
            if (r0 != 0) goto L_0x00cc
            boolean r0 = r10.mOpening
            java.lang.String r6 = "PanelViewBlur"
            if (r0 != 0) goto L_0x006a
            float r0 = r10.mStretchLength
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 >= 0) goto L_0x002f
            goto L_0x006a
        L_0x002f:
            java.lang.Object[] r0 = new java.lang.Object[r5]
            r0[r3] = r6
            miuix.animation.IStateStyle r0 = miuix.animation.Folme.useValue(r0)
            r0.cancel()
            float r0 = r10.mExpandedHeight
            float r0 = r0 * r1
            float r4 = r10.getPeekHeight()
            float r0 = r0 / r4
            float r0 = java.lang.Math.max(r2, r0)
            float r0 = java.lang.Math.min(r0, r1)
            boolean r1 = r10.mFlingAfterTracking
            if (r1 != 0) goto L_0x0056
            boolean r1 = r10.isTracking()
            if (r1 == 0) goto L_0x0055
            goto L_0x0056
        L_0x0055:
            r5 = r3
        L_0x0056:
            if (r5 == 0) goto L_0x005e
            android.view.animation.Interpolator r1 = com.android.systemui.Interpolators.DECELERATE_QUINT
            float r0 = r1.getInterpolation(r0)
        L_0x005e:
            boolean r1 = r10.mKeyguardBouncerShowing
            if (r1 == 0) goto L_0x0068
            float r1 = r10.mKeyguardBouncerFraction
            float r0 = java.lang.Math.max(r1, r0)
        L_0x0068:
            r1 = r0
            goto L_0x0009
        L_0x006a:
            boolean r0 = r10.mOpening
            if (r0 == 0) goto L_0x0070
            r0 = r3
            goto L_0x0071
        L_0x0070:
            r0 = r4
        L_0x0071:
            float r0 = (float) r0
            float r7 = r10.mStretchLength
            r8 = 1117782016(0x42a00000, float:80.0)
            float r7 = r7 / r8
            float r0 = r0 + r7
            float r0 = java.lang.Math.max(r2, r0)
            float r0 = java.lang.Math.min(r0, r1)
            miuix.animation.ValueTarget r1 = miuix.animation.Folme.getValueTarget(r6)
            r2 = 953267991(0x38d1b717, float:1.0E-4)
            java.lang.String r7 = "blurRatio"
            java.lang.String[] r8 = new java.lang.String[]{r7}
            r1.setMinVisibleChange((float) r2, (java.lang.String[]) r8)
            float r1 = r10.mBlurRatio
            int r1 = (r1 > r0 ? 1 : (r1 == r0 ? 0 : -1))
            if (r1 == 0) goto L_0x00c0
            java.lang.Object[] r1 = new java.lang.Object[r5]
            r1[r3] = r6
            miuix.animation.IStateStyle r1 = miuix.animation.Folme.useValue(r1)
            java.lang.Object[] r2 = new java.lang.Object[r4]
            r2[r3] = r7
            float r6 = r10.mBlurRatio
            java.lang.Float r6 = java.lang.Float.valueOf(r6)
            r2[r5] = r6
            r1.setTo((java.lang.Object[]) r2)
            com.android.systemui.util.AutoCleanFloatTransitionListener r10 = r10.mBlurRatioListener
            r1.addListener(r10)
            java.lang.Object[] r10 = new java.lang.Object[r4]
            r10[r3] = r7
            java.lang.Float r0 = java.lang.Float.valueOf(r0)
            r10[r5] = r0
            r1.to(r10)
            goto L_0x00cb
        L_0x00c0:
            java.lang.Class<com.android.systemui.statusbar.phone.StatusBarWindowManager> r10 = com.android.systemui.statusbar.phone.StatusBarWindowManager.class
            java.lang.Object r10 = com.android.systemui.Dependency.get(r10)
            com.android.systemui.statusbar.phone.StatusBarWindowManager r10 = (com.android.systemui.statusbar.phone.StatusBarWindowManager) r10
            r10.setBlurRatio(r0)
        L_0x00cb:
            return
        L_0x00cc:
            if (r0 != r5) goto L_0x00dd
            boolean r0 = r10.mKeyguardOccluded
            if (r0 != 0) goto L_0x00dd
            com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController r0 = r10.mKeyguardWallpaperController
            boolean r0 = r0.hasKeyguardWallpaperLayer()
            float r1 = r10.calculateBlurInKeyguard()
            goto L_0x00eb
        L_0x00dd:
            int r0 = r10.mStatusBarState
            if (r0 != r4) goto L_0x0008
            android.animation.ValueAnimator r0 = r10.mQsTopPaddingAnimator
            if (r0 == 0) goto L_0x0009
            float r0 = r0.getAnimatedFraction()
            goto L_0x0068
        L_0x00eb:
            r10.mBlurRatio = r1
            boolean r4 = com.android.keyguard.wallpaper.KeyguardWallpaperUtils.isSupportWallpaperBlur()
            if (r4 == 0) goto L_0x011f
            if (r0 == 0) goto L_0x010c
            boolean r0 = r10.mKeyguardBouncerShowing
            if (r0 != 0) goto L_0x010c
            com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController r10 = r10.mKeyguardWallpaperController
            java.lang.String r0 = TAG
            r10.requestWallpaperBlur(r0, r1)
            java.lang.Class<com.android.systemui.statusbar.phone.StatusBarWindowManager> r10 = com.android.systemui.statusbar.phone.StatusBarWindowManager.class
            java.lang.Object r10 = com.android.systemui.Dependency.get(r10)
            com.android.systemui.statusbar.phone.StatusBarWindowManager r10 = (com.android.systemui.statusbar.phone.StatusBarWindowManager) r10
            r10.setBlurRatio(r2, r3)
            goto L_0x012a
        L_0x010c:
            com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController r10 = r10.mKeyguardWallpaperController
            java.lang.String r0 = TAG
            r10.requestWallpaperBlur(r0, r2)
            java.lang.Class<com.android.systemui.statusbar.phone.StatusBarWindowManager> r10 = com.android.systemui.statusbar.phone.StatusBarWindowManager.class
            java.lang.Object r10 = com.android.systemui.Dependency.get(r10)
            com.android.systemui.statusbar.phone.StatusBarWindowManager r10 = (com.android.systemui.statusbar.phone.StatusBarWindowManager) r10
            r10.setBlurRatio(r1, r3)
            goto L_0x012a
        L_0x011f:
            java.lang.Class<com.android.systemui.statusbar.phone.StatusBarWindowManager> r10 = com.android.systemui.statusbar.phone.StatusBarWindowManager.class
            java.lang.Object r10 = com.android.systemui.Dependency.get(r10)
            com.android.systemui.statusbar.phone.StatusBarWindowManager r10 = (com.android.systemui.statusbar.phone.StatusBarWindowManager) r10
            r10.setBlurRatio(r1, r3)
        L_0x012a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationPanelView.updateStatusBarWindowBlur():void");
    }

    private float calculateBlurInKeyguard() {
        if (this.mKeyguardHorizontalMoving) {
            return 0.0f;
        }
        return Math.max(this.mKeyguardBouncerFraction, 1.0f - Math.min(getKeyguardContentsAlpha(), 1.0f - getQsExpansionFraction()));
    }

    private void updatePanelExpanded() {
        boolean z = !isFullyCollapsed();
        if (this.mPanelExpanded != z) {
            this.mHeadsUpManager.setIsExpanded(z);
            this.mStatusBar.setPanelExpanded(z);
            this.mPanelExpanded = z;
            updateGxzwState();
        }
    }

    private int getTempQsMaxExpansion() {
        return this.mQsMaxExpansionHeight;
    }

    private int calculatePanelHeightShade() {
        return (int) (((float) ((this.mNotificationStackScroller.getHeight() - this.mNotificationStackScroller.getEmptyBottomMargin()) - this.mTopPaddingAdjustment)) + this.mNotificationStackScroller.getTopPaddingOverflow());
    }

    private int calculatePanelHeightQsExpanded() {
        float contentHeight = (float) (this.mNotificationStackScroller.getContentHeight() - this.mNotificationStackScroller.getTopPadding());
        if (this.mNotificationStackScroller.getNotGoneChildCount() == 0 && this.mShowEmptyShadeView) {
            contentHeight = (float) this.mNotificationStackScroller.getEmptyShadeViewHeight();
        }
        int i = this.mQsMaxExpansionHeight;
        ValueAnimator valueAnimator = this.mQsSizeChangeAnimator;
        if (valueAnimator != null) {
            i = ((Integer) valueAnimator.getAnimatedValue()).intValue();
        }
        float max = ((float) Math.max(i, this.mStatusBarState == 1 ? this.mClockPositionResult.stackScrollerPadding - this.mTopPaddingAdjustment : 0)) + contentHeight + this.mNotificationStackScroller.getTopPaddingOverflow();
        if (max > ((float) this.mNotificationStackScroller.getHeight())) {
            max = Math.max((float) (i + this.mNotificationStackScroller.getLayoutMinHeight()), (float) this.mNotificationStackScroller.getHeight());
        }
        return (int) max;
    }

    private void updateNotificationTranslucency() {
        float fadeoutAlpha = (!this.mClosingWithAlphaFadeOut || this.mExpandingFromHeadsUp || this.mHeadsUpManager.hasPinnedHeadsUp()) ? 1.0f : getFadeoutAlpha();
        if (!isQsDetailShowing()) {
            this.mNotificationStackScroller.setAlpha(fadeoutAlpha);
        }
    }

    private float getFadeoutAlpha() {
        return (float) Math.pow((double) Math.max(0.0f, Math.min((getNotificationsTopY() + ((float) this.mNotificationStackScroller.getFirstItemMinHeight())) / ((float) this.mQsMinExpansionHeight), 1.0f)), 0.75d);
    }

    /* access modifiers changed from: protected */
    public float getOverExpansionAmount() {
        return this.mNotificationStackScroller.getCurrentOverScrollAmount(true);
    }

    /* access modifiers changed from: protected */
    public float getOverExpansionPixels() {
        return this.mNotificationStackScroller.getCurrentOverScrolledPixels(true);
    }

    private void updateHeader() {
        if (this.mStatusBar.getBarState() == 1) {
            updateHeaderKeyguardAlpha();
        }
        updateQsExpansion();
        updateDismissViewState();
    }

    /* access modifiers changed from: protected */
    public float getHeaderTranslation() {
        if (this.mStatusBar.getBarState() == 1) {
            return 0.0f;
        }
        return Math.min(0.0f, NotificationUtils.interpolate((float) (-this.mQsMinExpansionHeight), 0.0f, this.mNotificationStackScroller.getAppearFraction(this.mExpandedHeight)));
    }

    private float getAppearFraction() {
        return Math.max(Math.min(1.0f, this.mNotificationStackScroller.getAppearFraction(this.mExpandedHeight)), 0.0f);
    }

    private float getKeyguardContentsAlpha() {
        float f;
        float f2;
        if (this.mStatusBar.getBarState() == 1) {
            f2 = getNotificationsTopY();
            f = (float) (this.mKeyguardStatusBar.getHeight() + this.mNotificationsHeaderCollideDistance);
        } else {
            f2 = getNotificationsTopY();
            f = (float) this.mKeyguardStatusBar.getHeight();
        }
        return (float) Math.pow((double) MathUtils.constrain(f2 / f, 0.0f, 1.0f), 0.75d);
    }

    /* access modifiers changed from: private */
    public void updateHeaderKeyguardAlpha() {
        float min = Math.min(getKeyguardContentsAlpha(), 1.0f - Math.min(1.0f, getQsExpansionFraction() * 2.0f)) * this.mKeyguardStatusBarAnimateAlpha;
        this.mKeyguardStatusBar.setAlpha(min);
        KeyguardStatusBarView keyguardStatusBarView = this.mKeyguardStatusBar;
        keyguardStatusBarView.setVisibility((keyguardStatusBarView.getAlpha() == 0.0f || this.mDozing || !this.mKeyguardShowing) ? 4 : 0);
        this.mLockScreenMagazineController.setWallPaperViewsAlpha(min);
    }

    private void updateKeyguardClockBottomAreaAlpha() {
        float min = Math.min(getKeyguardContentsAlpha(), 1.0f - getQsExpansionFraction());
        this.mKeyguardBottomArea.setAlpha(min);
        this.mKeyguardClockView.setAlpha(min);
        this.mFaceUnlockView.setAlpha(min);
        int i = min == 0.0f ? 4 : 0;
        this.mKeyguardBottomArea.setImportantForAccessibility(i);
        this.mKeyguardClockView.setImportantForAccessibility(i);
        invalidate();
    }

    private float getNotificationsTopY() {
        if (this.mNotificationStackScroller.getNotGoneChildCount() == 0) {
            return getExpandedHeight();
        }
        return this.mNotificationStackScroller.getNotificationsTopY();
    }

    /* access modifiers changed from: protected */
    public void onExpandingStarted() {
        super.onExpandingStarted();
        this.mNotificationStackScroller.onExpansionStarted();
        this.mIsExpanding = true;
        this.mQsExpandedWhenExpandingStarted = this.mQsFullyExpanded;
        if (this.mQsExpanded) {
            onQsExpansionStarted();
        }
        setHeaderListening(true);
    }

    /* access modifiers changed from: protected */
    public void onExpandingFinished() {
        super.onExpandingFinished();
        this.mNotificationStackScroller.onExpansionStopped();
        this.mHeadsUpManager.onExpandingFinished();
        this.mIsExpanding = false;
        if (isFullyCollapsed()) {
            DejankUtils.postAfterTraversal(new Runnable() {
                public final void run() {
                    NotificationPanelView.this.lambda$onExpandingFinished$1$NotificationPanelView();
                }
            });
            postOnAnimation(new Runnable() {
                public void run() {
                    NotificationPanelView.this.getParent().invalidateChild(NotificationPanelView.this, NotificationPanelView.mDummyDirtyRect);
                }
            });
        } else {
            setListening(true);
        }
        this.mQsExpandImmediate = false;
        this.mTwoFingerQsExpandPossible = false;
        this.mIsExpansionFromHeadsUp = false;
        this.mNotificationStackScroller.setTrackingHeadsUp(false);
        this.mExpandingFromHeadsUp = false;
        setPanelScrimMinFraction(0.0f);
    }

    public /* synthetic */ void lambda$onExpandingFinished$1$NotificationPanelView() {
        setListening(false);
        AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
        if (awesomeLockScreen != null) {
            awesomeLockScreen.updatePauseResumeStatus();
        }
    }

    private void setListening(boolean z) {
        QS qs = this.mQs;
        if (qs != null) {
            qs.setListening(z);
        }
    }

    public void setHeaderListening(boolean z) {
        QS qs = this.mQs;
        if (qs != null) {
            qs.setHeaderListening(z);
        }
    }

    public void expand(boolean z) {
        super.expand(z);
        AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
        if (awesomeLockScreen != null) {
            awesomeLockScreen.updatePauseResumeStatus();
        }
        setListening(true);
    }

    /* access modifiers changed from: protected */
    public void setOverExpansion(float f, boolean z) {
        if (!this.mConflictingQsExpansionGesture && !this.mQsExpandImmediate && !this.mNotificationStackScroller.isQsCovered() && this.mStatusBar.getBarState() != 1) {
            this.mNotificationStackScroller.setOnHeightChangedListener((ExpandableView.OnHeightChangedListener) null);
            if (z) {
                this.mNotificationStackScroller.setOverScrolledPixels(f, true, false);
            } else {
                this.mNotificationStackScroller.setOverScrollAmount(f, true, false);
            }
            this.mNotificationStackScroller.setOnHeightChangedListener(this);
        }
    }

    /* access modifiers changed from: protected */
    public void onTrackingStarted() {
        this.mFalsingManager.onTrackingStarted();
        super.onTrackingStarted();
        if (this.mQsFullyExpanded) {
            this.mQsExpandImmediate = true;
        }
        this.mNotificationStackScroller.onPanelTrackingStarted();
    }

    /* access modifiers changed from: protected */
    public void onTrackingStopped(boolean z) {
        this.mFalsingManager.onTrackingStopped();
        super.onTrackingStopped(z);
        if (z) {
            this.mNotificationStackScroller.setOverScrolledPixels(0.0f, true, true);
        }
        this.mNotificationStackScroller.onPanelTrackingStopped();
        if (!z) {
            return;
        }
        if ((this.mStatusBar.getBarState() == 1 || this.mStatusBar.getBarState() == 2) && !this.mHintAnimationRunning && this.mQsExpansionHeight == 0.0f) {
            this.mKeyguardMoveHelper.reset(true);
        }
    }

    public void onHeightChanged(ExpandableView expandableView, boolean z) {
        if (expandableView != null || !this.mQsExpanded) {
            ExpandableView firstChildNotGone = this.mNotificationStackScroller.getFirstChildNotGone();
            ExpandableNotificationRow expandableNotificationRow = firstChildNotGone instanceof ExpandableNotificationRow ? (ExpandableNotificationRow) firstChildNotGone : null;
            if (expandableNotificationRow != null && (expandableView == expandableNotificationRow || expandableNotificationRow.getNotificationParent() == expandableNotificationRow)) {
                requestScrollerTopPaddingUpdate(false);
            }
            requestPanelHeightUpdate();
        }
    }

    public void onQsHeightChanged() {
        int i = this.mQsMaxExpansionHeight;
        QS qs = this.mQs;
        this.mQsMaxExpansionHeight = qs != null ? qs.getDesiredHeight() : 0;
        if (this.mQsExpanded && this.mQsFullyExpanded) {
            this.mQsExpansionHeight = (float) this.mQsMaxExpansionHeight;
            requestScrollerTopPaddingUpdate(false);
            requestPanelHeightUpdate();
            int i2 = this.mQsMaxExpansionHeight;
            if (i2 != i) {
                startQsSizeChangeAnimation(i, i2);
            }
        }
    }

    private void reInflateThemeBackgroundView() {
        int indexOfChild = indexOfChild(this.mThemeBackgroundView);
        removeView(this.mThemeBackgroundView);
        this.mThemeBackgroundView = LayoutInflater.from(getContext()).inflate(R.layout.notification_panel_window_bg, (ViewGroup) null, false);
        addView(this.mThemeBackgroundView, indexOfChild);
        updateThemeBackgroundVisibility();
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mMiuiStatusBarPromptController.updateTouchRegion();
        this.mNavigationBarBottomHeight = windowInsets.getStableInsetBottom();
        updateMaxHeadsUpTranslation();
        return windowInsets;
    }

    private void updateMaxHeadsUpTranslation() {
        this.mNotificationStackScroller.setHeadsUpBoundaries(getHeight(), this.mNavigationBarBottomHeight);
    }

    public void onRtlPropertiesChanged(int i) {
        if (i != this.mOldLayoutDirection) {
            this.mKeyguardMoveHelper.onRtlPropertiesChanged();
            this.mOldLayoutDirection = i;
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.expand_indicator) {
            onQsExpansionStarted();
            if (this.mQsExpanded) {
                flingSettings(0.0f, false, (Runnable) null, true);
            } else if (this.mQsExpansionEnabled) {
                this.mLockscreenGestureLogger.write(getContext(), 195, 0, 0);
                flingSettings(0.0f, true, (Runnable) null, true);
            }
        }
    }

    public void onAnimationToSideStarted(boolean z, float f, float f2) {
        if (getLayoutDirection() != 1) {
            z = !z;
        }
        this.mIsLaunchTransitionRunning = true;
        this.mLaunchAnimationEndRunnable = null;
        float displayDensity = this.mStatusBar.getDisplayDensity();
        int abs = Math.abs((int) (f / displayDensity));
        int abs2 = Math.abs((int) (f2 / displayDensity));
        if (z) {
            this.mLockscreenGestureLogger.write(getContext(), 190, abs, abs2);
            this.mFalsingManager.onLeftAffordanceOn();
        } else {
            if ("lockscreen_affordance".equals(this.mLastCameraLaunchSource)) {
                this.mLockscreenGestureLogger.write(getContext(), 189, abs, abs2);
            }
            this.mFalsingManager.onCameraOn();
            this.mKeyguardBottomArea.launchCamera(this.mLastCameraLaunchSource);
        }
        this.mStatusBar.startLaunchTransitionTimeout();
        this.mBlockTouches = true;
    }

    public void triggerAction(boolean z, float f, float f2) {
        if (z) {
            this.mKeyguardBottomArea.launchCamera(this.mLastCameraLaunchSource);
        } else if (this.mUpdateMonitor.isSupportLockScreenMagazineLeft()) {
            this.mKeyguardBottomArea.launchLockScreenMagazine();
        }
    }

    public void onAnimationToSideEnded() {
        this.mIsLaunchTransitionRunning = false;
        this.mIsLaunchTransitionFinished = true;
        Runnable runnable = this.mLaunchAnimationEndRunnable;
        if (runnable != null) {
            runnable.run();
            this.mLaunchAnimationEndRunnable = null;
        }
        this.mStatusBar.readyForKeyguardDone();
    }

    public float getMaxTranslationDistance() {
        return (float) Math.hypot((double) getWidth(), (double) getHeight());
    }

    public void onSwipingStarted() {
        requestDisallowInterceptTouchEvent(true);
        this.mOnlyAffordanceInThisMotion = true;
        this.mQsTracking = false;
    }

    public void onSwipingAborted() {
        this.mFalsingManager.onAffordanceSwipingAborted();
    }

    public KeyguardAffordanceView getBottomIcon(boolean z) {
        KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomArea;
        return z ? keyguardBottomAreaView.getRightView() : keyguardBottomAreaView.getLeftView();
    }

    public ViewGroup getBottomIconLayout(boolean z) {
        KeyguardBottomAreaView keyguardBottomAreaView = this.mKeyguardBottomArea;
        return z ? keyguardBottomAreaView.getRightViewLayout() : keyguardBottomAreaView.getLeftViewLayout();
    }

    public IntentButtonProvider.IntentButton.IconState getBottomButtonIconState(boolean z) {
        return this.mKeyguardBottomArea.getIconState(z);
    }

    public boolean isInCenterScreen() {
        return this.mKeyguardMoveHelper.isInCenterScreen();
    }

    public MiuiKeyguardMoveLeftViewContainer getLeftView() {
        return this.mKeyguardLeftView;
    }

    public View getLeftViewBg() {
        return this.mLeftViewBg;
    }

    public boolean isKeyguardWallpaperCarouselSwitchAnimating() {
        return this.mLockScreenMagazineController.isSwitchAnimating();
    }

    public List<View> getLockScreenView() {
        return this.mMoveListViews;
    }

    public boolean needsAntiFalsing() {
        return this.mStatusBarState == 1;
    }

    public View getFaceUnlockView() {
        return this.mFaceUnlockView;
    }

    /* access modifiers changed from: protected */
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
    public boolean shouldUseDismissingAnimation() {
        return this.mStatusBarState != 0 && (!this.mStatusBar.isKeyguardCurrentlySecure() || !isTracking());
    }

    /* access modifiers changed from: protected */
    public boolean isTrackingBlocked() {
        return this.mConflictingQsExpansionGesture && this.mQsExpanded;
    }

    public boolean isQsExpanded() {
        return this.mQsExpanded;
    }

    public boolean isQsDetailShowing() {
        QS qs = this.mQs;
        return qs != null && (qs.isCustomizing() || this.mQs.isShowingDetail());
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

    public void setLaunchTransitionEndRunnable(Runnable runnable) {
        this.mLaunchAnimationEndRunnable = runnable;
    }

    public void setEmptyDragAmount(float f) {
        float f2 = 0.4f;
        if (this.mNotificationStackScroller.getNotGoneChildCount() <= 0 && this.mStatusBar.hasActiveNotifications()) {
            f2 = 0.8f;
        }
        this.mEmptyDragAmount = f * f2;
        positionClockAndNotifications();
    }

    public void setDozing(boolean z, boolean z2) {
        if (z != this.mDozing) {
            this.mDozing = z;
            if (this.mStatusBarState == 1) {
                updateDozingVisibilities(z2);
            }
        }
    }

    private void updateDozingVisibilities(boolean z) {
        if (this.mDozing) {
            this.mKeyguardStatusBar.setVisibility(4);
            this.mKeyguardBottomArea.setDozing(this.mDozing, z);
            return;
        }
        this.mKeyguardStatusBar.setVisibility(0);
        this.mKeyguardBottomArea.setDozing(this.mDozing, z);
        if (z) {
            animateKeyguardStatusBarIn(700);
        }
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    public void showEmptyShadeView(boolean z) {
        this.mShowEmptyShadeView = z;
        updateEmptyShadeView();
    }

    private void updateEmptyShadeView() {
        int i = 0;
        boolean z = ((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter() && this.mPanelAppeared && this.mShowEmptyShadeView;
        TextView textView = this.mEmptyShadeView;
        if (!z) {
            i = 8;
        }
        textView.setVisibility(i);
    }

    public void setKeyguardUserSwitcher(KeyguardUserSwitcher keyguardUserSwitcher) {
        this.mKeyguardUserSwitcher = keyguardUserSwitcher;
    }

    public void onStartedWakingUp() {
        if (this.mIsDefaultTheme && this.mStatusBarState == 1 && !((MiuiFastUnlockController) Dependency.get(MiuiFastUnlockController.class)).isFastUnlock() && !this.mUpdateMonitor.isFingerprintUnlock()) {
            float dimension = this.mContext.getResources().getDimension(R.dimen.keyguard_clock_tranlation_y);
            this.mKeyguardClockView.startAnimation(ViewAnimationUtils.generalWakeupTranslateAnimation(dimension));
            List<View> keyguardNotificationsViewList = getKeyguardNotificationsViewList(this.mStatusBar.mMaxAllowedKeyguardNotifications);
            for (int i = 0; i < keyguardNotificationsViewList.size(); i++) {
                Animation generalWakeupTranslateAnimation = ViewAnimationUtils.generalWakeupTranslateAnimation(dimension);
                generalWakeupTranslateAnimation.setStartOffset(((long) i) * 50);
                keyguardNotificationsViewList.get(i).startAnimation(generalWakeupTranslateAnimation);
            }
            this.mKeyguardBottomArea.getLeftView().startAnimation(ViewAnimationUtils.generalWakeupScaleAimation());
            this.mKeyguardBottomArea.getRightView().startAnimation(ViewAnimationUtils.generalWakeupScaleAimation());
            this.mKeyguardStatusBar.startAnimation(ViewAnimationUtils.generalWakeupAlphaAimation());
            if (this.mLockScreenMagazinePreView.getMainLayout().getVisibility() == 0) {
                this.mLockScreenMagazinePreView.getMainLayout().startAnimation(ViewAnimationUtils.generalWakeupAlphaAimation());
            }
        }
        this.mIsInteractive = true;
        this.mKeyguardClockView.updateTime();
        this.mKeyguardMoveHelper.resetImmediately();
        AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
        if (awesomeLockScreen != null) {
            awesomeLockScreen.setIsInteractive(this.mIsInteractive);
        }
    }

    public void onStartedGoingToSleep() {
        this.mIsInteractive = false;
        AwesomeLockScreen awesomeLockScreen = this.mAwesomeLockScreen;
        if (awesomeLockScreen != null) {
            awesomeLockScreen.setIsInteractive(this.mIsInteractive);
        }
    }

    public void onEmptySpaceClicked(float f, float f2) {
        onEmptySpaceClick(f);
    }

    /* access modifiers changed from: protected */
    public boolean onMiddleClicked() {
        int barState = this.mStatusBar.getBarState();
        if (barState == 0) {
            post(this.mPostCollapseRunnable);
            return false;
        } else if (barState != 1) {
            if (barState == 2 && !this.mQsExpanded) {
                this.mStatusBar.goToKeyguard();
            }
            return true;
        } else {
            if (!this.mDozingOnDown) {
                this.mLockscreenGestureLogger.write(getContext(), 188, 0, 0);
            }
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public boolean drawChild(Canvas canvas, View view, long j) {
        if (this.mStatusBarState != 0 && view == this.mNotificationContainerParent && this.mKeyguardWallpaperController.isLegacyKeyguardWallpaper()) {
            float f = 1.0f;
            if (this.mStatusBarState != 2) {
                f = 1.0f - Math.min(getKeyguardContentsAlpha(), 1.0f - getQsExpansionFraction());
            }
            int wallpaperBlurColor = this.mUpdateMonitor.getWallpaperBlurColor();
            if (f > 0.0f && wallpaperBlurColor != -1) {
                this.mMaskPaint.reset();
                this.mMaskPaint.setColor(wallpaperBlurColor);
                this.mMaskPaint.setAlpha((int) (f * 255.0f));
                canvas.drawRect(0.0f, 0.0f, (float) getWidth(), (float) getHeight(), this.mMaskPaint);
            }
        }
        return super.drawChild(canvas, view, j);
    }

    /* access modifiers changed from: protected */
    public void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public void onHeadsUpPinnedModeChanged(boolean z) {
        this.mNotificationStackScroller.setInHeadsUpPinnedMode(z);
        if (z) {
            this.mHeadsUpExistenceChangedRunnable.run();
            updateNotificationTranslucency();
        } else {
            setHeadsUpAnimatingAway(true);
            this.mNotificationStackScroller.runAfterAnimationFinished(this.mHeadsUpExistenceChangedRunnable);
        }
        updateClearBlurInfo(z);
    }

    private void updateClearBlurInfo(boolean z) {
        if (z) {
            registerClearBlur();
            return;
        }
        if (getExpandedHeight() <= 0.0f) {
            this.mNotificationStackScroller.runAfterAnimationFinished(new Runnable() {
                public final void run() {
                    NotificationPanelView.this.unregisterClearBlur();
                }
            });
        } else {
            unregisterClearBlur();
        }
    }

    private void registerClearBlur() {
        RenderLayerManager.getInstance().register(this.mClearBlurInfo);
        RenderLayerManager.getInstance().runAfterDraw(getViewRootImpl(), new Runnable() {
            public final void run() {
                NotificationPanelView.this.lambda$registerClearBlur$3$NotificationPanelView();
            }
        });
    }

    public /* synthetic */ void lambda$registerClearBlur$3$NotificationPanelView() {
        post(new Runnable() {
            public final void run() {
                NotificationPanelView.this.lambda$registerClearBlur$2$NotificationPanelView();
            }
        });
    }

    public /* synthetic */ void lambda$registerClearBlur$2$NotificationPanelView() {
        this.mClearBlurRegistered = true;
        updateStatusBarWindowBlur();
    }

    /* access modifiers changed from: private */
    public void unregisterClearBlur() {
        this.mClearBlurRegistered = false;
        updateStatusBarWindowBlur();
        RenderLayerManager.getInstance().unregister(this.mClearBlurInfo);
    }

    public void setHeadsUpAnimatingAway(boolean z) {
        this.mHeadsUpAnimatingAway = z;
        this.mNotificationStackScroller.setHeadsUpAnimatingAway(z);
    }

    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
        this.mNotificationStackScroller.onHeadsUpPinned(expandableNotificationRow);
        updateStatusBarWindowBlur();
    }

    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
        this.mNotificationStackScroller.onHeadsUpUnPinned(expandableNotificationRow);
    }

    public void onHeadsUpStateChanged(NotificationData.Entry entry, boolean z) {
        this.mNotificationStackScroller.onHeadsUpStateChanged(entry, z);
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        super.setHeadsUpManager(headsUpManager);
        this.mHeadsUpTouchHelper = new HeadsUpTouchHelper(headsUpManager, this.mNotificationStackScroller, this, this.mStatusBar);
    }

    public void setTrackingHeadsUp(boolean z) {
        if (z) {
            this.mNotificationStackScroller.setTrackingHeadsUp(true);
            this.mExpandingFromHeadsUp = true;
        }
    }

    /* access modifiers changed from: protected */
    public void onClosingFinished() {
        super.onClosingFinished();
        resetVerticalPanelPosition();
        setClosingWithAlphaFadeout(false);
        QS qs = this.mQs;
        if (!(qs == null || qs.getQsContent() == null || this.mExpandedHeight != 0.0f)) {
            this.mQs.getQsContent().setScaleY(1.0f);
            this.mQs.getQsContent().setScaleX(1.0f);
            this.mQs.getQsContent().setAlpha(1.0f);
            this.mQs.onPanelDisplayChanged(false, true);
        }
        NotificationStackScrollLayout notificationStackScrollLayout = this.mNotificationStackScroller;
        if (notificationStackScrollLayout != null) {
            notificationStackScrollLayout.setScaleX(1.0f);
            this.mNotificationStackScroller.setScaleY(1.0f);
        }
        if (this.mQsFrame.getMeasuredWidth() > getMeasuredWidth() || this.mNotificationStackScroller.getMeasuredWidth() > getMeasuredWidth()) {
            Slog.i(TAG, "Force update resources to ensure width is correct.");
            post(new Runnable() {
                public final void run() {
                    NotificationPanelView.this.lambda$onClosingFinished$4$NotificationPanelView();
                }
            });
        }
    }

    public /* synthetic */ void lambda$onClosingFinished$4$NotificationPanelView() {
        updateResources(false);
    }

    private void setClosingWithAlphaFadeout(boolean z) {
        this.mClosingWithAlphaFadeOut = z;
        this.mNotificationStackScroller.forceNoOverlappingRendering(z);
    }

    private void resetVerticalPanelPosition() {
        setVerticalPanelTranslation(0.0f);
    }

    /* access modifiers changed from: protected */
    public void setVerticalPanelTranslation(float f) {
        this.mNotificationStackScroller.setTranslationX(f);
        this.mQsFrame.setTranslationX(f);
    }

    /* access modifiers changed from: protected */
    public void updateExpandedHeight(float f) {
        if (this.mTracking) {
            this.mNotificationStackScroller.setExpandingVelocity(getCurrentExpandVelocity());
        }
        this.mNotificationStackScroller.setExpandedHeight(f);
        updateKeyguardClockBottomAreaAlpha();
        updateStatusBarIcons();
    }

    public boolean isFullWidth() {
        return this.mIsFullWidth;
    }

    private void updateStatusBarIcons() {
        boolean z = isFullWidth() && getExpandedHeight() < getOpeningHeight();
        if (z && this.mNoVisibleNotifications && isOnKeyguard()) {
            z = false;
        }
        if (z != this.mShowIconsWhenExpanded) {
            this.mShowIconsWhenExpanded = z;
            this.mStatusBar.recomputeDisableFlags(false);
        }
    }

    public boolean isOnKeyguard() {
        return this.mStatusBar.getBarState() == 1;
    }

    public void setPanelScrimMinFraction(float f) {
        this.mBar.panelScrimMinFractionChanged(f);
    }

    /* access modifiers changed from: protected */
    public boolean isPanelVisibleBecauseOfHeadsUp() {
        return this.mHeadsUpManager.hasPinnedHeadsUp() || this.mHeadsUpAnimatingAway;
    }

    public boolean hasOverlappingRendering() {
        return !this.mDozing;
    }

    public void launchCamera(boolean z, int i) {
        boolean z2 = true;
        if (i == 1) {
            this.mLastCameraLaunchSource = "power_double_tap";
        } else if (i == 0) {
            this.mLastCameraLaunchSource = "wiggle_gesture";
        } else {
            this.mLastCameraLaunchSource = "lockscreen_affordance";
        }
        if (!isFullyCollapsed()) {
            this.mLaunchingAffordance = true;
            setLaunchingAffordance(true);
        } else {
            z = false;
        }
        KeyguardMoveHelper keyguardMoveHelper = this.mKeyguardMoveHelper;
        if (getLayoutDirection() != 1) {
            z2 = false;
        }
        keyguardMoveHelper.launchAffordance(z, z2);
    }

    public void onAffordanceLaunchEnded() {
        this.mLaunchingAffordance = false;
        setLaunchingAffordance(false);
    }

    public void setAlpha(float f) {
        super.setAlpha(f);
        updateFullyVisibleState(false);
    }

    public void notifyStartFading() {
        updateFullyVisibleState(true);
    }

    public void setVisibility(int i) {
        StatusBar statusBar = this.mStatusBar;
        if (statusBar == null || !statusBar.isAodUsingSuperWallpaper() || i != 0 || this.mIsInteractive) {
            super.setVisibility(i);
            updateFullyVisibleState(false);
        }
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            updateGxzwState();
        }
    }

    private void updateFullyVisibleState(boolean z) {
        this.mNotificationStackScroller.setParentNotFullyVisible((!z && getAlpha() == 1.0f && getVisibility() == 0) ? false : true);
    }

    private void setLaunchingAffordance(boolean z) {
        getBottomIcon(false).setLaunchingAffordance(z);
        getBottomIcon(true).setLaunchingAffordance(z);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0008, code lost:
        r0 = r0.activityInfo;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean canCameraGestureBeLaunched(boolean r2) {
        /*
            r1 = this;
            com.android.systemui.statusbar.phone.KeyguardBottomAreaView r0 = r1.mKeyguardBottomArea
            android.content.pm.ResolveInfo r0 = r0.resolveCameraIntent()
            if (r0 == 0) goto L_0x0010
            android.content.pm.ActivityInfo r0 = r0.activityInfo
            if (r0 != 0) goto L_0x000d
            goto L_0x0010
        L_0x000d:
            java.lang.String r0 = r0.packageName
            goto L_0x0011
        L_0x0010:
            r0 = 0
        L_0x0011:
            if (r0 == 0) goto L_0x0025
            if (r2 != 0) goto L_0x001b
            boolean r2 = r1.isForegroundApp(r0)
            if (r2 != 0) goto L_0x0025
        L_0x001b:
            com.android.systemui.statusbar.phone.KeyguardMoveHelper r1 = r1.mKeyguardMoveHelper
            boolean r1 = r1.isSwipingInProgress()
            if (r1 != 0) goto L_0x0025
            r1 = 1
            goto L_0x0026
        L_0x0025:
            r1 = 0
        L_0x0026:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationPanelView.canCameraGestureBeLaunched(boolean):boolean");
    }

    private boolean isForegroundApp(String str) {
        return str.equals(Util.getTopActivityPkg(getContext()));
    }

    public void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
    }

    public boolean hideStatusBarIconsWhenExpanded() {
        boolean z = this.mContext.getResources().getConfiguration().orientation == 1;
        if (isFullWidth() && this.mShowIconsWhenExpanded) {
            return false;
        }
        if (!isPanelVisibleBecauseOfHeadsUp() || !z) {
            return true;
        }
        return false;
    }

    public void setTouchDisabled(boolean z) {
        super.setTouchDisabled(z);
        if (z && this.mKeyguardMoveHelper.isSwipingInProgress() && !this.mIsLaunchTransitionRunning) {
            this.mKeyguardMoveHelper.resetImmediately();
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        String str2;
        String str3;
        String str4;
        String str5;
        String str6;
        String str7;
        String str8;
        super.dump(fileDescriptor, printWriter, strArr);
        Object[] objArr = new Object[17];
        objArr[0] = Float.valueOf(this.mQsExpansionHeight);
        objArr[1] = Integer.valueOf(this.mQsMinExpansionHeight);
        objArr[2] = Integer.valueOf(this.mQsMaxExpansionHeight);
        String str9 = "T";
        objArr[3] = this.mIntercepting ? str9 : "f";
        if (this.mPanelExpanded) {
            str = str9;
        } else {
            str = "f";
        }
        objArr[4] = str;
        if (this.mQsExpanded) {
            str2 = str9;
        } else {
            str2 = "f";
        }
        objArr[5] = str2;
        if (this.mQsFullyExpanded) {
            str3 = str9;
        } else {
            str3 = "f";
        }
        objArr[6] = str3;
        if (this.mKeyguardShowing) {
            str4 = str9;
        } else {
            str4 = "f";
        }
        objArr[7] = str4;
        if (this.mBlockTouches) {
            str5 = str9;
        } else {
            str5 = "f";
        }
        objArr[8] = str5;
        if (this.mOnlyAffordanceInThisMotion) {
            str6 = str9;
        } else {
            str6 = "f";
        }
        objArr[9] = str6;
        if (this.mHeadsUpTouchHelper.isTrackingHeadsUp()) {
            str7 = str9;
        } else {
            str7 = "f";
        }
        objArr[10] = str7;
        if (this.mConflictingQsExpansionGesture) {
            str8 = str9;
        } else {
            str8 = "f";
        }
        objArr[11] = str8;
        if (!this.mIsExpansionFromHeadsUp) {
            str9 = "f";
        }
        objArr[12] = str9;
        String str10 = "port";
        objArr[13] = isPort(this.mOrientation) ? str10 : "land";
        if (!isPort(getResources().getConfiguration().orientation)) {
            str10 = "land";
        }
        objArr[14] = str10;
        objArr[15] = Float.valueOf(this.mBlurRatio);
        objArr[16] = this.mClockPositionAlgorithm.toString();
        printWriter.println(String.format("      [NotificationPanelView: mQsExpansionHeight=%f mQsMinExpansionHeight=%d mQsMaxExpansionHeight=%d mIntercepting=%s mPanelExpanded=%s mQsExpanded=%s mQsFullyExpanded=%s mKeyguardShowing=%s mBlockTouches=%s mOnlyAffordanceInThisMotion=%s isTrackingHeadsUp=%s mConflictingQsExpansionGesture=%s mIsExpansionFromHeadsUp=%s mOrientation=%s orientation=%s mBlurRatio=%f mClockPositionAlgorithm=%s]", objArr));
        printWriter.println(String.format("      [QsFrame: %s width=%d pv-width=%d]", new Object[]{this.mQsFrame.getLayoutParams().debug(""), Integer.valueOf(this.mQsFrame.getMeasuredWidth()), Integer.valueOf(getMeasuredWidth())}));
        this.mNotificationStackScroller.dump(fileDescriptor, printWriter, strArr);
    }

    public void setDark(boolean z, boolean z2) {
        float f = z ? 1.0f : 0.0f;
        if (this.mDarkAmount != f) {
            ValueAnimator valueAnimator = this.mDarkAnimator;
            if (valueAnimator != null && valueAnimator.isRunning()) {
                this.mDarkAnimator.cancel();
            }
            if (z2) {
                this.mDarkAnimator = ObjectAnimator.ofFloat(this, SET_DARK_AMOUNT_PROPERTY, new float[]{f});
                this.mDarkAnimator.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
                this.mDarkAnimator.setDuration(200);
                this.mDarkAnimator.start();
                return;
            }
            setDarkAmount(f);
        }
    }

    /* access modifiers changed from: private */
    public void setDarkAmount(float f) {
        this.mDarkAmount = f;
        positionClockAndNotifications();
    }

    public void setNoVisibleNotifications(boolean z) {
        this.mNoVisibleNotifications = z;
        QS qs = this.mQs;
        if (qs != null) {
            qs.setHasNotifications(!z);
        }
    }

    public boolean isNoVisibleNotifications() {
        return this.mNoVisibleNotifications;
    }

    public void setDismissView(DismissView dismissView) {
        int i;
        DismissView dismissView2 = this.mDismissView;
        if (dismissView2 != null) {
            i = this.mNotificationContainerParent.indexOfChild(dismissView2);
            this.mNotificationContainerParent.removeView(this.mDismissView);
        } else {
            i = -1;
        }
        this.mDismissView = dismissView;
        this.mNotificationContainerParent.addView(this.mDismissView, i);
    }

    public void tryUpdateDismissView(boolean z) {
        this.mIsStatusBarShowDismissView = z;
        updateDismissView();
    }

    private void updateDismissView() {
        boolean z = true;
        int i = 0;
        boolean z2 = this.mIsStatusBarShowDismissView && this.mDismissViewShowUp;
        if ((this.mDismissView.getVisibility() == 0) != z2) {
            this.mDismissView.setVisibility(z2 ? 0 : 4);
        }
        boolean z3 = getResources().getConfiguration().orientation == 2;
        if (!z2 || z3) {
            z = false;
        }
        NotificationStackScrollLayout notificationStackScrollLayout = this.mNotificationStackScroller;
        int i2 = z ? this.mDismissViewBottomMargin : 0;
        if (z) {
            i = this.mDismissViewSize + (this.mDismissViewBottomMargin * 2);
        }
        notificationStackScrollLayout.setExtraBottomRange(i2, i);
    }

    public void setForceBlack(boolean z) {
        this.mForceBlack = z;
        updateNotchCornerVisibility();
        this.mKeyguardStatusBar.setDarkMode(!this.mForceBlack && KeyguardUpdateMonitor.isWallpaperColorLight(this.mContext));
    }

    public boolean isForceBlack() {
        return this.mForceBlack;
    }

    public void setKeyguardIndicationController(KeyguardIndicationController keyguardIndicationController) {
        this.mKeyguardIndicationController = keyguardIndicationController;
    }

    public void onKeyguardOccludedChanged(boolean z) {
        this.mKeyguardOccluded = z;
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            updateGxzwState();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0068, code lost:
        if (r7 == false) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0090, code lost:
        if (com.android.keyguard.fod.MiuiGxzwManager.getInstance().isDisableLockScreenFod() == false) goto L_0x006a;
     */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x009d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateGxzwState() {
        /*
            r10 = this;
            com.android.keyguard.fod.MiuiGxzwManager r0 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            boolean r0 = r0.isShow()
            if (r0 != 0) goto L_0x000b
            return
        L_0x000b:
            com.android.keyguard.fod.MiuiGxzwManager r0 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            boolean r0 = r0.isDozing()
            r1 = 0
            if (r0 == 0) goto L_0x0031
            com.android.keyguard.fod.MiuiGxzwManager r10 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            boolean r10 = r10.isShouldShowGxzwIcon()
            if (r10 != 0) goto L_0x0028
            java.lang.String r10 = TAG
            java.lang.String r0 = "updateGxzwState: dozing"
            android.util.Log.i(r10, r0)
        L_0x0028:
            com.android.keyguard.fod.MiuiGxzwManager r10 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            r10.dismissGxzwIconView(r1)
            goto L_0x014d
        L_0x0031:
            com.android.keyguard.fod.MiuiGxzwManager r0 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            boolean r0 = r0.getKeyguardAuthen()
            java.lang.String r2 = ", disableFingerprintIcon = "
            r3 = 1
            if (r0 == 0) goto L_0x010c
            com.android.systemui.statusbar.phone.KeyguardMoveHelper r0 = r10.mKeyguardMoveHelper
            boolean r0 = r0.canShowGxzw()
            com.android.keyguard.fod.MiuiGxzwManager r4 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            boolean r4 = r4.isBouncer()
            com.android.keyguard.KeyguardUpdateMonitor r5 = r10.mUpdateMonitor
            boolean r5 = r5.isShowingChargeAnimationWindow()
            com.android.keyguard.fod.MiuiGxzwManager r6 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            boolean r6 = r6.isShowFodInBouncer()
            com.android.keyguard.fod.MiuiGxzwManager r7 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            boolean r7 = r7.isDisableFingerprintIcon()
            if (r4 == 0) goto L_0x006c
            if (r6 == 0) goto L_0x0093
            if (r5 != 0) goto L_0x0093
            if (r7 != 0) goto L_0x0093
        L_0x006a:
            r1 = r3
            goto L_0x0093
        L_0x006c:
            boolean r8 = r10.mQsExpanded
            if (r8 != 0) goto L_0x007e
            boolean r8 = r10.mKeyguardOccluded
            if (r8 != 0) goto L_0x007e
            if (r0 == 0) goto L_0x007e
            boolean r8 = r10.mLockScreenMagazinePreViewVisible
            if (r8 != 0) goto L_0x007e
            if (r5 != 0) goto L_0x007e
            r5 = r3
            goto L_0x007f
        L_0x007e:
            r5 = r1
        L_0x007f:
            if (r5 == 0) goto L_0x0093
            int r5 = r10.mStatusBarState
            r8 = 2
            if (r5 == r8) goto L_0x0093
            if (r7 != 0) goto L_0x0093
            com.android.keyguard.fod.MiuiGxzwManager r5 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            boolean r5 = r5.isDisableLockScreenFod()
            if (r5 != 0) goto L_0x0093
            goto L_0x006a
        L_0x0093:
            com.android.keyguard.fod.MiuiGxzwManager r5 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            boolean r5 = r5.isShouldShowGxzwIcon()
            if (r5 == r1) goto L_0x0102
            java.lang.String r5 = TAG
            java.lang.StringBuilder r8 = new java.lang.StringBuilder
            r8.<init>()
            java.lang.String r9 = "updateGxzwState: mQsExpanded = "
            r8.append(r9)
            boolean r9 = r10.mQsExpanded
            r8.append(r9)
            java.lang.String r9 = ", mKeyguardOccluded = "
            r8.append(r9)
            boolean r9 = r10.mKeyguardOccluded
            r8.append(r9)
            java.lang.String r9 = ", moveHelperCanShow = "
            r8.append(r9)
            r8.append(r0)
            java.lang.String r0 = ", bouncer = "
            r8.append(r0)
            r8.append(r4)
            java.lang.String r0 = ", mLockScreenMagazinePreViewVisible = "
            r8.append(r0)
            boolean r0 = r10.mLockScreenMagazinePreViewVisible
            r8.append(r0)
            java.lang.String r0 = ", isShowFodInBouncer = "
            r8.append(r0)
            r8.append(r6)
            java.lang.String r0 = ", mStatusBarState = "
            r8.append(r0)
            int r10 = r10.mStatusBarState
            r8.append(r10)
            r8.append(r2)
            r8.append(r7)
            java.lang.String r10 = ", disableLockScreenFod = "
            r8.append(r10)
            com.android.keyguard.fod.MiuiGxzwManager r10 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            boolean r10 = r10.isDisableLockScreenFod()
            r8.append(r10)
            java.lang.String r10 = r8.toString()
            android.util.Slog.i(r5, r10)
        L_0x0102:
            com.android.keyguard.fod.MiuiGxzwManager r10 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            r0 = r1 ^ 1
            r10.dismissGxzwIconView(r0)
            goto L_0x014d
        L_0x010c:
            com.android.keyguard.fod.MiuiGxzwManager r0 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            boolean r0 = r0.isDisableFingerprintIcon()
            boolean r4 = r10.mPanelExpanded
            if (r4 != 0) goto L_0x011b
            if (r0 != 0) goto L_0x011b
            r1 = r3
        L_0x011b:
            com.android.keyguard.fod.MiuiGxzwManager r4 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            boolean r4 = r4.isShouldShowGxzwIcon()
            if (r4 == r1) goto L_0x0144
            java.lang.String r4 = TAG
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "updateGxzwState: mPanelExpanded = "
            r5.append(r6)
            boolean r10 = r10.mPanelExpanded
            r5.append(r10)
            r5.append(r2)
            r5.append(r0)
            java.lang.String r10 = r5.toString()
            android.util.Slog.i(r4, r10)
        L_0x0144:
            com.android.keyguard.fod.MiuiGxzwManager r10 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            r0 = r1 ^ 1
            r10.dismissGxzwIconView(r0)
        L_0x014d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.NotificationPanelView.updateGxzwState():void");
    }

    private void setBouncerShowingFraction(float f) {
        setAlpha(1.0f - f);
        this.mKeyguardBouncerFraction = f;
        updateStatusBarWindowBlur();
    }

    /* access modifiers changed from: private */
    public void onBouncerShowingChanged(boolean z) {
        this.mKeyguardBouncerShowing = z;
        ValueAnimator valueAnimator = this.mBouncerFractionAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        this.mBouncerFractionAnimator = ObjectAnimator.ofFloat(new float[]{this.mKeyguardBouncerFraction, 0.0f});
        if (z && KeyguardWallpaperUtils.isWallpaperShouldBlur(this.mContext)) {
            this.mBouncerFractionAnimator.setFloatValues(new float[]{this.mKeyguardBouncerFraction, 1.0f});
        }
        this.mBouncerFractionAnimator.setInterpolator(Interpolators.DECELERATE);
        this.mBouncerFractionAnimator.setDuration(300);
        this.mBouncerFractionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                NotificationPanelView.this.lambda$onBouncerShowingChanged$5$NotificationPanelView(valueAnimator);
            }
        });
        this.mBouncerFractionAnimator.start();
    }

    public /* synthetic */ void lambda$onBouncerShowingChanged$5$NotificationPanelView(ValueAnimator valueAnimator) {
        setBouncerShowingFraction(((Float) valueAnimator.getAnimatedValue()).floatValue());
    }

    public void inflateLeftView() {
        MiuiKeyguardMoveLeftViewContainer miuiKeyguardMoveLeftViewContainer = this.mKeyguardLeftView;
        if (miuiKeyguardMoveLeftViewContainer != null) {
            miuiKeyguardMoveLeftViewContainer.inflateLeftView();
        }
    }

    public void onBlurRatioChanged(float f) {
        this.mThemeBackgroundView.setAlpha(f);
    }

    private void initScreenSize() {
        Display display = ((DisplayManager) this.mContext.getSystemService("display")).getDisplay(0);
        Point point = new Point();
        display.getRealSize(point);
        this.mScreenWidth = point.x;
        this.mScreenHeight = point.y;
    }

    private boolean isDoubleTapBoundaryTouchEvent(MotionEvent motionEvent) {
        float rawX = motionEvent.getRawX();
        float rawY = motionEvent.getRawY();
        int i = this.mDoubleTapUninvalidStartEndAreaWidth;
        return rawX < ((float) i) || rawX > ((float) (this.mScreenWidth - i)) || rawY < ((float) this.mDoubleTapUninvalidTopAreaHeight) || rawY > ((float) (this.mScreenHeight - this.mDoubleTapUninvalidBottomAreaHeight));
    }

    public void onHorizontalMove(float f, boolean z) {
        if ((this.mKeyguardShowing || this.mKeyguardOccluded) && this.mHorizontalMoveDistance != f) {
            this.mHorizontalMoveDistance = f;
            float min = Math.min(Math.abs(this.mHorizontalMoveDistance) / 270.0f, 1.0f);
            if (this.mHorizontalMovePer != min) {
                this.mHorizontalMovePer = min;
                int i = (min > 0.0f ? 1 : (min == 0.0f ? 0 : -1));
                if (i == 0 || min == 1.0f) {
                    String str = TAG;
                    Slog.i(str, "onHorizontalMove per = " + min);
                }
                boolean z2 = i != 0;
                if (z2 != this.mKeyguardHorizontalMoving) {
                    this.mKeyguardHorizontalMoving = z2;
                    updateStatusBarWindowBlur();
                }
                if (z) {
                    setAlpha(Math.min(1.0f - min, 1.0f - this.mKeyguardBouncerFraction));
                }
                float f2 = 1.0f - (min * 0.1f);
                setScaleX(f2);
                setScaleY(f2);
            }
        }
    }

    public void startBottomButtonLayoutAnimate(boolean z, boolean z2) {
        this.mKeyguardBottomArea.startButtonLayoutAnimate(z, z2);
    }

    public void onSuperSaveModeChange(boolean z) {
        if (this.mSuperSaveModeOn != z) {
            this.mSuperSaveModeOn = z;
            this.mQsOverscrollExpansionEnabled = !z;
            requestLayout();
        }
    }

    public void onUseControlPanelChange(boolean z) {
        if (z) {
            this.mQsOverscrollExpansionEnabled = false;
        } else {
            this.mQsOverscrollExpansionEnabled = getResources().getBoolean(R.bool.config_enableQuickSettingsOverscrollExpansion);
        }
    }
}

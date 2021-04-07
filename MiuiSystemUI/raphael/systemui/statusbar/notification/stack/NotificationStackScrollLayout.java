package com.android.systemui.statusbar.notification.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.ServiceManager;
import android.provider.Settings;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.MathUtils;
import android.util.Pair;
import android.view.ContextThemeWrapper;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.OverScroller;
import android.widget.ScrollView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.colorextraction.ColorExtractor;
import com.android.internal.graphics.ColorUtils;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.settingslib.Utils;
import com.android.systemui.C0009R$attr;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.ExpandHelper;
import com.android.systemui.Interpolators;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.media.KeyguardMediaController;
import com.android.systemui.media.MediaDataFilter;
import com.android.systemui.media.MediaTimeoutListener;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.DragDownHelper;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.SysuiStatusBarStateController;
import com.android.systemui.statusbar.notification.ActivityLaunchAnimator;
import com.android.systemui.statusbar.notification.DynamicPrivacyController;
import com.android.systemui.statusbar.notification.ForegroundServiceDismissalFeatureController;
import com.android.systemui.statusbar.notification.MiuiNotificationSectionsManager;
import com.android.systemui.statusbar.notification.NotificationActivityStarter;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.NotificationPanelNavigationBarCoordinator;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.VisibilityLocationProvider;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.collection.NotifCollection;
import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.DismissedByUserStats;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.mediacontrol.MiuiKeyguardMediaController;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.row.FooterView;
import com.android.systemui.statusbar.notification.row.ForegroundServiceDungeonView;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationBlockingHelperManager;
import com.android.systemui.statusbar.notification.row.NotificationGuts;
import com.android.systemui.statusbar.notification.row.NotificationGutsManager;
import com.android.systemui.statusbar.notification.row.NotificationSnooze;
import com.android.systemui.statusbar.notification.row.StackScrollerDecorView;
import com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper;
import com.android.systemui.statusbar.notification.zen.ZenModeViewController;
import com.android.systemui.statusbar.phone.HeadsUpAppearanceController;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.phone.HeadsUpTouchHelper;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.LockscreenGestureLogger;
import com.android.systemui.statusbar.phone.MiuiNotificationPanelViewController;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.NotificationIconAreaController;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.ShadeController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.HeadsUpUtil;
import com.android.systemui.statusbar.policy.ScrollAdapter;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.util.Assert;
import com.miui.systemui.events.PanelSlidingDirection;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;

public class NotificationStackScrollLayout extends ViewGroup implements ScrollAdapter, NotificationListContainer, ConfigurationController.ConfigurationListener, Dumpable, DynamicPrivacyController.Listener {
    private boolean mActivateNeedsAnimation;
    private int mActivePointerId = -1;
    private ArrayList<View> mAddedHeadsUpChildren = new ArrayList<>();
    private final boolean mAllowLongPress;
    private final AmbientState mAmbientState;
    private boolean mAnimateBottomOnLayout;
    private boolean mAnimateNextBackgroundBottom;
    private boolean mAnimateNextBackgroundTop;
    private boolean mAnimateNextSectionBoundsChange;
    private ArrayList<AnimationEvent> mAnimationEvents = new ArrayList<>();
    private HashSet<Runnable> mAnimationFinishedRunnables = new HashSet<>();
    private boolean mAnimationRunning;
    private boolean mAnimationsEnabled;
    private final Rect mBackgroundAnimationRect;
    private final Paint mBackgroundPaint = new Paint();
    private ViewTreeObserver.OnPreDrawListener mBackgroundUpdater = new ViewTreeObserver.OnPreDrawListener() {
        /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$Q8bAVckgKDEBbXIsfAy3cWAYiM */

        public final boolean onPreDraw() {
            return NotificationStackScrollLayout.this.lambda$new$0$NotificationStackScrollLayout();
        }
    };
    private float mBackgroundXFactor;
    private boolean mBackwardScrollable;
    private final IStatusBarService mBarService;
    private int mBgColor;
    private int mBottomInset = 0;
    private int mBottomMargin;
    private int mCachedBackgroundColor;
    private boolean mChangePositionInProgress;
    boolean mCheckForLeavebehind;
    private boolean mChildTransferInProgress;
    private ArrayList<ExpandableView> mChildrenChangingPositions = new ArrayList<>();
    private HashSet<ExpandableView> mChildrenToAddAnimated = new HashSet<>();
    private ArrayList<ExpandableView> mChildrenToRemoveAnimated = new ArrayList<>();
    private boolean mChildrenUpdateRequested;
    private ViewTreeObserver.OnPreDrawListener mChildrenUpdater = new ViewTreeObserver.OnPreDrawListener() {
        /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass1 */

        public boolean onPreDraw() {
            NotificationStackScrollLayout.this.updateForcedScroll();
            NotificationStackScrollLayout.this.updateChildren();
            NotificationStackScrollLayout.this.mChildrenUpdateRequested = false;
            NotificationStackScrollLayout.this.getViewTreeObserver().removeOnPreDrawListener(this);
            return true;
        }
    };
    protected boolean mClearAllEnabled;
    private HashSet<ExpandableView> mClearTransientViewsWhenFinished = new HashSet<>();
    private final Rect mClipRect;
    private int mCollapsedSize;
    private final SysuiColorExtractor mColorExtractor;
    private int mContentHeight;
    private boolean mContinuousBackgroundUpdate;
    private boolean mContinuousShadowUpdate;
    private int mCornerRadius;
    private int mCurrentStackHeight = Integer.MAX_VALUE;
    private float mDimAmount;
    private ValueAnimator mDimAnimator;
    private final Animator.AnimatorListener mDimEndListener = new AnimatorListenerAdapter() {
        /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass4 */

        public void onAnimationEnd(Animator animator) {
            NotificationStackScrollLayout.this.mDimAnimator = null;
        }
    };
    private ValueAnimator.AnimatorUpdateListener mDimUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass5 */

        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            NotificationStackScrollLayout.this.setDimAmount(((Float) valueAnimator.getAnimatedValue()).floatValue());
        }
    };
    private boolean mDimmedNeedsAnimation;
    private boolean mDisallowDismissInThisMotion;
    private boolean mDisallowScrollingInThisMotion;
    private boolean mDismissAllInProgress;
    private boolean mDismissRtl;
    private final DisplayMetrics mDisplayMetrics;
    private boolean mDontClampNextScroll;
    private boolean mDontReportNextOverScroll;
    private int mDownX;
    private final DragDownHelper.DragDownCallback mDragDownCallback;
    private final DynamicPrivacyController mDynamicPrivacyController;
    protected EmptyShadeView mEmptyShadeView;
    private final NotificationEntryManager mEntryManager;
    private boolean mEverythingNeedsAnimation;
    private ExpandHelper mExpandHelper;
    private ExpandHelper.Callback mExpandHelperCallback;
    private ExpandableView mExpandedGroupView;
    private float mExpandedHeight;
    private ArrayList<BiConsumer<Float, Float>> mExpandedHeightListeners;
    private boolean mExpandedInThisMotion;
    private boolean mExpandingNotification;
    private boolean mFadeNotificationsOnDismiss;
    private FalsingManager mFalsingManager;
    private final FeatureFlags mFeatureFlags;
    private final ForegroundServiceSectionController mFgsSectionController;
    private ForegroundServiceDungeonView mFgsSectionView;
    private Runnable mFinishScrollingCallback;
    protected FooterView mFooterView;
    private boolean mForceNoOverlappingRendering;
    private View mForcedScroll;
    private boolean mForwardScrollable;
    private HashSet<View> mFromMoreCardAdditions = new HashSet<>();
    private int mGapHeight;
    private boolean mGenerateChildOrderChangedEvent;
    private long mGoToFullShadeDelay;
    private boolean mGoToFullShadeNeedsAnimation;
    private boolean mGroupExpanded;
    private boolean mGroupExpandedForMeasure;
    private NotificationGroupManager mGroupManager;
    private boolean mHeadsUpAnimatingAway;
    private HeadsUpAppearanceController mHeadsUpAppearanceController;
    private final HeadsUpTouchHelper.Callback mHeadsUpCallback;
    private HashSet<Pair<ExpandableNotificationRow, Boolean>> mHeadsUpChangeAnimations = new HashSet<>();
    private boolean mHeadsUpGoingAwayAnimationsAllowed;
    private int mHeadsUpInset;
    private HeadsUpManagerPhone mHeadsUpManager;
    private boolean mHideSensitiveNeedsAnimation;
    private Interpolator mHideXInterpolator;
    private boolean mHighPriorityBeforeSpeedBump;
    private NotificationIconAreaController mIconAreaController;
    private boolean mInHeadsUpPinnedMode;
    private int mIncreasedPaddingBetweenElements;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private float mInterpolatedHideAmount;
    private int mIntrinsicContentHeight;
    private int mIntrinsicPadding;
    private boolean mIsBeingDragged;
    private boolean mIsClipped;
    private boolean mIsExpanded = true;
    private boolean mIsExpansionChanging;
    private boolean mIsTrackingSliding;
    private final KeyguardBypassController mKeyguardBypassController;
    private final KeyguardMediaController mKeyguardMediaController;
    private int mLastMotionY;
    private float mLastSentAppear;
    private float mLastSentExpandedHeight;
    private float mLinearHideAmount;
    private NotificationLogger.OnChildLocationsChangedListener mListener;
    private final LockscreenGestureLogger mLockscreenGestureLogger;
    private final NotificationLockscreenUserManager.UserChangedListener mLockscreenUserChangeListener = new NotificationLockscreenUserManager.UserChangedListener() {
        /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass2 */

        @Override // com.android.systemui.statusbar.NotificationLockscreenUserManager.UserChangedListener
        public void onUserChanged(int i) {
            NotificationStackScrollLayout.this.updateSensitiveness(false);
        }
    };
    private final NotificationLockscreenUserManager mLockscreenUserManager;
    private ExpandableNotificationRow.LongPressListener mLongPressListener;
    private int mMaxDisplayedNotifications;
    private int mMaxLayoutHeight;
    private float mMaxOverScroll;
    private int mMaxScrollAfterExpand;
    private int mMaxTopPadding;
    private int mMaximumVelocity;
    @VisibleForTesting
    protected final NotificationMenuRowPlugin.OnMenuEventListener mMenuEventListener;
    @VisibleForTesting
    protected final MetricsLogger mMetricsLogger;
    private int mMinInteractionHeight;
    private float mMinTopOverScrollToEscape;
    private int mMinimumVelocity;
    private boolean mNeedViewResizeAnimation;
    private boolean mNeedsAnimation;
    private final NotifCollection mNotifCollection;
    private final NotifPipeline mNotifPipeline;
    private NotificationActivityStarter mNotificationActivityStarter;
    private final NotificationSwipeHelper.NotificationCallback mNotificationCallback;
    private final NotificationGutsManager mNotificationGutsManager;
    private NotificationPanelViewController mNotificationPanelController;
    private OnChildLocationsChangedListener mOnChildLocationsChangedListener;
    private ColorExtractor.OnColorsChangedListener mOnColorsChangedListener;
    private OnEmptySpaceClickListener mOnEmptySpaceClickListener;
    private final NotificationGroupManager.OnGroupChangeListener mOnGroupChangeListener;
    private ExpandableView.OnHeightChangedListener mOnHeightChangedListener;
    private boolean mOnlyScrollingInThisMotion;
    private final ViewOutlineProvider mOutlineProvider = new ViewOutlineProvider() {
        /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass8 */

        public void getOutline(View view, Outline outline) {
            if (NotificationStackScrollLayout.this.mAmbientState.isHiddenAtAll()) {
                outline.setRoundRect(NotificationStackScrollLayout.this.mBackgroundAnimationRect, MathUtils.lerp(((float) NotificationStackScrollLayout.this.mCornerRadius) / 2.0f, (float) NotificationStackScrollLayout.this.mCornerRadius, NotificationStackScrollLayout.this.mHideXInterpolator.getInterpolation((1.0f - NotificationStackScrollLayout.this.mLinearHideAmount) * NotificationStackScrollLayout.this.mBackgroundXFactor)));
                outline.setAlpha(1.0f - NotificationStackScrollLayout.this.mAmbientState.getHideAmount());
                return;
            }
            ViewOutlineProvider.BACKGROUND.getOutline(view, outline);
        }
    };
    private float mOverScrolledBottomPixels;
    private float mOverScrolledTopPixels;
    private int mOverflingDistance;
    private OnOverscrollTopChangedListener mOverscrollTopChangedListener;
    private int mOwnScrollY;
    private int mPaddingBetweenElements;
    private boolean mPanelTracking;
    private boolean mPulsing;
    protected ViewGroup mQsContainer;
    private boolean mQsExpanded;
    private float mQsExpansionFraction;
    private Runnable mReclamp;
    private Runnable mReflingAndAnimateScroll;
    private final NotificationRemoteInputManager mRemoteInputManager;
    private Rect mRequestedClipBounds;
    private final NotificationRoundnessManager mRoundnessManager;
    private ViewTreeObserver.OnPreDrawListener mRunningAnimationUpdater = new ViewTreeObserver.OnPreDrawListener() {
        /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass3 */

        public boolean onPreDraw() {
            NotificationStackScrollLayout.this.onPreDrawDuringAnimation();
            return true;
        }
    };
    private ScrimController mScrimController;
    private boolean mScrollable;
    private boolean mScrolledToTopOnFirstDown;
    private OverScroller mScroller;
    protected boolean mScrollingEnabled;
    private NotificationSection[] mSections;
    private final MiuiNotificationSectionsManager mSectionsManager;
    private ViewTreeObserver.OnPreDrawListener mShadowUpdater = new ViewTreeObserver.OnPreDrawListener() {
        /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass6 */

        public boolean onPreDraw() {
            NotificationStackScrollLayout.this.updateViewShadows();
            return true;
        }
    };
    private NotificationShelf mShelf;
    private final boolean mShouldDrawNotificationBackground;
    private boolean mShouldShowShelfOnly;
    private int mSidePaddings;
    private float mSlopMultiplier;
    protected final StackScrollAlgorithm mStackScrollAlgorithm;
    private float mStackTranslation;
    private final StackStateAnimator mStateAnimator = new MiuiStackStateAnimator(this);
    private final StatusBarStateController.StateListener mStateListener;
    private StatusBar mStatusBar;
    private int mStatusBarHeight;
    private int mStatusBarState;
    private final SysuiStatusBarStateController mStatusbarStateController;
    private final NotificationSwipeHelper mSwipeHelper;
    private ArrayList<View> mSwipedOutViews = new ArrayList<>();
    private boolean mSwipingInProgress;
    private int[] mTempInt2 = new int[2];
    private final ArrayList<Pair<ExpandableNotificationRow, Boolean>> mTmpList = new ArrayList<>();
    private final Rect mTmpRect;
    private ArrayList<ExpandableView> mTmpSortedChildren = new ArrayList<>();
    private int mTopPadding;
    private boolean mTopPaddingNeedsAnimation;
    private float mTopPaddingOverflow;
    private boolean mTouchIsClick;
    private int mTouchSlop;
    protected final UiEventLogger mUiEventLogger;
    private boolean mUsingLightTheme;
    private VelocityTracker mVelocityTracker;
    private Comparator<ExpandableView> mViewPositionComparator = new Comparator<ExpandableView>(this) {
        /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass7 */

        public int compare(ExpandableView expandableView, ExpandableView expandableView2) {
            float translationY = expandableView.getTranslationY() + ((float) expandableView.getActualHeight());
            float translationY2 = expandableView2.getTranslationY() + ((float) expandableView2.getActualHeight());
            if (translationY < translationY2) {
                return -1;
            }
            return translationY > translationY2 ? 1 : 0;
        }
    };
    private final VisualStabilityManager mVisualStabilityManager;
    private int mWaterfallTopInset;
    private boolean mWillExpand;
    private final ZenModeController mZenController;

    public interface OnEmptySpaceClickListener {
        void onEmptySpaceClicked(float f, float f2);
    }

    public interface OnOverscrollTopChangedListener {
        void flingTopOverscroll(float f, boolean z);

        void onOverscrollTopChanged(float f, boolean z);
    }

    @Override // com.android.systemui.statusbar.policy.ScrollAdapter
    public View getHostView() {
        return this;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public ViewGroup getViewParentForNotification(NotificationEntry notificationEntry) {
        return this;
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ boolean lambda$new$0$NotificationStackScrollLayout() {
        updateBackground();
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$2 */
    public /* synthetic */ void lambda$new$2$NotificationStackScrollLayout(ColorExtractor colorExtractor, int i) {
        updateDecorViews(this.mColorExtractor.getNeutralColors().supportsDarkText());
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public NotificationStackScrollLayout(Context context, AttributeSet attributeSet, boolean z, NotificationRoundnessManager notificationRoundnessManager, DynamicPrivacyController dynamicPrivacyController, SysuiStatusBarStateController sysuiStatusBarStateController, HeadsUpManagerPhone headsUpManagerPhone, KeyguardBypassController keyguardBypassController, MiuiKeyguardMediaController miuiKeyguardMediaController, ZenModeViewController zenModeViewController, FalsingManager falsingManager, NotificationLockscreenUserManager notificationLockscreenUserManager, NotificationGutsManager notificationGutsManager, ZenModeController zenModeController, MiuiNotificationSectionsManager miuiNotificationSectionsManager, ForegroundServiceSectionController foregroundServiceSectionController, ForegroundServiceDismissalFeatureController foregroundServiceDismissalFeatureController, FeatureFlags featureFlags, NotifPipeline notifPipeline, NotificationEntryManager notificationEntryManager, NotifCollection notifCollection, UiEventLogger uiEventLogger, MediaTimeoutListener mediaTimeoutListener, MediaDataFilter mediaDataFilter) {
        super(context, attributeSet, 0, 0);
        boolean z2 = false;
        new PorterDuffXfermode(PorterDuff.Mode.SRC);
        this.mInterpolatedHideAmount = 0.0f;
        this.mLinearHideAmount = 0.0f;
        this.mBackgroundXFactor = 1.0f;
        this.mMaxDisplayedNotifications = -1;
        this.mClipRect = new Rect();
        this.mHeadsUpGoingAwayAnimationsAllowed = true;
        this.mReflingAndAnimateScroll = new Runnable() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$Dpz6Zg1EwqGyFLQ68KdTUD2Xag */

            public final void run() {
                NotificationStackScrollLayout.this.lambda$new$1$NotificationStackScrollLayout();
            }
        };
        this.mBackgroundAnimationRect = new Rect();
        this.mExpandedHeightListeners = new ArrayList<>();
        this.mTmpRect = new Rect();
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
        this.mRemoteInputManager = (NotificationRemoteInputManager) Dependency.get(NotificationRemoteInputManager.class);
        this.mColorExtractor = (SysuiColorExtractor) Dependency.get(SysuiColorExtractor.class);
        this.mDisplayMetrics = (DisplayMetrics) Dependency.get(DisplayMetrics.class);
        this.mLockscreenGestureLogger = (LockscreenGestureLogger) Dependency.get(LockscreenGestureLogger.class);
        this.mVisualStabilityManager = (VisualStabilityManager) Dependency.get(VisualStabilityManager.class);
        this.mHideXInterpolator = Interpolators.FAST_OUT_SLOW_IN;
        this.mOnColorsChangedListener = new ColorExtractor.OnColorsChangedListener() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$76prqFuo9J9YXd5uaycSxZOCMM */

            public final void onColorsChanged(ColorExtractor colorExtractor, int i) {
                NotificationStackScrollLayout.this.lambda$new$2$NotificationStackScrollLayout(colorExtractor, i);
            }
        };
        this.mIsTrackingSliding = false;
        this.mReclamp = new Runnable() {
            /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass12 */

            public void run() {
                NotificationStackScrollLayout.this.mScroller.startScroll(((ViewGroup) NotificationStackScrollLayout.this).mScrollX, NotificationStackScrollLayout.this.mOwnScrollY, 0, NotificationStackScrollLayout.this.getScrollRange() - NotificationStackScrollLayout.this.mOwnScrollY);
                NotificationStackScrollLayout.this.mDontReportNextOverScroll = true;
                NotificationStackScrollLayout.this.mDontClampNextScroll = true;
                NotificationStackScrollLayout.this.lambda$new$1();
            }
        };
        this.mStateListener = new StatusBarStateController.StateListener() {
            /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass13 */

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStatePreChange(int i, int i2) {
                if (i == 2 && i2 == 1) {
                    NotificationStackScrollLayout.this.requestAnimateEverything();
                }
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStateChanged(int i) {
                NotificationStackScrollLayout.this.setStatusBarState(i);
            }

            @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
            public void onStatePostChange() {
                NotificationStackScrollLayout.this.onStatePostChange();
            }
        };
        this.mMenuEventListener = new NotificationMenuRowPlugin.OnMenuEventListener() {
            /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass14 */

            @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin.OnMenuEventListener
            public void onMenuClicked(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
                if (NotificationStackScrollLayout.this.mLongPressListener != null) {
                    if (view instanceof ExpandableNotificationRow) {
                        NotificationStackScrollLayout.this.mMetricsLogger.write(((ExpandableNotificationRow) view).getEntry().getSbn().getLogMaker().setCategory(333).setType(4));
                    }
                    NotificationStackScrollLayout.this.mLongPressListener.onLongPress(view, i, i2, menuItem);
                }
            }

            @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin.OnMenuEventListener
            public void onMenuReset(View view) {
                View translatingParentView = NotificationStackScrollLayout.this.mSwipeHelper.getTranslatingParentView();
                if (translatingParentView != null && view == translatingParentView) {
                    NotificationStackScrollLayout.this.mSwipeHelper.clearExposedMenuView();
                    NotificationStackScrollLayout.this.mSwipeHelper.clearTranslatingParentView();
                    if (view instanceof ExpandableNotificationRow) {
                        NotificationStackScrollLayout.this.mHeadsUpManager.setMenuShown(((ExpandableNotificationRow) view).getEntry(), false);
                    }
                }
            }

            @Override // com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin.OnMenuEventListener
            public void onMenuShown(View view) {
                if (view instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                    NotificationStackScrollLayout.this.mMetricsLogger.write(expandableNotificationRow.getEntry().getSbn().getLogMaker().setCategory(332).setType(4));
                    NotificationStackScrollLayout.this.mHeadsUpManager.setMenuShown(expandableNotificationRow.getEntry(), true);
                    NotificationStackScrollLayout.this.mSwipeHelper.onMenuShown(view);
                    NotificationStackScrollLayout.this.mNotificationGutsManager.closeAndSaveGuts(true, false, false, -1, -1, false);
                    NotificationMenuRowPlugin provider = expandableNotificationRow.getProvider();
                    if (provider.shouldShowGutsOnSnapOpen()) {
                        NotificationMenuRowPlugin.MenuItem menuItemToExposeOnSnap = provider.menuItemToExposeOnSnap();
                        if (menuItemToExposeOnSnap != null) {
                            Point revealAnimationOrigin = provider.getRevealAnimationOrigin();
                            NotificationStackScrollLayout.this.mNotificationGutsManager.openGuts(view, revealAnimationOrigin.x, revealAnimationOrigin.y, menuItemToExposeOnSnap);
                        } else {
                            Log.e("StackScroller", "Provider has shouldShowGutsOnSnapOpen, but provided no menu item in menuItemtoExposeOnSnap. Skipping.");
                        }
                        NotificationStackScrollLayout.this.resetExposedMenuView(false, true);
                    }
                }
            }
        };
        this.mNotificationCallback = new NotificationSwipeHelper.NotificationCallback() {
            /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass15 */

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
            public void onDismiss() {
                NotificationStackScrollLayout.this.mNotificationGutsManager.closeAndSaveGuts(true, false, false, -1, -1, false);
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
            public void onSnooze(StatusBarNotification statusBarNotification, NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
                NotificationStackScrollLayout.this.mStatusBar.setNotificationSnoozed(statusBarNotification, snoozeOption);
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
            public void onSnooze(StatusBarNotification statusBarNotification, int i) {
                NotificationStackScrollLayout.this.mStatusBar.setNotificationSnoozed(statusBarNotification, i);
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
            public boolean shouldDismissQuickly() {
                return NotificationStackScrollLayout.this.isExpanded() && NotificationStackScrollLayout.this.mAmbientState.isFullyAwake();
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public void onDragCancelled(View view) {
                NotificationStackScrollLayout.this.setSwipingInProgress(false);
                NotificationStackScrollLayout.this.mFalsingManager.onNotificationStopDismissing();
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public void onChildDismissed(View view) {
                if (view instanceof ActivatableNotificationView) {
                    ActivatableNotificationView activatableNotificationView = (ActivatableNotificationView) view;
                    if (!activatableNotificationView.isDismissed()) {
                        handleChildViewDismissed(view);
                    }
                    ViewGroup transientContainer = activatableNotificationView.getTransientContainer();
                    if (transientContainer != null) {
                        transientContainer.removeTransientView(view);
                    }
                }
            }

            @Override // com.android.systemui.statusbar.notification.stack.NotificationSwipeHelper.NotificationCallback
            public void handleChildViewDismissed(View view) {
                boolean z = false;
                NotificationStackScrollLayout.this.setSwipingInProgress(false);
                if (!NotificationStackScrollLayout.this.mDismissAllInProgress) {
                    NotificationStackScrollLayout.this.mAmbientState.onDragFinished(view);
                    NotificationStackScrollLayout.this.updateContinuousShadowDrawing();
                    if (view instanceof ExpandableNotificationRow) {
                        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                        if (expandableNotificationRow.isHeadsUp()) {
                            NotificationStackScrollLayout.this.mHeadsUpManager.addSwipedOutNotification(expandableNotificationRow.getEntry().getSbn().getKey());
                        }
                        z = expandableNotificationRow.performDismissWithBlockingHelper(false);
                    }
                    if (view instanceof PeopleHubView) {
                        NotificationStackScrollLayout.this.mSectionsManager.hidePeopleRow();
                    }
                    if (!z) {
                        NotificationStackScrollLayout.this.mSwipedOutViews.add(view);
                    }
                    NotificationStackScrollLayout.this.mFalsingManager.onNotificationDismissed();
                    if (NotificationStackScrollLayout.this.mFalsingManager.shouldEnforceBouncer()) {
                        NotificationStackScrollLayout.this.mStatusBar.executeRunnableDismissingKeyguard(null, null, false, true, false);
                    }
                }
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public boolean isAntiFalsingNeeded() {
                return NotificationStackScrollLayout.this.onKeyguard();
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public View getChildAtPosition(MotionEvent motionEvent) {
                ExpandableNotificationRow notificationParent;
                ExpandableView childAtPosition = NotificationStackScrollLayout.this.getChildAtPosition(motionEvent.getX(), motionEvent.getY(), true, false);
                if (!(childAtPosition instanceof ExpandableNotificationRow) || (notificationParent = ((ExpandableNotificationRow) childAtPosition).getNotificationParent()) == null || !notificationParent.areChildrenExpanded()) {
                    return childAtPosition;
                }
                return (notificationParent.areGutsExposed() || NotificationStackScrollLayout.this.mSwipeHelper.getExposedMenuView() == notificationParent || (notificationParent.getAttachedChildren().size() == 1 && notificationParent.getEntry().isClearable())) ? notificationParent : childAtPosition;
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public void onBeginDrag(View view) {
                NotificationStackScrollLayout.this.mFalsingManager.onNotificationStartDismissing();
                NotificationStackScrollLayout.this.setSwipingInProgress(true);
                NotificationStackScrollLayout.this.mAmbientState.onBeginDrag((ExpandableView) view);
                NotificationStackScrollLayout.this.updateContinuousShadowDrawing();
                NotificationStackScrollLayout.this.updateContinuousBackgroundDrawing();
                NotificationStackScrollLayout.this.requestChildrenUpdate();
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public void onChildSnappedBack(View view, float f) {
                NotificationStackScrollLayout.this.mAmbientState.onDragFinished(view);
                NotificationStackScrollLayout.this.updateContinuousShadowDrawing();
                NotificationStackScrollLayout.this.updateContinuousBackgroundDrawing();
                if (view instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                    if (expandableNotificationRow.isPinned() && !canChildBeDismissed(expandableNotificationRow) && expandableNotificationRow.getEntry().getSbn().getNotification().fullScreenIntent == null) {
                        NotificationStackScrollLayout.this.mHeadsUpManager.removeNotification(expandableNotificationRow.getEntry().getSbn().getKey(), true);
                    }
                }
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public boolean updateSwipeProgress(View view, boolean z, float f) {
                return !NotificationStackScrollLayout.this.mFadeNotificationsOnDismiss;
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public float getFalsingThresholdFactor() {
                return NotificationStackScrollLayout.this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f;
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public int getConstrainSwipeStartPosition() {
                NotificationMenuRowPlugin currentMenuRow = NotificationStackScrollLayout.this.mSwipeHelper.getCurrentMenuRow();
                if (currentMenuRow != null) {
                    return Math.abs(currentMenuRow.getMenuSnapTarget());
                }
                return 0;
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public boolean canChildBeDismissed(View view) {
                return NotificationStackScrollLayout.canChildBeDismissed(view);
            }

            @Override // com.android.systemui.SwipeHelper.Callback
            public boolean canChildBeDismissedInDirection(View view, boolean z) {
                return canChildBeDismissed(view);
            }
        };
        this.mDragDownCallback = new DragDownHelper.DragDownCallback() {
            /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass16 */

            static /* synthetic */ boolean lambda$onDraggedDown$0() {
                return false;
            }

            @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
            public boolean onDraggedDown(View view, int i) {
                boolean z = NotificationStackScrollLayout.this.hasActiveNotifications() || NotificationStackScrollLayout.this.mKeyguardMediaController.getView().getVisibility() == 0;
                if (NotificationStackScrollLayout.this.mStatusBarState == 1 && z) {
                    NotificationStackScrollLayout.this.mLockscreenGestureLogger.write(187, (int) (((float) i) / NotificationStackScrollLayout.this.mDisplayMetrics.density), 0);
                    NotificationStackScrollLayout.this.mLockscreenGestureLogger.log(LockscreenGestureLogger.LockscreenUiEvent.LOCKSCREEN_PULL_SHADE_OPEN);
                    if (!NotificationStackScrollLayout.this.mAmbientState.isDozing() || view != null) {
                        NotificationStackScrollLayout.this.mNotificationPanelController.goToLockedShade(view);
                        if (view instanceof ExpandableNotificationRow) {
                            ((ExpandableNotificationRow) view).onExpandedByGesture(true);
                        }
                    }
                    return true;
                } else if (!NotificationStackScrollLayout.this.mDynamicPrivacyController.isInLockedDownShade()) {
                    return false;
                } else {
                    NotificationStackScrollLayout.this.mStatusbarStateController.setLeaveOpenOnKeyguardHide(true);
                    NotificationStackScrollLayout.this.mStatusBar.dismissKeyguardThenExecute($$Lambda$NotificationStackScrollLayout$16$4jJHIvYLz4tPygD0Enr8OUidlx4.INSTANCE, null, false);
                    return true;
                }
            }

            @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
            public void onDragDownReset() {
                NotificationStackScrollLayout.this.setDimmed(true, true);
                NotificationStackScrollLayout.this.resetScrollPosition();
                NotificationStackScrollLayout.this.resetCheckSnoozeLeavebehind();
            }

            @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
            public void onCrossedThreshold(boolean z) {
                NotificationStackScrollLayout.this.setDimmed(!z, true);
            }

            @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
            public void onTouchSlopExceeded() {
                NotificationStackScrollLayout.this.cancelLongPress();
                NotificationStackScrollLayout.this.checkSnoozeLeavebehind();
            }

            @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
            public void setEmptyDragAmount(float f) {
                NotificationStackScrollLayout.this.mNotificationPanelController.setEmptyDragAmount(f);
            }

            @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
            public boolean isFalsingCheckNeeded() {
                return NotificationStackScrollLayout.this.mStatusBarState == 1;
            }

            @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
            public boolean isDragDownEnabledForView(ExpandableView expandableView) {
                if (isDragDownAnywhereEnabled()) {
                    return true;
                }
                if (!NotificationStackScrollLayout.this.mDynamicPrivacyController.isInLockedDownShade()) {
                    return false;
                }
                if (expandableView == null) {
                    return true;
                }
                if (expandableView instanceof ExpandableNotificationRow) {
                    return ((ExpandableNotificationRow) expandableView).getEntry().isSensitive();
                }
                return false;
            }

            @Override // com.android.systemui.statusbar.DragDownHelper.DragDownCallback
            public boolean isDragDownAnywhereEnabled() {
                if (NotificationStackScrollLayout.this.mStatusbarStateController.getState() != 1 || NotificationStackScrollLayout.this.mKeyguardBypassController.getBypassEnabled() || !MiuiKeyguardUtils.isDefaultLockScreenTheme()) {
                    return false;
                }
                return true;
            }
        };
        this.mHeadsUpCallback = new HeadsUpTouchHelper.Callback() {
            /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass17 */

            @Override // com.android.systemui.statusbar.phone.HeadsUpTouchHelper.Callback
            public ExpandableView getChildAtRawPosition(float f, float f2) {
                return NotificationStackScrollLayout.this.getChildAtRawPosition(f, f2);
            }

            @Override // com.android.systemui.statusbar.phone.HeadsUpTouchHelper.Callback
            public boolean isExpanded() {
                return NotificationStackScrollLayout.this.mIsExpanded;
            }

            @Override // com.android.systemui.statusbar.phone.HeadsUpTouchHelper.Callback
            public Context getContext() {
                return ((ViewGroup) NotificationStackScrollLayout.this).mContext;
            }
        };
        this.mOnGroupChangeListener = new NotificationGroupManager.OnGroupChangeListener() {
            /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass18 */

            @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
            public void onGroupExpansionChanged(final ExpandableNotificationRow expandableNotificationRow, boolean z) {
                boolean z2 = !NotificationStackScrollLayout.this.mGroupExpandedForMeasure && NotificationStackScrollLayout.this.mAnimationsEnabled && (NotificationStackScrollLayout.this.mIsExpanded || expandableNotificationRow.isPinned());
                if (z2) {
                    NotificationStackScrollLayout.this.mExpandedGroupView = expandableNotificationRow;
                    NotificationStackScrollLayout.this.mGroupExpanded = z;
                    NotificationStackScrollLayout.this.mNeedsAnimation = true;
                }
                expandableNotificationRow.setChildrenExpanded(z, z2);
                if (!NotificationStackScrollLayout.this.mGroupExpandedForMeasure) {
                    NotificationStackScrollLayout.this.onHeightChanged(expandableNotificationRow, false);
                }
                NotificationStackScrollLayout.this.runAfterAnimationFinished(new Runnable(this) {
                    /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass18.AnonymousClass1 */

                    public void run() {
                        expandableNotificationRow.onFinishedExpansionChange();
                    }
                });
            }

            @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
            public void onGroupCreatedFromChildren(NotificationGroupManager.NotificationGroup notificationGroup) {
                NotificationStackScrollLayout.this.mStatusBar.requestNotificationUpdate("onGroupCreatedFromChildren");
            }

            @Override // com.android.systemui.statusbar.phone.NotificationGroupManager.OnGroupChangeListener
            public void onGroupsChanged() {
                NotificationStackScrollLayout.this.mStatusBar.requestNotificationUpdate("onGroupsChanged");
            }
        };
        this.mExpandHelperCallback = new ExpandHelper.Callback() {
            /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass19 */

            @Override // com.android.systemui.ExpandHelper.Callback
            public ExpandableView getChildAtPosition(float f, float f2) {
                return NotificationStackScrollLayout.this.getChildAtPosition(f, f2);
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public ExpandableView getChildAtRawPosition(float f, float f2) {
                return NotificationStackScrollLayout.this.getChildAtRawPosition(f, f2);
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public boolean canChildBeExpanded(View view) {
                if (view instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                    return expandableNotificationRow.isExpandable() && !expandableNotificationRow.areGutsExposed() && (NotificationStackScrollLayout.this.mIsExpanded || !expandableNotificationRow.isPinned());
                }
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public void setUserExpandedChild(View view, boolean z) {
                if (view instanceof ExpandableNotificationRow) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                    if (!z || !NotificationStackScrollLayout.this.onKeyguard()) {
                        expandableNotificationRow.setUserExpanded(z, true);
                        expandableNotificationRow.onExpandedByGesture(z);
                        return;
                    }
                    expandableNotificationRow.setUserLocked(false);
                    NotificationStackScrollLayout.this.updateContentHeight();
                    NotificationStackScrollLayout.this.notifyHeightChangeListener(expandableNotificationRow);
                }
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public void setExpansionCancelled(View view) {
                if (view instanceof ExpandableNotificationRow) {
                    ((ExpandableNotificationRow) view).setGroupExpansionChanging(false);
                }
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public void setUserLockedChild(View view, boolean z) {
                if (view instanceof ExpandableNotificationRow) {
                    ((ExpandableNotificationRow) view).setUserLocked(z);
                }
                NotificationStackScrollLayout.this.cancelLongPress();
                NotificationStackScrollLayout.this.requestDisallowInterceptTouchEvent(true);
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public void expansionStateChanged(boolean z) {
                NotificationStackScrollLayout.this.mExpandingNotification = z;
                if (!NotificationStackScrollLayout.this.mExpandedInThisMotion) {
                    NotificationStackScrollLayout notificationStackScrollLayout = NotificationStackScrollLayout.this;
                    notificationStackScrollLayout.mMaxScrollAfterExpand = notificationStackScrollLayout.mOwnScrollY;
                    NotificationStackScrollLayout.this.mExpandedInThisMotion = true;
                }
            }

            @Override // com.android.systemui.ExpandHelper.Callback
            public int getMaxExpandHeight(ExpandableView expandableView) {
                return expandableView.getMaxContentHeight();
            }
        };
        Resources resources = getResources();
        this.mAllowLongPress = z;
        this.mRoundnessManager = notificationRoundnessManager;
        this.mLockscreenUserManager = notificationLockscreenUserManager;
        this.mNotificationGutsManager = notificationGutsManager;
        this.mHeadsUpManager = headsUpManagerPhone;
        headsUpManagerPhone.addListener(notificationRoundnessManager);
        this.mHeadsUpManager.setAnimationStateHandler(new HeadsUpManagerPhone.AnimationStateHandler() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$2kmwH5TzrEUhlI4yYwStAmSu1DU */

            @Override // com.android.systemui.statusbar.phone.HeadsUpManagerPhone.AnimationStateHandler
            public final void setHeadsUpGoingAwayAnimationsAllowed(boolean z) {
                NotificationStackScrollLayout.this.setHeadsUpGoingAwayAnimationsAllowed(z);
            }
        });
        this.mKeyguardBypassController = keyguardBypassController;
        this.mFalsingManager = falsingManager;
        this.mZenController = zenModeController;
        this.mFgsSectionController = foregroundServiceSectionController;
        this.mSectionsManager = miuiNotificationSectionsManager;
        miuiNotificationSectionsManager.initialize(this, LayoutInflater.from(context));
        this.mSectionsManager.setOnClearSilentNotifsClickListener(new View.OnClickListener() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$XmSQLyF8x0Au5zc1KRRUiRzAE9w */

            public final void onClick(View view) {
                NotificationStackScrollLayout.this.lambda$new$3$NotificationStackScrollLayout(view);
            }
        });
        this.mSections = this.mSectionsManager.createSectionsForBuckets();
        this.mAmbientState = new AmbientState(context, this.mSectionsManager, this.mHeadsUpManager);
        this.mBgColor = context.getColor(C0011R$color.notification_shade_background_color);
        ExpandHelper expandHelper = new ExpandHelper(getContext(), this.mExpandHelperCallback, resources.getDimensionPixelSize(C0012R$dimen.notification_min_height), resources.getDimensionPixelSize(C0012R$dimen.notification_max_height));
        this.mExpandHelper = expandHelper;
        expandHelper.setEventSource(this);
        this.mExpandHelper.setScrollAdapter(this);
        this.mSwipeHelper = new NotificationSwipeHelper(0, new MiuiNotificationSwipeCallback(this.mNotificationCallback, mediaTimeoutListener, mediaDataFilter, zenModeViewController), getContext(), this.mMenuEventListener, this.mFalsingManager);
        this.mStackScrollAlgorithm = createStackScrollAlgorithm(context);
        initView(context);
        this.mShouldDrawNotificationBackground = resources.getBoolean(C0010R$bool.config_drawNotificationBackground);
        this.mFadeNotificationsOnDismiss = resources.getBoolean(C0010R$bool.config_fadeNotificationsOnDismiss);
        this.mRoundnessManager.setAnimatedChildren(this.mChildrenToAddAnimated);
        this.mRoundnessManager.setOnRoundingChangedCallback(new Runnable() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$ZNzbjhiYOpIhFG8SoCZYGISAg68 */

            public final void run() {
                NotificationStackScrollLayout.this.invalidate();
            }
        });
        NotificationRoundnessManager notificationRoundnessManager2 = this.mRoundnessManager;
        Objects.requireNonNull(notificationRoundnessManager2);
        addOnExpandedHeightChangedListener(new BiConsumer() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$7_f8XxLoO1HD4OWprUeIqEzesjU */

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                NotificationRoundnessManager.this.setExpanded(((Float) obj).floatValue(), ((Float) obj2).floatValue());
            }
        });
        this.mLockscreenUserManager.addUserChangedListener(this.mLockscreenUserChangeListener);
        setOutlineProvider(this.mOutlineProvider);
        addOnExpandedHeightChangedListener(new BiConsumer() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$EOvrpynV_4_HqkQZPqElzpbHsN4 */

            @Override // java.util.function.BiConsumer
            public final void accept(Object obj, Object obj2) {
                Float f = (Float) obj2;
                NotificationBlockingHelperManager.this.setNotificationShadeExpanded(((Float) obj).floatValue());
            }
        });
        setWillNotDraw(!(this.mShouldDrawNotificationBackground ? true : z2));
        this.mBackgroundPaint.setAntiAlias(true);
        this.mClearAllEnabled = resources.getBoolean(C0010R$bool.config_enableNotificationsClearAll);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(new TunerService.Tunable() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$Jw0uVZk_QqBt9QukDWfY9zQ7BQU */

            @Override // com.android.systemui.tuner.TunerService.Tunable
            public final void onTuningChanged(String str, String str2) {
                NotificationStackScrollLayout.this.lambda$new$5$NotificationStackScrollLayout(str, str2);
            }
        }, "high_priority", "notification_dismiss_rtl", "notification_history_enabled");
        this.mFeatureFlags = featureFlags;
        this.mNotifPipeline = notifPipeline;
        this.mEntryManager = notificationEntryManager;
        this.mNotifCollection = notifCollection;
        if (featureFlags.isNewNotifPipelineRenderingEnabled()) {
            this.mNotifPipeline.addCollectionListener(new NotifCollectionListener() {
                /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass9 */

                @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
                public void onEntryUpdated(NotificationEntry notificationEntry) {
                    NotificationStackScrollLayout.this.onEntryUpdated(notificationEntry);
                }
            });
        } else {
            this.mEntryManager.addNotificationEntryListener(new NotificationEntryListener() {
                /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass10 */

                @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
                public void onPreEntryUpdated(NotificationEntry notificationEntry) {
                    NotificationStackScrollLayout.this.onEntryUpdated(notificationEntry);
                }
            });
        }
        dynamicPrivacyController.addListener(this);
        this.mDynamicPrivacyController = dynamicPrivacyController;
        this.mStatusbarStateController = sysuiStatusBarStateController;
        initializeForegroundServiceSection(foregroundServiceDismissalFeatureController);
        this.mUiEventLogger = uiEventLogger;
        this.mColorExtractor.addOnColorsChangedListener(this.mOnColorsChangedListener);
        this.mKeyguardMediaController = miuiKeyguardMediaController;
        miuiKeyguardMediaController.setVisibilityChangedListener(new Function1(miuiKeyguardMediaController) {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$YKigXFrCTlL9_LHDl6k5qMtmuY */
            public final /* synthetic */ MiuiKeyguardMediaController f$1;

            {
                this.f$1 = r2;
            }

            @Override // kotlin.jvm.functions.Function1
            public final Object invoke(Object obj) {
                return NotificationStackScrollLayout.this.lambda$new$6$NotificationStackScrollLayout(this.f$1, (Boolean) obj);
            }
        });
        zenModeViewController.setVisibilityChangedListener(new Function2(zenModeViewController) {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$S8KZ998NAdgqcywoW1IIdxmOe0 */
            public final /* synthetic */ ZenModeViewController f$1;

            {
                this.f$1 = r2;
            }

            @Override // kotlin.jvm.functions.Function2
            public final Object invoke(Object obj, Object obj2) {
                return NotificationStackScrollLayout.this.lambda$new$7$NotificationStackScrollLayout(this.f$1, (Boolean) obj, (Boolean) obj2);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$3 */
    public /* synthetic */ void lambda$new$3$NotificationStackScrollLayout(View view) {
        clearNotifications(2, true ^ hasActiveClearableNotifications(1));
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$5 */
    public /* synthetic */ void lambda$new$5$NotificationStackScrollLayout(String str, String str2) {
        if (str.equals("high_priority")) {
            this.mHighPriorityBeforeSpeedBump = "1".equals(str2);
        } else if (str.equals("notification_dismiss_rtl")) {
            updateDismissRtlSetting("1".equals(str2));
        } else if (str.equals("notification_history_enabled")) {
            updateFooter();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$6 */
    public /* synthetic */ Unit lambda$new$6$NotificationStackScrollLayout(MiuiKeyguardMediaController miuiKeyguardMediaController, Boolean bool) {
        if (bool.booleanValue()) {
            generateAddAnimation(miuiKeyguardMediaController.getView(), false);
        } else {
            generateRemoveAnimation(miuiKeyguardMediaController.getView());
        }
        requestChildrenUpdate();
        return null;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$7 */
    public /* synthetic */ Unit lambda$new$7$NotificationStackScrollLayout(ZenModeViewController zenModeViewController, Boolean bool, Boolean bool2) {
        if (bool.booleanValue()) {
            generateAddAnimation(zenModeViewController.getView(), false);
        } else if (!bool2.booleanValue()) {
            generateRemoveAnimation(zenModeViewController.getView());
        }
        requestChildrenUpdate();
        return null;
    }

    private void initializeForegroundServiceSection(ForegroundServiceDismissalFeatureController foregroundServiceDismissalFeatureController) {
        if (foregroundServiceDismissalFeatureController.isForegroundServiceDismissalEnabled()) {
            ForegroundServiceDungeonView foregroundServiceDungeonView = (ForegroundServiceDungeonView) this.mFgsSectionController.createView(LayoutInflater.from(((ViewGroup) this).mContext));
            this.mFgsSectionView = foregroundServiceDungeonView;
            addView(foregroundServiceDungeonView, -1);
        }
    }

    private void updateDismissRtlSetting(boolean z) {
        this.mDismissRtl = z;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) childAt).setDismissRtl(z);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        inflateEmptyShadeView();
        inflateFooterView();
        this.mVisualStabilityManager.setVisibilityLocationProvider(new VisibilityLocationProvider() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$U5xT0qKII52vil_DFEsN5YX5CE0 */

            @Override // com.android.systemui.statusbar.notification.VisibilityLocationProvider
            public final boolean isInVisibleLocation(NotificationEntry notificationEntry) {
                return NotificationStackScrollLayout.this.isInVisibleLocation(notificationEntry);
            }
        });
        if (this.mAllowLongPress) {
            NotificationGutsManager notificationGutsManager = this.mNotificationGutsManager;
            Objects.requireNonNull(notificationGutsManager);
            setLongPressListener(new ExpandableNotificationRow.LongPressListener() {
                /* class com.android.systemui.statusbar.notification.stack.$$Lambda$0lGYUT66Z7cr4TZs4rdZ8M7DQkw */

                @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow.LongPressListener
                public final boolean onLongPress(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
                    return NotificationGutsManager.this.openGuts(view, i, i2, menuItem);
                }
            });
        }
    }

    public float getWakeUpHeight() {
        int collapsedHeight;
        ExpandableView firstChildWithBackground = getFirstChildWithBackground();
        if (firstChildWithBackground == null) {
            return 0.0f;
        }
        if (this.mKeyguardBypassController.getBypassEnabled()) {
            collapsedHeight = firstChildWithBackground.getHeadsUpHeightWithoutHeader();
        } else {
            collapsedHeight = firstChildWithBackground.getCollapsedHeight();
        }
        return (float) collapsedHeight;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        reinflateViews();
    }

    private void reinflateViews() {
        inflateFooterView();
        inflateEmptyShadeView();
        updateFooter();
        this.mSectionsManager.reinflateViews(LayoutInflater.from(((ViewGroup) this).mContext));
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onThemeChanged() {
        updateFooter();
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onOverlayChanged() {
        int dimensionPixelSize = ((ViewGroup) this).mContext.getResources().getDimensionPixelSize(Utils.getThemeAttr(((ViewGroup) this).mContext, 16844145));
        if (this.mCornerRadius != dimensionPixelSize) {
            this.mCornerRadius = dimensionPixelSize;
            invalidate();
        }
        reinflateViews();
    }

    @VisibleForTesting
    public void updateFooter() {
        if (this.mFooterView != null) {
            if (((this.mClearAllEnabled && hasActiveClearableNotifications(0)) || hasActiveNotifications()) && this.mStatusBarState != 1) {
                this.mRemoteInputManager.getController().isRemoteInputActive();
            }
            Settings.Secure.getIntForUser(((ViewGroup) this).mContext.getContentResolver(), "notification_history_enabled", 0, -2);
            updateFooterView(false, false, false);
        }
    }

    public boolean hasActiveClearableNotifications(int i) {
        if (this.mDynamicPrivacyController.isInLockedDownShade()) {
            return false;
        }
        int childCount = getChildCount();
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = getChildAt(i2);
            if (childAt instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                if (expandableNotificationRow.canViewBeDismissed() && matchesSelection(expandableNotificationRow, i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public RemoteInputController.Delegate createDelegate() {
        return new RemoteInputController.Delegate() {
            /* class com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnonymousClass11 */

            @Override // com.android.systemui.statusbar.RemoteInputController.Delegate
            public void setRemoteInputActive(NotificationEntry notificationEntry, boolean z) {
                NotificationStackScrollLayout.this.mHeadsUpManager.setRemoteInputActive(notificationEntry, z);
                notificationEntry.notifyHeightChanged(true);
                NotificationStackScrollLayout.this.updateFooter();
            }

            @Override // com.android.systemui.statusbar.RemoteInputController.Delegate
            public void lockScrollTo(NotificationEntry notificationEntry) {
                NotificationStackScrollLayout.this.lockScrollTo(notificationEntry.getRow());
            }

            @Override // com.android.systemui.statusbar.RemoteInputController.Delegate
            public void requestDisallowLongPressAndDismiss() {
                NotificationStackScrollLayout.this.requestDisallowLongPress();
                NotificationStackScrollLayout.this.requestDisallowDismiss();
            }
        };
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((SysuiStatusBarStateController) Dependency.get(StatusBarStateController.class)).addCallback(this.mStateListener, 2);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((StatusBarStateController) Dependency.get(StatusBarStateController.class)).removeCallback(this.mStateListener);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).removeCallback(this);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public NotificationSwipeActionHelper getSwipeActionHelper() {
        return this.mSwipeHelper;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onUiModeChanged() {
        this.mBgColor = ((ViewGroup) this).mContext.getColor(C0011R$color.notification_shade_background_color);
        updateBackgroundDimming();
        this.mShelf.onUiModeChanged();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        if (this.mShouldDrawNotificationBackground) {
            int i = this.mSections[0].getCurrentBounds().top;
            NotificationSection[] notificationSectionArr = this.mSections;
            if (i < notificationSectionArr[notificationSectionArr.length - 1].getCurrentBounds().bottom || this.mAmbientState.isDozing()) {
                drawBackground(canvas);
                return;
            }
        }
        if (this.mInHeadsUpPinnedMode || this.mHeadsUpAnimatingAway) {
            drawHeadsUpBackground(canvas);
        }
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
    }

    private void drawBackground(Canvas canvas) {
        boolean z;
        boolean z2;
        int i = this.mSidePaddings;
        int width = getWidth() - this.mSidePaddings;
        boolean z3 = false;
        int i2 = this.mSections[0].getCurrentBounds().top;
        NotificationSection[] notificationSectionArr = this.mSections;
        int i3 = notificationSectionArr[notificationSectionArr.length - 1].getCurrentBounds().bottom;
        int i4 = this.mTopPadding;
        float f = 1.0f - this.mInterpolatedHideAmount;
        float interpolation = this.mHideXInterpolator.getInterpolation((1.0f - this.mLinearHideAmount) * this.mBackgroundXFactor);
        float width2 = (float) (getWidth() / 2);
        int lerp = (int) MathUtils.lerp(width2, (float) i, interpolation);
        int lerp2 = (int) MathUtils.lerp(width2, (float) width, interpolation);
        float f2 = (float) i4;
        int lerp3 = (int) MathUtils.lerp(f2, (float) i2, f);
        this.mBackgroundAnimationRect.set(lerp, lerp3, lerp2, (int) MathUtils.lerp(f2, (float) i3, f));
        int i5 = lerp3 - i2;
        NotificationSection[] notificationSectionArr2 = this.mSections;
        int length = notificationSectionArr2.length;
        int i6 = 0;
        while (true) {
            if (i6 >= length) {
                z = false;
                break;
            } else if (notificationSectionArr2[i6].needsBackground()) {
                z = true;
                break;
            } else {
                i6++;
            }
        }
        if (!this.mKeyguardBypassController.getBypassEnabled() || !onKeyguard()) {
            if (!this.mAmbientState.isDozing() || z) {
                z3 = true;
            }
            z2 = z3;
        } else {
            z2 = isPulseExpanding();
        }
        if (z2) {
            drawBackgroundRects(canvas, lerp, lerp2, lerp3, i5);
        }
        updateClipping();
    }

    private void drawBackgroundRects(Canvas canvas, int i, int i2, int i3, int i4) {
        int i5 = i2;
        NotificationSection[] notificationSectionArr = this.mSections;
        int length = notificationSectionArr.length;
        int i6 = 1;
        int i7 = i;
        int i8 = i5;
        int i9 = this.mSections[0].getCurrentBounds().bottom + i4;
        int i10 = 0;
        boolean z = true;
        int i11 = i3;
        while (i10 < length) {
            NotificationSection notificationSection = notificationSectionArr[i10];
            if (notificationSection.needsBackground()) {
                int i12 = notificationSection.getCurrentBounds().top + i4;
                int min = Math.min(Math.max(i, notificationSection.getCurrentBounds().left), i5);
                int max = Math.max(Math.min(i5, notificationSection.getCurrentBounds().right), min);
                if (i12 - i9 > i6 || (!(i7 == min && i8 == max) && !z)) {
                    int i13 = this.mCornerRadius;
                    canvas.drawRoundRect((float) i7, (float) i11, (float) i8, (float) i9, (float) i13, (float) i13, this.mBackgroundPaint);
                    i11 = i12;
                }
                i9 = notificationSection.getCurrentBounds().bottom + i4;
                i8 = max;
                i7 = min;
                z = false;
            }
            i10++;
            i5 = i2;
            i6 = 1;
        }
        int i14 = this.mCornerRadius;
        canvas.drawRoundRect((float) i7, (float) i11, (float) i8, (float) i9, (float) i14, (float) i14, this.mBackgroundPaint);
    }

    private void drawHeadsUpBackground(Canvas canvas) {
        int i = this.mSidePaddings;
        int width = getWidth() - this.mSidePaddings;
        int childCount = getChildCount();
        float height = (float) getHeight();
        float f = 0.0f;
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = getChildAt(i2);
            if (childAt.getVisibility() != 8 && (childAt instanceof ExpandableNotificationRow)) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                if ((expandableNotificationRow.isPinned() || expandableNotificationRow.isHeadsUpAnimatingAway()) && expandableNotificationRow.getTranslation() < 0.0f && expandableNotificationRow.getProvider().shouldShowGutsOnSnapOpen()) {
                    float min = Math.min(height, expandableNotificationRow.getTranslationY());
                    f = Math.max(f, expandableNotificationRow.getTranslationY() + ((float) expandableNotificationRow.getActualHeight()));
                    height = min;
                }
            }
        }
        if (height < f) {
            int i3 = this.mCornerRadius;
            canvas.drawRoundRect((float) i, height, (float) width, f, (float) i3, (float) i3, this.mBackgroundPaint);
        }
    }

    /* access modifiers changed from: private */
    public void updateBackgroundDimming() {
        int blendARGB;
        if (this.mShouldDrawNotificationBackground && this.mCachedBackgroundColor != (blendARGB = ColorUtils.blendARGB(this.mBgColor, -1, MathUtils.smoothStep(0.4f, 1.0f, this.mLinearHideAmount)))) {
            this.mCachedBackgroundColor = blendARGB;
            this.mBackgroundPaint.setColor(blendARGB);
            invalidate();
        }
    }

    private void initView(Context context) {
        this.mScroller = new OverScroller(getContext());
        setDescendantFocusability(262144);
        setClipChildren(false);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mSlopMultiplier = viewConfiguration.getScaledAmbiguousGestureMultiplier();
        this.mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mOverflingDistance = viewConfiguration.getScaledOverflingDistance();
        Resources resources = context.getResources();
        this.mCollapsedSize = resources.getDimensionPixelSize(C0012R$dimen.notification_min_height);
        this.mGapHeight = resources.getDimensionPixelSize(C0012R$dimen.notification_section_divider_height);
        this.mStackScrollAlgorithm.initView(context);
        this.mAmbientState.reload(context);
        this.mPaddingBetweenElements = Math.max(1, resources.getDimensionPixelSize(C0012R$dimen.notification_divider_height));
        this.mIncreasedPaddingBetweenElements = resources.getDimensionPixelSize(C0012R$dimen.notification_divider_height_increased);
        this.mMinTopOverScrollToEscape = (float) resources.getDimensionPixelSize(C0012R$dimen.min_top_overscroll_to_qs);
        this.mStatusBarHeight = resources.getDimensionPixelSize(C0012R$dimen.status_bar_height);
        this.mBottomMargin = resources.getDimensionPixelSize(C0012R$dimen.notification_panel_margin_bottom);
        this.mSidePaddings = resources.getDimensionPixelSize(C0012R$dimen.notification_side_paddings);
        this.mMinInteractionHeight = resources.getDimensionPixelSize(C0012R$dimen.notification_min_interaction_height);
        this.mCornerRadius = resources.getDimensionPixelSize(Utils.getThemeAttr(((ViewGroup) this).mContext, 16844145));
        this.mHeadsUpInset = this.mStatusBarHeight + resources.getDimensionPixelSize(C0012R$dimen.heads_up_status_bar_padding);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyHeightChangeListener(ExpandableView expandableView) {
        notifyHeightChangeListener(expandableView, false);
    }

    private void notifyHeightChangeListener(ExpandableView expandableView, boolean z) {
        ExpandableView.OnHeightChangedListener onHeightChangedListener = this.mOnHeightChangedListener;
        if (onHeightChangedListener != null) {
            onHeightChangedListener.onHeightChanged(expandableView, z);
        }
    }

    public boolean isPulseExpanding() {
        return this.mAmbientState.isPulseExpanding();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        super.onMeasure(i, i2);
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i) - (this.mSidePaddings * 2), View.MeasureSpec.getMode(i));
        int makeMeasureSpec2 = View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.getSize(i2), 0);
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            measureChild(getChildAt(i3), makeMeasureSpec, makeMeasureSpec2);
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        float width = ((float) getWidth()) / 2.0f;
        for (int i5 = 0; i5 < getChildCount(); i5++) {
            View childAt = getChildAt(i5);
            float measuredWidth = ((float) childAt.getMeasuredWidth()) / 2.0f;
            childAt.layout((int) (width - measuredWidth), 0, (int) (measuredWidth + width), (int) ((float) childAt.getMeasuredHeight()));
        }
        setMaxLayoutHeight(getHeight());
        updateContentHeight();
        clampScrollPosition();
        requestChildrenUpdate();
        updateFirstAndLastBackgroundViews();
        updateAlgorithmLayoutMinHeight();
        updateOwnTranslationZ();
    }

    private void requestAnimationOnViewResize(ExpandableNotificationRow expandableNotificationRow) {
        if (!this.mAnimationsEnabled) {
            return;
        }
        if (this.mIsExpanded || (expandableNotificationRow != null && expandableNotificationRow.isPinned())) {
            this.mNeedViewResizeAnimation = true;
            this.mNeedsAnimation = true;
        }
    }

    public void updateSpeedBumpIndex(int i, boolean z) {
        this.mAmbientState.setSpeedBumpIndex(i);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void setChildLocationsChangedListener(NotificationLogger.OnChildLocationsChangedListener onChildLocationsChangedListener) {
        this.mListener = onChildLocationsChangedListener;
    }

    @Override // com.android.systemui.statusbar.notification.VisibilityLocationProvider
    public boolean isInVisibleLocation(NotificationEntry notificationEntry) {
        ExpandableNotificationRow row = notificationEntry.getRow();
        ExpandableViewState viewState = row.getViewState();
        if (viewState == null || (viewState.location & 5) == 0 || row.getVisibility() != 0) {
            return false;
        }
        return true;
    }

    private void setMaxLayoutHeight(int i) {
        this.mMaxLayoutHeight = i;
        this.mShelf.setMaxLayoutHeight(i);
        updateAlgorithmHeightAndPadding();
    }

    private void updateAlgorithmHeightAndPadding() {
        this.mAmbientState.setLayoutHeight(getLayoutHeight());
        updateAlgorithmLayoutMinHeight();
        this.mAmbientState.setTopPadding(this.mTopPadding);
    }

    private void updateAlgorithmLayoutMinHeight() {
        int i;
        AmbientState ambientState = this.mAmbientState;
        if (this.mQsExpanded || isHeadsUpTransition()) {
            i = getLayoutMinHeight();
        } else {
            i = 0;
        }
        ambientState.setLayoutMinHeight(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateChildren() {
        float f;
        updateScrollStateForAddedChildren();
        AmbientState ambientState = this.mAmbientState;
        if (this.mScroller.isFinished()) {
            f = 0.0f;
        } else {
            f = this.mScroller.getCurrVelocity();
        }
        ambientState.setCurrentScrollVelocity(f);
        this.mAmbientState.setScrollY(this.mOwnScrollY);
        this.mStackScrollAlgorithm.resetViewStates(this.mAmbientState);
        if (isCurrentlyAnimating() || this.mNeedsAnimation) {
            startAnimationToState();
        } else {
            applyCurrentState();
        }
    }

    public void updateChildrenBg() {
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView instanceof MiuiExpandableNotificationRow) {
                ((MiuiExpandableNotificationRow) expandableView).updateBackground();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onPreDrawDuringAnimation() {
        this.mShelf.updateAppearance();
        updateClippingToTopRoundedCorner();
        if (!this.mNeedsAnimation && !this.mChildrenUpdateRequested) {
            updateBackground();
        }
    }

    private void updateClippingToTopRoundedCorner() {
        Float valueOf = Float.valueOf(((float) this.mTopPadding) + this.mStackTranslation + ((float) this.mAmbientState.getExpandAnimationTopChange()));
        Float valueOf2 = Float.valueOf(valueOf.floatValue() + ((float) this.mCornerRadius));
        boolean z = true;
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                float translationY = expandableView.getTranslationY();
                float actualHeight = ((float) expandableView.getActualHeight()) + translationY;
                expandableView.setDistanceToTopRoundness((!z || !isScrolledToTop()) & (((valueOf.floatValue() > translationY ? 1 : (valueOf.floatValue() == translationY ? 0 : -1)) > 0 && (valueOf.floatValue() > actualHeight ? 1 : (valueOf.floatValue() == actualHeight ? 0 : -1)) < 0) || ((valueOf2.floatValue() > translationY ? 1 : (valueOf2.floatValue() == translationY ? 0 : -1)) >= 0 && (valueOf2.floatValue() > actualHeight ? 1 : (valueOf2.floatValue() == actualHeight ? 0 : -1)) <= 0)) ? Math.max(translationY - valueOf.floatValue(), 0.0f) : -1.0f);
                z = false;
            }
        }
    }

    private void updateScrollStateForAddedChildren() {
        if (!this.mChildrenToAddAnimated.isEmpty()) {
            for (int i = 0; i < getChildCount(); i++) {
                ExpandableView expandableView = (ExpandableView) getChildAt(i);
                if (this.mChildrenToAddAnimated.contains(expandableView)) {
                    int positionInLinearLayout = getPositionInLinearLayout(expandableView);
                    float increasedPaddingAmount = expandableView.getIncreasedPaddingAmount();
                    int intrinsicHeight = getIntrinsicHeight(expandableView) + (increasedPaddingAmount == 1.0f ? this.mIncreasedPaddingBetweenElements : increasedPaddingAmount == -1.0f ? 0 : this.mPaddingBetweenElements);
                    int i2 = this.mOwnScrollY;
                    if (positionInLinearLayout < i2) {
                        setOwnScrollY(i2 + intrinsicHeight);
                    }
                }
            }
            clampScrollPosition();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateForcedScroll() {
        View view = this.mForcedScroll;
        if (view != null && (!view.hasFocus() || !this.mForcedScroll.isAttachedToWindow())) {
            this.mForcedScroll = null;
        }
        View view2 = this.mForcedScroll;
        if (view2 != null) {
            ExpandableView expandableView = (ExpandableView) view2;
            int positionInLinearLayout = getPositionInLinearLayout(expandableView);
            int targetScrollForView = targetScrollForView(expandableView, positionInLinearLayout);
            int intrinsicHeight = positionInLinearLayout + expandableView.getIntrinsicHeight();
            int max = Math.max(0, Math.min(targetScrollForView, getScrollRange()));
            int i = this.mOwnScrollY;
            if (i < max || intrinsicHeight < i) {
                setOwnScrollY(max);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void requestChildrenUpdate() {
        if (!this.mChildrenUpdateRequested) {
            getViewTreeObserver().addOnPreDrawListener(this.mChildrenUpdater);
            this.mChildrenUpdateRequested = true;
            invalidate();
        }
    }

    public int getVisibleNotificationCount() {
        int i = 0;
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            View childAt = getChildAt(i2);
            if (childAt.getVisibility() != 8 && (childAt instanceof ExpandableNotificationRow)) {
                i++;
            }
        }
        return i;
    }

    private boolean isCurrentlyAnimating() {
        return this.mStateAnimator.isRunning();
    }

    private void clampScrollPosition() {
        int scrollRange = getScrollRange();
        if (scrollRange < this.mOwnScrollY) {
            setOwnScrollY(scrollRange);
        }
    }

    public int getTopPadding() {
        return this.mTopPadding;
    }

    private void setTopPadding(int i, boolean z) {
        if (this.mTopPadding != i) {
            this.mTopPadding = i;
            updateAlgorithmHeightAndPadding();
            updateContentHeight();
            if (z && this.mAnimationsEnabled && this.mIsExpanded) {
                this.mTopPaddingNeedsAnimation = true;
                this.mNeedsAnimation = true;
            }
            requestChildrenUpdate();
            notifyHeightChangeListener(null, z);
        }
    }

    public void setExpandedHeight(float f) {
        int i;
        float f2;
        this.mExpandedHeight = f;
        float f3 = 0.0f;
        boolean z = true;
        setIsExpanded(f > 0.0f);
        float minExpansionHeight = (float) getMinExpansionHeight();
        if (f < minExpansionHeight) {
            Rect rect = this.mClipRect;
            rect.left = 0;
            rect.right = getWidth();
            Rect rect2 = this.mClipRect;
            rect2.top = 0;
            rect2.bottom = (int) f;
            setRequestedClipBounds(rect2);
            f = minExpansionHeight;
        } else {
            setRequestedClipBounds(null);
        }
        float appearEndPosition = getAppearEndPosition();
        float appearStartPosition = getAppearStartPosition();
        float f4 = 1.0f;
        if (f >= appearEndPosition) {
            z = false;
        }
        this.mAmbientState.setAppearing(z);
        if (z) {
            f4 = calculateAppearFraction(f);
            if (f4 >= 0.0f) {
                f2 = NotificationUtils.interpolate(getExpandTranslationStart(), 0.0f, f4);
            } else {
                f2 = (f - appearStartPosition) + getExpandTranslationStart();
            }
            i = (int) (f - f2);
            if (isHeadsUpTransition()) {
                f3 = MathUtils.lerp((float) (this.mHeadsUpInset - this.mTopPadding), 0.0f, f4);
            } else if (!((MiuiNotificationPanelViewController) this.mNotificationPanelController).isNCSwitching()) {
                f3 = f2;
            }
        } else if (this.mShouldShowShelfOnly) {
            i = this.mTopPadding + this.mShelf.getIntrinsicHeight();
        } else {
            if (this.mQsExpanded) {
                int i2 = (this.mContentHeight - this.mTopPadding) + this.mIntrinsicPadding;
                int intrinsicHeight = this.mMaxTopPadding + this.mShelf.getIntrinsicHeight();
                if (i2 <= intrinsicHeight) {
                    i = intrinsicHeight;
                } else {
                    f = NotificationUtils.interpolate((float) i2, (float) intrinsicHeight, this.mQsExpansionFraction);
                }
            }
            i = (int) f;
        }
        this.mAmbientState.setAppearFraction(f4);
        if (i != this.mCurrentStackHeight) {
            this.mCurrentStackHeight = i;
            updateAlgorithmHeightAndPadding();
            requestChildrenUpdate();
        }
        setStackTranslation(f3);
        notifyAppearChangedListeners();
    }

    private void notifyAppearChangedListeners() {
        float f;
        float f2;
        if (!this.mKeyguardBypassController.getBypassEnabled() || !onKeyguard()) {
            f2 = MathUtils.saturate(calculateAppearFraction(this.mExpandedHeight));
            f = this.mExpandedHeight;
        } else {
            f2 = calculateAppearFractionBypass();
            f = getPulseHeight();
        }
        if (!(f2 == this.mLastSentAppear && f == this.mLastSentExpandedHeight)) {
            this.mLastSentAppear = f2;
            this.mLastSentExpandedHeight = f;
            for (int i = 0; i < this.mExpandedHeightListeners.size(); i++) {
                this.mExpandedHeightListeners.get(i).accept(Float.valueOf(f), Float.valueOf(f2));
            }
        }
    }

    private void setRequestedClipBounds(Rect rect) {
        this.mRequestedClipBounds = rect;
        updateClipping();
    }

    public int getIntrinsicContentHeight() {
        return this.mIntrinsicContentHeight;
    }

    public void updateClipping() {
        boolean z = true;
        boolean z2 = this.mRequestedClipBounds != null && !this.mInHeadsUpPinnedMode && !this.mHeadsUpAnimatingAway;
        if (this.mIsClipped != z2) {
            this.mIsClipped = z2;
        }
        if (this.mAmbientState.isHiddenAtAll()) {
            invalidateOutline();
            if (isFullyHidden()) {
                setClipBounds(null);
            }
        } else {
            if (z2) {
                setClipBounds(this.mRequestedClipBounds);
            } else {
                setClipBounds(null);
            }
            z = false;
        }
        setClipToOutline(z);
    }

    private float getExpandTranslationStart() {
        return (float) (((-this.mTopPadding) + getMinExpansionHeight()) - this.mShelf.getIntrinsicHeight());
    }

    private float getAppearStartPosition() {
        int minExpansionHeight;
        if (isHeadsUpTransition()) {
            int i = 0;
            if (!(getFirstVisibleSection() == null || getFirstVisibleSection().getFirstVisibleChild() == null)) {
                i = getFirstVisibleSection().getFirstVisibleChild().getPinnedHeadsUpHeight();
            }
            minExpansionHeight = this.mHeadsUpInset + i;
        } else {
            minExpansionHeight = getMinExpansionHeight();
        }
        return (float) minExpansionHeight;
    }

    private int getTopHeadsUpPinnedHeight() {
        NotificationEntry groupSummary;
        NotificationEntry topEntry = this.mHeadsUpManager.getTopEntry();
        if (topEntry == null) {
            return 0;
        }
        ExpandableNotificationRow row = topEntry.getRow();
        if (row.isChildInGroup() && (groupSummary = this.mGroupManager.getGroupSummary(row.getEntry().getSbn())) != null) {
            row = groupSummary.getRow();
        }
        return row.getPinnedHeadsUpHeight();
    }

    private float getAppearEndPosition() {
        int i;
        int visibleNotificationCount = getVisibleNotificationCount();
        int i2 = 0;
        if (this.mEmptyShadeView.getVisibility() != 8 || visibleNotificationCount <= 0) {
            i2 = this.mEmptyShadeView.getHeight();
        } else {
            if (isHeadsUpTransition() || (this.mHeadsUpManager.hasPinnedHeadsUp() && !this.mAmbientState.isDozing())) {
                if (this.mShelf.getVisibility() != 8 && visibleNotificationCount > 1) {
                    i2 = 0 + this.mShelf.getIntrinsicHeight() + this.mPaddingBetweenElements;
                }
                i = getTopHeadsUpPinnedHeight() + getPositionInLinearLayout(this.mAmbientState.getTrackedHeadsUpRow());
            } else if (this.mShelf.getVisibility() != 8) {
                i = this.mShelf.getIntrinsicHeight();
            }
            i2 += i;
        }
        return (float) (i2 + (onKeyguard() ? this.mTopPadding : this.mIntrinsicPadding));
    }

    private boolean isHeadsUpTransition() {
        return this.mAmbientState.getTrackedHeadsUpRow() != null;
    }

    public float calculateAppearFraction(float f) {
        float appearEndPosition = getAppearEndPosition();
        float appearStartPosition = getAppearStartPosition();
        return (f - appearStartPosition) / (appearEndPosition - appearStartPosition);
    }

    public float getStackTranslation() {
        return this.mStackTranslation;
    }

    private void setStackTranslation(float f) {
        if (f != this.mStackTranslation) {
            this.mStackTranslation = f;
            this.mAmbientState.setStackTranslation(f);
            requestChildrenUpdate();
        }
    }

    private int getLayoutHeight() {
        if (!onKeyguard() && isExpanded()) {
            return this.mMaxLayoutHeight;
        }
        return Math.min(this.mMaxLayoutHeight, this.mCurrentStackHeight);
    }

    public void setQsContainer(ViewGroup viewGroup) {
        this.mQsContainer = viewGroup;
    }

    public static boolean isPinnedHeadsUp(View view) {
        if (!(view instanceof ExpandableNotificationRow)) {
            return false;
        }
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        if (!expandableNotificationRow.isHeadsUp() || !expandableNotificationRow.isPinned()) {
            return false;
        }
        return true;
    }

    private boolean isHeadsUp(View view) {
        if (view instanceof ExpandableNotificationRow) {
            return ((ExpandableNotificationRow) view).isHeadsUp();
        }
        return false;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ExpandableView getChildAtPosition(float f, float f2) {
        return getChildAtPosition(f, f2, true, true);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private ExpandableView getChildAtPosition(float f, float f2, boolean z, boolean z2) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() == 0 && (!z2 || !(expandableView instanceof StackScrollerDecorView))) {
                float translationY = expandableView.getTranslationY();
                float clipTopAmount = ((float) expandableView.getClipTopAmount()) + translationY;
                float actualHeight = (((float) expandableView.getActualHeight()) + translationY) - ((float) expandableView.getClipBottomAmount());
                int width = getWidth();
                if ((actualHeight - clipTopAmount >= ((float) this.mMinInteractionHeight) || !z) && f2 >= clipTopAmount && f2 <= actualHeight && f >= ((float) 0) && f <= ((float) width)) {
                    if (!(expandableView instanceof ExpandableNotificationRow)) {
                        return expandableView;
                    }
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                    NotificationEntry entry = expandableNotificationRow.getEntry();
                    if (this.mIsExpanded || !expandableNotificationRow.isHeadsUp() || !expandableNotificationRow.isPinned() || this.mHeadsUpManager.getTopEntry().getRow() == expandableNotificationRow || this.mGroupManager.getGroupSummary(this.mHeadsUpManager.getTopEntry().getSbn()) == entry) {
                        return expandableNotificationRow.getViewAtPosition(f2 - translationY);
                    }
                }
            }
        }
        return null;
    }

    public ExpandableView getChildAtRawPosition(float f, float f2) {
        getLocationOnScreen(this.mTempInt2);
        int[] iArr = this.mTempInt2;
        return getChildAtPosition(f - ((float) iArr[0]), f2 - ((float) iArr[1]));
    }

    public void setScrollingEnabled(boolean z) {
        this.mScrollingEnabled = z;
    }

    public void lockScrollTo(View view) {
        if (this.mForcedScroll != view) {
            this.mForcedScroll = view;
            scrollTo(view);
        }
    }

    public boolean scrollTo(View view) {
        ExpandableView expandableView = (ExpandableView) view;
        int positionInLinearLayout = getPositionInLinearLayout(view);
        int targetScrollForView = targetScrollForView(expandableView, positionInLinearLayout);
        int intrinsicHeight = positionInLinearLayout + expandableView.getIntrinsicHeight();
        int i = this.mOwnScrollY;
        if (i >= targetScrollForView && intrinsicHeight >= i) {
            return false;
        }
        OverScroller overScroller = this.mScroller;
        int i2 = ((ViewGroup) this).mScrollX;
        int i3 = this.mOwnScrollY;
        overScroller.startScroll(i2, i3, 0, targetScrollForView - i3);
        this.mDontReportNextOverScroll = true;
        lambda$new$1();
        return true;
    }

    private int targetScrollForView(ExpandableView expandableView, int i) {
        return (((i + expandableView.getIntrinsicHeight()) + getImeInset()) - getHeight()) + ((isExpanded() || !isPinnedHeadsUp(expandableView)) ? getTopPadding() : this.mHeadsUpInset);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mBottomInset = windowInsets.getSystemWindowInsetBottom();
        this.mWaterfallTopInset = 0;
        DisplayCutout displayCutout = windowInsets.getDisplayCutout();
        if (displayCutout != null) {
            this.mWaterfallTopInset = displayCutout.getWaterfallInsets().top;
        }
        if (this.mOwnScrollY > getScrollRange()) {
            removeCallbacks(this.mReclamp);
            postDelayed(this.mReclamp, 50);
        } else {
            View view = this.mForcedScroll;
            if (view != null) {
                scrollTo(view);
            }
        }
        return windowInsets;
    }

    public void setExpandingEnabled(boolean z) {
        this.mExpandHelper.setEnabled(z);
    }

    private boolean isScrollingEnabled() {
        return this.mScrollingEnabled;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean onKeyguard() {
        return this.mStatusBarState == 1;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mStatusBarHeight = getResources().getDimensionPixelOffset(C0012R$dimen.status_bar_height);
        this.mSwipeHelper.setDensityScale(getResources().getDisplayMetrics().density);
        this.mSwipeHelper.setPagingTouchSlop((float) ViewConfiguration.get(getContext()).getScaledPagingTouchSlop());
        initView(getContext());
    }

    public void dismissViewAnimated(View view, Runnable runnable, int i, long j) {
        this.mSwipeHelper.dismissChild(view, 0.0f, runnable, (long) i, true, j, true);
    }

    private void snapViewIfNeeded(NotificationEntry notificationEntry) {
        ExpandableNotificationRow row = notificationEntry.getRow();
        boolean z = this.mIsExpanded || isPinnedHeadsUp(row);
        if (row.getProvider() != null) {
            this.mSwipeHelper.snapChildIfNeeded(row, z, row.getProvider().isMenuVisible() ? row.getTranslation() : 0.0f);
        }
    }

    private float overScrollUp(int i, int i2) {
        int max = Math.max(i, 0);
        float currentOverScrollAmount = getCurrentOverScrollAmount(true);
        float f = currentOverScrollAmount - ((float) max);
        if (currentOverScrollAmount > 0.0f) {
            setOverScrollAmount(f, true, false);
        }
        float f2 = f < 0.0f ? -f : 0.0f;
        float f3 = ((float) this.mOwnScrollY) + f2;
        float f4 = (float) i2;
        if (f3 <= f4) {
            return f2;
        }
        if (!this.mExpandedInThisMotion) {
            setOverScrolledPixels((getCurrentOverScrolledPixels(false) + f3) - f4, false, false);
        }
        setOwnScrollY(i2);
        return 0.0f;
    }

    private float overScrollDown(int i) {
        int min = Math.min(i, 0);
        float currentOverScrollAmount = getCurrentOverScrollAmount(false);
        float f = ((float) min) + currentOverScrollAmount;
        if (currentOverScrollAmount > 0.0f) {
            setOverScrollAmount(f, false, false);
        }
        if (f >= 0.0f) {
            f = 0.0f;
        }
        float f2 = ((float) this.mOwnScrollY) + f;
        if (f2 >= 0.0f) {
            return f;
        }
        setOverScrolledPixels(getCurrentOverScrolledPixels(true) - f2, true, false);
        setOwnScrollY(0);
        return 0.0f;
    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private void initOrResetVelocityTracker() {
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
    }

    public void setFinishScrollingCallback(Runnable runnable) {
        this.mFinishScrollingCallback = runnable;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* renamed from: animateScroll */
    public void lambda$new$1() {
        if (this.mScroller.computeScrollOffset()) {
            int i = this.mOwnScrollY;
            int currY = this.mScroller.getCurrY();
            if (i != currY) {
                int scrollRange = getScrollRange();
                if ((currY < 0 && i >= 0) || (currY > scrollRange && i <= scrollRange)) {
                    setMaxOverScrollFromCurrentVelocity();
                }
                if (this.mDontClampNextScroll) {
                    scrollRange = Math.max(scrollRange, i);
                }
                customOverScrollBy(currY - i, i, scrollRange, (int) this.mMaxOverScroll);
            }
            postOnAnimation(this.mReflingAndAnimateScroll);
            return;
        }
        this.mDontClampNextScroll = false;
        Runnable runnable = this.mFinishScrollingCallback;
        if (runnable != null) {
            runnable.run();
        }
    }

    private void setMaxOverScrollFromCurrentVelocity() {
        float currVelocity = this.mScroller.getCurrVelocity();
        if (currVelocity >= ((float) this.mMinimumVelocity)) {
            this.mMaxOverScroll = (Math.abs(currVelocity) / 1000.0f) * ((float) this.mOverflingDistance);
        }
    }

    private void customOverScrollBy(int i, int i2, int i3, int i4) {
        int i5 = i2 + i;
        int i6 = -i4;
        int i7 = i4 + i3;
        boolean z = true;
        if (i5 > i7) {
            i5 = i7;
        } else if (i5 < i6) {
            i5 = i6;
        } else {
            z = false;
        }
        onCustomOverScrolled(i5, z);
    }

    public void setOverScrolledPixels(float f, boolean z, boolean z2) {
        setOverScrollAmount(f * getRubberBandFactor(z), z, z2, true);
    }

    public void setOverScrollAmount(float f, boolean z, boolean z2) {
        setOverScrollAmount(f, z, z2, true);
    }

    public void setOverScrollAmount(float f, boolean z, boolean z2, boolean z3) {
        setOverScrollAmount(f, z, z2, z3, isRubberbanded(z));
    }

    public void setOverScrollAmount(float f, boolean z, boolean z2, boolean z3, boolean z4) {
        if (z3) {
            this.mStateAnimator.cancelOverScrollAnimators(z);
        }
        setOverScrollAmountInternal(f, z, z2, z4);
    }

    private void setOverScrollAmountInternal(float f, boolean z, boolean z2, boolean z3) {
        float max = Math.max(0.0f, f);
        if (z2) {
            this.mStateAnimator.animateOverScrollToAmount(max, z, z3);
            return;
        }
        setOverScrolledPixels(max / getRubberBandFactor(z), z);
        this.mAmbientState.setOverScrollAmount(max, z);
        if (z) {
            notifyOverscrollTopListener(max, z3);
        }
        requestChildrenUpdate();
    }

    private void notifyOverscrollTopListener(float f, boolean z) {
        this.mExpandHelper.onlyObserveMovements(f > 1.0f);
        if (this.mDontReportNextOverScroll) {
            this.mDontReportNextOverScroll = false;
            return;
        }
        OnOverscrollTopChangedListener onOverscrollTopChangedListener = this.mOverscrollTopChangedListener;
        if (onOverscrollTopChangedListener != null) {
            onOverscrollTopChangedListener.onOverscrollTopChanged(f, z);
        }
    }

    public void setOverscrollTopChangedListener(OnOverscrollTopChangedListener onOverscrollTopChangedListener) {
        this.mOverscrollTopChangedListener = onOverscrollTopChangedListener;
    }

    public float getCurrentOverScrollAmount(boolean z) {
        return this.mAmbientState.getOverScrollAmount(z);
    }

    public float getCurrentOverScrolledPixels(boolean z) {
        return z ? this.mOverScrolledTopPixels : this.mOverScrolledBottomPixels;
    }

    private void setOverScrolledPixels(float f, boolean z) {
        if (z) {
            this.mOverScrolledTopPixels = f;
        } else {
            this.mOverScrolledBottomPixels = f;
        }
    }

    private void onCustomOverScrolled(int i, boolean z) {
        if (!this.mScroller.isFinished()) {
            setOwnScrollY(i);
            if (z) {
                springBack();
                return;
            }
            float currentOverScrollAmount = getCurrentOverScrollAmount(true);
            int i2 = this.mOwnScrollY;
            if (i2 < 0) {
                notifyOverscrollTopListener((float) (-i2), isRubberbanded(true));
            } else {
                notifyOverscrollTopListener(currentOverScrollAmount, isRubberbanded(true));
            }
        } else {
            setOwnScrollY(i);
        }
    }

    private void springBack() {
        boolean z;
        float f;
        int scrollRange = getScrollRange();
        boolean z2 = this.mOwnScrollY <= 0;
        boolean z3 = this.mOwnScrollY >= scrollRange;
        if (z2 || z3) {
            if (z2) {
                f = (float) (-this.mOwnScrollY);
                setOwnScrollY(0);
                this.mDontReportNextOverScroll = true;
                z = true;
            } else {
                setOwnScrollY(scrollRange);
                f = (float) (this.mOwnScrollY - scrollRange);
                z = false;
            }
            setOverScrollAmount(f, z, false);
            setOverScrollAmount(0.0f, z, true);
            this.mScroller.forceFinished(true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getScrollRange() {
        int i = this.mContentHeight;
        if (!isExpanded() && this.mHeadsUpManager.hasPinnedHeadsUp()) {
            i = this.mHeadsUpInset + getTopHeadsUpPinnedHeight();
        }
        int max = Math.max(0, i - this.mMaxLayoutHeight);
        int imeInset = getImeInset();
        return max + Math.min(imeInset, Math.max(0, i - (getHeight() - imeInset)));
    }

    private int getImeInset() {
        return Math.max(0, this.mBottomInset - (getRootView().getHeight() - getHeight()));
    }

    public ExpandableView getFirstChildNotGone() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (!(childAt.getVisibility() == 8 || childAt == this.mShelf)) {
                return (ExpandableView) childAt;
            }
        }
        return null;
    }

    private View getFirstChildBelowTranlsationY(float f, boolean z) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != 8) {
                float translationY = childAt.getTranslationY();
                if (translationY >= f) {
                    return childAt;
                }
                if (!z && (childAt instanceof ExpandableNotificationRow)) {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                    if (expandableNotificationRow.isSummaryWithChildren() && expandableNotificationRow.areChildrenExpanded()) {
                        List<ExpandableNotificationRow> attachedChildren = expandableNotificationRow.getAttachedChildren();
                        for (int i2 = 0; i2 < attachedChildren.size(); i2++) {
                            ExpandableNotificationRow expandableNotificationRow2 = attachedChildren.get(i2);
                            if (expandableNotificationRow2.getTranslationY() + translationY >= f) {
                                return expandableNotificationRow2;
                            }
                        }
                        continue;
                    }
                }
            }
        }
        return null;
    }

    public ExpandableView getLastChildNotGone() {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            View childAt = getChildAt(childCount);
            if (!(childAt.getVisibility() == 8 || childAt == this.mShelf)) {
                return (ExpandableView) childAt;
            }
        }
        return null;
    }

    public int getNotGoneChildCount() {
        int childCount = getChildCount();
        int i = 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i2);
            if (!(expandableView.getVisibility() == 8 || expandableView.willBeGone() || expandableView == this.mShelf)) {
                i++;
            }
        }
        return i;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateContentHeight() {
        float f;
        float f2;
        float f3;
        float f4 = (float) this.mPaddingBetweenElements;
        int i = this.mMaxDisplayedNotifications;
        ExpandableView expandableView = null;
        float f5 = 0.0f;
        int i2 = 0;
        int i3 = 0;
        boolean z = false;
        for (int i4 = 0; i4 < getChildCount(); i4++) {
            ExpandableView expandableView2 = (ExpandableView) getChildAt(i4);
            boolean z2 = expandableView2 == this.mFooterView && onKeyguard();
            if (expandableView2.getVisibility() != 8 && !expandableView2.hasNoContentHeight() && !z2) {
                if (i != -1 && i2 >= i) {
                    f = (float) this.mShelf.getIntrinsicHeight();
                    z = true;
                } else {
                    f = (float) expandableView2.getIntrinsicHeight();
                }
                float increasedPaddingAmount = expandableView2.getIncreasedPaddingAmount();
                if (increasedPaddingAmount >= 0.0f) {
                    f3 = (float) ((int) NotificationUtils.interpolate(f4, (float) this.mIncreasedPaddingBetweenElements, increasedPaddingAmount));
                    f2 = (float) ((int) NotificationUtils.interpolate((float) this.mPaddingBetweenElements, (float) this.mIncreasedPaddingBetweenElements, increasedPaddingAmount));
                } else {
                    int interpolate = (int) NotificationUtils.interpolate(0.0f, (float) this.mPaddingBetweenElements, 1.0f + increasedPaddingAmount);
                    float interpolate2 = f5 > 0.0f ? (float) ((int) NotificationUtils.interpolate((float) interpolate, (float) this.mIncreasedPaddingBetweenElements, f5)) : (float) interpolate;
                    f2 = (float) interpolate;
                    f3 = interpolate2;
                }
                if (i3 != 0) {
                    i3 = (int) (((float) i3) + f3);
                }
                i3 = (int) (((float) ((int) (((float) i3) + calculateGapHeight(expandableView, expandableView2, i2)))) + f);
                i2++;
                if (z) {
                    break;
                }
                f4 = f2;
                expandableView = expandableView2;
                f5 = increasedPaddingAmount;
            }
        }
        this.mIntrinsicContentHeight = i3;
        this.mContentHeight = i3 + Math.max(this.mIntrinsicPadding, this.mTopPadding) + this.mBottomMargin;
        updateScrollability();
        clampScrollPosition();
        this.mAmbientState.setLayoutMaxHeight(this.mContentHeight);
    }

    public float calculateGapHeight(ExpandableView expandableView, ExpandableView expandableView2, int i) {
        return this.mStackScrollAlgorithm.getGapHeightForChild(this.mSectionsManager, this.mAmbientState.getAnchorViewIndex(), i, expandableView2, expandableView);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public boolean hasPulsingNotifications() {
        return this.mPulsing;
    }

    private void updateScrollability() {
        boolean z = !this.mQsExpanded && getScrollRange() > 0;
        if (z != this.mScrollable) {
            this.mScrollable = z;
            setFocusable(z);
            updateForwardAndBackwardScrollability();
        }
    }

    private void updateForwardAndBackwardScrollability() {
        boolean z = true;
        boolean z2 = this.mScrollable && !isScrolledToBottom();
        boolean z3 = this.mScrollable && !isScrolledToTop();
        if (z2 == this.mForwardScrollable && z3 == this.mBackwardScrollable) {
            z = false;
        }
        this.mForwardScrollable = z2;
        this.mBackwardScrollable = z3;
        if (z) {
            sendAccessibilityEvent(2048);
        }
    }

    private void updateBackground() {
        ((NotificationPanelNavigationBarCoordinator) Dependency.get(NotificationPanelNavigationBarCoordinator.class)).switchNavigationBarModeIfNeed(this);
        if (this.mShouldDrawNotificationBackground) {
            updateBackgroundBounds();
            if (didSectionBoundsChange()) {
                boolean z = this.mAnimateNextSectionBoundsChange || this.mAnimateNextBackgroundTop || this.mAnimateNextBackgroundBottom || areSectionBoundsAnimating();
                if (!isExpanded()) {
                    abortBackgroundAnimators();
                    z = false;
                }
                if (z) {
                    startBackgroundAnimation();
                } else {
                    for (NotificationSection notificationSection : this.mSections) {
                        notificationSection.resetCurrentBounds();
                    }
                    invalidate();
                }
            } else {
                abortBackgroundAnimators();
            }
            this.mAnimateNextBackgroundTop = false;
            this.mAnimateNextBackgroundBottom = false;
            this.mAnimateNextSectionBoundsChange = false;
        }
    }

    private void abortBackgroundAnimators() {
        for (NotificationSection notificationSection : this.mSections) {
            notificationSection.cancelAnimators();
        }
    }

    private boolean didSectionBoundsChange() {
        for (NotificationSection notificationSection : this.mSections) {
            if (notificationSection.didBoundsChange()) {
                return true;
            }
        }
        return false;
    }

    private boolean areSectionBoundsAnimating() {
        for (NotificationSection notificationSection : this.mSections) {
            if (notificationSection.areBoundsAnimating()) {
                return true;
            }
        }
        return false;
    }

    private void startBackgroundAnimation() {
        boolean z;
        boolean z2;
        NotificationSection firstVisibleSection = getFirstVisibleSection();
        NotificationSection lastVisibleSection = getLastVisibleSection();
        NotificationSection[] notificationSectionArr = this.mSections;
        for (NotificationSection notificationSection : notificationSectionArr) {
            if (notificationSection == firstVisibleSection) {
                z = this.mAnimateNextBackgroundTop;
            } else {
                z = this.mAnimateNextSectionBoundsChange;
            }
            if (notificationSection == lastVisibleSection) {
                z2 = this.mAnimateNextBackgroundBottom;
            } else {
                z2 = this.mAnimateNextSectionBoundsChange;
            }
            notificationSection.startBackgroundAnimation(z, z2);
        }
    }

    private void updateBackgroundBounds() {
        int i;
        int i2 = this.mSidePaddings;
        int width = getWidth() - this.mSidePaddings;
        NotificationSection[] notificationSectionArr = this.mSections;
        for (NotificationSection notificationSection : notificationSectionArr) {
            notificationSection.getBounds().left = i2;
            notificationSection.getBounds().right = width;
        }
        if (!this.mIsExpanded) {
            NotificationSection[] notificationSectionArr2 = this.mSections;
            for (NotificationSection notificationSection2 : notificationSectionArr2) {
                notificationSection2.getBounds().top = 0;
                notificationSection2.getBounds().bottom = 0;
            }
            return;
        }
        NotificationSection lastVisibleSection = getLastVisibleSection();
        boolean z = true;
        boolean z2 = this.mStatusBarState == 1;
        if (!z2) {
            i = (int) (((float) this.mTopPadding) + this.mStackTranslation);
        } else if (lastVisibleSection == null) {
            i = this.mTopPadding;
        } else {
            NotificationSection firstVisibleSection = getFirstVisibleSection();
            firstVisibleSection.updateBounds(0, 0, false);
            i = firstVisibleSection.getBounds().top;
        }
        if (this.mHeadsUpManager.getAllEntries().count() > 1 || (!this.mAmbientState.isDozing() && (!this.mKeyguardBypassController.getBypassEnabled() || !z2))) {
            z = false;
        }
        NotificationSection[] notificationSectionArr3 = this.mSections;
        int length = notificationSectionArr3.length;
        int i3 = 0;
        while (i3 < length) {
            NotificationSection notificationSection3 = notificationSectionArr3[i3];
            i = notificationSection3.updateBounds(i, notificationSection3 == lastVisibleSection ? (int) (ViewState.getFinalTranslationY(this.mShelf) + ((float) this.mShelf.getIntrinsicHeight())) : i, z);
            i3++;
            z = false;
        }
    }

    private NotificationSection getFirstVisibleSection() {
        NotificationSection[] notificationSectionArr = this.mSections;
        for (NotificationSection notificationSection : notificationSectionArr) {
            if (notificationSection.getFirstVisibleChild() != null) {
                return notificationSection;
            }
        }
        return null;
    }

    private NotificationSection getLastVisibleSection() {
        for (int length = this.mSections.length - 1; length >= 0; length--) {
            NotificationSection notificationSection = this.mSections[length];
            if (notificationSection.getLastVisibleChild() != null) {
                return notificationSection;
            }
        }
        return null;
    }

    private ExpandableView getLastChildWithBackground() {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            ExpandableView expandableView = (ExpandableView) getChildAt(childCount);
            if (!(expandableView.getVisibility() == 8 || (expandableView instanceof StackScrollerDecorView) || expandableView == this.mShelf)) {
                return expandableView;
            }
        }
        return null;
    }

    private ExpandableView getFirstChildWithBackground() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (!(expandableView.getVisibility() == 8 || (expandableView instanceof StackScrollerDecorView) || expandableView == this.mShelf)) {
                return expandableView;
            }
        }
        return null;
    }

    private List<ExpandableView> getChildrenWithBackground() {
        ArrayList arrayList = new ArrayList();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (!(expandableView.getVisibility() == 8 || (expandableView instanceof StackScrollerDecorView) || expandableView == this.mShelf)) {
                arrayList.add(expandableView);
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public void fling(int i) {
        if (getChildCount() > 0) {
            float currentOverScrollAmount = getCurrentOverScrollAmount(true);
            int i2 = 0;
            float currentOverScrollAmount2 = getCurrentOverScrollAmount(false);
            if (i < 0 && currentOverScrollAmount > 0.0f) {
                setOwnScrollY(this.mOwnScrollY - ((int) currentOverScrollAmount));
                this.mDontReportNextOverScroll = true;
                setOverScrollAmount(0.0f, true, false);
                this.mMaxOverScroll = ((((float) Math.abs(i)) / 1000.0f) * getRubberBandFactor(true) * ((float) this.mOverflingDistance)) + currentOverScrollAmount;
            } else if (i <= 0 || currentOverScrollAmount2 <= 0.0f) {
                this.mMaxOverScroll = 0.0f;
            } else {
                setOwnScrollY((int) (((float) this.mOwnScrollY) + currentOverScrollAmount2));
                setOverScrollAmount(0.0f, false, false);
                this.mMaxOverScroll = ((((float) Math.abs(i)) / 1000.0f) * getRubberBandFactor(false) * ((float) this.mOverflingDistance)) + currentOverScrollAmount2;
            }
            int max = Math.max(0, getScrollRange());
            if (this.mExpandedInThisMotion) {
                max = Math.min(max, this.mMaxScrollAfterExpand);
            }
            OverScroller overScroller = this.mScroller;
            int i3 = ((ViewGroup) this).mScrollX;
            int i4 = this.mOwnScrollY;
            if (!this.mExpandedInThisMotion || i4 < 0) {
                i2 = 1073741823;
            }
            overScroller.fling(i3, i4, 1, i, 0, 0, 0, max, 0, i2);
            lambda$new$1();
        }
    }

    private boolean shouldOverScrollFling(int i) {
        float currentOverScrollAmount = getCurrentOverScrollAmount(true);
        if (!this.mScrolledToTopOnFirstDown || this.mExpandedInThisMotion || currentOverScrollAmount <= this.mMinTopOverScrollToEscape || i <= 0) {
            return false;
        }
        return true;
    }

    public void updateTopPadding(float f, boolean z) {
        int i = (int) f;
        int layoutMinHeight = getLayoutMinHeight() + i;
        if (layoutMinHeight > getHeight()) {
            this.mTopPaddingOverflow = (float) (layoutMinHeight - getHeight());
        } else {
            this.mTopPaddingOverflow = 0.0f;
        }
        setTopPadding(i, z && !this.mKeyguardBypassController.getBypassEnabled());
        setExpandedHeight(this.mExpandedHeight);
    }

    public void setMaxTopPadding(int i) {
        this.mMaxTopPadding = i;
    }

    public int getLayoutMinHeight() {
        if (isHeadsUpTransition()) {
            ExpandableNotificationRow trackedHeadsUpRow = this.mAmbientState.getTrackedHeadsUpRow();
            if (!trackedHeadsUpRow.isAboveShelf()) {
                return getTopHeadsUpPinnedHeight();
            }
            return getTopHeadsUpPinnedHeight() + ((int) MathUtils.lerp(0.0f, (float) getPositionInLinearLayout(trackedHeadsUpRow), this.mAmbientState.getAppearFraction()));
        } else if (this.mShelf.getVisibility() == 8) {
            return 0;
        } else {
            return this.mShelf.getIntrinsicHeight();
        }
    }

    public float getTopPaddingOverflow() {
        return this.mTopPaddingOverflow;
    }

    public int getPeekHeight() {
        int i;
        ExpandableView firstChildNotGone = getFirstChildNotGone();
        if (firstChildNotGone != null) {
            i = firstChildNotGone.getCollapsedHeight();
        } else {
            i = this.mCollapsedSize;
        }
        int i2 = 0;
        if (!(getLastVisibleSection() == null || this.mShelf.getVisibility() == 8)) {
            i2 = this.mShelf.getIntrinsicHeight();
        }
        return this.mIntrinsicPadding + i + i2;
    }

    private float getRubberBandFactor(boolean z) {
        if (!z) {
            return 0.35f;
        }
        if (this.mExpandedInThisMotion) {
            return 0.15f;
        }
        if (this.mScrolledToTopOnFirstDown) {
            return 1.0f;
        }
        if (this.mIsExpansionChanging || this.mPanelTracking) {
            return 0.21f;
        }
        return 0.35f;
    }

    private boolean isRubberbanded(boolean z) {
        return !z || this.mExpandedInThisMotion || this.mIsExpansionChanging || this.mPanelTracking || !this.mScrolledToTopOnFirstDown;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer, com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public void setChildTransferInProgress(boolean z) {
        Assert.isMainThread();
        this.mChildTransferInProgress = z;
    }

    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        if (!this.mChildTransferInProgress) {
            onViewRemovedInternal((ExpandableView) view, this);
        }
    }

    public void removeView(View view) {
        if (!(view == null || view.getAnimation() == null)) {
            view.clearAnimation();
        }
        super.removeView(view);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void cleanUpViewStateForEntry(NotificationEntry notificationEntry) {
        if (notificationEntry.getRow() == this.mSwipeHelper.getTranslatingParentView()) {
            this.mSwipeHelper.clearTranslatingParentView();
        }
    }

    private void onViewRemovedInternal(ExpandableView expandableView, ViewGroup viewGroup) {
        if (!this.mChangePositionInProgress) {
            expandableView.setOnHeightChangedListener(null);
            updateScrollStateForRemovedChild(expandableView);
            if (!generateRemoveAnimation(expandableView)) {
                this.mSwipedOutViews.remove(expandableView);
            } else if (!this.mSwipedOutViews.contains(expandableView) || Math.abs(expandableView.getTranslation()) != ((float) expandableView.getWidth())) {
                viewGroup.addTransientView(expandableView, 0);
                expandableView.setTransientContainer(viewGroup);
            }
            updateAnimationState(false, expandableView);
            focusNextViewIfFocused(expandableView);
        }
    }

    private void focusNextViewIfFocused(View view) {
        float f;
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (expandableNotificationRow.shouldRefocusOnDismiss()) {
                View childAfterViewWhenDismissed = expandableNotificationRow.getChildAfterViewWhenDismissed();
                if (childAfterViewWhenDismissed == null) {
                    View groupParentWhenDismissed = expandableNotificationRow.getGroupParentWhenDismissed();
                    if (groupParentWhenDismissed != null) {
                        f = groupParentWhenDismissed.getTranslationY();
                    } else {
                        f = view.getTranslationY();
                    }
                    childAfterViewWhenDismissed = getFirstChildBelowTranlsationY(f, true);
                }
                if (childAfterViewWhenDismissed != null) {
                    childAfterViewWhenDismissed.requestAccessibilityFocus();
                }
            }
        }
    }

    private boolean isChildInGroup(View view) {
        return (view instanceof ExpandableNotificationRow) && this.mGroupManager.isChildInGroupWithSummary(((ExpandableNotificationRow) view).getEntry().getSbn());
    }

    private boolean generateRemoveAnimation(ExpandableView expandableView) {
        if (removeRemovedChildFromHeadsUpChangeAnimations(expandableView)) {
            this.mAddedHeadsUpChildren.remove(expandableView);
            return false;
        } else if (isClickedHeadsUp(expandableView)) {
            this.mClearTransientViewsWhenFinished.add(expandableView);
            return true;
        } else {
            if (this.mIsExpanded && this.mAnimationsEnabled && !isChildInInvisibleGroup(expandableView)) {
                if (!this.mChildrenToAddAnimated.contains(expandableView)) {
                    this.mChildrenToRemoveAnimated.add(expandableView);
                    this.mNeedsAnimation = true;
                    return true;
                }
                this.mChildrenToAddAnimated.remove(expandableView);
                this.mFromMoreCardAdditions.remove(expandableView);
            }
            return false;
        }
    }

    private boolean isClickedHeadsUp(View view) {
        return HeadsUpUtil.isClickedHeadsUpNotification(view);
    }

    private boolean removeRemovedChildFromHeadsUpChangeAnimations(View view) {
        Iterator<Pair<ExpandableNotificationRow, Boolean>> it = this.mHeadsUpChangeAnimations.iterator();
        boolean z = false;
        while (it.hasNext()) {
            Pair<ExpandableNotificationRow, Boolean> next = it.next();
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) next.first;
            boolean booleanValue = ((Boolean) next.second).booleanValue();
            if (view == expandableNotificationRow) {
                this.mTmpList.add(next);
                z |= booleanValue;
            }
        }
        if (z) {
            this.mHeadsUpChangeAnimations.removeAll(this.mTmpList);
            ((ExpandableNotificationRow) view).setHeadsUpAnimatingAway(false);
        }
        this.mTmpList.clear();
        return z;
    }

    private boolean isChildInInvisibleGroup(View view) {
        if (!(view instanceof ExpandableNotificationRow)) {
            return false;
        }
        ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
        NotificationEntry groupSummary = this.mGroupManager.getGroupSummary(expandableNotificationRow.getEntry().getSbn());
        if (groupSummary == null || groupSummary.getRow() == expandableNotificationRow || expandableNotificationRow.getVisibility() != 4) {
            return false;
        }
        return true;
    }

    private void updateScrollStateForRemovedChild(ExpandableView expandableView) {
        float f;
        int positionInLinearLayout = getPositionInLinearLayout(expandableView);
        float increasedPaddingAmount = expandableView.getIncreasedPaddingAmount();
        if (increasedPaddingAmount >= 0.0f) {
            f = NotificationUtils.interpolate((float) this.mPaddingBetweenElements, (float) this.mIncreasedPaddingBetweenElements, increasedPaddingAmount);
        } else {
            f = NotificationUtils.interpolate(0.0f, (float) this.mPaddingBetweenElements, increasedPaddingAmount + 1.0f);
        }
        int intrinsicHeight = getIntrinsicHeight(expandableView) + ((int) f);
        int i = positionInLinearLayout + intrinsicHeight;
        int i2 = this.mOwnScrollY;
        if (i <= i2) {
            setOwnScrollY(i2 - intrinsicHeight);
        } else if (positionInLinearLayout < i2) {
            setOwnScrollY(positionInLinearLayout);
        }
    }

    private int getIntrinsicHeight(View view) {
        if (view instanceof ExpandableView) {
            return ((ExpandableView) view).getIntrinsicHeight();
        }
        return view.getHeight();
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r14v0, types: [com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout, android.view.ViewGroup] */
    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.systemui.statusbar.notification.row.ExpandableNotificationRow] */
    /* JADX WARN: Type inference failed for: r0v2 */
    /* JADX WARN: Type inference failed for: r0v3 */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getPositionInLinearLayout(android.view.View r15) {
        /*
        // Method dump skipped, instructions count: 146
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.getPositionInLinearLayout(android.view.View):int");
    }

    public void onViewAdded(View view) {
        super.onViewAdded(view);
        onViewAddedInternal((ExpandableView) view);
    }

    private void updateFirstAndLastBackgroundViews() {
        ExpandableView expandableView;
        NotificationSection firstVisibleSection = getFirstVisibleSection();
        NotificationSection lastVisibleSection = getLastVisibleSection();
        ExpandableView expandableView2 = null;
        if (firstVisibleSection == null) {
            expandableView = null;
        } else {
            expandableView = firstVisibleSection.getFirstVisibleChild();
        }
        if (lastVisibleSection != null) {
            expandableView2 = lastVisibleSection.getLastVisibleChild();
        }
        ExpandableView firstChildWithBackground = getFirstChildWithBackground();
        ExpandableView lastChildWithBackground = getLastChildWithBackground();
        boolean updateFirstAndLastViewsForAllSections = this.mSectionsManager.updateFirstAndLastViewsForAllSections(this.mSections, getChildrenWithBackground());
        if (!this.mAnimationsEnabled || !this.mIsExpanded) {
            this.mAnimateNextBackgroundTop = false;
            this.mAnimateNextBackgroundBottom = false;
            this.mAnimateNextSectionBoundsChange = false;
        } else {
            boolean z = true;
            this.mAnimateNextBackgroundTop = firstChildWithBackground != expandableView;
            if (lastChildWithBackground == expandableView2 && !this.mAnimateBottomOnLayout) {
                z = false;
            }
            this.mAnimateNextBackgroundBottom = z;
            this.mAnimateNextSectionBoundsChange = updateFirstAndLastViewsForAllSections;
        }
        this.mAmbientState.setLastVisibleBackgroundChild(lastChildWithBackground);
        this.mRoundnessManager.updateRoundedChildren(this.mSections);
        this.mAnimateBottomOnLayout = false;
        invalidate();
    }

    private void onViewAddedInternal(ExpandableView expandableView) {
        updateHideSensitiveForChild(expandableView);
        expandableView.setOnHeightChangedListener(this);
        generateAddAnimation(expandableView, false);
        updateAnimationState(expandableView);
        updateChronometerForChild(expandableView);
        if (expandableView instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) expandableView).setDismissRtl(this.mDismissRtl);
        }
    }

    private void updateHideSensitiveForChild(ExpandableView expandableView) {
        if (!(expandableView instanceof ExpandableNotificationRow) || !((ExpandableNotificationRow) expandableView).getEntry().hideSensitiveByAppLock) {
            expandableView.setHideSensitiveForIntrinsicHeight(this.mAmbientState.isHideSensitive());
        } else {
            expandableView.setHideSensitiveForIntrinsicHeight(true);
        }
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void notifyGroupChildRemoved(ExpandableView expandableView, ViewGroup viewGroup) {
        onViewRemovedInternal(expandableView, viewGroup);
    }

    @Override // com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public void notifyGroupChildRemoved(View view, ViewGroup viewGroup) {
        notifyGroupChildRemoved((ExpandableView) view, viewGroup);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void notifyGroupChildAdded(ExpandableView expandableView) {
        onViewAddedInternal(expandableView);
    }

    @Override // com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public void notifyGroupChildAdded(View view) {
        notifyGroupChildAdded((ExpandableView) view);
    }

    public void setAnimationsEnabled(boolean z) {
        this.mAnimationsEnabled = z;
        updateNotificationAnimationStates();
        if (!z) {
            this.mSwipedOutViews.clear();
            this.mChildrenToRemoveAnimated.clear();
            clearTemporaryViewsInGroup(this);
        }
    }

    private void updateNotificationAnimationStates() {
        boolean z = this.mAnimationsEnabled || hasPulsingNotifications();
        this.mShelf.setAnimationsEnabled(z);
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            z &= this.mIsExpanded || isPinnedHeadsUp(childAt);
            updateAnimationState(z, childAt);
        }
    }

    private void updateAnimationState(View view) {
        updateAnimationState((this.mAnimationsEnabled || hasPulsingNotifications()) && (this.mIsExpanded || isPinnedHeadsUp(view)), view);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void setExpandingNotification(ExpandableNotificationRow expandableNotificationRow) {
        this.mAmbientState.setExpandingNotification(expandableNotificationRow);
        requestChildrenUpdate();
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void bindRow(ExpandableNotificationRow expandableNotificationRow) {
        expandableNotificationRow.setHeadsUpAnimatingAwayListener(new Consumer(expandableNotificationRow) {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$IXSbC_TGjFKfhLk45pHR07QQ7PA */
            public final /* synthetic */ ExpandableNotificationRow f$1;

            {
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                NotificationStackScrollLayout.this.lambda$bindRow$8$NotificationStackScrollLayout(this.f$1, (Boolean) obj);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindRow$8 */
    public /* synthetic */ void lambda$bindRow$8$NotificationStackScrollLayout(ExpandableNotificationRow expandableNotificationRow, Boolean bool) {
        this.mRoundnessManager.onHeadsupAnimatingAwayChanged(expandableNotificationRow, bool.booleanValue());
        this.mHeadsUpAppearanceController.lambda$updateHeadsUpHeaders$3(expandableNotificationRow.getEntry());
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public boolean containsView(View view) {
        return view.getParent() == this;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void applyExpandAnimationParams(ActivityLaunchAnimator.ExpandAnimationParameters expandAnimationParameters) {
        this.mAmbientState.setExpandAnimationTopChange(expandAnimationParameters == null ? 0 : expandAnimationParameters.getTopChange());
        requestChildrenUpdate();
    }

    private void updateAnimationState(boolean z, View view) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).setIconAnimationRunning(z);
        }
    }

    public boolean isAddOrRemoveAnimationPending() {
        return this.mNeedsAnimation && (!this.mChildrenToAddAnimated.isEmpty() || !this.mChildrenToRemoveAnimated.isEmpty());
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void generateAddAnimation(ExpandableView expandableView, boolean z) {
        if (this.mIsExpanded && this.mAnimationsEnabled && !this.mChangePositionInProgress && !isFullyHidden()) {
            this.mChildrenToAddAnimated.add(expandableView);
            if (z) {
                this.mFromMoreCardAdditions.add(expandableView);
            }
            this.mNeedsAnimation = true;
        }
        if (isHeadsUp(expandableView) && this.mAnimationsEnabled && !this.mChangePositionInProgress && !isFullyHidden()) {
            this.mAddedHeadsUpChildren.add(expandableView);
            this.mChildrenToAddAnimated.remove(expandableView);
        }
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void changeViewPosition(ExpandableView expandableView, int i) {
        Assert.isMainThread();
        if (!this.mChangePositionInProgress) {
            int indexOfChild = indexOfChild(expandableView);
            boolean z = false;
            if (indexOfChild == -1) {
                if ((expandableView instanceof ExpandableNotificationRow) && expandableView.getTransientContainer() != null) {
                    z = true;
                }
                StringBuilder sb = new StringBuilder();
                sb.append("Attempting to re-position ");
                sb.append(z ? "transient" : "");
                sb.append(" view {");
                sb.append(expandableView);
                sb.append("}");
                Log.e("StackScroller", sb.toString());
            } else if (expandableView != null && expandableView.getParent() == this && indexOfChild != i) {
                this.mChangePositionInProgress = true;
                expandableView.setChangingPosition(true);
                removeView(expandableView);
                addView(expandableView, i);
                expandableView.setChangingPosition(false);
                this.mChangePositionInProgress = false;
                if (this.mIsExpanded && this.mAnimationsEnabled && expandableView.getVisibility() != 8) {
                    this.mChildrenChangingPositions.add(expandableView);
                    this.mNeedsAnimation = true;
                }
            }
        } else {
            throw new IllegalStateException("Reentrant call to changeViewPosition");
        }
    }

    private void startAnimationToState() {
        if (this.mNeedsAnimation) {
            generateAllAnimationEvents();
            this.mNeedsAnimation = false;
        }
        if (!this.mAnimationEvents.isEmpty() || isCurrentlyAnimating()) {
            setAnimationRunning(true);
            this.mStateAnimator.startAnimationForEvents(this.mAnimationEvents, this.mGoToFullShadeDelay);
            this.mAnimationEvents.clear();
            updateBackground();
            updateViewShadows();
            updateClippingToTopRoundedCorner();
        } else {
            applyCurrentState();
        }
        this.mGoToFullShadeDelay = 0;
    }

    private void generateAllAnimationEvents() {
        generateHeadsUpAnimationEvents();
        generateChildRemovalEvents();
        generateChildAdditionEvents();
        generatePositionChangeEvents();
        generateTopPaddingEvent();
        generateActivateEvent();
        generateDimmedEvent();
        generateHideSensitiveEvent();
        generateGoToFullShadeEvent();
        generateViewResizeEvent();
        generateGroupExpansionEvent();
        generateAnimateEverythingEvent();
    }

    private void generateHeadsUpAnimationEvents() {
        Iterator<Pair<ExpandableNotificationRow, Boolean>> it = this.mHeadsUpChangeAnimations.iterator();
        while (it.hasNext()) {
            Pair<ExpandableNotificationRow, Boolean> next = it.next();
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) next.first;
            boolean booleanValue = ((Boolean) next.second).booleanValue();
            if (booleanValue == expandableNotificationRow.isHeadsUp()) {
                int i = 14;
                boolean z = true;
                boolean z2 = false;
                boolean z3 = expandableNotificationRow.isPinned() && !this.mIsExpanded;
                if (this.mIsExpanded && (!this.mKeyguardBypassController.getBypassEnabled() || !onKeyguard() || !this.mHeadsUpManager.hasPinnedHeadsUp())) {
                    z = false;
                }
                if (!z || booleanValue) {
                    ExpandableViewState viewState = expandableNotificationRow.getViewState();
                    if (viewState != null) {
                        if (booleanValue && (this.mAddedHeadsUpChildren.contains(expandableNotificationRow) || z3)) {
                            i = (z3 || shouldHunAppearFromBottom(viewState)) ? 11 : 0;
                            z2 = !z3;
                        }
                    }
                } else {
                    i = expandableNotificationRow.wasJustClicked() ? 13 : 12;
                    if (expandableNotificationRow.isChildInGroup()) {
                        expandableNotificationRow.setHeadsUpAnimatingAway(false);
                    }
                }
                AnimationEvent animationEvent = new AnimationEvent(expandableNotificationRow, i);
                animationEvent.headsUpFromBottom = z2;
                this.mAnimationEvents.add(animationEvent);
            }
        }
        this.mHeadsUpChangeAnimations.clear();
        this.mAddedHeadsUpChildren.clear();
    }

    private boolean shouldHunAppearFromBottom(ExpandableViewState expandableViewState) {
        return expandableViewState.yTranslation + ((float) expandableViewState.height) >= this.mAmbientState.getMaxHeadsUpTranslation();
    }

    private void generateGroupExpansionEvent() {
        if (this.mExpandedGroupView != null) {
            if (NotificationSettingsHelper.showGoogleStyle()) {
                this.mAnimationEvents.add(new AnimationEvent(this.mExpandedGroupView, 10));
            } else if (this.mGroupExpanded) {
                this.mAnimationEvents.add(new GroupExpandingEvent(this.mExpandedGroupView));
            } else {
                this.mAnimationEvents.add(new GroupCollapseEvent(this.mExpandedGroupView));
            }
            this.mExpandedGroupView = null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0023 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:5:0x0011  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void generateViewResizeEvent() {
        /*
            r5 = this;
            boolean r0 = r5.mNeedViewResizeAnimation
            r1 = 0
            if (r0 == 0) goto L_0x0033
            java.util.ArrayList<com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$AnimationEvent> r0 = r5.mAnimationEvents
            java.util.Iterator r0 = r0.iterator()
        L_0x000b:
            boolean r2 = r0.hasNext()
            if (r2 == 0) goto L_0x0023
            java.lang.Object r2 = r0.next()
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$AnimationEvent r2 = (com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.AnimationEvent) r2
            int r2 = r2.animationType
            r3 = 13
            if (r2 == r3) goto L_0x0021
            r3 = 12
            if (r2 != r3) goto L_0x000b
        L_0x0021:
            r0 = 1
            goto L_0x0024
        L_0x0023:
            r0 = r1
        L_0x0024:
            if (r0 != 0) goto L_0x0033
            java.util.ArrayList<com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$AnimationEvent> r0 = r5.mAnimationEvents
            com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$AnimationEvent r2 = new com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout$AnimationEvent
            r3 = 0
            r4 = 9
            r2.<init>(r3, r4)
            r0.add(r2)
        L_0x0033:
            r5.mNeedViewResizeAnimation = r1
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.generateViewResizeEvent():void");
    }

    private void generateChildRemovalEvents() {
        boolean z;
        ViewGroup transientContainer;
        Iterator<ExpandableView> it = this.mChildrenToRemoveAnimated.iterator();
        while (it.hasNext()) {
            ExpandableView next = it.next();
            boolean contains = this.mSwipedOutViews.contains(next);
            float translationY = next.getTranslationY();
            boolean z2 = false;
            int i = 1;
            if (next instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) next;
                if (!expandableNotificationRow.isRemoved() || !expandableNotificationRow.wasChildInGroupWhenRemoved()) {
                    z = true;
                } else {
                    translationY = expandableNotificationRow.getTranslationWhenRemoved();
                    z = false;
                }
                contains |= Math.abs(expandableNotificationRow.getTranslation()) == ((float) expandableNotificationRow.getWidth());
            } else if (next instanceof MediaHeaderView) {
                contains = true;
                z = true;
            } else {
                z = true;
            }
            if (!contains) {
                Rect clipBounds = next.getClipBounds();
                if (clipBounds != null && clipBounds.height() == 0) {
                    z2 = true;
                }
                if (z2 && (next instanceof ExpandableView) && (transientContainer = next.getTransientContainer()) != null) {
                    transientContainer.removeTransientView(next);
                }
                contains = z2;
            }
            if (contains) {
                i = 2;
            }
            AnimationEvent animationEvent = new AnimationEvent(next, i);
            animationEvent.viewAfterChangingView = getFirstChildBelowTranlsationY(translationY, z);
            this.mAnimationEvents.add(animationEvent);
            this.mSwipedOutViews.remove(next);
        }
        this.mChildrenToRemoveAnimated.clear();
    }

    private void generatePositionChangeEvents() {
        AnimationEvent animationEvent;
        Iterator<ExpandableView> it = this.mChildrenChangingPositions.iterator();
        while (true) {
            Integer num = null;
            if (!it.hasNext()) {
                break;
            }
            ExpandableView next = it.next();
            if (next instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) next;
                if (expandableNotificationRow.getEntry().isMarkedForUserTriggeredMovement()) {
                    num = 500;
                    expandableNotificationRow.getEntry().markForUserTriggeredMovement(false);
                }
            }
            if (num == null) {
                animationEvent = new AnimationEvent(next, 6);
            } else {
                animationEvent = new AnimationEvent(next, 6, (long) num.intValue());
            }
            this.mAnimationEvents.add(animationEvent);
        }
        this.mChildrenChangingPositions.clear();
        if (this.mGenerateChildOrderChangedEvent) {
            this.mAnimationEvents.add(new AnimationEvent(null, 6));
            this.mGenerateChildOrderChangedEvent = false;
        }
    }

    private void generateChildAdditionEvents() {
        Iterator<ExpandableView> it = this.mChildrenToAddAnimated.iterator();
        while (it.hasNext()) {
            ExpandableView next = it.next();
            if (this.mFromMoreCardAdditions.contains(next)) {
                this.mAnimationEvents.add(new AnimationEvent(next, 0, 300));
            } else {
                this.mAnimationEvents.add(new AnimationEvent(next, 0));
            }
        }
        this.mChildrenToAddAnimated.clear();
        this.mFromMoreCardAdditions.clear();
    }

    private void generateTopPaddingEvent() {
        AnimationEvent animationEvent;
        if (this.mTopPaddingNeedsAnimation) {
            if (this.mAmbientState.isDozing()) {
                animationEvent = new AnimationEvent(null, 3, 550);
            } else {
                animationEvent = new AnimationEvent(null, 3);
            }
            this.mAnimationEvents.add(animationEvent);
        }
        this.mTopPaddingNeedsAnimation = false;
    }

    private void generateActivateEvent() {
        if (this.mActivateNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 4));
        }
        this.mActivateNeedsAnimation = false;
    }

    private void generateAnimateEverythingEvent() {
        if (this.mEverythingNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 15));
        }
        this.mEverythingNeedsAnimation = false;
    }

    private void generateDimmedEvent() {
        if (this.mDimmedNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 5));
        }
        this.mDimmedNeedsAnimation = false;
    }

    private void generateHideSensitiveEvent() {
        if (this.mHideSensitiveNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 8));
        }
        this.mHideSensitiveNeedsAnimation = false;
    }

    private void generateGoToFullShadeEvent() {
        if (this.mGoToFullShadeNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent(null, 7));
        }
        this.mGoToFullShadeNeedsAnimation = false;
    }

    /* access modifiers changed from: protected */
    public StackScrollAlgorithm createStackScrollAlgorithm(Context context) {
        return new MiuiStackScrollAlgorithm(context, this);
    }

    public boolean isInContentBounds(float f) {
        return f < ((float) (getHeight() - getEmptyBottomMargin()));
    }

    public void setLongPressListener(ExpandableNotificationRow.LongPressListener longPressListener) {
        this.mLongPressListener = longPressListener;
    }

    private float getTouchSlop(MotionEvent motionEvent) {
        if (motionEvent.getClassification() == 1) {
            return ((float) this.mTouchSlop) * this.mSlopMultiplier;
        }
        return (float) this.mTouchSlop;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z;
        NotificationGuts exposedGuts = this.mNotificationGutsManager.getExposedGuts();
        boolean z2 = motionEvent.getActionMasked() == 3 || motionEvent.getActionMasked() == 1;
        handleEmptySpaceClick(motionEvent);
        boolean z3 = this.mSwipingInProgress;
        if (!this.mIsExpanded || z3 || this.mOnlyScrollingInThisMotion || exposedGuts != null) {
            z = false;
        } else {
            if (z2) {
                this.mExpandHelper.onlyObserveMovements(false);
            }
            boolean z4 = this.mExpandingNotification;
            z = this.mExpandHelper.onTouchEvent(motionEvent);
            if (this.mExpandedInThisMotion && !this.mExpandingNotification && z4 && !this.mDisallowScrollingInThisMotion) {
                dispatchDownEventToScroller(motionEvent);
            }
        }
        boolean onScrollTouch = (!this.mIsExpanded || z3 || this.mExpandingNotification || this.mDisallowScrollingInThisMotion) ? false : onScrollTouch(motionEvent);
        boolean onTouchEvent = (this.mIsBeingDragged || this.mExpandingNotification || this.mExpandedInThisMotion || this.mOnlyScrollingInThisMotion || this.mDisallowDismissInThisMotion) ? false : this.mSwipeHelper.onTouchEvent(motionEvent);
        if (exposedGuts != null && !NotificationSwipeHelper.isTouchInView(motionEvent, exposedGuts) && (exposedGuts.getGutsContent() instanceof NotificationSnooze) && ((((NotificationSnooze) exposedGuts.getGutsContent()).isExpanded() && z2) || (!onTouchEvent && onScrollTouch))) {
            checkSnoozeLeavebehind();
        }
        if (motionEvent.getActionMasked() == 1) {
            this.mCheckForLeavebehind = true;
        }
        return onTouchEvent || onScrollTouch || z || super.onTouchEvent(motionEvent);
    }

    private void dispatchDownEventToScroller(MotionEvent motionEvent) {
        MotionEvent obtain = MotionEvent.obtain(motionEvent);
        obtain.setAction(0);
        onScrollTouch(obtain);
        obtain.recycle();
    }

    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        int i = 0;
        if (!isScrollingEnabled() || !this.mIsExpanded || this.mSwipingInProgress || this.mExpandingNotification || this.mDisallowScrollingInThisMotion) {
            return false;
        }
        if ((motionEvent.getSource() & 2) != 0 && motionEvent.getAction() == 8 && !this.mIsBeingDragged) {
            float axisValue = motionEvent.getAxisValue(9);
            if (axisValue != 0.0f) {
                int scrollRange = getScrollRange();
                int i2 = this.mOwnScrollY;
                int verticalScrollFactor = i2 - ((int) (axisValue * getVerticalScrollFactor()));
                if (verticalScrollFactor >= 0) {
                    i = verticalScrollFactor > scrollRange ? scrollRange : verticalScrollFactor;
                }
                if (i != i2) {
                    setOwnScrollY(i);
                    return true;
                }
            }
        }
        return super.onGenericMotionEvent(motionEvent);
    }

    private boolean onScrollTouch(MotionEvent motionEvent) {
        float f;
        if (!isScrollingEnabled()) {
            return false;
        }
        if (isInsideQsContainer(motionEvent) && !this.mIsBeingDragged) {
            return false;
        }
        this.mForcedScroll = null;
        initVelocityTrackerIfNotExists();
        this.mVelocityTracker.addMovement(motionEvent);
        int actionMasked = motionEvent.getActionMasked();
        if (motionEvent.findPointerIndex(this.mActivePointerId) != -1 || actionMasked == 0) {
            if (actionMasked != 0) {
                if (actionMasked != 1) {
                    if (actionMasked == 2) {
                        int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                        if (findPointerIndex == -1) {
                            Log.e("StackScroller", "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                        } else {
                            int y = (int) motionEvent.getY(findPointerIndex);
                            int i = this.mLastMotionY - y;
                            int abs = Math.abs(((int) motionEvent.getX(findPointerIndex)) - this.mDownX);
                            int abs2 = Math.abs(i);
                            float touchSlop = getTouchSlop(motionEvent);
                            if (!this.mIsBeingDragged && ((float) abs2) > touchSlop && abs2 > abs) {
                                setIsBeingDragged(true);
                                i = (int) (i > 0 ? ((float) i) - touchSlop : ((float) i) + touchSlop);
                            }
                            if (this.mIsBeingDragged) {
                                this.mLastMotionY = y;
                                int scrollRange = getScrollRange();
                                if (this.mExpandedInThisMotion) {
                                    scrollRange = Math.min(scrollRange, this.mMaxScrollAfterExpand);
                                }
                                if (i < 0) {
                                    f = overScrollDown(i);
                                } else {
                                    f = overScrollUp(i, scrollRange);
                                }
                                if (f != 0.0f) {
                                    customOverScrollBy((int) f, this.mOwnScrollY, scrollRange, getHeight() / 2);
                                    checkSnoozeLeavebehind();
                                }
                                if (this.mIsTrackingSliding) {
                                    if (i < 0) {
                                        trackPanelSliding(PanelSlidingDirection.DOWN);
                                    } else {
                                        trackPanelSliding(PanelSlidingDirection.UP);
                                    }
                                }
                            }
                        }
                    } else if (actionMasked != 3) {
                        if (actionMasked == 5) {
                            int actionIndex = motionEvent.getActionIndex();
                            this.mLastMotionY = (int) motionEvent.getY(actionIndex);
                            this.mDownX = (int) motionEvent.getX(actionIndex);
                            this.mActivePointerId = motionEvent.getPointerId(actionIndex);
                        } else if (actionMasked == 6) {
                            onSecondaryPointerUp(motionEvent);
                            this.mLastMotionY = (int) motionEvent.getY(motionEvent.findPointerIndex(this.mActivePointerId));
                            this.mDownX = (int) motionEvent.getX(motionEvent.findPointerIndex(this.mActivePointerId));
                        }
                    } else if (this.mIsBeingDragged && getChildCount() > 0) {
                        if (this.mScroller.springBack(((ViewGroup) this).mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                            lambda$new$1();
                        }
                        this.mActivePointerId = -1;
                        endDrag();
                    }
                } else if (this.mIsBeingDragged) {
                    VelocityTracker velocityTracker = this.mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                    int yVelocity = (int) velocityTracker.getYVelocity(this.mActivePointerId);
                    if (shouldOverScrollFling(yVelocity)) {
                        onOverScrollFling(true, yVelocity);
                    } else if (getChildCount() > 0) {
                        if (Math.abs(yVelocity) > this.mMinimumVelocity) {
                            if (getCurrentOverScrollAmount(true) == 0.0f || yVelocity > 0) {
                                fling(-yVelocity);
                            } else {
                                onOverScrollFling(false, yVelocity);
                            }
                        } else if (this.mScroller.springBack(((ViewGroup) this).mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                            lambda$new$1();
                        }
                    }
                    this.mActivePointerId = -1;
                    endDrag();
                }
            } else if (getChildCount() == 0 || !isInContentBounds(motionEvent)) {
                return false;
            } else {
                setIsBeingDragged(!this.mScroller.isFinished());
                if (!this.mScroller.isFinished()) {
                    this.mScroller.forceFinished(true);
                }
                this.mLastMotionY = (int) motionEvent.getY();
                this.mDownX = (int) motionEvent.getX();
                this.mActivePointerId = motionEvent.getPointerId(0);
            }
            return true;
        }
        Log.e("StackScroller", "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent " + MotionEvent.actionToString(motionEvent.getActionMasked()));
        return true;
    }

    private void trackPanelSliding(PanelSlidingDirection panelSlidingDirection) {
        ((NotificationStat) Dependency.get(NotificationStat.class)).onNotificationPanelSliding(panelSlidingDirection.name(), "panel");
        stopTrackingScrolling();
    }

    private void startTrackingScrolling() {
        this.mIsTrackingSliding = true;
    }

    private void stopTrackingScrolling() {
        this.mIsTrackingSliding = false;
    }

    /* access modifiers changed from: protected */
    public boolean isInsideQsContainer(MotionEvent motionEvent) {
        return motionEvent.getY() < ((float) this.mQsContainer.getBottom());
    }

    private void onOverScrollFling(boolean z, int i) {
        OnOverscrollTopChangedListener onOverscrollTopChangedListener = this.mOverscrollTopChangedListener;
        if (onOverscrollTopChangedListener != null) {
            onOverscrollTopChangedListener.flingTopOverscroll((float) i, z);
        }
        this.mDontReportNextOverScroll = true;
        setOverScrollAmount(0.0f, true, false);
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int action = (motionEvent.getAction() & 65280) >> 8;
        if (motionEvent.getPointerId(action) == this.mActivePointerId) {
            int i = action == 0 ? 1 : 0;
            this.mLastMotionY = (int) motionEvent.getY(i);
            this.mActivePointerId = motionEvent.getPointerId(i);
            VelocityTracker velocityTracker = this.mVelocityTracker;
            if (velocityTracker != null) {
                velocityTracker.clear();
            }
        }
    }

    private void endDrag() {
        setIsBeingDragged(false);
        recycleVelocityTracker();
        if (getCurrentOverScrollAmount(true) > 0.0f) {
            setOverScrollAmount(0.0f, true, true);
        }
        if (getCurrentOverScrollAmount(false) > 0.0f) {
            setOverScrollAmount(0.0f, false, true);
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        initDownStates(motionEvent);
        handleEmptySpaceClick(motionEvent);
        NotificationGuts exposedGuts = this.mNotificationGutsManager.getExposedGuts();
        boolean z = this.mSwipingInProgress;
        boolean onInterceptTouchEvent = (z || this.mOnlyScrollingInThisMotion || exposedGuts != null) ? false : this.mExpandHelper.onInterceptTouchEvent(motionEvent);
        boolean onInterceptTouchEventScroll = (z || this.mExpandingNotification) ? false : onInterceptTouchEventScroll(motionEvent);
        boolean onInterceptTouchEvent2 = (this.mIsBeingDragged || this.mExpandingNotification || this.mExpandedInThisMotion || this.mOnlyScrollingInThisMotion || this.mDisallowDismissInThisMotion) ? false : this.mSwipeHelper.onInterceptTouchEvent(motionEvent);
        boolean z2 = motionEvent.getActionMasked() == 1;
        if (!NotificationSwipeHelper.isTouchInView(motionEvent, exposedGuts) && z2 && !onInterceptTouchEvent2 && !onInterceptTouchEvent && !onInterceptTouchEventScroll) {
            this.mCheckForLeavebehind = false;
            this.mNotificationGutsManager.closeAndSaveGuts(true, false, false, -1, -1, false);
        }
        if (motionEvent.getActionMasked() == 1) {
            this.mCheckForLeavebehind = true;
        }
        if (onInterceptTouchEvent2 || onInterceptTouchEventScroll || onInterceptTouchEvent || super.onInterceptTouchEvent(motionEvent)) {
            return true;
        }
        return false;
    }

    private void handleEmptySpaceClick(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 1) {
            if (actionMasked == 2) {
                float touchSlop = getTouchSlop(motionEvent);
                if (!this.mTouchIsClick) {
                    return;
                }
                if (Math.abs(motionEvent.getY() - this.mInitialTouchY) > touchSlop || Math.abs(motionEvent.getX() - this.mInitialTouchX) > touchSlop) {
                    this.mTouchIsClick = false;
                }
            }
        } else if (this.mStatusBarState != 1 && this.mTouchIsClick && isBelowLastNotification(this.mInitialTouchX, this.mInitialTouchY)) {
            this.mOnEmptySpaceClickListener.onEmptySpaceClicked(this.mInitialTouchX, this.mInitialTouchY);
        }
    }

    private void initDownStates(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            this.mExpandedInThisMotion = false;
            this.mOnlyScrollingInThisMotion = !this.mScroller.isFinished();
            this.mDisallowScrollingInThisMotion = false;
            this.mDisallowDismissInThisMotion = false;
            this.mTouchIsClick = true;
            this.mInitialTouchX = motionEvent.getX();
            this.mInitialTouchY = motionEvent.getY();
        }
    }

    public void requestDisallowInterceptTouchEvent(boolean z) {
        super.requestDisallowInterceptTouchEvent(z);
        if (z) {
            cancelLongPress();
        }
    }

    private boolean onInterceptTouchEventScroll(MotionEvent motionEvent) {
        if (!isScrollingEnabled()) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 2 && this.mIsBeingDragged) {
            return true;
        }
        int i = action & 255;
        if (i != 0) {
            if (i != 1) {
                if (i == 2) {
                    int i2 = this.mActivePointerId;
                    if (i2 != -1) {
                        int findPointerIndex = motionEvent.findPointerIndex(i2);
                        if (findPointerIndex == -1) {
                            Log.e("StackScroller", "Invalid pointerId=" + i2 + " in onInterceptTouchEvent");
                        } else {
                            int y = (int) motionEvent.getY(findPointerIndex);
                            int x = (int) motionEvent.getX(findPointerIndex);
                            int abs = Math.abs(y - this.mLastMotionY);
                            int abs2 = Math.abs(x - this.mDownX);
                            if (((float) abs) > getTouchSlop(motionEvent) && abs > abs2) {
                                setIsBeingDragged(true);
                                this.mLastMotionY = y;
                                this.mDownX = x;
                                initVelocityTrackerIfNotExists();
                                this.mVelocityTracker.addMovement(motionEvent);
                            }
                        }
                    }
                } else if (i != 3) {
                    if (i == 6) {
                        onSecondaryPointerUp(motionEvent);
                    }
                }
            }
            setIsBeingDragged(false);
            this.mActivePointerId = -1;
            recycleVelocityTracker();
            if (this.mScroller.springBack(((ViewGroup) this).mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                lambda$new$1();
            }
        } else {
            int y2 = (int) motionEvent.getY();
            this.mScrolledToTopOnFirstDown = isScrolledToTop();
            if (getChildAtPosition(motionEvent.getX(), (float) y2, false, false) == null) {
                setIsBeingDragged(false);
                recycleVelocityTracker();
            } else {
                this.mLastMotionY = y2;
                this.mDownX = (int) motionEvent.getX();
                this.mActivePointerId = motionEvent.getPointerId(0);
                initOrResetVelocityTracker();
                this.mVelocityTracker.addMovement(motionEvent);
                setIsBeingDragged(!this.mScroller.isFinished());
            }
        }
        return this.mIsBeingDragged;
    }

    private boolean isInContentBounds(MotionEvent motionEvent) {
        return isInContentBounds(motionEvent.getY());
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setIsBeingDragged(boolean z) {
        this.mIsBeingDragged = z;
        if (z) {
            requestDisallowInterceptTouchEvent(true);
            cancelLongPress();
            resetExposedMenuView(true, true);
            return;
        }
        startTrackingScrolling();
    }

    public void requestDisallowLongPress() {
        cancelLongPress();
    }

    public void requestDisallowDismiss() {
        this.mDisallowDismissInThisMotion = true;
    }

    public void cancelLongPress() {
        this.mSwipeHelper.cancelLongPress();
    }

    public void setOnEmptySpaceClickListener(OnEmptySpaceClickListener onEmptySpaceClickListener) {
        this.mOnEmptySpaceClickListener = onEmptySpaceClickListener;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0021, code lost:
        if (r5 != 16908346) goto L_0x0059;
     */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x004d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean performAccessibilityActionInternal(int r5, android.os.Bundle r6) {
        /*
            r4 = this;
            boolean r6 = super.performAccessibilityActionInternal(r5, r6)
            r0 = 1
            if (r6 == 0) goto L_0x0008
            return r0
        L_0x0008:
            boolean r6 = r4.isEnabled()
            r1 = 0
            if (r6 != 0) goto L_0x0010
            return r1
        L_0x0010:
            r6 = -1
            r2 = 4096(0x1000, float:5.74E-42)
            if (r5 == r2) goto L_0x0024
            r2 = 8192(0x2000, float:1.14794E-41)
            if (r5 == r2) goto L_0x0025
            r2 = 16908344(0x1020038, float:2.3877386E-38)
            if (r5 == r2) goto L_0x0025
            r6 = 16908346(0x102003a, float:2.3877392E-38)
            if (r5 == r6) goto L_0x0024
            goto L_0x0059
        L_0x0024:
            r6 = r0
        L_0x0025:
            int r5 = r4.getHeight()
            int r2 = r4.mPaddingBottom
            int r5 = r5 - r2
            int r2 = r4.mTopPadding
            int r5 = r5 - r2
            int r2 = r4.mPaddingTop
            int r5 = r5 - r2
            com.android.systemui.statusbar.NotificationShelf r2 = r4.mShelf
            int r2 = r2.getIntrinsicHeight()
            int r5 = r5 - r2
            int r2 = r4.mOwnScrollY
            int r6 = r6 * r5
            int r2 = r2 + r6
            int r5 = r4.getScrollRange()
            int r5 = java.lang.Math.min(r2, r5)
            int r5 = java.lang.Math.max(r1, r5)
            int r6 = r4.mOwnScrollY
            if (r5 == r6) goto L_0x0059
            android.widget.OverScroller r2 = r4.mScroller
            int r3 = r4.mScrollX
            int r5 = r5 - r6
            r2.startScroll(r3, r6, r1, r5)
            r4.lambda$new$1()
            return r0
        L_0x0059:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.performAccessibilityActionInternal(int, android.os.Bundle):boolean");
    }

    public void closeControlsIfOutsideTouch(MotionEvent motionEvent) {
        NotificationGuts exposedGuts = this.mNotificationGutsManager.getExposedGuts();
        NotificationMenuRowPlugin currentMenuRow = this.mSwipeHelper.getCurrentMenuRow();
        View translatingParentView = this.mSwipeHelper.getTranslatingParentView();
        if (exposedGuts == null || exposedGuts.getGutsContent().isLeavebehind()) {
            exposedGuts = (currentMenuRow == null || !currentMenuRow.isMenuVisible() || translatingParentView == null) ? null : translatingParentView;
        }
        if (exposedGuts != null && !NotificationSwipeHelper.isTouchInView(motionEvent, exposedGuts)) {
            this.mNotificationGutsManager.closeAndSaveGuts(false, false, true, -1, -1, false);
            resetExposedMenuView(true, true);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setSwipingInProgress(boolean z) {
        this.mSwipingInProgress = z;
        if (z) {
            requestDisallowInterceptTouchEvent(true);
        }
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (!z) {
            cancelLongPress();
        }
    }

    public void clearChildFocus(View view) {
        super.clearChildFocus(view);
        if (this.mForcedScroll == view) {
            this.mForcedScroll = null;
        }
    }

    @Override // com.android.systemui.statusbar.policy.ScrollAdapter
    public boolean isScrolledToTop() {
        return this.mOwnScrollY == 0;
    }

    public boolean isScrolledToBottom() {
        return this.mOwnScrollY >= getScrollRange();
    }

    public int getEmptyBottomMargin() {
        return Math.max(this.mMaxLayoutHeight - this.mContentHeight, 0);
    }

    public void checkSnoozeLeavebehind() {
        if (this.mCheckForLeavebehind) {
            this.mNotificationGutsManager.closeAndSaveGuts(true, false, false, -1, -1, false);
            this.mCheckForLeavebehind = false;
        }
    }

    public void resetCheckSnoozeLeavebehind() {
        this.mCheckForLeavebehind = true;
    }

    public void onExpansionStarted() {
        this.mIsExpansionChanging = true;
        this.mAmbientState.setExpansionChanging(true);
        checkSnoozeLeavebehind();
    }

    public void onExpansionStopped() {
        this.mIsExpansionChanging = false;
        resetCheckSnoozeLeavebehind();
        this.mAmbientState.setExpansionChanging(false);
        if (!this.mIsExpanded) {
            resetScrollPosition();
            this.mStatusBar.resetUserExpandedStates();
            clearTemporaryViews();
            clearUserLockedViews();
            ArrayList<ExpandableView> draggedViews = this.mAmbientState.getDraggedViews();
            if (draggedViews.size() > 0) {
                draggedViews.clear();
                updateContinuousShadowDrawing();
            }
        }
    }

    private void clearUserLockedViews() {
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) expandableView).setUserLocked(false);
            }
        }
    }

    private void clearTemporaryViews() {
        clearTemporaryViewsInGroup(this);
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView instanceof ExpandableNotificationRow) {
                clearTemporaryViewsInGroup(((ExpandableNotificationRow) expandableView).getChildrenContainer());
            }
        }
    }

    private void clearTemporaryViewsInGroup(ViewGroup viewGroup) {
        while (viewGroup != null && viewGroup.getTransientViewCount() != 0) {
            viewGroup.removeTransientView(viewGroup.getTransientView(0));
        }
    }

    public void onPanelTrackingStarted() {
        this.mPanelTracking = true;
        this.mAmbientState.setPanelTracking(true);
        resetExposedMenuView(true, true);
    }

    public void onPanelTrackingStopped() {
        this.mPanelTracking = false;
        this.mAmbientState.setPanelTracking(false);
    }

    public void resetScrollPosition() {
        this.mScroller.abortAnimation();
        setOwnScrollY(0);
    }

    private void setIsExpanded(boolean z) {
        boolean z2 = z != this.mIsExpanded;
        this.mIsExpanded = z;
        this.mStackScrollAlgorithm.setIsExpanded(z);
        this.mAmbientState.setShadeExpanded(z);
        this.mStateAnimator.setShadeExpanded(z);
        this.mSwipeHelper.setIsExpanded(z);
        if (z2) {
            this.mWillExpand = false;
            if (!this.mIsExpanded) {
                this.mGroupManager.collapseAllGroups();
                this.mExpandHelper.cancelImmediately();
            }
            updateNotificationAnimationStates();
            updateChronometers();
            requestChildrenUpdate();
        }
    }

    private void updateChronometers() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            updateChronometerForChild(getChildAt(i));
        }
    }

    private void updateChronometerForChild(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).setChronometerRunning(this.mIsExpanded);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
    public void onHeightChanged(ExpandableView expandableView, boolean z) {
        updateContentHeight();
        updateScrollPositionOnExpandInBottom(expandableView);
        clampScrollPosition();
        notifyHeightChangeListener(expandableView, z);
        ExpandableView expandableView2 = null;
        ExpandableNotificationRow expandableNotificationRow = expandableView instanceof ExpandableNotificationRow ? (ExpandableNotificationRow) expandableView : null;
        NotificationSection firstVisibleSection = getFirstVisibleSection();
        if (firstVisibleSection != null) {
            expandableView2 = firstVisibleSection.getFirstVisibleChild();
        }
        if (expandableNotificationRow != null && (expandableNotificationRow == expandableView2 || expandableNotificationRow.getNotificationParent() == expandableView2)) {
            updateAlgorithmLayoutMinHeight();
        }
        if (z) {
            requestAnimationOnViewResize(expandableNotificationRow);
        }
        requestChildrenUpdate();
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableView.OnHeightChangedListener
    public void onReset(ExpandableView expandableView) {
        updateAnimationState(expandableView);
        updateChronometerForChild(expandableView);
    }

    private void updateScrollPositionOnExpandInBottom(ExpandableView expandableView) {
        ExpandableView expandableView2;
        if ((expandableView instanceof ExpandableNotificationRow) && !onKeyguard()) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
            if (expandableNotificationRow.isUserLocked() && expandableNotificationRow != getFirstChildNotGone() && !expandableNotificationRow.isSummaryWithChildren()) {
                float translationY = expandableNotificationRow.getTranslationY() + ((float) expandableNotificationRow.getActualHeight());
                if (expandableNotificationRow.isChildInGroup()) {
                    translationY += expandableNotificationRow.getNotificationParent().getTranslationY();
                }
                int i = this.mMaxLayoutHeight + ((int) this.mStackTranslation);
                NotificationSection lastVisibleSection = getLastVisibleSection();
                if (lastVisibleSection == null) {
                    expandableView2 = null;
                } else {
                    expandableView2 = lastVisibleSection.getLastVisibleChild();
                }
                if (!(expandableNotificationRow == expandableView2 || this.mShelf.getVisibility() == 8)) {
                    i -= this.mShelf.getIntrinsicHeight() + this.mPaddingBetweenElements;
                }
                float f = (float) i;
                if (translationY > f) {
                    setOwnScrollY((int) ((((float) this.mOwnScrollY) + translationY) - f));
                    this.mDisallowScrollingInThisMotion = true;
                }
            }
        }
    }

    public void setOnHeightChangedListener(ExpandableView.OnHeightChangedListener onHeightChangedListener) {
        this.mOnHeightChangedListener = onHeightChangedListener;
    }

    public void onChildAnimationFinished() {
        setAnimationRunning(false);
        requestChildrenUpdate();
        runAnimationFinishedRunnables();
        clearTransient();
        clearHeadsUpDisappearRunning();
    }

    private void clearHeadsUpDisappearRunning() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                expandableNotificationRow.setHeadsUpAnimatingAway(false);
                if (expandableNotificationRow.isSummaryWithChildren()) {
                    for (ExpandableNotificationRow expandableNotificationRow2 : expandableNotificationRow.getAttachedChildren()) {
                        expandableNotificationRow2.setHeadsUpAnimatingAway(false);
                    }
                }
            }
        }
    }

    private void clearTransient() {
        Iterator<ExpandableView> it = this.mClearTransientViewsWhenFinished.iterator();
        while (it.hasNext()) {
            StackStateAnimator.removeTransientView(it.next());
        }
        this.mClearTransientViewsWhenFinished.clear();
    }

    private void runAnimationFinishedRunnables() {
        Iterator<Runnable> it = this.mAnimationFinishedRunnables.iterator();
        while (it.hasNext()) {
            it.next().run();
        }
        this.mAnimationFinishedRunnables.clear();
    }

    public void setDimmed(boolean z, boolean z2) {
        boolean onKeyguard = z & onKeyguard();
        this.mAmbientState.setDimmed(onKeyguard);
        if (!z2 || !this.mAnimationsEnabled) {
            setDimAmount(onKeyguard ? 1.0f : 0.0f);
        } else {
            this.mDimmedNeedsAnimation = true;
            this.mNeedsAnimation = true;
            animateDimmed(onKeyguard);
        }
        requestChildrenUpdate();
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isDimmed() {
        return this.mAmbientState.isDimmed();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setDimAmount(float f) {
        this.mDimAmount = f;
        updateBackgroundDimming();
    }

    private void animateDimmed(boolean z) {
        ValueAnimator valueAnimator = this.mDimAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        float f = z ? 1.0f : 0.0f;
        float f2 = this.mDimAmount;
        if (f != f2) {
            ValueAnimator ofFloat = TimeAnimator.ofFloat(f2, f);
            this.mDimAnimator = ofFloat;
            ofFloat.setDuration(220L);
            this.mDimAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            this.mDimAnimator.addListener(this.mDimEndListener);
            this.mDimAnimator.addUpdateListener(this.mDimUpdateListener);
            this.mDimAnimator.start();
        }
    }

    public void updateSensitiveness(boolean z) {
        boolean isAnyProfilePublicMode = this.mLockscreenUserManager.isAnyProfilePublicMode();
        if (isAnyProfilePublicMode != this.mAmbientState.isHideSensitive()) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                ((ExpandableView) getChildAt(i)).setHideSensitiveForIntrinsicHeight(isAnyProfilePublicMode);
            }
            this.mAmbientState.setHideSensitive(isAnyProfilePublicMode);
            if (z && this.mAnimationsEnabled) {
                this.mHideSensitiveNeedsAnimation = true;
                this.mNeedsAnimation = true;
            }
            updateContentHeight();
            requestChildrenUpdate();
        }
    }

    public void setActivatedChild(ActivatableNotificationView activatableNotificationView) {
        this.mAmbientState.setActivatedChild(activatableNotificationView);
        if (this.mAnimationsEnabled) {
            this.mActivateNeedsAnimation = true;
            this.mNeedsAnimation = true;
        }
        requestChildrenUpdate();
    }

    public ActivatableNotificationView getActivatedChild() {
        return this.mAmbientState.getActivatedChild();
    }

    private void applyCurrentState() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ((ExpandableView) getChildAt(i)).applyViewState();
        }
        NotificationLogger.OnChildLocationsChangedListener onChildLocationsChangedListener = this.mListener;
        if (onChildLocationsChangedListener != null) {
            onChildLocationsChangedListener.onChildLocationsChanged();
        }
        runAnimationFinishedRunnables();
        setAnimationRunning(false);
        updateBackground();
        updateViewShadows();
        updateClippingToTopRoundedCorner();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateViewShadows() {
        float f;
        for (int i = 0; i < getChildCount(); i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                this.mTmpSortedChildren.add(expandableView);
            }
        }
        Collections.sort(this.mTmpSortedChildren, this.mViewPositionComparator);
        ExpandableView expandableView2 = null;
        int i2 = 0;
        while (i2 < this.mTmpSortedChildren.size()) {
            ExpandableView expandableView3 = this.mTmpSortedChildren.get(i2);
            float translationZ = expandableView3.getTranslationZ();
            if (expandableView2 == null) {
                f = translationZ;
            } else {
                f = expandableView2.getTranslationZ();
            }
            float f2 = f - translationZ;
            if (f2 <= 0.0f || f2 >= 0.1f) {
                expandableView3.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
            } else {
                expandableView3.setFakeShadowIntensity(f2 / 0.1f, expandableView2.getOutlineAlpha(), (int) (((expandableView2.getTranslationY() + ((float) expandableView2.getActualHeight())) - expandableView3.getTranslationY()) - ((float) expandableView2.getExtraBottomPadding())), expandableView2.getOutlineTranslation());
            }
            i2++;
            expandableView2 = expandableView3;
        }
        this.mTmpSortedChildren.clear();
    }

    public void updateDecorViews(boolean z) {
        if (z != this.mUsingLightTheme) {
            this.mUsingLightTheme = z;
            int colorAttrDefaultColor = Utils.getColorAttrDefaultColor(new ContextThemeWrapper(((ViewGroup) this).mContext, z ? C0022R$style.Theme_SystemUI_Light : C0022R$style.Theme_SystemUI), C0009R$attr.wallpaperTextColor);
            this.mSectionsManager.setHeaderForegroundColor(colorAttrDefaultColor);
            this.mFooterView.setTextColor(colorAttrDefaultColor);
        }
    }

    public void goToFullShade(long j) {
        this.mGoToFullShadeNeedsAnimation = true;
        this.mGoToFullShadeDelay = j;
        this.mNeedsAnimation = true;
        requestChildrenUpdate();
    }

    public void cancelExpandHelper() {
        this.mExpandHelper.cancel();
    }

    public void setIntrinsicPadding(int i) {
        this.mIntrinsicPadding = i;
        this.mAmbientState.setIntrinsicPadding(i);
    }

    public int getIntrinsicPadding() {
        return this.mIntrinsicPadding;
    }

    public void setDozing(boolean z, boolean z2, PointF pointF) {
        if (this.mAmbientState.isDozing() != z) {
            this.mAmbientState.setDozing(z);
            requestChildrenUpdate();
            notifyHeightChangeListener(this.mShelf);
        }
    }

    public void setHideAmount(float f, float f2) {
        this.mLinearHideAmount = f;
        this.mInterpolatedHideAmount = f2;
        boolean isFullyHidden = this.mAmbientState.isFullyHidden();
        boolean isHiddenAtAll = this.mAmbientState.isHiddenAtAll();
        this.mAmbientState.setHideAmount(f2);
        boolean isFullyHidden2 = this.mAmbientState.isFullyHidden();
        boolean isHiddenAtAll2 = this.mAmbientState.isHiddenAtAll();
        if (isFullyHidden2 != isFullyHidden) {
            updateVisibility();
        }
        if (!isHiddenAtAll && isHiddenAtAll2) {
            resetExposedMenuView(true, true);
        }
        if (!(isFullyHidden2 == isFullyHidden && isHiddenAtAll == isHiddenAtAll2)) {
            invalidateOutline();
        }
        updateAlgorithmHeightAndPadding();
        updateBackgroundDimming();
        requestChildrenUpdate();
        updateOwnTranslationZ();
    }

    private void updateOwnTranslationZ() {
        ExpandableView firstChildNotGone;
        setTranslationZ((!this.mKeyguardBypassController.getBypassEnabled() || !this.mAmbientState.isHiddenAtAll() || (firstChildNotGone = getFirstChildNotGone()) == null || !firstChildNotGone.showingPulsing()) ? 0.0f : firstChildNotGone.getTranslationZ());
    }

    private void updateVisibility() {
        int i = 0;
        if (!((!this.mAmbientState.isFullyHidden() && MiuiKeyguardUtils.isDefaultLockScreenTheme()) || !onKeyguard())) {
            i = 4;
        }
        setVisibility(i);
    }

    public void notifyHideAnimationStart(boolean z) {
        Interpolator interpolator;
        float f = this.mInterpolatedHideAmount;
        if (f == 0.0f || f == 1.0f) {
            this.mBackgroundXFactor = z ? 1.8f : 1.5f;
            if (z) {
                interpolator = Interpolators.FAST_OUT_SLOW_IN_REVERSE;
            } else {
                interpolator = Interpolators.FAST_OUT_SLOW_IN;
            }
            this.mHideXInterpolator = interpolator;
        }
    }

    public void setFooterView(FooterView footerView) {
        int i;
        FooterView footerView2 = this.mFooterView;
        if (footerView2 != null) {
            i = indexOfChild(footerView2);
            removeView(this.mFooterView);
        } else {
            i = -1;
        }
        this.mFooterView = footerView;
        addView(footerView, i);
    }

    public void setEmptyShadeView(EmptyShadeView emptyShadeView) {
        int i;
        EmptyShadeView emptyShadeView2 = this.mEmptyShadeView;
        if (emptyShadeView2 != null) {
            i = indexOfChild(emptyShadeView2);
            removeView(this.mEmptyShadeView);
        } else {
            i = -1;
        }
        this.mEmptyShadeView = emptyShadeView;
        addView(emptyShadeView, i);
    }

    public void updateEmptyShadeView(boolean z) {
        this.mEmptyShadeView.setVisible(z, this.mIsExpanded && this.mAnimationsEnabled);
        int textResource = this.mEmptyShadeView.getTextResource();
        int i = this.mZenController.areNotificationsHiddenInShade() ? C0021R$string.dnd_suppressing_shade_text : C0021R$string.empty_shade_text;
        if (textResource != i) {
            this.mEmptyShadeView.setText(i);
        }
    }

    public void updateFooterView(boolean z, boolean z2, boolean z3) {
        if (this.mFooterView != null) {
            boolean z4 = this.mIsExpanded && this.mAnimationsEnabled;
            this.mFooterView.setVisible(z, z4);
            this.mFooterView.setSecondaryVisible(z2, z4);
            this.mFooterView.showHistory(z3);
        }
    }

    public void setDismissAllInProgress(boolean z) {
        this.mDismissAllInProgress = z;
        this.mAmbientState.setDismissAllInProgress(z);
        handleDismissAllClipping();
    }

    private void handleDismissAllClipping() {
        int childCount = getChildCount();
        boolean z = false;
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                if (!this.mDismissAllInProgress || !z) {
                    expandableView.setMinClipTopAmount(0);
                } else {
                    expandableView.setMinClipTopAmount(expandableView.getClipTopAmount());
                }
                z = canChildBeDismissed(expandableView);
            }
        }
    }

    public boolean isFooterViewNotGone() {
        FooterView footerView = this.mFooterView;
        return (footerView == null || footerView.getVisibility() == 8 || this.mFooterView.willBeGone()) ? false : true;
    }

    public boolean isFooterViewContentVisible() {
        FooterView footerView = this.mFooterView;
        return footerView != null && footerView.isContentVisible();
    }

    public int getFooterViewHeightWithPadding() {
        FooterView footerView = this.mFooterView;
        if (footerView == null) {
            return 0;
        }
        return this.mGapHeight + footerView.getHeight() + this.mPaddingBetweenElements;
    }

    public int getEmptyShadeViewHeight() {
        return this.mEmptyShadeView.getHeight();
    }

    public float getBottomMostNotificationBottom() {
        int childCount = getChildCount();
        float f = 0.0f;
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                float translationY = (expandableView.getTranslationY() + ((float) expandableView.getActualHeight())) - ((float) expandableView.getClipBottomAmount());
                if (translationY > f) {
                    f = translationY;
                }
            }
        }
        return f + getStackTranslation();
    }

    public void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
    }

    public void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
        notificationGroupManager.addOnGroupChangeListener(this.mOnGroupChangeListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void requestAnimateEverything() {
        if (this.mIsExpanded && this.mAnimationsEnabled) {
            this.mEverythingNeedsAnimation = true;
            this.mNeedsAnimation = true;
            requestChildrenUpdate();
        }
    }

    public boolean isBelowLastNotification(float f, float f2) {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            ExpandableView expandableView = (ExpandableView) getChildAt(childCount);
            if (expandableView.getVisibility() != 8) {
                float y = expandableView.getY();
                if (y > f2) {
                    return false;
                }
                boolean z = f2 > (((float) expandableView.getActualHeight()) + y) - ((float) expandableView.getClipBottomAmount());
                FooterView footerView = this.mFooterView;
                if (expandableView == footerView) {
                    if (!z && !footerView.isOnEmptySpace(f - footerView.getX(), f2 - y)) {
                        return false;
                    }
                } else if (expandableView == this.mEmptyShadeView) {
                    return true;
                } else {
                    if (!z) {
                        return false;
                    }
                }
            }
        }
        return f2 > ((float) this.mTopPadding) + this.mStackTranslation;
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEventInternal(accessibilityEvent);
        accessibilityEvent.setScrollable(this.mScrollable);
        accessibilityEvent.setMaxScrollX(((ViewGroup) this).mScrollX);
        accessibilityEvent.setScrollY(this.mOwnScrollY);
        accessibilityEvent.setMaxScrollY(getScrollRange());
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfoInternal(accessibilityNodeInfo);
        if (this.mScrollable) {
            accessibilityNodeInfo.setScrollable(true);
            if (this.mBackwardScrollable) {
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_BACKWARD);
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_UP);
            }
            if (this.mForwardScrollable) {
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD);
                accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN);
            }
        }
        accessibilityNodeInfo.setClassName(ScrollView.class.getName());
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer, com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public void generateChildOrderChangedEvent() {
        if (this.mIsExpanded && this.mAnimationsEnabled) {
            this.mGenerateChildOrderChangedEvent = true;
            this.mNeedsAnimation = true;
            requestChildrenUpdate();
        }
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer, com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public int getContainerChildCount() {
        return getChildCount();
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer, com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public View getContainerChildAt(int i) {
        return getChildAt(i);
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void removeContainerView(View view) {
        Assert.isMainThread();
        removeView(view);
    }

    @Override // com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public void removeListItem(NotificationListItem notificationListItem) {
        removeContainerView(notificationListItem.getView());
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void addContainerView(View view) {
        Assert.isMainThread();
        addView(view);
    }

    @Override // com.android.systemui.statusbar.notification.collection.SimpleNotificationListContainer
    public void addListItem(NotificationListItem notificationListItem) {
        addContainerView(notificationListItem.getView());
    }

    public void runAfterAnimationFinished(Runnable runnable) {
        this.mAnimationFinishedRunnables.add(runnable);
    }

    public void generateHeadsUpAnimation(NotificationEntry notificationEntry, boolean z) {
        generateHeadsUpAnimation(notificationEntry.getHeadsUpAnimationView(), z);
    }

    public void generateHeadsUpAnimation(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        if (!this.mAnimationsEnabled) {
            return;
        }
        if (z || this.mHeadsUpGoingAwayAnimationsAllowed) {
            this.mHeadsUpChangeAnimations.add(new Pair<>(expandableNotificationRow, Boolean.valueOf(z)));
            this.mNeedsAnimation = true;
            if (!this.mIsExpanded && !this.mWillExpand && !z) {
                expandableNotificationRow.setHeadsUpAnimatingAway(true);
            }
            requestChildrenUpdate();
        }
    }

    public void setHeadsUpBoundaries(int i, int i2) {
        this.mAmbientState.setMaxHeadsUpTranslation((float) (i - i2));
        this.mStateAnimator.setHeadsUpAppearHeightBottom(i);
        requestChildrenUpdate();
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void setWillExpand(boolean z) {
        this.mWillExpand = z;
    }

    public void setTrackingHeadsUp(ExpandableNotificationRow expandableNotificationRow) {
        this.mAmbientState.setTrackedHeadsUpRow(expandableNotificationRow);
        this.mRoundnessManager.setTrackingHeadsUp(expandableNotificationRow);
    }

    public void setScrimController(ScrimController scrimController) {
        this.mScrimController = scrimController;
        scrimController.setScrimBehindChangeRunnable(new Runnable() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$EebmavE8B0v9pYEId75j8vvZNvI */

            public final void run() {
                NotificationStackScrollLayout.this.updateBackgroundDimming();
            }
        });
    }

    public void forceNoOverlappingRendering(boolean z) {
        this.mForceNoOverlappingRendering = z;
    }

    public boolean hasOverlappingRendering() {
        return !this.mForceNoOverlappingRendering && super.hasOverlappingRendering();
    }

    public void setAnimationRunning(boolean z) {
        if (z != this.mAnimationRunning) {
            if (z) {
                getViewTreeObserver().addOnPreDrawListener(this.mRunningAnimationUpdater);
            } else {
                getViewTreeObserver().removeOnPreDrawListener(this.mRunningAnimationUpdater);
            }
            this.mAnimationRunning = z;
            updateContinuousShadowDrawing();
        }
    }

    public boolean isExpanded() {
        return this.mIsExpanded;
    }

    public void setPulsing(boolean z, boolean z2) {
        if (this.mPulsing || z) {
            this.mPulsing = z;
            this.mAmbientState.setPulsing(z);
            this.mSwipeHelper.setPulsing(z);
            updateNotificationAnimationStates();
            updateAlgorithmHeightAndPadding();
            updateContentHeight();
            requestChildrenUpdate();
            notifyHeightChangeListener(null, z2);
        }
    }

    public void setQsExpanded(boolean z) {
        this.mQsExpanded = z;
        updateAlgorithmLayoutMinHeight();
        updateScrollability();
    }

    public void setQsExpansionFraction(float f) {
        this.mQsExpansionFraction = f;
    }

    private void setOwnScrollY(int i) {
        int i2 = this.mOwnScrollY;
        if (i != i2) {
            int i3 = ((ViewGroup) this).mScrollX;
            onScrollChanged(i3, i, i3, i2);
            this.mOwnScrollY = i;
            updateOnScrollChange();
        }
    }

    private void updateOnScrollChange() {
        updateForwardAndBackwardScrollability();
        requestChildrenUpdate();
    }

    public void setShelf(NotificationShelf notificationShelf) {
        int i;
        NotificationShelf notificationShelf2 = this.mShelf;
        if (notificationShelf2 != null) {
            i = indexOfChild(notificationShelf2);
            removeView(this.mShelf);
        } else {
            i = -1;
        }
        this.mShelf = notificationShelf;
        addView(notificationShelf, i);
        this.mAmbientState.setShelf(notificationShelf);
        this.mStateAnimator.setShelf(notificationShelf);
        notificationShelf.bind(this.mAmbientState, this);
    }

    public NotificationShelf getNotificationShelf() {
        return this.mShelf;
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void setMaxDisplayedNotifications(int i) {
        if (this.mMaxDisplayedNotifications != i) {
            this.mMaxDisplayedNotifications = i;
            updateContentHeight();
            notifyHeightChangeListener(this.mShelf);
        }
    }

    public void setShouldShowShelfOnly(boolean z) {
        this.mShouldShowShelfOnly = z;
        updateAlgorithmLayoutMinHeight();
    }

    public int getMinExpansionHeight() {
        int intrinsicHeight = this.mShelf.getIntrinsicHeight();
        int i = this.mWaterfallTopInset;
        return (intrinsicHeight - (((this.mShelf.getIntrinsicHeight() - this.mStatusBarHeight) + i) / 2)) + i;
    }

    public void setInHeadsUpPinnedMode(boolean z) {
        this.mInHeadsUpPinnedMode = z;
        updateClipping();
    }

    public void setHeadsUpAnimatingAway(boolean z) {
        this.mHeadsUpAnimatingAway = z;
        updateClipping();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setStatusBarState(int i) {
        this.mStatusBarState = i;
        this.mAmbientState.setStatusBarState(i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onStatePostChange() {
        boolean onKeyguard = onKeyguard();
        HeadsUpAppearanceController headsUpAppearanceController = this.mHeadsUpAppearanceController;
        if (headsUpAppearanceController != null) {
            headsUpAppearanceController.onStateChanged();
        }
        SysuiStatusBarStateController sysuiStatusBarStateController = (SysuiStatusBarStateController) Dependency.get(StatusBarStateController.class);
        updateSensitiveness(sysuiStatusBarStateController.goingToFullShade());
        setDimmed(onKeyguard, sysuiStatusBarStateController.fromShadeLocked());
        setExpandingEnabled(NotificationUtil.hasExpandingFeature() && !onKeyguard);
        ActivatableNotificationView activatedChild = getActivatedChild();
        setActivatedChild(null);
        if (activatedChild != null) {
            activatedChild.makeInactive(false);
        }
        updateFooter();
        requestChildrenUpdate();
        onUpdateRowStates();
        this.mEntryManager.updateNotifications("StatusBar state changed");
        updateVisibility();
    }

    public void setExpandingVelocity(float f) {
        this.mAmbientState.setExpandingVelocity(f);
    }

    public float getOpeningHeight() {
        if (this.mEmptyShadeView.getVisibility() == 8) {
            return (float) getMinExpansionHeight();
        }
        return getAppearEndPosition();
    }

    public void setIsFullWidth(boolean z) {
        this.mAmbientState.setPanelFullWidth(z);
    }

    public void setUnlockHintRunning(boolean z) {
        this.mAmbientState.setUnlockHintRunning(z);
    }

    public void setQsCustomizerShowing(boolean z) {
        this.mAmbientState.setQsCustomizerShowing(z);
        requestChildrenUpdate();
    }

    public void setHeadsUpGoingAwayAnimationsAllowed(boolean z) {
        this.mHeadsUpGoingAwayAnimationsAllowed = z;
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        String str2;
        Object[] objArr = new Object[9];
        objArr[0] = NotificationStackScrollLayout.class.getSimpleName();
        String str3 = "T";
        objArr[1] = this.mPulsing ? str3 : "f";
        if (this.mAmbientState.isQsCustomizerShowing()) {
            str = str3;
        } else {
            str = "f";
        }
        objArr[2] = str;
        if (getVisibility() == 0) {
            str2 = "visible";
        } else {
            str2 = getVisibility() == 8 ? "gone" : "invisible";
        }
        objArr[3] = str2;
        objArr[4] = Float.valueOf(getAlpha());
        objArr[5] = Integer.valueOf(this.mAmbientState.getScrollY());
        objArr[6] = Integer.valueOf(this.mMaxTopPadding);
        if (!this.mShouldShowShelfOnly) {
            str3 = "f";
        }
        objArr[7] = str3;
        objArr[8] = Float.valueOf(this.mQsExpansionFraction);
        printWriter.println(String.format("[%s: pulsing=%s qsCustomizerShowing=%s visibility=%s alpha:%f scrollY:%d maxTopPadding:%d showShelfOnly=%s qsExpandFraction=%f]", objArr));
        int childCount = getChildCount();
        printWriter.println("  Number of children: " + childCount);
        printWriter.println();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            expandableView.dump(fileDescriptor, printWriter, strArr);
            if (!(expandableView instanceof ExpandableNotificationRow)) {
                printWriter.println("  " + expandableView.getClass().getSimpleName());
                ExpandableViewState viewState = expandableView.getViewState();
                if (viewState == null) {
                    printWriter.println("    no viewState!!!");
                } else {
                    printWriter.print("    ");
                    viewState.dump(fileDescriptor, printWriter, strArr);
                    printWriter.println();
                    printWriter.println();
                }
            }
        }
        int transientViewCount = getTransientViewCount();
        printWriter.println("  Transient Views: " + transientViewCount);
        for (int i2 = 0; i2 < transientViewCount; i2++) {
            ((ExpandableView) getTransientView(i2)).dump(fileDescriptor, printWriter, strArr);
        }
        ArrayList<ExpandableView> draggedViews = this.mAmbientState.getDraggedViews();
        int size = draggedViews.size();
        printWriter.println("  Dragged Views: " + size);
        for (int i3 = 0; i3 < size; i3++) {
            draggedViews.get(i3).dump(fileDescriptor, printWriter, strArr);
        }
        printWriter.println("  mAmbientState: " + this.mAmbientState.getStackTranslation() + " " + this.mAmbientState.getTopPadding() + " " + this.mAmbientState.getExpandAnimationTopChange());
    }

    public boolean isFullyHidden() {
        return this.mAmbientState.isFullyHidden();
    }

    public void addOnExpandedHeightChangedListener(BiConsumer<Float, Float> biConsumer) {
        this.mExpandedHeightListeners.add(biConsumer);
    }

    public void removeOnExpandedHeightChangedListener(BiConsumer<Float, Float> biConsumer) {
        this.mExpandedHeightListeners.remove(biConsumer);
    }

    public void setHeadsUpAppearanceController(HeadsUpAppearanceController headsUpAppearanceController) {
        this.mHeadsUpAppearanceController = headsUpAppearanceController;
    }

    public void setIconAreaController(NotificationIconAreaController notificationIconAreaController) {
        this.mIconAreaController = notificationIconAreaController;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x004f, code lost:
        if (r11.mTmpRect.height() > 0) goto L_0x0053;
     */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0094 A[SYNTHETIC] */
    @com.android.internal.annotations.VisibleForTesting
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void clearNotifications(int r12, boolean r13) {
        /*
        // Method dump skipped, instructions count: 190
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout.clearNotifications(int, boolean):void");
    }

    private boolean includeChildInDismissAll(ExpandableNotificationRow expandableNotificationRow, int i) {
        return canChildBeDismissed(expandableNotificationRow) && matchesSelection(expandableNotificationRow, i);
    }

    private void performDismissAllAnimations(ArrayList<View> arrayList, boolean z, Runnable runnable) {
        $$Lambda$NotificationStackScrollLayout$Pgdm3okNKpoXRCoL8Bp8PnrMtvc r0 = new Runnable(z, runnable) {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$Pgdm3okNKpoXRCoL8Bp8PnrMtvc */
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ Runnable f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                NotificationStackScrollLayout.this.lambda$performDismissAllAnimations$11$NotificationStackScrollLayout(this.f$1, this.f$2);
            }
        };
        if (arrayList.isEmpty()) {
            r0.run();
            return;
        }
        setDismissAllInProgress(true);
        int i = 70;
        int i2 = 180;
        int size = arrayList.size() - 1;
        while (size >= 0) {
            dismissViewAnimated(arrayList.get(size), size == 0 ? r0 : null, i2, 200);
            i = Math.max(10, i - 5);
            i2 += i;
            size--;
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$performDismissAllAnimations$11 */
    public /* synthetic */ void lambda$performDismissAllAnimations$11$NotificationStackScrollLayout(boolean z, Runnable runnable) {
        if (z) {
            ((ShadeController) Dependency.get(ShadeController.class)).addPostCollapseAction(new Runnable(runnable) {
                /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$p9KMzxFV7tJKwl7CiA60zeih78 */
                public final /* synthetic */ Runnable f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    NotificationStackScrollLayout.this.lambda$performDismissAllAnimations$10$NotificationStackScrollLayout(this.f$1);
                }
            });
            ((ShadeController) Dependency.get(ShadeController.class)).animateCollapsePanels(0);
            return;
        }
        setDismissAllInProgress(false);
        runnable.run();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$performDismissAllAnimations$10 */
    public /* synthetic */ void lambda$performDismissAllAnimations$10$NotificationStackScrollLayout(Runnable runnable) {
        setDismissAllInProgress(false);
        runnable.run();
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void setNotificationActivityStarter(NotificationActivityStarter notificationActivityStarter) {
        this.mNotificationActivityStarter = notificationActivityStarter;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void inflateFooterView() {
        FooterView footerView = (FooterView) LayoutInflater.from(((ViewGroup) this).mContext).inflate(C0017R$layout.status_bar_notification_footer, (ViewGroup) this, false);
        footerView.setDismissButtonClickListener(new View.OnClickListener() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$tCwjRwJ30PZe98oBqPRQvVRpRU */

            public final void onClick(View view) {
                NotificationStackScrollLayout.this.lambda$inflateFooterView$12$NotificationStackScrollLayout(view);
            }
        });
        footerView.setManageButtonClickListener(new View.OnClickListener() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$oroTKggruvrHfYyaidLgs91S838 */

            public final void onClick(View view) {
                NotificationStackScrollLayout.this.lambda$inflateFooterView$13$NotificationStackScrollLayout(view);
            }
        });
        setFooterView(footerView);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$inflateFooterView$12 */
    public /* synthetic */ void lambda$inflateFooterView$12$NotificationStackScrollLayout(View view) {
        this.mMetricsLogger.action(148);
        clearNotifications(0, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$inflateFooterView$13 */
    public /* synthetic */ void lambda$inflateFooterView$13$NotificationStackScrollLayout(View view) {
        this.mNotificationActivityStarter.startHistoryIntent(this.mFooterView.isHistoryShown());
    }

    private void inflateEmptyShadeView() {
        EmptyShadeView emptyShadeView = (EmptyShadeView) LayoutInflater.from(((ViewGroup) this).mContext).inflate(C0017R$layout.status_bar_no_notifications, (ViewGroup) this, false);
        emptyShadeView.setText(C0021R$string.empty_shade_text);
        emptyShadeView.setOnClickListener(new View.OnClickListener() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$aYoS3zV7WlRKm89tA3jUB5k2PaQ */

            public final void onClick(View view) {
                NotificationStackScrollLayout.this.lambda$inflateEmptyShadeView$14$NotificationStackScrollLayout(view);
            }
        });
        setEmptyShadeView(emptyShadeView);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$inflateEmptyShadeView$14 */
    public /* synthetic */ void lambda$inflateEmptyShadeView$14$NotificationStackScrollLayout(View view) {
        this.mStatusBar.startActivity(MiuiNotificationSectionsManager.Companion.intent4NotificationControlCenterSettings(), true, true, 268435456);
    }

    public void onUpdateRowStates() {
        ForegroundServiceDungeonView foregroundServiceDungeonView = this.mFgsSectionView;
        int i = 1;
        if (foregroundServiceDungeonView != null) {
            changeViewPosition(foregroundServiceDungeonView, getChildCount() - 1);
            i = 2;
        }
        int i2 = i + 1;
        changeViewPosition(this.mFooterView, getChildCount() - i);
        changeViewPosition(this.mEmptyShadeView, getChildCount() - i2);
        changeViewPosition(this.mShelf, getChildCount() - (i2 + 1));
    }

    public void setNotificationPanelController(NotificationPanelViewController notificationPanelViewController) {
        this.mNotificationPanelController = notificationPanelViewController;
    }

    public void updateIconAreaViews() {
        this.mIconAreaController.updateNotificationIcons();
    }

    public float setPulseHeight(float f) {
        this.mAmbientState.setPulseHeight(f);
        if (this.mKeyguardBypassController.getBypassEnabled()) {
            notifyAppearChangedListeners();
        }
        requestChildrenUpdate();
        return Math.max(0.0f, f - ((float) this.mAmbientState.getInnerHeight(true)));
    }

    public float getPulseHeight() {
        return this.mAmbientState.getPulseHeight();
    }

    public void setDozeAmount(float f) {
        this.mAmbientState.setDozeAmount(f);
        updateContinuousBackgroundDrawing();
        requestChildrenUpdate();
    }

    public void wakeUpFromPulse() {
        setPulseHeight(getWakeUpHeight());
        int childCount = getChildCount();
        float f = -1.0f;
        boolean z = true;
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8) {
                boolean z2 = expandableView == this.mShelf;
                if ((expandableView instanceof ExpandableNotificationRow) || z2) {
                    if (expandableView.getVisibility() != 0 || z2) {
                        if (!z) {
                            expandableView.setTranslationY(f);
                        }
                    } else if (z) {
                        f = (expandableView.getTranslationY() + ((float) expandableView.getActualHeight())) - ((float) this.mShelf.getIntrinsicHeight());
                        z = false;
                    }
                }
            }
        }
        this.mDimmedNeedsAnimation = true;
    }

    @Override // com.android.systemui.statusbar.notification.DynamicPrivacyController.Listener
    public void onDynamicPrivacyChanged() {
        if (this.mIsExpanded) {
            this.mAnimateBottomOnLayout = true;
        }
        post(new Runnable() {
            /* class com.android.systemui.statusbar.notification.stack.$$Lambda$NotificationStackScrollLayout$CjO1PNXyUMIULI5IqCNFlCnRYU */

            public final void run() {
                NotificationStackScrollLayout.this.lambda$onDynamicPrivacyChanged$15$NotificationStackScrollLayout();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onDynamicPrivacyChanged$15 */
    public /* synthetic */ void lambda$onDynamicPrivacyChanged$15$NotificationStackScrollLayout() {
        updateFooter();
        updateSectionBoundaries("dynamic privacy changed");
    }

    public void setOnPulseHeightChangedListener(Runnable runnable) {
        this.mAmbientState.setOnPulseHeightChangedListener(runnable);
    }

    public float calculateAppearFractionBypass() {
        return MathUtils.smoothStep(0.0f, (float) getIntrinsicPadding(), getPulseHeight() - getWakeUpHeight());
    }

    public void updateSpeedBumpIndex() {
        int childCount = getChildCount();
        boolean z = false;
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        while (true) {
            boolean z2 = true;
            if (i >= childCount) {
                break;
            }
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != 8 && (childAt instanceof ExpandableNotificationRow)) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                i3++;
                if (!this.mHighPriorityBeforeSpeedBump) {
                    z2 = true ^ expandableNotificationRow.getEntry().isAmbient();
                } else if (expandableNotificationRow.getEntry().getBucket() >= 7) {
                    z2 = false;
                }
                if (z2) {
                    i2 = i3;
                }
            }
            i++;
        }
        if (i2 == childCount) {
            z = true;
        }
        updateSpeedBumpIndex(i2, z);
    }

    public void updateSectionBoundaries(String str) {
        this.mSectionsManager.updateSectionBoundaries(str);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateContinuousBackgroundDrawing() {
        boolean z = !this.mAmbientState.isFullyAwake() && !this.mAmbientState.getDraggedViews().isEmpty();
        if (z != this.mContinuousBackgroundUpdate) {
            this.mContinuousBackgroundUpdate = z;
            if (z) {
                getViewTreeObserver().addOnPreDrawListener(this.mBackgroundUpdater);
            } else {
                getViewTreeObserver().removeOnPreDrawListener(this.mBackgroundUpdater);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateContinuousShadowDrawing() {
        boolean z = this.mAnimationRunning || !this.mAmbientState.getDraggedViews().isEmpty();
        if (z != this.mContinuousShadowUpdate) {
            if (z) {
                getViewTreeObserver().addOnPreDrawListener(this.mShadowUpdater);
            } else {
                getViewTreeObserver().removeOnPreDrawListener(this.mShadowUpdater);
            }
            this.mContinuousShadowUpdate = z;
        }
    }

    @Override // com.android.systemui.statusbar.notification.stack.NotificationListContainer
    public void resetExposedMenuView(boolean z, boolean z2) {
        this.mSwipeHelper.resetExposedMenuView(z, z2);
    }

    private static boolean matchesSelection(ExpandableNotificationRow expandableNotificationRow, int i) {
        if (i == 0) {
            return true;
        }
        if (i == 1) {
            return expandableNotificationRow.getEntry().getBucket() < 7;
        }
        if (i == 2) {
            return expandableNotificationRow.getEntry().getBucket() == 7;
        }
        throw new IllegalArgumentException("Unknown selection: " + i);
    }

    /* access modifiers changed from: package-private */
    public static class AnimationEvent {
        static AnimationFilter[] FILTERS;
        static int[] LENGTHS = {300, 300, 300, 300, 220, 220, 300, 448, 300, 300, 300, 550, 300, 300, 300, 300};
        final int animationType;
        final AnimationFilter filter;
        boolean headsUpFromBottom;
        final long length;
        final ExpandableView mChangingView;
        View viewAfterChangingView;

        static {
            AnimationFilter animationFilter = new AnimationFilter();
            animationFilter.animateAlpha();
            animationFilter.animateHeight();
            animationFilter.animateTopInset();
            animationFilter.animateY();
            animationFilter.animateZ();
            animationFilter.hasDelays();
            AnimationFilter animationFilter2 = new AnimationFilter();
            animationFilter2.animateAlpha();
            animationFilter2.animateHeight();
            animationFilter2.animateTopInset();
            animationFilter2.animateY();
            animationFilter2.animateZ();
            animationFilter2.hasDelays();
            AnimationFilter animationFilter3 = new AnimationFilter();
            animationFilter3.animateHeight();
            animationFilter3.animateTopInset();
            animationFilter3.animateY();
            animationFilter3.animateZ();
            animationFilter3.hasDelays();
            AnimationFilter animationFilter4 = new AnimationFilter();
            animationFilter4.animateHeight();
            animationFilter4.animateTopInset();
            animationFilter4.animateY();
            animationFilter4.animateDimmed();
            animationFilter4.animateZ();
            AnimationFilter animationFilter5 = new AnimationFilter();
            animationFilter5.animateZ();
            AnimationFilter animationFilter6 = new AnimationFilter();
            animationFilter6.animateDimmed();
            AnimationFilter animationFilter7 = new AnimationFilter();
            animationFilter7.animateAlpha();
            animationFilter7.animateHeight();
            animationFilter7.animateTopInset();
            animationFilter7.animateY();
            animationFilter7.animateZ();
            AnimationFilter animationFilter8 = new AnimationFilter();
            animationFilter8.animateHeight();
            animationFilter8.animateTopInset();
            animationFilter8.animateY();
            animationFilter8.animateDimmed();
            animationFilter8.animateZ();
            animationFilter8.hasDelays();
            AnimationFilter animationFilter9 = new AnimationFilter();
            animationFilter9.animateHideSensitive();
            AnimationFilter animationFilter10 = new AnimationFilter();
            animationFilter10.animateHeight();
            animationFilter10.animateTopInset();
            animationFilter10.animateY();
            animationFilter10.animateZ();
            AnimationFilter animationFilter11 = new AnimationFilter();
            animationFilter11.animateAlpha();
            animationFilter11.animateHeight();
            animationFilter11.animateTopInset();
            animationFilter11.animateY();
            animationFilter11.animateZ();
            AnimationFilter animationFilter12 = new AnimationFilter();
            animationFilter12.animateHeight();
            animationFilter12.animateTopInset();
            animationFilter12.animateY();
            animationFilter12.animateZ();
            AnimationFilter animationFilter13 = new AnimationFilter();
            animationFilter13.animateHeight();
            animationFilter13.animateTopInset();
            animationFilter13.animateY();
            animationFilter13.animateZ();
            animationFilter13.hasDelays();
            AnimationFilter animationFilter14 = new AnimationFilter();
            animationFilter14.animateHeight();
            animationFilter14.animateTopInset();
            animationFilter14.animateY();
            animationFilter14.animateZ();
            animationFilter14.hasDelays();
            AnimationFilter animationFilter15 = new AnimationFilter();
            animationFilter15.animateHeight();
            animationFilter15.animateTopInset();
            animationFilter15.animateY();
            animationFilter15.animateZ();
            AnimationFilter animationFilter16 = new AnimationFilter();
            animationFilter16.animateAlpha();
            animationFilter16.animateDimmed();
            animationFilter16.animateHideSensitive();
            animationFilter16.animateHeight();
            animationFilter16.animateTopInset();
            animationFilter16.animateY();
            animationFilter16.animateZ();
            FILTERS = new AnimationFilter[]{animationFilter, animationFilter2, animationFilter3, animationFilter4, animationFilter5, animationFilter6, animationFilter7, animationFilter8, animationFilter9, animationFilter10, animationFilter11, animationFilter12, animationFilter13, animationFilter14, animationFilter15, animationFilter16};
        }

        AnimationEvent(ExpandableView expandableView, int i) {
            this(expandableView, i, (long) LENGTHS[i]);
        }

        AnimationEvent(ExpandableView expandableView, int i, long j) {
            this(expandableView, i, j, FILTERS[i]);
        }

        AnimationEvent(ExpandableView expandableView, int i, long j, AnimationFilter animationFilter) {
            AnimationUtils.currentAnimationTimeMillis();
            this.mChangingView = expandableView;
            this.animationType = i;
            this.length = j;
            this.filter = animationFilter;
        }

        static long combineLength(ArrayList<AnimationEvent> arrayList) {
            int size = arrayList.size();
            long j = 0;
            for (int i = 0; i < size; i++) {
                AnimationEvent animationEvent = arrayList.get(i);
                j = Math.max(j, animationEvent.length);
                if (animationEvent.animationType == 7) {
                    return animationEvent.length;
                }
            }
            return j;
        }
    }

    /* access modifiers changed from: private */
    public static boolean canChildBeDismissed(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (expandableNotificationRow.isBlockingHelperShowingAndTranslationFinished()) {
                return true;
            }
            if (expandableNotificationRow.areGutsExposed() || !expandableNotificationRow.getEntry().hasFinishedInitialization()) {
                return false;
            }
            return expandableNotificationRow.canViewBeDismissed();
        } else if (view instanceof PeopleHubView) {
            return ((PeopleHubView) view).getCanSwipe();
        } else {
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onEntryUpdated(NotificationEntry notificationEntry) {
        if (notificationEntry.rowExists() && !notificationEntry.getSbn().isClearable()) {
            snapViewIfNeeded(notificationEntry);
        }
        if (notificationEntry.rowExists()) {
            updateHideSensitiveForChild(notificationEntry.getRow());
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean hasActiveNotifications() {
        if (this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            return !this.mNotifPipeline.getShadeList().isEmpty();
        }
        return this.mEntryManager.hasActiveNotifications();
    }

    /* access modifiers changed from: private */
    /* renamed from: onDismissAllAnimationsEnd */
    public void lambda$clearNotifications$9(List<ExpandableNotificationRow> list, int i) {
        if (!this.mFeatureFlags.isNewNotifPipelineRenderingEnabled()) {
            for (ExpandableNotificationRow expandableNotificationRow : list) {
                if (!canChildBeDismissed(expandableNotificationRow)) {
                    expandableNotificationRow.resetTranslation();
                } else if (i == 0) {
                    this.mEntryManager.removeNotification(expandableNotificationRow.getEntry().getKey(), null, 3);
                } else {
                    this.mEntryManager.performRemoveNotification(expandableNotificationRow.getEntry().getSbn(), 3);
                }
            }
            if (i == 0) {
                try {
                    this.mBarService.onClearAllNotifications(this.mLockscreenUserManager.getCurrentUserId());
                } catch (Exception unused) {
                }
            }
        } else if (i == 0) {
            this.mNotifCollection.dismissAllNotifications(this.mLockscreenUserManager.getCurrentUserId());
        } else {
            ArrayList arrayList = new ArrayList();
            new ArrayList();
            int shadeListCount = this.mNotifPipeline.getShadeListCount();
            for (int i2 = 0; i2 < list.size(); i2++) {
                NotificationEntry entry = list.get(i2).getEntry();
                arrayList.add(new Pair(entry, new DismissedByUserStats(3, 1, NotificationVisibility.obtain(entry.getKey(), entry.getRanking().getRank(), shadeListCount, true, NotificationLogger.getNotificationLocation(entry)))));
            }
            this.mNotifCollection.dismissNotifications(arrayList);
        }
        ((NotificationStat) Dependency.get(NotificationStat.class)).onRemoveAll(list.size());
    }

    public DragDownHelper.DragDownCallback getDragDownCallback() {
        return this.mDragDownCallback;
    }

    public HeadsUpTouchHelper.Callback getHeadsUpCallback() {
        return this.mHeadsUpCallback;
    }

    public ExpandHelper.Callback getExpandHelperCallback() {
        return this.mExpandHelperCallback;
    }

    /* access modifiers changed from: package-private */
    public enum NotificationPanelEvent implements UiEventLogger.UiEventEnum {
        INVALID(0),
        DISMISS_ALL_NOTIFICATIONS_PANEL(312),
        DISMISS_SILENT_NOTIFICATIONS_PANEL(314);
        
        private final int mId;

        private NotificationPanelEvent(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }

        public static UiEventLogger.UiEventEnum fromSelection(int i) {
            if (i == 0) {
                return DISMISS_ALL_NOTIFICATIONS_PANEL;
            }
            if (i == 2) {
                return DISMISS_SILENT_NOTIFICATIONS_PANEL;
            }
            return INVALID;
        }
    }

    /* access modifiers changed from: protected */
    public AmbientState getAmbientState() {
        return this.mAmbientState;
    }

    /* access modifiers changed from: protected */
    public List<AnimationEvent> getAnimationEvents() {
        return this.mAnimationEvents;
    }

    /* access modifiers changed from: protected */
    public void requestAnimation() {
        this.mNeedsAnimation = true;
    }

    public void setOnChildLocationsChangedListener(OnChildLocationsChangedListener onChildLocationsChangedListener) {
        this.mOnChildLocationsChangedListener = onChildLocationsChangedListener;
    }
}

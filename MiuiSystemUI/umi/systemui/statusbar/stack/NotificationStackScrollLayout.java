package com.android.systemui.statusbar.stack;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Trace;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.Log;
import android.util.Pair;
import android.util.Property;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.widget.OverScroller;
import android.widget.ScrollView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.ExpandHelper;
import com.android.systemui.Interpolators;
import com.android.systemui.SwipeHelper;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.miui.statusbar.notification.HeadsUpAnimatedStubView;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QS;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.EmptyShadeView;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.FlingAnimationUtils;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationGuts;
import com.android.systemui.statusbar.NotificationLogger;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.NotificationSnooze;
import com.android.systemui.statusbar.StackScrollerDecorView;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.VisibilityLocationProvider;
import com.android.systemui.statusbar.phone.BarTransitions;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.ScrollAdapter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class NotificationStackScrollLayout extends ViewGroup implements SwipeHelper.Callback, ExpandHelper.Callback, ScrollAdapter, ExpandableView.OnHeightChangedListener, NotificationGroupManager.OnGroupChangeListener, NotificationMenuRowPlugin.OnMenuEventListener, ScrollContainer, VisibilityLocationProvider, HeadsUpAnimatedStubView.OnHeadsUpHiddenListener {
    private static final Property<NotificationStackScrollLayout, Float> BACKGROUND_FADE = new FloatProperty<NotificationStackScrollLayout>("backgroundFade") {
        public void setValue(NotificationStackScrollLayout notificationStackScrollLayout, float f) {
            notificationStackScrollLayout.setBackgroundFadeAmount(f);
        }

        public Float get(NotificationStackScrollLayout notificationStackScrollLayout) {
            return Float.valueOf(notificationStackScrollLayout.getBackgroundFadeAmount());
        }
    };
    private static final boolean DEBUG = Constants.DEBUG;
    private static final Object PRESENT = new Object();
    private boolean mActivateNeedsAnimation;
    private int mActivePointerId;
    private ArrayList<View> mAddedHeadsUpChildren;
    private final AmbientState mAmbientState;
    private boolean mAnimateNextBackgroundBottom;
    private boolean mAnimateNextBackgroundTop;
    private Runnable mAnimateScroll;
    private ArrayList<AnimationEvent> mAnimationEvents;
    private ConcurrentHashMap<Runnable, Object> mAnimationFinishedRunnables;
    private boolean mAnimationRunning;
    private boolean mAnimationsEnabled;
    private Rect mBackgroundBounds;
    private float mBackgroundFadeAmount;
    private boolean mBackwardScrollable;
    private int mBgColor;
    /* access modifiers changed from: private */
    public ObjectAnimator mBottomAnimator;
    private int mBottomInset;
    private int mCachedBackgroundColor;
    private boolean mChangePositionInProgress;
    boolean mCheckForLeavebehind;
    private boolean mChildRemoveAnimationRunning;
    private boolean mChildTransferInProgress;
    private ArrayList<View> mChildrenAppear;
    private ArrayList<View> mChildrenChangingPositions;
    private ArrayList<View> mChildrenDisappear;
    private ArrayList<View> mChildrenSpringReset;
    private HashSet<View> mChildrenToAddAnimated;
    private ArrayList<View> mChildrenToRemoveAnimated;
    /* access modifiers changed from: private */
    public boolean mChildrenUpdateRequested;
    private ViewTreeObserver.OnPreDrawListener mChildrenUpdater;
    private HashSet<View> mClearOverlayViewsWhenFinished;
    private final Rect mClipRect;
    private int mCollapsedSize;
    private int mContentHeight;
    private boolean mContinuousShadowUpdate;
    /* access modifiers changed from: private */
    public NotificationMenuRowPlugin mCurrMenuRow;
    private Rect mCurrentBounds;
    private int mCurrentStackHeight;
    private StackScrollState mCurrentStackScrollState;
    private int mDarkAnimationOriginIndex;
    private boolean mDarkNeedsAnimation;
    private Paint mDebugPaint;
    private float mDimAmount;
    /* access modifiers changed from: private */
    public ValueAnimator mDimAnimator;
    private Animator.AnimatorListener mDimEndListener;
    private ValueAnimator.AnimatorUpdateListener mDimUpdateListener;
    private boolean mDimmedNeedsAnimation;
    private boolean mDisallowDismissInThisMotion;
    private boolean mDisallowMeasureChildren;
    private boolean mDisallowScrollingInThisMotion;
    private boolean mDismissAllInProgress;
    /* access modifiers changed from: private */
    public boolean mDontClampNextScroll;
    /* access modifiers changed from: private */
    public boolean mDontReportNextOverScroll;
    private int mDownX;
    private ArrayList<View> mDragAnimPendingChildren;
    protected EmptyShadeView mEmptyShadeView;
    /* access modifiers changed from: private */
    public Rect mEndAnimationRect;
    private boolean mEverythingNeedsAnimation;
    private ExpandHelper mExpandHelper;
    private View mExpandedGroupView;
    private float mExpandedHeight;
    private boolean mExpandedInThisMotion;
    private boolean mExpandingNotification;
    private int mExtraBottomRange;
    private int mExtraBottomRangeQsCovered;
    private boolean mFadingOut;
    private FalsingManager mFalsingManager;
    private Runnable mFinishScrollingCallback;
    private ExpandableView mFirstVisibleBackgroundChild;
    private FlingAnimationUtils mFlingAnimationUtils;
    private boolean mForceNoOverlappingRendering;
    private View mForcedScroll;
    private boolean mForwardScrollable;
    private HashSet<View> mFromMoreCardAdditions;
    private boolean mGenerateChildOrderChangedEvent;
    private long mGoToFullShadeDelay;
    private boolean mGoToFullShadeNeedsAnimation;
    private boolean mGroupExpandedForMeasure;
    private NotificationGroupManager mGroupManager;
    private boolean mHeadsUpAnimatingAway;
    private HashSet<Pair<ExpandableNotificationRow, Boolean>> mHeadsUpChangeAnimations;
    private HeadsUpManager mHeadsUpManager;
    private boolean mHeadsUpPinned;
    private boolean mHideSensitiveNeedsAnimation;
    private boolean mInHeadsUpPinnedMode;
    private int mIncreasedPaddingBetweenElements;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private int mIntrinsicPadding;
    private boolean mIsBeingDragged;
    private boolean mIsClipped;
    /* access modifiers changed from: private */
    public boolean mIsExpanded;
    private boolean mIsExpansionChanging;
    private boolean mIsLandscape;
    /* access modifiers changed from: private */
    public boolean mIsQsBeingCovered;
    private boolean mIsQsCovered;
    private int mLastDrawBoundsBottom;
    private int mLastDrawBoundsTop;
    private int mLastMotionY;
    private int mLastNavigationBarMode;
    private ExpandableView mLastVisibleBackgroundChild;
    private NotificationLogger.OnChildLocationsChangedListener mListener;
    private SwipeHelper.LongPressListener mLongPressListener;
    private int mMaxDisplayedNotifications;
    private int mMaxLayoutHeight;
    private float mMaxOverScroll;
    private int mMaxScrollAfterExpand;
    private int mMaximumVelocity;
    /* access modifiers changed from: private */
    public View mMenuExposedView;
    private SwipeHelper.MenuPressListener mMenuPressListener;
    private float mMinTopOverScrollToEscape;
    private int mMinimumVelocity;
    private boolean mNavBarDarkMode;
    private boolean mNeedViewResizeAnimation;
    private boolean mNeedsAnimation;
    private OnEmptySpaceClickListener mOnEmptySpaceClickListener;
    private ExpandableView.OnHeightChangedListener mOnHeightChangedListener;
    private OnTopPaddingUpdateListener mOnTopPaddingUpdateListener;
    private boolean mOnlyScrollingInThisMotion;
    private int mOrientation;
    private float mOverScrolledBottomPixels;
    private float mOverScrolledTopPixels;
    private int mOverflingDistance;
    private OnOverscrollTopChangedListener mOverscrollTopChangedListener;
    /* access modifiers changed from: private */
    public int mOwnScrollY;
    private int mPaddingBetweenElements;
    private boolean mPanelTracking;
    private int mPanelWidth;
    private boolean mParentNotFullyVisible;
    private Collection<HeadsUpManager.HeadsUpEntry> mPulsing;
    /* access modifiers changed from: private */
    public QS mQs;
    /* access modifiers changed from: private */
    public ValueAnimator mQsBeingCoveredAnimator;
    private boolean mQsExpanded;
    private Runnable mReclamp;
    private Rect mRequestedClipBounds;
    private ViewTreeObserver.OnPreDrawListener mRunningAnimationUpdater;
    private ScrimController mScrimController;
    private boolean mScrollable;
    private boolean mScrolledToTopOnFirstDown;
    /* access modifiers changed from: private */
    public OverScroller mScroller;
    protected boolean mScrollingEnabled;
    private ViewTreeObserver.OnPreDrawListener mShadowUpdater;
    private NotificationShelf mShelf;
    private final boolean mShouldDrawNotificationBackground;
    private ArrayList<View> mSnappedBackChildren;
    private boolean mSpringAnimationRunning;
    private int mSpringIncrement;
    private int mSpringLength;
    protected final StackScrollAlgorithm mStackScrollAlgorithm;
    private float mStackTranslation;
    /* access modifiers changed from: private */
    public Rect mStartAnimationRect;
    private final StackStateAnimator mStateAnimator;
    /* access modifiers changed from: private */
    public StatusBar mStatusBar;
    private int mStatusBarHeight;
    private int mStatusBarState;
    private NotificationSwipeHelper mSwipeHelper;
    private ArrayList<View> mSwipedOutViews;
    private boolean mSwipingInProgress;
    private int[] mTempInt2;
    private final ArrayList<Pair<ExpandableNotificationRow, Boolean>> mTmpList;
    private ArrayList<ExpandableView> mTmpSortedChildren;
    /* access modifiers changed from: private */
    public ObjectAnimator mTopAnimator;
    /* access modifiers changed from: private */
    public int mTopPadding;
    private boolean mTopPaddingNeedsAnimation;
    private float mTopPaddingOverflow;
    private boolean mTouchIsClick;
    private int mTouchSlop;
    private boolean mTrackingHeadsUp;
    /* access modifiers changed from: private */
    public View mTranslatingParentView;
    private VelocityTracker mVelocityTracker;
    private Comparator<ExpandableView> mViewPositionComparator;

    public interface OnEmptySpaceClickListener {
        void onEmptySpaceClicked(float f, float f2);
    }

    public interface OnOverscrollTopChangedListener {
        void flingTopOverscroll(float f, boolean z);

        void onOverscrollTopChanged(float f, boolean z);
    }

    public interface OnTopPaddingUpdateListener {
        void onScrollerTopPaddingUpdate(int i);
    }

    public View getHostView() {
        return this;
    }

    public boolean shouldDelayChildPressedState() {
        return true;
    }

    public NotificationStackScrollLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public NotificationStackScrollLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public NotificationStackScrollLayout(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public NotificationStackScrollLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mCurrentStackHeight = Integer.MAX_VALUE;
        this.mActivePointerId = -1;
        this.mBottomInset = 0;
        this.mExtraBottomRange = 0;
        this.mExtraBottomRangeQsCovered = 0;
        this.mCurrentStackScrollState = new StackScrollState(this);
        this.mChildrenToAddAnimated = new HashSet<>();
        this.mAddedHeadsUpChildren = new ArrayList<>();
        this.mChildrenToRemoveAnimated = new ArrayList<>();
        this.mSnappedBackChildren = new ArrayList<>();
        this.mDragAnimPendingChildren = new ArrayList<>();
        this.mChildrenChangingPositions = new ArrayList<>();
        this.mFromMoreCardAdditions = new HashSet<>();
        this.mAnimationEvents = new ArrayList<>();
        this.mSwipedOutViews = new ArrayList<>();
        this.mChildrenAppear = new ArrayList<>();
        this.mChildrenDisappear = new ArrayList<>();
        this.mChildrenSpringReset = new ArrayList<>();
        this.mStateAnimator = new StackStateAnimator(this);
        this.mIsExpanded = true;
        this.mOrientation = 1;
        this.mLastNavigationBarMode = -1;
        this.mChildrenUpdater = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                NotificationStackScrollLayout.this.updateForcedScroll();
                NotificationStackScrollLayout.this.updateChildren();
                boolean unused = NotificationStackScrollLayout.this.mChildrenUpdateRequested = false;
                NotificationStackScrollLayout.this.getViewTreeObserver().removeOnPreDrawListener(this);
                return true;
            }
        };
        this.mTempInt2 = new int[2];
        this.mAnimationFinishedRunnables = new ConcurrentHashMap<>();
        this.mClearOverlayViewsWhenFinished = new HashSet<>();
        this.mHeadsUpChangeAnimations = new HashSet<>();
        this.mTmpList = new ArrayList<>();
        this.mRunningAnimationUpdater = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                NotificationStackScrollLayout.this.onPreDrawDuringAnimation();
                return true;
            }
        };
        this.mBackgroundBounds = new Rect();
        this.mStartAnimationRect = new Rect();
        this.mEndAnimationRect = new Rect();
        this.mCurrentBounds = new Rect(-1, -1, -1, -1);
        this.mBottomAnimator = null;
        this.mTopAnimator = null;
        this.mFirstVisibleBackgroundChild = null;
        this.mLastVisibleBackgroundChild = null;
        this.mTmpSortedChildren = new ArrayList<>();
        this.mDimEndListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                ValueAnimator unused = NotificationStackScrollLayout.this.mDimAnimator = null;
            }
        };
        this.mDimUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                NotificationStackScrollLayout.this.setDimAmount(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        };
        this.mShadowUpdater = new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                NotificationStackScrollLayout.this.updateViewShadows();
                return true;
            }
        };
        this.mViewPositionComparator = new Comparator<ExpandableView>() {
            public int compare(ExpandableView expandableView, ExpandableView expandableView2) {
                float translationY = expandableView.getTranslationY() + ((float) expandableView.getActualHeight());
                float translationY2 = expandableView2.getTranslationY() + ((float) expandableView2.getActualHeight());
                if (translationY < translationY2) {
                    return -1;
                }
                return translationY > translationY2 ? 1 : 0;
            }
        };
        new PorterDuffXfermode(PorterDuff.Mode.SRC);
        this.mBackgroundFadeAmount = 1.0f;
        this.mMaxDisplayedNotifications = -1;
        this.mClipRect = new Rect();
        this.mAnimateScroll = new Runnable() {
            public void run() {
                NotificationStackScrollLayout.this.animateScroll();
            }
        };
        this.mLastDrawBoundsTop = -1;
        this.mLastDrawBoundsBottom = -1;
        this.mReclamp = new Runnable() {
            public void run() {
                NotificationStackScrollLayout.this.mScroller.startScroll(NotificationStackScrollLayout.this.mScrollX, NotificationStackScrollLayout.this.mOwnScrollY, 0, NotificationStackScrollLayout.this.getScrollRange() - NotificationStackScrollLayout.this.mOwnScrollY);
                boolean unused = NotificationStackScrollLayout.this.mDontReportNextOverScroll = true;
                boolean unused2 = NotificationStackScrollLayout.this.mDontClampNextScroll = true;
                NotificationStackScrollLayout.this.animateScroll();
            }
        };
        Resources resources = getResources();
        AmbientState ambientState = new AmbientState(context);
        this.mAmbientState = ambientState;
        this.mCurrentStackScrollState.setAmbientState(ambientState);
        this.mBgColor = context.getColor(R.color.notification_shade_background_color);
        ExpandHelper expandHelper = new ExpandHelper(getContext(), this, resources.getDimensionPixelSize(R.dimen.notification_min_height), resources.getDimensionPixelSize(R.dimen.notification_max_height));
        this.mExpandHelper = expandHelper;
        expandHelper.setEventSource(this);
        this.mExpandHelper.setScrollAdapter(this);
        NotificationSwipeHelper notificationSwipeHelper = new NotificationSwipeHelper(0, this, getContext());
        this.mSwipeHelper = notificationSwipeHelper;
        notificationSwipeHelper.setLongPressListener(this.mLongPressListener);
        this.mStackScrollAlgorithm = createStackScrollAlgorithm(context);
        initView(context);
        this.mFalsingManager = FalsingManager.getInstance(context);
        this.mShouldDrawNotificationBackground = resources.getBoolean(R.bool.config_drawNotificationBackground);
        updateWillNotDraw();
        if (DEBUG) {
            Paint paint = new Paint();
            this.mDebugPaint = paint;
            paint.setColor(-65536);
            this.mDebugPaint.setStrokeWidth(2.0f);
            this.mDebugPaint.setStyle(Paint.Style.STROKE);
        }
    }

    public void setLastNavigationBarMode(int i) {
        if (this.mLastNavigationBarMode != -1) {
            this.mLastNavigationBarMode = i;
        }
    }

    private void switchNavigationBarModeIfNeed() {
        if (!this.mStatusBar.isFullScreenGestureMode()) {
            boolean z = false;
            int i = -1;
            if (this.mOrientation != 2) {
                if (this.mCurrentBounds.bottom > this.mStatusBar.getNavigationBarYPosition() && this.mStatusBar.getNavigationBarYPosition() > 0) {
                    z = true;
                }
                if (z != this.mNavBarDarkMode) {
                    this.mNavBarDarkMode = z;
                    updateNavigationBarMode(z);
                    if (z) {
                        i = this.mStatusBar.getNavigationBarMode();
                    }
                    this.mLastNavigationBarMode = i;
                }
            } else if (this.mLastNavigationBarMode != -1) {
                updateNavigationBarMode(false);
                this.mLastNavigationBarMode = -1;
                this.mNavBarDarkMode = false;
            }
        }
    }

    private void updateNavigationBarMode(boolean z) {
        int i;
        BarTransitions barTransitions = this.mStatusBar.getNavigationBarView().getBarTransitions();
        if (z) {
            i = 1;
        } else {
            i = this.mLastNavigationBarMode;
        }
        barTransitions.transitionTo(i, true);
        this.mStatusBar.getNavigationBarView().setDisabledFlags(this.mStatusBar.getFlagDisable1(), true);
    }

    public NotificationSwipeActionHelper getSwipeActionHelper() {
        return this.mSwipeHelper;
    }

    public void setFlingAnimationUtils(FlingAnimationUtils flingAnimationUtils) {
        this.mFlingAnimationUtils = flingAnimationUtils;
    }

    public void onMenuClicked(View view, int i, int i2, NotificationMenuRowPlugin.MenuItem menuItem) {
        if (this.mMenuPressListener != null) {
            if (view instanceof ExpandableNotificationRow) {
                MetricsLogger.action(this.mContext, 333, ((ExpandableNotificationRow) view).getStatusBarNotification().getPackageName());
            }
            this.mMenuPressListener.onMenuPress(view, i, i2, menuItem);
        }
    }

    public void onMenuReset(View view) {
        View view2 = this.mTranslatingParentView;
        if (view2 != null && view == view2) {
            this.mMenuExposedView = null;
            this.mTranslatingParentView = null;
        }
    }

    public void onMenuShown(View view) {
        this.mMenuExposedView = this.mTranslatingParentView;
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            MetricsLogger.action(this.mContext, 332, expandableNotificationRow.getStatusBarNotification().getPackageName());
            ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleNotiOpenMenuEvent(expandableNotificationRow.getStatusBarNotification(), getNotGoneNotifications().indexOf(expandableNotificationRow.getEntry()));
        }
        this.mSwipeHelper.onMenuShown(view);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int i = this.mLastDrawBoundsBottom;
        Rect rect = this.mCurrentBounds;
        if (!(i == rect.bottom && this.mLastDrawBoundsTop == rect.top)) {
            Rect rect2 = this.mCurrentBounds;
            this.mLastDrawBoundsTop = rect2.top;
            this.mLastDrawBoundsBottom = rect2.bottom;
        }
        if (DEBUG) {
            float f = (float) this.mTopPadding;
            Canvas canvas2 = canvas;
            canvas2.drawLine(0.0f, f, (float) getWidth(), f, this.mDebugPaint);
            float layoutHeight = (float) getLayoutHeight();
            canvas2.drawLine(0.0f, layoutHeight, (float) getWidth(), layoutHeight, this.mDebugPaint);
            float height = (float) (getHeight() - getEmptyBottomMargin());
            canvas.drawLine(0.0f, height, (float) getWidth(), height, this.mDebugPaint);
        }
    }

    /* access modifiers changed from: private */
    public void updateBackgroundDimming() {
        if (this.mShouldDrawNotificationBackground) {
            float f = (((1.0f - this.mDimAmount) * 0.3f) + 0.7f) * this.mBackgroundFadeAmount;
            int scrimBehindColor = this.mScrimController.getScrimBehindColor();
            float f2 = 1.0f - f;
            int argb = Color.argb((int) ((f * 255.0f) + (((float) Color.alpha(scrimBehindColor)) * f2)), (int) ((this.mBackgroundFadeAmount * ((float) Color.red(this.mBgColor))) + (((float) Color.red(scrimBehindColor)) * f2)), (int) ((this.mBackgroundFadeAmount * ((float) Color.green(this.mBgColor))) + (((float) Color.green(scrimBehindColor)) * f2)), (int) ((this.mBackgroundFadeAmount * ((float) Color.blue(this.mBgColor))) + (f2 * ((float) Color.blue(scrimBehindColor)))));
            if (this.mCachedBackgroundColor != argb) {
                this.mCachedBackgroundColor = argb;
                invalidate();
            }
        }
    }

    private void initView(Context context) {
        this.mScroller = new OverScroller(getContext());
        setDescendantFocusability(262144);
        setClipChildren(false);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        this.mTouchSlop = viewConfiguration.getScaledTouchSlop();
        this.mMinimumVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mOverflingDistance = viewConfiguration.getScaledOverflingDistance();
        this.mCollapsedSize = context.getResources().getDimensionPixelSize(R.dimen.notification_min_height);
        this.mStackScrollAlgorithm.initView(context);
        this.mAmbientState.reload(context);
        this.mPaddingBetweenElements = context.getResources().getDimensionPixelSize(R.dimen.notification_divider_height);
        this.mIncreasedPaddingBetweenElements = context.getResources().getDimensionPixelSize(R.dimen.notification_divider_height_increased);
        this.mMinTopOverScrollToEscape = (float) getResources().getDimensionPixelSize(R.dimen.min_top_overscroll_to_qs);
        this.mStatusBarHeight = getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        this.mPanelWidth = getResources().getDimensionPixelSize(R.dimen.notification_panel_width);
    }

    public void setDrawBackgroundAsSrc(boolean z) {
        updateSrcDrawing();
    }

    private void updateSrcDrawing() {
        if (this.mShouldDrawNotificationBackground) {
            invalidate();
        }
    }

    private void notifyHeightChangeListener(ExpandableView expandableView) {
        ExpandableView.OnHeightChangedListener onHeightChangedListener = this.mOnHeightChangedListener;
        if (onHeightChangedListener != null) {
            onHeightChangedListener.onHeightChanged(expandableView, false);
        }
    }

    /* access modifiers changed from: private */
    public void notifyTopPaddingUpdateListener(int i) {
        int min = Math.min(Math.max(i, this.mQs.getQsHeaderHeight()), this.mQs.getQsMinExpansionHeight());
        this.mTopPaddingOverflow = 0.0f;
        OnTopPaddingUpdateListener onTopPaddingUpdateListener = this.mOnTopPaddingUpdateListener;
        if (onTopPaddingUpdateListener != null) {
            onTopPaddingUpdateListener.onScrollerTopPaddingUpdate(min);
        }
    }

    public void doExpandCollapseAnimation(boolean z, int i) {
        int i2 = 1;
        if (z) {
            this.mIsQsBeingCovered = true;
        } else {
            this.mIsQsCovered = false;
        }
        if (z) {
            i2 = -1;
        }
        endQsBeingCoveredMotion(i * i2);
        this.mActivePointerId = -1;
        endDrag();
    }

    private void endQsBeingCoveredMotion(int i) {
        int i2 = this.mTopPadding;
        QS qs = this.mQs;
        int qsMinExpansionHeight = i >= 0 ? qs.getQsMinExpansionHeight() : qs.getQsHeaderHeight();
        ValueAnimator ofInt = ValueAnimator.ofInt(new int[]{i2, qsMinExpansionHeight});
        this.mQsBeingCoveredAnimator = ofInt;
        this.mFlingAnimationUtils.apply((Animator) ofInt, (float) i2, (float) qsMinExpansionHeight, (float) i);
        this.mQsBeingCoveredAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                NotificationStackScrollLayout.this.notifyTopPaddingUpdateListener(((Integer) valueAnimator.getAnimatedValue()).intValue());
            }
        });
        this.mQsBeingCoveredAnimator.addListener(new Animator.AnimatorListener() {
            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
            }

            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                boolean z = false;
                boolean unused = NotificationStackScrollLayout.this.mIsQsBeingCovered = false;
                NotificationStackScrollLayout notificationStackScrollLayout = NotificationStackScrollLayout.this;
                if (notificationStackScrollLayout.mTopPadding == NotificationStackScrollLayout.this.mQs.getQsHeaderHeight()) {
                    z = true;
                }
                notificationStackScrollLayout.resetIsQsCovered(z);
                ValueAnimator unused2 = NotificationStackScrollLayout.this.mQsBeingCoveredAnimator = null;
            }
        });
        this.mQsBeingCoveredAnimator.start();
    }

    public void disallowMeasureChildren(boolean z) {
        this.mDisallowMeasureChildren = z;
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int i3;
        Trace.traceBegin(8, "onMeasure StackScroller");
        if (this.mIsLandscape && (i3 = this.mPanelWidth) > 0) {
            i = View.MeasureSpec.makeMeasureSpec(i3, 1073741824);
        }
        super.onMeasure(i, i2);
        Trace.traceEnd(8);
        if (!this.mDisallowMeasureChildren || this.mIsExpanded || this.mStatusBarHeight != getMeasuredHeight() || getMeasuredHeight() != View.MeasureSpec.getSize(i2)) {
            Trace.traceBegin(8, "onMeasure StackScroller children");
            int childCount = getChildCount();
            for (int i4 = 0; i4 < childCount; i4++) {
                measureChild(getChildAt(i4), i, i2);
            }
            Trace.traceEnd(8);
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
        disallowMeasureChildren(false);
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

    public void setChildLocationsChangedListener(NotificationLogger.OnChildLocationsChangedListener onChildLocationsChangedListener) {
        this.mListener = onChildLocationsChangedListener;
    }

    public boolean isInVisibleLocation(ExpandableNotificationRow expandableNotificationRow) {
        ExpandableViewState viewStateForView = this.mCurrentStackScrollState.getViewStateForView(expandableNotificationRow);
        if (viewStateForView == null) {
            return false;
        }
        int i = viewStateForView.location & 5;
        viewStateForView.location = i;
        if (i == 0 || expandableNotificationRow.getVisibility() != 0) {
            return false;
        }
        if (this.mHeadsUpPinned || this.mHeadsUpAnimatingAway) {
            return this.mHeadsUpManager.isHeadsUp(expandableNotificationRow.getEntry().key);
        }
        expandableNotificationRow.getLocationOnScreen(this.mTempInt2);
        int i2 = this.mTempInt2[1];
        if (viewStateForView.height + i2 < Math.max(0, Math.max(getTop(), this.mBackgroundBounds.top)) || i2 > Math.min(getBottom(), this.mBackgroundBounds.bottom)) {
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
        this.mAmbientState.setLayoutMinHeight((!this.mQsExpanded || onKeyguard()) ? 0 : getLayoutMinHeight());
    }

    /* access modifiers changed from: private */
    public void updateChildren() {
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
        this.mStackScrollAlgorithm.getStackScrollState(this.mAmbientState, this.mCurrentStackScrollState);
        if (isCurrentlyAnimating() || this.mNeedsAnimation) {
            startAnimationToState();
        } else {
            applyCurrentState();
        }
    }

    /* access modifiers changed from: private */
    public void onPreDrawDuringAnimation() {
        this.mShelf.updateAppearance();
        if (!this.mNeedsAnimation && !this.mChildrenUpdateRequested) {
            updateBackground();
        }
        if (this.mChildRemoveAnimationRunning) {
            postInvalidateOnAnimation();
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
    public void updateForcedScroll() {
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

    private void requestChildrenUpdate() {
        if (!this.mChildrenUpdateRequested) {
            getViewTreeObserver().addOnPreDrawListener(this.mChildrenUpdater);
            this.mChildrenUpdateRequested = true;
            invalidate();
        }
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
        if (DEBUG) {
            Log.d("StackScroller", "setTopPadding topPadding=" + i + ", animate=" + z);
        }
        if (this.mTopPadding != i) {
            this.mTopPadding = i;
            updateContentHeight();
            updateAlgorithmHeightAndPadding();
            if (z && this.mAnimationsEnabled && this.mIsExpanded) {
                this.mTopPaddingNeedsAnimation = true;
                this.mNeedsAnimation = true;
            }
            requestChildrenUpdate();
            notifyHeightChangeListener((ExpandableView) null);
        }
    }

    public void setExpandedHeight(float f) {
        int i;
        float f2;
        if (DEBUG) {
            Log.d("StackScroller", "setExpandedHeight height=" + f);
        }
        this.mExpandedHeight = f;
        float f3 = 0.0f;
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
            setRequestedClipBounds((Rect) null);
        }
        float appearEndPosition = getAppearEndPosition();
        float appearStartPosition = getAppearStartPosition();
        float f4 = 1.0f;
        if (f >= appearEndPosition) {
            if (!this.mIsExpansionChanging || onKeyguard()) {
                i = (int) f;
            } else {
                float stackAppearMinHeight = (float) getStackAppearMinHeight();
                i = (int) Math.max(f, appearEndPosition + stackAppearMinHeight);
                if (stackAppearMinHeight > 0.0f) {
                    f4 = Math.min((f - appearEndPosition) / stackAppearMinHeight, 1.0f);
                }
            }
            updateStackAppearState(f4, getWidth() / 2, (int) appearEndPosition);
        } else {
            float appearFraction = getAppearFraction(f);
            if (appearFraction >= 0.0f) {
                f2 = NotificationUtils.interpolate(getExpandTranslationStart(), 0.0f, appearFraction);
            } else {
                f2 = (f - appearStartPosition) + getExpandTranslationStart();
            }
            i = (int) (f - f2);
            if (this.mHeadsUpPinned || this.mHeadsUpAnimatingAway) {
                f3 = 1.0f;
            }
            setTransitionAlpha(f3);
            f3 = f2;
        }
        int i2 = i + this.mSpringIncrement;
        if (i2 != this.mCurrentStackHeight) {
            this.mCurrentStackHeight = i2;
            updateAlgorithmHeightAndPadding();
            requestChildrenUpdate();
        }
        setStackTranslation(f3);
    }

    private void updateStackAppearState(float f, int i, int i2) {
        float f2 = (0.07999998f * f) + 0.92f;
        setPivotX((float) i);
        setPivotY((float) i2);
        setScaleX(f2);
        setScaleY(f2);
        setTransitionAlpha(f);
    }

    private int getStackAppearMinHeight() {
        int i = 0;
        for (int i2 = 0; i2 < getChildCount(); i2++) {
            View childAt = getChildAt(i2);
            if (childAt.getVisibility() == 0 && (childAt instanceof ExpandableView)) {
                if (!(childAt instanceof ExpandableNotificationRow) || ((ExpandableView) childAt).getViewType() != 0) {
                    i += ((ExpandableView) childAt).getActualHeight();
                } else {
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                    if ((expandableNotificationRow.areChildrenExpanded() || expandableNotificationRow.isGroupExpansionChanging()) && expandableNotificationRow.getNotificationChildren() != null && expandableNotificationRow.getNotificationChildren().size() > 0) {
                        expandableNotificationRow = expandableNotificationRow.getNotificationChildren().get(0);
                    }
                    return (i + expandableNotificationRow.getIntrinsicHeight()) - expandableNotificationRow.getExtraPadding();
                }
            }
        }
        return i;
    }

    private void setRequestedClipBounds(Rect rect) {
        this.mRequestedClipBounds = rect;
        updateClipping();
    }

    public void updateClipping() {
        boolean z = this.mRequestedClipBounds != null && !this.mInHeadsUpPinnedMode && !this.mHeadsUpAnimatingAway;
        if (this.mIsClipped != z) {
            this.mIsClipped = z;
            updateFadingState();
        }
        if (z) {
            setClipBounds(this.mRequestedClipBounds);
        } else {
            setClipBounds((Rect) null);
        }
    }

    private float getExpandTranslationStart() {
        if (this.mHeadsUpPinned || this.mHeadsUpAnimatingAway) {
            return (float) (-this.mTopPadding);
        }
        return 0.0f;
    }

    private float getAppearStartPosition() {
        int minExpansionHeight;
        ExpandableView expandableView;
        if (!this.mTrackingHeadsUp || (expandableView = this.mFirstVisibleBackgroundChild) == null || !expandableView.isAboveShelf()) {
            minExpansionHeight = getMinExpansionHeight();
        } else {
            minExpansionHeight = this.mFirstVisibleBackgroundChild.getPinnedHeadsUpHeight();
        }
        return (float) minExpansionHeight;
    }

    private float getAppearEndPosition() {
        int i;
        int notGoneChildCount = getNotGoneChildCount();
        if (this.mEmptyShadeView.getVisibility() != 8 || notGoneChildCount == 0) {
            i = this.mEmptyShadeView.getHeight();
        } else {
            int i2 = 1;
            if (this.mTrackingHeadsUp || this.mHeadsUpManager.hasPinnedHeadsUp()) {
                i = this.mHeadsUpManager.getTopHeadsUpPinnedHeight();
                i2 = 2;
            } else {
                i = 0;
            }
            if (notGoneChildCount >= i2) {
                i += this.mShelf.getIntrinsicHeight();
            }
        }
        return (float) (i + (onKeyguard() ? this.mTopPadding : this.mIntrinsicPadding));
    }

    public float getAppearFraction(float f) {
        float appearEndPosition = getAppearEndPosition();
        float appearStartPosition = getAppearStartPosition();
        if (appearStartPosition == appearEndPosition) {
            return 0.0f;
        }
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
        int i = this.mCurrentStackHeight;
        int i2 = this.mMaxLayoutHeight;
        return i < i2 ? i : i2;
    }

    public int getFirstItemMinHeight() {
        ExpandableView firstChildNotGone = getFirstChildNotGone();
        return firstChildNotGone != null ? firstChildNotGone.getMinHeight() : this.mCollapsedSize;
    }

    public void setLongPressListener(SwipeHelper.LongPressListener longPressListener) {
        this.mSwipeHelper.setLongPressListener(longPressListener);
        this.mLongPressListener = longPressListener;
    }

    public void setMenuPressListener(SwipeHelper.MenuPressListener menuPressListener) {
        this.mMenuPressListener = menuPressListener;
    }

    public void setQs(QS qs) {
        this.mQs = qs;
    }

    public void onChildDismissed(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (!expandableNotificationRow.isDismissed()) {
                handleChildDismissed(view);
            }
            ViewGroup transientContainer = expandableNotificationRow.getTransientContainer();
            if (transientContainer != null) {
                transientContainer.removeTransientView(view);
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleChildDismissed(View view) {
        if (!this.mDismissAllInProgress) {
            setSwipingInProgress(false);
            if (this.mDragAnimPendingChildren.contains(view)) {
                this.mDragAnimPendingChildren.remove(view);
            }
            this.mSwipedOutViews.add(view);
            this.mAmbientState.onDragFinished(view);
            updateContinuousShadowDrawing();
            if (view instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                if (expandableNotificationRow.isHeadsUp()) {
                    this.mHeadsUpManager.addSwipedOutNotification(expandableNotificationRow.getStatusBarNotification().getKey());
                }
            }
            performDismiss(view, this.mGroupManager, false);
            this.mFalsingManager.onNotificationDismissed();
            if (this.mFalsingManager.shouldEnforceBouncer()) {
                this.mStatusBar.executeRunnableDismissingKeyguard((Runnable) null, (Runnable) null, false, true, false);
            }
        }
    }

    public static void performDismiss(View view, NotificationGroupManager notificationGroupManager, boolean z) {
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (notificationGroupManager.isOnlyChildInGroup(expandableNotificationRow.getStatusBarNotification())) {
                ExpandableNotificationRow logicalGroupSummary = notificationGroupManager.getLogicalGroupSummary(expandableNotificationRow.getStatusBarNotification());
                if (logicalGroupSummary.isClearable()) {
                    performDismiss(logicalGroupSummary, notificationGroupManager, z);
                }
            }
            expandableNotificationRow.setDismissed(true, z);
            if (expandableNotificationRow.isClearable()) {
                expandableNotificationRow.performDismiss();
            }
            if (DEBUG) {
                Log.v("StackScroller", "onChildDismissed: " + view);
            }
        }
    }

    public void onChildSnappedBack(View view, float f) {
        this.mAmbientState.onDragFinished(view);
        updateContinuousShadowDrawing();
        if (!this.mDragAnimPendingChildren.contains(view)) {
            if (this.mAnimationsEnabled) {
                this.mSnappedBackChildren.add(view);
                this.mNeedsAnimation = true;
            }
            requestChildrenUpdate();
        } else {
            this.mDragAnimPendingChildren.remove(view);
        }
        NotificationMenuRowPlugin notificationMenuRowPlugin = this.mCurrMenuRow;
        if (notificationMenuRowPlugin != null && f == 0.0f) {
            notificationMenuRowPlugin.resetMenu();
            this.mCurrMenuRow = null;
        }
    }

    public void onBeginDrag(View view) {
        this.mFalsingManager.onNotificatonStartDismissing();
        setSwipingInProgress(true);
        this.mAmbientState.onBeginDrag(view);
        updateContinuousShadowDrawing();
        if (this.mAnimationsEnabled && (this.mIsExpanded || !isPinnedHeadsUp(view))) {
            this.mDragAnimPendingChildren.add(view);
            this.mNeedsAnimation = true;
        }
        requestChildrenUpdate();
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

    public void onDragCancelled(View view) {
        this.mFalsingManager.onNotificatonStopDismissing();
        setSwipingInProgress(false);
    }

    public float getFalsingThresholdFactor() {
        return this.mStatusBar.isWakeUpComingFromTouch() ? 1.5f : 1.0f;
    }

    public View getChildAtPosition(MotionEvent motionEvent) {
        ExpandableNotificationRow notificationParent;
        ExpandableView childAtPosition = getChildAtPosition(motionEvent.getX(), motionEvent.getY());
        if (!(childAtPosition instanceof ExpandableNotificationRow) || (notificationParent = ((ExpandableNotificationRow) childAtPosition).getNotificationParent()) == null || !notificationParent.areChildrenExpanded()) {
            return childAtPosition;
        }
        return (notificationParent.areGutsExposed() || this.mMenuExposedView == notificationParent || (notificationParent.getNotificationChildren().size() == 1 && notificationParent.isClearable())) ? notificationParent : childAtPosition;
    }

    public ExpandableView getClosestChildAtRawPosition(float f, float f2) {
        getLocationOnScreen(this.mTempInt2);
        float f3 = f2 - ((float) this.mTempInt2[1]);
        int childCount = getChildCount();
        ExpandableView expandableView = null;
        float f4 = Float.MAX_VALUE;
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView2 = (ExpandableView) getChildAt(i);
            if (expandableView2.getVisibility() != 8 && !(expandableView2 instanceof StackScrollerDecorView)) {
                float translationY = expandableView2.getTranslationY();
                float min = Math.min(Math.abs((((float) expandableView2.getClipTopAmount()) + translationY) - f3), Math.abs(((translationY + ((float) expandableView2.getActualHeight())) - ((float) expandableView2.getClipBottomAmount())) - f3));
                if (min < f4) {
                    expandableView = expandableView2;
                    f4 = min;
                }
            }
        }
        return expandableView;
    }

    public ExpandableView getChildAtRawPosition(float f, float f2) {
        getLocationOnScreen(this.mTempInt2);
        int[] iArr = this.mTempInt2;
        return getChildAtPosition(f - ((float) iArr[0]), f2 - ((float) iArr[1]));
    }

    public ExpandableView getChildAtPosition(float f, float f2) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8 && !(expandableView instanceof StackScrollerDecorView)) {
                float translationY = expandableView.getTranslationY();
                float actualHeight = (((float) expandableView.getActualHeight()) + translationY) - ((float) expandableView.getClipBottomAmount());
                int width = getWidth();
                if (f2 >= ((float) expandableView.getClipTopAmount()) + translationY && f2 <= actualHeight && f >= ((float) 0) && f <= ((float) width)) {
                    if (!(expandableView instanceof ExpandableNotificationRow)) {
                        return expandableView;
                    }
                    ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
                    if (this.mIsExpanded || !expandableNotificationRow.isHeadsUp() || !expandableNotificationRow.isPinned() || this.mHeadsUpManager.getTopEntry().entry.row == expandableNotificationRow || this.mGroupManager.getGroupSummary((StatusBarNotification) this.mHeadsUpManager.getTopEntry().entry.row.getStatusBarNotification()) == expandableNotificationRow) {
                        return expandableNotificationRow.getViewAtPosition(f2 - translationY);
                    }
                }
            }
        }
        return null;
    }

    public boolean canChildBeExpanded(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            return expandableNotificationRow.isExpandable() && !expandableNotificationRow.areGutsExposed() && (this.mIsExpanded || !expandableNotificationRow.isPinned());
        }
    }

    public void setUserExpandedChild(View view, boolean z) {
        if (view instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
            if (!z || !onKeyguard()) {
                expandableNotificationRow.setUserExpanded(z, true);
                expandableNotificationRow.onExpandedByGesture(z);
                return;
            }
            expandableNotificationRow.setUserLocked(false);
            updateContentHeight();
            notifyHeightChangeListener(expandableNotificationRow);
        }
    }

    public void setExpansionCancelled(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).setGroupExpansionChanging(false);
        }
    }

    public void setUserLockedChild(View view, boolean z) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).setUserLocked(z);
        }
        removeLongPressCallback();
        requestDisallowInterceptTouchEvent(true);
    }

    public void expansionStateChanged(boolean z) {
        this.mExpandingNotification = z;
        if (!this.mExpandedInThisMotion) {
            this.mMaxScrollAfterExpand = this.mOwnScrollY;
            this.mExpandedInThisMotion = true;
        }
    }

    public int getMaxExpandHeight(ExpandableView expandableView) {
        return expandableView.getMaxContentHeight();
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
        int i2 = this.mScrollX;
        int i3 = this.mOwnScrollY;
        overScroller.startScroll(i2, i3, 0, targetScrollForView - i3);
        this.mDontReportNextOverScroll = true;
        animateScroll();
        return true;
    }

    private int targetScrollForView(ExpandableView expandableView, int i) {
        return (((i + expandableView.getIntrinsicHeight()) + getImeInset()) - getHeight()) + getTopPadding();
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        this.mBottomInset = windowInsets.getSystemWindowInsetBottom();
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

    public boolean canChildBeDismissed(View view) {
        return StackScrollAlgorithm.canChildBeDismissed(view);
    }

    public boolean isAntiFalsingNeeded() {
        return onKeyguard();
    }

    private boolean onKeyguard() {
        return this.mStatusBarState == 1;
    }

    private void setSwipingInProgress(boolean z) {
        this.mSwipingInProgress = z;
        if (z) {
            requestDisallowInterceptTouchEvent(true);
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mIsLandscape = configuration.orientation == 2;
        this.mSwipeHelper.setDensityScale(getResources().getDisplayMetrics().density);
        this.mSwipeHelper.setPagingTouchSlop((float) ViewConfiguration.get(getContext()).getScaledPagingTouchSlop());
        initView(getContext());
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mOrientation = i2;
            switchNavigationBarModeIfNeed();
            disallowMeasureChildren(true);
            resetExposedMenuView(true, true);
        }
    }

    public void snapViewIfNeeded(ExpandableNotificationRow expandableNotificationRow) {
        this.mSwipeHelper.snapChildIfNeeded(expandableNotificationRow, this.mIsExpanded || isPinnedHeadsUp(expandableNotificationRow), expandableNotificationRow.getMenu().isMenuVisible() ? expandableNotificationRow.getTranslation() : 0.0f);
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        QS qs = this.mQs;
        if (qs == null || (!qs.isShowingDetail() && !this.mQs.isCustomizing())) {
            return super.dispatchTouchEvent(motionEvent);
        }
        return false;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z;
        boolean z2 = motionEvent.getActionMasked() == 3 || motionEvent.getActionMasked() == 1;
        handleEmptySpaceClick(motionEvent);
        if (!this.mIsExpanded || this.mSwipingInProgress || this.mOnlyScrollingInThisMotion || this.mIsQsBeingCovered) {
            z = false;
        } else {
            if (z2) {
                this.mExpandHelper.onlyObserveMovements(false);
            }
            boolean z3 = this.mExpandingNotification;
            z = this.mExpandHelper.onTouchEvent(motionEvent);
            if (this.mExpandedInThisMotion && !this.mExpandingNotification && z3 && !this.mDisallowScrollingInThisMotion) {
                dispatchDownEventToScroller(motionEvent);
            }
        }
        boolean onScrollTouch = (!this.mIsExpanded || this.mSwipingInProgress || this.mExpandingNotification || this.mDisallowScrollingInThisMotion) ? false : onScrollTouch(motionEvent);
        boolean onTouchEvent = (this.mIsBeingDragged || this.mExpandingNotification || this.mExpandedInThisMotion || this.mOnlyScrollingInThisMotion || this.mDisallowDismissInThisMotion) ? false : this.mSwipeHelper.onTouchEvent(motionEvent);
        NotificationGuts exposedGuts = this.mStatusBar.getExposedGuts();
        if (exposedGuts != null && !isTouchInView(motionEvent, exposedGuts) && (exposedGuts.getGutsContent() instanceof NotificationSnooze) && ((((NotificationSnooze) exposedGuts.getGutsContent()).isExpanded() && z2) || (!onTouchEvent && onScrollTouch))) {
            checkSnoozeLeavebehind();
        }
        if (motionEvent.getActionMasked() == 1) {
            this.mCheckForLeavebehind = true;
        }
        if (onTouchEvent || onScrollTouch || z || super.onTouchEvent(motionEvent)) {
            return true;
        }
        return false;
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
        int action = motionEvent.getAction() & 255;
        if (action != 0) {
            if (action == 1) {
                VelocityTracker velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                int yVelocity = (int) velocityTracker.getYVelocity(this.mActivePointerId);
                if (this.mIsQsBeingCovered) {
                    endQsBeingCoveredMotion(yVelocity);
                    this.mActivePointerId = -1;
                    endDrag();
                } else if (this.mIsBeingDragged) {
                    if (shouldOverScrollFling(yVelocity)) {
                        onOverScrollFling(true, yVelocity);
                    } else if (getChildCount() > 0) {
                        if (Math.abs(yVelocity) > this.mMinimumVelocity) {
                            if (getCurrentOverScrollAmount(true) == 0.0f || yVelocity > 0) {
                                fling(-yVelocity);
                            } else {
                                onOverScrollFling(false, yVelocity);
                            }
                        } else if (this.mScroller.springBack(this.mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                            animateScroll();
                        }
                    }
                    this.mActivePointerId = -1;
                    endDrag();
                }
            } else if (action == 2) {
                int findPointerIndex = motionEvent.findPointerIndex(this.mActivePointerId);
                if (findPointerIndex == -1) {
                    Log.e("StackScroller", "Invalid pointerId=" + this.mActivePointerId + " in onTouchEvent");
                } else {
                    int y = (int) motionEvent.getY(findPointerIndex);
                    int i = this.mLastMotionY - y;
                    int abs = Math.abs(((int) motionEvent.getX(findPointerIndex)) - this.mDownX);
                    int abs2 = Math.abs(i);
                    if (!this.mIsBeingDragged && abs2 > this.mTouchSlop && abs2 > abs) {
                        setIsBeingDragged(true);
                        if (!((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter() && ((isInContentBounds(motionEvent) || this.mIsQsCovered) && isScrolledToTop() && ((i > 0 && canScrollUp() && !isQsCovered()) || (i < 0 && isQsCovered())))) {
                            this.mIsQsBeingCovered = true;
                            this.mIsQsCovered = false;
                        }
                        i = i > 0 ? i - this.mTouchSlop : i + this.mTouchSlop;
                    }
                    if (this.mIsQsBeingCovered) {
                        ValueAnimator valueAnimator = this.mQsBeingCoveredAnimator;
                        if (valueAnimator != null) {
                            valueAnimator.cancel();
                            this.mQsBeingCoveredAnimator = null;
                            this.mIsQsBeingCovered = true;
                        }
                        this.mLastMotionY = y;
                        notifyTopPaddingUpdateListener(this.mTopPadding - i);
                    } else if (this.mIsBeingDragged) {
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
                    }
                }
            } else if (action != 3) {
                if (action == 5) {
                    int actionIndex = motionEvent.getActionIndex();
                    this.mLastMotionY = (int) motionEvent.getY(actionIndex);
                    this.mDownX = (int) motionEvent.getX(actionIndex);
                    this.mActivePointerId = motionEvent.getPointerId(actionIndex);
                } else if (action == 6) {
                    onSecondaryPointerUp(motionEvent);
                    int findPointerIndex2 = motionEvent.findPointerIndex(this.mActivePointerId);
                    if (findPointerIndex2 != -1) {
                        this.mLastMotionY = (int) motionEvent.getY(findPointerIndex2);
                        this.mDownX = (int) motionEvent.getX(findPointerIndex2);
                    } else {
                        Log.e("StackScroller", "Invalid pointerId= " + this.mActivePointerId + " in onTouchScroll:ACTION_POINTER_UP");
                    }
                }
            } else if (this.mIsQsBeingCovered) {
                endQsBeingCoveredMotion(0);
                this.mActivePointerId = -1;
                endDrag();
            } else if (this.mIsBeingDragged && getChildCount() > 0) {
                if (this.mScroller.springBack(this.mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                    animateScroll();
                }
                this.mActivePointerId = -1;
                endDrag();
            }
        } else if (getChildCount() == 0 || (!isInContentBounds(motionEvent) && !this.mIsQsCovered)) {
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

    /* access modifiers changed from: protected */
    public boolean isInsideQsContainer(MotionEvent motionEvent) {
        return motionEvent.getY() < ((float) this.mQs.getView().getBottom());
    }

    private void onOverScrollFling(boolean z, int i) {
        OnOverscrollTopChangedListener onOverscrollTopChangedListener = this.mOverscrollTopChangedListener;
        if (onOverscrollTopChangedListener != null) {
            onOverscrollTopChangedListener.flingTopOverscroll((float) i, z);
        }
        this.mDontReportNextOverScroll = true;
        setOverScrollAmount(0.0f, true, false);
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
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).onScrollMore();
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
    public void animateScroll() {
        if (this.mScroller.computeScrollOffset()) {
            int i = this.mOwnScrollY;
            int currY = this.mScroller.getCurrY();
            if (i != currY) {
                int scrollRange = getScrollRange();
                if ((currY < 0 && i >= 0) || (currY > scrollRange && i <= scrollRange)) {
                    float currVelocity = this.mScroller.getCurrVelocity();
                    if (currVelocity >= ((float) this.mMinimumVelocity)) {
                        this.mMaxOverScroll = (Math.abs(currVelocity) / 1000.0f) * ((float) this.mOverflingDistance);
                    }
                }
                if (this.mDontClampNextScroll) {
                    scrollRange = Math.max(scrollRange, i);
                }
                customOverScrollBy(currY - i, i, scrollRange, (int) this.mMaxOverScroll);
            }
            postOnAnimation(this.mAnimateScroll);
            return;
        }
        this.mDontClampNextScroll = false;
        Runnable runnable = this.mFinishScrollingCallback;
        if (runnable != null) {
            runnable.run();
        }
    }

    private boolean customOverScrollBy(int i, int i2, int i3, int i4) {
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
        return z;
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
    public int getScrollRange() {
        int max = Math.max(0, (this.mContentHeight - this.mMaxLayoutHeight) + (isQsCovered() ? this.mExtraBottomRangeQsCovered : this.mExtraBottomRange));
        int imeInset = getImeInset();
        return max + Math.min(imeInset, Math.max(0, this.mContentHeight - (getHeight() - imeInset)));
    }

    private int getImeInset() {
        return Math.max(0, this.mBottomInset - (getRootView().getHeight() - getHeight()));
    }

    public ExpandableView getFirstChildNotGone() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() != 8 && childAt != this.mShelf) {
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
                        List<ExpandableNotificationRow> notificationChildren = expandableNotificationRow.getNotificationChildren();
                        for (int i2 = 0; i2 < notificationChildren.size(); i2++) {
                            ExpandableNotificationRow expandableNotificationRow2 = notificationChildren.get(i2);
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

    public List<NotificationData.Entry> getNotGoneNotifications() {
        ArrayList arrayList = new ArrayList();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i);
            if (expandableView.getVisibility() != 8 && !expandableView.willBeGone() && (expandableView instanceof ExpandableNotificationRow)) {
                arrayList.add(((ExpandableNotificationRow) expandableView).getEntry());
            }
        }
        return arrayList;
    }

    public int getContentHeight() {
        return this.mContentHeight;
    }

    private void updateContentHeight() {
        int i;
        float f;
        float f2;
        float f3 = (float) this.mPaddingBetweenElements;
        if (this.mAmbientState.isDark()) {
            i = hasPulsingNotifications() ? 1 : 0;
        } else {
            i = this.mMaxDisplayedNotifications;
        }
        int i2 = 0;
        int i3 = 0;
        boolean z = false;
        float f4 = 0.0f;
        for (int i4 = 0; i4 < getChildCount(); i4++) {
            ExpandableView expandableView = (ExpandableView) getChildAt(i4);
            if (expandableView.getVisibility() != 8 && !expandableView.hasNoContentHeight()) {
                boolean z2 = i != -1 && i2 >= i;
                boolean z3 = this.mAmbientState.isDark() && hasPulsingNotifications() && (expandableView instanceof ExpandableNotificationRow) && !isPulsing(((ExpandableNotificationRow) expandableView).getEntry());
                if (z2 || z3) {
                    expandableView = this.mShelf;
                    z = true;
                }
                float increasedPaddingAmount = expandableView.getIncreasedPaddingAmount();
                if (increasedPaddingAmount >= 0.0f) {
                    f2 = (float) ((int) NotificationUtils.interpolate(f3, (float) this.mIncreasedPaddingBetweenElements, increasedPaddingAmount));
                    f = (float) ((int) NotificationUtils.interpolate((float) this.mPaddingBetweenElements, (float) this.mIncreasedPaddingBetweenElements, increasedPaddingAmount));
                } else {
                    int interpolate = (int) NotificationUtils.interpolate(0.0f, (float) this.mPaddingBetweenElements, 1.0f + increasedPaddingAmount);
                    float interpolate2 = f4 > 0.0f ? (float) ((int) NotificationUtils.interpolate((float) interpolate, (float) this.mIncreasedPaddingBetweenElements, f4)) : (float) interpolate;
                    f = (float) interpolate;
                    f2 = interpolate2;
                }
                if (i3 != 0) {
                    i3 = (int) (((float) i3) + f2);
                }
                i3 += expandableView.getIntrinsicHeight();
                i2++;
                if (z) {
                    break;
                }
                f3 = f;
                f4 = increasedPaddingAmount;
            }
        }
        this.mContentHeight = i3 + this.mTopPadding + this.mSpringIncrement;
        updateScrollability();
        this.mAmbientState.setLayoutMaxHeight(this.mContentHeight);
    }

    private boolean isPulsing(NotificationData.Entry entry) {
        for (HeadsUpManager.HeadsUpEntry headsUpEntry : this.mPulsing) {
            if (headsUpEntry.entry == entry) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPulsingNotifications() {
        return this.mPulsing != null;
    }

    private void updateScrollability() {
        boolean z = getScrollRange() > 0;
        if (z != this.mScrollable) {
            this.mScrollable = z;
            setFocusable(z);
            updateForwardAndBackwardScrollability();
        }
    }

    private void updateForwardAndBackwardScrollability() {
        boolean z = true;
        boolean z2 = this.mScrollable && this.mOwnScrollY < getScrollRange();
        boolean z3 = this.mScrollable && this.mOwnScrollY > 0;
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
        if (this.mShouldDrawNotificationBackground && !this.mAmbientState.isDark()) {
            updateBackgroundBounds();
            if (!this.mCurrentBounds.equals(this.mBackgroundBounds)) {
                boolean z = this.mAnimateNextBackgroundTop || this.mAnimateNextBackgroundBottom || areBoundsAnimating();
                if (!isExpanded()) {
                    abortBackgroundAnimators();
                    z = false;
                }
                if (z) {
                    startBackgroundAnimation();
                } else {
                    this.mCurrentBounds.set(this.mBackgroundBounds);
                    applyCurrentBackgroundBounds();
                }
            } else {
                abortBackgroundAnimators();
            }
            this.mAnimateNextBackgroundBottom = false;
            this.mAnimateNextBackgroundTop = false;
        }
    }

    private void abortBackgroundAnimators() {
        ObjectAnimator objectAnimator = this.mBottomAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        ObjectAnimator objectAnimator2 = this.mTopAnimator;
        if (objectAnimator2 != null) {
            objectAnimator2.cancel();
        }
    }

    private boolean areBoundsAnimating() {
        return (this.mBottomAnimator == null && this.mTopAnimator == null) ? false : true;
    }

    private void startBackgroundAnimation() {
        Rect rect = this.mCurrentBounds;
        Rect rect2 = this.mBackgroundBounds;
        rect.left = rect2.left;
        rect.right = rect2.right;
        startBottomAnimation();
        startTopAnimation();
    }

    private void startTopAnimation() {
        int i = this.mEndAnimationRect.top;
        int i2 = this.mBackgroundBounds.top;
        ObjectAnimator objectAnimator = this.mTopAnimator;
        if (objectAnimator != null && i == i2) {
            return;
        }
        if (this.mAnimateNextBackgroundTop) {
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            ObjectAnimator ofInt = ObjectAnimator.ofInt(this, "backgroundTop", new int[]{this.mCurrentBounds.top, i2});
            ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofInt.setDuration(360);
            ofInt.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    NotificationStackScrollLayout.this.mStartAnimationRect.top = -1;
                    NotificationStackScrollLayout.this.mEndAnimationRect.top = -1;
                    ObjectAnimator unused = NotificationStackScrollLayout.this.mTopAnimator = null;
                }
            });
            ofInt.start();
            this.mStartAnimationRect.top = this.mCurrentBounds.top;
            this.mEndAnimationRect.top = i2;
            this.mTopAnimator = ofInt;
        } else if (objectAnimator != null) {
            int i3 = this.mStartAnimationRect.top;
            objectAnimator.getValues()[0].setIntValues(new int[]{i3, i2});
            this.mStartAnimationRect.top = i3;
            this.mEndAnimationRect.top = i2;
            objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
        } else {
            setBackgroundTop(i2);
        }
    }

    private void startBottomAnimation() {
        int i = this.mStartAnimationRect.bottom;
        int i2 = this.mEndAnimationRect.bottom;
        int i3 = this.mBackgroundBounds.bottom;
        ObjectAnimator objectAnimator = this.mBottomAnimator;
        if (objectAnimator != null && i2 == i3) {
            return;
        }
        if (this.mAnimateNextBackgroundBottom) {
            if (objectAnimator != null) {
                objectAnimator.cancel();
            }
            ObjectAnimator ofInt = ObjectAnimator.ofInt(this, "backgroundBottom", new int[]{this.mCurrentBounds.bottom, i3});
            ofInt.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            ofInt.setDuration(360);
            ofInt.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    NotificationStackScrollLayout.this.mStartAnimationRect.bottom = -1;
                    NotificationStackScrollLayout.this.mEndAnimationRect.bottom = -1;
                    ObjectAnimator unused = NotificationStackScrollLayout.this.mBottomAnimator = null;
                }
            });
            ofInt.start();
            this.mStartAnimationRect.bottom = this.mCurrentBounds.bottom;
            this.mEndAnimationRect.bottom = i3;
            this.mBottomAnimator = ofInt;
        } else if (objectAnimator != null) {
            objectAnimator.getValues()[0].setIntValues(new int[]{i, i3});
            this.mStartAnimationRect.bottom = i;
            this.mEndAnimationRect.bottom = i3;
            objectAnimator.setCurrentPlayTime(objectAnimator.getCurrentPlayTime());
        } else {
            setBackgroundBottom(i3);
        }
    }

    private void setBackgroundTop(int i) {
        this.mCurrentBounds.top = i;
        applyCurrentBackgroundBounds();
    }

    public void setBackgroundBottom(int i) {
        this.mCurrentBounds.bottom = i;
        applyCurrentBackgroundBounds();
    }

    private void applyCurrentBackgroundBounds() {
        switchNavigationBarModeIfNeed();
        if (this.mShouldDrawNotificationBackground) {
            this.mScrimController.setExcludedBackgroundArea((this.mFadingOut || this.mParentNotFullyVisible || this.mAmbientState.isDark() || this.mIsClipped) ? null : this.mCurrentBounds);
            invalidate();
        }
    }

    private void updateBackgroundBounds() {
        int i;
        ExpandableView expandableView;
        int i2;
        int i3;
        float f;
        if (this.mAmbientState.isPanelFullWidth()) {
            Rect rect = this.mBackgroundBounds;
            rect.left = 0;
            rect.right = getWidth();
        } else {
            getLocationInWindow(this.mTempInt2);
            Rect rect2 = this.mBackgroundBounds;
            int[] iArr = this.mTempInt2;
            rect2.left = iArr[0];
            rect2.right = iArr[0] + getWidth();
        }
        if (!this.mIsExpanded) {
            Rect rect3 = this.mBackgroundBounds;
            int i4 = this.mTopPadding;
            rect3.top = i4;
            rect3.bottom = i4;
            return;
        }
        ExpandableView expandableView2 = this.mFirstVisibleBackgroundChild;
        if (expandableView2 != null) {
            i = (int) Math.ceil((double) ViewState.getFinalTranslationY(expandableView2));
            if (!this.mAnimateNextBackgroundTop && (!(this.mTopAnimator == null && this.mCurrentBounds.top == i) && (this.mTopAnimator == null || this.mEndAnimationRect.top != i))) {
                i = (int) Math.ceil((double) expandableView2.getTranslationY());
            }
        } else {
            i = 0;
        }
        if (this.mShelf.hasItemsInStableShelf()) {
            expandableView = this.mShelf;
        } else {
            expandableView = this.mLastVisibleBackgroundChild;
        }
        if (expandableView != null) {
            NotificationShelf notificationShelf = this.mShelf;
            if (expandableView == notificationShelf) {
                f = notificationShelf.getTranslationY();
            } else {
                f = ViewState.getFinalTranslationY(expandableView);
            }
            i2 = (((int) f) + ExpandableViewState.getFinalActualHeight(expandableView)) - expandableView.getClipBottomAmount();
            if (!this.mAnimateNextBackgroundBottom && (!(this.mBottomAnimator == null && this.mCurrentBounds.bottom == i2) && (this.mBottomAnimator == null || this.mEndAnimationRect.bottom != i2))) {
                i2 = (int) ((expandableView.getTranslationY() + ((float) expandableView.getActualHeight())) - ((float) expandableView.getClipBottomAmount()));
            }
        } else {
            i = this.mTopPadding;
            i2 = i;
        }
        if (this.mStatusBarState != 1) {
            i3 = (int) Math.max(((float) this.mTopPadding) + this.mStackTranslation, (float) i);
        } else {
            i3 = Math.max(0, i);
        }
        Rect rect4 = this.mBackgroundBounds;
        rect4.top = i3;
        rect4.bottom = Math.max(i2, i3);
    }

    private ExpandableView getLastChildWithBackground() {
        for (int childCount = getChildCount() - 1; childCount >= 0; childCount--) {
            if (getChildAt(childCount) instanceof ExpandableView) {
                ExpandableView expandableView = (ExpandableView) getChildAt(childCount);
                if (!(expandableView.getVisibility() == 8 || expandableView.getViewType() == 1 || expandableView.getViewType() == 2)) {
                    return expandableView;
                }
            }
        }
        return null;
    }

    private ExpandableView getFirstChildWithBackground() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i) instanceof ExpandableView) {
                ExpandableView expandableView = (ExpandableView) getChildAt(i);
                if (!(expandableView.getVisibility() == 8 || expandableView.getViewType() == 1 || expandableView.getViewType() == 2)) {
                    return expandableView;
                }
            }
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public void fling(int i) {
        if (getChildCount() > 0) {
            int scrollRange = getScrollRange();
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
            int max = Math.max(0, scrollRange);
            if (this.mExpandedInThisMotion) {
                max = Math.min(max, this.mMaxScrollAfterExpand);
            }
            int i3 = max;
            OverScroller overScroller = this.mScroller;
            int i4 = this.mScrollX;
            int i5 = this.mOwnScrollY;
            if (!this.mExpandedInThisMotion || i5 < 0) {
                i2 = 1073741823;
            }
            overScroller.fling(i4, i5, 1, i, 0, 0, 0, i3, 0, i2);
            animateScroll();
        }
    }

    private boolean shouldOverScrollFling(int i) {
        float currentOverScrollAmount = getCurrentOverScrollAmount(true);
        if (!this.mScrolledToTopOnFirstDown || this.mExpandedInThisMotion || currentOverScrollAmount <= this.mMinTopOverScrollToEscape || i <= 0) {
            return false;
        }
        return true;
    }

    public void onSpringLengthUpdated(float f) {
        if (DEBUG) {
            Log.d("StackScroller", "onSpringLengthUpdated " + f);
        }
        this.mSpringLength = (int) f;
        this.mSpringIncrement = calculateSpringIncrement();
        if (!this.mSpringAnimationRunning) {
            this.mAmbientState.setSpringLength(this.mSpringLength);
        }
        setExpandedHeight(this.mExpandedHeight);
        updateContentHeight();
    }

    private int calculateSpringIncrement() {
        if (!this.mSpringAnimationRunning) {
            this.mSpringIncrement = this.mSpringLength * 2;
        }
        return this.mSpringIncrement;
    }

    public void onSpringAnimationStart() {
        if (DEBUG) {
            Log.d("StackScroller", "onSpringAnimationEnd");
        }
        this.mSpringAnimationRunning = true;
        this.mAmbientState.setSpringLength(0);
        generateChildSpringResetAnimation();
    }

    public void onChildrenSpringAnimationUpdate() {
        requestChildrenUpdate();
    }

    public void onSpringAnimationEnd() {
        if (DEBUG) {
            Log.d("StackScroller", "onSpringAnimationEnd");
        }
        this.mSpringAnimationRunning = false;
        onSpringLengthUpdated(0.0f);
    }

    public void onSpringAnimationCanceled() {
        this.mSpringAnimationRunning = false;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            ViewState.cancelFolmeAnimation(childAt, R.id.folme_spring_reset);
            if (childAt instanceof ExpandableView) {
                ((ExpandableView) childAt).getViewState().cancelAnimations(childAt);
            }
        }
    }

    private void generateChildSpringResetAnimation() {
        int childCount = getChildCount();
        int i = 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = getChildAt(i2);
            if ((childAt instanceof ExpandableNotificationRow) && !isChildInGroup(childAt)) {
                if (this.mChildrenSpringReset.size() < 15) {
                    childAt.setTag(R.id.view_index_tag, Integer.valueOf(i));
                    this.mChildrenSpringReset.add(childAt);
                    i++;
                } else {
                    this.mCurrentStackScrollState.getViewStateForView(childAt).springYOffset = 0;
                }
            }
        }
        if (!this.mChildrenSpringReset.isEmpty()) {
            this.mNeedsAnimation = true;
            requestChildrenUpdate();
        }
    }

    public void onPanelDisplayChanged(boolean z) {
        if (DEBUG) {
            Log.d("StackScroller", "onPanelDisplayChanged " + z);
        }
        this.mAmbientState.setPanelAppear(z);
        generateChildVisibilityAnimation(z);
    }

    private void generateChildVisibilityAnimation(boolean z) {
        int childCount = getChildCount();
        boolean isScrolledToBottom = isScrolledToBottom();
        int i = 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = getChildAt(isScrolledToBottom ? (childCount - 1) - i2 : i2);
            if (childAt instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                if (!expandableNotificationRow.isDismissed() && !expandableNotificationRow.isRemoved() && !isChildInGroup(childAt)) {
                    int i3 = i + 1;
                    childAt.setTag(R.id.view_index_tag, Integer.valueOf(i));
                    if (z) {
                        this.mChildrenAppear.add(childAt);
                    } else {
                        this.mChildrenDisappear.add(childAt);
                    }
                    i = i3;
                }
            }
        }
        if (!this.mChildrenAppear.isEmpty() || !this.mChildrenDisappear.isEmpty()) {
            this.mNeedsAnimation = true;
            requestChildrenUpdate();
        }
    }

    public void updateTopPadding(float f, boolean z, boolean z2) {
        int i = (int) f;
        int layoutMinHeight = getLayoutMinHeight() + i;
        if (layoutMinHeight > getHeight()) {
            this.mTopPaddingOverflow = (float) (layoutMinHeight - getHeight());
        } else {
            this.mTopPaddingOverflow = 0.0f;
        }
        if (!z2) {
            i = clampPadding(i);
        }
        setTopPadding(i, z);
        setExpandedHeight(this.mExpandedHeight);
    }

    public int getLayoutMinHeight() {
        return this.mShelf.getIntrinsicHeight();
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
        if (this.mLastVisibleBackgroundChild != null) {
            i2 = this.mShelf.getIntrinsicHeight();
        }
        return this.mIntrinsicPadding + i + i2;
    }

    private int clampPadding(int i) {
        return Math.max(i, this.mIntrinsicPadding);
    }

    private float getRubberBandFactor(boolean z) {
        if (!z) {
            return 0.35f;
        }
        if (this.mExpandedInThisMotion) {
            return 0.15f;
        }
        if (this.mIsExpansionChanging || this.mPanelTracking) {
            return 0.21f;
        }
        if (this.mScrolledToTopOnFirstDown) {
            return 1.0f;
        }
        return 0.35f;
    }

    private boolean isRubberbanded(boolean z) {
        return !z || this.mExpandedInThisMotion || this.mIsExpansionChanging || this.mPanelTracking || !this.mScrolledToTopOnFirstDown;
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
        boolean onInterceptTouchEvent = (this.mSwipingInProgress || this.mOnlyScrollingInThisMotion) ? false : this.mExpandHelper.onInterceptTouchEvent(motionEvent);
        boolean onInterceptTouchEventScroll = (this.mSwipingInProgress || this.mExpandingNotification) ? false : onInterceptTouchEventScroll(motionEvent);
        boolean onInterceptTouchEvent2 = (this.mIsBeingDragged || this.mExpandingNotification || this.mExpandedInThisMotion || this.mOnlyScrollingInThisMotion || this.mDisallowDismissInThisMotion) ? false : this.mSwipeHelper.onInterceptTouchEvent(motionEvent);
        boolean z = motionEvent.getActionMasked() == 1;
        if (!isTouchInView(motionEvent, this.mStatusBar.getExposedGuts()) && z && !onInterceptTouchEvent2 && !onInterceptTouchEvent && !onInterceptTouchEventScroll) {
            this.mCheckForLeavebehind = false;
            this.mStatusBar.closeAndSaveGuts(true, false, false, -1, -1, false);
        }
        if (motionEvent.getActionMasked() == 1) {
            this.mCheckForLeavebehind = true;
        }
        if (DEBUG) {
            Log.d("StackScroller", String.format("onInterceptTouchEvent swipeWantsIt=%b scrollWantsIt=%b expandWantsIt=%b", new Object[]{Boolean.valueOf(onInterceptTouchEvent2), Boolean.valueOf(onInterceptTouchEventScroll), Boolean.valueOf(onInterceptTouchEvent)}));
        }
        if (onInterceptTouchEvent2 || onInterceptTouchEventScroll || onInterceptTouchEvent || super.onInterceptTouchEvent(motionEvent)) {
            return true;
        }
        return false;
    }

    private void handleEmptySpaceClick(MotionEvent motionEvent) {
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 1) {
            if (actionMasked != 2 || !this.mTouchIsClick) {
                return;
            }
            if (Math.abs(motionEvent.getY() - this.mInitialTouchY) > ((float) this.mTouchSlop) || Math.abs(motionEvent.getX() - this.mInitialTouchX) > ((float) this.mTouchSlop)) {
                this.mTouchIsClick = false;
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

    public void setChildTransferInProgress(boolean z) {
        this.mChildTransferInProgress = z;
    }

    public void onViewRemoved(View view) {
        super.onViewRemoved(view);
        if (!this.mChildTransferInProgress) {
            onViewRemovedInternal(view, this);
        }
    }

    public void removeView(View view) {
        if (!(view == null || view.getAnimation() == null)) {
            view.clearAnimation();
        }
        super.removeView(view);
    }

    public void cleanUpViewState(View view) {
        if (view == this.mTranslatingParentView) {
            this.mTranslatingParentView = null;
        }
        this.mCurrentStackScrollState.removeViewStateForView(view);
    }

    public void requestDisallowInterceptTouchEvent(boolean z) {
        super.requestDisallowInterceptTouchEvent(z);
        if (z) {
            this.mSwipeHelper.removeLongPressCallback();
        }
    }

    private void onViewRemovedInternal(View view, ViewGroup viewGroup) {
        if (!this.mChangePositionInProgress) {
            ExpandableView expandableView = (ExpandableView) view;
            expandableView.setOnHeightChangedListener((ExpandableView.OnHeightChangedListener) null);
            this.mCurrentStackScrollState.removeViewStateForView(view);
            updateScrollStateForRemovedChild(expandableView);
            if (!generateRemoveAnimation(view)) {
                this.mSwipedOutViews.remove(view);
            } else if (!this.mSwipedOutViews.contains(view)) {
                viewGroup.getOverlay().add(view);
            } else if (Math.abs(expandableView.getTranslation()) != ((float) expandableView.getWidth())) {
                viewGroup.addTransientView(view, 0);
                expandableView.setTransientContainer(viewGroup);
            }
            updateAnimationState(false, view);
            expandableView.setClipTopAmount(0);
            focusNextViewIfFocused(view);
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
        return (view instanceof ExpandableNotificationRow) && this.mGroupManager.isChildInGroupWithSummary(((ExpandableNotificationRow) view).getStatusBarNotification());
    }

    private boolean generateRemoveAnimation(View view) {
        if (removeRemovedChildFromHeadsUpChangeAnimations(view)) {
            this.mAddedHeadsUpChildren.remove(view);
            return false;
        } else if (isClickedHeadsUp(view)) {
            this.mClearOverlayViewsWhenFinished.add(view);
            return true;
        } else {
            if (this.mIsExpanded && this.mAnimationsEnabled && !isChildInInvisibleGroup(view)) {
                if (!this.mChildrenToAddAnimated.contains(view)) {
                    this.mChildrenToRemoveAnimated.add(view);
                    this.mNeedsAnimation = true;
                    return true;
                }
                this.mChildrenToAddAnimated.remove(view);
                this.mFromMoreCardAdditions.remove(view);
            }
            return false;
        }
    }

    private boolean isClickedHeadsUp(View view) {
        return HeadsUpManager.isClickedHeadsUpNotification(view);
    }

    private boolean removeRemovedChildFromHeadsUpChangeAnimations(View view) {
        Iterator<Pair<ExpandableNotificationRow, Boolean>> it = this.mHeadsUpChangeAnimations.iterator();
        boolean z = false;
        while (it.hasNext()) {
            Pair next = it.next();
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
        ExpandableNotificationRow groupSummary = this.mGroupManager.getGroupSummary((StatusBarNotification) expandableNotificationRow.getStatusBarNotification());
        if (groupSummary == null || groupSummary == expandableNotificationRow || expandableNotificationRow.getVisibility() != 4) {
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

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v1, resolved type: android.view.View} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: com.android.systemui.statusbar.ExpandableNotificationRow} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: com.android.systemui.statusbar.ExpandableNotificationRow} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r15v2, resolved type: android.view.View} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: com.android.systemui.statusbar.ExpandableNotificationRow} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int getPositionInLinearLayout(android.view.View r15) {
        /*
            r14 = this;
            boolean r0 = r14.isChildInGroup(r15)
            r1 = 0
            if (r0 == 0) goto L_0x0010
            r1 = r15
            com.android.systemui.statusbar.ExpandableNotificationRow r1 = (com.android.systemui.statusbar.ExpandableNotificationRow) r1
            com.android.systemui.statusbar.ExpandableNotificationRow r15 = r1.getNotificationParent()
            r0 = r15
            goto L_0x0011
        L_0x0010:
            r0 = r1
        L_0x0011:
            int r2 = r14.mPaddingBetweenElements
            float r2 = (float) r2
            r3 = 0
            r4 = 0
            r6 = r3
            r5 = r4
            r7 = r5
        L_0x0019:
            int r8 = r14.getChildCount()
            if (r5 >= r8) goto L_0x0091
            android.view.View r8 = r14.getChildAt(r5)
            com.android.systemui.statusbar.ExpandableView r8 = (com.android.systemui.statusbar.ExpandableView) r8
            int r9 = r8.getVisibility()
            r10 = 8
            if (r9 == r10) goto L_0x002f
            r9 = 1
            goto L_0x0030
        L_0x002f:
            r9 = r4
        L_0x0030:
            if (r9 == 0) goto L_0x007d
            boolean r10 = r8.hasNoContentHeight()
            if (r10 != 0) goto L_0x007d
            float r10 = r8.getIncreasedPaddingAmount()
            int r11 = (r10 > r3 ? 1 : (r10 == r3 ? 0 : -1))
            if (r11 < 0) goto L_0x0056
            int r6 = r14.mIncreasedPaddingBetweenElements
            float r6 = (float) r6
            float r2 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r2, r6, r10)
            int r2 = (int) r2
            float r2 = (float) r2
            int r6 = r14.mPaddingBetweenElements
            float r6 = (float) r6
            int r11 = r14.mIncreasedPaddingBetweenElements
            float r11 = (float) r11
            float r6 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r6, r11, r10)
            int r6 = (int) r6
            float r6 = (float) r6
            goto L_0x0075
        L_0x0056:
            int r2 = r14.mPaddingBetweenElements
            float r2 = (float) r2
            r11 = 1065353216(0x3f800000, float:1.0)
            float r11 = r11 + r10
            float r2 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r3, r2, r11)
            int r2 = (int) r2
            int r11 = (r6 > r3 ? 1 : (r6 == r3 ? 0 : -1))
            if (r11 <= 0) goto L_0x0070
            float r11 = (float) r2
            int r12 = r14.mIncreasedPaddingBetweenElements
            float r12 = (float) r12
            float r6 = com.android.systemui.statusbar.notification.NotificationUtils.interpolate(r11, r12, r6)
            int r6 = (int) r6
            float r6 = (float) r6
            goto L_0x0071
        L_0x0070:
            float r6 = (float) r2
        L_0x0071:
            float r2 = (float) r2
            r13 = r6
            r6 = r2
            r2 = r13
        L_0x0075:
            if (r7 == 0) goto L_0x007b
            float r7 = (float) r7
            float r7 = r7 + r2
            int r2 = (int) r7
            r7 = r2
        L_0x007b:
            r2 = r6
            r6 = r10
        L_0x007d:
            if (r8 != r15) goto L_0x0087
            if (r0 == 0) goto L_0x0086
            int r14 = r0.getPositionOfChild(r1)
            int r7 = r7 + r14
        L_0x0086:
            return r7
        L_0x0087:
            if (r9 == 0) goto L_0x008e
            int r8 = r14.getIntrinsicHeight(r8)
            int r7 = r7 + r8
        L_0x008e:
            int r5 = r5 + 1
            goto L_0x0019
        L_0x0091:
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.getPositionInLinearLayout(android.view.View):int");
    }

    public void onViewAdded(View view) {
        super.onViewAdded(view);
        onViewAddedInternal(view);
    }

    private void updateFirstAndLastBackgroundViews() {
        ExpandableView firstChildWithBackground = getFirstChildWithBackground();
        ExpandableView lastChildWithBackground = getLastChildWithBackground();
        boolean z = false;
        if (!this.mAnimationsEnabled || !this.mIsExpanded) {
            this.mAnimateNextBackgroundTop = false;
            this.mAnimateNextBackgroundBottom = false;
        } else {
            this.mAnimateNextBackgroundTop = firstChildWithBackground != this.mFirstVisibleBackgroundChild;
            if (lastChildWithBackground != this.mLastVisibleBackgroundChild) {
                z = true;
            }
            this.mAnimateNextBackgroundBottom = z;
        }
        this.mFirstVisibleBackgroundChild = firstChildWithBackground;
        this.mLastVisibleBackgroundChild = lastChildWithBackground;
        this.mAmbientState.setLastVisibleBackgroundChild(lastChildWithBackground);
    }

    private void onViewAddedInternal(View view) {
        updateHideSensitiveForChild(view);
        ((ExpandableView) view).setOnHeightChangedListener(this);
        generateAddAnimation(view, false);
        updateAnimationState(view);
        updateChronometerForChild(view);
    }

    private void updateHideSensitiveForChild(View view) {
        if (view instanceof ExpandableView) {
            ((ExpandableView) view).setHideSensitiveForIntrinsicHeight(this.mAmbientState.isHideSensitive());
        }
    }

    public void notifyGroupChildRemoved(View view, ViewGroup viewGroup) {
        onViewRemovedInternal(view, viewGroup);
    }

    public void notifyGroupChildAdded(View view) {
        onViewAddedInternal(view);
    }

    public void setAnimationsEnabled(boolean z) {
        this.mAnimationsEnabled = z;
        updateNotificationAnimationStates();
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

    private void updateAnimationState(boolean z, View view) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).setIconAnimationRunning(z);
        }
    }

    public boolean isAddOrRemoveAnimationPending() {
        return this.mNeedsAnimation && (!this.mChildrenToAddAnimated.isEmpty() || !this.mChildrenToRemoveAnimated.isEmpty());
    }

    public void generateAddAnimation(View view, boolean z) {
        if (this.mIsExpanded && this.mAnimationsEnabled && !this.mChangePositionInProgress) {
            this.mChildrenToAddAnimated.add(view);
            if (z) {
                this.mFromMoreCardAdditions.add(view);
            }
            this.mNeedsAnimation = true;
        }
        if (isHeadsUp(view) && this.mAnimationsEnabled && !this.mChangePositionInProgress) {
            this.mAddedHeadsUpChildren.add(view);
            this.mChildrenToAddAnimated.remove(view);
        }
    }

    public void changeViewPosition(View view, int i) {
        int indexOfChild = indexOfChild(view);
        if (view != null && view.getParent() == this && indexOfChild != i) {
            this.mChangePositionInProgress = true;
            ExpandableView expandableView = (ExpandableView) view;
            expandableView.setChangingPosition(true);
            removeView(view);
            addView(view, i);
            expandableView.setChangingPosition(false);
            this.mChangePositionInProgress = false;
            if (this.mIsExpanded && this.mAnimationsEnabled && view.getVisibility() != 8) {
                this.mChildrenChangingPositions.add(view);
                this.mNeedsAnimation = true;
            }
        }
    }

    private void startAnimationToState() {
        if (this.mNeedsAnimation) {
            generateChildHierarchyEvents();
            this.mNeedsAnimation = false;
        }
        if (!this.mAnimationEvents.isEmpty() || isCurrentlyAnimating()) {
            if (DEBUG) {
                Log.d("StackScroller", "startAnimationToState " + this.mAnimationEvents);
            }
            setAnimationRunning(true);
            this.mStateAnimator.startAnimationForEvents(this.mAnimationEvents, this.mCurrentStackScrollState, this.mGoToFullShadeDelay);
            this.mAnimationEvents.clear();
            updateBackground();
            updateViewShadows();
        } else {
            applyCurrentState();
        }
        this.mGoToFullShadeDelay = 0;
    }

    private void generateChildHierarchyEvents() {
        generateHeadsUpAnimationEvents();
        generateChildRemovalEvents();
        generateChildAdditionEvents();
        generatePositionChangeEvents();
        generateSnapBackEvents();
        generateDragEvents();
        generateTopPaddingEvent();
        generateActivateEvent();
        generateDimmedEvent();
        generateHideSensitiveEvent();
        generateDarkEvent();
        generateGoToFullShadeEvent();
        generateViewResizeEvent();
        generateGroupExpansionEvent();
        generateChildVisibilityEvent();
        generateChildSpringResetEvent();
        generateAnimateEverythingEvent();
    }

    private void generateChildVisibilityEvent() {
        Iterator<View> it = this.mChildrenAppear.iterator();
        while (it.hasNext()) {
            this.mAnimationEvents.add(new AnimationEvent(it.next(), 18));
        }
        Iterator<View> it2 = this.mChildrenDisappear.iterator();
        while (it2.hasNext()) {
            this.mAnimationEvents.add(new AnimationEvent(it2.next(), 19));
        }
        this.mChildrenAppear.clear();
        this.mChildrenDisappear.clear();
    }

    private void generateChildSpringResetEvent() {
        Iterator<View> it = this.mChildrenSpringReset.iterator();
        while (it.hasNext()) {
            this.mAnimationEvents.add(new AnimationEvent(it.next(), 20));
        }
        this.mChildrenSpringReset.clear();
    }

    private void generateHeadsUpAnimationEvents() {
        Iterator<Pair<ExpandableNotificationRow, Boolean>> it = this.mHeadsUpChangeAnimations.iterator();
        while (it.hasNext()) {
            Pair next = it.next();
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) next.first;
            boolean booleanValue = ((Boolean) next.second).booleanValue();
            int i = 17;
            boolean z = false;
            boolean z2 = expandableNotificationRow.isPinned() && !this.mIsExpanded;
            if (this.mIsExpanded || booleanValue) {
                ExpandableViewState viewStateForView = this.mCurrentStackScrollState.getViewStateForView(expandableNotificationRow);
                if (viewStateForView != null) {
                    if (booleanValue && (this.mAddedHeadsUpChildren.contains(expandableNotificationRow) || z2)) {
                        i = (z2 || shouldHunAppearFromBottom(viewStateForView)) ? 14 : 0;
                        z = !z2;
                    }
                }
            } else {
                i = expandableNotificationRow.wasJustClicked() ? 16 : 15;
                if (expandableNotificationRow.isChildInGroup()) {
                    expandableNotificationRow.setHeadsUpAnimatingAway(false);
                }
            }
            AnimationEvent animationEvent = new AnimationEvent(expandableNotificationRow, i);
            animationEvent.headsUpFromBottom = z;
            this.mAnimationEvents.add(animationEvent);
        }
        this.mHeadsUpChangeAnimations.clear();
        this.mAddedHeadsUpChildren.clear();
    }

    private boolean shouldHunAppearFromBottom(ExpandableViewState expandableViewState) {
        return expandableViewState.yTranslation + ((float) expandableViewState.height) >= this.mAmbientState.getMaxHeadsUpTranslation();
    }

    private void generateGroupExpansionEvent() {
        if (this.mExpandedGroupView != null) {
            this.mAnimationEvents.add(new AnimationEvent(this.mExpandedGroupView, 13));
            this.mExpandedGroupView = null;
        }
    }

    private void generateViewResizeEvent() {
        if (this.mNeedViewResizeAnimation) {
            this.mAnimationEvents.add(new AnimationEvent((View) null, 12));
        }
        this.mNeedViewResizeAnimation = false;
    }

    private void generateSnapBackEvents() {
        Iterator<View> it = this.mSnappedBackChildren.iterator();
        while (it.hasNext()) {
            this.mAnimationEvents.add(new AnimationEvent(it.next(), 5));
        }
        this.mSnappedBackChildren.clear();
    }

    private void generateDragEvents() {
        Iterator<View> it = this.mDragAnimPendingChildren.iterator();
        while (it.hasNext()) {
            this.mAnimationEvents.add(new AnimationEvent(it.next(), 4));
        }
        this.mDragAnimPendingChildren.clear();
    }

    private void generateChildRemovalEvents() {
        boolean z;
        Iterator<View> it = this.mChildrenToRemoveAnimated.iterator();
        while (it.hasNext()) {
            View next = it.next();
            AnimationEvent animationEvent = new AnimationEvent(next, this.mSwipedOutViews.contains(next) ? 2 : 1);
            float translationY = next.getTranslationY();
            if (next instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) next;
                if (expandableNotificationRow.isRemoved() && expandableNotificationRow.wasChildInGroupWhenRemoved()) {
                    translationY = expandableNotificationRow.getTranslationWhenRemoved();
                    z = false;
                    animationEvent.viewAfterChangingView = getFirstChildBelowTranlsationY(translationY, z);
                    this.mAnimationEvents.add(animationEvent);
                    this.mSwipedOutViews.remove(next);
                    this.mChildRemoveAnimationRunning = true;
                }
            }
            z = true;
            animationEvent.viewAfterChangingView = getFirstChildBelowTranlsationY(translationY, z);
            this.mAnimationEvents.add(animationEvent);
            this.mSwipedOutViews.remove(next);
            this.mChildRemoveAnimationRunning = true;
        }
        this.mChildrenToRemoveAnimated.clear();
    }

    private void generatePositionChangeEvents() {
        Iterator<View> it = this.mChildrenChangingPositions.iterator();
        while (it.hasNext()) {
            this.mAnimationEvents.add(new AnimationEvent(it.next(), 8));
        }
        this.mChildrenChangingPositions.clear();
        if (this.mGenerateChildOrderChangedEvent) {
            this.mAnimationEvents.add(new AnimationEvent((View) null, 8));
            this.mGenerateChildOrderChangedEvent = false;
        }
    }

    private void generateChildAdditionEvents() {
        Iterator<View> it = this.mChildrenToAddAnimated.iterator();
        while (it.hasNext()) {
            View next = it.next();
            if (this.mFromMoreCardAdditions.contains(next)) {
                this.mAnimationEvents.add(new AnimationEvent(next, 0, 360));
            } else {
                this.mAnimationEvents.add(new AnimationEvent(next, 0));
            }
        }
        this.mChildrenToAddAnimated.clear();
        this.mFromMoreCardAdditions.clear();
    }

    private void generateTopPaddingEvent() {
        if (this.mTopPaddingNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent((View) null, 3));
        }
        this.mTopPaddingNeedsAnimation = false;
    }

    private void generateActivateEvent() {
        if (this.mActivateNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent((View) null, 6));
        }
        this.mActivateNeedsAnimation = false;
    }

    private void generateAnimateEverythingEvent() {
        if (this.mEverythingNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent((View) null, 21));
        }
        this.mEverythingNeedsAnimation = false;
    }

    private void generateDimmedEvent() {
        if (this.mDimmedNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent((View) null, 7));
        }
        this.mDimmedNeedsAnimation = false;
    }

    private void generateHideSensitiveEvent() {
        if (this.mHideSensitiveNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent((View) null, 11));
        }
        this.mHideSensitiveNeedsAnimation = false;
    }

    private void generateDarkEvent() {
        if (this.mDarkNeedsAnimation) {
            AnimationFilter animationFilter = new AnimationFilter();
            animationFilter.animateDark();
            animationFilter.animateY(this.mShelf);
            this.mAnimationEvents.add(new AnimationEvent((View) null, 9, animationFilter));
            startBackgroundFadeIn();
        }
        this.mDarkNeedsAnimation = false;
    }

    private void generateGoToFullShadeEvent() {
        if (this.mGoToFullShadeNeedsAnimation) {
            this.mAnimationEvents.add(new AnimationEvent((View) null, 10));
        }
        this.mGoToFullShadeNeedsAnimation = false;
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
                            int i3 = this.mLastMotionY - y;
                            int abs = Math.abs(x - this.mDownX);
                            int abs2 = Math.abs(i3);
                            if (abs2 > this.mTouchSlop && abs2 > abs) {
                                setIsBeingDragged(true);
                                if (!((ControlPanelController) Dependency.get(ControlPanelController.class)).isUseControlCenter() && ((isInContentBounds(motionEvent) || this.mIsQsCovered) && isScrolledToTop() && ((i3 > 0 && canScrollUp() && !isQsCovered()) || (i3 < 0 && isQsCovered())))) {
                                    this.mIsQsBeingCovered = true;
                                    this.mIsQsCovered = false;
                                }
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
            if (this.mScroller.springBack(this.mScrollX, this.mOwnScrollY, 0, 0, 0, getScrollRange())) {
                animateScroll();
            }
        } else {
            int y2 = (int) motionEvent.getY();
            this.mScrolledToTopOnFirstDown = isScrolledToTop();
            if (getChildAtPosition(motionEvent.getX(), (float) y2) == null) {
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
        if (this.mIsBeingDragged || this.mIsQsBeingCovered) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public StackScrollAlgorithm createStackScrollAlgorithm(Context context) {
        return new StackScrollAlgorithm(context);
    }

    private boolean isInContentBounds(MotionEvent motionEvent) {
        return isInContentBounds(motionEvent.getY());
    }

    public boolean isInContentBounds(float f) {
        return f < ((float) (getHeight() - getEmptyBottomMargin()));
    }

    private void setIsBeingDragged(boolean z) {
        this.mIsBeingDragged = z;
        if (z) {
            requestDisallowInterceptTouchEvent(true);
            removeLongPressCallback();
        }
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (!z) {
            removeLongPressCallback();
        }
    }

    public void clearChildFocus(View view) {
        super.clearChildFocus(view);
        if (this.mForcedScroll == view) {
            this.mForcedScroll = null;
        }
    }

    public void requestDisallowLongPress() {
        removeLongPressCallback();
    }

    public void requestDisallowDismiss() {
        this.mDisallowDismissInThisMotion = true;
    }

    public void removeLongPressCallback() {
        this.mSwipeHelper.removeLongPressCallback();
    }

    public boolean isQsBeingCovered() {
        return this.mIsQsBeingCovered;
    }

    public boolean isQsCovered() {
        return this.mIsQsCovered;
    }

    public void resetIsQsCovered(boolean z) {
        QS qs;
        this.mIsQsCovered = z;
        if (!z && (qs = this.mQs) != null && qs.getQsContent() != null) {
            this.mQs.getQsContent().setScaleY(1.0f);
            this.mQs.getQsContent().setScaleX(1.0f);
            this.mQs.getQsContent().setAlpha(1.0f);
        }
    }

    public boolean canScrollUp() {
        return this.mOwnScrollY < getScrollRange();
    }

    public boolean canScrollDown() {
        return this.mOwnScrollY > 0;
    }

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
            this.mStatusBar.closeAndSaveGuts(true, false, false, -1, -1, false);
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
            setOwnScrollY(0);
            this.mStatusBar.resetUserExpandedStates();
            clearTemporaryViews(this);
            for (int i = 0; i < getChildCount(); i++) {
                ExpandableView expandableView = (ExpandableView) getChildAt(i);
                if (expandableView instanceof ExpandableNotificationRow) {
                    clearTemporaryViews(((ExpandableNotificationRow) expandableView).getChildrenContainer());
                }
            }
        }
    }

    private void clearTemporaryViews(ViewGroup viewGroup) {
        while (viewGroup != null && viewGroup.getTransientViewCount() != 0) {
            viewGroup.removeTransientView(viewGroup.getTransientView(0));
        }
        if (viewGroup != null) {
            viewGroup.getOverlay().clear();
        }
    }

    public void onPanelTrackingStarted() {
        this.mPanelTracking = true;
        this.mAmbientState.setPanelTracking(true);
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
        if (!z) {
            resetIsQsCovered(false);
        }
        this.mStackScrollAlgorithm.setIsExpanded(z);
        if (z2) {
            if (!this.mIsExpanded) {
                this.mSwipeHelper.resetAnimatingValue();
                this.mGroupManager.collapseAllGroups();
            } else {
                this.mStatusBar.showReturnToInCallScreenButtonIfNeed();
                this.mHeadsUpManager.removeHeadsUpNotification();
                hideHeadsUpBackground();
                this.mStatusBar.updateNotifications();
            }
            updateNotificationAnimationStates();
            updateChronometers();
            requestChildrenUpdate();
        }
    }

    private void hideHeadsUpBackground() {
        setPadding(0, getPaddingTop(), 0, getPaddingBottom());
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            hideHeadsUpBackgroundForChild(getChildAt(i));
        }
    }

    private void hideHeadsUpBackgroundForChild(View view) {
        if (view instanceof ExpandableNotificationRow) {
            ((ExpandableNotificationRow) view).hideHeadsUpBackground();
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

    public void onHeightChanged(ExpandableView expandableView, boolean z) {
        updateContentHeight();
        updateScrollPositionOnExpandInBottom(expandableView);
        clampScrollPosition();
        notifyHeightChangeListener(expandableView);
        ExpandableNotificationRow expandableNotificationRow = expandableView instanceof ExpandableNotificationRow ? (ExpandableNotificationRow) expandableView : null;
        if (expandableNotificationRow != null && (expandableNotificationRow == this.mFirstVisibleBackgroundChild || expandableNotificationRow.getNotificationParent() == this.mFirstVisibleBackgroundChild)) {
            updateAlgorithmLayoutMinHeight();
        }
        if (z) {
            requestAnimationOnViewResize(expandableNotificationRow);
        }
        requestChildrenUpdate();
    }

    public void onReset(ExpandableView expandableView) {
        updateAnimationState(expandableView);
        updateChronometerForChild(expandableView);
    }

    private void updateScrollPositionOnExpandInBottom(ExpandableView expandableView) {
        if (expandableView instanceof ExpandableNotificationRow) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) expandableView;
            if (expandableNotificationRow.isUserLocked() && expandableNotificationRow != getFirstChildNotGone() && !expandableNotificationRow.isSummaryWithChildren()) {
                float translationY = expandableNotificationRow.getTranslationY() + ((float) expandableNotificationRow.getActualHeight());
                if (expandableNotificationRow.isChildInGroup()) {
                    translationY += expandableNotificationRow.getNotificationParent().getTranslationY();
                }
                int i = this.mMaxLayoutHeight + ((int) this.mStackTranslation);
                if (expandableNotificationRow != this.mLastVisibleBackgroundChild) {
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

    public void setOnEmptySpaceClickListener(OnEmptySpaceClickListener onEmptySpaceClickListener) {
        this.mOnEmptySpaceClickListener = onEmptySpaceClickListener;
    }

    public void setOnTopPaddingUpdateListener(OnTopPaddingUpdateListener onTopPaddingUpdateListener) {
        this.mOnTopPaddingUpdateListener = onTopPaddingUpdateListener;
    }

    public void onChildAnimationFinished() {
        setAnimationRunning(false);
        requestChildrenUpdate();
        runAnimationFinishedRunnables();
        clearViewOverlays();
        clearHeadsUpDisappearRunning();
    }

    private void clearHeadsUpDisappearRunning() {
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) childAt;
                expandableNotificationRow.setHeadsUpAnimatingAway(false);
                if (expandableNotificationRow.isSummaryWithChildren()) {
                    for (ExpandableNotificationRow headsUpAnimatingAway : expandableNotificationRow.getNotificationChildren()) {
                        headsUpAnimatingAway.setHeadsUpAnimatingAway(false);
                    }
                }
            }
        }
    }

    private void clearViewOverlays() {
        Iterator<View> it = this.mClearOverlayViewsWhenFinished.iterator();
        while (it.hasNext()) {
            StackStateAnimator.removeFromOverlay(it.next());
        }
        this.mClearOverlayViewsWhenFinished.clear();
    }

    private void runAnimationFinishedRunnables() {
        for (Runnable run : this.mAnimationFinishedRunnables.keySet()) {
            run.run();
        }
        this.mAnimationFinishedRunnables.clear();
    }

    public void setDimmed(boolean z, boolean z2) {
        this.mAmbientState.setDimmed(z);
        if (!z2 || !this.mAnimationsEnabled) {
            setDimAmount(z ? 1.0f : 0.0f);
        } else {
            this.mDimmedNeedsAnimation = true;
            this.mNeedsAnimation = true;
            animateDimmed(z);
        }
        requestChildrenUpdate();
    }

    /* access modifiers changed from: private */
    public void setDimAmount(float f) {
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
            ValueAnimator ofFloat = TimeAnimator.ofFloat(new float[]{f2, f});
            this.mDimAnimator = ofFloat;
            ofFloat.setDuration(220);
            this.mDimAnimator.setInterpolator(Interpolators.FAST_OUT_SLOW_IN);
            this.mDimAnimator.addListener(this.mDimEndListener);
            this.mDimAnimator.addUpdateListener(this.mDimUpdateListener);
            this.mDimAnimator.start();
        }
    }

    public void setHideSensitive(boolean z, boolean z2) {
        if (z != this.mAmbientState.isHideSensitive()) {
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                ((ExpandableView) getChildAt(i)).setHideSensitiveForIntrinsicHeight(z);
            }
            this.mAmbientState.setHideSensitive(z);
            if (z2 && this.mAnimationsEnabled) {
                this.mHideSensitiveNeedsAnimation = true;
                this.mNeedsAnimation = true;
            }
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
        if (DEBUG) {
            Log.d("StackScroller", "applyCurrentState");
        }
        this.mCurrentStackScrollState.apply();
        NotificationLogger.OnChildLocationsChangedListener onChildLocationsChangedListener = this.mListener;
        if (onChildLocationsChangedListener != null) {
            onChildLocationsChangedListener.onChildLocationsChanged();
        }
        runAnimationFinishedRunnables();
        setAnimationRunning(false);
        updateBackground();
        updateViewShadows();
    }

    /* access modifiers changed from: private */
    public void updateViewShadows() {
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
            if (expandableView2 == null || f2 <= 0.0f || f2 >= 0.1f) {
                expandableView3.setFakeShadowIntensity(0.0f, 0.0f, 0, 0);
            } else {
                expandableView3.setFakeShadowIntensity(f2 / 0.1f, expandableView2.getOutlineAlpha(), (int) (((expandableView2.getTranslationY() + ((float) expandableView2.getActualHeight())) - expandableView3.getTranslationY()) - ((float) expandableView2.getExtraBottomPadding())), expandableView2.getOutlineTranslation());
            }
            i2++;
            expandableView2 = expandableView3;
        }
        this.mTmpSortedChildren.clear();
    }

    public void goToFullShade(long j) {
        this.mEmptyShadeView.setInvisible();
        this.mGoToFullShadeNeedsAnimation = true;
        this.mGoToFullShadeDelay = j;
        this.mNeedsAnimation = true;
        requestChildrenUpdate();
    }

    public void cancelExpandHelper() {
        this.mExpandHelper.cancel();
    }

    public void setIntrinsicPadding(int i) {
        if (DEBUG) {
            Log.d("StackScroller", "setIntrinsicPadding padding=" + i);
        }
        this.mIntrinsicPadding = i;
    }

    public int getIntrinsicPadding() {
        return this.mIntrinsicPadding;
    }

    public float getNotificationsTopY() {
        return ((float) this.mTopPadding) + getStackTranslation();
    }

    public void setDark(boolean z, boolean z2, PointF pointF) {
        this.mAmbientState.setDark(z);
        if (z2 && this.mAnimationsEnabled) {
            this.mDarkNeedsAnimation = true;
            this.mDarkAnimationOriginIndex = findDarkAnimationOriginIndex(pointF);
            this.mNeedsAnimation = true;
            setBackgroundFadeAmount(0.0f);
        } else if (!z) {
            setBackgroundFadeAmount(1.0f);
        }
        requestChildrenUpdate();
        if (z) {
            this.mScrimController.setExcludedBackgroundArea((Rect) null);
        } else {
            updateBackground();
        }
        updateWillNotDraw();
        updateContentHeight();
        notifyHeightChangeListener(this.mShelf);
    }

    private void updateWillNotDraw() {
        setWillNotDraw(!((!this.mAmbientState.isDark() && this.mShouldDrawNotificationBackground) || DEBUG));
    }

    /* access modifiers changed from: private */
    public void setBackgroundFadeAmount(float f) {
        this.mBackgroundFadeAmount = f;
        updateBackgroundDimming();
    }

    public float getBackgroundFadeAmount() {
        return this.mBackgroundFadeAmount;
    }

    private void startBackgroundFadeIn() {
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, BACKGROUND_FADE, new float[]{0.0f, 1.0f});
        ofFloat.setDuration(200);
        ofFloat.setInterpolator(Interpolators.ALPHA_IN);
        ofFloat.start();
    }

    private int findDarkAnimationOriginIndex(PointF pointF) {
        if (pointF != null) {
            float f = pointF.y;
            if (f >= ((float) this.mTopPadding)) {
                if (f > getBottomMostNotificationBottom()) {
                    return -2;
                }
                ExpandableView closestChildAtRawPosition = getClosestChildAtRawPosition(pointF.x, pointF.y);
                if (closestChildAtRawPosition != null) {
                    return getNotGoneIndex(closestChildAtRawPosition);
                }
            }
        }
        return -1;
    }

    private int getNotGoneIndex(View view) {
        int childCount = getChildCount();
        int i = 0;
        for (int i2 = 0; i2 < childCount; i2++) {
            View childAt = getChildAt(i2);
            if (view == childAt) {
                return i;
            }
            if (childAt.getVisibility() != 8) {
                i++;
            }
        }
        return -1;
    }

    public void setEmptyShadeView(EmptyShadeView emptyShadeView) {
        int removeTargetView = removeTargetView(this.mEmptyShadeView);
        this.mEmptyShadeView = emptyShadeView;
        addView(emptyShadeView, removeTargetView);
    }

    private int removeTargetView(View view) {
        if (view == null) {
            return -1;
        }
        int indexOfChild = indexOfChild(view);
        removeView(view);
        return indexOfChild;
    }

    public void setDismissAllInProgress(boolean z) {
        if (this.mDismissAllInProgress != z) {
            this.mDismissAllInProgress = z;
            this.mAmbientState.setDismissAllInProgress(z);
            handleDismissAllClipping();
        }
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

    public void dispatchDismissAllToChild(List<View> list, Runnable runnable) {
        this.mSwipeHelper.dispatchDismissAllToChild(list, runnable);
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
    }

    public void onGoToKeyguard() {
        requestAnimateEverything();
    }

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
                boolean z = f2 > (y + ((float) expandableView.getActualHeight())) - ((float) expandableView.getClipBottomAmount());
                if (expandableView == this.mEmptyShadeView) {
                    return true;
                }
                if (!z) {
                    return false;
                }
            }
        }
        if (f2 > ((float) this.mTopPadding) + this.mStackTranslation) {
            return true;
        }
        return false;
    }

    public void onGroupExpansionChanged(final ExpandableNotificationRow expandableNotificationRow, boolean z) {
        boolean z2 = !this.mGroupExpandedForMeasure && this.mAnimationsEnabled && (this.mIsExpanded || expandableNotificationRow.isPinned());
        if (z2) {
            this.mExpandedGroupView = expandableNotificationRow;
            this.mNeedsAnimation = true;
        }
        expandableNotificationRow.setChildrenExpanded(z, z2);
        if (!this.mGroupExpandedForMeasure) {
            onHeightChanged(expandableNotificationRow, false);
        }
        runAfterAnimationFinished(new Runnable() {
            public void run() {
                expandableNotificationRow.onFinishedExpansionChange();
            }
        });
    }

    public void onGroupCreatedFromChildren(NotificationGroupManager.NotificationGroup notificationGroup) {
        this.mStatusBar.requestNotificationUpdate();
    }

    public void onInitializeAccessibilityEventInternal(AccessibilityEvent accessibilityEvent) {
        super.onInitializeAccessibilityEventInternal(accessibilityEvent);
        accessibilityEvent.setScrollable(this.mScrollable);
        accessibilityEvent.setScrollX(this.mScrollX);
        accessibilityEvent.setScrollY(this.mOwnScrollY);
        accessibilityEvent.setMaxScrollX(this.mScrollX);
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
            r4.animateScroll()
            return r0
        L_0x0059:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.performAccessibilityActionInternal(int, android.os.Bundle):boolean");
    }

    public void onGroupsChanged() {
        this.mStatusBar.requestNotificationUpdate();
    }

    public void generateChildOrderChangedEvent() {
        if (this.mIsExpanded && this.mAnimationsEnabled) {
            this.mGenerateChildOrderChangedEvent = true;
            this.mNeedsAnimation = true;
            requestChildrenUpdate();
        }
    }

    public void runAfterAnimationFinished(Runnable runnable) {
        this.mAnimationFinishedRunnables.put(runnable, PRESENT);
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
        updateHeadsUpState();
        generateHeadsUpAnimation(expandableNotificationRow, true);
    }

    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
        ExpandableViewState viewStateForView = this.mCurrentStackScrollState.getViewStateForView(expandableNotificationRow);
        if (viewStateForView != null) {
            viewStateForView.cancelAnimations(expandableNotificationRow);
        }
        updateHeadsUpState();
    }

    public void onHeadsUpStateChanged(NotificationData.Entry entry, boolean z) {
        updateHeadsUpState();
        generateHeadsUpAnimation(entry.row, z);
    }

    private void updateHeadsUpState() {
        boolean hasPinnedHeadsUp = this.mHeadsUpManager.hasPinnedHeadsUp();
        this.mHeadsUpPinned = hasPinnedHeadsUp;
        this.mStackScrollAlgorithm.setExpandedBecauseOfHeadsUp(hasPinnedHeadsUp || this.mHeadsUpAnimatingAway);
    }

    private void generateHeadsUpAnimation(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        if (z && !this.mIsExpanded) {
            int dimensionPixelOffset = getResources().getDimensionPixelOffset(R.dimen.notification_heads_up_margin_horizontal) - getResources().getDimensionPixelOffset(R.dimen.panel_content_margin_horizontal);
            setPadding(dimensionPixelOffset, getPaddingTop(), dimensionPixelOffset, getPaddingBottom());
            expandableNotificationRow.showHeadsUpBackground();
        }
        if (this.mAnimationsEnabled && !expandableNotificationRow.isHiddenForAnimation()) {
            this.mHeadsUpChangeAnimations.add(new Pair(expandableNotificationRow, Boolean.valueOf(z)));
            this.mNeedsAnimation = true;
            if (!this.mIsExpanded && !z) {
                expandableNotificationRow.setHeadsUpAnimatingAway(true);
            }
            requestChildrenUpdate();
        }
    }

    public void setShadeExpanded(boolean z) {
        this.mAmbientState.setShadeExpanded(z);
        this.mStateAnimator.setShadeExpanded(z);
    }

    public void setHeadsUpBoundaries(int i, int i2) {
        this.mAmbientState.setMaxHeadsUpTranslation((float) (i - i2));
        this.mStateAnimator.setHeadsUpAppearHeightBottom(i);
        requestChildrenUpdate();
    }

    public void setTrackingHeadsUp(boolean z) {
        this.mTrackingHeadsUp = z;
    }

    public void setScrimController(ScrimController scrimController) {
        this.mScrimController = scrimController;
        scrimController.setScrimBehindChangeRunnable(new Runnable() {
            public void run() {
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
        if (!z) {
            this.mChildRemoveAnimationRunning = false;
        }
    }

    public boolean isExpanded() {
        return this.mIsExpanded;
    }

    public void setFadingOut(boolean z) {
        if (z != this.mFadingOut) {
            this.mFadingOut = z;
            updateFadingState();
        }
    }

    public void setParentNotFullyVisible(boolean z) {
        if (this.mScrimController != null && z != this.mParentNotFullyVisible) {
            this.mParentNotFullyVisible = z;
            updateFadingState();
        }
    }

    private void updateFadingState() {
        applyCurrentBackgroundBounds();
        updateSrcDrawing();
    }

    public void setAlpha(float f) {
        super.setAlpha(f);
        setFadingOut(f != 1.0f);
    }

    public void setQsExpanded(boolean z) {
        this.mQsExpanded = z;
        updateAlgorithmLayoutMinHeight();
    }

    public void setOwnScrollY(int i) {
        int i2 = this.mOwnScrollY;
        if (i != i2) {
            int i3 = this.mScrollX;
            onScrollChanged(i3, i, i3, i2);
            this.mOwnScrollY = i;
            updateForwardAndBackwardScrollability();
            requestChildrenUpdate();
        }
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

    public void setMaxDisplayedNotifications(int i) {
        if (this.mMaxDisplayedNotifications != i) {
            this.mMaxDisplayedNotifications = i;
            updateContentHeight();
            notifyHeightChangeListener(this.mShelf);
        }
    }

    public int getMinExpansionHeight() {
        return this.mShelf.getIntrinsicHeight() - ((this.mShelf.getIntrinsicHeight() - this.mStatusBarHeight) / 2);
    }

    public void setInHeadsUpPinnedMode(boolean z) {
        this.mInHeadsUpPinnedMode = z;
        updateClipping();
    }

    public void setHeadsUpAnimatingAway(boolean z) {
        this.mHeadsUpAnimatingAway = z;
        updateHeadsUpState();
        updateClipping();
    }

    public void setStatusBarState(int i) {
        this.mStatusBarState = i;
        this.mAmbientState.setStatusBarState(i);
    }

    public void setExpandingVelocity(float f) {
        this.mAmbientState.setExpandingVelocity(f);
    }

    public float getOpeningHeight() {
        return (float) getMinExpansionHeight();
    }

    public void setIsFullWidth(boolean z) {
        this.mAmbientState.setPanelFullWidth(z);
    }

    public boolean isInUserVisibleArea(ExpandableNotificationRow expandableNotificationRow) {
        if (!isInVisibleLocation(expandableNotificationRow)) {
            return false;
        }
        float f = ((!expandableNotificationRow.isChildInGroup() || expandableNotificationRow.getNotificationParent() == null) ? 0.0f : this.mCurrentStackScrollState.getViewStateForView(expandableNotificationRow.getNotificationParent()).yTranslation) + this.mCurrentStackScrollState.getViewStateForView(expandableNotificationRow).yTranslation;
        if (((float) expandableNotificationRow.getActualHeight()) + f <= 0.0f || f >= this.mExpandedHeight) {
            return false;
        }
        return true;
    }

    public void setExtraBottomRange(int i, int i2) {
        this.mExtraBottomRange = i;
        this.mExtraBottomRangeQsCovered = i2;
    }

    public void onHeadsUpHiddenForAnimationChanged() {
        requestChildrenUpdate();
    }

    private class NotificationSwipeHelper extends SwipeHelper implements NotificationSwipeActionHelper {
        private Runnable mFalsingCheck;
        private Handler mHandler = new Handler();

        public NotificationSwipeHelper(int i, SwipeHelper.Callback callback, Context context) {
            super(i, callback, context);
            this.mFalsingCheck = new Runnable(NotificationStackScrollLayout.this) {
                public void run() {
                    NotificationSwipeHelper.this.resetExposedMenuView(true, true);
                }
            };
        }

        public void onDownUpdate(View view, MotionEvent motionEvent) {
            View unused = NotificationStackScrollLayout.this.mTranslatingParentView = view;
            NotificationMenuRowPlugin unused2 = NotificationStackScrollLayout.this.mCurrMenuRow = null;
            if (NotificationStackScrollLayout.this.mCurrMenuRow != null) {
                NotificationStackScrollLayout.this.mCurrMenuRow.onTouchEvent(view, motionEvent, 0.0f);
            }
            this.mHandler.removeCallbacks(this.mFalsingCheck);
            resetExposedMenuView(true, false);
            if (view instanceof ExpandableNotificationRow) {
                NotificationMenuRowPlugin unused3 = NotificationStackScrollLayout.this.mCurrMenuRow = ((ExpandableNotificationRow) view).createMenu();
                NotificationStackScrollLayout.this.mCurrMenuRow.setSwipeActionHelper(this);
                NotificationStackScrollLayout.this.mCurrMenuRow.setMenuClickListener(NotificationStackScrollLayout.this);
            }
        }

        public void onMoveUpdate(View view, MotionEvent motionEvent, float f, float f2) {
            this.mHandler.removeCallbacks(this.mFalsingCheck);
            if (NotificationStackScrollLayout.this.mCurrMenuRow != null) {
                NotificationStackScrollLayout.this.mCurrMenuRow.onTouchEvent(view, motionEvent, 0.0f);
            }
        }

        public boolean handleUpEvent(MotionEvent motionEvent, View view, float f, float f2) {
            if (NotificationStackScrollLayout.this.mCurrMenuRow != null) {
                return NotificationStackScrollLayout.this.mCurrMenuRow.onTouchEvent(view, motionEvent, f);
            }
            return false;
        }

        public void dismissChild(View view, float f, boolean z) {
            super.dismissChild(view, f, z);
            if (NotificationStackScrollLayout.this.mIsExpanded) {
                NotificationStackScrollLayout.this.handleChildDismissed(view);
            }
            NotificationStackScrollLayout.this.mStatusBar.closeAndSaveGuts(true, false, false, -1, -1, false);
            handleMenuCoveredOrDismissed();
        }

        public void snapChild(View view, float f, float f2) {
            super.snapChild(view, f, f2);
            NotificationStackScrollLayout.this.onDragCancelled(view);
            if (f == 0.0f) {
                handleMenuCoveredOrDismissed();
            }
        }

        public void snooze(StatusBarNotification statusBarNotification, NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
            NotificationStackScrollLayout.this.mStatusBar.setNotificationSnoozed(statusBarNotification, snoozeOption);
        }

        public boolean isFalseGesture(MotionEvent motionEvent) {
            return super.isFalseGesture(motionEvent);
        }

        private void handleMenuCoveredOrDismissed() {
            if (NotificationStackScrollLayout.this.mMenuExposedView != null && NotificationStackScrollLayout.this.mMenuExposedView == NotificationStackScrollLayout.this.mTranslatingParentView) {
                View unused = NotificationStackScrollLayout.this.mMenuExposedView = null;
            }
        }

        public Animator getViewTranslationAnimator(View view, float f, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
            if (view instanceof ExpandableNotificationRow) {
                return ((ExpandableNotificationRow) view).getTranslateViewAnimator(f, animatorUpdateListener);
            }
            return super.getViewTranslationAnimator(view, f, animatorUpdateListener);
        }

        public void setTranslation(View view, float f) {
            ((ExpandableView) view).setTranslation(f);
        }

        public float getTranslation(View view) {
            return ((ExpandableView) view).getTranslation();
        }

        public void dismiss(View view, float f) {
            dismissChild(view, f, !swipedFastEnough(0.0f, 0.0f));
        }

        public void snap(View view, float f, float f2) {
            snapChild(view, f, f2);
        }

        public boolean swipedFarEnough(float f, float f2) {
            return swipedFarEnough();
        }

        public boolean swipedFastEnough(float f, float f2) {
            return swipedFastEnough();
        }

        public float getMinDismissVelocity() {
            return getEscapeVelocity();
        }

        public void onMenuShown(View view) {
            NotificationStackScrollLayout.this.onDragCancelled(view);
            if (NotificationStackScrollLayout.this.isAntiFalsingNeeded()) {
                this.mHandler.removeCallbacks(this.mFalsingCheck);
                this.mHandler.postDelayed(this.mFalsingCheck, 4000);
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:3:0x0014, code lost:
            if (r1 == false) goto L_0x003b;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void closeControlsIfOutsideTouch(android.view.MotionEvent r8) {
            /*
                r7 = this;
                com.android.systemui.statusbar.stack.NotificationStackScrollLayout r0 = com.android.systemui.statusbar.stack.NotificationStackScrollLayout.this
                com.android.systemui.statusbar.phone.StatusBar r0 = r0.mStatusBar
                com.android.systemui.statusbar.NotificationGuts r0 = r0.getExposedGuts()
                if (r0 == 0) goto L_0x0017
                com.android.systemui.statusbar.NotificationGuts$GutsContent r1 = r0.getGutsContent()
                boolean r1 = r1.isLeavebehind()
                if (r1 != 0) goto L_0x0017
                goto L_0x003b
            L_0x0017:
                com.android.systemui.statusbar.stack.NotificationStackScrollLayout r0 = com.android.systemui.statusbar.stack.NotificationStackScrollLayout.this
                com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin r0 = r0.mCurrMenuRow
                if (r0 == 0) goto L_0x003a
                com.android.systemui.statusbar.stack.NotificationStackScrollLayout r0 = com.android.systemui.statusbar.stack.NotificationStackScrollLayout.this
                com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin r0 = r0.mCurrMenuRow
                boolean r0 = r0.isMenuVisible()
                if (r0 == 0) goto L_0x003a
                com.android.systemui.statusbar.stack.NotificationStackScrollLayout r0 = com.android.systemui.statusbar.stack.NotificationStackScrollLayout.this
                android.view.View r0 = r0.mTranslatingParentView
                if (r0 == 0) goto L_0x003a
                com.android.systemui.statusbar.stack.NotificationStackScrollLayout r0 = com.android.systemui.statusbar.stack.NotificationStackScrollLayout.this
                android.view.View r0 = r0.mTranslatingParentView
                goto L_0x003b
            L_0x003a:
                r0 = 0
            L_0x003b:
                if (r0 == 0) goto L_0x0058
                com.android.systemui.statusbar.stack.NotificationStackScrollLayout r1 = com.android.systemui.statusbar.stack.NotificationStackScrollLayout.this
                boolean r8 = r1.isTouchInView(r8, r0)
                if (r8 != 0) goto L_0x0058
                com.android.systemui.statusbar.stack.NotificationStackScrollLayout r8 = com.android.systemui.statusbar.stack.NotificationStackScrollLayout.this
                com.android.systemui.statusbar.phone.StatusBar r0 = r8.mStatusBar
                r1 = 0
                r2 = 0
                r3 = 1
                r4 = -1
                r5 = -1
                r6 = 0
                r0.closeAndSaveGuts(r1, r2, r3, r4, r5, r6)
                r8 = 1
                r7.resetExposedMenuView(r8, r8)
            L_0x0058:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.stack.NotificationStackScrollLayout.NotificationSwipeHelper.closeControlsIfOutsideTouch(android.view.MotionEvent):void");
        }

        public void resetExposedMenuView(boolean z, boolean z2) {
            if (NotificationStackScrollLayout.this.mMenuExposedView == null) {
                return;
            }
            if (z2 || NotificationStackScrollLayout.this.mMenuExposedView != NotificationStackScrollLayout.this.mTranslatingParentView) {
                View access$3200 = NotificationStackScrollLayout.this.mMenuExposedView;
                if (z) {
                    Animator viewTranslationAnimator = getViewTranslationAnimator(access$3200, 0.0f, (ValueAnimator.AnimatorUpdateListener) null);
                    if (viewTranslationAnimator != null) {
                        viewTranslationAnimator.start();
                    }
                } else if (NotificationStackScrollLayout.this.mMenuExposedView instanceof ExpandableNotificationRow) {
                    ((ExpandableNotificationRow) NotificationStackScrollLayout.this.mMenuExposedView).resetTranslation();
                }
                View unused = NotificationStackScrollLayout.this.mMenuExposedView = null;
            }
        }
    }

    public void resetViews() {
        setOverScrollAmount(0.0f, true, false, true);
        resetScrollPosition();
        this.mIsBeingDragged = false;
        this.mIsQsBeingCovered = false;
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        String str;
        String str2;
        Object[] objArr = new Object[13];
        objArr[0] = Float.valueOf(this.mExpandedHeight);
        objArr[1] = Integer.valueOf(this.mCurrentStackHeight);
        objArr[2] = Integer.valueOf(this.mContentHeight);
        objArr[3] = Integer.valueOf(this.mMaxLayoutHeight);
        objArr[4] = Integer.valueOf(this.mOwnScrollY);
        objArr[5] = Integer.valueOf(getScrollRange());
        objArr[6] = Integer.valueOf(this.mTopPadding);
        objArr[7] = Integer.valueOf(this.mIntrinsicPadding);
        String str3 = "T";
        objArr[8] = this.mIsBeingDragged ? str3 : "f";
        if (this.mIsQsBeingCovered) {
            str = str3;
        } else {
            str = "f";
        }
        objArr[9] = str;
        if (this.mIsQsCovered) {
            str2 = str3;
        } else {
            str2 = "f";
        }
        objArr[10] = str2;
        if (!this.mQsExpanded) {
            str3 = "f";
        }
        objArr[11] = str3;
        objArr[12] = Integer.valueOf(this.mSpringLength);
        printWriter.println(String.format("      [NotificationStackScrollLayout: mExpandedHeight=%f mCurrentStackHeight=%d mContentHeight=%d mMaxLayoutHeight=%d mOwnScrollY=%d scrollRange=%d mTopPadding=%d mIntrinsicPadding=%d mIsBeingDragged=%s mIsQsBeingCovered=%s mIsQsCovered=%s mQsExpanded=%s mSpringLength=%d]", objArr));
    }

    /* access modifiers changed from: private */
    public boolean isTouchInView(MotionEvent motionEvent, View view) {
        int i;
        if (view == null) {
            return false;
        }
        if (view instanceof ExpandableView) {
            i = ((ExpandableView) view).getActualHeight();
        } else {
            i = view.getHeight();
        }
        view.getLocationOnScreen(this.mTempInt2);
        int[] iArr = this.mTempInt2;
        int i2 = iArr[0];
        int i3 = iArr[1];
        return new Rect(i2, i3, view.getWidth() + i2, i + i3).contains((int) motionEvent.getRawX(), (int) motionEvent.getRawY());
    }

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

    public void resetExposedMenuView(boolean z, boolean z2) {
        this.mSwipeHelper.resetExposedMenuView(z, z2);
    }

    public void closeControlsIfOutsideTouch(MotionEvent motionEvent) {
        this.mSwipeHelper.closeControlsIfOutsideTouch(motionEvent);
    }

    static class AnimationEvent {
        static AnimationFilter[] FILTERS;
        static int[] LENGTHS = {464, 464, 360, 360, 360, 360, 220, 220, 360, 200, 448, 360, 360, 360, 500, 150, 150, 360, 450, 450, 360, 360};
        final int animationType;
        final View changingView;
        final AnimationFilter filter;
        boolean headsUpFromBottom;
        final long length;
        View viewAfterChangingView;

        static {
            AnimationFilter animationFilter = new AnimationFilter();
            animationFilter.animateShadowAlpha();
            animationFilter.animateHeight();
            animationFilter.animateTopInset();
            animationFilter.animateY();
            animationFilter.animateZ();
            animationFilter.hasDelays();
            AnimationFilter animationFilter2 = new AnimationFilter();
            animationFilter2.animateShadowAlpha();
            animationFilter2.animateHeight();
            animationFilter2.animateTopInset();
            animationFilter2.animateY();
            animationFilter2.animateZ();
            animationFilter2.hasDelays();
            AnimationFilter animationFilter3 = new AnimationFilter();
            animationFilter3.animateShadowAlpha();
            animationFilter3.animateHeight();
            animationFilter3.animateTopInset();
            animationFilter3.animateY();
            animationFilter3.animateZ();
            animationFilter3.hasDelays();
            AnimationFilter animationFilter4 = new AnimationFilter();
            animationFilter4.animateShadowAlpha();
            animationFilter4.animateHeight();
            animationFilter4.animateTopInset();
            animationFilter4.animateY();
            animationFilter4.animateDimmed();
            animationFilter4.animateZ();
            AnimationFilter animationFilter5 = new AnimationFilter();
            animationFilter5.animateShadowAlpha();
            AnimationFilter animationFilter6 = new AnimationFilter();
            animationFilter6.animateShadowAlpha();
            animationFilter6.animateHeight();
            AnimationFilter animationFilter7 = new AnimationFilter();
            animationFilter7.animateZ();
            AnimationFilter animationFilter8 = new AnimationFilter();
            animationFilter8.animateDimmed();
            AnimationFilter animationFilter9 = new AnimationFilter();
            animationFilter9.animateAlpha();
            animationFilter9.animateShadowAlpha();
            animationFilter9.animateHeight();
            animationFilter9.animateTopInset();
            animationFilter9.animateY();
            animationFilter9.animateZ();
            AnimationFilter animationFilter10 = new AnimationFilter();
            animationFilter10.animateShadowAlpha();
            animationFilter10.animateHeight();
            animationFilter10.animateTopInset();
            animationFilter10.animateY();
            animationFilter10.animateDimmed();
            animationFilter10.animateZ();
            animationFilter10.hasDelays();
            AnimationFilter animationFilter11 = new AnimationFilter();
            animationFilter11.animateHideSensitive();
            AnimationFilter animationFilter12 = new AnimationFilter();
            animationFilter12.animateShadowAlpha();
            animationFilter12.animateHeight();
            animationFilter12.animateTopInset();
            animationFilter12.animateScale();
            animationFilter12.animateY();
            animationFilter12.animateZ();
            AnimationFilter animationFilter13 = new AnimationFilter();
            animationFilter13.animateAlpha();
            animationFilter13.animateShadowAlpha();
            animationFilter13.animateHeight();
            animationFilter13.animateTopInset();
            animationFilter13.animateScale();
            animationFilter13.animateY();
            animationFilter13.animateZ();
            AnimationFilter animationFilter14 = new AnimationFilter();
            animationFilter14.animateShadowAlpha();
            animationFilter14.animateHeight();
            animationFilter14.animateTopInset();
            animationFilter14.animateY();
            animationFilter14.animateZ();
            AnimationFilter animationFilter15 = new AnimationFilter();
            animationFilter15.animateShadowAlpha();
            animationFilter15.animateHeight();
            animationFilter15.animateTopInset();
            animationFilter15.animateY();
            animationFilter15.animateZ();
            AnimationFilter animationFilter16 = new AnimationFilter();
            animationFilter16.animateShadowAlpha();
            animationFilter16.animateHeight();
            animationFilter16.animateTopInset();
            animationFilter16.animateY();
            animationFilter16.animateZ();
            animationFilter16.hasDelays();
            AnimationFilter animationFilter17 = new AnimationFilter();
            animationFilter17.animateShadowAlpha();
            animationFilter17.animateHeight();
            animationFilter17.animateTopInset();
            animationFilter17.animateY();
            animationFilter17.animateZ();
            AnimationFilter animationFilter18 = new AnimationFilter();
            animationFilter18.animateAlpha();
            animationFilter18.animateScale();
            AnimationFilter animationFilter19 = new AnimationFilter();
            animationFilter19.animateAlpha();
            animationFilter19.animateScale();
            AnimationFilter animationFilter20 = new AnimationFilter();
            animationFilter20.animateAlpha();
            animationFilter20.animateShadowAlpha();
            animationFilter20.animateDark();
            animationFilter20.animateDimmed();
            animationFilter20.animateHideSensitive();
            animationFilter20.animateHeight();
            animationFilter20.animateTopInset();
            animationFilter20.animateY();
            animationFilter20.animateZ();
            FILTERS = new AnimationFilter[]{animationFilter, animationFilter2, animationFilter3, animationFilter4, animationFilter5, animationFilter6, animationFilter7, animationFilter8, animationFilter9, null, animationFilter10, animationFilter11, animationFilter12, animationFilter13, animationFilter14, animationFilter15, animationFilter16, animationFilter17, animationFilter18, animationFilter19, new AnimationFilter(), animationFilter20};
        }

        AnimationEvent(View view, int i) {
            this(view, i, (long) LENGTHS[i]);
        }

        AnimationEvent(View view, int i, AnimationFilter animationFilter) {
            this(view, i, (long) LENGTHS[i], animationFilter);
        }

        AnimationEvent(View view, int i, long j) {
            this(view, i, j, FILTERS[i]);
        }

        AnimationEvent(View view, int i, long j, AnimationFilter animationFilter) {
            AnimationUtils.currentAnimationTimeMillis();
            this.changingView = view;
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
                if (animationEvent.animationType == 10) {
                    return animationEvent.length;
                }
            }
            return j;
        }

        public String toString() {
            switch (this.animationType) {
                case 0:
                    return "ADD";
                case 1:
                    return "REMOVE";
                case 2:
                    return "REMOVE_SWIPED_OUT";
                case 3:
                    return "TOP_PADDING_CHANGED";
                case 4:
                    return "START_DRAG";
                case 5:
                    return "SNAP_BACK";
                case 6:
                    return "ACTIVATED_CHILD";
                case 7:
                    return "DIMMED";
                case 8:
                    return "CHANGE_POSITION";
                case 9:
                    return "DARK";
                case 10:
                    return "GO_TO_FULL_SHADE";
                case 11:
                    return "HIDE_SENSITIVE";
                case 12:
                    return "VIEW_RESIZE";
                case 13:
                    return "GROUP_EXPANSION_CHANGED";
                case 14:
                    return "HEADS_UP_APPEAR";
                case 15:
                    return "HEADS_UP_DISAPPEAR";
                case 16:
                    return "HEADS_UP_DISAPPEAR_CLICK";
                case 17:
                    return "HEADS_UP_OTHER";
                case 18:
                    return "APPEAR";
                case 19:
                    return "DISAPPEAR";
                case 20:
                    return "SPRING_RESET";
                case 21:
                    return "EVERYTHING";
                default:
                    return "Unknown";
            }
        }
    }
}

package com.android.systemui.statusbar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.graphics.Outline;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.widget.AbstractFrameLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Constants;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.notification.HybridGroupManager;
import com.android.systemui.statusbar.notification.HybridNotificationView;
import com.android.systemui.statusbar.notification.InCallNotificationView;
import com.android.systemui.statusbar.notification.NotificationCustomViewWrapper;
import com.android.systemui.statusbar.notification.NotificationMediaTemplateViewWrapper;
import com.android.systemui.statusbar.notification.NotificationTemplateViewWrapper;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.NotificationViewWrapper;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.RemoteInputView;

public class NotificationContentView extends AbstractFrameLayout {
    private View mAmbientChild;
    private HybridNotificationView mAmbientSingleLineChild;
    private NotificationViewWrapper mAmbientWrapper;
    /* access modifiers changed from: private */
    public boolean mAnimate;
    /* access modifiers changed from: private */
    public int mAnimationStartVisibleType = -1;
    private boolean mBeforeN;
    private RemoteInputView mCachedExpandedRemoteInput;
    private RemoteInputView mCachedHeadsUpRemoteInput;
    private int mClipBottomAmount;
    /* access modifiers changed from: private */
    public final Rect mClipBounds = new Rect();
    private boolean mClipToActualHeight = true;
    private int mClipTopAmount;
    /* access modifiers changed from: private */
    public ExpandableNotificationRow mContainingNotification;
    private int mContentHeight;
    private int mContentHeightAtAnimationStart = -1;
    private View mContractedChild;
    private NotificationViewWrapper mContractedWrapper;
    private boolean mDark;
    private final ViewTreeObserver.OnPreDrawListener mEnableAnimationPredrawListener = new ViewTreeObserver.OnPreDrawListener() {
        public boolean onPreDraw() {
            NotificationContentView.this.post(new Runnable() {
                public void run() {
                    boolean unused = NotificationContentView.this.mAnimate = true;
                }
            });
            NotificationContentView.this.getViewTreeObserver().removeOnPreDrawListener(this);
            return true;
        }
    };
    private View.OnClickListener mExpandClickListener;
    private boolean mExpandable;
    private View mExpandedChild;
    private RemoteInputView mExpandedRemoteInput;
    private Runnable mExpandedVisibleListener;
    private NotificationViewWrapper mExpandedWrapper;
    private boolean mFocusOnVisibilityChange;
    private boolean mForceSelectNextLayout = true;
    private NotificationGroupManager mGroupManager;
    private boolean mHeadsUpAnimatingAway;
    private View mHeadsUpChild;
    private int mHeadsUpHeight;
    private RemoteInputView mHeadsUpRemoteInput;
    private NotificationViewWrapper mHeadsUpWrapper;
    private HybridGroupManager mHybridGroupManager = new HybridGroupManager(getContext(), this);
    private boolean mIconsVisible;
    private boolean mIsChildInGroup;
    private boolean mIsContentExpandable;
    private boolean mIsHeadsUp;
    private boolean mIsLowPriority;
    private boolean mLegacy;
    private int mLowPriorityNotificationHeight;
    private MediaTransferManager mMediaTransferManager = new MediaTransferManager(getContext());
    private int mMinContractedHeight;
    private int mNotificationAmbientHeight;
    /* access modifiers changed from: private */
    public int mNotificationBgRadius;
    private int mNotificationContentMarginEnd;
    private int mNotificationCustomViewMargin;
    private int mNotificationMaxHeight;
    private PendingIntent mPreviousExpandedRemoteInputIntent;
    private PendingIntent mPreviousHeadsUpRemoteInputIntent;
    private RemoteInputController mRemoteInputController;
    private HybridNotificationView mSingleLineView;
    private int mSingleLineWidthIndention;
    private int mSmallHeight;
    private ExpandedNotification mStatusBarNotification;
    private int mTransformationStartVisibleType;
    private boolean mUserExpanding;
    /* access modifiers changed from: private */
    public int mVisibleType = 0;

    public boolean hasOverlappingRendering() {
        return false;
    }

    public NotificationContentView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initDimens();
        setOutlineProvider(new ViewOutlineProvider() {
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(NotificationContentView.this.mClipBounds, (float) NotificationContentView.this.mNotificationBgRadius);
            }
        });
        setClipToOutline(true);
    }

    private void initDimens() {
        this.mMinContractedHeight = getResources().getDimensionPixelSize(R.dimen.min_notification_layout_height);
        this.mNotificationContentMarginEnd = getResources().getDimensionPixelSize(17105360);
        this.mLowPriorityNotificationHeight = getResources().getDimensionPixelSize(R.dimen.low_priority_notification_layout_height);
        this.mNotificationBgRadius = NotificationUtil.getOutlineRadius(this.mContext);
        this.mNotificationCustomViewMargin = NotificationUtil.getCustomViewMargin(this.mContext);
    }

    public void setHeights(int i, int i2, int i3, int i4) {
        this.mSmallHeight = i;
        this.mHeadsUpHeight = i2;
        this.mNotificationMaxHeight = i3;
        this.mNotificationAmbientHeight = i4;
    }

    public void onDensityOrFontScaleChanged() {
        initDimens();
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int i3;
        boolean z;
        boolean z2;
        boolean z3;
        int i4;
        int i5;
        boolean z4;
        int mode = View.MeasureSpec.getMode(i2);
        boolean z5 = true;
        int i6 = 1073741824;
        boolean z6 = mode == 1073741824;
        boolean z7 = mode == Integer.MIN_VALUE;
        int i7 = 1073741823;
        int size = View.MeasureSpec.getSize(i);
        if ((z6 || z7) && !this.mIsHeadsUp) {
            i7 = View.MeasureSpec.getSize(i2);
        }
        View view = this.mExpandedChild;
        if (view != null) {
            int i8 = this.mNotificationMaxHeight;
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            int i9 = layoutParams.height;
            if (i9 >= 0) {
                i8 = Math.min(i8, i9);
                z4 = true;
            } else {
                z4 = false;
            }
            this.mExpandedChild.measure(getChildWidthSpec(i, (ViewGroup.MarginLayoutParams) layoutParams), View.MeasureSpec.makeMeasureSpec(i8, z4 ? 1073741824 : Integer.MIN_VALUE));
            i3 = Math.max(0, this.mExpandedChild.getMeasuredHeight());
        } else {
            i3 = 0;
        }
        View view2 = this.mContractedChild;
        if (view2 != null) {
            int i10 = this.mSmallHeight;
            ViewGroup.LayoutParams layoutParams2 = view2.getLayoutParams();
            int i11 = layoutParams2.height;
            if (i11 >= 0) {
                i10 = Math.min(i10, i11);
                z3 = true;
            } else {
                z3 = false;
            }
            boolean isCustomViewNotification = isCustomViewNotification(this.mStatusBarNotification);
            if ((isCustomViewNotification && this.mStatusBarNotification.isCustomHeight()) || isMediaNotification(this.mStatusBarNotification)) {
                i4 = View.MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE, Integer.MIN_VALUE);
            } else if (shouldContractedBeFixedSize() || z3) {
                i4 = View.MeasureSpec.makeMeasureSpec(i10, 1073741824);
            } else {
                i4 = View.MeasureSpec.makeMeasureSpec(i10, Integer.MIN_VALUE);
            }
            int childWidthSpec = getChildWidthSpec(i, (ViewGroup.MarginLayoutParams) layoutParams2);
            this.mContractedChild.measure(childWidthSpec, i4);
            int measuredHeight = this.mContractedChild.getMeasuredHeight();
            if (!isCustomViewNotification && measuredHeight < (i5 = this.mMinContractedHeight)) {
                if (this.mIsLowPriority) {
                    i5 = this.mLowPriorityNotificationHeight;
                }
                i4 = View.MeasureSpec.makeMeasureSpec(i5, 1073741824);
                this.mContractedChild.measure(childWidthSpec, i4);
            }
            i3 = Math.max(i3, measuredHeight);
            if (updateContractedHeaderWidth()) {
                this.mContractedChild.measure(childWidthSpec, i4);
            }
            if (this.mExpandedChild != null && this.mContractedChild.getMeasuredHeight() > this.mExpandedChild.getMeasuredHeight()) {
                this.mExpandedChild.measure(childWidthSpec, View.MeasureSpec.makeMeasureSpec(this.mContractedChild.getMeasuredHeight(), 1073741824));
            }
        }
        if (this.mHeadsUpChild != null) {
            int min = Math.min(i7, this.mHeadsUpHeight);
            ViewGroup.LayoutParams layoutParams3 = this.mHeadsUpChild.getLayoutParams();
            int i12 = layoutParams3.height;
            if (i12 >= 0) {
                min = Math.min(min, i12);
                z2 = true;
            } else {
                z2 = false;
            }
            this.mHeadsUpChild.measure(getChildWidthSpec(i, (ViewGroup.MarginLayoutParams) layoutParams3), View.MeasureSpec.makeMeasureSpec(min, z2 ? 1073741824 : Integer.MIN_VALUE));
            i3 = Math.max(i3, this.mHeadsUpChild.getMeasuredHeight());
        }
        if (this.mSingleLineView != null) {
            this.mSingleLineView.measure((this.mSingleLineWidthIndention == 0 || View.MeasureSpec.getMode(i) == 0) ? i : View.MeasureSpec.makeMeasureSpec((size - this.mSingleLineWidthIndention) + this.mSingleLineView.getPaddingEnd(), 1073741824), View.MeasureSpec.makeMeasureSpec(i7, Integer.MIN_VALUE));
            i3 = Math.max(i3, this.mSingleLineView.getMeasuredHeight());
        }
        if (this.mAmbientChild != null) {
            int min2 = Math.min(i7, this.mNotificationAmbientHeight);
            ViewGroup.LayoutParams layoutParams4 = this.mAmbientChild.getLayoutParams();
            int i13 = layoutParams4.height;
            if (i13 >= 0) {
                min2 = Math.min(min2, i13);
                z = true;
            } else {
                z = false;
            }
            this.mAmbientChild.measure(getChildWidthSpec(i, (ViewGroup.MarginLayoutParams) layoutParams4), View.MeasureSpec.makeMeasureSpec(min2, z ? 1073741824 : Integer.MIN_VALUE));
            i3 = Math.max(i3, this.mAmbientChild.getMeasuredHeight());
        }
        if (this.mAmbientSingleLineChild != null) {
            int min3 = Math.min(i7, this.mNotificationAmbientHeight);
            int i14 = this.mAmbientSingleLineChild.getLayoutParams().height;
            if (i14 >= 0) {
                min3 = Math.min(min3, i14);
            } else {
                z5 = false;
            }
            if (!(this.mSingleLineWidthIndention == 0 || View.MeasureSpec.getMode(i) == 0)) {
                i = View.MeasureSpec.makeMeasureSpec((size - this.mSingleLineWidthIndention) + this.mAmbientSingleLineChild.getPaddingEnd(), 1073741824);
            }
            HybridNotificationView hybridNotificationView = this.mAmbientSingleLineChild;
            if (!z5) {
                i6 = Integer.MIN_VALUE;
            }
            hybridNotificationView.measure(i, View.MeasureSpec.makeMeasureSpec(min3, i6));
            i3 = Math.max(i3, this.mAmbientSingleLineChild.getMeasuredHeight());
        }
        int min4 = Math.min(i3, i7);
        if (isCustomViewNotification(this.mStatusBarNotification)) {
            min4 += this.mNotificationCustomViewMargin * 2;
        } else if (NotificationUtil.showSingleLine(this.mStatusBarNotification.getNotification())) {
            min4 = Math.max(min4, this.mMinContractedHeight);
        }
        setMeasuredDimension(size, min4);
    }

    private int getChildWidthSpec(int i, ViewGroup.MarginLayoutParams marginLayoutParams) {
        return marginLayoutParams.width == -1 ? View.MeasureSpec.makeMeasureSpec(Math.max(0, (View.MeasureSpec.getSize(i) - marginLayoutParams.getMarginEnd()) - marginLayoutParams.getMarginStart()), View.MeasureSpec.getMode(i)) : i;
    }

    private boolean updateContractedHeaderWidth() {
        int i;
        int i2;
        NotificationHeaderView notificationHeader = this.mContractedWrapper.getNotificationHeader();
        if (notificationHeader != null) {
            if (this.mExpandedChild == null || this.mExpandedWrapper.getNotificationHeader() == null || this.mExpandedWrapper.getNotificationHeader().getVisibility() != 0) {
                int i3 = this.mNotificationContentMarginEnd;
                if (notificationHeader.getPaddingEnd() != i3) {
                    if (notificationHeader.isLayoutRtl()) {
                        i = i3;
                    } else {
                        i = notificationHeader.getPaddingLeft();
                    }
                    int paddingTop = notificationHeader.getPaddingTop();
                    if (notificationHeader.isLayoutRtl()) {
                        i3 = notificationHeader.getPaddingLeft();
                    }
                    notificationHeader.setPadding(i, paddingTop, i3, notificationHeader.getPaddingBottom());
                    notificationHeader.setShowWorkBadgeAtEnd(false);
                    return true;
                }
            } else {
                NotificationHeaderView notificationHeader2 = this.mExpandedWrapper.getNotificationHeader();
                int measuredWidth = notificationHeader2.getMeasuredWidth() - notificationHeader2.getPaddingEnd();
                if (measuredWidth != notificationHeader.getMeasuredWidth() - notificationHeader2.getPaddingEnd()) {
                    int measuredWidth2 = notificationHeader.getMeasuredWidth() - measuredWidth;
                    if (notificationHeader.isLayoutRtl()) {
                        i2 = measuredWidth2;
                    } else {
                        i2 = notificationHeader.getPaddingLeft();
                    }
                    int paddingTop2 = notificationHeader.getPaddingTop();
                    if (notificationHeader.isLayoutRtl()) {
                        measuredWidth2 = notificationHeader.getPaddingLeft();
                    }
                    notificationHeader.setPadding(i2, paddingTop2, measuredWidth2, notificationHeader.getPaddingBottom());
                    notificationHeader.setShowWorkBadgeAtEnd(true);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean shouldContractedBeFixedSize() {
        return this.mBeforeN && (this.mContractedWrapper instanceof NotificationCustomViewWrapper);
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int viewHeight = this.mExpandedChild != null ? getViewHeight(1) : 0;
        super.onLayout(z, i, i2, i3, i4);
        if (!(viewHeight == 0 || getViewHeight(1) == viewHeight)) {
            this.mContentHeightAtAnimationStart = viewHeight;
        }
        updateClipping();
        selectLayout(false, this.mForceSelectNextLayout);
        this.mForceSelectNextLayout = false;
        updateExpandButtons(this.mExpandable);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateVisibility();
    }

    public View getContractedChild() {
        return this.mContractedChild;
    }

    public View getExpandedChild() {
        return this.mExpandedChild;
    }

    public View getHeadsUpChild() {
        return this.mHeadsUpChild;
    }

    public View getAmbientChild() {
        return this.mAmbientChild;
    }

    public HybridNotificationView getAmbientSingleLineChild() {
        return this.mAmbientSingleLineChild;
    }

    public void setContractedChild(View view) {
        View view2 = this.mContractedChild;
        if (view2 != null) {
            view2.animate().cancel();
            removeView(this.mContractedChild);
        }
        addView(view);
        this.mContractedChild = view;
        NotificationViewWrapper wrap = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification, NotificationViewWrapper.TYPE_SHOWING.TYPE_CONTRACTED);
        this.mContractedWrapper = wrap;
        wrap.setDark(this.mDark, false, 0);
    }

    private NotificationViewWrapper getWrapperForView(View view) {
        if (view == this.mContractedChild) {
            return this.mContractedWrapper;
        }
        if (view == this.mExpandedChild) {
            return this.mExpandedWrapper;
        }
        if (view == this.mHeadsUpChild) {
            return this.mHeadsUpWrapper;
        }
        if (view == this.mAmbientChild) {
            return this.mAmbientWrapper;
        }
        return null;
    }

    public void setExpandedChild(View view) {
        if (this.mExpandedChild != null) {
            this.mPreviousExpandedRemoteInputIntent = null;
            RemoteInputView remoteInputView = this.mExpandedRemoteInput;
            if (remoteInputView != null) {
                remoteInputView.onNotificationUpdateOrReset();
                if (this.mExpandedRemoteInput.isActive()) {
                    this.mPreviousExpandedRemoteInputIntent = this.mExpandedRemoteInput.getPendingIntent();
                    RemoteInputView remoteInputView2 = this.mExpandedRemoteInput;
                    this.mCachedExpandedRemoteInput = remoteInputView2;
                    remoteInputView2.dispatchStartTemporaryDetach();
                    ((ViewGroup) this.mExpandedRemoteInput.getParent()).removeView(this.mExpandedRemoteInput);
                }
            }
            this.mExpandedChild.animate().cancel();
            removeView(this.mExpandedChild);
            this.mExpandedRemoteInput = null;
        }
        if (view == null) {
            this.mExpandedChild = null;
            this.mExpandedWrapper = null;
            if (this.mVisibleType == 1) {
                this.mVisibleType = 0;
            }
            if (this.mTransformationStartVisibleType == 1) {
                this.mTransformationStartVisibleType = -1;
                return;
            }
            return;
        }
        addView(view);
        this.mExpandedChild = view;
        this.mExpandedWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification, NotificationViewWrapper.TYPE_SHOWING.TYPE_EXPANDED);
    }

    public void setHeadsUpChild(View view) {
        if (this.mHeadsUpChild != null) {
            this.mPreviousHeadsUpRemoteInputIntent = null;
            RemoteInputView remoteInputView = this.mHeadsUpRemoteInput;
            if (remoteInputView != null) {
                remoteInputView.onNotificationUpdateOrReset();
                if (this.mHeadsUpRemoteInput.isActive()) {
                    this.mPreviousHeadsUpRemoteInputIntent = this.mHeadsUpRemoteInput.getPendingIntent();
                    RemoteInputView remoteInputView2 = this.mHeadsUpRemoteInput;
                    this.mCachedHeadsUpRemoteInput = remoteInputView2;
                    remoteInputView2.dispatchStartTemporaryDetach();
                    ((ViewGroup) this.mHeadsUpRemoteInput.getParent()).removeView(this.mHeadsUpRemoteInput);
                }
            }
            this.mHeadsUpChild.animate().cancel();
            removeView(this.mHeadsUpChild);
            this.mHeadsUpRemoteInput = null;
        }
        if (view == null) {
            this.mHeadsUpChild = null;
            this.mHeadsUpWrapper = null;
            if (this.mVisibleType == 2) {
                this.mVisibleType = 0;
            }
            if (this.mTransformationStartVisibleType == 2) {
                this.mTransformationStartVisibleType = -1;
                return;
            }
            return;
        }
        addView(view);
        this.mHeadsUpChild = view;
        this.mHeadsUpWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification, NotificationViewWrapper.TYPE_SHOWING.TYPE_HEADSUP);
    }

    public void setAmbientChild(View view) {
        View view2 = this.mAmbientChild;
        if (view2 != null) {
            view2.animate().cancel();
            removeView(this.mAmbientChild);
        }
        if (view != null) {
            addView(view);
            this.mAmbientChild = view;
            this.mAmbientWrapper = NotificationViewWrapper.wrap(getContext(), view, this.mContainingNotification, NotificationViewWrapper.TYPE_SHOWING.TYPE_AMBIENT);
        }
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        updateVisibility();
    }

    private void updateVisibility() {
        setVisible(isShown());
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
    }

    private void setVisible(boolean z) {
        if (z) {
            getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
            getViewTreeObserver().addOnPreDrawListener(this.mEnableAnimationPredrawListener);
            return;
        }
        getViewTreeObserver().removeOnPreDrawListener(this.mEnableAnimationPredrawListener);
        this.mAnimate = false;
    }

    private void focusExpandButtonIfNecessary() {
        ImageView expandButton;
        if (this.mFocusOnVisibilityChange) {
            NotificationHeaderView visibleNotificationHeader = getVisibleNotificationHeader();
            if (!(visibleNotificationHeader == null || (expandButton = visibleNotificationHeader.getExpandButton()) == null)) {
                expandButton.requestAccessibilityFocus();
            }
            this.mFocusOnVisibilityChange = false;
        }
    }

    public void setContentHeight(int i) {
        this.mContentHeight = Math.max(Math.min(i, getHeight()), getMinHeight());
        selectLayout(this.mAnimate, false);
        int minContentHeightHint = getMinContentHeightHint();
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(this.mVisibleType);
        if (visibleWrapper != null) {
            visibleWrapper.setContentHeight(this.mContentHeight, minContentHeightHint);
        }
        NotificationViewWrapper visibleWrapper2 = getVisibleWrapper(this.mTransformationStartVisibleType);
        if (visibleWrapper2 != null) {
            visibleWrapper2.setContentHeight(this.mContentHeight, minContentHeightHint);
        }
        updateClipping();
    }

    private int getMinContentHeightHint() {
        int i;
        int i2;
        if (this.mIsChildInGroup && isVisibleOrTransitioning(3)) {
            return this.mContext.getResources().getDimensionPixelSize(17105347);
        }
        if (!(this.mHeadsUpChild == null || this.mExpandedChild == null)) {
            boolean z = isTransitioningFromTo(2, 1) || isTransitioningFromTo(1, 2);
            boolean z2 = !isVisibleOrTransitioning(0) && (this.mIsHeadsUp || this.mHeadsUpAnimatingAway) && !this.mContainingNotification.isOnKeyguard();
            if (z || z2) {
                return Math.min(getViewHeight(2), getViewHeight(1));
            }
        }
        if (this.mVisibleType == 1 && (i2 = this.mContentHeightAtAnimationStart) >= 0 && this.mExpandedChild != null) {
            return Math.min(i2, getViewHeight(1));
        }
        if (this.mAmbientChild != null && isVisibleOrTransitioning(4)) {
            i = getViewHeight(4);
        } else if (this.mAmbientSingleLineChild != null && isVisibleOrTransitioning(5)) {
            i = getViewHeight(5);
        } else if (this.mHeadsUpChild != null && isVisibleOrTransitioning(2)) {
            i = getViewHeight(2);
        } else if (this.mExpandedChild != null) {
            i = getViewHeight(1);
        } else {
            i = getViewHeight(0) + this.mContext.getResources().getDimensionPixelSize(17105347);
        }
        return (this.mExpandedChild == null || !isVisibleOrTransitioning(1)) ? i : Math.min(i, getViewHeight(1));
    }

    private boolean isTransitioningFromTo(int i, int i2) {
        return (this.mTransformationStartVisibleType == i || this.mAnimationStartVisibleType == i) && this.mVisibleType == i2;
    }

    private boolean isVisibleOrTransitioning(int i) {
        return this.mVisibleType == i || this.mTransformationStartVisibleType == i || this.mAnimationStartVisibleType == i;
    }

    private void updateContentTransformation() {
        int calculateVisibleType = calculateVisibleType();
        int i = this.mVisibleType;
        if (calculateVisibleType != i) {
            this.mTransformationStartVisibleType = i;
            TransformableView transformableViewForVisibleType = getTransformableViewForVisibleType(calculateVisibleType);
            TransformableView transformableViewForVisibleType2 = getTransformableViewForVisibleType(this.mTransformationStartVisibleType);
            transformableViewForVisibleType.transformFrom(transformableViewForVisibleType2, 0.0f);
            getViewForVisibleType(calculateVisibleType).setVisibility(0);
            transformableViewForVisibleType2.transformTo(transformableViewForVisibleType, 0.0f);
            this.mVisibleType = calculateVisibleType;
            updateBackgroundColor(true);
        }
        if (this.mForceSelectNextLayout) {
            forceUpdateVisibilities();
        }
        int i2 = this.mTransformationStartVisibleType;
        if (i2 == -1 || this.mVisibleType == i2 || getViewForVisibleType(i2) == null) {
            updateViewVisibilities(calculateVisibleType);
            updateBackgroundColor(false);
            return;
        }
        TransformableView transformableViewForVisibleType3 = getTransformableViewForVisibleType(this.mVisibleType);
        TransformableView transformableViewForVisibleType4 = getTransformableViewForVisibleType(this.mTransformationStartVisibleType);
        float calculateTransformationAmount = calculateTransformationAmount();
        transformableViewForVisibleType3.transformFrom(transformableViewForVisibleType4, calculateTransformationAmount);
        transformableViewForVisibleType4.transformTo(transformableViewForVisibleType3, calculateTransformationAmount);
        updateBackgroundTransformation(calculateTransformationAmount);
    }

    private void updateBackgroundTransformation(float f) {
        int backgroundColor = getBackgroundColor(this.mVisibleType);
        int backgroundColor2 = getBackgroundColor(this.mTransformationStartVisibleType);
        if (backgroundColor != backgroundColor2) {
            if (backgroundColor2 == 0) {
                backgroundColor2 = this.mContainingNotification.getBackgroundColorWithoutTint();
            }
            if (backgroundColor == 0) {
                backgroundColor = this.mContainingNotification.getBackgroundColorWithoutTint();
            }
            backgroundColor = NotificationUtils.interpolateColors(backgroundColor2, backgroundColor, f);
        }
        this.mContainingNotification.updateBackgroundAlpha(f);
        this.mContainingNotification.setContentBackground(backgroundColor, false, this);
    }

    private float calculateTransformationAmount() {
        int height = getViewForVisibleType(this.mTransformationStartVisibleType).getHeight();
        int height2 = getViewForVisibleType(this.mVisibleType).getHeight();
        return Math.min(1.0f, ((float) Math.abs(this.mContentHeight - height)) / ((float) Math.abs(height2 - height)));
    }

    public int getMaxHeight() {
        if (this.mContainingNotification.isShowingAmbient()) {
            return getShowingAmbientView().getHeight();
        }
        if (this.mExpandedChild != null) {
            return getViewHeight(1);
        }
        if (this.mIsHeadsUp && this.mHeadsUpChild != null && !this.mContainingNotification.isOnKeyguard()) {
            return getViewHeight(2);
        }
        if (this.mContractedChild != null) {
            return getViewHeight(0);
        }
        return this.mNotificationMaxHeight;
    }

    public int getHeadsUpHeight() {
        return getViewHeight(this.mHeadsUpChild == null ? 0 : 2);
    }

    private int getViewHeight(int i) {
        View viewForVisibleType = getViewForVisibleType(i);
        int height = viewForVisibleType != null ? viewForVisibleType.getHeight() : 0;
        NotificationViewWrapper wrapperForView = getWrapperForView(viewForVisibleType);
        if (wrapperForView != null) {
            height = height + wrapperForView.getHeaderTranslation() + wrapperForView.getMiniBarHeight();
        }
        return isCustomViewNotification(this.mStatusBarNotification) ? height + (this.mNotificationCustomViewMargin * 2) : height;
    }

    public int getMinHeight() {
        return getMinHeight(false);
    }

    public int getMinHeight(boolean z) {
        if (this.mContainingNotification.isShowingAmbient() && getShowingAmbientView() != null) {
            return getShowingAmbientView().getHeight();
        }
        if (z || !this.mIsChildInGroup || isGroupExpanded()) {
            return this.mContractedChild != null ? getViewHeight(0) : this.mMinContractedHeight;
        }
        return getViewHeight(3);
    }

    public View getShowingAmbientView() {
        View view = this.mIsChildInGroup ? this.mAmbientSingleLineChild : this.mAmbientChild;
        if (view != null) {
            return view;
        }
        return this.mContractedChild;
    }

    private boolean isGroupExpanded() {
        return this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
    }

    public void setClipTopAmount(int i) {
        this.mClipTopAmount = i;
        updateClipping();
    }

    public void setClipBottomAmount(int i) {
        this.mClipBottomAmount = i;
        updateClipping();
    }

    public void setTranslationY(float f) {
        super.setTranslationY(f);
        updateClipping();
    }

    private void updateClipping() {
        if (this.mClipToActualHeight) {
            int translationY = (int) (((float) this.mClipTopAmount) - getTranslationY());
            this.mClipBounds.set(0, translationY, getWidth(), Math.max(translationY, (int) (((float) (this.mContentHeight - this.mClipBottomAmount)) - getTranslationY())));
            setClipBounds(this.mClipBounds);
        } else {
            setClipBounds((Rect) null);
        }
        invalidateOutline();
    }

    public void setClipToActualHeight(boolean z) {
        this.mClipToActualHeight = z;
        setClipToOutline(z);
        updateClipping();
    }

    private void selectLayout(boolean z, boolean z2) {
        if (this.mContractedChild != null) {
            if (this.mUserExpanding) {
                updateContentTransformation();
                return;
            }
            int calculateVisibleType = calculateVisibleType();
            boolean z3 = calculateVisibleType != this.mVisibleType;
            if (z3 || z2) {
                View viewForVisibleType = getViewForVisibleType(calculateVisibleType);
                if (viewForVisibleType != null) {
                    viewForVisibleType.setVisibility(0);
                    transferRemoteInputFocus(calculateVisibleType);
                }
                if (!z || ((calculateVisibleType != 1 || this.mExpandedChild == null) && ((calculateVisibleType != 2 || this.mHeadsUpChild == null) && ((calculateVisibleType != 3 || this.mSingleLineView == null) && calculateVisibleType != 0)))) {
                    updateViewVisibilities(calculateVisibleType);
                } else {
                    animateToVisibleType(calculateVisibleType);
                }
                this.mVisibleType = calculateVisibleType;
                if (z3) {
                    focusExpandButtonIfNecessary();
                }
                NotificationViewWrapper visibleWrapper = getVisibleWrapper(calculateVisibleType);
                if (visibleWrapper != null) {
                    visibleWrapper.setContentHeight(this.mContentHeight, getMinContentHeightHint());
                }
                updateBackgroundColor(z);
            }
        }
    }

    private void forceUpdateVisibilities() {
        forceUpdateVisibility(0, this.mContractedChild, this.mContractedWrapper);
        forceUpdateVisibility(1, this.mExpandedChild, this.mExpandedWrapper);
        forceUpdateVisibility(2, this.mHeadsUpChild, this.mHeadsUpWrapper);
        HybridNotificationView hybridNotificationView = this.mSingleLineView;
        forceUpdateVisibility(3, hybridNotificationView, hybridNotificationView);
        forceUpdateVisibility(4, this.mAmbientChild, this.mAmbientWrapper);
        HybridNotificationView hybridNotificationView2 = this.mAmbientSingleLineChild;
        forceUpdateVisibility(5, hybridNotificationView2, hybridNotificationView2);
        fireExpandedVisibleListenerIfVisible();
        this.mAnimationStartVisibleType = -1;
    }

    private void fireExpandedVisibleListenerIfVisible() {
        if (this.mExpandedVisibleListener != null && this.mExpandedChild != null && isShown() && this.mExpandedChild.getVisibility() == 0) {
            Runnable runnable = this.mExpandedVisibleListener;
            this.mExpandedVisibleListener = null;
            runnable.run();
        }
    }

    private void forceUpdateVisibility(int i, View view, TransformableView transformableView) {
        if (view != null) {
            if (!(this.mVisibleType == i || this.mTransformationStartVisibleType == i)) {
                view.setVisibility(4);
            } else {
                transformableView.setVisible(true);
            }
        }
    }

    public void updateBackgroundColor(boolean z) {
        int backgroundColor = getBackgroundColor(this.mVisibleType);
        this.mContainingNotification.resetBackgroundAlpha();
        this.mContainingNotification.setContentBackground(backgroundColor, z, this);
    }

    public int getVisibleType() {
        return this.mVisibleType;
    }

    public int getBackgroundColor(int i) {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(i);
        if (visibleWrapper != null) {
            return visibleWrapper.getCustomBackgroundColor();
        }
        return 0;
    }

    private void updateViewVisibilities(int i) {
        updateViewVisibility(i, 0, this.mContractedChild, this.mContractedWrapper);
        updateViewVisibility(i, 1, this.mExpandedChild, this.mExpandedWrapper);
        updateViewVisibility(i, 2, this.mHeadsUpChild, this.mHeadsUpWrapper);
        HybridNotificationView hybridNotificationView = this.mSingleLineView;
        updateViewVisibility(i, 3, hybridNotificationView, hybridNotificationView);
        updateViewVisibility(i, 4, this.mAmbientChild, this.mAmbientWrapper);
        HybridNotificationView hybridNotificationView2 = this.mAmbientSingleLineChild;
        updateViewVisibility(i, 5, hybridNotificationView2, hybridNotificationView2);
        fireExpandedVisibleListenerIfVisible();
        this.mAnimationStartVisibleType = -1;
        if (this.mContainingNotification.isChildInGroup()) {
            this.mContainingNotification.getNotificationParent().getChildrenContainer().requestLayout();
        }
    }

    private void updateViewVisibility(int i, int i2, View view, TransformableView transformableView) {
        if (view != null) {
            transformableView.setVisible(i == i2);
        }
    }

    private void animateToVisibleType(int i) {
        TransformableView transformableViewForVisibleType = getTransformableViewForVisibleType(i);
        final TransformableView transformableViewForVisibleType2 = getTransformableViewForVisibleType(this.mVisibleType);
        if (transformableViewForVisibleType == transformableViewForVisibleType2 || transformableViewForVisibleType2 == null) {
            transformableViewForVisibleType.setVisible(true);
            return;
        }
        this.mAnimationStartVisibleType = this.mVisibleType;
        transformableViewForVisibleType.transformFrom(transformableViewForVisibleType2);
        getViewForVisibleType(i).setVisibility(0);
        transformableViewForVisibleType2.transformTo(transformableViewForVisibleType, (Runnable) new Runnable() {
            public void run() {
                TransformableView transformableView = transformableViewForVisibleType2;
                NotificationContentView notificationContentView = NotificationContentView.this;
                if (transformableView != notificationContentView.getTransformableViewForVisibleType(notificationContentView.mVisibleType)) {
                    transformableViewForVisibleType2.setVisible(false);
                }
                int unused = NotificationContentView.this.mAnimationStartVisibleType = -1;
                if (NotificationContentView.this.mContainingNotification.isChildInGroup()) {
                    NotificationContentView.this.mContainingNotification.getNotificationParent().getChildrenContainer().requestLayout();
                }
            }
        });
        fireExpandedVisibleListenerIfVisible();
    }

    private void transferRemoteInputFocus(int i) {
        RemoteInputView remoteInputView;
        RemoteInputView remoteInputView2;
        if (i == 2 && this.mHeadsUpRemoteInput != null && (remoteInputView2 = this.mExpandedRemoteInput) != null && remoteInputView2.isActive()) {
            this.mHeadsUpRemoteInput.stealFocusFrom(this.mExpandedRemoteInput);
        }
        if (i == 1 && this.mExpandedRemoteInput != null && (remoteInputView = this.mHeadsUpRemoteInput) != null && remoteInputView.isActive()) {
            this.mExpandedRemoteInput.stealFocusFrom(this.mHeadsUpRemoteInput);
        }
    }

    /* access modifiers changed from: private */
    public TransformableView getTransformableViewForVisibleType(int i) {
        TransformableView transformableView;
        if (i == 1) {
            transformableView = this.mExpandedWrapper;
        } else if (i == 2) {
            transformableView = this.mHeadsUpWrapper;
        } else if (i == 3) {
            transformableView = this.mSingleLineView;
        } else if (i == 4) {
            transformableView = this.mAmbientWrapper;
        } else if (i != 5) {
            transformableView = this.mContractedWrapper;
        } else {
            transformableView = this.mAmbientSingleLineChild;
        }
        return transformableView == null ? this.mContractedWrapper : transformableView;
    }

    public View getViewForVisibleType(int i) {
        if (i == 1) {
            return this.mExpandedChild;
        }
        if (i == 2) {
            return this.mHeadsUpChild;
        }
        if (i == 3) {
            return this.mSingleLineView;
        }
        if (i == 4) {
            return this.mAmbientChild;
        }
        if (i != 5) {
            return this.mContractedChild;
        }
        return this.mAmbientSingleLineChild;
    }

    public NotificationViewWrapper getVisibleWrapper(int i) {
        if (i == 0) {
            return this.mContractedWrapper;
        }
        if (i == 1) {
            return this.mExpandedWrapper;
        }
        if (i == 2) {
            return this.mHeadsUpWrapper;
        }
        if (i != 4) {
            return null;
        }
        return this.mAmbientWrapper;
    }

    private boolean isForceShowHeadUpChild() {
        if ((StatusBar.sGameMode || isLandscape(this.mContext)) && ((this.mIsHeadsUp || this.mHeadsUpAnimatingAway) && this.mHeadsUpChild != null && !this.mContainingNotification.isExpanded(false) && !this.mContainingNotification.isOnKeyguard())) {
            return true;
        }
        return false;
    }

    private boolean isLandscape(Context context) {
        return context.getResources().getConfiguration().orientation == 2;
    }

    public int calculateVisibleType() {
        int i;
        int i2;
        if (this.mContainingNotification.isShowingAmbient()) {
            if (!this.mIsChildInGroup || this.mAmbientSingleLineChild == null) {
                return this.mAmbientChild != null ? 4 : 0;
            }
            return 5;
        } else if (this.mUserExpanding) {
            if (!this.mIsChildInGroup || isGroupExpanded() || this.mContainingNotification.isExpanded(true)) {
                i = this.mContainingNotification.getMaxContentHeight();
            } else {
                i = this.mContainingNotification.getShowingLayout().getMinHeight();
            }
            if (i == 0) {
                i = this.mContentHeight;
            }
            int visualTypeForHeight = getVisualTypeForHeight((float) i);
            if (!this.mIsChildInGroup || isGroupExpanded()) {
                i2 = getVisualTypeForHeight((float) this.mContainingNotification.getCollapsedHeight());
            } else {
                i2 = 3;
            }
            return this.mTransformationStartVisibleType == i2 ? visualTypeForHeight : i2;
        } else {
            int intrinsicHeight = this.mContainingNotification.getIntrinsicHeight() - this.mContainingNotification.getExtraPadding();
            int i3 = this.mContentHeight;
            if (intrinsicHeight != 0) {
                i3 = Math.min(i3, intrinsicHeight);
            }
            return getVisualTypeForHeight((float) i3);
        }
    }

    private int getVisualTypeForHeight(float f) {
        if (isForceShowHeadUpChild()) {
            return 2;
        }
        boolean z = this.mExpandedChild == null;
        if (!z && f == ((float) getViewHeight(1))) {
            return 1;
        }
        if (!this.mUserExpanding && this.mIsChildInGroup && !isGroupExpanded()) {
            return 3;
        }
        if ((!this.mIsHeadsUp && !this.mHeadsUpAnimatingAway) || this.mHeadsUpChild == null || this.mContainingNotification.isOnKeyguard()) {
            return (z || (this.mContractedChild != null && f <= ((float) getViewHeight(0)) && (!this.mIsChildInGroup || isGroupExpanded() || !this.mContainingNotification.isExpanded(true)))) ? 0 : 1;
        }
        if (f <= ((float) getViewHeight(2)) || z) {
            return 2;
        }
        return 1;
    }

    public boolean isContentExpandable() {
        return this.mIsContentExpandable;
    }

    public void setDark(boolean z, boolean z2, long j) {
        if (this.mContractedChild != null) {
            this.mDark = z;
            if (this.mVisibleType == 0 || !z) {
                this.mContractedWrapper.setDark(z, z2, j);
            }
            boolean z3 = true;
            if (this.mVisibleType == 1 || (this.mExpandedChild != null && !z)) {
                this.mExpandedWrapper.setDark(z, z2, j);
            }
            if (this.mVisibleType == 2 || (this.mHeadsUpChild != null && !z)) {
                this.mHeadsUpWrapper.setDark(z, z2, j);
            }
            if (this.mSingleLineView != null && (this.mVisibleType == 3 || !z)) {
                this.mSingleLineView.setDark(z, z2, j);
            }
            if (z || !z2) {
                z3 = false;
            }
            selectLayout(z3, false);
        }
    }

    public void setHeadsUp(boolean z) {
        this.mIsHeadsUp = z;
        View view = this.mHeadsUpChild;
        if (view != null && (view instanceof InCallNotificationView)) {
            if (z) {
                ((InCallNotificationView) view).show();
            } else {
                ((InCallNotificationView) view).hide();
            }
        }
        selectLayout(false, true);
        updateExpandButtons(this.mExpandable);
    }

    public void setLegacy(boolean z) {
        this.mLegacy = z;
        updateLegacy();
    }

    private void updateLegacy() {
        if (this.mContractedChild != null) {
            this.mContractedWrapper.setLegacy(this.mLegacy);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.setLegacy(this.mLegacy);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.setLegacy(this.mLegacy);
        }
    }

    public void setIsChildInGroup(boolean z) {
        this.mIsChildInGroup = z;
        if (this.mContractedChild != null) {
            this.mContractedWrapper.setIsChildInGroup(z);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.setIsChildInGroup(this.mIsChildInGroup);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.setIsChildInGroup(this.mIsChildInGroup);
        }
        if (this.mAmbientChild != null) {
            this.mAmbientWrapper.setIsChildInGroup(this.mIsChildInGroup);
        }
        updateAllSingleLineViews();
    }

    public void onNotificationUpdated(NotificationData.Entry entry) {
        this.mStatusBarNotification = entry.notification;
        this.mBeforeN = entry.targetSdk < 24;
        updateAllSingleLineViews();
        if (this.mContractedChild != null) {
            this.mContractedWrapper.onContentUpdated(entry.row);
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.onContentUpdated(entry.row);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.onContentUpdated(entry.row);
        }
        if (this.mAmbientChild != null) {
            this.mAmbientWrapper.onContentUpdated(entry.row);
        }
        applyRemoteInput(entry);
        applyMediaTransfer(entry);
        updateLegacy();
        this.mForceSelectNextLayout = true;
        setDark(this.mDark, false, 0);
        this.mPreviousExpandedRemoteInputIntent = null;
        this.mPreviousHeadsUpRemoteInputIntent = null;
    }

    private void updateAllSingleLineViews() {
        updateSingleLineView();
        if (Constants.SUPPORT_AOD) {
            updateAmbientSingleLineView();
        }
    }

    private void updateSingleLineView() {
        if (this.mIsChildInGroup) {
            this.mSingleLineView = this.mHybridGroupManager.bindFromNotification(this.mSingleLineView, this.mStatusBarNotification.getNotification());
            return;
        }
        HybridNotificationView hybridNotificationView = this.mSingleLineView;
        if (hybridNotificationView != null) {
            removeView(hybridNotificationView);
            this.mSingleLineView = null;
        }
    }

    private void applyMediaTransfer(NotificationData.Entry entry) {
        if (entry.isMediaNotification()) {
            View view = this.mExpandedChild;
            if (view != null && (view instanceof ViewGroup)) {
                this.mMediaTransferManager.applyMediaTransferView((ViewGroup) view, entry);
            }
            View view2 = this.mContractedChild;
            if (view2 != null && (view2 instanceof ViewGroup)) {
                this.mMediaTransferManager.applyMediaTransferView((ViewGroup) view2, entry);
            }
        }
    }

    private void updateAmbientSingleLineView() {
        if (this.mIsChildInGroup) {
            this.mAmbientSingleLineChild = this.mHybridGroupManager.bindAmbientFromNotification(this.mAmbientSingleLineChild, this.mStatusBarNotification.getNotification());
            return;
        }
        HybridNotificationView hybridNotificationView = this.mAmbientSingleLineChild;
        if (hybridNotificationView != null) {
            removeView(hybridNotificationView);
            this.mAmbientSingleLineChild = null;
        }
    }

    private void applyRemoteInput(NotificationData.Entry entry) {
        if (this.mRemoteInputController != null) {
            Notification.Action[] actionArr = entry.notification.getNotification().actions;
            boolean z = false;
            if (actionArr != null) {
                boolean z2 = false;
                for (Notification.Action action : actionArr) {
                    if (action.getRemoteInputs() != null) {
                        RemoteInput[] remoteInputs = action.getRemoteInputs();
                        int length = remoteInputs.length;
                        int i = 0;
                        while (true) {
                            if (i >= length) {
                                break;
                            } else if (remoteInputs[i].getAllowFreeFormInput()) {
                                z2 = true;
                                break;
                            } else {
                                i++;
                            }
                        }
                    }
                }
                z = z2;
            }
            View view = this.mExpandedChild;
            if (view != null) {
                this.mExpandedRemoteInput = applyRemoteInput(view, entry, z, this.mPreviousExpandedRemoteInputIntent, this.mCachedExpandedRemoteInput, this.mExpandedWrapper);
            } else {
                this.mExpandedRemoteInput = null;
            }
            RemoteInputView remoteInputView = this.mCachedExpandedRemoteInput;
            if (!(remoteInputView == null || remoteInputView == this.mExpandedRemoteInput)) {
                remoteInputView.dispatchFinishTemporaryDetach();
            }
            this.mCachedExpandedRemoteInput = null;
            View view2 = this.mHeadsUpChild;
            if (view2 != null) {
                this.mHeadsUpRemoteInput = applyRemoteInput(view2, entry, z, this.mPreviousHeadsUpRemoteInputIntent, this.mCachedHeadsUpRemoteInput, this.mHeadsUpWrapper);
            } else {
                this.mHeadsUpRemoteInput = null;
            }
            RemoteInputView remoteInputView2 = this.mCachedHeadsUpRemoteInput;
            if (!(remoteInputView2 == null || remoteInputView2 == this.mHeadsUpRemoteInput)) {
                remoteInputView2.dispatchFinishTemporaryDetach();
            }
            this.mCachedHeadsUpRemoteInput = null;
        }
    }

    private RemoteInputView applyRemoteInput(View view, NotificationData.Entry entry, boolean z, PendingIntent pendingIntent, RemoteInputView remoteInputView, NotificationViewWrapper notificationViewWrapper) {
        View findViewById = view.findViewById(16908724);
        if (!(findViewById instanceof FrameLayout)) {
            return null;
        }
        RemoteInputView remoteInputView2 = (RemoteInputView) view.findViewWithTag(RemoteInputView.VIEW_TAG);
        if (remoteInputView2 != null) {
            remoteInputView2.onNotificationUpdateOrReset();
        }
        if (remoteInputView2 != null || !z) {
            remoteInputView = remoteInputView2;
        } else {
            FrameLayout frameLayout = (FrameLayout) findViewById;
            if (remoteInputView == null) {
                remoteInputView = RemoteInputView.inflate(this.mContext, frameLayout, entry, this.mRemoteInputController);
                remoteInputView.setVisibility(4);
                frameLayout.addView(remoteInputView, new FrameLayout.LayoutParams(-1, -1));
            } else {
                frameLayout.addView(remoteInputView);
                remoteInputView.dispatchFinishTemporaryDetach();
                remoteInputView.requestFocus();
            }
        }
        if (z) {
            remoteInputView.setWrapper(notificationViewWrapper);
            if (pendingIntent != null || remoteInputView.isActive()) {
                Notification.Action[] actionArr = entry.notification.getNotification().actions;
                if (pendingIntent != null) {
                    remoteInputView.setPendingIntent(pendingIntent);
                }
                if (remoteInputView.updatePendingIntentFromActions(actionArr)) {
                    if (!remoteInputView.isActive()) {
                        remoteInputView.focus();
                    }
                } else if (remoteInputView.isActive()) {
                    remoteInputView.close();
                }
            }
        }
        return remoteInputView;
    }

    public void closeRemoteInput() {
        RemoteInputView remoteInputView = this.mHeadsUpRemoteInput;
        if (remoteInputView != null) {
            remoteInputView.close();
        }
        RemoteInputView remoteInputView2 = this.mExpandedRemoteInput;
        if (remoteInputView2 != null) {
            remoteInputView2.close();
        }
    }

    public void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
    }

    public void setRemoteInputController(RemoteInputController remoteInputController) {
        this.mRemoteInputController = remoteInputController;
    }

    public void setExpandClickListener(View.OnClickListener onClickListener) {
        this.mExpandClickListener = onClickListener;
    }

    public void updateExpandButtons(boolean z) {
        this.mExpandable = z;
        if (!(this.mExpandedChild == null || getViewHeight(1) == 0 || ((this.mIsHeadsUp || this.mHeadsUpAnimatingAway) && this.mHeadsUpChild != null && !this.mContainingNotification.isOnKeyguard() ? getViewHeight(1) > getViewHeight(2) : getViewHeight(1) > getViewHeight(0)))) {
            z = false;
        }
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.updateExpandability(z, this.mExpandClickListener);
        }
        if (this.mContractedChild != null) {
            this.mContractedWrapper.updateExpandability(z, this.mExpandClickListener);
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.updateExpandability(z, this.mExpandClickListener);
        }
        this.mIsContentExpandable = z;
    }

    public void showPublic() {
        if (this.mExpandedChild != null) {
            this.mExpandedWrapper.showPublic();
        }
        if (this.mContractedChild != null) {
            this.mContractedWrapper.showPublic();
        }
        if (this.mHeadsUpChild != null) {
            this.mHeadsUpWrapper.showPublic();
        }
    }

    public NotificationHeaderView getNotificationHeader() {
        NotificationHeaderView notificationHeader = this.mContractedChild != null ? this.mContractedWrapper.getNotificationHeader() : null;
        if (notificationHeader == null && this.mExpandedChild != null) {
            notificationHeader = this.mExpandedWrapper.getNotificationHeader();
        }
        if (notificationHeader == null && this.mHeadsUpChild != null) {
            notificationHeader = this.mHeadsUpWrapper.getNotificationHeader();
        }
        return (notificationHeader != null || this.mAmbientChild == null) ? notificationHeader : this.mAmbientWrapper.getNotificationHeader();
    }

    public NotificationHeaderView getVisibleNotificationHeader() {
        NotificationViewWrapper visibleWrapper = getVisibleWrapper(this.mVisibleType);
        if (visibleWrapper == null) {
            return null;
        }
        return visibleWrapper.getNotificationHeader();
    }

    public void setContainingNotification(ExpandableNotificationRow expandableNotificationRow) {
        this.mContainingNotification = expandableNotificationRow;
    }

    public void requestSelectLayout(boolean z) {
        selectLayout(z, false);
    }

    public void reInflateViews() {
        HybridNotificationView hybridNotificationView;
        if (this.mIsChildInGroup && (hybridNotificationView = this.mSingleLineView) != null) {
            removeView(hybridNotificationView);
            this.mSingleLineView = null;
            updateAllSingleLineViews();
        }
    }

    public void setUserExpanding(boolean z) {
        this.mUserExpanding = z;
        if (z) {
            this.mTransformationStartVisibleType = this.mVisibleType;
            return;
        }
        this.mTransformationStartVisibleType = -1;
        int calculateVisibleType = calculateVisibleType();
        this.mVisibleType = calculateVisibleType;
        updateViewVisibilities(calculateVisibleType);
        updateBackgroundColor(false);
    }

    public void setSingleLineWidthIndention(int i) {
        if (i != this.mSingleLineWidthIndention) {
            this.mSingleLineWidthIndention = i;
            this.mContainingNotification.forceLayout();
            forceLayout();
        }
    }

    public HybridNotificationView getSingleLineView() {
        return this.mSingleLineView;
    }

    public void setRemoved() {
        RemoteInputView remoteInputView = this.mExpandedRemoteInput;
        if (remoteInputView != null) {
            remoteInputView.setRemoved();
        }
        RemoteInputView remoteInputView2 = this.mHeadsUpRemoteInput;
        if (remoteInputView2 != null) {
            remoteInputView2.setRemoved();
        }
    }

    public void setContentHeightAnimating(boolean z) {
        if (!z) {
            this.mContentHeightAtAnimationStart = -1;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean isAnimatingVisibleType() {
        return this.mAnimationStartVisibleType != -1;
    }

    public void setHeadsUpAnimatingAway(boolean z) {
        this.mHeadsUpAnimatingAway = z;
        selectLayout(false, true);
    }

    public void setFocusOnVisibilityChange() {
        this.mFocusOnVisibilityChange = true;
    }

    public void setIconsVisible(boolean z) {
        this.mIconsVisible = z;
        updateIconVisibilities();
    }

    private void updateIconVisibilities() {
        NotificationHeaderView notificationHeader;
        NotificationHeaderView notificationHeader2;
        NotificationHeaderView notificationHeader3;
        NotificationViewWrapper notificationViewWrapper = this.mContractedWrapper;
        if (!(notificationViewWrapper == null || (notificationHeader3 = notificationViewWrapper.getNotificationHeader()) == null)) {
            notificationHeader3.getIcon().setForceHidden(!this.mIconsVisible);
        }
        NotificationViewWrapper notificationViewWrapper2 = this.mHeadsUpWrapper;
        if (!(notificationViewWrapper2 == null || (notificationHeader2 = notificationViewWrapper2.getNotificationHeader()) == null)) {
            notificationHeader2.getIcon().setForceHidden(!this.mIconsVisible);
        }
        NotificationViewWrapper notificationViewWrapper3 = this.mExpandedWrapper;
        if (notificationViewWrapper3 != null && (notificationHeader = notificationViewWrapper3.getNotificationHeader()) != null) {
            notificationHeader.getIcon().setForceHidden(!this.mIconsVisible);
        }
    }

    public void setMiniBarVisible(boolean z) {
        NotificationViewWrapper notificationViewWrapper = this.mContractedWrapper;
        if (notificationViewWrapper != null) {
            notificationViewWrapper.setMiniBarVisible(z);
        }
        NotificationViewWrapper notificationViewWrapper2 = this.mHeadsUpWrapper;
        if (notificationViewWrapper2 != null) {
            notificationViewWrapper2.setMiniBarVisible(z);
        }
        NotificationViewWrapper notificationViewWrapper3 = this.mExpandedWrapper;
        if (notificationViewWrapper3 != null) {
            notificationViewWrapper3.setMiniBarVisible(z);
        }
    }

    public void onVisibilityAggregated(boolean z) {
        super.onVisibilityAggregated(z);
        if (z) {
            fireExpandedVisibleListenerIfVisible();
        }
    }

    public void setOnExpandedVisibleListener(Runnable runnable) {
        this.mExpandedVisibleListener = runnable;
        fireExpandedVisibleListenerIfVisible();
    }

    public void setIsLowPriority(boolean z) {
        this.mIsLowPriority = z;
    }

    public boolean isDimmable() {
        NotificationViewWrapper notificationViewWrapper = this.mContractedWrapper;
        return notificationViewWrapper != null && notificationViewWrapper.isDimmable();
    }

    public boolean isMediaNotification(ExpandedNotification expandedNotification) {
        NotificationViewWrapper notificationViewWrapper = this.mContractedWrapper;
        if (notificationViewWrapper != null) {
            return notificationViewWrapper instanceof NotificationMediaTemplateViewWrapper;
        }
        return NotificationUtil.isMediaNotification(expandedNotification);
    }

    public boolean isCustomViewNotification(ExpandedNotification expandedNotification) {
        NotificationViewWrapper notificationViewWrapper = this.mContractedWrapper;
        if (notificationViewWrapper != null) {
            return notificationViewWrapper instanceof NotificationCustomViewWrapper;
        }
        return NotificationUtil.isCustomViewNotification(expandedNotification);
    }

    public void setContractedChildText(CharSequence charSequence) {
        NotificationViewWrapper notificationViewWrapper = this.mContractedWrapper;
        if (notificationViewWrapper instanceof NotificationTemplateViewWrapper) {
            ((NotificationTemplateViewWrapper) notificationViewWrapper).forceSetText(charSequence);
        }
    }
}

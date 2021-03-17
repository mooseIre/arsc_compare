package com.android.systemui.statusbar.notification.stack;

import android.content.Context;
import android.util.MathUtils;
import android.view.View;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.statusbar.NotificationShelf;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ActivatableNotificationView;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.StackScrollAlgorithm;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.util.ArrayList;

public class AmbientState extends MiuiAmbientStateBase {
    private ActivatableNotificationView mActivatedChild;
    private int mAnchorViewIndex;
    private float mAppearFraction;
    private boolean mAppearing;
    private int mBaseZHeight;
    private float mCurrentScrollVelocity;
    private boolean mDimmed;
    private float mDozeAmount = 0.0f;
    private boolean mDozing;
    private ArrayList<ExpandableView> mDraggedViews = new ArrayList<>();
    private int mExpandAnimationTopChange;
    private ExpandableNotificationRow mExpandingNotification;
    private float mExpandingVelocity;
    private boolean mExpansionChanging;
    private HeadsUpManager mHeadUpManager;
    private float mHideAmount;
    private boolean mHideSensitive;
    private int mIntrinsicPadding;
    private ExpandableView mLastVisibleBackgroundChild;
    private int mLayoutHeight;
    private int mLayoutMinHeight;
    private float mMaxHeadsUpTranslation;
    private int mMaxLayoutHeight;
    private Runnable mOnPulseHeightChangedListener;
    private float mOverScrollBottomAmount;
    private float mOverScrollTopAmount;
    private boolean mPanelFullWidth;
    private boolean mPanelTracking;
    private float mPulseHeight = 100000.0f;
    private boolean mPulsing;
    private boolean mQsCustomizerShowing;
    private int mScrollY;
    private final StackScrollAlgorithm.SectionProvider mSectionProvider;
    private boolean mShadeExpanded;
    private NotificationShelf mShelf;
    private int mSpeedBumpIndex = -1;
    private float mStackTranslation;
    private int mStatusBarState;
    private int mTopPadding;
    private ExpandableNotificationRow mTrackedHeadsUpRow;
    private boolean mUnlockHintRunning;
    private int mZDistanceBetweenElements;

    private static int getBaseHeight(int i) {
        return i * 4;
    }

    public void setDismissAllInProgress(boolean z) {
    }

    public AmbientState(Context context, StackScrollAlgorithm.SectionProvider sectionProvider, HeadsUpManager headsUpManager) {
        this.mSectionProvider = sectionProvider;
        this.mHeadUpManager = headsUpManager;
        reload(context);
    }

    public void reload(Context context) {
        int zDistanceBetweenElements = getZDistanceBetweenElements(context);
        this.mZDistanceBetweenElements = zDistanceBetweenElements;
        this.mBaseZHeight = getBaseHeight(zDistanceBetweenElements);
    }

    private static int getZDistanceBetweenElements(Context context) {
        return Math.max(1, context.getResources().getDimensionPixelSize(C0012R$dimen.z_distance_between_notifications));
    }

    public static int getNotificationLaunchHeight(Context context) {
        return getBaseHeight(getZDistanceBetweenElements(context)) * 2;
    }

    public int getBaseZHeight() {
        return this.mBaseZHeight;
    }

    public int getZDistanceBetweenElements() {
        return this.mZDistanceBetweenElements;
    }

    public int getScrollY() {
        return this.mScrollY;
    }

    public void setScrollY(int i) {
        this.mScrollY = i;
    }

    public int getAnchorViewIndex() {
        return this.mAnchorViewIndex;
    }

    public void onBeginDrag(ExpandableView expandableView) {
        this.mDraggedViews.add(expandableView);
    }

    public void onDragFinished(View view) {
        this.mDraggedViews.remove(view);
    }

    public ArrayList<ExpandableView> getDraggedViews() {
        return this.mDraggedViews;
    }

    public void setDimmed(boolean z) {
        this.mDimmed = z;
    }

    public void setDozing(boolean z) {
        this.mDozing = z;
    }

    public void setHideAmount(float f) {
        if (f == 1.0f && this.mHideAmount != f) {
            setPulseHeight(100000.0f);
        }
        this.mHideAmount = f;
    }

    public float getHideAmount() {
        return this.mHideAmount;
    }

    public void setHideSensitive(boolean z) {
        this.mHideSensitive = z;
    }

    public void setActivatedChild(ActivatableNotificationView activatableNotificationView) {
        this.mActivatedChild = activatableNotificationView;
    }

    public boolean isDimmed() {
        return this.mDimmed && (!isPulseExpanding() || this.mDozeAmount != 1.0f);
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    public boolean isHideSensitive() {
        return this.mHideSensitive;
    }

    public ActivatableNotificationView getActivatedChild() {
        return this.mActivatedChild;
    }

    public void setOverScrollAmount(float f, boolean z) {
        if (z) {
            this.mOverScrollTopAmount = f;
        } else {
            this.mOverScrollBottomAmount = f;
        }
    }

    public float getOverScrollAmount(boolean z) {
        return z ? this.mOverScrollTopAmount : this.mOverScrollBottomAmount;
    }

    public int getSpeedBumpIndex() {
        return this.mSpeedBumpIndex;
    }

    public void setSpeedBumpIndex(int i) {
        this.mSpeedBumpIndex = i;
    }

    public StackScrollAlgorithm.SectionProvider getSectionProvider() {
        return this.mSectionProvider;
    }

    public float getStackTranslation() {
        return this.mStackTranslation;
    }

    public void setStackTranslation(float f) {
        this.mStackTranslation = f;
    }

    public void setLayoutHeight(int i) {
        this.mLayoutHeight = i;
    }

    public float getTopPadding() {
        return (float) this.mTopPadding;
    }

    public void setTopPadding(int i) {
        this.mTopPadding = i;
    }

    public int getInnerHeight() {
        return getInnerHeight(false);
    }

    public int getInnerHeight(boolean z) {
        if (this.mDozeAmount == 1.0f && !isPulseExpanding()) {
            return this.mShelf.getHeight();
        }
        int max = Math.max(this.mLayoutMinHeight, Math.min(this.mLayoutHeight, this.mMaxLayoutHeight) - this.mTopPadding);
        if (z) {
            return max;
        }
        float f = (float) max;
        return (int) MathUtils.lerp(f, Math.min(this.mPulseHeight, f), this.mDozeAmount);
    }

    public boolean isPulseExpanding() {
        return (this.mPulseHeight == 100000.0f || this.mDozeAmount == 0.0f || this.mHideAmount == 1.0f) ? false : true;
    }

    public boolean isShadeExpanded() {
        return this.mShadeExpanded;
    }

    public void setShadeExpanded(boolean z) {
        this.mShadeExpanded = z;
    }

    public void setMaxHeadsUpTranslation(float f) {
        this.mMaxHeadsUpTranslation = f;
    }

    public float getMaxHeadsUpTranslation() {
        return this.mMaxHeadsUpTranslation;
    }

    public void setLayoutMinHeight(int i) {
        this.mLayoutMinHeight = i;
    }

    public void setShelf(NotificationShelf notificationShelf) {
        this.mShelf = notificationShelf;
    }

    public NotificationShelf getShelf() {
        return this.mShelf;
    }

    public void setLayoutMaxHeight(int i) {
        this.mMaxLayoutHeight = i;
    }

    public void setLastVisibleBackgroundChild(ExpandableView expandableView) {
        this.mLastVisibleBackgroundChild = expandableView;
    }

    public ExpandableView getLastVisibleBackgroundChild() {
        return this.mLastVisibleBackgroundChild;
    }

    public void setCurrentScrollVelocity(float f) {
        this.mCurrentScrollVelocity = f;
    }

    public float getCurrentScrollVelocity() {
        return this.mCurrentScrollVelocity;
    }

    public boolean isOnKeyguard() {
        return this.mStatusBarState == 1;
    }

    public void setStatusBarState(int i) {
        this.mStatusBarState = i;
    }

    public void setExpandingVelocity(float f) {
        this.mExpandingVelocity = f;
    }

    public void setExpansionChanging(boolean z) {
        this.mExpansionChanging = z;
    }

    public boolean isExpansionChanging() {
        return this.mExpansionChanging;
    }

    public float getExpandingVelocity() {
        return this.mExpandingVelocity;
    }

    public void setPanelTracking(boolean z) {
        this.mPanelTracking = z;
    }

    public boolean hasPulsingNotifications() {
        HeadsUpManager headsUpManager;
        return this.mPulsing && (headsUpManager = this.mHeadUpManager) != null && headsUpManager.hasNotifications();
    }

    public void setPulsing(boolean z) {
        this.mPulsing = z;
    }

    public boolean isPulsing(NotificationEntry notificationEntry) {
        HeadsUpManager headsUpManager;
        if (!this.mPulsing || (headsUpManager = this.mHeadUpManager) == null) {
            return false;
        }
        return headsUpManager.isAlerting(notificationEntry.getKey());
    }

    public boolean isPanelTracking() {
        return this.mPanelTracking;
    }

    public boolean isPanelFullWidth() {
        return this.mPanelFullWidth;
    }

    public void setPanelFullWidth(boolean z) {
        this.mPanelFullWidth = z;
    }

    public void setUnlockHintRunning(boolean z) {
        this.mUnlockHintRunning = z;
    }

    public boolean isUnlockHintRunning() {
        return this.mUnlockHintRunning;
    }

    public boolean isQsCustomizerShowing() {
        return this.mQsCustomizerShowing;
    }

    public void setQsCustomizerShowing(boolean z) {
        this.mQsCustomizerShowing = z;
    }

    public void setIntrinsicPadding(int i) {
        this.mIntrinsicPadding = i;
    }

    public int getIntrinsicPadding() {
        return this.mIntrinsicPadding;
    }

    public boolean isDozingAndNotPulsing(ExpandableView expandableView) {
        if (expandableView instanceof ExpandableNotificationRow) {
            return isDozingAndNotPulsing((ExpandableNotificationRow) expandableView);
        }
        return false;
    }

    public boolean isDozingAndNotPulsing(ExpandableNotificationRow expandableNotificationRow) {
        return isDozing() && !isPulsing(expandableNotificationRow.getEntry());
    }

    public void setExpandAnimationTopChange(int i) {
        this.mExpandAnimationTopChange = i;
    }

    public void setExpandingNotification(ExpandableNotificationRow expandableNotificationRow) {
        this.mExpandingNotification = expandableNotificationRow;
    }

    public ExpandableNotificationRow getExpandingNotification() {
        return this.mExpandingNotification;
    }

    public int getExpandAnimationTopChange() {
        return this.mExpandAnimationTopChange;
    }

    public boolean isFullyHidden() {
        return this.mHideAmount == 1.0f;
    }

    public boolean isHiddenAtAll() {
        return this.mHideAmount != 0.0f;
    }

    public void setAppearing(boolean z) {
        this.mAppearing = z;
    }

    public boolean isAppearing() {
        return this.mAppearing;
    }

    public void setPulseHeight(float f) {
        if (f != this.mPulseHeight) {
            this.mPulseHeight = f;
            Runnable runnable = this.mOnPulseHeightChangedListener;
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    public float getPulseHeight() {
        float f = this.mPulseHeight;
        if (f == 100000.0f) {
            return 0.0f;
        }
        return f;
    }

    public void setDozeAmount(float f) {
        if (f != this.mDozeAmount) {
            this.mDozeAmount = f;
            if (f == 0.0f || f == 1.0f) {
                setPulseHeight(100000.0f);
            }
        }
    }

    public boolean isFullyAwake() {
        return this.mDozeAmount == 0.0f;
    }

    public void setOnPulseHeightChangedListener(Runnable runnable) {
        this.mOnPulseHeightChangedListener = runnable;
    }

    public void setTrackedHeadsUpRow(ExpandableNotificationRow expandableNotificationRow) {
        this.mTrackedHeadsUpRow = expandableNotificationRow;
    }

    public ExpandableNotificationRow getTrackedHeadsUpRow() {
        ExpandableNotificationRow expandableNotificationRow = this.mTrackedHeadsUpRow;
        if (expandableNotificationRow == null || !expandableNotificationRow.isAboveShelf()) {
            return null;
        }
        return this.mTrackedHeadsUpRow;
    }

    public void setAppearFraction(float f) {
        this.mAppearFraction = f;
    }

    public float getAppearFraction() {
        return this.mAppearFraction;
    }
}

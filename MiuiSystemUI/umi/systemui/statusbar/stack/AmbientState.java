package com.android.systemui.statusbar.stack;

import android.content.Context;
import android.view.View;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ActivatableNotificationView;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.NotificationShelf;
import java.util.ArrayList;

public class AmbientState {
    private ActivatableNotificationView mActivatedChild;
    private int mBaseZHeight;
    private float mCurrentScrollVelocity;
    private boolean mDark;
    private boolean mDimmed;
    private ArrayList<View> mDraggedViews = new ArrayList<>();
    private float mExpandingVelocity;
    private boolean mExpansionChanging;
    private boolean mHasPulsingNotifications;
    private boolean mHideSensitive;
    private ExpandableView mLastVisibleBackgroundChild;
    private int mLayoutHeight;
    private int mLayoutMinHeight;
    private float mMaxHeadsUpTranslation;
    private int mMaxLayoutHeight;
    private float mOverScrollBottomAmount;
    private float mOverScrollTopAmount;
    private boolean mPanelAppear;
    private boolean mPanelFullWidth;
    private boolean mPanelTracking;
    private int mScrollY;
    private boolean mShadeExpanded;
    private NotificationShelf mShelf;
    private int mSpeedBumpIndex = -1;
    private int mSpringLength;
    private float mStackTranslation;
    private int mStatusBarState;
    private int mTopPadding;
    private boolean mUnlockHintRunning;
    private int mZDistanceBetweenElements;

    public void setDismissAllInProgress(boolean z) {
    }

    public AmbientState(Context context) {
        reload(context);
    }

    public void reload(Context context) {
        this.mZDistanceBetweenElements = Math.max(1, context.getResources().getDimensionPixelSize(R.dimen.z_distance_between_notifications));
        this.mBaseZHeight = context.getResources().getDimensionPixelSize(R.dimen.notification_heads_up_z_translation);
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

    public void onBeginDrag(View view) {
        this.mDraggedViews.add(view);
    }

    public void onDragFinished(View view) {
        this.mDraggedViews.remove(view);
    }

    public ArrayList<View> getDraggedViews() {
        return this.mDraggedViews;
    }

    public void setDimmed(boolean z) {
        this.mDimmed = z;
    }

    public void setDark(boolean z) {
        this.mDark = z;
    }

    public void setHideSensitive(boolean z) {
        this.mHideSensitive = z;
    }

    public void setActivatedChild(ActivatableNotificationView activatableNotificationView) {
        this.mActivatedChild = activatableNotificationView;
    }

    public boolean isDimmed() {
        return this.mDimmed;
    }

    public boolean isDark() {
        return this.mDark;
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
        return Math.max(Math.min(this.mLayoutHeight, this.mMaxLayoutHeight) - this.mTopPadding, this.mLayoutMinHeight);
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
        return this.mHasPulsingNotifications;
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

    public boolean isUnlockHintRunning() {
        return this.mUnlockHintRunning;
    }

    public void setSpringLength(int i) {
        this.mSpringLength = i;
    }

    public int getSpringLength() {
        return this.mSpringLength;
    }

    public void setPanelAppear(boolean z) {
        this.mPanelAppear = z;
    }

    public boolean isPanelAppear() {
        return this.mPanelAppear;
    }
}

package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.NotificationCompat;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.util.FloatProperty;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.NotificationHeaderView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RemoteViews;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.widget.CachingIconView;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.classifier.FalsingManager;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.PluginManager;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationGuts;
import com.android.systemui.statusbar.notification.HybridNotificationView;
import com.android.systemui.statusbar.notification.InCallNotificationView;
import com.android.systemui.statusbar.notification.NotificationInflater;
import com.android.systemui.statusbar.notification.OptimizedHeadsUpNotificationView;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.stack.AnimationProperties;
import com.android.systemui.statusbar.stack.ExpandableViewState;
import com.android.systemui.statusbar.stack.NotificationChildrenContainer;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.stack.StackScrollState;
import com.android.systemui.util.AutoCleanFloatTransitionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;

public class ExpandableNotificationRow extends ActivatableNotificationView implements PluginListener<NotificationMenuRowPlugin> {
    private static final Property<ExpandableNotificationRow, Float> TRANSLATE_CONTENT = new FloatProperty<ExpandableNotificationRow>("translate") {
        public void setValue(ExpandableNotificationRow expandableNotificationRow, float f) {
            expandableNotificationRow.setTranslation(f);
        }

        public Float get(ExpandableNotificationRow expandableNotificationRow) {
            return Float.valueOf(expandableNotificationRow.getTranslation());
        }
    };
    private boolean mAboveShelf;
    private String mAppName;
    private View mChildAfterViewWhenDismissed;
    /* access modifiers changed from: private */
    public NotificationChildrenContainer mChildrenContainer;
    private ViewStub mChildrenContainerStub;
    private boolean mChildrenExpanded;
    private float mContentTransformationAmount;
    private boolean mDismissed;
    /* access modifiers changed from: private */
    public NotificationData.Entry mEntry;
    private View.OnClickListener mExpandClickListener = new View.OnClickListener() {
        public void onClick(View view) {
            boolean z;
            if (ExpandableNotificationRow.this.mShowingPublic || ((ExpandableNotificationRow.this.mIsLowPriority && !ExpandableNotificationRow.this.isExpanded()) || !ExpandableNotificationRow.this.mGroupManager.isSummaryOfGroup(ExpandableNotificationRow.this.mStatusBarNotification))) {
                if (view.isAccessibilityFocused()) {
                    ExpandableNotificationRow.this.mPrivateLayout.setFocusOnVisibilityChange();
                }
                if (ExpandableNotificationRow.this.isPinned()) {
                    z = !ExpandableNotificationRow.this.mExpandedWhenPinned;
                    boolean unused = ExpandableNotificationRow.this.mExpandedWhenPinned = z;
                } else {
                    z = !ExpandableNotificationRow.this.isExpanded();
                    ExpandableNotificationRow.this.setUserExpanded(z);
                }
                ExpandableNotificationRow.this.setExpansionChanging(true);
                ExpandableNotificationRow.this.notifyHeightChanged(true);
                ExpandableNotificationRow.this.mOnExpandClickListener.onExpandClicked(ExpandableNotificationRow.this.mEntry, z);
                MetricsLogger.action(ExpandableNotificationRow.this.mContext, 407, z);
                return;
            }
            ExpandableNotificationRow.this.setGroupExpansionChanging(true);
            boolean isGroupExpanded = ExpandableNotificationRow.this.mGroupManager.isGroupExpanded(ExpandableNotificationRow.this.mStatusBarNotification);
            boolean z2 = ExpandableNotificationRow.this.mGroupManager.toggleGroupExpansion(ExpandableNotificationRow.this.mStatusBarNotification);
            ExpandableNotificationRow.this.mOnExpandClickListener.onExpandClicked(ExpandableNotificationRow.this.mEntry, z2);
            MetricsLogger.action(ExpandableNotificationRow.this.mContext, 408, z2);
            ExpandableNotificationRow.this.onExpansionChanged(true, isGroupExpanded);
        }
    };
    private boolean mExpandable;
    /* access modifiers changed from: private */
    public boolean mExpandedWhenPinned;
    private boolean mExpansionChanging;
    private FalsingManager mFalsingManager;
    private boolean mGroupExpansionChanging;
    /* access modifiers changed from: private */
    public NotificationGroupManager mGroupManager;
    private View mGroupParentWhenDismissed;
    /* access modifiers changed from: private */
    public NotificationGuts mGuts;
    /* access modifiers changed from: private */
    public ViewStub mGutsStub;
    private boolean mHasUserChangedExpansion;
    private int mHeadsUpHeight;
    private HeadsUpManager mHeadsUpManager;
    private boolean mHeadsupDisappearRunning;
    private boolean mHiddenForAnimation;
    private boolean mHideSensitiveForIntrinsicHeight;
    private boolean mIconAnimationRunning;
    private int mIconTransformContentShift;
    private int mIconTransformContentShiftNoIcon;
    private boolean mIconsVisible = true;
    private int mIncreasedPaddingBetweenElements;
    private boolean mIsColorized;
    private boolean mIsFirstRow;
    private boolean mIsHeadsUp;
    private boolean mIsLastChild;
    /* access modifiers changed from: private */
    public boolean mIsLowPriority;
    private boolean mIsOptimizedGameHeadsUpBg;
    private boolean mIsPinned;
    private boolean mIsShowHeadsUpBackground;
    private boolean mIsSummaryWithChildren;
    private boolean mIsSystemChildExpanded;
    private boolean mIsSystemExpanded;
    private boolean mJustClicked;
    private boolean mKeepInParent;
    private boolean mLastChronometerRunning = true;
    private LayoutListener mLayoutListener;
    private NotificationContentView[] mLayouts;
    private ExpansionLogger mLogger;
    private String mLoggingKey;
    private boolean mLowPriorityStateUpdated;
    private int mMaxExpandHeight;
    private int mMaxHeadsUpHeight;
    private int mMaxHeadsUpHeightBeforeN;
    private int mMaxHeadsUpHeightBeforeP;
    private int mMaxHeadsUpHeightIncreased;
    /* access modifiers changed from: private */
    public NotificationMenuRowPlugin mMenuRow;
    private int mNotificationAmbientHeight;
    private int mNotificationHeadsUpBgRadius;
    private final NotificationInflater mNotificationInflater;
    private int mNotificationMaxHeight;
    private int mNotificationMinHeight;
    private int mNotificationMinHeightLarge;
    private int mNotificationMinHeightLegacy;
    private int mNotificationMinHeightMedia;
    private ExpandableNotificationRow mNotificationParent;
    private View.OnClickListener mOnClickListener;
    private Runnable mOnDismissRunnable;
    /* access modifiers changed from: private */
    public OnExpandClickListener mOnExpandClickListener;
    private boolean mOnKeyguard;
    /* access modifiers changed from: private */
    public NotificationContentView mPrivateLayout;
    private NotificationContentView mPublicLayout;
    private int mReInflateFlags;
    private boolean mRefocusOnDismiss;
    private boolean mRemoved;
    private boolean mSensitive;
    private boolean mSensitiveHiddenInGeneral;
    private boolean mShowAmbient;
    private boolean mShowNoBackground;
    /* access modifiers changed from: private */
    public boolean mShowingPublic;
    private boolean mShowingPublicInitialized;
    /* access modifiers changed from: private */
    public ExpandedNotification mStatusBarNotification;
    private float mTargetTouchScale = 1.0f;
    /* access modifiers changed from: private */
    public boolean mTouchingScale = false;
    /* access modifiers changed from: private */
    public Animator mTranslateAnim;
    /* access modifiers changed from: private */
    public ArrayList<View> mTranslateableViews;
    private float mTranslationWhenRemoved;
    private boolean mUseIncreasedCollapsedHeight;
    private boolean mUseIncreasedHeadsUpHeight;
    private boolean mUserExpanded;
    private boolean mUserLocked;
    private boolean mWasChildInGroupWhenRemoved;

    public interface ExpansionLogger {
        void logNotificationExpansion(String str, boolean z, boolean z2);
    }

    public interface LayoutListener {
        void onLayout();
    }

    public interface OnExpandClickListener {
        void onExpandClicked(NotificationData.Entry entry, boolean z);
    }

    private boolean isSystemChildExpanded() {
        return false;
    }

    public float getIncreasedPaddingAmount() {
        return 1.0f;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public boolean isGroupExpansionChanging() {
        if (isChildInGroup()) {
            return this.mNotificationParent.isGroupExpansionChanging();
        }
        return this.mGroupExpansionChanging;
    }

    public void setExpansionChanging(boolean z) {
        this.mExpansionChanging = z;
        onExpansionChanged();
    }

    public void setGroupExpansionChanging(boolean z) {
        this.mGroupExpansionChanging = z;
        onExpansionChanged();
    }

    private void onExpansionChanged() {
        boolean z = this.mExpansionChanging || this.mGroupExpansionChanging;
        setTag(R.id.view_reset_scale_tag, Boolean.valueOf(z));
        if (z) {
            cancelTouchScale();
        }
    }

    public void setActualHeightAnimating(boolean z) {
        NotificationContentView notificationContentView = this.mPrivateLayout;
        if (notificationContentView != null) {
            notificationContentView.setContentHeightAnimating(z);
        }
    }

    public NotificationContentView getPrivateLayout() {
        return this.mPrivateLayout;
    }

    public NotificationContentView getPublicLayout() {
        return this.mPublicLayout;
    }

    public void setIconAnimationRunning(boolean z) {
        for (NotificationContentView iconAnimationRunning : this.mLayouts) {
            setIconAnimationRunning(z, iconAnimationRunning);
        }
        if (this.mIsSummaryWithChildren) {
            setIconAnimationRunningForChild(z, this.mChildrenContainer.getHeaderView());
            setIconAnimationRunningForChild(z, this.mChildrenContainer.getLowPriorityHeaderView());
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                notificationChildren.get(i).setIconAnimationRunning(z);
            }
        }
        this.mIconAnimationRunning = z;
    }

    private void setIconAnimationRunning(boolean z, NotificationContentView notificationContentView) {
        if (notificationContentView != null) {
            View contractedChild = notificationContentView.getContractedChild();
            View expandedChild = notificationContentView.getExpandedChild();
            View headsUpChild = notificationContentView.getHeadsUpChild();
            setIconAnimationRunningForChild(z, contractedChild);
            setIconAnimationRunningForChild(z, expandedChild);
            setIconAnimationRunningForChild(z, headsUpChild);
        }
    }

    private void setIconAnimationRunningForChild(boolean z, View view) {
        if (view != null) {
            setIconRunning((ImageView) view.findViewById(16908294), z);
            setIconRunning((ImageView) view.findViewById(16909333), z);
        }
    }

    private void setIconRunning(ImageView imageView, boolean z) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AnimationDrawable) {
                AnimationDrawable animationDrawable = (AnimationDrawable) drawable;
                if (z) {
                    animationDrawable.start();
                } else {
                    animationDrawable.stop();
                }
            } else if (drawable instanceof AnimatedVectorDrawable) {
                AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) drawable;
                if (z) {
                    animatedVectorDrawable.start();
                } else {
                    animatedVectorDrawable.stop();
                }
            }
        }
    }

    public void updateNotification(NotificationData.Entry entry) {
        boolean z = this.mEntry == null;
        this.mEntry = entry;
        this.mStatusBarNotification = entry.notification;
        this.mNotificationInflater.inflateNotificationViews();
        if (z && isMediaNotification()) {
            resetUserExpansion();
        }
    }

    public void onNotificationUpdated(int i) {
        for (NotificationContentView onNotificationUpdated : this.mLayouts) {
            onNotificationUpdated.onNotificationUpdated(this.mEntry);
        }
        this.mReInflateFlags = i;
        this.mIsColorized = NotificationCompat.isColorized(this.mStatusBarNotification.getNotification());
        if (!((i & 8) == 0 && (i & 1) == 0)) {
            this.mShowingPublicInitialized = false;
        }
        NotificationMenuRowPlugin notificationMenuRowPlugin = this.mMenuRow;
        if (notificationMenuRowPlugin != null) {
            notificationMenuRowPlugin.onNotificationUpdated();
        }
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.recreateNotificationHeader(this.mExpandClickListener);
            this.mChildrenContainer.onNotificationUpdated();
        }
        if (this.mIconAnimationRunning) {
            setIconAnimationRunning(true);
        }
        ExpandableNotificationRow expandableNotificationRow = this.mNotificationParent;
        if (expandableNotificationRow != null) {
            expandableNotificationRow.updateChildrenHeaderAppearance();
        }
        onChildrenCountChanged();
        this.mPublicLayout.updateExpandButtons(true);
        updateLimits();
        updateIconVisibilities();
        updateBackground();
    }

    public int getReInflateFlags() {
        return this.mReInflateFlags;
    }

    public boolean isDimmable() {
        if (!getShowingLayout().isDimmable()) {
            return false;
        }
        return super.isDimmable();
    }

    private void updateLimits() {
        for (NotificationContentView updateLimitsForView : this.mLayouts) {
            updateLimitsForView(updateLimitsForView);
        }
    }

    private void updateLimitsForView(NotificationContentView notificationContentView) {
        int i;
        int i2;
        boolean z = true;
        boolean z2 = (notificationContentView.getContractedChild() == null || notificationContentView.getContractedChild().getId() == 16909445) ? false : true;
        boolean z3 = this.mEntry.targetSdk < 24;
        boolean z4 = this.mEntry.targetSdk < 28;
        if (isMediaNotification()) {
            i = this.mNotificationMinHeightMedia;
        } else if (z2 && z3 && !this.mIsSummaryWithChildren) {
            i = this.mNotificationMinHeightLegacy;
        } else if (!this.mUseIncreasedCollapsedHeight || notificationContentView != this.mPrivateLayout) {
            i = this.mNotificationMinHeight;
        } else {
            i = this.mNotificationMinHeightLarge;
        }
        if (notificationContentView.getHeadsUpChild() == null || notificationContentView.getHeadsUpChild().getId() == 16909445) {
            z = false;
        }
        if (!z || !z4) {
            i2 = (!this.mUseIncreasedHeadsUpHeight || notificationContentView != this.mPrivateLayout) ? this.mMaxHeadsUpHeight : this.mMaxHeadsUpHeightIncreased;
        } else {
            i2 = z3 ? this.mMaxHeadsUpHeightBeforeN : this.mMaxHeadsUpHeightBeforeP;
        }
        notificationContentView.setHeights(i, i2, this.mNotificationMaxHeight, this.mNotificationAmbientHeight);
    }

    public ExpandedNotification getStatusBarNotification() {
        return this.mStatusBarNotification;
    }

    public NotificationData.Entry getEntry() {
        return this.mEntry;
    }

    public boolean isHeadsUp() {
        return this.mIsHeadsUp;
    }

    public void setHeadsUp(boolean z) {
        int intrinsicHeight = getIntrinsicHeight();
        this.mIsHeadsUp = z;
        this.mPrivateLayout.setHeadsUp(z);
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.updateGroupOverflow();
        }
        if (intrinsicHeight != getIntrinsicHeight()) {
            notifyHeightChanged(false);
        }
        if (z) {
            setAboveShelf(true);
            resetTranslation();
        }
        updateOutline();
    }

    public void setGroupManager(NotificationGroupManager notificationGroupManager) {
        this.mGroupManager = notificationGroupManager;
        this.mPrivateLayout.setGroupManager(notificationGroupManager);
    }

    public void setRemoteInputController(RemoteInputController remoteInputController) {
        this.mPrivateLayout.setRemoteInputController(remoteInputController);
    }

    public void setAppName(String str) {
        this.mAppName = str;
        NotificationMenuRowPlugin notificationMenuRowPlugin = this.mMenuRow;
        if (notificationMenuRowPlugin != null && notificationMenuRowPlugin.getMenuView() != null) {
            this.mMenuRow.setAppName(this.mAppName);
        }
    }

    public String getAppName() {
        return this.mAppName;
    }

    public void addChildNotification(ExpandableNotificationRow expandableNotificationRow, int i) {
        if (this.mChildrenContainer == null) {
            this.mChildrenContainerStub.inflate();
        }
        this.mChildrenContainer.addNotification(expandableNotificationRow, i);
        onChildrenCountChanged();
        expandableNotificationRow.setIsChildInGroup(true, this);
        this.mChildrenContainer.rebuildCollapseButton();
    }

    public void removeChildNotification(ExpandableNotificationRow expandableNotificationRow) {
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer != null) {
            notificationChildrenContainer.removeNotification(expandableNotificationRow);
        }
        onChildrenCountChanged();
        expandableNotificationRow.setIsChildInGroup(false, (ExpandableNotificationRow) null);
    }

    public boolean isChildInGroup() {
        return this.mNotificationParent != null;
    }

    public ExpandableNotificationRow getNotificationParent() {
        return this.mNotificationParent;
    }

    public void setIsChildInGroup(boolean z, ExpandableNotificationRow expandableNotificationRow) {
        boolean z2 = StatusBar.ENABLE_CHILD_NOTIFICATIONS && z;
        if (!z2) {
            expandableNotificationRow = null;
        }
        this.mNotificationParent = expandableNotificationRow;
        this.mPrivateLayout.setIsChildInGroup(z2);
        this.mNotificationInflater.setIsChildInGroup(z2);
        resetBackgroundAlpha();
        updateBackgroundForGroupState();
        updateClickAndFocus();
        if (this.mNotificationParent != null) {
            setOverrideTintColor(0, 0.0f);
            this.mNotificationParent.updateBackgroundForGroupState();
            setScaleX(1.0f);
            setScaleY(1.0f);
        }
        updateIconVisibilities();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean z;
        boolean z2 = true;
        boolean z3 = motionEvent.getActionMasked() == 0;
        boolean z4 = motionEvent.getActionMasked() == 1 || motionEvent.getActionMasked() == 3;
        if (this.mActivated || (isChildInGroup() && !isGroupExpanded())) {
            z2 = false;
        }
        if (z3 && z2) {
            animateTouchScale(0.95f);
        }
        if (motionEvent.getActionMasked() != 0 || !isChildInGroup() || isGroupExpanded()) {
            z = super.onTouchEvent(motionEvent);
        } else {
            z = false;
        }
        if (z4 && !this.mActivated && z2) {
            postDelayed(new Runnable() {
                public final void run() {
                    ExpandableNotificationRow.this.lambda$onTouchEvent$0$ExpandableNotificationRow();
                }
            }, isPinned() ? 120 : 0);
        }
        if (this.mActivated) {
            this.mTouchingScale = false;
        }
        return z;
    }

    public /* synthetic */ void lambda$onTouchEvent$0$ExpandableNotificationRow() {
        animateTouchScale(1.0f);
    }

    /* access modifiers changed from: protected */
    public void startActivateAnimation(boolean z) {
        animateTouchScale(z ? 1.0f : 1.05f);
    }

    public void makeInactive(boolean z) {
        super.makeInactive(z);
        if (!z) {
            this.mTouchingScale = false;
        }
    }

    private void cancelTouchScale() {
        Folme.useValue(getEntry().key).cancel();
        setScaleX(this.mTargetTouchScale);
        setScaleY(this.mTargetTouchScale);
    }

    private void animateTouchScale(final float f) {
        boolean z = false;
        if ((this.mExpansionChanging || this.mGroupExpansionChanging) || f == getScaleX()) {
            if (f != 1.0f) {
                z = true;
            }
            this.mTouchingScale = z;
            return;
        }
        this.mTargetTouchScale = f;
        String str = getEntry().key;
        Folme.useValue(str).cancel();
        Folme.getValueTarget(str).setMinVisibleChange(0.01f, "scale");
        IStateStyle useValue = Folme.useValue(str);
        useValue.setTo("scale", Float.valueOf(getScaleX()));
        useValue.addListener(new AutoCleanFloatTransitionListener(str) {
            public void onStart() {
                if (f != 1.0f) {
                    boolean unused = ExpandableNotificationRow.this.mTouchingScale = true;
                }
            }

            public void onUpdate(Map<String, Float> map) {
                float floatValue = map.get("scale").floatValue();
                ExpandableViewState viewState = ExpandableNotificationRow.this.getViewState();
                viewState.scaleX = floatValue;
                viewState.scaleY = floatValue;
                ExpandableNotificationRow.this.setScaleX(floatValue);
                ExpandableNotificationRow.this.setScaleY(floatValue);
            }

            public void onEnd() {
                if (f == 1.0f) {
                    boolean unused = ExpandableNotificationRow.this.mTouchingScale = false;
                }
                ExpandableNotificationRow.this.updateBackground();
            }
        });
        useValue.to("scale", Float.valueOf(f));
    }

    public boolean isTouchingScale() {
        return this.mTouchingScale;
    }

    /* access modifiers changed from: protected */
    public boolean handleSlideBack() {
        NotificationMenuRowPlugin notificationMenuRowPlugin = this.mMenuRow;
        if (notificationMenuRowPlugin == null || !notificationMenuRowPlugin.isMenuVisible()) {
            return false;
        }
        animateTranslateNotification(0.0f);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean shouldHideBackground() {
        return super.shouldHideBackground() || this.mShowNoBackground;
    }

    public boolean isSummaryWithChildren() {
        return this.mIsSummaryWithChildren;
    }

    public boolean areChildrenExpanded() {
        return this.mChildrenExpanded;
    }

    public List<ExpandableNotificationRow> getNotificationChildren() {
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer == null) {
            return null;
        }
        return notificationChildrenContainer.getNotificationChildren();
    }

    public int getNumberOfNotificationChildren() {
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer == null) {
            return 0;
        }
        return notificationChildrenContainer.getNotificationChildren().size();
    }

    public boolean applyChildOrder(List<ExpandableNotificationRow> list, VisualStabilityManager visualStabilityManager, VisualStabilityManager.Callback callback) {
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        return notificationChildrenContainer != null && notificationChildrenContainer.applyChildOrder(list, visualStabilityManager, callback);
    }

    public void getChildrenStates(StackScrollState stackScrollState) {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.getState(stackScrollState, stackScrollState.getViewStateForView(this));
        }
    }

    public void applyChildrenState() {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.applyState();
        }
    }

    public void prepareExpansionChanged(StackScrollState stackScrollState) {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.prepareExpansionChanged(stackScrollState);
        }
    }

    public void startChildAnimation(AnimationProperties animationProperties) {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.startAnimationToState(animationProperties);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:4:0x0009, code lost:
        r2 = r1.mChildrenContainer.getViewAtPosition(r2);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.statusbar.ExpandableNotificationRow getViewAtPosition(float r2) {
        /*
            r1 = this;
            boolean r0 = r1.mIsSummaryWithChildren
            if (r0 == 0) goto L_0x0013
            boolean r0 = r1.mChildrenExpanded
            if (r0 != 0) goto L_0x0009
            goto L_0x0013
        L_0x0009:
            com.android.systemui.statusbar.stack.NotificationChildrenContainer r0 = r1.mChildrenContainer
            com.android.systemui.statusbar.ExpandableNotificationRow r2 = r0.getViewAtPosition(r2)
            if (r2 != 0) goto L_0x0012
            goto L_0x0013
        L_0x0012:
            r1 = r2
        L_0x0013:
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.ExpandableNotificationRow.getViewAtPosition(float):com.android.systemui.statusbar.ExpandableNotificationRow");
    }

    public NotificationGuts getGuts() {
        return this.mGuts;
    }

    public void setPinned(boolean z) {
        int intrinsicHeight = getIntrinsicHeight();
        this.mIsPinned = z;
        if (intrinsicHeight != getIntrinsicHeight()) {
            notifyHeightChanged(false);
        }
        if (z) {
            setIconAnimationRunning(true);
            this.mExpandedWhenPinned = false;
        } else if (this.mExpandedWhenPinned) {
            setUserExpanded(true);
        }
        setChronometerRunning(this.mLastChronometerRunning);
    }

    public boolean isPinned() {
        return this.mIsPinned;
    }

    public int getPinnedHeadsUpHeight() {
        return getPinnedHeadsUpHeight(true);
    }

    private int getPinnedHeadsUpHeight(boolean z) {
        if (this.mIsSummaryWithChildren) {
            return this.mChildrenContainer.getIntrinsicHeight();
        }
        if (this.mExpandedWhenPinned) {
            return Math.max(getMaxExpandHeight(), this.mHeadsUpHeight);
        }
        if (z) {
            return Math.max(getCollapsedHeight(), this.mHeadsUpHeight);
        }
        return this.mHeadsUpHeight;
    }

    public void setJustClicked(boolean z) {
        this.mJustClicked = z;
    }

    public boolean wasJustClicked() {
        return this.mJustClicked;
    }

    public void setChronometerRunning(boolean z) {
        this.mLastChronometerRunning = z;
        setChronometerRunning(z, this.mPrivateLayout);
        setChronometerRunning(z, this.mPublicLayout);
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer != null) {
            List<ExpandableNotificationRow> notificationChildren = notificationChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                notificationChildren.get(i).setChronometerRunning(z);
            }
        }
    }

    private void setChronometerRunning(boolean z, NotificationContentView notificationContentView) {
        if (notificationContentView != null) {
            boolean z2 = z || isPinned();
            View contractedChild = notificationContentView.getContractedChild();
            View expandedChild = notificationContentView.getExpandedChild();
            View headsUpChild = notificationContentView.getHeadsUpChild();
            setChronometerRunningForChild(z2, contractedChild);
            setChronometerRunningForChild(z2, expandedChild);
            setChronometerRunningForChild(z2, headsUpChild);
        }
    }

    private void setChronometerRunningForChild(boolean z, View view) {
        if (view != null) {
            View findViewById = view.findViewById(16908819);
            if (findViewById instanceof Chronometer) {
                ((Chronometer) findViewById).setStarted(z);
            }
        }
    }

    public NotificationHeaderView getNotificationHeader() {
        if (this.mIsSummaryWithChildren) {
            return this.mChildrenContainer.getHeaderView();
        }
        return this.mPrivateLayout.getNotificationHeader();
    }

    public NotificationHeaderView getVisibleNotificationHeader() {
        if (!this.mIsSummaryWithChildren || this.mShowingPublic) {
            return getShowingLayout().getVisibleNotificationHeader();
        }
        return this.mChildrenContainer.getVisibleHeader();
    }

    public void setOnExpandClickListener(OnExpandClickListener onExpandClickListener) {
        this.mOnExpandClickListener = onExpandClickListener;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        super.setOnClickListener(onClickListener);
        this.mOnClickListener = onClickListener;
        updateClickAndFocus();
    }

    private void updateClickAndFocus() {
        boolean z = false;
        boolean z2 = !isChildInGroup() || isGroupExpanded();
        if (this.mOnClickListener != null && z2) {
            z = true;
        }
        if (isFocusable() != z2) {
            setFocusable(z2);
        }
        if (isClickable() != z) {
            setClickable(z);
        }
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public void setGutsView(NotificationMenuRowPlugin.MenuItem menuItem) {
        if (this.mGuts != null && (menuItem.getGutsView() instanceof NotificationGuts.GutsContent)) {
            ((NotificationGuts.GutsContent) menuItem.getGutsView()).setGutsParent(this.mGuts);
            this.mGuts.setGutsContent((NotificationGuts.GutsContent) menuItem.getGutsView());
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((PluginManager) Dependency.get(PluginManager.class)).addPluginListener(this, (Class<?>) NotificationMenuRowPlugin.class, false);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((PluginManager) Dependency.get(PluginManager.class)).removePluginListener(this);
    }

    public void onPluginConnected(NotificationMenuRowPlugin notificationMenuRowPlugin, Context context) {
        boolean z = this.mMenuRow.getMenuView() != null;
        if (z) {
            removeView(this.mMenuRow.getMenuView());
        }
        this.mMenuRow = notificationMenuRowPlugin;
        if (this.mMenuRow.useDefaultMenuItems()) {
            ArrayList arrayList = new ArrayList();
            arrayList.add(NotificationMenuRow.createInfoItem(this.mContext));
            if (Constants.IS_INTERNATIONAL) {
                arrayList.add(NotificationMenuRow.createSnoozeItem(this.mContext));
            } else if (NotificationAggregate.canAggregate(this.mContext, this.mStatusBarNotification)) {
                arrayList.add(NotificationMenuRow.createAggregateItem(this.mContext));
            }
            this.mMenuRow.setMenuItems(arrayList);
        }
        if (z) {
            createMenu();
        }
    }

    public void onPluginDisconnected(NotificationMenuRowPlugin notificationMenuRowPlugin) {
        boolean z = this.mMenuRow.getMenuView() != null;
        this.mMenuRow = new NotificationMenuRow(this.mContext);
        if (z) {
            createMenu();
        }
    }

    public NotificationMenuRowPlugin createMenu() {
        if (this.mMenuRow.getMenuView() == null) {
            this.mMenuRow.createMenu(this);
            this.mMenuRow.setAppName(this.mAppName);
            addView(this.mMenuRow.getMenuView(), 0, new FrameLayout.LayoutParams(-1, -1));
        }
        return this.mMenuRow;
    }

    public NotificationMenuRowPlugin getMenu() {
        return this.mMenuRow;
    }

    public void onDensityOrFontScaleChanged() {
        initDimens();
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer != null) {
            notificationChildrenContainer.reInflateViews(this.mExpandClickListener, this.mEntry.notification);
        }
        NotificationGuts notificationGuts = this.mGuts;
        if (notificationGuts != null) {
            int indexOfChild = indexOfChild(notificationGuts);
            notificationGuts.removeAllViews();
            removeView(notificationGuts);
            this.mGuts = (NotificationGuts) LayoutInflater.from(this.mContext).inflate(R.layout.notification_guts, this, false);
            this.mGuts.setVisibility(notificationGuts.getVisibility());
            addView(this.mGuts, indexOfChild);
        }
        View menuView = this.mMenuRow.getMenuView();
        if (menuView != null) {
            int indexOfChild2 = indexOfChild(menuView);
            removeView(menuView);
            this.mMenuRow.createMenu(this);
            this.mMenuRow.setAppName(this.mAppName);
            addView(this.mMenuRow.getMenuView(), indexOfChild2);
        }
        for (NotificationContentView notificationContentView : this.mLayouts) {
            notificationContentView.onDensityOrFontScaleChanged();
            notificationContentView.reInflateViews();
        }
        this.mNotificationInflater.onDensityOrFontScaleChanged();
        onNotificationUpdated(-1);
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (this.mMenuRow.getMenuView() != null) {
            this.mMenuRow.onConfigurationChanged();
        }
    }

    public void setContentBackground(int i, boolean z, NotificationContentView notificationContentView) {
        if (getShowingLayout() == notificationContentView) {
            setTintColor(i, z);
        }
    }

    public void closeRemoteInput() {
        for (NotificationContentView closeRemoteInput : this.mLayouts) {
            closeRemoteInput.closeRemoteInput();
        }
    }

    public void setSingleLineWidthIndention(int i) {
        this.mPrivateLayout.setSingleLineWidthIndention(i);
    }

    public HybridNotificationView getSingleLineView() {
        return this.mPrivateLayout.getSingleLineView();
    }

    public HybridNotificationView getAmbientSingleLineView() {
        return getShowingLayout().getAmbientSingleLineChild();
    }

    public boolean isOnKeyguard() {
        return this.mOnKeyguard;
    }

    public void setDismissed() {
        this.mDismissed = true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0011, code lost:
        r2 = r1.mNotificationParent.getNotificationChildren();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void setDismissed(boolean r2, boolean r3) {
        /*
            r1 = this;
            r1.mDismissed = r2
            com.android.systemui.statusbar.ExpandableNotificationRow r2 = r1.mNotificationParent
            r1.mGroupParentWhenDismissed = r2
            r1.mRefocusOnDismiss = r3
            r2 = 0
            r1.mChildAfterViewWhenDismissed = r2
            boolean r2 = r1.isChildInGroup()
            if (r2 == 0) goto L_0x0030
            com.android.systemui.statusbar.ExpandableNotificationRow r2 = r1.mNotificationParent
            java.util.List r2 = r2.getNotificationChildren()
            int r3 = r2.indexOf(r1)
            r0 = -1
            if (r3 == r0) goto L_0x0030
            int r0 = r2.size()
            int r0 = r0 + -1
            if (r3 >= r0) goto L_0x0030
            int r3 = r3 + 1
            java.lang.Object r2 = r2.get(r3)
            android.view.View r2 = (android.view.View) r2
            r1.mChildAfterViewWhenDismissed = r2
        L_0x0030:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.ExpandableNotificationRow.setDismissed(boolean, boolean):void");
    }

    public boolean isDismissed() {
        return this.mDismissed;
    }

    public boolean keepInParent() {
        return this.mKeepInParent;
    }

    public void setKeepInParent(boolean z) {
        this.mKeepInParent = z;
    }

    public boolean isRemoved() {
        return this.mRemoved;
    }

    public void setRemoved() {
        this.mRemoved = true;
        this.mTranslationWhenRemoved = getTranslationY();
        this.mWasChildInGroupWhenRemoved = isChildInGroup();
        if (isChildInGroup()) {
            this.mTranslationWhenRemoved += getNotificationParent().getTranslationY();
        }
        this.mPrivateLayout.setRemoved();
    }

    public boolean wasChildInGroupWhenRemoved() {
        return this.mWasChildInGroupWhenRemoved;
    }

    public float getTranslationWhenRemoved() {
        return this.mTranslationWhenRemoved;
    }

    public NotificationChildrenContainer getChildrenContainer() {
        return this.mChildrenContainer;
    }

    public void setHeadsUpAnimatingAway(boolean z) {
        this.mHeadsupDisappearRunning = z;
        this.mPrivateLayout.setHeadsUpAnimatingAway(z);
        updateOutline();
    }

    public boolean isHeadsUpAnimatingAway() {
        return this.mHeadsupDisappearRunning;
    }

    public View getChildAfterViewWhenDismissed() {
        return this.mChildAfterViewWhenDismissed;
    }

    public View getGroupParentWhenDismissed() {
        return this.mGroupParentWhenDismissed;
    }

    public void performDismiss() {
        Runnable runnable = this.mOnDismissRunnable;
        if (runnable != null) {
            runnable.run();
        }
    }

    public void setOnDismissRunnable(Runnable runnable) {
        this.mOnDismissRunnable = runnable;
    }

    public View getNotificationIcon() {
        NotificationHeaderView visibleNotificationHeader = getVisibleNotificationHeader();
        if (visibleNotificationHeader != null) {
            return visibleNotificationHeader.findViewById(16908294);
        }
        return null;
    }

    public boolean isShowingIcon() {
        if (!areGutsExposed() && getVisibleNotificationHeader() != null) {
            return true;
        }
        return false;
    }

    public void setContentTransformationAmount(float f, boolean z) {
        boolean z2 = true;
        boolean z3 = z != this.mIsLastChild;
        if (this.mContentTransformationAmount == f) {
            z2 = false;
        }
        boolean z4 = z3 | z2;
        this.mIsLastChild = z;
        this.mContentTransformationAmount = f;
        if (z4) {
            updateContentTransformation();
        }
    }

    public void setIconsVisible(boolean z) {
        if (z != this.mIconsVisible) {
            this.mIconsVisible = z;
            updateIconVisibilities();
        }
    }

    /* access modifiers changed from: protected */
    public void onBelowSpeedBumpChanged() {
        updateIconVisibilities();
    }

    private void updateContentTransformation() {
        float f = this.mContentTransformationAmount;
        float f2 = (-f) * ((float) this.mIconTransformContentShift);
        float f3 = 1.0f;
        if (this.mIsLastChild) {
            f3 = Interpolators.ALPHA_OUT.getInterpolation(Math.min((1.0f - f) / 0.5f, 1.0f));
            f2 *= 0.4f;
        }
        for (NotificationContentView notificationContentView : this.mLayouts) {
            notificationContentView.setAlpha(f3);
            notificationContentView.setTranslationY(f2);
        }
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer != null) {
            notificationChildrenContainer.setAlpha(f3);
            this.mChildrenContainer.setTranslationY(f2);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x001d A[LOOP:0: B:9:0x001b->B:10:0x001d, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0029  */
    /* JADX WARNING: Removed duplicated region for block: B:16:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateIconVisibilities() {
        /*
            r5 = this;
            boolean r0 = r5.isChildInGroup()
            r1 = 0
            if (r0 != 0) goto L_0x0017
            r5.isBelowSpeedBump()
            boolean r0 = r5.mIconsVisible
            if (r0 != 0) goto L_0x0017
            boolean r0 = com.android.systemui.miui.statusbar.notification.NotificationUtil.showMiuiStyle()
            if (r0 == 0) goto L_0x0015
            goto L_0x0017
        L_0x0015:
            r0 = r1
            goto L_0x0018
        L_0x0017:
            r0 = 1
        L_0x0018:
            com.android.systemui.statusbar.NotificationContentView[] r2 = r5.mLayouts
            int r3 = r2.length
        L_0x001b:
            if (r1 >= r3) goto L_0x0025
            r4 = r2[r1]
            r4.setIconsVisible(r0)
            int r1 = r1 + 1
            goto L_0x001b
        L_0x0025:
            com.android.systemui.statusbar.stack.NotificationChildrenContainer r5 = r5.mChildrenContainer
            if (r5 == 0) goto L_0x002c
            r5.setIconsVisible(r0)
        L_0x002c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.ExpandableNotificationRow.updateIconVisibilities():void");
    }

    public int getRelativeTopPadding(View view) {
        int i = 0;
        while (view.getParent() instanceof ViewGroup) {
            i += view.getTop();
            view = (View) view.getParent();
            if (view instanceof ExpandableNotificationRow) {
                break;
            }
        }
        return i;
    }

    public float getContentTranslation() {
        return this.mPrivateLayout.getTranslationY();
    }

    public void setIsLowPriority(boolean z) {
        this.mIsLowPriority = z;
        this.mPrivateLayout.setIsLowPriority(z);
        this.mNotificationInflater.setIsLowPriority(this.mIsLowPriority);
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer != null) {
            notificationChildrenContainer.setIsLowPriority(z);
        }
    }

    public void setLowPriorityStateUpdated(boolean z) {
        this.mLowPriorityStateUpdated = z;
    }

    public boolean hasLowPriorityStateUpdated() {
        return this.mLowPriorityStateUpdated;
    }

    public boolean isLowPriority() {
        return this.mIsLowPriority;
    }

    public void setUseIncreasedCollapsedHeight(boolean z) {
        this.mUseIncreasedCollapsedHeight = z;
        this.mNotificationInflater.setUsesIncreasedHeight(z);
    }

    public void setUseIncreasedHeadsUpHeight(boolean z) {
        this.mUseIncreasedHeadsUpHeight = z;
        this.mNotificationInflater.setUsesIncreasedHeadsUpHeight(z);
    }

    public void setRemoteViewClickHandler(RemoteViews.OnClickHandler onClickHandler) {
        this.mNotificationInflater.setRemoteViewClickHandler(onClickHandler);
    }

    public void setInflationCallback(NotificationInflater.InflationCallback inflationCallback) {
        this.mNotificationInflater.setInflationCallback(inflationCallback);
    }

    public void setInCallCallback(InCallNotificationView.InCallCallback inCallCallback) {
        this.mNotificationInflater.setInCallCallback(inCallCallback);
    }

    public void setNeedsRedaction(boolean z) {
        this.mNotificationInflater.setRedactAmbient(z);
    }

    @VisibleForTesting
    public NotificationInflater getNotificationInflater() {
        return this.mNotificationInflater;
    }

    public ExpandableNotificationRow(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mFalsingManager = FalsingManager.getInstance(context);
        this.mNotificationInflater = new NotificationInflater(this);
        this.mMenuRow = new NotificationMenuRow(this.mContext);
        initDimens();
    }

    private void initDimens() {
        this.mNotificationMinHeightMedia = getFontScaledHeight(R.dimen.notification_min_height_media);
        this.mNotificationMinHeightLegacy = getFontScaledHeight(R.dimen.notification_min_height_legacy);
        this.mNotificationMinHeight = getFontScaledHeight(R.dimen.notification_min_height);
        this.mNotificationMinHeightLarge = getFontScaledHeight(R.dimen.notification_min_height_increased);
        this.mNotificationMaxHeight = getFontScaledHeight(R.dimen.notification_max_height);
        this.mNotificationAmbientHeight = getFontScaledHeight(R.dimen.notification_ambient_height);
        this.mMaxHeadsUpHeightBeforeN = getFontScaledHeight(R.dimen.notification_max_heads_up_height_legacy);
        this.mMaxHeadsUpHeightBeforeP = getFontScaledHeight(R.dimen.notification_max_heads_up_height_before_p);
        this.mMaxHeadsUpHeight = getFontScaledHeight(R.dimen.notification_max_heads_up_height);
        this.mMaxHeadsUpHeightIncreased = getFontScaledHeight(R.dimen.notification_max_heads_up_height_increased);
        this.mIncreasedPaddingBetweenElements = getResources().getDimensionPixelSize(R.dimen.notification_divider_height_increased);
        this.mIconTransformContentShiftNoIcon = getResources().getDimensionPixelSize(R.dimen.notification_icon_transform_content_shift);
        this.mNotificationHeadsUpBgRadius = getResources().getDimensionPixelSize(R.dimen.notification_heads_up_bg_radius);
    }

    private int getFontScaledHeight(int i) {
        return (int) (((float) getResources().getDimensionPixelSize(i)) * Math.max(1.0f, getResources().getDisplayMetrics().scaledDensity / getResources().getDisplayMetrics().density));
    }

    public void reset() {
        this.mShowingPublicInitialized = false;
        onHeightReset();
        requestLayout();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mPublicLayout = (NotificationContentView) findViewById(R.id.expandedPublic);
        this.mPrivateLayout = (NotificationContentView) findViewById(R.id.expanded);
        this.mLayouts = new NotificationContentView[]{this.mPrivateLayout, this.mPublicLayout};
        for (NotificationContentView notificationContentView : this.mLayouts) {
            notificationContentView.setExpandClickListener(this.mExpandClickListener);
            notificationContentView.setContainingNotification(this);
        }
        this.mGutsStub = (ViewStub) findViewById(R.id.notification_guts_stub);
        this.mGutsStub.setOnInflateListener(new ViewStub.OnInflateListener() {
            public void onInflate(ViewStub viewStub, View view) {
                NotificationGuts unused = ExpandableNotificationRow.this.mGuts = (NotificationGuts) view;
                ExpandableNotificationRow.this.mGuts.setClipTopAmount(ExpandableNotificationRow.this.getClipTopAmount());
                ExpandableNotificationRow.this.mGuts.setActualHeight(ExpandableNotificationRow.this.getActualHeight());
                ViewStub unused2 = ExpandableNotificationRow.this.mGutsStub = null;
            }
        });
        this.mChildrenContainerStub = (ViewStub) findViewById(R.id.child_container_stub);
        this.mChildrenContainerStub.setOnInflateListener(new ViewStub.OnInflateListener() {
            public void onInflate(ViewStub viewStub, View view) {
                NotificationChildrenContainer unused = ExpandableNotificationRow.this.mChildrenContainer = (NotificationChildrenContainer) view;
                ExpandableNotificationRow.this.mChildrenContainer.setIsLowPriority(ExpandableNotificationRow.this.mIsLowPriority);
                ExpandableNotificationRow.this.mChildrenContainer.setContainingNotification(ExpandableNotificationRow.this);
                ExpandableNotificationRow.this.mChildrenContainer.onNotificationUpdated();
                ExpandableNotificationRow.this.mTranslateableViews.add(ExpandableNotificationRow.this.mChildrenContainer);
            }
        });
        this.mTranslateableViews = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            this.mTranslateableViews.add(getChildAt(i));
        }
        this.mTranslateableViews.remove(this.mChildrenContainerStub);
        this.mTranslateableViews.remove(this.mGutsStub);
    }

    public void resetTranslation() {
        Animator animator = this.mTranslateAnim;
        if (animator != null) {
            animator.cancel();
        }
        if (this.mTranslateableViews != null) {
            for (int i = 0; i < this.mTranslateableViews.size(); i++) {
                this.mTranslateableViews.get(i).setTranslationX(0.0f);
            }
        }
        invalidateOutline();
        this.mMenuRow.resetMenu();
    }

    public void animateTranslateNotification(float f) {
        Animator animator = this.mTranslateAnim;
        if (animator != null) {
            animator.cancel();
        }
        this.mTranslateAnim = getTranslateViewAnimator(f, (ValueAnimator.AnimatorUpdateListener) null);
        Animator animator2 = this.mTranslateAnim;
        if (animator2 != null) {
            animator2.start();
        }
    }

    public void setTranslation(float f) {
        for (int i = 0; i < this.mTranslateableViews.size(); i++) {
            if (this.mTranslateableViews.get(i) != null) {
                this.mTranslateableViews.get(i).setTranslationX(f);
            }
        }
        invalidateOutline();
        if (!areGutsAnimating() && this.mMenuRow.getMenuView() != null) {
            this.mMenuRow.onTranslationUpdate(f);
        }
        if (getParent() != null) {
            ((View) getParent()).invalidate();
        }
        if (isChildInGroup() && this.mBackgroundNormal != null) {
            updateBackground();
            this.mNotificationParent.getBackgroundNormal().invalidate();
        }
        updateClipping();
    }

    public float getTranslation() {
        ArrayList<View> arrayList = this.mTranslateableViews;
        if (arrayList == null || arrayList.size() <= 0) {
            return 0.0f;
        }
        return this.mTranslateableViews.get(0).getTranslationX();
    }

    /* access modifiers changed from: protected */
    public int getExtraClipRightAmount() {
        int translation = (int) getTranslation();
        if (translation > 0) {
            return 0;
        }
        return -translation;
    }

    public Animator getTranslateViewAnimator(final float f, ValueAnimator.AnimatorUpdateListener animatorUpdateListener) {
        Animator animator = this.mTranslateAnim;
        if (animator != null) {
            animator.cancel();
        }
        if (areGutsExposed()) {
            return null;
        }
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this, TRANSLATE_CONTENT, new float[]{f});
        if (animatorUpdateListener != null) {
            ofFloat.addUpdateListener(animatorUpdateListener);
        }
        ofFloat.addListener(new AnimatorListenerAdapter() {
            boolean cancelled = false;

            public void onAnimationCancel(Animator animator) {
                this.cancelled = true;
            }

            public void onAnimationEnd(Animator animator) {
                if (!this.cancelled && f == 0.0f) {
                    ExpandableNotificationRow.this.mMenuRow.resetMenu();
                    Animator unused = ExpandableNotificationRow.this.mTranslateAnim = null;
                }
            }
        });
        this.mTranslateAnim = ofFloat;
        return ofFloat;
    }

    public void inflateGuts() {
        if (this.mGuts == null) {
            this.mGutsStub.inflate();
        }
    }

    private void updateChildrenVisibility() {
        int i = 0;
        this.mPrivateLayout.setVisibility((this.mShowingPublic || this.mIsSummaryWithChildren) ? 4 : 0);
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer != null) {
            if (this.mShowingPublic || !this.mIsSummaryWithChildren) {
                i = 4;
            }
            notificationChildrenContainer.setVisibility(i);
        }
        updateLimits();
    }

    public boolean onRequestSendAccessibilityEventInternal(View view, AccessibilityEvent accessibilityEvent) {
        if (!super.onRequestSendAccessibilityEventInternal(view, accessibilityEvent)) {
            return false;
        }
        AccessibilityEvent obtain = AccessibilityEvent.obtain();
        onInitializeAccessibilityEvent(obtain);
        dispatchPopulateAccessibilityEvent(obtain);
        accessibilityEvent.appendRecord(obtain);
        return true;
    }

    public void setDark(boolean z, boolean z2, long j) {
        super.setDark(z, z2, j);
        if (!this.mIsHeadsUp) {
            z2 = false;
        }
        NotificationContentView showingLayout = getShowingLayout();
        if (showingLayout != null) {
            showingLayout.setDark(z, z2, j);
        }
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.setDark(z, z2, j);
        }
    }

    public boolean isExpandable() {
        if (!this.mIsSummaryWithChildren || this.mShowingPublic) {
            return this.mExpandable;
        }
        return !this.mChildrenExpanded;
    }

    public void setExpandable(boolean z) {
        this.mExpandable = z;
        this.mPrivateLayout.updateExpandButtons(isExpandable());
    }

    public void setClipToActualHeight(boolean z) {
        boolean z2 = false;
        super.setClipToActualHeight(z || isUserLocked());
        NotificationContentView showingLayout = getShowingLayout();
        if (z || isUserLocked()) {
            z2 = true;
        }
        showingLayout.setClipToActualHeight(z2);
    }

    public boolean hasUserChangedExpansion() {
        return this.mHasUserChangedExpansion;
    }

    public boolean isUserExpanded() {
        return this.mUserExpanded;
    }

    public void setUserExpanded(boolean z) {
        setUserExpanded(z, false);
    }

    public void setUserExpanded(boolean z, boolean z2) {
        this.mFalsingManager.setNotificationExpanded();
        if (this.mIsSummaryWithChildren && !this.mShowingPublic && z2 && !this.mChildrenContainer.showingAsLowPriority()) {
            boolean isGroupExpanded = this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
            this.mGroupManager.setGroupExpanded((StatusBarNotification) this.mStatusBarNotification, z);
            onExpansionChanged(true, isGroupExpanded);
        } else if (!z || this.mExpandable) {
            boolean isExpanded = isExpanded();
            this.mHasUserChangedExpansion = true;
            this.mUserExpanded = z;
            onExpansionChanged(true, isExpanded);
        }
    }

    public void resetUserExpansion() {
        boolean z = this.mUserExpanded;
        if (!isMediaNotification()) {
            this.mUserExpanded = false;
            this.mHasUserChangedExpansion = false;
        } else if (!this.mHasUserChangedExpansion) {
            this.mUserExpanded = true;
        }
        if (z && this.mIsSummaryWithChildren) {
            this.mChildrenContainer.onExpansionChanged();
        }
    }

    public boolean isUserLocked() {
        return this.mUserLocked;
    }

    public void setUserLocked(boolean z) {
        this.mUserLocked = z;
        this.mPrivateLayout.setUserExpanding(z);
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer != null) {
            notificationChildrenContainer.setUserLocked(z);
            if (!this.mIsSummaryWithChildren) {
                return;
            }
            if (z || !isGroupExpanded()) {
                updateBackgroundForGroupState();
            }
        }
    }

    public boolean isSystemExpanded() {
        return this.mIsSystemExpanded;
    }

    public void setSystemExpanded(boolean z) {
        if (z != this.mIsSystemExpanded) {
            boolean isExpanded = isExpanded();
            this.mIsSystemExpanded = z;
            notifyHeightChanged(false);
            onExpansionChanged(false, isExpanded);
            if (this.mIsSummaryWithChildren) {
                this.mChildrenContainer.updateGroupOverflow();
            }
        }
    }

    public void setOnKeyguard(boolean z) {
        if (z != this.mOnKeyguard) {
            boolean isExpanded = isExpanded();
            this.mOnKeyguard = z;
            onExpansionChanged(false, isExpanded);
            if (isExpanded != isExpanded()) {
                if (this.mIsSummaryWithChildren) {
                    this.mChildrenContainer.updateGroupOverflow();
                }
                notifyHeightChanged(false);
            }
            updateOutline();
            updateBackground();
        }
    }

    public boolean isClearable() {
        ExpandedNotification expandedNotification = this.mStatusBarNotification;
        if (expandedNotification == null || !expandedNotification.isClearable()) {
            return false;
        }
        if (!this.mIsSummaryWithChildren) {
            return true;
        }
        List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
        for (int i = 0; i < notificationChildren.size(); i++) {
            if (!notificationChildren.get(i).isClearable()) {
                return false;
            }
        }
        return true;
    }

    public int getIntrinsicHeight() {
        int collapsedHeight;
        int extraPadding;
        if (isUserLocked()) {
            return getActualHeight();
        }
        if (!isShowingPublic() || this.mPublicLayout.getContractedChild() == null) {
            NotificationGuts notificationGuts = this.mGuts;
            if (notificationGuts != null && notificationGuts.isExposed()) {
                return this.mGuts.getIntrinsicHeight();
            }
            if (isChildInGroup() && !isGroupExpanded()) {
                return this.mPrivateLayout.getMinHeight();
            }
            if (this.mSensitive && this.mHideSensitiveForIntrinsicHeight) {
                return getMinHeight();
            }
            if (this.mIsSummaryWithChildren) {
                collapsedHeight = Math.max(getShowingLayout().getMinHeight(), this.mChildrenContainer.getIntrinsicHeight());
                extraPadding = getExtraPadding();
            } else if (!isHeadsUpAllowed() || (!this.mIsHeadsUp && !this.mHeadsupDisappearRunning)) {
                if (isExpanded()) {
                    collapsedHeight = getMaxExpandHeight();
                    extraPadding = getExtraPadding();
                } else {
                    collapsedHeight = getCollapsedHeight();
                    extraPadding = getExtraPadding();
                }
            } else if (isPinned() || this.mHeadsupDisappearRunning) {
                return getPinnedHeadsUpHeight(true);
            } else {
                if (isExpanded()) {
                    return Math.max(getMaxExpandHeight(), this.mHeadsUpHeight);
                }
                return Math.max(getCollapsedHeight(), this.mHeadsUpHeight);
            }
        } else {
            collapsedHeight = this.mPublicLayout.getContractedChild().getHeight();
            extraPadding = getExtraPadding();
        }
        return collapsedHeight + extraPadding;
    }

    private boolean isHeadsUpAllowed() {
        return !this.mOnKeyguard && !this.mShowAmbient;
    }

    public boolean isGroupExpanded() {
        ExpandedNotification expandedNotification;
        NotificationGroupManager notificationGroupManager = this.mGroupManager;
        if (notificationGroupManager == null || (expandedNotification = this.mStatusBarNotification) == null) {
            return false;
        }
        return notificationGroupManager.isGroupExpanded(expandedNotification);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0006, code lost:
        r0 = r5.mChildrenContainer;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void onChildrenCountChanged() {
        /*
            r5 = this;
            boolean r0 = com.android.systemui.statusbar.phone.StatusBar.ENABLE_CHILD_NOTIFICATIONS
            r1 = 1
            r2 = 0
            if (r0 == 0) goto L_0x0012
            com.android.systemui.statusbar.stack.NotificationChildrenContainer r0 = r5.mChildrenContainer
            if (r0 == 0) goto L_0x0012
            int r0 = r0.getNotificationChildCount()
            if (r0 <= 0) goto L_0x0012
            r0 = r1
            goto L_0x0013
        L_0x0012:
            r0 = r2
        L_0x0013:
            r5.mIsSummaryWithChildren = r0
            boolean r0 = r5.mIsSummaryWithChildren
            if (r0 == 0) goto L_0x0028
            com.android.systemui.statusbar.stack.NotificationChildrenContainer r0 = r5.mChildrenContainer
            android.view.NotificationHeaderView r0 = r0.getHeaderView()
            if (r0 != 0) goto L_0x0028
            com.android.systemui.statusbar.stack.NotificationChildrenContainer r0 = r5.mChildrenContainer
            android.view.View$OnClickListener r3 = r5.mExpandClickListener
            r0.recreateNotificationHeader(r3)
        L_0x0028:
            com.android.systemui.statusbar.NotificationContentView r0 = r5.getShowingLayout()
            r0.updateBackgroundColor(r2)
            com.android.systemui.statusbar.NotificationContentView r0 = r5.mPrivateLayout
            boolean r3 = r5.isExpandable()
            r0.updateExpandButtons(r3)
            r5.updateChildrenHeaderAppearance()
            r5.updateChildrenVisibility()
            boolean r0 = r5.mIsSummaryWithChildren
            if (r0 == 0) goto L_0x0068
            com.android.systemui.miui.statusbar.ExpandedNotification r0 = r5.mStatusBarNotification
            if (r0 == 0) goto L_0x0068
            r0.setHasShownAfterUnlock(r2)
            com.android.systemui.statusbar.stack.NotificationChildrenContainer r0 = r5.mChildrenContainer
            java.util.List r0 = r0.getNotificationChildren()
            r3 = r2
        L_0x0050:
            int r4 = r0.size()
            if (r3 >= r4) goto L_0x0068
            java.lang.Object r4 = r0.get(r3)
            com.android.systemui.statusbar.ExpandableNotificationRow r4 = (com.android.systemui.statusbar.ExpandableNotificationRow) r4
            com.android.systemui.miui.statusbar.ExpandedNotification r4 = r4.getStatusBarNotification()
            if (r4 == 0) goto L_0x0065
            r4.setHasShownAfterUnlock(r2)
        L_0x0065:
            int r3 = r3 + 1
            goto L_0x0050
        L_0x0068:
            int r0 = r5.getNumberOfNotificationChildren()
            int r0 = java.lang.Math.max(r1, r0)
            r5.updateMiuiPublicView(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.ExpandableNotificationRow.onChildrenCountChanged():void");
    }

    private void updateMiuiPublicView(int i) {
        if (this.mStatusBarNotification.getNotification().publicVersion == null) {
            this.mPublicLayout.setContractedChildText(getContext().getResources().getQuantityString(R.plurals.new_notifications_msg, i, new Object[]{Integer.valueOf(i)}));
        }
    }

    public void updateChildrenHeaderAppearance() {
        if (this.mIsSummaryWithChildren) {
            this.mChildrenContainer.updateChildrenHeaderAppearance();
        }
    }

    public boolean isExpanded() {
        return isExpanded(isMediaNotification() || isExpandableOnKeyguard());
    }

    public boolean isExpanded(boolean z) {
        return (!this.mOnKeyguard || z) && ((!hasUserChangedExpansion() && (isSystemExpanded() || isSystemChildExpanded())) || isUserExpanded()) && !isShowingPublic();
    }

    public void setSystemChildExpanded(boolean z) {
        this.mIsSystemChildExpanded = z;
    }

    public void setLayoutListener(LayoutListener layoutListener) {
        this.mLayoutListener = layoutListener;
    }

    public void removeListener() {
        this.mLayoutListener = null;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        updateMaxHeights();
        if (this.mMenuRow.getMenuView() != null) {
            this.mMenuRow.onHeightUpdate();
        }
        updateContentShiftHeight();
        LayoutListener layoutListener = this.mLayoutListener;
        if (layoutListener != null) {
            layoutListener.onLayout();
        }
        this.mBackgroundNormal.setTop(0);
        NotificationGuts notificationGuts = this.mGuts;
        if (notificationGuts != null) {
            notificationGuts.setTop(0);
        }
    }

    private void updateContentShiftHeight() {
        NotificationHeaderView visibleNotificationHeader = getVisibleNotificationHeader();
        if (visibleNotificationHeader != null) {
            CachingIconView icon = visibleNotificationHeader.getIcon();
            this.mIconTransformContentShift = getRelativeTopPadding(icon) + icon.getHeight();
            return;
        }
        this.mIconTransformContentShift = this.mIconTransformContentShiftNoIcon;
    }

    private void updateMaxHeights() {
        int intrinsicHeight = getIntrinsicHeight();
        if (this.mPrivateLayout.getExpandedChild() == null) {
            this.mPrivateLayout.getContractedChild();
        }
        this.mMaxExpandHeight = this.mPrivateLayout.getMaxHeight();
        if (this.mPrivateLayout.getHeadsUpChild() == null) {
            this.mPrivateLayout.getContractedChild();
        }
        this.mHeadsUpHeight = this.mPrivateLayout.getHeadsUpHeight();
        if (intrinsicHeight != getIntrinsicHeight()) {
            notifyHeightChanged(true);
        }
    }

    public void notifyHeightChanged(boolean z) {
        super.notifyHeightChanged(z);
        getShowingLayout().requestSelectLayout(z || isUserLocked());
    }

    public void setSensitive(boolean z, boolean z2) {
        this.mSensitive = z;
        this.mSensitiveHiddenInGeneral = z2;
    }

    public void setHideSensitiveForIntrinsicHeight(boolean z) {
        this.mHideSensitiveForIntrinsicHeight = z;
        if (this.mIsSummaryWithChildren) {
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                notificationChildren.get(i).setHideSensitiveForIntrinsicHeight(z);
            }
        }
    }

    public void setHideSensitive(boolean z, boolean z2, long j, long j2) {
        boolean z3 = this.mShowingPublic;
        this.mShowingPublic = (this.mSensitive && z && !isMediaNotification()) || this.mEntry.hideSensitiveByAppLock;
        if (this.mShowingPublic && !z3) {
            Log.d("ExpandableNotificationRow", "show public, hideSensitive=" + z + ",mSensitive=" + this.mSensitive);
        }
        if ((!this.mShowingPublicInitialized || this.mShowingPublic != z3) && this.mPublicLayout.getChildCount() != 0) {
            if (!z2) {
                this.mPublicLayout.animate().cancel();
                this.mPrivateLayout.animate().cancel();
                NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
                if (notificationChildrenContainer != null) {
                    notificationChildrenContainer.animate().cancel();
                    this.mChildrenContainer.setAlpha(1.0f);
                }
                this.mPublicLayout.setAlpha(1.0f);
                this.mPrivateLayout.setAlpha(1.0f);
                this.mPublicLayout.setVisibility(this.mShowingPublic ? 0 : 4);
                updateChildrenVisibility();
            } else {
                animateShowingPublic(j, j2);
            }
            NotificationContentView showingLayout = getShowingLayout();
            showingLayout.updateBackgroundColor(z2);
            this.mPrivateLayout.updateExpandButtons(isExpandable());
            showingLayout.setDark(isDark(), false, 0);
            this.mPublicLayout.showPublic();
            this.mShowingPublicInitialized = true;
        }
    }

    private void animateShowingPublic(long j, long j2) {
        View[] viewArr = this.mIsSummaryWithChildren ? new View[]{this.mChildrenContainer} : new View[]{this.mPrivateLayout};
        View[] viewArr2 = {this.mPublicLayout};
        View[] viewArr3 = this.mShowingPublic ? viewArr : viewArr2;
        if (this.mShowingPublic) {
            viewArr = viewArr2;
        }
        for (final View view : viewArr3) {
            view.setVisibility(0);
            view.animate().cancel();
            view.animate().alpha(0.0f).setStartDelay(j).setDuration(j2).withEndAction(new Runnable() {
                public void run() {
                    view.setVisibility(4);
                }
            });
        }
        for (View view2 : viewArr) {
            view2.setVisibility(0);
            view2.setAlpha(0.0f);
            view2.animate().cancel();
            view2.animate().alpha(1.0f).setStartDelay(j).setDuration(j2);
        }
    }

    public boolean isShowingPublic() {
        return this.mShowingPublic;
    }

    public boolean mustStayOnScreen() {
        return this.mIsHeadsUp;
    }

    public boolean canViewBeDismissed() {
        return isClearable() && (!this.mShowingPublic || !this.mSensitiveHiddenInGeneral);
    }

    public void makeActionsVisibile() {
        setUserExpanded(true, true);
        if (isChildInGroup()) {
            this.mGroupManager.setGroupExpanded((StatusBarNotification) this.mStatusBarNotification, true);
        }
        notifyHeightChanged(false);
    }

    public void setChildrenExpanded(boolean z, boolean z2) {
        this.mChildrenExpanded = z;
        if (isChildInGroup() && (isExpanded() || isUserLocked())) {
            setUserExpanded(false);
        }
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer != null) {
            notificationChildrenContainer.setChildrenExpanded(z);
        }
        updateBackgroundForGroupState();
        updateClickAndFocus();
    }

    public int getMaxExpandHeight() {
        return this.mMaxExpandHeight;
    }

    public boolean areGutsExposed() {
        NotificationGuts notificationGuts = this.mGuts;
        return notificationGuts != null && notificationGuts.isExposed();
    }

    public boolean areGutsAnimating() {
        NotificationGuts notificationGuts = this.mGuts;
        return notificationGuts != null && notificationGuts.isAnimating();
    }

    public boolean isContentExpandable() {
        if (!this.mIsSummaryWithChildren || this.mShowingPublic) {
            return getShowingLayout().isContentExpandable();
        }
        return true;
    }

    public View getContentView() {
        if (!this.mIsSummaryWithChildren || this.mShowingPublic) {
            return getShowingLayout();
        }
        return this.mChildrenContainer;
    }

    /* access modifiers changed from: protected */
    public void onAppearAnimationFinished(boolean z) {
        super.onAppearAnimationFinished(z);
        if (z) {
            NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
            if (notificationChildrenContainer != null) {
                notificationChildrenContainer.setAlpha(1.0f);
                this.mChildrenContainer.setLayerType(0, (Paint) null);
            }
            for (NotificationContentView notificationContentView : this.mLayouts) {
                notificationContentView.setAlpha(1.0f);
                notificationContentView.setLayerType(0, (Paint) null);
            }
        }
    }

    public int getExtraBottomPadding() {
        if (!this.mIsSummaryWithChildren || !isGroupExpanded()) {
            return 0;
        }
        return this.mIncreasedPaddingBetweenElements;
    }

    public void setActualHeight(int i, boolean z) {
        ViewGroup viewGroup;
        boolean z2 = i != getActualHeight();
        super.setActualHeight(i, z);
        if (z2 && isRemoved() && (viewGroup = (ViewGroup) getParent()) != null) {
            viewGroup.invalidate();
        }
        NotificationGuts notificationGuts = this.mGuts;
        if (notificationGuts == null || !notificationGuts.isExposed()) {
            int max = Math.max(getMinHeight(), i);
            for (NotificationContentView contentHeight : this.mLayouts) {
                contentHeight.setContentHeight(max);
            }
            if (this.mIsSummaryWithChildren) {
                this.mChildrenContainer.setActualHeight(i);
            }
            NotificationGuts notificationGuts2 = this.mGuts;
            if (notificationGuts2 != null) {
                notificationGuts2.setActualHeight(i);
            }
            if (this.mMenuRow.getMenuView() != null) {
                this.mMenuRow.onHeightUpdate();
                return;
            }
            return;
        }
        this.mGuts.setActualHeight(i);
    }

    public int getMaxContentHeight() {
        int maxHeight;
        int extraPadding;
        if (!this.mIsSummaryWithChildren || this.mShowingPublic) {
            maxHeight = getShowingLayout().getMaxHeight();
            extraPadding = getExtraPadding();
        } else {
            maxHeight = this.mChildrenContainer.getMaxContentHeight();
            extraPadding = getExtraPadding();
        }
        return maxHeight + extraPadding;
    }

    public int getMinHeight() {
        NotificationGuts notificationGuts = this.mGuts;
        if (notificationGuts != null && notificationGuts.isExposed()) {
            return this.mGuts.getIntrinsicHeight();
        }
        if (isHeadsUpAllowed() && ((this.mIsHeadsUp || this.mHeadsupDisappearRunning) && this.mHeadsUpManager.isTrackingHeadsUp() && !this.mShowingPublic)) {
            return getPinnedHeadsUpHeight(false);
        }
        if (this.mIsSummaryWithChildren && !isGroupExpanded() && !this.mShowingPublic) {
            return this.mChildrenContainer.getMinHeight();
        }
        if (!isHeadsUpAllowed() || ((!this.mIsHeadsUp && !this.mHeadsupDisappearRunning) || this.mShowingPublic)) {
            return getShowingLayout().getMinHeight();
        }
        return this.mHeadsUpHeight;
    }

    public int getCollapsedHeight() {
        if (!this.mIsSummaryWithChildren || this.mShowingPublic) {
            return getMinHeight();
        }
        return this.mChildrenContainer.getCollapsedHeight();
    }

    public void setClipTopAmount(int i) {
        super.setClipTopAmount(i);
        for (NotificationContentView clipTopAmount : this.mLayouts) {
            clipTopAmount.setClipTopAmount(i);
        }
        NotificationGuts notificationGuts = this.mGuts;
        if (notificationGuts != null) {
            notificationGuts.setClipTopAmount(i);
        }
    }

    public void setClipBottomAmount(int i) {
        if (i != this.mClipBottomAmount) {
            super.setClipBottomAmount(i);
            for (NotificationContentView clipBottomAmount : this.mLayouts) {
                clipBottomAmount.setClipBottomAmount(i);
            }
            NotificationGuts notificationGuts = this.mGuts;
            if (notificationGuts != null) {
                notificationGuts.setClipBottomAmount(i);
            }
        }
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer != null) {
            notificationChildrenContainer.setClipBottomAmount(i);
        }
    }

    public NotificationContentView getShowingLayout() {
        return this.mShowingPublic ? this.mPublicLayout : this.mPrivateLayout;
    }

    public void setLegacy(boolean z) {
        for (NotificationContentView legacy : this.mLayouts) {
            legacy.setLegacy(z);
        }
    }

    /* access modifiers changed from: protected */
    public void updateBackgroundTint() {
        super.updateBackgroundTint();
        updateBackgroundForGroupState();
        if (this.mIsSummaryWithChildren) {
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                notificationChildren.get(i).updateBackgroundForGroupState();
            }
        }
    }

    public void onFinishedExpansionChange() {
        setGroupExpansionChanging(false);
        updateBackgroundForGroupState();
    }

    public void updateBackgroundForGroupState() {
        boolean z = true;
        if (this.mIsSummaryWithChildren) {
            if (isGroupExpanded() || isGroupExpansionChanging() || isUserLocked()) {
                z = false;
            }
            this.mShowNoBackground = z;
            List<ExpandableNotificationRow> notificationChildren = this.mChildrenContainer.getNotificationChildren();
            for (int i = 0; i < notificationChildren.size(); i++) {
                notificationChildren.get(i).updateBackgroundForGroupState();
            }
        } else if (isChildInGroup()) {
            this.mShowNoBackground = true;
        } else {
            this.mShowNoBackground = false;
        }
        updateOutline();
        updateBackground();
    }

    /* access modifiers changed from: protected */
    public void updateBackground() {
        super.updateBackground();
        boolean z = false;
        this.mIsOptimizedGameHeadsUpBg = false;
        int i = 4;
        this.mBackgroundDimmed.setVisibility(4);
        boolean z2 = !isChildInGroup() || getTranslation() < 0.0f;
        NotificationBackgroundView notificationBackgroundView = this.mBackgroundNormal;
        if (z2) {
            i = 0;
        }
        notificationBackgroundView.setVisibility(i);
        if (this.mIsShowHeadsUpBackground && !this.mGroupManager.isChildInGroupWithSummary(this.mStatusBarNotification)) {
            View headsUpChild = getShowingLayout().getHeadsUpChild();
            if ((headsUpChild instanceof OptimizedHeadsUpNotificationView) && ((OptimizedHeadsUpNotificationView) headsUpChild).isGameModeUi()) {
                z = true;
            }
            this.mBackgroundNormal.setCustomBackground(z ? R.drawable.optimized_game_heads_up_notification_bg : R.drawable.notification_heads_up_bg);
            this.mIsOptimizedGameHeadsUpBg = z;
        } else if (!isMediaNotification()) {
            if (z2 && isChildInGroup()) {
                z = true;
            }
            this.mBackgroundNormal.setCustomBackground(z ? R.drawable.notification_child_bg : R.drawable.notification_item_bg);
        }
    }

    public void showHeadsUpBackground() {
        setMiniBarVisible(this.mEntry.mIsShowMiniWindowBar);
        this.mIsShowHeadsUpBackground = true;
        updateBackground();
        this.mBackgroundNormal.setShowHeadsUp(true);
    }

    public void hideHeadsUpBackground() {
        this.mIsShowHeadsUpBackground = false;
        updateBackground();
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer != null && notificationChildrenContainer.getNotificationChildCount() > 0) {
            for (ExpandableNotificationRow hideHeadsUpBackground : this.mChildrenContainer.getNotificationChildren()) {
                hideHeadsUpBackground.hideHeadsUpBackground();
            }
        }
        setMiniBarVisible(false);
        View headsUpChild = getShowingLayout().getHeadsUpChild();
        if (headsUpChild instanceof OptimizedHeadsUpNotificationView) {
            ((OptimizedHeadsUpNotificationView) headsUpChild).hideMiniWindowBar();
        }
        this.mBackgroundNormal.setShowHeadsUp(false);
    }

    public void updateMiniBarAlpha(float f) {
        View findViewById;
        View viewForVisibleType = getShowingLayout().getViewForVisibleType(getShowingLayout().getVisibleType());
        if (viewForVisibleType != null && (findViewById = viewForVisibleType.findViewById(R.id.mini_window_bar)) != null) {
            findViewById.setAlpha(f);
        }
    }

    public void setMiniBarVisible(boolean z) {
        getShowingLayout().setMiniBarVisible(z);
    }

    public int getPositionOfChild(ExpandableNotificationRow expandableNotificationRow) {
        if (this.mIsSummaryWithChildren) {
            return this.mChildrenContainer.getPositionInLinearLayout(expandableNotificationRow);
        }
        return 0;
    }

    public void setExpansionLogger(ExpansionLogger expansionLogger, String str) {
        this.mLogger = expansionLogger;
        this.mLoggingKey = str;
    }

    public void onExpandedByGesture(boolean z) {
        MetricsLogger.action(this.mContext, this.mGroupManager.isSummaryOfGroup(getStatusBarNotification()) ? 410 : 409, z);
    }

    /* access modifiers changed from: protected */
    public boolean disallowSingleClick(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();
        NotificationHeaderView visibleNotificationHeader = getVisibleNotificationHeader();
        if (visibleNotificationHeader != null) {
            return visibleNotificationHeader.isInTouchRect(x - getTranslation(), y);
        }
        return super.disallowSingleClick(motionEvent);
    }

    /* access modifiers changed from: private */
    public void onExpansionChanged(boolean z, boolean z2) {
        NotificationHeaderView visibleNotificationHeader;
        boolean isExpanded = isExpanded();
        if (this.mIsSummaryWithChildren && (!this.mIsLowPriority || z2)) {
            isExpanded = this.mGroupManager.isGroupExpanded(this.mStatusBarNotification);
        }
        NotificationMenuRowPlugin notificationMenuRowPlugin = this.mMenuRow;
        if (notificationMenuRowPlugin != null) {
            notificationMenuRowPlugin.onExpansionChanged();
        }
        if (isExpanded != z2) {
            if (this.mLogger != null && isContentExpandable()) {
                this.mLogger.logNotificationExpansion(this.mLoggingKey, z, isExpanded);
            }
            if (this.mIsSummaryWithChildren) {
                this.mChildrenContainer.onExpansionChanged();
            }
            if (isLowPriority() && (visibleNotificationHeader = getVisibleNotificationHeader()) != null) {
                visibleNotificationHeader.setExpanded(isExpanded);
            }
            updateBackground();
        }
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfoInternal(accessibilityNodeInfo);
        if (canViewBeDismissed()) {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_DISMISS);
        }
        boolean z = this.mShowingPublic;
        boolean z2 = false;
        if (!z) {
            if (this.mIsSummaryWithChildren) {
                z = true;
                if (!this.mIsLowPriority || isExpanded()) {
                    z2 = isGroupExpanded();
                }
            } else {
                z = this.mPrivateLayout.isContentExpandable();
                z2 = isExpanded();
            }
        }
        if (!z) {
            return;
        }
        if (z2) {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_COLLAPSE);
        } else {
            accessibilityNodeInfo.addAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_EXPAND);
        }
    }

    public boolean performAccessibilityActionInternal(int i, Bundle bundle) {
        if (super.performAccessibilityActionInternal(i, bundle)) {
            return true;
        }
        if (i == 262144 || i == 524288) {
            this.mExpandClickListener.onClick(this);
            return true;
        } else if (i != 1048576) {
            return false;
        } else {
            NotificationStackScrollLayout.performDismiss(this, this.mGroupManager, true);
            return true;
        }
    }

    public boolean shouldRefocusOnDismiss() {
        return this.mRefocusOnDismiss || isAccessibilityFocused();
    }

    public ExpandableViewState createExpandableViewState() {
        return new NotificationViewState();
    }

    public boolean isAboveShelf() {
        return !isOnKeyguard() && (this.mIsPinned || this.mHeadsupDisappearRunning || (this.mIsHeadsUp && this.mAboveShelf));
    }

    public void setShowAmbient(boolean z) {
        if (z != this.mShowAmbient) {
            this.mShowAmbient = z;
            NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
            if (notificationChildrenContainer != null) {
                notificationChildrenContainer.notifyShowAmbientChanged();
            }
            notifyHeightChanged(false);
        }
    }

    public boolean isShowingAmbient() {
        return this.mShowAmbient;
    }

    public void setAboveShelf(boolean z) {
        this.mAboveShelf = z;
    }

    public static class NotificationViewState extends ExpandableViewState {
        private NotificationViewState() {
        }

        public void applyToView(View view) {
            super.applyToView(view);
            if (view instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) view).applyChildrenState();
            }
        }

        /* access modifiers changed from: protected */
        public void onYTranslationAnimationFinished(View view) {
            super.onYTranslationAnimationFinished(view);
            if (view instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) view;
                if (expandableNotificationRow.isHeadsUpAnimatingAway()) {
                    expandableNotificationRow.setHeadsUpAnimatingAway(false);
                }
            }
        }

        public void animateTo(View view, AnimationProperties animationProperties) {
            super.animateTo(view, animationProperties);
            if (view instanceof ExpandableNotificationRow) {
                ((ExpandableNotificationRow) view).startChildAnimation(animationProperties);
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setChildrenContainer(NotificationChildrenContainer notificationChildrenContainer) {
        this.mChildrenContainer = notificationChildrenContainer;
    }

    public View.OnClickListener getExpandClickListener() {
        return this.mExpandClickListener;
    }

    /* access modifiers changed from: protected */
    public boolean needsOutline() {
        if (!isOnKeyguard() && !isHeadsUp() && !isHeadsUpAnimatingAway()) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public int getOutlineRadius() {
        if (isHeadsUp() || isHeadsUpAnimatingAway()) {
            return this.mNotificationHeadsUpBgRadius;
        }
        return super.getOutlineRadius();
    }

    /* access modifiers changed from: protected */
    public void updateOutlineAlpha() {
        if (isOnKeyguard()) {
            setOutlineAlpha(0.0f);
        } else {
            super.updateOutlineAlpha();
        }
    }

    public void setIsFirstRow(boolean z) {
        NotificationChildrenContainer notificationChildrenContainer = this.mChildrenContainer;
        if (notificationChildrenContainer != null) {
            for (ExpandableNotificationRow isFirstRow : notificationChildrenContainer.getNotificationChildren()) {
                isFirstRow.setIsFirstRow(false);
            }
        }
        this.mIsFirstRow = z;
    }

    public int getExtraPadding() {
        return getPaddingTop() + getPaddingBottom();
    }

    public NotificationBackgroundView getBackgroundNormal() {
        return this.mBackgroundNormal;
    }

    public boolean isMediaNotification() {
        if (getPrivateLayout() != null) {
            return getPrivateLayout().isMediaNotification(this.mStatusBarNotification);
        }
        return NotificationUtil.isMediaNotification(this.mStatusBarNotification);
    }

    public boolean isCustomViewNotification() {
        if (getPrivateLayout() != null) {
            return getPrivateLayout().isCustomViewNotification(this.mStatusBarNotification);
        }
        return NotificationUtil.isCustomViewNotification(this.mStatusBarNotification);
    }

    private boolean isExpandableOnKeyguard() {
        ExpandedNotification expandedNotification = this.mStatusBarNotification;
        return expandedNotification != null && expandedNotification.isExpandableOnKeyguard();
    }

    public void setHiddenForAnimation(boolean z) {
        this.mHiddenForAnimation = z;
    }

    public boolean isHiddenForAnimation() {
        return this.mHiddenForAnimation;
    }

    public boolean isOptimizedGameHeadsUpBg() {
        return this.mIsOptimizedGameHeadsUpBg;
    }
}

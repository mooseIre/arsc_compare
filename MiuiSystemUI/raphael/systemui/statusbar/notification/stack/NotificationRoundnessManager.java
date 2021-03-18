package com.android.systemui.statusbar.notification.stack;

import android.util.MathUtils;
import com.android.systemui.statusbar.notification.MiuiNotificationSectionsFeatureManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.util.HashSet;

public class NotificationRoundnessManager implements OnHeadsUpChangedListener {
    private HashSet<ExpandableView> mAnimatedChildren;
    private float mAppearFraction;
    private final KeyguardBypassController mBypassController;
    private boolean mExpanded;
    private final ExpandableView[] mFirstInSectionViews;
    private final ExpandableView[] mLastInSectionViews;
    private Runnable mRoundingChangedCallback;
    private final ExpandableView[] mTmpFirstInSectionViews;
    private final ExpandableView[] mTmpLastInSectionViews;
    private ExpandableNotificationRow mTrackedHeadsUp;

    NotificationRoundnessManager(KeyguardBypassController keyguardBypassController, MiuiNotificationSectionsFeatureManager miuiNotificationSectionsFeatureManager) {
        int numberOfBuckets = miuiNotificationSectionsFeatureManager.getNumberOfBuckets();
        this.mFirstInSectionViews = new ExpandableView[numberOfBuckets];
        this.mLastInSectionViews = new ExpandableView[numberOfBuckets];
        this.mTmpFirstInSectionViews = new ExpandableView[numberOfBuckets];
        this.mTmpLastInSectionViews = new ExpandableView[numberOfBuckets];
        this.mBypassController = keyguardBypassController;
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpPinned(NotificationEntry notificationEntry) {
        updateView(notificationEntry.getRow(), false);
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpUnPinned(NotificationEntry notificationEntry) {
        updateView(notificationEntry.getRow(), true);
    }

    public void onHeadsupAnimatingAwayChanged(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        updateView(expandableNotificationRow, false);
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
        updateView(notificationEntry.getRow(), false);
    }

    private void updateView(ExpandableView expandableView, boolean z) {
        if (updateViewWithoutCallback(expandableView, z)) {
            this.mRoundingChangedCallback.run();
        }
    }

    private boolean updateViewWithoutCallback(ExpandableView expandableView, boolean z) {
        float roundness = getRoundness(expandableView, true);
        float roundness2 = getRoundness(expandableView, false);
        boolean topRoundness = expandableView.setTopRoundness(roundness, z);
        boolean bottomRoundness = expandableView.setBottomRoundness(roundness2, z);
        boolean isFirstInSection = isFirstInSection(expandableView, false);
        boolean isLastInSection = isLastInSection(expandableView, false);
        expandableView.setFirstInSection(isFirstInSection);
        expandableView.setLastInSection(isLastInSection);
        return (isFirstInSection || isLastInSection) && (topRoundness || bottomRoundness);
    }

    private boolean isFirstInSection(ExpandableView expandableView, boolean z) {
        int i = 0;
        int i2 = 0;
        while (true) {
            ExpandableView[] expandableViewArr = this.mFirstInSectionViews;
            if (i >= expandableViewArr.length) {
                return false;
            }
            if (expandableView != expandableViewArr[i]) {
                if (expandableViewArr[i] != null) {
                    i2++;
                }
                i++;
            } else if (z || i2 > 0) {
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean isLastInSection(ExpandableView expandableView, boolean z) {
        int i = 0;
        for (int length = this.mLastInSectionViews.length - 1; length >= 0; length--) {
            ExpandableView[] expandableViewArr = this.mLastInSectionViews;
            if (expandableView == expandableViewArr[length]) {
                return z || i > 0;
            }
            if (expandableViewArr[length] != null) {
                i++;
            }
        }
        return false;
    }

    private float getRoundness(ExpandableView expandableView, boolean z) {
        if ((expandableView.isPinned() || expandableView.isHeadsUpAnimatingAway()) && !this.mExpanded) {
            return 1.0f;
        }
        if (isFirstInSection(expandableView, true) && z) {
            return 1.0f;
        }
        if (isLastInSection(expandableView, true) && !z) {
            return 1.0f;
        }
        if (expandableView == this.mTrackedHeadsUp) {
            return MathUtils.saturate(1.0f - this.mAppearFraction);
        }
        if (!expandableView.showingPulsing() || this.mBypassController.getBypassEnabled()) {
            return 0.0f;
        }
        return 1.0f;
    }

    public void setExpanded(float f, float f2) {
        this.mExpanded = f != 0.0f;
        this.mAppearFraction = f2;
        ExpandableNotificationRow expandableNotificationRow = this.mTrackedHeadsUp;
        if (expandableNotificationRow != null) {
            updateView(expandableNotificationRow, true);
        }
    }

    public void updateRoundedChildren(NotificationSection[] notificationSectionArr) {
        boolean handleRemovedOldViews;
        for (int i = 0; i < notificationSectionArr.length; i++) {
            ExpandableView[] expandableViewArr = this.mTmpFirstInSectionViews;
            ExpandableView[] expandableViewArr2 = this.mFirstInSectionViews;
            expandableViewArr[i] = expandableViewArr2[i];
            this.mTmpLastInSectionViews[i] = this.mLastInSectionViews[i];
            expandableViewArr2[i] = notificationSectionArr[i].getFirstVisibleChild();
            this.mLastInSectionViews[i] = notificationSectionArr[i].getLastVisibleChild();
        }
        if (handleAddedNewViews(notificationSectionArr, this.mTmpLastInSectionViews, false) || (((handleRemovedOldViews(notificationSectionArr, this.mTmpFirstInSectionViews, true) | false) | handleRemovedOldViews(notificationSectionArr, this.mTmpLastInSectionViews, false)) || handleAddedNewViews(notificationSectionArr, this.mTmpFirstInSectionViews, true))) {
            this.mRoundingChangedCallback.run();
        }
    }

    private boolean handleRemovedOldViews(NotificationSection[] notificationSectionArr, ExpandableView[] expandableViewArr, boolean z) {
        boolean z2;
        boolean z3;
        ExpandableView expandableView;
        boolean z4 = false;
        for (ExpandableView expandableView2 : expandableViewArr) {
            if (expandableView2 != null) {
                int length = notificationSectionArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        z2 = false;
                        break;
                    }
                    NotificationSection notificationSection = notificationSectionArr[i];
                    if (z) {
                        expandableView = notificationSection.getFirstVisibleChild();
                    } else {
                        expandableView = notificationSection.getLastVisibleChild();
                    }
                    if (expandableView != expandableView2) {
                        i++;
                    } else if (expandableView2.isFirstInSection() == isFirstInSection(expandableView2, false) && expandableView2.isLastInSection() == isLastInSection(expandableView2, false)) {
                        z3 = false;
                        z2 = true;
                    } else {
                        z2 = true;
                    }
                }
                z3 = z2;
                if (!z2 || z3) {
                    if (!expandableView2.isRemoved()) {
                        updateViewWithoutCallback(expandableView2, expandableView2.isShown());
                    }
                    z4 = true;
                }
            }
        }
        return z4;
    }

    private boolean handleAddedNewViews(NotificationSection[] notificationSectionArr, ExpandableView[] expandableViewArr, boolean z) {
        boolean z2;
        boolean z3 = false;
        for (NotificationSection notificationSection : notificationSectionArr) {
            ExpandableView firstVisibleChild = z ? notificationSection.getFirstVisibleChild() : notificationSection.getLastVisibleChild();
            if (firstVisibleChild != null) {
                int length = expandableViewArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        z2 = false;
                        break;
                    } else if (expandableViewArr[i] == firstVisibleChild) {
                        z2 = true;
                        break;
                    } else {
                        i++;
                    }
                }
                if (!z2) {
                    updateViewWithoutCallback(firstVisibleChild, firstVisibleChild.isShown() && !this.mAnimatedChildren.contains(firstVisibleChild));
                    z3 = true;
                }
            }
        }
        return z3;
    }

    public void setAnimatedChildren(HashSet<ExpandableView> hashSet) {
        this.mAnimatedChildren = hashSet;
    }

    public void setOnRoundingChangedCallback(Runnable runnable) {
        this.mRoundingChangedCallback = runnable;
    }

    public void setTrackingHeadsUp(ExpandableNotificationRow expandableNotificationRow) {
        ExpandableNotificationRow expandableNotificationRow2 = this.mTrackedHeadsUp;
        this.mTrackedHeadsUp = expandableNotificationRow;
        if (expandableNotificationRow2 != null) {
            updateView(expandableNotificationRow2, true);
        }
    }
}

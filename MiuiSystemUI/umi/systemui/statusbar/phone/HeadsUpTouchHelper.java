package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.systemui.Dependency;
import com.android.systemui.Gefingerpoken;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.HeadsUpManagerInjector;

public class HeadsUpTouchHelper implements Gefingerpoken {
    private Callback mCallback;
    private boolean mCollapseSnoozes;
    private HeadsUpManagerPhone mHeadsUpManager;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private NotificationPanelViewController mPanel;
    private ExpandableNotificationRow mPickedChild;
    private float mTouchSlop;
    private boolean mTouchingHeadsUpView;
    private boolean mTrackingHeadsUp;
    private int mTrackingPointer;

    public interface Callback {
        ExpandableView getChildAtRawPosition(float f, float f2);

        Context getContext();

        boolean isExpanded();
    }

    public HeadsUpTouchHelper(HeadsUpManagerPhone headsUpManagerPhone, Callback callback, NotificationPanelViewController notificationPanelViewController) {
        this.mHeadsUpManager = headsUpManagerPhone;
        this.mCallback = callback;
        this.mPanel = notificationPanelViewController;
        this.mTouchSlop = (float) ViewConfiguration.get(callback.getContext()).getScaledTouchSlop();
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(new ConfigurationController.ConfigurationListener() {
            /* class com.android.systemui.statusbar.phone.HeadsUpTouchHelper.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
            public void onDensityOrFontScaleChanged() {
                Context context = HeadsUpTouchHelper.this.mCallback.getContext();
                if (context != null) {
                    HeadsUpTouchHelper.this.mTouchSlop = (float) ViewConfiguration.get(context).getScaledTouchSlop();
                }
            }
        });
    }

    public boolean isTrackingHeadsUp() {
        return this.mTrackingHeadsUp;
    }

    @Override // com.android.systemui.Gefingerpoken
    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        NotificationEntry topEntry;
        int pointerId;
        boolean z = false;
        if (!this.mTouchingHeadsUpView && motionEvent.getActionMasked() != 0) {
            return false;
        }
        int findPointerIndex = motionEvent.findPointerIndex(this.mTrackingPointer);
        if (findPointerIndex < 0) {
            this.mTrackingPointer = motionEvent.getPointerId(0);
            findPointerIndex = 0;
        }
        float x = motionEvent.getX(findPointerIndex);
        float y = motionEvent.getY(findPointerIndex);
        int actionMasked = motionEvent.getActionMasked();
        boolean z2 = true;
        int i = 1;
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    float f = y - this.mInitialTouchY;
                    if (this.mTouchingHeadsUpView && Math.abs(f) > this.mTouchSlop && Math.abs(f) > Math.abs(x - this.mInitialTouchX)) {
                        setTrackingHeadsUp(true);
                        float f2 = 0.0f;
                        if (f < 0.0f) {
                            z = true;
                        }
                        this.mCollapseSnoozes = z;
                        this.mInitialTouchX = x;
                        this.mInitialTouchY = y;
                        int actualHeight = (int) (((float) this.mPickedChild.getActualHeight()) + this.mPickedChild.getTranslationY());
                        float maxPanelHeight = (float) this.mPanel.getMaxPanelHeight();
                        NotificationPanelViewController notificationPanelViewController = this.mPanel;
                        if (maxPanelHeight > 0.0f) {
                            f2 = ((float) actualHeight) / maxPanelHeight;
                        }
                        notificationPanelViewController.setPanelScrimMinFraction(f2);
                        this.mPanel.startExpandMotion(x, y, true, (float) actualHeight);
                        this.mPanel.startExpandingFromPeek();
                        this.mHeadsUpManager.unpinAll(true);
                        this.mPanel.clearNotificationEffects();
                        endMotion();
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
            ExpandableNotificationRow expandableNotificationRow = this.mPickedChild;
            if (expandableNotificationRow == null || !this.mTouchingHeadsUpView || !this.mHeadsUpManager.shouldSwallowClick(expandableNotificationRow.getEntry().getSbn().getKey())) {
                endMotion();
            } else {
                endMotion();
                return true;
            }
        } else {
            this.mInitialTouchY = y;
            this.mInitialTouchX = x;
            setTrackingHeadsUp(false);
            ExpandableView childAtRawPosition = this.mCallback.getChildAtRawPosition(x, y);
            this.mTouchingHeadsUpView = false;
            if (childAtRawPosition instanceof ExpandableNotificationRow) {
                ExpandableNotificationRow expandableNotificationRow2 = (ExpandableNotificationRow) childAtRawPosition;
                if (this.mCallback.isExpanded() || !expandableNotificationRow2.isHeadsUp() || !expandableNotificationRow2.isPinned()) {
                    z2 = false;
                }
                this.mTouchingHeadsUpView = z2;
                if (z2) {
                    this.mPickedChild = expandableNotificationRow2;
                }
            } else if (childAtRawPosition == null && !this.mCallback.isExpanded() && (topEntry = this.mHeadsUpManager.getTopEntry()) != null && topEntry.isRowPinned()) {
                this.mPickedChild = topEntry.getRow();
                this.mTouchingHeadsUpView = true;
            }
        }
        return false;
    }

    public void setTrackingHeadsUp(boolean z) {
        this.mTrackingHeadsUp = z;
        this.mHeadsUpManager.setTrackingHeadsUp(z);
        this.mPanel.setTrackedHeadsUp(z ? this.mPickedChild : null);
    }

    public void notifyFling(boolean z) {
        if (z && this.mCollapseSnoozes) {
            if (this.mHeadsUpManager.getTopEntry() != null) {
                ((NotificationStat) Dependency.get(NotificationStat.class)).onFloatManualCollapse(this.mHeadsUpManager.getTopEntry(), HeadsUpManagerInjector.getSnoozeNotify());
            }
            this.mHeadsUpManager.snooze();
        }
        this.mCollapseSnoozes = false;
    }

    @Override // com.android.systemui.Gefingerpoken
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mTrackingHeadsUp) {
            return false;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 1 || actionMasked == 3) {
            endMotion();
            setTrackingHeadsUp(false);
        }
        return true;
    }

    private void endMotion() {
        this.mTrackingPointer = -1;
        this.mPickedChild = null;
        this.mTouchingHeadsUpView = false;
    }
}

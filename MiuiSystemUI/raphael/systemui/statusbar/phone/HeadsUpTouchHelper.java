package com.android.systemui.statusbar.phone;

import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import com.android.systemui.Gefingerpoken;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;

public class HeadsUpTouchHelper implements Gefingerpoken {
    private boolean mCollapseSnoozes;
    private HeadsUpManager mHeadsUpManager;
    private float mInitialTouchX;
    private float mInitialTouchY;
    private NotificationPanelView mPanel;
    private ExpandableNotificationRow mPickedChild;
    private NotificationStackScrollLayout mStackScroller;
    private StatusBar mStatusBar;
    private float mTouchSlop;
    private boolean mTouchingHeadsUpView;
    private boolean mTrackingHeadsUp;
    private int mTrackingPointer;

    public HeadsUpTouchHelper(HeadsUpManager headsUpManager, NotificationStackScrollLayout notificationStackScrollLayout, NotificationPanelView notificationPanelView, StatusBar statusBar) {
        this.mHeadsUpManager = headsUpManager;
        this.mStackScroller = notificationStackScrollLayout;
        this.mPanel = notificationPanelView;
        this.mStatusBar = statusBar;
        this.mTouchSlop = (float) ViewConfiguration.get(notificationStackScrollLayout.getContext()).getScaledTouchSlop();
    }

    public boolean isTrackingHeadsUp() {
        return this.mTrackingHeadsUp;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        int pointerId;
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
        boolean z = true;
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked == 2) {
                    float f = y - this.mInitialTouchY;
                    if (this.mTouchingHeadsUpView && Math.abs(f) > this.mTouchSlop && Math.abs(f) > Math.abs(x - this.mInitialTouchX) && f < 0.0f) {
                        this.mTrackingHeadsUp = true;
                        this.mStatusBar.showReturnToInCallScreenButtonIfNeed();
                        this.mHeadsUpManager.removeNotification(this.mPickedChild.getEntry().key, true);
                        this.mTouchingHeadsUpView = false;
                        this.mHeadsUpManager.unpinAll();
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
            ExpandableNotificationRow expandableNotificationRow = this.mPickedChild;
            if (expandableNotificationRow == null || !this.mTouchingHeadsUpView || !this.mHeadsUpManager.shouldSwallowClick(expandableNotificationRow.getStatusBarNotification().getKey())) {
                endMotion();
            } else {
                endMotion();
                return true;
            }
        } else {
            this.mInitialTouchY = y;
            this.mInitialTouchX = x;
            setTrackingHeadsUp(false);
            ExpandableView childAtRawPosition = this.mStackScroller.getChildAtRawPosition(x, y);
            this.mTouchingHeadsUpView = false;
            if (childAtRawPosition instanceof ExpandableNotificationRow) {
                this.mPickedChild = (ExpandableNotificationRow) childAtRawPosition;
                if (this.mStackScroller.isExpanded() || !this.mPickedChild.isHeadsUp() || !this.mPickedChild.isPinned()) {
                    z = false;
                }
                this.mTouchingHeadsUpView = z;
            }
        }
        return false;
    }

    private void setTrackingHeadsUp(boolean z) {
        if (this.mTrackingHeadsUp != z) {
            Log.d("HeadsUpTouchHelper", "setTrackingHeadsUp tracking=" + z);
        }
        this.mTrackingHeadsUp = z;
        this.mHeadsUpManager.setTrackingHeadsUp(z);
        this.mPanel.setTrackingHeadsUp(z);
    }

    public void notifyFling(boolean z) {
        if (z && this.mCollapseSnoozes) {
            this.mHeadsUpManager.snooze();
        }
        this.mCollapseSnoozes = false;
    }

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

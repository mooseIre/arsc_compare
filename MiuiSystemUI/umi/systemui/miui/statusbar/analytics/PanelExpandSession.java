package com.android.systemui.miui.statusbar.analytics;

import com.android.systemui.miui.statusbar.analytics.Analytics$CancelAllNotiEvent;
import com.android.systemui.miui.statusbar.analytics.Analytics$CollapseEvent;
import com.android.systemui.miui.statusbar.analytics.Analytics$ExpandEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PanelExpandSession {
    public Analytics$CancelAllNotiEvent cancelAllNotiEvent;
    public Analytics$CollapseEvent collapseEvent;
    public Analytics$ExpandEvent expandEvent;
    public boolean mChangeBrightness = false;
    public long mCreateTimeMillis = System.currentTimeMillis();
    public Analytics$CollapseEvent.ACTION mFistNotiAction = Analytics$CollapseEvent.ACTION.NONE;
    public long mFistNotiActionDuration;
    public long mFullyExpandedTimeMillis = this.mCreateTimeMillis;
    public boolean mIsBackPressed = false;
    public boolean mIsBlock = false;
    public boolean mIsClick = false;
    public boolean mIsClickQS = false;
    public boolean mIsHomePressed = false;
    public boolean mIsRemove = false;
    public boolean mIsRemoveAll = false;
    public boolean mOpenQSPanel = false;
    public boolean mScrollMore = false;
    public boolean mSetImportance = false;
    public Set<String> mVisibleKeys = new HashSet();

    public void start(String str, boolean z, int i) {
        if (this.expandEvent == null) {
            this.expandEvent = new Analytics$ExpandEvent();
            Analytics$ExpandEvent analytics$ExpandEvent = this.expandEvent;
            analytics$ExpandEvent.currentPage = str;
            analytics$ExpandEvent.expandMode = z ? Analytics$ExpandEvent.MODE.MANUAL : Analytics$ExpandEvent.MODE.COMMAND;
            this.expandEvent.notificationsCount = i;
        }
    }

    public void end(boolean z, int i) {
        if (this.collapseEvent == null) {
            this.collapseEvent = new Analytics$CollapseEvent();
            this.collapseEvent.collapseMode = getCollapseMode(z);
            Analytics$CollapseEvent analytics$CollapseEvent = this.collapseEvent;
            analytics$CollapseEvent.notificationsCount = i;
            analytics$CollapseEvent.isQsExpanded = this.mOpenQSPanel ? 1 : 0;
            analytics$CollapseEvent.isClickQsToggle = this.mIsClickQS ? 1 : 0;
            analytics$CollapseEvent.isSlideBrightnessBar = this.mChangeBrightness ? 1 : 0;
            analytics$CollapseEvent.isSlideNotificationBar = this.mScrollMore ? 1 : 0;
            analytics$CollapseEvent.isDeleteNotification = this.mIsRemove ? 1 : 0;
            analytics$CollapseEvent.residenceTime = System.currentTimeMillis() - this.mCreateTimeMillis;
            Analytics$CollapseEvent analytics$CollapseEvent2 = this.collapseEvent;
            analytics$CollapseEvent2.fistNotificationAction = this.mFistNotiAction;
            analytics$CollapseEvent2.fistNotificationActionDuration = this.mFistNotiActionDuration;
            analytics$CollapseEvent2.notificationVisibleCount = this.mVisibleKeys.size();
        }
    }

    public void removeAllNotifications(boolean z, int i, boolean z2) {
        if (this.cancelAllNotiEvent == null) {
            this.cancelAllNotiEvent = new Analytics$CancelAllNotiEvent();
            this.cancelAllNotiEvent.clearAllMode = z ? Analytics$CancelAllNotiEvent.MODE.CLEAR_FOLDED : Analytics$CancelAllNotiEvent.MODE.CLEAR_ALL;
            Analytics$CancelAllNotiEvent analytics$CancelAllNotiEvent = this.cancelAllNotiEvent;
            analytics$CancelAllNotiEvent.notificationsCount = i;
            analytics$CancelAllNotiEvent.isSlideNotificationBar = z2 ? 1 : 0;
        }
    }

    public void onBackPressed() {
        this.mIsBackPressed = true;
    }

    public void onHomePressed() {
        this.mIsHomePressed = true;
    }

    public void markRemove() {
        this.mIsRemove = true;
    }

    public void markRemoveAll() {
        this.mIsRemoveAll = true;
        markFirstNotiAction(Analytics$CollapseEvent.ACTION.CLEAR_ALL);
    }

    public void markClick() {
        this.mIsClick = true;
        markFirstNotiAction(Analytics$CollapseEvent.ACTION.CLICK);
    }

    public void markClickQS() {
        this.mIsClickQS = true;
    }

    public void markBlock() {
        this.mIsBlock = true;
    }

    public void scrollMore() {
        this.mScrollMore = true;
    }

    public void openQSPanel() {
        this.mOpenQSPanel = true;
    }

    public void markNotiLongPress() {
        markFirstNotiAction(Analytics$CollapseEvent.ACTION.LONG_PRESS);
    }

    public void markNotiSwipeLeft() {
        markFirstNotiAction(Analytics$CollapseEvent.ACTION.SWIPE_LEFT);
    }

    public void markNotiSwipeRight() {
        markFirstNotiAction(Analytics$CollapseEvent.ACTION.SWIPE_RIGHT);
    }

    public void logNotificationVisibilityChanges(List<String> list) {
        this.mVisibleKeys.addAll(list);
    }

    public void markAnimationEnd() {
        this.mFullyExpandedTimeMillis = System.currentTimeMillis();
    }

    private void markFirstNotiAction(Analytics$CollapseEvent.ACTION action) {
        if (this.mFistNotiAction == Analytics$CollapseEvent.ACTION.NONE) {
            this.mFistNotiAction = action;
            this.mFistNotiActionDuration = System.currentTimeMillis() - this.mFullyExpandedTimeMillis;
        }
    }

    private Analytics$CollapseEvent.MODE getCollapseMode(boolean z) {
        if (this.mIsClick) {
            return Analytics$CollapseEvent.MODE.CLICK_NOTIFICATION;
        }
        if (this.mIsBackPressed) {
            return Analytics$CollapseEvent.MODE.BACK;
        }
        if (this.mIsHomePressed) {
            return Analytics$CollapseEvent.MODE.HOME;
        }
        if (this.mIsRemoveAll) {
            return Analytics$CollapseEvent.MODE.CLICK_CLEAR_ALL;
        }
        if (!z) {
            return Analytics$CollapseEvent.MODE.COMMAND;
        }
        return Analytics$CollapseEvent.MODE.OTHER;
    }
}

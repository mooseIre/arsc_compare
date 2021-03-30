package com.android.systemui.statusbar.notification.analytics;

import android.content.Context;
import com.miui.systemui.EventTracker;
import com.miui.systemui.events.CollapseEvent;
import com.miui.systemui.events.CollapseMode;
import com.miui.systemui.events.ExpandEvent;
import com.miui.systemui.events.ExpandMode;
import com.miui.systemui.events.NotifAction;
import java.util.HashSet;
import java.util.Set;

public class NotificationPanelStat {
    private boolean mChangeBrightness = false;
    public CollapseEvent mCollapseEvent;
    private long mCreateTimeMillis = 0;
    private EventTracker mEventTracker;
    public ExpandEvent mExpandEvent;
    private NotifAction mFistNotifAction = NotifAction.NONE;
    private long mFistNotifActionDuration = 0;
    private long mFullyExpandedTimeMills = 0;
    private boolean mIsBackPressed = false;
    private boolean mIsClick = false;
    private boolean mIsClickQS = false;
    private boolean mIsHomePressed = false;
    private boolean mIsRemove = false;
    private boolean mIsRemoveAll = false;
    private boolean mOpenQSPanel = false;
    private int mPanelSlidingTimes = 0;
    private boolean mScrollMore = false;
    private Set<String> mVisibleKeys = new HashSet();

    public NotificationPanelStat(Context context, EventTracker eventTracker) {
        this.mEventTracker = eventTracker;
    }

    private void reset() {
        this.mIsBackPressed = false;
        this.mIsHomePressed = false;
        this.mIsRemove = false;
        this.mIsRemoveAll = false;
        this.mIsClick = false;
        this.mIsClickQS = false;
        this.mScrollMore = false;
        this.mOpenQSPanel = false;
        this.mChangeBrightness = false;
        this.mFistNotifAction = NotifAction.NONE;
        this.mFistNotifActionDuration = 0;
        this.mVisibleKeys.clear();
        this.mCreateTimeMillis = System.currentTimeMillis();
        this.mPanelSlidingTimes = 0;
    }

    public void start(String str, boolean z, int i, int i2) {
        if (this.mExpandEvent == null) {
            reset();
            ExpandEvent expandEvent = new ExpandEvent(str, (z ? ExpandMode.MANUAL : ExpandMode.COMMAND).name(), i, i2);
            this.mExpandEvent = expandEvent;
            this.mEventTracker.track(expandEvent);
        }
    }

    public void end(boolean z, int i) {
        if (this.mCollapseEvent == null) {
            String name = getCollapseMode(z).name();
            boolean z2 = this.mOpenQSPanel;
            boolean z3 = this.mIsClickQS;
            boolean z4 = this.mChangeBrightness;
            boolean z5 = this.mScrollMore;
            boolean z6 = this.mIsRemove;
            CollapseEvent collapseEvent = new CollapseEvent(name, i, z2 ? 1 : 0, z3 ? 1 : 0, z4 ? 1 : 0, z5 ? 1 : 0, z6 ? 1 : 0, System.currentTimeMillis() - this.mCreateTimeMillis, this.mFistNotifAction.name(), this.mFistNotifActionDuration, this.mVisibleKeys.size(), this.mPanelSlidingTimes);
            this.mCollapseEvent = collapseEvent;
            this.mEventTracker.track(collapseEvent);
            this.mExpandEvent = null;
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
        markFirstNotiAction(NotifAction.CLEAR_ALL);
    }

    public void markClick() {
        this.mIsClick = true;
        markFirstNotiAction(NotifAction.CLICK);
    }

    public void markClickQS() {
        this.mIsClickQS = true;
    }

    public void markOpenQSPanel() {
        this.mOpenQSPanel = true;
    }

    public void markChangeBrightness() {
        this.mChangeBrightness = true;
    }

    public void markSlidingTimes() {
        this.mPanelSlidingTimes++;
    }

    public int getPanelSlidingTimes() {
        return this.mPanelSlidingTimes;
    }

    private void markFirstNotiAction(NotifAction notifAction) {
        if (this.mFistNotifAction == NotifAction.NONE) {
            this.mFistNotifAction = notifAction;
            this.mFistNotifActionDuration = System.currentTimeMillis() - this.mFullyExpandedTimeMills;
        }
    }

    private CollapseMode getCollapseMode(boolean z) {
        if (this.mIsClick) {
            return CollapseMode.CLICK_NOTIFICATION;
        }
        if (this.mIsBackPressed) {
            return CollapseMode.BACK;
        }
        if (this.mIsHomePressed) {
            return CollapseMode.HOME;
        }
        if (this.mIsRemoveAll) {
            return CollapseMode.CLICK_CLEAR_ALL;
        }
        if (!z) {
            return CollapseMode.COMMAND;
        }
        return CollapseMode.OTHER;
    }
}

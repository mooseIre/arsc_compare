package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Region;
import android.util.Pools;
import androidx.collection.ArraySet;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0016R$integer;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.AlertingNotificationManager;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.phone.HeadsUpManagerPhone;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

public class HeadsUpManagerPhone extends HeadsUpManager implements Dumpable, VisualStabilityManager.Callback, OnHeadsUpChangedListener {
    private AnimationStateHandler mAnimationStateHandler;
    private final int mAutoHeadsUpNotificationDecay;
    private final KeyguardBypassController mBypassController;
    private HashSet<NotificationEntry> mEntriesToRemoveAfterExpand = new HashSet<>();
    private ArraySet<NotificationEntry> mEntriesToRemoveWhenReorderingAllowed = new ArraySet<>();
    private final Pools.Pool<HeadsUpEntryPhone> mEntryPool = new Pools.Pool<HeadsUpEntryPhone>() {
        /* class com.android.systemui.statusbar.phone.HeadsUpManagerPhone.AnonymousClass1 */
        private Stack<HeadsUpEntryPhone> mPoolObjects = new Stack<>();

        public HeadsUpEntryPhone acquire() {
            if (!this.mPoolObjects.isEmpty()) {
                return this.mPoolObjects.pop();
            }
            return new HeadsUpEntryPhone();
        }

        public boolean release(HeadsUpEntryPhone headsUpEntryPhone) {
            this.mPoolObjects.push(headsUpEntryPhone);
            return true;
        }
    };
    @VisibleForTesting
    final int mExtensionTime;
    private final NotificationGroupManager mGroupManager;
    private boolean mHeadsUpGoingAway;
    private int mHeadsUpInset;
    private final List<OnHeadsUpPhoneListenerChange> mHeadsUpPhoneListeners = new ArrayList();
    private boolean mIsExpanded;
    private HashSet<String> mKeysToRemoveWhenLeavingKeyguard = new HashSet<>();
    private boolean mReleaseOnExpandFinish;
    private int mStatusBarState;
    private final StatusBarStateController.StateListener mStatusBarStateListener = new StatusBarStateController.StateListener() {
        /* class com.android.systemui.statusbar.phone.HeadsUpManagerPhone.AnonymousClass3 */

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onStateChanged(int i) {
            boolean z = true;
            boolean z2 = HeadsUpManagerPhone.this.mStatusBarState == 1;
            if (i != 1) {
                z = false;
            }
            HeadsUpManagerPhone.this.mStatusBarState = i;
            if (z2 && !z && HeadsUpManagerPhone.this.mKeysToRemoveWhenLeavingKeyguard.size() != 0) {
                for (String str : (String[]) HeadsUpManagerPhone.this.mKeysToRemoveWhenLeavingKeyguard.toArray(new String[0])) {
                    HeadsUpManagerPhone.this.removeAlertEntry(str);
                }
                HeadsUpManagerPhone.this.mKeysToRemoveWhenLeavingKeyguard.clear();
            }
            if (z2 && !z && HeadsUpManagerPhone.this.mBypassController.getBypassEnabled()) {
                ArrayList arrayList = new ArrayList();
                for (AlertingNotificationManager.AlertEntry alertEntry : ((AlertingNotificationManager) HeadsUpManagerPhone.this).mAlertEntries.values()) {
                    NotificationEntry notificationEntry = alertEntry.mEntry;
                    if (notificationEntry != null && notificationEntry.isBubble() && !alertEntry.isSticky()) {
                        arrayList.add(alertEntry.mEntry.getKey());
                    }
                }
                Iterator it = arrayList.iterator();
                while (it.hasNext()) {
                    HeadsUpManagerPhone.this.removeAlertEntry((String) it.next());
                }
            }
        }

        @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
        public void onDozingChanged(boolean z) {
            if (!z) {
                for (AlertingNotificationManager.AlertEntry alertEntry : ((AlertingNotificationManager) HeadsUpManagerPhone.this).mAlertEntries.values()) {
                    alertEntry.updateEntry(true);
                }
            }
        }
    };
    private HashSet<String> mSwipedOutKeys = new HashSet<>();
    private final Region mTouchableRegion = new Region();
    private boolean mTrackingHeadsUp;
    private VisualStabilityManager mVisualStabilityManager;

    public interface AnimationStateHandler {
        void setHeadsUpGoingAwayAnimationsAllowed(boolean z);
    }

    public interface OnHeadsUpPhoneListenerChange {
        void onHeadsUpGoingAwayStateChanged(boolean z);
    }

    public HeadsUpManagerPhone(Context context, StatusBarStateController statusBarStateController, KeyguardBypassController keyguardBypassController, NotificationGroupManager notificationGroupManager, ConfigurationController configurationController) {
        super(context);
        Resources resources = this.mContext.getResources();
        this.mExtensionTime = resources.getInteger(C0016R$integer.ambient_notification_extension_time);
        this.mAutoHeadsUpNotificationDecay = resources.getInteger(C0016R$integer.auto_heads_up_notification_decay);
        statusBarStateController.addCallback(this.mStatusBarStateListener);
        this.mBypassController = keyguardBypassController;
        this.mGroupManager = notificationGroupManager;
        updateResources();
        configurationController.addCallback(new ConfigurationController.ConfigurationListener() {
            /* class com.android.systemui.statusbar.phone.HeadsUpManagerPhone.AnonymousClass2 */

            @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
            public void onDensityOrFontScaleChanged() {
                HeadsUpManagerPhone.this.updateResources();
            }

            @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
            public void onOverlayChanged() {
                HeadsUpManagerPhone.this.updateResources();
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void setup(VisualStabilityManager visualStabilityManager) {
        this.mVisualStabilityManager = visualStabilityManager;
    }

    public void setAnimationStateHandler(AnimationStateHandler animationStateHandler) {
        this.mAnimationStateHandler = animationStateHandler;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateResources() {
        Resources resources = this.mContext.getResources();
        this.mHeadsUpInset = resources.getDimensionPixelSize(17105489) + resources.getDimensionPixelSize(C0012R$dimen.heads_up_status_bar_padding);
    }

    /* access modifiers changed from: package-private */
    public void addHeadsUpPhoneListener(OnHeadsUpPhoneListenerChange onHeadsUpPhoneListenerChange) {
        this.mHeadsUpPhoneListeners.add(onHeadsUpPhoneListenerChange);
    }

    /* access modifiers changed from: package-private */
    public Region getTouchableRegion() {
        NotificationEntry groupSummary;
        NotificationEntry topEntry = getTopEntry();
        if (!hasPinnedHeadsUp() || topEntry == null) {
            return null;
        }
        if (topEntry.isChildInGroup() && (groupSummary = this.mGroupManager.getGroupSummary(topEntry.getSbn())) != null) {
            topEntry = groupSummary;
        }
        ExpandableNotificationRow row = topEntry.getRow();
        int[] iArr = new int[2];
        row.getLocationOnScreen(iArr);
        this.mTouchableRegion.set(iArr[0], 0, iArr[0] + row.getWidth(), this.mHeadsUpInset + row.getIntrinsicHeight());
        return this.mTouchableRegion;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldSwallowClick(String str) {
        HeadsUpManager.HeadsUpEntry headsUpEntry = getHeadsUpEntry(str);
        return headsUpEntry != null && this.mClock.currentTimeMillis() < headsUpEntry.mPostTime;
    }

    public void onExpandingFinished() {
        if (this.mReleaseOnExpandFinish) {
            releaseAllImmediately();
            this.mReleaseOnExpandFinish = false;
        } else {
            Iterator<NotificationEntry> it = this.mEntriesToRemoveAfterExpand.iterator();
            while (it.hasNext()) {
                NotificationEntry next = it.next();
                if (isAlerting(next.getKey())) {
                    removeAlertEntry(next.getKey());
                }
            }
        }
        this.mEntriesToRemoveAfterExpand.clear();
    }

    public void setTrackingHeadsUp(boolean z) {
        this.mTrackingHeadsUp = z;
    }

    /* access modifiers changed from: package-private */
    public void setIsPanelExpanded(boolean z) {
        if (z != this.mIsExpanded) {
            this.mIsExpanded = z;
            if (z) {
                this.mHeadsUpGoingAway = false;
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager
    public boolean isEntryAutoHeadsUpped(String str) {
        HeadsUpEntryPhone headsUpEntryPhone = getHeadsUpEntryPhone(str);
        if (headsUpEntryPhone == null) {
            return false;
        }
        return headsUpEntryPhone.isAutoHeadsUp();
    }

    /* access modifiers changed from: package-private */
    public void setHeadsUpGoingAway(boolean z) {
        if (z != this.mHeadsUpGoingAway) {
            this.mHeadsUpGoingAway = z;
            for (OnHeadsUpPhoneListenerChange onHeadsUpPhoneListenerChange : this.mHeadsUpPhoneListeners) {
                onHeadsUpPhoneListenerChange.onHeadsUpGoingAwayStateChanged(z);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isHeadsUpGoingAway() {
        return this.mHeadsUpGoingAway;
    }

    public void setRemoteInputActive(NotificationEntry notificationEntry, boolean z) {
        HeadsUpEntryPhone headsUpEntryPhone = getHeadsUpEntryPhone(notificationEntry.getKey());
        if (headsUpEntryPhone != null && headsUpEntryPhone.remoteInputActive != z) {
            headsUpEntryPhone.remoteInputActive = z;
            if (z) {
                headsUpEntryPhone.removeAutoRemovalCallbacks();
            } else {
                headsUpEntryPhone.updateEntry(false);
            }
        }
    }

    public void setMenuShown(NotificationEntry notificationEntry, boolean z) {
        HeadsUpManager.HeadsUpEntry headsUpEntry = getHeadsUpEntry(notificationEntry.getKey());
        if ((headsUpEntry instanceof HeadsUpEntryPhone) && notificationEntry.isRowPinned()) {
            ((HeadsUpEntryPhone) headsUpEntry).setMenuShownPinned(z);
        }
    }

    public void extendHeadsUp() {
        HeadsUpEntryPhone topHeadsUpEntryPhone = getTopHeadsUpEntryPhone();
        if (topHeadsUpEntryPhone != null) {
            topHeadsUpEntryPhone.extendPulse();
        }
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager
    public boolean isTrackingHeadsUp() {
        return this.mTrackingHeadsUp;
    }

    @Override // com.android.systemui.statusbar.policy.HeadsUpManager
    public void snooze() {
        super.snooze();
        this.mReleaseOnExpandFinish = true;
    }

    public void addSwipedOutNotification(String str) {
        this.mSwipedOutKeys.add(str);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("HeadsUpManagerPhone state:");
        dumpInternal(fileDescriptor, printWriter, strArr);
    }

    @Override // com.android.systemui.statusbar.AlertingNotificationManager, com.android.systemui.statusbar.NotificationLifetimeExtender
    public boolean shouldExtendLifetime(NotificationEntry notificationEntry) {
        return this.mVisualStabilityManager.isReorderingAllowed() && super.shouldExtendLifetime(notificationEntry);
    }

    @Override // com.android.systemui.statusbar.notification.VisualStabilityManager.Callback
    public void onChangeAllowed() {
        this.mAnimationStateHandler.setHeadsUpGoingAwayAnimationsAllowed(false);
        Iterator<NotificationEntry> it = this.mEntriesToRemoveWhenReorderingAllowed.iterator();
        while (it.hasNext()) {
            NotificationEntry next = it.next();
            if (isAlerting(next.getKey())) {
                removeAlertEntry(next.getKey());
            }
        }
        this.mEntriesToRemoveWhenReorderingAllowed.clear();
        this.mAnimationStateHandler.setHeadsUpGoingAwayAnimationsAllowed(true);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.AlertingNotificationManager, com.android.systemui.statusbar.policy.HeadsUpManager, com.android.systemui.statusbar.policy.HeadsUpManager
    public HeadsUpManager.HeadsUpEntry createAlertEntry() {
        return (HeadsUpManager.HeadsUpEntry) this.mEntryPool.acquire();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.AlertingNotificationManager, com.android.systemui.statusbar.policy.HeadsUpManager
    public void onAlertEntryRemoved(AlertingNotificationManager.AlertEntry alertEntry) {
        this.mKeysToRemoveWhenLeavingKeyguard.remove(alertEntry.mEntry.getKey());
        super.onAlertEntryRemoved(alertEntry);
        this.mEntryPool.release((HeadsUpEntryPhone) alertEntry);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.policy.HeadsUpManager
    public boolean shouldHeadsUpBecomePinned(NotificationEntry notificationEntry) {
        boolean z = this.mStatusBarState == 0 && !this.mIsExpanded;
        if (this.mBypassController.getBypassEnabled()) {
            z |= this.mStatusBarState == 1;
        }
        return z || super.shouldHeadsUpBecomePinned(notificationEntry);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.policy.HeadsUpManager
    public void dumpInternal(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dumpInternal(fileDescriptor, printWriter, strArr);
        printWriter.print("  mBarState=");
        printWriter.println(this.mStatusBarState);
        printWriter.print("  mTouchableRegion=");
        printWriter.println(this.mTouchableRegion);
    }

    private HeadsUpEntryPhone getHeadsUpEntryPhone(String str) {
        return (HeadsUpEntryPhone) this.mAlertEntries.get(str);
    }

    private HeadsUpEntryPhone getTopHeadsUpEntryPhone() {
        return (HeadsUpEntryPhone) getTopHeadsUpEntry();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.AlertingNotificationManager
    public boolean canRemoveImmediately(String str) {
        if (this.mSwipedOutKeys.contains(str)) {
            this.mSwipedOutKeys.remove(str);
            return true;
        }
        HeadsUpEntryPhone headsUpEntryPhone = getHeadsUpEntryPhone(str);
        HeadsUpEntryPhone topHeadsUpEntryPhone = getTopHeadsUpEntryPhone();
        if (headsUpEntryPhone == null || headsUpEntryPhone != topHeadsUpEntryPhone || super.canRemoveImmediately(str)) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public class HeadsUpEntryPhone extends HeadsUpManager.HeadsUpEntry {
        private boolean extended;
        private boolean mIsAutoHeadsUp;
        private boolean mMenuShownPinned;

        protected HeadsUpEntryPhone() {
            super();
        }

        @Override // com.android.systemui.statusbar.AlertingNotificationManager.AlertEntry, com.android.systemui.statusbar.policy.HeadsUpManager.HeadsUpEntry
        public boolean isSticky() {
            return super.isSticky() || this.mMenuShownPinned;
        }

        @Override // com.android.systemui.statusbar.AlertingNotificationManager.AlertEntry
        public void setEntry(NotificationEntry notificationEntry) {
            setEntry(notificationEntry, new Runnable(notificationEntry) {
                /* class com.android.systemui.statusbar.phone.$$Lambda$HeadsUpManagerPhone$HeadsUpEntryPhone$adyrhF30JE9Yr0JaVKYkiAV0Clw */
                public final /* synthetic */ NotificationEntry f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    HeadsUpManagerPhone.HeadsUpEntryPhone.this.lambda$setEntry$0$HeadsUpManagerPhone$HeadsUpEntryPhone(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$setEntry$0 */
        public /* synthetic */ void lambda$setEntry$0$HeadsUpManagerPhone$HeadsUpEntryPhone(NotificationEntry notificationEntry) {
            if (!HeadsUpManagerPhone.this.mVisualStabilityManager.isReorderingAllowed() && !notificationEntry.showingPulsing()) {
                HeadsUpManagerPhone.this.mEntriesToRemoveWhenReorderingAllowed.add(notificationEntry);
                HeadsUpManagerPhone.this.mVisualStabilityManager.addReorderingAllowedCallback(HeadsUpManagerPhone.this, false);
            } else if (HeadsUpManagerPhone.this.mTrackingHeadsUp) {
                HeadsUpManagerPhone.this.mEntriesToRemoveAfterExpand.add(notificationEntry);
            } else if (!this.mIsAutoHeadsUp || HeadsUpManagerPhone.this.mStatusBarState != 1) {
                HeadsUpManagerPhone.this.removeAlertEntry(notificationEntry.getKey());
            } else {
                HeadsUpManagerPhone.this.mKeysToRemoveWhenLeavingKeyguard.add(notificationEntry.getKey());
            }
            ((NotificationStat) Dependency.get(NotificationStat.class)).onFloatAutoCollapse(notificationEntry);
        }

        @Override // com.android.systemui.statusbar.AlertingNotificationManager.AlertEntry
        public void updateEntry(boolean z) {
            this.mIsAutoHeadsUp = this.mEntry.isAutoHeadsUp();
            super.updateEntry(z);
            if (HeadsUpManagerPhone.this.mEntriesToRemoveAfterExpand.contains(this.mEntry)) {
                HeadsUpManagerPhone.this.mEntriesToRemoveAfterExpand.remove(this.mEntry);
            }
            if (HeadsUpManagerPhone.this.mEntriesToRemoveWhenReorderingAllowed.contains(this.mEntry)) {
                HeadsUpManagerPhone.this.mEntriesToRemoveWhenReorderingAllowed.remove(this.mEntry);
            }
            HeadsUpManagerPhone.this.mKeysToRemoveWhenLeavingKeyguard.remove(this.mEntry.getKey());
        }

        @Override // com.android.systemui.statusbar.policy.HeadsUpManager.HeadsUpEntry
        public void setExpanded(boolean z) {
            if (this.expanded != z) {
                this.expanded = z;
                if (z) {
                    removeAutoRemovalCallbacks();
                } else {
                    updateEntry(false);
                }
            }
        }

        public void setMenuShownPinned(boolean z) {
            if (this.mMenuShownPinned != z) {
                this.mMenuShownPinned = z;
                if (z) {
                    removeAutoRemovalCallbacks();
                } else {
                    updateEntry(false);
                }
            }
        }

        @Override // com.android.systemui.statusbar.AlertingNotificationManager.AlertEntry, com.android.systemui.statusbar.policy.HeadsUpManager.HeadsUpEntry
        public void reset() {
            super.reset();
            this.mMenuShownPinned = false;
            this.extended = false;
            this.mIsAutoHeadsUp = false;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void extendPulse() {
            if (!this.extended) {
                this.extended = true;
                updateEntry(false);
            }
        }

        @Override // com.android.systemui.statusbar.AlertingNotificationManager.AlertEntry, com.android.systemui.statusbar.policy.HeadsUpManager.HeadsUpEntry
        public int compareTo(AlertingNotificationManager.AlertEntry alertEntry) {
            boolean isAutoHeadsUp = isAutoHeadsUp();
            boolean isAutoHeadsUp2 = ((HeadsUpEntryPhone) alertEntry).isAutoHeadsUp();
            if (isAutoHeadsUp && !isAutoHeadsUp2) {
                return 1;
            }
            if (isAutoHeadsUp || !isAutoHeadsUp2) {
                return super.compareTo(alertEntry);
            }
            return -1;
        }

        /* access modifiers changed from: protected */
        @Override // com.android.systemui.statusbar.AlertingNotificationManager.AlertEntry, com.android.systemui.statusbar.policy.HeadsUpManager.HeadsUpEntry
        public long calculateFinishTime() {
            return this.mPostTime + ((long) getDecayDuration()) + ((long) (this.extended ? HeadsUpManagerPhone.this.mExtensionTime : 0));
        }

        private int getDecayDuration() {
            if (isAutoHeadsUp()) {
                return getRecommendedHeadsUpTimeoutMs(HeadsUpManagerPhone.this.mAutoHeadsUpNotificationDecay);
            }
            return getRecommendedHeadsUpTimeoutMs(((AlertingNotificationManager) HeadsUpManagerPhone.this).mAutoDismissNotificationDecay);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private boolean isAutoHeadsUp() {
            return this.mIsAutoHeadsUp;
        }
    }
}

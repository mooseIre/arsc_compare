package com.android.systemui.statusbar.notification.logging;

import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.analytics.NotificationStat;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class NotificationLogger implements StatusBarStateController.StateListener {
    protected IStatusBarService mBarService;
    private final ArraySet<NotificationVisibility> mCurrentlyVisibleNotifications = new ArraySet<>();
    @GuardedBy({"mDozingLock"})
    private Boolean mDozing = null;
    private final Object mDozingLock = new Object();
    private final NotificationEntryManager mEntryManager;
    private final ExpansionStateLogger mExpansionStateLogger;
    private boolean mFloating = false;
    protected Handler mHandler = new Handler();
    private HeadsUpManager mHeadsUpManager;
    private boolean mKeyguard = false;
    private long mLastVisibilityReportUptimeMs;
    private NotificationListContainer mListContainer;
    @GuardedBy({"mDozingLock"})
    private Boolean mLockscreen = null;
    private boolean mLogging = false;
    private final NotificationListenerService mNotificationListener;
    protected final OnChildLocationsChangedListener mNotificationLocationsChangedListener = new OnChildLocationsChangedListener() {
        /* class com.android.systemui.statusbar.notification.logging.NotificationLogger.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.notification.logging.NotificationLogger.OnChildLocationsChangedListener
        public void onChildLocationsChanged() {
            NotificationLogger notificationLogger = NotificationLogger.this;
            if (!notificationLogger.mHandler.hasCallbacks(notificationLogger.mVisibilityReporter)) {
                NotificationLogger notificationLogger2 = NotificationLogger.this;
                notificationLogger2.mHandler.postAtTime(notificationLogger2.mVisibilityReporter, NotificationLogger.this.mLastVisibilityReportUptimeMs + 500);
            }
        }
    };
    private final NotificationPanelLogger mNotificationPanelLogger;
    private Boolean mPanelExpanded = null;
    private final Executor mUiBgExecutor;
    protected Runnable mVisibilityReporter = new Runnable() {
        /* class com.android.systemui.statusbar.notification.logging.NotificationLogger.AnonymousClass2 */
        private final ArraySet<NotificationVisibility> mTmpCurrentlyVisibleNotifications = new ArraySet<>();
        private final ArraySet<NotificationVisibility> mTmpNewlyVisibleNotifications = new ArraySet<>();
        private final ArraySet<NotificationVisibility> mTmpNoLongerVisibleNotifications = new ArraySet<>();

        public void run() {
            NotificationLogger.this.mLastVisibilityReportUptimeMs = SystemClock.uptimeMillis();
            List<NotificationEntry> visibleNotifications = NotificationLogger.this.mEntryManager.getVisibleNotifications();
            int size = visibleNotifications.size();
            for (int i = 0; i < size; i++) {
                NotificationEntry notificationEntry = visibleNotifications.get(i);
                String key = notificationEntry.getSbn().getKey();
                boolean isInVisibleLocation = NotificationLogger.this.mListContainer.isInVisibleLocation(notificationEntry);
                NotificationVisibility obtain = NotificationVisibility.obtain(key, i, size, isInVisibleLocation, NotificationLogger.getNotificationLocation(notificationEntry));
                boolean contains = NotificationLogger.this.mCurrentlyVisibleNotifications.contains(obtain);
                if (isInVisibleLocation) {
                    this.mTmpCurrentlyVisibleNotifications.add(obtain);
                    if (!contains) {
                        notificationEntry.getSbn().seeTime = System.currentTimeMillis();
                        this.mTmpNewlyVisibleNotifications.add(obtain);
                    }
                } else {
                    obtain.recycle();
                }
            }
            this.mTmpNoLongerVisibleNotifications.addAll(NotificationLogger.this.mCurrentlyVisibleNotifications);
            this.mTmpNoLongerVisibleNotifications.removeAll((ArraySet<? extends NotificationVisibility>) this.mTmpCurrentlyVisibleNotifications);
            NotificationLogger.this.logNotificationVisibilityChanges(this.mTmpNewlyVisibleNotifications, this.mTmpNoLongerVisibleNotifications);
            NotificationLogger notificationLogger = NotificationLogger.this;
            notificationLogger.recycleAllVisibilityObjects((NotificationLogger) notificationLogger.mCurrentlyVisibleNotifications);
            NotificationLogger.this.mCurrentlyVisibleNotifications.addAll((ArraySet) this.mTmpCurrentlyVisibleNotifications);
            ExpansionStateLogger expansionStateLogger = NotificationLogger.this.mExpansionStateLogger;
            ArraySet<NotificationVisibility> arraySet = this.mTmpCurrentlyVisibleNotifications;
            expansionStateLogger.onVisibilityChanged(arraySet, arraySet);
            NotificationLogger.this.recycleAllVisibilityObjects((NotificationLogger) this.mTmpNoLongerVisibleNotifications);
            this.mTmpCurrentlyVisibleNotifications.clear();
            this.mTmpNewlyVisibleNotifications.clear();
            this.mTmpNoLongerVisibleNotifications.clear();
        }
    };

    public interface OnChildLocationsChangedListener {
        void onChildLocationsChanged();
    }

    public static NotificationVisibility.NotificationLocation getNotificationLocation(NotificationEntry notificationEntry) {
        if (notificationEntry == null || notificationEntry.getRow() == null || notificationEntry.getRow().getViewState() == null) {
            return NotificationVisibility.NotificationLocation.LOCATION_UNKNOWN;
        }
        return convertNotificationLocation(notificationEntry.getRow().getViewState().location);
    }

    private static NotificationVisibility.NotificationLocation convertNotificationLocation(int i) {
        if (i == 1) {
            return NotificationVisibility.NotificationLocation.LOCATION_FIRST_HEADS_UP;
        }
        if (i == 2) {
            return NotificationVisibility.NotificationLocation.LOCATION_HIDDEN_TOP;
        }
        if (i == 4) {
            return NotificationVisibility.NotificationLocation.LOCATION_MAIN_AREA;
        }
        if (i == 8) {
            return NotificationVisibility.NotificationLocation.LOCATION_BOTTOM_STACK_PEEKING;
        }
        if (i == 16) {
            return NotificationVisibility.NotificationLocation.LOCATION_BOTTOM_STACK_HIDDEN;
        }
        if (i != 64) {
            return NotificationVisibility.NotificationLocation.LOCATION_UNKNOWN;
        }
        return NotificationVisibility.NotificationLocation.LOCATION_GONE;
    }

    public NotificationLogger(NotificationListener notificationListener, Executor executor, NotificationEntryManager notificationEntryManager, StatusBarStateController statusBarStateController, ExpansionStateLogger expansionStateLogger, NotificationPanelLogger notificationPanelLogger) {
        this.mNotificationListener = notificationListener;
        this.mUiBgExecutor = executor;
        this.mEntryManager = notificationEntryManager;
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mExpansionStateLogger = expansionStateLogger;
        this.mNotificationPanelLogger = notificationPanelLogger;
        statusBarStateController.addCallback(this);
        notificationEntryManager.addNotificationEntryListener(new NotificationEntryListener() {
            /* class com.android.systemui.statusbar.notification.logging.NotificationLogger.AnonymousClass3 */

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
                if (z && notificationVisibility != null) {
                    NotificationLogger.this.logNotificationClear(notificationEntry.getKey(), notificationEntry.getSbn(), notificationVisibility);
                    ((NotificationStat) Dependency.get(NotificationStat.class)).onRemove(notificationEntry);
                }
                NotificationLogger.this.mExpansionStateLogger.onEntryRemoved(notificationEntry.getKey());
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPreEntryUpdated(NotificationEntry notificationEntry) {
                NotificationLogger.this.mExpansionStateLogger.onEntryUpdated(notificationEntry.getKey());
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onInflationError(StatusBarNotification statusBarNotification, Exception exc) {
                NotificationLogger.this.logNotificationError(statusBarNotification, exc);
            }
        });
    }

    public void setUpWithContainer(NotificationListContainer notificationListContainer) {
        this.mListContainer = notificationListContainer;
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public void stopNotificationLogging() {
        if (this.mLogging) {
            this.mLogging = false;
            this.mKeyguard = false;
            this.mFloating = false;
            if (!this.mCurrentlyVisibleNotifications.isEmpty()) {
                logNotificationVisibilityChanges(Collections.emptyList(), this.mCurrentlyVisibleNotifications);
                recycleAllVisibilityObjects(this.mCurrentlyVisibleNotifications);
            }
            this.mHandler.removeCallbacks(this.mVisibilityReporter);
            this.mListContainer.setChildLocationsChangedListener(null);
        }
    }

    public void startNotificationLogging() {
        if (!this.mLogging) {
            this.mLogging = true;
            Boolean bool = this.mLockscreen;
            this.mKeyguard = bool == null ? false : bool.booleanValue();
            this.mFloating = this.mHeadsUpManager.hasPinnedHeadsUp();
            this.mListContainer.setChildLocationsChangedListener(this.mNotificationLocationsChangedListener);
            this.mNotificationLocationsChangedListener.onChildLocationsChanged();
        }
    }

    private void setDozing(boolean z) {
        synchronized (this.mDozingLock) {
            this.mDozing = Boolean.valueOf(z);
            maybeUpdateLoggingStatus();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logNotificationClear(String str, StatusBarNotification statusBarNotification, NotificationVisibility notificationVisibility) {
        int i;
        int i2;
        String packageName = statusBarNotification.getPackageName();
        if (!str.contains(packageName)) {
            packageName = statusBarNotification.getOpPkg();
        }
        String tag = statusBarNotification.getTag();
        int id = statusBarNotification.getId();
        int userId = statusBarNotification.getUserId();
        try {
            if (this.mHeadsUpManager.isAlerting(str)) {
                i2 = 1;
            } else if (this.mListContainer.hasPulsingNotifications()) {
                i2 = 2;
            } else {
                i = 3;
                this.mBarService.onNotificationClear(packageName, tag, id, userId, statusBarNotification.getKey(), i, 1, notificationVisibility);
            }
            i = i2;
            this.mBarService.onNotificationClear(packageName, tag, id, userId, statusBarNotification.getKey(), i, 1, notificationVisibility);
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logNotificationError(StatusBarNotification statusBarNotification, Exception exc) {
        try {
            this.mBarService.onNotificationError(statusBarNotification.getPackageName(), statusBarNotification.getTag(), statusBarNotification.getId(), statusBarNotification.getUid(), statusBarNotification.getInitialPid(), exc.getMessage(), statusBarNotification.getUserId());
        } catch (RemoteException unused) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void logNotificationVisibilityChanges(Collection<NotificationVisibility> collection, Collection<NotificationVisibility> collection2) {
        if (!collection.isEmpty() || !collection2.isEmpty()) {
            NotificationVisibility[] cloneVisibilitiesAsArr = cloneVisibilitiesAsArr(collection);
            NotificationVisibility[] cloneVisibilitiesAsArr2 = cloneVisibilitiesAsArr(collection2);
            ((NotificationStat) Dependency.get(NotificationStat.class)).logVisibilityChanges(cloneVisibilitiesAsKeyList(collection), cloneVisibilitiesAsKeyList(collection2), this.mFloating, this.mKeyguard);
            this.mUiBgExecutor.execute(new Runnable(cloneVisibilitiesAsArr, cloneVisibilitiesAsArr2) {
                /* class com.android.systemui.statusbar.notification.logging.$$Lambda$NotificationLogger$e3uKrBablkegG4HWqs1WzubMAs */
                public final /* synthetic */ NotificationVisibility[] f$1;
                public final /* synthetic */ NotificationVisibility[] f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    NotificationLogger.this.lambda$logNotificationVisibilityChanges$0$NotificationLogger(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$logNotificationVisibilityChanges$0 */
    public /* synthetic */ void lambda$logNotificationVisibilityChanges$0$NotificationLogger(NotificationVisibility[] notificationVisibilityArr, NotificationVisibility[] notificationVisibilityArr2) {
        try {
            this.mBarService.onNotificationVisibilityChanged(notificationVisibilityArr, notificationVisibilityArr2);
        } catch (RemoteException unused) {
        }
        int length = notificationVisibilityArr.length;
        if (length > 0) {
            String[] strArr = new String[length];
            for (int i = 0; i < length; i++) {
                strArr[i] = notificationVisibilityArr[i].key;
            }
            try {
                this.mNotificationListener.setNotificationsShown(strArr);
            } catch (RuntimeException e) {
                Log.d("NotificationLogger", "failed setNotificationsShown: ", e);
            }
        }
        recycleAllVisibilityObjects(notificationVisibilityArr);
        recycleAllVisibilityObjects(notificationVisibilityArr2);
    }

    private List<String> cloneVisibilitiesAsKeyList(Collection<NotificationVisibility> collection) {
        return (List) collection.stream().map($$Lambda$NotificationLogger$JO0enstrXhdro9FeK3TlyP_3L6E.INSTANCE).collect(Collectors.toList());
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recycleAllVisibilityObjects(ArraySet<NotificationVisibility> arraySet) {
        int size = arraySet.size();
        for (int i = 0; i < size; i++) {
            arraySet.valueAt(i).recycle();
        }
        arraySet.clear();
    }

    private void recycleAllVisibilityObjects(NotificationVisibility[] notificationVisibilityArr) {
        int length = notificationVisibilityArr.length;
        for (int i = 0; i < length; i++) {
            if (notificationVisibilityArr[i] != null) {
                notificationVisibilityArr[i].recycle();
            }
        }
    }

    /* access modifiers changed from: private */
    public static NotificationVisibility[] cloneVisibilitiesAsArr(Collection<NotificationVisibility> collection) {
        NotificationVisibility[] notificationVisibilityArr = new NotificationVisibility[collection.size()];
        int i = 0;
        for (NotificationVisibility notificationVisibility : collection) {
            if (notificationVisibility != null) {
                notificationVisibilityArr[i] = notificationVisibility.clone();
            }
            i++;
        }
        return notificationVisibilityArr;
    }

    @VisibleForTesting
    public Runnable getVisibilityReporter() {
        return this.mVisibilityReporter;
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onStateChanged(int i) {
        synchronized (this.mDozingLock) {
            boolean z = true;
            if (!(i == 1 || i == 2)) {
                z = false;
            }
            this.mLockscreen = Boolean.valueOf(z);
        }
    }

    @Override // com.android.systemui.plugins.statusbar.StatusBarStateController.StateListener
    public void onDozingChanged(boolean z) {
        setDozing(z);
    }

    @GuardedBy({"mDozingLock"})
    private void maybeUpdateLoggingStatus() {
        if (this.mPanelExpanded != null && this.mDozing != null) {
            Boolean bool = this.mLockscreen;
            boolean booleanValue = bool == null ? false : bool.booleanValue();
            if (!this.mPanelExpanded.booleanValue() || this.mDozing.booleanValue()) {
                stopNotificationLogging();
                return;
            }
            this.mNotificationPanelLogger.logPanelShown(booleanValue, this.mEntryManager.getVisibleNotifications());
            startNotificationLogging();
        }
    }

    public void onPanelExpandedChanged(boolean z) {
        this.mPanelExpanded = Boolean.valueOf(z);
        synchronized (this.mDozingLock) {
            maybeUpdateLoggingStatus();
        }
    }

    public void onExpansionChanged(String str, boolean z, boolean z2) {
        this.mExpansionStateLogger.onExpansionChanged(str, z, z2, getNotificationLocation(this.mEntryManager.getActiveNotificationUnfiltered(str)));
        ((NotificationStat) Dependency.get(NotificationStat.class)).onExpansionChanged(str, z, z2);
    }

    @VisibleForTesting
    public void setVisibilityReporter(Runnable runnable) {
        this.mVisibilityReporter = runnable;
    }

    public static class ExpansionStateLogger {
        @VisibleForTesting
        IStatusBarService mBarService;
        private final Map<String, State> mExpansionStates = new ArrayMap();
        private final Map<String, Boolean> mLoggedExpansionState = new ArrayMap();
        private final Executor mUiBgExecutor;

        public ExpansionStateLogger(Executor executor) {
            this.mUiBgExecutor = executor;
            this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public void onExpansionChanged(String str, boolean z, boolean z2, NotificationVisibility.NotificationLocation notificationLocation) {
            State state = getState(str);
            state.mIsUserAction = Boolean.valueOf(z);
            state.mIsExpanded = Boolean.valueOf(z2);
            state.mLocation = notificationLocation;
            maybeNotifyOnNotificationExpansionChanged(str, state);
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public void onVisibilityChanged(Collection<NotificationVisibility> collection, Collection<NotificationVisibility> collection2) {
            NotificationVisibility[] cloneVisibilitiesAsArr = NotificationLogger.cloneVisibilitiesAsArr(collection);
            NotificationVisibility[] cloneVisibilitiesAsArr2 = NotificationLogger.cloneVisibilitiesAsArr(collection2);
            for (NotificationVisibility notificationVisibility : cloneVisibilitiesAsArr) {
                State state = getState(notificationVisibility.key);
                state.mIsVisible = Boolean.TRUE;
                state.mLocation = notificationVisibility.location;
                maybeNotifyOnNotificationExpansionChanged(notificationVisibility.key, state);
            }
            for (NotificationVisibility notificationVisibility2 : cloneVisibilitiesAsArr2) {
                getState(notificationVisibility2.key).mIsVisible = Boolean.FALSE;
            }
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public void onEntryRemoved(String str) {
            this.mExpansionStates.remove(str);
            this.mLoggedExpansionState.remove(str);
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public void onEntryUpdated(String str) {
            this.mLoggedExpansionState.remove(str);
        }

        private State getState(String str) {
            State state = this.mExpansionStates.get(str);
            if (state != null) {
                return state;
            }
            State state2 = new State();
            this.mExpansionStates.put(str, state2);
            return state2;
        }

        private void maybeNotifyOnNotificationExpansionChanged(String str, State state) {
            if (state.isFullySet() && state.mIsVisible.booleanValue()) {
                Boolean bool = this.mLoggedExpansionState.get(str);
                if (bool == null && !state.mIsExpanded.booleanValue()) {
                    return;
                }
                if (bool == null || state.mIsExpanded != bool) {
                    this.mLoggedExpansionState.put(str, state.mIsExpanded);
                    this.mUiBgExecutor.execute(new Runnable(str, new State(state)) {
                        /* class com.android.systemui.statusbar.notification.logging.$$Lambda$NotificationLogger$ExpansionStateLogger$2Eiyi73G6QB8CNmBwaixENnG5Co */
                        public final /* synthetic */ String f$1;
                        public final /* synthetic */ NotificationLogger.ExpansionStateLogger.State f$2;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                        }

                        public final void run() {
                            NotificationLogger.ExpansionStateLogger.this.lambda$maybeNotifyOnNotificationExpansionChanged$0$NotificationLogger$ExpansionStateLogger(this.f$1, this.f$2);
                        }
                    });
                }
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$maybeNotifyOnNotificationExpansionChanged$0 */
        public /* synthetic */ void lambda$maybeNotifyOnNotificationExpansionChanged$0$NotificationLogger$ExpansionStateLogger(String str, State state) {
            try {
                this.mBarService.onNotificationExpansionChanged(str, state.mIsUserAction.booleanValue(), state.mIsExpanded.booleanValue(), state.mLocation.ordinal());
            } catch (RemoteException e) {
                Log.e("NotificationLogger", "Failed to call onNotificationExpansionChanged: ", e);
            }
        }

        /* access modifiers changed from: private */
        public static class State {
            Boolean mIsExpanded;
            Boolean mIsUserAction;
            Boolean mIsVisible;
            NotificationVisibility.NotificationLocation mLocation;

            private State() {
            }

            private State(State state) {
                this.mIsUserAction = state.mIsUserAction;
                this.mIsExpanded = state.mIsExpanded;
                this.mIsVisible = state.mIsVisible;
                this.mLocation = state.mLocation;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private boolean isFullySet() {
                return (this.mIsUserAction == null || this.mIsExpanded == null || this.mIsVisible == null || this.mLocation == null) ? false : true;
            }
        }
    }
}

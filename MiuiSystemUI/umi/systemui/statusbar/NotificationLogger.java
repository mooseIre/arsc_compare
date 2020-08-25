package com.android.systemui.statusbar;

import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.statusbar.NotificationVisibilityCompat;
import com.android.systemui.Dependency;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.miui.statusbar.analytics.NotificationStat;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NotificationLogger {
    protected IStatusBarService mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
    /* access modifiers changed from: private */
    public final ArraySet<NotificationVisibility> mCurrentlyVisibleNotifications = new ArraySet<>();
    private boolean mFloating = false;
    protected Handler mHandler = new Handler();
    private boolean mKeyguard = false;
    /* access modifiers changed from: private */
    public long mLastVisibilityReportUptimeMs;
    protected NotificationData mNotificationData;
    protected final OnChildLocationsChangedListener mNotificationLocationsChangedListener = new OnChildLocationsChangedListener() {
        public void onChildLocationsChanged() {
            NotificationLogger notificationLogger = NotificationLogger.this;
            if (!notificationLogger.mHandler.hasCallbacks(notificationLogger.mVisibilityReporter)) {
                NotificationLogger notificationLogger2 = NotificationLogger.this;
                notificationLogger2.mHandler.postAtTime(notificationLogger2.mVisibilityReporter, NotificationLogger.this.mLastVisibilityReportUptimeMs + 500);
            }
        }
    };
    /* access modifiers changed from: private */
    public NotificationStackScrollLayout mStackScroller;
    /* access modifiers changed from: private */
    public StatusBar mStatusBar;
    private final UiOffloadThread mUiOffloadThread = ((UiOffloadThread) Dependency.get(UiOffloadThread.class));
    protected final Runnable mVisibilityReporter = new Runnable() {
        private final ArraySet<NotificationVisibility> mTmpCurrentlyVisibleNotifications = new ArraySet<>();
        private final ArraySet<NotificationVisibility> mTmpNewlyVisibleNotifications = new ArraySet<>();
        private final ArraySet<NotificationVisibility> mTmpNoLongerVisibleNotifications = new ArraySet<>();

        public void run() {
            long unused = NotificationLogger.this.mLastVisibilityReportUptimeMs = SystemClock.uptimeMillis();
            ArrayList<NotificationData.Entry> activeNotifications = NotificationLogger.this.mNotificationData.getActiveNotifications();
            int size = activeNotifications.size();
            for (int i = 0; i < size; i++) {
                NotificationData.Entry entry = activeNotifications.get(i);
                String key = entry.notification.getKey();
                boolean isInVisibleLocation = NotificationLogger.this.mStackScroller.isInVisibleLocation(entry.row);
                NotificationVisibility obtain = NotificationVisibilityCompat.obtain(key, i, size, isInVisibleLocation);
                boolean contains = NotificationLogger.this.mCurrentlyVisibleNotifications.contains(obtain);
                if (isInVisibleLocation) {
                    this.mTmpCurrentlyVisibleNotifications.add(obtain);
                    if (!contains) {
                        entry.seeTime = System.currentTimeMillis();
                        this.mTmpNewlyVisibleNotifications.add(obtain);
                    }
                } else {
                    obtain.recycle();
                }
            }
            this.mTmpNoLongerVisibleNotifications.addAll(NotificationLogger.this.mCurrentlyVisibleNotifications);
            this.mTmpNoLongerVisibleNotifications.removeAll(this.mTmpCurrentlyVisibleNotifications);
            NotificationLogger.this.logNotificationVisibilityChanges(this.mTmpNewlyVisibleNotifications, this.mTmpNoLongerVisibleNotifications);
            NotificationLogger notificationLogger = NotificationLogger.this;
            notificationLogger.recycleAllVisibilityObjects((ArraySet<NotificationVisibility>) notificationLogger.mCurrentlyVisibleNotifications);
            NotificationLogger.this.mCurrentlyVisibleNotifications.addAll(this.mTmpCurrentlyVisibleNotifications);
            NotificationLogger.this.recycleAllVisibilityObjects(this.mTmpNoLongerVisibleNotifications);
            this.mTmpCurrentlyVisibleNotifications.clear();
            this.mTmpNewlyVisibleNotifications.clear();
            this.mTmpNoLongerVisibleNotifications.clear();
        }
    };

    public interface OnChildLocationsChangedListener {
        void onChildLocationsChanged();
    }

    public void setUp(StatusBar statusBar, NotificationData notificationData, NotificationStackScrollLayout notificationStackScrollLayout) {
        this.mStatusBar = statusBar;
        this.mNotificationData = notificationData;
        this.mStackScroller = notificationStackScrollLayout;
    }

    public void stopNotificationLogging() {
        if (!this.mCurrentlyVisibleNotifications.isEmpty()) {
            logNotificationVisibilityChanges(Collections.emptyList(), this.mCurrentlyVisibleNotifications);
            recycleAllVisibilityObjects(this.mCurrentlyVisibleNotifications);
        }
        this.mHandler.removeCallbacks(this.mVisibilityReporter);
        this.mStackScroller.setChildLocationsChangedListener((OnChildLocationsChangedListener) null);
    }

    public void startNotificationLogging() {
        this.mKeyguard = this.mStatusBar.isKeyguardShowing();
        this.mFloating = this.mStatusBar.isHeadsUpPinned();
        this.mStackScroller.setChildLocationsChangedListener(this.mNotificationLocationsChangedListener);
        this.mNotificationLocationsChangedListener.onChildLocationsChanged();
    }

    /* access modifiers changed from: private */
    public void logNotificationVisibilityChanges(Collection<NotificationVisibility> collection, Collection<NotificationVisibility> collection2) {
        if (!collection.isEmpty() || !collection2.isEmpty()) {
            final NotificationVisibility[] cloneVisibilitiesAsArr = cloneVisibilitiesAsArr(collection);
            final NotificationVisibility[] cloneVisibilitiesAsArr2 = cloneVisibilitiesAsArr(collection2);
            ((NotificationStat) Dependency.get(NotificationStat.class)).logNotificationVisibilityChanges(cloneVisibilitiesAsKeyList(collection), cloneVisibilitiesAsKeyList(collection2), this.mFloating, this.mKeyguard);
            this.mUiOffloadThread.submit(new Runnable() {
                public void run() {
                    try {
                        NotificationLogger.this.mBarService.onNotificationVisibilityChanged(cloneVisibilitiesAsArr, cloneVisibilitiesAsArr2);
                    } catch (RemoteException unused) {
                    }
                    int length = cloneVisibilitiesAsArr.length;
                    if (length > 0) {
                        String[] strArr = new String[length];
                        for (int i = 0; i < length; i++) {
                            strArr[i] = cloneVisibilitiesAsArr[i].key;
                        }
                        try {
                            NotificationLogger.this.mStatusBar.setNotificationsShown(strArr);
                        } catch (RuntimeException e) {
                            Log.d("NotificationLogger", "failed setNotificationsShown: ", e);
                        }
                    }
                    NotificationLogger.this.recycleAllVisibilityObjects(cloneVisibilitiesAsArr);
                    NotificationLogger.this.recycleAllVisibilityObjects(cloneVisibilitiesAsArr2);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void recycleAllVisibilityObjects(ArraySet<NotificationVisibility> arraySet) {
        int size = arraySet.size();
        for (int i = 0; i < size; i++) {
            arraySet.valueAt(i).recycle();
        }
        arraySet.clear();
    }

    /* access modifiers changed from: private */
    public void recycleAllVisibilityObjects(NotificationVisibility[] notificationVisibilityArr) {
        int length = notificationVisibilityArr.length;
        for (int i = 0; i < length; i++) {
            if (notificationVisibilityArr[i] != null) {
                notificationVisibilityArr[i].recycle();
            }
        }
    }

    private NotificationVisibility[] cloneVisibilitiesAsArr(Collection<NotificationVisibility> collection) {
        NotificationVisibility[] notificationVisibilityArr = new NotificationVisibility[collection.size()];
        int i = 0;
        for (NotificationVisibility next : collection) {
            if (next != null) {
                notificationVisibilityArr[i] = next.clone();
            }
            i++;
        }
        return notificationVisibilityArr;
    }

    private List<String> cloneVisibilitiesAsKeyList(Collection<NotificationVisibility> collection) {
        return (List) collection.stream().map($$Lambda$NotificationLogger$RcYyg5MiDFAhIVFtnq3IbAOhP20.INSTANCE).collect(Collectors.toList());
    }
}

package com.android.systemui.statusbar;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import com.android.systemui.statusbar.phone.NotificationGroupManagerInjectorKt;
import com.android.systemui.statusbar.phone.NotificationListenerWithPlugins;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressLint({"OverrideAbstract"})
public class NotificationListener extends NotificationListenerWithPlugins {
    private final Context mContext;
    private final Handler mMainHandler;
    private final List<NotificationHandler> mNotificationHandlers = new ArrayList();
    private final NotificationManager mNotificationManager;
    private final ArrayList<NotificationSettingsListener> mSettingsListeners = new ArrayList<>();

    public interface NotificationHandler {
        void onNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap);

        void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap);

        void onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, int i);

        void onNotificationsInitialized();
    }

    public interface NotificationSettingsListener {
        default void onStatusBarIconsBehaviorChanged(boolean z) {
        }
    }

    public NotificationListener(Context context, NotificationManager notificationManager, Handler handler) {
        this.mContext = context;
        this.mNotificationManager = notificationManager;
        this.mMainHandler = handler;
    }

    public void addNotificationHandler(NotificationHandler notificationHandler) {
        if (!this.mNotificationHandlers.contains(notificationHandler)) {
            this.mNotificationHandlers.add(notificationHandler);
            return;
        }
        throw new IllegalArgumentException("Listener is already added");
    }

    public void addNotificationSettingsListener(NotificationSettingsListener notificationSettingsListener) {
        this.mSettingsListeners.add(notificationSettingsListener);
    }

    public void onListenerConnected() {
        onPluginConnected();
        StatusBarNotification[] activeNotifications = getActiveNotifications();
        if (activeNotifications == null) {
            Log.w("NotificationListener", "onListenerConnected unable to get active notifications.");
            return;
        }
        this.mMainHandler.post(new Runnable(activeNotifications, getCurrentRanking()) {
            /* class com.android.systemui.statusbar.$$Lambda$NotificationListener$IqvG8K3BFQSXJ_G1S_U_QONW3G4 */
            public final /* synthetic */ StatusBarNotification[] f$1;
            public final /* synthetic */ NotificationListenerService.RankingMap f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void run() {
                NotificationListener.this.lambda$onListenerConnected$0$NotificationListener(this.f$1, this.f$2);
            }
        });
        onSilentStatusBarIconsVisibilityChanged(this.mNotificationManager.shouldHideSilentStatusBarIcons());
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onListenerConnected$0 */
    public /* synthetic */ void lambda$onListenerConnected$0$NotificationListener(StatusBarNotification[] statusBarNotificationArr, NotificationListenerService.RankingMap rankingMap) {
        ArrayList arrayList = new ArrayList();
        for (StatusBarNotification statusBarNotification : statusBarNotificationArr) {
            arrayList.add(getRankingOrTemporaryStandIn(rankingMap, statusBarNotification.getKey()));
        }
        NotificationListenerService.RankingMap rankingMap2 = new NotificationListenerService.RankingMap((NotificationListenerService.Ranking[]) arrayList.toArray(new NotificationListenerService.Ranking[0]));
        for (StatusBarNotification statusBarNotification2 : statusBarNotificationArr) {
            if (!NotificationGroupManagerInjectorKt.shouldHideGroupSummary(statusBarNotification2)) {
                for (NotificationHandler notificationHandler : this.mNotificationHandlers) {
                    notificationHandler.onNotificationPosted(statusBarNotification2, rankingMap2);
                }
            }
        }
        for (NotificationHandler notificationHandler2 : this.mNotificationHandlers) {
            notificationHandler2.onNotificationsInitialized();
        }
    }

    public void onNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        if (statusBarNotification != null && !onPluginNotificationPosted(statusBarNotification, rankingMap)) {
            this.mMainHandler.post(new Runnable(statusBarNotification, rankingMap) {
                /* class com.android.systemui.statusbar.$$Lambda$NotificationListener$NvFmU0XrVPuc5pizHcri9I0apkw */
                public final /* synthetic */ StatusBarNotification f$1;
                public final /* synthetic */ NotificationListenerService.RankingMap f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    NotificationListener.this.lambda$onNotificationPosted$1$NotificationListener(this.f$1, this.f$2);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onNotificationPosted$1 */
    public /* synthetic */ void lambda$onNotificationPosted$1$NotificationListener(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        RemoteInputController.processForRemoteInput(statusBarNotification.getNotification(), this.mContext);
        if (!NotificationGroupManagerInjectorKt.shouldHideGroupSummary(statusBarNotification)) {
            for (NotificationHandler notificationHandler : this.mNotificationHandlers) {
                notificationHandler.onNotificationPosted(statusBarNotification, rankingMap);
            }
        }
    }

    public void onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, int i) {
        if (statusBarNotification != null && !onPluginNotificationRemoved(statusBarNotification, rankingMap)) {
            this.mMainHandler.post(new Runnable(statusBarNotification, rankingMap, i) {
                /* class com.android.systemui.statusbar.$$Lambda$NotificationListener$WRx7hwuhf4Oq9iR81FcmuDk9R0 */
                public final /* synthetic */ StatusBarNotification f$1;
                public final /* synthetic */ NotificationListenerService.RankingMap f$2;
                public final /* synthetic */ int f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                public final void run() {
                    NotificationListener.this.lambda$onNotificationRemoved$2$NotificationListener(this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onNotificationRemoved$2 */
    public /* synthetic */ void lambda$onNotificationRemoved$2$NotificationListener(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, int i) {
        for (NotificationHandler notificationHandler : this.mNotificationHandlers) {
            notificationHandler.onNotificationRemoved(statusBarNotification, rankingMap, i);
        }
    }

    public void onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        onNotificationRemoved(statusBarNotification, rankingMap, 0);
    }

    public void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
        if (rankingMap != null) {
            this.mMainHandler.post(new Runnable(onPluginRankingUpdate(rankingMap)) {
                /* class com.android.systemui.statusbar.$$Lambda$NotificationListener$MPB4hTnfgfJz099PViVIkkbEBIE */
                public final /* synthetic */ NotificationListenerService.RankingMap f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    NotificationListener.this.lambda$onNotificationRankingUpdate$3$NotificationListener(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onNotificationRankingUpdate$3 */
    public /* synthetic */ void lambda$onNotificationRankingUpdate$3$NotificationListener(NotificationListenerService.RankingMap rankingMap) {
        for (NotificationHandler notificationHandler : this.mNotificationHandlers) {
            notificationHandler.onNotificationRankingUpdate(rankingMap);
        }
    }

    public void onSilentStatusBarIconsVisibilityChanged(boolean z) {
        Iterator<NotificationSettingsListener> it = this.mSettingsListeners.iterator();
        while (it.hasNext()) {
            it.next().onStatusBarIconsBehaviorChanged(z);
        }
    }

    public void registerAsSystemService() {
        try {
            registerAsSystemService(this.mContext, new ComponentName(this.mContext.getPackageName(), getClass().getCanonicalName()), -1);
        } catch (RemoteException e) {
            Log.e("NotificationListener", "Unable to register notification listener", e);
        }
    }

    private static NotificationListenerService.Ranking getRankingOrTemporaryStandIn(NotificationListenerService.RankingMap rankingMap, String str) {
        NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
        if (rankingMap.getRanking(str, ranking)) {
            return ranking;
        }
        ranking.populate(str, 0, false, 0, 0, 0, null, null, null, new ArrayList(), new ArrayList(), false, 0, false, 0, false, new ArrayList(), new ArrayList(), false, false, false, null, false);
        return ranking;
    }
}

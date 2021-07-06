package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import codeinjection.CodeInjection;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.FeatureFlags;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.NotificationRankingManager;
import com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinder;
import com.android.systemui.statusbar.notification.unimportant.FoldListener;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import com.android.systemui.statusbar.notification.unimportant.FoldNotifController;
import com.android.systemui.statusbar.notification.unimportant.FoldTool;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.Assert;
import com.android.systemui.util.leak.LeakDetector;
import dagger.Lazy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.SequencesKt___SequencesKt;

public class MiuiNotificationEntryManager extends NotificationEntryManager implements ConfigurationController.ConfigurationListener, FoldListener {
    private final ColorDrawable TRANSPARENT_DRAWABLE;
    private final ArrayMap<String, NotificationEntry> activeUnimportantNotifications = new ArrayMap<>();
    private final NotificationGroupManager groupManager;
    private List<? extends Drawable> iconList;
    private boolean isShowingUnimportant;
    private final NotificationListener.NotificationHandler notifListener;
    private final NotificationRankingManager rankingManager;
    private final List<NotificationEntry> readOnlyUnimportantNotifications;
    private final ArrayList<NotificationEntry> sortedAndFilteredUnimportant;
    private final HashSet<String> transferSet;

    public static final /* synthetic */ NotificationRankingManager access$getRankingManager$p(MiuiNotificationEntryManager miuiNotificationEntryManager) {
        return miuiNotificationEntryManager.rankingManager;
    }

    public static final /* synthetic */ void access$updateFoldRankingAndSort(MiuiNotificationEntryManager miuiNotificationEntryManager, NotificationListenerService.RankingMap rankingMap, String str) {
        miuiNotificationEntryManager.updateFoldRankingAndSort(rankingMap, str);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiNotificationEntryManager(Context context, NotificationEntryManagerLogger notificationEntryManagerLogger, NotificationGroupManager notificationGroupManager, NotificationRankingManager notificationRankingManager, NotificationEntryManager.KeyguardEnvironment keyguardEnvironment, FeatureFlags featureFlags, Lazy<NotificationRowBinder> lazy, Lazy<NotificationRemoteInputManager> lazy2, LeakDetector leakDetector, ForegroundServiceDismissalFeatureController foregroundServiceDismissalFeatureController) {
        super(notificationEntryManagerLogger, notificationGroupManager, notificationRankingManager, keyguardEnvironment, featureFlags, lazy, lazy2, leakDetector, foregroundServiceDismissalFeatureController);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(notificationEntryManagerLogger, "logger");
        Intrinsics.checkParameterIsNotNull(notificationGroupManager, "groupManager");
        Intrinsics.checkParameterIsNotNull(notificationRankingManager, "rankingManager");
        Intrinsics.checkParameterIsNotNull(keyguardEnvironment, "keyguardEnvironment");
        Intrinsics.checkParameterIsNotNull(featureFlags, "featureFlags");
        Intrinsics.checkParameterIsNotNull(lazy, "notificationRowBinderLazy");
        Intrinsics.checkParameterIsNotNull(lazy2, "notificationRemoteInputManagerLazy");
        Intrinsics.checkParameterIsNotNull(leakDetector, "leakDetector");
        Intrinsics.checkParameterIsNotNull(foregroundServiceDismissalFeatureController, "fgsFeatureController");
        this.groupManager = notificationGroupManager;
        this.rankingManager = notificationRankingManager;
        ArrayList<NotificationEntry> arrayList = new ArrayList<>();
        this.sortedAndFilteredUnimportant = arrayList;
        this.readOnlyUnimportantNotifications = Collections.unmodifiableList(arrayList);
        this.notifListener = new MiuiNotificationEntryManager$notifListener$1(this);
        FoldTool.INSTANCE.init(context);
        this.TRANSPARENT_DRAWABLE = new ColorDrawable(0);
        this.iconList = new ArrayList();
        this.transferSet = new HashSet<>();
    }

    @Override // com.android.systemui.statusbar.notification.NotificationEntryManager
    public void attach(NotificationListener notificationListener) {
        if (notificationListener != null) {
            notificationListener.addNotificationHandler(this.notifListener);
        }
        FoldManager.Companion.addListener(this);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationEntryManager
    public void removeVisibleNotification(String str) {
        super.removeVisibleNotification(str);
        NotificationEntry remove = this.activeUnimportantNotifications.remove(str);
        if (remove != null) {
            Intrinsics.checkExpressionValueIsNotNull(remove, "activeUnimportantNotific…ons.remove(key) ?: return");
            this.groupManager.onEntryRemoved(remove);
            checkFoldEntrance(remove.getSbn());
            if (this.isShowingUnimportant && !shouldShow()) {
                FoldManager.Companion.notifyListeners(5);
            }
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationEntryManager
    public void addActiveNotification(NotificationEntry notificationEntry) {
        if (notificationEntry != null) {
            Assert.isMainThread();
            if (NotificationUtil.isFold(notificationEntry.getSbn())) {
                this.activeUnimportantNotifications.put(notificationEntry.getKey(), notificationEntry);
                this.groupManager.onEntryAdded(notificationEntry);
                checkFoldEntrance(notificationEntry.getSbn());
            } else {
                super.addActiveNotification(notificationEntry);
            }
            ExpandedNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
            String targetPackageName = sbn.getTargetPackageName();
            Intrinsics.checkExpressionValueIsNotNull(targetPackageName, "entry.sbn.targetPackageName");
            ((FoldNotifController) Dependency.get(FoldNotifController.class)).addShowCount(targetPackageName);
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationEntryManager
    public void updateNotification(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        super.updateNotification(statusBarNotification, rankingMap);
        if (NotificationUtil.isFold(statusBarNotification)) {
            checkFoldEntrance(statusBarNotification);
        }
    }

    private final void checkFoldEntrance(StatusBarNotification statusBarNotification) {
        if (statusBarNotification != null && !NotificationUtil.isFoldEntrance(statusBarNotification).booleanValue()) {
            NotificationListenerService.RankingMap rankingMap = this.rankingManager.getRankingMap();
            if (rankingMap != null) {
                updateFoldRankingAndSort(rankingMap, "checkFoldEntrance");
                FoldManager.Companion.checkUnimportantNotification(shouldShow(), getUserFromSbn(statusBarNotification));
                return;
            }
            Intrinsics.throwNpe();
            throw null;
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationEntryManager
    public void updateRankingAndSort(NotificationListenerService.RankingMap rankingMap, String str) {
        updateFoldRankingAndSort(rankingMap, str);
        super.updateRankingAndSort(rankingMap, str);
    }

    private final void updateFoldRankingAndSort(NotificationListenerService.RankingMap rankingMap, String str) {
        if (!FoldManager.Companion.shouldSuppressFold()) {
            this.sortedAndFilteredUnimportant.clear();
            ArrayList<NotificationEntry> arrayList = this.sortedAndFilteredUnimportant;
            NotificationRankingManager notificationRankingManager = this.rankingManager;
            Collection<NotificationEntry> values = this.activeUnimportantNotifications.values();
            Intrinsics.checkExpressionValueIsNotNull(values, "activeUnimportantNotifications.values");
            if (str == null) {
                str = CodeInjection.MD5;
            }
            arrayList.addAll(notificationRankingManager.updateRanking(rankingMap, values, str));
            refreshIcons();
        }
    }

    private final void refreshIcons() {
        this.iconList = SequencesKt___SequencesKt.toMutableList(SequencesKt___SequencesKt.take(SequencesKt___SequencesKt.filter(SequencesKt___SequencesKt.map(SequencesKt___SequencesKt.distinctBy(SequencesKt___SequencesKt.filter(SequencesKt___SequencesKt.filterNotNull(CollectionsKt___CollectionsKt.asSequence(this.sortedAndFilteredUnimportant)), new MiuiNotificationEntryManager$refreshIcons$1(this)), MiuiNotificationEntryManager$refreshIcons$2.INSTANCE), MiuiNotificationEntryManager$refreshIcons$3.INSTANCE), new MiuiNotificationEntryManager$refreshIcons$4(this)), 4));
    }

    @Override // com.android.systemui.statusbar.notification.NotificationEntryManager
    public NotificationEntry getActiveNotificationUnfiltered(String str) {
        NotificationEntry notificationEntry = this.activeUnimportantNotifications.get(str);
        return notificationEntry != null ? notificationEntry : super.getActiveNotificationUnfiltered(str);
    }

    @Override // com.android.systemui.statusbar.notification.NotificationEntryManager
    public List<NotificationEntry> getVisibleNotifications() {
        if (this.isShowingUnimportant) {
            List<NotificationEntry> list = this.readOnlyUnimportantNotifications;
            Intrinsics.checkExpressionValueIsNotNull(list, "readOnlyUnimportantNotifications");
            return list;
        }
        List<NotificationEntry> visibleNotifications = super.getVisibleNotifications();
        Intrinsics.checkExpressionValueIsNotNull(visibleNotifications, "super.getVisibleNotifications()");
        return visibleNotifications;
    }

    @Override // com.android.systemui.statusbar.notification.NotificationEntryManager
    public NotificationEntry getPendingOrActiveNotif(String str) {
        if (this.mPendingNotifications.containsKey(str)) {
            return this.mPendingNotifications.get(str);
        }
        return getActiveNotificationUnfiltered(str);
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.FoldListener
    public void showUnimportantNotifications() {
        this.isShowingUnimportant = true;
        showUnimportantNotifications(true);
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.FoldListener
    public void resetAll(boolean z) {
        this.isShowingUnimportant = false;
        showUnimportantNotifications(false);
    }

    private final void showUnimportantNotifications(boolean z) {
        updateNotifications(z ? "show unimportant notifications" : "show important notifications");
    }

    public final void changeFoldEnabled(boolean z) {
        if (z) {
            ArrayMap<String, NotificationEntry> arrayMap = this.mActiveNotifications;
            Intrinsics.checkExpressionValueIsNotNull(arrayMap, "mActiveNotifications");
            transferNotifications("UNIMPORTANT", arrayMap, this.activeUnimportantNotifications, 0, true, false, "enable_fold");
            return;
        }
        ArrayMap<String, NotificationEntry> arrayMap2 = this.activeUnimportantNotifications;
        ArrayMap<String, NotificationEntry> arrayMap3 = this.mActiveNotifications;
        Intrinsics.checkExpressionValueIsNotNull(arrayMap3, "mActiveNotifications");
        transferNotifications("UNIMPORTANT", arrayMap2, arrayMap3, 0, false, false, "disable_fold");
    }

    public final void changeFold2SysCommend(String str) {
        ArrayMap<String, NotificationEntry> arrayMap = this.mActiveNotifications;
        Intrinsics.checkExpressionValueIsNotNull(arrayMap, "mActiveNotifications");
        transferNotifications(str, arrayMap, this.activeUnimportantNotifications, 0, true, true, "fold_sys_commend");
        ArrayMap<String, NotificationEntry> arrayMap2 = this.activeUnimportantNotifications;
        ArrayMap<String, NotificationEntry> arrayMap3 = this.mActiveNotifications;
        Intrinsics.checkExpressionValueIsNotNull(arrayMap3, "mActiveNotifications");
        transferNotifications(str, arrayMap2, arrayMap3, 0, false, true, "fold_sys_commend");
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.FoldListener
    public void recoverPackageFromUnimportant(String str) {
        Intrinsics.checkParameterIsNotNull(str, "packageName");
        ArrayMap<String, NotificationEntry> arrayMap = this.activeUnimportantNotifications;
        ArrayMap<String, NotificationEntry> arrayMap2 = this.mActiveNotifications;
        Intrinsics.checkExpressionValueIsNotNull(arrayMap2, "mActiveNotifications");
        transferNotifications(str, arrayMap, arrayMap2, 1, false, false, "recoverPkg");
    }

    @Override // com.android.systemui.statusbar.notification.unimportant.FoldListener
    public void foldPackageAsUnimportant(String str) {
        Intrinsics.checkParameterIsNotNull(str, "packageName");
        ArrayMap<String, NotificationEntry> arrayMap = this.mActiveNotifications;
        Intrinsics.checkExpressionValueIsNotNull(arrayMap, "mActiveNotifications");
        transferNotifications(str, arrayMap, this.activeUnimportantNotifications, -1, true, false, "foldPkg");
    }

    public final boolean shouldShow() {
        for (Map.Entry<String, NotificationEntry> entry : this.activeUnimportantNotifications.entrySet()) {
            NotificationEntry value = entry.getValue();
            Intrinsics.checkExpressionValueIsNotNull(value, "entry.value");
            if (isSameUser(value.getSbn())) {
                return true;
            }
        }
        return false;
    }

    /* JADX DEBUG: Type inference failed for r0v1. Raw type applied. Possible types: java.util.List<? extends android.graphics.drawable.Drawable>, java.util.List<android.graphics.drawable.Drawable> */
    public final List<Drawable> getIcons() {
        return this.iconList;
    }

    private final void transferNotifications(String str, ArrayMap<String, NotificationEntry> arrayMap, ArrayMap<String, NotificationEntry> arrayMap2, int i, boolean z, boolean z2, String str2) {
        if (!(FoldManager.Companion.shouldSuppressFold() || str == null)) {
            this.transferSet.clear();
            Collection<NotificationEntry> values = arrayMap.values();
            Intrinsics.checkExpressionValueIsNotNull(values, "fromMap.values");
            boolean z3 = false;
            for (NotificationEntry notificationEntry : SequencesKt___SequencesKt.filter(SequencesKt___SequencesKt.filter(SequencesKt___SequencesKt.filter(SequencesKt___SequencesKt.filterNotNull(CollectionsKt___CollectionsKt.asSequence(values)), new MiuiNotificationEntryManager$transferNotifications$1(this)), new MiuiNotificationEntryManager$transferNotifications$2(str)), new MiuiNotificationEntryManager$transferNotifications$3(z2, i, z))) {
                NotificationUtil.setFold(notificationEntry.getSbn(), z);
                notificationEntry.getRow().onNotificationRankingUpdated();
                this.transferSet.add(notificationEntry.getKey());
                arrayMap2.put(notificationEntry.getKey(), notificationEntry);
                z3 = true;
            }
            arrayMap.removeAll(this.transferSet);
            this.transferSet.clear();
            if (z3) {
                NotificationGroupManager notificationGroupManager = this.groupManager;
                List<NotificationGroupManager.NotificationGroup> allGroups = notificationGroupManager.getAllGroups(str);
                Intrinsics.checkExpressionValueIsNotNull(allGroups, "getAllGroups(packageName)");
                for (T t : allGroups) {
                    notificationGroupManager.setGroupExpanded((NotificationGroupManager.NotificationGroup) t, false);
                    notificationGroupManager.updateSuppression((NotificationGroupManager.NotificationGroup) t);
                }
                if (this.rankingManager.getRankingMap() != null) {
                    NotificationListenerService.RankingMap rankingMap = this.rankingManager.getRankingMap();
                    if (rankingMap != null) {
                        updateRankingAndSort(rankingMap, str2);
                    } else {
                        Intrinsics.throwNpe();
                        throw null;
                    }
                }
                boolean shouldShow = shouldShow();
                NotificationListenerService.RankingMap rankingMap2 = this.rankingManager.getRankingMap();
                if (rankingMap2 != null) {
                    updateFoldRankingAndSort(rankingMap2, "transferNotifications");
                    FoldManager.Companion.checkUnimportantNotification(shouldShow, getCurrentUser());
                    if (this.isShowingUnimportant && !shouldShow) {
                        FoldManager.Companion.notifyListeners(5);
                        return;
                    }
                    return;
                }
                Intrinsics.throwNpe();
                throw null;
            }
        }
    }

    @Override // com.android.systemui.statusbar.notification.NotificationEntryManager
    public List<NotificationEntry> getActiveNotificationsForCurrentUser() {
        Assert.isMainThread();
        ArrayList arrayList = new ArrayList();
        for (NotificationEntry notificationEntry : this.activeUnimportantNotifications.values()) {
            Intrinsics.checkExpressionValueIsNotNull(notificationEntry, "entry");
            ExpandedNotification sbn = notificationEntry.getSbn();
            Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
            if (this.mKeyguardEnvironment.isNotificationForCurrentProfiles(sbn)) {
                arrayList.add(notificationEntry);
            }
        }
        arrayList.addAll(super.getActiveNotificationsForCurrentUser());
        return arrayList;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onMiuiThemeChanged(boolean z) {
        super.onMiuiThemeChanged(z);
        FoldManager.Companion.getHandler().postDelayed(new MiuiNotificationEntryManager$onMiuiThemeChanged$1(this), 500);
    }

    public final UserHandle getCurrentUser() {
        Object obj = Dependency.get(NotificationLockscreenUserManager.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(Notificat…nUserManager::class.java)");
        UserHandle of = UserHandle.of(((NotificationLockscreenUserManager) obj).getCurrentUserId());
        Intrinsics.checkExpressionValueIsNotNull(of, "UserHandle.of(Dependency…lass.java).currentUserId)");
        return of;
    }

    private final UserHandle getUserFromSbn(StatusBarNotification statusBarNotification) {
        String str;
        UserHandle userHandle;
        if (statusBarNotification == null) {
            return getCurrentUser();
        }
        if (statusBarNotification.getUserId() == 999) {
            userHandle = UserHandle.OWNER;
            str = "UserHandle.OWNER";
        } else {
            userHandle = statusBarNotification.getUser();
            str = "sbn.user";
        }
        Intrinsics.checkExpressionValueIsNotNull(userHandle, str);
        return userHandle;
    }

    public final boolean isSameUser(StatusBarNotification statusBarNotification) {
        boolean z;
        if (statusBarNotification == null) {
            return false;
        }
        if (statusBarNotification.getUserId() != -1) {
            int userId = statusBarNotification.getUserId();
            Object obj = Dependency.get(NotificationLockscreenUserManager.class);
            Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(Notificat…nUserManager::class.java)");
            if (userId != ((NotificationLockscreenUserManager) obj).getCurrentUserId()) {
                z = false;
                return !z || (UserHandle.myUserId() != 0 && statusBarNotification.getUserId() == 999);
            }
        }
        z = true;
        if (!z) {
        }
    }
}

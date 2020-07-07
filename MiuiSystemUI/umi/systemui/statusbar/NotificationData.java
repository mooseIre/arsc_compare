package com.android.systemui.statusbar;

import android.app.AppGlobals;
import android.app.Notification;
import android.app.NotificationChannelCompat;
import android.content.Context;
import android.content.pm.IPackageManager;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.SystemClock;
import android.service.notification.NotificationListenerService;
import android.service.notification.NotificationListenerServiceCompat;
import android.service.notification.RankingCompat;
import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import android.service.notification.StatusBarNotificationCompat;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.NotificationVisibilityCompat;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.statusbar.StatusBarServiceCompat;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.ForegroundServiceController;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.miui.statusbar.phone.rank.RankUtil;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.notification.InflationException;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NotificationData {
    private IStatusBarService mBarService;
    private final ArrayList<Entry> mClearablEntries = new ArrayList<>();
    private final ArrayMap<String, Entry> mEntries = new ArrayMap<>();
    /* access modifiers changed from: private */
    public final Environment mEnvironment;
    /* access modifiers changed from: private */
    public NotificationGroupManager mGroupManager;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    /* access modifiers changed from: private */
    public HeadsUpManager mHeadsUpManager;
    private long mLastRankingMapUpdatedTime;
    private final Comparator<Entry> mRankingComparator = new Comparator<Entry>() {
        {
            new NotificationListenerService.Ranking();
            new NotificationListenerService.Ranking();
        }

        public int compare(Entry entry, Entry entry2) {
            ExpandedNotification expandedNotification = entry.notification;
            ExpandedNotification expandedNotification2 = entry2.notification;
            int importance = expandedNotification.getImportance();
            int importance2 = expandedNotification2.getImportance();
            int compareHeadsUp = RankUtil.compareHeadsUp(entry, entry2, NotificationData.this.mHeadsUpManager);
            if (compareHeadsUp != 0) {
                return compareHeadsUp;
            }
            int compareShowingAtTail = RankUtil.compareShowingAtTail(entry, entry2);
            if (compareShowingAtTail != 0) {
                return compareShowingAtTail;
            }
            int compareSystemWarnings = RankUtil.compareSystemWarnings(entry, entry2);
            if (compareSystemWarnings != 0) {
                return compareSystemWarnings;
            }
            int compareMedia = RankUtil.compareMedia(entry, entry2, importance, importance2, NotificationData.this.mEnvironment.getCurrentMediaNotificationKey());
            if (compareMedia != 0) {
                return compareMedia;
            }
            int comparePrioritizedMax = RankUtil.comparePrioritizedMax(entry, entry2, importance, importance2);
            return comparePrioritizedMax != 0 ? comparePrioritizedMax : RankUtil.compareWhen(expandedNotification, expandedNotification2);
        }
    };
    private NotificationListenerService.RankingMap mRankingMap;
    private final ArrayList<Entry> mSortedAndFiltered = new ArrayList<>();
    private final NotificationListenerService.Ranking mTmpRanking = new NotificationListenerService.Ranking();

    public interface Environment {
        IStatusBarService getBarService();

        String getCurrentMediaNotificationKey();

        NotificationGroupManager getGroupManager();

        boolean isDeviceProvisioned();

        boolean isNotificationForCurrentProfiles(StatusBarNotification statusBarNotification);

        boolean isSecurelyLocked(int i);

        boolean isSuperSaveModeOn();

        boolean shouldHideNotifications(int i);

        boolean shouldHideNotifications(String str);
    }

    public static final class Entry {
        public boolean autoRedacted;
        public RemoteViews cachedAmbientContentView;
        public RemoteViews cachedBigContentView;
        public RemoteViews cachedContentView;
        public RemoteViews cachedHeadsUpContentView;
        public RemoteViews cachedPublicContentView;
        public boolean canBubble;
        public boolean canShowBaged;
        public NotificationChannelCompat channel;
        public StatusBarIconView expandedIcon;
        public long firstWhen;
        public StatusBarIconView foldFooterIcon;
        public boolean hideSensitive;
        public boolean hideSensitiveByAppLock;
        public StatusBarIconView icon;
        private boolean interruption;
        public boolean isGameModeWhenHeadsUp;
        public String key;
        private long lastFullScreenIntentLaunchTime = -2000;
        public boolean mIsShowMiniWindowBar;
        private InflationTask mRunningTask = null;
        private boolean mShowInShadeWhenBubble;
        private boolean mUserDismissedBubble;
        public boolean needUpdateBadgeNum;
        public ExpandedNotification notification;
        public CharSequence remoteInputText;
        public ExpandableNotificationRow row;
        public long seeTime;
        public List<SnoozeCriterion> snoozeCriteria;
        public int targetSdk;

        public Entry(ExpandedNotification expandedNotification) {
            this.key = expandedNotification.getKey();
            this.notification = expandedNotification;
            this.firstWhen = expandedNotification.getNotification().when;
        }

        public void setInterruption() {
            this.interruption = true;
        }

        public boolean hasInterrupted() {
            return this.interruption;
        }

        public boolean isBubble() {
            return (this.notification.getNotification().flags & 4096) != 0;
        }

        public void setBubbleDismissed(boolean z) {
            this.mUserDismissedBubble = z;
        }

        public boolean isBubbleDismissed() {
            return this.mUserDismissedBubble;
        }

        public void setShowInShadeWhenBubble(boolean z) {
            this.mShowInShadeWhenBubble = z;
        }

        public boolean showInShadeWhenBubble() {
            return !isRowDismissed() && (!isClearable() || this.mShowInShadeWhenBubble);
        }

        public boolean isForegroundService() {
            return (this.notification.getNotification().flags & 64) != 0;
        }

        public boolean isRowDismissed() {
            ExpandableNotificationRow expandableNotificationRow = this.row;
            return expandableNotificationRow != null && expandableNotificationRow.isDismissed();
        }

        public boolean isClearable() {
            ExpandedNotification expandedNotification = this.notification;
            if (expandedNotification == null || !expandedNotification.isClearable()) {
                return false;
            }
            List<Entry> children = getChildren();
            if (children == null || children.size() <= 0) {
                return true;
            }
            for (int i = 0; i < children.size(); i++) {
                if (!children.get(i).isClearable()) {
                    return false;
                }
            }
            return true;
        }

        public List<Entry> getChildren() {
            List<ExpandableNotificationRow> notificationChildren;
            ExpandableNotificationRow expandableNotificationRow = this.row;
            if (expandableNotificationRow == null || (notificationChildren = expandableNotificationRow.getNotificationChildren()) == null) {
                return null;
            }
            ArrayList arrayList = new ArrayList();
            for (ExpandableNotificationRow entry : notificationChildren) {
                arrayList.add(entry.getEntry());
            }
            return arrayList;
        }

        public void reset() {
            this.lastFullScreenIntentLaunchTime = -2000;
            ExpandableNotificationRow expandableNotificationRow = this.row;
            if (expandableNotificationRow != null) {
                expandableNotificationRow.reset();
            }
        }

        public View getPrivateView() {
            return this.row.getPrivateLayout();
        }

        public View getPrivateContentView() {
            return this.row.getPrivateLayout().getContractedChild();
        }

        public View getPublicContentView() {
            return this.row.getPublicLayout().getContractedChild();
        }

        public void notifyFullScreenIntentLaunched() {
            this.lastFullScreenIntentLaunchTime = SystemClock.elapsedRealtime();
        }

        public boolean hasJustLaunchedFullScreenIntent() {
            return SystemClock.elapsedRealtime() < this.lastFullScreenIntentLaunchTime + 2000;
        }

        public void createIcons(Context context, ExpandedNotification expandedNotification) throws InflationException {
            Notification notification2 = expandedNotification.getNotification();
            Icon smallIcon = NotificationUtil.getSmallIcon(context, expandedNotification);
            if (smallIcon != null) {
                StatusBarIconView statusBarIconView = new StatusBarIconView(context, expandedNotification.getPackageName() + "/0x" + Integer.toHexString(expandedNotification.getId()), expandedNotification);
                this.icon = statusBarIconView;
                statusBarIconView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                StatusBarIconView statusBarIconView2 = new StatusBarIconView(context, expandedNotification.getPackageName() + "/0x" + Integer.toHexString(expandedNotification.getId()), expandedNotification);
                this.expandedIcon = statusBarIconView2;
                statusBarIconView2.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                StatusBarIconView statusBarIconView3 = new StatusBarIconView(context, expandedNotification.getPackageName() + "/0x" + Integer.toHexString(expandedNotification.getId()), expandedNotification);
                this.foldFooterIcon = statusBarIconView3;
                statusBarIconView3.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                StatusBarIcon statusBarIcon = new StatusBarIcon(expandedNotification.getUser(), expandedNotification.getPackageName(), smallIcon, notification2.iconLevel, notification2.number, StatusBarIconView.contentDescForNotification(context, notification2));
                if (!this.icon.set(statusBarIcon) || !this.expandedIcon.set(statusBarIcon) || !this.foldFooterIcon.set(statusBarIcon)) {
                    this.icon = null;
                    this.expandedIcon = null;
                    this.foldFooterIcon = null;
                    throw new InflationException("Couldn't create icon: " + statusBarIcon);
                }
                this.expandedIcon.setVisibility(4);
                this.expandedIcon.setOnVisibilityChangedListener(new StatusBarIconView.OnVisibilityChangedListener() {
                    public void onVisibilityChanged(int i) {
                        ExpandableNotificationRow expandableNotificationRow = Entry.this.row;
                        if (expandableNotificationRow != null) {
                            expandableNotificationRow.setIconsVisible(i != 0);
                        }
                    }
                });
                return;
            }
            throw new InflationException("No small icon in notification from " + expandedNotification.getPackageName());
        }

        public void updateIcons(Context context, ExpandedNotification expandedNotification) throws InflationException {
            if (this.icon != null) {
                Notification notification2 = expandedNotification.getNotification();
                StatusBarIcon statusBarIcon = new StatusBarIcon(this.notification.getUser(), this.notification.getPackageName(), NotificationUtil.getSmallIcon(context, expandedNotification), notification2.iconLevel, notification2.number, StatusBarIconView.contentDescForNotification(context, notification2));
                this.icon.setNotification(expandedNotification);
                this.expandedIcon.setNotification(expandedNotification);
                this.foldFooterIcon.setNotification(expandedNotification);
                if (!this.icon.set(statusBarIcon) || !this.expandedIcon.set(statusBarIcon) || !this.foldFooterIcon.set(statusBarIcon)) {
                    throw new InflationException("Couldn't update icon: " + statusBarIcon);
                }
            }
        }

        public void abortTask() {
            InflationTask inflationTask = this.mRunningTask;
            if (inflationTask != null) {
                inflationTask.abort();
                this.mRunningTask = null;
            }
        }

        public void setInflationTask(InflationTask inflationTask) {
            InflationTask inflationTask2 = this.mRunningTask;
            abortTask();
            this.mRunningTask = inflationTask;
            if (inflationTask2 != null && inflationTask != null) {
                inflationTask.supersedeTask(inflationTask2);
            }
        }

        public void onInflationTaskFinished() {
            this.mRunningTask = null;
        }

        @VisibleForTesting
        public InflationTask getRunningTask() {
            return this.mRunningTask;
        }

        public boolean isMediaNotification() {
            ExpandableNotificationRow expandableNotificationRow = this.row;
            if (expandableNotificationRow != null) {
                return expandableNotificationRow.isMediaNotification();
            }
            return NotificationUtil.isMediaNotification(this.notification);
        }

        public boolean isCustomViewNotification() {
            ExpandableNotificationRow expandableNotificationRow = this.row;
            if (expandableNotificationRow != null) {
                return expandableNotificationRow.isCustomViewNotification();
            }
            return NotificationUtil.isCustomViewNotification(this.notification);
        }
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public NotificationData(Environment environment) {
        this.mEnvironment = environment;
        this.mGroupManager = environment.getGroupManager();
        this.mBarService = environment.getBarService();
    }

    public ArrayList<Entry> getActiveNotifications() {
        return this.mSortedAndFiltered;
    }

    public ArrayList<Entry> getAllEntries() {
        return new ArrayList<>(this.mEntries.values());
    }

    public int indexOf(Entry entry) {
        return this.mSortedAndFiltered.indexOf(entry);
    }

    public int indexOf(ExpandedNotification expandedNotification) {
        Iterator<Entry> it = this.mSortedAndFiltered.iterator();
        int i = 0;
        while (it.hasNext() && it.next().notification != expandedNotification) {
            i++;
        }
        return i;
    }

    public List<Entry> getClearableNotifications() {
        return this.mClearablEntries;
    }

    public List<Entry> getPkgNotifications(String str) {
        return (List) new ArrayList(this.mSortedAndFiltered).stream().filter(new Predicate(str) {
            public final /* synthetic */ String f$0;

            {
                this.f$0 = r1;
            }

            public final boolean test(Object obj) {
                return this.f$0.equals(((NotificationData.Entry) obj).notification.getPackageName());
            }
        }).collect(Collectors.toList());
    }

    public Entry get(String str) {
        return this.mEntries.get(str);
    }

    public void add(Entry entry) {
        checkNotificationLimit(entry.notification.getPackageName());
        synchronized (this.mEntries) {
            this.mEntries.put(entry.notification.getKey(), entry);
        }
        this.mGroupManager.onEntryAdded(entry);
        updateRankingAndSort(this.mRankingMap);
    }

    public Entry remove(String str, NotificationListenerService.RankingMap rankingMap, boolean z) {
        Entry remove;
        synchronized (this.mEntries) {
            remove = this.mEntries.remove(str);
        }
        if (remove == null) {
            return null;
        }
        final NotificationGroupManager.NotificationGroup notificationGroup = this.mGroupManager.getNotificationGroup(remove.notification.getGroupKey());
        this.mGroupManager.onEntryRemoved(remove);
        if (this.mGroupManager.canRemove(notificationGroup)) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (NotificationData.this.mGroupManager.canRemove(notificationGroup)) {
                        NotificationData.this.performRemoveNotification(notificationGroup.summary.notification);
                    }
                }
            }, 200);
        }
        if (z) {
            updateRankingAndSort(rankingMap);
        }
        return remove;
    }

    public void updateRanking(NotificationListenerService.RankingMap rankingMap) {
        updateRankingAndSort(rankingMap);
    }

    public boolean updateRankingDelayed(NotificationListenerService.RankingMap rankingMap, long j) {
        if (j < this.mLastRankingMapUpdatedTime) {
            Log.d("NotificationData", "drop deprecated ranking update message, messageReceiveTime=" + j + ",mLastRankingMapUpdatedTime=" + this.mLastRankingMapUpdatedTime);
            return false;
        }
        updateRanking(rankingMap);
        return true;
    }

    public boolean isAmbient(String str) {
        NotificationListenerService.RankingMap rankingMap = this.mRankingMap;
        if (rankingMap == null) {
            return false;
        }
        rankingMap.getRanking(str, this.mTmpRanking);
        return this.mTmpRanking.isAmbient();
    }

    public int getVisibilityOverride(String str) {
        NotificationListenerService.RankingMap rankingMap = this.mRankingMap;
        if (rankingMap == null) {
            return -1000;
        }
        rankingMap.getRanking(str, this.mTmpRanking);
        return this.mTmpRanking.getVisibilityOverride();
    }

    public boolean shouldSuppressScreenOff(String str) {
        NotificationListenerService.RankingMap rankingMap = this.mRankingMap;
        if (rankingMap == null) {
            return false;
        }
        rankingMap.getRanking(str, this.mTmpRanking);
        return NotificationListenerServiceCompat.shouldSuppressScreenOff(this.mTmpRanking);
    }

    public boolean shouldSuppressScreenOn(String str) {
        NotificationListenerService.RankingMap rankingMap = this.mRankingMap;
        if (rankingMap == null) {
            return false;
        }
        rankingMap.getRanking(str, this.mTmpRanking);
        return NotificationListenerServiceCompat.shouldSuppressScreenOn(this.mTmpRanking);
    }

    public int getImportance(ExpandedNotification expandedNotification, NotificationListenerService.RankingMap rankingMap) {
        if (Build.VERSION.SDK_INT <= 23) {
            return NotificationListenerServiceCompat.getImportance(expandedNotification.getNotification());
        }
        if (rankingMap == null) {
            return -1000;
        }
        rankingMap.getRanking(expandedNotification.getKey(), this.mTmpRanking);
        return NotificationListenerServiceCompat.getImportance(this.mTmpRanking);
    }

    public String getOverrideGroupKey(String str) {
        NotificationListenerService.RankingMap rankingMap = this.mRankingMap;
        if (rankingMap == null) {
            return null;
        }
        rankingMap.getRanking(str, this.mTmpRanking);
        return NotificationListenerServiceCompat.getOverrideGroupKey(this.mTmpRanking);
    }

    public boolean canBubble(String str) {
        NotificationListenerService.RankingMap rankingMap = this.mRankingMap;
        if (rankingMap == null) {
            return false;
        }
        rankingMap.getRanking(str, this.mTmpRanking);
        return NotificationListenerServiceCompat.canBubble(this.mTmpRanking);
    }

    public List<SnoozeCriterion> getSnoozeCriteria(String str) {
        NotificationListenerService.RankingMap rankingMap = this.mRankingMap;
        if (rankingMap == null) {
            return null;
        }
        rankingMap.getRanking(str, this.mTmpRanking);
        return RankingCompat.getSnoozeCriteria(this.mTmpRanking);
    }

    public NotificationChannelCompat getChannel(String str) {
        NotificationListenerService.RankingMap rankingMap = this.mRankingMap;
        if (rankingMap == null) {
            return new NotificationChannelCompat("miscellaneous", "Default", -1000);
        }
        rankingMap.getRanking(str, this.mTmpRanking);
        return NotificationChannelCompat.getChannel(this.mTmpRanking);
    }

    public int getRank(String str) {
        NotificationListenerService.RankingMap rankingMap = this.mRankingMap;
        if (rankingMap == null) {
            return 0;
        }
        rankingMap.getRanking(str, this.mTmpRanking);
        return this.mTmpRanking.getRank();
    }

    private void updateRankingAndSort(NotificationListenerService.RankingMap rankingMap) {
        if (rankingMap != null) {
            this.mRankingMap = rankingMap;
            this.mLastRankingMapUpdatedTime = SystemClock.uptimeMillis();
            synchronized (this.mEntries) {
                int size = this.mEntries.size();
                for (int i = 0; i < size; i++) {
                    Entry valueAt = this.mEntries.valueAt(i);
                    StatusBarNotification cloneLight = valueAt.notification.cloneLight();
                    String overrideGroupKey = getOverrideGroupKey(valueAt.key);
                    if (!Objects.equals(StatusBarNotificationCompat.getOverrideGroupKey(cloneLight), overrideGroupKey)) {
                        StatusBarNotificationCompat.setOverrideGroupKey(valueAt.notification, overrideGroupKey);
                        this.mGroupManager.onEntryUpdated(valueAt, cloneLight);
                    }
                    valueAt.channel = getChannel(valueAt.key);
                    valueAt.snoozeCriteria = getSnoozeCriteria(valueAt.key);
                }
            }
        }
        filterAndSort();
    }

    public void filterAndSort() {
        this.mSortedAndFiltered.clear();
        this.mClearablEntries.clear();
        synchronized (this.mEntries) {
            int size = this.mEntries.size();
            for (int i = 0; i < size; i++) {
                Entry valueAt = this.mEntries.valueAt(i);
                ExpandedNotification expandedNotification = valueAt.notification;
                if (!shouldFilterOut(expandedNotification)) {
                    if (!filterBubble(valueAt)) {
                        if (expandedNotification.isClearable() && !expandedNotification.isSystemWarnings()) {
                            this.mClearablEntries.add(valueAt);
                        }
                        this.mSortedAndFiltered.add(valueAt);
                    }
                }
            }
        }
        Collections.sort(this.mSortedAndFiltered, this.mRankingComparator);
    }

    public boolean shouldFilterOut(ExpandedNotification expandedNotification) {
        if (this.mEnvironment.isSuperSaveModeOn()) {
            return true;
        }
        if ((!this.mEnvironment.isDeviceProvisioned() && !showNotificationEvenIfUnprovisioned(expandedNotification)) || !this.mEnvironment.isNotificationForCurrentProfiles(expandedNotification)) {
            return true;
        }
        if (this.mEnvironment.isSecurelyLocked(expandedNotification.getUserId()) && (expandedNotification.getNotification().visibility == -1 || this.mEnvironment.shouldHideNotifications(expandedNotification.getUserId()) || this.mEnvironment.shouldHideNotifications(expandedNotification.getKey()))) {
            return true;
        }
        if (!StatusBar.ENABLE_CHILD_NOTIFICATIONS && this.mGroupManager.isChildInGroupWithSummary(expandedNotification)) {
            return true;
        }
        ForegroundServiceController foregroundServiceController = (ForegroundServiceController) Dependency.get(ForegroundServiceController.class);
        if (foregroundServiceController.isDungeonNotification(expandedNotification) && !foregroundServiceController.isDungeonNeededForUser(expandedNotification.getUserId())) {
            return true;
        }
        if (!Constants.DEBUG || !NotificationUtil.isSystemNotification(expandedNotification)) {
            return false;
        }
        return true;
    }

    private boolean filterBubble(Entry entry) {
        return entry.isBubble() && !entry.showInShadeWhenBubble();
    }

    public static boolean showNotificationEvenIfUnprovisioned(StatusBarNotification statusBarNotification) {
        return showNotificationEvenIfUnprovisioned(AppGlobals.getPackageManager(), statusBarNotification);
    }

    @VisibleForTesting
    static boolean showNotificationEvenIfUnprovisioned(IPackageManager iPackageManager, StatusBarNotification statusBarNotification) {
        return ("android".equals(statusBarNotification.getPackageName()) || checkUidPermission(iPackageManager, "android.permission.NOTIFICATION_DURING_SETUP", statusBarNotification.getUid()) == 0) && statusBarNotification.getNotification().extras.getBoolean("android.allowDuringSetup");
    }

    private static int checkUidPermission(IPackageManager iPackageManager, String str, int i) {
        try {
            return iPackageManager.checkUidPermission(str, i);
        } catch (RemoteException unused) {
            return -1;
        }
    }

    public void dump(PrintWriter printWriter, String str) {
        int size = this.mSortedAndFiltered.size();
        printWriter.print(str);
        printWriter.println("active notifications: " + size);
        int i = 0;
        while (i < size) {
            dumpEntry(printWriter, str, i, this.mSortedAndFiltered.get(i));
            i++;
        }
        synchronized (this.mEntries) {
            int size2 = this.mEntries.size();
            printWriter.print(str);
            printWriter.println("inactive notifications: " + (size2 - i));
            for (int i2 = 0; i2 < size2; i2++) {
                Entry valueAt = this.mEntries.valueAt(i2);
                if (!this.mSortedAndFiltered.contains(valueAt)) {
                    dumpEntry(printWriter, str, i2, valueAt);
                }
            }
        }
    }

    private void dumpEntry(PrintWriter printWriter, String str, int i, Entry entry) {
        this.mRankingMap.getRanking(entry.key, this.mTmpRanking);
        printWriter.print(str);
        printWriter.println("  [" + i + "] " + entry.notification);
    }

    private void checkNotificationLimit(String str) {
        Entry entry;
        int i;
        synchronized (this.mEntries) {
            int size = this.mEntries.size();
            entry = null;
            i = 0;
            for (int i2 = 0; i2 < size; i2++) {
                Entry valueAt = this.mEntries.valueAt(i2);
                if (valueAt.notification.getPackageName().equals(str)) {
                    i++;
                    entry = shouldRemove(entry, valueAt);
                }
            }
        }
        if (i >= 10 && entry != null) {
            performRemoveNotification(entry.notification);
        }
    }

    public void performRemoveNotification(ExpandedNotification expandedNotification) {
        try {
            StatusBarServiceCompat.onNotificationClear(this.mBarService, expandedNotification.getBasePkg(), expandedNotification.getTag(), expandedNotification.getId(), expandedNotification.getUserId(), expandedNotification.getKey(), this.mHeadsUpManager.isHeadsUp(expandedNotification.getKey()) ? 1 : 3, 1, NotificationVisibilityCompat.obtain(expandedNotification.getKey(), getRank(expandedNotification.getKey()), getActiveNotifications().size(), true));
        } catch (Exception unused) {
        }
    }

    private Entry shouldRemove(Entry entry, Entry entry2) {
        if (entry == null) {
            return entry2;
        }
        if (entry2 == null) {
            return entry;
        }
        boolean isGroupSummary = entry.notification.getNotification().isGroupSummary();
        boolean isGroupSummary2 = entry2.notification.getNotification().isGroupSummary();
        if (isGroupSummary != isGroupSummary2) {
            if (isGroupSummary) {
                return entry2;
            }
            if (isGroupSummary2) {
                return entry;
            }
        }
        return entry.firstWhen < entry2.firstWhen ? entry : entry2;
    }
}

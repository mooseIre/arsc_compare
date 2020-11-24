package com.android.systemui.statusbar.notification.collection;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.MiuiNotificationSectionsFeatureManager;
import com.android.systemui.statusbar.notification.NotificationEntryManagerLogger;
import com.android.systemui.statusbar.notification.NotificationFilter;
import com.android.systemui.statusbar.notification.collection.provider.HighPriorityProvider;
import com.android.systemui.statusbar.notification.people.PeopleNotificationIdentifier;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import kotlin.Lazy;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: NotificationRankingManager.kt */
public class NotificationRankingManager {
    static final /* synthetic */ KProperty[] $$delegatedProperties;
    private final NotificationGroupManager groupManager;
    /* access modifiers changed from: private */
    public final HeadsUpManager headsUpManager;
    private final HighPriorityProvider highPriorityProvider;
    private final NotificationEntryManagerLogger logger;
    private final Lazy mediaManager$delegate = LazyKt__LazyJVMKt.lazy(new NotificationRankingManager$mediaManager$2(this));
    /* access modifiers changed from: private */
    public final dagger.Lazy<NotificationMediaManager> mediaManagerLazy;
    private final NotificationFilter notifFilter;
    /* access modifiers changed from: private */
    public final PeopleNotificationIdentifier peopleNotificationIdentifier;
    private final Comparator<NotificationEntry> rankingComparator = new NotificationRankingManager$rankingComparator$1(this);
    @Nullable
    private NotificationListenerService.RankingMap rankingMap;
    private final MiuiNotificationSectionsFeatureManager sectionsFeatureManager;

    static {
        PropertyReference1Impl propertyReference1Impl = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(NotificationRankingManager.class), "mediaManager", "getMediaManager()Lcom/android/systemui/statusbar/NotificationMediaManager;");
        Reflection.property1(propertyReference1Impl);
        $$delegatedProperties = new KProperty[]{propertyReference1Impl};
    }

    private final NotificationMediaManager getMediaManager() {
        Lazy lazy = this.mediaManager$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (NotificationMediaManager) lazy.getValue();
    }

    public NotificationRankingManager(@NotNull dagger.Lazy<NotificationMediaManager> lazy, @NotNull NotificationGroupManager notificationGroupManager, @NotNull HeadsUpManager headsUpManager2, @NotNull NotificationFilter notificationFilter, @NotNull NotificationEntryManagerLogger notificationEntryManagerLogger, @NotNull MiuiNotificationSectionsFeatureManager miuiNotificationSectionsFeatureManager, @NotNull PeopleNotificationIdentifier peopleNotificationIdentifier2, @NotNull HighPriorityProvider highPriorityProvider2) {
        Intrinsics.checkParameterIsNotNull(lazy, "mediaManagerLazy");
        Intrinsics.checkParameterIsNotNull(notificationGroupManager, "groupManager");
        Intrinsics.checkParameterIsNotNull(headsUpManager2, "headsUpManager");
        Intrinsics.checkParameterIsNotNull(notificationFilter, "notifFilter");
        Intrinsics.checkParameterIsNotNull(notificationEntryManagerLogger, "logger");
        Intrinsics.checkParameterIsNotNull(miuiNotificationSectionsFeatureManager, "sectionsFeatureManager");
        Intrinsics.checkParameterIsNotNull(peopleNotificationIdentifier2, "peopleNotificationIdentifier");
        Intrinsics.checkParameterIsNotNull(highPriorityProvider2, "highPriorityProvider");
        this.mediaManagerLazy = lazy;
        this.groupManager = notificationGroupManager;
        this.headsUpManager = headsUpManager2;
        this.notifFilter = notificationFilter;
        this.logger = notificationEntryManagerLogger;
        this.sectionsFeatureManager = miuiNotificationSectionsFeatureManager;
        this.peopleNotificationIdentifier = peopleNotificationIdentifier2;
        this.highPriorityProvider = highPriorityProvider2;
    }

    @Nullable
    public final NotificationListenerService.RankingMap getRankingMap() {
        return this.rankingMap;
    }

    /* access modifiers changed from: private */
    public final boolean getUsePeopleFiltering() {
        return this.sectionsFeatureManager.isFilteringEnabled();
    }

    @NotNull
    public final List<NotificationEntry> updateRanking(@Nullable NotificationListenerService.RankingMap rankingMap2, @NotNull Collection<NotificationEntry> collection, @NotNull String str) {
        List<NotificationEntry> filterAndSortLocked;
        Intrinsics.checkParameterIsNotNull(collection, "entries");
        Intrinsics.checkParameterIsNotNull(str, "reason");
        if (rankingMap2 != null) {
            this.rankingMap = rankingMap2;
            updateRankingForEntries(collection);
        }
        synchronized (this) {
            filterAndSortLocked = filterAndSortLocked(collection, str);
        }
        return filterAndSortLocked;
    }

    private final List<NotificationEntry> filterAndSortLocked(Collection<NotificationEntry> collection, String str) {
        this.logger.logFilterAndSort(str);
        List<NotificationEntry> list = SequencesKt___SequencesKt.toList(SequencesKt___SequencesKt.sortedWith(SequencesKt___SequencesKt.sortedWith(SequencesKt___SequencesKt.filterNot(CollectionsKt___CollectionsKt.asSequence(collection), new NotificationRankingManager$filterAndSortLocked$filtered$1(this)), this.rankingComparator), NotificationRankingManagerExKt.getMiuiRankingComparator()));
        for (NotificationEntry notificationEntry : collection) {
            notificationEntry.setBucket(getBucketForEntry(notificationEntry));
        }
        return list;
    }

    /* access modifiers changed from: private */
    public final boolean filter(NotificationEntry notificationEntry) {
        boolean shouldFilterOut = this.notifFilter.shouldFilterOut(notificationEntry);
        if (shouldFilterOut) {
            notificationEntry.resetInitializationTime();
        }
        return shouldFilterOut;
    }

    private final int getBucketForEntry(NotificationEntry notificationEntry) {
        boolean isRowHeadsUp = notificationEntry.isRowHeadsUp();
        boolean isImportantMedia = isImportantMedia(notificationEntry);
        boolean access$isSystemMax = NotificationRankingManagerKt.isSystemMax(notificationEntry);
        if (NotificationRankingManagerKt.isColorizedForegroundService(notificationEntry)) {
            return 3;
        }
        if (!getUsePeopleFiltering() || !isConversation(notificationEntry)) {
            return (isRowHeadsUp || isImportantMedia || access$isSystemMax || isHighPriority(notificationEntry)) ? 5 : 6;
        }
        return 4;
    }

    private final void updateRankingForEntries(Iterable<NotificationEntry> iterable) {
        NotificationListenerService.RankingMap rankingMap2 = this.rankingMap;
        if (rankingMap2 != null) {
            synchronized (iterable) {
                for (NotificationEntry next : iterable) {
                    NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
                    if (rankingMap2.getRanking(next.getKey(), ranking)) {
                        next.setRanking(ranking);
                        String overrideGroupKey = ranking.getOverrideGroupKey();
                        ExpandedNotification sbn = next.getSbn();
                        Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
                        if (!Objects.equals(sbn.getOverrideGroupKey(), overrideGroupKey)) {
                            ExpandedNotification sbn2 = next.getSbn();
                            Intrinsics.checkExpressionValueIsNotNull(sbn2, "entry.sbn");
                            String groupKey = sbn2.getGroupKey();
                            ExpandedNotification sbn3 = next.getSbn();
                            Intrinsics.checkExpressionValueIsNotNull(sbn3, "entry.sbn");
                            boolean isGroup = sbn3.isGroup();
                            ExpandedNotification sbn4 = next.getSbn();
                            Intrinsics.checkExpressionValueIsNotNull(sbn4, "entry.sbn");
                            Notification notification = sbn4.getNotification();
                            Intrinsics.checkExpressionValueIsNotNull(notification, "entry.sbn.notification");
                            boolean isGroupSummary = notification.isGroupSummary();
                            ExpandedNotification sbn5 = next.getSbn();
                            Intrinsics.checkExpressionValueIsNotNull(sbn5, "entry.sbn");
                            sbn5.setOverrideGroupKey(overrideGroupKey);
                            this.groupManager.onEntryUpdated(next, groupKey, isGroup, isGroupSummary);
                        }
                    }
                }
                Unit unit = Unit.INSTANCE;
            }
        }
    }

    /* access modifiers changed from: private */
    public final boolean isImportantMedia(@NotNull NotificationEntry notificationEntry) {
        String key = notificationEntry.getKey();
        NotificationMediaManager mediaManager = getMediaManager();
        Intrinsics.checkExpressionValueIsNotNull(mediaManager, "mediaManager");
        if (Intrinsics.areEqual((Object) key, (Object) mediaManager.getMediaNotificationKey())) {
            NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
            Intrinsics.checkExpressionValueIsNotNull(ranking, "ranking");
            if (ranking.getImportance() > 1) {
                return true;
            }
        }
        return false;
    }

    private final boolean isConversation(@NotNull NotificationEntry notificationEntry) {
        return getPeopleNotificationType(notificationEntry) != 0;
    }

    /* access modifiers changed from: private */
    public final int getPeopleNotificationType(@NotNull NotificationEntry notificationEntry) {
        PeopleNotificationIdentifier peopleNotificationIdentifier2 = this.peopleNotificationIdentifier;
        ExpandedNotification sbn = notificationEntry.getSbn();
        Intrinsics.checkExpressionValueIsNotNull(sbn, "sbn");
        NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
        Intrinsics.checkExpressionValueIsNotNull(ranking, "ranking");
        return peopleNotificationIdentifier2.getPeopleNotificationType(sbn, ranking);
    }

    /* access modifiers changed from: private */
    public final boolean isHighPriority(@NotNull NotificationEntry notificationEntry) {
        return this.highPriorityProvider.isHighPriority(notificationEntry);
    }
}

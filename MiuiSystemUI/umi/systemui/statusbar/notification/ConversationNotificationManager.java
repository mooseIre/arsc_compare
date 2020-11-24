package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.internal.widget.ConversationLayout;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationContentView;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ConversationNotifications.kt */
public final class ConversationNotificationManager {
    /* access modifiers changed from: private */
    public final Context context;
    /* access modifiers changed from: private */
    public final Handler mainHandler;
    /* access modifiers changed from: private */
    public boolean notifPanelCollapsed = true;
    /* access modifiers changed from: private */
    public final NotificationEntryManager notificationEntryManager;
    /* access modifiers changed from: private */
    public final NotificationGroupManager notificationGroupManager;
    /* access modifiers changed from: private */
    public final ConcurrentHashMap<String, ConversationState> states = new ConcurrentHashMap<>();

    public ConversationNotificationManager(@NotNull NotificationEntryManager notificationEntryManager2, @NotNull NotificationGroupManager notificationGroupManager2, @NotNull Context context2, @NotNull Handler handler) {
        Intrinsics.checkParameterIsNotNull(notificationEntryManager2, "notificationEntryManager");
        Intrinsics.checkParameterIsNotNull(notificationGroupManager2, "notificationGroupManager");
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(handler, "mainHandler");
        this.notificationEntryManager = notificationEntryManager2;
        this.notificationGroupManager = notificationGroupManager2;
        this.context = context2;
        this.mainHandler = handler;
        this.notificationEntryManager.addNotificationEntryListener(new NotificationEntryListener(this) {
            final /* synthetic */ ConversationNotificationManager this$0;

            {
                this.this$0 = r1;
            }

            public void onNotificationRankingUpdated(@NotNull NotificationListenerService.RankingMap rankingMap) {
                Sequence<ConversationLayout> sequence;
                NotificationContentView[] layouts;
                Sequence asSequence;
                Sequence flatMap;
                Intrinsics.checkParameterIsNotNull(rankingMap, "rankingMap");
                ConversationNotificationManager$1$onNotificationRankingUpdated$1 conversationNotificationManager$1$onNotificationRankingUpdated$1 = ConversationNotificationManager$1$onNotificationRankingUpdated$1.INSTANCE;
                NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
                Set keySet = this.this$0.states.keySet();
                Intrinsics.checkExpressionValueIsNotNull(keySet, "states.keys");
                for (NotificationEntry notificationEntry : SequencesKt___SequencesKt.mapNotNull(CollectionsKt___CollectionsKt.asSequence(keySet), new ConversationNotificationManager$1$onNotificationRankingUpdated$activeConversationEntries$1(this))) {
                    ExpandedNotification sbn = notificationEntry.getSbn();
                    Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
                    if (rankingMap.getRanking(sbn.getKey(), ranking) && ranking.isConversation()) {
                        NotificationChannel channel = ranking.getChannel();
                        Intrinsics.checkExpressionValueIsNotNull(channel, "ranking.channel");
                        boolean isImportantConversation = channel.isImportantConversation();
                        ExpandableNotificationRow row = notificationEntry.getRow();
                        if (row == null || (layouts = row.getLayouts()) == null || (asSequence = ArraysKt___ArraysKt.asSequence(layouts)) == null || (flatMap = SequencesKt___SequencesKt.flatMap(asSequence, ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$1.INSTANCE)) == null || (sequence = SequencesKt___SequencesKt.mapNotNull(flatMap, ConversationNotificationManager$1$onNotificationRankingUpdated$layouts$2.INSTANCE)) == null) {
                            sequence = SequencesKt__SequencesKt.emptySequence();
                        }
                        boolean z = false;
                        for (ConversationLayout conversationLayout : sequence) {
                            if (isImportantConversation != conversationLayout.isImportantConversation()) {
                                z = true;
                                if (!isImportantConversation || !notificationEntry.isMarkedForUserTriggeredMovement()) {
                                    conversationLayout.setIsImportantConversation(isImportantConversation);
                                } else {
                                    this.this$0.mainHandler.postDelayed(new ConversationNotificationManager$1$onNotificationRankingUpdated$2(conversationLayout, isImportantConversation), (long) 960);
                                }
                            }
                        }
                        if (z) {
                            this.this$0.notificationGroupManager.updateIsolation(notificationEntry);
                        }
                    }
                }
            }

            public void onEntryInflated(@NotNull NotificationEntry notificationEntry) {
                Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
                NotificationListenerService.Ranking ranking = notificationEntry.getRanking();
                Intrinsics.checkExpressionValueIsNotNull(ranking, "entry.ranking");
                if (ranking.isConversation()) {
                    ConversationNotificationManager$1$onEntryInflated$1 conversationNotificationManager$1$onEntryInflated$1 = new ConversationNotificationManager$1$onEntryInflated$1(this, notificationEntry);
                    ExpandableNotificationRow row = notificationEntry.getRow();
                    if (row != null) {
                        row.setOnExpansionChangedListener(new ConversationNotificationManager$1$onEntryInflated$2(notificationEntry, conversationNotificationManager$1$onEntryInflated$1));
                    }
                    ExpandableNotificationRow row2 = notificationEntry.getRow();
                    boolean z = true;
                    if (row2 == null || !row2.isExpanded()) {
                        z = false;
                    }
                    conversationNotificationManager$1$onEntryInflated$1.invoke(z);
                }
            }

            public void onEntryReinflated(@NotNull NotificationEntry notificationEntry) {
                Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
                onEntryInflated(notificationEntry);
            }

            public void onEntryRemoved(@NotNull NotificationEntry notificationEntry, @Nullable NotificationVisibility notificationVisibility, boolean z, int i) {
                Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
                this.this$0.removeTrackedEntry(notificationEntry);
            }
        });
    }

    public final int getUnreadCount(@NotNull NotificationEntry notificationEntry, @NotNull Notification.Builder builder) {
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        Intrinsics.checkParameterIsNotNull(builder, "recoveredBuilder");
        ConversationState compute = this.states.compute(notificationEntry.getKey(), new ConversationNotificationManager$getUnreadCount$1(this, builder, notificationEntry));
        if (compute != null) {
            return compute.getUnreadCount();
        }
        Intrinsics.throwNpe();
        throw null;
    }

    public final void onNotificationPanelExpandStateChanged(boolean z) {
        this.notifPanelCollapsed = z;
        if (!z) {
            Map<K, V> map = MapsKt__MapsKt.toMap(SequencesKt___SequencesKt.mapNotNull(MapsKt___MapsKt.asSequence(this.states), new ConversationNotificationManager$onNotificationPanelExpandStateChanged$expanded$1(this)));
            this.states.replaceAll(new ConversationNotificationManager$onNotificationPanelExpandStateChanged$1(map));
            for (R resetBadgeUi : SequencesKt___SequencesKt.mapNotNull(CollectionsKt___CollectionsKt.asSequence(map.values()), ConversationNotificationManager$onNotificationPanelExpandStateChanged$2.INSTANCE)) {
                resetBadgeUi(resetBadgeUi);
            }
        }
    }

    /* access modifiers changed from: private */
    public final void resetCount(String str) {
        this.states.compute(str, ConversationNotificationManager$resetCount$1.INSTANCE);
    }

    /* access modifiers changed from: private */
    public final void removeTrackedEntry(NotificationEntry notificationEntry) {
        this.states.remove(notificationEntry.getKey());
    }

    /* access modifiers changed from: private */
    public final void resetBadgeUi(ExpandableNotificationRow expandableNotificationRow) {
        Sequence sequence;
        NotificationContentView[] layouts = expandableNotificationRow.getLayouts();
        if (layouts == null || (sequence = ArraysKt___ArraysKt.asSequence(layouts)) == null) {
            sequence = SequencesKt__SequencesKt.emptySequence();
        }
        for (ConversationLayout unreadCount : SequencesKt___SequencesKt.mapNotNull(SequencesKt___SequencesKt.flatMap(sequence, ConversationNotificationManager$resetBadgeUi$1.INSTANCE), ConversationNotificationManager$resetBadgeUi$2.INSTANCE)) {
            unreadCount.setUnreadCount(0);
        }
    }

    /* compiled from: ConversationNotifications.kt */
    private static final class ConversationState {
        @NotNull
        private final Notification notification;
        private final int unreadCount;

        public static /* synthetic */ ConversationState copy$default(ConversationState conversationState, int i, Notification notification2, int i2, Object obj) {
            if ((i2 & 1) != 0) {
                i = conversationState.unreadCount;
            }
            if ((i2 & 2) != 0) {
                notification2 = conversationState.notification;
            }
            return conversationState.copy(i, notification2);
        }

        @NotNull
        public final ConversationState copy(int i, @NotNull Notification notification2) {
            Intrinsics.checkParameterIsNotNull(notification2, "notification");
            return new ConversationState(i, notification2);
        }

        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ConversationState)) {
                return false;
            }
            ConversationState conversationState = (ConversationState) obj;
            return this.unreadCount == conversationState.unreadCount && Intrinsics.areEqual((Object) this.notification, (Object) conversationState.notification);
        }

        public int hashCode() {
            int hashCode = Integer.hashCode(this.unreadCount) * 31;
            Notification notification2 = this.notification;
            return hashCode + (notification2 != null ? notification2.hashCode() : 0);
        }

        @NotNull
        public String toString() {
            return "ConversationState(unreadCount=" + this.unreadCount + ", notification=" + this.notification + ")";
        }

        public ConversationState(int i, @NotNull Notification notification2) {
            Intrinsics.checkParameterIsNotNull(notification2, "notification");
            this.unreadCount = i;
            this.notification = notification2;
        }

        @NotNull
        public final Notification getNotification() {
            return this.notification;
        }

        public final int getUnreadCount() {
            return this.unreadCount;
        }
    }
}

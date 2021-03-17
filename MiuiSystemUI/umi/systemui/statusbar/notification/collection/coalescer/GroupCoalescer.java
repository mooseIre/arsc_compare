package com.android.systemui.statusbar.notification.collection.coalescer;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import com.android.systemui.Dumpable;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.util.concurrency.DelayableExecutor;
import com.android.systemui.util.time.SystemClock;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GroupCoalescer implements Dumpable {
    private final Map<String, EventBatch> mBatches;
    private final SystemClock mClock;
    private final Map<String, CoalescedEvent> mCoalescedEvents;
    private final Comparator<CoalescedEvent> mEventComparator;
    private BatchableNotificationHandler mHandler;
    private final NotificationListener.NotificationHandler mListener;
    private final GroupCoalescerLogger mLogger;
    private final DelayableExecutor mMainExecutor;
    private final long mMaxGroupLingerDuration;
    private final long mMinGroupLingerDuration;

    public interface BatchableNotificationHandler extends NotificationListener.NotificationHandler {
        void onNotificationBatchPosted(List<CoalescedEvent> list);
    }

    public GroupCoalescer(DelayableExecutor delayableExecutor, SystemClock systemClock, GroupCoalescerLogger groupCoalescerLogger) {
        this(delayableExecutor, systemClock, groupCoalescerLogger, 50, 500);
    }

    GroupCoalescer(DelayableExecutor delayableExecutor, SystemClock systemClock, GroupCoalescerLogger groupCoalescerLogger, long j, long j2) {
        this.mCoalescedEvents = new ArrayMap();
        this.mBatches = new ArrayMap();
        this.mListener = new NotificationListener.NotificationHandler() {
            /* class com.android.systemui.statusbar.notification.collection.coalescer.GroupCoalescer.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
            public void onNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
                GroupCoalescer.this.maybeEmitBatch(statusBarNotification);
                GroupCoalescer.this.applyRanking(rankingMap);
                if (GroupCoalescer.this.handleNotificationPosted(statusBarNotification, rankingMap)) {
                    GroupCoalescer.this.mLogger.logEventCoalesced(statusBarNotification.getKey());
                    GroupCoalescer.this.mHandler.onNotificationRankingUpdate(rankingMap);
                    return;
                }
                GroupCoalescer.this.mHandler.onNotificationPosted(statusBarNotification, rankingMap);
            }

            @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
            public void onNotificationRemoved(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap, int i) {
                GroupCoalescer.this.maybeEmitBatch(statusBarNotification);
                GroupCoalescer.this.applyRanking(rankingMap);
                GroupCoalescer.this.mHandler.onNotificationRemoved(statusBarNotification, rankingMap, i);
            }

            @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
            public void onNotificationRankingUpdate(NotificationListenerService.RankingMap rankingMap) {
                GroupCoalescer.this.applyRanking(rankingMap);
                GroupCoalescer.this.mHandler.onNotificationRankingUpdate(rankingMap);
            }

            @Override // com.android.systemui.statusbar.NotificationListener.NotificationHandler
            public void onNotificationsInitialized() {
                GroupCoalescer.this.mHandler.onNotificationsInitialized();
            }
        };
        this.mEventComparator = $$Lambda$GroupCoalescer$M7iIsbJ8YQ8wPCcv2h3sqACpyk.INSTANCE;
        this.mMainExecutor = delayableExecutor;
        this.mClock = systemClock;
        this.mLogger = groupCoalescerLogger;
        this.mMinGroupLingerDuration = j;
        this.mMaxGroupLingerDuration = j2;
    }

    public void attach(NotificationListener notificationListener) {
        notificationListener.addNotificationHandler(this.mListener);
    }

    public void setNotificationHandler(BatchableNotificationHandler batchableNotificationHandler) {
        this.mHandler = batchableNotificationHandler;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void maybeEmitBatch(StatusBarNotification statusBarNotification) {
        CoalescedEvent coalescedEvent = this.mCoalescedEvents.get(statusBarNotification.getKey());
        EventBatch eventBatch = this.mBatches.get(statusBarNotification.getGroupKey());
        if (coalescedEvent != null) {
            GroupCoalescerLogger groupCoalescerLogger = this.mLogger;
            String key = statusBarNotification.getKey();
            EventBatch batch = coalescedEvent.getBatch();
            Objects.requireNonNull(batch);
            groupCoalescerLogger.logEarlyEmit(key, batch.mGroupKey);
            EventBatch batch2 = coalescedEvent.getBatch();
            Objects.requireNonNull(batch2);
            emitBatch(batch2);
        } else if (eventBatch != null && this.mClock.uptimeMillis() - eventBatch.mCreatedTimestamp >= this.mMaxGroupLingerDuration) {
            this.mLogger.logMaxBatchTimeout(statusBarNotification.getKey(), eventBatch.mGroupKey);
            emitBatch(eventBatch);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean handleNotificationPosted(StatusBarNotification statusBarNotification, NotificationListenerService.RankingMap rankingMap) {
        if (this.mCoalescedEvents.containsKey(statusBarNotification.getKey())) {
            throw new IllegalStateException("Notification has already been coalesced: " + statusBarNotification.getKey());
        } else if (!statusBarNotification.isGroup()) {
            return false;
        } else {
            EventBatch orBuildBatch = getOrBuildBatch(statusBarNotification.getGroupKey());
            CoalescedEvent coalescedEvent = new CoalescedEvent(statusBarNotification.getKey(), orBuildBatch.mMembers.size(), statusBarNotification, requireRanking(rankingMap, statusBarNotification.getKey()), orBuildBatch);
            this.mCoalescedEvents.put(coalescedEvent.getKey(), coalescedEvent);
            orBuildBatch.mMembers.add(coalescedEvent);
            resetShortTimeout(orBuildBatch);
            return true;
        }
    }

    private EventBatch getOrBuildBatch(String str) {
        EventBatch eventBatch = this.mBatches.get(str);
        if (eventBatch != null) {
            return eventBatch;
        }
        EventBatch eventBatch2 = new EventBatch(this.mClock.uptimeMillis(), str);
        this.mBatches.put(str, eventBatch2);
        return eventBatch2;
    }

    private void resetShortTimeout(EventBatch eventBatch) {
        Runnable runnable = eventBatch.mCancelShortTimeout;
        if (runnable != null) {
            runnable.run();
        }
        eventBatch.mCancelShortTimeout = this.mMainExecutor.executeDelayed(new Runnable(eventBatch) {
            /* class com.android.systemui.statusbar.notification.collection.coalescer.$$Lambda$GroupCoalescer$CkC530E2KSp8Q8dstQvPigtYz5M */
            public final /* synthetic */ EventBatch f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                GroupCoalescer.this.lambda$resetShortTimeout$0$GroupCoalescer(this.f$1);
            }
        }, this.mMinGroupLingerDuration);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$resetShortTimeout$0 */
    public /* synthetic */ void lambda$resetShortTimeout$0$GroupCoalescer(EventBatch eventBatch) {
        eventBatch.mCancelShortTimeout = null;
        emitBatch(eventBatch);
    }

    private void emitBatch(EventBatch eventBatch) {
        if (eventBatch != this.mBatches.get(eventBatch.mGroupKey)) {
            throw new IllegalStateException("Cannot emit out-of-date batch " + eventBatch.mGroupKey);
        } else if (!eventBatch.mMembers.isEmpty()) {
            Runnable runnable = eventBatch.mCancelShortTimeout;
            if (runnable != null) {
                runnable.run();
                eventBatch.mCancelShortTimeout = null;
            }
            this.mBatches.remove(eventBatch.mGroupKey);
            ArrayList<CoalescedEvent> arrayList = new ArrayList(eventBatch.mMembers);
            for (CoalescedEvent coalescedEvent : arrayList) {
                this.mCoalescedEvents.remove(coalescedEvent.getKey());
                coalescedEvent.setBatch(null);
            }
            arrayList.sort(this.mEventComparator);
            this.mLogger.logEmitBatch(eventBatch.mGroupKey);
            this.mHandler.onNotificationBatchPosted(arrayList);
        } else {
            throw new IllegalStateException("Batch " + eventBatch.mGroupKey + " cannot be empty");
        }
    }

    private NotificationListenerService.Ranking requireRanking(NotificationListenerService.RankingMap rankingMap, String str) {
        NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
        if (rankingMap.getRanking(str, ranking)) {
            return ranking;
        }
        throw new IllegalArgumentException("Ranking map does not contain key " + str);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void applyRanking(NotificationListenerService.RankingMap rankingMap) {
        for (CoalescedEvent coalescedEvent : this.mCoalescedEvents.values()) {
            NotificationListenerService.Ranking ranking = new NotificationListenerService.Ranking();
            if (rankingMap.getRanking(coalescedEvent.getKey(), ranking)) {
                coalescedEvent.setRanking(ranking);
            } else {
                this.mLogger.logMissingRanking(coalescedEvent.getKey());
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        long uptimeMillis = this.mClock.uptimeMillis();
        printWriter.println();
        printWriter.println("Coalesced notifications:");
        int i = 0;
        for (EventBatch eventBatch : this.mBatches.values()) {
            printWriter.println("   Batch " + eventBatch.mGroupKey + ":");
            printWriter.println("       Created " + (uptimeMillis - eventBatch.mCreatedTimestamp) + "ms ago");
            Iterator<CoalescedEvent> it = eventBatch.mMembers.iterator();
            while (it.hasNext()) {
                printWriter.println("       " + it.next().getKey());
                i++;
            }
        }
        if (i != this.mCoalescedEvents.size()) {
            printWriter.println("    ERROR: batches contain " + this.mCoalescedEvents.size() + " events but am tracking " + this.mCoalescedEvents.size() + " total events");
            printWriter.println("    All tracked events:");
            Iterator<CoalescedEvent> it2 = this.mCoalescedEvents.values().iterator();
            while (it2.hasNext()) {
                printWriter.println("        " + it2.next().getKey());
            }
        }
    }

    static /* synthetic */ int lambda$new$1(CoalescedEvent coalescedEvent, CoalescedEvent coalescedEvent2) {
        int compare = Boolean.compare(coalescedEvent2.getSbn().getNotification().isGroupSummary(), coalescedEvent.getSbn().getNotification().isGroupSummary());
        return compare == 0 ? coalescedEvent.getPosition() - coalescedEvent2.getPosition() : compare;
    }
}

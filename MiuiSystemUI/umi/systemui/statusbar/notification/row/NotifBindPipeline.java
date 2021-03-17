package com.android.systemui.statusbar.notification.row;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.util.ArraySet;
import androidx.core.os.CancellationSignal;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener;
import com.android.systemui.statusbar.notification.row.BindRequester;
import com.android.systemui.statusbar.notification.row.BindStage;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class NotifBindPipeline {
    private final Map<NotificationEntry, BindEntry> mBindEntries = new ArrayMap();
    private final NotifCollectionListener mCollectionListener;
    private final NotifBindPipelineLogger mLogger;
    private final Handler mMainHandler;
    private final List<BindCallback> mScratchCallbacksList = new ArrayList();
    private BindStage mStage;

    public interface BindCallback {
        void onBindFinished(NotificationEntry notificationEntry);
    }

    NotifBindPipeline(CommonNotifCollection commonNotifCollection, NotifBindPipelineLogger notifBindPipelineLogger, Looper looper) {
        AnonymousClass1 r0 = new NotifCollectionListener() {
            /* class com.android.systemui.statusbar.notification.row.NotifBindPipeline.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryInit(NotificationEntry notificationEntry) {
                NotifBindPipeline.this.mBindEntries.put(notificationEntry, new BindEntry());
                NotifBindPipeline.this.mStage.createStageParams(notificationEntry);
            }

            @Override // com.android.systemui.statusbar.notification.collection.notifcollection.NotifCollectionListener
            public void onEntryCleanUp(NotificationEntry notificationEntry) {
                ExpandableNotificationRow expandableNotificationRow = ((BindEntry) NotifBindPipeline.this.mBindEntries.remove(notificationEntry)).row;
                if (expandableNotificationRow != null) {
                    NotifBindPipeline.this.mStage.abortStage(notificationEntry, expandableNotificationRow);
                }
                NotifBindPipeline.this.mStage.deleteStageParams(notificationEntry);
                NotifBindPipeline.this.mMainHandler.removeMessages(1, notificationEntry);
            }
        };
        this.mCollectionListener = r0;
        commonNotifCollection.addCollectionListener(r0);
        this.mLogger = notifBindPipelineLogger;
        this.mMainHandler = new NotifBindPipelineHandler(looper);
    }

    public void setStage(BindStage bindStage) {
        this.mLogger.logStageSet(bindStage.getClass().getName());
        this.mStage = bindStage;
        bindStage.setBindRequestListener(new BindRequester.BindRequestListener() {
            /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotifBindPipeline$Ub0DE4cDhgUUqolBUMUBBCvUjYs */

            @Override // com.android.systemui.statusbar.notification.row.BindRequester.BindRequestListener
            public final void onBindRequest(NotificationEntry notificationEntry, CancellationSignal cancellationSignal, NotifBindPipeline.BindCallback bindCallback) {
                NotifBindPipeline.this.onBindRequested(notificationEntry, cancellationSignal, bindCallback);
            }
        });
    }

    public void manageRow(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow) {
        this.mLogger.logManagedRow(notificationEntry.getKey());
        BindEntry bindEntry = getBindEntry(notificationEntry);
        if (bindEntry != null) {
            bindEntry.row = expandableNotificationRow;
            if (bindEntry.invalidated) {
                requestPipelineRun(notificationEntry);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onBindRequested(NotificationEntry notificationEntry, CancellationSignal cancellationSignal, BindCallback bindCallback) {
        BindEntry bindEntry = getBindEntry(notificationEntry);
        if (bindEntry != null) {
            bindEntry.invalidated = true;
            if (bindCallback != null) {
                Set<BindCallback> set = bindEntry.callbacks;
                set.add(bindCallback);
                cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener(set, bindCallback) {
                    /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotifBindPipeline$AMh7TPDBgF4c0_yw5tceqMMco8 */
                    public final /* synthetic */ Set f$0;
                    public final /* synthetic */ NotifBindPipeline.BindCallback f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    @Override // androidx.core.os.CancellationSignal.OnCancelListener
                    public final void onCancel() {
                        this.f$0.remove(this.f$1);
                    }
                });
            }
            requestPipelineRun(notificationEntry);
        }
    }

    private void requestPipelineRun(NotificationEntry notificationEntry) {
        this.mLogger.logRequestPipelineRun(notificationEntry.getKey());
        ExpandableNotificationRow expandableNotificationRow = getBindEntry(notificationEntry).row;
        if (expandableNotificationRow == null) {
            this.mLogger.logRequestPipelineRowNotSet(notificationEntry.getKey());
            return;
        }
        this.mStage.abortStage(notificationEntry, expandableNotificationRow);
        if (!this.mMainHandler.hasMessages(1, notificationEntry)) {
            this.mMainHandler.sendMessage(Message.obtain(this.mMainHandler, 1, notificationEntry));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void startPipeline(NotificationEntry notificationEntry) {
        this.mLogger.logStartPipeline(notificationEntry.getKey());
        if (this.mStage != null) {
            this.mStage.executeStage(notificationEntry, this.mBindEntries.get(notificationEntry).row, new BindStage.StageCallback() {
                /* class com.android.systemui.statusbar.notification.row.$$Lambda$NotifBindPipeline$3PgyvwQAf2ulPzCouWU3DaPqlCo */

                @Override // com.android.systemui.statusbar.notification.row.BindStage.StageCallback
                public final void onStageFinished(NotificationEntry notificationEntry) {
                    NotifBindPipeline.this.lambda$startPipeline$1$NotifBindPipeline(notificationEntry);
                }
            });
            return;
        }
        throw new IllegalStateException("No stage was ever set on the pipeline");
    }

    /* access modifiers changed from: private */
    /* renamed from: onPipelineComplete */
    public void lambda$startPipeline$1(NotificationEntry notificationEntry) {
        BindEntry bindEntry = getBindEntry(notificationEntry);
        Set<BindCallback> set = bindEntry.callbacks;
        this.mLogger.logFinishedPipeline(notificationEntry.getKey(), set.size());
        bindEntry.invalidated = false;
        this.mScratchCallbacksList.addAll(set);
        set.clear();
        for (int i = 0; i < this.mScratchCallbacksList.size(); i++) {
            this.mScratchCallbacksList.get(i).onBindFinished(notificationEntry);
        }
        this.mScratchCallbacksList.clear();
    }

    private BindEntry getBindEntry(NotificationEntry notificationEntry) {
        return this.mBindEntries.get(notificationEntry);
    }

    /* access modifiers changed from: private */
    public class BindEntry {
        public final Set<BindCallback> callbacks;
        public boolean invalidated;
        public ExpandableNotificationRow row;

        private BindEntry(NotifBindPipeline notifBindPipeline) {
            this.callbacks = new ArraySet();
        }
    }

    private class NotifBindPipelineHandler extends Handler {
        NotifBindPipelineHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (message.what == 1) {
                NotifBindPipeline.this.startPipeline((NotificationEntry) message.obj);
                return;
            }
            throw new IllegalArgumentException("Unknown message type: " + message.what);
        }
    }
}

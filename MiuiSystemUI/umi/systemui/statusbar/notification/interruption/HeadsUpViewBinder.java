package com.android.systemui.statusbar.notification.interruption;

import android.util.ArrayMap;
import androidx.core.os.CancellationSignal;
import com.android.internal.util.NotificationMessagingUtil;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.notification.row.RowContentBindParams;
import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import java.util.Map;

public class HeadsUpViewBinder {
    private final NotificationMessagingUtil mNotificationMessagingUtil;
    private NotificationPresenter mNotificationPresenter;
    private final Map<NotificationEntry, CancellationSignal> mOngoingBindCallbacks = new ArrayMap();
    private final RowContentBindStage mStage;

    HeadsUpViewBinder(NotificationMessagingUtil notificationMessagingUtil, RowContentBindStage rowContentBindStage) {
        this.mNotificationMessagingUtil = notificationMessagingUtil;
        this.mStage = rowContentBindStage;
    }

    public void setPresenter(NotificationPresenter notificationPresenter) {
        this.mNotificationPresenter = notificationPresenter;
    }

    public void bindHeadsUpView(NotificationEntry notificationEntry, NotifBindPipeline.BindCallback bindCallback) {
        RowContentBindParams rowContentBindParams = (RowContentBindParams) this.mStage.getStageParams(notificationEntry);
        rowContentBindParams.setUseIncreasedHeadsUpHeight(this.mNotificationMessagingUtil.isImportantMessaging(notificationEntry.getSbn(), notificationEntry.getImportance()) && !this.mNotificationPresenter.isPresenterFullyCollapsed());
        rowContentBindParams.requireContentViews(4);
        CancellationSignal requestRebind = this.mStage.requestRebind(notificationEntry, new NotifBindPipeline.BindCallback(bindCallback) {
            /* class com.android.systemui.statusbar.notification.interruption.$$Lambda$HeadsUpViewBinder$6G6TWnHGbt5lFbnq41xlKU8ib20 */
            public final /* synthetic */ NotifBindPipeline.BindCallback f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
            public final void onBindFinished(NotificationEntry notificationEntry) {
                HeadsUpViewBinder.lambda$bindHeadsUpView$0(RowContentBindParams.this, this.f$1, notificationEntry);
            }
        });
        abortBindCallback(notificationEntry);
        this.mOngoingBindCallbacks.put(notificationEntry, requestRebind);
    }

    static /* synthetic */ void lambda$bindHeadsUpView$0(RowContentBindParams rowContentBindParams, NotifBindPipeline.BindCallback bindCallback, NotificationEntry notificationEntry) {
        notificationEntry.getRow().setUsesIncreasedHeadsUpHeight(rowContentBindParams.useIncreasedHeadsUpHeight());
        if (bindCallback != null) {
            bindCallback.onBindFinished(notificationEntry);
        }
    }

    public void abortBindCallback(NotificationEntry notificationEntry) {
        CancellationSignal remove = this.mOngoingBindCallbacks.remove(notificationEntry);
        if (remove != null) {
            remove.cancel();
        }
    }

    public void unbindHeadsUpView(NotificationEntry notificationEntry) {
        abortBindCallback(notificationEntry);
        ((RowContentBindParams) this.mStage.getStageParams(notificationEntry)).markContentViewsFreeable(4);
        this.mStage.requestRebind(notificationEntry, null);
    }
}

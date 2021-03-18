package com.android.systemui.statusbar.notification.collection.inflation;

import android.content.Context;
import android.view.ViewGroup;
import com.android.internal.util.NotificationMessagingUtil;
import com.android.systemui.statusbar.NotificationLockscreenUserManager;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.NotificationUiAdjustment;
import com.android.systemui.statusbar.notification.InflationException;
import com.android.systemui.statusbar.notification.NotificationClicker;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.icon.MiuiIconManager;
import com.android.systemui.statusbar.notification.interruption.NotificationInterruptStateProvider;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRowController;
import com.android.systemui.statusbar.notification.row.NotifBindPipeline;
import com.android.systemui.statusbar.notification.row.NotificationRowContentBinder;
import com.android.systemui.statusbar.notification.row.RowContentBindParams;
import com.android.systemui.statusbar.notification.row.RowContentBindStage;
import com.android.systemui.statusbar.notification.row.RowInflaterTask;
import com.android.systemui.statusbar.notification.row.dagger.ExpandableNotificationRowComponent;
import com.android.systemui.statusbar.notification.stack.NotificationListContainer;
import java.util.Objects;
import javax.inject.Provider;

public class NotificationRowBinderImpl implements NotificationRowBinder {
    private BindRowCallback mBindRowCallback;
    private final Context mContext;
    private final ExpandableNotificationRowComponent.Builder mExpandableNotificationRowComponentBuilder;
    private final MiuiIconManager mIconManager;
    private NotificationListContainer mListContainer;
    private final LowPriorityInflationHelper mLowPriorityInflationHelper;
    private final NotificationMessagingUtil mMessagingUtil;
    private final NotifBindPipeline mNotifBindPipeline;
    private NotificationClicker mNotificationClicker;
    private final NotificationLockscreenUserManager mNotificationLockscreenUserManager;
    private final NotificationRemoteInputManager mNotificationRemoteInputManager;
    private NotificationPresenter mPresenter;
    private final RowContentBindStage mRowContentBindStage;
    private final Provider<RowInflaterTask> mRowInflaterTaskProvider;

    public interface BindRowCallback {
        void onBindRow(ExpandableNotificationRow expandableNotificationRow);
    }

    public NotificationRowBinderImpl(Context context, NotificationMessagingUtil notificationMessagingUtil, NotificationRemoteInputManager notificationRemoteInputManager, NotificationLockscreenUserManager notificationLockscreenUserManager, NotifBindPipeline notifBindPipeline, RowContentBindStage rowContentBindStage, NotificationInterruptStateProvider notificationInterruptStateProvider, Provider<RowInflaterTask> provider, ExpandableNotificationRowComponent.Builder builder, MiuiIconManager miuiIconManager, LowPriorityInflationHelper lowPriorityInflationHelper) {
        this.mContext = context;
        this.mNotifBindPipeline = notifBindPipeline;
        this.mRowContentBindStage = rowContentBindStage;
        this.mMessagingUtil = notificationMessagingUtil;
        this.mNotificationRemoteInputManager = notificationRemoteInputManager;
        this.mNotificationLockscreenUserManager = notificationLockscreenUserManager;
        this.mRowInflaterTaskProvider = provider;
        this.mExpandableNotificationRowComponentBuilder = builder;
        this.mIconManager = miuiIconManager;
        this.mLowPriorityInflationHelper = lowPriorityInflationHelper;
    }

    public void setUpWithPresenter(NotificationPresenter notificationPresenter, NotificationListContainer notificationListContainer, BindRowCallback bindRowCallback) {
        this.mPresenter = notificationPresenter;
        this.mListContainer = notificationListContainer;
        this.mBindRowCallback = bindRowCallback;
        this.mIconManager.attach();
    }

    public void setNotificationClicker(NotificationClicker notificationClicker) {
        this.mNotificationClicker = notificationClicker;
    }

    @Override // com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinder
    public void inflateViews(NotificationEntry notificationEntry, Runnable runnable, NotificationRowContentBinder.InflationCallback inflationCallback) throws InflationException {
        ViewGroup viewParentForNotification = this.mListContainer.getViewParentForNotification(notificationEntry);
        if (notificationEntry.rowExists()) {
            this.mIconManager.updateIcons(notificationEntry);
            ExpandableNotificationRow row = notificationEntry.getRow();
            row.reset();
            updateRow(notificationEntry, row);
            inflateContentViews(notificationEntry, row, inflationCallback);
            notificationEntry.getRowController().setOnDismissRunnable(runnable);
            return;
        }
        this.mIconManager.createIcons(notificationEntry);
        this.mRowInflaterTaskProvider.get().inflate(this.mContext, viewParentForNotification, notificationEntry, new RowInflaterTask.RowInflationFinishedListener(notificationEntry, runnable, inflationCallback) {
            /* class com.android.systemui.statusbar.notification.collection.inflation.$$Lambda$NotificationRowBinderImpl$SFe64hfrb5m8XyXzSK440usXoQw */
            public final /* synthetic */ NotificationEntry f$1;
            public final /* synthetic */ Runnable f$2;
            public final /* synthetic */ NotificationRowContentBinder.InflationCallback f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.systemui.statusbar.notification.row.RowInflaterTask.RowInflationFinishedListener
            public final void onInflationFinished(ExpandableNotificationRow expandableNotificationRow) {
                NotificationRowBinderImpl.this.lambda$inflateViews$0$NotificationRowBinderImpl(this.f$1, this.f$2, this.f$3, expandableNotificationRow);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$inflateViews$0 */
    public /* synthetic */ void lambda$inflateViews$0$NotificationRowBinderImpl(NotificationEntry notificationEntry, Runnable runnable, NotificationRowContentBinder.InflationCallback inflationCallback, ExpandableNotificationRow expandableNotificationRow) {
        ExpandableNotificationRowController expandableNotificationRowController = this.mExpandableNotificationRowComponentBuilder.expandableNotificationRow(expandableNotificationRow).notificationEntry(notificationEntry).onDismissRunnable(runnable).rowContentBindStage(this.mRowContentBindStage).onExpandClickListener(this.mPresenter).build().getExpandableNotificationRowController();
        expandableNotificationRowController.init();
        notificationEntry.setRowController(expandableNotificationRowController);
        bindRow(notificationEntry, expandableNotificationRow);
        updateRow(notificationEntry, expandableNotificationRow);
        inflateContentViews(notificationEntry, expandableNotificationRow, inflationCallback);
    }

    private void bindRow(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow) {
        this.mListContainer.bindRow(expandableNotificationRow);
        this.mNotificationRemoteInputManager.bindRow(expandableNotificationRow);
        expandableNotificationRow.setOnActivatedListener(this.mPresenter);
        notificationEntry.setRow(expandableNotificationRow);
        expandableNotificationRow.setEntry(notificationEntry);
        this.mNotifBindPipeline.manageRow(notificationEntry, expandableNotificationRow);
        this.mBindRowCallback.onBindRow(expandableNotificationRow);
    }

    @Override // com.android.systemui.statusbar.notification.collection.inflation.NotificationRowBinder
    public void onNotificationRankingUpdated(NotificationEntry notificationEntry, Integer num, NotificationUiAdjustment notificationUiAdjustment, NotificationUiAdjustment notificationUiAdjustment2, NotificationRowContentBinder.InflationCallback inflationCallback) {
        if (NotificationUiAdjustment.needReinflate(notificationUiAdjustment, notificationUiAdjustment2)) {
            if (notificationEntry.rowExists()) {
                ExpandableNotificationRow row = notificationEntry.getRow();
                row.reset();
                updateRow(notificationEntry, row);
                inflateContentViews(notificationEntry, row, inflationCallback);
            }
        } else if (num != null && notificationEntry.getImportance() != num.intValue() && notificationEntry.rowExists()) {
            notificationEntry.getRow().onNotificationRankingUpdated();
        }
    }

    private void updateRow(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow) {
        int i = notificationEntry.targetSdk;
        expandableNotificationRow.setLegacy(i >= 9 && i < 21);
        NotificationClicker notificationClicker = this.mNotificationClicker;
        Objects.requireNonNull(notificationClicker);
        notificationClicker.register(expandableNotificationRow, notificationEntry.getSbn());
    }

    private void inflateContentViews(NotificationEntry notificationEntry, ExpandableNotificationRow expandableNotificationRow, NotificationRowContentBinder.InflationCallback inflationCallback) {
        boolean isImportantMessaging = this.mMessagingUtil.isImportantMessaging(notificationEntry.getSbn(), notificationEntry.getImportance());
        boolean shouldUseLowPriorityView = this.mLowPriorityInflationHelper.shouldUseLowPriorityView(notificationEntry);
        RowContentBindParams rowContentBindParams = (RowContentBindParams) this.mRowContentBindStage.getStageParams(notificationEntry);
        rowContentBindParams.setUseIncreasedCollapsedHeight(isImportantMessaging);
        rowContentBindParams.setUseLowPriority(shouldUseLowPriorityView);
        expandableNotificationRow.setNeedsRedaction(this.mNotificationLockscreenUserManager.needsRedaction(notificationEntry));
        rowContentBindParams.rebindAllContentViews();
        this.mRowContentBindStage.requestRebind(notificationEntry, new NotifBindPipeline.BindCallback(isImportantMessaging, shouldUseLowPriorityView, inflationCallback) {
            /* class com.android.systemui.statusbar.notification.collection.inflation.$$Lambda$NotificationRowBinderImpl$M_8U_SW8MSCq5I74bBphTK6ZY4 */
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ NotificationRowContentBinder.InflationCallback f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // com.android.systemui.statusbar.notification.row.NotifBindPipeline.BindCallback
            public final void onBindFinished(NotificationEntry notificationEntry) {
                NotificationRowBinderImpl.lambda$inflateContentViews$1(ExpandableNotificationRow.this, this.f$1, this.f$2, this.f$3, notificationEntry);
            }
        });
    }

    static /* synthetic */ void lambda$inflateContentViews$1(ExpandableNotificationRow expandableNotificationRow, boolean z, boolean z2, NotificationRowContentBinder.InflationCallback inflationCallback, NotificationEntry notificationEntry) {
        expandableNotificationRow.setUsesIncreasedCollapsedHeight(z);
        expandableNotificationRow.setIsLowPriority(z2);
        if (inflationCallback != null) {
            inflationCallback.onAsyncInflationFinished(notificationEntry);
        }
    }
}

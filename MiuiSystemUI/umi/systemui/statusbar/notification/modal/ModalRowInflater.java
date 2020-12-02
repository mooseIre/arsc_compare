package com.android.systemui.statusbar.notification.modal;

import android.app.Notification;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0017R$layout;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.ExpandedNotification;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationContentInflater;
import com.android.systemui.statusbar.notification.row.NotificationRowContentBinder;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ModalRowInflater.kt */
public final class ModalRowInflater {
    @NotNull
    public NotificationContentInflater contentInflater;
    @NotNull
    public NotificationRemoteInputManager remoteInputManager;

    @NotNull
    public final ExpandableNotificationRow inflateModalRow(@NotNull Context context, @NotNull NotificationEntry notificationEntry, @NotNull ViewGroup viewGroup) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(notificationEntry, "entry");
        Intrinsics.checkParameterIsNotNull(viewGroup, "parent");
        if (notificationEntry.getModalRow() != null) {
            ExpandableNotificationRow modalRow = notificationEntry.getModalRow();
            Intrinsics.checkExpressionValueIsNotNull(modalRow, "entry.modalRow");
            return modalRow;
        }
        View inflate = LayoutInflater.from(context).inflate(C0017R$layout.status_bar_notification_row, viewGroup, false);
        if (inflate != null) {
            ExpandableNotificationRow expandableNotificationRow = (ExpandableNotificationRow) inflate;
            notificationEntry.getRowController().init(expandableNotificationRow);
            NotificationRemoteInputManager notificationRemoteInputManager = this.remoteInputManager;
            if (notificationRemoteInputManager != null) {
                notificationRemoteInputManager.bindRow(expandableNotificationRow);
                notificationEntry.setModalRow(expandableNotificationRow);
                expandableNotificationRow.setEntry(notificationEntry);
                NotificationRowContentBinder.BindParams bindParams = new NotificationRowContentBinder.BindParams();
                ExpandedNotification sbn = notificationEntry.getSbn();
                Intrinsics.checkExpressionValueIsNotNull(sbn, "entry.sbn");
                Notification.Builder recoverBuilder = Notification.Builder.recoverBuilder(context, sbn.getNotification());
                NotificationContentInflater notificationContentInflater = this.contentInflater;
                if (notificationContentInflater != null) {
                    notificationContentInflater.inflateNotificationViews(notificationEntry, expandableNotificationRow, bindParams, true, 15, recoverBuilder, context);
                    expandableNotificationRow.onNotificationUpdated();
                    ExpandableViewState viewState = expandableNotificationRow.getViewState();
                    if (viewState != null) {
                        ExpandableNotificationRow row = notificationEntry.getRow();
                        Intrinsics.checkExpressionValueIsNotNull(row, "entry.row");
                        viewState.copyFrom(row.getViewState());
                    }
                    ExpandableViewState viewState2 = expandableNotificationRow.getViewState();
                    if (viewState2 != null) {
                        viewState2.height = 0;
                    }
                    expandableNotificationRow.applyViewState();
                    return expandableNotificationRow;
                }
                Intrinsics.throwUninitializedPropertyAccessException("contentInflater");
                throw null;
            }
            Intrinsics.throwUninitializedPropertyAccessException("remoteInputManager");
            throw null;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.statusbar.notification.row.ExpandableNotificationRow");
    }
}

package com.android.systemui.statusbar.notification.row;

import android.app.NotificationChannel;
import android.content.DialogInterface;

/* access modifiers changed from: package-private */
/* compiled from: ChannelEditorDialogController.kt */
public final class ChannelEditorDialogController$initDialog$$inlined$apply$lambda$2 implements DialogInterface.OnShowListener {
    final /* synthetic */ ChannelEditorListView $listView;
    final /* synthetic */ ChannelEditorDialogController this$0;

    ChannelEditorDialogController$initDialog$$inlined$apply$lambda$2(ChannelEditorListView channelEditorListView, ChannelEditorDialogController channelEditorDialogController) {
        this.$listView = channelEditorListView;
        this.this$0 = channelEditorDialogController;
    }

    public final void onShow(DialogInterface dialogInterface) {
        for (NotificationChannel notificationChannel : this.this$0.providedChannels) {
            ChannelEditorListView channelEditorListView = this.$listView;
            if (channelEditorListView != null) {
                channelEditorListView.highlightChannel(notificationChannel);
            }
        }
    }
}

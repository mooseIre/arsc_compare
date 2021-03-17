package com.android.systemui.statusbar.notification.row;

import android.content.DialogInterface;

/* access modifiers changed from: package-private */
/* compiled from: ChannelEditorDialogController.kt */
public final class ChannelEditorDialogController$initDialog$$inlined$apply$lambda$1 implements DialogInterface.OnDismissListener {
    final /* synthetic */ ChannelEditorDialogController this$0;

    ChannelEditorDialogController$initDialog$$inlined$apply$lambda$1(ChannelEditorDialogController channelEditorDialogController) {
        this.this$0 = channelEditorDialogController;
    }

    public final void onDismiss(DialogInterface dialogInterface) {
        OnChannelEditorDialogFinishedListener onFinishListener = this.this$0.getOnFinishListener();
        if (onFinishListener != null) {
            onFinishListener.onChannelEditorDialogFinished();
        }
    }
}

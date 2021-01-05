package com.android.systemui.statusbar.notification.row;

import android.widget.CompoundButton;

/* compiled from: ChannelEditorListView.kt */
final class ChannelEditorListView$updateAppControlRow$1 implements CompoundButton.OnCheckedChangeListener {
    final /* synthetic */ ChannelEditorListView this$0;

    ChannelEditorListView$updateAppControlRow$1(ChannelEditorListView channelEditorListView) {
        this.this$0 = channelEditorListView;
    }

    public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
        this.this$0.getController().proposeSetAppNotificationsEnabled(z);
        this.this$0.updateRows();
    }
}

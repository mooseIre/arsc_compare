package com.android.systemui.statusbar.notification.row;

import android.view.View;

/* compiled from: ChannelEditorListView.kt */
final class ChannelRow$onFinishInflate$2 implements View.OnClickListener {
    final /* synthetic */ ChannelRow this$0;

    ChannelRow$onFinishInflate$2(ChannelRow channelRow) {
        this.this$0 = channelRow;
    }

    public final void onClick(View view) {
        ChannelRow.access$getSwitch$p(this.this$0).toggle();
    }
}

package com.android.systemui.statusbar.notification.row;

import android.app.NotificationChannel;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: ChannelEditorDialogController.kt */
final class ChannelEditorDialogController$padToFourChannels$1 extends Lambda implements Function1<NotificationChannel, Boolean> {
    final /* synthetic */ ChannelEditorDialogController this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ChannelEditorDialogController$padToFourChannels$1(ChannelEditorDialogController channelEditorDialogController) {
        super(1);
        this.this$0 = channelEditorDialogController;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        return Boolean.valueOf(invoke((NotificationChannel) obj));
    }

    public final boolean invoke(@NotNull NotificationChannel notificationChannel) {
        Intrinsics.checkParameterIsNotNull(notificationChannel, "it");
        return this.this$0.getPaddedChannels$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core().contains(notificationChannel);
    }
}

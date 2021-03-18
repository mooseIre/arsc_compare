package com.android.systemui.statusbar.notification.collection.coordinator;

import com.android.systemui.statusbar.notification.collection.NotifPipeline;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConversationCoordinator.kt */
public final class ConversationCoordinator implements Coordinator {
    private final ConversationCoordinator$notificationPromoter$1 notificationPromoter = new ConversationCoordinator$notificationPromoter$1("ConversationCoordinator");

    @Override // com.android.systemui.statusbar.notification.collection.coordinator.Coordinator
    public void attach(@NotNull NotifPipeline notifPipeline) {
        Intrinsics.checkParameterIsNotNull(notifPipeline, "pipeline");
        notifPipeline.addPromoter(this.notificationPromoter);
    }
}

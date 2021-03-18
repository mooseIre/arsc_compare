package com.android.systemui.statusbar.notification.people;

import android.service.notification.StatusBarNotification;
import com.android.systemui.plugins.NotificationPersonExtractorPlugin;
import com.android.systemui.statusbar.policy.ExtensionController;
import java.util.function.Consumer;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: PeopleHubNotificationListener.kt */
public final class NotificationPersonExtractorPluginBoundary implements NotificationPersonExtractor {
    private NotificationPersonExtractorPlugin plugin;

    public NotificationPersonExtractorPluginBoundary(@NotNull ExtensionController extensionController) {
        Intrinsics.checkParameterIsNotNull(extensionController, "extensionController");
        ExtensionController.ExtensionBuilder newExtension = extensionController.newExtension(NotificationPersonExtractorPlugin.class);
        newExtension.withPlugin(NotificationPersonExtractorPlugin.class);
        newExtension.withCallback(new Consumer<NotificationPersonExtractorPlugin>(this) {
            /* class com.android.systemui.statusbar.notification.people.NotificationPersonExtractorPluginBoundary.AnonymousClass1 */
            final /* synthetic */ NotificationPersonExtractorPluginBoundary this$0;

            {
                this.this$0 = r1;
            }

            public final void accept(NotificationPersonExtractorPlugin notificationPersonExtractorPlugin) {
                this.this$0.plugin = notificationPersonExtractorPlugin;
            }
        });
        this.plugin = (NotificationPersonExtractorPlugin) newExtension.build().get();
    }

    @Override // com.android.systemui.statusbar.notification.people.NotificationPersonExtractor
    public boolean isPersonNotification(@NotNull StatusBarNotification statusBarNotification) {
        Intrinsics.checkParameterIsNotNull(statusBarNotification, "sbn");
        NotificationPersonExtractorPlugin notificationPersonExtractorPlugin = this.plugin;
        if (notificationPersonExtractorPlugin != null) {
            return notificationPersonExtractorPlugin.isPersonNotification(statusBarNotification);
        }
        return false;
    }
}

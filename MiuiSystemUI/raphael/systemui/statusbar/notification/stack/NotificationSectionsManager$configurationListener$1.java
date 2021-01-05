package com.android.systemui.statusbar.notification.stack;

import android.view.LayoutInflater;
import com.android.systemui.statusbar.policy.ConfigurationController;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: NotificationSectionsManager.kt */
public final class NotificationSectionsManager$configurationListener$1 implements ConfigurationController.ConfigurationListener {
    final /* synthetic */ NotificationSectionsManager this$0;

    NotificationSectionsManager$configurationListener$1(NotificationSectionsManager notificationSectionsManager) {
        this.this$0 = notificationSectionsManager;
    }

    public void onLocaleListChanged() {
        NotificationSectionsManager notificationSectionsManager = this.this$0;
        LayoutInflater from = LayoutInflater.from(notificationSectionsManager.getParent().getContext());
        Intrinsics.checkExpressionValueIsNotNull(from, "LayoutInflater.from(parent.context)");
        notificationSectionsManager.reinflateViews(from);
    }
}
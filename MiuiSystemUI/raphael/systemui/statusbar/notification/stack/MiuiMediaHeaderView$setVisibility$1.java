package com.android.systemui.statusbar.notification.stack;

import com.android.systemui.statusbar.notification.MiuiNotificationSectionsManager;

/* compiled from: MiuiMediaHeaderView.kt */
final class MiuiMediaHeaderView$setVisibility$1 implements Runnable {
    final /* synthetic */ MiuiMediaHeaderView this$0;

    MiuiMediaHeaderView$setVisibility$1(MiuiMediaHeaderView miuiMediaHeaderView) {
        this.this$0 = miuiMediaHeaderView;
    }

    public final void run() {
        MiuiNotificationSectionsManager notificationSectionsManager = this.this$0.getNotificationSectionsManager();
        if (notificationSectionsManager != null) {
            notificationSectionsManager.updateSectionBoundaries("MediaHeaderView visibility changed");
        }
    }
}

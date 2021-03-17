package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: NotificationSectionsManager.kt */
public final class NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$2 implements View.OnClickListener {
    final /* synthetic */ NotificationSectionsManager this$0;

    NotificationSectionsManager$reinflateViews$$inlined$apply$lambda$2(NotificationSectionsManager notificationSectionsManager) {
        this.this$0 = notificationSectionsManager;
    }

    public final void onClick(View view) {
        NotificationSectionsManager notificationSectionsManager = this.this$0;
        Intrinsics.checkExpressionValueIsNotNull(view, "it");
        notificationSectionsManager.onClearGentleNotifsClick(view);
    }
}

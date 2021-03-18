package com.android.systemui.statusbar.notification.modal;

import android.view.View;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.miui.systemui.events.ModalExitMode;

/* access modifiers changed from: package-private */
/* compiled from: ModalController.kt */
public final class ModalController$enterModal$2 implements View.OnClickListener {
    final /* synthetic */ ModalController this$0;

    ModalController$enterModal$2(ModalController modalController) {
        this.this$0 = modalController;
    }

    public final void onClick(View view) {
        ExpandableNotificationRow row;
        this.this$0.animExitModal(ModalExitMode.MANUAL.name());
        NotificationEntry notificationEntry = this.this$0.entry;
        if (notificationEntry != null && (row = notificationEntry.getRow()) != null) {
            row.performClick();
        }
    }
}

package com.android.systemui.statusbar.notification.stack;

import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.DungeonRow;
import kotlin.jvm.internal.Intrinsics;

/* access modifiers changed from: package-private */
/* compiled from: ForegroundServiceSectionController.kt */
public final class ForegroundServiceSectionController$update$$inlined$apply$lambda$1 implements View.OnClickListener {
    final /* synthetic */ DungeonRow $child;
    final /* synthetic */ NotificationEntry $entry;
    final /* synthetic */ ForegroundServiceSectionController this$0;

    ForegroundServiceSectionController$update$$inlined$apply$lambda$1(DungeonRow dungeonRow, NotificationEntry notificationEntry, LinearLayout linearLayout, ForegroundServiceSectionController foregroundServiceSectionController) {
        this.$child = dungeonRow;
        this.$entry = notificationEntry;
        this.this$0 = foregroundServiceSectionController;
    }

    public final void onClick(View view) {
        ForegroundServiceSectionController foregroundServiceSectionController = this.this$0;
        NotificationEntry entry = this.$child.getEntry();
        if (entry != null) {
            foregroundServiceSectionController.removeEntry(entry);
            this.this$0.update();
            this.$entry.getRow().unDismiss();
            this.$entry.getRow().resetTranslation();
            this.this$0.getEntryManager().updateNotifications("ForegroundServiceSectionController.onClick");
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }
}

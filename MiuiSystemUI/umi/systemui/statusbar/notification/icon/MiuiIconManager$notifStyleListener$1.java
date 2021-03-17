package com.android.systemui.statusbar.notification.icon;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.miui.systemui.NotificationSettings;
import java.util.Collection;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: MiuiIconManager.kt */
public final class MiuiIconManager$notifStyleListener$1 implements NotificationSettings.StyleListener {
    final /* synthetic */ MiuiIconManager this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiIconManager$notifStyleListener$1(MiuiIconManager miuiIconManager) {
        this.this$0 = miuiIconManager;
    }

    @Override // com.miui.systemui.NotificationSettings.StyleListener
    public void onChanged(int i) {
        Collection<NotificationEntry> allNotifs = this.this$0.notifCollection.getAllNotifs();
        Intrinsics.checkExpressionValueIsNotNull(allNotifs, "notifCollection.allNotifs");
        for (T t : allNotifs) {
            MiuiIconManager miuiIconManager = this.this$0;
            Intrinsics.checkExpressionValueIsNotNull(t, "it");
            miuiIconManager.updateIconsSafe(t);
        }
    }
}

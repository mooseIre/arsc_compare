package com.android.systemui.statusbar.notification.collection;

import com.android.systemui.statusbar.notification.collection.ShadeListBuilder;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: NotifViewManager.kt */
public final class NotifViewManager$attach$1 implements ShadeListBuilder.OnRenderListListener {
    final /* synthetic */ NotifViewManager this$0;

    NotifViewManager$attach$1(NotifViewManager notifViewManager) {
        this.this$0 = notifViewManager;
    }

    @Override // com.android.systemui.statusbar.notification.collection.ShadeListBuilder.OnRenderListListener
    public final void onRenderList(@NotNull List<? extends ListEntry> list) {
        Intrinsics.checkParameterIsNotNull(list, "entries");
        this.this$0.onNotifTreeBuilt(list);
    }
}

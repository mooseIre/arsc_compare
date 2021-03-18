package com.android.systemui.statusbar.notification.row;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.systemui.C0015R$id;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: ForegroundServiceDungeonView.kt */
public final class ForegroundServiceDungeonView extends StackScrollerDecorView {
    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    @Nullable
    public View findSecondaryView() {
        return null;
    }

    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public void setVisible(boolean z, boolean z2) {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public ForegroundServiceDungeonView(@NotNull Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    @Nullable
    public View findContentView() {
        return findViewById(C0015R$id.foreground_service_dungeon);
    }
}

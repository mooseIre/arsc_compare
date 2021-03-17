package com.android.systemui;

import android.view.View;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiWallpaperZoomOutService.kt */
final class MiuiWallpaperZoomOutService$onLauncherSupportsZoomChanged$1 extends Lambda implements Function1<View, Unit> {
    final /* synthetic */ boolean $launcherSupportsZoom;
    final /* synthetic */ MiuiWallpaperZoomOutService this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiWallpaperZoomOutService$onLauncherSupportsZoomChanged$1(MiuiWallpaperZoomOutService miuiWallpaperZoomOutService, boolean z) {
        super(1);
        this.this$0 = miuiWallpaperZoomOutService;
        this.$launcherSupportsZoom = z;
    }

    public /* bridge */ /* synthetic */ Object invoke(Object obj) {
        invoke((View) obj);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "it");
        this.this$0.mWallpaperManager.setWallpaperZoomOut(view.getWindowToken(), this.$launcherSupportsZoom ? 0.0f : 1.0f);
    }
}

package com.android.systemui;

import android.view.View;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Lambda;
import org.jetbrains.annotations.NotNull;

/* access modifiers changed from: package-private */
/* compiled from: MiuiWallpaperZoomOutService.kt */
public final class MiuiWallpaperZoomOutService$onLauncherSupportsZoomChanged$1 extends Lambda implements Function1<View, Unit> {
    final /* synthetic */ boolean $launcherSupportsZoom;
    final /* synthetic */ MiuiWallpaperZoomOutService this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiWallpaperZoomOutService$onLauncherSupportsZoomChanged$1(MiuiWallpaperZoomOutService miuiWallpaperZoomOutService, boolean z) {
        super(1);
        this.this$0 = miuiWallpaperZoomOutService;
        this.$launcherSupportsZoom = z;
    }

    /* Return type fixed from 'java.lang.Object' to match base method */
    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object] */
    @Override // kotlin.jvm.functions.Function1
    public /* bridge */ /* synthetic */ Unit invoke(View view) {
        invoke(view);
        return Unit.INSTANCE;
    }

    public final void invoke(@NotNull View view) {
        Intrinsics.checkParameterIsNotNull(view, "it");
        this.this$0.mWallpaperManager.setWallpaperZoomOut(view.getWindowToken(), this.$launcherSupportsZoom ? 0.0f : 1.0f);
    }
}

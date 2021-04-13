package com.android.keyguard.wallpaper;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* access modifiers changed from: package-private */
/* compiled from: MiuiWallpaperClient.kt */
public final class MiuiWallpaperClient$onKeyguardShowingChanged$1 extends Lambda implements Function0<Unit> {
    final /* synthetic */ boolean $showing;
    final /* synthetic */ MiuiWallpaperClient this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiWallpaperClient$onKeyguardShowingChanged$1(MiuiWallpaperClient miuiWallpaperClient, boolean z) {
        super(0);
        this.this$0 = miuiWallpaperClient;
        this.$showing = z;
    }

    @Override // kotlin.jvm.functions.Function0
    public final void invoke() {
        this.this$0.onKeyguardShowingChanged(this.$showing);
    }
}

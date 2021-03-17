package com.android.systemui;

import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* compiled from: MiuiWallpaperZoomOutService.kt */
final class MiuiWallpaperZoomOutService$mStatusBar$2 extends Lambda implements Function0<StatusBar> {
    final /* synthetic */ Lazy $statusBarLazy;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiWallpaperZoomOutService$mStatusBar$2(Lazy lazy) {
        super(0);
        this.$statusBarLazy = lazy;
    }

    public final StatusBar invoke() {
        return (StatusBar) this.$statusBarLazy.get();
    }
}

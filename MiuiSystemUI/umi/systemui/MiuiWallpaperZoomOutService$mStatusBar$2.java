package com.android.systemui;

import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Lambda;

/* access modifiers changed from: package-private */
/* compiled from: MiuiWallpaperZoomOutService.kt */
public final class MiuiWallpaperZoomOutService$mStatusBar$2 extends Lambda implements Function0<StatusBar> {
    final /* synthetic */ Lazy $statusBarLazy;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiWallpaperZoomOutService$mStatusBar$2(Lazy lazy) {
        super(0);
        this.$statusBarLazy = lazy;
    }

    @Override // kotlin.jvm.functions.Function0
    public final StatusBar invoke() {
        return (StatusBar) this.$statusBarLazy.get();
    }
}

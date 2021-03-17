package com.android.keyguard.wallpaper;

import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.util.MiuiTextUtils;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiKeyguardWallpaperControllerImpl.kt */
public final class MiuiKeyguardWallpaperControllerImpl$mAODCallback$1 implements SettingsObserver.Callback {
    final /* synthetic */ MiuiKeyguardWallpaperControllerImpl this$0;

    /* JADX WARN: Incorrect args count in method signature: ()V */
    MiuiKeyguardWallpaperControllerImpl$mAODCallback$1(MiuiKeyguardWallpaperControllerImpl miuiKeyguardWallpaperControllerImpl) {
        this.this$0 = miuiKeyguardWallpaperControllerImpl;
    }

    @Override // com.miui.systemui.SettingsObserver.Callback
    public void onContentChanged(@Nullable String str, @Nullable String str2) {
        if (Intrinsics.areEqual(str, MiuiKeyguardUtils.AOD_MODE)) {
            this.this$0.mAodEnable = MiuiTextUtils.parseBoolean(str2);
        } else if (Intrinsics.areEqual(str, "aod_using_super_wallpaper")) {
            this.this$0.mAodUsingSuperWallpaperStyle = MiuiTextUtils.parseBoolean(str2);
        }
    }
}

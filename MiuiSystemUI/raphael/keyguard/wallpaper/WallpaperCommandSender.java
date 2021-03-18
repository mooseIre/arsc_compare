package com.android.keyguard.wallpaper;

import android.app.ActivityManager;
import android.app.WallpaperManager;
import android.os.Bundle;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUIApplication;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.statusbar.policy.BatteryController;
import com.miui.systemui.DeviceConfig;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: WallpaperCommandSender.kt */
public final class WallpaperCommandSender implements BatteryController.BatteryStateChangeCallback {
    @NotNull
    private final String TAG = "UpdateWallpaperCommand";
    private boolean mIsPowerSave;
    private NotificationShadeWindowView mNotificationShadeWindowView;
    private final UiOffloadThread mUiOffloadThread = ((UiOffloadThread) Dependency.get(UiOffloadThread.class));
    private final WallpaperManager mWallpaperManager = ((WallpaperManager) SystemUIApplication.getContext().getSystemService(WallpaperManager.class));

    public final void setWindowView(@Nullable NotificationShadeWindowView notificationShadeWindowView) {
        this.mNotificationShadeWindowView = notificationShadeWindowView;
    }

    @Override // com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback
    public void onPowerSaveChanged(boolean z) {
        if (this.mIsPowerSave != z) {
            this.mIsPowerSave = z;
        }
    }

    public final void updateKeyguardRatio(float f, long j) {
        if (shouldDispatchEffects()) {
            Bundle bundle = new Bundle();
            bundle.putFloat("ratio", f);
            bundle.putLong("duration", j);
            sendWallpaperCommand("updateKeyguardRatio", bundle);
        }
    }

    private final boolean shouldDispatchEffects() {
        return ActivityManager.isHighEndGfx() && !DeviceConfig.isLowEndDevice() && !this.mIsPowerSave;
    }

    public final void sendWallpaperCommand(@NotNull String str, @Nullable Bundle bundle) {
        Intrinsics.checkParameterIsNotNull(str, "action");
        String str2 = this.TAG;
        Log.d(str2, "action: " + str);
        this.mUiOffloadThread.submit(new WallpaperCommandSender$sendWallpaperCommand$1(this, str, bundle));
    }
}

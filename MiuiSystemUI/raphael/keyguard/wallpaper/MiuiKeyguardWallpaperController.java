package com.android.keyguard.wallpaper;

import android.graphics.drawable.Drawable;
import android.view.View;
import com.android.systemui.statusbar.policy.CallbackController;
import java.io.File;

public interface MiuiKeyguardWallpaperController extends CallbackController<KeyguardWallpaperCallback> {

    public interface KeyguardWallpaperCallback {
        void onKeyguardAnimationUpdated(float f) {
        }

        void onKeyguardWallpaperUpdated(KeyguardWallpaperType keyguardWallpaperType, boolean z, File file, Drawable drawable);

        void onPreWakeUpWithReason(String str) {
        }

        void onWallpaperAnimationUpdated(boolean z) {
        }

        void onWallpaperBlurUpdated(float f) {
        }
    }

    public enum KeyguardWallpaperType {
        AWESOME_LOCK,
        AWESOME_SUPER_LOCK,
        LIVE_SYSTEM,
        LIVE_LOCK,
        PICTORIAL,
        LEGACY_PICTORIAL,
        LEGACY_LIVE_LOCK
    }

    KeyguardWallpaperType getKeyguardWallpaperType();

    boolean isWallpaperSupportsAmbientMode();

    void preWakeUpWithReason(String str);

    void requestWallpaperBlur(String str, float f);

    void setWallpaperScrim(View view);

    void setWallpaperSupportsAmbientMode(boolean z);

    void updateKeyguardRatio(float f, long j);

    void updateWallpaper(boolean z);

    boolean isLegacyKeyguardWallpaper() {
        KeyguardWallpaperType keyguardWallpaperType = getKeyguardWallpaperType();
        return keyguardWallpaperType == KeyguardWallpaperType.AWESOME_LOCK || keyguardWallpaperType == KeyguardWallpaperType.LEGACY_LIVE_LOCK || keyguardWallpaperType == KeyguardWallpaperType.LEGACY_PICTORIAL;
    }

    boolean hasKeyguardWallpaperLayer() {
        KeyguardWallpaperType keyguardWallpaperType = getKeyguardWallpaperType();
        return keyguardWallpaperType == KeyguardWallpaperType.LIVE_LOCK || keyguardWallpaperType == KeyguardWallpaperType.PICTORIAL;
    }
}

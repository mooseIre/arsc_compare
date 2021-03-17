package com.android.keyguard.wallpaper;

import android.content.Context;
import android.content.IntentFilter;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController;
import com.android.systemui.C0011R$color;
import com.android.systemui.Dependency;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.google.android.collect.Lists;
import com.miui.systemui.SettingsObserver;
import java.util.ArrayList;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiKeyguardWallpaperControllerImpl.kt */
public final class MiuiKeyguardWallpaperControllerImpl implements IMiuiKeyguardWallpaperController {
    private static final String KEY_IS_WALLPAPER_COLOR_LIGHT = "is_wallpaper_color_light";
    private static final String KEY_WALLPAPER_BLUR_COLOR = "key_wallpaper_blur_color";
    private static final String KEY_WALLPAPER_INFO = "wallpaper_info";
    private final String TAG = "KeyguardWallpaperControllerImpl";
    private final SettingsObserver.Callback mAODCallback;
    private boolean mAodEnable;
    private boolean mAodUsingSuperWallpaperStyle;
    private final BroadcastDispatcher mBroadcastDispatcher;
    @NotNull
    private final Context mContext;
    private boolean mIsSuperWallpaper;
    private boolean mIsWallpaperColorLight;
    private boolean mSupportsAmbientMode;
    private int mWallpaperBlurColor;
    private final ArrayList<IMiuiKeyguardWallpaperController.IWallpaperChangeCallback> mWallpaperChangeCallbacks;
    private String mWallpaperJsonString;
    private final MiuiKeyguardWallpaperControllerImpl$mWallpaperReceiver$1 mWallpaperReceiver;

    public MiuiKeyguardWallpaperControllerImpl(@NotNull Context context, @NotNull BroadcastDispatcher broadcastDispatcher) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "mBroadcastDispatcher");
        this.mContext = context;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mWallpaperBlurColor = context.getResources().getColor(C0011R$color.wallpaper_des_text_dark_color);
        this.mAODCallback = new MiuiKeyguardWallpaperControllerImpl$mAODCallback$1(this);
        ArrayList<IMiuiKeyguardWallpaperController.IWallpaperChangeCallback> newArrayList = Lists.newArrayList();
        Intrinsics.checkExpressionValueIsNotNull(newArrayList, "Lists.newArrayList()");
        this.mWallpaperChangeCallbacks = newArrayList;
        MiuiKeyguardWallpaperControllerImpl$mWallpaperReceiver$1 miuiKeyguardWallpaperControllerImpl$mWallpaperReceiver$1 = new MiuiKeyguardWallpaperControllerImpl$mWallpaperReceiver$1(this);
        this.mWallpaperReceiver = miuiKeyguardWallpaperControllerImpl$mWallpaperReceiver$1;
        BroadcastDispatcher.registerReceiver$default(this.mBroadcastDispatcher, miuiKeyguardWallpaperControllerImpl$mWallpaperReceiver$1, new IntentFilter("miui.intent.action.LOCK_WALLPAPER_CHANGED"), null, null, 12, null);
        SettingsObserver.Callback callback = this.mAODCallback;
        String str = MiuiKeyguardUtils.AOD_MODE;
        Intrinsics.checkExpressionValueIsNotNull(str, "MiuiKeyguardUtils.AOD_MODE");
        ((SettingsObserver) Dependency.get(SettingsObserver.class)).addCallback(callback, 1, 1, str, "aod_using_super_wallpaper");
    }

    @Override // com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController
    @Nullable
    public String getCurrentWallpaperString() {
        return this.mWallpaperJsonString;
    }

    @Override // com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController
    public void registerWallpaperChangeCallback(@NotNull IMiuiKeyguardWallpaperController.IWallpaperChangeCallback iWallpaperChangeCallback) {
        Intrinsics.checkParameterIsNotNull(iWallpaperChangeCallback, "callback");
        if (!this.mWallpaperChangeCallbacks.contains(iWallpaperChangeCallback)) {
            this.mWallpaperChangeCallbacks.add(iWallpaperChangeCallback);
            iWallpaperChangeCallback.onWallpaperChange(this.mIsWallpaperColorLight);
        }
    }

    @Override // com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController
    public void unregisterWallpaperChangeCallback(@NotNull IMiuiKeyguardWallpaperController.IWallpaperChangeCallback iWallpaperChangeCallback) {
        Intrinsics.checkParameterIsNotNull(iWallpaperChangeCallback, "callback");
        this.mWallpaperChangeCallbacks.remove(iWallpaperChangeCallback);
    }

    @Override // com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController
    public boolean isWallpaperColorLight() {
        return this.mIsWallpaperColorLight;
    }

    @Override // com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController
    public int getWallpaperBlurColor() {
        return this.mWallpaperBlurColor;
    }

    public void setWallpaperSupportsAmbientMode(boolean z) {
        this.mSupportsAmbientMode = z;
    }

    public boolean isWallpaperSupportsAmbientMode() {
        return this.mSupportsAmbientMode;
    }

    @Override // com.android.keyguard.wallpaper.IMiuiKeyguardWallpaperController
    public boolean isSuperWallpaper() {
        return this.mIsSuperWallpaper;
    }

    public final boolean isAodUsingSuperWallpaper() {
        return this.mAodEnable && this.mAodUsingSuperWallpaperStyle;
    }
}

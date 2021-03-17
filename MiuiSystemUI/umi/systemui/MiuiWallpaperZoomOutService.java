package com.android.systemui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.Log;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.shared.system.WallpaperManagerCompat;
import com.android.systemui.statusbar.phone.NotificationShadeWindowView;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.util.ExtensionsKt;
import com.miui.systemui.DebugConfig;
import com.miui.systemui.util.PackageEventController;
import com.miui.systemui.util.PackageEventReceiver;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiWallpaperZoomOutService.kt */
public final class MiuiWallpaperZoomOutService extends BroadcastReceiver implements DeviceProvisionedController.DeviceProvisionedListener, PackageEventReceiver {
    static final /* synthetic */ KProperty[] $$delegatedProperties;
    private final BroadcastDispatcher mBroadcastDispatcher;
    private final Context mContext;
    private final DeviceProvisionedController mDeviceProvisionedController;
    private ComponentName mLauncherComponent;
    private boolean mLauncherSupportWallpaperZoom;
    private final PackageEventController mPackageEventController;
    private final Lazy mStatusBar$delegate;
    private final WallpaperManagerCompat mWallpaperManager;

    static {
        PropertyReference1Impl propertyReference1Impl = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(MiuiWallpaperZoomOutService.class), "mStatusBar", "getMStatusBar()Lcom/android/systemui/statusbar/phone/StatusBar;");
        Reflection.property1(propertyReference1Impl);
        $$delegatedProperties = new KProperty[]{propertyReference1Impl};
    }

    private final StatusBar getMStatusBar() {
        Lazy lazy = this.mStatusBar$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (StatusBar) lazy.getValue();
    }

    public MiuiWallpaperZoomOutService(@NotNull Context context, @NotNull dagger.Lazy<StatusBar> lazy, @NotNull DeviceProvisionedController deviceProvisionedController, @NotNull BroadcastDispatcher broadcastDispatcher, @NotNull PackageEventController packageEventController) {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(lazy, "statusBarLazy");
        Intrinsics.checkParameterIsNotNull(deviceProvisionedController, "deviceProvisionedController");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        Intrinsics.checkParameterIsNotNull(packageEventController, "packageEventController");
        this.mContext = context;
        this.mWallpaperManager = new WallpaperManagerCompat(context);
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mPackageEventController = packageEventController;
        this.mStatusBar$delegate = LazyKt__LazyJVMKt.lazy(new MiuiWallpaperZoomOutService$mStatusBar$2(lazy));
    }

    public final void start() {
        checkLauncherInfo();
        onLauncherSupportsZoomChanged(this.mLauncherSupportWallpaperZoom);
        this.mDeviceProvisionedController.addCallback(this);
        BroadcastDispatcher broadcastDispatcher = this.mBroadcastDispatcher;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED");
        BroadcastDispatcher.registerReceiver$default(broadcastDispatcher, this, intentFilter, null, null, 12, null);
        this.mPackageEventController.addCallback(this);
    }

    public void onReceive(@Nullable Context context, @Nullable Intent intent) {
        String action = intent != null ? intent.getAction() : null;
        if (action != null && action.hashCode() == 1358685446 && action.equals("android.intent.action.ACTION_PREFERRED_ACTIVITY_CHANGED")) {
            checkLauncherInfo();
        }
    }

    @Override // com.miui.systemui.util.PackageEventReceiver
    public void onPackageAdded(int i, @Nullable String str, boolean z) {
        ComponentName componentName = this.mLauncherComponent;
        if (Intrinsics.areEqual(str, componentName != null ? componentName.getPackageName() : null)) {
            evaluateLauncherSupportsZoom();
        }
    }

    @Override // com.android.systemui.statusbar.policy.DeviceProvisionedController.DeviceProvisionedListener
    public void onDeviceProvisionedChanged() {
        checkLauncherInfo();
    }

    private final void checkLauncherInfo() {
        ResolveInfo currentLauncherInfo = getCurrentLauncherInfo(this.mContext);
        ComponentName componentName = this.mLauncherComponent;
        ComponentName componentName2 = null;
        if ((currentLauncherInfo != null ? currentLauncherInfo.activityInfo : null) != null) {
            ActivityInfo activityInfo = currentLauncherInfo.activityInfo;
            componentName2 = new ComponentName(activityInfo.packageName, activityInfo.name);
        }
        this.mLauncherComponent = componentName2;
        if (DebugConfig.DEBUG) {
            Log.i("MiuiWallpaperZoomOutService", "Launcher is: " + this.mLauncherComponent);
        }
        if (!Intrinsics.areEqual(componentName, componentName2)) {
            evaluateLauncherSupportsZoom();
        }
    }

    private final void evaluateLauncherSupportsZoom() {
        boolean z;
        ComponentName componentName = this.mLauncherComponent;
        if (componentName != null) {
            String packageName = componentName.getPackageName();
            Intrinsics.checkExpressionValueIsNotNull(packageName, "currentLauncherComponent.packageName");
            z = supportsZoomOut(packageName);
        } else {
            z = false;
        }
        if (this.mLauncherSupportWallpaperZoom != z) {
            this.mLauncherSupportWallpaperZoom = z;
            onLauncherSupportsZoomChanged(z);
            Log.i("MiuiWallpaperZoomOutService", "launcher changed, supports wallpaper zoom: " + this.mLauncherSupportWallpaperZoom);
        }
    }

    private final void onLauncherSupportsZoomChanged(boolean z) {
        StatusBar mStatusBar = getMStatusBar();
        Intrinsics.checkExpressionValueIsNotNull(mStatusBar, "mStatusBar");
        NotificationShadeWindowView notificationShadeWindowView = mStatusBar.getNotificationShadeWindowView();
        Intrinsics.checkExpressionValueIsNotNull(notificationShadeWindowView, "mStatusBar.notificationShadeWindowView");
        ExtensionsKt.runAfterAttached(notificationShadeWindowView, new MiuiWallpaperZoomOutService$onLauncherSupportsZoomChanged$1(this, z));
    }

    private final boolean supportsZoomOut(String str) {
        PackageInfo packageInfo;
        ApplicationInfo applicationInfo;
        Bundle bundle;
        try {
            packageInfo = this.mContext.getPackageManager().getPackageInfo(str, 128);
        } catch (Exception unused) {
            packageInfo = null;
        }
        if (packageInfo == null || (applicationInfo = packageInfo.applicationInfo) == null || (bundle = applicationInfo.metaData) == null) {
            return false;
        }
        return bundle.getBoolean("miui.supports_wallpaper_zoom_out", false);
    }

    private final ResolveInfo getCurrentLauncherInfo(Context context) {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        return context.getPackageManager().resolveActivity(intent, 0);
    }
}

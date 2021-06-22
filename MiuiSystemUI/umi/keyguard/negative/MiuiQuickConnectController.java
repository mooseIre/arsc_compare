package com.android.keyguard.negative;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.keyguard.injector.KeyguardNegative1PageInjector;
import com.android.systemui.Dependency;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.util.MiuiTextUtils;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiQuickConnectController.kt */
public final class MiuiQuickConnectController implements SettingsObserver.Callback {
    @NotNull
    private final String TAG = "MiuiQuickConnectController";
    @NotNull
    private final String XMYZL_ACTIVITY_NAME = "com.miui.smarthomeplus.UWBEntryActivity";
    @NotNull
    private final String XMYZL_PACKAGE_NAME = "com.miui.smarthomeplus";
    @NotNull
    private final String XMYZL_SWITCH_SETTING_KEY = "settings_uwb_lock_screen_entrance_open";
    private final Context mContext;
    private boolean mIsSupportXMYZL;
    private boolean mIsXMYZLEnable;
    private final MiuiKeyguardUpdateMonitorCallback mKeyguardUpdateMonitorCallback;
    private KeyguardUpdateMonitor mUpdateMonitor;

    public MiuiQuickConnectController(@NotNull Context context) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
        this.mContext = context;
        Object obj = Dependency.get(KeyguardUpdateMonitor.class);
        Intrinsics.checkExpressionValueIsNotNull(obj, "Dependency.get(KeyguardUpdateMonitor::class.java)");
        this.mUpdateMonitor = (KeyguardUpdateMonitor) obj;
        MiuiQuickConnectController$mKeyguardUpdateMonitorCallback$1 miuiQuickConnectController$mKeyguardUpdateMonitorCallback$1 = new MiuiQuickConnectController$mKeyguardUpdateMonitorCallback$1(this);
        this.mKeyguardUpdateMonitorCallback = miuiQuickConnectController$mKeyguardUpdateMonitorCallback$1;
        this.mUpdateMonitor.registerCallback(miuiQuickConnectController$mKeyguardUpdateMonitorCallback$1);
        ((SettingsObserver) Dependency.get(SettingsObserver.class)).addCallbackForUser(this, 0, this.XMYZL_SWITCH_SETTING_KEY);
        initXMYZLRes();
    }

    @Override // com.miui.systemui.SettingsObserver.Callback
    public void onContentChanged(@Nullable String str, @Nullable String str2) {
        if (Intrinsics.areEqual(this.XMYZL_SWITCH_SETTING_KEY, str)) {
            this.mIsXMYZLEnable = MiuiTextUtils.parseBoolean(str2, true);
            MiuiKeyguardMoveLeftViewContainer leftView = ((KeyguardNegative1PageInjector) Dependency.get(KeyguardNegative1PageInjector.class)).getLeftView();
            if (leftView != null) {
                leftView.inflateLeftView();
            }
        }
    }

    /* access modifiers changed from: private */
    public final void initXMYZLRes() {
        this.mIsSupportXMYZL = isSupportXMYZL();
        boolean z = true;
        if (Settings.System.getInt(this.mContext.getContentResolver(), this.XMYZL_SWITCH_SETTING_KEY, 1) != 1) {
            z = false;
        }
        this.mIsXMYZLEnable = z;
    }

    public final boolean isUseXMYZLLeft() {
        return this.mIsSupportXMYZL && this.mIsXMYZLEnable;
    }

    private final boolean isSupportXMYZL() {
        if (this.mContext.getPackageManager().queryIntentActivities(new Intent().setClassName(this.XMYZL_PACKAGE_NAME, this.XMYZL_ACTIVITY_NAME), 0).size() > 0) {
            return true;
        }
        return false;
    }

    public final void launchXMYZLActivity() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(this.XMYZL_PACKAGE_NAME, this.XMYZL_ACTIVITY_NAME));
            intent.addFlags(268435456);
            intent.putExtra("source", "lock_screen");
            this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
            Log.d(this.TAG, "launchXMYZLActivity");
        } catch (ActivityNotFoundException e) {
            String str = this.TAG;
            Log.w(str, "Unable to start xmyzl activity, activity not found " + e);
        }
    }
}

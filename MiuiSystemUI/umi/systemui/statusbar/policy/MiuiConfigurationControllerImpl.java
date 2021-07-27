package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.ConfigurationControllerImpl;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.miui.systemui.graphics.AppIconsManager;
import com.miui.systemui.util.MiuiThemeUtils;
import java.util.ArrayList;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiConfigurationControllerImpl.kt */
public final class MiuiConfigurationControllerImpl extends ConfigurationControllerImpl {
    private int density;
    private int themeChanged;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiConfigurationControllerImpl(@NotNull Context context) {
        super(context);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Resources resources = context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        Configuration configuration = resources.getConfiguration();
        this.themeChanged = configuration.extraConfig.themeChanged;
        this.density = configuration.densityDpi;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController, com.android.systemui.statusbar.phone.ConfigurationControllerImpl
    public void onConfigurationChanged(@NotNull Configuration configuration) {
        Intrinsics.checkParameterIsNotNull(configuration, "newConfig");
        MiuiThemeUtils.updateDefaultSysUiTheme(configuration);
        super.onConfigurationChanged(configuration);
        int i = configuration.extraConfig.themeChanged;
        if (i != this.themeChanged) {
            this.themeChanged = i;
            ((AppIconsManager) Dependency.get(AppIconsManager.class)).clearAll();
            onMiuiThemeChanged(MiuiKeyguardUtils.isDefaultLockScreenTheme(), MiuiThemeUtils.isDefaultSysUiTheme());
        }
        int i2 = configuration.densityDpi;
        if (i2 != this.density) {
            this.density = i2;
            onDensityChanged();
        }
    }

    public final void onMiuiThemeChanged(boolean z, boolean z2) {
        for (ConfigurationController.ConfigurationListener configurationListener : new ArrayList(getListeners())) {
            if (getListeners().contains(configurationListener)) {
                configurationListener.onMiuiThemeChanged(z, z2);
            }
        }
    }

    public final void onDensityChanged() {
        for (ConfigurationController.ConfigurationListener configurationListener : new ArrayList(getListeners())) {
            if (getListeners().contains(configurationListener)) {
                configurationListener.onDensityChanged();
            }
        }
    }
}

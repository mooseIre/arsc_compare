package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.ConfigurationControllerImpl;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.miui.systemui.graphics.AppIconsManager;
import java.util.ArrayList;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiConfigurationControllerImpl.kt */
public final class MiuiConfigurationControllerImpl extends ConfigurationControllerImpl {
    private int themeChanged;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiConfigurationControllerImpl(@NotNull Context context) {
        super(context);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Resources resources = context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        this.themeChanged = resources.getConfiguration().extraConfig.themeChanged;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController, com.android.systemui.statusbar.phone.ConfigurationControllerImpl
    public void onConfigurationChanged(@NotNull Configuration configuration) {
        Intrinsics.checkParameterIsNotNull(configuration, "newConfig");
        super.onConfigurationChanged(configuration);
        int i = configuration.extraConfig.themeChanged;
        if (i != this.themeChanged) {
            this.themeChanged = i;
            ((AppIconsManager) Dependency.get(AppIconsManager.class)).clearAll();
            onMiuiThemeChanged(MiuiKeyguardUtils.isDefaultLockScreenTheme());
        }
    }

    public final void onMiuiThemeChanged(boolean z) {
        for (ConfigurationController.ConfigurationListener configurationListener : new ArrayList(getListeners())) {
            if (getListeners().contains(configurationListener)) {
                configurationListener.onMiuiThemeChanged(z);
            }
        }
    }
}

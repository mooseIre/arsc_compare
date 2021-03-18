package com.android.systemui.statusbar.notification.icon;

import android.content.pm.LauncherApps;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.notification.collection.notifcollection.CommonNotifCollection;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.miui.systemui.SettingsManager;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiIconManager.kt */
public final class MiuiIconManager extends IconManager {
    private final MiuiIconManager$configurationListener$1 configurationListener = new MiuiIconManager$configurationListener$1(this);
    private final CommonNotifCollection notifCollection;
    private final MiuiIconManager$notifStyleListener$1 notifStyleListener = new MiuiIconManager$notifStyleListener$1(this);

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiIconManager(@NotNull CommonNotifCollection commonNotifCollection, @NotNull LauncherApps launcherApps, @NotNull IconBuilder iconBuilder) {
        super(commonNotifCollection, launcherApps, iconBuilder);
        Intrinsics.checkParameterIsNotNull(commonNotifCollection, "notifCollection");
        Intrinsics.checkParameterIsNotNull(launcherApps, "launcherApps");
        Intrinsics.checkParameterIsNotNull(iconBuilder, "iconBuilder");
        this.notifCollection = commonNotifCollection;
    }

    @Override // com.android.systemui.statusbar.notification.icon.IconManager
    public void attach() {
        super.attach();
        ((SettingsManager) Dependency.get(SettingsManager.class)).registerNotifStyleListener(this.notifStyleListener);
        ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this.configurationListener);
    }
}

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.LocaleList;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.util.ArrayList;
import java.util.List;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: ConfigurationControllerImpl.kt */
public class ConfigurationControllerImpl implements ConfigurationController {
    private final Context context;
    private int density;
    private float fontScale;
    private final boolean inCarMode;
    private final Configuration lastConfig = new Configuration();
    @NotNull
    private final List<ConfigurationController.ConfigurationListener> listeners = new ArrayList();
    private LocaleList localeList;
    private int uiMode;

    public ConfigurationControllerImpl(@NotNull Context context2) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Resources resources = context2.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        Configuration configuration = resources.getConfiguration();
        this.context = context2;
        this.fontScale = configuration.fontScale;
        this.density = configuration.densityDpi;
        this.inCarMode = (configuration.uiMode & 15) == 3;
        this.uiMode = configuration.uiMode & 48;
        Intrinsics.checkExpressionValueIsNotNull(configuration, "currentConfig");
        this.localeList = configuration.getLocales();
    }

    @NotNull
    public final List<ConfigurationController.ConfigurationListener> getListeners() {
        return this.listeners;
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController
    public void notifyThemeChanged() {
        for (ConfigurationController.ConfigurationListener configurationListener : new ArrayList(this.listeners)) {
            if (this.listeners.contains(configurationListener)) {
                configurationListener.onThemeChanged();
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController
    public void onConfigurationChanged(@NotNull Configuration configuration) {
        Intrinsics.checkParameterIsNotNull(configuration, "newConfig");
        ArrayList<ConfigurationController.ConfigurationListener> arrayList = new ArrayList(this.listeners);
        for (ConfigurationController.ConfigurationListener configurationListener : arrayList) {
            if (this.listeners.contains(configurationListener)) {
                configurationListener.onConfigChanged(configuration);
            }
        }
        float f = configuration.fontScale;
        int i = configuration.densityDpi;
        int i2 = configuration.uiMode & 48;
        boolean z = i2 != this.uiMode;
        if (!(i == this.density && f == this.fontScale && !(this.inCarMode && z))) {
            for (ConfigurationController.ConfigurationListener configurationListener2 : arrayList) {
                if (this.listeners.contains(configurationListener2)) {
                    configurationListener2.onDensityOrFontScaleChanged();
                }
            }
            this.density = i;
            this.fontScale = f;
        }
        LocaleList locales = configuration.getLocales();
        if (!Intrinsics.areEqual(locales, this.localeList)) {
            this.localeList = locales;
            for (ConfigurationController.ConfigurationListener configurationListener3 : arrayList) {
                if (this.listeners.contains(configurationListener3)) {
                    configurationListener3.onLocaleListChanged();
                }
            }
        }
        if (z) {
            this.context.getTheme().applyStyle(this.context.getThemeResId(), true);
            this.uiMode = i2;
            for (ConfigurationController.ConfigurationListener configurationListener4 : arrayList) {
                if (this.listeners.contains(configurationListener4)) {
                    configurationListener4.onUiModeChanged();
                }
            }
        }
        if ((this.lastConfig.updateFrom(configuration) & Integer.MIN_VALUE) != 0) {
            for (ConfigurationController.ConfigurationListener configurationListener5 : arrayList) {
                if (this.listeners.contains(configurationListener5)) {
                    configurationListener5.onOverlayChanged();
                }
            }
        }
    }

    public void addCallback(@NotNull ConfigurationController.ConfigurationListener configurationListener) {
        Intrinsics.checkParameterIsNotNull(configurationListener, "listener");
        this.listeners.add(configurationListener);
        configurationListener.onDensityOrFontScaleChanged();
    }

    public void removeCallback(@NotNull ConfigurationController.ConfigurationListener configurationListener) {
        Intrinsics.checkParameterIsNotNull(configurationListener, "listener");
        this.listeners.remove(configurationListener);
    }
}

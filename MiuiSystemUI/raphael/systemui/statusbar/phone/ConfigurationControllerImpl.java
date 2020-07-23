package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import com.android.systemui.ConfigurationChangedReceiver;
import com.android.systemui.Util;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ConfigurationControllerImpl implements ConfigurationController, ConfigurationChangedReceiver {
    private int mDensity;
    private float mFontScale;
    private boolean mIsNightMode = false;
    private final ArrayList<ConfigurationController.ConfigurationListener> mListeners = new ArrayList<>();
    private Configuration mPreviousConfig;

    public ConfigurationControllerImpl(Context context) {
        Configuration configuration = context.getResources().getConfiguration();
        this.mFontScale = configuration.fontScale;
        this.mDensity = configuration.densityDpi;
        this.mPreviousConfig = new Configuration();
        this.mPreviousConfig.updateFrom(configuration);
        this.mIsNightMode = isNightMode(configuration);
    }

    public void onConfigurationChanged(Configuration configuration) {
        ArrayList arrayList = new ArrayList(this.mListeners);
        arrayList.forEach(new Consumer(configuration) {
            private final /* synthetic */ Configuration f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ConfigurationControllerImpl.this.lambda$onConfigurationChanged$0$ConfigurationControllerImpl(this.f$1, (ConfigurationController.ConfigurationListener) obj);
            }
        });
        float f = configuration.fontScale;
        int i = configuration.densityDpi;
        boolean isThemeResourcesChanged = Util.isThemeResourcesChanged(this.mPreviousConfig.updateFrom(configuration), configuration.extraConfig.themeChangedFlags);
        this.mIsNightMode = isNightMode(configuration);
        if (i != this.mDensity || this.mFontScale != f || isThemeResourcesChanged) {
            arrayList.forEach(new Consumer() {
                public final void accept(Object obj) {
                    ConfigurationControllerImpl.this.lambda$onConfigurationChanged$1$ConfigurationControllerImpl((ConfigurationController.ConfigurationListener) obj);
                }
            });
            this.mDensity = i;
            this.mFontScale = f;
        }
    }

    public /* synthetic */ void lambda$onConfigurationChanged$0$ConfigurationControllerImpl(Configuration configuration, ConfigurationController.ConfigurationListener configurationListener) {
        if (this.mListeners.contains(configurationListener)) {
            configurationListener.onConfigChanged(configuration);
        }
    }

    public /* synthetic */ void lambda$onConfigurationChanged$1$ConfigurationControllerImpl(ConfigurationController.ConfigurationListener configurationListener) {
        if (this.mListeners.contains(configurationListener)) {
            configurationListener.onDensityOrFontScaleChanged();
        }
    }

    public void addCallback(ConfigurationController.ConfigurationListener configurationListener) {
        this.mListeners.add(configurationListener);
        configurationListener.onDensityOrFontScaleChanged();
    }

    public void removeCallback(ConfigurationController.ConfigurationListener configurationListener) {
        this.mListeners.remove(configurationListener);
    }

    public boolean isNightMode() {
        return this.mIsNightMode;
    }

    private static boolean isNightMode(Configuration configuration) {
        return (configuration.uiMode & 48) == 32;
    }
}

package com.android.systemui.plugins;

import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@ProvidesInterface(action = GlobalActions.ACTION, version = 1)
@DependsOn(target = GlobalActionsManager.class)
public interface GlobalActions extends Plugin {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_GLOBAL_ACTIONS";
    public static final int VERSION = 1;

    @ProvidesInterface(version = 1)
    public interface GlobalActionsManager {
        public static final int VERSION = 1;

        void onGlobalActionsHidden();

        void onGlobalActionsShown();

        void reboot(boolean z);

        void shutdown();
    }

    default void destroy() {
    }

    void showGlobalActions(GlobalActionsManager globalActionsManager);

    default void showShutdownUi(boolean z, String str) {
    }
}

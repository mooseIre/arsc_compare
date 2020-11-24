package com.android.systemui.controlcenter.dagger;

import android.content.Context;
import com.android.systemui.controlcenter.ControlCenter;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.controlcenter.phone.ExpandInfoController;
import com.android.systemui.controlcenter.phone.controls.ControlsPluginManager;
import com.android.systemui.controlcenter.policy.ControlCenterActivityStarter;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.util.InjectionInflationController;

public interface ControlCenterModule {
    static ControlCenter provideControlCenter(Context context, ControlPanelController controlPanelController, StatusBarIconController statusBarIconController, ExpandInfoController expandInfoController, ActivityStarter activityStarter, CommandQueue commandQueue, InjectionInflationController injectionInflationController, SuperSaveModeController superSaveModeController, ControlCenterActivityStarter controlCenterActivityStarter, QSTileHost qSTileHost, ControlPanelWindowManager controlPanelWindowManager, StatusBar statusBar, ControlsPluginManager controlsPluginManager) {
        return new ControlCenter(context, controlPanelController, statusBarIconController, expandInfoController, activityStarter, commandQueue, injectionInflationController, superSaveModeController, controlCenterActivityStarter, qSTileHost, controlPanelWindowManager, statusBar, controlsPluginManager);
    }
}

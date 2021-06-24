package com.android.systemui.statusbar.notification.mediacontrol;

import com.android.systemui.controlcenter.phone.ControlPanelController;

public final class MiuiMediaTransferManager_MembersInjector {
    public static void injectControlPanelController(MiuiMediaTransferManager miuiMediaTransferManager, ControlPanelController controlPanelController) {
        miuiMediaTransferManager.controlPanelController = controlPanelController;
    }
}

package com.android.systemui.statusbar.notification.modal;

import com.android.systemui.controlcenter.ControlCenter;

public final class ModalController_MembersInjector {
    public static void injectModalRowInflater(ModalController modalController, ModalRowInflater modalRowInflater) {
        modalController.modalRowInflater = modalRowInflater;
    }

    public static void injectControllCenter(ModalController modalController, ControlCenter controlCenter) {
        modalController.controllCenter = controlCenter;
    }
}

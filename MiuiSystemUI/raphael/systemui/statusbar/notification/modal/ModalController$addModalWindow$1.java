package com.android.systemui.statusbar.notification.modal;

import android.view.View;

/* compiled from: ModalController.kt */
final class ModalController$addModalWindow$1 implements View.OnClickListener {
    final /* synthetic */ ModalController this$0;

    ModalController$addModalWindow$1(ModalController modalController) {
        this.this$0 = modalController;
    }

    public final void onClick(View view) {
        this.this$0.animExitModal();
    }
}

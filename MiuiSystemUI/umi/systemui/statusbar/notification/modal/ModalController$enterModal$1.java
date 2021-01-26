package com.android.systemui.statusbar.notification.modal;

import android.view.View;
import com.miui.systemui.events.ModalExitMode;

/* compiled from: ModalController.kt */
final class ModalController$enterModal$1 implements View.OnClickListener {
    final /* synthetic */ ModalController this$0;

    ModalController$enterModal$1(ModalController modalController) {
        this.this$0 = modalController;
    }

    public final void onClick(View view) {
        this.this$0.animExitModal(ModalExitMode.MANUAL.name());
    }
}

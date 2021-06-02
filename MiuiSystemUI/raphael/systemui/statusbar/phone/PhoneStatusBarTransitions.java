package com.android.systemui.statusbar.phone;

import android.view.View;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;

public final class PhoneStatusBarTransitions extends BarTransitions {
    private void applyMode(int i, boolean z) {
    }

    public PhoneStatusBarTransitions(PhoneStatusBarView phoneStatusBarView, View view) {
        super(view, C0013R$drawable.status_background);
        phoneStatusBarView.getContext().getResources().getFraction(C0012R$dimen.status_bar_icon_drawing_alpha, 1, 1);
        phoneStatusBarView.findViewById(C0015R$id.status_bar_left_side);
        phoneStatusBarView.findViewById(C0015R$id.statusIcons);
        phoneStatusBarView.findViewById(C0015R$id.battery);
        applyModeBackground(-1, getMode(), false);
        applyMode(getMode(), false);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.phone.BarTransitions
    public void onTransition(int i, int i2, boolean z) {
        super.onTransition(i, i2, z);
        applyMode(i2, z);
    }
}

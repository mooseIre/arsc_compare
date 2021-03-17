package com.android.systemui.statusbar.phone;

import android.view.View;
import com.android.systemui.statusbar.policy.KeyButtonDrawable;

interface RotationButton {
    View getCurrentView();

    KeyButtonDrawable getImageDrawable();

    boolean hide();

    boolean isVisible();

    void setCanShowRotationButton(boolean z) {
    }

    void setDarkIntensity(float f);

    void setOnClickListener(View.OnClickListener onClickListener);

    void setOnHoverListener(View.OnHoverListener onHoverListener);

    void setRotationButtonController(RotationButtonController rotationButtonController);

    boolean show();

    void updateIcon();

    boolean acceptRotationProposal() {
        return getCurrentView() != null;
    }
}

package com.android.systemui.statusbar.phone;

import miuix.animation.base.AnimConfig;

public final class MiuiNotificationPanelViewControllerKt {
    private static final AnimConfig BLUR_ANIM_CONFIG;
    private static final AnimConfig SPRING_ANIM_CONFIG;

    static {
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(AnimConfig.sDefEase);
        BLUR_ANIM_CONFIG = animConfig;
        AnimConfig animConfig2 = new AnimConfig();
        animConfig2.setEase(-2, 0.7f, 0.5f);
        SPRING_ANIM_CONFIG = animConfig2;
    }
}

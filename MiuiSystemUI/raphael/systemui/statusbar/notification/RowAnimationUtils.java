package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import miuix.animation.Folme;
import miuix.animation.base.AnimConfig;

public final class RowAnimationUtils {
    public static final RowAnimationUtils INSTANCE = new RowAnimationUtils();

    private RowAnimationUtils() {
    }

    public final void startTouchAnimationIfNeed(ExpandableNotificationRow expandableNotificationRow, float f) {
        AnimConfig animConfig;
        if (expandableNotificationRow != null) {
            boolean z = false;
            if (f >= ((float) 0)) {
                int hashCode = expandableNotificationRow.hashCode();
                if (expandableNotificationRow.isGroupExpansionChanging()) {
                    Folme.useValue(Integer.valueOf(hashCode)).cancel();
                    if (f != 1.0f) {
                        z = true;
                    }
                    setTouchAnimatingState(expandableNotificationRow, z);
                    return;
                }
                RowAnimationUtils$startTouchAnimationIfNeed$listener$1 rowAnimationUtils$startTouchAnimationIfNeed$listener$1 = new RowAnimationUtils$startTouchAnimationIfNeed$listener$1(f, expandableNotificationRow, "scale", hashCode, Integer.valueOf(hashCode));
                if (f == 1.0f) {
                    animConfig = new AnimConfig();
                    animConfig.setEase(-2, 0.6f, 0.25f);
                    animConfig.addListeners(rowAnimationUtils$startTouchAnimationIfNeed$listener$1);
                } else {
                    animConfig = new AnimConfig();
                    animConfig.setEase(-2, 0.9f, 0.4f);
                    animConfig.addListeners(rowAnimationUtils$startTouchAnimationIfNeed$listener$1);
                }
                Folme.getValueTarget(Integer.valueOf(hashCode)).setMinVisibleChange(0.001f, "scale");
                Folme.useValue(Integer.valueOf(hashCode)).setTo("scale", Float.valueOf(expandableNotificationRow.getScaleX())).to("scale", Float.valueOf(f), animConfig);
            }
        }
    }

    /* access modifiers changed from: public */
    private final void setTouchAnimatingState(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        ExpandableViewState viewState;
        if (expandableNotificationRow != null && (viewState = expandableNotificationRow.getViewState()) != null) {
            viewState.setTouchAnimating(z);
        }
    }
}

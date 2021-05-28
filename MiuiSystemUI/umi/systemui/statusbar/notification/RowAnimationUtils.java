package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import miuix.animation.Folme;
import org.jetbrains.annotations.Nullable;

/* compiled from: RowAnimationUtils.kt */
public final class RowAnimationUtils {
    public static final RowAnimationUtils INSTANCE = new RowAnimationUtils();

    private RowAnimationUtils() {
    }

    public final void startTouchAnimationIfNeed(@Nullable ExpandableNotificationRow expandableNotificationRow, float f) {
        if (expandableNotificationRow != null) {
            boolean z = false;
            if (f >= ((float) 0)) {
                int hashCode = expandableNotificationRow.hashCode();
                if (expandableNotificationRow.isGroupExpansionChanging() || f == expandableNotificationRow.getScaleX()) {
                    Folme.useValue(Integer.valueOf(hashCode)).cancel();
                    if (f != 1.0f) {
                        z = true;
                    }
                    setTouchAnimatingState(expandableNotificationRow, z);
                    return;
                }
                Folme.useValue(Integer.valueOf(hashCode)).cancel();
                Folme.getValueTarget(Integer.valueOf(hashCode)).setMinVisibleChange(0.01f, "scale");
                Folme.useValue(Integer.valueOf(hashCode)).setTo("scale", Float.valueOf(expandableNotificationRow.getScaleX())).addListener(new RowAnimationUtils$startTouchAnimationIfNeed$1(f, expandableNotificationRow, "scale", hashCode, Integer.valueOf(hashCode))).to("scale", Float.valueOf(f));
            }
        }
    }

    /* access modifiers changed from: private */
    public final void setTouchAnimatingState(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        ExpandableViewState viewState;
        if (expandableNotificationRow != null && (viewState = expandableNotificationRow.getViewState()) != null) {
            viewState.setTouchAnimating(z);
        }
    }
}

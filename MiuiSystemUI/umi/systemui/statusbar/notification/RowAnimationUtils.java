package com.android.systemui.statusbar.notification;

import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import kotlin.jvm.internal.Intrinsics;
import miuix.animation.Folme;

public final class RowAnimationUtils {
    public static final RowAnimationUtils INSTANCE = new RowAnimationUtils();

    private RowAnimationUtils() {
    }

    public final void startTouchAnimationIfNeed(ExpandableNotificationRow expandableNotificationRow, float f) {
        if (expandableNotificationRow != null) {
            boolean z = false;
            if (f >= ((float) 0)) {
                if (expandableNotificationRow.isGroupExpansionChanging() || f == expandableNotificationRow.getScaleX()) {
                    if (f != 1.0f) {
                        z = true;
                    }
                    setTouchAnimatingState(expandableNotificationRow, z);
                    return;
                }
                NotificationEntry entry = expandableNotificationRow.getEntry();
                Intrinsics.checkExpressionValueIsNotNull(entry, "row.entry");
                String key = entry.getKey();
                Intrinsics.checkExpressionValueIsNotNull(key, "row.entry.key");
                Folme.useValue(key).cancel();
                Folme.getValueTarget(key).setMinVisibleChange(0.01f, "scale");
                Folme.useValue(key).setTo("scale", Float.valueOf(expandableNotificationRow.getScaleX())).addListener(new RowAnimationUtils$startTouchAnimationIfNeed$1(f, expandableNotificationRow, "scale", key, key)).to("scale", Float.valueOf(f));
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

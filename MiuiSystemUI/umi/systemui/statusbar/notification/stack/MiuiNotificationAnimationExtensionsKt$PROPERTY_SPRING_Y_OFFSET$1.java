package com.android.systemui.statusbar.notification.stack;

import android.util.FloatProperty;
import android.view.ViewParent;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiNotificationAnimationExtensions.kt */
public final class MiuiNotificationAnimationExtensionsKt$PROPERTY_SPRING_Y_OFFSET$1 extends FloatProperty<ExpandableView> {
    MiuiNotificationAnimationExtensionsKt$PROPERTY_SPRING_Y_OFFSET$1(String str) {
        super(str);
    }

    public void setValue(@NotNull ExpandableView expandableView, float f) {
        Intrinsics.checkParameterIsNotNull(expandableView, "view");
        ExpandableViewState viewState = expandableView.getViewState();
        if (viewState != null) {
            viewState.setSpringYOffset((int) f);
        }
        ViewParent parent = expandableView.getParent();
        if (parent instanceof NotificationStackScrollLayout) {
            ((NotificationStackScrollLayout) parent).requestChildrenUpdate();
        }
    }

    @NotNull
    public Float get(@NotNull ExpandableView expandableView) {
        Intrinsics.checkParameterIsNotNull(expandableView, "view");
        ExpandableViewState viewState = expandableView.getViewState();
        return Float.valueOf(viewState != null ? (float) viewState.getSpringYOffset() : 0.0f);
    }
}

package com.android.systemui.statusbar.notification.row.wrapper;

import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;
import com.android.systemui.statusbar.notification.TransformState;
import com.android.systemui.statusbar.notification.row.HybridNotificationView;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: NotificationConversationTemplateViewWrapper.kt */
public final class NotificationConversationTemplateViewWrapper$updateTransformedTypes$1 extends ViewTransformationHelper.CustomTransformation {
    NotificationConversationTemplateViewWrapper$updateTransformedTypes$1() {
    }

    @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
    public boolean transformTo(@NotNull TransformState transformState, @NotNull TransformableView transformableView, float f) {
        Intrinsics.checkParameterIsNotNull(transformState, "ownState");
        Intrinsics.checkParameterIsNotNull(transformableView, "otherView");
        if (transformableView instanceof HybridNotificationView) {
            return false;
        }
        transformState.ensureVisible();
        return true;
    }

    @Override // com.android.systemui.statusbar.ViewTransformationHelper.CustomTransformation
    public boolean transformFrom(@NotNull TransformState transformState, @NotNull TransformableView transformableView, float f) {
        Intrinsics.checkParameterIsNotNull(transformState, "ownState");
        Intrinsics.checkParameterIsNotNull(transformableView, "otherView");
        return transformTo(transformState, transformableView, f);
    }
}

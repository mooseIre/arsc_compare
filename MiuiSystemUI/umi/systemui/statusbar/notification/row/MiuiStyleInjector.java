package com.android.systemui.statusbar.notification.row;

import android.content.res.Resources;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0022R$style;
import com.android.systemui.statusbar.notification.NotificationSettingsHelper;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: MiuiStyleInjector.kt */
public final class MiuiStyleInjector {
    public static final MiuiStyleInjector INSTANCE = new MiuiStyleInjector();

    private MiuiStyleInjector() {
    }

    public final int getHybridNotificationStyle() {
        if (NotificationSettingsHelper.showGoogleStyle()) {
            return C0022R$style.HybridNotification;
        }
        return C0022R$style.HybridNotificationMiui;
    }

    public final void alignConversationIcon(@NotNull ImageView imageView, @NotNull Resources resources) {
        Intrinsics.checkParameterIsNotNull(imageView, "conversationIconView");
        Intrinsics.checkParameterIsNotNull(resources, "resources");
        if (imageView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            if (layoutParams != null) {
                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
                marginLayoutParams.setMarginStart(NotificationSettingsHelper.showMiuiStyle() ? resources.getDimensionPixelSize(C0012R$dimen.notification_hybrid_icon_margin_start) : 0);
                imageView.setLayoutParams(marginLayoutParams);
                return;
            }
            throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
        }
    }
}

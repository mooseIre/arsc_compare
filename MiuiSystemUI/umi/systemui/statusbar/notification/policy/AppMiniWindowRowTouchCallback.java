package com.android.systemui.statusbar.notification.policy;

import android.content.Context;
import com.android.systemui.statusbar.notification.MiniWindowExpandParameters;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.row.MiuiExpandableNotificationRow;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: AppMiniWindowRowTouchHelper.kt */
public interface AppMiniWindowRowTouchCallback {
    boolean canChildBePicked(@NotNull ExpandableView expandableView);

    @Nullable
    ExpandableView getChildAtRawPosition(float f, float f2);

    @NotNull
    Context getContext();

    void onExpandedParamsUpdated(@NotNull MiniWindowExpandParameters miniWindowExpandParameters) {
        Intrinsics.checkParameterIsNotNull(miniWindowExpandParameters, "params");
    }

    void onMiniWindowAppLaunched() {
    }

    void onMiniWindowChildPicked(@NotNull MiuiExpandableNotificationRow miuiExpandableNotificationRow) {
        Intrinsics.checkParameterIsNotNull(miuiExpandableNotificationRow, "child");
    }

    void onMiniWindowReset() {
    }

    void onMiniWindowTrackingEnd() {
    }

    void onMiniWindowTrackingStart() {
    }

    void onMiniWindowTrackingUpdate(float f) {
    }

    void onStartMiniWindowExpandAnimation() {
    }
}

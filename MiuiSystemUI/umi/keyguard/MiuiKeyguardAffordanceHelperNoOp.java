package com.android.keyguard;

import android.content.Context;
import android.view.MotionEvent;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.statusbar.phone.KeyguardAffordanceHelper;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiKeyguardAffordanceHelperNoOp.kt */
public final class MiuiKeyguardAffordanceHelperNoOp extends KeyguardAffordanceHelper {
    public void animateHideLeftRightIcon() {
    }

    public void launchAffordance(boolean z, boolean z2) {
    }

    public void onConfigurationChanged() {
    }

    public void onRtlPropertiesChanged() {
    }

    public boolean onTouchEvent(@Nullable MotionEvent motionEvent) {
        return false;
    }

    public void reset(boolean z) {
    }

    public void updatePreviews() {
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiKeyguardAffordanceHelperNoOp(@NotNull KeyguardAffordanceHelper.Callback callback, @NotNull Context context, @NotNull FalsingManager falsingManager) {
        super(callback, context, falsingManager);
        Intrinsics.checkParameterIsNotNull(callback, "callback");
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(falsingManager, "falsingManager");
    }
}

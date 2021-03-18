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
    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper
    public void animateHideLeftRightIcon() {
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper
    public void launchAffordance(boolean z, boolean z2) {
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper
    public void onConfigurationChanged() {
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper
    public void onRtlPropertiesChanged() {
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper
    public boolean onTouchEvent(@Nullable MotionEvent motionEvent) {
        return false;
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper
    public void reset(boolean z) {
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardAffordanceHelper
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

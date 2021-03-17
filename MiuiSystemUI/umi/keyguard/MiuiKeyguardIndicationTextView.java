package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import com.android.systemui.statusbar.phone.KeyguardIndicationTextView;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiKeyguardIndicationTextView.kt */
public final class MiuiKeyguardIndicationTextView extends KeyguardIndicationTextView {
    private final String TAG;
    private boolean mShouldVisible;

    public MiuiKeyguardIndicationTextView(@NotNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0, 4, null);
    }

    /* JADX INFO: this call moved to the top of the method (can break code semantics) */
    public /* synthetic */ MiuiKeyguardIndicationTextView(Context context, AttributeSet attributeSet, int i, int i2, DefaultConstructorMarker defaultConstructorMarker) {
        this(context, attributeSet, (i2 & 4) != 0 ? 0 : i);
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiKeyguardIndicationTextView(@NotNull Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        Intrinsics.checkParameterIsNotNull(context, "context");
        this.TAG = "KeyguardIndication";
        this.mShouldVisible = true;
    }

    public void setVisibility(int i) {
        if (i != 0 || this.mShouldVisible) {
            super.setVisibility(i);
            return;
        }
        String str = this.TAG;
        Log.e(str, "setVisibility ShouldNotVisible visibility:" + i);
    }

    public final void setVisibilityForSwitchIndication(boolean z) {
        String str = this.TAG;
        Log.e(str, "setVisibilityForSwitchIndication visible:" + z);
        this.mShouldVisible = z;
        if (!z) {
            setVisibility(4);
        }
    }

    @Override // com.android.systemui.statusbar.phone.KeyguardIndicationTextView
    public void switchIndication(@Nullable CharSequence charSequence) {
        String str = this.TAG;
        Log.d(str, " switchIndication:" + charSequence);
        super.switchIndication(charSequence);
        if (!this.mShouldVisible) {
            setVisibility(4);
        }
    }
}

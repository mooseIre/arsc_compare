package com.android.keyguard.negative;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.android.systemui.Dependency;

public class MiuiKeyguardMoveLeftXMYZLView extends MiuiKeyguardMoveLeftBaseView {
    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public void initLeftView() {
    }

    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public void setCustomBackground(Drawable drawable) {
    }

    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public void uploadData() {
    }

    public MiuiKeyguardMoveLeftXMYZLView(Context context) {
        this(context, null);
    }

    public MiuiKeyguardMoveLeftXMYZLView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public boolean isSupportRightMove() {
        return ((MiuiQuickConnectController) Dependency.get(MiuiQuickConnectController.class)).isUseXMYZLLeft();
    }
}

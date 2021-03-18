package com.android.keyguard.negative;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.systemui.Dependency;

public class MiuiKeyguardMoveLeftLockScreenMagazineView extends MiuiKeyguardMoveLeftBaseView {
    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public void initLeftView() {
    }

    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public void uploadData() {
    }

    public MiuiKeyguardMoveLeftLockScreenMagazineView(Context context) {
        this(context, null);
    }

    public MiuiKeyguardMoveLeftLockScreenMagazineView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public boolean isSupportRightMove() {
        return ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isSupportLockScreenMagazineLeft() || ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isLockScreenLeftOverlayAvailable();
    }

    @Override // com.android.keyguard.negative.MiuiKeyguardMoveLeftBaseView
    public void setCustomBackground(Drawable drawable) {
        setBackground(drawable);
    }
}

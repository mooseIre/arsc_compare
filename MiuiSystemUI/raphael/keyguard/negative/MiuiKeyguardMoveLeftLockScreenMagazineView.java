package com.android.keyguard.negative;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.systemui.Dependency;

public class MiuiKeyguardMoveLeftLockScreenMagazineView extends MiuiKeyguardMoveLeftBaseView {
    public void initLeftView() {
    }

    public void uploadData() {
    }

    public MiuiKeyguardMoveLeftLockScreenMagazineView(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiKeyguardMoveLeftLockScreenMagazineView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    public boolean isSupportRightMove() {
        Class cls = LockScreenMagazineController.class;
        return ((LockScreenMagazineController) Dependency.get(cls)).isSupportLockScreenMagazineLeft() || ((LockScreenMagazineController) Dependency.get(cls)).isLockScreenLeftOverlayAvailable();
    }

    public void setCustomBackground(Drawable drawable) {
        setBackground(drawable);
    }
}

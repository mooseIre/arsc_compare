package com.android.keyguard.negative;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;

public abstract class MiuiKeyguardMoveLeftBaseView extends RelativeLayout {
    protected StatusBar mStatusBar;

    public abstract void initLeftView();

    public abstract boolean isSupportRightMove();

    public abstract void setCustomBackground(Drawable drawable);

    public abstract void uploadData();

    public MiuiKeyguardMoveLeftBaseView(Context context) {
        this(context, null);
    }

    public MiuiKeyguardMoveLeftBaseView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mStatusBar = (StatusBar) Dependency.get(StatusBar.class);
    }
}

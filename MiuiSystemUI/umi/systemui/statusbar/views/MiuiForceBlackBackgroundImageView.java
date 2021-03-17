package com.android.systemui.statusbar.views;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.Dependency;
import com.miui.systemui.statusbar.phone.ForceBlackObserver;

public class MiuiForceBlackBackgroundImageView extends ImageView implements ForceBlackObserver.Callback {
    public boolean mForceBlack;
    public int mOrientation;

    public MiuiForceBlackBackgroundImageView(Context context) {
        this(context, null);
    }

    public MiuiForceBlackBackgroundImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        this.mOrientation = configuration.orientation;
        updateVisibility();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mOrientation = getResources().getConfiguration().orientation;
        ((ForceBlackObserver) Dependency.get(ForceBlackObserver.class)).addCallback(this);
        this.mForceBlack = ((ForceBlackObserver) Dependency.get(ForceBlackObserver.class)).isForceBlack();
        updateVisibility();
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ((ForceBlackObserver) Dependency.get(ForceBlackObserver.class)).removeCallback(this);
    }

    @Override // com.miui.systemui.statusbar.phone.ForceBlackObserver.Callback
    public void onForceBlackChange(boolean z, boolean z2) {
        this.mForceBlack = z;
        updateVisibility();
    }

    public void updateVisibility() {
        setVisibility((this.mOrientation != 1 || !this.mForceBlack || !MiuiKeyguardUtils.isDefaultLockScreenTheme()) ? 8 : 0);
    }
}

package com.android.keyguard.charge.container;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import com.android.keyguard.charge.ChargeUtils;
import com.android.keyguard.charge.lollipop.LollipopChargeView;
import com.android.keyguard.charge.video.VideoChargeView;
import com.android.keyguard.charge.wave.WaveChargeView;

public class MiuiChargeContainerView extends FrameLayout {
    private IChargeView mChargeView;

    public MiuiChargeContainerView(Context context) {
        this(context, null);
    }

    public MiuiChargeContainerView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiChargeContainerView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    /* access modifiers changed from: protected */
    public void init(Context context) {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(13);
        setChargeAnimationView();
        addView(this.mChargeView, layoutParams);
    }

    private void setChargeAnimationView() {
        if (ChargeUtils.supportWaveChargeAnimation()) {
            this.mChargeView = new WaveChargeView(getContext());
        } else if (ChargeUtils.supportVideoChargeAnimation()) {
            this.mChargeView = new VideoChargeView(getContext());
        } else {
            this.mChargeView = new LollipopChargeView(getContext());
        }
    }

    public float getVideoTranslationY() {
        return this.mChargeView.getVideoTranslationY();
    }

    public void startContainerAnimation(boolean z) {
        Log.d("MiuiChargeContainerView", "startContainerAnimation: screenOn " + z);
        this.mChargeView.startAnimation(z);
    }

    public void setProgress(int i) {
        this.mChargeView.setProgress(i);
    }

    public void startDismiss(String str) {
        this.mChargeView.startDismiss(str);
    }

    public void switchContainerViewAnimation(int i) {
        Log.d("MiuiChargeContainerView", "switchContainerViewAnimation: chargeSpeed=" + i);
        this.mChargeView.switchContainerViewAnimation(i);
    }
}

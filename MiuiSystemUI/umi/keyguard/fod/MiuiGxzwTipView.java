package com.android.keyguard.fod;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import miui.maml.animation.interpolater.ElasticEaseOutInterpolater;

/* access modifiers changed from: package-private */
public class MiuiGxzwTipView extends FrameLayout {
    private float mFontScale;
    private int mTranslateX;
    private int mTranslateY;
    private TextView mTryAgain;

    public MiuiGxzwTipView(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        setBackgroundColor(0);
        LayoutInflater.from(getContext()).inflate(C0017R$layout.miui_keyguard_gxzw_tip_view, this);
        this.mTryAgain = (TextView) findViewById(C0015R$id.gxzw_anim_try_again);
    }

    public void startTipAnim(boolean z, String str, float f) {
        if (!MiuiGxzwManager.getInstance().isBouncer()) {
            updateFontScale();
            this.mTryAgain.setText(str);
            this.mTryAgain.setVisibility(0);
            this.mTryAgain.setTextColor(z ? -16777216 : -1);
            float f2 = f + ((float) this.mTranslateY);
            if (MiuiGxzwUtils.isLargeFod()) {
                f2 += ((float) MiuiGxzwUtils.GXZW_ICON_HEIGHT) / 2.0f;
            }
            this.mTryAgain.setTranslationY(f2);
            this.mTryAgain.setTranslationX((float) this.mTranslateX);
            new ObjectAnimator();
            TextView textView = this.mTryAgain;
            int i = this.mTranslateX;
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(textView, "translationX", (float) (i + 60), (float) i);
            ofFloat.setDuration(700L);
            ofFloat.setInterpolator(new ElasticEaseOutInterpolater());
            new ObjectAnimator();
            ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(this.mTryAgain, "alpha", 0.0f, 1.0f);
            ofFloat2.setDuration(150L);
            ofFloat2.setInterpolator(new DecelerateInterpolator());
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(ofFloat, ofFloat2);
            animatorSet.start();
        }
    }

    public void stopTipAnim() {
        this.mTryAgain.setVisibility(8);
    }

    public void setTranslate(int i, int i2) {
        this.mTranslateX = i;
        this.mTranslateY = i2;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        updateFontScale();
    }

    private void updateFontScale() {
        Configuration configuration = getResources().getConfiguration();
        if (this.mFontScale != configuration.fontScale) {
            this.mTryAgain.setTextSize(0, (float) getResources().getDimensionPixelSize(C0012R$dimen.gxzw_tip_font_size));
            this.mFontScale = configuration.fontScale;
        }
    }
}

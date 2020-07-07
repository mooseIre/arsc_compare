package com.android.systemui.recents.misc;

import android.view.View;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import java.util.ArrayList;
import java.util.List;

public class SpringAnimationImpl {
    List<SpringAnimation> mAllSpringAnim;
    SpringAnimation mAlphaSpringAnim;
    SpringAnimation mScaleXSpringAnim;
    SpringAnimation mScaleYSpringAnim;
    View mTargetView;
    SpringAnimation mTransXSpringAnim;
    SpringAnimation mTransYSpringAnim;

    public SpringAnimationImpl(View view) {
        this.mTargetView = view;
    }

    public SpringAnimation getTranslationXSpringAnim() {
        if (this.mTransXSpringAnim == null) {
            this.mTransXSpringAnim = SpringAnimationUtils.getInstance().createDefaultSpringAnim(this.mTargetView, DynamicAnimation.TRANSLATION_X, 0.0f);
        }
        return this.mTransXSpringAnim;
    }

    public SpringAnimation getTranslationYSpringAnim() {
        if (this.mTransYSpringAnim == null) {
            this.mTransYSpringAnim = SpringAnimationUtils.getInstance().createDefaultSpringAnim(this.mTargetView, DynamicAnimation.TRANSLATION_Y, 0.0f);
        }
        return this.mTransYSpringAnim;
    }

    public SpringAnimation getScaleXSpringAnim() {
        if (this.mScaleXSpringAnim == null) {
            this.mScaleXSpringAnim = SpringAnimationUtils.getInstance().createDefaultSpringAnim(this.mTargetView, DynamicAnimation.SCALE_X, 1.0f);
            this.mScaleXSpringAnim.setMinimumVisibleChange(0.002f);
        }
        return this.mScaleXSpringAnim;
    }

    public SpringAnimation getScaleYSpringAnim() {
        if (this.mScaleYSpringAnim == null) {
            this.mScaleYSpringAnim = SpringAnimationUtils.getInstance().createDefaultSpringAnim(this.mTargetView, DynamicAnimation.SCALE_Y, 1.0f);
            this.mScaleYSpringAnim.setMinimumVisibleChange(0.002f);
        }
        return this.mScaleYSpringAnim;
    }

    public SpringAnimation getAlphaSpringAnim() {
        if (this.mAlphaSpringAnim == null) {
            this.mAlphaSpringAnim = SpringAnimationUtils.getInstance().createDefaultSpringAnim(this.mTargetView, DynamicAnimation.ALPHA, 1.0f);
        }
        return this.mAlphaSpringAnim;
    }

    public List<SpringAnimation> getAllSpringAnim() {
        List<SpringAnimation> list = this.mAllSpringAnim;
        if (list == null || list.isEmpty()) {
            this.mAllSpringAnim = new ArrayList();
            this.mAllSpringAnim.add(getTranslationXSpringAnim());
            this.mAllSpringAnim.add(getTranslationYSpringAnim());
            this.mAllSpringAnim.add(getScaleXSpringAnim());
            this.mAllSpringAnim.add(getScaleYSpringAnim());
            this.mAllSpringAnim.add(getAlphaSpringAnim());
        }
        return this.mAllSpringAnim;
    }

    public View getTargetView() {
        return this.mTargetView;
    }
}

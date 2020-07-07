package com.android.systemui.screenshot;

import android.animation.Animator;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.plugins.R;

public class LongScreenShotGuideLayout extends FrameLayout {
    private int mGap;
    private ImageView mImageView;
    private TextView mTextView;

    public LongScreenShotGuideLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public LongScreenShotGuideLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public LongScreenShotGuideLayout(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public LongScreenShotGuideLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mTextView = (TextView) findViewById(R.id.guide_textview);
        this.mImageView = (ImageView) findViewById(R.id.guide_imageview);
        this.mGap = getResources().getDimensionPixelSize(R.dimen.long_screenshot_guide_gap);
    }

    public void setLongScreenShotButtonBound(Rect rect, int i) {
        int i2 = -((i - rect.left) + this.mGap);
        int i3 = rect.top;
        this.mImageView.setTranslationX((float) i2);
        float f = (float) i3;
        this.mImageView.setTranslationY(f);
        Drawable drawable = this.mImageView.getDrawable();
        Rect bounds = drawable.getBounds();
        ViewGroup.LayoutParams layoutParams = this.mImageView.getLayoutParams();
        layoutParams.height = rect.height();
        layoutParams.width = (int) (((((float) bounds.width()) * 0.4f) / ((float) bounds.height())) * ((float) layoutParams.height));
        this.mImageView.setLayoutParams(layoutParams);
        if (drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
        this.mTextView.setTranslationX((float) ((i2 - layoutParams.width) - this.mGap));
        this.mTextView.setTranslationY(f);
    }

    public void startAlphaChangeAnim(float f) {
        this.mTextView.animate().alpha(f).setDuration(150).setListener((Animator.AnimatorListener) null).start();
        this.mImageView.animate().alpha(f).setDuration(150).setListener((Animator.AnimatorListener) null).start();
    }
}

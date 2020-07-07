package com.android.systemui.pip.tv;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.plugins.R;

public class PipControlButtonView extends RelativeLayout {
    private Animator mButtonFocusGainAnimator;
    private Animator mButtonFocusLossAnimator;
    ImageView mButtonImageView;
    private TextView mDescriptionTextView;
    /* access modifiers changed from: private */
    public View.OnFocusChangeListener mFocusChangeListener;
    private ImageView mIconImageView;
    private final View.OnFocusChangeListener mInternalFocusChangeListener;
    private Animator mTextFocusGainAnimator;
    private Animator mTextFocusLossAnimator;

    public PipControlButtonView(Context context) {
        this(context, (AttributeSet) null, 0, 0);
    }

    public PipControlButtonView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0, 0);
    }

    public PipControlButtonView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public PipControlButtonView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mInternalFocusChangeListener = new View.OnFocusChangeListener() {
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    PipControlButtonView.this.startFocusGainAnimation();
                } else {
                    PipControlButtonView.this.startFocusLossAnimation();
                }
                if (PipControlButtonView.this.mFocusChangeListener != null) {
                    PipControlButtonView.this.mFocusChangeListener.onFocusChange(PipControlButtonView.this, z);
                }
            }
        };
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.tv_pip_control_button, this);
        this.mIconImageView = (ImageView) findViewById(R.id.icon);
        this.mButtonImageView = (ImageView) findViewById(R.id.button);
        this.mDescriptionTextView = (TextView) findViewById(R.id.desc);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, new int[]{16843033, 16843087}, i, i2);
        setImageResource(obtainStyledAttributes.getResourceId(0, 0));
        setText(obtainStyledAttributes.getResourceId(1, 0));
        obtainStyledAttributes.recycle();
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mButtonImageView.setOnFocusChangeListener(this.mInternalFocusChangeListener);
        this.mTextFocusGainAnimator = AnimatorInflater.loadAnimator(getContext(), R.anim.tv_pip_controls_focus_gain_animation);
        this.mTextFocusGainAnimator.setTarget(this.mDescriptionTextView);
        this.mButtonFocusGainAnimator = AnimatorInflater.loadAnimator(getContext(), R.anim.tv_pip_controls_focus_gain_animation);
        this.mButtonFocusGainAnimator.setTarget(this.mButtonImageView);
        this.mTextFocusLossAnimator = AnimatorInflater.loadAnimator(getContext(), R.anim.tv_pip_controls_focus_loss_animation);
        this.mTextFocusLossAnimator.setTarget(this.mDescriptionTextView);
        this.mButtonFocusLossAnimator = AnimatorInflater.loadAnimator(getContext(), R.anim.tv_pip_controls_focus_loss_animation);
        this.mButtonFocusLossAnimator.setTarget(this.mButtonImageView);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.mButtonImageView.setOnClickListener(onClickListener);
    }

    public void setOnFocusChangeListener(View.OnFocusChangeListener onFocusChangeListener) {
        this.mFocusChangeListener = onFocusChangeListener;
    }

    public void setImageDrawable(Drawable drawable) {
        this.mIconImageView.setImageDrawable(drawable);
    }

    public void setImageResource(int i) {
        if (i != 0) {
            this.mIconImageView.setImageResource(i);
        }
    }

    public void setText(CharSequence charSequence) {
        this.mButtonImageView.setContentDescription(charSequence);
        this.mDescriptionTextView.setText(charSequence);
    }

    public void setText(int i) {
        if (i != 0) {
            this.mButtonImageView.setContentDescription(getContext().getString(i));
            this.mDescriptionTextView.setText(i);
        }
    }

    private static void cancelAnimator(Animator animator) {
        if (animator.isStarted()) {
            animator.cancel();
        }
    }

    public void startFocusGainAnimation() {
        cancelAnimator(this.mButtonFocusLossAnimator);
        cancelAnimator(this.mTextFocusLossAnimator);
        this.mTextFocusGainAnimator.start();
        if (this.mButtonImageView.getAlpha() < 1.0f) {
            this.mButtonFocusGainAnimator.start();
        }
    }

    public void startFocusLossAnimation() {
        cancelAnimator(this.mButtonFocusGainAnimator);
        cancelAnimator(this.mTextFocusGainAnimator);
        this.mTextFocusLossAnimator.start();
        if (this.mButtonImageView.hasFocus()) {
            this.mButtonFocusLossAnimator.start();
        }
    }
}

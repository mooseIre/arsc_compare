package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.keyguard.AlphaOptimizedLinearLayout;
import com.android.systemui.ViewInvertHelper;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.TransformableView;
import com.android.systemui.statusbar.ViewTransformationHelper;

public class HybridNotificationView extends AlphaOptimizedLinearLayout implements TransformableView {
    private ViewInvertHelper mInvertHelper;
    protected TextView mTextView;
    protected TextView mTitleView;
    private ViewTransformationHelper mTransformationHelper;

    public HybridNotificationView(Context context) {
        this(context, (AttributeSet) null);
    }

    public HybridNotificationView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public HybridNotificationView(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public HybridNotificationView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public TextView getTitleView() {
        return this.mTitleView;
    }

    public TextView getTextView() {
        return this.mTextView;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mTitleView = (TextView) findViewById(R.id.notification_title);
        this.mTextView = (TextView) findViewById(R.id.notification_text);
        this.mInvertHelper = new ViewInvertHelper((View) this, 700);
        ViewTransformationHelper viewTransformationHelper = new ViewTransformationHelper();
        this.mTransformationHelper = viewTransformationHelper;
        viewTransformationHelper.setCustomTransformation(new ViewTransformationHelper.CustomTransformation() {
            public boolean transformTo(TransformState transformState, TransformableView transformableView, float f) {
                TransformState currentState = transformableView.getCurrentState(1);
                CrossFadeHelper.fadeOut((View) HybridNotificationView.this.mTextView, f);
                if (currentState != null) {
                    transformState.transformViewVerticalTo(currentState, f);
                    currentState.recycle();
                }
                return true;
            }

            public boolean transformFrom(TransformState transformState, TransformableView transformableView, float f) {
                TransformState currentState = transformableView.getCurrentState(1);
                CrossFadeHelper.fadeIn(HybridNotificationView.this.mTextView, f);
                if (currentState != null) {
                    transformState.transformViewVerticalFrom(currentState, f);
                    currentState.recycle();
                }
                return true;
            }
        }, 2);
        this.mTransformationHelper.addTransformedView(1, this.mTitleView);
        this.mTransformationHelper.addTransformedView(2, this.mTextView);
    }

    public void bind(CharSequence charSequence, CharSequence charSequence2) {
        this.mTitleView.setText(charSequence);
        this.mTitleView.setVisibility(TextUtils.isEmpty(charSequence) ? 8 : 0);
        if (TextUtils.isEmpty(charSequence2)) {
            this.mTextView.setVisibility(8);
            this.mTextView.setText((CharSequence) null);
        } else {
            this.mTextView.setVisibility(0);
            this.mTextView.setText(charSequence2.toString());
        }
        requestLayout();
    }

    public void setDark(boolean z, boolean z2, long j) {
        this.mInvertHelper.setInverted(z, z2, j);
    }

    public TransformState getCurrentState(int i) {
        return this.mTransformationHelper.getCurrentState(i);
    }

    public void transformTo(TransformableView transformableView, Runnable runnable) {
        this.mTransformationHelper.transformTo(transformableView, runnable);
    }

    public void transformTo(TransformableView transformableView, float f) {
        this.mTransformationHelper.transformTo(transformableView, f);
    }

    public void transformFrom(TransformableView transformableView) {
        this.mTransformationHelper.transformFrom(transformableView);
    }

    public void transformFrom(TransformableView transformableView, float f) {
        this.mTransformationHelper.transformFrom(transformableView, f);
    }

    public void setVisible(boolean z) {
        setVisibility(z ? 0 : 4);
        this.mTransformationHelper.setVisible(z);
    }
}

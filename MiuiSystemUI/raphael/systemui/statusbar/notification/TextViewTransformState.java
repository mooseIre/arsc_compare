package com.android.systemui.statusbar.notification;

import android.text.Layout;
import android.text.TextUtils;
import android.util.Pools;
import android.view.View;
import android.widget.TextView;

public class TextViewTransformState extends TransformState {
    private static Pools.SimplePool<TextViewTransformState> sInstancePool = new Pools.SimplePool<>(40);
    private TextView mText;

    public void initFrom(View view) {
        super.initFrom(view);
        if (view instanceof TextView) {
            this.mText = (TextView) view;
        }
    }

    /* access modifiers changed from: protected */
    public boolean sameAs(TransformState transformState) {
        if (transformState instanceof TextViewTransformState) {
            TextViewTransformState textViewTransformState = (TextViewTransformState) transformState;
            if (TextUtils.equals(textViewTransformState.mText.getText(), this.mText.getText())) {
                return getEllipsisCount() == textViewTransformState.getEllipsisCount() && getInnerHeight(this.mText) == getInnerHeight(textViewTransformState.mText);
            }
        }
        return super.sameAs(transformState);
    }

    private int getInnerHeight(TextView textView) {
        return (textView.getHeight() - textView.getPaddingTop()) - textView.getPaddingBottom();
    }

    private int getEllipsisCount() {
        Layout layout = this.mText.getLayout();
        if (layout == null || layout.getLineCount() <= 0) {
            return 0;
        }
        return layout.getEllipsisCount(0);
    }

    public static TextViewTransformState obtain() {
        TextViewTransformState textViewTransformState = (TextViewTransformState) sInstancePool.acquire();
        if (textViewTransformState != null) {
            return textViewTransformState;
        }
        return new TextViewTransformState();
    }

    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }

    /* access modifiers changed from: protected */
    public void reset() {
        super.reset();
        this.mText = null;
    }
}

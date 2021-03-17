package com.android.systemui.statusbar.notification;

import android.text.Layout;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Pools;
import android.view.View;
import android.widget.TextView;
import com.android.systemui.statusbar.notification.TransformState;

public class TextViewTransformState extends TransformState {
    private static Pools.SimplePool<TextViewTransformState> sInstancePool = new Pools.SimplePool<>(40);
    private TextView mText;

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void initFrom(View view, TransformState.TransformInfo transformInfo) {
        super.initFrom(view, transformInfo);
        this.mText = (TextView) view;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public boolean sameAs(TransformState transformState) {
        if (super.sameAs(transformState)) {
            return true;
        }
        if (transformState instanceof TextViewTransformState) {
            TextViewTransformState textViewTransformState = (TextViewTransformState) transformState;
            if (TextUtils.equals(textViewTransformState.mText.getText(), this.mText.getText())) {
                if (getEllipsisCount() == textViewTransformState.getEllipsisCount() && this.mText.getLineCount() == textViewTransformState.mText.getLineCount() && hasSameSpans(textViewTransformState)) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

    private boolean hasSameSpans(TextViewTransformState textViewTransformState) {
        TextView textView = this.mText;
        boolean z = textView instanceof Spanned;
        if (z != (textViewTransformState.mText instanceof Spanned)) {
            return false;
        }
        if (!z) {
            return true;
        }
        Spanned spanned = (Spanned) textView;
        Object[] spans = spanned.getSpans(0, spanned.length(), Object.class);
        Spanned spanned2 = (Spanned) textViewTransformState.mText;
        Object[] spans2 = spanned2.getSpans(0, spanned2.length(), Object.class);
        if (spans.length != spans2.length) {
            return false;
        }
        for (int i = 0; i < spans.length; i++) {
            Object obj = spans[i];
            Object obj2 = spans2[i];
            if (!(obj.getClass().equals(obj2.getClass()) && spanned.getSpanStart(obj) == spanned2.getSpanStart(obj2) && spanned.getSpanEnd(obj) == spanned2.getSpanEnd(obj2))) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public boolean transformScale(TransformState transformState) {
        int lineCount;
        if (!(transformState instanceof TextViewTransformState)) {
            return false;
        }
        TextViewTransformState textViewTransformState = (TextViewTransformState) transformState;
        if (TextUtils.equals(this.mText.getText(), textViewTransformState.mText.getText()) && (lineCount = this.mText.getLineCount()) == 1 && lineCount == textViewTransformState.mText.getLineCount() && getEllipsisCount() == textViewTransformState.getEllipsisCount() && getViewHeight() != textViewTransformState.getViewHeight()) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public int getViewWidth() {
        Layout layout = this.mText.getLayout();
        if (layout != null) {
            return (int) layout.getLineWidth(0);
        }
        return super.getViewWidth();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public int getViewHeight() {
        return this.mText.getLineHeight();
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

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public void reset() {
        super.reset();
        this.mText = null;
    }
}

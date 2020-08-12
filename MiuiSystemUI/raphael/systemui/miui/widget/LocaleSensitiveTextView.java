package com.android.systemui.miui.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class LocaleSensitiveTextView extends TextView {
    private int mTextId;

    public LocaleSensitiveTextView(Context context) {
        this(context, (AttributeSet) null);
    }

    public LocaleSensitiveTextView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public LocaleSensitiveTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.LocaleSensitiveTextView, i, 0);
        if (obtainStyledAttributes != null) {
            this.mTextId = obtainStyledAttributes.getResourceId(R$styleable.LocaleSensitiveTextView_android_text, 0);
            obtainStyledAttributes.recycle();
        }
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        int i;
        if (!TextUtils.equals(getTextLocale().getLanguage(), configuration.locale.getLanguage()) && (i = this.mTextId) != 0) {
            setText(i);
        }
        super.onConfigurationChanged(configuration);
    }
}

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

public class KeyguardIndicationTextView extends TextView {
    private CharSequence mText = "";

    public KeyguardIndicationTextView(Context context) {
        super(context);
    }

    public KeyguardIndicationTextView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public KeyguardIndicationTextView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public KeyguardIndicationTextView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public void switchIndication(CharSequence charSequence) {
        if (TextUtils.isEmpty(charSequence)) {
            this.mText = "";
            setVisibility(4);
        } else if (!TextUtils.equals(charSequence, this.mText)) {
            this.mText = charSequence;
            setVisibility(0);
            setText(this.mText);
        }
    }

    public void switchIndication(int i) {
        switchIndication(getResources().getText(i));
    }
}

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.systemui.plugins.R;

public class KeyguardIndicationTextView extends TextView {
    private int mDensityDpi;
    private float mFontScale;
    private boolean mIsBottomButtonAnimating;
    private boolean mPowerPluggedIn;

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
        if (TextUtils.isEmpty(charSequence) || this.mIsBottomButtonAnimating) {
            setVisibility(4);
        } else {
            setVisibility(0);
        }
        setText(charSequence);
    }

    public void switchIndication(int i) {
        switchIndication(getResources().getText(i));
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        float f = configuration.fontScale;
        int i = configuration.densityDpi;
        if (this.mFontScale != f) {
            updateTextSize();
            this.mFontScale = f;
        }
        if (this.mDensityDpi != i) {
            updateTextSize();
            this.mDensityDpi = i;
        }
    }

    public void updateTextSize() {
        setTextSize(0, (float) getResources().getDimensionPixelSize(this.mPowerPluggedIn ? R.dimen.miui_charge_lock_screen_unlock_hint_text_size : R.dimen.miui_default_lock_screen_unlock_hint_text_size));
    }

    public void setBottomAreaButtonClicked(boolean z) {
        this.mIsBottomButtonAnimating = z;
    }

    public void setPowerPluggedIn(boolean z) {
        if (z != this.mPowerPluggedIn) {
            this.mPowerPluggedIn = z;
            updateTextSize();
        }
    }
}

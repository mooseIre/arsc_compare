package com.android.keyguard;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.keyguard.MiuiCarrierTextController;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import java.util.Locale;

public class CarrierText extends TextView {
    private MiuiCarrierTextController.CarrierTextListener mCarrierTextCallback;
    private MiuiCarrierTextController mCarrierTextController;
    private boolean mShouldMarquee;

    public CarrierText(Context context) {
        this(context, null);
    }

    /* JADX INFO: finally extract failed */
    public CarrierText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mCarrierTextCallback = new MiuiCarrierTextController.CarrierTextListener() {
            /* class com.android.keyguard.CarrierText.AnonymousClass1 */

            @Override // com.android.keyguard.MiuiCarrierTextController.CarrierTextListener
            public void onCarrierTextChanged(String str) {
                CarrierText.this.setText(str);
            }

            @Override // com.android.keyguard.MiuiCarrierTextController.CarrierTextListener
            public void onFinishedGoingToSleep() {
                CarrierText.this.setSelected(false);
            }

            @Override // com.android.keyguard.MiuiCarrierTextController.CarrierTextListener
            public void onStartedWakingUp() {
                CarrierText.this.setSelected(true);
            }
        };
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(attributeSet, R$styleable.CarrierText, 0, 0);
        try {
            boolean z = obtainStyledAttributes.getBoolean(R$styleable.CarrierText_allCaps, false);
            obtainStyledAttributes.getBoolean(R$styleable.CarrierText_showAirplaneMode, false);
            obtainStyledAttributes.getBoolean(R$styleable.CarrierText_showMissingSim, false);
            obtainStyledAttributes.recycle();
            setTransformationMethod(new CarrierTextTransformationMethod(this, ((TextView) this).mContext, z));
        } catch (Throwable th) {
            obtainStyledAttributes.recycle();
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        getResources().getString(17040469);
        this.mCarrierTextController = (MiuiCarrierTextController) Dependency.get(MiuiCarrierTextController.class);
        boolean isDeviceInteractive = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class)).isDeviceInteractive();
        this.mShouldMarquee = isDeviceInteractive;
        setSelected(isDeviceInteractive);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mCarrierTextController.addCallback(this.mCarrierTextCallback);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mCarrierTextController.removeCallback(this.mCarrierTextCallback);
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View view, int i) {
        super.onVisibilityChanged(view, i);
        if (i == 0) {
            setEllipsize(TextUtils.TruncateAt.MARQUEE);
        } else {
            setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    private class CarrierTextTransformationMethod extends SingleLineTransformationMethod {
        private final boolean mAllCaps;
        private final Locale mLocale;

        public CarrierTextTransformationMethod(CarrierText carrierText, Context context, boolean z) {
            this.mLocale = context.getResources().getConfiguration().locale;
            this.mAllCaps = z;
        }

        public CharSequence getTransformation(CharSequence charSequence, View view) {
            CharSequence transformation = super.getTransformation(charSequence, view);
            return (!this.mAllCaps || transformation == null) ? transformation : transformation.toString().toUpperCase(this.mLocale);
        }
    }
}

package com.android.systemui.qs.carrier;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settingslib.Utils;
import com.android.settingslib.graph.SignalDrawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.DualToneHandler;
import com.android.systemui.qs.QuickStatusBarHeader;

public class QSCarrier extends LinearLayout {
    private ImageView mMobileSignal;

    public QSCarrier(Context context) {
        super(context);
    }

    public QSCarrier(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public QSCarrier(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public QSCarrier(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        new DualToneHandler(getContext());
        findViewById(C0015R$id.mobile_combo);
        this.mMobileSignal = (ImageView) findViewById(C0015R$id.mobile_signal);
        ImageView imageView = (ImageView) findViewById(C0015R$id.mobile_roaming);
        TextView textView = (TextView) findViewById(C0015R$id.qs_carrier_text);
        this.mMobileSignal.setImageDrawable(new SignalDrawable(((LinearLayout) this).mContext));
        int colorAttrDefaultColor = Utils.getColorAttrDefaultColor(((LinearLayout) this).mContext, 16842800);
        ColorStateList.valueOf(colorAttrDefaultColor);
        QuickStatusBarHeader.getColorIntensity(colorAttrDefaultColor);
    }
}

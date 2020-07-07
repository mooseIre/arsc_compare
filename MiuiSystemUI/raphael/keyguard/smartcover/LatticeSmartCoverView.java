package com.android.keyguard.smartcover;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.plugins.R;

public class LatticeSmartCoverView extends SmartCoverView {
    private View mBatteryInfo;
    private BatteryLevelImageView mBatteryLevelImage;
    private TextView mBatteryLevelText;
    private View mInfo1;
    private TextView mInfo1TextView;
    private View mInfo2;
    private TextView mInfo2TextView1;
    private TextView mInfo2TextView2;

    public LatticeSmartCoverView(Context context) {
        super(context);
    }

    public LatticeSmartCoverView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mInfo1 = findViewById(R.id.info1);
        this.mInfo1TextView = (TextView) this.mInfo1.findViewById(R.id.num);
        this.mInfo2 = findViewById(R.id.info2);
        this.mInfo2TextView1 = (TextView) this.mInfo2.findViewById(R.id.num1);
        this.mInfo2TextView2 = (TextView) this.mInfo2.findViewById(R.id.num2);
        this.mBatteryInfo = findViewById(R.id.battery);
        this.mBatteryLevelImage = (BatteryLevelImageView) findViewById(R.id.battery_level);
        this.mBatteryLevelText = (TextView) findViewById(R.id.battery_text);
        Typeface create = Typeface.create("miuiex-bitmap", 0);
        this.mInfo1TextView.setTypeface(create);
        this.mInfo2TextView1.setTypeface(create);
        this.mInfo2TextView2.setTypeface(create);
        this.mBatteryLevelText.setTypeface(create);
    }

    /* access modifiers changed from: protected */
    public void refresh() {
        int i;
        this.mInfo1.setVisibility(4);
        this.mInfo2.setVisibility(4);
        this.mBatteryInfo.setVisibility(4);
        boolean z = this.mShowMissCall;
        int i2 = R.drawable.smart_cover_miss_call;
        if (z && this.mShowSms) {
            this.mInfo2.setVisibility(0);
            this.mInfo2TextView1.setText(String.valueOf(this.mSmsNum));
            this.mInfo2TextView2.setText(String.valueOf(this.mMissCallNum));
            ((ImageView) this.mInfo2.findViewById(R.id.image1)).setImageResource(R.drawable.smart_cover_sms);
            ((ImageView) this.mInfo2.findViewById(R.id.image2)).setImageResource(R.drawable.smart_cover_miss_call);
        } else if (this.mShowMissCall || this.mShowSms) {
            this.mInfo1.setVisibility(0);
            this.mInfo1TextView.setText(String.valueOf(this.mShowMissCall ? this.mMissCallNum : this.mSmsNum));
            ImageView imageView = (ImageView) this.mInfo1.findViewById(R.id.image);
            if (!this.mShowMissCall) {
                i2 = R.drawable.smart_cover_sms;
            }
            imageView.setImageResource(i2);
        } else if (this.mCharging || this.mLevel <= this.mLowBatteryWarningLevel) {
            this.mBatteryLevelImage.setImageResource(this.mCharging ? R.drawable.smart_cover_battery_charging : R.drawable.smart_cover_battery_low);
            this.mBatteryLevelImage.setBatteryLevel(this.mLevel);
            TextView textView = this.mBatteryLevelText;
            StringBuilder sb = new StringBuilder();
            sb.append(this.mLevel < 10 ? "0" : "");
            sb.append(String.valueOf(this.mLevel));
            sb.append("%");
            textView.setText(sb.toString());
            TextView textView2 = this.mBatteryLevelText;
            if (this.mCharging) {
                i = this.mContext.getResources().getColor(R.color.smart_cover_battery_text_color);
            } else {
                i = this.mContext.getResources().getColor(R.color.smart_cover_battery_low_text_color);
            }
            textView2.setTextColor(i);
            this.mBatteryInfo.setVisibility(0);
        }
    }
}

package com.android.keyguard.smartcover;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.plugins.R;

public class FullSmartCoverView extends SmartCoverView {
    private TextView mBatteryLevelText;
    private Clock mDate;
    private Clock mHour;
    private View mInfo1;
    private TextView mInfo1TextView;
    private View mInfo2;
    private TextView mInfo2TextView1;
    private TextView mInfo2TextView2;
    private Clock mMinute;

    public FullSmartCoverView(Context context) {
        super(context);
    }

    public FullSmartCoverView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mHour = (Clock) findViewById(R.id.time_hour);
        this.mHour.setShowHour(true);
        this.mMinute = (Clock) findViewById(R.id.time_minute);
        this.mMinute.setShowMinute(true);
        this.mDate = (Clock) findViewById(R.id.date);
        this.mDate.setShowDate(true);
        this.mInfo1 = findViewById(R.id.info1);
        this.mInfo1TextView = (TextView) this.mInfo1.findViewById(R.id.num);
        this.mInfo2 = findViewById(R.id.info2);
        this.mInfo2TextView1 = (TextView) this.mInfo2.findViewById(R.id.num1);
        this.mInfo2TextView2 = (TextView) this.mInfo2.findViewById(R.id.num2);
        this.mBatteryLevelText = (TextView) findViewById(R.id.battery_text);
    }

    /* access modifiers changed from: protected */
    public void refresh() {
        this.mInfo1.setVisibility(4);
        this.mInfo2.setVisibility(4);
        this.mBatteryLevelText.setVisibility(4);
        boolean z = this.mShowMissCall;
        int i = R.drawable.full_smart_cover_miss_call;
        if (z && this.mShowSms) {
            this.mInfo2.setVisibility(0);
            this.mInfo2TextView1.setText(String.valueOf(this.mSmsNum));
            this.mInfo2TextView2.setText(String.valueOf(this.mMissCallNum));
            ((ImageView) this.mInfo2.findViewById(R.id.image1)).setImageResource(R.drawable.full_smart_cover_sms);
            ((ImageView) this.mInfo2.findViewById(R.id.image2)).setImageResource(R.drawable.full_smart_cover_miss_call);
        } else if (this.mShowMissCall || this.mShowSms) {
            this.mInfo1.setVisibility(0);
            this.mInfo1TextView.setText(String.valueOf(this.mShowMissCall ? this.mMissCallNum : this.mSmsNum));
            ImageView imageView = (ImageView) this.mInfo1.findViewById(R.id.image);
            if (!this.mShowMissCall) {
                i = R.drawable.full_smart_cover_sms;
            }
            imageView.setImageResource(i);
        }
        if (this.mCharging || this.mLevel <= this.mLowBatteryWarningLevel) {
            if (this.mCharging) {
                if (this.mFull) {
                    this.mBatteryLevelText.setText(R.string.unlockscreen_recharge_completed);
                } else {
                    this.mBatteryLevelText.setText(getResources().getString(R.string.unlockscreen_recharging_message, new Object[]{Integer.valueOf(this.mLevel)}));
                }
            } else if (this.mLevel <= this.mLowBatteryWarningLevel) {
                this.mBatteryLevelText.setText(R.string.unlockscreen_low_battery);
            }
            this.mBatteryLevelText.setVisibility(0);
        }
    }
}

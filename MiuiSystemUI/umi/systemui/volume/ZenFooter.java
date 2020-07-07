package com.android.systemui.volume;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Prefs;
import com.android.systemui.plugins.R;

public class ZenFooter extends LinearLayout {
    private final ConfigurableTexts mConfigurableTexts;
    private final Context mContext;
    private TextView mEndNowButton;
    private ImageView mIcon;
    private TextView mSummaryLine1;
    private TextView mSummaryLine2;
    private int mZen = -1;
    private View mZenIntroduction;
    private View mZenIntroductionConfirm;
    private TextView mZenIntroductionMessage;

    static {
        Util.logTag(ZenFooter.class);
    }

    public ZenFooter(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mConfigurableTexts = new ConfigurableTexts(context);
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(new ValueAnimator().getDuration() / 2);
        setLayoutTransition(layoutTransition);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIcon = (ImageView) findViewById(R.id.volume_zen_icon);
        this.mSummaryLine1 = (TextView) findViewById(R.id.volume_zen_summary_line_1);
        this.mSummaryLine2 = (TextView) findViewById(R.id.volume_zen_summary_line_2);
        this.mEndNowButton = (TextView) findViewById(R.id.volume_zen_end_now);
        this.mZenIntroduction = findViewById(R.id.zen_introduction);
        TextView textView = (TextView) findViewById(R.id.zen_introduction_message);
        this.mZenIntroductionMessage = textView;
        this.mConfigurableTexts.add(textView, R.string.zen_alarms_introduction);
        View findViewById = findViewById(R.id.zen_introduction_confirm);
        this.mZenIntroductionConfirm = findViewById;
        findViewById.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ZenFooter.this.confirmZenIntroduction();
            }
        });
        Util.setVisOrGone(this.mZenIntroduction, shouldShowIntroduction());
        this.mConfigurableTexts.add(this.mSummaryLine1);
        this.mConfigurableTexts.add(this.mSummaryLine2);
        this.mConfigurableTexts.add(this.mEndNowButton, R.string.volume_zen_end_now);
    }

    /* access modifiers changed from: private */
    public void confirmZenIntroduction() {
        Prefs.putBoolean(this.mContext, "DndConfirmedAlarmIntroduction", true);
        updateIntroduction();
    }

    private boolean isZenAlarms() {
        return this.mZen == 3;
    }

    public boolean shouldShowIntroduction() {
        if (Prefs.getBoolean(this.mContext, "DndConfirmedAlarmIntroduction", false) || !isZenAlarms()) {
            return false;
        }
        return true;
    }

    public void updateIntroduction() {
        Util.setVisOrGone(this.mZenIntroduction, shouldShowIntroduction());
    }
}

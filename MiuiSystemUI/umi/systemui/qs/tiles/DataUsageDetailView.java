package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.android.systemui.FontUtils;
import com.android.systemui.plugins.R;
import java.text.DecimalFormat;

public class DataUsageDetailView extends LinearLayout {
    public DataUsageDetailView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        new DecimalFormat("#.##");
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontUtils.updateFontSize(this, 16908310, R.dimen.qs_data_usage_text_size);
        FontUtils.updateFontSize(this, R.id.usage_text, R.dimen.qs_data_usage_usage_text_size);
        FontUtils.updateFontSize(this, R.id.usage_carrier_text, R.dimen.qs_data_usage_text_size);
        FontUtils.updateFontSize(this, R.id.usage_info_top_text, R.dimen.qs_data_usage_text_size);
        FontUtils.updateFontSize(this, R.id.usage_period_text, R.dimen.qs_data_usage_text_size);
        FontUtils.updateFontSize(this, R.id.usage_info_bottom_text, R.dimen.qs_data_usage_text_size);
    }
}

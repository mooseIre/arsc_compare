package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.FontSizeUtils;
import java.text.DecimalFormat;

public class DataUsageDetailView extends LinearLayout {
    private final DecimalFormat FORMAT = new DecimalFormat("#.##");

    public DataUsageDetailView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontSizeUtils.updateFontSize(this, 16908310, C0012R$dimen.qs_data_usage_text_size);
        FontSizeUtils.updateFontSize(this, C0015R$id.usage_text, C0012R$dimen.qs_data_usage_usage_text_size);
        FontSizeUtils.updateFontSize(this, C0015R$id.usage_carrier_text, C0012R$dimen.qs_data_usage_text_size);
        FontSizeUtils.updateFontSize(this, C0015R$id.usage_info_top_text, C0012R$dimen.qs_data_usage_text_size);
        FontSizeUtils.updateFontSize(this, C0015R$id.usage_period_text, C0012R$dimen.qs_data_usage_text_size);
        FontSizeUtils.updateFontSize(this, C0015R$id.usage_info_bottom_text, C0012R$dimen.qs_data_usage_text_size);
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0089  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x00e9  */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x00fa  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x00fc  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0114  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x011a  */
    /* JADX WARNING: Removed duplicated region for block: B:30:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void bind(com.android.settingslib.net.DataUsageController.DataUsageInfo r20) {
        /*
        // Method dump skipped, instructions count: 286
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.qs.tiles.DataUsageDetailView.bind(com.android.settingslib.net.DataUsageController$DataUsageInfo):void");
    }

    private String formatBytes(long j) {
        String str;
        double d;
        double abs = (double) Math.abs(j);
        if (abs > 1.048576E8d) {
            d = abs / 1.073741824E9d;
            str = "GB";
        } else if (abs > 102400.0d) {
            d = abs / 1048576.0d;
            str = "MB";
        } else {
            d = abs / 1024.0d;
            str = "KB";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(this.FORMAT.format(d * ((double) (j < 0 ? -1 : 1))));
        sb.append(" ");
        sb.append(str);
        return sb.toString();
    }
}

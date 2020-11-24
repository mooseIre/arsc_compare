package com.android.keyguard.fod.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.View;
import android.widget.ImageView;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0018R$string;

public class SearchItem extends IQuickOpenItem {
    private final ImageView mView;

    public String getTag() {
        return "Browser/Search";
    }

    public SearchItem(RectF rectF, Region region, Context context) {
        super(rectF, region, context);
        ImageView imageView = new ImageView(context);
        this.mView = imageView;
        imageView.setImageResource(C0010R$drawable.gxzw_quick_open_search);
        this.mView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public View getView() {
        return this.mView;
    }

    public Intent getIntent() {
        Intent intent;
        if (MiuiKeyguardUtils.isBrowserSearchExist(this.mContext)) {
            intent = new Intent("android.intent.action.WEB_SEARCH");
            intent.setPackage(MiuiKeyguardUtils.isBrowserGlobalEnabled(this.mContext) ? "com.mi.globalbrowser" : "com.android.browser");
            intent.putExtra("from", "fingerprint");
        } else if (isGoogleQuickSearchExit()) {
            intent = getGoogleQuickSearchIntent();
        } else {
            intent = new Intent("android.intent.action.WEB_SEARCH");
        }
        intent.setFlags(343932928);
        return intent;
    }

    public String getTitle() {
        return this.mContext.getString(C0018R$string.gxzw_quick_open_search);
    }

    public String getSubTitle() {
        return this.mContext.getString(C0018R$string.gxzw_quick_open_search_sub);
    }

    private boolean isGoogleQuickSearchExit() {
        return MiuiKeyguardUtils.isIntentActivityExist(this.mContext, getGoogleQuickSearchIntent());
    }

    private Intent getGoogleQuickSearchIntent() {
        Intent intent = new Intent("android.search.action.GLOBAL_SEARCH");
        intent.setPackage("com.google.android.googlequicksearchbox");
        return intent;
    }
}

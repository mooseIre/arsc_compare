package com.android.keyguard.fod.item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.plugins.R;

public class WechatPayItem extends IQuickOpenItem {
    private final ImageView mView;

    public String getTag() {
        return "WeChat/Payment code";
    }

    public boolean needStartProcess() {
        return true;
    }

    public WechatPayItem(RectF rectF, Region region, Context context) {
        super(rectF, region, context);
        this.mView = new ImageView(context);
        this.mView.setImageResource(R.drawable.gxzw_quick_open_wechat_pay);
        this.mView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public View getView() {
        return this.mView;
    }

    public Intent getIntent() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setFlags(343932928);
        intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI"));
        intent.putExtra("key_entry_scene", 2);
        return intent;
    }

    public String getTitle() {
        return this.mContext.getString(R.string.gxzw_quick_open_wechat_pay);
    }

    public String getSubTitle() {
        return this.mContext.getString(R.string.gxzw_quick_open_wechat_pay_sub);
    }
}

package com.android.keyguard.fod.item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;

public class WechatPayItem extends IQuickOpenItem {
    private final ImageView mView;

    @Override // com.android.keyguard.fod.item.IQuickOpenItem
    public String getTag() {
        return "WeChat/Payment code";
    }

    @Override // com.android.keyguard.fod.item.IQuickOpenItem
    public boolean needStartProcess() {
        return true;
    }

    public WechatPayItem(RectF rectF, Region region, Context context) {
        super(rectF, region, context);
        ImageView imageView = new ImageView(context);
        this.mView = imageView;
        imageView.setImageResource(C0013R$drawable.gxzw_quick_open_wechat_pay);
        this.mView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    @Override // com.android.keyguard.fod.item.IQuickOpenItem
    public View getView() {
        return this.mView;
    }

    @Override // com.android.keyguard.fod.item.IQuickOpenItem
    public Intent getIntent() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setFlags(343932928);
        intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.plugin.offline.ui.WalletOfflineCoinPurseUI"));
        intent.putExtra("key_entry_scene", 2);
        return intent;
    }

    @Override // com.android.keyguard.fod.item.IQuickOpenItem
    public String getTitle() {
        return this.mContext.getString(C0021R$string.gxzw_quick_open_wechat_pay);
    }

    @Override // com.android.keyguard.fod.item.IQuickOpenItem
    public String getSubTitle() {
        return this.mContext.getString(C0021R$string.gxzw_quick_open_wechat_pay_sub);
    }
}

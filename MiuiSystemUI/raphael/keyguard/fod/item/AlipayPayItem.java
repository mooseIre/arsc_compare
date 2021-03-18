package com.android.keyguard.fod.item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Region;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;

public class AlipayPayItem extends IQuickOpenItem {
    private final ImageView mView;

    @Override // com.android.keyguard.fod.item.IQuickOpenItem
    public String getTag() {
        return "Alipay/Payment code";
    }

    public AlipayPayItem(RectF rectF, Region region, Context context) {
        super(rectF, region, context);
        ImageView imageView = new ImageView(context);
        this.mView = imageView;
        imageView.setImageResource(C0013R$drawable.gxzw_quick_open_alipay_pay);
        this.mView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    @Override // com.android.keyguard.fod.item.IQuickOpenItem
    public View getView() {
        return this.mView;
    }

    @Override // com.android.keyguard.fod.item.IQuickOpenItem
    public Intent getIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.eg.android.AlipayGphone", "com.eg.android.AlipayGphone.FastStartActivity"));
        intent.setAction("android.intent.action.VIEW");
        intent.setFlags(343932928);
        intent.setData(Uri.parse("alipayss://platformapi/startapp?appId=20000056&source=shortcut"));
        return intent;
    }

    @Override // com.android.keyguard.fod.item.IQuickOpenItem
    public String getTitle() {
        return this.mContext.getString(C0021R$string.gxzw_quick_open_alipay_pay);
    }

    @Override // com.android.keyguard.fod.item.IQuickOpenItem
    public String getSubTitle() {
        return this.mContext.getString(C0021R$string.gxzw_quick_open_alipay_pay_sub);
    }
}

package com.android.keyguard.fod.item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0018R$string;

public class WechatScanItem extends IQuickOpenItem {
    private final ImageView mView;

    public String getTag() {
        return "WeChat/Scanner";
    }

    public boolean needStartProcess() {
        return true;
    }

    public WechatScanItem(RectF rectF, Region region, Context context) {
        super(rectF, region, context);
        ImageView imageView = new ImageView(context);
        this.mView = imageView;
        imageView.setImageResource(C0010R$drawable.gxzw_quick_open_wechat_scan);
        this.mView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public View getView() {
        return this.mView;
    }

    public Intent getIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setFlags(343932928);
        intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.plugin.scanner.ui.BaseScanUI"));
        return intent;
    }

    public String getTitle() {
        return this.mContext.getString(C0018R$string.gxzw_quick_open_wechat_scan);
    }

    public String getSubTitle() {
        return this.mContext.getString(C0018R$string.gxzw_quick_open_wechat_scan_sub);
    }
}

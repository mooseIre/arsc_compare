package com.android.keyguard.fod.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0018R$string;

public class XiaoaiItem extends IQuickOpenItem {
    private static final int[] TITLE_RES = {C0018R$string.gxzw_quick_open_xiaoai_title1, C0018R$string.gxzw_quick_open_xiaoai_title2};
    private static int sTitleCount;
    private final ImageView mView;

    public String getTag() {
        return "Mi AI";
    }

    public boolean startActionByService() {
        return true;
    }

    public XiaoaiItem(RectF rectF, Region region, Context context) {
        super(rectF, region, context);
        ImageView imageView = new ImageView(context);
        this.mView = imageView;
        imageView.setImageResource(C0010R$drawable.gxzw_quick_open_xiaoai);
        this.mView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public View getView() {
        return this.mView;
    }

    public Intent getIntent() {
        Intent intent = new Intent("android.intent.action.ASSIST");
        intent.setPackage("com.miui.voiceassist");
        intent.putExtra("voice_assist_start_from_key", "FOD");
        intent.setFlags(343932928);
        return intent;
    }

    public String getTitle() {
        return this.mContext.getString(getStringRes());
    }

    public String getSubTitle() {
        return this.mContext.getString(C0018R$string.gxzw_quick_open_xiaoai_sub);
    }

    private static int getStringRes() {
        int i = sTitleCount;
        int[] iArr = TITLE_RES;
        int length = i % iArr.length;
        sTitleCount = length;
        sTitleCount = length + 1;
        return iArr[length];
    }
}

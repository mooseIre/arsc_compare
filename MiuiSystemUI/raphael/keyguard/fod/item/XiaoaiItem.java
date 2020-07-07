package com.android.keyguard.fod.item;

import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.plugins.R;

public class XiaoaiItem extends IQuickOpenItem {
    private static final int[] TITLE_RES = {R.string.gxzw_quick_open_xiaoai_title1, R.string.gxzw_quick_open_xiaoai_title2};
    private static int sTitleCount = 0;
    private final ImageView mView;

    public String getTag() {
        return "Mi AI";
    }

    public boolean startActionByService() {
        return true;
    }

    public XiaoaiItem(RectF rectF, Region region, Context context) {
        super(rectF, region, context);
        this.mView = new ImageView(context);
        this.mView.setImageResource(R.drawable.gxzw_quick_open_xiaoai);
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
        return this.mContext.getString(R.string.gxzw_quick_open_xiaoai_sub);
    }

    private static int getStringRes() {
        int i = sTitleCount;
        int[] iArr = TITLE_RES;
        sTitleCount = i % iArr.length;
        int i2 = sTitleCount;
        sTitleCount = i2 + 1;
        return iArr[i2];
    }
}

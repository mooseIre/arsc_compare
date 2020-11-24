package com.android.keyguard.fod.item;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.graphics.Region;
import android.view.View;
import android.widget.ImageView;
import com.android.systemui.C0010R$drawable;
import com.android.systemui.C0018R$string;

public class AddEventItem extends IQuickOpenItem {
    private String mPackageName = getMiCalendarPackageName(this.mContext);
    private final ImageView mView;

    public String getTag() {
        return "Calendar/Add event";
    }

    public AddEventItem(RectF rectF, Region region, Context context) {
        super(rectF, region, context);
        ImageView imageView = new ImageView(context);
        this.mView = imageView;
        imageView.setImageResource(C0010R$drawable.gxzw_quick_open_add_event);
        this.mView.setScaleType(ImageView.ScaleType.FIT_XY);
    }

    public View getView() {
        return this.mView;
    }

    public Intent getIntent() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(this.mPackageName, "com.android.calendar.event.EditEventActivity"));
        intent.putExtra("from", "fingerprint");
        intent.setFlags(343932928);
        return intent;
    }

    public String getTitle() {
        return this.mContext.getString(C0018R$string.gxzw_quick_open_add_event);
    }

    public String getSubTitle() {
        return this.mContext.getString(C0018R$string.gxzw_quick_open_add_event_sub);
    }

    private static String getMiCalendarPackageName(Context context) {
        try {
            context.getPackageManager().getPackageInfo("com.xiaomi.calendar", 0);
            return "com.xiaomi.calendar";
        } catch (PackageManager.NameNotFoundException unused) {
            return "com.android.calendar";
        }
    }
}

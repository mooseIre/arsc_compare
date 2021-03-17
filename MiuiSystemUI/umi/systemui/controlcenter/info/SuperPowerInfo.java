package com.android.systemui.controlcenter.info;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import com.android.systemui.controlcenter.phone.ExpandInfoController;

public class SuperPowerInfo extends BaseInfo {
    private static String CONTENT = "superpower_systemui_remaining_time";
    private static String ICON = "superpower_systemui_icon";
    private static String TITLE = "superpower_systemui_title";
    private static String UNIT = "superpower_systemui_remaining_time_unit";
    private static final Uri URI = Uri.parse("content://com.miui.powercenter.provider");

    public SuperPowerInfo(Context context, int i, ExpandInfoController expandInfoController) {
        super(context, i, expandInfoController);
        requestData(this.mUserHandle);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.controlcenter.info.BaseInfo
    public ExpandInfoController.Info getInfoDetail() {
        ExpandInfoController.Info info = new ExpandInfoController.Info();
        try {
            Bundle call = this.mContext.getContentResolverForUser(this.mUserHandle).call(URI, "getSuperpowerSystemuiStatus", (String) null, (Bundle) null);
            if (call != null) {
                info.available = true;
                info.initialized = true;
                info.title = call.getString(TITLE);
                info.status = call.getString(CONTENT);
                info.unit = call.getString(UNIT);
                info.icon = MediaStore.Images.Media.getBitmap(this.mContext.getContentResolverForUser(this.mUserHandle), Uri.parse(call.getString(ICON)));
                info.action = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.controlcenter.info.BaseInfo
    public Uri getUri() {
        return URI;
    }
}

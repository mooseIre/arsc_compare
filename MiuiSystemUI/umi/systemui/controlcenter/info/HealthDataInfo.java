package com.android.systemui.controlcenter.info;

import android.content.Context;
import android.net.Uri;
import com.android.systemui.controlcenter.phone.ExpandInfoController;

public class HealthDataInfo extends BaseInfo {
    private static final String[] PROJECTION = {"code", "title", "content", "unit", "icon", "setup_uri", "privacy_grant_uri"};
    private static final Uri URI = Uri.parse("content://com.mi.health.provider.main/widget/steps/simple");

    public HealthDataInfo(Context context, int i, ExpandInfoController expandInfoController) {
        super(context, i, expandInfoController);
        requestData(this.mUserHandle);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00d5, code lost:
        if (r2 != null) goto L_0x00e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00de, code lost:
        if (0 == 0) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00e0, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00e3, code lost:
        return r1;
     */
    @Override // com.android.systemui.controlcenter.info.BaseInfo
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.controlcenter.phone.ExpandInfoController.Info getInfoDetail() {
        /*
        // Method dump skipped, instructions count: 234
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.info.HealthDataInfo.getInfoDetail():com.android.systemui.controlcenter.phone.ExpandInfoController$Info");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.controlcenter.info.BaseInfo
    public Uri getUri() {
        return URI;
    }
}

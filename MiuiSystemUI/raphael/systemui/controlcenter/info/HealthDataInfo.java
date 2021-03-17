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
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00d5, code lost:
        if (r2 != null) goto L_0x00e0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00de, code lost:
        if (r2 == null) goto L_0x00e3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x00e0, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x00e3, code lost:
        return r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.controlcenter.phone.ExpandInfoController.Info getInfoDetail() {
        /*
            r11 = this;
            java.lang.String r0 = "code"
            com.android.systemui.controlcenter.phone.ExpandInfoController$Info r1 = new com.android.systemui.controlcenter.phone.ExpandInfoController$Info
            r1.<init>()
            r2 = 0
            android.content.Context r3 = r11.mContext     // Catch:{ Exception -> 0x00da }
            android.os.UserHandle r4 = r11.mUserHandle     // Catch:{ Exception -> 0x00da }
            android.content.ContentResolver r5 = r3.getContentResolverForUser(r4)     // Catch:{ Exception -> 0x00da }
            android.net.Uri r6 = URI     // Catch:{ Exception -> 0x00da }
            java.lang.String[] r7 = PROJECTION     // Catch:{ Exception -> 0x00da }
            r8 = 0
            r9 = 0
            r10 = 0
            android.database.Cursor r2 = r5.query(r6, r7, r8, r9, r10)     // Catch:{ Exception -> 0x00da }
            if (r2 == 0) goto L_0x00d5
            android.os.Bundle r3 = r2.getExtras()     // Catch:{ Exception -> 0x00da }
            int r3 = r3.getInt(r0)     // Catch:{ Exception -> 0x00da }
            if (r3 == 0) goto L_0x002d
            if (r2 == 0) goto L_0x002c
            r2.close()
        L_0x002c:
            return r1
        L_0x002d:
            r2.moveToFirst()     // Catch:{ Exception -> 0x00da }
            java.lang.String r3 = "title"
            int r3 = r2.getColumnIndex(r3)     // Catch:{ Exception -> 0x00da }
            java.lang.String r3 = r2.getString(r3)     // Catch:{ Exception -> 0x00da }
            r1.title = r3     // Catch:{ Exception -> 0x00da }
            java.lang.String r3 = "content"
            int r3 = r2.getColumnIndex(r3)     // Catch:{ Exception -> 0x00da }
            java.lang.String r3 = r2.getString(r3)     // Catch:{ Exception -> 0x00da }
            r1.status = r3     // Catch:{ Exception -> 0x00da }
            java.lang.String r3 = "unit"
            int r3 = r2.getColumnIndex(r3)     // Catch:{ Exception -> 0x00da }
            java.lang.String r3 = r2.getString(r3)     // Catch:{ Exception -> 0x00da }
            r1.unit = r3     // Catch:{ Exception -> 0x00da }
            int r3 = r2.getColumnIndex(r0)     // Catch:{ Exception -> 0x00da }
            int r3 = r2.getInt(r3)     // Catch:{ Exception -> 0x00da }
            r4 = 1
            if (r3 == r4) goto L_0x0061
            r3 = r4
            goto L_0x0062
        L_0x0061:
            r3 = 0
        L_0x0062:
            r1.initialized = r3     // Catch:{ Exception -> 0x00da }
            r1.available = r4     // Catch:{ Exception -> 0x00da }
            java.lang.String r3 = "setup_uri"
            int r3 = r2.getColumnIndex(r3)     // Catch:{ Exception -> 0x00da }
            java.lang.String r3 = r2.getString(r3)     // Catch:{ Exception -> 0x00da }
            r1.uri = r3     // Catch:{ Exception -> 0x00da }
            android.content.Context r3 = r11.mContext     // Catch:{ Exception -> 0x00da }
            java.lang.String r4 = "com.mi.health"
            r5 = 2
            android.os.UserHandle r6 = r11.mUserHandle     // Catch:{ Exception -> 0x00da }
            android.content.Context r3 = r3.createPackageContextAsUser(r4, r5, r6)     // Catch:{ Exception -> 0x00da }
            android.content.res.Resources r4 = r3.getResources()     // Catch:{ Exception -> 0x00da }
            java.lang.String r5 = "icon"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ Exception -> 0x00da }
            java.lang.String r5 = r2.getString(r5)     // Catch:{ Exception -> 0x00da }
            java.lang.String r6 = "drawable"
            java.lang.String r7 = r3.getPackageName()     // Catch:{ Exception -> 0x00da }
            int r4 = r4.getIdentifier(r5, r6, r7)     // Catch:{ Exception -> 0x00da }
            android.graphics.drawable.Drawable r3 = r3.getDrawable(r4)     // Catch:{ Exception -> 0x00da }
            android.graphics.drawable.BitmapDrawable r3 = (android.graphics.drawable.BitmapDrawable) r3     // Catch:{ Exception -> 0x00da }
            if (r3 == 0) goto L_0x00a3
            android.graphics.Bitmap r3 = r3.getBitmap()     // Catch:{ Exception -> 0x00da }
            r1.icon = r3     // Catch:{ Exception -> 0x00da }
        L_0x00a3:
            android.graphics.Bitmap r3 = r1.icon     // Catch:{ Exception -> 0x00da }
            if (r3 != 0) goto L_0x00ac
            android.graphics.Bitmap r11 = r11.mBpBitmap     // Catch:{ Exception -> 0x00da }
            r1.icon = r11     // Catch:{ Exception -> 0x00da }
            goto L_0x00b0
        L_0x00ac:
            android.graphics.Bitmap r3 = r1.icon     // Catch:{ Exception -> 0x00da }
            r11.mBpBitmap = r3     // Catch:{ Exception -> 0x00da }
        L_0x00b0:
            java.lang.String r11 = "HealthDataProvider"
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00da }
            r3.<init>()     // Catch:{ Exception -> 0x00da }
            java.lang.String r4 = r1.toString()     // Catch:{ Exception -> 0x00da }
            r3.append(r4)     // Catch:{ Exception -> 0x00da }
            java.lang.String r4 = "     "
            r3.append(r4)     // Catch:{ Exception -> 0x00da }
            int r0 = r2.getColumnIndex(r0)     // Catch:{ Exception -> 0x00da }
            int r0 = r2.getInt(r0)     // Catch:{ Exception -> 0x00da }
            r3.append(r0)     // Catch:{ Exception -> 0x00da }
            java.lang.String r0 = r3.toString()     // Catch:{ Exception -> 0x00da }
            android.util.Log.d(r11, r0)     // Catch:{ Exception -> 0x00da }
        L_0x00d5:
            if (r2 == 0) goto L_0x00e3
            goto L_0x00e0
        L_0x00d8:
            r11 = move-exception
            goto L_0x00e4
        L_0x00da:
            r11 = move-exception
            r11.printStackTrace()     // Catch:{ all -> 0x00d8 }
            if (r2 == 0) goto L_0x00e3
        L_0x00e0:
            r2.close()
        L_0x00e3:
            return r1
        L_0x00e4:
            if (r2 == 0) goto L_0x00e9
            r2.close()
        L_0x00e9:
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.info.HealthDataInfo.getInfoDetail():com.android.systemui.controlcenter.phone.ExpandInfoController$Info");
    }

    /* access modifiers changed from: protected */
    public Uri getUri() {
        return URI;
    }
}

package com.android.systemui.controlcenter.info;

import android.content.Context;
import android.net.Uri;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ExpandInfoController;
import com.android.systemui.statusbar.policy.NetworkController;

public class DataBillInfo extends BaseInfo implements NetworkController.SignalCallback {
    private static final String[] PROJECT = {"bill_name", "bill_unit", "bill_unit", "bill_icon", "sim_slot", "package_type", "click_action"};
    private static final Uri URI = Uri.parse("content://com.miui.networkassistant.provider/datausage_status_detailed");
    private static final Uri URI_ACTION = Uri.parse("content://vsimcore.setting");
    private int mDataSlot;
    private NetworkController mNetworkController;
    private boolean mNoSims;

    public DataBillInfo(Context context, int i, ExpandInfoController expandInfoController) {
        super(context, i, expandInfoController);
        this.mInfo.initialized = true;
        NetworkController networkController = (NetworkController) Dependency.get(NetworkController.class);
        this.mNetworkController = networkController;
        networkController.addCallback(this);
        requestData(this.mUserHandle);
    }

    public void setIsDefaultDataSim(int i, boolean z) {
        if (z && this.mDataSlot != i) {
            this.mDataSlot = i;
            refresh();
        }
    }

    public void setNoSims(boolean z, boolean z2) {
        boolean z3 = this.mNoSims;
        if (z3 != z) {
            boolean z4 = !z3 && z;
            this.mNoSims = z;
            if (z4) {
                this.mDataSlot = 0;
                refresh();
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003e, code lost:
        r1.title = r3.getString(r3.getColumnIndex("bill_name"));
        r1.status = r3.getString(r3.getColumnIndex("bill_value"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005d, code lost:
        if (android.text.TextUtils.isEmpty(r1.title) == false) goto L_0x006a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0065, code lost:
        if (android.text.TextUtils.isEmpty(r1.status) != false) goto L_0x0068;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0068, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x006a, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x006b, code lost:
        r1.available = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0076, code lost:
        if (r3.getInt(r3.getColumnIndex("package_type")) == -1) goto L_0x0079;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0078, code lost:
        r4 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0079, code lost:
        r1.initialized = r4;
        android.util.Log.d("DataBillProvider", "packageType:" + r3.getInt(r3.getColumnIndex("package_type")));
        r1.unit = r3.getString(r3.getColumnIndex("bill_unit"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        r1.icon = android.provider.MediaStore.Images.Media.getBitmap(r11.mContext.getContentResolverForUser(r11.mUserHandle), android.net.Uri.parse(r3.getString(r3.getColumnIndex("bill_icon"))));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00c0, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0109, code lost:
        r11 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x010a, code lost:
        r2 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x010c, code lost:
        r11 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x010d, code lost:
        r2 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x011d, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x0123, code lost:
        r2.close();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0109 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:4:0x001d] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0111  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x011d  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0123  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.controlcenter.phone.ExpandInfoController.Info getInfoDetail() {
        /*
            r11 = this;
            java.lang.String r0 = "package_type"
            com.android.systemui.controlcenter.phone.ExpandInfoController$Info r1 = new com.android.systemui.controlcenter.phone.ExpandInfoController$Info
            r1.<init>()
            r2 = 0
            android.content.Context r3 = r11.mContext     // Catch:{ Exception -> 0x0117 }
            android.os.UserHandle r4 = r11.mUserHandle     // Catch:{ Exception -> 0x0117 }
            android.content.ContentResolver r5 = r3.getContentResolverForUser(r4)     // Catch:{ Exception -> 0x0117 }
            android.net.Uri r6 = URI     // Catch:{ Exception -> 0x0117 }
            java.lang.String[] r7 = PROJECT     // Catch:{ Exception -> 0x0117 }
            r8 = 0
            r9 = 0
            r10 = 0
            android.database.Cursor r3 = r5.query(r6, r7, r8, r9, r10)     // Catch:{ Exception -> 0x0117 }
            if (r3 == 0) goto L_0x010f
            boolean r4 = r3.moveToFirst()     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            if (r4 == 0) goto L_0x010f
            r4 = 0
            r5 = r4
        L_0x0025:
            int r6 = r3.getColumnCount()     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            java.lang.String r7 = "DataBillProvider"
            if (r5 >= r6) goto L_0x0101
            r3.move(r5)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            java.lang.String r6 = "sim_slot"
            int r6 = r3.getColumnIndex(r6)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            int r6 = r3.getInt(r6)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            int r8 = r11.mDataSlot     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            if (r6 != r8) goto L_0x00fd
            java.lang.String r5 = "bill_name"
            int r5 = r3.getColumnIndex(r5)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            java.lang.String r5 = r3.getString(r5)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            r1.title = r5     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            java.lang.String r5 = "bill_value"
            int r5 = r3.getColumnIndex(r5)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            java.lang.String r5 = r3.getString(r5)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            r1.status = r5     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            java.lang.String r5 = r1.title     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            r6 = 1
            if (r5 == 0) goto L_0x006a
            java.lang.String r5 = r1.status     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            if (r5 != 0) goto L_0x0068
            goto L_0x006a
        L_0x0068:
            r5 = r4
            goto L_0x006b
        L_0x006a:
            r5 = r6
        L_0x006b:
            r1.available = r5     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            int r5 = r3.getColumnIndex(r0)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            int r5 = r3.getInt(r5)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            r8 = -1
            if (r5 == r8) goto L_0x0079
            r4 = r6
        L_0x0079:
            r1.initialized = r4     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            r4.<init>()     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            java.lang.String r5 = "packageType:"
            r4.append(r5)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            int r0 = r3.getColumnIndex(r0)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            int r0 = r3.getInt(r0)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            r4.append(r0)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            java.lang.String r0 = r4.toString()     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            android.util.Log.d(r7, r0)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            java.lang.String r0 = "bill_unit"
            int r0 = r3.getColumnIndex(r0)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            java.lang.String r0 = r3.getString(r0)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            r1.unit = r0     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            android.content.Context r0 = r11.mContext     // Catch:{ Exception -> 0x00c0, all -> 0x0109 }
            android.os.UserHandle r4 = r11.mUserHandle     // Catch:{ Exception -> 0x00c0, all -> 0x0109 }
            android.content.ContentResolver r0 = r0.getContentResolverForUser(r4)     // Catch:{ Exception -> 0x00c0, all -> 0x0109 }
            java.lang.String r4 = "bill_icon"
            int r4 = r3.getColumnIndex(r4)     // Catch:{ Exception -> 0x00c0, all -> 0x0109 }
            java.lang.String r4 = r3.getString(r4)     // Catch:{ Exception -> 0x00c0, all -> 0x0109 }
            android.net.Uri r4 = android.net.Uri.parse(r4)     // Catch:{ Exception -> 0x00c0, all -> 0x0109 }
            android.graphics.Bitmap r0 = android.provider.MediaStore.Images.Media.getBitmap(r0, r4)     // Catch:{ Exception -> 0x00c0, all -> 0x0109 }
            r1.icon = r0     // Catch:{ Exception -> 0x00c0, all -> 0x0109 }
            goto L_0x00c4
        L_0x00c0:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
        L_0x00c4:
            android.graphics.Bitmap r0 = r1.icon     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            if (r0 != 0) goto L_0x00cd
            android.graphics.Bitmap r0 = r11.mBpBitmap     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            r1.icon = r0     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            goto L_0x00d1
        L_0x00cd:
            android.graphics.Bitmap r0 = r1.icon     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            r11.mBpBitmap = r0     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
        L_0x00d1:
            android.os.Bundle r0 = new android.os.Bundle     // Catch:{ Exception -> 0x00f8, all -> 0x0109 }
            r0.<init>()     // Catch:{ Exception -> 0x00f8, all -> 0x0109 }
            java.lang.String r4 = "slotId"
            int r5 = r11.mDataSlot     // Catch:{ Exception -> 0x00f8, all -> 0x0109 }
            r0.putInt(r4, r5)     // Catch:{ Exception -> 0x00f8, all -> 0x0109 }
            android.content.Context r4 = r11.mContext     // Catch:{ Exception -> 0x00f8, all -> 0x0109 }
            android.os.UserHandle r11 = r11.mUserHandle     // Catch:{ Exception -> 0x00f8, all -> 0x0109 }
            android.content.ContentResolver r11 = r4.getContentResolverForUser(r11)     // Catch:{ Exception -> 0x00f8, all -> 0x0109 }
            android.net.Uri r4 = URI_ACTION     // Catch:{ Exception -> 0x00f8, all -> 0x0109 }
            java.lang.String r5 = "getIntentforControlCenter"
            android.os.Bundle r11 = r11.call(r4, r5, r2, r0)     // Catch:{ Exception -> 0x00f8, all -> 0x0109 }
            if (r11 == 0) goto L_0x0101
            java.lang.String r0 = "intentUri"
            java.lang.String r11 = r11.getString(r0)     // Catch:{ Exception -> 0x00f8, all -> 0x0109 }
            r1.uri = r11     // Catch:{ Exception -> 0x00f8, all -> 0x0109 }
            goto L_0x0101
        L_0x00f8:
            r11 = move-exception
            r11.printStackTrace()     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            goto L_0x0101
        L_0x00fd:
            int r5 = r5 + 1
            goto L_0x0025
        L_0x0101:
            java.lang.String r11 = r1.toString()     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            android.util.Log.d(r7, r11)     // Catch:{ Exception -> 0x010c, all -> 0x0109 }
            goto L_0x010f
        L_0x0109:
            r11 = move-exception
            r2 = r3
            goto L_0x0121
        L_0x010c:
            r11 = move-exception
            r2 = r3
            goto L_0x0118
        L_0x010f:
            if (r3 == 0) goto L_0x0120
            r3.close()
            goto L_0x0120
        L_0x0115:
            r11 = move-exception
            goto L_0x0121
        L_0x0117:
            r11 = move-exception
        L_0x0118:
            r11.printStackTrace()     // Catch:{ all -> 0x0115 }
            if (r2 == 0) goto L_0x0120
            r2.close()
        L_0x0120:
            return r1
        L_0x0121:
            if (r2 == 0) goto L_0x0126
            r2.close()
        L_0x0126:
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.info.DataBillInfo.getInfoDetail():com.android.systemui.controlcenter.phone.ExpandInfoController$Info");
    }

    /* access modifiers changed from: protected */
    public Uri getUri() {
        return URI;
    }
}

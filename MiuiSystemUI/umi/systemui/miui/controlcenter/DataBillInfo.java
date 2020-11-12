package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.net.Uri;
import com.android.systemui.Dependency;
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
        networkController.addCallback((NetworkController.SignalCallback) this);
        requestData(this.mUserHandle);
    }

    public void setIsDefaultDataSim(int i, boolean z) {
        if (z && this.mDataSlot != i) {
            this.mDataSlot = i;
            refresh(2500);
        }
    }

    public void setNoSims(boolean z) {
        boolean z2 = this.mNoSims;
        if (z2 != z) {
            boolean z3 = !z2 && z;
            this.mNoSims = z;
            refresh(2500);
            if (z3) {
                this.mDataSlot = 0;
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003b, code lost:
        r4 = r2.getInt(r2.getColumnIndex("package_type"));
        r0.title = r2.getString(r2.getColumnIndex("bill_name"));
        r0.status = r2.getString(r2.getColumnIndex("bill_value"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0064, code lost:
        if (android.text.TextUtils.isEmpty(r0.title) == false) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x006c, code lost:
        if (android.text.TextUtils.isEmpty(r0.status) != false) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x006f, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0071, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0072, code lost:
        r0.available = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0075, code lost:
        if (r4 == -1) goto L_0x0078;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0077, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0078, code lost:
        r0.initialized = r3;
        r0.unit = r2.getString(r2.getColumnIndex("bill_unit"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r0.icon = android.provider.MediaStore.Images.Media.getBitmap(r10.mContext.getContentResolverForUser(r10.mUserHandle), android.net.Uri.parse(r2.getString(r2.getColumnIndex("bill_icon"))));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00a3, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:?, code lost:
        r3.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00ef, code lost:
        r10 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00f0, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00f2, code lost:
        r10 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00f3, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0109, code lost:
        r1.close();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00ef A[ExcHandler: all (th java.lang.Throwable), Splitter:B:4:0x001b] */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00f7  */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x0103  */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0109  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.miui.controlcenter.ExpandInfoController.Info getInfoDetail() {
        /*
            r10 = this;
            com.android.systemui.miui.controlcenter.ExpandInfoController$Info r0 = new com.android.systemui.miui.controlcenter.ExpandInfoController$Info
            r0.<init>()
            r1 = 0
            android.content.Context r2 = r10.mContext     // Catch:{ Exception -> 0x00fd }
            android.os.UserHandle r3 = r10.mUserHandle     // Catch:{ Exception -> 0x00fd }
            android.content.ContentResolver r4 = r2.getContentResolverForUser(r3)     // Catch:{ Exception -> 0x00fd }
            android.net.Uri r5 = URI     // Catch:{ Exception -> 0x00fd }
            java.lang.String[] r6 = PROJECT     // Catch:{ Exception -> 0x00fd }
            r7 = 0
            r8 = 0
            r9 = 0
            android.database.Cursor r2 = r4.query(r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x00fd }
            if (r2 == 0) goto L_0x00f5
            boolean r3 = r2.moveToFirst()     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            if (r3 == 0) goto L_0x00f5
            r3 = 0
            r4 = r3
        L_0x0023:
            int r5 = r2.getColumnCount()     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            if (r4 >= r5) goto L_0x00e5
            r2.move(r4)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            java.lang.String r5 = "sim_slot"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            int r5 = r2.getInt(r5)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            int r6 = r10.mDataSlot     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            if (r5 != r6) goto L_0x00e1
            java.lang.String r4 = "package_type"
            int r4 = r2.getColumnIndex(r4)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            int r4 = r2.getInt(r4)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            java.lang.String r5 = "bill_name"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            java.lang.String r5 = r2.getString(r5)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            r0.title = r5     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            java.lang.String r5 = "bill_value"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            java.lang.String r5 = r2.getString(r5)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            r0.status = r5     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            java.lang.String r5 = r0.title     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            r6 = 1
            if (r5 == 0) goto L_0x0071
            java.lang.String r5 = r0.status     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            if (r5 != 0) goto L_0x006f
            goto L_0x0071
        L_0x006f:
            r5 = r3
            goto L_0x0072
        L_0x0071:
            r5 = r6
        L_0x0072:
            r0.available = r5     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            r5 = -1
            if (r4 == r5) goto L_0x0078
            r3 = r6
        L_0x0078:
            r0.initialized = r3     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            java.lang.String r3 = "bill_unit"
            int r3 = r2.getColumnIndex(r3)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            java.lang.String r3 = r2.getString(r3)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            r0.unit = r3     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            android.content.Context r3 = r10.mContext     // Catch:{ Exception -> 0x00a3, all -> 0x00ef }
            android.os.UserHandle r4 = r10.mUserHandle     // Catch:{ Exception -> 0x00a3, all -> 0x00ef }
            android.content.ContentResolver r3 = r3.getContentResolverForUser(r4)     // Catch:{ Exception -> 0x00a3, all -> 0x00ef }
            java.lang.String r4 = "bill_icon"
            int r4 = r2.getColumnIndex(r4)     // Catch:{ Exception -> 0x00a3, all -> 0x00ef }
            java.lang.String r4 = r2.getString(r4)     // Catch:{ Exception -> 0x00a3, all -> 0x00ef }
            android.net.Uri r4 = android.net.Uri.parse(r4)     // Catch:{ Exception -> 0x00a3, all -> 0x00ef }
            android.graphics.Bitmap r3 = android.provider.MediaStore.Images.Media.getBitmap(r3, r4)     // Catch:{ Exception -> 0x00a3, all -> 0x00ef }
            r0.icon = r3     // Catch:{ Exception -> 0x00a3, all -> 0x00ef }
            goto L_0x00a7
        L_0x00a3:
            r3 = move-exception
            r3.printStackTrace()     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
        L_0x00a7:
            android.graphics.Bitmap r3 = r0.icon     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            if (r3 != 0) goto L_0x00b0
            android.graphics.Bitmap r3 = r10.mBpBitmap     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            r0.icon = r3     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            goto L_0x00b4
        L_0x00b0:
            android.graphics.Bitmap r3 = r0.icon     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            r10.mBpBitmap = r3     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
        L_0x00b4:
            android.os.Bundle r3 = new android.os.Bundle     // Catch:{ Exception -> 0x00dc, all -> 0x00ef }
            r3.<init>()     // Catch:{ Exception -> 0x00dc, all -> 0x00ef }
            java.lang.String r4 = "slotId"
            int r5 = r10.mDataSlot     // Catch:{ Exception -> 0x00dc, all -> 0x00ef }
            r3.putInt(r4, r5)     // Catch:{ Exception -> 0x00dc, all -> 0x00ef }
            android.content.Context r4 = r10.mContext     // Catch:{ Exception -> 0x00dc, all -> 0x00ef }
            android.os.UserHandle r10 = r10.mUserHandle     // Catch:{ Exception -> 0x00dc, all -> 0x00ef }
            android.content.ContentResolver r10 = r4.getContentResolverForUser(r10)     // Catch:{ Exception -> 0x00dc, all -> 0x00ef }
            android.net.Uri r4 = URI_ACTION     // Catch:{ Exception -> 0x00dc, all -> 0x00ef }
            java.lang.String r5 = "getIntentforControlCenter"
            android.os.Bundle r10 = r10.call(r4, r5, r1, r3)     // Catch:{ Exception -> 0x00dc, all -> 0x00ef }
            if (r10 == 0) goto L_0x00e5
            java.lang.String r1 = "intentUri"
            java.lang.String r10 = r10.getString(r1)     // Catch:{ Exception -> 0x00dc, all -> 0x00ef }
            r0.uri = r10     // Catch:{ Exception -> 0x00dc, all -> 0x00ef }
            goto L_0x00e5
        L_0x00dc:
            r10 = move-exception
            r10.printStackTrace()     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            goto L_0x00e5
        L_0x00e1:
            int r4 = r4 + 1
            goto L_0x0023
        L_0x00e5:
            java.lang.String r10 = "DataBillProvider"
            java.lang.String r1 = r0.toString()     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            android.util.Log.d(r10, r1)     // Catch:{ Exception -> 0x00f2, all -> 0x00ef }
            goto L_0x00f5
        L_0x00ef:
            r10 = move-exception
            r1 = r2
            goto L_0x0107
        L_0x00f2:
            r10 = move-exception
            r1 = r2
            goto L_0x00fe
        L_0x00f5:
            if (r2 == 0) goto L_0x0106
            r2.close()
            goto L_0x0106
        L_0x00fb:
            r10 = move-exception
            goto L_0x0107
        L_0x00fd:
            r10 = move-exception
        L_0x00fe:
            r10.printStackTrace()     // Catch:{ all -> 0x00fb }
            if (r1 == 0) goto L_0x0106
            r1.close()
        L_0x0106:
            return r0
        L_0x0107:
            if (r1 == 0) goto L_0x010c
            r1.close()
        L_0x010c:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.controlcenter.DataBillInfo.getInfoDetail():com.android.systemui.miui.controlcenter.ExpandInfoController$Info");
    }

    /* access modifiers changed from: protected */
    public Uri getUri() {
        return URI;
    }
}

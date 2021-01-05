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
            refresh(2500);
        }
    }

    public void setNoSims(boolean z, boolean z2) {
        boolean z3 = this.mNoSims;
        if (z3 != z) {
            boolean z4 = !z3 && z;
            this.mNoSims = z;
            refresh(2500);
            if (z4) {
                this.mDataSlot = 0;
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003c, code lost:
        r4 = r2.getInt(r2.getColumnIndex("package_type"));
        r0.title = r2.getString(r2.getColumnIndex("bill_name"));
        r0.status = r2.getString(r2.getColumnIndex("bill_value"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0065, code lost:
        if (android.text.TextUtils.isEmpty(r0.title) == false) goto L_0x0072;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x006d, code lost:
        if (android.text.TextUtils.isEmpty(r0.status) != false) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0070, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0072, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0073, code lost:
        r0.available = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0076, code lost:
        if (r4 == -1) goto L_0x0079;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0078, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0079, code lost:
        r0.initialized = r3;
        android.util.Log.d("DataBillProvider", "packageType:" + r4);
        r0.unit = r2.getString(r2.getColumnIndex("bill_unit"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        r0.icon = android.provider.MediaStore.Images.Media.getBitmap(r10.mContext.getContentResolverForUser(r10.mUserHandle), android.net.Uri.parse(r2.getString(r2.getColumnIndex("bill_icon"))));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00b8, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r3.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0101, code lost:
        r10 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0102, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0104, code lost:
        r10 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0105, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x011b, code lost:
        r1.close();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x0101 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:4:0x001b] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x0109  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x0115  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x011b  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.controlcenter.phone.ExpandInfoController.Info getInfoDetail() {
        /*
            r10 = this;
            com.android.systemui.controlcenter.phone.ExpandInfoController$Info r0 = new com.android.systemui.controlcenter.phone.ExpandInfoController$Info
            r0.<init>()
            r1 = 0
            android.content.Context r2 = r10.mContext     // Catch:{ Exception -> 0x010f }
            android.os.UserHandle r3 = r10.mUserHandle     // Catch:{ Exception -> 0x010f }
            android.content.ContentResolver r4 = r2.getContentResolverForUser(r3)     // Catch:{ Exception -> 0x010f }
            android.net.Uri r5 = URI     // Catch:{ Exception -> 0x010f }
            java.lang.String[] r6 = PROJECT     // Catch:{ Exception -> 0x010f }
            r7 = 0
            r8 = 0
            r9 = 0
            android.database.Cursor r2 = r4.query(r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x010f }
            if (r2 == 0) goto L_0x0107
            boolean r3 = r2.moveToFirst()     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            if (r3 == 0) goto L_0x0107
            r3 = 0
            r4 = r3
        L_0x0023:
            int r5 = r2.getColumnCount()     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            java.lang.String r6 = "DataBillProvider"
            if (r4 >= r5) goto L_0x00f9
            r2.move(r4)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            java.lang.String r5 = "sim_slot"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            int r5 = r2.getInt(r5)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            int r7 = r10.mDataSlot     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            if (r5 != r7) goto L_0x00f5
            java.lang.String r4 = "package_type"
            int r4 = r2.getColumnIndex(r4)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            int r4 = r2.getInt(r4)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            java.lang.String r5 = "bill_name"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            java.lang.String r5 = r2.getString(r5)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            r0.title = r5     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            java.lang.String r5 = "bill_value"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            java.lang.String r5 = r2.getString(r5)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            r0.status = r5     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            java.lang.String r5 = r0.title     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            r7 = 1
            if (r5 == 0) goto L_0x0072
            java.lang.String r5 = r0.status     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            boolean r5 = android.text.TextUtils.isEmpty(r5)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            if (r5 != 0) goto L_0x0070
            goto L_0x0072
        L_0x0070:
            r5 = r3
            goto L_0x0073
        L_0x0072:
            r5 = r7
        L_0x0073:
            r0.available = r5     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            r5 = -1
            if (r4 == r5) goto L_0x0079
            r3 = r7
        L_0x0079:
            r0.initialized = r3     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            java.lang.StringBuilder r3 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            r3.<init>()     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            java.lang.String r5 = "packageType:"
            r3.append(r5)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            r3.append(r4)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            java.lang.String r3 = r3.toString()     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            android.util.Log.d(r6, r3)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            java.lang.String r3 = "bill_unit"
            int r3 = r2.getColumnIndex(r3)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            java.lang.String r3 = r2.getString(r3)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            r0.unit = r3     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            android.content.Context r3 = r10.mContext     // Catch:{ Exception -> 0x00b8, all -> 0x0101 }
            android.os.UserHandle r4 = r10.mUserHandle     // Catch:{ Exception -> 0x00b8, all -> 0x0101 }
            android.content.ContentResolver r3 = r3.getContentResolverForUser(r4)     // Catch:{ Exception -> 0x00b8, all -> 0x0101 }
            java.lang.String r4 = "bill_icon"
            int r4 = r2.getColumnIndex(r4)     // Catch:{ Exception -> 0x00b8, all -> 0x0101 }
            java.lang.String r4 = r2.getString(r4)     // Catch:{ Exception -> 0x00b8, all -> 0x0101 }
            android.net.Uri r4 = android.net.Uri.parse(r4)     // Catch:{ Exception -> 0x00b8, all -> 0x0101 }
            android.graphics.Bitmap r3 = android.provider.MediaStore.Images.Media.getBitmap(r3, r4)     // Catch:{ Exception -> 0x00b8, all -> 0x0101 }
            r0.icon = r3     // Catch:{ Exception -> 0x00b8, all -> 0x0101 }
            goto L_0x00bc
        L_0x00b8:
            r3 = move-exception
            r3.printStackTrace()     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
        L_0x00bc:
            android.graphics.Bitmap r3 = r0.icon     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            if (r3 != 0) goto L_0x00c5
            android.graphics.Bitmap r3 = r10.mBpBitmap     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            r0.icon = r3     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            goto L_0x00c9
        L_0x00c5:
            android.graphics.Bitmap r3 = r0.icon     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            r10.mBpBitmap = r3     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
        L_0x00c9:
            android.os.Bundle r3 = new android.os.Bundle     // Catch:{ Exception -> 0x00f0, all -> 0x0101 }
            r3.<init>()     // Catch:{ Exception -> 0x00f0, all -> 0x0101 }
            java.lang.String r4 = "slotId"
            int r5 = r10.mDataSlot     // Catch:{ Exception -> 0x00f0, all -> 0x0101 }
            r3.putInt(r4, r5)     // Catch:{ Exception -> 0x00f0, all -> 0x0101 }
            android.content.Context r4 = r10.mContext     // Catch:{ Exception -> 0x00f0, all -> 0x0101 }
            android.os.UserHandle r10 = r10.mUserHandle     // Catch:{ Exception -> 0x00f0, all -> 0x0101 }
            android.content.ContentResolver r10 = r4.getContentResolverForUser(r10)     // Catch:{ Exception -> 0x00f0, all -> 0x0101 }
            android.net.Uri r4 = URI_ACTION     // Catch:{ Exception -> 0x00f0, all -> 0x0101 }
            java.lang.String r5 = "getIntentforControlCenter"
            android.os.Bundle r10 = r10.call(r4, r5, r1, r3)     // Catch:{ Exception -> 0x00f0, all -> 0x0101 }
            if (r10 == 0) goto L_0x00f9
            java.lang.String r1 = "intentUri"
            java.lang.String r10 = r10.getString(r1)     // Catch:{ Exception -> 0x00f0, all -> 0x0101 }
            r0.uri = r10     // Catch:{ Exception -> 0x00f0, all -> 0x0101 }
            goto L_0x00f9
        L_0x00f0:
            r10 = move-exception
            r10.printStackTrace()     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            goto L_0x00f9
        L_0x00f5:
            int r4 = r4 + 1
            goto L_0x0023
        L_0x00f9:
            java.lang.String r10 = r0.toString()     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            android.util.Log.d(r6, r10)     // Catch:{ Exception -> 0x0104, all -> 0x0101 }
            goto L_0x0107
        L_0x0101:
            r10 = move-exception
            r1 = r2
            goto L_0x0119
        L_0x0104:
            r10 = move-exception
            r1 = r2
            goto L_0x0110
        L_0x0107:
            if (r2 == 0) goto L_0x0118
            r2.close()
            goto L_0x0118
        L_0x010d:
            r10 = move-exception
            goto L_0x0119
        L_0x010f:
            r10 = move-exception
        L_0x0110:
            r10.printStackTrace()     // Catch:{ all -> 0x010d }
            if (r1 == 0) goto L_0x0118
            r1.close()
        L_0x0118:
            return r0
        L_0x0119:
            if (r1 == 0) goto L_0x011e
            r1.close()
        L_0x011e:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.info.DataBillInfo.getInfoDetail():com.android.systemui.controlcenter.phone.ExpandInfoController$Info");
    }

    /* access modifiers changed from: protected */
    public Uri getUri() {
        return URI;
    }
}

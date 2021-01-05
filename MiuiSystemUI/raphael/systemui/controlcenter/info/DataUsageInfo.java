package com.android.systemui.controlcenter.info;

import android.content.Context;
import android.net.Uri;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ExpandInfoController;
import com.android.systemui.statusbar.policy.NetworkController;

public class DataUsageInfo extends BaseInfo implements NetworkController.SignalCallback {
    private static final String[] PROJECT = {"traffic_name", "traffic_value", "traffic_unit", "traffic_icon", "sim_slot", "package_type", "click_action"};
    private static final Uri URI = Uri.parse("content://com.miui.networkassistant.provider/datausage_status_detailed");
    private static final Uri URI_ACTION = Uri.parse("content://vsimcore.setting");
    private int mDataSlot;
    private NetworkController mNetworkController;
    private boolean mNoSims;

    public DataUsageInfo(Context context, int i, ExpandInfoController expandInfoController) {
        super(context, i, expandInfoController);
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
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x003a, code lost:
        r4 = r2.getInt(r2.getColumnIndex("package_type"));
        r0.title = r2.getString(r2.getColumnIndex("traffic_name"));
        r0.status = r2.getString(r2.getColumnIndex("traffic_value"));
        r0.unit = r2.getString(r2.getColumnIndex("traffic_unit"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x006a, code lost:
        if (r4 == -1) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x006c, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x006d, code lost:
        r0.initialized = r3;
        r0.available = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:?, code lost:
        r0.icon = android.provider.MediaStore.Images.Media.getBitmap(r10.mContext.getContentResolverForUser(r10.mUserHandle), android.net.Uri.parse(r2.getString(r2.getColumnIndex("traffic_icon"))));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x008e, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        r3.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00d9, code lost:
        r10 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00da, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00dc, code lost:
        r10 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00dd, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00ed, code lost:
        r1.close();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00d9 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:4:0x001b] */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00e1  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00ed  */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00f3  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.controlcenter.phone.ExpandInfoController.Info getInfoDetail() {
        /*
            r10 = this;
            com.android.systemui.controlcenter.phone.ExpandInfoController$Info r0 = new com.android.systemui.controlcenter.phone.ExpandInfoController$Info
            r0.<init>()
            r1 = 0
            android.content.Context r2 = r10.mContext     // Catch:{ Exception -> 0x00e7 }
            android.os.UserHandle r3 = r10.mUserHandle     // Catch:{ Exception -> 0x00e7 }
            android.content.ContentResolver r4 = r2.getContentResolverForUser(r3)     // Catch:{ Exception -> 0x00e7 }
            android.net.Uri r5 = URI     // Catch:{ Exception -> 0x00e7 }
            java.lang.String[] r6 = PROJECT     // Catch:{ Exception -> 0x00e7 }
            r7 = 0
            r8 = 0
            r9 = 0
            android.database.Cursor r2 = r4.query(r5, r6, r7, r8, r9)     // Catch:{ Exception -> 0x00e7 }
            if (r2 == 0) goto L_0x00df
            boolean r3 = r2.moveToFirst()     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            if (r3 == 0) goto L_0x00df
            r3 = 0
            r4 = r3
        L_0x0023:
            int r5 = r2.getColumnCount()     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            if (r4 >= r5) goto L_0x00cf
            r2.move(r4)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            java.lang.String r5 = "sim_slot"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            int r5 = r2.getInt(r5)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            int r6 = r10.mDataSlot     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            if (r5 != r6) goto L_0x00cb
            java.lang.String r4 = "package_type"
            int r4 = r2.getColumnIndex(r4)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            int r4 = r2.getInt(r4)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            java.lang.String r5 = "traffic_name"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            java.lang.String r5 = r2.getString(r5)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            r0.title = r5     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            java.lang.String r5 = "traffic_value"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            java.lang.String r5 = r2.getString(r5)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            r0.status = r5     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            java.lang.String r5 = "traffic_unit"
            int r5 = r2.getColumnIndex(r5)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            java.lang.String r5 = r2.getString(r5)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            r0.unit = r5     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            r5 = -1
            r6 = 1
            if (r4 == r5) goto L_0x006d
            r3 = r6
        L_0x006d:
            r0.initialized = r3     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            r0.available = r6     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            java.lang.String r3 = "traffic_icon"
            int r3 = r2.getColumnIndex(r3)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            java.lang.String r3 = r2.getString(r3)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            android.content.Context r4 = r10.mContext     // Catch:{ Exception -> 0x008e, all -> 0x00d9 }
            android.os.UserHandle r5 = r10.mUserHandle     // Catch:{ Exception -> 0x008e, all -> 0x00d9 }
            android.content.ContentResolver r4 = r4.getContentResolverForUser(r5)     // Catch:{ Exception -> 0x008e, all -> 0x00d9 }
            android.net.Uri r3 = android.net.Uri.parse(r3)     // Catch:{ Exception -> 0x008e, all -> 0x00d9 }
            android.graphics.Bitmap r3 = android.provider.MediaStore.Images.Media.getBitmap(r4, r3)     // Catch:{ Exception -> 0x008e, all -> 0x00d9 }
            r0.icon = r3     // Catch:{ Exception -> 0x008e, all -> 0x00d9 }
            goto L_0x0092
        L_0x008e:
            r3 = move-exception
            r3.printStackTrace()     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
        L_0x0092:
            android.graphics.Bitmap r3 = r0.icon     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            if (r3 != 0) goto L_0x009b
            android.graphics.Bitmap r3 = r10.mBpBitmap     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            r0.icon = r3     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            goto L_0x009f
        L_0x009b:
            android.graphics.Bitmap r3 = r0.icon     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            r10.mBpBitmap = r3     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
        L_0x009f:
            android.os.Bundle r3 = new android.os.Bundle     // Catch:{ Exception -> 0x00c6, all -> 0x00d9 }
            r3.<init>()     // Catch:{ Exception -> 0x00c6, all -> 0x00d9 }
            java.lang.String r4 = "slotId"
            int r5 = r10.mDataSlot     // Catch:{ Exception -> 0x00c6, all -> 0x00d9 }
            r3.putInt(r4, r5)     // Catch:{ Exception -> 0x00c6, all -> 0x00d9 }
            android.content.Context r4 = r10.mContext     // Catch:{ Exception -> 0x00c6, all -> 0x00d9 }
            android.os.UserHandle r10 = r10.mUserHandle     // Catch:{ Exception -> 0x00c6, all -> 0x00d9 }
            android.content.ContentResolver r10 = r4.getContentResolverForUser(r10)     // Catch:{ Exception -> 0x00c6, all -> 0x00d9 }
            android.net.Uri r4 = URI_ACTION     // Catch:{ Exception -> 0x00c6, all -> 0x00d9 }
            java.lang.String r5 = "getIntentforControlCenter"
            android.os.Bundle r10 = r10.call(r4, r5, r1, r3)     // Catch:{ Exception -> 0x00c6, all -> 0x00d9 }
            if (r10 == 0) goto L_0x00cf
            java.lang.String r1 = "intentUri"
            java.lang.String r10 = r10.getString(r1)     // Catch:{ Exception -> 0x00c6, all -> 0x00d9 }
            r0.uri = r10     // Catch:{ Exception -> 0x00c6, all -> 0x00d9 }
            goto L_0x00cf
        L_0x00c6:
            r10 = move-exception
            r10.printStackTrace()     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            goto L_0x00cf
        L_0x00cb:
            int r4 = r4 + 1
            goto L_0x0023
        L_0x00cf:
            java.lang.String r10 = "DataUsageProvider"
            java.lang.String r1 = r0.toString()     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            android.util.Log.d(r10, r1)     // Catch:{ Exception -> 0x00dc, all -> 0x00d9 }
            goto L_0x00df
        L_0x00d9:
            r10 = move-exception
            r1 = r2
            goto L_0x00f1
        L_0x00dc:
            r10 = move-exception
            r1 = r2
            goto L_0x00e8
        L_0x00df:
            if (r2 == 0) goto L_0x00f0
            r2.close()
            goto L_0x00f0
        L_0x00e5:
            r10 = move-exception
            goto L_0x00f1
        L_0x00e7:
            r10 = move-exception
        L_0x00e8:
            r10.printStackTrace()     // Catch:{ all -> 0x00e5 }
            if (r1 == 0) goto L_0x00f0
            r1.close()
        L_0x00f0:
            return r0
        L_0x00f1:
            if (r1 == 0) goto L_0x00f6
            r1.close()
        L_0x00f6:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.info.DataUsageInfo.getInfoDetail():com.android.systemui.controlcenter.phone.ExpandInfoController$Info");
    }

    /* access modifiers changed from: protected */
    public Uri getUri() {
        return URI;
    }
}

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
            refresh();
        }
    }

    public void setNoSims(boolean z) {
        boolean z2 = this.mNoSims;
        if (z2 != z) {
            boolean z3 = !z2 && z;
            this.mNoSims = z;
            if (z3) {
                this.mDataSlot = 0;
                refresh();
            }
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00bf, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0103, code lost:
        r11 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0104, code lost:
        r2 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0106, code lost:
        r11 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0107, code lost:
        r2 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x011d, code lost:
        r2.close();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0077 A[Catch:{ Exception -> 0x0106, all -> 0x0103 }] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00c7 A[Catch:{ Exception -> 0x0106, all -> 0x0103 }] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00cc A[Catch:{ Exception -> 0x0106, all -> 0x0103 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ee A[Catch:{ Exception -> 0x00f7, all -> 0x0103 }] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0103 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:4:0x001d] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0117  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x011d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.miui.controlcenter.ExpandInfoController.Info getInfoDetail() {
        /*
            r11 = this;
            java.lang.String r0 = "package_type"
            com.android.systemui.miui.controlcenter.ExpandInfoController$Info r1 = new com.android.systemui.miui.controlcenter.ExpandInfoController$Info
            r1.<init>()
            r2 = 0
            android.content.Context r3 = r11.mContext     // Catch:{ Exception -> 0x0111 }
            android.os.UserHandle r4 = r11.mUserHandle     // Catch:{ Exception -> 0x0111 }
            android.content.ContentResolver r5 = r3.getContentResolverForUser(r4)     // Catch:{ Exception -> 0x0111 }
            android.net.Uri r6 = URI     // Catch:{ Exception -> 0x0111 }
            java.lang.String[] r7 = PROJECT     // Catch:{ Exception -> 0x0111 }
            r8 = 0
            r9 = 0
            r10 = 0
            android.database.Cursor r3 = r5.query(r6, r7, r8, r9, r10)     // Catch:{ Exception -> 0x0111 }
            if (r3 == 0) goto L_0x0109
            boolean r4 = r3.moveToFirst()     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            if (r4 == 0) goto L_0x0109
            int r4 = r3.getColumnCount()     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            java.lang.String r5 = "DataBillProvider"
            if (r4 <= 0) goto L_0x00fb
            r4 = 0
            r3.move(r4)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            java.lang.String r6 = "sim_slot"
            int r6 = r3.getColumnIndex(r6)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            int r6 = r3.getInt(r6)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            int r7 = r11.mDataSlot     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            if (r6 != r7) goto L_0x00fb
            java.lang.String r6 = "bill_name"
            int r6 = r3.getColumnIndex(r6)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            java.lang.String r6 = r3.getString(r6)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            r1.title = r6     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            java.lang.String r6 = "bill_value"
            int r6 = r3.getColumnIndex(r6)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            java.lang.String r6 = r3.getString(r6)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            r1.status = r6     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            java.lang.String r6 = r1.title     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            boolean r6 = android.text.TextUtils.isEmpty(r6)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            r7 = 1
            if (r6 == 0) goto L_0x0069
            java.lang.String r6 = r1.status     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            boolean r6 = android.text.TextUtils.isEmpty(r6)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            if (r6 != 0) goto L_0x0067
            goto L_0x0069
        L_0x0067:
            r6 = r4
            goto L_0x006a
        L_0x0069:
            r6 = r7
        L_0x006a:
            r1.available = r6     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            int r6 = r3.getColumnIndex(r0)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            int r6 = r3.getInt(r6)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            r8 = -1
            if (r6 == r8) goto L_0x0078
            r4 = r7
        L_0x0078:
            r1.initialized = r4     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            r4.<init>()     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            java.lang.String r6 = "packageType:"
            r4.append(r6)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            int r0 = r3.getColumnIndex(r0)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            int r0 = r3.getInt(r0)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            r4.append(r0)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            java.lang.String r0 = r4.toString()     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            android.util.Log.d(r5, r0)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            java.lang.String r0 = "bill_unit"
            int r0 = r3.getColumnIndex(r0)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            java.lang.String r0 = r3.getString(r0)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            r1.unit = r0     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            android.content.Context r0 = r11.mContext     // Catch:{ Exception -> 0x00bf, all -> 0x0103 }
            android.os.UserHandle r4 = r11.mUserHandle     // Catch:{ Exception -> 0x00bf, all -> 0x0103 }
            android.content.ContentResolver r0 = r0.getContentResolverForUser(r4)     // Catch:{ Exception -> 0x00bf, all -> 0x0103 }
            java.lang.String r4 = "bill_icon"
            int r4 = r3.getColumnIndex(r4)     // Catch:{ Exception -> 0x00bf, all -> 0x0103 }
            java.lang.String r4 = r3.getString(r4)     // Catch:{ Exception -> 0x00bf, all -> 0x0103 }
            android.net.Uri r4 = android.net.Uri.parse(r4)     // Catch:{ Exception -> 0x00bf, all -> 0x0103 }
            android.graphics.Bitmap r0 = android.provider.MediaStore.Images.Media.getBitmap(r0, r4)     // Catch:{ Exception -> 0x00bf, all -> 0x0103 }
            r1.icon = r0     // Catch:{ Exception -> 0x00bf, all -> 0x0103 }
            goto L_0x00c3
        L_0x00bf:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
        L_0x00c3:
            android.graphics.Bitmap r0 = r1.icon     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            if (r0 != 0) goto L_0x00cc
            android.graphics.Bitmap r0 = r11.mBpBitmap     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            r1.icon = r0     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            goto L_0x00d0
        L_0x00cc:
            android.graphics.Bitmap r0 = r1.icon     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            r11.mBpBitmap = r0     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
        L_0x00d0:
            android.os.Bundle r0 = new android.os.Bundle     // Catch:{ Exception -> 0x00f7, all -> 0x0103 }
            r0.<init>()     // Catch:{ Exception -> 0x00f7, all -> 0x0103 }
            java.lang.String r4 = "slotId"
            int r6 = r11.mDataSlot     // Catch:{ Exception -> 0x00f7, all -> 0x0103 }
            r0.putInt(r4, r6)     // Catch:{ Exception -> 0x00f7, all -> 0x0103 }
            android.content.Context r4 = r11.mContext     // Catch:{ Exception -> 0x00f7, all -> 0x0103 }
            android.os.UserHandle r11 = r11.mUserHandle     // Catch:{ Exception -> 0x00f7, all -> 0x0103 }
            android.content.ContentResolver r11 = r4.getContentResolverForUser(r11)     // Catch:{ Exception -> 0x00f7, all -> 0x0103 }
            android.net.Uri r4 = URI_ACTION     // Catch:{ Exception -> 0x00f7, all -> 0x0103 }
            java.lang.String r6 = "getIntentforControlCenter"
            android.os.Bundle r11 = r11.call(r4, r6, r2, r0)     // Catch:{ Exception -> 0x00f7, all -> 0x0103 }
            if (r11 == 0) goto L_0x00fb
            java.lang.String r0 = "intentUri"
            java.lang.String r11 = r11.getString(r0)     // Catch:{ Exception -> 0x00f7, all -> 0x0103 }
            r1.uri = r11     // Catch:{ Exception -> 0x00f7, all -> 0x0103 }
            goto L_0x00fb
        L_0x00f7:
            r11 = move-exception
            r11.printStackTrace()     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
        L_0x00fb:
            java.lang.String r11 = r1.toString()     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            android.util.Log.d(r5, r11)     // Catch:{ Exception -> 0x0106, all -> 0x0103 }
            goto L_0x0109
        L_0x0103:
            r11 = move-exception
            r2 = r3
            goto L_0x011b
        L_0x0106:
            r11 = move-exception
            r2 = r3
            goto L_0x0112
        L_0x0109:
            if (r3 == 0) goto L_0x011a
            r3.close()
            goto L_0x011a
        L_0x010f:
            r11 = move-exception
            goto L_0x011b
        L_0x0111:
            r11 = move-exception
        L_0x0112:
            r11.printStackTrace()     // Catch:{ all -> 0x010f }
            if (r2 == 0) goto L_0x011a
            r2.close()
        L_0x011a:
            return r1
        L_0x011b:
            if (r2 == 0) goto L_0x0120
            r2.close()
        L_0x0120:
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.controlcenter.DataBillInfo.getInfoDetail():com.android.systemui.miui.controlcenter.ExpandInfoController$Info");
    }

    /* access modifiers changed from: protected */
    public Uri getUri() {
        return URI;
    }
}

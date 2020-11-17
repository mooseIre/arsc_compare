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
        networkController.addCallback(this);
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
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00c0, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0105, code lost:
        r11 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x0106, code lost:
        r2 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0108, code lost:
        r11 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0109, code lost:
        r2 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0119, code lost:
        r2.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x011f, code lost:
        r2.close();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x0078 A[Catch:{ Exception -> 0x0108, all -> 0x0105 }] */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00c8 A[Catch:{ Exception -> 0x0108, all -> 0x0105 }] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00cd A[Catch:{ Exception -> 0x0108, all -> 0x0105 }] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00f0 A[Catch:{ Exception -> 0x00f9, all -> 0x0105 }] */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0105 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:4:0x001d] */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x0119  */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x011f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.miui.controlcenter.ExpandInfoController.Info getInfoDetail() {
        /*
            r11 = this;
            java.lang.String r0 = "package_type"
            com.android.systemui.miui.controlcenter.ExpandInfoController$Info r1 = new com.android.systemui.miui.controlcenter.ExpandInfoController$Info
            r1.<init>()
            r2 = 0
            android.content.Context r3 = r11.mContext     // Catch:{ Exception -> 0x0113 }
            android.os.UserHandle r4 = r11.mUserHandle     // Catch:{ Exception -> 0x0113 }
            android.content.ContentResolver r5 = r3.getContentResolverForUser(r4)     // Catch:{ Exception -> 0x0113 }
            android.net.Uri r6 = URI     // Catch:{ Exception -> 0x0113 }
            java.lang.String[] r7 = PROJECT     // Catch:{ Exception -> 0x0113 }
            r8 = 0
            r9 = 0
            r10 = 0
            android.database.Cursor r3 = r5.query(r6, r7, r8, r9, r10)     // Catch:{ Exception -> 0x0113 }
            if (r3 == 0) goto L_0x010b
            boolean r4 = r3.moveToFirst()     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            if (r4 == 0) goto L_0x010b
            int r4 = r3.getColumnCount()     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            java.lang.String r5 = "DataBillProvider"
            if (r4 <= 0) goto L_0x00fd
            r4 = 0
            r3.move(r4)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            java.lang.String r6 = "sim_slot"
            int r6 = r3.getColumnIndex(r6)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            int r6 = r3.getInt(r6)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            int r7 = r11.mDataSlot     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            if (r6 != r7) goto L_0x00fd
            java.lang.String r6 = "bill_name"
            int r6 = r3.getColumnIndex(r6)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            java.lang.String r6 = r3.getString(r6)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            r1.title = r6     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            java.lang.String r6 = "bill_value"
            int r6 = r3.getColumnIndex(r6)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            java.lang.String r6 = r3.getString(r6)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            r1.status = r6     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            java.lang.String r6 = r1.title     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            boolean r6 = android.text.TextUtils.isEmpty(r6)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            r7 = 1
            if (r6 == 0) goto L_0x006a
            java.lang.String r6 = r1.status     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            boolean r6 = android.text.TextUtils.isEmpty(r6)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            if (r6 != 0) goto L_0x0068
            goto L_0x006a
        L_0x0068:
            r6 = r4
            goto L_0x006b
        L_0x006a:
            r6 = r7
        L_0x006b:
            r1.available = r6     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            int r6 = r3.getColumnIndex(r0)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            int r6 = r3.getInt(r6)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            r8 = -1
            if (r6 == r8) goto L_0x0079
            r4 = r7
        L_0x0079:
            r1.initialized = r4     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            java.lang.StringBuilder r4 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            r4.<init>()     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            java.lang.String r6 = "packageType:"
            r4.append(r6)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            int r0 = r3.getColumnIndex(r0)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            int r0 = r3.getInt(r0)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            r4.append(r0)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            java.lang.String r0 = r4.toString()     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            android.util.Log.d(r5, r0)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            java.lang.String r0 = "bill_unit"
            int r0 = r3.getColumnIndex(r0)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            java.lang.String r0 = r3.getString(r0)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            r1.unit = r0     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            android.content.Context r0 = r11.mContext     // Catch:{ Exception -> 0x00c0, all -> 0x0105 }
            android.os.UserHandle r4 = r11.mUserHandle     // Catch:{ Exception -> 0x00c0, all -> 0x0105 }
            android.content.ContentResolver r0 = r0.getContentResolverForUser(r4)     // Catch:{ Exception -> 0x00c0, all -> 0x0105 }
            java.lang.String r4 = "bill_icon"
            int r4 = r3.getColumnIndex(r4)     // Catch:{ Exception -> 0x00c0, all -> 0x0105 }
            java.lang.String r4 = r3.getString(r4)     // Catch:{ Exception -> 0x00c0, all -> 0x0105 }
            android.net.Uri r4 = android.net.Uri.parse(r4)     // Catch:{ Exception -> 0x00c0, all -> 0x0105 }
            android.graphics.Bitmap r0 = android.provider.MediaStore.Images.Media.getBitmap(r0, r4)     // Catch:{ Exception -> 0x00c0, all -> 0x0105 }
            r1.icon = r0     // Catch:{ Exception -> 0x00c0, all -> 0x0105 }
            goto L_0x00c4
        L_0x00c0:
            r0 = move-exception
            r0.printStackTrace()     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
        L_0x00c4:
            android.graphics.Bitmap r0 = r1.icon     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            if (r0 != 0) goto L_0x00cd
            android.graphics.Bitmap r0 = r11.mBpBitmap     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            r1.icon = r0     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            goto L_0x00d1
        L_0x00cd:
            android.graphics.Bitmap r0 = r1.icon     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            r11.mBpBitmap = r0     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
        L_0x00d1:
            android.os.Bundle r0 = new android.os.Bundle     // Catch:{ Exception -> 0x00f9, all -> 0x0105 }
            r0.<init>()     // Catch:{ Exception -> 0x00f9, all -> 0x0105 }
            java.lang.String r4 = "slotId"
            int r6 = r11.mDataSlot     // Catch:{ Exception -> 0x00f9, all -> 0x0105 }
            r0.putInt(r4, r6)     // Catch:{ Exception -> 0x00f9, all -> 0x0105 }
            android.content.Context r4 = r11.mContext     // Catch:{ Exception -> 0x00f9, all -> 0x0105 }
            android.os.UserHandle r11 = r11.mUserHandle     // Catch:{ Exception -> 0x00f9, all -> 0x0105 }
            android.content.ContentResolver r11 = r4.getContentResolverForUser(r11)     // Catch:{ Exception -> 0x00f9, all -> 0x0105 }
            android.net.Uri r4 = URI_ACTION     // Catch:{ Exception -> 0x00f9, all -> 0x0105 }
            java.lang.String r6 = "getIntentforControlCenter"
            android.os.Bundle r11 = r11.call(r4, r6, r2, r0)     // Catch:{ Exception -> 0x00f9, all -> 0x0105 }
            if (r11 == 0) goto L_0x00fd
            java.lang.String r0 = "intentUri"
            java.lang.String r11 = r11.getString(r0)     // Catch:{ Exception -> 0x00f9, all -> 0x0105 }
            r1.uri = r11     // Catch:{ Exception -> 0x00f9, all -> 0x0105 }
            goto L_0x00fd
        L_0x00f9:
            r11 = move-exception
            r11.printStackTrace()     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
        L_0x00fd:
            java.lang.String r11 = r1.toString()     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            android.util.Log.d(r5, r11)     // Catch:{ Exception -> 0x0108, all -> 0x0105 }
            goto L_0x010b
        L_0x0105:
            r11 = move-exception
            r2 = r3
            goto L_0x011d
        L_0x0108:
            r11 = move-exception
            r2 = r3
            goto L_0x0114
        L_0x010b:
            if (r3 == 0) goto L_0x011c
            r3.close()
            goto L_0x011c
        L_0x0111:
            r11 = move-exception
            goto L_0x011d
        L_0x0113:
            r11 = move-exception
        L_0x0114:
            r11.printStackTrace()     // Catch:{ all -> 0x0111 }
            if (r2 == 0) goto L_0x011c
            r2.close()
        L_0x011c:
            return r1
        L_0x011d:
            if (r2 == 0) goto L_0x0122
            r2.close()
        L_0x0122:
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.miui.controlcenter.DataBillInfo.getInfoDetail():com.android.systemui.miui.controlcenter.ExpandInfoController$Info");
    }

    /* access modifiers changed from: protected */
    public Uri getUri() {
        return URI;
    }
}

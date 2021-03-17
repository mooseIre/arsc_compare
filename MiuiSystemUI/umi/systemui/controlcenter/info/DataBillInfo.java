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
        networkController.addCallback((NetworkController.SignalCallback) this);
        requestData(this.mUserHandle);
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setIsDefaultDataSim(int i, boolean z) {
        if (z && this.mDataSlot != i) {
            this.mDataSlot = i;
            refresh(2500);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
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
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x003c, code lost:
        r4 = r2.getInt(r2.getColumnIndex("package_type"));
        r0.title = r2.getString(r2.getColumnIndex("bill_name"));
        r0.status = r2.getString(r2.getColumnIndex("bill_value"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0065, code lost:
        if (android.text.TextUtils.isEmpty(r0.title) == false) goto L_0x0072;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x006d, code lost:
        if (android.text.TextUtils.isEmpty(r0.status) != false) goto L_0x0070;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0070, code lost:
        r5 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0072, code lost:
        r5 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0073, code lost:
        r0.available = r5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0076, code lost:
        if (r4 == -1) goto L_0x0079;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0078, code lost:
        r3 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0079, code lost:
        r0.initialized = r3;
        android.util.Log.d("DataBillProvider", "packageType:" + r4);
        r0.unit = r2.getString(r2.getColumnIndex("bill_unit"));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        r0.icon = android.provider.MediaStore.Images.Media.getBitmap(r10.mContext.getContentResolverForUser(r10.mUserHandle), android.net.Uri.parse(r2.getString(r2.getColumnIndex("bill_icon"))));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x00b8, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00b9, code lost:
        r3.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0101, code lost:
        r10 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0102, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0104, code lost:
        r10 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x0105, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x011b, code lost:
        r1.close();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0101 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:4:0x001b] */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0109  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0115  */
    /* JADX WARNING: Removed duplicated region for block: B:53:0x011b  */
    @Override // com.android.systemui.controlcenter.info.BaseInfo
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.controlcenter.phone.ExpandInfoController.Info getInfoDetail() {
        /*
        // Method dump skipped, instructions count: 287
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.info.DataBillInfo.getInfoDetail():com.android.systemui.controlcenter.phone.ExpandInfoController$Info");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.controlcenter.info.BaseInfo
    public Uri getUri() {
        return URI;
    }
}

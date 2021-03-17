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
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x008f, code lost:
        r3.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x00d9, code lost:
        r10 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x00da, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00dc, code lost:
        r10 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x00dd, code lost:
        r1 = r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00ed, code lost:
        r1.close();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x00d9 A[ExcHandler: all (th java.lang.Throwable), Splitter:B:4:0x001b] */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00e1  */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00ed  */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00f3  */
    @Override // com.android.systemui.controlcenter.info.BaseInfo
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.android.systemui.controlcenter.phone.ExpandInfoController.Info getInfoDetail() {
        /*
        // Method dump skipped, instructions count: 247
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.info.DataUsageInfo.getInfoDetail():com.android.systemui.controlcenter.phone.ExpandInfoController$Info");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.controlcenter.info.BaseInfo
    public Uri getUri() {
        return URI;
    }
}

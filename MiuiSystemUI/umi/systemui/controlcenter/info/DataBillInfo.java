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
    public void setDefaultSim(int i) {
        if (this.mDataSlot != i) {
            this.mDataSlot = i;
            refresh(2500);
        }
    }

    @Override // com.android.systemui.statusbar.policy.NetworkController.SignalCallback
    public void setNoSims(boolean z, boolean z2) {
        if (this.mNoSims != z) {
            this.mNoSims = z;
            refresh(2500);
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x00bd, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00be, code lost:
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

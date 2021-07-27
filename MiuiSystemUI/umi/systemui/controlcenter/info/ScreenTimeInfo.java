package com.android.systemui.controlcenter.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.controlcenter.phone.ExpandInfoController;

public class ScreenTimeInfo extends BaseInfo {
    private Uri mUri = new Uri.Builder().authority("com.xiaomi.misettings.usagestats.screentimecontentprovider").scheme("content").build();
    private Runnable refreshRunnable = new Runnable() {
        /* class com.android.systemui.controlcenter.info.$$Lambda$ScreenTimeInfo$Du4Kp5Cz_JhnuJRP3moLt6aEV4 */

        public final void run() {
            ScreenTimeInfo.this.lambda$new$1$ScreenTimeInfo();
        }
    };

    public ScreenTimeInfo(Context context, int i, ExpandInfoController expandInfoController) {
        super(context, i, expandInfoController);
        requestData(this.mUserHandle);
    }

    @Override // com.android.systemui.controlcenter.info.BaseInfo
    public void requestData(UserHandle userHandle) {
        super.registerObserver();
        this.mExpandInfoController.getBgExecutor().execute(this.refreshRunnable);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$1 */
    public /* synthetic */ void lambda$new$1$ScreenTimeInfo() {
        ExpandInfoController.Info infoDetail = getInfoDetail();
        sendUpdate();
        if (!this.mInfo.equal(infoDetail)) {
            this.mInfo = infoDetail;
            this.mHandler.post(new Runnable() {
                /* class com.android.systemui.controlcenter.info.$$Lambda$ScreenTimeInfo$TKO0d_bNGc88JvCsJGxZQNrALE */

                public final void run() {
                    ScreenTimeInfo.this.lambda$new$0$ScreenTimeInfo();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$ScreenTimeInfo() {
        this.mExpandInfoController.updateInfo(this.mType, this.mInfo);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.controlcenter.info.BaseInfo
    public ExpandInfoController.Info getInfoDetail() {
        Bundle bundle;
        ExpandInfoController.Info info = new ExpandInfoController.Info();
        try {
            Bundle bundle2 = new Bundle();
            bundle2.putInt("UID", KeyguardUpdateMonitor.getCurrentUser());
            bundle = this.mContext.getContentResolver().call(this.mUri, "GET_DATA", "only_get_data", bundle2);
        } catch (Exception e) {
            e.printStackTrace();
            bundle = null;
        }
        if (bundle != null) {
            info.title = bundle.getString("name");
            info.initialized = bundle.getBoolean("state");
            info.available = bundle.getBoolean("is_support");
            info.status = bundle.getString(info.initialized ? "time" : "summary");
            info.unit = bundle.getString("unit");
            info.action = bundle.getString("action");
            Bitmap bitmap = (Bitmap) bundle.getParcelable("bitmap");
            info.icon = bitmap;
            if (bitmap == null) {
                info.icon = this.mBpBitmap;
            } else {
                this.mBpBitmap = bitmap;
            }
        }
        return info;
    }

    private void sendUpdate() {
        if (this.mInfo.available) {
            try {
                Bundle bundle = new Bundle();
                bundle.putInt("UID", KeyguardUpdateMonitor.getCurrentUser());
                this.mContext.getContentResolver().call(this.mUri, "GET_DATA", "send_update_signal", bundle);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.controlcenter.info.BaseInfo
    public Uri getUri() {
        return this.mUri;
    }
}

package com.android.systemui.controlcenter.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.controlcenter.phone.ExpandInfoController;

public class ScreenTimeInfo extends BaseInfo {
    private Uri mUri = new Uri.Builder().authority("com.xiaomi.misettings.usagestats.screentimecontentprovider").scheme("content").build();

    public ScreenTimeInfo(Context context, int i, ExpandInfoController expandInfoController) {
        super(context, i, expandInfoController);
        requestData(this.mUserHandle);
    }

    @Override // com.android.systemui.controlcenter.info.BaseInfo
    public void requestData(UserHandle userHandle) {
        super.registerObserver();
        new UpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    public class UpdateTask extends AsyncTask<Void, Void, ExpandInfoController.Info> {
        private UpdateTask() {
        }

        /* access modifiers changed from: protected */
        public ExpandInfoController.Info doInBackground(Void... voidArr) {
            ExpandInfoController.Info infoDetail = ScreenTimeInfo.this.getInfoDetail();
            ScreenTimeInfo.this.sendUpdate();
            return infoDetail;
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(ExpandInfoController.Info info) {
            super.onPostExecute((Object) info);
            if (!ScreenTimeInfo.this.mInfo.equal(info)) {
                ScreenTimeInfo screenTimeInfo = ScreenTimeInfo.this;
                screenTimeInfo.mInfo = info;
                screenTimeInfo.mExpandInfoController.updateInfo(screenTimeInfo.mType, info);
            }
        }
    }
}

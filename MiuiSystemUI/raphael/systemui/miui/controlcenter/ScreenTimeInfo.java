package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.miui.controlcenter.ExpandInfoController;
import com.xiaomi.stat.MiStat;
import com.xiaomi.stat.b;

public class ScreenTimeInfo extends BaseInfo {
    private Uri mUri = new Uri.Builder().authority("com.xiaomi.misettings.usagestats.screentimecontentprovider").scheme(MiStat.Param.CONTENT).build();

    public ScreenTimeInfo(Context context, int i, ExpandInfoController expandInfoController) {
        super(context, i, expandInfoController);
        requestData(this.mUserHandle);
    }

    public void requestData(UserHandle userHandle) {
        super.registerObserver();
        new UpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    /* access modifiers changed from: protected */
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
            info.status = bundle.getString(info.initialized ? b.j : "summary");
            info.unit = bundle.getString("unit");
            info.action = bundle.getString("action");
            info.icon = (Bitmap) bundle.getParcelable("bitmap");
            Bitmap bitmap = info.icon;
            if (bitmap == null) {
                info.icon = this.mBpBitmap;
            } else {
                this.mBpBitmap = bitmap;
            }
        }
        return info;
    }

    /* access modifiers changed from: private */
    public void sendUpdate() {
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
    public Uri getUri() {
        return this.mUri;
    }

    private class UpdateTask extends AsyncTask<Void, Void, ExpandInfoController.Info> {
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
            super.onPostExecute(info);
            if (!ScreenTimeInfo.this.mInfo.equal(info)) {
                ScreenTimeInfo screenTimeInfo = ScreenTimeInfo.this;
                screenTimeInfo.mInfo = info;
                screenTimeInfo.mExpandInfoController.updateInfo(screenTimeInfo.mType, screenTimeInfo.mInfo);
            }
        }
    }
}

package com.android.systemui.screenshot;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

class DeleteImageInBackgroundTask extends AsyncTask<Uri, Void, Void> {
    private Context mContext;

    DeleteImageInBackgroundTask(Context context) {
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public Void doInBackground(Uri... uriArr) {
        if (uriArr.length != 1) {
            return null;
        }
        this.mContext.getContentResolver().delete(uriArr[0], null, null);
        return null;
    }
}

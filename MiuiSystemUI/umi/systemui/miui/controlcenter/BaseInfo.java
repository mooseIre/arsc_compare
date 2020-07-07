package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.UserHandle;
import com.android.systemui.miui.controlcenter.ExpandInfoController;

public abstract class BaseInfo {
    protected Bitmap mBpBitmap;
    private ContentObserver mContentObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            BaseInfo.this.refresh();
        }
    };
    protected Context mContext;
    protected ExpandInfoController mExpandInfoController;
    private Handler mHandler;
    protected ExpandInfoController.Info mInfo = new ExpandInfoController.Info();
    private boolean mObserverRigstered;
    protected int mType;
    protected UserHandle mUserHandle;

    /* access modifiers changed from: protected */
    public abstract ExpandInfoController.Info getInfoDetail();

    /* access modifiers changed from: protected */
    public abstract Uri getUri();

    public BaseInfo(Context context, int i, ExpandInfoController expandInfoController) {
        this.mContext = context;
        this.mType = i;
        this.mExpandInfoController = expandInfoController;
        this.mHandler = new Handler(context.getMainLooper());
    }

    public ExpandInfoController.Info getInfo() {
        return this.mInfo;
    }

    public void requestData(UserHandle userHandle) {
        UserHandle userHandle2 = this.mUserHandle;
        if (userHandle2 != userHandle) {
            if (this.mObserverRigstered) {
                this.mContext.getContentResolverForUser(userHandle2).unregisterContentObserver(this.mContentObserver);
                this.mObserverRigstered = false;
            }
            this.mUserHandle = userHandle;
        }
        registerObserver();
        refresh();
    }

    /* access modifiers changed from: protected */
    public void registerObserver() {
        if (!this.mObserverRigstered) {
            try {
                this.mContext.getContentResolverForUser(this.mUserHandle).registerContentObserver(getUri(), false, this.mContentObserver);
                this.mObserverRigstered = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void refresh() {
        new UpdateInfoDetailTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    private class UpdateInfoDetailTask extends AsyncTask<Void, Void, ExpandInfoController.Info> {
        private UpdateInfoDetailTask() {
        }

        /* access modifiers changed from: protected */
        public ExpandInfoController.Info doInBackground(Void... voidArr) {
            return BaseInfo.this.getInfoDetail();
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(ExpandInfoController.Info info) {
            super.onPostExecute(info);
            if (!BaseInfo.this.mInfo.equal(info)) {
                BaseInfo baseInfo = BaseInfo.this;
                baseInfo.mInfo = info;
                baseInfo.mExpandInfoController.updateInfo(baseInfo.mType, info);
            }
        }
    }
}

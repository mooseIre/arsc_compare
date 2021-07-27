package com.android.systemui.controlcenter.info;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import com.android.systemui.controlcenter.info.BaseInfo;
import com.android.systemui.controlcenter.phone.ExpandInfoController;

public abstract class BaseInfo {
    protected Bitmap mBpBitmap;
    private ContentObserver mContentObserver = new ContentObserver(this.mHandler) {
        /* class com.android.systemui.controlcenter.info.BaseInfo.AnonymousClass4 */

        public void onChange(boolean z) {
            BaseInfo.this.refresh();
        }
    };
    protected Context mContext;
    protected ExpandInfoController mExpandInfoController;
    protected Handler mHandler;
    protected ExpandInfoController.Info mInfo = new ExpandInfoController.Info();
    private boolean mObserverRigstered;
    private BroadcastReceiver mSIMDataReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.controlcenter.info.BaseInfo.AnonymousClass3 */

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                    BaseInfo.this.refresh(2500);
                } else if ("com.miui.networkassistant.CORRECTION_SUCCEED".equals(intent.getAction())) {
                    BaseInfo.this.refresh();
                } else if ("com.miui.networkassistant.CORRECTION_FAILED".equals(intent.getAction())) {
                    BaseInfo.this.refresh();
                }
            }
        }
    };
    protected int mType;
    protected UserHandle mUserHandle;
    private Runnable refreshRunnable = new Runnable() {
        /* class com.android.systemui.controlcenter.info.BaseInfo.AnonymousClass1 */

        public void run() {
            ExpandInfoController.Info infoDetail = BaseInfo.this.getInfoDetail();
            if (!BaseInfo.this.mInfo.equal(infoDetail)) {
                BaseInfo baseInfo = BaseInfo.this;
                baseInfo.mInfo = infoDetail;
                baseInfo.mHandler.post(new Runnable() {
                    /* class com.android.systemui.controlcenter.info.$$Lambda$BaseInfo$1$aJhL7m2c0kTU_DR4vJXNDCcNLg */

                    public final void run() {
                        BaseInfo.AnonymousClass1.this.lambda$run$0$BaseInfo$1();
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$run$0 */
        public /* synthetic */ void lambda$run$0$BaseInfo$1() {
            BaseInfo baseInfo = BaseInfo.this;
            baseInfo.mExpandInfoController.updateInfo(baseInfo.mType, baseInfo.mInfo);
        }
    };

    /* access modifiers changed from: protected */
    public abstract ExpandInfoController.Info getInfoDetail();

    /* access modifiers changed from: protected */
    public abstract Uri getUri();

    public BaseInfo(Context context, int i, ExpandInfoController expandInfoController) {
        this.mContext = context;
        this.mType = i;
        this.mUserHandle = expandInfoController.getUserHandle();
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

    public void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.SIM_STATE_CHANGED");
        intentFilter.addAction("com.miui.networkassistant.CORRECTION_SUCCEED");
        intentFilter.addAction("com.miui.networkassistant.CORRECTION_FAILED");
        this.mContext.registerReceiver(this.mSIMDataReceiver, intentFilter);
    }

    public void unregister() {
        this.mContext.unregisterReceiver(this.mSIMDataReceiver);
    }

    /* access modifiers changed from: protected */
    public void refresh() {
        this.mExpandInfoController.getBgExecutor().execute(this.refreshRunnable);
    }

    /* access modifiers changed from: protected */
    public void refresh(long j) {
        this.mHandler.postDelayed(new Runnable() {
            /* class com.android.systemui.controlcenter.info.BaseInfo.AnonymousClass2 */

            public void run() {
                BaseInfo.this.refresh();
            }
        }, j);
    }
}

package com.android.systemui.statusbar.notification.unimportant;

import android.content.Context;
import com.miui.systemui.CloudDataListener;

/* compiled from: FoldTool.kt */
public final class FoldTool$registerWhiteListObserver$1 implements CloudDataListener {
    FoldTool$registerWhiteListObserver$1() {
    }

    @Override // com.miui.systemui.CloudDataListener
    public void onCloudDataUpdate(boolean z) {
        FoldTool foldTool = FoldTool.INSTANCE;
        Context context = FoldTool.mContext;
        if (context != null) {
            FoldCloudDataHelper.INSTANCE.updateAll(context);
        }
    }
}

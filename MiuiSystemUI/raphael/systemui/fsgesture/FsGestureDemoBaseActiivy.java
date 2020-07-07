package com.android.systemui.fsgesture;

import android.app.Activity;
import android.app.StatusBarManager;
import android.os.Bundle;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.FsGestureShowStateEvent;
import com.android.systemui.statusbar.phone.NavigationHandle;

public class FsGestureDemoBaseActiivy extends Activity {
    public NavigationHandle mNavigationHandle;
    private StatusBarManager mSBM;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mSBM = (StatusBarManager) getSystemService(StatusBarManager.class);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        RecentsEventBus.getDefault().send(new FsGestureShowStateEvent(true));
        GestureDemoBroadcastUtils.sendBroadcast(this, true);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        GestureLineUtils.updateNavigationHandleVisibility(this, this.mNavigationHandle);
        this.mSBM.disable(23068672);
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        this.mSBM.disable(0);
    }
}

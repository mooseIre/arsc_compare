package com.android.systemui.fsgesture;

import android.app.Activity;
import com.android.systemui.statusbar.phone.NavigationHandle;

public class FsGestureDemoBaseActiivy extends Activity {
    public NavigationHandle mNavigationHandle;

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        GestureDemoBroadcastUtils.sendBroadcast(this, true);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        GestureLineUtils.updateNavigationHandleVisibility(this, this.mNavigationHandle);
    }
}

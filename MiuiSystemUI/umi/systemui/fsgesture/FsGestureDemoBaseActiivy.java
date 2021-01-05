package com.android.systemui.fsgesture;

import android.app.Activity;
import android.os.Bundle;
import com.android.systemui.statusbar.phone.NavigationHandle;

public class FsGestureDemoBaseActiivy extends Activity {
    public NavigationHandle mNavigationHandle;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(1024);
        getWindow().getAttributes().layoutInDisplayCutoutMode = 3;
        FsgestureUtil.INSTANCE.hideSystemBars(getWindow().getDecorView());
    }

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

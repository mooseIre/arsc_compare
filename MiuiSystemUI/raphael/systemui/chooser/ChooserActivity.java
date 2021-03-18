package com.android.systemui.chooser;

import android.app.Activity;
import android.os.Bundle;

public final class ChooserActivity extends Activity {
    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ChooserHelper.onChoose(this);
        finish();
    }
}

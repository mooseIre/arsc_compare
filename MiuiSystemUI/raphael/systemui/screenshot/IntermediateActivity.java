package com.android.systemui.screenshot;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class IntermediateActivity extends Activity {
    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        Intent intent;
        super.onCreate(bundle);
        if (bundle == null && (intent = (Intent) getIntent().getParcelableExtra("Intent")) != null) {
            startActivityForResult(intent, 1);
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        Uri data = ((Intent) getIntent().getParcelableExtra("Intent")).getData();
        if (!(i2 != -1 || intent == null || intent.getData() == null)) {
            data = intent.getData();
        }
        Intent intent2 = new Intent("android.intent.action.VIEW");
        intent2.setPackage("com.miui.gallery");
        intent2.setDataAndType(data, "image/*");
        intent2.putExtra("com.miui.gallery.extra.show_bars_when_enter", true);
        startActivity(intent2);
        finish();
    }
}

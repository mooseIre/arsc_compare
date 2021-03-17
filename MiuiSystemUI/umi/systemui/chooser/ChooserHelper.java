package com.android.systemui.chooser;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;

public class ChooserHelper {
    static void onChoose(Activity activity) {
        Intent intent = activity.getIntent();
        Bundle extras = intent.getExtras();
        Intent intent2 = (Intent) intent.getParcelableExtra("android.intent.extra.INTENT");
        Bundle bundle = (Bundle) intent.getParcelableExtra("android.app.extra.OPTIONS");
        IBinder binder = extras.getBinder("android.app.extra.PERMISSION_TOKEN");
        boolean booleanExtra = intent.getBooleanExtra("android.app.extra.EXTRA_IGNORE_TARGET_SECURITY", false);
        int intExtra = intent.getIntExtra("android.intent.extra.USER_ID", -1);
        StrictMode.disableDeathOnFileUriExposure();
        try {
            activity.startActivityAsCaller(intent2, bundle, binder, booleanExtra, intExtra);
        } finally {
            StrictMode.enableDeathOnFileUriExposure();
        }
    }
}

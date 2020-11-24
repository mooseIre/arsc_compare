package com.android.systemui;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SystemUISecondaryUserService extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        ((SystemUIApplication) getApplication()).startSecondaryUserServicesIfNeeded();
    }
}

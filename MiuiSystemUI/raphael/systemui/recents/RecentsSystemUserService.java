package com.android.systemui.recents;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import com.android.systemui.Application;

public class RecentsSystemUserService extends Service {
    public void onCreate() {
        super.onCreate();
    }

    public IBinder onBind(Intent intent) {
        Recents recents = (Recents) ((Application) getApplication()).getSystemUIApplication().getComponent(Recents.class);
        if (recents != null) {
            return recents.getSystemUserCallbacks();
        }
        return null;
    }
}

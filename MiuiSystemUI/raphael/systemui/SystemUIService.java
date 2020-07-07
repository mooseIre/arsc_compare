package com.android.systemui;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemProperties;
import com.android.systemui.statusbar.policy.EncryptionHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SystemUIService extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        ((Application) getApplication()).getSystemUIApplication().startServicesIfNeeded();
        if (Build.IS_DEBUGGABLE && SystemProperties.getBoolean("debug.crash_sysui", false)) {
            throw new RuntimeException();
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        if (EncryptionHelper.systemNotReady()) {
            printWriter.println("system not ready");
            return;
        }
        SystemUI[] services = ((Application) getApplication()).getSystemUIApplication().getServices();
        int i = 0;
        if (strArr == null || strArr.length == 0) {
            int length = services.length;
            while (i < length) {
                SystemUI systemUI = services[i];
                if (systemUI != null) {
                    printWriter.println("dumping service: " + systemUI.getClass().getName());
                    systemUI.dump(fileDescriptor, printWriter, strArr);
                    i++;
                } else {
                    return;
                }
            }
            return;
        }
        String str = strArr[0];
        int length2 = services.length;
        while (i < length2) {
            SystemUI systemUI2 = services[i];
            if (systemUI2 != null) {
                if (systemUI2.getClass().getName().endsWith(str)) {
                    systemUI2.dump(fileDescriptor, printWriter, strArr);
                }
                i++;
            } else {
                return;
            }
        }
    }
}

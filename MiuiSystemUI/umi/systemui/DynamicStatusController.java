package com.android.systemui;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import com.miui.systemui.annotation.Inject;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class DynamicStatusController implements Dumpable {
    /* access modifiers changed from: private */
    public Context mContext;
    public boolean mDebug;
    private ContentObserver mMiuiDebugObserver = new ContentObserver((Handler) null) {
        public void onChange(boolean z) {
            try {
                DynamicStatusController dynamicStatusController = DynamicStatusController.this;
                boolean z2 = false;
                if (Settings.Global.getInt(DynamicStatusController.this.mContext.getContentResolver(), "enable_miui_systemui_debug", 0) == 1) {
                    z2 = true;
                }
                dynamicStatusController.mDebug = z2;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
    }

    public DynamicStatusController(@Inject Context context) {
        this.mContext = context;
        context.getContentResolver().registerContentObserver(Settings.Global.getUriFor("enable_miui_systemui_debug"), false, this.mMiuiDebugObserver, -1);
        this.mMiuiDebugObserver.onChange(true);
    }

    public boolean isDebug() {
        return this.mDebug;
    }
}

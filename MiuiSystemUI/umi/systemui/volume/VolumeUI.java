package com.android.systemui.volume;

import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.R;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class VolumeUI extends SystemUI {
    private static boolean LOGD = Log.isLoggable("VolumeUI", 3);
    private boolean mEnabled;
    private final Handler mHandler = new Handler();
    private VolumeDialogComponent mVolumeComponent;

    public void start() {
        this.mEnabled = this.mContext.getResources().getBoolean(R.bool.enable_volume_ui);
        if (this.mEnabled) {
            this.mVolumeComponent = new VolumeDialogComponent(this, this.mContext, (Handler) null);
            putComponent(VolumeComponent.class, getVolumeComponent());
            setDefaultVolumeController();
        }
    }

    private VolumeComponent getVolumeComponent() {
        return this.mVolumeComponent;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mEnabled) {
            getVolumeComponent().onConfigurationChanged(configuration);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("mEnabled=");
        printWriter.println(this.mEnabled);
        if (this.mEnabled) {
            getVolumeComponent().dump(fileDescriptor, printWriter, strArr);
        }
    }

    private void setDefaultVolumeController() {
        if (LOGD) {
            Log.d("VolumeUI", "Registering default volume controller");
        }
        getVolumeComponent().register();
    }
}

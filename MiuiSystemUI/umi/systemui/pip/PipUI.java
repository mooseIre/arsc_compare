package com.android.systemui.pip;

import android.content.res.Configuration;
import android.os.UserManager;
import com.android.systemui.SystemUI;
import com.android.systemui.pip.phone.PipManager;
import com.android.systemui.statusbar.CommandQueue;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class PipUI extends SystemUI implements CommandQueue.Callbacks {
    private BasePipManager mPipManager;

    public void start() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.picture_in_picture")) {
            if (UserManager.get(this.mContext).getUserHandle() == 0) {
                this.mPipManager = PipManager.getInstance(this.mContext);
                ((CommandQueue) getComponent(CommandQueue.class)).addCallbacks(this);
                return;
            }
            throw new IllegalStateException("Non-primary Pip component not currently supported.");
        }
    }

    public void showPictureInPictureMenu() {
        this.mPipManager.showPictureInPictureMenu();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        BasePipManager basePipManager = this.mPipManager;
        if (basePipManager != null) {
            basePipManager.onConfigurationChanged(configuration);
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        BasePipManager basePipManager = this.mPipManager;
        if (basePipManager != null) {
            basePipManager.dump(printWriter);
        }
    }
}

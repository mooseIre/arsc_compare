package com.android.systemui.pip;

import android.content.Context;
import android.content.res.Configuration;
import android.os.UserManager;
import com.android.systemui.SystemUI;
import com.android.systemui.shared.recents.IPinnedStackAnimationListener;
import com.android.systemui.statusbar.CommandQueue;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class PipUI extends SystemUI implements CommandQueue.Callbacks {
    private final CommandQueue mCommandQueue;
    private BasePipManager mPipManager;

    public PipUI(Context context, CommandQueue commandQueue, BasePipManager basePipManager) {
        super(context);
        this.mCommandQueue = commandQueue;
        this.mPipManager = basePipManager;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        if (this.mContext.getPackageManager().hasSystemFeature("android.software.picture_in_picture")) {
            if (UserManager.get(this.mContext).getUserHandle() == 0) {
                this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
                return;
            }
            throw new IllegalStateException("Non-primary Pip component not currently supported.");
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showPictureInPictureMenu() {
        this.mPipManager.showPictureInPictureMenu();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        BasePipManager basePipManager = this.mPipManager;
        if (basePipManager != null) {
            basePipManager.onConfigurationChanged(configuration);
        }
    }

    public void setShelfHeight(boolean z, int i) {
        BasePipManager basePipManager = this.mPipManager;
        if (basePipManager != null) {
            basePipManager.setShelfHeight(z, i);
        }
    }

    public void setPinnedStackAnimationType(int i) {
        BasePipManager basePipManager = this.mPipManager;
        if (basePipManager != null) {
            basePipManager.setPinnedStackAnimationType(i);
        }
    }

    public void setPinnedStackAnimationListener(IPinnedStackAnimationListener iPinnedStackAnimationListener) {
        BasePipManager basePipManager = this.mPipManager;
        if (basePipManager != null) {
            basePipManager.setPinnedStackAnimationListener(iPinnedStackAnimationListener);
        }
    }

    @Override // com.android.systemui.SystemUI, com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        BasePipManager basePipManager = this.mPipManager;
        if (basePipManager != null) {
            basePipManager.dump(printWriter);
        }
    }
}

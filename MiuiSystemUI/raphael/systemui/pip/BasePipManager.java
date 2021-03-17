package com.android.systemui.pip;

import android.content.res.Configuration;
import com.android.systemui.shared.recents.IPinnedStackAnimationListener;
import java.io.PrintWriter;

public interface BasePipManager {
    void dump(PrintWriter printWriter) {
    }

    void onConfigurationChanged(Configuration configuration);

    void setPinnedStackAnimationListener(IPinnedStackAnimationListener iPinnedStackAnimationListener) {
    }

    void setPinnedStackAnimationType(int i) {
    }

    void setShelfHeight(boolean z, int i) {
    }

    void showPictureInPictureMenu();
}

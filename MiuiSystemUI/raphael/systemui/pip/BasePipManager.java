package com.android.systemui.pip;

import android.content.res.Configuration;
import com.android.systemui.shared.recents.IPinnedStackAnimationListener;
import java.io.PrintWriter;

public interface BasePipManager {
    default void dump(PrintWriter printWriter) {
    }

    void onConfigurationChanged(Configuration configuration);

    default void setPinnedStackAnimationListener(IPinnedStackAnimationListener iPinnedStackAnimationListener) {
    }

    default void setPinnedStackAnimationType(int i) {
    }

    default void setShelfHeight(boolean z, int i) {
    }

    void showPictureInPictureMenu();
}

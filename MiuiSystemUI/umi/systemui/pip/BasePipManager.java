package com.android.systemui.pip;

import android.content.res.Configuration;
import java.io.PrintWriter;

public interface BasePipManager {
    void dump(PrintWriter printWriter) {
    }

    void onConfigurationChanged(Configuration configuration);

    void showPictureInPictureMenu();
}

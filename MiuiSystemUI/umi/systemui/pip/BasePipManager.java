package com.android.systemui.pip;

import android.content.Context;
import android.content.res.Configuration;
import java.io.PrintWriter;

public interface BasePipManager {
    void dump(PrintWriter printWriter);

    void initialize(Context context);

    void onConfigurationChanged(Configuration configuration);

    void showPictureInPictureMenu();
}

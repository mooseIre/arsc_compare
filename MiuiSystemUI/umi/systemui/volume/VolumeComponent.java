package com.android.systemui.volume;

import android.content.res.Configuration;
import com.android.systemui.DemoMode;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface VolumeComponent extends DemoMode {
    void dismissNow();

    void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    void onConfigurationChanged(Configuration configuration);

    void register();
}

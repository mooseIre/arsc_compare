package com.android.systemui.glwallpaper;

import android.util.Size;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface GLWallpaperRenderer {
    void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    void finish();

    boolean isWcgContent();

    void onDrawFrame();

    void onSurfaceChanged(int i, int i2);

    void onSurfaceCreated();

    Size reportSurfaceSize();
}

package com.android.systemui.glwallpaper;

import android.util.Size;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public interface GLWallpaperRenderer {

    public interface SurfaceProxy {
        void postRender();

        void preRender();

        void requestRender();
    }

    void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    void finish();

    void onDrawFrame();

    void onSurfaceChanged(int i, int i2);

    void onSurfaceCreated();

    Size reportSurfaceSize();

    void startUnlockAnim(boolean z, long j);

    void updateAmbientMode(boolean z, long j);

    void updateDarkWallpaperMode(boolean z);

    void updateDarken(boolean z);

    void updateOffsets(float f, float f2);

    void updateWallpaperAlpha(float f);
}

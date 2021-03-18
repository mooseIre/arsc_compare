package com.android.systemui.glwallpaper;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EglHelper {
    private static final String TAG = "EglHelper";
    private EGLConfig mEglConfig;
    private EGLContext mEglContext;
    private EGLDisplay mEglDisplay;
    private boolean mEglReady;
    private EGLSurface mEglSurface;
    private final int[] mEglVersion = new int[2];
    private final Set<String> mExts = new HashSet();

    public EglHelper() {
        connectDisplay();
    }

    public boolean init(SurfaceHolder surfaceHolder, boolean z) {
        if (hasEglDisplay() || connectDisplay()) {
            EGLDisplay eGLDisplay = this.mEglDisplay;
            int[] iArr = this.mEglVersion;
            if (!EGL14.eglInitialize(eGLDisplay, iArr, 0, iArr, 1)) {
                String str = TAG;
                Log.w(str, "eglInitialize failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
                return false;
            }
            EGLConfig chooseEglConfig = chooseEglConfig();
            this.mEglConfig = chooseEglConfig;
            if (chooseEglConfig == null) {
                Log.w(TAG, "eglConfig not initialized!");
                return false;
            } else if (!createEglContext()) {
                Log.w(TAG, "Can't create EGLContext!");
                return false;
            } else if (!createEglSurface(surfaceHolder, z)) {
                Log.w(TAG, "Can't create EGLSurface!");
                return false;
            } else {
                this.mEglReady = true;
                return true;
            }
        } else {
            Log.w(TAG, "Can not connect display, abort!");
            return false;
        }
    }

    private boolean connectDisplay() {
        this.mExts.clear();
        this.mEglDisplay = EGL14.eglGetDisplay(0);
        if (!hasEglDisplay()) {
            String str = TAG;
            Log.w(str, "eglGetDisplay failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            return false;
        }
        String eglQueryString = EGL14.eglQueryString(this.mEglDisplay, 12373);
        if (TextUtils.isEmpty(eglQueryString)) {
            return true;
        }
        Collections.addAll(this.mExts, eglQueryString.split(" "));
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean checkExtensionCapability(String str) {
        return this.mExts.contains(str);
    }

    /* access modifiers changed from: package-private */
    public int getWcgCapability() {
        return checkExtensionCapability("EGL_EXT_gl_colorspace_display_p3_passthrough") ? 13456 : 0;
    }

    private EGLConfig chooseEglConfig() {
        int[] iArr = new int[1];
        EGLConfig[] eGLConfigArr = new EGLConfig[1];
        if (!EGL14.eglChooseConfig(this.mEglDisplay, getConfig(), 0, eGLConfigArr, 0, 1, iArr, 0)) {
            String str = TAG;
            Log.w(str, "eglChooseConfig failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            return null;
        } else if (iArr[0] > 0) {
            return eGLConfigArr[0];
        } else {
            String str2 = TAG;
            Log.w(str2, "eglChooseConfig failed, invalid configs count: " + iArr[0]);
            return null;
        }
    }

    private int[] getConfig() {
        return new int[]{12324, 8, 12323, 8, 12322, 8, 12321, 0, 12325, 0, 12326, 0, 12352, 4, 12327, 12344, 12344};
    }

    public boolean createEglSurface(SurfaceHolder surfaceHolder, boolean z) {
        Log.d(TAG, "createEglSurface start");
        if (!hasEglDisplay() || !surfaceHolder.getSurface().isValid()) {
            String str = TAG;
            Log.w(str, "Create EglSurface failed: hasEglDisplay=" + hasEglDisplay() + ", has valid surface=" + surfaceHolder.getSurface().isValid());
            return false;
        }
        int[] iArr = null;
        int wcgCapability = getWcgCapability();
        if (z && checkExtensionCapability("EGL_KHR_gl_colorspace") && wcgCapability > 0) {
            iArr = new int[]{12445, wcgCapability, 12344};
        }
        this.mEglSurface = askCreatingEglWindowSurface(surfaceHolder, iArr, 0);
        if (!hasEglSurface()) {
            String str2 = TAG;
            Log.w(str2, "createWindowSurface failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            return false;
        }
        EGLDisplay eGLDisplay = this.mEglDisplay;
        EGLSurface eGLSurface = this.mEglSurface;
        if (!EGL14.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, this.mEglContext)) {
            String str3 = TAG;
            Log.w(str3, "eglMakeCurrent failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
            return false;
        }
        Log.d(TAG, "createEglSurface done");
        return true;
    }

    /* access modifiers changed from: package-private */
    public EGLSurface askCreatingEglWindowSurface(SurfaceHolder surfaceHolder, int[] iArr, int i) {
        return EGL14.eglCreateWindowSurface(this.mEglDisplay, this.mEglConfig, surfaceHolder, iArr, i);
    }

    public void destroyEglSurface() {
        if (hasEglSurface()) {
            EGLDisplay eGLDisplay = this.mEglDisplay;
            EGLSurface eGLSurface = EGL14.EGL_NO_SURFACE;
            EGL14.eglMakeCurrent(eGLDisplay, eGLSurface, eGLSurface, EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(this.mEglDisplay, this.mEglSurface);
            this.mEglSurface = EGL14.EGL_NO_SURFACE;
        }
    }

    public boolean hasEglSurface() {
        EGLSurface eGLSurface = this.mEglSurface;
        return (eGLSurface == null || eGLSurface == EGL14.EGL_NO_SURFACE) ? false : true;
    }

    public boolean createEglContext() {
        Log.d(TAG, "createEglContext start");
        int[] iArr = new int[5];
        iArr[0] = 12440;
        char c = 2;
        iArr[1] = 2;
        if (checkExtensionCapability("EGL_IMG_context_priority")) {
            iArr[2] = 12544;
            c = 4;
            iArr[3] = 12547;
        }
        iArr[c] = 12344;
        if (hasEglDisplay()) {
            this.mEglContext = EGL14.eglCreateContext(this.mEglDisplay, this.mEglConfig, EGL14.EGL_NO_CONTEXT, iArr, 0);
            if (!hasEglContext()) {
                String str = TAG;
                Log.w(str, "eglCreateContext failed: " + GLUtils.getEGLErrorString(EGL14.eglGetError()));
                return false;
            }
            Log.d(TAG, "createEglContext done");
            return true;
        }
        Log.w(TAG, "mEglDisplay is null");
        return false;
    }

    public void destroyEglContext() {
        if (hasEglContext()) {
            EGL14.eglDestroyContext(this.mEglDisplay, this.mEglContext);
            this.mEglContext = EGL14.EGL_NO_CONTEXT;
        }
    }

    public boolean hasEglContext() {
        EGLContext eGLContext = this.mEglContext;
        return (eGLContext == null || eGLContext == EGL14.EGL_NO_CONTEXT) ? false : true;
    }

    public boolean hasEglDisplay() {
        EGLDisplay eGLDisplay = this.mEglDisplay;
        return (eGLDisplay == null || eGLDisplay == EGL14.EGL_NO_DISPLAY) ? false : true;
    }

    public boolean swapBuffer() {
        boolean eglSwapBuffers = EGL14.eglSwapBuffers(this.mEglDisplay, this.mEglSurface);
        int eglGetError = EGL14.eglGetError();
        if (eglGetError != 12288) {
            String str = TAG;
            Log.w(str, "eglSwapBuffers failed: " + GLUtils.getEGLErrorString(eglGetError));
        }
        return eglSwapBuffers;
    }

    public void finish() {
        if (hasEglSurface()) {
            destroyEglSurface();
        }
        if (hasEglContext()) {
            destroyEglContext();
        }
        if (hasEglDisplay()) {
            terminateEglDisplay();
        }
        this.mEglReady = false;
    }

    /* access modifiers changed from: package-private */
    public void terminateEglDisplay() {
        EGL14.eglTerminate(this.mEglDisplay);
        this.mEglDisplay = EGL14.EGL_NO_DISPLAY;
    }

    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        StringBuilder sb = new StringBuilder();
        sb.append(this.mEglVersion[0]);
        sb.append(".");
        sb.append(this.mEglVersion[1]);
        String sb2 = sb.toString();
        printWriter.print(str);
        printWriter.print("EGL version=");
        printWriter.print(sb2);
        printWriter.print(", ");
        printWriter.print("EGL ready=");
        printWriter.print(this.mEglReady);
        printWriter.print(", ");
        printWriter.print("has EglContext=");
        printWriter.print(hasEglContext());
        printWriter.print(", ");
        printWriter.print("has EglSurface=");
        printWriter.println(hasEglSurface());
        int[] config = getConfig();
        StringBuilder sb3 = new StringBuilder();
        sb3.append('{');
        for (int i : config) {
            sb3.append("0x");
            sb3.append(Integer.toHexString(i));
            sb3.append(",");
        }
        sb3.setCharAt(sb3.length() - 1, '}');
        printWriter.print(str);
        printWriter.print("EglConfig=");
        printWriter.println(sb3.toString());
    }
}

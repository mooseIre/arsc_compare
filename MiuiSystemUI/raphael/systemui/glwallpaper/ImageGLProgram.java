package com.android.systemui.glwallpaper;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

public class ImageGLProgram {
    private static final String TAG = "ImageGLProgram";
    private Context mContext;
    private int mProgramHandle;

    public ImageGLProgram(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private int loadShaderProgram(int i, int i2) {
        return getProgramHandle(getShaderHandle(35633, getShaderResource(i)), getShaderHandle(35632, getShaderResource(i2)));
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x002e, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r1.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0037, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:?, code lost:
        r1.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private java.lang.String getShaderResource(int r4) {
        /*
            r3 = this;
            android.content.Context r3 = r3.mContext
            android.content.res.Resources r3 = r3.getResources()
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.io.BufferedReader r1 = new java.io.BufferedReader     // Catch:{ NotFoundException | IOException -> 0x0038 }
            java.io.InputStreamReader r2 = new java.io.InputStreamReader     // Catch:{ NotFoundException | IOException -> 0x0038 }
            java.io.InputStream r3 = r3.openRawResource(r4)     // Catch:{ NotFoundException | IOException -> 0x0038 }
            r2.<init>(r3)     // Catch:{ NotFoundException | IOException -> 0x0038 }
            r1.<init>(r2)     // Catch:{ NotFoundException | IOException -> 0x0038 }
        L_0x0019:
            java.lang.String r3 = r1.readLine()     // Catch:{ all -> 0x002c }
            if (r3 == 0) goto L_0x0028
            r0.append(r3)     // Catch:{ all -> 0x002c }
            java.lang.String r3 = "\n"
            r0.append(r3)     // Catch:{ all -> 0x002c }
            goto L_0x0019
        L_0x0028:
            r1.close()     // Catch:{ NotFoundException | IOException -> 0x0038 }
            goto L_0x0041
        L_0x002c:
            r3 = move-exception
            throw r3     // Catch:{ all -> 0x002e }
        L_0x002e:
            r4 = move-exception
            r1.close()     // Catch:{ all -> 0x0033 }
            goto L_0x0037
        L_0x0033:
            r0 = move-exception
            r3.addSuppressed(r0)     // Catch:{ NotFoundException | IOException -> 0x0038 }
        L_0x0037:
            throw r4     // Catch:{ NotFoundException | IOException -> 0x0038 }
        L_0x0038:
            r3 = move-exception
            java.lang.String r4 = TAG
            java.lang.String r0 = "Can not read the shader source"
            android.util.Log.d(r4, r0, r3)
            r0 = 0
        L_0x0041:
            if (r0 != 0) goto L_0x0046
            java.lang.String r3 = ""
            goto L_0x004a
        L_0x0046:
            java.lang.String r3 = r0.toString()
        L_0x004a:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.glwallpaper.ImageGLProgram.getShaderResource(int):java.lang.String");
    }

    private int getShaderHandle(int i, String str) {
        int glCreateShader = GLES20.glCreateShader(i);
        if (glCreateShader == 0) {
            String str2 = TAG;
            Log.d(str2, "Create shader failed, type=" + i);
            return 0;
        }
        GLES20.glShaderSource(glCreateShader, str);
        GLES20.glCompileShader(glCreateShader);
        return glCreateShader;
    }

    private int getProgramHandle(int i, int i2) {
        int glCreateProgram = GLES20.glCreateProgram();
        if (glCreateProgram == 0) {
            Log.d(TAG, "Can not create OpenGL ES program");
            return 0;
        }
        GLES20.glAttachShader(glCreateProgram, i);
        GLES20.glAttachShader(glCreateProgram, i2);
        GLES20.glLinkProgram(glCreateProgram);
        return glCreateProgram;
    }

    public boolean useGLProgram(int i, int i2) {
        this.mProgramHandle = loadShaderProgram(i, i2);
        GLES20.glUseProgram(this.mProgramHandle);
        return true;
    }

    /* access modifiers changed from: package-private */
    public int getAttributeHandle(String str) {
        return GLES20.glGetAttribLocation(this.mProgramHandle, str);
    }

    /* access modifiers changed from: package-private */
    public int getUniformHandle(String str) {
        return GLES20.glGetUniformLocation(this.mProgramHandle, str);
    }
}

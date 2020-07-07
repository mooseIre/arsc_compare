package com.android.systemui.glwallpaper;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class ImageGLWallpaper {
    private static final String TAG = "ImageGLWallpaper";
    private static final float[] TEXTURES = {0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f};
    private static final float[] VERTICES = {-1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, 1.0f, 1.0f, -1.0f, 1.0f, -1.0f, -1.0f};
    private int mAttrPosition;
    private int mAttrTextureCoordinates;
    private float[] mCurrentTexCoordinate;
    private final ImageGLProgram mProgram;
    private final FloatBuffer mTextureBuffer;
    private int mTextureId;
    private int mUniAod2Opacity;
    private int mUniDarkMode;
    private int mUniDarken;
    private int mUniPer85;
    private int mUniReveal;
    private int mUniTexture;
    private int mUniWallpaperAlpha;
    private int mUniWindowAlpha;
    private final FloatBuffer mVertexBuffer = ByteBuffer.allocateDirect(VERTICES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();

    public ImageGLWallpaper(ImageGLProgram imageGLProgram) {
        this.mProgram = imageGLProgram;
        this.mVertexBuffer.put(VERTICES);
        this.mVertexBuffer.position(0);
        this.mTextureBuffer = ByteBuffer.allocateDirect(TEXTURES.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mTextureBuffer.put(TEXTURES);
        this.mTextureBuffer.position(0);
    }

    public void setup(Bitmap bitmap) {
        setupAttributes();
        setupUniforms();
        setupTexture(bitmap);
    }

    private void setupAttributes() {
        this.mAttrPosition = this.mProgram.getAttributeHandle("aPosition");
        this.mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(this.mAttrPosition, 2, 5126, false, 0, this.mVertexBuffer);
        GLES20.glEnableVertexAttribArray(this.mAttrPosition);
        this.mAttrTextureCoordinates = this.mProgram.getAttributeHandle("aTextureCoordinates");
        this.mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(this.mAttrTextureCoordinates, 2, 5126, false, 0, this.mTextureBuffer);
        GLES20.glEnableVertexAttribArray(this.mAttrTextureCoordinates);
    }

    private void setupUniforms() {
        this.mUniAod2Opacity = this.mProgram.getUniformHandle("uAod2Opacity");
        this.mUniPer85 = this.mProgram.getUniformHandle("uPer85");
        this.mUniReveal = this.mProgram.getUniformHandle("uReveal");
        this.mUniDarken = this.mProgram.getUniformHandle("uDarken");
        this.mUniDarkMode = this.mProgram.getUniformHandle("uDarkMode");
        this.mUniWallpaperAlpha = this.mProgram.getUniformHandle("uWallpaperAlpha");
        this.mUniWindowAlpha = this.mProgram.getUniformHandle("uWindowAlpha");
        this.mUniTexture = this.mProgram.getUniformHandle("uTexture");
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getHandle(java.lang.String r3) {
        /*
            r2 = this;
            int r0 = r3.hashCode()
            r1 = -1
            switch(r0) {
                case -2002784538: goto L_0x006c;
                case -1971276870: goto L_0x0061;
                case -1496385676: goto L_0x0056;
                case -1091770206: goto L_0x004b;
                case -868354715: goto L_0x0040;
                case 17245217: goto L_0x0036;
                case 786698702: goto L_0x002b;
                case 1477515161: goto L_0x001f;
                case 1583025322: goto L_0x0015;
                case 1752268881: goto L_0x000a;
                default: goto L_0x0008;
            }
        L_0x0008:
            goto L_0x0078
        L_0x000a:
            java.lang.String r0 = "uWallpaperAlpha"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0078
            r3 = 7
            goto L_0x0079
        L_0x0015:
            java.lang.String r0 = "aPosition"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0078
            r3 = 0
            goto L_0x0079
        L_0x001f:
            java.lang.String r0 = "uWindowAlpha"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0078
            r3 = 8
            goto L_0x0079
        L_0x002b:
            java.lang.String r0 = "uDarkMode"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0078
            r3 = 6
            goto L_0x0079
        L_0x0036:
            java.lang.String r0 = "aTextureCoordinates"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0078
            r3 = 1
            goto L_0x0079
        L_0x0040:
            java.lang.String r0 = "uPer85"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0078
            r3 = 3
            goto L_0x0079
        L_0x004b:
            java.lang.String r0 = "uReveal"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0078
            r3 = 4
            goto L_0x0079
        L_0x0056:
            java.lang.String r0 = "uDarken"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0078
            r3 = 5
            goto L_0x0079
        L_0x0061:
            java.lang.String r0 = "uAod2Opacity"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0078
            r3 = 2
            goto L_0x0079
        L_0x006c:
            java.lang.String r0 = "uTexture"
            boolean r3 = r3.equals(r0)
            if (r3 == 0) goto L_0x0078
            r3 = 9
            goto L_0x0079
        L_0x0078:
            r3 = r1
        L_0x0079:
            switch(r3) {
                case 0: goto L_0x0098;
                case 1: goto L_0x0095;
                case 2: goto L_0x0092;
                case 3: goto L_0x008f;
                case 4: goto L_0x008c;
                case 5: goto L_0x0089;
                case 6: goto L_0x0086;
                case 7: goto L_0x0083;
                case 8: goto L_0x0080;
                case 9: goto L_0x007d;
                default: goto L_0x007c;
            }
        L_0x007c:
            return r1
        L_0x007d:
            int r2 = r2.mUniTexture
            return r2
        L_0x0080:
            int r2 = r2.mUniWindowAlpha
            return r2
        L_0x0083:
            int r2 = r2.mUniWallpaperAlpha
            return r2
        L_0x0086:
            int r2 = r2.mUniDarkMode
            return r2
        L_0x0089:
            int r2 = r2.mUniDarken
            return r2
        L_0x008c:
            int r2 = r2.mUniReveal
            return r2
        L_0x008f:
            int r2 = r2.mUniPer85
            return r2
        L_0x0092:
            int r2 = r2.mUniAod2Opacity
            return r2
        L_0x0095:
            int r2 = r2.mAttrTextureCoordinates
            return r2
        L_0x0098:
            int r2 = r2.mAttrPosition
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.glwallpaper.ImageGLWallpaper.getHandle(java.lang.String):int");
    }

    public void draw() {
        GLES20.glDrawArrays(4, 0, VERTICES.length / 2);
    }

    private void setupTexture(Bitmap bitmap) {
        int[] iArr = new int[1];
        if (bitmap == null) {
            Log.w(TAG, "setupTexture: invalid bitmap");
            return;
        }
        GLES20.glGenTextures(1, iArr, 0);
        if (iArr[0] == 0) {
            Log.w(TAG, "setupTexture: glGenTextures() failed");
            return;
        }
        GLES20.glBindTexture(3553, iArr[0]);
        GLUtils.texImage2D(3553, 0, bitmap, 0);
        GLES20.glTexParameteri(3553, 10241, 9729);
        GLES20.glTexParameteri(3553, 10240, 9729);
        this.mTextureId = iArr[0];
    }

    public void useTexture() {
        GLES20.glActiveTexture(33984);
        GLES20.glBindTexture(3553, this.mTextureId);
        GLES20.glUniform1i(this.mUniTexture, 0);
    }

    public void adjustTextureCoordinates(Rect rect, Rect rect2, float f, float f2) {
        this.mCurrentTexCoordinate = (float[]) TEXTURES.clone();
        if (rect == null || rect2 == null) {
            this.mTextureBuffer.put(this.mCurrentTexCoordinate);
            this.mTextureBuffer.position(0);
            return;
        }
        int width = rect.width();
        int height = rect.height();
        int width2 = rect2.width();
        int height2 = rect2.height();
        if (width > width2) {
            float f3 = (float) width;
            float round = ((float) Math.round(((float) (width - width2)) * f)) / f3;
            float f4 = ((float) width2) / f3;
            if (height < height2) {
                f4 *= ((float) height) / ((float) height2);
            }
            if (round + f4 > 1.0f) {
                round = 1.0f - f4;
            }
            int i = 0;
            while (true) {
                float[] fArr = this.mCurrentTexCoordinate;
                if (i >= fArr.length) {
                    break;
                }
                if (i == 2 || i == 4 || i == 6) {
                    this.mCurrentTexCoordinate[i] = Math.min(1.0f, round + f4);
                } else {
                    fArr[i] = round;
                }
                i += 2;
            }
        }
        if (height > height2) {
            float f5 = (float) height;
            float round2 = ((float) Math.round(((float) (height - height2)) * f2)) / f5;
            float f6 = ((float) height2) / f5;
            if (width < width2) {
                f6 *= ((float) width) / ((float) width2);
            }
            if (round2 + f6 > 1.0f) {
                round2 = 1.0f - f6;
            }
            int i2 = 1;
            while (true) {
                float[] fArr2 = this.mCurrentTexCoordinate;
                if (i2 >= fArr2.length) {
                    break;
                }
                if (i2 == 1 || i2 == 3 || i2 == 11) {
                    this.mCurrentTexCoordinate[i2] = Math.min(1.0f, round2 + f6);
                } else {
                    fArr2[i2] = round2;
                }
                i2 += 2;
            }
        }
        this.mTextureBuffer.put(this.mCurrentTexCoordinate);
        this.mTextureBuffer.position(0);
    }

    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        if (this.mCurrentTexCoordinate != null) {
            int i = 0;
            while (true) {
                float[] fArr = this.mCurrentTexCoordinate;
                if (i >= fArr.length) {
                    break;
                }
                sb.append(fArr[i]);
                sb.append(',');
                if (i == this.mCurrentTexCoordinate.length - 1) {
                    sb.deleteCharAt(sb.length() - 1);
                }
                i++;
            }
        }
        sb.append('}');
        printWriter.print(str);
        printWriter.print("mTexCoordinates=");
        printWriter.println(sb.toString());
    }
}

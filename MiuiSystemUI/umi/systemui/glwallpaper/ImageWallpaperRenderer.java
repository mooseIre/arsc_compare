package com.android.systemui.glwallpaper;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.util.Log;
import android.util.MathUtils;
import android.util.Size;
import android.view.DisplayInfo;
import android.view.WindowManager;
import com.android.systemui.glwallpaper.GLWallpaperRenderer;
import com.android.systemui.glwallpaper.ImageRevealHelper;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ImageWallpaperRenderer implements GLWallpaperRenderer, ImageRevealHelper.RevealStateListener {
    private static final String TAG = "ImageWallpaperRenderer";
    private Bitmap mBitmap;
    protected Context mContext;
    private DisplayInfo mDisplayInfo;
    private final ImageDarkenHelper mImageDarkenHelper;
    private final ImageProcessHelper mImageProcessHelper;
    private ImageRevealHelper mImageRevealHelper;
    private final ImageGLProgram mProgram;
    private GLWallpaperRenderer.SurfaceProxy mProxy;
    private final Rect mScissor;
    private boolean mScissorMode;
    private final Rect mSurfaceSize = new Rect();
    private final Rect mViewport = new Rect();
    private final ImageGLWallpaper mWallpaper;
    private float mWallpaperAlpha;
    public final WallpaperManager mWallpaperManager;
    private float mWindowAlpha;
    private float mXOffset;
    private float mYOffset;

    /* access modifiers changed from: protected */
    public abstract boolean enableScissorMode();

    /* access modifiers changed from: protected */
    public abstract Bitmap getBitmap();

    /* access modifiers changed from: protected */
    public abstract int getFragmentResId();

    /* access modifiers changed from: protected */
    public boolean getHasKeyguardWallpaperEffects() {
        return true;
    }

    /* access modifiers changed from: protected */
    public abstract int getVertexResId();

    public ImageWallpaperRenderer(Context context, GLWallpaperRenderer.SurfaceProxy surfaceProxy) {
        WallpaperManager wallpaperManager = (WallpaperManager) context.getSystemService(WallpaperManager.class);
        this.mWallpaperManager = wallpaperManager;
        if (wallpaperManager == null) {
            Log.w(TAG, "WallpaperManager not available");
        }
        this.mDisplayInfo = new DisplayInfo();
        ((WindowManager) context.getSystemService(WindowManager.class)).getDefaultDisplay().getDisplayInfo(this.mDisplayInfo);
        DisplayInfo displayInfo = this.mDisplayInfo;
        this.mScissor = new Rect(0, 0, displayInfo.logicalWidth, displayInfo.logicalHeight);
        this.mContext = context;
        this.mProxy = surfaceProxy;
        this.mProgram = new ImageGLProgram(context);
        this.mWallpaper = new ImageGLWallpaper(this.mProgram);
        this.mImageProcessHelper = new ImageProcessHelper();
        this.mImageRevealHelper = new ImageRevealHelper(this);
        this.mImageDarkenHelper = new ImageDarkenHelper();
        loadBitmap();
    }

    public void onSurfaceCreated() {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        this.mProgram.useGLProgram(getVertexResId(), getFragmentResId());
        GLES20.glEnable(3042);
        GLES20.glBlendFunc(770, 771);
        if (!loadBitmap()) {
            Log.w(TAG, "reload bitmap failed!");
            return;
        }
        this.mWallpaper.setup(this.mBitmap);
        this.mBitmap = null;
    }

    private boolean loadBitmap() {
        if (this.mBitmap == null) {
            Bitmap bitmap = getBitmap();
            this.mBitmap = bitmap;
            WallpaperManager wallpaperManager = this.mWallpaperManager;
            if (wallpaperManager != null && bitmap == null) {
                this.mBitmap = wallpaperManager.getBitmap();
                this.mWallpaperManager.forgetLoadedWallpaper();
            }
            Bitmap bitmap2 = this.mBitmap;
            if (bitmap2 != null) {
                this.mSurfaceSize.set(0, 0, Math.max(this.mDisplayInfo.logicalWidth, bitmap2.getWidth()), Math.max(this.mDisplayInfo.logicalHeight, this.mBitmap.getHeight()));
            }
        }
        if (this.mBitmap != null) {
            return true;
        }
        return false;
    }

    public void onSurfaceChanged(int i, int i2) {
        GLES20.glViewport(0, 0, i, i2);
    }

    public void onDrawFrame() {
        float threshold = this.mImageProcessHelper.getThreshold();
        float reveal = this.mImageRevealHelper.getReveal();
        boolean isDarken = this.mImageDarkenHelper.isDarken();
        boolean isInDarkWallpaperMode = this.mImageDarkenHelper.isInDarkWallpaperMode();
        GLES20.glUniform1f(this.mWallpaper.getHandle("uAod2Opacity"), 1.0f);
        GLES20.glUniform1f(this.mWallpaper.getHandle("uPer85"), threshold);
        GLES20.glUniform1f(this.mWallpaper.getHandle("uReveal"), reveal);
        GLES20.glUniform1i(this.mWallpaper.getHandle("uDarken"), isDarken ? 1 : 0);
        GLES20.glUniform1i(this.mWallpaper.getHandle("uDarkMode"), isInDarkWallpaperMode ? 1 : 0);
        GLES20.glUniform1f(this.mWallpaper.getHandle("uWallpaperAlpha"), this.mWallpaperAlpha);
        GLES20.glUniform1f(this.mWallpaper.getHandle("uWindowAlpha"), this.mWindowAlpha);
        GLES20.glClear(16384);
        if (this.mScissorMode) {
            scaleViewport(reveal);
        } else {
            GLES20.glViewport(0, 0, this.mSurfaceSize.width(), this.mSurfaceSize.height());
        }
        this.mWallpaper.useTexture();
        this.mWallpaper.draw();
    }

    public void updateAmbientMode(boolean z, long j) {
        this.mImageRevealHelper.updateAwake(!z, j);
    }

    public void startUnlockAnim(boolean z, long j) {
        if (this.mSurfaceSize.width() == this.mScissor.width() && this.mSurfaceSize.height() == this.mScissor.height() && getHasKeyguardWallpaperEffects()) {
            this.mImageRevealHelper.startUnlockAnim(z, j);
        }
    }

    public void updateOffsets(float f, float f2) {
        this.mXOffset = f;
        this.mYOffset = f2;
        int width = (int) (((float) (this.mSurfaceSize.width() - this.mScissor.width())) * f);
        Rect rect = this.mScissor;
        rect.set(width, rect.top, this.mScissor.width() + width, rect.bottom);
    }

    public void updateDarken(boolean z) {
        this.mImageDarkenHelper.setDarken(z);
    }

    public void updateDarkWallpaperMode(boolean z) {
        this.mImageDarkenHelper.setInDarkWallpaperMode(z);
    }

    public void updateWallpaperAlpha(float f) {
        this.mWallpaperAlpha = f;
    }

    public Size reportSurfaceSize() {
        return new Size(this.mSurfaceSize.width(), this.mSurfaceSize.height());
    }

    public void finish() {
        this.mImageRevealHelper.cancelAnimate();
        this.mImageRevealHelper = null;
        this.mProxy = null;
    }

    private void scaleViewport(float f) {
        Rect rect = this.mScissor;
        int i = rect.left;
        int i2 = rect.top;
        int width = rect.width();
        int height = this.mScissor.height();
        float lerp = MathUtils.lerp(1.0f, 1.2f, f);
        float f2 = (1.0f - lerp) / 2.0f;
        float f3 = (float) width;
        float f4 = (float) height;
        this.mViewport.set((int) (((float) i) + (f3 * f2)), (int) (((float) i2) + (f2 * f4)), (int) (f3 * lerp), (int) (f4 * lerp));
        Rect rect2 = this.mViewport;
        GLES20.glViewport(rect2.left, rect2.top, rect2.right, rect2.bottom);
    }

    public void onRevealStateChanged() {
        this.mProxy.requestRender();
    }

    public void onRevealStart() {
        this.mScissorMode = enableScissorMode();
        this.mWallpaper.adjustTextureCoordinates(this.mSurfaceSize, this.mScissor, this.mXOffset, this.mYOffset);
        this.mProxy.preRender();
    }

    public void onRevealEnd() {
        this.mScissorMode = false;
        this.mWallpaper.adjustTextureCoordinates((Rect) null, (Rect) null, 0.0f, 0.0f);
        this.mProxy.requestRender();
        this.mProxy.postRender();
    }

    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print(str);
        printWriter.print("mProxy=");
        printWriter.print(this.mProxy);
        printWriter.print(str);
        printWriter.print("mSurfaceSize=");
        printWriter.print(this.mSurfaceSize);
        printWriter.print(str);
        printWriter.print("mScissor=");
        printWriter.print(this.mScissor);
        printWriter.print(str);
        printWriter.print("mViewport=");
        printWriter.print(this.mViewport);
        printWriter.print(str);
        printWriter.print("mScissorMode=");
        printWriter.print(this.mScissorMode);
        printWriter.print(str);
        printWriter.print("mXOffset=");
        printWriter.print(this.mXOffset);
        printWriter.print(str);
        printWriter.print("mYOffset=");
        printWriter.print(this.mYOffset);
        printWriter.print(str);
        printWriter.print("threshold=");
        printWriter.print(this.mImageProcessHelper.getThreshold());
        this.mWallpaper.dump(str, fileDescriptor, printWriter, strArr);
    }
}

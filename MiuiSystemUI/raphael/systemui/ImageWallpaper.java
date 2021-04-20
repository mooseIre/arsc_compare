package com.android.systemui;

import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Trace;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.ImageWallpaper;
import com.android.systemui.glwallpaper.EglHelper;
import com.android.systemui.glwallpaper.GLWallpaperRenderer;
import com.android.systemui.glwallpaper.ImageWallpaperRenderer;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ImageWallpaper extends WallpaperService {
    private static final String TAG = ImageWallpaper.class.getSimpleName();
    private HandlerThread mWorker;

    public void onCreate() {
        super.onCreate();
        HandlerThread handlerThread = new HandlerThread(TAG);
        this.mWorker = handlerThread;
        handlerThread.start();
    }

    public WallpaperService.Engine onCreateEngine() {
        return new GLEngine();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mWorker.quitSafely();
        this.mWorker = null;
    }

    /* access modifiers changed from: package-private */
    public class GLEngine extends WallpaperService.Engine {
        @VisibleForTesting
        static final int MIN_SURFACE_HEIGHT = 64;
        @VisibleForTesting
        static final int MIN_SURFACE_WIDTH = 64;
        private EglHelper mEglHelper;
        private final Runnable mFinishRenderingTask = new Runnable() {
            /* class com.android.systemui.$$Lambda$ImageWallpaper$GLEngine$4IwqG_0jMNtMT6yCqqjKAFKSvE */

            public final void run() {
                ImageWallpaper.GLEngine.m7lambda$4IwqG_0jMNtMT6yCqqjKAFKSvE(ImageWallpaper.GLEngine.this);
            }
        };
        private GLWallpaperRenderer mRenderer;

        public boolean shouldZoomOutWallpaper() {
            return true;
        }

        GLEngine() {
            super(ImageWallpaper.this);
        }

        @VisibleForTesting
        GLEngine(Handler handler) {
            super(ImageWallpaper.this, $$Lambda$87DoTfJA3qVM7QF6F_6BpQlQTA.INSTANCE, handler);
        }

        public void onCreate(SurfaceHolder surfaceHolder) {
            this.mEglHelper = getEglHelperInstance();
            this.mRenderer = getRendererInstance();
            setFixedSizeAllowed(true);
            setOffsetNotificationsEnabled(false);
            updateSurfaceSize();
        }

        /* access modifiers changed from: package-private */
        public EglHelper getEglHelperInstance() {
            return new EglHelper();
        }

        /* access modifiers changed from: package-private */
        public ImageWallpaperRenderer getRendererInstance() {
            return new ImageWallpaperRenderer(getDisplayContext());
        }

        private void updateSurfaceSize() {
            SurfaceHolder surfaceHolder = getSurfaceHolder();
            Size reportSurfaceSize = this.mRenderer.reportSurfaceSize();
            surfaceHolder.setFixedSize(Math.max(64, reportSurfaceSize.getWidth()), Math.max(64, reportSurfaceSize.getHeight()));
        }

        public void onDestroy() {
            ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable() {
                /* class com.android.systemui.$$Lambda$ImageWallpaper$GLEngine$BobZgI4REJvgDbbrYxKQK2v8vCg */

                public final void run() {
                    ImageWallpaper.GLEngine.this.lambda$onDestroy$0$ImageWallpaper$GLEngine();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onDestroy$0 */
        public /* synthetic */ void lambda$onDestroy$0$ImageWallpaper$GLEngine() {
            this.mRenderer.finish();
            this.mRenderer = null;
            this.mEglHelper.finish();
            this.mEglHelper = null;
        }

        public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
            if (ImageWallpaper.this.mWorker != null) {
                ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable(surfaceHolder) {
                    /* class com.android.systemui.$$Lambda$ImageWallpaper$GLEngine$iLRwANP3nahTog6rPMk87G_B1tQ */
                    public final /* synthetic */ SurfaceHolder f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        ImageWallpaper.GLEngine.this.lambda$onSurfaceCreated$1$ImageWallpaper$GLEngine(this.f$1);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onSurfaceCreated$1 */
        public /* synthetic */ void lambda$onSurfaceCreated$1$ImageWallpaper$GLEngine(SurfaceHolder surfaceHolder) {
            this.mEglHelper.init(surfaceHolder, needSupportWideColorGamut());
            this.mRenderer.onSurfaceCreated();
        }

        public void onSurfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            if (ImageWallpaper.this.mWorker != null) {
                ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable(i2, i3) {
                    /* class com.android.systemui.$$Lambda$ImageWallpaper$GLEngine$NZAB5XGFpHaOG6R1lDvpakCYM */
                    public final /* synthetic */ int f$1;
                    public final /* synthetic */ int f$2;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                    }

                    public final void run() {
                        ImageWallpaper.GLEngine.this.lambda$onSurfaceChanged$2$ImageWallpaper$GLEngine(this.f$1, this.f$2);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onSurfaceChanged$2 */
        public /* synthetic */ void lambda$onSurfaceChanged$2$ImageWallpaper$GLEngine(int i, int i2) {
            this.mRenderer.onSurfaceChanged(i, i2);
        }

        public void onSurfaceRedrawNeeded(SurfaceHolder surfaceHolder) {
            if (ImageWallpaper.this.mWorker != null) {
                ImageWallpaper.this.mWorker.getThreadHandler().post(new Runnable() {
                    /* class com.android.systemui.$$Lambda$ImageWallpaper$GLEngine$dwIVxRzjo8QTPBtgktS9kM6mj4o */

                    public final void run() {
                        ImageWallpaper.GLEngine.lambda$dwIVxRzjo8QTPBtgktS9kM6mj4o(ImageWallpaper.GLEngine.this);
                    }
                });
            }
        }

        /* access modifiers changed from: private */
        public void drawFrame() {
            preRender();
            requestRender();
            postRender();
        }

        public void preRender() {
            Trace.beginSection("ImageWallpaper#preRender");
            preRenderInternal();
            Trace.endSection();
        }

        private void preRenderInternal() {
            boolean z;
            Rect surfaceFrame = getSurfaceHolder().getSurfaceFrame();
            cancelFinishRenderingTask();
            if (!this.mEglHelper.hasEglContext()) {
                this.mEglHelper.destroyEglSurface();
                if (!this.mEglHelper.createEglContext()) {
                    Log.w(ImageWallpaper.TAG, "recreate egl context failed!");
                } else {
                    z = true;
                    if (this.mEglHelper.hasEglContext() && !this.mEglHelper.hasEglSurface() && !this.mEglHelper.createEglSurface(getSurfaceHolder(), needSupportWideColorGamut())) {
                        Log.w(ImageWallpaper.TAG, "recreate egl surface failed!");
                    }
                    if (this.mEglHelper.hasEglContext() && this.mEglHelper.hasEglSurface() && z) {
                        this.mRenderer.onSurfaceCreated();
                        this.mRenderer.onSurfaceChanged(surfaceFrame.width(), surfaceFrame.height());
                        return;
                    }
                    return;
                }
            }
            z = false;
            Log.w(ImageWallpaper.TAG, "recreate egl surface failed!");
            if (this.mEglHelper.hasEglContext()) {
            }
        }

        public void requestRender() {
            Trace.beginSection("ImageWallpaper#requestRender");
            requestRenderInternal();
            Trace.endSection();
        }

        private void requestRenderInternal() {
            Rect surfaceFrame = getSurfaceHolder().getSurfaceFrame();
            if (this.mEglHelper.hasEglContext() && this.mEglHelper.hasEglSurface() && surfaceFrame.width() > 0 && surfaceFrame.height() > 0) {
                this.mRenderer.onDrawFrame();
                if (!this.mEglHelper.swapBuffer()) {
                    Log.e(ImageWallpaper.TAG, "drawFrame failed!");
                    return;
                }
                return;
            }
            String str = ImageWallpaper.TAG;
            Log.e(str, "requestRender: not ready, has context=" + this.mEglHelper.hasEglContext() + ", has surface=" + this.mEglHelper.hasEglSurface() + ", frame=" + surfaceFrame);
        }

        public void postRender() {
            Trace.beginSection("ImageWallpaper#postRender");
            scheduleFinishRendering();
            Trace.endSection();
        }

        private void cancelFinishRenderingTask() {
            if (ImageWallpaper.this.mWorker != null) {
                ImageWallpaper.this.mWorker.getThreadHandler().removeCallbacks(this.mFinishRenderingTask);
            }
        }

        private void scheduleFinishRendering() {
            if (ImageWallpaper.this.mWorker != null) {
                cancelFinishRenderingTask();
                ImageWallpaper.this.mWorker.getThreadHandler().postDelayed(this.mFinishRenderingTask, 1000);
            }
        }

        /* access modifiers changed from: private */
        public void finishRendering() {
            Trace.beginSection("ImageWallpaper#finishRendering");
            EglHelper eglHelper = this.mEglHelper;
            if (eglHelper != null) {
                eglHelper.destroyEglSurface();
                this.mEglHelper.destroyEglContext();
            }
            Trace.endSection();
        }

        private boolean needSupportWideColorGamut() {
            return this.mRenderer.isWcgContent();
        }

        /* access modifiers changed from: protected */
        public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            super.dump(str, fileDescriptor, printWriter, strArr);
            printWriter.print(str);
            printWriter.print("Engine=");
            printWriter.println(this);
            printWriter.print(str);
            printWriter.print("valid surface=");
            Object obj = "null";
            printWriter.println((getSurfaceHolder() == null || getSurfaceHolder().getSurface() == null) ? obj : Boolean.valueOf(getSurfaceHolder().getSurface().isValid()));
            printWriter.print(str);
            printWriter.print("surface frame=");
            if (getSurfaceHolder() != null) {
                obj = getSurfaceHolder().getSurfaceFrame();
            }
            printWriter.println(obj);
            this.mEglHelper.dump(str, fileDescriptor, printWriter, strArr);
            this.mRenderer.dump(str, fileDescriptor, printWriter, strArr);
        }
    }
}

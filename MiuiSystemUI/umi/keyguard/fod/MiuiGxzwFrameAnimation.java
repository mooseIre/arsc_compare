package com.android.keyguard.fod;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Slog;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/* access modifiers changed from: package-private */
public class MiuiGxzwFrameAnimation {
    private volatile float alpha = 1.0f;
    private final Context mContext;
    private final Handler mDrawHandler;
    private DrawRunnable mDrawRunnable;
    private final HandlerThread mDrawThread;
    private volatile int mFrameInterval = 32;
    private final Handler mHandler = new Handler();
    private boolean mLastDrawAnim = false;
    private volatile int mMode = 1;
    private final Queue<Bitmap> mRecycleBitmapQueue = new ArrayBlockingQueue(2, true);
    private volatile boolean mSupportInBitmap = true;
    private ISurfaceTextureStateHelper mSurfaceTextureStateHelper;
    private final View mView;

    public interface CustomerDrawBitmap {
        void drawBitmap(Canvas canvas, Bitmap bitmap, Matrix matrix);
    }

    public interface FrameAnimationListener {
        void onFinish();

        void onInterrupt();

        void onRepeat();

        void onStart();
    }

    /* access modifiers changed from: package-private */
    public interface ISurfaceTextureStateHelper {
        SurfaceTextureState getState();
    }

    /* access modifiers changed from: package-private */
    public enum SurfaceTextureState {
        Available,
        Destroyed,
        Unknown
    }

    public MiuiGxzwFrameAnimation(TextureView textureView, ISurfaceTextureStateHelper iSurfaceTextureStateHelper) {
        this.mView = textureView;
        this.mSurfaceTextureStateHelper = iSurfaceTextureStateHelper;
        textureView.setOpaque(false);
        this.mContext = textureView.getContext();
        HandlerThread handlerThread = new HandlerThread("FrameAnimation Draw Thread");
        this.mDrawThread = handlerThread;
        handlerThread.start();
        this.mDrawHandler = new Handler(this.mDrawThread.getLooper());
    }

    public void setFrameInterval(int i) {
        if (i >= 0) {
            this.mFrameInterval = i;
            return;
        }
        throw new UnsupportedOperationException("frameInterval < 0");
    }

    public void setMode(int i) {
        if (i == 1 || i == 2) {
            this.mMode = i;
            return;
        }
        throw new UnsupportedOperationException("wrong mode: " + i);
    }

    public void startAnimation(int[] iArr, int i, int i2, int i3, FrameAnimationListener frameAnimationListener, CustomerDrawBitmap customerDrawBitmap, int i4, int i5) {
        this.mLastDrawAnim = false;
        stopAnimation();
        DrawRunnable drawRunnable = new DrawRunnable(iArr, i, i2, i3, frameAnimationListener, customerDrawBitmap, i4, i5);
        this.mDrawRunnable = drawRunnable;
        this.mDrawHandler.post(drawRunnable);
    }

    public void stopAnimation() {
        DrawRunnable drawRunnable = this.mDrawRunnable;
        if (drawRunnable != null) {
            this.mDrawHandler.removeCallbacks(drawRunnable);
            Slog.d("MiuiGxzwFrameAnimation", "stopAnimation mDrawing=" + this.mDrawRunnable.getDrawing());
            this.mDrawRunnable.stopDraw();
        }
        this.mDrawRunnable = null;
    }

    public boolean isAniming() {
        DrawRunnable drawRunnable = this.mDrawRunnable;
        return drawRunnable != null && drawRunnable.getDrawing();
    }

    public int getCurrentPosition() {
        if (isAniming()) {
            return this.mDrawRunnable.getCurrentPosition();
        }
        return 0;
    }

    public void draw(final int i, final boolean z, final float f) {
        Log.i("MiuiGxzwFrameAnimation", "draw: res = " + i + ", anim = " + z + ", scale = " + f);
        stopAnimation();
        boolean z2 = this.mLastDrawAnim;
        this.mLastDrawAnim = z;
        if ((!z2 && z) || !z) {
            this.mRecycleBitmapQueue.clear();
        }
        this.mDrawHandler.post(new Runnable() {
            /* class com.android.keyguard.fod.MiuiGxzwFrameAnimation.AnonymousClass1 */

            public void run() {
                Bitmap decodeBitmap = MiuiGxzwFrameAnimation.this.decodeBitmap(i);
                if (decodeBitmap != null) {
                    MiuiGxzwFrameAnimation.this.drawBitmap(decodeBitmap, f);
                    if (z) {
                        MiuiGxzwFrameAnimation.this.mRecycleBitmapQueue.offer(decodeBitmap);
                    }
                }
            }
        });
    }

    public void clean() {
        Log.i("MiuiGxzwFrameAnimation", "clean");
        stopAnimation();
        this.mDrawHandler.post(new Runnable() {
            /* class com.android.keyguard.fod.MiuiGxzwFrameAnimation.AnonymousClass2 */

            public void run() {
                MiuiGxzwFrameAnimation.this.clearSurface();
            }
        });
    }

    private Matrix configureDrawMatrix(Bitmap bitmap, float f) {
        Matrix matrix = new Matrix();
        int width = bitmap.getWidth();
        int width2 = this.mView.getWidth();
        int height = bitmap.getHeight();
        int height2 = this.mView.getHeight();
        matrix.setScale(f, f);
        matrix.postTranslate((float) Math.round((((float) width2) - (((float) width) * f)) * 0.5f), (float) Math.round((((float) height2) - (((float) height) * f)) * 0.5f));
        return matrix;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void drawBitmap(Bitmap bitmap, float f) {
        drawBitmap(bitmap, null, f, null, 0, 0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void drawBitmap(Bitmap bitmap, Bitmap bitmap2, float f, CustomerDrawBitmap customerDrawBitmap, int i, int i2) {
        Canvas lockCanvas = lockCanvas();
        if (lockCanvas == null || bitmap == null) {
            Log.i("MiuiGxzwFrameAnimation", "drawBitmap: bitmap or canvas is null");
            return;
        }
        try {
            if (canCanvasDraw()) {
                Matrix configureDrawMatrix = configureDrawMatrix(bitmap, f);
                float f2 = (float) i;
                float f3 = (float) i2;
                configureDrawMatrix.postTranslate(f2, f3);
                lockCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                if (bitmap2 != null) {
                    Matrix configureDrawMatrix2 = configureDrawMatrix(bitmap2, f);
                    configureDrawMatrix2.postTranslate(f2, f3);
                    lockCanvas.drawBitmap(bitmap2, configureDrawMatrix2, null);
                }
                if (customerDrawBitmap == null) {
                    lockCanvas.drawBitmap(bitmap, configureDrawMatrix, null);
                } else {
                    customerDrawBitmap.drawBitmap(lockCanvas, bitmap, configureDrawMatrix);
                }
                unlockCanvasAndPostSafely(lockCanvas);
            }
        } finally {
            unlockCanvasAndPostSafely(lockCanvas);
        }
    }

    private Canvas lockCanvas() {
        View view = this.mView;
        if (view != null && (view instanceof SurfaceView)) {
            return ((SurfaceView) view).getHolder().lockCanvas();
        }
        View view2 = this.mView;
        if (view2 == null || !(view2 instanceof TextureView)) {
            return null;
        }
        TextureView textureView = (TextureView) view2;
        if (textureView.isAvailable()) {
            return textureView.lockCanvas();
        }
        return null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearSurface() {
        Canvas lockCanvas = lockCanvas();
        if (lockCanvas != null) {
            try {
                if (canCanvasDraw()) {
                    lockCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                    unlockCanvasAndPostSafely(lockCanvas);
                }
            } finally {
                unlockCanvasAndPostSafely(lockCanvas);
            }
        }
    }

    private boolean canCanvasDraw() {
        ISurfaceTextureStateHelper iSurfaceTextureStateHelper = this.mSurfaceTextureStateHelper;
        return iSurfaceTextureStateHelper == null || iSurfaceTextureStateHelper.getState() == SurfaceTextureState.Available;
    }

    private void unlockCanvasAndPostSafely(Canvas canvas) {
        View view = this.mView;
        if (view == null || !(view instanceof SurfaceView)) {
            View view2 = this.mView;
            if (view2 != null && (view2 instanceof TextureView)) {
                TextureView textureView = (TextureView) view2;
                if (textureView.isAvailable()) {
                    textureView.unlockCanvasAndPost(canvas);
                    return;
                }
                return;
            }
            return;
        }
        SurfaceHolder holder = ((SurfaceView) view).getHolder();
        Surface surface = holder.getSurface();
        if (surface != null && surface.isValid()) {
            holder.unlockCanvasAndPost(canvas);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Bitmap decodeBitmap(int i) {
        Bitmap poll = (!this.mSupportInBitmap || this.mRecycleBitmapQueue.size() < 2) ? null : this.mRecycleBitmapQueue.poll();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inSampleSize = 1;
        options.inBitmap = poll;
        try {
            return BitmapFactory.decodeResource(this.mContext.getResources(), i, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /* access modifiers changed from: private */
    public class DrawRunnable implements Runnable {
        private final int[] mAnimRes;
        private final int mBackgroundFrame;
        private final int mBackgroundRes;
        private volatile int mCurrentPosition;
        private final CustomerDrawBitmap mCustomerDrawBitmap;
        private boolean mDrawing;
        private final FrameAnimationListener mFrameAnimationListener;
        private final int mTranslateX;
        private final int mTranslateY;

        private DrawRunnable(int[] iArr, int i, int i2, int i3, FrameAnimationListener frameAnimationListener, CustomerDrawBitmap customerDrawBitmap, int i4, int i5) {
            this.mDrawing = false;
            this.mAnimRes = iArr;
            this.mCurrentPosition = i % iArr.length;
            this.mBackgroundRes = i2;
            this.mBackgroundFrame = i3;
            this.mFrameAnimationListener = frameAnimationListener;
            this.mCustomerDrawBitmap = customerDrawBitmap;
            this.mTranslateX = i4;
            this.mTranslateY = i5;
        }

        public synchronized void setDrawing(boolean z) {
            this.mDrawing = z;
        }

        public synchronized boolean getDrawing() {
            return this.mDrawing;
        }

        public int getCurrentPosition() {
            return this.mCurrentPosition;
        }

        public void stopDraw() {
            if (this.mDrawing) {
                MiuiGxzwFrameAnimation.this.mDrawThread.interrupt();
                MiuiGxzwFrameAnimation.this.mRecycleBitmapQueue.clear();
            }
            setDrawing(false);
        }

        public void run() {
            boolean z = true;
            setDrawing(true);
            notifyStart();
            int[] iArr = this.mAnimRes;
            if (iArr == null || iArr.length == 0) {
                notifyFinish();
                setDrawing(false);
                return;
            }
            MiuiGxzwFrameAnimation.this.mRecycleBitmapQueue.clear();
            int i = this.mBackgroundRes;
            Bitmap decodeBitmap = i == 0 ? null : MiuiGxzwFrameAnimation.this.decodeBitmap(i);
            long currentTimeMillis = System.currentTimeMillis();
            int i2 = 0;
            while (true) {
                if (!getDrawing()) {
                    break;
                }
                long currentTimeMillis2 = System.currentTimeMillis();
                this.mCurrentPosition = (int) ((currentTimeMillis2 - currentTimeMillis) / ((long) MiuiGxzwFrameAnimation.this.mFrameInterval));
                int i3 = this.mCurrentPosition;
                int[] iArr2 = this.mAnimRes;
                if (i3 >= iArr2.length) {
                    this.mCurrentPosition = iArr2.length - 1;
                } else if (this.mCurrentPosition < 0) {
                    this.mCurrentPosition = 0;
                }
                int i4 = this.mAnimRes[this.mCurrentPosition];
                if (i4 == 0 || MiuiGxzwFrameAnimation.this.alpha < 0.01f) {
                    MiuiGxzwFrameAnimation.this.clearSurface();
                } else {
                    Bitmap decodeBitmap2 = MiuiGxzwFrameAnimation.this.decodeBitmap(i4);
                    if (decodeBitmap2 == null) {
                        stopDraw();
                        break;
                    }
                    MiuiGxzwFrameAnimation miuiGxzwFrameAnimation = MiuiGxzwFrameAnimation.this;
                    int i5 = this.mBackgroundFrame;
                    miuiGxzwFrameAnimation.drawBitmap(decodeBitmap2, (i2 < i5 || i5 <= 0) ? decodeBitmap : null, 1.0f, this.mCustomerDrawBitmap, this.mTranslateX, this.mTranslateY);
                    MiuiGxzwFrameAnimation.this.mRecycleBitmapQueue.offer(decodeBitmap2);
                }
                i2++;
                if (this.mCurrentPosition == this.mAnimRes.length - 1) {
                    if (MiuiGxzwFrameAnimation.this.mMode == 1) {
                        MiuiGxzwFrameAnimation.this.mRecycleBitmapQueue.clear();
                        z = false;
                        break;
                    } else if (MiuiGxzwFrameAnimation.this.mMode == 2) {
                        nitifyRepeat();
                    }
                }
                try {
                    long currentTimeMillis3 = System.currentTimeMillis() - currentTimeMillis2;
                    if (((long) MiuiGxzwFrameAnimation.this.mFrameInterval) - currentTimeMillis3 > 0) {
                        Thread.sleep(((long) MiuiGxzwFrameAnimation.this.mFrameInterval) - currentTimeMillis3);
                    }
                } catch (InterruptedException unused) {
                }
                if (this.mCurrentPosition == this.mAnimRes.length - 1) {
                    currentTimeMillis = System.currentTimeMillis();
                }
            }
            setDrawing(false);
            if (z) {
                notifyInterrupt();
            } else {
                notifyFinish();
            }
        }

        private void notifyStart() {
            if (this.mFrameAnimationListener != null) {
                MiuiGxzwFrameAnimation.this.mHandler.post(new Runnable() {
                    /* class com.android.keyguard.fod.MiuiGxzwFrameAnimation.DrawRunnable.AnonymousClass1 */

                    public void run() {
                        DrawRunnable.this.mFrameAnimationListener.onStart();
                    }
                });
            }
        }

        private void notifyInterrupt() {
            if (this.mFrameAnimationListener != null) {
                MiuiGxzwFrameAnimation.this.mHandler.post(new Runnable() {
                    /* class com.android.keyguard.fod.MiuiGxzwFrameAnimation.DrawRunnable.AnonymousClass2 */

                    public void run() {
                        DrawRunnable.this.mFrameAnimationListener.onInterrupt();
                    }
                });
            }
        }

        private void notifyFinish() {
            if (this.mFrameAnimationListener != null) {
                MiuiGxzwFrameAnimation.this.mHandler.post(new Runnable() {
                    /* class com.android.keyguard.fod.MiuiGxzwFrameAnimation.DrawRunnable.AnonymousClass3 */

                    public void run() {
                        DrawRunnable.this.mFrameAnimationListener.onFinish();
                    }
                });
            }
        }

        private void nitifyRepeat() {
            if (this.mFrameAnimationListener != null) {
                MiuiGxzwFrameAnimation.this.mHandler.post(new Runnable() {
                    /* class com.android.keyguard.fod.MiuiGxzwFrameAnimation.DrawRunnable.AnonymousClass4 */

                    public void run() {
                        DrawRunnable.this.mFrameAnimationListener.onRepeat();
                    }
                });
            }
        }
    }
}

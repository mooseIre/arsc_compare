package com.android.keyguard.charge.lollipop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.TextureView;
import com.android.keyguard.charge.lollipop.LollipopWirelessAnimationView;
import com.android.systemui.C0013R$drawable;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class LollipopWirelessChargeCircleDrawer implements LollipopWirelessAnimationView.AnimationDrawer {
    public static final int[] WIRELESS_CIRCLE_RES_ARRAY = {C0013R$drawable.wireless_rapid_charge_0, C0013R$drawable.wireless_rapid_charge_1, C0013R$drawable.wireless_rapid_charge_2, C0013R$drawable.wireless_rapid_charge_3, C0013R$drawable.wireless_rapid_charge_4, C0013R$drawable.wireless_rapid_charge_5, C0013R$drawable.wireless_rapid_charge_6, C0013R$drawable.wireless_rapid_charge_7, C0013R$drawable.wireless_rapid_charge_8, C0013R$drawable.wireless_rapid_charge_9, C0013R$drawable.wireless_rapid_charge_10, C0013R$drawable.wireless_rapid_charge_11, C0013R$drawable.wireless_rapid_charge_12, C0013R$drawable.wireless_rapid_charge_13, C0013R$drawable.wireless_rapid_charge_14, C0013R$drawable.wireless_rapid_charge_15, C0013R$drawable.wireless_rapid_charge_16, C0013R$drawable.wireless_rapid_charge_17, C0013R$drawable.wireless_rapid_charge_18, C0013R$drawable.wireless_rapid_charge_19, C0013R$drawable.wireless_rapid_charge_20, C0013R$drawable.wireless_rapid_charge_21, C0013R$drawable.wireless_rapid_charge_22, C0013R$drawable.wireless_rapid_charge_23};
    private final BlockingQueue<Bitmap> mBitmapQueue = new ArrayBlockingQueue(4);
    private Context mContext;
    private Handler mDecodeHandler;
    private DecodeTask mDecodeTask;
    private HandlerThread mDecodeThread;
    private int mFrameInterval;
    private final Object mHandlerLock = new Object();
    private long mLastFrameTime = -1;
    private Matrix mMatrix;
    private Paint mPaint;
    private final Queue<Bitmap> mRecycleBitmapQueue = new ArrayBlockingQueue(2, true);

    LollipopWirelessChargeCircleDrawer(Context context) {
        this.mContext = context;
        this.mFrameInterval = 31;
        this.mPaint = new Paint();
        this.mMatrix = new Matrix();
    }

    public void onAnimationDraw(TextureView textureView, long j) {
        if (this.mBitmapQueue.peek() != null) {
            long j2 = this.mLastFrameTime;
            if (j2 == -1) {
                dequeueBitmapInfoAndDraw(textureView);
                this.mLastFrameTime = j;
            } else if (j - j2 >= ((long) (this.mFrameInterval * 1000000))) {
                dequeueBitmapInfoAndDraw(textureView);
                this.mLastFrameTime = j;
            }
        }
    }

    private void dequeueBitmapInfoAndDraw(TextureView textureView) {
        Bitmap poll = this.mBitmapQueue.poll();
        drawBitmap(textureView, poll, this.mPaint, this.mMatrix);
        try {
            this.mRecycleBitmapQueue.offer(poll);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawBitmap(TextureView textureView, Bitmap bitmap, Paint paint, Matrix matrix) {
        Canvas lockCanvas;
        if (textureView != null && bitmap != null && !bitmap.isRecycled() && (lockCanvas = textureView.lockCanvas()) != null) {
            try {
                matrix.reset();
                int width = bitmap.getWidth();
                int width2 = lockCanvas.getWidth();
                int height = bitmap.getHeight();
                int height2 = lockCanvas.getHeight();
                float f = (float) width2;
                float f2 = (f * 1.0f) / ((float) width);
                float f3 = (float) height2;
                float min = Math.min(f2, (1.0f * f3) / ((float) height));
                matrix.postTranslate((float) Math.round(((float) (width2 - width)) * 0.5f), (float) Math.round(((float) (height2 - height)) * 0.5f));
                matrix.postScale(min, min, f / 2.0f, f3 / 2.0f);
                lockCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
                lockCanvas.drawBitmap(bitmap, matrix, paint);
            } finally {
                if (textureView.isAvailable()) {
                    textureView.unlockCanvasAndPost(lockCanvas);
                }
            }
        }
    }

    public void startAnimation() {
        this.mLastFrameTime = -1;
        this.mBitmapQueue.clear();
        prepareDecodeThread();
        DecodeTask decodeTask = new DecodeTask(WIRELESS_CIRCLE_RES_ARRAY, 0);
        this.mDecodeTask = decodeTask;
        decodeTask.setDecoding(true);
        this.mDecodeHandler.post(this.mDecodeTask);
    }

    private void prepareDecodeThread() {
        if (this.mDecodeThread == null) {
            HandlerThread handlerThread = new HandlerThread("charge_animation_decode");
            this.mDecodeThread = handlerThread;
            handlerThread.start();
        }
        synchronized (this.mHandlerLock) {
            if (this.mDecodeHandler == null) {
                this.mDecodeHandler = new Handler(this.mDecodeThread.getLooper());
            }
        }
    }

    public void release() {
        DecodeTask decodeTask = this.mDecodeTask;
        if (decodeTask != null) {
            decodeTask.setDecoding(false);
            this.mDecodeTask = null;
        }
        synchronized (this.mHandlerLock) {
            if (this.mDecodeHandler != null) {
                this.mDecodeHandler.removeCallbacksAndMessages(null);
                this.mDecodeHandler = null;
            }
        }
        HandlerThread handlerThread = this.mDecodeThread;
        if (handlerThread != null) {
            handlerThread.quit();
            this.mDecodeThread = null;
        }
        this.mBitmapQueue.clear();
        setAnimationListener(null);
    }

    private class DecodeTask implements Runnable {
        private final int[] mAnimRes;
        private volatile int mCurrentPosition;
        private volatile boolean mDecoding;

        private DecodeTask(int[] iArr, int i) {
            this.mDecoding = false;
            this.mAnimRes = iArr;
            this.mCurrentPosition = i % iArr.length;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setDecoding(boolean z) {
            this.mDecoding = z;
        }

        private boolean shouldFinish() {
            int[] iArr = this.mAnimRes;
            return iArr == null || iArr.length == 0 || !this.mDecoding;
        }

        public void run() {
            if (shouldFinish()) {
                setDecoding(false);
                return;
            }
            try {
                LollipopWirelessChargeCircleDrawer.this.mBitmapQueue.put(LollipopWirelessChargeCircleDrawer.this.decodeBitmap(this.mAnimRes[this.mCurrentPosition]));
                this.mCurrentPosition++;
                if (this.mCurrentPosition >= this.mAnimRes.length) {
                    this.mCurrentPosition = 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (shouldFinish()) {
                setDecoding(false);
                return;
            }
            synchronized (LollipopWirelessChargeCircleDrawer.this.mHandlerLock) {
                if (LollipopWirelessChargeCircleDrawer.this.mDecodeHandler != null) {
                    LollipopWirelessChargeCircleDrawer.this.mDecodeHandler.post(this);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Bitmap decodeBitmap(int i) {
        try {
            Bitmap poll = this.mRecycleBitmapQueue.size() >= 2 ? this.mRecycleBitmapQueue.poll() : null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inSampleSize = 1;
            options.inBitmap = poll;
            return BitmapFactory.decodeResource(this.mContext.getResources(), i, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

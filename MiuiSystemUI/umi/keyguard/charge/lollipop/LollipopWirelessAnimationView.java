package com.android.keyguard.charge.lollipop;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.TextureView;
import android.view.WindowManager;

public class LollipopWirelessAnimationView extends TextureView implements TextureView.SurfaceTextureListener {
    private volatile boolean mAnimationRunning;
    private LollipopWirelessChargeCircleDrawer mCircleDrawer;
    private Context mContext;
    private int mDrawableHeight;
    private int mDrawableWidth;
    private Choreographer.FrameCallback mFrameCallback;
    private boolean mPendingStartAnimation;
    private Point mScreenSize;
    private boolean mSurfaceAvailable;
    private int mViewHeight;
    private int mViewWidth;
    private WindowManager mWindowManager;

    public interface AnimationDrawer {

        public interface AnimationStateListener {
        }

        default void setAnimationListener(AnimationStateListener animationStateListener) {
        }
    }

    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return true;
    }

    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
    }

    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    public LollipopWirelessAnimationView(Context context) {
        this(context, null);
    }

    public LollipopWirelessAnimationView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public LollipopWirelessAnimationView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mFrameCallback = new Choreographer.FrameCallback() {
            /* class com.android.keyguard.charge.lollipop.LollipopWirelessAnimationView.AnonymousClass1 */

            public void doFrame(long j) {
                if (LollipopWirelessAnimationView.this.mAnimationRunning) {
                    LollipopWirelessAnimationView.this.dispatchDraw(j);
                    Choreographer.getInstance().postFrameCallback(this);
                }
            }
        };
        init(context);
    }

    private void init(Context context) {
        setOpaque(false);
        this.mContext = context;
        this.mSurfaceAvailable = false;
        this.mAnimationRunning = false;
        setSurfaceTextureListener(this);
        Drawable drawable = getResources().getDrawable(LollipopWirelessChargeCircleDrawer.WIRELESS_CIRCLE_RES_ARRAY[0]);
        this.mDrawableWidth = drawable.getIntrinsicWidth();
        this.mDrawableHeight = drawable.getIntrinsicHeight();
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mScreenSize = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mScreenSize);
        updateSizeForScreenSizeChange();
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkScreenSize();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        checkScreenSize();
    }

    private void checkScreenSize() {
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        if (!this.mScreenSize.equals(point.x, point.y)) {
            this.mScreenSize.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            requestLayout();
        }
    }

    private void updateSizeForScreenSizeChange() {
        Point point = this.mScreenSize;
        float min = (((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f;
        this.mViewWidth = (int) (((float) this.mDrawableWidth) * min);
        this.mViewHeight = (int) (min * ((float) this.mDrawableHeight));
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        setMeasuredDimension(this.mViewWidth, this.mViewHeight);
    }

    public void startAnimation() {
        if (!this.mAnimationRunning) {
            if (this.mSurfaceAvailable) {
                this.mAnimationRunning = true;
                LollipopWirelessChargeCircleDrawer lollipopWirelessChargeCircleDrawer = new LollipopWirelessChargeCircleDrawer(this.mContext);
                this.mCircleDrawer = lollipopWirelessChargeCircleDrawer;
                lollipopWirelessChargeCircleDrawer.startAnimation();
                Choreographer.getInstance().postFrameCallback(this.mFrameCallback);
                return;
            }
            this.mPendingStartAnimation = true;
        }
    }

    public void stopAnimation() {
        this.mAnimationRunning = false;
        this.mPendingStartAnimation = false;
        Choreographer.getInstance().removeFrameCallback(this.mFrameCallback);
        LollipopWirelessChargeCircleDrawer lollipopWirelessChargeCircleDrawer = this.mCircleDrawer;
        if (lollipopWirelessChargeCircleDrawer != null) {
            lollipopWirelessChargeCircleDrawer.release();
            this.mCircleDrawer = null;
        }
    }

    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
        this.mSurfaceAvailable = true;
        if (this.mPendingStartAnimation) {
            startAnimation();
            this.mPendingStartAnimation = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchDraw(long j) {
        LollipopWirelessChargeCircleDrawer lollipopWirelessChargeCircleDrawer;
        if (this.mSurfaceAvailable && (lollipopWirelessChargeCircleDrawer = this.mCircleDrawer) != null) {
            lollipopWirelessChargeCircleDrawer.onAnimationDraw(this, j);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}

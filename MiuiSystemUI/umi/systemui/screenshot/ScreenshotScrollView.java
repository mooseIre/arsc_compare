package com.android.systemui.screenshot;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;
import com.android.systemui.Constants;
import com.android.systemui.plugins.R;
import java.util.List;
import miui.util.LongScreenshotUtils;

public class ScreenshotScrollView extends View {
    /* access modifiers changed from: private */
    public float mAnimatableOffsetY;
    private AnimatingCallback mAnimatingCallback;
    Runnable mAnimatingStepRunnable;
    /* access modifiers changed from: private */
    public int mAnimatorStep;
    private Bitmap mBottomPart;
    private long mFirstClickTime;
    /* access modifiers changed from: private */
    public boolean mIsAnimatingStoped;
    private boolean mIsBuildingLongScreenshot;
    private boolean mIsManuTaking;
    private boolean mIsTakingLongScreenshot;
    private float mLastTouchY;
    /* access modifiers changed from: private */
    public LongScreenshotUtils.LongBitmapDrawable mLongBitmapDrawable;
    private float mMaxOffsetY;
    private float mMinOffsetY;
    private int mMinTotalHeight;
    /* access modifiers changed from: private */
    public float mOffsetY;
    private Scroller mScroller;
    /* access modifiers changed from: private */
    public float mShowBig;
    private ValueAnimator mShowBigAnimator;
    private Rect mShowRect;
    private int mShowedPageCount;
    private Bitmap mSingleBitmap;
    private int mTotalHeight;
    private int mUiState;
    private VelocityTracker mVelocityTracker;

    public interface AnimatingCallback {
        void doubleClickEventReaction(boolean z);

        void onShowedPageCountChanged(int i);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public ScreenshotScrollView(Context context) {
        this(context, (AttributeSet) null);
    }

    public ScreenshotScrollView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ScreenshotScrollView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mVelocityTracker = VelocityTracker.obtain();
        this.mUiState = 0;
        this.mShowRect = new Rect();
        this.mAnimatingStepRunnable = new Runnable() {
            public void run() {
                if (ScreenshotScrollView.this.mIsAnimatingStoped) {
                    Log.d("ScreenshotScrollView", "mIsAnimatingStoped, but also get here.");
                } else if (ScreenshotScrollView.this.mLongBitmapDrawable == null) {
                    Log.d("ScreenshotScrollView", "bitmap is null.");
                } else {
                    ScreenshotScrollView screenshotScrollView = ScreenshotScrollView.this;
                    screenshotScrollView.doAnimatingStep(screenshotScrollView.mAnimatorStep);
                    ScreenshotScrollView screenshotScrollView2 = ScreenshotScrollView.this;
                    screenshotScrollView2.post(screenshotScrollView2.mAnimatingStepRunnable);
                }
            }
        };
        this.mScroller = new Scroller(context);
        this.mAnimatorStep = (int) ((getResources().getDisplayMetrics().density * 2.0f) + 0.5f);
        this.mShowBigAnimator = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        this.mShowBigAnimator.setDuration(200);
        this.mShowBigAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float unused = ScreenshotScrollView.this.mShowBig = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                ScreenshotScrollView screenshotScrollView = ScreenshotScrollView.this;
                float unused2 = screenshotScrollView.mAnimatableOffsetY = screenshotScrollView.mOffsetY * valueAnimator.getAnimatedFraction();
                ScreenshotScrollView.this.invalidate();
            }
        });
    }

    public boolean performClick(MotionEvent motionEvent) {
        if (System.currentTimeMillis() - this.mFirstClickTime <= ((long) ViewConfiguration.getDoubleTapTimeout())) {
            onDoubleClick(motionEvent);
            return true;
        }
        this.mFirstClickTime = System.currentTimeMillis();
        return true;
    }

    private void onDoubleClick(MotionEvent motionEvent) {
        if (this.mShowBigAnimator.isRunning() || ((Float) this.mShowBigAnimator.getAnimatedValue()).floatValue() != 0.0f) {
            this.mShowBigAnimator.reverse();
            AnimatingCallback animatingCallback = this.mAnimatingCallback;
            if (animatingCallback != null) {
                animatingCallback.doubleClickEventReaction(false);
            }
        } else {
            float max = Math.max(Math.min(1.0f - ((motionEvent.getY() - ((float) getPaddingTop())) / ((float) getHeightInner())), 1.0f), 0.0f);
            float f = this.mMaxOffsetY;
            float f2 = this.mMinOffsetY;
            this.mOffsetY = (max * (f - f2)) + f2;
            this.mShowBigAnimator.start();
            AnimatingCallback animatingCallback2 = this.mAnimatingCallback;
            if (animatingCallback2 != null) {
                animatingCallback2.doubleClickEventReaction(true);
            }
        }
        StatHelper.recordCountEvent(this.mContext, "double_click", this.mLongBitmapDrawable == null ? "normal" : "longscreenshot");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:8:0x001e, code lost:
        if (r1 != 3) goto L_0x0105;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r18) {
        /*
            r17 = this;
            r0 = r17
            boolean r1 = r0.mIsBuildingLongScreenshot
            r2 = 1
            if (r1 == 0) goto L_0x0008
            return r2
        L_0x0008:
            android.view.VelocityTracker r1 = r0.mVelocityTracker
            r3 = r18
            r1.addMovement(r3)
            int r1 = r18.getAction()
            r4 = 0
            r5 = 2
            if (r1 == 0) goto L_0x00dd
            r6 = 1065353216(0x3f800000, float:1.0)
            r7 = 3
            if (r1 == r2) goto L_0x006e
            if (r1 == r5) goto L_0x0022
            if (r1 == r7) goto L_0x006e
            goto L_0x0105
        L_0x0022:
            float r1 = r18.getY()
            float r4 = r0.mLastTouchY
            float r1 = r1 - r4
            boolean r4 = r0.mIsTakingLongScreenshot
            if (r4 == 0) goto L_0x003d
            r4 = 1056964608(0x3f000000, float:0.5)
            float r1 = r1 + r4
            int r1 = (int) r1
            int r1 = -r1
            r0.doAnimatingStep(r1)
            float r1 = r18.getY()
            r0.mLastTouchY = r1
            goto L_0x0105
        L_0x003d:
            float r4 = r0.mShowBig
            int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r4 != 0) goto L_0x0105
            int r4 = r0.mUiState
            if (r4 == r5) goto L_0x005c
            float r4 = java.lang.Math.abs(r1)
            android.content.Context r6 = r0.mContext
            android.view.ViewConfiguration r6 = android.view.ViewConfiguration.get(r6)
            int r6 = r6.getScaledDoubleTapTouchSlop()
            float r6 = (float) r6
            int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r4 < 0) goto L_0x005c
            r0.mUiState = r5
        L_0x005c:
            int r4 = r0.mUiState
            if (r4 != r5) goto L_0x0105
            float r4 = r0.mOffsetY
            float r4 = r4 + r1
            r0.setOffsetY(r4)
            float r1 = r18.getY()
            r0.mLastTouchY = r1
            goto L_0x0105
        L_0x006e:
            int r1 = r0.mUiState
            if (r1 != r5) goto L_0x00d2
            android.view.VelocityTracker r1 = r0.mVelocityTracker
            r3 = 1000(0x3e8, float:1.401E-42)
            r1.computeCurrentVelocity(r3)
            boolean r1 = r0.mIsTakingLongScreenshot
            if (r1 == 0) goto L_0x00a6
            android.widget.Scroller r1 = r0.mScroller
            float r3 = android.view.ViewConfiguration.getScrollFriction()
            r4 = 1073741824(0x40000000, float:2.0)
            float r3 = r3 * r4
            r1.setFriction(r3)
            android.widget.Scroller r8 = r0.mScroller
            r9 = 0
            int r10 = r0.mTotalHeight
            r11 = 0
            r1 = 7000(0x1b58, float:9.809E-42)
            int r1 = r0.getVelocityY(r1)
            int r12 = -r1
            r13 = 0
            r14 = 0
            r15 = -2147483648(0xffffffff80000000, float:-0.0)
            r16 = 2147483647(0x7fffffff, float:NaN)
            r8.fling(r9, r10, r11, r12, r13, r14, r15, r16)
            r0.mUiState = r7
            r17.invalidate()
            goto L_0x00d7
        L_0x00a6:
            float r1 = r0.mShowBig
            int r1 = (r1 > r6 ? 1 : (r1 == r6 ? 0 : -1))
            if (r1 != 0) goto L_0x00d7
            android.widget.Scroller r1 = r0.mScroller
            float r3 = android.view.ViewConfiguration.getScrollFriction()
            r1.setFriction(r3)
            android.widget.Scroller r8 = r0.mScroller
            r9 = 0
            float r1 = r0.mOffsetY
            int r10 = (int) r1
            r11 = 0
            r1 = 10000(0x2710, float:1.4013E-41)
            int r12 = r0.getVelocityY(r1)
            r13 = 0
            r14 = 0
            r15 = -2147483648(0xffffffff80000000, float:-0.0)
            r16 = 2147483647(0x7fffffff, float:NaN)
            r8.fling(r9, r10, r11, r12, r13, r14, r15, r16)
            r0.mUiState = r7
            r17.invalidate()
            goto L_0x00d7
        L_0x00d2:
            r17.performClick(r18)
            r0.mUiState = r4
        L_0x00d7:
            android.view.VelocityTracker r0 = r0.mVelocityTracker
            r0.clear()
            goto L_0x0105
        L_0x00dd:
            float r1 = r18.getY()
            r0.mLastTouchY = r1
            int r1 = r0.mUiState
            if (r1 != r5) goto L_0x00ec
            android.widget.Scroller r1 = r0.mScroller
            r1.forceFinished(r2)
        L_0x00ec:
            boolean r1 = r0.mIsTakingLongScreenshot
            if (r1 == 0) goto L_0x0103
            boolean r1 = r0.mIsManuTaking
            if (r1 != 0) goto L_0x00fb
            android.content.Context r1 = r0.mContext
            java.lang.String r3 = "longscreenshot_manual"
            com.android.systemui.screenshot.StatHelper.recordCountEvent(r1, r3)
        L_0x00fb:
            r0.stopAnimating(r4)
            r0.mIsManuTaking = r2
            r0.mUiState = r5
            goto L_0x0105
        L_0x0103:
            r0.mUiState = r2
        L_0x0105:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.screenshot.ScreenshotScrollView.onTouchEvent(android.view.MotionEvent):boolean");
    }

    private void setOffsetY(float f) {
        float max = Math.max(Math.min(f, this.mMaxOffsetY), this.mMinOffsetY);
        this.mOffsetY = max;
        this.mAnimatableOffsetY = max;
        invalidate();
    }

    private int getVelocityY(int i) {
        return Math.min(i, Math.max(-i, (int) this.mVelocityTracker.getYVelocity()));
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0038, code lost:
        r0 = r7.mLongBitmapDrawable;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDraw(android.graphics.Canvas r8) {
        /*
            r7 = this;
            int r0 = r7.mUiState
            r1 = 0
            r2 = 1065353216(0x3f800000, float:1.0)
            r3 = 3
            if (r0 != r3) goto L_0x0034
            android.widget.Scroller r0 = r7.mScroller
            boolean r0 = r0.computeScrollOffset()
            if (r0 == 0) goto L_0x0032
            boolean r0 = r7.mIsTakingLongScreenshot
            if (r0 == 0) goto L_0x0021
            android.widget.Scroller r0 = r7.mScroller
            int r0 = r0.getCurrY()
            int r3 = r7.mTotalHeight
            int r0 = r0 - r3
            r7.doAnimatingStep(r0)
            goto L_0x0034
        L_0x0021:
            float r0 = r7.mShowBig
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 != 0) goto L_0x0034
            android.widget.Scroller r0 = r7.mScroller
            int r0 = r0.getCurrY()
            float r0 = (float) r0
            r7.setOffsetY(r0)
            goto L_0x0034
        L_0x0032:
            r7.mUiState = r1
        L_0x0034:
            android.graphics.Bitmap r0 = r7.mSingleBitmap
            if (r0 == 0) goto L_0x004b
            miui.util.LongScreenshotUtils$LongBitmapDrawable r0 = r7.mLongBitmapDrawable
            if (r0 == 0) goto L_0x0049
            android.graphics.Bitmap[] r0 = r0.getBitmaps()
            int r0 = r0.length
            if (r0 == 0) goto L_0x0049
            float r0 = r7.mShowBig
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 >= 0) goto L_0x004b
        L_0x0049:
            r0 = 1
            goto L_0x004c
        L_0x004b:
            r0 = r1
        L_0x004c:
            float r3 = r7.calcScale(r0)
            if (r0 == 0) goto L_0x0073
            float r0 = r7.mAnimatableOffsetY
            int r1 = r7.getPaddingTop()
            float r1 = (float) r1
            float r4 = r7.mShowBig
            float r2 = r2 - r4
            float r1 = r1 * r2
            float r0 = r0 + r1
            int r0 = (int) r0
            float r0 = (float) r0
            r1 = 0
            r8.translate(r1, r0)
            int r0 = r7.getWidth()
            float r0 = (float) r0
            r2 = 1073741824(0x40000000, float:2.0)
            float r0 = r0 / r2
            r8.scale(r3, r3, r0, r1)
            r7.drawSingleBitmap(r8, r3)
            goto L_0x00d0
        L_0x0073:
            miui.util.LongScreenshotUtils$LongBitmapDrawable r0 = r7.mLongBitmapDrawable
            int r0 = r0.getIntrinsicWidth()
            float r0 = (float) r0
            float r0 = r0 * r3
            int r0 = (int) r0
            int r4 = r7.getPaddingLeft()
            int r5 = r7.getWidthInner()
            int r5 = r5 - r0
            int r5 = r5 / 2
            int r4 = r4 + r5
            boolean r5 = r7.mIsTakingLongScreenshot
            if (r5 == 0) goto L_0x009f
            int r2 = r7.getHeight()
            int r5 = r7.mTotalHeight
            float r5 = (float) r5
            float r5 = r5 * r3
            int r5 = (int) r5
            int r2 = r2 - r5
            int r5 = r7.getPaddingTop()
            int r2 = java.lang.Math.min(r2, r5)
            goto L_0x00ae
        L_0x009f:
            int r5 = r7.getPaddingTop()
            float r5 = (float) r5
            float r6 = r7.mShowBig
            float r2 = r2 - r6
            float r5 = r5 * r2
            float r2 = r7.mAnimatableOffsetY
            int r2 = (int) r2
            float r2 = (float) r2
            float r5 = r5 + r2
            int r2 = (int) r5
        L_0x00ae:
            android.graphics.Rect r5 = r7.mShowRect
            r5.left = r4
            int r1 = java.lang.Math.max(r1, r2)
            r5.top = r1
            android.graphics.Rect r1 = r7.mShowRect
            int r0 = r0 + r4
            r1.right = r0
            int r0 = r7.mTotalHeight
            float r0 = (float) r0
            float r0 = r0 * r3
            int r0 = (int) r0
            int r0 = r0 + r2
            r1.bottom = r0
            float r0 = (float) r4
            float r1 = (float) r2
            r8.translate(r0, r1)
            r8.scale(r3, r3)
            r7.drawLongScreenshot(r8)
        L_0x00d0:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.screenshot.ScreenshotScrollView.onDraw(android.graphics.Canvas):void");
    }

    private float calcScale(boolean z) {
        return calcScale(z, this.mShowBig);
    }

    private float calcScale(boolean z, float f) {
        float f2;
        if (z) {
            float width = (float) this.mSingleBitmap.getWidth();
            f2 = (1.0f - f) * Math.min(((float) getWidthInner()) / width, ((float) getHeightInner()) / ((float) this.mSingleBitmap.getHeight()));
            f *= ((float) getWidth()) / width;
        } else {
            float f3 = 1.0f - f;
            f2 = f3 * (((float) getWidthInner()) / ((float) this.mLongBitmapDrawable.getIntrinsicWidth()));
        }
        return f2 + f;
    }

    private void drawSingleBitmap(Canvas canvas, float f) {
        int width = (getWidth() - this.mSingleBitmap.getWidth()) / 2;
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.screenshot_stroke_width);
        int dimensionPixelSize2 = getResources().getDimensionPixelSize(R.dimen.screenshot_scrollview_bitmap_margintop);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(false);
        paint.setStrokeWidth(((float) dimensionPixelSize) / f);
        paint.setColor(getContext().getResources().getColor(R.color.screenshot_part_divider_color));
        int i = dimensionPixelSize2 + dimensionPixelSize;
        int width2 = this.mSingleBitmap.getWidth() + width;
        int height = this.mSingleBitmap.getHeight() + i;
        Rect rect = new Rect(width, i, width2, height);
        float f2 = (float) (height + dimensionPixelSize);
        canvas.drawRect((float) (width - dimensionPixelSize), (float) i, (float) (width2 + dimensionPixelSize), f2, paint);
        canvas.drawBitmap(this.mSingleBitmap, (Rect) null, rect, (Paint) null);
    }

    private void drawLongScreenshot(Canvas canvas) {
        canvas.clipRect(0, 0, getWidth(), this.mTotalHeight);
        this.mLongBitmapDrawable.draw(canvas);
        Bitmap bitmap = this.mBottomPart;
        if (bitmap != null) {
            canvas.drawBitmap(bitmap, 0.0f, (float) (this.mTotalHeight - bitmap.getHeight()), (Paint) null);
        }
    }

    public void resetToShortMode(boolean z) {
        if (this.mShowBigAnimator.isRunning()) {
            this.mShowBigAnimator.end();
        }
        if (this.mShowBig == 1.0f) {
            this.mShowBigAnimator.reverse();
            if (!z) {
                this.mShowBigAnimator.end();
            }
        }
    }

    public void setIsTakingLongScreenshot(boolean z) {
        this.mIsTakingLongScreenshot = z;
        this.mIsBuildingLongScreenshot = false;
    }

    public void setSingleBitmap(Bitmap bitmap) {
        this.mSingleBitmap = bitmap;
        resetState();
        invalidate();
    }

    public void setBitmaps(List<Bitmap> list, boolean z) {
        if (list == null) {
            this.mLongBitmapDrawable = null;
        } else {
            this.mLongBitmapDrawable = new LongScreenshotUtils.LongBitmapDrawable((Bitmap[]) list.toArray(new Bitmap[list.size()]));
        }
        if (z) {
            resetState();
        }
        invalidate();
    }

    private void resetState() {
        resetToShortMode(false);
        this.mIsManuTaking = false;
        this.mOffsetY = this.mMinOffsetY;
        this.mAnimatableOffsetY = 0.0f;
    }

    public void setBottomPart(Bitmap bitmap) {
        this.mBottomPart = bitmap;
        postInvalidate();
    }

    public boolean getIsManuTaking() {
        return this.mIsManuTaking;
    }

    public void setAnimatingCallback(AnimatingCallback animatingCallback) {
        this.mAnimatingCallback = animatingCallback;
    }

    public int getShowedPageCount() {
        return this.mShowedPageCount;
    }

    public void startAnimating() {
        startAnimating(true);
    }

    public void startAnimating(boolean z) {
        this.mIsAnimatingStoped = false;
        if (z) {
            this.mIsManuTaking = false;
            int i = getResources().getDisplayMetrics().heightPixels;
            this.mTotalHeight = i;
            this.mMinTotalHeight = i;
        }
        post(this.mAnimatingStepRunnable);
    }

    public void stopAnimating() {
        stopAnimating(true);
    }

    public void stopAnimating(boolean z) {
        this.mIsAnimatingStoped = true;
        removeCallbacks(this.mAnimatingStepRunnable);
        if (z) {
            this.mIsBuildingLongScreenshot = true;
            this.mMinOffsetY = (float) (getHeight() - this.mTotalHeight);
            this.mOffsetY = this.mMinOffsetY;
        }
    }

    public void autoCalcPadding() {
        Bitmap bitmap = this.mSingleBitmap;
        int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.screenshot_actionbar_back_height);
        Configuration configuration = this.mContext.getResources().getConfiguration();
        if (Constants.IS_NOTCH && configuration.orientation == 1) {
            dimensionPixelSize += this.mContext.getResources().getDimensionPixelSize(R.dimen.status_bar_height);
        }
        int dimensionPixelSize2 = dimensionPixelSize + getResources().getDimensionPixelSize(R.dimen.screenshot_bmp_paddingtop);
        int dimensionPixelSize3 = getResources().getDimensionPixelSize(R.dimen.screenshot_bmp_paddingbottom);
        if (configuration.orientation == 2) {
            dimensionPixelSize2 = getResources().getDimensionPixelSize(R.dimen.screenshot_bmp_paddingtop_landscape);
            dimensionPixelSize3 = getResources().getDimensionPixelSize(R.dimen.screenshot_bmp_paddingbottom_landscape);
        }
        int width = (getWidth() - ((((getHeight() - dimensionPixelSize2) - dimensionPixelSize3) * bitmap.getWidth()) / bitmap.getHeight())) / 2;
        setPadding(width, dimensionPixelSize2, width, dimensionPixelSize3);
        this.mOffsetY = 0.0f;
        this.mAnimatableOffsetY = 0.0f;
        this.mMinOffsetY = (float) (getHeight() - getResources().getDisplayMetrics().heightPixels);
        this.mMaxOffsetY = (float) getPaddingTop();
    }

    public Bitmap buildLongScreenshot() {
        try {
            Bitmap createBitmap = Bitmap.createBitmap(this.mLongBitmapDrawable.getIntrinsicWidth(), this.mTotalHeight, Bitmap.Config.ARGB_8888);
            drawLongScreenshot(new Canvas(createBitmap));
            return createBitmap;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getWidthInner() {
        return (getWidth() - getPaddingLeft()) - getPaddingRight();
    }

    public int getHeightInner() {
        return (getHeight() - getPaddingTop()) - getPaddingBottom();
    }

    public Rect getShowRect() {
        return this.mShowRect;
    }

    private int getMaxTotalHeight() {
        return this.mLongBitmapDrawable.getIntrinsicHeight();
    }

    /* access modifiers changed from: private */
    public void doAnimatingStep(int i) {
        this.mTotalHeight += i;
        this.mTotalHeight = Math.min(this.mTotalHeight, getMaxTotalHeight());
        this.mTotalHeight = Math.max(this.mTotalHeight, this.mMinTotalHeight);
        int i2 = 0;
        int i3 = 0;
        for (Bitmap height : this.mLongBitmapDrawable.getBitmaps()) {
            i2 += height.getHeight();
            if (i2 > this.mTotalHeight) {
                break;
            }
            i3++;
        }
        if (i3 != this.mShowedPageCount) {
            this.mShowedPageCount = i3;
            AnimatingCallback animatingCallback = this.mAnimatingCallback;
            if (animatingCallback != null) {
                animatingCallback.onShowedPageCountChanged(this.mShowedPageCount);
            }
        }
        invalidate();
    }
}

package com.android.systemui.bubbles;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.bubbles.Bubble;
import com.android.systemui.recents.TriangleShape;

public class BubbleFlyoutView extends FrameLayout {
    private final ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();
    private boolean mArrowPointingLeft = true;
    private final Paint mBgPaint = new Paint(3);
    private final RectF mBgRect = new RectF();
    private float mBgTranslationX;
    private float mBgTranslationY;
    private final int mBubbleBitmapSize;
    private final int mBubbleElevation;
    private final float mBubbleIconTopPadding;
    private final int mBubbleSize;
    private final float mCornerRadius;
    private float[] mDotCenter;
    private int mDotColor;
    private final int mFloatingBackgroundColor;
    private final int mFlyoutElevation;
    private final int mFlyoutPadding;
    private final int mFlyoutSpaceFromBubble;
    private final ViewGroup mFlyoutTextContainer;
    private float mFlyoutToDotHeightDelta = 0.0f;
    private float mFlyoutToDotWidthDelta = 0.0f;
    private final ShapeDrawable mLeftTriangleShape;
    private final TextView mMessageText;
    private final float mNewDotRadius;
    private final float mNewDotSize;
    private Runnable mOnHide;
    private final float mOriginalDotSize;
    private float mPercentStillFlyout = 0.0f;
    private float mPercentTransitionedToDot = 1.0f;
    private final int mPointerSize;
    private float mRestingTranslationX = 0.0f;
    private final ShapeDrawable mRightTriangleShape;
    private final ImageView mSenderAvatar;
    private final TextView mSenderText;
    private float mTranslationXWhenDot = 0.0f;
    private float mTranslationYWhenDot = 0.0f;
    private final Outline mTriangleOutline = new Outline();

    public BubbleFlyoutView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(C0017R$layout.bubble_flyout, (ViewGroup) this, true);
        this.mFlyoutTextContainer = (ViewGroup) findViewById(C0015R$id.bubble_flyout_text_container);
        this.mSenderText = (TextView) findViewById(C0015R$id.bubble_flyout_name);
        this.mSenderAvatar = (ImageView) findViewById(C0015R$id.bubble_flyout_avatar);
        this.mMessageText = (TextView) this.mFlyoutTextContainer.findViewById(C0015R$id.bubble_flyout_text);
        Resources resources = getResources();
        this.mFlyoutPadding = resources.getDimensionPixelSize(C0012R$dimen.bubble_flyout_padding_x);
        this.mFlyoutSpaceFromBubble = resources.getDimensionPixelSize(C0012R$dimen.bubble_flyout_space_from_bubble);
        this.mPointerSize = resources.getDimensionPixelSize(C0012R$dimen.bubble_flyout_pointer_size);
        this.mBubbleSize = resources.getDimensionPixelSize(C0012R$dimen.individual_bubble_size);
        int dimensionPixelSize = resources.getDimensionPixelSize(C0012R$dimen.bubble_bitmap_size);
        this.mBubbleBitmapSize = dimensionPixelSize;
        this.mBubbleIconTopPadding = ((float) (this.mBubbleSize - dimensionPixelSize)) / 2.0f;
        this.mBubbleElevation = resources.getDimensionPixelSize(C0012R$dimen.bubble_elevation);
        this.mFlyoutElevation = resources.getDimensionPixelSize(C0012R$dimen.bubble_flyout_elevation);
        float f = ((float) this.mBubbleBitmapSize) * 0.228f;
        this.mOriginalDotSize = f;
        float f2 = (f * 1.0f) / 2.0f;
        this.mNewDotRadius = f2;
        this.mNewDotSize = f2 * 2.0f;
        TypedArray obtainStyledAttributes = ((FrameLayout) this).mContext.obtainStyledAttributes(new int[]{16844002, 16844145});
        this.mFloatingBackgroundColor = obtainStyledAttributes.getColor(0, -1);
        this.mCornerRadius = (float) obtainStyledAttributes.getDimensionPixelSize(1, 0);
        obtainStyledAttributes.recycle();
        int i = this.mPointerSize;
        setPadding(i, 0, i, 0);
        setWillNotDraw(false);
        setClipChildren(false);
        setTranslationZ((float) this.mFlyoutElevation);
        setOutlineProvider(new ViewOutlineProvider() {
            /* class com.android.systemui.bubbles.BubbleFlyoutView.AnonymousClass1 */

            public void getOutline(View view, Outline outline) {
                BubbleFlyoutView.this.getOutline(outline);
            }
        });
        setLayoutDirection(3);
        this.mBgPaint.setColor(this.mFloatingBackgroundColor);
        int i2 = this.mPointerSize;
        ShapeDrawable shapeDrawable = new ShapeDrawable(TriangleShape.createHorizontal((float) i2, (float) i2, true));
        this.mLeftTriangleShape = shapeDrawable;
        int i3 = this.mPointerSize;
        shapeDrawable.setBounds(0, 0, i3, i3);
        this.mLeftTriangleShape.getPaint().setColor(this.mFloatingBackgroundColor);
        int i4 = this.mPointerSize;
        ShapeDrawable shapeDrawable2 = new ShapeDrawable(TriangleShape.createHorizontal((float) i4, (float) i4, false));
        this.mRightTriangleShape = shapeDrawable2;
        int i5 = this.mPointerSize;
        shapeDrawable2.setBounds(0, 0, i5, i5);
        this.mRightTriangleShape.getPaint().setColor(this.mFloatingBackgroundColor);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        renderBackground(canvas);
        invalidateOutline();
        super.onDraw(canvas);
    }

    /* access modifiers changed from: package-private */
    public void setupFlyoutStartingAsDot(Bubble.FlyoutMessage flyoutMessage, PointF pointF, float f, boolean z, int i, Runnable runnable, Runnable runnable2, float[] fArr, boolean z2) {
        Drawable drawable = flyoutMessage.senderAvatar;
        if (drawable == null || !flyoutMessage.isGroupChat) {
            this.mSenderAvatar.setVisibility(8);
            this.mSenderAvatar.setTranslationX(0.0f);
            this.mMessageText.setTranslationX(0.0f);
            this.mSenderText.setTranslationX(0.0f);
        } else {
            this.mSenderAvatar.setVisibility(0);
            this.mSenderAvatar.setImageDrawable(drawable);
        }
        int i2 = ((int) (f * 0.6f)) - (this.mFlyoutPadding * 2);
        if (!TextUtils.isEmpty(flyoutMessage.senderName)) {
            this.mSenderText.setMaxWidth(i2);
            this.mSenderText.setText(flyoutMessage.senderName);
            this.mSenderText.setVisibility(0);
        } else {
            this.mSenderText.setVisibility(8);
        }
        this.mArrowPointingLeft = z;
        this.mDotColor = i;
        this.mOnHide = runnable2;
        this.mDotCenter = fArr;
        setCollapsePercent(1.0f);
        this.mMessageText.setMaxWidth(i2);
        this.mMessageText.setText(flyoutMessage.message);
        post(new Runnable(pointF, z2, runnable) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleFlyoutView$MmTh2kLTzOgAqdKgn0YGS6zixjU */
            public final /* synthetic */ PointF f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ Runnable f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void run() {
                BubbleFlyoutView.this.lambda$setupFlyoutStartingAsDot$0$BubbleFlyoutView(this.f$1, this.f$2, this.f$3);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setupFlyoutStartingAsDot$0 */
    public /* synthetic */ void lambda$setupFlyoutStartingAsDot$0$BubbleFlyoutView(PointF pointF, boolean z, Runnable runnable) {
        float f;
        float f2;
        float f3;
        float f4;
        if (this.mMessageText.getLineCount() > 1) {
            f2 = pointF.y;
            f = this.mBubbleIconTopPadding;
        } else {
            f2 = pointF.y;
            f = ((float) (this.mBubbleSize - this.mFlyoutTextContainer.getHeight())) / 2.0f;
        }
        float f5 = f2 + f;
        setTranslationY(f5);
        if (this.mArrowPointingLeft) {
            f3 = pointF.x + ((float) this.mBubbleSize) + ((float) this.mFlyoutSpaceFromBubble);
        } else {
            f3 = (pointF.x - ((float) getWidth())) - ((float) this.mFlyoutSpaceFromBubble);
        }
        this.mRestingTranslationX = f3;
        float f6 = 0.0f;
        if (z) {
            f4 = 0.0f;
        } else {
            f4 = this.mNewDotSize;
        }
        this.mFlyoutToDotWidthDelta = ((float) getWidth()) - f4;
        this.mFlyoutToDotHeightDelta = ((float) getHeight()) - f4;
        if (!z) {
            f6 = this.mOriginalDotSize / 2.0f;
        }
        float f7 = pointF.x;
        float[] fArr = this.mDotCenter;
        float f8 = this.mRestingTranslationX;
        float f9 = f5 - ((pointF.y + fArr[1]) - f6);
        this.mTranslationXWhenDot = -(f8 - ((f7 + fArr[0]) - f6));
        this.mTranslationYWhenDot = -f9;
        if (runnable != null) {
            runnable.run();
        }
    }

    /* access modifiers changed from: package-private */
    public void hideFlyout() {
        Runnable runnable = this.mOnHide;
        if (runnable != null) {
            runnable.run();
            this.mOnHide = null;
        }
        setVisibility(8);
    }

    /* access modifiers changed from: package-private */
    public void setCollapsePercent(float f) {
        if (!Float.isNaN(f)) {
            float max = Math.max(0.0f, Math.min(f, 1.0f));
            this.mPercentTransitionedToDot = max;
            this.mPercentStillFlyout = 1.0f - max;
            float width = max * ((float) (this.mArrowPointingLeft ? -getWidth() : getWidth()));
            float clampPercentage = clampPercentage((this.mPercentStillFlyout - 0.75f) / 0.25f);
            this.mMessageText.setTranslationX(width);
            this.mMessageText.setAlpha(clampPercentage);
            this.mSenderText.setTranslationX(width);
            this.mSenderText.setAlpha(clampPercentage);
            this.mSenderAvatar.setTranslationX(width);
            this.mSenderAvatar.setAlpha(clampPercentage);
            int i = this.mFlyoutElevation;
            setTranslationZ(((float) i) - (((float) (i - this.mBubbleElevation)) * this.mPercentTransitionedToDot));
            invalidate();
        }
    }

    /* access modifiers changed from: package-private */
    public float getRestingTranslationX() {
        return this.mRestingTranslationX;
    }

    private float clampPercentage(float f) {
        return Math.min(1.0f, Math.max(0.0f, f));
    }

    private void renderBackground(Canvas canvas) {
        float width = ((float) getWidth()) - (this.mFlyoutToDotWidthDelta * this.mPercentTransitionedToDot);
        float height = ((float) getHeight()) - (this.mFlyoutToDotHeightDelta * this.mPercentTransitionedToDot);
        float interpolatedRadius = getInterpolatedRadius();
        float f = this.mTranslationXWhenDot;
        float f2 = this.mPercentTransitionedToDot;
        this.mBgTranslationX = f * f2;
        this.mBgTranslationY = this.mTranslationYWhenDot * f2;
        RectF rectF = this.mBgRect;
        int i = this.mPointerSize;
        float f3 = this.mPercentStillFlyout;
        rectF.set(((float) i) * f3, 0.0f, width - (((float) i) * f3), height);
        this.mBgPaint.setColor(((Integer) this.mArgbEvaluator.evaluate(this.mPercentTransitionedToDot, Integer.valueOf(this.mFloatingBackgroundColor), Integer.valueOf(this.mDotColor))).intValue());
        canvas.save();
        canvas.translate(this.mBgTranslationX, this.mBgTranslationY);
        renderPointerTriangle(canvas, width, height);
        canvas.drawRoundRect(this.mBgRect, interpolatedRadius, interpolatedRadius, this.mBgPaint);
        canvas.restore();
    }

    private void renderPointerTriangle(Canvas canvas, float f, float f2) {
        canvas.save();
        int i = this.mArrowPointingLeft ? 1 : -1;
        float f3 = this.mPercentTransitionedToDot;
        int i2 = this.mPointerSize;
        float f4 = ((float) i) * f3 * ((float) i2) * 2.0f;
        if (!this.mArrowPointingLeft) {
            f4 += f - ((float) i2);
        }
        float f5 = (f2 / 2.0f) - (((float) this.mPointerSize) / 2.0f);
        ShapeDrawable shapeDrawable = this.mArrowPointingLeft ? this.mLeftTriangleShape : this.mRightTriangleShape;
        canvas.translate(f4, f5);
        shapeDrawable.setAlpha((int) (this.mPercentStillFlyout * 255.0f));
        shapeDrawable.draw(canvas);
        shapeDrawable.getOutline(this.mTriangleOutline);
        this.mTriangleOutline.offset((int) f4, (int) f5);
        canvas.restore();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getOutline(Outline outline) {
        if (!this.mTriangleOutline.isEmpty()) {
            Path path = new Path();
            float interpolatedRadius = getInterpolatedRadius();
            path.addRoundRect(this.mBgRect, interpolatedRadius, interpolatedRadius, Path.Direction.CW);
            outline.setPath(path);
            if (this.mPercentStillFlyout > 0.5f) {
                outline.mPath.addPath(this.mTriangleOutline.mPath);
            }
            Matrix matrix = new Matrix();
            matrix.postTranslate(((float) getLeft()) + this.mBgTranslationX, ((float) getTop()) + this.mBgTranslationY);
            float f = this.mPercentTransitionedToDot;
            if (f > 0.98f) {
                float f2 = (f - 0.98f) / 0.02f;
                float f3 = 1.0f - f2;
                float f4 = this.mNewDotRadius;
                matrix.postTranslate(f4 * f2, f4 * f2);
                matrix.preScale(f3, f3);
            }
            outline.mPath.transform(matrix);
        }
    }

    private float getInterpolatedRadius() {
        float f = this.mNewDotRadius;
        float f2 = this.mPercentTransitionedToDot;
        return (f * f2) + (this.mCornerRadius * (1.0f - f2));
    }
}

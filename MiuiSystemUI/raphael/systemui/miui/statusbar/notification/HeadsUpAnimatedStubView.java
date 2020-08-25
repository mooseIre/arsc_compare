package com.android.systemui.miui.statusbar.notification;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import com.android.systemui.miui.BitmapUtils;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.OptimizedHeadsUpNotificationView;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.lang.ref.WeakReference;

public class HeadsUpAnimatedStubView extends View implements OnHeadsUpChangedListener {
    private Drawable mBar;
    private int mBarMarginBottom;
    private final Rect mBounds;
    private int mContentAlpha;
    /* access modifiers changed from: private */
    public Drawable mHeadsUpIcon;
    private HeadsUpManager mHeadsUpManager;
    /* access modifiers changed from: private */
    public Bitmap mHeadsUpStub;
    private Paint mHeadsUpStubPaint;
    private int mIconAlpha;
    private int mIconSize;
    private Drawable mIndicatorBg;
    private OnHeadsUpHiddenListener mListener;
    private final Handler mMainHandler;
    private final Runnable mRecycleRunnable;
    private WeakReference<ExpandableNotificationRow> mRow;
    private final Rect mScaledContentBounds;
    private boolean mShowMiniBar;

    public interface OnHeadsUpHiddenListener {
        void onHeadsUpHiddenForAnimationChanged();
    }

    public HeadsUpAnimatedStubView(Context context) {
        this(context, (AttributeSet) null);
    }

    public HeadsUpAnimatedStubView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public HeadsUpAnimatedStubView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mMainHandler = new Handler(Looper.getMainLooper());
        this.mRecycleRunnable = new Runnable() {
            public final void run() {
                HeadsUpAnimatedStubView.this.recycleStubs();
            }
        };
        this.mBounds = new Rect();
        this.mScaledContentBounds = new Rect();
        this.mIndicatorBg = null;
        this.mHeadsUpIcon = null;
        this.mHeadsUpStub = null;
        this.mHeadsUpStubPaint = new Paint();
        this.mRow = null;
        this.mBar = null;
        this.mShowMiniBar = false;
        this.mContentAlpha = 255;
        this.mIconAlpha = 0;
    }

    public void setHeadsHiddenListener(OnHeadsUpHiddenListener onHeadsUpHiddenListener) {
        this.mListener = onHeadsUpHiddenListener;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIconSize = getResources().getDimensionPixelSize(R.dimen.heads_up_animated_stub_icon_size);
        this.mBarMarginBottom = getResources().getDimensionPixelSize(R.dimen.mini_window_bar_marginBottom);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        HeadsUpManager headsUpManager = this.mHeadsUpManager;
        if (headsUpManager != null) {
            headsUpManager.addListener(this);
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        HeadsUpManager headsUpManager = this.mHeadsUpManager;
        if (headsUpManager != null) {
            headsUpManager.removeListener(this);
        }
        recycleStubs();
    }

    private void reScheduleRecycle() {
        this.mMainHandler.removeCallbacks(this.mRecycleRunnable);
        this.mMainHandler.postDelayed(this.mRecycleRunnable, 5000);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        Drawable drawable = this.mIndicatorBg;
        if (drawable != null) {
            drawable.draw(canvas);
        }
        Bitmap bitmap = this.mHeadsUpStub;
        if (bitmap != null && !bitmap.isRecycled()) {
            if (this.mIconAlpha > 0) {
                Rect rect = this.mBounds;
                int width = (int) (((float) rect.left) + (((float) (rect.width() - this.mIconSize)) / 2.0f));
                Rect rect2 = this.mBounds;
                int height = rect2.height();
                int i = this.mIconSize;
                int i2 = (int) (((float) rect2.top) + (((float) (height - i)) / 2.0f));
                this.mHeadsUpIcon.setBounds(width, i2, width + i, i + i2);
                this.mHeadsUpIcon.setAlpha(this.mIconAlpha);
                this.mHeadsUpIcon.draw(canvas);
            }
            int i3 = this.mContentAlpha;
            if (i3 > 0) {
                this.mHeadsUpStubPaint.setAlpha(i3);
                if (this.mBounds.width() > this.mHeadsUpStub.getWidth()) {
                    Rect rect3 = this.mBounds;
                    float width2 = ((float) rect3.left) + (((float) (rect3.width() - this.mHeadsUpStub.getWidth())) / 2.0f);
                    Rect rect4 = this.mBounds;
                    canvas.drawBitmap(this.mHeadsUpStub, width2, ((float) rect4.top) + (((float) (rect4.height() - this.mHeadsUpStub.getHeight())) / 2.0f), this.mHeadsUpStubPaint);
                } else {
                    int height2 = (int) (((float) this.mHeadsUpStub.getHeight()) * (((float) this.mBounds.width()) / ((float) this.mHeadsUpStub.getWidth())));
                    Rect rect5 = this.mBounds;
                    int height3 = (int) (((float) rect5.top) + (((float) (rect5.height() - height2)) / 2.0f));
                    Rect rect6 = this.mScaledContentBounds;
                    Rect rect7 = this.mBounds;
                    rect6.set(rect7.left, height3, rect7.right, height2 + height3);
                    canvas.drawBitmap(this.mHeadsUpStub, (Rect) null, this.mScaledContentBounds, this.mHeadsUpStubPaint);
                }
            }
            if (this.mShowMiniBar) {
                int i4 = this.mBounds.bottom - this.mBarMarginBottom;
                Rect rect8 = this.mBounds;
                int width3 = rect8.left + ((rect8.width() - this.mBar.getIntrinsicWidth()) / 2);
                this.mBar.setBounds(width3, i4 - this.mBar.getIntrinsicHeight(), this.mBar.getIntrinsicWidth() + width3, i4);
                this.mBar.draw(canvas);
            }
        }
    }

    public void onHeadsUpPinned(ExpandableNotificationRow expandableNotificationRow) {
        WeakReference<ExpandableNotificationRow> weakReference = this.mRow;
        if (weakReference == null || weakReference.get() == null || !((ExpandableNotificationRow) this.mRow.get()).isHiddenForAnimation()) {
            setHeadsUpRow(expandableNotificationRow);
        }
    }

    public void onHeadsUpUnPinned(ExpandableNotificationRow expandableNotificationRow) {
        WeakReference<ExpandableNotificationRow> weakReference = this.mRow;
        if (weakReference == null || weakReference.get() == null || !((ExpandableNotificationRow) this.mRow.get()).isHiddenForAnimation()) {
            reset();
        }
    }

    private void setHeadsUpRow(final ExpandableNotificationRow expandableNotificationRow) {
        recycleStubs();
        this.mRow = new WeakReference<>(expandableNotificationRow);
        this.mIndicatorBg = getResources().getDrawable(R.drawable.notification_heads_up_bg);
        this.mBar = getResources().getDrawable(R.drawable.mini_window_bar);
        expandableNotificationRow.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                View view;
                expandableNotificationRow.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                View headsUpChild = expandableNotificationRow.getShowingLayout().getHeadsUpChild();
                if (headsUpChild instanceof OptimizedHeadsUpNotificationView) {
                    view = ((OptimizedHeadsUpNotificationView) headsUpChild).getContent();
                } else {
                    view = expandableNotificationRow.getContentView();
                }
                if (view != null) {
                    Bitmap unused = HeadsUpAnimatedStubView.this.mHeadsUpStub = BitmapUtils.view2Bitmap(view, view.getMeasuredWidth(), expandableNotificationRow.getPinnedHeadsUpHeight());
                    Drawable unused2 = HeadsUpAnimatedStubView.this.mHeadsUpIcon = expandableNotificationRow.getStatusBarNotification().getAppIcon();
                    expandableNotificationRow.updateMiniBarAlpha(1.0f);
                }
            }
        });
    }

    public void setAnimationRunning(boolean z) {
        setVisibility(z ? 0 : 8);
        WeakReference<ExpandableNotificationRow> weakReference = this.mRow;
        if (weakReference != null && weakReference.get() != null) {
            ((ExpandableNotificationRow) this.mRow.get()).setHiddenForAnimation(z);
            OnHeadsUpHiddenListener onHeadsUpHiddenListener = this.mListener;
            if (onHeadsUpHiddenListener != null) {
                onHeadsUpHiddenListener.onHeadsUpHiddenForAnimationChanged();
            }
        }
    }

    public void setBarVisibility(boolean z) {
        this.mShowMiniBar = z;
    }

    public void setHeadsUpManager(HeadsUpManager headsUpManager) {
        this.mHeadsUpManager = headsUpManager;
    }

    public void applyStubBounds(Rect rect) {
        applyStubBounds(rect.left, rect.top, rect.right, rect.bottom);
    }

    public void applyStubBounds(int i, int i2, int i3, int i4) {
        this.mBounds.set(i, i2, i3, i4);
        this.mIndicatorBg.setBounds(this.mBounds);
        postInvalidateOnAnimation();
    }

    public void applyAlpha(float f, float f2) {
        this.mContentAlpha = (int) (f * 255.0f);
        this.mIconAlpha = (int) (f2 * 255.0f);
        postInvalidateOnAnimation();
    }

    /* access modifiers changed from: private */
    public void recycleStubs() {
        Bitmap bitmap = this.mHeadsUpStub;
        if (bitmap != null && !bitmap.isRecycled()) {
            this.mHeadsUpStub.recycle();
            this.mHeadsUpStub = null;
        }
        this.mMainHandler.removeCallbacks(this.mRecycleRunnable);
    }

    public void reset() {
        reScheduleRecycle();
        applyAlpha(1.0f, 0.0f);
    }
}

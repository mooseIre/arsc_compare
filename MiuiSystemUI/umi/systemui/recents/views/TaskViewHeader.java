package com.android.systemui.recents.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewDebug;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.ui.ShowApplicationInfoEvent;
import com.android.systemui.recents.misc.SpringAnimationImpl;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.Task;
import com.android.systemui.util.ColorUtils;

public class TaskViewHeader extends FrameLayout implements View.OnClickListener, View.OnLongClickListener {
    ImageView mAppIconView;
    ImageView mAppInfoView;
    FrameLayout mAppOverlayView;
    TextView mAppTitleView;
    private HighlightColorDrawable mBackground;
    int mCornerRadius;
    @ViewDebug.ExportedProperty(category = "recents")
    float mDimAlpha;
    private Paint mDimLayerPaint;
    int mDisabledTaskBarBackgroundColor;
    TextView mDismissView;
    private CountDownTimer mFocusTimerCountDown;
    ProgressBar mFocusTimerIndicator;
    int mHeaderBarHeight;
    int mHeaderButtonPadding;
    int mHighlightHeight;
    ImageView mIconView;
    ImageView mLockedView;
    private HighlightColorDrawable mOverlayBackground;
    public SpringAnimationImpl mSpringAnimationImpl;
    Task mTask;
    @ViewDebug.ExportedProperty(category = "recents")
    Rect mTaskViewRect;
    TextView mTitleView;
    /* access modifiers changed from: private */
    public float[] mTmpHSL;

    /* access modifiers changed from: protected */
    public int[] onCreateDrawableState(int i) {
        return new int[0];
    }

    /* access modifiers changed from: package-private */
    public void resetNoUserInteractionState() {
    }

    /* access modifiers changed from: package-private */
    public void setNoUserInteractionState() {
    }

    /* access modifiers changed from: package-private */
    public void startNoUserInteractionAnimation() {
    }

    private class HighlightColorDrawable extends Drawable {
        private Paint mBackgroundPaint;
        private int mColor;
        private float mDimAlpha;
        private Paint mHighlightPaint = new Paint();

        public int getOpacity() {
            return -1;
        }

        public void setAlpha(int i) {
        }

        public void setColorFilter(ColorFilter colorFilter) {
        }

        public HighlightColorDrawable() {
            Paint paint = new Paint();
            this.mBackgroundPaint = paint;
            paint.setColor(Color.argb(255, 0, 0, 0));
            this.mBackgroundPaint.setAntiAlias(true);
            this.mHighlightPaint.setColor(Color.argb(255, 255, 255, 255));
            this.mHighlightPaint.setAntiAlias(true);
        }

        public void setColorAndDim(int i, float f) {
            if (this.mColor != i || Float.compare(this.mDimAlpha, f) != 0) {
                this.mColor = i;
                this.mDimAlpha = f;
                this.mBackgroundPaint.setColor(i);
                ColorUtils.colorToHSL(i, TaskViewHeader.this.mTmpHSL);
                TaskViewHeader.this.mTmpHSL[2] = Math.min(1.0f, TaskViewHeader.this.mTmpHSL[2] + ((1.0f - f) * 0.075f));
                this.mHighlightPaint.setColor(ColorUtils.HSLToColor(TaskViewHeader.this.mTmpHSL));
                invalidateSelf();
            }
        }

        public void draw(Canvas canvas) {
            float width = (float) TaskViewHeader.this.mTaskViewRect.width();
            TaskViewHeader taskViewHeader = TaskViewHeader.this;
            float max = (float) (Math.max(taskViewHeader.mHighlightHeight, taskViewHeader.mCornerRadius) * 2);
            int i = TaskViewHeader.this.mCornerRadius;
            canvas.drawRoundRect(0.0f, 0.0f, width, max, (float) i, (float) i, this.mHighlightPaint);
            TaskViewHeader taskViewHeader2 = TaskViewHeader.this;
            float f = (float) taskViewHeader2.mHighlightHeight;
            float width2 = (float) taskViewHeader2.mTaskViewRect.width();
            int height = TaskViewHeader.this.getHeight();
            int i2 = TaskViewHeader.this.mCornerRadius;
            canvas.drawRoundRect(0.0f, f, width2, (float) (height + i2), (float) i2, (float) i2, this.mBackgroundPaint);
        }

        public int getColor() {
            return this.mColor;
        }
    }

    public TaskViewHeader(Context context) {
        this(context, (AttributeSet) null);
    }

    public TaskViewHeader(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public TaskViewHeader(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public TaskViewHeader(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mTaskViewRect = new Rect();
        this.mTmpHSL = new float[3];
        this.mDimLayerPaint = new Paint();
        setWillNotDraw(false);
        Resources resources = context.getResources();
        context.getDrawable(R.drawable.recents_dismiss_light);
        context.getDrawable(R.drawable.recents_dismiss_dark);
        this.mCornerRadius = resources.getDimensionPixelSize(R.dimen.recents_task_view_rounded_corners_radius);
        this.mHighlightHeight = resources.getDimensionPixelSize(R.dimen.recents_task_view_highlight);
        context.getColor(R.color.recents_task_bar_light_text_color);
        context.getColor(R.color.recents_task_bar_dark_text_color);
        context.getDrawable(R.drawable.recents_move_task_freeform_light);
        context.getDrawable(R.drawable.recents_move_task_freeform_dark);
        context.getDrawable(R.drawable.recents_move_task_fullscreen_light);
        context.getDrawable(R.drawable.recents_move_task_fullscreen_dark);
        context.getDrawable(R.drawable.recents_info_light);
        context.getDrawable(R.drawable.recents_info_dark);
        this.mDisabledTaskBarBackgroundColor = context.getColor(R.color.recents_task_bar_disabled_background_color);
        HighlightColorDrawable highlightColorDrawable = new HighlightColorDrawable();
        this.mBackground = highlightColorDrawable;
        highlightColorDrawable.setColorAndDim(Color.argb(255, 0, 0, 0), 0.0f);
        this.mOverlayBackground = new HighlightColorDrawable();
        this.mDimLayerPaint.setColor(Color.argb(255, 0, 0, 0));
        this.mDimLayerPaint.setAntiAlias(true);
        this.mSpringAnimationImpl = new SpringAnimationImpl(this);
    }

    public void reset() {
        setAlpha(1.0f);
        setScaleX(1.0f);
        setScaleY(1.0f);
        setTranslationX(0.0f);
        setTranslationY(0.0f);
        hideAppOverlay(true);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        Recents.getSystemServices();
        ImageView imageView = (ImageView) findViewById(R.id.icon);
        this.mIconView = imageView;
        imageView.setOnLongClickListener(this);
        this.mTitleView = (TextView) findViewById(R.id.title);
        this.mLockedView = (ImageView) findViewById(R.id.locked_flag);
        this.mDismissView = (TextView) findViewById(R.id.dismiss_task);
        onConfigurationChanged();
    }

    private void updateLayoutParams(View view, View view2, View view3, View view4) {
        setLayoutParams(new FrameLayout.LayoutParams(-1, this.mHeaderBarHeight, 48));
        int i = this.mHeaderBarHeight;
        view.setLayoutParams(new FrameLayout.LayoutParams(i, i, 8388611));
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2, 8388627);
        layoutParams.setMarginStart(this.mHeaderBarHeight);
        layoutParams.setMarginEnd(this.mHeaderBarHeight / 2);
        view2.setLayoutParams(layoutParams);
        if (view3 != null) {
            int i2 = this.mHeaderBarHeight;
            FrameLayout.LayoutParams layoutParams2 = new FrameLayout.LayoutParams(i2, i2, 8388613);
            layoutParams2.setMarginEnd(this.mHeaderBarHeight);
            view3.setLayoutParams(layoutParams2);
            int i3 = this.mHeaderButtonPadding;
            view3.setPadding(i3, i3, i3, i3);
        }
        if (view4 != null) {
            int i4 = this.mHeaderBarHeight;
            view4.setLayoutParams(new FrameLayout.LayoutParams(i4, i4, 8388613));
            int i5 = this.mHeaderButtonPadding;
            view4.setPadding(i5, i5, i5, i5);
        }
    }

    public void onConfigurationChanged() {
        getResources();
        int dimensionForDevice = TaskStackLayoutAlgorithm.getDimensionForDevice(getContext(), R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height_tablet_land, R.dimen.recents_task_view_header_height, R.dimen.recents_task_view_header_height_tablet_land);
        int dimensionForDevice2 = TaskStackLayoutAlgorithm.getDimensionForDevice(getContext(), R.dimen.recents_task_view_header_button_padding, R.dimen.recents_task_view_header_button_padding, R.dimen.recents_task_view_header_button_padding, R.dimen.recents_task_view_header_button_padding_tablet_land, R.dimen.recents_task_view_header_button_padding, R.dimen.recents_task_view_header_button_padding_tablet_land);
        if (dimensionForDevice != this.mHeaderBarHeight || dimensionForDevice2 != this.mHeaderButtonPadding) {
            this.mHeaderBarHeight = dimensionForDevice;
            this.mHeaderButtonPadding = dimensionForDevice2;
            updateLayoutParams(this.mIconView, this.mTitleView, (View) null, (View) null);
            if (this.mAppOverlayView != null) {
                updateLayoutParams(this.mAppIconView, this.mAppTitleView, (View) null, this.mAppInfoView);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        onTaskViewSizeChanged(this.mTaskViewRect.width(), this.mTaskViewRect.height());
        int width = this.mHeaderBarHeight + this.mTitleView.getWidth() + 10;
        if (isLayoutRtl()) {
            width = (getMeasuredWidth() - width) - this.mLockedView.getMeasuredWidth();
        }
        ImageView imageView = this.mLockedView;
        imageView.layout(width, imageView.getTop(), this.mLockedView.getMeasuredWidth() + width, this.mLockedView.getBottom());
    }

    public void onTaskViewSizeChanged(int i, int i2) {
        this.mTaskViewRect.set(0, 0, i, i2);
        setLeftTopRightBottom(0, 0, i, getMeasuredHeight());
    }

    public void startFocusTimerIndicator(int i) {
        ProgressBar progressBar = this.mFocusTimerIndicator;
        if (progressBar != null) {
            progressBar.setVisibility(0);
            this.mFocusTimerIndicator.setMax(i);
            this.mFocusTimerIndicator.setProgress(i);
            CountDownTimer countDownTimer = this.mFocusTimerCountDown;
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            this.mFocusTimerCountDown = new CountDownTimer((long) i, 30) {
                public void onFinish() {
                }

                public void onTick(long j) {
                    TaskViewHeader.this.mFocusTimerIndicator.setProgress((int) j);
                }
            }.start();
        }
    }

    public void cancelFocusTimerIndicator() {
        CountDownTimer countDownTimer;
        if (this.mFocusTimerIndicator != null && (countDownTimer = this.mFocusTimerCountDown) != null) {
            countDownTimer.cancel();
            this.mFocusTimerIndicator.setProgress(0);
            this.mFocusTimerIndicator.setVisibility(4);
        }
    }

    /* access modifiers changed from: package-private */
    public int getSecondaryColor(int i, boolean z) {
        return Utilities.getColorWithOverlay(i, z ? -1 : -16777216, 0.8f);
    }

    public void setDimAlpha(float f) {
        if (Float.compare(this.mDimAlpha, f) != 0) {
            this.mDimAlpha = f;
            this.mTitleView.setAlpha(1.0f - f);
            updateBackgroundColor(this.mBackground.getColor(), f);
        }
    }

    private void updateBackgroundColor(int i, float f) {
        if (this.mTask != null) {
            this.mBackground.setColorAndDim(i, f);
            ColorUtils.colorToHSL(i, this.mTmpHSL);
            float[] fArr = this.mTmpHSL;
            fArr[2] = Math.min(1.0f, fArr[2] + ((1.0f - f) * -0.0625f));
            this.mOverlayBackground.setColorAndDim(ColorUtils.HSLToColor(this.mTmpHSL), f);
            this.mDimLayerPaint.setAlpha((int) (f * 255.0f));
            invalidate();
        }
    }

    public void bindToTask(Task task, boolean z, boolean z2) {
        int i;
        this.mTask = task;
        if (z2) {
            i = this.mDisabledTaskBarBackgroundColor;
        } else {
            i = task.colorPrimary;
        }
        if (this.mBackground.getColor() != i) {
            updateBackgroundColor(i, this.mDimAlpha);
        }
        if (!this.mTitleView.getText().toString().equals(task.title)) {
            this.mTitleView.setText(task.title);
        }
        if (Recents.getDebugFlags().isFastToggleRecentsEnabled()) {
            if (this.mFocusTimerIndicator == null) {
                this.mFocusTimerIndicator = (ProgressBar) Utilities.findViewStubById((View) this, (int) R.id.focus_timer_indicator_stub).inflate();
            }
            this.mFocusTimerIndicator.getProgressDrawable().setColorFilter(getSecondaryColor(task.colorPrimary, task.useLightOnPrimaryColor), PorterDuff.Mode.SRC_IN);
        }
        if (z) {
            this.mIconView.setContentDescription(task.appInfoDescription);
            this.mIconView.setOnClickListener(this);
            this.mIconView.setClickable(true);
        }
    }

    public void onTaskDataLoaded() {
        Drawable drawable;
        Task task = this.mTask;
        if (task != null && (drawable = task.icon) != null) {
            this.mIconView.setImageDrawable(drawable);
        }
    }

    /* access modifiers changed from: package-private */
    public void unbindFromTask(boolean z) {
        this.mTask = null;
        this.mIconView.setImageDrawable((Drawable) null);
        if (z) {
            this.mIconView.setClickable(false);
        }
    }

    public void onClick(View view) {
        if (view == this.mIconView) {
            RecentsEventBus.getDefault().send(new ShowApplicationInfoEvent(this.mTask));
        } else if (view == this.mAppInfoView) {
            RecentsEventBus.getDefault().send(new ShowApplicationInfoEvent(this.mTask));
        } else if (view == this.mAppIconView) {
            hideAppOverlay(false);
        }
    }

    public boolean onLongClick(View view) {
        if (view == this.mIconView || view != this.mAppIconView) {
            return false;
        }
        hideAppOverlay(false);
        return true;
    }

    private void hideAppOverlay(boolean z) {
        FrameLayout frameLayout = this.mAppOverlayView;
        if (frameLayout != null) {
            if (z) {
                frameLayout.setVisibility(8);
                return;
            }
            Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(this.mAppOverlayView, this.mIconView.getLeft() + (this.mIconView.getWidth() / 2), this.mIconView.getTop() + (this.mIconView.getHeight() / 2), (float) getWidth(), 0.0f);
            createCircularReveal.setDuration(250);
            createCircularReveal.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            createCircularReveal.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animator) {
                    TaskViewHeader.this.mAppOverlayView.setVisibility(8);
                }
            });
            createCircularReveal.start();
        }
    }

    public void updateLockedFlagVisible(final boolean z, boolean z2, long j) {
        float f = 1.0f;
        if (z2) {
            ViewPropertyAnimator animate = this.mLockedView.animate();
            if (!z) {
                f = 0.0f;
            }
            animate.alpha(f).setStartDelay(j).setDuration(250).setListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animator) {
                    if (z) {
                        TaskViewHeader.this.mLockedView.setVisibility(0);
                    }
                }

                public void onAnimationEnd(Animator animator) {
                    if (!z) {
                        TaskViewHeader.this.mLockedView.setVisibility(4);
                    }
                }
            }).start();
            return;
        }
        if (z) {
            this.mLockedView.setAlpha(1.0f);
        }
        this.mLockedView.setVisibility(z ? 0 : 4);
    }

    public void startDismissTaskAnim() {
        this.mIconView.animate().setDuration(150).setStartDelay(0).alpha(0.0f).start();
        this.mTitleView.animate().setDuration(150).setStartDelay(0).alpha(0.0f).start();
        this.mLockedView.animate().setDuration(150).setStartDelay(0).alpha(0.0f).start();
        this.mDismissView.animate().setDuration(150).setStartDelay(0).alpha(1.0f).setListener((Animator.AnimatorListener) null).start();
        this.mDismissView.setVisibility(0);
    }

    public void startResetTaskAnim() {
        this.mIconView.animate().setDuration(150).setStartDelay(50).alpha(1.0f).start();
        this.mTitleView.animate().setDuration(150).setStartDelay(50).alpha(1.0f).start();
        this.mLockedView.animate().setDuration(150).setStartDelay(50).alpha(1.0f).start();
        this.mDismissView.animate().setDuration(150).setStartDelay(50).alpha(0.0f).setListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                TaskViewHeader.this.mDismissView.setVisibility(8);
            }
        }).start();
    }

    public void resetViewState() {
        this.mIconView.animate().cancel();
        this.mTitleView.animate().cancel();
        this.mLockedView.animate().cancel();
        this.mDismissView.animate().cancel();
        this.mIconView.setAlpha(1.0f);
        this.mTitleView.setAlpha(1.0f);
        this.mLockedView.setAlpha(1.0f);
        this.mDismissView.setAlpha(0.0f);
        this.mDismissView.setVisibility(8);
    }

    public SpringAnimationImpl getSpringAnimationImpl() {
        return this.mSpringAnimationImpl;
    }
}

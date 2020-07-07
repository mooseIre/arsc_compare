package com.android.systemui.miui.volume;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.systemui.miui.DrawableAnimators;
import com.android.systemui.miui.DrawableUtils;
import com.android.systemui.miui.volume.MiuiVolumeTimerSeekBar;
import com.android.systemui.miui.widget.CenterTextDrawable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class MiuiVolumeTimerDrawableHelper implements MiuiVolumeTimerSeekBar.TimerSeekBarMotions {
    private Drawable mBackground;
    private Drawable mBackgroundSegments;
    private Context mContext;
    private List<Object> mCountDownStates = new ArrayList();
    private int mCurrentSegment;
    private int mDeterminedSegment;
    /* access modifiers changed from: private */
    public boolean mDragging;
    private Drawable mDrawable;
    private Drawable mProgress;
    private Drawable mProgressDraggingIcon;
    /* access modifiers changed from: private */
    public Drawable mProgressDraggingRect;
    private Drawable mProgressDraggingRectIdle;
    /* access modifiers changed from: private */
    public Drawable mProgressNormalRect;
    /* access modifiers changed from: private */
    public boolean mTicking;
    private List<Object> mTickingTimes = new ArrayList();
    private CenterTextDrawable mTimeDrawableBg;
    private CenterTextDrawable mTimeDrawableFg;
    private String mTimeDrawableHint;
    private int mTimeRemain;
    private String[] mTimeSegmentTitle;

    MiuiVolumeTimerDrawableHelper(SeekBar seekBar, boolean z) {
        this.mContext = seekBar.getContext();
        this.mTimeDrawableHint = seekBar.getResources().getString(R$string.miui_ringer_count_down);
        this.mTimeSegmentTitle = seekBar.getResources().getStringArray(R$array.miui_volume_timer_segments_title);
        Drawable progressDrawable = seekBar.getProgressDrawable();
        this.mDrawable = progressDrawable;
        if (progressDrawable != null) {
            setupDrawables(seekBar.getContext(), z);
            setOutlineProvider(seekBar);
            seekBar.setProgressDrawable(this.mDrawable);
            updateDrawables();
        }
    }

    private void setupDrawables(Context context, boolean z) {
        Drawable findDrawableById = DrawableUtils.findDrawableById(this.mDrawable, 16908288);
        this.mBackground = findDrawableById;
        this.mBackgroundSegments = DrawableUtils.findDrawableById(findDrawableById, R$id.miui_volume_timer_background_segments);
        Drawable findDrawableById2 = DrawableUtils.findDrawableById(this.mDrawable, 16908301);
        this.mProgress = findDrawableById2;
        this.mProgressNormalRect = DrawableUtils.findDrawableById(findDrawableById2, R$id.miui_volume_timer_progress_normal);
        this.mProgressDraggingRect = DrawableUtils.findDrawableById(this.mProgress, R$id.miui_volume_timer_progress_dragging_rect);
        this.mProgressDraggingRectIdle = DrawableUtils.findDrawableById(this.mProgress, R$id.miui_volume_timer_progress_dragging_rect_idle);
        this.mProgressDraggingIcon = DrawableUtils.findDrawableById(this.mProgress, R$id.miui_volume_timer_progress_dragging_icon);
        if (z) {
            addTextDrawables(context);
        }
    }

    private void addTextDrawables(Context context) {
        Drawable drawable = this.mDrawable;
        if (!(drawable instanceof LayerDrawable)) {
            Log.e("VolumeTimerDrawables", "progress drawable is not a LayerDrawable");
            return;
        }
        LayerDrawable layerDrawable = (LayerDrawable) drawable;
        float dimension = context.getResources().getDimension(R$dimen.miui_volume_timer_time_text_size);
        CenterTextDrawable centerTextDrawable = new CenterTextDrawable();
        this.mTimeDrawableBg = centerTextDrawable;
        centerTextDrawable.setTextSize(dimension);
        this.mTimeDrawableBg.setTextColor(context.getResources().getColor(R$color.miui_volume_tint_dark));
        layerDrawable.setDrawableByLayerId(16908288, new LayerDrawable(new Drawable[]{this.mBackground, this.mTimeDrawableBg}));
        CenterTextDrawable centerTextDrawable2 = new CenterTextDrawable();
        this.mTimeDrawableFg = centerTextDrawable2;
        centerTextDrawable2.setTextSize(dimension);
        this.mTimeDrawableFg.setTextColor(context.getResources().getColor(R$color.miui_volume_tint_light));
        layerDrawable.setDrawableByLayerId(16908301, new LayerDrawable(new Drawable[]{this.mProgress, new ScaleDrawable(this.mTimeDrawableFg, 8388611, 1.0f, 0.0f)}));
        this.mTickingTimes.add(this.mTimeDrawableFg);
        this.mTickingTimes.add(this.mTimeDrawableBg);
    }

    private void setOutlineProvider(View view) {
        view.setOutlineProvider(new ViewOutlineProvider(this) {
            public void getOutline(View view, Outline outline) {
                Outline outline2 = outline;
                outline2.setRoundRect(0, 0, view.getWidth(), view.getHeight(), (float) (Math.min(view.getWidth(), view.getHeight()) / 2));
            }
        });
        view.setClipToOutline(true);
    }

    private void updateDrawables() {
        boolean z = true;
        DrawableAnimators.fade(this.mProgressDraggingIcon, this.mDragging || !this.mTicking);
        DrawableAnimators.fade(this.mBackgroundSegments, this.mDragging);
        DrawableAnimators.fade(this.mProgressDraggingRectIdle, !this.mDragging && !this.mTicking);
        boolean z2 = this.mTicking && !this.mDragging;
        boolean z3 = !this.mTicking && !this.mDragging;
        CenterTextDrawable centerTextDrawable = this.mTimeDrawableFg;
        if (centerTextDrawable != null) {
            DrawableAnimators.fade(centerTextDrawable, z2 && this.mCurrentSegment > 1);
        }
        CenterTextDrawable centerTextDrawable2 = this.mTimeDrawableBg;
        if (centerTextDrawable2 != null) {
            DrawableAnimators.fade(centerTextDrawable2, (z2 && this.mCurrentSegment <= 1) || z3);
        }
        if (z3) {
            updateTickingTimeText(0);
        }
        if (!z2) {
            if (this.mProgressNormalRect.getAlpha() != 255) {
                z = false;
            }
            Util.setVisOrInvis(this.mProgressNormalRect, false);
            if (z) {
                Util.setVisOrInvis(this.mProgressDraggingRect, this.mDragging);
            } else {
                DrawableAnimators.fade(this.mProgressDraggingRect, this.mDragging);
            }
            DrawableAnimators.updateCornerRadii(this.mContext, this.mProgressDraggingRect, R$array.miui_volume_progress_dragging_corners);
            DrawableAnimators.updateCornerRadii(this.mContext, this.mProgressDraggingRectIdle, R$array.miui_volume_progress_dragging_corners);
            return;
        }
        DrawableAnimators.updateCornerRadii(this.mContext, this.mProgressDraggingRectIdle, R$array.miui_volume_progress_released_corners);
        DrawableAnimators.updateCornerRadii(this.mContext, this.mProgressDraggingRect, R$array.miui_volume_progress_released_corners).addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                boolean z = MiuiVolumeTimerDrawableHelper.this.mTicking && !MiuiVolumeTimerDrawableHelper.this.mDragging;
                Util.setVisOrInvis(MiuiVolumeTimerDrawableHelper.this.mProgressNormalRect, z);
                Util.setVisOrInvis(MiuiVolumeTimerDrawableHelper.this.mProgressDraggingRect, !z);
            }
        });
    }

    public void onTouchDown() {
        this.mDragging = true;
        updateDrawables();
        updateCountDownStateText();
    }

    public void onTouchRelease() {
        this.mDragging = false;
        updateDrawables();
        updateTickingTimeText(this.mTimeRemain);
    }

    public void onSegmentChange(int i, int i2) {
        if (!(this.mCurrentSegment == i && this.mDeterminedSegment == i2)) {
            this.mCurrentSegment = i;
            this.mDeterminedSegment = i2;
            updateDrawables();
        }
        if (this.mDragging) {
            updateCountDownStateText();
        }
    }

    public void onTimeUpdate(int i) {
        this.mTimeRemain = i;
        updateTickingTimeText(i);
        boolean z = i > 0;
        if (this.mTicking != z) {
            this.mTicking = z;
            updateDrawables();
        }
    }

    public void addTickingTimeReceiver(TextView textView) {
        this.mTickingTimes.add(textView);
    }

    public void addCountDownStateReceiver(TextView textView) {
        this.mCountDownStates.add(textView);
    }

    private void updateTickingTimeText(int i) {
        String str;
        if (!this.mTicking && !this.mDragging) {
            str = this.mTimeDrawableHint;
        } else {
            str = formatRemainTime(i);
        }
        for (Object next : this.mTickingTimes) {
            if (next instanceof CenterTextDrawable) {
                ((CenterTextDrawable) next).setText(str);
            } else if (next instanceof TextView) {
                ((TextView) next).setText(str);
            }
        }
    }

    private void updateCountDownStateText() {
        String str;
        if (this.mDragging) {
            String str2 = this.mTimeSegmentTitle[Util.constrain(this.mDeterminedSegment - 1, 0, this.mTimeSegmentTitle.length - 1)];
            str = this.mContext.getResources().getString(R$string.miui_ringer_count_down_time, new Object[]{str2});
        } else {
            str = "";
        }
        for (Object next : this.mCountDownStates) {
            if (next instanceof CenterTextDrawable) {
                ((CenterTextDrawable) next).setText(str);
            } else if (next instanceof TextView) {
                ((TextView) next).setText(str);
            }
        }
    }

    private String formatRemainTime(int i) {
        int i2 = i / 60;
        return String.format(Locale.getDefault(), "%d:%02d:%02d", new Object[]{Integer.valueOf(i2 / 60), Integer.valueOf(i2 % 60), Integer.valueOf(i % 60)});
    }
}

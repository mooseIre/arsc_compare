package com.android.systemui.miui.volume;

import android.app.ExtraNotificationManager;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.provider.MiuiSettings;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.systemui.miui.DrawableAnimators;
import com.android.systemui.miui.widget.TimerSeekBar;
import com.xiaomi.stat.d;

public class MiuiRingerModeLayout extends LinearLayout implements TimerSeekBar.OnTimeUpdateListener, SeekBar.OnSeekBarChangeListener {
    /* access modifiers changed from: private */
    public Context mContext;
    private ProgressBar mCountDownProgress;
    private int mCurrentTimerMinitues;
    /* access modifiers changed from: private */
    public boolean mExpanded;
    private View mRingerBtnLayout;
    private RingerButtonHelper mRingerHelper;
    /* access modifiers changed from: private */
    public int mRingerMode;
    private View mTickingTimePortrait;
    private MiuiVolumeTimerSeekBar mTimer;
    private boolean mTimerDragging;
    private View mTimerLayout;
    /* access modifiers changed from: private */
    public boolean mTransitionRunning;
    private Runnable mUpdateTimerRunnable;

    public void cleanUp() {
    }

    public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
    }

    public void onSegmentChange(int i, int i2) {
    }

    public MiuiRingerModeLayout(Context context) {
        this(context, (AttributeSet) null);
    }

    public MiuiRingerModeLayout(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MiuiRingerModeLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mRingerMode = 0;
        this.mCurrentTimerMinitues = 0;
        this.mUpdateTimerRunnable = new Runnable() {
            public void run() {
                MiuiRingerModeLayout.this.updateRemainTimeH();
            }
        };
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        initialize();
    }

    private void initialize() {
        this.mRingerBtnLayout = findViewById(R$id.miui_ringer_btn_layout);
        this.mTimerLayout = findViewById(R$id.miui_volume_timer_layout);
        this.mTimer = (MiuiVolumeTimerSeekBar) findViewById(R$id.miui_volume_timer);
        this.mTimer.setOnTimeUpdateListener(this);
        this.mCountDownProgress = (ProgressBar) findViewById(R$id.miui_volume_count_down_progress);
        View findViewById = findViewById(R$id.miui_ringer_standard_btn);
        View findViewById2 = findViewById(R$id.miui_ringer_dnd_btn);
        this.mRingerHelper = new RingerButtonHelper(findViewById, findViewById2);
        findViewById.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!MiuiRingerModeLayout.this.mExpanded || !MiuiRingerModeLayout.this.mTransitionRunning) {
                    int i = 0;
                    MiuiRingerModeLayout.this.trackClickEvent(false);
                    MiuiRingerModeLayout miuiRingerModeLayout = MiuiRingerModeLayout.this;
                    if (miuiRingerModeLayout.mRingerMode != 4) {
                        i = 4;
                    }
                    miuiRingerModeLayout.setRingerModeByUser(i);
                    return;
                }
                Log.i("RingerModeLayout", "setSilenceMode mTransitionRunning is true.");
            }
        });
        findViewById2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!MiuiRingerModeLayout.this.mExpanded || !MiuiRingerModeLayout.this.mTransitionRunning) {
                    int i = 1;
                    MiuiRingerModeLayout.this.trackClickEvent(true);
                    MiuiRingerModeLayout miuiRingerModeLayout = MiuiRingerModeLayout.this;
                    if (miuiRingerModeLayout.mRingerMode == 1) {
                        i = 0;
                    }
                    miuiRingerModeLayout.setRingerModeByUser(i);
                    return;
                }
                Log.i("RingerModeLayout", "setSilenceMode mTransitionRunning is true.");
            }
        });
        addTickingTimeReceivers();
        this.mTimer.setOnSeekBarChangeListener(this);
        setMotionEventSplittingEnabled(false);
        setRingerModeInternal(MiuiSettings.SilenceMode.getZenMode(this.mContext));
    }

    /* access modifiers changed from: private */
    public void trackClickEvent(boolean z) {
        if (this.mExpanded) {
            VolumeEventTracker.trackClickExpandRingerBtn(z ? "dnd" : "silent");
        } else {
            VolumeEventTracker.trackClickRingerBtn(this.mRingerMode);
        }
    }

    private void addTickingTimeReceivers() {
        boolean z = this.mContext.getResources().getConfiguration().orientation == 2;
        TextView textView = (TextView) findViewById(R$id.miui_volume_timer_ticking);
        TextView textView2 = (TextView) findViewById(R$id.miui_volume_timer_ticking_port);
        if (textView != null) {
            this.mTimer.addTickingTimeReceiver(textView);
            this.mTimer.addCountDownStateReceiver(textView);
        }
        if (textView2 != null && !z) {
            this.mTimer.addCountDownStateReceiver(textView2);
            this.mTickingTimePortrait = textView2;
        }
    }

    private void setRingerModeInternal(int i) {
        this.mRingerMode = i;
        this.mRingerHelper.setRingerMode(i);
    }

    /* access modifiers changed from: private */
    public void setRingerModeByUser(final int i) {
        setRingerModeInternal(i);
        AsyncTask.execute(new Runnable() {
            public void run() {
                VolumeUtil.setSilenceMode(MiuiRingerModeLayout.this.mContext, i, MiuiRingerModeLayout.this.isSilenceModeOn() ? ExtraNotificationManager.getConditionId(MiuiRingerModeLayout.this.mContext) : null);
            }
        });
    }

    public void setSilenceMode(int i, boolean z) {
        Log.i("RingerModeLayout", "Zenmode changed " + this.mRingerMode + " -> " + i + " doAnimation:" + z);
        setRingerModeInternal(i);
        if (z) {
            if (!this.mExpanded || !this.mTransitionRunning) {
                post(new Runnable() {
                    public void run() {
                        TransitionManager.endTransitions(MiuiRingerModeLayout.this);
                        MiuiRingerModeLayout miuiRingerModeLayout = MiuiRingerModeLayout.this;
                        TransitionManager.beginDelayedTransition(miuiRingerModeLayout, miuiRingerModeLayout.getTimerLayoutTransition());
                        MiuiRingerModeLayout.this.updateExpandedStateH();
                        MiuiRingerModeLayout.this.updateRemainTimeH();
                    }
                });
            } else {
                Log.i("RingerModeLayout", "setSilenceMode mTransitionRunning is true.");
            }
        }
    }

    private void updateCountProgressH() {
        Util.setVisOrGone(this.mCountDownProgress, !this.mExpanded && isSilenceModeOn() && this.mTimer.getRemainTime() > 0);
    }

    /* access modifiers changed from: private */
    public void updateExpandedStateH() {
        this.mRingerHelper.updateState();
        updateCountProgressH();
        Util.setVisOrGone(this.mTimerLayout, isSilenceModeOn() && this.mExpanded);
    }

    private void updateDraggingStateH() {
        boolean z = this.mTickingTimePortrait != null && this.mTimerDragging;
        TransitionManager.beginDelayedTransition(this, new AutoTransition().setOrdering(0).setDuration((long) this.mContext.getResources().getInteger(R$integer.miui_volume_transition_duration_short)));
        Util.setVisOrGone(this.mRingerBtnLayout, !z);
        Util.setVisOrGone(this.mTickingTimePortrait, z);
    }

    /* access modifiers changed from: private */
    public void updateRemainTimeH() {
        updateRemainTimeH(false);
    }

    private void updateRemainTimeH(boolean z) {
        long remainTime = ExtraNotificationManager.getRemainTime(this.mContext);
        this.mTimer.updateRemainTime((int) (remainTime / 1000));
        scheduleTimerUpdateH(remainTime > 0 || z);
    }

    private void scheduleTimerUpdateH(boolean z) {
        if (!z || !this.mExpanded) {
            removeCallbacks(this.mUpdateTimerRunnable);
        } else {
            postDelayed(this.mUpdateTimerRunnable, 1000);
        }
    }

    public void updateExpandedH(boolean z) {
        TransitionManager.endTransitions(this);
        this.mExpanded = z;
        if (z) {
            updateRemainTimeH();
        } else {
            scheduleTimerUpdateH(false);
            if (this.mTimerDragging) {
                this.mTimerDragging = false;
                updateDraggingStateH();
            }
        }
        this.mRingerHelper.onExpanded(z);
        updateExpandedStateH();
    }

    private void setupCountDownProgress() {
        this.mCountDownProgress.setMax(Util.getLastTotalCountDownTime(this.mContext));
        this.mCountDownProgress.setProgress(this.mTimer.getRemainTime());
    }

    public void onTimeSet(int i) {
        Util.setLastTotalCountDownTime(this.mContext, i);
        setupCountDownProgress();
        int i2 = i / 60;
        ExtraNotificationManager.startCountDownSilenceMode(this.mContext, this.mRingerMode, i2);
        boolean z = true;
        updateRemainTimeH(true);
        if (this.mCurrentTimerMinitues != i2) {
            this.mCurrentTimerMinitues = i2;
            if (this.mRingerMode != 4) {
                z = false;
            }
            VolumeEventTracker.trackTimerRingerMode(z);
            VolumeEventTracker.trackTimerDuration(getTimerStr(i2));
        }
    }

    private String getTimerStr(int i) {
        if (i == 0) {
            return "0";
        }
        if (i < 60) {
            return i + d.V;
        }
        return (i / 60) + "h";
    }

    public void onTimeUpdate(int i) {
        this.mCountDownProgress.setProgress(i);
    }

    /* access modifiers changed from: private */
    public boolean isSilenceModeOn() {
        return this.mRingerMode > 0;
    }

    public void init() {
        setRingerModeInternal(MiuiSettings.SilenceMode.getZenMode(this.mContext));
        updateRemainTimeH();
        updateExpandedStateH();
        setupCountDownProgress();
    }

    public int getRingerMode() {
        return this.mRingerMode;
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        this.mTimerDragging = true;
        updateDraggingStateH();
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        this.mTimerDragging = false;
        updateDraggingStateH();
    }

    /* access modifiers changed from: private */
    public Transition getTimerLayoutTransition() {
        int i;
        TransitionInflater from = TransitionInflater.from(this.mContext);
        if (this.mExpanded) {
            i = R$transition.miui_volume_dialog;
        } else {
            i = R$transition.miui_volume_ringer_collapse;
        }
        return from.inflateTransition(i).addListener(new Transition.TransitionListener() {
            public void onTransitionPause(Transition transition) {
            }

            public void onTransitionResume(Transition transition) {
            }

            public void onTransitionStart(Transition transition) {
                boolean unused = MiuiRingerModeLayout.this.mTransitionRunning = true;
            }

            public void onTransitionEnd(Transition transition) {
                boolean unused = MiuiRingerModeLayout.this.mTransitionRunning = false;
            }

            public void onTransitionCancel(Transition transition) {
                boolean unused = MiuiRingerModeLayout.this.mTransitionRunning = false;
            }
        });
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        return super.dispatchTouchEvent(motionEvent);
    }

    private static class RingerButtonHelper {
        private View mDndView;
        private boolean mExpanded;
        private int mRingerMode;
        private View mStandardView;

        RingerButtonHelper(View view, View view2) {
            this.mStandardView = view;
            this.mDndView = view2;
        }

        /* access modifiers changed from: package-private */
        public void setRingerMode(int i) {
            this.mRingerMode = i;
        }

        /* access modifiers changed from: package-private */
        public void onExpanded(boolean z) {
            this.mExpanded = z;
        }

        /* access modifiers changed from: private */
        public void updateState() {
            int i;
            int i2;
            int i3 = this.mRingerMode;
            if (i3 == 0) {
                i3 = MiuiSettings.SilenceMode.getLastestQuietMode(this.mStandardView.getContext());
            }
            boolean z = false;
            this.mStandardView.setSelected(this.mRingerMode == 4);
            this.mStandardView.setActivated(!this.mExpanded);
            this.mDndView.setSelected(this.mRingerMode == 1);
            this.mDndView.setActivated(!this.mExpanded);
            Util.setVisOrGone(this.mStandardView, this.mExpanded || i3 != 1);
            View view = this.mDndView;
            if (this.mExpanded || i3 == 1) {
                z = true;
            }
            Util.setVisOrGone(view, z);
            Util.setVisOrGone(this.mStandardView.findViewById(16908310), this.mExpanded);
            Util.setVisOrGone(this.mDndView.findViewById(16908310), this.mExpanded);
            Context context = this.mDndView.getContext();
            if (this.mDndView.getBackground() instanceof GradientDrawable) {
                Drawable background = this.mDndView.getBackground();
                if (this.mExpanded) {
                    i2 = R$array.miui_volume_ringer_btn_dnd_corners;
                } else {
                    i2 = R$array.miui_volume_ringer_btn_corners_collapsed;
                }
                DrawableAnimators.updateCornerRadii(context, background, i2);
            }
            if (this.mStandardView.getBackground() instanceof GradientDrawable) {
                Drawable background2 = this.mStandardView.getBackground();
                if (this.mExpanded) {
                    i = R$array.miui_volume_ringer_btn_standard_corners;
                } else {
                    i = R$array.miui_volume_ringer_btn_corners_collapsed;
                }
                DrawableAnimators.updateCornerRadii(context, background2, i);
            }
        }
    }
}

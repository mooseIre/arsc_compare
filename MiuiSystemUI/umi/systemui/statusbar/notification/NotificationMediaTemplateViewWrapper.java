package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.metrics.LogMaker;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.NotificationMediaTemplateViewWrapper;
import com.xiaomi.stat.d;
import java.util.Timer;

public class NotificationMediaTemplateViewWrapper extends NotificationHeaderViewWrapper {
    private long mDuration = 0;
    /* access modifiers changed from: private */
    public final Handler mHandler = ((Handler) Dependency.get(Dependency.MAIN_HANDLER));
    /* access modifiers changed from: private */
    public boolean mIsSeeking = false;
    /* access modifiers changed from: private */
    public ViewGroup mMainColumnContainer;
    /* access modifiers changed from: private */
    public ViewGroup mMediaActions;
    /* access modifiers changed from: private */
    public int mMediaActionsMargin;
    /* access modifiers changed from: private */
    public int mMediaAppNameMarginTop;
    /* access modifiers changed from: private */
    public int mMediaAppNameTextSize;
    private MediaController.Callback mMediaCallback = new MediaController.Callback() {
        public void onSessionDestroyed() {
            NotificationMediaTemplateViewWrapper.this.clearTimer();
            NotificationMediaTemplateViewWrapper.this.mMediaController.unregisterCallback(this);
        }

        public void onPlaybackStateChanged(PlaybackState playbackState) {
            if (playbackState != null) {
                if (playbackState.getState() != 3) {
                    NotificationMediaTemplateViewWrapper.this.updatePlaybackUi(playbackState);
                    NotificationMediaTemplateViewWrapper.this.clearTimer();
                } else if (NotificationMediaTemplateViewWrapper.this.mSeekBarTimer == null && NotificationMediaTemplateViewWrapper.this.mSeekBarView != null && NotificationMediaTemplateViewWrapper.this.mSeekBarView.getVisibility() != 8) {
                    NotificationMediaTemplateViewWrapper.this.startTimer();
                }
            }
        }

        public void onMetadataChanged(MediaMetadata mediaMetadata) {
            if (NotificationMediaTemplateViewWrapper.this.mMediaMetadata == null || !NotificationMediaTemplateViewWrapper.this.mMediaMetadata.equals(mediaMetadata)) {
                MediaMetadata unused = NotificationMediaTemplateViewWrapper.this.mMediaMetadata = mediaMetadata;
                NotificationMediaTemplateViewWrapper.this.updateDuration();
                NotificationMediaTemplateViewWrapper.this.startTimer();
            }
        }
    };
    /* access modifiers changed from: private */
    public int mMediaContentMarginBottom;
    /* access modifiers changed from: private */
    public int mMediaContentMarginEnd;
    /* access modifiers changed from: private */
    public int mMediaContentMarginStart;
    /* access modifiers changed from: private */
    public int mMediaContentMarginTop;
    /* access modifiers changed from: private */
    public MediaController mMediaController;
    /* access modifiers changed from: private */
    public MediaMetadata mMediaMetadata;
    /* access modifiers changed from: private */
    public ImageView mMediaSeamlessButton;
    /* access modifiers changed from: private */
    public TextView mMediaText;
    /* access modifiers changed from: private */
    public int mMediaTextTextSize;
    /* access modifiers changed from: private */
    public TextView mMediaTitle;
    /* access modifiers changed from: private */
    public int mMediaTitleTextSize;
    /* access modifiers changed from: private */
    public MetricsLogger mMetricsLogger = ((MetricsLogger) Dependency.get(MetricsLogger.class));
    private boolean mOnPreDrawListenerRegistered = false;
    final Runnable mOnUpdateTimerTick = new Runnable() {
        public void run() {
            if (NotificationMediaTemplateViewWrapper.this.mMediaController == null || NotificationMediaTemplateViewWrapper.this.mSeekBar == null) {
                NotificationMediaTemplateViewWrapper.this.clearTimer();
                return;
            }
            PlaybackState playbackState = NotificationMediaTemplateViewWrapper.this.mMediaController.getPlaybackState();
            if (playbackState != null) {
                NotificationMediaTemplateViewWrapper.this.updatePlaybackUi(playbackState);
                if (playbackState.getState() == 3) {
                    NotificationMediaTemplateViewWrapper.this.addOnPreDrawListener();
                    return;
                }
                return;
            }
            NotificationMediaTemplateViewWrapper.this.clearTimer();
        }
    };
    /* access modifiers changed from: private */
    public ImageView mPicture;
    private ViewTreeObserver.OnPreDrawListener mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        public boolean onPreDraw() {
            NotificationMediaTemplateViewWrapper.this.removeOnPreDrawListener();
            NotificationMediaTemplateViewWrapper.this.mHandler.removeCallbacks(NotificationMediaTemplateViewWrapper.this.mOnUpdateTimerTick);
            NotificationMediaTemplateViewWrapper.this.mHandler.postDelayed(NotificationMediaTemplateViewWrapper.this.mOnUpdateTimerTick, 1000);
            return true;
        }
    };
    /* access modifiers changed from: private */
    public SeekBar mSeekBar;
    /* access modifiers changed from: private */
    public TextView mSeekBarElapsedTime;
    private Drawable mSeekBarThumbDrawable;
    /* access modifiers changed from: private */
    public Timer mSeekBarTimer;
    private TextView mSeekBarTotalTime;
    /* access modifiers changed from: private */
    public View mSeekBarView;
    @VisibleForTesting
    protected SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
            if (z) {
                NotificationMediaTemplateViewWrapper.this.mSeekBarElapsedTime.setText(NotificationMediaTemplateViewWrapper.this.millisecondsToTimeString((long) i));
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            boolean unused = NotificationMediaTemplateViewWrapper.this.mIsSeeking = true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            boolean unused = NotificationMediaTemplateViewWrapper.this.mIsSeeking = false;
            if (NotificationMediaTemplateViewWrapper.this.mMediaController != null) {
                NotificationMediaTemplateViewWrapper.this.mMediaController.getTransportControls().seekTo((long) NotificationMediaTemplateViewWrapper.this.mSeekBar.getProgress());
                NotificationMediaTemplateViewWrapper.this.mMetricsLogger.write(NotificationMediaTemplateViewWrapper.this.newLog(6));
            }
        }
    };
    private MediaStyleProcessor mStyleProcessor;

    /* access modifiers changed from: protected */
    public void addRemainingTransformTypes() {
    }

    protected NotificationMediaTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        initResources();
        resolveMediaViews();
        handleMediaViews();
    }

    private void initResources() {
        this.mMediaAppNameTextSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.media_notification_app_name_text_size);
        this.mMediaTitleTextSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.media_notification_title_text_size);
        this.mMediaTextTextSize = this.mContext.getResources().getDimensionPixelSize(R.dimen.media_notification_text_text_size);
        this.mMediaContentMarginStart = this.mContext.getResources().getDimensionPixelSize(R.dimen.media_notification_content_margin_start);
        this.mMediaContentMarginEnd = this.mContext.getResources().getDimensionPixelSize(R.dimen.media_notification_content_margin_end);
        this.mMediaContentMarginTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.media_notification_content_margin_top);
        this.mMediaContentMarginBottom = this.mContext.getResources().getDimensionPixelSize(R.dimen.media_notification_content_margin_bottom);
        this.mMediaAppNameMarginTop = this.mContext.getResources().getDimensionPixelSize(R.dimen.media_notification_app_name_margin_top);
        this.mMediaActionsMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.media_notification_actions_margin);
        this.mSeekBarThumbDrawable = this.mContext.getDrawable(R.drawable.media_notification_seek_thumb);
        this.mStyleProcessor = new MediaStyleProcessor();
    }

    private void resolveMediaViews() {
        this.mPicture = (ImageView) this.mView.findViewById(16909389);
        this.mMainColumnContainer = (ViewGroup) this.mView.findViewById(16909239);
        this.mMediaActions = (ViewGroup) this.mView.findViewById(16909161);
        this.mMediaTitle = (TextView) this.mView.findViewById(16908310);
        this.mMediaText = (TextView) this.mView.findViewById(16909525);
        int identifier = this.mView.getContext().getResources().getIdentifier("media_seamless", d.h, "android");
        if (identifier != 0) {
            this.mMediaSeamlessButton = (ImageView) this.mView.findViewById(identifier);
        }
    }

    private void handleMediaViews() {
        if (NotificationViewWrapper.DEBUG) {
            Log.d("NViewWrapper", "handleMediaViews");
        }
        this.mStyleProcessor.handleContainer();
        this.mStyleProcessor.handleAppNameText();
        this.mStyleProcessor.handleMainColumn();
        this.mStyleProcessor.handleTitleText();
        this.mStyleProcessor.handleActions();
        this.mStyleProcessor.handleMiuiMediaSeamlessButton();
        this.mStyleProcessor.handleRightIcon();
    }

    private void handleMediaData() {
        boolean z;
        MediaSession.Token token = (MediaSession.Token) this.mRow.getEntry().notification.getNotification().extras.getParcelable("android.mediaSession");
        if (token == null || isNormalMedia()) {
            View view = this.mSeekBarView;
            if (view != null) {
                view.setVisibility(8);
                return;
            }
            return;
        }
        MediaController mediaController = this.mMediaController;
        if (mediaController == null || !mediaController.getSessionToken().equals(token)) {
            MediaController mediaController2 = this.mMediaController;
            if (mediaController2 != null) {
                mediaController2.unregisterCallback(this.mMediaCallback);
            }
            this.mMediaController = new MediaController(this.mContext, token);
            z = true;
        } else {
            z = false;
        }
        MediaMetadata metadata = this.mMediaController.getMetadata();
        this.mMediaMetadata = metadata;
        if (metadata != null) {
            if (metadata.getLong("android.media.metadata.DURATION") <= 0) {
                View view2 = this.mSeekBarView;
                if (view2 != null && view2.getVisibility() != 8) {
                    this.mSeekBarView.setVisibility(8);
                    this.mMetricsLogger.write(newLog(2));
                    clearTimer();
                    return;
                } else if (this.mSeekBarView == null && z) {
                    this.mMetricsLogger.write(newLog(2));
                    return;
                } else {
                    return;
                }
            } else {
                View view3 = this.mSeekBarView;
                if (view3 != null && view3.getVisibility() == 8) {
                    this.mSeekBarView.setVisibility(0);
                    this.mMetricsLogger.write(newLog(1));
                    updateDuration();
                    startTimer();
                }
            }
        }
        ViewStub viewStub = (ViewStub) this.mView.findViewById(16909251);
        if (viewStub instanceof ViewStub) {
            viewStub.setLayoutInflater(LayoutInflater.from(this.mContext));
            viewStub.setLayoutResource(R.layout.notification_material_media_seekbar);
            this.mSeekBarView = viewStub.inflate();
            this.mMetricsLogger.write(newLog(1));
            SeekBar seekBar = (SeekBar) this.mSeekBarView.findViewById(R.id.media_notification_progress_bar);
            this.mSeekBar = seekBar;
            seekBar.setOnSeekBarChangeListener(this.mSeekListener);
            this.mSeekBarElapsedTime = (TextView) this.mSeekBarView.findViewById(R.id.media_notification_elapsed_time);
            this.mSeekBarTotalTime = (TextView) this.mSeekBarView.findViewById(R.id.media_notification_total_time);
            if (this.mSeekBarTimer == null) {
                MediaController mediaController3 = this.mMediaController;
                if (mediaController3 == null || !canSeekMedia(mediaController3.getPlaybackState())) {
                    setScrubberVisible(false);
                } else {
                    this.mMetricsLogger.write(newLog(3, 1));
                }
                updateDuration();
                startTimer();
                this.mMediaController.registerCallback(this.mMediaCallback);
            }
        }
        updateSeekBarTint(this.mSeekBarView);
    }

    /* access modifiers changed from: private */
    public void startTimer() {
        clearTimer();
        addOnPreDrawListener();
    }

    /* access modifiers changed from: private */
    public void addOnPreDrawListener() {
        if (!this.mOnPreDrawListenerRegistered) {
            this.mOnPreDrawListenerRegistered = true;
            this.mSeekBarView.getViewTreeObserver().addOnPreDrawListener(this.mPreDrawListener);
        }
    }

    /* access modifiers changed from: private */
    public void removeOnPreDrawListener() {
        if (this.mOnPreDrawListenerRegistered) {
            this.mSeekBarView.getViewTreeObserver().removeOnPreDrawListener(this.mPreDrawListener);
            this.mHandler.postDelayed(this.mOnUpdateTimerTick, 1000);
            this.mOnPreDrawListenerRegistered = false;
        }
    }

    /* access modifiers changed from: private */
    public void clearTimer() {
        Timer timer = this.mSeekBarTimer;
        if (timer != null) {
            timer.cancel();
            this.mSeekBarTimer.purge();
            this.mSeekBarTimer = null;
        }
        removeOnPreDrawListener();
    }

    private boolean canSeekMedia(PlaybackState playbackState) {
        return (playbackState == null || (playbackState.getActions() & 256) == 0) ? false : true;
    }

    private void setScrubberVisible(boolean z) {
        SeekBar seekBar = this.mSeekBar;
        if (seekBar != null && seekBar.isEnabled() != z) {
            this.mSeekBar.setThumb(z ? this.mSeekBarThumbDrawable : new ColorDrawable(0));
            this.mSeekBar.setEnabled(z);
            this.mMetricsLogger.write(newLog(3, z ? 1 : 0));
        }
    }

    /* access modifiers changed from: private */
    public void updateDuration() {
        MediaMetadata mediaMetadata = this.mMediaMetadata;
        if (!(mediaMetadata == null || this.mSeekBar == null)) {
            long j = mediaMetadata.getLong("android.media.metadata.DURATION");
            if (this.mDuration != j) {
                this.mDuration = j;
                this.mSeekBar.setMax((int) j);
                this.mSeekBarTotalTime.setText(millisecondsToTimeString(j));
            }
        }
        this.mSeekBarView.setVisibility(this.mDuration == 0 ? 8 : 0);
    }

    /* access modifiers changed from: private */
    public void updatePlaybackUi(PlaybackState playbackState) {
        long position = playbackState.getPosition();
        if (!this.mIsSeeking) {
            this.mSeekBar.setProgress((int) position);
            this.mSeekBarElapsedTime.setText(millisecondsToTimeString(position));
        }
        setScrubberVisible(canSeekMedia(playbackState));
    }

    /* access modifiers changed from: private */
    public String millisecondsToTimeString(long j) {
        return DateUtils.formatElapsedTime(j / 1000);
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        handleMediaViews();
        handleMediaData();
        super.onContentUpdated(expandableNotificationRow);
    }

    private void updateSeekBarTint(View view) {
        if (view != null && getNotificationHeader() != null) {
            int originalIconColor = getNotificationHeader().getOriginalIconColor();
            this.mSeekBarElapsedTime.setTextColor(originalIconColor);
            this.mSeekBarTotalTime.setTextColor(originalIconColor);
            this.mSeekBarTotalTime.setShadowLayer(1.5f, 1.5f, 1.5f, this.mBackgroundColor);
            ColorStateList valueOf = ColorStateList.valueOf(originalIconColor);
            this.mSeekBar.setThumbTintList(valueOf);
            ColorStateList withAlpha = valueOf.withAlpha(192);
            this.mSeekBar.setProgressTintList(withAlpha);
            this.mSeekBar.setProgressBackgroundTintList(withAlpha.withAlpha(128));
        }
    }

    /* access modifiers changed from: protected */
    public void updateTransformedTypes() {
        ImageView imageView = this.mPicture;
        if (imageView != null) {
            this.mTransformationHelper.addTransformedView(3, imageView);
        }
        ViewGroup viewGroup = this.mMediaActions;
        if (viewGroup != null) {
            this.mTransformationHelper.addTransformedView(5, viewGroup);
        }
    }

    /* access modifiers changed from: private */
    public LogMaker newLog(int i) {
        return new LogMaker(1743).setType(i).setPackageName(this.mRow.getEntry().notification.getPackageName());
    }

    private LogMaker newLog(int i, int i2) {
        return new LogMaker(1743).setType(i).setSubtype(i2).setPackageName(this.mRow.getEntry().notification.getPackageName());
    }

    /* access modifiers changed from: private */
    public boolean isNormalMedia() {
        return this.mView.getId() == 16909501 && "media".equals(this.mView.getTag());
    }

    private class MediaStyleProcessor {
        private MediaStyleProcessor() {
        }

        /* access modifiers changed from: package-private */
        public void handleContainer() {
            View view = (View) NotificationMediaTemplateViewWrapper.this.mMainColumnContainer.getParent();
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
            marginLayoutParams.topMargin = NotificationMediaTemplateViewWrapper.this.mMediaContentMarginTop;
            marginLayoutParams.bottomMargin = NotificationMediaTemplateViewWrapper.this.mMediaContentMarginBottom;
            view.setLayoutParams(marginLayoutParams);
        }

        /* access modifiers changed from: package-private */
        public void handleAppNameText() {
            NotificationMediaTemplateViewWrapper notificationMediaTemplateViewWrapper = NotificationMediaTemplateViewWrapper.this;
            notificationMediaTemplateViewWrapper.mAppNameText.setTextSize(0, (float) notificationMediaTemplateViewWrapper.mMediaAppNameTextSize);
            NotificationMediaTemplateViewWrapper notificationMediaTemplateViewWrapper2 = NotificationMediaTemplateViewWrapper.this;
            if (!notificationMediaTemplateViewWrapper2.isHeaderViewRemoved(notificationMediaTemplateViewWrapper2.mAppNameText)) {
                NotificationMediaTemplateViewWrapper notificationMediaTemplateViewWrapper3 = NotificationMediaTemplateViewWrapper.this;
                notificationMediaTemplateViewWrapper3.mNotificationHeader.removeView(notificationMediaTemplateViewWrapper3.mAppNameText);
            }
            NotificationMediaTemplateViewWrapper notificationMediaTemplateViewWrapper4 = NotificationMediaTemplateViewWrapper.this;
            if (((ViewGroup) notificationMediaTemplateViewWrapper4.mView).indexOfChild(notificationMediaTemplateViewWrapper4.mAppNameText) < 0) {
                NotificationMediaTemplateViewWrapper notificationMediaTemplateViewWrapper5 = NotificationMediaTemplateViewWrapper.this;
                ((ViewGroup) notificationMediaTemplateViewWrapper5.mView).addView(notificationMediaTemplateViewWrapper5.mAppNameText);
            }
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) NotificationMediaTemplateViewWrapper.this.mAppNameText.getLayoutParams();
            layoutParams.leftMargin = NotificationMediaTemplateViewWrapper.this.mMediaContentMarginStart;
            layoutParams.topMargin = NotificationMediaTemplateViewWrapper.this.mMediaAppNameMarginTop;
            NotificationMediaTemplateViewWrapper.this.mAppNameText.setLayoutParams(layoutParams);
        }

        /* access modifiers changed from: package-private */
        public void handleMainColumn() {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) NotificationMediaTemplateViewWrapper.this.mMainColumnContainer.getLayoutParams();
            marginLayoutParams.leftMargin = NotificationMediaTemplateViewWrapper.this.mMediaContentMarginStart;
            marginLayoutParams.rightMargin = NotificationMediaTemplateViewWrapper.this.isNormalMedia() ? NotificationMediaTemplateViewWrapper.this.mMediaContentMarginEnd : marginLayoutParams.rightMargin;
            NotificationMediaTemplateViewWrapper.this.mMainColumnContainer.setLayoutParams(marginLayoutParams);
        }

        /* access modifiers changed from: package-private */
        public void handleTitleText() {
            if (NotificationMediaTemplateViewWrapper.this.mMediaTitle != null) {
                NotificationMediaTemplateViewWrapper.this.mMediaTitle.setTextSize(0, (float) NotificationMediaTemplateViewWrapper.this.mMediaTitleTextSize);
            }
            if (NotificationMediaTemplateViewWrapper.this.mMediaText != null) {
                NotificationMediaTemplateViewWrapper.this.mMediaText.setTextSize(0, (float) NotificationMediaTemplateViewWrapper.this.mMediaTextTextSize);
                NotificationMediaTemplateViewWrapper.this.mMediaText.setSingleLine(true);
            }
        }

        /* access modifiers changed from: package-private */
        public void handleActions() {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) NotificationMediaTemplateViewWrapper.this.mMediaActions.getLayoutParams();
            marginLayoutParams.leftMargin = NotificationMediaTemplateViewWrapper.this.mMediaActionsMargin;
            marginLayoutParams.topMargin = NotificationMediaTemplateViewWrapper.this.mMediaActionsMargin;
            marginLayoutParams.rightMargin = NotificationMediaTemplateViewWrapper.this.mMediaActionsMargin;
            marginLayoutParams.bottomMargin = NotificationMediaTemplateViewWrapper.this.isNormalMedia() ? 0 : NotificationMediaTemplateViewWrapper.this.mMediaActionsMargin;
            NotificationMediaTemplateViewWrapper.this.mMediaActions.setLayoutParams(marginLayoutParams);
        }

        /* access modifiers changed from: package-private */
        public void handleMiuiMediaSeamlessButton() {
            int identifier;
            if (NotificationMediaTemplateViewWrapper.this.mMediaSeamlessButton != null && (identifier = NotificationMediaTemplateViewWrapper.this.mView.getContext().getResources().getIdentifier("ic_media_seamless", "drawable", "android")) != 0) {
                NotificationMediaTemplateViewWrapper.this.mMediaSeamlessButton.setImageResource(identifier);
                NotificationMediaTemplateViewWrapper.this.mMediaSeamlessButton.setImageTintList(ColorStateList.valueOf(NotificationMediaTemplateViewWrapper.this.getNotificationHeader().getOriginalIconColor()));
            }
        }

        /* access modifiers changed from: package-private */
        public void handleRightIcon() {
            NotificationMediaTemplateViewWrapper.this.mPicture.post(new Runnable() {
                public final void run() {
                    NotificationMediaTemplateViewWrapper.MediaStyleProcessor.this.lambda$handleRightIcon$0$NotificationMediaTemplateViewWrapper$MediaStyleProcessor();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$handleRightIcon$0 */
        public /* synthetic */ void lambda$handleRightIcon$0$NotificationMediaTemplateViewWrapper$MediaStyleProcessor() {
            ViewGroup.LayoutParams layoutParams = NotificationMediaTemplateViewWrapper.this.mPicture.getLayoutParams();
            layoutParams.width = NotificationMediaTemplateViewWrapper.this.mView.getMeasuredHeight();
            layoutParams.height = NotificationMediaTemplateViewWrapper.this.mView.getMeasuredHeight();
            NotificationMediaTemplateViewWrapper.this.mPicture.setLayoutParams(layoutParams);
        }
    }
}

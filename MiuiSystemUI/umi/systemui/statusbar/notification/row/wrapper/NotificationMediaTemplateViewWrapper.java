package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.content.res.ColorStateList;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.metrics.LogMaker;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.view.ViewTreeObserver;
import android.widget.SeekBar;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.widget.MediaNotificationView;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import java.util.Timer;

public class NotificationMediaTemplateViewWrapper extends NotificationTemplateViewWrapper {
    private View mActions;
    private View.OnAttachStateChangeListener mAttachStateListener = new View.OnAttachStateChangeListener() {
        /* class com.android.systemui.statusbar.notification.row.wrapper.NotificationMediaTemplateViewWrapper.AnonymousClass3 */

        public void onViewAttachedToWindow(View view) {
        }

        public void onViewDetachedFromWindow(View view) {
            NotificationMediaTemplateViewWrapper.this.mIsViewVisible = false;
        }
    };
    private Context mContext;
    private long mDuration = 0;
    private final Handler mHandler = ((Handler) Dependency.get(Dependency.MAIN_HANDLER));
    private boolean mIsViewVisible;
    private MediaController.Callback mMediaCallback = new MediaController.Callback() {
        /* class com.android.systemui.statusbar.notification.row.wrapper.NotificationMediaTemplateViewWrapper.AnonymousClass4 */

        public void onSessionDestroyed() {
            NotificationMediaTemplateViewWrapper.this.clearTimer();
            NotificationMediaTemplateViewWrapper.this.mMediaController.unregisterCallback(this);
            NotificationMediaTemplateViewWrapper notificationMediaTemplateViewWrapper = NotificationMediaTemplateViewWrapper.this;
            MediaNotificationView mediaNotificationView = notificationMediaTemplateViewWrapper.mView;
            if (mediaNotificationView instanceof MediaNotificationView) {
                mediaNotificationView.removeVisibilityListener(notificationMediaTemplateViewWrapper.mVisibilityListener);
                NotificationMediaTemplateViewWrapper notificationMediaTemplateViewWrapper2 = NotificationMediaTemplateViewWrapper.this;
                notificationMediaTemplateViewWrapper2.mView.removeOnAttachStateChangeListener(notificationMediaTemplateViewWrapper2.mAttachStateListener);
            }
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
                NotificationMediaTemplateViewWrapper.this.mMediaMetadata = mediaMetadata;
                NotificationMediaTemplateViewWrapper.this.updateDuration();
            }
        }
    };
    private MediaController mMediaController;
    private NotificationMediaManager mMediaManager;
    private MediaMetadata mMediaMetadata;
    private MetricsLogger mMetricsLogger;
    private boolean mOnPreDrawListenerRegistered = false;
    protected final Runnable mOnUpdateTimerTick = new Runnable() {
        /* class com.android.systemui.statusbar.notification.row.wrapper.NotificationMediaTemplateViewWrapper.AnonymousClass6 */

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
    private ViewTreeObserver.OnPreDrawListener mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
        /* class com.android.systemui.statusbar.notification.row.wrapper.NotificationMediaTemplateViewWrapper.AnonymousClass5 */

        public boolean onPreDraw() {
            NotificationMediaTemplateViewWrapper.this.removeOnPreDrawListener();
            NotificationMediaTemplateViewWrapper.this.mHandler.removeCallbacks(NotificationMediaTemplateViewWrapper.this.mOnUpdateTimerTick);
            NotificationMediaTemplateViewWrapper.this.mHandler.postDelayed(NotificationMediaTemplateViewWrapper.this.mOnUpdateTimerTick, 1000);
            return true;
        }
    };
    private SeekBar mSeekBar;
    private TextView mSeekBarElapsedTime;
    private Timer mSeekBarTimer;
    private TextView mSeekBarTotalTime;
    private View mSeekBarView;
    @VisibleForTesting
    protected SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {
        /* class com.android.systemui.statusbar.notification.row.wrapper.NotificationMediaTemplateViewWrapper.AnonymousClass1 */

        public void onProgressChanged(SeekBar seekBar, int i, boolean z) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (NotificationMediaTemplateViewWrapper.this.mMediaController != null) {
                NotificationMediaTemplateViewWrapper.this.mMediaController.getTransportControls().seekTo((long) NotificationMediaTemplateViewWrapper.this.mSeekBar.getProgress());
                NotificationMediaTemplateViewWrapper.this.mMetricsLogger.write(NotificationMediaTemplateViewWrapper.this.newLog(6));
            }
        }
    };
    private MediaNotificationView.VisibilityChangeListener mVisibilityListener = new MediaNotificationView.VisibilityChangeListener() {
        /* class com.android.systemui.statusbar.notification.row.wrapper.NotificationMediaTemplateViewWrapper.AnonymousClass2 */

        public void onAggregatedVisibilityChanged(boolean z) {
            NotificationMediaTemplateViewWrapper.this.mIsViewVisible = z;
            if (!z || NotificationMediaTemplateViewWrapper.this.mMediaController == null) {
                NotificationMediaTemplateViewWrapper.this.clearTimer();
                return;
            }
            PlaybackState playbackState = NotificationMediaTemplateViewWrapper.this.mMediaController.getPlaybackState();
            if (playbackState != null && playbackState.getState() == 3 && NotificationMediaTemplateViewWrapper.this.mSeekBarTimer == null && NotificationMediaTemplateViewWrapper.this.mSeekBarView != null && NotificationMediaTemplateViewWrapper.this.mSeekBarView.getVisibility() != 8) {
                NotificationMediaTemplateViewWrapper.this.startTimer();
            }
        }
    };

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public boolean shouldClipToRounding(boolean z, boolean z2) {
        return true;
    }

    protected NotificationMediaTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        this.mContext = context;
        this.mMediaManager = (NotificationMediaManager) Dependency.get(NotificationMediaManager.class);
        this.mMetricsLogger = (MetricsLogger) Dependency.get(MetricsLogger.class);
    }

    private void resolveViews() {
        boolean z;
        this.mActions = this.mView.findViewById(16909161);
        this.mIsViewVisible = this.mView.isShown();
        MediaSession.Token token = (MediaSession.Token) this.mRow.getEntry().getSbn().getNotification().extras.getParcelable("android.mediaSession");
        boolean showCompactMediaSeekbar = this.mMediaManager.getShowCompactMediaSeekbar();
        if (token == null || ("media".equals(this.mView.getTag()) && !showCompactMediaSeekbar)) {
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
        ViewStub viewStub = (ViewStub) this.mView.findViewById(16909250);
        if (viewStub instanceof ViewStub) {
            viewStub.setLayoutInflater(LayoutInflater.from(viewStub.getContext()));
            viewStub.setLayoutResource(17367206);
            this.mSeekBarView = viewStub.inflate();
            this.mMetricsLogger.write(newLog(1));
            SeekBar seekBar = (SeekBar) this.mSeekBarView.findViewById(16909248);
            this.mSeekBar = seekBar;
            seekBar.setOnSeekBarChangeListener(this.mSeekListener);
            this.mSeekBarElapsedTime = (TextView) this.mSeekBarView.findViewById(16909246);
            this.mSeekBarTotalTime = (TextView) this.mSeekBarView.findViewById(16909251);
            z = true;
        }
        if (z) {
            MediaNotificationView mediaNotificationView = this.mView;
            if (mediaNotificationView instanceof MediaNotificationView) {
                mediaNotificationView.addVisibilityListener(this.mVisibilityListener);
                this.mView.addOnAttachStateChangeListener(this.mAttachStateListener);
            }
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
    /* access modifiers changed from: public */
    private void startTimer() {
        clearTimer();
        if (this.mIsViewVisible) {
            addOnPreDrawListener();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addOnPreDrawListener() {
        if (!this.mOnPreDrawListenerRegistered) {
            this.mOnPreDrawListenerRegistered = true;
            this.mSeekBarView.getViewTreeObserver().addOnPreDrawListener(this.mPreDrawListener);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeOnPreDrawListener() {
        if (this.mOnPreDrawListenerRegistered) {
            this.mSeekBarView.getViewTreeObserver().removeOnPreDrawListener(this.mPreDrawListener);
            this.mHandler.postDelayed(this.mOnUpdateTimerTick, 1000);
            this.mOnPreDrawListenerRegistered = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void clearTimer() {
        Timer timer = this.mSeekBarTimer;
        if (timer != null) {
            timer.cancel();
            this.mSeekBarTimer.purge();
            this.mSeekBarTimer = null;
        }
        removeOnPreDrawListener();
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public void setRemoved() {
        clearTimer();
        MediaController mediaController = this.mMediaController;
        if (mediaController != null) {
            mediaController.unregisterCallback(this.mMediaCallback);
        }
        MediaNotificationView mediaNotificationView = this.mView;
        if (mediaNotificationView instanceof MediaNotificationView) {
            mediaNotificationView.removeVisibilityListener(this.mVisibilityListener);
            this.mView.removeOnAttachStateChangeListener(this.mAttachStateListener);
        }
    }

    private boolean canSeekMedia(PlaybackState playbackState) {
        return (playbackState == null || (playbackState.getActions() & 256) == 0) ? false : true;
    }

    private void setScrubberVisible(boolean z) {
        SeekBar seekBar = this.mSeekBar;
        if (seekBar != null && seekBar.isEnabled() != z) {
            this.mSeekBar.getThumb().setAlpha(z ? 255 : 0);
            this.mSeekBar.setEnabled(z);
            this.mMetricsLogger.write(newLog(3, z ? 1 : 0));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDuration() {
        MediaMetadata mediaMetadata = this.mMediaMetadata;
        if (mediaMetadata != null && this.mSeekBar != null) {
            long j = mediaMetadata.getLong("android.media.metadata.DURATION");
            if (this.mDuration != j) {
                this.mDuration = j;
                this.mSeekBar.setMax((int) j);
                this.mSeekBarTotalTime.setText(millisecondsToTimeString(j));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePlaybackUi(PlaybackState playbackState) {
        if (this.mSeekBar != null && this.mSeekBarElapsedTime != null) {
            long position = playbackState.getPosition();
            this.mSeekBar.setProgress((int) position);
            this.mSeekBarElapsedTime.setText(millisecondsToTimeString(position));
            setScrubberVisible(canSeekMedia(playbackState));
        }
    }

    private String millisecondsToTimeString(long j) {
        return DateUtils.formatElapsedTime(j / 1000);
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper
    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        resolveViews();
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
    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationTemplateViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper
    public void updateTransformedTypes() {
        super.updateTransformedTypes();
        View view = this.mActions;
        if (view != null) {
            this.mTransformationHelper.addTransformedView(5, view);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper
    public boolean isDimmable() {
        return getCustomBackgroundColor() == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private LogMaker newLog(int i) {
        return new LogMaker(1743).setType(i).setPackageName(this.mRow.getEntry().getSbn().getPackageName());
    }

    private LogMaker newLog(int i, int i2) {
        return new LogMaker(1743).setType(i).setSubtype(i2).setPackageName(this.mRow.getEntry().getSbn().getPackageName());
    }
}

package com.android.systemui.statusbar.notification.mediacontrol;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.res.ColorStateList;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.AsyncTask;
import android.util.ArraySet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import androidx.constraintlayout.widget.ConstraintSet;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.Dependency;
import com.android.systemui.media.MediaAction;
import com.android.systemui.media.MediaControlPanel;
import com.android.systemui.media.MediaData;
import com.android.systemui.media.MediaViewController;
import com.android.systemui.media.PlayerViewHolder;
import com.android.systemui.media.SeekBarViewModel;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.notification.mediacontrol.ProcessArtworkTask;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import miuix.animation.Folme;
import miuix.animation.base.AnimConfig;

public class MiuiMediaControlPanel extends MediaControlPanel {
    private int direction;
    private final MiuiMediaTransferManager mMediaTransferManager;
    private final Set<AsyncTask<?, ?, ?>> mProcessArtworkTasks = new ArraySet();

    public MiuiMediaControlPanel(Context context, Executor executor, ActivityStarter activityStarter, MediaViewController mediaViewController, SeekBarViewModel seekBarViewModel, MiuiMediaTransferManager miuiMediaTransferManager) {
        super(context, executor, activityStarter, mediaViewController, seekBarViewModel);
        this.mMediaTransferManager = miuiMediaTransferManager;
        this.direction = this.mContext.getResources().getConfiguration().getLayoutDirection();
    }

    @Override // com.android.systemui.media.MediaControlPanel
    public void attach(PlayerViewHolder playerViewHolder) {
        super.attach(playerViewHolder);
        this.mMediaTransferManager.applyMediaTransferView(playerViewHolder.getSeamless());
    }

    @Override // com.android.systemui.media.MediaControlPanel
    public void bind(MediaData mediaData) {
        PlayerViewHolder view = getView();
        if (view != null) {
            clearProcessArtworkTasks();
            refreshTokenAndController(mediaData);
            ConstraintSet expandedLayout = this.mMediaViewController.getExpandedLayout();
            ConstraintSet collapsedLayout = this.mMediaViewController.getCollapsedLayout();
            setClickAction(mediaData, view);
            setArtwork(mediaData);
            setInfoText(mediaData, view);
            setSeamless(mediaData, view, expandedLayout, collapsedLayout);
            setMediaActions(mediaData, view, expandedLayout, collapsedLayout);
            setSeekBar();
        }
    }

    private void clearProcessArtworkTasks() {
        for (AsyncTask<?, ?, ?> asyncTask : this.mProcessArtworkTasks) {
            asyncTask.cancel(true);
        }
        this.mProcessArtworkTasks.clear();
    }

    private void refreshTokenAndController(MediaData mediaData) {
        MediaSession.Token token = mediaData.getToken();
        this.mBackgroundColor = mediaData.getBackgroundColor();
        MediaSession.Token token2 = this.mToken;
        if (token2 == null || !token2.equals(token)) {
            this.mToken = token;
        }
        if (this.mToken != null) {
            this.mController = new MediaController(this.mContext, this.mToken);
        } else {
            this.mController = null;
        }
    }

    private void setClickAction(MediaData mediaData, PlayerViewHolder playerViewHolder) {
        PendingIntent clickIntent = mediaData.getClickIntent();
        if (clickIntent != null) {
            playerViewHolder.getPlayer().setOnClickListener(new View.OnClickListener(clickIntent) {
                /* class com.android.systemui.statusbar.notification.mediacontrol.$$Lambda$MiuiMediaControlPanel$OcZR49VGKdEWA7KEXttm40Y0OSs */
                public final /* synthetic */ PendingIntent f$1;

                {
                    this.f$1 = r2;
                }

                public final void onClick(View view) {
                    MiuiMediaControlPanel.this.lambda$setClickAction$0$MiuiMediaControlPanel(this.f$1, view);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setClickAction$0 */
    public /* synthetic */ void lambda$setClickAction$0$MiuiMediaControlPanel(PendingIntent pendingIntent, View view) {
        this.mActivityStarter.postStartActivityDismissingKeyguard(pendingIntent);
    }

    private void setArtwork(MediaData mediaData) {
        if (mediaData.getArtwork() != null) {
            this.mProcessArtworkTasks.add(new ProcessArtworkTask(this.direction, this).executeOnExecutor(this.mBackgroundExecutor, mediaData.getArtwork().loadDrawable(this.mContext)));
        }
    }

    private void setInfoText(MediaData mediaData, PlayerViewHolder playerViewHolder) {
        playerViewHolder.getTitleText().setText(mediaData.getSong());
        playerViewHolder.getAppName().setText(mediaData.getApp());
        playerViewHolder.getArtistText().setText(mediaData.getArtist());
    }

    private void setSeamless(MediaData mediaData, PlayerViewHolder playerViewHolder, ConstraintSet constraintSet, ConstraintSet constraintSet2) {
        playerViewHolder.getSeamless().setVisibility(0);
        setVisibleAndAlpha(constraintSet2, C0015R$id.media_seamless, true);
        setVisibleAndAlpha(constraintSet, C0015R$id.media_seamless, true);
        int id = playerViewHolder.getSeamless().getId();
        float f = mediaData.getResumption() ? 0.38f : 1.0f;
        constraintSet.setAlpha(id, f);
        constraintSet2.setAlpha(id, f);
        playerViewHolder.getSeamless().setEnabled(!mediaData.getResumption());
    }

    private void setMediaActions(MediaData mediaData, PlayerViewHolder playerViewHolder, ConstraintSet constraintSet, ConstraintSet constraintSet2) {
        int[] iArr = MediaControlPanel.ACTION_IDS;
        List<Integer> actionsToShowInCompact = mediaData.getActionsToShowInCompact();
        List<MediaAction> actions = mediaData.getActions();
        int i = 0;
        int i2 = 0;
        while (i < actions.size() && i < iArr.length) {
            int i3 = iArr[i];
            ImageButton action = playerViewHolder.getAction(i3);
            MediaAction mediaAction = actions.get(i);
            action.setImageDrawable(mediaAction.getDrawable());
            action.setContentDescription(mediaAction.getContentDescription());
            Notification.Action notificationAction = mediaAction.getNotificationAction();
            Runnable action2 = mediaAction.getAction();
            if (notificationAction != null) {
                enableActionButton(action, new View.OnClickListener(notificationAction, action) {
                    /* class com.android.systemui.statusbar.notification.mediacontrol.$$Lambda$MiuiMediaControlPanel$37drBkuRL76GGV6bNhqhR5LC18 */
                    public final /* synthetic */ Notification.Action f$0;
                    public final /* synthetic */ ImageButton f$1;

                    {
                        this.f$0 = r1;
                        this.f$1 = r2;
                    }

                    public final void onClick(View view) {
                        Notification.Action action;
                        ((NotificationRemoteInputManager) Dependency.get(NotificationRemoteInputManager.class)).getRemoteViewsOnClickHandler().onClickHandler(this.f$1, action.actionIntent, RemoteViews.RemoteResponse.fromPendingIntent(this.f$0.actionIntent));
                    }
                });
            } else if (action2 != null) {
                enableActionButton(action, new View.OnClickListener(action2) {
                    /* class com.android.systemui.statusbar.notification.mediacontrol.$$Lambda$MiuiMediaControlPanel$SmuBjPadan0Q8RJ_PKy9AlL7YwQ */
                    public final /* synthetic */ Runnable f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void onClick(View view) {
                        this.f$0.run();
                    }
                });
            } else {
                action.setEnabled(false);
            }
            boolean contains = actionsToShowInCompact.contains(Integer.valueOf(i));
            if (contains) {
                i2++;
            }
            setVisibleAndAlpha(constraintSet2, i3, contains);
            setVisibleAndAlpha(constraintSet, i3, true);
            i++;
        }
        constraintSet2.constrainWidth(C0015R$id.actions, this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.media_control_collapsed_gap) * i2);
        constraintSet.constrainWidth(C0015R$id.actions, this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.media_control_expanded_gap) * i);
        while (i < iArr.length) {
            setVisibleAndAlpha(constraintSet, iArr[i], false);
            setVisibleAndAlpha(constraintSet2, iArr[i], false);
            i++;
        }
    }

    private void enableActionButton(ImageButton imageButton, View.OnClickListener onClickListener) {
        if (imageButton != null && onClickListener != null) {
            imageButton.setEnabled(true);
            imageButton.setOnClickListener(onClickListener);
            Folme.useAt(imageButton).touch().handleTouchOf(imageButton, new AnimConfig[0]);
        }
    }

    private void setSeekBar() {
        this.mBackgroundExecutor.execute(new Runnable(getController()) {
            /* class com.android.systemui.statusbar.notification.mediacontrol.$$Lambda$MiuiMediaControlPanel$Gmc0cHXP2xFB6psxtQX0_kRpEQ */
            public final /* synthetic */ MediaController f$1;

            {
                this.f$1 = r2;
            }

            public final void run() {
                MiuiMediaControlPanel.this.lambda$setSeekBar$3$MiuiMediaControlPanel(this.f$1);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setSeekBar$3 */
    public /* synthetic */ void lambda$setSeekBar$3$MiuiMediaControlPanel(MediaController mediaController) {
        this.mSeekBarViewModel.updateController(mediaController);
    }

    public void setForegroundColors(ProcessArtworkTask.Result result) {
        int i = result.primaryTextColor;
        int i2 = result.secondaryTextColor;
        PlayerViewHolder view = getView();
        if (view != null) {
            view.getTitleText().setTextColor(i);
            view.getAppName().setTextColor(i);
            view.getArtistText().setTextColor(i2);
            view.getElapsedTimeView().setTextColor(i);
            view.getTotalTimeView().setTextColor(i);
            ColorStateList valueOf = ColorStateList.valueOf(i);
            view.getAction0().setImageTintList(valueOf);
            view.getAction1().setImageTintList(valueOf);
            view.getAction2().setImageTintList(valueOf);
            view.getAction3().setImageTintList(valueOf);
            view.getAction4().setImageTintList(valueOf);
            view.getSeekBar().setThumbTintList(valueOf);
            ColorStateList withAlpha = valueOf.withAlpha(192);
            view.getSeekBar().setProgressTintList(withAlpha);
            view.getSeekBar().setProgressBackgroundTintList(withAlpha.withAlpha(128));
        }
    }

    public void removeTask(AsyncTask<?, ?, ?> asyncTask) {
        this.mProcessArtworkTasks.remove(asyncTask);
    }

    @Override // com.android.systemui.media.MediaControlPanel
    public void onDestroy() {
        super.onDestroy();
        if (getView() != null) {
            this.mMediaTransferManager.setRemoved(getView().getSeamless());
        }
    }
}

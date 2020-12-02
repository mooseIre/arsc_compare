package com.android.systemui.statusbar.notification.mediacontrol;

import android.app.PendingIntent;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.os.AsyncTask;
import android.util.ArraySet;
import android.view.View;
import android.widget.ImageButton;
import androidx.constraintlayout.widget.ConstraintSet;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.media.MediaAction;
import com.android.systemui.media.MediaControlPanel;
import com.android.systemui.media.MediaData;
import com.android.systemui.media.MediaViewController;
import com.android.systemui.media.PlayerViewHolder;
import com.android.systemui.media.SeekBarViewModel;
import com.android.systemui.plugins.ActivityStarter;
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

    public void attach(PlayerViewHolder playerViewHolder) {
        super.attach(playerViewHolder);
        this.mMediaTransferManager.applyMediaTransferView(playerViewHolder.getSeamless());
    }

    public void bind(MediaData mediaData) {
        int[] iArr = MediaControlPanel.ACTION_IDS;
        PlayerViewHolder view = getView();
        if (view != null) {
            for (AsyncTask<?, ?, ?> cancel : this.mProcessArtworkTasks) {
                cancel.cancel(true);
            }
            this.mProcessArtworkTasks.clear();
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
            ConstraintSet expandedLayout = this.mMediaViewController.getExpandedLayout();
            ConstraintSet collapsedLayout = this.mMediaViewController.getCollapsedLayout();
            PendingIntent clickIntent = mediaData.getClickIntent();
            if (clickIntent != null) {
                view.getPlayer().setOnClickListener(new View.OnClickListener(clickIntent) {
                    public final /* synthetic */ PendingIntent f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onClick(View view) {
                        MiuiMediaControlPanel.this.lambda$bind$0$MiuiMediaControlPanel(this.f$1, view);
                    }
                });
            }
            if (mediaData.getArtwork() != null) {
                this.mProcessArtworkTasks.add(new ProcessArtworkTask(this.direction, this).executeOnExecutor(this.mBackgroundExecutor, new Drawable[]{mediaData.getArtwork().loadDrawable(this.mContext)}));
            }
            view.getTitleText().setText(mediaData.getSong());
            view.getAppName().setText(mediaData.getApp());
            view.getArtistText().setText(mediaData.getArtist());
            view.getSeamless().setVisibility(0);
            setVisibleAndAlpha(collapsedLayout, C0015R$id.media_seamless, true);
            setVisibleAndAlpha(expandedLayout, C0015R$id.media_seamless, true);
            int id = view.getSeamless().getId();
            float f = mediaData.getResumption() ? 0.38f : 1.0f;
            expandedLayout.setAlpha(id, f);
            collapsedLayout.setAlpha(id, f);
            view.getSeamless().setEnabled(!mediaData.getResumption());
            List<Integer> actionsToShowInCompact = mediaData.getActionsToShowInCompact();
            List<MediaAction> actions = mediaData.getActions();
            int i = 0;
            int i2 = 0;
            while (i < actions.size() && i < iArr.length) {
                int i3 = iArr[i];
                ImageButton action = view.getAction(i3);
                MediaAction mediaAction = actions.get(i);
                action.setImageDrawable(mediaAction.getDrawable());
                action.setContentDescription(mediaAction.getContentDescription());
                Runnable action2 = mediaAction.getAction();
                if (action2 == null) {
                    action.setEnabled(false);
                } else {
                    action.setEnabled(true);
                    action.setOnClickListener(new View.OnClickListener(action2) {
                        public final /* synthetic */ Runnable f$0;

                        {
                            this.f$0 = r1;
                        }

                        public final void onClick(View view) {
                            this.f$0.run();
                        }
                    });
                    Folme.useAt(action).touch().handleTouchOf(action, new AnimConfig[0]);
                }
                boolean contains = actionsToShowInCompact.contains(Integer.valueOf(i));
                if (contains) {
                    i2++;
                }
                setVisibleAndAlpha(collapsedLayout, i3, contains);
                setVisibleAndAlpha(expandedLayout, i3, true);
                i++;
            }
            collapsedLayout.constrainWidth(C0015R$id.actions, this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.media_control_collapsed_gap) * i2);
            expandedLayout.constrainWidth(C0015R$id.actions, this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.media_control_expanded_gap) * i);
            while (i < iArr.length) {
                setVisibleAndAlpha(expandedLayout, iArr[i], false);
                setVisibleAndAlpha(collapsedLayout, iArr[i], false);
                i++;
            }
            this.mBackgroundExecutor.execute(new Runnable(getController()) {
                public final /* synthetic */ MediaController f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MiuiMediaControlPanel.this.lambda$bind$2$MiuiMediaControlPanel(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bind$0 */
    public /* synthetic */ void lambda$bind$0$MiuiMediaControlPanel(PendingIntent pendingIntent, View view) {
        this.mActivityStarter.postStartActivityDismissingKeyguard(pendingIntent);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bind$2 */
    public /* synthetic */ void lambda$bind$2$MiuiMediaControlPanel(MediaController mediaController) {
        this.mSeekBarViewModel.updateController(mediaController);
    }

    public void setForegroundColor(int i) {
        PlayerViewHolder view = getView();
        if (view != null) {
            view.getTitleText().setTextColor(i);
            view.getAppName().setTextColor(i);
            view.getArtistText().setTextColor(i);
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

    public void onDestroy() {
        super.onDestroy();
        if (getView() != null) {
            this.mMediaTransferManager.setRemoved(getView().getSeamless());
        }
    }
}

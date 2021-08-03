package com.android.systemui.media;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.util.Log;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintSet;
import com.android.settingslib.Utils;
import com.android.settingslib.widget.AdaptiveIcon;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.util.animation.TransitionLayout;
import java.util.List;
import java.util.concurrent.Executor;

public class MediaControlPanel {
    public static final int[] ACTION_IDS = {C0015R$id.action0, C0015R$id.action1, C0015R$id.action2, C0015R$id.action3, C0015R$id.action4};
    protected final ActivityStarter mActivityStarter;
    private int mAlbumArtRadius;
    private int mAlbumArtSize;
    protected int mBackgroundColor;
    protected final Executor mBackgroundExecutor;
    protected Context mContext;
    protected MediaController mController;
    protected MediaViewController mMediaViewController;
    private SeekBarObserver mSeekBarObserver;
    protected final SeekBarViewModel mSeekBarViewModel;
    protected MediaSession.Token mToken;
    private PlayerViewHolder mViewHolder;
    private final ViewOutlineProvider mViewOutlineProvider = new ViewOutlineProvider() {
        /* class com.android.systemui.media.MediaControlPanel.AnonymousClass1 */

        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, MediaControlPanel.this.mAlbumArtSize, MediaControlPanel.this.mAlbumArtSize, (float) MediaControlPanel.this.mAlbumArtRadius);
        }
    };

    public MediaControlPanel(Context context, Executor executor, ActivityStarter activityStarter, MediaViewController mediaViewController, SeekBarViewModel seekBarViewModel) {
        this.mContext = context;
        this.mBackgroundExecutor = executor;
        this.mActivityStarter = activityStarter;
        this.mSeekBarViewModel = seekBarViewModel;
        this.mMediaViewController = mediaViewController;
        loadDimens();
    }

    public void onDestroy() {
        if (this.mSeekBarObserver != null) {
            this.mSeekBarViewModel.getProgress().removeObserver(this.mSeekBarObserver);
        }
        this.mSeekBarViewModel.onDestroy();
        this.mMediaViewController.onDestroy();
    }

    private void loadDimens() {
        this.mAlbumArtRadius = this.mContext.getResources().getDimensionPixelSize(Utils.getThemeAttr(this.mContext, 16844145));
        this.mAlbumArtSize = this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.qs_media_album_size);
    }

    public PlayerViewHolder getView() {
        return this.mViewHolder;
    }

    public MediaViewController getMediaViewController() {
        return this.mMediaViewController;
    }

    public void setListening(boolean z) {
        this.mSeekBarViewModel.setListening(z);
    }

    public void attach(PlayerViewHolder playerViewHolder) {
        this.mViewHolder = playerViewHolder;
        TransitionLayout player = playerViewHolder.getPlayer();
        ImageView albumView = playerViewHolder.getAlbumView();
        albumView.setOutlineProvider(this.mViewOutlineProvider);
        albumView.setClipToOutline(true);
        this.mSeekBarObserver = new SeekBarObserver(playerViewHolder);
        this.mSeekBarViewModel.getProgress().observeForever(this.mSeekBarObserver);
        this.mSeekBarViewModel.attachTouchHandlers(playerViewHolder.getSeekBar());
        this.mMediaViewController.attach(player);
    }

    public void bind(MediaData mediaData) {
        int[] iArr = ACTION_IDS;
        if (this.mViewHolder != null) {
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
            this.mViewHolder.getPlayer().setBackgroundTintList(ColorStateList.valueOf(this.mBackgroundColor));
            PendingIntent clickIntent = mediaData.getClickIntent();
            if (clickIntent != null) {
                this.mViewHolder.getPlayer().setOnClickListener(new View.OnClickListener(clickIntent) {
                    /* class com.android.systemui.media.$$Lambda$MediaControlPanel$r2fXPXw2z1qXnX31VuekOYH3JxQ */
                    public final /* synthetic */ PendingIntent f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onClick(View view) {
                        MediaControlPanel.this.lambda$bind$0$MediaControlPanel(this.f$1, view);
                    }
                });
            }
            ImageView albumView = this.mViewHolder.getAlbumView();
            boolean z = mediaData.getArtwork() != null;
            if (z) {
                albumView.setImageDrawable(scaleDrawable(mediaData.getArtwork()));
            }
            setVisibleAndAlpha(collapsedLayout, C0015R$id.album_art, z);
            setVisibleAndAlpha(expandedLayout, C0015R$id.album_art, z);
            ImageView appIcon = this.mViewHolder.getAppIcon();
            if (mediaData.getAppIcon() != null) {
                appIcon.setImageDrawable(mediaData.getAppIcon());
            } else {
                appIcon.setImageDrawable(this.mContext.getDrawable(C0013R$drawable.ic_music_note));
            }
            this.mViewHolder.getTitleText().setText(mediaData.getSong());
            this.mViewHolder.getAppName().setText(mediaData.getApp());
            this.mViewHolder.getArtistText().setText(mediaData.getArtist());
            this.mViewHolder.getSeamless().setVisibility(0);
            setVisibleAndAlpha(collapsedLayout, C0015R$id.media_seamless, true);
            setVisibleAndAlpha(expandedLayout, C0015R$id.media_seamless, true);
            this.mViewHolder.getSeamless().setOnClickListener(new View.OnClickListener(mediaData) {
                /* class com.android.systemui.media.$$Lambda$MediaControlPanel$TE2FbF426T83JfnZb2pju5PDfs */
                public final /* synthetic */ MediaData f$1;

                {
                    this.f$1 = r2;
                }

                public final void onClick(View view) {
                    MediaControlPanel.this.lambda$bind$1$MediaControlPanel(this.f$1, view);
                }
            });
            ImageView seamlessIcon = this.mViewHolder.getSeamlessIcon();
            TextView seamlessText = this.mViewHolder.getSeamlessText();
            MediaDeviceData device = mediaData.getDevice();
            int id = this.mViewHolder.getSeamless().getId();
            int id2 = this.mViewHolder.getSeamlessFallback().getId();
            boolean z2 = device != null && !device.getEnabled();
            int i = z2 ? 0 : 8;
            this.mViewHolder.getSeamlessFallback().setVisibility(i);
            expandedLayout.setVisibility(id2, i);
            collapsedLayout.setVisibility(id2, i);
            int i2 = z2 ? 8 : 0;
            this.mViewHolder.getSeamless().setVisibility(i2);
            expandedLayout.setVisibility(id, i2);
            collapsedLayout.setVisibility(id, i2);
            float f = mediaData.getResumption() ? 0.38f : 1.0f;
            expandedLayout.setAlpha(id, f);
            collapsedLayout.setAlpha(id, f);
            this.mViewHolder.getSeamless().setEnabled(!mediaData.getResumption());
            if (z2) {
                seamlessIcon.setImageDrawable(null);
                seamlessText.setText((CharSequence) null);
            } else if (device != null) {
                Drawable icon = device.getIcon();
                seamlessIcon.setVisibility(0);
                if (icon instanceof AdaptiveIcon) {
                    AdaptiveIcon adaptiveIcon = (AdaptiveIcon) icon;
                    adaptiveIcon.setBackgroundColor(this.mBackgroundColor);
                    seamlessIcon.setImageDrawable(adaptiveIcon);
                } else {
                    seamlessIcon.setImageDrawable(icon);
                }
                seamlessText.setText(device.getName());
            } else {
                Log.w("MediaControlPanel", "device is null. Not binding output chip.");
                seamlessIcon.setVisibility(8);
                seamlessText.setText(17040178);
            }
            List<Integer> actionsToShowInCompact = mediaData.getActionsToShowInCompact();
            List<MediaAction> actions = mediaData.getActions();
            int i3 = 0;
            while (i3 < actions.size() && i3 < iArr.length) {
                int i4 = iArr[i3];
                ImageButton action = this.mViewHolder.getAction(i4);
                MediaAction mediaAction = actions.get(i3);
                action.setImageDrawable(mediaAction.getDrawable());
                action.setContentDescription(mediaAction.getContentDescription());
                Runnable action2 = mediaAction.getAction();
                if (action2 == null) {
                    action.setEnabled(false);
                } else {
                    action.setEnabled(true);
                    action.setOnClickListener(new View.OnClickListener(action2) {
                        /* class com.android.systemui.media.$$Lambda$MediaControlPanel$_SKXzyUYhhL8GMTnpSLM2qnRFFw */
                        public final /* synthetic */ Runnable f$0;

                        {
                            this.f$0 = r1;
                        }

                        public final void onClick(View view) {
                            this.f$0.run();
                        }
                    });
                }
                setVisibleAndAlpha(collapsedLayout, i4, actionsToShowInCompact.contains(Integer.valueOf(i3)));
                setVisibleAndAlpha(expandedLayout, i4, true);
                i3++;
            }
            while (i3 < iArr.length) {
                setVisibleAndAlpha(expandedLayout, iArr[i3], false);
                setVisibleAndAlpha(collapsedLayout, iArr[i3], false);
                i3++;
            }
            this.mBackgroundExecutor.execute(new Runnable(getController()) {
                /* class com.android.systemui.media.$$Lambda$MediaControlPanel$hrbddZ18Mr3EF0WaK5GAsQc9Ds */
                public final /* synthetic */ MediaController f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MediaControlPanel.this.lambda$bind$3$MediaControlPanel(this.f$1);
                }
            });
            this.mMediaViewController.refreshState();
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bind$0 */
    public /* synthetic */ void lambda$bind$0$MediaControlPanel(PendingIntent pendingIntent, View view) {
        this.mActivityStarter.postStartActivityDismissingKeyguard(pendingIntent);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bind$1 */
    public /* synthetic */ void lambda$bind$1$MediaControlPanel(MediaData mediaData, View view) {
        this.mActivityStarter.startActivity(new Intent().setAction("com.android.settings.panel.action.MEDIA_OUTPUT").putExtra("com.android.settings.panel.extra.PACKAGE_NAME", mediaData.getPackageName()).putExtra("key_media_session_token", this.mToken), false, true, 268468224);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bind$3 */
    public /* synthetic */ void lambda$bind$3$MediaControlPanel(MediaController mediaController) {
        this.mSeekBarViewModel.updateController(mediaController);
    }

    private Drawable scaleDrawable(Icon icon) {
        Rect rect;
        if (icon == null) {
            return null;
        }
        Drawable loadDrawable = icon.loadDrawable(this.mContext);
        float intrinsicHeight = ((float) loadDrawable.getIntrinsicHeight()) / ((float) loadDrawable.getIntrinsicWidth());
        if (intrinsicHeight > 1.0f) {
            int i = this.mAlbumArtSize;
            rect = new Rect(0, 0, i, (int) (((float) i) * intrinsicHeight));
        } else {
            int i2 = this.mAlbumArtSize;
            rect = new Rect(0, 0, (int) (((float) i2) / intrinsicHeight), i2);
        }
        if (rect.width() > this.mAlbumArtSize || rect.height() > this.mAlbumArtSize) {
            rect.offset((int) (-(((float) (rect.width() - this.mAlbumArtSize)) / 2.0f)), (int) (-(((float) (rect.height() - this.mAlbumArtSize)) / 2.0f)));
        }
        loadDrawable.setBounds(rect);
        return loadDrawable;
    }

    public MediaController getController() {
        return this.mController;
    }

    public boolean isPlaying() {
        return isPlaying(this.mController);
    }

    /* access modifiers changed from: protected */
    public boolean isPlaying(MediaController mediaController) {
        PlaybackState playbackState;
        if (mediaController == null || (playbackState = mediaController.getPlaybackState()) == null || playbackState.getState() != 3) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void setVisibleAndAlpha(ConstraintSet constraintSet, int i, boolean z) {
        constraintSet.setVisibility(i, z ? 0 : 8);
        constraintSet.setAlpha(i, z ? 1.0f : 0.0f);
    }
}

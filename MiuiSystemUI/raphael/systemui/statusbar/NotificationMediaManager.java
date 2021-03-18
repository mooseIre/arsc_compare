package com.android.systemui.statusbar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Icon;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.AsyncTask;
import android.os.Trace;
import android.provider.DeviceConfig;
import android.util.ArraySet;
import android.widget.ImageView;
import com.android.internal.statusbar.NotificationVisibility;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.android.systemui.Interpolators;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.notification.NotificationEntryListener;
import com.android.systemui.statusbar.notification.NotificationEntryManager;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.phone.BiometricUnlockController;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.LockscreenWallpaper;
import com.android.systemui.statusbar.phone.NotificationShadeWindowController;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.ScrimState;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.Utils;
import com.android.systemui.util.concurrency.DelayableExecutor;
import dagger.Lazy;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NotificationMediaManager implements Dumpable {
    private static final HashSet<Integer> PAUSED_MEDIA_STATES;
    private BackDropView mBackdrop;
    private ImageView mBackdropBack;
    private ImageView mBackdropFront;
    private BiometricUnlockController mBiometricUnlockController;
    private final SysuiColorExtractor mColorExtractor = ((SysuiColorExtractor) Dependency.get(SysuiColorExtractor.class));
    private final Context mContext;
    private final NotificationEntryManager mEntryManager;
    protected final Runnable mHideBackdropFront = new Runnable() {
        /* class com.android.systemui.statusbar.NotificationMediaManager.AnonymousClass4 */

        public void run() {
            NotificationMediaManager.this.mBackdropFront.setVisibility(4);
            NotificationMediaManager.this.mBackdropFront.animate().cancel();
            NotificationMediaManager.this.mBackdropFront.setImageDrawable(null);
        }
    };
    private final KeyguardBypassController mKeyguardBypassController;
    private final KeyguardStateController mKeyguardStateController = ((KeyguardStateController) Dependency.get(KeyguardStateController.class));
    private final DelayableExecutor mMainExecutor;
    private final MediaArtworkProcessor mMediaArtworkProcessor;
    private MediaController mMediaController;
    private final MediaController.Callback mMediaListener = new MediaController.Callback() {
        /* class com.android.systemui.statusbar.NotificationMediaManager.AnonymousClass2 */

        public void onPlaybackStateChanged(PlaybackState playbackState) {
            super.onPlaybackStateChanged(playbackState);
            if (playbackState != null) {
                if (!NotificationMediaManager.this.isPlaybackActive(playbackState.getState())) {
                    NotificationMediaManager.this.clearCurrentMediaNotification();
                }
                NotificationMediaManager.this.findAndUpdateMediaNotifications();
            }
        }

        public void onMetadataChanged(MediaMetadata mediaMetadata) {
            super.onMetadataChanged(mediaMetadata);
            NotificationMediaManager.this.mMediaArtworkProcessor.clearCache();
            NotificationMediaManager.this.mMediaMetadata = mediaMetadata;
            NotificationMediaManager.this.dispatchUpdateMediaMetaData(true, true);
        }
    };
    private final ArrayList<MediaListener> mMediaListeners;
    private MediaMetadata mMediaMetadata;
    private String mMediaNotificationKey;
    private final MediaSessionManager mMediaSessionManager;
    private Lazy<NotificationShadeWindowController> mNotificationShadeWindowController;
    protected NotificationPresenter mPresenter;
    private final Set<AsyncTask<?, ?, ?>> mProcessArtworkTasks = new ArraySet();
    private final DeviceConfig.OnPropertiesChangedListener mPropertiesChangedListener = new DeviceConfig.OnPropertiesChangedListener() {
        /* class com.android.systemui.statusbar.NotificationMediaManager.AnonymousClass1 */

        public void onPropertiesChanged(DeviceConfig.Properties properties) {
            for (String str : properties.getKeyset()) {
                if ("compact_media_notification_seekbar_enabled".equals(str)) {
                    String string = properties.getString(str, (String) null);
                    NotificationMediaManager.this.mShowCompactMediaSeekbar = "true".equals(string);
                }
            }
        }
    };
    private ScrimController mScrimController;
    private boolean mShowCompactMediaSeekbar;
    private final Lazy<StatusBar> mStatusBarLazy;
    private final StatusBarStateController mStatusBarStateController = ((StatusBarStateController) Dependency.get(StatusBarStateController.class));

    public interface MediaListener {
        default void onPrimaryMetadataOrStateChanged(MediaMetadata mediaMetadata, int i) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isPlaybackActive(int i) {
        return (i == 1 || i == 7 || i == 0) ? false : true;
    }

    static {
        HashSet<Integer> hashSet = new HashSet<>();
        PAUSED_MEDIA_STATES = hashSet;
        hashSet.add(0);
        PAUSED_MEDIA_STATES.add(1);
        PAUSED_MEDIA_STATES.add(2);
        PAUSED_MEDIA_STATES.add(7);
        PAUSED_MEDIA_STATES.add(8);
    }

    public NotificationMediaManager(Context context, Lazy<StatusBar> lazy, Lazy<NotificationShadeWindowController> lazy2, NotificationEntryManager notificationEntryManager, MediaArtworkProcessor mediaArtworkProcessor, KeyguardBypassController keyguardBypassController, DelayableExecutor delayableExecutor, DeviceConfigProxy deviceConfigProxy, final MediaDataManager mediaDataManager) {
        this.mContext = context;
        this.mMediaArtworkProcessor = mediaArtworkProcessor;
        this.mKeyguardBypassController = keyguardBypassController;
        this.mMediaListeners = new ArrayList<>();
        this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService("media_session");
        this.mStatusBarLazy = lazy;
        this.mNotificationShadeWindowController = lazy2;
        this.mEntryManager = notificationEntryManager;
        this.mMainExecutor = delayableExecutor;
        notificationEntryManager.addNotificationEntryListener(new NotificationEntryListener() {
            /* class com.android.systemui.statusbar.NotificationMediaManager.AnonymousClass3 */

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPendingEntryAdded(NotificationEntry notificationEntry) {
                mediaDataManager.onNotificationAdded(notificationEntry.getKey(), notificationEntry.getSbn());
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPreEntryUpdated(NotificationEntry notificationEntry) {
                mediaDataManager.onNotificationAdded(notificationEntry.getKey(), notificationEntry.getSbn());
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryInflated(NotificationEntry notificationEntry) {
                NotificationMediaManager.this.findAndUpdateMediaNotifications();
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryReinflated(NotificationEntry notificationEntry) {
                NotificationMediaManager.this.findAndUpdateMediaNotifications();
            }

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onEntryRemoved(NotificationEntry notificationEntry, NotificationVisibility notificationVisibility, boolean z, int i) {
                NotificationMediaManager.this.onNotificationRemoved(notificationEntry.getKey());
                mediaDataManager.onNotificationRemoved(notificationEntry.getKey());
            }
        });
        this.mShowCompactMediaSeekbar = "true".equals(DeviceConfig.getProperty("systemui", "compact_media_notification_seekbar_enabled"));
        deviceConfigProxy.addOnPropertiesChangedListener("systemui", this.mContext.getMainExecutor(), this.mPropertiesChangedListener);
    }

    public static boolean isPlayingState(int i) {
        return !PAUSED_MEDIA_STATES.contains(Integer.valueOf(i));
    }

    public void setUpWithPresenter(NotificationPresenter notificationPresenter) {
        this.mPresenter = notificationPresenter;
    }

    public void onNotificationRemoved(String str) {
        if (str.equals(this.mMediaNotificationKey)) {
            clearCurrentMediaNotification();
            dispatchUpdateMediaMetaData(true, true);
        }
    }

    public String getMediaNotificationKey() {
        return this.mMediaNotificationKey;
    }

    public MediaMetadata getMediaMetadata() {
        return this.mMediaMetadata;
    }

    public boolean getShowCompactMediaSeekbar() {
        return this.mShowCompactMediaSeekbar;
    }

    public Icon getMediaIcon() {
        if (this.mMediaNotificationKey == null) {
            return null;
        }
        synchronized (this.mEntryManager) {
            NotificationEntry activeNotificationUnfiltered = this.mEntryManager.getActiveNotificationUnfiltered(this.mMediaNotificationKey);
            if (activeNotificationUnfiltered != null) {
                if (activeNotificationUnfiltered.getIcons().getShelfIcon() != null) {
                    return activeNotificationUnfiltered.getIcons().getShelfIcon().getSourceIcon();
                }
            }
            return null;
        }
    }

    public void addCallback(MediaListener mediaListener) {
        this.mMediaListeners.add(mediaListener);
        mediaListener.onPrimaryMetadataOrStateChanged(this.mMediaMetadata, getMediaControllerPlaybackState(this.mMediaController));
    }

    public void findAndUpdateMediaNotifications() {
        NotificationEntry notificationEntry;
        MediaController mediaController;
        boolean z;
        MediaSession.Token token;
        synchronized (this.mEntryManager) {
            Collection<NotificationEntry> allNotifs = this.mEntryManager.getAllNotifs();
            Iterator<NotificationEntry> it = allNotifs.iterator();
            while (true) {
                if (!it.hasNext()) {
                    notificationEntry = null;
                    mediaController = null;
                    break;
                }
                notificationEntry = it.next();
                if (notificationEntry.isMediaNotification() && (token = (MediaSession.Token) notificationEntry.getSbn().getNotification().extras.getParcelable("android.mediaSession")) != null) {
                    mediaController = new MediaController(this.mContext, token);
                    if (3 == getMediaControllerPlaybackState(mediaController)) {
                        break;
                    }
                }
            }
            if (notificationEntry == null && this.mMediaSessionManager != null) {
                for (MediaController mediaController2 : this.mMediaSessionManager.getActiveSessionsForUser(null, -1)) {
                    String packageName = mediaController2.getPackageName();
                    Iterator<NotificationEntry> it2 = allNotifs.iterator();
                    while (true) {
                        if (!it2.hasNext()) {
                            break;
                        }
                        NotificationEntry next = it2.next();
                        if (next.getSbn().getPackageName().equals(packageName)) {
                            mediaController = mediaController2;
                            notificationEntry = next;
                            break;
                        }
                    }
                }
            }
            if (mediaController == null || sameSessions(this.mMediaController, mediaController)) {
                z = false;
            } else {
                clearCurrentMediaNotificationSession();
                this.mMediaController = mediaController;
                mediaController.registerCallback(this.mMediaListener);
                this.mMediaMetadata = this.mMediaController.getMetadata();
                z = true;
            }
            if (notificationEntry != null && !notificationEntry.getSbn().getKey().equals(this.mMediaNotificationKey)) {
                this.mMediaNotificationKey = notificationEntry.getSbn().getKey();
            }
        }
        if (z) {
            this.mEntryManager.updateNotifications("NotificationMediaManager - metaDataChanged");
        }
        dispatchUpdateMediaMetaData(z, true);
    }

    public void clearCurrentMediaNotification() {
        this.mMediaNotificationKey = null;
        clearCurrentMediaNotificationSession();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchUpdateMediaMetaData(boolean z, boolean z2) {
        NotificationPresenter notificationPresenter = this.mPresenter;
        if (notificationPresenter != null) {
            notificationPresenter.updateMediaMetaData(z, z2);
        }
        int mediaControllerPlaybackState = getMediaControllerPlaybackState(this.mMediaController);
        ArrayList arrayList = new ArrayList(this.mMediaListeners);
        for (int i = 0; i < arrayList.size(); i++) {
            ((MediaListener) arrayList.get(i)).onPrimaryMetadataOrStateChanged(this.mMediaMetadata, mediaControllerPlaybackState);
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print("    mMediaSessionManager=");
        printWriter.println(this.mMediaSessionManager);
        printWriter.print("    mMediaNotificationKey=");
        printWriter.println(this.mMediaNotificationKey);
        printWriter.print("    mMediaController=");
        printWriter.print(this.mMediaController);
        if (this.mMediaController != null) {
            printWriter.print(" state=" + this.mMediaController.getPlaybackState());
        }
        printWriter.println();
        printWriter.print("    mMediaMetadata=");
        printWriter.print(this.mMediaMetadata);
        if (this.mMediaMetadata != null) {
            printWriter.print(" title=" + ((Object) this.mMediaMetadata.getText("android.media.metadata.TITLE")));
        }
        printWriter.println();
    }

    private boolean sameSessions(MediaController mediaController, MediaController mediaController2) {
        if (mediaController == mediaController2) {
            return true;
        }
        if (mediaController == null) {
            return false;
        }
        return mediaController.controlsSameSession(mediaController2);
    }

    private int getMediaControllerPlaybackState(MediaController mediaController) {
        PlaybackState playbackState;
        if (mediaController == null || (playbackState = mediaController.getPlaybackState()) == null) {
            return 0;
        }
        return playbackState.getState();
    }

    private void clearCurrentMediaNotificationSession() {
        this.mMediaArtworkProcessor.clearCache();
        this.mMediaMetadata = null;
        MediaController mediaController = this.mMediaController;
        if (mediaController != null) {
            mediaController.unregisterCallback(this.mMediaListener);
        }
        this.mMediaController = null;
    }

    public void updateMediaMetaData(boolean z, boolean z2) {
        Bitmap bitmap;
        Trace.beginSection("StatusBar#updateMediaMetaData");
        if (this.mBackdrop == null) {
            Trace.endSection();
            return;
        }
        BiometricUnlockController biometricUnlockController = this.mBiometricUnlockController;
        boolean z3 = biometricUnlockController != null && biometricUnlockController.isWakeAndUnlock();
        if (this.mKeyguardStateController.isLaunchTransitionFadingAway() || z3) {
            this.mBackdrop.setVisibility(4);
            Trace.endSection();
            return;
        }
        MediaMetadata mediaMetadata = getMediaMetadata();
        if (mediaMetadata == null || this.mKeyguardBypassController.getBypassEnabled()) {
            bitmap = null;
        } else {
            bitmap = mediaMetadata.getBitmap("android.media.metadata.ART");
            if (bitmap == null) {
                bitmap = mediaMetadata.getBitmap("android.media.metadata.ALBUM_ART");
            }
        }
        if (z) {
            for (AsyncTask<?, ?, ?> asyncTask : this.mProcessArtworkTasks) {
                asyncTask.cancel(true);
            }
            this.mProcessArtworkTasks.clear();
        }
        if (bitmap == null || Utils.useQsMediaPlayer(this.mContext)) {
            finishUpdateMediaMetaData(z, z2, null);
        } else {
            this.mProcessArtworkTasks.add(new ProcessArtworkTask(this, z, z2).execute(bitmap));
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void finishUpdateMediaMetaData(boolean z, boolean z2, Bitmap bitmap) {
        BiometricUnlockController biometricUnlockController;
        BitmapDrawable bitmapDrawable = bitmap != null ? new BitmapDrawable(this.mBackdropBack.getResources(), bitmap) : null;
        boolean z3 = true;
        boolean z4 = bitmapDrawable != null;
        NotificationShadeWindowController notificationShadeWindowController = this.mNotificationShadeWindowController.get();
        boolean isOccluded = this.mStatusBarLazy.get().isOccluded();
        boolean z5 = bitmapDrawable != null;
        this.mColorExtractor.setHasMediaArtwork(z4);
        ScrimController scrimController = this.mScrimController;
        if (scrimController != null) {
            scrimController.setHasBackdrop(z5);
        }
        if (z5 && this.mStatusBarStateController.getState() != 0 && (biometricUnlockController = this.mBiometricUnlockController) != null && biometricUnlockController.getMode() != 2 && !isOccluded) {
            if (this.mBackdrop.getVisibility() != 0) {
                this.mBackdrop.setVisibility(8);
                if (z2) {
                    this.mBackdrop.setAlpha(0.0f);
                    this.mBackdrop.animate().alpha(1.0f);
                } else {
                    this.mBackdrop.animate().cancel();
                    this.mBackdrop.setAlpha(1.0f);
                }
                if (notificationShadeWindowController != null) {
                    notificationShadeWindowController.setBackdropShowing(true);
                }
                z = true;
            }
            if (z) {
                if (this.mBackdropBack.getDrawable() != null) {
                    this.mBackdropFront.setImageDrawable(this.mBackdropBack.getDrawable().getConstantState().newDrawable(this.mBackdropFront.getResources()).mutate());
                    this.mBackdropFront.setAlpha(1.0f);
                    this.mBackdropFront.setVisibility(0);
                } else {
                    this.mBackdropFront.setVisibility(4);
                }
                this.mBackdropBack.setImageDrawable(bitmapDrawable);
                if (this.mBackdropFront.getVisibility() == 0) {
                    this.mBackdropFront.animate().setDuration(250).alpha(0.0f).withEndAction(this.mHideBackdropFront);
                }
            }
        } else if (this.mBackdrop.getVisibility() != 8) {
            if (!this.mStatusBarStateController.isDozing() || ScrimState.AOD.getAnimateChange()) {
                z3 = false;
            }
            boolean isBypassFadingAnimation = this.mKeyguardStateController.isBypassFadingAnimation();
            BiometricUnlockController biometricUnlockController2 = this.mBiometricUnlockController;
            if ((((biometricUnlockController2 == null || biometricUnlockController2.getMode() != 2) && !z3) || isBypassFadingAnimation) && !isOccluded) {
                if (notificationShadeWindowController != null) {
                    notificationShadeWindowController.setBackdropShowing(false);
                }
                this.mBackdrop.animate().alpha(0.0f).setInterpolator(Interpolators.ACCELERATE_DECELERATE).setDuration(300).setStartDelay(0).withEndAction(new Runnable() {
                    /* class com.android.systemui.statusbar.$$Lambda$NotificationMediaManager$5ApBYxWBRgBH6AkWUHgwLiCFqEk */

                    public final void run() {
                        NotificationMediaManager.this.lambda$finishUpdateMediaMetaData$0$NotificationMediaManager();
                    }
                });
                if (this.mKeyguardStateController.isKeyguardFadingAway()) {
                    this.mBackdrop.animate().setDuration(this.mKeyguardStateController.getShortenedFadingAwayDuration()).setStartDelay(this.mKeyguardStateController.getKeyguardFadingAwayDelay()).setInterpolator(Interpolators.LINEAR).start();
                    return;
                }
                return;
            }
            this.mBackdrop.setVisibility(8);
            this.mBackdropBack.setImageDrawable(null);
            if (notificationShadeWindowController != null) {
                notificationShadeWindowController.setBackdropShowing(false);
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$finishUpdateMediaMetaData$0 */
    public /* synthetic */ void lambda$finishUpdateMediaMetaData$0$NotificationMediaManager() {
        this.mBackdrop.setVisibility(8);
        this.mBackdropFront.animate().cancel();
        this.mBackdropBack.setImageDrawable(null);
        this.mMainExecutor.execute(this.mHideBackdropFront);
    }

    public void setup(BackDropView backDropView, ImageView imageView, ImageView imageView2, ScrimController scrimController, LockscreenWallpaper lockscreenWallpaper) {
        this.mBackdrop = backDropView;
        this.mBackdropFront = imageView;
        this.mBackdropBack = imageView2;
        this.mScrimController = scrimController;
    }

    public void setBiometricUnlockController(BiometricUnlockController biometricUnlockController) {
        this.mBiometricUnlockController = biometricUnlockController;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Bitmap processArtwork(Bitmap bitmap) {
        return this.mMediaArtworkProcessor.processArtwork(this.mContext, bitmap);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeTask(AsyncTask<?, ?, ?> asyncTask) {
        this.mProcessArtworkTasks.remove(asyncTask);
    }

    /* access modifiers changed from: private */
    public static final class ProcessArtworkTask extends AsyncTask<Bitmap, Void, Bitmap> {
        private final boolean mAllowEnterAnimation;
        private final WeakReference<NotificationMediaManager> mManagerRef;
        private final boolean mMetaDataChanged;

        ProcessArtworkTask(NotificationMediaManager notificationMediaManager, boolean z, boolean z2) {
            this.mManagerRef = new WeakReference<>(notificationMediaManager);
            this.mMetaDataChanged = z;
            this.mAllowEnterAnimation = z2;
        }

        /* access modifiers changed from: protected */
        public Bitmap doInBackground(Bitmap... bitmapArr) {
            NotificationMediaManager notificationMediaManager = this.mManagerRef.get();
            if (notificationMediaManager == null || bitmapArr.length == 0 || isCancelled()) {
                return null;
            }
            return notificationMediaManager.processArtwork(bitmapArr[0]);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
            NotificationMediaManager notificationMediaManager = this.mManagerRef.get();
            if (notificationMediaManager != null && !isCancelled()) {
                notificationMediaManager.removeTask(this);
                notificationMediaManager.finishUpdateMediaMetaData(this.mMetaDataChanged, this.mAllowEnterAnimation, bitmap);
            }
        }

        /* access modifiers changed from: protected */
        public void onCancelled(Bitmap bitmap) {
            if (bitmap != null) {
                bitmap.recycle();
            }
            NotificationMediaManager notificationMediaManager = this.mManagerRef.get();
            if (notificationMediaManager != null) {
                notificationMediaManager.removeTask(this);
            }
        }
    }
}

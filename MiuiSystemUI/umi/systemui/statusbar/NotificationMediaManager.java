package com.android.systemui.statusbar;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
    /* access modifiers changed from: private */
    public ImageView mBackdropFront;
    private BiometricUnlockController mBiometricUnlockController;
    private final SysuiColorExtractor mColorExtractor = ((SysuiColorExtractor) Dependency.get(SysuiColorExtractor.class));
    private final Context mContext;
    private final NotificationEntryManager mEntryManager;
    protected final Runnable mHideBackdropFront = new Runnable() {
        public void run() {
            NotificationMediaManager.this.mBackdropFront.setVisibility(4);
            NotificationMediaManager.this.mBackdropFront.animate().cancel();
            NotificationMediaManager.this.mBackdropFront.setImageDrawable((Drawable) null);
        }
    };
    private final KeyguardBypassController mKeyguardBypassController;
    private final KeyguardStateController mKeyguardStateController = ((KeyguardStateController) Dependency.get(KeyguardStateController.class));
    private final DelayableExecutor mMainExecutor;
    /* access modifiers changed from: private */
    public final MediaArtworkProcessor mMediaArtworkProcessor;
    private MediaController mMediaController;
    private final MediaController.Callback mMediaListener = new MediaController.Callback() {
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
            MediaMetadata unused = NotificationMediaManager.this.mMediaMetadata = mediaMetadata;
            NotificationMediaManager.this.dispatchUpdateMediaMetaData(true, true);
        }
    };
    private final ArrayList<MediaListener> mMediaListeners;
    /* access modifiers changed from: private */
    public MediaMetadata mMediaMetadata;
    private String mMediaNotificationKey;
    private final MediaSessionManager mMediaSessionManager;
    private Lazy<NotificationShadeWindowController> mNotificationShadeWindowController;
    protected NotificationPresenter mPresenter;
    private final Set<AsyncTask<?, ?, ?>> mProcessArtworkTasks = new ArraySet();
    private final DeviceConfig.OnPropertiesChangedListener mPropertiesChangedListener = new DeviceConfig.OnPropertiesChangedListener() {
        public void onPropertiesChanged(DeviceConfig.Properties properties) {
            for (String str : properties.getKeyset()) {
                if ("compact_media_notification_seekbar_enabled".equals(str)) {
                    boolean unused = NotificationMediaManager.this.mShowCompactMediaSeekbar = "true".equals(properties.getString(str, (String) null));
                }
            }
        }
    };
    private ScrimController mScrimController;
    /* access modifiers changed from: private */
    public boolean mShowCompactMediaSeekbar;
    private final Lazy<StatusBar> mStatusBarLazy;
    private final StatusBarStateController mStatusBarStateController = ((StatusBarStateController) Dependency.get(StatusBarStateController.class));

    public interface MediaListener {
        void onPrimaryMetadataOrStateChanged(MediaMetadata mediaMetadata, int i) {
        }
    }

    /* access modifiers changed from: private */
    public boolean isPlaybackActive(int i) {
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
            public void onPendingEntryAdded(NotificationEntry notificationEntry) {
                mediaDataManager.onNotificationAdded(notificationEntry.getKey(), notificationEntry.getSbn());
            }

            public void onPreEntryUpdated(NotificationEntry notificationEntry) {
                mediaDataManager.onNotificationAdded(notificationEntry.getKey(), notificationEntry.getSbn());
            }

            public void onEntryInflated(NotificationEntry notificationEntry) {
                NotificationMediaManager.this.findAndUpdateMediaNotifications();
            }

            public void onEntryReinflated(NotificationEntry notificationEntry) {
                NotificationMediaManager.this.findAndUpdateMediaNotifications();
            }

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

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x002d, code lost:
        return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public android.graphics.drawable.Icon getMediaIcon() {
        /*
            r3 = this;
            java.lang.String r0 = r3.mMediaNotificationKey
            r1 = 0
            if (r0 != 0) goto L_0x0006
            return r1
        L_0x0006:
            com.android.systemui.statusbar.notification.NotificationEntryManager r0 = r3.mEntryManager
            monitor-enter(r0)
            com.android.systemui.statusbar.notification.NotificationEntryManager r2 = r3.mEntryManager     // Catch:{ all -> 0x002e }
            java.lang.String r3 = r3.mMediaNotificationKey     // Catch:{ all -> 0x002e }
            com.android.systemui.statusbar.notification.collection.NotificationEntry r3 = r2.getActiveNotificationUnfiltered(r3)     // Catch:{ all -> 0x002e }
            if (r3 == 0) goto L_0x002c
            com.android.systemui.statusbar.notification.icon.IconPack r2 = r3.getIcons()     // Catch:{ all -> 0x002e }
            com.android.systemui.statusbar.StatusBarIconView r2 = r2.getShelfIcon()     // Catch:{ all -> 0x002e }
            if (r2 != 0) goto L_0x001e
            goto L_0x002c
        L_0x001e:
            com.android.systemui.statusbar.notification.icon.IconPack r3 = r3.getIcons()     // Catch:{ all -> 0x002e }
            com.android.systemui.statusbar.StatusBarIconView r3 = r3.getShelfIcon()     // Catch:{ all -> 0x002e }
            android.graphics.drawable.Icon r3 = r3.getSourceIcon()     // Catch:{ all -> 0x002e }
            monitor-exit(r0)     // Catch:{ all -> 0x002e }
            return r3
        L_0x002c:
            monitor-exit(r0)     // Catch:{ all -> 0x002e }
            return r1
        L_0x002e:
            r3 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x002e }
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.NotificationMediaManager.getMediaIcon():android.graphics.drawable.Icon");
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
                for (MediaController mediaController2 : this.mMediaSessionManager.getActiveSessionsForUser((ComponentName) null, -1)) {
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
    public void dispatchUpdateMediaMetaData(boolean z, boolean z2) {
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
            printWriter.print(" title=" + this.mMediaMetadata.getText("android.media.metadata.TITLE"));
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
            for (AsyncTask<?, ?, ?> cancel : this.mProcessArtworkTasks) {
                cancel.cancel(true);
            }
            this.mProcessArtworkTasks.clear();
        }
        if (bitmap == null || Utils.useQsMediaPlayer(this.mContext)) {
            finishUpdateMediaMetaData(z, z2, (Bitmap) null);
        } else {
            this.mProcessArtworkTasks.add(new ProcessArtworkTask(this, z, z2).execute(new Bitmap[]{bitmap}));
        }
        Trace.endSection();
    }

    /* access modifiers changed from: private */
    public void finishUpdateMediaMetaData(boolean z, boolean z2, Bitmap bitmap) {
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
            this.mBackdropBack.setImageDrawable((Drawable) null);
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
        this.mBackdropBack.setImageDrawable((Drawable) null);
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
    public Bitmap processArtwork(Bitmap bitmap) {
        return this.mMediaArtworkProcessor.processArtwork(this.mContext, bitmap);
    }

    /* access modifiers changed from: private */
    public void removeTask(AsyncTask<?, ?, ?> asyncTask) {
        this.mProcessArtworkTasks.remove(asyncTask);
    }

    private static final class ProcessArtworkTask extends AsyncTask<Bitmap, Void, Bitmap> {
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
            NotificationMediaManager notificationMediaManager = (NotificationMediaManager) this.mManagerRef.get();
            if (notificationMediaManager == null || bitmapArr.length == 0 || isCancelled()) {
                return null;
            }
            return notificationMediaManager.processArtwork(bitmapArr[0]);
        }

        /* access modifiers changed from: protected */
        public void onPostExecute(Bitmap bitmap) {
            NotificationMediaManager notificationMediaManager = (NotificationMediaManager) this.mManagerRef.get();
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
            NotificationMediaManager notificationMediaManager = (NotificationMediaManager) this.mManagerRef.get();
            if (notificationMediaManager != null) {
                notificationMediaManager.removeTask(this);
            }
        }
    }
}

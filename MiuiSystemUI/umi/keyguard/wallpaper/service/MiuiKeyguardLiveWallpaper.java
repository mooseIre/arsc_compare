package com.android.keyguard.wallpaper.service;

import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayerCompat;
import android.net.Uri;
import android.os.AsyncTask;
import android.service.wallpaper.WallpaperService;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import com.android.keyguard.charge.ChargeHelper;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController;
import com.android.keyguard.wallpaper.service.BaseKeyguardWallpaperService;
import com.android.keyguard.wallpaper.service.MiuiKeyguardLiveWallpaper;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import java.io.File;
import java.util.Objects;

public class MiuiKeyguardLiveWallpaper extends BaseKeyguardWallpaperService {
    public WallpaperService.Engine onCreateEngine() {
        return new LiveLockEngine();
    }

    private class LiveLockEngine extends BaseKeyguardWallpaperService.BaseEngine implements StatusBarStateController.StateListener {
        private ChargeHelper mChargeHelper;
        private StatusBarStateController mController = ((StatusBarStateController) Dependency.get(StatusBarStateController.class));
        private boolean mIsDozing;
        private boolean mIsInteractive;
        private String mLastLiveLockPath = null;
        private MediaPlayer mLiveLockWallpaperPlayer;
        private boolean mLiveReady = false;
        private float mWindowAlpha;

        public LiveLockEngine() {
            super(MiuiKeyguardWallpaperController.KeyguardWallpaperType.LIVE_LOCK);
            StatusBarStateController statusBarStateController = this.mController;
            if (statusBarStateController != null) {
                statusBarStateController.addCallback(this);
            }
        }

        public void onPreWakeUpWithReason(String str) {
            super.onPreWakeUpWithReason(str);
            this.mIsInteractive = true;
            this.mHandler.post(new Runnable() {
                public final void run() {
                    MiuiKeyguardLiveWallpaper.LiveLockEngine.this.updateWallpaperVisibility();
                }
            });
        }

        /* access modifiers changed from: protected */
        public void onStartedWakingUp() {
            super.onStartedWakingUp();
            this.mIsInteractive = true;
            updateWallpaperVisibility();
        }

        /* access modifiers changed from: protected */
        public void onStartedGoingToSleep(int i) {
            super.onStartedGoingToSleep(i);
            this.mIsInteractive = false;
            updateWallpaperVisibility();
        }

        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            this.mChargeHelper = ChargeHelper.getInstance(this.mContext.getApplicationContext());
            updateWallpaperVisibility();
        }

        public void onDestroy() {
            releaseLiveWallpaper();
            super.onDestroy();
        }

        public void onVisibilityChanged(boolean z) {
            super.onVisibilityChanged(z);
            updateWallpaperVisibility();
        }

        public void onDozingChanged(boolean z) {
            this.mIsDozing = z;
            this.mWindowAlpha = this.mIsDozing ? 0.0f : 1.0f;
            updateSurfaceAttrs(this.mWindowAlpha);
        }

        public void onKeyguardWallpaperUpdated(MiuiKeyguardWallpaperController.KeyguardWallpaperType keyguardWallpaperType, boolean z, File file, Drawable drawable) {
            super.onKeyguardWallpaperUpdated(keyguardWallpaperType, z, file, drawable);
            if (this.mWallpaperType != keyguardWallpaperType) {
                releaseLiveWallpaper();
            } else if (z || !TextUtils.equals(this.mLastLiveLockPath, file.getPath())) {
                if (this.mKeyguardWallpaperVisible) {
                    showMiLiveLockWallpaper(file);
                } else {
                    releaseLiveWallpaper();
                }
                this.mLastLiveLockPath = file.getPath();
            }
        }

        public void onKeyguardAnimationUpdated(float f) {
            super.onKeyguardAnimationUpdated(f);
            updateWallpaperVisibility();
        }

        private void releaseLiveWallpaper() {
            releaseLiveWallpaper(true);
        }

        private void releaseLiveWallpaper(boolean z) {
            MediaPlayer mediaPlayer = this.mLiveLockWallpaperPlayer;
            if (mediaPlayer != null) {
                this.mLiveLockWallpaperPlayer = null;
                Objects.requireNonNull(mediaPlayer);
                AsyncTask.execute(new Runnable(mediaPlayer) {
                    private final /* synthetic */ MediaPlayer f$0;

                    {
                        this.f$0 = r1;
                    }

                    public final void run() {
                        this.f$0.release();
                    }
                });
            }
            if (z) {
                this.mLastLiveLockPath = null;
            }
        }

        private void showMiLiveLockWallpaper(File file) {
            releaseLiveWallpaper(false);
            this.mLiveLockWallpaperPlayer = MediaPlayer.create(this.mContext, Uri.fromFile(file));
            this.mLiveReady = false;
            MediaPlayer mediaPlayer = this.mLiveLockWallpaperPlayer;
            if (mediaPlayer != null) {
                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    public final boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                        return MiuiKeyguardLiveWallpaper.LiveLockEngine.this.lambda$showMiLiveLockWallpaper$1$MiuiKeyguardLiveWallpaper$LiveLockEngine(mediaPlayer, i, i2);
                    }
                });
                this.mLiveLockWallpaperPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                    public final void onSeekComplete(MediaPlayer mediaPlayer) {
                        MiuiKeyguardLiveWallpaper.LiveLockEngine.this.lambda$showMiLiveLockWallpaper$2$MiuiKeyguardLiveWallpaper$LiveLockEngine(mediaPlayer);
                    }
                });
                this.mLiveLockWallpaperPlayer.setSurface(getSurfaceHolder().getSurface());
                this.mLiveLockWallpaperPlayer.setVideoScalingMode(2);
                this.mLiveReady = true;
                startLiveLockWallpaper();
                return;
            }
            Log.e("MiuiKeyguardWallpaper", "live lock wallpaper is null");
        }

        public /* synthetic */ boolean lambda$showMiLiveLockWallpaper$1$MiuiKeyguardLiveWallpaper$LiveLockEngine(MediaPlayer mediaPlayer, int i, int i2) {
            Log.e("MiuiKeyguardWallpaper", "restart: error happened " + i);
            this.mHandler.post(new Runnable() {
                public final void run() {
                    MiuiKeyguardLiveWallpaper.LiveLockEngine.this.lambda$showMiLiveLockWallpaper$0$MiuiKeyguardLiveWallpaper$LiveLockEngine();
                }
            });
            return false;
        }

        public /* synthetic */ void lambda$showMiLiveLockWallpaper$0$MiuiKeyguardLiveWallpaper$LiveLockEngine() {
            if (!TextUtils.isEmpty(this.mLastLiveLockPath)) {
                showMiLiveLockWallpaper(new File(this.mLastLiveLockPath));
            }
        }

        public /* synthetic */ void lambda$showMiLiveLockWallpaper$2$MiuiKeyguardLiveWallpaper$LiveLockEngine(MediaPlayer mediaPlayer) {
            if (this.mLiveReady) {
                mediaPlayer.pause();
            }
        }

        private void startLiveLockWallpaper() {
            if (this.mLiveLockWallpaperPlayer != null && this.mLiveReady && this.mKeyguardWallpaperVisible) {
                try {
                    if (!this.mChargeHelper.isExtremePowerModeEnabled(this.mContext)) {
                        Log.i("MiuiKeyguardWallpaper", "starting live wallpaper");
                        this.mLiveLockWallpaperPlayer.start();
                        this.mLiveLockWallpaperPlayer.setVolume(1.0f, 1.0f);
                        return;
                    }
                    Log.i("MiuiKeyguardWallpaper", "not starting live wallpaper, seek to 0");
                    MediaPlayerCompat.seekTo(this.mLiveLockWallpaperPlayer, 0, 3);
                } catch (Exception e) {
                    Log.e("MiuiKeyguardWallpaper", e.getMessage(), e);
                }
            }
        }

        private void pauseLiveLockWallpaper() {
            MediaPlayer mediaPlayer = this.mLiveLockWallpaperPlayer;
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                MediaPlayerCompat.seekTo(this.mLiveLockWallpaperPlayer, 0, 3);
                this.mLiveLockWallpaperPlayer.setVolume(0.0f, 0.0f);
            }
        }

        /* access modifiers changed from: private */
        public void updateWallpaperVisibility() {
            boolean z = this.mSurfaceCreated && this.mIsInteractive && this.mWallpaperAnimValue < 0.0f && (this.mKeyguardShowing || this.mEngineVisible) && !this.mIsDozing;
            boolean z2 = this.mKeyguardWallpaperVisible;
            this.mKeyguardWallpaperVisible = z;
            if (z != z2 && Constants.DEBUG) {
                Log.i("MiuiKeyguardWallpaper", "mSurfaceCreated " + this.mSurfaceCreated + ", mEngineVisible " + this.mEngineVisible + ", mIsInteractive " + this.mIsInteractive + ", mKeyguardShowing " + this.mKeyguardShowing);
            }
            if (!z || z2) {
                if (!z && z2) {
                    pauseLiveLockWallpaper();
                }
            } else if (this.mLiveLockWallpaperPlayer != null) {
                startLiveLockWallpaper();
            } else if (!TextUtils.isEmpty(this.mLastLiveLockPath)) {
                showMiLiveLockWallpaper(new File(this.mLastLiveLockPath));
            }
        }

        private void updateSurfaceAttrs(float f) {
            WindowManager.LayoutParams layoutParams = this.mLayoutParams;
            if (layoutParams != null && BaseKeyguardWallpaperService.ENGINE_UPDATE_SURFACE != null) {
                boolean z = layoutParams.alpha != f;
                this.mWindowAlpha = f;
                WindowManager.LayoutParams layoutParams2 = this.mLayoutParams;
                if (this.mIsDozing) {
                    f = 0.0f;
                }
                layoutParams2.alpha = f;
                if (z) {
                    try {
                        BaseKeyguardWallpaperService.ENGINE_UPDATE_SURFACE.invoke(this, new Object[]{true, false, true});
                    } catch (Exception e) {
                        Log.e("MiuiKeyguardWallpaper", "error in updateSurfaceAttrs", e);
                    }
                }
            }
        }

        public void onOffsetsChanged(float f, float f2, float f3, float f4, int i, int i2) {
            super.onOffsetsChanged(f, f2, f3, f4, i, i2);
            updateWallpaperVisibility();
        }

        public void onSurfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
            super.onSurfaceChanged(surfaceHolder, i, i2, i3);
            updateWallpaperVisibility();
        }

        public void onSurfaceRedrawNeeded(SurfaceHolder surfaceHolder) {
            super.onSurfaceRedrawNeeded(surfaceHolder);
            updateWallpaperVisibility();
        }

        public void onSurfaceCreated(SurfaceHolder surfaceHolder) {
            super.onSurfaceCreated(surfaceHolder);
            this.mSurfaceCreated = true;
            updateWallpaperVisibility();
        }

        public void onSurfaceDestroyed(SurfaceHolder surfaceHolder) {
            super.onSurfaceDestroyed(surfaceHolder);
            releaseLiveWallpaper();
            this.mSurfaceCreated = false;
        }
    }
}

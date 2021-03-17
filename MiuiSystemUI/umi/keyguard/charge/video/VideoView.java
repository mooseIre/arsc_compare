package com.android.keyguard.charge.video;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Property;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.keyguard.charge.video.VideoView;
import com.android.keyguard.utils.ExecutorHelper;
import com.android.systemui.C0010R$bool;
import com.android.systemui.C0013R$drawable;

public class VideoView extends RelativeLayout {
    private ObjectAnimator alphaOut;
    private ObjectAnimator alphaRapidOut;
    private ObjectAnimator alphaStrongOut;
    private ImageView mBackImage;
    private TextureView.SurfaceTextureListener mChargeSurfaceTextureListener;
    private String mChargeUri;
    private TextureView mChargeView;
    private Configuration mConfiguration;
    private Context mContext;
    private boolean mIsFoldChargeVideo;
    private MediaPlayer mMediaPlayer;
    MediaPlayer.OnCompletionListener mOnCompletionListener;
    MediaPlayer.OnPreparedListener mOnPreparedListener;
    private Point mPoint;
    private TextureView.SurfaceTextureListener mRapidChargeSurfaceTextureListener;
    private String mRapidChargeUri;
    private TextureView mRapidChargeView;
    private MediaPlayer mRapidMediaPlayer;
    private Point mScreenSize;
    private TextureView.SurfaceTextureListener mStrongRapidChargeSurfaceTextureListener;
    private String mStrongRapidChargeUri;
    private TextureView mStrongRapidChargeView;
    private MediaPlayer mStrongRapidMediaPlayer;
    private AnimatorSet mToNormalAnimatorSet;
    private AnimatorSet mToRapidAnimatorSet;
    private AnimatorSet mToStrongRapidAnimatorSet;
    private int mVideoHeight;
    private int mVideoWidth;
    private WindowManager mWindowManager;
    MediaPlayer.OnErrorListener onErrorListener;

    public VideoView(Context context) {
        this(context, null);
    }

    public VideoView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public VideoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mToStrongRapidAnimatorSet = new AnimatorSet();
        this.mToRapidAnimatorSet = new AnimatorSet();
        this.mToNormalAnimatorSet = new AnimatorSet();
        this.mVideoWidth = 1080;
        this.mVideoHeight = 2340;
        this.mScreenSize = new Point();
        this.mConfiguration = new Configuration();
        this.mIsFoldChargeVideo = false;
        this.mOnPreparedListener = new MediaPlayer.OnPreparedListener(this) {
            /* class com.android.keyguard.charge.video.VideoView.AnonymousClass1 */

            public void onPrepared(MediaPlayer mediaPlayer) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
        };
        this.onErrorListener = new MediaPlayer.OnErrorListener(this) {
            /* class com.android.keyguard.charge.video.VideoView.AnonymousClass2 */

            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                Log.e("ChargeVideoView", "error in playing charge video " + i + " " + i2);
                return true;
            }
        };
        this.mOnCompletionListener = new MediaPlayer.OnCompletionListener(this) {
            /* class com.android.keyguard.charge.video.VideoView.AnonymousClass3 */

            public void onCompletion(MediaPlayer mediaPlayer) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
        };
        this.mChargeSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
            /* class com.android.keyguard.charge.video.VideoView.AnonymousClass7 */

            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }

            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
                ExecutorHelper.getIOThreadPool().execute(new Runnable(surfaceTexture) {
                    /* class com.android.keyguard.charge.video.$$Lambda$VideoView$7$l2PbGj1KM7VmQ1nVGU89HOvlFg0 */
                    public final /* synthetic */ SurfaceTexture f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        VideoView.AnonymousClass7.this.lambda$onSurfaceTextureAvailable$0$VideoView$7(this.f$1);
                    }
                });
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onSurfaceTextureAvailable$0 */
            public /* synthetic */ void lambda$onSurfaceTextureAvailable$0$VideoView$7(SurfaceTexture surfaceTexture) {
                try {
                    if (VideoView.this.mMediaPlayer != null) {
                        VideoView.this.mMediaPlayer.setSurface(new Surface(surfaceTexture));
                        VideoView.this.mMediaPlayer.setDataSource(VideoView.this.mContext, Uri.parse(VideoView.this.mChargeUri));
                        VideoView.this.mMediaPlayer.prepareAsync();
                        VideoView.this.mMediaPlayer.setLooping(true);
                    }
                } catch (Exception e) {
                    Log.e("ChargeVideoView", "play charge video exception:", e);
                }
            }

            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                if (VideoView.this.mMediaPlayer == null) {
                    return false;
                }
                VideoView.this.mMediaPlayer.pause();
                VideoView.this.mMediaPlayer.stop();
                VideoView.this.mMediaPlayer.release();
                VideoView.this.mMediaPlayer = null;
                return false;
            }
        };
        this.mRapidChargeSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
            /* class com.android.keyguard.charge.video.VideoView.AnonymousClass8 */

            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }

            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
                ExecutorHelper.getIOThreadPool().execute(new Runnable(surfaceTexture) {
                    /* class com.android.keyguard.charge.video.$$Lambda$VideoView$8$9pY3gBtoCxpLmIi1wm8A1sltnV8 */
                    public final /* synthetic */ SurfaceTexture f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        VideoView.AnonymousClass8.this.lambda$onSurfaceTextureAvailable$0$VideoView$8(this.f$1);
                    }
                });
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onSurfaceTextureAvailable$0 */
            public /* synthetic */ void lambda$onSurfaceTextureAvailable$0$VideoView$8(SurfaceTexture surfaceTexture) {
                try {
                    if (VideoView.this.mRapidMediaPlayer != null) {
                        VideoView.this.mRapidMediaPlayer.setSurface(new Surface(surfaceTexture));
                        VideoView.this.mRapidMediaPlayer.setDataSource(VideoView.this.mContext, Uri.parse(VideoView.this.mRapidChargeUri));
                        VideoView.this.mRapidMediaPlayer.prepareAsync();
                        VideoView.this.mRapidMediaPlayer.setLooping(true);
                    }
                } catch (Exception e) {
                    Log.e("ChargeVideoView", "play rapid charge video exception:", e);
                }
            }

            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                if (VideoView.this.mRapidMediaPlayer == null) {
                    return false;
                }
                VideoView.this.mRapidMediaPlayer.pause();
                VideoView.this.mRapidMediaPlayer.stop();
                VideoView.this.mRapidMediaPlayer.release();
                VideoView.this.mRapidMediaPlayer = null;
                return false;
            }
        };
        this.mStrongRapidChargeSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
            /* class com.android.keyguard.charge.video.VideoView.AnonymousClass9 */

            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }

            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
                ExecutorHelper.getIOThreadPool().execute(new Runnable(surfaceTexture) {
                    /* class com.android.keyguard.charge.video.$$Lambda$VideoView$9$ZaEYdY378gzCCA2rHctcTDjBAg */
                    public final /* synthetic */ SurfaceTexture f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void run() {
                        VideoView.AnonymousClass9.this.lambda$onSurfaceTextureAvailable$0$VideoView$9(this.f$1);
                    }
                });
            }

            /* access modifiers changed from: private */
            /* renamed from: lambda$onSurfaceTextureAvailable$0 */
            public /* synthetic */ void lambda$onSurfaceTextureAvailable$0$VideoView$9(SurfaceTexture surfaceTexture) {
                try {
                    if (VideoView.this.mStrongRapidMediaPlayer != null) {
                        VideoView.this.mStrongRapidMediaPlayer.setSurface(new Surface(surfaceTexture));
                        VideoView.this.mStrongRapidMediaPlayer.setDataSource(VideoView.this.mContext, Uri.parse(VideoView.this.mStrongRapidChargeUri));
                        VideoView.this.mStrongRapidMediaPlayer.prepareAsync();
                        VideoView.this.mStrongRapidMediaPlayer.setLooping(true);
                    }
                } catch (Exception e) {
                    Log.e("ChargeVideoView", "play strong rapid charge video exception:", e);
                }
            }

            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                if (VideoView.this.mStrongRapidMediaPlayer == null) {
                    return false;
                }
                VideoView.this.mStrongRapidMediaPlayer.pause();
                VideoView.this.mStrongRapidMediaPlayer.stop();
                VideoView.this.mStrongRapidMediaPlayer.release();
                VideoView.this.mStrongRapidMediaPlayer = null;
                return false;
            }
        };
        this.mIsFoldChargeVideo = context.getResources().getBoolean(C0010R$bool.config_folding_charge_video);
        this.mContext = context;
        this.mWindowManager = (WindowManager) getContext().getSystemService("window");
        this.mPoint = new Point();
        ImageView imageView = new ImageView(context);
        this.mBackImage = imageView;
        imageView.setBackgroundResource(C0013R$drawable.wired_charge_video_bg_img);
        if (this.mIsFoldChargeVideo) {
            checkScreenSize(true);
            addView(this.mBackImage, getTextTureParams());
            return;
        }
        addView(this.mBackImage, getVideoLayoutParams());
    }

    public void setDefaultImage(int i) {
        this.mBackImage.setBackground(i != 0 ? this.mContext.getDrawable(i) : null);
        if (this.mIsFoldChargeVideo) {
            this.mBackImage.setLayoutParams(getTextTureParams());
        } else {
            this.mBackImage.setLayoutParams(getVideoLayoutParams());
        }
    }

    public void setChargeUri(String str) {
        this.mChargeUri = str;
    }

    public void setRapidChargeUri(String str) {
        this.mRapidChargeUri = str;
    }

    public void setStrongRapidChargeUri(String str) {
        this.mStrongRapidChargeUri = str;
    }

    public int getVideoHeight() {
        this.mWindowManager.getDefaultDisplay().getRealSize(this.mPoint);
        Point point = this.mPoint;
        return (int) (((((float) Math.min(point.x, point.y)) * 1.0f) / 1080.0f) * 2340.0f);
    }

    /* access modifiers changed from: protected */
    public RelativeLayout.LayoutParams getVideoLayoutParams() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, getVideoHeight());
        layoutParams.addRule(13);
        return layoutParams;
    }

    /* access modifiers changed from: protected */
    public RelativeLayout.LayoutParams getFoldingVideoLayoutParams() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -1);
        layoutParams.addRule(13);
        return layoutParams;
    }

    /* access modifiers changed from: protected */
    public RelativeLayout.LayoutParams getTextTureParams() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(this.mVideoWidth, this.mVideoHeight);
        layoutParams.addRule(13);
        return layoutParams;
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        if (this.mIsFoldChargeVideo) {
            if ((this.mConfiguration.updateFrom(configuration) & 2048) != 0) {
                checkScreenSize(false);
            }
        }
    }

    private void checkScreenSize(boolean z) {
        Point point = new Point();
        this.mWindowManager.getDefaultDisplay().getRealSize(point);
        boolean z2 = !this.mScreenSize.equals(point.x, point.y);
        if (z2 || z) {
            this.mScreenSize.set(point.x, point.y);
            updateSizeForScreenSizeChange();
            updateLayoutParamForScreenSizeChange();
            if (z2) {
                updateDataSourceForScreenSizeChange();
            }
        }
    }

    private void updateSizeForScreenSizeChange() {
        Configuration configuration = getResources().getConfiguration();
        this.mConfiguration.updateFrom(configuration);
        int i = configuration.screenLayout & 15;
        if (i == 3 || i == 4) {
            this.mVideoWidth = 1080;
            this.mVideoHeight = 2340;
            return;
        }
        this.mVideoWidth = -1;
        this.mVideoHeight = -1;
    }

    /* access modifiers changed from: protected */
    public void updateLayoutParamForScreenSizeChange() {
        ImageView imageView = this.mBackImage;
        if (imageView != null && imageView.isAttachedToWindow()) {
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) this.mBackImage.getLayoutParams();
            layoutParams.width = this.mVideoWidth;
            layoutParams.height = this.mVideoHeight;
            this.mBackImage.setLayoutParams(layoutParams);
        }
        TextureView textureView = this.mChargeView;
        if (textureView != null && textureView.isAttachedToWindow()) {
            RelativeLayout.LayoutParams layoutParams2 = (RelativeLayout.LayoutParams) this.mChargeView.getLayoutParams();
            layoutParams2.width = this.mVideoWidth;
            layoutParams2.height = this.mVideoHeight;
            this.mChargeView.setLayoutParams(layoutParams2);
        }
        TextureView textureView2 = this.mRapidChargeView;
        if (textureView2 != null && textureView2.isAttachedToWindow()) {
            RelativeLayout.LayoutParams layoutParams3 = (RelativeLayout.LayoutParams) this.mRapidChargeView.getLayoutParams();
            layoutParams3.width = this.mVideoWidth;
            layoutParams3.height = this.mVideoHeight;
            this.mRapidChargeView.setLayoutParams(layoutParams3);
        }
        TextureView textureView3 = this.mStrongRapidChargeView;
        if (textureView3 != null && textureView3.isAttachedToWindow()) {
            RelativeLayout.LayoutParams layoutParams4 = (RelativeLayout.LayoutParams) this.mStrongRapidChargeView.getLayoutParams();
            layoutParams4.width = this.mVideoWidth;
            layoutParams4.height = this.mVideoHeight;
            this.mStrongRapidChargeView.setLayoutParams(layoutParams4);
        }
    }

    private void updateDataSourceForScreenSizeChange() {
        ExecutorHelper.getIOThreadPool().execute(new Runnable() {
            /* class com.android.keyguard.charge.video.$$Lambda$VideoView$zBd1P0dVfIAf_HjLtFDAEbcaEJs */

            public final void run() {
                VideoView.this.lambda$updateDataSourceForScreenSizeChange$0$VideoView();
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$updateDataSourceForScreenSizeChange$0 */
    public /* synthetic */ void lambda$updateDataSourceForScreenSizeChange$0$VideoView() {
        try {
            if (this.mMediaPlayer != null) {
                this.mMediaPlayer.reset();
                this.mMediaPlayer.setDataSource(this.mContext, Uri.parse(this.mChargeUri));
                this.mMediaPlayer.prepareAsync();
                this.mMediaPlayer.setLooping(true);
            }
            if (this.mRapidMediaPlayer != null) {
                this.mRapidMediaPlayer.reset();
                this.mRapidMediaPlayer.setDataSource(this.mContext, Uri.parse(this.mRapidChargeUri));
                this.mRapidMediaPlayer.prepareAsync();
                this.mRapidMediaPlayer.setLooping(true);
            }
            if (this.mStrongRapidMediaPlayer != null) {
                this.mStrongRapidMediaPlayer.reset();
                this.mStrongRapidMediaPlayer.setDataSource(this.mContext, Uri.parse(this.mStrongRapidChargeUri));
                this.mStrongRapidMediaPlayer.prepareAsync();
                this.mStrongRapidMediaPlayer.setLooping(true);
            }
        } catch (Exception e) {
            Log.e("ChargeVideoView", "update charge video exception:", e);
        }
    }

    public void switchToStrongRapidChargeAnim() {
        Property property = RelativeLayout.ALPHA;
        if (!(this.mChargeView == null && this.mRapidChargeView == null) && !this.mToStrongRapidAnimatorSet.isRunning()) {
            if (this.mStrongRapidChargeView == null) {
                addStrongRapidChargeView();
            }
            TextureView textureView = this.mChargeView;
            if (textureView != null) {
                this.alphaStrongOut = ObjectAnimator.ofFloat(textureView, property, 1.0f, 0.0f);
            }
            TextureView textureView2 = this.mRapidChargeView;
            if (textureView2 != null) {
                this.alphaStrongOut = ObjectAnimator.ofFloat(textureView2, property, 1.0f, 0.0f);
            }
            this.mToStrongRapidAnimatorSet.play(ObjectAnimator.ofFloat(this.mStrongRapidChargeView, property, 0.0f, 1.0f)).with(this.alphaStrongOut);
            this.mToStrongRapidAnimatorSet.addListener(new Animator.AnimatorListener() {
                /* class com.android.keyguard.charge.video.VideoView.AnonymousClass4 */

                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                    if (VideoView.this.mChargeView != null) {
                        VideoView.this.mChargeView.setAlpha(1.0f);
                    }
                    if (VideoView.this.mRapidChargeView != null) {
                        VideoView.this.mRapidChargeView.setAlpha(1.0f);
                    }
                    if (VideoView.this.mStrongRapidChargeView != null) {
                        VideoView.this.mStrongRapidChargeView.setAlpha(0.0f);
                    }
                }

                public void onAnimationEnd(Animator animator) {
                    if (VideoView.this.mStrongRapidChargeView != null) {
                        VideoView.this.mStrongRapidChargeView.setAlpha(1.0f);
                    }
                    if (VideoView.this.mChargeView != null) {
                        VideoView.this.mChargeView.setAlpha(0.0f);
                        VideoView.this.removeChargeView();
                    }
                    if (VideoView.this.mRapidChargeView != null) {
                        VideoView.this.mRapidChargeView.setAlpha(0.0f);
                        VideoView.this.removeRapidChargeView();
                    }
                }
            });
            this.mToStrongRapidAnimatorSet.setDuration(600L);
            this.mToStrongRapidAnimatorSet.start();
        }
    }

    public void switchToRapidChargeAnim() {
        Property property = RelativeLayout.ALPHA;
        if (!(this.mChargeView == null && this.mStrongRapidChargeView == null) && !this.mToRapidAnimatorSet.isRunning()) {
            if (this.mRapidChargeView == null) {
                addRapidChargeView();
            }
            TextureView textureView = this.mChargeView;
            if (textureView != null) {
                this.alphaRapidOut = ObjectAnimator.ofFloat(textureView, property, 1.0f, 0.0f);
            }
            TextureView textureView2 = this.mStrongRapidChargeView;
            if (textureView2 != null) {
                this.alphaRapidOut = ObjectAnimator.ofFloat(textureView2, property, 1.0f, 0.0f);
            }
            this.mToRapidAnimatorSet.play(this.alphaRapidOut).with(ObjectAnimator.ofFloat(this.mRapidChargeView, property, 0.0f, 1.0f));
            this.mToRapidAnimatorSet.addListener(new Animator.AnimatorListener() {
                /* class com.android.keyguard.charge.video.VideoView.AnonymousClass5 */

                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                    if (VideoView.this.mChargeView != null) {
                        VideoView.this.mChargeView.setAlpha(1.0f);
                    }
                    if (VideoView.this.mStrongRapidChargeView != null) {
                        VideoView.this.mStrongRapidChargeView.setAlpha(1.0f);
                    }
                    if (VideoView.this.mRapidChargeView != null) {
                        VideoView.this.mRapidChargeView.setAlpha(0.0f);
                    }
                }

                public void onAnimationEnd(Animator animator) {
                    if (VideoView.this.mRapidChargeView != null) {
                        VideoView.this.mRapidChargeView.setAlpha(1.0f);
                    }
                    if (VideoView.this.mStrongRapidChargeView != null) {
                        VideoView.this.mStrongRapidChargeView.setAlpha(0.0f);
                        VideoView.this.removeStrongRapidChargeView();
                    }
                    if (VideoView.this.mChargeView != null) {
                        VideoView.this.mChargeView.setAlpha(0.0f);
                        VideoView.this.removeChargeView();
                    }
                }
            });
            this.mToRapidAnimatorSet.setDuration(600L);
            this.mToRapidAnimatorSet.start();
        }
    }

    public void switchToNormalChargeAnim() {
        Property property = RelativeLayout.ALPHA;
        if (!(this.mRapidChargeView == null && this.mStrongRapidChargeView == null) && !this.mToNormalAnimatorSet.isRunning()) {
            if (this.mChargeView == null) {
                addChargeView();
            }
            TextureView textureView = this.mRapidChargeView;
            if (textureView != null) {
                this.alphaOut = ObjectAnimator.ofFloat(textureView, property, 1.0f, 0.0f);
            }
            TextureView textureView2 = this.mStrongRapidChargeView;
            if (textureView2 != null) {
                this.alphaOut = ObjectAnimator.ofFloat(textureView2, property, 1.0f, 0.0f);
            }
            this.mToNormalAnimatorSet.play(this.alphaOut).with(ObjectAnimator.ofFloat(this.mChargeView, property, 0.0f, 1.0f));
            this.mToNormalAnimatorSet.addListener(new Animator.AnimatorListener() {
                /* class com.android.keyguard.charge.video.VideoView.AnonymousClass6 */

                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                    if (VideoView.this.mRapidChargeView != null) {
                        VideoView.this.mRapidChargeView.setAlpha(1.0f);
                    }
                    if (VideoView.this.mStrongRapidChargeView != null) {
                        VideoView.this.mStrongRapidChargeView.setAlpha(1.0f);
                    }
                    if (VideoView.this.mChargeView != null) {
                        VideoView.this.mChargeView.setAlpha(0.0f);
                    }
                }

                public void onAnimationEnd(Animator animator) {
                    if (VideoView.this.mChargeView != null) {
                        VideoView.this.mChargeView.setAlpha(1.0f);
                    }
                    if (VideoView.this.mStrongRapidChargeView != null) {
                        VideoView.this.mStrongRapidChargeView.setAlpha(0.0f);
                        VideoView.this.removeStrongRapidChargeView();
                    }
                    if (VideoView.this.mRapidChargeView != null) {
                        VideoView.this.mRapidChargeView.setAlpha(0.0f);
                        VideoView.this.removeRapidChargeView();
                    }
                }
            });
            this.mToNormalAnimatorSet.setDuration(600L);
            this.mToNormalAnimatorSet.start();
        }
    }

    public void stopAnimation() {
        AnimatorSet animatorSet = this.mToNormalAnimatorSet;
        if (animatorSet != null && animatorSet.isRunning()) {
            this.mToNormalAnimatorSet.cancel();
        }
        AnimatorSet animatorSet2 = this.mToRapidAnimatorSet;
        if (animatorSet2 != null && animatorSet2.isRunning()) {
            this.mToRapidAnimatorSet.cancel();
        }
        AnimatorSet animatorSet3 = this.mToStrongRapidAnimatorSet;
        if (animatorSet3 != null && animatorSet3.isRunning()) {
            this.mToStrongRapidAnimatorSet.cancel();
        }
    }

    /* access modifiers changed from: protected */
    public void addChargeView() {
        this.mChargeView = new TextureView(this.mContext);
        MediaPlayer mediaPlayer = new MediaPlayer();
        this.mMediaPlayer = mediaPlayer;
        mediaPlayer.setOnPreparedListener(this.mOnPreparedListener);
        this.mMediaPlayer.setOnCompletionListener(this.mOnCompletionListener);
        this.mMediaPlayer.setOnErrorListener(this.onErrorListener);
        if (this.mIsFoldChargeVideo) {
            checkScreenSize(true);
            addView(this.mChargeView, -1, getTextTureParams());
        } else {
            addView(this.mChargeView, -1, getVideoLayoutParams());
        }
        this.mChargeView.setSurfaceTextureListener(this.mChargeSurfaceTextureListener);
    }

    /* access modifiers changed from: protected */
    public void removeChargeView() {
        TextureView textureView = this.mChargeView;
        if (textureView != null) {
            removeView(textureView);
            this.mChargeView = null;
        }
    }

    /* access modifiers changed from: protected */
    public void addRapidChargeView() {
        this.mRapidChargeView = new TextureView(this.mContext);
        MediaPlayer mediaPlayer = new MediaPlayer();
        this.mRapidMediaPlayer = mediaPlayer;
        mediaPlayer.setOnPreparedListener(this.mOnPreparedListener);
        this.mRapidMediaPlayer.setOnCompletionListener(this.mOnCompletionListener);
        this.mRapidMediaPlayer.setOnErrorListener(this.onErrorListener);
        if (this.mIsFoldChargeVideo) {
            checkScreenSize(true);
            addView(this.mRapidChargeView, -1, getTextTureParams());
        } else {
            addView(this.mRapidChargeView, -1, getVideoLayoutParams());
        }
        this.mRapidChargeView.setSurfaceTextureListener(this.mRapidChargeSurfaceTextureListener);
    }

    /* access modifiers changed from: protected */
    public void removeRapidChargeView() {
        TextureView textureView = this.mRapidChargeView;
        if (textureView != null) {
            removeView(textureView);
            this.mRapidChargeView = null;
        }
    }

    /* access modifiers changed from: protected */
    public void addStrongRapidChargeView() {
        this.mStrongRapidChargeView = new TextureView(this.mContext);
        MediaPlayer mediaPlayer = new MediaPlayer();
        this.mStrongRapidMediaPlayer = mediaPlayer;
        mediaPlayer.setOnPreparedListener(this.mOnPreparedListener);
        this.mStrongRapidMediaPlayer.setOnCompletionListener(this.mOnCompletionListener);
        this.mStrongRapidMediaPlayer.setOnErrorListener(this.onErrorListener);
        if (this.mIsFoldChargeVideo) {
            checkScreenSize(true);
            addView(this.mStrongRapidChargeView, -1, getTextTureParams());
        } else {
            addView(this.mStrongRapidChargeView, -1, getVideoLayoutParams());
        }
        this.mStrongRapidChargeView.setSurfaceTextureListener(this.mStrongRapidChargeSurfaceTextureListener);
    }

    /* access modifiers changed from: protected */
    public void removeStrongRapidChargeView() {
        TextureView textureView = this.mStrongRapidChargeView;
        if (textureView != null) {
            removeView(textureView);
            this.mStrongRapidChargeView = null;
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mBackImage.setBackground(null);
    }
}

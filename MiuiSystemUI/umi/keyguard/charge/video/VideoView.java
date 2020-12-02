package com.android.keyguard.charge.video;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
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
import com.android.systemui.C0013R$drawable;

public class VideoView extends RelativeLayout {
    private ObjectAnimator alphaOut;
    private ObjectAnimator alphaRapidOut;
    private ObjectAnimator alphaStrongOut;
    private ImageView mBackImage;
    private TextureView.SurfaceTextureListener mChargeSurfaceTextureListener;
    /* access modifiers changed from: private */
    public String mChargeUri;
    /* access modifiers changed from: private */
    public TextureView mChargeView;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public MediaPlayer mMediaPlayer;
    MediaPlayer.OnCompletionListener mOnCompletionListener;
    MediaPlayer.OnPreparedListener mOnPreparedListener;
    private Point mPoint;
    private TextureView.SurfaceTextureListener mRapidChargeSurfaceTextureListener;
    /* access modifiers changed from: private */
    public String mRapidChargeUri;
    /* access modifiers changed from: private */
    public TextureView mRapidChargeView;
    /* access modifiers changed from: private */
    public MediaPlayer mRapidMediaPlayer;
    private TextureView.SurfaceTextureListener mStrongRapidChargeSurfaceTextureListener;
    /* access modifiers changed from: private */
    public String mStrongRapidChargeUri;
    /* access modifiers changed from: private */
    public TextureView mStrongRapidChargeView;
    /* access modifiers changed from: private */
    public MediaPlayer mStrongRapidMediaPlayer;
    private AnimatorSet mToNormalAnimatorSet;
    private AnimatorSet mToRapidAnimatorSet;
    private AnimatorSet mToStrongRapidAnimatorSet;
    private WindowManager mWindowManager;
    MediaPlayer.OnErrorListener onErrorListener;

    public VideoView(Context context) {
        this(context, (AttributeSet) null);
    }

    public VideoView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public VideoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mToStrongRapidAnimatorSet = new AnimatorSet();
        this.mToRapidAnimatorSet = new AnimatorSet();
        this.mToNormalAnimatorSet = new AnimatorSet();
        this.mOnPreparedListener = new MediaPlayer.OnPreparedListener(this) {
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
        };
        this.onErrorListener = new MediaPlayer.OnErrorListener(this) {
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                Log.e("ChargeVideoView", "error in playing charge video " + i + " " + i2);
                return true;
            }
        };
        this.mOnCompletionListener = new MediaPlayer.OnCompletionListener(this) {
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
        };
        this.mChargeSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }

            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
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
                MediaPlayer unused = VideoView.this.mMediaPlayer = null;
                return false;
            }
        };
        this.mRapidChargeSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }

            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
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
                MediaPlayer unused = VideoView.this.mRapidMediaPlayer = null;
                return false;
            }
        };
        this.mStrongRapidChargeSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
            public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
            }

            public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            }

            public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
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
                MediaPlayer unused = VideoView.this.mStrongRapidMediaPlayer = null;
                return false;
            }
        };
        this.mContext = context;
        this.mWindowManager = (WindowManager) getContext().getSystemService("window");
        this.mPoint = new Point();
        ImageView imageView = new ImageView(context);
        this.mBackImage = imageView;
        imageView.setBackgroundResource(C0013R$drawable.wired_charge_video_bg_img);
        addView(this.mBackImage, getVideoLayoutParams());
    }

    public void setDefaultImage(int i) {
        this.mBackImage.setBackgroundResource(i);
        this.mBackImage.setLayoutParams(getVideoLayoutParams());
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

    public void switchToStrongRapidChargeAnim() {
        Property property = RelativeLayout.ALPHA;
        if (!(this.mChargeView == null && this.mRapidChargeView == null) && !this.mToStrongRapidAnimatorSet.isRunning()) {
            if (this.mStrongRapidChargeView == null) {
                addStrongRapidChargeView();
            }
            TextureView textureView = this.mChargeView;
            if (textureView != null) {
                this.alphaStrongOut = ObjectAnimator.ofFloat(textureView, property, new float[]{1.0f, 0.0f});
            }
            TextureView textureView2 = this.mRapidChargeView;
            if (textureView2 != null) {
                this.alphaStrongOut = ObjectAnimator.ofFloat(textureView2, property, new float[]{1.0f, 0.0f});
            }
            this.mToStrongRapidAnimatorSet.play(ObjectAnimator.ofFloat(this.mStrongRapidChargeView, property, new float[]{0.0f, 1.0f})).with(this.alphaStrongOut);
            this.mToStrongRapidAnimatorSet.addListener(new Animator.AnimatorListener() {
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
            this.mToStrongRapidAnimatorSet.setDuration(600);
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
                this.alphaRapidOut = ObjectAnimator.ofFloat(textureView, property, new float[]{1.0f, 0.0f});
            }
            TextureView textureView2 = this.mStrongRapidChargeView;
            if (textureView2 != null) {
                this.alphaRapidOut = ObjectAnimator.ofFloat(textureView2, property, new float[]{1.0f, 0.0f});
            }
            this.mToRapidAnimatorSet.play(this.alphaRapidOut).with(ObjectAnimator.ofFloat(this.mRapidChargeView, property, new float[]{0.0f, 1.0f}));
            this.mToRapidAnimatorSet.addListener(new Animator.AnimatorListener() {
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
            this.mToRapidAnimatorSet.setDuration(600);
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
                this.alphaOut = ObjectAnimator.ofFloat(textureView, property, new float[]{1.0f, 0.0f});
            }
            TextureView textureView2 = this.mStrongRapidChargeView;
            if (textureView2 != null) {
                this.alphaOut = ObjectAnimator.ofFloat(textureView2, property, new float[]{1.0f, 0.0f});
            }
            this.mToNormalAnimatorSet.play(this.alphaOut).with(ObjectAnimator.ofFloat(this.mChargeView, property, new float[]{0.0f, 1.0f}));
            this.mToNormalAnimatorSet.addListener(new Animator.AnimatorListener() {
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
            this.mToNormalAnimatorSet.setDuration(600);
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
        addView(this.mChargeView, -1, getVideoLayoutParams());
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
        addView(this.mRapidChargeView, -1, getVideoLayoutParams());
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
        addView(this.mStrongRapidChargeView, -1, getVideoLayoutParams());
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
}

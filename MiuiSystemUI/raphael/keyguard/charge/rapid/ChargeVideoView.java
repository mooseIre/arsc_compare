package com.android.keyguard.charge.rapid;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import com.android.systemui.plugins.R;

public class ChargeVideoView extends RelativeLayout {
    private ImageView mBackImage;
    private TextureView.SurfaceTextureListener mChargeSurfaceTextureListener;
    /* access modifiers changed from: private */
    public String mChargeUri;
    /* access modifiers changed from: private */
    public TextureView mChargeView;
    /* access modifiers changed from: private */
    public MediaPlayer mMediaPlayer;
    MediaPlayer.OnCompletionListener mOnCompletionListener;
    MediaPlayer.OnPreparedListener mOnPreparedListener;
    private TextureView.SurfaceTextureListener mRapidChargeSurfaceTextureListener;
    /* access modifiers changed from: private */
    public String mRapidChargeUri;
    /* access modifiers changed from: private */
    public TextureView mRapidChargeView;
    /* access modifiers changed from: private */
    public MediaPlayer mRapidMediaPlayer;
    private AnimatorSet mToNormalAnimatorSet;
    private AnimatorSet mToRapidAnimatorSet;

    public ChargeVideoView(Context context) {
        this(context, (AttributeSet) null);
    }

    public ChargeVideoView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ChargeVideoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mToRapidAnimatorSet = new AnimatorSet();
        this.mToNormalAnimatorSet = new AnimatorSet();
        this.mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
            public void onPrepared(MediaPlayer mediaPlayer) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
        };
        this.mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
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
                    if (ChargeVideoView.this.mMediaPlayer != null) {
                        ChargeVideoView.this.mMediaPlayer.setSurface(new Surface(surfaceTexture));
                        ChargeVideoView.this.mMediaPlayer.setDataSource(ChargeVideoView.this.mContext, Uri.parse(ChargeVideoView.this.mChargeUri));
                        ChargeVideoView.this.mMediaPlayer.prepareAsync();
                        ChargeVideoView.this.mMediaPlayer.setLooping(true);
                    }
                } catch (Exception e) {
                    Log.e("ChargeVideoView", "play charge video exception:", e);
                }
            }

            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                if (ChargeVideoView.this.mMediaPlayer == null) {
                    return false;
                }
                ChargeVideoView.this.mMediaPlayer.pause();
                ChargeVideoView.this.mMediaPlayer.stop();
                ChargeVideoView.this.mMediaPlayer.release();
                MediaPlayer unused = ChargeVideoView.this.mMediaPlayer = null;
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
                    if (ChargeVideoView.this.mRapidMediaPlayer != null) {
                        ChargeVideoView.this.mRapidMediaPlayer.setSurface(new Surface(surfaceTexture));
                        ChargeVideoView.this.mRapidMediaPlayer.setDataSource(ChargeVideoView.this.mContext, Uri.parse(ChargeVideoView.this.mRapidChargeUri));
                        ChargeVideoView.this.mRapidMediaPlayer.prepareAsync();
                        ChargeVideoView.this.mRapidMediaPlayer.setLooping(true);
                    }
                } catch (Exception e) {
                    Log.e("ChargeVideoView", "play rapid charge video exception:", e);
                }
            }

            public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                if (ChargeVideoView.this.mRapidMediaPlayer == null) {
                    return false;
                }
                ChargeVideoView.this.mRapidMediaPlayer.pause();
                ChargeVideoView.this.mRapidMediaPlayer.stop();
                ChargeVideoView.this.mRapidMediaPlayer.release();
                MediaPlayer unused = ChargeVideoView.this.mRapidMediaPlayer = null;
                return false;
            }
        };
        this.mBackImage = new ImageView(this.mContext);
        this.mBackImage.setBackgroundResource(R.drawable.wired_charge_video_bg_img);
        addView(this.mBackImage, getVideoLayoutParams());
    }

    public void setChargeUri(String str) {
        this.mChargeUri = str;
    }

    public void setRapidChargeUri(String str) {
        this.mRapidChargeUri = str;
    }

    /* access modifiers changed from: protected */
    public RelativeLayout.LayoutParams getVideoLayoutParams() {
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(1080, 2340);
        layoutParams.addRule(13);
        return layoutParams;
    }

    /* access modifiers changed from: protected */
    public void switchToRapidChargeAnim() {
        if (this.mChargeView != null && !this.mToRapidAnimatorSet.isRunning()) {
            if (this.mRapidChargeView == null) {
                addRapidChargeView();
            }
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mChargeView, RelativeLayout.ALPHA, new float[]{1.0f, 0.0f});
            this.mToRapidAnimatorSet.play(ofFloat).with(ObjectAnimator.ofFloat(this.mRapidChargeView, RelativeLayout.ALPHA, new float[]{0.0f, 1.0f}));
            this.mToRapidAnimatorSet.addListener(new Animator.AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                    ChargeVideoView.this.mChargeView.setAlpha(1.0f);
                    ChargeVideoView.this.mRapidChargeView.setAlpha(0.0f);
                }

                public void onAnimationEnd(Animator animator) {
                    ChargeVideoView.this.mRapidChargeView.setAlpha(1.0f);
                    if (ChargeVideoView.this.mChargeView != null) {
                        ChargeVideoView.this.mChargeView.setAlpha(0.0f);
                        ChargeVideoView.this.removeChargeView();
                    }
                }
            });
            this.mToRapidAnimatorSet.setDuration(600);
            this.mToRapidAnimatorSet.start();
        }
    }

    /* access modifiers changed from: protected */
    public void switchToNormalChargeAnim() {
        if (this.mRapidChargeView != null && !this.mToNormalAnimatorSet.isRunning()) {
            if (this.mChargeView == null) {
                addChargeView();
            }
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(this.mRapidChargeView, RelativeLayout.ALPHA, new float[]{1.0f, 0.0f});
            this.mToNormalAnimatorSet.play(ofFloat).with(ObjectAnimator.ofFloat(this.mChargeView, RelativeLayout.ALPHA, new float[]{0.0f, 1.0f}));
            this.mToNormalAnimatorSet.addListener(new Animator.AnimatorListener() {
                public void onAnimationCancel(Animator animator) {
                }

                public void onAnimationRepeat(Animator animator) {
                }

                public void onAnimationStart(Animator animator) {
                    ChargeVideoView.this.mRapidChargeView.setAlpha(1.0f);
                    ChargeVideoView.this.mChargeView.setAlpha(0.0f);
                }

                public void onAnimationEnd(Animator animator) {
                    ChargeVideoView.this.mChargeView.setAlpha(1.0f);
                    if (ChargeVideoView.this.mRapidChargeView != null) {
                        ChargeVideoView.this.mRapidChargeView.setAlpha(0.0f);
                        ChargeVideoView.this.removeRapidChargeView();
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
    }

    /* access modifiers changed from: protected */
    public void addChargeView() {
        this.mChargeView = new TextureView(this.mContext);
        this.mMediaPlayer = new MediaPlayer();
        this.mMediaPlayer.setOnPreparedListener(this.mOnPreparedListener);
        this.mMediaPlayer.setOnCompletionListener(this.mOnCompletionListener);
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
        this.mRapidMediaPlayer = new MediaPlayer();
        this.mRapidMediaPlayer.setOnPreparedListener(this.mOnPreparedListener);
        this.mRapidMediaPlayer.setOnCompletionListener(this.mOnCompletionListener);
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
}

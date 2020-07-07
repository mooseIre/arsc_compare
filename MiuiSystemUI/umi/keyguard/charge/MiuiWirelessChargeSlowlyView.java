package com.android.keyguard.charge;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import com.android.systemui.plugins.R;
import java.io.IOException;

public class MiuiWirelessChargeSlowlyView {
    /* access modifiers changed from: private */
    public Context mContext;
    private AlertDialog mDialog;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    /* access modifiers changed from: private */
    public ImageView mImageView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        /* access modifiers changed from: private */
        public MediaPlayer mMediaPlayer;

        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
            MiuiWirelessChargeSlowlyView.this.mImageView.setVisibility(0);
            this.mMediaPlayer = new MediaPlayer();
            this.mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    MiuiWirelessChargeSlowlyView.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (AnonymousClass2.this.mMediaPlayer != null && !AnonymousClass2.this.mMediaPlayer.isPlaying()) {
                                AnonymousClass2.this.mMediaPlayer.start();
                            }
                        }
                    }, 1000);
                }
            });
            this.mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mediaPlayer) {
                    MiuiWirelessChargeSlowlyView.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            MiuiWirelessChargeSlowlyView.this.mImageView.setVisibility(0);
                        }
                    }, 1000);
                    MiuiWirelessChargeSlowlyView.this.mHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (AnonymousClass2.this.mMediaPlayer != null && !AnonymousClass2.this.mMediaPlayer.isPlaying()) {
                                AnonymousClass2.this.mMediaPlayer.start();
                            }
                        }
                    }, 2000);
                }
            });
            this.mMediaPlayer.setSurface(new Surface(surfaceTexture));
            try {
                this.mMediaPlayer.setDataSource(MiuiWirelessChargeSlowlyView.this.mContext, MiuiWirelessChargeSlowlyView.this.getVideoUri());
                this.mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            MiuiWirelessChargeSlowlyView.this.mImageView.setVisibility(0);
            MediaPlayer mediaPlayer = this.mMediaPlayer;
            if (mediaPlayer != null) {
                mediaPlayer.pause();
                this.mMediaPlayer.stop();
                this.mMediaPlayer.release();
                this.mMediaPlayer = null;
            }
            return false;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            if (MiuiWirelessChargeSlowlyView.this.mImageView.getVisibility() != 8) {
                MiuiWirelessChargeSlowlyView.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        if (AnonymousClass2.this.mMediaPlayer != null && AnonymousClass2.this.mMediaPlayer.isPlaying()) {
                            MiuiWirelessChargeSlowlyView.this.mImageView.setVisibility(8);
                        }
                    }
                }, 100);
            }
        }
    };
    private TextureView mTextureView;
    private boolean mTipOnlyOnce;

    public MiuiWirelessChargeSlowlyView(Context context, boolean z) {
        this.mContext = context;
        this.mTipOnlyOnce = z;
    }

    public void show() {
        Log.i("MiuiWirelessChargeSlowlyView", "show: ");
        if (this.mDialog == null) {
            initView();
        }
        this.mDialog.show();
        Button button = this.mDialog.getButton(-2);
        if (button != null) {
            button.setTextColor(-16777216);
            ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
            layoutParams.height = (int) this.mContext.getResources().getDimension(R.dimen.wireless_chagre_slowly_dialog_button_height);
            button.setLayoutParams(layoutParams);
        }
    }

    public void dismiss() {
        Log.i("MiuiWirelessChargeSlowlyView", "dismiss: ");
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        this.mDialog = null;
    }

    private void initView() {
        AnonymousClass1 r1 = null;
        View inflate = View.inflate(this.mContext, R.layout.miui_keyguard_wireless_charge_slowly, (ViewGroup) null);
        this.mImageView = (ImageView) inflate.findViewById(R.id.wireless_charge_picture);
        this.mTextureView = (TextureView) inflate.findViewById(R.id.wireless_charge_slowly_video);
        this.mTextureView.setSurfaceTextureListener(this.mSurfaceTextureListener);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, R.style.wireless_charge_slowly_dialog);
        builder.setCancelable(false);
        builder.setView(inflate);
        if (this.mTipOnlyOnce) {
            r1 = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    SharedPreferences.Editor edit = MiuiWirelessChargeSlowlyView.this.mContext.getSharedPreferences("wireless_charge", 0).edit();
                    edit.putBoolean("show_dialog", false);
                    edit.apply();
                }
            };
        }
        builder.setNegativeButton(R.string.wireless_charge_dialog_cancel, r1);
        this.mDialog = builder.create();
        this.mDialog.getWindow().setType(2010);
        this.mDialog.getWindow().requestFeature(1);
        this.mDialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg_light);
    }

    /* access modifiers changed from: private */
    public Uri getVideoUri() {
        return Uri.parse("android.resource://" + this.mContext.getPackageName() + "/" + R.raw.wireless_charge_slowly_video);
    }
}

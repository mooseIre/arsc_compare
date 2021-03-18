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
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0020R$raw;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import java.io.IOException;

public class MiuiWirelessChargeSlowlyView {
    private Context mContext;
    private AlertDialog mDialog;
    private Handler mHandler = new Handler();
    private ImageView mImageView;
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        /* class com.android.keyguard.charge.MiuiWirelessChargeSlowlyView.AnonymousClass2 */
        private MediaPlayer mMediaPlayer;

        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i2) {
        }

        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i2) {
            MiuiWirelessChargeSlowlyView.this.mImageView.setVisibility(0);
            MediaPlayer mediaPlayer = new MediaPlayer();
            this.mMediaPlayer = mediaPlayer;
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                /* class com.android.keyguard.charge.MiuiWirelessChargeSlowlyView.AnonymousClass2.AnonymousClass1 */

                public void onPrepared(MediaPlayer mediaPlayer) {
                    MiuiWirelessChargeSlowlyView.this.mHandler.postDelayed(new Runnable() {
                        /* class com.android.keyguard.charge.MiuiWirelessChargeSlowlyView.AnonymousClass2.AnonymousClass1.AnonymousClass1 */

                        public void run() {
                            if (AnonymousClass2.this.mMediaPlayer != null && !AnonymousClass2.this.mMediaPlayer.isPlaying()) {
                                AnonymousClass2.this.mMediaPlayer.start();
                            }
                        }
                    }, 1000);
                }
            });
            this.mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                /* class com.android.keyguard.charge.MiuiWirelessChargeSlowlyView.AnonymousClass2.AnonymousClass2 */

                public void onCompletion(MediaPlayer mediaPlayer) {
                    MiuiWirelessChargeSlowlyView.this.mHandler.postDelayed(new Runnable() {
                        /* class com.android.keyguard.charge.MiuiWirelessChargeSlowlyView.AnonymousClass2.AnonymousClass2.AnonymousClass1 */

                        public void run() {
                            MiuiWirelessChargeSlowlyView.this.mImageView.setVisibility(0);
                        }
                    }, 1000);
                    MiuiWirelessChargeSlowlyView.this.mHandler.postDelayed(new Runnable() {
                        /* class com.android.keyguard.charge.MiuiWirelessChargeSlowlyView.AnonymousClass2.AnonymousClass2.AnonymousClass2 */

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
                    /* class com.android.keyguard.charge.MiuiWirelessChargeSlowlyView.AnonymousClass2.AnonymousClass3 */

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
            layoutParams.height = (int) this.mContext.getResources().getDimension(C0012R$dimen.wireless_chagre_slowly_dialog_button_height);
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
        AnonymousClass1 r2 = null;
        View inflate = View.inflate(this.mContext, C0017R$layout.miui_keyguard_wireless_charge_slowly, null);
        this.mImageView = (ImageView) inflate.findViewById(C0015R$id.wireless_charge_picture);
        TextureView textureView = (TextureView) inflate.findViewById(C0015R$id.wireless_charge_slowly_video);
        this.mTextureView = textureView;
        textureView.setSurfaceTextureListener(this.mSurfaceTextureListener);
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, C0022R$style.wireless_charge_slowly_dialog);
        builder.setCancelable(false);
        builder.setView(inflate);
        if (this.mTipOnlyOnce) {
            r2 = new DialogInterface.OnClickListener() {
                /* class com.android.keyguard.charge.MiuiWirelessChargeSlowlyView.AnonymousClass1 */

                public void onClick(DialogInterface dialogInterface, int i) {
                    SharedPreferences.Editor edit = MiuiWirelessChargeSlowlyView.this.mContext.getSharedPreferences("wireless_charge", 0).edit();
                    edit.putBoolean("show_dialog", false);
                    edit.apply();
                }
            };
        }
        builder.setNegativeButton(C0021R$string.wireless_charge_dialog_cancel, r2);
        AlertDialog create = builder.create();
        this.mDialog = create;
        create.getWindow().setType(2010);
        this.mDialog.getWindow().requestFeature(1);
        this.mDialog.getWindow().setBackgroundDrawableResource(C0013R$drawable.dialog_bg_light);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Uri getVideoUri() {
        return Uri.parse("android.resource://" + this.mContext.getPackageName() + "/" + C0020R$raw.wireless_charge_slowly_video);
    }
}

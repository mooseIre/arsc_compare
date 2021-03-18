package com.android.systemui.controlcenter.phone.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.VideoView;
import com.android.systemui.C0011R$color;
import com.android.systemui.R$styleable;

public class CornerVideoView extends VideoView {
    private Uri mUri;

    public CornerVideoView(Context context) {
        super(context);
    }

    public CornerVideoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setCornerRadiusFromAttrs(context, attributeSet);
    }

    public CornerVideoView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        setCornerRadiusFromAttrs(context, attributeSet);
    }

    public CornerVideoView(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        setCornerRadiusFromAttrs(context, attributeSet);
    }

    private void setCornerRadiusFromAttrs(Context context, AttributeSet attributeSet) {
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.CornerVideoView);
        obtainStyledAttributes.recycle();
        setCornerRadius((float) obtainStyledAttributes.getDimensionPixelSize(R$styleable.CornerVideoView_cornerRadius, 0));
    }

    public void setCornerRadius(float f) {
        setOutlineProvider(new VideoViewOutlineProvider(this, f));
        setClipToOutline(true);
    }

    public void play(int i, int i2) {
        if (i2 != 0) {
            setBackground(getContext().getDrawable(i2));
        }
        if (!isPlaying() && i != 0) {
            Uri parse = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + i);
            this.mUri = parse;
            setVideoURI(parse);
            setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                /* class com.android.systemui.controlcenter.phone.widget.CornerVideoView.AnonymousClass1 */

                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.setLooping(true);
                    mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                        /* class com.android.systemui.controlcenter.phone.widget.CornerVideoView.AnonymousClass1.AnonymousClass1 */

                        public boolean onInfo(MediaPlayer mediaPlayer, int i, int i2) {
                            if (i != 3) {
                                return true;
                            }
                            CornerVideoView cornerVideoView = CornerVideoView.this;
                            cornerVideoView.setBackgroundColor(cornerVideoView.getContext().getColor(C0011R$color.miuix_sbl_transparent));
                            return true;
                        }
                    });
                    mediaPlayer.start();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public class VideoViewOutlineProvider extends ViewOutlineProvider {
        private float mRadius;

        public VideoViewOutlineProvider(CornerVideoView cornerVideoView, float f) {
            this.mRadius = f;
        }

        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(new Rect(0, 0, view.getWidth(), view.getHeight()), this.mRadius);
        }
    }
}

package com.android.systemui.statusbar.notification.mediacontrol;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatImageView;
import com.android.systemui.C0012R$dimen;

public class RoundedSideImageView extends AppCompatImageView {
    boolean isLTR;
    private Path path;
    private float radius;

    public RoundedSideImageView(Context context) {
        super(context);
        init(context);
    }

    public RoundedSideImageView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
    }

    public RoundedSideImageView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init(context);
    }

    private void init(Context context) {
        this.radius = context.getResources().getDimension(C0012R$dimen.media_control_bg_radius);
        this.path = new Path();
        this.isLTR = context.getResources().getConfiguration().getLayoutDirection() == 0;
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();
        float f = (float) width;
        float f2 = this.radius;
        if (f > f2) {
            float f3 = (float) height;
            if (f3 > f2) {
                this.path.reset();
                if (this.isLTR) {
                    this.path.moveTo(0.0f, 0.0f);
                    float f4 = (float) ((width * 3) / 4);
                    this.path.lineTo(f4, 0.0f);
                    this.path.lineTo(f, (float) (height / 4));
                    this.path.lineTo(f, (float) ((height * 3) / 4));
                    this.path.lineTo(f4, f3);
                    this.path.lineTo(0.0f, f3);
                    this.path.lineTo(0.0f, 0.0f);
                    float f5 = this.radius;
                    this.path.addRoundRect((float) (width / 2), 0.0f, f, f3, f5, f5, Path.Direction.CW);
                } else {
                    this.path.moveTo(f, 0.0f);
                    float f6 = (float) (width / 4);
                    this.path.lineTo(f6, 0.0f);
                    this.path.lineTo(0.0f, (float) (height / 4));
                    this.path.lineTo(0.0f, (float) ((height * 3) / 4));
                    this.path.lineTo(f6, f3);
                    this.path.lineTo(f, f3);
                    this.path.lineTo(f, 0.0f);
                    float f7 = this.radius;
                    this.path.addRoundRect(0.0f, 0.0f, (float) (width / 2), f3, f7, f7, Path.Direction.CCW);
                }
                canvas.clipPath(this.path);
            }
        }
        super.onDraw(canvas);
    }
}

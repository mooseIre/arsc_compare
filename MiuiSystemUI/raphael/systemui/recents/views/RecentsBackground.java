package com.android.systemui.recents.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Display;
import android.view.WindowManager;
import com.android.systemui.statusbar.AlphaOptimizedView;

public class RecentsBackground extends AlphaOptimizedView {
    private Display mDisplay;
    private int mScreenHeight;
    private int mScreenWidth;

    public RecentsBackground(Context context) {
        this(context, (AttributeSet) null);
    }

    public RecentsBackground(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public RecentsBackground(Context context, AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public RecentsBackground(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.mDisplay = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        Point point = new Point();
        this.mDisplay.getRealSize(point);
        this.mScreenWidth = Math.min(point.x, point.y);
        this.mScreenHeight = Math.max(point.x, point.y);
    }

    public void draw(Canvas canvas) {
        int i;
        int i2;
        Drawable background = getBackground();
        if (background != null) {
            canvas.save();
            if (background instanceof ColorDrawable) {
                background.setBounds(0, 0, getWidth(), getHeight());
            } else {
                int rotation = this.mDisplay.getRotation();
                int[] locationOnScreen = getLocationOnScreen();
                if (rotation == 1) {
                    canvas.rotate(-90.0f, (float) (canvas.getHeight() / 2), (float) (canvas.getHeight() / 2));
                    i2 = locationOnScreen[0];
                } else if (rotation == 3) {
                    canvas.rotate(90.0f, (float) (canvas.getWidth() / 2), (float) (canvas.getWidth() / 2));
                    i = 0;
                    background.setBounds(0, i, this.mScreenWidth, this.mScreenHeight + i);
                } else {
                    i2 = locationOnScreen[1];
                }
                i = -i2;
                background.setBounds(0, i, this.mScreenWidth, this.mScreenHeight + i);
            }
            background.draw(canvas);
            canvas.restore();
        }
    }
}

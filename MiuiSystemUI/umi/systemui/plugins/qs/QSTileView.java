package com.android.systemui.plugins.qs;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import com.android.systemui.plugins.qs.QSTile;

@ProvidesInterface(version = 2)
public abstract class QSTileView extends LinearLayout {
    public static final int VERSION = 2;
    private float mLastX = 0.0f;
    private float mLastY = 0.0f;

    public abstract int getDetailY();

    public abstract QSIconView getIcon();

    public abstract View getIconWithBackground();

    public abstract void init(QSTile qSTile);

    public abstract void onStateChanged(QSTile.State state);

    public abstract View updateAccessibilityOrder(View view);

    public QSTileView(Context context) {
        super(context);
    }

    public float getLastX() {
        return this.mLastX;
    }

    public float getLastY() {
        return this.mLastY;
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        this.mLastX = motionEvent.getX();
        this.mLastY = motionEvent.getY();
        return super.dispatchTouchEvent(motionEvent);
    }
}

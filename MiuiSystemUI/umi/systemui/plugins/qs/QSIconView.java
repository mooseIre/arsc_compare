package com.android.systemui.plugins.qs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import com.android.systemui.plugins.qs.QSTile;

@ProvidesInterface(version = 1)
public abstract class QSIconView extends ViewGroup {
    public static final int VERSION = 1;

    public abstract View getIconView();

    public abstract void setAnimationEnabled(boolean z);

    public abstract void setIcon(QSTile.State state);

    public abstract void setIsCustomTile(boolean z);

    public abstract void updateResources();

    public QSIconView(Context context) {
        super(context);
    }

    public QSIconView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }
}

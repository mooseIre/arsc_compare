package com.android.systemui.plugins.qs;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import com.android.systemui.plugins.annotations.Dependencies;
import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;
import com.android.systemui.plugins.qs.QSTile;

@Dependencies({@DependsOn(target = QSIconView.class), @DependsOn(target = QSTile.class)})
@ProvidesInterface(version = 2)
public abstract class QSTileView extends LinearLayout {
    public static final int VERSION = 2;

    public abstract int getDetailY();

    public abstract QSIconView getIcon();

    public abstract View getIconWithBackground();

    public abstract void init(QSTile qSTile);

    public abstract void onStateChanged(QSTile.State state);

    public abstract View updateAccessibilityOrder(View view);

    public QSTileView(Context context) {
        super(context);
    }
}

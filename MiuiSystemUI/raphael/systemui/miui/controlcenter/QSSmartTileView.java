package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.plugins.R;

public class QSSmartTileView extends LinearLayout {
    private ImageView mExpandIndicator;
    private TextView mStatusView;
    private ImageView mTileIcon;
    private TextView mTitle;

    public QSSmartTileView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LayoutInflater.from(context).inflate(R.layout.qs_control_smart_tile, this, false);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mTitle = (TextView) findViewById(R.id.title);
        this.mStatusView = (TextView) findViewById(R.id.status);
        this.mTileIcon = (ImageView) findViewById(R.id.tile_icon);
        this.mExpandIndicator = (ImageView) findViewById(R.id.expand_indicator);
    }
}

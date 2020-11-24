package com.android.systemui.controlcenter.phone.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.controlcenter.qs.tileview.CCQSTileView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.tiles.AutoBrightnessTile;

public class AutoBrightnessView extends FrameLayout {
    private AutoBrightnessTile mAutoBrightnessTile;
    /* access modifiers changed from: private */
    public QSTileView mAutoBrightnessView;
    private QSTileHost mHost;
    private boolean mListening;

    public AutoBrightnessView(Context context) {
        this(context, (AttributeSet) null);
    }

    public AutoBrightnessView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
        AutoBrightnessTile autoBrightnessTile = (AutoBrightnessTile) qSTileHost.createTile("autobrightness");
        this.mAutoBrightnessTile = autoBrightnessTile;
        autoBrightnessTile.setTileSpec("autobrightness");
        this.mAutoBrightnessTile.userSwitch(KeyguardUpdateMonitor.getCurrentUser());
        QSTileView createControlCenterTileView = this.mHost.getHostInjector().createControlCenterTileView(this.mAutoBrightnessTile, true);
        this.mAutoBrightnessView = createControlCenterTileView;
        addView(createControlCenterTileView);
        this.mAutoBrightnessTile.addCallback(new QSTile.Callback() {
            public void onAnnouncementRequested(CharSequence charSequence) {
            }

            public void onScanStateChanged(boolean z) {
            }

            public void onShowDetail(boolean z) {
            }

            public void onToggleStateChanged(boolean z) {
            }

            public void onStateChanged(QSTile.State state) {
                AutoBrightnessView.this.mAutoBrightnessView.onStateChanged(state);
            }
        });
        this.mAutoBrightnessView.init(this.mAutoBrightnessTile);
        this.mAutoBrightnessTile.refreshState();
    }

    public void handleSetListening(boolean z) {
        if (this.mListening != z) {
            this.mListening = z;
            this.mAutoBrightnessTile.handleSetListening(z);
            this.mAutoBrightnessTile.refreshState();
        }
    }

    public void updateResources() {
        ((CCQSTileView) this.mAutoBrightnessView).getIcon().updateResources();
    }
}

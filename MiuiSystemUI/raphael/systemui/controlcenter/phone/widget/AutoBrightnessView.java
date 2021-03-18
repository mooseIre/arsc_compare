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
    private QSTileView mAutoBrightnessView;
    private QSTileHost mHost;
    private boolean mListening;

    public AutoBrightnessView(Context context) {
        this(context, null);
    }

    public AutoBrightnessView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void onUserSwitched(int i) {
        AutoBrightnessTile autoBrightnessTile = this.mAutoBrightnessTile;
        if (autoBrightnessTile != null) {
            autoBrightnessTile.userSwitch(i);
        }
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
            /* class com.android.systemui.controlcenter.phone.widget.AutoBrightnessView.AnonymousClass1 */

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onAnnouncementRequested(CharSequence charSequence) {
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onScanStateChanged(boolean z) {
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onShowDetail(boolean z) {
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
            public void onToggleStateChanged(boolean z) {
            }

            @Override // com.android.systemui.plugins.qs.QSTile.Callback
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

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mAutoBrightnessTile.removeCallbacks();
    }

    public void updateResources() {
        ((CCQSTileView) this.mAutoBrightnessView).getIcon().updateResources();
    }
}

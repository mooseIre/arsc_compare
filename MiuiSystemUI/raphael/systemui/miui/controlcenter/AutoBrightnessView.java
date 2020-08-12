package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.miui.controlcenter.tileImpl.CCQSTileView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.tiles.AutoBrightnessTile;

public class AutoBrightnessView extends FrameLayout {
    private AutoBrightnessTile mAutoBrightnessTile;
    /* access modifiers changed from: private */
    public QSTileView mAutoBrightnessView;
    private Context mContext;
    private QSControlTileHost mHost;
    private boolean mListening;

    public AutoBrightnessView(Context context) {
        this(context, (AttributeSet) null);
    }

    public AutoBrightnessView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
    }

    public void onUserSwitched(int i) {
        AutoBrightnessTile autoBrightnessTile = this.mAutoBrightnessTile;
        if (autoBrightnessTile != null) {
            autoBrightnessTile.userSwitch(i);
        }
    }

    public void setHost(QSControlTileHost qSControlTileHost) {
        this.mHost = qSControlTileHost;
        this.mAutoBrightnessTile = (AutoBrightnessTile) this.mHost.createTile("autobrightness");
        this.mAutoBrightnessTile.userSwitch(KeyguardUpdateMonitor.getCurrentUser());
        this.mAutoBrightnessView = this.mHost.createControlCenterTileView(this.mAutoBrightnessTile, true);
        addView(this.mAutoBrightnessView);
        this.mAutoBrightnessTile.addCallback(new QSTile.Callback() {
            public void onAnnouncementRequested(CharSequence charSequence) {
            }

            public void onScanStateChanged(boolean z) {
            }

            public void onShowDetail(boolean z) {
            }

            public void onShowEdit(boolean z) {
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

package com.android.systemui.tuner;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;
import com.android.systemui.Prefs;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.volume.ZenModePanel;

public class TunerZenModePanel extends LinearLayout implements View.OnClickListener {
    private View mButtons;
    private ZenModeController mController;
    private View mDone;
    private View.OnClickListener mDoneListener;
    private View mHeaderSwitch;
    private View mMoreSettings;
    private final Runnable mUpdate = new Runnable() {
        /* class com.android.systemui.tuner.TunerZenModePanel.AnonymousClass1 */

        public void run() {
            TunerZenModePanel.this.updatePanel();
        }
    };
    private int mZenMode;
    private ZenModePanel mZenModePanel;

    public TunerZenModePanel(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void onClick(View view) {
        if (view == this.mHeaderSwitch) {
            if (this.mZenMode == 0) {
                int i = Prefs.getInt(((LinearLayout) this).mContext, "DndFavoriteZen", 3);
                this.mZenMode = i;
                this.mController.setZen(i, null, "TunerZenModePanel");
                postUpdatePanel();
                return;
            }
            this.mZenMode = 0;
            this.mController.setZen(0, null, "TunerZenModePanel");
            postUpdatePanel();
        } else if (view == this.mMoreSettings) {
            Intent intent = new Intent("android.settings.ZEN_MODE_SETTINGS");
            intent.addFlags(268435456);
            getContext().startActivity(intent);
        } else if (view == this.mDone) {
            setVisibility(8);
            this.mDoneListener.onClick(view);
        }
    }

    private void postUpdatePanel() {
        removeCallbacks(this.mUpdate);
        postDelayed(this.mUpdate, 40);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updatePanel() {
        int i = 0;
        boolean z = this.mZenMode != 0;
        ((Checkable) this.mHeaderSwitch.findViewById(16908311)).setChecked(z);
        this.mZenModePanel.setVisibility(z ? 0 : 8);
        View view = this.mButtons;
        if (!z) {
            i = 8;
        }
        view.setVisibility(i);
    }
}

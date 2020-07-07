package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Handler;
import android.os.UserHandle;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.Icons;
import com.miui.enterprise.RestrictionsHelper;

public class NfcTile extends QSTileImpl<QSTile.BooleanState> {
    private NfcAdapter mAdapter;
    private boolean mListening;
    private BroadcastReceiver mNfcReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            NfcTile.this.refreshState();
        }
    };
    private boolean mTransientEnabling;

    public int getMetricsCategory() {
        return 800;
    }

    /* access modifiers changed from: protected */
    public void handleUserSwitch(int i) {
    }

    public NfcTile(QSHost qSHost) {
        super(qSHost);
    }

    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    public void handleSetListening(boolean z) {
        this.mListening = z;
        if (this.mListening) {
            this.mContext.registerReceiverAsUser(this.mNfcReceiver, UserHandle.ALL, new IntentFilter("android.nfc.action.ADAPTER_STATE_CHANGED"), (String) null, (Handler) null);
        } else {
            this.mContext.unregisterReceiver(this.mNfcReceiver);
        }
    }

    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.nfc");
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.NFC_SETTINGS");
    }

    /* access modifiers changed from: protected */
    public void handleClick() {
        if (!RestrictionsHelper.hasNFCRestriction(this.mContext)) {
            NfcAdapter adapter = getAdapter();
            if (adapter == null || !isNfcReady(adapter)) {
                Log.d(this.TAG, "handleClick: not ready, ignore");
                return;
            }
            String str = this.TAG;
            StringBuilder sb = new StringBuilder();
            sb.append("handleClick: from: ");
            sb.append(((QSTile.BooleanState) this.mState).value);
            sb.append(", to: ");
            sb.append(!((QSTile.BooleanState) this.mState).value);
            Log.d(str, sb.toString());
            if (((QSTile.BooleanState) this.mState).value) {
                adapter.disable();
                return;
            }
            refreshState(QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING);
            adapter.enable();
        }
    }

    /* access modifiers changed from: protected */
    public void handleSecondaryClick() {
        handleClick();
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.quick_settings_nfc_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z = false;
        this.mTransientEnabling = obj == QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
        NfcAdapter adapter = getAdapter();
        int i = 2;
        if (adapter != null) {
            boolean z2 = adapter.getAdapterState() == 2;
            if (this.mTransientEnabling || z2 || adapter.isEnabled()) {
                z = true;
            }
            booleanState.value = z;
        } else {
            booleanState.value = false;
        }
        if (!booleanState.value) {
            i = 1;
        }
        booleanState.state = i;
        booleanState.label = this.mContext.getString(R.string.quick_settings_nfc_label);
        booleanState.icon = QSTileImpl.ResourceIcon.get(Icons.getQSIcons(Integer.valueOf(booleanState.value ? R.drawable.ic_qs_nfc_enabled : R.drawable.ic_qs_nfc_disabled), this.mInControlCenter));
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        booleanState.contentDescription = booleanState.label;
    }

    /* access modifiers changed from: protected */
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(R.string.quick_settings_nfc_on);
        }
        return this.mContext.getString(R.string.quick_settings_nfc_off);
    }

    private boolean isNfcReady(NfcAdapter nfcAdapter) {
        int adapterState = nfcAdapter.getAdapterState();
        return (adapterState == 2 || adapterState == 4) ? false : true;
    }

    private NfcAdapter getAdapter() {
        if (this.mAdapter == null) {
            try {
                this.mAdapter = NfcAdapter.getNfcAdapter(this.mContext);
            } catch (UnsupportedOperationException unused) {
                this.mAdapter = null;
            }
        }
        return this.mAdapter;
    }
}

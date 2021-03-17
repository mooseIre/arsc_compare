package com.android.systemui.qs.tiles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.miui.enterprise.RestrictionsHelper;

public class NfcTile extends QSTileImpl<QSTile.BooleanState> {
    private NfcAdapter mAdapter;
    private BroadcastDispatcher mBroadcastDispatcher;
    private boolean mListening;
    private BroadcastReceiver mNfcReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.qs.tiles.NfcTile.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            NfcTile.this.refreshState();
        }
    };
    private boolean mTransientEnabling;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return 800;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleUserSwitch(int i) {
    }

    public NfcTile(QSHost qSHost, BroadcastDispatcher broadcastDispatcher) {
        super(qSHost);
        this.mBroadcastDispatcher = broadcastDispatcher;
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSetListening(boolean z) {
        super.handleSetListening(z);
        this.mListening = z;
        if (z) {
            this.mBroadcastDispatcher.registerReceiver(this.mNfcReceiver, new IntentFilter("android.nfc.action.ADAPTER_STATE_CHANGED"));
        } else {
            this.mBroadcastDispatcher.unregisterReceiver(this.mNfcReceiver);
        }
    }

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public boolean isAvailable() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.nfc");
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.NFC_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
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
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleSecondaryClick() {
        handleClick();
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.quick_settings_nfc_label);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        int i = 1;
        boolean z = false;
        this.mTransientEnabling = obj == QSTileImpl.ARG_SHOW_TRANSIENT_ENABLING;
        NfcAdapter adapter = getAdapter();
        if (adapter != null) {
            boolean z2 = adapter.getAdapterState() == 2;
            if (this.mTransientEnabling || z2 || adapter.isEnabled()) {
                z = true;
            }
            booleanState.value = z;
        } else {
            booleanState.value = false;
        }
        if (booleanState.value) {
            i = 2;
        }
        booleanState.state = i;
        booleanState.icon = QSTileImpl.ResourceIcon.get(booleanState.value ? C0013R$drawable.ic_qs_nfc_enabled : C0013R$drawable.ic_qs_nfc_disabled);
        booleanState.label = this.mContext.getString(C0021R$string.quick_settings_nfc_label);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
        booleanState.contentDescription = booleanState.label;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0021R$string.quick_settings_nfc_on);
        }
        return this.mContext.getString(C0021R$string.quick_settings_nfc_off);
    }

    private NfcAdapter getAdapter() {
        if (this.mAdapter == null) {
            try {
                this.mAdapter = NfcAdapter.getDefaultAdapter(this.mContext);
            } catch (UnsupportedOperationException unused) {
                this.mAdapter = null;
            }
        }
        return this.mAdapter;
    }

    private boolean isNfcReady(NfcAdapter nfcAdapter) {
        int adapterState = nfcAdapter.getAdapterState();
        return (adapterState == 2 || adapterState == 4) ? false : true;
    }
}

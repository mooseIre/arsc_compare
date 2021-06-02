package com.android.systemui.qs.tiles;

import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Switch;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.Prefs;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.NetworkController;

public class DataSaverTile extends QSTileImpl<QSTile.BooleanState> implements DataSaverController.Listener {
    private final DataSaverController mDataSaverController;

    @Override // com.android.systemui.plugins.qs.QSTile, com.android.systemui.qs.tileimpl.QSTileImpl
    public int getMetricsCategory() {
        return 284;
    }

    public DataSaverTile(QSHost qSHost, NetworkController networkController) {
        super(qSHost);
        DataSaverController dataSaverController = networkController.getDataSaverController();
        this.mDataSaverController = dataSaverController;
        dataSaverController.observe(getLifecycle(), this);
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public QSTile.BooleanState newTileState() {
        return new QSTile.BooleanState();
    }

    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public Intent getLongClickIntent() {
        return new Intent("android.settings.DATA_SAVER_SETTINGS");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public void handleClick() {
        if (((QSTile.BooleanState) this.mState).value || Prefs.getBoolean(this.mContext, "QsDataSaverDialogShown", false)) {
            toggleDataSaver();
            return;
        }
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        systemUIDialog.setTitle(17040023);
        systemUIDialog.setMessage(17040021);
        systemUIDialog.setPositiveButton(17040022, new DialogInterface.OnClickListener() {
            /* class com.android.systemui.qs.tiles.$$Lambda$DataSaverTile$7vpE4nfIgph7ByTloh1_igU2EhI */

            public final void onClick(DialogInterface dialogInterface, int i) {
                DataSaverTile.this.lambda$handleClick$0$DataSaverTile(dialogInterface, i);
            }
        });
        systemUIDialog.setNegativeButton(17039360, null);
        systemUIDialog.setShowForAllUsers(true);
        systemUIDialog.show();
        Prefs.putBoolean(this.mContext, "QsDataSaverDialogShown", true);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleClick$0 */
    public /* synthetic */ void lambda$handleClick$0$DataSaverTile(DialogInterface dialogInterface, int i) {
        toggleDataSaver();
    }

    private void toggleDataSaver() {
        ((QSTile.BooleanState) this.mState).value = !this.mDataSaverController.isDataSaverEnabled();
        this.mDataSaverController.setDataSaverEnabled(((QSTile.BooleanState) this.mState).value);
        refreshState(Boolean.valueOf(((QSTile.BooleanState) this.mState).value));
    }

    @Override // com.android.systemui.plugins.qs.QSTile
    public CharSequence getTileLabel() {
        return this.mContext.getString(C0021R$string.data_saver);
    }

    /* access modifiers changed from: protected */
    public void handleUpdateState(QSTile.BooleanState booleanState, Object obj) {
        boolean z;
        int i;
        if (obj instanceof Boolean) {
            z = ((Boolean) obj).booleanValue();
        } else {
            z = this.mDataSaverController.isDataSaverEnabled();
        }
        booleanState.value = z;
        booleanState.state = z ? 2 : 1;
        String string = this.mContext.getString(C0021R$string.data_saver);
        booleanState.label = string;
        booleanState.contentDescription = string;
        if (booleanState.value) {
            i = C0013R$drawable.ic_data_saver;
        } else {
            i = C0013R$drawable.ic_data_saver_off;
        }
        booleanState.icon = QSTileImpl.ResourceIcon.get(i);
        booleanState.expandedAccessibilityClassName = Switch.class.getName();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.qs.tileimpl.QSTileImpl
    public String composeChangeAnnouncement() {
        if (((QSTile.BooleanState) this.mState).value) {
            return this.mContext.getString(C0021R$string.accessibility_quick_settings_data_saver_changed_on);
        }
        return this.mContext.getString(C0021R$string.accessibility_quick_settings_data_saver_changed_off);
    }

    @Override // com.android.systemui.statusbar.policy.DataSaverController.Listener
    public void onDataSaverChanged(boolean z) {
        refreshState(Boolean.valueOf(z));
    }
}

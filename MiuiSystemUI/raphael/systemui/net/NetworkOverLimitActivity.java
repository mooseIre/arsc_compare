package com.android.systemui.net;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.INetworkPolicyManager;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.android.systemui.C0021R$string;

public class NetworkOverLimitActivity extends Activity {
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        final NetworkTemplate parcelableExtra = getIntent().getParcelableExtra("android.net.NETWORK_TEMPLATE");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getLimitedDialogTitleForTemplate(parcelableExtra));
        builder.setMessage(C0021R$string.data_usage_disabled_dialog);
        builder.setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
        builder.setNegativeButton(C0021R$string.data_usage_disabled_dialog_enable, new DialogInterface.OnClickListener() {
            /* class com.android.systemui.net.NetworkOverLimitActivity.AnonymousClass1 */

            public void onClick(DialogInterface dialogInterface, int i) {
                NetworkOverLimitActivity.this.snoozePolicy(parcelableExtra);
            }
        });
        AlertDialog create = builder.create();
        create.getWindow().setType(2003);
        create.setOnDismissListener(new DialogInterface.OnDismissListener() {
            /* class com.android.systemui.net.NetworkOverLimitActivity.AnonymousClass2 */

            public void onDismiss(DialogInterface dialogInterface) {
                NetworkOverLimitActivity.this.finish();
            }
        });
        create.show();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void snoozePolicy(NetworkTemplate networkTemplate) {
        try {
            INetworkPolicyManager.Stub.asInterface(ServiceManager.getService("netpolicy")).snoozeLimit(networkTemplate);
        } catch (RemoteException e) {
            Log.w("NetworkOverLimitActivity", "problem snoozing network policy", e);
        }
    }

    private static int getLimitedDialogTitleForTemplate(NetworkTemplate networkTemplate) {
        if (networkTemplate.getMatchRule() != 1) {
            return C0021R$string.data_usage_disabled_dialog_title;
        }
        return C0021R$string.data_usage_disabled_dialog_mobile_title;
    }
}

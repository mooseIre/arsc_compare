package com.android.systemui.miui.policy;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import miui.app.AlertDialog;

public class SimpleAlertDialogActivity extends Activity {
    private AlertDialog mDialog;
    private CharSequence mMessage;
    private CharSequence mTitle;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        setupShowWhenLocked();
        super.onCreate(bundle);
        this.mTitle = getIntent().getCharSequenceExtra("title");
        if (TextUtils.isEmpty(this.mTitle)) {
            this.mTitle = getTitle();
        }
        this.mMessage = getIntent().getCharSequenceExtra("message");
        this.mDialog = createDialogBuilder().create();
        this.mDialog.show();
    }

    private void setupShowWhenLocked() {
        if ((getWindow().getAttributes().flags & 524288) != 0) {
            KeyguardManager keyguardManager = (KeyguardManager) getSystemService("keyguard");
            if (keyguardManager.isKeyguardSecure() && keyguardManager.isKeyguardLocked()) {
                getWindow().setDimAmount(1.0f);
            }
        }
    }

    /* access modifiers changed from: protected */
    public AlertDialog.Builder createDialogBuilder() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(this.mTitle);
        builder.setMessage(this.mMessage);
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                SimpleAlertDialogActivity.this.finish();
            }
        });
        builder.setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
        return builder;
    }

    /* access modifiers changed from: protected */
    public void dismissDialog() {
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null && alertDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    public void finish() {
        overridePendingTransition(0, 0);
        super.finish();
    }
}

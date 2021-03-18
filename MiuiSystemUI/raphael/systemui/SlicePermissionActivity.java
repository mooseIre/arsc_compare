package com.android.systemui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.slice.SliceManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.BidiFormatter;
import android.util.EventLog;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;

public class SlicePermissionActivity extends Activity implements DialogInterface.OnClickListener, DialogInterface.OnDismissListener {
    private CheckBox mAllCheckbox;
    private String mCallingPkg;
    private String mProviderPkg;
    private Uri mUri;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mUri = (Uri) getIntent().getParcelableExtra("slice_uri");
        this.mCallingPkg = getIntent().getStringExtra("pkg");
        if (this.mUri == null) {
            Log.e("SlicePermissionActivity", "slice_uri wasn't provided");
            finish();
            return;
        }
        try {
            PackageManager packageManager = getPackageManager();
            this.mProviderPkg = packageManager.resolveContentProvider(this.mUri.getAuthority(), 128).applicationInfo.packageName;
            verifyCallingPkg();
            String unicodeWrap = BidiFormatter.getInstance().unicodeWrap(packageManager.getApplicationInfo(this.mCallingPkg, 0).loadSafeLabel(packageManager, 500.0f, 5).toString());
            String unicodeWrap2 = BidiFormatter.getInstance().unicodeWrap(packageManager.getApplicationInfo(this.mProviderPkg, 0).loadSafeLabel(packageManager, 500.0f, 5).toString());
            AlertDialog create = new AlertDialog.Builder(this).setTitle(getString(C0021R$string.slice_permission_title, new Object[]{unicodeWrap, unicodeWrap2})).setView(C0017R$layout.slice_permission_request).setNegativeButton(C0021R$string.slice_permission_deny, this).setPositiveButton(C0021R$string.slice_permission_allow, this).setOnDismissListener(this).create();
            create.getWindow().addSystemFlags(524288);
            create.show();
            ((TextView) create.getWindow().getDecorView().findViewById(C0015R$id.text1)).setText(getString(C0021R$string.slice_permission_text_1, new Object[]{unicodeWrap2}));
            ((TextView) create.getWindow().getDecorView().findViewById(C0015R$id.text2)).setText(getString(C0021R$string.slice_permission_text_2, new Object[]{unicodeWrap2}));
            CheckBox checkBox = (CheckBox) create.getWindow().getDecorView().findViewById(C0015R$id.slice_permission_checkbox);
            this.mAllCheckbox = checkBox;
            checkBox.setText(getString(C0021R$string.slice_permission_checkbox, new Object[]{unicodeWrap}));
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("SlicePermissionActivity", "Couldn't find package", e);
            finish();
        }
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -1) {
            ((SliceManager) getSystemService(SliceManager.class)).grantPermissionFromUser(this.mUri, this.mCallingPkg, this.mAllCheckbox.isChecked());
        }
        finish();
    }

    public void onDismiss(DialogInterface dialogInterface) {
        finish();
    }

    private void verifyCallingPkg() {
        String stringExtra = getIntent().getStringExtra("provider_pkg");
        if (stringExtra != null && !this.mProviderPkg.equals(stringExtra)) {
            EventLog.writeEvent(1397638484, "159145361", Integer.valueOf(getUid(getCallingPkg())));
        }
    }

    private String getCallingPkg() {
        Uri referrer = getReferrer();
        if (referrer == null) {
            return null;
        }
        return referrer.getHost();
    }

    private int getUid(String str) {
        if (str == null) {
            return -1;
        }
        try {
            return getPackageManager().getApplicationInfo(str, 0).uid;
        } catch (PackageManager.NameNotFoundException unused) {
            return -1;
        }
    }
}

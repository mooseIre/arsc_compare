package com.android.systemui.controlcenter.phone.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0022R$style;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.SecurityController;
import com.miui.systemui.analytics.SystemUIStat;

public class QSControlFooter extends LinearLayout implements View.OnClickListener, DialogInterface.OnClickListener {
    private ActivityStarter mActivityStarter;
    private final Callback mCallback;
    private final Context mContext;
    private AlertDialog mDialog;
    private ImageView mFooterIcon;
    private int mFooterIconId;
    private TextView mFooterText;
    private CharSequence mFooterTextContent;
    private boolean mForceHide;
    protected H mHandler;
    private QSTileHost mHost;
    private boolean mIsVisible;
    private Handler mMainHandler;
    private SecurityController mSecurityController;
    private final Runnable mUpdateDisplayState;
    private final Runnable mUpdateIcon;

    static {
        Log.isLoggable("QSControlFooter", 3);
    }

    public QSControlFooter(Context context) {
        this(context, null, 0);
    }

    public QSControlFooter(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public QSControlFooter(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mCallback = new Callback();
        this.mFooterTextContent = null;
        this.mUpdateIcon = new Runnable() {
            /* class com.android.systemui.controlcenter.phone.widget.QSControlFooter.AnonymousClass1 */

            public void run() {
                QSControlFooter.this.mFooterIcon.setImageResource(QSControlFooter.this.mFooterIconId);
            }
        };
        this.mUpdateDisplayState = new Runnable() {
            /* class com.android.systemui.controlcenter.phone.widget.QSControlFooter.AnonymousClass2 */

            public void run() {
                if (QSControlFooter.this.mFooterTextContent != null) {
                    QSControlFooter.this.mFooterText.setText(QSControlFooter.this.mFooterTextContent);
                }
                QSControlFooter qSControlFooter = QSControlFooter.this;
                qSControlFooter.setVisibility((qSControlFooter.mForceHide || !QSControlFooter.this.mIsVisible) ? 8 : 0);
            }
        };
        this.mContext = context;
        init();
    }

    public View getFooterText() {
        return this.mFooterText;
    }

    public View getFooterIcon() {
        return this.mFooterIcon;
    }

    public void init() {
        setOnClickListener(this);
        this.mFooterIconId = C0013R$drawable.ic_info_outline;
        this.mMainHandler = new Handler(Looper.getMainLooper());
        this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        this.mSecurityController = (SecurityController) Dependency.get(SecurityController.class);
        this.mHandler = new H((Looper) Dependency.get(Dependency.BG_LOOPER));
    }

    public void updateResources() {
        this.mFooterText.setTextAppearance(C0022R$style.TextAppearance_QSControl_FooterText);
        this.mFooterIcon.getDrawable().setTint(this.mContext.getColor(C0011R$color.qs_control_footer_icon_color));
    }

    public void setHostEnvironment(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
    }

    public void setListening(boolean z) {
        if (z) {
            this.mSecurityController.addCallback(this.mCallback);
            refreshState();
            return;
        }
        this.mSecurityController.removeCallback(this.mCallback);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mFooterText = (TextView) findViewById(C0015R$id.footer_text);
        this.mFooterIcon = (ImageView) findViewById(C0015R$id.footer_icon);
    }

    public void onClick(View view) {
        this.mHandler.sendEmptyMessage(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleClick() {
        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleClickShortcutEvent("security_footer");
        showDeviceMonitoringDialog();
    }

    public void showDeviceMonitoringDialog() {
        this.mHost.collapsePanels();
        createDialog();
    }

    public void refreshState() {
        this.mHandler.sendEmptyMessage(1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRefreshState() {
        int i;
        boolean isDeviceManaged = this.mSecurityController.isDeviceManaged();
        boolean hasWorkProfile = this.mSecurityController.hasWorkProfile();
        boolean hasCACertInCurrentUser = this.mSecurityController.hasCACertInCurrentUser();
        boolean hasCACertInWorkProfile = this.mSecurityController.hasCACertInWorkProfile();
        boolean isNetworkLoggingEnabled = this.mSecurityController.isNetworkLoggingEnabled();
        String primaryVpnName = this.mSecurityController.getPrimaryVpnName();
        String workProfileVpnName = this.mSecurityController.getWorkProfileVpnName();
        CharSequence deviceOwnerOrganizationName = this.mSecurityController.getDeviceOwnerOrganizationName();
        CharSequence workProfileOrganizationName = this.mSecurityController.getWorkProfileOrganizationName();
        this.mIsVisible = (isDeviceManaged || hasCACertInCurrentUser || hasCACertInWorkProfile) && !this.mSecurityController.isSilentVpnPackage();
        this.mFooterTextContent = getFooterText(isDeviceManaged, hasWorkProfile, hasCACertInCurrentUser, hasCACertInWorkProfile, isNetworkLoggingEnabled, primaryVpnName, workProfileVpnName, deviceOwnerOrganizationName, workProfileOrganizationName);
        if (hasCACertInCurrentUser || hasCACertInWorkProfile || isNetworkLoggingEnabled) {
            i = C0013R$drawable.ic_qs_footer_managed;
        } else if (primaryVpnName == null && workProfileVpnName == null) {
            i = C0013R$drawable.ic_info_outline;
        } else {
            i = C0013R$drawable.ic_qs_vpn;
        }
        if (this.mFooterIconId != i) {
            this.mFooterIconId = i;
            this.mMainHandler.post(this.mUpdateIcon);
        }
        this.mMainHandler.post(this.mUpdateDisplayState);
    }

    /* access modifiers changed from: protected */
    public CharSequence getFooterText(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, String str, String str2, CharSequence charSequence, CharSequence charSequence2) {
        if (z) {
            if (z3 || z4 || z5) {
                if (charSequence == null) {
                    return this.mContext.getString(C0021R$string.quick_settings_disclosure_management_monitoring);
                }
                return this.mContext.getString(C0021R$string.quick_settings_disclosure_named_management_monitoring, charSequence);
            } else if (str == null || str2 == null) {
                if (str == null && str2 == null) {
                    if (charSequence == null) {
                        return this.mContext.getString(C0021R$string.quick_settings_disclosure_management);
                    }
                    return this.mContext.getString(C0021R$string.quick_settings_disclosure_named_management, charSequence);
                } else if (charSequence == null) {
                    Context context = this.mContext;
                    int i = C0021R$string.quick_settings_disclosure_management_named_vpn;
                    Object[] objArr = new Object[1];
                    if (str == null) {
                        str = str2;
                    }
                    objArr[0] = str;
                    return context.getString(i, objArr);
                } else {
                    Context context2 = this.mContext;
                    int i2 = C0021R$string.quick_settings_disclosure_named_management_named_vpn;
                    Object[] objArr2 = new Object[2];
                    objArr2[0] = charSequence;
                    if (str == null) {
                        str = str2;
                    }
                    objArr2[1] = str;
                    return context2.getString(i2, objArr2);
                }
            } else if (charSequence == null) {
                return this.mContext.getString(C0021R$string.quick_settings_disclosure_management_vpns);
            } else {
                return this.mContext.getString(C0021R$string.quick_settings_disclosure_named_management_vpns, charSequence);
            }
        } else if (z4) {
            if (charSequence2 == null) {
                return this.mContext.getString(C0021R$string.quick_settings_disclosure_managed_profile_monitoring);
            }
            return this.mContext.getString(C0021R$string.quick_settings_disclosure_named_managed_profile_monitoring, charSequence2);
        } else if (z3) {
            return this.mContext.getString(C0021R$string.quick_settings_disclosure_monitoring);
        } else {
            if (str != null && str2 != null) {
                return this.mContext.getString(C0021R$string.quick_settings_disclosure_vpns);
            }
            if (str2 != null) {
                return this.mContext.getString(C0021R$string.quick_settings_disclosure_managed_profile_named_vpn, str2);
            } else if (str == null) {
                return null;
            } else {
                if (z2) {
                    return this.mContext.getString(C0021R$string.quick_settings_disclosure_personal_profile_named_vpn, str);
                }
                return this.mContext.getString(C0021R$string.quick_settings_disclosure_named_vpn, str);
            }
        }
    }

    public void onClick(DialogInterface dialogInterface, int i) {
        if (i == -2) {
            Intent intent = new Intent("android.settings.ENTERPRISE_PRIVACY_SETTINGS");
            this.mDialog.dismiss();
            this.mActivityStarter.postStartActivityDismissingKeyguard(intent, 0);
        }
    }

    private void createDialog() {
        boolean isDeviceManaged = this.mSecurityController.isDeviceManaged();
        boolean hasWorkProfile = this.mSecurityController.hasWorkProfile();
        CharSequence deviceOwnerOrganizationName = this.mSecurityController.getDeviceOwnerOrganizationName();
        boolean hasCACertInCurrentUser = this.mSecurityController.hasCACertInCurrentUser();
        boolean hasCACertInWorkProfile = this.mSecurityController.hasCACertInWorkProfile();
        boolean isNetworkLoggingEnabled = this.mSecurityController.isNetworkLoggingEnabled();
        String primaryVpnName = this.mSecurityController.getPrimaryVpnName();
        String workProfileVpnName = this.mSecurityController.getWorkProfileVpnName();
        SystemUIDialog systemUIDialog = new SystemUIDialog(this.mContext);
        this.mDialog = systemUIDialog;
        systemUIDialog.requestWindowFeature(1);
        View inflate = LayoutInflater.from(this.mContext).inflate(C0017R$layout.quick_settings_footer_dialog, (ViewGroup) null, false);
        this.mDialog.setView(inflate);
        this.mDialog.setButton(-1, getPositiveButton(), this);
        CharSequence managementMessage = getManagementMessage(isDeviceManaged, deviceOwnerOrganizationName);
        if (managementMessage == null) {
            inflate.findViewById(C0015R$id.device_management_disclosures).setVisibility(8);
        } else {
            inflate.findViewById(C0015R$id.device_management_disclosures).setVisibility(0);
            ((TextView) inflate.findViewById(C0015R$id.device_management_warning)).setText(managementMessage);
            this.mDialog.setButton(-2, getSettingsButton(), this);
        }
        CharSequence caCertsMessage = getCaCertsMessage(isDeviceManaged, hasCACertInCurrentUser, hasCACertInWorkProfile);
        if (caCertsMessage == null) {
            inflate.findViewById(C0015R$id.ca_certs_disclosures).setVisibility(8);
        } else {
            inflate.findViewById(C0015R$id.ca_certs_disclosures).setVisibility(0);
            TextView textView = (TextView) inflate.findViewById(C0015R$id.ca_certs_warning);
            textView.setText(caCertsMessage);
            textView.setMovementMethod(new LinkMovementMethod());
        }
        CharSequence networkLoggingMessage = getNetworkLoggingMessage(isNetworkLoggingEnabled);
        if (networkLoggingMessage == null) {
            inflate.findViewById(C0015R$id.network_logging_disclosures).setVisibility(8);
        } else {
            inflate.findViewById(C0015R$id.network_logging_disclosures).setVisibility(0);
            ((TextView) inflate.findViewById(C0015R$id.network_logging_warning)).setText(networkLoggingMessage);
        }
        CharSequence vpnMessage = getVpnMessage(isDeviceManaged, hasWorkProfile, primaryVpnName, workProfileVpnName);
        if (vpnMessage == null) {
            inflate.findViewById(C0015R$id.vpn_disclosures).setVisibility(8);
        } else {
            inflate.findViewById(C0015R$id.vpn_disclosures).setVisibility(0);
            TextView textView2 = (TextView) inflate.findViewById(C0015R$id.vpn_warning);
            textView2.setText(vpnMessage);
            textView2.setMovementMethod(new LinkMovementMethod());
        }
        this.mDialog.show();
        this.mDialog.getWindow().setLayout(-1, -2);
    }

    private String getSettingsButton() {
        return this.mContext.getString(C0021R$string.monitoring_button_view_policies);
    }

    private String getPositiveButton() {
        return this.mContext.getString(C0021R$string.quick_settings_done);
    }

    /* access modifiers changed from: protected */
    public CharSequence getManagementMessage(boolean z, CharSequence charSequence) {
        if (!z) {
            return null;
        }
        if (charSequence == null) {
            return this.mContext.getString(C0021R$string.monitoring_description_management);
        }
        return this.mContext.getString(C0021R$string.monitoring_description_named_management, charSequence);
    }

    /* access modifiers changed from: protected */
    public CharSequence getCaCertsMessage(boolean z, boolean z2, boolean z3) {
        if (!z2 && !z3) {
            return null;
        }
        if (z) {
            return this.mContext.getString(C0021R$string.monitoring_description_management_ca_certificate);
        }
        if (z3) {
            return this.mContext.getString(C0021R$string.monitoring_description_managed_profile_ca_certificate);
        }
        return this.mContext.getString(C0021R$string.monitoring_description_ca_certificate);
    }

    /* access modifiers changed from: protected */
    public CharSequence getNetworkLoggingMessage(boolean z) {
        if (!z) {
            return null;
        }
        return this.mContext.getString(C0021R$string.monitoring_description_management_network_logging);
    }

    /* access modifiers changed from: protected */
    public CharSequence getVpnMessage(boolean z, boolean z2, String str, String str2) {
        if (str == null && str2 == null) {
            return null;
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        if (z) {
            if (str == null || str2 == null) {
                Context context = this.mContext;
                int i = C0021R$string.monitoring_description_named_vpn;
                Object[] objArr = new Object[1];
                if (str == null) {
                    str = str2;
                }
                objArr[0] = str;
                spannableStringBuilder.append((CharSequence) context.getString(i, objArr));
            } else {
                spannableStringBuilder.append((CharSequence) this.mContext.getString(C0021R$string.monitoring_description_two_named_vpns, str, str2));
            }
        } else if (str != null && str2 != null) {
            spannableStringBuilder.append((CharSequence) this.mContext.getString(C0021R$string.monitoring_description_two_named_vpns, str, str2));
        } else if (str2 != null) {
            spannableStringBuilder.append((CharSequence) this.mContext.getString(C0021R$string.monitoring_description_managed_profile_named_vpn, str2));
        } else if (z2) {
            spannableStringBuilder.append((CharSequence) this.mContext.getString(C0021R$string.monitoring_description_personal_profile_named_vpn, str));
        } else {
            spannableStringBuilder.append((CharSequence) this.mContext.getString(C0021R$string.monitoring_description_named_vpn, str));
        }
        spannableStringBuilder.append((CharSequence) this.mContext.getString(C0021R$string.monitoring_description_vpn_settings_separator));
        spannableStringBuilder.append(this.mContext.getString(C0021R$string.monitoring_description_vpn_settings), new VpnSpan(), 0);
        return spannableStringBuilder;
    }

    /* access modifiers changed from: private */
    public class Callback implements SecurityController.SecurityControllerCallback {
        private Callback() {
        }

        @Override // com.android.systemui.statusbar.policy.SecurityController.SecurityControllerCallback
        public void onStateChanged() {
            QSControlFooter.this.refreshState();
        }
    }

    /* access modifiers changed from: private */
    public class H extends Handler {
        private H(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            try {
                if (message.what == 1) {
                    QSControlFooter.this.handleRefreshState();
                } else if (message.what == 0) {
                    QSControlFooter.this.handleClick();
                }
            } catch (Exception e) {
                String str = "Error in " + ((String) null);
                Log.w("QSControlFooter", str, e);
                QSControlFooter.this.mHost.warn(str, e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public class VpnSpan extends ClickableSpan {
        public int hashCode() {
            return 314159257;
        }

        protected VpnSpan() {
        }

        public void onClick(View view) {
            Intent intent = new Intent("android.settings.VPN_SETTINGS");
            QSControlFooter.this.mDialog.dismiss();
            QSControlFooter.this.mActivityStarter.postStartActivityDismissingKeyguard(intent, 0);
        }

        public boolean equals(Object obj) {
            return obj instanceof VpnSpan;
        }
    }
}

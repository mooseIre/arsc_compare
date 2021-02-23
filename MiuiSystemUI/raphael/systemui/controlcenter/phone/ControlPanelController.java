package com.android.systemui.controlcenter.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0020R$raw;
import com.android.systemui.C0021R$string;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controlcenter.ControlCenter;
import com.android.systemui.controlcenter.phone.widget.CornerVideoView;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.CallbackController;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.statusbar.phone.MiuiSystemUIDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import miui.app.AlertDialog;

public class ControlPanelController implements CallbackController<UseControlPanelChangeListener>, SettingsObserver.Callback {
    private BroadcastDispatcher mBroadcastDispatcher;
    private Context mContext;
    /* access modifiers changed from: private */
    public ControlCenter mControlCenter;
    private CurrentUserTracker mCurrentUserTracker;
    private AlertDialog mDialog;
    private boolean mExpandableInKeyguard;
    private Handler mHandler = new H();
    private boolean mIsNCSwitching;
    private KeyguardViewMediator mKeyguardViewMediator;
    private final List<UseControlPanelChangeListener> mListeners;
    private boolean mNcSwitchGuideShown;
    private BroadcastReceiver mRemoteOperationReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String stringExtra = intent.getStringExtra("operation");
            if (!"action_panels_operation".equals(action)) {
                return;
            }
            if (!"reverse_notifications_panel".equals(stringExtra) || ControlPanelController.this.mStatusBar == null) {
                if (!"reverse_quick_settings_panel".equals(stringExtra)) {
                    return;
                }
                if (ControlPanelController.this.mUseControlPanel) {
                    if (ControlPanelController.this.isCCFullyCollapsed()) {
                        ControlPanelController.this.openPanel();
                    } else {
                        ControlPanelController.this.collapseControlCenter(true);
                    }
                } else if (ControlPanelController.this.mStatusBar == null) {
                } else {
                    if (ControlPanelController.this.mStatusBar.isQSFullyCollapsed()) {
                        ControlPanelController.this.mStatusBar.postAnimateOpenPanels();
                    } else {
                        ControlPanelController.this.mStatusBar.postAnimateCollapsePanels();
                    }
                }
            } else if (ControlPanelController.this.mStatusBar.isQSFullyCollapsed()) {
                ControlPanelController.this.mStatusBar.postAnimateOpenPanels();
            } else {
                ControlPanelController.this.mStatusBar.postAnimateCollapsePanels();
            }
        }
    };
    protected BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                ControlPanelController.this.collapseControlCenter(true);
            }
        }
    };
    /* access modifiers changed from: private */
    public SettingsObserver mSettingsObserver;
    /* access modifiers changed from: private */
    public StatusBar mStatusBar;
    private boolean mSuperPowerModeOn;
    /* access modifiers changed from: private */
    public boolean mUseControlPanel;
    /* access modifiers changed from: private */
    public int mUseControlPanelSettingDefault;

    public interface UseControlPanelChangeListener {
        void onUseControlPanelChange(boolean z);
    }

    public ControlPanelController(Context context, KeyguardViewMediator keyguardViewMediator, BroadcastDispatcher broadcastDispatcher, SettingsObserver settingsObserver) {
        this.mContext = context;
        this.mListeners = new ArrayList();
        this.mUseControlPanelSettingDefault = context.getResources().getInteger(C0016R$integer.use_control_panel_setting_default);
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mSettingsObserver = settingsObserver;
        this.mNcSwitchGuideShown = Settings.System.getIntForUser(this.mContext.getContentResolver(), "nc_switch_guide_shown", 0, 0) != 0;
        this.mCurrentUserTracker = new CurrentUserTracker(this.mBroadcastDispatcher) {
            public void onUserSwitched(int i) {
                ControlPanelController controlPanelController = ControlPanelController.this;
                controlPanelController.onContentChanged("use_control_panel", controlPanelController.mSettingsObserver.getValue("use_control_panel", 0, String.valueOf(ControlPanelController.this.mUseControlPanelSettingDefault)));
                ControlPanelController controlPanelController2 = ControlPanelController.this;
                controlPanelController2.onContentChanged("expandable_under_lock_screen", controlPanelController2.mSettingsObserver.getValue("expandable_under_lock_screen", 0, "1"));
                if (ControlPanelController.this.mUseControlPanel && ControlPanelController.this.mControlCenter != null) {
                    ControlPanelController.this.mControlCenter.onUserSwitched(i);
                }
            }
        };
    }

    public void setControlCenter(ControlCenter controlCenter) {
        this.mControlCenter = controlCenter;
    }

    public void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
    }

    public void collapsePanel(boolean z) {
        if (this.mControlCenter != null && !isCCFullyCollapsed()) {
            this.mControlCenter.collapse(z);
        }
    }

    public void collapseControlCenter(boolean z) {
        if (this.mControlCenter != null && !isCCFullyCollapsed()) {
            collapseControlCenter(z, false);
        }
    }

    public void collapseControlCenter(boolean z, boolean z2) {
        if (this.mControlCenter != null && !isCCFullyCollapsed()) {
            if (!z2) {
                this.mControlCenter.collapseControlCenter(z);
            } else {
                this.mControlCenter.handleCollapsePanel(z, true);
            }
        }
    }

    public boolean isUseControlCenter() {
        return this.mUseControlPanel;
    }

    public boolean isExpandable() {
        return !this.mKeyguardViewMediator.isShowing() || this.mExpandableInKeyguard;
    }

    public boolean isCCFullyCollapsed() {
        ControlCenter controlCenter = this.mControlCenter;
        if (controlCenter != null) {
            return controlCenter.isCollapsed();
        }
        return true;
    }

    public void openPanel() {
        ControlCenter controlCenter = this.mControlCenter;
        if (controlCenter != null) {
            controlCenter.openPanel();
        }
    }

    public void openPanelImmediately() {
        ControlCenter controlCenter = this.mControlCenter;
        if (controlCenter != null) {
            controlCenter.openPanelImmediately();
        }
    }

    private void register() {
        this.mCurrentUserTracker.startTracking();
        this.mSettingsObserver.addCallbackForSingleUser(this, 0, "use_control_panel");
        this.mSettingsObserver.addCallback(this, "expandable_under_lock_screen");
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action_panels_operation");
        this.mBroadcastDispatcher.registerReceiver(this.mRemoteOperationReceiver, intentFilter, (Executor) null, UserHandle.ALL);
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        intentFilter2.addAction("android.intent.action.SCREEN_OFF");
        this.mBroadcastDispatcher.registerReceiver(this.mScreenOffReceiver, intentFilter2, (Executor) null, UserHandle.ALL);
    }

    private void unRegister() {
        this.mCurrentUserTracker.stopTracking();
        this.mSettingsObserver.removeCallback(this);
        this.mBroadcastDispatcher.unregisterReceiver(this.mRemoteOperationReceiver);
        this.mBroadcastDispatcher.unregisterReceiver(this.mScreenOffReceiver);
    }

    /* access modifiers changed from: private */
    public void notifyAllListeners() {
        for (UseControlPanelChangeListener onUseControlPanelChange : this.mListeners) {
            onUseControlPanelChange.onUseControlPanelChange(this.mUseControlPanel);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x002a  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0056  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onContentChanged(@org.jetbrains.annotations.Nullable java.lang.String r5, @org.jetbrains.annotations.Nullable java.lang.String r6) {
        /*
            r4 = this;
            int r0 = r5.hashCode()
            r1 = -1630983538(0xffffffff9ec92a8e, float:-2.1299303E-20)
            r2 = 0
            r3 = 1
            if (r0 == r1) goto L_0x001b
            r1 = -1074300950(0xffffffffbff777ea, float:-1.933347)
            if (r0 == r1) goto L_0x0011
            goto L_0x0025
        L_0x0011:
            java.lang.String r0 = "use_control_panel"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x0025
            r5 = r2
            goto L_0x0026
        L_0x001b:
            java.lang.String r0 = "expandable_under_lock_screen"
            boolean r5 = r5.equals(r0)
            if (r5 == 0) goto L_0x0025
            r5 = r3
            goto L_0x0026
        L_0x0025:
            r5 = -1
        L_0x0026:
            java.lang.String r0 = "ControlPanelController"
            if (r5 == 0) goto L_0x0056
            if (r5 == r3) goto L_0x002d
            goto L_0x007a
        L_0x002d:
            int r5 = com.miui.systemui.util.MiuiTextUtils.parseInt(r6, r3)
            if (r5 == 0) goto L_0x0034
            r2 = r3
        L_0x0034:
            r4.mExpandableInKeyguard = r2
            boolean r5 = r4.isExpandable()
            if (r5 != 0) goto L_0x003f
            r4.collapsePanel(r3)
        L_0x003f:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "onChange: mExpandableInKeyguard = "
            r5.append(r6)
            boolean r4 = r4.mExpandableInKeyguard
            r5.append(r4)
            java.lang.String r4 = r5.toString()
            android.util.Log.d(r0, r4)
            goto L_0x007a
        L_0x0056:
            int r5 = r4.mUseControlPanelSettingDefault
            int r5 = com.miui.systemui.util.MiuiTextUtils.parseInt(r6, r5)
            if (r5 == 0) goto L_0x005f
            r2 = r3
        L_0x005f:
            r4.mUseControlPanel = r2
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r6 = "onChange: mUseControlPanel = "
            r5.append(r6)
            boolean r6 = r4.mUseControlPanel
            r5.append(r6)
            java.lang.String r5 = r5.toString()
            android.util.Log.d(r0, r5)
            r4.notifyAllListeners()
        L_0x007a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.controlcenter.phone.ControlPanelController.onContentChanged(java.lang.String, java.lang.String):void");
    }

    /* access modifiers changed from: private */
    public void addCallbackLocked(UseControlPanelChangeListener useControlPanelChangeListener) {
        if (this.mListeners.contains(useControlPanelChangeListener)) {
            useControlPanelChangeListener.onUseControlPanelChange(this.mUseControlPanel);
            return;
        }
        this.mListeners.add(useControlPanelChangeListener);
        if (this.mListeners.size() == 1) {
            register();
        } else {
            useControlPanelChangeListener.onUseControlPanelChange(this.mUseControlPanel);
        }
    }

    /* access modifiers changed from: private */
    public void removeCallbackLocked(UseControlPanelChangeListener useControlPanelChangeListener) {
        this.mListeners.remove(useControlPanelChangeListener);
        if (this.mListeners.size() == 0) {
            unRegister();
        }
    }

    public boolean useControlPanel() {
        return Settings.System.getIntForUser(this.mContext.getContentResolver(), "use_control_panel", this.mUseControlPanelSettingDefault, 0) != 0;
    }

    public void addCallback(UseControlPanelChangeListener useControlPanelChangeListener) {
        if (useControlPanelChangeListener != null) {
            this.mHandler.obtainMessage(1, useControlPanelChangeListener).sendToTarget();
        }
    }

    public void removeCallback(UseControlPanelChangeListener useControlPanelChangeListener) {
        if (useControlPanelChangeListener != null) {
            this.mHandler.obtainMessage(2, useControlPanelChangeListener).sendToTarget();
        }
    }

    public void setSuperPowerMode(boolean z) {
        this.mSuperPowerModeOn = z;
    }

    public boolean isSuperPowerMode() {
        return this.mSuperPowerModeOn;
    }

    public void requestNCSwitching(boolean z) {
        this.mIsNCSwitching = z;
    }

    public boolean isNCSwitching() {
        return this.mIsNCSwitching;
    }

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                ControlPanelController.this.addCallbackLocked((UseControlPanelChangeListener) message.obj);
            } else if (i == 2) {
                ControlPanelController.this.removeCallbackLocked((UseControlPanelChangeListener) message.obj);
            } else if (i == 3) {
                ControlPanelController.this.notifyAllListeners();
            }
        }
    }

    public void refreshAllTiles() {
        ControlCenter controlCenter = this.mControlCenter;
        if (controlCenter != null) {
            controlCenter.refreshAllTiles();
        }
    }

    public void showDialog(boolean z) {
        if (this.mDialog != null || !z) {
            dismissDialog(false);
        } else {
            showDialog();
        }
    }

    private void showDialog() {
        if (!this.mNcSwitchGuideShown && isUseControlCenter()) {
            FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(this.mContext).inflate(C0017R$layout.nc_switch_guide_dialog_content, (ViewGroup) null);
            ((CornerVideoView) frameLayout.findViewById(C0015R$id.guide_video)).play(C0020R$raw.nc_switch_guide_video, 0);
            AlertDialog create = new AlertDialog.Builder(this.mContext, 8).setTitle(C0021R$string.control_center_notification_switch_guide).setPositiveButton(C0021R$string.bubbles_user_education_got_it, (DialogInterface.OnClickListener) null).setView(frameLayout).setCancelable(false).create();
            this.mDialog = create;
            MiuiSystemUIDialog.applyFlags(create);
            MiuiSystemUIDialog.setShowForAllUsers(this.mDialog, true);
            this.mDialog.show();
            this.mDialog.getButton(-1).setOnClickListener(new View.OnClickListener() {
                public final void onClick(View view) {
                    ControlPanelController.this.lambda$showDialog$0$ControlPanelController(view);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showDialog$0 */
    public /* synthetic */ void lambda$showDialog$0$ControlPanelController(View view) {
        dismissDialog(true);
    }

    private void dismissDialog(boolean z) {
        View findViewById;
        if (z) {
            Settings.System.putIntForUser(this.mContext.getContentResolver(), "nc_switch_guide_shown", 1, 0);
            this.mNcSwitchGuideShown = true;
        }
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            FrameLayout frameLayout = (FrameLayout) alertDialog.findViewById(C0015R$id.guide_content);
            if (!(frameLayout == null || (findViewById = frameLayout.findViewById(C0015R$id.guide_video)) == null)) {
                frameLayout.removeView(findViewById);
            }
            frameLayout.postDelayed(new Runnable() {
                public final void run() {
                    ControlPanelController.this.lambda$dismissDialog$1$ControlPanelController();
                }
            }, 1);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$dismissDialog$1 */
    public /* synthetic */ void lambda$dismissDialog$1$ControlPanelController() {
        AlertDialog alertDialog = this.mDialog;
        if (alertDialog != null) {
            alertDialog.dismiss();
            this.mDialog = null;
        }
    }
}

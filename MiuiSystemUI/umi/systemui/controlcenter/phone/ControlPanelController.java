package com.android.systemui.controlcenter.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0016R$integer;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controlcenter.ControlCenter;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.CallbackController;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class ControlPanelController implements CallbackController<UseControlPanelChangeListener> {
    private BroadcastDispatcher mBroadcastDispatcher;
    /* access modifiers changed from: private */
    public Context mContext;
    private ControlCenter mControlCenter;
    /* access modifiers changed from: private */
    public boolean mExpandableInKeyguard;
    private ContentObserver mExpandableObserver;
    private Handler mHandler = new H();
    private KeyguardViewMediator mKeyguardViewMediator;
    private final List<UseControlPanelChangeListener> mListeners;
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
                    if (ControlPanelController.this.isQSFullyCollapsed()) {
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
    /* access modifiers changed from: private */
    public StatusBar mStatusBar;
    private boolean mSuperPowerModeOn;
    /* access modifiers changed from: private */
    public boolean mUseControlPanel;
    private ContentObserver mUseControlPanelObserver;
    /* access modifiers changed from: private */
    public int mUseControlPanelSettingDefault;

    public interface UseControlPanelChangeListener {
        void onUseControlPanelChange(boolean z);
    }

    public ControlPanelController(Context context, KeyguardViewMediator keyguardViewMediator, BroadcastDispatcher broadcastDispatcher) {
        this.mContext = context;
        this.mListeners = new ArrayList();
        this.mUseControlPanelSettingDefault = context.getResources().getInteger(C0016R$integer.use_control_panel_setting_default);
        this.mKeyguardViewMediator = keyguardViewMediator;
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mUseControlPanelObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                ControlPanelController controlPanelController = ControlPanelController.this;
                boolean z2 = false;
                if (Settings.System.getIntForUser(controlPanelController.mContext.getContentResolver(), "use_control_panel", ControlPanelController.this.mUseControlPanelSettingDefault, 0) != 0) {
                    z2 = true;
                }
                boolean unused = controlPanelController.mUseControlPanel = z2;
                Log.d("ControlPanelController", "onChange: mUseControlPanel = " + ControlPanelController.this.mUseControlPanel);
                ControlPanelController.this.notifyAllListeners();
            }
        };
        this.mExpandableObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                ControlPanelController controlPanelController = ControlPanelController.this;
                boolean unused = controlPanelController.mExpandableInKeyguard = Settings.System.getIntForUser(controlPanelController.mContext.getContentResolver(), "expandable_under_lock_screen", 1, KeyguardUpdateMonitor.getCurrentUser()) != 0;
                if (!ControlPanelController.this.isExpandable()) {
                    ControlPanelController.this.collapsePanel(true);
                }
                Log.d("ControlPanelController", "onChange: mExpandableInKeyguard = " + ControlPanelController.this.mExpandableInKeyguard);
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
        if (this.mControlCenter != null && !isQSFullyCollapsed()) {
            this.mControlCenter.collapse(z);
        }
    }

    public void collapseControlCenter(boolean z) {
        if (this.mControlCenter != null && !isQSFullyCollapsed()) {
            this.mControlCenter.collapseControlCenter(z);
        }
    }

    public boolean isUseControlCenter() {
        return this.mUseControlPanel;
    }

    public boolean isExpandable() {
        return !this.mKeyguardViewMediator.isShowing() || this.mExpandableInKeyguard;
    }

    public boolean isQSFullyCollapsed() {
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

    private void register() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("use_control_panel"), false, this.mUseControlPanelObserver, -1);
        this.mUseControlPanelObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("expandable_under_lock_screen"), false, this.mExpandableObserver, -1);
        this.mExpandableObserver.onChange(false);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("action_panels_operation");
        this.mBroadcastDispatcher.registerReceiver(this.mRemoteOperationReceiver, intentFilter, (Executor) null, UserHandle.ALL);
    }

    private void unRegister() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mUseControlPanelObserver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mExpandableObserver);
        this.mBroadcastDispatcher.unregisterReceiver(this.mRemoteOperationReceiver);
    }

    /* access modifiers changed from: private */
    public void notifyAllListeners() {
        for (UseControlPanelChangeListener onUseControlPanelChange : this.mListeners) {
            onUseControlPanelChange.onUseControlPanelChange(this.mUseControlPanel);
        }
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
}

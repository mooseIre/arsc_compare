package com.android.systemui.controlcenter.phone;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.C0016R$integer;
import com.android.systemui.controlcenter.ControlCenter;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.statusbar.policy.CallbackController;
import java.util.ArrayList;
import java.util.List;

public class ControlPanelController implements CallbackController<UseControlPanelChangeListener> {
    /* access modifiers changed from: private */
    public Context mContext;
    private ControlCenter mControlCenter;
    /* access modifiers changed from: private */
    public boolean mExpandableInKeyguard;
    private ContentObserver mExpandableObserver;
    private Handler mHandler = new H();
    private KeyguardViewMediator mKeyguardViewMediator;
    private final List<UseControlPanelChangeListener> mListeners;
    private boolean mSuperPowerModeOn;
    /* access modifiers changed from: private */
    public boolean mUseControlPanel;
    private ContentObserver mUseControlPanelObserver;
    /* access modifiers changed from: private */
    public int mUseControlPanelSettingDefault;

    public interface UseControlPanelChangeListener {
        void onUseControlPanelChange(boolean z);
    }

    public ControlPanelController(Context context, KeyguardViewMediator keyguardViewMediator) {
        this.mContext = context;
        this.mListeners = new ArrayList();
        this.mUseControlPanelSettingDefault = context.getResources().getInteger(C0016R$integer.use_control_panel_setting_default);
        this.mKeyguardViewMediator = keyguardViewMediator;
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

    public void collapsePanel(boolean z) {
        if (this.mControlCenter != null && !isQSFullyCollapsed()) {
            this.mControlCenter.collapse(z);
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

    private void register() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("use_control_panel"), false, this.mUseControlPanelObserver, -1);
        this.mUseControlPanelObserver.onChange(false);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("expandable_under_lock_screen"), false, this.mExpandableObserver, -1);
        this.mExpandableObserver.onChange(false);
    }

    private void unRegister() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mUseControlPanelObserver);
        this.mContext.getContentResolver().unregisterContentObserver(this.mExpandableObserver);
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

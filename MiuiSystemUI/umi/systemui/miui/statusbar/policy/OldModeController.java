package com.android.systemui.miui.statusbar.policy;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.CallbackController;
import java.util.ArrayList;
import java.util.List;

public class OldModeController implements CallbackController<OldModeChangeListener> {
    /* access modifiers changed from: private */
    public Context mContext;
    private CurrentUserTracker mCurrentUserTracker;
    private Handler mHandler = new H();
    private final List<OldModeChangeListener> mListeners;
    /* access modifiers changed from: private */
    public ContentObserver mOldModeObserver;
    /* access modifiers changed from: private */
    public boolean mOldModeOn;

    public interface OldModeChangeListener {
        void onOldModeChange(boolean z);
    }

    public OldModeController(Context context) {
        this.mContext = context;
        this.mListeners = new ArrayList();
        this.mCurrentUserTracker = new CurrentUserTracker(this.mContext) {
            public void onUserSwitched(int i) {
                OldModeController.this.mOldModeObserver.onChange(false);
            }
        };
        this.mOldModeObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                OldModeController oldModeController = OldModeController.this;
                boolean z2 = false;
                if (Settings.System.getIntForUser(oldModeController.mContext.getContentResolver(), "elderly_mode", 0, KeyguardUpdateMonitor.getCurrentUser()) != 0) {
                    z2 = true;
                }
                boolean unused = oldModeController.mOldModeOn = z2;
                Log.d("OldModeController", "onChange: mOldModeOn = " + OldModeController.this.mOldModeOn);
                OldModeController.this.notifyAllListeners();
            }
        };
    }

    public boolean isActive() {
        return this.mOldModeOn;
    }

    private void register() {
        this.mCurrentUserTracker.startTracking();
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("elderly_mode"), false, this.mOldModeObserver, 0);
        this.mOldModeObserver.onChange(false);
    }

    private void unRegister() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mOldModeObserver);
        this.mCurrentUserTracker.stopTracking();
    }

    /* access modifiers changed from: private */
    public void notifyAllListeners() {
        for (OldModeChangeListener onOldModeChange : this.mListeners) {
            onOldModeChange.onOldModeChange(this.mOldModeOn);
        }
    }

    /* access modifiers changed from: private */
    public void addCallbackLocked(OldModeChangeListener oldModeChangeListener) {
        if (this.mListeners.isEmpty()) {
            register();
        }
        this.mListeners.add(oldModeChangeListener);
        oldModeChangeListener.onOldModeChange(this.mOldModeOn);
    }

    /* access modifiers changed from: private */
    public void removeCallbackLocked(OldModeChangeListener oldModeChangeListener) {
        this.mListeners.remove(oldModeChangeListener);
        if (this.mListeners.size() == 0) {
            unRegister();
        }
    }

    public void addCallback(OldModeChangeListener oldModeChangeListener) {
        if (oldModeChangeListener != null) {
            this.mHandler.obtainMessage(1, oldModeChangeListener).sendToTarget();
        }
    }

    public void removeCallback(OldModeChangeListener oldModeChangeListener) {
        if (oldModeChangeListener != null) {
            this.mHandler.obtainMessage(2, oldModeChangeListener).sendToTarget();
        }
    }

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                OldModeController.this.addCallbackLocked((OldModeChangeListener) message.obj);
            } else if (i == 2) {
                OldModeController.this.removeCallbackLocked((OldModeChangeListener) message.obj);
            } else if (i == 3) {
                OldModeController.this.notifyAllListeners();
            }
        }
    }
}

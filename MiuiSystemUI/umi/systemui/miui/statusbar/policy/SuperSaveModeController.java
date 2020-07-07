package com.android.systemui.miui.statusbar.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.statusbar.policy.CallbackController;
import java.util.ArrayList;
import java.util.List;

public class SuperSaveModeController implements CallbackController<SuperSaveModeChangeListener> {
    /* access modifiers changed from: private */
    public Context mContext;
    private Handler mHandler = new H();
    private final List<SuperSaveModeChangeListener> mListeners;
    private ContentObserver mSuperSaveModeObserver;
    /* access modifiers changed from: private */
    public boolean mSuperSaveModeOn;

    public interface SuperSaveModeChangeListener {
        void onSuperSaveModeChange(boolean z);
    }

    public SuperSaveModeController(Context context) {
        this.mContext = context;
        this.mListeners = new ArrayList();
        Settings.Secure.putIntForUser(this.mContext.getContentResolver(), "shield_super_save_bar", 1, ActivityManager.getCurrentUser());
        this.mSuperSaveModeObserver = new ContentObserver(this.mHandler) {
            public void onChange(boolean z) {
                SuperSaveModeController superSaveModeController = SuperSaveModeController.this;
                boolean z2 = false;
                if (Settings.System.getIntForUser(superSaveModeController.mContext.getContentResolver(), "power_supersave_mode_open", 0, ActivityManager.getCurrentUser()) != 0) {
                    z2 = true;
                }
                boolean unused = superSaveModeController.mSuperSaveModeOn = z2;
                Log.d("SuperSaveModeController", "onChange: mSuperSaveModeOn = " + SuperSaveModeController.this.mSuperSaveModeOn);
                SuperSaveModeController.this.notifyAllListeners();
            }
        };
    }

    public boolean isActive() {
        return this.mSuperSaveModeOn;
    }

    private void register() {
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("power_supersave_mode_open"), false, this.mSuperSaveModeObserver, -1);
        this.mSuperSaveModeObserver.onChange(false);
    }

    private void unRegister() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSuperSaveModeObserver);
    }

    /* access modifiers changed from: private */
    public void notifyAllListeners() {
        for (SuperSaveModeChangeListener onSuperSaveModeChange : this.mListeners) {
            onSuperSaveModeChange.onSuperSaveModeChange(this.mSuperSaveModeOn);
        }
    }

    /* access modifiers changed from: private */
    public void addCallbackLocked(SuperSaveModeChangeListener superSaveModeChangeListener) {
        this.mListeners.add(superSaveModeChangeListener);
        if (this.mListeners.size() == 1) {
            register();
        } else {
            superSaveModeChangeListener.onSuperSaveModeChange(this.mSuperSaveModeOn);
        }
    }

    /* access modifiers changed from: private */
    public void removeCallbackLocked(SuperSaveModeChangeListener superSaveModeChangeListener) {
        this.mListeners.remove(superSaveModeChangeListener);
        if (this.mListeners.size() == 0) {
            unRegister();
        }
    }

    public void addCallback(SuperSaveModeChangeListener superSaveModeChangeListener) {
        if (superSaveModeChangeListener != null) {
            this.mHandler.obtainMessage(1, superSaveModeChangeListener).sendToTarget();
        }
    }

    public void removeCallback(SuperSaveModeChangeListener superSaveModeChangeListener) {
        if (superSaveModeChangeListener != null) {
            this.mHandler.obtainMessage(2, superSaveModeChangeListener).sendToTarget();
        }
    }

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                SuperSaveModeController.this.addCallbackLocked((SuperSaveModeChangeListener) message.obj);
            } else if (i == 2) {
                SuperSaveModeController.this.removeCallbackLocked((SuperSaveModeChangeListener) message.obj);
            } else if (i == 3) {
                SuperSaveModeController.this.notifyAllListeners();
            }
        }
    }
}

package com.android.systemui.controlcenter.policy;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.statusbar.policy.CallbackController;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.util.MiuiTextUtils;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

public class SuperSaveModeController implements CallbackController<SuperSaveModeChangeListener>, SettingsObserver.Callback {
    private Context mContext;
    private Handler mHandler = new H();
    private final List<SuperSaveModeChangeListener> mListeners;
    private SettingsObserver mSettingsObserver;
    private boolean mSuperSaveModeOn;

    public interface SuperSaveModeChangeListener {
        void onSuperSaveModeChange(boolean z);
    }

    public SuperSaveModeController(Context context, SettingsObserver settingsObserver) {
        this.mContext = context;
        this.mSettingsObserver = settingsObserver;
        this.mListeners = new ArrayList();
        Settings.Secure.putInt(this.mContext.getContentResolver(), "shield_super_save_bar", 1);
    }

    public boolean isActive() {
        return this.mSuperSaveModeOn;
    }

    private void register() {
        this.mSettingsObserver.addCallback(this, "power_supersave_mode_open");
    }

    private void unRegister() {
        this.mSettingsObserver.removeCallback(this);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void notifyAllListeners() {
        for (SuperSaveModeChangeListener superSaveModeChangeListener : this.mListeners) {
            superSaveModeChangeListener.onSuperSaveModeChange(this.mSuperSaveModeOn);
        }
    }

    @Override // com.miui.systemui.SettingsObserver.Callback
    public void onContentChanged(@Nullable String str, @Nullable String str2) {
        if (((str.hashCode() == 280401189 && str.equals("power_supersave_mode_open")) ? (char) 0 : 65535) == 0) {
            this.mSuperSaveModeOn = MiuiTextUtils.parseBoolean(str2, false);
            Log.d("SuperSaveModeController", "onChange: mSuperSaveModeOn = " + this.mSuperSaveModeOn);
            notifyAllListeners();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void addCallbackLocked(SuperSaveModeChangeListener superSaveModeChangeListener) {
        this.mListeners.add(superSaveModeChangeListener);
        if (this.mListeners.size() == 1) {
            register();
        } else {
            superSaveModeChangeListener.onSuperSaveModeChange(this.mSuperSaveModeOn);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void removeCallbackLocked(SuperSaveModeChangeListener superSaveModeChangeListener) {
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

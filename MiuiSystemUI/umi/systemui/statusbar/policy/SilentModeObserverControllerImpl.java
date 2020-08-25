package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.miui.volume.VolumeUtil;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.SilentModeObserverController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import miui.provider.ExtraTelephony;

public class SilentModeObserverControllerImpl implements SilentModeObserverController {
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public boolean mEnabled;
    private final ArrayList<WeakReference<SilentModeObserverController.SilentModeListener>> mListeners = new ArrayList<>(1);
    private ExtraTelephony.QuietModeEnableListener mQuietModeObserver;
    private CurrentUserTracker mUserTracker;

    static {
        Log.isLoggable("SilentModeController", 3);
    }

    public SilentModeObserverControllerImpl(Context context) {
        this.mContext = context;
        AnonymousClass1 r3 = new ExtraTelephony.QuietModeEnableListener() {
            public void onQuietModeEnableChange(boolean z) {
                if (Build.VERSION.SDK_INT > 29) {
                    z = SilentModeObserverControllerImpl.this.isSilenceMode();
                }
                boolean unused = SilentModeObserverControllerImpl.this.mEnabled = z;
                SilentModeObserverControllerImpl.this.dispatchListeners(z);
            }
        };
        this.mQuietModeObserver = r3;
        ExtraTelephony.registerQuietModeEnableListener(this.mContext, r3);
        if (Build.VERSION.SDK_INT > 29) {
            this.mEnabled = isSilenceMode();
            registerModeRinger();
        } else {
            this.mEnabled = MiuiSettings.SilenceMode.isSilenceModeEnable(this.mContext);
        }
        AnonymousClass2 r32 = new CurrentUserTracker(this.mContext) {
            public void onUserSwitched(int i) {
                if (Build.VERSION.SDK_INT > 29) {
                    SilentModeObserverControllerImpl silentModeObserverControllerImpl = SilentModeObserverControllerImpl.this;
                    boolean unused = silentModeObserverControllerImpl.mEnabled = silentModeObserverControllerImpl.isSilenceMode();
                } else {
                    SilentModeObserverControllerImpl silentModeObserverControllerImpl2 = SilentModeObserverControllerImpl.this;
                    boolean unused2 = silentModeObserverControllerImpl2.mEnabled = MiuiSettings.SilenceMode.isSilenceModeEnable(silentModeObserverControllerImpl2.mContext);
                }
                SilentModeObserverControllerImpl silentModeObserverControllerImpl3 = SilentModeObserverControllerImpl.this;
                silentModeObserverControllerImpl3.dispatchListeners(silentModeObserverControllerImpl3.mEnabled);
            }
        };
        this.mUserTracker = r32;
        r32.startTracking();
    }

    public void addCallback(SilentModeObserverController.SilentModeListener silentModeListener) {
        synchronized (this.mListeners) {
            cleanUpListenersLocked(silentModeListener);
            this.mListeners.add(new WeakReference(silentModeListener));
            silentModeListener.onSilentModeChanged(this.mEnabled);
        }
    }

    public void removeCallback(SilentModeObserverController.SilentModeListener silentModeListener) {
        synchronized (this.mListeners) {
            cleanUpListenersLocked(silentModeListener);
        }
    }

    /* access modifiers changed from: private */
    public void dispatchListeners(boolean z) {
        synchronized (this.mListeners) {
            int size = this.mListeners.size();
            boolean z2 = false;
            for (int i = 0; i < size; i++) {
                SilentModeObserverController.SilentModeListener silentModeListener = (SilentModeObserverController.SilentModeListener) this.mListeners.get(i).get();
                if (silentModeListener != null) {
                    silentModeListener.onSilentModeChanged(z);
                } else {
                    z2 = true;
                }
            }
            if (z2) {
                cleanUpListenersLocked((SilentModeObserverController.SilentModeListener) null);
            }
        }
    }

    private void cleanUpListenersLocked(SilentModeObserverController.SilentModeListener silentModeListener) {
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            SilentModeObserverController.SilentModeListener silentModeListener2 = (SilentModeObserverController.SilentModeListener) this.mListeners.get(size).get();
            if (silentModeListener2 == null || silentModeListener2 == silentModeListener) {
                this.mListeners.remove(size);
            }
        }
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("SilentModeObserverController state:");
    }

    /* access modifiers changed from: private */
    public boolean isSilenceMode() {
        return VolumeUtil.getZenMode(this.mContext) != 0;
    }

    private void registerModeRinger() {
        if (Build.VERSION.SDK_INT > 29) {
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("mode_ringer"), false, new ContentObserver(new Handler()) {
                public void onChange(boolean z) {
                    super.onChange(z);
                    SilentModeObserverControllerImpl silentModeObserverControllerImpl = SilentModeObserverControllerImpl.this;
                    boolean unused = silentModeObserverControllerImpl.mEnabled = silentModeObserverControllerImpl.isSilenceMode();
                    SilentModeObserverControllerImpl silentModeObserverControllerImpl2 = SilentModeObserverControllerImpl.this;
                    silentModeObserverControllerImpl2.dispatchListeners(silentModeObserverControllerImpl2.mEnabled);
                }
            }, -1);
        }
    }
}

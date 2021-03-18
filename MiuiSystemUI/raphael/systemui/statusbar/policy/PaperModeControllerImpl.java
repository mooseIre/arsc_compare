package com.android.systemui.statusbar.policy;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.PaperModeController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class PaperModeControllerImpl extends CurrentUserTracker implements PaperModeController {
    private Handler mBgHandler;
    private final ContentObserver mGameModeObserver;
    private final ArrayList<PaperModeController.PaperModeListener> mListeners = new ArrayList<>();
    private boolean mPaperModeAvailable;
    private boolean mPaperModeEnabled;
    private final ContentObserver mPaperModeObserver;
    private ContentResolver mResolver;
    private final ContentObserver mVideoModeObserver;

    static {
        Log.isLoggable("PaperModeController", 3);
    }

    public PaperModeControllerImpl(Context context, Looper looper, BroadcastDispatcher broadcastDispatcher) {
        super(broadcastDispatcher);
        this.mBgHandler = new Handler(looper);
        this.mResolver = context.getContentResolver();
        this.mPaperModeObserver = new ContentObserver(this.mBgHandler) {
            /* class com.android.systemui.statusbar.policy.PaperModeControllerImpl.AnonymousClass1 */

            public void onChange(boolean z) {
                boolean z2 = false;
                int intForUser = Settings.System.getIntForUser(PaperModeControllerImpl.this.mResolver, "screen_paper_mode_enabled", 0, -2);
                PaperModeControllerImpl paperModeControllerImpl = PaperModeControllerImpl.this;
                if (intForUser != 0) {
                    z2 = true;
                }
                paperModeControllerImpl.mPaperModeEnabled = z2;
                PaperModeControllerImpl paperModeControllerImpl2 = PaperModeControllerImpl.this;
                paperModeControllerImpl2.dispatchModeChanged(paperModeControllerImpl2.mPaperModeEnabled);
            }
        };
        this.mVideoModeObserver = new ContentObserver(this.mBgHandler) {
            /* class com.android.systemui.statusbar.policy.PaperModeControllerImpl.AnonymousClass2 */

            public void onChange(boolean z) {
                boolean z2 = false;
                int intForUser = Settings.Secure.getIntForUser(PaperModeControllerImpl.this.mResolver, "vtb_boosting", 0, -2);
                PaperModeControllerImpl paperModeControllerImpl = PaperModeControllerImpl.this;
                if (intForUser == 0) {
                    z2 = true;
                }
                paperModeControllerImpl.mPaperModeAvailable = z2;
                PaperModeControllerImpl paperModeControllerImpl2 = PaperModeControllerImpl.this;
                paperModeControllerImpl2.dispatchAvailabilityChanged(paperModeControllerImpl2.mPaperModeAvailable);
            }
        };
        this.mResolver.registerContentObserver(Settings.System.getUriFor("screen_paper_mode_enabled"), false, this.mPaperModeObserver, -1);
        this.mGameModeObserver = new ContentObserver(this.mBgHandler) {
            /* class com.android.systemui.statusbar.policy.PaperModeControllerImpl.AnonymousClass3 */

            public void onChange(boolean z) {
                boolean z2 = false;
                int intForUser = Settings.Secure.getIntForUser(PaperModeControllerImpl.this.mResolver, "gb_boosting", 0, -2);
                PaperModeControllerImpl paperModeControllerImpl = PaperModeControllerImpl.this;
                if ((intForUser & 1) == 0) {
                    z2 = true;
                }
                paperModeControllerImpl.mPaperModeAvailable = z2;
                PaperModeControllerImpl paperModeControllerImpl2 = PaperModeControllerImpl.this;
                paperModeControllerImpl2.dispatchAvailabilityChanged(paperModeControllerImpl2.mPaperModeAvailable);
            }
        };
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor("gb_boosting"), false, this.mGameModeObserver, -1);
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor("vtb_boosting"), false, this.mVideoModeObserver, -1);
        postInitPaperModeState();
        startTracking();
    }

    @Override // com.android.systemui.statusbar.policy.PaperModeController
    public boolean isAvailable() {
        return this.mPaperModeAvailable;
    }

    @Override // com.android.systemui.statusbar.policy.PaperModeController
    public boolean isEnabled() {
        return this.mPaperModeEnabled;
    }

    @Override // com.android.systemui.statusbar.policy.PaperModeController
    public void setEnabled(boolean z) {
        if (this.mPaperModeEnabled != z && this.mPaperModeAvailable) {
            Settings.System.putIntForUser(this.mResolver, "screen_paper_mode_enabled", z ? 1 : 0, -2);
        }
    }

    @Override // com.android.systemui.settings.CurrentUserTracker
    public void onUserSwitched(int i) {
        postInitPaperModeState();
    }

    private void postInitPaperModeState() {
        this.mBgHandler.post(new Runnable() {
            /* class com.android.systemui.statusbar.policy.PaperModeControllerImpl.AnonymousClass4 */

            public void run() {
                PaperModeControllerImpl.this.mPaperModeObserver.onChange(false);
                PaperModeControllerImpl.this.mGameModeObserver.onChange(false);
            }
        });
    }

    public void addCallback(PaperModeController.PaperModeListener paperModeListener) {
        if (paperModeListener != null && !this.mListeners.contains(paperModeListener)) {
            synchronized (this.mListeners) {
                this.mListeners.add(paperModeListener);
            }
            paperModeListener.onPaperModeAvailabilityChanged(isAvailable());
            paperModeListener.onPaperModeChanged(this.mPaperModeEnabled);
        }
    }

    public void removeCallback(PaperModeController.PaperModeListener paperModeListener) {
        if (paperModeListener != null) {
            synchronized (this.mListeners) {
                this.mListeners.remove(paperModeListener);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchModeChanged(boolean z) {
        dispatchListeners(0, z);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dispatchAvailabilityChanged(boolean z) {
        dispatchListeners(1, z);
    }

    private void dispatchListeners(int i, boolean z) {
        synchronized (this.mListeners) {
            Iterator<PaperModeController.PaperModeListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                PaperModeController.PaperModeListener next = it.next();
                if (i == 0) {
                    next.onPaperModeChanged(z);
                } else if (i == 1) {
                    next.onPaperModeAvailabilityChanged(z);
                }
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("PaperModeController state:");
        printWriter.print("  mPaperModeEnabled=");
        printWriter.println(this.mPaperModeEnabled);
        printWriter.print("  isAvailable=");
        printWriter.println(this.mPaperModeAvailable);
    }
}

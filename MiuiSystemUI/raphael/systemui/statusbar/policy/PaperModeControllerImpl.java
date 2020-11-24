package com.android.systemui.statusbar.policy;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.PaperModeController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class PaperModeControllerImpl extends CurrentUserTracker implements PaperModeController {
    private static final boolean DEBUG = Log.isLoggable("PaperModeController", 3);
    private Handler mBgHandler;
    /* access modifiers changed from: private */
    public final ContentObserver mGameModeObserver;
    private final ArrayList<PaperModeController.PaperModeListener> mListeners = new ArrayList<>();
    /* access modifiers changed from: private */
    public boolean mPaperModeAvailable;
    /* access modifiers changed from: private */
    public boolean mPaperModeEnabled;
    /* access modifiers changed from: private */
    public final ContentObserver mPaperModeObserver;
    /* access modifiers changed from: private */
    public ContentResolver mResolver;
    /* access modifiers changed from: private */
    public final ContentObserver mVideoModeObserver;

    public PaperModeControllerImpl(Context context, Looper looper) {
        super(context);
        this.mBgHandler = new Handler(looper);
        this.mResolver = context.getContentResolver();
        this.mPaperModeObserver = new ContentObserver(this.mBgHandler) {
            public void onChange(boolean z) {
                boolean z2 = false;
                int intForUser = Settings.System.getIntForUser(PaperModeControllerImpl.this.mResolver, "screen_paper_mode_enabled", 0, -2);
                PaperModeControllerImpl paperModeControllerImpl = PaperModeControllerImpl.this;
                if (intForUser != 0) {
                    z2 = true;
                }
                boolean unused = paperModeControllerImpl.mPaperModeEnabled = z2;
                PaperModeControllerImpl paperModeControllerImpl2 = PaperModeControllerImpl.this;
                paperModeControllerImpl2.dispatchModeChanged(paperModeControllerImpl2.mPaperModeEnabled);
            }
        };
        this.mResolver.registerContentObserver(Settings.System.getUriFor("screen_paper_mode_enabled"), false, this.mPaperModeObserver, -1);
        this.mGameModeObserver = new ContentObserver(this.mBgHandler) {
            public void onChange(boolean z) {
                boolean z2 = false;
                int intForUser = Settings.System.getIntForUser(PaperModeControllerImpl.this.mResolver, "screen_game_mode", 0, -2);
                PaperModeControllerImpl paperModeControllerImpl = PaperModeControllerImpl.this;
                if ((intForUser & 1) == 0) {
                    z2 = true;
                }
                boolean unused = paperModeControllerImpl.mPaperModeAvailable = z2;
                PaperModeControllerImpl paperModeControllerImpl2 = PaperModeControllerImpl.this;
                paperModeControllerImpl2.dispatchAvailabilityChanged(paperModeControllerImpl2.mPaperModeAvailable);
            }
        };
        this.mVideoModeObserver = new ContentObserver(this.mBgHandler) {
            public void onChange(boolean z) {
                boolean z2 = false;
                int intForUser = Settings.Secure.getIntForUser(PaperModeControllerImpl.this.mResolver, "vtb_boosting", 0, -2);
                PaperModeControllerImpl paperModeControllerImpl = PaperModeControllerImpl.this;
                if (intForUser == 0) {
                    z2 = true;
                }
                boolean unused = paperModeControllerImpl.mPaperModeAvailable = z2;
                PaperModeControllerImpl paperModeControllerImpl2 = PaperModeControllerImpl.this;
                paperModeControllerImpl2.dispatchAvailabilityChanged(paperModeControllerImpl2.mPaperModeAvailable);
            }
        };
        this.mResolver.registerContentObserver(Settings.System.getUriFor("screen_game_mode"), false, this.mGameModeObserver, -1);
        this.mResolver.registerContentObserver(Settings.Secure.getUriFor("vtb_boosting"), false, this.mVideoModeObserver, -1);
        postInitPaperModeState();
        startTracking();
    }

    public boolean isAvailable() {
        return this.mPaperModeAvailable;
    }

    public boolean isEnabled() {
        return this.mPaperModeEnabled;
    }

    public void setEnabled(boolean z) {
        if (this.mPaperModeEnabled != z && this.mPaperModeAvailable) {
            Settings.System.putIntForUser(this.mResolver, "screen_paper_mode_enabled", z ? 1 : 0, -2);
        }
    }

    public void onUserSwitched(int i) {
        postInitPaperModeState();
    }

    private void postInitPaperModeState() {
        this.mBgHandler.post(new Runnable() {
            public void run() {
                PaperModeControllerImpl.this.mPaperModeObserver.onChange(false);
                PaperModeControllerImpl.this.mGameModeObserver.onChange(false);
                PaperModeControllerImpl.this.mVideoModeObserver.onChange(false);
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
    public void dispatchModeChanged(boolean z) {
        dispatchListeners(0, z);
    }

    /* access modifiers changed from: private */
    public void dispatchAvailabilityChanged(boolean z) {
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

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("PaperModeController state:");
        printWriter.print("  mPaperModeEnabled=");
        printWriter.println(this.mPaperModeEnabled);
        printWriter.print("  isAvailable=");
        printWriter.println(this.mPaperModeAvailable);
    }
}

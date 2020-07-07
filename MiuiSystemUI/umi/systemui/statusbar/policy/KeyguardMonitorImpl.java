package com.android.systemui.statusbar.policy;

import android.app.ActivityManager;
import android.content.Context;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class KeyguardMonitorImpl extends KeyguardUpdateMonitorCallback implements KeyguardMonitor {
    private final List<KeyguardMonitor.Callback> mCallbacks = Collections.synchronizedList(new ArrayList());
    private boolean mCanSkipBouncer;
    private final Context mContext;
    /* access modifiers changed from: private */
    public int mCurrentUser;
    private boolean mKeyguardFadingAway;
    private long mKeyguardFadingAwayDelay;
    private long mKeyguardFadingAwayDuration;
    private boolean mKeyguardGoingAway;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private boolean mListening;
    private boolean mOccluded;
    private boolean mSecure;
    private boolean mShowing;
    private boolean mSkipVolumeDialog = false;
    private final CurrentUserTracker mUserTracker;

    public KeyguardMonitorImpl(Context context) {
        this.mContext = context;
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mUserTracker = new CurrentUserTracker(this.mContext) {
            public void onUserSwitched(int i) {
                int unused = KeyguardMonitorImpl.this.mCurrentUser = i;
                KeyguardMonitorImpl.this.updateCanSkipBouncerState();
            }
        };
    }

    public void addCallback(KeyguardMonitor.Callback callback) {
        this.mCallbacks.add(callback);
        if (this.mCallbacks.size() != 0 && !this.mListening) {
            this.mListening = true;
            this.mCurrentUser = ActivityManager.getCurrentUser();
            updateCanSkipBouncerState();
            this.mKeyguardUpdateMonitor.registerCallback(this);
            this.mUserTracker.startTracking();
        }
    }

    public void removeCallback(KeyguardMonitor.Callback callback) {
        if (this.mCallbacks.remove(callback) && this.mCallbacks.size() == 0 && this.mListening) {
            this.mListening = false;
            this.mKeyguardUpdateMonitor.removeCallback(this);
            this.mUserTracker.stopTracking();
        }
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public boolean isSecure() {
        return this.mSecure;
    }

    public boolean isOccluded() {
        return this.mOccluded;
    }

    public boolean canSkipBouncer() {
        return this.mCanSkipBouncer;
    }

    public void notifyKeyguardState(boolean z, boolean z2, boolean z3) {
        if (this.mShowing != z || this.mSecure != z2 || this.mOccluded != z3) {
            this.mShowing = z;
            this.mSecure = z2;
            this.mOccluded = z3;
            notifyKeyguardChanged();
        }
    }

    public void notifySkipVolumeDialog(boolean z) {
        this.mSkipVolumeDialog = z;
    }

    public boolean needSkipVolumeDialog() {
        return this.mSkipVolumeDialog;
    }

    public boolean isDeviceInteractive() {
        return this.mKeyguardUpdateMonitor.isDeviceInteractive();
    }

    /* access modifiers changed from: private */
    public void updateCanSkipBouncerState() {
        this.mCanSkipBouncer = this.mKeyguardUpdateMonitor.getUserCanSkipBouncer(this.mCurrentUser);
    }

    private void notifyKeyguardChanged() {
        Iterator it = new ArrayList(this.mCallbacks).iterator();
        while (it.hasNext()) {
            ((KeyguardMonitor.Callback) it.next()).onKeyguardShowingChanged();
        }
    }

    public void notifyKeyguardFadingAway(long j, long j2) {
        this.mKeyguardFadingAway = true;
        this.mKeyguardFadingAwayDelay = j;
        this.mKeyguardFadingAwayDuration = j2;
    }

    public void notifyKeyguardDoneFading() {
        this.mKeyguardFadingAway = false;
        this.mKeyguardGoingAway = false;
    }

    public boolean isKeyguardFadingAway() {
        return this.mKeyguardFadingAway;
    }

    public boolean isKeyguardGoingAway() {
        return this.mKeyguardGoingAway;
    }

    public long getKeyguardFadingAwayDelay() {
        return this.mKeyguardFadingAwayDelay;
    }

    public long getKeyguardFadingAwayDuration() {
        return this.mKeyguardFadingAwayDuration;
    }

    public void notifyKeyguardGoingAway(boolean z) {
        this.mKeyguardGoingAway = z;
    }
}

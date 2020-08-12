package com.android.systemui.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.KeyguardUpdateMonitor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class CurrentUserTracker {
    private Consumer<Integer> mCallback;
    private final UserReceiver mUserReceiver;

    public abstract void onUserSwitched(int i);

    public CurrentUserTracker(Context context) {
        this(UserReceiver.getInstance(context));
    }

    @VisibleForTesting
    CurrentUserTracker(UserReceiver userReceiver) {
        this.mCallback = new Consumer() {
            public final void accept(Object obj) {
                CurrentUserTracker.this.onUserSwitched(((Integer) obj).intValue());
            }
        };
        this.mUserReceiver = userReceiver;
    }

    public void startTracking() {
        this.mUserReceiver.addTracker(this.mCallback);
    }

    public void stopTracking() {
        this.mUserReceiver.removeTracker(this.mCallback);
    }

    @VisibleForTesting
    static class UserReceiver extends BroadcastReceiver {
        private static UserReceiver sInstance;
        private Context mAppContext;
        private List<Consumer<Integer>> mCallbacks = new ArrayList();
        private int mCurrentUserId;
        private boolean mReceiverRegistered;

        @VisibleForTesting
        UserReceiver(Context context) {
            this.mAppContext = context.getApplicationContext();
        }

        static UserReceiver getInstance(Context context) {
            if (sInstance == null) {
                sInstance = new UserReceiver(context);
            }
            return sInstance;
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                notifyUserSwitched(intent.getIntExtra("android.intent.extra.user_handle", 0));
            }
        }

        /* access modifiers changed from: private */
        public void addTracker(Consumer<Integer> consumer) {
            if (!this.mCallbacks.contains(consumer)) {
                this.mCallbacks.add(consumer);
            }
            if (!this.mReceiverRegistered) {
                this.mCurrentUserId = KeyguardUpdateMonitor.getCurrentUser();
                this.mAppContext.registerReceiver(this, new IntentFilter("android.intent.action.USER_SWITCHED"));
                this.mReceiverRegistered = true;
            }
        }

        /* access modifiers changed from: private */
        public void removeTracker(Consumer<Integer> consumer) {
            if (this.mCallbacks.contains(consumer)) {
                this.mCallbacks.remove(consumer);
                if (this.mCallbacks.size() == 0 && this.mReceiverRegistered) {
                    this.mAppContext.unregisterReceiver(this);
                    this.mReceiverRegistered = false;
                }
            }
        }

        private void notifyUserSwitched(int i) {
            if (this.mCurrentUserId != i) {
                this.mCurrentUserId = i;
                for (Consumer consumer : new ArrayList(this.mCallbacks)) {
                    if (this.mCallbacks.contains(consumer)) {
                        consumer.accept(Integer.valueOf(i));
                    }
                }
            }
        }
    }
}

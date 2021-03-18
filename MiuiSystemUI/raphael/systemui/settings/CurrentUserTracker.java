package com.android.systemui.settings;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.UserHandle;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.broadcast.BroadcastDispatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class CurrentUserTracker {
    private Consumer<Integer> mCallback;
    private final UserReceiver mUserReceiver;

    public abstract void onUserSwitched(int i);

    public CurrentUserTracker(BroadcastDispatcher broadcastDispatcher) {
        this(UserReceiver.getInstance(broadcastDispatcher));
    }

    @VisibleForTesting
    CurrentUserTracker(UserReceiver userReceiver) {
        this.mCallback = new Consumer() {
            /* class com.android.systemui.settings.$$Lambda$JYv4q5Exc5xk6WCK6WtC6eC0sA8 */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                CurrentUserTracker.this.onUserSwitched(((Integer) obj).intValue());
            }
        };
        this.mUserReceiver = userReceiver;
    }

    public int getCurrentUserId() {
        return this.mUserReceiver.getCurrentUserId();
    }

    public void startTracking() {
        this.mUserReceiver.addTracker(this.mCallback);
    }

    public void stopTracking() {
        this.mUserReceiver.removeTracker(this.mCallback);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public static class UserReceiver extends BroadcastReceiver {
        private static UserReceiver sInstance;
        private final BroadcastDispatcher mBroadcastDispatcher;
        private List<Consumer<Integer>> mCallbacks = new ArrayList();
        private int mCurrentUserId;
        private boolean mReceiverRegistered;

        @VisibleForTesting
        UserReceiver(BroadcastDispatcher broadcastDispatcher) {
            this.mBroadcastDispatcher = broadcastDispatcher;
        }

        static UserReceiver getInstance(BroadcastDispatcher broadcastDispatcher) {
            if (sInstance == null) {
                sInstance = new UserReceiver(broadcastDispatcher);
            }
            return sInstance;
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                notifyUserSwitched(intent.getIntExtra("android.intent.extra.user_handle", 0));
            }
        }

        public int getCurrentUserId() {
            return this.mCurrentUserId;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void addTracker(Consumer<Integer> consumer) {
            if (!this.mCallbacks.contains(consumer)) {
                this.mCallbacks.add(consumer);
            }
            if (!this.mReceiverRegistered) {
                this.mCurrentUserId = ActivityManager.getCurrentUser();
                this.mBroadcastDispatcher.registerReceiver(this, new IntentFilter("android.intent.action.USER_SWITCHED"), null, UserHandle.ALL);
                this.mReceiverRegistered = true;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void removeTracker(Consumer<Integer> consumer) {
            if (this.mCallbacks.contains(consumer)) {
                this.mCallbacks.remove(consumer);
                if (this.mCallbacks.size() == 0 && this.mReceiverRegistered) {
                    this.mBroadcastDispatcher.unregisterReceiver(this);
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

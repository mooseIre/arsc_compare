package com.android.systemui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.systemui.broadcast.BroadcastDispatcher;

public class CurrentUserObservable {
    /* access modifiers changed from: private */
    public final MutableLiveData<Integer> mCurrentUser = new MutableLiveData<Integer>() {
        /* access modifiers changed from: protected */
        public void onActive() {
            super.onActive();
            CurrentUserObservable.this.mTracker.startTracking();
        }

        /* access modifiers changed from: protected */
        public void onInactive() {
            super.onInactive();
            CurrentUserObservable.this.mTracker.stopTracking();
        }
    };
    /* access modifiers changed from: private */
    public final CurrentUserTracker mTracker;

    public CurrentUserObservable(BroadcastDispatcher broadcastDispatcher) {
        this.mTracker = new CurrentUserTracker(broadcastDispatcher) {
            public void onUserSwitched(int i) {
                CurrentUserObservable.this.mCurrentUser.setValue(Integer.valueOf(i));
            }
        };
    }

    public LiveData<Integer> getCurrentUser() {
        if (this.mCurrentUser.getValue() == null) {
            this.mCurrentUser.setValue(Integer.valueOf(this.mTracker.getCurrentUserId()));
        }
        return this.mCurrentUser;
    }
}

package com.android.systemui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.systemui.broadcast.BroadcastDispatcher;

public class CurrentUserObservable {
    private final MutableLiveData<Integer> mCurrentUser = new MutableLiveData<Integer>() {
        /* class com.android.systemui.settings.CurrentUserObservable.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // androidx.lifecycle.LiveData
        public void onActive() {
            super.onActive();
            CurrentUserObservable.this.mTracker.startTracking();
        }

        /* access modifiers changed from: protected */
        @Override // androidx.lifecycle.LiveData
        public void onInactive() {
            super.onInactive();
            CurrentUserObservable.this.mTracker.stopTracking();
        }
    };
    private final CurrentUserTracker mTracker;

    public CurrentUserObservable(BroadcastDispatcher broadcastDispatcher) {
        this.mTracker = new CurrentUserTracker(broadcastDispatcher) {
            /* class com.android.systemui.settings.CurrentUserObservable.AnonymousClass2 */

            @Override // com.android.systemui.settings.CurrentUserTracker
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

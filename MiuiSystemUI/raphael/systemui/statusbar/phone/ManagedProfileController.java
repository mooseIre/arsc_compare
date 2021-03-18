package com.android.systemui.statusbar.phone;

import com.android.systemui.statusbar.policy.CallbackController;

public interface ManagedProfileController extends CallbackController<Callback> {

    public interface Callback {
        void onManagedProfileChanged();

        void onManagedProfileRemoved();
    }

    boolean hasActiveProfile();

    boolean isWorkModeEnabled();

    void setWorkModeEnabled(boolean z);
}

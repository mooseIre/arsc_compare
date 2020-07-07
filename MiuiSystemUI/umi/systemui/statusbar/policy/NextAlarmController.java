package com.android.systemui.statusbar.policy;

import com.android.systemui.Dumpable;

public interface NextAlarmController extends CallbackController<NextAlarmChangeCallback>, Dumpable {

    public interface NextAlarmChangeCallback {
        void onNextAlarmChanged(boolean z);
    }

    boolean hasAlarm();
}

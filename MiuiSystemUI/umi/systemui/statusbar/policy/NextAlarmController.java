package com.android.systemui.statusbar.policy;

import android.app.AlarmManager;
import com.android.systemui.Dumpable;

public interface NextAlarmController extends CallbackController<NextAlarmChangeCallback>, Dumpable {

    public interface NextAlarmChangeCallback {
        void onNextAlarmChanged(AlarmManager.AlarmClockInfo alarmClockInfo);
    }

    boolean hasAlarm();
}

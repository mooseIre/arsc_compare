package com.android.systemui.statusbar.notification.unimportant;

import java.util.Calendar;
import java.util.Date;

public class DateUtils {
    public static int getDigitalFormatDateToday() {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        return getDigitalFormatDate(instance);
    }

    public static int getDigitalPreviousMonthDate() {
        int i;
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        int i2 = instance.get(1);
        int i3 = instance.get(2) + 1;
        if (i3 == 1) {
            i2--;
            i = 12;
        } else {
            i = i3 - 1;
        }
        return (i2 * 10000) + (i * 100) + instance.get(5);
    }

    public static int getDigitalFormatDate(Calendar calendar) {
        return (calendar.get(1) * 10000) + ((calendar.get(2) + 1) * 100) + calendar.get(5);
    }
}

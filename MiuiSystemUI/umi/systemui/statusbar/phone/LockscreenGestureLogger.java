package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.metrics.LogMaker;
import android.util.ArrayMap;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.MetricsLoggerCompat;
import com.android.systemui.Dependency;
import com.android.systemui.EventLogConstants;
import com.android.systemui.EventLogTags;

public class LockscreenGestureLogger {
    private ArrayMap<Integer, Integer> mLegacyMap = new ArrayMap<>(EventLogConstants.METRICS_GESTURE_TYPE_MAP.length);
    private LogMaker mLogMaker = new LogMaker(0).setType(4);
    private final MetricsLogger mMetricsLogger = ((MetricsLogger) Dependency.get(MetricsLogger.class));

    public LockscreenGestureLogger() {
        int i = 0;
        while (true) {
            int[] iArr = EventLogConstants.METRICS_GESTURE_TYPE_MAP;
            if (i < iArr.length) {
                this.mLegacyMap.put(Integer.valueOf(iArr[i]), Integer.valueOf(i));
                i++;
            } else {
                return;
            }
        }
    }

    public void write(Context context, int i, int i2, int i3) {
        MetricsLoggerCompat.write(context, this.mMetricsLogger, this.mLogMaker.setCategory(i).setType(4).addTaggedData(826, Integer.valueOf(i2)).addTaggedData(827, Integer.valueOf(i3)));
        EventLogTags.writeSysuiLockscreenGesture(safeLookup(i), i2, i3);
    }

    private int safeLookup(int i) {
        Integer num = this.mLegacyMap.get(Integer.valueOf(i));
        if (num == null) {
            return 0;
        }
        return num.intValue();
    }
}

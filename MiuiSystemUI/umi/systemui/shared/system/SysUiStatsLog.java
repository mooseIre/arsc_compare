package com.android.systemui.shared.system;

import android.util.StatsEvent;
import android.util.StatsLog;

public class SysUiStatsLog {
    public static void write(int i, String str, String str2, int i2, int i3, int i4, int i5, float f, float f2, boolean z, boolean z2, boolean z3) {
        StatsEvent.Builder newBuilder = StatsEvent.newBuilder();
        newBuilder.setAtomId(i);
        newBuilder.writeString(str);
        newBuilder.writeString(str2);
        newBuilder.writeInt(i2);
        newBuilder.writeInt(i3);
        newBuilder.writeInt(i4);
        newBuilder.writeInt(i5);
        newBuilder.writeFloat(f);
        newBuilder.writeFloat(f2);
        newBuilder.writeBoolean(z);
        newBuilder.writeBoolean(z2);
        newBuilder.writeBoolean(z3);
        newBuilder.usePooledBuffer();
        StatsLog.write(newBuilder.build());
    }
}

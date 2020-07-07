package com.android.systemui.statusbar;

public interface InflationTask {
    void abort();

    void supersedeTask(InflationTask inflationTask);
}

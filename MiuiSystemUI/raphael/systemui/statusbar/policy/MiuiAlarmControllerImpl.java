package com.android.systemui.statusbar.policy;

import android.content.Context;
import com.android.systemui.Dependency;
import com.android.systemui.Dumpable;
import com.miui.systemui.SettingsObserver;
import com.miui.systemui.util.MiuiTextUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.jetbrains.annotations.Nullable;

public class MiuiAlarmControllerImpl implements CallbackController, Dumpable, SettingsObserver.Callback {
    private final ArrayList<MiuiAlarmController$MiuiAlarmChangeCallback> mChangeCallbacks = new ArrayList<>();
    private boolean mHasAlarm;
    private SettingsObserver mSettingsObserver;

    public MiuiAlarmControllerImpl(Context context) {
        SettingsObserver settingsObserver = (SettingsObserver) Dependency.get(SettingsObserver.class);
        this.mSettingsObserver = settingsObserver;
        settingsObserver.addCallback(this, "next_alarm_clock_formatted");
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("MiuiNextAlarmControllerImpl state:");
        printWriter.print("  mHasAlarm=");
        printWriter.println(this.mHasAlarm);
    }

    public void addCallback(MiuiAlarmController$MiuiAlarmChangeCallback miuiAlarmController$MiuiAlarmChangeCallback) {
        this.mChangeCallbacks.add(miuiAlarmController$MiuiAlarmChangeCallback);
        miuiAlarmController$MiuiAlarmChangeCallback.onNextAlarmChanged(this.mHasAlarm);
    }

    public void removeCallback(MiuiAlarmController$MiuiAlarmChangeCallback miuiAlarmController$MiuiAlarmChangeCallback) {
        this.mChangeCallbacks.remove(miuiAlarmController$MiuiAlarmChangeCallback);
    }

    private void fireNextAlarmChanged() {
        int size = this.mChangeCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mChangeCallbacks.get(i).onNextAlarmChanged(this.mHasAlarm);
        }
    }

    @Override // com.miui.systemui.SettingsObserver.Callback
    public void onContentChanged(@Nullable String str, @Nullable String str2) {
        if (str.equals("next_alarm_clock_formatted")) {
            this.mHasAlarm = !MiuiTextUtils.isEmpty(str2);
            fireNextAlarmChanged();
        }
    }
}

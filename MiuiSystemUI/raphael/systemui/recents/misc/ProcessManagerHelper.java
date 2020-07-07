package com.android.systemui.recents.misc;

import android.util.Slog;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import miui.process.ProcessConfig;
import miui.process.ProcessManager;

public class ProcessManagerHelper {
    private static HashMap<String, ArrayList<String>> sRelatedPkgMap = new HashMap<>();

    static {
        ArrayList arrayList = new ArrayList();
        arrayList.add("com.miui.fmservice");
        sRelatedPkgMap.put("com.miui.fm", arrayList);
    }

    public static boolean isHasRelatedPkg(String str) {
        return sRelatedPkgMap.containsKey(str);
    }

    public static ArrayList<String> getRelatedPkg(String str) {
        return sRelatedPkgMap.get(str);
    }

    public static void updateApplicationLockedState(String str, int i, boolean z) {
        try {
            ProcessManager.updateApplicationLockedState(str, i, z);
            if (isHasRelatedPkg(str)) {
                Iterator<String> it = getRelatedPkg(str).iterator();
                while (it.hasNext()) {
                    ProcessManager.updateApplicationLockedState(it.next(), i, z);
                }
            }
        } catch (Exception e) {
            Slog.e("ProcessManagerHelper", "ChangeTaskLockState", e);
        }
    }

    public static void performSwipeUpClean(String str, int i, int i2) {
        try {
            ProcessConfig processConfig = new ProcessConfig(7, str, i, i2);
            processConfig.setRemoveTaskNeeded(true);
            ProcessManager.kill(processConfig);
            if (isHasRelatedPkg(str)) {
                Iterator<String> it = getRelatedPkg(str).iterator();
                while (it.hasNext()) {
                    ProcessManager.kill(new ProcessConfig(7, it.next(), i, i2));
                }
            }
        } catch (Exception e) {
            Slog.e("ProcessManagerHelper", "performSwipeUpClean", e);
        }
    }
}

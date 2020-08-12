package com.android.systemui.statistic;

import android.os.statistics.E2EScenario;
import android.os.statistics.E2EScenarioPayload;
import android.os.statistics.E2EScenarioPerfTracer;
import android.os.statistics.E2EScenarioSettings;
import android.util.Log;
import java.util.Map;

public class ScenarioTrackUtil {
    private static final String TAG = "ScenarioTrackUtil";
    private static E2EScenarioSettings sScenarioSettings = new E2EScenarioSettings();

    static {
        sScenarioSettings.setStatisticsMode(7);
        sScenarioSettings.setHistoryLimitPerDay(200);
    }

    public static void beginScenario(SystemUIEventScenario systemUIEventScenario) {
        beginScenario(systemUIEventScenario, (Map) null);
    }

    public static void beginScenario(SystemUIEventScenario systemUIEventScenario, Map map) {
        if (systemUIEventScenario.mE2eScenario == null) {
            String str = TAG;
            Log.w(str, systemUIEventScenario.toString() + " event start cancel due to scenario is null!");
            return;
        }
        if (systemUIEventScenario.isTrackStarted) {
            E2EScenarioPerfTracer.abortScenario(systemUIEventScenario.mE2eScenario);
        }
        if (map != null) {
            E2EScenarioPayload e2EScenarioPayload = new E2EScenarioPayload();
            e2EScenarioPayload.putAll(map);
            E2EScenarioPerfTracer.asyncBeginScenario(systemUIEventScenario.mE2eScenario, sScenarioSettings, e2EScenarioPayload);
        } else {
            E2EScenarioPerfTracer.asyncBeginScenario(systemUIEventScenario.mE2eScenario, sScenarioSettings);
        }
        systemUIEventScenario.isTrackStarted = true;
    }

    public static void finishScenario(SystemUIEventScenario systemUIEventScenario) {
        if (systemUIEventScenario.mE2eScenario == null) {
            String str = TAG;
            Log.w(str, systemUIEventScenario.toString() + " event end cancel, due to scenario is null!");
        } else if (!systemUIEventScenario.isTrackStarted) {
            String str2 = TAG;
            Log.w(str2, systemUIEventScenario.toString() + " event end cancel, due to scenario has not started!");
        } else {
            E2EScenarioPerfTracer.finishScenario(systemUIEventScenario.mE2eScenario);
            systemUIEventScenario.isTrackStarted = false;
        }
    }

    public static class SystemUIEventScenario {
        public volatile boolean isTrackStarted = false;
        E2EScenario mE2eScenario;
        String mEventName;

        SystemUIEventScenario(String str) {
            this.mE2eScenario = initE2EScenario(str);
            this.mEventName = str;
        }

        private E2EScenario initE2EScenario(String str) {
            return new E2EScenario("com.android.systemui", "Performance", str);
        }

        public String toString() {
            return this.mEventName;
        }
    }
}

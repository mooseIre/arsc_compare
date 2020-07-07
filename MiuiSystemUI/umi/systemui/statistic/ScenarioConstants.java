package com.android.systemui.statistic;

import com.android.systemui.statistic.ScenarioTrackUtil;

public class ScenarioConstants {
    public static final ScenarioTrackUtil.SystemUIEventScenario SCENARIO_CLEAR_ALL_NOTI = new ScenarioTrackUtil.SystemUIEventScenario("clearAllNotification");
    public static final ScenarioTrackUtil.SystemUIEventScenario SCENARIO_CLEAR_NOTI = new ScenarioTrackUtil.SystemUIEventScenario("clearNotification");
    public static final ScenarioTrackUtil.SystemUIEventScenario SCENARIO_EXPAND_VOLUME_DIALOG = new ScenarioTrackUtil.SystemUIEventScenario("expandVolumeDialog");
    public static final ScenarioTrackUtil.SystemUIEventScenario SCENARIO_VOLUME_DIALOG_HIDE = new ScenarioTrackUtil.SystemUIEventScenario("volumeDialogHide");
    public static final ScenarioTrackUtil.SystemUIEventScenario SCENARIO_VOLUME_DIALOG_SHOW = new ScenarioTrackUtil.SystemUIEventScenario("volumeDialogShow");

    static {
        new ScenarioTrackUtil.SystemUIEventScenario("statusBarShow");
        new ScenarioTrackUtil.SystemUIEventScenario("statusBarHide");
        new ScenarioTrackUtil.SystemUIEventScenario("openFold");
        new ScenarioTrackUtil.SystemUIEventScenario("closeFold");
    }
}

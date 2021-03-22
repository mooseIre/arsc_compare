package com.android.systemui.pip;

import android.app.TaskInfo;
import com.android.internal.logging.UiEventLogger;

public class PipUiEventLogger {
    private TaskInfo mTaskInfo;
    private final UiEventLogger mUiEventLogger;

    public PipUiEventLogger(UiEventLogger uiEventLogger) {
        this.mUiEventLogger = uiEventLogger;
    }

    public void setTaskInfo(TaskInfo taskInfo) {
        this.mTaskInfo = taskInfo;
    }

    public void log(PipUiEventEnum pipUiEventEnum) {
        TaskInfo taskInfo = this.mTaskInfo;
        if (taskInfo != null) {
            this.mUiEventLogger.log(pipUiEventEnum, taskInfo.userId, taskInfo.topActivity.getPackageName());
        }
    }

    public enum PipUiEventEnum implements UiEventLogger.UiEventEnum {
        PICTURE_IN_PICTURE_ENTER(603),
        PICTURE_IN_PICTURE_EXPAND_TO_FULLSCREEN(604),
        PICTURE_IN_PICTURE_TAP_TO_REMOVE(605),
        PICTURE_IN_PICTURE_DRAG_TO_REMOVE(606),
        PICTURE_IN_PICTURE_SHOW_MENU(607),
        PICTURE_IN_PICTURE_HIDE_MENU(608),
        PICTURE_IN_PICTURE_CHANGE_ASPECT_RATIO(609),
        PICTURE_IN_PICTURE_RESIZE(610);
        
        private final int mId;

        private PipUiEventEnum(int i) {
            this.mId = i;
        }

        public int getId() {
            return this.mId;
        }
    }
}

package com.android.systemui.assist;

public interface AssistHandleCallbacks {
    void hide();

    void showAndGo();

    void showAndGoDelayed(long j, boolean z);

    void showAndStay();
}

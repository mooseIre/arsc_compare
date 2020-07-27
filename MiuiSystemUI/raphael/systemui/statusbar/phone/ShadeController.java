package com.android.systemui.statusbar.phone;

import android.content.IntentSender;
import android.view.View;
import com.android.systemui.miui.statusbar.ExpandedNotification;
import com.android.systemui.statusbar.NotificationData;

public interface ShadeController {
    void addPostCollapseAction(Runnable runnable);

    void animateCollapsePanels(int i, boolean z);

    boolean collapsePanel();

    int indexOfEntry(NotificationData.Entry entry);

    boolean isKeyguardShowing();

    void performRemoveNotification(ExpandedNotification expandedNotification);

    void readyForKeyguardDone();

    boolean startWorkChallengeIfNecessary(int i, IntentSender intentSender, String str);

    void visibilityChanged(boolean z);

    void wakeUpIfDozing(long j, View view, String str);
}

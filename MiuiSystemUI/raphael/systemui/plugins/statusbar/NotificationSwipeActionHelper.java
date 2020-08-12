package com.android.systemui.plugins.statusbar;

import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import android.view.MotionEvent;
import android.view.View;
import com.android.systemui.plugins.annotations.DependsOn;
import com.android.systemui.plugins.annotations.ProvidesInterface;

@DependsOn(target = SnoozeOption.class)
@ProvidesInterface(version = 1)
public interface NotificationSwipeActionHelper {
    public static final String ACTION = "com.android.systemui.action.PLUGIN_NOTIFICATION_SWIPE_ACTION";
    public static final int VERSION = 1;

    void dismiss(View view, float f);

    float getMinDismissVelocity();

    boolean isDismissGesture(MotionEvent motionEvent);

    boolean isFalseGesture(MotionEvent motionEvent);

    void snap(View view, float f, float f2);

    void snooze(StatusBarNotification statusBarNotification, SnoozeOption snoozeOption);

    boolean swipedFarEnough(float f, float f2);

    boolean swipedFastEnough(float f, float f2);

    @ProvidesInterface(version = 1)
    public static class SnoozeOption {
        public static final int VERSION = 1;
        public CharSequence confirmation;
        public SnoozeCriterion criterion;
        public CharSequence description;
        public int snoozeForMinutes;

        public SnoozeOption(SnoozeCriterion snoozeCriterion, int i, CharSequence charSequence, CharSequence charSequence2) {
            this.criterion = snoozeCriterion;
            this.snoozeForMinutes = i;
            this.description = charSequence;
            this.confirmation = charSequence2;
        }
    }
}

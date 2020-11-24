package com.android.systemui.keyguard;

import android.app.AlarmManager;
import android.content.ContentResolver;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.ZenModeController;

public final class KeyguardSliceProvider_MembersInjector {
    public static void injectMDozeParameters(KeyguardSliceProvider keyguardSliceProvider, DozeParameters dozeParameters) {
        keyguardSliceProvider.mDozeParameters = dozeParameters;
    }

    public static void injectMZenModeController(KeyguardSliceProvider keyguardSliceProvider, ZenModeController zenModeController) {
        keyguardSliceProvider.mZenModeController = zenModeController;
    }

    public static void injectMNextAlarmController(KeyguardSliceProvider keyguardSliceProvider, NextAlarmController nextAlarmController) {
        keyguardSliceProvider.mNextAlarmController = nextAlarmController;
    }

    public static void injectMAlarmManager(KeyguardSliceProvider keyguardSliceProvider, AlarmManager alarmManager) {
        keyguardSliceProvider.mAlarmManager = alarmManager;
    }

    public static void injectMContentResolver(KeyguardSliceProvider keyguardSliceProvider, ContentResolver contentResolver) {
        keyguardSliceProvider.mContentResolver = contentResolver;
    }

    public static void injectMMediaManager(KeyguardSliceProvider keyguardSliceProvider, NotificationMediaManager notificationMediaManager) {
        keyguardSliceProvider.mMediaManager = notificationMediaManager;
    }

    public static void injectMStatusBarStateController(KeyguardSliceProvider keyguardSliceProvider, StatusBarStateController statusBarStateController) {
        keyguardSliceProvider.mStatusBarStateController = statusBarStateController;
    }

    public static void injectMKeyguardBypassController(KeyguardSliceProvider keyguardSliceProvider, KeyguardBypassController keyguardBypassController) {
        keyguardSliceProvider.mKeyguardBypassController = keyguardBypassController;
    }
}

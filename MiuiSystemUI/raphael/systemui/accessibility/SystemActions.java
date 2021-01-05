package com.android.systemui.accessibility;

import android.app.RemoteAction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.accessibility.dialog.AccessibilityButtonChooserActivity;
import com.android.internal.util.ScreenshotHelper;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.Recents;
import com.android.systemui.statusbar.phone.StatusBar;
import java.util.Locale;
import java.util.function.Consumer;

public class SystemActions extends SystemUI {
    private AccessibilityManager mA11yManager = ((AccessibilityManager) this.mContext.getSystemService("accessibility"));
    private Locale mLocale = this.mContext.getResources().getConfiguration().getLocales().get(0);
    private SystemActionsBroadcastReceiver mReceiver = new SystemActionsBroadcastReceiver();
    private Recents mRecents = ((Recents) Dependency.get(Recents.class));
    private StatusBar mStatusBar = ((StatusBar) Dependency.get(StatusBar.class));

    public SystemActions(Context context) {
        super(context);
    }

    public void start() {
        Context context = this.mContext;
        SystemActionsBroadcastReceiver systemActionsBroadcastReceiver = this.mReceiver;
        context.registerReceiverForAllUsers(systemActionsBroadcastReceiver, systemActionsBroadcastReceiver.createIntentFilter(), "com.android.systemui.permission.SELF", (Handler) null);
        registerActions();
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        Locale locale = this.mContext.getResources().getConfiguration().getLocales().get(0);
        if (!locale.equals(this.mLocale)) {
            this.mLocale = locale;
            registerActions();
        }
    }

    private void registerActions() {
        RemoteAction createRemoteAction = createRemoteAction(17039589, "SYSTEM_ACTION_BACK");
        RemoteAction createRemoteAction2 = createRemoteAction(17039591, "SYSTEM_ACTION_HOME");
        RemoteAction createRemoteAction3 = createRemoteAction(17039598, "SYSTEM_ACTION_RECENTS");
        RemoteAction createRemoteAction4 = createRemoteAction(17039593, "SYSTEM_ACTION_NOTIFICATIONS");
        RemoteAction createRemoteAction5 = createRemoteAction(17039597, "SYSTEM_ACTION_QUICK_SETTINGS");
        RemoteAction createRemoteAction6 = createRemoteAction(17039596, "SYSTEM_ACTION_POWER_DIALOG");
        RemoteAction createRemoteAction7 = createRemoteAction(17039592, "SYSTEM_ACTION_LOCK_SCREEN");
        RemoteAction createRemoteAction8 = createRemoteAction(17039599, "SYSTEM_ACTION_TAKE_SCREENSHOT");
        RemoteAction createRemoteAction9 = createRemoteAction(17039590, "SYSTEM_ACTION_ACCESSIBILITY_SHORTCUT");
        this.mA11yManager.registerSystemAction(createRemoteAction, 1);
        this.mA11yManager.registerSystemAction(createRemoteAction2, 2);
        this.mA11yManager.registerSystemAction(createRemoteAction3, 3);
        this.mA11yManager.registerSystemAction(createRemoteAction4, 4);
        this.mA11yManager.registerSystemAction(createRemoteAction5, 5);
        this.mA11yManager.registerSystemAction(createRemoteAction6, 6);
        this.mA11yManager.registerSystemAction(createRemoteAction7, 8);
        this.mA11yManager.registerSystemAction(createRemoteAction8, 9);
        this.mA11yManager.registerSystemAction(createRemoteAction9, 13);
    }

    public void register(int i) {
        String str;
        int i2;
        switch (i) {
            case 1:
                i2 = 17039589;
                str = "SYSTEM_ACTION_BACK";
                break;
            case 2:
                i2 = 17039591;
                str = "SYSTEM_ACTION_HOME";
                break;
            case 3:
                i2 = 17039598;
                str = "SYSTEM_ACTION_RECENTS";
                break;
            case 4:
                i2 = 17039593;
                str = "SYSTEM_ACTION_NOTIFICATIONS";
                break;
            case 5:
                i2 = 17039597;
                str = "SYSTEM_ACTION_QUICK_SETTINGS";
                break;
            case 6:
                i2 = 17039596;
                str = "SYSTEM_ACTION_POWER_DIALOG";
                break;
            case 8:
                i2 = 17039592;
                str = "SYSTEM_ACTION_LOCK_SCREEN";
                break;
            case 9:
                i2 = 17039599;
                str = "SYSTEM_ACTION_TAKE_SCREENSHOT";
                break;
            case 11:
                i2 = 17039595;
                str = "SYSTEM_ACTION_ACCESSIBILITY_BUTTON";
                break;
            case 12:
                i2 = 17039594;
                str = "SYSTEM_ACTION_ACCESSIBILITY_BUTTON_MENU";
                break;
            case 13:
                i2 = 17039590;
                str = "SYSTEM_ACTION_ACCESSIBILITY_SHORTCUT";
                break;
            default:
                return;
        }
        this.mA11yManager.registerSystemAction(createRemoteAction(i2, str), i);
    }

    private RemoteAction createRemoteAction(int i, String str) {
        return new RemoteAction(Icon.createWithResource(this.mContext, 17301684), this.mContext.getString(i), this.mContext.getString(i), this.mReceiver.createPendingIntent(this.mContext, str));
    }

    public void unregister(int i) {
        this.mA11yManager.unregisterSystemAction(i);
    }

    /* access modifiers changed from: private */
    public void handleBack() {
        sendDownAndUpKeyEvents(4);
    }

    /* access modifiers changed from: private */
    public void handleHome() {
        sendDownAndUpKeyEvents(3);
    }

    private void sendDownAndUpKeyEvents(int i) {
        long uptimeMillis = SystemClock.uptimeMillis();
        int i2 = i;
        long j = uptimeMillis;
        sendKeyEventIdentityCleared(i2, 0, j, uptimeMillis);
        sendKeyEventIdentityCleared(i2, 1, j, SystemClock.uptimeMillis());
    }

    private void sendKeyEventIdentityCleared(int i, int i2, long j, long j2) {
        KeyEvent obtain = KeyEvent.obtain(j, j2, i2, i, 0, 0, -1, 0, 8, 257, (String) null);
        InputManager.getInstance().injectInputEvent(obtain, 0);
        obtain.recycle();
    }

    /* access modifiers changed from: private */
    public void handleRecents() {
        this.mRecents.toggleRecentApps();
    }

    /* access modifiers changed from: private */
    public void handleNotifications() {
        this.mStatusBar.animateExpandNotificationsPanel();
    }

    /* access modifiers changed from: private */
    public void handleQuickSettings() {
        this.mStatusBar.animateExpandSettingsPanel((String) null);
    }

    /* access modifiers changed from: private */
    public void handlePowerDialog() {
        try {
            WindowManagerGlobal.getWindowManagerService().showGlobalActions();
        } catch (RemoteException unused) {
            Log.e("SystemActions", "failed to display power dialog.");
        }
    }

    /* access modifiers changed from: private */
    public void handleLockScreen() {
        IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
        ((PowerManager) this.mContext.getSystemService(PowerManager.class)).goToSleep(SystemClock.uptimeMillis(), 7, 0);
        try {
            windowManagerService.lockNow((Bundle) null);
        } catch (RemoteException unused) {
            Log.e("SystemActions", "failed to lock screen.");
        }
    }

    /* access modifiers changed from: private */
    public void handleTakeScreenshot() {
        new ScreenshotHelper(this.mContext).takeScreenshot(1, true, true, 0, new Handler(Looper.getMainLooper()), (Consumer) null);
    }

    /* access modifiers changed from: private */
    public void handleAccessibilityButton() {
        AccessibilityManager.getInstance(this.mContext).notifyAccessibilityButtonClicked(0);
    }

    /* access modifiers changed from: private */
    public void handleAccessibilityButtonChooser() {
        Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
        intent.addFlags(268468224);
        intent.setClassName("android", AccessibilityButtonChooserActivity.class.getName());
        this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    /* access modifiers changed from: private */
    public void handleAccessibilityShortcut() {
        this.mA11yManager.performAccessibilityShortcut();
    }

    private class SystemActionsBroadcastReceiver extends BroadcastReceiver {
        private SystemActionsBroadcastReceiver() {
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public android.app.PendingIntent createPendingIntent(android.content.Context r2, java.lang.String r3) {
            /*
                r1 = this;
                int r1 = r3.hashCode()
                r0 = 0
                switch(r1) {
                    case -1103811776: goto L_0x0072;
                    case -1103619272: goto L_0x0068;
                    case -720484549: goto L_0x005e;
                    case -535129457: goto L_0x0054;
                    case -181386672: goto L_0x0049;
                    case -153384569: goto L_0x003f;
                    case 42571871: goto L_0x0035;
                    case 526987266: goto L_0x002a;
                    case 1245940668: goto L_0x001f;
                    case 1579999269: goto L_0x0015;
                    case 1668921710: goto L_0x000a;
                    default: goto L_0x0008;
                }
            L_0x0008:
                goto L_0x007c
            L_0x000a:
                java.lang.String r1 = "SYSTEM_ACTION_QUICK_SETTINGS"
                boolean r1 = r3.equals(r1)
                if (r1 == 0) goto L_0x007c
                r1 = 4
                goto L_0x007d
            L_0x0015:
                java.lang.String r1 = "SYSTEM_ACTION_TAKE_SCREENSHOT"
                boolean r1 = r3.equals(r1)
                if (r1 == 0) goto L_0x007c
                r1 = 7
                goto L_0x007d
            L_0x001f:
                java.lang.String r1 = "SYSTEM_ACTION_ACCESSIBILITY_BUTTON"
                boolean r1 = r3.equals(r1)
                if (r1 == 0) goto L_0x007c
                r1 = 8
                goto L_0x007d
            L_0x002a:
                java.lang.String r1 = "SYSTEM_ACTION_ACCESSIBILITY_BUTTON_MENU"
                boolean r1 = r3.equals(r1)
                if (r1 == 0) goto L_0x007c
                r1 = 9
                goto L_0x007d
            L_0x0035:
                java.lang.String r1 = "SYSTEM_ACTION_RECENTS"
                boolean r1 = r3.equals(r1)
                if (r1 == 0) goto L_0x007c
                r1 = 2
                goto L_0x007d
            L_0x003f:
                java.lang.String r1 = "SYSTEM_ACTION_LOCK_SCREEN"
                boolean r1 = r3.equals(r1)
                if (r1 == 0) goto L_0x007c
                r1 = 6
                goto L_0x007d
            L_0x0049:
                java.lang.String r1 = "SYSTEM_ACTION_ACCESSIBILITY_SHORTCUT"
                boolean r1 = r3.equals(r1)
                if (r1 == 0) goto L_0x007c
                r1 = 10
                goto L_0x007d
            L_0x0054:
                java.lang.String r1 = "SYSTEM_ACTION_NOTIFICATIONS"
                boolean r1 = r3.equals(r1)
                if (r1 == 0) goto L_0x007c
                r1 = 3
                goto L_0x007d
            L_0x005e:
                java.lang.String r1 = "SYSTEM_ACTION_POWER_DIALOG"
                boolean r1 = r3.equals(r1)
                if (r1 == 0) goto L_0x007c
                r1 = 5
                goto L_0x007d
            L_0x0068:
                java.lang.String r1 = "SYSTEM_ACTION_HOME"
                boolean r1 = r3.equals(r1)
                if (r1 == 0) goto L_0x007c
                r1 = 1
                goto L_0x007d
            L_0x0072:
                java.lang.String r1 = "SYSTEM_ACTION_BACK"
                boolean r1 = r3.equals(r1)
                if (r1 == 0) goto L_0x007c
                r1 = r0
                goto L_0x007d
            L_0x007c:
                r1 = -1
            L_0x007d:
                switch(r1) {
                    case 0: goto L_0x0082;
                    case 1: goto L_0x0082;
                    case 2: goto L_0x0082;
                    case 3: goto L_0x0082;
                    case 4: goto L_0x0082;
                    case 5: goto L_0x0082;
                    case 6: goto L_0x0082;
                    case 7: goto L_0x0082;
                    case 8: goto L_0x0082;
                    case 9: goto L_0x0082;
                    case 10: goto L_0x0082;
                    default: goto L_0x0080;
                }
            L_0x0080:
                r1 = 0
                return r1
            L_0x0082:
                android.content.Intent r1 = new android.content.Intent
                r1.<init>(r3)
                java.lang.String r3 = r2.getPackageName()
                r1.setPackage(r3)
                android.app.PendingIntent r1 = android.app.PendingIntent.getBroadcast(r2, r0, r1, r0)
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.accessibility.SystemActions.SystemActionsBroadcastReceiver.createPendingIntent(android.content.Context, java.lang.String):android.app.PendingIntent");
        }

        /* access modifiers changed from: private */
        public IntentFilter createIntentFilter() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("SYSTEM_ACTION_BACK");
            intentFilter.addAction("SYSTEM_ACTION_HOME");
            intentFilter.addAction("SYSTEM_ACTION_RECENTS");
            intentFilter.addAction("SYSTEM_ACTION_NOTIFICATIONS");
            intentFilter.addAction("SYSTEM_ACTION_QUICK_SETTINGS");
            intentFilter.addAction("SYSTEM_ACTION_POWER_DIALOG");
            intentFilter.addAction("SYSTEM_ACTION_LOCK_SCREEN");
            intentFilter.addAction("SYSTEM_ACTION_TAKE_SCREENSHOT");
            intentFilter.addAction("SYSTEM_ACTION_ACCESSIBILITY_BUTTON");
            intentFilter.addAction("SYSTEM_ACTION_ACCESSIBILITY_BUTTON_MENU");
            intentFilter.addAction("SYSTEM_ACTION_ACCESSIBILITY_SHORTCUT");
            return intentFilter;
        }

        /* JADX WARNING: Can't fix incorrect switch cases order */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(android.content.Context r1, android.content.Intent r2) {
            /*
                r0 = this;
                java.lang.String r1 = r2.getAction()
                int r2 = r1.hashCode()
                switch(r2) {
                    case -1103811776: goto L_0x0075;
                    case -1103619272: goto L_0x006b;
                    case -720484549: goto L_0x0061;
                    case -535129457: goto L_0x0057;
                    case -181386672: goto L_0x004c;
                    case -153384569: goto L_0x0042;
                    case 42571871: goto L_0x0038;
                    case 526987266: goto L_0x002d;
                    case 1245940668: goto L_0x0022;
                    case 1579999269: goto L_0x0018;
                    case 1668921710: goto L_0x000d;
                    default: goto L_0x000b;
                }
            L_0x000b:
                goto L_0x007f
            L_0x000d:
                java.lang.String r2 = "SYSTEM_ACTION_QUICK_SETTINGS"
                boolean r1 = r1.equals(r2)
                if (r1 == 0) goto L_0x007f
                r1 = 4
                goto L_0x0080
            L_0x0018:
                java.lang.String r2 = "SYSTEM_ACTION_TAKE_SCREENSHOT"
                boolean r1 = r1.equals(r2)
                if (r1 == 0) goto L_0x007f
                r1 = 7
                goto L_0x0080
            L_0x0022:
                java.lang.String r2 = "SYSTEM_ACTION_ACCESSIBILITY_BUTTON"
                boolean r1 = r1.equals(r2)
                if (r1 == 0) goto L_0x007f
                r1 = 8
                goto L_0x0080
            L_0x002d:
                java.lang.String r2 = "SYSTEM_ACTION_ACCESSIBILITY_BUTTON_MENU"
                boolean r1 = r1.equals(r2)
                if (r1 == 0) goto L_0x007f
                r1 = 9
                goto L_0x0080
            L_0x0038:
                java.lang.String r2 = "SYSTEM_ACTION_RECENTS"
                boolean r1 = r1.equals(r2)
                if (r1 == 0) goto L_0x007f
                r1 = 2
                goto L_0x0080
            L_0x0042:
                java.lang.String r2 = "SYSTEM_ACTION_LOCK_SCREEN"
                boolean r1 = r1.equals(r2)
                if (r1 == 0) goto L_0x007f
                r1 = 6
                goto L_0x0080
            L_0x004c:
                java.lang.String r2 = "SYSTEM_ACTION_ACCESSIBILITY_SHORTCUT"
                boolean r1 = r1.equals(r2)
                if (r1 == 0) goto L_0x007f
                r1 = 10
                goto L_0x0080
            L_0x0057:
                java.lang.String r2 = "SYSTEM_ACTION_NOTIFICATIONS"
                boolean r1 = r1.equals(r2)
                if (r1 == 0) goto L_0x007f
                r1 = 3
                goto L_0x0080
            L_0x0061:
                java.lang.String r2 = "SYSTEM_ACTION_POWER_DIALOG"
                boolean r1 = r1.equals(r2)
                if (r1 == 0) goto L_0x007f
                r1 = 5
                goto L_0x0080
            L_0x006b:
                java.lang.String r2 = "SYSTEM_ACTION_HOME"
                boolean r1 = r1.equals(r2)
                if (r1 == 0) goto L_0x007f
                r1 = 1
                goto L_0x0080
            L_0x0075:
                java.lang.String r2 = "SYSTEM_ACTION_BACK"
                boolean r1 = r1.equals(r2)
                if (r1 == 0) goto L_0x007f
                r1 = 0
                goto L_0x0080
            L_0x007f:
                r1 = -1
            L_0x0080:
                switch(r1) {
                    case 0: goto L_0x00c0;
                    case 1: goto L_0x00ba;
                    case 2: goto L_0x00b4;
                    case 3: goto L_0x00ae;
                    case 4: goto L_0x00a8;
                    case 5: goto L_0x00a2;
                    case 6: goto L_0x009c;
                    case 7: goto L_0x0096;
                    case 8: goto L_0x0090;
                    case 9: goto L_0x008a;
                    case 10: goto L_0x0084;
                    default: goto L_0x0083;
                }
            L_0x0083:
                goto L_0x00c5
            L_0x0084:
                com.android.systemui.accessibility.SystemActions r0 = com.android.systemui.accessibility.SystemActions.this
                r0.handleAccessibilityShortcut()
                goto L_0x00c5
            L_0x008a:
                com.android.systemui.accessibility.SystemActions r0 = com.android.systemui.accessibility.SystemActions.this
                r0.handleAccessibilityButtonChooser()
                goto L_0x00c5
            L_0x0090:
                com.android.systemui.accessibility.SystemActions r0 = com.android.systemui.accessibility.SystemActions.this
                r0.handleAccessibilityButton()
                goto L_0x00c5
            L_0x0096:
                com.android.systemui.accessibility.SystemActions r0 = com.android.systemui.accessibility.SystemActions.this
                r0.handleTakeScreenshot()
                goto L_0x00c5
            L_0x009c:
                com.android.systemui.accessibility.SystemActions r0 = com.android.systemui.accessibility.SystemActions.this
                r0.handleLockScreen()
                goto L_0x00c5
            L_0x00a2:
                com.android.systemui.accessibility.SystemActions r0 = com.android.systemui.accessibility.SystemActions.this
                r0.handlePowerDialog()
                goto L_0x00c5
            L_0x00a8:
                com.android.systemui.accessibility.SystemActions r0 = com.android.systemui.accessibility.SystemActions.this
                r0.handleQuickSettings()
                goto L_0x00c5
            L_0x00ae:
                com.android.systemui.accessibility.SystemActions r0 = com.android.systemui.accessibility.SystemActions.this
                r0.handleNotifications()
                goto L_0x00c5
            L_0x00b4:
                com.android.systemui.accessibility.SystemActions r0 = com.android.systemui.accessibility.SystemActions.this
                r0.handleRecents()
                goto L_0x00c5
            L_0x00ba:
                com.android.systemui.accessibility.SystemActions r0 = com.android.systemui.accessibility.SystemActions.this
                r0.handleHome()
                goto L_0x00c5
            L_0x00c0:
                com.android.systemui.accessibility.SystemActions r0 = com.android.systemui.accessibility.SystemActions.this
                r0.handleBack()
            L_0x00c5:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.accessibility.SystemActions.SystemActionsBroadcastReceiver.onReceive(android.content.Context, android.content.Intent):void");
        }
    }
}

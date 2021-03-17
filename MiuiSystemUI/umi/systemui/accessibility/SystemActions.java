package com.android.systemui.accessibility;

import android.app.PendingIntent;
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

    @Override // com.android.systemui.SystemUI
    public void start() {
        Context context = this.mContext;
        SystemActionsBroadcastReceiver systemActionsBroadcastReceiver = this.mReceiver;
        context.registerReceiverForAllUsers(systemActionsBroadcastReceiver, systemActionsBroadcastReceiver.createIntentFilter(), "com.android.systemui.permission.SELF", null);
        registerActions();
    }

    @Override // com.android.systemui.SystemUI
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
            case 7:
            case 10:
            default:
                return;
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
    /* access modifiers changed from: public */
    private void handleBack() {
        sendDownAndUpKeyEvents(4);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleHome() {
        sendDownAndUpKeyEvents(3);
    }

    private void sendDownAndUpKeyEvents(int i) {
        long uptimeMillis = SystemClock.uptimeMillis();
        sendKeyEventIdentityCleared(i, 0, uptimeMillis, uptimeMillis);
        sendKeyEventIdentityCleared(i, 1, uptimeMillis, SystemClock.uptimeMillis());
    }

    private void sendKeyEventIdentityCleared(int i, int i2, long j, long j2) {
        KeyEvent obtain = KeyEvent.obtain(j, j2, i2, i, 0, 0, -1, 0, 8, 257, null);
        InputManager.getInstance().injectInputEvent(obtain, 0);
        obtain.recycle();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRecents() {
        this.mRecents.toggleRecentApps();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNotifications() {
        this.mStatusBar.animateExpandNotificationsPanel();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleQuickSettings() {
        this.mStatusBar.animateExpandSettingsPanel(null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePowerDialog() {
        try {
            WindowManagerGlobal.getWindowManagerService().showGlobalActions();
        } catch (RemoteException unused) {
            Log.e("SystemActions", "failed to display power dialog.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleLockScreen() {
        IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
        ((PowerManager) this.mContext.getSystemService(PowerManager.class)).goToSleep(SystemClock.uptimeMillis(), 7, 0);
        try {
            windowManagerService.lockNow((Bundle) null);
        } catch (RemoteException unused) {
            Log.e("SystemActions", "failed to lock screen.");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleTakeScreenshot() {
        new ScreenshotHelper(this.mContext).takeScreenshot(1, true, true, 0, new Handler(Looper.getMainLooper()), (Consumer) null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAccessibilityButton() {
        AccessibilityManager.getInstance(this.mContext).notifyAccessibilityButtonClicked(0);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAccessibilityButtonChooser() {
        Intent intent = new Intent("com.android.internal.intent.action.CHOOSE_ACCESSIBILITY_BUTTON");
        intent.addFlags(268468224);
        intent.setClassName("android", AccessibilityButtonChooserActivity.class.getName());
        this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAccessibilityShortcut() {
        this.mA11yManager.performAccessibilityShortcut();
    }

    /* access modifiers changed from: private */
    public class SystemActionsBroadcastReceiver extends BroadcastReceiver {
        private SystemActionsBroadcastReceiver() {
        }

        /* access modifiers changed from: private */
        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        /* access modifiers changed from: public */
        private PendingIntent createPendingIntent(Context context, String str) {
            char c;
            switch (str.hashCode()) {
                case -1103811776:
                    if (str.equals("SYSTEM_ACTION_BACK")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1103619272:
                    if (str.equals("SYSTEM_ACTION_HOME")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -720484549:
                    if (str.equals("SYSTEM_ACTION_POWER_DIALOG")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -535129457:
                    if (str.equals("SYSTEM_ACTION_NOTIFICATIONS")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -181386672:
                    if (str.equals("SYSTEM_ACTION_ACCESSIBILITY_SHORTCUT")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case -153384569:
                    if (str.equals("SYSTEM_ACTION_LOCK_SCREEN")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 42571871:
                    if (str.equals("SYSTEM_ACTION_RECENTS")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 526987266:
                    if (str.equals("SYSTEM_ACTION_ACCESSIBILITY_BUTTON_MENU")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 1245940668:
                    if (str.equals("SYSTEM_ACTION_ACCESSIBILITY_BUTTON")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 1579999269:
                    if (str.equals("SYSTEM_ACTION_TAKE_SCREENSHOT")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 1668921710:
                    if (str.equals("SYSTEM_ACTION_QUICK_SETTINGS")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case '\b':
                case '\t':
                case '\n':
                    Intent intent = new Intent(str);
                    intent.setPackage(context.getPackageName());
                    return PendingIntent.getBroadcast(context, 0, intent, 0);
                default:
                    return null;
            }
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private IntentFilter createIntentFilter() {
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

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        public void onReceive(Context context, Intent intent) {
            char c;
            String action = intent.getAction();
            switch (action.hashCode()) {
                case -1103811776:
                    if (action.equals("SYSTEM_ACTION_BACK")) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                case -1103619272:
                    if (action.equals("SYSTEM_ACTION_HOME")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -720484549:
                    if (action.equals("SYSTEM_ACTION_POWER_DIALOG")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -535129457:
                    if (action.equals("SYSTEM_ACTION_NOTIFICATIONS")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -181386672:
                    if (action.equals("SYSTEM_ACTION_ACCESSIBILITY_SHORTCUT")) {
                        c = '\n';
                        break;
                    }
                    c = 65535;
                    break;
                case -153384569:
                    if (action.equals("SYSTEM_ACTION_LOCK_SCREEN")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 42571871:
                    if (action.equals("SYSTEM_ACTION_RECENTS")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case 526987266:
                    if (action.equals("SYSTEM_ACTION_ACCESSIBILITY_BUTTON_MENU")) {
                        c = '\t';
                        break;
                    }
                    c = 65535;
                    break;
                case 1245940668:
                    if (action.equals("SYSTEM_ACTION_ACCESSIBILITY_BUTTON")) {
                        c = '\b';
                        break;
                    }
                    c = 65535;
                    break;
                case 1579999269:
                    if (action.equals("SYSTEM_ACTION_TAKE_SCREENSHOT")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case 1668921710:
                    if (action.equals("SYSTEM_ACTION_QUICK_SETTINGS")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    SystemActions.this.handleBack();
                    return;
                case 1:
                    SystemActions.this.handleHome();
                    return;
                case 2:
                    SystemActions.this.handleRecents();
                    return;
                case 3:
                    SystemActions.this.handleNotifications();
                    return;
                case 4:
                    SystemActions.this.handleQuickSettings();
                    return;
                case 5:
                    SystemActions.this.handlePowerDialog();
                    return;
                case 6:
                    SystemActions.this.handleLockScreen();
                    return;
                case 7:
                    SystemActions.this.handleTakeScreenshot();
                    return;
                case '\b':
                    SystemActions.this.handleAccessibilityButton();
                    return;
                case '\t':
                    SystemActions.this.handleAccessibilityButtonChooser();
                    return;
                case '\n':
                    SystemActions.this.handleAccessibilityShortcut();
                    return;
                default:
                    return;
            }
        }
    }
}

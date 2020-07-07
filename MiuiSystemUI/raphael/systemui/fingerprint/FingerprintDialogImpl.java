package com.android.systemui.fingerprint;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.CommandQueue;

public class FingerprintDialogImpl extends SystemUI implements CommandQueue.Callbacks {
    public void addQsTile(ComponentName componentName) {
    }

    public void animateCollapsePanels(int i) {
    }

    public void animateExpandNotificationsPanel() {
    }

    public void animateExpandSettingsPanel(String str) {
    }

    public void appTransitionCancelled() {
    }

    public void appTransitionFinished() {
    }

    public void appTransitionPending(boolean z) {
    }

    public void appTransitionStarting(long j, long j2, boolean z) {
    }

    public void cancelPreloadRecentApps() {
    }

    public void clickTile(ComponentName componentName) {
    }

    public void disable(int i, int i2, boolean z) {
    }

    public void dismissKeyboardShortcutsMenu() {
    }

    public void handleShowGlobalActionsMenu() {
    }

    public void handleSystemNavigationKey(int i) {
    }

    public void hideFingerprintDialog() {
    }

    public void hideRecentApps(boolean z, boolean z2) {
    }

    public void onFingerprintAuthenticated() {
    }

    public void onFingerprintError(String str) {
    }

    public void onFingerprintHelp(String str) {
    }

    public void preloadRecentApps() {
    }

    public void remQsTile(ComponentName componentName) {
    }

    public void removeIcon(String str) {
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
    }

    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
    }

    public void setStatus(int i, String str, Bundle bundle) {
    }

    public void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
    }

    public void setWindowState(int i, int i2) {
    }

    public void showAssistDisclosure() {
    }

    public void showFingerprintDialog(SomeArgs someArgs) {
    }

    public void showPictureInPictureMenu() {
    }

    public void showRecentApps(boolean z, boolean z2) {
    }

    public void showScreenPinningRequest(int i) {
    }

    public void start() {
    }

    public void startAssist(Bundle bundle) {
    }

    public void toggleKeyboardShortcutsMenu(int i) {
    }

    public void toggleRecentApps() {
    }

    public void toggleSplitScreen() {
    }

    public void topAppWindowChanged(boolean z) {
    }
}

package com.android.systemui.util;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.CommandQueue;

public class DisableStateTracker implements CommandQueue.Callbacks, View.OnAttachStateChangeListener {
    private boolean mDisabled;
    private final int mMask1;
    private final int mMask2;
    private View mView;

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

    public DisableStateTracker(int i, int i2) {
        this.mMask1 = i;
        this.mMask2 = i2;
    }

    public void onViewAttachedToWindow(View view) {
        this.mView = view;
        ((CommandQueue) SystemUI.getComponent(view.getContext(), CommandQueue.class)).addCallbacks(this);
    }

    public void onViewDetachedFromWindow(View view) {
        ((CommandQueue) SystemUI.getComponent(this.mView.getContext(), CommandQueue.class)).removeCallbacks(this);
        this.mView = null;
    }

    public void disable(int i, int i2, boolean z) {
        int i3 = 0;
        boolean z2 = ((i & this.mMask1) == 0 && (this.mMask2 & i2) == 0) ? false : true;
        if (z2 != this.mDisabled) {
            this.mDisabled = z2;
            View view = this.mView;
            if (z2) {
                i3 = 8;
            }
            view.setVisibility(i3);
        }
    }
}

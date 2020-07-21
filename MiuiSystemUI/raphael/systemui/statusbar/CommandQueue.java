package com.android.systemui.statusbar;

import android.content.ComponentName;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.SystemUI;
import java.util.ArrayList;

public class CommandQueue extends CompatibilityCommandQueue {
    /* access modifiers changed from: private */
    public ArrayList<Callbacks> mCallbacks = new ArrayList<>();
    private int mDisable1;
    private int mDisable2;
    /* access modifiers changed from: private */
    public Handler mHandler = new H(Looper.getMainLooper());
    private final Object mLock = new Object();

    public interface Callbacks {
        void addQsTile(ComponentName componentName) {
        }

        void animateCollapsePanels(int i) {
        }

        void animateExpandNotificationsPanel() {
        }

        void animateExpandSettingsPanel(String str) {
        }

        void appTransitionCancelled() {
        }

        void appTransitionFinished() {
        }

        void appTransitionPending(boolean z) {
        }

        void appTransitionStarting(long j, long j2, boolean z) {
        }

        void cancelPreloadRecentApps() {
        }

        void clickTile(ComponentName componentName) {
        }

        void disable(int i, int i2, boolean z) {
        }

        void dismissKeyboardShortcutsMenu() {
        }

        void handleShowGlobalActionsMenu() {
        }

        void handleSystemNavigationKey(int i) {
        }

        void hideBiometricDialog() {
        }

        void hideFingerprintDialog() {
        }

        void hideRecentApps(boolean z, boolean z2) {
        }

        void hideToast(String str, IBinder iBinder) {
        }

        void onBiometricAuthenticated(boolean z) {
        }

        void onBiometricError(SomeArgs someArgs) {
        }

        void onBiometricHelp(String str) {
        }

        void onFingerprintAuthenticated() {
        }

        void onFingerprintError(String str) {
        }

        void onFingerprintHelp(String str) {
        }

        void preloadRecentApps() {
        }

        void remQsTile(ComponentName componentName) {
        }

        void removeIcon(String str) {
        }

        void setIcon(String str, StatusBarIcon statusBarIcon) {
        }

        void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
        }

        void setStatus(int i, String str, Bundle bundle) {
        }

        void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
        }

        void setWindowState(int i, int i2) {
        }

        void showAssistDisclosure() {
        }

        void showBiometricDialog(SomeArgs someArgs) {
        }

        void showFingerprintDialog(SomeArgs someArgs) {
        }

        void showPictureInPictureMenu() {
        }

        void showRecentApps(boolean z, boolean z2) {
        }

        void showScreenPinningRequest(int i) {
        }

        void showToast(SomeArgs someArgs) {
        }

        void startAssist(Bundle bundle) {
        }

        void toggleKeyboardShortcutsMenu(int i) {
        }

        void toggleRecentApps() {
        }

        void toggleSplitScreen() {
        }

        void topAppWindowChanged(boolean z) {
        }
    }

    protected CommandQueue() {
    }

    public void addCallbacks(Callbacks callbacks) {
        if (!this.mCallbacks.contains(callbacks)) {
            this.mCallbacks.add(callbacks);
        }
        callbacks.disable(this.mDisable1, this.mDisable2, false);
    }

    public void removeCallbacks(Callbacks callbacks) {
        this.mCallbacks.remove(callbacks);
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(65536, 1, 0, new Pair(str, statusBarIcon)).sendToTarget();
        }
    }

    public void removeIcon(String str) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(65536, 2, 0, str).sendToTarget();
        }
    }

    public void disable(int i, int i2, boolean z) {
        synchronized (this.mLock) {
            this.mDisable1 = i;
            this.mDisable2 = i2;
            this.mHandler.removeMessages(131072);
            Message obtainMessage = this.mHandler.obtainMessage(131072, i, i2, Boolean.valueOf(z));
            if (Looper.myLooper() == this.mHandler.getLooper()) {
                this.mHandler.handleMessage(obtainMessage);
                obtainMessage.recycle();
            } else {
                obtainMessage.sendToTarget();
            }
        }
    }

    public void disable(int i, int i2, int i3) {
        disable(i2, i3, true);
    }

    public void recomputeDisableFlags(boolean z) {
        if (!this.mHandler.hasMessages(131072)) {
            synchronized (this.mLock) {
                disable(this.mDisable1, this.mDisable2, z);
            }
            return;
        }
        Log.d("StatusBar", "give up recomputeDisableFlags");
    }

    public void animateExpandNotificationsPanel() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(196608);
            this.mHandler.sendEmptyMessage(196608);
        }
    }

    public void animateCollapsePanels() {
        animateCollapsePanels(0);
    }

    public void animateCollapsePanels(int i) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(262144);
            this.mHandler.obtainMessage(262144, i, 0).sendToTarget();
        }
    }

    public void togglePanel() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2293760);
            this.mHandler.obtainMessage(2293760, 0, 0).sendToTarget();
        }
    }

    public void animateExpandSettingsPanel(String str) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(327680);
            this.mHandler.obtainMessage(327680, str).sendToTarget();
        }
    }

    public void setSystemUiVisibility(int i, int i2, int i3, int i4, int i5, Rect rect, Rect rect2, boolean z) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i2;
            obtain.argi2 = i3;
            obtain.argi3 = i4;
            obtain.argi4 = i5;
            obtain.arg1 = rect;
            obtain.arg2 = rect2;
            this.mHandler.obtainMessage(393216, obtain).sendToTarget();
        }
    }

    public void topAppWindowChanged(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(458752);
            this.mHandler.obtainMessage(458752, z ? 1 : 0, 0, (Object) null).sendToTarget();
        }
    }

    public void topAppWindowChanged(int i, boolean z) {
        topAppWindowChanged(z);
    }

    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(524288);
            Message obtainMessage = this.mHandler.obtainMessage(524288, i, i2, iBinder);
            obtainMessage.getData().putBoolean("showImeSwitcherKey", z);
            obtainMessage.sendToTarget();
        }
    }

    public void setImeWindowStatus(int i, IBinder iBinder, int i2, int i3, boolean z) {
        setImeWindowStatus(iBinder, i2, i3, z);
    }

    public void showRecentApps(boolean z) {
        showRecentApps(z, false);
    }

    public void showRecentApps(boolean z, boolean z2) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(851968);
            this.mHandler.obtainMessage(851968, z ? 1 : 0, z2 ? 1 : 0, (Object) null).sendToTarget();
        }
    }

    public void hideRecentApps(boolean z, boolean z2) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(917504);
            this.mHandler.obtainMessage(917504, z ? 1 : 0, z2 ? 1 : 0, (Object) null).sendToTarget();
        }
    }

    public void toggleSplitScreen() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1966080);
            this.mHandler.obtainMessage(1966080, 0, 0, (Object) null).sendToTarget();
        }
    }

    public void toggleRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(589824);
            Message obtainMessage = this.mHandler.obtainMessage(589824, 0, 0, (Object) null);
            obtainMessage.setAsynchronous(true);
            obtainMessage.sendToTarget();
        }
    }

    public void preloadRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(655360);
            this.mHandler.obtainMessage(655360, 0, 0, (Object) null).sendToTarget();
        }
    }

    public void cancelPreloadRecentApps() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(720896);
            this.mHandler.obtainMessage(720896, 0, 0, (Object) null).sendToTarget();
        }
    }

    public void dismissKeyboardShortcutsMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2097152);
            this.mHandler.obtainMessage(2097152).sendToTarget();
        }
    }

    public void toggleKeyboardShortcutsMenu(int i) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1638400);
            this.mHandler.obtainMessage(1638400, i, 0).sendToTarget();
        }
    }

    public void showPictureInPictureMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1703936);
            this.mHandler.obtainMessage(1703936).sendToTarget();
        }
    }

    public void setStatus(int i, String str, Bundle bundle) {
        synchronized (this.mLock) {
            Bundle bundle2 = new Bundle();
            bundle2.putInt("what", i);
            bundle2.putString("action", str);
            bundle2.putParcelable("ext", bundle);
            this.mHandler.obtainMessage(6553600, 0, 0, bundle2).sendToTarget();
        }
    }

    public void setWindowState(int i, int i2) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(786432, i, i2, (Object) null).sendToTarget();
        }
    }

    public void setWindowState(int i, int i2, int i3) {
        setWindowState(i2, i3);
    }

    public void showScreenPinningRequest(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1179648, i, 0, (Object) null).sendToTarget();
        }
    }

    public void appTransitionPending(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1245184, z ? 1 : 0, 0).sendToTarget();
        }
    }

    public void appTransitionPending(int i) {
        appTransitionPending(false);
    }

    public void appTransitionCancelled() {
        synchronized (this.mLock) {
            this.mHandler.sendEmptyMessage(1310720);
        }
    }

    public void appTransitionCancelled(int i) {
        appTransitionCancelled();
    }

    public void appTransitionStarting(long j, long j2) {
        appTransitionStarting(j, j2, false);
    }

    public void appTransitionStarting(long j, long j2, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1376256, z ? 1 : 0, 0, Pair.create(Long.valueOf(j), Long.valueOf(j2))).sendToTarget();
        }
    }

    public void appTransitionStarting(int i, long j, long j2) {
        appTransitionStarting(j, j2);
    }

    public void appTransitionFinished() {
        synchronized (this.mLock) {
            this.mHandler.sendEmptyMessage(2031616);
        }
    }

    public void appTransitionFinished(int i) {
        appTransitionFinished();
    }

    public void showAssistDisclosure() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1441792);
            this.mHandler.obtainMessage(1441792).sendToTarget();
        }
    }

    public void startAssist(Bundle bundle) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1507328);
            this.mHandler.obtainMessage(1507328, bundle).sendToTarget();
        }
    }

    public void onCameraLaunchGestureDetected(int i) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(1572864);
            this.mHandler.obtainMessage(1572864, i, 0).sendToTarget();
        }
    }

    public void addQsTile(ComponentName componentName) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1769472, componentName).sendToTarget();
        }
    }

    public void remQsTile(ComponentName componentName) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1835008, componentName).sendToTarget();
        }
    }

    public void clickQsTile(ComponentName componentName) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1900544, componentName).sendToTarget();
        }
    }

    public void handleSystemKey(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2162688, i, 0).sendToTarget();
        }
    }

    public void showPinningEnterExitToast(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2949120, Boolean.valueOf(z)).sendToTarget();
        }
    }

    public void showPinningEscapeToast() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3014656).sendToTarget();
        }
    }

    public void showGlobalActionsMenu() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2228224);
            this.mHandler.obtainMessage(2228224).sendToTarget();
        }
    }

    public void setTopAppHidesStatusBar(boolean z) {
        this.mHandler.removeMessages(2424832);
        this.mHandler.obtainMessage(2424832, z ? 1 : 0, 0).sendToTarget();
    }

    public void showShutdownUi(boolean z, String str) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2359296);
            this.mHandler.obtainMessage(2359296, z ? 1 : 0, 0, str).sendToTarget();
        }
    }

    public void showWirelessChargingAnimation(int i) {
        this.mHandler.removeMessages(2883584);
        this.mHandler.obtainMessage(2883584, i, 0).sendToTarget();
    }

    public void onProposedRotationChanged(int i, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2490368);
            this.mHandler.obtainMessage(2490368, i, z ? 1 : 0, (Object) null).sendToTarget();
        }
    }

    public void showBiometricDialog(SomeArgs someArgs) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(13172736, someArgs).sendToTarget();
        }
    }

    public void onBiometricAuthenticated(boolean z, String str) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(13238272, Boolean.valueOf(z)).sendToTarget();
        }
    }

    public void onBiometricHelp(String str) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(13303808, str).sendToTarget();
        }
    }

    public void onBiometricError(SomeArgs someArgs) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(13369344, someArgs).sendToTarget();
        }
    }

    public void hideBiometricDialog() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(13434880).sendToTarget();
        }
    }

    private final class H extends Handler {
        private H(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:108:0x0433, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:109:0x0435, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).showScreenPinningRequest(r12.arg1);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x005a, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x005c, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).hideBiometricDialog();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:135:0x04ad, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:136:0x04af, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).setWindowState(r12.arg1, r12.arg2);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:138:0x04cf, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:139:0x04d1, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).cancelPreloadRecentApps();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:141:0x04ed, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:142:0x04ef, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).preloadRecentApps();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:144:0x050b, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:145:0x050d, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).toggleRecentApps();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:164:0x05ba, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:165:0x05bc, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).animateExpandSettingsPanel((java.lang.String) r12.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:167:0x05dc, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:168:0x05de, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).animateCollapsePanels(r12.arg1);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:170:0x05fc, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:171:0x05fe, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).animateExpandNotificationsPanel();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:173:0x061a, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:174:0x061c, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).disable(r12.arg1, r12.arg2, ((java.lang.Boolean) r12.obj).booleanValue());
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x009f, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x00a1, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).onBiometricHelp((java.lang.String) r12.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x00c1, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x00c3, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).onBiometricAuthenticated(((java.lang.Boolean) r12.obj).booleanValue());
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:248:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:249:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:250:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:252:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:253:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:254:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:255:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:256:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:258:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:259:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:260:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:261:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:262:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:263:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:264:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:265:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:266:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:267:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:268:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:269:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:271:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:273:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:276:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:277:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:278:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:279:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:282:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:283:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:284:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:285:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x0166, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x0168, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).handleSystemNavigationKey(r12.arg1);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:37:0x0186, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:38:0x0188, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).hideFingerprintDialog();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:40:0x01a4, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:41:0x01a6, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).onFingerprintError((java.lang.String) r12.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:43:0x01c6, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:44:0x01c8, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).onFingerprintHelp((java.lang.String) r12.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:0x01e8, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:47:0x01ea, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).onFingerprintAuthenticated();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:53:0x0249, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:54:0x024b, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).handleShowGlobalActionsMenu();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:56:0x0267, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x0269, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).dismissKeyboardShortcutsMenu();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:59:0x0285, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:60:0x0287, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).appTransitionFinished();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:62:0x02a3, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:63:0x02a5, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).toggleSplitScreen();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:65:0x02c1, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:66:0x02c3, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).clickTile((android.content.ComponentName) r12.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:68:0x02e3, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:69:0x02e5, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).remQsTile((android.content.ComponentName) r12.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:71:0x0305, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:72:0x0307, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).addQsTile((android.content.ComponentName) r12.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:74:0x0327, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:75:0x0329, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).showPictureInPictureMenu();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:77:0x0345, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:78:0x0347, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).toggleKeyboardShortcutsMenu(r12.arg1);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:80:0x0365, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:81:0x0367, code lost:
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:83:0x0374, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:84:0x0376, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).startAssist((android.os.Bundle) r12.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:86:0x0396, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:87:0x0398, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).showAssistDisclosure();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:97:0x03ef, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).size()) goto L_0x068e;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:98:0x03f1, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r11.this$0).get(r1)).appTransitionCancelled();
            r1 = r1 + 1;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r12) {
            /*
                r11 = this;
                int r0 = r12.what
                r1 = -65536(0xffffffffffff0000, float:NaN)
                r0 = r0 & r1
                r1 = 0
                r2 = 1
                switch(r0) {
                    case 65536: goto L_0x063a;
                    case 131072: goto L_0x0610;
                    case 196608: goto L_0x05f2;
                    case 262144: goto L_0x05d2;
                    case 327680: goto L_0x05b0;
                    case 393216: goto L_0x0576;
                    case 458752: goto L_0x0550;
                    case 524288: goto L_0x051f;
                    case 589824: goto L_0x0501;
                    case 655360: goto L_0x04e3;
                    case 720896: goto L_0x04c5;
                    case 786432: goto L_0x04a3;
                    case 851968: goto L_0x0476;
                    case 917504: goto L_0x0449;
                    case 1179648: goto L_0x0429;
                    case 1245184: goto L_0x0403;
                    case 1310720: goto L_0x03e5;
                    case 1376256: goto L_0x03aa;
                    case 1441792: goto L_0x038c;
                    case 1507328: goto L_0x036a;
                    case 1572864: goto L_0x035b;
                    case 1638400: goto L_0x033b;
                    case 1703936: goto L_0x031d;
                    case 1769472: goto L_0x02fb;
                    case 1835008: goto L_0x02d9;
                    case 1900544: goto L_0x02b7;
                    case 1966080: goto L_0x0299;
                    case 2031616: goto L_0x027b;
                    case 2097152: goto L_0x025d;
                    case 2162688: goto L_0x068e;
                    case 2228224: goto L_0x023f;
                    case 2293760: goto L_0x068e;
                    case 2359296: goto L_0x068e;
                    case 2424832: goto L_0x068e;
                    case 2490368: goto L_0x068e;
                    case 2555904: goto L_0x01fc;
                    case 2621440: goto L_0x01de;
                    case 2686976: goto L_0x01bc;
                    case 2752512: goto L_0x019a;
                    case 2818048: goto L_0x017c;
                    case 2883584: goto L_0x068e;
                    case 2949120: goto L_0x068e;
                    case 3014656: goto L_0x068e;
                    case 6488064: goto L_0x015c;
                    case 6553600: goto L_0x0125;
                    case 13172736: goto L_0x00dd;
                    case 13238272: goto L_0x00b7;
                    case 13303808: goto L_0x0095;
                    case 13369344: goto L_0x006e;
                    case 13434880: goto L_0x0050;
                    case 19726336: goto L_0x0032;
                    case 19791872: goto L_0x000c;
                    default: goto L_0x000a;
                }
            L_0x000a:
                goto L_0x068e
            L_0x000c:
                java.lang.Object r12 = r12.obj
                com.android.internal.os.SomeArgs r12 = (com.android.internal.os.SomeArgs) r12
                java.lang.Object r0 = r12.arg1
                java.lang.String r0 = (java.lang.String) r0
                java.lang.Object r12 = r12.arg2
                android.os.IBinder r12 = (android.os.IBinder) r12
                com.android.systemui.statusbar.CommandQueue r11 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r11 = r11.mCallbacks
                java.util.Iterator r11 = r11.iterator()
            L_0x0022:
                boolean r1 = r11.hasNext()
                if (r1 == 0) goto L_0x068e
                java.lang.Object r1 = r11.next()
                com.android.systemui.statusbar.CommandQueue$Callbacks r1 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r1
                r1.hideToast(r0, r12)
                goto L_0x0022
            L_0x0032:
                java.lang.Object r12 = r12.obj
                com.android.internal.os.SomeArgs r12 = (com.android.internal.os.SomeArgs) r12
                com.android.systemui.statusbar.CommandQueue r11 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r11 = r11.mCallbacks
                java.util.Iterator r11 = r11.iterator()
            L_0x0040:
                boolean r0 = r11.hasNext()
                if (r0 == 0) goto L_0x068e
                java.lang.Object r0 = r11.next()
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                r0.showToast(r12)
                goto L_0x0040
            L_0x0050:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.hideBiometricDialog()
                int r1 = r1 + 1
                goto L_0x0050
            L_0x006e:
                java.lang.Object r12 = r12.obj
                com.android.internal.os.SomeArgs r12 = (com.android.internal.os.SomeArgs) r12
            L_0x0072:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0090
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                r0.onBiometricError(r12)
                int r1 = r1 + 1
                goto L_0x0072
            L_0x0090:
                r12.recycle()
                goto L_0x068e
            L_0x0095:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r12.obj
                java.lang.String r2 = (java.lang.String) r2
                r0.onBiometricHelp(r2)
                int r1 = r1 + 1
                goto L_0x0095
            L_0x00b7:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r12.obj
                java.lang.Boolean r2 = (java.lang.Boolean) r2
                boolean r2 = r2.booleanValue()
                r0.onBiometricAuthenticated(r2)
                int r1 = r1 + 1
                goto L_0x00b7
            L_0x00dd:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                android.os.Handler r0 = r0.mHandler
                r2 = 13369344(0xcc0000, float:1.8734441E-38)
                r0.removeMessages(r2)
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                android.os.Handler r0 = r0.mHandler
                r2 = 13303808(0xcb0000, float:1.8642606E-38)
                r0.removeMessages(r2)
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                android.os.Handler r0 = r0.mHandler
                r2 = 13238272(0xca0000, float:1.855077E-38)
                r0.removeMessages(r2)
                java.lang.Object r12 = r12.obj
                com.android.internal.os.SomeArgs r12 = (com.android.internal.os.SomeArgs) r12
            L_0x0102:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0120
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                r0.showBiometricDialog(r12)
                int r1 = r1 + 1
                goto L_0x0102
            L_0x0120:
                r12.recycle()
                goto L_0x068e
            L_0x0125:
                java.lang.Object r12 = r12.obj
                android.os.Bundle r12 = (android.os.Bundle) r12
                java.lang.String r0 = "what"
                int r0 = r12.getInt(r0)
                java.lang.String r2 = "action"
                java.lang.String r2 = r12.getString(r2)
                java.lang.String r3 = "ext"
                android.os.Parcelable r12 = r12.getParcelable(r3)
                android.os.Bundle r12 = (android.os.Bundle) r12
            L_0x013e:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r1 >= r3) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                r3.setStatus(r0, r2, r12)
                int r1 = r1 + 1
                goto L_0x013e
            L_0x015c:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r12.arg1
                r0.handleSystemNavigationKey(r2)
                int r1 = r1 + 1
                goto L_0x015c
            L_0x017c:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.hideFingerprintDialog()
                int r1 = r1 + 1
                goto L_0x017c
            L_0x019a:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r12.obj
                java.lang.String r2 = (java.lang.String) r2
                r0.onFingerprintError(r2)
                int r1 = r1 + 1
                goto L_0x019a
            L_0x01bc:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r12.obj
                java.lang.String r2 = (java.lang.String) r2
                r0.onFingerprintHelp(r2)
                int r1 = r1 + 1
                goto L_0x01bc
            L_0x01de:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.onFingerprintAuthenticated()
                int r1 = r1 + 1
                goto L_0x01de
            L_0x01fc:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                android.os.Handler r0 = r0.mHandler
                r2 = 2752512(0x2a0000, float:3.857091E-39)
                r0.removeMessages(r2)
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                android.os.Handler r0 = r0.mHandler
                r2 = 2686976(0x290000, float:3.765255E-39)
                r0.removeMessages(r2)
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                android.os.Handler r0 = r0.mHandler
                r2 = 2621440(0x280000, float:3.67342E-39)
                r0.removeMessages(r2)
            L_0x021d:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r12.obj
                com.android.internal.os.SomeArgs r2 = (com.android.internal.os.SomeArgs) r2
                r0.showFingerprintDialog(r2)
                int r1 = r1 + 1
                goto L_0x021d
            L_0x023f:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.handleShowGlobalActionsMenu()
                int r1 = r1 + 1
                goto L_0x023f
            L_0x025d:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.dismissKeyboardShortcutsMenu()
                int r1 = r1 + 1
                goto L_0x025d
            L_0x027b:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.appTransitionFinished()
                int r1 = r1 + 1
                goto L_0x027b
            L_0x0299:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.toggleSplitScreen()
                int r1 = r1 + 1
                goto L_0x0299
            L_0x02b7:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r12.obj
                android.content.ComponentName r2 = (android.content.ComponentName) r2
                r0.clickTile(r2)
                int r1 = r1 + 1
                goto L_0x02b7
            L_0x02d9:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r12.obj
                android.content.ComponentName r2 = (android.content.ComponentName) r2
                r0.remQsTile(r2)
                int r1 = r1 + 1
                goto L_0x02d9
            L_0x02fb:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r12.obj
                android.content.ComponentName r2 = (android.content.ComponentName) r2
                r0.addQsTile(r2)
                int r1 = r1 + 1
                goto L_0x02fb
            L_0x031d:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.showPictureInPictureMenu()
                int r1 = r1 + 1
                goto L_0x031d
            L_0x033b:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r12.arg1
                r0.toggleKeyboardShortcutsMenu(r2)
                int r1 = r1 + 1
                goto L_0x033b
            L_0x035b:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                int r1 = r1 + 1
                goto L_0x035b
            L_0x036a:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r12.obj
                android.os.Bundle r2 = (android.os.Bundle) r2
                r0.startAssist(r2)
                int r1 = r1 + 1
                goto L_0x036a
            L_0x038c:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.showAssistDisclosure()
                int r1 = r1 + 1
                goto L_0x038c
            L_0x03aa:
                r0 = r1
            L_0x03ab:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x068e
                java.lang.Object r3 = r12.obj
                android.util.Pair r3 = (android.util.Pair) r3
                com.android.systemui.statusbar.CommandQueue r4 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r4 = r4.mCallbacks
                java.lang.Object r4 = r4.get(r0)
                r5 = r4
                com.android.systemui.statusbar.CommandQueue$Callbacks r5 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r5
                java.lang.Object r4 = r3.first
                java.lang.Long r4 = (java.lang.Long) r4
                long r6 = r4.longValue()
                java.lang.Object r3 = r3.second
                java.lang.Long r3 = (java.lang.Long) r3
                long r8 = r3.longValue()
                int r3 = r12.arg1
                if (r3 == 0) goto L_0x03de
                r10 = r2
                goto L_0x03df
            L_0x03de:
                r10 = r1
            L_0x03df:
                r5.appTransitionStarting(r6, r8, r10)
                int r0 = r0 + 1
                goto L_0x03ab
            L_0x03e5:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.appTransitionCancelled()
                int r1 = r1 + 1
                goto L_0x03e5
            L_0x0403:
                r0 = r1
            L_0x0404:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r12.arg1
                if (r4 == 0) goto L_0x0422
                r4 = r2
                goto L_0x0423
            L_0x0422:
                r4 = r1
            L_0x0423:
                r3.appTransitionPending(r4)
                int r0 = r0 + 1
                goto L_0x0404
            L_0x0429:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r12.arg1
                r0.showScreenPinningRequest(r2)
                int r1 = r1 + 1
                goto L_0x0429
            L_0x0449:
                r0 = r1
            L_0x044a:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r12.arg1
                if (r4 == 0) goto L_0x0468
                r4 = r2
                goto L_0x0469
            L_0x0468:
                r4 = r1
            L_0x0469:
                int r5 = r12.arg2
                if (r5 == 0) goto L_0x046f
                r5 = r2
                goto L_0x0470
            L_0x046f:
                r5 = r1
            L_0x0470:
                r3.hideRecentApps(r4, r5)
                int r0 = r0 + 1
                goto L_0x044a
            L_0x0476:
                r0 = r1
            L_0x0477:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r12.arg1
                if (r4 == 0) goto L_0x0495
                r4 = r2
                goto L_0x0496
            L_0x0495:
                r4 = r1
            L_0x0496:
                int r5 = r12.arg2
                if (r5 == 0) goto L_0x049c
                r5 = r2
                goto L_0x049d
            L_0x049c:
                r5 = r1
            L_0x049d:
                r3.showRecentApps(r4, r5)
                int r0 = r0 + 1
                goto L_0x0477
            L_0x04a3:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r12.arg1
                int r3 = r12.arg2
                r0.setWindowState(r2, r3)
                int r1 = r1 + 1
                goto L_0x04a3
            L_0x04c5:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.cancelPreloadRecentApps()
                int r1 = r1 + 1
                goto L_0x04c5
            L_0x04e3:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.preloadRecentApps()
                int r1 = r1 + 1
                goto L_0x04e3
            L_0x0501:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.toggleRecentApps()
                int r1 = r1 + 1
                goto L_0x0501
            L_0x051f:
                r0 = r1
            L_0x0520:
                com.android.systemui.statusbar.CommandQueue r2 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r2 = r2.mCallbacks
                int r2 = r2.size()
                if (r0 >= r2) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r2 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r2 = r2.mCallbacks
                java.lang.Object r2 = r2.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r2 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r2
                java.lang.Object r3 = r12.obj
                android.os.IBinder r3 = (android.os.IBinder) r3
                int r4 = r12.arg1
                int r5 = r12.arg2
                android.os.Bundle r6 = r12.getData()
                java.lang.String r7 = "showImeSwitcherKey"
                boolean r6 = r6.getBoolean(r7, r1)
                r2.setImeWindowStatus(r3, r4, r5, r6)
                int r0 = r0 + 1
                goto L_0x0520
            L_0x0550:
                r0 = r1
            L_0x0551:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r12.arg1
                if (r4 == 0) goto L_0x056f
                r4 = r2
                goto L_0x0570
            L_0x056f:
                r4 = r1
            L_0x0570:
                r3.topAppWindowChanged(r4)
                int r0 = r0 + 1
                goto L_0x0551
            L_0x0576:
                java.lang.Object r12 = r12.obj
                com.android.internal.os.SomeArgs r12 = (com.android.internal.os.SomeArgs) r12
            L_0x057a:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x05ab
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                r2 = r0
                com.android.systemui.statusbar.CommandQueue$Callbacks r2 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r2
                int r3 = r12.argi1
                int r4 = r12.argi2
                int r5 = r12.argi3
                int r6 = r12.argi4
                java.lang.Object r0 = r12.arg1
                r7 = r0
                android.graphics.Rect r7 = (android.graphics.Rect) r7
                java.lang.Object r0 = r12.arg2
                r8 = r0
                android.graphics.Rect r8 = (android.graphics.Rect) r8
                r2.setSystemUiVisibility(r3, r4, r5, r6, r7, r8)
                int r1 = r1 + 1
                goto L_0x057a
            L_0x05ab:
                r12.recycle()
                goto L_0x068e
            L_0x05b0:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r12.obj
                java.lang.String r2 = (java.lang.String) r2
                r0.animateExpandSettingsPanel(r2)
                int r1 = r1 + 1
                goto L_0x05b0
            L_0x05d2:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r12.arg1
                r0.animateCollapsePanels(r2)
                int r1 = r1 + 1
                goto L_0x05d2
            L_0x05f2:
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                int r12 = r12.size()
                if (r1 >= r12) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r12 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r12 = r12.mCallbacks
                java.lang.Object r12 = r12.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r12 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r12
                r12.animateExpandNotificationsPanel()
                int r1 = r1 + 1
                goto L_0x05f2
            L_0x0610:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r12.arg1
                int r3 = r12.arg2
                java.lang.Object r4 = r12.obj
                java.lang.Boolean r4 = (java.lang.Boolean) r4
                boolean r4 = r4.booleanValue()
                r0.disable(r2, r3, r4)
                int r1 = r1 + 1
                goto L_0x0610
            L_0x063a:
                int r0 = r12.arg1
                if (r0 == r2) goto L_0x0664
                r2 = 2
                if (r0 == r2) goto L_0x0642
                goto L_0x068e
            L_0x0642:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r12.obj
                java.lang.String r2 = (java.lang.String) r2
                r0.removeIcon(r2)
                int r1 = r1 + 1
                goto L_0x0642
            L_0x0664:
                java.lang.Object r12 = r12.obj
                android.util.Pair r12 = (android.util.Pair) r12
            L_0x0668:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x068e
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r12.first
                java.lang.String r2 = (java.lang.String) r2
                java.lang.Object r3 = r12.second
                com.android.internal.statusbar.StatusBarIcon r3 = (com.android.internal.statusbar.StatusBarIcon) r3
                r0.setIcon(r2, r3)
                int r1 = r1 + 1
                goto L_0x0668
            L_0x068e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.CommandQueue.H.handleMessage(android.os.Message):void");
        }
    }

    public static class CommandQueueStart extends SystemUI {
        public void start() {
            putComponent(CommandQueue.class, new CommandQueue());
        }
    }
}

package com.android.systemui.statusbar;

import android.app.ITransientNotificationCallback;
import android.content.ComponentName;
import android.content.Context;
import android.hardware.biometrics.IBiometricServiceReceiverInternal;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.IStatusBar;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.view.AppearanceRegion;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.CallbackController;
import com.android.systemui.tracing.ProtoTracer;
import java.util.ArrayList;

public class CommandQueue extends IStatusBar.Stub implements CallbackController<Callbacks>, DisplayManager.DisplayListener {
    /* access modifiers changed from: private */
    public ArrayList<Callbacks> mCallbacks = new ArrayList<>();
    private SparseArray<Pair<Integer, Integer>> mDisplayDisabled = new SparseArray<>();
    /* access modifiers changed from: private */
    public Handler mHandler = new H(Looper.getMainLooper());
    private int mLastUpdatedImeDisplayId = -1;
    private final Object mLock = new Object();
    private ProtoTracer mProtoTracer;

    public interface Callbacks {
        void abortTransient(int i, int[] iArr) {
        }

        void addQsTile(ComponentName componentName) {
        }

        void animateCollapsePanels(int i, boolean z) {
        }

        void animateExpandNotificationsPanel() {
        }

        void animateExpandSettingsPanel(String str) {
        }

        void appTransitionCancelled(int i) {
        }

        void appTransitionFinished(int i) {
        }

        void appTransitionPending(int i, boolean z) {
        }

        void appTransitionStarting(int i, long j, long j2, boolean z) {
        }

        void cancelPreloadRecentApps() {
        }

        void clickTile(ComponentName componentName) {
        }

        void disable(int i, int i2, int i3, boolean z) {
        }

        void dismissInattentiveSleepWarning(boolean z) {
        }

        void dismissKeyboardShortcutsMenu() {
        }

        void handleShowGlobalActionsMenu() {
        }

        void handleShowShutdownUi(boolean z, String str) {
        }

        void handleSystemKey(int i) {
        }

        void hideAuthenticationDialog() {
        }

        void hideRecentApps(boolean z, boolean z2) {
        }

        void hideToast(String str, IBinder iBinder) {
        }

        void onBiometricAuthenticated() {
        }

        void onBiometricError(int i, int i2, int i3) {
        }

        void onBiometricHelp(String str) {
        }

        void onCameraLaunchGestureDetected(int i) {
        }

        void onDisplayReady(int i) {
        }

        void onDisplayRemoved(int i) {
        }

        void onRecentsAnimationStateChanged(boolean z) {
        }

        void onRotationProposal(int i, boolean z) {
        }

        void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        }

        void onTracingStateChanged(boolean z) {
        }

        void preloadRecentApps() {
        }

        void remQsTile(ComponentName componentName) {
        }

        void removeIcon(String str) {
        }

        void setIcon(String str, StatusBarIcon statusBarIcon) {
        }

        void setImeWindowStatus(int i, IBinder iBinder, int i2, int i3, boolean z) {
        }

        void setStatus(int i, String str, Bundle bundle) {
        }

        void setTopAppHidesStatusBar(boolean z) {
        }

        void setWindowState(int i, int i2, int i3) {
        }

        void showAssistDisclosure() {
        }

        void showAuthenticationDialog(Bundle bundle, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, int i, boolean z, int i2, String str, long j, int i3) {
        }

        void showInattentiveSleepWarning() {
        }

        void showPictureInPictureMenu() {
        }

        void showPinningEnterExitToast(boolean z) {
        }

        void showPinningEscapeToast() {
        }

        void showRecentApps(boolean z) {
        }

        void showScreenPinningRequest(int i) {
        }

        void showToast(int i, String str, IBinder iBinder, CharSequence charSequence, IBinder iBinder2, int i2, ITransientNotificationCallback iTransientNotificationCallback) {
        }

        void showTransient(int i, int[] iArr) {
        }

        void showWirelessChargingAnimation(int i) {
        }

        void startAssist(Bundle bundle) {
        }

        void suppressAmbientDisplay(boolean z) {
        }

        void toggleKeyboardShortcutsMenu(int i) {
        }

        void togglePanel() {
        }

        void toggleRecentApps() {
        }

        void toggleSplitScreen() {
        }

        void topAppWindowChanged(int i, boolean z, boolean z2) {
        }
    }

    public void onDisplayAdded(int i) {
    }

    public void onDisplayChanged(int i) {
    }

    public void showWirelessChargingAnimation(int i) {
    }

    public CommandQueue(Context context, ProtoTracer protoTracer) {
        this.mProtoTracer = protoTracer;
        ((DisplayManager) context.getSystemService(DisplayManager.class)).registerDisplayListener(this, this.mHandler);
        setDisabled(0, 0, 0);
    }

    public void onDisplayRemoved(int i) {
        synchronized (this.mLock) {
            this.mDisplayDisabled.remove(i);
        }
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            this.mCallbacks.get(size).onDisplayRemoved(i);
        }
    }

    public boolean panelsEnabled() {
        int disabled1 = getDisabled1(0);
        int disabled2 = getDisabled2(0);
        if ((disabled1 & 65536) == 0 && (disabled2 & 4) == 0 && !StatusBar.ONLY_CORE_APPS) {
            return true;
        }
        return false;
    }

    public void addCallback(Callbacks callbacks) {
        if (!this.mCallbacks.contains(callbacks)) {
            this.mCallbacks.add(callbacks);
        }
        for (int i = 0; i < this.mDisplayDisabled.size(); i++) {
            int keyAt = this.mDisplayDisabled.keyAt(i);
            callbacks.disable(keyAt, getDisabled1(keyAt), getDisabled2(keyAt), false);
        }
    }

    public void removeCallback(Callbacks callbacks) {
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

    public void disable(int i, int i2, int i3, boolean z) {
        synchronized (this.mLock) {
            setDisabled(i, i2, i3);
            this.mHandler.removeMessages(131072);
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.argi2 = i2;
            obtain.argi3 = i3;
            obtain.argi4 = z ? 1 : 0;
            Message obtainMessage = this.mHandler.obtainMessage(131072, obtain);
            if (Looper.myLooper() == this.mHandler.getLooper()) {
                this.mHandler.handleMessage(obtainMessage);
                obtainMessage.recycle();
            } else {
                obtainMessage.sendToTarget();
            }
        }
    }

    public void disable(int i, int i2, int i3) {
        disable(i, i2, i3, true);
    }

    public void recomputeDisableFlags(int i, boolean z) {
        disable(i, getDisabled1(i), getDisabled2(i), z);
    }

    private void setDisabled(int i, int i2, int i3) {
        this.mDisplayDisabled.put(i, new Pair(Integer.valueOf(i2), Integer.valueOf(i3)));
    }

    private int getDisabled1(int i) {
        return ((Integer) getDisabled(i).first).intValue();
    }

    private int getDisabled2(int i) {
        return ((Integer) getDisabled(i).second).intValue();
    }

    private Pair<Integer, Integer> getDisabled(int i) {
        Pair<Integer, Integer> pair = this.mDisplayDisabled.get(i);
        if (pair != null) {
            return pair;
        }
        Pair<Integer, Integer> pair2 = new Pair<>(0, 0);
        this.mDisplayDisabled.put(i, pair2);
        return pair2;
    }

    public void animateExpandNotificationsPanel() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(196608);
            this.mHandler.sendEmptyMessage(196608);
        }
    }

    public void animateCollapsePanels() {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(262144);
            this.mHandler.obtainMessage(262144, 0, 0).sendToTarget();
        }
    }

    public void animateCollapsePanels(int i, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(262144);
            this.mHandler.obtainMessage(262144, i, z ? 1 : 0).sendToTarget();
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

    public void topAppWindowChanged(int i, boolean z, boolean z2) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            int i2 = 1;
            obtain.argi2 = z ? 1 : 0;
            if (!z2) {
                i2 = 0;
            }
            obtain.argi3 = i2;
            this.mHandler.obtainMessage(3276800, obtain).sendToTarget();
        }
    }

    public void setImeWindowStatus(int i, IBinder iBinder, int i2, int i3, boolean z, boolean z2) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(524288);
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.argi2 = i2;
            obtain.argi3 = i3;
            int i4 = 1;
            obtain.argi4 = z ? 1 : 0;
            if (!z2) {
                i4 = 0;
            }
            obtain.argi5 = i4;
            obtain.arg1 = iBinder;
            this.mHandler.obtainMessage(524288, obtain).sendToTarget();
        }
    }

    public void showRecentApps(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(851968);
            this.mHandler.obtainMessage(851968, z ? 1 : 0, 0, (Object) null).sendToTarget();
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

    public void setWindowState(int i, int i2, int i3) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(786432, i, i2, Integer.valueOf(i3)).sendToTarget();
        }
    }

    public void showScreenPinningRequest(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1179648, i, 0, (Object) null).sendToTarget();
        }
    }

    public void appTransitionPending(int i) {
        appTransitionPending(i, false);
    }

    public void appTransitionPending(int i, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1245184, i, z ? 1 : 0).sendToTarget();
        }
    }

    public void appTransitionCancelled(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(1310720, i, 0).sendToTarget();
        }
    }

    public void appTransitionStarting(int i, long j, long j2) {
        appTransitionStarting(i, j, j2, false);
    }

    public void appTransitionStarting(int i, long j, long j2, boolean z) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.argi2 = z ? 1 : 0;
            obtain.arg1 = Long.valueOf(j);
            obtain.arg2 = Long.valueOf(j2);
            this.mHandler.obtainMessage(1376256, obtain).sendToTarget();
        }
    }

    public void appTransitionFinished(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2031616, i, 0).sendToTarget();
        }
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

    public void onProposedRotationChanged(int i, boolean z) {
        synchronized (this.mLock) {
            this.mHandler.removeMessages(2490368);
            this.mHandler.obtainMessage(2490368, i, z ? 1 : 0, (Object) null).sendToTarget();
        }
    }

    public void showAuthenticationDialog(Bundle bundle, IBiometricServiceReceiverInternal iBiometricServiceReceiverInternal, int i, boolean z, int i2, String str, long j, int i3) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = bundle;
            obtain.arg2 = iBiometricServiceReceiverInternal;
            obtain.argi1 = i;
            obtain.arg3 = Boolean.valueOf(z);
            obtain.argi2 = i2;
            obtain.arg4 = str;
            obtain.arg5 = Long.valueOf(j);
            obtain.argi3 = i3;
            this.mHandler.obtainMessage(2555904, obtain).sendToTarget();
        }
    }

    public void showToast(int i, String str, IBinder iBinder, CharSequence charSequence, IBinder iBinder2, int i2, ITransientNotificationCallback iTransientNotificationCallback) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = str;
            obtain.arg2 = iBinder;
            obtain.arg3 = charSequence;
            obtain.arg4 = iBinder2;
            obtain.arg5 = iTransientNotificationCallback;
            obtain.argi1 = i;
            obtain.argi2 = i2;
            this.mHandler.obtainMessage(3473408, obtain).sendToTarget();
        }
    }

    public void hideToast(String str, IBinder iBinder) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.arg1 = str;
            obtain.arg2 = iBinder;
            this.mHandler.obtainMessage(3538944, obtain).sendToTarget();
        }
    }

    public void onBiometricAuthenticated() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2621440).sendToTarget();
        }
    }

    public void onBiometricHelp(String str) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2686976, str).sendToTarget();
        }
    }

    public void onBiometricError(int i, int i2, int i3) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.argi2 = i2;
            obtain.argi3 = i3;
            this.mHandler.obtainMessage(2752512, obtain).sendToTarget();
        }
    }

    public void hideAuthenticationDialog() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(2818048).sendToTarget();
        }
    }

    public void onDisplayReady(int i) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(458752, i, 0).sendToTarget();
        }
    }

    public void onRecentsAnimationStateChanged(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3080192, z ? 1 : 0, 0).sendToTarget();
        }
    }

    public void showInattentiveSleepWarning() {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3342336).sendToTarget();
        }
    }

    public void dismissInattentiveSleepWarning(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3407872, Boolean.valueOf(z)).sendToTarget();
        }
    }

    /* access modifiers changed from: private */
    public void handleShowImeButton(int i, IBinder iBinder, int i2, int i3, boolean z, boolean z2) {
        int i4;
        if (i != -1) {
            if (!(z2 || (i4 = this.mLastUpdatedImeDisplayId) == i || i4 == -1)) {
                sendImeInvisibleStatusForPrevNavBar();
            }
            for (int i5 = 0; i5 < this.mCallbacks.size(); i5++) {
                this.mCallbacks.get(i5).setImeWindowStatus(i, iBinder, i2, i3, z);
            }
            this.mLastUpdatedImeDisplayId = i;
        }
    }

    private void sendImeInvisibleStatusForPrevNavBar() {
        for (int i = 0; i < this.mCallbacks.size(); i++) {
            this.mCallbacks.get(i).setImeWindowStatus(this.mLastUpdatedImeDisplayId, (IBinder) null, 4, 0, false);
        }
    }

    public void onSystemBarAppearanceChanged(int i, int i2, AppearanceRegion[] appearanceRegionArr, boolean z) {
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.argi2 = i2;
            obtain.argi3 = z ? 1 : 0;
            obtain.arg1 = appearanceRegionArr;
            this.mHandler.obtainMessage(393216, obtain).sendToTarget();
        }
    }

    public void showTransient(int i, int[] iArr) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3145728, i, 0, iArr).sendToTarget();
        }
    }

    public void abortTransient(int i, int[] iArr) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3211264, i, 0, iArr).sendToTarget();
        }
    }

    public void startTracing() {
        synchronized (this.mLock) {
            if (this.mProtoTracer != null) {
                this.mProtoTracer.start();
            }
            this.mHandler.obtainMessage(3604480, Boolean.TRUE).sendToTarget();
        }
    }

    public void stopTracing() {
        synchronized (this.mLock) {
            if (this.mProtoTracer != null) {
                this.mProtoTracer.stop();
            }
            this.mHandler.obtainMessage(3604480, Boolean.FALSE).sendToTarget();
        }
    }

    public void setStatus(int i, String str, Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        sb.append("setStatus: what = ");
        sb.append(i);
        sb.append(", action = ");
        sb.append(str);
        sb.append(", ext = ");
        sb.append(bundle);
        if (!(bundle == null || bundle.keySet() == null)) {
            for (String str2 : bundle.keySet()) {
                sb.append("[");
                sb.append(str2);
                sb.append(" = ");
                sb.append(bundle.get(str2));
            }
        }
        Log.d("CommandQueue", "setStatus: " + sb);
        synchronized (this.mLock) {
            SomeArgs obtain = SomeArgs.obtain();
            obtain.argi1 = i;
            obtain.arg1 = str;
            obtain.arg2 = bundle;
            this.mHandler.obtainMessage(65601536, obtain).sendToTarget();
        }
    }

    public void suppressAmbientDisplay(boolean z) {
        synchronized (this.mLock) {
            this.mHandler.obtainMessage(3670016, Boolean.valueOf(z)).sendToTarget();
        }
    }

    private final class H extends Handler {
        private H(Looper looper) {
            super(looper);
        }

        /* JADX WARNING: Code restructure failed: missing block: B:109:0x03ac, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:110:0x03ae, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).togglePanel();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:112:0x03ca, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:113:0x03cc, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).handleShowGlobalActionsMenu();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:115:0x03e8, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:116:0x03ea, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).handleSystemKey(r15.arg1);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:118:0x0408, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:119:0x040a, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).dismissKeyboardShortcutsMenu();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:11:0x0060, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:121:0x0426, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:122:0x0428, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).appTransitionFinished(r15.arg1);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:124:0x0446, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:125:0x0448, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).toggleSplitScreen();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:127:0x0464, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:128:0x0466, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).clickTile((android.content.ComponentName) r15.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0062, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).onTracingStateChanged(((java.lang.Boolean) r15.obj).booleanValue());
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:130:0x0486, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:131:0x0488, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).remQsTile((android.content.ComponentName) r15.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:133:0x04a8, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:134:0x04aa, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).addQsTile((android.content.ComponentName) r15.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:136:0x04ca, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:137:0x04cc, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).showPictureInPictureMenu();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:139:0x04e8, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:140:0x04ea, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).toggleKeyboardShortcutsMenu(r15.arg1);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:142:0x0508, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:143:0x050a, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).onCameraLaunchGestureDetected(r15.arg1);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:145:0x0528, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:146:0x052a, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).startAssist((android.os.Bundle) r15.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:148:0x054a, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:149:0x054c, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).showAssistDisclosure();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:159:0x05a5, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:160:0x05a7, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).appTransitionCancelled(r15.arg1);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:170:0x05ed, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:171:0x05ef, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).showScreenPinningRequest(r15.arg1);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:193:0x0660, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:194:0x0662, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).setWindowState(r15.arg1, r15.arg2, ((java.lang.Integer) r15.obj).intValue());
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:196:0x068a, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:197:0x068c, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).cancelPreloadRecentApps();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:199:0x06a8, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:200:0x06aa, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).preloadRecentApps();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:202:0x06c6, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:203:0x06c8, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).toggleRecentApps();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:214:0x0708, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:215:0x070a, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).onDisplayReady(r15.arg1);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:226:0x075f, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:227:0x0761, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).animateExpandSettingsPanel((java.lang.String) r15.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x00ed, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:237:0x07a9, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:238:0x07ab, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).animateExpandNotificationsPanel();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:23:0x00ef, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).dismissInattentiveSleepWarning(((java.lang.Boolean) r15.obj).booleanValue());
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:25:0x0113, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:26:0x0115, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).showInattentiveSleepWarning();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:345:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:348:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:349:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:353:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:354:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:355:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:356:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:357:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:358:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:362:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:363:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:364:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:365:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:366:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:367:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:368:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:369:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:370:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:371:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:372:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:373:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:374:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:375:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:377:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:379:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:382:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:383:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:384:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:385:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:386:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:387:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:389:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:57:0x01d7, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:58:0x01d9, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).showPinningEscapeToast();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:60:0x01f5, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:61:0x01f7, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).showPinningEnterExitToast(((java.lang.Boolean) r15.obj).booleanValue());
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:63:0x021b, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:64:0x021d, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).showWirelessChargingAnimation(r15.arg1);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:66:0x023b, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:67:0x023d, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).hideAuthenticationDialog();
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:74:0x0286, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:75:0x0288, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).onBiometricHelp((java.lang.String) r15.obj);
            r1 = r1 + 1;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:77:0x02a8, code lost:
            if (r1 >= com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).size()) goto L_0x0841;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:78:0x02aa, code lost:
            ((com.android.systemui.statusbar.CommandQueue.Callbacks) com.android.systemui.statusbar.CommandQueue.access$100(r14.this$0).get(r1)).onBiometricAuthenticated();
            r1 = r1 + 1;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r15) {
            /*
                r14 = this;
                int r0 = r15.what
                r1 = -65536(0xffffffffffff0000, float:NaN)
                r0 = r0 & r1
                r1 = 0
                r2 = 1
                switch(r0) {
                    case 65536: goto L_0x07ed;
                    case 131072: goto L_0x07bd;
                    case 196608: goto L_0x079f;
                    case 262144: goto L_0x0777;
                    case 327680: goto L_0x0755;
                    case 393216: goto L_0x071e;
                    case 458752: goto L_0x06fe;
                    case 524288: goto L_0x06da;
                    case 589824: goto L_0x06bc;
                    case 655360: goto L_0x069e;
                    case 720896: goto L_0x0680;
                    case 786432: goto L_0x0656;
                    case 851968: goto L_0x0630;
                    case 917504: goto L_0x0603;
                    case 1179648: goto L_0x05e3;
                    case 1245184: goto L_0x05bb;
                    case 1310720: goto L_0x059b;
                    case 1376256: goto L_0x055e;
                    case 1441792: goto L_0x0540;
                    case 1507328: goto L_0x051e;
                    case 1572864: goto L_0x04fe;
                    case 1638400: goto L_0x04de;
                    case 1703936: goto L_0x04c0;
                    case 1769472: goto L_0x049e;
                    case 1835008: goto L_0x047c;
                    case 1900544: goto L_0x045a;
                    case 1966080: goto L_0x043c;
                    case 2031616: goto L_0x041c;
                    case 2097152: goto L_0x03fe;
                    case 2162688: goto L_0x03de;
                    case 2228224: goto L_0x03c0;
                    case 2293760: goto L_0x03a2;
                    case 2359296: goto L_0x0378;
                    case 2424832: goto L_0x0352;
                    case 2490368: goto L_0x032a;
                    case 2555904: goto L_0x02bc;
                    case 2621440: goto L_0x029e;
                    case 2686976: goto L_0x027c;
                    case 2752512: goto L_0x024f;
                    case 2818048: goto L_0x0231;
                    case 2883584: goto L_0x0211;
                    case 2949120: goto L_0x01eb;
                    case 3014656: goto L_0x01cd;
                    case 3080192: goto L_0x01a7;
                    case 3145728: goto L_0x0183;
                    case 3211264: goto L_0x015f;
                    case 3276800: goto L_0x0127;
                    case 3342336: goto L_0x0109;
                    case 3407872: goto L_0x00e3;
                    case 3473408: goto L_0x00a2;
                    case 3538944: goto L_0x007c;
                    case 3604480: goto L_0x0056;
                    case 3670016: goto L_0x0034;
                    case 65601536: goto L_0x000c;
                    default: goto L_0x000a;
                }
            L_0x000a:
                goto L_0x0841
            L_0x000c:
                java.lang.Object r15 = r15.obj
                com.android.internal.os.SomeArgs r15 = (com.android.internal.os.SomeArgs) r15
                com.android.systemui.statusbar.CommandQueue r14 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r14 = r14.mCallbacks
                java.util.Iterator r14 = r14.iterator()
            L_0x001a:
                boolean r0 = r14.hasNext()
                if (r0 == 0) goto L_0x0841
                java.lang.Object r0 = r14.next()
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r1 = r15.argi1
                java.lang.Object r2 = r15.arg1
                java.lang.String r2 = (java.lang.String) r2
                java.lang.Object r3 = r15.arg2
                android.os.Bundle r3 = (android.os.Bundle) r3
                r0.setStatus(r1, r2, r3)
                goto L_0x001a
            L_0x0034:
                com.android.systemui.statusbar.CommandQueue r14 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r14 = r14.mCallbacks
                java.util.Iterator r14 = r14.iterator()
            L_0x003e:
                boolean r0 = r14.hasNext()
                if (r0 == 0) goto L_0x0841
                java.lang.Object r0 = r14.next()
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r1 = r15.obj
                java.lang.Boolean r1 = (java.lang.Boolean) r1
                boolean r1 = r1.booleanValue()
                r0.suppressAmbientDisplay(r1)
                goto L_0x003e
            L_0x0056:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r15.obj
                java.lang.Boolean r2 = (java.lang.Boolean) r2
                boolean r2 = r2.booleanValue()
                r0.onTracingStateChanged(r2)
                int r1 = r1 + 1
                goto L_0x0056
            L_0x007c:
                java.lang.Object r15 = r15.obj
                com.android.internal.os.SomeArgs r15 = (com.android.internal.os.SomeArgs) r15
                java.lang.Object r0 = r15.arg1
                java.lang.String r0 = (java.lang.String) r0
                java.lang.Object r15 = r15.arg2
                android.os.IBinder r15 = (android.os.IBinder) r15
                com.android.systemui.statusbar.CommandQueue r14 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r14 = r14.mCallbacks
                java.util.Iterator r14 = r14.iterator()
            L_0x0092:
                boolean r1 = r14.hasNext()
                if (r1 == 0) goto L_0x0841
                java.lang.Object r1 = r14.next()
                com.android.systemui.statusbar.CommandQueue$Callbacks r1 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r1
                r1.hideToast(r0, r15)
                goto L_0x0092
            L_0x00a2:
                java.lang.Object r15 = r15.obj
                com.android.internal.os.SomeArgs r15 = (com.android.internal.os.SomeArgs) r15
                java.lang.Object r0 = r15.arg1
                java.lang.String r0 = (java.lang.String) r0
                java.lang.Object r1 = r15.arg2
                r9 = r1
                android.os.IBinder r9 = (android.os.IBinder) r9
                java.lang.Object r1 = r15.arg3
                r10 = r1
                java.lang.CharSequence r10 = (java.lang.CharSequence) r10
                java.lang.Object r1 = r15.arg4
                r11 = r1
                android.os.IBinder r11 = (android.os.IBinder) r11
                java.lang.Object r1 = r15.arg5
                r12 = r1
                android.app.ITransientNotificationCallback r12 = (android.app.ITransientNotificationCallback) r12
                int r13 = r15.argi1
                int r15 = r15.argi2
                com.android.systemui.statusbar.CommandQueue r14 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r14 = r14.mCallbacks
                java.util.Iterator r14 = r14.iterator()
            L_0x00cc:
                boolean r1 = r14.hasNext()
                if (r1 == 0) goto L_0x0841
                java.lang.Object r1 = r14.next()
                com.android.systemui.statusbar.CommandQueue$Callbacks r1 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r1
                r2 = r13
                r3 = r0
                r4 = r9
                r5 = r10
                r6 = r11
                r7 = r15
                r8 = r12
                r1.showToast(r2, r3, r4, r5, r6, r7, r8)
                goto L_0x00cc
            L_0x00e3:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r15.obj
                java.lang.Boolean r2 = (java.lang.Boolean) r2
                boolean r2 = r2.booleanValue()
                r0.dismissInattentiveSleepWarning(r2)
                int r1 = r1 + 1
                goto L_0x00e3
            L_0x0109:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.showInattentiveSleepWarning()
                int r1 = r1 + 1
                goto L_0x0109
            L_0x0127:
                java.lang.Object r15 = r15.obj
                com.android.internal.os.SomeArgs r15 = (com.android.internal.os.SomeArgs) r15
                r0 = r1
            L_0x012c:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x015a
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r15.argi1
                int r5 = r15.argi2
                if (r5 == 0) goto L_0x014c
                r5 = r2
                goto L_0x014d
            L_0x014c:
                r5 = r1
            L_0x014d:
                int r6 = r15.argi3
                if (r6 == 0) goto L_0x0153
                r6 = r2
                goto L_0x0154
            L_0x0153:
                r6 = r1
            L_0x0154:
                r3.topAppWindowChanged(r4, r5, r6)
                int r0 = r0 + 1
                goto L_0x012c
            L_0x015a:
                r15.recycle()
                goto L_0x0841
            L_0x015f:
                int r0 = r15.arg1
                java.lang.Object r15 = r15.obj
                int[] r15 = (int[]) r15
            L_0x0165:
                com.android.systemui.statusbar.CommandQueue r2 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r2 = r2.mCallbacks
                int r2 = r2.size()
                if (r1 >= r2) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r2 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r2 = r2.mCallbacks
                java.lang.Object r2 = r2.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r2 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r2
                r2.abortTransient(r0, r15)
                int r1 = r1 + 1
                goto L_0x0165
            L_0x0183:
                int r0 = r15.arg1
                java.lang.Object r15 = r15.obj
                int[] r15 = (int[]) r15
            L_0x0189:
                com.android.systemui.statusbar.CommandQueue r2 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r2 = r2.mCallbacks
                int r2 = r2.size()
                if (r1 >= r2) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r2 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r2 = r2.mCallbacks
                java.lang.Object r2 = r2.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r2 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r2
                r2.showTransient(r0, r15)
                int r1 = r1 + 1
                goto L_0x0189
            L_0x01a7:
                r0 = r1
            L_0x01a8:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r15.arg1
                if (r4 <= 0) goto L_0x01c6
                r4 = r2
                goto L_0x01c7
            L_0x01c6:
                r4 = r1
            L_0x01c7:
                r3.onRecentsAnimationStateChanged(r4)
                int r0 = r0 + 1
                goto L_0x01a8
            L_0x01cd:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.showPinningEscapeToast()
                int r1 = r1 + 1
                goto L_0x01cd
            L_0x01eb:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r15.obj
                java.lang.Boolean r2 = (java.lang.Boolean) r2
                boolean r2 = r2.booleanValue()
                r0.showPinningEnterExitToast(r2)
                int r1 = r1 + 1
                goto L_0x01eb
            L_0x0211:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r15.arg1
                r0.showWirelessChargingAnimation(r2)
                int r1 = r1 + 1
                goto L_0x0211
            L_0x0231:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.hideAuthenticationDialog()
                int r1 = r1 + 1
                goto L_0x0231
            L_0x024f:
                java.lang.Object r15 = r15.obj
                com.android.internal.os.SomeArgs r15 = (com.android.internal.os.SomeArgs) r15
            L_0x0253:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0277
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r15.argi1
                int r3 = r15.argi2
                int r4 = r15.argi3
                r0.onBiometricError(r2, r3, r4)
                int r1 = r1 + 1
                goto L_0x0253
            L_0x0277:
                r15.recycle()
                goto L_0x0841
            L_0x027c:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r15.obj
                java.lang.String r2 = (java.lang.String) r2
                r0.onBiometricHelp(r2)
                int r1 = r1 + 1
                goto L_0x027c
            L_0x029e:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.onBiometricAuthenticated()
                int r1 = r1 + 1
                goto L_0x029e
            L_0x02bc:
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
                java.lang.Object r15 = r15.obj
                com.android.internal.os.SomeArgs r15 = (com.android.internal.os.SomeArgs) r15
            L_0x02e1:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0325
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                r2 = r0
                com.android.systemui.statusbar.CommandQueue$Callbacks r2 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r2
                java.lang.Object r0 = r15.arg1
                r3 = r0
                android.os.Bundle r3 = (android.os.Bundle) r3
                java.lang.Object r0 = r15.arg2
                r4 = r0
                android.hardware.biometrics.IBiometricServiceReceiverInternal r4 = (android.hardware.biometrics.IBiometricServiceReceiverInternal) r4
                int r5 = r15.argi1
                java.lang.Object r0 = r15.arg3
                java.lang.Boolean r0 = (java.lang.Boolean) r0
                boolean r6 = r0.booleanValue()
                int r7 = r15.argi2
                java.lang.Object r0 = r15.arg4
                r8 = r0
                java.lang.String r8 = (java.lang.String) r8
                java.lang.Object r0 = r15.arg5
                java.lang.Long r0 = (java.lang.Long) r0
                long r9 = r0.longValue()
                int r11 = r15.argi3
                r2.showAuthenticationDialog(r3, r4, r5, r6, r7, r8, r9, r11)
                int r1 = r1 + 1
                goto L_0x02e1
            L_0x0325:
                r15.recycle()
                goto L_0x0841
            L_0x032a:
                r0 = r1
            L_0x032b:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r15.arg1
                int r5 = r15.arg2
                if (r5 == 0) goto L_0x034b
                r5 = r2
                goto L_0x034c
            L_0x034b:
                r5 = r1
            L_0x034c:
                r3.onRotationProposal(r4, r5)
                int r0 = r0 + 1
                goto L_0x032b
            L_0x0352:
                r0 = r1
            L_0x0353:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r15.arg1
                if (r4 == 0) goto L_0x0371
                r4 = r2
                goto L_0x0372
            L_0x0371:
                r4 = r1
            L_0x0372:
                r3.setTopAppHidesStatusBar(r4)
                int r0 = r0 + 1
                goto L_0x0353
            L_0x0378:
                r0 = r1
            L_0x0379:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r15.arg1
                if (r4 == 0) goto L_0x0397
                r4 = r2
                goto L_0x0398
            L_0x0397:
                r4 = r1
            L_0x0398:
                java.lang.Object r5 = r15.obj
                java.lang.String r5 = (java.lang.String) r5
                r3.handleShowShutdownUi(r4, r5)
                int r0 = r0 + 1
                goto L_0x0379
            L_0x03a2:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.togglePanel()
                int r1 = r1 + 1
                goto L_0x03a2
            L_0x03c0:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.handleShowGlobalActionsMenu()
                int r1 = r1 + 1
                goto L_0x03c0
            L_0x03de:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r15.arg1
                r0.handleSystemKey(r2)
                int r1 = r1 + 1
                goto L_0x03de
            L_0x03fe:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.dismissKeyboardShortcutsMenu()
                int r1 = r1 + 1
                goto L_0x03fe
            L_0x041c:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r15.arg1
                r0.appTransitionFinished(r2)
                int r1 = r1 + 1
                goto L_0x041c
            L_0x043c:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.toggleSplitScreen()
                int r1 = r1 + 1
                goto L_0x043c
            L_0x045a:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r15.obj
                android.content.ComponentName r2 = (android.content.ComponentName) r2
                r0.clickTile(r2)
                int r1 = r1 + 1
                goto L_0x045a
            L_0x047c:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r15.obj
                android.content.ComponentName r2 = (android.content.ComponentName) r2
                r0.remQsTile(r2)
                int r1 = r1 + 1
                goto L_0x047c
            L_0x049e:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r15.obj
                android.content.ComponentName r2 = (android.content.ComponentName) r2
                r0.addQsTile(r2)
                int r1 = r1 + 1
                goto L_0x049e
            L_0x04c0:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.showPictureInPictureMenu()
                int r1 = r1 + 1
                goto L_0x04c0
            L_0x04de:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r15.arg1
                r0.toggleKeyboardShortcutsMenu(r2)
                int r1 = r1 + 1
                goto L_0x04de
            L_0x04fe:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r15.arg1
                r0.onCameraLaunchGestureDetected(r2)
                int r1 = r1 + 1
                goto L_0x04fe
            L_0x051e:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r15.obj
                android.os.Bundle r2 = (android.os.Bundle) r2
                r0.startAssist(r2)
                int r1 = r1 + 1
                goto L_0x051e
            L_0x0540:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.showAssistDisclosure()
                int r1 = r1 + 1
                goto L_0x0540
            L_0x055e:
                java.lang.Object r15 = r15.obj
                com.android.internal.os.SomeArgs r15 = (com.android.internal.os.SomeArgs) r15
                r0 = r1
            L_0x0563:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                r4 = r3
                com.android.systemui.statusbar.CommandQueue$Callbacks r4 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r4
                int r5 = r15.argi1
                java.lang.Object r3 = r15.arg1
                java.lang.Long r3 = (java.lang.Long) r3
                long r6 = r3.longValue()
                java.lang.Object r3 = r15.arg2
                java.lang.Long r3 = (java.lang.Long) r3
                long r8 = r3.longValue()
                int r3 = r15.argi2
                if (r3 == 0) goto L_0x0594
                r10 = r2
                goto L_0x0595
            L_0x0594:
                r10 = r1
            L_0x0595:
                r4.appTransitionStarting(r5, r6, r8, r10)
                int r0 = r0 + 1
                goto L_0x0563
            L_0x059b:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r15.arg1
                r0.appTransitionCancelled(r2)
                int r1 = r1 + 1
                goto L_0x059b
            L_0x05bb:
                r0 = r1
            L_0x05bc:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r15.arg1
                int r5 = r15.arg2
                if (r5 == 0) goto L_0x05dc
                r5 = r2
                goto L_0x05dd
            L_0x05dc:
                r5 = r1
            L_0x05dd:
                r3.appTransitionPending(r4, r5)
                int r0 = r0 + 1
                goto L_0x05bc
            L_0x05e3:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r15.arg1
                r0.showScreenPinningRequest(r2)
                int r1 = r1 + 1
                goto L_0x05e3
            L_0x0603:
                r0 = r1
            L_0x0604:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r15.arg1
                if (r4 == 0) goto L_0x0622
                r4 = r2
                goto L_0x0623
            L_0x0622:
                r4 = r1
            L_0x0623:
                int r5 = r15.arg2
                if (r5 == 0) goto L_0x0629
                r5 = r2
                goto L_0x062a
            L_0x0629:
                r5 = r1
            L_0x062a:
                r3.hideRecentApps(r4, r5)
                int r0 = r0 + 1
                goto L_0x0604
            L_0x0630:
                r0 = r1
            L_0x0631:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r15.arg1
                if (r4 == 0) goto L_0x064f
                r4 = r2
                goto L_0x0650
            L_0x064f:
                r4 = r1
            L_0x0650:
                r3.showRecentApps(r4)
                int r0 = r0 + 1
                goto L_0x0631
            L_0x0656:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r15.arg1
                int r3 = r15.arg2
                java.lang.Object r4 = r15.obj
                java.lang.Integer r4 = (java.lang.Integer) r4
                int r4 = r4.intValue()
                r0.setWindowState(r2, r3, r4)
                int r1 = r1 + 1
                goto L_0x0656
            L_0x0680:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.cancelPreloadRecentApps()
                int r1 = r1 + 1
                goto L_0x0680
            L_0x069e:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.preloadRecentApps()
                int r1 = r1 + 1
                goto L_0x069e
            L_0x06bc:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.toggleRecentApps()
                int r1 = r1 + 1
                goto L_0x06bc
            L_0x06da:
                java.lang.Object r15 = r15.obj
                com.android.internal.os.SomeArgs r15 = (com.android.internal.os.SomeArgs) r15
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                int r4 = r15.argi1
                java.lang.Object r14 = r15.arg1
                r5 = r14
                android.os.IBinder r5 = (android.os.IBinder) r5
                int r6 = r15.argi2
                int r7 = r15.argi3
                int r14 = r15.argi4
                if (r14 == 0) goto L_0x06f1
                r8 = r2
                goto L_0x06f2
            L_0x06f1:
                r8 = r1
            L_0x06f2:
                int r14 = r15.argi5
                if (r14 == 0) goto L_0x06f8
                r9 = r2
                goto L_0x06f9
            L_0x06f8:
                r9 = r1
            L_0x06f9:
                r3.handleShowImeButton(r4, r5, r6, r7, r8, r9)
                goto L_0x0841
            L_0x06fe:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                int r2 = r15.arg1
                r0.onDisplayReady(r2)
                int r1 = r1 + 1
                goto L_0x06fe
            L_0x071e:
                java.lang.Object r15 = r15.obj
                com.android.internal.os.SomeArgs r15 = (com.android.internal.os.SomeArgs) r15
                r0 = r1
            L_0x0723:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x0750
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r15.argi1
                int r5 = r15.argi2
                java.lang.Object r6 = r15.arg1
                com.android.internal.view.AppearanceRegion[] r6 = (com.android.internal.view.AppearanceRegion[]) r6
                int r7 = r15.argi3
                if (r7 != r2) goto L_0x0749
                r7 = r2
                goto L_0x074a
            L_0x0749:
                r7 = r1
            L_0x074a:
                r3.onSystemBarAppearanceChanged(r4, r5, r6, r7)
                int r0 = r0 + 1
                goto L_0x0723
            L_0x0750:
                r15.recycle()
                goto L_0x0841
            L_0x0755:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r15.obj
                java.lang.String r2 = (java.lang.String) r2
                r0.animateExpandSettingsPanel(r2)
                int r1 = r1 + 1
                goto L_0x0755
            L_0x0777:
                r0 = r1
            L_0x0778:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r15.arg1
                int r5 = r15.arg2
                if (r5 == 0) goto L_0x0798
                r5 = r2
                goto L_0x0799
            L_0x0798:
                r5 = r1
            L_0x0799:
                r3.animateCollapsePanels(r4, r5)
                int r0 = r0 + 1
                goto L_0x0778
            L_0x079f:
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                int r15 = r15.size()
                if (r1 >= r15) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r15 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r15 = r15.mCallbacks
                java.lang.Object r15 = r15.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r15 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r15
                r15.animateExpandNotificationsPanel()
                int r1 = r1 + 1
                goto L_0x079f
            L_0x07bd:
                java.lang.Object r15 = r15.obj
                com.android.internal.os.SomeArgs r15 = (com.android.internal.os.SomeArgs) r15
                r0 = r1
            L_0x07c2:
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                int r3 = r3.size()
                if (r0 >= r3) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r3 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r3 = r3.mCallbacks
                java.lang.Object r3 = r3.get(r0)
                com.android.systemui.statusbar.CommandQueue$Callbacks r3 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r3
                int r4 = r15.argi1
                int r5 = r15.argi2
                int r6 = r15.argi3
                int r7 = r15.argi4
                if (r7 == 0) goto L_0x07e6
                r7 = r2
                goto L_0x07e7
            L_0x07e6:
                r7 = r1
            L_0x07e7:
                r3.disable(r4, r5, r6, r7)
                int r0 = r0 + 1
                goto L_0x07c2
            L_0x07ed:
                int r0 = r15.arg1
                if (r0 == r2) goto L_0x0817
                r2 = 2
                if (r0 == r2) goto L_0x07f5
                goto L_0x0841
            L_0x07f5:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r15.obj
                java.lang.String r2 = (java.lang.String) r2
                r0.removeIcon(r2)
                int r1 = r1 + 1
                goto L_0x07f5
            L_0x0817:
                java.lang.Object r15 = r15.obj
                android.util.Pair r15 = (android.util.Pair) r15
            L_0x081b:
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                int r0 = r0.size()
                if (r1 >= r0) goto L_0x0841
                com.android.systemui.statusbar.CommandQueue r0 = com.android.systemui.statusbar.CommandQueue.this
                java.util.ArrayList r0 = r0.mCallbacks
                java.lang.Object r0 = r0.get(r1)
                com.android.systemui.statusbar.CommandQueue$Callbacks r0 = (com.android.systemui.statusbar.CommandQueue.Callbacks) r0
                java.lang.Object r2 = r15.first
                java.lang.String r2 = (java.lang.String) r2
                java.lang.Object r3 = r15.second
                com.android.internal.statusbar.StatusBarIcon r3 = (com.android.internal.statusbar.StatusBarIcon) r3
                r0.setIcon(r2, r3)
                int r1 = r1 + 1
                goto L_0x081b
            L_0x0841:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.CommandQueue.H.handleMessage(android.os.Message):void");
        }
    }
}

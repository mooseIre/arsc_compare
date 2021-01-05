package com.android.systemui.classifier;

import android.content.Context;
import android.net.Uri;
import android.provider.DeviceConfig;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.Dumpable;
import com.android.systemui.classifier.brightline.BrightLineFalsingManager;
import com.android.systemui.classifier.brightline.FalsingDataProvider;
import com.android.systemui.dock.DockManager;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.FalsingPlugin;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.util.DeviceConfigProxy;
import com.android.systemui.util.sensors.ProximitySensor;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.Executor;

public class FalsingManagerProxy implements FalsingManager, Dumpable {
    private boolean mBrightlineEnabled;
    private final DeviceConfigProxy mDeviceConfig;
    private DeviceConfig.OnPropertiesChangedListener mDeviceConfigListener;
    private final DisplayMetrics mDisplayMetrics;
    private final DockManager mDockManager;
    /* access modifiers changed from: private */
    public FalsingManager mInternalFalsingManager;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final ProximitySensor mProximitySensor;
    private final StatusBarStateController mStatusBarStateController;
    /* access modifiers changed from: private */
    public Executor mUiBgExecutor;

    FalsingManagerProxy(final Context context, PluginManager pluginManager, Executor executor, DisplayMetrics displayMetrics, ProximitySensor proximitySensor, DeviceConfigProxy deviceConfigProxy, DockManager dockManager, KeyguardUpdateMonitor keyguardUpdateMonitor, DumpManager dumpManager, Executor executor2, StatusBarStateController statusBarStateController) {
        this.mDisplayMetrics = displayMetrics;
        this.mProximitySensor = proximitySensor;
        this.mDockManager = dockManager;
        this.mKeyguardUpdateMonitor = keyguardUpdateMonitor;
        this.mUiBgExecutor = executor2;
        this.mStatusBarStateController = statusBarStateController;
        proximitySensor.setTag("FalsingManager");
        this.mProximitySensor.setSensorDelay(1);
        this.mDeviceConfig = deviceConfigProxy;
        this.mDeviceConfigListener = new DeviceConfig.OnPropertiesChangedListener(context) {
            public final /* synthetic */ Context f$1;

            {
                this.f$1 = r2;
            }

            public final void onPropertiesChanged(DeviceConfig.Properties properties) {
                FalsingManagerProxy.this.lambda$new$0$FalsingManagerProxy(this.f$1, properties);
            }
        };
        setupFalsingManager(context);
        this.mDeviceConfig.addOnPropertiesChangedListener("systemui", executor, this.mDeviceConfigListener);
        pluginManager.addPluginListener(new PluginListener<FalsingPlugin>() {
            public void onPluginConnected(FalsingPlugin falsingPlugin, Context context) {
                FalsingManager falsingManager = falsingPlugin.getFalsingManager(context);
                if (falsingManager != null) {
                    FalsingManagerProxy.this.mInternalFalsingManager.cleanup();
                    FalsingManager unused = FalsingManagerProxy.this.mInternalFalsingManager = falsingManager;
                }
            }

            public void onPluginDisconnected(FalsingPlugin falsingPlugin) {
                FalsingManager unused = FalsingManagerProxy.this.mInternalFalsingManager = new FalsingManagerImpl(context, FalsingManagerProxy.this.mUiBgExecutor);
            }
        }, FalsingPlugin.class);
        dumpManager.registerDumpable("FalsingManager", this);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$FalsingManagerProxy(Context context, DeviceConfig.Properties properties) {
        onDeviceConfigPropertiesChanged(context, properties.getNamespace());
    }

    private void onDeviceConfigPropertiesChanged(Context context, String str) {
        if ("systemui".equals(str)) {
            setupFalsingManager(context);
        }
    }

    private void setupFalsingManager(Context context) {
        boolean z = this.mDeviceConfig.getBoolean("systemui", "brightline_falsing_manager_enabled", false);
        if (z != this.mBrightlineEnabled || this.mInternalFalsingManager == null) {
            this.mBrightlineEnabled = z;
            FalsingManager falsingManager = this.mInternalFalsingManager;
            if (falsingManager != null) {
                falsingManager.cleanup();
            }
            if (!z) {
                this.mInternalFalsingManager = new FalsingManagerImpl(context, this.mUiBgExecutor);
            } else {
                this.mInternalFalsingManager = new BrightLineFalsingManager(new FalsingDataProvider(this.mDisplayMetrics), this.mKeyguardUpdateMonitor, this.mProximitySensor, this.mDeviceConfig, this.mDockManager, this.mStatusBarStateController);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public FalsingManager getInternalFalsingManager() {
        return this.mInternalFalsingManager;
    }

    public void onSuccessfulUnlock() {
        this.mInternalFalsingManager.onSuccessfulUnlock();
    }

    public void onNotificationActive() {
        this.mInternalFalsingManager.onNotificationActive();
    }

    public void setShowingAod(boolean z) {
        this.mInternalFalsingManager.setShowingAod(z);
    }

    public void onNotificatonStartDraggingDown() {
        this.mInternalFalsingManager.onNotificatonStartDraggingDown();
    }

    public boolean isUnlockingDisabled() {
        return this.mInternalFalsingManager.isUnlockingDisabled();
    }

    public boolean isFalseTouch() {
        return this.mInternalFalsingManager.isFalseTouch();
    }

    public void onNotificatonStopDraggingDown() {
        this.mInternalFalsingManager.onNotificatonStartDraggingDown();
    }

    public void setNotificationExpanded() {
        this.mInternalFalsingManager.setNotificationExpanded();
    }

    public boolean isClassifierEnabled() {
        return this.mInternalFalsingManager.isClassifierEnabled();
    }

    public void onQsDown() {
        this.mInternalFalsingManager.onQsDown();
    }

    public void setQsExpanded(boolean z) {
        this.mInternalFalsingManager.setQsExpanded(z);
    }

    public boolean shouldEnforceBouncer() {
        return this.mInternalFalsingManager.shouldEnforceBouncer();
    }

    public void onTrackingStarted(boolean z) {
        this.mInternalFalsingManager.onTrackingStarted(z);
    }

    public void onTrackingStopped() {
        this.mInternalFalsingManager.onTrackingStopped();
    }

    public void onLeftAffordanceOn() {
        this.mInternalFalsingManager.onLeftAffordanceOn();
    }

    public void onCameraOn() {
        this.mInternalFalsingManager.onCameraOn();
    }

    public void onAffordanceSwipingStarted(boolean z) {
        this.mInternalFalsingManager.onAffordanceSwipingStarted(z);
    }

    public void onAffordanceSwipingAborted() {
        this.mInternalFalsingManager.onAffordanceSwipingAborted();
    }

    public void onStartExpandingFromPulse() {
        this.mInternalFalsingManager.onStartExpandingFromPulse();
    }

    public void onExpansionFromPulseStopped() {
        this.mInternalFalsingManager.onExpansionFromPulseStopped();
    }

    public Uri reportRejectedTouch() {
        return this.mInternalFalsingManager.reportRejectedTouch();
    }

    public void onScreenOnFromTouch() {
        this.mInternalFalsingManager.onScreenOnFromTouch();
    }

    public boolean isReportingEnabled() {
        return this.mInternalFalsingManager.isReportingEnabled();
    }

    public void onUnlockHintStarted() {
        this.mInternalFalsingManager.onUnlockHintStarted();
    }

    public void onCameraHintStarted() {
        this.mInternalFalsingManager.onCameraHintStarted();
    }

    public void onLeftAffordanceHintStarted() {
        this.mInternalFalsingManager.onLeftAffordanceHintStarted();
    }

    public void onScreenTurningOn() {
        this.mInternalFalsingManager.onScreenTurningOn();
    }

    public void onScreenOff() {
        this.mInternalFalsingManager.onScreenOff();
    }

    public void onNotificationStopDismissing() {
        this.mInternalFalsingManager.onNotificationStopDismissing();
    }

    public void onNotificationDismissed() {
        this.mInternalFalsingManager.onNotificationDismissed();
    }

    public void onNotificationStartDismissing() {
        this.mInternalFalsingManager.onNotificationStartDismissing();
    }

    public void onNotificationDoubleTap(boolean z, float f, float f2) {
        this.mInternalFalsingManager.onNotificationDoubleTap(z, f, f2);
    }

    public void onBouncerShown() {
        this.mInternalFalsingManager.onBouncerShown();
    }

    public void onBouncerHidden() {
        this.mInternalFalsingManager.onBouncerHidden();
    }

    public void onTouchEvent(MotionEvent motionEvent, int i, int i2) {
        this.mInternalFalsingManager.onTouchEvent(motionEvent, i, i2);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        this.mInternalFalsingManager.dump(printWriter);
    }

    public void dump(PrintWriter printWriter) {
        this.mInternalFalsingManager.dump(printWriter);
    }

    public void cleanup() {
        this.mDeviceConfig.removeOnPropertiesChangedListener(this.mDeviceConfigListener);
        this.mInternalFalsingManager.cleanup();
    }
}

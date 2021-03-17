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
    private FalsingManager mInternalFalsingManager;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final ProximitySensor mProximitySensor;
    private final StatusBarStateController mStatusBarStateController;
    private Executor mUiBgExecutor;

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
            /* class com.android.systemui.classifier.$$Lambda$FalsingManagerProxy$15gIs9mVwyDjJbglxP0IV0T3ag */
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
            /* class com.android.systemui.classifier.FalsingManagerProxy.AnonymousClass1 */

            public void onPluginConnected(FalsingPlugin falsingPlugin, Context context) {
                FalsingManager falsingManager = falsingPlugin.getFalsingManager(context);
                if (falsingManager != null) {
                    FalsingManagerProxy.this.mInternalFalsingManager.cleanup();
                    FalsingManagerProxy.this.mInternalFalsingManager = falsingManager;
                }
            }

            public void onPluginDisconnected(FalsingPlugin falsingPlugin) {
                FalsingManagerProxy.this.mInternalFalsingManager = new FalsingManagerImpl(context, FalsingManagerProxy.this.mUiBgExecutor);
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

    @Override // com.android.systemui.plugins.FalsingManager
    public void onSuccessfulUnlock() {
        this.mInternalFalsingManager.onSuccessfulUnlock();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationActive() {
        this.mInternalFalsingManager.onNotificationActive();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void setShowingAod(boolean z) {
        this.mInternalFalsingManager.setShowingAod(z);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificatonStartDraggingDown() {
        this.mInternalFalsingManager.onNotificatonStartDraggingDown();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isUnlockingDisabled() {
        return this.mInternalFalsingManager.isUnlockingDisabled();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isFalseTouch() {
        return this.mInternalFalsingManager.isFalseTouch();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificatonStopDraggingDown() {
        this.mInternalFalsingManager.onNotificatonStartDraggingDown();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void setNotificationExpanded() {
        this.mInternalFalsingManager.setNotificationExpanded();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isClassifierEnabled() {
        return this.mInternalFalsingManager.isClassifierEnabled();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onQsDown() {
        this.mInternalFalsingManager.onQsDown();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void setQsExpanded(boolean z) {
        this.mInternalFalsingManager.setQsExpanded(z);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean shouldEnforceBouncer() {
        return this.mInternalFalsingManager.shouldEnforceBouncer();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onTrackingStarted(boolean z) {
        this.mInternalFalsingManager.onTrackingStarted(z);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onTrackingStopped() {
        this.mInternalFalsingManager.onTrackingStopped();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onLeftAffordanceOn() {
        this.mInternalFalsingManager.onLeftAffordanceOn();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onCameraOn() {
        this.mInternalFalsingManager.onCameraOn();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onAffordanceSwipingStarted(boolean z) {
        this.mInternalFalsingManager.onAffordanceSwipingStarted(z);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onAffordanceSwipingAborted() {
        this.mInternalFalsingManager.onAffordanceSwipingAborted();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onStartExpandingFromPulse() {
        this.mInternalFalsingManager.onStartExpandingFromPulse();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onExpansionFromPulseStopped() {
        this.mInternalFalsingManager.onExpansionFromPulseStopped();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public Uri reportRejectedTouch() {
        return this.mInternalFalsingManager.reportRejectedTouch();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onScreenOnFromTouch() {
        this.mInternalFalsingManager.onScreenOnFromTouch();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public boolean isReportingEnabled() {
        return this.mInternalFalsingManager.isReportingEnabled();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onUnlockHintStarted() {
        this.mInternalFalsingManager.onUnlockHintStarted();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onCameraHintStarted() {
        this.mInternalFalsingManager.onCameraHintStarted();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onLeftAffordanceHintStarted() {
        this.mInternalFalsingManager.onLeftAffordanceHintStarted();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onScreenTurningOn() {
        this.mInternalFalsingManager.onScreenTurningOn();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onScreenOff() {
        this.mInternalFalsingManager.onScreenOff();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationStopDismissing() {
        this.mInternalFalsingManager.onNotificationStopDismissing();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationDismissed() {
        this.mInternalFalsingManager.onNotificationDismissed();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationStartDismissing() {
        this.mInternalFalsingManager.onNotificationStartDismissing();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onNotificationDoubleTap(boolean z, float f, float f2) {
        this.mInternalFalsingManager.onNotificationDoubleTap(z, f, f2);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onBouncerShown() {
        this.mInternalFalsingManager.onBouncerShown();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onBouncerHidden() {
        this.mInternalFalsingManager.onBouncerHidden();
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void onTouchEvent(MotionEvent motionEvent, int i, int i2) {
        this.mInternalFalsingManager.onTouchEvent(motionEvent, i, i2);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        this.mInternalFalsingManager.dump(printWriter);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void dump(PrintWriter printWriter) {
        this.mInternalFalsingManager.dump(printWriter);
    }

    @Override // com.android.systemui.plugins.FalsingManager
    public void cleanup() {
        this.mDeviceConfig.removeOnPropertiesChangedListener(this.mDeviceConfigListener);
        this.mInternalFalsingManager.cleanup();
    }
}

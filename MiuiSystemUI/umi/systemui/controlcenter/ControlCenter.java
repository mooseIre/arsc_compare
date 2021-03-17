package com.android.systemui.controlcenter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.SystemUI;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.controlcenter.phone.ControlCenterPanelView;
import com.android.systemui.controlcenter.phone.ControlPanelContentView;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.phone.ControlPanelWindowManager;
import com.android.systemui.controlcenter.phone.ControlPanelWindowView;
import com.android.systemui.controlcenter.phone.ExpandInfoController;
import com.android.systemui.controlcenter.phone.controls.ControlsPluginManager;
import com.android.systemui.controlcenter.policy.ControlCenterActivityStarter;
import com.android.systemui.controlcenter.policy.SuperSaveModeController;
import com.android.systemui.controlcenter.utils.Constants;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.InjectionInflationController;
import com.miui.systemui.util.CommonUtil;

public class ControlCenter extends SystemUI implements ControlPanelController.UseControlPanelChangeListener, SuperSaveModeController.SuperSaveModeChangeListener, CommandQueue.Callbacks, ConfigurationController.ConfigurationListener {
    public static final boolean DEBUG = Constants.DEBUG;
    private static final boolean ONLY_CORE_APPS;
    private CommandQueue mCommandQueue;
    private Configuration mConfiguration;
    private ConfigurationController mConfigurationController;
    private ControlCenterActivityStarter mControlCenterActivityStarter;
    private ControlPanelContentView mControlPanelContentView;
    private ControlPanelController mControlPanelController;
    protected ControlPanelWindowManager mControlPanelWindowManager;
    protected ControlPanelWindowView mControlPanelWindowView;
    private ControlsPluginManager mControlsPluginManager;
    private int mDisabled1 = 0;
    private int mDisabled2 = 0;
    protected Display mDisplay;
    private int mDisplayId;
    private ExpandInfoController mExpandInfoController;
    private Handler mHandler = new H();
    private InjectionInflationController mInjectionInflationController;
    private QSTileHost mQSControlTileHost;
    protected StatusBar mStatusBar;
    private ActivityStarter mStatusBarActivityStarter;
    private boolean mSuperPowerModeOn;
    private SuperSaveModeController mSuperSaveModeController;
    protected boolean mUseControlCenter = false;
    protected WindowManager mWindowManager;

    static {
        boolean z;
        try {
            z = IPackageManager.Stub.asInterface(ServiceManager.getService("package")).isOnlyCoreApps();
        } catch (RemoteException unused) {
            z = false;
        }
        ONLY_CORE_APPS = z;
    }

    public ControlCenter(Context context, ControlPanelController controlPanelController, StatusBarIconController statusBarIconController, ExpandInfoController expandInfoController, ActivityStarter activityStarter, CommandQueue commandQueue, InjectionInflationController injectionInflationController, SuperSaveModeController superSaveModeController, ControlCenterActivityStarter controlCenterActivityStarter, QSTileHost qSTileHost, ControlPanelWindowManager controlPanelWindowManager, StatusBar statusBar, ControlsPluginManager controlsPluginManager, BroadcastDispatcher broadcastDispatcher, ConfigurationController configurationController) {
        super(context);
        this.mControlPanelController = controlPanelController;
        this.mExpandInfoController = expandInfoController;
        this.mStatusBarActivityStarter = activityStarter;
        this.mSuperSaveModeController = superSaveModeController;
        this.mControlCenterActivityStarter = controlCenterActivityStarter;
        this.mCommandQueue = commandQueue;
        this.mInjectionInflationController = injectionInflationController;
        this.mQSControlTileHost = qSTileHost;
        this.mControlPanelWindowManager = controlPanelWindowManager;
        this.mStatusBar = statusBar;
        this.mControlsPluginManager = controlsPluginManager;
        this.mConfigurationController = configurationController;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mControlPanelController.addCallback((ControlPanelController.UseControlPanelChangeListener) this);
        this.mConfigurationController.addCallback(this);
        this.mControlPanelController.setStatusBar(this.mStatusBar);
        WindowManager windowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mWindowManager = windowManager;
        Display defaultDisplay = windowManager.getDefaultDisplay();
        this.mDisplay = defaultDisplay;
        this.mDisplayId = defaultDisplay.getDisplayId();
        this.mConfiguration = new Configuration(this.mContext.getResources().getConfiguration());
        this.mControlsPluginManager.addControlsPluginListener();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onBootCompleted() {
        super.onBootCompleted();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.SystemUI
    public void onConfigurationChanged(Configuration configuration) {
        ControlPanelContentView controlPanelContentView;
        if (this.mControlPanelWindowView != null) {
            int updateFrom = this.mConfiguration.updateFrom(configuration);
            boolean isThemeResourcesChanged = CommonUtil.isThemeResourcesChanged(updateFrom, configuration.extraConfig.themeChangedFlags);
            boolean z = true;
            boolean z2 = (1073741824 & updateFrom) != 0;
            boolean z3 = (updateFrom & 4) != 0;
            boolean z4 = (updateFrom & 512) != 0;
            if ((updateFrom & 2048) == 0) {
                z = false;
            }
            if ((isThemeResourcesChanged && !z4) || z2 || z3 || z) {
                reCreateWindow();
            } else if (z4 && (controlPanelContentView = this.mControlPanelContentView) != null) {
                controlPanelContentView.updateResources();
            }
        }
    }

    @Override // com.android.systemui.statusbar.policy.ConfigurationController.ConfigurationListener
    public void onDensityOrFontScaleChanged() {
        if (isCollapsed()) {
            reCreateWindow();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void animateCollapsePanels(int i, boolean z) {
        if (this.mControlPanelWindowView != null && !isCollapsed()) {
            collapse(true);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void animateExpandSettingsPanel(String str) {
        if (this.mControlPanelWindowView != null && isCollapsed()) {
            openPanel();
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void addQsTile(ComponentName componentName) {
        this.mQSControlTileHost.addTile(componentName);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void remQsTile(ComponentName componentName) {
        this.mQSControlTileHost.removeTile(componentName);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void clickTile(ComponentName componentName) {
        ControlPanelContentView controlPanelContentView = this.mControlPanelContentView;
        if (controlPanelContentView != null && controlPanelContentView.getControlCenterPanel() != null) {
            ((ControlCenterPanelView) this.mControlPanelContentView.getControlCenterPanel()).clickTile(componentName);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void disable(int i, int i2, int i3, boolean z) {
        if (i == this.mDisplayId) {
            int i4 = this.mDisabled1;
            int i5 = i2 ^ i4;
            this.mDisabled1 = i2;
            int i6 = this.mDisabled2;
            int i7 = i3 ^ i6;
            this.mDisabled2 = i3;
            if (DEBUG) {
                Log.d("ControlCenter", String.format("disable1: 0x%08x -> 0x%08x (diff1: 0x%08x)", Integer.valueOf(i4), Integer.valueOf(i2), Integer.valueOf(i5)));
                Log.d("ControlCenter", String.format("disable2: 0x%08x -> 0x%08x (diff2: 0x%08x)", Integer.valueOf(i6), Integer.valueOf(i3), Integer.valueOf(i7)));
            }
            if ((i5 & 65536) != 0 && (65536 & i2) != 0) {
                collapse(true);
            }
        }
    }

    public boolean panelEnabled() {
        return (this.mDisabled1 & 65536) == 0 && (this.mDisabled2 & 4) == 0 && !ONLY_CORE_APPS;
    }

    public void onUserSwitched(int i) {
        this.mExpandInfoController.onUserSwitched();
        this.mControlPanelWindowView.onUserSwitched(i);
    }

    private void reCreateWindow() {
        if (this.mUseControlCenter) {
            if (!isCollapsed()) {
                collapse(true);
            }
            removeControlPanelWindow();
            addControlPanelWindow();
        }
    }

    /* access modifiers changed from: protected */
    public void addControlPanelWindow() {
        this.mControlCenterActivityStarter.setControlCenter(this);
        this.mControlPanelController.setControlCenter(this);
        this.mSuperSaveModeController.addCallback((SuperSaveModeController.SuperSaveModeChangeListener) this);
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
        ControlPanelWindowView controlPanelWindowView = (ControlPanelWindowView) this.mInjectionInflationController.injectable(LayoutInflater.from(this.mContext)).inflate(C0017R$layout.control_panel, (ViewGroup) null);
        this.mControlPanelWindowView = controlPanelWindowView;
        controlPanelWindowView.setControlCenter(this);
        this.mControlPanelWindowManager.addControlPanel(this.mControlPanelWindowView);
        register();
        ControlPanelContentView controlPanelContentView = (ControlPanelContentView) this.mControlPanelWindowView.findViewById(C0015R$id.control_panel_content);
        this.mControlPanelContentView = controlPanelContentView;
        if (controlPanelContentView != null) {
            controlPanelContentView.setHost(this.mQSControlTileHost);
        }
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void preloadRecentApps() {
        collapse(false);
    }

    /* access modifiers changed from: protected */
    public void removeControlPanelWindow() {
        if (this.mControlPanelWindowView != null) {
            this.mControlCenterActivityStarter.setControlCenter(null);
            this.mControlPanelController.setControlCenter(null);
            this.mCommandQueue.removeCallback((CommandQueue.Callbacks) this);
            this.mSuperSaveModeController.removeCallback((SuperSaveModeController.SuperSaveModeChangeListener) this);
            unregister();
            this.mControlPanelWindowManager.removeControlPanel();
            this.mControlPanelWindowView = null;
        }
    }

    /* access modifiers changed from: protected */
    public void register() {
        this.mExpandInfoController.register();
    }

    /* access modifiers changed from: protected */
    public void unregister() {
        this.mExpandInfoController.unregister();
    }

    public boolean isExpandable() {
        return this.mControlPanelController.isExpandable();
    }

    public void collapse(boolean z) {
        StatusBar statusBar = this.mStatusBar;
        if (statusBar != null && !statusBar.isQSFullyCollapsed()) {
            this.mStatusBar.postAnimateCollapsePanels();
        }
        collapseControlCenter(z);
    }

    public void collapseControlCenter(boolean z) {
        if (!isCollapsed()) {
            this.mHandler.removeMessages(1);
            Message obtainMessage = this.mHandler.obtainMessage();
            obtainMessage.what = 1;
            obtainMessage.obj = Boolean.valueOf(z);
            this.mHandler.sendMessage(obtainMessage);
        }
    }

    public boolean isCollapsed() {
        ControlPanelWindowView controlPanelWindowView = this.mControlPanelWindowView;
        if (controlPanelWindowView != null) {
            return controlPanelWindowView.isCollapsed();
        }
        return true;
    }

    public void openPanel() {
        if (this.mControlPanelController.isExpandable()) {
            this.mHandler.removeMessages(2);
            Message obtainMessage = this.mHandler.obtainMessage();
            obtainMessage.what = 2;
            this.mHandler.sendMessage(obtainMessage);
        }
    }

    public void openPanelImmediately() {
        handleOpenPanel();
    }

    public void handleCollapsePanel(boolean z) {
        ControlPanelWindowManager controlPanelWindowManager = this.mControlPanelWindowManager;
        if (controlPanelWindowManager != null) {
            controlPanelWindowManager.collapsePanel(z);
        }
    }

    public void handleCollapsePanel(boolean z, boolean z2) {
        ControlPanelWindowManager controlPanelWindowManager = this.mControlPanelWindowManager;
        if (controlPanelWindowManager != null) {
            controlPanelWindowManager.collapsePanel(z, z2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleOpenPanel() {
        if (this.mControlPanelWindowView != null && panelEnabled()) {
            this.mControlPanelWindowView.expandPanel();
        }
    }

    public void startActivityDismissingKeyguard(Intent intent) {
        collapse(true);
        this.mStatusBarActivityStarter.startActivity(intent, true);
    }

    public void postStartActivityDismissingKeyguard(Intent intent) {
        this.mHandler.post(new Runnable() {
            /* class com.android.systemui.controlcenter.ControlCenter.AnonymousClass1 */

            public void run() {
                ControlCenter.this.handleCollapsePanel(true);
            }
        });
        this.mStatusBarActivityStarter.postStartActivityDismissingKeyguard(intent, 350);
    }

    @Override // com.android.systemui.controlcenter.phone.ControlPanelController.UseControlPanelChangeListener
    public void onUseControlPanelChange(boolean z) {
        if (this.mUseControlCenter != z) {
            this.mUseControlCenter = z;
            this.mQSControlTileHost.getHostInjector().switchControlCenter(z);
            if (z) {
                addControlPanelWindow();
            } else {
                removeControlPanelWindow();
            }
        }
    }

    @Override // com.android.systemui.controlcenter.policy.SuperSaveModeController.SuperSaveModeChangeListener
    public void onSuperSaveModeChange(boolean z) {
        if (this.mSuperPowerModeOn != z) {
            this.mSuperPowerModeOn = z;
            this.mControlPanelController.setSuperPowerMode(z);
            this.mExpandInfoController.setSuperPowerMode(z);
            reCreateWindow();
        }
    }

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            int i = message.what;
            if (i == 1) {
                ControlCenter.this.handleCollapsePanel(((Boolean) message.obj).booleanValue());
            } else if (i == 2) {
                ControlCenter.this.handleOpenPanel();
            }
        }
    }

    public void refreshAllTiles() {
        ControlPanelWindowView controlPanelWindowView = this.mControlPanelWindowView;
        if (controlPanelWindowView != null) {
            controlPanelWindowView.refreshAllTiles();
        }
    }
}

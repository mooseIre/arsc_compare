package com.android.systemui.miui.statusbar;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.Application;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.SystemUIFactory;
import com.android.systemui.Util;
import com.android.systemui.content.pm.PackageManagerCompat;
import com.android.systemui.events.ScreenOffEvent;
import com.android.systemui.miui.ToastOverlayManager;
import com.android.systemui.miui.controlcenter.ExpandInfoController;
import com.android.systemui.miui.controlcenter.QSControlCenterPanel;
import com.android.systemui.miui.controlcenter.QSControlTileHost;
import com.android.systemui.miui.statusbar.phone.ControlPanelContentView;
import com.android.systemui.miui.statusbar.phone.ControlPanelWindowManager;
import com.android.systemui.miui.statusbar.phone.ControlPanelWindowView;
import com.android.systemui.miui.statusbar.policy.ControlPanelController;
import com.android.systemui.miui.statusbar.policy.SuperSaveModeController;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.miui.systemui.annotation.Inject;

public class ControlCenter extends SystemUI implements ControlPanelController.UseControlPanelChangeListener, SuperSaveModeController.SuperSaveModeChangeListener, CommandQueue.Callbacks {
    public static final boolean DEBUG = Constants.DEBUG;
    private static final boolean ONLY_CORE_APPS;
    protected BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(intent.getAction())) {
                ControlCenter.this.collapse(true);
            }
        }
    };
    private CommandQueue mCommandQueue;
    private Configuration mConfiguration;
    private ControlPanelContentView mControlPanelContentView;
    protected ControlPanelWindowManager mControlPanelWindowManager;
    protected ControlPanelWindowView mControlPanelWindowView;
    private int mDisabled1 = 0;
    private int mDisabled2 = 0;
    private ExpandInfoController mExpandInfoController;
    private Handler mHandler = new H();
    @Inject
    protected StatusBarIconController mIconController;
    private ControlPanelController mPanelController;
    private QSControlTileHost mQSControlTileHost;
    protected StatusBar mStatusBar;
    private ActivityStarter mStatusBarActivityStarter;
    private boolean mSuperPowerModeOn;
    protected boolean mUseControlCenter = false;

    static {
        boolean z = false;
        try {
            IPackageManager asInterface = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            boolean isOnlyCoreApps = asInterface.isOnlyCoreApps();
            PackageManagerCompat.hasSystemFeature(asInterface, "android.software.freeform_window_management", 0);
            z = isOnlyCoreApps;
        } catch (RemoteException unused) {
        }
        ONLY_CORE_APPS = z;
    }

    public void start() {
        Class cls = ControlPanelController.class;
        ((ControlPanelController) Dependency.get(cls)).addCallback((ControlPanelController.UseControlPanelChangeListener) this);
        this.mExpandInfoController = (ExpandInfoController) Dependency.get(ExpandInfoController.class);
        this.mPanelController = (ControlPanelController) Dependency.get(cls);
        this.mCommandQueue = (CommandQueue) getComponent(CommandQueue.class);
        this.mControlPanelWindowManager = (ControlPanelWindowManager) Dependency.get(ControlPanelWindowManager.class);
        this.mStatusBarActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        this.mConfiguration = new Configuration(this.mContext.getResources().getConfiguration());
    }

    /* access modifiers changed from: protected */
    public void onBootCompleted() {
        super.onBootCompleted();
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        ControlPanelContentView controlPanelContentView;
        if (this.mControlPanelWindowView != null) {
            int updateFrom = this.mConfiguration.updateFrom(configuration);
            boolean isThemeResourcesChanged = Util.isThemeResourcesChanged(updateFrom, configuration.extraConfig.themeChangedFlags);
            boolean z = true;
            boolean z2 = (1073741824 & updateFrom) != 0;
            boolean z3 = (updateFrom & 4) != 0;
            if ((updateFrom & 512) == 0) {
                z = false;
            }
            if ((isThemeResourcesChanged && !z) || z2 || z3) {
                reCreateWindow();
            } else if (z && (controlPanelContentView = this.mControlPanelContentView) != null) {
                controlPanelContentView.updateResources();
            }
        }
    }

    public void animateCollapsePanels(int i) {
        if (this.mControlPanelWindowView != null && !isCollapsed()) {
            collapse(true);
        }
    }

    public void animateExpandSettingsPanel(String str) {
        if (this.mControlPanelWindowView != null && isCollapsed()) {
            openPanel();
        }
    }

    public void addQsTile(ComponentName componentName) {
        this.mQSControlTileHost.addTile(componentName);
    }

    public void remQsTile(ComponentName componentName) {
        this.mQSControlTileHost.removeTile(componentName);
    }

    public void clickTile(ComponentName componentName) {
        ControlPanelContentView controlPanelContentView = this.mControlPanelContentView;
        if (controlPanelContentView != null && controlPanelContentView.getControlCenterPanel() != null) {
            ((QSControlCenterPanel) this.mControlPanelContentView.getControlCenterPanel()).clickTile(componentName);
        }
    }

    public void disable(int i, int i2, boolean z) {
        int i3 = this.mDisabled1;
        int i4 = i ^ i3;
        this.mDisabled1 = i;
        int i5 = this.mDisabled2;
        int i6 = i2 ^ i5;
        this.mDisabled2 = i2;
        if (DEBUG) {
            Log.d("ControlCenter", String.format("disable1: 0x%08x -> 0x%08x (diff1: 0x%08x)", new Object[]{Integer.valueOf(i3), Integer.valueOf(i), Integer.valueOf(i4)}));
            Log.d("ControlCenter", String.format("disable2: 0x%08x -> 0x%08x (diff2: 0x%08x)", new Object[]{Integer.valueOf(i5), Integer.valueOf(i2), Integer.valueOf(i6)}));
        }
        if ((i4 & 65536) != 0 && (i & 65536) != 0) {
            collapse(true);
        }
    }

    public boolean panelEnabled() {
        return (this.mDisabled1 & 65536) == 0 && (this.mDisabled2 & 4) == 0 && !ONLY_CORE_APPS;
    }

    public void onUserSwitched(int i) {
        ((ExpandInfoController) Dependency.get(ExpandInfoController.class)).onUserSwitched();
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
        ((ControlCenterActivityStarter) Dependency.get(ControlCenterActivityStarter.class)).setControlCenter(this);
        ((ControlPanelController) Dependency.get(ControlPanelController.class)).setControlCenter(this);
        ((SuperSaveModeController) Dependency.get(SuperSaveModeController.class)).addCallback((SuperSaveModeController.SuperSaveModeChangeListener) this);
        this.mCommandQueue.addCallbacks(this);
        ControlPanelWindowView controlPanelWindowView = (ControlPanelWindowView) View.inflate(this.mContext, R.layout.control_panel, (ViewGroup) null);
        this.mControlPanelWindowView = controlPanelWindowView;
        controlPanelWindowView.setControlCenter(this);
        this.mControlPanelWindowManager.addControlPanel(this.mControlPanelWindowView);
        StatusBar statusBar = (StatusBar) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
        if (statusBar != null) {
            statusBar.getStatusBarWindow().setControlPanel(this.mControlPanelWindowView);
        }
        register();
        ((ToastOverlayManager) Dependency.get(ToastOverlayManager.class)).setup(this.mContext, this.mControlPanelWindowView);
        ControlPanelContentView controlPanelContentView = (ControlPanelContentView) this.mControlPanelWindowView.findViewById(R.id.control_panel_content);
        this.mControlPanelContentView = controlPanelContentView;
        if (controlPanelContentView != null) {
            QSControlTileHost createQSControlTileHost = SystemUIFactory.getInstance().createQSControlTileHost(this.mContext, statusBar, this.mIconController);
            this.mQSControlTileHost = createQSControlTileHost;
            createQSControlTileHost.init();
            this.mControlPanelContentView.setHost(this.mQSControlTileHost);
        }
        RecentsEventBus.getDefault().register(this);
    }

    /* access modifiers changed from: protected */
    public void removeControlPanelWindow() {
        if (this.mControlPanelWindowView != null) {
            ((ControlCenterActivityStarter) Dependency.get(ControlCenterActivityStarter.class)).setControlCenter((ControlCenter) null);
            ((ControlPanelController) Dependency.get(ControlPanelController.class)).setControlCenter((ControlCenter) null);
            this.mCommandQueue.removeCallbacks(this);
            ((SuperSaveModeController) Dependency.get(SuperSaveModeController.class)).removeCallback((SuperSaveModeController.SuperSaveModeChangeListener) this);
            QSControlTileHost qSControlTileHost = this.mQSControlTileHost;
            if (qSControlTileHost != null) {
                qSControlTileHost.destroy();
            }
            unregister();
            ((ToastOverlayManager) Dependency.get(ToastOverlayManager.class)).clear(this.mContext, this.mControlPanelWindowView);
            StatusBar statusBar = (StatusBar) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
            if (statusBar != null) {
                statusBar.getStatusBarWindow().setControlPanel((ControlPanelWindowView) null);
            }
            this.mControlPanelWindowManager.removeControlPanel();
            this.mControlPanelWindowView = null;
            RecentsEventBus.getDefault().unregister(this);
        }
    }

    public final void onBusEvent(ScreenOffEvent screenOffEvent) {
        collapse(false);
    }

    /* access modifiers changed from: protected */
    public void register() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        this.mContext.registerReceiverAsUser(this.mBroadcastReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
    }

    /* access modifiers changed from: protected */
    public void unregister() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
    }

    public void collapse(boolean z) {
        StatusBar statusBar = this.mStatusBar;
        if (statusBar != null && !statusBar.isQSFullyCollapsed()) {
            this.mStatusBar.collapsePanels();
        }
        this.mHandler.removeMessages(1);
        Message obtainMessage = this.mHandler.obtainMessage();
        obtainMessage.what = 1;
        obtainMessage.obj = Boolean.valueOf(z);
        this.mHandler.sendMessage(obtainMessage);
    }

    public boolean isCollapsed() {
        ControlPanelWindowView controlPanelWindowView = this.mControlPanelWindowView;
        if (controlPanelWindowView != null) {
            return controlPanelWindowView.isCollapsed();
        }
        return true;
    }

    public void openPanel() {
        this.mHandler.removeMessages(2);
        Message obtainMessage = this.mHandler.obtainMessage();
        obtainMessage.what = 2;
        this.mHandler.sendMessage(obtainMessage);
    }

    /* access modifiers changed from: private */
    public void handleCollapsePanel(boolean z) {
        ControlPanelWindowManager controlPanelWindowManager = this.mControlPanelWindowManager;
        if (controlPanelWindowManager != null) {
            controlPanelWindowManager.collapsePanel(z);
        }
    }

    /* access modifiers changed from: private */
    public void handleOpenPanel() {
        if (this.mControlPanelWindowView != null && panelEnabled()) {
            this.mControlPanelWindowView.expandPanel();
        }
    }

    public void postStartActivityDismissingKeyguard(Intent intent) {
        this.mHandler.post(new Runnable() {
            public void run() {
                ControlCenter.this.handleCollapsePanel(true);
            }
        });
        this.mStatusBarActivityStarter.postStartActivityDismissingKeyguard(intent, 350);
    }

    public void onUseControlPanelChange(boolean z) {
        if (this.mUseControlCenter != z) {
            this.mUseControlCenter = z;
            StatusBar statusBar = (StatusBar) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
            if (z) {
                if (statusBar != null) {
                    statusBar.updateQSTileHost(z);
                }
                addControlPanelWindow();
                return;
            }
            removeControlPanelWindow();
            if (statusBar != null) {
                statusBar.updateQSTileHost(z);
            }
        }
    }

    public void onSuperSaveModeChange(boolean z) {
        if (this.mSuperPowerModeOn != z) {
            this.mSuperPowerModeOn = z;
            this.mPanelController.setSuperPowerMode(z);
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
}

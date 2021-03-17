package com.android.systemui.globalactions;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.SystemUI;
import com.android.systemui.plugins.GlobalActions;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.ExtensionController;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.inject.Provider;

public class GlobalActionsComponent extends SystemUI implements CommandQueue.Callbacks, GlobalActions.GlobalActionsManager {
    private IStatusBarService mBarService;
    private final CommandQueue mCommandQueue;
    private ExtensionController.Extension<GlobalActions> mExtension;
    private final ExtensionController mExtensionController;
    private final Provider<GlobalActions> mGlobalActionsProvider;
    private GlobalActions mPlugin;
    private StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;

    public GlobalActionsComponent(Context context, CommandQueue commandQueue, ExtensionController extensionController, Provider<GlobalActions> provider, StatusBarKeyguardViewManager statusBarKeyguardViewManager) {
        super(context);
        this.mCommandQueue = commandQueue;
        this.mExtensionController = extensionController;
        this.mGlobalActionsProvider = provider;
        this.mStatusBarKeyguardViewManager = statusBarKeyguardViewManager;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        ExtensionController.ExtensionBuilder newExtension = this.mExtensionController.newExtension(GlobalActions.class);
        newExtension.withPlugin(GlobalActions.class);
        Provider<GlobalActions> provider = this.mGlobalActionsProvider;
        Objects.requireNonNull(provider);
        newExtension.withDefault(new Supplier() {
            /* class com.android.systemui.globalactions.$$Lambda$_JK3806lHhx7U5FWWTWu23JrO_A */

            @Override // java.util.function.Supplier
            public final Object get() {
                return (GlobalActions) Provider.this.get();
            }
        });
        newExtension.withCallback(new Consumer() {
            /* class com.android.systemui.globalactions.$$Lambda$GlobalActionsComponent$bGplH0pcKhfpL1pOMBpgWKJntvw */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                GlobalActionsComponent.this.onExtensionCallback((GlobalActions) obj);
            }
        });
        ExtensionController.Extension<GlobalActions> build = newExtension.build();
        this.mExtension = build;
        this.mPlugin = build.get();
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    /* access modifiers changed from: private */
    public void onExtensionCallback(GlobalActions globalActions) {
        GlobalActions globalActions2 = this.mPlugin;
        if (globalActions2 != null) {
            globalActions2.destroy();
        }
        this.mPlugin = globalActions;
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void handleShowShutdownUi(boolean z, String str) {
        this.mExtension.get().showShutdownUi(z, str);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void handleShowGlobalActionsMenu() {
        this.mStatusBarKeyguardViewManager.setGlobalActionsVisible(true);
        this.mExtension.get().showGlobalActions(this);
    }

    @Override // com.android.systemui.plugins.GlobalActions.GlobalActionsManager
    public void onGlobalActionsShown() {
        try {
            this.mBarService.onGlobalActionsShown();
        } catch (RemoteException unused) {
        }
    }

    @Override // com.android.systemui.plugins.GlobalActions.GlobalActionsManager
    public void onGlobalActionsHidden() {
        try {
            this.mStatusBarKeyguardViewManager.setGlobalActionsVisible(false);
            this.mBarService.onGlobalActionsHidden();
        } catch (RemoteException unused) {
        }
    }

    @Override // com.android.systemui.plugins.GlobalActions.GlobalActionsManager
    public void shutdown() {
        try {
            this.mBarService.shutdown();
        } catch (RemoteException unused) {
        }
    }

    @Override // com.android.systemui.plugins.GlobalActions.GlobalActionsManager
    public void reboot(boolean z) {
        try {
            this.mBarService.reboot(z);
        } catch (RemoteException unused) {
        }
    }
}

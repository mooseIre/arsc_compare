package com.android.systemui.statusbar;

import android.app.Fragment;
import android.content.Context;
import android.hardware.display.DisplayManager;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.IWindowManager;
import android.view.View;
import android.view.WindowManagerGlobal;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.statusbar.RegisterStatusBarResult;
import com.android.systemui.Dependency;
import com.android.systemui.assist.AssistHandleViewController;
import com.android.systemui.fragments.FragmentHostManager;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.AutoHideController;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.NavigationBarFragment;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NavigationModeController;
import com.android.systemui.statusbar.phone.NavigationModeControllerExt;
import com.android.systemui.statusbar.policy.BatteryController;

public class NavigationBarController implements CommandQueue.Callbacks {
    private static final String TAG = "NavigationBarController";
    private final Context mContext;
    private final DisplayManager mDisplayManager;
    private final Handler mHandler;
    @VisibleForTesting
    SparseArray<NavigationBarFragment> mNavigationBars = new SparseArray<>();

    public NavigationBarController(Context context, Handler handler, CommandQueue commandQueue) {
        this.mContext = context;
        this.mHandler = handler;
        this.mDisplayManager = (DisplayManager) context.getSystemService("display");
        commandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    public void onDisplayRemoved(int i) {
        removeNavigationBar(i);
    }

    public void onDisplayReady(int i) {
        createNavigationBar(this.mDisplayManager.getDisplay(i), (RegisterStatusBarResult) null);
    }

    public void createNavigationBars(boolean z, RegisterStatusBarResult registerStatusBarResult) {
        for (Display display : this.mDisplayManager.getDisplays()) {
            if (z || display.getDisplayId() != 0) {
                createNavigationBar(display, registerStatusBarResult);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void createNavigationBar(Display display, RegisterStatusBarResult registerStatusBarResult) {
        Context context;
        if (display != null) {
            int displayId = display.getDisplayId();
            boolean z = displayId == 0;
            try {
                if (WindowManagerGlobal.getWindowManagerService().hasNavigationBar(displayId) && !NavigationModeControllerExt.INSTANCE.hideNavigationBar()) {
                    if (z) {
                        context = this.mContext;
                    } else {
                        context = this.mContext.createDisplayContext(display);
                    }
                    Context context2 = context;
                    NavigationBarFragment.create(context2, new FragmentHostManager.FragmentListener(z, context2, displayId, registerStatusBarResult, display) {
                        public final /* synthetic */ boolean f$1;
                        public final /* synthetic */ Context f$2;
                        public final /* synthetic */ int f$3;
                        public final /* synthetic */ RegisterStatusBarResult f$4;
                        public final /* synthetic */ Display f$5;

                        {
                            this.f$1 = r2;
                            this.f$2 = r3;
                            this.f$3 = r4;
                            this.f$4 = r5;
                            this.f$5 = r6;
                        }

                        public final void onFragmentViewCreated(String str, Fragment fragment) {
                            NavigationBarController.this.lambda$createNavigationBar$0$NavigationBarController(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, str, fragment);
                        }
                    });
                }
            } catch (RemoteException unused) {
                Log.w(TAG, "Cannot get WindowManager.");
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$createNavigationBar$0 */
    public /* synthetic */ void lambda$createNavigationBar$0$NavigationBarController(boolean z, Context context, int i, RegisterStatusBarResult registerStatusBarResult, Display display, String str, Fragment fragment) {
        LightBarController lightBarController;
        AutoHideController autoHideController;
        NavigationBarFragment navigationBarFragment = (NavigationBarFragment) fragment;
        if (z) {
            lightBarController = (LightBarController) Dependency.get(LightBarController.class);
        } else {
            lightBarController = new LightBarController(context, (DarkIconDispatcher) Dependency.get(DarkIconDispatcher.class), (BatteryController) Dependency.get(BatteryController.class), (NavigationModeController) Dependency.get(NavigationModeController.class));
        }
        navigationBarFragment.setLightBarController(lightBarController);
        if (z) {
            autoHideController = (AutoHideController) Dependency.get(AutoHideController.class);
        } else {
            autoHideController = new AutoHideController(context, this.mHandler, (IWindowManager) Dependency.get(IWindowManager.class));
        }
        navigationBarFragment.setAutoHideController(autoHideController);
        navigationBarFragment.restoreAppearanceAndTransientState();
        this.mNavigationBars.append(i, navigationBarFragment);
        if (registerStatusBarResult != null) {
            navigationBarFragment.setImeWindowStatus(display.getDisplayId(), registerStatusBarResult.mImeToken, registerStatusBarResult.mImeWindowVis, registerStatusBarResult.mImeBackDisposition, registerStatusBarResult.mShowImeSwitcher);
        }
    }

    private void removeNavigationBar(int i) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment != null) {
            navigationBarFragment.setAutoHideController((AutoHideController) null);
            View rootView = navigationBarFragment.getView().getRootView();
            WindowManagerGlobal.getInstance().removeView(rootView, true);
            FragmentHostManager.removeAndDestroy(rootView);
            this.mNavigationBars.remove(i);
        }
    }

    public void checkNavBarModes(int i) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment != null) {
            navigationBarFragment.checkNavBarModes();
        }
    }

    public void finishBarAnimations(int i) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment != null) {
            navigationBarFragment.finishBarAnimations();
        }
    }

    public void touchAutoDim(int i) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment != null) {
            navigationBarFragment.touchAutoDim();
        }
    }

    public void transitionTo(int i, int i2, boolean z) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment != null) {
            navigationBarFragment.transitionTo(i2, z);
        }
    }

    public void disableAnimationsDuringHide(int i, long j) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment != null) {
            navigationBarFragment.disableAnimationsDuringHide(j);
        }
    }

    public NavigationBarView getDefaultNavigationBarView() {
        return getNavigationBarView(0);
    }

    public NavigationBarView getNavigationBarView(int i) {
        NavigationBarFragment navigationBarFragment = this.mNavigationBars.get(i);
        if (navigationBarFragment == null) {
            return null;
        }
        return (NavigationBarView) navigationBarFragment.getView();
    }

    public NavigationBarFragment getDefaultNavigationBarFragment() {
        return this.mNavigationBars.get(0);
    }

    public AssistHandleViewController getAssistHandlerViewController() {
        NavigationBarFragment defaultNavigationBarFragment = getDefaultNavigationBarFragment();
        if (defaultNavigationBarFragment == null) {
            return null;
        }
        return defaultNavigationBarFragment.getAssistHandlerViewController();
    }

    public void addDefaultNavigationBar() {
        if (this.mNavigationBars.get(0) == null) {
            createNavigationBar(this.mDisplayManager.getDisplay(0), (RegisterStatusBarResult) null);
        }
    }

    public void removeDefaultNavigationBar() {
        removeNavigationBar(0);
    }
}

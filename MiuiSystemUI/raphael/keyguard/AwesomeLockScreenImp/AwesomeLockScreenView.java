package com.android.keyguard.AwesomeLockScreenImp;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.IWindowManager;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.statusbar.phone.NotificationPanelViewController;
import java.util.List;
import miui.maml.component.MamlView;

public class AwesomeLockScreenView extends MamlView {
    private final AccessibilityManager.AccessibilityServicesStateChangeListener mAccessibilityListener = new AccessibilityManager.AccessibilityServicesStateChangeListener() {
        public void onAccessibilityServicesStateChanged(AccessibilityManager accessibilityManager) {
            AwesomeLockScreenView.this.updateAccessibilityServicesState(accessibilityManager);
        }
    };
    private AccessibilityManager mAccessibilityManager = ((AccessibilityManager) this.mContext.getSystemService("accessibility"));
    private boolean mAccessibilityServiceEnabled = false;
    /* access modifiers changed from: private */
    public boolean mAccessibleNodeAdded = false;
    private Runnable mAddAccessibleNodeRunnable = new Runnable() {
        public void run() {
            if (AccessibleElementRoot.isFileExists()) {
                LockScreenRoot root = AwesomeLockScreenView.this.getRoot();
                AccessibleElementRoot unused = AwesomeLockScreenView.this.mVirtualRoot = new AccessibleElementRoot(AwesomeLockScreenView.this.mContext, root);
                if (AwesomeLockScreenView.this.mVirtualRoot.isInited()) {
                    Log.d("AwesomeLockScreenView", "try to use virtual accessible nodes for 3rd lockscreen");
                    root.removeAllAccessibleElements();
                    root.addAccessibleList(AwesomeLockScreenView.this.mVirtualRoot.getAccessibleElements());
                    boolean unused2 = AwesomeLockScreenView.this.mAccessibleNodeAdded = true;
                }
            }
        }
    };
    private boolean mHasNavigationBar;
    private NotificationPanelViewController mPanelViewController;
    private boolean mPaused = false;
    /* access modifiers changed from: private */
    public AccessibleElementRoot mVirtualRoot;

    public AwesomeLockScreenView(Context context, LockScreenRoot lockScreenRoot) {
        super(context, lockScreenRoot);
        initInner();
        updateAccessibilityServicesState(this.mAccessibilityManager);
    }

    public void pause() {
        this.mPaused = true;
        onPause();
    }

    public void resume() {
        this.mPaused = false;
        onResume();
    }

    private void initInner() {
        try {
            this.mHasNavigationBar = IWindowManager.Stub.asInterface(ServiceManager.getService("window")).hasNavigationBar(getContext().getDisplayId());
        } catch (RemoteException unused) {
        }
    }

    private void tryToAddAccessibleNode() {
        if (this.mAccessibilityServiceEnabled && !this.mAccessibleNodeAdded && !getRoot().isSupportAccessibilityService() && !BackgroundThread.getHandler().hasCallbacks(this.mAddAccessibleNodeRunnable)) {
            BackgroundThread.getHandler().post(this.mAddAccessibleNodeRunnable);
        }
    }

    /* access modifiers changed from: private */
    public void updateAccessibilityServicesState(AccessibilityManager accessibilityManager) {
        List<AccessibilityServiceInfo> enabledAccessibilityServiceList = accessibilityManager.getEnabledAccessibilityServiceList(-1);
        boolean z = enabledAccessibilityServiceList != null && enabledAccessibilityServiceList.size() > 0;
        if (this.mAccessibilityServiceEnabled != z) {
            this.mAccessibilityServiceEnabled = z;
            tryToAddAccessibleNode();
        }
    }

    public void setPanelView(NotificationPanelViewController notificationPanelViewController) {
        this.mPanelViewController = notificationPanelViewController;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        int actionMasked;
        NotificationPanelViewController notificationPanelViewController = this.mPanelViewController;
        if (notificationPanelViewController != null && notificationPanelViewController.isInSettings()) {
            return false;
        }
        if (motionEvent.getPointerCount() > 1) {
            motionEvent.setAction(3);
            Log.d("AwesomeLockScreenView", "touch point count > 1, set to ACTION_CANCEL");
        } else if (this.mHasNavigationBar && ((actionMasked = motionEvent.getActionMasked()) == 3 || actionMasked == 4)) {
            motionEvent.setAction(1);
        }
        return AwesomeLockScreenView.super.onTouchEvent(motionEvent);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        AwesomeLockScreenView.super.onAttachedToWindow();
        if (this.mPaused) {
            onPause();
        }
        this.mAccessibilityManager.addAccessibilityServicesStateChangeListener(this.mAccessibilityListener, (Handler) null);
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        AwesomeLockScreenView.super.onDetachedFromWindow();
        this.mAccessibilityManager.removeAccessibilityServicesStateChangeListener(this.mAccessibilityListener);
    }

    public void rebindRoot() {
        init();
    }

    public void finishRoot() {
        BackgroundThread.getHandler().removeCallbacks(this.mAddAccessibleNodeRunnable);
        AccessibleElementRoot accessibleElementRoot = this.mVirtualRoot;
        if (accessibleElementRoot != null && accessibleElementRoot.isInited()) {
            this.mVirtualRoot.finish();
            this.mVirtualRoot.setKeepResource(true);
        }
        getRoot().finish();
    }
}

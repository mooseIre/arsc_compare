package com.android.systemui.pip.phone;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ParceledListSlice;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.DisplayInfo;
import android.view.IPinnedStackController;
import android.view.IPinnedStackListener;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.systemui.pip.BasePipManager;
import com.android.systemui.pip.phone.PipManager;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.component.ExpandPipEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import java.io.PrintWriter;
import java.util.Objects;

public class PipManager implements BasePipManager {
    private static PipManager sPipController;
    /* access modifiers changed from: private */
    public IActivityManager mActivityManager;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler();
    private InputConsumerController mInputConsumerController;
    /* access modifiers changed from: private */
    public PipMediaController mMediaController;
    /* access modifiers changed from: private */
    public PipMenuActivityController mMenuController;
    /* access modifiers changed from: private */
    public PipNotificationController mNotificationController;
    private final PinnedStackListener mPinnedStackListener = new PinnedStackListener();
    SystemServicesProxy.TaskStackListener mTaskStackListener = new SystemServicesProxy.TaskStackListener() {
        public void onActivityPinned(String str, int i, int i2) {
            if (checkCurrentUserId(PipManager.this.mContext, false)) {
                PipManager.this.mTouchHandler.onActivityPinned();
                PipManager.this.mMediaController.onActivityPinned();
                PipManager.this.mMenuController.onActivityPinned();
                PipManager.this.mNotificationController.onActivityPinned(str, true);
                SystemServicesProxy.getInstance(PipManager.this.mContext).setPipVisibility(true);
            }
        }

        public void onActivityUnpinned() {
            boolean z = false;
            if (checkCurrentUserId(PipManager.this.mContext, false)) {
                ComponentName topPinnedActivity = PipUtils.getTopPinnedActivity(PipManager.this.mContext, PipManager.this.mActivityManager);
                PipManager.this.mMenuController.hideMenu();
                PipManager.this.mNotificationController.onActivityUnpinned(topPinnedActivity);
                SystemServicesProxy instance = SystemServicesProxy.getInstance(PipManager.this.mContext);
                if (topPinnedActivity != null) {
                    z = true;
                }
                instance.setPipVisibility(z);
            }
        }

        public void onPinnedStackAnimationStarted() {
            PipManager.this.mTouchHandler.setTouchEnabled(false);
        }

        public void onPinnedStackAnimationEnded() {
            PipManager.this.mTouchHandler.setTouchEnabled(true);
            PipManager.this.mTouchHandler.onPinnedStackAnimationEnded();
            PipManager.this.mMenuController.onPinnedStackAnimationEnded();
            PipManager.this.mNotificationController.onPinnedStackAnimationEnded();
        }

        public void onPinnedActivityRestartAttempt(boolean z) {
            if (checkCurrentUserId(PipManager.this.mContext, false)) {
                PipManager.this.mTouchHandler.getMotionHelper().expandPip(z);
            }
        }
    };
    /* access modifiers changed from: private */
    public PipTouchHandler mTouchHandler;
    private IWindowManager mWindowManager;

    private class PinnedStackListener extends IPinnedStackListener.Stub {
        public void onActivityHidden(ComponentName componentName) {
        }

        public void onAspectRatioChanged(float f) {
        }

        public void onConfigurationChanged() {
        }

        public void onDisplayInfoChanged(DisplayInfo displayInfo) {
        }

        public void onMovementBoundsChanged(boolean z) {
        }

        private PinnedStackListener() {
        }

        public void onListenerRegistered(IPinnedStackController iPinnedStackController) {
            PipManager.this.mHandler.post(new Runnable(iPinnedStackController) {
                public final /* synthetic */ IPinnedStackController f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.PinnedStackListener.this.lambda$onListenerRegistered$0$PipManager$PinnedStackListener(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onListenerRegistered$0 */
        public /* synthetic */ void lambda$onListenerRegistered$0$PipManager$PinnedStackListener(IPinnedStackController iPinnedStackController) {
            PipManager.this.mTouchHandler.setPinnedStackController(iPinnedStackController);
        }

        public void onImeVisibilityChanged(boolean z, int i) {
            PipManager.this.mHandler.post(new Runnable(z, i) {
                public final /* synthetic */ boolean f$1;
                public final /* synthetic */ int f$2;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                public final void run() {
                    PipManager.PinnedStackListener.this.lambda$onImeVisibilityChanged$1$PipManager$PinnedStackListener(this.f$1, this.f$2);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onImeVisibilityChanged$1 */
        public /* synthetic */ void lambda$onImeVisibilityChanged$1$PipManager$PinnedStackListener(boolean z, int i) {
            PipManager.this.mTouchHandler.onImeVisibilityChanged(z, i);
        }

        public void onActionsChanged(ParceledListSlice parceledListSlice) {
            PipManager.this.mHandler.post(new Runnable(parceledListSlice) {
                public final /* synthetic */ ParceledListSlice f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    PipManager.PinnedStackListener.this.lambda$onActionsChanged$4$PipManager$PinnedStackListener(this.f$1);
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onActionsChanged$4 */
        public /* synthetic */ void lambda$onActionsChanged$4$PipManager$PinnedStackListener(ParceledListSlice parceledListSlice) {
            PipManager.this.mMenuController.setAppActions(parceledListSlice);
        }
    }

    private PipManager() {
    }

    public void initialize(Context context) {
        this.mContext = context;
        this.mActivityManager = ActivityManagerCompat.getService();
        IWindowManager windowManagerService = WindowManagerGlobal.getWindowManagerService();
        this.mWindowManager = windowManagerService;
        try {
            windowManagerService.registerPinnedStackListener(0, this.mPinnedStackListener);
        } catch (RemoteException e) {
            Log.e("PipManager", "Failed to register pinned stack listener", e);
        }
        SystemServicesProxy.getInstance(this.mContext).registerTaskStackListener(this.mTaskStackListener);
        this.mInputConsumerController = new InputConsumerController(this.mWindowManager);
        PipMediaController pipMediaController = new PipMediaController(context, this.mActivityManager);
        this.mMediaController = pipMediaController;
        PipMenuActivityController pipMenuActivityController = new PipMenuActivityController(context, this.mActivityManager, pipMediaController, this.mInputConsumerController);
        this.mMenuController = pipMenuActivityController;
        this.mTouchHandler = new PipTouchHandler(context, this.mActivityManager, pipMenuActivityController, this.mInputConsumerController);
        this.mNotificationController = new PipNotificationController(context, this.mActivityManager, this.mTouchHandler.getMotionHelper());
        RecentsEventBus.getDefault().register(this);
    }

    public void onConfigurationChanged(Configuration configuration) {
        this.mTouchHandler.onConfigurationChanged();
    }

    public final void onBusEvent(ExpandPipEvent expandPipEvent) {
        Objects.requireNonNull(expandPipEvent);
        try {
            ActivityManager.StackInfo stackInfo = ActivityManagerCompat.getStackInfo(4, 2, 0);
            if (!(stackInfo == null || stackInfo.taskIds == null)) {
                SystemServicesProxy instance = SystemServicesProxy.getInstance(this.mContext);
                for (int cancelThumbnailTransition : stackInfo.taskIds) {
                    instance.cancelThumbnailTransition(cancelThumbnailTransition);
                }
            }
        } catch (Exception unused) {
        }
        this.mTouchHandler.getMotionHelper().expandPip(false);
    }

    public void showPictureInPictureMenu() {
        this.mTouchHandler.showPictureInPictureMenu();
    }

    public static PipManager getInstance() {
        if (sPipController == null) {
            sPipController = new PipManager();
        }
        return sPipController;
    }

    public void dump(PrintWriter printWriter) {
        printWriter.println("PipManager");
        this.mInputConsumerController.dump(printWriter, "  ");
        this.mMenuController.dump(printWriter, "  ");
        this.mTouchHandler.dump(printWriter, "  ");
    }
}

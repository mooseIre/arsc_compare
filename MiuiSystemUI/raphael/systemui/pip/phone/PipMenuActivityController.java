package com.android.systemui.pip.phone;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.ActivityTaskManager;
import android.app.RemoteAction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.view.MotionEvent;
import com.android.systemui.pip.phone.PipMediaController;
import com.android.systemui.pip.phone.PipMenuActivityController;
import com.android.systemui.shared.system.InputConsumerController;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PipMenuActivityController {
    private ParceledListSlice mAppActions;
    private Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class com.android.systemui.pip.phone.PipMenuActivityController.AnonymousClass1 */

        public void handleMessage(Message message) {
            int i = message.what;
            boolean z = true;
            if (i == 100) {
                int i2 = message.arg1;
                if (message.arg2 == 0) {
                    z = false;
                }
                PipMenuActivityController pipMenuActivityController = PipMenuActivityController.this;
                pipMenuActivityController.onMenuStateChanged(i2, z, pipMenuActivityController.getMenuStateChangeFinishedCallback(message.replyTo, (Bundle) message.obj));
            } else if (i == 101) {
                PipMenuActivityController.this.mListeners.forEach($$Lambda$Yf7sZoTIPl0lv58dfbsbQ3za13A.INSTANCE);
            } else if (i == 103) {
                PipMenuActivityController.this.mListeners.forEach($$Lambda$zhx89MCRVbbUuwAz2vBzNfzR3hg.INSTANCE);
            } else if (i == 104) {
                PipMenuActivityController.this.mToActivityMessenger = message.replyTo;
                PipMenuActivityController.this.setStartActivityRequested(false);
                if (PipMenuActivityController.this.mOnAnimationEndRunnable != null) {
                    PipMenuActivityController.this.mOnAnimationEndRunnable.run();
                    PipMenuActivityController.this.mOnAnimationEndRunnable = null;
                }
                if (PipMenuActivityController.this.mToActivityMessenger == null) {
                    if (message.arg1 == 0) {
                        z = false;
                    }
                    PipMenuActivityController.this.onMenuStateChanged(0, z, null);
                }
            } else if (i == 107) {
                PipMenuActivityController.this.mListeners.forEach($$Lambda$ab7bqy0BtiE8EwwZ2rb49JCCbFA.INSTANCE);
            }
        }
    };
    private InputConsumerController mInputConsumerController;
    private ArrayList<Listener> mListeners = new ArrayList<>();
    private PipMediaController.ActionListener mMediaActionListener = new PipMediaController.ActionListener() {
        /* class com.android.systemui.pip.phone.PipMenuActivityController.AnonymousClass2 */

        @Override // com.android.systemui.pip.phone.PipMediaController.ActionListener
        public void onMediaActionsChanged(List<RemoteAction> list) {
            PipMenuActivityController.this.mMediaActions = new ParceledListSlice(list);
            PipMenuActivityController.this.updateMenuActions();
        }
    };
    private ParceledListSlice mMediaActions;
    private PipMediaController mMediaController;
    private int mMenuState;
    private Messenger mMessenger = new Messenger(this.mHandler);
    private Runnable mOnAnimationEndRunnable;
    private boolean mStartActivityRequested;
    private long mStartActivityRequestedTime;
    private Runnable mStartActivityRequestedTimeoutRunnable = new Runnable() {
        /* class com.android.systemui.pip.phone.$$Lambda$PipMenuActivityController$46Yr3xVHMZsGyZiGhSKF_IPBnzk */

        public final void run() {
            PipMenuActivityController.this.lambda$new$0$PipMenuActivityController();
        }
    };
    private Bundle mTmpDismissFractionData = new Bundle();
    private Messenger mToActivityMessenger;

    public interface Listener {
        void onPipDismiss();

        void onPipExpand();

        void onPipMenuStateChanged(int i, boolean z, Runnable runnable);

        void onPipShowMenu();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$PipMenuActivityController() {
        setStartActivityRequested(false);
        Runnable runnable = this.mOnAnimationEndRunnable;
        if (runnable != null) {
            runnable.run();
            this.mOnAnimationEndRunnable = null;
        }
        Log.e("PipMenuActController", "Expected start menu activity request timed out");
    }

    public PipMenuActivityController(Context context, PipMediaController pipMediaController, InputConsumerController inputConsumerController) {
        this.mContext = context;
        this.mMediaController = pipMediaController;
        this.mInputConsumerController = inputConsumerController;
    }

    public boolean isMenuActivityVisible() {
        return this.mToActivityMessenger != null;
    }

    public void onActivityPinned() {
        this.mInputConsumerController.registerInputConsumer(true);
    }

    public void onActivityUnpinned() {
        hideMenu();
        this.mInputConsumerController.unregisterInputConsumer();
        setStartActivityRequested(false);
    }

    public void onPinnedStackAnimationEnded() {
        if (this.mToActivityMessenger != null) {
            Message obtain = Message.obtain();
            obtain.what = 6;
            try {
                this.mToActivityMessenger.send(obtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify menu pinned animation ended", e);
            }
        }
    }

    public void addListener(Listener listener) {
        if (!this.mListeners.contains(listener)) {
            this.mListeners.add(listener);
        }
    }

    public void setDismissFraction(float f) {
        if (this.mToActivityMessenger != null) {
            this.mTmpDismissFractionData.clear();
            this.mTmpDismissFractionData.putFloat("dismiss_fraction", f);
            Message obtain = Message.obtain();
            obtain.what = 5;
            obtain.obj = this.mTmpDismissFractionData;
            try {
                this.mToActivityMessenger.send(obtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify menu to update dismiss fraction", e);
            }
        } else if (!this.mStartActivityRequested || isStartActivityRequestedElapsed()) {
            startMenuActivity(0, null, false, false, false, false);
        }
    }

    public void showMenuWithDelay(int i, Rect rect, boolean z, boolean z2, boolean z3) {
        fadeOutMenu();
        showMenuInternal(i, rect, z, z2, true, z3);
    }

    public void showMenu(int i, Rect rect, boolean z, boolean z2, boolean z3) {
        showMenuInternal(i, rect, z, z2, false, z3);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Runnable getMenuStateChangeFinishedCallback(Messenger messenger, Bundle bundle) {
        if (messenger == null || bundle == null) {
            return null;
        }
        return new Runnable(bundle, messenger) {
            /* class com.android.systemui.pip.phone.$$Lambda$PipMenuActivityController$_kNQCJDSdqogZpt_djM4gC8wQ7M */
            public final /* synthetic */ Bundle f$0;
            public final /* synthetic */ Messenger f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run() {
                PipMenuActivityController.lambda$getMenuStateChangeFinishedCallback$1(this.f$0, this.f$1);
            }
        };
    }

    static /* synthetic */ void lambda$getMenuStateChangeFinishedCallback$1(Bundle bundle, Messenger messenger) {
        try {
            Message obtain = Message.obtain();
            obtain.what = bundle.getInt("message_callback_what");
            messenger.send(obtain);
        } catch (RemoteException unused) {
        }
    }

    private void showMenuInternal(int i, Rect rect, boolean z, boolean z2, boolean z3, boolean z4) {
        if (this.mToActivityMessenger != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("menu_state", i);
            if (rect != null) {
                bundle.putParcelable("stack_bounds", rect);
            }
            bundle.putBoolean("allow_timeout", z);
            bundle.putBoolean("resize_menu_on_show", z2);
            bundle.putBoolean("show_menu_with_delay", z3);
            bundle.putBoolean("show_resize_handle", z4);
            Message obtain = Message.obtain();
            obtain.what = 1;
            obtain.obj = bundle;
            try {
                this.mToActivityMessenger.send(obtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify menu to show", e);
            }
        } else if (!this.mStartActivityRequested || isStartActivityRequestedElapsed()) {
            startMenuActivity(i, rect, z, z2, z3, z4);
        }
    }

    public void pokeMenu() {
        if (this.mToActivityMessenger != null) {
            Message obtain = Message.obtain();
            obtain.what = 2;
            try {
                this.mToActivityMessenger.send(obtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify poke menu", e);
            }
        }
    }

    private void fadeOutMenu() {
        if (this.mToActivityMessenger != null) {
            Message obtain = Message.obtain();
            obtain.what = 9;
            try {
                this.mToActivityMessenger.send(obtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify menu to fade out", e);
            }
        }
    }

    public void hideMenu() {
        if (this.mToActivityMessenger != null) {
            Message obtain = Message.obtain();
            obtain.what = 3;
            try {
                this.mToActivityMessenger.send(obtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify menu to hide", e);
            }
        }
    }

    public void hideMenuWithoutResize() {
        onMenuStateChanged(0, false, null);
    }

    public void setAppActions(ParceledListSlice parceledListSlice) {
        this.mAppActions = parceledListSlice;
        updateMenuActions();
    }

    private ParceledListSlice resolveMenuActions() {
        if (isValidActions(this.mAppActions)) {
            return this.mAppActions;
        }
        return this.mMediaActions;
    }

    private void startMenuActivity(int i, Rect rect, boolean z, boolean z2, boolean z3, boolean z4) {
        try {
            ActivityManager.StackInfo stackInfo = ActivityTaskManager.getService().getStackInfo(2, 0);
            if (stackInfo == null || stackInfo.taskIds == null || stackInfo.taskIds.length <= 0) {
                Log.e("PipMenuActController", "No PIP tasks found");
                return;
            }
            Intent intent = new Intent(this.mContext, PipMenuActivity.class);
            intent.setFlags(268435456);
            intent.putExtra("messenger", this.mMessenger);
            intent.putExtra("actions", (Parcelable) resolveMenuActions());
            if (rect != null) {
                intent.putExtra("stack_bounds", rect);
            }
            intent.putExtra("menu_state", i);
            intent.putExtra("allow_timeout", z);
            intent.putExtra("resize_menu_on_show", z2);
            intent.putExtra("show_menu_with_delay", z3);
            intent.putExtra("show_resize_handle", z4);
            ActivityOptions makeCustomAnimation = ActivityOptions.makeCustomAnimation(this.mContext, 0, 0);
            makeCustomAnimation.setLaunchTaskId(stackInfo.taskIds[stackInfo.taskIds.length - 1]);
            makeCustomAnimation.setTaskOverlay(true, true);
            this.mContext.startActivityAsUser(intent, makeCustomAnimation.toBundle(), UserHandle.CURRENT);
            setStartActivityRequested(true);
        } catch (RemoteException e) {
            setStartActivityRequested(false);
            Log.e("PipMenuActController", "Error showing PIP menu activity", e);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMenuActions() {
        if (this.mToActivityMessenger != null) {
            Rect rect = null;
            try {
                ActivityManager.StackInfo stackInfo = ActivityTaskManager.getService().getStackInfo(2, 0);
                if (stackInfo != null) {
                    rect = stackInfo.bounds;
                }
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Error showing PIP menu activity", e);
            }
            Bundle bundle = new Bundle();
            bundle.putParcelable("stack_bounds", rect);
            bundle.putParcelable("actions", resolveMenuActions());
            Message obtain = Message.obtain();
            obtain.what = 4;
            obtain.obj = bundle;
            try {
                this.mToActivityMessenger.send(obtain);
            } catch (RemoteException e2) {
                Log.e("PipMenuActController", "Could not notify menu activity to update actions", e2);
            }
        }
    }

    private boolean isValidActions(ParceledListSlice parceledListSlice) {
        return parceledListSlice != null && parceledListSlice.getList().size() > 0;
    }

    private boolean isStartActivityRequestedElapsed() {
        return SystemClock.uptimeMillis() - this.mStartActivityRequestedTime >= 300;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onMenuStateChanged(int i, boolean z, Runnable runnable) {
        if (i != this.mMenuState) {
            this.mListeners.forEach(new Consumer(i, z, runnable) {
                /* class com.android.systemui.pip.phone.$$Lambda$PipMenuActivityController$_vkUUS2B_ghvg2Kknl2htGLPiZU */
                public final /* synthetic */ int f$0;
                public final /* synthetic */ boolean f$1;
                public final /* synthetic */ Runnable f$2;

                {
                    this.f$0 = r1;
                    this.f$1 = r2;
                    this.f$2 = r3;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    ((PipMenuActivityController.Listener) obj).onPipMenuStateChanged(this.f$0, this.f$1, this.f$2);
                }
            });
            if (i == 2) {
                this.mMediaController.addListener(this.mMediaActionListener);
            } else {
                this.mMediaController.removeListener(this.mMediaActionListener);
            }
        }
        this.mMenuState = i;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setStartActivityRequested(boolean z) {
        this.mHandler.removeCallbacks(this.mStartActivityRequestedTimeoutRunnable);
        this.mStartActivityRequested = z;
        this.mStartActivityRequestedTime = z ? SystemClock.uptimeMillis() : 0;
    }

    /* access modifiers changed from: package-private */
    public void handlePointerEvent(MotionEvent motionEvent) {
        if (this.mToActivityMessenger != null) {
            Message obtain = Message.obtain();
            obtain.what = 7;
            obtain.obj = motionEvent;
            try {
                this.mToActivityMessenger.send(obtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not dispatch touch event", e);
            }
        }
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipMenuActController");
        printWriter.println(str2 + "mMenuState=" + this.mMenuState);
        printWriter.println(str2 + "mToActivityMessenger=" + this.mToActivityMessenger);
        printWriter.println(str2 + "mListeners=" + this.mListeners.size());
        printWriter.println(str2 + "mStartActivityRequested=" + this.mStartActivityRequested);
        printWriter.println(str2 + "mStartActivityRequestedTime=" + this.mStartActivityRequestedTime);
    }
}

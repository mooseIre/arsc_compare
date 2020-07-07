package com.android.systemui.pip.phone;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.ActivityOptions;
import android.app.IActivityManager;
import android.app.RemoteAction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.pip.phone.PipMediaController;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.component.HidePipMenuEvent;
import com.android.systemui.recents.misc.ReferenceCountedTrigger;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PipMenuActivityController {
    private IActivityManager mActivityManager;
    private ParceledListSlice mAppActions;
    private Context mContext;
    /* access modifiers changed from: private */
    public InputConsumerController mInputConsumerController;
    /* access modifiers changed from: private */
    public ArrayList<Listener> mListeners = new ArrayList<>();
    private PipMediaController.ActionListener mMediaActionListener = new PipMediaController.ActionListener() {
        public void onMediaActionsChanged(List<RemoteAction> list) {
            ParceledListSlice unused = PipMenuActivityController.this.mMediaActions = new ParceledListSlice(list);
            PipMenuActivityController.this.updateMenuActions();
        }
    };
    /* access modifiers changed from: private */
    public ParceledListSlice mMediaActions;
    private PipMediaController mMediaController;
    private int mMenuState;
    private Messenger mMessenger = new Messenger(new Handler() {
        public void handleMessage(Message message) {
            switch (message.what) {
                case R.styleable.AppCompatTheme_textAppearanceLargePopupMenu /*100*/:
                    PipMenuActivityController.this.onMenuStateChanged(message.arg1, true);
                    return;
                case R.styleable.AppCompatTheme_textAppearanceListItem /*101*/:
                    Iterator it = PipMenuActivityController.this.mListeners.iterator();
                    while (it.hasNext()) {
                        ((Listener) it.next()).onPipExpand();
                    }
                    return;
                case R.styleable.AppCompatTheme_textAppearanceListItemSecondary /*102*/:
                    Iterator it2 = PipMenuActivityController.this.mListeners.iterator();
                    while (it2.hasNext()) {
                        ((Listener) it2.next()).onPipMinimize();
                    }
                    return;
                case R.styleable.AppCompatTheme_textAppearanceListItemSmall /*103*/:
                    Iterator it3 = PipMenuActivityController.this.mListeners.iterator();
                    while (it3.hasNext()) {
                        ((Listener) it3.next()).onPipDismiss();
                    }
                    return;
                case R.styleable.AppCompatTheme_textAppearancePopupMenuHeader /*104*/:
                    Messenger unused = PipMenuActivityController.this.mToActivityMessenger = message.replyTo;
                    boolean unused2 = PipMenuActivityController.this.mStartActivityRequested = false;
                    if (PipMenuActivityController.this.mOnAttachDecrementTrigger != null) {
                        PipMenuActivityController.this.mOnAttachDecrementTrigger.decrement();
                        ReferenceCountedTrigger unused3 = PipMenuActivityController.this.mOnAttachDecrementTrigger = null;
                    }
                    if (PipMenuActivityController.this.mToActivityMessenger == null) {
                        PipMenuActivityController.this.onMenuStateChanged(0, true);
                        return;
                    }
                    return;
                case R.styleable.AppCompatTheme_textAppearanceSearchResultSubtitle /*105*/:
                    PipMenuActivityController.this.mInputConsumerController.registerInputConsumer();
                    return;
                case R.styleable.AppCompatTheme_textAppearanceSearchResultTitle /*106*/:
                    PipMenuActivityController.this.mInputConsumerController.unregisterInputConsumer();
                    return;
                case R.styleable.AppCompatTheme_textAppearanceSmallPopupMenu /*107*/:
                    Iterator it4 = PipMenuActivityController.this.mListeners.iterator();
                    while (it4.hasNext()) {
                        ((Listener) it4.next()).onPipShowMenu();
                    }
                    return;
                default:
                    return;
            }
        }
    });
    /* access modifiers changed from: private */
    public ReferenceCountedTrigger mOnAttachDecrementTrigger;
    /* access modifiers changed from: private */
    public boolean mStartActivityRequested;
    private Bundle mTmpDismissFractionData = new Bundle();
    /* access modifiers changed from: private */
    public Messenger mToActivityMessenger;

    public interface Listener {
        void onPipDismiss();

        void onPipExpand();

        void onPipMenuStateChanged(int i, boolean z);

        void onPipMinimize();

        void onPipShowMenu();
    }

    public PipMenuActivityController(Context context, IActivityManager iActivityManager, PipMediaController pipMediaController, InputConsumerController inputConsumerController) {
        this.mContext = context;
        this.mActivityManager = iActivityManager;
        this.mMediaController = pipMediaController;
        this.mInputConsumerController = inputConsumerController;
        RecentsEventBus.getDefault().register(this);
    }

    public void onActivityPinned() {
        if (this.mMenuState == 0) {
            this.mInputConsumerController.registerInputConsumer();
        }
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
        } else if (!this.mStartActivityRequested) {
            startMenuActivity(0, (Rect) null, (Rect) null, false);
        }
    }

    public void showMenu(int i, Rect rect, Rect rect2, boolean z) {
        if (this.mToActivityMessenger != null) {
            Bundle bundle = new Bundle();
            bundle.putInt("menu_state", i);
            bundle.putParcelable("stack_bounds", rect);
            bundle.putParcelable("movement_bounds", rect2);
            bundle.putBoolean("allow_timeout", z);
            Message obtain = Message.obtain();
            obtain.what = 1;
            obtain.obj = bundle;
            try {
                this.mToActivityMessenger.send(obtain);
            } catch (RemoteException e) {
                Log.e("PipMenuActController", "Could not notify menu to show", e);
            }
        } else if (!this.mStartActivityRequested) {
            startMenuActivity(i, rect, rect2, z);
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
        onMenuStateChanged(0, false);
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

    private void startMenuActivity(int i, Rect rect, Rect rect2, boolean z) {
        try {
            ActivityManager.StackInfo stackInfo = ActivityManagerCompat.getStackInfo(4, 2, 0);
            if (stackInfo == null || stackInfo.taskIds == null || stackInfo.taskIds.length <= 0) {
                Log.e("PipMenuActController", "No PIP tasks found");
                return;
            }
            Intent intent = new Intent(this.mContext, PipMenuActivity.class);
            intent.putExtra("messenger", this.mMessenger);
            intent.putExtra("actions", resolveMenuActions());
            if (rect != null) {
                intent.putExtra("stack_bounds", rect);
            }
            if (rect2 != null) {
                intent.putExtra("movement_bounds", rect2);
            }
            intent.putExtra("menu_state", i);
            intent.putExtra("allow_timeout", z);
            ActivityOptions makeCustomAnimation = ActivityOptions.makeCustomAnimation(this.mContext, 0, 0);
            makeCustomAnimation.setLaunchTaskId(stackInfo.taskIds[stackInfo.taskIds.length - 1]);
            makeCustomAnimation.setTaskOverlay(true, true);
            this.mContext.startActivityAsUser(intent, makeCustomAnimation.toBundle(), UserHandle.CURRENT);
            this.mStartActivityRequested = true;
        } catch (Exception e) {
            this.mStartActivityRequested = false;
            Log.e("PipMenuActController", "Error showing PIP menu activity", e);
        }
    }

    /* access modifiers changed from: private */
    public void updateMenuActions() {
        if (this.mToActivityMessenger != null) {
            Rect rect = null;
            try {
                ActivityManager.StackInfo stackInfo = ActivityManagerCompat.getStackInfo(4, 2, 0);
                if (stackInfo != null) {
                    rect = stackInfo.bounds;
                }
            } catch (Exception e) {
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

    /* access modifiers changed from: private */
    public void onMenuStateChanged(int i, boolean z) {
        if (i == 0) {
            this.mInputConsumerController.registerInputConsumer();
        } else {
            this.mInputConsumerController.unregisterInputConsumer();
        }
        if (i != this.mMenuState) {
            Iterator<Listener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onPipMenuStateChanged(i, z);
            }
            if (i == 2) {
                this.mMediaController.addListener(this.mMediaActionListener);
            } else {
                this.mMediaController.removeListener(this.mMediaActionListener);
            }
        }
        this.mMenuState = i;
    }

    public final void onBusEvent(HidePipMenuEvent hidePipMenuEvent) {
        if (this.mStartActivityRequested) {
            this.mOnAttachDecrementTrigger = hidePipMenuEvent.getAnimationTrigger();
            this.mOnAttachDecrementTrigger.increment();
        }
    }

    public void dump(PrintWriter printWriter, String str) {
        String str2 = str + "  ";
        printWriter.println(str + "PipMenuActController");
        printWriter.println(str2 + "mMenuState=" + this.mMenuState);
        printWriter.println(str2 + "mToActivityMessenger=" + this.mToActivityMessenger);
        printWriter.println(str2 + "mListeners=" + this.mListeners.size());
    }
}

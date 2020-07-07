package com.android.systemui.toast;

import android.app.INotificationManager;
import android.app.ITransientNotificationCallback;
import android.content.Context;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.View;
import android.view.accessibility.IAccessibilityManager;
import android.widget.ToastPresenter;
import com.android.internal.os.SomeArgs;
import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.CommandQueue;
import java.util.Objects;

public class ToastUI extends SystemUI implements CommandQueue.Callbacks {
    private static final boolean DBG = Log.isLoggable("ToastUI", 3);
    private final IAccessibilityManager mAccessibilityManager = IAccessibilityManager.Stub.asInterface(ServiceManager.getService("accessibility"));
    private ITransientNotificationCallback mCallback;
    private int mGravity;
    private final INotificationManager mNotificationManager = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
    private ToastPresenter mPresenter;
    private int mY;

    public void start() {
        if (DBG) {
            Log.d("ToastUI", "start()");
        }
        Resources resources = this.mContext.getResources();
        this.mGravity = resources.getInteger(17694914);
        this.mY = resources.getDimensionPixelSize(17105577);
        ((CommandQueue) SystemUI.getComponent(this.mContext, CommandQueue.class)).addCallbacks(this);
    }

    public void showToast(SomeArgs someArgs) {
        showToast(someArgs.argi1, (String) someArgs.arg1, (IBinder) someArgs.arg2, (CharSequence) someArgs.arg3, (IBinder) someArgs.arg4, someArgs.argi2, (ITransientNotificationCallback) someArgs.arg5);
    }

    public void showToast(int i, String str, IBinder iBinder, CharSequence charSequence, IBinder iBinder2, int i2, ITransientNotificationCallback iTransientNotificationCallback) {
        String str2 = str;
        CharSequence charSequence2 = charSequence;
        if (DBG) {
            Log.d("ToastUI", "showToast packageName=" + str2 + ", text=" + charSequence2);
        }
        if (this.mPresenter != null) {
            hideCurrentToast();
        }
        Context createContextAsUser = this.mContext.createContextAsUser(UserHandle.getUserHandleForUid(i), 0);
        View textToastView = ToastPresenter.getTextToastView(createContextAsUser, charSequence2);
        this.mCallback = iTransientNotificationCallback;
        ToastPresenter toastPresenter = new ToastPresenter(createContextAsUser, this.mAccessibilityManager, this.mNotificationManager, str2);
        this.mPresenter = toastPresenter;
        toastPresenter.show(textToastView, iBinder, iBinder2, i2, this.mGravity, 0, this.mY, 0.0f, 0.0f, this.mCallback);
    }

    public void hideToast(String str, IBinder iBinder) {
        if (DBG) {
            Log.d("ToastUI", "hideToast packageName=" + str);
        }
        ToastPresenter toastPresenter = this.mPresenter;
        if (toastPresenter == null || !Objects.equals(toastPresenter.getPackageName(), str) || !Objects.equals(this.mPresenter.getToken(), iBinder)) {
            Log.w("ToastUI", "Attempt to hide non-current toast from package " + str);
            return;
        }
        hideCurrentToast();
    }

    private void hideCurrentToast() {
        this.mPresenter.hide(this.mCallback);
        this.mPresenter = null;
    }
}

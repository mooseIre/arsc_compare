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
import com.android.internal.annotations.VisibleForTesting;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.SystemUI;
import com.android.systemui.statusbar.CommandQueue;
import java.util.Objects;

public class ToastUI extends SystemUI implements CommandQueue.Callbacks {
    private final IAccessibilityManager mAccessibilityManager;
    private ITransientNotificationCallback mCallback;
    private final CommandQueue mCommandQueue;
    private final int mGravity;
    private final INotificationManager mNotificationManager;
    private ToastPresenter mPresenter;
    private final int mY;

    public ToastUI(Context context, CommandQueue commandQueue) {
        this(context, commandQueue, INotificationManager.Stub.asInterface(ServiceManager.getService("notification")), IAccessibilityManager.Stub.asInterface(ServiceManager.getService("accessibility")));
    }

    @VisibleForTesting
    ToastUI(Context context, CommandQueue commandQueue, INotificationManager iNotificationManager, IAccessibilityManager iAccessibilityManager) {
        super(context);
        this.mCommandQueue = commandQueue;
        this.mNotificationManager = iNotificationManager;
        this.mAccessibilityManager = iAccessibilityManager;
        Resources resources = this.mContext.getResources();
        this.mGravity = resources.getInteger(17694915);
        this.mY = resources.getDimensionPixelSize(17105547);
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mCommandQueue.addCallback((CommandQueue.Callbacks) this);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void showToast(int i, String str, IBinder iBinder, CharSequence charSequence, IBinder iBinder2, int i2, ITransientNotificationCallback iTransientNotificationCallback) {
        Context context;
        IBinder iBinder3;
        if (this.mPresenter != null) {
            hideCurrentToast();
        }
        if (i / 100000 == 999) {
            context = this.mContext.createContextAsUser(new UserHandle(KeyguardUpdateMonitor.getCurrentUser()), 0);
        } else {
            context = this.mContext.createContextAsUser(UserHandle.getUserHandleForUid(i), 0);
        }
        View textToastView = ToastPresenter.getTextToastView(context, charSequence);
        this.mCallback = iTransientNotificationCallback;
        this.mPresenter = new ToastPresenter(context, this.mAccessibilityManager, this.mNotificationManager, str);
        if (str.equals("com.android.systemui")) {
            this.mPresenter.getLayoutParams().type = 2006;
            iBinder3 = null;
        } else {
            iBinder3 = iBinder2;
        }
        this.mPresenter.show(textToastView, iBinder, iBinder3, i2, this.mGravity, 0, this.mY, 0.0f, 0.0f, this.mCallback);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void hideToast(String str, IBinder iBinder) {
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

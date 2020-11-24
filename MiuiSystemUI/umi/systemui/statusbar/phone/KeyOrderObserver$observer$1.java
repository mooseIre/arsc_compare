package com.android.systemui.statusbar.phone;

import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;

/* compiled from: KeyOrderObserver.kt */
public final class KeyOrderObserver$observer$1 extends ContentObserver {
    final /* synthetic */ KeyOrderObserver this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    KeyOrderObserver$observer$1(KeyOrderObserver keyOrderObserver, Handler handler) {
        super(handler);
        this.this$0 = keyOrderObserver;
    }

    public void onChange(boolean z) {
        new Handler(Looper.getMainLooper()).post(new KeyOrderObserver$observer$1$onChange$1(this));
    }
}

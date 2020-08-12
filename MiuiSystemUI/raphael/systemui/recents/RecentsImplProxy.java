package com.android.systemui.recents;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import com.android.internal.os.SomeArgs;
import com.android.systemui.recents.IRecentsNonSystemUserCallbacks;

public class RecentsImplProxy extends IRecentsNonSystemUserCallbacks.Stub {
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            boolean z = true;
            switch (message.what) {
                case 1:
                    RecentsImplProxy.this.mImpl.preloadRecents();
                    break;
                case 2:
                    RecentsImplProxy.this.mImpl.cancelPreloadingRecents();
                    break;
                case 3:
                    SomeArgs someArgs = (SomeArgs) message.obj;
                    RecentsImplProxy.this.mImpl.showRecents(someArgs.argi1 != 0, someArgs.argi2 != 0, someArgs.argi3 != 0, someArgs.argi4 != 0, someArgs.argi5 != 0, someArgs.argi6, false);
                    break;
                case 4:
                    RecentsImpl access$000 = RecentsImplProxy.this.mImpl;
                    boolean z2 = message.arg1 != 0;
                    if (message.arg2 == 0) {
                        z = false;
                    }
                    access$000.hideRecents(z2, z, false);
                    break;
                case 5:
                    RecentsImplProxy.this.mImpl.toggleRecents(((SomeArgs) message.obj).argi1);
                    break;
                case 6:
                    RecentsImplProxy.this.mImpl.onConfigurationChanged();
                    break;
                case 7:
                    SomeArgs someArgs2 = (SomeArgs) message.obj;
                    RecentsImpl access$0002 = RecentsImplProxy.this.mImpl;
                    int i = someArgs2.argi1;
                    int i2 = someArgs2.argi2;
                    someArgs2.argi3 = 0;
                    access$0002.dockTopTask(i, i2, 0, (Rect) someArgs2.arg1);
                    break;
                case 8:
                    RecentsImplProxy.this.mImpl.onDraggingInRecents(((Float) message.obj).floatValue());
                    break;
                case 9:
                    RecentsImplProxy.this.mImpl.onDraggingInRecentsEnded(((Float) message.obj).floatValue());
                    break;
                default:
                    super.handleMessage(message);
                    break;
            }
            super.handleMessage(message);
        }
    };
    /* access modifiers changed from: private */
    public RecentsImpl mImpl;

    public RecentsImplProxy(RecentsImpl recentsImpl) {
        this.mImpl = recentsImpl;
    }

    public void preloadRecents() throws RemoteException {
        this.mHandler.sendEmptyMessage(1);
    }

    public void cancelPreloadingRecents() throws RemoteException {
        this.mHandler.sendEmptyMessage(2);
    }

    public void showRecents(boolean z, boolean z2, boolean z3, boolean z4, boolean z5, int i) throws RemoteException {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.argi1 = z ? 1 : 0;
        obtain.argi2 = z2 ? 1 : 0;
        obtain.argi3 = z3 ? 1 : 0;
        obtain.argi4 = z4 ? 1 : 0;
        obtain.argi5 = z5 ? 1 : 0;
        obtain.argi6 = i;
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(3, obtain));
    }

    public void hideRecents(boolean z, boolean z2) throws RemoteException {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(4, z ? 1 : 0, z2 ? 1 : 0));
    }

    public void toggleRecents(int i) throws RemoteException {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.argi1 = i;
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(5, obtain));
    }

    public void onConfigurationChanged() throws RemoteException {
        this.mHandler.sendEmptyMessage(6);
    }

    public void dockTopTask(int i, int i2, int i3, Rect rect) throws RemoteException {
        SomeArgs obtain = SomeArgs.obtain();
        obtain.argi1 = i;
        obtain.argi2 = i2;
        obtain.argi3 = i3;
        obtain.arg1 = rect;
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(7, obtain));
    }

    public void onDraggingInRecents(float f) throws RemoteException {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(8, Float.valueOf(f)));
    }

    public void onDraggingInRecentsEnded(float f) throws RemoteException {
        Handler handler = this.mHandler;
        handler.sendMessage(handler.obtainMessage(9, Float.valueOf(f)));
    }
}

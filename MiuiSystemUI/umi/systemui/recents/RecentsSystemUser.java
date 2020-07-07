package com.android.systemui.recents;

import android.content.Context;
import android.graphics.Rect;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.EventLog;
import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.recents.IRecentsNonSystemUserCallbacks;
import com.android.systemui.recents.IRecentsSystemUserCallbacks;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.DockedTopTaskEvent;
import com.android.systemui.recents.events.activity.RecentsActivityStartingEvent;
import com.android.systemui.recents.events.ui.RecentsDrawnEvent;
import com.android.systemui.recents.misc.ForegroundThread;

public class RecentsSystemUser extends IRecentsSystemUserCallbacks.Stub {
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public RecentsImpl mImpl;
    /* access modifiers changed from: private */
    public final SparseArray<IRecentsNonSystemUserCallbacks> mNonSystemUserRecents = new SparseArray<>();

    public RecentsSystemUser(Context context, RecentsImpl recentsImpl) {
        this.mContext = context;
        this.mImpl = recentsImpl;
    }

    public void registerNonSystemUserCallbacks(IBinder iBinder, final int i) {
        try {
            final IRecentsNonSystemUserCallbacks asInterface = IRecentsNonSystemUserCallbacks.Stub.asInterface(iBinder);
            iBinder.linkToDeath(new IBinder.DeathRecipient() {
                public void binderDied() {
                    RecentsSystemUser.this.mNonSystemUserRecents.removeAt(RecentsSystemUser.this.mNonSystemUserRecents.indexOfValue(asInterface));
                    EventLog.writeEvent(36060, new Object[]{5, Integer.valueOf(i)});
                }
            }, 0);
            this.mNonSystemUserRecents.put(i, asInterface);
            EventLog.writeEvent(36060, new Object[]{4, Integer.valueOf(i)});
        } catch (RemoteException e) {
            Log.e("RecentsSystemUser", "Failed to register NonSystemUserCallbacks", e);
        }
    }

    public IRecentsNonSystemUserCallbacks getNonSystemUserRecentsForUser(int i) {
        return this.mNonSystemUserRecents.get(i);
    }

    public void updateRecentsVisibility(final boolean z) {
        ForegroundThread.getHandler().post(new Runnable() {
            public void run() {
                RecentsSystemUser.this.mImpl.onVisibilityChanged(RecentsSystemUser.this.mContext, z);
            }
        });
    }

    public void startScreenPinning(final int i) {
        ForegroundThread.getHandler().post(new Runnable() {
            public void run() {
                RecentsSystemUser.this.mImpl.onStartScreenPinning(RecentsSystemUser.this.mContext, i);
            }
        });
    }

    public void sendRecentsDrawnEvent() {
        RecentsEventBus.getDefault().post(new RecentsDrawnEvent());
    }

    public void sendDockingTopTaskEvent(int i, Rect rect) throws RemoteException {
        RecentsEventBus.getDefault().post(new DockedTopTaskEvent(i, rect));
    }

    public void sendLaunchRecentsEvent() throws RemoteException {
        RecentsEventBus.getDefault().post(new RecentsActivityStartingEvent());
    }
}

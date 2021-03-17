package com.android.systemui.settings;

import android.content.Context;
import android.os.UserHandle;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.util.Assert;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: CurrentUserContextTracker.kt */
public final class CurrentUserContextTracker {
    private Context _curUserContext;
    private boolean initialized;
    private final Context sysuiContext;
    private final CurrentUserTracker userTracker;

    public CurrentUserContextTracker(@NotNull Context context, @NotNull BroadcastDispatcher broadcastDispatcher) {
        Intrinsics.checkParameterIsNotNull(context, "sysuiContext");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcher, "broadcastDispatcher");
        this.sysuiContext = context;
        this.userTracker = new CurrentUserTracker(this, broadcastDispatcher, broadcastDispatcher) {
            /* class com.android.systemui.settings.CurrentUserContextTracker.AnonymousClass1 */
            final /* synthetic */ CurrentUserContextTracker this$0;

            {
                this.this$0 = r1;
            }

            @Override // com.android.systemui.settings.CurrentUserTracker
            public void onUserSwitched(int i) {
                this.this$0.handleUserSwitched(i);
            }
        };
    }

    @NotNull
    public final Context getCurrentUserContext() {
        if (this.initialized) {
            Context context = this._curUserContext;
            if (context != null) {
                return context;
            }
            Intrinsics.throwNpe();
            throw null;
        }
        throw new IllegalStateException("Must initialize before getting context");
    }

    public final void initialize() {
        this.initialized = true;
        this._curUserContext = makeUserContext(this.userTracker.getCurrentUserId());
        this.userTracker.startTracking();
    }

    public final void handleUserSwitched(int i) {
        this._curUserContext = makeUserContext(i);
    }

    private final Context makeUserContext(int i) {
        Assert.isMainThread();
        Context createContextAsUser = this.sysuiContext.createContextAsUser(UserHandle.of(i), 0);
        Intrinsics.checkExpressionValueIsNotNull(createContextAsUser, "sysuiContext.createConte…er(UserHandle.of(uid), 0)");
        return createContextAsUser;
    }
}

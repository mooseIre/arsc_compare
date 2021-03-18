package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.Looper;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.IndentingPrintWriter;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger;
import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.Executor;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: BroadcastDispatcher.kt */
public class BroadcastDispatcher extends BroadcastReceiver implements Dumpable {
    private final Executor bgExecutor;
    private final Looper bgLooper;
    private final Context context;
    private final DumpManager dumpManager;
    private final BroadcastDispatcher$handler$1 handler = new BroadcastDispatcher$handler$1(this, this.bgLooper);
    private final BroadcastDispatcherLogger logger;
    private final SparseArray<UserBroadcastDispatcher> receiversByUser = new SparseArray<>(20);

    public void registerReceiver(@NotNull BroadcastReceiver broadcastReceiver, @NotNull IntentFilter intentFilter) {
        registerReceiver$default(this, broadcastReceiver, intentFilter, null, null, 12, null);
    }

    public void registerReceiverWithHandler(@NotNull BroadcastReceiver broadcastReceiver, @NotNull IntentFilter intentFilter, @NotNull Handler handler2) {
        registerReceiverWithHandler$default(this, broadcastReceiver, intentFilter, handler2, null, 8, null);
    }

    public BroadcastDispatcher(@NotNull Context context2, @NotNull Looper looper, @NotNull Executor executor, @NotNull DumpManager dumpManager2, @NotNull BroadcastDispatcherLogger broadcastDispatcherLogger) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(looper, "bgLooper");
        Intrinsics.checkParameterIsNotNull(executor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(dumpManager2, "dumpManager");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcherLogger, "logger");
        this.context = context2;
        this.bgLooper = looper;
        this.bgExecutor = executor;
        this.dumpManager = dumpManager2;
        this.logger = broadcastDispatcherLogger;
    }

    public final void initialize() {
        DumpManager dumpManager2 = this.dumpManager;
        String name = BroadcastDispatcher.class.getName();
        Intrinsics.checkExpressionValueIsNotNull(name, "javaClass.name");
        dumpManager2.registerDumpable(name, this);
        this.handler.sendEmptyMessage(99);
        IntentFilter intentFilter = new IntentFilter("android.intent.action.USER_SWITCHED");
        UserHandle userHandle = UserHandle.ALL;
        Intrinsics.checkExpressionValueIsNotNull(userHandle, "UserHandle.ALL");
        registerReceiver(this, intentFilter, null, userHandle);
    }

    public void onReceive(@NotNull Context context2, @NotNull Intent intent) {
        Intrinsics.checkParameterIsNotNull(context2, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (Intrinsics.areEqual(intent.getAction(), "android.intent.action.USER_SWITCHED")) {
            this.handler.obtainMessage(3, intent.getIntExtra("android.intent.extra.user_handle", -10000), 0).sendToTarget();
        }
    }

    public static /* synthetic */ void registerReceiverWithHandler$default(BroadcastDispatcher broadcastDispatcher, BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, Handler handler2, UserHandle userHandle, int i, Object obj) {
        if (obj == null) {
            if ((i & 8) != 0) {
                userHandle = broadcastDispatcher.context.getUser();
                Intrinsics.checkExpressionValueIsNotNull(userHandle, "context.user");
            }
            broadcastDispatcher.registerReceiverWithHandler(broadcastReceiver, intentFilter, handler2, userHandle);
            return;
        }
        throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: registerReceiverWithHandler");
    }

    public void registerReceiverWithHandler(@NotNull BroadcastReceiver broadcastReceiver, @NotNull IntentFilter intentFilter, @NotNull Handler handler2, @NotNull UserHandle userHandle) {
        Intrinsics.checkParameterIsNotNull(broadcastReceiver, "receiver");
        Intrinsics.checkParameterIsNotNull(intentFilter, "filter");
        Intrinsics.checkParameterIsNotNull(handler2, "handler");
        Intrinsics.checkParameterIsNotNull(userHandle, "user");
        registerReceiver(broadcastReceiver, intentFilter, new HandlerExecutor(handler2), userHandle);
    }

    public static /* synthetic */ void registerReceiver$default(BroadcastDispatcher broadcastDispatcher, BroadcastReceiver broadcastReceiver, IntentFilter intentFilter, Executor executor, UserHandle userHandle, int i, Object obj) {
        if (obj == null) {
            if ((i & 4) != 0) {
                executor = broadcastDispatcher.context.getMainExecutor();
            }
            if ((i & 8) != 0) {
                userHandle = broadcastDispatcher.context.getUser();
                Intrinsics.checkExpressionValueIsNotNull(userHandle, "context.user");
            }
            broadcastDispatcher.registerReceiver(broadcastReceiver, intentFilter, executor, userHandle);
            return;
        }
        throw new UnsupportedOperationException("Super calls with default arguments not supported in this target, function: registerReceiver");
    }

    public void registerReceiver(@NotNull BroadcastReceiver broadcastReceiver, @NotNull IntentFilter intentFilter, @Nullable Executor executor, @NotNull UserHandle userHandle) {
        Intrinsics.checkParameterIsNotNull(broadcastReceiver, "receiver");
        Intrinsics.checkParameterIsNotNull(intentFilter, "filter");
        Intrinsics.checkParameterIsNotNull(userHandle, "user");
        checkFilter(intentFilter);
        BroadcastDispatcher$handler$1 broadcastDispatcher$handler$1 = this.handler;
        if (executor == null) {
            executor = this.context.getMainExecutor();
            Intrinsics.checkExpressionValueIsNotNull(executor, "context.mainExecutor");
        }
        broadcastDispatcher$handler$1.obtainMessage(0, new ReceiverData(broadcastReceiver, intentFilter, executor, userHandle)).sendToTarget();
    }

    private final void checkFilter(IntentFilter intentFilter) {
        StringBuilder sb = new StringBuilder();
        if (intentFilter.countActions() == 0) {
            sb.append("Filter must contain at least one action. ");
        }
        if (intentFilter.countDataAuthorities() != 0) {
            sb.append("Filter cannot contain DataAuthorities. ");
        }
        if (intentFilter.countDataPaths() != 0) {
            sb.append("Filter cannot contain DataPaths. ");
        }
        if (intentFilter.countDataSchemes() != 0) {
            sb.append("Filter cannot contain DataSchemes. ");
        }
        if (intentFilter.countDataTypes() != 0) {
            sb.append("Filter cannot contain DataTypes. ");
        }
        if (intentFilter.getPriority() != 0) {
            sb.append("Filter cannot modify priority. ");
        }
        if (!TextUtils.isEmpty(sb)) {
            throw new IllegalArgumentException(sb.toString());
        }
    }

    public void unregisterReceiver(@NotNull BroadcastReceiver broadcastReceiver) {
        Intrinsics.checkParameterIsNotNull(broadcastReceiver, "receiver");
        this.handler.obtainMessage(1, broadcastReceiver).sendToTarget();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    @NotNull
    public UserBroadcastDispatcher createUBRForUser(int i) {
        return new UserBroadcastDispatcher(this.context, i, this.bgLooper, this.bgExecutor, this.logger);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        printWriter.println("Broadcast dispatcher:");
        PrintWriter indentingPrintWriter = new IndentingPrintWriter(printWriter, "  ");
        indentingPrintWriter.increaseIndent();
        indentingPrintWriter.println("Current user: " + this.handler.getCurrentUser());
        int size = this.receiversByUser.size();
        for (int i = 0; i < size; i++) {
            indentingPrintWriter.println("User " + this.receiversByUser.keyAt(i));
            this.receiversByUser.valueAt(i).dump(fileDescriptor, indentingPrintWriter, strArr);
        }
        indentingPrintWriter.decreaseIndent();
    }
}

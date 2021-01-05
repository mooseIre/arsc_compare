package com.android.systemui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.ArraySet;
import com.android.internal.util.IndentingPrintWriter;
import com.android.systemui.Dumpable;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import org.jetbrains.annotations.NotNull;

/* compiled from: ActionReceiver.kt */
public final class ActionReceiver extends BroadcastReceiver implements Dumpable {
    @NotNull
    private static final AtomicInteger index = new AtomicInteger(0);
    /* access modifiers changed from: private */
    public final String action;
    private final ArraySet<String> activeCategories = new ArraySet<>();
    private final Executor bgExecutor;
    /* access modifiers changed from: private */
    public final BroadcastDispatcherLogger logger;
    /* access modifiers changed from: private */
    public final ArraySet<ReceiverData> receiverDatas = new ArraySet<>();
    private final Function2<BroadcastReceiver, IntentFilter, Unit> registerAction;
    private boolean registered;
    private final Function1<BroadcastReceiver, Unit> unregisterAction;
    private final int userId;

    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        boolean z = printWriter instanceof IndentingPrintWriter;
        if (z) {
            ((IndentingPrintWriter) printWriter).increaseIndent();
        }
        printWriter.println("Registered: " + this.registered);
        printWriter.println("Receivers:");
        if (z) {
            ((IndentingPrintWriter) printWriter).increaseIndent();
        }
        for (ReceiverData receiver : this.receiverDatas) {
            printWriter.println(receiver.getReceiver());
        }
        if (z) {
            ((IndentingPrintWriter) printWriter).decreaseIndent();
        }
        printWriter.println("Categories: " + CollectionsKt___CollectionsKt.joinToString$default(this.activeCategories, ", ", (CharSequence) null, (CharSequence) null, 0, (CharSequence) null, (Function1) null, 62, (Object) null));
        if (z) {
            ((IndentingPrintWriter) printWriter).decreaseIndent();
        }
    }

    public ActionReceiver(@NotNull String str, int i, @NotNull Function2<? super BroadcastReceiver, ? super IntentFilter, Unit> function2, @NotNull Function1<? super BroadcastReceiver, Unit> function1, @NotNull Executor executor, @NotNull BroadcastDispatcherLogger broadcastDispatcherLogger) {
        Intrinsics.checkParameterIsNotNull(str, "action");
        Intrinsics.checkParameterIsNotNull(function2, "registerAction");
        Intrinsics.checkParameterIsNotNull(function1, "unregisterAction");
        Intrinsics.checkParameterIsNotNull(executor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(broadcastDispatcherLogger, "logger");
        this.action = str;
        this.userId = i;
        this.registerAction = function2;
        this.unregisterAction = function1;
        this.bgExecutor = executor;
        this.logger = broadcastDispatcherLogger;
    }

    public final void addReceiverData(@NotNull ReceiverData receiverData) throws IllegalArgumentException {
        Sequence<T> sequence;
        Intrinsics.checkParameterIsNotNull(receiverData, "receiverData");
        if (receiverData.getFilter().hasAction(this.action)) {
            ArraySet<String> arraySet = this.activeCategories;
            Iterator<String> categoriesIterator = receiverData.getFilter().categoriesIterator();
            if (categoriesIterator == null || (sequence = SequencesKt__SequencesKt.asSequence(categoriesIterator)) == null) {
                sequence = SequencesKt__SequencesKt.emptySequence();
            }
            boolean addAll = CollectionsKt__MutableCollectionsKt.addAll(arraySet, sequence);
            if (this.receiverDatas.add(receiverData) && this.receiverDatas.size() == 1) {
                this.registerAction.invoke(this, createFilter());
                this.registered = true;
            } else if (addAll) {
                this.unregisterAction.invoke(this);
                this.registerAction.invoke(this, createFilter());
            }
        } else {
            throw new IllegalArgumentException("Trying to attach to " + this.action + " without correct action," + "receiver: " + receiverData.getReceiver());
        }
    }

    public final boolean hasReceiver(@NotNull BroadcastReceiver broadcastReceiver) {
        Intrinsics.checkParameterIsNotNull(broadcastReceiver, "receiver");
        ArraySet<ReceiverData> arraySet = this.receiverDatas;
        if ((arraySet instanceof Collection) && arraySet.isEmpty()) {
            return false;
        }
        for (ReceiverData receiver : arraySet) {
            if (Intrinsics.areEqual((Object) receiver.getReceiver(), (Object) broadcastReceiver)) {
                return true;
            }
        }
        return false;
    }

    private final IntentFilter createFilter() {
        IntentFilter intentFilter = new IntentFilter(this.action);
        for (String addCategory : this.activeCategories) {
            intentFilter.addCategory(addCategory);
        }
        return intentFilter;
    }

    public final void removeReceiver(@NotNull BroadcastReceiver broadcastReceiver) {
        Intrinsics.checkParameterIsNotNull(broadcastReceiver, "receiver");
        if (CollectionsKt__MutableCollectionsKt.removeAll(this.receiverDatas, new ActionReceiver$removeReceiver$1(broadcastReceiver)) && this.receiverDatas.isEmpty() && this.registered) {
            this.unregisterAction.invoke(this);
            this.registered = false;
            this.activeCategories.clear();
        }
    }

    public void onReceive(@NotNull Context context, @NotNull Intent intent) throws IllegalStateException {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (!(!Intrinsics.areEqual((Object) intent.getAction(), (Object) this.action))) {
            int andIncrement = index.getAndIncrement();
            this.logger.logBroadcastReceived(andIncrement, this.userId, intent);
            this.bgExecutor.execute(new ActionReceiver$onReceive$1(this, intent, context, andIncrement));
            return;
        }
        throw new IllegalStateException("Received intent for " + intent.getAction() + ' ' + "in receiver for " + this.action + '}');
    }
}

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
import kotlin.collections.CollectionsKt__MutableCollectionsKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt__SequencesKt;
import org.jetbrains.annotations.NotNull;

/* compiled from: ActionReceiver.kt */
public final class ActionReceiver extends BroadcastReceiver implements Dumpable {
    @NotNull
    private static final AtomicInteger index = new AtomicInteger(0);
    private final String action;
    private final ArraySet<String> activeCategories = new ArraySet<>();
    private final Executor bgExecutor;
    private final BroadcastDispatcherLogger logger;
    private final ArraySet<ReceiverData> receiverDatas = new ArraySet<>();
    private final Function2<BroadcastReceiver, IntentFilter, Unit> registerAction;
    private boolean registered;
    private final Function1<BroadcastReceiver, Unit> unregisterAction;
    private final int userId;

    @Override // com.android.systemui.Dumpable
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
        Iterator<T> it = this.receiverDatas.iterator();
        while (it.hasNext()) {
            printWriter.println(it.next().getReceiver());
        }
        if (z) {
            ((IndentingPrintWriter) printWriter).decreaseIndent();
        }
        printWriter.println("Categories: " + CollectionsKt___CollectionsKt.joinToString$default(this.activeCategories, ", ", null, null, 0, null, null, 62, null));
        if (z) {
            ((IndentingPrintWriter) printWriter).decreaseIndent();
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: kotlin.jvm.functions.Function2<? super android.content.BroadcastReceiver, ? super android.content.IntentFilter, kotlin.Unit> */
    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: kotlin.jvm.functions.Function1<? super android.content.BroadcastReceiver, kotlin.Unit> */
    /* JADX WARN: Multi-variable type inference failed */
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
        Sequence sequence;
        Intrinsics.checkParameterIsNotNull(receiverData, "receiverData");
        if (receiverData.getFilter().hasAction(this.action)) {
            ArraySet<String> arraySet = this.activeCategories;
            Iterator<String> categoriesIterator = receiverData.getFilter().categoriesIterator();
            if (categoriesIterator == null || (sequence = SequencesKt__SequencesKt.asSequence(categoriesIterator)) == null) {
                sequence = SequencesKt__SequencesKt.emptySequence();
            }
            boolean z = CollectionsKt__MutableCollectionsKt.addAll(arraySet, sequence);
            if (this.receiverDatas.add(receiverData) && this.receiverDatas.size() == 1) {
                this.registerAction.invoke(this, createFilter());
                this.registered = true;
            } else if (z) {
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
        Iterator<T> it = arraySet.iterator();
        while (it.hasNext()) {
            if (Intrinsics.areEqual(it.next().getReceiver(), broadcastReceiver)) {
                return true;
            }
        }
        return false;
    }

    private final IntentFilter createFilter() {
        IntentFilter intentFilter = new IntentFilter(this.action);
        Iterator<T> it = this.activeCategories.iterator();
        while (it.hasNext()) {
            intentFilter.addCategory(it.next());
        }
        return intentFilter;
    }

    public final void removeReceiver(@NotNull BroadcastReceiver broadcastReceiver) {
        Intrinsics.checkParameterIsNotNull(broadcastReceiver, "receiver");
        if ((CollectionsKt__MutableCollectionsKt.removeAll(this.receiverDatas, new ActionReceiver$removeReceiver$1(broadcastReceiver))) && this.receiverDatas.isEmpty() && this.registered) {
            this.unregisterAction.invoke(this);
            this.registered = false;
            this.activeCategories.clear();
        }
    }

    public void onReceive(@NotNull Context context, @NotNull Intent intent) throws IllegalStateException {
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(intent, "intent");
        if (!(!Intrinsics.areEqual(intent.getAction(), this.action))) {
            int andIncrement = index.getAndIncrement();
            this.logger.logBroadcastReceived(andIncrement, this.userId, intent);
            this.bgExecutor.execute(new ActionReceiver$onReceive$1(this, intent, context, andIncrement));
            return;
        }
        throw new IllegalStateException("Received intent for " + intent.getAction() + ' ' + "in receiver for " + this.action + '}');
    }
}

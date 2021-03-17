package com.android.systemui.bubbles;

import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.JobKt;
import kotlinx.coroutines.YieldKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@DebugMetadata(c = "com.android.systemui.bubbles.BubbleDataRepository$persistToDisk$1", f = "BubbleDataRepository.kt", l = {107, 109}, m = "invokeSuspend")
/* compiled from: BubbleDataRepository.kt */
final class BubbleDataRepository$persistToDisk$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ Job $prev;
    Object L$0;
    int label;
    private CoroutineScope p$;
    final /* synthetic */ BubbleDataRepository this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    BubbleDataRepository$persistToDisk$1(BubbleDataRepository bubbleDataRepository, Job job, Continuation continuation) {
        super(2, continuation);
        this.this$0 = bubbleDataRepository;
        this.$prev = job;
    }

    @NotNull
    public final Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> continuation) {
        Intrinsics.checkParameterIsNotNull(continuation, "completion");
        BubbleDataRepository$persistToDisk$1 bubbleDataRepository$persistToDisk$1 = new BubbleDataRepository$persistToDisk$1(this.this$0, this.$prev, continuation);
        bubbleDataRepository$persistToDisk$1.p$ = (CoroutineScope) obj;
        return bubbleDataRepository$persistToDisk$1;
    }

    public final Object invoke(Object obj, Object obj2) {
        return ((BubbleDataRepository$persistToDisk$1) create(obj, (Continuation) obj2)).invokeSuspend(Unit.INSTANCE);
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        CoroutineScope coroutineScope;
        Object coroutine_suspended = IntrinsicsKt__IntrinsicsKt.getCOROUTINE_SUSPENDED();
        int i = this.label;
        if (i == 0) {
            ResultKt.throwOnFailure(obj);
            coroutineScope = this.p$;
            Job job = this.$prev;
            if (job != null) {
                this.L$0 = coroutineScope;
                this.label = 1;
                if (JobKt.cancelAndJoin(job, this) == coroutine_suspended) {
                    return coroutine_suspended;
                }
            }
        } else if (i == 1) {
            coroutineScope = (CoroutineScope) this.L$0;
            ResultKt.throwOnFailure(obj);
        } else if (i == 2) {
            CoroutineScope coroutineScope2 = (CoroutineScope) this.L$0;
            ResultKt.throwOnFailure(obj);
            this.this$0.persistentRepository.persistsToDisk(this.this$0.volatileRepository.getBubbles());
            return Unit.INSTANCE;
        } else {
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
        this.L$0 = coroutineScope;
        this.label = 2;
        if (YieldKt.yield(this) == coroutine_suspended) {
            return coroutine_suspended;
        }
        this.this$0.persistentRepository.persistsToDisk(this.this$0.volatileRepository.getBubbles());
        return Unit.INSTANCE;
    }
}

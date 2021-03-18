package com.android.systemui.util;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.RestrictedSuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequenceScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
@DebugMetadata(c = "com.android.systemui.util.ConvenienceExtensionsKt$takeUntil$1", f = "ConvenienceExtensions.kt", l = {32}, m = "invokeSuspend")
/* compiled from: ConvenienceExtensions.kt */
public final class ConvenienceExtensionsKt$takeUntil$1 extends RestrictedSuspendLambda implements Function2<SequenceScope<? super T>, Continuation<? super Unit>, Object> {
    final /* synthetic */ Function1 $pred;
    final /* synthetic */ Sequence $this_takeUntil;
    Object L$0;
    Object L$1;
    Object L$2;
    int label;
    private SequenceScope p$;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ConvenienceExtensionsKt$takeUntil$1(Sequence sequence, Function1 function1, Continuation continuation) {
        super(2, continuation);
        this.$this_takeUntil = sequence;
        this.$pred = function1;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    @NotNull
    public final Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> continuation) {
        Intrinsics.checkParameterIsNotNull(continuation, "completion");
        ConvenienceExtensionsKt$takeUntil$1 convenienceExtensionsKt$takeUntil$1 = new ConvenienceExtensionsKt$takeUntil$1(this.$this_takeUntil, this.$pred, continuation);
        convenienceExtensionsKt$takeUntil$1.p$ = (SequenceScope) obj;
        return convenienceExtensionsKt$takeUntil$1;
    }

    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(Object obj, Continuation<? super Unit> continuation) {
        return ((ConvenienceExtensionsKt$takeUntil$1) create(obj, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0052, code lost:
        if (((java.lang.Boolean) r5.$pred.invoke(r3)).booleanValue() == false) goto L_0x002d;
     */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    @org.jetbrains.annotations.Nullable
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final java.lang.Object invokeSuspend(@org.jetbrains.annotations.NotNull java.lang.Object r6) {
        /*
            r5 = this;
            java.lang.Object r0 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r1 = r5.label
            r2 = 1
            if (r1 == 0) goto L_0x0021
            if (r1 != r2) goto L_0x0019
            java.lang.Object r1 = r5.L$2
            java.util.Iterator r1 = (java.util.Iterator) r1
            java.lang.Object r3 = r5.L$1
            java.lang.Object r4 = r5.L$0
            kotlin.sequences.SequenceScope r4 = (kotlin.sequences.SequenceScope) r4
            kotlin.ResultKt.throwOnFailure(r6)
            goto L_0x0046
        L_0x0019:
            java.lang.IllegalStateException r5 = new java.lang.IllegalStateException
            java.lang.String r6 = "call to 'resume' before 'invoke' with coroutine"
            r5.<init>(r6)
            throw r5
        L_0x0021:
            kotlin.ResultKt.throwOnFailure(r6)
            kotlin.sequences.SequenceScope r6 = r5.p$
            kotlin.sequences.Sequence r1 = r5.$this_takeUntil
            java.util.Iterator r1 = r1.iterator()
            r4 = r6
        L_0x002d:
            boolean r6 = r1.hasNext()
            if (r6 == 0) goto L_0x0054
            java.lang.Object r3 = r1.next()
            r5.L$0 = r4
            r5.L$1 = r3
            r5.L$2 = r1
            r5.label = r2
            java.lang.Object r6 = r4.yield(r3, r5)
            if (r6 != r0) goto L_0x0046
            return r0
        L_0x0046:
            kotlin.jvm.functions.Function1 r6 = r5.$pred
            java.lang.Object r6 = r6.invoke(r3)
            java.lang.Boolean r6 = (java.lang.Boolean) r6
            boolean r6 = r6.booleanValue()
            if (r6 == 0) goto L_0x002d
        L_0x0054:
            kotlin.Unit r5 = kotlin.Unit.INSTANCE
            return r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.util.ConvenienceExtensionsKt$takeUntil$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}

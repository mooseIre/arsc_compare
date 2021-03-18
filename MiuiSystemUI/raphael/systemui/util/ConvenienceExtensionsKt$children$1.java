package com.android.systemui.util;

import android.view.View;
import android.view.ViewGroup;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.RestrictedSuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlin.sequences.SequenceScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* access modifiers changed from: package-private */
@DebugMetadata(c = "com.android.systemui.util.ConvenienceExtensionsKt$children$1", f = "ConvenienceExtensions.kt", l = {26}, m = "invokeSuspend")
/* compiled from: ConvenienceExtensions.kt */
public final class ConvenienceExtensionsKt$children$1 extends RestrictedSuspendLambda implements Function2<SequenceScope<? super View>, Continuation<? super Unit>, Object> {
    final /* synthetic */ ViewGroup $this_children;
    int I$0;
    int I$1;
    Object L$0;
    int label;
    private SequenceScope p$;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    ConvenienceExtensionsKt$children$1(ViewGroup viewGroup, Continuation continuation) {
        super(2, continuation);
        this.$this_children = viewGroup;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    @NotNull
    public final Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> continuation) {
        Intrinsics.checkParameterIsNotNull(continuation, "completion");
        ConvenienceExtensionsKt$children$1 convenienceExtensionsKt$children$1 = new ConvenienceExtensionsKt$children$1(this.$this_children, continuation);
        convenienceExtensionsKt$children$1.p$ = (SequenceScope) obj;
        return convenienceExtensionsKt$children$1;
    }

    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(SequenceScope<? super View> sequenceScope, Continuation<? super Unit> continuation) {
        return ((ConvenienceExtensionsKt$children$1) create(sequenceScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0031  */
    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    @org.jetbrains.annotations.Nullable
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final java.lang.Object invokeSuspend(@org.jetbrains.annotations.NotNull java.lang.Object r7) {
        /*
            r6 = this;
            java.lang.Object r0 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r1 = r6.label
            r2 = 1
            if (r1 == 0) goto L_0x001f
            if (r1 != r2) goto L_0x0017
            int r1 = r6.I$1
            int r3 = r6.I$0
            java.lang.Object r4 = r6.L$0
            kotlin.sequences.SequenceScope r4 = (kotlin.sequences.SequenceScope) r4
            kotlin.ResultKt.throwOnFailure(r7)
            goto L_0x0046
        L_0x0017:
            java.lang.IllegalStateException r6 = new java.lang.IllegalStateException
            java.lang.String r7 = "call to 'resume' before 'invoke' with coroutine"
            r6.<init>(r7)
            throw r6
        L_0x001f:
            kotlin.ResultKt.throwOnFailure(r7)
            kotlin.sequences.SequenceScope r7 = r6.p$
            r1 = 0
            android.view.ViewGroup r3 = r6.$this_children
            int r3 = r3.getChildCount()
            r4 = r7
            r5 = r3
            r3 = r1
            r1 = r5
        L_0x002f:
            if (r3 >= r1) goto L_0x0048
            android.view.ViewGroup r7 = r6.$this_children
            android.view.View r7 = r7.getChildAt(r3)
            r6.L$0 = r4
            r6.I$0 = r3
            r6.I$1 = r1
            r6.label = r2
            java.lang.Object r7 = r4.yield(r7, r6)
            if (r7 != r0) goto L_0x0046
            return r0
        L_0x0046:
            int r3 = r3 + r2
            goto L_0x002f
        L_0x0048:
            kotlin.Unit r6 = kotlin.Unit.INSTANCE
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.util.ConvenienceExtensionsKt$children$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}

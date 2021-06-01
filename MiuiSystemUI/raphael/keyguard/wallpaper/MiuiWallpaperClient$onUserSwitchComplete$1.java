package com.android.keyguard.wallpaper;

import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@DebugMetadata(c = "com.android.keyguard.wallpaper.MiuiWallpaperClient$onUserSwitchComplete$1", f = "MiuiWallpaperClient.kt", l = {}, m = "invokeSuspend")
/* compiled from: MiuiWallpaperClient.kt */
final class MiuiWallpaperClient$onUserSwitchComplete$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    int label;
    private CoroutineScope p$;
    final /* synthetic */ MiuiWallpaperClient this$0;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiWallpaperClient$onUserSwitchComplete$1(MiuiWallpaperClient miuiWallpaperClient, Continuation continuation) {
        super(2, continuation);
        this.this$0 = miuiWallpaperClient;
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    @NotNull
    public final Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> continuation) {
        Intrinsics.checkParameterIsNotNull(continuation, "completion");
        MiuiWallpaperClient$onUserSwitchComplete$1 miuiWallpaperClient$onUserSwitchComplete$1 = new MiuiWallpaperClient$onUserSwitchComplete$1(this.this$0, continuation);
        miuiWallpaperClient$onUserSwitchComplete$1.p$ = (CoroutineScope) obj;
        return miuiWallpaperClient$onUserSwitchComplete$1;
    }

    /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
    @Override // kotlin.jvm.functions.Function2
    public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
        return ((MiuiWallpaperClient$onUserSwitchComplete$1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
    }

    @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        IntrinsicsKt.getCOROUTINE_SUSPENDED();
        if (this.label == 0) {
            ResultKt.throwOnFailure(obj);
            this.this$0.unBindService();
            this.this$0.bindService();
            return Unit.INSTANCE;
        }
        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
    }
}

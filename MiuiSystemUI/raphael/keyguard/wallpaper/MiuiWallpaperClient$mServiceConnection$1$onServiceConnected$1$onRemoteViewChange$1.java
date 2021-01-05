package com.android.keyguard.wallpaper;

import android.widget.RemoteViews;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.systemui.Dependency;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.CoroutineScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@DebugMetadata(c = "com.android.keyguard.wallpaper.MiuiWallpaperClient$mServiceConnection$1$onServiceConnected$1$onRemoteViewChange$1", f = "MiuiWallpaperClient.kt", l = {}, m = "invokeSuspend")
/* compiled from: MiuiWallpaperClient.kt */
final class MiuiWallpaperClient$mServiceConnection$1$onServiceConnected$1$onRemoteViewChange$1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
    final /* synthetic */ RemoteViews $fullScreenRemoteView;
    final /* synthetic */ RemoteViews $mainRemoteView;
    int label;
    private CoroutineScope p$;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    MiuiWallpaperClient$mServiceConnection$1$onServiceConnected$1$onRemoteViewChange$1(RemoteViews remoteViews, RemoteViews remoteViews2, Continuation continuation) {
        super(2, continuation);
        this.$mainRemoteView = remoteViews;
        this.$fullScreenRemoteView = remoteViews2;
    }

    @NotNull
    public final Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> continuation) {
        Intrinsics.checkParameterIsNotNull(continuation, "completion");
        MiuiWallpaperClient$mServiceConnection$1$onServiceConnected$1$onRemoteViewChange$1 miuiWallpaperClient$mServiceConnection$1$onServiceConnected$1$onRemoteViewChange$1 = new MiuiWallpaperClient$mServiceConnection$1$onServiceConnected$1$onRemoteViewChange$1(this.$mainRemoteView, this.$fullScreenRemoteView, continuation);
        miuiWallpaperClient$mServiceConnection$1$onServiceConnected$1$onRemoteViewChange$1.p$ = (CoroutineScope) obj;
        return miuiWallpaperClient$mServiceConnection$1$onServiceConnected$1$onRemoteViewChange$1;
    }

    public final Object invoke(Object obj, Object obj2) {
        return ((MiuiWallpaperClient$mServiceConnection$1$onServiceConnected$1$onRemoteViewChange$1) create(obj, (Continuation) obj2)).invokeSuspend(Unit.INSTANCE);
    }

    @Nullable
    public final Object invokeSuspend(@NotNull Object obj) {
        Object unused = IntrinsicsKt__IntrinsicsKt.getCOROUTINE_SUSPENDED();
        if (this.label == 0) {
            ResultKt.throwOnFailure(obj);
            ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).onRemoteViewChange(this.$mainRemoteView, this.$fullScreenRemoteView);
            return Unit.INSTANCE;
        }
        throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
    }
}

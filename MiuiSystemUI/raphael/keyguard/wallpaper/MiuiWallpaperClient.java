package com.android.keyguard.wallpaper;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.MiuiKeyguardUpdateMonitorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.WakefulnessLifecycle;
import com.miui.miwallpaper.IMiuiKeyguardWallpaperService;
import kotlin.ResultKt;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.intrinsics.IntrinsicsKt;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function2;
import kotlin.jvm.internal.Intrinsics;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Dispatchers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiWallpaperClient.kt */
public final class MiuiWallpaperClient extends MiuiKeyguardUpdateMonitorCallback {
    @NotNull
    private final String TAG = "MiuiWallpaperClient";
    private boolean mBinding;
    private final Context mContext;
    private final ServiceConnection mServiceConnection;
    private final CoroutineScope mUiScope = CoroutineScopeKt.CoroutineScope(Dispatchers.getMain());
    private final KeyguardUpdateMonitor mUpdateMonitor = ((KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class));
    private final MiuiWallpaperClient$mWakefulnessLifecycle$1 mWakefulnessLifecycle = new MiuiWallpaperClient$mWakefulnessLifecycle$1(this);
    private IMiuiKeyguardWallpaperService mWallpaperService;

    public MiuiWallpaperClient(@NotNull Context context, @NotNull final WakefulnessLifecycle wakefulnessLifecycle) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
        Intrinsics.checkParameterIsNotNull(wakefulnessLifecycle, "wakefulnessLifecycle");
        this.mContext = context;
        BuildersKt.launch$default(this.mUiScope, null, null, new AnonymousClass1(this, null), 3, null);
        this.mServiceConnection = new MiuiWallpaperClient$mServiceConnection$1(this);
    }

    @NotNull
    public final String getTAG() {
        return this.TAG;
    }

    @DebugMetadata(c = "com.android.keyguard.wallpaper.MiuiWallpaperClient$1", f = "MiuiWallpaperClient.kt", l = {}, m = "invokeSuspend")
    /* renamed from: com.android.keyguard.wallpaper.MiuiWallpaperClient$1  reason: invalid class name */
    /* compiled from: MiuiWallpaperClient.kt */
    static final class AnonymousClass1 extends SuspendLambda implements Function2<CoroutineScope, Continuation<? super Unit>, Object> {
        int label;
        private CoroutineScope p$;
        final /* synthetic */ MiuiWallpaperClient this$0;

        {
            this.this$0 = r1;
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        @NotNull
        public final Continuation<Unit> create(@Nullable Object obj, @NotNull Continuation<?> continuation) {
            Intrinsics.checkParameterIsNotNull(continuation, "completion");
            AnonymousClass1 r0 = new AnonymousClass1(this.this$0, wakefulnessLifecycle, continuation);
            r0.p$ = (CoroutineScope) obj;
            return r0;
        }

        /* JADX DEBUG: Method arguments types fixed to match base method, original types: [java.lang.Object, java.lang.Object] */
        @Override // kotlin.jvm.functions.Function2
        public final Object invoke(CoroutineScope coroutineScope, Continuation<? super Unit> continuation) {
            return ((AnonymousClass1) create(coroutineScope, continuation)).invokeSuspend(Unit.INSTANCE);
        }

        @Override // kotlin.coroutines.jvm.internal.BaseContinuationImpl
        @Nullable
        public final Object invokeSuspend(@NotNull Object obj) {
            IntrinsicsKt.getCOROUTINE_SUSPENDED();
            if (this.label == 0) {
                ResultKt.throwOnFailure(obj);
                this.this$0.mUpdateMonitor.registerCallback(this.this$0);
                wakefulnessLifecycle.addObserver(this.this$0.mWakefulnessLifecycle);
                return Unit.INSTANCE;
            }
            throw new IllegalStateException("call to 'resume' before 'invoke' with coroutine");
        }
    }

    public final void bindService() {
        Log.d(this.TAG, "bind services");
        if (!this.mBinding) {
            try {
                Intent intent = new Intent();
                intent.setPackage("com.miui.miwallpaper");
                intent.setAction("android.service.wallpaper.WallpaperRemoteService");
                this.mContext.bindServiceAsUser(intent, this.mServiceConnection, 1, UserHandle.CURRENT);
            } catch (SecurityException unused) {
                Log.e(this.TAG, "Unable to connect to miwallpaper service");
            }
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onScreenTurnedOff() {
        try {
            IMiuiKeyguardWallpaperService iMiuiKeyguardWallpaperService = this.mWallpaperService;
            if (iMiuiKeyguardWallpaperService != null) {
                iMiuiKeyguardWallpaperService.onScreenTurnedOff();
            }
        } catch (RemoteException e) {
            String str = this.TAG;
            Log.e(str, "onScreenTurnedOff: " + e.getMessage());
        }
    }

    @Override // com.android.keyguard.KeyguardUpdateMonitorCallback
    public void onFinishedGoingToSleep(int i) {
        try {
            IMiuiKeyguardWallpaperService iMiuiKeyguardWallpaperService = this.mWallpaperService;
            if (iMiuiKeyguardWallpaperService != null) {
                iMiuiKeyguardWallpaperService.onFinishedGoingToSleep(i);
            }
        } catch (RemoteException e) {
            String str = this.TAG;
            Log.e(str, "onFinishedGoingToSleep: " + e.getMessage());
        }
    }

    @Override // com.android.keyguard.MiuiKeyguardUpdateMonitorCallback
    public void onKeyguardShowingChanged(boolean z) {
        try {
            IMiuiKeyguardWallpaperService iMiuiKeyguardWallpaperService = this.mWallpaperService;
            if (iMiuiKeyguardWallpaperService != null) {
                iMiuiKeyguardWallpaperService.onKeyguardShowingChanged(z);
            }
        } catch (RemoteException e) {
            String str = this.TAG;
            Log.e(str, "onKeyguardShowingChanged: " + e.getMessage());
        }
    }

    public final void onKeyguardGoingAway(boolean z, boolean z2) {
        try {
            IMiuiKeyguardWallpaperService iMiuiKeyguardWallpaperService = this.mWallpaperService;
            if (iMiuiKeyguardWallpaperService != null) {
                iMiuiKeyguardWallpaperService.onKeyguardGoingAway(z, z2);
            }
        } catch (RemoteException e) {
            String str = this.TAG;
            Log.e(str, "onKeyguardGoingAway: " + e.getMessage());
        }
    }

    public final void onDozingChanged(boolean z) {
        try {
            IMiuiKeyguardWallpaperService iMiuiKeyguardWallpaperService = this.mWallpaperService;
            if (iMiuiKeyguardWallpaperService != null) {
                iMiuiKeyguardWallpaperService.onDozingChanged(z);
            }
        } catch (RemoteException e) {
            String str = this.TAG;
            Log.e(str, "onDozingChanged: " + e.getMessage());
        }
    }

    public final void updateWallpaper(boolean z) {
        try {
            IMiuiKeyguardWallpaperService iMiuiKeyguardWallpaperService = this.mWallpaperService;
            if (iMiuiKeyguardWallpaperService != null) {
                iMiuiKeyguardWallpaperService.updateWallpaper(z);
            }
        } catch (RemoteException e) {
            String str = this.TAG;
            Log.e(str, "updateWallpaper: " + e.getMessage());
        }
    }

    @Nullable
    public final Bitmap getLockWallpaperPreview() {
        try {
            IMiuiKeyguardWallpaperService iMiuiKeyguardWallpaperService = this.mWallpaperService;
            if (iMiuiKeyguardWallpaperService != null) {
                return iMiuiKeyguardWallpaperService.getLockWallpaperPreview();
            }
            Intrinsics.throwNpe();
            throw null;
        } catch (Exception e) {
            String str = this.TAG;
            Log.d(str, "getLockWallpaperPreview error: " + e.getMessage());
            return null;
        }
    }
}

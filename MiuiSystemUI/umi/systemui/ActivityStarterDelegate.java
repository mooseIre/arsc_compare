package com.android.systemui;

import android.app.PendingIntent;
import android.content.Intent;
import android.view.View;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.phone.StatusBar;
import dagger.Lazy;
import java.util.Optional;
import java.util.function.Consumer;

public class ActivityStarterDelegate implements ActivityStarter {
    private Optional<Lazy<StatusBar>> mActualStarter;

    public ActivityStarterDelegate(Optional<Lazy<StatusBar>> optional) {
        this.mActualStarter = optional;
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent) {
        this.mActualStarter.ifPresent(new Consumer(pendingIntent) {
            /* class com.android.systemui.$$Lambda$ActivityStarterDelegate$ADi9yiVtZ_7ObMe5Z0tk1YjrdVA */
            public final /* synthetic */ PendingIntent f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$startPendingIntentDismissingKeyguard$0(this.f$0, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent, Runnable runnable) {
        this.mActualStarter.ifPresent(new Consumer(pendingIntent, runnable) {
            /* class com.android.systemui.$$Lambda$ActivityStarterDelegate$INm749Eqo5FOmTBr8joulwrrt64 */
            public final /* synthetic */ PendingIntent f$0;
            public final /* synthetic */ Runnable f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$startPendingIntentDismissingKeyguard$1(this.f$0, this.f$1, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent, Runnable runnable, View view) {
        this.mActualStarter.ifPresent(new Consumer(pendingIntent, runnable, view) {
            /* class com.android.systemui.$$Lambda$ActivityStarterDelegate$wcup9XfV8BDxZsAFv2kWIfmGN0 */
            public final /* synthetic */ PendingIntent f$0;
            public final /* synthetic */ Runnable f$1;
            public final /* synthetic */ View f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$startPendingIntentDismissingKeyguard$2(this.f$0, this.f$1, this.f$2, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startActivity(Intent intent, boolean z, boolean z2, int i) {
        this.mActualStarter.ifPresent(new Consumer(intent, z, z2, i) {
            /* class com.android.systemui.$$Lambda$ActivityStarterDelegate$ILGza7s66HZ0nctdJ0wnDebSRW8 */
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ boolean f$2;
            public final /* synthetic */ int f$3;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$startActivity$3(this.f$0, this.f$1, this.f$2, this.f$3, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startActivity(Intent intent, boolean z) {
        this.mActualStarter.ifPresent(new Consumer(intent, z) {
            /* class com.android.systemui.$$Lambda$ActivityStarterDelegate$EQWsLMWn8q7rwvIKj7BUOEWOer0 */
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$startActivity$4(this.f$0, this.f$1, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startActivity(Intent intent, boolean z, boolean z2) {
        this.mActualStarter.ifPresent(new Consumer(intent, z, z2) {
            /* class com.android.systemui.$$Lambda$ActivityStarterDelegate$6Sj7OMH4lNAnb8MJLTpMcmyzi58 */
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$startActivity$5(this.f$0, this.f$1, this.f$2, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void startActivity(Intent intent, boolean z, ActivityStarter.Callback callback) {
        this.mActualStarter.ifPresent(new Consumer(intent, z, callback) {
            /* class com.android.systemui.$$Lambda$ActivityStarterDelegate$oudv1wNK3Nlq7Lmdo4di21Zs8MY */
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ ActivityStarter.Callback f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$startActivity$6(this.f$0, this.f$1, this.f$2, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void postStartActivityDismissingKeyguard(Intent intent, int i) {
        this.mActualStarter.ifPresent(new Consumer(intent, i) {
            /* class com.android.systemui.$$Lambda$ActivityStarterDelegate$Bkt5K0j7l11YRIlpia_xFvXNPbk */
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$postStartActivityDismissingKeyguard$7(this.f$0, this.f$1, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void postStartActivityDismissingKeyguard(PendingIntent pendingIntent) {
        this.mActualStarter.ifPresent(new Consumer(pendingIntent) {
            /* class com.android.systemui.$$Lambda$ActivityStarterDelegate$ntMGdPXHlgGHJa34MKvZ31nUwKY */
            public final /* synthetic */ PendingIntent f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$postStartActivityDismissingKeyguard$8(this.f$0, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void postQSRunnableDismissingKeyguard(Runnable runnable) {
        this.mActualStarter.ifPresent(new Consumer(runnable) {
            /* class com.android.systemui.$$Lambda$ActivityStarterDelegate$nAMiUKIuJCQJlUCym9gIzdU3mxI */
            public final /* synthetic */ Runnable f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$postQSRunnableDismissingKeyguard$9(this.f$0, (Lazy) obj);
            }
        });
    }

    @Override // com.android.systemui.plugins.ActivityStarter
    public void dismissKeyguardThenExecute(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable, boolean z) {
        this.mActualStarter.ifPresent(new Consumer(runnable, z) {
            /* class com.android.systemui.$$Lambda$ActivityStarterDelegate$EdR7EnJaQsucB6gVTu3f0VVIJG0 */
            public final /* synthetic */ Runnable f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ActivityStarterDelegate.lambda$dismissKeyguardThenExecute$10(ActivityStarter.OnDismissAction.this, this.f$1, this.f$2, (Lazy) obj);
            }
        });
    }
}

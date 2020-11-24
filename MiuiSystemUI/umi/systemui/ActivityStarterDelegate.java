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

    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent) {
        this.mActualStarter.ifPresent(new Consumer(pendingIntent) {
            public final /* synthetic */ PendingIntent f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).lambda$postStartActivityDismissingKeyguard$24(this.f$0);
            }
        });
    }

    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent, Runnable runnable) {
        this.mActualStarter.ifPresent(new Consumer(pendingIntent, runnable) {
            public final /* synthetic */ PendingIntent f$0;
            public final /* synthetic */ Runnable f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).startPendingIntentDismissingKeyguard(this.f$0, this.f$1);
            }
        });
    }

    public void startPendingIntentDismissingKeyguard(PendingIntent pendingIntent, Runnable runnable, View view) {
        this.mActualStarter.ifPresent(new Consumer(pendingIntent, runnable, view) {
            public final /* synthetic */ PendingIntent f$0;
            public final /* synthetic */ Runnable f$1;
            public final /* synthetic */ View f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).startPendingIntentDismissingKeyguard(this.f$0, this.f$1, this.f$2);
            }
        });
    }

    public void startActivity(Intent intent, boolean z, boolean z2, int i) {
        this.mActualStarter.ifPresent(new Consumer(intent, z, z2, i) {
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

            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).startActivity(this.f$0, this.f$1, this.f$2, this.f$3);
            }
        });
    }

    public void startActivity(Intent intent, boolean z) {
        this.mActualStarter.ifPresent(new Consumer(intent, z) {
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ boolean f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).startActivity(this.f$0, this.f$1);
            }
        });
    }

    public void startActivity(Intent intent, boolean z, boolean z2) {
        this.mActualStarter.ifPresent(new Consumer(intent, z, z2) {
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).startActivity(this.f$0, this.f$1, this.f$2);
            }
        });
    }

    public void startActivity(Intent intent, boolean z, ActivityStarter.Callback callback) {
        this.mActualStarter.ifPresent(new Consumer(intent, z, callback) {
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ boolean f$1;
            public final /* synthetic */ ActivityStarter.Callback f$2;

            {
                this.f$0 = r1;
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).startActivity(this.f$0, this.f$1, this.f$2);
            }
        });
    }

    public void postStartActivityDismissingKeyguard(Intent intent, int i) {
        this.mActualStarter.ifPresent(new Consumer(intent, i) {
            public final /* synthetic */ Intent f$0;
            public final /* synthetic */ int f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).postStartActivityDismissingKeyguard(this.f$0, this.f$1);
            }
        });
    }

    public void postStartActivityDismissingKeyguard(PendingIntent pendingIntent) {
        this.mActualStarter.ifPresent(new Consumer(pendingIntent) {
            public final /* synthetic */ PendingIntent f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).postStartActivityDismissingKeyguard(this.f$0);
            }
        });
    }

    public void postQSRunnableDismissingKeyguard(Runnable runnable) {
        this.mActualStarter.ifPresent(new Consumer(runnable) {
            public final /* synthetic */ Runnable f$0;

            {
                this.f$0 = r1;
            }

            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).postQSRunnableDismissingKeyguard(this.f$0);
            }
        });
    }

    public void dismissKeyguardThenExecute(ActivityStarter.OnDismissAction onDismissAction, Runnable runnable, boolean z) {
        this.mActualStarter.ifPresent(new Consumer(runnable, z) {
            public final /* synthetic */ Runnable f$1;
            public final /* synthetic */ boolean f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void accept(Object obj) {
                ((StatusBar) ((Lazy) obj).get()).dismissKeyguardThenExecute(ActivityStarter.OnDismissAction.this, this.f$1, this.f$2);
            }
        });
    }
}

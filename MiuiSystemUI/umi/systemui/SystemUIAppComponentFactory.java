package com.android.systemui;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.AppComponentFactory;
import com.android.systemui.dagger.ContextComponentHelper;

public class SystemUIAppComponentFactory extends AppComponentFactory {
    public ContextComponentHelper mComponentHelper;

    public interface ContextAvailableCallback {
        void onContextAvailable(Context context);
    }

    public interface ContextInitializer {
        void setContextAvailableCallback(ContextAvailableCallback contextAvailableCallback);
    }

    public Application instantiateApplicationCompat(ClassLoader classLoader, String str) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        Application instantiateApplicationCompat = super.instantiateApplicationCompat(classLoader, str);
        if (instantiateApplicationCompat instanceof ContextInitializer) {
            ((ContextInitializer) instantiateApplicationCompat).setContextAvailableCallback(new ContextAvailableCallback() {
                public final void onContextAvailable(Context context) {
                    SystemUIAppComponentFactory.this.lambda$instantiateApplicationCompat$0$SystemUIAppComponentFactory(context);
                }
            });
        }
        return instantiateApplicationCompat;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$instantiateApplicationCompat$0 */
    public /* synthetic */ void lambda$instantiateApplicationCompat$0$SystemUIAppComponentFactory(Context context) {
        SystemUIFactory.createFromConfig(context);
        SystemUIFactory.getInstance().getRootComponent().inject(this);
    }

    public ContentProvider instantiateProviderCompat(ClassLoader classLoader, String str) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        ContentProvider instantiateProviderCompat = super.instantiateProviderCompat(classLoader, str);
        if (instantiateProviderCompat instanceof ContextInitializer) {
            ((ContextInitializer) instantiateProviderCompat).setContextAvailableCallback(new ContextAvailableCallback(instantiateProviderCompat) {
                public final /* synthetic */ ContentProvider f$0;

                {
                    this.f$0 = r1;
                }

                public final void onContextAvailable(Context context) {
                    SystemUIAppComponentFactory.lambda$instantiateProviderCompat$1(this.f$0, context);
                }
            });
        }
        return instantiateProviderCompat;
    }

    static /* synthetic */ void lambda$instantiateProviderCompat$1(ContentProvider contentProvider, Context context) {
        SystemUIFactory.createFromConfig(context);
        SystemUIFactory.getInstance().getRootComponent().inject(contentProvider);
    }

    public Activity instantiateActivityCompat(ClassLoader classLoader, String str, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (this.mComponentHelper == null) {
            SystemUIFactory.getInstance().getRootComponent().inject(this);
        }
        Activity resolveActivity = this.mComponentHelper.resolveActivity(str);
        if (resolveActivity != null) {
            return resolveActivity;
        }
        return super.instantiateActivityCompat(classLoader, str, intent);
    }

    public Service instantiateServiceCompat(ClassLoader classLoader, String str, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (this.mComponentHelper == null) {
            SystemUIFactory.getInstance().getRootComponent().inject(this);
        }
        Service resolveService = this.mComponentHelper.resolveService(str);
        if (resolveService != null) {
            return resolveService;
        }
        return super.instantiateServiceCompat(classLoader, str, intent);
    }

    public BroadcastReceiver instantiateReceiverCompat(ClassLoader classLoader, String str, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        if (this.mComponentHelper == null) {
            SystemUIFactory.getInstance().getRootComponent().inject(this);
        }
        BroadcastReceiver resolveBroadcastReceiver = this.mComponentHelper.resolveBroadcastReceiver(str);
        if (resolveBroadcastReceiver != null) {
            return resolveBroadcastReceiver;
        }
        return super.instantiateReceiverCompat(classLoader, str, intent);
    }
}

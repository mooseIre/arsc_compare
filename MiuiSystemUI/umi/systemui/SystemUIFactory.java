package com.android.systemui;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.ViewGroup;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.bubbles.BubbleController;
import com.android.systemui.dagger.DaggerSystemUIRootComponent;
import com.android.systemui.dagger.DependencyProvider;
import com.android.systemui.dagger.SystemUIRootComponent;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.statusbar.StatusBarStateController;
import com.android.systemui.screenshot.ScreenshotNotificationSmartActionsProvider;
import com.android.systemui.statusbar.NotificationListener;
import com.android.systemui.statusbar.NotificationMediaManager;
import com.android.systemui.statusbar.notification.NotificationWakeUpCoordinator;
import com.android.systemui.statusbar.phone.DozeParameters;
import com.android.systemui.statusbar.phone.KeyguardBouncer;
import com.android.systemui.statusbar.phone.KeyguardBypassController;
import com.android.systemui.statusbar.phone.NotificationIconAreaController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.KeyguardStateController;
import java.util.concurrent.Executor;

public class SystemUIFactory {
    static SystemUIFactory mFactory;
    private SystemUIRootComponent mRootComponent;

    public static <T extends SystemUIFactory> T getInstance() {
        return (T) mFactory;
    }

    public static void createFromConfig(Context context) {
        if (mFactory == null) {
            String string = context.getString(C0021R$string.config_systemUIFactoryComponent);
            if (string == null || string.length() == 0) {
                throw new RuntimeException("No SystemUIFactory component configured");
            }
            try {
                SystemUIFactory systemUIFactory = (SystemUIFactory) context.getClassLoader().loadClass(string).newInstance();
                mFactory = systemUIFactory;
                systemUIFactory.init(context);
            } catch (Throwable th) {
                Log.w("SystemUIFactory", "Error creating SystemUIFactory component: " + string, th);
                throw new RuntimeException(th);
            }
        }
    }

    @VisibleForTesting
    static void cleanup() {
        mFactory = null;
    }

    private void init(Context context) {
        this.mRootComponent = buildSystemUIRootComponent(context);
        Dependency dependency = new Dependency();
        this.mRootComponent.createDependency().createSystemUI(dependency);
        dependency.start();
    }

    /* access modifiers changed from: protected */
    public SystemUIRootComponent buildSystemUIRootComponent(Context context) {
        DaggerSystemUIRootComponent.Builder builder = DaggerSystemUIRootComponent.builder();
        builder.dependencyProvider(new DependencyProvider());
        builder.contextHolder(new ContextHolder(context));
        return builder.build();
    }

    public SystemUIRootComponent getRootComponent() {
        return this.mRootComponent;
    }

    public String[] getSystemUIServiceComponents(Resources resources) {
        return resources.getStringArray(C0008R$array.config_systemUIServiceComponents);
    }

    public String[] getSystemUIServiceComponentsPerUser(Resources resources) {
        return resources.getStringArray(C0008R$array.config_systemUIServiceComponentsPerUser);
    }

    public ScreenshotNotificationSmartActionsProvider createScreenshotNotificationSmartActionsProvider(Context context, Executor executor, Handler handler) {
        return new ScreenshotNotificationSmartActionsProvider();
    }

    public KeyguardBouncer createKeyguardBouncer(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils, ViewGroup viewGroup, DismissCallbackRegistry dismissCallbackRegistry, KeyguardBouncer.BouncerExpansionCallback bouncerExpansionCallback, KeyguardStateController keyguardStateController, FalsingManager falsingManager, KeyguardBypassController keyguardBypassController) {
        return new KeyguardBouncer(context, viewMediatorCallback, lockPatternUtils, viewGroup, dismissCallbackRegistry, falsingManager, bouncerExpansionCallback, keyguardStateController, (KeyguardUpdateMonitor) Dependency.get(KeyguardUpdateMonitor.class), keyguardBypassController, new Handler(Looper.getMainLooper()));
    }

    public NotificationIconAreaController createNotificationIconAreaController(Context context, StatusBar statusBar, NotificationWakeUpCoordinator notificationWakeUpCoordinator, KeyguardBypassController keyguardBypassController, StatusBarStateController statusBarStateController) {
        return new NotificationIconAreaController(context, statusBar, statusBarStateController, notificationWakeUpCoordinator, keyguardBypassController, (NotificationMediaManager) Dependency.get(NotificationMediaManager.class), (NotificationListener) Dependency.get(NotificationListener.class), (DozeParameters) Dependency.get(DozeParameters.class), (BubbleController) Dependency.get(BubbleController.class));
    }

    public static class ContextHolder {
        private Context mContext;

        public ContextHolder(Context context) {
            this.mContext = context;
        }

        public Context provideContext() {
            return this.mContext;
        }
    }
}

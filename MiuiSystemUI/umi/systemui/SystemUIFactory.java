package com.android.systemui;

import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.Dependency;
import com.android.systemui.keyguard.DismissCallbackRegistry;
import com.android.systemui.miui.ActivityObserver;
import com.android.systemui.miui.ActivityObserverImpl;
import com.android.systemui.miui.AppIconsManager;
import com.android.systemui.miui.ToastOverlayManager;
import com.android.systemui.miui.controlcenter.QSControlTileHost;
import com.android.systemui.miui.policy.NotificationsMonitor;
import com.android.systemui.miui.policy.NotificationsMonitorImpl;
import com.android.systemui.plugins.R;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.phone.KeyguardBouncer;
import com.android.systemui.statusbar.phone.LightBarController;
import com.android.systemui.statusbar.phone.LockscreenWallpaper;
import com.android.systemui.statusbar.phone.NotificationIconAreaController;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.NotificationPeekingIconAreaController;
import com.android.systemui.statusbar.phone.ScrimController;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarTypeController;

public class SystemUIFactory {
    static SystemUIFactory mFactory;

    public <T> T createInstance(Class<T> cls) {
        return null;
    }

    public static SystemUIFactory getInstance() {
        return mFactory;
    }

    public static void createFromConfig(Context context) {
        String string = context.getString(R.string.config_systemUIFactoryComponent);
        if (string == null || string.length() == 0) {
            throw new RuntimeException("No SystemUIFactory component configured");
        }
        try {
            mFactory = (SystemUIFactory) context.getClassLoader().loadClass(string).newInstance();
        } catch (Throwable th) {
            Log.w("SystemUIFactory", "Error creating SystemUIFactory component: " + string, th);
            throw new RuntimeException(th);
        }
    }

    public StatusBarKeyguardViewManager createStatusBarKeyguardViewManager(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils) {
        return new StatusBarKeyguardViewManager(context, viewMediatorCallback, lockPatternUtils);
    }

    public KeyguardBouncer createKeyguardBouncer(Context context, ViewMediatorCallback viewMediatorCallback, LockPatternUtils lockPatternUtils, ViewGroup viewGroup, DismissCallbackRegistry dismissCallbackRegistry) {
        return new KeyguardBouncer(context, viewMediatorCallback, lockPatternUtils, viewGroup, dismissCallbackRegistry);
    }

    public ScrimController createScrimController(LightBarController lightBarController, ScrimView scrimView, ScrimView scrimView2, View view, LockscreenWallpaper lockscreenWallpaper) {
        return new ScrimController(lightBarController, scrimView, scrimView2, view);
    }

    public NotificationIconAreaController createNotificationIconAreaController(Context context, StatusBar statusBar) {
        if (((StatusBarTypeController) Dependency.get(StatusBarTypeController.class)).getCutoutType() == StatusBarTypeController.CutoutType.NONE || !context.getResources().getBoolean(R.bool.status_bar_notification_icons_notch_peeking_enabled)) {
            return new NotificationIconAreaController(context, statusBar);
        }
        return new NotificationPeekingIconAreaController(context, statusBar);
    }

    public KeyguardIndicationController createKeyguardIndicationController(Context context, NotificationPanelView notificationPanelView) {
        return new KeyguardIndicationController(context, notificationPanelView);
    }

    public QSTileHost createQSTileHost(Context context, StatusBar statusBar, StatusBarIconController statusBarIconController) {
        return new QSTileHost(context, statusBar, statusBarIconController);
    }

    public QSControlTileHost createQSControlTileHost(Context context, StatusBar statusBar, StatusBarIconController statusBarIconController) {
        return new QSControlTileHost(context, statusBar, statusBarIconController);
    }

    public void injectDependencies(ArrayMap<Object, Dependency.DependencyProvider> arrayMap, final Context context) {
        arrayMap.put(ToastOverlayManager.class, new Dependency.DependencyProvider(this) {
            public Object createDependency() {
                return new ToastOverlayManager();
            }
        });
        arrayMap.put(AppIconsManager.class, new Dependency.DependencyProvider(this) {
            public Object createDependency() {
                return new AppIconsManager();
            }
        });
        arrayMap.put(NotificationsMonitor.class, new Dependency.DependencyProvider(this) {
            public Object createDependency() {
                return new NotificationsMonitorImpl();
            }
        });
        arrayMap.put(ActivityObserver.class, new Dependency.DependencyProvider(this) {
            public Object createDependency() {
                return new ActivityObserverImpl(context);
            }
        });
    }
}

package com.android.systemui.dagger;

import android.app.INotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.display.AmbientDisplayConfiguration;
import android.hardware.display.NightDisplayListener;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.view.Choreographer;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.WindowManager;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.UiEventLogger;
import com.android.internal.logging.UiEventLoggerImpl;
import com.android.internal.util.NotificationMessagingUtil;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.ViewMediatorCallback;
import com.android.systemui.Prefs;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.broadcast.logging.BroadcastDispatcherLogger;
import com.android.systemui.doze.AlwaysOnDisplayPolicy;
import com.android.systemui.dump.DumpManager;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.plugins.PluginInitializerImpl;
import com.android.systemui.shared.plugins.PluginManager;
import com.android.systemui.shared.plugins.PluginManagerImpl;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.DevicePolicyManagerWrapper;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.NavigationBarController;
import com.android.systemui.statusbar.phone.AutoHideController;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.statusbar.policy.DataSaverController;
import com.android.systemui.statusbar.policy.MiuiConfigurationControllerImpl;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.util.leak.LeakDetector;
import java.util.concurrent.Executor;

public class DependencyProvider {
    public Handler provideTimeTickHandler() {
        HandlerThread handlerThread = new HandlerThread("TimeTick");
        handlerThread.start();
        return new Handler(handlerThread.getLooper());
    }

    public SharedPreferences provideSharePreferences(Context context) {
        return Prefs.get(context);
    }

    public AmbientDisplayConfiguration provideAmbientDisplayConfiguration(Context context) {
        return new AmbientDisplayConfiguration(context);
    }

    public DataSaverController provideDataSaverController(NetworkController networkController) {
        return networkController.getDataSaverController();
    }

    public DisplayMetrics provideDisplayMetrics(Context context, WindowManager windowManager) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        context.getDisplay().getMetrics(displayMetrics);
        return displayMetrics;
    }

    public INotificationManager provideINotificationManager() {
        return INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
    }

    public LayoutInflater providerLayoutInflater(Context context) {
        return LayoutInflater.from(context);
    }

    public LeakDetector provideLeakDetector() {
        return LeakDetector.create();
    }

    public MetricsLogger provideMetricsLogger() {
        return new MetricsLogger();
    }

    public NightDisplayListener provideNightDisplayListener(Context context, Handler handler) {
        return new NightDisplayListener(context, handler);
    }

    public PluginManager providePluginManager(Context context) {
        return new PluginManagerImpl(context, new PluginInitializerImpl());
    }

    public NavigationBarController provideNavigationBarController(Context context, Handler handler, CommandQueue commandQueue) {
        return new NavigationBarController(context, handler, commandQueue);
    }

    public ConfigurationController provideConfigurationController(Context context) {
        return new MiuiConfigurationControllerImpl(context);
    }

    public AutoHideController provideAutoHideController(Context context, Handler handler, IWindowManager iWindowManager) {
        return new AutoHideController(context, handler, iWindowManager);
    }

    public ActivityManagerWrapper provideActivityManagerWrapper() {
        return ActivityManagerWrapper.getInstance();
    }

    public BroadcastDispatcher providesBroadcastDispatcher(Context context, Looper looper, Executor executor, DumpManager dumpManager, BroadcastDispatcherLogger broadcastDispatcherLogger) {
        BroadcastDispatcher broadcastDispatcher = new BroadcastDispatcher(context, looper, executor, dumpManager, broadcastDispatcherLogger);
        broadcastDispatcher.initialize();
        return broadcastDispatcher;
    }

    public DevicePolicyManagerWrapper provideDevicePolicyManagerWrapper() {
        return DevicePolicyManagerWrapper.getInstance();
    }

    public LockPatternUtils provideLockPatternUtils(Context context) {
        return new LockPatternUtils(context);
    }

    public AlwaysOnDisplayPolicy provideAlwaysOnDisplayPolicy(Context context) {
        return new AlwaysOnDisplayPolicy(context);
    }

    public NotificationMessagingUtil provideNotificationMessagingUtil(Context context) {
        return new NotificationMessagingUtil(context);
    }

    public ViewMediatorCallback providesViewMediatorCallback(KeyguardViewMediator keyguardViewMediator) {
        return keyguardViewMediator.getViewMediatorCallback();
    }

    public Choreographer providesChoreographer() {
        return Choreographer.getInstance();
    }

    static UiEventLogger provideUiEventLogger() {
        return new UiEventLoggerImpl();
    }
}

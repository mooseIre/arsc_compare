package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkScoreManager;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.telephony.TelephonyManager;
import com.android.systemui.broadcast.BroadcastDispatcher;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class NetworkControllerImpl_Factory implements Factory<NetworkControllerImpl> {
    private final Provider<Looper> bgLooperProvider;
    private final Provider<BroadcastDispatcher> broadcastDispatcherProvider;
    private final Provider<ConnectivityManager> connectivityManagerProvider;
    private final Provider<Context> contextProvider;
    private final Provider<DeviceProvisionedController> deviceProvisionedControllerProvider;
    private final Provider<NetworkScoreManager> networkScoreManagerProvider;
    private final Provider<TelephonyManager> telephonyManagerProvider;
    private final Provider<WifiManager> wifiManagerProvider;

    public NetworkControllerImpl_Factory(Provider<Context> provider, Provider<Looper> provider2, Provider<DeviceProvisionedController> provider3, Provider<BroadcastDispatcher> provider4, Provider<ConnectivityManager> provider5, Provider<TelephonyManager> provider6, Provider<WifiManager> provider7, Provider<NetworkScoreManager> provider8) {
        this.contextProvider = provider;
        this.bgLooperProvider = provider2;
        this.deviceProvisionedControllerProvider = provider3;
        this.broadcastDispatcherProvider = provider4;
        this.connectivityManagerProvider = provider5;
        this.telephonyManagerProvider = provider6;
        this.wifiManagerProvider = provider7;
        this.networkScoreManagerProvider = provider8;
    }

    @Override // javax.inject.Provider
    public NetworkControllerImpl get() {
        return provideInstance(this.contextProvider, this.bgLooperProvider, this.deviceProvisionedControllerProvider, this.broadcastDispatcherProvider, this.connectivityManagerProvider, this.telephonyManagerProvider, this.wifiManagerProvider, this.networkScoreManagerProvider);
    }

    public static NetworkControllerImpl provideInstance(Provider<Context> provider, Provider<Looper> provider2, Provider<DeviceProvisionedController> provider3, Provider<BroadcastDispatcher> provider4, Provider<ConnectivityManager> provider5, Provider<TelephonyManager> provider6, Provider<WifiManager> provider7, Provider<NetworkScoreManager> provider8) {
        return new NetworkControllerImpl(provider.get(), provider2.get(), provider3.get(), provider4.get(), provider5.get(), provider6.get(), provider7.get(), provider8.get());
    }

    public static NetworkControllerImpl_Factory create(Provider<Context> provider, Provider<Looper> provider2, Provider<DeviceProvisionedController> provider3, Provider<BroadcastDispatcher> provider4, Provider<ConnectivityManager> provider5, Provider<TelephonyManager> provider6, Provider<WifiManager> provider7, Provider<NetworkScoreManager> provider8) {
        return new NetworkControllerImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8);
    }
}

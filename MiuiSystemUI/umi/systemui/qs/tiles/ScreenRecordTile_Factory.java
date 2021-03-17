package com.android.systemui.qs.tiles;

import com.android.systemui.qs.QSHost;
import com.android.systemui.screenrecord.RecordingController;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class ScreenRecordTile_Factory implements Factory<ScreenRecordTile> {
    private final Provider<RecordingController> controllerProvider;
    private final Provider<QSHost> hostProvider;
    private final Provider<KeyguardDismissUtil> keyguardDismissUtilProvider;

    public ScreenRecordTile_Factory(Provider<QSHost> provider, Provider<RecordingController> provider2, Provider<KeyguardDismissUtil> provider3) {
        this.hostProvider = provider;
        this.controllerProvider = provider2;
        this.keyguardDismissUtilProvider = provider3;
    }

    @Override // javax.inject.Provider
    public ScreenRecordTile get() {
        return provideInstance(this.hostProvider, this.controllerProvider, this.keyguardDismissUtilProvider);
    }

    public static ScreenRecordTile provideInstance(Provider<QSHost> provider, Provider<RecordingController> provider2, Provider<KeyguardDismissUtil> provider3) {
        return new ScreenRecordTile(provider.get(), provider2.get(), provider3.get());
    }

    public static ScreenRecordTile_Factory create(Provider<QSHost> provider, Provider<RecordingController> provider2, Provider<KeyguardDismissUtil> provider3) {
        return new ScreenRecordTile_Factory(provider, provider2, provider3);
    }
}

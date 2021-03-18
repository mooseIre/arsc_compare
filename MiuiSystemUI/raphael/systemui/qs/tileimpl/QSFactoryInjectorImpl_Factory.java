package com.android.systemui.qs.tileimpl;

import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tiles.AutoBrightnessTile;
import com.android.systemui.qs.tiles.DriveModeTile;
import com.android.systemui.qs.tiles.EditTile;
import com.android.systemui.qs.tiles.LocationTile;
import com.android.systemui.qs.tiles.MiuiAirplaneModeTile;
import com.android.systemui.qs.tiles.MiuiCellularTile;
import com.android.systemui.qs.tiles.MiuiHotspotTile;
import com.android.systemui.qs.tiles.MuteTile;
import com.android.systemui.qs.tiles.NightModeTile;
import com.android.systemui.qs.tiles.PaperModeTile;
import com.android.systemui.qs.tiles.PowerModeTile;
import com.android.systemui.qs.tiles.PowerSaverExtremeTile;
import com.android.systemui.qs.tiles.PowerSaverTile;
import com.android.systemui.qs.tiles.QuietModeTile;
import com.android.systemui.qs.tiles.ScreenButtonTile;
import com.android.systemui.qs.tiles.ScreenLockTile;
import com.android.systemui.qs.tiles.ScreenShotTile;
import com.android.systemui.qs.tiles.SyncTile;
import com.android.systemui.qs.tiles.VibrateTile;
import dagger.internal.DoubleCheck;
import dagger.internal.Factory;
import javax.inject.Provider;

public final class QSFactoryInjectorImpl_Factory implements Factory<QSFactoryInjectorImpl> {
    private final Provider<MiuiAirplaneModeTile> airplaneModeTileProvider;
    private final Provider<AutoBrightnessTile> autoBrightnessTileProvider;
    private final Provider<DriveModeTile> driveModeTileProvider;
    private final Provider<EditTile> editTileProvider;
    private final Provider<LocationTile> gpsTileProvider;
    private final Provider<MiuiCellularTile> miuiCellularTileProvider;
    private final Provider<MiuiHotspotTile> miuiHotspotTileProvider;
    private final Provider<MuteTile> muteTileProvider;
    private final Provider<NightModeTile> nightModeTileProvider;
    private final Provider<PaperModeTile> paperModeTileProvider;
    private final Provider<PowerModeTile> powerModeTileProvider;
    private final Provider<PowerSaverExtremeTile> powerSaverExtremeTileProvider;
    private final Provider<PowerSaverTile> powerSaverTileProvider;
    private final Provider<QSHost> qsHostLazyProvider;
    private final Provider<QuietModeTile> quietModeTileProvider;
    private final Provider<ScreenButtonTile> screenButtonTileProvider;
    private final Provider<ScreenLockTile> screenLockTileProvider;
    private final Provider<ScreenShotTile> screenShotTileProvider;
    private final Provider<SyncTile> syncTileProvider;
    private final Provider<VibrateTile> vibrateTileProvider;

    public QSFactoryInjectorImpl_Factory(Provider<QSHost> provider, Provider<AutoBrightnessTile> provider2, Provider<DriveModeTile> provider3, Provider<EditTile> provider4, Provider<MiuiCellularTile> provider5, Provider<MiuiHotspotTile> provider6, Provider<MuteTile> provider7, Provider<NightModeTile> provider8, Provider<PaperModeTile> provider9, Provider<PowerModeTile> provider10, Provider<PowerSaverExtremeTile> provider11, Provider<PowerSaverTile> provider12, Provider<QuietModeTile> provider13, Provider<ScreenButtonTile> provider14, Provider<ScreenLockTile> provider15, Provider<ScreenShotTile> provider16, Provider<SyncTile> provider17, Provider<VibrateTile> provider18, Provider<MiuiAirplaneModeTile> provider19, Provider<LocationTile> provider20) {
        this.qsHostLazyProvider = provider;
        this.autoBrightnessTileProvider = provider2;
        this.driveModeTileProvider = provider3;
        this.editTileProvider = provider4;
        this.miuiCellularTileProvider = provider5;
        this.miuiHotspotTileProvider = provider6;
        this.muteTileProvider = provider7;
        this.nightModeTileProvider = provider8;
        this.paperModeTileProvider = provider9;
        this.powerModeTileProvider = provider10;
        this.powerSaverExtremeTileProvider = provider11;
        this.powerSaverTileProvider = provider12;
        this.quietModeTileProvider = provider13;
        this.screenButtonTileProvider = provider14;
        this.screenLockTileProvider = provider15;
        this.screenShotTileProvider = provider16;
        this.syncTileProvider = provider17;
        this.vibrateTileProvider = provider18;
        this.airplaneModeTileProvider = provider19;
        this.gpsTileProvider = provider20;
    }

    @Override // javax.inject.Provider
    public QSFactoryInjectorImpl get() {
        return provideInstance(this.qsHostLazyProvider, this.autoBrightnessTileProvider, this.driveModeTileProvider, this.editTileProvider, this.miuiCellularTileProvider, this.miuiHotspotTileProvider, this.muteTileProvider, this.nightModeTileProvider, this.paperModeTileProvider, this.powerModeTileProvider, this.powerSaverExtremeTileProvider, this.powerSaverTileProvider, this.quietModeTileProvider, this.screenButtonTileProvider, this.screenLockTileProvider, this.screenShotTileProvider, this.syncTileProvider, this.vibrateTileProvider, this.airplaneModeTileProvider, this.gpsTileProvider);
    }

    public static QSFactoryInjectorImpl provideInstance(Provider<QSHost> provider, Provider<AutoBrightnessTile> provider2, Provider<DriveModeTile> provider3, Provider<EditTile> provider4, Provider<MiuiCellularTile> provider5, Provider<MiuiHotspotTile> provider6, Provider<MuteTile> provider7, Provider<NightModeTile> provider8, Provider<PaperModeTile> provider9, Provider<PowerModeTile> provider10, Provider<PowerSaverExtremeTile> provider11, Provider<PowerSaverTile> provider12, Provider<QuietModeTile> provider13, Provider<ScreenButtonTile> provider14, Provider<ScreenLockTile> provider15, Provider<ScreenShotTile> provider16, Provider<SyncTile> provider17, Provider<VibrateTile> provider18, Provider<MiuiAirplaneModeTile> provider19, Provider<LocationTile> provider20) {
        return new QSFactoryInjectorImpl(DoubleCheck.lazy(provider), provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20);
    }

    public static QSFactoryInjectorImpl_Factory create(Provider<QSHost> provider, Provider<AutoBrightnessTile> provider2, Provider<DriveModeTile> provider3, Provider<EditTile> provider4, Provider<MiuiCellularTile> provider5, Provider<MiuiHotspotTile> provider6, Provider<MuteTile> provider7, Provider<NightModeTile> provider8, Provider<PaperModeTile> provider9, Provider<PowerModeTile> provider10, Provider<PowerSaverExtremeTile> provider11, Provider<PowerSaverTile> provider12, Provider<QuietModeTile> provider13, Provider<ScreenButtonTile> provider14, Provider<ScreenLockTile> provider15, Provider<ScreenShotTile> provider16, Provider<SyncTile> provider17, Provider<VibrateTile> provider18, Provider<MiuiAirplaneModeTile> provider19, Provider<LocationTile> provider20) {
        return new QSFactoryInjectorImpl_Factory(provider, provider2, provider3, provider4, provider5, provider6, provider7, provider8, provider9, provider10, provider11, provider12, provider13, provider14, provider15, provider16, provider17, provider18, provider19, provider20);
    }
}

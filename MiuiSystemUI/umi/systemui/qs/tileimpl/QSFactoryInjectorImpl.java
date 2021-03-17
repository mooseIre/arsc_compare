package com.android.systemui.qs.tileimpl;

import com.android.systemui.controlcenter.qs.MiuiQSTileHostInjector;
import com.android.systemui.controlcenter.qs.tile.PluginTile;
import com.android.systemui.plugins.miui.qs.MiuiQSTile;
import com.android.systemui.plugins.miui.qs.MiuiQSTilePlugin;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.tileimpl.QSFactoryImpl;
import com.android.systemui.qs.tiles.AutoBrightnessTile;
import com.android.systemui.qs.tiles.DriveModeTile;
import com.android.systemui.qs.tiles.EditTile;
import com.android.systemui.qs.tiles.IntentTile;
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
import dagger.Lazy;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Provider;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.StringsKt__StringsJVMKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: QSFactoryInjectorImpl.kt */
public final class QSFactoryInjectorImpl implements QSFactoryImpl.QSFactoryInjector {
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
    private final Lazy<QSHost> qsHostLazy;
    private final Provider<QuietModeTile> quietModeTileProvider;
    private final Provider<ScreenButtonTile> screenButtonTileProvider;
    private final Provider<ScreenLockTile> screenLockTileProvider;
    private final Provider<ScreenShotTile> screenShotTileProvider;
    private final Provider<SyncTile> syncTileProvider;
    private final Provider<VibrateTile> vibrateTileProvider;

    public QSFactoryInjectorImpl(@NotNull Lazy<QSHost> lazy, @NotNull Provider<AutoBrightnessTile> provider, @NotNull Provider<DriveModeTile> provider2, @NotNull Provider<EditTile> provider3, @NotNull Provider<MiuiCellularTile> provider4, @NotNull Provider<MiuiHotspotTile> provider5, @NotNull Provider<MuteTile> provider6, @NotNull Provider<NightModeTile> provider7, @NotNull Provider<PaperModeTile> provider8, @NotNull Provider<PowerModeTile> provider9, @NotNull Provider<PowerSaverExtremeTile> provider10, @NotNull Provider<PowerSaverTile> provider11, @NotNull Provider<QuietModeTile> provider12, @NotNull Provider<ScreenButtonTile> provider13, @NotNull Provider<ScreenLockTile> provider14, @NotNull Provider<ScreenShotTile> provider15, @NotNull Provider<SyncTile> provider16, @NotNull Provider<VibrateTile> provider17, @NotNull Provider<MiuiAirplaneModeTile> provider18, @NotNull Provider<LocationTile> provider19) {
        Intrinsics.checkParameterIsNotNull(lazy, "qsHostLazy");
        Intrinsics.checkParameterIsNotNull(provider, "autoBrightnessTileProvider");
        Intrinsics.checkParameterIsNotNull(provider2, "driveModeTileProvider");
        Intrinsics.checkParameterIsNotNull(provider3, "editTileProvider");
        Intrinsics.checkParameterIsNotNull(provider4, "miuiCellularTileProvider");
        Intrinsics.checkParameterIsNotNull(provider5, "miuiHotspotTileProvider");
        Intrinsics.checkParameterIsNotNull(provider6, "muteTileProvider");
        Intrinsics.checkParameterIsNotNull(provider7, "nightModeTileProvider");
        Intrinsics.checkParameterIsNotNull(provider8, "paperModeTileProvider");
        Intrinsics.checkParameterIsNotNull(provider9, "powerModeTileProvider");
        Intrinsics.checkParameterIsNotNull(provider10, "powerSaverExtremeTileProvider");
        Intrinsics.checkParameterIsNotNull(provider11, "powerSaverTileProvider");
        Intrinsics.checkParameterIsNotNull(provider12, "quietModeTileProvider");
        Intrinsics.checkParameterIsNotNull(provider13, "screenButtonTileProvider");
        Intrinsics.checkParameterIsNotNull(provider14, "screenLockTileProvider");
        Intrinsics.checkParameterIsNotNull(provider15, "screenShotTileProvider");
        Intrinsics.checkParameterIsNotNull(provider16, "syncTileProvider");
        Intrinsics.checkParameterIsNotNull(provider17, "vibrateTileProvider");
        Intrinsics.checkParameterIsNotNull(provider18, "airplaneModeTileProvider");
        Intrinsics.checkParameterIsNotNull(provider19, "gpsTileProvider");
        this.qsHostLazy = lazy;
        this.autoBrightnessTileProvider = provider;
        this.driveModeTileProvider = provider2;
        this.editTileProvider = provider3;
        this.miuiCellularTileProvider = provider4;
        this.miuiHotspotTileProvider = provider5;
        this.muteTileProvider = provider6;
        this.nightModeTileProvider = provider7;
        this.paperModeTileProvider = provider8;
        this.powerModeTileProvider = provider9;
        this.powerSaverExtremeTileProvider = provider10;
        this.powerSaverTileProvider = provider11;
        this.quietModeTileProvider = provider12;
        this.screenButtonTileProvider = provider13;
        this.screenLockTileProvider = provider14;
        this.screenShotTileProvider = provider15;
        this.syncTileProvider = provider16;
        this.vibrateTileProvider = provider17;
        this.airplaneModeTileProvider = provider18;
        this.gpsTileProvider = provider19;
    }

    @Override // com.android.systemui.qs.tileimpl.QSFactoryImpl.QSFactoryInjector
    @Nullable
    public QSTileImpl<?> interceptCreateTile(@NotNull String str) {
        HashMap hashMap;
        Intrinsics.checkParameterIsNotNull(str, "tileSpec");
        QSHost qSHost = this.qsHostLazy.get();
        if (qSHost != null) {
            MiuiQSTileHostInjector hostInjector = ((QSTileHost) qSHost).getHostInjector();
            Intrinsics.checkExpressionValueIsNotNull(hostInjector, "(qsHostLazy.get() as QSTileHost).hostInjector");
            MiuiQSTilePlugin miuiQSTilePlugin = hostInjector.getMiuiQSTilePlugin();
            if (miuiQSTilePlugin != null) {
                Map<String, MiuiQSTile> allPluginTiles = miuiQSTilePlugin.getAllPluginTiles();
                if (allPluginTiles != null) {
                    hashMap = (HashMap) allPluginTiles;
                } else {
                    throw new TypeCastException("null cannot be cast to non-null type java.util.HashMap<kotlin.String!, com.android.systemui.plugins.miui.qs.MiuiQSTile!>");
                }
            } else {
                hashMap = null;
            }
            if (hashMap != null && hashMap.containsKey(str)) {
                return new PluginTile(this.qsHostLazy.get(), (MiuiQSTile) hashMap.get(str));
            }
            switch (str.hashCode()) {
                case -1672707928:
                    if (str.equals("batterysaver")) {
                        return this.powerSaverTileProvider.get();
                    }
                    break;
                case -1652176044:
                    if (str.equals("extremebatterysaver")) {
                        return this.powerSaverExtremeTileProvider.get();
                    }
                    break;
                case -1366426067:
                    if (str.equals("drivemode")) {
                        return this.driveModeTileProvider.get();
                    }
                    break;
                case -1248270690:
                    if (str.equals("screenbutton")) {
                        return this.screenButtonTileProvider.get();
                    }
                    break;
                case -919209152:
                    if (str.equals("autobrightness")) {
                        return this.autoBrightnessTileProvider.get();
                    }
                    break;
                case -677011630:
                    if (str.equals("airplane")) {
                        return this.airplaneModeTileProvider.get();
                    }
                    break;
                case -416649321:
                    if (str.equals("screenlock")) {
                        return this.screenLockTileProvider.get();
                    }
                    break;
                case -416447130:
                    if (str.equals("screenshot")) {
                        return this.screenShotTileProvider.get();
                    }
                    break;
                case 102570:
                    if (str.equals("gps")) {
                        return this.gpsTileProvider.get();
                    }
                    break;
                case 3049826:
                    if (str.equals("cell")) {
                        return this.miuiCellularTileProvider.get();
                    }
                    break;
                case 3108362:
                    if (str.equals("edit")) {
                        return this.editTileProvider.get();
                    }
                    break;
                case 3363353:
                    if (str.equals("mute")) {
                        return this.muteTileProvider.get();
                    }
                    break;
                case 3545755:
                    if (str.equals("sync")) {
                        return this.syncTileProvider.get();
                    }
                    break;
                case 104817688:
                    if (str.equals("night")) {
                        return this.nightModeTileProvider.get();
                    }
                    break;
                case 298820911:
                    if (str.equals("papermode")) {
                        return this.paperModeTileProvider.get();
                    }
                    break;
                case 451310959:
                    if (str.equals("vibrate")) {
                        return this.vibrateTileProvider.get();
                    }
                    break;
                case 845920296:
                    if (str.equals("powermode")) {
                        return this.powerModeTileProvider.get();
                    }
                    break;
                case 1099603663:
                    if (str.equals("hotspot")) {
                        return this.miuiHotspotTileProvider.get();
                    }
                    break;
                case 1367090647:
                    if (str.equals("quietmode")) {
                        return this.quietModeTileProvider.get();
                    }
                    break;
            }
            if (StringsKt__StringsJVMKt.startsWith$default(str, "intent(", false, 2, null)) {
                return IntentTile.create(this.qsHostLazy.get(), str);
            }
            return null;
        }
        throw new TypeCastException("null cannot be cast to non-null type com.android.systemui.qs.QSTileHost");
    }
}

package com.android.systemui.qs.tileimpl;

import android.os.Build;
import android.util.Log;
import android.view.ContextThemeWrapper;
import com.android.systemui.C0022R$style;
import com.android.systemui.plugins.qs.QSFactory;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.tiles.AirplaneModeTile;
import com.android.systemui.qs.tiles.BatterySaverTile;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.qs.tiles.CastTile;
import com.android.systemui.qs.tiles.CellularTile;
import com.android.systemui.qs.tiles.ColorInversionTile;
import com.android.systemui.qs.tiles.DataSaverTile;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.qs.tiles.FlashlightTile;
import com.android.systemui.qs.tiles.HotspotTile;
import com.android.systemui.qs.tiles.LocationTile;
import com.android.systemui.qs.tiles.NfcTile;
import com.android.systemui.qs.tiles.NightDisplayTile;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.qs.tiles.ScreenRecordTile;
import com.android.systemui.qs.tiles.UiModeNightTile;
import com.android.systemui.qs.tiles.UserTile;
import com.android.systemui.qs.tiles.WifiTile;
import com.android.systemui.qs.tiles.WorkModeTile;
import com.android.systemui.util.leak.GarbageMonitor;
import dagger.Lazy;
import javax.inject.Provider;

public class QSFactoryImpl implements QSFactory {
    private final Provider<AirplaneModeTile> mAirplaneModeTileProvider;
    private final Provider<BatterySaverTile> mBatterySaverTileProvider;
    private final Provider<BluetoothTile> mBluetoothTileProvider;
    private final Provider<CastTile> mCastTileProvider;
    private final Provider<CellularTile> mCellularTileProvider;
    private final Provider<ColorInversionTile> mColorInversionTileProvider;
    private final Provider<DataSaverTile> mDataSaverTileProvider;
    private final Provider<DndTile> mDndTileProvider;
    private final Provider<FlashlightTile> mFlashlightTileProvider;
    private final Provider<HotspotTile> mHotspotTileProvider;
    private final Provider<LocationTile> mLocationTileProvider;
    private final Provider<GarbageMonitor.MemoryTile> mMemoryTileProvider;
    private final Provider<NfcTile> mNfcTileProvider;
    private final Provider<NightDisplayTile> mNightDisplayTileProvider;
    private final QSFactoryInjector mQSFactoryInjector;
    private final Lazy<QSHost> mQsHostLazy;
    private final Provider<RotationLockTile> mRotationLockTileProvider;
    private final Provider<ScreenRecordTile> mScreenRecordTileProvider;
    private final Provider<UiModeNightTile> mUiModeNightTileProvider;
    private final Provider<UserTile> mUserTileProvider;
    private final Provider<WifiTile> mWifiTileProvider;
    private final Provider<WorkModeTile> mWorkModeTileProvider;

    public interface QSFactoryInjector {
        QSTileImpl interceptCreateTile(String str);
    }

    public QSFactoryImpl(Lazy<QSHost> lazy, Provider<WifiTile> provider, Provider<BluetoothTile> provider2, Provider<CellularTile> provider3, Provider<DndTile> provider4, Provider<ColorInversionTile> provider5, Provider<AirplaneModeTile> provider6, Provider<WorkModeTile> provider7, Provider<RotationLockTile> provider8, Provider<FlashlightTile> provider9, Provider<LocationTile> provider10, Provider<CastTile> provider11, Provider<HotspotTile> provider12, Provider<UserTile> provider13, Provider<BatterySaverTile> provider14, Provider<DataSaverTile> provider15, Provider<NightDisplayTile> provider16, Provider<NfcTile> provider17, Provider<GarbageMonitor.MemoryTile> provider18, Provider<UiModeNightTile> provider19, Provider<ScreenRecordTile> provider20, QSFactoryInjector qSFactoryInjector) {
        this.mQsHostLazy = lazy;
        this.mWifiTileProvider = provider;
        this.mBluetoothTileProvider = provider2;
        this.mCellularTileProvider = provider3;
        this.mDndTileProvider = provider4;
        this.mColorInversionTileProvider = provider5;
        this.mAirplaneModeTileProvider = provider6;
        this.mWorkModeTileProvider = provider7;
        this.mRotationLockTileProvider = provider8;
        this.mFlashlightTileProvider = provider9;
        this.mLocationTileProvider = provider10;
        this.mCastTileProvider = provider11;
        this.mHotspotTileProvider = provider12;
        this.mUserTileProvider = provider13;
        this.mBatterySaverTileProvider = provider14;
        this.mDataSaverTileProvider = provider15;
        this.mNightDisplayTileProvider = provider16;
        this.mNfcTileProvider = provider17;
        this.mMemoryTileProvider = provider18;
        this.mUiModeNightTileProvider = provider19;
        this.mScreenRecordTileProvider = provider20;
        this.mQSFactoryInjector = qSFactoryInjector;
    }

    @Override // com.android.systemui.plugins.qs.QSFactory
    public QSTile createTile(String str) {
        return createTile(str, false);
    }

    private QSTileImpl createTileInternal(String str) {
        QSTileImpl interceptCreateTile = this.mQSFactoryInjector.interceptCreateTile(str);
        if (interceptCreateTile != null) {
            return interceptCreateTile;
        }
        char c = 65535;
        switch (str.hashCode()) {
            case -2016941037:
                if (str.equals("inversion")) {
                    c = 4;
                    break;
                }
                break;
            case -1183073498:
                if (str.equals("flashlight")) {
                    c = '\b';
                    break;
                }
                break;
            case -805491779:
                if (str.equals("screenrecord")) {
                    c = 18;
                    break;
                }
                break;
            case -677011630:
                if (str.equals("airplane")) {
                    c = 5;
                    break;
                }
                break;
            case -331239923:
                if (str.equals("battery")) {
                    c = '\r';
                    break;
                }
                break;
            case -40300674:
                if (str.equals("rotation")) {
                    c = 7;
                    break;
                }
                break;
            case 3154:
                if (str.equals("bt")) {
                    c = 1;
                    break;
                }
                break;
            case 99610:
                if (str.equals("dnd")) {
                    c = 3;
                    break;
                }
                break;
            case 108971:
                if (str.equals("nfc")) {
                    c = 16;
                    break;
                }
                break;
            case 3046207:
                if (str.equals("cast")) {
                    c = '\n';
                    break;
                }
                break;
            case 3049826:
                if (str.equals("cell")) {
                    c = 2;
                    break;
                }
                break;
            case 3075958:
                if (str.equals("dark")) {
                    c = 17;
                    break;
                }
                break;
            case 3599307:
                if (str.equals("user")) {
                    c = '\f';
                    break;
                }
                break;
            case 3649301:
                if (str.equals("wifi")) {
                    c = 0;
                    break;
                }
                break;
            case 3655441:
                if (str.equals("work")) {
                    c = 6;
                    break;
                }
                break;
            case 104817688:
                if (str.equals("night")) {
                    c = 15;
                    break;
                }
                break;
            case 109211285:
                if (str.equals("saver")) {
                    c = 14;
                    break;
                }
                break;
            case 1099603663:
                if (str.equals("hotspot")) {
                    c = 11;
                    break;
                }
                break;
            case 1901043637:
                if (str.equals("location")) {
                    c = '\t';
                    break;
                }
                break;
        }
        switch (c) {
            case 0:
                return this.mWifiTileProvider.get();
            case 1:
                return this.mBluetoothTileProvider.get();
            case 2:
                return this.mCellularTileProvider.get();
            case 3:
                return this.mDndTileProvider.get();
            case 4:
                return this.mColorInversionTileProvider.get();
            case 5:
                return this.mAirplaneModeTileProvider.get();
            case 6:
                return this.mWorkModeTileProvider.get();
            case 7:
                return this.mRotationLockTileProvider.get();
            case '\b':
                return this.mFlashlightTileProvider.get();
            case '\t':
                return this.mLocationTileProvider.get();
            case '\n':
                return this.mCastTileProvider.get();
            case 11:
                return this.mHotspotTileProvider.get();
            case '\f':
                return this.mUserTileProvider.get();
            case '\r':
                return this.mBatterySaverTileProvider.get();
            case 14:
                return this.mDataSaverTileProvider.get();
            case 15:
                return this.mNightDisplayTileProvider.get();
            case 16:
                return this.mNfcTileProvider.get();
            case 17:
                return this.mUiModeNightTileProvider.get();
            case 18:
                return this.mScreenRecordTileProvider.get();
            default:
                if (str.startsWith("custom(")) {
                    return CustomTile.create(this.mQsHostLazy.get(), str, this.mQsHostLazy.get().getUserContext());
                }
                if (Build.IS_DEBUGGABLE && str.equals("dbg:mem")) {
                    return this.mMemoryTileProvider.get();
                }
                Log.w("QSFactory", "No stock tile spec: " + str);
                return null;
        }
    }

    @Override // com.android.systemui.plugins.qs.QSFactory
    public QSTileView createTileView(QSTile qSTile, boolean z) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(this.mQsHostLazy.get().getContext(), C0022R$style.qs_theme);
        QSIconView createTileView = qSTile.createTileView(contextThemeWrapper);
        if (z) {
            return new MiuiQSTileBaseView(contextThemeWrapper, createTileView, z);
        }
        return new MiuiQSTileView(contextThemeWrapper, createTileView);
    }

    @Override // com.android.systemui.plugins.qs.QSFactory
    public QSTile createTile(String str, boolean z) {
        QSTileImpl createTileInternal = createTileInternal(str);
        if (createTileInternal != null) {
            createTileInternal.handleStale();
        }
        if (createTileInternal != null) {
            createTileInternal.setInControlCenter(z);
        }
        return createTileInternal;
    }
}

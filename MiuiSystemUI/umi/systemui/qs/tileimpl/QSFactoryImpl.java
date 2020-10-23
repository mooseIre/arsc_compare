package com.android.systemui.qs.tileimpl;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.android.systemui.miui.controlcenter.tileImpl.CCQSTileView;
import com.android.systemui.plugins.miui.qs.MiuiQSTile;
import com.android.systemui.plugins.miui.qs.MiuiQSTilePlugin;
import com.android.systemui.plugins.qs.QSFactory;
import com.android.systemui.plugins.qs.QSIconView;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.plugins.qs.QSTileView;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.external.PluginTile;
import com.android.systemui.qs.tiles.AirplaneModeTile;
import com.android.systemui.qs.tiles.AutoBrightnessTile;
import com.android.systemui.qs.tiles.BluetoothTile;
import com.android.systemui.qs.tiles.CellularTile;
import com.android.systemui.qs.tiles.DataSaverTile;
import com.android.systemui.qs.tiles.DriveModeTile;
import com.android.systemui.qs.tiles.EditTile;
import com.android.systemui.qs.tiles.FlashlightTile;
import com.android.systemui.qs.tiles.GpsTile;
import com.android.systemui.qs.tiles.HotspotTile;
import com.android.systemui.qs.tiles.IntentTile;
import com.android.systemui.qs.tiles.LocationTile;
import com.android.systemui.qs.tiles.MiuiWirelessPowerTile;
import com.android.systemui.qs.tiles.MuteTile;
import com.android.systemui.qs.tiles.NfcTile;
import com.android.systemui.qs.tiles.NightModeTile;
import com.android.systemui.qs.tiles.PaperModeTile;
import com.android.systemui.qs.tiles.PowerModeTile;
import com.android.systemui.qs.tiles.PowerSaverExtremeTile;
import com.android.systemui.qs.tiles.PowerSaverTile;
import com.android.systemui.qs.tiles.QuietModeTile;
import com.android.systemui.qs.tiles.RotationLockTile;
import com.android.systemui.qs.tiles.ScreenButtonTile;
import com.android.systemui.qs.tiles.ScreenLockTile;
import com.android.systemui.qs.tiles.ScreenShotTile;
import com.android.systemui.qs.tiles.SyncTile;
import com.android.systemui.qs.tiles.UserTile;
import com.android.systemui.qs.tiles.VibrateTile;
import com.android.systemui.qs.tiles.VoWifiTile;
import com.android.systemui.qs.tiles.WifiTile;
import com.android.systemui.qs.tiles.WorkModeTile;
import java.util.HashMap;

public class QSFactoryImpl implements QSFactory {
    private final QSTileHost mHost;

    public int getVersion() {
        return -1;
    }

    public void onCreate(Context context, Context context2) {
    }

    public void onDestroy() {
    }

    public QSFactoryImpl(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
    }

    public QSTile createTile(String str) {
        return createTile(str, false);
    }

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

    private QSTileImpl createTileInternal(String str) {
        MiuiQSTilePlugin miuiQSTilePlugin = this.mHost.getMiuiQSTilePlugin();
        HashMap hashMap = miuiQSTilePlugin != null ? (HashMap) miuiQSTilePlugin.getAllPluginTiles() : null;
        if (hashMap != null && hashMap.containsKey(str)) {
            return new PluginTile(this.mHost, (MiuiQSTile) hashMap.get(str));
        }
        if (str.equals("wifi")) {
            return new WifiTile(this.mHost);
        }
        if (str.equals("bt")) {
            return new BluetoothTile(this.mHost);
        }
        if (str.equals("cell")) {
            return new CellularTile(this.mHost);
        }
        if (str.equals("airplane")) {
            return new AirplaneModeTile(this.mHost);
        }
        if (str.equals("rotation")) {
            return new RotationLockTile(this.mHost);
        }
        if (str.equals("flashlight")) {
            return new FlashlightTile(this.mHost);
        }
        if (str.equals("gps")) {
            return Build.VERSION.SDK_INT > 28 ? new LocationTile(this.mHost) : new GpsTile(this.mHost);
        }
        if (str.equals("hotspot")) {
            return new HotspotTile(this.mHost);
        }
        if (str.equals("user")) {
            return new UserTile(this.mHost);
        }
        if (str.equals("saver")) {
            return new DataSaverTile(this.mHost);
        }
        if (str.equals("nfc")) {
            return new NfcTile(this.mHost);
        }
        if (str.equals("screenlock")) {
            return new ScreenLockTile(this.mHost);
        }
        if (str.equals("screenshot")) {
            return new ScreenShotTile(this.mHost);
        }
        if (str.equals("papermode")) {
            return new PaperModeTile(this.mHost);
        }
        if (str.equals("autobrightness")) {
            return new AutoBrightnessTile(this.mHost);
        }
        if (str.equals("vibrate")) {
            return new VibrateTile(this.mHost);
        }
        if (str.equals("sync")) {
            return new SyncTile(this.mHost);
        }
        if (str.equals("quietmode")) {
            return new QuietModeTile(this.mHost);
        }
        if (str.equals("mute")) {
            return new MuteTile(this.mHost);
        }
        if (str.equals("edit")) {
            return new EditTile(this.mHost);
        }
        if (str.equals("powermode")) {
            return new PowerModeTile(this.mHost);
        }
        if (str.equals("screenbutton")) {
            return new ScreenButtonTile(this.mHost);
        }
        if (str.equals("batterysaver")) {
            return new PowerSaverTile(this.mHost);
        }
        if (str.equals("extremebatterysaver")) {
            return new PowerSaverExtremeTile(this.mHost);
        }
        if (str.equals("drivemode")) {
            return new DriveModeTile(this.mHost);
        }
        if (str.equals("night")) {
            return new NightModeTile(this.mHost);
        }
        if (str.equals("wirelesspower")) {
            return new MiuiWirelessPowerTile(this.mHost);
        }
        if (str.equals("workmode")) {
            return new WorkModeTile(this.mHost);
        }
        if (str.equals("vowifi1")) {
            return new VoWifiTile(this.mHost, 0);
        }
        if (str.equals("vowifi2")) {
            return new VoWifiTile(this.mHost, 1);
        }
        if (str.startsWith("intent(")) {
            return IntentTile.create(this.mHost, str);
        }
        if (str.startsWith("custom(")) {
            return CustomTile.create(this.mHost, str);
        }
        Log.w("QSFactory", "Bad tile spec: " + str);
        return null;
    }

    public QSTileView createTileView(QSTile qSTile, boolean z) {
        Context context = this.mHost.getContext();
        QSIconView createTileView = qSTile.createTileView(context);
        if (z) {
            return new QSTileBaseView(context, createTileView, z);
        }
        return new QSTileView(context, createTileView);
    }

    public QSTileView createControlCenterTileView(QSTile qSTile, boolean z) {
        Context context = this.mHost.getContext();
        return new CCQSTileView(context, qSTile.createControlCenterTileView(context), z);
    }
}

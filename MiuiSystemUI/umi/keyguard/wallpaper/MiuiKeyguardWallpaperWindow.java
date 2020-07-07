package com.android.keyguard.wallpaper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.service.wallpaper.IWallpaperConnectionCompat;
import android.service.wallpaper.IWallpaperEngine;
import android.service.wallpaper.IWallpaperService;
import android.service.wallpaper.IWallpaperServiceCompat;
import android.view.IWindowManagerCompat;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import com.android.keyguard.wallpaper.MiuiKeyguardWallpaperController;
import com.android.keyguard.wallpaper.service.MiuiKeyguardLiveWallpaper;
import com.android.keyguard.wallpaper.service.MiuiKeyguardPictorialWallpaper;

public class MiuiKeyguardWallpaperWindow {
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public int mDesiredHeight;
    /* access modifiers changed from: private */
    public int mDesiredWidth;
    /* access modifiers changed from: private */
    public WallpaperConnection mWallpaperConnection;
    private MiuiKeyguardWallpaperController.KeyguardWallpaperType mWallpaperType = MiuiKeyguardWallpaperController.KeyguardWallpaperType.LIVE_SYSTEM;

    public MiuiKeyguardWallpaperWindow(Context context) {
        this.mContext = context;
        Point point = new Point();
        ((WindowManager) context.getSystemService(WindowManager.class)).getDefaultDisplay().getRealSize(point);
        this.mDesiredWidth = point.x;
        this.mDesiredHeight = point.y;
    }

    /* access modifiers changed from: package-private */
    public void updateWallpaperType(MiuiKeyguardWallpaperController.KeyguardWallpaperType keyguardWallpaperType) {
        if (this.mWallpaperType != keyguardWallpaperType) {
            this.mWallpaperType = keyguardWallpaperType;
            resetWallpaperWindow();
        }
    }

    /* access modifiers changed from: package-private */
    public void resetWallpaperWindow() {
        if (this.mWallpaperType == MiuiKeyguardWallpaperController.KeyguardWallpaperType.LIVE_SYSTEM) {
            hide();
        } else if (this.mWallpaperConnection == null) {
            createAndAdd();
        } else {
            update();
        }
    }

    private void createAndAdd() {
        WallpaperConnection wallpaperConnection = new WallpaperConnection();
        this.mWallpaperConnection = wallpaperConnection;
        wallpaperConnection.setBindIntent(getBindIntent());
        this.mWallpaperConnection.connect();
        addWindowToken();
    }

    private void update() {
        this.mWallpaperConnection.disconnect();
        this.mWallpaperConnection.setBindIntent(getBindIntent());
        this.mWallpaperConnection.connect();
    }

    private void hide() {
        WallpaperConnection wallpaperConnection = this.mWallpaperConnection;
        if (wallpaperConnection != null) {
            wallpaperConnection.disconnect();
            removeWindowToken();
            this.mWallpaperConnection = null;
        }
    }

    private Intent getBindIntent() {
        String str;
        Intent intent = new Intent("android.service.wallpaper.WallpaperService");
        if (this.mWallpaperType == MiuiKeyguardWallpaperController.KeyguardWallpaperType.PICTORIAL) {
            str = MiuiKeyguardPictorialWallpaper.class.getName();
        } else {
            str = MiuiKeyguardLiveWallpaper.class.getName();
        }
        intent.setComponent(ComponentName.createRelative(this.mContext.getPackageName(), str));
        return intent;
    }

    private void addWindowToken() {
        try {
            IWindowManagerCompat.addWindowToken(WindowManagerGlobal.getWindowManagerService(), this.mWallpaperConnection.mToken, 2013, 0);
        } catch (Exception unused) {
        }
    }

    private void removeWindowToken() {
        try {
            IWindowManagerCompat.removeWindowToken(WindowManagerGlobal.getWindowManagerService(), this.mWallpaperConnection.mToken, 0);
        } catch (Exception unused) {
        }
    }

    class WallpaperConnection extends IWallpaperConnectionCompat implements ServiceConnection {
        boolean mConnected;
        IWallpaperEngine mEngine;
        Intent mIntent;
        IWallpaperService mService;
        final Binder mToken;

        public void engineShown(IWallpaperEngine iWallpaperEngine) throws RemoteException {
        }

        public ParcelFileDescriptor setWallpaper(String str) {
            return null;
        }

        WallpaperConnection() {
            Binder binder = new Binder();
            this.mToken = binder;
            binder.attachInterface((IInterface) null, "miui.systemui.keyguard.Wallpaper");
        }

        public void setBindIntent(Intent intent) {
            this.mIntent = intent;
        }

        public boolean connect() {
            synchronized (this) {
                if (!MiuiKeyguardWallpaperWindow.this.mContext.bindService(this.mIntent, this, 1)) {
                    return false;
                }
                this.mConnected = true;
                return true;
            }
        }

        /* JADX WARNING: Exception block dominator not found, dom blocks: [] */
        /* JADX WARNING: Missing exception handler attribute for start block: B:12:0x0019 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:8:0x000e */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void disconnect() {
            /*
                r2 = this;
                monitor-enter(r2)
                r0 = 0
                r2.mConnected = r0     // Catch:{ all -> 0x001d }
                android.service.wallpaper.IWallpaperEngine r0 = r2.mEngine     // Catch:{ all -> 0x001d }
                r1 = 0
                if (r0 == 0) goto L_0x0010
                android.service.wallpaper.IWallpaperEngine r0 = r2.mEngine     // Catch:{ RemoteException -> 0x000e }
                r0.destroy()     // Catch:{ RemoteException -> 0x000e }
            L_0x000e:
                r2.mEngine = r1     // Catch:{ all -> 0x001d }
            L_0x0010:
                com.android.keyguard.wallpaper.MiuiKeyguardWallpaperWindow r0 = com.android.keyguard.wallpaper.MiuiKeyguardWallpaperWindow.this     // Catch:{ IllegalArgumentException -> 0x0019 }
                android.content.Context r0 = r0.mContext     // Catch:{ IllegalArgumentException -> 0x0019 }
                r0.unbindService(r2)     // Catch:{ IllegalArgumentException -> 0x0019 }
            L_0x0019:
                r2.mService = r1     // Catch:{ all -> 0x001d }
                monitor-exit(r2)     // Catch:{ all -> 0x001d }
                return
            L_0x001d:
                r0 = move-exception
                monitor-exit(r2)     // Catch:{ all -> 0x001d }
                throw r0
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.wallpaper.MiuiKeyguardWallpaperWindow.WallpaperConnection.disconnect():void");
        }

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (MiuiKeyguardWallpaperWindow.this.mWallpaperConnection == this) {
                IWallpaperService asInterface = IWallpaperService.Stub.asInterface(iBinder);
                this.mService = asInterface;
                try {
                    IWallpaperServiceCompat.attach(asInterface, this, this.mToken, 2013, false, MiuiKeyguardWallpaperWindow.this.mDesiredWidth, MiuiKeyguardWallpaperWindow.this.mDesiredHeight, new Rect(0, 0, 0, 0), 0);
                } catch (RemoteException unused) {
                }
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            this.mService = null;
            this.mEngine = null;
            if (MiuiKeyguardWallpaperWindow.this.mWallpaperConnection == this) {
                WallpaperConnection unused = MiuiKeyguardWallpaperWindow.this.mWallpaperConnection = null;
            }
        }

        /* JADX WARNING: Can't wrap try/catch for region: R(5:1|2|(1:4)(2:5|6)|7|8) */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x000b */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void attachEngine(android.service.wallpaper.IWallpaperEngine r2) {
            /*
                r1 = this;
                monitor-enter(r1)
                boolean r0 = r1.mConnected     // Catch:{ all -> 0x000d }
                if (r0 == 0) goto L_0x0008
                r1.mEngine = r2     // Catch:{ all -> 0x000d }
                goto L_0x000b
            L_0x0008:
                r2.destroy()     // Catch:{ RemoteException -> 0x000b }
            L_0x000b:
                monitor-exit(r1)     // Catch:{ all -> 0x000d }
                return
            L_0x000d:
                r2 = move-exception
                monitor-exit(r1)     // Catch:{ all -> 0x000d }
                throw r2
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.wallpaper.MiuiKeyguardWallpaperWindow.WallpaperConnection.attachEngine(android.service.wallpaper.IWallpaperEngine):void");
        }
    }
}

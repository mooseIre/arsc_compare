package com.android.systemui.statusbar;

import android.app.MiuiStatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.miui.systemui.annotation.Inject;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkSpeedController {
    /* access modifiers changed from: private */
    public Handler mBgHandler;
    private ConnectivityManager mConnectivityManager;
    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                if (intent.hasExtra("noConnectivity")) {
                    boolean unused = NetworkSpeedController.this.mIsNetworkConnected = !intent.getBooleanExtra("noConnectivity", false);
                    NetworkSpeedController.this.postUpdateNetworkSpeed();
                    return;
                }
                NetworkSpeedController.this.mBgHandler.removeMessages(R.styleable.AppCompatTheme_textAppearanceListItemSmall);
                NetworkSpeedController.this.mBgHandler.sendEmptyMessage(R.styleable.AppCompatTheme_textAppearanceListItemSmall);
                NetworkSpeedController.this.postUpdateNetworkSpeed();
            } else if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                NetworkSpeedController.this.mBgHandler.removeMessages(100);
                NetworkSpeedController.this.mBgHandler.sendEmptyMessage(100);
                NetworkSpeedController.this.postUpdateNetworkSpeed();
            }
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private boolean mDisabled;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message message) {
            if (message.what == 200000) {
                int i = 0;
                boolean z = message.arg1 != 0;
                NetworkSpeedController networkSpeedController = NetworkSpeedController.this;
                if (!z) {
                    i = 8;
                }
                networkSpeedController.setVisibilityToViewList(i);
                if (z) {
                    long longValue = ((Long) message.obj).longValue();
                    NetworkSpeedController networkSpeedController2 = NetworkSpeedController.this;
                    networkSpeedController2.setTextToViewList(NetworkSpeedController.formatSpeed(networkSpeedController2.mContext, longValue));
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mIsNetworkConnected;
    private long mLastTime;
    private ContentObserver mNetworkSpeedObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            NetworkSpeedController.this.mBgHandler.removeMessages(R.styleable.AppCompatTheme_textAppearanceListItem);
            NetworkSpeedController.this.mBgHandler.sendEmptyMessage(R.styleable.AppCompatTheme_textAppearanceListItem);
            NetworkSpeedController.this.postUpdateNetworkSpeed();
        }
    };
    private int mNetworkUpdateInterval;
    private Uri mNetworkUri;
    private long mTotalBytes;
    private CopyOnWriteArrayList<NetworkSpeedView> mViewList = new CopyOnWriteArrayList<>();

    public NetworkSpeedController(@Inject Context context) {
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        initNetworkAssistantProviderUri();
        this.mBgHandler = new WorkHandler((Looper) Dependency.get(Dependency.NET_BG_LOOPER));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiverAsUser(this.mConnectivityReceiver, UserHandle.CURRENT, intentFilter, (String) null, this.mBgHandler);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_network_speed"), true, this.mNetworkSpeedObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_network_speed_interval"), true, this.mNetworkSpeedObserver, -1);
        this.mNetworkSpeedObserver.onChange(true);
    }

    public void addToViewList(NetworkSpeedView networkSpeedView) {
        this.mViewList.add(networkSpeedView);
        postUpdateNetworkSpeed();
    }

    public void removeFromViewList(NetworkSpeedView networkSpeedView) {
        this.mViewList.remove(networkSpeedView);
        if (this.mViewList.isEmpty()) {
            this.mBgHandler.removeCallbacksAndMessages((Object) null);
        }
    }

    /* access modifiers changed from: private */
    public void setTextToViewList(CharSequence charSequence) {
        CopyOnWriteArrayList<NetworkSpeedView> copyOnWriteArrayList = this.mViewList;
        if (copyOnWriteArrayList != null) {
            Iterator<NetworkSpeedView> it = copyOnWriteArrayList.iterator();
            while (it.hasNext()) {
                it.next().setText(charSequence);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setVisibilityToViewList(int i) {
        CopyOnWriteArrayList<NetworkSpeedView> copyOnWriteArrayList = this.mViewList;
        if (copyOnWriteArrayList != null) {
            Iterator<NetworkSpeedView> it = copyOnWriteArrayList.iterator();
            while (it.hasNext()) {
                NetworkSpeedView next = it.next();
                if (!next.isDriveMode() && !next.isNotch() && !next.isForceHide()) {
                    next.setVisibility(i);
                }
            }
        }
    }

    private void initNetworkAssistantProviderUri() {
        this.mNetworkUri = Uri.parse("content://com.miui.networkassistant.provider/na_traffic_stats");
    }

    private long getTotalByte() {
        Cursor query = this.mContext.getContentResolver().query(this.mNetworkUri, (String[]) null, (String) null, (String[]) null, (String) null);
        long j = 0;
        boolean z = false;
        if (query != null) {
            try {
                if (query.moveToFirst()) {
                    j = query.getLong(query.getColumnIndex("total_tx_byte")) + query.getLong(query.getColumnIndex("total_rx_byte"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                z = true;
            } catch (Throwable th) {
                query.close();
                throw th;
            }
            query.close();
        }
        return (z || query == null) ? TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes() : j;
    }

    public void postUpdateNetworkSpeed() {
        postUpdateNetworkSpeedDelay(0);
    }

    private void postUpdateNetworkSpeedDelay(long j) {
        this.mBgHandler.removeMessages(R.styleable.AppCompatTheme_textAppearanceListItemSecondary);
        this.mBgHandler.sendEmptyMessageDelayed(R.styleable.AppCompatTheme_textAppearanceListItemSecondary, j);
    }

    /* access modifiers changed from: private */
    public void updateSwitchState() {
        this.mDisabled = !MiuiStatusBarManager.isShowNetworkSpeedForUser(this.mContext, -2);
    }

    /* access modifiers changed from: private */
    public void updateInterval() {
        this.mNetworkUpdateInterval = Settings.System.getInt(this.mContext.getContentResolver(), "status_bar_network_speed_interval", 4000);
    }

    public boolean isDemoOrDrive() {
        if (this.mViewList.isEmpty()) {
            return true;
        }
        Iterator<NetworkSpeedView> it = this.mViewList.iterator();
        while (it.hasNext()) {
            NetworkSpeedView next = it.next();
            if (next != null && (next.isDemoMode() || next.isDriveMode())) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void updateNetworkSpeed() {
        if (!isDemoOrDrive()) {
            Message obtain = Message.obtain();
            obtain.what = 200000;
            long j = 0;
            if (this.mDisabled || !this.mIsNetworkConnected) {
                obtain.arg1 = 0;
                this.mHandler.removeMessages(200000);
                this.mHandler.sendMessage(obtain);
                this.mLastTime = 0;
                this.mTotalBytes = 0;
                return;
            }
            long currentTimeMillis = System.currentTimeMillis();
            long totalByte = getTotalByte();
            if (totalByte == 0) {
                this.mLastTime = 0;
                this.mTotalBytes = 0;
                totalByte = getTotalByte();
            }
            long j2 = this.mLastTime;
            if (j2 != 0 && currentTimeMillis > j2) {
                long j3 = this.mTotalBytes;
                if (!(j3 == 0 || totalByte == 0 || totalByte <= j3)) {
                    j = ((totalByte - j3) * 1000) / (currentTimeMillis - j2);
                }
            }
            obtain.arg1 = 1;
            obtain.obj = Long.valueOf(j);
            this.mHandler.removeMessages(200000);
            this.mHandler.sendMessage(obtain);
            this.mLastTime = currentTimeMillis;
            this.mTotalBytes = totalByte;
            postUpdateNetworkSpeedDelay((long) this.mNetworkUpdateInterval);
        }
    }

    /* access modifiers changed from: private */
    public void updateConnectedState() {
        NetworkInfo activeNetworkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        this.mIsNetworkConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /* access modifiers changed from: private */
    public static String formatSpeed(Context context, long j) {
        int i;
        String str;
        float f = ((float) j) / 1024.0f;
        if (f > 999.0f) {
            i = R.string.megabyte_per_second;
            f /= 1024.0f;
        } else {
            i = R.string.kilobyte_per_second;
        }
        if (f < 100.0f) {
            str = String.format("%.1f", new Object[]{Float.valueOf(f)});
        } else {
            str = String.format("%.0f", new Object[]{Float.valueOf(f)});
        }
        return context.getResources().getString(R.string.network_speed_suffix, new Object[]{str, context.getString(i)});
    }

    private final class WorkHandler extends Handler {
        WorkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case R.styleable.AppCompatTheme_textAppearanceLargePopupMenu:
                    NetworkSpeedController.this.updateSwitchState();
                    return;
                case R.styleable.AppCompatTheme_textAppearanceListItem:
                    NetworkSpeedController.this.updateSwitchState();
                    NetworkSpeedController.this.updateInterval();
                    return;
                case R.styleable.AppCompatTheme_textAppearanceListItemSecondary:
                    NetworkSpeedController.this.updateNetworkSpeed();
                    return;
                case R.styleable.AppCompatTheme_textAppearanceListItemSmall:
                    NetworkSpeedController.this.updateConnectedState();
                    return;
                default:
                    return;
            }
        }
    }
}

package com.android.systemui.statusbar;

import android.app.MiuiStatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import androidx.constraintlayout.widget.R$styleable;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.views.NetworkSpeedView;
import com.miui.systemui.statusbar.phone.DriveModeObserver;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkSpeedController implements DriveModeObserver.Callback {
    private Handler mBgHandler;
    private ConnectivityManager mConnectivityManager;
    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.NetworkSpeedController.AnonymousClass2 */

        public void onReceive(Context context, Intent intent) {
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                NetworkSpeedController.this.mBgHandler.removeMessages(R$styleable.Constraint_layout_goneMarginTop);
                NetworkSpeedController.this.mBgHandler.sendEmptyMessage(R$styleable.Constraint_layout_goneMarginTop);
                NetworkSpeedController.this.postUpdateNetworkSpeed();
            } else if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                NetworkSpeedController.this.mBgHandler.removeMessages(100);
                NetworkSpeedController.this.mBgHandler.sendEmptyMessage(100);
                NetworkSpeedController.this.postUpdateNetworkSpeed();
            }
        }
    };
    private Context mContext;
    private boolean mDisabled;
    protected boolean mDriveMode;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class com.android.systemui.statusbar.NetworkSpeedController.AnonymousClass3 */

        public void handleMessage(Message message) {
            if (message.what == 200000) {
                boolean z = message.arg1 != 0;
                NetworkSpeedController.this.setVisibilityToViewList(z);
                if (z) {
                    long longValue = ((Long) message.obj).longValue();
                    NetworkSpeedController networkSpeedController = NetworkSpeedController.this;
                    networkSpeedController.setTextToViewList(NetworkSpeedController.formatSpeed(networkSpeedController.mContext, longValue));
                }
            }
        }
    };
    private boolean mIsNetworkConnected;
    private long mLastTime;
    private ContentObserver mNetworkSpeedObserver = new ContentObserver(new Handler()) {
        /* class com.android.systemui.statusbar.NetworkSpeedController.AnonymousClass1 */

        public void onChange(boolean z) {
            NetworkSpeedController.this.mBgHandler.removeMessages(R$styleable.Constraint_layout_goneMarginRight);
            NetworkSpeedController.this.mBgHandler.sendEmptyMessage(R$styleable.Constraint_layout_goneMarginRight);
            NetworkSpeedController.this.postUpdateNetworkSpeed();
        }
    };
    private int mNetworkUpdateInterval;
    private long mTotalBytes;
    private CopyOnWriteArrayList<NetworkSpeedView> mViewList = new CopyOnWriteArrayList<>();

    public NetworkSpeedController(Context context, Looper looper) {
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mBgHandler = new WorkHandler(looper);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiverAsUser(this.mConnectivityReceiver, UserHandle.CURRENT, intentFilter, null, this.mBgHandler);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_network_speed"), true, this.mNetworkSpeedObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_network_speed_interval"), true, this.mNetworkSpeedObserver, -1);
        this.mNetworkSpeedObserver.onChange(true);
        ((DriveModeObserver) Dependency.get(DriveModeObserver.class)).addCallback(this);
    }

    public void addToViewList(NetworkSpeedView networkSpeedView) {
        this.mViewList.add(networkSpeedView);
        postUpdateNetworkSpeed();
    }

    public void removeFromViewList(NetworkSpeedView networkSpeedView) {
        this.mViewList.remove(networkSpeedView);
        postUpdateNetworkSpeed();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setTextToViewList(CharSequence charSequence) {
        CopyOnWriteArrayList<NetworkSpeedView> copyOnWriteArrayList = this.mViewList;
        if (copyOnWriteArrayList != null) {
            Iterator<NetworkSpeedView> it = copyOnWriteArrayList.iterator();
            while (it.hasNext()) {
                it.next().setText(charSequence);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void setVisibilityToViewList(boolean z) {
        CopyOnWriteArrayList<NetworkSpeedView> copyOnWriteArrayList = this.mViewList;
        if (copyOnWriteArrayList != null) {
            Iterator<NetworkSpeedView> it = copyOnWriteArrayList.iterator();
            while (it.hasNext()) {
                it.next().setVisibilityByController(z);
            }
        }
    }

    private long getTotalByte() {
        return TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
    }

    public void postUpdateNetworkSpeed() {
        postUpdateNetworkSpeedDelay(0);
    }

    private void postUpdateNetworkSpeedDelay(long j) {
        this.mBgHandler.removeMessages(R$styleable.Constraint_layout_goneMarginStart);
        this.mBgHandler.sendEmptyMessageDelayed(R$styleable.Constraint_layout_goneMarginStart, j);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateSwitchState() {
        this.mDisabled = !MiuiStatusBarManager.isShowNetworkSpeedForUser(this.mContext, -2);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateInterval() {
        this.mNetworkUpdateInterval = Settings.System.getInt(this.mContext.getContentResolver(), "status_bar_network_speed_interval", 4000);
    }

    public boolean isDemoOrDrive() {
        return this.mDriveMode;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNetworkSpeed() {
        Message obtain = Message.obtain();
        obtain.what = 200000;
        long j = 0;
        if (isDemoOrDrive() || this.mDisabled || !this.mIsNetworkConnected) {
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateConnectedState() {
        NetworkInfo activeNetworkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        this.mIsNetworkConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /* access modifiers changed from: private */
    public static String formatSpeed(Context context, long j) {
        String str;
        int i = C0021R$string.kilobyte_per_second;
        float f = ((float) j) / 1024.0f;
        if (f > 999.0f) {
            i = C0021R$string.megabyte_per_second;
            f /= 1024.0f;
        }
        if (f < 100.0f) {
            str = String.format("%.1f", Float.valueOf(f));
        } else {
            str = String.format("%.0f", Float.valueOf(f));
        }
        return context.getResources().getString(C0021R$string.network_speed_suffix, str, context.getString(i));
    }

    @Override // com.miui.systemui.statusbar.phone.DriveModeObserver.Callback
    public void onDriveModeChanged(boolean z) {
        this.mDriveMode = z;
        postUpdateNetworkSpeed();
    }

    private final class WorkHandler extends Handler {
        WorkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case R$styleable.Constraint_layout_goneMarginLeft:
                    NetworkSpeedController.this.updateSwitchState();
                    return;
                case R$styleable.Constraint_layout_goneMarginRight:
                    NetworkSpeedController.this.updateSwitchState();
                    NetworkSpeedController.this.updateInterval();
                    return;
                case R$styleable.Constraint_layout_goneMarginStart:
                    NetworkSpeedController.this.updateNetworkSpeed();
                    return;
                case R$styleable.Constraint_layout_goneMarginTop:
                    NetworkSpeedController.this.updateConnectedState();
                    return;
                default:
                    return;
            }
        }
    }
}

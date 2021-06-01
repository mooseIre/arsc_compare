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
import codeinjection.CodeInjection;
import com.android.systemui.C0021R$string;
import com.android.systemui.statusbar.views.NetworkSpeedView;
import java.util.ArrayList;
import java.util.Iterator;

public class NetworkSpeedController {
    private Handler mBgHandler;
    private ConnectivityManager mConnectivityManager;
    private BroadcastReceiver mConnectivityReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.statusbar.NetworkSpeedController.AnonymousClass4 */

        public void onReceive(Context context, Intent intent) {
            if ("android.net.conn.CONNECTIVITY_CHANGE".equals(intent.getAction())) {
                NetworkSpeedController.this.updateNetworkConnected();
            } else if ("android.intent.action.USER_SWITCHED".equals(intent.getAction())) {
                NetworkSpeedController.this.updateShowNetworkSpeed();
            }
        }
    };
    private Context mContext;
    private boolean mDriveMode;
    private ContentObserver mDriveModeObserver;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        /* class com.android.systemui.statusbar.NetworkSpeedController.AnonymousClass5 */

        public void handleMessage(Message message) {
            boolean booleanValue;
            boolean booleanValue2;
            boolean booleanValue3;
            switch (message.what) {
                case 100001:
                    Object obj = message.obj;
                    if ((obj instanceof Boolean) && (booleanValue = ((Boolean) obj).booleanValue()) != NetworkSpeedController.this.mNetworkConnected) {
                        NetworkSpeedController.this.mNetworkConnected = booleanValue;
                        NetworkSpeedController.this.updateVisibility();
                        if (NetworkSpeedController.this.mVisible) {
                            NetworkSpeedController.this.postUpdateNetworkSpeed();
                            return;
                        } else {
                            NetworkSpeedController.this.mBgHandler.removeMessages(200001);
                            return;
                        }
                    } else {
                        return;
                    }
                case 100002:
                    Object obj2 = message.obj;
                    if ((obj2 instanceof Boolean) && NetworkSpeedController.this.mShowNetworkSpeed != (booleanValue2 = ((Boolean) obj2).booleanValue())) {
                        NetworkSpeedController.this.mShowNetworkSpeed = booleanValue2;
                        NetworkSpeedController.this.updateVisibility();
                        if (NetworkSpeedController.this.mVisible) {
                            NetworkSpeedController.this.postUpdateNetworkSpeed();
                            return;
                        } else {
                            NetworkSpeedController.this.mBgHandler.removeMessages(200001);
                            return;
                        }
                    } else {
                        return;
                    }
                case 100003:
                    Object obj3 = message.obj;
                    if ((obj3 instanceof Boolean) && NetworkSpeedController.this.mDriveMode != (booleanValue3 = ((Boolean) obj3).booleanValue())) {
                        NetworkSpeedController.this.mDriveMode = booleanValue3;
                        NetworkSpeedController.this.updateVisibility();
                        if (NetworkSpeedController.this.mVisible) {
                            NetworkSpeedController.this.postUpdateNetworkSpeed();
                            return;
                        } else {
                            NetworkSpeedController.this.mBgHandler.removeMessages(200001);
                            return;
                        }
                    } else {
                        return;
                    }
                case 100004:
                    if (message.obj instanceof Long) {
                        NetworkSpeedController networkSpeedController = NetworkSpeedController.this;
                        networkSpeedController.updateText(NetworkSpeedController.formatSpeed(networkSpeedController.mContext, ((Long) message.obj).longValue()));
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private long mLastTime;
    private boolean mNetworkConnected;
    private boolean mShowNetworkSpeed;
    private ContentObserver mShowNetworkSpeedObserver;
    private String mText = CodeInjection.MD5;
    private long mTotalBytes;
    private ArrayList<NetworkSpeedView> mViewList = new ArrayList<>();
    private boolean mVisible;

    public NetworkSpeedController(Context context, Looper looper) {
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.mBgHandler = new WorkHandler(looper);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        this.mContext.registerReceiverAsUser(this.mConnectivityReceiver, UserHandle.CURRENT, intentFilter, null, this.mBgHandler);
        this.mShowNetworkSpeedObserver = new ContentObserver(this.mBgHandler) {
            /* class com.android.systemui.statusbar.NetworkSpeedController.AnonymousClass1 */

            public void onChange(boolean z) {
                NetworkSpeedController.this.updateShowNetworkSpeed();
            }
        };
        this.mDriveModeObserver = new ContentObserver(this.mBgHandler) {
            /* class com.android.systemui.statusbar.NetworkSpeedController.AnonymousClass2 */

            public void onChange(boolean z) {
                NetworkSpeedController.this.updateDriveMode();
            }
        };
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("status_bar_show_network_speed"), true, this.mShowNetworkSpeedObserver, -1);
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("drive_mode_drive_mode"), true, this.mDriveModeObserver, -1);
        this.mBgHandler.post(new Runnable() {
            /* class com.android.systemui.statusbar.NetworkSpeedController.AnonymousClass3 */

            public void run() {
                NetworkSpeedController.this.updateDriveMode();
                NetworkSpeedController.this.updateShowNetworkSpeed();
                NetworkSpeedController.this.updateNetworkConnected();
            }
        });
    }

    public void addToViewList(NetworkSpeedView networkSpeedView) {
        if (this.mViewList.indexOf(networkSpeedView) == -1) {
            this.mViewList.add(networkSpeedView);
            networkSpeedView.setNetworkSpeed(this.mText);
            networkSpeedView.setVisibilityByController(this.mVisible);
        }
    }

    public void removeFromViewList(NetworkSpeedView networkSpeedView) {
        this.mViewList.remove(networkSpeedView);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateShowNetworkSpeed() {
        this.mHandler.obtainMessage(100002, Boolean.valueOf(MiuiStatusBarManager.isShowNetworkSpeedForUser(this.mContext, -2))).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNetworkConnected() {
        NetworkInfo activeNetworkInfo = this.mConnectivityManager.getActiveNetworkInfo();
        this.mHandler.obtainMessage(100001, Boolean.valueOf(activeNetworkInfo != null && activeNetworkInfo.isConnected())).sendToTarget();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateDriveMode() {
        boolean z = false;
        if (Settings.System.getIntForUser(this.mContext.getContentResolver(), "drive_mode_drive_mode", 0, -2) == 1) {
            z = true;
        }
        this.mHandler.obtainMessage(100003, Boolean.valueOf(z)).sendToTarget();
    }

    private long getTotalByte() {
        return TrafficStats.getTotalRxBytes() + TrafficStats.getTotalTxBytes();
    }

    public void postUpdateNetworkSpeed() {
        postUpdateNetworkSpeedDelay(0);
    }

    private void postUpdateNetworkSpeedDelay(long j) {
        this.mBgHandler.removeMessages(200001);
        this.mBgHandler.sendEmptyMessageDelayed(200001, j);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateNetworkSpeed() {
        Message obtainMessage = this.mHandler.obtainMessage(100004);
        long currentTimeMillis = System.currentTimeMillis();
        long totalByte = getTotalByte();
        long j = this.mLastTime;
        long j2 = 0;
        if (j != 0 && currentTimeMillis > j) {
            long j3 = this.mTotalBytes;
            if (!(j3 == 0 || totalByte == 0 || totalByte <= j3)) {
                j2 = ((totalByte - j3) * 1000) / (currentTimeMillis - j);
            }
        }
        this.mLastTime = currentTimeMillis;
        this.mTotalBytes = totalByte;
        obtainMessage.obj = Long.valueOf(j2);
        obtainMessage.sendToTarget();
        postUpdateNetworkSpeedDelay(4000);
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

    private void clearNetworkSpeed() {
        this.mHandler.removeMessages(100004);
        this.mLastTime = 0;
        this.mTotalBytes = 0;
        updateText(CodeInjection.MD5);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateVisibility() {
        boolean z = !this.mDriveMode && this.mShowNetworkSpeed && this.mNetworkConnected;
        if (this.mVisible != z) {
            this.mVisible = z;
            if (!z) {
                clearNetworkSpeed();
            }
            Iterator<NetworkSpeedView> it = this.mViewList.iterator();
            while (it.hasNext()) {
                it.next().setVisibilityByController(z);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateText(String str) {
        if (!this.mText.equals(str)) {
            this.mText = str;
            Iterator<NetworkSpeedView> it = this.mViewList.iterator();
            while (it.hasNext()) {
                it.next().setNetworkSpeed(str);
            }
        }
    }

    private class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message message) {
            if (message.what == 200001) {
                NetworkSpeedController.this.updateNetworkSpeed();
            }
        }
    }
}

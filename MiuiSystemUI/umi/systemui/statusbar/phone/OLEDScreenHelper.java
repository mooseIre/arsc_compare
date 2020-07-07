package com.android.systemui.statusbar.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.phone.MiuiStatusBarPromptController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class OLEDScreenHelper {
    private static final boolean DEBUG = Constants.DEBUG;
    /* access modifiers changed from: private */
    public static final int DEFAULT_INTERVAL = ((int) TimeUnit.MINUTES.toMillis(2));
    private Context mContext;
    /* access modifiers changed from: private */
    public int mDirection;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 10001) {
                OLEDScreenHelper.this.update();
                OLEDScreenHelper oLEDScreenHelper = OLEDScreenHelper.this;
                int unused = oLEDScreenHelper.mDirection = OLEDScreenHelper.access$604(oLEDScreenHelper) % 4;
                OLEDScreenHelper.this.mHandler.sendEmptyMessageDelayed(10001, (long) OLEDScreenHelper.this.mInterval);
            }
        }
    };
    /* access modifiers changed from: private */
    public int mInterval;
    /* access modifiers changed from: private */
    public boolean mIsScreenOn;
    private MiuiStatusBarPromptController.OnPromptStateChangedListener mListener = new MiuiStatusBarPromptController.OnPromptStateChangedListener() {
        public void onPromptStateChanged(boolean z, String str) {
            if (Constants.IS_OLED_SCREEN) {
                if (z) {
                    OLEDScreenHelper.this.restart();
                    return;
                }
                OLEDScreenHelper oLEDScreenHelper = OLEDScreenHelper.this;
                oLEDScreenHelper.stop(oLEDScreenHelper.mIsScreenOn);
            }
        }
    };
    private NavigationBarView mNavigationBarView;
    /* access modifiers changed from: private */
    public int mPixels;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            OLEDScreenHelper oLEDScreenHelper = OLEDScreenHelper.this;
            oLEDScreenHelper.stop(oLEDScreenHelper.mIsScreenOn);
            int unused = OLEDScreenHelper.this.mInterval = intent.getIntExtra("interval", OLEDScreenHelper.DEFAULT_INTERVAL);
            int unused2 = OLEDScreenHelper.this.mPixels = intent.getIntExtra("pixels", 3);
            OLEDScreenHelper oLEDScreenHelper2 = OLEDScreenHelper.this;
            oLEDScreenHelper2.start(oLEDScreenHelper2.mIsScreenOn);
        }
    };
    private int mStatusBarMode;
    private View mStatusBarView;

    static /* synthetic */ int access$604(OLEDScreenHelper oLEDScreenHelper) {
        int i = oLEDScreenHelper.mDirection + 1;
        oLEDScreenHelper.mDirection = i;
        return i;
    }

    public OLEDScreenHelper(Context context) {
        Log.d("OLEDScreenHelper", String.format("IS_OLED_SCREEN=%b", new Object[]{Boolean.valueOf(Constants.IS_OLED_SCREEN)}));
        this.mContext = context;
        this.mInterval = DEFAULT_INTERVAL;
        this.mPixels = 3;
        ((MiuiStatusBarPromptController) Dependency.get(MiuiStatusBarPromptController.class)).addPromptStateChangedListener("OLEDScreenHelper", this.mListener);
        if (DEBUG) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("systemui.oled.strategy");
            this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, intentFilter, (String) null, (Handler) null);
        }
    }

    public void setStatusBarView(View view) {
        if (Constants.IS_OLED_SCREEN) {
            this.mStatusBarView = view;
            view.post(new Runnable() {
                public final void run() {
                    OLEDScreenHelper.this.lambda$setStatusBarView$0$OLEDScreenHelper();
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setStatusBarView$0 */
    public /* synthetic */ void lambda$setStatusBarView$0$OLEDScreenHelper() {
        ((View) this.mStatusBarView.getParent()).setTouchDelegate(new FullTouchDelegate(this.mStatusBarView));
    }

    public void setNavigationBarView(NavigationBarView navigationBarView) {
        if (Constants.IS_OLED_SCREEN) {
            this.mNavigationBarView = navigationBarView;
        }
    }

    public void onStatusBarModeChanged(int i) {
        if (Constants.IS_OLED_SCREEN && this.mStatusBarMode != i) {
            if (i == 4 || i == 2 || i == 6) {
                restart();
            } else {
                stop(this.mIsScreenOn);
            }
            this.mStatusBarMode = i;
        }
    }

    public void start(boolean z) {
        this.mIsScreenOn = z;
        if (Constants.IS_OLED_SCREEN) {
            if (DEBUG) {
                Log.d("OLEDScreenHelper", String.format("start isScreenOn=%b", new Object[]{Boolean.valueOf(z)}));
            }
            if (z && !this.mHandler.hasMessages(10001)) {
                this.mDirection = generateRandomDirection();
                this.mHandler.sendEmptyMessageDelayed(10001, (long) this.mInterval);
            }
        }
    }

    public void stop(boolean z) {
        this.mIsScreenOn = z;
        if (Constants.IS_OLED_SCREEN) {
            if (DEBUG) {
                Log.d("OLEDScreenHelper", "stop");
            }
            this.mHandler.removeMessages(10001);
            resetView(this.mStatusBarView);
            NavigationBarView navigationBarView = this.mNavigationBarView;
            if (navigationBarView != null) {
                resetView(navigationBarView.getRecentsButton());
                resetView(this.mNavigationBarView.getHomeButton());
                resetView(this.mNavigationBarView.getBackButton());
                resetNavigationHandle();
            }
        }
    }

    private void resetNavigationHandle() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null && navigationBarView.getNavigationHandle() != null) {
            this.mNavigationBarView.getNavigationHandle().resetNavigationHandleFromOLEDHelper();
        }
    }

    private void resetView(View view) {
        if (view != null) {
            view.setTranslationX(0.0f);
            view.setTranslationY(0.0f);
        }
    }

    public void onConfigurationChanged() {
        restart();
    }

    /* access modifiers changed from: private */
    public void restart() {
        stop(this.mIsScreenOn);
        start(this.mIsScreenOn);
    }

    /* access modifiers changed from: private */
    public void update() {
        if (DEBUG) {
            Log.d("OLEDScreenHelper", String.format("update mDirection=%d mInterval=%d mPixels=%d", new Object[]{Integer.valueOf(this.mDirection), Integer.valueOf(this.mInterval), Integer.valueOf(this.mPixels)}));
        }
        updateView(this.mStatusBarView);
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null) {
            updateView(navigationBarView.getRecentsButton());
            updateView(this.mNavigationBarView.getHomeButton());
            updateView(this.mNavigationBarView.getBackButton());
            updateNavigationHandle();
        }
    }

    private void updateNavigationHandle() {
        NavigationBarView navigationBarView = this.mNavigationBarView;
        if (navigationBarView != null && navigationBarView.getNavigationHandle() != null) {
            this.mNavigationBarView.getNavigationHandle().updateNavigationHandleFromOLEDHelper();
        }
    }

    private void updateView(View view) {
        if (view != null && view.isShown()) {
            float translationX = view.getTranslationX();
            float translationY = view.getTranslationY();
            int i = this.mDirection;
            if (i == 0) {
                view.setTranslationX(translationX - ((float) this.mPixels));
            } else if (i == 1) {
                view.setTranslationY(translationY - ((float) this.mPixels));
            } else if (i == 2) {
                view.setTranslationX(translationX + ((float) this.mPixels));
            } else if (i == 3) {
                view.setTranslationY(translationY + ((float) this.mPixels));
            }
        }
    }

    private int generateRandomDirection() {
        return new Random(SystemClock.uptimeMillis()).nextInt(4);
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        Object[] objArr = new Object[6];
        String str = "T";
        objArr[0] = Constants.IS_OLED_SCREEN ? str : "f";
        objArr[1] = Integer.valueOf(this.mDirection);
        objArr[2] = Long.valueOf(TimeUnit.MILLISECONDS.toSeconds((long) this.mInterval));
        objArr[3] = Integer.valueOf(this.mPixels);
        objArr[4] = Integer.valueOf(this.mStatusBarMode);
        if (!this.mIsScreenOn) {
            str = "f";
        }
        objArr[5] = str;
        printWriter.println(String.format("  OLEDScreenHelper: [IS_OLED_SCREEN=%s mDirection=%d mInterval=%d mPixels=%d mStatusBarMode=%d mIsScreenOn=%s]", objArr));
    }

    private static class FullTouchDelegate extends TouchDelegate {
        private View mDelegateView;

        public FullTouchDelegate(View view) {
            super(new Rect(), view);
            this.mDelegateView = view;
        }

        public boolean onTouchEvent(MotionEvent motionEvent) {
            return this.mDelegateView.dispatchTouchEvent(motionEvent);
        }
    }
}

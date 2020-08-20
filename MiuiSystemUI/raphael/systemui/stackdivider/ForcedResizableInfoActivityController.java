package com.android.systemui.stackdivider;

import android.app.ActivityOptions;
import android.app.ActivityOptionsCompat;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.ArraySet;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.AppTransitionFinishedEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.stackdivider.events.StartedDragingEvent;
import com.android.systemui.stackdivider.events.StoppedDragingEvent;

public class ForcedResizableInfoActivityController {
    /* access modifiers changed from: private */
    public final Context mContext;
    private boolean mDividerDraging;
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler();
    private final ArraySet<String> mPackagesShownInSession = new ArraySet<>();
    private final ArraySet<Integer> mPendingTaskIds = new ArraySet<>();
    private final Runnable mTimeoutRunnable = new Runnable() {
        public void run() {
            ForcedResizableInfoActivityController.this.showPending();
        }
    };

    public ForcedResizableInfoActivityController(Context context) {
        this.mContext = context;
        RecentsEventBus.getDefault().register(this);
        SystemServicesProxy.getInstance(context).registerTaskStackListener(new SystemServicesProxy.TaskStackListener() {
            public void onActivityForcedResizable(String str, int i, int i2) {
                ForcedResizableInfoActivityController.this.activityForcedResizable(str, i);
            }

            public void onActivityDismissingDockedStack() {
                ForcedResizableInfoActivityController.this.activityDismissingDockedStack();
            }
        });
    }

    public void notifyDockedStackExistsChanged(boolean z) {
        if (!z) {
            this.mPackagesShownInSession.clear();
        }
    }

    public final void onBusEvent(AppTransitionFinishedEvent appTransitionFinishedEvent) {
        if (!this.mDividerDraging) {
            showPending();
        }
    }

    public final void onBusEvent(StartedDragingEvent startedDragingEvent) {
        this.mDividerDraging = true;
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
    }

    public final void onBusEvent(StoppedDragingEvent stoppedDragingEvent) {
        this.mDividerDraging = false;
        showPending();
    }

    /* access modifiers changed from: private */
    public void activityForcedResizable(String str, int i) {
        if (!debounce(str)) {
            this.mPendingTaskIds.add(Integer.valueOf(i));
            postTimeout();
        }
    }

    /* access modifiers changed from: private */
    public void activityDismissingDockedStack() {
        BackgroundThread.getHandler().post(new Runnable() {
            public void run() {
                if (((KeyguardManager) ForcedResizableInfoActivityController.this.mContext.getSystemService("keyguard")).isKeyguardLocked()) {
                    ForcedResizableInfoActivityController.this.mHandler.post(new Runnable() {
                        public void run() {
                            ForcedResizableInfoActivityController.this.showToast(R.string.dock_keyguard_locked_failed_to_dock_text);
                        }
                    });
                } else {
                    ForcedResizableInfoActivityController.this.mHandler.post(new Runnable() {
                        public void run() {
                            ForcedResizableInfoActivityController.this.showToast(R.string.dock_non_resizeble_failed_to_dock_text);
                        }
                    });
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void showToast(int i) {
        Toast.makeText(this.mContext, i, 0).show();
    }

    /* access modifiers changed from: private */
    public void showPending() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        for (int size = this.mPendingTaskIds.size() - 1; size >= 0; size--) {
            Intent intent = new Intent(this.mContext, ForcedResizableInfoActivity.class);
            intent.addFlags(268435456);
            ActivityOptions makeBasic = ActivityOptions.makeBasic();
            if (Utilities.isAndroidNorNewer()) {
                ActivityOptionsCompat.setLaunchTaskId(makeBasic, this.mPendingTaskIds.valueAt(size).intValue());
                ActivityOptionsCompat.setTaskOverlay(makeBasic, true, true);
            } else if (!Utilities.isAndroidNorNewer()) {
                ActivityOptionsCompat.setLaunchStackId(makeBasic, this.mPendingTaskIds.valueAt(size).intValue(), -1, -1);
            }
            try {
                this.mContext.startActivity(intent, makeBasic.toBundle());
            } catch (Exception e) {
                Log.e("ForcedResizableInfoActivityController", "Start ForcedResizableInfoActivity error.", e);
            }
        }
        this.mPendingTaskIds.clear();
    }

    private void postTimeout() {
        this.mHandler.removeCallbacks(this.mTimeoutRunnable);
        this.mHandler.postDelayed(this.mTimeoutRunnable, 1000);
    }

    private boolean debounce(String str) {
        if (str == null) {
            return false;
        }
        if ("com.android.systemui".equals(str)) {
            return true;
        }
        boolean contains = this.mPackagesShownInSession.contains(str);
        this.mPackagesShownInSession.add(str);
        return contains;
    }
}

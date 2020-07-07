package com.android.systemui.pip.tv;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.IActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ParceledListSlice;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.os.Debug;
import android.os.Handler;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.DisplayInfo;
import android.view.IPinnedStackController;
import android.view.IPinnedStackListener;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.systemui.pip.BasePipManager;
import com.android.systemui.pip.PipUIHelper;
import com.android.systemui.pip.tv.PipManager;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.misc.SystemServicesProxy;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import miui.view.MiuiHapticFeedbackConstants;

public class PipManager implements BasePipManager {
    static final boolean DEBUG = Log.isLoggable("PipManager", 3);
    private static PipManager sPipManager;
    private static List<Pair<String, String>> sSettingsPackageAndClassNamePairList;
    /* access modifiers changed from: private */
    public final MediaSessionManager.OnActiveSessionsChangedListener mActiveMediaSessionListener = new MediaSessionManager.OnActiveSessionsChangedListener() {
        public void onActiveSessionsChanged(List<MediaController> list) {
            PipManager.this.updateMediaController(list);
        }
    };
    private IActivityManager mActivityManager;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.MEDIA_RESOURCE_GRANTED".equals(intent.getAction())) {
                String[] stringArrayExtra = intent.getStringArrayExtra("android.intent.extra.PACKAGES");
                int intExtra = intent.getIntExtra("android.intent.extra.MEDIA_RESOURCE_TYPE", -1);
                if (stringArrayExtra != null && stringArrayExtra.length > 0 && intExtra == 0) {
                    PipManager.this.handleMediaResourceGranted(stringArrayExtra);
                }
            }
        }
    };
    private final Runnable mClosePipRunnable = new Runnable() {
        public void run() {
            PipManager.this.closePip();
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public Rect mCurrentPipBounds;
    /* access modifiers changed from: private */
    public ParceledListSlice mCustomActions;
    /* access modifiers changed from: private */
    public Rect mDefaultPipBounds = new Rect();
    /* access modifiers changed from: private */
    public final Handler mHandler = new Handler();
    private boolean mInitialized;
    private int mLastOrientation = 0;
    private String[] mLastPackagesResourceGranted;
    /* access modifiers changed from: private */
    public List<Listener> mListeners = new ArrayList();
    private List<MediaListener> mMediaListeners = new ArrayList();
    /* access modifiers changed from: private */
    public MediaSessionManager mMediaSessionManager;
    private Rect mMenuModePipBounds;
    private final PinnedStackListener mPinnedStackListener = new PinnedStackListener();
    /* access modifiers changed from: private */
    public Rect mPipBounds;
    /* access modifiers changed from: private */
    public ComponentName mPipComponentName;
    private MediaController mPipMediaController;
    private PipNotification mPipNotification;
    /* access modifiers changed from: private */
    public int mPipTaskId = -1;
    private final Runnable mResizePinnedStackRunnable = new Runnable() {
        public void run() {
            PipManager pipManager = PipManager.this;
            pipManager.resizePinnedStack(pipManager.mResumeResizePinnedStackRunnableState);
        }
    };
    /* access modifiers changed from: private */
    public int mResumeResizePinnedStackRunnableState = 0;
    /* access modifiers changed from: private */
    public Rect mSettingsPipBounds;
    /* access modifiers changed from: private */
    public int mState = 0;
    private int mSuspendPipResizingReason;
    private SystemServicesProxy.TaskStackListener mTaskStackListener = new SystemServicesProxy.TaskStackListener() {
        public void onTaskStackChanged() {
            int[] iArr;
            if (PipManager.DEBUG) {
                Log.d("PipManager", "onTaskStackChanged()");
            }
            if (checkCurrentUserId(PipManager.this.mContext, PipManager.DEBUG)) {
                if (PipManager.this.getState() != 0) {
                    ActivityManager.StackInfo access$1000 = PipManager.this.getPinnedStackInfo();
                    boolean z = false;
                    if (access$1000 == null || (iArr = access$1000.taskIds) == null) {
                        Log.w("PipManager", "There is nothing in pinned stack");
                        PipManager.this.closePipInternal(false);
                        return;
                    }
                    int length = iArr.length - 1;
                    while (true) {
                        if (length < 0) {
                            break;
                        } else if (access$1000.taskIds[length] == PipManager.this.mPipTaskId) {
                            z = true;
                            break;
                        } else {
                            length--;
                        }
                    }
                    if (!z) {
                        PipManager.this.closePipInternal(true);
                        return;
                    }
                }
                if (PipManager.this.getState() == 1) {
                    Rect access$1400 = PipManager.this.isSettingsShown() ? PipManager.this.mSettingsPipBounds : PipManager.this.mDefaultPipBounds;
                    if (PipManager.this.mPipBounds != access$1400) {
                        Rect unused = PipManager.this.mPipBounds = access$1400;
                        PipManager.this.resizePinnedStack(1);
                    }
                }
            }
        }

        public void onActivityPinned(String str, int i, int i2) {
            if (PipManager.DEBUG) {
                Log.d("PipManager", "onActivityPinned()");
            }
            if (checkCurrentUserId(PipManager.this.mContext, PipManager.DEBUG)) {
                ActivityManager.StackInfo access$1000 = PipManager.this.getPinnedStackInfo();
                if (access$1000 == null) {
                    Log.w("PipManager", "Cannot find pinned stack");
                    return;
                }
                if (PipManager.DEBUG) {
                    Log.d("PipManager", "PINNED_STACK:" + access$1000);
                }
                PipManager pipManager = PipManager.this;
                int[] iArr = access$1000.taskIds;
                int unused = pipManager.mPipTaskId = iArr[iArr.length - 1];
                PipManager pipManager2 = PipManager.this;
                String[] strArr = access$1000.taskNames;
                ComponentName unused2 = pipManager2.mPipComponentName = ComponentName.unflattenFromString(strArr[strArr.length - 1]);
                int unused3 = PipManager.this.mState = 1;
                PipManager pipManager3 = PipManager.this;
                Rect unused4 = pipManager3.mCurrentPipBounds = pipManager3.mPipBounds;
                PipManager.this.mMediaSessionManager.addOnActiveSessionsChangedListener(PipManager.this.mActiveMediaSessionListener, (ComponentName) null);
                PipManager pipManager4 = PipManager.this;
                pipManager4.updateMediaController(pipManager4.mMediaSessionManager.getActiveSessions((ComponentName) null));
                for (int size = PipManager.this.mListeners.size() - 1; size >= 0; size--) {
                    ((Listener) PipManager.this.mListeners.get(size)).onPipEntered();
                }
                PipManager.this.updatePipVisibility(true);
            }
        }

        public void onPinnedActivityRestartAttempt(boolean z) {
            if (PipManager.DEBUG) {
                Log.d("PipManager", "onPinnedActivityRestartAttempt()");
            }
            if (checkCurrentUserId(PipManager.this.mContext, PipManager.DEBUG)) {
                PipManager.this.movePipToFullscreen();
            }
        }

        public void onPinnedStackAnimationEnded() {
            if (PipManager.DEBUG) {
                Log.d("PipManager", "onPinnedStackAnimationEnded()");
            }
            if (checkCurrentUserId(PipManager.this.mContext, PipManager.DEBUG) && PipManager.this.getState() == 2) {
                PipManager.this.showPipMenu();
            }
        }
    };
    private IWindowManager mWindowManager;

    public interface Listener {
        void onMoveToFullscreen();

        void onPipActivityClosed();

        void onPipEntered();

        void onPipMenuActionsChanged(ParceledListSlice parceledListSlice);

        void onPipResizeAboutToStart();

        void onShowPipMenu();
    }

    public interface MediaListener {
        void onMediaControllerChanged();
    }

    public void dump(PrintWriter printWriter) {
    }

    private class PinnedStackListener extends IPinnedStackListener.Stub {
        public void onActivityHidden(ComponentName componentName) {
        }

        public void onAspectRatioChanged(float f) {
        }

        public void onConfigurationChanged() {
        }

        public void onDisplayInfoChanged(DisplayInfo displayInfo) {
        }

        public void onImeVisibilityChanged(boolean z, int i) {
        }

        public void onListenerRegistered(IPinnedStackController iPinnedStackController) {
        }

        public void onMovementBoundsChanged(boolean z) {
        }

        private PinnedStackListener() {
        }

        public void onActionsChanged(ParceledListSlice parceledListSlice) {
            ParceledListSlice unused = PipManager.this.mCustomActions = parceledListSlice;
            PipManager.this.mHandler.post(new Runnable() {
                public final void run() {
                    PipManager.PinnedStackListener.this.lambda$onActionsChanged$1$PipManager$PinnedStackListener();
                }
            });
        }

        /* access modifiers changed from: private */
        /* renamed from: lambda$onActionsChanged$1 */
        public /* synthetic */ void lambda$onActionsChanged$1$PipManager$PinnedStackListener() {
            for (int size = PipManager.this.mListeners.size() - 1; size >= 0; size--) {
                ((Listener) PipManager.this.mListeners.get(size)).onPipMenuActionsChanged(PipManager.this.mCustomActions);
            }
        }
    }

    private PipManager() {
    }

    public void initialize(Context context) {
        if (!this.mInitialized) {
            this.mInitialized = true;
            this.mContext = context;
            this.mActivityManager = ActivityManagerCompat.getService();
            this.mWindowManager = WindowManagerGlobal.getWindowManagerService();
            SystemServicesProxy.getInstance(context).registerTaskStackListener(this.mTaskStackListener);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.MEDIA_RESOURCE_GRANTED");
            this.mContext.registerReceiver(this.mBroadcastReceiver, intentFilter);
            if (sSettingsPackageAndClassNamePairList == null) {
                String[] stringArray = this.mContext.getResources().getStringArray(R.array.tv_pip_settings_class_name);
                sSettingsPackageAndClassNamePairList = new ArrayList();
                if (stringArray != null) {
                    for (int i = 0; i < stringArray.length; i++) {
                        String[] split = stringArray[i].split("/");
                        int length = split.length;
                        Pair pair = null;
                        if (length == 1) {
                            pair = Pair.create(split[0], (Object) null);
                        } else if (length == 2 && split[1] != null && split[1].startsWith(".")) {
                            pair = Pair.create(split[0], split[0] + split[1]);
                        }
                        if (pair != null) {
                            sSettingsPackageAndClassNamePairList.add(pair);
                        } else {
                            Log.w("PipManager", "Ignoring malformed settings name " + stringArray[i]);
                        }
                    }
                }
            }
            Configuration configuration = this.mContext.getResources().getConfiguration();
            this.mLastOrientation = configuration.orientation;
            loadConfigurationsAndApply(configuration);
            this.mMediaSessionManager = (MediaSessionManager) this.mContext.getSystemService("media_session");
            try {
                this.mWindowManager.registerPinnedStackListener(0, this.mPinnedStackListener);
            } catch (RemoteException e) {
                Log.e("PipManager", "Failed to register pinned stack listener", e);
            }
            this.mPipNotification = new PipNotification(context);
        }
    }

    private void loadConfigurationsAndApply(Configuration configuration) {
        int i = this.mLastOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mLastOrientation = i2;
            return;
        }
        Resources resources = this.mContext.getResources();
        this.mSettingsPipBounds = Rect.unflattenFromString(resources.getString(R.string.pip_settings_bounds));
        this.mMenuModePipBounds = Rect.unflattenFromString(resources.getString(R.string.pip_menu_bounds));
        this.mPipBounds = isSettingsShown() ? this.mSettingsPipBounds : this.mDefaultPipBounds;
        resizePinnedStack(getPinnedStackInfo() == null ? 0 : 1);
    }

    public void onConfigurationChanged(Configuration configuration) {
        loadConfigurationsAndApply(configuration);
        this.mPipNotification.onConfigurationChanged(this.mContext);
    }

    public void showPictureInPictureMenu() {
        if (getState() == 1) {
            resizePinnedStack(2);
        }
    }

    public void closePip() {
        closePipInternal(true);
    }

    /* access modifiers changed from: private */
    public void closePipInternal(boolean z) {
        this.mState = 0;
        this.mPipTaskId = -1;
        this.mPipMediaController = null;
        this.mMediaSessionManager.removeOnActiveSessionsChangedListener(this.mActiveMediaSessionListener);
        if (z) {
            try {
                this.mActivityManager.removeStack(4);
            } catch (RemoteException e) {
                Log.e("PipManager", "removeStack failed", e);
            }
        }
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onPipActivityClosed();
        }
        this.mHandler.removeCallbacks(this.mClosePipRunnable);
        updatePipVisibility(false);
    }

    /* access modifiers changed from: package-private */
    public void movePipToFullscreen() {
        this.mPipTaskId = -1;
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onMoveToFullscreen();
        }
        resizePinnedStack(0);
        updatePipVisibility(false);
    }

    public void suspendPipResizing(int i) {
        if (DEBUG) {
            Log.d("PipManager", "suspendPipResizing() reason=" + i + " callers=" + Debug.getCallers(2));
        }
        this.mSuspendPipResizingReason = i | this.mSuspendPipResizingReason;
    }

    public void resumePipResizing(int i) {
        if ((this.mSuspendPipResizingReason & i) != 0) {
            if (DEBUG) {
                Log.d("PipManager", "resumePipResizing() reason=" + i + " callers=" + Debug.getCallers(2));
            }
            this.mSuspendPipResizingReason = (~i) & this.mSuspendPipResizingReason;
            this.mHandler.post(this.mResizePinnedStackRunnable);
        }
    }

    /* access modifiers changed from: package-private */
    public void resizePinnedStack(int i) {
        if (DEBUG) {
            Log.d("PipManager", "resizePinnedStack() state=" + i, new Exception());
        }
        boolean z = this.mState == 0;
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onPipResizeAboutToStart();
        }
        if (this.mSuspendPipResizingReason != 0) {
            this.mResumeResizePinnedStackRunnableState = i;
            if (DEBUG) {
                Log.d("PipManager", "resizePinnedStack() deferring mSuspendPipResizingReason=" + this.mSuspendPipResizingReason + " mResumeResizePinnedStackRunnableState=" + this.mResumeResizePinnedStackRunnableState);
                return;
            }
            return;
        }
        this.mState = i;
        if (i == 0 && z) {
        }
    }

    /* access modifiers changed from: private */
    public int getState() {
        if (this.mSuspendPipResizingReason != 0) {
            return this.mResumeResizePinnedStackRunnableState;
        }
        return this.mState;
    }

    /* access modifiers changed from: private */
    public void showPipMenu() {
        if (DEBUG) {
            Log.d("PipManager", "showPipMenu()");
        }
        this.mState = 2;
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onShowPipMenu();
        }
        Intent intent = new Intent(this.mContext, PipMenuActivity.class);
        intent.setFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
        intent.putExtra("custom_actions", this.mCustomActions);
        this.mContext.startActivity(intent);
    }

    public void addListener(Listener listener) {
        this.mListeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.mListeners.remove(listener);
    }

    public void addMediaListener(MediaListener mediaListener) {
        this.mMediaListeners.add(mediaListener);
    }

    public void removeMediaListener(MediaListener mediaListener) {
        this.mMediaListeners.remove(mediaListener);
    }

    /* access modifiers changed from: private */
    public ActivityManager.StackInfo getPinnedStackInfo() {
        try {
            return ActivityManagerCompat.getStackInfo(4, 2, 0);
        } catch (Exception e) {
            Log.e("PipManager", "getStackInfo failed", e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void handleMediaResourceGranted(String[] strArr) {
        if (getState() == 0) {
            this.mLastPackagesResourceGranted = strArr;
            return;
        }
        String[] strArr2 = this.mLastPackagesResourceGranted;
        boolean z = false;
        if (strArr2 != null) {
            boolean z2 = false;
            for (String str : strArr2) {
                int length = strArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (TextUtils.equals(strArr[i], str)) {
                        z2 = true;
                        break;
                    } else {
                        i++;
                    }
                }
            }
            z = z2;
        }
        this.mLastPackagesResourceGranted = strArr;
        if (!z) {
            closePip();
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:24:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateMediaController(java.util.List<android.media.session.MediaController> r5) {
        /*
            r4 = this;
            if (r5 == 0) goto L_0x002e
            int r0 = r4.getState()
            if (r0 == 0) goto L_0x002e
            android.content.ComponentName r0 = r4.mPipComponentName
            if (r0 == 0) goto L_0x002e
            int r0 = r5.size()
            int r0 = r0 + -1
        L_0x0012:
            if (r0 < 0) goto L_0x002e
            java.lang.Object r1 = r5.get(r0)
            android.media.session.MediaController r1 = (android.media.session.MediaController) r1
            java.lang.String r2 = r1.getPackageName()
            android.content.ComponentName r3 = r4.mPipComponentName
            java.lang.String r3 = r3.getPackageName()
            boolean r2 = r2.equals(r3)
            if (r2 == 0) goto L_0x002b
            goto L_0x002f
        L_0x002b:
            int r0 = r0 + -1
            goto L_0x0012
        L_0x002e:
            r1 = 0
        L_0x002f:
            android.media.session.MediaController r5 = r4.mPipMediaController
            if (r5 == r1) goto L_0x0062
            r4.mPipMediaController = r1
            java.util.List<com.android.systemui.pip.tv.PipManager$MediaListener> r5 = r4.mMediaListeners
            int r5 = r5.size()
            int r5 = r5 + -1
        L_0x003d:
            if (r5 < 0) goto L_0x004d
            java.util.List<com.android.systemui.pip.tv.PipManager$MediaListener> r0 = r4.mMediaListeners
            java.lang.Object r0 = r0.get(r5)
            com.android.systemui.pip.tv.PipManager$MediaListener r0 = (com.android.systemui.pip.tv.PipManager.MediaListener) r0
            r0.onMediaControllerChanged()
            int r5 = r5 + -1
            goto L_0x003d
        L_0x004d:
            android.media.session.MediaController r5 = r4.mPipMediaController
            if (r5 != 0) goto L_0x005b
            android.os.Handler r5 = r4.mHandler
            java.lang.Runnable r4 = r4.mClosePipRunnable
            r0 = 3000(0xbb8, double:1.482E-320)
            r5.postDelayed(r4, r0)
            goto L_0x0062
        L_0x005b:
            android.os.Handler r5 = r4.mHandler
            java.lang.Runnable r4 = r4.mClosePipRunnable
            r5.removeCallbacks(r4)
        L_0x0062:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.pip.tv.PipManager.updateMediaController(java.util.List):void");
    }

    /* access modifiers changed from: package-private */
    public MediaController getMediaController() {
        return this.mPipMediaController;
    }

    /* access modifiers changed from: package-private */
    public int getPlaybackState() {
        MediaController mediaController = this.mPipMediaController;
        if (!(mediaController == null || mediaController.getPlaybackState() == null)) {
            int state = this.mPipMediaController.getPlaybackState().getState();
            boolean z = state == 6 || state == 8 || state == 3 || state == 4 || state == 5 || state == 9 || state == 10;
            long actions = this.mPipMediaController.getPlaybackState().getActions();
            if (!z && (4 & actions) != 0) {
                return 1;
            }
            if (!z || (actions & 2) == 0) {
                return 2;
            }
            return 0;
        }
        return 2;
    }

    /* access modifiers changed from: private */
    public boolean isSettingsShown() {
        String str;
        try {
            List<ActivityManager.RunningTaskInfo> tasks = PipUIHelper.getTasks(this.mActivityManager, 1, 0);
            if (!(tasks == null || tasks.size() == 0)) {
                ComponentName componentName = tasks.get(0).topActivity;
                for (Pair next : sSettingsPackageAndClassNamePairList) {
                    if (componentName.getPackageName().equals((String) next.first) && ((str = (String) next.second) == null || componentName.getClassName().equals(str))) {
                        return true;
                    }
                }
            }
            return false;
        } catch (Exception e) {
            Log.d("PipManager", "Failed to detect top activity", e);
            return false;
        }
    }

    public static PipManager getInstance() {
        if (sPipManager == null) {
            sPipManager = new PipManager();
        }
        return sPipManager;
    }

    /* access modifiers changed from: private */
    public void updatePipVisibility(boolean z) {
        SystemServicesProxy.getInstance(this.mContext).setPipVisibility(z);
    }
}

package com.android.systemui.pip.tv;

import android.app.ActivityManager;
import android.app.ActivityTaskManager;
import android.app.IActivityTaskManager;
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
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.DisplayInfo;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.UiOffloadThread;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.android.systemui.pip.BasePipManager;
import com.android.systemui.pip.PipBoundsHandler;
import com.android.systemui.pip.PipSurfaceTransactionHelper;
import com.android.systemui.pip.PipTaskOrganizer;
import com.android.systemui.pip.tv.PipManager;
import com.android.systemui.shared.system.ActivityManagerWrapper;
import com.android.systemui.shared.system.PinnedStackListenerForwarder;
import com.android.systemui.shared.system.TaskStackChangeListener;
import com.android.systemui.shared.system.WindowManagerWrapper;
import com.android.systemui.stackdivider.Divider;
import java.util.ArrayList;
import java.util.List;

public class PipManager implements BasePipManager, PipTaskOrganizer.PipTransitionCallback {
    static final boolean DEBUG = Log.isLoggable("PipManager", 3);
    private static List<Pair<String, String>> sSettingsPackageAndClassNamePairList;
    private final MediaSessionManager.OnActiveSessionsChangedListener mActiveMediaSessionListener = new MediaSessionManager.OnActiveSessionsChangedListener() {
        /* class com.android.systemui.pip.tv.PipManager.AnonymousClass4 */

        @Override // android.media.session.MediaSessionManager.OnActiveSessionsChangedListener
        public void onActiveSessionsChanged(List<MediaController> list) {
            PipManager.this.updateMediaController(list);
        }
    };
    private IActivityTaskManager mActivityTaskManager;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.systemui.pip.tv.PipManager.AnonymousClass3 */

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
        /* class com.android.systemui.pip.tv.PipManager.AnonymousClass2 */

        public void run() {
            PipManager.this.closePip();
        }
    };
    private Context mContext;
    private Rect mCurrentPipBounds;
    private ParceledListSlice mCustomActions;
    private Rect mDefaultPipBounds = new Rect();
    private final Handler mHandler = new Handler();
    private int mImeHeightAdjustment;
    private boolean mImeVisible;
    private boolean mInitialized;
    private int mLastOrientation = 0;
    private String[] mLastPackagesResourceGranted;
    private List<Listener> mListeners = new ArrayList();
    private List<MediaListener> mMediaListeners = new ArrayList();
    private MediaSessionManager mMediaSessionManager;
    private Rect mMenuModePipBounds;
    private int mPinnedStackId = -1;
    private final PinnedStackListenerForwarder.PinnedStackListener mPinnedStackListener = new PipManagerPinnedStackListener();
    private Rect mPipBounds;
    private PipBoundsHandler mPipBoundsHandler;
    private ComponentName mPipComponentName;
    private MediaController mPipMediaController;
    private PipNotification mPipNotification;
    private int mPipTaskId = -1;
    private PipTaskOrganizer mPipTaskOrganizer;
    private int mResizeAnimationDuration;
    private final Runnable mResizePinnedStackRunnable = new Runnable() {
        /* class com.android.systemui.pip.tv.PipManager.AnonymousClass1 */

        public void run() {
            PipManager pipManager = PipManager.this;
            pipManager.resizePinnedStack(pipManager.mResumeResizePinnedStackRunnableState);
        }
    };
    private int mResumeResizePinnedStackRunnableState = 0;
    private Rect mSettingsPipBounds;
    private int mState = 0;
    private int mSuspendPipResizingReason;
    private TaskStackChangeListener mTaskStackListener = new TaskStackChangeListener() {
        /* class com.android.systemui.pip.tv.PipManager.AnonymousClass5 */

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onTaskStackChanged() {
            int[] iArr;
            if (PipManager.DEBUG) {
                Log.d("PipManager", "onTaskStackChanged()");
            }
            if (PipManager.this.getState() != 0) {
                ActivityManager.StackInfo pinnedStackInfo = PipManager.this.getPinnedStackInfo();
                boolean z = false;
                if (pinnedStackInfo == null || (iArr = pinnedStackInfo.taskIds) == null) {
                    Log.w("PipManager", "There is nothing in pinned stack");
                    PipManager.this.closePipInternal(false);
                    return;
                }
                int length = iArr.length - 1;
                while (true) {
                    if (length < 0) {
                        break;
                    } else if (pinnedStackInfo.taskIds[length] == PipManager.this.mPipTaskId) {
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
                Rect rect = PipManager.this.isSettingsShown() ? PipManager.this.mSettingsPipBounds : PipManager.this.mDefaultPipBounds;
                if (PipManager.this.mPipBounds != rect) {
                    PipManager.this.mPipBounds = rect;
                    PipManager.this.resizePinnedStack(1);
                }
            }
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityPinned(String str, int i, int i2, int i3) {
            if (PipManager.DEBUG) {
                Log.d("PipManager", "onActivityPinned()");
            }
            ActivityManager.StackInfo pinnedStackInfo = PipManager.this.getPinnedStackInfo();
            if (pinnedStackInfo == null) {
                Log.w("PipManager", "Cannot find pinned stack");
                return;
            }
            if (PipManager.DEBUG) {
                Log.d("PipManager", "PINNED_STACK:" + pinnedStackInfo);
            }
            PipManager.this.mPinnedStackId = pinnedStackInfo.stackId;
            PipManager pipManager = PipManager.this;
            int[] iArr = pinnedStackInfo.taskIds;
            pipManager.mPipTaskId = iArr[iArr.length - 1];
            PipManager pipManager2 = PipManager.this;
            String[] strArr = pinnedStackInfo.taskNames;
            pipManager2.mPipComponentName = ComponentName.unflattenFromString(strArr[strArr.length - 1]);
            PipManager.this.mState = 1;
            PipManager pipManager3 = PipManager.this;
            pipManager3.mCurrentPipBounds = pipManager3.mPipBounds;
            PipManager.this.mMediaSessionManager.addOnActiveSessionsChangedListener(PipManager.this.mActiveMediaSessionListener, null);
            PipManager pipManager4 = PipManager.this;
            pipManager4.updateMediaController(pipManager4.mMediaSessionManager.getActiveSessions(null));
            for (int size = PipManager.this.mListeners.size() - 1; size >= 0; size--) {
                ((Listener) PipManager.this.mListeners.get(size)).onPipEntered(str);
            }
            PipManager.this.updatePipVisibility(true);
        }

        @Override // com.android.systemui.shared.system.TaskStackChangeListener
        public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo runningTaskInfo, boolean z, boolean z2, boolean z3) {
            if (runningTaskInfo.configuration.windowConfiguration.getWindowingMode() == 2) {
                if (PipManager.DEBUG) {
                    Log.d("PipManager", "onPinnedActivityRestartAttempt()");
                }
                PipManager.this.movePipToFullscreen();
            }
        }
    };
    private final DisplayInfo mTmpDisplayInfo = new DisplayInfo();
    private final Rect mTmpInsetBounds = new Rect();
    private final Rect mTmpNormalBounds = new Rect();

    public interface Listener {
        void onMoveToFullscreen();

        void onPipActivityClosed();

        void onPipEntered(String str);

        void onPipMenuActionsChanged(ParceledListSlice parceledListSlice);

        void onPipResizeAboutToStart();

        void onShowPipMenu();
    }

    public interface MediaListener {
        void onMediaControllerChanged();
    }

    @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
    public void onPipTransitionStarted(ComponentName componentName, int i) {
    }

    public class PipManagerPinnedStackListener extends PinnedStackListenerForwarder.PinnedStackListener {
        private PipManagerPinnedStackListener() {
            PipManager.this = r1;
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onImeVisibilityChanged(boolean z, int i) {
            if (PipManager.this.mState == 1 && PipManager.this.mImeVisible != z) {
                if (z) {
                    PipManager.this.mPipBounds.offset(0, -i);
                    PipManager.this.mImeHeightAdjustment = i;
                } else {
                    PipManager.this.mPipBounds.offset(0, PipManager.this.mImeHeightAdjustment);
                }
                PipManager.this.mImeVisible = z;
                PipManager.this.resizePinnedStack(1);
            }
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onMovementBoundsChanged(boolean z) {
            PipManager.this.mHandler.post(new Runnable() {
                /* class com.android.systemui.pip.tv.$$Lambda$PipManager$PipManagerPinnedStackListener$xzENomr6fPtav6biMAAyvIPEf_c */

                public final void run() {
                    PipManager.PipManagerPinnedStackListener.this.lambda$onMovementBoundsChanged$0$PipManager$PipManagerPinnedStackListener();
                }
            });
        }

        /* access modifiers changed from: public */
        /* access modifiers changed from: private */
        /* renamed from: lambda$onMovementBoundsChanged$0 */
        public /* synthetic */ void lambda$onMovementBoundsChanged$0$PipManager$PipManagerPinnedStackListener() {
            Rect rect = new Rect();
            PipManager.this.mPipBoundsHandler.onMovementBoundsChanged(PipManager.this.mTmpInsetBounds, PipManager.this.mTmpNormalBounds, rect, PipManager.this.mTmpDisplayInfo);
            PipManager.this.mDefaultPipBounds.set(rect);
        }

        @Override // com.android.systemui.shared.system.PinnedStackListenerForwarder.PinnedStackListener
        public void onActionsChanged(ParceledListSlice parceledListSlice) {
            PipManager.this.mCustomActions = parceledListSlice;
            PipManager.this.mHandler.post(new Runnable() {
                /* class com.android.systemui.pip.tv.$$Lambda$PipManager$PipManagerPinnedStackListener$pvNxqxzg0ewPBlnvdhxIPv7OOlM */

                public final void run() {
                    PipManager.PipManagerPinnedStackListener.this.lambda$onActionsChanged$1$PipManager$PipManagerPinnedStackListener();
                }
            });
        }

        /* access modifiers changed from: public */
        /* access modifiers changed from: private */
        /* renamed from: lambda$onActionsChanged$1 */
        public /* synthetic */ void lambda$onActionsChanged$1$PipManager$PipManagerPinnedStackListener() {
            for (int size = PipManager.this.mListeners.size() - 1; size >= 0; size--) {
                ((Listener) PipManager.this.mListeners.get(size)).onPipMenuActionsChanged(PipManager.this.mCustomActions);
            }
        }
    }

    public PipManager(Context context, BroadcastDispatcher broadcastDispatcher, PipBoundsHandler pipBoundsHandler, PipTaskOrganizer pipTaskOrganizer, PipSurfaceTransactionHelper pipSurfaceTransactionHelper, Divider divider) {
        Pair<String, String> pair;
        if (!this.mInitialized) {
            this.mInitialized = true;
            this.mContext = context;
            this.mPipBoundsHandler = pipBoundsHandler;
            DisplayInfo displayInfo = new DisplayInfo();
            context.getDisplay().getDisplayInfo(displayInfo);
            this.mPipBoundsHandler.onDisplayInfoChanged(displayInfo);
            this.mResizeAnimationDuration = context.getResources().getInteger(C0016R$integer.config_pipResizeAnimationDuration);
            this.mPipTaskOrganizer = pipTaskOrganizer;
            pipTaskOrganizer.registerPipTransitionCallback(this);
            this.mActivityTaskManager = ActivityTaskManager.getService();
            ActivityManagerWrapper.getInstance().registerTaskStackListener(this.mTaskStackListener);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.MEDIA_RESOURCE_GRANTED");
            broadcastDispatcher.registerReceiver(this.mBroadcastReceiver, intentFilter, null, UserHandle.ALL);
            if (sSettingsPackageAndClassNamePairList == null) {
                String[] stringArray = this.mContext.getResources().getStringArray(C0008R$array.tv_pip_settings_class_name);
                sSettingsPackageAndClassNamePairList = new ArrayList();
                if (stringArray != null) {
                    for (int i = 0; i < stringArray.length; i++) {
                        String[] split = stringArray[i].split("/");
                        int length = split.length;
                        if (length == 1) {
                            pair = Pair.create(split[0], null);
                        } else if (length == 2 && split[1] != null) {
                            pair = Pair.create(split[0], split[1].startsWith(".") ? split[0] + split[1] : split[1]);
                        } else {
                            pair = null;
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
                WindowManagerWrapper.getInstance().addPinnedStackListener(this.mPinnedStackListener);
                this.mPipTaskOrganizer.registerOrganizer(2);
            } catch (RemoteException | UnsupportedOperationException e) {
                Log.e("PipManager", "Failed to register pinned stack listener", e);
            }
            this.mPipNotification = new PipNotification(context, broadcastDispatcher, this);
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
        this.mSettingsPipBounds = Rect.unflattenFromString(resources.getString(C0021R$string.pip_settings_bounds));
        this.mMenuModePipBounds = Rect.unflattenFromString(resources.getString(C0021R$string.pip_menu_bounds));
        this.mPipBounds = isSettingsShown() ? this.mSettingsPipBounds : this.mDefaultPipBounds;
        resizePinnedStack(getPinnedStackInfo() == null ? 0 : 1);
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void onConfigurationChanged(Configuration configuration) {
        loadConfigurationsAndApply(configuration);
        this.mPipNotification.onConfigurationChanged(this.mContext);
    }

    @Override // com.android.systemui.pip.BasePipManager
    public void showPictureInPictureMenu() {
        if (DEBUG) {
            Log.d("PipManager", "showPictureInPictureMenu(), current state=" + getStateDescription());
        }
        if (getState() == 1) {
            resizePinnedStack(2);
        }
    }

    public void closePip() {
        if (DEBUG) {
            Log.d("PipManager", "closePip(), current state=" + getStateDescription());
        }
        closePipInternal(true);
    }

    private void closePipInternal(boolean z) {
        if (DEBUG) {
            Log.d("PipManager", "closePipInternal() removePipStack=" + z + ", current state=" + getStateDescription());
        }
        this.mState = 0;
        this.mPipTaskId = -1;
        this.mPipMediaController = null;
        this.mMediaSessionManager.removeOnActiveSessionsChangedListener(this.mActiveMediaSessionListener);
        if (z) {
            try {
                this.mActivityTaskManager.removeStack(this.mPinnedStackId);
            } catch (RemoteException e) {
                Log.e("PipManager", "removeStack failed", e);
            } catch (Throwable th) {
                this.mPinnedStackId = -1;
                throw th;
            }
            this.mPinnedStackId = -1;
        }
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onPipActivityClosed();
        }
        this.mHandler.removeCallbacks(this.mClosePipRunnable);
        updatePipVisibility(false);
    }

    public void movePipToFullscreen() {
        if (DEBUG) {
            Log.d("PipManager", "movePipToFullscreen(), current state=" + getStateDescription());
        }
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

    public void resizePinnedStack(int i) {
        if (DEBUG) {
            Log.d("PipManager", "resizePinnedStack() state=" + stateToName(i) + ", current state=" + getStateDescription(), new Exception());
        }
        boolean z = this.mState == 0;
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onPipResizeAboutToStart();
        }
        if (this.mSuspendPipResizingReason != 0) {
            this.mResumeResizePinnedStackRunnableState = i;
            if (DEBUG) {
                Log.d("PipManager", "resizePinnedStack() deferring mSuspendPipResizingReason=" + this.mSuspendPipResizingReason + " mResumeResizePinnedStackRunnableState=" + stateToName(this.mResumeResizePinnedStackRunnableState));
                return;
            }
            return;
        }
        this.mState = i;
        if (i == 0) {
            this.mCurrentPipBounds = null;
            if (z) {
                return;
            }
        } else if (i != 2) {
            this.mCurrentPipBounds = this.mPipBounds;
        } else {
            this.mCurrentPipBounds = this.mMenuModePipBounds;
        }
        Rect rect = this.mCurrentPipBounds;
        if (rect != null) {
            this.mPipTaskOrganizer.scheduleAnimateResizePip(rect, this.mResizeAnimationDuration, null);
        } else {
            this.mPipTaskOrganizer.exitPip(this.mResizeAnimationDuration);
        }
    }

    private int getState() {
        if (this.mSuspendPipResizingReason != 0) {
            return this.mResumeResizePinnedStackRunnableState;
        }
        return this.mState;
    }

    private void showPipMenu() {
        if (DEBUG) {
            Log.d("PipManager", "showPipMenu(), current state=" + getStateDescription());
        }
        this.mState = 2;
        for (int size = this.mListeners.size() - 1; size >= 0; size--) {
            this.mListeners.get(size).onShowPipMenu();
        }
        Intent intent = new Intent(this.mContext, PipMenuActivity.class);
        intent.setFlags(268435456);
        intent.putExtra("custom_actions", (Parcelable) this.mCustomActions);
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

    public boolean isPipShown() {
        return this.mState != 0;
    }

    private ActivityManager.StackInfo getPinnedStackInfo() {
        try {
            return ActivityTaskManager.getService().getStackInfo(2, 0);
        } catch (RemoteException e) {
            Log.e("PipManager", "getStackInfo failed", e);
            return null;
        }
    }

    private void handleMediaResourceGranted(String[] strArr) {
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

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0033  */
    /* JADX WARNING: Removed duplicated region for block: B:24:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void updateMediaController(java.util.List<android.media.session.MediaController> r5) {
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

    public MediaController getMediaController() {
        return this.mPipMediaController;
    }

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

    private boolean isSettingsShown() {
        String str;
        try {
            List tasks = this.mActivityTaskManager.getTasks(1);
            if (tasks.isEmpty()) {
                return false;
            }
            ComponentName componentName = ((ActivityManager.RunningTaskInfo) tasks.get(0)).topActivity;
            for (Pair<String, String> pair : sSettingsPackageAndClassNamePairList) {
                if (componentName.getPackageName().equals((String) pair.first) && ((str = (String) pair.second) == null || componentName.getClassName().equals(str))) {
                    return true;
                }
            }
            return false;
        } catch (RemoteException e) {
            Log.d("PipManager", "Failed to detect top activity", e);
            return false;
        }
    }

    @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
    public void onPipTransitionFinished(ComponentName componentName, int i) {
        onPipTransitionFinishedOrCanceled();
    }

    @Override // com.android.systemui.pip.PipTaskOrganizer.PipTransitionCallback
    public void onPipTransitionCanceled(ComponentName componentName, int i) {
        onPipTransitionFinishedOrCanceled();
    }

    private void onPipTransitionFinishedOrCanceled() {
        if (DEBUG) {
            Log.d("PipManager", "onPipTransitionFinishedOrCanceled()");
        }
        if (getState() == 2) {
            showPipMenu();
        }
    }

    private void updatePipVisibility(boolean z) {
        ((UiOffloadThread) Dependency.get(UiOffloadThread.class)).execute(new Runnable(z) {
            /* class com.android.systemui.pip.tv.$$Lambda$PipManager$B3cwmVrFFG3e6pUajgQn8FpuCeM */
            public final /* synthetic */ boolean f$0;

            {
                this.f$0 = r1;
            }

            public final void run() {
                PipManager.lambda$updatePipVisibility$0(this.f$0);
            }
        });
    }

    private String getStateDescription() {
        if (this.mSuspendPipResizingReason == 0) {
            return stateToName(this.mState);
        }
        return stateToName(this.mResumeResizePinnedStackRunnableState) + " (while " + stateToName(this.mState) + " is suspended)";
    }

    private static String stateToName(int i) {
        if (i == 0) {
            return "NO_PIP";
        }
        if (i == 1) {
            return "PIP";
        }
        if (i == 2) {
            return "PIP_MENU";
        }
        return "UNKNOWN(" + i + ")";
    }
}

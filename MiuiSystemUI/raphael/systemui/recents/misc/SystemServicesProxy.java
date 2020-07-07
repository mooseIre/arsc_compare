package com.android.systemui.recents.misc;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.AppGlobals;
import android.app.IActivityManager;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserHandleCompat;
import android.os.UserManager;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.service.dreams.IDreamManager;
import android.speech.tts.TtsEngines;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.view.Display;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IDockedStackListener;
import android.view.IWindowManager;
import android.view.IWindowManagerCompat;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.app.AssistUtils;
import com.android.internal.os.BackgroundThread;
import com.android.systemui.SystemUICompat;
import com.android.systemui.fsgesture.IFsGestureCallback;
import com.android.systemui.plugins.R;
import com.android.systemui.proxy.ActivityManager$TaskThumbnailInfo;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsActivity;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.recents.events.activity.TaskSnapshotChangedEvent;
import com.android.systemui.recents.model.MutableBoolean;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.RecentsTaskLoader;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.ThumbnailData;
import com.miui.browser.webapps.WebAppDAO;
import com.miui.browser.webapps.WebAppInfo;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import miui.maml.util.AppIconsHelper;
import miui.security.SecurityManager;
import miui.securityspace.XSpaceUserHandle;
import org.json.JSONObject;

public class SystemServicesProxy {
    public static boolean DEBUG = false;
    static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();
    private static final List<String> sMultiWindowForceNotResizePkgList = new ArrayList();
    private static final List<String> sMultiWindowForceResizePkgList = new ArrayList();
    static final List<String> sRecentsBlacklist = new ArrayList();
    private static SystemServicesProxy sSystemServicesProxy;
    private int mAccessControlLockMode;
    private SoftReference<Bitmap> mAccessLockedFakeScreenshotLand;
    private SoftReference<Bitmap> mAccessLockedFakeScreenshotPort;
    AccessibilityManager mAccm;
    ActivityManager mAm;
    ComponentName mAssistComponent;
    AssistUtils mAssistUtils;
    Canvas mBgProtectionCanvas;
    Paint mBgProtectionPaint;
    Context mContext;
    Display mDisplay;
    /* access modifiers changed from: private */
    public final IDreamManager mDreamManager;
    int mDummyThumbnailHeight;
    int mDummyThumbnailWidth;
    /* access modifiers changed from: private */
    public final Handler mHandler = new H();
    boolean mHasFreeformWorkspaceSupport;
    public final HashMap<String, IFsGestureCallback> mIFsGestureCallbackMap = new HashMap<>();
    IActivityManager mIam;
    IPackageManager mIpm;
    private boolean mIsFsGestureAnimating = false;
    boolean mIsSafeMode;
    IWindowManager mIwm;
    PackageManager mPm;
    String mRecentsPackage;
    int mStatusBarHeight;
    private TaskStackListener mTaskStackListener = new TaskStackListener() {
        private final List<TaskStackListener> mTmpListeners = new ArrayList();

        public void onActivityPinned(String str, int i, int i2, int i3) throws RemoteException {
        }

        public void onTaskStackChanged() throws RemoteException {
            synchronized (SystemServicesProxy.this.mTaskStackListeners) {
                this.mTmpListeners.clear();
                this.mTmpListeners.addAll(SystemServicesProxy.this.mTaskStackListeners);
            }
            for (int size = this.mTmpListeners.size() - 1; size >= 0; size--) {
                this.mTmpListeners.get(size).onTaskStackChangedBackground();
            }
            SystemServicesProxy.this.mHandler.removeMessages(1);
            SystemServicesProxy.this.mHandler.sendEmptyMessage(1);
        }

        public void onActivityUnpinned() throws RemoteException {
            SystemServicesProxy.this.mHandler.removeMessages(10);
            SystemServicesProxy.this.mHandler.sendEmptyMessage(10);
        }

        public void onPinnedActivityRestartAttempt(boolean z) throws RemoteException {
            SystemServicesProxy.this.mHandler.removeMessages(4);
            SystemServicesProxy.this.mHandler.obtainMessage(4, z ? 1 : 0, 0).sendToTarget();
        }

        public void onPinnedStackAnimationStarted() throws RemoteException {
            SystemServicesProxy.this.mHandler.removeMessages(9);
            SystemServicesProxy.this.mHandler.sendEmptyMessage(9);
        }

        public void onPinnedStackAnimationEnded() throws RemoteException {
            SystemServicesProxy.this.mHandler.removeMessages(5);
            SystemServicesProxy.this.mHandler.sendEmptyMessage(5);
        }

        public void onActivityForcedResizable(String str, int i, int i2) throws RemoteException {
            SystemServicesProxy.this.mHandler.obtainMessage(6, i, i2, str).sendToTarget();
        }

        public void onActivityDismissingDockedStack() throws RemoteException {
            SystemServicesProxy.this.mHandler.sendEmptyMessage(7);
        }

        public void onTaskProfileLocked(int i, int i2) {
            SystemServicesProxy.this.mHandler.obtainMessage(8, i, i2).sendToTarget();
        }

        public void onTaskSnapshotChanged(int i, ActivityManager.TaskSnapshot taskSnapshot) throws RemoteException {
            SystemServicesProxy.this.mHandler.removeMessages(2);
            SystemServicesProxy.this.mHandler.obtainMessage(2, i, 0, taskSnapshot).sendToTarget();
        }
    };
    /* access modifiers changed from: private */
    public List<TaskStackListener> mTaskStackListeners = new ArrayList();
    TtsEngines mTtsEngines;
    UserManager mUm;
    WebAppDAO mWebAppDAO;
    public WindowManager mWm;
    WallpaperManager mWpm;

    public static boolean isFreeformStack(int i) {
        return i == 2;
    }

    static {
        BitmapFactory.Options options = sBitmapOptions;
        options.inMutable = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        sRecentsBlacklist.add("com.android.systemui.tv.pip.PipOnboardingActivity");
        sRecentsBlacklist.add("com.android.systemui.tv.pip.PipMenuActivity");
        sRecentsBlacklist.add("com.android.systemui.recents.RecentsActivity");
    }

    public static abstract class TaskStackListener {
        public void onActivityDismissingDockedStack() {
        }

        public void onActivityForcedResizable(String str, int i, int i2) {
        }

        public void onActivityLaunchOnSecondaryDisplayFailed() {
        }

        public void onActivityPinned(String str, int i, int i2) {
        }

        public void onActivityUnpinned() {
        }

        public void onPinnedActivityRestartAttempt(boolean z) {
        }

        public void onPinnedStackAnimationEnded() {
        }

        public void onPinnedStackAnimationStarted() {
        }

        public void onTaskProfileLocked(int i, int i2) {
        }

        public void onTaskSnapshotChanged(int i, ActivityManager.TaskSnapshot taskSnapshot) {
        }

        public void onTaskStackChanged() {
        }

        public void onTaskStackChangedBackground() {
        }

        /* access modifiers changed from: protected */
        public final boolean checkCurrentUserId(Context context, boolean z) {
            int myUserId = UserHandle.myUserId();
            int currentUser = SystemServicesProxy.getInstance(context).getCurrentUser();
            if (myUserId == currentUser) {
                return true;
            }
            if (!z) {
                return false;
            }
            Log.d("SystemServicesProxy", "UID mismatch. SystemUI is running uid=" + myUserId + " and the current user is uid=" + currentUser);
            return false;
        }
    }

    private SystemServicesProxy(Context context) {
        this.mAccm = AccessibilityManager.getInstance(context);
        this.mAm = (ActivityManager) context.getSystemService("activity");
        this.mIam = ActivityManagerNative.getDefault();
        this.mPm = context.getPackageManager();
        this.mIpm = AppGlobals.getPackageManager();
        this.mAssistUtils = new AssistUtils(context);
        this.mWm = (WindowManager) context.getSystemService("window");
        this.mIwm = WindowManagerGlobal.getWindowManagerService();
        this.mUm = UserManager.get(context);
        this.mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.checkService("dreams"));
        this.mDisplay = this.mWm.getDefaultDisplay();
        this.mWpm = WallpaperManager.getInstance(context);
        this.mRecentsPackage = context.getPackageName();
        this.mHasFreeformWorkspaceSupport = false;
        this.mIsSafeMode = this.mPm.isSafeMode();
        Resources resources = context.getResources();
        this.mDummyThumbnailWidth = resources.getDimensionPixelSize(17104898);
        this.mDummyThumbnailHeight = resources.getDimensionPixelSize(17104897);
        this.mStatusBarHeight = resources.getDimensionPixelSize(17105467);
        this.mBgProtectionPaint = new Paint();
        this.mBgProtectionPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
        this.mBgProtectionPaint.setColor(-1);
        this.mBgProtectionCanvas = new Canvas();
        this.mAssistComponent = this.mAssistUtils.getAssistComponentForUser(UserHandle.myUserId());
        this.mWebAppDAO = WebAppDAO.getInstance(context);
        this.mTtsEngines = new TtsEngines(this.mContext);
        this.mContext = context.getApplicationContext();
        this.mAccessControlLockMode = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "access_control_lock_mode", 1, -2);
    }

    public static SystemServicesProxy getInstance(Context context) {
        if (Looper.getMainLooper().isCurrentThread()) {
            if (sSystemServicesProxy == null) {
                sSystemServicesProxy = new SystemServicesProxy(context);
            }
            return sSystemServicesProxy;
        }
        throw new RuntimeException("Must be called on the UI thread");
    }

    public List<ActivityManager.RecentTaskInfo> getRecentTasks(int i, int i2, boolean z, ArraySet<Integer> arraySet) {
        List list = null;
        if (this.mAm == null) {
            return null;
        }
        int max = Math.max(10, i);
        int i3 = 62;
        if (z) {
            i3 = 63;
        }
        try {
            list = ActivityManager.getService().getRecentTasks(max, i3, i2).getList();
        } catch (Exception e) {
            Log.e("SystemServicesProxy", "Failed to get recent tasks", e);
        }
        if (list == null) {
            return new ArrayList();
        }
        Iterator it = list.iterator();
        boolean z2 = true;
        while (it.hasNext()) {
            ActivityManager.RecentTaskInfo recentTaskInfo = (ActivityManager.RecentTaskInfo) it.next();
            if (recentTaskInfo == null || recentTaskInfo.topActivity == null || (!sRecentsBlacklist.contains(recentTaskInfo.topActivity.getClassName()) && !sRecentsBlacklist.contains(recentTaskInfo.topActivity.getPackageName()))) {
                if ((arraySet.contains(Integer.valueOf(recentTaskInfo.userId)) || ((recentTaskInfo.baseIntent.getFlags() & 8388608) == 8388608)) && (!z2 || !z)) {
                    it.remove();
                }
                z2 = false;
            } else {
                it.remove();
            }
        }
        return list.subList(0, Math.min(list.size(), i));
    }

    public ActivityManager.RunningTaskInfo getRunningTask() {
        try {
            List filteredTasks = ActivityManager.getService().getFilteredTasks(1, 3, 2);
            if (filteredTasks != null) {
                if (!filteredTasks.isEmpty()) {
                    return (ActivityManager.RunningTaskInfo) filteredTasks.get(0);
                }
            }
        } catch (RemoteException unused) {
        }
        return null;
    }

    public boolean isRecentsActivityVisible() {
        return isRecentsActivityVisible((MutableBoolean) null);
    }

    public boolean isRecentsActivityVisible(MutableBoolean mutableBoolean) {
        return SystemUICompat.isRecentsActivityVisible(mutableBoolean, this.mIam, this.mPm);
    }

    public boolean hasFreeformWorkspaceSupport() {
        return this.mHasFreeformWorkspaceSupport;
    }

    public boolean isInSafeMode() {
        return this.mIsSafeMode;
    }

    public boolean startTaskInDockedMode(Task task, int i, Context context) {
        return SystemUICompat.startTaskInDockedMode(task, i, this.mIam, context);
    }

    public boolean startTaskInDockedMode(int i, int i2) {
        if (this.mIam == null) {
            return false;
        }
        try {
            ActivityOptions makeBasic = ActivityOptions.makeBasic();
            makeBasic.setLaunchWindowingMode(3);
            makeBasic.setSplitScreenCreateMode(i2 == 0 ? 0 : 1);
            this.mIam.startActivityFromRecents(i, makeBasic.toBundle());
            return true;
        } catch (Exception e) {
            Log.e("SystemServicesProxy", "Failed to dock taskId: " + i + " with createMode: " + i2, e);
            return false;
        }
    }

    public boolean moveTaskToDockedStack(int i, int i2, Rect rect) {
        IActivityManager iActivityManager = this.mIam;
        if (iActivityManager == null) {
            return false;
        }
        try {
            return ActivityManagerCompat.moveTaskToDockedStack(iActivityManager, i, i2, true, false, rect, true);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isHomeOrRecentsStack(int i, ActivityManager.RunningTaskInfo runningTaskInfo) {
        return SystemUICompat.isHomeOrRecentsStack(i, runningTaskInfo);
    }

    public boolean hasDockedTask() {
        return SystemUICompat.hasDockedTask(this.mIam);
    }

    public int getWindowModeFromRecentTaskInfo(ActivityManager.RecentTaskInfo recentTaskInfo) {
        return recentTaskInfo.configuration.windowConfiguration.getWindowingMode();
    }

    public boolean hasSoftNavigationBar(int i) {
        try {
            return IWindowManagerCompat.hasNavigationBar(this.mIwm, i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void cancelWindowTransition(int i) {
        IActivityManager iActivityManager = this.mIam;
        if (iActivityManager != null) {
            try {
                SystemUICompat.cancelTaskWindowTransition(iActivityManager, i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void cancelThumbnailTransition(int i) {
        IActivityManager iActivityManager = this.mIam;
        if (iActivityManager != null) {
            try {
                SystemUICompat.cancelTaskThumbnailTransition(iActivityManager, i);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public ThumbnailData getTaskThumbnail(Task.TaskKey taskKey) {
        if (this.mAm == null) {
            return null;
        }
        ThumbnailData thumbnailData = new ThumbnailData();
        getThumbnail(taskKey, thumbnailData);
        Bitmap bitmap = thumbnailData.thumbnail;
        if (bitmap != null) {
            bitmap.setHasAlpha(false);
        }
        return thumbnailData;
    }

    public void getThumbnail(Task.TaskKey taskKey, ThumbnailData thumbnailData) {
        ActivityManager.TaskSnapshot taskSnapshot;
        if (this.mAm != null) {
            Bitmap bitmap = null;
            try {
                taskSnapshot = ActivityManager.getService().getTaskSnapshot(taskKey.id, false);
            } catch (Exception e) {
                Log.w("SystemServicesProxy", "Failed to retrieve task snapshot", e);
                taskSnapshot = null;
            }
            if (taskSnapshot != null) {
                bitmap = SystemUICompat.createHardwareBitmapFromSnapShot(taskSnapshot);
            }
            thumbnailData.thumbnail = bitmap;
            thumbnailData.thumbnailInfo = getTaskThumbnailInfo(taskSnapshot);
            thumbnailData.isAccessLocked = false;
        }
    }

    private ActivityManager$TaskThumbnailInfo getTaskThumbnailInfo(ActivityManager.TaskSnapshot taskSnapshot) {
        if (taskSnapshot == null || taskSnapshot.getSnapshot() == null) {
            return null;
        }
        ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo = new ActivityManager$TaskThumbnailInfo();
        activityManager$TaskThumbnailInfo.taskWidth = taskSnapshot.getSnapshot().getWidth();
        activityManager$TaskThumbnailInfo.taskHeight = taskSnapshot.getSnapshot().getHeight();
        activityManager$TaskThumbnailInfo.screenOrientation = taskSnapshot.getOrientation();
        activityManager$TaskThumbnailInfo.insets = taskSnapshot.getContentInsets();
        activityManager$TaskThumbnailInfo.scale = taskSnapshot.getScale();
        return activityManager$TaskThumbnailInfo;
    }

    public void moveTaskToStack(int i, int i2) {
        IActivityManager iActivityManager = this.mIam;
        if (iActivityManager != null) {
            try {
                iActivityManager.positionTaskInStack(i, i2, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void removeTask(final int i, boolean z) {
        if (this.mAm != null) {
            BackgroundThread.getHandler().post(new Runnable() {
                public void run() {
                    try {
                        ActivityManager.getService().removeTask(i);
                    } catch (RemoteException e) {
                        Log.w("SystemServicesProxy", "Failed to remove task=" + i, e);
                    }
                }
            });
        }
    }

    public void killProcess(final Task task) {
        BackgroundThread.getHandler().post(new Runnable() {
            public void run() {
                String packageName = task.key.getComponent().getPackageName();
                Task.TaskKey taskKey = task.key;
                ProcessManagerHelper.performSwipeUpClean(packageName, taskKey.userId, taskKey.id);
            }
        });
    }

    public void sendCloseSystemWindows(String str) {
        try {
            this.mIam.closeSystemDialogs(str);
        } catch (RemoteException unused) {
        }
    }

    public ActivityInfo getActivityInfo(ComponentName componentName, int i) {
        IPackageManager iPackageManager = this.mIpm;
        if (iPackageManager == null) {
            return null;
        }
        try {
            return iPackageManager.getActivityInfo(componentName, 128, i);
        } catch (RemoteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ActivityInfo getActivityInfo(ComponentName componentName) {
        PackageManager packageManager = this.mPm;
        if (packageManager == null) {
            return null;
        }
        try {
            return packageManager.getActivityInfo(componentName, 128);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getBadgedActivityLabel(ActivityInfo activityInfo, int i) {
        String str = null;
        if (this.mPm == null) {
            return null;
        }
        WebAppInfo webAppInfo = this.mWebAppDAO.get(activityInfo);
        if (webAppInfo != null) {
            str = webAppInfo.mLabel;
        }
        if (str == null) {
            str = activityInfo.loadLabel(this.mPm).toString();
        }
        return getBadgedLabel(str, i);
    }

    public String getBadgedContentDescription(ActivityInfo activityInfo, int i, Resources resources) {
        String charSequence = activityInfo.loadLabel(this.mPm).toString();
        String charSequence2 = activityInfo.applicationInfo.loadLabel(this.mPm).toString();
        String badgedLabel = getBadgedLabel(charSequence2, i);
        if (charSequence2.equals(charSequence)) {
            return badgedLabel;
        }
        return resources.getString(R.string.accessibility_recents_task_header, new Object[]{badgedLabel, charSequence});
    }

    public Drawable getBadgedActivityIcon(ActivityInfo activityInfo, int i) {
        Drawable drawable = null;
        if (this.mPm == null) {
            return null;
        }
        WebAppInfo webAppInfo = this.mWebAppDAO.get(activityInfo);
        if (webAppInfo != null) {
            drawable = webAppInfo.getIcon(this.mContext);
        }
        if (drawable == null) {
            drawable = AppIconsHelper.getIconDrawable(this.mContext, activityInfo, this.mPm, 43200000);
        }
        if (drawable == null) {
            drawable = activityInfo.loadIcon(this.mPm);
        }
        if (XSpaceUserHandle.isXSpaceUserId(i)) {
            drawable = XSpaceUserHandle.getXSpaceIcon(this.mContext, drawable);
        }
        return getBadgedIcon(drawable, i);
    }

    public Drawable getBadgedTaskDescriptionIcon(ActivityManager.TaskDescription taskDescription, int i, Resources resources) {
        Bitmap inMemoryIcon = taskDescription.getInMemoryIcon();
        if (inMemoryIcon == null) {
            inMemoryIcon = ActivityManagerCompat.loadTaskDescriptionIcon(taskDescription.getIconFilename(), i);
        }
        if (inMemoryIcon != null) {
            return getBadgedIcon(new BitmapDrawable(resources, inMemoryIcon), i);
        }
        return null;
    }

    private Drawable getBadgedIcon(Drawable drawable, int i) {
        return i != UserHandle.myUserId() ? this.mPm.getUserBadgedIcon(drawable, new UserHandle(i)) : drawable;
    }

    private String getBadgedLabel(String str, int i) {
        return i != UserHandle.myUserId() ? this.mPm.getUserBadgedLabel(str, new UserHandle(i)).toString() : str;
    }

    public boolean isSystemUser(int i) {
        return i == UserHandleCompat.SYSTEM.getIdentifier();
    }

    public int getCurrentUser() {
        if (this.mAm == null) {
            return 0;
        }
        return ActivityManager.getCurrentUser();
    }

    public int getProcessUser() {
        UserManager userManager = this.mUm;
        if (userManager == null) {
            return 0;
        }
        return userManager.getUserHandle();
    }

    public boolean isTouchExplorationEnabled() {
        AccessibilityManager accessibilityManager = this.mAccm;
        if (accessibilityManager != null && accessibilityManager.isEnabled() && this.mAccm.isTouchExplorationEnabled()) {
            return true;
        }
        return false;
    }

    public boolean isScreenPinningActive() {
        IActivityManager iActivityManager = this.mIam;
        if (iActivityManager == null) {
            return false;
        }
        try {
            return iActivityManager.isInLockTaskMode();
        } catch (RemoteException unused) {
            return false;
        }
    }

    public int getSystemSetting(Context context, String str) {
        return Settings.System.getInt(context.getContentResolver(), str, 0);
    }

    public int getDeviceSmallestWidth() {
        if (this.mDisplay == null) {
            return 0;
        }
        Point point = new Point();
        this.mDisplay.getCurrentSizeRange(point, new Point());
        return point.x;
    }

    public Rect getDisplayRect() {
        Rect rect = new Rect();
        if (this.mDisplay == null) {
            return rect;
        }
        Point point = new Point();
        this.mDisplay.getRealSize(point);
        rect.set(0, 0, point.x, point.y);
        return rect;
    }

    public Rect getWindowRect() {
        return SystemUICompat.getRecentsWindowRect(this.mIam);
    }

    public int getDisplayRotation() {
        Display display = this.mDisplay;
        if (display == null) {
            return 0;
        }
        return display.getRotation();
    }

    public boolean startActivityFromRecents(Context context, Task.TaskKey taskKey, String str, ActivityOptions activityOptions) {
        Bundle bundle;
        if (this.mIam != null) {
            try {
                if (taskKey.windowingMode == 3) {
                    if (activityOptions == null) {
                        activityOptions = ActivityOptions.makeBasic();
                    }
                    activityOptions.setLaunchWindowingMode(4);
                }
                IActivityManager iActivityManager = this.mIam;
                int i = taskKey.id;
                if (activityOptions == null) {
                    bundle = null;
                } else {
                    bundle = activityOptions.toBundle();
                }
                iActivityManager.startActivityFromRecents(i, bundle);
                return true;
            } catch (Exception e) {
                Log.e("SystemServicesProxy", context.getString(R.string.recents_launch_error_message, new Object[]{str}), e);
            }
        }
        return false;
    }

    public void startInPlaceAnimationOnFrontMostApplication(ActivityOptions activityOptions) {
        IActivityManager iActivityManager = this.mIam;
        if (iActivityManager != null) {
            try {
                ActivityManagerCompat.startInPlaceAnimationOnFrontMostApplication(iActivityManager, activityOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerTaskStackListener(TaskStackListener taskStackListener) {
        if (this.mIam != null) {
            this.mTaskStackListeners.add(taskStackListener);
            if (this.mTaskStackListeners.size() == 1) {
                try {
                    this.mIam.registerTaskStackListener(this.mTaskStackListener);
                } catch (Exception e) {
                    Log.w("SystemServicesProxy", "Failed to call registerTaskStackListener", e);
                }
            }
        }
    }

    public void endProlongedAnimations() {
        if (this.mWm != null) {
            try {
                WindowManagerGlobal.getWindowManagerService().endProlongedAnimations();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void registerDockedStackListener(IDockedStackListener iDockedStackListener) {
        if (this.mWm != null) {
            try {
                WindowManagerGlobal.getWindowManagerService().registerDockedStackListener(iDockedStackListener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getDockedDividerSize(Context context) {
        Resources resources = context.getResources();
        return resources.getDimensionPixelSize(R.dimen.docked_stack_divider_thickness) - (resources.getDimensionPixelSize(R.dimen.docked_stack_divider_insets) * 2);
    }

    public void getStableInsets(Rect rect) {
        if (this.mWm != null) {
            try {
                SystemUICompat.getStableInsets(rect);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (MiuiSettings.Global.getBoolean(this.mContext.getContentResolver(), "force_fsg_nav_bar")) {
                rect.left = 0;
                rect.right = 0;
                rect.bottom = 0;
            }
        }
    }

    public void overridePendingAppTransitionMultiThumbFuture(IAppTransitionAnimationSpecsFuture iAppTransitionAnimationSpecsFuture, IRemoteCallback iRemoteCallback, boolean z, int i) {
        try {
            IWindowManagerCompat.overridePendingAppTransitionMultiThumbFuture(this.mIwm, iAppTransitionAnimationSpecsFuture, iRemoteCallback, z, i);
        } catch (RemoteException e) {
            Log.w("SystemServicesProxy", "Failed to override transition: " + e);
        }
    }

    public void setAccessControlLockMode(int i) {
        this.mAccessControlLockMode = i;
    }

    public boolean isAccessLocked(Task.TaskKey taskKey) {
        SecurityManager securityManager = (SecurityManager) this.mContext.getSystemService("security");
        if (!securityManager.isAccessControlActived(this.mContext) || !securityManager.getApplicationAccessControlEnabledAsUser(taskKey.getComponent().getPackageName(), taskKey.userId)) {
            return false;
        }
        if (this.mAccessControlLockMode == 0 || !securityManager.checkAccessControlPassAsUser(taskKey.getComponent().getPackageName(), taskKey.userId)) {
            return true;
        }
        return false;
    }

    public void onLanguageChange() {
        SoftReference<Bitmap> softReference = this.mAccessLockedFakeScreenshotPort;
        if (softReference != null) {
            softReference.clear();
        }
        SoftReference<Bitmap> softReference2 = this.mAccessLockedFakeScreenshotLand;
        if (softReference2 != null) {
            softReference2.clear();
        }
    }

    public void setRecentsVisibility(Context context, boolean z) {
        SystemUICompat.setRecentsVisibility(context, z);
    }

    public void setPipVisibility(boolean z) {
        try {
            this.mIwm.setPipVisibility(z);
        } catch (RemoteException e) {
            Log.e("SystemServicesProxy", "Unable to reach window manager", e);
        }
    }

    public boolean isDreaming() {
        try {
            return this.mDreamManager.isDreaming();
        } catch (RemoteException e) {
            Log.e("SystemServicesProxy", "Failed to query dream manager.", e);
            return false;
        }
    }

    public void awakenDreamsAsync() {
        BackgroundThread.getHandler().post(new Runnable() {
            public void run() {
                try {
                    SystemServicesProxy.this.mDreamManager.awaken();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void registerMiuiTaskResizeList(final Context context) {
        BackgroundThread.getHandler().post(new Runnable() {
            public void run() {
                List<String> multiWindowForceResizeList = SystemServicesProxy.getMultiWindowForceResizeList(context);
                try {
                    SystemServicesProxy.this.mAm.getClass().getMethod("setResizeWhiteList", new Class[]{List.class}).invoke(SystemServicesProxy.this.mAm, new Object[]{multiWindowForceResizeList});
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e2) {
                    e2.printStackTrace();
                } catch (IllegalAccessException e3) {
                    e3.printStackTrace();
                }
                List<String> multiWindowForceNotResizeList = SystemServicesProxy.getMultiWindowForceNotResizeList(context);
                if (multiWindowForceNotResizeList.contains("com.miui.home")) {
                    SystemServicesProxy systemServicesProxy = SystemServicesProxy.this;
                    if (systemServicesProxy.isRecentsWithinLauncher(systemServicesProxy.mContext)) {
                        Log.e("SystemServicesProxy", "Remove com.miui.home from multiWindowForceNotResizeList");
                        multiWindowForceNotResizeList.remove("com.miui.home");
                    }
                }
                try {
                    SystemServicesProxy.this.mAm.getClass().getMethod("setResizeBlackList", new Class[]{List.class}).invoke(SystemServicesProxy.this.mAm, new Object[]{multiWindowForceNotResizeList});
                } catch (NoSuchMethodException e4) {
                    e4.printStackTrace();
                } catch (InvocationTargetException e5) {
                    e5.printStackTrace();
                } catch (IllegalAccessException e6) {
                    e6.printStackTrace();
                }
            }
        });
    }

    public boolean isRecentsWithinLauncher(Context context) {
        PackageInfo packageInfo;
        ApplicationInfo applicationInfo;
        Bundle bundle;
        try {
            packageInfo = context.getPackageManager().getPackageInfo("com.miui.home", 128);
        } catch (Exception e) {
            Log.e("SystemServicesProxy", "isRecentsWithinLauncher: getPackageInfo error.", e);
            packageInfo = null;
        }
        boolean z = false;
        if (!(packageInfo == null || (applicationInfo = packageInfo.applicationInfo) == null || (bundle = applicationInfo.metaData) == null)) {
            z = bundle.getBoolean("supportRecents", false);
        }
        Log.e("RecentsImpl", "isRecentsWithinLauncher=" + z);
        return z;
    }

    public boolean useMiuiHomeAsDefaultHome(Context context) {
        ActivityInfo activityInfo;
        String str;
        ResolveInfo resolveActivity = context.getPackageManager().resolveActivity(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME"), 786432);
        if (resolveActivity == null || (activityInfo = resolveActivity.activityInfo) == null || (str = activityInfo.packageName) == null || "com.miui.home".equals(str)) {
            return true;
        }
        return false;
    }

    public static List<String> getMultiWindowForceResizeList(Context context) {
        synchronized (sMultiWindowForceResizePkgList) {
            if (sMultiWindowForceResizePkgList.isEmpty()) {
                List<MiuiSettings.SettingsCloudData.CloudData> cloudDataList = MiuiSettings.SettingsCloudData.getCloudDataList(context.getContentResolver(), Utilities.isAndroidNorNewer() ? "MultiWindowForceResizePkgsForN" : "MultiWindowForceResizePkgsForM");
                if (cloudDataList != null) {
                    try {
                        for (MiuiSettings.SettingsCloudData.CloudData cloudData : cloudDataList) {
                            String cloudData2 = cloudData.toString();
                            Log.d("SystemServicesProxy", "json=" + cloudData2);
                            if (!TextUtils.isEmpty(cloudData2)) {
                                String optString = new JSONObject(cloudData2).optString("pkg");
                                if (!TextUtils.isEmpty(optString)) {
                                    sMultiWindowForceResizePkgList.add(optString);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (sMultiWindowForceResizePkgList.isEmpty()) {
                sMultiWindowForceResizePkgList.addAll(Arrays.asList(context.getResources().getStringArray(R.array.multi_window_force_resize_pkgs)));
            }
        }
        return sMultiWindowForceResizePkgList;
    }

    public static List<String> getMultiWindowForceNotResizeList(Context context) {
        synchronized (sMultiWindowForceNotResizePkgList) {
            if (sMultiWindowForceNotResizePkgList.isEmpty()) {
                List<MiuiSettings.SettingsCloudData.CloudData> cloudDataList = MiuiSettings.SettingsCloudData.getCloudDataList(context.getContentResolver(), Utilities.isAndroidNorNewer() ? "MultiWindowForceNotResizePkgsForN" : "MultiWindowForceNotResizePkgsForM");
                if (cloudDataList != null) {
                    try {
                        for (MiuiSettings.SettingsCloudData.CloudData cloudData : cloudDataList) {
                            String cloudData2 = cloudData.toString();
                            Log.d("SystemServicesProxy", "json=" + cloudData2);
                            if (!TextUtils.isEmpty(cloudData2)) {
                                String optString = new JSONObject(cloudData2).optString("pkg");
                                if (!TextUtils.isEmpty(optString)) {
                                    sMultiWindowForceNotResizePkgList.add(optString);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (sMultiWindowForceNotResizePkgList.isEmpty()) {
                sMultiWindowForceNotResizePkgList.addAll(Arrays.asList(context.getResources().getStringArray(R.array.multi_window_force_not_resize_pkgs)));
            }
        }
        return sMultiWindowForceNotResizePkgList;
    }

    public void registerFsGestureCall(String str, IFsGestureCallback iFsGestureCallback) {
        if (str != null && iFsGestureCallback != null) {
            this.mIFsGestureCallbackMap.put(str, iFsGestureCallback);
        }
    }

    public void unRegisterFsGestureCall(String str, IFsGestureCallback iFsGestureCallback) {
        if (str != null && iFsGestureCallback != null && this.mIFsGestureCallbackMap.containsKey(str)) {
            this.mIFsGestureCallbackMap.remove(str);
        }
    }

    public void changeAlphaScaleForFsGesture(String str, float f, float f2) {
        changeAlphaScaleForFsGesture(str, f, f2, 0, 0);
    }

    public void changeAlphaScaleForFsGesture(String str, float f, float f2, int i, int i2) {
        changeAlphaScaleForFsGesture(str, f, f2, i, i2, 0, 0, false);
    }

    public void changeAlphaScaleForFsGesture(String str, float f, float f2, int i, int i2, int i3, int i4, boolean z) {
        String str2 = str;
        IFsGestureCallback iFsGestureCallback = this.mIFsGestureCallbackMap.get(str);
        if (iFsGestureCallback != null) {
            try {
                iFsGestureCallback.changeAlphaScale(f, f2, i, i2, i3, i4, z);
                if (DEBUG) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("changeAlphaScaleForFsGesture callbackKey=");
                    sb.append(str);
                    sb.append(" alpha=");
                    float f3 = f;
                    sb.append(f);
                    sb.append(" scale=");
                    float f4 = f2;
                    sb.append(f2);
                    Log.d("SystemServicesProxy", sb.toString(), new Throwable());
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void notifyHomeModeFsGestureStart(String str) {
        IFsGestureCallback iFsGestureCallback = this.mIFsGestureCallbackMap.get(str);
        if (iFsGestureCallback != null) {
            try {
                iFsGestureCallback.notifyHomeModeFsGestureStart();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void setIsFsGestureAnimating(boolean z) {
        this.mIsFsGestureAnimating = z;
    }

    public boolean isFsGestureAnimating() {
        return this.mIsFsGestureAnimating;
    }

    private final class H extends Handler {
        private H() {
        }

        public void handleMessage(Message message) {
            synchronized (SystemServicesProxy.this.mTaskStackListeners) {
                switch (message.what) {
                    case 1:
                        Trace.beginSection("onTaskStackChanged");
                        for (int size = SystemServicesProxy.this.mTaskStackListeners.size() - 1; size >= 0; size--) {
                            ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(size)).onTaskStackChanged();
                        }
                        Trace.endSection();
                        break;
                    case 2:
                        Trace.beginSection("onTaskSnapshotChanged");
                        for (int size2 = SystemServicesProxy.this.mTaskStackListeners.size() - 1; size2 >= 0; size2--) {
                            ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(size2)).onTaskSnapshotChanged(message.arg1, (ActivityManager.TaskSnapshot) message.obj);
                        }
                        Trace.endSection();
                        break;
                    case 3:
                        for (int size3 = SystemServicesProxy.this.mTaskStackListeners.size() - 1; size3 >= 0; size3--) {
                            ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(size3)).onActivityPinned((String) message.obj, message.arg1, message.arg2);
                        }
                        break;
                    case 4:
                        for (int size4 = SystemServicesProxy.this.mTaskStackListeners.size() - 1; size4 >= 0; size4--) {
                            ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(size4)).onPinnedActivityRestartAttempt(message.arg1 != 0);
                        }
                        break;
                    case 5:
                        for (int size5 = SystemServicesProxy.this.mTaskStackListeners.size() - 1; size5 >= 0; size5--) {
                            ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(size5)).onPinnedStackAnimationEnded();
                        }
                        break;
                    case 6:
                        for (int size6 = SystemServicesProxy.this.mTaskStackListeners.size() - 1; size6 >= 0; size6--) {
                            ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(size6)).onActivityForcedResizable((String) message.obj, message.arg1, message.arg2);
                        }
                        break;
                    case 7:
                        for (int size7 = SystemServicesProxy.this.mTaskStackListeners.size() - 1; size7 >= 0; size7--) {
                            ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(size7)).onActivityDismissingDockedStack();
                        }
                        break;
                    case 8:
                        for (int size8 = SystemServicesProxy.this.mTaskStackListeners.size() - 1; size8 >= 0; size8--) {
                            ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(size8)).onTaskProfileLocked(message.arg1, message.arg2);
                        }
                        break;
                    case 9:
                        for (int size9 = SystemServicesProxy.this.mTaskStackListeners.size() - 1; size9 >= 0; size9--) {
                            ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(size9)).onPinnedStackAnimationStarted();
                        }
                        break;
                    case 10:
                        for (int size10 = SystemServicesProxy.this.mTaskStackListeners.size() - 1; size10 >= 0; size10--) {
                            ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(size10)).onActivityUnpinned();
                        }
                        break;
                    case 11:
                        for (int size11 = SystemServicesProxy.this.mTaskStackListeners.size() - 1; size11 >= 0; size11--) {
                            ((TaskStackListener) SystemServicesProxy.this.mTaskStackListeners.get(size11)).onActivityLaunchOnSecondaryDisplayFailed();
                        }
                        break;
                }
            }
        }
    }

    public TaskStackListener getTaskStackListener() {
        return new TaskStackListener() {
            public void onTaskStackChangedBackground() {
                if (Recents.getConfiguration().svelteLevel == 0) {
                    RecentsTaskLoader taskLoader = Recents.getTaskLoader();
                    ActivityManager.RunningTaskInfo runningTask = Recents.getSystemServices().getRunningTask();
                    RecentsTaskLoadPlan createLoadPlan = taskLoader.createLoadPlan(SystemServicesProxy.this.mContext);
                    taskLoader.preloadTasks(createLoadPlan, -1, false);
                    RecentsTaskLoadPlan.Options options = new RecentsTaskLoadPlan.Options();
                    if (runningTask != null) {
                        options.runningTaskId = runningTask.id;
                        if (!runningTask.topActivity.getPackageName().equals("com.miui.home") && !runningTask.topActivity.getPackageName().equals("com.android.systemui")) {
                            RecentsActivity.mFreeBeforeClean = 0;
                        }
                    }
                    options.numVisibleTasks = 2;
                    options.numVisibleTaskThumbnails = 2;
                    options.onlyLoadForCache = true;
                    options.onlyLoadPausedActivities = true;
                    taskLoader.loadTasks(SystemServicesProxy.this.mContext, createLoadPlan, options);
                }
                RecentsPushEventHelper.sendTaskStackChangedEvent();
            }

            public void onTaskSnapshotChanged(int i, ActivityManager.TaskSnapshot taskSnapshot) {
                if (checkCurrentUserId(SystemServicesProxy.this.mContext, false) && taskSnapshot != null && taskSnapshot.getSnapshot() != null) {
                    ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo = new ActivityManager$TaskThumbnailInfo();
                    activityManager$TaskThumbnailInfo.taskWidth = taskSnapshot.getSnapshot().getWidth();
                    activityManager$TaskThumbnailInfo.taskHeight = taskSnapshot.getSnapshot().getHeight();
                    activityManager$TaskThumbnailInfo.screenOrientation = taskSnapshot.getOrientation();
                    activityManager$TaskThumbnailInfo.insets = taskSnapshot.getContentInsets();
                    activityManager$TaskThumbnailInfo.scale = taskSnapshot.getScale();
                    RecentsEventBus.getDefault().send(new TaskSnapshotChangedEvent(i, SystemUICompat.createHardwareBitmapFromSnapShot(taskSnapshot), activityManager$TaskThumbnailInfo, false));
                }
            }
        };
    }
}

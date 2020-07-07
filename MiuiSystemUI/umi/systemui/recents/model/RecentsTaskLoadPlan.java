package com.android.systemui.recents.model;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.UserInfo;
import android.content.pm.UserInfoCompat;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.UserManager;
import android.util.ArraySet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.android.systemui.Application;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import miui.process.ProcessManager;

public class RecentsTaskLoadPlan {
    Context mContext;
    ArraySet<Integer> mCurrentQuietProfiles = new ArraySet<>();
    List<ActivityManager.RecentTaskInfo> mRawTasks;
    TaskStack mStack;

    public static class Options {
        public boolean loadIcons = true;
        public boolean loadThumbnails = true;
        public int numVisibleTaskThumbnails = 0;
        public int numVisibleTasks = 0;
        public boolean onlyLoadForCache = false;
        public boolean onlyLoadPausedActivities = false;
        public int runningTaskId = -1;
    }

    RecentsTaskLoadPlan(Context context) {
        this.mContext = context;
    }

    private void updateCurrentQuietProfilesCache(int i) {
        this.mCurrentQuietProfiles.clear();
        if (i == -2) {
            i = ActivityManager.getCurrentUser();
        }
        List profiles = ((UserManager) this.mContext.getSystemService("user")).getProfiles(i);
        if (profiles != null) {
            for (int i2 = 0; i2 < profiles.size(); i2++) {
                UserInfo userInfo = (UserInfo) profiles.get(i2);
                if (userInfo.isManagedProfile() && UserInfoCompat.isQuietModeEnabled(userInfo)) {
                    this.mCurrentQuietProfiles.add(Integer.valueOf(userInfo.id));
                }
            }
        }
    }

    public synchronized void preloadRawTasks(boolean z) {
        updateCurrentQuietProfilesCache(-2);
        this.mRawTasks = Recents.getSystemServices().getRecentTasks(ActivityManager.getMaxRecentTasksStatic(), -2, z, this.mCurrentQuietProfiles);
    }

    public synchronized void preloadPlan(RecentsTaskLoader recentsTaskLoader, int i, boolean z, Set set) {
        Task.TaskKey taskKey;
        boolean z2;
        String str;
        RecentsTaskLoader recentsTaskLoader2 = recentsTaskLoader;
        synchronized (this) {
            SystemServicesProxy systemServices = Recents.getSystemServices();
            Resources resources = this.mContext.getResources();
            ArrayList arrayList = new ArrayList();
            if (this.mRawTasks == null) {
                preloadRawTasks(z);
            }
            SparseArray sparseArray = new SparseArray();
            SparseIntArray sparseIntArray = new SparseIntArray();
            String string = this.mContext.getString(R.string.accessibility_recents_item_will_be_dismissed);
            String string2 = this.mContext.getString(R.string.accessibility_recents_item_open_app_info);
            int size = this.mRawTasks.size();
            int i2 = 0;
            while (i2 < size) {
                ActivityManager.RecentTaskInfo recentTaskInfo = this.mRawTasks.get(i2);
                int windowModeFromRecentTaskInfo = systemServices.getWindowModeFromRecentTaskInfo(recentTaskInfo);
                boolean contains = set.contains(recentTaskInfo.baseIntent.getComponent().getPackageName());
                Recents recents = (Recents) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(Recents.class);
                if (recents.getRecentsImpl() != null) {
                    recents.getRecentsImpl().isScreeningPkg(recentTaskInfo.baseIntent.getComponent().getPackageName());
                }
                int i3 = size;
                int i4 = i2;
                SparseArray sparseArray2 = sparseArray;
                SparseIntArray sparseIntArray2 = sparseIntArray;
                new Task.TaskKey(recentTaskInfo.persistentId, recentTaskInfo.stackId, windowModeFromRecentTaskInfo, recentTaskInfo.baseIntent, recentTaskInfo.userId, recentTaskInfo.lastActiveTime, contains, false);
                SystemServicesProxy.isFreeformStack(recentTaskInfo.stackId);
                Task.TaskKey taskKey2 = taskKey;
                boolean z3 = taskKey2.id == i;
                ActivityInfo andUpdateActivityInfo = recentsTaskLoader2.getAndUpdateActivityInfo(taskKey2);
                String andUpdateActivityTitle = recentsTaskLoader2.getAndUpdateActivityTitle(taskKey2, recentTaskInfo.taskDescription);
                String andUpdateContentDescription = recentsTaskLoader2.getAndUpdateContentDescription(taskKey2, resources);
                String format = String.format(string, new Object[]{andUpdateContentDescription});
                String format2 = String.format(string2, new Object[]{andUpdateContentDescription});
                Drawable andUpdateActivityIcon = recentsTaskLoader2.getAndUpdateActivityIcon(taskKey2, recentTaskInfo.taskDescription, resources, false);
                boolean isAccessLocked = systemServices.isAccessLocked(taskKey2);
                Bitmap andUpdateThumbnail = recentsTaskLoader2.getAndUpdateThumbnail(taskKey2, false, isAccessLocked);
                int activityPrimaryColor = recentsTaskLoader2.getActivityPrimaryColor(recentTaskInfo.taskDescription);
                int activityBackgroundColor = recentsTaskLoader2.getActivityBackgroundColor(recentTaskInfo.taskDescription);
                boolean z4 = (andUpdateActivityInfo == null || (andUpdateActivityInfo.applicationInfo.flags & 1) == 0) ? false : true;
                if (andUpdateActivityInfo != null) {
                    try {
                        str = andUpdateActivityInfo.packageName;
                    } catch (Exception e) {
                        Log.e("RecentsTaskLoadPlan", "getAppLockStateForUserId", e);
                        z2 = false;
                    }
                } else {
                    str = null;
                }
                z2 = ProcessManager.isLockedApplication(str, recentTaskInfo.userId);
                arrayList.add(new Task(taskKey2, andUpdateActivityIcon, andUpdateThumbnail, andUpdateActivityTitle, andUpdateContentDescription, format, format2, activityPrimaryColor, activityBackgroundColor, z3, true, z4, ActivityManagerCompat.isRecentTaskDockable(recentTaskInfo), ActivityManagerCompat.getRecentTaskBound(recentTaskInfo), recentTaskInfo.taskDescription, ActivityManagerCompat.getRecentTaskResizeMode(recentTaskInfo), recentTaskInfo.topActivity, z2, isAccessLocked));
                SparseIntArray sparseIntArray3 = sparseIntArray2;
                sparseIntArray3.put(taskKey2.id, sparseIntArray3.get(taskKey2.id, 0) + 1);
                SparseArray sparseArray3 = sparseArray2;
                sparseArray3.put(taskKey2.id, taskKey2);
                sparseArray = sparseArray3;
                sparseIntArray = sparseIntArray3;
                size = i3;
                i2 = i4 + 1;
            }
            TaskStack taskStack = new TaskStack();
            this.mStack = taskStack;
            taskStack.setTasks(this.mContext, arrayList, false);
        }
    }

    public synchronized void executePlan(Options options, RecentsTaskLoader recentsTaskLoader, TaskResourceLoadQueue taskResourceLoadQueue) {
        Recents.getConfiguration();
        Resources resources = this.mContext.getResources();
        ArrayList<Task> stackTasks = this.mStack.getStackTasks();
        int size = stackTasks.size();
        int i = 0;
        while (i < size) {
            Task task = stackTasks.get(i);
            Task.TaskKey taskKey = task.key;
            boolean z = task.key.id == options.runningTaskId;
            boolean z2 = i <= options.numVisibleTasks;
            boolean z3 = i <= options.numVisibleTaskThumbnails;
            if (!options.onlyLoadPausedActivities || !z) {
                if (options.loadIcons && ((z || z2) && task.icon == null)) {
                    task.icon = recentsTaskLoader.getAndUpdateActivityIcon(taskKey, task.taskDescription, resources, true);
                }
                if (options.loadThumbnails && ((z || z3) && (task.thumbnail == null || z || task.isAccessLocked))) {
                    if (!task.isBlurThumbnail()) {
                        task.thumbnail = recentsTaskLoader.getAndUpdateThumbnail(taskKey, true, task.isAccessLocked);
                    } else {
                        taskResourceLoadQueue.addTask(task);
                        options.onlyLoadForCache = false;
                    }
                }
            }
            i++;
        }
    }

    public TaskStack getTaskStack() {
        return this.mStack;
    }

    public boolean hasTasks() {
        TaskStack taskStack = this.mStack;
        if (taskStack == null || taskStack.getTaskCount() <= 0) {
            return false;
        }
        return true;
    }
}

package com.android.systemui.recents.model;

import android.app.ActivityManager;
import android.app.ActivityManagerCompat;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MiuiSettings;
import android.util.Log;
import android.util.LruCache;
import com.android.systemui.plugins.R;
import com.android.systemui.proxy.ActivityManager$TaskThumbnailInfo;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.RecentsConfiguration;
import com.android.systemui.recents.events.activity.PackagesChangedEvent;
import com.android.systemui.recents.events.activity.ThumbnailBlurPkgsChangedEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import com.android.systemui.recents.model.RecentsTaskLoadPlan;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.model.TaskKeyLruCache;
import java.io.PrintWriter;
import java.util.HashSet;

public class RecentsTaskLoader {
    /* access modifiers changed from: private */
    public final LruCache<ComponentName, ActivityInfo> mActivityInfoCache;
    private final TaskKeyLruCache<String> mActivityLabelCache;
    private TaskKeyLruCache.EvictionCallback mClearActivityInfoOnEviction = new TaskKeyLruCache.EvictionCallback() {
        public void onEntryEvicted(Task.TaskKey taskKey) {
            if (taskKey != null) {
                RecentsTaskLoader.this.mActivityInfoCache.remove(taskKey.getComponent());
            }
        }
    };
    private final TaskKeyLruCache<String> mContentDescriptionCache;
    private Context mContext;
    BitmapDrawable mDefaultIcon;
    int mDefaultTaskBarBackgroundColor;
    int mDefaultTaskViewBackgroundColor;
    Bitmap mDefaultThumbnail;
    private final TaskKeyLruCache<Drawable> mIconCache;
    private final TaskResourceLoadQueue mLoadQueue;
    private final BackgroundTaskLoader mLoader;
    private final int mMaxIconCacheSize;
    private final int mMaxThumbnailCacheSize;
    private int mNumVisibleTasksLoaded;
    private int mNumVisibleThumbnailsLoaded;
    private final TaskKeyLruCache<ThumbnailData> mSnapshotCache;
    private HashSet mThumbnailBlurPkgSet;
    private final TaskKeyLruCache<ThumbnailData> mThumbnailCache;

    public RecentsTaskLoader(Context context) {
        this.mContext = context;
        Resources resources = context.getResources();
        this.mDefaultTaskBarBackgroundColor = context.getColor(R.color.recents_task_bar_default_background_color);
        this.mDefaultTaskViewBackgroundColor = context.getColor(R.color.recents_task_view_default_background_color);
        this.mMaxThumbnailCacheSize = resources.getInteger(R.integer.config_recents_max_thumbnail_count);
        this.mMaxIconCacheSize = resources.getInteger(R.integer.config_recents_max_icon_count);
        int i = this.mMaxIconCacheSize;
        int i2 = this.mMaxThumbnailCacheSize;
        Bitmap createBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8);
        createBitmap.eraseColor(0);
        this.mDefaultThumbnail = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        this.mDefaultThumbnail.setHasAlpha(false);
        this.mDefaultThumbnail.eraseColor(-1);
        this.mDefaultIcon = new BitmapDrawable(context.getResources(), createBitmap);
        int maxRecentTasksStatic = ActivityManager.getMaxRecentTasksStatic();
        this.mLoadQueue = new TaskResourceLoadQueue();
        this.mIconCache = new TaskKeyLruCache<>(i, this.mClearActivityInfoOnEviction);
        this.mThumbnailCache = new TaskKeyLruCache<>(i2);
        this.mSnapshotCache = new TaskKeyLruCache<>(i2);
        this.mActivityLabelCache = new TaskKeyLruCache<>(maxRecentTasksStatic, this.mClearActivityInfoOnEviction);
        this.mContentDescriptionCache = new TaskKeyLruCache<>(maxRecentTasksStatic, this.mClearActivityInfoOnEviction);
        this.mActivityInfoCache = new LruCache<>(maxRecentTasksStatic);
        this.mLoader = new BackgroundTaskLoader(this.mLoadQueue, this.mIconCache, this.mThumbnailCache, this.mSnapshotCache, this.mDefaultThumbnail, this.mDefaultIcon);
    }

    public int getIconCacheSize() {
        return this.mMaxIconCacheSize;
    }

    public int getThumbnailCacheSize() {
        return this.mMaxThumbnailCacheSize;
    }

    public RecentsTaskLoadPlan createLoadPlan(Context context) {
        return new RecentsTaskLoadPlan(context);
    }

    public void preloadTasks(RecentsTaskLoadPlan recentsTaskLoadPlan, int i, boolean z) {
        if (this.mThumbnailBlurPkgSet == null) {
            this.mThumbnailBlurPkgSet = Utilities.convertStringToSet(MiuiSettings.System.getStringForUser(this.mContext.getContentResolver(), "miui_recents_privacy_thumbnail_blur", -2));
        }
        recentsTaskLoadPlan.preloadPlan(this, i, z, this.mThumbnailBlurPkgSet);
    }

    public final void onBusEvent(ThumbnailBlurPkgsChangedEvent thumbnailBlurPkgsChangedEvent) {
        this.mThumbnailBlurPkgSet = thumbnailBlurPkgsChangedEvent.mThumbnailBlurPkgSet;
    }

    public void loadTasks(Context context, RecentsTaskLoadPlan recentsTaskLoadPlan, RecentsTaskLoadPlan.Options options) {
        if (options != null) {
            recentsTaskLoadPlan.executePlan(options, this, this.mLoadQueue);
            if (!options.onlyLoadForCache) {
                this.mNumVisibleTasksLoaded = options.numVisibleTasks;
                this.mNumVisibleThumbnailsLoaded = options.numVisibleTaskThumbnails;
                this.mLoader.start(context);
                return;
            }
            return;
        }
        throw new RuntimeException("Requires load options");
    }

    public void updateBlurThumbnail(Context context, Task task, Bitmap bitmap, ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo) {
        ThumbnailData thumbnailData = new ThumbnailData();
        thumbnailData.thumbnail = bitmap;
        thumbnailData.isDeterminedWhetherBlur = false;
        thumbnailData.thumbnailInfo = activityManager$TaskThumbnailInfo;
        this.mSnapshotCache.put(task.key, thumbnailData);
        this.mLoadQueue.addTask(task);
        this.mLoader.start(context);
    }

    public void loadTaskData(Task task) {
        Bitmap bitmap;
        ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo;
        Drawable andInvalidateIfModified = this.mIconCache.getAndInvalidateIfModified(task.key);
        ThumbnailData andInvalidateIfModified2 = this.mThumbnailCache.getAndInvalidateIfModified(task.key);
        Bitmap bitmap2 = null;
        if (andInvalidateIfModified2 != null) {
            bitmap = andInvalidateIfModified2.thumbnail;
            activityManager$TaskThumbnailInfo = andInvalidateIfModified2.thumbnailInfo;
        } else {
            activityManager$TaskThumbnailInfo = null;
            bitmap = null;
        }
        boolean z = andInvalidateIfModified == null || bitmap == null;
        if (andInvalidateIfModified == null) {
            andInvalidateIfModified = this.mDefaultIcon;
        }
        if (z) {
            this.mLoadQueue.addTask(task);
        }
        if (bitmap != this.mDefaultThumbnail) {
            bitmap2 = bitmap;
        }
        task.notifyTaskDataLoaded(bitmap2, andInvalidateIfModified, activityManager$TaskThumbnailInfo);
    }

    public void unloadTaskData(Task task) {
        this.mLoadQueue.removeTask(task);
        task.notifyTaskDataUnloaded((Bitmap) null, this.mDefaultIcon);
    }

    public void deleteTaskData(Task task, boolean z) {
        this.mLoadQueue.removeTask(task);
        this.mThumbnailCache.remove(task.key);
        this.mIconCache.remove(task.key);
        this.mActivityLabelCache.remove(task.key);
        this.mContentDescriptionCache.remove(task.key);
        if (z) {
            task.notifyTaskDataUnloaded((Bitmap) null, this.mDefaultIcon);
        }
    }

    public void onTrimMemory(int i) {
        RecentsConfiguration configuration = Recents.getConfiguration();
        if (i != 5) {
            if (i != 10) {
                if (i != 15) {
                    if (i == 20) {
                        stopLoader();
                        int i2 = configuration.svelteLevel;
                        if (i2 == 0) {
                            this.mThumbnailCache.trimToSize(Math.max(this.mNumVisibleTasksLoaded, this.mMaxThumbnailCacheSize / 2));
                        } else if (i2 == 1) {
                            this.mThumbnailCache.trimToSize(this.mNumVisibleThumbnailsLoaded);
                        } else if (i2 >= 2) {
                            this.mThumbnailCache.evictAll();
                        }
                        this.mIconCache.trimToSize(Math.max(this.mNumVisibleTasksLoaded, this.mMaxIconCacheSize / 2));
                        return;
                    } else if (i != 40) {
                        if (i != 60) {
                            if (i != 80) {
                                return;
                            }
                        }
                    }
                }
                this.mThumbnailCache.evictAll();
                this.mIconCache.evictAll();
                this.mActivityInfoCache.evictAll();
                this.mActivityLabelCache.evictAll();
                this.mContentDescriptionCache.evictAll();
                return;
            }
            this.mThumbnailCache.trimToSize(Math.max(1, this.mMaxThumbnailCacheSize / 4));
            this.mIconCache.trimToSize(Math.max(1, this.mMaxIconCacheSize / 4));
            this.mActivityInfoCache.trimToSize(Math.max(1, ActivityManager.getMaxRecentTasksStatic() / 4));
            return;
        }
        this.mThumbnailCache.trimToSize(Math.max(1, this.mMaxThumbnailCacheSize / 2));
        this.mIconCache.trimToSize(Math.max(1, this.mMaxIconCacheSize / 2));
        this.mActivityInfoCache.trimToSize(Math.max(1, ActivityManager.getMaxRecentTasksStatic() / 2));
    }

    /* access modifiers changed from: package-private */
    public String getAndUpdateActivityTitle(Task.TaskKey taskKey, ActivityManager.TaskDescription taskDescription) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        if (taskDescription != null && taskDescription.getLabel() != null) {
            return taskDescription.getLabel();
        }
        String andInvalidateIfModified = this.mActivityLabelCache.getAndInvalidateIfModified(taskKey);
        if (andInvalidateIfModified != null) {
            return andInvalidateIfModified;
        }
        ActivityInfo andUpdateActivityInfo = getAndUpdateActivityInfo(taskKey);
        if (andUpdateActivityInfo == null) {
            return "";
        }
        String badgedActivityLabel = systemServices.getBadgedActivityLabel(andUpdateActivityInfo, taskKey.userId);
        this.mActivityLabelCache.put(taskKey, badgedActivityLabel);
        return badgedActivityLabel;
    }

    /* access modifiers changed from: package-private */
    public String getAndUpdateContentDescription(Task.TaskKey taskKey, Resources resources) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        String andInvalidateIfModified = this.mContentDescriptionCache.getAndInvalidateIfModified(taskKey);
        if (andInvalidateIfModified != null) {
            return andInvalidateIfModified;
        }
        ActivityInfo andUpdateActivityInfo = getAndUpdateActivityInfo(taskKey);
        if (andUpdateActivityInfo == null) {
            return "";
        }
        String badgedContentDescription = systemServices.getBadgedContentDescription(andUpdateActivityInfo, taskKey.userId, resources);
        this.mContentDescriptionCache.put(taskKey, badgedContentDescription);
        return badgedContentDescription;
    }

    public Drawable getAndUpdateActivityIcon(Task.TaskKey taskKey, ActivityManager.TaskDescription taskDescription, Resources resources, boolean z) {
        Drawable badgedActivityIcon;
        SystemServicesProxy systemServices = Recents.getSystemServices();
        Drawable andInvalidateIfModified = this.mIconCache.getAndInvalidateIfModified(taskKey);
        if (andInvalidateIfModified != null) {
            return andInvalidateIfModified;
        }
        if (!z) {
            return null;
        }
        try {
            Drawable badgedTaskDescriptionIcon = systemServices.getBadgedTaskDescriptionIcon(taskDescription, taskKey.userId, resources);
            if (badgedTaskDescriptionIcon != null) {
                this.mIconCache.put(taskKey, badgedTaskDescriptionIcon);
                return badgedTaskDescriptionIcon;
            }
        } catch (Exception e) {
            Log.e("RecentsTaskLoader", "getBadgedTaskDescriptionIcon error", e);
        }
        ActivityInfo andUpdateActivityInfo = getAndUpdateActivityInfo(taskKey);
        if (andUpdateActivityInfo == null || (badgedActivityIcon = systemServices.getBadgedActivityIcon(andUpdateActivityInfo, taskKey.userId)) == null) {
            return null;
        }
        this.mIconCache.put(taskKey, badgedActivityIcon);
        return badgedActivityIcon;
    }

    /* access modifiers changed from: package-private */
    public Bitmap getAndUpdateThumbnail(Task.TaskKey taskKey, boolean z, boolean z2) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        ThumbnailData andInvalidateIfModified = this.mThumbnailCache.getAndInvalidateIfModified(taskKey);
        if (andInvalidateIfModified != null) {
            if (andInvalidateIfModified.isAccessLocked == z2) {
                return andInvalidateIfModified.thumbnail;
            }
            this.mThumbnailCache.remove(taskKey);
        }
        if (!z || Recents.getConfiguration().svelteLevel >= 3) {
            return null;
        }
        ThumbnailData taskThumbnail = systemServices.getTaskThumbnail(taskKey);
        if (taskThumbnail.thumbnail == null) {
            return null;
        }
        this.mThumbnailCache.put(taskKey, taskThumbnail);
        return taskThumbnail.thumbnail;
    }

    /* access modifiers changed from: package-private */
    public int getActivityPrimaryColor(ActivityManager.TaskDescription taskDescription) {
        if (taskDescription == null || taskDescription.getPrimaryColor() == 0) {
            return this.mDefaultTaskBarBackgroundColor;
        }
        return taskDescription.getPrimaryColor();
    }

    /* access modifiers changed from: package-private */
    public int getActivityBackgroundColor(ActivityManager.TaskDescription taskDescription) {
        if (taskDescription == null || ActivityManagerCompat.getTaskDescriptionBackgroundColor(taskDescription) == 0) {
            return this.mDefaultTaskViewBackgroundColor;
        }
        return ActivityManagerCompat.getTaskDescriptionBackgroundColor(taskDescription);
    }

    /* access modifiers changed from: package-private */
    public ActivityInfo getAndUpdateActivityInfo(Task.TaskKey taskKey) {
        SystemServicesProxy systemServices = Recents.getSystemServices();
        ComponentName component = taskKey.getComponent();
        ActivityInfo activityInfo = this.mActivityInfoCache.get(component);
        if (activityInfo == null) {
            activityInfo = systemServices.getActivityInfo(component, taskKey.userId);
            if (component == null || activityInfo == null) {
                Log.e("RecentsTaskLoader", "Unexpected null component name or activity info: " + component + ", " + activityInfo);
                return null;
            }
            this.mActivityInfoCache.put(component, activityInfo);
        }
        return activityInfo;
    }

    private void stopLoader() {
        this.mLoader.stop();
        this.mLoadQueue.clearTasks();
    }

    public void onThemeChanged() {
        this.mIconCache.evictAll();
    }

    public void onLanguageChange() {
        this.mActivityLabelCache.evictAll();
    }

    public final void onBusEvent(PackagesChangedEvent packagesChangedEvent) {
        for (ComponentName next : this.mActivityInfoCache.snapshot().keySet()) {
            if (next.getPackageName().equals(packagesChangedEvent.packageName)) {
                this.mActivityInfoCache.remove(next);
            }
        }
    }

    public void dump(String str, PrintWriter printWriter) {
        String str2 = str + "  ";
        printWriter.print(str);
        printWriter.println("RecentsTaskLoader");
        printWriter.print(str);
        printWriter.println("Icon Cache");
        this.mIconCache.dump(str2, printWriter);
        printWriter.print(str);
        printWriter.println("Thumbnail Cache");
        this.mThumbnailCache.dump(str2, printWriter);
    }
}

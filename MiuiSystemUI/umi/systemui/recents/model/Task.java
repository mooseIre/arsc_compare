package com.android.systemui.recents.model;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ViewDebug;
import com.android.systemui.proxy.ActivityManager$TaskThumbnailInfo;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.misc.Utilities;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Objects;

public class Task {
    @ViewDebug.ExportedProperty(category = "recents")
    public String appInfoDescription;
    @ViewDebug.ExportedProperty(category = "recents")
    public Rect bounds;
    @ViewDebug.ExportedProperty(category = "recents")
    public int colorBackground;
    @ViewDebug.ExportedProperty(category = "recents")
    public int colorPrimary;
    @ViewDebug.ExportedProperty(category = "recents")
    public String dismissDescription;
    public Drawable icon;
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean isAccessLocked;
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean isDockable;
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean isLaunchTarget;
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean isLocked;
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean isStackTask;
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean isSystemApp;
    @ViewDebug.ExportedProperty(deepExport = true, prefix = "key_")
    public TaskKey key;
    private ArrayList<TaskCallbacks> mCallbacks = new ArrayList<>();
    @ViewDebug.ExportedProperty(category = "recents")
    public int resizeMode;
    public ActivityManager.TaskDescription taskDescription;
    public int temporarySortIndexInStack;
    public Bitmap thumbnail;
    @ViewDebug.ExportedProperty(category = "recents")
    public String title;
    @ViewDebug.ExportedProperty(category = "recents")
    public String titleDescription;
    @ViewDebug.ExportedProperty(category = "recents")
    public ComponentName topActivity;
    @ViewDebug.ExportedProperty(category = "recents")
    public boolean useLightOnPrimaryColor;

    public interface TaskCallbacks {
        void onTaskDataLoaded(Task task, ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo);

        void onTaskDataUnloaded();

        void onTaskStackIdChanged();
    }

    public static class TaskKey {
        @ViewDebug.ExportedProperty(category = "recents")
        public final Intent baseIntent;
        @ViewDebug.ExportedProperty(category = "recents")
        public final int id;
        public boolean isScreening;
        public boolean isThumbnailBlur;
        @ViewDebug.ExportedProperty(category = "recents")
        public long lastActiveTime;
        private int mHashCode;
        @ViewDebug.ExportedProperty(category = "recents")
        public int stackId;
        @ViewDebug.ExportedProperty(category = "recents")
        public final int userId;
        public int windowingMode;

        public TaskKey(int i, int i2, int i3, Intent intent, int i4, long j, boolean z, boolean z2) {
            this.id = i;
            this.stackId = i2;
            this.windowingMode = i3;
            this.baseIntent = intent;
            this.userId = i4;
            this.lastActiveTime = j;
            this.isThumbnailBlur = z;
            this.isScreening = z2;
            updateHashCode();
        }

        public void setStackId(int i) {
            this.stackId = i;
            updateHashCode();
        }

        public ComponentName getComponent() {
            return this.baseIntent.getComponent();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof TaskKey)) {
                return false;
            }
            TaskKey taskKey = (TaskKey) obj;
            if (this.id == taskKey.id && this.stackId == taskKey.stackId && this.userId == taskKey.userId && this.windowingMode == taskKey.windowingMode) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return this.mHashCode;
        }

        public String toString() {
            return "id=" + this.id + " stackId=" + this.stackId + " windowingMode=" + this.windowingMode + " user=" + this.userId + " lastActiveTime=" + this.lastActiveTime;
        }

        private void updateHashCode() {
            this.mHashCode = Objects.hash(new Object[]{Integer.valueOf(this.id), Integer.valueOf(this.stackId), Integer.valueOf(this.windowingMode), Integer.valueOf(this.userId)});
        }
    }

    public Task() {
    }

    public Task(TaskKey taskKey, Drawable drawable, Bitmap bitmap, String str, String str2, String str3, String str4, int i, int i2, boolean z, boolean z2, boolean z3, boolean z4, Rect rect, ActivityManager.TaskDescription taskDescription2, int i3, ComponentName componentName, boolean z5, boolean z6) {
        this.key = taskKey;
        this.icon = drawable;
        this.thumbnail = bitmap;
        this.title = str;
        this.titleDescription = str2;
        this.dismissDescription = str3;
        this.appInfoDescription = str4;
        this.colorPrimary = i;
        this.colorBackground = i2;
        this.useLightOnPrimaryColor = Utilities.computeContrastBetweenColors(i, -1) > 3.0f;
        this.bounds = rect;
        this.taskDescription = taskDescription2;
        this.isLaunchTarget = z;
        this.isStackTask = z2;
        this.isSystemApp = z3;
        this.isDockable = z4;
        this.resizeMode = i3;
        this.topActivity = componentName;
        this.isLocked = z5;
        this.isAccessLocked = z6;
    }

    public void copyFrom(Task task) {
        this.key = task.key;
        this.icon = task.icon;
        this.thumbnail = task.thumbnail;
        this.title = task.title;
        this.titleDescription = task.titleDescription;
        this.dismissDescription = task.dismissDescription;
        this.appInfoDescription = task.appInfoDescription;
        this.colorPrimary = task.colorPrimary;
        this.colorBackground = task.colorBackground;
        this.useLightOnPrimaryColor = task.useLightOnPrimaryColor;
        this.bounds = task.bounds;
        this.taskDescription = task.taskDescription;
        this.isLaunchTarget = task.isLaunchTarget;
        this.isStackTask = task.isStackTask;
        this.isSystemApp = task.isSystemApp;
        this.isDockable = task.isDockable;
        this.resizeMode = task.resizeMode;
        this.topActivity = task.topActivity;
        this.isLocked = task.isLocked;
        this.isAccessLocked = task.isAccessLocked;
    }

    public void addCallback(TaskCallbacks taskCallbacks) {
        if (!this.mCallbacks.contains(taskCallbacks)) {
            this.mCallbacks.add(taskCallbacks);
        }
    }

    public void removeCallback(TaskCallbacks taskCallbacks) {
        this.mCallbacks.remove(taskCallbacks);
    }

    public boolean hasCallBack() {
        return this.mCallbacks.size() > 0;
    }

    public void setStackId(int i) {
        this.key.setStackId(i);
        int size = this.mCallbacks.size();
        for (int i2 = 0; i2 < size; i2++) {
            this.mCallbacks.get(i2).onTaskStackIdChanged();
        }
    }

    public boolean isFreeformTask() {
        return Recents.getSystemServices().hasFreeformWorkspaceSupport() && SystemServicesProxy.isFreeformStack(this.key.stackId);
    }

    public boolean isBlurThumbnail() {
        TaskKey taskKey = this.key;
        return taskKey.isThumbnailBlur || this.isAccessLocked || taskKey.isScreening;
    }

    public boolean isCoverThumbnail() {
        return this.isAccessLocked || this.key.isScreening;
    }

    public void notifyTaskDataLoaded(Bitmap bitmap, Drawable drawable, ActivityManager$TaskThumbnailInfo activityManager$TaskThumbnailInfo) {
        this.icon = drawable;
        this.thumbnail = bitmap;
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mCallbacks.get(i).onTaskDataLoaded(this, activityManager$TaskThumbnailInfo);
        }
    }

    public void notifyTaskDataUnloaded(Bitmap bitmap, Drawable drawable) {
        this.icon = drawable;
        this.thumbnail = bitmap;
        for (int size = this.mCallbacks.size() - 1; size >= 0; size--) {
            this.mCallbacks.get(size).onTaskDataUnloaded();
        }
    }

    public ComponentName getTopComponent() {
        ComponentName componentName = this.topActivity;
        return componentName != null ? componentName : this.key.baseIntent.getComponent();
    }

    public boolean equals(Object obj) {
        return this.key.equals(((Task) obj).key);
    }

    public String toString() {
        return "[" + this.key.toString() + "] " + this.title;
    }

    public void dump(String str, PrintWriter printWriter) {
        printWriter.print(str);
        printWriter.print(this.key);
        if (!this.isDockable) {
            printWriter.print(" dockable=N");
        }
        if (this.isLaunchTarget) {
            printWriter.print(" launchTarget=Y");
        }
        if (isFreeformTask()) {
            printWriter.print(" freeform=Y");
        }
        printWriter.print(" ");
        printWriter.print(this.title);
        printWriter.println();
    }

    public boolean isProtected() {
        return this.isLocked || this.isLaunchTarget;
    }
}

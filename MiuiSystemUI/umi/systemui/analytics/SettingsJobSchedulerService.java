package com.android.systemui.analytics;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import com.android.systemui.Dependency;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.xiaomi.stat.d.r;
import java.util.List;

public class SettingsJobSchedulerService extends JobService {
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

    public static void schedule(Context context) {
        if (!isScheduled(context)) {
            JobInfo.Builder builder = new JobInfo.Builder(300002, new ComponentName(context, SettingsJobSchedulerService.class));
            builder.setPeriodic(r.a);
            builder.setPersisted(true);
            if (((JobScheduler) context.getSystemService(JobScheduler.class)).schedule(builder.build()) > 0) {
                Log.d("SettingsJobSchedulerService", "SettingsJobSchedulerService schedule success");
            }
        }
    }

    public static boolean isScheduled(Context context) {
        List<JobInfo> allPendingJobs = ((JobScheduler) context.getSystemService(JobScheduler.class)).getAllPendingJobs();
        if (allPendingJobs == null) {
            return false;
        }
        for (JobInfo id : allPendingJobs) {
            if (id.getId() == 300002) {
                return true;
            }
        }
        return false;
    }

    public boolean onStartJob(JobParameters jobParameters) {
        try {
            trackSettings();
            return false;
        } catch (Exception e) {
            Log.e("SettingsJobSchedulerService", "trackSettings exception", e);
            return false;
        }
    }

    private void trackSettings() {
        Class cls = SystemUIStat.class;
        ((SystemUIStat) Dependency.get(cls)).handleNotchEvent();
        ((SystemUIStat) Dependency.get(cls)).handleSettingsStatusEvent();
        ((SystemUIStat) Dependency.get(cls)).handlePhoneStatusEvent();
        ((SystemUIStat) Dependency.get(cls)).handleQSTileStateEvent();
        ((SystemUIStat) Dependency.get(cls)).handleToggleFullscreenSettingStateEvent();
    }
}

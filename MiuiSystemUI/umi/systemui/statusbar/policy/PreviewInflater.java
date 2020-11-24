package com.android.systemui.statusbar.policy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.ActivityIntentHelper;
import com.android.systemui.statusbar.phone.KeyguardPreviewContainer;
import java.util.List;

public class PreviewInflater {
    private final ActivityIntentHelper mActivityIntentHelper;
    private Context mContext;

    public PreviewInflater(Context context, LockPatternUtils lockPatternUtils, ActivityIntentHelper activityIntentHelper) {
        this.mContext = context;
        this.mActivityIntentHelper = activityIntentHelper;
    }

    public View inflatePreview(Intent intent) {
        return inflatePreview(getWidgetInfo(intent));
    }

    public View inflatePreviewFromService(ComponentName componentName) {
        return inflatePreview(getWidgetInfoFromService(componentName));
    }

    private KeyguardPreviewContainer inflatePreview(WidgetInfo widgetInfo) {
        View inflateWidgetView;
        if (widgetInfo == null || (inflateWidgetView = inflateWidgetView(widgetInfo)) == null) {
            return null;
        }
        KeyguardPreviewContainer keyguardPreviewContainer = new KeyguardPreviewContainer(this.mContext, (AttributeSet) null);
        keyguardPreviewContainer.addView(inflateWidgetView);
        return keyguardPreviewContainer;
    }

    private View inflateWidgetView(WidgetInfo widgetInfo) {
        try {
            Context createPackageContext = this.mContext.createPackageContext(widgetInfo.contextPackage, 4);
            return ((LayoutInflater) createPackageContext.getSystemService("layout_inflater")).cloneInContext(createPackageContext).inflate(widgetInfo.layoutId, (ViewGroup) null, false);
        } catch (PackageManager.NameNotFoundException | RuntimeException e) {
            Log.w("PreviewInflater", "Error creating widget view", e);
            return null;
        }
    }

    private WidgetInfo getWidgetInfoFromService(ComponentName componentName) {
        try {
            return getWidgetInfoFromMetaData(componentName.getPackageName(), this.mContext.getPackageManager().getServiceInfo(componentName, 128).metaData);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w("PreviewInflater", "Failed to load preview; " + componentName.flattenToShortString() + " not found", e);
            return null;
        }
    }

    private WidgetInfo getWidgetInfoFromMetaData(String str, Bundle bundle) {
        int i;
        if (bundle == null || (i = bundle.getInt("com.android.keyguard.layout")) == 0) {
            return null;
        }
        WidgetInfo widgetInfo = new WidgetInfo();
        widgetInfo.contextPackage = str;
        widgetInfo.layoutId = i;
        return widgetInfo;
    }

    private WidgetInfo getWidgetInfo(Intent intent) {
        ActivityInfo activityInfo;
        PackageManager packageManager = this.mContext.getPackageManager();
        List queryIntentActivitiesAsUser = packageManager.queryIntentActivitiesAsUser(intent, 851968, KeyguardUpdateMonitor.getCurrentUser());
        if (queryIntentActivitiesAsUser.size() == 0) {
            return null;
        }
        ResolveInfo resolveActivityAsUser = packageManager.resolveActivityAsUser(intent, 852096, KeyguardUpdateMonitor.getCurrentUser());
        if (this.mActivityIntentHelper.wouldLaunchResolverActivity(resolveActivityAsUser, (List<ResolveInfo>) queryIntentActivitiesAsUser) || resolveActivityAsUser == null || (activityInfo = resolveActivityAsUser.activityInfo) == null) {
            return null;
        }
        return getWidgetInfoFromMetaData(activityInfo.packageName, activityInfo.metaData);
    }

    private static class WidgetInfo {
        String contextPackage;
        int layoutId;

        private WidgetInfo() {
        }
    }
}

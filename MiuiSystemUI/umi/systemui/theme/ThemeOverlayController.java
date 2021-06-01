package com.android.systemui.theme;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.om.OverlayManager;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import com.android.systemui.C0021R$string;
import com.android.systemui.SystemUI;
import com.android.systemui.broadcast.BroadcastDispatcher;
import com.google.android.collect.Sets;
import java.util.Collection;
import java.util.HashSet;
import org.json.JSONException;
import org.json.JSONObject;

public class ThemeOverlayController extends SystemUI {
    private final Handler mBgHandler;
    private BroadcastDispatcher mBroadcastDispatcher;
    private ThemeOverlayManager mThemeManager;
    private UserManager mUserManager;

    public ThemeOverlayController(Context context, BroadcastDispatcher broadcastDispatcher, Handler handler) {
        super(context);
        this.mBroadcastDispatcher = broadcastDispatcher;
        this.mBgHandler = handler;
    }

    @Override // com.android.systemui.SystemUI
    public void start() {
        this.mUserManager = (UserManager) this.mContext.getSystemService(UserManager.class);
        this.mThemeManager = new ThemeOverlayManager((OverlayManager) this.mContext.getSystemService(OverlayManager.class), AsyncTask.THREAD_POOL_EXECUTOR, this.mContext.getString(C0021R$string.launcher_overlayable_package), this.mContext.getString(C0021R$string.themepicker_overlayable_package));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
        this.mBroadcastDispatcher.registerReceiverWithHandler(new BroadcastReceiver() {
            /* class com.android.systemui.theme.ThemeOverlayController.AnonymousClass1 */

            public void onReceive(Context context, Intent intent) {
                ThemeOverlayController.this.updateThemeOverlays();
            }
        }, intentFilter, this.mBgHandler, UserHandle.ALL);
        this.mContext.getContentResolver().registerContentObserver(Settings.Secure.getUriFor("theme_customization_overlay_packages"), false, new ContentObserver(this.mBgHandler) {
            /* class com.android.systemui.theme.ThemeOverlayController.AnonymousClass2 */

            public void onChange(boolean z, Collection<Uri> collection, int i, int i2) {
                if (ActivityManager.getCurrentUser() == i2) {
                    ThemeOverlayController.this.updateThemeOverlays();
                }
            }
        }, -1);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateThemeOverlays() {
        int currentUser = ActivityManager.getCurrentUser();
        String stringForUser = Settings.Secure.getStringForUser(this.mContext.getContentResolver(), "theme_customization_overlay_packages", currentUser);
        ArrayMap arrayMap = new ArrayMap();
        if (!TextUtils.isEmpty(stringForUser)) {
            try {
                JSONObject jSONObject = new JSONObject(stringForUser);
                for (String str : ThemeOverlayManager.THEME_CATEGORIES) {
                    if (jSONObject.has(str)) {
                        arrayMap.put(str, jSONObject.getString(str));
                    }
                }
            } catch (JSONException e) {
                Log.i("ThemeOverlayController", "Failed to parse THEME_CUSTOMIZATION_OVERLAY_PACKAGES.", e);
            }
        }
        HashSet newHashSet = Sets.newHashSet(new UserHandle[]{UserHandle.of(currentUser)});
        for (UserInfo userInfo : this.mUserManager.getEnabledProfiles(currentUser)) {
            if (userInfo.isManagedProfile()) {
                newHashSet.add(userInfo.getUserHandle());
            }
        }
        this.mThemeManager.applyCurrentUserOverlays(arrayMap, newHashSet);
    }
}

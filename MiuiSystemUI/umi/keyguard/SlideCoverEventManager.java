package com.android.keyguard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.faceunlock.FaceUnlockManager;
import com.android.keyguard.faceunlock.MiuiFaceUnlockUtils;
import com.android.keyguard.utils.PackageUtils;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.StatusBar;
import com.miui.systemui.annotation.Inject;

public class SlideCoverEventManager {
    private static volatile SlideCoverEventManager sInstance;
    /* access modifiers changed from: private */
    public Context mContext;
    /* access modifiers changed from: private */
    public FaceUnlockManager mFaceUnlockManager;
    /* access modifiers changed from: private */
    public String mLaunchPkg;
    /* access modifiers changed from: private */
    public int mSCEventStatus = 0;
    /* access modifiers changed from: private */
    public int mSCStatus = 0;
    private ContentObserver mSCStatusProviderObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            Intent launchIntentForPackage;
            SlideCoverEventManager slideCoverEventManager = SlideCoverEventManager.this;
            int unused = slideCoverEventManager.mSCEventStatus = Settings.System.getIntForUser(slideCoverEventManager.mContext.getContentResolver(), "sc_event_status", 0, 0);
            if (SlideCoverEventManager.this.mSCEventStatus != 2) {
                SlideCoverEventManager slideCoverEventManager2 = SlideCoverEventManager.this;
                int unused2 = slideCoverEventManager2.mSCStatus = Settings.System.getIntForUser(slideCoverEventManager2.mContext.getContentResolver(), "sc_status", 0, 0);
                if (SlideCoverEventManager.this.mSCStatus != 0 || z) {
                    if (SlideCoverEventManager.this.mSCStatus == 1) {
                        SlideCoverEventManager.this.mFaceUnlockManager.stopFaceUnlock();
                    }
                } else if (SlideCoverEventManager.this.mFaceUnlockManager.shouldListenForFaceUnlock()) {
                    SlideCoverEventManager.this.mFaceUnlockManager.startFaceUnlock();
                } else if ((SlideCoverEventManager.this.mUpdateMonitor.isKeyguardOccluded() && !MiuiFaceUnlockUtils.isSCSlideNotOpenCamera(SlideCoverEventManager.this.mContext)) || !SlideCoverEventManager.this.mUpdateMonitor.isKeyguardOccluded()) {
                    if (SlideCoverEventManager.this.mSlideChoice == 1) {
                        Intent cameraIntent = PackageUtils.getCameraIntent();
                        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
                        cameraIntent.putExtra("autofocus", true);
                        cameraIntent.putExtra("fullScreen", false);
                        cameraIntent.putExtra("showActionIcons", false);
                        SlideCoverEventManager.this.mContext.startActivityAsUser(cameraIntent, UserHandle.CURRENT);
                    } else if (SlideCoverEventManager.this.mSlideChoice == 2) {
                        Intent intent = new Intent();
                        intent.setFlags(276824064);
                        intent.putExtra("StartActivityWhenLocked", true);
                        intent.setComponent(new ComponentName("com.android.systemui", "com.android.systemui.sliderpanel.SliderPanelActivity"));
                        intent.putExtra("onKeyguard", true);
                        intent.putExtra("blurColor", KeyguardUpdateMonitor.getInstance(SlideCoverEventManager.this.mContext).getWallpaperBlurColor());
                        SlideCoverEventManager.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                    } else if (SlideCoverEventManager.this.mSlideChoice == 3) {
                        SlideCoverEventManager slideCoverEventManager3 = SlideCoverEventManager.this;
                        String unused3 = slideCoverEventManager3.mLaunchPkg = Settings.System.getStringForUser(slideCoverEventManager3.mContext.getContentResolver(), "miui_slider_launch_pkg", KeyguardUpdateMonitor.getCurrentUser());
                        if (SlideCoverEventManager.this.mLaunchPkg != null && (launchIntentForPackage = SlideCoverEventManager.this.mContext.getPackageManager().getLaunchIntentForPackage(SlideCoverEventManager.this.mLaunchPkg)) != null) {
                            launchIntentForPackage.setFlags(276824064);
                            SlideCoverEventManager.this.mStatusBar.startActivity(launchIntentForPackage, true);
                        }
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public int mSlideChoice = 0;
    /* access modifiers changed from: private */
    public StatusBar mStatusBar;
    /* access modifiers changed from: private */
    public KeyguardUpdateMonitor mUpdateMonitor;

    public static SlideCoverEventManager getInstance() {
        if (sInstance == null) {
            synchronized (SlideCoverEventManager.class) {
                if (sInstance == null) {
                    sInstance = (SlideCoverEventManager) Dependency.get(SlideCoverEventManager.class);
                }
            }
        }
        return sInstance;
    }

    public SlideCoverEventManager(@Inject Context context) {
        this.mContext = context;
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(context);
        this.mFaceUnlockManager = FaceUnlockManager.getInstance();
    }

    public void setStatusBar(StatusBar statusBar) {
        this.mStatusBar = statusBar;
    }

    public void updateShowingStatus(boolean z) {
        Log.d("miui_face", "updateShowingStatus showing=" + z);
        updateSCStatusContentObserver(z);
    }

    private void updateSCStatusContentObserver(boolean z) {
        if (!z || !this.mUpdateMonitor.getStrongAuthTracker().hasUserAuthenticatedSinceBoot()) {
            this.mContext.getContentResolver().unregisterContentObserver(this.mSCStatusProviderObserver);
            return;
        }
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("sc_event_status"), false, this.mSCStatusProviderObserver, -1);
        this.mSCStatusProviderObserver.onChange(true);
        this.mSlideChoice = Settings.System.getIntForUser(this.mContext.getContentResolver(), "miui_slider_tool_choice", 1, KeyguardUpdateMonitor.getCurrentUser());
    }
}

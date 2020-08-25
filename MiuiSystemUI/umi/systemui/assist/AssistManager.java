package com.android.systemui.assist;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.ConfigurationChangedReceiver;
import com.android.systemui.Dependency;
import com.android.systemui.SystemUI;
import com.android.systemui.assist.ui.DefaultUiController;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.OverviewProxyService;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.policy.DeviceProvisionedController;
import com.android.systemui.util.InterestingConfigChanges;
import com.miui.systemui.annotation.Inject;
import com.xiaomi.stat.MiStat;

public class AssistManager implements ConfigurationChangedReceiver {
    private final AssistDisclosure mAssistDisclosure;
    protected final AssistUtils mAssistUtils;
    protected final Context mContext;
    private final DeviceProvisionedController mDeviceProvisionedController;
    /* access modifiers changed from: private */
    public Runnable mHideRunnable = new Runnable() {
        public void run() {
            AssistManager.this.mView.removeCallbacks(this);
            AssistManager.this.mView.show(false, true);
        }
    };
    private final InterestingConfigChanges mInterestingConfigChanges;
    private boolean mNeedUpdate;
    private IVoiceInteractionSessionShowCallback mShowCallback = new IVoiceInteractionSessionShowCallback.Stub() {
        public void onFailed() throws RemoteException {
            AssistManager.this.mView.post(AssistManager.this.mHideRunnable);
        }

        public void onShown() throws RemoteException {
            AssistManager.this.mView.post(AssistManager.this.mHideRunnable);
        }
    };
    private final UiController mUiController;
    /* access modifiers changed from: private */
    public AssistOrbContainer mView;
    private final WindowManager mWindowManager;

    public interface UiController {
        void onGestureCompletion(float f);

        void onInvocationProgress(int i, float f);
    }

    /* access modifiers changed from: protected */
    public boolean shouldShowOrb() {
        return false;
    }

    public AssistManager(@Inject DeviceProvisionedController deviceProvisionedController, @Inject Context context) {
        this.mContext = context;
        this.mDeviceProvisionedController = deviceProvisionedController;
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mAssistUtils = new AssistUtils(context);
        this.mAssistDisclosure = new AssistDisclosure(context, new Handler());
        this.mInterestingConfigChanges = new InterestingConfigChanges(-2147482748);
        onConfigurationChanged(context.getResources().getConfiguration());
        this.mUiController = new DefaultUiController(this.mContext);
        ((OverviewProxyService) Dependency.get(OverviewProxyService.class)).addCallback((OverviewProxyService.OverviewProxyListener) new OverviewProxyService.OverviewProxyListener() {
            public void onBackButtonAlphaChanged(float f, boolean z) {
            }

            public void onConnectionChanged(boolean z) {
            }

            public void onOverviewShown(boolean z) {
            }

            public void onAssistantProgress(float f) {
                AssistManager.this.onInvocationProgress(1, f);
            }

            public void startAssistant(Bundle bundle) {
                AssistManager.this.startAssist(bundle);
            }

            public void completeAssistant() {
                AssistManager.this.onAssistantGestureCompletion();
            }
        });
    }

    public void onConfigurationChanged(Configuration configuration) {
        if (this.mInterestingConfigChanges.applyNewConfig(this.mContext.getResources())) {
            boolean z = false;
            AssistOrbContainer assistOrbContainer = this.mView;
            if (assistOrbContainer != null) {
                z = assistOrbContainer.isShowing();
            }
            if (z || this.mView == null) {
                update();
            } else {
                this.mNeedUpdate = true;
            }
        }
    }

    private void update() {
        boolean z;
        AssistOrbContainer assistOrbContainer = this.mView;
        if (assistOrbContainer != null) {
            z = assistOrbContainer.isShowing();
            this.mWindowManager.removeView(this.mView);
        } else {
            z = false;
        }
        AssistOrbContainer assistOrbContainer2 = (AssistOrbContainer) LayoutInflater.from(this.mContext).inflate(R.layout.assist_orb, (ViewGroup) null);
        this.mView = assistOrbContainer2;
        assistOrbContainer2.setVisibility(8);
        this.mView.setSystemUiVisibility(1792);
        this.mWindowManager.addView(this.mView, getLayoutParams());
        if (z) {
            this.mView.show(true, false);
        }
        this.mNeedUpdate = false;
    }

    public void onInvocationProgress(int i, float f) {
        this.mUiController.onInvocationProgress(i, f);
    }

    public void onAssistantGestureCompletion() {
        this.mUiController.onGestureCompletion(0.0f);
    }

    public void startAssist(Bundle bundle) {
        ComponentName assistInfo = getAssistInfo();
        if (assistInfo != null) {
            boolean equals = assistInfo.equals(getVoiceInteractorComponentName());
            if (!equals || (!isVoiceSessionRunning() && shouldShowOrb())) {
                showOrb(assistInfo, equals);
                this.mView.postDelayed(this.mHideRunnable, equals ? 2500 : 1000);
            }
            startAssistInternal(bundle, assistInfo, equals);
        }
    }

    public void hideAssist() {
        this.mAssistUtils.hideCurrentSession();
    }

    private WindowManager.LayoutParams getLayoutParams() {
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-1, this.mContext.getResources().getDimensionPixelSize(R.dimen.assist_orb_scrim_height), 2033, 280, -3);
        layoutParams.token = new Binder();
        if (ActivityManager.isHighEndGfx()) {
            layoutParams.flags |= 16777216;
        }
        layoutParams.gravity = 8388691;
        layoutParams.setTitle("AssistPreviewPanel");
        layoutParams.softInputMode = 49;
        return layoutParams;
    }

    private void showOrb(ComponentName componentName, boolean z) {
        if (this.mNeedUpdate) {
            update();
        }
        maybeSwapSearchIcon(componentName, z);
        this.mView.show(true, true);
    }

    private void startAssistInternal(Bundle bundle, ComponentName componentName, boolean z) {
        if (z) {
            startVoiceInteractor(bundle);
        } else {
            startAssistActivity(bundle, componentName);
        }
    }

    private void startAssistActivity(Bundle bundle, ComponentName componentName) {
        if (this.mDeviceProvisionedController.isDeviceProvisioned()) {
            ((CommandQueue) SystemUI.getComponent(this.mContext, CommandQueue.class)).animateCollapsePanels(3);
            boolean z = true;
            if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "assist_structure_enabled", 1, KeyguardUpdateMonitor.getCurrentUser()) == 0) {
                z = false;
            }
            final Intent assistIntent = ((SearchManager) this.mContext.getSystemService(MiStat.Event.SEARCH)).getAssistIntent(z);
            if (assistIntent != null) {
                assistIntent.setComponent(componentName);
                assistIntent.putExtras(bundle);
                if (z) {
                    showDisclosure();
                }
                try {
                    final ActivityOptions makeCustomAnimation = ActivityOptions.makeCustomAnimation(this.mContext, R.anim.search_launch_enter, R.anim.search_launch_exit);
                    assistIntent.addFlags(268435456);
                    AsyncTask.execute(new Runnable() {
                        public void run() {
                            AssistManager.this.mContext.startActivityAsUser(assistIntent, makeCustomAnimation.toBundle(), new UserHandle(-2));
                        }
                    });
                } catch (ActivityNotFoundException unused) {
                    Log.w("AssistManager", "Activity not found for " + assistIntent.getAction());
                }
            }
        }
    }

    private void startVoiceInteractor(Bundle bundle) {
        try {
            this.mAssistUtils.showSessionForActiveService(bundle, 4, this.mShowCallback, (IBinder) null);
        } catch (Exception e) {
            Log.e("AssistManager", "Failed to startVoiceInteractor", e);
        }
    }

    public ComponentName getVoiceInteractorComponentName() {
        return this.mAssistUtils.getActiveServiceComponentName();
    }

    private boolean isVoiceSessionRunning() {
        return this.mAssistUtils.isSessionRunning();
    }

    private void maybeSwapSearchIcon(ComponentName componentName, boolean z) {
        replaceDrawable(this.mView.getOrb().getLogo(), componentName, "com.android.systemui.action_assist_icon", z);
    }

    public void replaceDrawable(ImageView imageView, ComponentName componentName, String str, boolean z) {
        Bundle bundle;
        int i;
        if (componentName != null) {
            try {
                PackageManager packageManager = this.mContext.getPackageManager();
                if (z) {
                    bundle = packageManager.getServiceInfo(componentName, 128).metaData;
                } else {
                    bundle = packageManager.getActivityInfo(componentName, 128).metaData;
                }
                if (!(bundle == null || (i = bundle.getInt(str)) == 0)) {
                    imageView.setImageDrawable(packageManager.getResourcesForApplication(componentName.getPackageName()).getDrawable(i));
                    return;
                }
            } catch (PackageManager.NameNotFoundException unused) {
                Log.v("AssistManager", "Assistant component " + componentName.flattenToShortString() + " not found");
            } catch (Resources.NotFoundException e) {
                Log.w("AssistManager", "Failed to swap drawable from " + componentName.flattenToShortString(), e);
            }
        }
        imageView.setImageDrawable((Drawable) null);
    }

    public boolean isSupportGoogleAssist(int i) {
        ComponentName assistInfoForUser = getAssistInfoForUser(i);
        return assistInfoForUser != null && "com.google.android.googlequicksearchbox".equals(assistInfoForUser.getPackageName());
    }

    public ComponentName getAssistInfoForUser(int i) {
        return this.mAssistUtils.getAssistComponentForUser(i);
    }

    private ComponentName getAssistInfo() {
        return getAssistInfoForUser(KeyguardUpdateMonitor.getCurrentUser());
    }

    public void showDisclosure() {
        this.mAssistDisclosure.postShow();
    }

    public void onLockscreenShown() {
        this.mAssistUtils.onLockscreenShown();
    }
}

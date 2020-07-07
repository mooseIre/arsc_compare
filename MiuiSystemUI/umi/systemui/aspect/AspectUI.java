package com.android.systemui.aspect;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContextCompat;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.MiuiSettings;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.IWindowManagerCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManagerCompat;
import android.view.WindowManagerGlobal;
import com.android.internal.os.SomeArgs;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.systemui.Application;
import com.android.systemui.Dependency;
import com.android.systemui.DisplayCutoutCompat;
import com.android.systemui.SystemUI;
import com.android.systemui.events.AspectClickEvent;
import com.android.systemui.miui.ActivityObserver;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.events.RecentsEventBus;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.phone.StatusBar;
import com.android.systemui.statusbar.policy.ConfigurationController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import miui.R$style;
import miui.os.Build;
import miui.os.MiuiInit;

public class AspectUI extends SystemUI implements ConfigurationController.ConfigurationListener, CommandQueue.Callbacks {
    private final ActivityObserver.ActivityObserverCallback mActivityStateObserver = new ActivityObserver.ActivityObserverCallback() {
        public void activityResumed(Intent intent) {
            AspectUI.this.mHandler.removeMessages(1);
            AspectUI.this.mHandler.obtainMessage(1, 0, 0, intent).sendToTarget();
        }
    };
    /* access modifiers changed from: private */
    public AlertDialog mAspectDialog;
    /* access modifiers changed from: private */
    public String mAspectPkg;
    private View mAspectView;
    /* access modifiers changed from: private */
    public Point mCurrentSize;
    /* access modifiers changed from: private */
    public boolean mDeviceRatioEqualAspectRatio;
    /* access modifiers changed from: private */
    public Display mDisplay;
    private ContentObserver mFullScreenGestureListener = new ContentObserver(this.mHandler) {
        public void onChange(boolean z) {
            AspectUI aspectUI = AspectUI.this;
            boolean unused = aspectUI.mIsFsgMode = MiuiSettings.Global.getBoolean(aspectUI.mContext.getContentResolver(), "force_fsg_nav_bar");
            AspectUI.this.updateAspectVisibility();
        }
    };
    /* access modifiers changed from: private */
    public H mHandler = new H();
    /* access modifiers changed from: private */
    public final DisplayInfo mInfo = new DisplayInfo();
    /* access modifiers changed from: private */
    public boolean mIsFsgMode;
    private Configuration mLastConfiguration;
    private int mMinWindowHeight;
    /* access modifiers changed from: private */
    public int mRotation;
    /* access modifiers changed from: private */
    public boolean mShowAspect;
    private boolean mSoftInputVisible;
    Runnable mUpdateAspectRunnable = new Runnable() {
        public void run() {
            AspectUI aspectUI = AspectUI.this;
            aspectUI.setAspectVisibility(aspectUI.mShowAspect, AspectUI.this.mAspectPkg);
        }
    };
    private int mWindowHeight;
    private WindowManager mWindowManager;

    public void addQsTile(ComponentName componentName) {
    }

    public void animateCollapsePanels(int i) {
    }

    public void animateExpandNotificationsPanel() {
    }

    public void animateExpandSettingsPanel(String str) {
    }

    public void appTransitionCancelled() {
    }

    public void appTransitionFinished() {
    }

    public void appTransitionPending(boolean z) {
    }

    public void appTransitionStarting(long j, long j2, boolean z) {
    }

    public void cancelPreloadRecentApps() {
    }

    public void clickTile(ComponentName componentName) {
    }

    public void disable(int i, int i2, boolean z) {
    }

    public void dismissKeyboardShortcutsMenu() {
    }

    public void handleShowGlobalActionsMenu() {
    }

    public void handleSystemNavigationKey(int i) {
    }

    public void hideFingerprintDialog() {
    }

    public void hideRecentApps(boolean z, boolean z2) {
    }

    public void onFingerprintAuthenticated() {
    }

    public void onFingerprintError(String str) {
    }

    public void onFingerprintHelp(String str) {
    }

    public void preloadRecentApps() {
    }

    public void remQsTile(ComponentName componentName) {
    }

    public void removeIcon(String str) {
    }

    public void setIcon(String str, StatusBarIcon statusBarIcon) {
    }

    public void setStatus(int i, String str, Bundle bundle) {
    }

    public void setSystemUiVisibility(int i, int i2, int i3, int i4, Rect rect, Rect rect2) {
    }

    public void setWindowState(int i, int i2) {
    }

    public void showAssistDisclosure() {
    }

    public void showFingerprintDialog(SomeArgs someArgs) {
    }

    public void showPictureInPictureMenu() {
    }

    public void showRecentApps(boolean z, boolean z2) {
    }

    public void showScreenPinningRequest(int i) {
    }

    public void startAssist(Bundle bundle) {
    }

    public void toggleKeyboardShortcutsMenu(int i) {
    }

    public void toggleRecentApps() {
    }

    public void toggleSplitScreen() {
    }

    public void topAppWindowChanged(boolean z) {
    }

    public void setImeWindowStatus(IBinder iBinder, int i, int i2, boolean z) {
        this.mSoftInputVisible = (i & 2) != 0;
        updateAspectVisibility();
    }

    private class H extends Handler {
        private H() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:9:0x005d, code lost:
            if (((int) (((double) (((float) r3) * 1.86f)) + 0.5d)) == r8) goto L_0x0061;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void handleMessage(android.os.Message r8) {
            /*
                r7 = this;
                int r0 = r8.what
                r1 = 1
                if (r0 == r1) goto L_0x0006
                goto L_0x0066
            L_0x0006:
                java.lang.Object r8 = r8.obj
                android.content.Intent r8 = (android.content.Intent) r8
                android.content.ComponentName r0 = r8.getComponent()
                java.lang.String r0 = r0.getPackageName()
                java.lang.String r2 = "appBounds"
                android.os.Parcelable r8 = r8.getParcelableExtra(r2)
                android.graphics.Rect r8 = (android.graphics.Rect) r8
                r2 = 0
                if (r8 == 0) goto L_0x0060
                int r3 = r8.width()
                int r4 = r8.height()
                int r3 = java.lang.Math.min(r3, r4)
                int r4 = r8.width()
                int r8 = r8.height()
                int r8 = java.lang.Math.max(r4, r8)
                com.android.systemui.aspect.AspectUI r4 = com.android.systemui.aspect.AspectUI.this
                android.graphics.Point r4 = r4.mCurrentSize
                int r4 = r4.x
                com.android.systemui.aspect.AspectUI r5 = com.android.systemui.aspect.AspectUI.this
                android.graphics.Point r5 = r5.mCurrentSize
                int r5 = r5.y
                int r4 = java.lang.Math.min(r4, r5)
                if (r4 != r3) goto L_0x0060
                com.android.systemui.aspect.AspectUI r4 = com.android.systemui.aspect.AspectUI.this
                boolean r4 = r4.mDeviceRatioEqualAspectRatio
                if (r4 != 0) goto L_0x0060
                float r3 = (float) r3
                r4 = 1072567419(0x3fee147b, float:1.86)
                float r3 = r3 * r4
                double r3 = (double) r3
                r5 = 4602678819172646912(0x3fe0000000000000, double:0.5)
                double r3 = r3 + r5
                int r3 = (int) r3
                if (r3 != r8) goto L_0x0060
                goto L_0x0061
            L_0x0060:
                r1 = r2
            L_0x0061:
                com.android.systemui.aspect.AspectUI r7 = com.android.systemui.aspect.AspectUI.this
                r7.setAspectVisibility(r1, r0)
            L_0x0066:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.aspect.AspectUI.H.handleMessage(android.os.Message):void");
        }
    }

    public void start() {
        boolean z;
        Log.d("AspectUI", "start AspectUI");
        try {
            z = IWindowManagerCompat.hasNavigationBar(WindowManagerGlobal.getWindowManagerService(), ContextCompat.getDisplayId(this.mContext));
        } catch (RemoteException e) {
            e.printStackTrace();
            z = false;
        }
        if (z && !Build.IS_TABLET && !"lithium".equals(android.os.Build.DEVICE)) {
            updateResource();
            this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
            this.mDisplay = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay();
            this.mDisplay.getDisplayInfo(this.mInfo);
            this.mRotation = this.mDisplay.getRotation();
            this.mCurrentSize = new Point();
            this.mDisplay.getRealSize(this.mCurrentSize);
            addAspectWindow();
            ((ActivityObserver) Dependency.get(ActivityObserver.class)).addCallback(this.mActivityStateObserver);
            RecentsEventBus.getDefault().register(this);
            this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("force_fsg_nav_bar"), false, this.mFullScreenGestureListener);
            this.mFullScreenGestureListener.onChange(false);
            ((DisplayManager) this.mContext.getSystemService("display")).registerDisplayListener(new DisplayManager.DisplayListener() {
                public void onDisplayAdded(int i) {
                }

                public void onDisplayRemoved(int i) {
                }

                public void onDisplayChanged(int i) {
                    int rotation = AspectUI.this.mDisplay.getRotation();
                    AspectUI.this.mDisplay.getDisplayInfo(AspectUI.this.mInfo);
                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    AspectUI.this.mDisplay.getMetrics(displayMetrics);
                    boolean z = false;
                    boolean z2 = ((int) (((double) (((float) Math.min(displayMetrics.heightPixels, displayMetrics.widthPixels)) * 1.86f)) + 0.5d)) == Math.max(displayMetrics.heightPixels, displayMetrics.widthPixels);
                    if (AspectUI.this.mRotation != rotation || AspectUI.this.mDeviceRatioEqualAspectRatio != z2) {
                        int unused = AspectUI.this.mRotation = rotation;
                        boolean unused2 = AspectUI.this.mDeviceRatioEqualAspectRatio = z2;
                        AspectUI aspectUI = AspectUI.this;
                        if (aspectUI.mShowAspect && !AspectUI.this.mDeviceRatioEqualAspectRatio) {
                            z = true;
                        }
                        boolean unused3 = aspectUI.mShowAspect = z;
                        Message obtain = Message.obtain(AspectUI.this.mHandler, AspectUI.this.mUpdateAspectRunnable);
                        obtain.setAsynchronous(true);
                        AspectUI.this.mHandler.sendMessageAtFrontOfQueue(obtain);
                    }
                }
            }, this.mHandler);
            this.mLastConfiguration = new Configuration();
            this.mLastConfiguration.updateFrom(this.mContext.getResources().getConfiguration());
            ((ConfigurationController) Dependency.get(ConfigurationController.class)).addCallback(this);
            ((CommandQueue) SystemUI.getComponent(this.mContext, CommandQueue.class)).addCallbacks(this);
        }
    }

    private View addAspectWindow() {
        View inflate = LayoutInflater.from(this.mContext).inflate(R.layout.aspect_window, (ViewGroup) null);
        this.mWindowHeight = getWindowHeight();
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(-2, this.mWindowHeight, 2003, 296, -2);
        WindowManagerCompat.setLayoutInDisplayCutoutMode(layoutParams, 1);
        layoutParams.setTitle("Aspect");
        layoutParams.gravity = 80;
        layoutParams.privateFlags |= 16;
        this.mWindowManager.addView(inflate, layoutParams);
        inflate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AspectUI.this.showAspectDialog();
            }
        });
        this.mAspectView = inflate;
        updateAspectVisibility();
        return inflate;
    }

    private int getWindowHeight() {
        Point point = new Point();
        this.mContext.getDisplay().getRealSize(point);
        return (Math.max(point.x, point.y) - DisplayCutoutCompat.getHeight(this.mInfo)) - ((int) (((double) (((float) Math.min(point.x, point.y)) * 1.86f)) + 0.5d));
    }

    /* access modifiers changed from: private */
    public void setAspectVisibility(boolean z, String str) {
        AlertDialog alertDialog;
        if (this.mShowAspect != z || !TextUtils.equals(str, this.mAspectPkg)) {
            this.mShowAspect = z;
            this.mAspectPkg = str;
            StatusBar statusBar = (StatusBar) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
            if (!(statusBar == null || statusBar.getNavigationBarView() == null)) {
                statusBar.getNavigationBarView().setAspectVisibility(this.mShowAspect);
            }
            updateAspectVisibility();
            if (!this.mShowAspect && (alertDialog = this.mAspectDialog) != null) {
                alertDialog.dismiss();
                this.mAspectDialog = null;
            }
        }
    }

    /* access modifiers changed from: private */
    public void updateAspectVisibility() {
        boolean z = true;
        int i = 0;
        boolean z2 = this.mRotation == 0 && this.mIsFsgMode && this.mShowAspect && this.mWindowHeight > this.mMinWindowHeight && !this.mSoftInputVisible;
        if (this.mAspectView.getVisibility() != 0) {
            z = false;
        }
        if (z != z2) {
            View view = this.mAspectView;
            if (!z2) {
                i = 8;
            }
            view.setVisibility(i);
        }
    }

    /* access modifiers changed from: package-private */
    public void showAspectDialog() {
        if (this.mAspectDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext, R$style.Theme_Light_Dialog_Alert);
            builder.setCancelable(true);
            builder.setTitle(R.string.aspect_title);
            builder.setMessage(R.string.aspect_message);
            builder.setIconAttribute(16843605);
            builder.setPositiveButton(R.string.aspect_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    MiuiInit.setRestrictAspect(AspectUI.this.mAspectPkg, false);
                    Intent launchIntentForPackage = AspectUI.this.mContext.getPackageManager().getLaunchIntentForPackage(AspectUI.this.mAspectPkg);
                    if (launchIntentForPackage != null) {
                        AspectUI.this.mContext.startActivityAsUser(launchIntentForPackage, UserHandle.CURRENT);
                    }
                }
            });
            builder.setNegativeButton(R.string.aspect_cancel, (DialogInterface.OnClickListener) null);
            AlertDialog create = builder.create();
            create.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    AlertDialog unused = AspectUI.this.mAspectDialog = null;
                }
            });
            create.getWindow().setType(2003);
            create.getWindow().addPrivateFlags(16);
            create.show();
            this.mAspectDialog = create;
        }
    }

    private void updateResource() {
        this.mMinWindowHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.min_aspect_window_height);
    }

    private void reCreateAspectWindow() {
        View view = this.mAspectView;
        if (view != null) {
            this.mWindowManager.removeView(view);
            addAspectWindow();
        }
    }

    public void onConfigChanged(Configuration configuration) {
        updateResource();
        if ((this.mLastConfiguration.updateFrom(configuration) & Integer.MIN_VALUE) != 0) {
            Log.d("AspectUI", "recreate when assets change");
            this.mDisplay.getDisplayInfo(this.mInfo);
            reCreateAspectWindow();
        }
    }

    public void onDensityOrFontScaleChanged() {
        Log.d("AspectUI", "recreate when density change");
        this.mDisplay.getRealSize(this.mCurrentSize);
        reCreateAspectWindow();
    }

    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(fileDescriptor, printWriter, strArr);
        printWriter.print("  mAspectPkg=");
        printWriter.println(this.mAspectPkg);
        printWriter.print("  mShowAspect=");
        printWriter.println(this.mShowAspect);
        printWriter.print("  mMinWindowHeight=");
        printWriter.println(this.mMinWindowHeight);
        printWriter.print("  mWindowHeight=");
        printWriter.println(this.mWindowHeight);
        printWriter.print("  mRotation=");
        printWriter.println(this.mRotation);
        printWriter.print("  mIsFsgMode=");
        printWriter.println(this.mIsFsgMode);
        printWriter.print("  mSoftInputVisible=");
        printWriter.println(this.mSoftInputVisible);
    }

    public final void onBusEvent(AspectClickEvent aspectClickEvent) {
        showAspectDialog();
        StatusBar statusBar = (StatusBar) ((Application) this.mContext.getApplicationContext()).getSystemUIApplication().getComponent(StatusBar.class);
        if (statusBar != null) {
            statusBar.animateCollapsePanels();
        }
    }
}

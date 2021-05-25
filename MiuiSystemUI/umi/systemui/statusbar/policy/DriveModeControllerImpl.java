package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import com.android.systemui.statusbar.policy.DriveModeController;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executor;
import kotlin.Unit;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: DriveModeControllerImpl.kt */
public final class DriveModeControllerImpl implements DriveModeController {
    private final Handler mBgHandler;
    private final Context mContext;
    private ContentObserver mDriveModeObserver;
    private int mDriveModeValue;
    private volatile boolean mIsDriveModeAvailable;
    private final ArrayList<DriveModeController.DriveModeListener> mListeners = new ArrayList<>();
    private BroadcastReceiver mPackageChangeReceiver;
    private final ContentResolver mResolver;
    private final Executor mUIExecutor;

    public DriveModeControllerImpl(@NotNull Context context, @Nullable Looper looper, @NotNull Executor executor, @NotNull Executor executor2) {
        Intrinsics.checkParameterIsNotNull(context, "mContext");
        Intrinsics.checkParameterIsNotNull(executor, "bgExecutor");
        Intrinsics.checkParameterIsNotNull(executor2, "uiExecutor");
        this.mContext = context;
        ContentResolver contentResolver = context.getContentResolver();
        Intrinsics.checkExpressionValueIsNotNull(contentResolver, "mContext.contentResolver");
        this.mResolver = contentResolver;
        updateDriveModeValue();
        if (looper != null) {
            this.mBgHandler = new Handler(looper);
            this.mUIExecutor = executor2;
            executor.execute(new Runnable(this) {
                /* class com.android.systemui.statusbar.policy.DriveModeControllerImpl.AnonymousClass1 */
                final /* synthetic */ DriveModeControllerImpl this$0;

                {
                    this.this$0 = r1;
                }

                public final void run() {
                    DriveModeControllerImpl driveModeControllerImpl = this.this$0;
                    boolean z = false;
                    try {
                        driveModeControllerImpl.mContext.getPackageManager().getApplicationInfo("com.xiaomi.drivemode", 0);
                        z = true;
                    } catch (PackageManager.NameNotFoundException unused) {
                        Log.d("DriveModeController", "Drive app not exist.");
                    }
                    driveModeControllerImpl.mIsDriveModeAvailable = z;
                }
            });
            observe();
            return;
        }
        Intrinsics.throwNpe();
        throw null;
    }

    static {
        Log.isLoggable("DriveModeController", 3);
    }

    /* access modifiers changed from: private */
    public final void updateDriveModeValue() {
        this.mDriveModeValue = Settings.System.getIntForUser(this.mResolver, "drive_mode_drive_mode", -1, -2);
    }

    private final void observe() {
        this.mDriveModeObserver = new DriveModeControllerImpl$observe$1(this, this.mBgHandler);
        ContentResolver contentResolver = this.mResolver;
        Uri uriFor = Settings.System.getUriFor("drive_mode_drive_mode");
        ContentObserver contentObserver = this.mDriveModeObserver;
        if (contentObserver != null) {
            contentResolver.registerContentObserver(uriFor, false, contentObserver, -1);
            this.mPackageChangeReceiver = new DriveModeControllerImpl$observe$2(this);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("android.intent.action.PACKAGE_ADDED");
            intentFilter.addAction("android.intent.action.PACKAGE_REMOVED");
            intentFilter.addDataScheme("package");
            Context context = this.mContext;
            BroadcastReceiver broadcastReceiver = this.mPackageChangeReceiver;
            if (broadcastReceiver != null) {
                context.registerReceiverAsUser(broadcastReceiver, UserHandle.ALL, intentFilter, null, null);
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("mPackageChangeReceiver");
                throw null;
            }
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("mDriveModeObserver");
            throw null;
        }
    }

    /* access modifiers changed from: private */
    public final void dispatchOnDriveModeChanged() {
        synchronized (this.mListeners) {
            Iterator<DriveModeController.DriveModeListener> it = this.mListeners.iterator();
            while (it.hasNext()) {
                it.next().onDriveModeChanged();
            }
            Unit unit = Unit.INSTANCE;
        }
    }

    @Override // com.android.systemui.statusbar.policy.DriveModeController
    public boolean isMiuiLabDriveModeOn() {
        return this.mDriveModeValue != -1;
    }

    @Override // com.android.systemui.statusbar.policy.DriveModeController
    public boolean isDriveModeAvailable() {
        return this.mIsDriveModeAvailable;
    }

    @Override // com.android.systemui.statusbar.policy.DriveModeController
    public boolean isDriveModeEnabled() {
        return this.mDriveModeValue > 0;
    }

    @Override // com.android.systemui.statusbar.policy.DriveModeController
    public void setDriveModeEnabled(boolean z) {
        Settings.System.putIntForUser(this.mResolver, "drive_mode_drive_mode", z ? 1 : 0, -2);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(@NotNull FileDescriptor fileDescriptor, @NotNull PrintWriter printWriter, @NotNull String[] strArr) {
        Intrinsics.checkParameterIsNotNull(fileDescriptor, "fd");
        Intrinsics.checkParameterIsNotNull(printWriter, "pw");
        Intrinsics.checkParameterIsNotNull(strArr, "args");
        printWriter.println("DriveModeController state:");
        printWriter.print("  mDriveModeValue=");
        printWriter.println(this.mDriveModeValue);
        printWriter.print("  isAvailable=");
        printWriter.println(this.mIsDriveModeAvailable);
    }

    public void addCallback(@Nullable DriveModeController.DriveModeListener driveModeListener) {
        if (driveModeListener != null && !this.mListeners.contains(driveModeListener)) {
            synchronized (this.mListeners) {
                this.mListeners.add(driveModeListener);
            }
            driveModeListener.onDriveModeChanged();
        }
    }

    public void removeCallback(@Nullable DriveModeController.DriveModeListener driveModeListener) {
        if (driveModeListener != null) {
            synchronized (this.mListeners) {
                this.mListeners.remove(driveModeListener);
            }
        }
    }

    /* access modifiers changed from: private */
    public final void leaveDriveMode() {
        this.mDriveModeValue = -1;
        Settings.System.putIntForUser(this.mResolver, "drive_mode_drive_mode", -1, -2);
        Intent intent = new Intent();
        intent.setAction("com.miui.app.ExtraStatusBarManager.action_leave_drive_mode");
        this.mContext.sendBroadcast(intent);
    }
}

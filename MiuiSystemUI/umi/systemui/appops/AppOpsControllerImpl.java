package com.android.systemui.appops;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.Dumpable;
import com.android.systemui.appops.AppOpsController;
import com.android.systemui.dump.DumpManager;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AppOpsControllerImpl implements AppOpsController, AppOpsManager.OnOpActiveChangedInternalListener, AppOpsManager.OnOpNotedListener, Dumpable {
    protected static final int[] OPS = {26, 24, 27, 0, 1};
    @GuardedBy({"mActiveItems"})
    private final List<AppOpItem> mActiveItems = new ArrayList();
    private final AppOpsManager mAppOps;
    private H mBGHandler;
    private final List<AppOpsController.Callback> mCallbacks = new ArrayList();
    private final ArrayMap<Integer, Set<AppOpsController.Callback>> mCallbacksByCode = new ArrayMap<>();
    private boolean mListening;
    @GuardedBy({"mNotedItems"})
    private final List<AppOpItem> mNotedItems = new ArrayList();

    public AppOpsControllerImpl(Context context, Looper looper, DumpManager dumpManager) {
        int[] iArr = OPS;
        this.mAppOps = (AppOpsManager) context.getSystemService("appops");
        this.mBGHandler = new H(looper);
        for (int i : iArr) {
            this.mCallbacksByCode.put(Integer.valueOf(i), new ArraySet());
        }
        dumpManager.registerDumpable("AppOpsControllerImpl", this);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setBGHandler(H h) {
        this.mBGHandler = h;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void setListening(boolean z) {
        int[] iArr = OPS;
        this.mListening = z;
        if (z) {
            this.mAppOps.startWatchingActive(iArr, this);
            this.mAppOps.startWatchingNoted(iArr, this);
            return;
        }
        this.mAppOps.stopWatchingActive(this);
        this.mAppOps.stopWatchingNoted(this);
        this.mBGHandler.removeCallbacksAndMessages(null);
        synchronized (this.mActiveItems) {
            this.mActiveItems.clear();
        }
        synchronized (this.mNotedItems) {
            this.mNotedItems.clear();
        }
    }

    @Override // com.android.systemui.appops.AppOpsController
    public void addCallback(int[] iArr, AppOpsController.Callback callback) {
        int length = iArr.length;
        boolean z = false;
        for (int i = 0; i < length; i++) {
            if (this.mCallbacksByCode.containsKey(Integer.valueOf(iArr[i]))) {
                this.mCallbacksByCode.get(Integer.valueOf(iArr[i])).add(callback);
                z = true;
            }
        }
        if (z) {
            this.mCallbacks.add(callback);
        }
        if (!this.mCallbacks.isEmpty()) {
            setListening(true);
        }
    }

    private AppOpItem getAppOpItemLocked(List<AppOpItem> list, int i, int i2, String str) {
        int size = list.size();
        for (int i3 = 0; i3 < size; i3++) {
            AppOpItem appOpItem = list.get(i3);
            if (appOpItem.getCode() == i && appOpItem.getUid() == i2 && appOpItem.getPackageName().equals(str)) {
                return appOpItem;
            }
        }
        return null;
    }

    private boolean updateActives(int i, int i2, String str, boolean z) {
        synchronized (this.mActiveItems) {
            AppOpItem appOpItemLocked = getAppOpItemLocked(this.mActiveItems, i, i2, str);
            if (appOpItemLocked == null && z) {
                this.mActiveItems.add(new AppOpItem(i, i2, str, System.currentTimeMillis()));
                return true;
            } else if (appOpItemLocked == null || z) {
                return false;
            } else {
                this.mActiveItems.remove(appOpItemLocked);
                return true;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0015, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x001d, code lost:
        if (getAppOpItemLocked(r3.mActiveItems, r4, r5, r6) == null) goto L_0x0021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x001f, code lost:
        r0 = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0021, code lost:
        r0 = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0022, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0023, code lost:
        if (r0 != false) goto L_?;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0025, code lost:
        notifySuscribers(r4, r5, r6, false);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0013, code lost:
        r1 = r3.mActiveItems;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void removeNoted(int r4, int r5, java.lang.String r6) {
        /*
            r3 = this;
            java.util.List<com.android.systemui.appops.AppOpItem> r0 = r3.mNotedItems
            monitor-enter(r0)
            java.util.List<com.android.systemui.appops.AppOpItem> r1 = r3.mNotedItems     // Catch:{ all -> 0x002c }
            com.android.systemui.appops.AppOpItem r1 = r3.getAppOpItemLocked(r1, r4, r5, r6)     // Catch:{ all -> 0x002c }
            if (r1 != 0) goto L_0x000d
            monitor-exit(r0)     // Catch:{ all -> 0x002c }
            return
        L_0x000d:
            java.util.List<com.android.systemui.appops.AppOpItem> r2 = r3.mNotedItems     // Catch:{ all -> 0x002c }
            r2.remove(r1)     // Catch:{ all -> 0x002c }
            monitor-exit(r0)     // Catch:{ all -> 0x002c }
            java.util.List<com.android.systemui.appops.AppOpItem> r1 = r3.mActiveItems
            monitor-enter(r1)
            java.util.List<com.android.systemui.appops.AppOpItem> r0 = r3.mActiveItems     // Catch:{ all -> 0x0029 }
            com.android.systemui.appops.AppOpItem r0 = r3.getAppOpItemLocked(r0, r4, r5, r6)     // Catch:{ all -> 0x0029 }
            r2 = 0
            if (r0 == 0) goto L_0x0021
            r0 = 1
            goto L_0x0022
        L_0x0021:
            r0 = r2
        L_0x0022:
            monitor-exit(r1)     // Catch:{ all -> 0x0029 }
            if (r0 != 0) goto L_0x0028
            r3.lambda$onOpActiveChanged$0(r4, r5, r6, r2)
        L_0x0028:
            return
        L_0x0029:
            r3 = move-exception
            monitor-exit(r1)
            throw r3
        L_0x002c:
            r3 = move-exception
            monitor-exit(r0)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.appops.AppOpsControllerImpl.removeNoted(int, int, java.lang.String):void");
    }

    private boolean addNoted(int i, int i2, String str) {
        AppOpItem appOpItemLocked;
        boolean z;
        synchronized (this.mNotedItems) {
            appOpItemLocked = getAppOpItemLocked(this.mNotedItems, i, i2, str);
            if (appOpItemLocked == null) {
                appOpItemLocked = new AppOpItem(i, i2, str, System.currentTimeMillis());
                this.mNotedItems.add(appOpItemLocked);
                z = true;
            } else {
                z = false;
            }
        }
        this.mBGHandler.removeCallbacksAndMessages(appOpItemLocked);
        this.mBGHandler.scheduleRemoval(appOpItemLocked, 5000);
        return z;
    }

    public void onOpActiveChanged(int i, int i2, String str, boolean z) {
        boolean z2;
        if (updateActives(i, i2, str, z)) {
            synchronized (this.mNotedItems) {
                z2 = getAppOpItemLocked(this.mNotedItems, i, i2, str) != null;
            }
            if (!z2) {
                this.mBGHandler.post(new Runnable(i, i2, str, z) {
                    /* class com.android.systemui.appops.$$Lambda$AppOpsControllerImpl$ytWudla0eUXQNol33KSx7VyQvYM */
                    public final /* synthetic */ int f$1;
                    public final /* synthetic */ int f$2;
                    public final /* synthetic */ String f$3;
                    public final /* synthetic */ boolean f$4;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                        this.f$4 = r5;
                    }

                    public final void run() {
                        AppOpsControllerImpl.this.lambda$onOpActiveChanged$0$AppOpsControllerImpl(this.f$1, this.f$2, this.f$3, this.f$4);
                    }
                });
            }
        }
    }

    public void onOpNoted(int i, int i2, String str, int i3) {
        boolean z;
        if (i3 == 0 && addNoted(i, i2, str)) {
            synchronized (this.mActiveItems) {
                z = getAppOpItemLocked(this.mActiveItems, i, i2, str) != null;
            }
            if (!z) {
                this.mBGHandler.post(new Runnable(i, i2, str) {
                    /* class com.android.systemui.appops.$$Lambda$AppOpsControllerImpl$Ikchvj1nqb8W_dVPetwy70ZXqg */
                    public final /* synthetic */ int f$1;
                    public final /* synthetic */ int f$2;
                    public final /* synthetic */ String f$3;

                    {
                        this.f$1 = r2;
                        this.f$2 = r3;
                        this.f$3 = r4;
                    }

                    public final void run() {
                        AppOpsControllerImpl.this.lambda$onOpNoted$1$AppOpsControllerImpl(this.f$1, this.f$2, this.f$3);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onOpNoted$1 */
    public /* synthetic */ void lambda$onOpNoted$1$AppOpsControllerImpl(int i, int i2, String str) {
        lambda$onOpActiveChanged$0(i, i2, str, true);
    }

    /* access modifiers changed from: private */
    /* renamed from: notifySuscribers */
    public void lambda$onOpActiveChanged$0(int i, int i2, String str, boolean z) {
        if (this.mCallbacksByCode.containsKey(Integer.valueOf(i))) {
            for (AppOpsController.Callback callback : this.mCallbacksByCode.get(Integer.valueOf(i))) {
                callback.onActiveStateChanged(i, i2, str, z);
            }
        }
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("AppOpsController state:");
        printWriter.println("  Listening: " + this.mListening);
        printWriter.println("  Active Items:");
        for (int i = 0; i < this.mActiveItems.size(); i++) {
            printWriter.print("    ");
            printWriter.println(this.mActiveItems.get(i).toString());
        }
        printWriter.println("  Noted Items:");
        for (int i2 = 0; i2 < this.mNotedItems.size(); i2++) {
            printWriter.print("    ");
            printWriter.println(this.mNotedItems.get(i2).toString());
        }
    }

    /* access modifiers changed from: protected */
    public class H extends Handler {
        H(Looper looper) {
            super(looper);
        }

        public void scheduleRemoval(final AppOpItem appOpItem, long j) {
            removeCallbacksAndMessages(appOpItem);
            postDelayed(new Runnable() {
                /* class com.android.systemui.appops.AppOpsControllerImpl.H.AnonymousClass1 */

                public void run() {
                    AppOpsControllerImpl.this.removeNoted(appOpItem.getCode(), appOpItem.getUid(), appOpItem.getPackageName());
                }
            }, appOpItem, j);
        }
    }
}

package com.android.systemui.statusbar.notification;

import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import androidx.collection.ArraySet;
import com.android.systemui.Dumpable;
import com.android.systemui.statusbar.NotificationPresenter;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.policy.OnHeadsUpChangedListener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class VisualStabilityManager implements OnHeadsUpChangedListener, Dumpable {
    public VisualStabilityManagerInjector injector = new VisualStabilityManagerInjector();
    private ArraySet<View> mAddedChildren = new ArraySet<>();
    private ArraySet<View> mAllowedReorderViews = new ArraySet<>();
    private boolean mGroupChangedAllowed;
    private final ArrayList<Callback> mGroupChangesAllowedCallbacks = new ArrayList<>();
    private final Handler mHandler;
    private boolean mIsTemporaryReorderingAllowed;
    private ArraySet<NotificationEntry> mLowPriorityReorderingViews = new ArraySet<>();
    private final Runnable mOnTemporaryReorderingExpired = new Runnable() {
        /* class com.android.systemui.statusbar.notification.$$Lambda$VisualStabilityManager$6rf_6W4K3PrMdhwP_O1LDBveJ6k */

        public final void run() {
            VisualStabilityManager.this.lambda$new$0$VisualStabilityManager();
        }
    };
    private boolean mPanelExpanded;
    private final ArraySet<Callback> mPersistentGroupCallbacks = new ArraySet<>();
    private final ArraySet<Callback> mPersistentReorderingCallbacks = new ArraySet<>();
    private boolean mPulsing;
    private boolean mReorderingAllowed;
    private final ArrayList<Callback> mReorderingAllowedCallbacks = new ArrayList<>();
    private boolean mScreenOn;
    private long mTemporaryReorderingStart;
    private VisibilityLocationProvider mVisibilityLocationProvider;

    public interface Callback {
        void onChangeAllowed();
    }

    public void setUpWithPresenter(NotificationPresenter notificationPresenter) {
    }

    public VisualStabilityManager(NotificationEntryManager notificationEntryManager, Handler handler) {
        this.mHandler = handler;
        notificationEntryManager.addNotificationEntryListener(new NotificationEntryListener() {
            /* class com.android.systemui.statusbar.notification.VisualStabilityManager.AnonymousClass1 */

            @Override // com.android.systemui.statusbar.notification.NotificationEntryListener
            public void onPreEntryUpdated(NotificationEntry notificationEntry) {
                if (notificationEntry.isAmbient() != notificationEntry.getRow().isLowPriority()) {
                    VisualStabilityManager.this.mLowPriorityReorderingViews.add(notificationEntry);
                }
            }
        });
    }

    public void addReorderingAllowedCallback(Callback callback, boolean z) {
        if (z) {
            this.mPersistentReorderingCallbacks.add(callback);
        }
        if (!this.mReorderingAllowedCallbacks.contains(callback)) {
            this.mReorderingAllowedCallbacks.add(callback);
        }
    }

    public void addGroupChangesAllowedCallback(Callback callback, boolean z) {
        if (z) {
            this.mPersistentGroupCallbacks.add(callback);
        }
        if (!this.mGroupChangesAllowedCallbacks.contains(callback)) {
            this.mGroupChangesAllowedCallbacks.add(callback);
        }
    }

    public void setPanelExpanded(boolean z) {
        this.mPanelExpanded = z;
        updateAllowedStates();
    }

    public void setScreenOn(boolean z) {
        this.mScreenOn = z;
        updateAllowedStates();
    }

    public void setPulsing(boolean z) {
        if (this.mPulsing != z) {
            this.mPulsing = z;
            updateAllowedStates();
        }
    }

    private void updateAllowedStates() {
        boolean z = false;
        boolean z2 = (!this.mScreenOn || !this.mPanelExpanded || this.mIsTemporaryReorderingAllowed) && !this.mPulsing;
        boolean z3 = z2 && !this.mReorderingAllowed;
        this.mReorderingAllowed = z2;
        if (z3) {
            notifyChangeAllowed(this.mReorderingAllowedCallbacks, this.mPersistentReorderingCallbacks);
        }
        boolean z4 = !this.mPulsing;
        if (z4 && !this.mGroupChangedAllowed) {
            z = true;
        }
        this.mGroupChangedAllowed = z4;
        if (z) {
            notifyChangeAllowed(this.mGroupChangesAllowedCallbacks, this.mPersistentGroupCallbacks);
        }
        this.injector.updateAllowedStates(this.mScreenOn, this.mPanelExpanded, this.mPulsing);
    }

    private void notifyChangeAllowed(ArrayList<Callback> arrayList, ArraySet<Callback> arraySet) {
        int i = 0;
        while (i < arrayList.size()) {
            Callback callback = arrayList.get(i);
            callback.onChangeAllowed();
            if (!arraySet.contains(callback)) {
                arrayList.remove(callback);
                i--;
            }
            i++;
        }
    }

    public boolean isReorderingAllowed() {
        return this.mReorderingAllowed;
    }

    public boolean areGroupChangesAllowed() {
        return this.mGroupChangedAllowed;
    }

    public boolean canReorderNotification(ExpandableNotificationRow expandableNotificationRow) {
        if (this.mReorderingAllowed || this.mAddedChildren.contains(expandableNotificationRow) || this.mLowPriorityReorderingViews.contains(expandableNotificationRow.getEntry())) {
            return true;
        }
        if (!this.mAllowedReorderViews.contains(expandableNotificationRow) || this.mVisibilityLocationProvider.isInVisibleLocation(expandableNotificationRow.getEntry())) {
            return false;
        }
        return true;
    }

    public void setVisibilityLocationProvider(VisibilityLocationProvider visibilityLocationProvider) {
        this.mVisibilityLocationProvider = visibilityLocationProvider;
    }

    public void onReorderingFinished() {
        this.mAllowedReorderViews.clear();
        this.mAddedChildren.clear();
        this.mLowPriorityReorderingViews.clear();
    }

    @Override // com.android.systemui.statusbar.policy.OnHeadsUpChangedListener
    public void onHeadsUpStateChanged(NotificationEntry notificationEntry, boolean z) {
        if (z) {
            this.mAllowedReorderViews.add(notificationEntry.getRow());
        }
    }

    public void temporarilyAllowReordering() {
        this.mHandler.removeCallbacks(this.mOnTemporaryReorderingExpired);
        this.mHandler.postDelayed(this.mOnTemporaryReorderingExpired, 1000);
        if (!this.mIsTemporaryReorderingAllowed) {
            this.mTemporaryReorderingStart = SystemClock.elapsedRealtime();
        }
        this.mIsTemporaryReorderingAllowed = true;
        updateAllowedStates();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$new$0 */
    public /* synthetic */ void lambda$new$0$VisualStabilityManager() {
        this.mIsTemporaryReorderingAllowed = false;
        updateAllowedStates();
    }

    public void notifyViewAddition(View view) {
        this.mAddedChildren.add(view);
    }

    @Override // com.android.systemui.Dumpable
    public void dump(FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.println("VisualStabilityManager state:");
        printWriter.print("  mIsTemporaryReorderingAllowed=");
        printWriter.println(this.mIsTemporaryReorderingAllowed);
        printWriter.print("  mTemporaryReorderingStart=");
        printWriter.println(this.mTemporaryReorderingStart);
        long elapsedRealtime = SystemClock.elapsedRealtime();
        printWriter.print("    Temporary reordering window has been open for ");
        printWriter.print(elapsedRealtime - (this.mIsTemporaryReorderingAllowed ? this.mTemporaryReorderingStart : elapsedRealtime));
        printWriter.println("ms");
        printWriter.println();
    }
}

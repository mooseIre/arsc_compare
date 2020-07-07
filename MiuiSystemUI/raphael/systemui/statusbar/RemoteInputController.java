package com.android.systemui.statusbar;

import android.util.ArrayMap;
import android.util.Pair;
import com.android.internal.util.Preconditions;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class RemoteInputController {
    private final ArrayList<Callback> mCallbacks = new ArrayList<>(3);
    private final HeadsUpManager mHeadsUpManager;
    private final ArrayList<Pair<WeakReference<NotificationData.Entry>, Object>> mOpen = new ArrayList<>();
    private final ArrayMap<String, Object> mSpinning = new ArrayMap<>();

    public interface Callback {
        void onRemoteInputActive(boolean z);

        void onRemoteInputSent(NotificationData.Entry entry);
    }

    public RemoteInputController(HeadsUpManager headsUpManager) {
        addCallback((Callback) Dependency.get(StatusBarWindowManager.class));
        this.mHeadsUpManager = headsUpManager;
    }

    public void addRemoteInput(NotificationData.Entry entry, Object obj) {
        Preconditions.checkNotNull(entry);
        Preconditions.checkNotNull(obj);
        if (!pruneWeakThenRemoveAndContains(entry, (NotificationData.Entry) null, obj)) {
            this.mOpen.add(new Pair(new WeakReference(entry), obj));
        }
        apply(entry);
    }

    public void removeRemoteInput(NotificationData.Entry entry, Object obj) {
        Preconditions.checkNotNull(entry);
        pruneWeakThenRemoveAndContains((NotificationData.Entry) null, entry, obj);
        apply(entry);
    }

    public void addSpinning(String str, Object obj) {
        Preconditions.checkNotNull(str);
        Preconditions.checkNotNull(obj);
        this.mSpinning.put(str, obj);
    }

    public void removeSpinning(String str, Object obj) {
        Preconditions.checkNotNull(str);
        if (obj == null || this.mSpinning.get(str) == obj) {
            this.mSpinning.remove(str);
        }
    }

    public boolean isSpinning(String str) {
        return this.mSpinning.containsKey(str);
    }

    private void apply(NotificationData.Entry entry) {
        this.mHeadsUpManager.setRemoteInputActive(entry, isRemoteInputActive(entry));
        boolean isRemoteInputActive = isRemoteInputActive();
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mCallbacks.get(i).onRemoteInputActive(isRemoteInputActive);
        }
    }

    public boolean isRemoteInputActive(NotificationData.Entry entry) {
        return pruneWeakThenRemoveAndContains(entry, (NotificationData.Entry) null, (Object) null);
    }

    public boolean isRemoteInputActive() {
        pruneWeakThenRemoveAndContains((NotificationData.Entry) null, (NotificationData.Entry) null, (Object) null);
        return !this.mOpen.isEmpty();
    }

    private boolean pruneWeakThenRemoveAndContains(NotificationData.Entry entry, NotificationData.Entry entry2, Object obj) {
        boolean z = false;
        for (int size = this.mOpen.size() - 1; size >= 0; size--) {
            NotificationData.Entry entry3 = (NotificationData.Entry) ((WeakReference) this.mOpen.get(size).first).get();
            Object obj2 = this.mOpen.get(size).second;
            boolean z2 = obj == null || obj2 == obj;
            if (entry3 == null || (entry3 == entry2 && z2)) {
                this.mOpen.remove(size);
            } else if (entry3 == entry) {
                if (obj == null || obj == obj2) {
                    z = true;
                } else {
                    this.mOpen.remove(size);
                }
            }
        }
        return z;
    }

    public void addCallback(Callback callback) {
        Preconditions.checkNotNull(callback);
        this.mCallbacks.add(callback);
    }

    public void remoteInputSent(NotificationData.Entry entry) {
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mCallbacks.get(i).onRemoteInputSent(entry);
        }
    }

    public void closeRemoteInputs() {
        if (this.mOpen.size() != 0) {
            ArrayList arrayList = new ArrayList(this.mOpen.size());
            for (int size = this.mOpen.size() - 1; size >= 0; size--) {
                NotificationData.Entry entry = (NotificationData.Entry) ((WeakReference) this.mOpen.get(size).first).get();
                if (!(entry == null || entry.row == null)) {
                    arrayList.add(entry);
                }
            }
            for (int size2 = arrayList.size() - 1; size2 >= 0; size2--) {
                ExpandableNotificationRow expandableNotificationRow = ((NotificationData.Entry) arrayList.get(size2)).row;
                if (expandableNotificationRow != null) {
                    expandableNotificationRow.closeRemoteInput();
                }
            }
        }
    }
}

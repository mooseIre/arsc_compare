package com.android.systemui.statusbar;

import android.app.Notification;
import android.app.RemoteInput;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.util.Pair;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.policy.RemoteInputUriController;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RemoteInputController {
    private static final boolean ENABLE_REMOTE_INPUT = SystemProperties.getBoolean("debug.enable_remote_input", true);
    private final ArrayList<Callback> mCallbacks = new ArrayList<>(3);
    private final Delegate mDelegate;
    private final ArrayList<Pair<WeakReference<NotificationEntry>, Object>> mOpen = new ArrayList<>();
    private final RemoteInputUriController mRemoteInputUriController;
    private final ArrayMap<String, Object> mSpinning = new ArrayMap<>();

    public interface Callback {
        default void onRemoteInputActive(boolean z) {
        }

        default void onRemoteInputSent(NotificationEntry notificationEntry) {
        }
    }

    public interface Delegate {
        void lockScrollTo(NotificationEntry notificationEntry);

        void requestDisallowLongPressAndDismiss();

        void setRemoteInputActive(NotificationEntry notificationEntry, boolean z);
    }

    public RemoteInputController(Delegate delegate, RemoteInputUriController remoteInputUriController) {
        this.mDelegate = delegate;
        this.mRemoteInputUriController = remoteInputUriController;
    }

    public static void processForRemoteInput(Notification notification, Context context) {
        Bundle bundle;
        RemoteInput[] remoteInputs;
        if (ENABLE_REMOTE_INPUT && (bundle = notification.extras) != null && bundle.containsKey("android.wearable.EXTENSIONS")) {
            Notification.Action[] actionArr = notification.actions;
            if (actionArr == null || actionArr.length == 0) {
                Notification.Action action = null;
                List<Notification.Action> actions = new Notification.WearableExtender(notification).getActions();
                int size = actions.size();
                for (int i = 0; i < size; i++) {
                    Notification.Action action2 = actions.get(i);
                    if (!(action2 == null || (remoteInputs = action2.getRemoteInputs()) == null)) {
                        int length = remoteInputs.length;
                        int i2 = 0;
                        while (true) {
                            if (i2 >= length) {
                                break;
                            } else if (remoteInputs[i2].getAllowFreeFormInput()) {
                                action = action2;
                                break;
                            } else {
                                i2++;
                            }
                        }
                        if (action != null) {
                            break;
                        }
                    }
                }
                if (action != null) {
                    Notification.Builder recoverBuilder = Notification.Builder.recoverBuilder(context, notification);
                    recoverBuilder.setActions(action);
                    recoverBuilder.build();
                }
            }
        }
    }

    public void addRemoteInput(NotificationEntry notificationEntry, Object obj) {
        Objects.requireNonNull(notificationEntry);
        Objects.requireNonNull(obj);
        if (!pruneWeakThenRemoveAndContains(notificationEntry, null, obj)) {
            this.mOpen.add(new Pair<>(new WeakReference(notificationEntry), obj));
        }
        apply(notificationEntry);
    }

    public void removeRemoteInput(NotificationEntry notificationEntry, Object obj) {
        Objects.requireNonNull(notificationEntry);
        pruneWeakThenRemoveAndContains(null, notificationEntry, obj);
        apply(notificationEntry);
    }

    public void addSpinning(String str, Object obj) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(obj);
        this.mSpinning.put(str, obj);
    }

    public void removeSpinning(String str, Object obj) {
        Objects.requireNonNull(str);
        if (obj == null || this.mSpinning.get(str) == obj) {
            this.mSpinning.remove(str);
        }
    }

    public boolean isSpinning(String str) {
        return this.mSpinning.containsKey(str);
    }

    public boolean isSpinning(String str, Object obj) {
        return this.mSpinning.get(str) == obj;
    }

    private void apply(NotificationEntry notificationEntry) {
        this.mDelegate.setRemoteInputActive(notificationEntry, isRemoteInputActive(notificationEntry));
        boolean isRemoteInputActive = isRemoteInputActive();
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mCallbacks.get(i).onRemoteInputActive(isRemoteInputActive);
        }
    }

    public boolean isRemoteInputActive(NotificationEntry notificationEntry) {
        return pruneWeakThenRemoveAndContains(notificationEntry, null, null);
    }

    public boolean isRemoteInputActive() {
        pruneWeakThenRemoveAndContains(null, null, null);
        return !this.mOpen.isEmpty();
    }

    private boolean pruneWeakThenRemoveAndContains(NotificationEntry notificationEntry, NotificationEntry notificationEntry2, Object obj) {
        boolean z = false;
        for (int size = this.mOpen.size() - 1; size >= 0; size--) {
            NotificationEntry notificationEntry3 = (NotificationEntry) ((WeakReference) this.mOpen.get(size).first).get();
            Object obj2 = this.mOpen.get(size).second;
            boolean z2 = obj == null || obj2 == obj;
            if (notificationEntry3 == null || (notificationEntry3 == notificationEntry2 && z2)) {
                this.mOpen.remove(size);
            } else if (notificationEntry3 == notificationEntry) {
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
        Objects.requireNonNull(callback);
        this.mCallbacks.add(callback);
    }

    public void remoteInputSent(NotificationEntry notificationEntry) {
        int size = this.mCallbacks.size();
        for (int i = 0; i < size; i++) {
            this.mCallbacks.get(i).onRemoteInputSent(notificationEntry);
        }
    }

    public void closeRemoteInputs() {
        if (this.mOpen.size() != 0) {
            ArrayList arrayList = new ArrayList(this.mOpen.size());
            for (int size = this.mOpen.size() - 1; size >= 0; size--) {
                NotificationEntry notificationEntry = (NotificationEntry) ((WeakReference) this.mOpen.get(size).first).get();
                if (notificationEntry != null && notificationEntry.rowExists()) {
                    arrayList.add(notificationEntry);
                }
            }
            for (int size2 = arrayList.size() - 1; size2 >= 0; size2--) {
                NotificationEntry notificationEntry2 = (NotificationEntry) arrayList.get(size2);
                if (notificationEntry2.rowExists()) {
                    notificationEntry2.closeRemoteInput();
                }
            }
        }
    }

    public void requestDisallowLongPressAndDismiss() {
        this.mDelegate.requestDisallowLongPressAndDismiss();
    }

    public void lockScrollTo(NotificationEntry notificationEntry) {
        this.mDelegate.lockScrollTo(notificationEntry);
    }

    public void grantInlineReplyUriPermission(StatusBarNotification statusBarNotification, Uri uri) {
        this.mRemoteInputUriController.grantInlineReplyUriPermission(statusBarNotification, uri);
    }
}

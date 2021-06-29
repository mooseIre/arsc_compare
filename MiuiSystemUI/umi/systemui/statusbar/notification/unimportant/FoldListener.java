package com.android.systemui.statusbar.notification.unimportant;

public interface FoldListener {
    default void cancelAllUnimportantNotifications() {
    }

    default void foldPackageAsUnimportant(String str) {
    }

    default void recoverPackageFromUnimportant(String str) {
    }

    default void resetAll(boolean z) {
    }

    default void showUnimportantNotifications() {
    }
}

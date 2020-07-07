package com.android.systemui.bubbles;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.UserHandle;
import android.view.LayoutInflater;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.bubbles.BubbleExpandedView;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.NotificationData;
import java.util.Objects;

class Bubble {
    public NotificationData.Entry entry;
    BubbleExpandedView expandedView;
    BubbleView iconView;
    private String mAppName;
    private final String mGroupId;
    private boolean mInflated;
    private final String mKey;
    private long mLastAccessed;
    private long mLastUpdated;
    private final BubbleExpandedView.OnBubbleBlockedListener mListener;
    private PackageManager mPm;

    public static String groupId(NotificationData.Entry entry2) {
        UserHandle user = entry2.notification.getUser();
        return user.getIdentifier() + "|" + entry2.notification.getPackageName();
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    Bubble(Context context, NotificationData.Entry entry2) {
        this(context, entry2, (BubbleExpandedView.OnBubbleBlockedListener) null);
    }

    Bubble(Context context, NotificationData.Entry entry2, BubbleExpandedView.OnBubbleBlockedListener onBubbleBlockedListener) {
        this.entry = entry2;
        this.mKey = entry2.key;
        this.mLastUpdated = entry2.notification.getPostTime();
        this.mGroupId = groupId(entry2);
        this.mListener = onBubbleBlockedListener;
        PackageManager packageManager = context.getPackageManager();
        this.mPm = packageManager;
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.entry.notification.getPackageName(), 795136);
            if (applicationInfo != null) {
                this.mAppName = String.valueOf(this.mPm.getApplicationLabel(applicationInfo));
            }
        } catch (PackageManager.NameNotFoundException unused) {
            this.mAppName = this.entry.notification.getPackageName();
        }
    }

    public String getKey() {
        return this.mKey;
    }

    public String getGroupId() {
        return this.mGroupId;
    }

    public String getAppName() {
        return this.mAppName;
    }

    public void updateDotVisibility() {
        BubbleView bubbleView = this.iconView;
        if (bubbleView != null) {
            bubbleView.updateDotVisibility(true);
        }
    }

    /* access modifiers changed from: package-private */
    public void inflate(LayoutInflater layoutInflater, BubbleStackView bubbleStackView) {
        if (!this.mInflated) {
            BubbleView bubbleView = (BubbleView) layoutInflater.inflate(R.layout.bubble_view, bubbleStackView, false);
            this.iconView = bubbleView;
            bubbleView.setNotif(this.entry);
            BubbleExpandedView bubbleExpandedView = (BubbleExpandedView) layoutInflater.inflate(R.layout.bubble_expanded_view, bubbleStackView, false);
            this.expandedView = bubbleExpandedView;
            bubbleExpandedView.setEntry(this.entry, bubbleStackView, this.mAppName);
            this.expandedView.setOnBlockedListener(this.mListener);
            this.mInflated = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void setDismissed() {
        this.entry.setBubbleDismissed(true);
        BubbleExpandedView bubbleExpandedView = this.expandedView;
        if (bubbleExpandedView != null) {
            bubbleExpandedView.cleanUpExpandedState();
        }
    }

    /* access modifiers changed from: package-private */
    public void setEntry(NotificationData.Entry entry2) {
        this.entry = entry2;
        this.mLastUpdated = entry2.notification.getPostTime();
        if (this.mInflated) {
            this.iconView.update(entry2);
            this.expandedView.update(entry2);
        }
    }

    public long getLastActivity() {
        return Math.max(this.mLastUpdated, this.mLastAccessed);
    }

    public long getLastUpdateTime() {
        return this.mLastUpdated;
    }

    /* access modifiers changed from: package-private */
    public void markAsAccessedAt(long j) {
        this.mLastAccessed = j;
        this.entry.setShowInShadeWhenBubble(false);
    }

    public boolean isOngoing() {
        return this.entry.isForegroundService();
    }

    public String toString() {
        return "Bubble{" + this.mKey + '}';
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Bubble)) {
            return false;
        }
        return Objects.equals(this.mKey, ((Bubble) obj).mKey);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.mKey});
    }
}

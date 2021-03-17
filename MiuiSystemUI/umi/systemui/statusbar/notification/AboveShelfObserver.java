package com.android.systemui.statusbar.notification;

import android.view.View;
import android.view.ViewGroup;
import com.android.internal.annotations.VisibleForTesting;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public class AboveShelfObserver implements AboveShelfChangedListener {
    private boolean mHasViewsAboveShelf = false;
    private final ViewGroup mHostLayout;
    private HasViewAboveShelfChangedListener mListener;

    public interface HasViewAboveShelfChangedListener {
        void onHasViewsAboveShelfChanged(boolean z);
    }

    public AboveShelfObserver(ViewGroup viewGroup) {
        this.mHostLayout = viewGroup;
    }

    public void setListener(HasViewAboveShelfChangedListener hasViewAboveShelfChangedListener) {
        this.mListener = hasViewAboveShelfChangedListener;
    }

    public void onAboveShelfStateChanged(boolean z) {
        ViewGroup viewGroup;
        if (!z && (viewGroup = this.mHostLayout) != null) {
            int childCount = viewGroup.getChildCount();
            int i = 0;
            while (true) {
                if (i >= childCount) {
                    break;
                }
                View childAt = this.mHostLayout.getChildAt(i);
                if ((childAt instanceof ExpandableNotificationRow) && ((ExpandableNotificationRow) childAt).isAboveShelf()) {
                    z = true;
                    break;
                }
                i++;
            }
        }
        if (this.mHasViewsAboveShelf != z) {
            this.mHasViewsAboveShelf = z;
            HasViewAboveShelfChangedListener hasViewAboveShelfChangedListener = this.mListener;
            if (hasViewAboveShelfChangedListener != null) {
                hasViewAboveShelfChangedListener.onHasViewsAboveShelfChanged(z);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean hasViewsAboveShelf() {
        return this.mHasViewsAboveShelf;
    }
}

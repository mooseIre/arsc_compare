package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.NotificationHeaderView;
import android.view.View;
import com.android.systemui.Constants;
import com.android.systemui.miui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.CrossFadeHelper;
import com.android.systemui.statusbar.ExpandableNotificationRow;
import com.android.systemui.statusbar.TransformableView;

public abstract class NotificationViewWrapper implements TransformableView {
    public static final boolean DEBUG = Constants.DEBUG;
    protected int mBackgroundColor = 0;
    protected Context mContext;
    protected final ExpandableNotificationRow mRow;
    protected TYPE_SHOWING mShowingType = TYPE_SHOWING.TYPE_UNKNOWN;
    protected final View mView;

    public enum TYPE_SHOWING {
        TYPE_UNKNOWN,
        TYPE_CONTRACTED,
        TYPE_EXPANDED,
        TYPE_HEADSUP,
        TYPE_AMBIENT
    }

    public TransformState getCurrentState(int i) {
        return null;
    }

    public int getHeaderTranslation() {
        return 0;
    }

    public NotificationHeaderView getNotificationHeader() {
        return null;
    }

    public boolean isDimmable() {
        return true;
    }

    public void setContentHeight(int i, int i2) {
    }

    public void setDark(boolean z, boolean z2, long j) {
    }

    public void setIsChildInGroup(boolean z) {
    }

    public void setLegacy(boolean z) {
    }

    public void setRemoteInputVisible(boolean z) {
    }

    /* access modifiers changed from: protected */
    public boolean shouldClearBackgroundOnReapply() {
        return true;
    }

    public void showPublic() {
    }

    public void updateExpandability(boolean z, View.OnClickListener onClickListener) {
    }

    public static NotificationViewWrapper wrap(Context context, View view, ExpandableNotificationRow expandableNotificationRow, TYPE_SHOWING type_showing) {
        if (DEBUG) {
            Log.d("NViewWrapper", "wrap key=" + expandableNotificationRow.getEntry().key + ", showingType=" + type_showing + ", v=" + view.getTag());
        }
        NotificationViewWrapper wrap = wrap(context, view, expandableNotificationRow);
        wrap.mShowingType = type_showing;
        return wrap;
    }

    public static NotificationViewWrapper wrap(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        if (view.getId() == 16909516) {
            if ("bigPicture".equals(view.getTag())) {
                return new NotificationBigPictureTemplateViewWrapper(context, view, expandableNotificationRow);
            }
            if ("bigText".equals(view.getTag())) {
                return new NotificationBigTextTemplateViewWrapper(context, view, expandableNotificationRow);
            }
            if ("media".equals(view.getTag()) || "bigMediaNarrow".equals(view.getTag())) {
                return new NotificationMediaTemplateViewWrapper(context, view, expandableNotificationRow);
            }
            if ("messaging".equals(view.getTag())) {
                return new NotificationMessagingTemplateViewWrapper(context, view, expandableNotificationRow);
            }
            if ("inbox".equals(view.getTag())) {
                return new NotificationInboxTemplateViewWrapper(context, view, expandableNotificationRow);
            }
            return new NotificationTemplateViewWrapper(context, view, expandableNotificationRow);
        } else if (view instanceof NotificationHeaderView) {
            return new NotificationHeaderViewWrapper(context, view, expandableNotificationRow);
        } else {
            if (view instanceof OptimizedHeadsUpNotificationView) {
                return new NotificationOptimizedHeadsUpViewWrapper(context, view, expandableNotificationRow);
            }
            if (view instanceof InCallNotificationView) {
                return new NotificationInCallViewWrapper(context, view, expandableNotificationRow);
            }
            return new NotificationCustomViewWrapper(context, view, expandableNotificationRow);
        }
    }

    protected NotificationViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        this.mContext = context;
        this.mView = view;
        this.mRow = expandableNotificationRow;
        onReinflated();
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        if (DEBUG) {
            Log.d("NViewWrapper", "onContentUpdated key=" + expandableNotificationRow.getEntry().key + ", mShowingType=" + this.mShowingType);
        }
    }

    public void onReinflated() {
        if (shouldClearBackgroundOnReapply()) {
            this.mBackgroundColor = 0;
        }
        int backgroundColor = getBackgroundColor(this.mView);
        if (backgroundColor != 0) {
            this.mBackgroundColor = backgroundColor;
        }
        this.mView.setBackground(new ColorDrawable(0));
    }

    /* access modifiers changed from: protected */
    public int getBackgroundColor(View view) {
        if (view != null && NotificationUtil.isColorizedNotification(this.mRow.getEntry().notification)) {
            Drawable background = view.getBackground();
            if (background instanceof ColorDrawable) {
                return ((ColorDrawable) background).getColor();
            }
        }
        return 0;
    }

    public void transformTo(TransformableView transformableView, Runnable runnable) {
        CrossFadeHelper.fadeOut(this.mView, runnable);
    }

    public void transformTo(TransformableView transformableView, float f) {
        CrossFadeHelper.fadeOut(this.mView, f);
    }

    public void transformFrom(TransformableView transformableView) {
        CrossFadeHelper.fadeIn(this.mView);
    }

    public void transformFrom(TransformableView transformableView, float f) {
        CrossFadeHelper.fadeIn(this.mView, f);
    }

    public void setVisible(boolean z) {
        this.mView.animate().cancel();
        this.mView.setVisibility(z ? 0 : 4);
    }

    public int getCustomBackgroundColor() {
        if (this.mRow.isSummaryWithChildren()) {
            return 0;
        }
        return this.mBackgroundColor;
    }
}

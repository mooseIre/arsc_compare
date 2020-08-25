package com.android.systemui.statusbar;

import android.app.Notification;
import android.app.NotificationCompat;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.IconCompat;
import android.text.TextUtils;
import android.view.NotificationHeaderView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.SystemUICompat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NotificationHeaderUtil {
    private static final ResultApplicator mGreyApplicator = new ResultApplicator() {
        public void apply(View view, boolean z) {
            NotificationHeaderView notificationHeaderView = (NotificationHeaderView) view;
            applyToChild((ImageView) view.findViewById(16908294), z, notificationHeaderView.getOriginalIconColor());
            applyToChild((ImageView) view.findViewById(16908906), z, notificationHeaderView.getOriginalNotificationColor());
        }

        private void applyToChild(View view, boolean z, int i) {
            Drawable drawable;
            if (view != null && i != 1 && (drawable = ((ImageView) view).getDrawable()) != null) {
                drawable.mutate();
                if (z) {
                    drawable.setColorFilter(view.getContext().getColor(SystemUICompat.getNotificationDefaultColor()), PorterDuff.Mode.SRC_ATOP);
                } else {
                    drawable.setColorFilter(i, PorterDuff.Mode.SRC_ATOP);
                }
            }
        }
    };
    private static final IconComparator sGreyComparator = new IconComparator() {
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return !hasSameIcon(obj, obj2) || hasSameColor(obj, obj2);
        }
    };
    private static final DataExtractor sIconExtractor = new DataExtractor() {
        public Object extractData(ExpandableNotificationRow expandableNotificationRow) {
            return expandableNotificationRow.getStatusBarNotification().getNotification();
        }
    };
    private static final IconComparator sIconVisibilityComparator = new IconComparator() {
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return hasSameIcon(obj, obj2) && hasSameColor(obj, obj2);
        }
    };
    /* access modifiers changed from: private */
    public static final TextViewComparator sTextViewComparator = new TextViewComparator();
    /* access modifiers changed from: private */
    public static final VisibilityApplicator sVisibilityApplicator = new VisibilityApplicator();
    private final ArrayList<HeaderProcessor> mComparators = new ArrayList<>();
    private final HashSet<Integer> mDividers = new HashSet<>();
    private final ExpandableNotificationRow mRow;

    private interface DataExtractor {
        Object extractData(ExpandableNotificationRow expandableNotificationRow);
    }

    private interface ResultApplicator {
        void apply(View view, boolean z);
    }

    private interface ViewComparator {
        boolean compare(View view, View view2, Object obj, Object obj2);

        boolean isEmpty(View view);
    }

    public NotificationHeaderUtil(ExpandableNotificationRow expandableNotificationRow) {
        this.mRow = expandableNotificationRow;
        this.mComparators.add(new HeaderProcessor(this.mRow, 16908294, sIconExtractor, sIconVisibilityComparator, sVisibilityApplicator));
        this.mComparators.add(new HeaderProcessor(this.mRow, 16909276, (DataExtractor) null, new ViewComparator() {
            public boolean compare(View view, View view2, Object obj, Object obj2) {
                return view.getVisibility() != 8;
            }

            public boolean isEmpty(View view) {
                if (!(view instanceof ImageView) || ((ImageView) view).getDrawable() != null) {
                    return false;
                }
                return true;
            }
        }, sVisibilityApplicator));
        this.mComparators.add(HeaderProcessor.forTextView(this.mRow, 16908733));
        this.mComparators.add(HeaderProcessor.forTextView(this.mRow, 16908985));
        this.mDividers.add(16908986);
        this.mDividers.add(16909483);
    }

    public void updateChildrenHeaderAppearance() {
        List<ExpandableNotificationRow> notificationChildren = this.mRow.getNotificationChildren();
        if (notificationChildren != null) {
            for (int i = 0; i < this.mComparators.size(); i++) {
                this.mComparators.get(i).init();
            }
            for (int i2 = 0; i2 < notificationChildren.size(); i2++) {
                ExpandableNotificationRow expandableNotificationRow = notificationChildren.get(i2);
                for (int i3 = 0; i3 < this.mComparators.size(); i3++) {
                    this.mComparators.get(i3).compareToHeader(expandableNotificationRow);
                }
            }
            for (int i4 = 0; i4 < notificationChildren.size(); i4++) {
                ExpandableNotificationRow expandableNotificationRow2 = notificationChildren.get(i4);
                for (int i5 = 0; i5 < this.mComparators.size(); i5++) {
                    this.mComparators.get(i5).apply(expandableNotificationRow2);
                }
                sanitizeHeaderViews(expandableNotificationRow2);
            }
        }
    }

    private void sanitizeHeaderViews(ExpandableNotificationRow expandableNotificationRow) {
        if (expandableNotificationRow.isSummaryWithChildren()) {
            sanitizeHeader(expandableNotificationRow.getNotificationHeader());
            return;
        }
        NotificationContentView privateLayout = expandableNotificationRow.getPrivateLayout();
        sanitizeChild(privateLayout.getContractedChild());
        sanitizeChild(privateLayout.getHeadsUpChild());
        sanitizeChild(privateLayout.getExpandedChild());
    }

    private void sanitizeChild(View view) {
        if (view != null) {
            sanitizeHeader(view.findViewById(16909178));
        }
    }

    private void sanitizeHeader(NotificationHeaderView notificationHeaderView) {
        View view;
        boolean z;
        boolean z2;
        if (notificationHeaderView != null) {
            int childCount = notificationHeaderView.getChildCount();
            View findViewById = notificationHeaderView.findViewById(16909479);
            if (findViewById != null) {
                int i = 1;
                while (true) {
                    if (i >= childCount - 1) {
                        z2 = false;
                        break;
                    }
                    View childAt = notificationHeaderView.getChildAt(i);
                    if ((childAt instanceof TextView) && childAt.getVisibility() != 8 && !this.mDividers.contains(Integer.valueOf(childAt.getId())) && childAt != findViewById) {
                        z2 = true;
                        break;
                    }
                    i++;
                }
                findViewById.setVisibility((!z2 || NotificationCompat.showsTime(this.mRow.getStatusBarNotification().getNotification())) ? 0 : 8);
            }
            View view2 = null;
            int i2 = 1;
            while (true) {
                int i3 = childCount - 1;
                if (i2 < i3) {
                    View childAt2 = notificationHeaderView.getChildAt(i2);
                    if (this.mDividers.contains(Integer.valueOf(childAt2.getId()))) {
                        while (true) {
                            i2++;
                            if (i2 >= i3) {
                                break;
                            }
                            view = notificationHeaderView.getChildAt(i2);
                            if (this.mDividers.contains(Integer.valueOf(view.getId()))) {
                                i2--;
                                break;
                            } else if (view.getVisibility() != 8 && (view instanceof TextView)) {
                                if (view2 != null) {
                                    z = true;
                                }
                            }
                        }
                        view = view2;
                        z = false;
                        childAt2.setVisibility(z ? 0 : 8);
                        view2 = view;
                    } else if (childAt2.getVisibility() != 8 && (childAt2 instanceof TextView)) {
                        view2 = childAt2;
                    }
                    i2++;
                } else {
                    return;
                }
            }
        }
    }

    public void restoreNotificationHeader(ExpandableNotificationRow expandableNotificationRow) {
        for (int i = 0; i < this.mComparators.size(); i++) {
            this.mComparators.get(i).apply(expandableNotificationRow, true);
        }
        sanitizeHeaderViews(expandableNotificationRow);
    }

    private static class HeaderProcessor {
        private final ResultApplicator mApplicator;
        private boolean mApply;
        private ViewComparator mComparator;
        private final DataExtractor mExtractor;
        private final int mId;
        private Object mParentData;
        private final ExpandableNotificationRow mParentRow;
        private View mParentView;

        public static HeaderProcessor forTextView(ExpandableNotificationRow expandableNotificationRow, int i) {
            return new HeaderProcessor(expandableNotificationRow, i, (DataExtractor) null, NotificationHeaderUtil.sTextViewComparator, NotificationHeaderUtil.sVisibilityApplicator);
        }

        HeaderProcessor(ExpandableNotificationRow expandableNotificationRow, int i, DataExtractor dataExtractor, ViewComparator viewComparator, ResultApplicator resultApplicator) {
            this.mId = i;
            this.mExtractor = dataExtractor;
            this.mApplicator = resultApplicator;
            this.mComparator = viewComparator;
            this.mParentRow = expandableNotificationRow;
        }

        public void init() {
            this.mParentView = this.mParentRow.getNotificationHeader().findViewById(this.mId);
            DataExtractor dataExtractor = this.mExtractor;
            this.mParentData = dataExtractor == null ? null : dataExtractor.extractData(this.mParentRow);
            this.mApply = !this.mComparator.isEmpty(this.mParentView);
        }

        public void compareToHeader(ExpandableNotificationRow expandableNotificationRow) {
            if (this.mApply) {
                NotificationHeaderView notificationHeader = expandableNotificationRow.getNotificationHeader();
                if (this.mParentView == null || notificationHeader == null) {
                    this.mApply = false;
                    return;
                }
                DataExtractor dataExtractor = this.mExtractor;
                this.mApply = this.mComparator.compare(this.mParentView, notificationHeader.findViewById(this.mId), this.mParentData, dataExtractor == null ? null : dataExtractor.extractData(expandableNotificationRow));
            }
        }

        public void apply(ExpandableNotificationRow expandableNotificationRow) {
            apply(expandableNotificationRow, false);
        }

        public void apply(ExpandableNotificationRow expandableNotificationRow, boolean z) {
            boolean z2 = this.mApply && !z;
            if (expandableNotificationRow.isSummaryWithChildren()) {
                applyToView(z2, expandableNotificationRow.getNotificationHeader());
                return;
            }
            applyToView(z2, expandableNotificationRow.getPrivateLayout().getContractedChild());
            applyToView(z2, expandableNotificationRow.getPrivateLayout().getHeadsUpChild());
            applyToView(z2, expandableNotificationRow.getPrivateLayout().getExpandedChild());
        }

        private void applyToView(boolean z, View view) {
            View findViewById;
            if (view != null && (findViewById = view.findViewById(this.mId)) != null && !this.mComparator.isEmpty(findViewById)) {
                this.mApplicator.apply(findViewById, z);
            }
        }
    }

    private static class TextViewComparator implements ViewComparator {
        private TextViewComparator() {
        }

        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return ((TextView) view).getText().equals(((TextView) view2).getText());
        }

        public boolean isEmpty(View view) {
            return TextUtils.isEmpty(((TextView) view).getText());
        }
    }

    private static abstract class IconComparator implements ViewComparator {
        public boolean isEmpty(View view) {
            return false;
        }

        private IconComparator() {
        }

        /* access modifiers changed from: protected */
        public boolean hasSameIcon(Object obj, Object obj2) {
            return IconCompat.sameAs(((Notification) obj).getSmallIcon(), ((Notification) obj2).getSmallIcon());
        }

        /* access modifiers changed from: protected */
        public boolean hasSameColor(Object obj, Object obj2) {
            return ((Notification) obj).color == ((Notification) obj2).color;
        }
    }

    private static class VisibilityApplicator implements ResultApplicator {
        private VisibilityApplicator() {
        }

        public void apply(View view, boolean z) {
            view.setVisibility(z ? 8 : 0);
        }
    }
}

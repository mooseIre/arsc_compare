package com.android.systemui.statusbar;

import android.app.Notification;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.widget.ConversationLayout;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;
import com.android.systemui.statusbar.notification.row.NotificationContentView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NotificationHeaderUtil {
    private static final VisibilityApplicator sAppNameApplicator = new AppNameApplicator();
    private static final DataExtractor sIconExtractor = new DataExtractor() {
        /* class com.android.systemui.statusbar.NotificationHeaderUtil.AnonymousClass1 */

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.DataExtractor
        public Object extractData(ExpandableNotificationRow expandableNotificationRow) {
            return expandableNotificationRow.getEntry().getSbn().getNotification();
        }
    };
    private static final IconComparator sIconVisibilityComparator = new IconComparator() {
        /* class com.android.systemui.statusbar.NotificationHeaderUtil.AnonymousClass2 */

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return hasSameIcon(obj, obj2) && hasSameColor(obj, obj2);
        }
    };
    private static final TextViewComparator sTextViewComparator = new TextViewComparator();
    private static final VisibilityApplicator sVisibilityApplicator = new VisibilityApplicator();
    private final ArrayList<HeaderProcessor> mComparators = new ArrayList<>();
    private final HashSet<Integer> mDividers = new HashSet<>();
    private final ExpandableNotificationRow mRow;

    /* access modifiers changed from: private */
    public interface DataExtractor {
        Object extractData(ExpandableNotificationRow expandableNotificationRow);
    }

    /* access modifiers changed from: private */
    public interface ResultApplicator {
        void apply(View view, View view2, boolean z, boolean z2);
    }

    /* access modifiers changed from: private */
    public interface ViewComparator {
        boolean compare(View view, View view2, Object obj, Object obj2);

        boolean isEmpty(View view);
    }

    public NotificationHeaderUtil(ExpandableNotificationRow expandableNotificationRow) {
        this.mRow = expandableNotificationRow;
        this.mComparators.add(new HeaderProcessor(expandableNotificationRow, 16908294, sIconExtractor, sIconVisibilityComparator, sVisibilityApplicator));
        this.mComparators.add(new HeaderProcessor(this.mRow, 16909335, null, new ViewComparator(this) {
            /* class com.android.systemui.statusbar.NotificationHeaderUtil.AnonymousClass5 */

            @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
            public boolean compare(View view, View view2, Object obj, Object obj2) {
                return view.getVisibility() != 8;
            }

            @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
            public boolean isEmpty(View view) {
                if (!(view instanceof ImageView) || ((ImageView) view).getDrawable() != null) {
                    return false;
                }
                return true;
            }
        }, sVisibilityApplicator));
        this.mComparators.add(new HeaderProcessor(this.mRow, 16908763, null, sTextViewComparator, sAppNameApplicator));
        this.mComparators.add(HeaderProcessor.forTextView(this.mRow, 16909041));
        this.mDividers.add(16909042);
        this.mDividers.add(16909044);
        this.mDividers.add(16909555);
    }

    public void updateChildrenHeaderAppearance() {
        List<ExpandableNotificationRow> attachedChildren = this.mRow.getAttachedChildren();
        if (attachedChildren != null) {
            for (int i = 0; i < this.mComparators.size(); i++) {
                this.mComparators.get(i).init();
            }
            for (int i2 = 0; i2 < attachedChildren.size(); i2++) {
                ExpandableNotificationRow expandableNotificationRow = attachedChildren.get(i2);
                for (int i3 = 0; i3 < this.mComparators.size(); i3++) {
                    this.mComparators.get(i3).compareToHeader(expandableNotificationRow);
                }
            }
            for (int i4 = 0; i4 < attachedChildren.size(); i4++) {
                ExpandableNotificationRow expandableNotificationRow2 = attachedChildren.get(i4);
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
            sanitizeHeader((ViewGroup) view.findViewById(16909237));
        }
    }

    private void sanitizeHeader(ViewGroup viewGroup) {
        boolean z;
        View view;
        boolean z2;
        if (viewGroup != null) {
            int childCount = viewGroup.getChildCount();
            View findViewById = viewGroup.findViewById(16909551);
            int i = 0;
            while (true) {
                if (i >= childCount) {
                    z = false;
                    break;
                }
                View childAt = viewGroup.getChildAt(i);
                if ((childAt instanceof TextView) && childAt.getVisibility() != 8 && !this.mDividers.contains(Integer.valueOf(childAt.getId())) && childAt != findViewById) {
                    z = true;
                    break;
                }
                i++;
            }
            findViewById.setVisibility((!z || this.mRow.getEntry().getSbn().getNotification().showsTime()) ? 0 : 8);
            View view2 = null;
            int i2 = 0;
            while (i2 < childCount) {
                View childAt2 = viewGroup.getChildAt(i2);
                if (this.mDividers.contains(Integer.valueOf(childAt2.getId()))) {
                    while (true) {
                        i2++;
                        if (i2 >= childCount) {
                            break;
                        }
                        view = viewGroup.getChildAt(i2);
                        if (this.mDividers.contains(Integer.valueOf(view.getId()))) {
                            i2--;
                            break;
                        } else if (view.getVisibility() != 8 && (view instanceof TextView)) {
                            if (view2 != null) {
                                z2 = true;
                            }
                        }
                    }
                    view = view2;
                    z2 = false;
                    childAt2.setVisibility(z2 ? 0 : 8);
                    view2 = view;
                } else if (childAt2.getVisibility() != 8 && (childAt2 instanceof TextView)) {
                    view2 = childAt2;
                }
                i2++;
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
            return new HeaderProcessor(expandableNotificationRow, i, null, NotificationHeaderUtil.sTextViewComparator, NotificationHeaderUtil.sVisibilityApplicator);
        }

        HeaderProcessor(ExpandableNotificationRow expandableNotificationRow, int i, DataExtractor dataExtractor, ViewComparator viewComparator, ResultApplicator resultApplicator) {
            this.mId = i;
            this.mExtractor = dataExtractor;
            this.mApplicator = resultApplicator;
            this.mComparator = viewComparator;
            this.mParentRow = expandableNotificationRow;
        }

        public void init() {
            if (this.mParentRow.getNotificationHeader() != null) {
                this.mParentView = this.mParentRow.getNotificationHeader().findViewById(this.mId);
                DataExtractor dataExtractor = this.mExtractor;
                this.mParentData = dataExtractor == null ? null : dataExtractor.extractData(this.mParentRow);
                this.mApply = !this.mComparator.isEmpty(this.mParentView);
            }
        }

        public void compareToHeader(ExpandableNotificationRow expandableNotificationRow) {
            View contractedChild;
            View findViewById;
            if (this.mApply && (contractedChild = expandableNotificationRow.getPrivateLayout().getContractedChild()) != null && (findViewById = contractedChild.findViewById(this.mId)) != null) {
                DataExtractor dataExtractor = this.mExtractor;
                this.mApply = this.mComparator.compare(this.mParentView, findViewById, this.mParentData, dataExtractor == null ? null : dataExtractor.extractData(expandableNotificationRow));
            }
        }

        public void apply(ExpandableNotificationRow expandableNotificationRow) {
            apply(expandableNotificationRow, false);
        }

        public void apply(ExpandableNotificationRow expandableNotificationRow, boolean z) {
            boolean z2 = this.mApply && !z;
            if (expandableNotificationRow.isSummaryWithChildren()) {
                applyToView(z2, z, expandableNotificationRow.getNotificationHeader());
                return;
            }
            applyToView(z2, z, expandableNotificationRow.getPrivateLayout().getContractedChild());
            applyToView(z2, z, expandableNotificationRow.getPrivateLayout().getHeadsUpChild());
            applyToView(z2, z, expandableNotificationRow.getPrivateLayout().getExpandedChild());
        }

        private void applyToView(boolean z, boolean z2, View view) {
            View findViewById;
            if (view != null && (findViewById = view.findViewById(this.mId)) != null && !this.mComparator.isEmpty(findViewById)) {
                this.mApplicator.apply(view, findViewById, z, z2);
            }
        }
    }

    /* access modifiers changed from: private */
    public static class TextViewComparator implements ViewComparator {
        private TextViewComparator() {
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean compare(View view, View view2, Object obj, Object obj2) {
            return ((TextView) view).getText().equals(((TextView) view2).getText());
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean isEmpty(View view) {
            return TextUtils.isEmpty(((TextView) view).getText());
        }
    }

    private static abstract class IconComparator implements ViewComparator {
        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ViewComparator
        public boolean isEmpty(View view) {
            return false;
        }

        private IconComparator() {
        }

        /* access modifiers changed from: protected */
        public boolean hasSameIcon(Object obj, Object obj2) {
            return ((Notification) obj).getSmallIcon().sameAs(((Notification) obj2).getSmallIcon());
        }

        /* access modifiers changed from: protected */
        public boolean hasSameColor(Object obj, Object obj2) {
            return ((Notification) obj).color == ((Notification) obj2).color;
        }
    }

    /* access modifiers changed from: private */
    public static class VisibilityApplicator implements ResultApplicator {
        private VisibilityApplicator() {
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.ResultApplicator
        public void apply(View view, View view2, boolean z, boolean z2) {
            view2.setVisibility(z ? 8 : 0);
        }
    }

    private static class AppNameApplicator extends VisibilityApplicator {
        private AppNameApplicator() {
            super();
        }

        @Override // com.android.systemui.statusbar.NotificationHeaderUtil.VisibilityApplicator, com.android.systemui.statusbar.NotificationHeaderUtil.ResultApplicator
        public void apply(View view, View view2, boolean z, boolean z2) {
            if (z2 && (view instanceof ConversationLayout)) {
                z = ((ConversationLayout) view).shouldHideAppName();
            }
            super.apply(view, view2, z, z2);
        }
    }
}

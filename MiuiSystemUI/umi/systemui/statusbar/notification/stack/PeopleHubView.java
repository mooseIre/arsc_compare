package com.android.systemui.statusbar.notification.stack;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0015R$id;
import com.android.systemui.plugins.statusbar.NotificationMenuRowPlugin;
import com.android.systemui.statusbar.notification.row.StackScrollerDecorView;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.ranges.RangesKt___RangesKt;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt___SequencesKt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: PeopleHubView.kt */
public final class PeopleHubView extends StackScrollerDecorView implements SwipeableView {
    private boolean canSwipe;
    private ViewGroup contents;
    private TextView label;

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    @Nullable
    public NotificationMenuRowPlugin createMenu() {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    @Nullable
    public View findSecondaryView() {
        return null;
    }

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    public boolean hasFinishedInitialization() {
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView, com.android.systemui.statusbar.notification.row.ExpandableView
    public boolean needsClippingToShelf() {
        return true;
    }

    public static final /* synthetic */ ViewGroup access$getContents$p(PeopleHubView peopleHubView) {
        ViewGroup viewGroup = peopleHubView.contents;
        if (viewGroup != null) {
            return viewGroup;
        }
        Intrinsics.throwUninitializedPropertyAccessException("contents");
        throw null;
    }

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public PeopleHubView(@NotNull Context context, @NotNull AttributeSet attributeSet) {
        super(context, attributeSet);
        Intrinsics.checkParameterIsNotNull(context, "context");
        Intrinsics.checkParameterIsNotNull(attributeSet, "attrs");
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    public void onFinishInflate() {
        View requireViewById = requireViewById(C0015R$id.people_list);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "requireViewById(R.id.people_list)");
        this.contents = (ViewGroup) requireViewById;
        View requireViewById2 = requireViewById(C0015R$id.header_label);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "requireViewById(R.id.header_label)");
        this.label = (TextView) requireViewById2;
        ViewGroup viewGroup = this.contents;
        if (viewGroup != null) {
            Sequence<T> unused = CollectionsKt___CollectionsKt.asSequence(SequencesKt___SequencesKt.toList(SequencesKt___SequencesKt.mapNotNull(CollectionsKt___CollectionsKt.asSequence(RangesKt___RangesKt.until(0, viewGroup.getChildCount())), new PeopleHubView$onFinishInflate$1(this))));
            super.onFinishInflate();
            setVisible(true, false);
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("contents");
        throw null;
    }

    public final void setTextColor(int i) {
        TextView textView = this.label;
        if (textView != null) {
            textView.setTextColor(i);
        } else {
            Intrinsics.throwUninitializedPropertyAccessException("label");
            throw null;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.StackScrollerDecorView
    @NotNull
    public View findContentView() {
        ViewGroup viewGroup = this.contents;
        if (viewGroup != null) {
            return viewGroup;
        }
        Intrinsics.throwUninitializedPropertyAccessException("contents");
        throw null;
    }

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView
    public void resetTranslation() {
        setTranslationX(0.0f);
    }

    @Override // com.android.systemui.statusbar.notification.stack.SwipeableView, com.android.systemui.statusbar.notification.row.ExpandableView
    public void setTranslation(float f) {
        if (this.canSwipe) {
            super.setTranslation(f);
        }
    }

    public final boolean getCanSwipe() {
        return this.canSwipe;
    }

    public final void setCanSwipe(boolean z) {
        boolean z2 = this.canSwipe;
        if (z2 != z) {
            if (z2) {
                resetTranslation();
            }
            this.canSwipe = z;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.ExpandableView
    public void applyContentTransformation(float f, float f2) {
        super.applyContentTransformation(f, f2);
        ViewGroup viewGroup = this.contents;
        if (viewGroup != null) {
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ViewGroup viewGroup2 = this.contents;
                if (viewGroup2 != null) {
                    View childAt = viewGroup2.getChildAt(i);
                    Intrinsics.checkExpressionValueIsNotNull(childAt, "view");
                    childAt.setAlpha(f);
                    childAt.setTranslationY(f2);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("contents");
                    throw null;
                }
            }
            return;
        }
        Intrinsics.throwUninitializedPropertyAccessException("contents");
        throw null;
    }

    /* access modifiers changed from: private */
    /* compiled from: PeopleHubView.kt */
    public final class PersonDataListenerImpl {
        @NotNull
        private final ImageView avatarView;

        public PersonDataListenerImpl(@NotNull PeopleHubView peopleHubView, ImageView imageView) {
            Intrinsics.checkParameterIsNotNull(imageView, "avatarView");
            this.avatarView = imageView;
        }
    }
}

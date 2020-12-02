package com.android.systemui.statusbar.notification.stack;

import android.util.Log;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.C0015R$id;
import com.android.systemui.statusbar.notification.AnimatableProperty;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationAnimationExtensions.kt */
public final class MiuiStackStateAnimator extends StackStateAnimator {
    private boolean mHasPanelAppearDisappearEvent;
    private int mHeadsUpAppearHeightBottom;
    private final ArrayList<ExpandableView> mHeadsUpAppearView = new ArrayList<>();
    private final ArrayList<ExpandableView> mHeadsUpDisappearView = new ArrayList<>();
    private final ArrayList<ExpandableView> mHeadsUpPositionView = new ArrayList<>();

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiStackStateAnimator(@NotNull NotificationStackScrollLayout notificationStackScrollLayout) {
        super(notificationStackScrollLayout);
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "hostLayout");
    }

    public void startAnimationForEvents(@NotNull ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList, long j) {
        Intrinsics.checkParameterIsNotNull(arrayList, "animationEvents");
        processAnimationEvents(arrayList);
        super.startAnimationForEvents(arrayList, j);
        clearAnimationState();
    }

    private final void processAnimationEvents(ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList) {
        ExpandableViewState expandableViewState = new ExpandableViewState();
        ArrayList arrayList2 = new ArrayList();
        for (T next : arrayList) {
            if (next instanceof PanelAppearDisappearEvent) {
                arrayList2.add(next);
            }
        }
        this.mHasPanelAppearDisappearEvent = !arrayList2.isEmpty();
        ArrayList<HeadsUpPositionEvent> arrayList3 = new ArrayList<>();
        for (T next2 : arrayList) {
            if (next2 instanceof HeadsUpPositionEvent) {
                arrayList3.add(next2);
            }
        }
        ArrayList arrayList4 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(arrayList3, 10));
        for (HeadsUpPositionEvent headsUpPositionEvent : arrayList3) {
            arrayList4.add(headsUpPositionEvent.mChangingView);
        }
        this.mHeadsUpPositionView.addAll(arrayList4);
        ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList5 = new ArrayList<>();
        for (T next3 : arrayList) {
            if (isHeadsUpAnimationType((NotificationStackScrollLayout.AnimationEvent) next3)) {
                arrayList5.add(next3);
            }
        }
        arrayList.removeAll(arrayList5);
        ArrayList arrayList6 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(arrayList5, 10));
        for (NotificationStackScrollLayout.AnimationEvent mapHeadsUpAnimationEvent : arrayList5) {
            arrayList6.add(mapHeadsUpAnimationEvent(mapHeadsUpAnimationEvent));
        }
        arrayList.addAll(arrayList6);
        ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList7 = new ArrayList<>();
        Iterator it = arrayList5.iterator();
        while (true) {
            boolean z = false;
            if (!it.hasNext()) {
                break;
            }
            Object next4 = it.next();
            NotificationStackScrollLayout.AnimationEvent animationEvent = (NotificationStackScrollLayout.AnimationEvent) next4;
            if (animationEvent.animationType == 11 && animationEvent.mChangingView != null) {
                z = true;
            }
            if (z) {
                arrayList7.add(next4);
            }
        }
        for (NotificationStackScrollLayout.AnimationEvent animationEvent2 : arrayList7) {
            ExpandableView expandableView = animationEvent2.mChangingView;
            this.mHeadsUpAppearView.add(expandableView);
            Intrinsics.checkExpressionValueIsNotNull(expandableView, "view");
            expandableViewState.copyFrom(expandableView.getViewState());
            if (animationEvent2.headsUpFromBottom) {
                expandableViewState.yTranslation = (float) this.mHeadsUpAppearHeightBottom;
            } else {
                expandableViewState.yTranslation = -((float) expandableViewState.height);
            }
            expandableViewState.applyToView(expandableView);
        }
        ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList8 = new ArrayList<>();
        for (Object next5 : arrayList5) {
            NotificationStackScrollLayout.AnimationEvent animationEvent3 = (NotificationStackScrollLayout.AnimationEvent) next5;
            if (animationEvent3.animationType == 12 && animationEvent3.mChangingView != null) {
                arrayList8.add(next5);
            }
        }
        for (NotificationStackScrollLayout.AnimationEvent animationEvent4 : arrayList8) {
            ExpandableView expandableView2 = animationEvent4.mChangingView;
            this.mHeadsUpDisappearView.add(expandableView2);
            Intrinsics.checkExpressionValueIsNotNull(expandableView2, "view");
            if (expandableView2.getParent() == null) {
                this.mHostLayout.addTransientView(expandableView2, 0);
                expandableView2.setTransientContainer(this.mHostLayout);
                expandableViewState.initFrom(expandableView2);
                expandableViewState.yTranslation = -((float) expandableView2.getActualHeight());
                Log.e("TEST", " parent " + expandableView2.getParent() + "   tmpState.yTranslation " + expandableViewState.yTranslation + "  height " + expandableView2.getActualHeight() + ' ' + expandableViewState.height);
                this.mAnimationFilter.animateY = true;
                AnimationProperties animationProperties = this.mAnimationProperties;
                animationProperties.duration = (long) 300;
                expandableViewState.animateTo(expandableView2, animationProperties);
                this.mTransientViewsToRemove.add(expandableView2);
            }
        }
    }

    private final void clearAnimationState() {
        this.mHasPanelAppearDisappearEvent = false;
        this.mHeadsUpAppearView.clear();
        this.mHeadsUpDisappearView.clear();
        this.mHeadsUpPositionView.clear();
    }

    /* access modifiers changed from: protected */
    public boolean applyWithAnimation(@Nullable View view) {
        return CollectionsKt___CollectionsKt.contains(this.mHeadsUpAppearView, view) || CollectionsKt___CollectionsKt.contains(this.mHeadsUpDisappearView, view);
    }

    /* access modifiers changed from: protected */
    @Nullable
    public Interpolator getCustomInterpolator(@Nullable View view, @Nullable Property<?, ?> property) {
        AnimatableProperty access$getPROPERTY_SPRING_Y_OFFSET$p = MiuiNotificationAnimationExtensionsKt.PROPERTY_SPRING_Y_OFFSET;
        Intrinsics.checkExpressionValueIsNotNull(access$getPROPERTY_SPRING_Y_OFFSET$p, "PROPERTY_SPRING_Y_OFFSET");
        if (Intrinsics.areEqual((Object) access$getPROPERTY_SPRING_Y_OFFSET$p.getProperty(), (Object) property)) {
            Object tag = view != null ? view.getTag(C0015R$id.miui_child_index_hint) : null;
            return SpringAnimationEvent.Companion.getInterpolatorForIndex$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(tag instanceof Integer ? ((Number) tag).intValue() : 0);
        } else if (this.mHasPanelAppearDisappearEvent && (Intrinsics.areEqual((Object) property, (Object) View.ALPHA) || Intrinsics.areEqual((Object) property, (Object) View.SCALE_X) || Intrinsics.areEqual((Object) property, (Object) View.SCALE_Y))) {
            return PanelAppearDisappearEvent.Companion.getINTERPOLATOR();
        } else {
            if (CollectionsKt___CollectionsKt.contains(this.mHeadsUpPositionView, view)) {
                return HeadsUpPositionEvent.Companion.getINTERPOLATOR();
            }
            if (CollectionsKt___CollectionsKt.contains(this.mHeadsUpAppearView, view) && Intrinsics.areEqual((Object) View.TRANSLATION_Y, (Object) property)) {
                return MiuiNotificationAnimations.INSTANCE.getHEADS_UP_APPEAR_INTERPOLATOR();
            }
            if (!CollectionsKt___CollectionsKt.contains(this.mHeadsUpDisappearView, view) || !Intrinsics.areEqual((Object) View.TRANSLATION_Y, (Object) property)) {
                return super.getCustomInterpolator(view, property);
            }
            return MiuiNotificationAnimations.INSTANCE.getHEADS_UP_DISAPPEAR_INTERPOLATOR();
        }
    }

    public void setHeadsUpAppearHeightBottom(int i) {
        super.setHeadsUpAppearHeightBottom(i);
        this.mHeadsUpAppearHeightBottom = i;
    }

    private final boolean isHeadsUpAnimationType(NotificationStackScrollLayout.AnimationEvent animationEvent) {
        int i = animationEvent.animationType;
        return 11 == i || 12 == i;
    }

    private final NotificationStackScrollLayout.AnimationEvent mapHeadsUpAnimationEvent(NotificationStackScrollLayout.AnimationEvent animationEvent) {
        NotificationStackScrollLayout.AnimationEvent animationEvent2;
        int i = animationEvent.animationType;
        if (i == 11) {
            animationEvent2 = new HeadsUpAppearEvent(animationEvent);
        } else if (i != 12) {
            return animationEvent;
        } else {
            animationEvent2 = new HeadsUpDisappearEvent(animationEvent);
        }
        return animationEvent2;
    }
}

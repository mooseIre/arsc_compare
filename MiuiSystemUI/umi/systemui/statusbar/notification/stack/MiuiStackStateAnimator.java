package com.android.systemui.statusbar.notification.stack;

import android.content.Context;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.AnimatableProperty;
import com.android.systemui.statusbar.notification.row.ExpandableView;
import com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout;
import com.android.systemui.statusbar.notification.unimportant.FoldManager;
import java.util.ArrayList;
import java.util.Iterator;
import kotlin.collections.CollectionsKt__IterablesKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiNotificationAnimationExtensions.kt */
public final class MiuiStackStateAnimator extends StackStateAnimator {
    private final ArrayList<ExpandableView> mChangePositionViews = new ArrayList<>();
    private int mGapHeight;
    private boolean mHasPanelAppearDisappearEvent;
    private boolean mHasSpringAnimationEvent;
    private int mHeadsUpAppearHeightBottom;
    private final ArrayList<ExpandableView> mHeadsUpAppearView = new ArrayList<>();
    private final ArrayList<ExpandableView> mHeadsUpDisappearView = new ArrayList<>();
    private final ArrayList<ExpandableView> mHeadsUpPositionView = new ArrayList<>();
    private int tagId = FoldManager.Companion.getTagId();

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    public MiuiStackStateAnimator(@NotNull NotificationStackScrollLayout notificationStackScrollLayout) {
        super(notificationStackScrollLayout);
        Intrinsics.checkParameterIsNotNull(notificationStackScrollLayout, "hostLayout");
        Context context = notificationStackScrollLayout.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "hostLayout.context");
        this.mGapHeight = context.getResources().getDimensionPixelSize(C0012R$dimen.notification_section_divider_height);
    }

    @Override // com.android.systemui.statusbar.notification.stack.StackStateAnimator
    public void startAnimationForEvents(@NotNull ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList, long j) {
        Intrinsics.checkParameterIsNotNull(arrayList, "animationEvents");
        processHeadsUpAnimationEvents(arrayList);
        processAddRemoveAnimationEvents(arrayList);
        super.startAnimationForEvents(arrayList, j);
        clearAnimationState();
    }

    private final void processHeadsUpAnimationEvents(ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList) {
        ExpandableViewState expandableViewState = new ExpandableViewState();
        ArrayList arrayList2 = new ArrayList();
        for (T t : arrayList) {
            if (t instanceof PanelAppearDisappearEvent) {
                arrayList2.add(t);
            }
        }
        this.mHasPanelAppearDisappearEvent = !arrayList2.isEmpty();
        ArrayList arrayList3 = new ArrayList();
        for (T t2 : arrayList) {
            if (t2 instanceof SpringAnimationEvent) {
                arrayList3.add(t2);
            }
        }
        this.mHasSpringAnimationEvent = !arrayList3.isEmpty();
        ArrayList<HeadsUpPositionEvent> arrayList4 = new ArrayList();
        for (T t3 : arrayList) {
            if (t3 instanceof HeadsUpPositionEvent) {
                arrayList4.add(t3);
            }
        }
        ArrayList arrayList5 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(arrayList4, 10));
        for (HeadsUpPositionEvent headsUpPositionEvent : arrayList4) {
            arrayList5.add(headsUpPositionEvent.mChangingView);
        }
        this.mHeadsUpPositionView.addAll(arrayList5);
        ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList6 = new ArrayList();
        for (T t4 : arrayList) {
            if (isHeadsUpAnimationType(t4)) {
                arrayList6.add(t4);
            }
        }
        arrayList.removeAll(arrayList6);
        ArrayList arrayList7 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(arrayList6, 10));
        for (NotificationStackScrollLayout.AnimationEvent animationEvent : arrayList6) {
            arrayList7.add(mapHeadsUpAnimationEvent(animationEvent));
        }
        arrayList.addAll(arrayList7);
        ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList8 = new ArrayList();
        Iterator it = arrayList6.iterator();
        while (true) {
            boolean z = false;
            if (!it.hasNext()) {
                break;
            }
            Object next = it.next();
            NotificationStackScrollLayout.AnimationEvent animationEvent2 = (NotificationStackScrollLayout.AnimationEvent) next;
            if (animationEvent2.animationType == 11 && animationEvent2.mChangingView != null) {
                z = true;
            }
            if (z) {
                arrayList8.add(next);
            }
        }
        for (NotificationStackScrollLayout.AnimationEvent animationEvent3 : arrayList8) {
            ExpandableView expandableView = animationEvent3.mChangingView;
            this.mHeadsUpAppearView.add(expandableView);
            Intrinsics.checkExpressionValueIsNotNull(expandableView, "view");
            expandableViewState.copyFrom(expandableView.getViewState());
            if (animationEvent3.headsUpFromBottom) {
                expandableViewState.yTranslation = (float) this.mHeadsUpAppearHeightBottom;
            } else {
                expandableViewState.yTranslation = -((float) expandableViewState.height);
            }
            expandableViewState.applyToView(expandableView);
        }
        ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList9 = new ArrayList();
        for (Object obj : arrayList6) {
            NotificationStackScrollLayout.AnimationEvent animationEvent4 = (NotificationStackScrollLayout.AnimationEvent) obj;
            if (animationEvent4.animationType == 12 && animationEvent4.mChangingView != null) {
                arrayList9.add(obj);
            }
        }
        for (NotificationStackScrollLayout.AnimationEvent animationEvent5 : arrayList9) {
            ExpandableView expandableView2 = animationEvent5.mChangingView;
            this.mHeadsUpDisappearView.add(expandableView2);
            Intrinsics.checkExpressionValueIsNotNull(expandableView2, "view");
            if (expandableView2.getParent() == null) {
                this.mHostLayout.addTransientView(expandableView2, 0);
                expandableView2.setTransientContainer(this.mHostLayout);
                expandableViewState.initFrom(expandableView2);
                expandableViewState.yTranslation = -((float) expandableView2.getActualHeight());
                this.mAnimationFilter.animateY = true;
                AnimationProperties animationProperties = this.mAnimationProperties;
                animationProperties.duration = (long) 300;
                expandableViewState.animateTo(expandableView2, animationProperties);
                this.mTransientViewsToRemove.add(expandableView2);
            }
        }
    }

    private final void processAddRemoveAnimationEvents(ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList) {
        float f;
        if (FoldManager.Companion.isUnimportantAnimating()) {
            ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList2 = new ArrayList();
            for (T t : arrayList) {
                if (isUnimportAnimationType(t)) {
                    arrayList2.add(t);
                }
            }
            arrayList.removeAll(arrayList2);
            ArrayList arrayList3 = new ArrayList(CollectionsKt__IterablesKt.collectionSizeOrDefault(arrayList2, 10));
            for (NotificationStackScrollLayout.AnimationEvent animationEvent : arrayList2) {
                arrayList3.add(mapUnimportantAnimationEvent(animationEvent));
            }
            arrayList.addAll(arrayList3);
            ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList4 = new ArrayList();
            for (T t2 : arrayList) {
                T t3 = t2;
                if (isUnimportantTransferedType(t3) && t3.mChangingView != null) {
                    arrayList4.add(t2);
                }
            }
            float f2 = 0.0f;
            int i = 0;
            int i2 = 0;
            float f3 = 0.0f;
            for (NotificationStackScrollLayout.AnimationEvent animationEvent2 : arrayList4) {
                ExpandableView expandableView = animationEvent2.mChangingView;
                if (isUnimportantAppearType(animationEvent2)) {
                    if (i2 < 12) {
                        i2++;
                        Intrinsics.checkExpressionValueIsNotNull(expandableView, "view");
                        f2 += (float) (expandableView.getActualHeight() + this.mGapHeight);
                    }
                } else if (i < 12) {
                    i++;
                    Intrinsics.checkExpressionValueIsNotNull(expandableView, "view");
                    f3 += (float) (expandableView.getActualHeight() + this.mGapHeight);
                }
            }
            float min = Math.min(f2 + ((float) this.mGapHeight), 300.0f);
            float min2 = Math.min(f3 + ((float) this.mGapHeight), 300.0f);
            ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList5 = new ArrayList();
            for (T t4 : arrayList) {
                T t5 = t4;
                if (isUnimportantTransferedType(t5) && t5.mChangingView != null) {
                    arrayList5.add(t4);
                }
            }
            for (NotificationStackScrollLayout.AnimationEvent animationEvent3 : arrayList5) {
                ExpandableView expandableView2 = animationEvent3.mChangingView;
                expandableView2.clearAnimation();
                if (isUnimportantUpEvent(animationEvent3)) {
                    f = isUnimportantAppearType(animationEvent3) ? FoldManager.Companion.getHeaderDif() + min2 : min2;
                } else if (isUnimportantAppearType(animationEvent3)) {
                    f = -min;
                } else {
                    f = (-min) - FoldManager.Companion.getHeaderDif();
                }
                expandableView2.setTag(this.tagId, Float.valueOf(f));
            }
        }
        ArrayList<NotificationStackScrollLayout.AnimationEvent> arrayList6 = new ArrayList();
        for (T t6 : arrayList) {
            T t7 = t6;
            if (t7.animationType == 6 && t7.mChangingView != null) {
                arrayList6.add(t6);
            }
        }
        ArrayList<ExpandableView> arrayList7 = this.mChangePositionViews;
        for (NotificationStackScrollLayout.AnimationEvent animationEvent4 : arrayList6) {
            ExpandableView expandableView3 = animationEvent4.mChangingView;
            Intrinsics.checkExpressionValueIsNotNull(expandableView3, "it.mChangingView");
            arrayList7.add(expandableView3);
        }
    }

    private final void clearAnimationState() {
        this.mHasPanelAppearDisappearEvent = false;
        this.mHasSpringAnimationEvent = false;
        this.mChangePositionViews.clear();
        this.mHeadsUpAppearView.clear();
        this.mHeadsUpDisappearView.clear();
        this.mHeadsUpPositionView.clear();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.StackStateAnimator
    public long calculateChildAnimationDelay(@Nullable ExpandableViewState expandableViewState, int i) {
        AnimationFilter animationFilter = this.mAnimationFilter;
        if (animationFilter.customDelay != ((long) -1) || animationFilter.hasGoToFullShadeEvent) {
            return super.calculateChildAnimationDelay(expandableViewState, i);
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.StackStateAnimator
    public boolean applyWithAnimation(@Nullable View view) {
        return (CollectionsKt___CollectionsKt.contains(this.mHeadsUpAppearView, view)) || (CollectionsKt___CollectionsKt.contains(this.mHeadsUpDisappearView, view)) || this.mHasPanelAppearDisappearEvent || this.mHasSpringAnimationEvent;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.stack.StackStateAnimator
    @Nullable
    public Interpolator getCustomInterpolator(@Nullable View view, @Nullable Property<?, ?> property) {
        AnimatableProperty animatableProperty = MiuiNotificationAnimationExtensionsKt.PROPERTY_SPRING_Y_OFFSET;
        Intrinsics.checkExpressionValueIsNotNull(animatableProperty, "PROPERTY_SPRING_Y_OFFSET");
        if (Intrinsics.areEqual(animatableProperty.getProperty(), property)) {
            Object tag = view != null ? view.getTag(C0015R$id.miui_child_index_hint) : null;
            return SpringAnimationEvent.Companion.getInterpolatorForIndex$packages__apps__MiuiSystemUI__packages__SystemUI__android_common__MiuiSystemUI_core(tag instanceof Integer ? ((Number) tag).intValue() : 0);
        } else if (this.mHasPanelAppearDisappearEvent && (Intrinsics.areEqual(property, View.ALPHA) || Intrinsics.areEqual(property, View.SCALE_X) || Intrinsics.areEqual(property, View.SCALE_Y))) {
            return PanelAppearDisappearEvent.Companion.getINTERPOLATOR();
        } else {
            if (CollectionsKt___CollectionsKt.contains(this.mHeadsUpPositionView, view)) {
                return HeadsUpPositionEvent.Companion.getINTERPOLATOR();
            }
            if ((CollectionsKt___CollectionsKt.contains(this.mHeadsUpAppearView, view)) && Intrinsics.areEqual(View.TRANSLATION_Y, property)) {
                return MiuiNotificationAnimations.INSTANCE.getHEADS_UP_APPEAR_INTERPOLATOR();
            }
            if ((CollectionsKt___CollectionsKt.contains(this.mHeadsUpDisappearView, view)) && Intrinsics.areEqual(View.TRANSLATION_Y, property)) {
                return MiuiNotificationAnimations.INSTANCE.getHEADS_UP_DISAPPEAR_INTERPOLATOR();
            }
            if (!(CollectionsKt___CollectionsKt.contains(this.mChangePositionViews, view)) || !Intrinsics.areEqual(View.TRANSLATION_Y, property)) {
                return super.getCustomInterpolator(view, property);
            }
            return Interpolators.DECELERATE_QUINT;
        }
    }

    @Override // com.android.systemui.statusbar.notification.stack.StackStateAnimator
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

    private final boolean isUnimportAnimationType(NotificationStackScrollLayout.AnimationEvent animationEvent) {
        int i = animationEvent.animationType;
        return i == 0 || 1 == i;
    }

    private final boolean isUnimportantTransferedType(NotificationStackScrollLayout.AnimationEvent animationEvent) {
        return isUnimportantAppearType(animationEvent) || isUnimportantDisappearType(animationEvent);
    }

    private final boolean isUnimportantAppearType(NotificationStackScrollLayout.AnimationEvent animationEvent) {
        int i = animationEvent.animationType;
        return 23 == i || 25 == i;
    }

    private final boolean isUnimportantDisappearType(NotificationStackScrollLayout.AnimationEvent animationEvent) {
        int i = animationEvent.animationType;
        return 24 == i || 26 == i;
    }

    private final boolean isUnimportantUpEvent(NotificationStackScrollLayout.AnimationEvent animationEvent) {
        int i = animationEvent.animationType;
        return 23 == i || 24 == i;
    }

    private final NotificationStackScrollLayout.AnimationEvent mapUnimportantAnimationEvent(NotificationStackScrollLayout.AnimationEvent animationEvent) {
        NotificationStackScrollLayout.AnimationEvent animationEvent2;
        int i = animationEvent.animationType;
        if (i != 0) {
            if (i != 1) {
                return animationEvent;
            }
            if (FoldManager.Companion.isShowingUnimportant()) {
                animationEvent2 = new UnimportantUpRemoveEvent(animationEvent);
            } else {
                animationEvent2 = new UnimportantDownRemoveEvent(animationEvent);
            }
        } else if (FoldManager.Companion.isShowingUnimportant()) {
            animationEvent2 = new UnimportantUpAddEvent(animationEvent);
        } else {
            animationEvent2 = new UnimportantDownAddEvent(animationEvent);
        }
        return animationEvent2;
    }
}

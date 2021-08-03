package com.android.systemui.statusbar.notification.row;

import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import com.android.systemui.statusbar.notification.RowAnimationUtils;
import com.android.systemui.statusbar.notification.stack.ExpandableViewState;
import com.miui.systemui.animation.AnimationListenerFolmeConverter;
import java.util.List;
import kotlin.Lazy;
import kotlin.LazyKt__LazyJVMKt;
import kotlin.collections.CollectionsKt___CollectionsKt;
import kotlin.jvm.internal.PropertyReference1Impl;
import kotlin.jvm.internal.Reflection;
import kotlin.reflect.KProperty;
import kotlin.sequences.Sequence;
import kotlin.sequences.SequencesKt___SequencesKt;
import miuix.animation.Folme;
import miuix.animation.IStateStyle;
import miuix.animation.base.AnimConfig;
import miuix.animation.controller.AnimState;
import miuix.animation.property.FloatProperty;
import miuix.animation.property.ViewProperty;
import org.jetbrains.annotations.Nullable;

/* compiled from: MiuiAnimatedNotificationRowBase.kt */
public class MiuiAnimatedNotificationRowBase extends ExpandableNotificationRow {
    static final /* synthetic */ KProperty[] $$delegatedProperties;
    private static final AnimState STATE_HIDDEN;
    private static final AnimState STATE_VISIBLE;
    private static final MiuiAnimatedNotificationRowBase$Companion$TRANSITION_ALPHA$1 TRANSITION_ALPHA = new MiuiAnimatedNotificationRowBase$Companion$TRANSITION_ALPHA$1("TransitionAlpha");
    private final AnimConfig mAnimConfig;
    private final Lazy mFolme$delegate = LazyKt__LazyJVMKt.lazy(new MiuiAnimatedNotificationRowBase$mFolme$2(this));

    private final IStateStyle getMFolme() {
        Lazy lazy = this.mFolme$delegate;
        KProperty kProperty = $$delegatedProperties[0];
        return (IStateStyle) lazy.getValue();
    }

    public MiuiAnimatedNotificationRowBase(@Nullable Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        AnimConfig animConfig = new AnimConfig();
        animConfig.setEase(-2, 0.6f, 0.4f);
        animConfig.addListeners(new MiuiAnimatedNotificationRowBase$mAnimConfig$1(this));
        this.mAnimConfig = animConfig;
    }

    static {
        PropertyReference1Impl propertyReference1Impl = new PropertyReference1Impl(Reflection.getOrCreateKotlinClass(MiuiAnimatedNotificationRowBase.class), "mFolme", "getMFolme()Lmiuix/animation/IStateStyle;");
        Reflection.property1(propertyReference1Impl);
        $$delegatedProperties = new KProperty[]{propertyReference1Impl};
        AnimState animState = new AnimState("state_hide");
        animState.add((FloatProperty) TRANSITION_ALPHA, 0.0f, new long[0]);
        animState.add(ViewProperty.SCALE_X, 0.9f, new long[0]);
        animState.add(ViewProperty.SCALE_Y, 0.9f, new long[0]);
        STATE_HIDDEN = animState;
        AnimState animState2 = new AnimState("state_show");
        animState2.add((FloatProperty) TRANSITION_ALPHA, 1.0f, new long[0]);
        animState2.add(ViewProperty.SCALE_X, 1.0f, new long[0]);
        animState2.add(ViewProperty.SCALE_Y, 1.0f, new long[0]);
        STATE_VISIBLE = animState2;
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView, com.android.systemui.statusbar.notification.row.ExpandableView
    public void performAddAnimation(long j, long j2, boolean z) {
        if (z) {
            super.performAddAnimation(j, j2, z);
            return;
        }
        setTransitionAlpha(0.0f);
        getMFolme().cancel();
        getMFolme().fromTo(STATE_HIDDEN, STATE_VISIBLE, this.mAnimConfig);
    }

    @Override // com.android.systemui.statusbar.notification.row.ExpandableNotificationRow, com.android.systemui.statusbar.notification.row.ActivatableNotificationView, com.android.systemui.statusbar.notification.row.ExpandableView
    public long performRemoveAnimation(long j, long j2, float f, boolean z, float f2, @Nullable Runnable runnable, @Nullable AnimatorListenerAdapter animatorListenerAdapter) {
        if (z) {
            return super.performRemoveAnimation(j, j2, f, z, f2, runnable, animatorListenerAdapter);
        }
        getMFolme().cancel();
        IStateStyle mFolme = getMFolme();
        AnimState animState = STATE_HIDDEN;
        AnimConfig[] animConfigArr = new AnimConfig[1];
        AnimConfig animConfig = new AnimConfig(this.mAnimConfig);
        if (animatorListenerAdapter != null) {
            animConfig.addListeners(new AnimationListenerFolmeConverter(animatorListenerAdapter));
        }
        animConfig.addListeners(new MiuiAnimatedNotificationRowBase$performRemoveAnimation$$inlined$apply$lambda$1(animatorListenerAdapter, runnable));
        animConfigArr[0] = animConfig;
        mFolme.to(animState, animConfigArr);
        return 0;
    }

    @Override // com.android.systemui.statusbar.notification.row.ActivatableNotificationView
    public void cancelAppearDrawing() {
        super.cancelAppearDrawing();
        resetTouchAnimation();
        getMFolme().cancel();
        getMFolme().setTo(STATE_VISIBLE);
        setTransitionAlpha(1.0f);
    }

    private final void resetTouchAnimation() {
        Sequence sequence;
        Sequence<ExpandableNotificationRow> sequence2;
        Folme.useValue(RowAnimationUtils.INSTANCE.getFolmeTarget(this)).cancel();
        ExpandableViewState viewState = getViewState();
        if (viewState != null) {
            viewState.setTouchAnimating(false);
        }
        List<ExpandableNotificationRow> attachedChildren = getAttachedChildren();
        if (!(attachedChildren == null || (sequence = CollectionsKt___CollectionsKt.asSequence(attachedChildren)) == null || (sequence2 = SequencesKt___SequencesKt.filterNotNull(sequence)) == null)) {
            for (ExpandableNotificationRow expandableNotificationRow : sequence2) {
                Folme.useValue(RowAnimationUtils.INSTANCE.getFolmeTarget(expandableNotificationRow)).cancel();
                ExpandableViewState viewState2 = expandableNotificationRow.getViewState();
                if (viewState2 != null) {
                    viewState2.setTouchAnimating(false);
                }
            }
        }
    }
}

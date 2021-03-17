package com.android.systemui.statusbar.notification;

import android.text.Layout;
import android.util.Pools;
import android.view.View;
import android.widget.TextView;
import com.android.internal.widget.IMessagingLayout;
import com.android.internal.widget.MessagingGroup;
import com.android.internal.widget.MessagingImageMessage;
import com.android.internal.widget.MessagingLinearLayout;
import com.android.internal.widget.MessagingPropertyAnimator;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.notification.TransformState;
import java.util.ArrayList;
import java.util.HashMap;

public class MessagingLayoutTransformState extends TransformState {
    private static Pools.SimplePool<MessagingLayoutTransformState> sInstancePool = new Pools.SimplePool<>(40);
    private HashMap<MessagingGroup, MessagingGroup> mGroupMap = new HashMap<>();
    private MessagingLinearLayout mMessageContainer;
    private IMessagingLayout mMessagingLayout;
    private float mRelativeTranslationOffset;

    public static MessagingLayoutTransformState obtain() {
        MessagingLayoutTransformState messagingLayoutTransformState = (MessagingLayoutTransformState) sInstancePool.acquire();
        if (messagingLayoutTransformState != null) {
            return messagingLayoutTransformState;
        }
        return new MessagingLayoutTransformState();
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void initFrom(View view, TransformState.TransformInfo transformInfo) {
        super.initFrom(view, transformInfo);
        MessagingLinearLayout messagingLinearLayout = this.mTransformedView;
        if (messagingLinearLayout instanceof MessagingLinearLayout) {
            MessagingLinearLayout messagingLinearLayout2 = messagingLinearLayout;
            this.mMessageContainer = messagingLinearLayout2;
            this.mMessagingLayout = messagingLinearLayout2.getMessagingLayout();
            this.mRelativeTranslationOffset = view.getContext().getResources().getDisplayMetrics().density * 8.0f;
        }
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public boolean transformViewTo(TransformState transformState, float f) {
        if (!(transformState instanceof MessagingLayoutTransformState)) {
            return super.transformViewTo(transformState, f);
        }
        transformViewInternal((MessagingLayoutTransformState) transformState, f, true);
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void transformViewFrom(TransformState transformState, float f) {
        if (transformState instanceof MessagingLayoutTransformState) {
            transformViewInternal((MessagingLayoutTransformState) transformState, f, false);
        } else {
            super.transformViewFrom(transformState, f);
        }
    }

    private void transformViewInternal(MessagingLayoutTransformState messagingLayoutTransformState, float f, boolean z) {
        float f2;
        float f3;
        float f4;
        ensureVisible();
        ArrayList<MessagingGroup> filterHiddenGroups = filterHiddenGroups(this.mMessagingLayout.getMessagingGroups());
        HashMap<MessagingGroup, MessagingGroup> findPairs = findPairs(filterHiddenGroups, filterHiddenGroups(messagingLayoutTransformState.mMessagingLayout.getMessagingGroups()));
        MessagingGroup messagingGroup = null;
        float f5 = 0.0f;
        for (int size = filterHiddenGroups.size() - 1; size >= 0; size--) {
            MessagingGroup messagingGroup2 = filterHiddenGroups.get(size);
            MessagingGroup messagingGroup3 = findPairs.get(messagingGroup2);
            if (!isGone(messagingGroup2)) {
                if (messagingGroup3 != null) {
                    int transformGroups = transformGroups(messagingGroup2, messagingGroup3, f, z);
                    if (messagingGroup == null) {
                        if (z) {
                            f5 = messagingGroup3.getAvatar().getTranslationY() - ((float) transformGroups);
                        } else {
                            f5 = messagingGroup2.getAvatar().getTranslationY();
                        }
                        messagingGroup = messagingGroup2;
                    }
                } else {
                    if (messagingGroup != null) {
                        adaptGroupAppear(messagingGroup2, f, f5, z);
                        float top = ((float) messagingGroup2.getTop()) + f5;
                        if (!this.mTransformInfo.isAnimating()) {
                            float f6 = ((float) (-messagingGroup2.getHeight())) * 0.5f;
                            f4 = top - f6;
                            f3 = Math.abs(f6);
                        } else {
                            float f7 = ((float) (-messagingGroup2.getHeight())) * 0.75f;
                            f4 = top - f7;
                            f3 = Math.abs(f7) + ((float) messagingGroup2.getTop());
                        }
                        f2 = Math.max(0.0f, Math.min(1.0f, f4 / f3));
                        if (z) {
                            f2 = 1.0f - f2;
                        }
                    } else {
                        f2 = f;
                    }
                    if (z) {
                        disappear(messagingGroup2, f2);
                    } else {
                        appear(messagingGroup2, f2);
                    }
                }
            }
        }
    }

    private void appear(MessagingGroup messagingGroup, float f) {
        MessagingLinearLayout messageContainer = messagingGroup.getMessageContainer();
        for (int i = 0; i < messageContainer.getChildCount(); i++) {
            View childAt = messageContainer.getChildAt(i);
            if (!isGone(childAt)) {
                appear(childAt, f);
                setClippingDeactivated(childAt, true);
            }
        }
        appear(messagingGroup.getAvatar(), f);
        appear(messagingGroup.getSenderView(), f);
        appear((View) messagingGroup.getIsolatedMessage(), f);
        setClippingDeactivated(messagingGroup.getSenderView(), true);
        setClippingDeactivated(messagingGroup.getAvatar(), true);
    }

    private void adaptGroupAppear(MessagingGroup messagingGroup, float f, float f2, boolean z) {
        float f3;
        if (z) {
            f3 = f * this.mRelativeTranslationOffset;
        } else {
            f3 = (1.0f - f) * this.mRelativeTranslationOffset;
        }
        if (messagingGroup.getSenderView().getVisibility() != 8) {
            f3 *= 0.5f;
        }
        messagingGroup.getMessageContainer().setTranslationY(f3);
        messagingGroup.getSenderView().setTranslationY(f3);
        messagingGroup.setTranslationY(f2 * 0.9f);
    }

    private void disappear(MessagingGroup messagingGroup, float f) {
        MessagingLinearLayout messageContainer = messagingGroup.getMessageContainer();
        for (int i = 0; i < messageContainer.getChildCount(); i++) {
            View childAt = messageContainer.getChildAt(i);
            if (!isGone(childAt)) {
                disappear(childAt, f);
                setClippingDeactivated(childAt, true);
            }
        }
        disappear(messagingGroup.getAvatar(), f);
        disappear(messagingGroup.getSenderView(), f);
        disappear((View) messagingGroup.getIsolatedMessage(), f);
        setClippingDeactivated(messagingGroup.getSenderView(), true);
        setClippingDeactivated(messagingGroup.getAvatar(), true);
    }

    private void appear(View view, float f) {
        if (view != null && view.getVisibility() != 8) {
            TransformState createFrom = TransformState.createFrom(view, this.mTransformInfo);
            createFrom.appear(f, null);
            createFrom.recycle();
        }
    }

    private void disappear(View view, float f) {
        if (view != null && view.getVisibility() != 8) {
            TransformState createFrom = TransformState.createFrom(view, this.mTransformInfo);
            createFrom.disappear(f, null);
            createFrom.recycle();
        }
    }

    private ArrayList<MessagingGroup> filterHiddenGroups(ArrayList<MessagingGroup> arrayList) {
        ArrayList<MessagingGroup> arrayList2 = new ArrayList<>(arrayList);
        int i = 0;
        while (i < arrayList2.size()) {
            if (isGone(arrayList2.get(i))) {
                arrayList2.remove(i);
                i--;
            }
            i++;
        }
        return arrayList2;
    }

    private boolean hasEllipses(TextView textView) {
        Layout layout = textView.getLayout();
        if (layout == null || layout.getEllipsisCount(layout.getLineCount() - 1) <= 0) {
            return false;
        }
        return true;
    }

    private boolean needsReflow(TextView textView, TextView textView2) {
        return hasEllipses(textView) != hasEllipses(textView2);
    }

    /* JADX WARNING: Removed duplicated region for block: B:28:0x00cf  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00ed  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00fe  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int transformGroups(com.android.internal.widget.MessagingGroup r22, com.android.internal.widget.MessagingGroup r23, float r24, boolean r25) {
        /*
        // Method dump skipped, instructions count: 291
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.notification.MessagingLayoutTransformState.transformGroups(com.android.internal.widget.MessagingGroup, com.android.internal.widget.MessagingGroup, float, boolean):int");
    }

    private int transformView(float f, boolean z, View view, View view2, boolean z2, boolean z3) {
        TransformState createFrom = TransformState.createFrom(view, this.mTransformInfo);
        if (z3) {
            createFrom.setDefaultInterpolator(Interpolators.LINEAR);
        }
        int i = 0;
        createFrom.setIsSameAsAnyView(z2 && !isGone(view2));
        if (z) {
            if (view2 != null) {
                TransformState createFrom2 = TransformState.createFrom(view2, this.mTransformInfo);
                if (!isGone(view2)) {
                    createFrom.transformViewTo(createFrom2, f);
                } else {
                    if (!isGone(view)) {
                        createFrom.disappear(f, null);
                    }
                    createFrom.transformViewVerticalTo(createFrom2, f);
                }
                i = createFrom.getLaidOutLocationOnScreen()[1] - createFrom2.getLaidOutLocationOnScreen()[1];
                createFrom2.recycle();
            } else {
                createFrom.disappear(f, null);
            }
        } else if (view2 != null) {
            TransformState createFrom3 = TransformState.createFrom(view2, this.mTransformInfo);
            if (!isGone(view2)) {
                createFrom.transformViewFrom(createFrom3, f);
            } else {
                if (!isGone(view)) {
                    createFrom.appear(f, null);
                }
                createFrom.transformViewVerticalFrom(createFrom3, f);
            }
            i = createFrom.getLaidOutLocationOnScreen()[1] - createFrom3.getLaidOutLocationOnScreen()[1];
            createFrom3.recycle();
        } else {
            createFrom.appear(f, null);
        }
        createFrom.recycle();
        return i;
    }

    private HashMap<MessagingGroup, MessagingGroup> findPairs(ArrayList<MessagingGroup> arrayList, ArrayList<MessagingGroup> arrayList2) {
        this.mGroupMap.clear();
        int i = Integer.MAX_VALUE;
        for (int size = arrayList.size() - 1; size >= 0; size--) {
            MessagingGroup messagingGroup = arrayList.get(size);
            MessagingGroup messagingGroup2 = null;
            int i2 = 0;
            for (int min = Math.min(arrayList2.size(), i) - 1; min >= 0; min--) {
                MessagingGroup messagingGroup3 = arrayList2.get(min);
                int calculateGroupCompatibility = messagingGroup.calculateGroupCompatibility(messagingGroup3);
                if (calculateGroupCompatibility > i2) {
                    i = min;
                    messagingGroup2 = messagingGroup3;
                    i2 = calculateGroupCompatibility;
                }
            }
            if (messagingGroup2 != null) {
                this.mGroupMap.put(messagingGroup, messagingGroup2);
            }
        }
        return this.mGroupMap;
    }

    private boolean isGone(View view) {
        if (view == null || view.getVisibility() == 8 || view.getParent() == null || view.getWidth() == 0) {
            return true;
        }
        MessagingLinearLayout.LayoutParams layoutParams = view.getLayoutParams();
        if (!(layoutParams instanceof MessagingLinearLayout.LayoutParams) || !layoutParams.hide) {
            return false;
        }
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void setVisible(boolean z, boolean z2) {
        super.setVisible(z, z2);
        resetTransformedView();
        ArrayList messagingGroups = this.mMessagingLayout.getMessagingGroups();
        for (int i = 0; i < messagingGroups.size(); i++) {
            MessagingGroup messagingGroup = (MessagingGroup) messagingGroups.get(i);
            if (!isGone(messagingGroup)) {
                MessagingLinearLayout messageContainer = messagingGroup.getMessageContainer();
                for (int i2 = 0; i2 < messageContainer.getChildCount(); i2++) {
                    setVisible(messageContainer.getChildAt(i2), z, z2);
                }
                setVisible(messagingGroup.getAvatar(), z, z2);
                setVisible(messagingGroup.getSenderView(), z, z2);
                MessagingImageMessage isolatedMessage = messagingGroup.getIsolatedMessage();
                if (isolatedMessage != null) {
                    setVisible(isolatedMessage, z, z2);
                }
            }
        }
    }

    private void setVisible(View view, boolean z, boolean z2) {
        if (!isGone(view) && !MessagingPropertyAnimator.isAnimatingAlpha(view)) {
            TransformState createFrom = TransformState.createFrom(view, this.mTransformInfo);
            createFrom.setVisible(z, z2);
            createFrom.recycle();
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public void resetTransformedView() {
        super.resetTransformedView();
        ArrayList messagingGroups = this.mMessagingLayout.getMessagingGroups();
        for (int i = 0; i < messagingGroups.size(); i++) {
            MessagingGroup messagingGroup = (MessagingGroup) messagingGroups.get(i);
            if (!isGone(messagingGroup)) {
                MessagingLinearLayout messageContainer = messagingGroup.getMessageContainer();
                for (int i2 = 0; i2 < messageContainer.getChildCount(); i2++) {
                    View childAt = messageContainer.getChildAt(i2);
                    if (!isGone(childAt)) {
                        resetTransformedView(childAt);
                        setClippingDeactivated(childAt, false);
                    }
                }
                resetTransformedView(messagingGroup.getAvatar());
                resetTransformedView(messagingGroup.getSenderView());
                MessagingImageMessage isolatedMessage = messagingGroup.getIsolatedMessage();
                if (isolatedMessage != null) {
                    resetTransformedView(isolatedMessage);
                }
                setClippingDeactivated(messagingGroup.getAvatar(), false);
                setClippingDeactivated(messagingGroup.getSenderView(), false);
                messagingGroup.setTranslationY(0.0f);
                messagingGroup.getMessageContainer().setTranslationY(0.0f);
                messagingGroup.getSenderView().setTranslationY(0.0f);
            }
            messagingGroup.setClippingDisabled(false);
            messagingGroup.updateClipRect();
        }
        this.mMessagingLayout.setMessagingClippingDisabled(false);
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void prepareFadeIn() {
        super.prepareFadeIn();
        setVisible(true, false);
    }

    private void resetTransformedView(View view) {
        TransformState createFrom = TransformState.createFrom(view, this.mTransformInfo);
        createFrom.resetTransformedView();
        createFrom.recycle();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public void reset() {
        super.reset();
        this.mMessageContainer = null;
        this.mMessagingLayout = null;
    }

    @Override // com.android.systemui.statusbar.notification.TransformState
    public void recycle() {
        super.recycle();
        this.mGroupMap.clear();
        sInstancePool.release(this);
    }
}

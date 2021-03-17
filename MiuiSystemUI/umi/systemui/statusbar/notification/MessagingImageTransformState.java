package com.android.systemui.statusbar.notification;

import android.util.Pools;
import android.view.View;
import com.android.internal.widget.MessagingImageMessage;
import com.android.systemui.C0015R$id;
import com.android.systemui.statusbar.ViewTransformationHelper;
import com.android.systemui.statusbar.notification.TransformState;

public class MessagingImageTransformState extends ImageTransformState {
    private static final int START_ACTUAL_HEIGHT = C0015R$id.transformation_start_actual_height;
    private static final int START_ACTUAL_WIDTH = C0015R$id.transformation_start_actual_width;
    private static Pools.SimplePool<MessagingImageTransformState> sInstancePool = new Pools.SimplePool<>(40);
    private MessagingImageMessage mImageMessage;

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public boolean transformScale(TransformState transformState) {
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.ImageTransformState, com.android.systemui.statusbar.notification.TransformState
    public void initFrom(View view, TransformState.TransformInfo transformInfo) {
        super.initFrom(view, transformInfo);
        this.mImageMessage = (MessagingImageMessage) view;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.ImageTransformState, com.android.systemui.statusbar.notification.TransformState
    public boolean sameAs(TransformState transformState) {
        if (super.sameAs(transformState)) {
            return true;
        }
        if (transformState instanceof MessagingImageTransformState) {
            return this.mImageMessage.sameAs(((MessagingImageTransformState) transformState).mImageMessage);
        }
        return false;
    }

    public static MessagingImageTransformState obtain() {
        MessagingImageTransformState messagingImageTransformState = (MessagingImageTransformState) sInstancePool.acquire();
        if (messagingImageTransformState != null) {
            return messagingImageTransformState;
        }
        return new MessagingImageTransformState();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public void transformViewFrom(TransformState transformState, int i, ViewTransformationHelper.CustomTransformation customTransformation, float f) {
        super.transformViewFrom(transformState, i, customTransformation, f);
        float interpolation = this.mDefaultInterpolator.getInterpolation(f);
        if ((transformState instanceof MessagingImageTransformState) && sameAs(transformState)) {
            MessagingImageMessage messagingImageMessage = ((MessagingImageTransformState) transformState).mImageMessage;
            if (f == 0.0f) {
                setStartActualWidth(messagingImageMessage.getActualWidth());
                setStartActualHeight(messagingImageMessage.getActualHeight());
            }
            MessagingImageMessage messagingImageMessage2 = this.mImageMessage;
            messagingImageMessage2.setActualWidth((int) NotificationUtils.interpolate((float) getStartActualWidth(), (float) messagingImageMessage2.getWidth(), interpolation));
            MessagingImageMessage messagingImageMessage3 = this.mImageMessage;
            messagingImageMessage3.setActualHeight((int) NotificationUtils.interpolate((float) getStartActualHeight(), (float) messagingImageMessage3.getHeight(), interpolation));
        }
    }

    public int getStartActualWidth() {
        Object tag = this.mTransformedView.getTag(START_ACTUAL_WIDTH);
        if (tag == null) {
            return -1;
        }
        return ((Integer) tag).intValue();
    }

    public void setStartActualWidth(int i) {
        this.mTransformedView.setTag(START_ACTUAL_WIDTH, Integer.valueOf(i));
    }

    public int getStartActualHeight() {
        Object tag = this.mTransformedView.getTag(START_ACTUAL_HEIGHT);
        if (tag == null) {
            return -1;
        }
        return ((Integer) tag).intValue();
    }

    public void setStartActualHeight(int i) {
        this.mTransformedView.setTag(START_ACTUAL_HEIGHT, Integer.valueOf(i));
    }

    @Override // com.android.systemui.statusbar.notification.ImageTransformState, com.android.systemui.statusbar.notification.TransformState
    public void recycle() {
        super.recycle();
        sInstancePool.release(this);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.TransformState
    public void resetTransformedView() {
        super.resetTransformedView();
        MessagingImageMessage messagingImageMessage = this.mImageMessage;
        messagingImageMessage.setActualWidth(messagingImageMessage.getWidth());
        MessagingImageMessage messagingImageMessage2 = this.mImageMessage;
        messagingImageMessage2.setActualHeight(messagingImageMessage2.getHeight());
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.ImageTransformState, com.android.systemui.statusbar.notification.TransformState
    public void reset() {
        super.reset();
        this.mImageMessage = null;
    }
}

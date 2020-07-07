package com.android.systemui.recents.misc;

import android.view.View;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.android.systemui.recents.Recents;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.RecentsView;
import com.android.systemui.recents.views.TaskStackView;
import com.android.systemui.recents.views.TaskView;

public class SpringAnimationUtils {
    private static final SpringAnimationUtils sInstance = new SpringAnimationUtils();
    private final ViewState HEADER_HIDE;
    private final ViewState HEADER_NORMAL;
    private final ViewState HEADER_SCALE_DOWN;
    private final ViewState RECENTS_VIEW_HIDE;
    private final ViewState RECENTS_VIEW_NORMAL;
    private final ViewState RECENTS_VIEW_SCALE_DOWN;
    private final ViewState TASK_VIEW_HIDE;
    private final ViewState TASK_VIEW_NORMAL;
    private final ViewState THUMBNAIL_NORMAL;
    private final ViewState THUMBNAIL_SCALE_DOWN;
    private final ViewState THUMBNAIL_SCALE_UP;
    private final ViewState THUMBNAIL_SCALE_UP_PLUS = new ViewState(1.0f, 1.1f, 1.1f, 0.0f, 0.0f);
    private boolean mIsCanUpdateSpringAnim;

    private class ViewState {
        float alpha;
        float scaleX;
        float scaleY;
        float translationX;
        float translationY;

        public ViewState(float f, float f2, float f3, float f4, float f5) {
            this.alpha = f;
            this.scaleX = f2;
            this.scaleY = f3;
            this.translationX = f4;
            this.translationY = f5;
        }
    }

    private SpringAnimationUtils() {
        this.THUMBNAIL_SCALE_UP = new ViewState(1.0f, 1.05f, 1.05f, 0.0f, 0.0f);
        this.THUMBNAIL_NORMAL = new ViewState(1.0f, 1.0f, 1.0f, 0.0f, 0.0f);
        this.THUMBNAIL_SCALE_DOWN = new ViewState(1.0f, 0.9f, 0.9f, 0.0f, 0.0f);
        this.HEADER_SCALE_DOWN = new ViewState(0.0f, 0.95f, 0.95f, 0.0f, 0.0f);
        this.HEADER_NORMAL = new ViewState(1.0f, 1.0f, 1.0f, 0.0f, 0.0f);
        this.HEADER_HIDE = new ViewState(0.0f, 1.0f, 1.0f, 0.0f, 0.0f);
        this.TASK_VIEW_HIDE = new ViewState(0.0f, 0.95f, 0.95f, 0.0f, 0.0f);
        this.TASK_VIEW_NORMAL = new ViewState(1.0f, 1.0f, 1.0f, 0.0f, 0.0f);
        this.RECENTS_VIEW_SCALE_DOWN = new ViewState(0.8f, 0.92f, 0.92f, 0.0f, 0.0f);
        this.RECENTS_VIEW_NORMAL = new ViewState(1.0f, 1.0f, 1.0f, 0.0f, 0.0f);
        this.RECENTS_VIEW_HIDE = new ViewState(0.0f, 1.0f, 1.0f, 0.0f, 0.0f);
        this.mIsCanUpdateSpringAnim = false;
    }

    public static SpringAnimationUtils getInstance() {
        return sInstance;
    }

    public float calculateStiffFromResponse(float f) {
        if (f <= 0.0f) {
            f = 0.32f;
        }
        return (float) Math.pow((3.141592653589793d / ((double) Math.max(0.05f, f))) * 2.0d, 2.0d);
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private androidx.dynamicanimation.animation.SpringAnimation getSpringAnimation(com.android.systemui.recents.misc.SpringAnimationImpl r5, java.lang.String r6) {
        /*
            r4 = this;
            int r4 = r6.hashCode()
            r0 = 4
            r1 = 3
            r2 = 2
            r3 = 1
            switch(r4) {
                case -1225497657: goto L_0x0035;
                case -1225497656: goto L_0x002a;
                case -908189618: goto L_0x0020;
                case -908189617: goto L_0x0016;
                case 92909918: goto L_0x000c;
                default: goto L_0x000b;
            }
        L_0x000b:
            goto L_0x0040
        L_0x000c:
            java.lang.String r4 = "alpha"
            boolean r4 = r6.equals(r4)
            if (r4 == 0) goto L_0x0040
            r4 = 0
            goto L_0x0041
        L_0x0016:
            java.lang.String r4 = "scaleY"
            boolean r4 = r6.equals(r4)
            if (r4 == 0) goto L_0x0040
            r4 = r0
            goto L_0x0041
        L_0x0020:
            java.lang.String r4 = "scaleX"
            boolean r4 = r6.equals(r4)
            if (r4 == 0) goto L_0x0040
            r4 = r1
            goto L_0x0041
        L_0x002a:
            java.lang.String r4 = "translationY"
            boolean r4 = r6.equals(r4)
            if (r4 == 0) goto L_0x0040
            r4 = r2
            goto L_0x0041
        L_0x0035:
            java.lang.String r4 = "translationX"
            boolean r4 = r6.equals(r4)
            if (r4 == 0) goto L_0x0040
            r4 = r3
            goto L_0x0041
        L_0x0040:
            r4 = -1
        L_0x0041:
            if (r4 == 0) goto L_0x0061
            if (r4 == r3) goto L_0x005c
            if (r4 == r2) goto L_0x0057
            if (r4 == r1) goto L_0x0052
            if (r4 == r0) goto L_0x004d
            r4 = 0
            return r4
        L_0x004d:
            androidx.dynamicanimation.animation.SpringAnimation r4 = r5.getScaleYSpringAnim()
            return r4
        L_0x0052:
            androidx.dynamicanimation.animation.SpringAnimation r4 = r5.getScaleXSpringAnim()
            return r4
        L_0x0057:
            androidx.dynamicanimation.animation.SpringAnimation r4 = r5.getTranslationYSpringAnim()
            return r4
        L_0x005c:
            androidx.dynamicanimation.animation.SpringAnimation r4 = r5.getTranslationXSpringAnim()
            return r4
        L_0x0061:
            androidx.dynamicanimation.animation.SpringAnimation r4 = r5.getAlphaSpringAnim()
            return r4
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.recents.misc.SpringAnimationUtils.getSpringAnimation(com.android.systemui.recents.misc.SpringAnimationImpl, java.lang.String):androidx.dynamicanimation.animation.SpringAnimation");
    }

    public SpringAnimation createDefaultSpringAnim(View view, DynamicAnimation.ViewProperty viewProperty, float f) {
        SpringForce springForce = new SpringForce(f);
        springForce.setDampingRatio(0.86f);
        springForce.setStiffness(calculateStiffFromResponse(0.32f));
        SpringAnimation springAnimation = new SpringAnimation(view, viewProperty);
        springAnimation.setSpring(springForce);
        return springAnimation;
    }

    public void updateSpringAnimation(SpringAnimationImpl springAnimationImpl, float f, float f2, float f3, String str) {
        SpringAnimation springAnimation = getSpringAnimation(springAnimationImpl, str);
        SpringForce spring = springAnimation.getSpring();
        spring.setDampingRatio(f);
        spring.setStiffness(calculateStiffFromResponse(f2));
        spring.setFinalPosition(f3);
        springAnimation.setSpring(spring);
        if (!springAnimation.isRunning()) {
            springAnimation.start();
        }
    }

    public void updateSpringAnimation(SpringAnimationImpl springAnimationImpl, float f, float f2, ViewState viewState) {
        View targetView = springAnimationImpl.getTargetView();
        if (getSpringAnimation(springAnimationImpl, "alpha").isRunning() || viewState.alpha != targetView.getAlpha()) {
            updateSpringAnimation(springAnimationImpl, f, f2, viewState.alpha, "alpha");
        }
        if (getSpringAnimation(springAnimationImpl, "scaleX").isRunning() || viewState.scaleX != targetView.getScaleX()) {
            updateSpringAnimation(springAnimationImpl, f, f2, viewState.scaleX, "scaleX");
        }
        if (getSpringAnimation(springAnimationImpl, "scaleY").isRunning() || viewState.scaleY != targetView.getScaleY()) {
            updateSpringAnimation(springAnimationImpl, f, f2, viewState.scaleY, "scaleY");
        }
        if (getSpringAnimation(springAnimationImpl, "translationX").isRunning() || viewState.translationX != targetView.getTranslationX()) {
            updateSpringAnimation(springAnimationImpl, f, f2, viewState.translationX, "translationX");
        }
        if (getSpringAnimation(springAnimationImpl, "translationY").isRunning() || viewState.translationY != targetView.getTranslationY()) {
            updateSpringAnimation(springAnimationImpl, f, f2, viewState.translationY, "translationY");
        }
    }

    public void cancelAllSpringAnimation(SpringAnimationImpl springAnimationImpl) {
        for (SpringAnimation cancel : springAnimationImpl.getAllSpringAnim()) {
            cancel.cancel();
        }
    }

    public void addEndListener(final SpringAnimation springAnimation, final Runnable runnable) {
        if (springAnimation.isRunning()) {
            springAnimation.addEndListener(new DynamicAnimation.OnAnimationEndListener() {
                public void onAnimationEnd(DynamicAnimation dynamicAnimation, boolean z, float f, float f2) {
                    Runnable runnable = runnable;
                    if (runnable != null) {
                        runnable.run();
                    }
                    springAnimation.removeEndListener(this);
                }
            });
        } else if (runnable != null) {
            runnable.run();
        }
    }

    public void startSlideInSpringAnim(RecentsView recentsView, float f, float f2) {
        this.mIsCanUpdateSpringAnim = true;
        float height = (float) (recentsView.getHeight() - recentsView.getTaskViewPaddingView());
        for (int i = 0; i < recentsView.getTaskViews().size(); i++) {
            TaskView taskView = recentsView.getTaskViews().get(i);
            cancelAllSpringAnimation(taskView.getSpringAnimationImpl());
            taskView.setTranslationY(height);
            getSpringAnimation(taskView.getSpringAnimationImpl(), "translationY").setStartVelocity(10000.0f);
            updateSpringAnimation(taskView.getSpringAnimationImpl(), 0.86f, (((float) i) * 0.08f) + 0.32f, new ViewState(1.0f, 1.0f, 1.0f, ((f - (((float) recentsView.getWidth()) / 2.0f)) / (((float) recentsView.getWidth()) / 2.0f)) * ((float) recentsView.getWidth()) * 0.08f, ((f2 - (((float) recentsView.getHeight()) / 2.0f)) / (((float) recentsView.getHeight()) / 2.0f)) * ((float) recentsView.getWidth()) * 0.08f));
        }
        updateSpringAnimation(recentsView.getSpringAnimationImpl(), 0.86f, 0.32f, this.RECENTS_VIEW_SCALE_DOWN);
    }

    public void cancelSlideInSpringAnim(RecentsView recentsView) {
        for (int i = 0; i < recentsView.getTaskViews().size(); i++) {
            cancelAllSpringAnimation(recentsView.getTaskViews().get(i).getSpringAnimationImpl());
        }
        cancelAllSpringAnimation(recentsView.getSpringAnimationImpl());
    }

    public void startSlideOutSpringAnim(RecentsView recentsView) {
        this.mIsCanUpdateSpringAnim = false;
        for (int i = 0; i < recentsView.getTaskViews().size(); i++) {
            updateSpringAnimation(recentsView.getTaskViews().get(i).getSpringAnimationImpl(), 0.86f, 0.32f, new ViewState(1.0f, 1.0f, 1.0f, 0.0f, (float) (recentsView.getHeight() - recentsView.getTaskViewPaddingView())));
        }
        updateSpringAnimation(recentsView.getSpringAnimationImpl(), 0.86f, 0.32f, this.RECENTS_VIEW_HIDE);
    }

    public void startFsMoveAnim(RecentsView recentsView, float f, float f2) {
        if (this.mIsCanUpdateSpringAnim) {
            float width = ((f - (((float) recentsView.getWidth()) / 2.0f)) / (((float) recentsView.getWidth()) / 2.0f)) * ((float) recentsView.getWidth()) * 0.08f;
            float height = ((f2 - (((float) recentsView.getHeight()) / 2.0f)) / (((float) recentsView.getHeight()) / 2.0f)) * ((float) recentsView.getWidth()) * 0.08f;
            for (int i = 0; i < recentsView.getTaskViews().size(); i++) {
                updateSpringAnimation(recentsView.getTaskViews().get(i).getSpringAnimationImpl(), 0.86f, (((float) i) * 0.08f) + 0.32f, new ViewState(1.0f, 1.0f, 1.0f, width, height));
            }
        }
    }

    public void startFsZoomAnim(RecentsView recentsView, Runnable runnable) {
        this.mIsCanUpdateSpringAnim = false;
        float f = 0.25f;
        if (recentsView.getTaskViews().size() > 0) {
            f = 0.25f - (Math.max(0.0f, Math.min(1.0f, Math.abs(recentsView.getTaskViews().get(0).getTranslationY()) / ((float) (recentsView.getHeight() - recentsView.getTaskViewPaddingView())))) * 0.13f);
        }
        for (int i = 0; i < recentsView.getTaskViews().size(); i++) {
            TaskView taskView = recentsView.getTaskViews().get(i);
            updateSpringAnimation(taskView.getHeaderView().getSpringAnimationImpl(), 0.86f, f, this.HEADER_NORMAL);
            updateSpringAnimation(taskView.getSpringAnimationImpl(), 0.86f, f, this.TASK_VIEW_NORMAL);
            if (i == recentsView.getTaskViews().size() - 1) {
                addEndListener(taskView.getSpringAnimationImpl().getTranslationYSpringAnim(), runnable);
            }
        }
        updateSpringAnimation(recentsView.getSpringAnimationImpl(), 0.86f, f, this.RECENTS_VIEW_NORMAL);
    }

    public void startFsGestureRecentsModeSlideOutAnim(RecentsView recentsView) {
        for (int i = 0; i < recentsView.getTaskViews().size(); i++) {
            updateSpringAnimation(recentsView.getTaskViews().get(i).getSpringAnimationImpl(), 0.86f, 0.32f, new ViewState(0.0f, 1.0f, 1.0f, 0.0f, (float) (recentsView.getHeight() - recentsView.getTaskViewPaddingView())));
        }
    }

    public void startFsGestureRecentsModeSlideInAnim(RecentsView recentsView) {
        for (int i = 0; i < recentsView.getTaskViews().size(); i++) {
            TaskView taskView = recentsView.getTaskViews().get(i);
            cancelAllSpringAnimation(taskView.getSpringAnimationImpl());
            getSpringAnimation(taskView.getSpringAnimationImpl(), "translationY").setStartVelocity(10000.0f);
            updateSpringAnimation(taskView.getSpringAnimationImpl(), 0.86f, (((float) i) * 0.08f) + 0.32f, this.TASK_VIEW_NORMAL);
        }
    }

    public void startFsGestureRecentsModeResetAnim(RecentsView recentsView, Runnable runnable) {
        for (int i = 0; i < recentsView.getTaskViews().size(); i++) {
            TaskView taskView = recentsView.getTaskViews().get(i);
            updateSpringAnimation(taskView.getSpringAnimationImpl(), 0.86f, (((float) i) * 0.08f) + 0.32f, this.TASK_VIEW_NORMAL);
            if (i == recentsView.getTaskViews().size() - 1) {
                addEndListener(getSpringAnimation(taskView.getSpringAnimationImpl(), "translationY"), runnable);
            }
        }
    }

    public void startShowTaskMenuAnim(TaskStackView taskStackView, TaskView taskView) {
        for (TaskView next : taskStackView.getTaskViews()) {
            float width = (float) ((taskStackView.getWidth() / 2) - next.getLeft());
            float height = (float) ((taskStackView.getHeight() / 2) - next.getTop());
            if (next != taskView) {
                next.setPivotX(width);
                next.setPivotY(height);
                updateSpringAnimation(next.getSpringAnimationImpl(), 0.99f, 0.3f, this.TASK_VIEW_HIDE);
            } else {
                next.getHeaderView().setPivotX(width);
                next.getHeaderView().setPivotY(height);
                updateSpringAnimation(next.getHeaderView().getSpringAnimationImpl(), 0.99f, 0.3f, this.HEADER_SCALE_DOWN);
                updateSpringAnimation(next.getThumbnailView().getSpringAnimationImpl(), 0.8f, 0.3f, this.THUMBNAIL_SCALE_UP);
            }
        }
    }

    public void startRemoveTaskMenuAnim(TaskStackView taskStackView, TaskView taskView) {
        for (TaskView next : taskStackView.getTaskViews()) {
            float width = (float) ((taskStackView.getWidth() / 2) - next.getLeft());
            float height = (float) ((taskStackView.getHeight() / 2) - next.getTop());
            if (next != taskView) {
                next.setPivotX(width);
                next.setPivotY(height);
                updateSpringAnimation(next.getSpringAnimationImpl(), 0.99f, 0.3f, this.TASK_VIEW_NORMAL);
            } else {
                next.getHeaderView().setPivotX(width);
                next.getHeaderView().setPivotY(height);
                updateSpringAnimation(next.getHeaderView().getSpringAnimationImpl(), 0.99f, 0.3f, this.HEADER_NORMAL);
                updateSpringAnimation(next.getThumbnailView().getSpringAnimationImpl(), 0.45f, 0.5f, this.THUMBNAIL_NORMAL);
            }
        }
    }

    public void startTaskViewScaleUpMenuModeAnim(TaskView taskView) {
        updateSpringAnimation(taskView.getThumbnailView().getSpringAnimationImpl(), 0.8f, 0.3f, this.THUMBNAIL_SCALE_UP_PLUS);
    }

    public void startTaskViewScaleDownMenuModeAnim(TaskView taskView) {
        updateSpringAnimation(taskView.getThumbnailView().getSpringAnimationImpl(), 0.8f, 0.3f, this.THUMBNAIL_SCALE_UP);
    }

    public void startTaskViewTouchDownAnim(TaskView taskView) {
        if (taskView != null) {
            updateSpringAnimation(taskView.getThumbnailView().getSpringAnimationImpl(), 0.6f, 0.5f, this.THUMBNAIL_SCALE_DOWN);
        }
    }

    public void startTaskViewTouchMoveOrUpAnim(TaskView taskView) {
        if (taskView != null) {
            updateSpringAnimation(taskView.getThumbnailView().getSpringAnimationImpl(), 0.6f, 0.5f, this.THUMBNAIL_NORMAL);
        }
    }

    public void startTaskViewDragStartAnim(TaskView taskView) {
        if (taskView != null) {
            updateSpringAnimation(taskView.getThumbnailView().getSpringAnimationImpl(), 0.6f, 0.5f, this.THUMBNAIL_SCALE_UP);
        }
    }

    public void startTaskViewDragEndAnim(TaskView taskView) {
        if (taskView != null) {
            updateSpringAnimation(taskView.getThumbnailView().getSpringAnimationImpl(), 0.6f, 0.5f, this.THUMBNAIL_NORMAL);
            updateSpringAnimation(taskView.getSpringAnimationImpl(), 0.6f, 0.5f, this.TASK_VIEW_NORMAL);
        }
    }

    public void startTaskViewSwipeAnim(TaskView taskView) {
        if (taskView != null) {
            updateSpringAnimation(taskView.getThumbnailView().getSpringAnimationImpl(), 0.6f, 0.5f, this.THUMBNAIL_SCALE_UP);
        }
    }

    public void startTaskViewSwipeCancelAnim(TaskView taskView) {
        if (taskView != null) {
            updateSpringAnimation(taskView.getThumbnailView().getSpringAnimationImpl(), 0.6f, 0.5f, this.TASK_VIEW_NORMAL);
        }
    }

    public SpringAnimation startTaskViewSnapAnim(TaskView taskView, Runnable runnable) {
        if (taskView == null) {
            return null;
        }
        updateSpringAnimation(taskView.getSpringAnimationImpl(), 0.6f, 0.5f, this.TASK_VIEW_NORMAL);
        SpringAnimation springAnimation = getSpringAnimation(taskView.getSpringAnimationImpl(), "translationX");
        addEndListener(springAnimation, runnable);
        return springAnimation;
    }

    public void startDragExitRecentsAnim(TaskStackView taskStackView) {
        for (TaskView headerView : taskStackView.getTaskViews()) {
            updateSpringAnimation(headerView.getHeaderView().getSpringAnimationImpl(), 0.99f, 0.3f, this.HEADER_HIDE);
        }
    }

    public void startCancelDragExitRecentsAnim(TaskStackView taskStackView) {
        for (TaskView headerView : taskStackView.getTaskViews()) {
            updateSpringAnimation(headerView.getHeaderView().getSpringAnimationImpl(), 0.99f, 0.3f, this.HEADER_NORMAL);
        }
    }

    public void startAppToRecentsAnim(RecentsView recentsView) {
        float height = (float) (recentsView.getHeight() - recentsView.getTaskViewPaddingView());
        int i = Recents.getConfiguration().getLaunchState().launchedToTaskId;
        for (int i2 = 0; i2 < recentsView.getTaskViews().size(); i2++) {
            TaskView taskView = recentsView.getTaskViews().get(i2);
            cancelAllSpringAnimation(taskView.getSpringAnimationImpl());
            if (taskView.getTask() == null || taskView.getTask().key.id != i) {
                taskView.setTranslationY(height / 4.0f);
                getSpringAnimation(taskView.getSpringAnimationImpl(), "translationY").setStartVelocity(20000.0f);
                updateSpringAnimation(taskView.getSpringAnimationImpl(), 0.99f, 0.18f, this.TASK_VIEW_NORMAL);
            } else {
                setToState(taskView, this.TASK_VIEW_NORMAL);
            }
        }
    }

    private void setToState(View view, ViewState viewState) {
        view.setAlpha(viewState.alpha);
        view.setScaleX(viewState.scaleX);
        view.setScaleY(viewState.scaleY);
        view.setTranslationX(viewState.translationX);
        view.setTranslationY(viewState.translationY);
    }

    public void startHomeToRecentsAnim(TaskView taskView, Runnable runnable) {
        cancelAllSpringAnimation(taskView.getSpringAnimationImpl());
        updateSpringAnimation(taskView.getSpringAnimationImpl(), 0.86f, 0.15f, this.TASK_VIEW_NORMAL);
        SpringAnimation springAnimation = getSpringAnimation(taskView.getSpringAnimationImpl(), "translationY");
        springAnimation.setStartVelocity(20000.0f);
        addEndListener(springAnimation, runnable);
    }

    public void startLaunchTaskSucceededAnim(RecentsView recentsView, Task task) {
        Task task2 = task;
        float height = (float) (recentsView.getHeight() - recentsView.getTaskViewPaddingView());
        for (int i = 0; i < recentsView.getTaskViews().size(); i++) {
            TaskView taskView = recentsView.getTaskViews().get(i);
            cancelAllSpringAnimation(taskView.getSpringAnimationImpl());
            if (taskView.getTask() == null || task2 == null || taskView.getTask().key.id != task2.key.id) {
                updateSpringAnimation(taskView.getSpringAnimationImpl(), 0.86f, 0.32f, new ViewState(0.0f, 1.0f, 1.0f, 0.0f, taskView.getTranslationY() + (height / 6.0f)));
            }
        }
    }
}

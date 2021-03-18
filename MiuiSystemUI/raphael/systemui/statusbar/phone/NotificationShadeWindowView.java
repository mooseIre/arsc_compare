package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.DisplayCutout;
import android.view.InputQueue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.FrameLayout;
import com.android.internal.view.FloatingActionMode;
import com.android.internal.widget.FloatingToolbar;
import com.android.systemui.C0015R$id;
import com.android.systemui.R$styleable;

public class NotificationShadeWindowView extends FrameLayout {
    private Window mFakeWindow = new Window(((FrameLayout) this).mContext) {
        /* class com.android.systemui.statusbar.phone.NotificationShadeWindowView.AnonymousClass2 */

        public void addContentView(View view, ViewGroup.LayoutParams layoutParams) {
        }

        public void alwaysReadCloseOnTouchAttr() {
        }

        public void clearContentView() {
        }

        public void closeAllPanels() {
        }

        public void closePanel(int i) {
        }

        public View getCurrentFocus() {
            return null;
        }

        public WindowInsetsController getInsetsController() {
            return null;
        }

        public LayoutInflater getLayoutInflater() {
            return null;
        }

        public int getNavigationBarColor() {
            return 0;
        }

        public int getStatusBarColor() {
            return 0;
        }

        public int getVolumeControlStream() {
            return 0;
        }

        public void invalidatePanelMenu(int i) {
        }

        public boolean isFloating() {
            return false;
        }

        public boolean isShortcutKey(int i, KeyEvent keyEvent) {
            return false;
        }

        /* access modifiers changed from: protected */
        public void onActive() {
        }

        public void onConfigurationChanged(Configuration configuration) {
        }

        public void onMultiWindowModeChanged() {
        }

        public void onPictureInPictureModeChanged(boolean z) {
        }

        public void openPanel(int i, KeyEvent keyEvent) {
        }

        public View peekDecorView() {
            return null;
        }

        public boolean performContextMenuIdentifierAction(int i, int i2) {
            return false;
        }

        public boolean performPanelIdentifierAction(int i, int i2, int i3) {
            return false;
        }

        public boolean performPanelShortcut(int i, int i2, KeyEvent keyEvent, int i3) {
            return false;
        }

        public void reportActivityRelaunched() {
        }

        public void restoreHierarchyState(Bundle bundle) {
        }

        public Bundle saveHierarchyState() {
            return null;
        }

        public void setBackgroundDrawable(Drawable drawable) {
        }

        public void setChildDrawable(int i, Drawable drawable) {
        }

        public void setChildInt(int i, int i2) {
        }

        @Override // android.view.Window
        public void setContentView(int i) {
        }

        @Override // android.view.Window
        public void setContentView(View view) {
        }

        public void setContentView(View view, ViewGroup.LayoutParams layoutParams) {
        }

        public void setDecorCaptionShade(int i) {
        }

        public void setFeatureDrawable(int i, Drawable drawable) {
        }

        public void setFeatureDrawableAlpha(int i, int i2) {
        }

        public void setFeatureDrawableResource(int i, int i2) {
        }

        public void setFeatureDrawableUri(int i, Uri uri) {
        }

        public void setFeatureInt(int i, int i2) {
        }

        public void setNavigationBarColor(int i) {
        }

        public void setResizingCaptionDrawable(Drawable drawable) {
        }

        public void setStatusBarColor(int i) {
        }

        public void setTitle(CharSequence charSequence) {
        }

        public void setTitleColor(int i) {
        }

        public void setVolumeControlStream(int i) {
        }

        public boolean superDispatchGenericMotionEvent(MotionEvent motionEvent) {
            return false;
        }

        public boolean superDispatchKeyEvent(KeyEvent keyEvent) {
            return false;
        }

        public boolean superDispatchKeyShortcutEvent(KeyEvent keyEvent) {
            return false;
        }

        public boolean superDispatchTouchEvent(MotionEvent motionEvent) {
            return false;
        }

        public boolean superDispatchTrackballEvent(MotionEvent motionEvent) {
            return false;
        }

        public void takeInputQueue(InputQueue.Callback callback) {
        }

        public void takeKeyEvents(boolean z) {
        }

        public void takeSurface(SurfaceHolder.Callback2 callback2) {
        }

        public void togglePanel(int i, KeyEvent keyEvent) {
        }

        public View getDecorView() {
            return NotificationShadeWindowView.this;
        }
    };
    private ActionMode mFloatingActionMode;
    private View mFloatingActionModeOriginatingView;
    private FloatingToolbar mFloatingToolbar;
    private ViewTreeObserver.OnPreDrawListener mFloatingToolbarPreDrawListener;
    private InteractionEventHandler mInteractionEventHandler;
    private int mLeftInset = 0;
    private int mRightInset = 0;

    /* access modifiers changed from: package-private */
    public interface InteractionEventHandler {
        void didIntercept(MotionEvent motionEvent);

        void didNotHandleTouchEvent(MotionEvent motionEvent);

        boolean dispatchKeyEvent(KeyEvent keyEvent);

        Boolean handleDispatchTouchEvent(MotionEvent motionEvent);

        boolean handleTouchEvent(MotionEvent motionEvent);

        boolean interceptMediaKey(KeyEvent keyEvent);

        boolean shouldInterceptTouchEvent(MotionEvent motionEvent);
    }

    public NotificationShadeWindowView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setMotionEventSplittingEnabled(false);
    }

    public NotificationPanelView getNotificationPanelView() {
        return (NotificationPanelView) findViewById(C0015R$id.notification_panel);
    }

    public WindowInsets onApplyWindowInsets(WindowInsets windowInsets) {
        Insets insetsIgnoringVisibility = windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
        boolean z = true;
        if (getFitsSystemWindows()) {
            if (insetsIgnoringVisibility.top == getPaddingTop() && insetsIgnoringVisibility.bottom == getPaddingBottom()) {
                z = false;
            }
            if (z) {
                setPadding(0, 0, 0, 0);
            }
        } else {
            if (getPaddingLeft() == 0 && getPaddingRight() == 0 && getPaddingTop() == 0 && getPaddingBottom() == 0) {
                z = false;
            }
            if (z) {
                setPadding(0, 0, 0, 0);
            }
        }
        this.mLeftInset = 0;
        this.mRightInset = 0;
        DisplayCutout displayCutout = getRootWindowInsets().getDisplayCutout();
        if (displayCutout != null) {
            this.mLeftInset = displayCutout.getSafeInsetLeft();
            this.mRightInset = displayCutout.getSafeInsetRight();
        }
        this.mLeftInset = Math.max(insetsIgnoringVisibility.left, this.mLeftInset);
        this.mRightInset = Math.max(insetsIgnoringVisibility.right, this.mRightInset);
        applyMargins();
        return windowInsets;
    }

    private void applyMargins() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getLayoutParams() instanceof LayoutParams) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (!layoutParams.ignoreRightInset && !(((FrameLayout.LayoutParams) layoutParams).rightMargin == this.mRightInset && ((FrameLayout.LayoutParams) layoutParams).leftMargin == this.mLeftInset)) {
                    ((FrameLayout.LayoutParams) layoutParams).rightMargin = this.mRightInset;
                    ((FrameLayout.LayoutParams) layoutParams).leftMargin = this.mLeftInset;
                    childAt.requestLayout();
                }
            }
        }
    }

    @Override // android.widget.FrameLayout, android.widget.FrameLayout, android.view.ViewGroup
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(this, getContext(), attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // android.widget.FrameLayout, android.widget.FrameLayout
    public FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(this, -1, -1);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setWillNotDraw(true);
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        if (!this.mInteractionEventHandler.interceptMediaKey(keyEvent) && !super.dispatchKeyEvent(keyEvent)) {
            return this.mInteractionEventHandler.dispatchKeyEvent(keyEvent);
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void setInteractionEventHandler(InteractionEventHandler interactionEventHandler) {
        this.mInteractionEventHandler = interactionEventHandler;
    }

    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        Boolean handleDispatchTouchEvent = this.mInteractionEventHandler.handleDispatchTouchEvent(motionEvent);
        return handleDispatchTouchEvent != null ? handleDispatchTouchEvent.booleanValue() : super.dispatchTouchEvent(motionEvent);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean shouldInterceptTouchEvent = this.mInteractionEventHandler.shouldInterceptTouchEvent(motionEvent);
        if (!shouldInterceptTouchEvent) {
            shouldInterceptTouchEvent = super.onInterceptTouchEvent(motionEvent);
        }
        if (shouldInterceptTouchEvent) {
            this.mInteractionEventHandler.didIntercept(motionEvent);
        }
        return shouldInterceptTouchEvent;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        boolean handleTouchEvent = this.mInteractionEventHandler.handleTouchEvent(motionEvent);
        if (!handleTouchEvent) {
            handleTouchEvent = super.onTouchEvent(motionEvent);
        }
        if (!handleTouchEvent) {
            this.mInteractionEventHandler.didNotHandleTouchEvent(motionEvent);
        }
        return handleTouchEvent;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    /* access modifiers changed from: package-private */
    public class LayoutParams extends FrameLayout.LayoutParams {
        public boolean ignoreRightInset;

        LayoutParams(NotificationShadeWindowView notificationShadeWindowView, int i, int i2) {
            super(i, i2);
        }

        LayoutParams(NotificationShadeWindowView notificationShadeWindowView, Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.StatusBarWindowView_Layout);
            this.ignoreRightInset = obtainStyledAttributes.getBoolean(R$styleable.StatusBarWindowView_Layout_ignoreRightInset, false);
            obtainStyledAttributes.recycle();
        }
    }

    public ActionMode startActionModeForChild(View view, ActionMode.Callback callback, int i) {
        if (i == 1) {
            return startActionMode(view, callback, i);
        }
        return super.startActionModeForChild(view, callback, i);
    }

    private ActionMode createFloatingActionMode(View view, ActionMode.Callback2 callback2) {
        ActionMode actionMode = this.mFloatingActionMode;
        if (actionMode != null) {
            actionMode.finish();
        }
        cleanupFloatingActionModeViews();
        this.mFloatingToolbar = new FloatingToolbar(this.mFakeWindow);
        final FloatingActionMode floatingActionMode = new FloatingActionMode(((FrameLayout) this).mContext, callback2, view, this.mFloatingToolbar);
        this.mFloatingActionModeOriginatingView = view;
        this.mFloatingToolbarPreDrawListener = new ViewTreeObserver.OnPreDrawListener(this) {
            /* class com.android.systemui.statusbar.phone.NotificationShadeWindowView.AnonymousClass1 */

            public boolean onPreDraw() {
                floatingActionMode.updateViewLocationInWindow();
                return true;
            }
        };
        return floatingActionMode;
    }

    private void setHandledFloatingActionMode(ActionMode actionMode) {
        this.mFloatingActionMode = actionMode;
        actionMode.invalidate();
        this.mFloatingActionModeOriginatingView.getViewTreeObserver().addOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cleanupFloatingActionModeViews() {
        FloatingToolbar floatingToolbar = this.mFloatingToolbar;
        if (floatingToolbar != null) {
            floatingToolbar.dismiss();
            this.mFloatingToolbar = null;
        }
        View view = this.mFloatingActionModeOriginatingView;
        if (view != null) {
            if (this.mFloatingToolbarPreDrawListener != null) {
                view.getViewTreeObserver().removeOnPreDrawListener(this.mFloatingToolbarPreDrawListener);
                this.mFloatingToolbarPreDrawListener = null;
            }
            this.mFloatingActionModeOriginatingView = null;
        }
    }

    private ActionMode startActionMode(View view, ActionMode.Callback callback, int i) {
        ActionModeCallback2Wrapper actionModeCallback2Wrapper = new ActionModeCallback2Wrapper(callback);
        ActionMode createFloatingActionMode = createFloatingActionMode(view, actionModeCallback2Wrapper);
        if (createFloatingActionMode == null || !actionModeCallback2Wrapper.onCreateActionMode(createFloatingActionMode, createFloatingActionMode.getMenu())) {
            return null;
        }
        setHandledFloatingActionMode(createFloatingActionMode);
        return createFloatingActionMode;
    }

    /* access modifiers changed from: private */
    public class ActionModeCallback2Wrapper extends ActionMode.Callback2 {
        private final ActionMode.Callback mWrapped;

        ActionModeCallback2Wrapper(ActionMode.Callback callback) {
            this.mWrapped = callback;
        }

        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            return this.mWrapped.onCreateActionMode(actionMode, menu);
        }

        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            NotificationShadeWindowView.this.requestFitSystemWindows();
            return this.mWrapped.onPrepareActionMode(actionMode, menu);
        }

        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return this.mWrapped.onActionItemClicked(actionMode, menuItem);
        }

        public void onDestroyActionMode(ActionMode actionMode) {
            this.mWrapped.onDestroyActionMode(actionMode);
            if (actionMode == NotificationShadeWindowView.this.mFloatingActionMode) {
                NotificationShadeWindowView.this.cleanupFloatingActionModeViews();
                NotificationShadeWindowView.this.mFloatingActionMode = null;
            }
            NotificationShadeWindowView.this.requestFitSystemWindows();
        }

        public void onGetContentRect(ActionMode actionMode, View view, Rect rect) {
            ActionMode.Callback callback = this.mWrapped;
            if (callback instanceof ActionMode.Callback2) {
                ((ActionMode.Callback2) callback).onGetContentRect(actionMode, view, rect);
            } else {
                super.onGetContentRect(actionMode, view, rect);
            }
        }
    }
}

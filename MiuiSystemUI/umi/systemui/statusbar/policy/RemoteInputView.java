package com.android.systemui.statusbar.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutManager;
import android.content.pm.ShortcutManagerCompat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewCompat;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.CompletionInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableView;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.notification.NotificationViewWrapper;
import com.android.systemui.statusbar.stack.ScrollContainer;
import miui.view.MiuiHapticFeedbackConstants;

public class RemoteInputView extends LinearLayout implements View.OnClickListener, TextWatcher {
    public static final Object VIEW_TAG = new Object();
    private RemoteInputController mController;
    /* access modifiers changed from: private */
    public RemoteEditText mEditText;
    /* access modifiers changed from: private */
    public NotificationData.Entry mEntry;
    private PendingIntent mPendingIntent;
    private ProgressBar mProgressBar;
    private RemoteInput mRemoteInput;
    private RemoteInput[] mRemoteInputs;
    /* access modifiers changed from: private */
    public boolean mRemoved;
    private boolean mResetting;
    private int mRevealCx;
    private int mRevealCy;
    private int mRevealR;
    private ScrollContainer mScrollContainer;
    private View mScrollContainerChild;
    private ImageButton mSendButton;
    public final Object mToken = new Object();
    /* access modifiers changed from: private */
    public NotificationViewWrapper mWrapper;

    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public RemoteInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mProgressBar = (ProgressBar) findViewById(R.id.remote_input_progress);
        ImageButton imageButton = (ImageButton) findViewById(R.id.remote_input_send);
        this.mSendButton = imageButton;
        imageButton.setOnClickListener(this);
        RemoteEditText remoteEditText = (RemoteEditText) getChildAt(0);
        this.mEditText = remoteEditText;
        remoteEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean z = keyEvent == null && (i == 6 || i == 5 || i == 4);
                boolean z2 = keyEvent != null && KeyEvent.isConfirmKey(keyEvent.getKeyCode()) && keyEvent.getAction() == 0;
                if (!z && !z2) {
                    return false;
                }
                if (RemoteInputView.this.mEditText.length() > 0) {
                    RemoteInputView.this.sendRemoteInput();
                }
                return true;
            }
        });
        this.mEditText.addTextChangedListener(this);
        this.mEditText.setInnerFocusable(false);
        RemoteInputView unused = this.mEditText.mRemoteInputView = this;
        updateSendButton();
    }

    /* access modifiers changed from: private */
    public void sendRemoteInput() {
        Bundle bundle = new Bundle();
        bundle.putString(this.mRemoteInput.getResultKey(), this.mEditText.getText().toString());
        Intent addFlags = new Intent().addFlags(MiuiHapticFeedbackConstants.FLAG_MIUI_HAPTIC_TAP_NORMAL);
        RemoteInput.addResultsToIntent(this.mRemoteInputs, addFlags, bundle);
        this.mEditText.setEnabled(false);
        this.mSendButton.setVisibility(4);
        this.mProgressBar.setVisibility(0);
        this.mEntry.remoteInputText = this.mEditText.getText();
        this.mController.addSpinning(this.mEntry.key, this.mToken);
        this.mController.removeRemoteInput(this.mEntry, this.mToken);
        this.mEditText.mShowImeOnInputConnection = false;
        this.mController.remoteInputSent(this.mEntry);
        ShortcutManagerCompat.onApplicationActive((ShortcutManager) getContext().getSystemService(ShortcutManager.class), this.mEntry.notification.getPackageName(), this.mEntry.notification.getUser().getIdentifier());
        MetricsLogger.action(this.mContext, 398, this.mEntry.notification.getPackageName());
        try {
            this.mPendingIntent.send(this.mContext, 0, addFlags);
        } catch (PendingIntent.CanceledException e) {
            Log.i("RemoteInput", "Unable to send remote input result", e);
            MetricsLogger.action(this.mContext, 399, this.mEntry.notification.getPackageName());
        }
    }

    public static RemoteInputView inflate(Context context, ViewGroup viewGroup, NotificationData.Entry entry, RemoteInputController remoteInputController) {
        RemoteInputView remoteInputView = (RemoteInputView) LayoutInflater.from(context).inflate(R.layout.remote_input, viewGroup, false);
        remoteInputView.mController = remoteInputController;
        remoteInputView.mEntry = entry;
        remoteInputView.setTag(VIEW_TAG);
        return remoteInputView;
    }

    public void onClick(View view) {
        if (view == this.mSendButton) {
            sendRemoteInput();
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        return true;
    }

    /* access modifiers changed from: private */
    public void onDefocus(boolean z) {
        int i;
        this.mController.removeRemoteInput(this.mEntry, this.mToken);
        this.mEntry.remoteInputText = this.mEditText.getText();
        if (!this.mRemoved) {
            if (!z || (i = this.mRevealR) <= 0) {
                setVisibility(4);
                NotificationViewWrapper notificationViewWrapper = this.mWrapper;
                if (notificationViewWrapper != null) {
                    notificationViewWrapper.setRemoteInputVisible(false);
                }
            } else {
                Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(this, this.mRevealCx, this.mRevealCy, (float) i, 0.0f);
                createCircularReveal.setInterpolator(Interpolators.FAST_OUT_LINEAR_IN);
                createCircularReveal.setDuration(150);
                createCircularReveal.addListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(Animator animator) {
                        RemoteInputView.this.setVisibility(4);
                        if (RemoteInputView.this.mWrapper != null) {
                            RemoteInputView.this.mWrapper.setRemoteInputVisible(false);
                        }
                    }
                });
                createCircularReveal.start();
            }
        }
        MetricsLogger.action(this.mContext, 400, this.mEntry.notification.getPackageName());
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mEntry.row.isChangingPosition() && getVisibility() == 0 && this.mEditText.isFocusable()) {
            this.mEditText.requestFocus();
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!this.mEntry.row.isChangingPosition() && !ViewCompat.isTemporarilyDetached(this)) {
            this.mController.removeRemoteInput(this.mEntry, this.mToken);
            this.mController.removeSpinning(this.mEntry.key, this.mToken);
        }
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.mPendingIntent = pendingIntent;
    }

    public void setRemoteInput(RemoteInput[] remoteInputArr, RemoteInput remoteInput) {
        this.mRemoteInputs = remoteInputArr;
        this.mRemoteInput = remoteInput;
        this.mEditText.setHint(remoteInput.getLabel());
    }

    public void focusAnimated() {
        if (getVisibility() != 0) {
            Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(this, this.mRevealCx, this.mRevealCy, 0.0f, (float) this.mRevealR);
            createCircularReveal.setDuration(360);
            createCircularReveal.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            createCircularReveal.start();
        }
        focus();
    }

    public void focus() {
        MetricsLogger.action(this.mContext, 397, this.mEntry.notification.getPackageName());
        setVisibility(0);
        NotificationViewWrapper notificationViewWrapper = this.mWrapper;
        if (notificationViewWrapper != null) {
            notificationViewWrapper.setRemoteInputVisible(true);
        }
        this.mController.addRemoteInput(this.mEntry, this.mToken);
        this.mEditText.setInnerFocusable(true);
        RemoteEditText remoteEditText = this.mEditText;
        remoteEditText.mShowImeOnInputConnection = true;
        remoteEditText.setText(this.mEntry.remoteInputText);
        RemoteEditText remoteEditText2 = this.mEditText;
        remoteEditText2.setSelection(remoteEditText2.getText().length());
        this.mEditText.requestFocus();
        updateSendButton();
    }

    public void onNotificationUpdateOrReset() {
        NotificationViewWrapper notificationViewWrapper;
        if (this.mProgressBar.getVisibility() == 0) {
            reset();
        }
        if (isActive() && (notificationViewWrapper = this.mWrapper) != null) {
            notificationViewWrapper.setRemoteInputVisible(true);
        }
    }

    private void reset() {
        this.mResetting = true;
        this.mEditText.getText().clear();
        this.mEditText.setEnabled(true);
        this.mSendButton.setVisibility(0);
        this.mProgressBar.setVisibility(4);
        this.mController.removeSpinning(this.mEntry.key, this.mToken);
        updateSendButton();
        onDefocus(false);
        this.mResetting = false;
    }

    public boolean onRequestSendAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
        if (!this.mResetting || view != this.mEditText) {
            return super.onRequestSendAccessibilityEvent(view, accessibilityEvent);
        }
        return false;
    }

    private void updateSendButton() {
        this.mSendButton.setEnabled(this.mEditText.getText().length() != 0);
    }

    public void afterTextChanged(Editable editable) {
        updateSendButton();
    }

    public void close() {
        this.mEditText.defocusIfNeeded(false);
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == 0) {
            findScrollContainer();
            ScrollContainer scrollContainer = this.mScrollContainer;
            if (scrollContainer != null) {
                scrollContainer.requestDisallowLongPress();
                this.mScrollContainer.requestDisallowDismiss();
            }
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    public boolean requestScrollTo() {
        findScrollContainer();
        this.mScrollContainer.lockScrollTo(this.mScrollContainerChild);
        return true;
    }

    private void findScrollContainer() {
        if (this.mScrollContainer == null) {
            this.mScrollContainerChild = null;
            for (ViewParent viewParent = this; viewParent != null; viewParent = viewParent.getParent()) {
                if (this.mScrollContainerChild == null && (viewParent instanceof ExpandableView)) {
                    this.mScrollContainerChild = (View) viewParent;
                }
                if (viewParent.getParent() instanceof ScrollContainer) {
                    this.mScrollContainer = (ScrollContainer) viewParent.getParent();
                    if (this.mScrollContainerChild == null) {
                        this.mScrollContainerChild = (View) viewParent;
                        return;
                    }
                    return;
                }
            }
        }
    }

    public boolean isActive() {
        return this.mEditText.isFocused() && this.mEditText.isEnabled();
    }

    public void stealFocusFrom(RemoteInputView remoteInputView) {
        remoteInputView.close();
        setPendingIntent(remoteInputView.mPendingIntent);
        setRemoteInput(remoteInputView.mRemoteInputs, remoteInputView.mRemoteInput);
        setRevealParameters(remoteInputView.mRevealCx, remoteInputView.mRevealCy, remoteInputView.mRevealR);
        focus();
    }

    public boolean updatePendingIntentFromActions(Notification.Action[] actionArr) {
        Intent intent;
        PendingIntent pendingIntent = this.mPendingIntent;
        if (pendingIntent == null || actionArr == null || (intent = pendingIntent.getIntent()) == null) {
            return false;
        }
        for (Notification.Action action : actionArr) {
            RemoteInput[] remoteInputs = action.getRemoteInputs();
            PendingIntent pendingIntent2 = action.actionIntent;
            if (!(pendingIntent2 == null || remoteInputs == null || !intent.filterEquals(pendingIntent2.getIntent()))) {
                RemoteInput remoteInput = null;
                for (RemoteInput remoteInput2 : remoteInputs) {
                    if (remoteInput2.getAllowFreeFormInput()) {
                        remoteInput = remoteInput2;
                    }
                }
                if (remoteInput != null) {
                    setPendingIntent(action.actionIntent);
                    setRemoteInput(remoteInputs, remoteInput);
                    return true;
                }
            }
        }
        return false;
    }

    public PendingIntent getPendingIntent() {
        return this.mPendingIntent;
    }

    public void setRemoved() {
        this.mRemoved = true;
    }

    public void setRevealParameters(int i, int i2, int i3) {
        this.mRevealCx = i;
        this.mRevealCy = i2;
        this.mRevealR = i3;
    }

    public void dispatchStartTemporaryDetach() {
        super.dispatchStartTemporaryDetach();
        if (this.mEditText == findFocus()) {
            clearChildFocus(this.mEditText);
        }
        detachViewFromParent(this.mEditText);
    }

    public void dispatchFinishTemporaryDetach() {
        if (isAttachedToWindow()) {
            RemoteEditText remoteEditText = this.mEditText;
            attachViewToParent(remoteEditText, 0, remoteEditText.getLayoutParams());
        } else {
            removeDetachedView(this.mEditText, false);
        }
        super.dispatchFinishTemporaryDetach();
    }

    public void setWrapper(NotificationViewWrapper notificationViewWrapper) {
        this.mWrapper = notificationViewWrapper;
    }

    public static class RemoteEditText extends EditText {
        private final Drawable mBackground = getBackground();
        /* access modifiers changed from: private */
        public RemoteInputView mRemoteInputView;
        boolean mShowImeOnInputConnection;

        public RemoteEditText(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        /* access modifiers changed from: private */
        public void defocusIfNeeded(boolean z) {
            RemoteInputView remoteInputView;
            RemoteInputView remoteInputView2 = this.mRemoteInputView;
            if ((remoteInputView2 == null || !remoteInputView2.mEntry.row.isChangingPosition()) && !ViewCompat.isTemporarilyDetached(this)) {
                if (isFocusable() && isEnabled()) {
                    setInnerFocusable(false);
                    RemoteInputView remoteInputView3 = this.mRemoteInputView;
                    if (remoteInputView3 != null) {
                        remoteInputView3.onDefocus(z);
                    }
                    this.mShowImeOnInputConnection = false;
                }
            } else if (ViewCompat.isTemporarilyDetached(this) && (remoteInputView = this.mRemoteInputView) != null) {
                remoteInputView.mEntry.remoteInputText = getText();
            }
        }

        /* access modifiers changed from: protected */
        public void onVisibilityChanged(View view, int i) {
            super.onVisibilityChanged(view, i);
            if (!isShown()) {
                defocusIfNeeded(false);
            }
        }

        /* access modifiers changed from: protected */
        public void onFocusChanged(boolean z, int i, Rect rect) {
            super.onFocusChanged(z, i, rect);
            if (!z) {
                defocusIfNeeded(true);
            }
        }

        public void getFocusedRect(Rect rect) {
            super.getFocusedRect(rect);
            int i = this.mScrollY;
            rect.top = i;
            rect.bottom = i + (this.mBottom - this.mTop);
        }

        public boolean requestRectangleOnScreen(Rect rect) {
            return this.mRemoteInputView.requestScrollTo();
        }

        public boolean onKeyDown(int i, KeyEvent keyEvent) {
            if (i == 4) {
                return true;
            }
            return super.onKeyDown(i, keyEvent);
        }

        public boolean onKeyUp(int i, KeyEvent keyEvent) {
            if (i != 4) {
                return super.onKeyUp(i, keyEvent);
            }
            defocusIfNeeded(true);
            return true;
        }

        public boolean onCheckIsTextEditor() {
            RemoteInputView remoteInputView = this.mRemoteInputView;
            if ((remoteInputView != null && remoteInputView.mRemoved) || !super.onCheckIsTextEditor()) {
                return false;
            }
            return true;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:3:0x000a, code lost:
            r0 = android.view.inputmethod.InputMethodManager.getInstance();
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public android.view.inputmethod.InputConnection onCreateInputConnection(android.view.inputmethod.EditorInfo r3) {
            /*
                r2 = this;
                android.view.inputmethod.InputConnection r3 = super.onCreateInputConnection(r3)
                boolean r0 = r2.mShowImeOnInputConnection
                if (r0 == 0) goto L_0x0018
                if (r3 == 0) goto L_0x0018
                android.view.inputmethod.InputMethodManager r0 = android.view.inputmethod.InputMethodManager.getInstance()
                if (r0 == 0) goto L_0x0018
                com.android.systemui.statusbar.policy.RemoteInputView$RemoteEditText$1 r1 = new com.android.systemui.statusbar.policy.RemoteInputView$RemoteEditText$1
                r1.<init>(r0)
                r2.post(r1)
            L_0x0018:
                return r3
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.RemoteInputView.RemoteEditText.onCreateInputConnection(android.view.inputmethod.EditorInfo):android.view.inputmethod.InputConnection");
        }

        public void onCommitCompletion(CompletionInfo completionInfo) {
            clearComposingText();
            setText(completionInfo.getText());
            setSelection(getText().length());
        }

        /* access modifiers changed from: package-private */
        public void setInnerFocusable(boolean z) {
            setFocusableInTouchMode(z);
            setFocusable(z);
            setCursorVisible(z);
            if (z) {
                requestFocus();
                setBackground(this.mBackground);
                return;
            }
            setBackground((Drawable) null);
        }
    }
}

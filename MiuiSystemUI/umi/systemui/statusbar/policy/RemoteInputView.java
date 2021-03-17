package com.android.systemui.statusbar.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.text.Editable;
import android.text.SpannedString;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.Interpolators;
import com.android.systemui.statusbar.RemoteInputController;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper;
import com.android.systemui.statusbar.phone.LightBarController;
import java.util.HashMap;
import java.util.function.Consumer;

public class RemoteInputView extends LinearLayout implements View.OnClickListener, TextWatcher {
    public static final Object VIEW_TAG = new Object();
    private RemoteInputController mController;
    private RemoteEditText mEditText;
    private NotificationEntry mEntry;
    private Consumer<Boolean> mOnVisibilityChangedListener;
    private PendingIntent mPendingIntent;
    private ProgressBar mProgressBar;
    private RemoteInput mRemoteInput;
    private RemoteInputQuickSettingsDisabler mRemoteInputQuickSettingsDisabler = ((RemoteInputQuickSettingsDisabler) Dependency.get(RemoteInputQuickSettingsDisabler.class));
    private RemoteInput[] mRemoteInputs;
    private boolean mRemoved;
    private boolean mResetting;
    private int mRevealCx;
    private int mRevealCy;
    private int mRevealR;
    private ImageButton mSendButton;
    public final Object mToken = new Object();
    private NotificationViewWrapper mWrapper;

    public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
    }

    public RemoteInputView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mProgressBar = (ProgressBar) findViewById(C0015R$id.remote_input_progress);
        ImageButton imageButton = (ImageButton) findViewById(C0015R$id.remote_input_send);
        this.mSendButton = imageButton;
        imageButton.setOnClickListener(this);
        RemoteEditText remoteEditText = (RemoteEditText) getChildAt(0);
        this.mEditText = remoteEditText;
        remoteEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            /* class com.android.systemui.statusbar.policy.RemoteInputView.AnonymousClass1 */

            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                boolean z = keyEvent == null && (i == 6 || i == 5 || i == 4);
                boolean z2 = keyEvent != null && KeyEvent.isConfirmKey(keyEvent.getKeyCode()) && keyEvent.getAction() == 0;
                if (!z && !z2) {
                    return false;
                }
                if (RemoteInputView.this.mEditText.length() > 0) {
                    RemoteInputView remoteInputView = RemoteInputView.this;
                    remoteInputView.sendRemoteInput(remoteInputView.prepareRemoteInputFromText());
                }
                return true;
            }
        });
        this.mEditText.addTextChangedListener(this);
        this.mEditText.setInnerFocusable(false);
        this.mEditText.mRemoteInputView = this;
    }

    /* access modifiers changed from: protected */
    public Intent prepareRemoteInputFromText() {
        Bundle bundle = new Bundle();
        bundle.putString(this.mRemoteInput.getResultKey(), this.mEditText.getText().toString());
        Intent addFlags = new Intent().addFlags(268435456);
        RemoteInput.addResultsToIntent(this.mRemoteInputs, addFlags, bundle);
        this.mEntry.remoteInputText = this.mEditText.getText();
        NotificationEntry notificationEntry = this.mEntry;
        notificationEntry.remoteInputUri = null;
        notificationEntry.remoteInputMimeType = null;
        if (notificationEntry.editedSuggestionInfo == null) {
            RemoteInput.setResultsSource(addFlags, 0);
        } else {
            RemoteInput.setResultsSource(addFlags, 1);
        }
        return addFlags;
    }

    /* access modifiers changed from: protected */
    public Intent prepareRemoteInputFromData(String str, Uri uri) {
        HashMap hashMap = new HashMap();
        hashMap.put(str, uri);
        this.mController.grantInlineReplyUriPermission(this.mEntry.getSbn(), uri);
        Intent addFlags = new Intent().addFlags(268435456);
        RemoteInput.addDataResultToIntent(this.mRemoteInput, addFlags, hashMap);
        this.mEntry.remoteInputText = ((LinearLayout) this).mContext.getString(C0021R$string.remote_input_image_insertion_text);
        NotificationEntry notificationEntry = this.mEntry;
        notificationEntry.remoteInputMimeType = str;
        notificationEntry.remoteInputUri = uri;
        return addFlags;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendRemoteInput(Intent intent) {
        this.mEditText.setEnabled(false);
        this.mSendButton.setVisibility(4);
        this.mProgressBar.setVisibility(0);
        this.mEntry.lastRemoteInputSent = SystemClock.elapsedRealtime();
        this.mController.addSpinning(this.mEntry.getKey(), this.mToken);
        this.mController.removeRemoteInput(this.mEntry, this.mToken);
        this.mEditText.mShowImeOnInputConnection = false;
        this.mController.remoteInputSent(this.mEntry);
        this.mEntry.setHasSentReply();
        ((ShortcutManager) getContext().getSystemService(ShortcutManager.class)).onApplicationActive(this.mEntry.getSbn().getPackageName(), this.mEntry.getSbn().getUser().getIdentifier());
        MetricsLogger.action(((LinearLayout) this).mContext, 398, this.mEntry.getSbn().getPackageName());
        try {
            this.mPendingIntent.send(((LinearLayout) this).mContext, 0, intent);
        } catch (PendingIntent.CanceledException e) {
            Log.i("RemoteInput", "Unable to send remote input result", e);
            MetricsLogger.action(((LinearLayout) this).mContext, 399, this.mEntry.getSbn().getPackageName());
        }
    }

    public CharSequence getText() {
        return this.mEditText.getText();
    }

    public static RemoteInputView inflate(Context context, ViewGroup viewGroup, NotificationEntry notificationEntry, RemoteInputController remoteInputController) {
        int i = C0017R$layout.remote_input;
        if (viewGroup.getId() == C0015R$id.actions_container) {
            i = C0017R$layout.miui_remote_input;
        }
        RemoteInputView remoteInputView = (RemoteInputView) LayoutInflater.from(context).inflate(i, viewGroup, false);
        remoteInputView.mController = remoteInputController;
        remoteInputView.mEntry = notificationEntry;
        UserHandle computeTextOperationUser = computeTextOperationUser(notificationEntry.getSbn().getUser());
        RemoteEditText remoteEditText = remoteInputView.mEditText;
        remoteEditText.mUser = computeTextOperationUser;
        remoteEditText.setTextOperationUser(computeTextOperationUser);
        remoteInputView.setTag(VIEW_TAG);
        return remoteInputView;
    }

    public void onClick(View view) {
        if (view == this.mSendButton) {
            sendRemoteInput(prepareRemoteInputFromText());
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        return true;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onDefocus(boolean z) {
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
                    /* class com.android.systemui.statusbar.policy.RemoteInputView.AnonymousClass2 */

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
        this.mRemoteInputQuickSettingsDisabler.setRemoteInputActive(false);
        MetricsLogger.action(((LinearLayout) this).mContext, 400, this.mEntry.getSbn().getPackageName());
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mEntry.getRow().isChangingPosition() && getVisibility() == 0 && this.mEditText.isFocusable()) {
            this.mEditText.requestFocus();
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!this.mEntry.getRow().isChangingPosition() && !isTemporarilyDetached()) {
            this.mController.removeRemoteInput(this.mEntry, this.mToken);
            this.mController.removeSpinning(this.mEntry.getKey(), this.mToken);
        }
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.mPendingIntent = pendingIntent;
    }

    public void setRemoteInput(RemoteInput[] remoteInputArr, RemoteInput remoteInput, NotificationEntry.EditedSuggestionInfo editedSuggestionInfo) {
        this.mRemoteInputs = remoteInputArr;
        this.mRemoteInput = remoteInput;
        this.mEditText.setHint(remoteInput.getLabel());
        NotificationEntry notificationEntry = this.mEntry;
        notificationEntry.editedSuggestionInfo = editedSuggestionInfo;
        if (editedSuggestionInfo != null) {
            notificationEntry.remoteInputText = editedSuggestionInfo.originalText;
        }
    }

    public void focusAnimated() {
        if (getVisibility() != 0) {
            Animator createCircularReveal = ViewAnimationUtils.createCircularReveal(this, this.mRevealCx, this.mRevealCy, 0.0f, (float) this.mRevealR);
            createCircularReveal.setDuration(300);
            createCircularReveal.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
            createCircularReveal.start();
        }
        focus();
    }

    private static UserHandle computeTextOperationUser(UserHandle userHandle) {
        return UserHandle.ALL.equals(userHandle) ? UserHandle.of(ActivityManager.getCurrentUser()) : userHandle;
    }

    public void focus() {
        MetricsLogger.action(((LinearLayout) this).mContext, 397, this.mEntry.getSbn().getPackageName());
        setVisibility(0);
        NotificationViewWrapper notificationViewWrapper = this.mWrapper;
        if (notificationViewWrapper != null) {
            notificationViewWrapper.setRemoteInputVisible(true);
        }
        this.mEditText.setInnerFocusable(true);
        RemoteEditText remoteEditText = this.mEditText;
        remoteEditText.mShowImeOnInputConnection = true;
        remoteEditText.setText(this.mEntry.remoteInputText);
        RemoteEditText remoteEditText2 = this.mEditText;
        remoteEditText2.setSelection(remoteEditText2.getText().length());
        this.mEditText.requestFocus();
        this.mController.addRemoteInput(this.mEntry, this.mToken);
        this.mRemoteInputQuickSettingsDisabler.setRemoteInputActive(true);
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
        this.mEntry.remoteInputTextWhenReset = SpannedString.valueOf(this.mEditText.getText());
        this.mEditText.getText().clear();
        this.mEditText.setEnabled(true);
        this.mSendButton.setVisibility(0);
        this.mProgressBar.setVisibility(4);
        this.mController.removeSpinning(this.mEntry.getKey(), this.mToken);
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
            this.mController.requestDisallowLongPressAndDismiss();
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    public boolean requestScrollTo() {
        this.mController.lockScrollTo(this.mEntry);
        return true;
    }

    public boolean isActive() {
        return this.mEditText.isFocused() && this.mEditText.isEnabled();
    }

    public void stealFocusFrom(RemoteInputView remoteInputView) {
        remoteInputView.close();
        setPendingIntent(remoteInputView.mPendingIntent);
        setRemoteInput(remoteInputView.mRemoteInputs, remoteInputView.mRemoteInput, this.mEntry.editedSuggestionInfo);
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
                    setRemoteInput(remoteInputs, remoteInput, null);
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

    public void setOnVisibilityChangedListener(Consumer<Boolean> consumer) {
        this.mOnVisibilityChangedListener = consumer;
    }

    /* access modifiers changed from: protected */
    public void onVisibilityChanged(View view, int i) {
        Consumer<Boolean> consumer;
        super.onVisibilityChanged(view, i);
        if (view == this && (consumer = this.mOnVisibilityChangedListener) != null) {
            consumer.accept(Boolean.valueOf(i == 0));
        }
    }

    public boolean isSending() {
        return getVisibility() == 0 && this.mController.isSpinning(this.mEntry.getKey(), this.mToken);
    }

    public static class RemoteEditText extends EditText {
        private final Drawable mBackground = getBackground();
        private LightBarController mLightBarController = ((LightBarController) Dependency.get(LightBarController.class));
        private RemoteInputView mRemoteInputView;
        boolean mShowImeOnInputConnection;
        UserHandle mUser;

        public RemoteEditText(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void defocusIfNeeded(boolean z) {
            RemoteInputView remoteInputView;
            RemoteInputView remoteInputView2 = this.mRemoteInputView;
            if ((remoteInputView2 == null || !remoteInputView2.mEntry.getRow().isChangingPosition()) && !isTemporarilyDetached()) {
                if (isFocusable() && isEnabled()) {
                    setInnerFocusable(false);
                    RemoteInputView remoteInputView3 = this.mRemoteInputView;
                    if (remoteInputView3 != null) {
                        remoteInputView3.onDefocus(z);
                    }
                    this.mShowImeOnInputConnection = false;
                }
            } else if (isTemporarilyDetached() && (remoteInputView = this.mRemoteInputView) != null) {
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
            if (!this.mRemoteInputView.mRemoved) {
                this.mLightBarController.setDirectReplying(z);
            }
        }

        public void getFocusedRect(Rect rect) {
            super.getFocusedRect(rect);
            int i = ((EditText) this).mScrollY;
            rect.top = i;
            rect.bottom = i + (((EditText) this).mBottom - ((EditText) this).mTop);
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

        public boolean onKeyPreIme(int i, KeyEvent keyEvent) {
            if (keyEvent.getKeyCode() == 4 && keyEvent.getAction() == 1) {
                defocusIfNeeded(true);
            }
            return super.onKeyPreIme(i, keyEvent);
        }

        public boolean onCheckIsTextEditor() {
            RemoteInputView remoteInputView = this.mRemoteInputView;
            return !(remoteInputView != null && remoteInputView.mRemoved) && super.onCheckIsTextEditor();
        }

        public InputConnection onCreateInputConnection(EditorInfo editorInfo) {
            InputConnection onCreateInputConnection = super.onCreateInputConnection(editorInfo);
            Context context = null;
            InputConnection createWrapper = onCreateInputConnection == null ? null : InputConnectionCompat.createWrapper(onCreateInputConnection, editorInfo, new InputConnectionCompat.OnCommitContentListener() {
                /* class com.android.systemui.statusbar.policy.RemoteInputView.RemoteEditText.AnonymousClass1 */

                @Override // androidx.core.view.inputmethod.InputConnectionCompat.OnCommitContentListener
                public boolean onCommitContent(InputContentInfoCompat inputContentInfoCompat, int i, Bundle bundle) {
                    Uri contentUri = inputContentInfoCompat.getContentUri();
                    ClipDescription description = inputContentInfoCompat.getDescription();
                    String mimeType = (description == null || description.getMimeTypeCount() <= 0) ? null : description.getMimeType(0);
                    if (mimeType == null) {
                        return true;
                    }
                    RemoteEditText.this.mRemoteInputView.sendRemoteInput(RemoteEditText.this.mRemoteInputView.prepareRemoteInputFromData(mimeType, contentUri));
                    return true;
                }
            });
            try {
                context = ((EditText) this).mContext.createPackageContextAsUser(((EditText) this).mContext.getPackageName(), 0, this.mUser);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("RemoteInput", "Unable to create user context:" + e.getMessage(), e);
            }
            if (this.mShowImeOnInputConnection && createWrapper != null) {
                if (context == null) {
                    context = getContext();
                }
                final InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(InputMethodManager.class);
                if (inputMethodManager != null) {
                    post(new Runnable() {
                        /* class com.android.systemui.statusbar.policy.RemoteInputView.RemoteEditText.AnonymousClass2 */

                        public void run() {
                            inputMethodManager.viewClicked(RemoteEditText.this);
                            inputMethodManager.showSoftInput(RemoteEditText.this, 0);
                        }
                    });
                }
            }
            return createWrapper;
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
            setBackground(null);
        }
    }
}

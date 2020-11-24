package com.android.systemui.statusbar.policy;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Layout;
import android.text.TextPaint;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.C0008R$color;
import com.android.systemui.C0009R$dimen;
import com.android.systemui.C0014R$layout;
import com.android.systemui.C0018R$string;
import com.android.systemui.Dependency;
import com.android.systemui.R$styleable;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.statusbar.NotificationRemoteInputManager;
import com.android.systemui.statusbar.SmartReplyController;
import com.android.systemui.statusbar.notification.NotificationUtils;
import com.android.systemui.statusbar.notification.collection.NotificationEntry;
import com.android.systemui.statusbar.notification.logging.NotificationLogger;
import com.android.systemui.statusbar.phone.KeyguardDismissUtil;
import com.android.systemui.statusbar.policy.SmartReplyView;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;

public class SmartReplyView extends ViewGroup {
    private static final Comparator<View> DECREASING_MEASURED_WIDTH_WITHOUT_PADDING_COMPARATOR = $$Lambda$SmartReplyView$UA3QkbRzztEFRlbb86djKcGIV5E.INSTANCE;
    private static final int MEASURE_SPEC_ANY_LENGTH = View.MeasureSpec.makeMeasureSpec(0, 0);
    private ActivityStarter mActivityStarter;
    private final BreakIterator mBreakIterator;
    private PriorityQueue<Button> mCandidateButtonQueueForSqueezing;
    private final SmartReplyConstants mConstants = ((SmartReplyConstants) Dependency.get(SmartReplyConstants.class));
    private int mCurrentBackgroundColor;
    private final int mDefaultBackgroundColor;
    private final int mDefaultStrokeColor;
    private final int mDefaultTextColor;
    private final int mDefaultTextColorDarkBg;
    private final int mDoubleLineButtonPaddingHorizontal;
    private final int mHeightUpperLimit = NotificationUtils.getFontScaledHeight(this.mContext, C0009R$dimen.smart_reply_button_max_height);
    private final KeyguardDismissUtil mKeyguardDismissUtil = ((KeyguardDismissUtil) Dependency.get(KeyguardDismissUtil.class));
    private final double mMinStrokeContrast;
    private final NotificationRemoteInputManager mRemoteInputManager = ((NotificationRemoteInputManager) Dependency.get(NotificationRemoteInputManager.class));
    private final int mRippleColor;
    private final int mRippleColorDarkBg;
    private final int mSingleLineButtonPaddingHorizontal;
    private final int mSingleToDoubleLineButtonWidthIncrease;
    private boolean mSmartRepliesGeneratedByAssistant = false;
    private View mSmartReplyContainer;
    private final int mSpacing;
    private final int mStrokeWidth;

    private enum SmartButtonType {
        REPLY,
        ACTION
    }

    static /* synthetic */ int lambda$static$0(View view, View view2) {
        return ((view2.getMeasuredWidth() - view2.getPaddingLeft()) - view2.getPaddingRight()) - ((view.getMeasuredWidth() - view.getPaddingLeft()) - view.getPaddingRight());
    }

    public SmartReplyView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        int color = context.getColor(C0008R$color.smart_reply_button_background);
        this.mCurrentBackgroundColor = color;
        this.mDefaultBackgroundColor = color;
        this.mDefaultTextColor = this.mContext.getColor(C0008R$color.smart_reply_button_text);
        this.mDefaultTextColorDarkBg = this.mContext.getColor(C0008R$color.smart_reply_button_text_dark_bg);
        this.mDefaultStrokeColor = this.mContext.getColor(C0008R$color.smart_reply_button_stroke);
        int color2 = this.mContext.getColor(C0008R$color.notification_ripple_untinted_color);
        this.mRippleColor = color2;
        this.mRippleColorDarkBg = Color.argb(Color.alpha(color2), 255, 255, 255);
        this.mMinStrokeContrast = ContrastColorUtil.calculateContrast(this.mDefaultStrokeColor, this.mDefaultBackgroundColor);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, R$styleable.SmartReplyView, 0, 0);
        int indexCount = obtainStyledAttributes.getIndexCount();
        int i = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        for (int i5 = 0; i5 < indexCount; i5++) {
            int index = obtainStyledAttributes.getIndex(i5);
            if (index == R$styleable.SmartReplyView_spacing) {
                i2 = obtainStyledAttributes.getDimensionPixelSize(i5, 0);
            } else if (index == R$styleable.SmartReplyView_singleLineButtonPaddingHorizontal) {
                i3 = obtainStyledAttributes.getDimensionPixelSize(i5, 0);
            } else if (index == R$styleable.SmartReplyView_doubleLineButtonPaddingHorizontal) {
                i4 = obtainStyledAttributes.getDimensionPixelSize(i5, 0);
            } else if (index == R$styleable.SmartReplyView_buttonStrokeWidth) {
                i = obtainStyledAttributes.getDimensionPixelSize(i5, 0);
            }
        }
        obtainStyledAttributes.recycle();
        this.mStrokeWidth = i;
        this.mSpacing = i2;
        this.mSingleLineButtonPaddingHorizontal = i3;
        this.mDoubleLineButtonPaddingHorizontal = i4;
        this.mSingleToDoubleLineButtonWidthIncrease = (i4 - i3) * 2;
        this.mBreakIterator = BreakIterator.getLineInstance();
        reallocateCandidateButtonQueueForSqueezing();
    }

    public int getHeightUpperLimit() {
        return this.mHeightUpperLimit;
    }

    private void reallocateCandidateButtonQueueForSqueezing() {
        this.mCandidateButtonQueueForSqueezing = new PriorityQueue<>(Math.max(getChildCount(), 1), DECREASING_MEASURED_WIDTH_WITHOUT_PADDING_COMPARATOR);
    }

    public void resetSmartSuggestions(View view) {
        this.mSmartReplyContainer = view;
        removeAllViews();
        this.mCurrentBackgroundColor = this.mDefaultBackgroundColor;
    }

    public void addPreInflatedButtons(List<Button> list) {
        for (Button addView : list) {
            addView(addView);
        }
        reallocateCandidateButtonQueueForSqueezing();
    }

    public List<Button> inflateRepliesFromRemoteInput(SmartReplies smartReplies, SmartReplyController smartReplyController, NotificationEntry notificationEntry, boolean z) {
        ArrayList arrayList = new ArrayList();
        if (!(smartReplies.remoteInput == null || smartReplies.pendingIntent == null || smartReplies.choices == null)) {
            for (int i = 0; i < smartReplies.choices.size(); i++) {
                arrayList.add(inflateReplyButton(this, getContext(), i, smartReplies, smartReplyController, notificationEntry, z));
            }
            this.mSmartRepliesGeneratedByAssistant = smartReplies.fromAssistant;
        }
        return arrayList;
    }

    public List<Button> inflateSmartActions(Context context, SmartActions smartActions, SmartReplyController smartReplyController, NotificationEntry notificationEntry, HeadsUpManager headsUpManager, boolean z) {
        SmartActions smartActions2 = smartActions;
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, this.mContext.getTheme());
        ArrayList arrayList = new ArrayList();
        int size = smartActions2.actions.size();
        for (int i = 0; i < size; i++) {
            if (smartActions2.actions.get(i).actionIntent != null) {
                arrayList.add(inflateActionButton(this, getContext(), contextThemeWrapper, i, smartActions, smartReplyController, notificationEntry, headsUpManager, z));
            }
        }
        return arrayList;
    }

    public static SmartReplyView inflate(Context context) {
        return (SmartReplyView) LayoutInflater.from(context).inflate(C0014R$layout.smart_reply_view, (ViewGroup) null);
    }

    @VisibleForTesting
    static Button inflateReplyButton(SmartReplyView smartReplyView, Context context, int i, SmartReplies smartReplies, SmartReplyController smartReplyController, NotificationEntry notificationEntry, boolean z) {
        SmartReplyView smartReplyView2 = smartReplyView;
        Button button = (Button) LayoutInflater.from(context).inflate(C0014R$layout.smart_reply_button, smartReplyView, false);
        SmartReplies smartReplies2 = smartReplies;
        CharSequence charSequence = smartReplies2.choices.get(i);
        button.setText(charSequence);
        View.OnClickListener r0 = new View.OnClickListener(new ActivityStarter.OnDismissAction(smartReplies2, charSequence, i, button, smartReplyController, notificationEntry, context) {
            public final /* synthetic */ SmartReplyView.SmartReplies f$1;
            public final /* synthetic */ CharSequence f$2;
            public final /* synthetic */ int f$3;
            public final /* synthetic */ Button f$4;
            public final /* synthetic */ SmartReplyController f$5;
            public final /* synthetic */ NotificationEntry f$6;
            public final /* synthetic */ Context f$7;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
                this.f$6 = r7;
                this.f$7 = r8;
            }

            public final boolean onDismiss() {
                return SmartReplyView.lambda$inflateReplyButton$1(SmartReplyView.this, this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, this.f$6, this.f$7);
            }
        }, notificationEntry) {
            public final /* synthetic */ ActivityStarter.OnDismissAction f$1;
            public final /* synthetic */ NotificationEntry f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            public final void onClick(View view) {
                SmartReplyView.this.mKeyguardDismissUtil.executeWhenUnlocked(this.f$1, !this.f$2.isRowPinned());
            }
        };
        if (z) {
            r0 = new DelayedOnClickListener(r0, smartReplyView2.mConstants.getOnClickInitDelay());
        }
        button.setOnClickListener(r0);
        button.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, SmartReplyView.this.getResources().getString(C0018R$string.accessibility_send_smart_reply)));
            }
        });
        setButtonColors(button, smartReplyView2.mCurrentBackgroundColor, smartReplyView2.mDefaultStrokeColor, smartReplyView2.mDefaultTextColor, smartReplyView2.mRippleColor, smartReplyView2.mStrokeWidth);
        return button;
    }

    static /* synthetic */ boolean lambda$inflateReplyButton$1(SmartReplyView smartReplyView, SmartReplies smartReplies, CharSequence charSequence, int i, Button button, SmartReplyController smartReplyController, NotificationEntry notificationEntry, Context context) {
        SmartReplyView smartReplyView2 = smartReplyView;
        SmartReplies smartReplies2 = smartReplies;
        if (smartReplyView2.mConstants.getEffectiveEditChoicesBeforeSending(smartReplies2.remoteInput.getEditChoicesBeforeSending())) {
            NotificationEntry.EditedSuggestionInfo editedSuggestionInfo = new NotificationEntry.EditedSuggestionInfo(charSequence, i);
            NotificationRemoteInputManager notificationRemoteInputManager = smartReplyView2.mRemoteInputManager;
            RemoteInput remoteInput = smartReplies2.remoteInput;
            Button button2 = button;
            notificationRemoteInputManager.activateRemoteInput(button2, new RemoteInput[]{remoteInput}, remoteInput, smartReplies2.pendingIntent, editedSuggestionInfo);
            return false;
        }
        CharSequence charSequence2 = charSequence;
        int i2 = i;
        smartReplyController.smartReplySent(notificationEntry, i, button.getText(), NotificationLogger.getNotificationLocation(notificationEntry).toMetricsEventEnum(), false);
        Bundle bundle = new Bundle();
        bundle.putString(smartReplies2.remoteInput.getResultKey(), charSequence.toString());
        Intent addFlags = new Intent().addFlags(268435456);
        RemoteInput.addResultsToIntent(new RemoteInput[]{smartReplies2.remoteInput}, addFlags, bundle);
        RemoteInput.setResultsSource(addFlags, 1);
        notificationEntry.setHasSentReply();
        try {
            smartReplies2.pendingIntent.send(context, 0, addFlags);
        } catch (PendingIntent.CanceledException e) {
            Log.w("SmartReplyView", "Unable to send smart reply", e);
        }
        smartReplyView2.mSmartReplyContainer.setVisibility(8);
        return false;
    }

    /* JADX WARNING: type inference failed for: r0v10, types: [com.android.systemui.statusbar.policy.SmartReplyView$DelayedOnClickListener] */
    /* JADX WARNING: Multi-variable type inference failed */
    @com.android.internal.annotations.VisibleForTesting
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static android.widget.Button inflateActionButton(com.android.systemui.statusbar.policy.SmartReplyView r11, android.content.Context r12, android.content.Context r13, int r14, com.android.systemui.statusbar.policy.SmartReplyView.SmartActions r15, com.android.systemui.statusbar.SmartReplyController r16, com.android.systemui.statusbar.notification.collection.NotificationEntry r17, com.android.systemui.statusbar.policy.HeadsUpManager r18, boolean r19) {
        /*
            r8 = r11
            r6 = r15
            java.util.List<android.app.Notification$Action> r0 = r6.actions
            r5 = r14
            java.lang.Object r0 = r0.get(r14)
            r2 = r0
            android.app.Notification$Action r2 = (android.app.Notification.Action) r2
            android.view.LayoutInflater r0 = android.view.LayoutInflater.from(r12)
            int r1 = com.android.systemui.C0014R$layout.smart_action_button
            r3 = 0
            android.view.View r0 = r0.inflate(r1, r11, r3)
            r9 = r0
            android.widget.Button r9 = (android.widget.Button) r9
            java.lang.CharSequence r0 = r2.title
            r9.setText(r0)
            android.graphics.drawable.Icon r0 = r2.getIcon()
            r1 = r13
            android.graphics.drawable.Drawable r0 = r0.loadDrawable(r13)
            android.content.res.Resources r1 = r12.getResources()
            int r4 = com.android.systemui.C0009R$dimen.smart_action_button_icon_size
            int r1 = r1.getDimensionPixelSize(r4)
            r0.setBounds(r3, r3, r1, r1)
            r1 = 0
            r9.setCompoundDrawables(r0, r1, r1, r1)
            com.android.systemui.statusbar.policy.-$$Lambda$SmartReplyView$tct0o0Zp_9czv90IHtUOrdcaxl0 r10 = new com.android.systemui.statusbar.policy.-$$Lambda$SmartReplyView$tct0o0Zp_9czv90IHtUOrdcaxl0
            r0 = r10
            r1 = r11
            r3 = r16
            r4 = r17
            r7 = r18
            r0.<init>(r2, r3, r4, r5, r6, r7)
            if (r19 == 0) goto L_0x0054
            com.android.systemui.statusbar.policy.SmartReplyView$DelayedOnClickListener r0 = new com.android.systemui.statusbar.policy.SmartReplyView$DelayedOnClickListener
            com.android.systemui.statusbar.policy.SmartReplyConstants r1 = r8.mConstants
            long r1 = r1.getOnClickInitDelay()
            r0.<init>(r10, r1)
            r10 = r0
        L_0x0054:
            r9.setOnClickListener(r10)
            android.view.ViewGroup$LayoutParams r0 = r9.getLayoutParams()
            com.android.systemui.statusbar.policy.SmartReplyView$LayoutParams r0 = (com.android.systemui.statusbar.policy.SmartReplyView.LayoutParams) r0
            com.android.systemui.statusbar.policy.SmartReplyView$SmartButtonType r1 = com.android.systemui.statusbar.policy.SmartReplyView.SmartButtonType.ACTION
            com.android.systemui.statusbar.policy.SmartReplyView.SmartButtonType unused = r0.buttonType = r1
            return r9
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.SmartReplyView.inflateActionButton(com.android.systemui.statusbar.policy.SmartReplyView, android.content.Context, android.content.Context, int, com.android.systemui.statusbar.policy.SmartReplyView$SmartActions, com.android.systemui.statusbar.SmartReplyController, com.android.systemui.statusbar.notification.collection.NotificationEntry, com.android.systemui.statusbar.policy.HeadsUpManager, boolean):android.widget.Button");
    }

    static /* synthetic */ void lambda$inflateActionButton$3(SmartReplyController smartReplyController, NotificationEntry notificationEntry, int i, Notification.Action action, SmartActions smartActions, HeadsUpManager headsUpManager) {
        smartReplyController.smartActionClicked(notificationEntry, i, action, smartActions.fromAssistant);
        headsUpManager.removeNotification(notificationEntry.getKey(), true);
    }

    public LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(this.mContext, attributeSet);
    }

    /* access modifiers changed from: protected */
    public LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(-2, -2);
    }

    /* access modifiers changed from: protected */
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return new LayoutParams(layoutParams.width, layoutParams.height);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int i, int i2) {
        int i3;
        Iterator it;
        int i4;
        int i5;
        int i6 = i2;
        if (View.MeasureSpec.getMode(i) == 0) {
            i3 = Integer.MAX_VALUE;
        } else {
            i3 = View.MeasureSpec.getSize(i);
        }
        resetButtonsLayoutParams();
        if (!this.mCandidateButtonQueueForSqueezing.isEmpty()) {
            Log.wtf("SmartReplyView", "Single line button queue leaked between onMeasure calls");
            this.mCandidateButtonQueueForSqueezing.clear();
        }
        SmartSuggestionMeasures smartSuggestionMeasures = new SmartSuggestionMeasures(this.mPaddingLeft + this.mPaddingRight, 0, this.mSingleLineButtonPaddingHorizontal);
        List<View> filterActionsOrReplies = filterActionsOrReplies(SmartButtonType.ACTION);
        List<View> filterActionsOrReplies2 = filterActionsOrReplies(SmartButtonType.REPLY);
        ArrayList<View> arrayList = new ArrayList<>(filterActionsOrReplies);
        arrayList.addAll(filterActionsOrReplies2);
        ArrayList arrayList2 = new ArrayList();
        SmartSuggestionMeasures smartSuggestionMeasures2 = null;
        int maxNumActions = this.mConstants.getMaxNumActions();
        Iterator it2 = arrayList.iterator();
        int i7 = 0;
        int i8 = 0;
        while (it2.hasNext()) {
            View view = (View) it2.next();
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (maxNumActions == -1 || layoutParams.buttonType != SmartButtonType.ACTION || i7 < maxNumActions) {
                i4 = maxNumActions;
                it = it2;
                view.setPadding(smartSuggestionMeasures.mButtonPaddingHorizontal, view.getPaddingTop(), smartSuggestionMeasures.mButtonPaddingHorizontal, view.getPaddingBottom());
                view.measure(MEASURE_SPEC_ANY_LENGTH, i6);
                arrayList2.add(view);
                Button button = (Button) view;
                int lineCount = button.getLineCount();
                if (lineCount >= 1 && lineCount <= 2) {
                    if (lineCount == 1) {
                        this.mCandidateButtonQueueForSqueezing.add(button);
                    }
                    SmartSuggestionMeasures clone = smartSuggestionMeasures.clone();
                    if (smartSuggestionMeasures2 == null && layoutParams.buttonType == SmartButtonType.REPLY) {
                        smartSuggestionMeasures2 = smartSuggestionMeasures.clone();
                    }
                    if (i8 == 0) {
                        i5 = 0;
                    } else {
                        i5 = this.mSpacing;
                    }
                    int measuredWidth = view.getMeasuredWidth();
                    int measuredHeight = view.getMeasuredHeight();
                    SmartSuggestionMeasures smartSuggestionMeasures3 = clone;
                    smartSuggestionMeasures.mMeasuredWidth += i5 + measuredWidth;
                    smartSuggestionMeasures.mMaxChildHeight = Math.max(smartSuggestionMeasures.mMaxChildHeight, measuredHeight);
                    if (smartSuggestionMeasures.mButtonPaddingHorizontal == this.mSingleLineButtonPaddingHorizontal && (lineCount == 2 || smartSuggestionMeasures.mMeasuredWidth > i3)) {
                        smartSuggestionMeasures.mMeasuredWidth += (i8 + 1) * this.mSingleToDoubleLineButtonWidthIncrease;
                        smartSuggestionMeasures.mButtonPaddingHorizontal = this.mDoubleLineButtonPaddingHorizontal;
                    }
                    if (smartSuggestionMeasures.mMeasuredWidth > i3) {
                        while (smartSuggestionMeasures.mMeasuredWidth > i3 && !this.mCandidateButtonQueueForSqueezing.isEmpty()) {
                            Button poll = this.mCandidateButtonQueueForSqueezing.poll();
                            int squeezeButton = squeezeButton(poll, i6);
                            if (squeezeButton != -1) {
                                smartSuggestionMeasures.mMaxChildHeight = Math.max(smartSuggestionMeasures.mMaxChildHeight, poll.getMeasuredHeight());
                                smartSuggestionMeasures.mMeasuredWidth -= squeezeButton;
                            }
                        }
                        if (smartSuggestionMeasures.mMeasuredWidth > i3) {
                            markButtonsWithPendingSqueezeStatusAs(3, arrayList2);
                            maxNumActions = i4;
                            it2 = it;
                            smartSuggestionMeasures = smartSuggestionMeasures3;
                        } else {
                            markButtonsWithPendingSqueezeStatusAs(2, arrayList2);
                        }
                    }
                    boolean unused = layoutParams.show = true;
                    i8++;
                    if (layoutParams.buttonType == SmartButtonType.ACTION) {
                        i7++;
                    }
                }
            } else {
                i4 = maxNumActions;
                it = it2;
            }
            maxNumActions = i4;
            it2 = it;
        }
        if (this.mSmartRepliesGeneratedByAssistant && !gotEnoughSmartReplies(filterActionsOrReplies2)) {
            for (View layoutParams2 : filterActionsOrReplies2) {
                boolean unused2 = ((LayoutParams) layoutParams2.getLayoutParams()).show = false;
            }
            smartSuggestionMeasures = smartSuggestionMeasures2;
        }
        this.mCandidateButtonQueueForSqueezing.clear();
        remeasureButtonsIfNecessary(smartSuggestionMeasures.mButtonPaddingHorizontal, smartSuggestionMeasures.mMaxChildHeight);
        int max = Math.max(getSuggestedMinimumHeight(), this.mPaddingTop + smartSuggestionMeasures.mMaxChildHeight + this.mPaddingBottom);
        for (View view2 : arrayList) {
            setCornerRadius((Button) view2, ((float) max) / 2.0f);
        }
        setMeasuredDimension(ViewGroup.resolveSize(Math.max(getSuggestedMinimumWidth(), smartSuggestionMeasures.mMeasuredWidth), i), ViewGroup.resolveSize(max, i6));
    }

    private static class SmartSuggestionMeasures {
        int mButtonPaddingHorizontal = -1;
        int mMaxChildHeight = -1;
        int mMeasuredWidth = -1;

        SmartSuggestionMeasures(int i, int i2, int i3) {
            this.mMeasuredWidth = i;
            this.mMaxChildHeight = i2;
            this.mButtonPaddingHorizontal = i3;
        }

        public SmartSuggestionMeasures clone() {
            return new SmartSuggestionMeasures(this.mMeasuredWidth, this.mMaxChildHeight, this.mButtonPaddingHorizontal);
        }
    }

    private boolean gotEnoughSmartReplies(List<View> list) {
        int i = 0;
        for (View layoutParams : list) {
            if (((LayoutParams) layoutParams.getLayoutParams()).show) {
                i++;
            }
        }
        if (i == 0 || i >= this.mConstants.getMinNumSystemGeneratedReplies()) {
            return true;
        }
        return false;
    }

    private List<View> filterActionsOrReplies(SmartButtonType smartButtonType) {
        ArrayList arrayList = new ArrayList();
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (childAt.getVisibility() == 0 && (childAt instanceof Button) && layoutParams.buttonType == smartButtonType) {
                arrayList.add(childAt);
            }
        }
        return arrayList;
    }

    private void resetButtonsLayoutParams() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            LayoutParams layoutParams = (LayoutParams) getChildAt(i).getLayoutParams();
            boolean unused = layoutParams.show = false;
            int unused2 = layoutParams.squeezeStatus = 0;
        }
    }

    private int squeezeButton(Button button, int i) {
        int estimateOptimalSqueezedButtonTextWidth = estimateOptimalSqueezedButtonTextWidth(button);
        if (estimateOptimalSqueezedButtonTextWidth == -1) {
            return -1;
        }
        return squeezeButtonToTextWidth(button, i, estimateOptimalSqueezedButtonTextWidth);
    }

    private int estimateOptimalSqueezedButtonTextWidth(Button button) {
        String charSequence = button.getText().toString();
        TransformationMethod transformationMethod = button.getTransformationMethod();
        if (transformationMethod != null) {
            charSequence = transformationMethod.getTransformation(charSequence, button).toString();
        }
        int length = charSequence.length();
        this.mBreakIterator.setText(charSequence);
        if (this.mBreakIterator.preceding(length / 2) == -1 && this.mBreakIterator.next() == -1) {
            return -1;
        }
        TextPaint paint = button.getPaint();
        int current = this.mBreakIterator.current();
        float desiredWidth = Layout.getDesiredWidth(charSequence, 0, current, paint);
        float desiredWidth2 = Layout.getDesiredWidth(charSequence, current, length, paint);
        float max = Math.max(desiredWidth, desiredWidth2);
        int i = (desiredWidth > desiredWidth2 ? 1 : (desiredWidth == desiredWidth2 ? 0 : -1));
        if (i != 0) {
            boolean z = i > 0;
            int maxSqueezeRemeasureAttempts = this.mConstants.getMaxSqueezeRemeasureAttempts();
            int i2 = 0;
            while (true) {
                if (i2 >= maxSqueezeRemeasureAttempts) {
                    break;
                }
                BreakIterator breakIterator = this.mBreakIterator;
                int previous = z ? breakIterator.previous() : breakIterator.next();
                if (previous == -1) {
                    break;
                }
                float desiredWidth3 = Layout.getDesiredWidth(charSequence, 0, previous, paint);
                float desiredWidth4 = Layout.getDesiredWidth(charSequence, previous, length, paint);
                float max2 = Math.max(desiredWidth3, desiredWidth4);
                if (max2 >= max) {
                    break;
                }
                if (!z ? desiredWidth3 >= desiredWidth4 : desiredWidth3 <= desiredWidth4) {
                    max = max2;
                    break;
                }
                i2++;
                max = max2;
            }
        }
        return (int) Math.ceil((double) max);
    }

    private int getLeftCompoundDrawableWidthWithPadding(Button button) {
        Drawable drawable = button.getCompoundDrawables()[0];
        if (drawable == null) {
            return 0;
        }
        return drawable.getBounds().width() + button.getCompoundDrawablePadding();
    }

    private int squeezeButtonToTextWidth(Button button, int i, int i2) {
        int measuredWidth = button.getMeasuredWidth();
        if (button.getPaddingLeft() != this.mDoubleLineButtonPaddingHorizontal) {
            measuredWidth += this.mSingleToDoubleLineButtonWidthIncrease;
        }
        button.setPadding(this.mDoubleLineButtonPaddingHorizontal, button.getPaddingTop(), this.mDoubleLineButtonPaddingHorizontal, button.getPaddingBottom());
        button.measure(View.MeasureSpec.makeMeasureSpec((this.mDoubleLineButtonPaddingHorizontal * 2) + i2 + getLeftCompoundDrawableWidthWithPadding(button), Integer.MIN_VALUE), i);
        int measuredWidth2 = button.getMeasuredWidth();
        LayoutParams layoutParams = (LayoutParams) button.getLayoutParams();
        if (button.getLineCount() > 2 || measuredWidth2 >= measuredWidth) {
            int unused = layoutParams.squeezeStatus = 3;
            return -1;
        }
        int unused2 = layoutParams.squeezeStatus = 1;
        return measuredWidth - measuredWidth2;
    }

    private void remeasureButtonsIfNecessary(int i, int i2) {
        boolean z;
        int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec(i2, 1073741824);
        int childCount = getChildCount();
        for (int i3 = 0; i3 < childCount; i3++) {
            View childAt = getChildAt(i3);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            if (layoutParams.show) {
                int measuredWidth = childAt.getMeasuredWidth();
                boolean z2 = true;
                if (layoutParams.squeezeStatus == 3) {
                    measuredWidth = Integer.MAX_VALUE;
                    z = true;
                } else {
                    z = false;
                }
                if (childAt.getPaddingLeft() != i) {
                    if (measuredWidth != Integer.MAX_VALUE) {
                        if (i == this.mSingleLineButtonPaddingHorizontal) {
                            measuredWidth -= this.mSingleToDoubleLineButtonWidthIncrease;
                        } else {
                            measuredWidth += this.mSingleToDoubleLineButtonWidthIncrease;
                        }
                    }
                    childAt.setPadding(i, childAt.getPaddingTop(), i, childAt.getPaddingBottom());
                    z = true;
                }
                if (childAt.getMeasuredHeight() == i2) {
                    z2 = z;
                }
                if (z2) {
                    childAt.measure(View.MeasureSpec.makeMeasureSpec(measuredWidth, Integer.MIN_VALUE), makeMeasureSpec);
                }
            }
        }
    }

    private void markButtonsWithPendingSqueezeStatusAs(int i, List<View> list) {
        for (View layoutParams : list) {
            LayoutParams layoutParams2 = (LayoutParams) layoutParams.getLayoutParams();
            if (layoutParams2.squeezeStatus == 1) {
                int unused = layoutParams2.squeezeStatus = i;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean z2 = true;
        if (getLayoutDirection() != 1) {
            z2 = false;
        }
        int i5 = z2 ? (i3 - i) - this.mPaddingRight : this.mPaddingLeft;
        int childCount = getChildCount();
        for (int i6 = 0; i6 < childCount; i6++) {
            View childAt = getChildAt(i6);
            if (((LayoutParams) childAt.getLayoutParams()).show) {
                int measuredWidth = childAt.getMeasuredWidth();
                int measuredHeight = childAt.getMeasuredHeight();
                int i7 = z2 ? i5 - measuredWidth : i5;
                childAt.layout(i7, 0, i7 + measuredWidth, measuredHeight);
                int i8 = measuredWidth + this.mSpacing;
                i5 = z2 ? i5 - i8 : i5 + i8;
            }
        }
    }

    /* access modifiers changed from: protected */
    public boolean drawChild(Canvas canvas, View view, long j) {
        return ((LayoutParams) view.getLayoutParams()).show && super.drawChild(canvas, view, j);
    }

    public void setBackgroundTintColor(int i) {
        if (i != this.mCurrentBackgroundColor) {
            this.mCurrentBackgroundColor = i;
            boolean z = !ContrastColorUtil.isColorLight(i);
            int i2 = -16777216 | i;
            int ensureTextContrast = ContrastColorUtil.ensureTextContrast(z ? this.mDefaultTextColorDarkBg : this.mDefaultTextColor, i2, z);
            int ensureContrast = ContrastColorUtil.ensureContrast(this.mDefaultStrokeColor, i2, z, this.mMinStrokeContrast);
            int i3 = z ? this.mRippleColorDarkBg : this.mRippleColor;
            int childCount = getChildCount();
            for (int i4 = 0; i4 < childCount; i4++) {
                setButtonColors((Button) getChildAt(i4), i, ensureContrast, ensureTextContrast, i3, this.mStrokeWidth);
            }
        }
    }

    private static void setButtonColors(Button button, int i, int i2, int i3, int i4, int i5) {
        Drawable background = button.getBackground();
        if (background instanceof RippleDrawable) {
            Drawable mutate = background.mutate();
            RippleDrawable rippleDrawable = (RippleDrawable) mutate;
            rippleDrawable.setColor(ColorStateList.valueOf(i4));
            Drawable drawable = rippleDrawable.getDrawable(0);
            if (drawable instanceof InsetDrawable) {
                Drawable drawable2 = ((InsetDrawable) drawable).getDrawable();
                if (drawable2 instanceof GradientDrawable) {
                    GradientDrawable gradientDrawable = (GradientDrawable) drawable2;
                    gradientDrawable.setColor(i);
                    gradientDrawable.setStroke(i5, i2);
                }
            }
            button.setBackground(mutate);
        }
        button.setTextColor(i3);
    }

    private void setCornerRadius(Button button, float f) {
        Drawable background = button.getBackground();
        if (background instanceof RippleDrawable) {
            Drawable drawable = ((RippleDrawable) background.mutate()).getDrawable(0);
            if (drawable instanceof InsetDrawable) {
                Drawable drawable2 = ((InsetDrawable) drawable).getDrawable();
                if (drawable2 instanceof GradientDrawable) {
                    ((GradientDrawable) drawable2).setCornerRadius(f);
                }
            }
        }
    }

    private ActivityStarter getActivityStarter() {
        if (this.mActivityStarter == null) {
            this.mActivityStarter = (ActivityStarter) Dependency.get(ActivityStarter.class);
        }
        return this.mActivityStarter;
    }

    @VisibleForTesting
    static class LayoutParams extends ViewGroup.LayoutParams {
        /* access modifiers changed from: private */
        public SmartButtonType buttonType;
        /* access modifiers changed from: private */
        public boolean show;
        /* access modifiers changed from: private */
        public int squeezeStatus;

        private LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            this.show = false;
            this.squeezeStatus = 0;
            this.buttonType = SmartButtonType.REPLY;
        }

        private LayoutParams(int i, int i2) {
            super(i, i2);
            this.show = false;
            this.squeezeStatus = 0;
            this.buttonType = SmartButtonType.REPLY;
        }

        /* access modifiers changed from: package-private */
        @VisibleForTesting
        public boolean isShown() {
            return this.show;
        }
    }

    public static class SmartReplies {
        public final List<CharSequence> choices;
        public final boolean fromAssistant;
        public final PendingIntent pendingIntent;
        public final RemoteInput remoteInput;

        public SmartReplies(List<CharSequence> list, RemoteInput remoteInput2, PendingIntent pendingIntent2, boolean z) {
            this.choices = list;
            this.remoteInput = remoteInput2;
            this.pendingIntent = pendingIntent2;
            this.fromAssistant = z;
        }
    }

    public static class SmartActions {
        public final List<Notification.Action> actions;
        public final boolean fromAssistant;

        public SmartActions(List<Notification.Action> list, boolean z) {
            this.actions = list;
            this.fromAssistant = z;
        }
    }

    private static class DelayedOnClickListener implements View.OnClickListener {
        private final View.OnClickListener mActualListener;
        private final long mInitDelayMs;
        private final long mInitTimeMs = SystemClock.elapsedRealtime();

        DelayedOnClickListener(View.OnClickListener onClickListener, long j) {
            this.mActualListener = onClickListener;
            this.mInitDelayMs = j;
        }

        public void onClick(View view) {
            if (hasFinishedInitialization()) {
                this.mActualListener.onClick(view);
                return;
            }
            Log.i("SmartReplyView", "Accidental Smart Suggestion click registered, delay: " + this.mInitDelayMs);
        }

        private boolean hasFinishedInitialization() {
            return SystemClock.elapsedRealtime() >= this.mInitTimeMs + this.mInitDelayMs;
        }
    }
}

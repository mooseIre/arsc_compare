package com.android.systemui.statusbar.notification.row;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.metrics.LogMaker;
import android.os.Bundle;
import android.provider.Settings;
import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.KeyValueListParser;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.C0008R$array;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0019R$plurals;
import com.android.systemui.C0021R$string;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.notification.row.NotificationGuts;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class NotificationSnooze extends LinearLayout implements NotificationGuts.GutsContent, View.OnClickListener {
    private static final LogMaker OPTIONS_CLOSE_LOG = new LogMaker(1142).setType(2);
    private static final LogMaker OPTIONS_OPEN_LOG = new LogMaker(1142).setType(1);
    private static final LogMaker UNDO_LOG = new LogMaker(1141).setType(4);
    private static final int[] sAccessibilityActions = {C0015R$id.action_snooze_shorter, C0015R$id.action_snooze_short, C0015R$id.action_snooze_long, C0015R$id.action_snooze_longer};
    private int mCollapsedHeight;
    private NotificationSwipeActionHelper.SnoozeOption mDefaultOption;
    private View mDivider;
    private AnimatorSet mExpandAnimation;
    private ImageView mExpandButton;
    private boolean mExpanded;
    private NotificationGuts mGutsContainer;
    private MetricsLogger mMetricsLogger = new MetricsLogger();
    private KeyValueListParser mParser = new KeyValueListParser(',');
    private StatusBarNotification mSbn;
    private NotificationSwipeActionHelper.SnoozeOption mSelectedOption;
    private TextView mSelectedOptionText;
    private NotificationSwipeActionHelper mSnoozeListener;
    private ViewGroup mSnoozeOptionContainer;
    private List<NotificationSwipeActionHelper.SnoozeOption> mSnoozeOptions;
    private View mSnoozeView;
    private boolean mSnoozing;
    private TextView mUndoButton;

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean isLeavebehind() {
        return true;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean needsFalsingProtection() {
        return false;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean shouldBeSaved() {
        return true;
    }

    public NotificationSnooze(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public NotificationSwipeActionHelper.SnoozeOption getDefaultOption() {
        return this.mDefaultOption;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void setKeyValueListParser(KeyValueListParser keyValueListParser) {
        this.mParser = keyValueListParser;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mCollapsedHeight = getResources().getDimensionPixelSize(C0012R$dimen.snooze_snackbar_min_height);
        View findViewById = findViewById(C0015R$id.notification_snooze);
        this.mSnoozeView = findViewById;
        findViewById.setOnClickListener(this);
        this.mSelectedOptionText = (TextView) findViewById(C0015R$id.snooze_option_default);
        TextView textView = (TextView) findViewById(C0015R$id.undo);
        this.mUndoButton = textView;
        textView.setOnClickListener(this);
        this.mExpandButton = (ImageView) findViewById(C0015R$id.expand_button);
        View findViewById2 = findViewById(C0015R$id.divider);
        this.mDivider = findViewById2;
        findViewById2.setAlpha(0.0f);
        ViewGroup viewGroup = (ViewGroup) findViewById(C0015R$id.snooze_options);
        this.mSnoozeOptionContainer = viewGroup;
        viewGroup.setVisibility(4);
        this.mSnoozeOptionContainer.setAlpha(0.0f);
        this.mSnoozeOptions = getDefaultSnoozeOptions();
        createOptionViews();
        setSelected(this.mDefaultOption, false);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        logOptionSelection(1137, this.mDefaultOption);
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo accessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(accessibilityNodeInfo);
        accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(C0015R$id.action_snooze_undo, getResources().getString(C0021R$string.snooze_undo)));
        int size = this.mSnoozeOptions.size();
        for (int i = 0; i < size; i++) {
            AccessibilityNodeInfo.AccessibilityAction accessibilityAction = this.mSnoozeOptions.get(i).getAccessibilityAction();
            if (accessibilityAction != null) {
                accessibilityNodeInfo.addAction(accessibilityAction);
            }
        }
    }

    public boolean performAccessibilityActionInternal(int i, Bundle bundle) {
        if (super.performAccessibilityActionInternal(i, bundle)) {
            return true;
        }
        if (i == C0015R$id.action_snooze_undo) {
            undoSnooze(this.mUndoButton);
            return true;
        }
        for (int i2 = 0; i2 < this.mSnoozeOptions.size(); i2++) {
            NotificationSwipeActionHelper.SnoozeOption snoozeOption = this.mSnoozeOptions.get(i2);
            if (snoozeOption.getAccessibilityAction() != null && snoozeOption.getAccessibilityAction().getId() == i) {
                setSelected(snoozeOption, true);
                return true;
            }
        }
        return false;
    }

    public void setSnoozeOptions(List<SnoozeCriterion> list) {
        if (list != null) {
            this.mSnoozeOptions.clear();
            this.mSnoozeOptions = getDefaultSnoozeOptions();
            int min = Math.min(1, list.size());
            for (int i = 0; i < min; i++) {
                SnoozeCriterion snoozeCriterion = list.get(i);
                this.mSnoozeOptions.add(new NotificationSnoozeOption(this, snoozeCriterion, 0, snoozeCriterion.getExplanation(), snoozeCriterion.getConfirmation(), new AccessibilityNodeInfo.AccessibilityAction(C0015R$id.action_snooze_assistant_suggestion_1, snoozeCriterion.getExplanation())));
            }
            createOptionViews();
        }
    }

    public boolean isExpanded() {
        return this.mExpanded;
    }

    public void setSnoozeListener(NotificationSwipeActionHelper notificationSwipeActionHelper) {
        this.mSnoozeListener = notificationSwipeActionHelper;
    }

    public void setStatusBarNotification(StatusBarNotification statusBarNotification) {
        this.mSbn = statusBarNotification;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public ArrayList<NotificationSwipeActionHelper.SnoozeOption> getDefaultSnoozeOptions() {
        Resources resources = getContext().getResources();
        ArrayList<NotificationSwipeActionHelper.SnoozeOption> arrayList = new ArrayList<>();
        try {
            this.mParser.setString(Settings.Global.getString(getContext().getContentResolver(), "notification_snooze_options"));
        } catch (IllegalArgumentException unused) {
            Log.e("NotificationSnooze", "Bad snooze constants");
        }
        int i = this.mParser.getInt("default", resources.getInteger(C0016R$integer.config_notification_snooze_time_default));
        int[] intArray = this.mParser.getIntArray("options_array", resources.getIntArray(C0008R$array.config_notification_snooze_times));
        for (int i2 = 0; i2 < intArray.length; i2++) {
            int[] iArr = sAccessibilityActions;
            if (i2 >= iArr.length) {
                break;
            }
            int i3 = intArray[i2];
            NotificationSwipeActionHelper.SnoozeOption createOption = createOption(i3, iArr[i2]);
            if (i2 == 0 || i3 == i) {
                this.mDefaultOption = createOption;
            }
            arrayList.add(createOption);
        }
        return arrayList;
    }

    private NotificationSwipeActionHelper.SnoozeOption createOption(int i, int i2) {
        int i3;
        Resources resources = getResources();
        boolean z = i >= 60;
        if (z) {
            i3 = C0019R$plurals.snoozeHourOptions;
        } else {
            i3 = C0019R$plurals.snoozeMinuteOptions;
        }
        int i4 = z ? i / 60 : i;
        String quantityString = resources.getQuantityString(i3, i4, Integer.valueOf(i4));
        String format = String.format(resources.getString(C0021R$string.snoozed_for_time), quantityString);
        AccessibilityNodeInfo.AccessibilityAction accessibilityAction = new AccessibilityNodeInfo.AccessibilityAction(i2, quantityString);
        int indexOf = format.indexOf(quantityString);
        if (indexOf == -1) {
            return new NotificationSnoozeOption(this, null, i, quantityString, format, accessibilityAction);
        }
        SpannableString spannableString = new SpannableString(format);
        spannableString.setSpan(new StyleSpan(1), indexOf, quantityString.length() + indexOf, 0);
        return new NotificationSnoozeOption(this, null, i, quantityString, spannableString, accessibilityAction);
    }

    private void createOptionViews() {
        this.mSnoozeOptionContainer.removeAllViews();
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        for (int i = 0; i < this.mSnoozeOptions.size(); i++) {
            NotificationSwipeActionHelper.SnoozeOption snoozeOption = this.mSnoozeOptions.get(i);
            TextView textView = (TextView) layoutInflater.inflate(C0017R$layout.notification_snooze_option, this.mSnoozeOptionContainer, false);
            this.mSnoozeOptionContainer.addView(textView);
            textView.setText(snoozeOption.getDescription());
            textView.setTag(snoozeOption);
            textView.setOnClickListener(this);
        }
    }

    private void hideSelectedOption() {
        int childCount = this.mSnoozeOptionContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mSnoozeOptionContainer.getChildAt(i);
            childAt.setVisibility(childAt.getTag() == this.mSelectedOption ? 8 : 0);
        }
    }

    private void showSnoozeOptions(boolean z) {
        this.mExpandButton.setImageResource(z ? 17302381 : 17302440);
        if (this.mExpanded != z) {
            this.mExpanded = z;
            animateSnoozeOptions(z);
            NotificationGuts notificationGuts = this.mGutsContainer;
            if (notificationGuts != null) {
                notificationGuts.onHeightChanged();
            }
        }
    }

    private void animateSnoozeOptions(final boolean z) {
        Property property = View.ALPHA;
        AnimatorSet animatorSet = this.mExpandAnimation;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        View view = this.mDivider;
        float[] fArr = new float[2];
        fArr[0] = view.getAlpha();
        float f = 1.0f;
        fArr[1] = z ? 1.0f : 0.0f;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, property, fArr);
        ViewGroup viewGroup = this.mSnoozeOptionContainer;
        float[] fArr2 = new float[2];
        fArr2[0] = viewGroup.getAlpha();
        if (!z) {
            f = 0.0f;
        }
        fArr2[1] = f;
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(viewGroup, property, fArr2);
        this.mSnoozeOptionContainer.setVisibility(0);
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mExpandAnimation = animatorSet2;
        animatorSet2.playTogether(ofFloat, ofFloat2);
        this.mExpandAnimation.setDuration(150L);
        this.mExpandAnimation.setInterpolator(z ? Interpolators.ALPHA_IN : Interpolators.ALPHA_OUT);
        this.mExpandAnimation.addListener(new AnimatorListenerAdapter() {
            /* class com.android.systemui.statusbar.notification.row.NotificationSnooze.AnonymousClass1 */
            boolean cancelled = false;

            public void onAnimationCancel(Animator animator) {
                this.cancelled = true;
            }

            public void onAnimationEnd(Animator animator) {
                if (!z && !this.cancelled) {
                    NotificationSnooze.this.mSnoozeOptionContainer.setVisibility(4);
                    NotificationSnooze.this.mSnoozeOptionContainer.setAlpha(0.0f);
                }
            }
        });
        this.mExpandAnimation.start();
    }

    private void setSelected(NotificationSwipeActionHelper.SnoozeOption snoozeOption, boolean z) {
        this.mSelectedOption = snoozeOption;
        this.mSelectedOptionText.setText(snoozeOption.getConfirmation());
        showSnoozeOptions(false);
        hideSelectedOption();
        if (z) {
            this.mSnoozeView.sendAccessibilityEvent(8);
            logOptionSelection(1138, snoozeOption);
        }
    }

    public boolean requestAccessibilityFocus() {
        if (this.mExpanded) {
            return super.requestAccessibilityFocus();
        }
        this.mSnoozeView.requestAccessibilityFocus();
        return false;
    }

    private void logOptionSelection(int i, NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
        this.mMetricsLogger.write(new LogMaker(i).setType(4).addTaggedData(1140, Integer.valueOf(this.mSnoozeOptions.indexOf(snoozeOption))).addTaggedData(1139, Long.valueOf(TimeUnit.MINUTES.toMillis((long) snoozeOption.getMinutesToSnoozeFor()))));
    }

    public void onClick(View view) {
        NotificationGuts notificationGuts = this.mGutsContainer;
        if (notificationGuts != null) {
            notificationGuts.resetFalsingCheck();
        }
        int id = view.getId();
        NotificationSwipeActionHelper.SnoozeOption snoozeOption = (NotificationSwipeActionHelper.SnoozeOption) view.getTag();
        if (snoozeOption != null) {
            setSelected(snoozeOption, true);
        } else if (id == C0015R$id.notification_snooze) {
            showSnoozeOptions(!this.mExpanded);
            this.mMetricsLogger.write(!this.mExpanded ? OPTIONS_OPEN_LOG : OPTIONS_CLOSE_LOG);
        } else {
            undoSnooze(view);
            this.mMetricsLogger.write(UNDO_LOG);
        }
    }

    private void undoSnooze(View view) {
        this.mSelectedOption = null;
        showSnoozeOptions(false);
        this.mGutsContainer.closeControls(view, false);
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public int getActualHeight() {
        return this.mExpanded ? getHeight() : this.mCollapsedHeight;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean willBeRemoved() {
        return this.mSnoozing;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public View getContentView() {
        setSelected(this.mDefaultOption, false);
        return this;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public void setGutsParent(NotificationGuts notificationGuts) {
        this.mGutsContainer = notificationGuts;
    }

    @Override // com.android.systemui.statusbar.notification.row.NotificationGuts.GutsContent
    public boolean handleCloseControls(boolean z, boolean z2) {
        NotificationSwipeActionHelper.SnoozeOption snoozeOption;
        if (!this.mExpanded || z2) {
            NotificationSwipeActionHelper notificationSwipeActionHelper = this.mSnoozeListener;
            if (notificationSwipeActionHelper == null || (snoozeOption = this.mSelectedOption) == null) {
                setSelected(this.mSnoozeOptions.get(0), false);
                return false;
            }
            this.mSnoozing = true;
            notificationSwipeActionHelper.snooze(this.mSbn, snoozeOption);
            return true;
        }
        showSnoozeOptions(false);
        return true;
    }

    public class NotificationSnoozeOption implements NotificationSwipeActionHelper.SnoozeOption {
        private AccessibilityNodeInfo.AccessibilityAction mAction;
        private CharSequence mConfirmation;
        private SnoozeCriterion mCriterion;
        private CharSequence mDescription;
        private int mMinutesToSnoozeFor;

        public NotificationSnoozeOption(NotificationSnooze notificationSnooze, SnoozeCriterion snoozeCriterion, int i, CharSequence charSequence, CharSequence charSequence2, AccessibilityNodeInfo.AccessibilityAction accessibilityAction) {
            this.mCriterion = snoozeCriterion;
            this.mMinutesToSnoozeFor = i;
            this.mDescription = charSequence;
            this.mConfirmation = charSequence2;
            this.mAction = accessibilityAction;
        }

        @Override // com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper.SnoozeOption
        public SnoozeCriterion getSnoozeCriterion() {
            return this.mCriterion;
        }

        @Override // com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper.SnoozeOption
        public CharSequence getDescription() {
            return this.mDescription;
        }

        @Override // com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper.SnoozeOption
        public CharSequence getConfirmation() {
            return this.mConfirmation;
        }

        @Override // com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper.SnoozeOption
        public int getMinutesToSnoozeFor() {
            return this.mMinutesToSnoozeFor;
        }

        @Override // com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper.SnoozeOption
        public AccessibilityNodeInfo.AccessibilityAction getAccessibilityAction() {
            return this.mAction;
        }
    }
}

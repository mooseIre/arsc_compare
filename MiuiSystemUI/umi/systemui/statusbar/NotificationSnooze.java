package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.service.notification.SnoozeCriterion;
import android.service.notification.StatusBarNotification;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Interpolators;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.statusbar.NotificationSwipeActionHelper;
import com.android.systemui.statusbar.NotificationGuts;
import java.util.ArrayList;
import java.util.List;

public class NotificationSnooze extends LinearLayout implements NotificationGuts.GutsContent, View.OnClickListener {
    private int mCollapsedHeight;
    private NotificationSwipeActionHelper.SnoozeOption mDefaultOption;
    private AnimatorSet mExpandAnimation;
    private ImageView mExpandButton;
    private boolean mExpanded;
    private int mExpandedHeight;
    private NotificationGuts mGutsContainer;
    private StatusBarNotification mSbn;
    private NotificationSwipeActionHelper.SnoozeOption mSelectedOption;
    private TextView mSelectedOptionText;
    private NotificationSwipeActionHelper mSnoozeListener;
    private ViewGroup mSnoozeOptionContainer;
    private List<NotificationSwipeActionHelper.SnoozeOption> mSnoozeOptions;
    private boolean mSnoozing;
    private TextView mUndoButton;

    public boolean isLeavebehind() {
        return true;
    }

    public NotificationSnooze(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mCollapsedHeight = getResources().getDimensionPixelSize(R.dimen.snooze_snackbar_min_height);
        this.mExpandedHeight = getResources().getDimensionPixelSize(R.dimen.snooze_snackbar_max_height);
        findViewById(R.id.notification_snooze).setOnClickListener(this);
        this.mSelectedOptionText = (TextView) findViewById(R.id.snooze_option_default);
        TextView textView = (TextView) findViewById(R.id.undo);
        this.mUndoButton = textView;
        textView.setOnClickListener(this);
        this.mExpandButton = (ImageView) findViewById(R.id.expand_button);
        ViewGroup viewGroup = (ViewGroup) findViewById(R.id.snooze_options);
        this.mSnoozeOptionContainer = viewGroup;
        viewGroup.setAlpha(0.0f);
        this.mSnoozeOptions = getDefaultSnoozeOptions();
        createOptionViews();
        setSelected(this.mDefaultOption);
    }

    public void setSnoozeOptions(List<SnoozeCriterion> list) {
        if (list != null) {
            this.mSnoozeOptions.clear();
            this.mSnoozeOptions = getDefaultSnoozeOptions();
            int min = Math.min(1, list.size());
            for (int i = 0; i < min; i++) {
                SnoozeCriterion snoozeCriterion = list.get(i);
                this.mSnoozeOptions.add(new NotificationSwipeActionHelper.SnoozeOption(snoozeCriterion, 0, snoozeCriterion.getExplanation(), snoozeCriterion.getConfirmation()));
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

    private ArrayList<NotificationSwipeActionHelper.SnoozeOption> getDefaultSnoozeOptions() {
        ArrayList<NotificationSwipeActionHelper.SnoozeOption> arrayList = new ArrayList<>();
        arrayList.add(createOption(R.string.snooze_option_15_min, 15));
        arrayList.add(createOption(R.string.snooze_option_30_min, 30));
        NotificationSwipeActionHelper.SnoozeOption createOption = createOption(R.string.snooze_option_1_hour, 60);
        this.mDefaultOption = createOption;
        arrayList.add(createOption);
        arrayList.add(createOption(R.string.snooze_option_2_hour, 120));
        return arrayList;
    }

    private NotificationSwipeActionHelper.SnoozeOption createOption(int i, int i2) {
        Resources resources = getResources();
        String string = resources.getString(i);
        String format = String.format(resources.getString(R.string.snoozed_for_time), new Object[]{string});
        SpannableString spannableString = new SpannableString(format);
        spannableString.setSpan(new StyleSpan(1), format.length() - string.length(), format.length(), 0);
        return new NotificationSwipeActionHelper.SnoozeOption((SnoozeCriterion) null, i2, resources.getString(i), spannableString);
    }

    private void createOptionViews() {
        this.mSnoozeOptionContainer.removeAllViews();
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService("layout_inflater");
        for (int i = 0; i < this.mSnoozeOptions.size(); i++) {
            NotificationSwipeActionHelper.SnoozeOption snoozeOption = this.mSnoozeOptions.get(i);
            Button button = (Button) layoutInflater.inflate(R.layout.notification_snooze_option, this.mSnoozeOptionContainer, false);
            this.mSnoozeOptionContainer.addView(button);
            button.setText(snoozeOption.description);
            button.setTag(snoozeOption);
            button.setOnClickListener(this);
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
        this.mExpandButton.setImageResource(z ? R.drawable.ic_collapse_notification : R.drawable.ic_expand_notification);
        if (this.mExpanded != z) {
            this.mExpanded = z;
            animateSnoozeOptions(z);
            NotificationGuts notificationGuts = this.mGutsContainer;
            if (notificationGuts != null) {
                notificationGuts.onHeightChanged();
            }
        }
    }

    private void animateSnoozeOptions(boolean z) {
        AnimatorSet animatorSet = this.mExpandAnimation;
        if (animatorSet != null) {
            animatorSet.cancel();
        }
        ViewGroup viewGroup = this.mSnoozeOptionContainer;
        Property property = View.ALPHA;
        float[] fArr = new float[2];
        fArr[0] = viewGroup.getAlpha();
        fArr[1] = z ? 1.0f : 0.0f;
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(viewGroup, property, fArr);
        AnimatorSet animatorSet2 = new AnimatorSet();
        this.mExpandAnimation = animatorSet2;
        animatorSet2.playTogether(new Animator[]{ofFloat});
        this.mExpandAnimation.setDuration(150);
        this.mExpandAnimation.setInterpolator(z ? Interpolators.ALPHA_IN : Interpolators.ALPHA_OUT);
        this.mExpandAnimation.start();
    }

    private void setSelected(NotificationSwipeActionHelper.SnoozeOption snoozeOption) {
        this.mSelectedOption = snoozeOption;
        this.mSelectedOptionText.setText(snoozeOption.confirmation);
        showSnoozeOptions(false);
        hideSelectedOption();
    }

    public void onClick(View view) {
        NotificationGuts notificationGuts = this.mGutsContainer;
        if (notificationGuts != null) {
            notificationGuts.resetFalsingCheck();
        }
        int id = view.getId();
        NotificationSwipeActionHelper.SnoozeOption snoozeOption = (NotificationSwipeActionHelper.SnoozeOption) view.getTag();
        if (snoozeOption != null) {
            setSelected(snoozeOption);
        } else if (id == R.id.notification_snooze) {
            showSnoozeOptions(!this.mExpanded);
        } else {
            this.mSelectedOption = null;
            int[] iArr = new int[2];
            int[] iArr2 = new int[2];
            this.mGutsContainer.getLocationOnScreen(iArr);
            view.getLocationOnScreen(iArr2);
            int i = iArr2[0] - iArr[0];
            int i2 = iArr2[1] - iArr[1];
            showSnoozeOptions(false);
            this.mGutsContainer.closeControls(i + (view.getWidth() / 2), i2 + (view.getHeight() / 2), false, false);
        }
    }

    public int getActualHeight() {
        return this.mExpanded ? this.mExpandedHeight : this.mCollapsedHeight;
    }

    public boolean willBeRemoved() {
        return this.mSnoozing;
    }

    public View getContentView() {
        setSelected(this.mDefaultOption);
        return this;
    }

    public void setGutsParent(NotificationGuts notificationGuts) {
        this.mGutsContainer = notificationGuts;
    }

    public boolean handleCloseControls(boolean z, boolean z2) {
        NotificationSwipeActionHelper.SnoozeOption snoozeOption;
        if (!this.mExpanded || z2) {
            NotificationSwipeActionHelper notificationSwipeActionHelper = this.mSnoozeListener;
            if (notificationSwipeActionHelper == null || (snoozeOption = this.mSelectedOption) == null) {
                setSelected(this.mSnoozeOptions.get(0));
                return false;
            }
            this.mSnoozing = true;
            notificationSwipeActionHelper.snooze(this.mSbn, snoozeOption);
            return true;
        }
        showSnoozeOptions(false);
        return true;
    }
}

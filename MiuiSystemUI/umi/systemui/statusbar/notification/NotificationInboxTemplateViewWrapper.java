package com.android.systemui.statusbar.notification;

import android.app.Notification;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.android.systemui.Constants;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class NotificationInboxTemplateViewWrapper extends NotificationTemplateViewWrapper {
    protected int mInboxItemTopPadding = this.mContext.getResources().getDimensionPixelSize(17105396);

    /* access modifiers changed from: protected */
    public boolean showExpandButton() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean showSingleLine() {
        return false;
    }

    protected NotificationInboxTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        handleInboxTemplateViews();
    }

    /* access modifiers changed from: protected */
    public boolean showTimeChronometer() {
        Notification notification = this.mRow.getEntry().notification.getNotification();
        return notification.showsTime() || notification.showsChronometer();
    }

    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        handleInboxTemplateViews();
        super.onContentUpdated(expandableNotificationRow);
    }

    private void handleInboxTemplateViews() {
        handleLine1();
        handleMainColumnContainer();
    }

    private void handleLine1() {
        if (showMiuiStyle()) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) this.mLine1Container.getLayoutParams();
            marginLayoutParams.leftMargin = 0;
            this.mLine1Container.setLayoutParams(marginLayoutParams);
        }
    }

    private void handleMainColumnContainer() {
        ViewGroup viewGroup = (ViewGroup) this.mMainColumnContainer;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof TextView) {
                childAt.setPadding(0, this.mInboxItemTopPadding, 0, 0);
                makeSenderSpanBold((TextView) childAt);
            }
        }
    }

    private void makeSenderSpanBold(TextView textView) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(textView.getText());
        Object[] spans = spannableStringBuilder.getSpans(0, spannableStringBuilder.length(), Object.class);
        int length = spans.length;
        int i = 0;
        while (i < length) {
            Object obj = spans[i];
            int spanStart = spannableStringBuilder.getSpanStart(obj);
            int spanEnd = spannableStringBuilder.getSpanEnd(obj);
            if (!(obj instanceof TextAppearanceSpan) || spanStart != 0) {
                i++;
            } else {
                TextAppearanceSpan textAppearanceSpan = (TextAppearanceSpan) obj;
                spannableStringBuilder.setSpan(new TextAppearanceSpan(Constants.IS_INTERNATIONAL ? null : "miui", 1, textAppearanceSpan.getTextSize(), textAppearanceSpan.getTextColor(), textAppearanceSpan.getLinkTextColor()), spanStart, spanEnd, 0);
                textView.setText(spannableStringBuilder);
                return;
            }
        }
    }
}

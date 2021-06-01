package com.android.systemui.statusbar.notification.row.wrapper;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.statusbar.notification.NotificationUtil;
import com.android.systemui.statusbar.notification.row.ExpandableNotificationRow;

public class MiuiNotificationBigTextViewWrapper extends MiuiNotificationViewWrapper {
    private TextView mBigText;
    private View mMainColumn;
    private ImageView mRightIcon;

    protected MiuiNotificationBigTextViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        resolveBigTextViews();
        handleBigTextViews();
        reprocessIfNeeded();
    }

    private void resolveBigTextViews() {
        this.mMainColumn = this.mView.findViewById(C0015R$id.notification_main_column);
        this.mRightIcon = (ImageView) this.mView.findViewById(C0015R$id.right_icon);
        this.mBigText = (TextView) this.mView.findViewById(C0015R$id.big_text);
    }

    private void reprocessIfNeeded() {
        int lineCount = this.mBigText.getLineCount();
        if (lineCount <= 1) {
            this.mBigText.post(new Runnable(lineCount) {
                /* class com.android.systemui.statusbar.notification.row.wrapper.$$Lambda$MiuiNotificationBigTextViewWrapper$UclYt6BQf_JW_5YeD9FEq57lMA */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MiuiNotificationBigTextViewWrapper.this.lambda$reprocessIfNeeded$0$MiuiNotificationBigTextViewWrapper(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$reprocessIfNeeded$0 */
    public /* synthetic */ void lambda$reprocessIfNeeded$0$MiuiNotificationBigTextViewWrapper(int i) {
        if (i != this.mBigText.getLineCount()) {
            handleBigTextViews();
        }
    }

    private void handleBigTextViews() {
        handleMainColumn();
        handleRightIcon();
        handleBigText();
    }

    private void handleMainColumn() {
        if (!showRightIcon() || showTimeChronometer()) {
            setViewMarginEnd(this.mMainColumn, 0);
            return;
        }
        setViewMarginEnd(this.mMainColumn, getDimensionPixelSize(C0012R$dimen.notification_right_icon_size) + getDimensionPixelSize(C0012R$dimen.notification_main_column_right_margin));
    }

    private void handleRightIcon() {
        if (showRightIcon()) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mRightIcon.getLayoutParams();
            layoutParams.topMargin = getDimensionPixelSize(C0012R$dimen.miui_notification_content_margin_top);
            if (showTimeChronometer()) {
                layoutParams.topMargin += getDimensionPixelSize(C0012R$dimen.notification_right_icon_margin_top);
            }
            this.mRightIcon.setLayoutParams(layoutParams);
            this.mRightIcon.setVisibility(0);
        } else {
            this.mRightIcon.setVisibility(8);
        }
        NotificationUtil.setViewRoundCorner(this.mRightIcon, (float) getDimensionPixelSize(C0012R$dimen.notification_right_icon_corner_radius));
    }

    private void handleBigText() {
        if (showRightIcon()) {
            int i = 0;
            if (showTimeChronometer()) {
                i = getDimensionPixelSize(C0012R$dimen.notification_right_icon_size) + getDimensionPixelSize(C0012R$dimen.notification_main_column_right_margin);
            }
            setViewMarginEnd(this.mBigText, i);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.MiuiNotificationViewWrapper
    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        resolveBigTextViews();
        handleBigTextViews();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.wrapper.MiuiNotificationViewWrapper
    public boolean showTimeChronometer() {
        if (showProgressBar()) {
            return false;
        }
        if (!showRightIcon() || this.mBigText.getLineCount() >= 2) {
            return super.showTimeChronometer();
        }
        return false;
    }
}

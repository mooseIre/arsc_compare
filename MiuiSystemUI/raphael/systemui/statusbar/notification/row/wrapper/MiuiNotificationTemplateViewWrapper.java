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
import miui.widget.ProgressBar;

public class MiuiNotificationTemplateViewWrapper extends MiuiNotificationViewWrapper {
    private View mMainColumn;
    private View mMiuiAction;
    private View mProgressBar;
    private ImageView mRightIcon;
    private TextView mText;
    private View mTimeLine1;

    public MiuiNotificationTemplateViewWrapper(Context context, View view, ExpandableNotificationRow expandableNotificationRow) {
        super(context, view, expandableNotificationRow);
        resolveTemplateViews();
        handleTemplateViews();
        reprocessIfNeeded();
    }

    private void resolveTemplateViews() {
        this.mMainColumn = this.mView.findViewById(C0015R$id.notification_main_column);
        this.mRightIcon = (ImageView) this.mView.findViewById(C0015R$id.right_icon);
        this.mText = (TextView) this.mView.findViewById(C0015R$id.text);
        this.mTimeLine1 = this.mView.findViewById(C0015R$id.time_line_1);
        this.mProgressBar = this.mView.findViewById(C0015R$id.progress);
        this.mMiuiAction = this.mView.findViewById(C0015R$id.miui_action);
    }

    private void reprocessIfNeeded() {
        int textLineCount = getTextLineCount();
        if (textLineCount <= 1) {
            this.mText.post(new Runnable(textLineCount) {
                /* class com.android.systemui.statusbar.notification.row.wrapper.$$Lambda$MiuiNotificationTemplateViewWrapper$Ci9gNfbFsvzimgRnTKgJurl7z7c */
                public final /* synthetic */ int f$1;

                {
                    this.f$1 = r2;
                }

                public final void run() {
                    MiuiNotificationTemplateViewWrapper.this.lambda$reprocessIfNeeded$0$MiuiNotificationTemplateViewWrapper(this.f$1);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$reprocessIfNeeded$0 */
    public /* synthetic */ void lambda$reprocessIfNeeded$0$MiuiNotificationTemplateViewWrapper(int i) {
        if (i != getTextLineCount()) {
            handleTemplateViews();
        }
    }

    private void handleTemplateViews() {
        handleMainColumn();
        handleRightIcon();
        handleTimeLine1();
        handleText();
    }

    private void handleMainColumn() {
        if (showMiuiAction()) {
            this.mMiuiAction.post(new Runnable() {
                /* class com.android.systemui.statusbar.notification.row.wrapper.$$Lambda$MiuiNotificationTemplateViewWrapper$GpO3urcxvnEYqEBC8Ickwsck */

                public final void run() {
                    MiuiNotificationTemplateViewWrapper.this.lambda$handleMainColumn$1$MiuiNotificationTemplateViewWrapper();
                }
            });
        } else if (!showRightIcon() || showTimeChronometer()) {
            setViewMarginEnd(this.mMainColumn, getDimensionPixelSize(C0012R$dimen.miui_notification_content_margin_end));
        } else {
            int dimensionPixelSize = getDimensionPixelSize(C0012R$dimen.notification_right_icon_size) + getDimensionPixelSize(C0012R$dimen.notification_main_column_right_margin);
            if (isBaseLayout()) {
                dimensionPixelSize += getDimensionPixelSize(C0012R$dimen.miui_notification_content_margin_end);
            }
            setViewMarginEnd(this.mMainColumn, dimensionPixelSize);
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$handleMainColumn$1 */
    public /* synthetic */ void lambda$handleMainColumn$1$MiuiNotificationTemplateViewWrapper() {
        setViewMarginEnd(this.mMainColumn, getDimensionPixelSize(C0012R$dimen.miui_notification_content_margin_end) + this.mMiuiAction.getMeasuredWidth() + getDimensionPixelSize(C0012R$dimen.notification_main_column_right_margin));
    }

    private void handleRightIcon() {
        if (showRightIcon()) {
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) this.mRightIcon.getLayoutParams();
            if (showTimeChronometer()) {
                layoutParams.topMargin = getDimensionPixelSize(C0012R$dimen.notification_right_icon_margin_top);
                if (isBaseLayout()) {
                    layoutParams.topMargin += getDimensionPixelSize(C0012R$dimen.miui_notification_content_margin_top);
                }
                layoutParams.gravity = 8388661;
            } else {
                layoutParams.topMargin = 0;
                layoutParams.gravity = 8388629;
            }
            this.mRightIcon.setLayoutParams(layoutParams);
            this.mRightIcon.setVisibility(0);
        } else {
            this.mRightIcon.setVisibility(8);
        }
        NotificationUtil.setViewRoundCorner(this.mRightIcon, (float) getDimensionPixelSize(C0012R$dimen.notification_right_icon_corner_radius));
    }

    private void handleTimeLine1() {
        this.mTimeLine1.setVisibility(showTimeChronometer() ? 0 : 8);
    }

    private void handleText() {
        if (showRightIcon()) {
            int i = 0;
            if (showTimeChronometer()) {
                i = getDimensionPixelSize(C0012R$dimen.notification_right_icon_size) + getDimensionPixelSize(C0012R$dimen.notification_main_column_right_margin);
            }
            setViewMarginEnd(this.mText, i);
        }
    }

    @Override // com.android.systemui.statusbar.notification.row.wrapper.NotificationViewWrapper, com.android.systemui.statusbar.notification.row.wrapper.MiuiNotificationViewWrapper
    public void onContentUpdated(ExpandableNotificationRow expandableNotificationRow) {
        super.onContentUpdated(expandableNotificationRow);
        resolveTemplateViews();
        handleTemplateViews();
        reprocessIfNeeded();
    }

    private boolean showMiuiAction() {
        View view = this.mMiuiAction;
        return view != null && view.getVisibility() == 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.wrapper.MiuiNotificationViewWrapper
    public boolean showRightIcon() {
        if (showMiuiAction()) {
            return false;
        }
        return super.showRightIcon();
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.wrapper.MiuiNotificationViewWrapper
    public boolean showProgressBar() {
        View view = this.mProgressBar;
        return (view instanceof ProgressBar) && view.getVisibility() == 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.statusbar.notification.row.wrapper.MiuiNotificationViewWrapper
    public boolean showTimeChronometer() {
        if (showMiuiAction() || showProgressBar()) {
            return false;
        }
        if (!showRightIcon() || getTextLineCount() >= 2) {
            return super.showTimeChronometer();
        }
        return false;
    }

    private int getTextLineCount() {
        TextView textView = this.mText;
        if (textView != null) {
            return textView.getLineCount();
        }
        return 0;
    }

    private boolean isBaseLayout() {
        return "base".equals(this.mView.getTag());
    }
}

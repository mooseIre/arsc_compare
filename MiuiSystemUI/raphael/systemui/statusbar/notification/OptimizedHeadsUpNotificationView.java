package com.android.systemui.statusbar.notification;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.ExpandableNotificationRow;

public class OptimizedHeadsUpNotificationView extends FrameLayout {
    private TextView mAction;
    private View mContent;
    private Context mContext;
    private ImageView mIcon;
    private boolean mIsGameModeUI;
    private View mMiniWindowBar;
    private TextView mText;
    private TextView mTitle;
    private ExpandableNotificationRow row;

    public OptimizedHeadsUpNotificationView(Context context) {
        this(context, (AttributeSet) null);
    }

    public OptimizedHeadsUpNotificationView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OptimizedHeadsUpNotificationView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mContext = context;
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mIcon = (ImageView) findViewById(R.id.icon);
        this.mTitle = (TextView) findViewById(R.id.title);
        this.mText = (TextView) findViewById(R.id.text);
        this.mAction = (TextView) findViewById(R.id.action);
        this.mMiniWindowBar = findViewById(R.id.mini_window_bar);
        this.mContent = findViewById(R.id.content);
    }

    public void setRow(ExpandableNotificationRow expandableNotificationRow) {
        this.row = expandableNotificationRow;
    }

    public void wrapMiniWindowBar(ExpandableNotificationRow expandableNotificationRow, boolean z) {
        if (!expandableNotificationRow.getEntry().mIsShowMiniWindowBar || z) {
            this.mMiniWindowBar.setVisibility(8);
        } else {
            this.mMiniWindowBar.setVisibility(0);
        }
    }

    public View getContent() {
        return this.mContent;
    }

    public void wrapIconView(ImageView imageView) {
        if (this.mIcon != null && imageView != null && imageView.getDrawable() != null && imageView.getDrawable().getConstantState() != null) {
            this.mIcon.setImageDrawable(imageView.getDrawable().getConstantState().newDrawable());
        }
    }

    public void wrapTitleView(TextView textView, boolean z) {
        TextView textView2 = this.mTitle;
        if (textView2 != null && textView != null) {
            textView2.setTextColor(this.mContext.getColor(z ? R.color.optimized_game_heads_up_notification_text : R.color.optimized_heads_up_notification_text));
            if (!TextUtils.isEmpty(textView.getText())) {
                this.mTitle.setVisibility(0);
                this.mTitle.setText(textView.getText());
            } else {
                this.mTitle.setVisibility(8);
            }
            this.mIsGameModeUI = z;
        }
    }

    public void wrapTextView(TextView textView, boolean z) {
        TextView textView2 = this.mText;
        if (textView2 != null && textView != null) {
            textView2.setTextColor(this.mContext.getColor(z ? R.color.optimized_game_heads_up_notification_text : R.color.optimized_heads_up_notification_text));
            if (this.mTitle != null && !TextUtils.isEmpty(textView.getText()) && textView.getText().toString().contains(this.mTitle.getText())) {
                this.mTitle.setVisibility(8);
            }
            this.mText.setText(textView.getText());
            this.mIsGameModeUI = z;
        }
    }

    public TextView getActionView() {
        return this.mAction;
    }

    public boolean isGameModeUi() {
        return this.mIsGameModeUI;
    }

    public void hideMiniWindowBar() {
        this.mMiniWindowBar.setVisibility(8);
    }
}

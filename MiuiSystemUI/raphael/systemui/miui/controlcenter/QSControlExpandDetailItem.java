package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Dependency;
import com.android.systemui.miui.controlcenter.ExpandInfoController;
import com.android.systemui.plugins.R;

public class QSControlExpandDetailItem extends LinearLayout implements ExpandInfoController.Callback {
    private Context mContext;
    /* access modifiers changed from: private */
    public ExpandInfoController mExpandInfoController = ((ExpandInfoController) Dependency.get(ExpandInfoController.class));
    /* access modifiers changed from: private */
    public ExpandInfoController.Info mInfo = new ExpandInfoController.Info();
    private TextView mStatus;
    private ImageView mStatusIcon;
    private TextView mTitle;
    private int mType;

    public QSControlExpandDetailItem(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        View inflate = LayoutInflater.from(this.mContext).inflate(R.layout.qs_control_expand_detail_item, this);
        this.mStatusIcon = (ImageView) inflate.findViewById(R.id.status_icon);
        this.mTitle = (TextView) inflate.findViewById(R.id.title);
        this.mStatus = (TextView) inflate.findViewById(R.id.status);
        setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (QSControlExpandDetailItem.this.mInfo == null) {
                    return;
                }
                if (QSControlExpandDetailItem.this.mInfo.initialized) {
                    QSControlExpandDetailItem.this.setSelected(true);
                } else if (!TextUtils.isEmpty(QSControlExpandDetailItem.this.mInfo.action)) {
                    QSControlExpandDetailItem.this.mExpandInfoController.startActivity(QSControlExpandDetailItem.this.mInfo.action);
                } else {
                    QSControlExpandDetailItem.this.mExpandInfoController.startActivityByUri(QSControlExpandDetailItem.this.mInfo.uri);
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
    }

    public void updateInfo(int i, ExpandInfoController.Info info) {
        if (this.mType == i) {
            this.mInfo.copy(info);
            setVisibility(this.mInfo.available ? 0 : 8);
            TextView textView = this.mTitle;
            if (textView != null) {
                textView.setText(info.title);
                this.mStatus.setText(info.status);
                this.mStatusIcon.setImageBitmap(info.icon);
                if (!TextUtils.isEmpty(info.status)) {
                    String str = info.unit;
                    if (str == null) {
                        str = "";
                    }
                    SpannableString spannableString = new SpannableString(info.status + " " + str);
                    spannableString.setSpan(new TextAppearanceSpan(this.mContext, R.style.TextAppearance_QSControl_ExpandItemSubTitle), 0, info.status.length() + -1, 18);
                    spannableString.setSpan(new TextAppearanceSpan(this.mContext, R.style.TextAppearance_QSControl_ExpandItemUnit), info.status.length(), spannableString.length(), 18);
                    this.mStatus.setText(spannableString);
                }
            }
            Log.d("QSControlExpandDetailItem", "type:" + this.mType + " " + this.mInfo.toString());
        }
    }

    public void updateSelectedType(int i) {
        setSelected(this.mType == this.mExpandInfoController.getSelectedType());
    }
}

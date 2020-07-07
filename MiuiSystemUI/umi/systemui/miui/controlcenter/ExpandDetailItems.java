package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.content.res.Configuration;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.plugins.R;
import com.android.systemui.qs.QSDetailItems;

public class ExpandDetailItems extends QSDetailItems {
    /* access modifiers changed from: private */
    public Context mContext;
    private int mOrientation;

    public ExpandDetailItems(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mAdapter = new ExpandAdapter();
    }

    public static ExpandDetailItems convertOrInflate(Context context, View view, ViewGroup viewGroup) {
        context.getColor(R.color.qs_control_expand_item_selected_color);
        if (view instanceof QSDetailItems) {
            return (ExpandDetailItems) view;
        }
        return (ExpandDetailItems) LayoutInflater.from(context).inflate(R.layout.qs_control_expand_detail_items, viewGroup, false);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int i = this.mOrientation;
        int i2 = configuration.orientation;
        if (i != i2) {
            this.mOrientation = i2;
            if (i2 == 2) {
                layoutParams.height = -2;
            } else {
                layoutParams.height = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_control_expand_detail_items_height);
            }
            setLayoutParams(layoutParams);
        }
    }

    protected static class CompleteItemHolder extends QSDetailItems.ItemHolder {
        protected ImageView icon;
        protected ImageView icon2;
        protected TextView summary;
        protected TextView title;

        public CompleteItemHolder(View view) {
            super(view);
            this.icon = (ImageView) view.findViewById(R.id.status_icon);
            this.icon2 = (ImageView) view.findViewById(R.id.select_icon);
            this.title = (TextView) view.findViewById(R.id.title);
            this.summary = (TextView) view.findViewById(R.id.summary);
        }
    }

    private class ExpandAdapter extends QSDetailItems.Adapter {
        private ExpandAdapter() {
            super();
        }

        public QSDetailItems.ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            if (i != 2) {
                return new CompleteItemHolder(LayoutInflater.from(ExpandDetailItems.this.mContext).inflate(R.layout.qs_control_expand_detail_item, viewGroup, false));
            }
            return new QSDetailItems.LineItemHolder(LayoutInflater.from(ExpandDetailItems.this.mContext).inflate(R.layout.qs_detail_line_item, viewGroup, false));
        }

        public void onBindViewHolder(QSDetailItems.ItemHolder itemHolder, int i) {
            if (ExpandDetailItems.this.mItems[i].type == 1) {
                final QSDetailItems.Item item = ExpandDetailItems.this.mItems[i];
                CompleteItemHolder completeItemHolder = (CompleteItemHolder) itemHolder;
                completeItemHolder.itemView.setVisibility(item.activated ? 0 : 4);
                completeItemHolder.itemView.setBackgroundColor(item.selected ? ExpandDetailItems.this.mContext.getColor(R.color.qs_control_detail_selected_color) : 0);
                completeItemHolder.icon.setImageDrawable(item.drawable);
                completeItemHolder.itemView.setActivated(item.activated);
                completeItemHolder.itemView.setSelected(item.selected);
                completeItemHolder.title.setText(item.line1);
                completeItemHolder.title.setMaxLines(1);
                completeItemHolder.summary.setVisibility(0);
                completeItemHolder.icon2.setVisibility(item.selected ? 0 : 8);
                SpannableString spannableString = new SpannableString(item.line2 + " " + item.unit);
                if (item.selected) {
                    completeItemHolder.title.setTextAppearance(R.style.TextAppearance_QSControl_ExpandItemTitleSelect);
                } else {
                    completeItemHolder.title.setTextAppearance(R.style.TextAppearance_QSControl_ExpandItemTitle);
                }
                if (!TextUtils.isEmpty(item.line2)) {
                    if (item.selected) {
                        spannableString.setSpan(new TextAppearanceSpan(ExpandDetailItems.this.mContext, R.style.TextAppearance_QSControl_ExpandItemSubTitleSelect), 0, item.line2.length() - 1, 18);
                        spannableString.setSpan(new TextAppearanceSpan(ExpandDetailItems.this.mContext, R.style.TextAppearance_QSControl_ExpandItemUnitSelect), item.line2.length(), spannableString.length(), 18);
                    } else {
                        spannableString.setSpan(new TextAppearanceSpan(ExpandDetailItems.this.mContext, R.style.TextAppearance_QSControl_ExpandItemSubTitle), 0, item.line2.length() - 1, 18);
                        spannableString.setSpan(new TextAppearanceSpan(ExpandDetailItems.this.mContext, R.style.TextAppearance_QSControl_ExpandItemUnit), item.line2.length(), spannableString.length(), 18);
                    }
                    completeItemHolder.summary.setText(spannableString);
                }
                if (item.activated) {
                    completeItemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View view) {
                            if (ExpandDetailItems.this.mCallback != null) {
                                ExpandDetailItems.this.mCallback.onDetailItemClick(item);
                            }
                        }
                    });
                } else {
                    completeItemHolder.itemView.setOnClickListener((View.OnClickListener) null);
                }
            }
        }
    }
}

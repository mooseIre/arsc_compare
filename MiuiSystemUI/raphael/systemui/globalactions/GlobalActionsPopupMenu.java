package com.android.systemui.globalactions;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.ListView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;

public class GlobalActionsPopupMenu extends ListPopupWindow {
    private ListAdapter mAdapter;
    private Context mContext;
    private int mGlobalActionsSidePadding = 0;
    private boolean mIsDropDownMode;
    private int mMenuVerticalPadding = 0;
    private AdapterView.OnItemLongClickListener mOnItemLongClickListener;

    public GlobalActionsPopupMenu(Context context, boolean z) {
        super(context);
        this.mContext = context;
        Resources resources = context.getResources();
        setBackgroundDrawable(resources.getDrawable(C0013R$drawable.rounded_bg_full, context.getTheme()));
        this.mIsDropDownMode = z;
        setWindowLayoutType(2020);
        setInputMethodMode(2);
        setModal(true);
        this.mGlobalActionsSidePadding = resources.getDimensionPixelSize(C0012R$dimen.global_actions_side_margin);
        if (!z) {
            this.mMenuVerticalPadding = resources.getDimensionPixelSize(C0012R$dimen.control_menu_vertical_padding);
        }
    }

    public void setAdapter(ListAdapter listAdapter) {
        this.mAdapter = listAdapter;
        super.setAdapter(listAdapter);
    }

    public void show() {
        super.show();
        if (this.mOnItemLongClickListener != null) {
            getListView().setOnItemLongClickListener(this.mOnItemLongClickListener);
        }
        ListView listView = getListView();
        Resources resources = this.mContext.getResources();
        setVerticalOffset((-getAnchorView().getHeight()) / 2);
        if (this.mIsDropDownMode) {
            listView.setDividerHeight(resources.getDimensionPixelSize(C0012R$dimen.control_list_divider));
            listView.setDivider(resources.getDrawable(C0013R$drawable.controls_list_divider_inset));
        } else if (this.mAdapter != null) {
            double d = (double) Resources.getSystem().getDisplayMetrics().widthPixels;
            int makeMeasureSpec = View.MeasureSpec.makeMeasureSpec((int) (0.9d * d), Integer.MIN_VALUE);
            int i = 0;
            for (int i2 = 0; i2 < this.mAdapter.getCount(); i2++) {
                View view = this.mAdapter.getView(i2, null, listView);
                view.measure(makeMeasureSpec, 0);
                i = Math.max(view.getMeasuredWidth(), i);
            }
            int max = Math.max(i, (int) (d * 0.5d));
            int i3 = this.mMenuVerticalPadding;
            listView.setPadding(0, i3, 0, i3);
            setWidth(max);
            if (getAnchorView().getLayoutDirection() == 0) {
                setHorizontalOffset((getAnchorView().getWidth() - this.mGlobalActionsSidePadding) - max);
            } else {
                setHorizontalOffset(this.mGlobalActionsSidePadding);
            }
        } else {
            return;
        }
        super.show();
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener onItemLongClickListener) {
        this.mOnItemLongClickListener = onItemLongClickListener;
    }
}

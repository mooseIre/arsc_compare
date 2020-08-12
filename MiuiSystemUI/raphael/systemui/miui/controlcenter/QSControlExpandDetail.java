package com.android.systemui.miui.controlcenter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.Dependency;
import com.android.systemui.miui.controlcenter.ExpandInfoController;
import com.android.systemui.miui.statusbar.analytics.SystemUIStat;
import com.android.systemui.plugins.R;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.QSDetailItems;
import com.android.systemui.qs.QSPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class QSControlExpandDetail implements ExpandInfoController.Callback {
    /* access modifiers changed from: private */
    public Context mContext;
    private ExpandDetailAdapter mDetailAdapter = new ExpandDetailAdapter();
    private View mExpandIndicatorView;
    /* access modifiers changed from: private */
    public ExpandInfoController mExpandInfoController = ((ExpandInfoController) Dependency.get(ExpandInfoController.class));
    private QSPanel.Record mRecord;
    private View mTileView;

    public void updateInfo(int i, ExpandInfoController.Info info) {
    }

    public QSControlExpandDetail(Context context, View view, View view2) {
        this.mContext = context;
        this.mExpandInfoController.addCallback(this);
        this.mTileView = view;
        this.mExpandIndicatorView = view2;
        this.mRecord = new QSPanel.Record();
        QSPanel.Record record = this.mRecord;
        record.detailAdapter = this.mDetailAdapter;
        record.wholeView = this.mTileView;
        record.translateView = this.mExpandIndicatorView;
    }

    public void show() {
        this.mExpandInfoController.getContentView().showExpandDetail(true, this.mRecord);
    }

    public void updateInfosMap() {
        this.mDetailAdapter.updateItems();
    }

    public void updateSelectedType(int i) {
        this.mDetailAdapter.updateItems();
    }

    public void updateResources() {
        ExpandDetailItems unused = this.mDetailAdapter.mItems = null;
    }

    private class ExpandDetailAdapter implements DetailAdapter, QSDetailItems.Callback {
        /* access modifiers changed from: private */
        public ExpandDetailItems mItems;

        public int getMetricsCategory() {
            return 167;
        }

        public Intent getSettingsIntent() {
            return null;
        }

        public boolean getToggleEnabled() {
            return false;
        }

        public Boolean getToggleState() {
            return null;
        }

        public boolean hasHeader() {
            return true;
        }

        public boolean hasSwitch() {
            return false;
        }

        public void onDetailItemDisconnect(QSDetailItems.Item item) {
        }

        public void setToggleState(boolean z) {
        }

        private ExpandDetailAdapter() {
        }

        public CharSequence getTitle() {
            return QSControlExpandDetail.this.mContext.getString(R.string.qs_control_expand_detail_title);
        }

        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            this.mItems = ExpandDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems.setTagSuffix("expand");
            this.mItems.setCallback(this);
            setItemsVisible(true);
            updateItems();
            return this.mItems;
        }

        public void onDetailItemClick(QSDetailItems.Item item) {
            if (item != null && item.tag != null) {
                ExpandInfoController.Info info = QSControlExpandDetail.this.mExpandInfoController.getInfosMap().get(item.tag);
                if (info.initialized) {
                    this.mItems.setItemClicked(true);
                    int intValue = ((Integer) item.tag).intValue();
                    if (intValue != QSControlExpandDetail.this.mExpandInfoController.getSelectedType()) {
                        QSControlExpandDetail.this.mExpandInfoController.setSelectedType(intValue);
                        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent("expand_tile_switch");
                    }
                } else if (!TextUtils.isEmpty(info.action)) {
                    QSControlExpandDetail.this.mExpandInfoController.startActivity(info.action);
                } else {
                    QSControlExpandDetail.this.mExpandInfoController.startActivityByUri(info.uri);
                }
            }
        }

        public void setItemsVisible(boolean z) {
            ExpandDetailItems expandDetailItems = this.mItems;
            if (expandDetailItems != null) {
                expandDetailItems.setItemsVisible(z);
            }
        }

        /* access modifiers changed from: private */
        public void updateItems() {
            if (this.mItems != null) {
                ArrayList arrayList = new ArrayList();
                HashMap<Integer, ExpandInfoController.Info> infosMap = QSControlExpandDetail.this.mExpandInfoController.getInfosMap();
                Set<Integer> keySet = infosMap.keySet();
                int selectedType = QSControlExpandDetail.this.mExpandInfoController.getSelectedType();
                if (selectedType == 16) {
                    QSDetailItems.Item acquireItem = this.mItems.acquireItem();
                    acquireItem.selected = true;
                    setItemInfo(acquireItem, QSControlExpandDetail.this.mExpandInfoController.getSuperPowerInfo(), selectedType);
                    arrayList.add(acquireItem);
                } else {
                    for (Integer next : keySet) {
                        QSDetailItems.Item acquireItem2 = this.mItems.acquireItem();
                        ExpandInfoController.Info info = infosMap.get(next);
                        if (info.available) {
                            setItemInfo(acquireItem2, info, next.intValue());
                            arrayList.add(acquireItem2);
                        }
                    }
                }
                this.mItems.setItems((QSDetailItems.Item[]) arrayList.toArray(new QSDetailItems.Item[arrayList.size()]));
            }
        }

        private void setItemInfo(QSDetailItems.Item item, ExpandInfoController.Info info, int i) {
            item.activated = info.available;
            item.drawable = new BitmapDrawable(info.icon);
            item.line2 = info.status;
            item.unit = info.unit;
            item.line1 = info.title;
            item.tag = Integer.valueOf(i);
            item.initailed = info.initialized;
            item.selected = QSControlExpandDetail.this.mExpandInfoController.getSelectedType() == i;
        }
    }
}

package com.android.systemui.controlcenter.phone.detail;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import com.android.systemui.C0021R$string;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ExpandInfoController;
import com.android.systemui.controlcenter.phone.widget.MiuiQSPanel$MiuiRecord;
import com.android.systemui.plugins.qs.DetailAdapter;
import com.android.systemui.qs.MiuiQSDetailItems;
import com.miui.systemui.analytics.SystemUIStat;
import com.miui.systemui.events.ExpandTileSwitchEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class QSControlExpandDetail implements ExpandInfoController.Callback {
    private Context mContext;
    private ExpandDetailAdapter mDetailAdapter = new ExpandDetailAdapter();
    private View mExpandIndicatorView;
    private ExpandInfoController mExpandInfoController = ((ExpandInfoController) Dependency.get(ExpandInfoController.class));
    private MiuiQSPanel$MiuiRecord mRecord;
    private View mTileView;

    @Override // com.android.systemui.controlcenter.phone.ExpandInfoController.Callback
    public void updateInfo(int i, ExpandInfoController.Info info) {
    }

    public QSControlExpandDetail(Context context, View view, View view2) {
        this.mContext = context;
        this.mTileView = view;
        this.mExpandIndicatorView = view2;
        MiuiQSPanel$MiuiRecord miuiQSPanel$MiuiRecord = new MiuiQSPanel$MiuiRecord();
        this.mRecord = miuiQSPanel$MiuiRecord;
        miuiQSPanel$MiuiRecord.detailAdapter = this.mDetailAdapter;
        miuiQSPanel$MiuiRecord.wholeView = this.mTileView;
        miuiQSPanel$MiuiRecord.translateView = this.mExpandIndicatorView;
    }

    public void show() {
        this.mExpandInfoController.getContentView().showExpandDetail(true, this.mRecord);
    }

    public void addExpandInfoCallback() {
        this.mExpandInfoController.addCallback(this);
    }

    public void removeExpandInfoCallback() {
        ExpandInfoController expandInfoController = this.mExpandInfoController;
        if (expandInfoController != null) {
            expandInfoController.removeCallback(this);
        }
    }

    @Override // com.android.systemui.controlcenter.phone.ExpandInfoController.Callback
    public void updateInfosMap() {
        this.mDetailAdapter.updateItems();
    }

    @Override // com.android.systemui.controlcenter.phone.ExpandInfoController.Callback
    public void updateSelectedType(int i) {
        this.mDetailAdapter.updateItems();
    }

    public void updateResources() {
        this.mDetailAdapter.mItems = null;
    }

    private class ExpandDetailAdapter implements DetailAdapter, MiuiQSDetailItems.Callback {
        private ExpandDetailItems mItems;

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public int getMetricsCategory() {
            return 167;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Intent getSettingsIntent() {
            return null;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public boolean getToggleEnabled() {
            return false;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public Boolean getToggleState() {
            return null;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public boolean hasHeader() {
            return true;
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public void setToggleState(boolean z) {
        }

        private ExpandDetailAdapter() {
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public CharSequence getTitle() {
            return QSControlExpandDetail.this.mContext.getString(C0021R$string.qs_control_expand_detail_title);
        }

        @Override // com.android.systemui.plugins.qs.DetailAdapter
        public View createDetailView(Context context, View view, ViewGroup viewGroup) {
            ExpandDetailItems convertOrInflate = ExpandDetailItems.convertOrInflate(context, view, viewGroup);
            this.mItems = convertOrInflate;
            convertOrInflate.setTagSuffix("expand");
            this.mItems.setCallback(this);
            setItemsVisible(true);
            updateItems();
            return this.mItems;
        }

        @Override // com.android.systemui.qs.MiuiQSDetailItems.Callback
        public void onDetailItemClick(MiuiQSDetailItems.Item item) {
            if (item != null && item.tag != null) {
                ExpandInfoController.Info info = QSControlExpandDetail.this.mExpandInfoController.getInfosMap().get(item.tag);
                if (info.initialized) {
                    this.mItems.setItemClicked(true);
                    int intValue = ((Integer) item.tag).intValue();
                    if (intValue != QSControlExpandDetail.this.mExpandInfoController.getSelectedType()) {
                        QSControlExpandDetail.this.mExpandInfoController.setSelectedType(intValue);
                        ((SystemUIStat) Dependency.get(SystemUIStat.class)).handleControlCenterEvent(new ExpandTileSwitchEvent());
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
        /* access modifiers changed from: public */
        private void updateItems() {
            if (this.mItems != null) {
                ArrayList arrayList = new ArrayList();
                HashMap<Integer, ExpandInfoController.Info> infosMap = QSControlExpandDetail.this.mExpandInfoController.getInfosMap();
                Set<Integer> keySet = infosMap.keySet();
                int selectedType = QSControlExpandDetail.this.mExpandInfoController.getSelectedType();
                if (selectedType == 16) {
                    MiuiQSDetailItems.Item acquireItem = this.mItems.acquireItem();
                    acquireItem.selected = true;
                    setItemInfo(acquireItem, QSControlExpandDetail.this.mExpandInfoController.getSuperPowerInfo(), selectedType);
                    arrayList.add(acquireItem);
                } else {
                    for (Integer num : keySet) {
                        MiuiQSDetailItems.Item acquireItem2 = this.mItems.acquireItem();
                        ExpandInfoController.Info info = infosMap.get(num);
                        if (info.available) {
                            setItemInfo(acquireItem2, info, num.intValue());
                            arrayList.add(acquireItem2);
                        }
                    }
                }
                this.mItems.setItems((MiuiQSDetailItems.Item[]) arrayList.toArray(new MiuiQSDetailItems.Item[arrayList.size()]));
            }
        }

        private void setItemInfo(MiuiQSDetailItems.Item item, ExpandInfoController.Info info, int i) {
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

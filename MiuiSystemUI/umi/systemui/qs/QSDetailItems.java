package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0017R$layout;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.plugins.qs.QSTile;

public class QSDetailItems extends FrameLayout {
    private static final boolean DEBUG = Log.isLoggable("QSDetailItems", 3);
    protected Adapter mAdapter = new Adapter();
    protected Callback mCallback;
    private final Context mContext;
    private View mEmpty;
    private TextView mEmptyText;
    private AutoSizingList mItemList;
    protected Item[] mItems;
    private boolean mItemsVisible = true;
    private final int mQsDetailIconOverlaySize;
    private String mTag;

    public interface Callback {
        void onDetailItemClick(Item item);

        void onDetailItemDisconnect(Item item);
    }

    public static class Item {
        public boolean canDisconnect;
        public QSTile.Icon icon;
        public int icon2 = -1;
        public int iconResId;
        public CharSequence line1;
        public CharSequence line2;
        public Drawable overlay;
    }

    public QSDetailItems(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        new H();
        this.mContext = context;
        this.mTag = "QSDetailItems";
        this.mQsDetailIconOverlaySize = (int) getResources().getDimension(C0012R$dimen.qs_detail_icon_overlay_size);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        AutoSizingList autoSizingList = (AutoSizingList) findViewById(16908298);
        this.mItemList = autoSizingList;
        autoSizingList.setVisibility(8);
        this.mItemList.setAdapter(this.mAdapter);
        View findViewById = findViewById(16908292);
        this.mEmpty = findViewById;
        findViewById.setVisibility(8);
        this.mEmptyText = (TextView) this.mEmpty.findViewById(16908310);
        ImageView imageView = (ImageView) this.mEmpty.findViewById(16908294);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        FontSizeUtils.updateFontSize(this.mEmptyText, C0012R$dimen.qs_detail_empty_text_size);
        int childCount = this.mItemList.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = this.mItemList.getChildAt(i);
            FontSizeUtils.updateFontSize(childAt, 16908310, C0012R$dimen.qs_detail_item_primary_text_size);
            FontSizeUtils.updateFontSize(childAt, 16908304, C0012R$dimen.qs_detail_item_secondary_text_size);
        }
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (DEBUG) {
            Log.d(this.mTag, "onAttachedToWindow");
        }
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (DEBUG) {
            Log.d(this.mTag, "onDetachedFromWindow");
        }
        this.mCallback = null;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetCallback(Callback callback) {
        this.mCallback = callback;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetItems(Item[] itemArr) {
        int i = 0;
        int length = itemArr != null ? itemArr.length : 0;
        this.mEmpty.setVisibility(length == 0 ? 0 : 8);
        AutoSizingList autoSizingList = this.mItemList;
        if (length == 0) {
            i = 8;
        }
        autoSizingList.setVisibility(i);
        this.mItems = itemArr;
        this.mAdapter.notifyDataSetChanged();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleSetItemsVisible(boolean z) {
        if (this.mItemsVisible != z) {
            this.mItemsVisible = z;
            for (int i = 0; i < this.mItemList.getChildCount(); i++) {
                this.mItemList.getChildAt(i).setVisibility(this.mItemsVisible ? 0 : 4);
            }
        }
    }

    /* access modifiers changed from: protected */
    public class Adapter extends BaseAdapter {
        public long getItemId(int i) {
            return 0;
        }

        protected Adapter() {
        }

        public int getCount() {
            Item[] itemArr = QSDetailItems.this.mItems;
            if (itemArr != null) {
                return itemArr.length;
            }
            return 0;
        }

        public Object getItem(int i) {
            return QSDetailItems.this.mItems[i];
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            QSDetailItems qSDetailItems = QSDetailItems.this;
            final Item item = qSDetailItems.mItems[i];
            if (view == null) {
                view = LayoutInflater.from(qSDetailItems.mContext).inflate(C0017R$layout.miui_qs_detail_item, viewGroup, false);
            }
            view.setVisibility(QSDetailItems.this.mItemsVisible ? 0 : 4);
            ImageView imageView = (ImageView) view.findViewById(16908294);
            QSTile.Icon icon = item.icon;
            if (icon != null) {
                imageView.setImageDrawable(icon.getDrawable(imageView.getContext()));
            } else {
                imageView.setImageResource(item.iconResId);
            }
            imageView.getOverlay().clear();
            Drawable drawable = item.overlay;
            if (drawable != null) {
                drawable.setBounds(0, 0, QSDetailItems.this.mQsDetailIconOverlaySize, QSDetailItems.this.mQsDetailIconOverlaySize);
                imageView.getOverlay().add(item.overlay);
            }
            TextView textView = (TextView) view.findViewById(16908310);
            textView.setText(item.line1);
            TextView textView2 = (TextView) view.findViewById(16908304);
            boolean z = !TextUtils.isEmpty(item.line2);
            textView.setMaxLines(z ? 1 : 2);
            textView2.setVisibility(z ? 0 : 8);
            textView2.setText(z ? item.line2 : null);
            view.setOnClickListener(new View.OnClickListener() {
                /* class com.android.systemui.qs.QSDetailItems.Adapter.AnonymousClass1 */

                public void onClick(View view) {
                    Callback callback = QSDetailItems.this.mCallback;
                    if (callback != null) {
                        callback.onDetailItemClick(item);
                    }
                }
            });
            ImageView imageView2 = (ImageView) view.findViewById(16908296);
            if (item.canDisconnect) {
                imageView2.setImageResource(C0013R$drawable.ic_qs_cancel);
                imageView2.setVisibility(0);
                imageView2.setClickable(true);
                imageView2.setOnClickListener(new View.OnClickListener() {
                    /* class com.android.systemui.qs.QSDetailItems.Adapter.AnonymousClass2 */

                    public void onClick(View view) {
                        Callback callback = QSDetailItems.this.mCallback;
                        if (callback != null) {
                            callback.onDetailItemDisconnect(item);
                        }
                    }
                });
            } else if (item.icon2 != -1) {
                imageView2.setVisibility(0);
                imageView2.setImageResource(item.icon2);
                imageView2.setClickable(false);
            } else {
                imageView2.setVisibility(8);
            }
            return view;
        }
    }

    private class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message message) {
            int i = message.what;
            boolean z = true;
            if (i == 1) {
                QSDetailItems.this.handleSetItems((Item[]) message.obj);
            } else if (i == 2) {
                QSDetailItems.this.handleSetCallback((Callback) message.obj);
            } else if (i == 3) {
                QSDetailItems qSDetailItems = QSDetailItems.this;
                if (message.arg1 == 0) {
                    z = false;
                }
                qSDetailItems.handleSetItemsVisible(z);
            }
        }
    }
}

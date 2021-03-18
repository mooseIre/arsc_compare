package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0017R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.controlcenter.phone.ControlPanelController;
import com.android.systemui.controlcenter.utils.Constants;
import miui.util.Pools;
import miuix.recyclerview.widget.RecyclerView;

public class MiuiQSDetailItems extends FrameLayout {
    private static final boolean DEBUG = Log.isLoggable("MiuiQSDetailItems", 3);
    private static final Pools.Pool<Item> ITEM_POOL = Pools.createSoftReferencePool(new Pools.Manager<Item>() {
        /* class com.android.systemui.qs.MiuiQSDetailItems.AnonymousClass1 */
        private int count = 0;

        public Item createInstance() {
            if (Constants.DEBUG) {
                StringBuilder sb = new StringBuilder();
                sb.append("Item Pool createInstance ");
                int i = this.count;
                this.count = i + 1;
                sb.append(i);
                Log.i("MiuiQSDetailItems", sb.toString());
            }
            return new Item();
        }
    }, 50);
    protected Adapter mAdapter = new Adapter();
    protected Callback mCallback;
    private final Context mContext;
    private ControlPanelController mControlPanelController;
    private View mEmpty;
    private ImageView mEmptyIcon;
    protected Runnable mEmptyStateRunnable = new Runnable() {
        /* class com.android.systemui.qs.MiuiQSDetailItems.AnonymousClass2 */

        public void run() {
            MiuiQSDetailItems.this.mEmptyIcon.setImageResource(MiuiQSDetailItems.this.mIconId);
            MiuiQSDetailItems.this.mEmptyText.setText(MiuiQSDetailItems.this.mTextId);
        }
    };
    private TextView mEmptyText;
    private final H mHandler = new H();
    private int mIconId;
    protected boolean mIsDetailShowing = false;
    protected boolean mIsExpanding = false;
    private boolean mItemClicked;
    private RecyclerView mItemList;
    protected Item[] mItems;
    private boolean mItemsVisible = true;
    private final int mQsDetailIconOverlaySize;
    private Item[] mScrapItems;
    private String mSuffix;
    private String mTag;
    private int mTextId;

    public interface Callback {
        void onDetailItemClick(Item item);

        default void onDetailItemDisconnect(Item item) {
        }
    }

    public MiuiQSDetailItems(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mContext = context;
        this.mTag = "MiuiQSDetailItems";
        Resources resources = getResources();
        this.mControlPanelController = (ControlPanelController) Dependency.get(ControlPanelController.class);
        this.mQsDetailIconOverlaySize = (int) resources.getDimension(C0012R$dimen.qs_detail_icon_overlay_size);
    }

    public static MiuiQSDetailItems convertOrInflate(Context context, View view, ViewGroup viewGroup) {
        if (view instanceof MiuiQSDetailItems) {
            return (MiuiQSDetailItems) view;
        }
        return (MiuiQSDetailItems) LayoutInflater.from(context).inflate(C0017R$layout.miui_qs_detail_items, viewGroup, false);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        RecyclerView recyclerView = (RecyclerView) findViewById(16908298);
        this.mItemList = recyclerView;
        recyclerView.setVisibility(8);
        this.mItemList.setLayoutManager(new LinearLayoutManager(getContext()));
        this.mItemList.setAdapter(this.mAdapter);
        View findViewById = findViewById(16908292);
        this.mEmpty = findViewById;
        findViewById.setVisibility(8);
        this.mEmptyText = (TextView) this.mEmpty.findViewById(16908310);
        this.mEmptyIcon = (ImageView) this.mEmpty.findViewById(16908294);
    }

    public void setTagSuffix(String str) {
        this.mTag = "MiuiQSDetailItems." + str;
        this.mSuffix = str;
        initItemsListPosition();
    }

    public String getSuffix() {
        return this.mSuffix;
    }

    public void setItemClicked(boolean z) {
        this.mItemClicked = z;
    }

    public boolean isItemClicked() {
        return this.mItemClicked;
    }

    public void setEmptyState(int i, int i2) {
        this.mIconId = i;
        this.mTextId = i2;
        this.mEmptyIcon.removeCallbacks(this.mEmptyStateRunnable);
        this.mEmptyIcon.post(this.mEmptyStateRunnable);
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

    public void setCallback(Callback callback) {
        this.mHandler.removeMessages(2);
        this.mHandler.obtainMessage(2, callback).sendToTarget();
    }

    public void setItems(Item[] itemArr) {
        Item[] itemArr2 = this.mScrapItems;
        if (!(itemArr2 == itemArr || itemArr2 == null)) {
            synchronized (ITEM_POOL) {
                for (Item item : this.mScrapItems) {
                    ITEM_POOL.release(item);
                }
            }
        }
        this.mScrapItems = itemArr;
        this.mHandler.removeMessages(1);
        this.mHandler.obtainMessage(1, itemArr).sendToTarget();
    }

    public void setItemsVisible(boolean z) {
        this.mHandler.removeMessages(3);
        this.mHandler.obtainMessage(3, z ? 1 : 0, 0).sendToTarget();
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
        RecyclerView recyclerView = this.mItemList;
        if (length == 0) {
            i = 8;
        }
        recyclerView.setVisibility(i);
        this.mItems = itemArr;
        if ((this.mIsExpanding || !this.mIsDetailShowing) && this.mControlPanelController.isUseControlCenter()) {
            Log.d("MiuiQSDetailItems", "ignore refresh items:" + this.mIsExpanding + this.mIsDetailShowing);
            return;
        }
        Log.d("MiuiQSDetailItems", "refresh detail items");
        this.mAdapter.notifyDataSetChanged();
    }

    public void notifyData() {
        this.mAdapter.notifyDataSetChanged();
    }

    public void setDetailShowing(boolean z) {
        this.mIsDetailShowing = z;
    }

    public void suppressLayout(boolean z) {
        super.suppressLayout(z);
        this.mIsExpanding = z;
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

    public void initItemsListPosition() {
        RecyclerView recyclerView = this.mItemList;
        if (recyclerView != null) {
            recyclerView.scrollToPosition(0);
        }
    }

    public Item acquireItem() {
        Item item = (Item) ITEM_POOL.acquire();
        item.reset();
        return item;
    }

    /* access modifiers changed from: protected */
    public class Adapter extends RecyclerView.Adapter<ItemHolder> {
        protected Adapter() {
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public ItemHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            int i2;
            if (i == 2) {
                return new LineItemHolder(LayoutInflater.from(MiuiQSDetailItems.this.mContext).inflate(C0017R$layout.qs_detail_line_item, viewGroup, false));
            }
            LayoutInflater from = LayoutInflater.from(MiuiQSDetailItems.this.mContext);
            if (MiuiQSDetailItems.this.mControlPanelController.isUseControlCenter()) {
                i2 = C0017R$layout.qs_control_detail_item;
            } else {
                i2 = C0017R$layout.miui_qs_detail_item;
            }
            return new CompleteItemHolder(from.inflate(i2, viewGroup, false));
        }

        public void onBindViewHolder(ItemHolder itemHolder, int i) {
            MiuiQSDetailItems miuiQSDetailItems = MiuiQSDetailItems.this;
            Item[] itemArr = miuiQSDetailItems.mItems;
            if (itemArr[i].type == 1) {
                final Item item = itemArr[i];
                CompleteItemHolder completeItemHolder = (CompleteItemHolder) itemHolder;
                completeItemHolder.itemView.setVisibility(miuiQSDetailItems.mItemsVisible ? 0 : 4);
                completeItemHolder.icon.setImageResource(item.icon);
                completeItemHolder.icon.getOverlay().clear();
                Drawable drawable = item.overlay;
                if (drawable != null) {
                    drawable.setBounds(0, 0, MiuiQSDetailItems.this.mQsDetailIconOverlaySize, MiuiQSDetailItems.this.mQsDetailIconOverlaySize);
                    completeItemHolder.icon.getOverlay().add(item.overlay);
                }
                completeItemHolder.itemView.setActivated(item.activated);
                completeItemHolder.itemView.setSelected(item.selected);
                completeItemHolder.title.setText(item.line1);
                boolean z = !TextUtils.isEmpty(item.line2);
                completeItemHolder.title.setMaxLines(z ? 1 : 2);
                completeItemHolder.summary.setVisibility(z ? 0 : 8);
                completeItemHolder.summary.setText(z ? item.line2 : null);
                if (item.activated) {
                    completeItemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                        /* class com.android.systemui.qs.MiuiQSDetailItems.Adapter.AnonymousClass1 */

                        public void onClick(View view) {
                            Callback callback = MiuiQSDetailItems.this.mCallback;
                            if (callback != null) {
                                callback.onDetailItemClick(item);
                            }
                        }
                    });
                } else {
                    completeItemHolder.itemView.setOnClickListener(null);
                }
                if (item.canDisconnect) {
                    completeItemHolder.button.setImageResource(C0013R$drawable.ic_qs_cancel);
                    completeItemHolder.button.setVisibility(0);
                    completeItemHolder.button.setClickable(true);
                    completeItemHolder.button.setOnClickListener(new View.OnClickListener() {
                        /* class com.android.systemui.qs.MiuiQSDetailItems.Adapter.AnonymousClass2 */

                        public void onClick(View view) {
                            Callback callback = MiuiQSDetailItems.this.mCallback;
                            if (callback != null) {
                                callback.onDetailItemDisconnect(item);
                            }
                        }
                    });
                } else if (item.icon2 != -1) {
                    completeItemHolder.button.setVisibility(0);
                    completeItemHolder.button.setImageResource(item.icon2);
                    completeItemHolder.button.setClickable(false);
                } else {
                    completeItemHolder.button.setVisibility(8);
                }
            }
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            Item[] itemArr = MiuiQSDetailItems.this.mItems;
            if (itemArr != null) {
                return Math.min(itemArr.length, 20);
            }
            return 0;
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemViewType(int i) {
            if (i < 0) {
                return 1;
            }
            Item[] itemArr = MiuiQSDetailItems.this.mItems;
            if (i >= itemArr.length) {
                return 1;
            }
            return itemArr[i].type;
        }
    }

    protected static abstract class ItemHolder extends RecyclerView.ViewHolder {
        public ItemHolder(View view) {
            super(view);
        }
    }

    /* access modifiers changed from: protected */
    public static class CompleteItemHolder extends ItemHolder {
        public ImageView button;
        public ImageView icon;
        public TextView summary;
        public TextView title;

        public CompleteItemHolder(View view) {
            super(view);
            this.icon = (ImageView) view.findViewById(16908294);
            this.title = (TextView) view.findViewById(16908310);
            this.summary = (TextView) view.findViewById(16908304);
            this.button = (ImageView) view.findViewById(16908296);
        }
    }

    /* access modifiers changed from: protected */
    public static class LineItemHolder extends ItemHolder {
        public LineItemHolder(View view) {
            super(view);
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
                MiuiQSDetailItems.this.handleSetItems((Item[]) message.obj);
            } else if (i == 2) {
                MiuiQSDetailItems.this.handleSetCallback((Callback) message.obj);
            } else if (i == 3) {
                MiuiQSDetailItems miuiQSDetailItems = MiuiQSDetailItems.this;
                if (message.arg1 == 0) {
                    z = false;
                }
                miuiQSDetailItems.handleSetItemsVisible(z);
            }
        }
    }

    public static class Item {
        public boolean activated = true;
        public boolean canDisconnect;
        public Drawable drawable;
        public int icon;
        public int icon2 = -1;
        public boolean initailed;
        public CharSequence line1;
        public CharSequence line2;
        public Drawable overlay;
        public boolean selected;
        public Object tag;
        public int type = 1;
        public CharSequence unit;

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private Item reset() {
            this.icon2 = -1;
            this.icon = -1;
            this.overlay = null;
            this.line1 = null;
            this.line2 = null;
            this.tag = null;
            this.selected = false;
            this.canDisconnect = false;
            this.activated = true;
            this.type = 1;
            return this;
        }
    }
}

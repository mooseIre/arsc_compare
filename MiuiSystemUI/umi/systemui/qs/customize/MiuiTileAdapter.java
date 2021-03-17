package com.android.systemui.qs.customize;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.logging.UiEventLogger;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0016R$integer;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.plugins.qs.QSTile;
import com.android.systemui.qs.QSEditEvent;
import com.android.systemui.qs.QSTileHost;
import com.android.systemui.qs.customize.MiuiTileAdapter;
import com.android.systemui.qs.customize.TileQueryHelper;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.qs.tileimpl.MiuiQSIconViewImpl;
import com.miui.systemui.statusbar.phone.MiuiSystemUIDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import miui.app.AlertDialog;

public class MiuiTileAdapter extends RecyclerView.Adapter<Holder> implements TileQueryHelper.TileStateListener {
    private int mAccessibilityAction = 0;
    private int mAccessibilityFromIndex;
    private CharSequence mAccessibilityFromLabel;
    private final AccessibilityManager mAccessibilityManager;
    private List<TileQueryHelper.TileInfo> mAllTiles;
    private final ItemTouchHelper.Callback mCallbacks = new ItemTouchHelper.Callback() {
        /* class com.android.systemui.qs.customize.MiuiTileAdapter.AnonymousClass3 */

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int i) {
            super.onSelectedChanged(viewHolder, i);
            if (i != 2) {
                viewHolder = null;
            }
            if (viewHolder != MiuiTileAdapter.this.mCurrentDrag) {
                if (MiuiTileAdapter.this.mCurrentDrag != null) {
                    int adapterPosition = MiuiTileAdapter.this.mCurrentDrag.getAdapterPosition();
                    if (adapterPosition != -1) {
                        MiuiTileAdapter.this.mCurrentDrag.mTileView.setShowAppLabel(adapterPosition > MiuiTileAdapter.this.mEditIndex && !((TileQueryHelper.TileInfo) MiuiTileAdapter.this.mTiles.get(adapterPosition)).isSystem);
                        MiuiTileAdapter.this.mCurrentDrag.stopDrag();
                        MiuiTileAdapter.this.mCurrentDrag = null;
                    } else {
                        return;
                    }
                }
                if (viewHolder != null) {
                    MiuiTileAdapter.this.mCurrentDrag = (Holder) viewHolder;
                    MiuiTileAdapter.this.mCurrentDrag.startDrag();
                }
                MiuiTileAdapter.this.mHandler.post(new Runnable() {
                    /* class com.android.systemui.qs.customize.MiuiTileAdapter.AnonymousClass3.AnonymousClass1 */

                    public void run() {
                        MiuiTileAdapter miuiTileAdapter = MiuiTileAdapter.this;
                        miuiTileAdapter.notifyItemChanged(miuiTileAdapter.mEditIndex);
                    }
                });
            }
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            int adapterPosition = viewHolder2.getAdapterPosition();
            if (adapterPosition == -1) {
                return false;
            }
            if (MiuiTileAdapter.this.canRemoveTiles() || viewHolder.getAdapterPosition() >= MiuiTileAdapter.this.mEditIndex) {
                if (adapterPosition <= MiuiTileAdapter.this.mEditIndex) {
                    return true;
                }
                return false;
            } else if (adapterPosition < MiuiTileAdapter.this.mEditIndex) {
                return true;
            } else {
                return false;
            }
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int itemViewType = viewHolder.getItemViewType();
            if (itemViewType == 1 || itemViewType == 3) {
                return ItemTouchHelper.Callback.makeMovementFlags(0, 0);
            }
            return ItemTouchHelper.Callback.makeMovementFlags(15, 0);
        }

        @Override // androidx.recyclerview.widget.ItemTouchHelper.Callback
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            return MiuiTileAdapter.this.move((MiuiTileAdapter) viewHolder.getAdapterPosition(), viewHolder2.getAdapterPosition(), (int) viewHolder2.itemView);
        }
    };
    private final Context mContext;
    private Holder mCurrentDrag;
    private List<String> mCurrentSpecs;
    private final RecyclerView.ItemDecoration mDecoration;
    private int mEditIndex;
    private final Handler mHandler = new Handler();
    private QSTileHost mHost;
    private final ItemTouchHelper mItemTouchHelper;
    private Map<String, QSTile> mLiveTiles;
    private final int mMinNumTiles;
    private boolean mNeedsFocus;
    private List<TileQueryHelper.TileInfo> mOtherTiles;
    private RecyclerView mParent;
    private final GridLayoutManager.SpanSizeLookup mSizeLookup = new GridLayoutManager.SpanSizeLookup() {
        /* class com.android.systemui.qs.customize.MiuiTileAdapter.AnonymousClass2 */

        @Override // androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
        public int getSpanSize(int i) {
            int itemViewType = MiuiTileAdapter.this.getItemViewType(i);
            if (itemViewType == 1 || itemViewType == 3) {
                return MiuiTileAdapter.this.mSpanCount;
            }
            return 1;
        }
    };
    private int mSpanCount;
    private int mTileDividerIndex;
    private final List<TileQueryHelper.TileInfo> mTiles = new ArrayList();
    private final UiEventLogger mUiEventLogger;

    public MiuiTileAdapter(Context context, UiEventLogger uiEventLogger, RecyclerView recyclerView) {
        this.mContext = context;
        this.mUiEventLogger = uiEventLogger;
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mItemTouchHelper = new ItemTouchHelper(this.mCallbacks);
        this.mDecoration = new TileItemDecoration(context);
        this.mMinNumTiles = context.getResources().getInteger(C0016R$integer.quick_settings_min_num_tiles);
        this.mParent = recyclerView;
    }

    public void setHost(QSTileHost qSTileHost) {
        this.mHost = qSTileHost;
    }

    public ItemTouchHelper getItemTouchHelper() {
        return this.mItemTouchHelper;
    }

    public RecyclerView.ItemDecoration getItemDecoration() {
        return this.mDecoration;
    }

    public void saveSpecs(QSTileHost qSTileHost) {
        ArrayList arrayList = new ArrayList();
        clearAccessibilityState();
        int i = 0;
        while (i < this.mTiles.size() && this.mTiles.get(i) != null) {
            arrayList.add(this.mTiles.get(i).spec);
            i++;
        }
        arrayList.add("edit");
        qSTileHost.changeTiles(this.mCurrentSpecs, arrayList);
        this.mCurrentSpecs = arrayList;
    }

    private void clearAccessibilityState() {
        if (this.mAccessibilityAction == 1) {
            List<TileQueryHelper.TileInfo> list = this.mTiles;
            int i = this.mEditIndex - 1;
            this.mEditIndex = i;
            list.remove(i);
            this.mTileDividerIndex--;
            notifyDataSetChanged();
        }
        this.mAccessibilityAction = 0;
    }

    public void resetTileSpecs(QSTileHost qSTileHost, List<String> list) {
        qSTileHost.changeTiles(this.mCurrentSpecs, list);
        setTileSpecs(list);
    }

    public void setTileSpecs(List<String> list) {
        if (!list.equals(this.mCurrentSpecs)) {
            this.mCurrentSpecs = list;
            recalcSpecs();
        }
    }

    @Override // com.android.systemui.qs.customize.TileQueryHelper.TileStateListener
    public void onTilesChanged(List<TileQueryHelper.TileInfo> list) {
        this.mAllTiles = list;
        recalcSpecs();
    }

    @Override // com.android.systemui.qs.customize.TileQueryHelper.TileStateListener
    public void onTilesChanged(List<TileQueryHelper.TileInfo> list, Map<String, QSTile> map) {
        this.mLiveTiles = map;
        onTilesChanged(list);
    }

    @Override // com.android.systemui.qs.customize.TileQueryHelper.TileStateListener
    public void onTileChanged(TileQueryHelper.TileInfo tileInfo) {
        for (int i = 0; i < this.mTiles.size(); i++) {
            TileQueryHelper.TileInfo tileInfo2 = this.mTiles.get(i);
            if (tileInfo2 != null && TextUtils.equals(tileInfo.spec, tileInfo2.spec)) {
                handleUpdateStateForPosition(i, tileInfo.state);
                return;
            }
        }
    }

    private void handleUpdateStateForPosition(int i, QSTile.State state) {
        this.mTiles.get(i).state = state;
        Holder holder = (Holder) this.mParent.findViewHolderForAdapterPosition(i);
        if (holder != null) {
            holder.mTileView.getIcon().setAnimationEnabled(true);
            holder.mTileView.handleStateChanged(state);
            holder.mTileView.getIcon().setAnimationEnabled(false);
        }
    }

    private void recalcSpecs() {
        if (!(this.mCurrentSpecs == null || this.mAllTiles == null)) {
            this.mOtherTiles = new ArrayList(this.mAllTiles);
            this.mTiles.clear();
            int i = 0;
            for (int i2 = 0; i2 < this.mCurrentSpecs.size(); i2++) {
                TileQueryHelper.TileInfo andRemoveOther = getAndRemoveOther(this.mCurrentSpecs.get(i2));
                if (andRemoveOther != null) {
                    this.mTiles.add(andRemoveOther);
                }
            }
            this.mTiles.add(null);
            while (i < this.mOtherTiles.size()) {
                TileQueryHelper.TileInfo tileInfo = this.mOtherTiles.get(i);
                if (tileInfo.isSystem) {
                    this.mOtherTiles.remove(i);
                    this.mTiles.add(tileInfo);
                    i--;
                }
                i++;
            }
            this.mTileDividerIndex = this.mTiles.size();
            this.mTiles.add(null);
            this.mTiles.addAll(this.mOtherTiles);
            updateDividerLocations();
            notifyDataSetChanged();
        }
    }

    private TileQueryHelper.TileInfo getAndRemoveOther(String str) {
        for (int i = 0; i < this.mOtherTiles.size(); i++) {
            if (this.mOtherTiles.get(i).spec.equals(str)) {
                return this.mOtherTiles.remove(i);
            }
        }
        return null;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemViewType(int i) {
        if (this.mAccessibilityAction == 1 && i == this.mEditIndex - 1) {
            return 2;
        }
        if (i == this.mTileDividerIndex) {
            return 3;
        }
        if (this.mTiles.get(i) == null) {
            return 1;
        }
        return 0;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater from = LayoutInflater.from(context);
        if (i == 3) {
            return new Holder(this, from.inflate(C0017R$layout.qs_customize_tile_divider, viewGroup, false));
        }
        if (i == 1) {
            return new Holder(this, from.inflate(C0017R$layout.qs_customize_tile_divider, viewGroup, false));
        }
        FrameLayout frameLayout = (FrameLayout) from.inflate(C0017R$layout.qs_customize_tile_frame, viewGroup, false);
        frameLayout.addView(new MiuiCustomizeTileView(context, new MiuiQSIconViewImpl(context)));
        return new Holder(this, frameLayout);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mTiles.size();
    }

    public boolean onFailedToRecycleView(Holder holder) {
        holder.clearDrag();
        return true;
    }

    public void onBindViewHolder(Holder holder, int i) {
        int i2 = 4;
        boolean z = false;
        if (holder.getItemViewType() == 3) {
            View view = holder.itemView;
            if (this.mTileDividerIndex < this.mTiles.size() - 1) {
                i2 = 0;
            }
            view.setVisibility(i2);
        } else if (holder.getItemViewType() != 1) {
            if (holder.getItemViewType() == 2) {
                holder.mTileView.setClickable(true);
                holder.mTileView.setFocusable(true);
                holder.mTileView.setFocusableInTouchMode(true);
                holder.mTileView.setVisibility(0);
                holder.mTileView.setImportantForAccessibility(1);
                holder.mTileView.setContentDescription(this.mContext.getString(C0021R$string.accessibility_qs_edit_tile_add, this.mAccessibilityFromLabel, Integer.valueOf(i)));
                holder.mTileView.setOnClickListener(new View.OnClickListener(holder) {
                    /* class com.android.systemui.qs.customize.$$Lambda$MiuiTileAdapter$T8sU63Oe85g4tAMwUk91lvHL7P8 */
                    public final /* synthetic */ MiuiTileAdapter.Holder f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void onClick(View view) {
                        MiuiTileAdapter.this.lambda$onBindViewHolder$0$MiuiTileAdapter(this.f$1, view);
                    }
                });
                focusOnHolder(holder);
                return;
            }
            TileQueryHelper.TileInfo tileInfo = this.mTiles.get(i);
            if (tileInfo != null) {
                holder.mTileView.getIcon().setIsCustomTile(!tileInfo.isSystem);
                if (i > this.mEditIndex) {
                    QSTile.State state = tileInfo.state;
                    state.contentDescription = this.mContext.getString(C0021R$string.accessibility_qs_edit_add_tile_label, state.label);
                } else {
                    int i3 = this.mAccessibilityAction;
                    if (i3 == 1) {
                        tileInfo.state.contentDescription = this.mContext.getString(C0021R$string.accessibility_qs_edit_tile_add, this.mAccessibilityFromLabel, Integer.valueOf(i));
                    } else if (i3 == 2) {
                        tileInfo.state.contentDescription = this.mContext.getString(C0021R$string.accessibility_qs_edit_tile_move, this.mAccessibilityFromLabel, Integer.valueOf(i));
                    } else {
                        tileInfo.state.contentDescription = this.mContext.getString(C0021R$string.accessibility_qs_edit_tile_label, Integer.valueOf(i), tileInfo.state.label);
                    }
                }
                holder.mTileView.handleStateChanged(tileInfo.state);
                holder.mTileView.setShowAppLabel(i > this.mEditIndex && !tileInfo.isSystem);
                bindOnClickListeners(tileInfo, holder);
                if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
                    if (this.mAccessibilityAction == 0 || i < this.mEditIndex) {
                        z = true;
                    }
                    holder.mTileView.setClickable(z);
                    holder.mTileView.setFocusable(z);
                    MiuiCustomizeTileView miuiCustomizeTileView = holder.mTileView;
                    if (z) {
                        i2 = 1;
                    }
                    miuiCustomizeTileView.setImportantForAccessibility(i2);
                    holder.mTileView.setFocusableInTouchMode(z);
                    if (z) {
                        holder.mTileView.setOnClickListener(new View.OnClickListener(holder) {
                            /* class com.android.systemui.qs.customize.$$Lambda$MiuiTileAdapter$6fiDXgXK0TlgMtGpjaf6Ox57E8U */
                            public final /* synthetic */ MiuiTileAdapter.Holder f$1;

                            {
                                this.f$1 = r2;
                            }

                            public final void onClick(View view) {
                                MiuiTileAdapter.this.lambda$onBindViewHolder$1$MiuiTileAdapter(this.f$1, view);
                            }
                        });
                        if (i == this.mAccessibilityFromIndex) {
                            focusOnHolder(holder);
                        }
                    }
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onBindViewHolder$0 */
    public /* synthetic */ void lambda$onBindViewHolder$0$MiuiTileAdapter(Holder holder, View view) {
        selectPosition(holder.getAdapterPosition(), view);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onBindViewHolder$1 */
    public /* synthetic */ void lambda$onBindViewHolder$1$MiuiTileAdapter(Holder holder, View view) {
        int adapterPosition = holder.getAdapterPosition();
        if (adapterPosition != -1) {
            if (this.mAccessibilityAction != 0) {
                selectPosition(adapterPosition, view);
            } else if (adapterPosition < this.mEditIndex && canRemoveTiles()) {
                showAccessibilityDialog(adapterPosition, view);
            } else if (adapterPosition >= this.mEditIndex || canRemoveTiles()) {
                startAccessibleAdd(adapterPosition);
            } else {
                startAccessibleMove(adapterPosition);
            }
        }
    }

    private void bindOnClickListeners(TileQueryHelper.TileInfo tileInfo, Holder holder) {
        holder.mTileView.init(new View.OnClickListener(tileInfo) {
            /* class com.android.systemui.qs.customize.$$Lambda$MiuiTileAdapter$XxOMA_09fP7gLjO1IQNti2SikCg */
            public final /* synthetic */ TileQueryHelper.TileInfo f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(View view) {
                MiuiTileAdapter.this.lambda$bindOnClickListeners$2$MiuiTileAdapter(this.f$1, view);
            }
        }, null, null);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindOnClickListeners$2 */
    public /* synthetic */ void lambda$bindOnClickListeners$2$MiuiTileAdapter(TileQueryHelper.TileInfo tileInfo, View view) {
        QSTile qSTile;
        Map<String, QSTile> map = this.mLiveTiles;
        if (map == null || !map.containsKey(tileInfo.spec)) {
            qSTile = this.mHost.getTile(tileInfo.spec);
        } else {
            qSTile = this.mLiveTiles.get(tileInfo.spec);
        }
        if (qSTile != null) {
            qSTile.click(true);
        } else if (isCustomTile(tileInfo)) {
            Toast.makeText(view.getContext(), C0021R$string.quick_settings_toast_drag_to_enable_custom_tile, 0).show();
        }
    }

    private void focusOnHolder(final Holder holder) {
        if (this.mNeedsFocus) {
            holder.mTileView.requestLayout();
            holder.mTileView.addOnLayoutChangeListener(new View.OnLayoutChangeListener(this) {
                /* class com.android.systemui.qs.customize.MiuiTileAdapter.AnonymousClass1 */

                public void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                    holder.mTileView.removeOnLayoutChangeListener(this);
                    holder.mTileView.requestFocus();
                }
            });
            this.mNeedsFocus = false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean canRemoveTiles() {
        return this.mCurrentSpecs.size() > this.mMinNumTiles;
    }

    private void selectPosition(int i, View view) {
        if (this.mAccessibilityAction == 1) {
            List<TileQueryHelper.TileInfo> list = this.mTiles;
            int i2 = this.mEditIndex;
            this.mEditIndex = i2 - 1;
            list.remove(i2);
            notifyItemRemoved(this.mEditIndex);
        }
        this.mAccessibilityAction = 0;
        move(this.mAccessibilityFromIndex, i, view);
        notifyDataSetChanged();
    }

    private void showAccessibilityDialog(int i, View view) {
        TileQueryHelper.TileInfo tileInfo = this.mTiles.get(i);
        AlertDialog create = new AlertDialog.Builder(this.mContext, 8).setItems(new CharSequence[]{this.mContext.getString(C0021R$string.accessibility_qs_edit_move_tile, tileInfo.state.label), this.mContext.getString(C0021R$string.accessibility_qs_edit_remove_tile, tileInfo.state.label)}, new DialogInterface.OnClickListener(i, tileInfo, view) {
            /* class com.android.systemui.qs.customize.$$Lambda$MiuiTileAdapter$uJrbW1k8KwvErcje5El9R0opaUs */
            public final /* synthetic */ int f$1;
            public final /* synthetic */ TileQueryHelper.TileInfo f$2;
            public final /* synthetic */ View f$3;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
            }

            public final void onClick(DialogInterface dialogInterface, int i) {
                MiuiTileAdapter.this.lambda$showAccessibilityDialog$3$MiuiTileAdapter(this.f$1, this.f$2, this.f$3, dialogInterface, i);
            }
        }).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).create();
        MiuiSystemUIDialog.setShowForAllUsers(create, true);
        MiuiSystemUIDialog.applyFlags(create);
        create.show();
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$showAccessibilityDialog$3 */
    public /* synthetic */ void lambda$showAccessibilityDialog$3$MiuiTileAdapter(int i, TileQueryHelper.TileInfo tileInfo, View view, DialogInterface dialogInterface, int i2) {
        if (i2 == 0) {
            startAccessibleMove(i);
            return;
        }
        move(i, tileInfo.isSystem ? this.mEditIndex : this.mTileDividerIndex, view);
        notifyItemChanged(this.mTileDividerIndex);
        notifyDataSetChanged();
    }

    private void startAccessibleAdd(int i) {
        this.mAccessibilityFromIndex = i;
        this.mAccessibilityFromLabel = this.mTiles.get(i).state.label;
        this.mAccessibilityAction = 1;
        List<TileQueryHelper.TileInfo> list = this.mTiles;
        int i2 = this.mEditIndex;
        this.mEditIndex = i2 + 1;
        list.add(i2, null);
        this.mTileDividerIndex++;
        this.mNeedsFocus = true;
        notifyDataSetChanged();
    }

    private void startAccessibleMove(int i) {
        this.mAccessibilityFromIndex = i;
        this.mAccessibilityFromLabel = this.mTiles.get(i).state.label;
        this.mAccessibilityAction = 2;
        this.mNeedsFocus = true;
        notifyDataSetChanged();
    }

    public GridLayoutManager.SpanSizeLookup getSizeLookup() {
        return this.mSizeLookup;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean move(int i, int i2, View view) {
        if (i2 == i) {
            return true;
        }
        CharSequence charSequence = this.mTiles.get(i).state.label;
        move(i, i2, this.mTiles);
        updateDividerLocations();
        int i3 = this.mEditIndex;
        if (i2 >= i3) {
            this.mUiEventLogger.log(QSEditEvent.QS_EDIT_REMOVE, 0, strip(this.mTiles.get(i2)));
        } else if (i >= i3) {
            this.mUiEventLogger.log(QSEditEvent.QS_EDIT_ADD, 0, strip(this.mTiles.get(i2)));
        } else {
            this.mUiEventLogger.log(QSEditEvent.QS_EDIT_MOVE, 0, strip(this.mTiles.get(i2)));
        }
        saveSpecs(this.mHost);
        return true;
    }

    private void updateDividerLocations() {
        this.mEditIndex = -1;
        this.mTileDividerIndex = this.mTiles.size();
        for (int i = 0; i < this.mTiles.size(); i++) {
            if (this.mTiles.get(i) == null) {
                if (this.mEditIndex == -1) {
                    this.mEditIndex = i;
                } else {
                    this.mTileDividerIndex = i;
                }
            }
        }
        int size = this.mTiles.size() - 1;
        int i2 = this.mTileDividerIndex;
        if (size == i2) {
            notifyItemChanged(i2);
        }
    }

    private static String strip(TileQueryHelper.TileInfo tileInfo) {
        String str = tileInfo.spec;
        return isCustomTile(tileInfo) ? CustomTile.getComponentFromSpec(str).getPackageName() : str;
    }

    private static boolean isCustomTile(TileQueryHelper.TileInfo tileInfo) {
        return tileInfo.spec.startsWith("custom(");
    }

    private <T> void move(int i, int i2, List<T> list) {
        list.add(i2, list.remove(i));
        notifyItemMoved(i, i2);
    }

    public class Holder extends RecyclerView.ViewHolder {
        private MiuiCustomizeTileView mTileView;

        public Holder(MiuiTileAdapter miuiTileAdapter, View view) {
            super(view);
            if (view instanceof FrameLayout) {
                MiuiCustomizeTileView miuiCustomizeTileView = (MiuiCustomizeTileView) ((FrameLayout) view).getChildAt(0);
                this.mTileView = miuiCustomizeTileView;
                miuiCustomizeTileView.setBackground(null);
                this.mTileView.getIcon().disableAnimation();
            }
        }

        public void clearDrag() {
            this.itemView.clearAnimation();
            this.mTileView.findViewById(C0015R$id.tile_label).clearAnimation();
            this.mTileView.findViewById(C0015R$id.tile_label).setAlpha(1.0f);
            this.mTileView.getAppLabel().clearAnimation();
            this.mTileView.getAppLabel().setAlpha(0.6f);
        }

        public void startDrag() {
            this.itemView.animate().setDuration(100).scaleX(1.2f).scaleY(1.2f);
            this.mTileView.findViewById(C0015R$id.tile_label).animate().setDuration(100).alpha(0.0f);
            this.mTileView.getAppLabel().animate().setDuration(100).alpha(0.0f);
        }

        public void stopDrag() {
            this.itemView.animate().setDuration(100).scaleX(1.0f).scaleY(1.0f);
            this.mTileView.findViewById(C0015R$id.tile_label).animate().setDuration(100).alpha(1.0f);
            this.mTileView.getAppLabel().animate().setDuration(100).alpha(0.6f);
        }
    }

    private class TileItemDecoration extends RecyclerView.ItemDecoration {
        private final Drawable mDrawable;

        private TileItemDecoration(Context context) {
            this.mDrawable = context.getDrawable(C0013R$drawable.qs_customize_tile_decoration);
        }

        @Override // androidx.recyclerview.widget.RecyclerView.ItemDecoration
        public void onDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
            super.onDraw(canvas, recyclerView, state);
            int findEditViewIndex = findEditViewIndex(recyclerView);
            if (findEditViewIndex >= 0) {
                drawBackgroundAfter(recyclerView, findEditViewIndex, canvas);
            }
        }

        private int findEditViewIndex(RecyclerView recyclerView) {
            if ((recyclerView.getLayoutManager() instanceof LinearLayoutManager) && ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition() > MiuiTileAdapter.this.mEditIndex) {
                return 0;
            }
            int childCount = recyclerView.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (recyclerView.getChildViewHolder(recyclerView.getChildAt(i)).getAdapterPosition() == MiuiTileAdapter.this.mEditIndex) {
                    return i;
                }
            }
            return -1;
        }

        private void drawBackgroundAfter(RecyclerView recyclerView, int i, Canvas canvas) {
            View childAt = recyclerView.getChildAt(i);
            int width = recyclerView.getWidth();
            int bottom = recyclerView.getBottom();
            this.mDrawable.setBounds(0, childAt.getTop() + ((ViewGroup.MarginLayoutParams) ((RecyclerView.LayoutParams) childAt.getLayoutParams())).topMargin + Math.round(ViewCompat.getTranslationY(childAt)), width, bottom);
            this.mDrawable.draw(canvas);
        }
    }

    public void setSpanCount(int i) {
        this.mSpanCount = i;
    }
}

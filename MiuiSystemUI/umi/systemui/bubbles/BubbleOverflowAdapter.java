package com.android.systemui.bubbles;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.android.internal.util.ContrastColorUtil;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import com.android.systemui.bubbles.BadgedImageView;
import java.util.List;
import java.util.function.Consumer;

/* access modifiers changed from: package-private */
/* compiled from: BubbleOverflowActivity */
public class BubbleOverflowAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<Bubble> mBubbles;
    private Context mContext;
    private int mHeight;
    private Consumer<Bubble> mPromoteBubbleFromOverflow;
    private int mWidth;

    public BubbleOverflowAdapter(Context context, List<Bubble> list, Consumer<Bubble> consumer, int i, int i2) {
        this.mContext = context;
        this.mBubbles = list;
        this.mPromoteBubbleFromOverflow = consumer;
        this.mWidth = i;
        this.mHeight = i2;
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(viewGroup.getContext()).inflate(C0017R$layout.bubble_overflow_view, viewGroup, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.width = this.mWidth;
        layoutParams.height = this.mHeight;
        linearLayout.setLayoutParams(layoutParams);
        TypedArray obtainStyledAttributes = this.mContext.obtainStyledAttributes(new int[]{16844002, 16842806});
        int ensureTextContrast = ContrastColorUtil.ensureTextContrast(obtainStyledAttributes.getColor(1, -16777216), obtainStyledAttributes.getColor(0, -1), true);
        obtainStyledAttributes.recycle();
        ((TextView) linearLayout.findViewById(C0015R$id.bubble_view_name)).setTextColor(ensureTextContrast);
        return new ViewHolder(linearLayout);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        CharSequence charSequence;
        Bubble bubble = this.mBubbles.get(i);
        viewHolder.iconView.setRenderedBubble(bubble);
        viewHolder.iconView.removeDotSuppressionFlag(BadgedImageView.SuppressionFlag.FLYOUT_VISIBLE);
        viewHolder.iconView.setOnClickListener(new View.OnClickListener(bubble) {
            /* class com.android.systemui.bubbles.$$Lambda$BubbleOverflowAdapter$MgnimWNCDitXqbPJN2vzJpXXigU */
            public final /* synthetic */ Bubble f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(View view) {
                BubbleOverflowAdapter.this.lambda$onBindViewHolder$0$BubbleOverflowAdapter(this.f$1, view);
            }
        });
        String title = bubble.getTitle();
        if (title == null) {
            title = this.mContext.getResources().getString(C0021R$string.notification_bubble_title);
        }
        viewHolder.iconView.setContentDescription(this.mContext.getResources().getString(C0021R$string.bubble_content_description_single, title, bubble.getAppName()));
        viewHolder.iconView.setAccessibilityDelegate(new View.AccessibilityDelegate() {
            /* class com.android.systemui.bubbles.BubbleOverflowAdapter.AnonymousClass1 */

            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
                super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
                accessibilityNodeInfo.addAction(new AccessibilityNodeInfo.AccessibilityAction(16, BubbleOverflowAdapter.this.mContext.getResources().getString(C0021R$string.bubble_accessibility_action_add_back)));
            }
        });
        if (bubble.getShortcutInfo() != null) {
            charSequence = bubble.getShortcutInfo().getLabel();
        } else {
            charSequence = bubble.getAppName();
        }
        viewHolder.textView.setText(charSequence);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$onBindViewHolder$0 */
    public /* synthetic */ void lambda$onBindViewHolder$0$BubbleOverflowAdapter(Bubble bubble, View view) {
        this.mBubbles.remove(bubble);
        notifyDataSetChanged();
        this.mPromoteBubbleFromOverflow.accept(bubble);
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return this.mBubbles.size();
    }

    /* compiled from: BubbleOverflowActivity */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public BadgedImageView iconView;
        public TextView textView;

        public ViewHolder(LinearLayout linearLayout) {
            super(linearLayout);
            this.iconView = (BadgedImageView) linearLayout.findViewById(C0015R$id.bubble_view);
            this.textView = (TextView) linearLayout.findViewById(C0015R$id.bubble_view_name);
        }
    }
}

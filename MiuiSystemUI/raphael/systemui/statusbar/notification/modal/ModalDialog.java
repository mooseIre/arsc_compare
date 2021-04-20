package com.android.systemui.statusbar.notification.modal;

import android.content.Context;
import android.content.DialogInterface;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Space;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.android.systemui.C0011R$dimen;
import com.android.systemui.C0014R$id;
import com.android.systemui.C0016R$layout;
import com.android.systemui.C0021R$style;
import com.android.systemui.Dependency;
import miui.R;
import miuix.animation.Folme;
import miuix.animation.ITouchStyle;
import miuix.animation.base.AnimConfig;

public class ModalDialog implements DialogInterface {
    private View mButtonContainer = this.mView.findViewById(C0014R$id.modal_dialog_button_container);
    private Space mButtonSpace = ((Space) this.mView.findViewById(C0014R$id.modal_dialog_button_space));
    private Context mContext;
    private ImageView mIconView = ((ImageView) this.mView.findViewById(C0014R$id.modal_dialog_icon));
    private ListView mListView = ((ListView) this.mView.findViewById(C0014R$id.modal_dialog_list));
    private TextView mMessageTv = ((TextView) this.mView.findViewById(C0014R$id.modal_dialog_message));
    private TextView mNegativeButton = ((TextView) this.mView.findViewById(C0014R$id.modal_dialog_negative_button));
    private TextView mPositiveButton = ((TextView) this.mView.findViewById(C0014R$id.modal_dialog_positive_button));
    private TextView mTitleTv;
    private View mView;

    public void cancel() {
    }

    public ModalDialog(Context context) {
        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(context, C0021R$style.Theme_Dialog_Alert);
        this.mContext = contextThemeWrapper;
        View inflate = LayoutInflater.from(contextThemeWrapper).inflate(C0016R$layout.miui_notification_modal_dialog, (ViewGroup) null, false);
        this.mView = inflate;
        this.mTitleTv = (TextView) inflate.findViewById(C0014R$id.modal_dialog_title);
        ITouchStyle iTouchStyle = Folme.useAt(this.mPositiveButton).touch();
        iTouchStyle.setScale(1.0f, new ITouchStyle.TouchType[0]);
        iTouchStyle.handleTouchOf(this.mPositiveButton, new AnimConfig[0]);
        reset();
    }

    public ModalDialog reset() {
        this.mTitleTv.setVisibility(8);
        this.mMessageTv.setVisibility(8);
        this.mIconView.setVisibility(8);
        this.mListView.setVisibility(8);
        this.mPositiveButton.setVisibility(8);
        this.mNegativeButton.setVisibility(8);
        this.mButtonSpace.setVisibility(8);
        return this;
    }

    public View getView() {
        return this.mView;
    }

    public void show() {
        int i;
        if (this.mListView.getVisibility() == 0) {
            i = C0011R$dimen.modal_dialog_button_margin_top_list;
        } else {
            i = C0011R$dimen.modal_dialog_button_margin_top_msg;
        }
        ((ViewGroup.MarginLayoutParams) ((ConstraintLayout.LayoutParams) this.mButtonContainer.getLayoutParams())).topMargin = this.mContext.getResources().getDimensionPixelOffset(i);
        ((ModalController) Dependency.get(ModalController.class)).showModalDialog(this);
    }

    public void dismiss() {
        ((ModalController) Dependency.get(ModalController.class)).hideModalDialog();
    }

    public ModalDialog setIcon(int i) {
        this.mIconView.setImageResource(i);
        this.mIconView.setVisibility(0);
        return this;
    }

    public ModalDialog setTitle(int i) {
        setTitle(this.mIconView.getResources().getString(i));
        return this;
    }

    public ModalDialog setTitle(CharSequence charSequence) {
        this.mTitleTv.setText(charSequence);
        this.mTitleTv.setVisibility(0);
        return this;
    }

    public ModalDialog setMessage(CharSequence charSequence) {
        this.mMessageTv.setText(charSequence);
        this.mMessageTv.setVisibility(0);
        return this;
    }

    public ModalDialog setPositiveButton(int i, DialogInterface.OnClickListener onClickListener) {
        this.mPositiveButton.setText(i);
        this.mPositiveButton.setOnClickListener(new View.OnClickListener(onClickListener) {
            /* class com.android.systemui.statusbar.notification.modal.$$Lambda$ModalDialog$z2MZVfyAPIBXhiCN0uZ86_FCYfA */
            public final /* synthetic */ DialogInterface.OnClickListener f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(View view) {
                ModalDialog.this.lambda$setPositiveButton$0$ModalDialog(this.f$1, view);
            }
        });
        this.mPositiveButton.setVisibility(0);
        this.mButtonSpace.setVisibility(0);
        return this;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setPositiveButton$0 */
    public /* synthetic */ void lambda$setPositiveButton$0$ModalDialog(DialogInterface.OnClickListener onClickListener, View view) {
        onClickListener.onClick(this, 0);
    }

    public ModalDialog setNegativeButton(int i, DialogInterface.OnClickListener onClickListener) {
        this.mNegativeButton.setText(i);
        this.mNegativeButton.setOnClickListener(new View.OnClickListener(onClickListener) {
            /* class com.android.systemui.statusbar.notification.modal.$$Lambda$ModalDialog$OeyqM480bpvxlLcD32p9q4VTNvw */
            public final /* synthetic */ DialogInterface.OnClickListener f$1;

            {
                this.f$1 = r2;
            }

            public final void onClick(View view) {
                ModalDialog.this.lambda$setNegativeButton$1$ModalDialog(this.f$1, view);
            }
        });
        this.mNegativeButton.setVisibility(0);
        return this;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setNegativeButton$1 */
    public /* synthetic */ void lambda$setNegativeButton$1$ModalDialog(DialogInterface.OnClickListener onClickListener, View view) {
        onClickListener.onClick(this, 0);
    }

    public ModalDialog setSingleChoiceItems(CharSequence[] charSequenceArr, int i, DialogInterface.OnClickListener onClickListener) {
        this.mListView.setAdapter((ListAdapter) new CheckedItemAdapter(this.mListView.getContext(), R.layout.select_dialog_singlechoice, 16908308, charSequenceArr));
        this.mListView.setChoiceMode(1);
        this.mListView.setItemChecked(i, true);
        this.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(onClickListener) {
            /* class com.android.systemui.statusbar.notification.modal.$$Lambda$ModalDialog$reX4x2_BbkXLhpx0agaBG4w6vmk */
            public final /* synthetic */ DialogInterface.OnClickListener f$1;

            {
                this.f$1 = r2;
            }

            @Override // android.widget.AdapterView.OnItemClickListener
            public final void onItemClick(AdapterView adapterView, View view, int i, long j) {
                ModalDialog.this.lambda$setSingleChoiceItems$2$ModalDialog(this.f$1, adapterView, view, i, j);
            }
        });
        this.mListView.setVisibility(0);
        return this;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$setSingleChoiceItems$2 */
    public /* synthetic */ void lambda$setSingleChoiceItems$2$ModalDialog(DialogInterface.OnClickListener onClickListener, AdapterView adapterView, View view, int i, long j) {
        onClickListener.onClick(this, i);
    }

    private static class CheckedItemAdapter extends ArrayAdapter<CharSequence> {
        private int mItemPadding;

        public long getItemId(int i) {
            return (long) i;
        }

        public boolean hasStableIds() {
            return true;
        }

        public CheckedItemAdapter(Context context, int i, int i2, CharSequence[] charSequenceArr) {
            super(context, i, i2, charSequenceArr);
            this.mItemPadding = context.getResources().getDimensionPixelOffset(C0011R$dimen.modal_dialog_margin_left_right);
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            View view2 = super.getView(i, view, viewGroup);
            view2.setPaddingRelative(this.mItemPadding, view2.getPaddingTop(), view2.getPaddingEnd(), view2.getPaddingBottom());
            return view2;
        }
    }
}

package com.android.systemui.tuner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.AttributeSet;
import androidx.preference.ListPreference;
import androidx.preference.ListPreferenceDialogFragment;

public class CustomListPreference extends ListPreference {
    /* access modifiers changed from: protected */
    public boolean isAutoClosePreference() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void onDialogClosed(boolean z) {
    }

    /* access modifiers changed from: protected */
    public Dialog onDialogCreated(DialogFragment dialogFragment, Dialog dialog) {
        return dialog;
    }

    /* access modifiers changed from: protected */
    public void onDialogStateRestored(DialogFragment dialogFragment, Dialog dialog, Bundle bundle) {
    }

    /* access modifiers changed from: protected */
    public void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener onClickListener) {
    }

    public CustomListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public CustomListPreference(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
    }

    public static class CustomListPreferenceDialogFragment extends ListPreferenceDialogFragment {
        private int mClickedDialogEntryIndex;

        public static ListPreferenceDialogFragment newInstance(String str) {
            CustomListPreferenceDialogFragment customListPreferenceDialogFragment = new CustomListPreferenceDialogFragment();
            Bundle bundle = new Bundle(1);
            bundle.putString("key", str);
            customListPreferenceDialogFragment.setArguments(bundle);
            return customListPreferenceDialogFragment;
        }

        public CustomListPreference getCustomizablePreference() {
            return (CustomListPreference) getPreference();
        }

        /* access modifiers changed from: protected */
        public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
            super.onPrepareDialogBuilder(builder);
            this.mClickedDialogEntryIndex = getCustomizablePreference().findIndexOfValue(getCustomizablePreference().getValue());
            getCustomizablePreference().onPrepareDialogBuilder(builder, getOnItemClickListener());
            if (!getCustomizablePreference().isAutoClosePreference()) {
                builder.setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CustomListPreferenceDialogFragment.this.onItemConfirmed();
                    }
                });
            }
        }

        public Dialog onCreateDialog(Bundle bundle) {
            Dialog onCreateDialog = super.onCreateDialog(bundle);
            if (bundle != null) {
                this.mClickedDialogEntryIndex = bundle.getInt("settings.CustomListPrefDialog.KEY_CLICKED_ENTRY_INDEX", this.mClickedDialogEntryIndex);
            }
            return getCustomizablePreference().onDialogCreated(this, onCreateDialog);
        }

        public void onSaveInstanceState(Bundle bundle) {
            super.onSaveInstanceState(bundle);
            bundle.putInt("settings.CustomListPrefDialog.KEY_CLICKED_ENTRY_INDEX", this.mClickedDialogEntryIndex);
        }

        public void onActivityCreated(Bundle bundle) {
            super.onActivityCreated(bundle);
            getCustomizablePreference().onDialogStateRestored(this, getDialog(), bundle);
        }

        /* access modifiers changed from: protected */
        public DialogInterface.OnClickListener getOnItemClickListener() {
            return new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    CustomListPreferenceDialogFragment.this.setClickedDialogEntryIndex(i);
                    if (CustomListPreferenceDialogFragment.this.getCustomizablePreference().isAutoClosePreference()) {
                        CustomListPreferenceDialogFragment.this.onItemConfirmed();
                    }
                }
            };
        }

        /* access modifiers changed from: protected */
        public void setClickedDialogEntryIndex(int i) {
            this.mClickedDialogEntryIndex = i;
        }

        private String getValue() {
            CustomListPreference customizablePreference = getCustomizablePreference();
            if (this.mClickedDialogEntryIndex < 0 || customizablePreference.getEntryValues() == null) {
                return null;
            }
            return customizablePreference.getEntryValues()[this.mClickedDialogEntryIndex].toString();
        }

        /* access modifiers changed from: protected */
        public void onItemConfirmed() {
            onClick(getDialog(), -1);
            getDialog().dismiss();
        }

        public void onDialogClosed(boolean z) {
            getCustomizablePreference().onDialogClosed(z);
            CustomListPreference customizablePreference = getCustomizablePreference();
            String value = getValue();
            if (z && value != null && customizablePreference.callChangeListener(value)) {
                customizablePreference.setValue(value);
            }
        }
    }
}

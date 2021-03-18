package com.android.systemui.tuner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toolbar;
import androidx.preference.Preference;
import com.android.settingslib.Utils;
import com.android.systemui.C0015R$id;
import com.android.systemui.fragments.FragmentHostManager;
import java.util.Objects;

public class RadioListPreference extends CustomListPreference {
    private DialogInterface.OnClickListener mOnClickListener;
    private CharSequence mSummary;

    public RadioListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.tuner.CustomListPreference
    public void onPrepareDialogBuilder(AlertDialog.Builder builder, DialogInterface.OnClickListener onClickListener) {
        this.mOnClickListener = onClickListener;
    }

    @Override // androidx.preference.ListPreference, androidx.preference.Preference
    public void setSummary(CharSequence charSequence) {
        super.setSummary(charSequence);
        this.mSummary = charSequence;
    }

    @Override // androidx.preference.ListPreference, androidx.preference.Preference
    public CharSequence getSummary() {
        CharSequence charSequence = this.mSummary;
        if (charSequence == null || charSequence.toString().contains("%s")) {
            return super.getSummary();
        }
        return this.mSummary;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.tuner.CustomListPreference
    public Dialog onDialogCreated(DialogFragment dialogFragment, Dialog dialog) {
        Dialog dialog2 = new Dialog(getContext(), 16974371);
        Toolbar toolbar = (Toolbar) dialog2.findViewById(16908711);
        View view = new View(getContext());
        view.setId(C0015R$id.content);
        dialog2.setContentView(view);
        toolbar.setTitle(getTitle());
        toolbar.setNavigationIcon(Utils.getDrawable(dialog2.getContext(), 16843531));
        toolbar.setNavigationOnClickListener(new View.OnClickListener(dialog2) {
            /* class com.android.systemui.tuner.$$Lambda$RadioListPreference$4DEUOALD3KxT1NUXowELf5ZJ2M */
            public final /* synthetic */ Dialog f$0;

            {
                this.f$0 = r1;
            }

            public final void onClick(View view) {
                RadioListPreference.lambda$onDialogCreated$0(this.f$0, view);
            }
        });
        RadioFragment radioFragment = new RadioFragment();
        radioFragment.setPreference(this);
        FragmentHostManager.get(view).getFragmentManager().beginTransaction().add(16908290, radioFragment).commit();
        return dialog2;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.tuner.CustomListPreference
    public void onDialogStateRestored(DialogFragment dialogFragment, Dialog dialog, Bundle bundle) {
        super.onDialogStateRestored(dialogFragment, dialog, bundle);
        RadioFragment radioFragment = (RadioFragment) FragmentHostManager.get(dialog.findViewById(C0015R$id.content)).getFragmentManager().findFragmentById(C0015R$id.content);
        if (radioFragment != null) {
            radioFragment.setPreference(this);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.systemui.tuner.CustomListPreference
    public void onDialogClosed(boolean z) {
        super.onDialogClosed(z);
    }

    public static class RadioFragment extends TunerPreferenceFragment {
        private RadioListPreference mListPref;

        @Override // androidx.preference.PreferenceFragment
        public void onCreatePreferences(Bundle bundle, String str) {
            setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getPreferenceManager().getContext()));
            if (this.mListPref != null) {
                update();
            }
        }

        private void update() {
            Context context = getPreferenceManager().getContext();
            CharSequence[] entries = this.mListPref.getEntries();
            CharSequence[] entryValues = this.mListPref.getEntryValues();
            String value = this.mListPref.getValue();
            for (int i = 0; i < entries.length; i++) {
                CharSequence charSequence = entries[i];
                SelectablePreference selectablePreference = new SelectablePreference(context);
                getPreferenceScreen().addPreference(selectablePreference);
                selectablePreference.setTitle(charSequence);
                selectablePreference.setChecked(Objects.equals(value, entryValues[i]));
                selectablePreference.setKey(String.valueOf(i));
            }
        }

        @Override // androidx.preference.PreferenceManager.OnPreferenceTreeClickListener, androidx.preference.PreferenceFragment
        public boolean onPreferenceTreeClick(Preference preference) {
            this.mListPref.mOnClickListener.onClick(null, Integer.parseInt(preference.getKey()));
            return true;
        }

        public void setPreference(RadioListPreference radioListPreference) {
            this.mListPref = radioListPreference;
            if (getPreferenceManager() != null) {
                update();
            }
        }
    }
}

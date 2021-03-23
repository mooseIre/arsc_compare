package com.android.systemui.tuner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.TypedValue;
import android.widget.EditText;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0021R$string;
import com.android.systemui.C0023R$xml;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.phone.NavigationBarInflaterView;
import com.android.systemui.tuner.TunerService;
import java.util.ArrayList;

public class NavBarTuner extends TunerPreferenceFragment {
    private static final int[][] ICONS = {new int[]{C0013R$drawable.ic_qs_circle, C0021R$string.tuner_circle}, new int[]{C0013R$drawable.ic_add, C0021R$string.tuner_plus}, new int[]{C0013R$drawable.ic_remove, C0021R$string.tuner_minus}, new int[]{C0013R$drawable.ic_left, C0021R$string.tuner_left}, new int[]{C0013R$drawable.ic_right, C0021R$string.tuner_right}, new int[]{C0013R$drawable.ic_menu, C0021R$string.tuner_menu}};
    private Handler mHandler;
    private final ArrayList<TunerService.Tunable> mTunables = new ArrayList<>();

    @Override // androidx.preference.PreferenceFragment
    public void onCreate(Bundle bundle) {
        this.mHandler = new Handler();
        super.onCreate(bundle);
    }

    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
        getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override // androidx.preference.PreferenceFragment
    public void onCreatePreferences(Bundle bundle, String str) {
        addPreferencesFromResource(C0023R$xml.nav_bar_tuner);
        bindLayout((ListPreference) findPreference("layout"));
        bindButton("sysui_nav_bar_left", "space", "left");
        bindButton("sysui_nav_bar_right", "menu_ime", "right");
    }

    public void onDestroy() {
        super.onDestroy();
        this.mTunables.forEach($$Lambda$NavBarTuner$tsKQ8HfwaDSvc3iDCsgHsW954hc.INSTANCE);
    }

    private void addTunable(TunerService.Tunable tunable, String... strArr) {
        this.mTunables.add(tunable);
        ((TunerService) Dependency.get(TunerService.class)).addTunable(tunable, strArr);
    }

    private void bindLayout(ListPreference listPreference) {
        addTunable(new TunerService.Tunable(listPreference) {
            /* class com.android.systemui.tuner.$$Lambda$NavBarTuner$nx5Q7aHowvZ9Bevy96_zeYYIxAY */
            public final /* synthetic */ ListPreference f$1;

            {
                this.f$1 = r2;
            }

            @Override // com.android.systemui.tuner.TunerService.Tunable
            public final void onTuningChanged(String str, String str2) {
                NavBarTuner.this.lambda$bindLayout$2$NavBarTuner(this.f$1, str, str2);
            }
        }, "sysui_nav_bar");
        listPreference.setOnPreferenceChangeListener($$Lambda$NavBarTuner$xJajVHN9uODpq3muoNpXW6uxwc.INSTANCE);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindLayout$2 */
    public /* synthetic */ void lambda$bindLayout$2$NavBarTuner(ListPreference listPreference, String str, String str2) {
        this.mHandler.post(new Runnable(str2, listPreference) {
            /* class com.android.systemui.tuner.$$Lambda$NavBarTuner$RQUqCpCXtFwKbIxFJ1AuU4K69W4 */
            public final /* synthetic */ String f$0;
            public final /* synthetic */ ListPreference f$1;

            {
                this.f$0 = r1;
                this.f$1 = r2;
            }

            public final void run() {
                NavBarTuner.lambda$bindLayout$1(this.f$0, this.f$1);
            }
        });
    }

    static /* synthetic */ void lambda$bindLayout$1(String str, ListPreference listPreference) {
        if (str == null) {
            str = "default";
        }
        listPreference.setValue(str);
    }

    static /* synthetic */ boolean lambda$bindLayout$3(Preference preference, Object obj) {
        String str = (String) obj;
        if ("default".equals(str)) {
            str = null;
        }
        ((TunerService) Dependency.get(TunerService.class)).setValue("sysui_nav_bar", str);
        return true;
    }

    private void bindButton(String str, String str2, String str3) {
        ListPreference listPreference = (ListPreference) findPreference("type_" + str3);
        Preference findPreference = findPreference("keycode_" + str3);
        ListPreference listPreference2 = (ListPreference) findPreference("icon_" + str3);
        setupIcons(listPreference2);
        addTunable(new TunerService.Tunable(str2, listPreference, listPreference2, findPreference) {
            /* class com.android.systemui.tuner.$$Lambda$NavBarTuner$AtqwC3eDMLXM8PvQu0SrBbBcxZQ */
            public final /* synthetic */ String f$1;
            public final /* synthetic */ ListPreference f$2;
            public final /* synthetic */ ListPreference f$3;
            public final /* synthetic */ Preference f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            @Override // com.android.systemui.tuner.TunerService.Tunable
            public final void onTuningChanged(String str, String str2) {
                NavBarTuner.this.lambda$bindButton$5$NavBarTuner(this.f$1, this.f$2, this.f$3, this.f$4, str, str2);
            }
        }, str);
        $$Lambda$NavBarTuner$5vkJoJwaFUhdGZ7Fp4qtkLVqooo r11 = new Preference.OnPreferenceChangeListener(str, listPreference, findPreference, listPreference2) {
            /* class com.android.systemui.tuner.$$Lambda$NavBarTuner$5vkJoJwaFUhdGZ7Fp4qtkLVqooo */
            public final /* synthetic */ String f$1;
            public final /* synthetic */ ListPreference f$2;
            public final /* synthetic */ Preference f$3;
            public final /* synthetic */ ListPreference f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            @Override // androidx.preference.Preference.OnPreferenceChangeListener
            public final boolean onPreferenceChange(Preference preference, Object obj) {
                return NavBarTuner.this.lambda$bindButton$7$NavBarTuner(this.f$1, this.f$2, this.f$3, this.f$4, preference, obj);
            }
        };
        listPreference.setOnPreferenceChangeListener(r11);
        listPreference2.setOnPreferenceChangeListener(r11);
        findPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(findPreference, str, listPreference, listPreference2) {
            /* class com.android.systemui.tuner.$$Lambda$NavBarTuner$VEefG8gxDDp8OSjE4w47bWNl4eQ */
            public final /* synthetic */ Preference f$1;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ ListPreference f$3;
            public final /* synthetic */ ListPreference f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            @Override // androidx.preference.Preference.OnPreferenceClickListener
            public final boolean onPreferenceClick(Preference preference) {
                return NavBarTuner.this.lambda$bindButton$9$NavBarTuner(this.f$1, this.f$2, this.f$3, this.f$4, preference);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindButton$5 */
    public /* synthetic */ void lambda$bindButton$5$NavBarTuner(String str, ListPreference listPreference, ListPreference listPreference2, Preference preference, String str2, String str3) {
        this.mHandler.post(new Runnable(str3, str, listPreference, listPreference2, preference) {
            /* class com.android.systemui.tuner.$$Lambda$NavBarTuner$sQQgaEvmFdhni6jwm3oIAJf94Eo */
            public final /* synthetic */ String f$1;
            public final /* synthetic */ String f$2;
            public final /* synthetic */ ListPreference f$3;
            public final /* synthetic */ ListPreference f$4;
            public final /* synthetic */ Preference f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void run() {
                NavBarTuner.this.lambda$bindButton$4$NavBarTuner(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5);
            }
        });
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindButton$4 */
    public /* synthetic */ void lambda$bindButton$4$NavBarTuner(String str, String str2, ListPreference listPreference, ListPreference listPreference2, Preference preference) {
        if (str == null) {
            str = str2;
        }
        String extractButton = NavigationBarInflaterView.extractButton(str);
        if (extractButton.startsWith("key")) {
            listPreference.setValue("key");
            String extractImage = NavigationBarInflaterView.extractImage(extractButton);
            int extractKeycode = NavigationBarInflaterView.extractKeycode(extractButton);
            listPreference2.setValue(extractImage);
            updateSummary(listPreference2);
            preference.setSummary(extractKeycode + "");
            preference.setVisible(true);
            listPreference2.setVisible(true);
            return;
        }
        listPreference.setValue(extractButton);
        preference.setVisible(false);
        listPreference2.setVisible(false);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindButton$7 */
    public /* synthetic */ boolean lambda$bindButton$7$NavBarTuner(String str, ListPreference listPreference, Preference preference, ListPreference listPreference2, Preference preference2, Object obj) {
        this.mHandler.post(new Runnable(str, listPreference, preference, listPreference2) {
            /* class com.android.systemui.tuner.$$Lambda$NavBarTuner$Q4QuzL1NB7uGZ3GCFspFwSEMA8g */
            public final /* synthetic */ String f$1;
            public final /* synthetic */ ListPreference f$2;
            public final /* synthetic */ Preference f$3;
            public final /* synthetic */ ListPreference f$4;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
            }

            public final void run() {
                NavBarTuner.this.lambda$bindButton$6$NavBarTuner(this.f$1, this.f$2, this.f$3, this.f$4);
            }
        });
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindButton$6 */
    public /* synthetic */ void lambda$bindButton$6$NavBarTuner(String str, ListPreference listPreference, Preference preference, ListPreference listPreference2) {
        setValue(str, listPreference, preference, listPreference2);
        updateSummary(listPreference2);
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindButton$9 */
    public /* synthetic */ boolean lambda$bindButton$9$NavBarTuner(Preference preference, String str, ListPreference listPreference, ListPreference listPreference2, Preference preference2) {
        EditText editText = new EditText(getContext());
        new AlertDialog.Builder(getContext()).setTitle(preference2.getTitle()).setView(editText).setNegativeButton(17039360, (DialogInterface.OnClickListener) null).setPositiveButton(17039370, new DialogInterface.OnClickListener(editText, preference, str, listPreference, listPreference2) {
            /* class com.android.systemui.tuner.$$Lambda$NavBarTuner$oFwpdLrZA2BGC8nFWvjJ8NeCnQE */
            public final /* synthetic */ EditText f$1;
            public final /* synthetic */ Preference f$2;
            public final /* synthetic */ String f$3;
            public final /* synthetic */ ListPreference f$4;
            public final /* synthetic */ ListPreference f$5;

            {
                this.f$1 = r2;
                this.f$2 = r3;
                this.f$3 = r4;
                this.f$4 = r5;
                this.f$5 = r6;
            }

            public final void onClick(DialogInterface dialogInterface, int i) {
                NavBarTuner.this.lambda$bindButton$8$NavBarTuner(this.f$1, this.f$2, this.f$3, this.f$4, this.f$5, dialogInterface, i);
            }
        }).show();
        return true;
    }

    /* access modifiers changed from: private */
    /* renamed from: lambda$bindButton$8 */
    public /* synthetic */ void lambda$bindButton$8$NavBarTuner(EditText editText, Preference preference, String str, ListPreference listPreference, ListPreference listPreference2, DialogInterface dialogInterface, int i) {
        int i2;
        try {
            i2 = Integer.parseInt(editText.getText().toString());
        } catch (Exception unused) {
            i2 = 66;
        }
        preference.setSummary(i2 + "");
        setValue(str, listPreference, preference, listPreference2);
    }

    private void updateSummary(ListPreference listPreference) {
        int[][] iArr = ICONS;
        try {
            int applyDimension = (int) TypedValue.applyDimension(1, 14.0f, getContext().getResources().getDisplayMetrics());
            String str = listPreference.getValue().split("/")[0];
            int parseInt = Integer.parseInt(listPreference.getValue().split("/")[1]);
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            Drawable loadDrawable = Icon.createWithResource(str, parseInt).loadDrawable(getContext());
            loadDrawable.setTint(-16777216);
            loadDrawable.setBounds(0, 0, applyDimension, applyDimension);
            spannableStringBuilder.append("  ", new ImageSpan(loadDrawable, 1), 0);
            spannableStringBuilder.append((CharSequence) " ");
            for (int i = 0; i < iArr.length; i++) {
                if (iArr[i][0] == parseInt) {
                    spannableStringBuilder.append((CharSequence) getString(iArr[i][1]));
                }
            }
            listPreference.setSummary(spannableStringBuilder);
        } catch (Exception e) {
            Log.d("NavButton", "Problem with summary", e);
            listPreference.setSummary((CharSequence) null);
        }
    }

    private void setValue(String str, ListPreference listPreference, Preference preference, ListPreference listPreference2) {
        String value = listPreference.getValue();
        if ("key".equals(value)) {
            String value2 = listPreference2.getValue();
            int i = 66;
            try {
                i = Integer.parseInt(preference.getSummary().toString());
            } catch (Exception unused) {
            }
            value = value + "(" + i + ":" + value2 + ")";
        }
        ((TunerService) Dependency.get(TunerService.class)).setValue(str, value);
    }

    private void setupIcons(ListPreference listPreference) {
        int[][] iArr = ICONS;
        CharSequence[] charSequenceArr = new CharSequence[iArr.length];
        CharSequence[] charSequenceArr2 = new CharSequence[iArr.length];
        int applyDimension = (int) TypedValue.applyDimension(1, 14.0f, getContext().getResources().getDisplayMetrics());
        for (int i = 0; i < iArr.length; i++) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            Drawable loadDrawable = Icon.createWithResource(getContext().getPackageName(), iArr[i][0]).loadDrawable(getContext());
            loadDrawable.setTint(-16777216);
            loadDrawable.setBounds(0, 0, applyDimension, applyDimension);
            spannableStringBuilder.append("  ", new ImageSpan(loadDrawable, 1), 0);
            spannableStringBuilder.append((CharSequence) " ");
            spannableStringBuilder.append((CharSequence) getString(iArr[i][1]));
            charSequenceArr[i] = spannableStringBuilder;
            charSequenceArr2[i] = getContext().getPackageName() + "/" + iArr[i][0];
        }
        listPreference.setEntries(charSequenceArr);
        listPreference.setEntryValues(charSequenceArr2);
    }
}

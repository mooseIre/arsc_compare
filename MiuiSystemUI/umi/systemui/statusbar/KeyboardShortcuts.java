package com.android.systemui.statusbar;

import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyboardShortcutGroup;
import android.view.KeyboardShortcutInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManagerCompat;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.app.AssistUtils;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.plugins.R;
import com.android.systemui.recents.Recents;
import com.android.systemui.util.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class KeyboardShortcuts {
    private static final String TAG = "KeyboardShortcuts";
    private static KeyboardShortcuts sInstance;
    private static final Object sLock = new Object();
    private final Comparator<KeyboardShortcutInfo> mApplicationItemsComparator = new Comparator<KeyboardShortcutInfo>(this) {
        public int compare(KeyboardShortcutInfo keyboardShortcutInfo, KeyboardShortcutInfo keyboardShortcutInfo2) {
            boolean z = keyboardShortcutInfo.getLabel() == null || keyboardShortcutInfo.getLabel().toString().isEmpty();
            boolean z2 = keyboardShortcutInfo2.getLabel() == null || keyboardShortcutInfo2.getLabel().toString().isEmpty();
            if (z && z2) {
                return 0;
            }
            if (z) {
                return 1;
            }
            if (z2) {
                return -1;
            }
            return keyboardShortcutInfo.getLabel().toString().compareToIgnoreCase(keyboardShortcutInfo2.getLabel().toString());
        }
    };
    private KeyCharacterMap mBackupKeyCharacterMap;
    private final Context mContext;
    private final DialogInterface.OnClickListener mDialogCloseListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialogInterface, int i) {
            KeyboardShortcuts.this.dismissKeyboardShortcuts();
        }
    };
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private KeyCharacterMap mKeyCharacterMap;
    private Dialog mKeyboardShortcutsDialog;
    private final SparseArray<Drawable> mModifierDrawables = new SparseArray<>();
    private final int[] mModifierList = {65536, 4096, 2, 1, 4, 8};
    private final SparseArray<String> mModifierNames = new SparseArray<>();
    private final IPackageManager mPackageManager;
    private final SparseArray<Drawable> mSpecialCharacterDrawables = new SparseArray<>();
    private final SparseArray<String> mSpecialCharacterNames = new SparseArray<>();

    private KeyboardShortcuts(Context context) {
        this.mContext = new ContextThemeWrapper(context, 16974123);
        this.mPackageManager = AppGlobals.getPackageManager();
        loadResources(context);
    }

    private static KeyboardShortcuts getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new KeyboardShortcuts(context);
        }
        return sInstance;
    }

    public static void show(Context context, int i) {
        MetricsLogger.visible(context, 500);
        synchronized (sLock) {
            if (sInstance != null && !sInstance.mContext.equals(context)) {
                dismiss();
            }
            getInstance(context).showKeyboardShortcuts(i);
        }
    }

    public static void toggle(Context context, int i) {
        synchronized (sLock) {
            if (isShowing()) {
                dismiss();
            } else {
                show(context, i);
            }
        }
    }

    public static void dismiss() {
        synchronized (sLock) {
            if (sInstance != null) {
                MetricsLogger.hidden(sInstance.mContext, 500);
                sInstance.dismissKeyboardShortcuts();
                sInstance = null;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0004, code lost:
        r0 = r0.mKeyboardShortcutsDialog;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean isShowing() {
        /*
            com.android.systemui.statusbar.KeyboardShortcuts r0 = sInstance
            if (r0 == 0) goto L_0x0010
            android.app.Dialog r0 = r0.mKeyboardShortcutsDialog
            if (r0 == 0) goto L_0x0010
            boolean r0 = r0.isShowing()
            if (r0 == 0) goto L_0x0010
            r0 = 1
            goto L_0x0011
        L_0x0010:
            r0 = 0
        L_0x0011:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.KeyboardShortcuts.isShowing():boolean");
    }

    private void loadResources(Context context) {
        Context context2 = context;
        this.mSpecialCharacterNames.put(3, context2.getString(R.string.keyboard_key_home));
        this.mSpecialCharacterNames.put(4, context2.getString(R.string.keyboard_key_back));
        this.mSpecialCharacterNames.put(19, context2.getString(R.string.keyboard_key_dpad_up));
        this.mSpecialCharacterNames.put(20, context2.getString(R.string.keyboard_key_dpad_down));
        this.mSpecialCharacterNames.put(21, context2.getString(R.string.keyboard_key_dpad_left));
        this.mSpecialCharacterNames.put(22, context2.getString(R.string.keyboard_key_dpad_right));
        this.mSpecialCharacterNames.put(23, context2.getString(R.string.keyboard_key_dpad_center));
        this.mSpecialCharacterNames.put(56, ".");
        this.mSpecialCharacterNames.put(61, context2.getString(R.string.keyboard_key_tab));
        this.mSpecialCharacterNames.put(62, context2.getString(R.string.keyboard_key_space));
        this.mSpecialCharacterNames.put(66, context2.getString(R.string.keyboard_key_enter));
        this.mSpecialCharacterNames.put(67, context2.getString(R.string.keyboard_key_backspace));
        this.mSpecialCharacterNames.put(85, context2.getString(R.string.keyboard_key_media_play_pause));
        this.mSpecialCharacterNames.put(86, context2.getString(R.string.keyboard_key_media_stop));
        this.mSpecialCharacterNames.put(87, context2.getString(R.string.keyboard_key_media_next));
        this.mSpecialCharacterNames.put(88, context2.getString(R.string.keyboard_key_media_previous));
        this.mSpecialCharacterNames.put(89, context2.getString(R.string.keyboard_key_media_rewind));
        this.mSpecialCharacterNames.put(90, context2.getString(R.string.keyboard_key_media_fast_forward));
        this.mSpecialCharacterNames.put(92, context2.getString(R.string.keyboard_key_page_up));
        this.mSpecialCharacterNames.put(93, context2.getString(R.string.keyboard_key_page_down));
        this.mSpecialCharacterNames.put(96, context2.getString(R.string.keyboard_key_button_template, new Object[]{"A"}));
        this.mSpecialCharacterNames.put(97, context2.getString(R.string.keyboard_key_button_template, new Object[]{"B"}));
        this.mSpecialCharacterNames.put(98, context2.getString(R.string.keyboard_key_button_template, new Object[]{"C"}));
        this.mSpecialCharacterNames.put(99, context2.getString(R.string.keyboard_key_button_template, new Object[]{"X"}));
        this.mSpecialCharacterNames.put(100, context2.getString(R.string.keyboard_key_button_template, new Object[]{"Y"}));
        this.mSpecialCharacterNames.put(R.styleable.AppCompatTheme_switchStyle, context2.getString(R.string.keyboard_key_button_template, new Object[]{"Z"}));
        this.mSpecialCharacterNames.put(R.styleable.AppCompatTheme_textAppearanceLargePopupMenu, context2.getString(R.string.keyboard_key_button_template, new Object[]{"L1"}));
        this.mSpecialCharacterNames.put(R.styleable.AppCompatTheme_textAppearanceListItem, context2.getString(R.string.keyboard_key_button_template, new Object[]{"R1"}));
        this.mSpecialCharacterNames.put(R.styleable.AppCompatTheme_textAppearanceListItemSecondary, context2.getString(R.string.keyboard_key_button_template, new Object[]{"L2"}));
        this.mSpecialCharacterNames.put(R.styleable.AppCompatTheme_textAppearanceListItemSmall, context2.getString(R.string.keyboard_key_button_template, new Object[]{"R2"}));
        this.mSpecialCharacterNames.put(R.styleable.AppCompatTheme_textAppearanceSearchResultTitle, context2.getString(R.string.keyboard_key_button_template, new Object[]{"Start"}));
        this.mSpecialCharacterNames.put(R.styleable.AppCompatTheme_textAppearanceSmallPopupMenu, context2.getString(R.string.keyboard_key_button_template, new Object[]{"Select"}));
        this.mSpecialCharacterNames.put(R.styleable.AppCompatTheme_textColorAlertDialogListItem, context2.getString(R.string.keyboard_key_button_template, new Object[]{"Mode"}));
        this.mSpecialCharacterNames.put(R.styleable.AppCompatTheme_toolbarNavigationButtonStyle, context2.getString(R.string.keyboard_key_forward_del));
        this.mSpecialCharacterNames.put(R.styleable.AppCompatTheme_textColorSearchUrl, "Esc");
        this.mSpecialCharacterNames.put(120, "SysRq");
        this.mSpecialCharacterNames.put(121, "Break");
        this.mSpecialCharacterNames.put(116, "Scroll Lock");
        this.mSpecialCharacterNames.put(122, context2.getString(R.string.keyboard_key_move_home));
        this.mSpecialCharacterNames.put(123, context2.getString(R.string.keyboard_key_move_end));
        this.mSpecialCharacterNames.put(124, context2.getString(R.string.keyboard_key_insert));
        this.mSpecialCharacterNames.put(131, "F1");
        this.mSpecialCharacterNames.put(132, "F2");
        this.mSpecialCharacterNames.put(133, "F3");
        this.mSpecialCharacterNames.put(134, "F4");
        this.mSpecialCharacterNames.put(135, "F5");
        this.mSpecialCharacterNames.put(136, "F6");
        this.mSpecialCharacterNames.put(137, "F7");
        this.mSpecialCharacterNames.put(138, "F8");
        this.mSpecialCharacterNames.put(139, "F9");
        this.mSpecialCharacterNames.put(140, "F10");
        this.mSpecialCharacterNames.put(141, "F11");
        this.mSpecialCharacterNames.put(142, "F12");
        this.mSpecialCharacterNames.put(143, context2.getString(R.string.keyboard_key_num_lock));
        this.mSpecialCharacterNames.put(144, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"0"}));
        this.mSpecialCharacterNames.put(145, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"1"}));
        this.mSpecialCharacterNames.put(146, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"2"}));
        this.mSpecialCharacterNames.put(147, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"3"}));
        this.mSpecialCharacterNames.put(148, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"4"}));
        this.mSpecialCharacterNames.put(149, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"5"}));
        this.mSpecialCharacterNames.put(150, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"6"}));
        this.mSpecialCharacterNames.put(151, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"7"}));
        this.mSpecialCharacterNames.put(152, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"8"}));
        this.mSpecialCharacterNames.put(153, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"9"}));
        this.mSpecialCharacterNames.put(154, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"/"}));
        this.mSpecialCharacterNames.put(155, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"*"}));
        this.mSpecialCharacterNames.put(156, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"-"}));
        this.mSpecialCharacterNames.put(157, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"+"}));
        this.mSpecialCharacterNames.put(158, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"."}));
        this.mSpecialCharacterNames.put(159, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{","}));
        this.mSpecialCharacterNames.put(160, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{context2.getString(R.string.keyboard_key_enter)}));
        this.mSpecialCharacterNames.put(161, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"="}));
        this.mSpecialCharacterNames.put(162, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{"("}));
        this.mSpecialCharacterNames.put(163, context2.getString(R.string.keyboard_key_numpad_template, new Object[]{")"}));
        this.mSpecialCharacterNames.put(211, "半角/全角");
        this.mSpecialCharacterNames.put(212, "英数");
        this.mSpecialCharacterNames.put(213, "無変換");
        this.mSpecialCharacterNames.put(214, "変換");
        this.mSpecialCharacterNames.put(215, "かな");
        this.mModifierNames.put(65536, "Meta");
        this.mModifierNames.put(4096, "Ctrl");
        this.mModifierNames.put(2, "Alt");
        this.mModifierNames.put(1, "Shift");
        this.mModifierNames.put(4, "Sym");
        this.mModifierNames.put(8, "Fn");
        this.mSpecialCharacterDrawables.put(67, context2.getDrawable(R.drawable.ic_ksh_key_backspace));
        this.mSpecialCharacterDrawables.put(66, context2.getDrawable(R.drawable.ic_ksh_key_enter));
        this.mSpecialCharacterDrawables.put(19, context2.getDrawable(R.drawable.ic_ksh_key_up));
        this.mSpecialCharacterDrawables.put(22, context2.getDrawable(R.drawable.ic_ksh_key_right));
        this.mSpecialCharacterDrawables.put(20, context2.getDrawable(R.drawable.ic_ksh_key_down));
        this.mSpecialCharacterDrawables.put(21, context2.getDrawable(R.drawable.ic_ksh_key_left));
        this.mModifierDrawables.put(65536, context2.getDrawable(R.drawable.ic_ksh_key_meta));
    }

    private void retrieveKeyCharacterMap(int i) {
        InputDevice inputDevice;
        InputManager instance = InputManager.getInstance();
        this.mBackupKeyCharacterMap = instance.getInputDevice(-1).getKeyCharacterMap();
        if (i == -1 || (inputDevice = instance.getInputDevice(i)) == null) {
            int[] inputDeviceIds = instance.getInputDeviceIds();
            int i2 = 0;
            while (i2 < inputDeviceIds.length) {
                InputDevice inputDevice2 = instance.getInputDevice(inputDeviceIds[i2]);
                if (inputDevice2.getId() == -1 || !inputDevice2.isFullKeyboard()) {
                    i2++;
                } else {
                    this.mKeyCharacterMap = inputDevice2.getKeyCharacterMap();
                    return;
                }
            }
            this.mKeyCharacterMap = this.mBackupKeyCharacterMap;
            return;
        }
        this.mKeyCharacterMap = inputDevice.getKeyCharacterMap();
    }

    private void showKeyboardShortcuts(int i) {
        retrieveKeyCharacterMap(i);
        WindowManagerCompat.requestAppKeyboardShortcuts(Recents.getSystemServices().mWm, new WindowManagerCompat.IKeyboardShortcutsReceiver() {
            public void onKeyboardShortcutsReceived(List<KeyboardShortcutGroup> list) {
                list.add(KeyboardShortcuts.this.getSystemShortcuts());
                KeyboardShortcutGroup access$200 = KeyboardShortcuts.this.getDefaultApplicationShortcuts();
                if (access$200 != null) {
                    list.add(access$200);
                }
                KeyboardShortcuts.this.showKeyboardShortcutsDialog(list);
            }
        }, i);
    }

    /* access modifiers changed from: private */
    public void dismissKeyboardShortcuts() {
        Dialog dialog = this.mKeyboardShortcutsDialog;
        if (dialog != null) {
            dialog.dismiss();
            this.mKeyboardShortcutsDialog = null;
        }
    }

    /* access modifiers changed from: private */
    public KeyboardShortcutGroup getSystemShortcuts() {
        KeyboardShortcutGroup keyboardShortcutGroup = new KeyboardShortcutGroup(this.mContext.getString(R.string.keyboard_shortcut_group_system), true);
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_system_home), 66, 65536));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_system_back), 67, 65536));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_system_recents), 61, 2));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_system_notifications), 42, 65536));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_system_shortcuts_helper), 76, 65536));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_system_switch_input), 62, 65536));
        return keyboardShortcutGroup;
    }

    /* access modifiers changed from: private */
    public KeyboardShortcutGroup getDefaultApplicationShortcuts() {
        PackageInfo packageInfo;
        int userId = this.mContext.getUserId();
        ArrayList arrayList = new ArrayList();
        ComponentName assistComponentForUser = new AssistUtils(this.mContext).getAssistComponentForUser(userId);
        if (assistComponentForUser != null) {
            try {
                packageInfo = this.mPackageManager.getPackageInfo(assistComponentForUser.getPackageName(), 0, userId);
            } catch (RemoteException unused) {
                Log.e(TAG, "PackageManagerService is dead");
                packageInfo = null;
            }
            if (packageInfo != null) {
                ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_applications_assist), Icon.createWithResource(applicationInfo.packageName, applicationInfo.icon), 0, 65536));
            }
        }
        Icon iconForIntentCategory = getIconForIntentCategory("android.intent.category.APP_BROWSER", userId);
        if (iconForIntentCategory != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_applications_browser), iconForIntentCategory, 30, 65536));
        }
        Icon iconForIntentCategory2 = getIconForIntentCategory("android.intent.category.APP_CONTACTS", userId);
        if (iconForIntentCategory2 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_applications_contacts), iconForIntentCategory2, 31, 65536));
        }
        Icon iconForIntentCategory3 = getIconForIntentCategory("android.intent.category.APP_EMAIL", userId);
        if (iconForIntentCategory3 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_applications_email), iconForIntentCategory3, 33, 65536));
        }
        Icon iconForIntentCategory4 = getIconForIntentCategory("android.intent.category.APP_MESSAGING", userId);
        if (iconForIntentCategory4 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_applications_sms), iconForIntentCategory4, 47, 65536));
        }
        Icon iconForIntentCategory5 = getIconForIntentCategory("android.intent.category.APP_MUSIC", userId);
        if (iconForIntentCategory5 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_applications_music), iconForIntentCategory5, 44, 65536));
        }
        Icon iconForIntentCategory6 = getIconForIntentCategory("android.intent.category.APP_CALENDAR", userId);
        if (iconForIntentCategory6 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(R.string.keyboard_shortcut_group_applications_calendar), iconForIntentCategory6, 40, 65536));
        }
        if (arrayList.size() == 0) {
            return null;
        }
        Collections.sort(arrayList, this.mApplicationItemsComparator);
        return new KeyboardShortcutGroup(this.mContext.getString(R.string.keyboard_shortcut_group_applications), arrayList, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0010, code lost:
        r2 = r2.applicationInfo;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private android.graphics.drawable.Icon getIconForIntentCategory(java.lang.String r3, int r4) {
        /*
            r2 = this;
            android.content.Intent r0 = new android.content.Intent
            java.lang.String r1 = "android.intent.action.MAIN"
            r0.<init>(r1)
            r0.addCategory(r3)
            android.content.pm.PackageInfo r2 = r2.getPackageInfoForIntent(r0, r4)
            if (r2 == 0) goto L_0x001d
            android.content.pm.ApplicationInfo r2 = r2.applicationInfo
            int r3 = r2.icon
            if (r3 == 0) goto L_0x001d
            java.lang.String r2 = r2.packageName
            android.graphics.drawable.Icon r2 = android.graphics.drawable.Icon.createWithResource(r2, r3)
            return r2
        L_0x001d:
            r2 = 0
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.KeyboardShortcuts.getIconForIntentCategory(java.lang.String, int):android.graphics.drawable.Icon");
    }

    private PackageInfo getPackageInfoForIntent(Intent intent, int i) {
        try {
            ResolveInfo resolveIntent = this.mPackageManager.resolveIntent(intent, intent.resolveTypeIfNeeded(this.mContext.getContentResolver()), 0, i);
            if (resolveIntent != null) {
                if (resolveIntent.activityInfo != null) {
                    return this.mPackageManager.getPackageInfo(resolveIntent.activityInfo.packageName, 0, i);
                }
            }
            return null;
        } catch (RemoteException e) {
            Log.e(TAG, "PackageManagerService is dead", e);
            return null;
        }
    }

    /* access modifiers changed from: private */
    public void showKeyboardShortcutsDialog(final List<KeyboardShortcutGroup> list) {
        this.mHandler.post(new Runnable() {
            public void run() {
                KeyboardShortcuts.this.handleShowKeyboardShortcuts(list);
            }
        });
    }

    /* access modifiers changed from: private */
    public void handleShowKeyboardShortcuts(List<KeyboardShortcutGroup> list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        View inflate = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.keyboard_shortcuts_view, (ViewGroup) null);
        populateKeyboardShortcuts((LinearLayout) inflate.findViewById(R.id.keyboard_shortcuts_container), list);
        builder.setView(inflate);
        builder.setPositiveButton(R.string.quick_settings_done, this.mDialogCloseListener);
        AlertDialog create = builder.create();
        this.mKeyboardShortcutsDialog = create;
        create.setCanceledOnTouchOutside(true);
        this.mKeyboardShortcutsDialog.getWindow().setType(2008);
        synchronized (sLock) {
            if (sInstance != null) {
                this.mKeyboardShortcutsDialog.show();
            }
        }
    }

    private void populateKeyboardShortcuts(LinearLayout linearLayout, List<KeyboardShortcutGroup> list) {
        int i;
        boolean z;
        int i2;
        int i3;
        int i4;
        int i5;
        KeyboardShortcutGroup keyboardShortcutGroup;
        char c;
        int i6;
        int i7;
        List<StringDrawableContainer> list2;
        int i8;
        int i9;
        LinearLayout linearLayout2 = linearLayout;
        LayoutInflater from = LayoutInflater.from(this.mContext);
        int size = list.size();
        char c2 = 127;
        boolean z2 = false;
        TextView textView = (TextView) from.inflate(R.layout.keyboard_shortcuts_key_view, (ViewGroup) null, false);
        textView.measure(0, 0);
        int measuredHeight = textView.getMeasuredHeight();
        int measuredHeight2 = (textView.getMeasuredHeight() - textView.getPaddingTop()) - textView.getPaddingBottom();
        int i10 = 0;
        while (i10 < size) {
            KeyboardShortcutGroup keyboardShortcutGroup2 = list.get(i10);
            TextView textView2 = (TextView) from.inflate(R.layout.keyboard_shortcuts_category_title, linearLayout2, z2);
            textView2.setText(keyboardShortcutGroup2.getLabel());
            if (keyboardShortcutGroup2.isSystemGroup()) {
                i = Utils.getColorAccent(this.mContext);
            } else {
                i = this.mContext.getColor(R.color.ksh_application_group_color);
            }
            textView2.setTextColor(i);
            linearLayout2.addView(textView2);
            LinearLayout linearLayout3 = (LinearLayout) from.inflate(R.layout.keyboard_shortcuts_container, linearLayout2, z2);
            int size2 = keyboardShortcutGroup2.getItems().size();
            int i11 = z2;
            while (i11 < size2) {
                KeyboardShortcutInfo keyboardShortcutInfo = keyboardShortcutGroup2.getItems().get(i11);
                List<StringDrawableContainer> humanReadableShortcutKeys = getHumanReadableShortcutKeys(keyboardShortcutInfo);
                if (humanReadableShortcutKeys == null) {
                    Log.w(TAG, "Keyboard Shortcut contains unsupported keys, skipping.");
                    i2 = size;
                    c = c2;
                    i3 = i10;
                    i4 = measuredHeight2;
                    keyboardShortcutGroup = keyboardShortcutGroup2;
                    i5 = size2;
                } else {
                    View inflate = from.inflate(R.layout.keyboard_shortcut_app_item, linearLayout3, z2);
                    if (keyboardShortcutInfo.getIcon() != null) {
                        ImageView imageView = (ImageView) inflate.findViewById(R.id.keyboard_shortcuts_icon);
                        imageView.setImageIcon(keyboardShortcutInfo.getIcon());
                        imageView.setVisibility(0);
                    }
                    TextView textView3 = (TextView) inflate.findViewById(R.id.keyboard_shortcuts_keyword);
                    textView3.setText(keyboardShortcutInfo.getLabel());
                    if (keyboardShortcutInfo.getIcon() != null) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) textView3.getLayoutParams();
                        layoutParams.removeRule(20);
                        textView3.setLayoutParams(layoutParams);
                    }
                    ViewGroup viewGroup = (ViewGroup) inflate.findViewById(R.id.keyboard_shortcuts_item_container);
                    int size3 = humanReadableShortcutKeys.size();
                    int i12 = 0;
                    while (i12 < size3) {
                        int i13 = size3;
                        StringDrawableContainer stringDrawableContainer = humanReadableShortcutKeys.get(i12);
                        KeyboardShortcutGroup keyboardShortcutGroup3 = keyboardShortcutGroup2;
                        if (stringDrawableContainer.mDrawable != null) {
                            i9 = size2;
                            ImageView imageView2 = (ImageView) from.inflate(R.layout.keyboard_shortcuts_key_icon_view, viewGroup, false);
                            Bitmap createBitmap = Bitmap.createBitmap(measuredHeight2, measuredHeight2, Bitmap.Config.ARGB_8888);
                            i8 = measuredHeight2;
                            Canvas canvas = new Canvas(createBitmap);
                            list2 = humanReadableShortcutKeys;
                            i7 = i10;
                            i6 = size;
                            stringDrawableContainer.mDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                            stringDrawableContainer.mDrawable.draw(canvas);
                            imageView2.setImageBitmap(createBitmap);
                            imageView2.setImportantForAccessibility(1);
                            imageView2.setAccessibilityDelegate(new ShortcutKeyAccessibilityDelegate(this, stringDrawableContainer.mString));
                            viewGroup.addView(imageView2);
                        } else {
                            i6 = size;
                            i7 = i10;
                            i8 = measuredHeight2;
                            i9 = size2;
                            list2 = humanReadableShortcutKeys;
                            if (stringDrawableContainer.mString != null) {
                                TextView textView4 = (TextView) from.inflate(R.layout.keyboard_shortcuts_key_view, viewGroup, false);
                                textView4.setMinimumWidth(measuredHeight);
                                textView4.setText(stringDrawableContainer.mString);
                                textView4.setAccessibilityDelegate(new ShortcutKeyAccessibilityDelegate(this, stringDrawableContainer.mString));
                                viewGroup.addView(textView4);
                                i12++;
                                LinearLayout linearLayout4 = linearLayout;
                                keyboardShortcutGroup2 = keyboardShortcutGroup3;
                                size3 = i13;
                                size2 = i9;
                                measuredHeight2 = i8;
                                humanReadableShortcutKeys = list2;
                                i10 = i7;
                                size = i6;
                            }
                        }
                        i12++;
                        LinearLayout linearLayout42 = linearLayout;
                        keyboardShortcutGroup2 = keyboardShortcutGroup3;
                        size3 = i13;
                        size2 = i9;
                        measuredHeight2 = i8;
                        humanReadableShortcutKeys = list2;
                        i10 = i7;
                        size = i6;
                    }
                    i2 = size;
                    i3 = i10;
                    i4 = measuredHeight2;
                    keyboardShortcutGroup = keyboardShortcutGroup2;
                    i5 = size2;
                    c = 127;
                    linearLayout3.addView(inflate);
                }
                i11++;
                List<KeyboardShortcutGroup> list3 = list;
                c2 = c;
                keyboardShortcutGroup2 = keyboardShortcutGroup;
                size2 = i5;
                measuredHeight2 = i4;
                i10 = i3;
                size = i2;
                z2 = false;
                linearLayout2 = linearLayout;
            }
            int i14 = size;
            int i15 = measuredHeight2;
            LinearLayout linearLayout5 = linearLayout2;
            char c3 = c2;
            linearLayout5.addView(linearLayout3);
            int i16 = i10;
            if (i16 < i14 - 1) {
                z = false;
                linearLayout5.addView(from.inflate(R.layout.keyboard_shortcuts_category_separator, linearLayout5, false));
            } else {
                z = false;
            }
            int i17 = i16 + 1;
            z2 = z;
            measuredHeight2 = i15;
            i10 = i17;
            c2 = c3;
            linearLayout2 = linearLayout5;
            size = i14;
        }
    }

    private List<StringDrawableContainer> getHumanReadableShortcutKeys(KeyboardShortcutInfo keyboardShortcutInfo) {
        String str;
        List<StringDrawableContainer> humanReadableModifiers = getHumanReadableModifiers(keyboardShortcutInfo);
        Drawable drawable = null;
        if (humanReadableModifiers == null) {
            return null;
        }
        if (keyboardShortcutInfo.getBaseCharacter() > 0) {
            str = String.valueOf(keyboardShortcutInfo.getBaseCharacter());
        } else if (this.mSpecialCharacterDrawables.get(keyboardShortcutInfo.getKeycode()) != null) {
            drawable = this.mSpecialCharacterDrawables.get(keyboardShortcutInfo.getKeycode());
            str = this.mSpecialCharacterNames.get(keyboardShortcutInfo.getKeycode());
        } else if (this.mSpecialCharacterNames.get(keyboardShortcutInfo.getKeycode()) != null) {
            str = this.mSpecialCharacterNames.get(keyboardShortcutInfo.getKeycode());
        } else if (keyboardShortcutInfo.getKeycode() == 0) {
            return humanReadableModifiers;
        } else {
            char displayLabel = this.mKeyCharacterMap.getDisplayLabel(keyboardShortcutInfo.getKeycode());
            if (displayLabel != 0) {
                str = String.valueOf(displayLabel);
            } else {
                char displayLabel2 = this.mBackupKeyCharacterMap.getDisplayLabel(keyboardShortcutInfo.getKeycode());
                if (displayLabel2 == 0) {
                    return null;
                }
                str = String.valueOf(displayLabel2);
            }
        }
        if (str != null) {
            humanReadableModifiers.add(new StringDrawableContainer(str, drawable));
        } else {
            Log.w(TAG, "Keyboard Shortcut does not have a text representation, skipping.");
        }
        return humanReadableModifiers;
    }

    private List<StringDrawableContainer> getHumanReadableModifiers(KeyboardShortcutInfo keyboardShortcutInfo) {
        ArrayList arrayList = new ArrayList();
        int modifiers = keyboardShortcutInfo.getModifiers();
        if (modifiers == 0) {
            return arrayList;
        }
        int i = 0;
        while (true) {
            int[] iArr = this.mModifierList;
            if (i >= iArr.length) {
                break;
            }
            int i2 = iArr[i];
            if ((modifiers & i2) != 0) {
                arrayList.add(new StringDrawableContainer(this.mModifierNames.get(i2), this.mModifierDrawables.get(i2)));
                modifiers &= ~i2;
            }
            i++;
        }
        if (modifiers != 0) {
            return null;
        }
        return arrayList;
    }

    private final class ShortcutKeyAccessibilityDelegate extends View.AccessibilityDelegate {
        private String mContentDescription;

        ShortcutKeyAccessibilityDelegate(KeyboardShortcuts keyboardShortcuts, String str) {
            this.mContentDescription = str;
        }

        public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo accessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfo);
            String str = this.mContentDescription;
            if (str != null) {
                accessibilityNodeInfo.setContentDescription(str.toLowerCase());
            }
        }
    }

    private static final class StringDrawableContainer {
        public Drawable mDrawable;
        public String mString;

        StringDrawableContainer(String str, Drawable drawable) {
            this.mString = str;
            this.mDrawable = drawable;
        }
    }
}

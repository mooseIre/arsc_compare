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
import android.content.res.ColorStateList;
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
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.constraintlayout.widget.R$styleable;
import com.android.internal.app.AssistUtils;
import com.android.internal.logging.MetricsLogger;
import com.android.settingslib.Utils;
import com.android.systemui.C0011R$color;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.C0021R$string;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class KeyboardShortcuts {
    private static final String TAG = "KeyboardShortcuts";
    private static KeyboardShortcuts sInstance;
    private static final Object sLock = new Object();
    private final Comparator<KeyboardShortcutInfo> mApplicationItemsComparator = new Comparator<KeyboardShortcutInfo>(this) {
        /* class com.android.systemui.statusbar.KeyboardShortcuts.AnonymousClass2 */

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
        /* class com.android.systemui.statusbar.KeyboardShortcuts.AnonymousClass1 */

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
        this.mContext = new ContextThemeWrapper(context, 16974371);
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

    private static boolean isShowing() {
        Dialog dialog;
        KeyboardShortcuts keyboardShortcuts = sInstance;
        return (keyboardShortcuts == null || (dialog = keyboardShortcuts.mKeyboardShortcutsDialog) == null || !dialog.isShowing()) ? false : true;
    }

    private void loadResources(Context context) {
        this.mSpecialCharacterNames.put(3, context.getString(C0021R$string.keyboard_key_home));
        this.mSpecialCharacterNames.put(4, context.getString(C0021R$string.keyboard_key_back));
        this.mSpecialCharacterNames.put(19, context.getString(C0021R$string.keyboard_key_dpad_up));
        this.mSpecialCharacterNames.put(20, context.getString(C0021R$string.keyboard_key_dpad_down));
        this.mSpecialCharacterNames.put(21, context.getString(C0021R$string.keyboard_key_dpad_left));
        this.mSpecialCharacterNames.put(22, context.getString(C0021R$string.keyboard_key_dpad_right));
        this.mSpecialCharacterNames.put(23, context.getString(C0021R$string.keyboard_key_dpad_center));
        this.mSpecialCharacterNames.put(56, ".");
        this.mSpecialCharacterNames.put(61, context.getString(C0021R$string.keyboard_key_tab));
        this.mSpecialCharacterNames.put(62, context.getString(C0021R$string.keyboard_key_space));
        this.mSpecialCharacterNames.put(66, context.getString(C0021R$string.keyboard_key_enter));
        this.mSpecialCharacterNames.put(67, context.getString(C0021R$string.keyboard_key_backspace));
        this.mSpecialCharacterNames.put(85, context.getString(C0021R$string.keyboard_key_media_play_pause));
        this.mSpecialCharacterNames.put(86, context.getString(C0021R$string.keyboard_key_media_stop));
        this.mSpecialCharacterNames.put(87, context.getString(C0021R$string.keyboard_key_media_next));
        this.mSpecialCharacterNames.put(88, context.getString(C0021R$string.keyboard_key_media_previous));
        this.mSpecialCharacterNames.put(89, context.getString(C0021R$string.keyboard_key_media_rewind));
        this.mSpecialCharacterNames.put(90, context.getString(C0021R$string.keyboard_key_media_fast_forward));
        this.mSpecialCharacterNames.put(92, context.getString(C0021R$string.keyboard_key_page_up));
        this.mSpecialCharacterNames.put(93, context.getString(C0021R$string.keyboard_key_page_down));
        this.mSpecialCharacterNames.put(96, context.getString(C0021R$string.keyboard_key_button_template, "A"));
        this.mSpecialCharacterNames.put(97, context.getString(C0021R$string.keyboard_key_button_template, "B"));
        this.mSpecialCharacterNames.put(98, context.getString(C0021R$string.keyboard_key_button_template, "C"));
        this.mSpecialCharacterNames.put(99, context.getString(C0021R$string.keyboard_key_button_template, "X"));
        this.mSpecialCharacterNames.put(100, context.getString(C0021R$string.keyboard_key_button_template, "Y"));
        this.mSpecialCharacterNames.put(R$styleable.Constraint_layout_goneMarginRight, context.getString(C0021R$string.keyboard_key_button_template, "Z"));
        this.mSpecialCharacterNames.put(R$styleable.Constraint_layout_goneMarginStart, context.getString(C0021R$string.keyboard_key_button_template, "L1"));
        this.mSpecialCharacterNames.put(R$styleable.Constraint_layout_goneMarginTop, context.getString(C0021R$string.keyboard_key_button_template, "R1"));
        this.mSpecialCharacterNames.put(R$styleable.Constraint_motionStagger, context.getString(C0021R$string.keyboard_key_button_template, "L2"));
        this.mSpecialCharacterNames.put(R$styleable.Constraint_pathMotionArc, context.getString(C0021R$string.keyboard_key_button_template, "R2"));
        this.mSpecialCharacterNames.put(R$styleable.Constraint_transitionEasing, context.getString(C0021R$string.keyboard_key_button_template, "Start"));
        this.mSpecialCharacterNames.put(R$styleable.Constraint_transitionPathRotate, context.getString(C0021R$string.keyboard_key_button_template, "Select"));
        this.mSpecialCharacterNames.put(R$styleable.Constraint_visibilityMode, context.getString(C0021R$string.keyboard_key_button_template, "Mode"));
        this.mSpecialCharacterNames.put(112, context.getString(C0021R$string.keyboard_key_forward_del));
        this.mSpecialCharacterNames.put(111, "Esc");
        this.mSpecialCharacterNames.put(androidx.appcompat.R$styleable.AppCompatTheme_windowFixedHeightMajor, "SysRq");
        this.mSpecialCharacterNames.put(androidx.appcompat.R$styleable.AppCompatTheme_windowFixedHeightMinor, "Break");
        this.mSpecialCharacterNames.put(androidx.appcompat.R$styleable.AppCompatTheme_viewInflaterClass, "Scroll Lock");
        this.mSpecialCharacterNames.put(androidx.appcompat.R$styleable.AppCompatTheme_windowFixedWidthMajor, context.getString(C0021R$string.keyboard_key_move_home));
        this.mSpecialCharacterNames.put(androidx.appcompat.R$styleable.AppCompatTheme_windowFixedWidthMinor, context.getString(C0021R$string.keyboard_key_move_end));
        this.mSpecialCharacterNames.put(androidx.appcompat.R$styleable.AppCompatTheme_windowMinWidthMajor, context.getString(C0021R$string.keyboard_key_insert));
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
        this.mSpecialCharacterNames.put(143, context.getString(C0021R$string.keyboard_key_num_lock));
        this.mSpecialCharacterNames.put(144, context.getString(C0021R$string.keyboard_key_numpad_template, "0"));
        this.mSpecialCharacterNames.put(145, context.getString(C0021R$string.keyboard_key_numpad_template, "1"));
        this.mSpecialCharacterNames.put(146, context.getString(C0021R$string.keyboard_key_numpad_template, "2"));
        this.mSpecialCharacterNames.put(147, context.getString(C0021R$string.keyboard_key_numpad_template, "3"));
        this.mSpecialCharacterNames.put(148, context.getString(C0021R$string.keyboard_key_numpad_template, "4"));
        this.mSpecialCharacterNames.put(149, context.getString(C0021R$string.keyboard_key_numpad_template, "5"));
        this.mSpecialCharacterNames.put(150, context.getString(C0021R$string.keyboard_key_numpad_template, "6"));
        this.mSpecialCharacterNames.put(151, context.getString(C0021R$string.keyboard_key_numpad_template, "7"));
        this.mSpecialCharacterNames.put(152, context.getString(C0021R$string.keyboard_key_numpad_template, "8"));
        this.mSpecialCharacterNames.put(153, context.getString(C0021R$string.keyboard_key_numpad_template, "9"));
        this.mSpecialCharacterNames.put(154, context.getString(C0021R$string.keyboard_key_numpad_template, "/"));
        this.mSpecialCharacterNames.put(155, context.getString(C0021R$string.keyboard_key_numpad_template, "*"));
        this.mSpecialCharacterNames.put(156, context.getString(C0021R$string.keyboard_key_numpad_template, "-"));
        this.mSpecialCharacterNames.put(157, context.getString(C0021R$string.keyboard_key_numpad_template, "+"));
        this.mSpecialCharacterNames.put(158, context.getString(C0021R$string.keyboard_key_numpad_template, "."));
        this.mSpecialCharacterNames.put(159, context.getString(C0021R$string.keyboard_key_numpad_template, ","));
        this.mSpecialCharacterNames.put(160, context.getString(C0021R$string.keyboard_key_numpad_template, context.getString(C0021R$string.keyboard_key_enter)));
        this.mSpecialCharacterNames.put(161, context.getString(C0021R$string.keyboard_key_numpad_template, "="));
        this.mSpecialCharacterNames.put(162, context.getString(C0021R$string.keyboard_key_numpad_template, "("));
        this.mSpecialCharacterNames.put(163, context.getString(C0021R$string.keyboard_key_numpad_template, ")"));
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
        this.mSpecialCharacterDrawables.put(67, context.getDrawable(C0013R$drawable.ic_ksh_key_backspace));
        this.mSpecialCharacterDrawables.put(66, context.getDrawable(C0013R$drawable.ic_ksh_key_enter));
        this.mSpecialCharacterDrawables.put(19, context.getDrawable(C0013R$drawable.ic_ksh_key_up));
        this.mSpecialCharacterDrawables.put(22, context.getDrawable(C0013R$drawable.ic_ksh_key_right));
        this.mSpecialCharacterDrawables.put(20, context.getDrawable(C0013R$drawable.ic_ksh_key_down));
        this.mSpecialCharacterDrawables.put(21, context.getDrawable(C0013R$drawable.ic_ksh_key_left));
        this.mModifierDrawables.put(65536, context.getDrawable(C0013R$drawable.ic_ksh_key_meta));
    }

    private void retrieveKeyCharacterMap(int i) {
        int[] inputDeviceIds;
        InputDevice inputDevice;
        InputManager instance = InputManager.getInstance();
        this.mBackupKeyCharacterMap = instance.getInputDevice(-1).getKeyCharacterMap();
        if (i == -1 || (inputDevice = instance.getInputDevice(i)) == null) {
            for (int i2 : instance.getInputDeviceIds()) {
                InputDevice inputDevice2 = instance.getInputDevice(i2);
                if (inputDevice2.getId() != -1 && inputDevice2.isFullKeyboard()) {
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
        ((WindowManager) this.mContext.getSystemService("window")).requestAppKeyboardShortcuts(new WindowManager.KeyboardShortcutsReceiver() {
            /* class com.android.systemui.statusbar.KeyboardShortcuts.AnonymousClass3 */

            public void onKeyboardShortcutsReceived(List<KeyboardShortcutGroup> list) {
                list.add(KeyboardShortcuts.this.getSystemShortcuts());
                KeyboardShortcutGroup defaultApplicationShortcuts = KeyboardShortcuts.this.getDefaultApplicationShortcuts();
                if (defaultApplicationShortcuts != null) {
                    list.add(defaultApplicationShortcuts);
                }
                KeyboardShortcuts.this.showKeyboardShortcutsDialog(list);
            }
        }, i);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void dismissKeyboardShortcuts() {
        Dialog dialog = this.mKeyboardShortcutsDialog;
        if (dialog != null) {
            dialog.dismiss();
            this.mKeyboardShortcutsDialog = null;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private KeyboardShortcutGroup getSystemShortcuts() {
        KeyboardShortcutGroup keyboardShortcutGroup = new KeyboardShortcutGroup((CharSequence) this.mContext.getString(C0021R$string.keyboard_shortcut_group_system), true);
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_system_home), 66, 65536));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_system_back), 67, 65536));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_system_recents), 61, 2));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_system_notifications), 42, 65536));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_system_shortcuts_helper), 76, 65536));
        keyboardShortcutGroup.addItem(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_system_switch_input), 62, 65536));
        return keyboardShortcutGroup;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private KeyboardShortcutGroup getDefaultApplicationShortcuts() {
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
                arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_applications_assist), Icon.createWithResource(applicationInfo.packageName, applicationInfo.icon), 0, 65536));
            }
        }
        Icon iconForIntentCategory = getIconForIntentCategory("android.intent.category.APP_BROWSER", userId);
        if (iconForIntentCategory != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_applications_browser), iconForIntentCategory, 30, 65536));
        }
        Icon iconForIntentCategory2 = getIconForIntentCategory("android.intent.category.APP_CONTACTS", userId);
        if (iconForIntentCategory2 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_applications_contacts), iconForIntentCategory2, 31, 65536));
        }
        Icon iconForIntentCategory3 = getIconForIntentCategory("android.intent.category.APP_EMAIL", userId);
        if (iconForIntentCategory3 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_applications_email), iconForIntentCategory3, 33, 65536));
        }
        Icon iconForIntentCategory4 = getIconForIntentCategory("android.intent.category.APP_MESSAGING", userId);
        if (iconForIntentCategory4 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_applications_sms), iconForIntentCategory4, 47, 65536));
        }
        Icon iconForIntentCategory5 = getIconForIntentCategory("android.intent.category.APP_MUSIC", userId);
        if (iconForIntentCategory5 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_applications_music), iconForIntentCategory5, 44, 65536));
        }
        Icon iconForIntentCategory6 = getIconForIntentCategory("android.intent.category.APP_CALENDAR", userId);
        if (iconForIntentCategory6 != null) {
            arrayList.add(new KeyboardShortcutInfo(this.mContext.getString(C0021R$string.keyboard_shortcut_group_applications_calendar), iconForIntentCategory6, 40, 65536));
        }
        if (arrayList.size() == 0) {
            return null;
        }
        Collections.sort(arrayList, this.mApplicationItemsComparator);
        return new KeyboardShortcutGroup(this.mContext.getString(C0021R$string.keyboard_shortcut_group_applications), arrayList, true);
    }

    private Icon getIconForIntentCategory(String str, int i) {
        ApplicationInfo applicationInfo;
        int i2;
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory(str);
        PackageInfo packageInfoForIntent = getPackageInfoForIntent(intent, i);
        if (packageInfoForIntent == null || (i2 = (applicationInfo = packageInfoForIntent.applicationInfo).icon) == 0) {
            return null;
        }
        return Icon.createWithResource(applicationInfo.packageName, i2);
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
    /* access modifiers changed from: public */
    private void showKeyboardShortcutsDialog(final List<KeyboardShortcutGroup> list) {
        this.mHandler.post(new Runnable() {
            /* class com.android.systemui.statusbar.KeyboardShortcuts.AnonymousClass4 */

            public void run() {
                KeyboardShortcuts.this.handleShowKeyboardShortcuts(list);
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleShowKeyboardShortcuts(List<KeyboardShortcutGroup> list) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.mContext);
        View inflate = ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(C0017R$layout.keyboard_shortcuts_view, (ViewGroup) null);
        populateKeyboardShortcuts((LinearLayout) inflate.findViewById(C0015R$id.keyboard_shortcuts_container), list);
        builder.setView(inflate);
        builder.setPositiveButton(C0021R$string.quick_settings_done, this.mDialogCloseListener);
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

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r12v0 */
    /* JADX WARN: Type inference failed for: r12v1, types: [int] */
    private void populateKeyboardShortcuts(LinearLayout linearLayout, List<KeyboardShortcutGroup> list) {
        ColorStateList colorStateList;
        boolean z;
        int i;
        int i2;
        int i3;
        int i4;
        KeyboardShortcutGroup keyboardShortcutGroup;
        int i5;
        int i6;
        List<StringDrawableContainer> list2;
        int i7;
        int i8;
        LinearLayout linearLayout2 = linearLayout;
        LayoutInflater from = LayoutInflater.from(this.mContext);
        int size = list.size();
        boolean z2 = false;
        TextView textView = (TextView) from.inflate(C0017R$layout.keyboard_shortcuts_key_view, (ViewGroup) null, false);
        textView.measure(0, 0);
        int measuredHeight = textView.getMeasuredHeight();
        int measuredHeight2 = (textView.getMeasuredHeight() - textView.getPaddingTop()) - textView.getPaddingBottom();
        int i9 = 0;
        while (i9 < size) {
            KeyboardShortcutGroup keyboardShortcutGroup2 = list.get(i9);
            TextView textView2 = (TextView) from.inflate(C0017R$layout.keyboard_shortcuts_category_title, linearLayout2, z2);
            textView2.setText(keyboardShortcutGroup2.getLabel());
            if (keyboardShortcutGroup2.isSystemGroup()) {
                colorStateList = Utils.getColorAccent(this.mContext);
            } else {
                colorStateList = ColorStateList.valueOf(this.mContext.getColor(C0011R$color.ksh_application_group_color));
            }
            textView2.setTextColor(colorStateList);
            linearLayout2.addView(textView2);
            LinearLayout linearLayout3 = (LinearLayout) from.inflate(C0017R$layout.keyboard_shortcuts_container, linearLayout2, z2);
            int size2 = keyboardShortcutGroup2.getItems().size();
            int i10 = z2;
            while (i10 < size2) {
                KeyboardShortcutInfo keyboardShortcutInfo = keyboardShortcutGroup2.getItems().get(i10 == true ? 1 : 0);
                List<StringDrawableContainer> humanReadableShortcutKeys = getHumanReadableShortcutKeys(keyboardShortcutInfo);
                if (humanReadableShortcutKeys == null) {
                    Log.w(TAG, "Keyboard Shortcut contains unsupported keys, skipping.");
                    i = size;
                    i2 = i9;
                    i3 = measuredHeight2;
                    keyboardShortcutGroup = keyboardShortcutGroup2;
                    i4 = size2;
                } else {
                    View inflate = from.inflate(C0017R$layout.keyboard_shortcut_app_item, linearLayout3, z2);
                    if (keyboardShortcutInfo.getIcon() != null) {
                        ImageView imageView = (ImageView) inflate.findViewById(C0015R$id.keyboard_shortcuts_icon);
                        imageView.setImageIcon(keyboardShortcutInfo.getIcon());
                        imageView.setVisibility(0);
                    }
                    TextView textView3 = (TextView) inflate.findViewById(C0015R$id.keyboard_shortcuts_keyword);
                    textView3.setText(keyboardShortcutInfo.getLabel());
                    if (keyboardShortcutInfo.getIcon() != null) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) textView3.getLayoutParams();
                        layoutParams.removeRule(20);
                        textView3.setLayoutParams(layoutParams);
                    }
                    ViewGroup viewGroup = (ViewGroup) inflate.findViewById(C0015R$id.keyboard_shortcuts_item_container);
                    int size3 = humanReadableShortcutKeys.size();
                    int i11 = 0;
                    while (i11 < size3) {
                        StringDrawableContainer stringDrawableContainer = humanReadableShortcutKeys.get(i11);
                        if (stringDrawableContainer.mDrawable != null) {
                            i8 = size2;
                            ImageView imageView2 = (ImageView) from.inflate(C0017R$layout.keyboard_shortcuts_key_icon_view, viewGroup, false);
                            Bitmap createBitmap = Bitmap.createBitmap(measuredHeight2, measuredHeight2, Bitmap.Config.ARGB_8888);
                            i7 = measuredHeight2;
                            Canvas canvas = new Canvas(createBitmap);
                            list2 = humanReadableShortcutKeys;
                            i6 = i9;
                            i5 = size;
                            stringDrawableContainer.mDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                            stringDrawableContainer.mDrawable.draw(canvas);
                            imageView2.setImageBitmap(createBitmap);
                            imageView2.setImportantForAccessibility(1);
                            imageView2.setAccessibilityDelegate(new ShortcutKeyAccessibilityDelegate(this, stringDrawableContainer.mString));
                            viewGroup.addView(imageView2);
                        } else {
                            i5 = size;
                            i6 = i9;
                            i7 = measuredHeight2;
                            i8 = size2;
                            list2 = humanReadableShortcutKeys;
                            if (stringDrawableContainer.mString != null) {
                                TextView textView4 = (TextView) from.inflate(C0017R$layout.keyboard_shortcuts_key_view, viewGroup, false);
                                textView4.setMinimumWidth(measuredHeight);
                                textView4.setText(stringDrawableContainer.mString);
                                textView4.setAccessibilityDelegate(new ShortcutKeyAccessibilityDelegate(this, stringDrawableContainer.mString));
                                viewGroup.addView(textView4);
                            }
                        }
                        i11++;
                        keyboardShortcutGroup2 = keyboardShortcutGroup2;
                        size3 = size3;
                        size2 = i8;
                        measuredHeight2 = i7;
                        humanReadableShortcutKeys = list2;
                        i9 = i6;
                        size = i5;
                    }
                    i = size;
                    i2 = i9;
                    i3 = measuredHeight2;
                    keyboardShortcutGroup = keyboardShortcutGroup2;
                    i4 = size2;
                    linearLayout3.addView(inflate);
                }
                linearLayout2 = linearLayout;
                keyboardShortcutGroup2 = keyboardShortcutGroup;
                size2 = i4;
                measuredHeight2 = i3;
                i9 = i2;
                size = i;
                z2 = false;
                i10++;
            }
            linearLayout2.addView(linearLayout3);
            if (i9 < size - 1) {
                z = false;
                linearLayout2.addView(from.inflate(C0017R$layout.keyboard_shortcuts_category_separator, (ViewGroup) linearLayout2, false));
            } else {
                z = false;
            }
            z2 = z;
            measuredHeight2 = measuredHeight2;
            i9++;
            size = size;
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

    /* access modifiers changed from: private */
    public final class ShortcutKeyAccessibilityDelegate extends View.AccessibilityDelegate {
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

    /* access modifiers changed from: private */
    public static final class StringDrawableContainer {
        public Drawable mDrawable;
        public String mString;

        StringDrawableContainer(String str, Drawable drawable) {
            this.mString = str;
            this.mDrawable = drawable;
        }
    }
}

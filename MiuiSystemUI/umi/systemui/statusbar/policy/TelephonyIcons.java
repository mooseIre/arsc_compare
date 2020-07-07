package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.telephony.SignalStrength;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import com.android.systemui.MCCUtils;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.policy.MobileSignalController;

public class TelephonyIcons {
    static final MobileSignalController.MobileIconGroup CARRIER_NETWORK_CHANGE;
    static final MobileSignalController.MobileIconGroup DATA_DISABLED;
    static final MobileSignalController.MobileIconGroup E;
    static final MobileSignalController.MobileIconGroup FIVE_G_BASIC;
    static final MobileSignalController.MobileIconGroup FIVE_G_SA;
    static final MobileSignalController.MobileIconGroup FIVE_G_UWB;
    static final MobileSignalController.MobileIconGroup FOUR_G;
    static final MobileSignalController.MobileIconGroup FOUR_G_PLUS;
    static final MobileSignalController.MobileIconGroup G;
    static final MobileSignalController.MobileIconGroup H;
    static final MobileSignalController.MobileIconGroup LTE;
    static final MobileSignalController.MobileIconGroup LTE_PLUS;
    static final MobileSignalController.MobileIconGroup ONE_X;
    public static final int[][] TELEPHONY_SIGNAL_STRENGTH = {new int[]{R.drawable.stat_sys_signal_0, R.drawable.stat_sys_signal_1, R.drawable.stat_sys_signal_2, R.drawable.stat_sys_signal_3, R.drawable.stat_sys_signal_4, R.drawable.stat_sys_signal_5}};
    static final int[][] TELEPHONY_SIGNAL_STRENGTH_ROAMING_R = {new int[]{R.drawable.stat_sys_signal_0_default_roam, R.drawable.stat_sys_signal_1_default_roam, R.drawable.stat_sys_signal_2_default_roam, R.drawable.stat_sys_signal_3_default_roam, R.drawable.stat_sys_signal_4_default_roam}, new int[]{R.drawable.stat_sys_signal_0_default_fully_roam, R.drawable.stat_sys_signal_1_default_fully_roam, R.drawable.stat_sys_signal_2_default_fully_roam, R.drawable.stat_sys_signal_3_default_fully_roam, R.drawable.stat_sys_signal_4_default_fully_roam}};
    static final MobileSignalController.MobileIconGroup THREE_G;
    static final MobileSignalController.MobileIconGroup UNKNOWN;
    static final MobileSignalController.MobileIconGroup WFC;
    private static boolean isInitiated = false;
    static String[] mDataActivityArray;
    static String[] mDataTypeArray;
    static String[] mDataTypeDescriptionArray;
    static String[] mDataTypeGenerationArray;
    static String[] mDataTypeGenerationDescArray;
    static SparseArray<String> mDataTypeNameCusRegMap = new SparseArray<>();
    static String[] mDataTypeNameDefault;
    static SparseArray<String> mDataTypeNameMIUIRegion = new SparseArray<>();
    static SparseArray<SparseArray<String>> mDataTypeNameMcc = new SparseArray<>();
    static SparseArray<SparseArray<String>> mDataTypeNameMccMnc = new SparseArray<>();
    private static Resources mRes;
    static int[] mSelectedDataActivityIndex;
    static String[] mSelectedDataTypeDesc;
    static int[] mSelectedDataTypeIcon;
    static int[] mSelectedQSDataTypeIcon;
    static int[] mSelectedSignalStreagthIndex;
    static String[] mSignalNullArray;
    static String[] mSignalStrengthArray;
    static String[] mSignalStrengthDesc;
    static SparseArray<Integer> mStacked2SingleIconLookup;

    static int getStackedVoiceIcon(int i) {
        if (i == 0) {
            return R.drawable.stat_sys_signal_0_2g;
        }
        if (i == 1) {
            return R.drawable.stat_sys_signal_1_2g;
        }
        if (i == 2) {
            return R.drawable.stat_sys_signal_2_2g;
        }
        if (i == 3) {
            return R.drawable.stat_sys_signal_3_2g;
        }
        if (i != 4) {
            return 0;
        }
        return R.drawable.stat_sys_signal_4_2g;
    }

    static {
        mDataTypeNameMcc.put(0, new SparseArray());
        mDataTypeNameMcc.put(1, new SparseArray());
        mDataTypeNameMccMnc.put(0, new SparseArray());
        mDataTypeNameMccMnc.put(1, new SparseArray());
        int[] iArr = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        CARRIER_NETWORK_CHANGE = new MobileSignalController.MobileIconGroup("CARRIER_NETWORK_CHANGE", (int[][]) null, (int[][]) null, iArr, 0, 0, 0, 0, iArr[0], R.string.accessibility_carrier_network_change_mode, 0, false, 0);
        int[] iArr2 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        THREE_G = new MobileSignalController.MobileIconGroup("3G", (int[][]) null, (int[][]) null, iArr2, 0, 0, 0, 0, iArr2[0], R.string.accessibility_data_connection_3g, R.drawable.stat_sys_data_fully_connected_3g, true, R.drawable.ic_qs_signal_3g);
        int[] iArr3 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        WFC = new MobileSignalController.MobileIconGroup("WFC", (int[][]) null, (int[][]) null, iArr3, 0, 0, 0, 0, iArr3[0], 0, 0, false, 0);
        int[] iArr4 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        UNKNOWN = new MobileSignalController.MobileIconGroup("Unknown", (int[][]) null, (int[][]) null, iArr4, 0, 0, 0, 0, iArr4[0], 0, 0, false, 0);
        int[] iArr5 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        E = new MobileSignalController.MobileIconGroup("E", (int[][]) null, (int[][]) null, iArr5, 0, 0, 0, 0, iArr5[0], R.string.accessibility_data_connection_edge, R.drawable.stat_sys_data_fully_connected_e, false, R.drawable.ic_qs_signal_e);
        int[] iArr6 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        ONE_X = new MobileSignalController.MobileIconGroup("1X", (int[][]) null, (int[][]) null, iArr6, 0, 0, 0, 0, iArr6[0], R.string.accessibility_data_connection_cdma, R.drawable.stat_sys_data_fully_connected_1x, true, R.drawable.ic_qs_signal_1x);
        int[] iArr7 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        G = new MobileSignalController.MobileIconGroup("G", (int[][]) null, (int[][]) null, iArr7, 0, 0, 0, 0, iArr7[0], R.string.accessibility_data_connection_gprs, R.drawable.stat_sys_data_fully_connected_g, false, R.drawable.ic_qs_signal_g);
        int[] iArr8 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        H = new MobileSignalController.MobileIconGroup("H", (int[][]) null, (int[][]) null, iArr8, 0, 0, 0, 0, iArr8[0], R.string.f0accessibility_data_connection_35g, R.drawable.stat_sys_data_fully_connected_h, false, R.drawable.ic_qs_signal_h);
        int[] iArr9 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        FOUR_G = new MobileSignalController.MobileIconGroup("4G", (int[][]) null, (int[][]) null, iArr9, 0, 0, 0, 0, iArr9[0], R.string.accessibility_data_connection_4g, R.drawable.stat_sys_data_fully_connected_4g, true, R.drawable.ic_qs_signal_4g);
        int[] iArr10 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        FOUR_G_PLUS = new MobileSignalController.MobileIconGroup("4G+", (int[][]) null, (int[][]) null, iArr10, 0, 0, 0, 0, iArr10[0], R.string.accessibility_data_connection_4g_plus, R.drawable.stat_sys_data_fully_connected_4g_plus, true, R.drawable.ic_qs_signal_4g_plus);
        int[] iArr11 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        LTE = new MobileSignalController.MobileIconGroup("LTE", (int[][]) null, (int[][]) null, iArr11, 0, 0, 0, 0, iArr11[0], R.string.accessibility_data_connection_lte, R.drawable.stat_sys_data_fully_connected_lte, true, R.drawable.ic_qs_signal_lte);
        int[] iArr12 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        LTE_PLUS = new MobileSignalController.MobileIconGroup("LTE+", (int[][]) null, (int[][]) null, iArr12, 0, 0, 0, 0, iArr12[0], R.string.accessibility_data_connection_lte_plus, R.drawable.stat_sys_data_fully_connected_lte_plus, true, R.drawable.ic_qs_signal_lte_plus);
        int i = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH[0];
        int[] iArr13 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        FIVE_G_BASIC = new MobileSignalController.MobileIconGroup("5GBasic", (int[][]) null, (int[][]) null, iArr13, 0, 0, 0, 0, iArr13[0], R.string.data_connection_5g_basic, R.drawable.ic_5g_mobiledata, false, R.drawable.ic_5g_mobiledata);
        int[] iArr14 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        FIVE_G_UWB = new MobileSignalController.MobileIconGroup("5GUWB", (int[][]) null, (int[][]) null, iArr14, 0, 0, 0, 0, iArr14[0], R.string.data_connection_5g_uwb, R.drawable.ic_5g_uwb_mobiledata, false, R.drawable.ic_5g_uwb_mobiledata);
        int[] iArr15 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        FIVE_G_SA = new MobileSignalController.MobileIconGroup("5GSA", (int[][]) null, (int[][]) null, iArr15, 0, 0, 0, 0, iArr15[0], R.string.data_connection_5g_sa, R.drawable.ic_5g_mobiledata, false, R.drawable.ic_5g_mobiledata);
        int[] iArr16 = AccessibilityContentDescriptions.PHONE_SIGNAL_STRENGTH;
        DATA_DISABLED = new MobileSignalController.MobileIconGroup("DataDisabled", (int[][]) null, (int[][]) null, iArr16, 0, 0, 0, 0, iArr16[0], R.string.accessibility_cell_data_off, R.drawable.stat_sys_data_disabled, false, R.drawable.ic_data_disabled);
    }

    public static void initDataTypeName(Context context) {
        setDataTypeDefault(context);
        setDataTypeCusomizedRegion(context);
        updateDataTypeMiuiRegion(context, System.getProperty("ro.miui.mcc"));
    }

    public static void setDataTypeDefault(Context context) {
        if (mDataTypeNameDefault == null) {
            mDataTypeNameDefault = context.getResources().getStringArray(R.array.data_network_type_name_default);
        }
    }

    public static void setDataTypeCusomizedRegion(Context context) {
        int[] intArray = context.getResources().getIntArray(R.array.data_type_name_cus_reg_key);
        String[] stringArray = context.getResources().getStringArray(R.array.data_type_name_cus_reg_value);
        for (int i = 0; i < intArray.length; i++) {
            mDataTypeNameCusRegMap.put(intArray[i], stringArray[i]);
        }
    }

    public static void updateDataTypeMcc(Context context, String str, int i) {
        SparseArray sparseArray = mDataTypeNameMcc.get(i);
        sparseArray.clear();
        Resources resourcesForOperation = MCCUtils.getResourcesForOperation(context, str, false);
        int[] intArray = resourcesForOperation.getIntArray(R.array.data_type_name_mcc_key);
        String[] stringArray = resourcesForOperation.getStringArray(R.array.data_type_name_mcc_value);
        for (int i2 = 0; i2 < intArray.length; i2++) {
            sparseArray.put(intArray[i2], stringArray[i2]);
        }
    }

    public static void updateDataTypeMccMnc(Context context, String str, int i) {
        SparseArray sparseArray = mDataTypeNameMccMnc.get(i);
        sparseArray.clear();
        Resources resourcesForOperation = MCCUtils.getResourcesForOperation(context, str, true);
        int[] intArray = resourcesForOperation.getIntArray(R.array.data_type_name_mcc_mnc_key);
        String[] stringArray = resourcesForOperation.getStringArray(R.array.data_type_name_mcc_mnc_value);
        for (int i2 = 0; i2 < intArray.length; i2++) {
            sparseArray.put(intArray[i2], stringArray[i2]);
        }
    }

    public static void updateDataTypeMiuiRegion(Context context, String str) {
        mDataTypeNameMIUIRegion.clear();
        if (!TextUtils.isEmpty(str)) {
            str = str.substring(1, str.length());
        }
        Resources resourcesForOperation = MCCUtils.getResourcesForOperation(context, str, false);
        int[] intArray = resourcesForOperation.getIntArray(R.array.data_type_name_miui_mcc_key);
        String[] stringArray = resourcesForOperation.getStringArray(R.array.data_type_name_miui_mcc_value);
        for (int i = 0; i < intArray.length; i++) {
            mDataTypeNameMIUIRegion.put(intArray[i], stringArray[i]);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x007a, code lost:
        r2 = mDataTypeNameDefault;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.lang.String getNetworkTypeName(int r1, int r2) {
        /*
            android.util.SparseArray<java.lang.String> r0 = mDataTypeNameCusRegMap
            int r0 = r0.size()
            if (r0 <= 0) goto L_0x0019
            android.util.SparseArray<java.lang.String> r2 = mDataTypeNameCusRegMap
            java.lang.Object r2 = r2.get(r1)
            if (r2 == 0) goto L_0x0074
            android.util.SparseArray<java.lang.String> r2 = mDataTypeNameCusRegMap
            java.lang.Object r1 = r2.get(r1)
            java.lang.String r1 = (java.lang.String) r1
            return r1
        L_0x0019:
            android.util.SparseArray<android.util.SparseArray<java.lang.String>> r0 = mDataTypeNameMccMnc
            java.lang.Object r0 = r0.get(r2)
            android.util.SparseArray r0 = (android.util.SparseArray) r0
            int r0 = r0.size()
            if (r0 <= 0) goto L_0x0038
            android.util.SparseArray<android.util.SparseArray<java.lang.String>> r0 = mDataTypeNameMccMnc
            java.lang.Object r2 = r0.get(r2)
            android.util.SparseArray r2 = (android.util.SparseArray) r2
            java.lang.Object r2 = r2.get(r1)
            java.lang.String r2 = (java.lang.String) r2
            if (r2 == 0) goto L_0x0074
            return r2
        L_0x0038:
            android.util.SparseArray<android.util.SparseArray<java.lang.String>> r0 = mDataTypeNameMcc
            java.lang.Object r0 = r0.get(r2)
            android.util.SparseArray r0 = (android.util.SparseArray) r0
            int r0 = r0.size()
            if (r0 <= 0) goto L_0x0057
            android.util.SparseArray<android.util.SparseArray<java.lang.String>> r0 = mDataTypeNameMcc
            java.lang.Object r2 = r0.get(r2)
            android.util.SparseArray r2 = (android.util.SparseArray) r2
            java.lang.Object r2 = r2.get(r1)
            java.lang.String r2 = (java.lang.String) r2
            if (r2 == 0) goto L_0x0074
            return r2
        L_0x0057:
            android.util.SparseArray<java.lang.String> r2 = mDataTypeNameMIUIRegion
            int r2 = r2.size()
            if (r2 <= 0) goto L_0x006a
            android.util.SparseArray<java.lang.String> r2 = mDataTypeNameMIUIRegion
            java.lang.Object r2 = r2.get(r1)
            java.lang.String r2 = (java.lang.String) r2
            if (r2 == 0) goto L_0x0074
            return r2
        L_0x006a:
            boolean r2 = miui.os.Build.IS_CM_CUSTOMIZATION_TEST
            if (r2 == 0) goto L_0x0074
            r2 = 1
            if (r1 != r2) goto L_0x0074
            java.lang.String r1 = "2G"
            return r1
        L_0x0074:
            if (r1 < 0) goto L_0x0084
            r2 = 11
            if (r1 >= r2) goto L_0x0084
            java.lang.String[] r2 = mDataTypeNameDefault
            if (r2 == 0) goto L_0x0084
            int r0 = r2.length
            if (r1 >= r0) goto L_0x0084
            r1 = r2[r1]
            return r1
        L_0x0084:
            java.lang.String r1 = ""
            return r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.policy.TelephonyIcons.getNetworkTypeName(int, int):java.lang.String");
    }

    static void readIconsFromXml(Context context) {
        if (isInitiated) {
            log("TelephonyIcons", "readIconsFromXml, already read!");
            return;
        }
        Resources resources = context.getResources();
        mRes = resources;
        try {
            mDataTypeArray = resources.getStringArray(R.array.multi_data_type);
            mDataTypeDescriptionArray = mRes.getStringArray(R.array.telephony_data_type_description);
            mDataTypeGenerationArray = mRes.getStringArray(R.array.telephony_data_type_generation);
            mDataTypeGenerationDescArray = mRes.getStringArray(R.array.telephony_data_type_generation_description);
            mDataActivityArray = mRes.getStringArray(R.array.multi_data_activity);
            mSignalStrengthArray = mRes.getStringArray(R.array.multi_signal_strength);
            mRes.getStringArray(R.array.multi_signal_strength_roaming);
            mSignalNullArray = mRes.getStringArray(R.array.multi_signal_null);
            mSignalStrengthDesc = mRes.getStringArray(R.array.signal_strength_description);
            initStacked2SingleIconLookup();
            if (mSelectedDataTypeIcon == null) {
                String[] strArr = mDataTypeArray;
                if (strArr.length != 0) {
                    mSelectedDataTypeIcon = new int[strArr.length];
                }
            }
            if (mSelectedQSDataTypeIcon == null) {
                String[] strArr2 = mDataTypeArray;
                if (strArr2.length != 0) {
                    mSelectedQSDataTypeIcon = new int[strArr2.length];
                }
            }
            if (mSelectedDataTypeDesc == null) {
                String[] strArr3 = mDataTypeArray;
                if (strArr3.length != 0) {
                    mSelectedDataTypeDesc = new String[strArr3.length];
                }
            }
            if (mSelectedDataActivityIndex == null) {
                String[] strArr4 = mDataActivityArray;
                if (strArr4.length != 0) {
                    mSelectedDataActivityIndex = new int[strArr4.length];
                }
            }
            if (mSelectedSignalStreagthIndex == null) {
                String[] strArr5 = mSignalStrengthArray;
                if (strArr5.length != 0) {
                    mSelectedSignalStreagthIndex = new int[strArr5.length];
                }
            }
            isInitiated = true;
        } catch (Resources.NotFoundException e) {
            isInitiated = false;
            log("TelephonyIcons", "readIconsFromXml, exception happened: " + e);
        }
    }

    static void initStacked2SingleIconLookup() {
        mStacked2SingleIconLookup = new SparseArray<>();
        TypedArray obtainTypedArray = mRes.obtainTypedArray(R.array.stacked_signal_icons);
        TypedArray obtainTypedArray2 = mRes.obtainTypedArray(R.array.single_signal_icons);
        mStacked2SingleIconLookup.clear();
        int i = 0;
        while (i < obtainTypedArray.length() && i < obtainTypedArray2.length()) {
            mStacked2SingleIconLookup.put(obtainTypedArray.getResourceId(i, 0), Integer.valueOf(obtainTypedArray2.getResourceId(i, 0)));
            i++;
        }
        obtainTypedArray.recycle();
        obtainTypedArray2.recycle();
        log("TelephonyIcons", "initStacked2SingleIconLookup: size=" + mStacked2SingleIconLookup.size());
    }

    static int getSignalNullIcon(int i) {
        String[] strArr = mSignalNullArray;
        if (strArr == null) {
            return 0;
        }
        String str = strArr[i];
        log("TelephonyIcons", "null signal icon name: " + str);
        return mRes.getIdentifier(str, (String) null, "com.android.systemui");
    }

    static int getQSDataTypeIcon(int i) {
        return mSelectedQSDataTypeIcon[i];
    }

    static int getDataTypeIcon(int i) {
        log("TelephonyIcons", "getDataTypeIcon " + String.format("sub=%d", new Object[]{Integer.valueOf(i)}));
        return mSelectedDataTypeIcon[i];
    }

    static int getDataTypeDesc(int i) {
        return mRes.getIdentifier(mSelectedDataTypeDesc[i], (String) null, "com.android.systemui");
    }

    static int getDataActivity(int i, int i2) {
        log("TelephonyIcons", String.format("getDataActivity, slot=%d, activity=%d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)}));
        Resources resources = mRes;
        String[] stringArray = resources.getStringArray(resources.getIdentifier(mDataActivityArray[i], (String) null, "com.android.systemui"));
        Resources resources2 = mRes;
        return mRes.getIdentifier(resources2.getStringArray(resources2.getIdentifier(stringArray[mSelectedDataActivityIndex[i]], (String) null, "com.android.systemui"))[i2], (String) null, "com.android.systemui");
    }

    static int getSignalStrengthIcon(int i, int i2, int i3, boolean z, SignalStrength signalStrength) {
        log("TelephonyIcons", "getSignalStrengthIcon: " + String.format("slot=%d, inetCondition=%d, level=%d, roaming=%b, signalstrength=%s", new Object[]{Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3), Boolean.valueOf(z), signalStrength}));
        return TELEPHONY_SIGNAL_STRENGTH[0][i3];
    }

    static int convertMobileStrengthIcon(int i) {
        SparseArray<Integer> sparseArray = mStacked2SingleIconLookup;
        return (sparseArray != null && sparseArray.indexOfKey(i) >= 0) ? mStacked2SingleIconLookup.get(i).intValue() : i;
    }

    static int getRoamingSignalIconId(int i, int i2) {
        return TELEPHONY_SIGNAL_STRENGTH_ROAMING_R[i2][i];
    }

    static int[] getSignalStrengthDes(int i) {
        int[] iArr = new int[5];
        for (int i2 = 0; i2 < 5; i2++) {
            iArr[i2] = mRes.getIdentifier(mSignalStrengthDesc[i2], (String) null, "com.android.systemui");
        }
        return iArr;
    }

    private static void log(String str, String str2) {
        Log.d(str, str2);
    }
}

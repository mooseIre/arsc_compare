package com.android.keyguard.fod;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import com.android.keyguard.fod.item.AddEventItem;
import com.android.keyguard.fod.item.AlipayPayItem;
import com.android.keyguard.fod.item.AlipayScanItem;
import com.android.keyguard.fod.item.IQuickOpenItem;
import com.android.keyguard.fod.item.QrCodeItem;
import com.android.keyguard.fod.item.SearchItem;
import com.android.keyguard.fod.item.WechatPayItem;
import com.android.keyguard.fod.item.WechatScanItem;
import com.android.keyguard.fod.item.XiaoaiItem;
import com.android.keyguard.utils.MiuiKeyguardUtils;
import com.android.systemui.C0012R$dimen;
import java.util.ArrayList;
import java.util.List;
import miui.os.Build;

/* access modifiers changed from: package-private */
public class MiuiGxzwQuickOpenUtil {
    private static final int[] DEFAULT_ITEM_ID_LIST;
    private static final boolean SUPPORT_QUICK_OPEN = (MiuiKeyguardUtils.isGxzwSensor() && !MiuiGxzwUtils.isLargeFod());
    private static int sShowQuickOpenPressCount = -1;
    private static long sShowQuickOpenSlideTime = -1;
    private static int sShowQuickOpenTeachValue = -1;

    private static float getAngleDetal(int i) {
        return i == 5 ? 45.0f : 60.0f;
    }

    static {
        int[] iArr;
        if (Build.IS_INTERNATIONAL_BUILD) {
            iArr = new int[]{1006, 1007, 1008};
        } else {
            iArr = new int[]{1001, 1002, 1003, 1005, 1004};
        }
        DEFAULT_ITEM_ID_LIST = iArr;
    }

    public static boolean isQuickOpenEnable(Context context) {
        return SUPPORT_QUICK_OPEN && Settings.Secure.getIntForUser(context.getContentResolver(), "fod_quick_open", 1, 0) == 1;
    }

    public static void setFodAuthFingerprint(Context context, int i, int i2) {
        if (SUPPORT_QUICK_OPEN) {
            Settings.Secure.putIntForUser(context.getContentResolver(), "fod_auth_fingerprint", i, i2);
        }
    }

    public static void loadSharedPreferencesValue(final Context context) {
        if (SUPPORT_QUICK_OPEN) {
            final Handler handler = new Handler(Looper.getMainLooper());
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                /* class com.android.keyguard.fod.MiuiGxzwQuickOpenUtil.AnonymousClass1 */

                public void run() {
                    SharedPreferences sharedPreferences = context.getSharedPreferences("quick_open", 0);
                    final int i = sharedPreferences.getInt("sp_fod_show_quick_open_press_count", 0);
                    final long j = sharedPreferences.getLong("sp_fod_show_quick_open_slide_time", 0);
                    final int i2 = sharedPreferences.getInt("sp_fod_show_quick_open_teach", 1);
                    handler.post(new Runnable(this) {
                        /* class com.android.keyguard.fod.MiuiGxzwQuickOpenUtil.AnonymousClass1.AnonymousClass1 */

                        public void run() {
                            int unused = MiuiGxzwQuickOpenUtil.sShowQuickOpenPressCount = i;
                            long unused2 = MiuiGxzwQuickOpenUtil.sShowQuickOpenSlideTime = j;
                            int unused3 = MiuiGxzwQuickOpenUtil.sShowQuickOpenTeachValue = i2;
                        }
                    });
                }
            });
        }
    }

    public static boolean isShowQuickOpenPress(Context context) {
        if (!SUPPORT_QUICK_OPEN) {
            return false;
        }
        if (sShowQuickOpenPressCount == -1) {
            sShowQuickOpenPressCount = context.getSharedPreferences("quick_open", 0).getInt("sp_fod_show_quick_open_press_count", 0);
        }
        if (sShowQuickOpenPressCount >= 5 || !isShowQuickOpenSlide(context)) {
            return false;
        }
        return true;
    }

    public static void increaseShowQuickOpenPressCount(final Context context) {
        if (SUPPORT_QUICK_OPEN) {
            if (sShowQuickOpenPressCount == -1) {
                sShowQuickOpenPressCount = context.getSharedPreferences("quick_open", 0).getInt("sp_fod_show_quick_open_press_count", 0);
            }
            int i = sShowQuickOpenPressCount + 1;
            sShowQuickOpenPressCount = i;
            if (i > 5) {
                sShowQuickOpenPressCount = 5;
            }
            final int i2 = sShowQuickOpenPressCount;
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                /* class com.android.keyguard.fod.MiuiGxzwQuickOpenUtil.AnonymousClass2 */

                public void run() {
                    SharedPreferences.Editor edit = context.getSharedPreferences("quick_open", 0).edit();
                    edit.putInt("sp_fod_show_quick_open_press_count", i2);
                    edit.commit();
                }
            });
        }
    }

    public static boolean isShowQuickOpenSlide(Context context) {
        if (!SUPPORT_QUICK_OPEN) {
            return false;
        }
        if (sShowQuickOpenSlideTime == -1) {
            sShowQuickOpenSlideTime = context.getSharedPreferences("quick_open", 0).getLong("sp_fod_show_quick_open_slide_time", 0);
        }
        if (sShowQuickOpenSlideTime <= 0) {
            return true;
        }
        return false;
    }

    public static void disableShowQuickOpenSlide(final Context context) {
        if (SUPPORT_QUICK_OPEN) {
            if (sShowQuickOpenSlideTime == -1) {
                sShowQuickOpenSlideTime = context.getSharedPreferences("quick_open", 0).getLong("sp_fod_show_quick_open_slide_time", 0);
            }
            if (sShowQuickOpenSlideTime <= 0) {
                final long currentTimeMillis = System.currentTimeMillis();
                sShowQuickOpenSlideTime = currentTimeMillis;
                AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                    /* class com.android.keyguard.fod.MiuiGxzwQuickOpenUtil.AnonymousClass3 */

                    public void run() {
                        SharedPreferences.Editor edit = context.getSharedPreferences("quick_open", 0).edit();
                        edit.putLong("sp_fod_show_quick_open_slide_time", currentTimeMillis);
                        edit.commit();
                    }
                });
            }
        }
    }

    public static boolean isShowQuickOpenTeach(Context context) {
        if (!SUPPORT_QUICK_OPEN) {
            return false;
        }
        if (sShowQuickOpenTeachValue == -1) {
            sShowQuickOpenTeachValue = context.getSharedPreferences("quick_open", 0).getInt("sp_fod_show_quick_open_teach", 1);
        }
        if (sShowQuickOpenTeachValue == 1) {
            return true;
        }
        return false;
    }

    public static void disableShowQuickOpenTeach(final Context context) {
        if (SUPPORT_QUICK_OPEN && sShowQuickOpenTeachValue != 0) {
            sShowQuickOpenTeachValue = 0;
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
                /* class com.android.keyguard.fod.MiuiGxzwQuickOpenUtil.AnonymousClass4 */

                public void run() {
                    SharedPreferences.Editor edit = context.getSharedPreferences("quick_open", 0).edit();
                    edit.putInt("sp_fod_show_quick_open_teach", 0);
                    edit.commit();
                }
            });
        }
    }

    public static float getLargeItemDetal(Context context) {
        int length = DEFAULT_ITEM_ID_LIST.length;
        if (length != 3) {
            return length != 4 ? 0.0f : 5.0f;
        }
        return 10.0f;
    }

    public static List<IQuickOpenItem> generateQuickOpenItemList(Context context, float f, float f2, boolean z) {
        float f3 = f2;
        ArrayList arrayList = new ArrayList();
        if (!SUPPORT_QUICK_OPEN) {
            return arrayList;
        }
        MiuiGxzwUtils.caculateGxzwIconSize(context);
        int i = MiuiGxzwUtils.GXZW_ICON_X + (MiuiGxzwUtils.GXZW_ICON_WIDTH / 2);
        int i2 = MiuiGxzwUtils.GXZW_ICON_Y + (MiuiGxzwUtils.GXZW_ICON_HEIGHT / 2);
        float dimension = context.getResources().getDimension(C0012R$dimen.gxzw_quick_open_region_big);
        float dimension2 = context.getResources().getDimension(C0012R$dimen.gxzw_quick_open_region_samll);
        float f4 = (float) i;
        float f5 = (float) i2;
        RectF rectF = new RectF(f4 - dimension, f5 - dimension, f4 + dimension, dimension + f5);
        RectF rectF2 = new RectF(f4 - dimension2, f5 - dimension2, f4 + dimension2, dimension2 + f5);
        Region region = new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
        List<Integer> validItemIdList = getValidItemIdList(context);
        int size = validItemIdList.size();
        float angleDetal = getAngleDetal(size);
        float f6 = (float) size;
        float f7 = ((180.0f - ((f6 - 1.0f) * angleDetal)) / 2.0f) - 0.024902344f;
        float f8 = ((180.0f - (f6 * angleDetal)) / 2.0f) - 0.024902344f;
        int i3 = 0;
        while (i3 < size) {
            float f9 = ((float) i3) * angleDetal;
            float f10 = f7 + f9;
            float f11 = f9 + f8;
            float circleCoordinateX = getCircleCoordinateX(i, f3, f10);
            float circleCoordinateY = getCircleCoordinateY(i2, f3, f10);
            RectF rectF3 = new RectF(circleCoordinateX - f, circleCoordinateY - f, circleCoordinateX + f, circleCoordinateY + f);
            Path path = new Path();
            Path path2 = new Path();
            path.moveTo(f4, f5);
            path2.moveTo(f4, f5);
            path.arcTo(rectF, f11, angleDetal, false);
            path2.arcTo(rectF2, f11, angleDetal, false);
            path.close();
            path2.close();
            path.op(path2, Path.Op.DIFFERENCE);
            Region region2 = new Region();
            region2.setPath(path, region);
            arrayList = arrayList;
            arrayList.add(generateQuickOpenItem(rectF3, region2, context, validItemIdList.get(z ? (size - i3) - 1 : i3).intValue()));
            i3++;
            f3 = f2;
            i = i;
            i2 = i2;
            f7 = f7;
        }
        return arrayList;
    }

    private static List<Integer> getValidItemIdList(Context context) {
        ArrayList arrayList = new ArrayList();
        int[] iArr = DEFAULT_ITEM_ID_LIST;
        for (int i : iArr) {
            Intent intent = generateQuickOpenItem(new RectF(), new Region(), context, i).getIntent();
            String str = intent.getPackage();
            if (str == null && intent.getComponent() != null) {
                str = intent.getComponent().getPackageName();
            }
            if (str == null || !str.startsWith("com.android") || MiuiKeyguardUtils.isIntentActivityExist(context, intent)) {
                arrayList.add(Integer.valueOf(i));
            }
        }
        return arrayList;
    }

    public static float getTeachViewRotation(int i) {
        if (i % 2 != 0) {
            return 0.0f;
        }
        float angleDetal = getAngleDetal(i);
        return (((180.0f - ((((float) i) - 1.0f) * angleDetal)) / 2.0f) - 0.024902344f) + (angleDetal * ((float) ((i / 2) - 1))) + 90.0f;
    }

    private static float getCircleCoordinateX(int i, float f, float f2) {
        return ((float) i) + ((float) (((double) f) * Math.cos((((double) f2) * 3.14d) / 180.0d)));
    }

    private static float getCircleCoordinateY(int i, float f, float f2) {
        return ((float) i) + ((float) (((double) f) * Math.sin((((double) f2) * 3.14d) / 180.0d)));
    }

    private static IQuickOpenItem generateQuickOpenItem(RectF rectF, Region region, Context context, int i) {
        switch (i) {
            case 1001:
                return new WechatPayItem(rectF, region, context);
            case 1002:
                return new WechatScanItem(rectF, region, context);
            case 1003:
                return new XiaoaiItem(rectF, region, context);
            case 1004:
                return new AlipayPayItem(rectF, region, context);
            case 1005:
                return new AlipayScanItem(rectF, region, context);
            case 1006:
                return new QrCodeItem(rectF, region, context);
            case 1007:
                return new SearchItem(rectF, region, context);
            case 1008:
                return new AddEventItem(rectF, region, context);
            default:
                throw new UnsupportedOperationException();
        }
    }
}

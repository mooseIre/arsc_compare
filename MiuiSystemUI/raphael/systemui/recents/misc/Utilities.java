package com.android.systemui.recents.misc;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.RectEvaluator;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.IntProperty;
import android.util.Property;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import com.android.systemui.recents.model.Task;
import com.android.systemui.recents.views.TaskViewTransform;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import miui.os.Build;
import miui.securityspace.CrossUserUtils;

public class Utilities {
    public static final Property<Drawable, Integer> DRAWABLE_ALPHA = new IntProperty<Drawable>("drawableAlpha") {
        public void setValue(Drawable drawable, int i) {
            drawable.setAlpha(i);
        }

        public Integer get(Drawable drawable) {
            return Integer.valueOf(drawable.getAlpha());
        }
    };
    public static final Property<Drawable, Rect> DRAWABLE_RECT = new Property<Drawable, Rect>(Rect.class, "drawableBounds") {
        public void set(Drawable drawable, Rect rect) {
            drawable.setBounds(rect);
        }

        public Rect get(Drawable drawable) {
            return drawable.getBounds();
        }
    };
    public static final Rect EMPTY_RECT = new Rect();
    public static final boolean IS_MIUI_LITE_VERSION = Build.IS_MIUI_LITE_VERSION;
    public static final boolean IS_NOT_SUPPORT_GESTURE_V3_DEVICE = isNotSupportGestureV3Device();
    public static Set<String> LOW_MEMORY_DEVICES = new ArraySet();
    public static final RectFEvaluator RECTF_EVALUATOR = new RectFEvaluator();
    public static final RectEvaluator RECT_EVALUATOR = new RectEvaluator(new Rect());

    public static float mapRange(float f, float f2, float f3) {
        return f2 + (f * (f3 - f2));
    }

    static {
        LOW_MEMORY_DEVICES.add("pine");
        LOW_MEMORY_DEVICES.add("olive");
        LOW_MEMORY_DEVICES.add("olivelite");
        LOW_MEMORY_DEVICES.add("olivewood");
    }

    private static final boolean isNotSupportGestureV3Device() {
        try {
            return ((Boolean) Class.forName("android.util.MiuiGestureUtils").getMethod("isNotSupportGestureV3Device", new Class[0]).invoke((Object) null, new Object[0])).booleanValue();
        } catch (Exception unused) {
            return false;
        }
    }

    public static boolean isLowMemoryDevice() {
        return LOW_MEMORY_DEVICES.contains(Build.DEVICE) || IS_MIUI_LITE_VERSION || IS_NOT_SUPPORT_GESTURE_V3_DEVICE;
    }

    public static <T> ArraySet<T> arrayToSet(T[] tArr, ArraySet<T> arraySet) {
        arraySet.clear();
        if (tArr != null) {
            Collections.addAll(arraySet, tArr);
        }
        return arraySet;
    }

    public static float clamp(float f, float f2, float f3) {
        return Math.max(f2, Math.min(f3, f));
    }

    public static int clamp(int i, int i2, int i3) {
        return Math.max(i2, Math.min(i3, i));
    }

    public static float clamp01(float f) {
        return Math.max(0.0f, Math.min(1.0f, f));
    }

    public static void scaleRectAboutCenter(RectF rectF, float f) {
        if (f != 1.0f) {
            float centerX = rectF.centerX();
            float centerY = rectF.centerY();
            rectF.offset(-centerX, -centerY);
            rectF.left *= f;
            rectF.top *= f;
            rectF.right *= f;
            rectF.bottom *= f;
            rectF.offset(centerX, centerY);
        }
    }

    public static float computeContrastBetweenColors(int i, int i2) {
        float f;
        float f2;
        float f3;
        float f4;
        float f5;
        float f6;
        float red = ((float) Color.red(i)) / 255.0f;
        float green = ((float) Color.green(i)) / 255.0f;
        float blue = ((float) Color.blue(i)) / 255.0f;
        if (red < 0.03928f) {
            f = red / 12.92f;
        } else {
            f = (float) Math.pow((double) ((red + 0.055f) / 1.055f), 2.4000000953674316d);
        }
        if (green < 0.03928f) {
            f2 = green / 12.92f;
        } else {
            f2 = (float) Math.pow((double) ((green + 0.055f) / 1.055f), 2.4000000953674316d);
        }
        if (blue < 0.03928f) {
            f3 = blue / 12.92f;
        } else {
            f3 = (float) Math.pow((double) ((blue + 0.055f) / 1.055f), 2.4000000953674316d);
        }
        float f7 = (f * 0.2126f) + (f2 * 0.7152f) + (f3 * 0.0722f);
        float red2 = ((float) Color.red(i2)) / 255.0f;
        float green2 = ((float) Color.green(i2)) / 255.0f;
        float blue2 = ((float) Color.blue(i2)) / 255.0f;
        if (red2 < 0.03928f) {
            f4 = red2 / 12.92f;
        } else {
            f4 = (float) Math.pow((double) ((red2 + 0.055f) / 1.055f), 2.4000000953674316d);
        }
        if (green2 < 0.03928f) {
            f5 = green2 / 12.92f;
        } else {
            f5 = (float) Math.pow((double) ((green2 + 0.055f) / 1.055f), 2.4000000953674316d);
        }
        if (blue2 < 0.03928f) {
            f6 = blue2 / 12.92f;
        } else {
            f6 = (float) Math.pow((double) ((blue2 + 0.055f) / 1.055f), 2.4000000953674316d);
        }
        return Math.abs(((((f4 * 0.2126f) + (f5 * 0.7152f)) + (f6 * 0.0722f)) + 0.05f) / (f7 + 0.05f));
    }

    public static int getColorWithOverlay(int i, int i2, float f) {
        float f2 = 1.0f - f;
        return Color.rgb((int) ((((float) Color.red(i)) * f) + (((float) Color.red(i2)) * f2)), (int) ((((float) Color.green(i)) * f) + (((float) Color.green(i2)) * f2)), (int) ((f * ((float) Color.blue(i))) + (f2 * ((float) Color.blue(i2)))));
    }

    public static void cancelAnimationWithoutCallbacks(Animator animator) {
        if (animator != null && animator.isStarted()) {
            removeAnimationListenersRecursive(animator);
            animator.cancel();
        }
    }

    public static void removeAnimationListenersRecursive(Animator animator) {
        if (animator instanceof AnimatorSet) {
            ArrayList<Animator> childAnimations = ((AnimatorSet) animator).getChildAnimations();
            for (int size = childAnimations.size() - 1; size >= 0; size--) {
                removeAnimationListenersRecursive(childAnimations.get(size));
            }
        }
        animator.removeAllListeners();
    }

    public static void setViewFrameFromTranslation(View view) {
        RectF rectF = new RectF((float) view.getLeft(), (float) view.getTop(), (float) view.getRight(), (float) view.getBottom());
        rectF.offset(view.getTranslationX(), view.getTranslationY());
        view.setTranslationX(0.0f);
        view.setTranslationY(0.0f);
        view.setLeftTopRightBottom((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom);
    }

    public static ViewStub findViewStubById(View view, int i) {
        return (ViewStub) view.findViewById(i);
    }

    public static ViewStub findViewStubById(Activity activity, int i) {
        return (ViewStub) activity.findViewById(i);
    }

    public static void matchTaskListSize(List<Task> list, List<TaskViewTransform> list2) {
        int size = list2.size();
        int size2 = list.size();
        if (size < size2) {
            while (size < size2) {
                list2.add(new TaskViewTransform());
                size++;
            }
        } else if (size > size2) {
            list2.subList(size2, size).clear();
        }
    }

    public static float dpToPx(Resources resources, float f) {
        return TypedValue.applyDimension(1, f, resources.getDisplayMetrics());
    }

    public static boolean isDescendentAccessibilityFocused(View view) {
        if (view.isAccessibilityFocused()) {
            return true;
        }
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                if (isDescendentAccessibilityFocused(viewGroup.getChildAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Configuration getAppConfiguration(Context context) {
        return context.getApplicationContext().getResources().getConfiguration();
    }

    public static String dumpRect(Rect rect) {
        if (rect == null) {
            return "N:0,0-0,0";
        }
        return rect.left + "," + rect.top + "-" + rect.right + "," + rect.bottom;
    }

    public static boolean isAndroidNorNewer() {
        return Build.VERSION.SDK_INT >= 24;
    }

    public static boolean isAndroidPorNewer() {
        return Build.VERSION.SDK_INT >= 28;
    }

    public static boolean isAndroidQorNewer() {
        return Build.VERSION.SDK_INT >= 29;
    }

    public static boolean isAndroidRorNewer() {
        return Build.VERSION.SDK_INT >= 30;
    }

    public static boolean supportsMultiWindow() {
        return CrossUserUtils.getCurrentUserId() == 0;
    }

    public static boolean isInSmallWindowMode(Context context) {
        return Settings.Secure.getInt(context.getContentResolver(), "freeform_window_state", -1) != -1;
    }

    public static boolean isSlideCoverDevice() {
        return "perseus".equals(miui.os.Build.DEVICE);
    }

    public static boolean isPackageEnabled(Context context, String str) {
        if (context == null && TextUtils.isEmpty(str)) {
            return false;
        }
        try {
            return context.getPackageManager().getApplicationInfo(str, 0).enabled;
        } catch (PackageManager.NameNotFoundException unused) {
            return false;
        }
    }

    public static boolean isShowRecentsRecommend() {
        return miui.os.Build.IS_INTERNATIONAL_BUILD;
    }

    public static String convertSetToString(HashSet<String> hashSet) {
        if (hashSet == null || hashSet.size() == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = hashSet.iterator();
        while (it.hasNext()) {
            String next = it.next();
            if (!TextUtils.isEmpty(next)) {
                sb.append(next + ",");
            }
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static HashSet<String> convertStringToSet(String str) {
        if (TextUtils.isEmpty(str)) {
            return new HashSet<>();
        }
        String[] split = str.split(",");
        HashSet<String> hashSet = new HashSet<>();
        for (String str2 : split) {
            if (!hashSet.contains(str2)) {
                hashSet.add(str2);
            }
        }
        return hashSet;
    }
}

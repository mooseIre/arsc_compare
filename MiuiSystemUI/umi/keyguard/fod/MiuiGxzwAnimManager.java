package com.android.keyguard.fod;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.fod.MiuiGxzwFrameAnimation;
import com.android.systemui.C0013R$drawable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/* access modifiers changed from: package-private */
public class MiuiGxzwAnimManager {
    private final Map<Integer, MiuiGxzwAnimItem> mAnimItemMap;
    private boolean mBouncer;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        /* class com.android.keyguard.fod.MiuiGxzwAnimManager.AnonymousClass1 */

        public void onChange(boolean z) {
            int defaultAnimType = MiuiGxzwAnimManager.getDefaultAnimType();
            Set legalAnimTypeSet = MiuiGxzwAnimManager.this.getLegalAnimTypeSet();
            int intForUser = Settings.System.getIntForUser(MiuiGxzwAnimManager.this.mContext.getContentResolver(), "fod_animation_type", defaultAnimType, 0);
            if (legalAnimTypeSet.contains(Integer.valueOf(intForUser))) {
                defaultAnimType = intForUser;
            }
            MiuiGxzwAnimManager.this.mGxzwAnimType = defaultAnimType;
        }
    };
    private Context mContext;
    private boolean mEnrolling;
    private int mGxzwAnimType = 6;
    private boolean mKeyguardAuthen;
    private boolean mLightIcon = false;
    private boolean mLightWallpaperGxzw;
    private MiuiGxzwFrameAnimation mMiuiGxzwFrameAnimation;
    private int mTranslateX;
    private int mTranslateY;

    public static int getDefaultAnimType() {
        return 6;
    }

    public MiuiGxzwAnimManager(Context context, MiuiGxzwFrameAnimation miuiGxzwFrameAnimation) {
        this.mContext = context;
        this.mMiuiGxzwFrameAnimation = miuiGxzwFrameAnimation;
        this.mAnimItemMap = new HashMap();
        initAnimItemMap();
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("fod_animation_type"), false, this.mContentObserver, 0);
        this.mContentObserver.onChange(false);
    }

    public MiuiGxzwAnimArgs getIconAnimArgs(boolean z) {
        MiuiGxzwAnimItem miuiGxzwAnimItem = this.mAnimItemMap.get(Integer.valueOf(this.mGxzwAnimType));
        if (miuiGxzwAnimItem == null) {
            return new MiuiGxzwAnimArgs.Builder(null).build();
        }
        MiuiGxzwAnimRes iconAnimRes = miuiGxzwAnimItem.getIconAnimRes(z, isLightResource() && this.mKeyguardAuthen);
        MiuiGxzwAnimArgs.Builder builder = new MiuiGxzwAnimArgs.Builder(iconAnimRes.getAnimRes(this.mContext));
        builder.setRepeat(iconAnimRes.mRepeat);
        builder.setFrameInterval(iconAnimRes.mFrameInterval);
        builder.setAod(z);
        return builder.build();
    }

    public MiuiGxzwAnimArgs getRecognizingAnimArgs(boolean z) {
        MiuiGxzwAnimItem miuiGxzwAnimItem = this.mAnimItemMap.get(Integer.valueOf(this.mGxzwAnimType));
        if (miuiGxzwAnimItem == null) {
            return new MiuiGxzwAnimArgs.Builder(null).build();
        }
        MiuiGxzwAnimRes recognizingAnimRes = miuiGxzwAnimItem.getRecognizingAnimRes(z, isLightResource() && this.mKeyguardAuthen);
        MiuiGxzwAnimArgs.Builder builder = new MiuiGxzwAnimArgs.Builder(recognizingAnimRes.getAnimRes(this.mContext));
        builder.setRepeat(recognizingAnimRes.mRepeat);
        builder.setFrameInterval(recognizingAnimRes.mFrameInterval);
        builder.setFeetback(true);
        builder.setAod(z);
        builder.setTranslate(this.mTranslateX, this.mTranslateY);
        if (miuiGxzwAnimItem.isShowIconWhenRecognizingStart()) {
            builder.setBackgroundRes(getFingerIconResource(z));
            builder.setBackgroundFrame(6);
        }
        return builder.build();
    }

    public MiuiGxzwAnimArgs getFalseAnimArgs(boolean z) {
        MiuiGxzwAnimItem miuiGxzwAnimItem = this.mAnimItemMap.get(Integer.valueOf(this.mGxzwAnimType));
        if (miuiGxzwAnimItem == null) {
            return new MiuiGxzwAnimArgs.Builder(null).build();
        }
        boolean z2 = false;
        MiuiGxzwAnimRes falseAnimRes = miuiGxzwAnimItem.getFalseAnimRes(z, isLightResource() && this.mKeyguardAuthen);
        boolean z3 = falseAnimRes.mRepeat;
        int[] animRes = falseAnimRes.getAnimRes(this.mContext);
        if (miuiGxzwAnimItem.isDismissRecognizingWhenFalse()) {
            int currentPosition = (this.mMiuiGxzwFrameAnimation.getCurrentPosition() + 1) % animRes.length;
            int length = animRes.length - currentPosition;
            int[] iArr = new int[(length + 1)];
            for (int i = 0; i < length; i++) {
                iArr[i] = animRes[(currentPosition + i) % animRes.length];
            }
            iArr[length] = getFingerIconResource(z);
            animRes = iArr;
        } else {
            z2 = z3;
        }
        MiuiGxzwAnimArgs.Builder builder = new MiuiGxzwAnimArgs.Builder(animRes);
        builder.setRepeat(z2);
        builder.setFrameInterval(falseAnimRes.mFrameInterval);
        builder.setAod(z);
        builder.setTranslate(this.mTranslateX, this.mTranslateY);
        return builder.build();
    }

    public MiuiGxzwAnimArgs getBackAnimArgs(boolean z) {
        MiuiGxzwAnimItem miuiGxzwAnimItem = this.mAnimItemMap.get(Integer.valueOf(this.mGxzwAnimType));
        if (miuiGxzwAnimItem == null) {
            return new MiuiGxzwAnimArgs.Builder(null).build();
        }
        MiuiGxzwAnimRes backAnimRes = miuiGxzwAnimItem.getBackAnimRes(z, isLightResource() && this.mKeyguardAuthen);
        MiuiGxzwAnimArgs.Builder builder = new MiuiGxzwAnimArgs.Builder(backAnimRes.getAnimRes(this.mContext));
        builder.setRepeat(backAnimRes.mRepeat);
        builder.setFrameInterval(backAnimRes.mFrameInterval);
        builder.setAod(z);
        return builder.build();
    }

    public int getFodMotionRtpId() {
        return this.mAnimItemMap.get(Integer.valueOf(this.mGxzwAnimType)).getFodMotionRtpId();
    }

    public int getFingerIconResource(boolean z) {
        Log.i("MiuiGxzwAnimManager", "getFingerIconResource: mKeyguardAuthen = " + this.mKeyguardAuthen + ", mLightWallpaperGxzw = " + this.mLightWallpaperGxzw + ", mEnrolling = " + this.mEnrolling + ", mLightIcon = " + this.mLightIcon);
        if (this.mKeyguardAuthen) {
            if (z) {
                return C0013R$drawable.finger_image_aod;
            }
            if (isLightResource()) {
                return C0013R$drawable.finger_image_light;
            }
            return C0013R$drawable.finger_image_normal;
        } else if (this.mEnrolling) {
            return C0013R$drawable.finger_image_normal;
        } else {
            return this.mLightIcon ? C0013R$drawable.finger_image_normal : C0013R$drawable.finger_image_grey;
        }
    }

    public void setTranslate(int i, int i2) {
        this.mTranslateX = i;
        this.mTranslateY = i2;
    }

    private boolean isLightResource() {
        return this.mLightWallpaperGxzw && !this.mBouncer;
    }

    public int getFalseTipTranslationY(Context context) {
        return this.mAnimItemMap.get(Integer.valueOf(this.mGxzwAnimType)).getFalseTipTranslationY(context);
    }

    public void setBouncer(boolean z) {
        this.mBouncer = z;
    }

    public void onKeyguardAuthen(boolean z) {
        this.mKeyguardAuthen = z;
    }

    public void setEnrolling(boolean z) {
        this.mEnrolling = z;
    }

    public void setLightWallpaperGxzw(boolean z) {
        this.mLightWallpaperGxzw = z;
    }

    public void setLightIcon(boolean z) {
        this.mLightIcon = z;
    }

    public static class MiuiGxzwAnimArgs {
        final boolean aod;
        final int backgroundFrame;
        final int backgroundRes;
        final MiuiGxzwFrameAnimation.CustomerDrawBitmap customerDrawBitmap;
        final boolean feedback;
        final int frameInterval;
        final boolean repeat;
        final int[] res;
        final int startPosition;
        final int translateX;
        final int translateY;

        private MiuiGxzwAnimArgs(int[] iArr, int i, boolean z, int i2, int i3, int i4, MiuiGxzwFrameAnimation.CustomerDrawBitmap customerDrawBitmap2, boolean z2, boolean z3, int i5, int i6) {
            this.res = iArr;
            this.startPosition = i;
            this.repeat = z;
            this.frameInterval = i2;
            this.backgroundRes = i3;
            this.backgroundFrame = i4;
            this.customerDrawBitmap = customerDrawBitmap2;
            this.feedback = z2;
            this.aod = z3;
            this.translateX = i5;
            this.translateY = i6;
        }

        /* access modifiers changed from: private */
        public static class Builder {
            private boolean aod;
            private int backgroundFrame;
            private int backgroundRes;
            private MiuiGxzwFrameAnimation.CustomerDrawBitmap customerDrawBitmap;
            private boolean feedback;
            private int frameInterval;
            private boolean repeat;
            private int[] res;
            private int startPosition;
            private int translateX;
            private int translateY;

            private Builder(int[] iArr) {
                this.repeat = false;
                this.frameInterval = 30;
                this.backgroundRes = 0;
                this.backgroundFrame = 0;
                this.feedback = false;
                this.aod = false;
                this.translateX = 0;
                this.translateY = 0;
                this.res = iArr;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private Builder setRepeat(boolean z) {
                this.repeat = z;
                return this;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private Builder setFrameInterval(int i) {
                this.frameInterval = i;
                return this;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private Builder setBackgroundRes(int i) {
                this.backgroundRes = i;
                return this;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private Builder setBackgroundFrame(int i) {
                this.backgroundFrame = i;
                return this;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private Builder setFeetback(boolean z) {
                this.feedback = z;
                return this;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private Builder setAod(boolean z) {
                this.aod = z;
                return this;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private Builder setTranslate(int i, int i2) {
                this.translateX = i;
                this.translateY = i2;
                return this;
            }

            /* access modifiers changed from: private */
            /* access modifiers changed from: public */
            private MiuiGxzwAnimArgs build() {
                return new MiuiGxzwAnimArgs(this.res, this.startPosition, this.repeat, this.frameInterval, this.backgroundRes, this.backgroundFrame, this.customerDrawBitmap, this.feedback, this.aod, this.translateX, this.translateY);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Set<Integer> getLegalAnimTypeSet() {
        return this.mAnimItemMap.keySet();
    }

    private void initAnimItemMap() {
        this.mAnimItemMap.put(6, new MiuiGxzwAnimItemLight());
        this.mAnimItemMap.put(7, new MiuiGxzwAnimItemStar());
        this.mAnimItemMap.put(8, new MiuiGxzwAinmItemAurora());
        this.mAnimItemMap.put(9, new MiuiGxzwAnimItemPulse());
    }
}

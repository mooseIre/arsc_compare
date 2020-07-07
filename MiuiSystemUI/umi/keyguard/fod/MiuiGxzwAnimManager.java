package com.android.keyguard.fod;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import com.android.keyguard.fod.MiuiGxzwFrameAnimation;
import com.android.systemui.plugins.R;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class MiuiGxzwAnimManager {
    private final Map<Integer, MiuiGxzwAnimItem> mAnimItemMap;
    private boolean mBouncer;
    private ContentObserver mContentObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean z) {
            int access$000 = MiuiGxzwAnimManager.this.getDefaultAnimType();
            Set access$100 = MiuiGxzwAnimManager.this.getLegalAnimTypeSet();
            int intForUser = Settings.System.getIntForUser(MiuiGxzwAnimManager.this.mContext.getContentResolver(), "fod_animation_type", access$000, 0);
            if (access$100.contains(Integer.valueOf(intForUser))) {
                access$000 = intForUser;
            }
            int unused = MiuiGxzwAnimManager.this.mGxzwAnimType = access$000;
        }
    };
    /* access modifiers changed from: private */
    public Context mContext;
    private boolean mEnrolling;
    /* access modifiers changed from: private */
    public int mGxzwAnimType = 1;
    private boolean mKeyguardAuthen;
    private boolean mLightIcon = false;
    private boolean mLightWallpaperGxzw;
    private MiuiGxzwFrameAnimation mMiuiGxzwFrameAnimation;
    private final boolean mSupportAurora;
    private int mTranslateX;
    private int mTranslateY;

    public MiuiGxzwAnimManager(Context context, MiuiGxzwFrameAnimation miuiGxzwFrameAnimation) {
        this.mContext = context;
        this.mMiuiGxzwFrameAnimation = miuiGxzwFrameAnimation;
        this.mSupportAurora = MiuiGxzwAinmItemAurora.supportAuroraAnim(context);
        this.mAnimItemMap = new HashMap();
        initAnimItemMap();
        this.mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor("fod_animation_type"), false, this.mContentObserver, 0);
        this.mContentObserver.onChange(false);
    }

    public MiuiGxzwAnimArgs getIconAnimArgs(boolean z) {
        MiuiGxzwAnimItem miuiGxzwAnimItem = this.mAnimItemMap.get(Integer.valueOf(this.mGxzwAnimType));
        if (miuiGxzwAnimItem == null) {
            return new MiuiGxzwAnimArgs.Builder((int[]) null).build();
        }
        MiuiGxzwAnimRes iconAnimRes = miuiGxzwAnimItem.getIconAnimRes(z, isLightResource() && this.mKeyguardAuthen);
        MiuiGxzwAnimArgs.Builder builder = new MiuiGxzwAnimArgs.Builder(iconAnimRes.getAnimRes(this.mContext));
        MiuiGxzwAnimArgs.Builder unused = builder.setRepeat(iconAnimRes.mRepeat);
        MiuiGxzwAnimArgs.Builder unused2 = builder.setFrameInterval(iconAnimRes.mFrameInterval);
        MiuiGxzwAnimArgs.Builder unused3 = builder.setAod(z);
        return builder.build();
    }

    public MiuiGxzwAnimArgs getRecognizingAnimArgs(boolean z) {
        MiuiGxzwAnimItem miuiGxzwAnimItem = this.mAnimItemMap.get(Integer.valueOf(this.mGxzwAnimType));
        if (miuiGxzwAnimItem == null) {
            return new MiuiGxzwAnimArgs.Builder((int[]) null).build();
        }
        MiuiGxzwAnimRes recognizingAnimRes = miuiGxzwAnimItem.getRecognizingAnimRes(z, isLightResource() && this.mKeyguardAuthen);
        MiuiGxzwAnimArgs.Builder builder = new MiuiGxzwAnimArgs.Builder(recognizingAnimRes.getAnimRes(this.mContext));
        MiuiGxzwAnimArgs.Builder unused = builder.setRepeat(recognizingAnimRes.mRepeat);
        MiuiGxzwAnimArgs.Builder unused2 = builder.setFrameInterval(recognizingAnimRes.mFrameInterval);
        MiuiGxzwAnimArgs.Builder unused3 = builder.setFeetback(true);
        MiuiGxzwAnimArgs.Builder unused4 = builder.setAod(z);
        MiuiGxzwAnimArgs.Builder unused5 = builder.setTranslate(this.mTranslateX, this.mTranslateY);
        if (miuiGxzwAnimItem.isShowIconWhenRecognizingStart()) {
            MiuiGxzwAnimArgs.Builder unused6 = builder.setBackgroundRes(getFingerIconResource(z));
            MiuiGxzwAnimArgs.Builder unused7 = builder.setBackgroundFrame(6);
        }
        return builder.build();
    }

    public MiuiGxzwAnimArgs getFalseAnimArgs(boolean z) {
        MiuiGxzwAnimItem miuiGxzwAnimItem = this.mAnimItemMap.get(Integer.valueOf(this.mGxzwAnimType));
        if (miuiGxzwAnimItem == null) {
            return new MiuiGxzwAnimArgs.Builder((int[]) null).build();
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
        MiuiGxzwAnimArgs.Builder unused = builder.setRepeat(z2);
        MiuiGxzwAnimArgs.Builder unused2 = builder.setFrameInterval(falseAnimRes.mFrameInterval);
        MiuiGxzwAnimArgs.Builder unused3 = builder.setAod(z);
        MiuiGxzwAnimArgs.Builder unused4 = builder.setTranslate(this.mTranslateX, this.mTranslateY);
        return builder.build();
    }

    public MiuiGxzwAnimArgs getBackAnimArgs(boolean z) {
        MiuiGxzwAnimItem miuiGxzwAnimItem = this.mAnimItemMap.get(Integer.valueOf(this.mGxzwAnimType));
        if (miuiGxzwAnimItem == null) {
            return new MiuiGxzwAnimArgs.Builder((int[]) null).build();
        }
        MiuiGxzwAnimRes backAnimRes = miuiGxzwAnimItem.getBackAnimRes(z, isLightResource() && this.mKeyguardAuthen);
        MiuiGxzwAnimArgs.Builder builder = new MiuiGxzwAnimArgs.Builder(backAnimRes.getAnimRes(this.mContext));
        MiuiGxzwAnimArgs.Builder unused = builder.setRepeat(backAnimRes.mRepeat);
        MiuiGxzwAnimArgs.Builder unused2 = builder.setFrameInterval(backAnimRes.mFrameInterval);
        MiuiGxzwAnimArgs.Builder unused3 = builder.setAod(z);
        return builder.build();
    }

    public int getFingerIconResource(boolean z) {
        Log.i("MiuiGxzwAnimManager", "getFingerIconResource: mKeyguardAuthen = " + this.mKeyguardAuthen + ", mLightWallpaperGxzw = " + this.mLightWallpaperGxzw + ", mEnrolling = " + this.mEnrolling + ", mLightIcon = " + this.mLightIcon);
        if (MiuiGxzwUtils.isLargeFod()) {
            return this.mContext.getResources().getIdentifier("gxzw_scan_anim_1", "drawable", this.mContext.getPackageName());
        } else if (this.mKeyguardAuthen) {
            if (z) {
                return R.drawable.finger_image_aod;
            }
            if (isLightResource()) {
                return R.drawable.finger_image_light;
            }
            return R.drawable.finger_image_normal;
        } else if (!this.mEnrolling && !this.mLightIcon) {
            return R.drawable.finger_image_grey;
        } else {
            return R.drawable.finger_image_normal;
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

        private static class Builder {
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
            public Builder setRepeat(boolean z) {
                this.repeat = z;
                return this;
            }

            /* access modifiers changed from: private */
            public Builder setFrameInterval(int i) {
                this.frameInterval = i;
                return this;
            }

            /* access modifiers changed from: private */
            public Builder setBackgroundRes(int i) {
                this.backgroundRes = i;
                return this;
            }

            /* access modifiers changed from: private */
            public Builder setBackgroundFrame(int i) {
                this.backgroundFrame = i;
                return this;
            }

            /* access modifiers changed from: private */
            public Builder setFeetback(boolean z) {
                this.feedback = z;
                return this;
            }

            /* access modifiers changed from: private */
            public Builder setAod(boolean z) {
                this.aod = z;
                return this;
            }

            /* access modifiers changed from: private */
            public Builder setTranslate(int i, int i2) {
                this.translateX = i;
                this.translateY = i2;
                return this;
            }

            /* access modifiers changed from: private */
            public MiuiGxzwAnimArgs build() {
                return new MiuiGxzwAnimArgs(this.res, this.startPosition, this.repeat, this.frameInterval, this.backgroundRes, this.backgroundFrame, this.customerDrawBitmap, this.feedback, this.aod, this.translateX, this.translateY);
            }
        }
    }

    /* access modifiers changed from: private */
    public Set<Integer> getLegalAnimTypeSet() {
        return this.mAnimItemMap.keySet();
    }

    /* access modifiers changed from: private */
    public int getDefaultAnimType() {
        if (MiuiGxzwUtils.isLargeFod()) {
            return 4;
        }
        if (MiuiGxzwUtils.isSpecialCepheus()) {
            return 3;
        }
        return this.mSupportAurora ? 5 : 0;
    }

    private void initAnimItemMap() {
        if (MiuiGxzwUtils.isLargeFod()) {
            this.mAnimItemMap.put(4, new MiuiGxzwAnimItemCircle());
        } else if (this.mSupportAurora) {
            this.mAnimItemMap.put(5, new MiuiGxzwAinmItemAurora());
            this.mAnimItemMap.put(0, new MiuiGxzwAnimItemStar());
            this.mAnimItemMap.put(1, new MiuiGxzwAnimItemLight());
            this.mAnimItemMap.put(3, new MiuiGxzwAnimItemPulse());
        } else {
            this.mAnimItemMap.put(0, new MiuiGxzwAnimItemStar());
            this.mAnimItemMap.put(1, new MiuiGxzwAnimItemLight());
            this.mAnimItemMap.put(2, new MiuiGxzwAnimItemRhythm());
            this.mAnimItemMap.put(3, new MiuiGxzwAnimItemPulse());
        }
    }
}

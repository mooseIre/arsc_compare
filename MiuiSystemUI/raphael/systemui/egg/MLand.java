package com.android.systemui.egg;

import android.animation.LayoutTransition;
import android.animation.TimeAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.plugins.R;
import java.util.ArrayList;
import java.util.Iterator;

public class MLand extends FrameLayout {
    static final int[] ANTENNAE = {R.drawable.mm_antennae, R.drawable.mm_antennae2};
    static final int[] CACTI = {R.drawable.cactus1, R.drawable.cactus2, R.drawable.cactus3};
    public static final boolean DEBUG = Log.isLoggable("MLand", 3);
    public static final boolean DEBUG_IDDQD = Log.isLoggable("MLand.iddqd", 3);
    static final int[] EYES = {R.drawable.mm_eyes, R.drawable.mm_eyes2};
    static final int[] MOUNTAINS = {R.drawable.mountain1, R.drawable.mountain2, R.drawable.mountain3};
    static final int[] MOUTHS = {R.drawable.mm_mouth1, R.drawable.mm_mouth2, R.drawable.mm_mouth3, R.drawable.mm_mouth4};
    /* access modifiers changed from: private */
    public static Params PARAMS;
    private static final int[][] SKIES = {new int[]{-4144897, -6250241}, new int[]{-16777200, -16777216}, new int[]{-16777152, -16777200}, new int[]{-6258656, -14663552}};
    private static float dp = 1.0f;
    static final float[] hsv = {0.0f, 0.0f, 0.0f};
    static final Rect sTmpRect = new Rect();
    private float dt;
    private TimeAnimator mAnim;
    private boolean mAnimating;
    private final AudioAttributes mAudioAttrs;
    private AudioManager mAudioManager;
    /* access modifiers changed from: private */
    public int mCountdown;
    /* access modifiers changed from: private */
    public int mCurrentPipeId;
    private boolean mFlipped;
    /* access modifiers changed from: private */
    public boolean mFrozen;
    private ArrayList<Integer> mGameControllers;
    /* access modifiers changed from: private */
    public int mHeight;
    private float mLastPipeTime;
    private ArrayList<Obstacle> mObstaclesInPlay;
    private Paint mPlayerTracePaint;
    private ArrayList<Player> mPlayers;
    private boolean mPlaying;
    private int mScene;
    private ViewGroup mScoreFields;
    /* access modifiers changed from: private */
    public View mSplash;
    private int mTaps;
    private int mTimeOfDay;
    private Paint mTouchPaint;
    private Vibrator mVibrator;
    private int mWidth;
    private float t;

    private interface GameView {
        void step(long j, long j2, float f, float f2);
    }

    public static final float clamp(float f) {
        if (f < 0.0f) {
            return 0.0f;
        }
        if (f > 1.0f) {
            return 1.0f;
        }
        return f;
    }

    public static final float lerp(float f, float f2, float f3) {
        return ((f3 - f2) * f) + f2;
    }

    /* access modifiers changed from: private */
    public static float luma(int i) {
        return ((((float) (16711680 & i)) * 0.2126f) / 1.671168E7f) + ((((float) (65280 & i)) * 0.7152f) / 65280.0f) + ((((float) (i & 255)) * 0.0722f) / 255.0f);
    }

    public static final float rlerp(float f, float f2, float f3) {
        return (f - f2) / (f3 - f2);
    }

    static /* synthetic */ int access$210(MLand mLand) {
        int i = mLand.mCountdown;
        mLand.mCountdown = i - 1;
        return i;
    }

    public static void L(String str, Object... objArr) {
        if (DEBUG) {
            if (objArr.length != 0) {
                str = String.format(str, objArr);
            }
            Log.d("MLand", str);
        }
    }

    private static class Params {
        public int BOOST_DV;
        public int BUILDING_HEIGHT_MIN;
        public int BUILDING_WIDTH_MAX;
        public int BUILDING_WIDTH_MIN;
        public int CLOUD_SIZE_MAX;
        public int CLOUD_SIZE_MIN;
        public int G;
        public float HUD_Z;
        public int MAX_V;
        public int OBSTACLE_GAP;
        public int OBSTACLE_MIN;
        public int OBSTACLE_PERIOD = ((int) (((float) this.OBSTACLE_SPACING) / this.TRANSLATION_PER_SEC));
        public int OBSTACLE_SPACING;
        public int OBSTACLE_STEM_WIDTH;
        public int OBSTACLE_WIDTH;
        public float OBSTACLE_Z;
        public int PLAYER_HIT_SIZE;
        public int PLAYER_SIZE;
        public float PLAYER_Z;
        public float PLAYER_Z_BOOST;
        public float SCENERY_Z;
        public int STAR_SIZE_MAX;
        public int STAR_SIZE_MIN;
        public float TRANSLATION_PER_SEC;

        public Params(Resources resources) {
            this.TRANSLATION_PER_SEC = resources.getDimension(R.dimen.translation_per_sec);
            this.OBSTACLE_SPACING = resources.getDimensionPixelSize(R.dimen.obstacle_spacing);
            this.BOOST_DV = resources.getDimensionPixelSize(R.dimen.boost_dv);
            this.PLAYER_HIT_SIZE = resources.getDimensionPixelSize(R.dimen.player_hit_size);
            this.PLAYER_SIZE = resources.getDimensionPixelSize(R.dimen.player_size);
            this.OBSTACLE_WIDTH = resources.getDimensionPixelSize(R.dimen.obstacle_width);
            this.OBSTACLE_STEM_WIDTH = resources.getDimensionPixelSize(R.dimen.obstacle_stem_width);
            this.OBSTACLE_GAP = resources.getDimensionPixelSize(R.dimen.obstacle_gap);
            this.OBSTACLE_MIN = resources.getDimensionPixelSize(R.dimen.obstacle_height_min);
            this.BUILDING_HEIGHT_MIN = resources.getDimensionPixelSize(R.dimen.building_height_min);
            this.BUILDING_WIDTH_MIN = resources.getDimensionPixelSize(R.dimen.building_width_min);
            this.BUILDING_WIDTH_MAX = resources.getDimensionPixelSize(R.dimen.building_width_max);
            this.CLOUD_SIZE_MIN = resources.getDimensionPixelSize(R.dimen.cloud_size_min);
            this.CLOUD_SIZE_MAX = resources.getDimensionPixelSize(R.dimen.cloud_size_max);
            this.STAR_SIZE_MIN = resources.getDimensionPixelSize(R.dimen.star_size_min);
            this.STAR_SIZE_MAX = resources.getDimensionPixelSize(R.dimen.star_size_max);
            this.G = resources.getDimensionPixelSize(R.dimen.G);
            this.MAX_V = resources.getDimensionPixelSize(R.dimen.max_v);
            this.SCENERY_Z = (float) resources.getDimensionPixelSize(R.dimen.scenery_z);
            this.OBSTACLE_Z = (float) resources.getDimensionPixelSize(R.dimen.obstacle_z);
            this.PLAYER_Z = (float) resources.getDimensionPixelSize(R.dimen.player_z);
            this.PLAYER_Z_BOOST = (float) resources.getDimensionPixelSize(R.dimen.player_z_boost);
            this.HUD_Z = (float) resources.getDimensionPixelSize(R.dimen.hud_z);
            if (this.OBSTACLE_MIN <= this.OBSTACLE_WIDTH / 2) {
                MLand.L("error: obstacles might be too short, adjusting", new Object[0]);
                this.OBSTACLE_MIN = (this.OBSTACLE_WIDTH / 2) + 1;
            }
        }
    }

    public MLand(Context context) {
        this(context, (AttributeSet) null);
    }

    public MLand(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public MLand(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mAudioAttrs = new AudioAttributes.Builder().setUsage(14).build();
        this.mPlayers = new ArrayList<>();
        this.mObstaclesInPlay = new ArrayList<>();
        this.mCountdown = 0;
        this.mGameControllers = new ArrayList<>();
        this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        setFocusable(true);
        PARAMS = new Params(getResources());
        this.mTimeOfDay = irand(0, SKIES.length - 1);
        this.mScene = irand(0, 3);
        this.mTouchPaint = new Paint(1);
        this.mTouchPaint.setColor(-2130706433);
        this.mTouchPaint.setStyle(Paint.Style.FILL);
        this.mPlayerTracePaint = new Paint(1);
        this.mPlayerTracePaint.setColor(-2130706433);
        this.mPlayerTracePaint.setStyle(Paint.Style.STROKE);
        this.mPlayerTracePaint.setStrokeWidth(dp * 2.0f);
        setLayoutDirection(0);
        setupPlayers(1);
        MetricsLogger.count(getContext(), "egg_mland_create", 1);
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        dp = getResources().getDisplayMetrics().density;
        reset();
        start(false);
    }

    public boolean willNotDraw() {
        return !DEBUG;
    }

    public float getGameTime() {
        return this.t;
    }

    public void setScoreFieldHolder(ViewGroup viewGroup) {
        this.mScoreFields = viewGroup;
        if (viewGroup != null) {
            LayoutTransition layoutTransition = new LayoutTransition();
            layoutTransition.setDuration(250);
            this.mScoreFields.setLayoutTransition(layoutTransition);
        }
        Iterator<Player> it = this.mPlayers.iterator();
        while (it.hasNext()) {
            this.mScoreFields.addView(it.next().mScoreField, new ViewGroup.MarginLayoutParams(-2, -1));
        }
    }

    public void setSplash(View view) {
        this.mSplash = view;
    }

    public static boolean isGamePad(InputDevice inputDevice) {
        int sources = inputDevice.getSources();
        return (sources & 1025) == 1025 || (sources & 16777232) == 16777232;
    }

    public ArrayList getGameControllers() {
        this.mGameControllers.clear();
        for (int i : InputDevice.getDeviceIds()) {
            if (isGamePad(InputDevice.getDevice(i)) && !this.mGameControllers.contains(Integer.valueOf(i))) {
                this.mGameControllers.add(Integer.valueOf(i));
            }
        }
        return this.mGameControllers;
    }

    public int getControllerPlayer(int i) {
        int indexOf = this.mGameControllers.indexOf(Integer.valueOf(i));
        if (indexOf < 0 || indexOf >= this.mPlayers.size()) {
            return 0;
        }
        return indexOf;
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        dp = getResources().getDisplayMetrics().density;
        stop();
        reset();
        start(false);
    }

    public Player getPlayer(int i) {
        if (i < this.mPlayers.size()) {
            return this.mPlayers.get(i);
        }
        return null;
    }

    private int addPlayerInternal(Player player) {
        this.mPlayers.add(player);
        realignPlayers();
        TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.mland_scorefield, (ViewGroup) null);
        ViewGroup viewGroup = this.mScoreFields;
        if (viewGroup != null) {
            viewGroup.addView(textView, new ViewGroup.MarginLayoutParams(-2, -1));
        }
        player.setScoreField(textView);
        return this.mPlayers.size() - 1;
    }

    private void removePlayerInternal(Player player) {
        if (this.mPlayers.remove(player)) {
            removeView(player);
            this.mScoreFields.removeView(player.mScoreField);
            realignPlayers();
        }
    }

    private void realignPlayers() {
        int size = this.mPlayers.size();
        float f = (float) ((this.mWidth - ((size - 1) * PARAMS.PLAYER_SIZE)) / 2);
        for (int i = 0; i < size; i++) {
            this.mPlayers.get(i).setX(f);
            f += (float) PARAMS.PLAYER_SIZE;
        }
    }

    private void clearPlayers() {
        while (this.mPlayers.size() > 0) {
            removePlayerInternal(this.mPlayers.get(0));
        }
    }

    public void setupPlayers(int i) {
        clearPlayers();
        for (int i2 = 0; i2 < i; i2++) {
            addPlayerInternal(Player.create(this));
        }
    }

    public void addPlayer() {
        if (getNumPlayers() != 6) {
            addPlayerInternal(Player.create(this));
        }
    }

    public int getNumPlayers() {
        return this.mPlayers.size();
    }

    public void removePlayer() {
        if (getNumPlayers() != 1) {
            ArrayList<Player> arrayList = this.mPlayers;
            removePlayerInternal(arrayList.get(arrayList.size() - 1));
        }
    }

    private void thump(int i, long j) {
        InputDevice device;
        if (this.mAudioManager.getRingerMode() != 0) {
            if (i >= this.mGameControllers.size() || (device = InputDevice.getDevice(this.mGameControllers.get(i).intValue())) == null || !device.getVibrator().hasVibrator()) {
                this.mVibrator.vibrate(j, this.mAudioAttrs);
            } else {
                device.getVibrator().vibrate((long) (((float) j) * 2.0f), this.mAudioAttrs);
            }
        }
    }

    public void reset() {
        Scenery scenery;
        L("reset", new Object[0]);
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, SKIES[this.mTimeOfDay]);
        gradientDrawable.setDither(true);
        setBackground(gradientDrawable);
        this.mFlipped = frand() > 0.5f;
        float f = -1.0f;
        setScaleX(this.mFlipped ? -1.0f : 1.0f);
        int childCount = getChildCount();
        while (true) {
            int i = childCount - 1;
            if (childCount <= 0) {
                break;
            }
            if (getChildAt(i) instanceof GameView) {
                removeViewAt(i);
            }
            childCount = i;
        }
        this.mObstaclesInPlay.clear();
        this.mCurrentPipeId = 0;
        this.mWidth = getWidth();
        this.mHeight = getHeight();
        int i2 = this.mTimeOfDay;
        boolean z = (i2 == 0 || i2 == 3) && ((double) frand()) > 0.25d;
        if (z) {
            Star star = new Star(getContext());
            star.setBackgroundResource(R.drawable.sun);
            int dimensionPixelSize = getResources().getDimensionPixelSize(R.dimen.sun_size);
            float f2 = (float) dimensionPixelSize;
            star.setTranslationX(frand(f2, (float) (this.mWidth - dimensionPixelSize)));
            if (this.mTimeOfDay == 0) {
                star.setTranslationY(frand(f2, ((float) this.mHeight) * 0.66f));
                star.getBackground().setTint(0);
            } else {
                int i3 = this.mHeight;
                star.setTranslationY(frand(((float) i3) * 0.66f, (float) (i3 - dimensionPixelSize)));
                star.getBackground().setTintMode(PorterDuff.Mode.SRC_ATOP);
                star.getBackground().setTint(-1056997376);
            }
            addView(star, new FrameLayout.LayoutParams(dimensionPixelSize, dimensionPixelSize));
        }
        if (!z) {
            int i4 = this.mTimeOfDay;
            boolean z2 = i4 == 1 || i4 == 2;
            float frand = frand();
            if ((z2 && frand < 0.75f) || frand < 0.5f) {
                Star star2 = new Star(getContext());
                star2.setBackgroundResource(R.drawable.moon);
                star2.getBackground().setAlpha(z2 ? 255 : 128);
                if (((double) frand()) <= 0.5d) {
                    f = 1.0f;
                }
                star2.setScaleX(f);
                star2.setRotation(star2.getScaleX() * frand(5.0f, 30.0f));
                int dimensionPixelSize2 = getResources().getDimensionPixelSize(R.dimen.sun_size);
                float f3 = (float) dimensionPixelSize2;
                star2.setTranslationX(frand(f3, (float) (this.mWidth - dimensionPixelSize2)));
                star2.setTranslationY(frand(f3, (float) (this.mHeight - dimensionPixelSize2)));
                addView(star2, new FrameLayout.LayoutParams(dimensionPixelSize2, dimensionPixelSize2));
            }
        }
        int i5 = this.mHeight / 6;
        boolean z3 = ((double) frand()) < 0.25d;
        for (int i6 = 0; i6 < 20; i6++) {
            double frand2 = (double) frand();
            if (frand2 < 0.3d && this.mTimeOfDay != 0) {
                scenery = new Star(getContext());
            } else if (frand2 >= 0.6d || z3) {
                int i7 = this.mScene;
                if (i7 != 1) {
                    scenery = i7 != 2 ? new Building(getContext()) : new Mountain(getContext());
                } else {
                    scenery = new Cactus(getContext());
                }
                scenery.z = ((float) i6) / 20.0f;
                scenery.v = scenery.z * 0.85f;
                if (this.mScene == 0) {
                    scenery.setBackgroundColor(-7829368);
                    scenery.h = irand(PARAMS.BUILDING_HEIGHT_MIN, i5);
                }
                int i8 = (int) (scenery.z * 255.0f);
                Drawable background = scenery.getBackground();
                if (background != null) {
                    background.setColorFilter(Color.rgb(i8, i8, i8), PorterDuff.Mode.MULTIPLY);
                }
            } else {
                scenery = new Cloud(getContext());
            }
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(scenery.w, scenery.h);
            if (scenery instanceof Building) {
                layoutParams.gravity = 80;
            } else {
                layoutParams.gravity = 48;
                float frand3 = frand();
                if (scenery instanceof Star) {
                    layoutParams.topMargin = (int) (frand3 * frand3 * ((float) this.mHeight));
                } else {
                    int i9 = this.mHeight;
                    layoutParams.topMargin = ((int) (1.0f - (((frand3 * frand3) * ((float) i9)) / 2.0f))) + (i9 / 2);
                }
            }
            addView(scenery, layoutParams);
            scenery.setTranslationX(frand((float) (-layoutParams.width), (float) (this.mWidth + layoutParams.width)));
        }
        Iterator<Player> it = this.mPlayers.iterator();
        while (it.hasNext()) {
            Player next = it.next();
            addView(next);
            next.reset();
        }
        realignPlayers();
        TimeAnimator timeAnimator = this.mAnim;
        if (timeAnimator != null) {
            timeAnimator.cancel();
        }
        this.mAnim = new TimeAnimator();
        this.mAnim.setTimeListener(new TimeAnimator.TimeListener() {
            public void onTimeUpdate(TimeAnimator timeAnimator, long j, long j2) {
                MLand.this.step(j, j2);
            }
        });
    }

    public void start(boolean z) {
        Object[] objArr = new Object[1];
        objArr[0] = z ? "true" : "false";
        L("start(startPlaying=%s)", objArr);
        if (z && this.mCountdown <= 0) {
            showSplash();
            this.mSplash.findViewById(R.id.play_button).setEnabled(false);
            View findViewById = this.mSplash.findViewById(R.id.play_button_image);
            final TextView textView = (TextView) this.mSplash.findViewById(R.id.play_button_text);
            findViewById.animate().alpha(0.0f);
            textView.animate().alpha(1.0f);
            this.mCountdown = 3;
            post(new Runnable() {
                public void run() {
                    if (MLand.this.mCountdown == 0) {
                        MLand.this.startPlaying();
                    } else {
                        MLand.this.postDelayed(this, 500);
                    }
                    textView.setText(String.valueOf(MLand.this.mCountdown));
                    MLand.access$210(MLand.this);
                }
            });
        }
        Iterator<Player> it = this.mPlayers.iterator();
        while (it.hasNext()) {
            it.next().setVisibility(4);
        }
        if (!this.mAnimating) {
            this.mAnim.start();
            this.mAnimating = true;
        }
    }

    public void hideSplash() {
        View view = this.mSplash;
        if (view != null && view.getVisibility() == 0) {
            this.mSplash.setClickable(false);
            this.mSplash.animate().alpha(0.0f).translationZ(0.0f).setDuration(300).withEndAction(new Runnable() {
                public void run() {
                    MLand.this.mSplash.setVisibility(8);
                }
            });
        }
    }

    public void showSplash() {
        View view = this.mSplash;
        if (view != null && view.getVisibility() != 0) {
            this.mSplash.setClickable(true);
            this.mSplash.setAlpha(0.0f);
            this.mSplash.setVisibility(0);
            this.mSplash.animate().alpha(1.0f).setDuration(1000);
            this.mSplash.findViewById(R.id.play_button_image).setAlpha(1.0f);
            this.mSplash.findViewById(R.id.play_button_text).setAlpha(0.0f);
            this.mSplash.findViewById(R.id.play_button).setEnabled(true);
            this.mSplash.findViewById(R.id.play_button).requestFocus();
        }
    }

    public void startPlaying() {
        this.mPlaying = true;
        this.t = 0.0f;
        this.mLastPipeTime = getGameTime() - ((float) PARAMS.OBSTACLE_PERIOD);
        hideSplash();
        realignPlayers();
        this.mTaps = 0;
        int size = this.mPlayers.size();
        MetricsLogger.histogram(getContext(), "egg_mland_players", size);
        for (int i = 0; i < size; i++) {
            Player player = this.mPlayers.get(i);
            player.setVisibility(0);
            player.reset();
            player.start();
            player.boost(-1.0f, -1.0f);
            player.unboost();
        }
    }

    public void stop() {
        if (this.mAnimating) {
            this.mAnim.cancel();
            this.mAnim = null;
            this.mAnimating = false;
            this.mPlaying = false;
            this.mTimeOfDay = irand(0, SKIES.length - 1);
            this.mScene = irand(0, 3);
            this.mFrozen = true;
            Iterator<Player> it = this.mPlayers.iterator();
            while (it.hasNext()) {
                it.next().die();
            }
            postDelayed(new Runnable() {
                public void run() {
                    boolean unused = MLand.this.mFrozen = false;
                }
            }, 250);
        }
    }

    public static final float frand() {
        return (float) Math.random();
    }

    public static final float frand(float f, float f2) {
        return lerp(frand(), f, f2);
    }

    public static final int irand(int i, int i2) {
        return Math.round(frand((float) i, (float) i2));
    }

    public static int pick(int[] iArr) {
        return iArr[irand(0, iArr.length - 1)];
    }

    /* access modifiers changed from: private */
    public void step(long j, long j2) {
        int i;
        this.t = ((float) j) / 1000.0f;
        this.dt = ((float) j2) / 1000.0f;
        if (DEBUG) {
            this.t *= 0.5f;
            this.dt *= 0.5f;
        }
        int childCount = getChildCount();
        int i2 = 0;
        while (i < childCount) {
            View childAt = getChildAt(i);
            if (childAt instanceof GameView) {
                ((GameView) childAt).step(j, j2, this.t, this.dt);
            }
            i2 = i + 1;
        }
        if (this.mPlaying) {
            int i3 = 0;
            i = 0;
            while (i < this.mPlayers.size()) {
                Player player = getPlayer(i);
                if (player.mAlive) {
                    if (player.below(this.mHeight)) {
                        if (DEBUG_IDDQD) {
                            poke(i);
                            unpoke(i);
                        } else {
                            L("player %d hit the floor", Integer.valueOf(i));
                            thump(i, 80);
                            player.die();
                        }
                    }
                    int size = this.mObstaclesInPlay.size();
                    int i4 = 0;
                    while (true) {
                        int i5 = size - 1;
                        if (size <= 0) {
                            break;
                        }
                        Obstacle obstacle = this.mObstaclesInPlay.get(i5);
                        if (obstacle.intersects(player) && !DEBUG_IDDQD) {
                            L("player hit an obstacle", new Object[0]);
                            thump(i, 80);
                            player.die();
                        } else if (obstacle.cleared(player) && (obstacle instanceof Stem)) {
                            i4 = Math.max(i4, ((Stem) obstacle).id);
                        }
                        size = i5;
                    }
                    if (i4 > player.mScore) {
                        player.addScore(1);
                    }
                }
                if (player.mAlive) {
                    i3++;
                }
                i++;
            }
            if (i3 == 0) {
                stop();
                MetricsLogger.count(getContext(), "egg_mland_taps", this.mTaps);
                this.mTaps = 0;
                int size2 = this.mPlayers.size();
                for (int i6 = 0; i6 < size2; i6++) {
                    MetricsLogger.histogram(getContext(), "egg_mland_score", this.mPlayers.get(i6).getScore());
                }
            }
        }
        while (true) {
            int i7 = i - 1;
            if (i <= 0) {
                break;
            }
            View childAt2 = getChildAt(i7);
            if (childAt2 instanceof Obstacle) {
                if (childAt2.getTranslationX() + ((float) childAt2.getWidth()) < 0.0f) {
                    removeViewAt(i7);
                    this.mObstaclesInPlay.remove(childAt2);
                }
            } else if ((childAt2 instanceof Scenery) && childAt2.getTranslationX() + ((float) ((Scenery) childAt2).w) < 0.0f) {
                childAt2.setTranslationX((float) getWidth());
            }
            i = i7;
        }
        if (this.mPlaying) {
            float f = this.t;
            if (f - this.mLastPipeTime > ((float) PARAMS.OBSTACLE_PERIOD)) {
                this.mLastPipeTime = f;
                this.mCurrentPipeId++;
                float frand = frand();
                int i8 = this.mHeight;
                Params params = PARAMS;
                int i9 = params.OBSTACLE_MIN;
                int i10 = ((int) (frand * ((float) ((i8 - (i9 * 2)) - params.OBSTACLE_GAP)))) + i9;
                int i11 = params.OBSTACLE_WIDTH;
                int i12 = (i11 - params.OBSTACLE_STEM_WIDTH) / 2;
                int i13 = i11 / 2;
                int irand = irand(0, 250);
                Stem stem = new Stem(getContext(), (float) (i10 - i13), false);
                addView(stem, new FrameLayout.LayoutParams(PARAMS.OBSTACLE_STEM_WIDTH, (int) stem.h, 51));
                stem.setTranslationX((float) (this.mWidth + i12));
                float f2 = (float) i13;
                stem.setTranslationY((-stem.h) - f2);
                stem.setTranslationZ(PARAMS.OBSTACLE_Z * 0.75f);
                long j3 = (long) irand;
                stem.animate().translationY(0.0f).setStartDelay(j3).setDuration(250);
                this.mObstaclesInPlay.add(stem);
                Pop pop = new Pop(getContext(), (float) PARAMS.OBSTACLE_WIDTH);
                int i14 = PARAMS.OBSTACLE_WIDTH;
                addView(pop, new FrameLayout.LayoutParams(i14, i14, 51));
                pop.setTranslationX((float) this.mWidth);
                pop.setTranslationY((float) (-PARAMS.OBSTACLE_WIDTH));
                pop.setTranslationZ(PARAMS.OBSTACLE_Z);
                pop.setScaleX(0.25f);
                pop.setScaleY(-0.25f);
                pop.animate().translationY(stem.h - ((float) i12)).scaleX(1.0f).scaleY(-1.0f).setStartDelay(j3).setDuration(250);
                this.mObstaclesInPlay.add(pop);
                int irand2 = irand(0, 250);
                Stem stem2 = new Stem(getContext(), (float) (((this.mHeight - i10) - PARAMS.OBSTACLE_GAP) - i13), true);
                addView(stem2, new FrameLayout.LayoutParams(PARAMS.OBSTACLE_STEM_WIDTH, (int) stem2.h, 51));
                stem2.setTranslationX((float) (this.mWidth + i12));
                stem2.setTranslationY((float) (this.mHeight + i13));
                stem2.setTranslationZ(PARAMS.OBSTACLE_Z * 0.75f);
                long j4 = (long) irand2;
                stem2.animate().translationY(((float) this.mHeight) - stem2.h).setStartDelay(j4).setDuration(400);
                this.mObstaclesInPlay.add(stem2);
                Pop pop2 = new Pop(getContext(), (float) PARAMS.OBSTACLE_WIDTH);
                int i15 = PARAMS.OBSTACLE_WIDTH;
                addView(pop2, new FrameLayout.LayoutParams(i15, i15, 51));
                pop2.setTranslationX((float) this.mWidth);
                pop2.setTranslationY((float) this.mHeight);
                pop2.setTranslationZ(PARAMS.OBSTACLE_Z);
                pop2.setScaleX(0.25f);
                pop2.setScaleY(0.25f);
                pop2.animate().translationY((((float) this.mHeight) - stem2.h) - f2).scaleX(1.0f).scaleY(1.0f).setStartDelay(j4).setDuration(400);
                this.mObstaclesInPlay.add(pop2);
            }
        }
        invalidate();
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        L("touch: %s", motionEvent);
        int actionIndex = motionEvent.getActionIndex();
        float x = motionEvent.getX(actionIndex);
        float y = motionEvent.getY(actionIndex);
        int numPlayers = (int) (((float) getNumPlayers()) * (x / ((float) getWidth())));
        if (this.mFlipped) {
            numPlayers = (getNumPlayers() - 1) - numPlayers;
        }
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked != 0) {
            if (actionMasked != 1) {
                if (actionMasked != 5) {
                    if (actionMasked != 6) {
                        return false;
                    }
                }
            }
            unpoke(numPlayers);
            return true;
        }
        poke(numPlayers, x, y);
        return true;
    }

    public boolean onTrackballEvent(MotionEvent motionEvent) {
        L("trackball: %s", motionEvent);
        int action = motionEvent.getAction();
        if (action == 0) {
            poke(0);
            return true;
        } else if (action != 1) {
            return false;
        } else {
            unpoke(0);
            return true;
        }
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        L("keyDown: %d", Integer.valueOf(i));
        if (i != 19 && i != 23 && i != 62 && i != 66 && i != 96) {
            return false;
        }
        poke(getControllerPlayer(keyEvent.getDeviceId()));
        return true;
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        L("keyDown: %d", Integer.valueOf(i));
        if (i != 19 && i != 23 && i != 62 && i != 66 && i != 96) {
            return false;
        }
        unpoke(getControllerPlayer(keyEvent.getDeviceId()));
        return true;
    }

    public boolean onGenericMotionEvent(MotionEvent motionEvent) {
        L("generic: %s", motionEvent);
        return false;
    }

    private void poke(int i) {
        poke(i, -1.0f, -1.0f);
    }

    private void poke(int i, float f, float f2) {
        L("poke(%d)", Integer.valueOf(i));
        if (!this.mFrozen) {
            if (!this.mAnimating) {
                reset();
            }
            if (!this.mPlaying) {
                start(true);
                return;
            }
            Player player = getPlayer(i);
            if (player != null) {
                player.boost(f, f2);
                this.mTaps++;
                if (DEBUG) {
                    player.dv *= 0.5f;
                    player.animate().setDuration(400);
                }
            }
        }
    }

    private void unpoke(int i) {
        Player player;
        L("unboost(%d)", Integer.valueOf(i));
        if (!this.mFrozen && this.mAnimating && this.mPlaying && (player = getPlayer(i)) != null) {
            player.unboost();
        }
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Iterator<Player> it = this.mPlayers.iterator();
        while (it.hasNext()) {
            Player next = it.next();
            if (next.mTouchX > 0.0f) {
                this.mTouchPaint.setColor(next.color & -2130706433);
                this.mPlayerTracePaint.setColor(next.color & -2130706433);
                float access$800 = next.mTouchX;
                float access$900 = next.mTouchY;
                canvas.drawCircle(access$800, access$900, 100.0f, this.mTouchPaint);
                float x = next.getX() + next.getPivotX();
                float y = next.getY() + next.getPivotY();
                double atan2 = (double) (1.5707964f - ((float) Math.atan2((double) (x - access$800), (double) (y - access$900))));
                canvas.drawLine((float) (((double) access$800) + (Math.cos(atan2) * 100.0d)), (float) (((double) access$900) + (Math.sin(atan2) * 100.0d)), x, y, this.mPlayerTracePaint);
            }
        }
    }

    private static class Player extends ImageView implements GameView {
        static int sNextColor;
        public int color;
        public final float[] corners = new float[this.sHull.length];
        public float dv;
        /* access modifiers changed from: private */
        public boolean mAlive;
        private boolean mBoosting;
        private MLand mLand;
        /* access modifiers changed from: private */
        public int mScore;
        /* access modifiers changed from: private */
        public TextView mScoreField;
        /* access modifiers changed from: private */
        public float mTouchX = -1.0f;
        /* access modifiers changed from: private */
        public float mTouchY = -1.0f;
        private final int[] sColors = {-2407369, -12879641, -740352, -15753896, -8710016, -6381922};
        private final float[] sHull = {0.3f, 0.0f, 0.7f, 0.0f, 0.92f, 0.33f, 0.92f, 0.75f, 0.6f, 1.0f, 0.4f, 1.0f, 0.08f, 0.75f, 0.08f, 0.33f};

        public static Player create(MLand mLand2) {
            Player player = new Player(mLand2.getContext());
            player.mLand = mLand2;
            player.reset();
            player.setVisibility(4);
            mLand2.addView(player, new FrameLayout.LayoutParams(MLand.PARAMS.PLAYER_SIZE, MLand.PARAMS.PLAYER_SIZE));
            return player;
        }

        private void setScore(int i) {
            this.mScore = i;
            TextView textView = this.mScoreField;
            if (textView != null) {
                textView.setText(MLand.DEBUG_IDDQD ? "??" : String.valueOf(i));
            }
        }

        public int getScore() {
            return this.mScore;
        }

        /* access modifiers changed from: private */
        public void addScore(int i) {
            setScore(this.mScore + i);
        }

        public void setScoreField(TextView textView) {
            this.mScoreField = textView;
            if (textView != null) {
                setScore(this.mScore);
                this.mScoreField.getBackground().setColorFilter(this.color, PorterDuff.Mode.SRC_ATOP);
                this.mScoreField.setTextColor(MLand.luma(this.color) > 0.7f ? -16777216 : -1);
            }
        }

        public void reset() {
            setY((float) (((this.mLand.mHeight / 2) + ((int) (Math.random() * ((double) MLand.PARAMS.PLAYER_SIZE)))) - (MLand.PARAMS.PLAYER_SIZE / 2)));
            setScore(0);
            setScoreField(this.mScoreField);
            this.mBoosting = false;
            this.dv = 0.0f;
        }

        public Player(Context context) {
            super(context);
            setBackgroundResource(R.drawable.f1android);
            getBackground().setTintMode(PorterDuff.Mode.SRC_ATOP);
            int[] iArr = this.sColors;
            int i = sNextColor;
            sNextColor = i + 1;
            this.color = iArr[i % iArr.length];
            getBackground().setTint(this.color);
            setOutlineProvider(new ViewOutlineProvider() {
                public void getOutline(View view, Outline outline) {
                    int width = view.getWidth();
                    int height = view.getHeight();
                    int i = (int) (((float) width) * 0.3f);
                    int i2 = (int) (((float) height) * 0.2f);
                    outline.setRect(i, i2, width - i, height - i2);
                }
            });
        }

        public void prepareCheckIntersections() {
            int i = (MLand.PARAMS.PLAYER_SIZE - MLand.PARAMS.PLAYER_HIT_SIZE) / 2;
            int i2 = MLand.PARAMS.PLAYER_HIT_SIZE;
            int length = this.sHull.length / 2;
            for (int i3 = 0; i3 < length; i3++) {
                float[] fArr = this.corners;
                int i4 = i3 * 2;
                float f = (float) i2;
                float[] fArr2 = this.sHull;
                float f2 = (float) i;
                fArr[i4] = (fArr2[i4] * f) + f2;
                int i5 = i4 + 1;
                fArr[i5] = (f * fArr2[i5]) + f2;
            }
            getMatrix().mapPoints(this.corners);
        }

        public boolean below(int i) {
            int length = this.corners.length / 2;
            for (int i2 = 0; i2 < length; i2++) {
                if (((int) this.corners[(i2 * 2) + 1]) >= i) {
                    return true;
                }
            }
            return false;
        }

        public void step(long j, long j2, float f, float f2) {
            if (!this.mAlive) {
                setTranslationX(getTranslationX() - (MLand.PARAMS.TRANSLATION_PER_SEC * f2));
                return;
            }
            if (this.mBoosting) {
                this.dv = (float) (-MLand.PARAMS.BOOST_DV);
            } else {
                this.dv += (float) MLand.PARAMS.G;
            }
            if (this.dv < ((float) (-MLand.PARAMS.MAX_V))) {
                this.dv = (float) (-MLand.PARAMS.MAX_V);
            } else if (this.dv > ((float) MLand.PARAMS.MAX_V)) {
                this.dv = (float) MLand.PARAMS.MAX_V;
            }
            float translationY = getTranslationY() + (this.dv * f2);
            if (translationY < 0.0f) {
                translationY = 0.0f;
            }
            setTranslationY(translationY);
            setRotation(MLand.lerp(MLand.clamp(MLand.rlerp(this.dv, (float) MLand.PARAMS.MAX_V, (float) (MLand.PARAMS.MAX_V * -1))), 90.0f, -90.0f) + 90.0f);
            prepareCheckIntersections();
        }

        public void boost(float f, float f2) {
            this.mTouchX = f;
            this.mTouchY = f2;
            boost();
        }

        public void boost() {
            this.mBoosting = true;
            this.dv = (float) (-MLand.PARAMS.BOOST_DV);
            animate().cancel();
            animate().scaleX(1.25f).scaleY(1.25f).translationZ(MLand.PARAMS.PLAYER_Z_BOOST).setDuration(100);
            setScaleX(1.25f);
            setScaleY(1.25f);
        }

        public void unboost() {
            this.mBoosting = false;
            this.mTouchY = -1.0f;
            this.mTouchX = -1.0f;
            animate().cancel();
            animate().scaleX(1.0f).scaleY(1.0f).translationZ(MLand.PARAMS.PLAYER_Z).setDuration(200);
        }

        public void die() {
            this.mAlive = false;
        }

        public void start() {
            this.mAlive = true;
        }
    }

    private class Obstacle extends View implements GameView {
        public float h;
        public final Rect hitRect = new Rect();

        public Obstacle(Context context, float f) {
            super(context);
            setBackgroundColor(-65536);
            this.h = f;
        }

        public boolean intersects(Player player) {
            int length = player.corners.length / 2;
            for (int i = 0; i < length; i++) {
                float[] fArr = player.corners;
                int i2 = i * 2;
                if (this.hitRect.contains((int) fArr[i2], (int) fArr[i2 + 1])) {
                    return true;
                }
            }
            return false;
        }

        public boolean cleared(Player player) {
            int length = player.corners.length / 2;
            for (int i = 0; i < length; i++) {
                if (this.hitRect.right >= ((int) player.corners[i * 2])) {
                    return false;
                }
            }
            return true;
        }

        public void step(long j, long j2, float f, float f2) {
            setTranslationX(getTranslationX() - (MLand.PARAMS.TRANSLATION_PER_SEC * f2));
            getHitRect(this.hitRect);
        }
    }

    private class Pop extends Obstacle {
        Drawable antenna;
        int cx;
        int cy;
        Drawable eyes;
        int mRotate;
        Drawable mouth;
        int r;

        public Pop(Context context, float f) {
            super(context, f);
            setBackgroundResource(R.drawable.mm_head);
            this.antenna = context.getDrawable(MLand.pick(MLand.ANTENNAE));
            if (MLand.frand() > 0.5f) {
                this.eyes = context.getDrawable(MLand.pick(MLand.EYES));
                if (MLand.frand() > 0.8f) {
                    this.mouth = context.getDrawable(MLand.pick(MLand.MOUTHS));
                }
            }
            setOutlineProvider(new ViewOutlineProvider(MLand.this) {
                public void getOutline(View view, Outline outline) {
                    int width = (int) ((((float) Pop.this.getWidth()) * 1.0f) / 6.0f);
                    outline.setOval(width, width, Pop.this.getWidth() - width, Pop.this.getHeight() - width);
                }
            });
        }

        public boolean intersects(Player player) {
            int length = player.corners.length / 2;
            for (int i = 0; i < length; i++) {
                float[] fArr = player.corners;
                int i2 = i * 2;
                if (Math.hypot((double) (((int) fArr[i2]) - this.cx), (double) (((int) fArr[i2 + 1]) - this.cy)) <= ((double) this.r)) {
                    return true;
                }
            }
            return false;
        }

        public void step(long j, long j2, float f, float f2) {
            super.step(j, j2, f, f2);
            if (this.mRotate != 0) {
                setRotation(getRotation() + (f2 * 45.0f * ((float) this.mRotate)));
            }
            Rect rect = this.hitRect;
            this.cx = (rect.left + rect.right) / 2;
            this.cy = (rect.top + rect.bottom) / 2;
            this.r = getWidth() / 3;
        }

        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            Drawable drawable = this.antenna;
            if (drawable != null) {
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                this.antenna.draw(canvas);
            }
            Drawable drawable2 = this.eyes;
            if (drawable2 != null) {
                drawable2.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                this.eyes.draw(canvas);
            }
            Drawable drawable3 = this.mouth;
            if (drawable3 != null) {
                drawable3.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                this.mouth.draw(canvas);
            }
        }
    }

    private class Stem extends Obstacle {
        int id;
        boolean mDrawShadow;
        GradientDrawable mGradient = new GradientDrawable();
        Path mJandystripe;
        Paint mPaint = new Paint();
        Paint mPaint2;
        Path mShadow = new Path();

        public Stem(Context context, float f, boolean z) {
            super(context, f);
            this.id = MLand.this.mCurrentPipeId;
            this.mDrawShadow = z;
            setBackground((Drawable) null);
            this.mGradient.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            this.mPaint.setColor(-16777216);
            this.mPaint.setColorFilter(new PorterDuffColorFilter(570425344, PorterDuff.Mode.MULTIPLY));
            if (MLand.frand() < 0.01f) {
                this.mGradient.setColors(new int[]{-1, -2236963});
                this.mJandystripe = new Path();
                this.mPaint2 = new Paint();
                this.mPaint2.setColor(-65536);
                this.mPaint2.setColorFilter(new PorterDuffColorFilter(-65536, PorterDuff.Mode.MULTIPLY));
                return;
            }
            this.mGradient.setColors(new int[]{-4412764, -6190977});
        }

        public void onAttachedToWindow() {
            super.onAttachedToWindow();
            setWillNotDraw(false);
            setOutlineProvider(new ViewOutlineProvider() {
                public void getOutline(View view, Outline outline) {
                    outline.setRect(0, 0, Stem.this.getWidth(), Stem.this.getHeight());
                }
            });
        }

        public void onDraw(Canvas canvas) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            float f = (float) width;
            this.mGradient.setGradientCenter(0.75f * f, 0.0f);
            int i = 0;
            this.mGradient.setBounds(0, 0, width, height);
            this.mGradient.draw(canvas);
            Path path = this.mJandystripe;
            if (path != null) {
                path.reset();
                this.mJandystripe.moveTo(0.0f, f);
                this.mJandystripe.lineTo(f, 0.0f);
                this.mJandystripe.lineTo(f, (float) (width * 2));
                this.mJandystripe.lineTo(0.0f, (float) (width * 3));
                this.mJandystripe.close();
                while (i < height) {
                    canvas.drawPath(this.mJandystripe, this.mPaint2);
                    int i2 = width * 4;
                    this.mJandystripe.offset(0.0f, (float) i2);
                    i += i2;
                }
            }
            if (this.mDrawShadow) {
                this.mShadow.reset();
                this.mShadow.moveTo(0.0f, 0.0f);
                this.mShadow.lineTo(f, 0.0f);
                this.mShadow.lineTo(f, (((float) MLand.PARAMS.OBSTACLE_WIDTH) * 0.4f) + (1.5f * f));
                this.mShadow.lineTo(0.0f, ((float) MLand.PARAMS.OBSTACLE_WIDTH) * 0.4f);
                this.mShadow.close();
                canvas.drawPath(this.mShadow, this.mPaint);
            }
        }
    }

    private class Scenery extends FrameLayout implements GameView {
        public int h;
        public float v;
        public int w;
        public float z;

        public Scenery(Context context) {
            super(context);
        }

        public void step(long j, long j2, float f, float f2) {
            setTranslationX(getTranslationX() - ((MLand.PARAMS.TRANSLATION_PER_SEC * f2) * this.v));
        }
    }

    private class Building extends Scenery {
        public Building(Context context) {
            super(context);
            this.w = MLand.irand(MLand.PARAMS.BUILDING_WIDTH_MIN, MLand.PARAMS.BUILDING_WIDTH_MAX);
            this.h = 0;
        }
    }

    private class Cactus extends Building {
        public Cactus(Context context) {
            super(context);
            setBackgroundResource(MLand.pick(MLand.CACTI));
            int irand = MLand.irand(MLand.PARAMS.BUILDING_WIDTH_MAX / 4, MLand.PARAMS.BUILDING_WIDTH_MAX / 2);
            this.h = irand;
            this.w = irand;
        }
    }

    private class Mountain extends Building {
        public Mountain(Context context) {
            super(context);
            setBackgroundResource(MLand.pick(MLand.MOUNTAINS));
            int irand = MLand.irand(MLand.PARAMS.BUILDING_WIDTH_MAX / 2, MLand.PARAMS.BUILDING_WIDTH_MAX);
            this.h = irand;
            this.w = irand;
            this.z = 0.0f;
        }
    }

    private class Cloud extends Scenery {
        public Cloud(Context context) {
            super(context);
            setBackgroundResource(MLand.frand() < 0.01f ? R.drawable.cloud_off : R.drawable.cloud);
            getBackground().setAlpha(64);
            int irand = MLand.irand(MLand.PARAMS.CLOUD_SIZE_MIN, MLand.PARAMS.CLOUD_SIZE_MAX);
            this.h = irand;
            this.w = irand;
            this.z = 0.0f;
            this.v = MLand.frand(0.15f, 0.5f);
        }
    }

    private class Star extends Scenery {
        public Star(Context context) {
            super(context);
            setBackgroundResource(R.drawable.star);
            int irand = MLand.irand(MLand.PARAMS.STAR_SIZE_MIN, MLand.PARAMS.STAR_SIZE_MAX);
            this.h = irand;
            this.w = irand;
            this.z = 0.0f;
            this.v = 0.0f;
        }
    }
}

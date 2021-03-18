package com.android.keyguard;

import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class HeiHeiGestureView extends FrameLayout {
    private static final float DENSITY;
    private static final float MOVE_DOWN_DISTANCE_THREDHOLD;
    private static final float MOVE_UP_DISTANCE_THREDHOLD;
    private static final float TWO_POINTS_DISTANCE_X_THREDHOLD;
    private static final float TWO_POINTS_DISTANCE_Y_THREDHOLD;
    private static final float TWO_POINTS_DISTANCE_Y_THREDHOLD_MIN;
    private float mBottomY;
    private double[] mChances = new double[0];
    private int mCurrentPicture;
    private String mCurrentSound;
    private float mFirstY;
    private ImageView mImageView;
    private long mLastMatchTime;
    private long mLastTiggerTime;
    private OnTriggerListener mListener;
    private int[] mPictures;
    private MediaPlayer mPlayer;
    private String[] mSounds;
    private DetectingStage mStage;
    private float mTopY;

    /* access modifiers changed from: package-private */
    public enum DetectingStage {
        STOP,
        WAITING,
        MOVE_DOWN,
        MOVE_UP,
        MATCHED
    }

    public interface OnTriggerListener {
        void onTrigger();
    }

    static {
        float f = Resources.getSystem().getDisplayMetrics().density;
        DENSITY = f;
        TWO_POINTS_DISTANCE_X_THREDHOLD = 150.0f * f;
        TWO_POINTS_DISTANCE_Y_THREDHOLD = 300.0f * f;
        TWO_POINTS_DISTANCE_Y_THREDHOLD_MIN = 50.0f * f;
        MOVE_DOWN_DISTANCE_THREDHOLD = f * 100.0f;
        MOVE_UP_DISTANCE_THREDHOLD = f * 100.0f;
    }

    public HeiHeiGestureView(Context context) {
        super(context);
    }

    public void setOnTriggerListener(OnTriggerListener onTriggerListener) {
        this.mListener = onTriggerListener;
    }

    private void trigger() {
        this.mLastTiggerTime = System.currentTimeMillis();
        if (this.mChances.length == 0) {
            OnTriggerListener onTriggerListener = this.mListener;
            if (onTriggerListener != null) {
                onTriggerListener.onTrigger();
            }
            if (1 == Settings.System.getIntForUser(((FrameLayout) this).mContext.getContentResolver(), "lockscreen_sounds_enabled", 1, KeyguardUpdateMonitor.getCurrentUser())) {
                playSound();
                return;
            }
            return;
        }
        playSound();
        this.mImageView.setVisibility(0);
        this.mImageView.setImageResource(this.mCurrentPicture);
        postDelayed(new Runnable() {
            /* class com.android.keyguard.HeiHeiGestureView.AnonymousClass1 */

            public void run() {
                HeiHeiGestureView.this.mImageView.setVisibility(8);
            }
        }, 1500);
    }

    private void prepare() {
        String str;
        if (this.mChances.length > 0) {
            double random = Math.random();
            int i = 0;
            while (true) {
                double[] dArr = this.mChances;
                if (i >= dArr.length) {
                    break;
                }
                random -= dArr[i];
                if (random <= 0.0d) {
                    str = this.mSounds[i];
                    this.mCurrentPicture = this.mPictures[i];
                    break;
                }
                i++;
            }
        }
        str = "/system/media/audio/ui/HeiHei.mp3";
        try {
            if (this.mPlayer == null) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                this.mPlayer = mediaPlayer;
                mediaPlayer.setAudioStreamType(1);
            }
            if (!TextUtils.equals(this.mCurrentSound, str)) {
                this.mPlayer.reset();
                this.mPlayer.setDataSource(str);
            } else {
                this.mPlayer.stop();
                this.mPlayer.seekTo(0);
            }
            this.mPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
            this.mPlayer = null;
        }
    }

    private void playSound() {
        try {
            this.mPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            this.mPlayer = null;
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getActionMasked() == 0 && this.mLastTiggerTime + 1500 < System.currentTimeMillis()) {
            this.mStage = DetectingStage.WAITING;
        }
        if (exitWaiting(motionEvent)) {
            return true;
        }
        return super.onInterceptTouchEvent(motionEvent);
    }

    private boolean exitWaiting(MotionEvent motionEvent) {
        if (DetectingStage.WAITING != this.mStage || 5 != motionEvent.getActionMasked()) {
            return false;
        }
        if (getElapsedTime(motionEvent) < 200) {
            this.mStage = DetectingStage.MOVE_UP;
            this.mFirstY = getTrackingY(motionEvent);
            this.mBottomY = -1.0f;
            this.mTopY = 2.14748365E9f;
            prepare();
            return true;
        }
        this.mStage = DetectingStage.STOP;
        return true;
    }

    private boolean matchGesture(MotionEvent motionEvent) {
        if (this.mBottomY - this.mTopY < MOVE_DOWN_DISTANCE_THREDHOLD || getElapsedTime(motionEvent) > 1000) {
            this.mStage = DetectingStage.STOP;
            return false;
        }
        this.mStage = DetectingStage.MATCHED;
        this.mLastMatchTime = getElapsedTime(motionEvent);
        return true;
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (1 == motionEvent.getActionMasked()) {
            if (DetectingStage.MATCHED == this.mStage && getElapsedTime(motionEvent) - this.mLastMatchTime < 200) {
                trigger();
            }
            return true;
        } else if (exitWaiting(motionEvent)) {
            return true;
        } else {
            DetectingStage detectingStage = DetectingStage.MOVE_DOWN;
            DetectingStage detectingStage2 = this.mStage;
            if (detectingStage != detectingStage2 && DetectingStage.MOVE_UP != detectingStage2) {
                return true;
            }
            if (6 == motionEvent.getActionMasked()) {
                matchGesture(motionEvent);
                return true;
            } else if (motionEvent.getPointerCount() != 2) {
                this.mStage = DetectingStage.STOP;
                return true;
            } else if (Math.abs(motionEvent.getX(0) - motionEvent.getX(1)) > TWO_POINTS_DISTANCE_X_THREDHOLD || Math.abs(motionEvent.getY(0) - motionEvent.getY(1)) > TWO_POINTS_DISTANCE_Y_THREDHOLD || Math.abs(motionEvent.getY(0) - motionEvent.getY(1)) < TWO_POINTS_DISTANCE_Y_THREDHOLD_MIN) {
                this.mStage = DetectingStage.STOP;
                return true;
            } else {
                float trackingY = getTrackingY(motionEvent);
                if (DetectingStage.MOVE_UP == this.mStage) {
                    float f = this.mTopY;
                    if (f >= trackingY) {
                        this.mTopY = trackingY;
                    } else if (this.mFirstY - f < MOVE_UP_DISTANCE_THREDHOLD || getElapsedTime(motionEvent) > 2000) {
                        this.mStage = DetectingStage.STOP;
                        return true;
                    } else {
                        this.mStage = DetectingStage.MOVE_DOWN;
                    }
                } else if (this.mBottomY <= trackingY) {
                    this.mBottomY = trackingY;
                } else {
                    matchGesture(motionEvent);
                }
                return super.onTouchEvent(motionEvent);
            }
        }
    }

    private long getElapsedTime(MotionEvent motionEvent) {
        return motionEvent.getEventTime() - motionEvent.getDownTime();
    }

    private float getTrackingY(MotionEvent motionEvent) {
        return motionEvent.getY(0);
    }
}

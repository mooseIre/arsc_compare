package com.android.keyguard.injector;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.keyguard.Ease$Cubic;
import com.android.keyguard.MiuiKeyguardIndicationTextView;
import com.android.keyguard.charge.ChargeUtils;
import com.android.keyguard.charge.MiuiChargeController;
import com.android.keyguard.magazine.LockScreenMagazineController;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.phone.KeyguardIndicationTextView;
import com.miui.systemui.DeviceConfig;

public class KeyguardIndicationInjector {
    private ObjectAnimator mBottomButtonClickAnimator;
    private AsyncTask<?, ?, ?> mChargeAsyncTask;
    private int mChargeClickCount = 0;
    private long mChargeTextClickTime;
    private final Context mContext;
    private Animation mIndicationFromBottomAni;
    private ValueAnimator mIndicationTVAlphaAni;

    static /* synthetic */ int access$008(KeyguardIndicationInjector keyguardIndicationInjector) {
        int i = keyguardIndicationInjector.mChargeClickCount;
        keyguardIndicationInjector.mChargeClickCount = i + 1;
        return i;
    }

    public KeyguardIndicationInjector(Context context) {
        this.mContext = context;
    }

    public void handleEnterArrowAnimation(ImageView imageView, Handler handler) {
        if (imageView != null) {
            TranslateAnimation translateAnimation = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 2.0f, 1, 0.0f);
            Animation loadAnimation = AnimationUtils.loadAnimation(this.mContext, 17432576);
            AnimationSet animationSet = new AnimationSet(true);
            animationSet.addAnimation(translateAnimation);
            animationSet.addAnimation(loadAnimation);
            animationSet.setDuration((long) (DeviceConfig.getAnimationDurationRatio() * 500.0f));
            animationSet.setStartOffset(30);
            imageView.setVisibility(0);
            imageView.startAnimation(animationSet);
            handler.sendEmptyMessageDelayed(Integer.MAX_VALUE, 100);
        }
    }

    public void handleExitArrowAndTextAnimation(final ImageView imageView, KeyguardIndicationTextView keyguardIndicationTextView, Animation.AnimationListener animationListener) {
        if (imageView != null && keyguardIndicationTextView != null) {
            Animation loadAnimation = AnimationUtils.loadAnimation(this.mContext, 17432577);
            Animation loadAnimation2 = AnimationUtils.loadAnimation(this.mContext, 17432576);
            TranslateAnimation translateAnimation = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 0.0f, 1, -2.0f);
            TranslateAnimation translateAnimation2 = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 2.0f, 1, 0.0f);
            AnimationSet animationSet = new AnimationSet(true);
            AnimationSet animationSet2 = new AnimationSet(true);
            animationSet.addAnimation(loadAnimation);
            animationSet.addAnimation(translateAnimation);
            long j = (long) 500;
            animationSet.setDuration(j);
            animationSet2.addAnimation(loadAnimation2);
            animationSet2.addAnimation(translateAnimation2);
            animationSet2.setDuration(j);
            animationSet2.setStartOffset(100);
            animationSet.setAnimationListener(new Animation.AnimationListener(this) {
                /* class com.android.keyguard.injector.KeyguardIndicationInjector.AnonymousClass1 */

                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    imageView.setVisibility(4);
                }
            });
            animationSet2.setAnimationListener(animationListener);
            imageView.startAnimation(animationSet);
            keyguardIndicationTextView.startAnimation(animationSet2);
        }
    }

    public void setDoubleClickListener(final KeyguardIndicationTextView keyguardIndicationTextView) {
        keyguardIndicationTextView.setOnClickListener(new View.OnClickListener() {
            /* class com.android.keyguard.injector.KeyguardIndicationInjector.AnonymousClass2 */

            public void onClick(View view) {
                boolean isPreViewVisible = ((LockScreenMagazineController) Dependency.get(LockScreenMagazineController.class)).isPreViewVisible();
                boolean isPowerPluggedIn = ((KeyguardIndicationController) Dependency.get(KeyguardIndicationController.class)).isPowerPluggedIn();
                Log.i("KeyguardIndicationInjector", "onClick: mPowerPluggedIn " + isPowerPluggedIn + ";isMagazinePreViewVisibility=" + isPreViewVisible);
                if (isPowerPluggedIn && !isPreViewVisible) {
                    if (KeyguardIndicationInjector.this.mChargeClickCount == 0) {
                        KeyguardIndicationInjector.this.mChargeTextClickTime = System.currentTimeMillis();
                    }
                    KeyguardIndicationInjector.access$008(KeyguardIndicationInjector.this);
                    Log.i("KeyguardIndicationInjector", "onClick: mChargeClickCount " + KeyguardIndicationInjector.this.mChargeClickCount + ";time=" + (System.currentTimeMillis() - KeyguardIndicationInjector.this.mChargeTextClickTime));
                    if (KeyguardIndicationInjector.this.mChargeClickCount < 2) {
                        return;
                    }
                    if (System.currentTimeMillis() - KeyguardIndicationInjector.this.mChargeTextClickTime > 150 && System.currentTimeMillis() - KeyguardIndicationInjector.this.mChargeTextClickTime < 500) {
                        KeyguardIndicationInjector.this.mChargeClickCount = 0;
                        KeyguardIndicationInjector.this.mChargeTextClickTime = System.currentTimeMillis();
                        ((MiuiChargeController) Dependency.get(MiuiChargeController.class)).checkBatteryStatus(true);
                        keyguardIndicationTextView.setAlpha(0.0f);
                    } else if (System.currentTimeMillis() - KeyguardIndicationInjector.this.mChargeTextClickTime > 500) {
                        KeyguardIndicationInjector.this.mChargeClickCount = 1;
                        KeyguardIndicationInjector.this.mChargeTextClickTime = System.currentTimeMillis();
                    } else {
                        KeyguardIndicationInjector.this.mChargeClickCount = 0;
                    }
                }
            }
        });
    }

    @SuppressLint({"StaticFieldLeak"})
    public void updatePowerIndication(final boolean z, final KeyguardIndicationTextView keyguardIndicationTextView) {
        final boolean isPowerPluggedIn = ((KeyguardIndicationController) Dependency.get(KeyguardIndicationController.class)).isPowerPluggedIn();
        final int batteryLevel = ((KeyguardIndicationController) Dependency.get(KeyguardIndicationController.class)).getBatteryLevel();
        if (this.mChargeAsyncTask == null && isPowerPluggedIn && keyguardIndicationTextView != null) {
            this.mChargeAsyncTask = new AsyncTask<Void, Void, String>() {
                /* class com.android.keyguard.injector.KeyguardIndicationInjector.AnonymousClass3 */

                /* access modifiers changed from: protected */
                public void onPreExecute() {
                    keyguardIndicationTextView.setAlpha(0.0f);
                }

                /* access modifiers changed from: protected */
                public String doInBackground(Void... voidArr) {
                    return ChargeUtils.getChargingHintText(KeyguardIndicationInjector.this.mContext, isPowerPluggedIn, batteryLevel);
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(String str) {
                    Log.i("KeyguardIndicationInjector", "handleChargeTextAnimation: " + z + ";powerPluggedIn=" + isPowerPluggedIn);
                    if (!z) {
                        KeyguardIndicationInjector.this.handlePowerIndicationAnimation(keyguardIndicationTextView);
                    } else {
                        keyguardIndicationTextView.setAlpha(1.0f);
                    }
                    ((KeyguardIndicationController) Dependency.get(KeyguardIndicationController.class)).showMiuiPowerIndication(str);
                    KeyguardIndicationInjector.this.mChargeAsyncTask = null;
                }

                /* access modifiers changed from: protected */
                public void onCancelled() {
                    keyguardIndicationTextView.setAlpha(1.0f);
                    KeyguardIndicationInjector.this.mChargeAsyncTask = null;
                }
            }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    public void handlePowerIndicationAnimation(KeyguardIndicationTextView keyguardIndicationTextView) {
        ValueAnimator valueAnimator = this.mIndicationTVAlphaAni;
        if (valueAnimator != null && valueAnimator.isRunning()) {
            this.mIndicationTVAlphaAni.cancel();
        }
        Animation animation = this.mIndicationFromBottomAni;
        if (animation != null && animation.hasStarted()) {
            this.mIndicationFromBottomAni.cancel();
        }
        ValueAnimator ofFloat = ValueAnimator.ofFloat(0.0f, 1.0f);
        this.mIndicationTVAlphaAni = ofFloat;
        ofFloat.setInterpolator(new DecelerateInterpolator());
        this.mIndicationTVAlphaAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.injector.$$Lambda$KeyguardIndicationInjector$rvtW3oq3pC4W0s7s7NwIVUmWbuE */

            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                KeyguardIndicationInjector.lambda$handlePowerIndicationAnimation$0(KeyguardIndicationTextView.this, valueAnimator);
            }
        });
        this.mIndicationTVAlphaAni.setDuration(500L).start();
        TranslateAnimation translateAnimation = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 2.0f, 1, 0.0f);
        this.mIndicationFromBottomAni = translateAnimation;
        translateAnimation.setDuration(500);
        keyguardIndicationTextView.startAnimation(this.mIndicationFromBottomAni);
    }

    public void doIndicatorAnimation(boolean z, final TextView textView) {
        if (textView != null && (textView instanceof MiuiKeyguardIndicationTextView)) {
            ((MiuiKeyguardIndicationTextView) textView).setVisibilityForSwitchIndication(!z);
            ObjectAnimator objectAnimator = this.mBottomButtonClickAnimator;
            if (objectAnimator != null && objectAnimator.isRunning()) {
                this.mBottomButtonClickAnimator.cancel();
            }
            if (z) {
                textView.setVisibility(8);
                textView.setAlpha(0.0f);
                return;
            }
            ObjectAnimator ofFloat = ObjectAnimator.ofFloat(textView, View.ALPHA, 0.0f, 1.0f);
            this.mBottomButtonClickAnimator = ofFloat;
            ofFloat.setInterpolator(Ease$Cubic.easeInOut);
            this.mBottomButtonClickAnimator.addListener(new AnimatorListenerAdapter(this) {
                /* class com.android.keyguard.injector.KeyguardIndicationInjector.AnonymousClass4 */

                public void onAnimationStart(Animator animator) {
                    textView.setVisibility(0);
                }

                public void onAnimationEnd(Animator animator) {
                    textView.setAlpha(1.0f);
                }

                public void onAnimationCancel(Animator animator) {
                    super.onAnimationCancel(animator);
                    textView.setVisibility(8);
                    textView.setAlpha(1.0f);
                }
            });
            this.mBottomButtonClickAnimator.setDuration(800L);
            this.mBottomButtonClickAnimator.start();
        }
    }
}

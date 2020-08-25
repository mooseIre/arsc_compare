package com.android.keyguard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardSecurityModel;
import com.android.keyguard.fod.MiuiGxzwManager;
import com.android.keyguard.magazine.LockScreenMagazineUtils;
import com.android.systemui.plugins.R;

public class KeyguardSecurityContainer extends FrameLayout implements KeyguardSecurityView {
    /* access modifiers changed from: private */
    public KeyguardSecurityCallback mCallback;
    private KeyguardSecurityModel.SecurityMode mCurrentSecuritySelection;
    /* access modifiers changed from: private */
    public View mFogetPasswordMethod;
    /* access modifiers changed from: private */
    public View mFogetPasswordSuggestion;
    private TextView mForgetPasswordMethodBack;
    private TextView mForgetPasswordMethodNext;
    /* access modifiers changed from: private */
    public LockPatternUtils mLockPatternUtils;
    /* access modifiers changed from: private */
    public View mLockoutView;
    private KeyguardSecurityCallback mNullCallback;
    /* access modifiers changed from: private */
    public SecurityCallback mSecurityCallback;
    private KeyguardSecurityModel mSecurityModel;
    private KeyguardSecurityViewFlipper mSecurityViewFlipper;
    private final KeyguardUpdateMonitor mUpdateMonitor;

    public interface SecurityCallback {
        boolean dismiss(boolean z, int i);

        void finish(boolean z, int i);

        void onSecurityModeChanged(KeyguardSecurityModel.SecurityMode securityMode, boolean z);

        void reset();

        void userActivity();
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public KeyguardSecurityContainer(Context context) {
        this(context, (AttributeSet) null, 0);
    }

    public KeyguardSecurityContainer(Context context, AttributeSet attributeSet, int i) {
        super(new ContextThemeWrapper(context, 16974120), attributeSet, i);
        this.mCurrentSecuritySelection = KeyguardSecurityModel.SecurityMode.Invalid;
        this.mCallback = new KeyguardSecurityCallback() {
            public void userActivity() {
                if (KeyguardSecurityContainer.this.mSecurityCallback != null) {
                    KeyguardSecurityContainer.this.mSecurityCallback.userActivity();
                }
            }

            public void dismiss(boolean z, int i) {
                KeyguardSecurityContainer.this.mSecurityCallback.dismiss(z, i);
            }

            public void reportUnlockAttempt(int i, boolean z, int i2) {
                KeyguardUpdateMonitor instance = KeyguardUpdateMonitor.getInstance(KeyguardSecurityContainer.this.mContext);
                if (z) {
                    instance.clearFailedUnlockAttempts();
                    KeyguardSecurityContainer.this.mLockPatternUtils.reportSuccessfulPasswordAttempt(i);
                    return;
                }
                KeyguardSecurityContainer.this.reportFailedUnlockAttempt(i, i2);
            }

            public void handleAttemptLockout(long j) {
                KeyguardSecurityContainer.this.showLockoutView(j);
            }

            public void reset() {
                KeyguardSecurityContainer.this.mSecurityCallback.reset();
                LockScreenMagazineUtils.sendLockScreenMagazineEventBroadcast(KeyguardSecurityContainer.this.mContext, "Wallpaper_Uncovered");
            }
        };
        this.mNullCallback = new KeyguardSecurityCallback() {
            public void dismiss(boolean z, int i) {
            }

            public void handleAttemptLockout(long j) {
            }

            public void reportUnlockAttempt(int i, boolean z, int i2) {
            }

            public void reset() {
            }

            public void userActivity() {
            }
        };
        this.mSecurityModel = new KeyguardSecurityModel(context);
        this.mLockPatternUtils = new LockPatternUtils(context);
        this.mUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
    }

    public void setSecurityCallback(SecurityCallback securityCallback) {
        this.mSecurityCallback = securityCallback;
    }

    public boolean onBackPressed() {
        View view = this.mFogetPasswordSuggestion;
        if (!(view == null || this.mFogetPasswordMethod == null)) {
            if (view.getVisibility() == 0) {
                this.mFogetPasswordSuggestion.setVisibility(4);
                this.mFogetPasswordMethod.setVisibility(0);
                return true;
            } else if (this.mFogetPasswordMethod.getVisibility() == 0) {
                this.mFogetPasswordMethod.setVisibility(4);
                setLockoutViewVisible(0);
                return true;
            }
        }
        return false;
    }

    public void onResume(int i) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).onResume(i);
        }
    }

    public void onPause() {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).onPause();
        }
    }

    public void startAppearAnimation() {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).startAppearAnimation();
        }
    }

    public boolean startDisappearAnimation(Runnable runnable) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            return getSecurityView(securityMode).startDisappearAnimation(runnable);
        }
        return false;
    }

    public CharSequence getCurrentSecurityModeContentDescription() {
        View view = (View) getSecurityView(this.mCurrentSecuritySelection);
        return view != null ? view.getContentDescription() : "";
    }

    private KeyguardSecurityView getSecurityView(KeyguardSecurityModel.SecurityMode securityMode) {
        KeyguardSecurityView keyguardSecurityView;
        int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
        int childCount = this.mSecurityViewFlipper.getChildCount();
        int i = 0;
        while (true) {
            if (i >= childCount) {
                keyguardSecurityView = null;
                break;
            } else if (this.mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                keyguardSecurityView = (KeyguardSecurityView) this.mSecurityViewFlipper.getChildAt(i);
                break;
            } else {
                i++;
            }
        }
        int layoutIdFor = getLayoutIdFor(securityMode);
        if (keyguardSecurityView != null || layoutIdFor == 0) {
            return keyguardSecurityView;
        }
        LayoutInflater from = LayoutInflater.from(this.mContext);
        Log.v("KeyguardSecurityView", "inflating id = " + layoutIdFor);
        View inflate = from.inflate(layoutIdFor, this.mSecurityViewFlipper, false);
        this.mSecurityViewFlipper.addView(inflate);
        updateSecurityView(inflate);
        return (KeyguardSecurityView) inflate;
    }

    private void updateSecurityView(View view) {
        if (view instanceof KeyguardSecurityView) {
            KeyguardSecurityView keyguardSecurityView = (KeyguardSecurityView) view;
            keyguardSecurityView.setKeyguardCallback(this.mCallback);
            keyguardSecurityView.setLockPatternUtils(this.mLockPatternUtils);
            return;
        }
        Log.w("KeyguardSecurityView", "View " + view + " is not a KeyguardSecurityView");
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        KeyguardSecurityViewFlipper keyguardSecurityViewFlipper = (KeyguardSecurityViewFlipper) findViewById(R.id.view_flipper);
        this.mSecurityViewFlipper = keyguardSecurityViewFlipper;
        keyguardSecurityViewFlipper.setLockPatternUtils(this.mLockPatternUtils);
    }

    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
        this.mLockPatternUtils = lockPatternUtils;
        this.mSecurityModel.setLockPatternUtils(lockPatternUtils);
        this.mSecurityViewFlipper.setLockPatternUtils(this.mLockPatternUtils);
    }

    private void showDialog(String str, String str2) {
        AlertDialog create = new AlertDialog.Builder(this.mContext).setTitle(str).setMessage(str2).setCancelable(false).setNeutralButton(R.string.ok, (DialogInterface.OnClickListener) null).create();
        if (!(this.mContext instanceof Activity)) {
            create.getWindow().setType(2009);
        }
        create.show();
    }

    /* renamed from: com.android.keyguard.KeyguardSecurityContainer$10  reason: invalid class name */
    static /* synthetic */ class AnonymousClass10 {
        static final /* synthetic */ int[] $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|(3:13|14|16)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(16:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|16) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.android.keyguard.KeyguardSecurityModel$SecurityMode[] r0 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode = r0
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.Pattern     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.PIN     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.Password     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.Invalid     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x003e }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.None     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.SimPin     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.android.keyguard.KeyguardSecurityModel$SecurityMode r1 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.SimPuk     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardSecurityContainer.AnonymousClass10.<clinit>():void");
        }
    }

    private void showAlmostAtWipeDialog(int i, int i2, int i3) {
        String str;
        if (i3 == 1) {
            str = this.mContext.getString(R.string.kg_failed_attempts_almost_at_wipe, new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        } else if (i3 == 2) {
            str = this.mContext.getString(R.string.kg_failed_attempts_almost_at_erase_profile, new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        } else if (i3 != 3) {
            str = null;
        } else {
            str = this.mContext.getString(R.string.kg_failed_attempts_almost_at_erase_user, new Object[]{Integer.valueOf(i), Integer.valueOf(i2)});
        }
        showDialog((String) null, str);
    }

    private void showWipeDialog(int i, int i2) {
        String str;
        if (i2 == 1) {
            str = this.mContext.getString(R.string.kg_failed_attempts_now_wiping, new Object[]{Integer.valueOf(i)});
        } else if (i2 == 2) {
            str = this.mContext.getString(R.string.kg_failed_attempts_now_erasing_profile, new Object[]{Integer.valueOf(i)});
        } else if (i2 != 3) {
            str = null;
        } else {
            str = this.mContext.getString(R.string.kg_failed_attempts_now_erasing_user, new Object[]{Integer.valueOf(i)});
        }
        showDialog((String) null, str);
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x004a  */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x004e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void reportFailedUnlockAttempt(int r9, int r10) {
        /*
            r8 = this;
            android.content.Context r0 = r8.mContext
            com.android.keyguard.KeyguardUpdateMonitor r0 = com.android.keyguard.KeyguardUpdateMonitor.getInstance(r0)
            int r1 = r0.getFailedUnlockAttempts(r9)
            r2 = 1
            int r1 = r1 + r2
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "reportFailedPatternAttempt: #"
            r3.append(r4)
            r3.append(r1)
            java.lang.String r3 = r3.toString()
            java.lang.String r4 = "KeyguardSecurityView"
            android.util.Log.d(r4, r3)
            com.android.internal.widget.LockPatternUtils r3 = r8.mLockPatternUtils
            android.app.admin.DevicePolicyManager r3 = r3.getDevicePolicyManager()
            r5 = 0
            int r5 = r3.getMaximumFailedPasswordsForWipe(r5, r9)
            if (r5 <= 0) goto L_0x0031
            int r5 = r5 - r1
            goto L_0x0034
        L_0x0031:
            r5 = 2147483647(0x7fffffff, float:NaN)
        L_0x0034:
            r6 = 5
            if (r5 >= r6) goto L_0x006a
            int r3 = r3.getProfileWithMinimumFailedPasswordsForWipe(r9)
            if (r3 != r9) goto L_0x0041
            if (r3 == 0) goto L_0x0047
            r6 = 3
            goto L_0x0048
        L_0x0041:
            r6 = -10000(0xffffffffffffd8f0, float:NaN)
            if (r3 == r6) goto L_0x0047
            r6 = 2
            goto L_0x0048
        L_0x0047:
            r6 = r2
        L_0x0048:
            if (r5 <= 0) goto L_0x004e
            r8.showAlmostAtWipeDialog(r1, r5, r6)
            goto L_0x006a
        L_0x004e:
            java.lang.StringBuilder r5 = new java.lang.StringBuilder
            r5.<init>()
            java.lang.String r7 = "Too many unlock attempts; user "
            r5.append(r7)
            r5.append(r3)
            java.lang.String r3 = " will be wiped!"
            r5.append(r3)
            java.lang.String r3 = r5.toString()
            android.util.Slog.i(r4, r3)
            r8.showWipeDialog(r1, r6)
        L_0x006a:
            r0.reportFailedStrongAuthUnlockAttempt(r9)
            com.android.internal.widget.LockPatternUtils r0 = r8.mLockPatternUtils
            r0.reportFailedPasswordAttempt(r9)
            if (r10 <= 0) goto L_0x008a
            com.android.internal.widget.LockPatternUtils r0 = r8.mLockPatternUtils
            r0.reportPasswordLockout(r10, r9)
            long r9 = (long) r10
            r8.showLockoutView(r9)
            boolean r8 = com.android.keyguard.MiuiKeyguardUtils.isGxzwSensor()
            if (r8 == 0) goto L_0x008a
            com.android.keyguard.fod.MiuiGxzwManager r8 = com.android.keyguard.fod.MiuiGxzwManager.getInstance()
            r8.setUnlockLockout(r2)
        L_0x008a:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardSecurityContainer.reportFailedUnlockAttempt(int, int):void");
    }

    /* access modifiers changed from: protected */
    public void showLockoutView(long j) {
        View view = this.mLockoutView;
        if (view == null) {
            loadLockoutView();
        } else if (view.getVisibility() == 0) {
            return;
        }
        this.mLockoutView.setVisibility(0);
        final TextView textView = (TextView) this.mLockoutView.findViewById(R.id.phone_locked_timeout_id);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setDuration(500);
        this.mLockoutView.startAnimation(alphaAnimation);
        new CountDownTimer(((long) Math.ceil(((double) j) / 1000.0d)) * 1000, 1000) {
            public void onTick(long j) {
                KeyguardSecurityContainer.this.updateCountDown(textView, (long) ((int) Math.round(((double) j) / 1000.0d)));
            }

            public void onFinish() {
                KeyguardSecurityContainer.this.hideLockoutView();
            }
        }.start();
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().setShowLockoutView(true);
        }
    }

    /* access modifiers changed from: private */
    public void updateCountDown(TextView textView, long j) {
        if (j <= 60) {
            textView.setText(getResources().getQuantityString(R.plurals.phone_locked_timeout_seconds_string, (int) j, new Object[]{Long.valueOf(j)}));
            return;
        }
        textView.setText(getResources().getQuantityString(R.plurals.phone_locked_timeout_minutes_string, ((int) j) / 60, new Object[]{Long.valueOf(j / 60)}));
    }

    /* access modifiers changed from: protected */
    public void hideLockoutView() {
        hideLockoutView(true);
    }

    /* access modifiers changed from: protected */
    public void hideLockoutView(boolean z) {
        if (z) {
            Animation loadAnimation = AnimationUtils.loadAnimation(this.mContext, 17432577);
            loadAnimation.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    KeyguardSecurityContainer.this.mLockoutView.setVisibility(4);
                }
            });
            this.mLockoutView.startAnimation(loadAnimation);
        } else {
            this.mLockoutView.clearAnimation();
            this.mLockoutView.setVisibility(4);
        }
        if (MiuiKeyguardUtils.isGxzwSensor()) {
            MiuiGxzwManager.getInstance().setShowLockoutView(false);
        }
    }

    private void loadLockoutView() {
        View inflate = View.inflate(getContext(), R.layout.miui_unlockscreen_lockout, (ViewGroup) null);
        ((ViewGroup) getParent()).addView(inflate);
        View findViewById = inflate.findViewById(R.id.unlockscreen_lockout_id);
        this.mLockoutView = findViewById;
        Button button = (Button) findViewById.findViewById(R.id.foget_password);
        this.mFogetPasswordMethod = this.mLockoutView.findViewById(R.id.forget_password_hint_container);
        this.mFogetPasswordSuggestion = this.mLockoutView.findViewById(R.id.forget_password_suggesstion);
        this.mForgetPasswordMethodBack = (TextView) this.mFogetPasswordMethod.findViewById(R.id.forget_password_method_back);
        this.mForgetPasswordMethodNext = (TextView) this.mFogetPasswordMethod.findViewById(R.id.forget_password_method_next);
        if (getLayoutDirection() == 1) {
            this.mForgetPasswordMethodBack.setBackgroundResource(R.drawable.miui_keyguard_forget_password_suggestion_right);
            this.mForgetPasswordMethodNext.setBackgroundResource(R.drawable.miui_keyguard_forget_password_suggestion_left);
        }
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                KeyguardSecurityContainer.this.setLockoutViewVisible(4);
                KeyguardSecurityContainer.this.mFogetPasswordMethod.setVisibility(0);
                ((TextView) KeyguardSecurityContainer.this.mFogetPasswordMethod.findViewById(R.id.forget_password_method_content)).setText(Html.fromHtml(KeyguardSecurityContainer.this.getResources().getString(R.string.phone_locked_foget_password_method_content)));
            }
        });
        this.mLockoutView.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() != 0) {
                    return true;
                }
                KeyguardSecurityContainer.this.mCallback.userActivity();
                return true;
            }
        });
        this.mForgetPasswordMethodNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                KeyguardSecurityContainer.this.mFogetPasswordMethod.setVisibility(4);
                KeyguardSecurityContainer.this.setLockoutViewVisible(4);
                KeyguardSecurityContainer.this.mFogetPasswordSuggestion.setVisibility(0);
                ((TextView) KeyguardSecurityContainer.this.mFogetPasswordSuggestion.findViewById(R.id.forget_password_suggesstion_one)).setText(Html.fromHtml(KeyguardSecurityContainer.this.getResources().getString(R.string.phone_locked_forget_password_suggesstion_one_content), new Html.ImageGetter() {
                    public Drawable getDrawable(String str) {
                        if (str == null) {
                            return null;
                        }
                        Drawable drawable = KeyguardSecurityContainer.this.getResources().getDrawable(R.drawable.miui_keyguard_forget_password_mi);
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                        return drawable;
                    }
                }, (Html.TagHandler) null));
                ((TextView) KeyguardSecurityContainer.this.mFogetPasswordSuggestion.findViewById(R.id.forget_password_suggesstion_two)).setText(Html.fromHtml(KeyguardSecurityContainer.this.getResources().getString(R.string.phone_locked_forget_password_suggesstion_two_content)));
            }
        });
        this.mForgetPasswordMethodBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                KeyguardSecurityContainer.this.mFogetPasswordMethod.setVisibility(4);
                KeyguardSecurityContainer.this.setLockoutViewVisible(0);
            }
        });
        this.mFogetPasswordSuggestion.findViewById(R.id.forget_password_suggesstion_ok).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                KeyguardSecurityContainer.this.mFogetPasswordSuggestion.setVisibility(4);
                KeyguardSecurityContainer.this.mFogetPasswordMethod.setVisibility(4);
                KeyguardSecurityContainer.this.setLockoutViewVisible(0);
            }
        });
    }

    /* access modifiers changed from: private */
    public void setLockoutViewVisible(int i) {
        this.mLockoutView.findViewById(R.id.phone_locked_textview).setVisibility(i);
        this.mLockoutView.findViewById(R.id.phone_locked_timeout_id).setVisibility(i);
        this.mLockoutView.findViewById(R.id.foget_password).setVisibility(i);
    }

    /* access modifiers changed from: package-private */
    public void showPrimarySecurityScreen(boolean z) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser());
        Log.v("KeyguardSecurityView", "showPrimarySecurityScreen(turningOff=" + z + ")");
        showSecurityScreen(securityMode);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0097  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean showNextSecurityScreenOrFinish(boolean r7, int r8) {
        /*
            r6 = this;
            java.lang.StringBuilder r0 = new java.lang.StringBuilder
            r0.<init>()
            java.lang.String r1 = "showNextSecurityScreenOrFinish("
            r0.append(r1)
            r0.append(r7)
            java.lang.String r1 = ")"
            r0.append(r1)
            java.lang.String r0 = r0.toString()
            java.lang.String r1 = "KeyguardSecurityView"
            android.util.Log.d(r1, r0)
            com.android.keyguard.KeyguardUpdateMonitor r0 = r6.mUpdateMonitor
            boolean r0 = r0.getUserCanSkipBouncer(r8)
            r2 = 0
            r3 = 1
            if (r0 == 0) goto L_0x002b
        L_0x0026:
            r5 = r3
            r3 = r2
            r2 = r5
            goto L_0x0095
        L_0x002b:
            com.android.keyguard.KeyguardSecurityModel$SecurityMode r0 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.None
            com.android.keyguard.KeyguardSecurityModel$SecurityMode r4 = r6.mCurrentSecuritySelection
            if (r0 != r4) goto L_0x0041
            com.android.keyguard.KeyguardSecurityModel r7 = r6.mSecurityModel
            com.android.keyguard.KeyguardSecurityModel$SecurityMode r7 = r7.getSecurityMode(r8)
            com.android.keyguard.KeyguardSecurityModel$SecurityMode r0 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.None
            if (r0 != r7) goto L_0x003c
            goto L_0x0026
        L_0x003c:
            r6.showSecurityScreen(r7)
            r3 = r2
            goto L_0x0026
        L_0x0041:
            if (r7 == 0) goto L_0x0094
            int[] r7 = com.android.keyguard.KeyguardSecurityContainer.AnonymousClass10.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode
            int r0 = r4.ordinal()
            r7 = r7[r0]
            if (r7 == r3) goto L_0x0092
            r0 = 2
            if (r7 == r0) goto L_0x0092
            r0 = 3
            if (r7 == r0) goto L_0x0092
            r0 = 6
            if (r7 == r0) goto L_0x0078
            r0 = 7
            if (r7 == r0) goto L_0x0078
            java.lang.StringBuilder r7 = new java.lang.StringBuilder
            r7.<init>()
            java.lang.String r0 = "Bad security screen "
            r7.append(r0)
            com.android.keyguard.KeyguardSecurityModel$SecurityMode r0 = r6.mCurrentSecuritySelection
            r7.append(r0)
            java.lang.String r0 = ", fail safe"
            r7.append(r0)
            java.lang.String r7 = r7.toString()
            android.util.Log.v(r1, r7)
            r6.showPrimarySecurityScreen(r2)
            goto L_0x0094
        L_0x0078:
            com.android.keyguard.KeyguardSecurityModel r7 = r6.mSecurityModel
            com.android.keyguard.KeyguardSecurityModel$SecurityMode r7 = r7.getSecurityMode(r8)
            com.android.keyguard.KeyguardSecurityModel$SecurityMode r0 = com.android.keyguard.KeyguardSecurityModel.SecurityMode.None
            if (r7 != r0) goto L_0x008e
            com.android.internal.widget.LockPatternUtils r0 = r6.mLockPatternUtils
            int r1 = com.android.keyguard.KeyguardUpdateMonitor.getCurrentUser()
            boolean r0 = r0.isLockScreenDisabled(r1)
            if (r0 != 0) goto L_0x0026
        L_0x008e:
            r6.showSecurityScreen(r7)
            goto L_0x0094
        L_0x0092:
            r2 = r3
            goto L_0x0095
        L_0x0094:
            r3 = r2
        L_0x0095:
            if (r2 == 0) goto L_0x009c
            com.android.keyguard.KeyguardSecurityContainer$SecurityCallback r6 = r6.mSecurityCallback
            r6.finish(r3, r8)
        L_0x009c:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.keyguard.KeyguardSecurityContainer.showNextSecurityScreenOrFinish(boolean, int):boolean");
    }

    private void showSecurityScreen(KeyguardSecurityModel.SecurityMode securityMode) {
        Log.d("KeyguardSecurityView", "showSecurityScreen(" + securityMode + ")");
        KeyguardSecurityModel.SecurityMode securityMode2 = this.mCurrentSecuritySelection;
        if (securityMode != securityMode2) {
            KeyguardSecurityView securityView = getSecurityView(securityMode2);
            KeyguardSecurityView securityView2 = getSecurityView(securityMode);
            if (securityView != null) {
                securityView.onPause();
                securityView.setKeyguardCallback(this.mNullCallback);
            }
            if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
                securityView2.onResume(2);
                securityView2.setKeyguardCallback(this.mCallback);
            }
            int childCount = this.mSecurityViewFlipper.getChildCount();
            int securityViewIdForMode = getSecurityViewIdForMode(securityMode);
            boolean z = false;
            int i = 0;
            while (true) {
                if (i >= childCount) {
                    break;
                } else if (this.mSecurityViewFlipper.getChildAt(i).getId() == securityViewIdForMode) {
                    this.mSecurityViewFlipper.setDisplayedChild(i);
                    break;
                } else {
                    i++;
                }
            }
            this.mCurrentSecuritySelection = securityMode;
            SecurityCallback securityCallback = this.mSecurityCallback;
            if (securityMode != KeyguardSecurityModel.SecurityMode.None && securityView2.needsInput()) {
                z = true;
            }
            securityCallback.onSecurityModeChanged(securityMode, z);
        }
    }

    private int getSecurityViewIdForMode(KeyguardSecurityModel.SecurityMode securityMode) {
        int i = AnonymousClass10.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[securityMode.ordinal()];
        if (i == 1) {
            return R.id.keyguard_pattern_view;
        }
        if (i == 2) {
            return R.id.keyguard_pin_view;
        }
        if (i == 3) {
            return R.id.keyguard_password_view;
        }
        if (i == 6) {
            return R.id.keyguard_sim_pin_view;
        }
        if (i != 7) {
            return 0;
        }
        return R.id.keyguard_sim_puk_view;
    }

    /* access modifiers changed from: protected */
    public int getLayoutIdFor(KeyguardSecurityModel.SecurityMode securityMode) {
        int i = AnonymousClass10.$SwitchMap$com$android$keyguard$KeyguardSecurityModel$SecurityMode[securityMode.ordinal()];
        if (i == 1) {
            return R.layout.keyguard_pattern_view;
        }
        if (i == 2) {
            return R.layout.keyguard_pin_view;
        }
        if (i == 3) {
            return R.layout.keyguard_password_view;
        }
        if (i == 6) {
            return R.layout.keyguard_sim_pin_view;
        }
        if (i != 7) {
            return 0;
        }
        return R.layout.keyguard_sim_puk_view;
    }

    public KeyguardSecurityModel.SecurityMode getSecurityMode() {
        return this.mSecurityModel.getSecurityMode(KeyguardUpdateMonitor.getCurrentUser());
    }

    public KeyguardSecurityModel.SecurityMode getCurrentSecurityMode() {
        return this.mCurrentSecuritySelection;
    }

    public boolean needsInput() {
        return this.mSecurityViewFlipper.needsInput();
    }

    public void setKeyguardCallback(KeyguardSecurityCallback keyguardSecurityCallback) {
        this.mSecurityViewFlipper.setKeyguardCallback(keyguardSecurityCallback);
    }

    public void showPromptReason(int i) {
        if (this.mCurrentSecuritySelection != KeyguardSecurityModel.SecurityMode.None) {
            if (i != 0) {
                Log.i("KeyguardSecurityView", "Strong auth required, reason: " + i);
            }
            getSecurityView(this.mCurrentSecuritySelection).showPromptReason(i);
        }
    }

    public void showMessage(String str, String str2, int i) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).showMessage(str, str2, i);
        }
    }

    public void applyHintAnimation(long j) {
        KeyguardSecurityModel.SecurityMode securityMode = this.mCurrentSecuritySelection;
        if (securityMode != KeyguardSecurityModel.SecurityMode.None) {
            getSecurityView(securityMode).applyHintAnimation(j);
        }
    }
}

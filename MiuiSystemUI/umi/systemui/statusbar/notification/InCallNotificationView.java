package com.android.systemui.statusbar.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.SubscriptionManagerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.Constants;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.R;
import com.android.systemui.statusbar.CallStateController;
import miui.telephony.TelephonyManager;

public class InCallNotificationView extends LinearLayout {
    private static final boolean DEBUG = Constants.DEBUG;
    private static final int[] SIM_ICONS = {R.drawable.stat_sys_sim_card_1, R.drawable.stat_sys_sim_card_2};
    private ImageView mAnswerIcon;
    private Chronometer mAutoDisconnTip;
    /* access modifiers changed from: private */
    public PendingIntent mBubbleAnswerClickIntent;
    private ImageView mBubbleAnswerIcon;
    private TextView mCallerInfo;
    private TextView mCallerName;
    private ImageView mCallerSim;
    private boolean mCanBubbleAnswer;
    /* access modifiers changed from: private */
    public Context mContext;
    private ImageView mEndCallIcon;
    /* access modifiers changed from: private */
    public InCallCallback mInCallCallback;
    private boolean mIsVideoCall;
    private int mSubId;
    private TelephonyManager mTelephonyManager = TelephonyManager.getDefault();

    public interface InCallCallback {
        void onAnswerCall();

        void onBubbleAnswerCall();

        void onEndCall();

        void onExitCall();

        void onInCallNotificationHide();

        void onInCallNotificationShow();
    }

    public InCallNotificationView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setClipChildren(false);
        setClipToPadding(false);
        this.mContext = context;
    }

    public void updateInfo(View view, Bundle bundle) {
        if (view != null) {
            TextView textView = (TextView) view.findViewById(16908310);
            TextView textView2 = (TextView) view.findViewById(16909540);
            String str = "";
            this.mCallerName.setText(textView == null ? str : textView.getText().toString());
            TextView textView3 = this.mCallerInfo;
            if (textView2 != null) {
                str = textView2.getText().toString();
            }
            textView3.setText(str);
        }
        int i = -1;
        if (bundle != null) {
            bundle.getString("phoneNumber");
            this.mSubId = bundle.getInt("subId", -1);
            this.mIsVideoCall = bundle.getBoolean("isVideoCall", false);
            boolean z = bundle.getBoolean("bubble_answer", false);
            this.mCanBubbleAnswer = z;
            if (z) {
                this.mBubbleAnswerClickIntent = (PendingIntent) bundle.getParcelable("bubble_answer_intent");
            }
            this.mBubbleAnswerIcon.setVisibility((!this.mCanBubbleAnswer || !(getResources().getConfiguration().orientation == 2)) ? 8 : 0);
            setupAutoDisconnChronometer(bundle.getInt("autoDisconnectTimer", 0));
        }
        CallStateController callStateController = (CallStateController) Dependency.get(CallStateController.class);
        if (!Constants.IS_CUST_SINGLE_SIM && callStateController.isMsim()) {
            i = SubscriptionManagerCompat.getSlotIndex(this.mSubId);
        }
        if (i >= 0) {
            this.mCallerSim.setVisibility(0);
            this.mCallerSim.setImageResource(SIM_ICONS[i]);
            this.mCallerSim.setContentDescription(getResources().getString(R.string.description_image_icon_sim_card, new Object[]{Integer.valueOf(i + 1)}));
        } else {
            this.mCallerSim.setVisibility(8);
        }
        updateAnswerIcon();
    }

    private void setupAutoDisconnChronometer(int i) {
        if (i > 0) {
            this.mAutoDisconnTip.setVisibility(0);
            this.mAutoDisconnTip.setBase(SystemClock.elapsedRealtime() + ((long) i));
            this.mAutoDisconnTip.start();
            return;
        }
        this.mAutoDisconnTip.setVisibility(8);
    }

    /* access modifiers changed from: protected */
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        int i = 0;
        boolean z = configuration.orientation == 2;
        ImageView imageView = this.mBubbleAnswerIcon;
        if (!this.mCanBubbleAnswer || !z) {
            i = 8;
        }
        imageView.setVisibility(i);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        this.mCallerName = (TextView) findViewById(R.id.caller_name);
        this.mCallerInfo = (TextView) findViewById(R.id.caller_Info);
        this.mCallerSim = (ImageView) findViewById(R.id.caller_sim);
        this.mEndCallIcon = (ImageView) findViewById(R.id.end_call_icon);
        this.mAnswerIcon = (ImageView) findViewById(R.id.answer_icon);
        this.mBubbleAnswerIcon = (ImageView) findViewById(R.id.bubble_answer_icon);
        initAutoDisconnChronometer();
        this.mEndCallIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (InCallNotificationView.this.mInCallCallback != null) {
                    InCallNotificationView.this.mInCallCallback.onEndCall();
                }
            }
        });
        this.mBubbleAnswerIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (InCallNotificationView.this.mBubbleAnswerClickIntent != null) {
                    try {
                        InCallNotificationView.this.mBubbleAnswerClickIntent.send();
                        if (InCallNotificationView.this.mInCallCallback != null) {
                            InCallNotificationView.this.mInCallCallback.onBubbleAnswerCall();
                        }
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        this.mAnswerIcon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (InCallNotificationView.this.mInCallCallback != null) {
                    InCallNotificationView.this.mInCallCallback.onAnswerCall();
                }
            }
        });
        setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (InCallNotificationView.this.mInCallCallback != null) {
                    InCallNotificationView.this.mInCallCallback.onExitCall();
                }
            }
        });
    }

    private void initAutoDisconnChronometer() {
        Chronometer chronometer = (Chronometer) findViewById(R.id.auto_disconnect_tip);
        this.mAutoDisconnTip = chronometer;
        chronometer.setCountDown(true);
        this.mAutoDisconnTip.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            public void onChronometerTick(Chronometer chronometer) {
                int max = (int) (Math.max(chronometer.getBase() - SystemClock.elapsedRealtime(), 0) / 1000);
                chronometer.setText(InCallNotificationView.this.mContext.getResources().getQuantityString(R.plurals.auto_disconnect_timer_tip, max, new Object[]{Integer.valueOf(max)}));
            }
        });
    }

    public void show() {
        if (DEBUG) {
            Log.d("InCallNotificationView", "show()");
        }
        setVisibility(0);
        InCallCallback inCallCallback = this.mInCallCallback;
        if (inCallCallback != null) {
            inCallCallback.onInCallNotificationShow();
        }
    }

    public void hide() {
        if (DEBUG) {
            Log.d("InCallNotificationView", "hide()");
        }
        setVisibility(8);
        InCallCallback inCallCallback = this.mInCallCallback;
        if (inCallCallback != null) {
            inCallCallback.onInCallNotificationHide();
        }
    }

    private void updateAnswerIcon() {
        if (this.mTelephonyManager.getCallState() == 0) {
            return;
        }
        if (this.mIsVideoCall) {
            this.mAnswerIcon.setImageResource(R.drawable.video_answer_icon);
        } else {
            this.mAnswerIcon.setImageResource(R.drawable.answer_icon);
        }
    }

    public void setInCallCallback(InCallCallback inCallCallback) {
        this.mInCallCallback = inCallCallback;
    }
}

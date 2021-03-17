package com.android.keyguard.widget;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.systemui.C0007R$anim;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0013R$drawable;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import java.util.ArrayList;
import java.util.Iterator;

public class MiuiKeyBoardView extends FrameLayout implements View.OnClickListener, View.OnTouchListener {
    private static final float[][] LETTER_SIZE_GROUP = {new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f}, new float[]{1.6f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.6f}, new float[]{1.6f, 1.6f, 1.0f, 2.2f, 1.0f, 3.4f}};
    private static final float[][] NUMBER_SIZE_GROUP = {new float[]{3.8f, 3.8f, 3.8f}, new float[]{3.8f, 3.8f, 3.8f}, new float[]{3.8f, 3.8f, 3.8f}, new float[]{3.8f, 3.8f, 3.8f}};
    private static final float[][] SYMBOL_SIZE_GROUP = {new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f}, new float[]{1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f}, new float[]{1.6f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.6f}, new float[]{3.4f, 4.6f, 3.4f}};
    private ArrayList<KeyButton> mAllKeys;
    private View mBtnBack;
    private View mBtnCapsLock;
    private View mBtnLetterDelete;
    private View mBtnLetterOK;
    private View mBtnLetterSpace;
    private View mBtnNumberDelete;
    private View mBtnSymbolDelete;
    private View mBtnSymbolLock;
    private View mBtnSymbolOK;
    private View mBtnSymbolSpace;
    private View mBtnToLetterBoard;
    private View mBtnToNumberBoard;
    private View mBtnToSymbolBoard;
    private Runnable mConfirmHide;
    private Context mContext;
    private boolean mIsShowingPreview;
    private boolean mIsSymbolLock;
    private boolean mIsUpperMode;
    private ArrayList<OnKeyboardActionListener> mKeyboardListeners;
    private FrameLayout mLetterBoard;
    private FrameLayout mNumberBoard;
    private int mPaddingLeft;
    private int mPaddingTop;
    private int mPopupViewHeight;
    private int mPopupViewWidth;
    private int mPopupViewX;
    private int mPopupViewY;
    private TextView mPreviewText;
    private final Runnable mSendDeleteActionRunnable;
    private ValueAnimator mShowPreviewAnimator;
    private long mShowPreviewLastTime;
    private FrameLayout mSymbolBoard;

    public interface OnKeyboardActionListener {
        void onKeyBoardDelete();

        void onKeyBoardOK();

        void onText(CharSequence charSequence);
    }

    public MiuiKeyBoardView(Context context) {
        this(context, null);
    }

    public MiuiKeyBoardView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, -1);
    }

    public MiuiKeyBoardView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mAllKeys = new ArrayList<>();
        this.mIsUpperMode = false;
        this.mIsSymbolLock = false;
        this.mIsShowingPreview = false;
        this.mShowPreviewLastTime = 0;
        this.mShowPreviewAnimator = new ValueAnimator();
        this.mSendDeleteActionRunnable = new Runnable() {
            /* class com.android.keyguard.widget.MiuiKeyBoardView.AnonymousClass1 */

            public void run() {
                MiuiKeyBoardView.this.onKeyBoardDelete();
                MiuiKeyBoardView.this.postDelayed(this, 50);
            }
        };
        this.mConfirmHide = new Runnable() {
            /* class com.android.keyguard.widget.MiuiKeyBoardView.AnonymousClass2 */

            public void run() {
                MiuiKeyBoardView.this.showPreviewAnim(false);
            }
        };
        this.mContext = context;
        View.inflate(context, C0017R$layout.keyboard_letter_board, this);
        View.inflate(this.mContext, C0017R$layout.keyboard_symbol_board, this);
        View.inflate(this.mContext, C0017R$layout.keyboard_number_board, this);
        View.inflate(this.mContext, C0017R$layout.keyboard_key_preview_text, this);
        setFocusableInTouchMode(true);
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        if (getParent() != null) {
            ((ViewGroup) getParent()).setClipChildren(false);
        }
        super.onAttachedToWindow();
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        Resources resources = this.mContext.getResources();
        this.mPaddingTop = resources.getDimensionPixelSize(C0012R$dimen.keyboard_padding_top);
        this.mPaddingLeft = resources.getDimensionPixelSize(C0012R$dimen.keyboard_padding_left);
        AnimationUtils.loadAnimation(getContext(), C0007R$anim.stretch_from_bottom);
        AnimationUtils.loadAnimation(getContext(), C0007R$anim.shrink_to_bottom);
        this.mKeyboardListeners = new ArrayList<>();
        setClipChildren(false);
        setClipToPadding(false);
        this.mPreviewText = (TextView) findViewById(C0015R$id.preview_text);
        FrameLayout frameLayout = (FrameLayout) findViewById(C0015R$id.keyboard_letter);
        this.mLetterBoard = frameLayout;
        frameLayout.setVisibility(0);
        this.mBtnCapsLock = findViewById(C0015R$id.btn_caps_lock);
        this.mBtnLetterDelete = findViewById(C0015R$id.btn_letter_delete);
        this.mBtnToSymbolBoard = findViewById(C0015R$id.btn_shift2symbol);
        this.mBtnToNumberBoard = findViewById(C0015R$id.btn_shift2number);
        this.mBtnLetterSpace = findViewById(C0015R$id.btn_letter_space);
        this.mBtnLetterOK = findViewById(C0015R$id.btn_letter_ok);
        FrameLayout frameLayout2 = (FrameLayout) findViewById(C0015R$id.keyboard_symbol);
        this.mSymbolBoard = frameLayout2;
        frameLayout2.setVisibility(4);
        this.mBtnSymbolDelete = findViewById(C0015R$id.btn_symbol_delete);
        this.mBtnSymbolLock = findViewById(C0015R$id.btn_symbol_lock);
        this.mBtnToLetterBoard = findViewById(C0015R$id.btn_shift2letter);
        this.mBtnSymbolSpace = findViewById(C0015R$id.btn_symbol_space);
        this.mBtnSymbolOK = findViewById(C0015R$id.btn_symbol_ok);
        FrameLayout frameLayout3 = (FrameLayout) findViewById(C0015R$id.keyboard_number);
        this.mNumberBoard = frameLayout3;
        frameLayout3.setVisibility(4);
        this.mBtnNumberDelete = findViewById(C0015R$id.btn_number_delete);
        this.mBtnBack = findViewById(C0015R$id.btn_back);
        setOnTouchAndClickListenerForKey(this.mLetterBoard);
        setOnTouchAndClickListenerForKey(this.mSymbolBoard);
        setOnTouchAndClickListenerForKey(this.mNumberBoard);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        super.onTouchEvent(motionEvent);
        return true;
    }

    private void setOnTouchAndClickListenerForKey(ViewGroup viewGroup) {
        int childCount = viewGroup.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = viewGroup.getChildAt(i);
            if (childAt instanceof KeyButton) {
                childAt.setOnClickListener(this);
                childAt.setOnTouchListener(this);
                this.mAllKeys.add((KeyButton) childAt);
            } else if (childAt instanceof ViewGroup) {
                setOnTouchAndClickListenerForKey((ViewGroup) childAt);
            }
        }
    }

    private float getChildCoordRelativeToKeyboard(View view, float[] fArr, boolean z, boolean z2) {
        fArr[1] = 0.0f;
        fArr[0] = 0.0f;
        if (z) {
            view.getMatrix().mapPoints(fArr);
        }
        float scaleX = view.getScaleX() * 1.0f;
        fArr[0] = fArr[0] + ((float) view.getLeft());
        fArr[1] = fArr[1] + ((float) view.getTop());
        ViewParent parent = view.getParent();
        while ((parent instanceof View) && parent != this) {
            View view2 = (View) parent;
            if (z) {
                view2.getMatrix().mapPoints(fArr);
                scaleX *= view2.getScaleX();
            }
            fArr[0] = fArr[0] + ((float) (view2.getLeft() - view2.getScrollX()));
            fArr[1] = fArr[1] + ((float) (view2.getTop() - view2.getScrollY()));
            parent = view2.getParent();
        }
        if (z2) {
            float f = 1.0f - scaleX;
            fArr[0] = fArr[0] - ((((float) view.getWidth()) * f) / 2.0f);
            fArr[1] = fArr[1] - ((((float) view.getHeight()) * f) / 2.0f);
        }
        return scaleX;
    }

    @SuppressLint({"AppCompatCustomView"})
    public static class KeyButton extends TextView {
        public KeyButton(Context context) {
            super(context);
        }

        public KeyButton(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
        }

        public KeyButton(Context context, AttributeSet attributeSet, int i) {
            super(context, attributeSet, i);
        }

        /* access modifiers changed from: protected */
        public void onFinishInflate() {
            if (getTag() instanceof String) {
                setText((String) getTag());
            }
            super.onFinishInflate();
        }

        public void layout(int i, int i2, int i3, int i4) {
            measure(View.MeasureSpec.makeMeasureSpec(i3 - i, 1073741824), View.MeasureSpec.makeMeasureSpec(i4 - i2, 1073741824));
            super.layout(i, i2, i3, i4);
        }
    }

    public void addKeyboardListener(OnKeyboardActionListener onKeyboardActionListener) {
        Iterator<OnKeyboardActionListener> it = this.mKeyboardListeners.iterator();
        while (it.hasNext()) {
            if (onKeyboardActionListener.equals(it.next())) {
                return;
            }
        }
        this.mKeyboardListeners.add(onKeyboardActionListener);
    }

    private void onText(CharSequence charSequence) {
        Iterator<OnKeyboardActionListener> it = this.mKeyboardListeners.iterator();
        while (it.hasNext()) {
            it.next().onText(charSequence);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void onKeyBoardDelete() {
        Iterator<OnKeyboardActionListener> it = this.mKeyboardListeners.iterator();
        while (it.hasNext()) {
            it.next().onKeyBoardDelete();
        }
    }

    private void onKeyBoardOK() {
        Iterator<OnKeyboardActionListener> it = this.mKeyboardListeners.iterator();
        while (it.hasNext()) {
            it.next().onKeyBoardOK();
        }
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (!isEnabled()) {
            return false;
        }
        int action = motionEvent.getAction();
        if (action == 0) {
            if ((view.getTag() instanceof String) && ((String) view.getTag()).length() == 1) {
                calcAndStartShowPreviewAnim(view);
            }
            if (view != this.mBtnLetterDelete && view != this.mBtnSymbolDelete) {
                return false;
            }
            postDelayed(this.mSendDeleteActionRunnable, 500);
            return false;
        } else if (action != 1 && action != 3) {
            return false;
        } else {
            long currentTimeMillis = 300 - (System.currentTimeMillis() - this.mShowPreviewLastTime);
            if (this.mIsShowingPreview) {
                Runnable runnable = this.mConfirmHide;
                if (currentTimeMillis <= 0) {
                    currentTimeMillis = 0;
                }
                postDelayed(runnable, currentTimeMillis);
            }
            if (view != this.mBtnLetterDelete && view != this.mBtnSymbolDelete) {
                return false;
            }
            removeCallbacks(this.mSendDeleteActionRunnable);
            return false;
        }
    }

    public void onClick(View view) {
        if (isEnabled()) {
            if (view == this.mBtnCapsLock) {
                shiftLetterBoard();
            } else if (view == this.mBtnSymbolLock) {
                shiftSymbolLock();
            } else if (view == this.mBtnToSymbolBoard) {
                showLetterBoard(false);
            } else if (view == this.mBtnToLetterBoard) {
                showLetterBoard(true);
            } else if (view == this.mBtnToNumberBoard) {
                showNumberBoard();
            } else if (view == this.mBtnBack) {
                showLetterBoard(true);
            } else if (view == this.mBtnLetterDelete || view == this.mBtnSymbolDelete || view == this.mBtnNumberDelete) {
                onKeyBoardDelete();
            } else if (view == this.mBtnSymbolOK || view == this.mBtnLetterOK) {
                onKeyBoardOK();
            } else if (view == this.mBtnSymbolSpace || view == this.mBtnLetterSpace) {
                onText(" ");
            } else {
                onText(((KeyButton) view).getText());
                if (!this.mIsSymbolLock && this.mSymbolBoard.getVisibility() == 0) {
                    showLetterBoard(true);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5 = i3 - i;
        int i6 = i4 - i2;
        float[][] fArr = LETTER_SIZE_GROUP;
        int length = (int) (((float) (((i5 - (this.mPaddingLeft * 2)) / fArr[0].length) * 1)) / 1.14f);
        int i7 = (int) (((float) length) * 0.14f);
        int length2 = (int) (((float) (((i6 - (this.mPaddingTop * 2)) / fArr.length) * 1)) / 1.13f);
        int i8 = (int) (((float) length2) * 0.14f);
        this.mLetterBoard.layout(0, 0, i5, i6);
        this.mSymbolBoard.layout(0, 0, i5, i6);
        this.mNumberBoard.layout(0, 0, i5, i6);
        keyboardOnLayout(this.mLetterBoard, i5, length, i7, length2, i8, LETTER_SIZE_GROUP);
        keyboardOnLayout(this.mSymbolBoard, i5, length, i7, length2, i8, SYMBOL_SIZE_GROUP);
        float[][] fArr2 = NUMBER_SIZE_GROUP;
        keyboardOnLayout(this.mNumberBoard, i5, length, i7, (int) (((float) (((i6 - (this.mPaddingTop * 2)) / fArr2.length) * 1)) / 1.13f), i8, fArr2);
        TextView textView = this.mPreviewText;
        int i9 = this.mPopupViewX;
        int i10 = this.mPopupViewY;
        textView.layout(i9, i10, this.mPopupViewWidth + i9, this.mPopupViewHeight + i10);
    }

    /* access modifiers changed from: package-private */
    public void keyboardOnLayout(ViewGroup viewGroup, int i, int i2, int i3, int i4, int i5, float[][] fArr) {
        int i6 = i2;
        int length = fArr.length;
        int i7 = this.mPaddingTop;
        int i8 = 0;
        int i9 = 0;
        while (i8 < length) {
            float[] fArr2 = fArr[i8];
            float f = 0.0f;
            for (float f2 : fArr2) {
                f += f2 * ((float) i6);
            }
            int length2 = (int) ((((float) i) - (f + ((float) ((fArr2.length - 1) * i3)))) / 2.0f);
            int i10 = 0;
            while (i10 < fArr2.length) {
                KeyButton keyButton = (KeyButton) viewGroup.getChildAt(i9);
                int i11 = "!".equals(keyButton.getText()) ? (int) (((float) length2) + (((float) i6) * (fArr2[i10] - 1.0f))) : length2;
                float f3 = (float) length2;
                float f4 = (float) i6;
                keyButton.layout(i11, i7, (int) (f3 + (fArr2[i10] * f4)), i7 + i4);
                length2 = (int) (f3 + (f4 * fArr2[i10]) + ((float) i3));
                i9++;
                i10++;
                i6 = i2;
            }
            i7 += i5 + i4;
            i8++;
            i6 = i2;
        }
    }

    private void calcAndStartShowPreviewAnim(View view) {
        if (view instanceof KeyButton) {
            this.mPreviewText.setText(((KeyButton) view).getText());
            this.mPreviewText.setTypeface(Typeface.DEFAULT_BOLD);
            this.mPopupViewWidth = (int) this.mContext.getResources().getDimension(C0012R$dimen.keyboard_key_preview_radius);
            this.mPopupViewHeight = (int) this.mContext.getResources().getDimension(C0012R$dimen.keyboard_key_preview_radius);
            this.mPreviewText.setWidth(this.mPopupViewWidth);
            this.mPreviewText.setHeight(this.mPopupViewHeight);
            float[] fArr = new float[2];
            getChildCoordRelativeToKeyboard(view, fArr, false, true);
            float f = fArr[0];
            int width = view.getWidth();
            int i = this.mPopupViewWidth;
            int i2 = (int) (f + ((float) ((width - i) / 2)));
            this.mPopupViewX = i2;
            if (i2 < 0) {
                this.mPopupViewX = 4;
            } else if (i2 + i > this.mLetterBoard.getWidth()) {
                this.mPopupViewX = (this.mLetterBoard.getWidth() - this.mPopupViewWidth) - 4;
            }
            this.mPopupViewY = (int) ((fArr[1] - ((float) this.mPopupViewHeight)) - (((float) view.getHeight()) * 0.13f));
            showPreviewAnim(true);
            this.mPreviewText.setVisibility(0);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void showPreviewAnim(boolean z) {
        this.mShowPreviewAnimator.cancel();
        removeCallbacks(this.mConfirmHide);
        this.mShowPreviewAnimator.removeAllListeners();
        this.mShowPreviewAnimator.removeAllUpdateListeners();
        if (z) {
            this.mShowPreviewAnimator.setFloatValues(0.0f, 1.0f);
        } else {
            this.mShowPreviewAnimator.setFloatValues(1.0f, 0.0f);
        }
        this.mShowPreviewAnimator.setDuration(100L);
        this.mPreviewText.setVisibility(0);
        this.mPreviewText.setPivotX(((float) this.mPopupViewWidth) * 0.5f);
        this.mPreviewText.setPivotY((float) this.mPopupViewHeight);
        this.mShowPreviewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.keyguard.widget.MiuiKeyBoardView.AnonymousClass3 */

            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                MiuiKeyBoardView.this.mPreviewText.setAlpha(((Float) valueAnimator.getAnimatedValue()).floatValue());
            }
        });
        this.mShowPreviewAnimator.start();
        this.mIsShowingPreview = z;
        if (z) {
            this.mShowPreviewLastTime = System.currentTimeMillis();
        }
    }

    private void showNumberBoard() {
        this.mNumberBoard.setVisibility(0);
        this.mLetterBoard.setVisibility(4);
        this.mSymbolBoard.setVisibility(4);
    }

    private void showLetterBoard(boolean z) {
        int i = 0;
        this.mLetterBoard.setVisibility(z ? 0 : 4);
        FrameLayout frameLayout = this.mSymbolBoard;
        if (z) {
            i = 4;
        }
        frameLayout.setVisibility(i);
        this.mNumberBoard.setVisibility(4);
    }

    private void shiftSymbolLock() {
        boolean z = !this.mIsSymbolLock;
        this.mIsSymbolLock = z;
        if (z) {
            this.mBtnSymbolLock.setBackgroundResource(C0013R$drawable.keyboard_lock_pressed_list);
        } else {
            this.mBtnSymbolLock.setBackgroundResource(C0013R$drawable.keyboard_lock_list);
        }
    }

    private void shiftLetterBoard() {
        Iterator<KeyButton> it = this.mAllKeys.iterator();
        while (it.hasNext()) {
            KeyButton next = it.next();
            if (next.getTag() instanceof String) {
                String str = (String) next.getTag();
                if (str.length() == 1 && Character.isLowerCase(str.toCharArray()[0])) {
                    next.setText(this.mIsUpperMode ? str.toLowerCase() : str.toUpperCase());
                }
            }
        }
        boolean z = !this.mIsUpperMode;
        this.mIsUpperMode = z;
        if (z) {
            this.mBtnCapsLock.setBackgroundResource(C0013R$drawable.keyboard_caps_lock_pressed_list);
        } else {
            this.mBtnCapsLock.setBackgroundResource(C0013R$drawable.keyboard_caps_lock_list);
        }
    }
}

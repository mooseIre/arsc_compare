package com.android.keyguard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

public class BackButton extends Button {
    private BackButtonCallback mBackButtonCallback;

    public interface BackButtonCallback {
        void onBackButtonClicked();
    }

    public BackButton(Context context) {
        this(context, null);
    }

    public BackButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /* access modifiers changed from: protected */
    public void onFinishInflate() {
        super.onFinishInflate();
        setOnClickListener(new View.OnClickListener() {
            /* class com.android.keyguard.BackButton.AnonymousClass1 */

            public void onClick(View view) {
                BackButton.this.takeBackAction();
            }
        });
    }

    public void takeBackAction() {
        BackButtonCallback backButtonCallback = this.mBackButtonCallback;
        if (backButtonCallback != null) {
            backButtonCallback.onBackButtonClicked();
        }
    }

    public void setCallback(BackButtonCallback backButtonCallback) {
        this.mBackButtonCallback = backButtonCallback;
    }
}

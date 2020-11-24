package com.android.keyguard.charge.view;

public interface IChargeAnimationListener {
    void onChargeAnimationDismiss(int i, String str);

    void onChargeAnimationEnd(int i, String str);

    void onChargeAnimationStart(int i);
}

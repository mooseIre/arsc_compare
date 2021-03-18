package com.android.keyguard.AwesomeLockScreenImp;

import android.os.AsyncTask;
import android.util.Log;
import miui.maml.ActionCommand;
import miui.maml.CommandTriggers;
import miui.maml.data.Expression;
import miui.maml.elements.ScreenElement;
import miui.maml.util.Utils;
import org.w3c.dom.Element;

public class UnlockVerifyPasswordCommand extends ActionCommand {
    private Expression mDelayExp;
    private boolean mEnable;
    private Expression mEnableExp;
    private Expression mPasswordExp;
    private CommandTriggers mTriggers;

    public UnlockVerifyPasswordCommand(ScreenElement screenElement, Element element) {
        super(screenElement);
        Expression build = Expression.build(getVariables(), element.getAttribute("password"));
        this.mPasswordExp = build;
        if (build == null) {
            Log.e("UnlockVerifyPasswordCommand", "no password");
        }
        this.mDelayExp = Expression.build(getVariables(), element.getAttribute("unlockDelay"));
        this.mEnableExp = Expression.build(getVariables(), element.getAttribute("enable"));
        Element child = Utils.getChild(element, "Triggers");
        if (child != null) {
            this.mTriggers = new CommandTriggers(child, screenElement);
        }
    }

    /* access modifiers changed from: protected */
    public void doPerform() {
        if (this.mEnable) {
            Expression expression = this.mDelayExp;
            final int evaluate = (int) (expression == null ? 0.0d : expression.evaluate());
            new AsyncTask<Void, Void, Boolean>() {
                /* class com.android.keyguard.AwesomeLockScreenImp.UnlockVerifyPasswordCommand.AnonymousClass1 */

                /* access modifiers changed from: protected */
                public Boolean doInBackground(Void... voidArr) {
                    try {
                        return Boolean.valueOf(UnlockVerifyPasswordCommand.this.getRoot().unlockVerify(UnlockVerifyPasswordCommand.this.mPasswordExp.evaluateStr(), evaluate));
                    } catch (Exception unused) {
                        return Boolean.FALSE;
                    }
                }

                /* access modifiers changed from: protected */
                public void onPostExecute(Boolean bool) {
                    if (UnlockVerifyPasswordCommand.this.mTriggers != null) {
                        UnlockVerifyPasswordCommand.this.mTriggers.onAction(bool.booleanValue() ? "success" : "fail");
                    }
                }
            }.execute(new Void[0]);
            return;
        }
        getRoot().unlocked(null, 0);
    }

    public void init() {
        Expression expression;
        UnlockVerifyPasswordCommand.super.init();
        CommandTriggers commandTriggers = this.mTriggers;
        if (commandTriggers != null) {
            commandTriggers.init();
        }
        boolean z = true;
        if (this.mPasswordExp == null || (((expression = this.mEnableExp) != null && expression.evaluate() <= 0.0d) || getRoot().getPasswordMode() != 1)) {
            z = false;
        }
        this.mEnable = z;
        if (z) {
            getRoot().setCapability(7, false);
        }
    }

    public void finish() {
        UnlockVerifyPasswordCommand.super.finish();
        CommandTriggers commandTriggers = this.mTriggers;
        if (commandTriggers != null) {
            commandTriggers.finish();
        }
    }

    public void pause() {
        UnlockVerifyPasswordCommand.super.pause();
        CommandTriggers commandTriggers = this.mTriggers;
        if (commandTriggers != null) {
            commandTriggers.pause();
        }
    }

    public void resume() {
        UnlockVerifyPasswordCommand.super.resume();
        CommandTriggers commandTriggers = this.mTriggers;
        if (commandTriggers != null) {
            commandTriggers.resume();
        }
    }
}

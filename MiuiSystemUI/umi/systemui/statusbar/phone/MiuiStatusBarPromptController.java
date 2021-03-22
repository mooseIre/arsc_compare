package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.Dependency;
import com.android.systemui.statusbar.CommandQueue;
import com.miui.system.internal.R;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class MiuiStatusBarPromptController implements CommandQueue.Callbacks {
    protected Context mContext;
    protected SystemUIPromptState mCurrentPromptState;
    protected LinkedHashMap<String, SystemUIPromptState> mMiuiStatusBarStates = new LinkedHashMap<>();
    protected HashMap<FrameLayout, Integer> mPromptContainers = new HashMap<>();

    public MiuiStatusBarPromptController(Context context) {
        this.mContext = context;
        ((CommandQueue) Dependency.get(CommandQueue.class)).addCallback((CommandQueue.Callbacks) this);
    }

    @Override // com.android.systemui.statusbar.CommandQueue.Callbacks
    public void setStatus(int i, String str, Bundle bundle) {
        String string;
        if (str == null) {
            return;
        }
        if (!str.startsWith("action_set_status_bar_state") && !str.startsWith("action_clear_status_bar_state")) {
            return;
        }
        if (bundle == null && i == 0 && str.startsWith("action_set_status_bar_state")) {
            int lastIndexOf = str.lastIndexOf(46);
            if (lastIndexOf >= 0 && lastIndexOf < str.length() - 1) {
                removeMiuiStatusBarState(str.substring(lastIndexOf + 1));
            }
        } else if (bundle != null && (string = bundle.getString("key_status_bar_tag")) != null) {
            if (str.startsWith("action_clear_status_bar_state")) {
                removeMiuiStatusBarState(string);
            } else if (str.startsWith("action_set_status_bar_state")) {
                try {
                    int i2 = bundle.getInt("key_status_bar_priority", -1);
                    if (i2 > 3) {
                        return;
                    }
                    if (i2 >= 0) {
                        RemoteViews remoteViews = (RemoteViews) bundle.getParcelable("key_status_bar_mini_state");
                        if (remoteViews != null) {
                            addMiuiStatusBarState(string, new SystemUIPromptState(string, null, remoteViews, i2));
                        }
                    }
                } catch (Exception e) {
                    Log.e("MiuiStatusBarPromptController", "setStatus: ", e);
                }
            }
        }
    }

    public void addPromptContainer(FrameLayout frameLayout, int i) {
        if (frameLayout != null) {
            frameLayout.removeAllViews();
            LayoutInflater.from(this.mContext).inflate(C0017R$layout.miui_status_bar_prompt, (ViewGroup) frameLayout, true);
            this.mPromptContainers.put(frameLayout, Integer.valueOf(i));
            updateMiuiStatusBarPrompt(frameLayout, i);
        }
    }

    public void removePromptContainer(FrameLayout frameLayout) {
        if (frameLayout != null) {
            frameLayout.removeAllViews();
            this.mPromptContainers.remove(frameLayout);
        }
    }

    /* access modifiers changed from: protected */
    public void addMiuiStatusBarState(String str, SystemUIPromptState systemUIPromptState) {
        this.mMiuiStatusBarStates.put(str, systemUIPromptState);
        updateMiuiStatusBarPrompt();
    }

    /* access modifiers changed from: protected */
    public void removeMiuiStatusBarState(String str) {
        this.mMiuiStatusBarStates.remove(str);
        updateMiuiStatusBarPrompt();
    }

    /* access modifiers changed from: protected */
    public void updateMiuiStatusBarPrompt() {
        int i = 0;
        SystemUIPromptState systemUIPromptState = null;
        for (Map.Entry<String, SystemUIPromptState> entry : this.mMiuiStatusBarStates.entrySet()) {
            SystemUIPromptState value = entry.getValue();
            int i2 = value.mPriority;
            if (i2 >= i) {
                systemUIPromptState = value;
                i = i2;
            }
        }
        if (this.mCurrentPromptState != systemUIPromptState) {
            this.mCurrentPromptState = systemUIPromptState;
            for (Map.Entry<FrameLayout, Integer> entry2 : this.mPromptContainers.entrySet()) {
                updateMiuiStatusBarPrompt(entry2.getKey(), entry2.getValue().intValue());
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateMiuiStatusBarPrompt(FrameLayout frameLayout, int i) {
        FrameLayout frameLayout2 = (FrameLayout) frameLayout.findViewById(C0015R$id.mini_state_container);
        ViewGroup viewGroup = null;
        if (this.mCurrentPromptState != null) {
            frameLayout2.removeAllViews();
            try {
                ViewGroup viewGroup2 = (ViewGroup) this.mCurrentPromptState.mMiniStateViews.apply(this.mContext, frameLayout2);
                viewGroup2.setMinimumWidth(this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_prompt_min_width));
                TextView textView = (TextView) viewGroup2.findViewById(R.id.chronometer);
                TextView textView2 = (TextView) viewGroup2.findViewById(16908310);
                textView.setMinimumWidth(this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_prompt_content_min_width));
                textView2.setMinimumWidth(this.mContext.getResources().getDimensionPixelSize(C0012R$dimen.status_bar_prompt_content_min_width));
                textView.setTextAlignment(4);
                textView2.setTextAlignment(4);
                viewGroup = viewGroup2;
            } catch (Exception e) {
                Log.e("MiuiStatusBarPromptController", "updateMiuiStatusBarPrompt: something wrong", e);
            }
            if (viewGroup != null) {
                frameLayout2.addView(viewGroup, new FrameLayout.LayoutParams(-2, -1));
                frameLayout2.setVisibility(0);
                return;
            }
            return;
        }
        frameLayout.setOnClickListener(null);
        frameLayout2.setVisibility(8);
    }
}

package com.android.systemui.media;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.widget.SeekBar;
import androidx.lifecycle.Observer;
import com.android.systemui.C0012R$dimen;
import com.android.systemui.media.SeekBarViewModel;
import kotlin.jvm.internal.Intrinsics;
import org.jetbrains.annotations.NotNull;

/* compiled from: SeekBarObserver.kt */
public final class SeekBarObserver implements Observer<SeekBarViewModel.Progress> {
    private final PlayerViewHolder holder;
    private final int seekBarDefaultMaxHeight;
    private final int seekBarDisabledHeight;

    public SeekBarObserver(@NotNull PlayerViewHolder playerViewHolder) {
        Intrinsics.checkParameterIsNotNull(playerViewHolder, "holder");
        this.holder = playerViewHolder;
        SeekBar seekBar = playerViewHolder.getSeekBar();
        Intrinsics.checkExpressionValueIsNotNull(seekBar, "holder.seekBar");
        Context context = seekBar.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context, "holder.seekBar.context");
        this.seekBarDefaultMaxHeight = context.getResources().getDimensionPixelSize(C0012R$dimen.qs_media_enabled_seekbar_height);
        SeekBar seekBar2 = this.holder.getSeekBar();
        Intrinsics.checkExpressionValueIsNotNull(seekBar2, "holder.seekBar");
        Context context2 = seekBar2.getContext();
        Intrinsics.checkExpressionValueIsNotNull(context2, "holder.seekBar.context");
        this.seekBarDisabledHeight = context2.getResources().getDimensionPixelSize(C0012R$dimen.qs_media_disabled_seekbar_height);
    }

    public void onChanged(@NotNull SeekBarViewModel.Progress progress) {
        Intrinsics.checkParameterIsNotNull(progress, "data");
        int i = 0;
        if (!progress.getEnabled()) {
            SeekBar seekBar = this.holder.getSeekBar();
            Intrinsics.checkExpressionValueIsNotNull(seekBar, "holder.seekBar");
            if (seekBar.getMaxHeight() != this.seekBarDisabledHeight) {
                SeekBar seekBar2 = this.holder.getSeekBar();
                Intrinsics.checkExpressionValueIsNotNull(seekBar2, "holder.seekBar");
                seekBar2.setMaxHeight(this.seekBarDisabledHeight);
            }
            this.holder.getSeekBar().setEnabled(false);
            this.holder.getSeekBar().getThumb().setAlpha(0);
            this.holder.getSeekBar().setProgress(0);
            this.holder.getElapsedTimeView().setText("");
            this.holder.getTotalTimeView().setText("");
            return;
        }
        Drawable thumb = this.holder.getSeekBar().getThumb();
        if (progress.getSeekAvailable()) {
            i = 255;
        }
        thumb.setAlpha(i);
        this.holder.getSeekBar().setEnabled(progress.getSeekAvailable());
        SeekBar seekBar3 = this.holder.getSeekBar();
        Intrinsics.checkExpressionValueIsNotNull(seekBar3, "holder.seekBar");
        if (seekBar3.getMaxHeight() != this.seekBarDefaultMaxHeight) {
            SeekBar seekBar4 = this.holder.getSeekBar();
            Intrinsics.checkExpressionValueIsNotNull(seekBar4, "holder.seekBar");
            seekBar4.setMaxHeight(this.seekBarDefaultMaxHeight);
        }
        Integer elapsedTime = progress.getElapsedTime();
        if (elapsedTime != null) {
            int intValue = elapsedTime.intValue();
            this.holder.getSeekBar().setProgress(intValue);
            this.holder.getElapsedTimeView().setText(DateUtils.formatElapsedTime(((long) intValue) / 1000));
        }
        Integer duration = progress.getDuration();
        if (duration != null) {
            int intValue2 = duration.intValue();
            this.holder.getSeekBar().setMax(intValue2);
            this.holder.getTotalTimeView().setText(DateUtils.formatElapsedTime(((long) intValue2) / 1000));
        }
    }
}

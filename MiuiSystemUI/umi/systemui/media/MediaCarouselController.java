package com.android.systemui.media;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.Log;
import android.util.MathUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.android.systemui.C0015R$id;
import com.android.systemui.C0017R$layout;
import com.android.systemui.media.MediaDataManager;
import com.android.systemui.media.MediaHostStatesManager;
import com.android.systemui.media.PlayerViewHolder;
import com.android.systemui.plugins.ActivityStarter;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.qs.PageIndicator;
import com.android.systemui.statusbar.notification.VisualStabilityManager;
import com.android.systemui.statusbar.notification.VisualStabilityManagerInjector$Companion$Callback;
import com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaCarouselScrollHandler;
import com.android.systemui.statusbar.notification.mediacontrol.MiuiMediaControlPanel;
import com.android.systemui.statusbar.notification.stack.MiuiMediaHeaderView;
import com.android.systemui.statusbar.policy.ConfigurationController;
import com.android.systemui.util.Utils;
import com.android.systemui.util.animation.TransitionLayout;
import com.android.systemui.util.animation.UniqueObjectHostView;
import com.android.systemui.util.animation.UniqueObjectHostViewKt;
import com.android.systemui.util.concurrency.DelayableExecutor;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Provider;
import kotlin.TypeCastException;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.internal.Intrinsics;
import kotlin.jvm.internal.Reflection;
import kotlin.jvm.internal.TypeIntrinsics;
import kotlin.reflect.KDeclarationContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/* compiled from: MediaCarouselController.kt */
public final class MediaCarouselController {
    /* access modifiers changed from: private */
    public final ActivityStarter activityStarter;
    private int carouselMeasureHeight;
    private int carouselMeasureWidth;
    private final MediaCarouselController$configListener$1 configListener = new MediaCarouselController$configListener$1(this);
    /* access modifiers changed from: private */
    public final Context context;
    private int currentCarouselHeight;
    private int currentCarouselWidth;
    private int currentEndLocation = -1;
    private int currentStartLocation = -1;
    private float currentTransitionProgress = 1.0f;
    private boolean currentlyExpanded = true;
    private boolean currentlyShowingOnlyActive;
    private boolean currentlyVisibility;
    private MediaHostState desiredHostState;
    /* access modifiers changed from: private */
    public int desiredLocation = -1;
    private boolean isRtl;
    private final MediaScrollView mediaCarousel;
    /* access modifiers changed from: private */
    public final MediaCarouselScrollHandler mediaCarouselScrollHandler;
    private final ViewGroup mediaContent;
    private final Provider<MiuiMediaControlPanel> mediaControlPanelFactory;
    /* access modifiers changed from: private */
    public final Map<String, MediaData> mediaData = new LinkedHashMap();
    @NotNull
    private final ViewGroup mediaFrame;
    private final MediaHostStatesManager mediaHostStatesManager;
    @NotNull
    private final Map<String, MediaControlPanel> mediaPlayers = new LinkedHashMap();
    /* access modifiers changed from: private */
    public boolean needsReordering;
    private final PageIndicator pageIndicator;
    private boolean playersVisible;
    private View settingsButton;
    private final VisualStabilityManager.Callback visualStabilityCallback;
    private final VisualStabilityManager visualStabilityManager;

    public MediaCarouselController(@NotNull Context context2, @NotNull Provider<MiuiMediaControlPanel> provider, @NotNull VisualStabilityManager visualStabilityManager2, @NotNull MediaHostStatesManager mediaHostStatesManager2, @NotNull ActivityStarter activityStarter2, @NotNull DelayableExecutor delayableExecutor, @NotNull MediaDataFilter mediaDataFilter, @NotNull ConfigurationController configurationController, @NotNull FalsingManager falsingManager) {
        Context context3 = context2;
        Provider<MiuiMediaControlPanel> provider2 = provider;
        VisualStabilityManager visualStabilityManager3 = visualStabilityManager2;
        MediaHostStatesManager mediaHostStatesManager3 = mediaHostStatesManager2;
        ActivityStarter activityStarter3 = activityStarter2;
        MediaDataFilter mediaDataFilter2 = mediaDataFilter;
        ConfigurationController configurationController2 = configurationController;
        Intrinsics.checkParameterIsNotNull(context3, "context");
        Intrinsics.checkParameterIsNotNull(provider2, "mediaControlPanelFactory");
        Intrinsics.checkParameterIsNotNull(visualStabilityManager3, "visualStabilityManager");
        Intrinsics.checkParameterIsNotNull(mediaHostStatesManager3, "mediaHostStatesManager");
        Intrinsics.checkParameterIsNotNull(activityStarter3, "activityStarter");
        DelayableExecutor delayableExecutor2 = delayableExecutor;
        Intrinsics.checkParameterIsNotNull(delayableExecutor2, "executor");
        Intrinsics.checkParameterIsNotNull(mediaDataFilter2, "mediaManager");
        Intrinsics.checkParameterIsNotNull(configurationController2, "configurationController");
        FalsingManager falsingManager2 = falsingManager;
        Intrinsics.checkParameterIsNotNull(falsingManager2, "falsingManager");
        this.context = context3;
        this.mediaControlPanelFactory = provider2;
        this.visualStabilityManager = visualStabilityManager3;
        this.mediaHostStatesManager = mediaHostStatesManager3;
        this.activityStarter = activityStarter3;
        ViewGroup inflateMediaCarousel = inflateMediaCarousel();
        this.mediaFrame = inflateMediaCarousel;
        View requireViewById = inflateMediaCarousel.requireViewById(C0015R$id.media_carousel_scroller);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById, "mediaFrame.requireViewBy….media_carousel_scroller)");
        this.mediaCarousel = (MediaScrollView) requireViewById;
        View requireViewById2 = this.mediaFrame.requireViewById(C0015R$id.media_page_indicator);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById2, "mediaFrame.requireViewBy….id.media_page_indicator)");
        this.pageIndicator = (PageIndicator) requireViewById2;
        this.mediaCarouselScrollHandler = new MiuiMediaCarouselScrollHandler(this.mediaCarousel, this.pageIndicator, delayableExecutor2, new Function0<Unit>(mediaDataFilter2) {
            public final String getName() {
                return "onSwipeToDismiss";
            }

            public final KDeclarationContainer getOwner() {
                return Reflection.getOrCreateKotlinClass(MediaDataFilter.class);
            }

            public final String getSignature() {
                return "onSwipeToDismiss()V";
            }

            public final void invoke() {
                ((MediaDataFilter) this.receiver).onSwipeToDismiss();
            }
        }, new Function0<Unit>(this) {
            public final String getName() {
                return "updatePageIndicatorLocation";
            }

            public final KDeclarationContainer getOwner() {
                return Reflection.getOrCreateKotlinClass(MediaCarouselController.class);
            }

            public final String getSignature() {
                return "updatePageIndicatorLocation()V";
            }

            public final void invoke() {
                ((MediaCarouselController) this.receiver).updatePageIndicatorLocation();
            }
        }, falsingManager2);
        Resources resources = this.context.getResources();
        Intrinsics.checkExpressionValueIsNotNull(resources, "context.resources");
        Configuration configuration = resources.getConfiguration();
        Intrinsics.checkExpressionValueIsNotNull(configuration, "context.resources.configuration");
        setRtl(configuration.getLayoutDirection() == 1);
        inflateSettingsButton();
        View requireViewById3 = this.mediaCarousel.requireViewById(C0015R$id.media_carousel);
        Intrinsics.checkExpressionValueIsNotNull(requireViewById3, "mediaCarousel.requireViewById(R.id.media_carousel)");
        this.mediaContent = (ViewGroup) requireViewById3;
        configurationController2.addCallback(this.configListener);
        AnonymousClass3 r2 = new VisualStabilityManager.Callback(this) {
            final /* synthetic */ MediaCarouselController this$0;

            {
                this.this$0 = r1;
            }

            public final void onChangeAllowed() {
                if (this.this$0.needsReordering) {
                    this.this$0.needsReordering = false;
                    this.this$0.reorderAllPlayers();
                }
                this.this$0.mediaCarouselScrollHandler.scrollToStart();
            }
        };
        this.visualStabilityCallback = r2;
        this.visualStabilityManager.addReorderingAllowedCallback(r2, true);
        this.visualStabilityManager.injector.addPanelVisibilityChangedCallback(new VisualStabilityManagerInjector$Companion$Callback(this) {
            final /* synthetic */ MediaCarouselController this$0;

            {
                this.this$0 = r1;
            }

            public void onVisibilityChanged(boolean z) {
                this.this$0.setCurrentlyVisibility(z);
            }
        });
        mediaDataFilter2.addListener(new MediaDataManager.Listener(this) {
            final /* synthetic */ MediaCarouselController this$0;

            {
                this.this$0 = r1;
            }

            public void onMediaDataLoaded(@NotNull String str, @Nullable String str2, @NotNull MediaData mediaData) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                Intrinsics.checkParameterIsNotNull(mediaData, "data");
                if (str2 != null) {
                    MediaData mediaData2 = (MediaData) this.this$0.mediaData.remove(str2);
                }
                if (mediaData.getActive() || Utils.useMediaResumption(this.this$0.context)) {
                    this.this$0.mediaData.put(str, mediaData);
                    this.this$0.addOrUpdatePlayer(str, str2, mediaData);
                    return;
                }
                onMediaDataRemoved(str);
            }

            public void onMediaDataRemoved(@NotNull String str) {
                Intrinsics.checkParameterIsNotNull(str, "key");
                this.this$0.mediaData.remove(str);
                this.this$0.removePlayer(str);
            }
        });
        this.mediaFrame.addOnLayoutChangeListener(new View.OnLayoutChangeListener(this) {
            final /* synthetic */ MediaCarouselController this$0;

            {
                this.this$0 = r1;
            }

            public final void onLayoutChange(View view, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8) {
                this.this$0.updatePageIndicatorLocation();
            }
        });
        this.mediaHostStatesManager.addCallback(new MediaHostStatesManager.Callback(this) {
            final /* synthetic */ MediaCarouselController this$0;

            {
                this.this$0 = r1;
            }

            public void onHostStateChanged(int i, @NotNull MediaHostState mediaHostState) {
                Intrinsics.checkParameterIsNotNull(mediaHostState, "mediaHostState");
                if (i == this.this$0.desiredLocation) {
                    MediaCarouselController mediaCarouselController = this.this$0;
                    MediaCarouselController.onDesiredLocationChanged$default(mediaCarouselController, mediaCarouselController.desiredLocation, mediaHostState, false, 0, 0, 24, (Object) null);
                }
            }
        });
    }

    @NotNull
    public final ViewGroup getMediaFrame() {
        return this.mediaFrame;
    }

    /* access modifiers changed from: private */
    public final void setRtl(boolean z) {
        if (z != this.isRtl) {
            this.isRtl = z;
            this.mediaFrame.setLayoutDirection(z ? 1 : 0);
            this.mediaCarouselScrollHandler.scrollToStart();
        }
    }

    private final void setCurrentlyExpanded(boolean z) {
        if (this.currentlyExpanded != z) {
            this.currentlyExpanded = z;
            for (MediaControlPanel listening : this.mediaPlayers.values()) {
                listening.setListening(this.currentlyExpanded && this.currentlyVisibility);
            }
        }
    }

    /* access modifiers changed from: private */
    public final void setCurrentlyVisibility(boolean z) {
        if (this.currentlyVisibility != z) {
            this.currentlyVisibility = z;
            for (MediaControlPanel listening : this.mediaPlayers.values()) {
                listening.setListening(this.currentlyExpanded && this.currentlyVisibility);
            }
        }
    }

    /* access modifiers changed from: private */
    public final void inflateSettingsButton() {
        View inflate = LayoutInflater.from(this.context).inflate(C0017R$layout.media_carousel_settings_button, this.mediaFrame, false);
        if (inflate != null) {
            View view = this.settingsButton;
            if (view != null) {
                ViewGroup viewGroup = this.mediaFrame;
                if (view != null) {
                    viewGroup.removeView(view);
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                    throw null;
                }
            }
            this.settingsButton = inflate;
            ViewGroup viewGroup2 = this.mediaFrame;
            if (inflate != null) {
                viewGroup2.addView(inflate);
                this.mediaCarouselScrollHandler.onSettingsButtonUpdated(inflate);
                View view2 = this.settingsButton;
                if (view2 != null) {
                    view2.setOnClickListener(new MediaCarouselController$inflateSettingsButton$2(this));
                } else {
                    Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                    throw null;
                }
            } else {
                Intrinsics.throwUninitializedPropertyAccessException("settingsButton");
                throw null;
            }
        } else {
            throw new TypeCastException("null cannot be cast to non-null type android.view.View");
        }
    }

    private final ViewGroup inflateMediaCarousel() {
        View inflate = LayoutInflater.from(this.context).inflate(C0017R$layout.media_carousel, new UniqueObjectHostView(this.context), false);
        if (inflate != null) {
            ViewGroup viewGroup = (ViewGroup) inflate;
            viewGroup.setLayoutDirection(3);
            return viewGroup;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup");
    }

    /* access modifiers changed from: private */
    public final void reorderAllPlayers() {
        for (MediaControlPanel next : this.mediaPlayers.values()) {
            PlayerViewHolder view = next.getView();
            TransitionLayout player = view != null ? view.getPlayer() : null;
            if (next.isPlaying() && this.mediaContent.indexOfChild(player) != 0) {
                this.mediaContent.removeView(player);
                this.mediaContent.addView(player, 0);
            }
        }
        this.mediaCarouselScrollHandler.onPlayersChanged();
    }

    /* access modifiers changed from: private */
    public final void addOrUpdatePlayer(String str, String str2, MediaData mediaData2) {
        TransitionLayout player;
        TransitionLayout transitionLayout = null;
        if (this.mediaPlayers.get(str2) != null) {
            Map<String, MediaControlPanel> map = this.mediaPlayers;
            if (map != null) {
                MediaControlPanel mediaControlPanel = (MediaControlPanel) TypeIntrinsics.asMutableMap(map).remove(str2);
                Map<String, MediaControlPanel> map2 = this.mediaPlayers;
                if (mediaControlPanel == null) {
                    Intrinsics.throwNpe();
                    throw null;
                } else if (map2.put(str, mediaControlPanel) != null) {
                    Log.wtf("MediaCarouselController", "new key " + str + " already exists when migrating from " + str2);
                }
            } else {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.collections.MutableMap<K, V>");
            }
        }
        MediaControlPanel mediaControlPanel2 = this.mediaPlayers.get(str);
        if (mediaControlPanel2 == null) {
            MediaControlPanel mediaControlPanel3 = this.mediaControlPanelFactory.get();
            PlayerViewHolder.Companion companion = PlayerViewHolder.Companion;
            LayoutInflater from = LayoutInflater.from(this.context);
            Intrinsics.checkExpressionValueIsNotNull(from, "LayoutInflater.from(context)");
            mediaControlPanel3.attach(companion.create(from, this.mediaContent));
            Intrinsics.checkExpressionValueIsNotNull(mediaControlPanel3, "existingPlayer");
            MiuiMediaControlPanel miuiMediaControlPanel = (MiuiMediaControlPanel) mediaControlPanel3;
            miuiMediaControlPanel.getMediaViewController().setSizeChangedListener(new MediaCarouselController$addOrUpdatePlayer$2(this));
            this.mediaPlayers.put(str, mediaControlPanel3);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
            PlayerViewHolder view = miuiMediaControlPanel.getView();
            if (!(view == null || (player = view.getPlayer()) == null)) {
                player.setLayoutParams(layoutParams);
            }
            mediaControlPanel3.bind(mediaData2);
            mediaControlPanel3.setListening(this.currentlyExpanded && this.currentlyVisibility);
            updatePlayerToState(mediaControlPanel3, true);
            if (miuiMediaControlPanel.isPlaying()) {
                ViewGroup viewGroup = this.mediaContent;
                PlayerViewHolder view2 = miuiMediaControlPanel.getView();
                if (view2 != null) {
                    transitionLayout = view2.getPlayer();
                }
                viewGroup.addView(transitionLayout, 0);
            } else {
                ViewGroup viewGroup2 = this.mediaContent;
                PlayerViewHolder view3 = miuiMediaControlPanel.getView();
                if (view3 != null) {
                    transitionLayout = view3.getPlayer();
                }
                viewGroup2.addView(transitionLayout);
            }
        } else {
            mediaControlPanel2.bind(mediaData2);
            if (mediaControlPanel2.isPlaying()) {
                ViewGroup viewGroup3 = this.mediaContent;
                PlayerViewHolder view4 = mediaControlPanel2.getView();
                if (viewGroup3.indexOfChild(view4 != null ? view4.getPlayer() : null) != 0) {
                    if (this.visualStabilityManager.isReorderingAllowed()) {
                        ViewGroup viewGroup4 = this.mediaContent;
                        PlayerViewHolder view5 = mediaControlPanel2.getView();
                        viewGroup4.removeView(view5 != null ? view5.getPlayer() : null);
                        ViewGroup viewGroup5 = this.mediaContent;
                        PlayerViewHolder view6 = mediaControlPanel2.getView();
                        if (view6 != null) {
                            transitionLayout = view6.getPlayer();
                        }
                        viewGroup5.addView(transitionLayout, 0);
                    } else {
                        this.needsReordering = true;
                    }
                }
            }
        }
        updatePageIndicator();
        this.mediaCarouselScrollHandler.onPlayersChanged();
        UniqueObjectHostViewKt.setRequiresRemeasuring(this.mediaCarousel, true);
        if (this.mediaPlayers.size() != this.mediaContent.getChildCount()) {
            Log.wtf("MediaCarouselController", "Size of players list and number of views in carousel are out of sync");
        }
    }

    /* access modifiers changed from: private */
    public final void removePlayer(String str) {
        MediaControlPanel remove = this.mediaPlayers.remove(str);
        if (remove != null) {
            this.mediaCarouselScrollHandler.onPrePlayerRemoved(remove);
            ViewGroup viewGroup = this.mediaContent;
            PlayerViewHolder view = remove.getView();
            viewGroup.removeView(view != null ? view.getPlayer() : null);
            remove.onDestroy();
            this.mediaCarouselScrollHandler.onPlayersChanged();
            updatePageIndicator();
        }
    }

    /* access modifiers changed from: private */
    public final void recreatePlayers() {
        this.mediaData.forEach(new MediaCarouselController$recreatePlayers$1(this));
    }

    private final void updatePageIndicator() {
        int childCount = this.mediaContent.getChildCount();
        this.pageIndicator.setNumPages(childCount, -1);
        if (childCount == 1) {
            this.pageIndicator.setLocation(0.0f);
        }
        updatePageIndicatorAlpha();
    }

    public final void setCurrentState(int i, int i2, float f, boolean z) {
        if (i != this.currentStartLocation || i2 != this.currentEndLocation || f != this.currentTransitionProgress || z) {
            this.currentStartLocation = i;
            this.currentEndLocation = i2;
            this.currentTransitionProgress = f;
            for (MediaControlPanel updatePlayerToState : this.mediaPlayers.values()) {
                updatePlayerToState(updatePlayerToState, z);
            }
            maybeResetSettingsCog();
            updatePageIndicatorAlpha();
        }
    }

    private final void updatePageIndicatorAlpha() {
        Map<Integer, MediaHostState> mediaHostStates = this.mediaHostStatesManager.getMediaHostStates();
        MediaHostState mediaHostState = mediaHostStates.get(Integer.valueOf(this.currentEndLocation));
        boolean z = false;
        boolean visible = mediaHostState != null ? mediaHostState.getVisible() : false;
        MediaHostState mediaHostState2 = mediaHostStates.get(Integer.valueOf(this.currentStartLocation));
        if (mediaHostState2 != null) {
            z = mediaHostState2.getVisible();
        }
        float f = 1.0f;
        float f2 = z ? 1.0f : 0.0f;
        float f3 = visible ? 1.0f : 0.0f;
        if (!visible || !z) {
            float f4 = this.currentTransitionProgress;
            if (!visible) {
                f4 = 1.0f - f4;
            }
            f = MathUtils.lerp(f2, f3, MathUtils.constrain(MathUtils.map(0.95f, 1.0f, 0.0f, 1.0f, f4), 0.0f, 1.0f));
        }
        this.pageIndicator.setAlpha(f);
    }

    /* access modifiers changed from: private */
    public final void updatePageIndicatorLocation() {
        int i;
        int i2;
        if (this.isRtl) {
            i2 = this.pageIndicator.getWidth();
            i = this.currentCarouselWidth;
        } else {
            i2 = this.currentCarouselWidth;
            i = this.pageIndicator.getWidth();
        }
        this.pageIndicator.setTranslationX((((float) (i2 - i)) / 2.0f) + this.mediaCarouselScrollHandler.getContentTranslation());
        ViewGroup.LayoutParams layoutParams = this.pageIndicator.getLayoutParams();
        if (layoutParams != null) {
            PageIndicator pageIndicator2 = this.pageIndicator;
            pageIndicator2.setTranslationY((float) ((this.currentCarouselHeight - pageIndicator2.getHeight()) - ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin));
            return;
        }
        throw new TypeCastException("null cannot be cast to non-null type android.view.ViewGroup.MarginLayoutParams");
    }

    /* access modifiers changed from: private */
    public final void updateCarouselDimensions() {
        int i = 0;
        int i2 = 0;
        for (MediaControlPanel mediaViewController : this.mediaPlayers.values()) {
            MediaViewController mediaViewController2 = mediaViewController.getMediaViewController();
            Intrinsics.checkExpressionValueIsNotNull(mediaViewController2, "mediaPlayer.mediaViewController");
            i = Math.max(i, mediaViewController2.getCurrentWidth() + ((int) mediaViewController2.getTranslationX()));
            i2 = Math.max(i2, mediaViewController2.getCurrentHeight() + ((int) mediaViewController2.getTranslationY()));
        }
        if (i != this.currentCarouselWidth || i2 != this.currentCarouselHeight) {
            this.currentCarouselWidth = i;
            this.currentCarouselHeight = i2;
            this.mediaCarouselScrollHandler.setCarouselBounds(i + (MiuiMediaHeaderView.Companion.getMSidePaddings() * 2), this.currentCarouselHeight);
            updatePageIndicatorLocation();
        }
    }

    private final void maybeResetSettingsCog() {
        Map<Integer, MediaHostState> mediaHostStates = this.mediaHostStatesManager.getMediaHostStates();
        MediaHostState mediaHostState = mediaHostStates.get(Integer.valueOf(this.currentEndLocation));
        boolean showsOnlyActiveMedia = mediaHostState != null ? mediaHostState.getShowsOnlyActiveMedia() : true;
        MediaHostState mediaHostState2 = mediaHostStates.get(Integer.valueOf(this.currentStartLocation));
        boolean showsOnlyActiveMedia2 = mediaHostState2 != null ? mediaHostState2.getShowsOnlyActiveMedia() : showsOnlyActiveMedia;
        if (this.currentlyShowingOnlyActive == showsOnlyActiveMedia) {
            float f = this.currentTransitionProgress;
            if (f == 1.0f || f == 0.0f || showsOnlyActiveMedia2 == showsOnlyActiveMedia) {
                return;
            }
        }
        this.currentlyShowingOnlyActive = showsOnlyActiveMedia;
        this.mediaCarouselScrollHandler.resetTranslation(true);
    }

    private final void updatePlayerToState(MediaControlPanel mediaControlPanel, boolean z) {
        mediaControlPanel.getMediaViewController().setCurrentState(this.currentStartLocation, this.currentEndLocation, this.currentTransitionProgress, z);
    }

    public static /* synthetic */ void onDesiredLocationChanged$default(MediaCarouselController mediaCarouselController, int i, MediaHostState mediaHostState, boolean z, long j, long j2, int i2, Object obj) {
        mediaCarouselController.onDesiredLocationChanged(i, mediaHostState, z, (i2 & 8) != 0 ? 200 : j, (i2 & 16) != 0 ? 0 : j2);
    }

    public final void onDesiredLocationChanged(int i, @Nullable MediaHostState mediaHostState, boolean z, long j, long j2) {
        if (mediaHostState != null) {
            this.desiredLocation = i;
            this.desiredHostState = mediaHostState;
            setCurrentlyExpanded(mediaHostState.getExpansion() > ((float) 0));
            for (MediaControlPanel next : this.mediaPlayers.values()) {
                if (z) {
                    next.getMediaViewController().animatePendingStateChange(j, j2);
                }
                next.getMediaViewController().onLocationPreChange(i);
            }
            this.mediaCarouselScrollHandler.setShowsSettingsButton(!mediaHostState.getShowsOnlyActiveMedia());
            this.mediaCarouselScrollHandler.setFalsingProtectionNeeded(mediaHostState.getFalsingProtectionNeeded());
            boolean visible = mediaHostState.getVisible();
            if (visible != this.playersVisible) {
                this.playersVisible = visible;
                if (visible) {
                    MediaCarouselScrollHandler.resetTranslation$default(this.mediaCarouselScrollHandler, false, 1, (Object) null);
                }
            }
            updateCarouselSize();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:2:0x0005, code lost:
        r0 = r0.getMeasurementInput();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void updateCarouselSize() {
        /*
            r6 = this;
            com.android.systemui.media.MediaHostState r0 = r6.desiredHostState
            r1 = 0
            if (r0 == 0) goto L_0x0010
            com.android.systemui.util.animation.MeasurementInput r0 = r0.getMeasurementInput()
            if (r0 == 0) goto L_0x0010
            int r0 = r0.getWidth()
            goto L_0x0011
        L_0x0010:
            r0 = r1
        L_0x0011:
            com.android.systemui.media.MediaHostState r2 = r6.desiredHostState
            if (r2 == 0) goto L_0x0020
            com.android.systemui.util.animation.MeasurementInput r2 = r2.getMeasurementInput()
            if (r2 == 0) goto L_0x0020
            int r2 = r2.getHeight()
            goto L_0x0021
        L_0x0020:
            r2 = r1
        L_0x0021:
            int r3 = r6.carouselMeasureWidth
            if (r0 == r3) goto L_0x0027
            if (r0 != 0) goto L_0x002d
        L_0x0027:
            int r3 = r6.carouselMeasureHeight
            if (r2 == r3) goto L_0x006c
            if (r2 == 0) goto L_0x006c
        L_0x002d:
            r6.carouselMeasureWidth = r0
            r6.carouselMeasureHeight = r2
            com.android.systemui.statusbar.notification.stack.MiuiMediaHeaderView$Companion r2 = com.android.systemui.statusbar.notification.stack.MiuiMediaHeaderView.Companion
            int r2 = r2.getMSidePaddings()
            int r0 = r0 + r2
            int r2 = r6.carouselMeasureWidth
            com.android.systemui.statusbar.notification.stack.MiuiMediaHeaderView$Companion r3 = com.android.systemui.statusbar.notification.stack.MiuiMediaHeaderView.Companion
            int r3 = r3.getMSidePaddings()
            int r3 = r3 * 2
            int r2 = r2 + r3
            r3 = 1073741824(0x40000000, float:2.0)
            int r3 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r3)
            com.android.systemui.media.MediaHostState r4 = r6.desiredHostState
            if (r4 == 0) goto L_0x0058
            com.android.systemui.util.animation.MeasurementInput r4 = r4.getMeasurementInput()
            if (r4 == 0) goto L_0x0058
            int r4 = r4.getHeightMeasureSpec()
            goto L_0x0059
        L_0x0058:
            r4 = r1
        L_0x0059:
            com.android.systemui.media.MediaScrollView r5 = r6.mediaCarousel
            r5.measure(r3, r4)
            com.android.systemui.media.MediaScrollView r3 = r6.mediaCarousel
            int r4 = r3.getMeasuredHeight()
            r3.layout(r1, r1, r2, r4)
            com.android.systemui.media.MediaCarouselScrollHandler r6 = r6.mediaCarouselScrollHandler
            r6.setPlayerWidthPlusPadding(r0)
        L_0x006c:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.media.MediaCarouselController.updateCarouselSize():void");
    }
}
